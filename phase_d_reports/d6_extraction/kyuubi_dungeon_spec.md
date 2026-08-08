# Kyuubi Dungeon — Extraction Spec (1.7.10 → NeoForge 1.21.1)

Source of truth: `reference_1_7_10_source/sources/danger/orespawn/GenericDungeon.java`,
methods `makeKyuubiDungeon` (GD:1095-1209), `addlavasquare` (GD:1211-1217), `addkyuubi` (GD:1219-1258),
`addblaze` (GD:1260-1361), plus loot fields GD:53-54.
All coordinates below are relative to the **build origin** `(X0, Y0, Z0)` = the three int args of
`makeKyuubiDungeon(World, int cposx, int cposy, int cposz)` (GD:1095).

Citation convention: `GD:NN` = GenericDungeon.java line NN, `OSW:NN` = OreSpawnWorld.java,
`OSM:NN` = OreSpawnMain.java, `DSB:NN` = DungeonSpawnerBlock.java. Port citations name the port file + line.

---

## 1. Entry points

| Caller | Signature used | Coords passed | Notes |
|---|---|---|---|
| `OreSpawnWorld.addKyuubiDungeon` (OSW:2599-2623) | `OreSpawnMain.MyDungeon.makeKyuubiDungeon(world, lowestX, lowestY - 2, lowestZ)` (OSW:2620) | ground-scan result, **Y offset −2** | worldgen path (see §9) |
| `DungeonSpawnerBlock` **type 7** (DSB:74-76) | `OreSpawnMain.MyDungeon.makeKyuubiDungeon(world, clickedX, clickedY, clickedZ)` | player-placed block pos, **no −2 offset** | `type = world.rand.nextInt(50)` (DSB:52), fires on a scheduled tick 400 ticks after placement (DSB:39) |

`OreSpawnMain.MyDungeon` is the shared `GenericDungeon` instance (same object used by the D5 tower specs).
Client-side guard: `if (world.field_72995_K) return;` (GD:1106-1108).

All plain block writes go through `FastSetBlock` (GD:187-189) = `OreSpawnMain.setBlockFast(world, x, y, z,
block, meta=0, flags=2)` — direct write, send-to-client, **no neighbor updates**. Modern equivalent:
`level.setBlock(pos, state, Block.UPDATE_CLIENTS)` — the port's `piece.place(...)` helper.
Chests/spawners use `world.func_147465_d(..., 0, 2)` (setBlock flag 2) and chest facing via
`world.func_72921_c(x, y, z, meta, 3)` (setBlockMetadataWithNotify flag 3).

---

## 2. Local constants (GD:1099-1105)

| Name | Value | Meaning |
|---|---|---|
| `width` | 5 | entrance hut / shaft / corridor cross-section |
| `height` | 5 | hut wall height |
| `depth` | 20 | shaft depth (fixed — **no** random component) |
| `length` | 12 | corridor length |
| `rwidth` | 30 | boss room Z extent |
| `rheight` | 18 | boss room Y extent |
| `rlength` | 20 | boss room X extent |

There are **zero RNG draws in the geometry** — the only `world.rand` use is the chest fill counts
(GD:1256, 1341, 1347, 1353, 1359). Every block position is deterministic given the origin.

---

## 3. Surface hut + drop shaft — per-loop geometry (all ranges inclusive)

