package com.voiceguard.domain.risk

import com.voiceguard.domain.model.RiskScore
import com.voiceguard.domain.model.RiskTier
import kotlin.math.sqrt

class AndroidRiskFusionEngine(
    private val weightVoice: Float = 0.35f,
    private val weightCaller: Float = 0.20f,
    private val weightSpeaker: Float = 0.15f,
    private val weightConversation: Float = 0.20f,
    private val weightHistory: Float = 0.10f,
    private val emaAlpha: Float = 0.40f
) {
    private var previousSmoothedScore: Float? = null

    fun calculateRisk(
        voiceRisk: Float,
        callerRisk: Float,
        speakerMismatchRisk: Float?,
        conversationRisk: Float,
        historicalRisk: Float = 0.05f,
        claimedIdentity: String? = null
    ): RiskScore {
        val actualSpeakerRisk = speakerMismatchRisk ?: 0.0f
        val wSpeaker = if (speakerMismatchRisk != null) weightSpeaker else 0.0f

        // Rebalance weights if no enrolled voice profile exists
        val totalWeight = weightVoice + weightCaller + wSpeaker + weightConversation + weightHistory
        val normFactor = 1.0f / totalWeight

        var rawComposite = (
            weightVoice * voiceRisk +
            weightCaller * callerRisk +
            wSpeaker * actualSpeakerRisk +
            weightConversation * conversationRisk +
            weightHistory * historicalRisk
        ) * normFactor

        // Cross-Signal Amplifier #1: Synthetic voice + Financial Urgency
        if (voiceRisk > 0.65f && conversationRisk > 0.50f) {
            rawComposite = minOf(1.0f, rawComposite * 1.25f)
        }

        // Cross-Signal Amplifier #2: Claimed Trusted Person + Voice Mismatch
        if (!claimedIdentity.isNullOrEmpty() && actualSpeakerRisk > 0.50f) {
            rawComposite = minOf(1.0f, rawComposite * 1.30f)
        }

        val rawScore = rawComposite * 100f
        val prev = previousSmoothedScore ?: rawScore
        // Fast-attack response for threat escalation in cybersecurity:
        // When danger is detected (rawScore > prev), escalate fast (alpha = 0.85f), smooth decay otherwise
        val effectiveAlpha = if (rawScore > prev) 0.85f else emaAlpha
        val smoothed = (effectiveAlpha * rawScore) + ((1.0f - effectiveAlpha) * prev)
        previousSmoothedScore = smoothed

        val finalScore = smoothed.toInt().coerceIn(0, 100)

        val tier = when {
            finalScore >= 80 -> RiskTier.CRITICAL_IMPERSONATION
            finalScore >= 60 -> RiskTier.SUSPICIOUS
            finalScore >= 30 -> RiskTier.CAUTION
            else -> RiskTier.SAFE
        }

        val threat = when (tier) {
            RiskTier.CRITICAL_IMPERSONATION -> "AI Voice Impersonation Attack"
            RiskTier.SUSPICIOUS -> "Suspicious Voice / Coercion Tactics"
            RiskTier.CAUTION -> "Unverified Caller"
            RiskTier.SAFE -> "Genuine Bonafide Caller"
        }

        val action = when (tier) {
            RiskTier.CRITICAL_IMPERSONATION -> "DO NOT TRANSFER MONEY. Do not share OTP. Call trusted number independently."
            RiskTier.SUSPICIOUS -> "Exercise caution. Do not reveal passwords, banking credentials, or personal identity."
            RiskTier.CAUTION -> "Maintain standard security vigilance. Verify unknown caller claims."
            RiskTier.SAFE -> "Standard verified communication."
        }

        val evidenceList = mutableListOf<String>()
        if (voiceRisk >= 0.60f) evidenceList.add("High-frequency vocoder synthesis artifacts")
        if (actualSpeakerRisk >= 0.50f) evidenceList.add("Speaker voice mismatch from enrolled profile")
        if (conversationRisk >= 0.40f) evidenceList.add("Urgent financial / credential pressure tactics")
        if (callerRisk >= 0.60f) evidenceList.add("Unknown number with low reputation score")

        val primaryEvidence = if (evidenceList.isNotEmpty()) {
            evidenceList.joinToString(" + ")
        } else {
            "Acoustic features and caller profile within normal parameters"
        }

        // Confidence estimation
        val variance = (
            (voiceRisk - rawComposite) * (voiceRisk - rawComposite) +
            (callerRisk - rawComposite) * (callerRisk - rawComposite) +
            (conversationRisk - rawComposite) * (conversationRisk - rawComposite)
        ) / 3.0f
        val confidence = (1.0f - sqrt(variance)).coerceIn(0.65f, 0.98f)

        return RiskScore(
            overallScore = finalScore,
            tier = tier,
            threatType = threat,
            confidence = confidence,
            voiceRisk = voiceRisk,
            callerRisk = callerRisk,
            speakerMismatchRisk = actualSpeakerRisk,
            conversationRisk = conversationRisk,
            historicalRisk = historicalRisk,
            primaryEvidence = primaryEvidence,
            recommendedAction = action,
            isSmoothed = true
        )
    }

    fun reset() {
        previousSmoothedScore = null
    }
}
