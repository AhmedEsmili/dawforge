@file:OptIn(ExperimentalMaterial3Api::class)

package com.dawforge.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dawforge.audio.AudioEngine
import com.dawforge.model.*

@Composable
fun MainScreen(p: Project, e: AudioEngine) {
    var sel by remember { mutableIntStateOf(0) }
    val idx = sel.coerceIn(0, p.tracks.lastIndex)
    val t = p.tracks[idx]
    val accent = trackColor(idx)
    Column(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
            .statusBarsPadding().navigationBarsPadding().padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(Modifier.padding(horizontal = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(12.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
            Spacer(Modifier.width(10.dp))
            Text("DAWForge", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.weight(1f))
            Text("Sequencer", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Transport(p, e, t)
        TrackStrip(p, idx) { sel = it }
        TrackPanel(t, accent)
        PianoRoll(t, accent, e.playhead, { e.audition(t, it) }, Modifier.weight(1f).fillMaxWidth())
    }
}

@Composable
private fun Transport(p: Project, e: AudioEngine, t: Track) {
    val cs = MaterialTheme.colorScheme
    val corner by animateDpAsState(if (e.playing) 18.dp else 32.dp, label = "corner")
    val icon = cs.onPrimary
    Surface(shape = RoundedCornerShape(32.dp), color = cs.surfaceContainer) {
        Row(
            Modifier.fillMaxWidth().padding(10.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                Modifier.size(64.dp).clip(RoundedCornerShape(corner)).background(cs.primary)
                    .clickable { e.playing = !e.playing },
                contentAlignment = Alignment.Center
            ) {
                Canvas(Modifier.size(24.dp)) {
                    if (e.playing) drawRoundRect(icon, cornerRadius = CornerRadius(5.dp.toPx()))
                    else drawPath(Path().apply {
                        moveTo(size.width * 0.12f, 0f); lineTo(size.width, size.height / 2)
                        lineTo(size.width * 0.12f, size.height); close()
                    }, icon)
                }
            }
            Surface(shape = CircleShape, color = cs.surfaceContainerHigh) {
                Row(Modifier.padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    FilledTonalIconButton(onClick = { p.bpm = (p.bpm - 5).coerceAtLeast(40) }) { Text("−", fontSize = 20.sp) }
                    Column(Modifier.width(64.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${p.bpm}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = cs.onSurface)
                        Text("BPM", style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant)
                    }
                    FilledTonalIconButton(onClick = { p.bpm = (p.bpm + 5).coerceAtMost(240) }) { Text("+", fontSize = 20.sp) }
                }
            }
            TextButton(onClick = { t.clear() }) { Text("Clear") }
        }
    }
}

@Composable
private fun TrackStrip(p: Project, selected: Int, onSelect: (Int) -> Unit) {
    val cs = MaterialTheme.colorScheme
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        itemsIndexed(p.tracks) { i, tr ->
            val c = trackColor(i)
            val on = i == selected
            Surface(
                onClick = { onSelect(i) }, shape = RoundedCornerShape(20.dp),
                color = if (on) c.copy(alpha = 0.18f) else cs.surfaceContainer,
                border = if (on) BorderStroke(1.5.dp, c) else null, modifier = Modifier.width(128.dp)
            ) {
                Column(Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(10.dp).clip(CircleShape).background(if (tr.muted) cs.outline else c))
                        Spacer(Modifier.width(8.dp))
                        Text(tr.name, maxLines = 1, fontWeight = FontWeight.SemiBold, color = cs.onSurface)
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "${tr.notes.size} notes · ${tr.wave.name.lowercase()}",
                        style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant
                    )
                }
            }
        }
        item {
            Surface(
                onClick = { p.tracks.add(Track("Track ${p.tracks.size + 1}")); onSelect(p.tracks.lastIndex) },
                shape = RoundedCornerShape(20.dp), color = cs.surface,
                border = BorderStroke(1.dp, cs.outlineVariant), modifier = Modifier.height(56.dp).width(64.dp)
            ) { Box(contentAlignment = Alignment.Center) { Text("+", fontSize = 24.sp, color = cs.primary) } }
        }
    }
}

@Composable
private fun TrackPanel(t: Track, accent: androidx.compose.ui.graphics.Color) {
    val cs = MaterialTheme.colorScheme
    Surface(shape = RoundedCornerShape(24.dp), color = cs.surfaceContainer) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                Wave.values().forEachIndexed { i, w ->
                    SegmentedButton(
                        selected = t.wave == w, onClick = { t.wave = w },
                        shape = SegmentedButtonDefaults.itemShape(i, Wave.values().size),
                        label = { Text(w.name.lowercase().replaceFirstChar { it.uppercase() }) }
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("VOL", style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
                Slider(
                    t.volume, { t.volume = it }, Modifier.weight(1f).padding(horizontal = 10.dp),
                    colors = SliderDefaults.colors(thumbColor = accent, activeTrackColor = accent)
                )
                Text("${(t.volume * 100).toInt()}", Modifier.width(28.dp), color = cs.onSurface)
                Spacer(Modifier.width(8.dp))
                FilterChip(selected = t.muted, onClick = { t.muted = !t.muted }, label = { Text("Mute") })
            }
        }
    }
}
