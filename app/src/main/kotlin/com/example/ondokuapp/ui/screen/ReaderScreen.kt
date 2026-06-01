package com.example.ondokuapp.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.ondokuapp.MainViewModel
import com.example.ondokuapp.model.Novel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    viewModel: MainViewModel,
    novel: Novel,
    onBack: () -> Unit
) {
    val speechSettings = viewModel.speechSettings
    val isSpeaking = viewModel.isSpeaking

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(novel.title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "戻る")
                    }
                }
            )
        },
        bottomBar = {
            Surface(tonalElevation = 4.dp) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    // Playback Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { viewModel.stopSpeaking() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "停止")
                        }
                        
                        if (isSpeaking) {
                            Button(onClick = { viewModel.pauseSpeaking() }) {
                                Text("一時停止")
                            }
                        } else {
                            Button(onClick = { viewModel.startSpeaking(novel) }) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "再生")
                                Spacer(Modifier.width(8.dp))
                                Text("再生")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Settings Sliders
                    Text("再生速度: ${"%.1f".format(speechSettings.speed)}", style = MaterialTheme.typography.labelMedium)
                    Slider(
                        value = speechSettings.speed,
                        onValueChange = { viewModel.updateSpeechSettings(speechSettings.copy(speed = it)) },
                        valueRange = 0.5f..3.0f
                    )

                    Text("ピッチ: ${"%.1f".format(speechSettings.pitch)}", style = MaterialTheme.typography.labelMedium)
                    Slider(
                        value = speechSettings.pitch,
                        onValueChange = { viewModel.updateSpeechSettings(speechSettings.copy(pitch = it)) },
                        valueRange = 0.5f..2.0f
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = novel.content,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
