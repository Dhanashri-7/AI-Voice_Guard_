package com.voiceguard.ml

import android.content.Context
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import kotlin.math.*

data class ModelPrediction(
    val aiProbability: Float,
    val isDeepfake: Boolean,
    val confidence: Float,
    val latencyMs: Long,
    val modelSource: String = "VoiceGuard 2D-CNN (TFLite Quantized)"
)

/**
 * On-Device Real-Time Deepfake Voice Classifier using TensorFlow Lite.
 * Model: 2D-CNN trained on ASVspoof 2021 & WaveFake representations.
 * Input: [1, 80, 40, 1] Log-Mel Spectrogram (80 mel bands, 40 frames @ 16kHz).
 * Output: [1, 1] AI Probability (0.0 = Authentic Human, 1.0 = AI Voice Clone).
 */
class TFLiteAudioClassifier(private val context: Context) {

    private var interpreter: Interpreter? = null
    private val TAG = "TFLiteAudioClassifier"
    private val MODEL_FILE = "voiceguard_deepfake_detector.tflite"

    // Audio specification: 16kHz mono
    private val SAMPLE_RATE = 16000
    private val FFT_SIZE = 512
    private val FRAME_LENGTH = 400   // 25 ms
    private val FRAME_STEP = 160     // 10 ms hop
    private val NUM_MEL_BINS = 80
    private val NUM_FRAMES = 40

    // Rolling audio buffer (holds last 8000 samples = 500ms)
    private val rollingBuffer = FloatArray(8000)
    private var rollingBufferFilled = 0

    // Precomputed Mel filterbank matrix: [NUM_MEL_BINS][FFT_SIZE / 2 + 1]
    private val melFilterbank: Array<FloatArray> = createMelFilterbank(
        numBins = NUM_MEL_BINS,
        fftSize = FFT_SIZE,
        sampleRate = SAMPLE_RATE,
        lowFreq = 50f,
        highFreq = 7800f
    )

    // Precomputed Hann window
    private val hannWindow = FloatArray(FRAME_LENGTH) { n ->
        (0.5 * (1.0 - cos(2.0 * Math.PI * n / (FRAME_LENGTH - 1)))).toFloat()
    }

    init {
        loadModel()
    }

