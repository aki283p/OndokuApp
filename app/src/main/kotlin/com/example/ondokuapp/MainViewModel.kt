package com.example.ondokuapp

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ondokuapp.model.AppDatabase
import com.example.ondokuapp.model.Novel
import com.example.ondokuapp.repository.NovelImportRepository
import com.example.ondokuapp.repository.NovelRepository
import com.example.ondokuapp.settings.SpeechSettings
import com.example.ondokuapp.speech.TextToSpeechManager
import com.example.ondokuapp.util.TextCleaner
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

enum class SortOrder {
    UPDATED_AT, LAST_READ_AT, TITLE
}

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val repository = NovelRepository(db.novelDao())
    private val importRepository = NovelImportRepository()
    
    private val ttsManager = TextToSpeechManager(
        context = application,
        onStart = { index -> 
            currentChunkIndex = index
            isSpeaking = true 
        },
        onDone = { index -> 
            // 全チャンク終わったら false になる（ttsManager の挙動に依存）
            // 実際には最後のチャンクが終わったときだけ false にしたい
            if (index >= currentChunks.size - 1) {
                isSpeaking = false
            }
            saveCurrentPosition(index)
        },
        onError = { _, _ -> isSpeaking = false }
    )

    // 本棚の表示用State
    var searchQuery by mutableStateOf("")
        private set
    var sortOrder by mutableStateOf(SortOrder.UPDATED_AT)
        private set
    var showFavoritesOnly by mutableStateOf(false)
        private set

    val novels: StateFlow<List<Novel>> = combine(
        repository.allNovels,
        snapshotFlow { searchQuery },
        snapshotFlow { sortOrder },
        snapshotFlow { showFavoritesOnly }
    ) { allNovels, query, order, favOnly ->
        allNovels.filter {
            (it.title.contains(query, ignoreCase = true) || it.content.contains(query, ignoreCase = true)) &&
                    (!favOnly || it.isFavorite)
        }.sortedWith(when (order) {
            SortOrder.UPDATED_AT -> compareByDescending { it.updatedAt }
            SortOrder.LAST_READ_AT -> compareByDescending { it.lastReadAt ?: 0L }
            SortOrder.TITLE -> compareBy { it.title }
        })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 読み上げ状態
    var speechSettings by mutableStateOf(SpeechSettings())
        private set
    var isSpeaking by mutableStateOf(false)
        private set
    var currentChunks by mutableStateOf<List<String>>(emptyList())
        private set
    var currentChunkIndex by mutableStateOf(0)
        private set
    var currentReadingNovel by mutableStateOf<Novel?>(null)
        private set

    // スリープタイマー
    private var sleepTimerJob: Job? = null
    var sleepTimerMinutes by mutableStateOf(0) // 0 means off
        private set

    // インポート状態
    var isImporting by mutableStateOf(false)
        private set
    var importError by mutableStateOf<String?>(null)
        private set

    fun updateSearchQuery(query: String) { searchQuery = query }
    fun updateSortOrder(order: SortOrder) { sortOrder = order }
    fun toggleFavoriteFilter() { showFavoritesOnly = !showFavoritesOnly }

    fun upsertNovel(title: String, content: String, id: Long = 0, sourceUrl: String? = null) {
        viewModelScope.launch {
            if (id != 0L) {
                repository.getNovelById(id)?.let { existing ->
                    repository.updateNovel(existing.copy(
                        title = title,
                        content = content,
                        sourceUrl = sourceUrl ?: existing.sourceUrl,
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
                    content = content,
                    sourceUrl = sourceUrl,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                ))
            }
        }
    }

    fun toggleFavorite(novel: Novel) {
        viewModelScope.launch {
            repository.updateNovel(novel.copy(isFavorite = !novel.isFavorite))
        }
    }

    fun deleteNovel(novel: Novel) {
        viewModelScope.launch {
            repository.deleteNovel(novel)
        }
    }

    fun importFromUrl(url: String, onComplete: (String, String) -> Unit) {
        if (url.isBlank()) return
        viewModelScope.launch {
            isImporting = true
            importError = null
            importRepository.fetchFromUrl(url).onSuccess { novel ->
                onComplete(novel.title, novel.content)
            }.onFailure {
                importError = "取得に失敗しました: ${it.message}"
            }
            isImporting = false
        }
    }

    fun cleanText(text: String): String {
        return TextCleaner.clean(text)
    }

    fun updateSpeechSettings(settings: SpeechSettings) {
        speechSettings = settings
        ttsManager.applySettings(settings)
    }

    fun startSpeaking(novel: Novel, fromStart: Boolean = false) {
        currentReadingNovel = novel
        currentChunks = TextCleaner.splitIntoChunks(novel.content)
        val startIndex = if (fromStart) 0 else novel.currentPosition
        currentChunkIndex = startIndex
        
        isSpeaking = true
        ttsManager.speakChunks(currentChunks, startIndex)
        
        // Update last read time
        viewModelScope.launch {
            repository.updateNovel(novel.copy(lastReadAt = System.currentTimeMillis()))
        }
    }

    private fun saveCurrentPosition(index: Int) {
        val novel = currentReadingNovel ?: return
        viewModelScope.launch {
            repository.updateNovel(novel.copy(currentPosition = index))
        }
    }

    fun pauseSpeaking() {
        isSpeaking = false
        ttsManager.pause()
        cancelSleepTimer()
    }

    fun stopSpeaking() {
        isSpeaking = false
        ttsManager.stop()
        cancelSleepTimer()
    }

    fun setSleepTimer(minutes: Int) {
        sleepTimerMinutes = minutes
        sleepTimerJob?.cancel()
        if (minutes > 0) {
            sleepTimerJob = viewModelScope.launch {
                delay(minutes * 60 * 1000L)
                stopSpeaking()
                sleepTimerMinutes = 0
            }
        }
    }

    private fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        sleepTimerMinutes = 0
    }

    override fun onCleared() {
        super.onCleared()
        ttsManager.shutdown()
        sleepTimerJob?.cancel()
    }
}