| # | What | Where (relative to X0,Y0,Z0) | Block | Cite |
|---|---|---|---|---|
| 1 | Pre-clear cavity | `(X0+0..4, Y0-4..Y0, Z0+0..4)` — full 5×5, 5 layers down from Y0 | air | GD:1109-1115 |
| 2 | Hut roof slab | `(X0+0..4, Y0+5, Z0+0..4)` | sandstone | GD:1116-1121 |
| 3 | Roof entry hole | `(X0+2, Y0+5, Z0+2)` — single 1×1 hole in roof center (`width/2 = 2`) | air | GD:1122 |
| 4 | Hut walls (no door!) | `(X0+0..4, Y0+0..4, Z0+0..4)`: perimeter (`i∈{0,4}` or `k∈{0,4}`) sandstone; interior 3×3 air | sandstone / air | GD:1123-1134 |
| 5 | Shaft walls | `(X0+0..4, Y0-19..Y0-1, Z0+0..4)`: perimeter stone, interior 3×3 air (loop `j = -1` down to `-19`; `j > -depth`) | stone / air | GD:1135-1146 |
| 6 | Water brake | `(X0+1..3, Y0-21..Y0-20, Z0+1..3)` — interior 3×3 only, 2 layers (loop `j = -20, -21`; `j > -(depth+2)`) | **water (source)** `field_150355_j` | GD:1147-1153 |
| 7 | Basin floor | `(X0+1..3, Y0-22, Z0+1..3)` | stone | GD:1154-1159 |

The hut is a sealed sandstone box (all 4 walls solid, GD:1127); the only way in is the 1×1 roof hole
(row 3). With the worldgen anchor at `lowestY − 2`, the roof sits ~3-4 blocks above grade. The player
drops ~25 blocks from the roof hole into the 2-deep water pool.

**Unwalled water flank:** the water/basin layers (rows 6-7) have **no perimeter walls of their own** —
the shaft's stone perimeter stops at `Y0-19` (row 5). At `Y0-20..Y0-22` the 3×3 water/stone column is
bordered by whatever terrain was already there; a cave intersecting there lets the water escape.
Faithful quirk — do not add walls.

---

## 4. Boss room + lava corridor

Boss-room origin `R = (X0 + width + length - 2, Y0 - depth, Z0 - rwidth/2)` = **`(X0+15, Y0-20, Z0-15)`** (GD:1160-1162).

| # | What | Where | Block | Cite |
|---|---|---|---|---|
| 1 | Boss room hollow shell, 20×18×30 | `(Rx+0..19, Ry+0..17, Rz+0..29)`: shell where `i∈{0,19}` or `j∈{0,17}` or `k∈{0,29}`; interior air | **netherrack** `field_150424_aL` / air | GD:1163-1174 |
| 2 | Corridor, 12 long, 5×5 section | origin `(X0+4, Y0-20, Z0)` (GD:1175-1177); for `i=0..11, k=0..4, j=0..4`: perimeter (`k∈{0,4}` or `j∈{0,4}`) → stone, **but side-wall cells with `j∈{1,2,3}` → lava**; interior 3×3 air | stone floor/ceiling, **lava (source) side walls** `field_150353_l`, air bore | GD:1178-1192 |

Corridor details (GD:1181-1186): the perimeter test is `k==0 || k==width-1 || j==0 || j==width-1`
(note `j < width` — width doubles as the corridor height, both 5). Inside that branch `blk = stone`,
then `if (j > 0 && j < width-1) blk = lava` — so the floor row (`j=0`) and ceiling row (`j=4`) are stone,
while both side walls at head/body height (`j=1..3`) are **liquid lava source blocks**. Flag-2 writes
suppress neighbor updates, so the lava initially stands as a wall; any later block update makes it flow
into the corridor. Faithful hazard — reproduce exactly.

Connectivity (write-order dependent, all same-method overwrites):
- Corridor `i=0` column is at `X0+4` = the shaft's east wall plane; its interior-air writes
  (`Z0+1..3`, `Y0-19..Y0-17`) carve the doorway out of the shaft (shaft wall written GD:1136-1146,
  corridor after, GD:1178-1192).
- Corridor `i=11` column is at `X0+15` = `Rx`, the room's west wall; its interior-air writes carve the
  room entrance. Room shell is written first (GD:1164), corridor second — order matters.

### Floor decorations (all at `y = Ry+1 = Y0-19`, the first air layer; `++y` at GD:1196)

`addlavasquare(world, x, y, z)` (GD:1211-1217) places a plus-shape: netherrack at `(x±1, y, z)` and
`(x, y, z±1)`, lava (source) at `(x, y, z)`. Five calls (GD:1196-1200), positions relative to `R`:

