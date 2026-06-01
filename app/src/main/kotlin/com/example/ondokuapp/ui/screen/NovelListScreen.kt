package com.example.ondokuapp.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.example.ondokuapp.MainViewModel
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("OndokuApp - 小説一覧") }
            )
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
                Text("小説が登録されていません。\n右下の＋ボタンから追加してください。")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                items(novels) { novel ->
                    NovelListItem(
                        novel = novel,
                        onClick = { onNovelClick(novel) },
                        onEditClick = { onEditClick(novel) },
                        onDeleteClick = { novelToDelete = novel }
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
fun NovelListItem(
    novel: Novel,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()) }
    val dateText = dateFormat.format(Date(novel.updatedAt))

    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = {
            Text(novel.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        supportingContent = {
            Column {
                Text(
                    novel.content.take(50).replace("\n", " "),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    "最終更新: $dateText",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        },
        trailingContent = {
            Row {
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
