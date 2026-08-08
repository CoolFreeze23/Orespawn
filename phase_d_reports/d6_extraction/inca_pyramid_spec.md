# Inca Pyramid — Extraction Spec (1.7.10 → NeoForge 1.21.1)

Source of truth: `reference_1_7_10_source/sources/danger/orespawn/GenericDungeon.java`
— `makeIncaPyramid` (GD:3735-3952) + helpers `makepoolalter` (GD:3954-3961),
`makeincagraves` (GD:3963-3977), `makeincagrave` (GD:3979-4042). Loot list
`IncaPyramidContentsList` (GD:38).

All coordinates below are **relative to `(cposx, cposy, cposz)`**, the three int args of
`makeIncaPyramid` (the "base origin"). In worldgen, `cposy` is the Y of the grass block found by
the ground scan (OSW:2351-2353), so the 41×31 stone-brick floor plate is written AT grass level.
The method shifts its own cpos by `+baseheight` (=+10) on all three axes at GD:3879-3881 before
building the inner temple; tables below give both temple-relative and base-relative coordinates
where that matters.

Citation convention: `GD:NN` = GenericDungeon.java, `OSW:NN` = OreSpawnWorld.java,
`OSM:NN` = OreSpawnMain.java, `DSB:NN` = DungeonSpawnerBlock.java. Port citations:
`MI:NN` = src/main/java/danger/orespawn/ModItems.java, `MB:NN` = ModBlocks.java,
`ME:NN` = ModEntities.java, `LDP:NN` = world/structure/LegacyDungeonPiece.java,
`RDS:NN` = block/entity/RandomDungeonSpawnerBlockEntity.java.

Block writes: `OreSpawnMain.setBlockFast(world, x, y, z, block, meta, 2)` (OSM:5833; flag 2 =
send-to-client, no neighbor updates — modern `level.setBlock(pos, state, Block.UPDATE_CLIENTS)`).
Exceptions that use `world.func_147465_d(..., 2)` (vanilla setBlock, still flag 2): the 4
CreeperRepellent blocks (GD:3934-3937), the Molenoid spawner (GD:3938), the Ghost spawners
(GD:4001/4030) and the grave chests (GD:4007/4036). Spawner/chest tile entities fetched via
`getSpawnerTileEntity`/`getChestTileEntity` (GD:75-95, plain `func_147438_o` getTileEntity reads).

Client bail: `if (world.field_72995_K) return;` (GD:3752-3754).

---

## 1. Entry points

| Caller | Condition | Coords passed | Cite |
|---|---|---|---|
| `OreSpawnWorld.addD4IncaPyramid` (worldgen, Islands dim D4) | D4 roll `i == 8` of `nextInt(19)` | `(posX, posY_grass, posZ)` | dispatch OSW:135, 144-146; method OSW:2345-2366 |
| `DungeonSpawnerBlock` type **29** | player-placed random-dungeon block | placed block pos | DSB:140-142 |

DSB mechanics: on placement a scheduled tick is queued 400 ticks out (DSB:35-40); on that tick
the block deletes itself and the block above (DSB:50-51) then rolls `type = nextInt(50)`
(DSB:52); type 29 → `makeIncaPyramid` (DSB:140-142).

**Port entry points: MISSING-IN-PORT (all three).**
- No `INCA_PYRAMID` in the port's `LegacyDungeonPiece.DungeonType` enum (LDP:79-125) and no
  `makeIncaPyramid`/"inca" match anywhere under `src/main/java` (verified by case-insensitive
  grep; the only hits are in the second reference copy `src/danger/orespawn/`, which is 1.7.10
  source, not port code).
- The Islands D4 dispatch counterpart for `i == 8` has no ported structure.
- The port random-dungeon spawner pool (`RandomDungeonSpawnerBlockEntity`) has no type-29 case;
  its own doc comment says the pool was narrowed to ported builders (RDS:30-31), and unported
  rolls fall through to `default -> GenericDungeon.placeGenericDungeonAt` (RDS:134).

---

## 2. Constants / setup (GD:3746-3751)

| Name | Value | Meaning | Cite |
|---|---|---|---|
| `width` | 21 | inner temple X size | GD:3746 |
| `depth` | 11 | inner temple Z size | GD:3747 |
| `height` | 9 | inner temple Y size (floor..ceiling inclusive) | GD:3748 |
| `basewidth` | 41 | pyramid base X size | GD:3749 |
| `basedepth` | 31 | pyramid base Z size | GD:3750 |
| `baseheight` | 10 | pyramid step count / temple Y offset | GD:3751 |

