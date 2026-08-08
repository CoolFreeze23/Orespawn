# Ender Dragon Hospital + Monster Island — Extraction Spec (1.7.10 → NeoForge 1.21.1)

Source of truth: `reference_1_7_10_source/sources/danger/orespawn/GenericDungeon.java` —
`makeEnderDragonHospital` (GD:2815-2991) and `makeMonsterIsland` (GD:5170-5240).
All coordinates are relative to the build origin `(cposx, cposy, cposz)` = the three int args of each method.

Citation convention: `GD:NN` = GenericDungeon.java line NN, `OSW:NN` = OreSpawnWorld.java,
`OSM:NN` = OreSpawnMain.java, `DSB:NN` = DungeonSpawnerBlock.java. Port citations name the port file + line.

Shared plumbing (both structures):
- `FastSetBlock(world, x, y, z, block)` (GD:187-189) → `OreSpawnMain.setBlockFast(..., meta=0, flags=2)` —
  direct chunk write, send-to-client, **no neighbor updates**. Modern: `level.setBlock(pos, state, Block.UPDATE_CLIENTS)`
  = the port's `piece.place(...)` helper (phase_d_reports/structure_conversion_pattern.md, step 3 table).
- Spawner/chest blocks are placed with `world.func_147465_d(x, y, z, block, 0, 2)` (setBlock meta 0, flag 2),
  then the tile entity is fetched back via `getSpawnerTileEntity` (GD:86-95) / `getChestTileEntity` (GD:75-84),
  both of which call `world.func_147438_o` (getTileEntity). These TE fetches are the **only world reads inside
  either builder** — the standard pattern already absorbed by the port's `piece.placeSpawner` /
  `piece.placeLootChest` helpers. **Neither method reads a world BLOCK mid-build** (no `func_147439_a` calls
  inside GD:2815-2991 or GD:5170-5240), so the RNG-stitching "never branch on world state" rule
  (structure_conversion_pattern.md §1 step 3, rule 2) is satisfiable without an in-memory model.

---

# PART A — Ender Dragon Hospital (`makeEnderDragonHospital`, GD:2815-2991)

## A1. Entry points

| Caller | Signature used | Coords passed | Notes |
|---|---|---|---|
| `OreSpawnWorld.addHospital` (OSW:1551) | `OreSpawnMain.MyDungeon.makeEnderDragonHospital(world, posX, posY, posZ)` | scan hit, **no Y offset** | worldgen path, **The End only** (see §A8) |
| `DungeonSpawnerBlock` type **24** (DSB:125-127) | `...makeEnderDragonHospital(world, clickedX, clickedY, clickedZ)` | player-placed block pos, no offset | `type = world.rand.nextInt(50)` (DSB:52), fires on the scheduled tick 400 ticks after placement (DSB:39) |

## A2. THE ENDER DRAGON — what actually happens

**No Ender Dragon entity is spawned, referenced, health-set, or NBT-tagged anywhere in this method.**
The only concrete entity class referenced in all of GenericDungeon.java is `EntityEnderCrystal` (import
GD:11; uses GD:2906, 2910, 2914, 2918) — other methods spawn entities only via `EntityList` name lookups
("Girlfriend" GD:3728, "Robot Spider" GD:7038, "Robot Red Ant" GD:7064), none of them in this method and
none a dragon. The method's name is descriptive: it builds a *hospital FOR ender dragons* — four End
Crystals on corner pillars, which heal any vanilla `EntityDragon` that flies within range via the dragon's
own vanilla crystal-healing AI. There is no dragon NBT, no health handling, no `EntityList` lookup.
Verified by grep over the whole file: zero `EntityDragon` / `"Ender Dragon"` matches.

Per-crystal spawn sequence (GD:2906-2921), repeated 4×:
1. `new EntityEnderCrystal(world)` (GD:2906).
2. `func_70012_b(x, y, z, world.field_73012_v.nextFloat() * 360.0f, 0.0f)` — setLocationAndAngles with a
   **random yaw drawn from `world.rand`**, pitch 0 (GD:2907).
3. `world.func_72838_d(entity)` — spawnEntityInWorld (GD:2908).
4. **THEN** `FastSetBlock(..., Blocks.field_150357_h)` — a **bedrock** block is written into the very cell
   the crystal was just spawned at (GD:2909, 2913, 2917, 2921). See surprise S3.

