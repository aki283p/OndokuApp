package com.example.ondokuapp.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.unit.dp
import com.example.ondokuapp.MainViewModel
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
    
    val clipboardManager = LocalClipboardManager.current
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (novel == null) "小説の追加" else "小説の編集") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "戻る")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            viewModel.upsertNovel(title, content, novel?.id ?: 0L, url)
                            onBack()
                        },
                        enabled = content.isNotBlank() && !viewModel.isImporting
                    ) {
                        Text("保存")
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
                    Text("Webから取り込む", style = MaterialTheme.typography.titleSmall)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = url,
                            onValueChange = { url = it },
                            label = { Text("小説のURL") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        IconButton(onClick = {
                            clipboardManager.getText()?.text?.let { url = it }
                        }) {
                            Icon(Icons.Default.ContentPaste, contentDescription = "貼り付け")
                        }
                    }
                    Button(
                        onClick = {
                            viewModel.importFromUrl(url) { t, c ->
                                title = t
                                content = c
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
                            Text("URLから取得")
                        }
                    }
                    viewModel.importError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("タイトル (空の場合は本文冒頭から生成)") },
                modifier = Modifier.fillMaxWidth()
            )

            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("本文", style = MaterialTheme.typography.titleSmall)
                    TextButton(onClick = { content = viewModel.cleanText(content) }) {
                        Icon(Icons.Default.Translate, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("本文を整形")
                    }
                }
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 200.dp)
                )
            }
        }
    }
}
