# F004 - Object assistance in controlled environments

## Metadata

- Feature ID: F004
- Name: Object assistance in controlled environments
- Owner: Codex + user
- Status: In Progress
- Started: 2026-08-20

## Goal

Provide spoken object-awareness in a reliable, constrained MVP scope before attempting broad urban coverage.

## Scope

In scope:

- controlled room and classroom-like scenes
- limited object set with conservative thresholds
- spoken alerts with cooldown and class priority
- frame pipeline prepared for DAT stream integration

Out of scope:

- full urban scene understanding
- safety-critical navigation claims
- dense multi-object narration

## Source Prototype

Reference Python prototype in `Referencia_detec_python/script_inferencia.py`:

- `YOLO("yolov8n.pt")`
- confidence threshold `0.45`
- area-based proximity heuristic
- cooldown per class
- audio-file-first notification with TTS fallback

## MVP Direction

If urban scope starts growing exponentially, the MVP remains intentionally reduced to controlled environments such as:

- room
- classroom
- indoor corridor

The first detector class set should stay narrow:

- `person`
- `car`
- `motorcycle`
- `bus`
- `truck`
- `bicycle`

Optional indoor extras may be enabled later:

- `chair`
- `bottle`

## Current Implementation Slice

- Kotlin detection module scaffold created under `samples/CameraAccess/.../detection`
- object mapping, proximity estimation, phrase generation, cooldown, and alert priority ported from the Python logic
- detector runtime intentionally left abstract so we can attach either TFLite or ONNX later without rewriting the policy layer
- `descrever ambiente` can now activate the object-assistance feature flow even before the detector runtime is connected
- the orchestrator can now stop object assistance as an explicit active feature
- TFLite Task Vision is now connected to the configured model asset
- while preview and object assistance are active, the app captures one still every 2 seconds,
  downscales it to a maximum side of 320 px, and runs inference off the main thread
- spoken-alert ranking is configurable: `DISTANCE_FIRST` is the controlled-room default and
  `SAFETY_FIRST` is reserved for a future urban profile

## Acceptance Checks

- [x] one mobile detector is integrated on-device
- [x] one frame from DAT can be processed through the detector path
- [x] the app can enter and leave object-assistance mode through the current voice/orchestrator flow
- [ ] alerts are prioritized and cooled down
- [ ] the spoken output focuses on the single most relevant object at a time

## Design Note

DAT's preview stream is compressed HEVC and is rendered directly to a `Surface`. The F004 MVP
therefore uses periodic still capture as its RGB input instead of adding a duplicate HEVC
decode-to-bitmap pipeline. This protects preview stability and limits inference work to one sample
every 2 seconds.

For controlled rooms, the ranking weights distance more than the object class. A nearby chair or
person can therefore be announced before a distant car. The alternative safety profile increases
the class-priority weight for future urban work without changing the detector or voice layer.

## Next Steps

- validate detection with a person, chair, bottle, or other COCO object in the phone camera feed
- tune threshold, priority, and cadence after measuring latency on the target phone
- evaluate a decoded-video pipeline only if 2-second sampling is insufficient
