package com.voiceguard.domain.model

enum class RiskTier(val label: String) {
    SAFE("SAFE"),
    CAUTION("CAUTION"),
    SUSPICIOUS("SUSPICIOUS"),
    CRITICAL_IMPERSONATION("CRITICAL IMPERSONATION RISK")
}

data class RiskScore(
    val overallScore: Int,
    val tier: RiskTier,
    val threatType: String,
    val confidence: Float,
    val voiceRisk: Float,
    val callerRisk: Float,
    val speakerMismatchRisk: Float,
    val conversationRisk: Float,
    val historicalRisk: Float,
    val primaryEvidence: String,
    val recommendedAction: String,
    val isSmoothed: Boolean = true
)

data class CallEventStep(
    val timeOffsetSeconds: Int,
    val speaker: String,
    val text: String,
    val targetRiskScore: Int,
    val voiceAuthenticity: Float,
    val detectedIntent: String?,
    val urgency: String,
    val alertMessage: String?
)

data class AttackStage(
    val stageNumber: Int,
    val title: String,
    val description: String,
    val evidence: String
)

data class AttackScenario(
    val id: String,
    val title: String,
    val subtitle: String,
    val incomingNumber: String,
    val callerName: String,
    val claimedIdentity: String?,
    val callerReputation: Float,
    val language: String,
    val attackType: String,
    val expectedThreatLevel: RiskTier,
    val dialogueSteps: List<CallEventStep>,
    val reconstructionStages: List<AttackStage>
)

data class ProtectedContact(
    val id: String,
    val name: String,
    val relationship: String,
    val phone: String,
    val hasEnrolledVoice: Boolean,
    val enrolledSamplesCount: Int,
    val lastVerifiedDate: String,
    val baselinePitchHz: Float
)

data class IncidentRecord(
    val incidentId: String,
    val timestamp: String,
    val callId: String,
    val callerNumber: String,
    val callerName: String?,
    val riskScore: Int,
    val threatType: String,
    val language: String,
    val transcriptSummary: String,
    val forensicEvidence: List<String>,
    val actionTaken: String,
    val reportingDestinations: List<String> = listOf("Cybercrime (1930)", "Chakshu Portal")
)
