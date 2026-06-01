package com.example.ondokuapp.speech

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import com.example.ondokuapp.settings.SpeechSettings
import java.util.Locale

class TextToSpeechManager(context: Context) {
    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var pendingAction: (() -> Unit)? = null
    private var currentSettings = SpeechSettings()

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale.JAPANESE)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTS", "Language Japanese is not supported")
                } else {
                    isInitialized = true
                    applySettings(currentSettings)
                    pendingAction?.invoke()
                    pendingAction = null
                }
            } else {
                Log.e("TTS", "Initialization failed")
            }
        }
    }

    fun speak(text: String, startPosition: Int = 0) {
        if (text.isBlank()) return
        
        val textToSpeak = if (startPosition > 0 && startPosition < text.length) {
            text.substring(startPosition)
        } else {
            text
        }

        if (!isInitialized) {
            pendingAction = { speak(text, startPosition) }
            return
        }
        
        tts?.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, "OndokuAppID")
    }

    fun applySettings(settings: SpeechSettings) {
        currentSettings = settings
        if (isInitialized) {
            tts?.setPitch(settings.pitch)
            tts?.setSpeechRate(settings.speed)
        }
    }

    fun setPitch(pitch: Float) {
        currentSettings = currentSettings.copy(pitch = pitch)
        if (isInitialized) {
            tts?.setPitch(pitch)
        }
    }

    fun setSpeechRate(speed: Float) {
        currentSettings = currentSettings.copy(speed = speed)
        if (isInitialized) {
            tts?.setSpeechRate(speed)
        }
    }

    fun pause() {
        // Android TTS does not have a native pause. Treat as stop.
        stop()
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }
}
