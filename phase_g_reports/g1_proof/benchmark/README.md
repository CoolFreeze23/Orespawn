# Phase G1 repeatable performance benchmark

Status: **SMOKE_ONLY / COMPONENT_PROXY_ONLY / PENDING_LIVE_PRECUTOVER**

This is the G1 headless renderer-component proxy. It measures the concrete
classic `EntityModel` and candidate `GeoRenderer` CPU vertex paths. It does
not a Q6 pass. The Tier-1, GPU, MSPT, and MHLib packet scenes remain a
binding pre-cutover gate for the first runtime-integrated conversion slice.

- Captured: 2026-09-06T00:24:23.498075200Z
- OS: Windows 11 10.0 (amd64)
- CPU: Intel64 Family 6 Model 198 Stepping 2, GenuineIntel (24 logical processors)
- JVM: Microsoft 21.0.7 / OpenJDK 64-Bit Server VM
- JVM flags: ['-Dfile.encoding=UTF-8', '-Duser.country=US', '-Duser.language=en', '-Duser.variant']
- Repository base revision: `a03c0c571eb0c5b732d1d8e159491ee3a1273a86` (working content bound by source/input hashes).
- Warmup/runs: 1s; 2 x 2s per scene
- Seed: N/A — no randomized world exists in the headless component proxy
- Resolution: N/A — no window, raster target, or GPU submission
- Camera: N/A — the proxy submits no camera or view transform
- Fixed state: fixed bind/static or Beaver full-amplitude quarter-cycle pose
- Timing order: paired AB/BA alternation per measured batch; smoke run 1 starts classic/candidate and run 2 starts candidate/classic

## elevator_100_visible

- Scope: renderer vertex submission only; no window, GPU, client tick, or server.
- Classic median/p95: 0.051465000 / 0.070030000 ms.
- Candidate median/p95: 0.147030000 / 0.195885000 ms.
- Candidate 1% low: 3998.321 FPS (component-only inverse p99).
- WARNING — component median ratio delta: 185.68930341008451%; absolute p95 delta: 0.12585499999999999 ms.
- Allocation classic/candidate: 93880.000 / 346680.000 bytes per frame.
- Model-bone instances: 500; MHLib parts: 0.

## beaver_100_visible

- Scope: renderer vertex submission only; no window, GPU, client tick, or server.
- Classic median/p95: 0.108510000 / 0.127580000 ms.
- Candidate median/p95: 0.221720000 / 0.261580000 ms.
- Candidate 1% low: 3495.465 FPS (component-only inverse p99).
- WARNING — component median ratio delta: 104.33139802783154%; absolute p95 delta: 0.13400000000000004 ms.
- Allocation classic/candidate: 173880.000 / 394680.000 bytes per frame.
- Model-bone instances: 900; MHLib parts: 0.

## mixed_100_visible

- Scope: renderer vertex submission only; no window, GPU, client tick, or server.
- Classic median/p95: 0.097330000 / 0.117985000 ms.
- Candidate median/p95: 0.180580000 / 0.220015000 ms.
- Candidate 1% low: 4040.894 FPS (component-only inverse p99).
- WARNING — component median ratio delta: 85.533751155861523%; absolute p95 delta: 0.10203000000000001 ms.
- Allocation classic/candidate: 133880.000 / 305080.000 bytes per frame.
- Model-bone instances: 700; MHLib parts: 0.

## mixed_100_rotation_state_only

- Scope: rotation-state iteration only; not offscreen rendering, culling, or controller work.
- Classic median/p95: 0.001009000 / 0.001164000 ms.
- Candidate median/p95: 0.002133500 / 0.002464000 ms.
- Candidate 1% low: 377928.949 FPS (component-only inverse p99).
- WARNING — component median ratio delta: 111.44697720515362%; absolute p95 delta: 0.0012999999999999999 ms.
- Allocation classic/candidate: 280.000 / 280.000 bytes per frame.
- Model-bone instances: 700; MHLib parts: 0.

## Q6 status and exact mixed-100 warning numbers

- WARNING — mixed-100 component median ratio delta: 85.533751155861523%.
- WARNING — mixed-100 absolute component p95 delta: 0.10203000000000001 ms.
- These component warnings must not be compared with or substituted for Q6's
  whole-client ≤10% median and ≤2 ms p95 acceptance limits.
- Final whole-client median/p95, GPU time, server p95 <50 ms, and no sustained
  MHLib packet growth remain mandatory when a runtime candidate exists.

Reproduce with `gradlew.bat g1Benchmark`,
then validate/promote with `tools/g1_benchmark_gate.py`.
