package com.example

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.isActive
import kotlin.math.sin

object RetroAudioSynthesizer {
    private const val SAMPLE_RATE = 22050
    private var isSoundEnabled = true
    private var musicJob: Job? = null
    private val synthScope = CoroutineScope(Dispatchers.Default)

    fun init(context: Context) {
        // Initialization if needed
    }

    fun setSoundEnabled(enabled: Boolean) {
        isSoundEnabled = enabled
        if (!enabled) {
            stopMusicLoop()
        }
    }

    fun isSoundEnabled(): Boolean = isSoundEnabled

    private var musicAudioTrack: AudioTrack? = null

    fun startMusicLoop() {
        if (!isSoundEnabled) return
        if (musicJob != null && musicJob?.isActive == true) return

        musicJob = synthScope.launch {
            try {
                val bufferSize = AudioTrack.getMinBufferSize(
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )
                if (bufferSize <= 0) return@launch

                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(SAMPLE_RATE)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                if (track.state != AudioTrack.STATE_INITIALIZED) {
                    track.release()
                    return@launch
                }

                musicAudioTrack = track
                track.play()

                // Notes frequencies
                val C3 = 130.81f
                val E3 = 164.81f
                val F3 = 174.61f
                val G3 = 196.00f
                val A3 = 220.00f

                val C4 = 261.63f
                val E4 = 329.63f
                val F4 = 349.23f
                val G4 = 392.00f
                val A4 = 440.00f
                val B4 = 493.88f

                val C5 = 523.25f
                val D5 = 587.33f
                val E5 = 659.25f
                val F5 = 698.46f
                val G5 = 783.99f
                val A5 = 880.00f
                val B5 = 987.77f
                val C6 = 1046.50f
                val E6 = 1318.51f

                // 32-step patterns
                val melodyPattern = listOf(
                    C5, E5, G5, C6,  E6, C6, G5, E5, // C Major arpeggio
                    A4, C5, E5, A5,  C6, A5, E5, C5, // A Minor arpeggio
                    F4, A4, C5, F5,  A5, F5, C5, A4, // F Major arpeggio
                    G4, B4, D5, G5,  B5, G5, D5, B4  // G Major arpeggio
                )
                val bassPattern = listOf(
                    C3, 0f, C3, 0f,  C3, 0f, C3, 0f,
                    A3, 0f, A3, 0f,  A3, 0f, A3, 0f,
                    F3, 0f, F3, 0f,  F3, 0f, F3, 0f,
                    G3, 0f, G3, 0f,  G3, 0f, G3, 0f
                )

                val stepDuration = 0.16f // 160ms per step
                val stepSamples = (stepDuration * SAMPLE_RATE).toInt()
                val shortBuffer = ShortArray(stepSamples)

                var melodyPhase = 0.0
                var bassPhase = 0.0
                var stepIndex = 0

                while (isActive && isSoundEnabled) {
                    val melFreq = melodyPattern[stepIndex]
                    val bassFreq = bassPattern[stepIndex]

                    for (i in 0 until stepSamples) {
                        // Melody oscillator (25% pulse wave, decaying)
                        var melVal = 0.0
                        if (melFreq > 0) {
                            melodyPhase += 2.0 * Math.PI * melFreq / SAMPLE_RATE
                            val sinVal = sin(melodyPhase)
                            val waveVal = if (sinVal > 0.5) 1.0 else -1.0
                            val env = 1.0 - (i.toFloat() / stepSamples)
                            melVal = waveVal * env * 0.06f // Soft melody volume
                        }

                        // Bass oscillator (triangle wave)
                        var bassVal = 0.0
                        if (bassFreq > 0) {
                            bassPhase += 2.0 * Math.PI * bassFreq / SAMPLE_RATE
                            val triVal = Math.abs((bassPhase % (2.0 * Math.PI)) / Math.PI - 1.0) * 2.0 - 1.0
                            val env = 1.0 - (i.toFloat() / stepSamples) * 0.4
                            bassVal = triVal * env * 0.06f // Soft warm bass volume
                        }

                        val mixed = melVal + bassVal
                        shortBuffer[i] = (mixed * 32767).toInt().coerceIn(-32768, 32767).toShort()
                    }

                    track.write(shortBuffer, 0, shortBuffer.size)
                    stepIndex = (stepIndex + 1) % 32
                }
            } catch (e: Exception) {
                // Graceful handle
            } finally {
                try {
                    musicAudioTrack?.stop()
                    musicAudioTrack?.release()
                } catch (e: Exception) {
                    // ignore
                }
                musicAudioTrack = null
            }
        }
    }

