"""
VoiceGuard Backend - Streaming WebSocket for Real-Time Call Analysis
Endpoint: /ws/live-analysis
Processes sliding-window audio chunks, runs real-time speech features,
updates risk fusion scores, and pushes dynamic alerts back to mobile client.
"""

import json
import asyncio
from datetime import datetime
from fastapi import APIRouter, WebSocket, WebSocketDisconnect
from services.risk_fusion import RiskFusionEngine
from services.multilingual_nlu import MultilingualNLUEngine

router = APIRouter()
risk_engine = RiskFusionEngine()
nlu_engine = MultilingualNLUEngine()

@router.websocket("/live-analysis")
async def websocket_live_analysis(websocket: WebSocket):
    await websocket.accept()
    call_id = None
    sequence = 0

    try:
        while True:
            raw_data = await websocket.receive_text()
            data = json.loads(raw_data)

            call_id = data.get("call_id", f"CALL-{sequence}")
            sequence += 1
            audio_base64 = data.get("audio_chunk_base64")
            transcript_text = data.get("transcript_text", "")
            voice_prob = data.get("synthetic_voice_prob", 0.15)
            caller_reputation = data.get("caller_reputation", 0.5)
            speaker_consistency = data.get("speaker_consistency", None)
            claimed_identity = data.get("claimed_identity")

            # Analyze transcript
            nlu_res = nlu_engine.analyze_transcript(transcript_text)
            conv_risk = nlu_res["semantic_risk_score"]

            # Compute smoothed risk
            risk_result = risk_engine.calculate_risk(
                call_id=call_id,
                voice_risk=voice_prob,
                caller_risk=1.0 - caller_reputation,
                speaker_mismatch_risk=(1.0 - speaker_consistency) if speaker_consistency is not None else None,
                conversation_risk=conv_risk,
                historical_risk=0.10,
                claimed_identity=claimed_identity
            )

            # Alert message formulation
            alert_msg = None
            if risk_result["overall_risk_score"] >= 80:
                alert_msg = "CRITICAL: AI voice impersonation + financial request detected! Do NOT transfer money."
            elif risk_result["overall_risk_score"] >= 60:
                alert_msg = "WARNING: Suspicious audio features and high-urgency conversational tactics."

            # Construct live event payload
            event_payload = {
                "call_id": call_id,
                "sequence": sequence,
                "timestamp_str": datetime.now().strftime("%H:%M:%S"),
                "event_type": "FRAME_EVALUATED",
                "current_risk_score": risk_result["overall_risk_score"],
                "risk_tier": risk_result["risk_tier"],
                "voice_authenticity": round(1.0 - voice_prob, 2),
                "conversation_risk": round(conv_risk, 2),
                "transcript": transcript_text,
                "detected_language": nlu_res["detected_language"],
                "detected_intents": nlu_res["intents"],
                "urgency_level": nlu_res["urgency_level"],
                "alert_message": alert_msg,
                "primary_evidence": risk_result["primary_evidence"],
                "recommended_action": risk_result["recommended_action"]
            }

            await websocket.send_text(json.dumps(event_payload))

    except WebSocketDisconnect:
        # Client disconnected normally
        pass
    except Exception as e:
        await websocket.close(code=1011, reason=str(e))
