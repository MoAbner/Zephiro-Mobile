package com.meta.wearable.dat.externalsampleapps.cameraaccess.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale
import java.util.UUID

class TtsManager(context: Context) : TextToSpeech.OnInitListener {
  private val appContext = context.applicationContext
  private var textToSpeech: TextToSpeech? = TextToSpeech(appContext, this)
  private var isReady = false

  override fun onInit(status: Int) {
    val tts = textToSpeech ?: return
    if (status == TextToSpeech.SUCCESS) {
      tts.language = Locale("pt", "BR")
      isReady = true
    }
  }

  fun speak(text: String, flush: Boolean = true) {
    val tts = textToSpeech ?: return
    if (!isReady) return
    val queueMode = if (flush) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
    tts.speak(text, queueMode, null, UUID.randomUUID().toString())
  }

  fun stop() {
    textToSpeech?.stop()
  }

  fun shutdown() {
    textToSpeech?.stop()
    textToSpeech?.shutdown()
    textToSpeech = null
    isReady = false
  }
}
