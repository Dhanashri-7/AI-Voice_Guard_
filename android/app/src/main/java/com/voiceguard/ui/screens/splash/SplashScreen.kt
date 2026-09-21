package com.voiceguard.ui.screens.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voiceguard.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateNext: () -> Unit
) {
    var telemetryStage by remember { mutableIntStateOf(0) }
    var progressVal by remember { mutableFloatStateOf(0.18f) }

    // Multi-stage Telemetry Simulation
    LaunchedEffect(Unit) {
        delay(600L)
        telemetryStage = 1
        progressVal = 0.52f

        delay(700L)
        telemetryStage = 2
        progressVal = 0.85f

        delay(800L)
        telemetryStage = 3
        progressVal = 1.0f

        delay(600L)
        onNavigateNext()
    }

    // Infinite breathing pulse effect for the Tricolor aura
    val infiniteTransition = rememberInfiniteTransition(label = "tirangaPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val auraAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "auraAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                // Light, pristine government/defense porcelain gradient with subtle soft tint
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF8FAFC), // Pure Pearl
                        Color(0xFFF0F7FF), // Soft Sky Tint
                        Color(0xFFF8FAFC)  // Pure Pearl
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            // ==========================================
            // TOP BADGE: MyBharat & Government Alliance
            // ==========================================
            Box(
                modifier = Modifier
                    .shadow(3.dp, RoundedCornerShape(30.dp), spotColor = Color(0x18000000))
                    .clip(RoundedCornerShape(30.dp))
                    .background(Color.White)
                    .border(
                        1.dp,
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFFFF7A00).copy(alpha = 0.6f), // Saffron
                                Color(0xFFCBD5E1),                     // Silver
                                Color(0xFF16A34A).copy(alpha = 0.6f)  // India Green
                            )
                        ),
                        RoundedCornerShape(30.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 5.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "🇮🇳",
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "BHARAT CYBER DEFENSE",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        letterSpacing = 0.4.sp,
                        maxLines = 1,
                        softWrap = false
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "•",
                        fontSize = 9.sp,
                        color = Color(0xFF94A3B8)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "MY BHARAT ALLIANCE",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1D4ED8),
                        letterSpacing = 0.4.sp,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            // ==========================================
            // CENTER: Creative Aesthetic Combination
            // VoiceGuard Shield + MyBharat Tiranga Motif
            // ==========================================
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(190.dp)
                ) {
                    // Outer Tricolor Glowing Halo Ring
                    Box(
                        modifier = Modifier
                            .size(185.dp)
                            .scale(pulseScale)
                            .alpha(auraAlpha)
                            .clip(CircleShape)
                            .background(
                                Brush.sweepGradient(
                                    colors = listOf(
                                        Color(0x33FF7A00), // Saffron aura
                                        Color(0x22FFFFFF), // White aura
                                        Color(0x3316A34A), // India Green aura
                                        Color(0x221D4ED8), // Royal Blue aura
                                        Color(0x33FF7A00)  // Saffron aura loop
                                    )
                                )
                            )
                            .border(
                                1.5.dp,
                                Brush.sweepGradient(
                                    listOf(
                                        Color(0xFFFF7A00),
                                        Color(0xFFCBD5E1),
                                        Color(0xFF16A34A),
                                        Color(0xFF1D4ED8),
                                        Color(0xFFFF7A00)
                                    )
                                ),
                                CircleShape
                            )
                    )

                    // Secondary Inner Ring
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF8FAFC))
                            .border(1.dp, Color(0xFFE2E8F0), CircleShape)
                    )

                    // Center White Card housing the VoiceGuard Metallic Shield
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(118.dp)
                            .shadow(10.dp, RoundedCornerShape(26.dp), spotColor = Color(0x33000000))
                            .clip(RoundedCornerShape(26.dp))
                            .background(Color.White)
                            .border(
                                1.5.dp,
                                Brush.linearGradient(
                                    listOf(Color(0xFF0F172A), Color(0xFF38BDF8))
                                ),
                                RoundedCornerShape(26.dp)
                            )
                            .padding(10.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.voiceguard_app_logo),
                            contentDescription = "VoiceGuard Shield Logo",
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Floating Overlapping MyBharat Tiranga Seal Badge (Top Right)
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = (-4).dp, y = 4.dp)
                            .size(46.dp)
                            .shadow(6.dp, CircleShape, spotColor = Color(0x33000000))
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(1.5.dp, Color(0xFFFF7A00), CircleShape)
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_mybharat_logo),
                            contentDescription = "MyBharat National Emblem",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Official Dual Brand Header Pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFFEFF6FF))
                        .border(1.dp, Color(0xFFDBEAFE), RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = null,
                        tint = Color(0xFF1D4ED8),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "VOICEGUARD × MERA YUVA BHARAT",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E40AF),
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // App Brand Title (Deep Executive Navy)
                Text(
                    text = "VOICEGUARD",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.5.sp,
                    color = Color(0xFF0F172A),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                // High-Tech Cyber Tagline - centered and aesthetically balanced on a single clean line
                Text(
                    text = "NATIONAL ACOUSTIC DEEPFAKE INTERCEPTOR",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    color = Color(0xFF1D4ED8),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    softWrap = false
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Autonomous On-Device AI • Protecting 1.4 Billion Citizens",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(26.dp))

                // Telemetry Progress Bar & Live Status
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth(0.86f)
                ) {
                    LinearProgressIndicator(
                        progress = { progressVal },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = Color(0xFF1D4ED8), // Royal Blue
                        trackColor = Color(0xFFE2E8F0)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (telemetryStage >= 3) Icons.Default.CheckCircle else Icons.Default.Lock,
                            contentDescription = null,
                            tint = if (telemetryStage >= 3) Color(0xFF16A34A) else Color(0xFF1D4ED8),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when (telemetryStage) {
                                0 -> "Synchronizing MyBharat Cyber Grid..."
                                1 -> "Calibrating Neural Vocoder Engine..."
                                2 -> "Securing Telecom Audio Pipeline..."
                                else -> "National Defense Grid Armed & Ready ✓"
                            },
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (telemetryStage >= 3) Color(0xFF15803D) else Color(0xFF334155)
                        )
                    }
                }
            }

            // ==========================================
            // BOTTOM: Trust Credentials & SIH 2026 Footer
            // ==========================================
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                // Trust Badges Pill Row (Crisp Government Cards)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LightTrustChip(label = "Mera Yuva Bharat", icon = Icons.Default.Verified, tint = Color(0xFFFF7A00))
                    LightTrustChip(label = "DPDP Act 2023", icon = Icons.Default.Shield, tint = Color(0xFF1D4ED8))
                    LightTrustChip(label = "100% Offline AI", icon = Icons.Default.Lock, tint = Color(0xFF16A34A))
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "SMART INDIA HACKATHON 2026 • GRAND FINALE",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = Color(0xFF0F172A),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Ministry of Home Affairs (MHA) & Ministry of Youth Affairs Alliance",
                    fontSize = 9.sp,
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun LightTrustChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color
) {
    Box(
        modifier = Modifier
            .shadow(2.dp, RoundedCornerShape(8.dp), spotColor = Color(0x15000000))
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(10.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = label,
                fontSize = 8.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF334155),
                maxLines = 1,
                softWrap = false
            )
        }
    }
}
