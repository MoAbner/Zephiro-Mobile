package com.meta.wearable.dat.externalsampleapps.cameraaccess.detection

interface ObjectDetector {
  val isConfigured: Boolean

  suspend fun detect(
      rgbaBytes: ByteArray,
      frameWidth: Int,
      frameHeight: Int,
  ): List<DetectionCandidate>
}
