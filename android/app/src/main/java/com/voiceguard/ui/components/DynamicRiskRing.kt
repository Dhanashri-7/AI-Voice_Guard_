package com.voiceguard.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voiceguard.domain.model.RiskTier
import com.voiceguard.ui.theme.*

@Composable
fun DynamicRiskRing(
    riskScore: Int,
    riskTier: RiskTier,
    modifier: Modifier = Modifier,
    size: Dp = 190.dp,
    strokeWidth: Dp = 14.dp
) {
    val targetScore = riskScore.coerceIn(0, 100).toFloat()
    val animatedScore by animateFloatAsState(
        targetValue = targetScore,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "RiskScoreAnimation"
    )

    val targetColor = when {
        riskScore >= 80 -> ThreatCrimson
        riskScore >= 60 -> Color(0xFFFF6D00) // Vibrant Orange
        riskScore >= 30 -> CautionAmber
        else -> SafeEmerald
    }

    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 600),
        label = "RiskColorAnimation"
    )

    // Subtle pulse for high risk
    val infiniteTransition = rememberInfiniteTransition(label = "PulseTransition")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    val currentScale = if (riskScore >= 80) pulseScale else 1.0f

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(size * currentScale)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasSize = this.size
            val diameter = minOf(canvasSize.width, canvasSize.height)
            val strokePx = strokeWidth.toPx()
            val radius = (diameter - strokePx) / 2f
            val center = Offset(canvasSize.width / 2f, canvasSize.height / 2f)

            // Background Track
            drawCircle(
                color = CyberDarkSurface,
                radius = radius,
                center = center,
                style = Stroke(width = strokePx)
            )

            // Outer subtle track border
            drawCircle(
                color = CyberCardBorder,
                radius = radius + strokePx / 2f,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )

            // Dynamic Active Arc
            val sweepAngle = (animatedScore / 100f) * 280f
            val startAngle = 130f

            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(animatedColor.copy(alpha = 0.6f), animatedColor),
                    center = center
                ),
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2f, radius * 2f),
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
        }

        // Center Content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "${animatedScore.toInt()}",
                fontSize = 46.sp,
                fontWeight = FontWeight.Black,
                color = TextPrimary,
                letterSpacing = (-1).sp
            )
            Text(
                text = "/ 100",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(2.dp))
            val shortLabel = when (riskTier) {
                RiskTier.CRITICAL_IMPERSONATION -> "CRITICAL RISK"
                RiskTier.SUSPICIOUS -> "SUSPICIOUS"
                RiskTier.CAUTION -> "CAUTION"
                RiskTier.SAFE -> "SAFE"
            }
            Text(
                text = shortLabel,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = animatedColor,
                letterSpacing = 1.sp,
                maxLines = 1
            )
        }
    }
}
