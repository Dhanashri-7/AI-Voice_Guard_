"""
VoiceGuard Backend - 5-Factor Calibrated Risk Fusion Engine
Integrates:
1. Voice Authenticity Risk (35%)
2. Caller Reputation Risk (20%)
3. Speaker Mismatch Risk (15%)
4. Conversational / Social-Engineering Risk (20%)
5. Historical Incident Risk (10%)
Features:
- Configurable weights
- Temporal Exponential Moving Average (EMA) smoothing
- Transparent risk tiering & actionable guidance
"""

import math
from typing import Dict, Any, Optional

class RiskFusionEngine:
    def __init__(
        self,
        weight_voice: float = 0.35,
        weight_caller: float = 0.20,
        weight_speaker: float = 0.15,
        weight_conversation: float = 0.20,
        weight_history: float = 0.10,
        ema_alpha: float = 0.45
    ):
        self.w_voice = weight_voice
        self.w_caller = weight_caller
        self.w_speaker = weight_speaker
        self.w_conv = weight_conversation
        self.w_hist = weight_history
        self.ema_alpha = ema_alpha

        # In-memory call session history for temporal smoothing
        self.session_smoothed_scores: Dict[str, float] = {}

    def calculate_risk(
        self,
        call_id: str,
        voice_risk: float,              # [0.0, 1.0] (1.0 = highly synthetic)
        caller_risk: float,             # [0.0, 1.0] (1.0 = unknown/reported spam)
        speaker_mismatch_risk: Optional[float] = None, # [0.0, 1.0] (1.0 = completely different voice)
        conversation_risk: float = 0.0, # [0.0, 1.0] (1.0 = aggressive OTP/financial urgency)
        historical_risk: float = 0.0,   # [0.0, 1.0] (1.0 = past incident history)
        claimed_identity: Optional[str] = None
    ) -> Dict[str, Any]:
        """
        Computes calibrated 0-100 risk score with multi-factor breakdown and temporal smoothing.
        """
        # If no enrolled voice profile exists for contact, speaker weight redistributed
        if speaker_mismatch_risk is None:
            # Rebalance weights dynamically
            scale = 1.0 / (self.w_voice + self.w_caller + self.w_conv + self.w_hist)
            w_v = self.w_voice * scale
            w_c = self.w_caller * scale
            w_s = 0.0
            w_cv = self.w_conv * scale
            w_h = self.w_hist * scale
            actual_speaker_risk = 0.0
        else:
            w_v = self.w_voice
            w_c = self.w_caller
            w_s = self.w_speaker
            w_cv = self.w_conv
            w_h = self.w_hist
            actual_speaker_risk = speaker_mismatch_risk

        # Calculate raw composite risk in range [0.0, 1.0]
        raw_composite = (
            w_v * voice_risk +
            w_c * caller_risk +
            w_s * actual_speaker_risk +
            w_cv * conversation_risk +
            w_h * historical_risk
        )

        # Cross-signal amplifier: If synthetic voice + financial/OTP urgency are both high
        if voice_risk > 0.65 and conversation_risk > 0.50:
            raw_composite = min(1.0, raw_composite * 1.25)

        # Cross-signal amplifier: If trusted person claimed but speaker voice mismatches
        if claimed_identity and actual_speaker_risk > 0.55:
            raw_composite = min(1.0, raw_composite * 1.30)

        # Convert to 0 - 100 scale
        raw_score = raw_composite * 100.0

        # Apply Exponential Moving Average (EMA) temporal smoothing
        prev_score = self.session_smoothed_scores.get(call_id, raw_score)
        smoothed_score = self.ema_alpha * raw_score + (1.0 - self.ema_alpha) * prev_score
        self.session_smoothed_scores[call_id] = smoothed_score

        final_score = int(round(smoothed_score))
        final_score = max(0, min(100, final_score))

        # Risk Tier Classification
        if final_score >= 80:
            tier = "CRITICAL IMPERSONATION RISK"
            threat = "AI Voice Impersonation Attack"
            recommended_action = "DO NOT TRANSFER MONEY. Do not share OTP. Verify caller on their saved number."
        elif final_score >= 60:
            tier = "SUSPICIOUS"
            threat = "Suspicious Audio / Conversational Pattern"
            recommended_action = "Exercise caution. Do not reveal sensitive banking or personal details."
        elif final_score >= 30:
            tier = "CAUTION"
            threat = "Unverified Caller"
            recommended_action = "Caller is unverified. Maintain standard security awareness."
        else:
            tier = "SAFE"
            threat = "Normal Genuine Caller"
            recommended_action = "No threat indicators detected."

        # Confidence Estimation based on signal consistency
        signal_variance = (
            (voice_risk - raw_composite)**2 +
            (caller_risk - raw_composite)**2 +
            (conversation_risk - raw_composite)**2
        ) / 3.0
        confidence = max(0.65, min(0.98, 1.0 - math.sqrt(signal_variance)))

        # Primary Evidence Explanation
        evidence_parts = []
        if voice_risk >= 0.60:
            evidence_parts.append("Synthetic voice artifacts detected")
        if actual_speaker_risk >= 0.50:
            evidence_parts.append("Speaker voice mismatch from trusted profile")
        if conversation_risk >= 0.40:
            evidence_parts.append("High-pressure financial or credential request")
        if caller_risk >= 0.60:
            evidence_parts.append("Unknown caller with poor reputation")

        primary_evidence = " + ".join(evidence_parts) if evidence_parts else "Acoustic and caller signals within normal limits"

        return {
            "overall_risk_score": final_score,
            "risk_tier": tier,
            "threat_type": threat,
            "confidence": round(confidence, 2),
            "breakdown": {
                "voice_risk": round(voice_risk, 3),
                "caller_risk": round(caller_risk, 3),
                "speaker_mismatch_risk": round(actual_speaker_risk, 3),
                "conversation_risk": round(conversation_risk, 3),
                "historical_risk": round(historical_risk, 3)
            },
            "primary_evidence": primary_evidence,
            "recommended_action": recommended_action,
            "is_smoothed": True
        }
