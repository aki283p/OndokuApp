package com.example.ondokuapp.speech

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.example.ondokuapp.model.UserDictionaryEntry
import com.example.ondokuapp.settings.SpeechSettings
import java.util.Locale

data class TtsVoiceInfo(
    val name: String,
    val locale: String,
    val displayName: String
)

class TextToSpeechManager(
    context: Context,
    private val onStart: (Int) -> Unit = {},
    private val onDone: (Int) -> Unit = {},
    private val onError: (Int, String) -> Unit = { _, _ -> }
) {
    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var pendingAction: (() -> Unit)? = null
    private var currentSettings = SpeechSettings()
    private var selectedVoiceName: String? = null
    
    private var chunks: List<String> = emptyList()
    private var currentChunkIndex: Int = 0
    private var isStoppedManually: Boolean = false

    // ユーザー辞書
    private var userDictionary: List<UserDictionaryEntry> = emptyList()

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale.JAPANESE)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTS", "Language Japanese is not supported")
                    onError(-1, "日本語の読み上げがサポートされていません")
                } else {
                    isInitialized = true
                    setupListener()
                    applySettings(currentSettings)
                    applyVoice(selectedVoiceName)
                    pendingAction?.invoke()
                    pendingAction = null
                }
            } else {
                Log.e("TTS", "Initialization failed")
                onError(-1, "TTSの初期化に失敗しました")
            }
        }
    }

    private fun setupListener() {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                if (utteranceId == "test") return
                val index = utteranceId?.toIntOrNull() ?: currentChunkIndex
                onStart(index)
            }

            override fun onDone(utteranceId: String?) {
                if (utteranceId == "test") return
                if (isStoppedManually) return
                
                val index = utteranceId?.toIntOrNull() ?: currentChunkIndex
                // 現在のチャンク完了を通知
                onDone(index)
                
                // 次のチャンクへ
                playNext()
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                if (utteranceId == "test") return
                val index = utteranceId?.toIntOrNull() ?: currentChunkIndex
                onError(index, "再生中にエラーが発生しました")
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
                if (utteranceId == "test") return
                val index = utteranceId?.toIntOrNull() ?: currentChunkIndex
                onError(index, "再生中にエラーが発生しました (code: $errorCode)")
            }
        })
    }

    /**
     * 利用可能な音声一覧を取得する
     */
    fun getAvailableVoices(): List<TtsVoiceInfo> {
        return tts?.voices?.filter { it.locale.language == "ja" }?.map {
            TtsVoiceInfo(it.name, it.locale.toString(), it.name)
        } ?: emptyList()
    }

    /**
     * 音声を適用する
     */
    fun applyVoice(voiceName: String?) {
        selectedVoiceName = voiceName
        if (isInitialized && voiceName != null) {
            val voice = tts?.voices?.find { it.name == voiceName }
            if (voice != null) {
                tts?.voice = voice
            }
        }
    }

    /**
     * 指定したチャンクリストを順番に読み上げる
     */
    fun speakChunks(newChunks: List<String>, startIndex: Int = 0) {
        if (newChunks.isEmpty()) {
            onDone(-1)
            return
        }
        
        isStoppedManually = false
        chunks = newChunks
        // 範囲補正
        currentChunkIndex = startIndex.coerceIn(0, chunks.size - 1)

        if (!isInitialized) {
            pendingAction = { speakChunks(newChunks, startIndex) }
            return
        }
        
        playCurrent()
    }

    private fun playCurrent() {
        if (currentChunkIndex < 0 || currentChunkIndex >= chunks.size || !isInitialized) {
            return
        }

        var text = chunks[currentChunkIndex]
        if (text.isBlank()) {
            // 空のチャンクは飛ばす
            playNext()
            return
        }
        
        text = applyDictionary(text)

        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, currentChunkIndex.toString())
    }

    private fun applyDictionary(text: String): String {
        var processedText = text
        userDictionary.forEach { entry ->
            processedText = processedText.replace(entry.from, entry.to)
        }
        return processedText
    }

    fun speakTest(text: String) {
        if (!isInitialized || text.isBlank()) return
        
        // 既存の読み上げを停止（安全のため）
        // ただし isStoppedManually は true にしない（再開を考慮する場合は別途管理が必要だが、
        // 今回は「テスト再生時は止まって良い」という要件なので単純化）
        tts?.stop()
        
        val processedText = applyDictionary(text)
        tts?.speak(processedText, TextToSpeech.QUEUE_FLUSH, null, "test")
    }

    fun stopTest() {
        tts?.stop()
    }

    fun updateDictionary(entries: List<UserDictionaryEntry>) {
        userDictionary = entries
            .filter { it.from.isNotEmpty() && it.to.isNotEmpty() }
            .sortedByDescending { it.from.length }
    }

    private fun playNext() {
        if (isStoppedManually) return

        if (currentChunkIndex < chunks.size - 1) {
            currentChunkIndex++
            playCurrent()
        }
        // 最後のチャンクの場合は、setupListener の onDone で onDone が呼ばれるためここでは何もしない
    }

    fun applySettings(settings: SpeechSettings) {
        currentSettings = settings
        if (isInitialized) {
            tts?.setPitch(settings.pitch)
            tts?.setSpeechRate(settings.speed)
        }
    }

    fun pause() {
        stop()
    }

    fun stop() {
        isStoppedManually = true
        tts?.stop()
        pendingAction = null
    }

    fun shutdown() {
        stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }
}
