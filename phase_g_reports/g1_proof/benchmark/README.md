# Phase G1 repeatable performance benchmark

Status: **SMOKE_ONLY / COMPONENT_PROXY_ONLY / PENDING_LIVE_PRECUTOVER**

This is the G1 headless renderer-component proxy. It measures the concrete
classic `EntityModel` and candidate `GeoRenderer` CPU vertex paths. It does
not a Q6 pass. The Tier-1, GPU, MSPT, and MHLib packet scenes remain a
binding pre-cutover gate for the first runtime-integrated conversion slice.

- Captured: 2026-09-19T05:10:59.832566500Z
- OS: Windows 11 10.0 (amd64)
- CPU: Intel64 Family 6 Model 198 Stepping 2, GenuineIntel (24 logical processors)
- JVM: Microsoft 21.0.7 / OpenJDK 64-Bit Server VM
- JVM flags: ['-Dfile.encoding=UTF-8', '-Duser.country=US', '-Duser.language=en', '-Duser.variant']
- Repository base revision: `537312ee4a395b0acc771ee1d5ebc8d82bb13495` (working content bound by source/input hashes).
- Warmup/runs: 1s; 2 x 2s per scene
- Seed: N/A — no randomized world exists in the headless component proxy
- Resolution: N/A — no window, raster target, or GPU submission
- Camera: N/A — the proxy submits no camera or view transform
- Fixed state: fixed bind/static or Beaver full-amplitude quarter-cycle pose
- Timing order: paired AB/BA alternation per measured batch; smoke run 1 starts classic/candidate and run 2 starts candidate/classic

## elevator_100_visible

- Scope: renderer vertex submission only; no window, GPU, client tick, or server.
- Classic median/p95: 0.045720000 / 0.062635000 ms.
- Candidate median/p95: 0.129410000 / 0.156460000 ms.
- Candidate 1% low: 5014.165 FPS (component-only inverse p99).
- WARNING — component median ratio delta: 183.04899387576558%; absolute p95 delta: 0.093824999999999992 ms.
- Allocation classic/candidate: 93880.000 / 346680.000 bytes per frame.
- Model-bone instances: 500; MHLib parts: 0.

## beaver_100_visible

- Scope: renderer vertex submission only; no window, GPU, client tick, or server.
- Classic median/p95: 0.096205000 / 0.112260000 ms.
- Candidate median/p95: 0.203100000 / 0.238090000 ms.
- Candidate 1% low: 3438.790 FPS (component-only inverse p99).
- WARNING — component median ratio delta: 111.11168858167457%; absolute p95 delta: 0.12583 ms.
- Allocation classic/candidate: 171480.000 / 394680.000 bytes per frame.
- Model-bone instances: 900; MHLib parts: 0.

## mixed_100_visible

- Scope: renderer vertex submission only; no window, GPU, client tick, or server.
- Classic median/p95: 0.075805000 / 0.086550000 ms.
- Candidate median/p95: 0.157520000 / 0.179660000 ms.
- Candidate 1% low: 4551.661 FPS (component-only inverse p99).
- WARNING — component median ratio delta: 107.79631950399052%; absolute p95 delta: 0.093109999999999998 ms.
- Allocation classic/candidate: 132680.000 / 305080.000 bytes per frame.
- Model-bone instances: 700; MHLib parts: 0.

## mixed_100_rotation_state_only

- Scope: rotation-state iteration only; not offscreen rendering, culling, or controller work.
- Classic median/p95: 0.000873500 / 0.000976000 ms.
- Candidate median/p95: 0.001823000 / 0.002024500 ms.
- Candidate 1% low: 467945.718 FPS (component-only inverse p99).
- WARNING — component median ratio delta: 108.70062965083002%; absolute p95 delta: 0.0010485000000000002 ms.
- Allocation classic/candidate: 280.000 / 280.000 bytes per frame.
- Model-bone instances: 700; MHLib parts: 0.

## Q6 status and exact mixed-100 warning numbers

- WARNING — mixed-100 component median ratio delta: 107.79631950399052%.
- WARNING — mixed-100 absolute component p95 delta: 0.093109999999999998 ms.
- These component warnings must not be compared with or substituted for Q6's
  whole-client ≤10% median and ≤2 ms p95 acceptance limits.
- Final whole-client median/p95, GPU time, server p95 <50 ms, and no sustained
  MHLib packet growth remain mandatory when a runtime candidate exists.

Reproduce with `gradlew.bat g1Benchmark`,
then validate/promote with `tools/g1_benchmark_gate.py`.
