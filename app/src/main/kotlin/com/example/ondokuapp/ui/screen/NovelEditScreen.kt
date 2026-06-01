package com.example.ondokuapp.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
                            viewModel.upsertNovel(title, content, novel?.id ?: 0L)
                            onBack()
                        },
                        enabled = content.isNotBlank()
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("タイトル (空の場合は本文冒頭から生成)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("本文") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }
    }
}
