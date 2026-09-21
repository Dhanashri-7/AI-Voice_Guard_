"""
VoiceGuard ML Engine - Audio Preprocessing & Feature Extraction
Supports:
- 8kHz / 16kHz telephony audio
- Sliding window chunking (1-2 seconds)
- Log-Mel Spectrogram, MFCC, Chroma, Spectral Centroid, ZCR
- Prosody & F0 Pitch Dynamics
- Telephony degradation simulation (GSM codec, 8kHz downsample, noise, packet-loss)
"""

import numpy as np
import scipy.signal
from typing import Dict, Tuple, Optional

class AudioProcessor:
    def __init__(
        self,
        sample_rate: int = 16000,
        n_fft: int = 1024,
        hop_length: int = 256,
        n_mels: int = 80,
        n_mfcc: int = 20
    ):
        self.sample_rate = sample_rate
        self.n_fft = n_fft
        self.hop_length = hop_length
        self.n_mels = n_mels
        self.n_mfcc = n_mfcc
        self.mel_basis = self._create_mel_basis(
            sr=sample_rate,
            n_fft=n_fft,
            n_mels=n_mels,
            fmin=50.0,
            fmax=sample_rate / 2.0
        )

    def _hz_to_mel(self, hz: np.ndarray) -> np.ndarray:
        return 2595.0 * np.log10(1.0 + hz / 700.0)

    def _mel_to_hz(self, mel: np.ndarray) -> np.ndarray:
        return 700.0 * (10.0 ** (mel / 2595.0) - 1.0)

    def _create_mel_basis(self, sr: int, n_fft: int, n_mels: int, fmin: float, fmax: float) -> np.ndarray:
        """Constructs a triangular filter bank for Mel spectrogram calculation."""
        weights = np.zeros((n_mels, int(1 + n_fft // 2)), dtype=np.float32)
        fftfreqs = np.linspace(0, sr / 2, int(1 + n_fft // 2))

        min_mel = self._hz_to_mel(np.array([fmin]))[0]
        max_mel = self._hz_to_mel(np.array([fmax]))[0]
        mels = np.linspace(min_mel, max_mel, n_mels + 2)
        hzs = self._mel_to_hz(mels)

        for i in range(n_mels):
            lower = hzs[i]
            center = hzs[i + 1]
            upper = hzs[i + 2]

            # Up-slope
            up = (fftfreqs - lower) / (center - lower + 1e-8)
            # Down-slope
            down = (upper - fftfreqs) / (upper - center + 1e-8)

            weights[i] = np.maximum(0, np.minimum(up, down))
        return weights

    def compute_spectrogram(self, y: np.ndarray) -> np.ndarray:
        """Computes STFT magnitude spectrogram."""
        if len(y) < self.n_fft:
            y = np.pad(y, (0, self.n_fft - len(y)))
        window = np.hanning(self.n_fft)
        _, _, Zxx = scipy.signal.stft(
            y,
            fs=self.sample_rate,
            window=window,
            nperseg=self.n_fft,
            noverlap=self.n_fft - self.hop_length,
            boundary=None,
            padded=False
        )
        return np.abs(Zxx)

    def compute_mel_spectrogram(self, y: np.ndarray) -> np.ndarray:
        """Computes log-Mel spectrogram."""
        mag = self.compute_spectrogram(y)
        mel_spec = np.dot(self.mel_basis, mag[:self.mel_basis.shape[1], :])
        log_mel = np.log(np.maximum(mel_spec, 1e-6))
        # Normalize to standard range [-1, 1] approximately
        mean = np.mean(log_mel)
        std = np.std(log_mel) + 1e-6
        return (log_mel - mean) / std

    def compute_mfcc(self, y: np.ndarray) -> np.ndarray:
        """Computes MFCC features via DCT-II of log Mel spectrum."""
        log_mel = self.compute_mel_spectrogram(y)
        # Type-II DCT
        num_mels, time_steps = log_mel.shape
        mfcc = np.zeros((self.n_mfcc, time_steps), dtype=np.float32)
        for k in range(self.n_mfcc):
            n = np.arange(num_mels)
            basis = np.cos(np.pi * k * (2 * n + 1) / (2 * num_mels))
            mfcc[k] = np.dot(basis, log_mel)
        return mfcc

    def extract_prosodic_features(self, y: np.ndarray) -> Dict[str, float]:
        """Extracts pitch (F0), energy, and rate dynamics."""
        if len(y) == 0:
            return {"f0_mean": 0.0, "f0_std": 0.0, "energy_rms": 0.0, "zcr_rate": 0.0, "pause_ratio": 0.0}

        # Energy RMS
        frame_size = 512
        frames = [y[i:i + frame_size] for i in range(0, len(y) - frame_size, 256)]
        if not frames:
            frames = [y]
        energies = [np.sqrt(np.mean(f ** 2) + 1e-8) for f in frames]
        energy_rms = float(np.mean(energies))
        
        # Zero Crossing Rate (ZCR)
        zcr = np.mean(np.abs(np.diff(np.sign(y)))) / 2.0

        # Autocorrelation based F0 Pitch tracking
        pitches = []
        min_lag = int(self.sample_rate / 400.0)  # max pitch ~400Hz
        max_lag = int(self.sample_rate / 60.0)   # min pitch ~60Hz
        for frame in frames:
            if len(frame) > max_lag and np.std(frame) > 0.01:
                corr = np.correlate(frame, frame, mode='full')
                corr = corr[len(corr) // 2:]
                peak_idx = np.argmax(corr[min_lag:max_lag]) + min_lag
                pitch_hz = self.sample_rate / peak_idx if peak_idx > 0 else 0
                if 60 <= pitch_hz <= 400:
                    pitches.append(pitch_hz)

        f0_mean = float(np.mean(pitches)) if pitches else 140.0
        f0_std = float(np.std(pitches)) if pitches else 15.0

        # Silence / pause duration detection
        silence_threshold = 0.015
        silent_frames = sum(1 for e in energies if e < silence_threshold)
        pause_ratio = float(silent_frames / max(len(energies), 1))

        return {
            "f0_mean": round(f0_mean, 2),
            "f0_std": round(f0_std, 2),
            "energy_rms": round(energy_rms, 4),
            "zcr_rate": round(float(zcr), 4),
            "pause_ratio": round(pause_ratio, 3)
        }

    def simulate_telephony_degradation(
        self,
        y: np.ndarray,
        downsample_8khz: bool = True,
        add_noise: bool = True,
        snr_db: float = 20.0,
        packet_loss_ratio: float = 0.05
    ) -> np.ndarray:
        """
        Simulates realistic cellular telephony degradation (GSM codec filtering,
        bandpass 300Hz-3400Hz, background noise, packet-loss stutter).
        """
        y_out = np.copy(y).astype(np.float32)

        # Bandpass filter for standard telephone bandwidth (300Hz - 3400Hz)
        nyq = 0.5 * self.sample_rate
        low = 300.0 / nyq
        high = min(3400.0 / nyq, 0.95)
        b, a = scipy.signal.butter(4, [low, high], btype='band')
        y_out = scipy.signal.filtfilt(b, a, y_out)

        # 8kHz telephony decimation & reconstruction
        if downsample_8khz:
            target_sr = 8000
            num_samples = int(len(y_out) * target_sr / self.sample_rate)
            y_8k = scipy.signal.resample(y_out, num_samples)
            # Resample back to standard 16kHz for pipeline uniformity
            y_out = scipy.signal.resample(y_8k, len(y))

        # Add additive acoustic background noise
        if add_noise and snr_db is not None:
            signal_power = np.mean(y_out ** 2) + 1e-8
            noise_power = signal_power / (10 ** (snr_db / 10.0))
            noise = np.random.normal(0, np.sqrt(noise_power), len(y_out))
            y_out += noise.astype(np.float32)

        # Simulate cellular burst packet loss (stutter/dropouts)
        if packet_loss_ratio > 0:
            chunk_size = int(self.sample_rate * 0.03) # 30ms packet drop
            num_chunks = len(y_out) // chunk_size
            for i in range(num_chunks):
                if np.random.rand() < packet_loss_ratio:
                    y_out[i * chunk_size : (i + 1) * chunk_size] = 0.0

        # Peak normalization
        max_val = np.max(np.abs(y_out))
        if max_val > 0:
            y_out = y_out / max_val * 0.95

        return y_out
