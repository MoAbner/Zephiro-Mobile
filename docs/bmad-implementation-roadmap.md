# Implementation Roadmap

## Stage 0 - Project Foundation

Goal: prepare the existing `CameraAccess` app to host assistive features without destabilizing DAT session and stream behavior.

Outputs:

- BMAD artifact structure
- project context and roadmap
- feature tracking model

## Stage 1 - Voice Foundation

Goal: make voice the universal entry point for the system.

Deliverables:

- wake word or push-to-talk fallback
- on-device or local-first command recognition
- TTS response manager
- basic command routing

Acceptance:

- user can trigger at least `start object assist`, `read document`, `start recording`, `stop`, `status`

## Stage 2 - Orchestrator Foundation

Goal: centralize feature control and fallback behavior.

Deliverables:

- feature state machine
- conflict handling between camera, mic, OCR, and recording modes
- progress audio prompts for long tasks

Acceptance:

- conflicting requests are detected
- user receives confirmation prompts before replacing active work

Current entry condition:

- F001 validated with manual trigger, TTS, and local sherpa hands-free using packaged keywords
- custom `Zephiro` wake-word assets remain a follow-up inside the voice stack, but they no longer block orchestrator work

## Continuous Track - Battery and Latency

Goal: benchmark each persistent workload before adding the next one.

Required comparisons:

- session baseline
- preview baseline
- preview plus hands-free
- preview plus controlled object assistance
- combined demo configuration

The reproducible procedure is in `docs/battery-performance-test-plan.md`. Results must influence
sampling cadence, input size, stream quality, and feature fallback rules.

## Stage 3 - OCR Slice

Goal: guided document reading with spoken output.

Deliverables:

- capture or sampled frame pipeline for OCR
- framing guidance prompts
- text reading flow

Acceptance:

- printed document can be captured and read aloud in a repeatable demo flow

## Stage 4 - Object Assistance Slice

Goal: narrow, reliable object detection in controlled contexts.

Deliverables:

- lightweight detector integration
- prioritized alert queue
- cooldown and persistence logic

Acceptance:

- system announces a limited class set with conservative thresholds

Current implementation note:

- the object-assistance policy layer can now be built independently from the detector runtime
- if urban scope grows too quickly, keep the MVP locked to room/classroom use cases instead of widening the class set

## Stage 5 - Audio Knowledge Slice

Goal: record content and make it queryable later.

Deliverables:

- titled recordings with timestamps
- transcription pipeline
- local retrieval over transcript chunks
- flashcard and summary generation only from stored content

Acceptance:

- user can ask questions about a specific recording and receive grounded answers only from stored transcript evidence

## Deferred Scope

- full urban scene coverage
- live route learning and playback
- autonomous navigation assistance
- cloud-heavy reasoning in real time
