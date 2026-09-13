# Phase G1 repeatable performance benchmark

Status: **SMOKE_ONLY / COMPONENT_PROXY_ONLY / PENDING_LIVE_PRECUTOVER**

This is the G1 headless renderer-component proxy. It measures the concrete
classic `EntityModel` and candidate `GeoRenderer` CPU vertex paths. It does
not a Q6 pass. The Tier-1, GPU, MSPT, and MHLib packet scenes remain a
binding pre-cutover gate for the first runtime-integrated conversion slice.

- Captured: 2026-09-13T17:47:43.173900800Z
- OS: Windows 11 10.0 (amd64)
- CPU: Intel64 Family 6 Model 198 Stepping 2, GenuineIntel (24 logical processors)
- JVM: Microsoft 21.0.7 / OpenJDK 64-Bit Server VM
- JVM flags: ['-Dfile.encoding=UTF-8', '-Duser.country=US', '-Duser.language=en', '-Duser.variant']
- Repository base revision: `fff420791815d830f92cf59b6b774d1bfdeb643c` (working content bound by source/input hashes).
- Warmup/runs: 1s; 2 x 2s per scene
- Seed: N/A — no randomized world exists in the headless component proxy
- Resolution: N/A — no window, raster target, or GPU submission
- Camera: N/A — the proxy submits no camera or view transform
- Fixed state: fixed bind/static or Beaver full-amplitude quarter-cycle pose
- Timing order: paired AB/BA alternation per measured batch; smoke run 1 starts classic/candidate and run 2 starts candidate/classic

## elevator_100_visible

- Scope: renderer vertex submission only; no window, GPU, client tick, or server.
- Classic median/p95: 0.050145000 / 0.067085000 ms.
- Candidate median/p95: 0.147845000 / 0.198420000 ms.
- Candidate 1% low: 4081.716 FPS (component-only inverse p99).
- WARNING — component median ratio delta: 194.83497856216974%; absolute p95 delta: 0.13133499999999998 ms.
- Allocation classic/candidate: 93880.000 / 346680.000 bytes per frame.
- Model-bone instances: 500; MHLib parts: 0.

## beaver_100_visible

- Scope: renderer vertex submission only; no window, GPU, client tick, or server.
- Classic median/p95: 0.112445000 / 0.140530000 ms.
- Candidate median/p95: 0.238910000 / 0.297325000 ms.
- Candidate 1% low: 2868.247 FPS (component-only inverse p99).
- WARNING — component median ratio delta: 112.46831784427944%; absolute p95 delta: 0.15679500000000002 ms.
- Allocation classic/candidate: 171480.000 / 394680.000 bytes per frame.
- Model-bone instances: 900; MHLib parts: 0.

## mixed_100_visible

- Scope: renderer vertex submission only; no window, GPU, client tick, or server.
- Classic median/p95: 0.082705000 / 0.103665000 ms.
- Candidate median/p95: 0.174825000 / 0.219900000 ms.
- Candidate 1% low: 3789.960 FPS (component-only inverse p99).
- WARNING — component median ratio delta: 111.38383410918324%; absolute p95 delta: 0.11623499999999998 ms.
- Allocation classic/candidate: 132680.000 / 305080.000 bytes per frame.
- Model-bone instances: 700; MHLib parts: 0.

## mixed_100_rotation_state_only

- Scope: rotation-state iteration only; not offscreen rendering, culling, or controller work.
- Classic median/p95: 0.000955000 / 0.001273000 ms.
- Candidate median/p95: 0.001966000 / 0.002493000 ms.
- Candidate 1% low: 294117.647 FPS (component-only inverse p99).
- WARNING — component median ratio delta: 105.86387434554973%; absolute p95 delta: 0.0012199999999999999 ms.
- Allocation classic/candidate: 280.000 / 280.000 bytes per frame.
- Model-bone instances: 700; MHLib parts: 0.

## Q6 status and exact mixed-100 warning numbers

- WARNING — mixed-100 component median ratio delta: 111.38383410918324%.
- WARNING — mixed-100 absolute component p95 delta: 0.11623499999999998 ms.
- These component warnings must not be compared with or substituted for Q6's
  whole-client ≤10% median and ≤2 ms p95 acceptance limits.
- Final whole-client median/p95, GPU time, server p95 <50 ms, and no sustained
  MHLib packet growth remain mandatory when a runtime candidate exists.

Reproduce with `gradlew.bat g1Benchmark`,
then validate/promote with `tools/g1_benchmark_gate.py`.
