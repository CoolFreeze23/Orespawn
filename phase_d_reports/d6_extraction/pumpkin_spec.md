# Giant Jack-o'-Lantern ("Pumpkin") — Extraction Spec (1.7.10 → NeoForge 1.21.1)

Source of truth: `reference_1_7_10_source/sources/danger/orespawn/GenericDungeon.java` —
`makePumpkin` (GD:6041-6182). Method read IN FULL. The next two methods,
`makeRoundRotator` (GD:6184-6258) and `makeRainbow` (GD:6260-…), are SEPARATE structures
(DSB types 45/46) — `makePumpkin` calls neither; there are NO helpers between it and
makeRainbow that belong to this builder. All coordinates are relative to the build origin
`(cposx, cposy, cposz)` = the three int args. Method-local constants: `width = 14`
(GD:6045, X), `depth = 12` (GD:6046, Z), `height = 14` (GD:6047, Y), `dark_green = 13`
(GD:6048), `orange = 1` (GD:6049).

Citation convention: `GD:NN` = GenericDungeon.java line NN, `OSW:NN` = OreSpawnWorld.java,
`OSM:NN` = OreSpawnMain.java, `DSB:NN` = DungeonSpawnerBlock.java, `RDS:NN` =
`src/main/java/danger/orespawn/block/entity/RandomDungeonSpawnerBlockEntity.java`,
`LDS:NN` = `src/main/java/danger/orespawn/world/structure/LegacyDungeonStructure.java`.

Shared plumbing:
- Every geometry write is `OreSpawnMain.setBlockFast(world, x, y, z, blk, meta, 2)`
  (GD:6070 etc.) — flag-2 chunk write, no neighbor updates → port
  `piece.place(x, y, z, state)` (structure_conversion_pattern.md §1 step 3 table). Note
  this builder passes a META (`which_color`) — the stained-clay color — unlike the
  meta-0 `FastSetBlock` wrapper most builders use.
- Spawners: `world.func_147465_d(..., field_150474_ac, 0, 2)` + `getSpawnerTileEntity`
  (GD:86-95) + `func_98272_a(name)` → `piece.placeSpawner(x, y, z, type)`.
- **No chests, no loot lists, no direct entity spawns, no mid-build world reads** other
  than the two spawner tile-entity self-fetches (§9) — and **zero RNG draws** inside the
  builder (§10). The ported generator is a pure constant block stamp.

---

## 1. Entry points — EVERY call site

Grep of the whole original tree for `makePumpkin`: exactly two call sites (OSW:2416,
DSB:186; GD:6041 is the definition; the pre-audit copy under `src/danger/orespawn/` is
not shipped code).

| Caller | Signature used | Coords passed | Notes |
|---|---|---|---|
| `OreSpawnWorld.addPumpkin` (OSW:2416) | `OreSpawnMain.MyDungeon.makePumpkin(world, posX, posY + 1, posZ)` | grass-scan hit **+1** — origin is the AIR block above grass | worldgen path, Islands D4 only (§1.2) |
| `DungeonSpawnerBlock` type **44** (DSB:185-187) | `...makePumpkin(world, clickedX, clickedY + 1, clickedZ)` | player-placed block pos **+1** | `type = world.rand.nextInt(50)` (DSB:52), fires 400 ticks after placement (DSB:39), self + block above deleted first (DSB:50-51). The `if (type == 44)` block read IN FULL: **single builder call, nothing else** (DSB:185-187) — not a two-builder index. Both DSB neighbors 43 (FrogPond) and 45 (RoundRotator) also use `clickedY + 1`; each is its own index. |

### 1.1 `addPumpkin` — FULL method + return contract (OSW:2407-2421)

```java
// OSW:2407-2421
public boolean addPumpkin(World world, Random random, int chunkX, int chunkZ) {
    if (OreSpawnMain.LessLag != 0 && random.nextInt(2) != 0) {
        return false;
    }
    int posX = chunkX + random.nextInt(8);
    int posZ = chunkZ + random.nextInt(8);
    for (int posY = 20; posY > 4; --posY) {
        Block bid = world.func_147439_a(posX, posY, posZ);
        if (bid != Blocks.field_150349_c) continue;
        OreSpawnMain.MyDungeon.makePumpkin(world, posX, posY + 1, posZ);
        recently_placed = 50;
        return true;
    }
    return false;
}
```

