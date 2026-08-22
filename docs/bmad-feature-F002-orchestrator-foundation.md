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

- `FeatureOrchestrator` is the deterministic transition agent for voice requests
- voice commands for `confirmar`, `cancelar`, and `parar` are part of the control surface
- preview and object assistance are explicitly compatible: the agent starts preview when needed and
  then enables F004 without asking to replace it
- recording and object assistance are exclusive in this MVP: the agent asks confirmation before
  replacing one with the other
- stopping preview or ending the session first stops dependent recording/object-assistance work
- document reading is prepared as a future exclusive replacement of recording or object assistance

## Compatibility Rules

| Requested activity | Existing activity | Agent decision |
| --- | --- | --- |
| Object assistance | Session, no preview | Start preview, then assistance |
| Object assistance | Preview | Keep preview and start assistance |
| Object assistance | Recording | Ask confirmation, stop recording if confirmed |
| Video recording | Object assistance | Ask confirmation, stop assistance if confirmed |
| Document reading | Object assistance or recording | Ask confirmation, stop conflicting activity if confirmed |
| Stop preview | Object assistance or recording | Stop dependent activity, then preview |
| End session | Object assistance or recording | Stop dependent activity, then session |

## Acceptance Checks

- [x] conflicting commands are intercepted before execution
- [x] user can confirm or cancel a replacement by voice
- [x] `stop current` resolves to the most relevant active capability
- [ ] verify the compatibility matrix on a physical device and record results

## Known Next Steps

- connect the future OCR and audio-note implementations to the already defined transition rules
- add progress beeps or spoken progress for longer tasks
- move UI command execution from `CameraScreen` into a dedicated orchestration ViewModel once the
  next feature adds enough actions to justify that extra layer
