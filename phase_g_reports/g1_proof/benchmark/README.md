# Phase G1 repeatable performance benchmark

Status: **SMOKE_ONLY / COMPONENT_PROXY_ONLY / PENDING_LIVE_PRECUTOVER**

This is the G1 headless renderer-component proxy. It measures the concrete
classic `EntityModel` and candidate `GeoRenderer` CPU vertex paths. It does
not a Q6 pass. The Tier-1, GPU, MSPT, and MHLib packet scenes remain a
binding pre-cutover gate for the first runtime-integrated conversion slice.

- Captured: 2026-09-03T11:25:57.820019300Z
- OS: Windows 11 10.0 (amd64)
- CPU: Intel64 Family 6 Model 198 Stepping 2, GenuineIntel (24 logical processors)
- JVM: Microsoft 21.0.7 / OpenJDK 64-Bit Server VM
- JVM flags: ['-Dfile.encoding=UTF-8', '-Duser.country=US', '-Duser.language=en', '-Duser.variant']
- Repository base revision: `02ea8551ce51a2544aa0fa47e346e07188ae7818` (working content bound by source/input hashes).
- Warmup/runs: 1s; 2 x 2s per scene
- Seed: N/A — no randomized world exists in the headless component proxy
- Resolution: N/A — no window, raster target, or GPU submission
- Camera: N/A — the proxy submits no camera or view transform
- Fixed state: fixed bind/static or Beaver full-amplitude quarter-cycle pose
- Timing order: paired AB/BA alternation per measured batch; smoke run 1 starts classic/candidate and run 2 starts candidate/classic

## elevator_100_visible

- Scope: renderer vertex submission only; no window, GPU, client tick, or server.
- Classic median/p95: 0.039610000 / 0.051980000 ms.
- Candidate median/p95: 0.123650000 / 0.167820000 ms.
- Candidate 1% low: 4804.228 FPS (component-only inverse p99).
- WARNING — component median ratio delta: 212.16864428174708%; absolute p95 delta: 0.11584000000000003 ms.
- Allocation classic/candidate: 91480.000 / 346680.000 bytes per frame.
- Model-bone instances: 500; MHLib parts: 0.

## beaver_100_visible

- Scope: renderer vertex submission only; no window, GPU, client tick, or server.
- Classic median/p95: 0.105615000 / 0.133875000 ms.
- Candidate median/p95: 0.195715000 / 0.246805000 ms.
- Candidate 1% low: 3502.627 FPS (component-only inverse p99).
- WARNING — component median ratio delta: 85.309851820290675%; absolute p95 delta: 0.11292999999999997 ms.
- Allocation classic/candidate: 171480.000 / 394680.000 bytes per frame.
- Model-bone instances: 900; MHLib parts: 0.

## mixed_100_visible

- Scope: renderer vertex submission only; no window, GPU, client tick, or server.
- Classic median/p95: 0.078245000 / 0.095035000 ms.
- Candidate median/p95: 0.147090000 / 0.178925000 ms.
- Candidate 1% low: 4585.368 FPS (component-only inverse p99).
- WARNING — component median ratio delta: 87.986452808486135%; absolute p95 delta: 0.083889999999999992 ms.
- Allocation classic/candidate: 131480.000 / 305080.000 bytes per frame.
- Model-bone instances: 700; MHLib parts: 0.

## mixed_100_rotation_state_only

- Scope: rotation-state iteration only; not offscreen rendering, culling, or controller work.
- Classic median/p95: 0.001014500 / 0.001136000 ms.
- Candidate median/p95: 0.002083500 / 0.002406500 ms.
- Candidate 1% low: 368188.513 FPS (component-only inverse p99).
- WARNING — component median ratio delta: 105.37210448496795%; absolute p95 delta: 0.0012704999999999999 ms.
- Allocation classic/candidate: 280.000 / 280.000 bytes per frame.
- Model-bone instances: 700; MHLib parts: 0.

## Q6 status and exact mixed-100 warning numbers

- WARNING — mixed-100 component median ratio delta: 87.986452808486135%.
- WARNING — mixed-100 absolute component p95 delta: 0.083889999999999992 ms.
- These component warnings must not be compared with or substituted for Q6's
  whole-client ≤10% median and ≤2 ms p95 acceptance limits.
- Final whole-client median/p95, GPU time, server p95 <50 ms, and no sustained
  MHLib packet growth remain mandatory when a runtime candidate exists.

Reproduce with `gradlew.bat g1Benchmark`,
then validate/promote with `tools/g1_benchmark_gate.py`.
