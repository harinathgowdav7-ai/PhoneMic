package com.phonemic.app.audio

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.media.audiofx.AcousticEchoCanceler
import android.media.audiofx.AutomaticGainControl
import android.media.audiofx.NoiseSuppressor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.sqrt

/**
 * Captures live low-latency PCM 16-bit Mono audio using Android's [AudioRecord] API,
 * computes real-time RMS dBFS levels and a 33-bar spectrum envelope for the UI,
 * and emits 10 ms audio frames to the network transmitter.
 */
class AudioCaptureEngine(
    private val scope: CoroutineScope,
    private val onAudioFrame: (pcmBytes: ByteArray, length: Int, rmsDb: Float, spectrum33: List<Float>) -> Unit
) {
    private var audioRecord: AudioRecord? = null
    private var captureJob: Job? = null

    @Volatile
    var isMuted: Boolean = false

    @SuppressLint("MissingPermission")
    fun startCapture(sampleRate: Int = 48000) {
        stopCapture()

        val channelConfig = AudioFormat.CHANNEL_IN_MONO
        val encoding = AudioFormat.ENCODING_PCM_16BIT
        val frameSamples = sampleRate / 100 // 10 ms frame = 480 samples at 48 kHz
        val frameBytes = frameSamples * 2   // 960 bytes

        val minBufSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, encoding)
            .coerceAtLeast(frameBytes * 4)

        val record = AudioRecord(
            MediaRecorder.AudioSource.VOICE_COMMUNICATION,
            sampleRate,
            channelConfig,
            encoding,
            minBufSize
        )

        if (record.state != AudioRecord.STATE_INITIALIZED) {
            record.release()
            return
        }

        // Enable hardware noise suppression & echo cancellation where supported
        runCatching {
            if (NoiseSuppressor.isAvailable()) {
                NoiseSuppressor.create(record.audioSessionId)?.enabled = true
            }
            if (AcousticEchoCanceler.isAvailable()) {
                AcousticEchoCanceler.create(record.audioSessionId)?.enabled = true
            }
            if (AutomaticGainControl.isAvailable()) {
                AutomaticGainControl.create(record.audioSessionId)?.enabled = true
            }
        }

        audioRecord = record
        record.startRecording()

        captureJob = scope.launch(Dispatchers.IO) {
            val pcmBuffer = ByteArray(frameBytes)
            val shortBuffer = ShortArray(frameSamples)

            while (isActive) {
                val readBytes = record.read(pcmBuffer, 0, frameBytes)
                if (readBytes > 0) {
                    if (isMuted) {
                        pcmBuffer.fill(0, 0, readBytes)
                        val mutedBars = List(33) { 0.08f }
                        onAudioFrame(pcmBuffer, readBytes, -60.0f, mutedBars)
                    } else {
                        val samplesCount = readBytes / 2
                        ByteBuffer.wrap(pcmBuffer, 0, readBytes)
                            .order(ByteOrder.LITTLE_ENDIAN)
                            .asShortBuffer()
                            .get(shortBuffer, 0, samplesCount)

                        val rmsDb = computeRmsDb(shortBuffer, samplesCount)
                        val bars = compute33BarEnvelope(shortBuffer, samplesCount)
                        onAudioFrame(pcmBuffer, readBytes, rmsDb, bars)
                    }
                }
            }
        }
    }

    fun stopCapture() {
        captureJob?.cancel()
        captureJob = null
        runCatching {
            audioRecord?.stop()
            audioRecord?.release()
        }
        audioRecord = null
    }

    companion object {
        fun computeRmsDb(samples: ShortArray, count: Int): Float {
            if (count <= 0) return -60.0f
            var sumSq = 0.0
            for (i in 0 until count) {
                val normalized = samples[i] / 32768.0
                sumSq += normalized * normalized
            }
            val rms = sqrt(sumSq / count)
            if (rms <= 1e-5) return -60.0f
            return (20.0 * log10(rms)).toFloat().coerceIn(-60.0f, 0.0f)
        }

        fun compute33BarEnvelope(samples: ShortArray, count: Int): List<Float> {
            val barCount = 33
            if (count <= 0) return List(barCount) { 0.1f }
            val chunk = (count / barCount).coerceAtLeast(1)
            return List(barCount) { idx ->
                val start = (idx * chunk).coerceAtMost(count - 1)
                val end = ((idx + 1) * chunk).coerceAtMost(count)
                var peak = 0f
                for (i in start until end) {
                    val v = abs(samples[i].toInt()) / 32768f
                    if (v > peak) peak = v
                }
                // Apply perceptual scaling so conversational speech looks lively and accurate
                val boosted = sqrt(peak).coerceIn(0.08f, 0.98f)
                boosted
            }
        }
    }
}
