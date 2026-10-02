# Phase G1 repeatable performance benchmark

Status: **SMOKE_ONLY / COMPONENT_PROXY_ONLY / PENDING_LIVE_PRECUTOVER**

This is the G1 headless renderer-component proxy. It measures the concrete
classic `EntityModel` and candidate `GeoRenderer` CPU vertex paths. It does
not a Q6 pass. The Tier-1, GPU, MSPT, and MHLib packet scenes remain a
binding pre-cutover gate for the first runtime-integrated conversion slice.

- Captured: 2026-10-02T07:01:21.678639600Z
- OS: Windows 11 10.0 (amd64)
- CPU: Intel64 Family 6 Model 198 Stepping 2, GenuineIntel (24 logical processors)
- JVM: Microsoft 21.0.7 / OpenJDK 64-Bit Server VM
- JVM flags: ['-Dfile.encoding=UTF-8', '-Duser.country=US', '-Duser.language=en', '-Duser.variant']
- Repository base revision: `75d426d1296a82fe2248970ebf47722b69b8fa42` (working content bound by source/input hashes).
- Warmup/runs: 1s; 2 x 2s per scene
- Seed: N/A — no randomized world exists in the headless component proxy
- Resolution: N/A — no window, raster target, or GPU submission
- Camera: N/A — the proxy submits no camera or view transform
- Fixed state: fixed bind/static or Beaver full-amplitude quarter-cycle pose
- Timing order: paired AB/BA alternation per measured batch; smoke run 1 starts classic/candidate and run 2 starts candidate/classic

## elevator_100_visible

- Scope: renderer vertex submission only; no window, GPU, client tick, or server.
- Classic median/p95: 0.050385000 / 0.069065000 ms.
- Candidate median/p95: 0.142180000 / 0.192475000 ms.
- Candidate 1% low: 4409.852 FPS (component-only inverse p99).
- WARNING — component median ratio delta: 182.18715887664976%; absolute p95 delta: 0.12341000000000001 ms.
- Allocation classic/candidate: 93880.000 / 346680.000 bytes per frame.
- Model-bone instances: 500; MHLib parts: 0.

## beaver_100_visible

- Scope: renderer vertex submission only; no window, GPU, client tick, or server.
- Classic median/p95: 0.110305000 / 0.140675000 ms.
- Candidate median/p95: 0.229350000 / 0.290035000 ms.
- Candidate 1% low: 3118.228 FPS (component-only inverse p99).
- WARNING — component median ratio delta: 107.92348488282491%; absolute p95 delta: 0.14936000000000005 ms.
- Allocation classic/candidate: 171480.000 / 394680.000 bytes per frame.
- Model-bone instances: 900; MHLib parts: 0.

## mixed_100_visible

- Scope: renderer vertex submission only; no window, GPU, client tick, or server.
- Classic median/p95: 0.085710000 / 0.112485000 ms.
- Candidate median/p95: 0.176310000 / 0.225465000 ms.
- Candidate 1% low: 3921.415 FPS (component-only inverse p99).
- WARNING — component median ratio delta: 105.70528526426321%; absolute p95 delta: 0.11298 ms.
- Allocation classic/candidate: 132680.000 / 305080.000 bytes per frame.
- Model-bone instances: 700; MHLib parts: 0.

## mixed_100_rotation_state_only

- Scope: rotation-state iteration only; not offscreen rendering, culling, or controller work.
- Classic median/p95: 0.000982000 / 0.001126000 ms.
- Candidate median/p95: 0.002014500 / 0.002316500 ms.
- Candidate 1% low: 411522.634 FPS (component-only inverse p99).
- WARNING — component median ratio delta: 105.14256619144602%; absolute p95 delta: 0.0011905000000000002 ms.
- Allocation classic/candidate: 280.000 / 280.000 bytes per frame.
- Model-bone instances: 700; MHLib parts: 0.

## Q6 status and exact mixed-100 warning numbers

- WARNING — mixed-100 component median ratio delta: 105.70528526426321%.
- WARNING — mixed-100 absolute component p95 delta: 0.11298 ms.
- These component warnings must not be compared with or substituted for Q6's
  whole-client ≤10% median and ≤2 ms p95 acceptance limits.
- Final whole-client median/p95, GPU time, server p95 <50 ms, and no sustained
  MHLib packet growth remain mandatory when a runtime candidate exists.

Reproduce with `gradlew.bat g1Benchmark`,
then validate/promote with `tools/g1_benchmark_gate.py`.