| # | Crystal entity position (double) | Bedrock block written after | Cite |
|---|---|---|---|
| 1 | `(cposx+0.5, cposy+9, cposz+0.5)` | `(cposx+0, cposy+9, cposz+0)` | GD:2906-2909 |
| 2 | `(cposx+0.5, cposy+9, cposz+9.5)` | `(cposx+0, cposy+9, cposz+9)` | GD:2910-2913 |
| 3 | `(cposx+9.5, cposy+9, cposz+0.5)` | `(cposx+9, cposy+9, cposz+0)` | GD:2914-2917 |
| 4 | `(cposx+9.5, cposy+9, cposz+9.5)` | `(cposx+9, cposy+9, cposz+9)` | GD:2918-2921 |

RNG draw order for stitching parity: yaw₁ → yaw₂ → yaw₃ → yaw₄ (each immediately before its spawn),
all AFTER the geometry loops and BEFORE the 8 spawner placements and the chest fill.
Port mapping: vanilla `EntityType.END_CRYSTAL`; use a gated entity-spawn helper with caller-drawn yaw
(same contract as `piece.spawnPersistent`, structure_conversion_pattern.md step 3 — note EndCrystal is not
a `Mob`, so `setPersistenceRequired` does not apply and the original calls no persistence method either).

## A3. Geometry — per-loop table (all ranges inclusive)

Assignment order within loop 1 matters (later `if`s override earlier ones):
`air` → perimeter `iron bars` → corners `obsidian` → `j==0` end stone → `j==6 && perimeter` end stone.

| # | What | Where (relative, inclusive) | Block | Cite |
|---|---|---|---|---|
| 1a | Floor | `(0..9, 0, 0..9)` | end stone (`field_150377_bs`) — j==0 overrides all | GD:2843-2845 |
| 1b | Cage walls | perimeter `i∈{0,9} or k∈{0,9}`, `j = 1..5` | iron bars (`field_150411_aY`) | GD:2828-2830 |
| 1c | Corner posts | `(0,1..5,0), (0,1..5,9), (9,1..5,0), (9,1..5,9)` | obsidian (overrides iron bars) | GD:2831-2842 |
| 1d | Top rim | perimeter at `j = 6` (incl. corners — j==6 rule wins over obsidian) | end stone | GD:2846-2848 |
| 1e | Interior | everything else in `(0..9, 1..6, 0..9)` | air | GD:2827 |
| 2 | Dome ring 1 | `j = 7`, `i,k = 1..8`; edge `i∈{1,8} or k∈{1,8}` = block, interior air | `OreSpawnMain.MyEyeOfEnderBlock` | GD:2853-2862 |
| 3 | Dome ring 2 | `j = 8`, `i,k = 2..7`; edge `{2,7}` = block, interior air | MyEyeOfEnderBlock | GD:2863-2872 |
| 4 | Dome ring 3 | `j = 9`, `i,k = 3..6`; edge `{3,6}` = block, interior air (top opening = 2×2 air at `i,k = 4..5`) | MyEyeOfEnderBlock | GD:2873-2882 |
| 5 | Entrance ramp, 6 steps | start `i=-6, j=1, k=3`; per step m=0..5: end stone at `(i, j, k..k+3)`; iron bars at `(i, j+1, 3)` and `(i, j+1, 6)`; glowstone (`field_150426_aN`) at `(i, j+2, 3)` and `(i, j+2, 6)`; then `i++, j++` → steps at `(-6,1)…(-1,6)` | end stone + iron bars + glowstone | GD:2883-2897 |
| 6 | Corner towers | `(0/9, 7, 0/9)` and `(0/9, 8, 0/9)` — 4 columns, 2 high | obsidian | GD:2898-2905 |
| 7 | Crystal caps | `(0/9, 9, 0/9)` — 4 blocks (written after each crystal spawn) | bedrock | GD:2909-2921 |

Net shape: a 10×10 iron-bar cage (floor + rim end stone) under a 3-step eye-of-ender ziggurat dome with a
2×2 opening at the top center; obsidian corner towers rise to bedrock-capped crystal perches at y+9; a
4-wide stair ramp climbs from `(-6, 1)` up to the west rim. The ramp does NOT carve a doorway through the
iron-bar wall — the cage interior is only reachable through the 2×2 dome opening (see surprise S5).

## A4. Spawners (all placed with `func_147465_d(..., Blocks.field_150474_ac, 0, 2)` then `func_98272_a(name)`)

