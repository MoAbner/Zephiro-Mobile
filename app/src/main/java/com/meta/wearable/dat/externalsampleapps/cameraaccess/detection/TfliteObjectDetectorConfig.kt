package com.meta.wearable.dat.externalsampleapps.cameraaccess.detection

object TfliteObjectDetectorConfig {
  const val modelAssetPath: String = "models/object_detection/object_detector.tflite"
  const val scoreThreshold: Float = 0.45f
  const val maxResults: Int = 5
}
