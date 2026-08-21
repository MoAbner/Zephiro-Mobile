package com.meta.wearable.dat.externalsampleapps.cameraaccess.detection

class NoOpObjectDetector(
    val reason: String? = null,
) : ObjectDetector {
  override val isConfigured: Boolean = false

  override suspend fun detect(
      rgbaBytes: ByteArray,
      frameWidth: Int,
      frameHeight: Int,
  ): List<DetectionCandidate> = emptyList()
}
