# Kotlin Coroutines and Flow Review

Date: 2026-08-20

## Purpose

Review how the current `samples/CameraAccess` codebase uses Kotlin concurrency tools for the hackathon project, and identify where it already follows Kotlin-first patterns versus where it still uses lower-level threading primitives.

This document is based on the current project code. It is not a transcription of the attached hackathon support PDF.

## Executive Summary

The app already uses Kotlin coroutines and `Flow` in important parts of the architecture:

- `viewModelScope`
- `MutableStateFlow` / `StateFlow`
- `MutableSharedFlow` / `SharedFlow`
- `collect`
- `withContext`
- explicit dispatchers for heavier work

So the project is **not** written in a Java-style fully imperative way. The main state architecture is already aligned with Kotlin Android best practices.

However, some lower-level subsystems still rely on:

- raw `Thread`
- `Handler(Looper.getMainLooper())`
- callback-style APIs
- manual start/stop synchronization with `@Volatile` and locks

That is acceptable for a fast prototype, especially around audio and native SDK interop, but there is room to make the code more idiomatic and safer with structured concurrency.

## Where Coroutines and Flow Are Already Used Well

### ViewModel state

The following areas already use `StateFlow` correctly for screen state:

- `wearables/WearablesViewModel.kt`
- `mockdevicekit/MockDeviceKitViewModel.kt`
- `camera/CameraViewModel.kt`
- `voice/VoiceAssistantViewModel.kt`
- `detection/ObjectAssistViewModel.kt`

This is the main advantage of Kotlin in the current app. UI state is exposed reactively instead of being pushed imperatively into the screen.

### Scoped ownership

`viewModelScope` is already used in the right places:

- camera lifecycle observers
- stream collectors
- voice command emission
- wearables state observation
- mock-device flows

This is good because every coroutine has a clear owner and is cancelled with the `ViewModel`.

### Dispatcher usage

There is already correct dispatcher intent in some heavy paths:

- `CameraViewModel.frameDispatcher = Dispatchers.Default.limitedParallelism(1)`
- `withContext(Dispatchers.Default)` for bitmap decode
- `withContext(Dispatchers.IO)` in `VideoRecorder`

This is a strong sign that the code already understands the difference between UI work and CPU/I/O work.

## Why `async` Is Rare in This Codebase

`async` is useful when:

- you have two or more independent tasks
- they can run in parallel
- you need both results before continuing

Today, most operations here are event-driven and sequential:

- wait for a voice command
- react to one stream frame
- react to one DAT session state change
- react to one stream state change

So `launch` is currently more appropriate than `async` in most places.

The absence of `async` is not, by itself, a problem.

Good future candidates for `async`:

- object detection + OCR running from the same captured frame for comparison
- preprocessing and metadata lookup in parallel
- audio transcript indexing plus summary generation in separate background tasks

## Main Kotlin/Coroutine Improvement Opportunities

### 1. Replace raw `Thread` in wake word capture

Current file:

- `voice/SherpaWakeWordEngine.kt`

Current behavior:

- starts a manual background thread
- manages `Thread`, `interrupt()`, `join()`, locks, and `@Volatile`

Why improve:

- this works, but it bypasses structured concurrency
- cancellation is more manual than necessary
- error propagation is harder to reason about

Recommended future shape:

- inject a `CoroutineScope`
- run the audio loop with `scope.launch(Dispatchers.Default or IO)`
- replace `Thread` ownership with `Job`
- keep native resource cleanup in `try/finally`

Priority: high

Reason:

This is one of the most concurrency-sensitive pieces in the project.

### 2. Replace raw `Thread` in `AudioInputHandler`

Current file:

- `stream/AudioInputHandler.kt`

Current behavior:

- manual thread for microphone PCM loop
- `Handler` used to update state on main thread

Why improve:

- the class already exposes `StateFlow`, so its internal loop can also become coroutine-based
- it would simplify interruption and state publication

Recommended future shape:

- internal `CoroutineScope` or injected scope
- `Job` for active recording loop
- `withContext(Dispatchers.IO)` or `launch(Dispatchers.IO)` for PCM read loop
- use `MutableStateFlow.update` directly instead of main-thread `Handler` where safe

Priority: high

### 3. Wrap callback APIs into suspending or Flow adapters

Current file:

- `voice/VoiceAssistantViewModel.kt`

Current behavior:

- `SpeechRecognizer` is still handled through Android callbacks

Why improve:

- callback code works, but it spreads lifecycle handling across several methods
- a `callbackFlow` or suspend wrapper would make the command intake more composable

Recommended future shape:

- `callbackFlow<String>` for recognized transcripts
- emit parsed `VoiceCommand`
- collect that flow in the `ViewModel`

Priority: medium

### 4. Move more orchestration out of Compose screen code

Current file:

- `ui/CameraScreen.kt`

Current behavior:

- `LaunchedEffect(Unit)` collects commands
- screen contains significant routing/orchestration logic

Why improve:

- screen works now, but it is getting dense
- feature routing belongs more naturally to a coordinator `ViewModel`

Recommended future shape:

- keep Compose responsible for rendering and user gestures
- move command execution/orchestration to a dedicated coordinator or `ViewModel`

Priority: medium

### 5. Use Flow more explicitly for detector runtime state

Current file:

- `detection/ObjectAssistViewModel.kt`

Current behavior:

- feature mode state is already reactive
- actual detector loop is not yet connected to frame stream

Recommended future shape:

- frame source -> `Flow<FramePacket>`
- detector pipeline -> `Flow<ObjectAlert>`
- throttling and cooldown remain in policy layer

Priority: medium

### 6. Clean small Java-legacy details

Current files:

- `voice/VoiceAssistantViewModel.kt`
- `voice/TtsManager.kt`

Current behavior:

- deprecated `Locale("pt", "BR")` constructor warning

Recommended future shape:

- centralize locale as `Locale.forLanguageTag("pt-BR")`

Priority: low

## Suggested Refactor Order

Do not refactor everything at once. The safer order is:

1. Convert `SherpaWakeWordEngine` from `Thread` to coroutine `Job`
2. Convert `AudioInputHandler` from `Thread` to coroutine `Job`
3. Wrap `SpeechRecognizer` into `callbackFlow`
4. Reduce orchestration logic inside `CameraScreen`
5. Connect detector frame pipeline as `Flow`

## What Should Stay As-Is For Now

These current choices are reasonable for the hackathon phase:

- `StateFlow` for UI state
- `SharedFlow` for one-shot voice commands
- `viewModelScope`
- `withContext(Dispatchers.IO)` in recording paths
- `Dispatchers.Default.limitedParallelism(1)` for serialized frame work

## Practical Guidance For The Team

If the goal is to improve Kotlin quality without destabilizing the prototype, do this:

- keep current state architecture
- avoid adding more raw threads
- any new background loop should use coroutine `Job`
- any new event stream should prefer `Flow`
- use `async` only for truly independent parallel tasks, not as a default replacement for `launch`

## Bottom Line

The project already uses Kotlin coroutines and `Flow` in meaningful ways. The main gap is not "lack of coroutines"; the main gap is that the most low-level audio/wake-word subsystems still use manual thread management.

So the right conclusion is:

- Kotlin advantages are already partially present
- the codebase is not ignoring `Flow` and `viewModelScope`
- the best next improvement is to convert raw audio/wake-word loops into structured coroutine jobs
