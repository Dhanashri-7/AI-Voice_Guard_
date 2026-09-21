package com.voiceguard.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voiceguard.ui.theme.*

@Composable
fun RiskFactorBar(
    label: String,
    value: Float, // [0.0, 1.0]
    weightLabel: String,
    barColor: Color = NeuralCyan,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = value.coerceIn(0f, 1f),
        animationSpec = tween(600),
        label = "FactorBarAnimation"
    )

    Column(modifier = modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${(animatedProgress * 100).toInt()}%",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = barColor
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "($weightLabel)",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(CyberDarkSurface)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedProgress)
                    .clip(RoundedCornerShape(3.dp))
                    .background(barColor)
            )
        }
    }
}

@Composable
fun LiveAudioWaveform(
    isActive: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 18,
    activeColor: Color = NeuralCyan
) {
    val infiniteTransition = rememberInfiniteTransition(label = "WaveformTransition")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "WaveformPhase"
    )

    Canvas(modifier = modifier.fillMaxWidth().height(36.dp)) {
        val totalWidth = size.width
        val barWidth = 4.dp.toPx()
        val spacing = (totalWidth - (barCount * barWidth)) / (barCount - 1)
        val centerY = size.height / 2f

        for (i in 0 until barCount) {
            val normalizedX = i.toFloat() / barCount
            val wave = if (isActive) {
                kotlin.math.sin(phase + i * 0.45f) * 0.5f + 0.5f
            } else {
                0.15f
            }
            val barHeight = (size.height * 0.85f * wave).coerceAtLeast(4.dp.toPx())

            val left = i * (barWidth + spacing)
            val top = centerY - (barHeight / 2f)

            drawRoundRect(
                color = if (isActive) activeColor else TextMuted.copy(alpha = 0.3f),
                topLeft = Offset(left, top),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
            )
        }
    }
}