Derived centers used below: `basewidth/2 = 20`, `basedepth/2 = 15`, `width/2 = 10`, `depth/2 = 5`.

---

## 3. Build sequence

### 3.1 Stepped hollow pyramid base (GD:3755-3782)

Triple loop `j = 0..9`, `i = 0..basewidth-2j-1`, `k = 0..basedepth-2j-1`; each cell written at
`(cposx + i + j, cposy + j, cposz + k + j)` (GD:3776) — i.e. **layer j is a 1-high hollow
rectangular ring** inset one block per side per layer:

| Layer j (y `+j`) | X range (incl.) | Z range (incl.) | Ring size |
|---|---|---|---|
| 0 | `0..40` | `0..30` | 41×31 — **solid stone-brick floor plate** (GD:3769-3771 overrides everything at j==0) |
| 1 | `1..39` | `1..29` | 39×29 ring |
| 2 | `2..38` | `2..28` | 37×27 ring + torch rows |
| 3 | `3..37` | `3..27` | 35×25 ring |
| 4 | `4..36` | `4..26` | 33×23 ring |
| 5 | `5..35` | `5..25` | 31×21 ring + torch rows |
| 6 | `6..34` | `6..24` | 29×19 ring |
| 7 | `7..33` | `7..23` | 27×17 ring |
| 8 | `8..32` | `8..22` | 25×15 ring + torch rows |
| 9 | `9..31` | `9..21` | 23×13 ring (open top — no cap; the temple sits inside it) |

Cell rules, in override order (GD:3758-3775):
1. default air (interior of each ring → the base is one big hollow cavity, y `+1..+9`);
2. ring perimeter (`i==0 || k==0 || i==max || k==max`): stone, then `nextInt(2)==0` → cobblestone,
   then `nextInt(4)==0` → mossy cobblestone (GD:3760-3768). Net odds per wall block:
   mossy 1/4, cobblestone 3/8, stone 3/8;
3. `j==0` → whole layer stone brick (GD:3769-3771);
4. north torch row: `k==1 && j%3==2 && i` not an endpoint → **torch meta 3** (south-facing, on the
   inner face of the north ring wall) at z `= j+1`, x `= j+1..39-j`, for j ∈ {2,5,8} (GD:3772-3775);
5. south torch row (extra write after the cell): when `k == basedepth-2j-1 && j%3==2 && i` interior
   → **torch meta 4** at `(cposx+i+j, cposy+j, cposz+k+j-1)` = z `29-j`, same x range and layers
   (GD:3777-3779).

### 3.2 Four staircase ramps (GD:3784-3878) — ⚠ world-block READS

Four structurally identical loops, one per side. Common pattern per loop iteration
(`m = 0..2*baseheight-2 = 0..18`, `j = m/2` → 0,0,1,1,…,9; `p = -2..2`; `meta` reset to 0 at
GD:3783/3807/3831/3855):

- **Rails** (`p == ±2`): **read** `world.func_147439_a(x, cposy+j+1, z)` (GD:3791/3815/3839/3863);
  if air → stone brick at y `+j+1`; additionally when `m==0 || m==18` → **torch meta 0**
  (standing) one higher at y `+j+2` (GD:3794-3796 etc.).
- **Treads** (`p ∈ {-1,0,1}` and `m` odd): **read** same position; if air → **stone slab**
  (`field_150333_U`, bottom half) at y `+j+1` (GD:3798-3800 etc.).
- **Support pillar** (all p): `while (j >= 0 && read(x, cposy+j, z) == air) { set stone; --j; }`
  (GD:3801-3804 etc.) — fills stone downward, stopping at the first non-air block or at relative
  y `+0` (never below cposy).

| Ramp | m→ walk axis | Fixed lane | Walk range (incl.) | Torch ends (y `+2` / y `+11`) | Cite |
|---|---|---|---|---|---|
| West | `i = -baseheight + m` | z `13..17` (`basedepth/2 + p`) | x `-10..+8` | x `-10` / x `+8` | GD:3784-3806 |
| East | `i = basewidth + baseheight - m - 1` | z `13..17` | x `+50..+32` (descending) | x `+50` / x `+32` | GD:3808-3830 |
| North | `k = -baseheight + m` | x `18..22` (`basewidth/2 + p`) | z `-10..+8` | z `-10` / z `+8` | GD:3832-3854 |
| South | `k = basedepth + baseheight - m - 1` | x `18..22` | z `+40..+22` (descending) | z `+40` / z `+22` | GD:3856-3878 |

