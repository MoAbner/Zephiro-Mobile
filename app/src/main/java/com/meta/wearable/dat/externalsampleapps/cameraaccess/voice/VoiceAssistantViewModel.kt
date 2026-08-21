package com.meta.wearable.dat.externalsampleapps.cameraaccess.voice

import android.app.Application
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import java.util.Locale
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class VoiceAssistantViewModel(application: Application) : AndroidViewModel(application) {
  private val wakeWordProfile = SherpaWakeWordConfig.activeProfile
  private val _uiState = MutableStateFlow(VoiceAssistantUiState())
  val uiState: StateFlow<VoiceAssistantUiState> = _uiState.asStateFlow()

  private val _commands = MutableSharedFlow<VoiceCommand>()
  val commands: SharedFlow<VoiceCommand> = _commands.asSharedFlow()

  private val ttsManager = TtsManager(application)
  private val mainHandler = Handler(Looper.getMainLooper())
  private var speechRecognizer: SpeechRecognizer? = null
  private var wakeWordCooldown = false
  @Volatile private var recordingWithPhoneMicActive = false

  private val wakeWordEngine =
      WakeWordEngineFactory.create(
          context = application,
          onDetected = ::onWakeWordDetected,
          onError = ::onWakeWordError,
      )

  init {
    _uiState.update { it.copy(isWakeWordConfigured = wakeWordEngine.isConfigured) }
  }

  fun startListening() {
    if (handleRecordingMicConflict()) {
      return
    }

    val context = getApplication<Application>()
    if (!SpeechRecognizer.isRecognitionAvailable(context)) {
      val feedback = "Reconhecimento de voz indisponivel neste dispositivo."
      _uiState.update { it.copy(isAvailable = false, lastFeedback = feedback) }
      ttsManager.speak(feedback)
      return
    }

    val recognizer = speechRecognizer ?: createSpeechRecognizer().also { speechRecognizer = it }
    runCatching { recognizer.startListening(buildRecognizerIntent()) }
        .onSuccess {
          _uiState.update { it.copy(isListening = true, lastFeedback = "Ouvindo comando.") }
        }
        .onFailure {
          val feedback =
              "Nao foi possivel iniciar a escuta do comando. Verifique se o microfone nao esta em uso por outra funcao."
          _uiState.update { state -> state.copy(isListening = false, lastFeedback = feedback) }
          ttsManager.speak(feedback)
          restartWakeWordIfNeeded()
        }
  }

  fun stopListening() {
    speechRecognizer?.stopListening()
    _uiState.update { it.copy(isListening = false) }
  }

  fun updateRecordingContext(recordingWithMic: Boolean) {
    recordingWithPhoneMicActive = recordingWithMic
  }

  fun setHandsFreeEnabled(enabled: Boolean) {
    if (enabled) {
      if (!wakeWordEngine.isConfigured) {
        val feedback = "Wake word ainda nao configurada. Use o botao manual por enquanto."
        _uiState.update {
          it.copy(
              isHandsFreeEnabled = false,
              isWakeWordConfigured = false,
              lastFeedback = feedback,
          )
        }
        ttsManager.speak(feedback)
        return
      }

      val started = wakeWordEngine.start()
      val failureFeedback =
          _uiState.value.lastFeedback
              ?: "A wake word retornou falha sem diagnostico. Verifique assets, microfone e logs do sherpa."
      val successFeedback = wakeWordProfile.activationPrompt
      _uiState.update {
        it.copy(
            isHandsFreeEnabled = started,
            isWakeWordConfigured = wakeWordEngine.isConfigured,
            lastFeedback =
                if (started) successFeedback
                else failureFeedback ?: "Falha ao ativar wake word.",
        )
      }
      if (started) {
        ttsManager.speak(successFeedback)
      } else {
        ttsManager.speak(_uiState.value.lastFeedback ?: "Falha ao ativar wake word.")
      }
    } else {
      wakeWordEngine.stop()
      _uiState.update {
        it.copy(isHandsFreeEnabled = false, lastFeedback = "Modo hands-free desativado.")
      }
      ttsManager.speak("Modo hands-free desativado.")
    }
  }

  fun speak(text: String, flush: Boolean = true) {
    _uiState.update { it.copy(lastFeedback = text) }
    ttsManager.speak(text, flush)
  }

  private fun createSpeechRecognizer(): SpeechRecognizer {
    val context = getApplication<Application>()
    val recognizer =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            SpeechRecognizer.isOnDeviceRecognitionAvailable(context)) {
          SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
        } else {
          SpeechRecognizer.createSpeechRecognizer(context)
        }

    recognizer.setRecognitionListener(
        object : RecognitionListener {
          override fun onReadyForSpeech(params: Bundle?) = Unit

          override fun onBeginningOfSpeech() = Unit

          override fun onRmsChanged(rmsdB: Float) = Unit

          override fun onBufferReceived(buffer: ByteArray?) = Unit

          override fun onEndOfSpeech() {
            _uiState.update { it.copy(isListening = false) }
            restartWakeWordIfNeeded()
          }

          override fun onError(error: Int) {
            val feedback = speechRecognizerErrorMessage(error)
            _uiState.update { it.copy(isListening = false, lastFeedback = feedback) }
            ttsManager.speak(feedback)
            restartWakeWordIfNeeded()
          }

          override fun onResults(results: Bundle?) {
            val transcript =
                results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
            _uiState.update { it.copy(isListening = false, lastTranscript = transcript) }
            if (transcript.isNullOrBlank()) {
              val feedback = "Nenhum comando reconhecido."
              _uiState.update { it.copy(lastFeedback = feedback) }
              ttsManager.speak(feedback)
              restartWakeWordIfNeeded()
              return
            }

            val command = CommandNormalizer.parse(transcript)
            viewModelScope.launch { _commands.emit(command) }
            restartWakeWordIfNeeded()
          }

          override fun onPartialResults(partialResults: Bundle?) = Unit

          override fun onEvent(eventType: Int, params: Bundle?) = Unit
        }
    )

    return recognizer
  }

  private fun buildRecognizerIntent(): Intent =
      Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale("pt", "BR"))
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "pt-BR")
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
        putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
        putExtra(RecognizerIntent.EXTRA_PROMPT, "Diga um comando")
      }

  private fun onWakeWordDetected() {
    if (wakeWordCooldown || _uiState.value.isListening) {
      return
    }

    if (handleRecordingMicConflict()) {
      return
    }

    wakeWordCooldown = true
    _uiState.update { it.copy(lastFeedback = "Wake word detectada. Ouvindo comando.") }
    mainHandler.post { startListening() }
  }

  private fun handleRecordingMicConflict(): Boolean {
    if (!recordingWithPhoneMicActive) {
      return false
    }

    wakeWordCooldown = true
    val feedback =
        "Gravacao com audio detectada. Encerrando a gravacao para liberar os proximos comandos de voz."
    _uiState.update { it.copy(isListening = false, lastFeedback = feedback) }
    ttsManager.speak(feedback)
    viewModelScope.launch { _commands.emit(VoiceCommand.StopRecording) }
    restartWakeWordIfNeeded()
    return true
  }

  private fun speechRecognizerErrorMessage(error: Int): String =
      when (error) {
        SpeechRecognizer.ERROR_RECOGNIZER_BUSY,
        SpeechRecognizer.ERROR_AUDIO ->
            "O microfone parece estar ocupado por outra funcao, como a gravacao com audio. Desative o microfone da gravacao para manter os comandos de voz."
        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS ->
            "Permissao de microfone indisponivel para ouvir o comando."
        SpeechRecognizer.ERROR_NO_MATCH,
        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Nao consegui entender o comando."
        else -> "Nao consegui entender o comando."
      }

  private fun onWakeWordError(message: String) {
    _uiState.update {
      it.copy(
          isHandsFreeEnabled = false,
          isWakeWordConfigured = wakeWordEngine.isConfigured,
          lastFeedback = message,
      )
    }
    ttsManager.speak(message)
  }

  private fun restartWakeWordIfNeeded() {
    if (!_uiState.value.isHandsFreeEnabled) {
      wakeWordCooldown = false
      return
    }

    mainHandler.postDelayed(
        {
          wakeWordCooldown = false
          wakeWordEngine.start()
        },
        800L,
    )
  }

  override fun onCleared() {
    super.onCleared()
    speechRecognizer?.destroy()
    speechRecognizer = null
    wakeWordEngine.release()
    ttsManager.shutdown()
  }
}
