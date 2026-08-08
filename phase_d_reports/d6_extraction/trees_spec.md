# Trees.java — DuplicatorTree / ExperienceTree / FairyTree / FairyCastleTree — Extraction Spec (1.7.10 → NeoForge 1.21.1)

Slice: D6 (tree generators). Original: `reference_1_7_10_source\sources\danger\orespawn\Trees.java` (whole file read).
Abbreviations: **T** = `Trees.java`, **OSM** = `OreSpawnMain.java`, **OSW** = `OreSpawnWorld.java`,
**DSB** = `DungeonSpawnerBlock.java`, **BDL** = `BlockDuplicatorLog.java`, **BEP** = `BlockExperiencePlant.java`,
**IETS** = `ItemExperienceTreeSeed.java` (all under `reference_1_7_10_source\sources\danger\orespawn\`).
Port abbreviations: **CS** = `src\main\java\danger\orespawn\world\CrystalStructures.java`,
**pBDL** = `src\main\java\danger\orespawn\block\BlockDuplicatorLog.java`,
**pBEP** = `src\main\java\danger\orespawn\block\BlockExperiencePlant.java`,
**RDSBE** = `src\main\java\danger\orespawn\block\entity\RandomDungeonSpawnerBlockEntity.java`,
**MB** = `src\main\java\danger\orespawn\ModBlocks.java`, **MI** = `src\main\java\danger\orespawn\ModItems.java`.

Obfuscation key used below: `func_147439_a` = getBlock (WORLD READ), `func_147437_c` = isAirBlock (WORLD READ),
`func_72805_g` = getBlockMetadata (WORLD READ), `func_147465_d` = setBlock(flags), `func_147438_o` = getTileEntity,
`field_73012_v` = `world.rand`, `field_150364_r` = oak log, `field_150362_t` = oak leaves, `field_150349_c` = grass block,
`field_150346_d` = dirt, `field_150458_ak` = farmland, `field_150350_a` = air, `field_150474_ac` = mob spawner,
`field_150486_ae` = chest.

`Trees` is instantiated once as `OreSpawnMain.OreSpawnTrees` (OSM:528, OSM:5785). All four generators are instance
methods on it. All block placement goes through `OreSpawnMain.setBlockFast(world, x, y, z, block, meta=0, flags=2)`
(OSM:5833-5852 — direct chunk write, marks for render update, no neighbor notifications with flags=2), except where
noted (`func_147465_d` for the duplication copy, spawners, and chests).

---

## 1. Entry points (ALL trigger sites)

| Generator | Trigger | Cite | Context |
|---|---|---|---|
| `FairyTree(world,x,y,z)` T:452 | DungeonSpawnerBlock outcome **type 0** of `nextInt(50)` | DSB:52-55 | Player-placed Random Dungeon Spawner block; fires from `updateTick` after the 400-tick schedule (DSB:35-40) **or any random tick** (`func_149675_a(true)`, DSB:22); block + block above are cleared to air first (DSB:50-51); tree called at the (now-air) block pos |
| `FairyCastleTree(world,x,y,z)` T:617 | DungeonSpawnerBlock outcome **type 1** | DSB:56-58 | same as above |
| `FairyTree` | Natural worldgen, **Crystal dimension** (DimensionID5 = `WorldProviderOreSpawn5`, OSM:1599/5387-5388) | OSW:187-188, OSW:1962-1996 | `addFairyTree` — 1/5 per chunk, ground scan (§7.4), then 4/5 of successes → `FairyTree(world, posX, posY-1, posZ)` (OSW:1987-1988) |
| `FairyCastleTree` | Natural worldgen, Crystal dimension | OSW:1989-1991 | same `addFairyTree`, 1/5 of successes → `FairyCastleTree(world, posX, posY, posZ)` (note: **castle gets posY, plain tree gets posY-1**) |
| `DuplicatorTree(world,x,y,z)` T:121 | `BlockDuplicatorLog.func_149674_a` — **every update/random tick** of any Duplicator Log block (`MyDT`), server side, gated by `enableduplicatortree != 0` | BDL:32-39 (tick), OSM:1482 (config `DuplicatorTreeEnable`, default 1), OSM:388 | `MyDT` = BlockDuplicatorLog (OSM:1946, registered "OreSpawn_DuplicatorLog" OSM:2161). `func_149675_a(true)` (BDL:25) → random ticks. The log itself is the sapling AND the trunk: the generator is a **multi-tick state machine** keyed off what already exists in the world |
| `MyDT` log seeded by worldgen | `addVeggies` crop picker, `what == 5` fall-through case | OSW:1915-1917 | 1/15 chunk gate (OSW:1883), dims Utopia (`DimensionID` = `WorldProviderOreSpawn`)/Mining (`DimensionID2`)/Islands (`DimensionID6`) **or overworld biomes named "River"/"Swampland"** (OSW:1887); 8 attempts/chunk (OSW:1888), scan y=100→41 while air for grass below (OSW:1891-1892); `what = nextInt(6)` (OSW:1893); the `what==5` branch places a single `MyDT` log on 1/50 roll if `enableduplicatortree != 0` (OSW:1915-1916). `addVeggies` call sites: OSW:46 (Utopia), OSW:110 (Mining), OSW:205 (Islands), OSW:279 (`generateSurface` → overworld case 0 at OSW:214-215 and Utopia at OSW:41) |
| Player placement of the `MyDT` block item | shapeless plank recipe exists (OSM:5365) but item drops itself (BDL:45-47, :57-59) — any placed log restarts/continues growth | BDL:32-39 | e.g. logs harvested from a grown tree replant it |
| `ExperienceTree(world,x,y,z)` T:294 | `BlockExperiencePlant.func_149674_a` — update/random tick of the Experience Plant, server side, 1/10 per tick; plant replaced with air, then `ExperienceTree(world, x, y-1, z)` | BEP:42-51 | `MyExperiencePlant` = BlockExperiencePlant "experiencesapling" (OSM:1950). Plant is placed by `ItemExperienceTreeSeed.func_77648_a` right-click on grass/dirt/farmland at y+1 (IETS:25-41); seed item `MyExperienceTreeSeed` (OSM:1949, recipe OSM:3396 = 8×XP bottle around an Apple Seed; drops from Hammerhead (`Hammerhead.java:144`) and appears in CloudShark/shadow-dungeon loot (GenericDungeon.java:47, :52)) |
| `ExperienceTree` natural worldgen | **NONE** — grep over all original sources shows the only caller is BEP:50 | (grep) | no `addExperienceTree` exists in OSW |
| `DuplicatorTree`/`ExperienceTree` via DSB | **NOT in the DSB table** — DSB types 0-49 never call them | DSB:52-202 | — |

Out-of-scope members of the same file (already covered elsewhere): `WindTree` (T:45-77), `SkyTree` (T:96-119),
`SmallTree` (T:335-383, IslandToo-only), `ScragglyTreeWithBranches` (T:418-450, dead code — see WGEN-046 resolution
in AUDIT_FINDINGS.md).

---

## 2. `DuplicatorTree(world, x, y, z)` — T:121-182

Called with `(x,y,z)` = position of the ticking `MyDT` log. **One action per tick**; every call re-derives state
from world reads and performs at most one block write, then returns. Sequence of gates, in order:

### 2.1 Soil gate (T:124-139) — WORLD READS
- Read block at `(x, y-1, z)` (T:125). Accepted soils: grass, dirt, farmland (T:126).
- If y-1 is NOT soil: reads at y-2 (T:127) and y-3 (T:129) occur and `realy` is assigned y-3 (T:133) or
  y-2 (T:135), **but the branch unconditionally returns at T:137 (and T:131)** — the y-2/y-3 "buried log"
  re-anchoring is dead code (CFR-visible artifact of the original control flow). Effective behavior: *a log with
  soil directly below it grows; any other log does nothing.*
- On success `realy = y - 1` (T:139) — the soil block. All later offsets are relative to `realy`.

### 2.2 Incremental growth state machine (one write per tick, each step returns)

| Step | Condition (WORLD READ) | Action (write, flags=2) | Cite |
|---|---|---|---|
| 1 | `(x, realy+1, z)` ≠ `MyDT` | set `MyDT` at `realy+1` | T:140-144 |
| 2 | `(x, realy+2, z)` ≠ `MyDT` | set `MyDT` at `realy+2` | T:145-149 |
| 3 | `(x, realy+3, z)` ≠ `MyDT` | set `MyDT` at `realy+3` | T:150-154 |
| 4 | `(x, realy+4, z)` ≠ `MyAppleLeaves` | set `MyAppleLeaves` at `realy+4` (cap) | T:155-159 |
| 5 | scan `i ∈ [-1,1]`, `j ∈ [-1,1]`, skip (0,0): first cell at `(x+i, realy+3, z+j)` ≠ `MyAppleLeaves` | set `MyAppleLeaves` there (3×3 ring around top log, 8 cells, deterministic row-major order i=-1..1 outer, j=-1..1 inner) | T:160-166 |

Final tree: 3-high `MyDT` trunk on soil, 3×3×1 `MyAppleLeaves` layer at `realy+3` (ring) + 1 cap at `realy+4`.
Footprint: 3×3 horizontal (x±1, z±1) during growth; 5×5 (x±2, z±2) once duplicating; height `realy+1 .. realy+4`.

### 2.3 Duplication step (only once fully grown) — T:167-181
- Outer loop: up to 20 tries (T:168): pick `i, j = nextInt(5)-2` (T:169-170), READ block **and metadata** at
  `(x+i, realy+1, z+j)` (T:171-172, `func_72805_g`). Retry while the pick is air or `MyDT` (loop condition T:168 +
  `continue` T:173).
- On a valid source: inner loop, up to 20 tries (T:174): pick new `i, j = nextInt(5)-2`, READ `(x+i, realy+1, z+j)`
  (T:176); first air cell gets a copy of the source **block + metadata** via `world.func_147465_d(..., bidm, meta, 2)`
  (T:178 — note: NOT `setBlockFast`; this is the plain setBlock, still flags=2) and the method returns (T:179).
- If the inner loop finds no air, the outer loop condition (`bidm` no longer air/`MyDT`) is false → method exits with
  no write. Net effect: each tick, ~one random non-air non-log block in the 5×5 ring at trunk-base level is cloned
  into a random air cell of the same 5×5 — the namesake item/block duplication.

**World reads mid-build: pervasive and load-bearing** — soil probe (T:125-130), trunk probes (T:140-155), cap/ring
probes (T:156-162), duplication source read incl. metadata (T:171-172), destination air check (T:176-177). This
generator CANNOT be expressed as a build-once feature; it must live on a ticking block (as the original does).

---

## 3. `ExperienceTree(world, x, y, z)` — T:294-333

Called with `(x,y,z)` = soil position (BEP passes `par3 - 1`, BEP:50). Single-shot build (whole tree in one call).

### 3.1 Soil gate (T:298-301) — WORLD READ
Block AT `(x, y, z)` must be grass, dirt, or farmland, else return with nothing built.

### 3.2 Geometry (all trunk/branch wood = **vanilla oak log** `Blocks.field_150364_r`, meta 0)

| Phase | Blocks | Inclusive ranges | Cite |
|---|---|---|---|
| Lower trunk | oak log, 2×2 | x..x+1, z..z+1, y+1..y+5 (j=1..5) | T:302-308 |
| 4 large branches | `grow_branch` from the 4 trunk corners at y+6 | args (x,y+6,z,0,1,1,1), (x+1,y+6,z,1,0,1,-1), (x,y+6,z+1,-1,0,-1,1), (x+1,y+6,z+1,0,-1,-1,-1) | T:309-312 |
| Mid trunk | oak log, 2×2 | y+7..y+18 (j=7..18) | T:313-319 |
| 4 small branches | `grow_small_branch` from corners at y+19 | args (x,y+19,z,0,1,-1,1), (x+1,y+19,z,1,0,1,1), (x,y+19,z+1,-1,0,-1,-1), (x+1,y+19,z+1,0,-1,1,-1) | T:320-323 |
| Crown trunk | oak log 2×2 + `make_leaves` at every log | y+19..y+18+grow, `grow = 5 + nextInt(6)` ∈ [5,10] → top log ≤ y+28 | T:324-332 |

Note trunk row y+6 itself is only wood where the branch seg-1 starts write it — the 2×2 loops skip j=6
(first loop ends at j=5, second starts at j=7); the four `grow_branch` calls each place their first log at one
trunk corner at y+6, filling that layer.

### 3.3 `make_leaves(world, x, y, z)` — T:184-194 — WORLD READS
7×7×3 box: `l1 ∈ [-3,3]`, `l2 ∈ [-3,3]`, `l3 ∈ [0,2]` (offsets x+l1, y+l3, z+l2). Each cell READ (T:188); only air
cells are set to `MyExperienceLeaves` (T:190). Called after every single branch/crown log placement — leaves never
overwrite anything.

### 3.4 `grow_branch(world, x, y, z, xdir, zdir, xxdir, zzdir)` — T:245-292
State: `(i,j,k)` starts at the call pos; `(i2,k2)` snapshots the seg-1 endpoint; every placed log also calls
`make_leaves` at itself. Five segments, in order:

| Seg | Length | Per-step (place log at cursor, then advance) | Cursor | Cite |
|---|---|---|---|---|
| 1 (rising) | `5 + nextInt(4)` ∈ [5,8] | `++j; i += xdir; k += zdir` (`i2=i, k2=k` track) | (i,j,k) | T:253-260 |
| 2 (level, main arm) | `6 + nextInt(5)` ∈ [6,10] | `i += xdir; k += zdir` | (i,j,k) | T:261-267 |
| 3 (level, fork) | `6 + nextInt(5)` ∈ [6,10] | `i2 += xxdir; k2 += zzdir` at same j | (i2,j,k2) | T:268-274 |
| — | `j2 = --j` (drop one) | | | T:275 |
| 4 (drooping, main) | `4 + nextInt(4)` ∈ [4,7] | `i += xdir; k += zdir; --j` | (i,j,k) | T:276-283 |
| 5 (drooping, fork) | `4 + nextInt(4)` ∈ [4,7] | `i2 += xxdir; k2 += zzdir; --j2` | (i2,j2,k2) | T:284-291 |

Seg 2 continues from seg 1's endpoint; seg 3 forks from the SAME endpoint in the secondary direction; segs 4/5
continue segs 2/3 outward while descending 1/block. Max main-arm run = 8+10+7 = 25 steps from the trunk corner
(+3 leaf radius → 28); fork max = 10+7 = 17 steps in the (xxdir,zzdir) direction from a point already ≤8 out.
Branch apex = y+6+8 = y+14 (+2 leaf box → y+16); droop bottom worst case = base + g1 − g4 (seg2 runs at
j = y+6+g1; `j2 = --j` drops 1; seg4 descends g4−1 further) = y+6+5−7 = **y+4** (min g1, max g4).

### 3.5 `grow_small_branch` — T:196-243
Identical 5-segment structure and cursor math, smaller rolls (same cites order as grow_branch):
seg1 `4+nextInt(2)` ∈ [4,5] (T:204), seg2 `4+nextInt(3)` ∈ [4,6] (T:212), seg3 `4+nextInt(3)` ∈ [4,6] (T:219),
seg4 `3+nextInt(3)` ∈ [3,5] (T:227), seg5 `3+nextInt(3)` ∈ [3,5] (T:235). Called at y+19: apex ≤ y+24 (+2 → y+26);
max run 5+6+5 = 16 (+3 leaves → 19).

### 3.6 Footprint
Horizontal: worst case ±28 around the 2×2 trunk (large branch main arm + leaf radius); typical ±20.
Vertical: y+1 .. y+30 (crown top log y+28 + `make_leaves` l3 ≤ 2). Nothing below y (soil untouched).
No spawners, no chests, no entities.

---

## 4. `FairyTree(world, x, y, z)` — T:452-490

**No soil/ground gate inside the generator** — callers pre-validate (worldgen) or don't validate at all (DSB).
Wood = `MyCrystalTreeLog`, leaves = `MyCrystalLeaves3`.

| Phase | Blocks | Inclusive ranges | Cite |
|---|---|---|---|
| Trunk | crystal tree log, 2×2 | x..x+1, z..z+1, y+1..y+5 (j=1..5) | T:456-462 |
| 8 branches | `grow_crystal_branch`, ydir = −1 for all | 4 at y+5: (x,y+5,z,0,1,1,1,-1), (x+1,y+5,z,1,0,1,-1,-1), (x,y+5,z+1,-1,0,-1,1,-1), (x+1,y+5,z+1,0,-1,-1,-1,-1); 4 at y+6: (x,y+6,z,0,1,-1,1,-1), (x+1,y+6,z,1,0,1,1,-1), (x,y+6,z+1,-1,0,-1,-1,-1), (x+1,y+6,z+1,0,-1,1,-1,-1) | T:463-470 |
| Crown trunk | log 2×2 + `make_crystal_leaves` per log | y+6..y+5+grow, `grow = 5 + nextInt(5)` ∈ [5,9] → top log ≤ y+14 | T:471-479 |
| **Fairy spawner** | vanilla mob spawner at `(x-1, y+1, z)` via `func_147465_d(..., flags=2)`; `TileEntityMobSpawner.func_145881_a().func_98272_a("Fairy")` (null-checked) | T:480-484 |
| **Loot chest** | vanilla chest at `(x+2, y+1, z)`; filled `WeightedRandomChestContent.func_76293_a(world.rand, CrystalChestContentsList, chest, 1 + nextInt(5))` → **1-5 weighted picks** (null-checked) | T:485-489 |

### 4.1 `make_crystal_leaves(world, x, y, z)` — T:492-502 — WORLD READS
5×5×2 box: `l1, l2 ∈ [-2,2]`, `l3 ∈ [0,1]`; READ each (T:496), set `MyCrystalLeaves3` only where air (T:498).

### 4.2 `grow_crystal_branch(world, x, y, z, xdir, zdir, xxdir, zzdir, ydir)` — T:520-597
Same 5-segment skeleton as `grow_branch` (§3.4) with crystal log + `make_crystal_leaves`, plus:
- **LessLag shrink on EVERY segment** (OSM:471 config): `LessLag == 1` → length −1; `LessLag == 2` → length −2
  (T:529-534, :543-548, :556-561, :570-575, :584-589).
- Segment base lengths (LessLag=0): seg1 `4+nextInt(4)` ∈ [4,7] (T:528), seg2 `5+nextInt(5)` ∈ [5,9] (T:542),
  seg3 `5+nextInt(5)` ∈ [5,9] (T:555), seg4 `4+nextInt(4)` ∈ [4,7] (T:569), seg5 `4+nextInt(4)` ∈ [4,7] (T:583).
- Droop segments use `j += ydir` / `j2 += ydir` (T:581, :595) — generalized, but every caller passes ydir = −1.

### 4.3 Footprint
Horizontal: main arm ≤ 7+9+7 = 23 steps (+2 leaf radius → **±25**). Vertical: crown top y+14 (+1 leaf → y+15);
droop bottom worst case (y+5 branch, min seg1 4, max seg4 7): base + g1 − g4 = y+5+4−7 = **y+2**; spawner/chest at y+1.

---

## 5. `FairyCastleTree(world, x, y, z)` — T:617-785

No ground gate. A vertical stack of `nc` tiers; each tier is 1-9 square log platforms pushed outward by a growing
`spread`. Wood = `MyCrystalTreeLog`; platform-edge skirts = `MyCrystalLeaves2`/`MyCrystalLeaves3`; corner lamps =
`CrystalTorch`; contents via `addSomething`.

### 5.1 Setup (T:618-626)
- `nc = 6`; `LessLag == 1` → 5; `LessLag == 2` → 4 (T:618-624).
- `j = 3 + nextInt(3)` ∈ [3,5] — height of the first tier above y (T:625). `spread = 0` (T:626).

### 5.2 Per-iteration `iter = 0 .. nc-1` (T:627-784)
Each tier rolls `grow = 4 + nextInt(3)` ∈ [4,6] (T:630) — the vertical step to the next tier — and builds
platforms. **Every platform** re-rolls its own `randy = nextInt(3) - 1` ∈ [-1,1] (vertical jitter) and, except the
first, its own `width`. A platform centered `(cx, cy, cz)` with half-width `w` is (pattern identical for all nine
placements, cites below per platform):

- Full square of crystal log: `cx-w..cx+w` × `cz-w..cz+w` at `cy` (e.g. T:633-635).
- On edge cells (`i == ±w` or `k == ±w`): `make_crystal_castle_leaves` at the cell (e.g. T:636-638).
- At the exact center (`i==0 && k==0`), **only when tier content is enabled**: `addSomething` (e.g. T:639-641).
- At the four corners (`i == -w && k == ±w`, T:642-644; `i == w && k == ±w`, T:645-646): `CrystalTorch` at `cy+1`.

Platforms per iteration:

| # | Center | Half-width | Content? | Cite |
|---|---|---|---|---|
| A (always) | `(x+spread, y+j+randy, z)` | `1 + nextInt(3)` ∈ [1,3] (T:631) | only if `iter != 0` (T:639) | T:633-648 |
| B (iter ≥ 1) | `(x-spread, y+j+randy, z)` | `1 + nextInt(3+iter)` | yes | T:649-667 |
| C (iter ≥ 1) | `(x, y+j+randy, z+spread)` | `1 + nextInt(3+iter)` | yes | T:668-685 |
| D (iter ≥ 1) | `(x, y+j+randy, z-spread)` | `1 + nextInt(3+iter)` | yes | T:686-703 |
| E (iter ≥ 2) | `(x+spread, y+j+randy, z+spread)` | `1 + nextInt(3+iter)` | yes | T:705-723 |
| F (iter ≥ 2) | `(x-spread, y+j+randy, z-spread)` | `1 + nextInt(3+iter)` | yes | T:724-741 |
| G (iter ≥ 2) | `(x-spread, y+j+randy, z+spread)` | `1 + nextInt(3+iter)` | yes | T:742-759 |
| H (iter ≥ 2) | `(x+spread, y+j+randy, z-spread)` | `1 + nextInt(3+iter)` | yes | T:760-777 |

End of iteration (T:779-783): `j += grow`; if `iter == 0` then `spread = 3`; `spread += grow`.
So spread sequence: after iter0 `3+g0` ∈ [7,9]; grows by g each later iter; at the last tier (iter 5, LessLag=0)
spread ∈ [23, 33]. Max platform half-width at iter5 = `1+nextInt(8)` ≤ 8.

### 5.3 `make_crystal_castle_leaves(world, x, y, z)` — T:504-518 — WORLD READS
3×3×2 box `l1, l2 ∈ [-1,1]`, `l3 ∈ [0,1]`; READ each (T:508); air cells only; layer `l3 == 0` gets
`MyCrystalLeaves2`, layer `l3 == 1` gets `MyCrystalLeaves3` (T:510-514). Because it runs per EDGE cell of the
platform at platform height, it drapes a 2-high leaf skirt around each platform rim (never overwriting logs —
placed-this-tick logs are not air).

### 5.4 `addSomething(world, x, y, z)` — T:599-615
`i = nextInt(3)`:
- `i == 1` (1/3): vanilla mob spawner at `(x, y+1, z)` (`func_147465_d`, flags 2), entity `"Fairy"` (T:601-607).
- `i == 2` (1/3): vanilla chest at `(x, y+1, z)` filled from `CrystalChestContentsList`, `1 + nextInt(5)` picks
  (T:608-614).
- `i == 0` (1/3): nothing.

Content-eligible platforms per iteration (nc=6): iter0: 0 (A exists but content is gated on `iter != 0`);
iter1: A,B,C,D = 4; iter2-5: A-H = 8 each = 32; total **36 `addSomething` rolls** → expected ≈12 spawners +
≈12 chests per castle (LessLag=0).

### 5.5 Footprint
Horizontal: max |offset| = spread(iter5) + w + 1 (leaf) ≤ 33 + 8 + 1 = **±42** (min = 23 + 1 + 1 = ±25); diagonal
platforms put mass at (±spread, ±spread). Vertical: first tier y+3−1 = y+2; last tier at `j ≤ 5+5·6 = 35` (+randy 1,
then torch/leaf +1) → **top ≈ y+37**; min top = y+3+5·4−1+1 = y+23. No ground contact below the lowest platform — it floats unless terrain
intersects (matches the "fairy castle in the sky" look).

---

## 6. Loot — `CrystalChestContentsList` (T:19) — FULL transcription

Constructor semantics: `WeightedRandomChestContent(item, meta, minStack, maxStack, weight)`; all meta = 0.
Fill: `func_76293_a(world.rand, list, chest, 1 + nextInt(5))` → **1-5 weighted pulls**, each pull a stack of
uniform `[min,max]` into a random slot. Used by FairyTree base chest (T:485-489) and every FairyCastleTree
`addSomething` chest (T:608-614). Total weight = **755**, 82 entries.
Port mapping: the port already transcribes this table 1:1 as the data-driven loot table
`src\main\resources\data\orespawn\loot_table\chests\crystal_chest.json` (rolls uniform 1-5; per-entry weight and
set_count verified against every row below); registry names below are that file's entries.

| # | 1.7.10 item (OSM cite) | Port id (crystal_chest.json) | min | max | wt |
|---|---|---|---|---|---|
| 1 | `CrystalTermiteBlock` | `orespawn:crystal_termite_block` | 1 | 5 | 10 |
| 2 | `CrystalFlowerRedBlock` | `orespawn:crystal_flower_red` (MB:489) | 1 | 10 | 10 |
| 3 | `CrystalFlowerBlueBlock` | `orespawn:crystal_flower_blue` (MB:493) | 1 | 10 | 10 |
| 4 | `CrystalFlowerGreenBlock` | `orespawn:crystal_flower_green` (MB:491) | 1 | 10 | 10 |
| 5 | `CrystalFlowerYellowBlock` | `orespawn:crystal_flower_yellow` (MB:495) | 1 | 10 | 10 |
| 6 | `CrystalPlanksBlock` | `orespawn:crystal_planks` | 1 | 10 | 10 |
| 7 | `CrystalWorkbenchBlock` | `orespawn:crystal_workbench` | 1 | 1 | 10 |
| 8 | `CrystalFurnaceBlock` | `orespawn:crystal_furnace` | 1 | 1 | 10 |
| 9 | `MyTigersEyeBlock` | `orespawn:block_tigers_eye` | 1 | 10 | 5 |
| 10 | `CrystalStone` | `orespawn:crystal_stone` | 1 | 10 | 10 |
| 11 | `CrystalRat` | `orespawn:crystal_rat` | 1 | 10 | 10 |
| 12 | `CrystalFairy` | `orespawn:crystal_fairy` | 1 | 10 | 10 |
| 13 | `CrystalCoal` | `orespawn:crystal_coal` | 1 | 10 | 10 |
| 14 | `CrystalGrass` (OSM:1866) | `orespawn:crystal_grass` (MB:89) | 1 | 10 | 10 |
| 15 | `CrystalCrystal` | `orespawn:crystal_crystal` | 1 | 10 | 10 |
| 16 | `CrystalTorch` (OSM:1938) | `orespawn:crystal_torch` (MB:136) | 1 | 10 | 10 |
| 17 | `MyCrystalLeaves` | `orespawn:crystal_leaves` (MB:461) | 1 | 10 | 10 |
| 18 | `MyCrystalLeaves2` (OSM:1967) | `orespawn:crystal_leaves_2` (MB:463) | 1 | 10 | 10 |
| 19 | `MyCrystalLeaves3` (OSM:1968) | `orespawn:crystal_leaves_3` (MB:465) | 1 | 10 | 10 |
| 20 | `MyCrystalTreeLog` (OSM:1966) | `orespawn:crystal_tree_log` (MB:473) | 1 | 10 | 10 |
| 21 | `TigersEye` (ore block) | `orespawn:tigers_eye_ore` | 1 | 10 | 5 |
| 22-26 | `MyCrystalWood{Sword,Axe,Shovel,Pickaxe,Hoe}` | `orespawn:crystal_wood_*` | 1 | 1 | 10 each |
| 27-31 | `MyCrystalPink{Sword,Axe,Shovel,Pickaxe,Hoe}` | `orespawn:crystal_pink_*` | 1 | 1 | 10 each |
| 32-36 | `MyTigersEye{Sword,Axe,Shovel,Pickaxe,Hoe}` | `orespawn:tigers_eye_*` | 1 | 1 | 5 each |
| 37-41 | `MyCrystalStone{Sword,Axe,Shovel,Pickaxe,Hoe}` | `orespawn:crystal_stone_*` | 1 | 1 | 10 each |
| 42 | `MyTigersEyeIngot` | `orespawn:tigers_eye_ingot` | 1 | 5 | 5 |
| 43 | `MyCrystalPinkIngot` | `orespawn:crystal_pink_ingot` | 1 | 5 | 10 |
| 44 | `MyCrystalApple` | `orespawn:crystal_apple` | 1 | 5 | 10 |
| 45 | `MyPeacockFeather` | `orespawn:peacock_feather` | 1 | 5 | 10 |
| 46 | `MyPeacock` | `orespawn:cooked_peacock` | 1 | 10 | 20 |
| 47 | `MyRawPeacock` | `orespawn:raw_peacock` | 1 | 10 | 20 |
| 48 | `MyRice` | `orespawn:rice` | 1 | 10 | 20 |
| 49 | `MyQuinoa` | `orespawn:quinoa` | 1 | 10 | 20 |
| 50-53 | `CrystalPink{Helmet,Body,Legs,Boots}` | `orespawn:pink_{helmet,chestplate,leggings,boots}` | 1 | 1 | 10 each |
| 54-57 | `TigersEye{Helmet,Body,Legs,Boots}` | `orespawn:tigerseye_{helmet,chestplate,leggings,boots}` | 1 | 1 | 5 each |
| 58-61 | `PeacockFeather{Helmet,Body,Legs,Boots}` | `orespawn:peacock_{helmet,chestplate,leggings,boots}` | 1 | 1 | 10 each |
| 62-74 | Spawn eggs: `Rotator, Vortex, Peacock, DungeonBeast, Fairy, Rat, Flounder, Whale, Irukandji, Skate, Urchin, Ghost, GhostSkelly` | `orespawn:*_spawn_egg` (13 entries) | 1 | 5 | 10 each |
| 75 | `MySkateBow` | `orespawn:skate_bow` | 1 | 1 | 2 |
| 76 | `MyIrukandjiArrow` | `orespawn:irukandji_arrow` | 5 | 10 | 2 |
| 77 | `MyIrukandji` | `orespawn:dead_irukandji` | 2 | 8 | 5 |
| 78 | `MyUltimateBow` | `orespawn:ultimate_bow` | 1 | 1 | 2 |
| 79 | `MyUltimateSword` | `orespawn:ultimate_sword` | 1 | 1 | 2 |
| 80 | `Items.field_151042_j` (iron ingot) | `minecraft:iron_ingot` | 1 | 4 | 10 |
| 81 | `Blocks.field_150364_r` (oak log item) | `minecraft:oak_log` | 1 | 4 | 10 |
| 82 | `Items.field_151153_ao` (golden apple) | `minecraft:golden_apple` | 1 | 5 | 2 |

---

## 7. Block palette — modern mapping

| 1.7.10 (cite) | Meaning | Port block (cite) | Used by |
|---|---|---|---|
| `OreSpawnMain.MyDT` (OSM:1946, "duplicatortreelog") | Duplicator Log | `ModBlocks.DUPLICATOR_LOG` (MB:471) | DuplicatorTree trunk |
| `OreSpawnMain.MyAppleLeaves` (OSM:1943, "leaves_apple") | Apple Leaves | `ModBlocks.APPLE_LEAVES` (MB:448) | DuplicatorTree canopy |
| `Blocks.field_150364_r` meta 0 | oak log | `minecraft:oak_log` | ExperienceTree trunk/branches |
| `OreSpawnMain.MyExperienceLeaves` (OSM:1947, "leaves_experience") | Experience Leaves | `ModBlocks.EXPERIENCE_LEAVES` (MB:450) | ExperienceTree canopy |
| `OreSpawnMain.MyCrystalTreeLog` (OSM:1966) | Crystal Tree Log | `ModBlocks.CRYSTAL_TREE_LOG` (MB:473) | FairyTree + FairyCastleTree wood |
| `OreSpawnMain.MyCrystalLeaves3` (OSM:1968) | Crystal Leaves 3 | `ModBlocks.CRYSTAL_LEAVES_3` (MB:465) | FairyTree canopy; castle skirt upper layer |
| `OreSpawnMain.MyCrystalLeaves2` (OSM:1967) | Crystal Leaves 2 | `ModBlocks.CRYSTAL_LEAVES_2` (MB:463) | castle skirt lower layer |
| `OreSpawnMain.CrystalTorch` (OSM:1938) | Crystal Torch | `ModBlocks.CRYSTAL_TORCH` (MB:136) | castle platform corners |
| `Blocks.field_150474_ac` | mob spawner | `minecraft:spawner` | FairyTree base, castle addSomething |
| `Blocks.field_150486_ae` | chest | `minecraft:chest` | FairyTree base, castle addSomething |
| `OreSpawnMain.CrystalGrass` (OSM:1866) | Crystal Grass | `ModBlocks.CRYSTAL_GRASS` (MB:89) | worldgen ground check only (OSW:1972, :1983) |
| grass/dirt/farmland (vanilla) | soil gates | vanilla | DuplicatorTree + ExperienceTree gates |

## 8. Mob mapping

| Spawner string | 1.7.10 class (registration) | Port EntityType |
|---|---|---|
| `"Fairy"` (T:483, T:605) | `Fairy.class` — `EntityRegistry.registerGlobalEntityID(Fairy.class, "Fairy", FairyID)` OSM:4157, `registerModEntity` OSM:4161 | `ModEntities.FAIRY` (`ModEntities.java:528`, `EntityType<Fairy>`) — used by port CS:212/CS:387 |

No other entities. ExperienceTree and DuplicatorTree place no spawners/chests/entities.

---

## 9. Worldgen call context — `addFairyTree` (OSW:1962-1996), Crystal dimension

- Dispatch: `OreSpawnWorld.generate` for `DimensionID5` (OSW:187): `if (!addFairyTree(...)) { termites; big structures if recently_placed==0 }` — a **true** return from `addFairyTree` suppresses crystal termites and the big-structure block for that chunk; maze chests/spawners (OSW:197) and rocks (OSW:198-200) run regardless.
- Fixed probe column: `posX = chunkX+8, posZ = chunkZ+8` (OSW:1963-1964; args are block coords `chunkX*16`).
- Gate: `random.nextInt(5) != 0 → return false` (OSW:1965) — 1/5 of chunks proceed.
- Ground scan (WORLD READS): `posY` from 128 down to 41 (OSW:1968); require `isAirBlock(posX,posY,posZ)` and block below == `CrystalGrass` (OSW:1972). At the first candidate:
  - 17×17 clearance: every `(posX+i, posY, posZ+j)`, `i,j ∈ [-8,8]` must be air, else **return false** (OSW:1973-1979).
  - 5×5 footing: every `(posX+i, posY-1, posZ+j)`, `i,j ∈ [-2,2]` must be `CrystalGrass`, else **return false** (OSW:1980-1986).
  - Variant roll `nextInt(5) != 1` (4/5) → `FairyTree(world, posX, posY-1, posZ)` (OSW:1987-1988); else (1/5) → `FairyCastleTree(world, posX, posY, posZ)` (OSW:1989-1991).
  - `recently_placed = 50` (OSW:1992) then `break`.
- **Return value quirk**: after passing the 1/5 gate the method returns `true` (OSW:1995) even when the descending scan never found a candidate (loop falls through without placing) — only the explicit 17×17 / 5×5 failures return false. So 1/5 of crystal chunks skip termites/structures whether or not a tree actually generated.

DSB trigger context (both fairy variants): any dimension, player-driven, at the placed block's position; ground
is whatever the block sat on (DSB requires a solid block below to stay placed, DSB:25-27); the spawner item is
`OreSpawnMain.RandomDungeon` (OSM:1941), block `MyDungeonSpawnerBlock` (OSM:1942, registered OSM:2185).

---

## 10. World-block READS mid-build (chunk-stitching hazard list)

Per `phase_d_reports\structure_conversion_pattern.md` §1 step 3, the port's chunk-stitching build path cannot READ
world blocks. Every read in these generators, explicitly:

| Generator | Read | Cite | Purpose |
|---|---|---|---|
| DuplicatorTree | soil probes y-1/y-2/y-3 | T:125, :127, :129 | anchor gate (y-2/y-3 dead) |
| DuplicatorTree | trunk cells realy+1..+3 | T:140, :145, :150 | state machine "what's built so far" |
| DuplicatorTree | cap + 3×3 ring cells | T:155, :162 | ditto |
| DuplicatorTree | 5×5 source block **and metadata** | T:171-172 | duplication source |
| DuplicatorTree | 5×5 destination air check | T:176 | duplication target |
| ExperienceTree | soil at (x,y,z) | T:298 | gate |
| ExperienceTree | `make_leaves` 7×7×3 air checks per branch log | T:188 | leaves fill air only |
| FairyTree | `make_crystal_leaves` 5×5×2 air checks per log | T:496 | ditto |
| FairyTree | TileEntity fetches (spawner, chest) | T:481, :486 | configure spawner / fill chest |
| FairyCastleTree | `make_crystal_castle_leaves` 3×3×2 air checks per rim cell | T:508 | ditto |
| FairyCastleTree | TileEntity fetches in `addSomething` | T:603, :610 | ditto |
| addFairyTree (trigger) | column scan + 17×17 air + 5×5 grass | OSW:1968-1986 | site selection |
| addVeggies (trigger) | air column scan + grass-below check | OSW:1891-1892 | MyDT seed-log placement |

Consequences: the fairy pair and ExperienceTree are read-tolerant ONLY if built during the decoration phase
(`WorldGenLevel` with neighbor chunks available) or at play time on a `ServerLevel` — both true for all their
actual trigger sites. None of them can run inside the noise/`ChunkAccess` build pass. DuplicatorTree cannot be a
worldgen feature at all — it is a play-time ticking-block state machine by construction.

---

## 11. PORT-STATE (what exists today)

1. **DuplicatorTree — PORTED, faithful.** `pBDL` (`block/BlockDuplicatorLog.java:34-117`) implements the full
   T:121-182 sequence on `randomTick`: soil gate (pBDL:61-64, documenting the dead y-2/y-3 branch), 1-log-per-tick
   trunk (pBDL:68-74), apple-leaf cap + ring (pBDL:76-95), 20×20 duplication copying the whole `BlockState`
   (pBDL:100-115). Config gate `OreSpawnConfig.DUPLICATOR_TREE_ENABLE` (pBDL:49; `OreSpawnConfig.java:126/:276`,
   default true = orig `DuplicatorTreeEnable` default 1, OSM:1482). Registered `ModBlocks.DUPLICATOR_LOG` (MB:471).
   Adaptation: cap/ring leaves get `PERSISTENT=true` (pBDL:77-78) — no orig equivalent (1.7.10 leaf decay is
   distance-checked differently); acceptable.
   **MISSING-IN-PORT (trigger c/worldgen seed):** the port's `VeggiePatchFeature.place` deliberately rolls
   `random.nextInt(50)` for the `what==5` case but **places nothing**
   (`world/feature/VeggiePatchFeature.java:68-73`, comment cites WGEN-044). Now that `pBDL` is faithful, the
   original OSW:1915-1916 behavior (place one `DUPLICATOR_LOG` on the 1/50 roll, config-gated) is a two-line fix
   there.
2. **ExperienceTree — NOT PORTED (WGEN-045 open, AUDIT_FINDINGS.md:4703).** `pBEP`
   (`block/BlockExperiencePlant.java`) has the correct trigger plumbing — random tick 1/10 (pBEP:59-65 = BEP:46),
   self-removal, then `generateExperienceTree` — but pBEP:83-110 is a **self-declared placeholder** (comment
   pBEP:79-82: "The geometry below is a placeholder; the faithful Trees.ExperienceTree port lands with WGEN-045"):
   single-column trunk of height 6-9 with a disk canopy, vs the original 2×2 trunk to ~y+28 with 8 two-pronged
   droop branches. It does use the right materials (oak log + `EXPERIENCE_LEAVES`, pBEP:85-86).
   `ItemExperienceTreeSeed` (`item/ItemExperienceTreeSeed.java:26-55`) is a faithful D4 port of IETS:25-41
   (`ModItems.EXPERIENCE_TREE_SEED`, MI:610; `ModBlocks.EXPERIENCE_PLANT`, MB:499).
   Oddity: `ModBlocks.EXPERIENCE_SAPLING` (MB:501) + its BlockItem (MI:206) is a plain `Block` with **no original
   counterpart and no grower/no usages** beyond registration and the creative tab — decorative orphan.
3. **FairyTree / FairyCastleTree — PORTED (natural worldgen path).** `CS` builds both:
   `tryPlaceFairyTree` (CS:142-176) mirrors OSW:1962-1996 (1/5 gate, y128→41 scan, 17×17 air, 5×5 crystal grass,
   4/5-vs-1/5 variant split, posY-1/posY asymmetry); `buildFairyTree` (CS:182-216) = T:452-489 including the Fairy
   spawner (CS:212, `ModEntities.FAIRY`) and loot chest (CS:215, `CRYSTAL_CHEST_LOOT` → `chests/crystal_chest.json`);
   `growCrystalBranch` (CS:251-304) = T:520-597; `makeCrystalLeaves` (CS:218-230), `makeCrystalCastleLeaves`
   (CS:232-245); `buildFairyCastleTree` (CS:310-360) + `buildCastlePlatform` (CS:362-382) + `addSomething`
   (CS:384-392) = T:617-785/599-615. Runs from `OreSpawnChunkGenerator.applyBiomeDecoration`
   (`world/OreSpawnChunkGenerator.java:254-259`, CRYSTAL style only) — decoration phase, so its world READS are
   legal; spawner config passes `null` Level to avoid worldgen deadlock (CS:871-876); `safeSetBlock` clamps to
   build height (CS:883-888).
   Known divergences (present-state, flag for D6 review):
   - **LessLag dropped**: orig shrinks every crystal-branch segment (T:529-534 etc.) and `nc` 6→5→4 (T:618-624);
     port hard-codes LessLag=0 lengths (CS:258/269/277/287/296) and `nc = 6` (CS:313) even though
     `OreSpawnConfig.LESS_LAG` exists (`OreSpawnConfig.java:115/:265`).
   - **addFairyTree return quirk not modeled**: orig returns `true` after the 1/5 gate even with no placement
     (OSW:1995) — suppressing termites/structures that chunk; port returns `false` when the scan finds no site
     (CS:175), letting termites/structures proceed.
   - Port `addSomething` chest comment cites T:605-613; spawner odds/positions match (CS:385-391).
4. **FairyTree/FairyCastleTree via DungeonSpawnerBlock — MISSING-IN-PORT (types 0/1).** `RDSBE` reproduces the
   400-tick countdown, self+above clearing, and `nextInt(50)` roll (RDSBE:40-42, :79-86 = DSB:39/:50-52), but
   `buildForType` (RDSBE:104-136) only maps types 2, 21, 22, 23, 38, 47; **types 0 (FairyTree) and 1
   (FairyCastleTree) currently fall into the generic-dungeon fallback** (RDSBE:133-134). The builders exist in CS
   but are `private static` and typed on `WorldGenLevel` (a `ServerLevel` satisfies it) — exposing them is the
   whole fix.
5. **Mechanism exemplars for new tree work:**
   - `world/feature/MagicAppleTreeFeature.java` — `Feature<NoneFeatureConfiguration>` doing a faithful legacy
     build inside `place()` on `WorldGenLevel` (heightmap anchor, soil gate at :62-65); registered/placed via
     biome-modifier JSON with rarity filters (see WGEN-047 resolution: rarity values derived from the orig rolls).
     Same pattern: `SkyTreeFeature.java`, `WindTreeFeature.java`, registered in `world/feature/ModFeatures.java`.
   - `world/CrystalTreeGenerator.java` — chunk-local generator run during the **noise/build phase on
     `ChunkAccess`** (CrystalTreeGenerator:20-48): all reads/writes clamped to the current chunk
     (`isInChunk`/`safeSet`) because cross-chunk access is impossible there. This is the "cannot read" regime;
     none of the four D6 trees may be moved into it.
   - `world/CrystalStructures.java` — decoration-phase direct builder (reads allowed), documented in CS:31-48.

---

## 12. Recommendation per tree (basis for the tree-pattern addendum)

| Tree | Recommended port shape | Reasoning (grounded in trigger sites) |
|---|---|---|
| DuplicatorTree | **Keep as ticking-block logic on `BlockDuplicatorLog.randomTick`** (already done, pBDL). No Feature, no TreeGrower. | The generator is not a tree-build; it is a per-tick state machine whose input is the current world state (§2). Its only original trigger is the log's own tick (BDL:32-39). Remaining work: restore the OSW:1915-1916 worldgen seeding of a lone `DUPLICATOR_LOG` in `VeggiePatchFeature` (1/50 on `what==5`, `DUPLICATOR_TREE_ENABLE` gate). Do NOT re-run the whole build during worldgen — orig worldgen places only the first log; the tree then grows live, tick by tick. |
| ExperienceTree | **Direct build method invoked from the use-site** (`BlockExperiencePlant.randomTick` on `ServerLevel`), i.e. replace pBEP:83-110 with a faithful §3 transcription. A standalone static `ExperienceTreeBuilder` class (parallel to CS's private builders) is fine; a registered `Feature` is unnecessary; a vanilla `TreeGrower` cannot express the 2×2 trunk + custom droop branches. | Its ONLY trigger is the plant's random tick (BEP:50); there is no natural worldgen call site (§1), so registering a ConfiguredFeature buys nothing. Play-time `ServerLevel` build makes the §3 air-checks legal. Its ±28 reach is irrelevant at play time (neighbors loaded). |
| FairyTree | **Keep the CS decoration-phase builder for worldgen** (done); additionally expose `buildFairyTree` (or a public wrapper taking `ServerLevel`) and wire it to **DSB type 0** in `RDSBE.buildForType`. | Two triggers: crystal-dim decoration (reads OK in `applyBiomeDecoration`) and the play-time DSB roll (reads OK on `ServerLevel`). One shared builder over `WorldGenLevel` covers both, exactly like the orig shares `OreSpawnMain.OreSpawnTrees` between OSW and DSB. Restore the LessLag segment shrink while touching it (T:529-534 …). |
| FairyCastleTree | Same as FairyTree: **shared CS builder + DSB type 1 wiring**. | Same two trigger sites (DSB:56-58, OSW:1989-1991). Also restore `nc` LessLag shrink (T:618-624). |

---

## 13. Surprises / MISSING-IN-PORT flags

1. **DuplicatorTree dead code**: the y-2/y-3 soil re-anchoring assigns `realy` but the branch always returns
   (T:131, :137) — only soil directly below the log ever grows. Port documents and matches this (pBDL:22-24).
2. **DuplicatorTree copies metadata**: the duplication step reads `func_72805_g` and replays block+meta (T:172,
   :178) — the only metadata-aware operation in the file. Port's whole-`BlockState` copy subsumes it.
3. **DuplicatorTree duplicates *anything*** in the 5×5 at trunk-base level except air and its own logs — including
   ores, chests-as-blocks, etc. (T:167-181). Known-by-design duplication exploit; faithful port keeps it.
4. **ExperienceTree trunk wood is vanilla oak log** (T:305 etc.), not a custom log — only the leaves are custom.
5. **ExperienceTree has zero natural worldgen** — sapling-tick only (§1). Any port that adds a worldgen feature for
   it would be inventing behavior.
6. **FairyTree/FairyCastleTree have no in-generator ground checks** — DSB can conjure them floating/inside terrain;
   worldgen pre-validates instead (OSW:1973-1986).
7. **DSB fires on random ticks too**: `func_149675_a(true)` (DSB:22) means the 400-tick schedule (DSB:39) is an
   upper bound, not exact — a random tick can trigger the roll early. Port `RDSBE` models only the fixed 400-tick
   countdown (RDSBE:40) — minor divergence, pre-existing.
8. **addFairyTree "returns true without placing"** (OSW:1995): 1/5 of crystal chunks suppress termites/structures
   even when the site scan fails. Port CS:175 returns false there — divergence (port is arguably saner; flag only).
9. **LessLag dropped in the port's crystal builders**: orig shrinks all 5 crystal-branch segments (T:529-589) and
   castle tier count (T:618-624); port hard-codes the LessLag=0 values (CS:258-296, CS:313) despite having
   `LESS_LAG` in config.
10. **DSB types 0/1 fall back to a generic dungeon in the port** (RDSBE:133-134) — the fairy tree/castle builders
    exist (CS) but aren't reachable from the Random Dungeon Spawner. MISSING-IN-PORT.
11. **VeggiePatchFeature stub**: rolls `nextInt(50)` for random-stream fidelity but never places the duplicator
    log (VeggiePatchFeature.java:68-73) — MISSING-IN-PORT now trivially fixable since pBDL is faithful. Note the
    orig seeding also runs in the **overworld** (River/Swampland biomes via generateSurface → addVeggies,
    OSW:279/1887), not just the custom dims.
12. **`ModBlocks.EXPERIENCE_SAPLING` (MB:501) is an orphan**: no original counterpart ("experiencesapling" in
    1.7.10 IS the Experience Plant, OSM:1950), no grower, no placement — registration + creative tab only.
13. **Castle content density**: 36 `addSomething` rolls per castle (nc=6) → expected ~12 Fairy spawners and ~12
    crystal chests per FairyCastleTree — it is a loot piñata, not scenery (§5.4).
14. **`ScragglyTreeWithBranches` (T:418-450) is dead code** and `SmallTree` (T:335-383) is IslandToo-only —
    confirmed against the WGEN-046 VERIFIED-CORRECT resolution; not part of this slice's port surface.
