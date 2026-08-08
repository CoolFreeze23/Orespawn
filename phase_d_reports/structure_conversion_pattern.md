# Structure Conversion Pattern — 1.7.10 hardcoded builders → NeoForge 1.21.1

The playbook for porting the remaining ~22 WGEN-042 structures in slice D6.
Written from the D5 reference conversions (BasiliskMaze WGEN-037, Nightmare
Rookery, Challenge Tower reconciliation). Follow it mechanically; every
deviation from it in D5 is called out with WHY so D6 doesn't have to re-derive
the reasoning.

Paths: `orig:` = `reference_1_7_10_source/sources/danger/orespawn/`,
`port:` = `src/main/java/danger/orespawn/`,
`RES:` = `src/main/resources/data/orespawn/`.

---

## 0. The mechanism decision — and why it is almost always the same one

Modern 1.21.1 offers four ways to place a structure. For OreSpawn originals
the choice collapses almost every time:

| Mechanism | When to use | OreSpawn reality |
|---|---|---|
| **NBT template / jigsaw** | Static geometry, identical every instance | **Never.** Every 1.7.10 OreSpawn builder is an imperative loop with `world.rand` calls baked into the geometry (random heights, trap positions, chest counts, mob picks). A template cannot express any of them. |
| **`Feature` (configured/placed)** | Small, chunk-local scatter (plants, ore, single blocks) | Only for flora/ore-ish placements (anthills, veggie patches — already done in C7). A Feature runs inside the ~24-block `WorldGenLevel` write window; anything wider gets sheared at chunk borders. |
| **Chunk-generator code hook** | Same-chunk boxes ≤ ~16 blocks, one dimension | Only the generic/ruby dungeon boxes (12×12/10×10) use this (`port:world/OreSpawnChunkGenerator.placeDungeons`). Do NOT add new structures here — no `/locate`, no save/reload persistence of partial generation, and the write window limits size. |
| **`Structure` + `StructurePiece` (LegacyDungeonStructure pipeline)** | Everything else | **The default.** Multi-chunk footprint, deterministic per-chunk replay, `/locate` support, structure-set frequency control. All D5/D6 structures go this way. |

**Decision rule for D6: use `LegacyDungeonStructure` + a per-structure
generator class unless the original is a ≤12-block same-chunk box (none of
the remaining 22 are).**

Precedent chain: Royal Trees (Phase 13C-fix) → Audit Part 2-4 dungeons →
royal altars → D5 BasiliskMaze/Nightmare Rookery. The one-time failure mode
this pipeline exists to prevent: the pre-audit port ran big builders inside a
single write window and either sheared them at chunk borders or shrank them
("procedural fabrication" — see `port:world/structure/ModStructureTypes.java`
Javadoc).

---

## 1. Step-by-step conversion recipe

### Step 1 — Extraction spec first, code second

Write `phase_d_reports/d5_extraction/<structure>_spec.md` in the established
format (exemplars: `basilisk_maze_spec.md`, `nightmare_spec.md`,
`enormous_castle_spec.md`): entry points, per-loop geometry tables with
inclusive coordinate ranges, FULL loot-list transcription with weights and
port item mappings, block palette table, mob table, footprint extents,
worldgen call context (dimension, odds, ground scan), DungeonSpawnerBlock
type number, surprises/MISSING-IN-PORT section. Cite every number as
file:line. THEN implement from the original source with the spec as the map.

Dead-code check before porting anything: grep the whole original tree for the
class/method name. `NightmareDungeon` (WGEN-038) was never instantiated in
1.7.10 — porting it as generating content would have INVENTED behavior. The
audit's structure list contains at least that one trap; verify reachability
for every D6 entry (the `OreSpawnWorld` dispatch tables and
`DungeonSpawnerBlock.java:52-202` are the two reachability roots, plus
direct item/block triggers).

### Step 2 — DungeonType enum entry

Add one entry to `port:world/structure/LegacyDungeonPiece.DungeonType` with:

- **Box extents** from the spec's footprint table (+1 margin). Use the
  6-arg asymmetric constructor when the algorithm's reach differs per side
  (BasiliskMaze: X −8..+64) — do not inflate a symmetric box to cover an
  asymmetric footprint; every covered chunk pays a full generation replay.
  For unbounded random walks use the theoretical bound (Rookery: 52-step
  ±1 walk → ±52 ±1 bulge = ±54 with margin), not the "typical" extent.
