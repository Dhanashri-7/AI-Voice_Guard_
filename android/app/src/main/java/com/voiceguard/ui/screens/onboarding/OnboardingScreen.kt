package com.voiceguard.ui.screens.onboarding

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.voiceguard.R
import com.voiceguard.ui.theme.*

@Composable
fun OnboardingScreen(
    onFinishOnboarding: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var micGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }
    var phoneGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
        )
    }
    var callLogGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED
        )
    }
    var notificationsGranted by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }

    val allPermissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        micGranted = results[Manifest.permission.RECORD_AUDIO] ?: micGranted
        phoneGranted = results[Manifest.permission.READ_PHONE_STATE] ?: phoneGranted
        callLogGranted = results[Manifest.permission.READ_CALL_LOG] ?: callLogGranted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationsGranted = results[Manifest.permission.POST_NOTIFICATIONS] ?: notificationsGranted
        }
        Toast.makeText(context, "VoiceGuard Threat Protection Configured!", Toast.LENGTH_SHORT).show()
        onFinishOnboarding()
    }

    val singleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        micGranted = results[Manifest.permission.RECORD_AUDIO] ?: micGranted
        phoneGranted = results[Manifest.permission.READ_PHONE_STATE] ?: phoneGranted
        callLogGranted = results[Manifest.permission.READ_CALL_LOG] ?: callLogGranted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationsGranted = results[Manifest.permission.POST_NOTIFICATIONS] ?: notificationsGranted
        }
    }

    val isAllGranted = micGranted && phoneGranted && callLogGranted && notificationsGranted

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .padding(20.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.Start
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(CardBackground)
                        .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                        .padding(6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.voiceguard_app_logo),
                        contentDescription = "VoiceGuard",
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "DEVICE PERMISSION SETUP",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GovNavyPrimary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "System Security Access",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = GovTextPrimary
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0xFFDCFCE7))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "STEP 2/2",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = StatusSafe
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "VoiceGuard requires standard system-level access to inspect incoming calls and acoustic harmonics in real time without storing user audio.",
            fontSize = 12.sp,
            color = GovTextSecondary,
            lineHeight = 17.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        // DPDP Act 2023 Compliance Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFFF0FDF4))
                .border(1.dp, Color(0xFFBBF7D0), RoundedCornerShape(14.dp))
                .padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = StatusSafe,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Zero Raw Audio Retention (DPDP Act 2023)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF166534)
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "Audio frames are analyzed ephemerally in RAM on your device. No recordings, voice samples, or transcripts are uploaded to any server.",
                        fontSize = 11.sp,
                        color = Color(0xFF15803D),
                        lineHeight = 15.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Permission Card 1: Microphone
        GovPermissionCard(
            title = "Acoustic Neural Scanner",
            badge = "MICROPHONE",
            icon = Icons.Default.Mic,
            whyNeeded = "Required to calculate 80-bin Mel-spectrograms and detect deepfake vocoder artifacts in volatile RAM during active calls.",
            isGranted = micGranted,
            onGrantClick = {
                singleLauncher.launch(arrayOf(Manifest.permission.RECORD_AUDIO))
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Permission Card 2: Telephony & Call Screening
        GovPermissionCard(
            title = "Telephony Threat Interception",
            badge = "PHONE STATE",
            icon = Icons.Default.PhoneCallback,
            whyNeeded = "Silently detects incoming unknown callers in the background to cross-examine numbers with national fraud registries.",
            isGranted = phoneGranted,
            onGrantClick = {
                singleLauncher.launch(arrayOf(Manifest.permission.READ_PHONE_STATE))
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Permission Card 3: Call Log & Contacts
        GovPermissionCard(
            title = "Trusted Caller Verification",
            badge = "CALL LOGS",
            icon = Icons.Default.ContactPhone,
            whyNeeded = "Distinguishes enrolled trusted family numbers from spoofed virtual IDs to stop emergency impersonation scams.",
            isGranted = callLogGranted,
            onGrantClick = {
                singleLauncher.launch(arrayOf(Manifest.permission.READ_CALL_LOG))
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Permission Card 4: Notifications & Heads-Up Alerts
        GovPermissionCard(
            title = "High-Priority In-Call Alerts",
            badge = "NOTIFICATIONS",
            icon = Icons.Default.NotificationsActive,
            whyNeeded = "Displays instant floating warning HUD and triggers emergency haptic vibration when an AI clone call is detected.",
            isGranted = notificationsGranted,
            onGrantClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    singleLauncher.launch(arrayOf(Manifest.permission.POST_NOTIFICATIONS))
                } else {
                    notificationsGranted = true
                }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Primary Action Button
        Button(
            onClick = {
                val perms = mutableListOf(
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.READ_CALL_LOG
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    perms.add(Manifest.permission.POST_NOTIFICATIONS)
                }
                allPermissionsLauncher.launch(perms.toTypedArray())
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = GovNavyPrimary
            )
        ) {
            Icon(
                imageVector = if (isAllGranted) Icons.Default.CheckCircle else Icons.Default.Shield,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isAllGranted) "Protection Active — Enter Dashboard" else "Grant Permissions & Activate Shield",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.5.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun GovPermissionCard(
    title: String,
    badge: String,
    icon: ImageVector,
    whyNeeded: String,
    isGranted: Boolean,
    onGrantClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CardBackground)
            .border(
                1.dp,
                if (isGranted) Color(0xFFBBF7D0) else CardBorder,
                RoundedCornerShape(14.dp)
            )
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isGranted) Color(0xFFDCFCE7) else Color(0xFFEFF6FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isGranted) StatusSafe else GovNavyPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = title,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = GovTextPrimary
                        )
                        Text(
                            text = badge,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = GovTextMuted
                        )
                    }
                }

                if (isGranted) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFDCFCE7))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = StatusSafe,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "ACTIVE",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = StatusSafe
                            )
                        }
                    }
                } else {
                    Button(
                        onClick = onGrantClick,
                        modifier = Modifier.height(32.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GovNavyPrimary),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                    ) {
                        Text(text = "ENABLE", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = whyNeeded,
                fontSize = 11.sp,
                color = GovTextSecondary,
                lineHeight = 15.sp
            )
        }
    }
}
