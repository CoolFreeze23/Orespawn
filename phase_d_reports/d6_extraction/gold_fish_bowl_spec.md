# Gold Fish Bowl — Extraction Spec (1.7.10 → NeoForge 1.21.1)

Source of truth: `reference_1_7_10_source/sources/danger/orespawn/GenericDungeon.java` —
`makeGoldFishBowl` (GD:2408-2488, next method `makeEnderReaperGraveyard` at GD:2490).
All coordinates are relative to the build origin `(cposx, cposy, cposz)` = the three int args.

Citation convention: `GD:NN` = GenericDungeon.java line NN, `OSW:NN` = OreSpawnWorld.java,
`OSM:NN` = OreSpawnMain.java, `DSB:NN` = DungeonSpawnerBlock.java. Port citations name file + line.

Shared plumbing:
- `FastSetBlock(world, x, y, z, block)` (GD:187-189) → flag-2 chunk write, no neighbor updates
  → port `piece.place(x, y, z, state)` (structure_conversion_pattern.md §1 step 3 table).
- Spawner placed with `world.func_147465_d(x, y, z, Blocks.field_150474_ac, 0, 2)` then
  `getSpawnerTileEntity` (GD:86-95, a `func_147438_o` getTileEntity fetch) +
  `func_98272_a(name)` → port `piece.placeSpawner(x, y, z, type)`.

## 1. Entry points — EVERY call site

Grep of the whole original tree for `makeGoldFishBowl`: exactly two call sites.

