# Phase G1 repeatable performance benchmark

Status: **SMOKE_ONLY / COMPONENT_PROXY_ONLY / PENDING_LIVE_PRECUTOVER**

This is the G1 headless renderer-component proxy. It measures the concrete
classic `EntityModel` and candidate `GeoRenderer` CPU vertex paths. It does
not a Q6 pass. The Tier-1, GPU, MSPT, and MHLib packet scenes remain a
binding pre-cutover gate for the first runtime-integrated conversion slice.

- Captured: 2026-09-12T18:31:35.004624100Z
- OS: Windows 11 10.0 (amd64)
- CPU: Intel64 Family 6 Model 198 Stepping 2, GenuineIntel (24 logical processors)
- JVM: Microsoft 21.0.7 / OpenJDK 64-Bit Server VM
- JVM flags: ['-Dfile.encoding=UTF-8', '-Duser.country=US', '-Duser.language=en', '-Duser.variant']
- Repository base revision: `5904681ac43ccc8840131b9af0238bdafa75877f` (working content bound by source/input hashes).
- Warmup/runs: 1s; 2 x 2s per scene
- Seed: N/A — no randomized world exists in the headless component proxy
- Resolution: N/A — no window, raster target, or GPU submission
- Camera: N/A — the proxy submits no camera or view transform
- Fixed state: fixed bind/static or Beaver full-amplitude quarter-cycle pose
- Timing order: paired AB/BA alternation per measured batch; smoke run 1 starts classic/candidate and run 2 starts candidate/classic

## elevator_100_visible

- Scope: renderer vertex submission only; no window, GPU, client tick, or server.
- Classic median/p95: 0.048510000 / 0.070035000 ms.
- Candidate median/p95: 0.120100000 / 0.174975000 ms.
- Candidate 1% low: 4507.042 FPS (component-only inverse p99).
- WARNING — component median ratio delta: 147.57781900639046%; absolute p95 delta: 0.10493999999999999 ms.
- Allocation classic/candidate: 91480.000 / 215480.000 bytes per frame.
- Model-bone instances: 500; MHLib parts: 0.

## beaver_100_visible

- Scope: renderer vertex submission only; no window, GPU, client tick, or server.
- Classic median/p95: 0.119705000 / 0.173235000 ms.
- Candidate median/p95: 0.222830000 / 0.311765000 ms.
- Candidate 1% low: 2550.923 FPS (component-only inverse p99).
- WARNING — component median ratio delta: 86.149283655653463%; absolute p95 delta: 0.13852999999999996 ms.
- Allocation classic/candidate: 171480.000 / 394680.000 bytes per frame.
- Model-bone instances: 900; MHLib parts: 0.

## mixed_100_visible

- Scope: renderer vertex submission only; no window, GPU, client tick, or server.
- Classic median/p95: 0.088215000 / 0.124495000 ms.
- Candidate median/p95: 0.168190000 / 0.238520000 ms.
- Candidate 1% low: 3298.099 FPS (component-only inverse p99).
- WARNING — component median ratio delta: 90.659184945870891%; absolute p95 delta: 0.11402500000000002 ms.
- Allocation classic/candidate: 131480.000 / 305080.000 bytes per frame.
- Model-bone instances: 700; MHLib parts: 0.

## mixed_100_rotation_state_only

- Scope: rotation-state iteration only; not offscreen rendering, culling, or controller work.
- Classic median/p95: 0.001117500 / 0.001579500 ms.
- Candidate median/p95: 0.002268500 / 0.003145000 ms.
- Candidate 1% low: 262226.301 FPS (component-only inverse p99).
- WARNING — component median ratio delta: 102.99776286353466%; absolute p95 delta: 0.0015655000000000003 ms.
- Allocation classic/candidate: 280.000 / 280.000 bytes per frame.
- Model-bone instances: 700; MHLib parts: 0.

## Q6 status and exact mixed-100 warning numbers

- WARNING — mixed-100 component median ratio delta: 90.659184945870891%.
- WARNING — mixed-100 absolute component p95 delta: 0.11402500000000002 ms.
- These component warnings must not be compared with or substituted for Q6's
  whole-client ≤10% median and ≤2 ms p95 acceptance limits.
- Final whole-client median/p95, GPU time, server p95 <50 ms, and no sustained
  MHLib packet growth remain mandatory when a runtime candidate exists.

Reproduce with `gradlew.bat g1Benchmark`,
then validate/promote with `tools/g1_benchmark_gate.py`.
