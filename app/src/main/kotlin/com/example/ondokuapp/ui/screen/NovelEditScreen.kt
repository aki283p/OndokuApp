package com.example.ondokuapp.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ondokuapp.MainViewModel
import com.example.ondokuapp.R
import com.example.ondokuapp.model.Novel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NovelEditScreen(
    viewModel: MainViewModel,
    novel: Novel? = null,
    onBack: () -> Unit
) {
    var title by remember { mutableStateOf(novel?.title ?: "") }
    var content by remember { mutableStateOf(novel?.content ?: "") }
    var url by remember { mutableStateOf(novel?.sourceUrl ?: "") }
    
    var showOverwriteDialog by remember { mutableStateOf(false) }
    var pendingImportData by remember { mutableStateOf<Pair<String, String>?>(null) }

    val clipboardManager = LocalClipboardManager.current
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        viewModel.clearDetectedEpisodeLinks()
    }

    // 新規保存または更新が可能かどうかの判定
    val canSave = content.isNotBlank() && (novel != null || url.isNotBlank())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (novel == null) stringResource(R.string.add_novel) else stringResource(R.string.edit_novel)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cancel))
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            viewModel.upsertNovel(title, content, novel?.id ?: 0L, url)
                            onBack()
                        },
                        enabled = canSave && !viewModel.isImporting
                    ) {
                        Text(stringResource(R.string.save))
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // URL Import Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.import_from_web), style = MaterialTheme.typography.titleSmall)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = url,
                            onValueChange = { url = it },
                            label = { Text(stringResource(R.string.source_url)) },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        IconButton(onClick = {
                            clipboardManager.getText()?.text?.let { url = it }
                        }) {
                            Icon(Icons.Default.ContentPaste, contentDescription = stringResource(R.string.paste_url))
                        }
                    }
                    Button(
                        onClick = {
                            viewModel.importFromUrl(url) { t, c ->
                                if (content.isNotBlank()) {
                                    pendingImportData = t to c
                                    showOverwriteDialog = true
                                } else {
                                    title = t
                                    content = c
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = url.isNotBlank() && !viewModel.isImporting
                    ) {
                        if (viewModel.isImporting) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Download, null)
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.import_from_url))
                        }
                    }
                    viewModel.importError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }

                    // 話一覧確認ボタン (追加分)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            viewModel.detectEpisodeLinks(url)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = url.isNotBlank() && !viewModel.isDetectingEpisodeLinks,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        if (viewModel.isDetectingEpisodeLinks) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onSecondary)
                        } else {
                            Icon(Icons.AutoMirrored.Filled.List, null)
                            Spacer(Modifier.width(8.dp))
                            Text("話一覧を確認")
                        }
                    }

                    if (viewModel.detectedEpisodeLinks.isNotEmpty() || viewModel.episodeLinkDetectionError != null) {
                        Column(modifier = Modifier.padding(top = 4.dp)) {
                            if (viewModel.detectedEpisodeLinks.isNotEmpty()) {
                                Text(
                                    "${viewModel.detectedEpisodeLinks.size}件の話リンクを検出しました",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                viewModel.detectedEpisodeLinks.take(5).forEach { link ->
                                    Text(
                                        "・${link.title}",
                                        style = MaterialTheme.typography.labelSmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (viewModel.detectedEpisodeLinks.size > 5) {
                                    Text("...", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            viewModel.episodeLinkDetectionError?.let {
                                Text(
                                    it, 
                                    color = if (viewModel.detectedEpisodeLinks.isEmpty()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline, 
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }

            if (content.isNotBlank()) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.novel_title)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // 本文プレビュー (読み取り専用)
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            stringResource(R.string.content_preview),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        stringResource(R.string.manual_input_not_available),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = content.take(500),
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 10,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (content.length > 500) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    stringResource(R.string.preview_note),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }
            } else {
                // 未取得時のガイド
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        stringResource(R.string.error_import_required),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }

        if (showOverwriteDialog) {
            AlertDialog(
                onDismissRequest = { showOverwriteDialog = false },
                title = { Text(stringResource(R.string.confirm_overwrite)) },
                text = { Text(stringResource(R.string.confirm_overwrite_message)) },
                confirmButton = {
                    TextButton(onClick = {
                        pendingImportData?.let { (t, c) ->
                            title = t
                            content = c
                        }
                        showOverwriteDialog = false
                    }) {
                        Text(stringResource(R.string.save))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showOverwriteDialog = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }
    }
}
