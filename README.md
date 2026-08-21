# Zefiro Smart Glasses Prototype

Voice-first assistive smart-glasses prototype for controlled environments. The Android app uses
Meta Wearables DAT, MockDeviceKit, local wake-word detection with sherpa-onnx, Android speech
recognition/TTS, and TensorFlow Lite object detection.

This is a hackathon prototype. It assists with awareness of objects; it is not a certified
navigation or safety system.

## Current MVP

| Feature | Status | Test command or action |
| --- | --- | --- |
| F001 voice foundation | Validated | `Voice command`, or hands-free followed by a command |
| Local hands-free | Validated with packaged words | `alexa`, `hello world`, `hey siri`, `hi google` |
| F002 orchestration | In progress | `confirmar`, `cancelar`, `parar` |
| F004 controlled object assistance | In progress and runnable | Start Preview, then say `descrever ambiente` |

The currently bundled wake-word model does not yet recognize `Zephiro`. Custom Zephiro keywords
remain a documented next step in `docs/bmad-plan-wakeword-tflite-future.md`.

## Repository layout

```text
app/                         Android application
app/src/main/assets/         Versioned Sherpa-ONNX and TFLite models
docs/                        BMAD records, decisions, roadmap, and test guide
references/                  Original Python inference reference
LICENSE and NOTICE           Required notices inherited from Meta sample code
```

## Requirements

- Android Studio with Android SDK Platform 36
- JDK 21 for Gradle builds
- Android device running Android 12 or later, or emulator with MockDeviceKit
- GitHub personal access token with `read:packages` to resolve Meta DAT dependencies

Create a local `local.properties` file; do not commit it:

```properties
sdk.dir=C\:\\Android\\Sdk
github_token=YOUR_GITHUB_TOKEN_WITH_READ_PACKAGES
```

For DAT development mode, use MockDeviceKit or enable Developer Mode in the Meta AI app. The
development build uses DAT placeholder credentials (`0`); production credentials are intentionally
not part of this repository.

## Build and run

1. Open this directory (`CameraAccess`) in Android Studio.
2. Set Gradle JDK to JDK 21.
3. Sync Gradle and run the `app` configuration on a phone or emulator.
4. Open the debug menu, enable MockDeviceKit, pair Ray-Ban Meta, then set Power, Unfolded, and
   Donned to on.
5. Start a session, start Preview, and use the voice commands described in
   `docs/team-test-guide.md`.

Terminal build on Windows:

```powershell
$env:JAVA_HOME='C:\\Users\\YOUR_USER\\.jdks\\jbr-21.0.11'
.\gradlew.bat assembleDebug
```

## Models and licenses

Both binary model packages are committed because they are small enough for ordinary Git and are
necessary for reproducible team testing:

- sherpa-onnx wake-word package: Apache-2.0 project license and source documented in
  `app/src/main/assets/README-sherpa-wakeword.md`.
- EfficientDet Lite0 TFLite model: source and attribution documented in
  `app/src/main/assets/models/object_detection/README.md`.
- Consolidated third-party notices: `THIRD_PARTY_NOTICES.md`.

## Documentation

- `docs/team-test-guide.md`: exact team test runbook
- `docs/bmad-project-readme.md`: BMAD artifact index
- `docs/bmad-backlog.md`: feature status
- `docs/bmad-decision-log.md`: durable architectural decisions
- `docs/bmad-feature-F001-voice-foundation.md`: voice validation record
- `docs/bmad-feature-F004-object-assistance.md`: detector scope and validation record

## Git hygiene

Do not commit `local.properties`, signing keys, APK/AAB files, Android Studio folders, builds, or
tokens. Debug builds use the standard Android debug key generated on each developer machine.
