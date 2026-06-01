package com.example.ondokuapp.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
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
    onEditClick: (Novel) -> Unit,
    onOpenDictionary: () -> Unit
) {
    val novels by viewModel.novels.collectAsState()
    var novelToDelete by remember { mutableStateOf<Novel?>(null) }

    Scaffold(
        topBar = {
            BookshelfTopBar(
                viewModel = viewModel,
                onOpenDictionary = onOpenDictionary
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_novel))
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            BookshelfSearchAndFilters(viewModel = viewModel)
            
            if (novels.isEmpty()) {
                EmptyBookshelfMessage(
                    isSearchActive = viewModel.searchQuery.isNotEmpty() || viewModel.showFavoritesOnly
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(novels, key = { it.id }) { novel ->
                        NovelListItem(
                            novel = novel,
                            onClick = { onNovelClick(novel) },
                            onEditClick = { onEditClick(novel) },
                            onDeleteClick = { novelToDelete = novel },
                            onFavoriteToggle = { viewModel.toggleFavorite(novel) }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }
            }
        }

        novelToDelete?.let { novel ->
            DeleteConfirmDialog(
                novelTitle = novel.title,
                onConfirm = {
                    viewModel.deleteNovel(novel)
                    novelToDelete = null
                },
                onDismiss = { novelToDelete = null }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookshelfTopBar(
    viewModel: MainViewModel,
    onOpenDictionary: () -> Unit
) {
    var showSortMenu by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }

    TopAppBar(
        title = { Text(stringResource(R.string.bookshelf), fontWeight = FontWeight.Bold) },
        actions = {
            IconButton(onClick = { viewModel.toggleFavoriteFilter() }) {
                Icon(
                    imageVector = if (viewModel.showFavoritesOnly) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "お気に入りフィルター",
                    tint = if (viewModel.showFavoritesOnly) Color.Red else LocalContentColor.current
                )
            }
            IconButton(onClick = { showSortMenu = true }) {
                Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "並び替え")
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
            
            IconButton(onClick = { showMoreMenu = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "メニュー")
            }
            DropdownMenu(
                expanded = showMoreMenu,
                onDismissRequest = { showMoreMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("読み替え辞書") },
                    onClick = { onOpenDictionary(); showMoreMenu = false },
                    leadingIcon = { Icon(Icons.Default.Spellcheck, null) }
                )
            }
        }
    )
}

@Composable
private fun BookshelfSearchAndFilters(
    viewModel: MainViewModel
) {
    OutlinedTextField(
        value = viewModel.searchQuery,
        onValueChange = { viewModel.updateSearchQuery(it) },
        placeholder = { Text("タイトルや本文で検索...", style = MaterialTheme.typography.bodyMedium) },
        leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(20.dp)) },
        trailingIcon = {
            if (viewModel.searchQuery.isNotEmpty()) {
                IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                    Icon(Icons.Default.Clear, null)
                }
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    )
}

@Composable
private fun EmptyBookshelfMessage(isSearchActive: Boolean) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = if (isSearchActive) Icons.Default.SearchOff else Icons.Default.AutoStories,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (isSearchActive) stringResource(R.string.empty_search_result) else stringResource(R.string.empty_bookshelf),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.outline
            )
            if (!isSearchActive) {
                Text(
                    text = "右下の＋ボタンから追加してください",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
private fun NovelListItem(
    novel: Novel,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onFavoriteToggle: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()) }
    val updateDateText = dateFormat.format(Date(novel.updatedAt))
    val lastReadDateText = novel.lastReadAt?.let { dateFormat.format(Date(it)) } ?: "未再生"
    
    var showMenu by remember { mutableStateOf(false) }

    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (novel.sourceUrl != null) {
                    Box(
                        modifier = Modifier
                            .padding(end = 6.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            "Web",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .padding(end = 6.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            "手入力",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text(
                    text = novel.title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
            }
        },
        supportingContent = {
            Column(modifier = Modifier.padding(top = 4.dp)) {
                Text(
                    text = novel.content.take(60).replace("\n", " "),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "更新: $updateDateText",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = "再生: $lastReadDateText",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (novel.lastReadAt != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        )
                    }
                    
                    if (novel.currentPosition > 0) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text(
                                text = "読みかけ",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }
            }
        },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onFavoriteToggle) {
                    Icon(
                        imageVector = if (novel.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "お気に入り",
                        tint = if (novel.isFavorite) Color.Red else MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "操作",
                            tint = MaterialTheme.colorScheme.outline
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("編集") },
                            onClick = { onEditClick(); showMenu = false },
                            leadingIcon = { Icon(Icons.Default.Edit, null) }
                        )
                        DropdownMenuItem(
                            text = { Text("削除", color = MaterialTheme.colorScheme.error) },
                            onClick = { onDeleteClick(); showMenu = false },
                            leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) }
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun DeleteConfirmDialog(
    novelTitle: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("小説の削除") },
        text = { Text("「$novelTitle」を本棚から削除しますか？\nこの操作は取り消せません。") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("削除", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("キャンセル")
            }
        }
    )
}
