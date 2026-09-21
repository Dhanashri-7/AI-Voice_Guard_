package com.voiceguard

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Build
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.voiceguard.ui.navigation.VoiceGuardNavHost
import com.voiceguard.ui.theme.CyberDarkBackground
import com.voiceguard.ui.theme.VoiceGuardTheme

class MainActivity : ComponentActivity() {

    private val openForensicDetailsState = androidx.compose.runtime.mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        openForensicDetailsState.value = intent?.getBooleanExtra("EXTRA_OPEN_FORENSIC_DETAILS", false) == true

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }

        // Request critical runtime permissions: Contacts, Mic, Phone State, Call Log, Notifications
        requestCriticalPermissions()
        checkAndRequestCallScreeningRole()

        val app = application as VoiceGuardApplication
        val repository = app.repository
        val prefs = app.preferencesManager
        val simulatorEngine = app.simulatorEngine
        val realCallManager = app.realCallManager

        // Self-heal any false-positive incidents for saved contacts (e.g. Sakshi Didi)
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                repository.sanitizeSavedContactIncidents()
            } catch (e: Exception) {
                // Ignore
            }
        }

        handleCallIntent(intent, realCallManager)

        setContent {
            VoiceGuardTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = CyberDarkBackground
                ) {
                    VoiceGuardNavHost(
                        repository = repository,
                        prefs = prefs,
                        simulatorEngine = simulatorEngine,
                        realCallManager = realCallManager,
                        openForensicDetails = openForensicDetailsState.value
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val app = application as? VoiceGuardApplication
        app?.realCallManager?.let { handleCallIntent(intent, it) }
        if (intent.getBooleanExtra("EXTRA_OPEN_FORENSIC_DETAILS", false)) {
            openForensicDetailsState.value = true
        }
    }

    private fun handleCallIntent(intent: Intent?, realCallManager: com.voiceguard.telecom.RealCallManager) {
        if (intent == null) return
        val isRealCall = intent.getBooleanExtra("EXTRA_REAL_CALL_ACTIVE", false)
        val incomingNumber = intent.getStringExtra("EXTRA_INCOMING_NUMBER") ?: ""
        val callState = intent.getStringExtra("EXTRA_CALL_STATE") ?: ""
        if (isRealCall && callState.isNotBlank()) {
            when (callState) {
                "OFFHOOK" -> realCallManager.onCallAnswered(incomingNumber)
                "IDLE" -> realCallManager.onCallEnded()
                "RINGING" -> if (incomingNumber.isNotBlank()) realCallManager.onCallRinging(incomingNumber)
            }
        }
    }

    private fun checkAndRequestCallScreeningRole() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleHelper = com.voiceguard.telecom.TelecomRoleHelper(this)
            if (!roleHelper.isCallScreeningRoleHeld()) {
                val roleIntent = roleHelper.createRequestRoleIntent()
                if (roleIntent != null) {
                    try {
                        @Suppress("DEPRECATION")
                        startActivityForResult(roleIntent, 2026)
                    } catch (e: Exception) {
                        // Ignored on platforms not supporting role dialog
                    }
                }
            }
        }
    }

    private fun requestCriticalPermissions() {
        val requiredPerms = mutableListOf(
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.SEND_SMS
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requiredPerms.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val missing = requiredPerms.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missing.toTypedArray(), 101)
        }
    }
}
