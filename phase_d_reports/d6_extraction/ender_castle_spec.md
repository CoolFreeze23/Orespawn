# Ender Castle — Extraction Spec (1.7.10 → NeoForge 1.21.1)

Source of truth: `reference_1_7_10_source/sources/danger/orespawn/GenericDungeon.java`
— `makeEnderCastle` (GD:3207-3493) + private helper `makeAColumn` (GD:3495-3623);
TE fetch helpers `getChestTileEntity` (GD:75-84) / `getSpawnerTileEntity` (GD:86-95).

All coordinates below are **relative to `(cposx, cposy, cposz)`**, the three int args of
`makeEnderCastle`. In End worldgen `cposy` is the Y of an **air** block sitting directly on
end stone (OSW:1564-1566); in Islands (D4) worldgen `cposy` is the Y of the **grass** block
itself (OSW:2328-2338) — the base plate overwrites it.

Citation convention: `GD:NN` = GenericDungeon.java, `OSW:NN` = OreSpawnWorld.java,
`OSM:NN` = OreSpawnMain.java, `DSB:NN` = DungeonSpawnerBlock.java, `OGE:NN` = OreGenericEgg.java.
Port citations: `MB:NN` = src/main/java/danger/orespawn/ModBlocks.java, `MI:NN` = ModItems.java,
`ME:NN` = ModEntities.java, `LDP:NN` = world/structure/LegacyDungeonPiece.java,
`RDSBE:NN` = block/entity/RandomDungeonSpawnerBlockEntity.java.

Loot-list dump `phase_d_reports/d5_extraction/tmp/gd_constants.txt` was **not** relied on; the
`EnderCastleContentsList` declaration was transcribed directly from GD:40 (single physical line).

Block writes: almost everything goes through `OreSpawnMain.setBlockFast(world, x, y, z, block, 0, 2)`
(flag 2 = send-to-client, no neighbor updates; modern `level.setBlock(pos, state, Block.UPDATE_CLIENTS)`).
Exceptions using `world.func_147465_d(x, y, z, block, meta, 2)` (setBlock with metadata, flag 2):
the central ender chest (GD:3325), all 16 mob spawners (GD:3339/3344/3349/3354/3359/3364/3369/3374,
3437/3442, 3448/3453/3463/3468/3478/3483), and the 3 loot chests (GD:3458/3473/3488).

There is **no client-side bail** in `makeEnderCastle` (contrast `makeEnormousCastle` GD:199-201);
callers are all server-side.

Decompiler quirk seen throughout: CFR renders some `||` chains as bitwise `|`
(e.g. `i == -3 || i == width + 3 || k == width + 3 | k == -3`, GD:3226). Operands are booleans,
so semantics are identical to `||` (minus short-circuit); treat as plain OR.

---

## 1. Entry points

| Caller | Condition | Coords passed | Cite |
|---|---|---|---|
| `OreSpawnWorld.generateEnd` → `addEnderCastle` (worldgen, **The End**, dim 1) | `nextInt(4)==3` branch, then 1/50 roll | `(posX, posY_air_above_endstone, posZ)` | OSW:219-222 (dispatch), OSW:226-241 (branch), OSW:1557-1570 |
| `OreSpawnWorld` D4 dispatch → `addD4EnderCastle` (worldgen, **Islands**, DimensionID4) | `i == 7` of `random.nextInt(19)` | `(posX, posY_grass, posZ)` | OSW:132-143 (dispatch), OSW:2322-2343 |
| `DungeonSpawnerBlock` type **27** (player-placed random dungeon spawner) | `world.rand.nextInt(50) == 27` after 400-tick delay | block pos | DSB:52 (roll), DSB:134-136 |

DSB mechanics: on placement a scheduled tick is queued 400 ticks out (DSB:35-40); on that tick the
block deletes itself and the block above (DSB:50-51), then rolls `type = world.rand.nextInt(50)`
(DSB:52); `type == 27` → `OreSpawnMain.MyDungeon.makeEnderCastle(world, x, y, z)` (DSB:134-136).