    fun stopMusicLoop() {
        musicJob?.cancel()
        musicJob = null
        try {
            musicAudioTrack?.stop()
            musicAudioTrack?.release()
        } catch (e: Exception) {
            // ignore
        }
        musicAudioTrack = null
    }

    fun playFlap() {
        if (!isSoundEnabled) return
        playSweep(400f, 800f, 0.08f)
    }

    fun playScore() {
        if (!isSoundEnabled) return
        CoroutineScope(Dispatchers.Default).launch {
            playTone(600f, 0.05f)
            kotlinx.coroutines.delay(40)
            playTone(900f, 0.10f)
        }
    }

    fun playCrash() {
        if (!isSoundEnabled) return
        playSweep(300f, 80f, 0.35f, isNoise = true)
    }

    fun playRecord() {
        if (!isSoundEnabled) return
        CoroutineScope(Dispatchers.Default).launch {
            val notes = listOf(523.25f, 587.33f, 659.25f, 783.99f) // C5, D5, E5, G5
            for (note in notes) {
                playTone(note, 0.08f)
                kotlinx.coroutines.delay(80)
            }
        }
    }

    fun playCoin() {
        if (!isSoundEnabled) return
        CoroutineScope(Dispatchers.Default).launch {
            playTone(987.77f, 0.08f) // B5
            kotlinx.coroutines.delay(60)
            playTone(1318.51f, 0.15f) // E6
        }
    }

    private fun playTone(frequency: Float, durationSeconds: Float, volume: Float = 0.8f) {
        try {
            val numSamples = (durationSeconds * SAMPLE_RATE).toInt()
            val sample = FloatArray(numSamples)
            for (i in 0 until numSamples) {
                val t = i.toFloat() / SAMPLE_RATE
                sample[i] = sin(2 * Math.PI * frequency * t).toFloat() * volume
            }
            writeAndPlayFloat(sample)
        } catch (e: Exception) {
            // Graceful handle
        }
    }

    private fun playSweep(startFreq: Float, endFreq: Float, durationSeconds: Float, isNoise: Boolean = false) {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val numSamples = (durationSeconds * SAMPLE_RATE).toInt()
                val sample = FloatArray(numSamples)
                for (i in 0 until numSamples) {
                    val progress = i.toFloat() / numSamples
                    val freq = startFreq + (endFreq - startFreq) * progress
                    val t = i.toFloat() / SAMPLE_RATE
                    var v = sin(2 * Math.PI * freq * t).toFloat()
                    if (isNoise) {
                        val noise = (Math.random() * 2.0 - 1.0).toFloat()
                        v = v * 0.4f + noise * 0.6f
                    }
                    sample[i] = v
                }
                writeAndPlayFloat(sample)
            } catch (e: Exception) {
                // Graceful handle
            }
        }
    }

    private fun writeAndPlayFloat(samples: FloatArray) {
        try {
            val bufferSize = samples.size * 2
            val minBufferSize = AudioTrack.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            if (minBufferSize <= 0) return
            val finalBufferSize = maxOf(bufferSize, minBufferSize)

            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(finalBufferSize)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            if (audioTrack.state != AudioTrack.STATE_INITIALIZED) {
                audioTrack.release()
                return
            }

            val shortSamples = ShortArray(samples.size)
            for (i in samples.indices) {
                val fadeOutLength = (samples.size * 0.15f).toInt()
                val fadeOutFactor = if (i > samples.size - fadeOutLength) {
                    (samples.size - i).toFloat() / fadeOutLength
                } else {
                    1.0f
                }
                shortSamples[i] = (samples[i] * 32767 * fadeOutFactor).toInt().coerceIn(-32768, 32767).toShort()
            }

            audioTrack.write(shortSamples, 0, shortSamples.size)
            audioTrack.play()
            
            CoroutineScope(Dispatchers.Default).launch {
                kotlinx.coroutines.delay((samples.size.toFloat() / SAMPLE_RATE * 1000).toLong() + 200)
                try {
                    audioTrack.stop()
                    audioTrack.release()
                } catch (e: Exception) {
                    // ignore
                }
            }
        } catch (e: Exception) {
            // Graceful handle
        }
    }
}