1. LessLag gate: config on → 1/2 skip (OSW:2408-2410), chunk-provided `random`.
2. ONE position attempt (no retry loop): `posX/posZ = chunk + random.nextInt(8)`
   (OSW:2411-2412) — 8-block jitter, not 16.
3. Column scan `posY = 20` down to `5` inclusive (`posY > 4`, OSW:2413): first
   **grass block** (`field_150349_c`, OSW:2414-2415).
4. Hit → `makePumpkin(world, posX, posY + 1, posZ)` — **anchored ONE ABOVE the grass**
   (OSW:2416) — then `recently_placed = 50`, `return true` (OSW:2417-2418).
5. Scan miss → `return false` (OSW:2420).

**FULL return contract: `boolean`, `true` ONLY on an actual placement** (which also sets
the 50-chunk global cooldown). LessLag skip and scan miss both return `false` with no
side effects. The call site (OSW:172) ignores the return value — no WGEN-062-style
early-true quirk, no chain coupling to port. **Unlike the large-footprint Islands D4
adds (Castle OSW:2212-2218, Greenhouse :2239-2245, Rookery :2262-2268, StinkyHouse
:2285-2291, WhiteHouse :2308-2314, EnderCastle :2331-2337, IncaPyramid :2354-2360,
RobotLab :2377-2383), addPumpkin performs NO air clearance probe** — just gate + grass
scan, the same probe-free shape as RubyDungeon (OSW:2171-2185), CephadromeAltar
(:2187-2201), Mini (:2391-2405), and GenericDungeon (:2438-2452) (§12 S4).

### 1.2 Worldgen dispatch chain (complete)

- `OreSpawnWorld.generate` — `world.field_73011_w.field_76574_g ==
  OreSpawnMain.DimensionID4` branch (OSW:132).
- Gate: `recently_placed == 0 && random.nextInt(100) == 0 &&
  this.D4BigSpaceCheck(world, chunkX*16, 7, chunkZ*16)` (OSW:134).
- Roll: `i = random.nextInt(19)` (OSW:135); **`i == 17` → `addPumpkin(world, random,
  chunkX*16, chunkZ*16)` (OSW:171-173)** — the full i→structure table is in
  `enormous_castle_spec.md` per the pattern doc §1 step 4.
- Effective odds: 1/100 × 1/19 = **1/1900 per Islands chunk** (single-outcome i), before
  the LessLag halving and the grass scan. The `D4BigSpaceCheck` air probe, the shared
  `recently_placed` cooldown, and the D4 gate coupling map onto structure-set
  spacing/separation (C7-approved, pattern §1 step 4).

---

## 2. Geometry — per-loop tables (all ranges inclusive)

Write order: hollow shell → face carvings (punch air through the z=0 wall) → stem →
candle pillar → netherrack cap → fire → 2 spawners. Later writes overwrite earlier ones.

### 2.1 Loop A — hollow orange shell, interior cleared (GD:6053-6073)

Triple loop `i = 0..13`, `j = 0..13`, `k = 0..11`. Per cell: default
`blk = air, which_color = 0` (GD:6056-6057); then THREE independent ifs, any of which
switches to stained clay meta `orange = 1`: `j == 0 || j == 13` (GD:6058-6061),
`i == 0 || i == 13` (GD:6062-6065), `k == 0 || k == 11` (GD:6066-6069). Write
`setBlockFast(..., blk, which_color, 2)` (GD:6070).

| Where | Cell (rel) | Block | Cite |
|---|---|---|---|
| any face of the box (`i∈{0,13}` or `j∈{0,13}` or `k∈{0,11}`) | shell | orange stained clay (meta 1) | GD:6058-6069 |
| interior `(1..12, 1..12, 1..10)` | 12×12×10 | air (clears terrain) | GD:6056-6057 |

A closed 14(X)×14(Y)×12(Z) orange box: floor at `j=0`, lid at `j=13`, four walls;
every interior cell explicitly written air.

### 2.2 Face carvings — air punched through the `z = 0` wall (GD:6074-6143)

All writes are `air, meta 0, flag 2` at `k = 0` — holes through the front (−z) wall.
Two mirrored halves: right half uses base `i = width/2 − 1 = 6` (GD:6074), left half
base `i = width/2 = 7` (GD:6109). Union per row (x = rel X, y = rel Y, z = 0):