| Call | Position `(Rx+, Ry+1, Rz+)` |
|---|---|
| 1 | `(+2, ·, +2)` |
| 2 | `(+4, ·, +6)` |
| 3 | `(+12, ·, +10)` |
| 4 | `(+6, ·, +15)` |
| 5 | `(+3, ·, +22)` |

Six **fire** blocks (`field_150480_ab`, GD:1203-1208), same layer, relative to `R`:
`(+7,·,+1)`, `(+5,·,+9)`, `(+2,·,+12)`, `(+16,·,+18)`, `(+2,·,+27)`, `(+18,·,+28)` — each sits on the
netherrack floor below, so it burns indefinitely (netherrack keeps fire alive). Placed with flag 2
(no update); faithful.

---

## 5. Kyuubi altar — `addkyuubi(world, Kx, Ky, Kz)` (GD:1219-1258)

Called as `addkyuubi(world, x + rlength/4, y, z + rwidth*3/4 - 3)` (GD:1201) →
`K = (Rx+5, Ry+1, Rz+19)` = **`(X0+20, Y0-19, Z0+4)`** (integer division: `20/4=5`, `30*3/4=22`, `22-3=19`).

| # | What | Where | Block | Cite |
|---|---|---|---|---|
| 1 | Base tier 9×9 | `(Kx+0..8, Ky, Kz+0..8)`: perimeter **nether brick** `field_150385_bj`; interior 7×7 **lava** | nether brick / lava | GD:1222, 1227-1235 |
| 2 | Second tier 7×7 | `(Kx+1..7, Ky+1, Kz+1..7)`: perimeter nether brick; interior 5×5 lava | nether brick / lava | GD:1236-1245 |
| 3 | Spawner stack ×3 | `(Kx+4, Ky+2..4, Kz+4)` — three vertically stacked `mob_spawner` blocks, each set to `"Kyuubi"` via `func_98272_a` | spawner `field_150474_ac` | GD:1246-1251 |
| 4 | Loot chest (floating cap) | `(Kx+4, Ky+5, Kz+4)`, metadata **2** (facing north/−Z); filled from `kyuubiContentsList` with `7 + world.rand.nextInt(7)` = **7-13 weighted stacks** | chest | GD:1252-1257 |

The chest sits directly on top of the 3-spawner column — a 6-block-tall lava-moated pillar; the lava
tiers touch the pillar base, so approaching on foot means wading 1-2 lava layers.

---

## 6. Blaze ziggurat — `addblaze(world, Bx, By, Bz)` (GD:1260-1361)

Called as `addblaze(world, x + rlength*2/3 - 3, y, z + rwidth/4 - 2)` (GD:1202) →
`B = (Rx+10, Ry+1, Rz+5)` = **`(X0+25, Y0-19, Z0-10)`** (integer division: `40/3=13`, `13-3=10`, `30/4=7`, `7-2=5`).
All tiers are **solid obsidian** (`field_150343_Z`). Local cursor `(xx, yy, zz)` starts at `B` and is
incremented `+1, +tier-height, +1` between tiers (GD:1280-1282, 1292-1294, 1304-1306).

| Tier | Size (X×Z × height) | Occupies | Cite |
|---|---|---|---|
| 1 | 7×7 × 4 | `(Bx+0..6, By+0..3, Bz+0..6)` | GD:1264-1279 |
| 2 | 5×5 × 1 | `(Bx+1..5, By+4, Bz+1..5)` | GD:1283-1291 |
| 3 | 3×3 × 6 | `(Bx+2..4, By+5..10, Bz+2..4)` | GD:1295-1303 |
| 4 | 1×1 × 5 (column) | `(Bx+3, By+11..15, Bz+3)` | GD:1307-1315 |

Column top `By+15 = Y0-4` — exactly the boss room's highest interior air layer (`Ry+16`).

