# DAWForge
Android DAW built with Kotlin + Jetpack Compose.

## Phase 1
- Low-latency float PCM engine (AudioTrack, LOW_LATENCY mode)
- Polyphonic synth (sine/saw/square) with attack/release
- 32-step sequencer, BPM control, playhead
- Piano roll (tap to add/remove notes, audition on tap)
- Multi-track: volume, mute, waveform per track

## Build
Push to GitHub: Actions builds `dawforge-debug-apk` automatically.
Locally (Android SDK + Gradle 8.9): `gradle assembleDebug`.

See ROADMAP.md for upcoming phases.