**Port entry points: MISSING-IN-PORT — all three.** No `makeEnderCastle` / `ENDER_CASTLE` /
`ender_castle` exists anywhere in `src/main/java/danger/orespawn` (grep 2026-08-08). The
`LegacyDungeonPiece.DungeonType` enum (LDP:79+) has no Ender Castle entry; the port
`RandomDungeonSpawnerBlockEntity` pool has no `TYPE_...= 27` constant (RDSBE:43-52 — unported
indices currently fall back to the generic dungeon, RDSBE:30-33); and no file under
`src/main/resources/data` references `the_end` / `is_end` — **the port has no End-dimension
structure placement precedent at all** (see §11.1 for the biome facts the implementer needs).

---

## 2. Constants / setup (GD:3207-3218)

| Name | Value | Cite |
|---|---|---|
| `width` | 22 | GD:3212 |
| `height` | 12 | GD:3213 |
| `chestContents` | `this.EnderCastleContentsList` | GD:3217 |
| `width/2` | 11 (room/plaza center used throughout) | — |

All randomness inside the build uses `world.field_73012_v` (the **World's** RNG), never a passed-in
`Random` (GD:3244, 3273, 3275, 3461, 3476, 3491) — relevant for port determinism.

---

## 3. `makeEnderCastle` — step by step (GD:3207-3493)

| # | What | Where (inclusive, rel. cpos) | Block / content | Cite |
|---|---|---|---|---|
| 1 | Base plate | x `-3..25`, y `+0`, z `-3..25` | obsidian | GD:3219-3232 (j==0 branch GD:3223-3225) |
| 2 | Perimeter fence on plate edge | same loop, y `+1`, only cells with x∈{-3,25} or z∈{-3,25} | **iron bars**; all other y+1 cells → air | GD:3226-3228 |
| 3 | Main keep shell | x `0..22`, z `0..22`, y `+1..+12`; perimeter cells (x∈{0,22} or z∈{0,22}) | **bedrock** walls; interior → air | GD:3233-3239 |
| 4 | Crenellated wall top | wall cells at y `+12` with `(i+k)&1 == 0` | air (checker gaps) | GD:3240-3242 |
| 5 | Wall egg-block band | wall cells at y `+10` (= height−2) with `(i+k)&1 == 0` (44 cells) | each rolls `world.rand.nextInt(4)`: 0→`MyEnderKnightSpawnBlock`, 1→`MyEnderReaperSpawnBlock`, 2→`MyEndermanSpawnBlock`, 3→`MyEnderDragonSpawnBlock` | GD:3243-3257 |
| 6 | Eye-of-Ender band | wall cells at y `+7` with `(i+k)&1 != 0` (44 cells) | `MyEyeOfEnderBlock` | GD:3258-3260 |
| 7 | Outer parapet ring (never writes air — `continue` on air, GD:3288) | ring x∈{-1,23} or z∈{-1,23}, x,z `-1..23`, at y `+6` and y `+9..+11` | bedrock | GD:3265-3272, 3269 (`j == 6 \|\| j > 8`), loop bound j `1..11` GD:3267 |
| 8 | Hanging ender-pearl stalactites | under ring cells at y `+6`: 1/2 chance (`nextInt(2)==1`) → pearl block at y `+5`; then 1/3 chance (`nextInt(3)==1`) → second at y `+4` | `MyEnderPearlBlock` | GD:3273-3278 |
| 9 | Parapet walkway band | ring cells at y `+7`; checker `(i+k)&1 == 0` → air (not written), odd → bedrock | bedrock | GD:3280-3287 |
| 10 | 4 corner towers | `makeAColumn` at rel. `(-2, +0, -2)` dir 0, `(+20, +0, -2)` dir 1, `(-2, +0, +20)` dir 2, `(+20, +0, +20)` dir 3; height arg = `height+1` = **13** | see §4 | GD:3293-3296 |
| 11 | Upper floor (roof plaza) | x `1..21`, y `+8`, z `1..21` | obsidian, but **bedrock** where `i==11 \|\| k==11 \|\| i==k \|\| i==22-k` (cross + both diagonals) | GD:3297-3306 |
| 12 | Rooftop lava pool | x `9..13`, y `+9`, z `9..13` (5×5 centered on 11,11) | lava | GD:3307-3313 |
| 13 | Pool bedrock rim bits | y `+9`: `(10..12, 14)`, `(10..12, 8)`, `(14, 10..12)`, `(8, 10..12)` (m=-1..1); corners `(9,9)`, `(13,13)`, `(9,13)`, `(13,9)`; center `(11,11)` | bedrock | GD:3314-3324 |
| 14 | Central **ender chest** | `(11, +10, 11)`, metadata 2 (1.7.10 chest facing north) | `Blocks.ender_chest` — plain block, **no loot fill** | GD:3325 |
| 15 | Center pedestal spike | `(11,+11,11)` obsidian; `(11,+12,11)` bedrock; `(10..12,+12,11)` + `(11,+12,10..12)` bedrock cross; 4 torches at `(10,+13,11)`,`(12,+13,11)`,`(11,+13,10)`,`(11,+13,12)`; `(11,+13,11)` + `(11,+14,11)` bedrock; **dragon egg** at `(11,+15,11)` | see row | GD:3326-3338 |
| 16 | 8 rooftop spawners | 4 corners `(16,16)`, `(6,16)`, `(16,6)`, `(6,6)` (= 11±5): y `+9` = "Ender Reaper", y `+10` = "Ender Knight" | mob spawner | GD:3339-3378 |
| 17 | Ground-level ceiling plate | x `1..21`, y `+4`, z `1..21`, only where `i<=5 \|\| k<=5 \|\| i>=17 \|\| k>=17` (ring; interior 6..16 left open as light/drop shaft) | bedrock | GD:3379-3388 |
| 18 | Inner bar cage | at `i==5` (k `5..17`), `i==17` (k `5..17`), `k==5` (i `5..17`), `k==17` (i `5..17`): y `+5`,`+6`,`+7` | iron bars | GD:3389-3408 |
| 19 | Entry stairs (east side) | bedrock 3-wide steps at `(16, +3, 10..12)`, `(15, +2, 10..12)`, `(14, +1, 10..12)` | bedrock | GD:3410-3428 |
| 20 | Cage doorway | air carved at `(17, +5..+7, 10..12)` (hole in the i==17 bar wall) | air | GD:3429-3435 |
| 21 | Pit spawners (center of shaft floor) | `(11, +1, 11)` = "Ender Reaper", `(11, +2, 11)` = "Ender Knight" | mob spawner | GD:3436-3446 |
| 22 | West wall alcove | `(1, +5, 10)` + `(1, +5, 12)` = "CaveFisher" spawners; **chest** `(1, +5, 11)` meta 2, filled `6 + nextInt(5)` stacks from `EnderCastleContentsList` | see §6 | GD:3447-3462 |
| 23 | North wall alcove | `(10, +5, 1)` + `(12, +5, 1)` = "CaveFisher"; **chest** `(11, +5, 1)` meta 3 (south), same fill | see §6 | GD:3463-3477 |
| 24 | South wall alcove | `(10, +5, 21)` + `(12, +5, 21)` = "CaveFisher"; **chest** `(11, +5, 21)` meta 4 (west), same fill | see §6 | GD:3478-3492 |

No 4th (east) alcove — the east wall carries the stairs/doorway instead (rows 19-20).

Note on clearing: there is **no clear-envelope loop**. Interior air comes only from the shell loop
(row 3) writing air over `0..22 × +1..+12`, the plate loop writing air at y+1 over `-3..25`
(row 2), and the tower loops' own shaft/cap air writes (§4 rows 2-3). The parapet loop (row 7)
explicitly skips air writes (GD:3288). Outside terrain above y+1 in the `-4..-1` / `23..26`
margins is untouched except where the tower loops themselves write (shaft cells, caps at
+15/+16) — harmless in the End (void/air), but in D4/DSB contexts pre-existing blocks survive
next to the towers.

---

## 4. Helper `makeAColumn(world, x, y, z, height=13, dir)` (GD:3495-3623)

Local constants: `width = 4`, `halfwidth = 2`, `step = dir` (GD:3500-3502). All coords below are
relative to the column's own `(cposx, cposy, cposz)` (= main-castle rel. `(±const, +0, ±const)`
from §3 row 10). With height arg 13: `height+2 = 15`, `height+3 = 16`.