- **PlacementMode** (see step 4).

### Step 3 — Generator class, one per structure

`port:world/structure/<Name>Generator.java`, package-private final class,
one static `generate(LegacyDungeonPiece piece, BlockPos origin, RandomSource
random)` — see `BasiliskMazeGenerator` (large, multi-method) and
`NightmareRookeryGenerator` (small, single-method) for the two size shapes.
Add the dispatch case in `LegacyDungeonPiece.postProcess`.

Write through the piece's gated helpers only:

| Helper | Ports | Notes |
|---|---|---|
| `piece.place(x,y,z,state)` | `setBlockFast` / `FastSetBlock` | UPDATE_CLIENTS flag, no neighbor updates — floating torches etc. survive exactly like the original's flag-2 writes |
| `piece.placeSpawner(x,y,z,type)` | spawner block + `func_98272_a(name)` | mob-name → EntityType mapping goes in the spec's mob table |
| `piece.placeLootChest(x,y,z,lootKey)` | chest + `WeightedRandomChestContent.func_76293_a` | loot lives in JSON (step 5) |
| `piece.spawnPersistent(type,x,y,z,yaw)` | `spawnCreature` + `func_110163_bv()` | yaw drawn by the CALLER (see RNG contract) |

**THE RNG STITCHING CONTRACT (the one rule that, broken, produces bugs no
compiler catches):** the piece replays the entire generator once per
intersecting chunk with a deterministic RandomSource seeded from the piece
bounding box. Every pass must consume the identical draw sequence, so:

1. Draw randomness UNCONDITIONALLY; gate only the writes. (The helpers gate
   internally — never wrap a `random.nextInt` in your own `inChunk` check.
   That is why `spawnPersistent` takes yaw as a parameter.)
2. Never branch on world state (`level.getBlockState`) inside a generator —
   neighbor chunks' pending writes are invisible, so passes would diverge.
   If the original reads blocks it just wrote (BasiliskMaze's `openMaze`
   entrance probe), model that state in memory (the maze wall bitmap) and
   probe the model.
3. Loot draw counts move into the JSON (no in-code draws), which keeps the
   stream shorter and identical in every pass.

RNG mapping deltas to document per structure (PARITY-note style, in the
slice report): the original used `world.rand` (and BasiliskMaze's topology
used raw unseeded `Math.random()`); the port's deterministic per-position
seed changes no distribution but makes layouts seed-stable. The
DungeonSpawnerBlock path (`buildNow`) keeps the original's live-RNG behavior.

### Step 4 — Placement (frequency + anchoring)

Two halves: **how often** (structure set JSON) and **where exactly**
(PlacementMode in `findGenerationPoint`).

**Frequency:** convert the original per-chunk odds to `random_spread`
spacing ≈ √(1/odds), separation = spacing/2 — the C7-approved equivalence
(WGEN-039). Known dispatch tables:

- Mining rotation (orig `OreSpawnWorld.java:79-101`):
  `recently_placed==0 && nextInt(95)==1` then `nextInt(7)` → 1/665 each →
  spacing 26/13. (BasiliskMaze = i==0; the other six are D6 candidates:
  Kyuubi dungeon, beehive [done], shadow dungeon [done], AlienWTF [done],
  EnderKnight, LeonNest [done].)
- Islands "D4" roll (orig `OreSpawnWorld.java:132-178`):
  `recently_placed==0 && nextInt(100)==0` then `i=nextInt(19)` → each
  single-outcome i is 1/1900 → spacing 44/22. Rookery = i==14. The full
  i→structure table is in `enormous_castle_spec.md` — D6 reuses it for
  every remaining Islands structure.
- The shared `recently_placed` 50-chunk global cooldown and the
  `D4BigSpaceCheck`-style air probes map onto structure-set
  separation — approximation approved in C7, do not try to reproduce the
  cross-structure coupling.

Unique salt per set — take the next free value (D5 used 84330/84331; check
`grep salt RES:worldgen/structure_set/*.json` first; note mantis_nest and
royal_trees currently collide on 84312, logged as a finding in D5).

**Anchoring:** if the original's `add*` method does a ground scan the
existing `PlacementMode`s don't cover, add a mode to
`LegacyDungeonPiece.DungeonType.PlacementMode` + a case in
`LegacyDungeonStructure.findGenerationPoint`. Available now:

