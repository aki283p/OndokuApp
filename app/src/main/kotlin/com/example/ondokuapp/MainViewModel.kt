package com.example.ondokuapp

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ondokuapp.model.AppDatabase
import com.example.ondokuapp.model.Novel
import com.example.ondokuapp.settings.SpeechSettings
import com.example.ondokuapp.speech.TextToSpeechManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val novelDao = db.novelDao()
    private val ttsManager = TextToSpeechManager(application)

    val novels: StateFlow<List<Novel>> = novelDao.getAllNovels()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    var speechSettings by mutableStateOf(SpeechSettings())
        private set

    var isSpeaking by mutableStateOf(false)
        private set

    fun upsertNovel(title: String, content: String, id: Long = 0) {
        viewModelScope.launch {
            val novel = if (id != 0L) {
                novelDao.getNovelById(id)?.copy(
                    title = title,
                    content = content,
                    updatedAt = System.currentTimeMillis()
                )
            } else {
                Novel(
                    title = if (title.isBlank()) content.take(20).replace("\n", " ") else title,
                    content = content
                )
            }
            novel?.let {
                if (it.id != 0L) novelDao.updateNovel(it)
                else novelDao.insertNovel(it)
            }
        }
    }

    fun deleteNovel(novel: Novel) {
        viewModelScope.launch {
            novelDao.deleteNovel(novel)
        }
    }

    fun updateSpeechSettings(settings: SpeechSettings) {
        speechSettings = settings
        ttsManager.applySettings(settings)
    }

    fun startSpeaking(novel: Novel) {
        isSpeaking = true
        ttsManager.speak(novel.content, novel.currentPosition)
        
        // Update last read time
        viewModelScope.launch {
            novelDao.updateNovel(novel.copy(lastReadAt = System.currentTimeMillis()))
        }
    }

    fun pauseSpeaking() {
        // 現在は疑似一時停止（停止して次回は最初または指定位置から再生）
        isSpeaking = false
        ttsManager.pause()
    }

    fun stopSpeaking() {
        isSpeaking = false
        ttsManager.stop()
    }

    override fun onCleared() {
        super.onCleared()
        ttsManager.shutdown()
    }
}