| # | Position (rel) | Mob name string | Cite |
|---|---|---|---|
| 1 | `(3, 9, 3)` | `"Ender Reaper"` | GD:2922-2929 |
| 2 | `(3, 9, 6)` | `"Ender Reaper"` | GD:2930-2937 |
| 3 | `(6, 9, 3)` | `"Ender Reaper"` | GD:2938-2945 |
| 4 | `(6, 9, 6)` | `"Ender Reaper"` | GD:2946-2953 |
| 5 | `(1, 1, 1)` | `"Nightmare"` | GD:2954-2961 |
| 6 | `(1, 1, 8)` | `"Nightmare"` | GD:2962-2969 |
| 7 | `(8, 1, 1)` | `"Nightmare"` | GD:2970-2977 |
| 8 | `(8, 1, 8)` | `"Nightmare"` | GD:2978-2985 |

The 4 Ender Reaper spawners sit at the CORNERS of the j=9 dome ring — i.e. they **overwrite four
MyEyeOfEnderBlock blocks** placed by loop 4 (GD:2877-2878 put eye blocks at edge `{3,6}`; GD:2925/2933/2941/2949
then replace `(3,3),(3,6),(6,3),(6,6)`). Port must preserve this write order (or just place spawners last, same result).
The 4 Nightmare spawners are inside the cage at floor level, just inside each corner post.

## A5. Chest + loot — FULL transcription

One chest: `(4, 1, 4)` (GD:2986), filled with `WeightedRandomChestContent.func_76293_a(world.rand,
HospitalContentsList, chest, 6 + world.rand.nextInt(5))` → **6-10 weighted pulls**, each into a random slot
(collisions overwrite) (GD:2987-2990).

`HospitalContentsList` (GD:44) — constructor semantics `(item, meta=0, minStack, maxStack, weight)`.
**Total weight = 210.**

| # | 1.7.10 item | Modern / port mapping (cite) | min | max | weight |
|---|---|---|---|---|---|
| 1 | `Item.getItemFromBlock(Blocks.field_150477_bB)` | `minecraft:ender_chest` | 2 | 4 | 35 |
| 2 | `Item.getItemFromBlock(Blocks.field_150484_ah)` | `minecraft:diamond_block` | 2 | 4 | 35 |
| 3 | `Item.getItemFromBlock(Blocks.field_150380_bt)` | `minecraft:dragon_egg` | 1 | 1 | 35 |
| 4 | `OreSpawnMain.MyEnderPearlBlock` ("blockenderpearl", OSM:1972) | port `ModBlocks.BLOCK_ENDER_PEARL` "block_ender_pearl" (ModBlocks.java:57) | 3 | 6 | 35 |
| 5 | `Items.field_151079_bi` | `minecraft:ender_pearl` | 2 | 4 | 35 |
| 6 | `Items.field_151061_bv` | `minecraft:ender_eye` | 2 | 4 | 35 |

(Note: near-identical to `EnderCastleContentsList` GD:40, but the Hospital list DROPS the
MyEyeOfEnderBlock and MyExperienceCatcher entries — do not copy the castle table.)

## A6. Block palette

| 1.7.10 field | Modern block | Used for | Port cite |
|---|---|---|---|
| `Blocks.field_150350_a` | `minecraft:air` | cage interior, dome interior | vanilla |
| `Blocks.field_150411_aY` | `minecraft:iron_bars` | cage walls, ramp railings | vanilla |
| `Blocks.field_150343_Z` | `minecraft:obsidian` | corner posts j1-5, corner towers j7-8 | vanilla |
| `Blocks.field_150377_bs` | `minecraft:end_stone` | floor, top rim, ramp treads | vanilla |
| `Blocks.field_150426_aN` | `minecraft:glowstone` | ramp railing caps | vanilla |
| `Blocks.field_150357_h` | `minecraft:bedrock` | 4 crystal caps at j9 | vanilla |
| `Blocks.field_150474_ac` | `minecraft:spawner` | 8 spawners | vanilla |
| `Blocks.field_150486_ae` | `minecraft:chest` | 1 loot chest | vanilla |
| `OreSpawnMain.MyEyeOfEnderBlock` ("blockeyeofender", OSM:1973) | port `ModBlocks.BLOCK_EYE_OF_ENDER` "block_eye_of_ender" | 3 dome rings | ModBlocks.java:59 |

## A7. Footprint extents (relative to `(cposx, cposy, cposz)`)