Because every write is gated on an air read, the ramps conform to whatever is already there: they
dock into the pyramid steps built in §3.1 (reads hit the ring walls and stop), and the support
pillars extend down through open terrain to relative y 0. **This cannot be reproduced by a writer
that cannot read the world mid-build** — see §10.

### 3.3 Inner temple shell (GD:3879-3919)

`cposx += 10; cposy += 10; cposz += 10` (GD:3879-3881). Loop `j = 0..8`, `i = 0..20`, `k = 0..10`;
write at temple-relative `(i, j, k)` (GD:3916) → base-relative x `10..30`, y `+10..+18`, z `10..20`.

Cell rules in override order (GD:3885-3915):

| # | Rule | Where | Block | Cite |
|---|---|---|---|---|
| 1 | default | interior | air | GD:3885 |
| 2 | perimeter (`i`/`k` == 0 or max) | walls | stone / cobble / mossy, same 3/8-3/8-1/4 rolls as §3.1 | GD:3887-3895 |
| 3 | `j==0 \|\| j==8` | floor + ceiling slabs (21×11) | stone brick | GD:3896-3898 |
| 4 | doorways, `j ∈ {1,2,3}`: N/S walls (`k==0`/`k==10`) at `i 9..11`; E/W walls (`i==0`/`i==20`) at `k 4..6` | 3-wide × 2-tall openings mid-wall, all four sides | `j==3` → **oak fence** (lintel row), else air | GD:3899-3906 |
| 5 | `j==6` (`height-3`) and `(i+k)%2==1` and cell not air | checkerboard in the walls at y `+16` | **lit redstone lamp** (`field_150374_bv`) | GD:3907-3911 |
| 6 | `j==7` (`height-2`) and `(i+k)%2==1` | checkerboard at y `+17` — punches openings through the walls | air | GD:3907, 3912-3914 |

### 3.4 Roof slab rim (GD:3920-3928)

`j = height = 9` (base y `+19`). For `i = -1..21`, `k = -1..11` (temple-relative): place
**stone slab** only when on the extended rim ring (`i==-1 || k==-1 || i==21 || k==11`) **and**
`(i+k)&1 == 1` (GD:3925-3926) — a checkered crenellation one block outside the temple walls, one
above the ceiling (base-relative ring x `9..31` / z `9..21` at y `+19`, floating off the roof edge).

### 3.5 Five water altars (`makepoolalter`, GD:3929-3933, helper GD:3954-3961)

Helper: 3×3 **cobblestone** pad at `(cx±1, cy+1, cz±1)`, then center block replaced with **water**
(still, `field_150355_j`) (GD:3955-3960). All five calls pass the temple origin ± offsets, so pads
sit at temple j `+1` (base y `+11`), resting on the temple floor:

| Altar center (temple-rel) | Pad spans (temple-rel) | Base-rel center | Cite |
|---|---|---|---|
| `(1, +1, 1)` | x `0..2`, z `0..2` — **overlaps the W and N wall columns at j=1** | `(11, +11, 11)` | GD:3929 |
| `(19, +1, 9)` | x `18..20`, z `8..10` — overlaps E and S walls | `(29, +11, 19)` | GD:3930 |
| `(1, +1, 9)` | x `0..2`, z `8..10` — overlaps W and S walls | `(11, +11, 19)` | GD:3931 |
| `(19, +1, 1)` | x `18..20`, z `0..2` — overlaps E and N walls | `(29, +11, 11)` | GD:3932 |
| `(10, +1, 5)` (room center) | x `9..11`, z `4..6` | `(20, +11, 15)` | GD:3933 |

### 3.6 Center-altar guard: 4 CreeperRepellent blocks (GD:3934-3937)

`func_147465_d(..., OreSpawnMain.CreeperRepellent, 0, 2)` at temple-relative
`(9, +2, 4)`, `(11, +2, 6)`, `(9, +2, 6)`, `(11, +2, 4)` — the diagonal corners one above the
center altar pad (base-rel `(19/21, +12, 14/16)`).

