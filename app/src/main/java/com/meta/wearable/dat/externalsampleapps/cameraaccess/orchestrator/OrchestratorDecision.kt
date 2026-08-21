package com.meta.wearable.dat.externalsampleapps.cameraaccess.orchestrator

import com.meta.wearable.dat.externalsampleapps.cameraaccess.voice.VoiceCommand

sealed interface OrchestratorDecision {
  data class Execute(
      val commands: List<VoiceCommand>,
      val spokenFeedback: String? = null,
  ) : OrchestratorDecision

  data class Speak(val message: String) : OrchestratorDecision
}
