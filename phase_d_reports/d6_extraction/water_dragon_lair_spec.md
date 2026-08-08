# Water Dragon Lair — Extraction Spec (1.7.10 → NeoForge 1.21.1)

Source of truth: `reference_1_7_10_source/sources/danger/orespawn/GenericDungeon.java` —
`makeWaterDragonLair` (GD:1959-2057, next method `makeCloudSharkDungeon` at GD:2059).
All coordinates are relative to the build origin `(cposx, cposy, cposz)` = the three int
args. At worldgen the origin is the ocean's water-surface block (scan hit `posY − 1`,
OSW:1370).

Citation convention: `GD:NN` = GenericDungeon.java line NN, `OSW:NN` = OreSpawnWorld.java,
`OSM:NN` = OreSpawnMain.java, `DSB:NN` = DungeonSpawnerBlock.java, `COG:NN` =
ChunkOreGenerator.java. Port citations name file + line.

Shared plumbing:
- `FastSetBlock(world, x, y, z, block)` (GD:187-189) = `OreSpawnMain.setBlockFast(...,
  meta 0, flag 2)` — chunk write, no neighbor updates → port `piece.place(x, y, z, state)`
  (structure_conversion_pattern.md §1 step 3 table).
- Spawners: `world.func_147465_d(..., field_150474_ac, 0, 2)` then `getSpawnerTileEntity`
  (GD:86-95, a `func_147438_o` getTileEntity fetch) + `func_98272_a("Water Dragon")` →
  port `piece.placeSpawner(x, y, z, type)`.
- Chest: `func_147465_d(..., field_150486_ae, 0, 2)` (meta 0 — default facing, no facing
  stamp) then `getChestTileEntity` (GD:75-84) + `WeightedRandomChestContent.func_76293_a`
  → port `piece.placeLootChest(x, y, z, lootKey)` (null facing).

## 1. Entry points — EVERY call site

Grep of the whole original tree for `makeWaterDragonLair`: exactly two call sites
(OSW:1370, DSB:93; GD:1959 is the definition). `addWaterDragonLair` itself is called only
from the OSW:290 picker.