### 3.7 Molenoid spawner (GD:3938-3942)

Mob spawner at temple-relative `(width/2 - 2, +1, depth/2)` = `(8, +1, 5)` (base-rel
`(18, +11, 15)`), entity name **"Molenoid"** via `func_98272_a` (GD:3941).

### 3.8 Trapdoor + ladder shaft into the base cavity (GD:3943-3950)

| # | What | Position (temple-rel → base-rel) | Block/meta | Cite |
|---|---|---|---|---|
| 1 | Trapdoor | `(12, +1, 5)` → `(22, +11, 15)` | wooden trapdoor, meta 3 | GD:3943 |
| 2 | Floor hole | `(12, +0, 5)` → `(22, +10, 15)` — punches the temple floor slab | air | GD:3944 |
| 3 | Shaft, `j = 1..9` | backing column `(12, -j, 6)` → `(22, +10-j, 16)`, y `+9..+1` | cobblestone | GD:3947-3948 |
| 4 | | ladder `(12, -j, 5)` → `(22, +10-j, 15)`, y `+9..+1` | ladder, meta 2 (faces −Z, mounted on the cobble column at z+1) | GD:3949 |

The shaft descends through the hollow pyramid interior from the temple floor to base y `+1`
(ground level inside the cavity).

### 3.9 Graveyard inside the base cavity (`makeincagraves`, GD:3951 / 3963-3977)

Called with the **original** base origin (`cpos - baseheight` un-does the §3.3 shift, GD:3951) and
`width=41, depth=31`. Grave centers: `x ∈ {5, 11, 17, 23, 29, 35}` (`i` from 5 step 6 while
`i < 36`, GD:3965) on four rows — 24 graves total:

| Row | z (base-rel) | `dir` | Chest sits at | Cite |
|---|---|---|---|---|
| 1 | `+5` | 1 | z `+4` (north of headstone) | GD:3965-3967 |
| 2 | `+10` | 1 | z `+9` | GD:3968-3970 |
| 3 | `+20` | 3 | z `+21` (south of headstone) | GD:3971-3973 |
| 4 | `+25` | 3 | z `+26` | GD:3974-3976 |

`makeincagrave(world, gx, gy=cposy, gz, dir)` (GD:3979-4042), dir=1 layout (dir=3 is the exact
z-mirror, GD:4013-4041; offsets below negate for dir=3):

| # | What | Where (rel. grave center gx,gz; y rel. cposy) | Block | Cite (dir 1 / dir 3) |
|---|---|---|---|---|
| 1 | Flower beds | `(gx±1, +0, gz..gz+2)` grass (replaces the stone-brick floor plate); `(gx±1, +1, gz)` poppy, `(gx±1, +1, gz+1)` dandelion, `(gx±1, +1, gz+2)` poppy | grass / red flower / yellow flower | GD:3985-3996 / 4014-4025 |
| 2 | Headstone | `(gx, +1, gz)` | stone | GD:3997 / 4026 |
| 3 | Grave slab | `(gx, +1, gz+1)` and `(gx, +1, gz+2)` | stone slab | GD:3998-3999 / 4027-4028 |
| 4 | Ghost spawner — **only if `nextInt(3)==1` (1/3 per grave)** | `(gx, +2, gz)` atop the headstone | mob spawner, **"Ghost"** | GD:4000-4006 / 4029-4035 |
| 5 | Loot chest | `(gx, +1, gz-1)`, chest meta 2 (faces north) | chest ← `IncaPyramidContentsList`, `10 + nextInt(5)` = **10-14 weighted pulls** | GD:4007-4011 / 4036-4040 |

Expected Ghost spawners: 24 × 1/3 = **8** (0-24 possible). Chests: **24**, all identical list.
All graves sit inside the hollow base cavity (max grave y `+2`; the lowest ring walls near those
x/z are at y ≥ 4), reachable via the §3.8 ladder shaft or the wall doorways… note the temple
doorways open onto the pyramid TOP; the cavity itself is only accessible via the shaft.

---

## 4. Loot table — FULL transcription

### `IncaPyramidContentsList` (GD:38) — 14 entries, total weight **480**

Constructor semantics: `WeightedRandomChestContent(item, meta, minStack, maxStack, weight)`;
meta 0 everywhere. Used only by the 24 grave chests (GD:3983, 4010, 4039), 10-14 pulls each,
random slots (collisions overwrite).

