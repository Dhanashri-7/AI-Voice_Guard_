package com.voiceguard.data.local

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("voiceguard_prefs", Context.MODE_PRIVATE)

    var isLoggedIn: Boolean
        get() = prefs.getBoolean("is_logged_in", false)
        set(value) = prefs.edit().putBoolean("is_logged_in", value).apply()

    var userName: String
        get() = prefs.getString("user_name", "Dhanashri Pawar") ?: "Dhanashri Pawar"
        set(value) = prefs.edit().putString("user_name", value).apply()

    var userPhone: String
        get() = prefs.getString("user_phone", "") ?: ""
        set(value) = prefs.edit().putString("user_phone", value).apply()

    var userEmail: String
        get() = prefs.getString("user_email", "") ?: ""
        set(value) = prefs.edit().putString("user_email", value).apply()

    var userAdditionalInfo: String
        get() = prefs.getString("user_additional_info", "") ?: ""
        set(value) = prefs.edit().putString("user_additional_info", value).apply()

    var isDemoAccount: Boolean
        get() = prefs.getBoolean("is_demo_account", false)
        set(value) = prefs.edit().putBoolean("is_demo_account", value).apply()

    var retainRawAudio: Boolean
        get() = prefs.getBoolean("retain_raw_audio", false) // Default strictly FALSE (Privacy First)
        set(value) = prefs.edit().putBoolean("retain_raw_audio", value).apply()

    var retentionPeriod: String
        get() = prefs.getString("retention_period", "NEVER") ?: "NEVER"
        set(value) = prefs.edit().putString("retention_period", value).apply()

    var onDeviceOnlyInference: Boolean
        get() = prefs.getBoolean("on_device_only", true)
        set(value) = prefs.edit().putBoolean("on_device_only", value).apply()

    var callScreeningRoleGranted: Boolean
        get() = prefs.getBoolean("call_screening_granted", false)
        set(value) = prefs.edit().putBoolean("call_screening_granted", value).apply()

    var detectionSensitivity: String
        get() = prefs.getString("detection_sensitivity", "HIGH") ?: "HIGH"
        set(value) = prefs.edit().putString("detection_sensitivity", value).apply()

    var autoDeployHoneypot: Boolean
        get() = prefs.getBoolean("auto_deploy_honeypot", true)
        set(value) = prefs.edit().putBoolean("auto_deploy_honeypot", value).apply()

    var honeypotPersona: String
        get() = prefs.getString("honeypot_persona", "AAJI") ?: "AAJI"
        set(value) = prefs.edit().putString("honeypot_persona", value).apply()

    fun calculateSecurityScore(protectedContactsCount: Int, callScreeningActive: Boolean): Int {
        var score = 50
        if (protectedContactsCount > 0) score += 20
        if (callScreeningActive) score += 15
        if (!retainRawAudio) score += 10
        if (onDeviceOnlyInference) score += 5
        return minOf(100, score)
    }

    fun logout() {
        prefs.edit()
            .putBoolean("is_logged_in", false)
            .remove("user_name")
            .remove("user_phone")
            .remove("user_email")
            .apply()
    }

    fun clearAllUserData() {
        prefs.edit().clear().apply()
    }
}
