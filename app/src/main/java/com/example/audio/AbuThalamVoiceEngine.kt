package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.ToneGenerator
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import kotlin.math.sin

class AbuThalamVoiceEngine private constructor(private val context: Context) :
    TextToSpeech.OnInitListener {

    private val tag = "AbuThalamVoice"
    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var isArabicSupported = false

    private val _engineState = MutableStateFlow(VoiceEngineState.INITIALIZING)
    val engineState: StateFlow<VoiceEngineState> = _engineState.asStateFlow()

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private val mainHandler = Handler(Looper.getMainLooper())
    private var pendingSpeech: SpeechRequest? = null

    private data class SpeechRequest(val text: String, val speechRate: Float, val pitch: Float)

    init {
        initializeTts()
    }

    private fun initializeTts() {
        try {
            _engineState.value = VoiceEngineState.INITIALIZING
            tts = TextToSpeech(context.applicationContext, this)
        } catch (e: Exception) {
            Log.e(tag, "Failed to instantiate TTS", e)
            _engineState.value = VoiceEngineState.ERROR
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.let { engine ->
                engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        Log.d(tag, "Utterance started: $utteranceId")
                    }

                    override fun onDone(utteranceId: String?) {
                        Log.d(tag, "Utterance done: $utteranceId")
                    }

                    override fun onError(utteranceId: String?) {
                        Log.e(tag, "Utterance error: $utteranceId")
                    }
                })

                val arabicLocale = Locale("ar")
                val langResult = engine.isLanguageAvailable(arabicLocale)
                if (langResult >= TextToSpeech.LANG_AVAILABLE) {
                    engine.language = arabicLocale
                    isArabicSupported = true
                    _engineState.value = VoiceEngineState.ARABIC_READY
                } else {
                    val saudiLocale = Locale("ar", "SA")
                    val saudiResult = engine.isLanguageAvailable(saudiLocale)
                    if (saudiResult >= TextToSpeech.LANG_AVAILABLE) {
                        engine.language = saudiLocale
                        isArabicSupported = true
                        _engineState.value = VoiceEngineState.ARABIC_READY
                    } else {
                        isArabicSupported = false
                        _engineState.value = VoiceEngineState.FALLBACK_LANG
                    }
                }
                isInitialized = true

                // Process pending speech if any
                pendingSpeech?.let { req ->
                    speakInternal(req.text, req.speechRate, req.pitch)
                    pendingSpeech = null
                }
            }
        } else {
            Log.e(tag, "TTS onInit failed with status: $status")
            isInitialized = false
            _engineState.value = VoiceEngineState.ERROR
        }
    }

    fun speak(
        text: String,
        speechRate: Float = 0.88f,
        pitch: Float = 0.58f,
        playMonsterEffectFirst: Boolean = true
    ) {
        if (playMonsterEffectFirst) {
            playMonsterBassImpact()
            mainHandler.postDelayed({
                speakInternal(text, speechRate, pitch)
            }, 380)
        } else {
            speakInternal(text, speechRate, pitch)
        }
    }

    private fun speakInternal(text: String, speechRate: Float, pitch: Float) {
        if (!isInitialized || tts == null) {
            pendingSpeech = SpeechRequest(text, speechRate, pitch)
            initializeTts()
            return
        }

        requestAudioFocus()
        boostVolume()

        tts?.let { engine ->
            // Beast voice pitch is 0.55f - 0.65f, deliberately deep and booming
            engine.setSpeechRate(speechRate.coerceIn(0.5f, 2.0f))
            engine.setPitch(pitch.coerceIn(0.5f, 2.0f))

            val params = Bundle().apply {
                putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, AudioManager.STREAM_ALARM)
                putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f)
            }
            engine.speak(text, TextToSpeech.QUEUE_FLUSH, params, "abu_thalam_${System.currentTimeMillis()}")
        }
    }

    /**
     * Boosts stream volume so Abu Thalam's monster alert is loud, commanding and crystal clear.
     */
    private fun boostVolume() {
        try {
            audioManager?.let { am ->
                val maxAlarm = am.getStreamMaxVolume(AudioManager.STREAM_ALARM)
                val currentAlarm = am.getStreamVolume(AudioManager.STREAM_ALARM)
                if (currentAlarm < (maxAlarm * 0.75f).toInt()) {
                    am.setStreamVolume(
                        AudioManager.STREAM_ALARM,
                        (maxAlarm * 0.85f).toInt().coerceAtLeast(1),
                        0
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Could not adjust stream volume", e)
        }
    }

    /**
     * Synthesizes a deep sub-bass cinematic impact / monster roar effect (80Hz - 160Hz)
     * using AudioTrack for an epic, authoritative intro before the spoken words!
     */
    fun playMonsterBassImpact() {
        Thread {
            try {
                val sampleRate = 22050
                val durationMs = 350
                val numSamples = (durationMs * sampleRate) / 1000
                val buffer = ShortArray(numSamples)

                for (i in 0 until numSamples) {
                    val progress = i.toDouble() / numSamples
                    // Pitch slides down from 140Hz to 65Hz (heavy monster bass impact)
                    val freq = 140.0 - (75.0 * progress)
                    val t = i.toDouble() / sampleRate
                    // Dual harmonic with exponential decay
                    val envelope = (1.0 - progress) * (1.0 - progress)
                    val sample1 = sin(2.0 * Math.PI * freq * t)
                    val sample2 = 0.5 * sin(2.0 * Math.PI * (freq * 0.5) * t) // sub-bass harmonic
                    val combined = (sample1 + sample2) * envelope
                    buffer[i] = (combined * 32767 * 0.9).toInt().toShort()
                }

                val minBufferSize = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )

                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(buffer.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(buffer, 0, buffer.size)
                audioTrack.play()

                mainHandler.postDelayed({
                    try {
                        audioTrack.stop()
                        audioTrack.release()
                    } catch (ignored: Exception) {}
                }, durationMs + 100L)

            } catch (e: Exception) {
                Log.e(tag, "AudioTrack synthesis error, falling back to tone", e)
                playAlertChime()
            }
        }.start()
    }

    fun playAlertChime() {
        try {
            val toneGen = ToneGenerator(AudioManager.STREAM_ALARM, 100)
            toneGen.startTone(ToneGenerator.TONE_CDMA_ALERT_NETWORK_LITE, 200)
            mainHandler.postDelayed({
                try {
                    toneGen.release()
                } catch (ignored: Exception) {}
            }, 500)
        } catch (e: Exception) {
            Log.e(tag, "Could not play tone chime", e)
        }
    }

    private fun requestAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val playbackAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()
            val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                .setAudioAttributes(playbackAttributes)
                .build()
            audioManager?.requestAudioFocus(focusRequest)
        } else {
            @Suppress("DEPRECATION")
            audioManager?.requestAudioFocus(
                null,
                AudioManager.STREAM_ALARM,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
            )
        }
    }

    fun stop() {
        try {
            tts?.stop()
        } catch (e: Exception) {
            Log.e(tag, "Error stopping TTS", e)
        }
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
            tts = null
            isInitialized = false
        } catch (e: Exception) {
            Log.e(tag, "Error shutting down TTS", e)
        }
    }

    enum class VoiceEngineState(val labelAr: String) {
        INITIALIZING("جاري تهيئة محرك الصوت..."),
        ARABIC_READY("محرك الصوت العربي جاهز بنجاح"),
        FALLBACK_LANG("اللغة العربية غير محملة في النظام - سيتم استخدام المحرك المتوفر"),
        ERROR("تعذر تشغيل محرك الصوت")
    }

    companion object {
        @Volatile
        private var INSTANCE: AbuThalamVoiceEngine? = null

        fun getInstance(context: Context): AbuThalamVoiceEngine {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AbuThalamVoiceEngine(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
