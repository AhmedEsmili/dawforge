package com.dawforge.model

import androidx.compose.runtime.*

enum class Wave { SINE, SAW, SQUARE }

/** A note placed on a 16th-note step grid. */
data class Note(val pitch: Int, val step: Int, val length: Int = 1)

class Track(val name: String, wave: Wave = Wave.SAW) {
    var wave by mutableStateOf(wave)
    var volume by mutableFloatStateOf(0.8f)
    var muted by mutableStateOf(false)
    val notes = mutableStateListOf<Note>()

    /** Immutable copy that is safe to read from the audio thread. */
    @Volatile var snapshot: List<Note> = emptyList()
        private set

    /** @return true if a note was added, false if removed. */
    fun toggle(step: Int, pitch: Int): Boolean {
        val existing = notes.find { it.step == step && it.pitch == pitch }
        val added = existing == null
        if (existing != null) notes.remove(existing) else notes.add(Note(pitch, step))
        snapshot = notes.toList()
        return added
    }

    fun clear() { notes.clear(); snapshot = emptyList() }
}

class Project {
    var bpm by mutableIntStateOf(120)
    val tracks = mutableStateListOf(Track("Lead", Wave.SAW), Track("Bass", Wave.SQUARE))
    companion object { const val STEPS = 32; const val PITCH_LO = 36; const val PITCH_HI = 83 }
}