| # | 1.7.10 item | Port mapping (cite) | min | max | wt |
|---|---|---|---|---|---|
| 1 | `Items.field_151010_B` (golden sword) | `minecraft:golden_sword` | 1 | 1 | 35 |
| 2 | `Items.field_151151_aj` (golden boots) | `minecraft:golden_boots` | 1 | 1 | 35 |
| 3 | `Items.field_151149_ai` (golden leggings) | `minecraft:golden_leggings` | 1 | 1 | 35 |
| 4 | `Items.field_151169_ag` (golden helmet) | `minecraft:golden_helmet` | 1 | 1 | 35 |
| 5 | `Items.field_151171_ah` (golden chestplate) | `minecraft:golden_chestplate` | 1 | 1 | 35 |
| 6 | `Blocks.field_150327_N` as item (yellow flower) | `minecraft:dandelion` | 3 | 10 | 35 |
| 7 | `Blocks.field_150328_O` as item (red flower) | `minecraft:poppy` | 3 | 10 | 35 |
| 8 | `Items.field_151074_bl` (gold nugget) | `minecraft:gold_nugget` | 3 | 10 | 35 |
| 9 | `Items.field_151043_k` (gold ingot) | `minecraft:gold_ingot` | 3 | 10 | 35 |
| 10 | `Items.field_151062_by` (bottle o' enchanting) | `minecraft:experience_bottle` | 4 | 10 | 35 |
| 11 | `OreSpawnMain.MyCornCob` "corn_seed" (OSM:1909) | `ModItems.CORN_COB` (MI:593) | 4 | 10 | 35 |
| 12 | `OreSpawnMain.MyExperienceCatcher` (OSM:1948) | `ModItems.EXPERIENCE_CATCHER` (MI:622) | 4 | 10 | **25** |
| 13 | `Items.field_151103_aS` (bone) | `minecraft:bone` | 4 | 10 | 35 |
| 14 | `Blocks.field_150340_R` as item (gold block) | `minecraft:gold_block` | 4 | 10 | 35 |

---

## 5. Block palette — modern mapping

| 1.7.10 field | Modern block | Used for | Cite |
|---|---|---|---|
| `Blocks.field_150350_a` | `minecraft:air` | cavity interiors, doorways, y+17 checker holes, floor hole | GD:3759/3885/3944 |
| `Blocks.field_150348_b` | `minecraft:stone` | wall roll default, ramp support pillars, headstones | GD:3761/3802/3997 |
| `Blocks.field_150347_e` | `minecraft:cobblestone` | wall roll (3/8), altar pads, shaft backing | GD:3763/3957/3948 |
| `Blocks.field_150341_Y` | `minecraft:mossy_cobblestone` | wall roll (1/4) | GD:3766/3893 |
| `Blocks.field_150417_aV` | `minecraft:stone_bricks` | base floor plate, temple floor/ceiling, ramp rails | GD:3770/3897/3793 |
| `Blocks.field_150478_aa` | `minecraft:wall_torch` (meta 3 = facing south, meta 4 = facing north) / `minecraft:torch` (ramp ends, meta 0) | base-wall torch rows; ramp end torches | GD:3773-3779, 3795 etc. |
| `Blocks.field_150333_U` (meta 0) | `minecraft:smooth_stone_slab` (bottom) — 1.7.10 stone slab | ramp treads, roof rim, grave slabs | GD:3799/3920/3998 |
| `Blocks.field_150422_aJ` | `minecraft:oak_fence` | doorway lintel rows (j=3) | GD:3901/3904 |
| `Blocks.field_150374_bv` | `minecraft:redstone_lamp` [lit=true] | y+16 wall checkerboard | GD:3910 |
| `Blocks.field_150355_j` | `minecraft:water` (source) | altar pool centers | GD:3960 |
| `Blocks.field_150474_ac` | `minecraft:spawner` | Molenoid + Ghost spawners | GD:3938/4001/4030 |
| `Blocks.field_150486_ae` (meta 2) | `minecraft:chest` [facing=north] | 24 grave chests | GD:4007/4036 |
| `Blocks.field_150415_aT` (meta 3) | `minecraft:oak_trapdoor` | shaft entrance in temple floor | GD:3943 |
| `Blocks.field_150468_ap` (meta 2) | `minecraft:ladder` [facing=north] | shaft ladder | GD:3949 |
| `Blocks.field_150349_c` | `minecraft:grass_block` | grave flower beds (replace floor plate) | GD:3985 etc. |
| `Blocks.field_150328_O` | `minecraft:poppy` | grave flowers | GD:3986 etc. |
| `Blocks.field_150327_N` | `minecraft:dandelion` | grave flowers | GD:3988 etc. |
| `OreSpawnMain.CreeperRepellent` "creeperrepellent" (OSM:1930) | `ModBlocks.CREEPER_REPELLENT` "creeper_repellent" (MB:144) | 4 blocks over center altar | GD:3934-3937 |

---

## 6. Mob mapping table

Both spawns are **spawner blocks**; names are 1.7.10 `EntityList` names set via `func_98272_a`.

| Spawner name | 1.7.10 class (OSM registration) | Port EntityType (ME line) | Used by |
|---|---|---|---|
| "Molenoid" | `Molenoid` (OSM:4329, `registerGlobalEntityID(Molenoid.class, "Molenoid", ...)`) | `ENTITY_MOLENOID` (ME:246) | 1 spawner in temple (GD:3938-3942) |
| "Ghost" | `Ghost` (OSM:4047) | `GHOST` (ME:549) | up to 24 grave spawners, 1/3 each (GD:4000-4006/4029-4035) |

---

## 7. Footprint extents (relative to base origin `(cposx, cposy, cposz)`)

| Axis | Min | Max | Extent | Determined by |
|---|---|---|---|---|
| X | `-10` | `+50` | **61** | west ramp start i=−10 (GD:3785) / east ramp start i=+50 (GD:3809); base itself 0..40 |
| Z | `-10` | `+40` | **51** | north ramp k=−10 (GD:3833) / south ramp k=+40 (GD:3857); base itself 0..30 |
| Y | `+0` | `+19` | **20** | floor plate + ramp pillar floor (`while j >= 0`, GD:3801 — never digs below cposy) / roof slab rim at +19 (GD:3922-3926); highest torches: ramp near-end at +11, temple lamps +16 |

---

## 8. Worldgen call context (Islands dimension, D4)

Dimension: `DimensionID4 = BaseDimensionID + 3` (OSM:1598), provider `WorldProviderOreSpawn4`
(OSM:5385-5386) → port `ModDimensionKeys.ISLANDS` (`orespawn:islands`).

Dispatch chain (OSW:132-146):
1. dimension check `field_76574_g == DimensionID4` (OSW:132);
2. gate: `recently_placed == 0 && random.nextInt(100) == 0 && D4BigSpaceCheck(world, chunkX*16, 7, chunkZ*16)` (OSW:134). `D4BigSpaceCheck` (OSW:2655-2664) **reads** the plane y = 7+4 = 11 over x `-25..+39`, z `-25..+29` and fails on anything that is not air / vanilla log / `MyAppleLeaves` / `MyScaryLeaves`;
3. `i = random.nextInt(19)` (OSW:135); `i == 8` → `addD4IncaPyramid` (OSW:144-146) — 1/19 given the gate.

`addD4IncaPyramid` (OSW:2345-2366):
1. **LessLag gate**: `if (OreSpawnMain.LessLag != 0 && random.nextInt(2) != 0) return false;`
   (OSW:2346-2348) — halves placement when LessLag ≥ 1 (config OSM:1471, clamped 0..2 OSM:1550-1556);
2. `posX/posZ = chunk + nextInt(8)` (OSW:2349-2350);
3. **ground scan**: `posY` from 20 down to 5, looking for **grass** at `(posX, posY, posZ)`
   (world read, OSW:2351-2353); no grass in that band → no structure;
4. **clearance check**: reads the full plane `(posX-10..posX+49, posY+18, posZ-10..posZ+39)`;
   any non-air → `return false` (OSW:2354-2360);
5. `makeIncaPyramid(world, posX, posY, posZ)` — origin AT the grass Y (OSW:2361) —
   then `recently_placed = 50` (OSW:2362).

`recently_placed`: static, init 50 (OSW:30), decremented once per generate call (OSW:37-39),
reset to 50 by every successful OreSpawn structure, global across dimensions.

No overworld or other-dimension placement site exists: the only `makeIncaPyramid` callers in the
entire 1.7.10 source are OSW:2361 (Islands D4) and DSB:141 (spawner block type 29).

---

## 9. DungeonSpawnerBlock trigger

Type **29** of the 50-way roll → `OreSpawnMain.MyDungeon.makeIncaPyramid(world, clickedX,
clickedY, clickedZ)` (DSB:140-142). Timing/self-delete mechanics at DSB:35-40, 50-52.
Port: the type is absent from `RandomDungeonSpawnerBlockEntity`'s pool (RDS:30-31, default
fallthrough RDS:134) — **MISSING-IN-PORT**.

