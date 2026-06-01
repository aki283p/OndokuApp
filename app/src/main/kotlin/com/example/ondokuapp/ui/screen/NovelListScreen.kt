package com.example.ondokuapp.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ondokuapp.MainViewModel
import com.example.ondokuapp.SortOrder
import com.example.ondokuapp.model.Novel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NovelListScreen(
    viewModel: MainViewModel,
    onNovelClick: (Novel) -> Unit,
    onAddClick: () -> Unit,
    onEditClick: (Novel) -> Unit
) {
    val novels by viewModel.novels.collectAsState()
    var novelToDelete by remember { mutableStateOf<Novel?>(null) }
    var showSortMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("OndokuApp - 本棚") },
                    actions = {
                        IconButton(onClick = { viewModel.toggleFavoriteFilter() }) {
                            Icon(
                                if (viewModel.showFavoritesOnly) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "お気に入りフィルター",
                                tint = if (viewModel.showFavoritesOnly) Color.Red else LocalContentColor.current
                            )
                        }
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "並び替え")
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("更新日順") },
                                onClick = { viewModel.updateSortOrder(SortOrder.UPDATED_AT); showSortMenu = false },
                                leadingIcon = { if (viewModel.sortOrder == SortOrder.UPDATED_AT) Icon(Icons.Default.Check, null) }
                            )
                            DropdownMenuItem(
                                text = { Text("最終再生日順") },
                                onClick = { viewModel.updateSortOrder(SortOrder.LAST_READ_AT); showSortMenu = false },
                                leadingIcon = { if (viewModel.sortOrder == SortOrder.LAST_READ_AT) Icon(Icons.Default.Check, null) }
                            )
                            DropdownMenuItem(
                                text = { Text("タイトル順") },
                                onClick = { viewModel.updateSortOrder(SortOrder.TITLE); showSortMenu = false },
                                leadingIcon = { if (viewModel.sortOrder == SortOrder.TITLE) Icon(Icons.Default.Check, null) }
                            )
                        }
                    }
                )
                SearchBar(
                    query = viewModel.searchQuery,
                    onQueryChange = { viewModel.updateSearchQuery(it) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = "追加")
            }
        }
    ) { innerPadding ->
        if (novels.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(if (viewModel.searchQuery.isEmpty()) "小説が登録されていません。\n右下の＋ボタンから追加してください。" else "見つかりませんでした。")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                items(novels, key = { it.id }) { novel ->
                    NovelListItem(
                        novel = novel,
                        onClick = { onNovelClick(novel) },
                        onEditClick = { onEditClick(novel) },
                        onDeleteClick = { novelToDelete = novel },
                        onFavoriteToggle = { viewModel.toggleFavorite(novel) }
                    )
                    HorizontalDivider()
                }
            }
        }

        novelToDelete?.let { novel ->
            AlertDialog(
                onDismissRequest = { novelToDelete = null },
                title = { Text("削除の確認") },
                text = { Text("「${novel.title}」を削除してもよろしいですか？") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.deleteNovel(novel)
                        novelToDelete = null
                    }) {
                        Text("削除", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { novelToDelete = null }) {
                        Text("キャンセル")
                    }
                }
            )
        }
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("タイトルや本文で検索") },
        leadingIcon = { Icon(Icons.Default.Search, null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, null)
                }
            }
        },
        modifier = modifier,
        singleLine = true,
        shape = MaterialTheme.shapes.medium
    )
}

@Composable
fun NovelListItem(
    novel: Novel,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onFavoriteToggle: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()) }
    val dateText = dateFormat.format(Date(novel.updatedAt))
    val lastReadText = novel.lastReadAt?.let { "最終再生: ${dateFormat.format(Date(it))}" }

    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(novel.title, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                if (novel.isFavorite) {
                    Icon(Icons.Default.Favorite, null, tint = Color.Red, modifier = Modifier.size(16.dp))
                }
            }
        },
        supportingContent = {
            Column {
                Text(
                    novel.content.take(50).replace("\n", " "),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        "更新: $dateText",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    if (lastReadText != null) {
                        Text(
                            lastReadText,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                if (novel.currentPosition > 0) {
                    LinearProgressIndicator(
                        progress = { 0.5f }, // 簡易的な進捗表示。本来は チャンク/総数 等
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp).height(2.dp)
                    )
                }
            }
        },
        trailingContent = {
            Row {
                IconButton(onClick = onFavoriteToggle) {
                    Icon(
                        if (novel.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "お気に入り",
                        tint = if (novel.isFavorite) Color.Red else LocalContentColor.current
                    )
                }
                IconButton(onClick = onEditClick) {
                    Icon(Icons.Default.Edit, contentDescription = "編集")
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(Icons.Default.Delete, contentDescription = "削除")
                }
            }
        }
    )
}
