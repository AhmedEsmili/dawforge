package com.dawforge.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dawforge.model.*
import kotlinx.coroutines.flow.first

private val BLACK = setOf(1, 3, 6, 8, 10)

@Composable
fun PianoRoll(track: Track, accent: Color, playhead: Int, onAudition: (Int) -> Unit, modifier: Modifier = Modifier) {
    val cellW = 44.dp
    val cellH = 30.dp
    val rows = Project.PITCH_HI - Project.PITCH_LO + 1
    val cw = with(LocalDensity.current) { cellW.toPx() }
    val rh = with(LocalDensity.current) { cellH.toPx() }
    val v = rememberScrollState()
    val h = rememberScrollState()
    LaunchedEffect(Unit) { snapshotFlow { v.maxValue }.first { it > 0 }; v.scrollTo((10 * rh).toInt()) }

    Box(modifier.clip(RoundedCornerShape(24.dp)).background(Color(0xFF14131D))) {
        Row(Modifier.fillMaxSize()) {
            // Piano keys
            Column(Modifier.width(52.dp).verticalScroll(v)) {
                for (p in Project.PITCH_HI downTo Project.PITCH_LO) {
                    val black = p % 12 in BLACK
                    Box(
                        Modifier.fillMaxWidth().height(cellH).padding(bottom = 1.dp).background(Color(0xFFE9E4F5)),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        if (black) Box(
                            Modifier.fillMaxWidth(0.62f).fillMaxHeight()
                                .clip(RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp)).background(Color(0xFF0B0A12))
                        )
                        if (p % 12 == 0) Text(
                            "C${p / 12 - 1}", fontSize = 11.sp, color = Color(0xFF3A3550),
                            modifier = Modifier.align(Alignment.CenterStart).padding(start = 6.dp)
                        )
                    }
                }
            }
            // Grid
            Box(Modifier.weight(1f).fillMaxHeight().verticalScroll(v).horizontalScroll(h)) {
                Canvas(
                    Modifier.size(cellW * Project.STEPS, cellH * rows).pointerInput(track) {
                        detectTapGestures { o ->
                            val step = (o.x / cw).toInt().coerceIn(0, Project.STEPS - 1)
                            val pitch = Project.PITCH_HI - (o.y / rh).toInt().coerceIn(0, rows - 1)
                            if (track.toggle(step, pitch)) onAudition(pitch)
                        }
                    }
                ) {
                    for (r in 0 until rows) {
                        val p = Project.PITCH_HI - r
                        val c = when { p % 12 == 0 -> Color(0xFF2B2940); p % 12 in BLACK -> Color(0xFF181723); else -> Color(0xFF1F1E2E) }
                        drawRect(c, Offset(0f, r * rh), Size(size.width, rh))
                    }
                    for (s in 0..Project.STEPS) {
                        val bar = s % 16 == 0; val beat = s % 4 == 0
                        drawLine(
                            if (bar) Color(0xFF6B668A) else if (beat) Color(0xFF3F3C57) else Color(0xFF2A2840),
                            Offset(s * cw, 0f), Offset(s * cw, size.height), if (bar) 3f else if (beat) 2f else 1f
                        )
                    }
                    if (playhead >= 0) {
                        drawRect(accent.copy(alpha = 0.14f), Offset(playhead * cw, 0f), Size(cw, size.height))
                        drawLine(accent, Offset(playhead * cw, 0f), Offset(playhead * cw, size.height), 4f)
                    }
                    for (n in track.notes) {
                        val tl = Offset(n.step * cw + 3, (Project.PITCH_HI - n.pitch) * rh + 3)
                        val sz = Size(n.length * cw - 6, rh - 6)
                        drawRoundRect(accent, tl, sz, CornerRadius(10f))
                        drawRoundRect(Color.White.copy(alpha = 0.35f), tl, Size(6f, sz.height), CornerRadius(3f))
                    }
                }
            }
        }
        if (track.notes.isEmpty()) Text(
            "Tap the grid to add notes", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp,
            modifier = Modifier.align(Alignment.Center).clip(RoundedCornerShape(50))
                .background(Color(0xCC242335)).padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}
