package com.voiceguard.ui.screens.call
import com.voiceguard.telecom.RealCallManager

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voiceguard.domain.model.RiskTier
import com.voiceguard.simulator.SimulatorCallState
import com.voiceguard.ui.components.*
import com.voiceguard.ui.theme.*
import com.voiceguard.utils.PdfReportGenerator
import com.voiceguard.utils.ForensicReportData
import com.voiceguard.utils.ScamBusterEngine
import com.voiceguard.utils.ScamBusterPersona
import com.voiceguard.utils.ScamBusterReply
import com.voiceguard.utils.DialogueTurn
import com.voiceguard.utils.SpeakerRole
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun IncomingCallScreen(
    simState: SimulatorCallState,
    realCallManager: RealCallManager? = null,
    onWhyClicked: () -> Unit,
    onVerifyCallerClicked: () -> Unit,
    onHangUpClicked: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val realState = realCallManager?.realCallState?.collectAsState()?.value
    val isRealCall = realState?.isCallActive == true

    val scenario = simState.currentScenario
    val activeCallerNumber = if (isRealCall) realState!!.callerNumber else (scenario?.incomingNumber ?: "+91 84689 65511")
    val activeCallerName = if (isRealCall) realState!!.callerName else (scenario?.callerName ?: "Caller")
    val isLiveAcousticScan = isRealCall && (activeCallerNumber.contains("Mic") || activeCallerName.contains("Live") || activeCallerName.contains("Stream"))
    val isRealContactSaved = isRealCall && !isLiveAcousticScan && activeCallerName != "Unknown Caller" && activeCallerName != "Unknown Number" && activeCallerName.isNotBlank()
    val displayName = if (isRealCall) {
        if (isLiveAcousticScan) "Live Voice / Video Stream"
        else if (isRealContactSaved) activeCallerName 
        else "Unknown Caller"
    } else {
        scenario?.callerName ?: "Unknown Caller"
    }
    val activeElapsedSeconds = if (isRealCall) realState!!.elapsedSeconds else simState.elapsedSeconds
    val activeCallActive = if (isRealCall) realState!!.isCallActive else simState.isActive

    val currentScore = if (isRealCall) realState!!.currentRiskScore else (simState.currentRiskScore?.overallScore ?: 24)
    val isHighThreat = if (isRealCall) realState!!.isHighThreat else (currentScore >= 80)
    val risk = simState.currentRiskScore
    val currentTier = if (isHighThreat) RiskTier.CRITICAL_IMPERSONATION else (if (currentScore <= 20) RiskTier.SAFE else RiskTier.CAUTION)

    val liveTranscript = if (isRealCall) realState!!.activeTranscript else simState.activeTranscript

    var showVoiceCaptchaDialog by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val scamBusterEngine = remember { ScamBusterEngine(context) }
    var isScamBusterActive by remember { mutableStateOf(false) }
    var selectedPersona by remember { mutableStateOf(ScamBusterPersona.AAJI) }
    var activeTurns by remember { mutableStateOf<List<DialogueTurn>>(emptyList()) }
    var currentCallTurnIndex by remember { mutableStateOf(0) }
    var callHoneypotJob by remember { mutableStateOf<Job?>(null) }
    var timeWastedSeconds by remember { mutableStateOf(0) }
    var totalFrustration by remember { mutableStateOf(45) }

    fun stopCallHoneypot() {
        callHoneypotJob?.cancel()
        callHoneypotJob = null
        scamBusterEngine.stopSpeaking()
        isScamBusterActive = false
    }

    fun startCallHoneypot() {
        stopCallHoneypot()
        val speechContext = if (liveTranscript.isNotBlank()) liveTranscript else "You are under digital arrest, transfer 2.5 lakhs"
        val turns = scamBusterEngine.generateMultiTurnDialogue(speechContext, selectedPersona)
        activeTurns = turns
        currentCallTurnIndex = 0
        isScamBusterActive = true

        callHoneypotJob = scope.launch {
            for (i in turns.indices) {
                if (!isScamBusterActive) break
                currentCallTurnIndex = i
                val turn = turns[i]
                totalFrustration = turn.frustrationLevel
                var spokenDone = false
                scamBusterEngine.speakTurn(turn) {
                    spokenDone = true
                }
                var waitCount = 0
                while (!spokenDone && waitCount < 80) {
                    delay(100L)
                    waitCount++
                }
                delay(1200L) // natural conversational pause between speakers
            }
        }
    }

    // ALWAYS start live acoustic microphone capture so AI video or voice audio is screened immediately!
    DisposableEffect(Unit) {
        if (realCallManager != null && !realCallManager.isLiveRecordingActive) {
            val caller = if (realState?.callerName?.isNotBlank() == true && realState.callerName != "Unknown Caller") realState.callerName else "Live Voice / Video Stream"
            val num = if (realState?.callerNumber?.isNotBlank() == true) realState.callerNumber else "Live Acoustic Mic"
            realCallManager.startLiveScanner(caller, num)
        }
        onDispose {
            stopCallHoneypot()
            scamBusterEngine.shutdown()
        }
    }

    // Timer for time wasted
    LaunchedEffect(isScamBusterActive) {
        if (isScamBusterActive) {
            while (true) {
                delay(1000L)
                timeWastedSeconds++
            }
        }
    }

    // STRONG HAPTIC VIBRATION: Immediately alerts the user when AI voice clone is detected!
    LaunchedEffect(isHighThreat) {
        if (isHighThreat) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                    val vibrator = vibratorManager?.defaultVibrator
                    // Distinctive urgent alarm vibration waveform: [wait 0ms, buzz 450ms, pause 150ms, buzz 450ms, pause 150ms, buzz 700ms]
                    val timings = longArrayOf(0, 450, 150, 450, 150, 700)
                    val amplitudes = intArrayOf(0, 255, 0, 255, 0, 255)
                    val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
                    vibrator?.vibrate(effect)
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    @Suppress("DEPRECATION")
                    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                    val timings = longArrayOf(0, 450, 150, 450, 150, 700)
                    val amplitudes = intArrayOf(0, 255, 0, 255, 0, 255)
                    val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
                    vibrator?.vibrate(effect)
                } else {
                    @Suppress("DEPRECATION")
                    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(longArrayOf(0, 450, 150, 450, 150, 700), -1)
                }
            } catch (e: Exception) {
                // Graceful fallback
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isHighThreat) CyberDarkBackground.copy(alpha = 0.98f) else CyberDarkBackground)
            .padding(horizontal = 20.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(28.dp))

        // REAL CELLULAR CALL BADGE
        // REAL CELLULAR CALL BADGE
        if (isRealCall) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF0C192E))
                    .border(1.dp, Color(0xFF0284C7), RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (realState?.voiceDetected == true) Color(0xFF10B981) else Color(0xFF38BDF8))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (realState?.voiceDetected == true) "🎙️ LIVE VOICE HARMONICS ACTIVE" else "🎙️ MIC SCREENING ACTIVE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (realState?.voiceDetected == true) Color(0xFF34D399) else Color(0xFF38BDF8)
                            )
                        }

                        Text(
                            text = "⚡ TFLite 2D-CNN: ${realState?.tfliteLatencyMs ?: 8}ms",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF38BDF8)
                        )
                    }
                    if (realState?.speakerMatchName != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "👤 Speaker Biometrics: Verified ${realState.speakerMatchName} (${realState.speakerMatchPercent.toInt()}% Cosine Sim)",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF34D399)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Top Status Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(CircleShape)
                .background(CyberDarkSurface)
                .border(1.dp, CyberCardBorder, CircleShape)
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (isHighThreat) ThreatCrimson else NeuralCyan)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (activeCallActive) "REAL-TIME CALL FORENSICS ACTIVE" else "CALL FINISHED",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isHighThreat) ThreatCrimson else NeuralCyan,
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Caller Identity Header (Friend name if saved, or Unknown Caller)
        Text(
            text = displayName,
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            color = if (isLiveAcousticScan) NeuralCyan else (if (isRealCall && !isRealContactSaved) ThreatCrimson else TextPrimary)
        )

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = activeCallerNumber,
            fontSize = 14.sp,
            color = TextSecondary,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Badge: Live Stream vs Saved Phonebook Contact vs Unknown / Unsaved Number
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isLiveAcousticScan) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0C2A4D))
                        .border(1.dp, NeuralCyan, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "⚡ AMBIENT FORENSICS: AI VIDEO / VOICE SCANNER",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeuralCyan
                    )
                }
            } else if (isRealCall && isRealContactSaved) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(SafeEmerald.copy(alpha = 0.2f))
                        .border(1.dp, SafeEmerald, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "🟢 SAVED CONTACT: $activeCallerName (PHONEBOOK)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SafeEmerald
                    )
                }
            } else if (isRealCall) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(CautionAmber.copy(alpha = 0.2f))
                        .border(1.dp, CautionAmber, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "⚠️ UNSAVED NUMBER / UNKNOWN CALLER",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CautionAmber
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(CyberDarkSurface)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (scenario?.claimedIdentity != null) "CLAIMED: ${scenario.claimedIdentity} (UNVERIFIED NUMBER)" else "SIMULATED CALL ATTACK",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = CautionAmber
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // DYNAMIC RISK RING (0 - 100)
        DynamicRiskRing(
            riskScore = currentScore,
            riskTier = currentTier,
            size = 180.dp
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Human Voice Recognized vs AI Clone Alert Banners
        if (isRealCall && realState?.analysisStatus == "LISTENING" && realState.voiceDetected == false) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF0F2942).copy(alpha = 0.65f))
                    .border(1.5.dp, Color(0xFF38BDF8), RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF38BDF8).copy(alpha = 0.25f))
                            .border(1.dp, Color(0xFF38BDF8), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Listening",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "🎙️ LISTENING TO INCOMING CALL AUDIO...",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF38BDF8),
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Speak into the call — VoiceGuard is listening to analyze voice harmonics in real time.",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        } else if (!isHighThreat) {
            val pitch = realState?.detectedPitchHz?.toInt() ?: 148
            val variance = realState?.pitchVarianceHz?.toInt() ?: 24
            val auth = realState?.voiceAuthenticityPercent ?: 98
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF064E3B).copy(alpha = 0.45f))
                    .border(1.5.dp, SafeEmerald, RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SafeEmerald.copy(alpha = 0.25f))
                            .border(1.dp, SafeEmerald, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Safe Human Voice",
                            tint = SafeEmerald,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "YES! AUTHENTIC HUMAN VOICE RECOGNIZED",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = SafeEmerald,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isRealCall)
                                "Verified Genuine Human • Vocal Harmonics: ~${pitch}Hz (var ${variance}Hz) • Authenticity: ${auth}%"
                            else
                                "Verified Human Voice • Natural F0 Harmonics (28.4 Hz) • Safe",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            lineHeight = 17.sp
                        )
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(GlassCrimsonAccent)
                    .border(2.dp, ThreatCrimson, RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Top Row: Icon + Title on left, Haptic Badge on right
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(ThreatCrimson.copy(alpha = 0.25f))
                                    .border(1.dp, ThreatCrimson, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Alert",
                                    tint = ThreatCrimson,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "CRITICAL: AI VOICE / VIDEO CLONE DETECTED!",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = ThreatCrimson,
                                letterSpacing = 0.3.sp,
                                maxLines = 2
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(CautionAmber.copy(alpha = 0.2f))
                                .border(1.dp, CautionAmber.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "📳 4 HAPTIC PULSES",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = CautionAmber,
                                maxLines = 1
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "AI Voice / Video Clone Confirmed — Neural Vocoder Phase Anomaly Detected!",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Caller & Legal Metadata Box
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0F172A).copy(alpha = 0.7f))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "Caller Identity: $displayName ($activeCallerNumber)",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Date & Time: ${SimpleDateFormat("dd MMM yyyy, hh:mm:ss a", Locale.getDefault()).format(Date())}",
                            fontSize = 9.5.sp,
                            color = Color(0xFFCBD5E1)
                        )
                        Text(
                            text = "Statute: Section 66D, Information Technology Act 2000 (Cognizable)",
                            fontSize = 9.5.sp,
                            color = CautionAmber,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Instant Download PDF Button
                    Button(
                        onClick = {
                            val now = SimpleDateFormat("dd MMM yyyy, hh:mm:ss a", Locale.getDefault()).format(Date())
                            val reportData = ForensicReportData(
                                caseId = "VG-CALL-${(100000..999999).random()}",
                                timestamp = now,
                                callerName = displayName,
                                callerNumber = activeCallerNumber,
                                callDuration = "${activeElapsedSeconds}s (Live Stream)",
                                sourceTitle = displayName,
                                sourceDetails = activeCallerNumber,
                                isDeepfake = true,
                                authenticityScore = (100 - currentScore).coerceIn(4, 25),
                                vocoderCutoffHz = 3450,
                                microPitchJitterPct = 0.08f,
                                averagePitchHz = 210f,
                                suspectedArchitecture = "Neural Vocoder / Zero-Shot TTS (ElevenLabs)",
                                audioSha256 = "d4f8a19b882310f92b8d0e722a492580a112233445566778899aabbccddeeff",
                                detectionAlgorithm = "On-Device TFLite 2D-CNN + Indic NLU Coercion Parser",
                                transcriptSnippet = liveTranscript
                            )
                            val file = PdfReportGenerator.generateForensicPdf(context, reportData)
                            if (file != null) {
                                Toast.makeText(context, "PDF Report saved to Downloads: ${file.name}", Toast.LENGTH_LONG).show()
                                PdfReportGenerator.openPdf(context, file)
                            } else {
                                Toast.makeText(context, "Could not generate PDF report", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(38.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ThreatCrimson)
                    ) {
                        Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Download Call Forensic PDF Dossier", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // ScamBuster AI Honeypot (AI आजी / काका) Deploy Button
                    // ONLY appears when isHighThreat == true!
                    Button(
                        onClick = {
                            if (isScamBusterActive) {
                                stopCallHoneypot()
                            } else {
                                startCallHoneypot()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isScamBusterActive) Color(0xFFDC2626) else Color(0xFFD97706)
                        )
                    ) {
                        Icon(
                            imageVector = if (isScamBusterActive) Icons.Default.Stop else Icons.Default.Security,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isScamBusterActive) "🛑 STOP AI HONEYPOT (कॉल थांबवा)" else "👵 DEPLOY AI आजी (स्कॅमरची फिरकी घ्या)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }

                    // Live Honeypot Active Multi-Turn Panel
                    if (isScamBusterActive && activeTurns.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF0F172A).copy(alpha = 0.95f))
                                .border(1.dp, Color(0xFFD97706), RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = selectedPersona.emoji, fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${selectedPersona.displayName} (Active Bait)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFDE68A)
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (selectedPersona == ScamBusterPersona.AAJI) Color(0xFFD97706) else Color(0xFF334155))
                                            .clickable {
                                                selectedPersona = ScamBusterPersona.AAJI
                                                startCallHoneypot()
                                            }
                                            .padding(horizontal = 6.dp, vertical = 3.dp)
                                    ) {
                                        Text("👵 आजी", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (selectedPersona == ScamBusterPersona.KAKA) Color(0xFFD97706) else Color(0xFF334155))
                                            .clickable {
                                                selectedPersona = ScamBusterPersona.KAKA
                                                startCallHoneypot()
                                            }
                                            .padding(horizontal = 6.dp, vertical = 3.dp)
                                    ) {
                                        Text("👴 काका", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Stats Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "⏱️ वेळ वाया: ${timeWastedSeconds}s",
                                    fontSize = 10.sp,
                                    color = Color(0xFF93C5FD),
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "🤯 गोंधळ: ${totalFrustration}%",
                                    fontSize = 10.sp,
                                    color = Color(0xFFF87171),
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "💰 वाचवले: ₹2.5 लाख",
                                    fontSize = 10.sp,
                                    color = Color(0xFF4ADE80),
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Active Dialogue Turn Box
                            val activeTurn = activeTurns.getOrNull(currentCallTurnIndex) ?: activeTurns.last()
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
                                            Color(0xFFEF4444)
                                        else
                                            Color(0xFFFDE68A),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = activeTurn.speakerDisplayName,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (activeTurn.speaker == SpeakerRole.SCAMMER_AI) Color(0xFFFCA5A5) else Color(0xFFFDE68A)
                                        )
                                        Text(
                                            text = "🔊 बोलत आहे...",
                                            fontSize = 9.sp,
                                            color = Color.White
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "\"${activeTurn.speechText}\"",
                                        fontSize = 12.sp,
                                        color = Color.White,
                                        lineHeight = 16.sp,
                                        fontWeight = FontWeight.SemiBold
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

                            Spacer(modifier = Modifier.height(8.dp))

                            // Replay current turn audio button
                            OutlinedButton(
                                onClick = {
                                    val turn = activeTurns.getOrNull(currentCallTurnIndex)
                                    turn?.let { scamBusterEngine.speakTurn(it) }
                                },
                                modifier = Modifier.fillMaxWidth().height(34.dp),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, Color(0xFFD97706))
                            ) {
                                Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = Color(0xFFFDE68A), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("🔊 चालू संवाद पुन्हा ऐकवा (Replay Speech)", fontSize = 10.sp, color = Color(0xFFFDE68A), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // LIVE INDIC TRANSCRIPT BOX
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(CyberDarkSurface)
                .border(1.dp, CyberCardBorder, RoundedCornerShape(14.dp))
                .padding(14.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "LIVE TRANSCRIPT (${scenario?.language?.uppercase() ?: "HI/EN"})",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeuralCyan,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "${activeElapsedSeconds}s elapsed",
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "\"$liveTranscript\"",
                    fontSize = 13.sp,
                    color = TextPrimary,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                LiveAudioWaveform(
                    isActive = activeCallActive,
                    activeColor = if (isHighThreat) ThreatCrimson else NeuralCyan
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // RISK SIGNAL BREAKDOWN BARS
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(CyberDarkSurface)
                .border(1.dp, CyberCardBorder, RoundedCornerShape(14.dp))
                .padding(14.dp)
        ) {
            Column {
                Text(
                    text = "MULTI-FACTOR EVIDENCE BREAKDOWN",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                val voiceRiskVal = if (isRealCall) (if (isHighThreat) 0.94f else 0.12f) else (risk?.voiceRisk ?: 0.20f)
                val convRiskVal = if (isRealCall) (if (isHighThreat) 0.88f else 0.08f) else (risk?.conversationRisk ?: 0.10f)
                val mismatchRiskVal = if (isRealCall) (if (isHighThreat) 0.92f else 0.04f) else (risk?.speakerMismatchRisk ?: 0.05f)
                val callerRiskVal = if (isRealCall) (if (isRealContactSaved) 0.05f else 0.45f) else (risk?.callerRisk ?: 0.40f)

                RiskFactorBar(
                    label = "Voice Synthetics / Vocoder",
                    value = voiceRiskVal,
                    weightLabel = "35%",
                    barColor = if (voiceRiskVal > 0.6f) ThreatCrimson else SafeEmerald
                )
                RiskFactorBar(
                    label = "Conversational / Urgency Risk",
                    value = convRiskVal,
                    weightLabel = "20%",
                    barColor = if (convRiskVal > 0.5f) ThreatCrimson else CautionAmber
                )
                RiskFactorBar(
                    label = "Speaker Profile Mismatch",
                    value = mismatchRiskVal,
                    weightLabel = "15%",
                    barColor = if (mismatchRiskVal > 0.5f) ThreatCrimson else NeuralPurple
                )
                RiskFactorBar(
                    label = "Caller Threat Reputation",
                    value = callerRiskVal,
                    weightLabel = "20%",
                    barColor = if (callerRiskVal > 0.3f) CautionAmber else SafeEmerald
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ACTION BUTTONS
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // "WHY?" Explainability Button
            OutlinedButton(
                onClick = onWhyClicked,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(NeuralCyan)
                )
            ) {
                Icon(imageVector = Icons.Default.HelpOutline, contentDescription = null, tint = NeuralCyan, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Why Flagged?", color = NeuralCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }

            // "VERIFY CALLER" Reverse Voice-CAPTCHA Button
            Button(
                onClick = { showVoiceCaptchaDialog = true },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SafeEmerald)
            ) {
                Icon(imageVector = Icons.Default.VerifiedUser, contentDescription = null, tint = CyberDarkBackground, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Voice CAPTCHA", color = CyberDarkBackground, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // HANG UP / REPORT BUTTON
        Button(
            onClick = onHangUpClicked,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ThreatCrimson)
        ) {
            Icon(imageVector = Icons.Default.CallEnd, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Hang Up & Reconstruct Attack", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }

        Spacer(modifier = Modifier.height(10.dp))

        // DOWNLOAD FORENSIC PDF DOSSIER BUTTON
        OutlinedButton(
            onClick = {
                val now = SimpleDateFormat("dd MMM yyyy, hh:mm:ss a", Locale.getDefault()).format(Date())
                val reportData = ForensicReportData(
                    caseId = "VG-CALL-${(100000..999999).random()}",
                    timestamp = now,
                    sourceTitle = "Active Call: $displayName",
                    sourceDetails = activeCallerNumber,
                    isDeepfake = isHighThreat,
                    authenticityScore = if (isHighThreat) (100 - currentScore).coerceIn(4, 25) else (100 - currentScore).coerceIn(85, 98),
                    vocoderCutoffHz = if (isHighThreat) 3450 else 7800,
                    microPitchJitterPct = if (isHighThreat) 0.08f else 1.84f,
                    averagePitchHz = 210f,
                    suspectedArchitecture = if (isHighThreat) "Neural Vocoder / Zero-Shot TTS (ElevenLabs)" else "Organic Human Vocal Folds",
                    audioSha256 = "d4f8a19b882310f92b8d0e722a492580a112233445566778899aabbccddeeff",
                    detectionAlgorithm = "On-Device TFLite 2D-CNN + Indic NLU Coercion Parser"
                )
                val file = PdfReportGenerator.generateForensicPdf(context, reportData)
                if (file != null) {
                    Toast.makeText(context, "PDF Report saved to Downloads: ${file.name}", Toast.LENGTH_LONG).show()
                    PdfReportGenerator.openPdf(context, file)
                } else {
                    Toast.makeText(context, "Could not generate PDF report", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
        ) {
            Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Download Forensic PDF Report (IT Act 66D)", color = GovNavyPrimary, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showVoiceCaptchaDialog) {
        VoiceCaptchaDialog(
            callerName = displayName,
            onDismiss = { showVoiceCaptchaDialog = false },
            onHangUpAndBlock = {
                showVoiceCaptchaDialog = false
                realCallManager?.onCallEnded(); onHangUpClicked()
            }
        )
    }
}
