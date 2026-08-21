# Battery and Performance Test Plan

## Goal

Measure the incremental cost of each edge feature on the same Android phone before adding more
continuous workloads. Battery percentage alone is too coarse for short tests, so record it together
with Android battery statistics, elapsed time, latency, and device temperature.

## Controlled conditions

- Use the same physical phone for every comparison. An emulator is useful for behavior, not battery.
- Start each test at 80% battery or higher and do not charge while measuring.
- Do not leave a USB cable connected during a measurement. Use Android Wireless Debugging so ADB
  remains available without external power.
- Keep screen brightness, volume, network, Bluetooth state, camera source, and ambient temperature
  unchanged across runs.
- Close unrelated applications and disable battery saver.
- Use the same MockDeviceKit feed or the same fixed physical scene.
- Repeat each scenario three times for 10 minutes. Use 20 minutes when battery percentage changes
  are too small to observe.

## Connect ADB without charging

On Android 14, pair the phone through Wi-Fi before the first benchmark:

1. Connect the phone and development computer to the same Wi-Fi network.
2. On the phone, open **Settings > Developer options > Wireless debugging** and enable it.
3. In Android Studio, open **Device Manager > Pair Devices Using Wi-Fi** and choose QR-code or
   pairing-code mode.
4. Confirm that `adb devices` lists the phone, then unplug the USB cable.
5. Wait 2 minutes after disconnecting USB before resetting statistics and starting the timer.

Wireless debugging itself consumes some energy, but it is acceptable for a comparative benchmark if
it stays enabled in every scenario. Record that it was enabled in the evidence template.

If Wi-Fi debugging is unavailable, run the timed test fully unplugged and collect the ADB outputs
only after it ends. In that fallback, record battery percentage before reconnecting USB and wait for
the phone to stop charging before reading final diagnostics.

## Scenarios

| ID | Scenario | Purpose |
| --- | --- | --- |
| P0 | App open, no session | Application baseline |
| P1 | Session started, no preview | DAT connection cost |
| P2 | Preview only | Camera stream and HEVC decoder cost |
| P3 | Preview plus Hands-free | Continuous microphone and wake-word cost |
| P4 | Preview plus object assistance | Still capture every 2 seconds plus TFLite cost |
| P5 | Preview plus Hands-free plus object assistance | Primary demo configuration |
| P6 | Recording without microphone | Video recording cost |
| P7 | Recording with microphone | Video and microphone contention cost |

## Data collection

Before each run, connect the phone through ADB and reset Android's accumulated battery statistics:

```powershell
adb shell dumpsys batterystats --reset
```

At the end of each run, save the outputs with a scenario-specific file name:

```powershell
adb shell dumpsys batterystats > batterystats-P4-run1.txt
adb shell dumpsys battery > battery-P4-run1.txt
adb shell dumpsys thermalservice > thermal-P4-run1.txt
```

Also record manually: start/end battery percentage, elapsed minutes, average command-response
latency, object-alert latency, number of alerts, crashes, and whether the phone became hot.

Do not commit generated `batterystats-*.txt` files. Store team evidence in the issue, shared drive,
or a concise summary in the relevant BMAD feature record.

## Decision thresholds

- If P3 materially increases consumption versus P2, increase wake-word sensitivity threshold only
  after first checking microphone and TTS lifecycle leaks.
- If P4 is costly, increase the still-capture interval from 2 seconds to 3 or 4 seconds before
  reducing the model quality.
- If P5 is too costly, suspend object sampling while TTS is speaking and resume after speech ends.
- If temperature rises consistently, stop the feature after a defined duration and announce a
  voice-first fallback.
- Never optimize using battery percentage alone; compare repeated runs and latency together.

## First optimization levers

1. Object sampling cadence: 2 seconds is the current baseline; make it adaptive before changing
   the model.
2. Object model input: current maximum image side is 320 px; test 256 px if latency is high.
3. Stream configuration: use medium or low video quality and the lowest frame rate that keeps the
   preview usable.
4. Wake-word lifecycle: stop the engine whenever hands-free is off, session ends, or another
   microphone owner is active.
5. TTS lifecycle: do not run inference at full cadence while long speech is playing.

## Evidence template

```text
Date:
Tester and phone model:
Android version:
Scenario: P0-P7
Start/end battery percentage:
Elapsed time:
Average latency:
Observed temperature or throttling:
Errors or unexpected behavior:
Decision and next adjustment:
```
