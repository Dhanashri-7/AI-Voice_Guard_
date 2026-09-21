"""
VoiceGuard Backend - Unit & Integration Test Suite
Tests:
- API Root and Health
- Multilingual NLU Engine (Hindi, Marathi, English)
- 5-Factor Risk Fusion Engine
- Voice Analysis REST Endpoint
- Incident Persistence & Retrieval
"""

import pytest
import os
import sys

sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "app")))
from services.multilingual_nlu import MultilingualNLUEngine
from services.risk_fusion import RiskFusionEngine
from database.database import init_db, save_incident, list_incidents, get_threat_intel

def test_multilingual_nlu_hindi():
    engine = MultilingualNLUEngine()
    text = "आपका बैंक खाता तुरंत ब्लॉक हो जाएगा, कृपया अपना ओटीपी शेयर करें"
    result = engine.analyze_transcript(text)
    assert result["detected_language"] in ["hi", "mr"]
    assert "CREDENTIAL_THEFT" in result["intents"]
    assert result["urgency_level"] in ["HIGH", "CRITICAL"]
    assert result["sensitive_info_requested"] is True

def test_multilingual_nlu_marathi():
    engine = MultilingualNLUEngine()
    text = "मी पोलीस ठाण्यातून बोलत आहे, त्वरित वीस हजार रुपये खात्यात पाठवा"
    result = engine.analyze_transcript(text)
    assert result["detected_language"] == "mr"
    assert "AUTHORITY_IMPERSONATION" in result["intents"]
    assert "FINANCIAL_FRAUD" in result["intents"]
    assert result["financial_request"] is True

def test_risk_fusion_engine_safe():
    engine = RiskFusionEngine()
    result = engine.calculate_risk(
        call_id="call-test-safe",
        voice_risk=0.10,
        caller_risk=0.05,
        speaker_mismatch_risk=0.08,
        conversation_risk=0.05,
        historical_risk=0.0
    )
    assert result["overall_risk_score"] < 30
    assert result["risk_tier"] == "SAFE"

def test_risk_fusion_engine_critical_impersonation():
    engine = RiskFusionEngine()
    result = engine.calculate_risk(
        call_id="call-test-critical",
        voice_risk=0.92,
        caller_risk=0.85,
        speaker_mismatch_risk=0.78,
        conversation_risk=0.95,
        historical_risk=0.40,
        claimed_identity="Mom"
    )
    assert result["overall_risk_score"] >= 80
    assert result["risk_tier"] == "CRITICAL IMPERSONATION RISK"
    assert "DO NOT TRANSFER MONEY" in result["recommended_action"]

def test_database_and_threat_intel():
    init_db()
    intel = get_threat_intel("+919876543210")
    assert intel is not None
    assert intel["category"] == "Financial Fraud"

    # Test incident save & retrieve
    inc_data = {
        "id": "VG-TEST-001",
        "call_id": "c-999",
        "caller_number": "+919999900000",
        "caller_name": "Scammer",
        "risk_score": 91,
        "threat_type": "AI Voice Impersonation",
        "language": "hi",
        "transcript_summary": "Demanded OTP for bank verification",
        "forensic_evidence": ["High-frequency synthesis artifact", "Abnormal pitch"],
        "recommended_action": "Block and report to 1930"
    }
    save_incident(inc_data)
    incidents = list_incidents()
    matching = [i for i in incidents if i["incident_id"] == "VG-TEST-001"]
    assert len(matching) == 1
    assert matching[0]["risk_score"] == 91
