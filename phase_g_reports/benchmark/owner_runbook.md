# Owner runbook — the spawn-100 live scenes (Phase G slice (d), 2026-09-06; revised after refuter B)

The harness runs inside your client (singleplayer: the integrated server and the client share one JVM, so one
report carries both halves). A dedicated server can run the server half alone (no frame metrics).

## 1. Launch

JVM flags (the dev run configuration, or the launcher's JVM arguments):

```
-Dorespawn.dev.bench=true      registers /orespawn bench (op level 2) and the client frame timer
-Dmhlib.counters=true          turns the MHLib counters on (both dumps, every 100 ticks; the reports need them)
-Dorespawn.dev.geckolibRenderers=beaver     ONLY for the candidate runs of scene E (classic runs: omit)
```

Same-machine controls (the protocol's list — keep them identical across classic and candidate, and across the
five runs): machine and GPU, JDK and JVM flags, resolution and fullscreen state, **render distance (12 chunks
or more)** and **simulation distance (12 chunks or more)**, graphics settings, camera path, world seed. The
scenes are wedges that keep every mob under 180 blocks (the farthest of a hundred Queens stands 159.8 blocks
out — 10 chunks): with the simulation distance at 12 every mob stands in an entity-ticking chunk (192 blocks),
and with the render distance at 12 every Queen is tracked (her 256-block tracking range is capped by the view
distance). The report's `coverage` object tells you whether that held (§3). Set **VSync off and Max Framerate
Unlimited** so the frame interval measures work rather than a cap. Know that under `-Dmhlib.counters=true` the
byte accounting re-encodes every bone packet on the client tick (the render thread) and every multipart
broadcast on the server thread — the real encode runs on the netty thread — so frame time, render-thread
allocation, GC and MSPT carry that extra encode; it is the same under both labels and outside
`client.collector_ns` / `server.placement_ns` (refuter A). Creative mode (mobs do not target creative players;
the Queens and robots stand no-AI anyway). PlayNicely off in the config (scene A is "hostile size"; the report's
`part_count` and `count_spawned` tell you what you got). **Commit before a run**: the report records
`git_head` and a `working_tree` reading (from the index's stat data — it sees modified tracked files, not
untracked files or staged changes), and only a clean, committed tree makes `git_head` name the measured code.

## 2. One run

1. Stand where the camera will be, look along the direction the scene should extend (scene B spawns behind you).
   A flat world is easiest: the spawner puts every mob on the heightmap top, and a no-AI mob stays exactly where
   it is put.
2. `/orespawn bench scene A` (default count 100; `scene A 50` for fewer; `scene C 100 idle`). The reply says how
   many spawned, how many MHLib parts exist and how far the farthest mob stands. The layout is a wedge: rows
   along your look direction from the scene's base distance at its pitch, each row's columns within 35° of the
   axis (half the default 70° FOV), every mob under 180 blocks, rows filled in order and each row from the
   centre out. Scene sizes: A/B Queens at a 12-block pitch from 40 blocks out (a hundred fill ten rows, 40 to 148
   blocks; the Queen's 22-block boxes overlap on purpose — she is noPhysics, and every overlapping Queen is still
   drawn in full, which is the cost measured; the wedge holds 132 Queens and the command refuses more), C/D robots
   at a 6-block pitch from 8 blocks (a hundred reach 74 blocks), E/F at a 2-block pitch from 6 blocks (24 blocks).
   Scene B is the same wedge behind you — NOTE the Queen is noCulling and is drawn off-screen too, so B measures
   loaded-but-unseen Queens drawn anyway, not frustum culling.
3. Wait the protocol's warm-up: **60 s** (let the JIT and the chunk/entity loading settle; `/orespawn bench
   status` shows the session).
4. `/orespawn bench start 120` — samples 120 s on both halves. The chat reply confirms the client sampler is
   available and the counters are enabled; if either says NOT / DISABLED, fix the flags first.
5. When the log says `bench: run finished` (it also says how many mobs stood in entity-ticking chunks; a
   WARNING there means the simulation distance was too short — rerun) and `bench client: run finished`,
   `/orespawn bench report`. The reply names the two files written:
   `phase_g_reports/benchmark/live/<scene>_<classic|candidate>_<timestamp>.json` and `.md`, warns again when the
   coverage fell short, and notes a DIRTY working tree. (The report command stats every tracked file for the
   working-tree reading: well under a second.)
   The label comes from the dev switch's state for the scene's species; pass your own for harvest
   before/afters: `/orespawn bench report before6`, `/orespawn bench report after6` (pairing by git HEAD then,
   not automatic).
6. `/orespawn bench stop` despawns the scene (`scene` again also despawns the previous one).
7. Repeat 2–6 five times per scene (the protocol's `runs_per_scene: 5`), then the candidate side (relaunch with
   the dev switch for scene E; for A–D a harvest's build), five times again.

## 3. Where the numbers are

- `.md` — the tables: run, server MSPT (median/p95/p99), client frames (median/p95/p99, 1 % low FPS, average
  FPS, render-thread and process CPU, render-thread and JVM allocation MB/s, GC), the MHLib counters per second
  and per entity per second, network bytes per direction, the controls (JDK, flags, resolution, options, GPU),
  the dev switch per landed species, and — when a report of the same scene under the other label exists — the
  pairing (candidate relative to classic). The header's coverage line: `spawned` (what the scene command added),
  `ticking` (of those, the mobs alive in entity-ticking chunks at the end of the run — the divisor of every
  per-entity SERVER figure), `in the client level` (the benchmark-tagged entities the client held at the end —
  the divisor of every per-entity CLIENT figure), the farthest mob's distance, and a WARNING when either count
  is below `spawned` (raise the simulation / render distance and rerun; the per-entity figures already divide by
  what ran, so they stay honest, but the scene was smaller than you asked).
- `.json` — the same, for tools: the `coverage` object (`spawned`, `ticking`, `in_client_level`,
  `max_distance_blocks`, `warning`, `note`), `working_tree`, and `counters.per_entity_divisors`; a metric the run
  could not define is `null`, never `NaN`; `controls.owner_fields` is yours to fill (machine, GPU and driver,
  camera path, world seed, notes).
- The log also carries the raw dumps: `MHLib counters (per 100 ticks): ...` (client) and
  `MHLib counters (server, per 100 ticks): ...` (server).

## 4. Reading the counters

Per Queen per second at 20 ticks: `server.align_synched_parts` 200, `server.part_setpos` 400 (20 per tick:
`updateLastPos` + the alignment; a part's FIRST tick carries one more — 30 per Queen, 32 per modern robot on its spawn tick, the
first-tick lerp snap of OPT-030 — so a scene's first tick after the spawn adds 10 × Queens / 8 × robots once), `net.c2s_bone_packets` ≈ 20 while animating (fewer once the change-only
stream settles), `net.s2c_update_packets` ≈ 6.7 per Queen while its parts move (`ServerEntity.sendChanges` reaches
`sendDirtyEntityData` only every `updateInterval` ticks — THE_QUEEN's builder leaves the default 3 — or on impulse or
dirty synched data; a standing no-AI Queen has no impulse), counted per broadcast call for every tracked entity whether
or not a player tracks it (an upper bound; `coverage.in_client_level` is the honest single-player count); then the
linger, then silence.
Per modern robot per second: `server.align_sub_parts_parts` 160, `server.part_setpos` 480 (24 per tick from the second tick
on; 32 on the spawn tick),
`server.placement_ns` the gait feed plus the static alignment. `client.collector_ns` is the whole GeckoLib render of
each multipart entity between MHLib's Pre and Post hooks — divide by `client.frames` for the per-draw cost; the
headless `collector_bench.json` is the collector alone (its `queen_collect_yawpi` row is the baseline; it does not
include the trust-client apply per synched bone, which needs a live entity).

## 5. What the harness cannot do for you

GPU frame time (no query in this build), the mixed scene (a follow-up if ruled), pairing by anything other than
the two labels, the five-run median (one report per run; the median is yours), a frustum-culling control for the
Queen (she is noCulling — scene B measures her drawn off-screen), and a working-tree reading beyond the index's
stat data (commit before you run).