- `SURFACE_CENTER` — chunk-center heightmap probe (pre-D5 types).
- `LOWEST_SURFACE_36` — BasiliskMaze's lowest-of-36-columns scan with the
  original's Y window and −2 sink.
- `ISLANDS_GRASS` — D4 grass-level anchor + `nextInt(8)` jitter + LessLag
  50% skip.

Structure starts resolve BEFORE terrain exists, so original block scans map
to `chunkGenerator.getBaseHeight(x, z, WORLD_SURFACE_WG, ...)`; the
predicted height is "first free block", so the original's "topmost solid
block" = `getBaseHeight − 1`. Keep the original's scan window as a hard
accept/reject (e.g. maze `lowestY > 40`, rookery grass 5..20) — outside it,
return empty and let the set try elsewhere.

Lava-seek placements (ruby-dungeon style, and any D6 structure anchored on
lava/specific blocks) cannot use `getBaseHeight`; those stay as
chunk-generator code hooks (existing precedent) or scan in `postProcess`'s
first pass — decide per structure and document.

**JSONs (copy an existing trio and rename):**
- `RES:worldgen/structure/<name>.json` — `"type": "orespawn:legacy_dungeon"`,
  `"dungeon_type": "<ENUM_NAME>"`, `"biomes": "#orespawn:has_structure/<name>"`,
  `"step": "surface_structures"` (or `underground_structures` for fully
  buried builds).
- `RES:worldgen/structure_set/<name>.json` — spacing/separation/salt as above.
- `RES:tags/worldgen/biome/has_structure/<name>.json` — the dimension's
  biome (from `RES:worldgen/biome/`): `orespawn:mining_biome`,
  `orespawn:island_biome`, `orespawn:utopia_plains`, `orespawn:village_biome`,
  `orespawn:chaos_biome`, `orespawn:crystal_plains`. Overworld structures use
  vanilla biome tags per the original's biome dispatch.

### Step 5 — Loot tables

One `RES:loot_table/chests/<name>.json` per `WeightedRandomChestContent[]`
list. Transcription rules (established Phase C, reaffirmed D5):

- `pools[0].rolls` = the original fill count: `N + nextInt(M)` →
  uniform `min N, max N+M−1`.
- One entry per original list element: same weight, `set_count` uniform
  min/max from the original's min/max stack args (omit for 1/1).
- Documented approximation (do NOT try to fix): the original placed each
  stack at a RANDOM chest slot, so later stacks could overwrite earlier
  ones; a loot pool never collides. Slightly higher average yield, same
  distribution per pull.
- Items with per-stack state use `minecraft:set_components` (D5 example:
  CagedGirlfriend → `orespawn:caged_mob` + `"orespawn:caged_entity":
  "orespawn:girlfriend"` — the original's dedicated item has no port
  equivalent; MISSING-IN-PORT noted in the spec).
