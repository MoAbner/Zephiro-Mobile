# Backlog

## Active Delivery Order

| ID | Feature | Priority | Status | Notes |
|---|---:|---:|---|---|
| F001 | Voice foundation: wake word, command intake, TTS | 1 | Validated | Manual + hands-free packaged sherpa flow validated; custom Zephiro keywords remain follow-up tuning |
| F002 | Orchestrator: feature arbitration and fallbacks | 2 | In Progress | Compatibility matrix implemented; physical-device validation remains |
| F003 | OCR: guided document reading | 3 | Planned | Strong demo value, moderate complexity |
| F004 | Object assistance in controlled environments | 4 | In Progress | TFLite runtime and DAT still-capture bridge are running; validation and tuning remain |
| F005 | Audio recording, transcription, grounded Q&A | 5 | Planned | More storage and battery impact |
| F006 | Progress audio prompts | 6 | Planned | May ship earlier if needed by OCR |
| F007 | Route capture and replay | 7 | Deferred | High complexity and safety risk |
| F008 | Broad urban object assistance | 8 | Deferred | Over-scoped for first prototype |
| F009 | Battery and performance validation | Cross-cutting | In Progress | Baseline and comparison protocol required before expanding continuous features |

## Status Definitions

- `Planned` - accepted into roadmap, not started
- `In Progress` - actively being implemented
- `Blocked` - waiting on a dependency or unresolved ambiguity
- `Validated` - implemented and tested with evidence recorded
- `Partially Validated` - core path tested, but one planned sub-capability is still pending
- `Deferred` - intentionally out of current scope

## Execution Rule

No feature moves to `Validated` until its feature record includes:

- implementation summary
- test procedure
- observed result
- known limits
- reusable knowledge
