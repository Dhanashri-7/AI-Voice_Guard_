package com.voiceguard.ui.screens.call

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.voiceguard.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun VoiceCaptchaDialog(
    callerName: String,
    onDismiss: () -> Unit,
    onHangUpAndBlock: () -> Unit
) {
    var isAnalyzing by remember { mutableStateOf(true) }
    var challengePassed by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(2000L)
        isAnalyzing = false
        challengePassed = false
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(CyberDarkSurface)
                .border(1.dp, if (!isAnalyzing && !challengePassed) ThreatCrimson else CyberCardBorder, RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(NeuralCyan.copy(alpha = 0.2f))
                                .border(1.dp, NeuralCyan, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = NeuralCyan, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ZERO-TRUST CHALLENGE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeuralCyan,
                            letterSpacing = 1.sp
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Reverse Voice-CAPTCHA",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Text(
                    text = "Acoustic liveness and neural synthesis latency test",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // CHALLENGE PROMPT CARD
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CyberDarkBackground)
                        .border(1.dp, SafeEmerald.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Text(
                            text = "ASK CALLER TO REPEAT EXACT PHRASE:",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = SafeEmerald,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "\"Pacific Blue Echo 94\"",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Uncached random token forces generative TTS inference latency",
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // LATENCY & ACOUSTIC METER
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CyberDarkBackground)
                        .padding(14.dp)
                ) {
                    Column {
                        Text(
                            text = "NEURAL INFERENCE LATENCY GAUGE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        if (isAnalyzing) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = NeuralCyan,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Measuring synthesis buffer latency...",
                                    fontSize = 11.sp,
                                    color = NeuralCyan
                                )
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Human Baseline:", fontSize = 11.sp, color = TextSecondary)
                                Text(text = "< 350 ms", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SafeEmerald)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Caller Response Latency:", fontSize = 11.sp, color = TextSecondary)
                                Text(text = "1840 ms (+1490ms delay)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ThreatCrimson)
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(ThreatCrimson.copy(alpha = 0.15f))
                                    .border(1.dp, ThreatCrimson, RoundedCornerShape(6.dp))
                                    .padding(8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Cancel, contentDescription = null, tint = ThreatCrimson, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "CHALLENGE FAILED: Autoregressive TTS delay confirms synthetic voice pipeline.",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ThreatCrimson,
                                        lineHeight = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // ACTIONS
                if (!isAnalyzing) {
                    Button(
                        onClick = onHangUpAndBlock,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ThreatCrimson)
                    ) {
                        Icon(imageVector = Icons.Default.CallEnd, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Disconnect and Blacklist Caller", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                } else {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(text = "Cancel Challenge", color = TextSecondary, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