- If an item genuinely cannot be expressed (registry-draw loot like
  TheKing's 150-random-items), keep a code-side fill — precedent B1 — but
  exhaust the JSON option first. None of the remaining 22 structures needs
  this (their lists are all static).

### Step 6 — DungeonSpawnerBlock outcome

Every structure has a type number in the original's `nextInt(50)` table
(orig `DungeonSpawnerBlock.java:52-202`). Add
`case TYPE_<NAME> -> LegacyDungeonPiece.buildNow(server, pos, DungeonType.<NAME>)`
in `port:block/entity/RandomDungeonSpawnerBlockEntity.buildForType`, keyed by
the ORIGINAL index, with the orig line citation. Some indices fire TWO
builders (e.g. type 24 = EnderDragonHospital + MonsterIsland) — port the
pair together, and read the original `if (type == N)` block IN FULL before
wiring (a truncated excerpt once made type 2 look like a two-builder index;
it is the King castle alone). `buildNow` handles the rest (unclipped write
window, live RNG).

### Step 7 — Verification checklist (per structure)

1. `./gradlew build` green (the slice gate is the FULL build, not
   compileJava).
2. Number-by-number re-diff of the generator against the original file —
   every loop bound, coordinate offset, block type, weight; fix the spec if
   it disagrees with the source (the source wins).
3. RNG contract audit: search the generator for `random.` — every call
   reachable identically in all passes? No `level.get*` reads?
4. Loot audit: entry count == original list length; total weight == spec's
   total; rolls == original fill formula.
5. Ledger: resolution lines for the finding IDs; slice report row with
   citations; TESTING_CHECKLIST entries (a `/locate` line + an interior
   checklist line).

---

## 2. Worked examples (read these before starting a D6 structure)

- **`BasiliskMazeGenerator`** — the maximal case: asymmetric box, custom
  placement scan, in-memory state model replacing a read-after-write,
  procedural maze with list-mutation RNG, direct entity spawns, floating
  torches, loot with a data component.
- **`NightmareRookeryGenerator`** — the minimal case: ~90 lines, one loop
  ported twice with drift state carried across, spawner+chest caps.
- **Challenge Towers (`generateChallengeTower`)** — the reconciliation case:
  an existing port audited against the original, invented "QA fix" content
  removed per the standing Phase-10 ruling, loot palettes rebuilt from the
  original lists. If a D6 structure already has port code (check
  `DungeonType` first!), treat the original as the source of truth and diff
  the port against it line-by-line before assuming anything.

## 3. Known traps, in one place

1. RNG draw gated by chunk → maze/ridge differs per chunk → visible seams.
   (Contract in step 3.)
2. Reading world blocks inside a generator → pass divergence. Model it.
3. `Math.random()` in originals (BasiliskMaze topology) — port to the piece
   RandomSource; document the seed-stability delta.
4. Dead-code structures (NightmareDungeon) — verify reachability first.
5. Duplicate structure-set salts — always grep before picking.
6. Two-builders-per-DSB-type indices — port pairs together.
7. Non-flat originals' Y windows (maze >40, rookery 5..20) are behavior,
   not plumbing — losing them changes where structures appear.
8. `spawn_overrides: {}` — leave empty; OreSpawn mobs spawn by dimension
   lists (D1 architecture), not structure overrides.
9. Persistent entity spawns: `spawnPersistent` only — a naked
   `addFreshEntity` without `setPersistenceRequired` despawns the boss
   the original pinned (`func_110163_bv`).

---

## 4. Tree-generator addendum (D6a)

The `orig:Trees.java` builders are NOT structures in the section-1 sense, and
forcing them through the `LegacyDungeonStructure` pipeline (or any worldgen
`Feature`) is wrong for most of them. The mechanism decision for a tree is
made by ONE question: **what calls it in 1.7.10?** The full trigger-site table
is in `phase_d_reports/d6_extraction/trees_spec.md` section 1; the decision it
produces:

| Original call site | Port shape | D6 examples |
|---|---|---|
| A ticking block's `func_149674_a` (the tree grows/acts at play time) | Logic on the port block's `randomTick` — no Feature, no generator class, no structure JSON | DuplicatorTree (`port:block/BlockDuplicatorLog.randomTick`, orig BlockDuplicatorLog.java:32-39), ExperienceTree (`port:block/BlockExperiencePlant.randomTick`, orig BlockExperiencePlant.java:42-51) |
| An `OreSpawnWorld.add*` decoration call only | Decoration-phase direct builder (`port:world/CrystalStructures`-style) or a registered `Feature` with rarity filters (`port:world/feature/MagicAppleTreeFeature`-style) | WindTree/SkyTree (done pre-D6), FairyTree/FairyCastleTree worldgen path (orig OreSpawnWorld.java:1962-1996) |
| BOTH `OreSpawnWorld` and `DungeonSpawnerBlock` | ONE shared builder serving both paths (see the FairyTree worked example below) | FairyTree (DSB type 0), FairyCastleTree (DSB type 1) |
| Nothing (dead code) | Do not port as generating content — porting it would invent behavior (same rule as NightmareDungeon, section 1 step 1) | ScragglyTreeWithBranches (orig Trees.java:418-450, WGEN-046) |

**Do not "upgrade" a live-tick tree to worldgen.** ExperienceTree has zero
natural worldgen in 1.7.10 (the only caller in the whole original tree is
BlockExperiencePlant.java:50); registering a ConfiguredFeature for it would
invent content. Conversely, the DuplicatorTree's worldgen presence is ONLY the
single seed log placed by `addVeggies` (orig OreSpawnWorld.java:1915-1916 →
`port:world/feature/VeggiePatchFeature`); the tree itself then grows live, one
block per tick — building the finished tree during worldgen would skip the
original's multi-day growth and its duplication behavior entirely.

### Read legality: why the RNG stitching contract does NOT apply here

Section 1 step 3's "never read world state" rule exists because a
`LegacyDungeonPiece` generator REPLAYS per intersecting chunk against a
half-written world. Tree builders run in exactly two other regimes, and both
may read freely:

- **Live `ServerLevel` tick** (`randomTick`, DSB `buildForType`): the world is
  fully loaded and there is no replay — one call, one build. The
  DuplicatorTree is the extreme case: its ENTIRE algorithm is world reads
  (soil probe orig Trees.java:125, trunk-state probes :140-155, duplication
  source+meta :171-172); it re-derives "how grown am I" from the world every
  tick and performs at most one write. That is unportable to any worldgen
  mechanism by construction — the world IS its state model, and unlike the
  BasiliskMaze case you cannot move that state into memory, because the state
  must persist across ticks/saves and reflect player edits.
- **Decoration phase** (`applyBiomeDecoration`): neighbor chunks exist, so
  the original's air-check leaf drapes (`make_leaves` orig Trees.java:188,
  `make_crystal_leaves` :496) and ground scans (orig OreSpawnWorld.java:
  1968-1986) port verbatim. What remains forbidden is the noise/`ChunkAccess`
  build pass (`port:world/CrystalTreeGenerator` is that regime's one
  clamped-to-chunk exception) — never move a Trees.java builder there.

