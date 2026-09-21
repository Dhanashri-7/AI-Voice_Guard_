package com.voiceguard.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val displayName: String,
    val email: String,
    val isDemoAccount: Boolean = true,
    val biometricEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "callers")
data class CallerEntity(
    @PrimaryKey val phoneNumber: String,
    val callerName: String?,
    val category: String,
    val reputationScore: Float,
    val reportsCount: Int,
    val historicalIncidents: Int,
    val isVerified: Boolean
)

@Entity(tableName = "protected_contacts")
data class ProtectedContactEntity(
    @PrimaryKey val id: String,
    val name: String,
    val relationship: String,
    val phoneNumber: String,
    val hasEnrolledVoice: Boolean,
    val enrolledSamplesCount: Int,
    val baselinePitchHz: Float,
    val enrolledEmbeddingJson: String?,
    val lastVerifiedTimestamp: Long
)

@Entity(tableName = "call_sessions")
data class CallSessionEntity(
    @PrimaryKey val id: String,
    val callerNumber: String,
    val callerDisplayName: String?,
    val timestamp: Long,
    val durationSeconds: Int,
    val language: String,
    val voiceRisk: Float,
    val speakerRisk: Float,
    val contextRisk: Float,
    val overallRisk: Int,
    val classification: String,
    val threatType: String
)

@Entity(tableName = "voice_analyses")
data class VoiceAnalysisEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val callSessionId: String,
    val timestamp: Long,
    val chunkSequence: Int,
    val syntheticProbability: Float,
    val f0Mean: Float,
    val f0Std: Float,
    val pauseRatio: Float,
    val latencyMs: Float
)

@Entity(tableName = "risk_events")
data class RiskEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val callSessionId: String,
    val timestamp: Long,
    val eventType: String,
    val currentRiskScore: Int,
    val alertMessage: String?,
    val transcriptSnippet: String?
)

@Entity(tableName = "incidents")
data class IncidentEntity(
    @PrimaryKey val incidentId: String,
    val callSessionId: String,
    val timestamp: Long,
    val callerNumber: String,
    val callerName: String?,
    val riskScore: Int,
    val threatType: String,
    val language: String,
    val transcriptSummary: String,
    val forensicEvidenceJson: String,
    val recommendedAction: String,
    val userPhone: String = "",
    val reportedTo1930: Boolean = false,
    val reportedToChakshu: Boolean = false
)

@Entity(tableName = "threat_indicators")
data class ThreatIndicatorEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val incidentId: String,
    val indicatorType: String,
    val severity: String,
    val detail: String,
    val confidence: Float
)

@Entity(tableName = "model_predictions")
data class ModelPredictionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val callSessionId: String,
    val modelName: String,
    val modelVersion: String,
    val predictionScore: Float,
    val latencyMs: Float,
    val inferenceEngine: String
)

@Entity(tableName = "consent_records")
data class ConsentRecordEntity(
    @PrimaryKey val permissionKey: String,
    val isGranted: Boolean,
    val rationaleShown: Boolean,
    val grantedTimestamp: Long
)
