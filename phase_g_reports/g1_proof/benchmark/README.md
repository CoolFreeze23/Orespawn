# Phase G1 repeatable performance benchmark

Status: **SMOKE_ONLY / COMPONENT_PROXY_ONLY / PENDING_LIVE_PRECUTOVER**

This is the G1 headless renderer-component proxy. It measures the concrete
classic `EntityModel` and candidate `GeoRenderer` CPU vertex paths. It does
not a Q6 pass. The Tier-1, GPU, MSPT, and MHLib packet scenes remain a
binding pre-cutover gate for the first runtime-integrated conversion slice.

- Captured: 2026-09-02T14:34:24.548857400Z
- OS: Windows 11 10.0 (amd64)
- CPU: Intel64 Family 6 Model 198 Stepping 2, GenuineIntel (24 logical processors)
- JVM: Microsoft 21.0.7 / OpenJDK 64-Bit Server VM
- JVM flags: ['-Dfile.encoding=UTF-8', '-Duser.country=US', '-Duser.language=en', '-Duser.variant']
- Repository base revision: `6b10b145c8860dcf1e34bd44a9ebeafb024995a9` (working content bound by source/input hashes).
- Warmup/runs: 1s; 2 x 2s per scene
- Seed: N/A — no randomized world exists in the headless component proxy
- Resolution: N/A — no window, raster target, or GPU submission
- Camera: N/A — the proxy submits no camera or view transform
- Fixed state: fixed bind/static or Beaver full-amplitude quarter-cycle pose
- Timing order: paired AB/BA alternation per measured batch; smoke run 1 starts classic/candidate and run 2 starts candidate/classic

## elevator_100_visible

- Scope: renderer vertex submission only; no window, GPU, client tick, or server.
- Classic median/p95: 0.056970000 / 0.098480000 ms.
- Candidate median/p95: 0.159365000 / 0.262705000 ms.
- Candidate 1% low: 2662.336 FPS (component-only inverse p99).
- WARNING — component median ratio delta: 179.73494821836056%; absolute p95 delta: 0.16422500000000001 ms.
- Allocation classic/candidate: 93880.000 / 346680.000 bytes per frame.
- Model-bone instances: 500; MHLib parts: 0.

## beaver_100_visible

- Scope: renderer vertex submission only; no window, GPU, client tick, or server.
- Classic median/p95: 0.112935000 / 0.145460000 ms.
- Candidate median/p95: 0.230630000 / 0.287550000 ms.
- Candidate 1% low: 2781.989 FPS (component-only inverse p99).
- WARNING — component median ratio delta: 104.21481383096469%; absolute p95 delta: 0.14208999999999997 ms.
- Allocation classic/candidate: 173880.000 / 394680.000 bytes per frame.
- Model-bone instances: 900; MHLib parts: 0.

## mixed_100_visible

- Scope: renderer vertex submission only; no window, GPU, client tick, or server.
- Classic median/p95: 0.091520000 / 0.114160000 ms.
- Candidate median/p95: 0.175475000 / 0.220300000 ms.
- Candidate 1% low: 3662.668 FPS (component-only inverse p99).
- WARNING — component median ratio delta: 91.734047202797186%; absolute p95 delta: 0.10613999999999998 ms.
- Allocation classic/candidate: 133880.000 / 305080.000 bytes per frame.
- Model-bone instances: 700; MHLib parts: 0.

## mixed_100_rotation_state_only

- Scope: rotation-state iteration only; not offscreen rendering, culling, or controller work.
- Classic median/p95: 0.000988000 / 0.001527500 ms.
- Candidate median/p95: 0.002055000 / 0.003225000 ms.
- Candidate 1% low: 279446.696 FPS (component-only inverse p99).
- WARNING — component median ratio delta: 107.99595141700399%; absolute p95 delta: 0.0016975 ms.
- Allocation classic/candidate: 280.000 / 280.000 bytes per frame.
- Model-bone instances: 700; MHLib parts: 0.

## Q6 status and exact mixed-100 warning numbers

- WARNING — mixed-100 component median ratio delta: 91.734047202797186%.
- WARNING — mixed-100 absolute component p95 delta: 0.10613999999999998 ms.
- These component warnings must not be compared with or substituted for Q6's
  whole-client ≤10% median and ≤2 ms p95 acceptance limits.
- Final whole-client median/p95, GPU time, server p95 <50 ms, and no sustained
  MHLib packet growth remain mandatory when a runtime candidate exists.

Reproduce with `gradlew.bat g1Benchmark`,
then validate/promote with `tools/g1_benchmark_gate.py`.
