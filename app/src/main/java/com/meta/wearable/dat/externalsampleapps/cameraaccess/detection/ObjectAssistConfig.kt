package com.meta.wearable.dat.externalsampleapps.cameraaccess.detection

data class ObjectAssistConfig(
    val mode: ObjectAssistMode = ObjectAssistMode.CONTROLLED_ROOM,
    val announceVisualDebugOnly: Boolean = false,
)
