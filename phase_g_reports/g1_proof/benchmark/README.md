# Phase G1 repeatable performance benchmark

Status: **SMOKE_ONLY / COMPONENT_PROXY_ONLY / PENDING_LIVE_PRECUTOVER**

This is the G1 headless renderer-component proxy. It measures the concrete
classic `EntityModel` and candidate `GeoRenderer` CPU vertex paths. It does
not a Q6 pass. The Tier-1, GPU, MSPT, and MHLib packet scenes remain a
binding pre-cutover gate for the first runtime-integrated conversion slice.

- Captured: 2026-11-08T01:11:06.197708700Z
- OS: Windows 11 10.0 (amd64)
- CPU: Intel64 Family 6 Model 198 Stepping 2, GenuineIntel (24 logical processors)
- JVM: Eclipse Adoptium 21.0.12.1 / OpenJDK 64-Bit Server VM
- JVM flags: ['-Dfile.encoding=UTF-8', '-Duser.country=US', '-Duser.language=en', '-Duser.variant']
- Repository base revision: `5587b1bc93c8d6606bfb44bcb0bf51288353fdde` (working content bound by source/input hashes).
- Warmup/runs: 1s; 2 x 2s per scene
- Seed: N/A — no randomized world exists in the headless component proxy
- Resolution: N/A — no window, raster target, or GPU submission
- Camera: N/A — the proxy submits no camera or view transform
- Fixed state: fixed bind/static or Beaver full-amplitude quarter-cycle pose
- Timing order: paired AB/BA alternation per measured batch; smoke run 1 starts classic/candidate and run 2 starts candidate/classic

## elevator_100_visible

- Scope: renderer vertex submission only; no window, GPU, client tick, or server.
- Classic median/p95: 0.049155000 / 0.064045000 ms.
- Candidate median/p95: 0.150140000 / 0.197805000 ms.
- Candidate 1% low: 4340.937 FPS (component-only inverse p99).
- WARNING — component median ratio delta: 205.44196928084634%; absolute p95 delta: 0.13376000000000002 ms.
- Allocation classic/candidate: 93880.000 / 346680.000 bytes per frame.
- Model-bone instances: 500; MHLib parts: 0.

## beaver_100_visible

- Scope: renderer vertex submission only; no window, GPU, client tick, or server.
- Classic median/p95: 0.134510000 / 0.169570000 ms.
- Candidate median/p95: 0.250820000 / 0.309640000 ms.
- Candidate 1% low: 2900.316 FPS (component-only inverse p99).
- WARNING — component median ratio delta: 86.469407478997852%; absolute p95 delta: 0.14007000000000003 ms.
- Allocation classic/candidate: 171480.000 / 394680.000 bytes per frame.
- Model-bone instances: 900; MHLib parts: 0.

## mixed_100_visible

- Scope: renderer vertex submission only; no window, GPU, client tick, or server.
- Classic median/p95: 0.082385000 / 0.112380000 ms.
- Candidate median/p95: 0.158560000 / 0.213375000 ms.
- Candidate 1% low: 4014.452 FPS (component-only inverse p99).
- WARNING — component median ratio delta: 92.462220064332072%; absolute p95 delta: 0.100995 ms.
- Allocation classic/candidate: 132680.000 / 305080.000 bytes per frame.
- Model-bone instances: 700; MHLib parts: 0.

## mixed_100_rotation_state_only

- Scope: rotation-state iteration only; not offscreen rendering, culling, or controller work.
- Classic median/p95: 0.001157000 / 0.001550000 ms.
- Candidate median/p95: 0.002403000 / 0.003171000 ms.
- Candidate 1% low: 277315.585 FPS (component-only inverse p99).
- WARNING — component median ratio delta: 107.69230769230766%; absolute p95 delta: 0.001621 ms.
- Allocation classic/candidate: 280.000 / 280.000 bytes per frame.
- Model-bone instances: 700; MHLib parts: 0.

## Q6 status and exact mixed-100 warning numbers

- WARNING — mixed-100 component median ratio delta: 92.462220064332072%.
- WARNING — mixed-100 absolute component p95 delta: 0.100995 ms.
- These component warnings must not be compared with or substituted for Q6's
  whole-client ≤10% median and ≤2 ms p95 acceptance limits.
- Final whole-client median/p95, GPU time, server p95 <50 ms, and no sustained
  MHLib packet growth remain mandatory when a runtime candidate exists.

Reproduce with `gradlew.bat g1Benchmark`,
then validate/promote with `tools/g1_benchmark_gate.py`.