| Feature | y | x cells | Cites |
|---|---|---|---|
| Right eye (3×3) | 9, 10, 11 | 9, 10, 11 | GD:6076-6087 (i+3..i+5, i=6) |
| Left eye (3×3) | 9, 10, 11 | 2, 3, 4 | GD:6111-6122 (i−5..i−3, i=7) |
| Nose, right half (2×2) | 7, 8 | 8, 9 | GD:6088-6093 (i+2..i+3) |
| Nose, left half (2×2) | 7, 8 | 4, 5 | GD:6123-6128 (i−3..i−2) |
| Mouth top notches | 4 | 3, 6, 7, 10 | GD:6094-6096 (i+1, i+4), GD:6129-6131 (i−1, i−4) |
| Mouth band | 3 | 3..10 (all 8) | GD:6097-6101, 6132-6136 |
| Mouth band | 2 | 3..10 (all 8) | GD:6102-6106, 6137-6141 |
| Mouth bottom fangs | 1 | 5, 8 | GD:6107-6108 (i+2, i=6), GD:6142-6143 (i−2, i=7) |

48 carved cells total (18 eyes + 8 nose + 22 mouth), every one a Loop-A shell cell —
the classic jack-o'-lantern face, jagged grin included. Nothing is carved on any other
wall.

### 2.3 Stem — diagonal green slab, 1 thick (GD:6144-6149)

`k = depth/2 − 1 = 5` (GD:6144). For `j = 0..3`, `i = 0..2`: write stained clay meta
`dark_green = 13` at `x = width/2 − i − j = 7 − i − j`, `y = height + j = 14 + j`,
`z = 5` (GD:6145-6148).

| j | y | x cells | Cite |
|---|---|---|---|
| 0 | 14 | 7, 6, 5 | GD:6147 |
| 1 | 15 | 6, 5, 4 | GD:6147 |
| 2 | 16 | 5, 4, 3 | GD:6147 |
| 3 | 17 | 4, 3, 2 | GD:6147 |

A 3-wide, 4-tall stem leaning one block −x per layer, sitting on the lid, exactly one
block thick in Z (`z = 5` only).

### 2.4 Candle pillar — oak planks 2×2×5 (GD:6150-6156)

For `j = 0..4`, `i = 0..1`, `k = 0..1`: oak planks (`field_150344_f`, meta 0) at
`x = width/2 + i − 1 = 6..7`, `y = j + 1 = 1..5`, `z = depth/2 + k − 1 = 5..6`
(GD:6153). Stands on the Loop-A floor (`j = 0` shell), centered in the interior.

### 2.5 Netherrack cap (GD:6157-6162)

`j = 5` → `y = 6`: netherrack (`field_150424_aL`) 2×2 at `x = 6..7`, `z = 5..6`
(GD:6160) — caps the planks pillar.

### 2.6 Fire — the candle flame (GD:6163-6167)

`j = 6, k = 0` → `y = 7`, `z = depth/2 + 0 − 1 = 5`: fire (`field_150480_ab`) at
`(6, 7, 5)` and `(7, 7, 5)` (GD:6165-6166) — the front pair of the netherrack top.
Fire on netherrack burns forever (infiniburn) — an eternal candle behind the carved
face. Flag-2 write, no update.

### 2.7 Spawners — 2 × "Ghost Pumpkin Skelly" (GD:6168-6181)

`j = 6, k = 1` → `y = 7`, `z = 6` (the back pair of the netherrack top):

| # | Position (rel) | Mob name string | Cite |
|---|---|---|---|
| 1 | `(6, 7, 6)` | `"Ghost Pumpkin Skelly"` | GD:6168-6175 |
| 2 | `(7, 7, 6)` | `"Ghost Pumpkin Skelly"` | GD:6176-6181 |

Each `func_147465_d(..., field_150474_ac, 0, 2)` + `func_98272_a` (GD:6171-6174,
6177-6180). The spawner pair stands shoulder-to-shoulder with the fire pair on the
same netherrack cap.

Net shape: a 14×14×12 hollow orange-terracotta jack-o'-lantern with a carved face on
the −z wall (two 3×3 eyes, split 2×2 nose, jagged 8-wide grin), a green diagonal stem
on top, and inside — an oak-planks candle capped with netherrack carrying two eternal
fires and two Ghost Pumpkin Skelly spawners. No chest, no loot.

---

## 3. Loot — FULL transcription

**None.** `makePumpkin` declares no chest, references no `WeightedRandomChestContent`
list, and writes no chest block anywhere in GD:6041-6182 (the `TileEntityMobSpawner`
local at GD:6052 is the only tile-entity variable). No loot table JSON is needed —
do not invent one (§12 S3).

