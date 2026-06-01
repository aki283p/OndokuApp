package com.example.ondokuapp.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Spellcheck
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ondokuapp.MainViewModel
import com.example.ondokuapp.model.UserDictionaryEntry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DictionaryScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingEntry by remember { mutableStateOf<UserDictionaryEntry?>(null) }
    var entryToDelete by remember { mutableStateOf<UserDictionaryEntry?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("読み替え辞書") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "戻る")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "追加")
            }
        }
    ) { innerPadding ->
        if (viewModel.dictionaryEntries.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Spellcheck,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "辞書が空です",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = "右下の＋ボタンから追加してください",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                items(viewModel.dictionaryEntries, key = { it.id }) { entry ->
                    DictionaryItem(
                        entry = entry,
                        onEdit = { editingEntry = entry },
                        onDelete = { entryToDelete = entry }
                    )
                    HorizontalDivider()
                }
            }
        }

        if (showAddDialog) {
            DictionaryEntryDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { from, to ->
                    viewModel.addDictionaryEntry(from, to)
                    showAddDialog = false
                }
            )
        }

        editingEntry?.let { entry ->
            DictionaryEntryDialog(
                entry = entry,
                onDismiss = { editingEntry = null },
                onConfirm = { from, to ->
                    viewModel.updateDictionaryEntry(entry.copy(from = from, to = to))
                    editingEntry = null
                }
            )
        }

        entryToDelete?.let { entry ->
            AlertDialog(
                onDismissRequest = { entryToDelete = null },
                title = { Text("読み替えを削除") },
                text = { Text("「${entry.from} → ${entry.to}」を削除しますか？") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteDictionaryEntry(entry)
                            entryToDelete = null
                        }
                    ) {
                        Text("削除", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { entryToDelete = null }) {
                        Text("キャンセル")
                    }
                }
            )
        }
    }
}

@Composable
fun DictionaryItem(
    entry: UserDictionaryEntry,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    ListItem(
        headlineContent = { Text(entry.from) },
        supportingContent = { Text("→ ${entry.to}") },
        trailingContent = {
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "編集")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "削除")
                }
            }
        }
    )
}

@Composable
fun DictionaryEntryDialog(
    entry: UserDictionaryEntry? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var from by remember(entry?.id) { mutableStateOf(entry?.from ?: "") }
    var to by remember(entry?.id) { mutableStateOf(entry?.to ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (entry == null) "辞書の追加" else "辞書の編集") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = from,
                    onValueChange = { from = it },
                    label = { Text("表記 (置換前)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = to,
                    onValueChange = { to = it },
                    label = { Text("読み (置換後)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(from, to) },
                enabled = from.trim().isNotBlank() && to.trim().isNotBlank()
            ) {
                Text("確定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("キャンセル")
            }
        }
    )
}