| Axis | Min | Max | Extent | Determined by |
|---|---|---|---|---|
| X | `-6` | `+9` | **16** | ramp start `i=-6` (GD:2883) / cage east wall `i=9` (GD:2824) |
| Y | `0` | `+9` | **10 blocks** (crystal entities render up to ≈ `+11`; EnderCrystal hitbox 2.0 tall) | floor j=0 (GD:2843) / bedrock caps j=9 (GD:2909) |
| Z | `0` | `+9` | **10** | cage walls (GD:2825); ramp occupies only `k=3..6` (GD:2885-2894) |

Suggested `DungeonType` box (with 1 margin): X `-7..+10`, Z `-1..+10`, down 1, up 12.

## A8. Worldgen call context — The End only

- Dispatch: `OreSpawnWorld.generate` switch on `world.provider.dimensionId`, `case 1:` →
  `generateEnd(world, random, chunkX*16, chunkZ*16)` (OSW:219-221). The Hospital is **End-exclusive** in
  worldgen — it appears in no overworld, Nether, or OreSpawn-dimension table (only other path is DSB type 24).
- `generateEnd` (OSW:226-241): always `addEndAnts` (empty method, OSW:1509-1510), then
  `i = world.field_73012_v.nextInt(4)` (OSW:228 — note: **world rand**, not the passed `random`):
  `i==0` EndKnights, `i==1` EndReapers, **`i==2` → addHospital (OSW:235-237)**, `i==3` EnderCastle.
- `addHospital(world, random, chunkX, chunkZ)` (OSW:1542-1555):
  1. Gate: `random.nextInt(25) != 0 → return` (OSW:1543) — 1/25.
  2. Up to 3 attempts (OSW:1546): `posX = chunkX + random.nextInt(16)`, `posZ = chunkZ + random.nextInt(16)` (OSW:1547-1548).
  3. Column scan `posY = 90` down to `11` (OSW:1549): require `world.func_147437_c(posX, posY, posZ)` (air at pos)
     AND `world.func_147439_a(posX, posY-1, posZ) == Blocks.field_150377_bs` (**end stone directly below**)
     AND `quickSpaceCheck(world, posX, posY, posZ)` (OSW:1550).
  4. Hit → `makeEnderDragonHospital(world, posX, posY, posZ)` and `return` (OSW:1551-1552).
     **`recently_placed` is NOT set** (contrast `addMonsterIsland` OSW:1411) — End structures don't touch the cooldown.
- `quickSpaceCheck` (OSW:2625-2633): 12×12 probe — every block at `(posX-2..posX+9, posY+4, posZ-2..posZ+9)`
  must be air, else reject. (World READ, but it's a placement scan, not a mid-build read → maps to a
  PlacementMode in `findGenerationPoint`, per structure_conversion_pattern.md step 4.)
- Effective odds: 1/4 × 1/25 = **1/100 per End chunk** before scan success. Structure-set conversion per the
  C7 equivalence: spacing ≈ √100 = 10, separation 5.

---

# PART B — Monster Island (`makeMonsterIsland`, GD:5170-5240)

## B1. Entry points