---

## 4. Spawner / mob mapping table

| Spawner name | 1.7.10 class (registration) | Port EntityType (cite) |
|---|---|---|
| `"Ghost Pumpkin Skelly"` (×2, GD:6174/6180) | `GhostSkelly` — `registerGlobalEntityID(GhostSkelly.class, "Ghost Pumpkin Skelly", GhostSkellyID)` OSM:4055, `registerModEntity` OSM:4059 | `ModEntities.GHOST_SKELLY` "ghost_skelly" (ModEntities.java:555-557) |

No direct entity spawns — spawner blocks only (no yaw/NBT/persistence handling exists
in this builder). The port's `GhostSkelly` already carries the original's
"Ghost Pumpkin Skelly"-spawner proximity bypass (GhostSkelly.java:175 area ← orig
GhostSkelly.java:173-188), so spawner-spawned skellies behave as in 1.7.10.

---

## 5. Block palette

| 1.7.10 field (meta) | Modern block | Used for | Cite |
|---|---|---|---|
| `Blocks.field_150406_ce` meta 1 | `minecraft:orange_terracotta` | 14×14×12 shell | GD:6059-6060, 6049 |
| `Blocks.field_150406_ce` meta 13 | `minecraft:green_terracotta` | stem | GD:6147, 6048 |
| `Blocks.field_150350_a` | `minecraft:air` | interior clear + 48 face holes | GD:6057, 6077-6143 |
| `Blocks.field_150344_f` meta 0 | `minecraft:oak_planks` | 2×2×5 candle pillar | GD:6153 |
| `Blocks.field_150424_aL` | `minecraft:netherrack` | 2×2 candle cap | GD:6160 |
| `Blocks.field_150480_ab` | `minecraft:fire` (default state) | 2 eternal flames | GD:6166 |
| `Blocks.field_150474_ac` | `minecraft:spawner` | 2 spawners | GD:6171, 6177 |
| (placement scan only) `Blocks.field_150349_c` | grass block | grass anchor test | OSW:2414-2415 |

1.7.10 stained-hardened-clay meta 1 = orange, meta 13 = green (the locals are literally
named `orange`/`dark_green`, GD:6048-6049). Fire on netherrack is eternal in both
versions; modern fire's spread scan reaches only y−1..+4 around itself and every
neighbor within reach is netherrack/spawner/air, so the planks two blocks below the
flames never ignite — same equilibrium as 1.7.10.

---

## 6. Footprint extents + suggested DungeonType

Relative to `(cposx, cposy, cposz)`:

| Axis | Min | Max | Extent | Determined by |
|---|---|---|---|---|
| X | `0` | `+13` | **14** | shell `i = 0..13` (GD:6045, 6053); stem reaches only 2..7 |
| Y | `0` | `+17` | **18** | shell floor `j = 0` (GD:6054) / stem top `y = 14 + 3` (GD:6145-6147); nothing below origin |
| Z | `0` | `+11` | **12** | shell `k = 0..11` (GD:6046, 6055); stem/candle interior |

Suggested entry (asymmetric 6-int ctor `(xMin, xMax, down, up, zMin, zMax, mode)`, +1
margin):

```java
PUMPKIN(-1, 14, 1, 18, -1, 12, PlacementMode.ISLANDS_GRASS_AIR),
```

## 7. Placement — NEEDS_NEW_MODE: `ISLANDS_GRASS_AIR` (grass anchor + 1)

`addPumpkin`'s scan (quoted in full, §1.1) is the standard Islands D4 anchor — LessLag
1/2 skip (OSW:2408-2410), `chunk + nextInt(8)` jitter (OSW:2411-2412), Y 20→5 downward
grass scan (OSW:2413-2415) — which `ISLANDS_GRASS` (`islandsGrassOrigin`, LDS:305-318)
already reproduces exactly, **but the origin passed to the builder is `posY + 1`, the
air block ABOVE the grass** (OSW:2416). Every other ISLANDS_GRASS original anchors AT
the grass block (Rookery OSW:2269, StinkyHouse :2292, WhiteHouse :2315, EnderCastle
:2338, IncaPyramid :2361, RobotLab :2384, Mini :2400), so the existing mode returns the
grass Y (LDS:312-317) and cannot serve the pumpkin unmodified.

