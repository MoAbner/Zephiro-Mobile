# Future Plan - Wake Word with TFLite

## Purpose

This is a deferred implementation plan for replacing or complementing the wake-word path with a small custom TensorFlow Lite keyword-spotting model trained for this project.

This plan is intentionally asynchronous:

- it does not block the current product path
- it can proceed once the training assets and model are ready
- it remains compatible with the current voice architecture

## Why This Exists

The project still wants a true wake word, but should avoid a commercial trial dependency as a long-term base.

The TFLite option is attractive because:

- it stays local on device
- it avoids enterprise trial coupling
- the model can be tuned to the exact phrase chosen for the prototype
- it can remain under project control after the hackathon

## Current Recommendation

Primary wake-word target for near-term product integration:

- `sherpa-onnx`

Deferred experimental path:

- custom `TFLite` keyword spotting model trained by the user

## Intended Architecture

The app should keep the `WakeWordEngine` abstraction and swap implementations behind it:

- `SherpaWakeWordEngine` for the main near-term path
- `TfliteWakeWordEngine` for the future experimental path
- manual voice button as permanent fallback

## Training Deliverables Needed

Before integration, the future TFLite path should produce:

- trained `.tflite` model
- label file or output mapping
- expected sample rate
- expected frame/window duration
- preprocessing requirements
- threshold recommendation
- false positive and false negative notes from offline testing

## Integration Requirements

When the model is ready, the Android app will need:

- a small audio capture loop dedicated to keyword spotting
- deterministic preprocessing matching the model training pipeline
- inference wrapper around `Interpreter` or TensorFlow Lite Task API
- debounce/cooldown to prevent repeated triggers
- background lifecycle handling

## Proposed Implementation Steps

1. Define the wake phrase and the exact language/accent expectation.
2. Train and export the keyword model to `.tflite`.
3. Freeze the preprocessing contract:
   - sample rate
   - frame size
   - feature extraction
   - normalization
4. Create `TfliteWakeWordEngine`.
5. Add offline test assets under Android test or local test resources.
6. Compare runtime CPU and trigger quality against the manual flow.
7. Decide whether it is strong enough to replace or complement `sherpa-onnx`.

## Validation Checklist

- [ ] trigger works repeatedly on device microphone input
- [ ] false activations are acceptable in indoor test conditions
- [ ] idle CPU and battery use remain acceptable
- [ ] command recognition still starts reliably after wake detection
- [ ] manual fallback remains available

## Risks

- a custom TFLite model may require more training iteration than expected
- mismatch between training preprocessing and Android runtime can ruin accuracy
- poor threshold tuning can make the wake word unusable
- always-listening audio still has battery cost even with a small model

## Decision Rule

Use the TFLite wake word in the app only if it proves:

- stable enough for demos
- materially independent from commercial gating
- no worse than the manual trigger path in usability

If not, keep it as a research branch and continue with `sherpa-onnx` or gesture/manual triggering.
