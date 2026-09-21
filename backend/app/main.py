"""
VoiceGuard AI - Real-Time Voice Integrity & Impersonation Protection
FastAPI Backend Application
Smart India Hackathon 2026 - Problem Statement 26104 (AICTE Cyber Security Cell)
"""

import os
import sys
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from contextlib import asynccontextmanager

from api.v1.endpoints import router as api_v1_router
from api.v1.websocket import router as ws_router
from api.v1.telecom import router as telecom_router
from database.database import init_db

@asynccontextmanager
async def lifespan(app: FastAPI):
    # Startup: ensure SQLite database and threat tables are initialized
    init_db()
    print("[+] VoiceGuard Backend started successfully.")
    yield
    print("[-] VoiceGuard Backend shutting down.")

app = FastAPI(
    title="VoiceGuard AI - Real-Time Voice Integrity API",
    description="Backend microservice for AI voice cloning detection, multi-factor risk fusion, and telecom screening. Smart India Hackathon 2026.",
    version="1.0.0",
    docs_url="/docs",
    redoc_url="/redoc",
    lifespan=lifespan
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Include API Routers
app.include_router(api_v1_router, prefix="/api/v1", tags=["Voice Forensics & Risk"])
app.include_router(telecom_router, prefix="/api/v1", tags=["Telecom & Banking Enterprise"])
app.include_router(ws_router, prefix="/ws", tags=["Streaming WebSocket"])

@app.get("/")
def root():
    return {
        "service": "VoiceGuard AI Protection Gateway",
        "status": "OPERATIONAL",
        "hackathon": "Smart India Hackathon 2026 - AICTE 26104",
        "documentation": "/docs"
    }

@app.get("/health")
def health_check():
    return {
        "status": "HEALTHY",
        "edge_engine": "SpectroCNNAttention PyTorch",
        "speech_languages": ["Hindi", "Marathi", "English", "Gujarati"],
        "privacy": "Zero Audio Retention Compliant"
    }

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True)
