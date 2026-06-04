package com.example.ondokuapp.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ondokuapp.MainViewModel
import com.example.ondokuapp.R
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
    
    val readerEpisodes = viewModel.readerEpisodes
    val currentEpisodeIndex = viewModel.currentEpisodeIndex
    val currentEpisode = readerEpisodes.getOrNull(currentEpisodeIndex)

    // 初回読み込み
    LaunchedEffect(novel.id) {
        viewModel.loadEpisodesForReader(novel)
    }

    // 表示用のチャンク
    // ViewModelにまだ入っていない場合（未再生など）は、今のエピソードまたはNovel.contentから作る
    val displayChunks = remember(currentEpisode, currentChunks) {
        if (currentChunks.isNotEmpty()) {
            currentChunks
        } else {
            val content = currentEpisode?.content ?: novel.content
            TextCleaner.splitIntoChunks(content)
        }
    }

    val listState = rememberLazyListState()
    var showSettings by remember { mutableStateOf(false) }
    var showEpisodeList by remember { mutableStateOf(false) }

    // 自動スクロール
    LaunchedEffect(currentChunkIndex) {
        if (isSpeaking && currentChunkIndex >= 0 && currentChunkIndex < displayChunks.size) {
            listState.animateScrollToItem(currentChunkIndex)
        }
    }

    // エピソードが切り替わったらトップにスクロール（再生中でない場合）
    LaunchedEffect(currentEpisodeIndex) {
        if (!isSpeaking) {
            listState.scrollToItem(0)
        }
    }

    Scaffold(
        topBar = {
            ReaderTopBar(
                title = novel.title,
                subtitle = currentEpisode?.title,
                episodeIndex = currentEpisodeIndex,
                totalEpisodes = readerEpisodes.size,
                sleepTimerMinutes = viewModel.sleepTimerMinutes,
                onBack = {
                    viewModel.stopSpeaking()
                    onBack()
                },
                onSetTimer = { viewModel.setSleepTimer(it) },
                onOpenEpisodeList = { showEpisodeList = true }
            )
        },
        bottomBar = {
            ReaderBottomBar(
                viewModel = viewModel,
                novel = novel,
                currentEpisode = currentEpisode,
                isSpeaking = isSpeaking,
                currentChunkIndex = currentChunkIndex,
                totalChunks = displayChunks.size,
                showSettings = showSettings,
                onToggleSettings = { showSettings = !showSettings },
                onPreviousEpisode = { viewModel.moveToPreviousEpisode() },
                onNextEpisode = { viewModel.moveToNextEpisode() },
                hasPrevious = currentEpisodeIndex > 0,
                hasNext = currentEpisodeIndex < readerEpisodes.size - 1,
                hasMultipleEpisodes = readerEpisodes.size > 1
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

        if (showEpisodeList) {
            EpisodeListSheet(
                episodes = readerEpisodes,
                currentIndex = currentEpisodeIndex,
                onEpisodeSelect = { index ->
                    viewModel.selectEpisode(index)
                    showEpisodeList = false
                },
                onDismiss = { showEpisodeList = false }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EpisodeListSheet(
    episodes: List<com.example.ondokuapp.model.Episode>,
    currentIndex: Int,
    onEpisodeSelect: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = stringResource(R.string.episode_list),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )
            
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp)
            ) {
                itemsIndexed(episodes) { index, episode ->
                    val isSelected = index == currentIndex
                    ListItem(
                        modifier = Modifier.clickable { onEpisodeSelect(index) },
                        headlineContent = {
                            Text(
                                text = stringResource(R.string.episode_number, index + 1) + ". " + episode.title,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        supportingContent = {
                            if (isSelected) {
                                Text(
                                    text = stringResource(R.string.current_episode),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        trailingContent = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (episode.currentPosition > 0) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = MaterialTheme.colorScheme.tertiaryContainer,
                                        modifier = Modifier.padding(end = 8.dp)
                                    ) {
                                        Text(
                                            text = stringResource(R.string.reading_status_in_progress),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onTertiaryContainer
                                        )
                                    }
                                }
                                if (isSelected) {
                                    Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        },
                        colors = ListItemDefaults.colors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else Color.Transparent
                        )
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReaderTopBar(
    title: String,
    subtitle: String?,
    episodeIndex: Int,
    totalEpisodes: Int,
    sleepTimerMinutes: Int,
    onBack: () -> Unit,
    onSetTimer: (Int) -> Unit,
    onOpenEpisodeList: () -> Unit
) {
    var showTimerMenu by remember { mutableStateOf(false) }

    TopAppBar(
        title = { 
            Column {
                Text(
                    text = title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
            }
        },
        actions = {
            if (totalEpisodes > 1) {
                IconButton(onClick = onOpenEpisodeList) {
                    Icon(Icons.AutoMirrored.Filled.FormatListBulleted, contentDescription = stringResource(R.string.episode_list))
                }
                Text(
                    text = stringResource(R.string.episode_count, episodeIndex + 1, totalEpisodes),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
            Box {
                IconButton(onClick = { showTimerMenu = true }) {
                    BadgedBox(
                        badge = {
                            if (sleepTimerMinutes > 0) {
                                Badge { Text(sleepTimerMinutes.toString()) }
                            }
                        }
                    ) {
                        Icon(Icons.Default.Timer, contentDescription = stringResource(R.string.sleep_timer))
                    }
                }
                DropdownMenu(expanded = showTimerMenu, onDismissRequest = { showTimerMenu = false }) {
                    listOf(0, 5, 10, 15, 30, 60).forEach { mins ->
                        DropdownMenuItem(
                            text = { Text(if (mins == 0) stringResource(R.string.off) else stringResource(R.string.minutes, mins)) },
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
    currentEpisode: com.example.ondokuapp.model.Episode?,
    isSpeaking: Boolean,
    currentChunkIndex: Int,
    totalChunks: Int,
    showSettings: Boolean,
    onToggleSettings: () -> Unit,
    onPreviousEpisode: () -> Unit,
    onNextEpisode: () -> Unit,
    hasPrevious: Boolean,
    hasNext: Boolean,
    hasMultipleEpisodes: Boolean
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
                hasProgress = (currentEpisode?.currentPosition ?: novel.currentPosition) > 0
            )

            if (hasMultipleEpisodes) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onPreviousEpisode,
                        enabled = hasPrevious,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.NavigateBefore, null)
                        Text(stringResource(R.string.previous_episode), style = MaterialTheme.typography.labelMedium)
                    }
                    TextButton(
                        onClick = onNextEpisode,
                        enabled = hasNext,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.next_episode), style = MaterialTheme.typography.labelMedium)
                        Icon(Icons.AutoMirrored.Filled.NavigateNext, null)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            PlaybackControls(
                isSpeaking = isSpeaking,
                hasProgress = (currentEpisode?.currentPosition ?: novel.currentPosition) > 0,
                onStart = { 
                    val fromStart = viewModel.consumeReaderStartFromBeginning()
                    if (currentEpisode != null) {
                        viewModel.startSpeakingEpisode(novel, currentEpisode, fromStart = fromStart)
                    } else {
                        viewModel.startSpeaking(novel, fromStart = fromStart)
                    }
                },
                onRestart = { 
                    if (currentEpisode != null) {
                        viewModel.startSpeakingEpisode(novel, currentEpisode, fromStart = true)
                    } else {
                        viewModel.startSpeaking(novel, fromStart = true)
                    }
                },
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
                    autoPlayNext = viewModel.autoPlayNextEpisode,
                    onSettingsChange = { viewModel.updateSpeechSettings(it) },
                    onToggleAutoPlay = { viewModel.toggleAutoPlayNextEpisode() }
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
        isSpeaking -> stringResource(R.string.status_playing)
        hasProgress -> stringResource(R.string.status_paused)
        else -> stringResource(R.string.label_not_read)
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
                contentDescription = if (showSettings) stringResource(R.string.close_settings) else stringResource(R.string.open_settings)
            )
        }

        // 停止ボタン
        OutlinedIconButton(
            onClick = onStop,
            modifier = Modifier.size(48.dp),
            enabled = isSpeaking || hasProgress
        ) {
            Icon(Icons.Default.Stop, contentDescription = stringResource(R.string.stop))
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
                Text(stringResource(R.string.pause))
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
                Text(if (hasProgress) stringResource(R.string.resume) else stringResource(R.string.play))
            }
        }

        // 最初から再生ボタン
        if (!isSpeaking && hasProgress) {
            FilledTonalIconButton(
                onClick = onRestart,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.Replay, contentDescription = stringResource(R.string.restart))
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
    autoPlayNext: Boolean,
    onSettingsChange: (com.example.ondokuapp.settings.SpeechSettings) -> Unit,
    onToggleAutoPlay: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(top = 16.dp)
            .fillMaxWidth()
    ) {
        HorizontalDivider(modifier = Modifier.padding(bottom = 16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.reading_settings),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.auto_play_next),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(8.dp))
                Switch(
                    checked = autoPlayNext,
                    onCheckedChange = { onToggleAutoPlay() },
                    modifier = Modifier.scale(0.8f)
                )
            }
        }
        
        Spacer(Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.label_speed, speed), style = MaterialTheme.typography.labelLarge, modifier = Modifier.width(96.dp))
            Slider(
                value = speed,
                onValueChange = { onSettingsChange(com.example.ondokuapp.settings.SpeechSettings(speed = it, pitch = pitch)) },
                valueRange = 0.5f..3.0f,
                modifier = Modifier.weight(1f)
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.label_pitch, pitch), style = MaterialTheme.typography.labelLarge, modifier = Modifier.width(96.dp))
            Slider(
                value = pitch,
                onValueChange = { onSettingsChange(com.example.ondokuapp.settings.SpeechSettings(speed = speed, pitch = it)) },
                valueRange = 0.5f..2.0f,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
