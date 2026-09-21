# VoiceGuard API Reference (v1)
## Enterprise, Banking & Telecom Integration Gateway
### OpenAPI / Swagger Documentation available at `/docs`

---

## 1. Voice Forensics & Risk Endpoints

### 1.1 Analyze Voice Segment
`POST /api/v1/analyze/voice`

Analyzes an audio segment or acoustic feature vector for synthetic vocoder artifacts.

**Request**:
```json
{
  "caller_id": "+919123456789",
  "sample_rate": 16000,
  "prosodic_features": {
    "f0_mean": 135.0,
    "f0_std": 5.8,
    "pause_ratio": 0.02
  },
  "language": "hi"
}
```

**Response (200 OK)**:
```json
{
  "caller_id": "+919123456789",
  "voice_authenticity_score": 0.12,
  "synthetic_voice_probability": 0.88,
  "threat_classification": "AI_VOICE_CLONE",
  "confidence": 88.0,
  "latency_ms": 2.15,
  "forensic_indicators": [
    {
      "factor": "High-Frequency Synthesis Artifact",
      "severity": "CRITICAL",
      "detail": "Acoustic energy distribution exhibits vocoder phase boundary discontinuity above 3.2 kHz.",
      "confidence": 88.0
    },
    {
      "factor": "Unnatural Pitch Rigidity (Robotic Monotone)",
      "severity": "HIGH",
      "detail": "F0 standard deviation (5.8 Hz) is abnormally low, characteristic of cloned pitch synthesis.",
      "confidence": 88.5
    }
  ],
  "spectrogram_anomaly_regions": [
    {
      "freq_band": "3200Hz - 7800Hz",
      "anomaly": "Phase Vocoder Discontinuity"
    }
  ],
  "is_demo_model": true
}
```

---

### 1.2 Calculate Multi-Factor Risk Score
`POST /api/v1/risk/score`

Computes the calibrated 0-100 composite risk score with multi-factor breakdown and EMA temporal smoothing.

**Request**:
```json
{
  "call_id": "CALL-2026-991",
  "caller_number": "+919123456789",
  "voice_score": 0.88,
  "caller_reputation_score": 0.40,
  "speaker_consistency_score": 0.34,
  "transcript_segment": "हॉस्पिटलमध्ये तातडीची गरज आहे, त्वरित वीस हजार रुपये पाठवा",
  "claimed_identity": "Mom",
  "call_duration_seconds": 12
}
```

**Response (200 OK)**:
```json
{
  "call_id": "CALL-2026-991",
  "overall_risk_score": 91,
  "risk_tier": "CRITICAL IMPERSONATION RISK",
  "threat_type": "AI Voice Impersonation Attack",
  "confidence": 0.94,
  "breakdown": {
    "voice_risk": 0.88,
    "caller_risk": 0.60,
    "speaker_mismatch_risk": 0.66,
    "conversation_risk": 0.80,
    "historical_risk": 0.10
  },
  "primary_evidence": "Synthetic voice artifacts detected + Speaker voice mismatch from trusted profile + High-pressure financial request",
  "recommended_action": "DO NOT TRANSFER MONEY. Do not share OTP. Verify caller on their saved number.",
  "temporal_smoothed": true
}
```

---

## 2. Enterprise & Telecom Gateway Endpoints

### 2.1 Telecom Call Screening
`POST /api/v1/telecom/screen`

Enables cellular network operators (Jio, Airtel, Vi) to query the threat registry before ringing.

**Request**:
```json
{
  "incoming_number": "+919876543210",
  "call_direction": "INCOMING",
  "carrier": "Jio"
}
```

**Response (200 OK)**:
```json
{
  "incoming_number": "+919876543210",
  "screening_decision": "SILENCE_AND_WARN",
  "risk_level": "HIGH_THREAT",
  "reputation_score": 0.92,
  "recommended_overlay": "VOICEGUARD_CRITICAL_WARNING_OVERLAY",
  "explanation": "Number flagged in National Fraud Registry (Financial Fraud) with 142 user reports."
}
```

---

### 2.2 Banking Transfer Call Verification
`POST /api/v1/banking/verify-transaction-call`

Real-time fraud verification for banks during high-value customer approvals.

**Request**:
```json
{
  "caller_phone": "+919876543210",
  "transaction_amount_inr": 50000,
  "live_voice_risk": 0.85
}
```

**Response (200 OK)**:
```json
{
  "status": "BLOCKED_PENDING_STEP_UP",
  "action_code": "TRIGGER_BIOMETRIC_VIDEO_KYC",
  "voice_authenticity": 0.15,
  "transaction_amount_inr": 50000,
  "coercion_risk": "CRITICAL",
  "recommendation": "Do not process transfer. Initiate independent out-of-band video verification."
}
```

---

## 3. Streaming WebSocket (`/ws/live-analysis`)

Clients stream JSON frames containing 1.5s sliding window features. The server pushes real-time events with updated risk scores, language identification, and warning banners.
