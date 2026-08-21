package com.meta.wearable.dat.externalsampleapps.cameraaccess.detection

data class DetectedObject(
    val objectClass: ControlledObjectClass,
    val confidence: Float,
    val box: DetectionBox,
    val proximity: ProximityLevel,
)
