package com.voiceguard.ui.screens.privacy

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voiceguard.data.local.PreferencesManager
import com.voiceguard.ui.theme.*
import com.voiceguard.utils.DialogueTurn
import com.voiceguard.utils.ScamBusterEngine
import com.voiceguard.utils.ScamBusterPersona
import com.voiceguard.utils.SpeakerRole
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PrivacyCenterScreen(
    prefs: PreferencesManager,
    onWipeAllData: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val scamBusterEngine = remember { ScamBusterEngine(context) }
    DisposableEffect(Unit) {
        onDispose {
            scamBusterEngine.shutdown()
        }
    }

    var retainAudio by remember { mutableStateOf(prefs.retainRawAudio) }
    var onDeviceOnly by remember { mutableStateOf(prefs.onDeviceOnlyInference) }
    var autoHoneypot by remember { mutableStateOf(prefs.autoDeployHoneypot) }
    var selectedPersona by remember {
        mutableStateOf(
            if (prefs.honeypotPersona == "KAKA") ScamBusterPersona.KAKA else ScamBusterPersona.AAJI
        )
    }
    var showConfirmWipeDialog by remember { mutableStateOf(false) }

    // Multi-turn Honeypot Simulator State
    val scenarios = listOf(
        Pair("🚨 CBI Digital Arrest", "This is Inspector Vikram Rathore from CBI Cyber Cell, New Delhi! You are under digital arrest! Transfer 2.5 Lakhs immediately!"),
        Pair("🔑 Bank OTP Theft", "Your account is blocked! Share the 6-digit OTP code sent to your phone immediately!"),
        Pair("🏥 Family Emergency", "Beta, please help me fast! I met with a car accident, send 50,000 rupees to this hospital UPI fast!")
    )
    var selectedScenarioIndex by remember { mutableStateOf(0) }
    var isSimulatingCall by remember { mutableStateOf(false) }
    var activeDialogueTurns by remember { mutableStateOf<List<DialogueTurn>>(emptyList()) }
    var currentTurnIndex by remember { mutableStateOf(0) }
    var simulationJob by remember { mutableStateOf<Job?>(null) }

    fun stopSimulation() {
        simulationJob?.cancel()
        simulationJob = null
        scamBusterEngine.stopSpeaking()
        isSimulatingCall = false
    }

    fun startSimulation() {
        stopSimulation()
        val scamSnippet = scenarios[selectedScenarioIndex].second
        val turns = scamBusterEngine.generateMultiTurnDialogue(scamSnippet, selectedPersona)
        activeDialogueTurns = turns
        currentTurnIndex = 0
        isSimulatingCall = true

        simulationJob = scope.launch {
            for (i in turns.indices) {
                if (!isSimulatingCall) break
                currentTurnIndex = i
                val turn = turns[i]
                var spokenDone = false
                scamBusterEngine.speakTurn(turn) {
                    spokenDone = true
                }
                // Wait for speech completion or fallback timeout
                var waitCount = 0
                while (!spokenDone && waitCount < 80) {
                    delay(100L)
                    waitCount++
                }
                delay(1200L) // natural conversation pause before next turn
            }
            isSimulatingCall = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .padding(horizontal = 20.dp)
            .verticalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                stopSimulation()
                onBack()
            }) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = GovTextPrimary)
            }
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = "DATA SOVEREIGNTY & DPDP ACT",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = GovNavyPrimary,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Privacy & Consent Center",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = GovTextPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ACTIVE DEFENSE & HONEYPOT BOT
        Text(
            text = "ACTIVE DEFENSE & HONEYPOT BOT",
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold,
            color = GovNavyPrimary,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        ScamBusterHoneypotCard(
            selectedPersona = selectedPersona,
            onPersonaChange = { persona ->
                selectedPersona = persona
                prefs.honeypotPersona = persona.name
                if (isSimulatingCall) startSimulation()
            },
            autoHoneypot = autoHoneypot,
            onAutoHoneypotChange = {
                autoHoneypot = it
                prefs.autoDeployHoneypot = it
            },
            scenarios = scenarios,
            selectedScenarioIndex = selectedScenarioIndex,
            onScenarioSelect = { index ->
                selectedScenarioIndex = index
                if (isSimulatingCall) startSimulation()
            },
            isSimulatingCall = isSimulatingCall,
            activeDialogueTurns = activeDialogueTurns,
            currentTurnIndex = currentTurnIndex,
            onToggleSimulation = {
                if (isSimulatingCall) stopSimulation() else startSimulation()
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "PRIVACY PREFERENCES",
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold,
            color = GovTextSecondary,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Toggle 1: Raw Audio Retention
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(CardBackground)
                .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Retain Raw Audio Waveforms", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = GovTextPrimary)
                    Text(text = "Keep temporary audio files for user playback. Strongly recommended OFF.", fontSize = 10.5.sp, color = GovTextSecondary)
                }
                Switch(
                    checked = retainAudio,
                    onCheckedChange = {
                        retainAudio = it
                        prefs.retainRawAudio = it
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = GovNavyPrimary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Toggle 2: Strict On-Device Inference
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(CardBackground)
                .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Strict On-Device Processing", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = GovTextPrimary)
                    Text(text = "Perform ML inference strictly on mobile edge CPU/NNAPI without internet access.", fontSize = 10.5.sp, color = GovTextSecondary)
                }
                Switch(
                    checked = onDeviceOnly,
                    onCheckedChange = {
                        onDeviceOnly = it
                        prefs.onDeviceOnlyInference = it
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = GovNavyPrimary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "TRANSPARENCY AUDIT",
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold,
            color = GovTextSecondary,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        TransparencyItem(title = "What We Collect", detail = "Incoming caller phone numbers, acoustic feature summaries (e.g. F0 std, Mel variance), and security timestamps.")
        Spacer(modifier = Modifier.height(6.dp))
        TransparencyItem(title = "Why We Collect It", detail = "To compute multi-factor impersonation risk and protect citizens against financial extortion.")
        Spacer(modifier = Modifier.height(6.dp))
        TransparencyItem(title = "Where Processing Happens", detail = "On-Device mobile hardware (TFLite edge neural network).")
        Spacer(modifier = Modifier.height(6.dp))
        TransparencyItem(title = "How Long It Is Retained", detail = "Ephemeral RAM buffers are destroyed immediately upon window completion.")

        Spacer(modifier = Modifier.height(24.dp))

        // ONE-TAP DATA PURGE BUTTON
        Button(
            onClick = { showConfirmWipeDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = StatusThreat)
        ) {
            Icon(imageVector = Icons.Default.DeleteForever, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Delete All My Data (One-Tap Wipe)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
        }

        Spacer(modifier = Modifier.height(90.dp))
    }

    if (showConfirmWipeDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmWipeDialog = false },
            title = { Text("Delete All Stored Data?", color = GovTextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "This will permanently purge all call session logs, incident records, enrolled voice embeddings, and local settings from this device.",
                    color = GovTextSecondary,
                    fontSize = 12.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        stopSimulation()
                        onWipeAllData()
                        prefs.clearAllUserData()
                        showConfirmWipeDialog = false
                        Toast.makeText(context, "All user data purged successfully.", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusThreat)
                ) {
                    Text("Permanently Wipe", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmWipeDialog = false }) {
                    Text("Cancel", color = GovTextSecondary)
                }
            },
            containerColor = CardBackground
        )
    }
}

@Composable
fun TransparencyItem(title: String, detail: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(CardBackground)
            .border(1.dp, CardBorder, RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Column {
            Text(text = title, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = GovNavyPrimary)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = detail, fontSize = 10.5.sp, color = GovTextSecondary, lineHeight = 15.sp)
        }
    }
}

@Composable
private fun ScamBusterHoneypotCard(
    selectedPersona: ScamBusterPersona,
    onPersonaChange: (ScamBusterPersona) -> Unit,
    autoHoneypot: Boolean,
    onAutoHoneypotChange: (Boolean) -> Unit,
    scenarios: List<Pair<String, String>>,
    selectedScenarioIndex: Int,
    onScenarioSelect: (Int) -> Unit,
    isSimulatingCall: Boolean,
    activeDialogueTurns: List<DialogueTurn>,
    currentTurnIndex: Int,
    onToggleSimulation: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)), // Navy Obsidian
        border = BorderStroke(1.5.dp, Color(0xFFD97706).copy(alpha = 0.8f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = selectedPersona.emoji, fontSize = 28.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "SCAMBUSTER AI HONEYPOT",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFFDE68A),
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "स्कॅमरची फिरकी घेणारा AI बॉट",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Surface(
                    color = Color(0xFFD97706),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "DESI BAIT",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Turn scam calls into a cybersecurity honeypot! When VoiceGuard detects an AI Voice Clone during a call, deploy AI Aaji or AI Kaka to engage the scammer in a hilarious, dynamic multi-turn conversation that wastes their time and burns their operational budget.",
                fontSize = 11.5.sp,
                color = Color(0xFFCBD5E1),
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Toggle: Auto-Deploy during Cloned Calls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF1E293B))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Auto-Deploy on Fraud Cloned Calls",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Show 1-tap 'Deploy AI आजी' banner on high-threat calls",
                        fontSize = 10.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
                Switch(
                    checked = autoHoneypot,
                    onCheckedChange = onAutoHoneypotChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFFD97706)
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Persona Selector
            Text(
                text = "CHOOSE DEFAULT HONEYPOT PERSONA:",
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFDE68A),
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Aaji
                Button(
                    onClick = { onPersonaChange(ScamBusterPersona.AAJI) },
                    modifier = Modifier.weight(1f).height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedPersona == ScamBusterPersona.AAJI) Color(0xFFD97706) else Color(0xFF1E293B)
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (selectedPersona == ScamBusterPersona.AAJI) Color(0xFFFDE68A) else Color(0xFF334155)
                    )
                ) {
                    Text(
                        text = "👵 AI आजी (Dadi-ji)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Kaka
                Button(
                    onClick = { onPersonaChange(ScamBusterPersona.KAKA) },
                    modifier = Modifier.weight(1f).height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedPersona == ScamBusterPersona.KAKA) Color(0xFFD97706) else Color(0xFF1E293B)
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (selectedPersona == ScamBusterPersona.KAKA) Color(0xFFFDE68A) else Color(0xFF334155)
                    )
                ) {
                    Text(
                        text = "👴 AI काका (Tau-ji)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Interactive Dual-Voice Call Simulator Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF020617)) // Deep Night
                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "🔊 LIVE DUAL-VOICE CALL SIMULATOR",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF38BDF8)
                        )
                        if (isSimulatingCall) {
                            Surface(
                                color = Color(0xFFDC2626),
                                shape = CircleShape
                            ) {
                                Text(
                                    text = "LIVE AUDIO",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Scenario selector row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        scenarios.forEachIndexed { index, pair ->
                            val isSel = selectedScenarioIndex == index
                            Surface(
                                onClick = { onScenarioSelect(index) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSel) Color(0xFF4338CA) else Color(0xFF1E293B),
                                border = BorderStroke(1.dp, if (isSel) Color(0xFF818CF8) else Color.Transparent)
                            ) {
                                Text(
                                    text = pair.first.split(" ").take(2).joinToString(" "),
                                    fontSize = 8.5.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) Color.White else Color(0xFF94A3B8),
                                    modifier = Modifier.padding(vertical = 6.dp, horizontal = 2.dp),
                                    maxLines = 1,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Multi-turn transcript view
                    if (isSimulatingCall && activeDialogueTurns.isNotEmpty()) {
                        val activeTurn = activeDialogueTurns.getOrNull(currentTurnIndex)
                        if (activeTurn != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (activeTurn.speaker == SpeakerRole.SCAMMER_AI)
                                            Color(0xFF450A0A)
                                        else
                                            Color(0xFF1E1B4B)
                                    )
                                    .border(
                                        1.dp,
                                        if (activeTurn.speaker == SpeakerRole.SCAMMER_AI)
                                            Color(0xFFDC2626)
                                        else
                                            Color(0xFFFDE68A),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Text(
                                        text = activeTurn.speakerDisplayName,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (activeTurn.speaker == SpeakerRole.SCAMMER_AI) Color(0xFFFCA5A5) else Color(0xFFFDE68A)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "\"${activeTurn.speechText}\"",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White,
                                        lineHeight = 16.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Subtitle: ${activeTurn.subtitleText}",
                                        fontSize = 9.5.sp,
                                        color = Color(0xFFCBD5E1),
                                        lineHeight = 13.sp
                                    )
                                }
                            }
                        }
                    } else {
                        // Idle state explanation
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0F172A))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = "💡 Tap the button below to test a live multi-turn phone conversation between the Scammer AI clone voice and ${selectedPersona.displayName}. Both voices will speak out loud through the phone's speaker!",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8),
                                lineHeight = 15.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Start / Stop Simulation Button
                    Button(
                        onClick = onToggleSimulation,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSimulatingCall) Color(0xFFDC2626) else Color(0xFF15803D)
                        )
                    ) {
                        Icon(
                            imageVector = if (isSimulatingCall) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isSimulatingCall) "⏹️ कॉल थांबवा (Stop Simulation)" else "▶️ सुरू करा (Live Dual-Speaker Call Simulation)",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Stats footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "⏱️ वेळ वाया: ०२:४५",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFDE68A)
                )
                Text(
                    text = "🤯 गोंधळ: ९८%",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF87171)
                )
                Text(
                    text = "💰 वाचवले: ₹२.५ लाख",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4ADE80)
                )
            }
        }
    }
}
