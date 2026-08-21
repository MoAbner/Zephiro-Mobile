package com.meta.wearable.dat.externalsampleapps.cameraaccess.voice

sealed interface VoiceCommand {
  data object StartSession : VoiceCommand

  data object EndSession : VoiceCommand

  data object StartPreview : VoiceCommand

  data object StopPreview : VoiceCommand

  data object CapturePhoto : VoiceCommand

  data object StartRecording : VoiceCommand

  data object StopRecording : VoiceCommand

  data object TurnMicOn : VoiceCommand

  data object TurnMicOff : VoiceCommand

  data object Status : VoiceCommand

  data object ReadDocument : VoiceCommand

  data object StartObjectAssist : VoiceCommand

  data object StartAudioNotes : VoiceCommand

  data object StopCurrent : VoiceCommand

  data object ConfirmAction : VoiceCommand

  data object CancelAction : VoiceCommand

  data class Unknown(val transcript: String) : VoiceCommand
}
