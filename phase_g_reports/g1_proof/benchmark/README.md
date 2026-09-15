# Phase G1 repeatable performance benchmark

Status: **SMOKE_ONLY / COMPONENT_PROXY_ONLY / PENDING_LIVE_PRECUTOVER**

This is the G1 headless renderer-component proxy. It measures the concrete
classic `EntityModel` and candidate `GeoRenderer` CPU vertex paths. It does
not a Q6 pass. The Tier-1, GPU, MSPT, and MHLib packet scenes remain a
binding pre-cutover gate for the first runtime-integrated conversion slice.

- Captured: 2026-09-15T10:26:21.676504700Z
- OS: Windows 11 10.0 (amd64)
- CPU: Intel64 Family 6 Model 198 Stepping 2, GenuineIntel (24 logical processors)
- JVM: Microsoft 21.0.7 / OpenJDK 64-Bit Server VM
- JVM flags: ['-Dfile.encoding=UTF-8', '-Duser.country=US', '-Duser.language=en', '-Duser.variant']
- Repository base revision: `27179b75c6bd04e7b334781e39dc667dbe302c0a` (working content bound by source/input hashes).
- Warmup/runs: 1s; 2 x 2s per scene
- Seed: N/A — no randomized world exists in the headless component proxy
- Resolution: N/A — no window, raster target, or GPU submission
- Camera: N/A — the proxy submits no camera or view transform
- Fixed state: fixed bind/static or Beaver full-amplitude quarter-cycle pose
- Timing order: paired AB/BA alternation per measured batch; smoke run 1 starts classic/candidate and run 2 starts candidate/classic

## elevator_100_visible

- Scope: renderer vertex submission only; no window, GPU, client tick, or server.
- Classic median/p95: 0.062795000 / 0.143905000 ms.
- Candidate median/p95: 0.177315000 / 0.408810000 ms.
- Candidate 1% low: 1939.112 FPS (component-only inverse p99).
- WARNING — component median ratio delta: 182.37120789871804%; absolute p95 delta: 0.264905 ms.
- Allocation classic/candidate: 93880.000 / 346680.000 bytes per frame.
- Model-bone instances: 500; MHLib parts: 0.

## beaver_100_visible

- Scope: renderer vertex submission only; no window, GPU, client tick, or server.
- Classic median/p95: 0.103525000 / 0.138685000 ms.
- Candidate median/p95: 0.216945000 / 0.288675000 ms.
- Candidate 1% low: 2615.576 FPS (component-only inverse p99).
- WARNING — component median ratio delta: 109.55807775899542%; absolute p95 delta: 0.14999000000000001 ms.
- Allocation classic/candidate: 171480.000 / 394680.000 bytes per frame.
- Model-bone instances: 900; MHLib parts: 0.

## mixed_100_visible

- Scope: renderer vertex submission only; no window, GPU, client tick, or server.
- Classic median/p95: 0.091245000 / 0.129920000 ms.
- Candidate median/p95: 0.190575000 / 0.261530000 ms.
- Candidate 1% low: 3446.493 FPS (component-only inverse p99).
- WARNING — component median ratio delta: 108.86075949367088%; absolute p95 delta: 0.13161000000000003 ms.
- Allocation classic/candidate: 132680.000 / 305080.000 bytes per frame.
- Model-bone instances: 700; MHLib parts: 0.

## mixed_100_rotation_state_only

- Scope: rotation-state iteration only; not offscreen rendering, culling, or controller work.
- Classic median/p95: 0.000988500 / 0.001310000 ms.
- Candidate median/p95: 0.002041500 / 0.002693500 ms.
- Candidate 1% low: 344293.338 FPS (component-only inverse p99).
- WARNING — component median ratio delta: 106.52503793626704%; absolute p95 delta: 0.0013834999999999997 ms.
- Allocation classic/candidate: 280.000 / 280.000 bytes per frame.
- Model-bone instances: 700; MHLib parts: 0.

## Q6 status and exact mixed-100 warning numbers

- WARNING — mixed-100 component median ratio delta: 108.86075949367088%.
- WARNING — mixed-100 absolute component p95 delta: 0.13161000000000003 ms.
- These component warnings must not be compared with or substituted for Q6's
  whole-client ≤10% median and ≤2 ms p95 acceptance limits.
- Final whole-client median/p95, GPU time, server p95 <50 ms, and no sustained
  MHLib packet growth remain mandatory when a runtime candidate exists.

Reproduce with `gradlew.bat g1Benchmark`,
then validate/promote with `tools/g1_benchmark_gate.py`.
