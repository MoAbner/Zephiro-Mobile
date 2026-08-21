# F002 - Orchestrator foundation

## Metadata

- Feature ID: F002
- Name: Orchestrator foundation: arbitration, fallback, and confirmation
- Owner: Codex + user
- Status: In Progress
- Started: 2026-08-20

## Goal

Centralize feature control so voice commands do not execute blindly when camera, microphone, recording, OCR, and future assistive flows compete for the same resources.

## Scope

In scope:

- command arbitration before execution
- `stop current` behavior
- confirmation flow for replacing active work
- fallback-first handling when a requested feature is not ready

Out of scope:

- full autonomous task planning
- LLM-based orchestration
- multi-step route planning

## Current Implementation Slice

- a first `FeatureOrchestrator` is being introduced inside `samples/CameraAccess`
- voice commands for `confirmar` and `cancelar` are part of the control surface
- the first guarded transitions target conflicts between recording, preview, and future OCR/object/audio-note flows

## Acceptance Checks

- [ ] conflicting commands are intercepted before execution
- [ ] user can confirm or cancel a replacement by voice
- [ ] `stop current` resolves to the most relevant active capability

## Known Next Steps

- extend arbitration to OCR and object-assist runtimes once those features exist
- add progress beeps or spoken progress for longer tasks
- expose orchestrator state more explicitly in the debug UI if needed
