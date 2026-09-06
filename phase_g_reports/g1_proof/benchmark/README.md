# Phase G1 repeatable performance benchmark

Status: **SMOKE_ONLY / COMPONENT_PROXY_ONLY / PENDING_LIVE_PRECUTOVER**

This is the G1 headless renderer-component proxy. It measures the concrete
classic `EntityModel` and candidate `GeoRenderer` CPU vertex paths. It does
not a Q6 pass. The Tier-1, GPU, MSPT, and MHLib packet scenes remain a
binding pre-cutover gate for the first runtime-integrated conversion slice.

- Captured: 2026-09-06T10:50:42.071026900Z
- OS: Windows 11 10.0 (amd64)
- CPU: Intel64 Family 6 Model 198 Stepping 2, GenuineIntel (24 logical processors)
- JVM: Microsoft 21.0.7 / OpenJDK 64-Bit Server VM
- JVM flags: ['-Dfile.encoding=UTF-8', '-Duser.country=US', '-Duser.language=en', '-Duser.variant']
- Repository base revision: `887379056c16fbd87acb8aa857f003f62e366a3b` (working content bound by source/input hashes).
- Warmup/runs: 1s; 2 x 2s per scene
- Seed: N/A — no randomized world exists in the headless component proxy
- Resolution: N/A — no window, raster target, or GPU submission
- Camera: N/A — the proxy submits no camera or view transform
- Fixed state: fixed bind/static or Beaver full-amplitude quarter-cycle pose
- Timing order: paired AB/BA alternation per measured batch; smoke run 1 starts classic/candidate and run 2 starts candidate/classic

## elevator_100_visible

- Scope: renderer vertex submission only; no window, GPU, client tick, or server.
- Classic median/p95: 0.044995000 / 0.061295000 ms.
- Candidate median/p95: 0.110985000 / 0.151630000 ms.
- Candidate 1% low: 5202.372 FPS (component-only inverse p99).
- WARNING — component median ratio delta: 146.66074008223137%; absolute p95 delta: 0.090334999999999985 ms.
- Allocation classic/candidate: 91480.000 / 215480.000 bytes per frame.
- Model-bone instances: 500; MHLib parts: 0.

## beaver_100_visible

- Scope: renderer vertex submission only; no window, GPU, client tick, or server.
- Classic median/p95: 0.133105000 / 0.227775000 ms.
- Candidate median/p95: 0.246840000 / 0.399235000 ms.
- Candidate 1% low: 2083.681 FPS (component-only inverse p99).
- WARNING — component median ratio delta: 85.447578979001548%; absolute p95 delta: 0.17146 ms.
- Allocation classic/candidate: 171480.000 / 394680.000 bytes per frame.
- Model-bone instances: 900; MHLib parts: 0.

## mixed_100_visible

- Scope: renderer vertex submission only; no window, GPU, client tick, or server.
- Classic median/p95: 0.093315000 / 0.180605000 ms.
- Candidate median/p95: 0.180995000 / 0.389150000 ms.
- Candidate 1% low: 1314.112 FPS (component-only inverse p99).
- WARNING — component median ratio delta: 93.961313829502217%; absolute p95 delta: 0.20854499999999998 ms.
- Allocation classic/candidate: 132680.000 / 305080.000 bytes per frame.
- Model-bone instances: 700; MHLib parts: 0.

## mixed_100_rotation_state_only

- Scope: rotation-state iteration only; not offscreen rendering, culling, or controller work.
- Classic median/p95: 0.001144500 / 0.002278500 ms.
- Candidate median/p95: 0.002345000 / 0.003936000 ms.
- Candidate 1% low: 204081.633 FPS (component-only inverse p99).
- WARNING — component median ratio delta: 104.89296636085625%; absolute p95 delta: 0.0016575000000000001 ms.
- Allocation classic/candidate: 280.000 / 280.000 bytes per frame.
- Model-bone instances: 700; MHLib parts: 0.

## Q6 status and exact mixed-100 warning numbers

- WARNING — mixed-100 component median ratio delta: 93.961313829502217%.
- WARNING — mixed-100 absolute component p95 delta: 0.20854499999999998 ms.
- These component warnings must not be compared with or substituted for Q6's
  whole-client ≤10% median and ≤2 ms p95 acceptance limits.
- Final whole-client median/p95, GPU time, server p95 <50 ms, and no sustained
  MHLib packet growth remain mandatory when a runtime candidate exists.

Reproduce with `gradlew.bat g1Benchmark`,
then validate/promote with `tools/g1_benchmark_gate.py`.
