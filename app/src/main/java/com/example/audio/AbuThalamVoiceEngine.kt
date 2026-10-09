package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
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
    private var pendingSpeech: String? = null

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
                pendingSpeech?.let { text ->
                    speakInternal(text, 1.0f, 1.0f)
                    pendingSpeech = null
                }
            }
        } else {
            Log.e(tag, "TTS onInit failed with status: $status")
            isInitialized = false
            _engineState.value = VoiceEngineState.ERROR
        }
    }

    fun speak(text: String, speechRate: Float = 1.0f, pitch: Float = 1.0f, playChimeFirst: Boolean = true) {
        if (playChimeFirst) {
            playAlertChime()
            mainHandler.postDelayed({
                speakInternal(text, speechRate, pitch)
            }, 300)
        } else {
            speakInternal(text, speechRate, pitch)
        }
    }

    private fun speakInternal(text: String, speechRate: Float, pitch: Float) {
        if (!isInitialized || tts == null) {
            pendingSpeech = text
            initializeTts()
            return
        }

        requestAudioFocus()

        tts?.let { engine ->
            engine.setSpeechRate(speechRate.coerceIn(0.5f, 2.0f))
            engine.setPitch(pitch.coerceIn(0.5f, 2.0f))

            val params = Bundle().apply {
                putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, AudioManager.STREAM_ALARM)
                putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f)
            }
            engine.speak(text, TextToSpeech.QUEUE_FLUSH, params, "abu_thalam_${System.currentTimeMillis()}")
        }
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
