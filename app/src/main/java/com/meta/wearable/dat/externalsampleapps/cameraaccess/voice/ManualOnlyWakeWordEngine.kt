package com.meta.wearable.dat.externalsampleapps.cameraaccess.voice

class ManualOnlyWakeWordEngine : WakeWordEngine {
  override val isConfigured: Boolean = false

  override fun start(): Boolean = false

  override fun stop() = Unit

  override fun release() = Unit
}