    private fun loadModel() {
        try {
            val assetFileDescriptor = context.assets.openFd(MODEL_FILE)
            val inputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
            val fileChannel = inputStream.channel
            val startOffset = assetFileDescriptor.startOffset
            val declaredLength = assetFileDescriptor.declaredLength
            val modelBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)

            val options = Interpreter.Options().apply {
                setNumThreads(2)
                setUseNNAPI(false) // Safe compatibility across all Android architectures
            }
            interpreter = Interpreter(modelBuffer, options)
            Log.d(TAG, "TFLite Model loaded successfully from assets: $MODEL_FILE")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load TFLite model from assets: ${e.message}. Using acoustic heuristic fallback.")
            interpreter = null
        }
    }

    /**
     * Push new incoming PCM 16-bit audio samples into the classifier.
     * Extracts Mel-spectrogram and runs on-device neural inference when sufficient samples are present.
     */
    fun classifyAudio(pcmSamples: ShortArray, readCount: Int): ModelPrediction {
        val startTime = System.currentTimeMillis()

        // 1. Update circular/rolling buffer with normalized float samples [-1.0, 1.0]
        val validCount = readCount.coerceAtMost(pcmSamples.size)
        if (validCount <= 0) {
            return ModelPrediction(0.05f, false, 0.95f, 1, "Idle")
        }

        val shift = validCount.coerceAtMost(rollingBuffer.size)
        System.arraycopy(rollingBuffer, shift, rollingBuffer, 0, rollingBuffer.size - shift)
        var writeIdx = rollingBuffer.size - shift
        for (i in 0 until validCount) {
            if (writeIdx < rollingBuffer.size) {
                rollingBuffer[writeIdx++] = pcmSamples[i].toFloat() / 32768.0f
            }
        }
        rollingBufferFilled = (rollingBufferFilled + validCount).coerceAtMost(rollingBuffer.size)

        // If interpreter is not loaded, return empirical heuristic
        val currentInterpreter = interpreter
        if (currentInterpreter == null) {
            val latency = System.currentTimeMillis() - startTime
            return fallbackHeuristic(pcmSamples, validCount, latency)
        }

        try {
            // 2. Extract Log-Mel Spectrogram: [80 mel_bins, 40 time_frames]
            val melSpec = extractMelSpectrogram(rollingBuffer)

            // 3. Format input buffer: Shape [1, 80, 40, 1]
            val inputBuffer = ByteBuffer.allocateDirect(1 * NUM_MEL_BINS * NUM_FRAMES * 1 * 4)
            inputBuffer.order(ByteOrder.nativeOrder())
            inputBuffer.rewind()

            for (m in 0 until NUM_MEL_BINS) {
                for (t in 0 until NUM_FRAMES) {
                    inputBuffer.putFloat(melSpec[m][t])
                }
            }

            // Output buffer: Shape [1, 1]
            val outputArray = Array(1) { FloatArray(1) }

            // 4. Run TFLite Inference
            currentInterpreter.run(inputBuffer, outputArray)

            val rawAiProb = outputArray[0][0].coerceIn(0.0f, 1.0f)
            val isDeepfake = rawAiProb >= 0.50f
            val confidence = if (isDeepfake) rawAiProb else (1.0f - rawAiProb)
            val latencyMs = (System.currentTimeMillis() - startTime).coerceAtLeast(1)

            return ModelPrediction(
                aiProbability = rawAiProb,
                isDeepfake = isDeepfake,
                confidence = confidence,
                latencyMs = latencyMs,
                modelSource = "VoiceGuard 2D-CNN (.tflite On-Device)"
            )
        } catch (e: Exception) {
            Log.e(TAG, "Inference error: ${e.message}", e)
            val latencyMs = (System.currentTimeMillis() - startTime).coerceAtLeast(1)
            return fallbackHeuristic(pcmSamples, validCount, latencyMs)
        }
    }

    /**
     * Compute 80-band Mel Spectrogram over 40 frames from the audio buffer.
     * Output matrix: mel[80][40]
     */
    private fun extractMelSpectrogram(buffer: FloatArray): Array<FloatArray> {
        val melSpec = Array(NUM_MEL_BINS) { FloatArray(NUM_FRAMES) }
        val halfFft = FFT_SIZE / 2 + 1

        val fftReal = FloatArray(FFT_SIZE)
        val fftImag = FloatArray(FFT_SIZE)
        val powerSpectrum = FloatArray(halfFft)

        for (frameIdx in 0 until NUM_FRAMES) {
            val startSample = (buffer.size - (NUM_FRAMES * FRAME_STEP + FRAME_LENGTH - FRAME_STEP) + (frameIdx * FRAME_STEP)).coerceAtLeast(0)

            // Windowed frame with zero-padding to FFT_SIZE
            fftReal.fill(0f)
            fftImag.fill(0f)
            for (n in 0 until FRAME_LENGTH) {
                val sampleIndex = startSample + n
                if (sampleIndex in buffer.indices) {
                    fftReal[n] = buffer[sampleIndex] * hannWindow[n]
                }
            }

            // Compute in-place FFT
            radix2Fft(fftReal, fftImag, FFT_SIZE)

            // Compute power spectrum: |X[k]|^2 / N
            for (k in 0 until halfFft) {
                val r = fftReal[k]
                val im = fftImag[k]
                powerSpectrum[k] = (r * r + im * im) / FFT_SIZE
            }

            // Apply 80 Mel filterbanks and calculate log energy
            for (m in 0 until NUM_MEL_BINS) {
                var bandEnergy = 0.0f
                val filterWeights = melFilterbank[m]
                for (k in 0 until halfFft) {
                    bandEnergy += powerSpectrum[k] * filterWeights[k]
                }
                // Log compression: log(1.0 + 1000 * bandEnergy)
                val logMel = ln(1.0f + 1000.0f * bandEnergy)
                // Normalize to [0.0, 1.0]
                melSpec[m][frameIdx] = (logMel / 6.0f).coerceIn(0.0f, 1.0f)
            }
        }

        return melSpec
    }

    /**
     * Cooley-Tukey Radix-2 Decimation-In-Time FFT
     */
    private fun radix2Fft(real: FloatArray, imag: FloatArray, n: Int) {
        // Bit-reversal permutation
        var j = 0
        for (i in 0 until n - 1) {
            if (i < j) {
                val tempR = real[i]
                real[i] = real[j]
                real[j] = tempR
                val tempI = imag[i]
                imag[i] = imag[j]
                imag[j] = tempI
            }
            var k = n shr 1
            while (k <= j) {
                j -= k
                k = k shr 1
            }
            j += k
        }

        // Cooley-Tukey stages
        var len = 2
        while (len <= n) {
            val halfLen = len shr 1
            val angle = -2.0 * Math.PI / len
            val wStepR = cos(angle).toFloat()
            val wStepI = sin(angle).toFloat()

            var i = 0
            while (i < n) {
                var wR = 1.0f
                var wI = 0.0f
                for (k in 0 until halfLen) {
                    val uR = real[i + k]
                    val uI = imag[i + k]
                    val vR = real[i + k + halfLen] * wR - imag[i + k + halfLen] * wI
                    val vI = real[i + k + halfLen] * wI + imag[i + k + halfLen] * wR

                    real[i + k] = uR + vR
                    imag[i + k] = uI + vI
                    real[i + k + halfLen] = uR - vR
                    imag[i + k + halfLen] = uI - vI

                    val nextWR = wR * wStepR - wI * wStepI
                    val nextWI = wR * wStepI + wI * wStepR
                    wR = nextWR
                    wI = nextWI
                }
                i += len
            }
            len = len shl 1
        }
    }

    private fun hzToMel(hz: Float): Float {
        return 2595.0f * log10(1.0f + hz / 700.0f)
    }

    private fun melToHz(mel: Float): Float {
        return 700.0f * (10.0f.pow(mel / 2595.0f) - 1.0f)
    }

    private fun createMelFilterbank(
        numBins: Int,
        fftSize: Int,
        sampleRate: Int,
        lowFreq: Float,
        highFreq: Float
    ): Array<FloatArray> {
        val halfFft = fftSize / 2 + 1
        val filterbank = Array(numBins) { FloatArray(halfFft) }

        val minMel = hzToMel(lowFreq)
        val maxMel = hzToMel(highFreq)
        val melPoints = FloatArray(numBins + 2) { i ->
            minMel + (maxMel - minMel) * i / (numBins + 1)
        }
        val binPoints = IntArray(numBins + 2) { i ->
            val hz = melToHz(melPoints[i])
            floor((fftSize + 1) * hz / sampleRate).toInt().coerceIn(0, halfFft - 1)
        }

        for (m in 0 until numBins) {
            val left = binPoints[m]
            val center = binPoints[m + 1]
            val right = binPoints[m + 2]

            for (k in left until center) {
                if (center > left) {
                    filterbank[m][k] = (k - left).toFloat() / (center - left)
                }
            }
            for (k in center until right) {
                if (right > center) {
                    filterbank[m][k] = (right - k).toFloat() / (right - center)
                }
            }
        }
        return filterbank
    }

    private fun fallbackHeuristic(pcmSamples: ShortArray, count: Int, latencyMs: Long): ModelPrediction {
        // High-frequency derivative heuristic fallback
        var hfEnergy = 0.0
        var totalEnergy = 0.0
        for (i in 1 until count) {
            val diff = (pcmSamples[i] - pcmSamples[i - 1]).toDouble()
            val s = pcmSamples[i].toDouble()
            hfEnergy += diff * diff
            totalEnergy += s * s
        }
        val ratio = if (totalEnergy > 0) (hfEnergy / totalEnergy).toFloat() else 0.0f
        val prob = (ratio * 1.5f).coerceIn(0.04f, 0.95f)
        return ModelPrediction(
            aiProbability = prob,
            isDeepfake = prob >= 0.50f,
            confidence = if (prob >= 0.50f) prob else 1.0f - prob,
            latencyMs = latencyMs,
            modelSource = "Empirical Acoustic Fallback"
        )
    }

    fun close() {
        try {
            interpreter?.close()
            interpreter = null
        } catch (e: Exception) {
            Log.w(TAG, "Error closing interpreter: ${e.message}")
        }
    }
}