The existing-mode list has an exact precedent for this delta: `OCEAN_SURFACE_AIR` is
`OCEAN_SURFACE`'s scan with the anchor lifted one block, implemented inline as
`oceanSurfaceOrigin(context).above()` in `findGenerationPoint` (LDS:72-78, Play Pool).
**Recommendation: add `ISLANDS_GRASS_AIR` the same way** — a new `PlacementMode`
constant whose `findGenerationPoint` case yields
`islandsGrassOrigin(context)` `== null ? null : origin.above()` — rather than baking a
+1 into every generator coordinate (which would desynchronize the DSB path's
`pos.above()` convention, §9, and the box math). This decision is covered by pattern
§1 step 4 ("add a mode ... + a case in findGenerationPoint") plus the OCEAN_SURFACE_AIR
precedent — no NEEDS_DESIGN_RULING condition arises.

JSON trio (copy the `mini_dungeon` trio — same dimension, same scan family):

- `RES:worldgen/structure/pumpkin.json` — `"type": "orespawn:legacy_dungeon"`,
  `"dungeon_type": "PUMPKIN"`, `"biomes": "#orespawn:has_structure/pumpkin"`,
  `"step": "surface_structures"`, `"terrain_adaptation": "none"`,
  `"spawn_overrides": {}`.
- `RES:tags/worldgen/biome/has_structure/pumpkin.json` — `["orespawn:island_biome"]`
  (matching mini_dungeon's tag).
- `RES:worldgen/structure_set/pumpkin.json` — §8.

The LessLag 1/2 skip lives inside `islandsGrassOrigin` (LDS:306-308) and carries over;
`addPumpkin` has **no clearance probe** to approximate (§12 S4) — the `D4BigSpaceCheck`
at the D4 gate (OSW:134) is absorbed by set spacing per the standing C7 treatment.

## 8. Structure-set conversion

Effective odds: 1/100 (D4 gate, OSW:134) × 1/19 (`i == 17`, OSW:135/171) = **1/1900 per
Islands chunk** — the standard single-outcome D4 arithmetic (pattern §1 step 4:
"each single-outcome i is 1/1900 → spacing 44/22"; mini_dungeon shipped exactly that,
`RES:worldgen/structure_set/mini_dungeon.json`).

C7 sqrt equivalence: spacing ≈ √1900 ≈ 43.6 → **spacing 44, separation 22**.
Salt: **84366** (assigned to this task; verified free — grep of
`RES:worldgen/structure_set/*.json` shows the OreSpawn 84xxx salt block topping out at
84361 (cephadrome_altar); the only larger salt is dim_villages' 10387399; no 84366
anywhere).

`RES:worldgen/structure_set/pumpkin.json`: random_spread, spacing 44, separation 22,
salt 84366.

## 9. DungeonSpawnerBlock outcome

- Original: `if (type == 44)` → `makePumpkin(world, clickedX, clickedY + 1, clickedZ)`
  — one call, **+1 Y offset**, block read in full (DSB:185-187).
- Port: add `TYPE_PUMPKIN = 44` (cite DSB:185-187) and
  `case TYPE_PUMPKIN -> LegacyDungeonPiece.buildNow(server, pos.above(),
  DungeonType.PUMPKIN)` in `RandomDungeonSpawnerBlockEntity.buildForType` — the
  `pos.above()` carries the original's `clickedY + 1` (the DSB already cleared that
  cell, DSB:50-51 → RDS:121, so the shell floor lands in cleared space). Wired types
  today: 0/1/2/3/7/12/13/14/15/16/17/18/19/20/21/22/23/24/27/29/30/34/37/38/47
  (RDS:44-91, cases RDS:145-259); **type 44 currently falls through to the
  generic-dungeon default** (RDS:261-262). No currently-wired case uses an offset, so
  this is the first `pos.above()` — comment it with the DSB citation.
- The DSB path bypasses the grass scan entirely — a jack-o'-lantern embedded in a
  hillside or floating one block above wherever the spawner sat is faithful behavior
  (the interior air writes carve the terrain out of the inside regardless).

## 10. Mid-build world READS — classified

1. **Spawner tile-entity fetches (GD:6172, 6178)** — SELF-reads of the spawner blocks
   written the line before (GD:6171, 6177); absorbed by `piece.placeSpawner`. No
   deviation decision needed.
2. **There are no other reads.** No `func_147439_a`/`func_147437_c`/`func_147438_o`
   call exists anywhere in GD:6041-6182 — no terrain probe, no conditional foundation.
   The generator branches only on loop indices; every pass of the per-chunk replay is
   trivially identical.