| # | What | Where (inclusive, rel. column origin) | Block | Cite |
|---|---|---|---|---|
| 1 | Cap plate | x `-2..6`, y `+15`, z `-2..6` | obsidian | GD:3503-3508 |
| 2 | Cap crenellation ring | x,z `-2..6` at y `+16`: ring cells (x∈{-2,6} or z∈{-2,6}) obsidian, but checker `(i+k)&1==0` → air; non-ring cells → air (written) | obsidian/air | GD:3509-3521 |
| 3 | Shaft walls | x `0..4`, z `0..4`, y `+1..+15`: perimeter (x∈{0,4} or z∈{0,4}) obsidian, interior air | obsidian | GD:3522-3528 |
| 4 | Window bars | wall cells where `(j%3==0 or j%3==1)` and `j != 15` and (`i==2` or `k==2`) (mid-face columns) | iron bars | GD:3529-3531 |
| 5 | Doorways | two 3-block-footprint openings per column, at y `+1..+2` (ground) and y `+9..+10` (parapet level), on the corner **facing the castle**: dir 0 → cells `(4,·,4)`,`(3,·,4)`,`(4,·,3)`; dir 1 → `(0,·,4)`,`(1,·,4)`,`(0,·,3)`; dir 2 → `(4,·,0)`,`(3,·,0)`,`(4,·,1)`; dir 3 → `(0,·,0)`,`(1,·,0)`,`(0,·,1)` | air | GD:3536-3598 |
| 6 | Spiral staircase | one **nether brick** block per y `+1..+15`, cycling positions step 0→`(1,·,1)`, 1→`(1,·,3)`, 2→`(3,·,3)`, 3→`(3,·,1)`, incrementing (wrap >3→0) each layer | nether brick | GD:3599-3622 |

