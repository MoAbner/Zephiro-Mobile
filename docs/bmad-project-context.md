# Project Context

## Project

Urban accessibility smart-glasses prototype for a hackathon, using Meta Wearables DAT on Android with processing prioritized on the phone.

## Technical Base

- Android app base: `samples/CameraAccess`
- Language: Kotlin
- UI: Jetpack Compose
- Wearable integration: Meta Wearables DAT `0.9.0`
- Development mode: MockDeviceKit and mock user flows first
- Interaction mode: voice-only for end users
- Glasses display: not available for this prototype

## Local Build Environment

- `samples/CameraAccess` builds successfully with Gradle `8.14.1`
- Android Gradle Plugin is `8.11.1`
- Local Android SDK intersection is present:
  - Android platform `36`
  - Build Tools `35.0.0` or newer
  - `platform-tools` installed
- The local machine must use a Java runtime compatible with the Android build:
  - working runtime found: `C:\Users\abner\.jdks\jbr-21.0.11`
  - non-working runtime for this project flow: JDK `25.0.2`

## Local Runbook

For terminal builds in this environment, use:

`$env:JAVA_HOME='C:\Users\abner\.jdks\jbr-21.0.11'; .\gradlew.bat assembleDebug`

This avoids the local JDK `25.0.2` path that previously blocked Gradle before Kotlin compilation.

## Product Constraints

- Voice is the primary interface
- Edge processing is preferred over cloud processing
- Battery and latency are first-order constraints
- The system must degrade gracefully when camera, microphone, or recognition features are unavailable
- Safety-critical claims must be conservative; the prototype should assist, not promise reliable navigation autonomy

## Engineering Rules

- Build on top of the existing DAT sample instead of replacing it
- Prefer modular services over feature logic embedded directly in activities or composables
- Keep long-running camera or microphone work behind lifecycle-aware services or managers
- Use Kotlin coroutines and `StateFlow` / `Flow` for feature state and event propagation
- Introduce feature flags so expensive capabilities can be disabled independently
- Favor deterministic rule-based orchestration before adding LLM-style reasoning
- Add test hooks and reproducible sample inputs wherever possible

## Initial Architecture Direction

- `voice/` for wake word, ASR, and TTS
- `vision/` for object detection and OCR
- `audio/` for recording, chunking, and transcription
- `orchestrator/` for feature arbitration and fallbacks
- `domain/` for use cases and intent handling
- `data/` for storage, transcripts, and metadata

## Delivery Discipline

Each approved feature must produce:

- purpose and scope
- implementation notes
- dependencies and risks
- validation steps
- test evidence
- reusable lessons learned

Those records become the source of truth for future reuse.
