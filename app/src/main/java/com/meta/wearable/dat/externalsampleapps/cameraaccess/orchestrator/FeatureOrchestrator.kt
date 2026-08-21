package com.meta.wearable.dat.externalsampleapps.cameraaccess.orchestrator

import com.meta.wearable.dat.externalsampleapps.cameraaccess.voice.VoiceCommand

class FeatureOrchestrator {
  private var pendingCommand: VoiceCommand? = null

  fun handle(command: VoiceCommand, snapshot: FeatureSnapshot): OrchestratorDecision =
      when (command) {
        VoiceCommand.ConfirmAction -> confirmPending(snapshot)
        VoiceCommand.CancelAction -> cancelPending()
        VoiceCommand.StopCurrent -> stopCurrent(snapshot)
        VoiceCommand.ReadDocument,
        VoiceCommand.StartObjectAssist,
        VoiceCommand.StartAudioNotes -> guardFeatureSwitch(command, snapshot)
        else -> {
          pendingCommand = null
          OrchestratorDecision.Execute(listOf(command))
        }
      }

  private fun guardFeatureSwitch(
      command: VoiceCommand,
      snapshot: FeatureSnapshot,
  ): OrchestratorDecision {
    val currentActivity =
        when {
          snapshot.isRecording -> "uma gravacao"
          snapshot.isObjectAssistActive -> "a assistencia por objetos"
          snapshot.isStreaming -> "o preview da camera"
          else -> null
        }

    if (currentActivity == null) {
      pendingCommand = null
      return OrchestratorDecision.Execute(listOf(command))
    }

    pendingCommand = command
    val requested = describeCommand(command)
    return OrchestratorDecision.Speak(
        "Ja existe $currentActivity em andamento. Diga confirmar para encerrar a atividade atual e seguir para $requested, ou diga cancelar.",
    )
  }

  private fun confirmPending(snapshot: FeatureSnapshot): OrchestratorDecision {
    val command = pendingCommand
    if (command == null) {
      return OrchestratorDecision.Speak("Nao ha nenhuma troca pendente.")
    }

    pendingCommand = null
    val commands =
        buildList {
          when {
            snapshot.isRecording -> add(VoiceCommand.StopRecording)
            snapshot.isObjectAssistActive -> add(VoiceCommand.StopCurrent)
            snapshot.isStreaming -> add(VoiceCommand.StopPreview)
            snapshot.hasSession -> Unit
          }
          add(command)
        }
    return OrchestratorDecision.Execute(
        commands = commands,
        spokenFeedback = "Confirmado. Encerrando a atividade atual e seguindo.",
    )
  }

  private fun cancelPending(): OrchestratorDecision {
    return if (pendingCommand != null) {
      pendingCommand = null
      OrchestratorDecision.Speak("Troca cancelada.")
    } else {
      OrchestratorDecision.Speak("Nao ha nenhuma troca pendente.")
    }
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

  private fun describeCommand(command: VoiceCommand): String =
      when (command) {
        VoiceCommand.ReadDocument -> "leitura de documento"
        VoiceCommand.StartObjectAssist -> "assistencia por objetos"
        VoiceCommand.StartAudioNotes -> "gravacao de aula"
        else -> "a proxima atividade"
      }
}
