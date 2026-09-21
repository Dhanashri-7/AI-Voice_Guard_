package com.voiceguard.domain.nlu

data class SemanticAnalysisResult(
    val language: String,
    val semanticRiskScore: Float,
    val urgencyLevel: String,
    val detectedIntents: List<String>,
    val sensitiveInfoRequested: Boolean,
    val financialRequest: Boolean,
    val authorityThreat: Boolean
)

class IndicSocialEngineeringClassifier {

    private val credentialRegex = Regex("(?i)(otp|one time password|pin|mpin|cvv|password|passcode|ओटीपी|पिन|पासवर्ड|सीवीवी|गुपित क्रमांक)")
    private val financialRegex = Regex("(?i)(transfer|send money|gpay|phonepe|paytm|upi|rupees|rs|पैसे भेजो|पैसे डालो|रुपये|रुपए|पैसे पाठवा|खात्यात टाका)")
    private val urgencyRegex = Regex("(?i)(immediately|right now|urgent|hurry|emergency|blocked|तुरंत|अभी के अभी|जल्दी|तत्काल|बंद हो जाएगा|त्वरित|आत्ताच|लगेच)")
    private val authorityRegex = Regex("(?i)(cbi|police|cyber cell|rbi|customs|arrest|वारंट|पुलिस|सीबीआई|आरबीआई|अदालत|गिरफ्तारी|पोलीस|न्यायालय|अटक)")

    fun analyze(text: String): SemanticAnalysisResult {
        if (text.isBlank()) {
            return SemanticAnalysisResult("en", 0.0f, "LOW", emptyList(), false, false, false)
        }

        val intents = mutableListOf<String>()
        var riskAccum = 0.0f

        val hasCredential = credentialRegex.containsMatchIn(text)
        if (hasCredential) {
            intents.add("CREDENTIAL_THEFT")
            riskAccum += 0.40f
        }

        val hasFinancial = financialRegex.containsMatchIn(text)
        if (hasFinancial) {
            intents.add("FINANCIAL_FRAUD")
            riskAccum += 0.30f
        }

        val hasUrgency = urgencyRegex.containsMatchIn(text)
        if (hasUrgency) {
            intents.add("URGENCY_MANIPULATION")
            riskAccum += 0.20f
        }

        val hasAuthority = authorityRegex.containsMatchIn(text)
        if (hasAuthority) {
            intents.add("AUTHORITY_IMPERSONATION")
            riskAccum += 0.35f
        }

        val lang = detectLanguage(text)
        val score = minOf(1.0f, riskAccum)

        val urgencyLevel = when {
            hasUrgency && (hasAuthority || hasCredential) -> "CRITICAL"
            hasUrgency -> "HIGH"
            score > 0.2f -> "MEDIUM"
            else -> "LOW"
        }

        return SemanticAnalysisResult(
            language = lang,
            semanticRiskScore = score,
            urgencyLevel = urgencyLevel,
            detectedIntents = intents,
            sensitiveInfoRequested = hasCredential,
            financialRequest = hasFinancial,
            authorityThreat = hasAuthority
        )
    }

    private fun detectLanguage(text: String): String {
        val devanagariCount = text.count { it in '\u0900'..'\u097F' }
        if (devanagariCount > 2) {
            val marathiWords = listOf("आहे", "नाही", "करा", "पाठवा", "आलो", "मी", "खात्यात", "लगेच", "त्वरित")
            if (marathiWords.any { text.contains(it) }) return "mr"
            return "hi"
        }
        return "en"
    }
}