**Blaze spawner ring** (GD:1316-1336): after tier 4 the cursor is `xx = Bx+3`, `yy = By+11`, `zz = Bz+3`,
`height = 5`; spawner Y = `yy + height + j - 3 = By+13+j` for `j = 0, 1`. Per layer, 4 spawners in a
cardinal plus around the column: `(xx-1, ·, zz)`, `(xx+1, ·, zz)`, `(xx, ·, zz-1)`, `(xx, ·, zz+1)` —
**8 `"Blaze"` spawners total** at `(Bx+2/Bx+4/Bz+2/Bz+4 pattern, By+13..14, ·)` = world Y `Y0-6, Y0-5`,
embedded replacing tier-3/column-adjacent air next to the obsidian column.

**4 loot chests on the tier-1 rim at tier-2 level** (`By+4`), each facing outward, each filled from
`blazeContentsList`:

| Chest | Position | Facing meta (GD cite) | Fill count | Cite |
|---|---|---|---|---|
| West | `(Bx+0, By+4, Bz+3)` | 4 = west (−X) | `4 + nextInt(5)` = 4-8 stacks | GD:1337-1342 |
| North | `(Bx+3, By+4, Bz+0)` | 2 = north (−Z) | `3 + nextInt(5)` = 3-7 | GD:1343-1348 |
| South | `(Bx+3, By+4, Bz+6)` | 3 = south (+Z) | `5 + nextInt(5)` = 5-9 | GD:1349-1354 |
| East | `(Bx+6, By+4, Bz+3)` | 5 = east (+X) | `6 + nextInt(5)` = 6-10 | GD:1355-1360 |

---

## 7. Chest loot — FULL transcriptions

Constructor semantics: `WeightedRandomChestContent(item, meta, minStack, maxStack, weight)`.
Fill: `WeightedRandomChestContent.func_76293_a(world.rand, list, chest, count)` — each pull rolls a
stack uniform in `[min, max]` into a **random slot** (collisions overwrite; a chest can end up with
fewer than the rolled count).

### 7a. `kyuubiContentsList` (GD:53) — Kyuubi altar chest (1 chest, 7-13 pulls). Total weight = **110**.

| # | 1.7.10 item (meta) | Modern / port mapping (cite) | min | max | weight |
|---|---|---|---|---|---|
| 1 | `Items.field_151137_ax` (0) | `minecraft:redstone` | 2 | 8 | 10 |
| 2 | `Blocks.field_150451_bX` (0) | `minecraft:redstone_block` | 4 | 8 | 15 |
| 3 | `Items.field_151128_bU` (0) | `minecraft:quartz` | 2 | 8 | 15 |
| 4 | `Items.field_151044_h` (0) | `minecraft:coal` | 2 | 8 | 15 |
| 5 | `OreSpawnMain.MyNightmareSword` (OSM:1644) | `ModItems.NIGHTMARE_SWORD` "nightmare_sword" (ModItems.java:370) | 1 | 1 | 20 |
| 6 | `OreSpawnMain.MyPoisonSword` (OSM:1658) | `ModItems.POISON_SWORD` "poison_sword" (ModItems.java:410) | 1 | 1 | 20 |
| 7 | `OreSpawnMain.KyuubiEgg` (OSM:5548, decl 1092) | `ModItems.KYUUBI_SPAWN_EGG` "kyuubi_spawn_egg" (ModItems.java:982-983) | 2 | 8 | 15 |

### 7b. `blazeContentsList` (GD:54) — 4 ziggurat chests. Total weight = **130**.

