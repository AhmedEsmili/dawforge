package com.dawforge.audio

import android.media.*
import androidx.compose.runtime.*
import com.dawforge.model.*
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.math.*

/** Low-latency float PCM engine: step sequencer + polyphonic synth, rendered on a dedicated thread. */
class AudioEngine(private val project: Project) {
    companion object { const val SR = 44100; private const val CHUNK = 256 }

    var playing by mutableStateOf(false)
    var playhead by mutableIntStateOf(-1)

    @Volatile private var running = false
    private var thread: Thread? = null
    private val auditions = ConcurrentLinkedQueue<Pair<Track, Int>>()

    private class Voice(val freq: Double, val wave: Wave, val gain: Float, var left: Int) {
        var ph = 0.0
        var env = 0f
        fun next(): Float {
            ph += freq / SR
            if (ph >= 1.0) ph -= 1.0
            val s = when (wave) {
                Wave.SINE -> sin(2 * PI * ph).toFloat()
                Wave.SAW -> (2 * ph - 1).toFloat()
                Wave.SQUARE -> if (ph < 0.5) 1f else -1f
            }
            env = if (left > REL) min(1f, env + ATK) else max(0f, env - 1f / REL)
            left--
            return s * env * gain
        }
        companion object { const val REL = 2000; const val ATK = 1f / 220f }
    }

    fun audition(track: Track, pitch: Int) { auditions.add(track to pitch) }

    fun start() {
        if (running) return
        running = true
        thread = Thread(::render, "daw-audio").apply { priority = Thread.MAX_PRIORITY; start() }
    }

    fun stop() {
        running = false
        thread?.join(500)
        thread = null
        playing = false
    }

    private fun hz(pitch: Int) = 440.0 * 2.0.pow((pitch - 69) / 12.0)

    private fun render() {
        val min = AudioTrack.getMinBufferSize(SR, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_FLOAT)
        val at = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()
            )
            .setAudioFormat(
                AudioFormat.Builder().setSampleRate(SR)
                    .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO).build()
            )
            .setBufferSizeInBytes(max(min, CHUNK * 2 * 4 * 2))
            .setPerformanceMode(AudioTrack.PERFORMANCE_MODE_LOW_LATENCY)
            .build()
        at.play()
        val buf = FloatArray(CHUNK * 2)
        val voices = ArrayList<Voice>()
        var wasPlaying = false
        var counter = 0.0
        var step = 0

        while (running) {
            while (true) {
                val (t, p) = auditions.poll() ?: break
                voices.add(Voice(hz(p), t.wave, t.volume, SR / 4))
            }
            val isPlaying = playing
            if (isPlaying && !wasPlaying) { step = 0; counter = 0.0 }
            if (!isPlaying && wasPlaying) playhead = -1
            wasPlaying = isPlaying
            val perStep = SR * 60.0 / project.bpm / 4.0

            for (i in 0 until CHUNK) {
                if (isPlaying) {
                    if (counter <= 0.0) {
                        playhead = step
                        for (t in project.tracks) {
                            if (t.muted) continue
                            for (n in t.snapshot) if (n.step == step)
                                voices.add(Voice(hz(n.pitch), t.wave, t.volume, (n.length * perStep).toInt()))
                        }
                        step = (step + 1) % Project.STEPS
                        counter += perStep
                    }
                    counter -= 1.0
                }
                var s = 0f
                val it = voices.iterator()
                while (it.hasNext()) {
                    val v = it.next()
                    s += v.next()
                    if (v.left <= 0) it.remove()
                }
                s = (s * 0.25f).coerceIn(-1f, 1f)
                buf[i * 2] = s
                buf[i * 2 + 1] = s
            }
            at.write(buf, 0, buf.size, AudioTrack.WRITE_BLOCKING)
        }
        at.stop()
        at.release()
    }
}
