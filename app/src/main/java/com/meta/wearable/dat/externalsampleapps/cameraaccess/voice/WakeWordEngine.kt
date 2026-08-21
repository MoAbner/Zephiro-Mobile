package com.meta.wearable.dat.externalsampleapps.cameraaccess.voice

interface WakeWordEngine {
  val isConfigured: Boolean

  fun start(): Boolean

  fun stop()

  fun release()
}