| Caller | Signature used | Coords passed | Notes |
|---|---|---|---|
| `OreSpawnWorld.addWaterDragonLair` (OSW:1370) | `OreSpawnMain.MyDungeon.makeWaterDragonLair(world, posX, posY - 1, posZ)` | scan hit, **Y offset −1** (cposy = the found still-water surface block's Y) | worldgen path, vanilla-overworld Ocean only (§1.1) |
| `DungeonSpawnerBlock` type **13** (DSB:92-94) | `...makeWaterDragonLair(world, clickedX, clickedY, clickedZ)` | player-placed block pos, no offset | the `if (type == 13)` block read IN FULL: **single builder call, nothing else** (DSB:92-94) — not a two-builder index. Roll `world.rand.nextInt(50)` (DSB:52), fires 400 ticks after placement (DSB:39), self+above deleted first (DSB:50-51) |

### 1.1 Worldgen dispatch chain (complete) + FULL return contract

- The whole overworld dungeon-picker block is gated by
  `OreSpawnMain.DisableOverworldDungeons == 0 && world.field_73011_w.field_76574_g == 0 &&
  recently_placed == 0` (OSW:284) — **vanilla-overworld-exclusive**, config-disableable,
  suppressed for 50 chunks after any structure placement.
- Picker: `i = world.field_73012_v.nextInt(6)` (OSW:285 — **world rand**, not the chunk
  `random`): 0 PlayPool, **1 → addWaterDragonLair (OSW:289-291)**, 2 GoldFishBowl,
  3 GirlfriendIsland, 4 MonsterIsland, 5 FrogPond.
- `addWaterDragonLair(world, random, chunkX, chunkZ)` (OSW:1358-1376), read in full:
  1. Gate: `random.nextInt(350) != 0 → return` (OSW:1359-1361) — 1/350, chunk-provided
     `random`.
  2. Biome: `world.func_72807_a(chunkX, chunkZ)` (chunk-corner block coords) must have
     `biomeName` **exactly `"Ocean"`** (OSW:1362-1363) — excludes Deep Ocean, FrozenOcean,
     beaches (same check as addGoldFishBowl OSW:1180-1181 and addMonsterIsland).
  3. Up to 4 attempts (OSW:1364): `posX = chunkX + random.nextInt(16)`,
     `posZ = chunkZ + random.nextInt(16)` (OSW:1365-1366). (`boolean which = false`
     OSW:1367 is a dead local — never read.)
  4. Column scan `posY = 100` down to `41` inclusive (`posY > 40`, OSW:1368): require
     `block(posX, posY, posZ) == air` AND `block(posX, posY-1, posZ) ==
     Blocks.field_150355_j` (**still water** directly below) (OSW:1369).
  5. Hit → `makeWaterDragonLair(world, posX, posY - 1, posZ)` — origin = the
     water-surface block — then `recently_placed = 50` and `return` (OSW:1370-1372).
- **FULL return contract: the method is `void`.** Gate/biome/scan failure returns having
  done nothing. Success sets the global 50-chunk cooldown (`recently_placed = 50`,
  OSW:1371) and returns. Either way the same chunk's follow-on `ahh` chain
  (addANest → … → addRubberDuckyPond, OSW:304-321) still runs — the OSW:284
  `recently_placed == 0` gate was evaluated before the picker. No boolean coupling, no
  addFairyTree-style early-true quirk (WGEN-062) to port. Byte-identical contract shape to
  `addGoldFishBowl` (gold_fish_bowl_spec.md §1.1).
- Effective odds: 1/6 × 1/350 = **1/2100 per overworld chunk whose corner biome is
  Ocean**, before scan success. No LessLag, no `D4BigSpaceCheck` air probe.
- `WaterDragonEnable` (OSM:4843-4847 region) gates only the natural ocean/river/swamp
  spawn-list entries — it does NOT gate the lair worldgen, the DSB roll, or the spawners
  (same scope finding as GoldFishEnable, gold_fish_bowl_spec.md S7). Port mirror:
  `OreSpawnConfig.WATER_DRAGON_ENABLE` (OreSpawnConfig.java:65,200) →
  `ModSpawnControl` (ModSpawnControl.java:115). The structure needs no config check.

## 2. Geometry — per-loop tables (all ranges inclusive)

Write order matters (later writes overwrite earlier ones — see §2.9): roof disc →
roof spokes → roof center → rim cylinder → sand pad → leaf canopy → tree → spawners →
chest.

**The float-polar idiom (loops A, B-center writes, C):** cells are addressed as
`(int)((float)cposx + curx + 0.5f)` etc. For positive world coordinates this is
round-half-up (`floor(v + 0.5)`); for negative world coordinates Java's
truncation-toward-zero shifts every such write **+1** on that axis relative to the
plain-int writes (pad/tree/spawners/chest). This coordinate-sign-dependent misalignment is
original behavior. Port precedent: `CrystalStructures.buildCrystalBattleTower`
(CrystalStructures.java:801-837) transcribes the identical idiom **verbatim** — the piece
helpers take absolute world coordinates, so a verbatim transcription reproduces the
original exactly, quirk included. Do the same here (S2).

### 2.1 Loop A — roof disc at `y+7` (GD:1972-1982)

`for (currad = 0.0f; currad < 10.0f; currad += 0.33f)` — 31 radius steps (0.00, 0.33, …,
9.90; float accumulation), × `for (curdeg = 0.0f; curdeg < 360.0f; curdeg += 5.0f)` — 72
angles. `curx = currad·cos(rad(curdeg))`, `curz = currad·sin(rad(curdeg))` (GD:1974-1975).

| Condition | Cell | Block | Cite |
|---|---|---|---|
| default | `((int)(cposx+curx+0.5f), +7, (int)(cposz+curz+0.5f))` | bedrock | GD:1976, 1980 |
| `currad > 5.0f && currad < 6.0f` (exclusive; steps 5.28, 5.61, 5.94 only) | same | iron block | GD:1977-1979 |

Net: a solid disc of bedrock, radius ≈ 10 (max sampled radius 9.90 → rel extent −10..+10
at positive coords), with a ~1-block-wide iron-block annulus at radius ≈ 5-6. At
`currad = 0` all 72 angles write the same center cell (harmless). No RNG.

### 2.2 Loop B — roof spokes at `y+7` (GD:1983-1988)

For `i = 1..9` (`i < 10`), iron block at `(±i, +7, 0)` and `(0, +7, ±i)` (each line uses
the `(int)(v+0.5f)` idiom on integer sums — identity at positive coords). Overwrites disc
bedrock along both axes → an iron cross over the bedrock roof, merging with the annulus.

### 2.3 Roof center — hole + glowstone cross at `y+7` (GD:1989-1993)

| Cell | Block | Overwrites | Cite |
|---|---|---|---|
| `(0, +7, 0)` | **air** | loop-A disc center | GD:1989 |
| `(+1, +7, 0)`, `(−1, +7, 0)`, `(0, +7, +1)`, `(0, +7, −1)` | glowstone | loop-B spoke iron at rel 1 | GD:1990-1993 |

The 1×1 air hole is the roof's only opening (S3) — do not fill it.

### 2.4 Loop C — rim cylinder, radius 10, `y+1..+6` (GD:1994-2012)

`currad = 10.0f` fixed (GD:1994); 72 angles (`curdeg` 0→355 step 5). Per angle, at
`((int)(cposx+curx+0.5f), y, (int)(cposz+curz+0.5f))`:

| y | Block | RNG | Cite |
|---|---|---|---|
| `+1` | glowstone | — | GD:1998 |
| `+2` | lapis block, **or Water Dragon spawn-egg block if `world.field_73012_v.nextInt(2) == 0`** | draw #1 | GD:1999-2003 |
| `+3` | lapis block, or spawn-egg block (independent 50% draw) | draw #2 | GD:2004-2008 |
| `+4` | glowstone | — | GD:2009 |
| `+5` | bedrock | — | GD:2010 |
| `+6` | bedrock | — | GD:2011 |

**2 draws × 72 angles = 144 `nextInt(2)` draws total**, unconditional inside the loop —
in the port generator draw them exactly as written, unconditionally, and let the gated
`piece.place` drop out-of-chunk writes (RNG stitching contract, pattern §1 step 3).
Mirrored angles that round to the same cell simply overwrite with a fresh draw — per-cell
distribution stays 50/50; transcribe verbatim, do not deduplicate. Arc spacing at r=10 is
2π·10/72 ≈ 0.87 blocks — the ring closes with no gaps, same idiom as the crystal battle
tower rim.

### 2.5 Loop D — sand pad at `y 0 / −1` (GD:2013-2018)

For `i = −3..3`, `j = −3..3` (plain ints, no rounding idiom):

| Cell | Block | Cite |
|---|---|---|
| `(i, 0, j)` | sand | GD:2015 |
| `(i, −1, j)` | stone | GD:2016 |

7×7 pad replacing the water-surface layer and the water one below; ocean water remains
beneath the stone (a floating raft, faithful). Everything between the pad and rim stays
untouched ocean surface (S6).

### 2.6 Loop E — leaf canopy at `y+3` (GD:2019-2023)

For `i = −2..2`, `j = −2..2`: `(i, +3, j)` = oak leaves (`field_150362_t`, meta 0). 5×5
slab.

### 2.7 Tree — apex leaf + trunk + diagonal branches (GD:2024-2031)

| Cell | Block | Overwrites | Cite |
|---|---|---|---|
| `(0, +4, 0)` | oak leaves | — (above canopy) | GD:2024 |
| `(0, +3, 0)` | oak log | canopy leaf | GD:2025 |
| `(0, +2, 0)` | oak log | — | GD:2026 |
| `(0, +1, 0)` | oak log | — | GD:2027 |
| `(+1, +3, +1)`, `(−1, +3, −1)`, `(+1, +3, −1)`, `(−1, +3, +1)` | oak log | canopy leaves | GD:2028-2031 |

### 2.8 Spawners ×4 + chest (GD:2032-2056)

Four spawners, each `func_147465_d(..., field_150474_ac, 0, 2)` + TE fetch +
`func_98272_a("Water Dragon")`, embedded in the canopy at the trunk-top's cardinal
neighbors (each overwrites a loop-E leaf):

| # | Position | Cite |
|---|---|---|
| 1 | `(+1, +3, 0)` | GD:2032-2036 |
| 2 | `(−1, +3, 0)` | GD:2037-2041 |
| 3 | `(0, +3, +1)` | GD:2042-2046 |
| 4 | `(0, +3, −1)` | GD:2047-2051 |

One chest at `(0, +1, −1)` (GD:2052), filled with `WeightedRandomChestContent.func_76293_a
(world.field_73012_v, WaterDragonContentsList, chest, 4 + world.field_73012_v.nextInt(5))`
→ **4-8 weighted pulls** into random slots (collisions overwrite — documented
approximation, pattern §1 step 5) (GD:2053-2056).

### 2.9 Net shape + overwrite summary

An open-air ocean-surface arena: a radius-10 rim wall 6 high (glowstone courses at +1/+4,
a 2-course egg-studded lapis band at +2..+3, bedrock parapet at +5..+6), roofed at +7 by a
bedrock disc bearing an iron annulus + iron cross + central glowstone-ringed air hole. On
the water inside: a 7×7 floating sand raft carrying a 3-log oak tree whose 5×5 canopy at
+3 holds four Water Dragon spawners and four diagonal branch logs, apex leaf at +4, loot
chest on the raft beside the trunk at `(0,+1,−1)`. Overwrite chain (preserve write order):
disc bedrock → spoke iron → center air/glowstone; canopy leaves → trunk/branch logs →
spawners.

## 3. Loot — FULL transcription

`WaterDragonContentsList` (GD:48) — constructor semantics `(item, meta=0, minStack,
maxStack, weight)`. 7 entries, **total weight = 145** (25×4 + 15×3).
`pools[0].rolls`: uniform **min 4, max 8** (from `4 + nextInt(5)`, GD:2055).

| # | 1.7.10 item | Modern / port mapping (cite) | min | max | weight |
|---|---|---|---|---|---|
| 1 | `Items.field_151115_aP` (raw fish, meta 0) | `minecraft:cod` | 6 | 16 | 25 |
| 2 | `OreSpawnMain.MyUltimateAxe` ("ultimateaxe", OSM:1642) | port `ModItems.ULTIMATE_AXE` "ultimate_axe" (ModItems.java:282) | 1 | 1 | 15 |
| 3 | `OreSpawnMain.MyUltimatePickaxe` ("ultimatepickaxe", OSM:1637) | `ModItems.ULTIMATE_PICKAXE` "ultimate_pickaxe" (ModItems.java:276) | 1 | 1 | 15 |
| 4 | `OreSpawnMain.MyUltimateShovel` ("ultimateshovel", OSM:1639) | `ModItems.ULTIMATE_SHOVEL` "ultimate_shovel" (ModItems.java:278) | 1 | 1 | 15 |
| 5 | `OreSpawnMain.MyExperienceCatcher` ("experiencecatcher", OSM:1948) | `ModItems.EXPERIENCE_CATCHER` "experience_catcher" (ModItems.java:622) | 4 | 10 | 25 |
| 6 | `Item.func_150898_a(Blocks.field_150339_S)` | `minecraft:iron_block` | 6 | 16 | 25 |
| 7 | `Items.field_151078_bh` | `minecraft:rotten_flesh` | 6 | 16 | 25 |

→ `RES:loot_table/chests/water_dragon_lair.json`, rolls uniform 4-8, one entry per row
above.

## 4. Spawner / mob mapping table

| Spawner name | 1.7.10 registration | Port EntityType (cite) |
|---|---|---|
| `"Water Dragon"` (×4, GD:2035/2040/2045/2050) | `WaterDragon.class` — `registerGlobalEntityID(..., "Water Dragon", WaterDragonID)` OSM:3855, `registerModEntity` OSM:3859 | `ModEntities.WATER_DRAGON` "water_dragon" (ModEntities.java:510-512) |

No direct entity spawns — spawner blocks only. (The port `WaterDragon` honors the
"Water Dragon"-spawner gate bypass, WaterDragon.java:337-344.)

## 5. Block palette

| 1.7.10 field | Modern block | Used for | Cite |
|---|---|---|---|
| `Blocks.field_150357_h` | `minecraft:bedrock` | roof disc, rim +5..+6 | GD:1976, 2010-2011 |
| `Blocks.field_150339_S` | `minecraft:iron_block` | roof annulus + spokes | GD:1978, 1984-1987 |
| `Blocks.field_150426_aN` | `minecraft:glowstone` | roof center cross, rim +1 and +4 | GD:1990-1993, 1998, 2009 |
| `Blocks.field_150368_y` | `minecraft:lapis_block` | rim +2..+3 (50%) | GD:1999, 2004 |
| `OreSpawnMain.MyWaterDragonSpawnBlock` — `OreGenericEgg` "orewaterdragon" (OSM:6294), reg "OreSpawn_WaterDragonSpawnBlock" (OSM:2033), "Ancient Dried WaterDragon Spawn Egg" (OSM:2822) | port `ModBlocks.WATER_DRAGON_SPAWN_BLOCK` "water_dragon_spawn_block" (ModBlocks.java:308-309; same mapping as SpawnOresPoolFeature.java:132 c49) | rim +2..+3 (50%) | GD:2001, 2006 |
| `Blocks.field_150350_a` | `minecraft:air` | roof hole | GD:1989 |
| `Blocks.field_150354_m` | `minecraft:sand` | pad y 0 | GD:2015 |
| `Blocks.field_150348_b` | `minecraft:stone` | pad y −1 | GD:2016 |
| `Blocks.field_150362_t` meta 0 | `minecraft:oak_leaves` **with `PERSISTENT=true`** (flag-2 writes never run neighbor updates → default DISTANCE=7 decays; adaptation per pattern doc §4 trap, precedent MonsterIslandGenerator.java:77-79) | canopy +3, apex +4 | GD:2021, 2024 |
| `Blocks.field_150364_r` meta 0 | `minecraft:oak_log` (default axis Y) | trunk +1..+3, 4 branch logs | GD:2025-2031 |
| `Blocks.field_150474_ac` | `minecraft:spawner` | 4 spawners | GD:2032-2051 |
| `Blocks.field_150486_ae` | `minecraft:chest` (default facing) | 1 loot chest | GD:2052 |

## 6. Footprint extents + suggested DungeonType

Relative to `(cposx, cposy, cposz)`:

| Axis | Min | Max | Extent | Determined by |
|---|---|---|---|---|
| X | `−10` | `+10` (positive-coord regime; **−9..+11 at negative world coords** — §2 idiom) | 21 (union −10..+11) | rim radius 10.0 (GD:1994) / disc max radius 9.90 (GD:1972), `(int)(v+0.5f)` rounding |
| Y | `−1` | `+7` | **9** | pad stone y−1 (GD:2016) / roof y+7 (GD:1980) |
| Z | `−10` | `+10` (same negative-coord caveat) | 21 | same lines as X |

Suggested entry (asymmetric 6-int ctor; bounds cover the UNION of both truncation regimes
[−10, +11] plus 1 margin — a write outside the box would be dropped in non-intersecting
chunks):

```java
WATER_DRAGON_LAIR(-11, 12, 2, 8, -11, 12, PlacementMode.OCEAN_SURFACE),
```

**PlacementMode: `OCEAN_SURFACE` — exact fit, no new mode needed.** The scan
(OSW:1364-1374) is line-for-line identical to `addMonsterIsland`'s (OSW:1398-1412), which
`oceanSurfaceOrigin` (LegacyDungeonStructure.java:197-208, dispatched at :71) already
implements: 4 attempts, chunk + `nextInt(16)` jitter, Y 100→41 downward scan for air
directly above still water, anchor at `posY − 1` (the water-surface block). Same mode
already reused by GOLD_FISH_BOWL (LegacyDungeonPiece.java:172).

## 7. Structure-set conversion

- Effective odds 1/6 × 1/350 = **1/2100** per qualifying chunk (§1.1) — identical to Gold
  Fish Bowl. C7 sqrt equivalence: spacing ≈ √2100 ≈ 45.8 → **spacing 46, separation 23**.
- **Salt 84358** (assigned to this task; verified free — grep of
  `RES:worldgen/structure_set/*.json` on extraction date shows highest in use = 84354,
  plus the known 84312 mantis_nest/royal_trees collision).
- The `recently_placed = 50` cooldown (OSW:1371) maps onto structure-set separation per
  the standing C7 approximation.

JSON trio (copy the `gold_fish_bowl`/`monster_island` trio — same anchor + biome):

- `RES:worldgen/structure/water_dragon_lair.json` — `"type": "orespawn:legacy_dungeon"`,
  `"dungeon_type": "WATER_DRAGON_LAIR"`, `"biomes": "minecraft:ocean"` (original
  exact-name `"Ocean"`, OSW:1363 — matches the monster_island exact-"Ocean" precedent; no
  deep/frozen variants), `"step": "surface_structures"`, `"terrain_adaptation": "none"`,
  `"spawn_overrides": {}`.
- `RES:worldgen/structure_set/water_dragon_lair.json` — random_spread, spacing 46,
  separation 23, salt 84358.
- No biome tag file needed (vanilla tag inline, matching monster_island/gold_fish_bowl).

## 8. DungeonSpawnerBlock outcome

- Original: `if (type == 13)` → `makeWaterDragonLair(world, clickedX, clickedY, clickedZ)`
  — one call, no offset, block read in full (DSB:92-94).
- Port: add constant `TYPE_WATER_DRAGON_LAIR = 13` (cite DSB:92-94) and case
  `TYPE_WATER_DRAGON_LAIR -> LegacyDungeonPiece.buildNow(server, pos,
  DungeonType.WATER_DRAGON_LAIR)` in
  `src/main/java/danger/orespawn/block/entity/RandomDungeonSpawnerBlockEntity.buildForType`
  (wired types today: 0/1/2/3/7/12/14/17/19/21/22/23/24/27/29/30/37/38/47,
  RandomDungeonSpawnerBlockEntity.java:44-79, 133-218; **type 13 currently falls through
  to the generic-dungeon fallback**, `default` arm at :219).
- The DSB path faithfully bypasses the ocean scan — a lair embedded in terrain or hanging
  in the air at the clicked position is original behavior (same ruling as
  GoldFishBowl/MonsterIsland).

## 9. World-block READS mid-build

**None that branch.** The only world reads inside GD:1959-2057 are the four post-placement
spawner TE fetches (GD:2033/2038/2043/2048) and the chest TE fetch (GD:2053) — all
`func_147438_o` self-reads at positions just written, absorbed by `piece.placeSpawner` /
`piece.placeLootChest`. Zero `func_147439_a` (getBlock) calls — the "never branch on world
state" rule is satisfied with no in-memory model and no deviation decision.

## 10. RNG stream

All builder draws come from `world.field_73012_v` (world rand — PARITY: port maps to the
deterministic piece RandomSource; DSB `buildNow` keeps live RNG, pattern §1 step 3):

1. **144 × `nextInt(2)`** — rim block picks, two per angle (GD:2000, 2005), 72 angles.
   Draws are already unconditional in the source loop; keep them unconditional in the
   port and gate only the writes.
2. **1 × `nextInt(5)`** — chest fill count `4 + nextInt(5)` (GD:2055) → moves into the
   loot JSON `rolls 4-8`, leaving the ported generator's stream at exactly 144 draws,
   identical every replay pass.

Dispatch-layer rolls (OSW:285 picker on world rand, OSW:1359 gate on the chunk `random`,
DSB:52) collapse into structure-set frequency / the DSB roll as usual; the mixed-source
shape is the same as gold_fish_bowl_spec.md S8.

## 11. Surprises / MISSING-IN-PORT

- **S1 (MISSING-IN-PORT)**: `makeWaterDragonLair` has no counterpart anywhere in
  `src/main/java/` — the port has the entity, spawn-egg block, scale, config, and renderer
  (WaterDragon.java, ModBlocks.java:308, etc.) but no lair: no DungeonType, no JSON trio,
  no loot table; DSB type 13 falls through to the generic-dungeon fallback today.
- **S2 (float-polar rounding)**: roof disc, spokes, center cross, and rim all use
  `(int)((float)c + off + 0.5f)`; pad, canopy, tree, spawners, chest use plain ints. At
  negative world coordinates the idiom's truncation-toward-zero shifts the entire
  roof/rim assembly +1 per negative axis relative to the pad/tree — an original,
  coordinate-dependent misalignment. Transcribe verbatim (helpers take absolute world
  coords; precedent CrystalStructures.java:801-837) and it reproduces itself; the box
  extents in §6 cover both regimes. Also keep `currad`/`curdeg` as float accumulation
  (`+= 0.33f` / `+= 5.0f`), not recomputed multiples.
- **S3**: The roof's center block is deliberately set to AIR (GD:1989) after the disc and
  spokes — a 1×1 skylight ringed by glowstone, the arena's only through-roof opening. Do
  not fill it.
- **S4**: The rim's +2..+3 band is ~50% `MyWaterDragonSpawnBlock` — the harvestable
  "Ancient Dried WaterDragon Spawn Egg" block (`OreGenericEgg`, OSM:6294). The wall is
  literally studded with mineable dragon-egg blocks; that IS the reward structure of the
  lair alongside the chest. Port block exists (ModBlocks.java:308).
- **S5**: Oak leaves need `PERSISTENT=true` in the port (flag-2 writes skip neighbor
  updates; default DISTANCE=7 leaves beside logs still decay on random ticks) —
  established adaptation, MonsterIslandGenerator.java:77-79.
- **S6**: The arena interior (between pad edge and rim, y ≥ 0) is NEVER written — at
  worldgen it stays open ocean surface, so the lair floor is mostly water with a floating
  7×7 raft; water also remains beneath the pad's 1-block stone layer. Faithful; do not
  floor it over. Via the DSB path (no water present) the interior is simply whatever was
  there.
- **S7**: Spawner/branch overwrites carve the canopy: 4 cardinal leaves → spawners,
  4 diagonal leaves + center leaf → logs (§2.7-2.8). Preserve write order or place
  logs/spawners after the leaf slab — same result.
- **S8**: `addWaterDragonLair` is `void` with no early-true quirk; success sets
  `recently_placed = 50`; the same-chunk `ahh` chain still runs either way (§1.1) — no
  coupling to port beyond structure-set separation.
- **S9**: Rim RNG can double-draw cells where mirrored angles round to the same block
  (§2.4) — later draw wins; per-cell odds remain 50/50. Transcribe verbatim, never
  deduplicate or gate the draws (RNG stitching contract).
- **S10**: Dead local `which` (OSW:1367) — ignore. Unused method locals `i`, `j`, `blk`
  declarations at GD:1960-1966 collapse into loop-scoped variables in the port.
