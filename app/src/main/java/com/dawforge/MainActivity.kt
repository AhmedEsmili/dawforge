package com.dawforge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import com.dawforge.audio.AudioEngine
import com.dawforge.model.Project
import com.dawforge.ui.MainScreen

class MainActivity : ComponentActivity() {
    private val project = Project()
    private lateinit var engine: AudioEngine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        engine = AudioEngine(project).also { it.start() }
        setContent { MaterialTheme(colorScheme = darkColorScheme()) { MainScreen(project, engine) } }
    }

    override fun onStop() { engine.playing = false; super.onStop() }
    override fun onDestroy() { engine.stop(); super.onDestroy() }
}
