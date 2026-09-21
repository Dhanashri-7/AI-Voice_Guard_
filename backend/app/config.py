"""
VoiceGuard Backend - Configuration & Settings
"""

from pydantic_settings import BaseSettings
from typing import List

class Settings(BaseSettings):
    PROJECT_NAME: str = "VoiceGuard AI"
    VERSION: str = "1.0.0"
    API_V1_STR: str = "/api/v1"
    DEBUG: bool = True

    # Risk Fusion Calibration Weights
    WEIGHT_VOICE_AUTHENTICITY: float = 0.35
    WEIGHT_CALLER_REPUTATION: float = 0.20
    WEIGHT_SPEAKER_MISMATCH: float = 0.15
    WEIGHT_CONVERSATION_RISK: float = 0.20
    WEIGHT_HISTORICAL_RISK: float = 0.10

    # Risk Thresholds
    THRESHOLD_SAFE_MAX: int = 29
    THRESHOLD_CAUTION_MAX: int = 59
    THRESHOLD_SUSPICIOUS_MAX: int = 79
    THRESHOLD_CRITICAL_MIN: int = 80

    # Privacy Settings
    RETAIN_RAW_AUDIO: bool = False
    EPHEMERAL_PROCESSING_ONLY: bool = True

    # CORS
    CORS_ORIGINS: List[str] = ["*"]

    class Config:
        env_file = ".env"
        extra = "ignore"

settings = Settings()
