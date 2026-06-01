package com.example.ondokuapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ondokuapp.model.Novel
import com.example.ondokuapp.ui.screen.DictionaryScreen
import com.example.ondokuapp.ui.screen.NovelEditScreen
import com.example.ondokuapp.ui.screen.NovelListScreen
import com.example.ondokuapp.ui.screen.ReaderScreen
import com.example.ondokuapp.ui.theme.OndokuAppTheme

sealed class Screen {
    data object List : Screen()
    data class Edit(val novel: Novel? = null) : Screen()
    data class Reader(val novel: Novel) : Screen()
    data object Dictionary : Screen()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OndokuAppTheme {
                AppRoot()
            }
        }
    }
}

@Composable
fun AppRoot() {
    val viewModel: MainViewModel = viewModel()
    var currentScreen by remember { mutableStateOf<Screen>(Screen.List) }

    when (val screen = currentScreen) {
        is Screen.List -> {
            NovelListScreen(
                viewModel = viewModel,
                onNovelClick = { currentScreen = Screen.Reader(it) },
                onAddClick = { currentScreen = Screen.Edit(null) },
                onEditClick = { currentScreen = Screen.Edit(it) },
                onOpenDictionary = { currentScreen = Screen.Dictionary }
            )
        }
        is Screen.Edit -> {
            NovelEditScreen(
                viewModel = viewModel,
                novel = screen.novel,
                onBack = { currentScreen = Screen.List }
            )
        }
        is Screen.Reader -> {
            ReaderScreen(
                viewModel = viewModel,
                novel = screen.novel,
                onBack = { currentScreen = Screen.List }
            )
        }
        is Screen.Dictionary -> {
            DictionaryScreen(
                viewModel = viewModel,
                onBack = { currentScreen = Screen.List }
            )
        }
    }
}
