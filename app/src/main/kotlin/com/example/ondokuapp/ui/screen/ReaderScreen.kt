package com.example.ondokuapp.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ondokuapp.MainViewModel
import com.example.ondokuapp.model.Novel
import com.example.ondokuapp.util.TextCleaner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    viewModel: MainViewModel,
    novel: Novel,
    onBack: () -> Unit
) {
    val isSpeaking = viewModel.isSpeaking
    val currentChunks = viewModel.currentChunks
    val currentChunkIndex = viewModel.currentChunkIndex
    
    // 表示用のチャンク（ViewModelにまだ入っていない場合は今の本文を分割して使う）
    val displayChunks = remember(novel.content, currentChunks) {
        if (currentChunks.isNotEmpty()) currentChunks else TextCleaner.splitIntoChunks(novel.content)
    }

    val listState = rememberLazyListState()
    var showSettings by remember { mutableStateOf(false) }

    // 自動スクロール
    LaunchedEffect(currentChunkIndex) {
        if (isSpeaking && currentChunkIndex >= 0 && currentChunkIndex < displayChunks.size) {
            listState.animateScrollToItem(currentChunkIndex)
        }
    }

    Scaffold(
        topBar = {
            ReaderTopBar(
                title = novel.title,
                sleepTimerMinutes = viewModel.sleepTimerMinutes,
                onBack = {
                    viewModel.stopSpeaking()
                    onBack()
                },
                onSetTimer = { viewModel.setSleepTimer(it) }
            )
        },
        bottomBar = {
            ReaderBottomBar(
                viewModel = viewModel,
                novel = novel,
                isSpeaking = isSpeaking,
                currentChunkIndex = currentChunkIndex,
                totalChunks = displayChunks.size,
                showSettings = showSettings,
                onToggleSettings = { showSettings = !showSettings }
            )
        }
    ) { innerPadding ->
        ReaderContent(
            modifier = Modifier.padding(innerPadding),
            listState = listState,
            chunks = displayChunks,
            currentChunkIndex = currentChunkIndex,
            isSpeaking = isSpeaking
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReaderTopBar(
    title: String,
    sleepTimerMinutes: Int,
    onBack: () -> Unit,
    onSetTimer: (Int) -> Unit
) {
    var showTimerMenu by remember { mutableStateOf(false) }

    TopAppBar(
        title = { 
            Text(
                text = title,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            ) 
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "戻る")
            }
        },
        actions = {
            Box {
                IconButton(onClick = { showTimerMenu = true }) {
                    BadgedBox(
                        badge = {
                            if (sleepTimerMinutes > 0) {
                                Badge { Text(sleepTimerMinutes.toString()) }
                            }
                        }
                    ) {
                        Icon(Icons.Default.Timer, contentDescription = "スリープタイマー")
                    }
                }
                DropdownMenu(expanded = showTimerMenu, onDismissRequest = { showTimerMenu = false }) {
                    listOf(0, 5, 10, 15, 30, 60).forEach { mins ->
                        DropdownMenuItem(
                            text = { Text(if (mins == 0) "オフ" else "${mins}分") },
                            onClick = { onSetTimer(mins); showTimerMenu = false }
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun ReaderContent(
    modifier: Modifier = Modifier,
    listState: androidx.compose.foundation.lazy.LazyListState,
    chunks: List<String>,
    currentChunkIndex: Int,
    isSpeaking: Boolean
) {
    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        itemsIndexed(chunks) { index, chunk ->
            val isCurrent = isSpeaking && index == currentChunkIndex
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isCurrent) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                    .padding(8.dp)
            ) {
                Text(
                    text = chunk,
                    style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 28.sp)
                )
            }
        }
        item {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
private fun ReaderBottomBar(
    viewModel: MainViewModel,
    novel: Novel,
    isSpeaking: Boolean,
    currentChunkIndex: Int,
    totalChunks: Int,
    showSettings: Boolean,
    onToggleSettings: () -> Unit
) {
    Surface(
        tonalElevation = 8.dp,
        shadowElevation = 8.dp,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            ReadingProgress(
                currentIndex = currentChunkIndex,
                totalCount = totalChunks,
                isSpeaking = isSpeaking,
                hasProgress = novel.currentPosition > 0
            )

            Spacer(modifier = Modifier.height(20.dp))

            PlaybackControls(
                isSpeaking = isSpeaking,
                hasProgress = novel.currentPosition > 0,
                onStart = { viewModel.startSpeaking(novel, fromStart = false) },
                onRestart = { viewModel.startSpeaking(novel, fromStart = true) },
                onPause = { viewModel.pauseSpeaking() },
                onStop = { viewModel.stopSpeaking() },
                showSettings = showSettings,
                onToggleSettings = onToggleSettings
            )

            AnimatedVisibility(
                visible = showSettings,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                SpeechSettingsPanel(
                    speed = viewModel.speechSettings.speed,
                    pitch = viewModel.speechSettings.pitch,
                    onSettingsChange = { viewModel.updateSpeechSettings(it) }
                )
            }
        }
    }
}

@Composable
private fun ReadingProgress(
    currentIndex: Int,
    totalCount: Int,
    isSpeaking: Boolean,
    hasProgress: Boolean
) {
    val progress = if (totalCount > 0) (currentIndex + 1).toFloat() / totalCount else 0f
    
    val statusText = when {
        isSpeaking -> "再生中"
        hasProgress -> "一時停止中"
        else -> "未再生"
    }
    
    val countText = if (totalCount > 0) {
        if (!isSpeaking && !hasProgress) "0 / $totalCount" else "${currentIndex + 1} / $totalCount"
    } else {
        "-- / --"
    }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = statusText,
                style = MaterialTheme.typography.labelMedium,
                color = if (isSpeaking) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
            )
            Text(
                text = countText,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        LinearProgressIndicator(
            progress = { if (!isSpeaking && !hasProgress) 0f else progress.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = if (isSpeaking) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
private fun PlaybackControls(
    isSpeaking: Boolean,
    hasProgress: Boolean,
    onStart: () -> Unit,
    onRestart: () -> Unit,
    onPause: () -> Unit,
    onStop: () -> Unit,
    showSettings: Boolean,
    onToggleSettings: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 設定ボタン
        OutlinedIconButton(
            onClick = onToggleSettings,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = if (showSettings) Icons.Default.SettingsApplications else Icons.Default.Settings,
                contentDescription = if (showSettings) "設定を閉じる" else "設定を開く"
            )
        }

        // 停止ボタン
        OutlinedIconButton(
            onClick = onStop,
            modifier = Modifier.size(48.dp),
            enabled = isSpeaking || hasProgress
        ) {
            Icon(Icons.Default.Stop, contentDescription = "停止")
        }

        // 再生 / 一時停止ボタン (メイン)
        if (isSpeaking) {
            Button(
                onClick = onPause,
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(Icons.Default.Pause, null)
                Spacer(Modifier.width(8.dp))
                Text("一時停止")
            }
        } else {
            Button(
                onClick = onStart,
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(if (hasProgress) Icons.Default.PlayArrow else Icons.Default.PlayArrow, null)
                Spacer(Modifier.width(8.dp))
                Text(if (hasProgress) "続きから" else "再生")
            }
        }

        // 最初から再生ボタン
        if (!isSpeaking && hasProgress) {
            FilledTonalIconButton(
                onClick = onRestart,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.Replay, contentDescription = "最初から再生")
            }
        } else {
            Spacer(modifier = Modifier.size(48.dp))
        }
    }
}

@Composable
private fun SpeechSettingsPanel(
    speed: Float,
    pitch: Float,
    onSettingsChange: (com.example.ondokuapp.settings.SpeechSettings) -> Unit
) {
    Column(
        modifier = Modifier
            .padding(top = 16.dp)
            .fillMaxWidth()
    ) {
        HorizontalDivider(modifier = Modifier.padding(bottom = 16.dp))
        
        Text(
            text = "読み上げ設定",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("速度: ${"%.1f".format(speed)}x", style = MaterialTheme.typography.labelLarge, modifier = Modifier.width(72.dp))
            Slider(
                value = speed,
                onValueChange = { onSettingsChange(com.example.ondokuapp.settings.SpeechSettings(speed = it, pitch = pitch)) },
                valueRange = 0.5f..3.0f,
                modifier = Modifier.weight(1f)
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("ピッチ: ${"%.1f".format(pitch)}", style = MaterialTheme.typography.labelLarge, modifier = Modifier.width(72.dp))
            Slider(
                value = pitch,
                onValueChange = { onSettingsChange(com.example.ondokuapp.settings.SpeechSettings(speed = speed, pitch = it)) },
                valueRange = 0.5f..2.0f,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
