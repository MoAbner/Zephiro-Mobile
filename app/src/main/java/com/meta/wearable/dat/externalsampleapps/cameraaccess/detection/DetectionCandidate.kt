package com.meta.wearable.dat.externalsampleapps.cameraaccess.detection

data class DetectionCandidate(
    val label: String,
    val confidence: Float,
    val box: DetectionBox,
)