Spiral phase: `step` starts at `dir`, then the doorway blocks mutate it before the spiral loop —
dir 0: no increment (start step 0); dir 1: one `++step` w/ wrap (GD:3559-3561, start 2); dir 2: two
(GD:3574-3579, 2→3→wrap 0, start 0); dir 3: two (GD:3592-3597, 3→wrap 0→1, start 1). So the four
towers' staircases start at different rotations. (The dir-2/dir-3 double-increment looks like a
decompiled copy-paste oddity but is real behavior — transcribe as-is.)

Column footprint per tower: x,z `-2..+6` local, y `+1..+16` (plus cap plate at +15). Against the
main castle: towers at `(-2,-2)`, `(20,-2)`, `(-2,20)`, `(20,20)` → absolute rel. extents
x,z `-4..26`.

---

## 5. Mob spawner inventory (16 total)

| # | Rel. pos | Mob string (`func_98272_a`) | Cite |
|---|---|---|---|
| 1 | (16, +9, 16) | "Ender Reaper" | GD:3339-3343 |
| 2 | (16, +10, 16) | "Ender Knight" | GD:3344-3348 |
| 3 | (6, +9, 16) | "Ender Reaper" | GD:3349-3353 |
| 4 | (6, +10, 16) | "Ender Knight" | GD:3354-3358 |
| 5 | (16, +9, 6) | "Ender Reaper" | GD:3359-3363 |
| 6 | (16, +10, 6) | "Ender Knight" | GD:3364-3368 |
| 7 | (6, +9, 6) | "Ender Reaper" | GD:3369-3373 |
| 8 | (6, +10, 6) | "Ender Knight" | GD:3374-3378 |
| 9 | (11, +1, 11) | "Ender Reaper" | GD:3437-3441 |
| 10 | (11, +2, 11) | "Ender Knight" | GD:3442-3446 |
| 11 | (1, +5, 10) | "CaveFisher" | GD:3448-3452 |
| 12 | (1, +5, 12) | "CaveFisher" | GD:3453-3457 |
| 13 | (10, +5, 1) | "CaveFisher" | GD:3463-3467 |
| 14 | (12, +5, 1) | "CaveFisher" | GD:3468-3472 |
| 15 | (10, +5, 21) | "CaveFisher" | GD:3478-3482 |
| 16 | (12, +5, 21) | "CaveFisher" | GD:3483-3487 |

All spawners are placed as `Blocks.field_150474_ac` (mob spawner) meta 0 flag 2 via
`func_147465_d`, then the TE is fetched via `getSpawnerTileEntity` → `world.func_147438_o`
(GD:86-95) and the entity name set with `func_145881_a().func_98272_a(name)`. No custom
spawner NBT beyond the entity id (default vanilla delays/counts/ranges).

There are **no direct entity spawns** (`spawnEntityInWorld`) anywhere in GD:3207-3623.

---

## 6. Chests

