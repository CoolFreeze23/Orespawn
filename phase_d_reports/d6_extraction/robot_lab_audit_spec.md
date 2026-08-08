# Robot Lab — Reconciliation Audit Spec (1.7.10 → NeoForge 1.21.1)

Source of truth: `reference_1_7_10_source/sources/danger/orespawn/GenericDungeon.java` —
`makeRobotLab` (GD:4044-4091) + helpers `makerobopillar` (GD:4093-4125), `makerobomain`
(GD:4127-4164), `makerobotower` (GD:4166-4221), `makeroboaltar` (GD:4223-4258),
`makeroborailway` (GD:4260-4293), `makeroboassemblyline` (GD:4295-4308),
`makerobotreasureroom` (GD:4310-4351); loot list `RobotContentsList` (GD:37).

Audit target: `src/main/java/danger/orespawn/world/structure/LegacyDungeonPiece.java` —
`generateRobotLab` (LDP:694-745; the task brief's "~532-800" is stale) + port helpers
`makeRoboPillar` (LDP:748-768), `makeRoboMain` (LDP:771-807), `makeRoboAltar` (LDP:810-835),
`makeRoboRailway` (LDP:838-852), `makeRoboAssemblyLine` (LDP:855-873), `makeRoboTreasureRoom`
(LDP:876-895), `makeRoboTower` (LDP:898-941), `fillRobotChest` (LDP:948-967).

All coordinates below are **relative to `(cposx, cposy, cposz)`**, the three int args of
`makeRobotLab`. In worldgen `cposy` is the Y of the **grass block** found by the ground scan
(OSW:2374-2376), so the floor plate replaces the grass row.

Citation convention: `GD:NN` = GenericDungeon.java, `OSW:NN` = OreSpawnWorld.java,
`OSM:NN` = OreSpawnMain.java, `DSB:NN` = DungeonSpawnerBlock.java (all under
`reference_1_7_10_source/sources/danger/orespawn/`). Port: `LDP:NN` = LegacyDungeonPiece.java,
`LDS:NN` = LegacyDungeonStructure.java (both `src/main/java/danger/orespawn/world/structure/`),
`ME:NN` = ModItems.java's sibling `ModEntities.java`, `MI:NN` = ModItems.java,
`RDSBE:NN` = `src/main/java/danger/orespawn/block/entity/RandomDungeonSpawnerBlockEntity.java`.

Block writes: `OreSpawnMain.setBlockFast(world, x, y, z, block, meta, 2)` throughout (flag 2 =
send-to-client, no neighbor updates; modern `level.setBlock(pos, state, Block.UPDATE_CLIENTS)`).
Spawners and chests use `world.func_147465_d(..., meta, 2)` (GD:4112, 4248, 4336, 4341, 4346).
Doors use `ItemDoor.func_150924_a` (GD:4080-4081 — vanilla `placeDoorBlock`, see §17).

---

## 1. Entry points

| Caller | Coords passed | Cite |
|---|---|---|
| `OreSpawnWorld.addD4RobotLab` (worldgen, Islands dim D4) | `(posX, posY_grass, posZ)` | OSW:2368-2389, dispatched at OSW:147-149 |
| `DungeonSpawnerBlock` type **30** | player-placed block pos | DSB:143-145, roll `nextInt(50)` DSB:52 |

`makeRobotLab` bails on the client: `if (world.field_72995_K) return;` (GD:4050-4052).
There is **no** non-D4 `addRobotLab` in OreSpawnWorld — grep over the reference finds only
`addD4RobotLab` (OSW:148, 2368, 2384). D4 = `DimensionID4 = BaseDimensionID + 3` (OSM:1598),
the Islands dimension (see `d5_extraction/enormous_castle_spec.md` §12.1).

**Port entry points**: `LegacyDungeonPiece.DungeonType.ROBOT_LAB(48, 2, 50)` (LDP:82, symmetric
3-arg ctor → `PlacementMode.SURFACE_CENTER`, LDP:158-160), dispatched
`case ROBOT_LAB -> generateRobotLab(rng)` (LDP:249). Registered as structure
`orespawn:robot_lab` (`data/orespawn/worldgen/structure/robot_lab.json`: type
`orespawn:legacy_dungeon`, `dungeon_type: "ROBOT_LAB"`, biome tag
`#orespawn:has_structure/robot_lab` = `orespawn:island_biome`,
`terrain_adaptation: beard_thin`), placed via
`data/orespawn/worldgen/structure_set/robot_lab.json` (`random_spread`, spacing 44,
separation 22, salt 84301). The live-game DSB path is **not** wired for type 30 (§16).

---

## 2. Full call graph (with args)

`makeRobotLab(world, cposx, cposy, cposz)` (GD:4044) calls, in order:

| # | Call | Args (relative to makeRobotLab's cposx/cposy/cposz; `width=10`, `length=20`) | Cite |
|---|---|---|---|
| 1 | `makerobomain` | `(cposx, cposy, cposz + 19)` | GD:4084 |
| 2 | `makerobopillar` | `(cposx, cposy, cposz + 6, dir=0)` (`length/3`) | GD:4085 |
| 3 | `makerobopillar` | `(cposx, cposy, cposz + 13, dir=0)` (`length*2/3`) | GD:4086 |
| 4 | `makerobopillar` | `(cposx, cposy, cposz + 19, dir=0)` | GD:4087 |
| 5 | `makerobopillar` | `(cposx + 9, cposy, cposz + 6, dir=1)` | GD:4088 |
| 6 | `makerobopillar` | `(cposx + 9, cposy, cposz + 13, dir=1)` | GD:4089 |
| 7 | `makerobopillar` | `(cposx + 9, cposy, cposz + 19, dir=1)` | GD:4090 |

`makerobomain(world, mx=cposx, my=cposy, mz=cposz+19)` (GD:4127) first does `mx -= 10`
(GD:4132; so `mx = cposx − 10`), builds the 30×30×9 hangar, then calls (`width=30`,
`length=30`, `height=9`):

| # | Call | Absolute args (relative to makeRobotLab origin) | Cite |
|---|---|---|---|
| 8 | `makeroboaltar` | `(cposx + 1, cposy, cposz + 25)` (`mx + 11, my, mz + 6`) | GD:4159 |
| 9 | `makeroborailway` | `(cposx − 7, cposy, cposz + 29)` (`mx + 3, my, mz + 10`) | GD:4160 |
| 10 | `makeroboassemblyline` | `(cposx + 16, cposy, cposz + 23)` (`mx + 26, my, mz + 4`) | GD:4161 |
| 11 | `makerobotreasureroom` | `(cposx − 1, cposy, cposz + 37)` (`mx + 9, my, mz + 18`) | GD:4162 |
| 12 | `makerobotower` | `(cposx − 1, cposy + 9, cposz + 28)` (`mx + 9, my + 9, mz + 9`) | GD:4163 |

`makerobotower(tx, ty, tz)` in turn calls `makerobopillar` four more times (GD:4193-4196):
`(tx+4, ty+1, tz+4, 1)`, `(tx+7, ty+1, tz+7, 0)`, `(tx+4, ty+1, tz+7, 1)`, `(tx+7, ty+1, tz+4, 0)`.

So **every** annex helper is live: makerobomain, makerobotower, makeroboaltar, makeroborailway,
makeroboassemblyline, makerobotreasureroom, plus the un-briefed 7th helper `makerobopillar`
(private, GD:4093) invoked **10 times total** (6 from makeRobotLab + 4 from makerobotower) —
i.e. 10 Robo-Sniper spawners.

**CRITICAL ORDER NOTE**: the original builds the hangar (`makerobomain`, GD:4084) **before**
the six entry pillars (GD:4085-4090). The hangar's k=0 row (z = cposz+19) coincides with the
entry hall's far wall, and its door-opening carve (GD:4152-4154) blows air through x 0..9,
y 1..3 of that shared row; the two rear pillars (calls #4/#7) then repair the columns at
x −1..1 and x 8..10 **after** the hangar pass, so their bodies and spawners survive. The port
reverses this order (§18 item 2).

---

## 3. `makeRobotLab` — entry hall (GD:4044-4083)

Constants: `width = 10`, `length = 20`, `height = 5` (GD:4047-4049). Triple loop
`j 0..height` (inclusive), `i 0..width-1`, `k 0..length-1` (GD:4053-4075):

| Priority (later wins) | Condition | Block | Cite |
|---|---|---|---|
| 1 (default) | — | air | GD:4056 |
| 2 | `i==0 \|\| k==0 \|\| i==9 \|\| k==19` (walls) | quartz block | GD:4057-4059 |
| 3 | `j==0` (floor) | quartz block; `i==5 \|\| i==4` → **iron block** center stripe | GD:4060-4065 |
| 4 | `j==5` (roof) | quartz block; rim (`i/k` edge) → **air** (open parapet rim) | GD:4066-4071 |

Then the doorway on the k=0 (north/entry) face:

- Air carve at `(4..5, y+1..y+2, z+0)` — 2×2 (GD:4076-4079).
- `ItemDoor.func_150924_a(world, cposx+5, cposy+1, cposz, 3, iron_door)` and same at
  `cposx+4` — **two full iron doors** (lower + upper halves, facing from dir=3)
  (GD:4080-4081).
- **Stone buttons, meta 4** at `(cposx+3, cposy+2, cposz−1)` and `(cposx+6, cposy+2, cposz−1)`
  — outside the wall, flanking the doors, the only way to open them (GD:4082-4083).

## 4. `makerobopillar(cposx, cposy, cposz, dir)` (GD:4093-4125) — ×10

3×3×5 solid quartz column centered on `(cposx, cposz)`: loops `j 0..4`, `i −1..1`, `k −1..1`;
default quartz (GD:4098); at `j==2 || j==3` the four edge-midpoint cells
(`k==0 && i==±1`, `i==0 && k==±1`) are **redstone block** (GD:4099-4106) — a redstone "plus"
band.

Spawner (replaces a pillar cell, placed after the body):
- `dir==0` → spawner at `(cposx+1, cposy+1, cposz)`, entity `"Robo-Sniper"` (GD:4111-4117).
- `dir==1` → spawner at `(cposx−1, cposy+1, cposz)`, entity `"Robo-Sniper"` (GD:4118-4124).

## 5. `makerobomain` — 30×30×9 hangar (GD:4127-4164)

`width = 30`, `length = 30`, `height = 9` (GD:4129-4131); `cposx -= 10` (GD:4132). Same
shell logic as the entry hall, loops `j 0..9`, `i 0..29`, `k 0..29` (GD:4133-4158):

| Priority | Condition | Block | Cite |
|---|---|---|---|
| 1 | default | air | GD:4136 |
| 2 | wall (`i/k` edges) | quartz block | GD:4137-4139 |
| 3 | `j==0` floor | quartz; `i==15 \|\| i==14` → iron block stripe | GD:4140-4145 |
| 4 | `j==9` roof | quartz; rim → air | GD:4146-4151 |
| 5 | `(j==1..3) && k==0 && i in [10,20)` | air — 10-wide × 3-tall opening in the shared wall back to the entry hall | GD:4152-4154 |

Then annex calls #8-#12 (§2).

## 6. `makerobotower` — rooftop sniper tower (GD:4166-4221)

Built ON the hangar roof (`ty = cposy + 9`). Base pad, loops `j 0..1`, `i 0..11`, `k 0..11`
(GD:4171-4192):

| Condition | Block | Cite |
|---|---|---|
| `j==0` | quartz block (full 12×12 pad) | GD:4186-4188 |
| `j==1`, `i/k` edge | **iron bars** railing | GD:4176-4178 |
| `j==1`, four corners | **redstone block** | GD:4179-4184 |
| `j==1` interior | air | GD:4174 |

Four inner `makerobopillar` calls at `(tx+4/7, ty+1, tz+4/7)` with dirs 1/0/1/0
(GD:4193-4196) → 4 more Robo-Sniper spawners at `ty+2`.

Central spire, loops `j 5..34`, `i 0..1`, `k 0..2`, written at `(tx+i+5, ty+j, tz+k+5)`
(GD:4197-4220):

| Band | k==0 | k==1 | k==2 | Cite |
|---|---|---|---|---|
| `j 5..14` | quartz | quartz | quartz | GD:4201-4202 |
| `j 15..24` | quartz | quartz | iron bars | GD:4203-4207 |
| `j 25..34` | quartz | iron bars | air | GD:4208-4216 |

Spire top = `ty + 34` = **cposy + 43** — the tallest point of the structure.

## 7. `makeroboaltar` — Robo-Pounder altar (GD:4223-4258)

- 8×8 **iron block** pad at `y+0` (GD:4228-4233).
- 6×6 **quartz** slab at `y+1`, inset 1 (GD:4234-4239).
- Four posts at `(+2/+5, +2/+5)`: **redstone block** at `y+1`, **torch** at `y+2`
  (GD:4240-4247).
- Two spawners: `(ax+3, y+2, az+3)` and `(ax+4, y+2, az+4)`, both entity
  `"Robo-Pounder"` (GD:4248-4257).

## 8. `makeroborailway` (GD:4260-4293)

Two parallel 13-long rail lines at `x+0` and `x+3`, all at `y+1`, z `+0..+12`; 32 writes
(GD:4261-4292 — 10 rail rows × 2 + 3 boost rows × 4):

| z row | x+0 | x+1 | x+2 | x+3 | Cite |
|---|---|---|---|---|---|
| 0, 1, 3, 4, 5, 7, 8, 9, 11, 12 | rail | — | — | rail | GD:4261-4264, 4269-4274, 4279-4284, 4289-4292 |
| 2, 6, 10 | **golden (powered) rail** (`field_150318_D`) | lever meta 5 (floor, unpowered) | lever meta 5 | golden rail | GD:4265-4268, 4275-4278, 4285-4288 |

## 9. `makeroboassemblyline` (GD:4295-4308)

For `k 0..23` (GD:4296):

| Condition | Position | Block | Cite |
|---|---|---|---|
| always | `(x, y+1, z+k)` and `(x+1, y+1, z+k)` | quartz block (2-wide belt) | GD:4305-4306 |
| `k%3==1` | `(x−2, y+1, z+k)` | **quartz stairs**, meta 1 (ascending west) | GD:4298 |
| `k%3==1` | `(x, y+2, z+k)` | **sticky piston**, meta 3 (facing south) | GD:4299 |
| `k%3==1` | `(x, y+3, z+k)` | **carpet**, meta 0 (white) | GD:4300 |
| `k%3==0` | `(x, y+2, z+k)` | **lever**, meta 13 = orientation 5 + bit 8 → floor lever, **POWERED ON** | GD:4302-4304 |

The powered levers drive the sticky pistons — the "assembly line crusher" is visibly active.

## 10. `makerobotreasureroom` (GD:4310-4351)

12×8 room, loops `j 1..6`, `i 0..11`, `k 0..7` (GD:4316-4335):

| Priority | Condition | Block | Cite |
|---|---|---|---|
| 1 | default | air | GD:4319 |
| 2 | `i==0 \|\| k==0 \|\| i==11 \|\| k==7` walls | quartz block | GD:4320-4322 |
| 3 | `j==2 && i==11` | iron bars (window band, east wall) | GD:4323-4325 |
| 4 | `j==3 && bid != air` | iron bars (full ring band at j==3) | GD:4326-4328 |
| 5 | `(j==1..3) && k==0 && (i==1 \|\| i==2)` | air — 2-wide × 3-tall doorway | GD:4329-4331 |

Contents (all at `y+1`, `z+1` row):
- Spawner `(rx+10, y+1, rz+1)`, entity `"Robo-Warrior"` (GD:4336-4340).
- Chest meta 2 (faces north) at `(rx+8, y+1, rz+1)`, filled from `RobotContentsList`,
  count `10 + nextInt(5)` (GD:4341-4345).
- Chest meta 2 at `(rx+6, y+1, rz+1)`, same list, same count (GD:4346-4350).

These 2 chests are the **only** chests in the whole Robot Lab; both use `RobotContentsList`
(GD:4315). No annex-specific lists exist.

---

## 11. `RobotContentsList` — FULL transcription (GD:37)

`WeightedRandomChestContent(item, damage, min, max, weight)`. 23 entries, total weight **755**.
Fill: `WeightedRandomChestContent.func_76293_a(world.rand, list, chest, 10 + nextInt(5))`
(GD:4344, 4349) → 10-14 weighted draws per chest.

| # | 1.7.10 item (srg) | Decoded | min | max | wt | Port counterpart | In port fill (LDP:948-967)? |
|---|---|---|---|---|---|---|---|
| 1 | `Items.field_151137_ax` | redstone dust | 1 | 10 | 35 | `Items.REDSTONE` | yes (LDP:950) |
| 2 | `Items.field_151107_aW` | **repeater** | 1 | 10 | 35 | `Items.REPEATER` | **wrong — CLOCK** (LDP:951) |
| 3 | `Items.field_151143_au` | minecart | 1 | 1 | 35 | `Items.MINECART` | yes (LDP:952) |
| 4 | `Items.field_151059_bz` | fire charge | 1 | 10 | 35 | `Items.FIRE_CHARGE` | yes (LDP:953) |
| 5 | `Items.field_151140_bW` | **hopper minecart** | 1 | 1 | 35 | `Items.HOPPER_MINECART` | **missing** |
| 6 | `Blocks.field_150451_bX` | redstone block | 1 | 10 | 35 | `Items.REDSTONE_BLOCK` | yes (LDP:955) |
| 7 | `Blocks.field_150448_aq` | rail | 1 | 10 | 35 | `Items.RAIL` | yes (LDP:956) |
| 8 | `Blocks.field_150319_E` | **detector rail** | 1 | 10 | 35 | `Items.DETECTOR_RAIL` | **missing** |
| 9 | `Blocks.field_150320_F` | sticky piston | 1 | 10 | 35 | `Items.STICKY_PISTON` | yes (LDP:958) |
| 10 | `Blocks.field_150331_J` | piston | 1 | 10 | 35 | `Items.PISTON` | yes (LDP:957) |
| 11 | `Blocks.field_150429_aA` | redstone torch | 1 | 10 | 35 | `Items.REDSTONE_TORCH` | **missing** |
| 12 | `Blocks.field_150335_W` | TNT | 1 | 10 | 35 | `Items.TNT` | **missing** |
| 13 | `Blocks.field_150448_aq` | rail (**duplicate**, doubles rail weight) | 1 | 10 | 35 | `Items.RAIL` | **missing (dup)** |
| 14 | `Blocks.field_150442_at` | lever | 1 | 10 | 35 | `Items.LEVER` | **missing** |
| 15 | `OreSpawnMain.AntRobotKit` (OSM:798, 1724) | Red Ant Robot Kit | 1 | 1 | **10** | `ModItems.ANT_ROBOT_KIT` (MI:633) | **missing** |
| 16 | `OreSpawnMain.SpiderRobotKit` (OSM:797, 1723) | Spider Robot Kit | 1 | 1 | **10** | `ModItems.SPIDER_ROBOT_KIT` (MI:629) | **missing** |
| 17 | `Items.field_151139_aw` | iron door (item) | 1 | 10 | 35 | `Items.IRON_DOOR` | **missing** |
| 18 | `Blocks.field_150429_aA` | redstone torch (**duplicate**) | 1 | 10 | 35 | `Items.REDSTONE_TORCH` | **missing (dup)** |
| 19 | `Blocks.field_150471_bO` | wooden (oak) button | 1 | 10 | 35 | `Items.OAK_BUTTON` | **missing** |
| 20 | `Blocks.field_150411_aY` | iron bars | 1 | 10 | 35 | `Items.IRON_BARS` | **missing** |
| 21 | `Items.field_151132_bS` | comparator | 1 | 10 | 35 | `Items.COMPARATOR` | partial — present but count locked to 1 (LDP:954) |
| 22 | `Blocks.field_150408_cc` | activator rail | 1 | 10 | 35 | `Items.ACTIVATOR_RAIL` | **missing** |
| 23 | `OreSpawnMain.MyRayGun` (OSM:1746) | Ray Gun | 1 | 1 | **35** | `ModItems.RAY_GUN` (MI:430) | **missing** |

Port entries with **no original counterpart** (invented): `Items.DROPPER` (LDP:959),
`Items.DISPENSER` (LDP:960). The port's own doc comment "11 entries, all weight 35"
(LDP:943-947) is wrong on both counts — the original has 23 entries and the two robot kits
are weight 10.

## 12. Block palette — modern mapping

| 1.7.10 (srg) | Decoded | Modern | Used at (orig) | Port state |
|---|---|---|---|---|
| `field_150350_a` | air | `AIR` | everywhere | ✓ |
| `field_150371_ca` | quartz block | `QUARTZ_BLOCK` | shells, pillars, belt, spire | ✓ (LDP:702) |
| `field_150339_S` | iron block | `IRON_BLOCK` | floor stripes, altar pad | ✓ (LDP:703) |
| `field_150454_av` | iron door (block) | `IRON_DOOR` (two halves + facing/hinge) | GD:4080-4081 | ✗ lower half only, default facing (LDP:732-733) |
| `field_150430_aB` meta 4 | stone button (north-facing) | `STONE_BUTTON` wall-attached | GD:4082-4083 | ✗ **absent** |
| `field_150451_bX` | redstone block | `REDSTONE_BLOCK` | pillar band, tower corners, altar posts | ✓ |
| `field_150474_ac` | mob spawner | `SPAWNER` | 13 spawners | ✓ mechanism (LDP:379-386) |
| `field_150486_ae` meta 2 | chest, faces north | `CHEST` (default = north) | GD:4341, 4346 | ✓ equivalent (default state) |
| `field_150478_aa` | torch | `TORCH` | altar posts | ✓ (LDP:814) |
| `field_150411_aY` | iron bars | `IRON_BARS` | treasure bands, tower rail, spire | ✓ |
| `field_150448_aq` | rail | `RAIL` | railway | ✓ (LDP:839) |
| `field_150318_D` | **golden (powered) rail** | `POWERED_RAIL` | GD:4265/4268/4275/4278/4285/4288 | ✗ **DETECTOR_RAIL** (LDP:840, 844) |
| `field_150442_at` meta 5 / meta 13 | floor lever (13 = powered) | `LEVER` FACE=FLOOR (+POWERED for meta 13) | GD:4266-4267 etc., GD:4303 | ✗ default wall lever, unpowered (LDP:841, 848-849, 860, 870) |
| `field_150370_cb` meta 1 | quartz stairs, ascending west | `QUARTZ_STAIRS` facing west | GD:4298 | ✗ **RED_CARPET** (LDP:857, 865) |
| `field_150320_F` meta 3 | sticky piston facing south | `STICKY_PISTON` FACING=SOUTH | GD:4299 | ✗ default facing (LDP:858, 866) |
| `field_150404_cg` meta 0 | **white carpet** | `WHITE_CARPET` | GD:4300 | ✗ **RED_WOOL** (LDP:859, 867) |

## 13. Mob mapping table

Spawner name → 1.7.10 class (OreSpawnMain registration) → correct port `EntityType` vs what the
port actually binds:

| Spawner name | 1.7.10 class | Registration cite | Correct port type | Port actually uses | Verdict |
|---|---|---|---|---|---|
| `"Robo-Sniper"` (10× pillar, GD:4115/4122) | `Robot5` | OSM:3719-3723 | `ModEntities.ROBOT_5` (ME:137-139) | `ROBOT_5` (LDP:764, 766) | ✓ |
| `"Robo-Pounder"` (2× altar, GD:4251/4256) | `Robot2` | OSM:3695-3699 | `ModEntities.ROBOT_2` (ME:125-127) | **`ROBOT_4`** (LDP:833-834) | ✗ **SWAPPED** |
| `"Robo-Warrior"` (1× treasure room, GD:4339) | `Robot4` | OSM:3711-3715 | `ModEntities.ROBOT_4` (ME:133-135) | **`ROBOT_2`** (LDP:892) | ✗ **SWAPPED** |

Original totals: 10 Robo-Sniper + 2 Robo-Pounder + 1 Robo-Warrior = 13 spawners.

## 14. Footprint extents (relative to `(cposx, cposy, cposz)`)

| Component | X | Y | Z | Cite |
|---|---|---|---|---|
| Entry hall | 0..9 | 0..5 | 0..19 | GD:4053-4075 |
| Door buttons | 3, 6 | 2 | **−1** | GD:4082-4083 |
| Entry wall pillars | −1..10 (bulge ±1 past walls) | 0..4 | 5..7, 12..14, 18..20 | GD:4085-4090, 4093-4110 |
| Hangar | **−10..19** | 0..9 | 19..**48** | GD:4127-4158 (x−=10: GD:4132) |
| Altar | 1..8 | 0..2 | 25..32 | GD:4159, 4223-4257 |
| Railway | −7..−4 | 1 | 29..41 | GD:4160, 4260-4292 |
| Assembly line | 14..17 | 1..3 | 23..46 | GD:4161, 4295-4307 |
| Treasure room | −1..10 | 1..6 | 37..44 | GD:4162, 4310-4350 |
| Tower + spire | −1..10 | 9..**43** | 28..39 | GD:4163, 4166-4220 |
| **TOTAL** | **−10..+19** | **0..+43** | **−1..+48** | — |

**Port bounding box adequacy**: the port re-centers the build at `ox = origin.x − 5`,
`oz = origin.z − 25` (LDP:699-700, invented "/locate" nicety), so relative to the piece
origin the true footprint is X −15..+14, Y 0..+43, Z −26..+23. `ROBOT_LAB(48, 2, 50)`
(LDP:82) gives box X/Z ±48, Y −2..+50 → **fully covers the footprint including all
annexes** (margins ≥33 X, ≥22 Z, +7 Y). No clipping risk; box is ~3-4× oversized in
area (harmless, just extra postProcess passes).

## 15. Worldgen call context (Islands dimension, D4)

Original gate chain:
1. Dim check `world.provider.dimensionId == OreSpawnMain.DimensionID4` (OSW:132; ID at OSM:1598).
2. `recently_placed == 0 && random.nextInt(100) == 0 && D4BigSpaceCheck(world, cx*16, 7, cz*16)`
   (OSW:134).
3. `i = random.nextInt(19)`; `i == 9` → `addD4RobotLab` (OSW:135, 147-149) — see
   `enormous_castle_spec.md` §12.3 for the full 19-outcome table.
4. `addD4RobotLab` (OSW:2368-2389): `LessLag != 0` → 50% skip (OSW:2369-2371); position =
   chunk corner + `nextInt(8)` each axis (OSW:2372-2373); scan `posY` 20→5 for a **grass**
   block (`field_150349_c`, OSW:2374-2376); clearance probe: every column `x −5..59`,
   `z −5..69` at `posY+4` must be air, log (`field_150364_r`), `MyAppleLeaves` or
   `MyScaryLeaves` (OSW:2377-2383 — note the decompile checks air twice); then
   `makeRobotLab(world, posX, posY, posZ)` **at grass level** (OSW:2384) and
   `recently_placed = 50` (OSW:2385) — a ~50-chunk global cooldown shared by all D4
   structures.

Port placement: `random_spread` spacing 44 / separation 22 / salt 84301 (structure_set JSON),
biome-gated to `orespawn:island_biome` (tag file) — dimension-equivalent to the original
D4-only gate. But the anchor is `PlacementMode.SURFACE_CENTER` (LDP:82 → LDS:69-78):
chunk centre +8/+8 (no `nextInt(8)` jitter), **no LessLag gate**, no Y 20→5 window, no
clearance probe, and `y = getBaseHeight(WORLD_SURFACE_WG)` **without the −1** that
`islandsGrassOrigin` applies (LDS:144-146) — so the floor sits one block **above** the grass
instead of replacing it. The faithful `ISLANDS_GRASS` mode built for D5 (LDS:137-150, used by
NIGHTMARE_ROOKERY / KING_TOWER / QUEEN_TOWER, LDP:109-110, 125) is *not* used for ROBOT_LAB.
`terrain_adaptation: beard_thin` is also invented (the original relied on the clearance probe
and placed no filler).

## 16. DungeonSpawnerBlock trigger

Original: placing the DSB schedules a tick 400 out (DSB:35-40); on fire it deletes itself +
the block above (DSB:50-51), rolls `type = world.rand.nextInt(50)` (DSB:52); **type 30** →
`OreSpawnMain.MyDungeon.makeRobotLab(world, x, y, z)` at the block position (DSB:143-145).

Port: `RandomDungeonSpawnerBlockEntity` reproduces the 400-tick countdown and `nextInt(50)`
roll (RDSBE:40-42, 85), but `buildForType` has **no case 30** — only types 2, 21, 22, 23, 38,
47 are wired; type 30 falls into the `default -> placeGenericDungeonAt` fallback
(RDSBE:104-135). The infrastructure to fix this already exists:
`LegacyDungeonPiece.buildNow(server, pos, DungeonType.ROBOT_LAB)` (LDP:285-290) is exactly the
pattern used for cases 23/38/2/47. **MISSING-IN-PORT.** (The port also adds firework/explosion
sounds + a fixed 20-tick particle cadence, RDSBE:63-70, 88-94 — invented flourish, original
had only random display-tick `fireworksSpark` particles, DSB:29-33.)

## 17. World-block READS in the originals

- **Mid-build**: `makeRobotLab` and all seven helpers write only (`setBlockFast` /
  `func_147465_d`); tile-entity fetches (GD:4113, 4249, 4337, 4342, ...) read back blocks the
  method itself just placed — safe under chunk-stitching, and the port's
  `placeSpawner`/`placeChest` reproduce them (LDP:379-386, 369-376). The one genuine read is
  hidden inside vanilla `ItemDoor.func_150924_a` (GD:4080-4081): `placeDoorBlock` inspects
  neighbouring world blocks to pick the hinge side / pair the double door. A faithful port
  must hardcode both door halves' facing + hinge statically (left hinge on one, right on the
  other) — it cannot rely on the vanilla helper's neighbour scan.
- **Placement-time** (not mid-build): the grass scan and the 65×75 clearance probe
  (OSW:2374-2383) read world blocks; in the port this belongs to `findGenerationPoint`
  (noise-predicted heights only) per the established D5 treatment.

---

## 18. AUDIT — numbered discrepancy table (port `generateRobotLab` vs original)

| # | Severity | Discrepancy | Original cite | Port cite |
|---|---|---|---|---|
| 1 | **HIGH — wrong mobs** | Altar and treasure-room spawners are **swapped**: altar spawns "Robo-Pounder" = `Robot2` (OSM:3695), port binds `ROBOT_4`; treasure room spawns "Robo-Warrior" = `Robot4` (OSM:3711), port binds `ROBOT_2`. (Pillar "Robo-Sniper" = `Robot5` → `ROBOT_5` is correct, OSM:3719.) | GD:4251/4256, GD:4339 | LDP:833-834, LDP:892 |
| 2 | **HIGH — build order** | Port builds the 6 entry pillars **before** the hangar (original: hangar first, GD:4084, pillars after, GD:4085-4090). The hangar's k=0 door-opening carve (GD:4152-4154) then erases the two rear pillars' cells at z rel 19-20 — including **both rear Robo-Sniper spawners** (at hangar cells i=11/i=18, j=1, k=0 → air). Net: port loses 2 of 10 sniper spawners and the pillar columns framing the entry-hangar doorway. | GD:4084-4090 | LDP:736-744 |
| 3 | **HIGH — loot** | `fillRobotChest` covers only 9 of 23 original entries; repeater → `CLOCK` (wrong item); hopper minecart, detector rail, redstone torch (×2), TNT, duplicate rail, lever, iron door, wooden button, iron bars, activator rail all missing; **AntRobotKit / SpiderRobotKit (w10) and Ray Gun (w35) missing** despite port items existing (MI:633/629/430); `DROPPER` + `DISPENSER` invented; comparator count locked to 1 (orig 1-10). Full faithful list in §11. Fill count 10+nextInt(5) ✓. | GD:37, 4344 | LDP:948-967 |
| 4 | **MEDIUM — missing blocks** | The two stone buttons (meta 4) at z = −1 that open the iron doors are absent — the lab's doors cannot be opened legitimately. **MISSING-IN-PORT.** | GD:4082-4083 | LDP:727-733 (nothing after door place) |
| 5 | **MEDIUM — broken doors** | Original places two complete iron doors (lower + upper halves, dir 3) via `ItemDoor.func_150924_a`; port places only two **lower halves** with default (north) facing and no hinge — orphaned half-doors. | GD:4080-4081 | LDP:732-733 |
| 6 | **MEDIUM — wrong block** | Railway boost rows use golden/powered rail (`field_150318_D`); port uses `DETECTOR_RAIL`. Flanking levers lose meta 5 floor orientation (default = unpowered wall lever). | GD:4265-4288 | LDP:840-849 |
| 7 | **MEDIUM — wrong blocks** | Assembly line: quartz stairs meta 1 → `RED_CARPET`; white carpet (meta 0) → `RED_WOOL`; sticky piston meta 3 (south) → default facing; lever meta 13 (**powered** floor lever, drives the pistons) → default unpowered wall lever. The animated "crusher" tableau is lost. | GD:4298-4304 | LDP:855-873 |
| 8 | **MEDIUM — DSB** | DSB type 30 (Robot Lab) not registered in the port outcome pool; falls back to a generic dungeon even though `buildNow` + `DungeonType.ROBOT_LAB` exist. **MISSING-IN-PORT.** | DSB:143-145 | RDSBE:104-135 |
| 9 | **LOW — placement** | `SURFACE_CENTER` anchor instead of the faithful D5 `ISLANDS_GRASS` mode: no LessLag 50% gate, no `nextInt(8)` jitter, no Y 20→5 grass window, and a +1 Y offset (no `−1` on `getBaseHeight`) so the floor rests on top of the grass instead of replacing it. Biome tag (island_biome) does match the original D4-only gate. `terrain_adaptation: beard_thin` invented. | OSW:2368-2385 | LDP:82, LDS:69-78; structure JSON |
| 10 | **LOW — invented recentring** | Port shifts the build origin `ox = x−5, oz = z−25` for /locate aesthetics; harmless but a coordinate delta vs the original (which builds NE-ward from the anchor). Documented in-code. | GD:4044 | LDP:698-700 |
| 11 | **OK** | Bounding box `ROBOT_LAB(48, 2, 50)` fully covers the true footprint incl. annexes (needs X −15..+14, Y 0..+43, Z −26..+23 after recentring) — oversized ~3-4×, no clipping. | §14 | LDP:82, 188-196 |
| 12 | **OK** | Geometry of entry hall, hangar shell, pillar bodies, altar, treasure room, tower pad + spire, railway/assembly layouts, spawner/chest positions, chest facing (meta 2 = default north), and fill count are all faithfully reproduced loop-for-loop. | §3-§10 | LDP:694-941 |

No "QA Fix"-style invented-QoL comments exist in the Robot Lab section (the file's three
"QA fix" hits, LDP:1824/2174/2232, are all Challenge Tower).

## 19. Surprises

1. **Robot2/Robot4 name inversion trap**: the class numbers do not follow the in-lab
   difficulty order — "Robo-Pounder" is `Robot2` and "Robo-Warrior" is `Robot4`
   (OSM:3695/3711). The port hit exactly this trap (§18.1).
2. **`makerobopillar` is a hidden 7th helper** (private, GD:4093) and the only Robo-Sniper
   source — 10 call sites (6 + 4 via the tower).
3. **Original relies on overwrite order**: hangar first, rear pillars second — the pillars
   deliberately re-fill the doorway the hangar carved into the shared wall. Any port that
   reorders the calls silently deletes two spawners (§18.2).
4. **The lab has only 2 chests total** (treasure room), both from `RobotContentsList`; no
   annex-specific loot lists despite the task brief's suspicion.
5. **`RobotContentsList` has intentional duplicates** (rail #13, redstone torch #18) that
   double those items' effective weights.
6. **The assembly-line levers are placed pre-powered** (meta 13, GD:4303) so the sticky
   pistons are extended/active from the moment of generation.
7. **The clearance probe is oversized and offset** (x −5..59, z −5..69 at Y+4, OSW:2377-2383)
   vs the true footprint (x −10..19, z −1..48) — looks copy-pasted from another structure;
   the duplicated `air` check in the leaf whitelist is decompiler-visible dead code.
8. **`ItemDoor.func_150924_a` reads world blocks** (hinge/double-door pairing) — the only
   mid-build world read in the whole structure; must be replaced with static door states
   under the port's chunk-stitching constraint (§17).
9. **DSB type 30 unported** while five other DSB types already use the exact `buildNow`
   pattern that would serve it (§16).
