package com.example.ondokuapp.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ondokuapp.MainViewModel
import com.example.ondokuapp.model.Novel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    viewModel: MainViewModel,
    novel: Novel,
    onBack: () -> Unit
) {
    val speechSettings = viewModel.speechSettings
    val isSpeaking = viewModel.isSpeaking
    val currentChunks = viewModel.currentChunks
    val currentChunkIndex = viewModel.currentChunkIndex
    
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    
    var showTimerMenu by remember { mutableStateOf(false) }

    // 自動スクロール
    LaunchedEffect(currentChunkIndex) {
        if (isSpeaking && currentChunkIndex >= 0 && currentChunkIndex < currentChunks.size) {
            listState.animateScrollToItem(currentChunkIndex)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(novel.title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "戻る")
                    }
                },
                actions = {
                    IconButton(onClick = { showTimerMenu = true }) {
                        Icon(Icons.Default.Timer, contentDescription = "スリープタイマー")
                        if (viewModel.sleepTimerMinutes > 0) {
                            Badge { Text("${viewModel.sleepTimerMinutes}") }
                        }
                    }
                    DropdownMenu(expanded = showTimerMenu, onDismissRequest = { showTimerMenu = false }) {
                        listOf(0, 5, 10, 15, 30, 60).forEach { mins ->
                            DropdownMenuItem(
                                text = { Text(if (mins == 0) "オフ" else "${mins}分") },
                                onClick = { viewModel.setSleepTimer(mins); showTimerMenu = false }
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            Surface(tonalElevation = 4.dp) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    // Playback Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { viewModel.stopSpeaking() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "停止")
                        }
                        
                        if (isSpeaking) {
                            Button(onClick = { viewModel.pauseSpeaking() }) {
                                Text("一時停止")
                            }
                        } else {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (novel.currentPosition > 0) {
                                    FilledTonalButton(onClick = { viewModel.startSpeaking(novel, fromStart = true) }) {
                                        Text("最初から")
                                    }
                                }
                                Button(onClick = { viewModel.startSpeaking(novel, fromStart = false) }) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = "再生")
                                    Spacer(Modifier.width(8.dp))
                                    Text(if (novel.currentPosition > 0) "続きから" else "再生")
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Settings Sliders
                    Text("再生速度: ${"%.1f".format(speechSettings.speed)}", style = MaterialTheme.typography.labelMedium)
                    Slider(
                        value = speechSettings.speed,
                        onValueChange = { viewModel.updateSpeechSettings(speechSettings.copy(speed = it)) },
                        valueRange = 0.5f..3.0f
                    )

                    Text("ピッチ: ${"%.1f".format(speechSettings.pitch)}", style = MaterialTheme.typography.labelMedium)
                    Slider(
                        value = speechSettings.pitch,
                        onValueChange = { viewModel.updateSpeechSettings(speechSettings.copy(pitch = it)) },
                        valueRange = 0.5f..2.0f
                    )
                }
            }
        }
    ) { innerPadding ->
        // 本文をチャンク（段落・文）単位で表示し、現在位置をハイライト
        val displayChunks = if (currentChunks.isNotEmpty()) currentChunks else listOf(novel.content)
        
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            itemsIndexed(displayChunks) { index, chunk ->
                val isCurrent = isSpeaking && index == currentChunkIndex
                Text(
                    text = chunk,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .background(if (isCurrent) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                        .padding(4.dp)
                )
            }
        }
    }
}
