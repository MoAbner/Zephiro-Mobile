# Benchmark Runbook

## Objective

Create a reproducible battery, thermal, and latency baseline for the current Android prototype
before adding OCR or another continuous workload.

## Device and environment

- Physical Android 14 phone only; do not use an emulator for battery results.
- Start each run at 80% battery or higher.
- Keep screen brightness, volume, Wi-Fi, Bluetooth, camera source, and room temperature constant.
- Close unrelated applications and disable battery saver.
- Configure MockDeviceKit and the selected camera source before each run.
- USB is permitted only for install, setup, and final diagnostics. It must be disconnected during
  the timed measurement.

## Script

The collection helper is `tools/run-benchmark.ps1`. It saves local diagnostics in
`benchmark-results/`, which is intentionally ignored by Git.

If PowerShell blocks local scripts for the current terminal only, run:

```powershell
Set-ExecutionPolicy -Scope Process Bypass
```

## Test order

Run each scenario three times. Use 10 minutes initially; increase to 20 minutes if the battery
percentage does not change enough to compare runs.

| Order | Scenario | App state |
| --- | --- | --- |
| 1 | P2 | Session + Preview; hands-free off; object assistance off |
| 2 | P3 | Session + Preview + Hands-free; object assistance off |
| 3 | P4 | Session + Preview + object assistance; hands-free off |
| 4 | P5 | Session + Preview + Hands-free + object assistance |

Do not start P3-P5 until P2 has a successful, crash-free run.

## Per-run procedure

1. Connect USB and deploy the debug app from Android Studio if needed.
2. Open the app and configure exactly the scenario in the table.
3. Run the start command. Example for P2, first run:

```powershell
.\tools\run-benchmark.ps1 -Scenario P2 -Run 1 -Phase Begin -DurationMinutes 10 -RunTimer
```

4. When prompted by the script, disconnect USB, read the battery percentage on the phone, enter it
   in the terminal, and leave the phone in the configured scenario.
5. When the timer ends, read and write down the final percentage on the phone before reconnecting
   USB.
6. Reconnect USB and run the finish command with the value observed in step 5:

```powershell
.\tools\run-benchmark.ps1 -Scenario P2 -Run 1 -Phase Finish -BatteryEndPercent 89
```

7. Repeat as `-Run 2` and `-Run 3`, then proceed to the next scenario.

## Evidence table

Copy the values from each `*-summary.json` into this table or the relevant BMAD feature record.

| Scenario | Run | Start % | End % | Minutes | %/hour | Wake-word latency | Object-alert latency | Temperature/notes |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| P2 | 1 | | | | | N/A | N/A | |
| P3 | 1 | | | | | | N/A | |
| P4 | 1 | | | | | N/A | | |
| P5 | 1 | | | | | | | |

## Decision rules

- Compare the average of three runs, not a single battery reading.
- Keep P5 only if its latency remains usable and its added consumption is justified by the demo.
- If P4 or P5 is costly, first test object sampling at 3 seconds, then 4 seconds.
- If heat rises or the app slows down, lower image input from 320 px to 256 px before changing the
  detector model.
- Record every selected change in `docs/bmad-decision-log.md` and repeat P4/P5 after it.
