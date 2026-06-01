package com.example.ondokuapp.speech

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.example.ondokuapp.settings.SpeechSettings
import java.util.Locale

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
    
    private var chunks: List<String> = emptyList()
    private var currentChunkIndex: Int = 0
    private var isStoppedManually: Boolean = false

    // 簡易ユーザー辞書
    private val userDictionary = mapOf(
        "異世界" to "いせかい",
        "魔王" to "まおう",
        "勇者" to "ゆうしゃ"
    )

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
                val index = utteranceId?.toIntOrNull() ?: currentChunkIndex
                onStart(index)
            }

            override fun onDone(utteranceId: String?) {
                if (isStoppedManually) return
                
                val index = utteranceId?.toIntOrNull() ?: currentChunkIndex
                // 現在のチャンク完了を通知
                onDone(index)
                
                // 次のチャンクへ
                playNext()
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                val index = utteranceId?.toIntOrNull() ?: currentChunkIndex
                onError(index, "再生中にエラーが発生しました")
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
                val index = utteranceId?.toIntOrNull() ?: currentChunkIndex
                onError(index, "再生中にエラーが発生しました (code: $errorCode)")
            }
        })
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
        
        // ユーザー辞書の適用
        userDictionary.forEach { (from, to) ->
            text = text.replace(from, to)
        }

        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, currentChunkIndex.toString())
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