| # | Rel. pos | Block/meta | Loot | Fill count | Cite |
|---|---|---|---|---|---|
| 1 | (11, +10, 11) | `Blocks.ender_chest`, meta 2 | none (player ender chest) | — | GD:3325 |
| 2 | (1, +5, 11) | `Blocks.chest`, meta 2 (facing north) | `EnderCastleContentsList` | `6 + world.rand.nextInt(5)` = 6..10 weighted stacks | GD:3458-3462 |
| 3 | (11, +5, 1) | `Blocks.chest`, meta 3 (facing south) | `EnderCastleContentsList` | 6..10 | GD:3473-3477 |
| 4 | (11, +5, 21) | `Blocks.chest`, meta 4 (facing west) | `EnderCastleContentsList` | 6..10 | GD:3488-3492 |

Fill call: `WeightedRandomChestContent.func_76293_a(world.rand, chestContents, chest, count)`
(= `generateChestContents`, puts `count` weighted rolls into random slots). 1.7.10 chest meta →
facing: 2=north, 3=south, 4=west, 5=east. (Chest #2's meta-2/north on a west wall is faithful
even if visually odd.)

---

## 7. Loot table — FULL transcription

### `EnderCastleContentsList` (GD:40) — 8 entries. Total weight **270**.

`new WeightedRandomChestContent(item, meta, min, max, weight)`:

| # | 1.7.10 item | meta | min | max | weight | Port mapping |
|---|---|---|---|---|---|---|
| 1 | `Item.getItemFromBlock(Blocks.field_150477_bB)` (ender chest) | 0 | 2 | 4 | 35 | vanilla `Items.ENDER_CHEST` |
| 2 | `Item.getItemFromBlock(Blocks.field_150484_ah)` (block of diamond) | 0 | 2 | 4 | 35 | vanilla `Items.DIAMOND_BLOCK` |
| 3 | `Item.getItemFromBlock(Blocks.field_150380_bt)` (dragon egg) | 0 | 1 | 1 | 35 | vanilla `Items.DRAGON_EGG` |
| 4 | `Item.getItemFromBlock(OreSpawnMain.MyEnderPearlBlock)` (OSM:1972) | 0 | 3 | 6 | 35 | `ModBlocks.BLOCK_ENDER_PEARL` (MB:57-58) |
| 5 | `Item.getItemFromBlock(OreSpawnMain.MyEyeOfEnderBlock)` (OSM:1973) | 0 | 3 | 6 | 35 | `ModBlocks.BLOCK_EYE_OF_ENDER` (MB:59-60) |
| 6 | `OreSpawnMain.MyExperienceCatcher` (OSM:1948) | 0 | 4 | 10 | 25 | `ModItems.EXPERIENCE_CATCHER` (MI:622-623) |
| 7 | `Items.field_151079_bi` (ender pearl) | 0 | 2 | 4 | 35 | vanilla `Items.ENDER_PEARL` |
| 8 | `Items.field_151061_bv` (eye of ender) | 0 | 2 | 4 | 35 | vanilla `Items.ENDER_EYE` |

---

## 8. Block palette — modern mapping