Corollary: tree builders write with plain `level.setBlock(pos, state, 2)`
(the original's `setBlockFast` flags=2), not the piece helpers — there is no
piece, no gating, and no per-chunk replay to protect. Draw randomness from
whatever `RandomSource` the trigger hands you (`randomTick`'s parameter, the
decoration random); there is no cross-pass determinism requirement.

One modern-paradigm trap that replaces the RNG contract in this regime:
flag-2 writes never run neighbor updates, so a modern `LeavesBlock` keeps its
default `DISTANCE=7` and decays on random ticks even beside its trunk. Port
adaptation: place tree leaves with `PERSISTENT=true`
(`port:block/BlockDuplicatorLog` apple leaves, `port:block/
BlockExperiencePlant.makeLeaves` experience leaves). Document it as an
adaptation — the original's metadata-driven decay had no equivalent hazard.

### Worked example: FairyTree/FairyCastleTree dual trigger

The original shares one `OreSpawnMain.OreSpawnTrees` instance between crystal
worldgen (orig OreSpawnWorld.java:1987-1991) and the player-placed Random
Dungeon Spawner (orig DungeonSpawnerBlock.java:53-58). The port mirrors that
sharing with one private builder per tree in `port:world/CrystalStructures`,
reached two ways:

1. **Worldgen**: `tryPlaceFairyTree` (decoration phase) reproduces the site
   scan — 1/5 chunk gate, y128→41 descending probe, 17×17 air clearance, 5×5
   crystal-grass footing, then the 4/5-vs-1/5 variant split with the
   original's asymmetric anchors (plain tree gets `posY-1`, castle gets
   `posY`, orig OreSpawnWorld.java:1987-1991).
2. **Play time**: `buildFairyTreeAt` / `buildFairyCastleTreeAt(ServerLevel,
   RandomSource, BlockPos)` — thin public wrappers called by
   `RandomDungeonSpawnerBlockEntity.buildForType` for DSB types 0/1, at the
   cleared spawner-block position with NO ground gate and NO y adjustment
   (orig DungeonSpawnerBlock.java:53-58 validates nothing — a floating or
   terrain-embedded tree is faithful behavior).

The builders themselves are typed on `WorldGenLevel`, which `ServerLevel`
satisfies — that one signature is what lets a single transcription serve both
regimes. Config-driven size reduction (`LessLag`) shrinks every
crystal-branch segment and the castle tier count AFTER the random draw (orig
Trees.java:529-589, :618-624 → `CrystalStructures.lessLagShrink`) — the draw
itself is never gated, so toggling the config never desynchronizes anything.

Return-value quirks are behavior: the original `addFairyTree` returns
`true` after passing its 1/5 gate even when the site scan fails (orig
OreSpawnWorld.java:1995), suppressing that chunk's termites and big
structures with no tree placed. The port briefly returned `false` there;
the D6a verification pass restored the original semantics (WGEN-062) —
when converting an `add*` method, port its FULL return contract, not just
its build path.
