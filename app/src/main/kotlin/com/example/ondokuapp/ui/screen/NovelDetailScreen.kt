package com.example.ondokuapp.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ondokuapp.MainViewModel
import com.example.ondokuapp.R
import com.example.ondokuapp.model.Episode
import com.example.ondokuapp.model.Novel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NovelDetailScreen(
    viewModel: MainViewModel,
    novel: Novel,
    onBack: () -> Unit,
    onStartReader: () -> Unit
) {
    val episodes = viewModel.detailEpisodes
    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()) }

    LaunchedEffect(novel.id) {
        viewModel.loadEpisodesForDetail(novel.id)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.novel_detail)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleFavorite(novel) }) {
                        Icon(
                            imageVector = if (novel.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = stringResource(R.string.favorite_filter),
                            tint = if (novel.isFavorite) Color.Red else LocalContentColor.current
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 作品情報カード
            item {
                NovelInfoCard(novel, episodes.size, dateFormat)
            }

            // 操作ボタン
            item {
                ActionButtons(
                    onContinue = {
                        val lastReadId = novel.lastReadEpisodeId
                        val index = if (lastReadId != null) {
                            episodes.indexOfFirst { it.id == lastReadId }.coerceAtLeast(0)
                        } else {
                            0
                        }
                        viewModel.prepareReaderEpisode(novel.id, index)
                        onStartReader()
                    },
                    onStartFromBegin = {
                        viewModel.prepareReaderEpisode(novel.id, 0)
                        onStartReader()
                    }
                )
            }

            // Episode一覧見出し
            item {
                Text(
                    text = stringResource(R.string.saved_episodes),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )
            }

            // Episode一覧
            itemsIndexed(episodes) { index, episode ->
                EpisodeItem(
                    index = index,
                    episode = episode,
                    isLastRead = episode.id == novel.lastReadEpisodeId,
                    onClick = {
                        viewModel.prepareReaderEpisode(novel.id, index)
                        onStartReader()
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
            }
            
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun NovelInfoCard(
    novel: Novel,
    episodeCount: Int,
    dateFormat: SimpleDateFormat
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = novel.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            
            if (novel.sourceSite != null) {
                InfoRow(stringResource(R.string.source_label), novel.sourceSite)
            }
            if (novel.sourceUrl != null) {
                InfoRow(stringResource(R.string.source_url), novel.sourceUrl, isSecondary = true)
            }
            
            val count = if (episodeCount > 0) episodeCount else 1
            InfoRow(stringResource(R.string.downloaded_episodes_label), stringResource(R.string.downloaded_episodes, count))
            
            novel.lastReadAt?.let {
                InfoRow(stringResource(R.string.last_read_at_label), dateFormat.format(Date(it)))
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String, isSecondary: Boolean = false) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
        Text(
            text = value,
            style = if (isSecondary) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
            maxLines = if (isSecondary) 1 else 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ActionButtons(
    onContinue: () -> Unit,
    onStartFromBegin: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = onContinue,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.PlayArrow, null)
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.continue_listening))
        }
        OutlinedButton(
            onClick = onStartFromBegin,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Replay, null)
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.start_from_begin))
        }
    }
}

@Composable
private fun EpisodeItem(
    index: Int,
    episode: Episode,
    isLastRead: Boolean,
    onClick: () -> Unit
) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = {
            Text(
                text = stringResource(R.string.episode_number, index + 1) + ". " + episode.title,
                fontWeight = if (isLastRead) FontWeight.Bold else FontWeight.Normal,
                color = if (isLastRead) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        },
        supportingContent = {
            if (isLastRead) {
                Text(
                    text = stringResource(R.string.last_read_episode),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        trailingContent = {
            if (episode.currentPosition > 0) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                ) {
                    Text(
                        text = stringResource(R.string.reading_status_in_progress),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        },
        colors = ListItemDefaults.colors(
            containerColor = if (isLastRead) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f) else Color.Transparent
        )
    )
}