| 1.7.10 field | Block | Modern (1.21.1) | Used at |
|---|---|---|---|
| `field_150343_Z` | obsidian | `Blocks.OBSIDIAN` | plate, plaza, towers, pedestal |
| `field_150350_a` | air | `Blocks.AIR` | interior/crenels/doorways |
| `field_150357_h` | bedrock | `Blocks.BEDROCK` | keep walls, parapet, stairs, pedestal |
| `field_150411_aY` | iron bars | `Blocks.IRON_BARS` | plate fence, cage, tower windows |
| `field_150353_l` | lava (still/stationary block) | `Blocks.LAVA` (source) | rooftop pool |
| `field_150385_bj` | nether brick block | `Blocks.NETHER_BRICKS` | tower spiral stairs |
| `field_150478_aa` | torch (standing) | `Blocks.TORCH` | 4 pedestal torches |
| `field_150477_bB` | ender chest | `Blocks.ENDER_CHEST` | center trophy (meta 2 → `FACING=NORTH`) |
| `field_150486_ae` | chest | `Blocks.CHEST` | 3 loot chests |
| `field_150474_ac` | mob spawner | `Blocks.SPAWNER` | 16 spawners |
| `field_150380_bt` | dragon egg | `Blocks.DRAGON_EGG` | pedestal top (also loot) |
| `OreSpawnMain.MyEnderPearlBlock` (OSM:1225/1972, reg name `OreSpawn_EnderPearlBlock` OSM:2186) | Ender-Pearl Block (an `OreGenericEgg`) | `ModBlocks.BLOCK_ENDER_PEARL` (MB:57-58) | stalactites, loot |
| `OreSpawnMain.MyEyeOfEnderBlock` (OSM:1226/1973, reg `OreSpawn_EyeOfEnderBlock` OSM:2187) | Eye-of-Ender Block (`OreGenericEgg`) | `ModBlocks.BLOCK_EYE_OF_ENDER` (MB:59-60) | wall band y+7, loot |
| `OreSpawnMain.MyEnderKnightSpawnBlock` (OSM:619/6323, reg `OreSpawn_EnderKnightSpawnBlock` OSM:2066) | "Ancient Dried Ender Knight Spawn Egg" (OSM:2878) | `ModBlocks.ENDER_KNIGHT_SPAWN_BLOCK` (MB:370) | wall band y+10 |
| `OreSpawnMain.MyEnderReaperSpawnBlock` (OSM:620/6324, reg OSM:2067) | "Ancient Dried Ender Reaper Spawn Egg" (OSM:2881) | `ModBlocks.ENDER_REAPER_SPAWN_BLOCK` (MB:372) | wall band y+10 |
| `OreSpawnMain.MyEndermanSpawnBlock` (OSM:546/6248, reg OSM:1993) | "Ancient Dried Enderman Spawn Egg" (OSM:2702) | `ModBlocks.ENDERMAN_SPAWN_BLOCK` (MB:228) | wall band y+10 |
| `OreSpawnMain.MyEnderDragonSpawnBlock` (OSM:557/6259, reg OSM:2028) | "Ancient Dried Ender Dragon Spawn Egg" (OSM:2735) | `ModBlocks.ENDER_DRAGON_SPAWN_BLOCK` (MB:250) | wall band y+10 |

The four "spawn egg" wall blocks and the pearl/eye blocks are all `OreGenericEgg` — **decorative**
blocks that do NOT spawn mobs; on break they have a 50% chance to drop `5 + nextInt(3) + nextInt(3)`
XP (OGE:24-30). The port already models this (MB:55-56 comment, MB:168-171 comment).

---

## 9. Mob mapping table (spawner names)

| Spawner string | 1.7.10 class (via OreSpawnMain registration) | Port EntityType | Cite |
|---|---|---|---|
| "Ender Reaper" | `EnderReaper` — `registerGlobalEntityID(EnderReaper.class, "Ender Reaper", ...)` OSM:4133 | `ModEntities.ENDER_REAPER` (ME:89-91) | — |
| "Ender Knight" | `EnderKnight` — OSM:4125 | `ModEntities.ENDER_KNIGHT` (ME:85-87) | — |
| "CaveFisher" | `CaveFisher` — OSM:3799 | `ModEntities.CAVE_FISHER` (ME:55-58) | — |

---

## 10. Footprint extents (relative to `(cposx, cposy, cposz)`)

| Axis | Min | Max | Driver |
|---|---|---|---|
| X | −4 | +26 | corner towers at (−2,−2)/(+20,+20) with local −2..+6 caps (GD:3293-3296, 3503-3521); plate itself is −3..+25 (GD:3219) |
| Z | −4 | +26 | same |
| Y | +0 | +16 | plate at +0 (GD:3223); tower crenellation ring at height+3 = +16 (GD:3515) |

29×29 core plate (−3..+25, GD:3219); 31×31 total span with towers (−4..+26); 17 blocks tall. Suggested port bounding box
with 1-block margin: x,z −5..+27, y −1..+17.

---

## 11. Worldgen call context

### 11.1 The End path (dimension 1)

Dispatch: `OreSpawnWorld.generate` `switch(dimensionId)` `case 1:` →
`generateEnd(world, random, chunkX*16, chunkZ*16)` (OSW:219-222) — **dimension-gated, not
biome-gated**; fires for every End chunk.

`generateEnd` (OSW:226-241): always `addEndAnts`; then one roll `i = world.rand.nextInt(4)`:
0 → `addEndKnights`, 1 → `addEndReapers`, 2 → `addHospital`, **3 → `addEnderCastle`** (OSW:238-240).

`addEnderCastle(world, random, chunkX, chunkZ)` (OSW:1557-1570) — args are already **block** coords:

