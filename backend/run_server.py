"""
VoiceGuard AI Backend Server Runner
Smart India Hackathon 2026 - Problem Statement 26104 (AICTE Cyber Security Cell)
Runs FastAPI REST & WebSocket server at http://localhost:8000
"""

import os
import sys

# Configure UTF-8 encoding for Windows console
try:
    sys.stdout.reconfigure(encoding='utf-8')
    sys.stderr.reconfigure(encoding='utf-8')
except Exception:
    pass

if __name__ == "__main__":
    import uvicorn
    app_dir = os.path.join(os.path.dirname(os.path.abspath(__file__)), "app")
    sys.path.insert(0, app_dir)

    print("=" * 65)
    print("  VOICEGUARD AI CLOUD THREAT INTEL BACKEND")
    print("  Smart India Hackathon 2026 - Problem Statement 26104")
    print("=" * 65)
    print("  • API Documentation: http://localhost:8000/docs")
    print("  • Health Endpoint:   http://localhost:8000/health")
    print("  • Telecom Sandbox:   http://localhost:8000/api/v1/telecom/cdr/lookup")
    print("=" * 65)

    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=False, app_dir=app_dir)
