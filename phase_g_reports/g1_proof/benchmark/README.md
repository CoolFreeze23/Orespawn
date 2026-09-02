# Phase G1 repeatable performance benchmark

Status: **SMOKE_ONLY / COMPONENT_PROXY_ONLY / PENDING_LIVE_PRECUTOVER**

This is the G1 headless renderer-component proxy. It measures the concrete
classic `EntityModel` and candidate `GeoRenderer` CPU vertex paths. It does
not a Q6 pass. The Tier-1, GPU, MSPT, and MHLib packet scenes remain a
binding pre-cutover gate for the first runtime-integrated conversion slice.

- Captured: 2026-09-02T14:17:48.587173900Z
- OS: Windows 11 10.0 (amd64)
- CPU: Intel64 Family 6 Model 198 Stepping 2, GenuineIntel (24 logical processors)
- JVM: Microsoft 21.0.7 / OpenJDK 64-Bit Server VM
- JVM flags: ['-Dfile.encoding=UTF-8', '-Duser.country=US', '-Duser.language=en', '-Duser.variant']
- Repository base revision: `67c4764715c504fc5f4d4ba0a3594363744a1756` (working content bound by source/input hashes).
- Warmup/runs: 1s; 2 x 2s per scene
- Seed: N/A — no randomized world exists in the headless component proxy
- Resolution: N/A — no window, raster target, or GPU submission
- Camera: N/A — the proxy submits no camera or view transform
- Fixed state: fixed bind/static or Beaver full-amplitude quarter-cycle pose
- Timing order: paired AB/BA alternation per measured batch; smoke run 1 starts classic/candidate and run 2 starts candidate/classic

## elevator_100_visible

- Scope: renderer vertex submission only; no window, GPU, client tick, or server.
- Classic median/p95: 0.052510000 / 0.078760000 ms.
- Candidate median/p95: 0.154240000 / 0.224410000 ms.
- Candidate 1% low: 3465.424 FPS (component-only inverse p99).
- WARNING — component median ratio delta: 193.73452675680821%; absolute p95 delta: 0.14565 ms.
- Allocation classic/candidate: 93880.000 / 346680.000 bytes per frame.
- Model-bone instances: 500; MHLib parts: 0.

## beaver_100_visible

- Scope: renderer vertex submission only; no window, GPU, client tick, or server.
- Classic median/p95: 0.120700000 / 0.165600000 ms.
- Candidate median/p95: 0.225760000 / 0.310610000 ms.
- Candidate 1% low: 2869.687 FPS (component-only inverse p99).
- WARNING — component median ratio delta: 87.042253521126781%; absolute p95 delta: 0.14501000000000006 ms.
- Allocation classic/candidate: 176280.000 / 394680.000 bytes per frame.
- Model-bone instances: 900; MHLib parts: 0.

## mixed_100_visible

- Scope: renderer vertex submission only; no window, GPU, client tick, or server.
- Classic median/p95: 0.086065000 / 0.117585000 ms.
- Candidate median/p95: 0.152205000 / 0.206345000 ms.
- Candidate 1% low: 4326.569 FPS (component-only inverse p99).
- WARNING — component median ratio delta: 76.848893278336107%; absolute p95 delta: 0.088760000000000006 ms.
- Allocation classic/candidate: 135080.000 / 305080.000 bytes per frame.
- Model-bone instances: 700; MHLib parts: 0.

## mixed_100_rotation_state_only

- Scope: rotation-state iteration only; not offscreen rendering, culling, or controller work.
- Classic median/p95: 0.001084000 / 0.001387500 ms.
- Candidate median/p95: 0.002310500 / 0.002960500 ms.
- Candidate 1% low: 312842.171 FPS (component-only inverse p99).
- WARNING — component median ratio delta: 113.1457564575646%; absolute p95 delta: 0.001573 ms.
- Allocation classic/candidate: 280.000 / 280.000 bytes per frame.
- Model-bone instances: 700; MHLib parts: 0.

## Q6 status and exact mixed-100 warning numbers

- WARNING — mixed-100 component median ratio delta: 76.848893278336107%.
- WARNING — mixed-100 absolute component p95 delta: 0.088760000000000006 ms.
- These component warnings must not be compared with or substituted for Q6's
  whole-client ≤10% median and ≤2 ms p95 acceptance limits.
- Final whole-client median/p95, GPU time, server p95 <50 ms, and no sustained
  MHLib packet growth remain mandatory when a runtime candidate exists.

Reproduce with `gradlew.bat g1Benchmark`,
then validate/promote with `tools/g1_benchmark_gate.py`.
