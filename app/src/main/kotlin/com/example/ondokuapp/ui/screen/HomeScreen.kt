package com.example.ondokuapp.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import androidx.compose.ui.res.stringResource
import com.example.ondokuapp.R

@Composable
fun HomeScreen(
    onSpeak: (String) -> Unit,
    onPause: () -> Unit,
    onStop: () -> Unit,
    onSettingsClick: () -> Unit
) {
    var text by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(title = { Text(stringResource(R.string.app_name)) })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.description),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text(stringResource(R.string.input_label)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onSpeak(text) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.start_reading))
                }
                Button(
                    onClick = onPause,
                    modifier = Modifier.weight(0.7f)
                ) {
                    Text(stringResource(R.string.pause_reading))
                }
                Button(
                    onClick = onStop,
                    modifier = Modifier.weight(0.5f)
                ) {
                    Text(stringResource(R.string.stop_reading))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onSettingsClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Text(stringResource(R.string.settings))
            }
        }
    }
}
