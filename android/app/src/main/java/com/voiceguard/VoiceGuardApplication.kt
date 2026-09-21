package com.voiceguard

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.voiceguard.data.local.PreferencesManager
import com.voiceguard.data.local.VoiceGuardDatabase
import com.voiceguard.data.repository.VoiceGuardRepository
import com.voiceguard.simulator.CallSimulatorEngine
import com.voiceguard.telecom.RealCallManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class VoiceGuardApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val database by lazy { VoiceGuardDatabase.getDatabase(this, applicationScope) }
    val repository by lazy { VoiceGuardRepository(database.voiceGuardDao()) }
    val preferencesManager by lazy { PreferencesManager(this) }
    val simulatorEngine by lazy { CallSimulatorEngine() }
    val realCallManager by lazy { RealCallManager(this, repository) }

    override fun onCreate() {
        super.onCreate()
        instance = this
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val alertChannel = NotificationChannel(
                CHANNEL_ID_ALERTS,
                "VoiceGuard Critical Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Urgent warnings when an active call is identified as an AI voice impersonation attack."
                enableVibration(true)
            }

            val monitorChannel = NotificationChannel(
                CHANNEL_ID_MONITOR,
                "VoiceGuard Call Protection",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Silent background notification indicating VoiceGuard active screening."
                enableVibration(false)
                setShowBadge(false)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(alertChannel)
            notificationManager?.createNotificationChannel(monitorChannel)
        }
    }

    companion object {
        const val CHANNEL_ID_ALERTS = "voiceguard_threat_alerts"
        const val CHANNEL_ID_MONITOR = "voiceguard_monitor_channel"
        var instance: VoiceGuardApplication? = null
            private set
    }
}
