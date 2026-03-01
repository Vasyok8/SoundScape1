package com.soundscape.core.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import com.soundscape.core.domain.model.GeneratorType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

/**
 * Генератор шумов и тонов через Android AudioTrack API.
 *
 * Поддерживает:
 * - White Noise  — равномерный спектр, случайные сэмплы
 * - Pink Noise   — алгоритм Paul Kellet (Voss-McCartney), спад 3dB/октаву
 * - Brown Noise  — интеграл белого шума, спад 6dB/октаву, глубокий звук
 * - Binaural Beats — разные синусы в L/R каналах (только в наушниках!)
 * - Isochronic Tones — AM-модулированный синус (работает без наушников)
 * - Solfeggio 528 Hz — частота трансформации
 *
 * Каждый экземпляр работает в отдельном корутинном потоке.
 */
class NoiseGenerator(
    private val generatorType: GeneratorType,
    private val sampleRate: Int = 44100,
    private val bufferSamples: Int = 4096
) {
    companion object {
        private const val TAG = "NoiseGenerator"
        private const val BINAURAL_CARRIER_HZ = 200.0
        private const val BEAT_ALPHA = 10.0
        private const val BEAT_THETA = 6.0
        private const val BEAT_DELTA = 2.0
        private const val BEAT_BETA = 20.0
        private const val SOLFEGGIO_528_HZ = 528.0
    }

    private var audioTrack: AudioTrack? = null
    private var generatorJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    @Volatile var volume: Float = 0.5f
        set(value) { field = value.coerceIn(0f, 1f) }

    @Volatile private var isRunning = false

    // --- Pink Noise state (Paul Kellet filter) ---
    private var b0=0.0; private var b1=0.0; private var b2=0.0; private var b3=0.0
    private var b4=0.0; private var b5=0.0; private var b6=0.0

    // --- Brown Noise state ---
    private var brownLast = 0.0

    // --- Oscillator phases ---
    private var phaseL = 0.0
    private var phaseR = 0.0
    private var isoPhase = 0.0

    fun start() {
        if (isRunning) return
        isRunning = true

        val minBuf = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_STEREO,
            AudioFormat.ENCODING_PCM_FLOAT
        )
        val trackBuf = maxOf(bufferSamples * 4 * 2, minBuf) // *2 стерео, *4 байт/float

        audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                    .build()
            )
            .setBufferSizeInBytes(trackBuf)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        audioTrack?.play()
        Log.d(TAG, "NoiseGenerator started: $generatorType")

        generatorJob = scope.launch {
            // Стерео буфер: [L0, R0, L1, R1, ...]
            val buf = FloatArray(bufferSamples * 2)
            while (isActive && isRunning) {
                fill(buf)
                audioTrack?.write(buf, 0, buf.size, AudioTrack.WRITE_BLOCKING)
            }
        }
    }

    fun stop() {
        isRunning = false
        generatorJob?.cancel()
        generatorJob = null
        try {
            audioTrack?.pause()
            audioTrack?.flush()
            audioTrack?.stop()
            audioTrack?.release()
        } catch (e: Exception) {
            Log.w(TAG, "Error stopping: ${e.message}")
        }
        audioTrack = null
    }

    private fun fill(buf: FloatArray) {
        val vol = volume
        val n = buf.size / 2   // количество стерео-фреймов
        when (generatorType) {
            GeneratorType.WHITE_NOISE     -> fillWhite(buf, n, vol)
            GeneratorType.PINK_NOISE      -> fillPink(buf, n, vol)
            GeneratorType.BROWN_NOISE     -> fillBrown(buf, n, vol)
            GeneratorType.BINAURAL_ALPHA  -> fillBinaural(buf, n, vol, BEAT_ALPHA)
            GeneratorType.BINAURAL_THETA  -> fillBinaural(buf, n, vol, BEAT_THETA)
            GeneratorType.BINAURAL_DELTA  -> fillBinaural(buf, n, vol, BEAT_DELTA)
            GeneratorType.BINAURAL_BETA   -> fillBinaural(buf, n, vol, BEAT_BETA)
            GeneratorType.ISOCHRONIC_ALPHA -> fillIsochronic(buf, n, vol, BEAT_ALPHA)
            GeneratorType.SOLFEGGIO_528   -> fillSolfeggio(buf, n, vol, SOLFEGGIO_528_HZ)
        }
    }

    // ─── Белый шум ───────────────────────────────────────────────
    private fun fillWhite(buf: FloatArray, n: Int, vol: Float) {
        for (i in 0 until n) {
            val s = (Random.nextFloat() * 2f - 1f) * vol * 0.5f
            buf[i * 2] = s; buf[i * 2 + 1] = s
        }
    }

    // ─── Розовый шум (Paul Kellet) ───────────────────────────────
    private fun fillPink(buf: FloatArray, n: Int, vol: Float) {
        for (i in 0 until n) {
            val w = Random.nextDouble() * 2.0 - 1.0
            b0 = 0.99886*b0 + w*0.0555179
            b1 = 0.99332*b1 + w*0.0750759
            b2 = 0.96900*b2 + w*0.1538520
            b3 = 0.86650*b3 + w*0.3104856
            b4 = 0.55000*b4 + w*0.5329522
            b5 = -0.7616*b5 - w*0.0168980
            b6 = w * 0.115926
            val p = ((b0+b1+b2+b3+b4+b5+b6+w*0.5362)/9.0).coerceIn(-1.0,1.0).toFloat()
            val s = p * vol * 0.4f
            buf[i * 2] = s; buf[i * 2 + 1] = s
        }
    }

    // ─── Коричневый шум ──────────────────────────────────────────
    private fun fillBrown(buf: FloatArray, n: Int, vol: Float) {
        for (i in 0 until n) {
            val w = Random.nextDouble() * 2.0 - 1.0
            brownLast = (brownLast + 0.02 * w) / 1.02
            val s = (brownLast * 3.5).coerceIn(-1.0, 1.0).toFloat() * vol * 0.6f
            buf[i * 2] = s; buf[i * 2 + 1] = s
        }
    }

    // ─── Бинауральные биения ─────────────────────────────────────
    private fun fillBinaural(buf: FloatArray, n: Int, vol: Float, beatHz: Double) {
        val incL = 2.0 * PI * BINAURAL_CARRIER_HZ / sampleRate
        val incR = 2.0 * PI * (BINAURAL_CARRIER_HZ + beatHz) / sampleRate
        for (i in 0 until n) {
            buf[i * 2]     = (sin(phaseL) * vol * 0.35).toFloat()
            buf[i * 2 + 1] = (sin(phaseR) * vol * 0.35).toFloat()
            phaseL += incL; if (phaseL > 2*PI) phaseL -= 2*PI
            phaseR += incR; if (phaseR > 2*PI) phaseR -= 2*PI
        }
    }

    // ─── Изохронные тоны ─────────────────────────────────────────
    private fun fillIsochronic(buf: FloatArray, n: Int, vol: Float, beatHz: Double) {
        val carrierInc = 2.0 * PI * BINAURAL_CARRIER_HZ / sampleRate
        val isoInc = 2.0 * PI * beatHz / sampleRate
        for (i in 0 until n) {
            val env = (sin(isoPhase) + 1.0) / 2.0   // огибающая 0..1
            val s = (sin(phaseL) * env * vol * 0.45).toFloat()
            buf[i * 2] = s; buf[i * 2 + 1] = s
            phaseL += carrierInc; if (phaseL > 2*PI) phaseL -= 2*PI
            isoPhase += isoInc; if (isoPhase > 2*PI) isoPhase -= 2*PI
        }
    }

    // ─── Solfeggio синус ─────────────────────────────────────────
    private fun fillSolfeggio(buf: FloatArray, n: Int, vol: Float, freqHz: Double) {
        val inc = 2.0 * PI * freqHz / sampleRate
        for (i in 0 until n) {
            val s = (sin(phaseL) * vol * 0.3).toFloat()
            buf[i * 2] = s; buf[i * 2 + 1] = s
            phaseL += inc; if (phaseL > 2*PI) phaseL -= 2*PI
        }
    }
}