| Caller | Signature used | Coords passed | Notes |
|---|---|---|---|
| `OreSpawnWorld.addMonsterIsland` (OSW:1410) | `OreSpawnMain.MyDungeon.makeMonsterIsland(world, posX, posY - 1, posZ)` | scan hit, **Y offset −1** (cposy = the found WATER block's Y → sand fills the water surface) | worldgen path, **overworld Ocean only** (see §B7) |
| `DungeonSpawnerBlock` type **37** (DSB:164-166) | `...makeMonsterIsland(world, clickedX, clickedY, clickedZ)` | player-placed block pos, no offset | same roll mechanism as type 24 (DSB:52) |

## B2. Mob pick — FIRST RNG draw

`String monster = "Sea Viper"` (GD:5176); `if (world.field_73012_v.nextInt(2) == 0) monster = "Sea Monster"`
(GD:5178-5180). **One draw, before any block is placed; all 4 spawners share the same mob** — 50% all-Sea-Monster,
50% all-Sea-Viper. For stitching parity this draw must stay first in the sequence.

## B3. Geometry — per-loop table (all ranges inclusive)

| # | What | Where (relative, inclusive) | Block | Cite |
|---|---|---|---|---|
| 1 | Island body, lens-shaped: for `i = -5..5`, half-width `k = 3` (default), `k = 1` at `i = ±5`, `k = 2` at `i = ±4` and `i = ±3`; for `j = -k..k`: sand at `(i, 0, j)`, stone at `(i, -1, j)` | X `-5..5`, Z `-3..3` | sand (`field_150354_m`) top layer, stone (`field_150348_b`) beneath | GD:5181-5196 |
| 2 | Canopy slab | `(-2..2, +3, -2..2)` — 5×5 | leaves (`field_150362_t`) | GD:5197-5201 |
| 3 | Canopy tip | `(0, +4, 0)` | leaves | GD:5202 |
| 4 | Trunk | `(0, +1, 0)`, `(0, +2, 0)`, `(0, +3, 0)` | log (`field_150364_r`) | GD:5203-5205 |
| 5 | Diagonal branches | `(+1, +3, +1)`, `(-1, +3, -1)`, `(+1, +3, -1)`, `(-1, +3, +1)` | log | GD:5206-5209 |

(Loops 4-5 overwrite canopy leaves from loop 2 at those cells; write order is leaves → logs.)

## B4. Spawners

All four in the canopy at `j = +3`, overwriting loop-2 leaves; all use the single `monster` string from §B2.

| # | Position (rel) | Mob | Cite |
|---|---|---|---|
| 1 | `(+1, +3, 0)` | `monster` | GD:5210-5214 |
| 2 | `(-1, +3, 0)` | `monster` | GD:5215-5219 |
| 3 | `(0, +3, +1)` | `monster` | GD:5220-5224 |
| 4 | `(0, +3, -1)` | `monster` | GD:5225-5229 |

## B5. Chests + loot — FULL transcription

Two chests flanking the trunk, each filled with `func_76293_a(world.rand, MonsterIslandContentsList, chest,
4 + world.rand.nextInt(5))` → **4-8 weighted pulls each**:

| Chest | Position (rel) | Cite |
|---|---|---|
| 1 | `(0, +1, -1)` | GD:5230-5234 |
| 2 | `(0, +1, +1)` | GD:5235-5239 |

`MonsterIslandContentsList` (GD:30) — `(item, meta=0, minStack, maxStack, weight)`. **Total weight = 450.**

| # | 1.7.10 item | Modern / port mapping (cite) | min | max | weight |
|---|---|---|---|---|---|
| 1 | `Item.getItemFromBlock(OreSpawnMain.CreeperRepellent)` (OSM:1930, "creeperrepellent") | port `ModItems.CREEPER_REPELLENT_ITEM` "creeper_repellent" (ModItems.java:44; block ModBlocks.java:144) | 4 | 10 | 35 |
| 2 | `Item.getItemFromBlock(OreSpawnMain.KrakenRepellent)` (OSM:1928, "krakenrepellent") | port `ModItems.KRAKEN_REPELLENT_ITEM` "kraken_repellent" (ModItems.java:43; block ModBlocks.java:141) | 4 | 10 | 35 |
| 3 | `Items.field_151100_aR` (dye, **meta 0 = ink sac**) | `minecraft:ink_sac` | 6 | 16 | 25 |
| 4 | `Items.field_151103_aS` | `minecraft:bone` | 6 | 16 | 25 |
| 5 | `Items.field_151007_F` | `minecraft:string` | 6 | 16 | 25 |
| 6 | `Items.field_151147_al` | `minecraft:porkchop` (raw) | 3 | 10 | 35 |
| 7 | `Items.field_151082_bd` | `minecraft:beef` (raw) | 3 | 10 | 35 |
| 8 | `Items.field_151076_bf` | `minecraft:chicken` (raw) | 3 | 10 | 35 |
| 9 | `Items.field_151115_aP` | `minecraft:cod` (raw fish, meta 0) | 3 | 10 | 35 |
| 10 | `Items.field_151078_bh` | `minecraft:rotten_flesh` | 3 | 10 | 35 |
| 11 | `Items.field_151062_by` | `minecraft:experience_bottle` | 4 | 10 | 35 |
| 12 | `OreSpawnMain.MyRawBacon` (OSM:1851, unloc "bacon") | port `ModItems.RAW_BACON` "raw_bacon" (ModItems.java:536) | 6 | 16 | 35 |
| 13 | `OreSpawnMain.MyRawPeacock` (OSM:1874, unloc "rawpeacock") | port `ModItems.RAW_PEACOCK` "raw_peacock" (ModItems.java:552) | 6 | 16 | 35 |
| 14 | `Item.getItemFromBlock(Blocks.field_150364_r)` (log, meta 0) | `minecraft:oak_log` | 6 | 16 | 25 |

## B6. Block palette

| 1.7.10 field | Modern block | Used for | Port cite |
|---|---|---|---|
| `Blocks.field_150354_m` | `minecraft:sand` | island surface | vanilla |
| `Blocks.field_150348_b` | `minecraft:stone` | island underlayer | vanilla |
| `Blocks.field_150362_t` | `minecraft:oak_leaves` (meta 0) | canopy | vanilla |
| `Blocks.field_150364_r` | `minecraft:oak_log` (meta 0) | trunk + branches | vanilla |
| `Blocks.field_150474_ac` | `minecraft:spawner` | 4 spawners | vanilla |
| `Blocks.field_150486_ae` | `minecraft:chest` | 2 loot chests | vanilla |

## B7. Worldgen call context — overworld Ocean only

- Dispatch: `generateSurface` runs for vanilla dim 0 (OSW:214-215) AND for `OreSpawnMain.DimensionID`
  (OSW:40-41), but the whole dungeon-picker block is gated by
  `OreSpawnMain.DisableOverworldDungeons == 0 && world.provider.dimensionId == 0 && recently_placed == 0`
  (OSW:284) — so Monster Island worldgen is **vanilla-overworld-exclusive** (config-disableable).
- Picker: `i = world.field_73012_v.nextInt(6)` (OSW:285 — world rand): 0 PlayPool, 1 WaterDragonLair,
  2 GoldFishBowl, 3 GirlfriendIsland, **4 → addMonsterIsland (OSW:298-300)**, 5 FrogPond.
- `addMonsterIsland(world, random, chunkX, chunkZ)` (OSW:1398-1416):
  1. Gate: `random.nextInt(300) != 0 → return` (OSW:1399) — 1/300.
  2. Biome: `world.func_72807_a(chunkX, chunkZ)` (block coords = chunk corner) must have `biomeName`
     **exactly `"Ocean"`** (OSW:1402-1403) — excludes Deep Ocean, FrozenOcean, beaches.
  3. Up to 4 attempts (OSW:1404): random `posX/posZ` in chunk (OSW:1405-1406).
  4. Column scan `posY = 100` down to `41` (OSW:1408): require `block(posX, posY, posZ) == air` AND
     `block(posX, posY-1, posZ) == Blocks.field_150355_j` (**still water** directly below) (OSW:1409).
  5. Hit → `makeMonsterIsland(world, posX, posY - 1, posZ)` — origin is the water-surface block, so the sand
     layer replaces surface water — then `recently_placed = 50` and return (OSW:1410-1412).
- `recently_placed` is the global static 50-chunk cooldown shared by all structures (OSW:30, 37-39).
- Effective odds: 1/6 × 1/300 = **1/1800 per overworld chunk whose corner biome is Ocean**.
  Structure-set conversion: spacing ≈ √1800 ≈ 42, separation 21, biome filter `minecraft:ocean`
  (match the GirlfriendIsland treatment — identical scan at OSW:1378-1396).

## B8. Footprint extents (relative to `(cposx, cposy, cposz)`)

| Axis | Min | Max | Extent | Determined by |
|---|---|---|---|---|
| X | `-5` | `+5` | **11** | island body loop (GD:5181) |
| Y | `-1` | `+4` | **6** | stone underlayer (GD:5194) / canopy tip (GD:5202) |
| Z | `-3` | `+3` | **7** | half-width k=3 (GD:5182); canopy only reaches ±2 |

Suggested `DungeonType` box (with 1 margin): X `-6..+6`, Z `-4..+4`, down 2, up 5.

---

# C. DungeonSpawnerBlock type table — the "pairing" verified

**The two structures are NOT paired under type 24.** The task brief's premise does not match the source:

- `type == 24` → **only** `makeEnderDragonHospital(world, clickedX, clickedY, clickedZ)` — one call (DSB:125-127).
- `type == 37` → **only** `makeMonsterIsland(world, clickedX, clickedY, clickedZ)` — one call (DSB:164-166).
- Both use the identical `(clickedX, clickedY, clickedZ)` position args with no offset (unlike types 43/44/45,
  which pass `clickedY + 1`, DSB:182-190). Every type in DSB:52-202 makes exactly one builder call;
  no type makes two.
- Shared trigger mechanics: block scheduled 400 ticks after placement (DSB:39), server-side only (DSB:47-49),
  deletes itself + the block above first (DSB:50-51), then one `world.rand.nextInt(50)` roll (DSB:52).

Port status of the trigger: `RandomDungeonSpawnerBlockEntity.buildForType`
(src/main/java/danger/orespawn/block/entity/RandomDungeonSpawnerBlockEntity.java:104-135) has cases for
2/21/22/23/38/47 only; **types 24 and 37 currently fall through to the generic-dungeon fallback**
(RandomDungeonSpawnerBlockEntity.java:133-134) — register both here when the generators land.

# D. Mob mapping table

| Spawner name / entity | 1.7.10 class (registration) | Port EntityType (cite) |
|---|---|---|
| `"Ender Reaper"` | `EnderReaper` (OSM:4133-4137) | `ModEntities.ENDER_REAPER` "ender_reaper" (ModEntities.java:89-91) |
| `"Nightmare"` | `PitchBlack` (OSM:4023-4027) | `ModEntities.PITCH_BLACK` "pitch_black" (ModEntities.java:113-115) |
| `"Sea Viper"` | `SeaViper` (OSM:4345-4349) | `ModEntities.SEA_VIPER` "sea_viper" (ModEntities.java:145-147) |
| `"Sea Monster"` | `SeaMonster` (OSM:4337-4341) | `ModEntities.SEA_MONSTER` "sea_monster" (ModEntities.java:141-143) |
| `EntityEnderCrystal` (direct spawn ×4, GD:2906-2921) | vanilla | vanilla `EntityType.END_CRYSTAL` |

# E. Surprises / MISSING-IN-PORT

- **S1 (MISSING-IN-PORT)**: neither `makeEnderDragonHospital` nor `makeMonsterIsland` exists anywhere in
  `src/main/java/danger/orespawn/` (grep: zero matches). No worldgen counterpart, no DSB case (§C).
- **S2**: **No Ender Dragon is spawned** despite the method name — only 4 End Crystals with random yaw; no
  entity NBT or health handling of any kind exists in the method (§A2). Do not invent a dragon spawn.
- **S3**: Each End Crystal is spawned at `y = cposy+9` and THEN a bedrock block is written into that same
  cell (GD:2906-2909 etc.) — the crystal ends up embedded in/coincident with its bedrock cap, unlike vanilla
  pillars (crystal one block above bedrock). Replicate coords and order faithfully; do not "fix" to +10.
- **S4**: The 4 Ender Reaper spawners overwrite the 4 corner MyEyeOfEnderBlock blocks of the j=9 dome ring
  placed moments earlier (GD:2877-2878 vs 2925/2933/2941/2949).
- **S5**: The hospital ramp leads to the top rim but no doorway is carved through the iron-bar cage wall —
  the caged area (Nightmare spawners + chest) is only enterable via the 2×2 dome-top opening at `i,k=4..5, j=9`.
- **S6**: `addHospital` does NOT set `recently_placed` (OSW:1551-1552), unlike the overworld/Islands adds
  (e.g. OSW:1411) — End structures bypass the global cooldown.
- **S7**: Monster Island's mob pick is a single 50/50 roll shared by all four spawners (GD:5176-5180), drawn
  BEFORE any block write — first item in the RNG stream for stitching parity.
- **S8**: `Items.field_151100_aR` at meta 0 in the Monster Island loot is **ink sac**, not "dye"; log/leaves
  meta 0 map to oak; raw fish meta 0 maps to cod.
- **S9**: Mixed RNG sources in the dispatch layers: `generateEnd`'s 4-way and `generateSurface`'s 6-way rolls
  use `world.rand` (OSW:228, 285) while the `add*` gates use the chunk-provided `random` (OSW:1399, 1543);
  the builders themselves use `world.rand` for everything (yaws, mob pick, loot counts).
- **S10**: Worldgen dimension split — Hospital: The End only (end-stone-below scan + 12×12 air probe at y+4,
  no cooldown); Monster Island: vanilla overworld only, exact-"Ocean" biome corner check, water-surface
  anchoring with a −1 Y offset, sets the 50-chunk cooldown. The DSB path bypasses all scans for both.
- **No mid-build world-block reads in either builder** — only post-placement tile-entity fetches
  (GD:75-84, 86-95), already covered by the port's `placeSpawner`/`placeLootChest` helpers.
