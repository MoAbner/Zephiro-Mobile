# F001 - Voice Foundation

## Metadata

- Feature ID: F001
- Name: Voice foundation: wake word, command intake, and TTS
- Owner: Codex + user
- Status: Validated
- Started: 2026-08-19
- Validated: 2026-08-20

## Goal

Create the shared voice entry point for the whole prototype so every assistive function can be triggered and controlled without relying on the glasses display.

## Scope

In scope:

- wake word strategy or explicit fallback path
- command capture for short intents
- TTS response manager
- command routing to the orchestrator
- basic commands: `start object assist`, `read document`, `start recording`, `stop`, `status`

Out of scope:

- open-domain conversation
- long-form dictation
- cloud-only continuous speech understanding

## Dependencies

- DAT app base in `samples/CameraAccess`
- microphone permission flow
- future orchestrator integration

## Technical Design

### Entry points

- voice trigger service
- developer test UI buttons

### Main classes or modules

- `voice/wakeword/WakeWordEngine`
- `voice/asr/CommandRecognitionEngine`
- `voice/tts/TtsManager`
- `orchestrator/IntentRouter`

### Data flow

Microphone input -> wake word or manual trigger -> short ASR command -> normalized intent -> orchestrator -> TTS confirmation or action result

## Acceptance Checks

- [x] user can trigger the system without using the screen for the main demo flow
- [x] at least five core commands are recognized and routed
- [x] spoken responses can interrupt, queue, or replace earlier responses deterministically for the current single-command voice flow
- [x] fallback path exists if on-device wake word is unavailable

## Implementation Notes

Current implementation slice in `samples/CameraAccess`:

- one-shot voice command button added to the camera screen
- command parser for Portuguese command phrases
- `TextToSpeech` manager for spoken feedback
- `SpeechRecognizer` wrapper with on-device preference when available
- command routing to existing camera actions: session, preview, photo, recording, microphone, status
- vendor-neutral wake-word abstraction added with manual fallback preserved
- `sherpa-onnx` wake-word engine integrated and validated with the packaged keyword set
- configurable wake-word profile introduced so the validated packaged runtime can later be swapped to a custom `Zephiro` profile without restructuring the voice flow

## Test Plan

### Functional checks

- confirm each supported command is recognized
- confirm TTS feedback after each recognized command

### Failure or fallback checks

- no mic permission
- recognizer unavailable
- user speaks while another feature is active

### Performance checks

- command latency target under 2 seconds after trigger
- no runaway CPU usage when idle

## Test Evidence

- Device or emulator: Android emulator with MockDeviceKit enabled
- Build: `assembleDebug` succeeds when run with `JAVA_HOME=C:\Users\abner\.jdks\jbr-21.0.11`
- Inputs:
  - manual voice button plus commands such as `iniciar sessao`
  - hands-free wake word using packaged sherpa keywords such as `alexa` and `hello world`
- Observed result:
  - manual command recognition and TTS feedback worked coherently after the mock device was paired and set to `Power`, `Unfolded`, and `Donned`
  - hands-free wake word successfully transitioned into command listening, accepted a spoken command, and returned to the idle wake-word cycle in favorable tests
  - hands-free off no longer crashes the app after the sherpa stream lifecycle and decoder teardown fixes
- Logs or notes:
  - DAT session startup still fails with an eligibility error until the mock device is in an active-wearable state, so UX guidance was prioritized
  - stream teardown may emit benign capability-removal logs because the preview stream is intentionally stopped during certain transitions

## Known Limits

- the currently validated wake word uses packaged English sherpa keywords, not the final `Zephiro/Zefiro` set
- commands for OCR, object assistance, and lecture recording are recognized but still return not-implemented feedback
- broader runtime validation for noisy environments is still pending
- there is still no multi-feature arbitration layer yet; voice currently routes into the existing command handlers directly

## Reusable Knowledge

- the existing `CameraAccess` sample can host voice control without changing DAT session ownership
- the safest first slice is manual voice command plus TTS, before always-listening wake word
- hybrid wake-word plus push-to-talk is the correct compromise for battery and reliability in this prototype
- `sherpa-onnx` can be integrated safely if stream ownership and native resource teardown are treated conservatively
- the right path for custom branding is a profile-based wake-word layer plus a future compatible `keywords.txt`, not hardcoded trigger phrases in the ViewModel

## Follow-up Work

- switch from packaged keywords to a custom `Zephiro/Zefiro` sherpa keyword package
- connect to F002 orchestrator state machine
- add progress prompt hooks
