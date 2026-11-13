package com.dawforge

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.dawforge.audio.AudioEngine
import com.dawforge.model.Project
import com.dawforge.ui.DawTheme
import com.dawforge.ui.MainScreen

class MainActivity : ComponentActivity() {
    private val project = Project()
    private lateinit var engine: AudioEngine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        engine = AudioEngine(project).also { it.start() }
        setContent { DawTheme { MainScreen(project, engine) } }
    }

    override fun onStop() { engine.playing = false; super.onStop() }
    override fun onDestroy() { engine.stop(); super.onDestroy() }
}