1. Odds gate: `random.nextInt(50) != 0 → return` (OSW:1558-1560). Combined with the 1/4 branch
   above: expected ~1/200 End chunks attempt placement.
2. Up to **3 attempts** (OSW:1561): `posX = chunkX + random.nextInt(16)`,
   `posZ = chunkZ + random.nextInt(16)` (OSW:1562-1563).
3. Ground scan **top-down** `posY = 90; posY > 10; --posY` (OSW:1564). Accept the first Y where ALL:
   - `world.func_147437_c(posX, posY, posZ)` — block at posY is **air** (OSW:1565);
   - `world.func_147439_a(posX, posY-1, posZ) == Blocks.field_150377_bs` — block below is
     **end stone** (OSW:1565);
   - `quickBigSpaceCheck(world, posX, posY, posZ)` — the entire 30×30 horizontal plane at
     `posY+8`, x,z offsets `-5..24`, is **air** (OSW:2635-2643). This is the only clearance
     check — there is **no explicit obsidian-pillar or void check**; a pillar or terrain bulge
     intersecting the y+8 plane simply fails the attempt.
4. On success: `makeEnderCastle(world, posX, posY, posZ)` then `return` (OSW:1566-1567). So
   `cposy` is the air block sitting on end stone; the obsidian plate replaces that air layer.
   **`recently_placed` is NOT set and not consulted** on the End path (contrast D4, §11.2).

**End biome facts for the 1.21.1 implementer** (decision left open, per ground rules):
- 1.7.10 dim 1 had exactly **one** biome (`BiomeGenBase.sky`, "The End", id 9), and no outer
  islands existed before MC 1.9 — so in practice every 1.7.10 Ender Castle generated on the
  central island, the only end-stone terrain in range of the y 90→11 scan.
- The original gate is the **dimension id** (OSW:219), not the biome; biome is never inspected.
- Modern 1.21.1 has 5 End biomes: `minecraft:the_end` (central island), and the 1.9+ outer-ring
  biomes `small_end_islands`, `end_midlands`, `end_highlands`, `end_barrens` (all covered by the
  vanilla dimension tag `#minecraft:is_end`).
- Faithful-to-geography choice: biome tag containing only `minecraft:the_end` (structures appear
  where they did in 1.7.10 — but note modern central-island terrain near the origin is heavily
  contested by the dragon fight). Faithful-to-code choice: all 5 (`#minecraft:is_end`), since the
  original ran dimension-wide and the end-stone + 30×30-air scan self-selects island terrain
  (outer islands top out ~y60-75, comfortably inside the 90→11 scan). Document-only; implementer
  picks the tag.

### 11.2 Islands D4 path (`addD4EnderCastle`, OSW:2322-2343)

Outer gate (OSW:132-135): dimension == `OreSpawnMain.DimensionID4`, `recently_placed == 0`
(static cooldown, decremented once per generated chunk, OSW:30/37-38), `random.nextInt(100) == 0`,
and `D4BigSpaceCheck(world, chunkX*16, 7, chunkZ*16)` — plane at y `7+4 = 11`, x offsets
`-25..39`, z `-25..29`, every block must be air, vanilla log, `MyAppleLeaves`, or `MyScaryLeaves`
(OSW:2655-2664). Then `i = random.nextInt(19)`; **`i == 7` → `addD4EnderCastle`** (OSW:141-143;
full 19-outcome table in `d5_extraction/enormous_castle_spec.md` §12.3).

`addD4EnderCastle` body:
1. LessLag gate: `if (OreSpawnMain.LessLag != 0 && random.nextInt(2) != 0) return false;`
   (OSW:2323-2325).
2. `posX = chunkX + random.nextInt(8)`, `posZ = chunkZ + random.nextInt(8)` (OSW:2326-2327) —
   note nextInt(**8**) jitter here vs nextInt(16) on the End path.
3. Ground scan `posY = 20; posY > 4; --posY`: accept first Y where the block **at** posY is
   `Blocks.field_150349_c` (grass block) (OSW:2328-2330).
4. Clearance: every block on the 30×30 plane at `posY+18`, offsets x,z `-5..24`, must be air,
   else `return false` immediately (OSW:2331-2337).
5. `makeEnderCastle(world, posX, posY, posZ)` — `cposy` = the grass block's own Y (plate
   overwrites the grass); `recently_placed = 50; return true` (OSW:2338-2340).

