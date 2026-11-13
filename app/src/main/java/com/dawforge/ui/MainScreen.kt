package com.dawforge.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dawforge.audio.AudioEngine
import com.dawforge.model.*

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(p: Project, e: AudioEngine) {
    var sel by remember { mutableIntStateOf(0) }
    val t = p.tracks[sel.coerceIn(0, p.tracks.lastIndex)]
    Column(
        Modifier.fillMaxSize().background(Color(0xFF121218)).statusBarsPadding().navigationBarsPadding().padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { e.playing = !e.playing }) { Text(if (e.playing) "Stop" else "Play") }
            OutlinedButton(onClick = { p.bpm = (p.bpm - 5).coerceAtLeast(40) }) { Text("-") }
            Text("${p.bpm} BPM", color = Color.White)
            OutlinedButton(onClick = { p.bpm = (p.bpm + 5).coerceAtMost(240) }) { Text("+") }
            OutlinedButton(onClick = { t.clear() }) { Text("Clear") }
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            p.tracks.forEachIndexed { i, tr ->
                FilterChip(selected = sel == i, onClick = { sel = i }, label = { Text(tr.name) })
            }
            AssistChip(
                onClick = { p.tracks.add(Track("Track ${p.tracks.size + 1}")); sel = p.tracks.lastIndex },
                label = { Text("+ Track") }
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Wave.values().forEach { w ->
                FilterChip(selected = t.wave == w, onClick = { t.wave = w }, label = { Text(w.name) })
            }
            FilterChip(selected = t.muted, onClick = { t.muted = !t.muted }, label = { Text("Mute") })
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Vol", color = Color.White)
            Slider(t.volume, { t.volume = it }, Modifier.padding(horizontal = 8.dp))
        }
        PianoRoll(t, e.playhead, { e.audition(t, it) }, Modifier.weight(1f).fillMaxWidth())
    }
}
