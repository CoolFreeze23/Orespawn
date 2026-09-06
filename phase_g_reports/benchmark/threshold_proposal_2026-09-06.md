<!-- ADOPTED (owner, 2026-09-06, item 16): R1–R7 as proposed — bytes lead R1, ns informational within the ±40 % band; the
mixed scene is added before the first Tier-2 cutover; the live scenes are the owner's and off the critical path until a
classic / candidate pair exists. The text below is the proposal as presented. -->

# Threshold proposal — the spawn-100 benchmark (Phase G slice (d), 2026-09-06; revised after refuter B)

**Status: PROPOSED, NOT ADOPTED.** A threshold is a ruling. Nothing below is wired into any gate;
`tools/g1_benchmark_gate.py` and `tools/g1_performance_benchmark.json` are untouched by this slice. The
owner adopts, amends, or rejects; the harness only produces the rows the rule would read.

## 1. The rule shape (from `phase_g_reports/morehitboxes_evaluation.md` §5 and the protocol's budgets)

For a change to MHLib (a harvest, a mixin change, a collector change) or to a renderer (classic → candidate):

| # | Metric | Rule | Where it is read |
|---|---|---|---|
| R1 | the collector's per-Queen cost (scene A) | **bytes per walk must not rise** (the headless companion's `queen_collect_yawpi.median_bytes_per_walk`, the primary signal); **ns per walk is informational** — reported, compared within a ≈ ±40 % band, never the sole ground of a verdict; the live `counters.per_entity_per_second["client.collector_ns"]` and `["client.collector_alloc_bytes"]` (per Queen in the client level) are the in-game confirmation | `collector_bench.json` (bytes, ns, the last-five median); the live report |
| R2 | `net.c2s_bone_bytes` per Queen per second (scene A) | must not rise | `counters.per_entity_per_second["net.c2s_bone_bytes"]` (the master client's own count, encoded payload bytes, per Queen in the client level) |
| R3 | scene E allocation rate | candidate's render-thread MB/s ≤ classic's | `client.render_thread_alloc_mb_per_second`, candidate report vs the paired classic report (`pairing.render_thread_alloc_ratio_candidate_over_classic` ≤ 1.0) |
| R4 | mixed-scene median frame time | ≤ +10.0 % candidate over classic | the protocol's `max_mixed_100_median_frame_regression_percent` (already in `live_acceptance_protocol.budgets`); read from `pairing.frame_median_regression_percent` of the mixed scene when the owner runs one (the harness offers A–F; a mixed scene is a follow-up) |
| R5 | p95 frame time | ≤ +2.0 ms candidate over classic | the protocol's `max_mixed_100_p95_frame_regression_ms`; `pairing.frame_p95_delta_ms` |
| R6 | server p95 MSPT | ≤ 50.0 ms in every scene | the protocol's `max_server_p95_ms`; `server.mspt_p95_ms` |
| R7 | MHLib packet growth | no sustained growth in `net.s2c_update_packets` + `net.set_master_packets` per second across the run | the protocol's `sustained_mhlib_packet_growth_allowed: false`; compare the first and last quarter of the run (the harness reports the run total; a per-quarter split is a follow-up if the owner wants it enforced) |

Why bytes lead R1 and ns follows: across eight invocations of the yaw-0 companion walk on record (the first
lane 9,181 / 7,403; refuter B 7,542 / 6,435 / 7,116; the fix lane 6,563 / 6,726 / 6,829 ns per walk) the ns
spread is max/min ≈ 1.43 — a band of roughly **±40 %** — while the bytes per walk were 16,904 in every run; the
yaw-π walk's bytes were 32,864 in all thirty runs of three invocations. The bytes count what the collector
allocates per walk, deterministic per rig; the ns is the JIT's and the machine's. A rule on ns needs the
owner's band (§5, point 2); a rule on bytes needs only the caveat in §3.

R2 exact (the byte count is deterministic per payload shape). Per-entity live figures divide by what ran
(`coverage.ticking` for the server counters, `coverage.in_client_level` for the client counters — §2).

## 2. What is measured where

- Live scenes A–F: `/orespawn bench scene <A-F> [count] [idle|wander]`, `start <seconds>`, `report [label]`
  (see `owner_runbook.md`). Reports land in `phase_g_reports/benchmark/live/<scene>_<label>_<timestamp>.json`
  and `.md`; a classic/candidate pair of the same scene gets a `pairing` section with R3–R6's inputs. Each
  report carries a `coverage` object — `spawned`, `ticking` (mobs in entity-ticking chunks at the end of the
  run), `in_client_level` (benchmark entities in the client level), `max_distance_blocks`, a `warning` when
  either count falls short — and its per-entity figures divide by those counts, not by the scene size. The
  scenes are wedges: every mob within 35° of the look axis and under 180 blocks (the default simulation
  distance is 192, the Queen's tracking range 256); a hundred Queens stand at a 12-block pitch 40–148 blocks
  out, the farthest 159.8. Any metric the run could not define is JSON `null`.
- Headless companion: `QueenPartPlacementProbe --bench <runs> <the_queen.geo.json> <the_queen.json>
  <beaver.geo.json> <outDir>` → `collector_bench.json` (the collector alone, no GeckoLib render; the HEAD/TAIL
  hooks through the real `MixinGeoRenderer._mhlib_callLayers` loop). What it does NOT measure, both needing a
  live entity: `tryAddBoneInformation` and the trust-client apply per synched bone (`getPartByName` +
  `MHLibPartEntity.applyInformation`; `the_queen.json` is `trust-client: true`) — the companion measures the
  collector's walk and fold only; and the inactive rig's real `isBoneCollectionActive()` chain (a
  `GeoReplacedEntityRenderer` and an entity), which makes `beaver_tax_inactive` a FLOOR of the in-game hook tax.

## 3. First baseline row — today's headless numbers (HEAD a03c0c5, JDK 21.0.7 Microsoft, this laptop)

`QueenPartPlacementProbe --bench 10` (an adaptive warm-up of at least 20 runs until the per-run ns plateaus,
then 10 measured runs of 1,000 walks; the median run and the median of the last five; three invocations).

**The baseline row: `queen_collect_yawpi`** — the body-yaw term at −π (a Queen facing the camera, as the scenes
spawn them), so `foldBodyYaw` builds its seven 3×3 matrices per synched bone as in-game:

| invocation | warm-up runs | ns / walk (median) | ns / walk (last five) | B / walk (all 10 runs) | ns / bone | B / bone | ns runs |
|---|---|---|---|---|---|---|---|
| 1 | 20 | 11,003.3 | 11,027.5 | 32,864.0 | 100.0 | 298.8 | 11101.9, 10359.8, 9022.1, 10976.0, 11494.2, 10979.1, 12093.5, 9807.1, 11027.5, 11494.5 |
| 2 | 20 | 11,800.5 | 11,702.7 | 32,864.0 | 107.3 | 298.8 | 11829.9, 12383.7, 12506.2, 10636.3, 11874.8, 12786.4, 11702.7, 11572.7, 11556.8, 11771.0 |
| 3 | 29 | 12,002.1 | 12,076.4 | 32,864.0 | 109.1 | 298.8 | 11927.9, 14714.9, 11360.1, 10639.3, 12321.1, 13666.3, 12076.4, 11476.9, 11468.9, 12556.6 |

The yaw-0 variant (the fold's early-out returns the input: no matrices), kept for comparison — the first lane's
baseline:

| invocation | warm-up runs | ns / walk (median) | ns / walk (last five) | B / walk | ns / bone | B / bone | ns runs |
|---|---|---|---|---|---|---|---|
| fix 1 | 23 | 6,563.4 | 5,992.6 | 16,904.0 | 59.7 | 153.7 | 6896.9, 7118.3, 7862.9, 6690.2, 6714.1, 5992.6, 5300.3, 6436.6, 5603.6, 6364.0 |
| fix 2 | 23 | 6,725.9 | 6,795.2 | 16,904.0 | 61.1 | 153.7 | 6617.2, 6917.1, 6554.1, 7374.8, 6517.6, 7343.6, 6656.5, 7234.9, 6506.1, 6795.2 |
| fix 3 | 26 | 6,829.2 | 7,375.1 | 16,904.0 | 62.1 | 153.7 | 6966.8, 6130.8, 6594.8, 6390.3, 7308.0, 6691.6, 6515.0, 7900.7, 7375.1, 7834.0 |
| first lane 1 (3 warm-up runs, 5 measured) | 3 | 9,181.3 | — | 16,904.0 | 83.5 | 153.7 | 9350.8, 9181.3, 8875.0, 11013.9, 9046.7 |
| first lane 2 | 3 | 7,403.3 | — | 16,904.0 | 67.3 | 153.7 | 6751.8, 6892.4, 7403.3, 8485.9, 8173.1 |
| refuter B 1 / 2 / 3 | 3 | 7,542.4 / 6,434.8 / 7,116.0 | — | 16,904.0 | 68.6 / 58.5 / 64.7 | 153.7 | (bench\refuterB\run1..3) |

The Beaver rigs (9 bones, no synched bone):

| rig | invocation | warm-up runs | ns / walk (median) | ns / walk (last five) | B / walk | ns / bone | B / bone |
|---|---|---|---|---|---|---|---|
| `beaver_tax_inactive` — a FLOOR of the hook tax | 1 / 2 / 3 | 24 / 24 / 20 | 403.2 / 322.4 / 309.1 | 358.2 / 313.1 / 318.5 | 0.0 | 44.8 / 35.8 / 34.3 | 0.0 |
| `beaver_tax_active` | 1 / 2 / 3 | 26 / 30 / 21 | 485.2 / 393.3 / 389.8 | 377.7 / 384.9 / 404.8 | 1,120.0 | 53.9 / 43.7 / 43.3 | 124.4 |

Reading: the collector's own cost for one Queen frame-walk today, facing the camera, is **≈ 11–12 µs and
32,864 bytes** (110 bones: 3 world-position reads + 1 `Vec3` per bone, the push/pop's two `Vector3d` copies and
a `Tuple` per bone, and for each of the ten synched bones the fold's seven 3×3 `double[][]` plus the shipped
vectors — 1,596 B per synched bone over the yaw-0 walk, of which the matrices and the `Vec3` account for
1,104 by reading; the rest is measured, not explained). At yaw 0 the same walk is ≈ 6–7 µs and 16,904 bytes.
A **replaced-renderer non-multipart entity (the Beaver candidate) pays zero bytes and at least ≈ 35–45 ns per
bone** for MHLib's HEAD/TAIL hooks through the mixin's layer loop (a floor: `isBoneCollectionActive()` is a
constant false here, the real chain needs a client renderer and a live entity); a GeoEntity-path
non-multipart entity would pay ≈ 124 B/bone (the push/pop copies) — no shipped OreSpawn species is on that
path today (the Queen is the only GeoEntity and it collects).

The caveat on the bytes signal: the companion stores every per-bone result into a static sink so it escapes as
in-game (`tryAddBoneInformation` / `applyInformation` keep the vectors); without that, one invocation with a
33-run warm-up saw C2's escape analysis remove the walk's 130 `Vec3`s (16,904 → 11,704 B). The fold's own
temporaries (the matrices) die inside `foldBodyYaw` in-game too, so a future JIT could scalar-replace them and
make the number DROP without a collector change — a "must not rise" rule tolerates a drop; a RISE always means
more allocation per walk. The ns runs still show the JIT's long tail (the warm-up lists in the JSON): a rule on
ns needs the owner's band; a rule on bytes does not.

## 4. Fields the owner's live runs fill

Per scene, classic and candidate (E), or the harvest's before/after by `git_head` (A–D): `client.frame_median_ms`,
`frame_p95_ms`, `one_percent_low_fps`, `render_thread_alloc_mb_per_second`, `server.mspt_p95_ms`,
`counters.per_entity_per_second[client.collector_ns | client.collector_alloc_bytes | net.c2s_bone_bytes |
server.placement_ns]` (divided by `coverage.in_client_level` / `coverage.ticking`), `network.s2c_bytes_per_second`,
the `coverage` object (a warning there means the run under-covered the scene: raise the simulation or render
distance and rerun), `working_tree` (commit first: `git_head` must name the measured code), and the
`controls.owner_fields` (machine, GPU and driver, camera path, world seed, notes). Five runs per scene per the
protocol; the harness writes one report per run and pairs the newest classic with the newest candidate — the
median of five is the owner's aggregation, as the protocol says (`runs_per_scene: 5`).

Conventions: every percentile (p95, p99) is NEAREST-RANK — `sorted[ceil(p·N) − 1]`, the 99th smallest of a hundred, so a
single slow frame in a hundred sits above p99 (it is the max) — and `one_percent_low_fps` is 1000 / p99(frame ms), the frame
rate at the p99 frame time, not the worst frame and not the average of the slowest 1 % (refuter A, A6). The alternative
"exclusive" reading (`sorted[floor(p·N)]`) is not used; the slice (d) gate corrected a row that had assumed it (2026-09-06).

## 5. Open for the ruling

1. Adopt R1–R7 as written, or amend the metric each reads (R1 in particular: bytes primary, ns informational).
2. The ns noise band for R1 (headless; ≈ ±40 % on the record) and the run-to-run band for R4/R5 (live, same
   machine, five runs).
3. Whether scene B gets its own rule. Note that TheQueen is `noCulling` and answers `shouldRenderAtSqrDistance`
   with true, so scene B is NOT a frustum-culling control for the Queen: it measures loaded-but-unseen Queens
   the renderer draws anyway (the collector runs per frame); a true culling control needs a species the
   frustum culls.
4. Whether the harness's `wander` state is ever a rule input (it is offered for looks; the protocol's fixed
   state is `idle`).
5. §5's derived `server.part_setpos` (Queen 10 / spider 16 per tick) undercounted `updateLastPos`: the measured
   values are 20 / 24 per tick from a part's second tick on, and 30 / 32 on its spawn tick (`MHLibPartEntity.tick`'s
   first-tick lerp snap to the zero interp target — OPT-030, drafted; pinned by `BenchHarnessTests` rows 11–12 at the
   slice (d) gate, 2026-09-06). Whether the expectation table in `morehitboxes_evaluation.md` §5 is corrected is a
   records matter for the orchestrator.
6. The Queen's 12-block pitch (below its 22-block box: a hundred Queens at 24 blocks cannot fit inside the
   tracking and simulation ranges; overlapping Queens are each drawn in full) — accept, or rule a smaller
   count at a wider pitch (the wedge holds 28 Queens at a 24-block pitch under 180 blocks).
7. Whether the trust-client apply per synched bone (not measurable headlessly) needs its own live counter, or
   whether `client.apply_information` (one of the nine) plus `client.collector_ns` covers it.
