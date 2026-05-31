package com.example.ondokuapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.ondokuapp.speech.TextToSpeechManager
import com.example.ondokuapp.ui.screen.HomeScreen
import com.example.ondokuapp.ui.theme.OndokuAppTheme

class MainActivity : ComponentActivity() {
    private lateinit var ttsManager: TextToSpeechManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        ttsManager = TextToSpeechManager(this)
        
        enableEdgeToEdge()
        setContent {
            OndokuAppTheme {
                HomeScreen(
                    onSpeak = { text -> ttsManager.speak(text) },
                    onPause = { ttsManager.pause() },
                    onStop = { ttsManager.stop() },
                    onSettingsClick = {
                        Toast.makeText(this, "設定機能は将来実装予定です", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ttsManager.shutdown()
    }
}
