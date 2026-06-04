package com.example.ondokuapp.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.ondokuapp.settings.SpeechSettings

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("speech_settings", Context.MODE_PRIVATE)

    fun loadSpeechSettings(): SpeechSettings {
        val pitch = prefs.getFloat("pitch", 1.0f).coerceIn(0.5f, 2.0f)
        val speed = prefs.getFloat("speed", 1.0f).coerceIn(0.5f, 3.0f)
        return SpeechSettings(pitch = pitch, speed = speed)
    }

    fun saveSpeechSettings(settings: SpeechSettings) {
        prefs.edit().apply {
            putFloat("pitch", settings.pitch)
            putFloat("speed", settings.speed)
            apply()
        }
    }

    fun loadAutoPlayNextEpisode(): Boolean {
        return prefs.getBoolean("auto_play_next", true)
    }

    fun saveAutoPlayNextEpisode(enabled: Boolean) {
        prefs.edit().apply {
            putBoolean("auto_play_next", enabled)
            apply()
        }
    }

    fun loadSelectedVoiceName(): String? {
        return prefs.getString("selected_voice", null)
    }

    fun saveSelectedVoiceName(voiceName: String?) {
        prefs.edit().apply {
            putString("selected_voice", voiceName)
            apply()
        }
    }
}