| # | 1.7.10 item (meta) | Modern / port mapping (cite) | min | max | weight |
|---|---|---|---|---|---|
| 1 | `Items.field_151072_bj` (0) | `minecraft:blaze_rod` | 2 | 8 | 15 |
| 2 | `Items.field_151065_br` (0) | `minecraft:blaze_powder` | 2 | 8 | 15 |
| 3 | `Items.field_151059_bz` (0) | `minecraft:fire_charge` | 4 | 8 | 15 |
| 4 | `Items.field_151033_d` (0) | `minecraft:flint_and_steel` | 1 | 1 | 10 |
| 5 | `OreSpawnMain.LavaEelHelmet` (OSM:1788) | `ModItems.LAVAEEL_HELMET` (ModItems.java:708) | 1 | 1 | 15 |
| 6 | `OreSpawnMain.LavaEelBody` (OSM:1789) | `ModItems.LAVAEEL_CHESTPLATE` (ModItems.java:711) | 1 | 1 | 15 |
| 7 | `OreSpawnMain.LavaEelLegs` (OSM:1790) | `ModItems.LAVAEEL_LEGGINGS` (ModItems.java:714) | 1 | 1 | 15 |
| 8 | `OreSpawnMain.LavaEelBoots` (OSM:1791) | `ModItems.LAVAEEL_BOOTS` (ModItems.java:717) | 1 | 1 | 15 |
| 9 | `Items.field_151063_bx` **meta 61** | vanilla spawn egg, damage 61 = entity ID 61 = Blaze → `minecraft:blaze_spawn_egg` | 2 | 8 | 15 |

Row 9 is the only nonzero-meta loot entry in either list — 1.7.10 encoded the egg's mob in the item
damage; the port maps it to the dedicated vanilla blaze spawn egg item.

---

## 8. Block palette — modern mapping

| 1.7.10 field | Modern block | Used for | Cites |
|---|---|---|---|
| `Blocks.field_150350_a` | `minecraft:air` | cavity, hut/shaft/corridor/room interiors, roof hole | GD:1112, 1122, 1131, 1143, 1171, 1189 |
| `Blocks.field_150322_A` | `minecraft:sandstone` | hut roof + walls | GD:1119, 1123, 1128 |
| `Blocks.field_150348_b` | `minecraft:stone` | shaft walls, basin floor, corridor floor/ceiling | GD:1135, 1157, 1182 |
| `Blocks.field_150355_j` | `minecraft:water` (source) | 2-layer landing pool | GD:1150 |
| `Blocks.field_150424_aL` | `minecraft:netherrack` | boss room shell, lava-square pluses | GD:1163, 1212-1215 |
| `Blocks.field_150353_l` | `minecraft:lava` (source) | corridor side walls, altar tier interiors, lava-square centers | GD:1184, 1233, 1243, 1216 |
| `Blocks.field_150480_ab` | `minecraft:fire` | 6 floor fires | GD:1203-1208 |
| `Blocks.field_150385_bj` | `minecraft:nether_bricks` | altar tier rims | GD:1230, 1240 |
| `Blocks.field_150343_Z` | `minecraft:obsidian` | ziggurat tiers 1-4 | GD:1276, 1288, 1300, 1312 |
| `Blocks.field_150474_ac` | `minecraft:spawner` | 3 Kyuubi + 8 Blaze spawners | GD:1247, 1317-1332 |
| `Blocks.field_150486_ae` | `minecraft:chest` | 5 loot chests | GD:1252, 1337, 1343, 1349, 1355 |

No OreSpawn custom blocks appear in this structure.

## 9. Mob mapping (spawner names)

| Spawner name | 1.7.10 class (registration) | Count / placement | Port EntityType |
|---|---|---|---|
| `"Kyuubi"` | `danger.orespawn.Kyuubi` — `EntityRegistry.registerGlobalEntityID(Kyuubi.class, "Kyuubi", KyuubiID)` (OSM:3847, mod-entity OSM:3851) | 3 stacked spawners `(Kx+4, Ky+2..4, Kz+4)` (GD:1246-1251) | `ModEntities.ENTITY_KYUUBI` → `EntityType<EntityKyuubi>` (ModEntities.java:229; entity/EntityKyuubi.java) |
| `"Blaze"` | vanilla `EntityBlaze` (vanilla EntityList name) | 8 spawners, plus-pattern ×2 layers at `By+13, By+14` (GD:1316-1336) | vanilla `EntityType.BLAZE` |

