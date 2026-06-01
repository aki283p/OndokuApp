package com.example.ondokuapp

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ondokuapp.model.AppDatabase
import com.example.ondokuapp.model.Novel
import com.example.ondokuapp.repository.NovelRepository
import com.example.ondokuapp.settings.SpeechSettings
import com.example.ondokuapp.speech.TextToSpeechManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val repository = NovelRepository(db.novelDao())
    
    private val ttsManager = TextToSpeechManager(
        context = application,
        onStart = { isSpeaking = true },
        onDone = { isSpeaking = false },
        onError = { isSpeaking = false }
    )

    val novels: StateFlow<List<Novel>> = repository.allNovels
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    var speechSettings by mutableStateOf(SpeechSettings())
        private set

    var isSpeaking by mutableStateOf(false)
        private set

    fun upsertNovel(title: String, content: String, id: Long = 0) {
        viewModelScope.launch {
            if (id != 0L) {
                repository.getNovelById(id)?.let { existing ->
                    repository.updateNovel(existing.copy(
                        title = title,
                        content = content,
                        updatedAt = System.currentTimeMillis()
                    ))
                }
            } else {
                val newTitle = if (title.isBlank()) {
                    content.take(20).replace("\n", " ")
                } else {
                    title
                }
                repository.insertNovel(Novel(
                    title = newTitle,
                    content = content
                ))
            }
        }
    }

    fun deleteNovel(novel: Novel) {
        viewModelScope.launch {
            repository.deleteNovel(novel)
        }
    }

    fun updateSpeechSettings(settings: SpeechSettings) {
        speechSettings = settings
        ttsManager.applySettings(settings)
    }

    fun startSpeaking(novel: Novel) {
        // isSpeaking は ttsManager のコールバックで true になる
        ttsManager.speak(novel.content, novel.currentPosition)
        
        // Update last read time
        viewModelScope.launch {
            repository.updateNovel(novel.copy(lastReadAt = System.currentTimeMillis()))
        }
    }

    fun pauseSpeaking() {
        // 現在は疑似一時停止（停止して次回は最初または指定位置から再生）
        // isSpeaking は ttsManager のコールバックで false になる
        ttsManager.pause()
    }

    fun stopSpeaking() {
        // isSpeaking は ttsManager のコールバックで false になる
        ttsManager.stop()
    }

    override fun onCleared() {
        super.onCleared()
        ttsManager.shutdown()
    }
}
