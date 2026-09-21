package com.voiceguard.ui.screens.explain

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.voiceguard.ui.theme.*

@Composable
fun ExplainabilityDialog(
    voiceRisk: Float,
    speakerMismatch: Float,
    onDismiss: () -> Unit
) {
    val scrollState = rememberScrollState()

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(CyberDarkSurface)
                .border(1.dp, CyberCardBorder, RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "EXPLAINABLE AI FORENSICS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeuralCyan,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Why Was This Call Flagged?",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // SPECTROGRAM VISUALIZATION WITH HIGHLIGHTED ANOMALOUS BANDS
                Text(
                    text = "ACOUSTIC SPECTROGRAM & ANOMALY HEATMAP",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                AcousticForensicWaveformView(voiceRisk = voiceRisk)

                Spacer(modifier = Modifier.height(16.dp))

                // FORENSIC EVIDENCE CHECKLIST
                Text(
                    text = "FORENSIC EVIDENCE CHECKLIST",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(10.dp))

                EvidenceCheckItem(
                    title = "High-Frequency Synthesis Artifacts",
                    description = "Energy discontinuities detected above 3.2 kHz typical of neural vocoders (HiFi-GAN / WaveGlow).",
                    isFlagged = voiceRisk >= 0.60f
                )

                EvidenceCheckItem(
                    title = "Unnatural Pitch Rigidity (Robotic Monotone)",
                    description = "F0 fundamental frequency standard deviation is abnormally constrained (std = 6.2 Hz).",
                    isFlagged = voiceRisk >= 0.50f
                )

                EvidenceCheckItem(
                    title = "Speaker Similarity Anomaly",
                    description = "Acoustic speaker embedding cosine similarity to enrolled voice profile is only 34%.",
                    isFlagged = speakerMismatch >= 0.50f
                )

                EvidenceCheckItem(
                    title = "Social Engineering Urgency Triggers",
                    description = "Immediate financial extortion phrases identified in dialogue transcript.",
                    isFlagged = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeuralCyan)
                ) {
                    Text(text = "Dismiss", color = CyberDarkBackground, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun EvidenceCheckItem(
    title: String,
    description: String,
    isFlagged: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = if (isFlagged) Icons.Default.Warning else Icons.Default.CheckCircle,
            contentDescription = null,
            tint = if (isFlagged) ThreatCrimson else SafeEmerald,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(16.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isFlagged) ThreatCrimson else TextPrimary
            )
            Text(
                text = description,
                fontSize = 10.sp,
                color = TextSecondary,
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
fun AcousticForensicWaveformView(
    voiceRisk: Float,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "SpectrogramAnimation")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "WavePhase"
    )
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseGlow"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(135.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(CyberDarkBackground)
            .border(1.dp, if (voiceRisk > 0.5f) ThreatCrimson.copy(alpha = 0.5f) else CyberCardBorder, RoundedCornerShape(12.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // 1. Frequency Gridlines (8kHz, 5kHz, 3.2kHz, 300Hz)
            val freqLevels = listOf(0.18f, 0.42f, 0.65f, 0.88f)
            freqLevels.forEach { frac ->
                drawLine(
                    color = Color(0xFF1E293B).copy(alpha = 0.6f),
                    start = Offset(0f, h * frac),
                    end = Offset(w, h * frac),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // 2. Anomaly Highlight Zone (Right side: > 3.2 kHz vocoder synthesis cutoff)
            val cutoffX = w * 0.42f
            if (voiceRisk > 0.5f) {
                // Glowing red heat gradient in anomalous band
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            ThreatCrimson.copy(alpha = 0.35f * pulseGlow),
                            ThreatCrimson.copy(alpha = 0.10f * pulseGlow),
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = h * 0.80f
                    ),
                    topLeft = Offset(cutoffX, 0f),
                    size = Size(w - cutoffX, h * 0.80f)
                )

                // Vertical Cutoff Boundary Line at 3.2 kHz
                drawLine(
                    color = ThreatCrimson.copy(alpha = 0.85f * pulseGlow),
                    start = Offset(cutoffX, 0f),
                    end = Offset(cutoffX, h),
                    strokeWidth = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                )
            }

            // 3. Dynamic Acoustic Spectral FFT Bars
            val numBars = 36
            val barWidth = (w / numBars) * 0.65f
            val barSpacing = w / numBars

            for (i in 0 until numBars) {
                val x = i * barSpacing + (barSpacing - barWidth) / 2f
                val isAnomalousZone = x >= cutoffX && voiceRisk > 0.5f

                // Dynamic height based on frequency harmonics and live phase
                val baseHarmonic = kotlin.math.sin(i * 0.45f + phase).toFloat() * 0.35f + 0.55f
                val jitter = if (isAnomalousZone) {
                    // Synthetic vocoder glitch jitter
                    (kotlin.math.sin(i * 1.8f + phase * 3f).toFloat() * 0.25f)
                } else {
                    (kotlin.math.cos(i * 0.9f - phase).toFloat() * 0.12f)
                }

                val normalizedHeight = (baseHarmonic + jitter).coerceIn(0.12f, 0.92f)
                val barHeight = h * normalizedHeight
                val barTop = h - barHeight

                val barColor = if (isAnomalousZone) {
                    // High-frequency synthetic vocoder energy (Vibrant Crimson Red / Neon Pink)
                    Color(0xFFFF1744).copy(alpha = 0.85f * pulseGlow)
                } else {
                    // Natural lower telephony harmonics (Neon Cyan / Electric Blue)
                    NeuralCyan.copy(alpha = 0.65f)
                }

                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(barColor, barColor.copy(alpha = 0.2f)),
                        startY = barTop,
                        endY = h
                    ),
                    topLeft = Offset(x, barTop),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                )
            }

            // 4. Glowing Audio Synthesis Waveform Path running across the spectrum
            val wavePath = Path()
            val points = 60
            for (p in 0..points) {
                val px = (p / points.toFloat()) * w
                val isAnomaly = px >= cutoffX && voiceRisk > 0.5f

                val py = if (isAnomaly) {
                    // Jagged vocoder phase distortion
                    val jagged = (p % 2 == 0)
                    h * 0.35f + (if (jagged) -12.dp.toPx() else 14.dp.toPx()) * (kotlin.math.sin(p * 0.8f + phase * 2f).toFloat() * 0.7f + 0.3f)
                } else {
                    // Smooth natural voice wave
                    h * 0.60f + kotlin.math.sin(p * 0.35f + phase).toFloat() * 14.dp.toPx()
                }

                if (p == 0) {
                    wavePath.moveTo(px, py)
                } else {
                    wavePath.lineTo(px, py)
                }
            }

            // Draw wave with glowing stroke
            drawPath(
                path = wavePath,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        NeuralCyan,
                        NeuralPurple,
                        if (voiceRisk > 0.5f) ThreatCrimson else NeuralCyan
                    )
                ),
                style = Stroke(
                    width = 2.2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            )
        }

        // Overlay Annotations
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "8.0 kHz (Nyquist)",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted
                )

                if (voiceRisk > 0.5f) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(ThreatCrimson)
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "VOCODER PHASE ANOMALY (> 3.2 kHz)",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "300 Hz (Telephony Base)",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextMuted
                )
                Text(
                    text = "3.2 kHz Cutoff Line ──",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (voiceRisk > 0.5f) ThreatCrimson else TextMuted
                )
            }
        }
    }
}
