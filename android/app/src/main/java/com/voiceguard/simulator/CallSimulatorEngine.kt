package com.voiceguard.simulator

import com.voiceguard.domain.model.*
import com.voiceguard.domain.risk.AndroidRiskFusionEngine
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*

data class SimulatorCallState(
    val isActive: Boolean = false,
    val currentScenario: AttackScenario? = null,
    val currentStepIndex: Int = 0,
    val activeSpeaker: String = "",
    val activeTranscript: String = "",
    val currentRiskScore: RiskScore? = null,
    val elapsedSeconds: Int = 0,
    val eventTimeline: List<String> = emptyList(),
    val latestAlertMessage: String? = null,
    val isCompleted: Boolean = false
)

class CallSimulatorEngine(
    private val riskEngine: AndroidRiskFusionEngine = AndroidRiskFusionEngine()
) {
    private val _simState = MutableStateFlow(SimulatorCallState())
    val simState: StateFlow<SimulatorCallState> = _simState.asStateFlow()

    private var simJob: Job? = null
    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    fun startScenario(scenario: AttackScenario, scope: CoroutineScope) {
        simJob?.cancel()
        riskEngine.reset()

        val initialTimeline = mutableListOf(
            "${timeFormat.format(Date())} Incoming call from ${scenario.incomingNumber}",
            "${timeFormat.format(Date())} Caller ID identified as: '${scenario.callerName}'",
            "${timeFormat.format(Date())} VoiceGuard real-time analysis engaged"
        )

        val initialRisk = riskEngine.calculateRisk(
            voiceRisk = 0.15f,
            callerRisk = scenario.callerReputation,
            speakerMismatchRisk = if (scenario.claimedIdentity != null) 0.10f else null,
            conversationRisk = 0.0f,
            historicalRisk = 0.05f,
            claimedIdentity = scenario.claimedIdentity
        )

        _simState.value = SimulatorCallState(
            isActive = true,
            currentScenario = scenario,
            currentStepIndex = 0,
            activeSpeaker = scenario.callerName,
            activeTranscript = "Call connected. Listening for acoustic features...",
            currentRiskScore = initialRisk,
            elapsedSeconds = 0,
            eventTimeline = initialTimeline,
            latestAlertMessage = null,
            isCompleted = false
        )

        simJob = scope.launch(Dispatchers.Default) {
            val steps = scenario.dialogueSteps
            for ((index, step) in steps.withIndex()) {
                val stepDelay = if (index == 0) 1800L else 2500L
                delay(stepDelay)

                val updatedTimeline = _simState.value.eventTimeline.toMutableList()
                val timestamp = timeFormat.format(Date())
                updatedTimeline.add("$timestamp ${step.speaker}: \"${step.text.take(35)}...\"")

                if (step.alertMessage != null) {
                    updatedTimeline.add("$timestamp ALERT: ${step.alertMessage}")
                }

                // Compute updated risk
                val voiceRisk = 1.0f - step.voiceAuthenticity
                val speakerMismatch = if (scenario.claimedIdentity != null) {
                    if (step.targetRiskScore > 50) 0.80f else 0.20f
                } else null

                val convRisk = when (step.urgency) {
                    "CRITICAL" -> 0.95f
                    "HIGH" -> 0.75f
                    "MEDIUM" -> 0.40f
                    else -> 0.05f
                }

                val calculatedScore = riskEngine.calculateRisk(
                    voiceRisk = voiceRisk,
                    callerRisk = scenario.callerReputation,
                    speakerMismatchRisk = speakerMismatch,
                    conversationRisk = convRisk,
                    historicalRisk = 0.10f,
                    claimedIdentity = scenario.claimedIdentity
                )

                // Ensure the simulator score reliably achieves the scenario's designed target score
                val finalScoreValue = maxOf(step.targetRiskScore, calculatedScore.overallScore)
                val finalTier = when {
                    finalScoreValue >= 80 -> RiskTier.CRITICAL_IMPERSONATION
                    finalScoreValue >= 60 -> RiskTier.SUSPICIOUS
                    finalScoreValue >= 30 -> RiskTier.CAUTION
                    else -> RiskTier.SAFE
                }

                val score = calculatedScore.copy(
                    overallScore = finalScoreValue,
                    tier = finalTier,
                    voiceRisk = maxOf(voiceRisk, calculatedScore.voiceRisk),
                    conversationRisk = maxOf(convRisk, calculatedScore.conversationRisk),
                    speakerMismatchRisk = maxOf(speakerMismatch ?: 0f, calculatedScore.speakerMismatchRisk)
                )

                _simState.value = _simState.value.copy(
                    currentStepIndex = index + 1,
                    activeSpeaker = step.speaker,
                    activeTranscript = step.text,
                    currentRiskScore = score,
                    elapsedSeconds = _simState.value.elapsedSeconds + (stepDelay / 1000).toInt(),
                    eventTimeline = updatedTimeline,
                    latestAlertMessage = step.alertMessage
                )
            }

            // Completed scenario
            _simState.value = _simState.value.copy(isCompleted = true)
        }
    }

    fun endCall() {
        simJob?.cancel()
        _simState.value = _simState.value.copy(
            isActive = false,
            isCompleted = true
        )
    }

    fun reset() {
        simJob?.cancel()
        riskEngine.reset()
        _simState.value = SimulatorCallState()
    }
}
