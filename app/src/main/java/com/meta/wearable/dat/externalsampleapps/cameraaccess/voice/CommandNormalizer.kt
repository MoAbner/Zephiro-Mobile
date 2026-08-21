package com.meta.wearable.dat.externalsampleapps.cameraaccess.voice

import java.text.Normalizer
import java.util.Locale

object CommandNormalizer {
  fun parse(transcript: String): VoiceCommand {
    val normalized = normalize(transcript)

    return when {
      normalized.contains("iniciar sessao") ||
          normalized.contains("comecar sessao") ||
          normalized.contains("abrir sessao") -> VoiceCommand.StartSession
      normalized.contains("encerrar sessao") ||
          normalized.contains("finalizar sessao") ||
          normalized.contains("parar sessao") -> VoiceCommand.EndSession
      normalized.contains("iniciar preview") ||
          normalized.contains("iniciar camera") ||
          normalized.contains("abrir camera") ||
          normalized.contains("comecar preview") -> VoiceCommand.StartPreview
      normalized.contains("parar preview") ||
          normalized.contains("fechar camera") ||
          normalized.contains("parar camera") -> VoiceCommand.StopPreview
      normalized.contains("tirar foto") ||
          normalized.contains("capturar foto") ||
          normalized.contains("bater foto") -> VoiceCommand.CapturePhoto
      normalized.contains("iniciar gravacao") ||
          normalized.contains("comecar gravacao") ||
          normalized.contains("gravar video") -> VoiceCommand.StartRecording
      normalized.contains("parar gravacao") ||
          normalized.contains("encerrar gravacao") ||
          normalized.contains("finalizar gravacao") -> VoiceCommand.StopRecording
      normalized.contains("ligar microfone") ||
          normalized.contains("ativar microfone") -> VoiceCommand.TurnMicOn
      normalized.contains("desligar microfone") ||
          normalized.contains("mutar microfone") -> VoiceCommand.TurnMicOff
      normalized == "status" ||
          normalized.contains("qual status") ||
          normalized.contains("como esta o status") -> VoiceCommand.Status
      normalized.contains("ler documento") ||
          normalized.contains("leia documento") ||
          normalized.contains("fazer ocr") -> VoiceCommand.ReadDocument
      normalized.contains("detectar objetos") ||
          normalized.contains("iniciar deteccao") ||
          normalized.contains("descrever ambiente") -> VoiceCommand.StartObjectAssist
      normalized.contains("gravar aula") ||
          normalized.contains("iniciar gravacao de aula") ||
          normalized.contains("anotar audio") -> VoiceCommand.StartAudioNotes
      normalized == "confirmar" ||
          normalized.contains("pode trocar") ||
          normalized.contains("confirmar troca") -> VoiceCommand.ConfirmAction
      normalized == "cancelar troca" ||
          normalized == "nao confirmar" -> VoiceCommand.CancelAction
      normalized == "parar" ||
          normalized == "cancelar" ||
          normalized.contains("parar atual") -> VoiceCommand.StopCurrent
      else -> VoiceCommand.Unknown(transcript)
    }
  }

  private fun normalize(input: String): String {
    val noAccents =
        Normalizer.normalize(input, Normalizer.Form.NFD)
            .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
    return noAccents.lowercase(Locale.ROOT).trim()
  }
}
