package com.meta.wearable.dat.externalsampleapps.cameraaccess.voice

data class VoiceAssistantUiState(
    val isListening: Boolean = false,
    val isAvailable: Boolean = true,
    val isHandsFreeEnabled: Boolean = false,
    val isWakeWordConfigured: Boolean = false,
    val lastTranscript: String? = null,
    val lastFeedback: String? = null,
)
