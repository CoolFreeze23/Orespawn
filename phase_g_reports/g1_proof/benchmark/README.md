# Phase G1 repeatable performance benchmark

Status: **SMOKE_ONLY / COMPONENT_PROXY_ONLY / PENDING_LIVE_PRECUTOVER**

This is the G1 headless renderer-component proxy. It measures the concrete
classic `EntityModel` and candidate `GeoRenderer` CPU vertex paths. It does
not a Q6 pass. The Tier-1, GPU, MSPT, and MHLib packet scenes remain a
binding pre-cutover gate for the first runtime-integrated conversion slice.

- Captured: 2026-09-13T07:01:19.401180500Z
- OS: Windows 11 10.0 (amd64)
- CPU: Intel64 Family 6 Model 198 Stepping 2, GenuineIntel (24 logical processors)
- JVM: Microsoft 21.0.7 / OpenJDK 64-Bit Server VM
- JVM flags: ['-Dfile.encoding=UTF-8', '-Duser.country=US', '-Duser.language=en', '-Duser.variant']
- Repository base revision: `b57853c3f56d0a8c08ebbed6222079d42cea2643` (working content bound by source/input hashes).
- Warmup/runs: 1s; 2 x 2s per scene
- Seed: N/A — no randomized world exists in the headless component proxy
- Resolution: N/A — no window, raster target, or GPU submission
- Camera: N/A — the proxy submits no camera or view transform
- Fixed state: fixed bind/static or Beaver full-amplitude quarter-cycle pose
- Timing order: paired AB/BA alternation per measured batch; smoke run 1 starts classic/candidate and run 2 starts candidate/classic

## elevator_100_visible

- Scope: renderer vertex submission only; no window, GPU, client tick, or server.
- Classic median/p95: 0.044680000 / 0.060905000 ms.
- Candidate median/p95: 0.137215000 / 0.183210000 ms.
- Candidate 1% low: 4264.392 FPS (component-only inverse p99).
- WARNING — component median ratio delta: 207.10608773500442%; absolute p95 delta: 0.12230500000000001 ms.
- Allocation classic/candidate: 93880.000 / 346680.000 bytes per frame.
- Model-bone instances: 500; MHLib parts: 0.

## beaver_100_visible

- Scope: renderer vertex submission only; no window, GPU, client tick, or server.
- Classic median/p95: 0.109285000 / 0.147825000 ms.
- Candidate median/p95: 0.208610000 / 0.273075000 ms.
- Candidate 1% low: 2822.905 FPS (component-only inverse p99).
- WARNING — component median ratio delta: 90.886214942581361%; absolute p95 delta: 0.12524999999999994 ms.
- Allocation classic/candidate: 171480.000 / 394680.000 bytes per frame.
- Model-bone instances: 900; MHLib parts: 0.

## mixed_100_visible

- Scope: renderer vertex submission only; no window, GPU, client tick, or server.
- Classic median/p95: 0.079945000 / 0.105715000 ms.
- Candidate median/p95: 0.155435000 / 0.206275000 ms.
- Candidate 1% low: 4044.980 FPS (component-only inverse p99).
- WARNING — component median ratio delta: 94.427418850459716%; absolute p95 delta: 0.10055999999999998 ms.
- Allocation classic/candidate: 132680.000 / 305080.000 bytes per frame.
- Model-bone instances: 700; MHLib parts: 0.

## mixed_100_rotation_state_only

- Scope: rotation-state iteration only; not offscreen rendering, culling, or controller work.
- Classic median/p95: 0.001016000 / 0.001299000 ms.
- Candidate median/p95: 0.002130500 / 0.002657000 ms.
- Candidate 1% low: 336417.157 FPS (component-only inverse p99).
- WARNING — component median ratio delta: 109.69488188976379%; absolute p95 delta: 0.001358 ms.
- Allocation classic/candidate: 280.000 / 280.000 bytes per frame.
- Model-bone instances: 700; MHLib parts: 0.

## Q6 status and exact mixed-100 warning numbers

- WARNING — mixed-100 component median ratio delta: 94.427418850459716%.
- WARNING — mixed-100 absolute component p95 delta: 0.10055999999999998 ms.
- These component warnings must not be compared with or substituted for Q6's
  whole-client ≤10% median and ≤2 ms p95 acceptance limits.
- Final whole-client median/p95, GPU time, server p95 <50 ms, and no sustained
  MHLib packet growth remain mandatory when a runtime candidate exists.

Reproduce with `gradlew.bat g1Benchmark`,
then validate/promote with `tools/g1_benchmark_gate.py`.