---

## 10. ⚠ World-block READS mid-build (chunk-stitching hazard)

Per `phase_d_reports/structure_conversion_pattern.md` §1 step 3, the port's chunk-stitching
writer cannot read the world. The original Inca Pyramid reads in these places:

1. **All four ramps** (GD:3791, 3798, 3801 / 3815, 3822, 3825 / 3839, 3846, 3849 / 3863, 3870,
   3873): every rail, tread and support-pillar write is conditioned on `func_147439_a(...) ==
   air`. Two distinct behaviors depend on it:
   - ramps do not overwrite the already-placed pyramid step walls where they dock (the reads at
     x/z ≥ 0 hit §3.1 blocks placed earlier in the same call — self-reads, reproducible from the
     structure's own write set);
   - support pillars extend down to **terrain** (stop at first non-air), and rails/treads skip
     cells occupied by pre-existing terrain — these read the **pre-build world** and cannot be
     derived from the write set. A faithful port needs terrain-aware post-processing or an
     accepted deviation (e.g. always-fill pillars to relative y 0).
2. **Placement scans** (not mid-build, but reads): D4BigSpaceCheck plane (OSW:2656-2661), grass
   column scan (OSW:2351-2353), 60×50 clearance plane at y+18 (OSW:2354-2360).
3. Tile-entity fetches after spawner/chest writes (`func_147438_o`, GD:78/89) — reads of blocks
   the structure itself just placed; harmless.

Everything else in the build (§3.1, §3.3-3.9) is write-only.

---

## 11. Surprises / MISSING-IN-PORT

1. **MISSING-IN-PORT (whole structure)**: no `INCA_PYRAMID` DungeonType (LDP:79-125), no builder
   in the port `world/GenericDungeon.java`, no Islands `i==8` counterpart, no spawner-pool type 29
   (RDS:134 falls back to a generic dungeon).
2. **Clearance check is 1 block short of the footprint**: it scans x `-10..+49`, z `-10..+39`
   (OSW:2354-2355) but the east ramp reaches x `+50` and the south ramp z `+40` (GD:3809/3857) —
   the outermost ramp column is placed unchecked.
3. **The pyramid is hollow** with a hidden graveyard (24 graves, 24 chests, ~8 Ghost spawners) at
   ground level; the only intended access is the trapdoor + ladder shaft from the temple
   (GD:3943-3950). The temple doorways open onto the pyramid top, not into the cavity.
4. **Grave rows use only dir 1 and dir 3** (GD:3966-3975); `makeincagrave` has no code for any
   other dir value — dir 2/4 (E/W-facing) graves do not exist.
5. **Ramp end torches are meta 0** (`meta` reset at GD:3783 and never changed in the ramp loops)
   — an unattached/default torch value, unlike the deliberate meta 3/4 wall torches of the base.
6. **Corner altar pads overwrite temple wall cells**: the four corner `makepoolalter` calls
   (GD:3929-3932) write their 3×3 cobble ring into the wall columns at temple i=0/20, k=0/10 at
   j=1 (cobble replacing rolled stone/cobble/mossy — visually near-invisible, but a faithful
   port must keep the write order).
7. **Roof rim slabs float**: the y `+19` checkered ring sits at i=−1/k=−1/i=21/k=11, one block
   outside the ceiling slab, with nothing beneath (GD:3922-3926).
8. **Torch rows are huge**: layers j∈{2,5,8} carry full-width interior torch rows on both N and S
   ring walls — e.g. layer 2 places 2×35 torches (GD:3772-3779).
9. The structure origin is AT grass level (OSW:2351-2361), so the 41×31 stone-brick plate
   replaces the grass surface rather than sitting on it.
10. `IncaPyramidContentsList` weight 480; the only non-35 weights are ExperienceCatcher (25) —
    all five golden-gear entries are single-item pulls (GD:38).