## 11. RNG stream

**Zero draws.** `makePumpkin` never touches `world.rand` or any `Random` — every
coordinate, block, meta, and mob name is a constant. Nothing moves to JSON (no loot);
the ported generator consumes no randomness and is stitching-safe by construction. The
dispatch-layer rolls (OSW:134 gate + OSW:135 i-roll + OSW:2408/2411-2412 LessLag/jitter
on the chunk `random`; DSB:52 on `world.rand`) collapse into structure-set frequency /
the `ISLANDS_GRASS_AIR` anchor / the DSB roll as usual.

## 12. Surprises / MISSING-IN-PORT

- **S1 (MISSING-IN-PORT)**: `makePumpkin` exists nowhere in `src/main/java/` (grep
  `Pumpkin` there: only ItemSifter pumpkin seeds, a vanilla PUMPKIN block in the
  generic-dungeon palette, and Ghost-Pumpkin-Skelly comments). No DungeonType, no
  generator, no JSON trio; DSB type 44 falls through to the generic-dungeon default
  (RDS:261-262).
- **S2 (NEEDS_NEW_MODE)**: the **+1 anchor** (OSW:2416) is unique among the Islands D4
  grass-scan adds — existing `ISLANDS_GRASS` returns the grass block itself. Add
  `ISLANDS_GRASS_AIR` (`islandsGrassOrigin(...).above()`), mirroring the
  OCEAN_SURFACE_AIR/Play-Pool precedent (LDS:72-78). Covered by pattern §1 step 4 — not
  a NEEDS_DESIGN_RULING situation.
- **S3**: **No loot anywhere** — one of the few chest-free dungeons. Do not invent a
  chest or a loot table.
- **S4**: `addPumpkin` has **no clearance probe** (contrast the eight probed adds in
  the D4 i-roll — Castle/Greenhouse/Rookery/StinkyHouse/WhiteHouse/EnderCastle/
  IncaPyramid/RobotLab, §1.1; RubyDungeon/CephadromeAltar/Mini/GenericDungeon are
  probe-free like the pumpkin, and Rainbow has no ground scan at all, OSW:2430-2436)
  and only ONE jitter attempt — nothing beyond the shared
  `D4BigSpaceCheck`/cooldown couplings to absorb into set spacing.
- **S5**: Loop A explicitly writes AIR through the whole 12×12×10 interior
  (GD:6056-6057, 6070) — terrain inside the box is cleared, not skipped. Keep the
  interior writes; do not optimize them away.
- **S6**: The face is carved ONLY on the `z = 0` (−z) wall — 48 air holes (two 3×3
  eyes, a split 2×2+2×2 nose with a 2-wide gap, a grin with 4 top notches at y4, a
  solid 8×2 band at y2-3, and 2 bottom fangs at y1). The other three walls and the lid
  are solid. Openings mean rain/mobs/players can pass through the face holes —
  faithful.
- **S7**: The candle assembly (planks 2×2×5 → netherrack 2×2 → fire pair at z=5 +
  spawner pair at z=6, §2.4-2.7) puts eternal fire TWO blocks above the flammable
  planks with only netherrack between — outside modern fire's ignition reach (y−1..+4
  neighbors are all non-flammable), so the candle never burns down; same as 1.7.10.
  Place fire with `piece.place(x, y, z, Blocks.FIRE.defaultBlockState())` (flag-2, no
  update — it will not instantly re-shape or vanish on placement).
- **S8**: The stem (§2.3) is one block thick (z=5 only) and leans −x one block per
  layer — green terracotta meta 13 (`dark_green`), not lime. Top of stem (y+17) is the
  structure's Y max.
- **S9**: The two spawners share the netherrack cap with the fire pair — mobs spawn
  centered on spawners at y+7 inside the lantern. `GhostSkelly`'s spawner-proximity
  gate (port GhostSkelly.java:175) already whitelists this exact spawner string.
- **S10**: `addPumpkin`'s return contract is the plain one — `true` only on placement
  (sets `recently_placed = 50`); the OSW:172 call site discards the result. LessLag
  halving is already implemented in `islandsGrassOrigin` (LDS:306-308) and must NOT be
  duplicated in the new mode's `.above()` wrapper.
- **S11**: `makeRoundRotator` (GD:6184) sits between makePumpkin and makeRainbow in
  the file but is a separate DSB-45 structure with its own extraction task — not a
  helper; nothing in GD:6041-6182 references it.
