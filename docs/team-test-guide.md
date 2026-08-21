# Team Test Guide

## Goal

Validate the voice-first prototype with MockDeviceKit and a physical phone camera before changing
feature status in the BMAD records.

## One-time setup

1. Open `CameraAccess` in Android Studio.
2. Set Gradle JDK to 21 and sync the project.
3. Create `local.properties` with Android SDK path and `github_token` with GitHub `read:packages`.
4. Run the `app` configuration on an Android 12+ phone or emulator.
5. In the app, open the debug menu, enable MockDeviceKit, pair Ray-Ban Meta, and enable Power,
   Unfolded, and Donned.

## F001: voice foundation

1. Start a device session.
2. Tap `Voice command` and say `status` or `iniciar sessao`.
3. Enable Hands-free.
4. Say one wake word: `alexa`, `hello world`, `hey siri`, or `hi google`.
5. After the activation sound or prompt, say `status`.
6. Disable Hands-free and confirm the application stays open.

Expected result: command recognition and spoken response work in manual and hands-free paths.

## F002: orchestration

1. With an active session, say `descrever ambiente`.
2. While it is active, request another feature that requires confirmation.
3. Say `confirmar` or `cancelar`.
4. Say `parar` and verify that the active feature is stopped.

Expected result: the app asks before replacing an activity and obeys confirmation, cancellation,
and stop commands.

## F004: controlled object assistance

1. Start a device session and tap `Preview`.
2. Point the selected camera feed at a person, chair, bottle, car, motorcycle, bus, truck, or
   bicycle.
3. Say `descrever ambiente`.
4. Wait up to 2 seconds for a spoken alert.
5. Keep the same object visible and confirm it is not repeated continuously; the per-class cooldown
   is 4 seconds.
6. Place a near object and a distant object in frame to observe the default distance-first ranking.

Expected result: one relevant object is announced at a time. The feature is limited to controlled
environments and must not be presented as a navigation guarantee.

## Evidence to record

For each test, record date, phone or emulator model, Android version, camera source, command,
observed spoken result, latency, and Logcat errors. Add the result to the corresponding
`docs/bmad-feature-FXXX-*.md` record.

## Known limits

- Packaged English wake words are the active profile. `Zephiro` is not implemented yet.
- With recording audio enabled, microphone contention can stop recording to restore voice control.
- F004 samples still images every 2 seconds instead of decoding every HEVC preview frame.