No direct entity spawns (`spawnCreature`) — all mobs come from spawner blocks, unlike BasiliskMaze.

---

## 10. Total footprint (relative to origin `(X0, Y0, Z0)`) — fully deterministic

| Axis | Min | Max | Extent | Determined by |
|---|---|---|---|---|
| X | `X0 + 0` | `X0 + 34` | **35 blocks** | hut/shaft west face `X0` (GD:1112) / room east shell `Rx+19 = X0+34` (GD:1164-1168) |
| Y | `Y0 - 22` | `Y0 + 5` | **28 blocks** | basin floor (GD:1156-1157) / hut roof (GD:1119) |
| Z | `Z0 - 15` | `Z0 + 14` | **30 blocks** | room shell `Rz = Z0-15` .. `Rz+29 = Z0+14` (GD:1165-1168) |

Interior sub-structures stay inside the room shell: ziggurat `X0+25..31 / Z0-10..-4`,
altar `X0+20..28 / Z0+4..12`, both within interior `X0+16..33 / Z0-14..+13`.

Suggested `DungeonType` entry (asymmetric constructor, +1 margin each side):
`KYUUBI_DUNGEON(-1, 35, 23, 6, -16, 15, PlacementMode.LOWEST_SURFACE_36)`
(pattern: LegacyDungeonPiece.java:119 BASILISK_MAZE precedent, constructor :163-172).

---

## 11. Worldgen call context

- **Dimension:** the `addKyuubiDungeon` call (OSW:85) sits in the `DimensionID2` (Mining /
  "Dimension-Extreme") branch — same rotation as BasiliskMaze → port dimension `orespawn:mining`.
- **Gating rolls** (OSW:79-101): `recently_placed == 0 && random.nextInt(95) == 1`, then
  `i = random.nextInt(7)`; **Kyuubi dungeon is `i == 1`** (OSW:84-86). Effective per-chunk odds
  ≈ 1/95 × 1/7 ≈ **1 in 665**, plus the shared 50-chunk cooldown (`recently_placed = 50`, OSW:2621).
- **WGEN-039 equivalence (D5 precedent):** identical odds to BasiliskMaze → `random_spread`
  **spacing 26 / separation 13** on the mining biome, mirroring the D5 basilisk_maze structure set.
  Take the next free salt (D5 used 84330/84331; grep `RES:worldgen/structure_set/*.json` first).

### `addKyuubiDungeon(world, random, chunkX, chunkZ)` (OSW:2599-2623) — ground scan

Character-for-character the same algorithm as `addBasiliskMaze` (OSW:2573-2597):

- Init `lowestY = 128`, `lowestX = chunkX`, `lowestZ = chunkZ`, `found = false` (OSW:2600-2603).
- Sample 6×6 columns at chunk offsets `i, j ∈ {0, 3, 6, 9, 12, 15}` (OSW:2604-2607); per column scan
  `posY` from 128 down to 31 (OSW:2608) for the first Y where `block(posX, posY+1, posZ) == air`
  **and** `block(posX, posY, posZ) != air` (OSW:2609).
- Keep the **lowest** surface of the 36 samples (OSW:2610-2615).
- Accept iff `found && lowestY > 40`; build at `(lowestX, lowestY - 2, lowestZ)`; set
  `recently_placed = 50` (OSW:2619-2622).

**Placement-mode verdict: the existing `LOWEST_SURFACE_36` mode fits exactly** — same 6×6 grid, same
Y 128→31 window, same lowest-column selection, same `> 40` accept gate, same −2 sink
(LegacyDungeonPiece.java:131-138 documents that mode against OSW:2573-2597). **No new PlacementMode is
needed.** With `depth = 20` fixed, the `lowestY > 40` gate guarantees the basin floor
`Y0-22 ≥ 41-2-22 = 17` stays well above bedrock.

### DungeonSpawnerBlock path

