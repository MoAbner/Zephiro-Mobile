package com.meta.wearable.dat.externalsampleapps.cameraaccess.voice

data class WakeWordProfile(
    val id: String,
    val displayName: String,
    val examples: List<String>,
    val activationPrompt: String,
    val notReadyReason: String? = null,
)
