"""
VoiceGuard Backend - Pydantic Data Schemas
"""

from pydantic import BaseModel, Field
from typing import List, Optional, Dict, Any

class ForensicIndicator(BaseModel):
    factor: str
    severity: str
    detail: str
    confidence: float

class VoiceAnalysisRequest(BaseModel):
    caller_id: str
    audio_base64: Optional[str] = None
    sample_rate: int = 16000
    prosodic_features: Optional[Dict[str, float]] = None
    language: Optional[str] = "en"

class VoiceAnalysisResponse(BaseModel):
    caller_id: str
    voice_authenticity_score: float
    synthetic_voice_probability: float
    threat_classification: str
    confidence: float
    latency_ms: float
    forensic_indicators: List[ForensicIndicator]
    spectrogram_anomaly_regions: List[Dict[str, str]] = []
    is_demo_model: bool = True

class RiskBreakdown(BaseModel):
    voice_risk: float
    caller_risk: float
    speaker_mismatch_risk: float
    conversation_risk: float
    historical_risk: float

class RiskScoreRequest(BaseModel):
    call_id: str
    caller_number: str
    voice_score: float = Field(..., ge=0.0, le=1.0)
    caller_reputation_score: float = Field(0.5, ge=0.0, le=1.0)
    speaker_consistency_score: Optional[float] = Field(None, ge=0.0, le=1.0)
    transcript_segment: Optional[str] = None
    claimed_identity: Optional[str] = None
    call_duration_seconds: int = 0

class RiskScoreResponse(BaseModel):
    call_id: str
    overall_risk_score: int
    risk_tier: str
    threat_type: str
    confidence: float
    breakdown: RiskBreakdown
    primary_evidence: str
    recommended_action: str
    temporal_smoothed: bool = True

class CallerReputationResponse(BaseModel):
    phone_number: str
    name: Optional[str]
    category: str
    verified_status: bool
    reputation_score: float
    reports_count: int
    historical_incidents: int
    is_synthetic_demo_data: bool = True

class LiveAnalysisFrame(BaseModel):
    call_id: str
    sequence: int
    audio_chunk_base64: Optional[str] = None
    features: Optional[Dict[str, Any]] = None
    transcript_text: Optional[str] = None

class LiveAnalysisEvent(BaseModel):
    call_id: str
    sequence: int
    timestamp_str: str
    event_type: str
    current_risk_score: int
    risk_tier: str
    voice_authenticity: float
    conversation_risk: float
    transcript: Optional[str] = None
    detected_intent: Optional[str] = None
    urgency_level: str
    alert_message: Optional[str] = None

class TelecomScreenRequest(BaseModel):
    incoming_number: str
    call_direction: str = "INCOMING"
    carrier: Optional[str] = "Jio / Airtel / Vi"

class TelecomScreenResponse(BaseModel):
    incoming_number: str
    screening_decision: str
    risk_level: str
    reputation_score: float
    recommended_overlay: str
    explanation: str

class IncidentReportCreate(BaseModel):
    call_id: str
    caller_number: str
    caller_name: Optional[str] = None
    risk_score: int
    threat_type: str
    language: str
    transcript_summary: str
    forensic_evidence: List[str]
    action_taken: str

class IncidentReportResponse(BaseModel):
    incident_id: str
    timestamp: str
    call_id: str
    caller_number: str
    caller_name: Optional[str]
    risk_score: int
    threat_type: str
    language: str
    transcript_summary: str
    forensic_evidence: List[str]
    recommended_action: str
    reporting_destinations: List[str]