| Caller | Signature used | Coords passed | Notes |
|---|---|---|---|
| `OreSpawnWorld.addGoldFishBowl` (OSW:1188) | `OreSpawnMain.MyDungeon.makeGoldFishBowl(world, posX, posY - 1, posZ)` | scan hit, **Y offset −1** (cposy = the found still-water surface block's Y) | worldgen path, vanilla-overworld Ocean only (§1.1) |
| `DungeonSpawnerBlock` type **17** (DSB:104-106) | `...makeGoldFishBowl(world, clickedX, clickedY, clickedZ)` | player-placed block pos, no offset | single builder call for this index (block read IN FULL, DSB:104-106); roll `world.rand.nextInt(50)` (DSB:52), fires 400 ticks after placement (DSB:39), self+above deleted first (DSB:50-51) |

### 1.1 Worldgen dispatch chain (complete)

- `generateSurface` runs for dim 0 and `OreSpawnMain.DimensionID`, but the whole overworld
  dungeon-picker block is gated by `OreSpawnMain.DisableOverworldDungeons == 0 &&
  world.provider.dimensionId == 0 && recently_placed == 0` (OSW:284) — **vanilla-overworld-exclusive**,
  config-disableable, suppressed for 50 chunks after any structure placement.
- Picker: `i = world.field_73012_v.nextInt(6)` (OSW:285 — **world rand**, not the chunk `random`):
  0 PlayPool, 1 WaterDragonLair, **2 → addGoldFishBowl (OSW:292-294)**, 3 GirlfriendIsland,
  4 MonsterIsland, 5 FrogPond.
- `addGoldFishBowl(world, random, chunkX, chunkZ)` (OSW:1176-1194), read in full:
  1. Gate: `random.nextInt(350) != 0 → return` (OSW:1177-1179) — 1/350, chunk-provided `random`.
  2. Biome: `world.func_72807_a(chunkX, chunkZ)` (chunk-corner block coords) must have `biomeName`
     **exactly `"Ocean"`** (OSW:1180-1181) — excludes Deep Ocean, FrozenOcean, beaches.
  3. Up to 4 attempts (OSW:1182): `posX = chunkX + random.nextInt(16)`,
     `posZ = chunkZ + random.nextInt(16)` (OSW:1183-1184). (`boolean which = false` OSW:1185 is a
     dead local — never read.)
  4. Column scan `posY = 100` down to `41` (OSW:1186): require `block(posX, posY, posZ) == air`
     AND `block(posX, posY-1, posZ) == Blocks.field_150355_j` (**still water** directly below)
     (OSW:1187).
  5. Hit → `makeGoldFishBowl(world, posX, posY - 1, posZ)` — origin = the water-surface block —
     then `recently_placed = 50` and `return` (OSW:1188-1190).
- **FULL return contract**: the method is `void`. On gate/biome/scan failure it returns having done
  nothing — no `recently_placed`, no suppression of anything. On success it sets the global
  50-chunk cooldown (OSW:30-style static) and returns. Either way, `generateSurface`'s follow-on
  `ahh` chain (addANest → addHauntedHouse → ... → addRubberDuckyPond, OSW:304-321) still runs for
  the same chunk — the OSW:284 `recently_placed == 0` gate was already evaluated before the picker,
  so a fresh placement does NOT suppress the same-chunk `ahh` chain. There is no boolean coupling
  to port (contrast the `addANest` chain and the FairyTree WGEN-062 case).
- Effective odds: 1/6 × 1/350 = **1/2100 per overworld chunk whose corner biome is Ocean**,
  before scan success. No LessLag involvement, no `D4BigSpaceCheck`/`quickSpaceCheck` air probe.

## 2. Geometry — per-loop table (all ranges inclusive)

The builder draws **zero randomness** (no `nextInt`/`nextFloat`/`Math.random` anywhere in
GD:2408-2488) and writes in this exact order. Later writes overwrite earlier ones where noted.

| # | What | Where (relative, inclusive) | Block | Cite |
|---|---|---|---|---|
| 1 | Base plate, 5×5 | `(0..4, +1, 0..4)` | glass (`field_150359_w`) | GD:2412-2418 |
| 2 | Sand bed layer, 7×7 | `j=+2`, `i,k = -1..5`: perimeter `i∈{-1,5} or k∈{-1,5}` = glass, interior `(0..4)` = sand (`field_150354_m`) | glass ring + sand fill | GD:2419-2428 |
| 3 | Lower water layer, 7×7 | `j=+3`, `i,k = -1..5`: perimeter glass, interior water (`field_150355_j`) | glass ring + water fill | GD:2429-2438 |
| 4 | Glowstone corners (overwrite loop-3 interior water) | `(0,+3,0)`, `(4,+3,4)`, `(0,+3,4)`, `(4,+3,0)` | glowstone (`field_150426_aN`) | GD:2439-2451 |
| 5 | Upper water layer, 7×7 | `j=+4`, `i,k = -1..5`: perimeter glass, interior water | glass ring + water fill | GD:2452-2461 |
| 6 | Air headspace, 3 layers | `j = +5..+7`, `i,k = -1..5`: perimeter glass, interior air (`field_150350_a`) | glass ring + air fill | GD:2462-2472 |
| 7 | Lid, 5×5 | `(0..4, +8, 0..4)` | glass | GD:2473-2479 |
| 8 | Spawner (overwrites loop-6 interior air) | `(+2, +6, +2)` | mob spawner, `"Gold Fish"` | GD:2480-2487 |

Net shape: a glass fishbowl sitting on the ocean surface (worldgen cposy = the water block, so
loop 1's `j=+1` base plate lands on the first air block above the sea). Inside: sand bed (+2),
2-deep water pool (+3..+4) with glowstone in the four lower corners, 3 blocks of air headspace
(+5..+7), and one Gold Fish spawner floating centered in the headspace at +6. Two geometry
oddities are faithful, not bugs — see surprises S3/S4: the 7×7 glass walls span only `j = +2..+7`
(the `j=+1` base ring at `i/k = -1/5` is never written), and the 5×5 lid at `j=+8` is inset one
block, so the wall-top ring `(i or k ∈ {-1,5}, j=+8)` is also never written.

## 3. Loot — FULL transcription

**This builder places NO chest and fills NO loot.** GD:2408-2488 contains zero
`field_150486_ae` / `getChestTileEntity` / `WeightedRandomChestContent` calls (the
`func_76293_a` at GD:2404 belongs to the previous method, which ends at GD:2406).
No `loot_table/chests/*.json` is needed. Total weight: n/a.

## 4. Spawner / mob mapping table

| Spawner name | 1.7.10 registration | Port EntityType (cite) |
|---|---|---|
| `"Gold Fish"` | `GoldFish.class` registered as `"Gold Fish"` (OSM:4101-4107: `registerGlobalEntityID` OSM:4103, `registerModEntity` OSM:4107) | `ModEntities.GOLD_FISH` "gold_fish" (src/main/java/danger/orespawn/ModEntities.java:344-346) |

One spawner total, at `(+2, +6, +2)` (GD:2480-2487).

## 5. Block palette

| 1.7.10 field | Modern block | Used for | Cite |
|---|---|---|---|
| `Blocks.field_150359_w` | `minecraft:glass` | base, walls, lid | vanilla |
| `Blocks.field_150354_m` | `minecraft:sand` | bowl bed at +2 | vanilla |
| `Blocks.field_150355_j` | `minecraft:water` (source) | pool at +3..+4 | vanilla |
| `Blocks.field_150426_aN` | `minecraft:glowstone` | 4 pool corners at +3 | vanilla |
| `Blocks.field_150350_a` | `minecraft:air` | headspace +5..+7 | vanilla |
| `Blocks.field_150474_ac` | `minecraft:spawner` | 1 spawner | vanilla |

All flag-2 writes (no neighbor updates) — the enclosed source-water blocks stay static exactly as
in 1.7.10; the port's `piece.place` preserves this.

## 6. Footprint extents + suggested DungeonType

Relative to `(cposx, cposy, cposz)`:

| Axis | Min | Max | Extent | Determined by |
|---|---|---|---|---|
| X | `-1` | `+5` | **7** | 7×7 wall loops (GD:2420, 2430, 2453, 2463) |
| Y | `+1` | `+8` | **8** (nothing at or below origin) | base plate `j=1` (GD:2412) / lid `j=8` (GD:2473) |
| Z | `-1` | `+5` | **7** | 7×7 wall loops (same lines as X) |

Suggested entry (asymmetric 6-int ctor, +1 margin; `down 0` still covers the origin level):

```java
GOLD_FISH_BOWL(-2, 6, 0, 9, -2, 6, PlacementMode.OCEAN_SURFACE),
```

**PlacementMode: `OCEAN_SURFACE` — exact fit, no new mode needed.** The existing
`oceanSurfaceOrigin` (port LegacyDungeonStructure.java:188-208) was written for
`addMonsterIsland` (OSW:1398-1412), and `addGoldFishBowl`'s scan (OSW:1182-1191) is
line-for-line identical: 4 attempts, `chunk + nextInt(16)` jitter, Y 100→41 downward scan for
air directly above still water, anchor at `posY − 1` (the water-surface block). The corner-biome
`"Ocean"` string check maps to the structure JSON's biome field, matching the
`monster_island.json` precedent (`"biomes": "minecraft:ocean"`).

## 7. Structure-set conversion

- Effective odds 1/6 × 1/350 = 1/2100 per qualifying chunk (§1.1) → C7 sqrt equivalence:
  spacing ≈ √2100 ≈ 45.8 → **spacing 46, separation 23**.
- **Salt 84352** (assigned to this task, batch 84350-84355; grep of
  `RES:worldgen/structure_set/*.json` on extraction date shows highest in use = 84345, plus the
  known 84312 collision — 84352 is free).
- The `recently_placed = 50` global cooldown (OSW:1189) maps onto structure-set separation,
  per the standing C7 approximation.

JSON trio (copy the `monster_island` trio and rename — it is the same anchor + biome):

- `RES:worldgen/structure/gold_fish_bowl.json` — `"type": "orespawn:legacy_dungeon"`,
  `"dungeon_type": "GOLD_FISH_BOWL"`, `"biomes": "minecraft:ocean"`,
  `"step": "surface_structures"`, `"terrain_adaptation": "none"`, `"spawn_overrides": {}`.
- `RES:worldgen/structure_set/gold_fish_bowl.json` — random_spread, spacing 46, separation 23,
  salt 84352.
- No biome tag file needed (vanilla tag inline, matching monster_island).

## 8. DungeonSpawnerBlock outcome

- Original: `if (type == 17)` → `makeGoldFishBowl(world, clickedX, clickedY, clickedZ)` — one
  call, no offset, block read in full (DSB:104-106).
- Port: add to `RandomDungeonSpawnerBlockEntity` (src/main/java/danger/orespawn/block/entity/
  RandomDungeonSpawnerBlockEntity.java): constant
  `TYPE_GOLD_FISH_BOWL = 17` (cite DSB:104-106) and case
  `TYPE_GOLD_FISH_BOWL -> LegacyDungeonPiece.buildNow(server, pos, DungeonType.GOLD_FISH_BOWL)`
  in `buildForType`. Type 17 currently falls through to the generic-dungeon fallback
  (RandomDungeonSpawnerBlockEntity.java:186).
- DSB path faithfully bypasses the ocean scan — a fishbowl embedded in terrain or floating
  wherever the spawner block sat is original behavior (same ruling as FairyTree/Hospital).

## 9. World-block READS mid-build

**None.** The only world read inside GD:2408-2488 is the post-placement spawner tile-entity fetch
(`getSpawnerTileEntity` GD:2484 → `func_147438_o`), which the port's `piece.placeSpawner` helper
already absorbs. No `func_147439_a` block reads, so the RNG-stitching "never branch on world
state" rule is satisfied with no in-memory model and no deviation decision.

## 10. RNG stream

**Empty.** The builder consumes zero random draws (verified over GD:2408-2488) — the geometry,
spawner position, and mob name are all constants. Every per-chunk replay pass is trivially
identical; there is no draw-order contract to maintain and no PARITY note needed beyond this
statement. (The dispatch-layer rolls — OSW:285 picker on `world.rand`, OSW:1177 gate on the chunk
`random`, DSB:52 on `world.rand` — all live outside the builder and map to structure-set
placement / the DSB roll, as usual.)

## 11. Surprises / MISSING-IN-PORT

- **S1 (MISSING-IN-PORT)**: `makeGoldFishBowl` has no counterpart anywhere in
  `src/main/java/danger/orespawn/` (grep `GoldFish|gold_fish`: only the entity, egg, cage,
  config, and spawn-block hits — no structure/DungeonType/DSB case). Worldgen path and DSB
  type 17 both fall through today.
- **S2**: No chest, no loot, no boss — the entire reward is one Gold Fish spawner. Do not invent
  loot.
- **S3**: The 7×7 glass walls start at `j=+2` (GD:2420), one above the 5×5 base plate at `j=+1`
  (GD:2412) — the base's perimeter ring is never written, so at worldgen the sand/water layers'
  glass ring rests directly on whatever is at sea-surface +1 around the base plate. Faithful;
  do not "complete" the ring.
- **S4**: The 5×5 lid at `j=+8` (GD:2473-2479) is inset one block from the 7×7 walls — the
  wall-top ring at `j=+8` is never written, leaving an open 1-wide ledge/gap around the lid at
  wall-top height (walls end at `j=+7`, GD:2462). Also faithful.
- **S5**: The glowstone corners (GD:2439-2451) overwrite four interior WATER cells of the `j=+3`
  layer placed instants earlier (GD:2429-2438), and the spawner (GD:2483) overwrites a loop-6
  air cell — preserve write order (or place them after their layers; same result).
- **S6**: The builder is 100% deterministic (§10) — unique among the D6 structures extracted so
  far; a template COULD express it, but per the pattern doc §0 decision rule it still goes
  through `LegacyDungeonStructure` (multi-chunk 7×7 footprint can straddle a chunk border, and
  the pipeline gives /locate + DSB `buildNow` for free).
- **S7**: `OreSpawnMain.GoldFishEnable` (OSM:516, config OSM:6453) gates only the Utopia biome
  natural-spawn lists (BiomeGenUtopianPlains.java:119-120, 175-176, 367-368) — it does NOT gate
  the bowl's worldgen, the DSB roll, or the spawner. The port's `GOLD_FISH_ENABLE`
  (OreSpawnConfig.java:99) mirrors that scope via `ModSpawnControl` (ModSpawnControl.java:160);
  the structure needs no config check.
- **S8**: Mixed RNG sources in the dispatch layers (same shape as hospital spec S9): the OSW:285
  6-way picker uses `world.rand` while the OSW:1177 gate uses the chunk-provided `random`;
  irrelevant to the port since both collapse into structure-set frequency.
- **S9**: Worldgen anchoring reuses `OCEAN_SURFACE` verbatim (§6) — the scan is byte-identical to
  Monster Island's; the two structures differ only in odds (1/350 vs 1/300 gate) and Y-origin
  use (both anchor at the water block; bowl builds from +1 up, island replaces the water at 0/−1).
