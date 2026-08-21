# Project Brief

## Vision

Build a voice-first assistive smart-glasses prototype for people with visual impairment in urban or semi-controlled environments, using Meta Wearables DAT as the wearable access layer and Android as the edge compute host.

## Hackathon Goal

Deliver a demonstrable prototype that shows practical assistive value with low interaction friction, acceptable latency, and clear scope boundaries.

## Primary User Outcomes

- Start interaction with a wake word or short voice command
- Ask the system to describe nearby objects or people
- Ask the system to read a document aloud
- Record spoken content for later transcription and question answering
- Receive spoken updates when a requested operation is still processing

## Non-Goals for Initial Delivery

- Fully autonomous urban navigation
- High-confidence safety guidance in uncontrolled outdoor settings
- Rich graphical UI for end users
- Dependence on smart-glasses display output

## Success Criteria

- Voice foundation works reliably enough to trigger and route commands
- At least one vision workflow works end-to-end with spoken feedback
- At least one audio workflow works end-to-end with persistent records
- Orchestrator can resolve conflicts between concurrent feature requests
- All validated features are documented for repeatable future use

## Scope Positioning

The first version should optimize for controlled demonstrations:

- indoor or semi-controlled object assistance first
- document OCR under guided framing
- audio recording with post-processing, not simultaneous full duplex intelligence

## Delivery Strategy

Implement in thin vertical slices:

1. voice foundation
2. orchestration foundation
3. OCR slice
4. object assistance slice
5. recording and knowledge slice

Each slice must be independently testable and documented before the next one expands scope.
