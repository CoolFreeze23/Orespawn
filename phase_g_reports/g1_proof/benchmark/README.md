# Phase G1 repeatable performance benchmark

Status: **SMOKE_ONLY / COMPONENT_PROXY_ONLY / PENDING_LIVE_PRECUTOVER**

This is the G1 headless renderer-component proxy. It measures the concrete
classic `EntityModel` and candidate `GeoRenderer` CPU vertex paths. It does
not a Q6 pass. The Tier-1, GPU, MSPT, and MHLib packet scenes remain a
binding pre-cutover gate for the first runtime-integrated conversion slice.

- Captured: 2026-09-06T08:51:50.199156600Z
- OS: Windows 11 10.0 (amd64)
- CPU: Intel64 Family 6 Model 198 Stepping 2, GenuineIntel (24 logical processors)
- JVM: Microsoft 21.0.7 / OpenJDK 64-Bit Server VM
- JVM flags: ['-Dfile.encoding=UTF-8', '-Duser.country=US', '-Duser.language=en', '-Duser.variant']
- Repository base revision: `6391434b8b2a86f568b283ef41d4299baf6fe42e` (working content bound by source/input hashes).
- Warmup/runs: 1s; 2 x 2s per scene
- Seed: N/A — no randomized world exists in the headless component proxy
- Resolution: N/A — no window, raster target, or GPU submission
- Camera: N/A — the proxy submits no camera or view transform
- Fixed state: fixed bind/static or Beaver full-amplitude quarter-cycle pose
- Timing order: paired AB/BA alternation per measured batch; smoke run 1 starts classic/candidate and run 2 starts candidate/classic

## elevator_100_visible

- Scope: renderer vertex submission only; no window, GPU, client tick, or server.
- Classic median/p95: 0.041455000 / 0.056530000 ms.
- Candidate median/p95: 0.101990000 / 0.178485000 ms.
- Candidate 1% low: 2889.422 FPS (component-only inverse p99).
- WARNING — component median ratio delta: 146.02581112049205%; absolute p95 delta: 0.12195500000000001 ms.
- Allocation classic/candidate: 91480.000 / 215480.000 bytes per frame.
- Model-bone instances: 500; MHLib parts: 0.

## beaver_100_visible

- Scope: renderer vertex submission only; no window, GPU, client tick, or server.
- Classic median/p95: 0.109000000 / 0.162450000 ms.
- Candidate median/p95: 0.204445000 / 0.307890000 ms.
- Candidate 1% low: 2652.590 FPS (component-only inverse p99).
- WARNING — component median ratio delta: 87.564220183486214%; absolute p95 delta: 0.14544000000000001 ms.
- Allocation classic/candidate: 171480.000 / 394680.000 bytes per frame.
- Model-bone instances: 900; MHLib parts: 0.

## mixed_100_visible

- Scope: renderer vertex submission only; no window, GPU, client tick, or server.
- Classic median/p95: 0.079225000 / 0.115620000 ms.
- Candidate median/p95: 0.151540000 / 0.210740000 ms.
- Candidate 1% low: 2902.421 FPS (component-only inverse p99).
- WARNING — component median ratio delta: 91.278005680025288%; absolute p95 delta: 0.09512000000000001 ms.
- Allocation classic/candidate: 131480.000 / 305080.000 bytes per frame.
- Model-bone instances: 700; MHLib parts: 0.

## mixed_100_rotation_state_only

- Scope: rotation-state iteration only; not offscreen rendering, culling, or controller work.
- Classic median/p95: 0.001078500 / 0.001535000 ms.
- Candidate median/p95: 0.002185000 / 0.003202000 ms.
- Candidate 1% low: 218197.687 FPS (component-only inverse p99).
- WARNING — component median ratio delta: 102.59619842373668%; absolute p95 delta: 0.0016670000000000001 ms.
- Allocation classic/candidate: 280.000 / 280.000 bytes per frame.
- Model-bone instances: 700; MHLib parts: 0.

## Q6 status and exact mixed-100 warning numbers

- WARNING — mixed-100 component median ratio delta: 91.278005680025288%.
- WARNING — mixed-100 absolute component p95 delta: 0.09512000000000001 ms.
- These component warnings must not be compared with or substituted for Q6's
  whole-client ≤10% median and ≤2 ms p95 acceptance limits.
- Final whole-client median/p95, GPU time, server p95 <50 ms, and no sustained
  MHLib packet growth remain mandatory when a runtime candidate exists.

Reproduce with `gradlew.bat g1Benchmark`,
then validate/promote with `tools/g1_benchmark_gate.py`.
