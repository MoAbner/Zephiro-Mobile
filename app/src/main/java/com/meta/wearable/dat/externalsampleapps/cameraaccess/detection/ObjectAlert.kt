package com.meta.wearable.dat.externalsampleapps.cameraaccess.detection

data class ObjectAlert(
    val objectClass: ControlledObjectClass,
    val phrase: String,
    val confidence: Float,
    val proximity: ProximityLevel,
)