### 11.3 DungeonSpawnerBlock

Type 27 of the `nextInt(50)` roll (DSB:52, DSB:134-136) — build origin is the spawner block's own
position, 400 ticks after placement (DSB:35-40, 46-52). Port pool: MISSING-IN-PORT (§1).

---

## 12. World-block READS inside the build (chunk-stitching audit)

`makeEnderCastle` + `makeAColumn` (GD:3207-3623) contain **zero terrain reads** — no
`func_147439_a`/`func_147437_c` calls; every `bid == ...` comparison is against the local variable
just assigned. The only world reads are:

1. `world.func_147438_o` (getTileEntity) via `getChestTileEntity`/`getSpawnerTileEntity`
   (GD:75-95), always fetching the TE of the spawner/chest **the method itself just placed**
   one line earlier — the standard pattern; in the port, place spawner/chest block entities
   directly through the piece (see `structure_conversion_pattern.md` §1 step 3).
2. `world.field_73012_v` (the World RNG) for all build randomness (GD:3244/3273/3275/3461/3476/3491)
   — the port should substitute the piece's seeded `RandomSource`.

All placement-context reads (air/end-stone/grass scans, clearance planes) happen in
`OreSpawnWorld` **before** the build is invoked (§11) and map onto the port's placement-time
heightmap/scan machinery, not the piece generator.

---

## 13. Surprises / porting notes / MISSING-IN-PORT

1. **MISSING-IN-PORT — the whole structure.** No builder, no `DungeonType`, no structure JSON,
   no DSB pool entry (§1).
2. **MISSING-IN-PORT — End-dimension placement precedent.** Nothing in the port's data folder
   references any End biome; this will be the first End structure. Biome-tag decision documented
   in §11.1.
3. The four "mob spawn blocks" in the walls are **decoration, not spawners**: `OreGenericEgg`
   is a plain dirt-material block whose only behavior is a 50% 5..9 XP drop on break
   (OGE:16-30). The real mob pressure comes from the 16 vanilla spawners (§5).
4. The central ender chest (GD:3325) is a **plain vanilla ender chest**, not a loot container —
   do not attach a loot table to it.
5. The rooftop 5×5 **lava pool at y+9** (GD:3307-3313) sits one block above the y+8 plaza and is
   rimmed only by the partial bedrock bits of §3 row 13 — lava is exposed on the diagonals'
   gaps; faithful port should use `Blocks.LAVA` source states and rely on flag-2 (no updates)
   placement order, exactly as the original (setBlockFast flag 2 means no immediate fluid
   update scheduling at build time).
6. No `world.isRemote` bail in `makeEnderCastle`, unlike `makeEnormousCastle` (GD:199-201) —
   irrelevant once ported to server-side structure pieces, but noteworthy for faithfulness audits.
7. `makeAColumn`'s dir-2 and dir-3 branches each apply **two** `++step` wraps (GD:3574-3579,
   3592-3597) while dir-1 applies one and dir-0 none — the four tower staircases deliberately(?)
   start at spiral phases 0/2/0/1. Transcribe as-is; do not "fix" the asymmetry.
8. The stalactite pearls (§3 row 8) are written at `j-1`/`j-2` **while the loop is still iterating
   j upward** — since only ring cells at j==6 trigger it and rows y+4/y+5 at the ring are never
   otherwise written by any loop, there is no overwrite hazard; the pearls hang below the parapet
   over pre-existing terrain/void.
9. There is no clear-envelope; outside the 0..22 keep, the y+1 plate layer, and the tower
   footprints, terrain is left in place (§3 note). On D4 placements next to trees this can leave leaves/logs abutting the
   towers — faithful.
10. Chest metadata quirk: the west-wall chest uses meta 2 (north-facing) while the north/south
    chests use 3/4 — likely an original bug; keep raw facings (§6).
11. All build randomness uses `world.rand`, not the chunk-gen `Random` passed to the `add*`
    methods — for deterministic port pieces substitute the piece RNG (§12.2).
12. End-path effective odds: 1/4 branch × 1/50 = 1/200 per End chunk **before** the ground scan;
    3 attempts per success roll; no `recently_placed` interaction. D4-path odds: 1/100 × 1/19
    (× 1/2 if LessLag) behind `recently_placed==0` and two 30×30/65×55 air-plane checks; sets
    `recently_placed = 50` on success.
