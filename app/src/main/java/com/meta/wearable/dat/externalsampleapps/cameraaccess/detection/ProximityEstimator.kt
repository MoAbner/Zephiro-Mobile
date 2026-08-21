package com.meta.wearable.dat.externalsampleapps.cameraaccess.detection

class ProximityEstimator(
    private val veryCloseRatio: Float = 0.25f,
    private val approachingRatio: Float = 0.08f,
) {
  fun estimate(box: DetectionBox, frameWidth: Int, frameHeight: Int): ProximityLevel {
    val frameArea = (frameWidth * frameHeight).toFloat().coerceAtLeast(1f)
    val ratio = box.area() / frameArea
    return when {
      ratio > veryCloseRatio -> ProximityLevel.VERY_CLOSE
      ratio > approachingRatio -> ProximityLevel.APPROACHING
      else -> ProximityLevel.FAR
    }
  }
}
