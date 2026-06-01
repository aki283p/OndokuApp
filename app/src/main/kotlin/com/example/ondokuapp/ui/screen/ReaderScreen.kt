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
    
    val listState = rememberLazyListState()
    var showSettings by remember { mutableStateOf(false) }

    // 自動スクロール
    LaunchedEffect(currentChunkIndex) {
        if (isSpeaking && currentChunkIndex >= 0 && currentChunkIndex < currentChunks.size) {
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
                totalChunks = currentChunks.size,
                showSettings = showSettings,
                onToggleSettings = { showSettings = !showSettings }
            )
        }
    ) { innerPadding ->
        ReaderContent(
            modifier = Modifier.padding(innerPadding),
            listState = listState,
            chunks = if (currentChunks.isNotEmpty()) currentChunks else listOf(novel.content),
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
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
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
                isVisible = totalChunks > 0
            )

            Spacer(modifier = Modifier.height(16.dp))

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
    isVisible: Boolean
) {
    if (!isVisible) return

    val progress = if (totalCount > 0) (currentIndex + 1).toFloat() / totalCount else 0f
    
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "読み上げ位置",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
            Text(
                text = "${currentIndex + 1} / $totalCount",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
        }
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
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
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onToggleSettings) {
            Icon(
                imageVector = if (showSettings) Icons.Default.SettingsApplications else Icons.Default.Settings,
                contentDescription = "設定",
                tint = if (showSettings) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            IconButton(onClick = onStop) {
                Icon(Icons.Default.Stop, contentDescription = "停止")
            }

            if (isSpeaking) {
                LargeFloatingActionButton(
                    onClick = onPause,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Pause, contentDescription = "一時停止", modifier = Modifier.size(36.dp))
                }
            } else {
                FloatingActionButton(
                    onClick = onStart,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        imageVector = if (hasProgress) Icons.Default.PlayArrow else Icons.Default.PlayArrow, 
                        contentDescription = "再生", 
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
            
            if (!isSpeaking && hasProgress) {
                IconButton(onClick = onRestart) {
                    Icon(Icons.Default.Replay, contentDescription = "最初から再生")
                }
            } else {
                // バランスのためにダミーのスペースを置くか、単に表示しない
                Spacer(modifier = Modifier.size(48.dp))
            }
        }

        // 右側のスペース調整用
        Spacer(modifier = Modifier.size(48.dp))
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
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("速度: ${"%.1f".format(speed)}x", style = MaterialTheme.typography.labelMedium, modifier = Modifier.width(64.dp))
            Slider(
                value = speed,
                onValueChange = { onSettingsChange(com.example.ondokuapp.settings.SpeechSettings(speed = it, pitch = pitch)) },
                valueRange = 0.5f..3.0f,
                modifier = Modifier.weight(1f)
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("ピッチ: ${"%.1f".format(pitch)}", style = MaterialTheme.typography.labelMedium, modifier = Modifier.width(64.dp))
            Slider(
                value = pitch,
                onValueChange = { onSettingsChange(com.example.ondokuapp.settings.SpeechSettings(speed = speed, pitch = it)) },
                valueRange = 0.5f..2.0f,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
