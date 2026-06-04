package com.example.ondokuapp.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Spellcheck
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.ondokuapp.MainViewModel
import com.example.ondokuapp.R
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
                title = { Text(stringResource(R.string.dictionary)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add))
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
                        text = stringResource(R.string.dictionary_empty),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = stringResource(R.string.dictionary_empty_guide),
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
                        onDelete = { entryToDelete = entry },
                        onTestPlay = { viewModel.speakTest(entry.to) }
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
                },
                onTestPlay = { viewModel.speakTest(it) }
            )
        }

        editingEntry?.let { entry ->
            DictionaryEntryDialog(
                entry = entry,
                onDismiss = { editingEntry = null },
                onConfirm = { from, to ->
                    viewModel.updateDictionaryEntry(entry.copy(from = from, to = to))
                    editingEntry = null
                },
                onTestPlay = { viewModel.speakTest(it) }
            )
        }

        entryToDelete?.let { entry ->
            AlertDialog(
                onDismissRequest = { entryToDelete = null },
                title = { Text(stringResource(R.string.delete_dictionary_entry)) },
                text = { Text(stringResource(R.string.confirm_delete_dictionary_entry, entry.from, entry.to)) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteDictionaryEntry(entry)
                            entryToDelete = null
                        }
                    ) {
                        Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { entryToDelete = null }) {
                        Text(stringResource(R.string.cancel))
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
    onDelete: () -> Unit,
    onTestPlay: () -> Unit
) {
    ListItem(
        headlineContent = { Text(entry.from) },
        supportingContent = { Text("→ ${entry.to}") },
        trailingContent = {
            Row {
                IconButton(onClick = onTestPlay) {
                    Icon(Icons.Default.PlayArrow, contentDescription = stringResource(R.string.test_playback))
                }
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete))
                }
            }
        }
    )
}

@Composable
fun DictionaryEntryDialog(
    entry: UserDictionaryEntry? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit,
    onTestPlay: (String) -> Unit
) {
    var from by remember(entry?.id) { mutableStateOf(entry?.from ?: "") }
    var to by remember(entry?.id) { mutableStateOf(entry?.to ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (entry == null) stringResource(R.string.add_dictionary_entry) else stringResource(R.string.edit_dictionary_entry)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = from,
                    onValueChange = { from = it },
                    label = { Text(stringResource(R.string.label_from)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = to,
                    onValueChange = { to = it },
                    label = { Text(stringResource(R.string.label_to)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Button(
                    onClick = { onTestPlay(to) },
                    modifier = Modifier.align(Alignment.End),
                    enabled = to.isNotBlank()
                ) {
                    Icon(Icons.Default.PlayArrow, null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.test_playback))
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(from, to) },
                enabled = from.trim().isNotBlank() && to.trim().isNotBlank()
            ) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
