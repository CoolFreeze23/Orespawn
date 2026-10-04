# Phase G1 repeatable performance benchmark

Status: **SMOKE_ONLY / COMPONENT_PROXY_ONLY / PENDING_LIVE_PRECUTOVER**

This is the G1 headless renderer-component proxy. It measures the concrete
classic `EntityModel` and candidate `GeoRenderer` CPU vertex paths. It does
not a Q6 pass. The Tier-1, GPU, MSPT, and MHLib packet scenes remain a
binding pre-cutover gate for the first runtime-integrated conversion slice.

- Captured: 2026-10-02T21:17:06.501526700Z
- OS: Windows 11 10.0 (amd64)
- CPU: Intel64 Family 6 Model 198 Stepping 2, GenuineIntel (24 logical processors)
- JVM: Microsoft 21.0.7 / OpenJDK 64-Bit Server VM
- JVM flags: ['-Dfile.encoding=UTF-8', '-Duser.country=US', '-Duser.language=en', '-Duser.variant']
- Repository base revision: `c6f30a497bad2b5fbef4334aac5636a2a7eed53e` (working content bound by source/input hashes).
- Warmup/runs: 1s; 2 x 2s per scene
- Seed: N/A — no randomized world exists in the headless component proxy
- Resolution: N/A — no window, raster target, or GPU submission
- Camera: N/A — the proxy submits no camera or view transform
- Fixed state: fixed bind/static or Beaver full-amplitude quarter-cycle pose
- Timing order: paired AB/BA alternation per measured batch; smoke run 1 starts classic/candidate and run 2 starts candidate/classic

## elevator_100_visible

- Scope: renderer vertex submission only; no window, GPU, client tick, or server.
- Classic median/p95: 0.054910000 / 0.083170000 ms.
- Candidate median/p95: 0.159330000 / 0.241875000 ms.
- Candidate 1% low: 2874.678 FPS (component-only inverse p99).
- WARNING — component median ratio delta: 190.16572573301764%; absolute p95 delta: 0.15870500000000001 ms.
- Allocation classic/candidate: 93880.000 / 346680.000 bytes per frame.
- Model-bone instances: 500; MHLib parts: 0.

## beaver_100_visible

- Scope: renderer vertex submission only; no window, GPU, client tick, or server.
- Classic median/p95: 0.120769500 / 0.179210000 ms.
- Candidate median/p95: 0.226144500 / 0.328835000 ms.
- Candidate 1% low: 2619.035 FPS (component-only inverse p99).
- WARNING — component median ratio delta: 87.252990200340321%; absolute p95 delta: 0.14962499999999998 ms.
- Allocation classic/candidate: 171480.000 / 394680.000 bytes per frame.
- Model-bone instances: 900; MHLib parts: 0.

## mixed_100_visible

- Scope: renderer vertex submission only; no window, GPU, client tick, or server.
- Classic median/p95: 0.087984500 / 0.118474500 ms.
- Candidate median/p95: 0.170805000 / 0.227050000 ms.
- Candidate 1% low: 3814.537 FPS (component-only inverse p99).
- WARNING — component median ratio delta: 94.130784399524913%; absolute p95 delta: 0.10857549999999999 ms.
- Allocation classic/candidate: 132680.000 / 305080.000 bytes per frame.
- Model-bone instances: 700; MHLib parts: 0.

## mixed_100_rotation_state_only

- Scope: rotation-state iteration only; not offscreen rendering, culling, or controller work.
- Classic median/p95: 0.001006500 / 0.001285500 ms.
- Candidate median/p95: 0.002099000 / 0.002715500 ms.
- Candidate 1% low: 325839.036 FPS (component-only inverse p99).
- WARNING — component median ratio delta: 108.54446100347741%; absolute p95 delta: 0.0014300000000000001 ms.
- Allocation classic/candidate: 280.000 / 280.000 bytes per frame.
- Model-bone instances: 700; MHLib parts: 0.

## Q6 status and exact mixed-100 warning numbers

- WARNING — mixed-100 component median ratio delta: 94.130784399524913%.
- WARNING — mixed-100 absolute component p95 delta: 0.10857549999999999 ms.
- These component warnings must not be compared with or substituted for Q6's
  whole-client ≤10% median and ≤2 ms p95 acceptance limits.
- Final whole-client median/p95, GPU time, server p95 <50 ms, and no sustained
  MHLib packet growth remain mandatory when a runtime candidate exists.

Reproduce with `gradlew.bat g1Benchmark`,
then validate/promote with `tools/g1_benchmark_gate.py`.
