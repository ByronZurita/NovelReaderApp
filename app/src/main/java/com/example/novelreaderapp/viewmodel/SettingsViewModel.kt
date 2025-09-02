package com.example.novelreaderapp.viewmodel

import android.app.Application
import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.core.text.HtmlCompat
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

/**
 * Extension property for accessing DataStore for user settings.
 * Currently unused, but can be used for persisting settings (like font size, theme, etc.)
 */
private val Context.dataStore by preferencesDataStore(name = "user_settings")

/**
 * ViewModel responsible for managing user settings and TTS playback.
 * Extends AndroidViewModel to get access to Application context for TTS initialization.
 */
class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    // Holds current HTML content string (e.g. chapter content)
    private val _htmlContent = MutableStateFlow("")
    val htmlContent: StateFlow<String> = _htmlContent

    /**
     * Updates the stored HTML content.
     * @param content new HTML content string
     */
    fun setHtmlContent(content: String) {
        _htmlContent.value = content
    }

    // Text-to-Speech engine instance, initialized in init block
    private lateinit var tts: TextToSpeech

    // State to track if TTS is currently speaking
    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking

    // Font size state (default 18f)
    private val _fontSize = MutableStateFlow(18f)
    val fontSize: StateFlow<Float> = _fontSize

    /**
     * Update font size preference.
     * @param newSize new font size in float
     */
    fun setFontSize(newSize: Float) {
        _fontSize.value = newSize
    }

    private val appContext = getApplication<Application>().applicationContext

    /**
     * Initialize TextToSpeech engine and set up listeners.
     * This runs once when ViewModel is created.
     */
    init {
        tts = TextToSpeech(appContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // Set default language to device locale
                tts.language = Locale.getDefault()
            }
        }

        // Listener to update speaking state flow based on TTS events
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _isSpeaking.value = true
            }

            override fun onDone(utteranceId: String?) {
                _isSpeaking.value = false
            }

            override fun onError(utteranceId: String?) {
                _isSpeaking.value = false
            }
        })
    }

    /**
     * Starts TTS playback asynchronously.
     * Converts HTML content to plain text and speaks in chunks.
     * @param htmlContent raw HTML string to speak
     */
    fun startTTSAsync(htmlContent: String) {
        viewModelScope.launch {
            val plainText = HtmlCompat.fromHtml(htmlContent, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
            speakInChunks(plainText)
        }
    }

    /**
     * Toggles TTS playback on or off depending on current speaking state.
     * If speaking, stops; otherwise starts speaking the passed HTML content.
     * @param htmlContent raw HTML string to speak
     */
    fun toggleTTS(htmlContent: String) {
        val plainText = HtmlCompat.fromHtml(htmlContent, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()

        if (_isSpeaking.value) {
            stopTTS()
        } else {
            speakInChunks(plainText)
        }
    }

    /**
     * Splits the text into chunks to avoid exceeding TTS maximum length limit,
     * and feeds them into the TTS engine sequentially.
     * @param text plain text to speak
     */
    private fun speakInChunks(text: String) {
        val maxLength = 4000 // Max chunk size for TTS engine
        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "tts_chunk")
        }

        var start = 0
        while (start < text.length) {
            val end = (start + maxLength).coerceAtMost(text.length)
            val chunk = text.substring(start, end)
            val queueMode = if (start == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
            tts.speak(chunk, queueMode, params, "chunk_$start")
            start = end
        }
    }

    /**
     * Stops any ongoing TTS playback and updates speaking state.
     */
    fun stopTTS() {
        if (::tts.isInitialized) {
            tts.stop()
            _isSpeaking.value = false
        }
    }

    /**
     * Clean up TTS resources when ViewModel is destroyed.
     */
    override fun onCleared() {
        super.onCleared()
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
    }
}
