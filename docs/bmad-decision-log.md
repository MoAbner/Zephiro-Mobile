# Decision Log

## D001 - Use `samples/CameraAccess` as the product base

- Date: 2026-08-19
- Status: Accepted
- Reason: the sample already contains DAT initialization, permissions, camera streaming, audio capture, foreground service handling, and mock-device support
- Consequence: new features should extend the sample architecture rather than replace it

## D002 - Voice-first interaction is mandatory

- Date: 2026-08-19
- Status: Accepted
- Reason: glasses display is unavailable for the prototype
- Consequence: all feature entry points and system feedback need audio paths; UI remains mainly for developer testing

## D003 - Start with rule-based orchestration, not LLM orchestration

- Date: 2026-08-19
- Status: Accepted
- Reason: deterministic behavior is easier to test, lighter on battery, and more predictable for conflict handling
- Consequence: the first agent implementation will be a state machine plus routing rules; generative AI can be added later for summarization or post-processing

## D004 - Controlled environment detection before broad urban detection

- Date: 2026-08-19
- Status: Accepted
- Reason: broad urban coverage is too risky and too large for the first delivery slice
- Consequence: detector class list and demo scenarios must be intentionally constrained

## D005 - Edge-first processing with explicit fallbacks

- Date: 2026-08-19
- Status: Accepted
- Reason: latency, privacy, and battery-aware design are core project constraints
- Consequence: prefer on-device ASR, OCR, and lightweight vision; any cloud fallback must be explicit and optional

## D006 - Picovoice is not the long-term wake-word base

- Date: 2026-08-19
- Status: Accepted
- Reason: the free-trial model is not a stable foundation for the prototype beyond short evaluation
- Consequence: remove Picovoice coupling from the app and keep wake-word architecture vendor-neutral

## D007 - Sherpa-onnx is the primary wake-word target, with manual fallback preserved

- Date: 2026-08-19
- Status: Accepted
- Reason: it aligns better with offline continuity and avoids trial lock-in
- Consequence: the current app should target a `WakeWordEngine` abstraction and preserve the manual trigger path permanently

## D008 - TFLite wake-word model is a deferred experimental path

- Date: 2026-08-19
- Status: Accepted
- Reason: a custom model may become the best long-term fit, but training and runtime validation should not block current product delivery
- Consequence: keep a documented future plan for `TfliteWakeWordEngine` while progressing with the main voice architecture now

## D009 - F001 was partially validated before full hands-free completion

- Date: 2026-08-19
- Status: Accepted
- Reason: the manual command path already demonstrated command recognition, DAT action routing, and TTS feedback, which is enough to lock the voice-foundation slice while wake word remains in progress
- Consequence: downstream features can build on the current voice interface now, while sherpa-onnx wake word integration continues as the remaining gap inside F001

## D010 - Close F001 with packaged sherpa keywords before custom Zephiro branding

- Date: 2026-08-20
- Status: Accepted
- Reason: the packaged sherpa keyword set already proves the local hands-free architecture, while `Zephiro/Zefiro` still requires a compatible tokenized `keywords.txt`
- Consequence: F001 can be treated as validated, and the next engineering slice should focus on orchestration plus a clean migration path to custom wake-word assets rather than blocking on branding immediately

## D011 - Keep object detection MVP constrained to controlled environments

- Date: 2026-08-20
- Status: Accepted
- Reason: the reference prototype is useful, but letting the scope drift back to a full urban detector would expand class coverage, testing burden, and false-positive risk too quickly
- Consequence: F004 should first target controlled room/classroom flows, while broad urban object assistance remains deferred in F008
