# Phase G1 repeatable performance benchmark

Status: **SMOKE_ONLY / COMPONENT_PROXY_ONLY / PENDING_LIVE_PRECUTOVER**

This is the G1 headless renderer-component proxy. It measures the concrete
classic `EntityModel` and candidate `GeoRenderer` CPU vertex paths. It does
not a Q6 pass. The Tier-1, GPU, MSPT, and MHLib packet scenes remain a
binding pre-cutover gate for the first runtime-integrated conversion slice.

- Captured: 2026-09-13T03:53:06.485754900Z
- OS: Windows 11 10.0 (amd64)
- CPU: Intel64 Family 6 Model 198 Stepping 2, GenuineIntel (24 logical processors)
- JVM: Microsoft 21.0.7 / OpenJDK 64-Bit Server VM
- JVM flags: ['-Dfile.encoding=UTF-8', '-Duser.country=US', '-Duser.language=en', '-Duser.variant']
- Repository base revision: `9c20d3e9662f2ef5a7dfffcb6b9d1cd17ba4407c` (working content bound by source/input hashes).
- Warmup/runs: 1s; 2 x 2s per scene
- Seed: N/A — no randomized world exists in the headless component proxy
- Resolution: N/A — no window, raster target, or GPU submission
- Camera: N/A — the proxy submits no camera or view transform
- Fixed state: fixed bind/static or Beaver full-amplitude quarter-cycle pose
- Timing order: paired AB/BA alternation per measured batch; smoke run 1 starts classic/candidate and run 2 starts candidate/classic

## elevator_100_visible

- Scope: renderer vertex submission only; no window, GPU, client tick, or server.
- Classic median/p95: 0.049265000 / 0.070755000 ms.
- Candidate median/p95: 0.119000000 / 0.172785000 ms.
- Candidate 1% low: 4017.516 FPS (component-only inverse p99).
- WARNING — component median ratio delta: 141.55079671166138%; absolute p95 delta: 0.10203 ms.
- Allocation classic/candidate: 91480.000 / 215480.000 bytes per frame.
- Model-bone instances: 500; MHLib parts: 0.

## beaver_100_visible

- Scope: renderer vertex submission only; no window, GPU, client tick, or server.
- Classic median/p95: 0.127165000 / 0.177510000 ms.
- Candidate median/p95: 0.239665000 / 0.325355000 ms.
- Candidate 1% low: 2732.315 FPS (component-only inverse p99).
- WARNING — component median ratio delta: 88.467738764597186%; absolute p95 delta: 0.147845 ms.
- Allocation classic/candidate: 171480.000 / 394680.000 bytes per frame.
- Model-bone instances: 900; MHLib parts: 0.

## mixed_100_visible

- Scope: renderer vertex submission only; no window, GPU, client tick, or server.
- Classic median/p95: 0.093285000 / 0.133095000 ms.
- Candidate median/p95: 0.176440000 / 0.248080000 ms.
- Candidate 1% low: 3275.413 FPS (component-only inverse p99).
- WARNING — component median ratio delta: 89.140805059763068%; absolute p95 delta: 0.11498500000000003 ms.
- Allocation classic/candidate: 131480.000 / 305080.000 bytes per frame.
- Model-bone instances: 700; MHLib parts: 0.

## mixed_100_rotation_state_only

- Scope: rotation-state iteration only; not offscreen rendering, culling, or controller work.
- Classic median/p95: 0.001186500 / 0.001505000 ms.
- Candidate median/p95: 0.002347500 / 0.003020500 ms.
- Candidate 1% low: 297707.651 FPS (component-only inverse p99).
- WARNING — component median ratio delta: 97.850821744627027%; absolute p95 delta: 0.0015154999999999999 ms.
- Allocation classic/candidate: 280.000 / 280.000 bytes per frame.
- Model-bone instances: 700; MHLib parts: 0.

## Q6 status and exact mixed-100 warning numbers

- WARNING — mixed-100 component median ratio delta: 89.140805059763068%.
- WARNING — mixed-100 absolute component p95 delta: 0.11498500000000003 ms.
- These component warnings must not be compared with or substituted for Q6's
  whole-client ≤10% median and ≤2 ms p95 acceptance limits.
- Final whole-client median/p95, GPU time, server p95 <50 ms, and no sustained
  MHLib packet growth remain mandatory when a runtime candidate exists.

Reproduce with `gradlew.bat g1Benchmark`,
then validate/promote with `tools/g1_benchmark_gate.py`.
