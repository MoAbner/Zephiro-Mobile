package com.meta.wearable.dat.externalsampleapps.cameraaccess.orchestrator

import com.meta.wearable.dat.externalsampleapps.cameraaccess.voice.VoiceCommand

/**
 * Rule-based feature agent. It decides transitions before UI code touches camera or microphone
 * owners, keeping compatible activities together and asking before an exclusive replacement.
 */
class FeatureOrchestrator {
  private var pendingTransition: PendingTransition? = null

  fun handle(command: VoiceCommand, snapshot: FeatureSnapshot): OrchestratorDecision =
      when (command) {
        VoiceCommand.ConfirmAction -> confirmPending()
        VoiceCommand.CancelAction -> cancelPending()
        VoiceCommand.StopCurrent -> {
          pendingTransition = null
          stopCurrent(snapshot)
        }
        VoiceCommand.StopPreview -> {
          pendingTransition = null
          stopPreview(snapshot)
        }
        VoiceCommand.EndSession -> {
          pendingTransition = null
          endSession(snapshot)
        }
        VoiceCommand.StartObjectAssist -> startObjectAssist(snapshot)
        VoiceCommand.StartRecording -> startRecording(snapshot)
        VoiceCommand.ReadDocument -> startDocumentReading(snapshot)
        VoiceCommand.StartAudioNotes -> startAudioNotes(snapshot)
        else -> {
          pendingTransition = null
          OrchestratorDecision.Execute(listOf(command))
        }
      }

  private fun startObjectAssist(snapshot: FeatureSnapshot): OrchestratorDecision {
    if (snapshot.isObjectAssistActive) {
      return OrchestratorDecision.Speak("A assistencia por objetos ja esta ativa.")
    }
    if (!snapshot.isSessionActive) {
      return OrchestratorDecision.Speak("Inicie uma sessao antes de ativar a assistencia por objetos.")
    }
    if (snapshot.isRecording) {
      return requestReplacement(
          requestedCommand = VoiceCommand.StartObjectAssist,
          currentActivity = "uma gravacao",
          commandsToStop = listOf(VoiceCommand.StopRecording),
      )
    }

    // Object assistance is compatible with preview and depends on it for camera frames.
    val commands =
        buildList {
          if (!snapshot.isStreaming) add(VoiceCommand.StartPreview)
          add(VoiceCommand.StartObjectAssist)
        }
    return OrchestratorDecision.Execute(
        commands = commands,
        spokenFeedback =
            if (snapshot.isStreaming) null
            else "Iniciando preview para ativar a assistencia por objetos.",
    )
  }

  private fun startRecording(snapshot: FeatureSnapshot): OrchestratorDecision {
    if (snapshot.isObjectAssistActive) {
      return requestReplacement(
          requestedCommand = VoiceCommand.StartRecording,
          currentActivity = "a assistencia por objetos",
          commandsToStop = listOf(VoiceCommand.StopCurrent),
      )
    }
    return OrchestratorDecision.Execute(listOf(VoiceCommand.StartRecording))
  }

  private fun startDocumentReading(snapshot: FeatureSnapshot): OrchestratorDecision =
      when {
        snapshot.isRecording ->
            requestReplacement(
                requestedCommand = VoiceCommand.ReadDocument,
                currentActivity = "uma gravacao",
                commandsToStop = listOf(VoiceCommand.StopRecording),
            )
        snapshot.isObjectAssistActive ->
            requestReplacement(
                requestedCommand = VoiceCommand.ReadDocument,
                currentActivity = "a assistencia por objetos",
                commandsToStop = listOf(VoiceCommand.StopCurrent),
            )
        else -> OrchestratorDecision.Execute(listOf(VoiceCommand.ReadDocument))
      }

  private fun startAudioNotes(snapshot: FeatureSnapshot): OrchestratorDecision =
      if (snapshot.isRecording) {
        OrchestratorDecision.Speak("Ja existe uma gravacao em andamento.")
      } else {
        OrchestratorDecision.Execute(listOf(VoiceCommand.StartAudioNotes))
      }

