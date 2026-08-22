package com.meta.wearable.dat.externalsampleapps.cameraaccess.orchestrator

import com.meta.wearable.dat.externalsampleapps.cameraaccess.voice.VoiceCommand
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FeatureOrchestratorTest {
  @Test
  fun `object assistance keeps an active preview`() {
    val decision =
        FeatureOrchestrator().handle(
            VoiceCommand.StartObjectAssist,
            snapshot(isSessionActive = true, isStreaming = true),
        )

    assertEquals(
        listOf(VoiceCommand.StartObjectAssist),
        (decision as OrchestratorDecision.Execute).commands,
    )
  }

  @Test
  fun `object assistance starts preview when the session is active`() {
    val decision =
        FeatureOrchestrator().handle(
            VoiceCommand.StartObjectAssist,
            snapshot(isSessionActive = true),
        )

    assertEquals(
        listOf(VoiceCommand.StartPreview, VoiceCommand.StartObjectAssist),
        (decision as OrchestratorDecision.Execute).commands,
    )
  }

  @Test
  fun `object assistance asks before replacing recording`() {
    val orchestrator = FeatureOrchestrator()
    val requested =
        orchestrator.handle(
            VoiceCommand.StartObjectAssist,
            snapshot(isSessionActive = true, isStreaming = true, isRecording = true),
        )

    assertTrue(requested is OrchestratorDecision.Speak)

    val confirmed = orchestrator.handle(VoiceCommand.ConfirmAction, snapshot())
    assertEquals(
        listOf(VoiceCommand.StopRecording, VoiceCommand.StartObjectAssist),
        (confirmed as OrchestratorDecision.Execute).commands,
    )
  }

  @Test
  fun `recording asks before replacing object assistance`() {
    val orchestrator = FeatureOrchestrator()
    val requested =
        orchestrator.handle(
            VoiceCommand.StartRecording,
            snapshot(isSessionActive = true, isStreaming = true, isObjectAssistActive = true),
        )

    assertTrue(requested is OrchestratorDecision.Speak)

    val cancelled = orchestrator.handle(VoiceCommand.CancelAction, snapshot())
    assertEquals("Troca cancelada.", (cancelled as OrchestratorDecision.Speak).message)
  }

  @Test
  fun `stopping preview stops dependent object assistance first`() {
    val decision =
        FeatureOrchestrator().handle(
            VoiceCommand.StopPreview,
            snapshot(isSessionActive = true, isStreaming = true, isObjectAssistActive = true),
        )

    assertEquals(
        listOf(VoiceCommand.StopCurrent, VoiceCommand.StopPreview),
        (decision as OrchestratorDecision.Execute).commands,
    )
  }

  private fun snapshot(
      isSessionActive: Boolean = false,
      isStreaming: Boolean = false,
      isRecording: Boolean = false,
      isObjectAssistActive: Boolean = false,
  ) =
      FeatureSnapshot(
          hasSession = isSessionActive,
          isSessionActive = isSessionActive,
          isStreaming = isStreaming,
          isRecording = isRecording,
          isObjectAssistActive = isObjectAssistActive,
      )
}