Original: `type == 7` → `makeKyuubiDungeon(world, clickedX, clickedY, clickedZ)` (DSB:74-76), no scan,
no offset. Port: `RandomDungeonSpawnerBlockEntity.buildForType` currently has **no case 7** — index 7
falls into the generic-dungeon fallback (RandomDungeonSpawnerBlockEntity.java:104-135). Add
`TYPE_KYUUBI_DUNGEON = 7` → `LegacyDungeonPiece.buildNow(server, pos, DungeonType.KYUUBI_DUNGEON)`,
matching the BASILISK_MAZE case (:108-114).

---

## 12. World-block READS during build

**None in the geometry.** `makeKyuubiDungeon`, `addlavasquare`, `addkyuubi`, and `addblaze` contain
zero `world.func_147439_a` (getBlock) calls — every write is position-computed. The only world reads are:

1. `getChestTileEntity` / `getSpawnerTileEntity` (GD:75-95) — `world.func_147438_o` (getTileEntity)
   on blocks the method **just placed itself** (GD:1248, 1254, 1318-1333, 1339-1357). The port's
   `piece.placeSpawner` / `piece.placeLootChest` helpers subsume these; no cross-chunk read exists.
2. The ground scan in `addKyuubiDungeon` (OSW:2609) — placement-time only, replaced by the
   `LOWEST_SURFACE_36` `getBaseHeight` prediction per structure_conversion_pattern.md §Step 4.

So the chunk-stitched replay (structure_conversion_pattern.md §1 Step 3, RNG contract rule 2) needs
**no in-memory world model** for this structure — unlike BasiliskMaze's `openMaze` probes. RNG stream
is trivially stable: the only draws are the 5 chest fill counts, which move into loot-table JSON
(`number_of_pools`-style roll), leaving the generator draw-free.

---

## 13. Surprises / MISSING-IN-PORT

1. **MISSING-IN-PORT — the entire structure.** No Kyuubi-dungeon generator exists under
   `src/main/java/danger/orespawn/world/` (grep: only the makeDungeon spawner table references
   ENTITY_KYUUBI, world/GenericDungeon.java:58). Needs: `KyuubiDungeonGenerator`, `DungeonType`
   entry (§10), structure + structure-set JSON (§11), and the spawner-block case 7 (§11).
2. **Sealed entrance hut** — 4 solid sandstone walls, no door (GD:1123-1134); sole entry is the 1×1
   roof hole (GD:1122). Easy to mistake for a bug; it is the intended drop-in gimmick.
3. **Liquid lava as corridor walls** (GD:1183-1185) — source blocks standing only because flag-2
   writes skip neighbor updates; first player interaction floods the corridor. Port must place with
   `UPDATE_CLIENTS` only (the `piece.place` helper) or the trap defuses/pre-floods.
4. **Water brake and basin are unwalled below Y0-19** (§3) — bordered by raw terrain; intersecting
   caves drain the pool making the 25-block drop lethal. Faithful; do not wall it.
5. **Fire blocks on netherrack** (GD:1203-1208) burn forever — flag-2 placement means they never get
   an initial update tick; on netherrack that is also the steady state.
6. **Chest floats on the spawner stack** at the altar (GD:1252) — chest directly atop 3 stacked
   Kyuubi spawners; three simultaneous active spawners in an 8-block radius around the loot.
7. **Integer-division quirks in the sub-structure anchors** (GD:1201-1202): `rlength*2/3 - 3 = 10`
   (not 10.33−3) and `rwidth*3/4 - 3 = 19` — keep integer math, do not "fix" to floats.
8. **No RNG in geometry at all** (§2) — unusual for OreSpawn; depth is fixed at 20 (contrast
   BasiliskMaze's `20 + nextInt(10)`). Chunk-replay determinism is free.
9. **Corridor "height" reuses `width`** (`j < width`, GD:1180) — works only because both are 5;
   transcribe as-is with a comment.
10. **Write-order dependencies** (§4): room shell before corridor (doorway carve), shaft walls before
    corridor (shaft-exit carve), hut clear before wall rebuild. The generator must preserve GD's
    statement order or the openings vanish.