  private fun requestReplacement(
      requestedCommand: VoiceCommand,
      currentActivity: String,
      commandsToStop: List<VoiceCommand>,
  ): OrchestratorDecision {
    pendingTransition = PendingTransition(requestedCommand, commandsToStop)
    return OrchestratorDecision.Speak(
        "Ja existe $currentActivity em andamento. Diga confirmar para encerrar a atividade atual e seguir para ${describeCommand(requestedCommand)}, ou diga cancelar.",
    )
  }

  private fun confirmPending(): OrchestratorDecision {
    val transition = pendingTransition ?: return OrchestratorDecision.Speak("Nao ha nenhuma troca pendente.")
    pendingTransition = null
    return OrchestratorDecision.Execute(
        commands = transition.commandsToStop + transition.requestedCommand,
        spokenFeedback = "Confirmado. Encerrando a atividade atual e seguindo.",
    )
  }

  private fun cancelPending(): OrchestratorDecision =
      if (pendingTransition != null) {
        pendingTransition = null
        OrchestratorDecision.Speak("Troca cancelada.")
      } else {
        OrchestratorDecision.Speak("Nao ha nenhuma troca pendente.")
      }

  private fun stopCurrent(snapshot: FeatureSnapshot): OrchestratorDecision =
      when {
        snapshot.isRecording ->
            OrchestratorDecision.Execute(
                commands = listOf(VoiceCommand.StopRecording),
                spokenFeedback = "Encerrando a gravacao atual.",
            )
        snapshot.isObjectAssistActive ->
            OrchestratorDecision.Execute(
                commands = listOf(VoiceCommand.StopCurrent),
                spokenFeedback = "Encerrando a assistencia por objetos.",
            )
        snapshot.isStreaming ->
            OrchestratorDecision.Execute(
                commands = listOf(VoiceCommand.StopPreview),
                spokenFeedback = "Encerrando o preview atual.",
            )
        snapshot.hasSession ->
            OrchestratorDecision.Execute(
                commands = listOf(VoiceCommand.EndSession),
                spokenFeedback = "Encerrando a sessao atual.",
            )
        else -> OrchestratorDecision.Speak("Nenhuma atividade ativa para parar.")
      }

  private fun stopPreview(snapshot: FeatureSnapshot): OrchestratorDecision {
    val commands = buildList {
      if (snapshot.isRecording) add(VoiceCommand.StopRecording)
      if (snapshot.isObjectAssistActive) add(VoiceCommand.StopCurrent)
      if (snapshot.isStreaming) add(VoiceCommand.StopPreview)
    }
    return if (commands.isEmpty()) {
      OrchestratorDecision.Speak("O preview da camera ja esta parado.")
    } else {
      OrchestratorDecision.Execute(commands, "Encerrando atividades que usam o preview da camera.")
    }
  }

  private fun endSession(snapshot: FeatureSnapshot): OrchestratorDecision {
    val commands = buildList {
      if (snapshot.isRecording) add(VoiceCommand.StopRecording)
      if (snapshot.isObjectAssistActive) add(VoiceCommand.StopCurrent)
      if (snapshot.hasSession) add(VoiceCommand.EndSession)
    }
    return if (commands.isEmpty()) {
      OrchestratorDecision.Speak("Nao ha sessao ativa para encerrar.")
    } else {
      OrchestratorDecision.Execute(commands, "Encerrando a sessao e as atividades dependentes.")
    }
  }

  private fun describeCommand(command: VoiceCommand): String =
      when (command) {
        VoiceCommand.ReadDocument -> "leitura de documento"
        VoiceCommand.StartObjectAssist -> "assistencia por objetos"
        VoiceCommand.StartAudioNotes -> "gravacao de aula"
        VoiceCommand.StartRecording -> "gravacao de video"
        else -> "a proxima atividade"
      }

  private data class PendingTransition(
      val requestedCommand: VoiceCommand,
      val commandsToStop: List<VoiceCommand>,
  )
}
