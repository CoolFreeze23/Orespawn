# Phase G1 repeatable performance benchmark

Status: **SMOKE_ONLY / COMPONENT_PROXY_ONLY / PENDING_LIVE_PRECUTOVER**

This is the G1 headless renderer-component proxy. It measures the concrete
classic `EntityModel` and candidate `GeoRenderer` CPU vertex paths. It does
not a Q6 pass. The Tier-1, GPU, MSPT, and MHLib packet scenes remain a
binding pre-cutover gate for the first runtime-integrated conversion slice.

- Captured: 2026-09-13T02:48:35.552181200Z
- OS: Windows 11 10.0 (amd64)
- CPU: Intel64 Family 6 Model 198 Stepping 2, GenuineIntel (24 logical processors)
- JVM: Microsoft 21.0.7 / OpenJDK 64-Bit Server VM
- JVM flags: ['-Dfile.encoding=UTF-8', '-Duser.country=US', '-Duser.language=en', '-Duser.variant']
- Repository base revision: `3ad64a17494a17fa32ce78adfbaf9a22a123f695` (working content bound by source/input hashes).
- Warmup/runs: 1s; 2 x 2s per scene
- Seed: N/A — no randomized world exists in the headless component proxy
- Resolution: N/A — no window, raster target, or GPU submission
- Camera: N/A — the proxy submits no camera or view transform
- Fixed state: fixed bind/static or Beaver full-amplitude quarter-cycle pose
- Timing order: paired AB/BA alternation per measured batch; smoke run 1 starts classic/candidate and run 2 starts candidate/classic

## elevator_100_visible

- Scope: renderer vertex submission only; no window, GPU, client tick, or server.
- Classic median/p95: 0.062575000 / 0.079710000 ms.
- Candidate median/p95: 0.193465000 / 0.257715000 ms.
- Candidate 1% low: 2858.164 FPS (component-only inverse p99).
- WARNING — component median ratio delta: 209.17299240910913%; absolute p95 delta: 0.17800500000000002 ms.
- Allocation classic/candidate: 91480.000 / 346680.000 bytes per frame.
- Model-bone instances: 500; MHLib parts: 0.

## beaver_100_visible

- Scope: renderer vertex submission only; no window, GPU, client tick, or server.
- Classic median/p95: 0.179380000 / 0.211020000 ms.
- Candidate median/p95: 0.329805000 / 0.387720000 ms.
- Candidate 1% low: 1888.235 FPS (component-only inverse p99).
- WARNING — component median ratio delta: 83.858289664399635%; absolute p95 delta: 0.17670000000000002 ms.
- Allocation classic/candidate: 171480.000 / 394680.000 bytes per frame.
- Model-bone instances: 900; MHLib parts: 0.

## mixed_100_visible

- Scope: renderer vertex submission only; no window, GPU, client tick, or server.
- Classic median/p95: 0.122400000 / 0.145225000 ms.
- Candidate median/p95: 0.228325000 / 0.268605000 ms.
- Candidate 1% low: 2649.989 FPS (component-only inverse p99).
- WARNING — component median ratio delta: 86.540032679738573%; absolute p95 delta: 0.12337999999999999 ms.
- Allocation classic/candidate: 131480.000 / 305080.000 bytes per frame.
- Model-bone instances: 700; MHLib parts: 0.

## mixed_100_rotation_state_only

- Scope: rotation-state iteration only; not offscreen rendering, culling, or controller work.
- Classic median/p95: 0.001578000 / 0.001887500 ms.
- Candidate median/p95: 0.003106000 / 0.003553000 ms.
- Candidate 1% low: 250218.942 FPS (component-only inverse p99).
- WARNING — component median ratio delta: 96.831432192648933%; absolute p95 delta: 0.0016655000000000003 ms.
- Allocation classic/candidate: 280.000 / 280.000 bytes per frame.
- Model-bone instances: 700; MHLib parts: 0.

## Q6 status and exact mixed-100 warning numbers

- WARNING — mixed-100 component median ratio delta: 86.540032679738573%.
- WARNING — mixed-100 absolute component p95 delta: 0.12337999999999999 ms.
- These component warnings must not be compared with or substituted for Q6's
  whole-client ≤10% median and ≤2 ms p95 acceptance limits.
- Final whole-client median/p95, GPU time, server p95 <50 ms, and no sustained
  MHLib packet growth remain mandatory when a runtime candidate exists.

Reproduce with `gradlew.bat g1Benchmark`,
then validate/promote with `tools/g1_benchmark_gate.py`.
