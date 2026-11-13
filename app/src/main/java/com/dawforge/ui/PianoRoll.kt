package com.dawforge.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.dawforge.model.*

private val BLACK = setOf(1, 3, 6, 8, 10)

@Composable
fun PianoRoll(track: Track, playhead: Int, onAudition: (Int) -> Unit, modifier: Modifier = Modifier) {
    val cellW = 44.dp
    val cellH = 28.dp
    val rows = Project.PITCH_HI - Project.PITCH_LO + 1
    val cw = with(LocalDensity.current) { cellW.toPx() }
    val rh = with(LocalDensity.current) { cellH.toPx() }
    Box(modifier.verticalScroll(rememberScrollState()).horizontalScroll(rememberScrollState())) {
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
                val pitch = Project.PITCH_HI - r
                drawRect(
                    if (pitch % 12 in BLACK) Color(0xFF1B1B22) else Color(0xFF26262F),
                    Offset(0f, r * rh), Size(size.width, rh)
                )
                if (pitch % 12 == 0) drawLine(Color(0xFF555566), Offset(0f, (r + 1) * rh), Offset(size.width, (r + 1) * rh), 2f)
            }
            for (s in 0..Project.STEPS) {
                drawLine(
                    if (s % 4 == 0) Color(0xFF555566) else Color(0xFF33333D),
                    Offset(s * cw, 0f), Offset(s * cw, size.height), if (s % 4 == 0) 2f else 1f
                )
            }
            if (playhead >= 0) drawRect(Color(0x33FFFFFF), Offset(playhead * cw, 0f), Size(cw, size.height))
            for (n in track.notes) {
                drawRoundRect(
                    Color(0xFF7C4DFF),
                    Offset(n.step * cw + 2, (Project.PITCH_HI - n.pitch) * rh + 2),
                    Size(n.length * cw - 4, rh - 4), CornerRadius(6f)
                )
            }
        }
    }
}
