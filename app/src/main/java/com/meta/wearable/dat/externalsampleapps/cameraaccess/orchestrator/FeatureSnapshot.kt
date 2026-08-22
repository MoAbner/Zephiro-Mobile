package com.meta.wearable.dat.externalsampleapps.cameraaccess.orchestrator

data class FeatureSnapshot(
    val hasSession: Boolean,
    val isSessionActive: Boolean,
    val isStreaming: Boolean,
    val isRecording: Boolean,
    val isObjectAssistActive: Boolean,
)
