# BasiliskMaze — Extraction Spec (1.7.10 → NeoForge 1.21.1)

Source of truth: `reference_1_7_10_source/sources/danger/orespawn/BasiliskMaze.java` (460 lines).
All coordinates below are relative to the **build origin** `(X0, Y0, Z0)` = the three int args of
`buildBasiliskMaze(World, int x, int y, int z)` (BasiliskMaze.java:30).

Citation convention: `BM:NN` = BasiliskMaze.java line NN, `OSW:NN` = OreSpawnWorld.java, `OSM:NN` = OreSpawnMain.java,
`DSB:NN` = DungeonSpawnerBlock.java, `WP2:NN` = WorldProviderOreSpawn2.java. Port citations name the port file + line.

---

## 1. Entry points

| Caller | Signature used | Coords passed | Notes |
|---|---|---|---|
| `OreSpawnWorld.addBasiliskMaze` (OSW:2594) | `OreSpawnMain.BMaze.buildBasiliskMaze(world, lowestX, lowestY - 2, lowestZ)` | ground-scan result, **Y offset −2** | worldgen path (see §10) |
| `DungeonSpawnerBlock` type 23 (DSB:122-124) | `OreSpawnMain.BMaze.buildBasiliskMaze(world, clickedX, clickedY, clickedZ)` | player-placed block pos, **no −2 offset** | same public method; `type = world.rand.nextInt(50)` (DSB:52), fires on a scheduled tick 400 ticks after placement (DSB:39) |

`OreSpawnMain.BMaze` is a single shared instance: declared OSM:529, constructed OSM:5782.

All block writes go through `OreSpawnMain.setBlockFast(world, x, y, z, block, meta=0, flags=2)`
(OSM:5833) — a direct chunk write with flag 2 (send-to-client, **no neighbor updates**). Modern equivalent:
`level.setBlock(pos, state, Block.UPDATE_CLIENTS)`.

---

## 2. Fields / constants (BM:24-28)

| Field | Value | Cite |
|---|---|---|
| `WTOP` | 1 (top/north wall bit) | BM:24 |
| `WRGT` | 2 (right/east wall bit) | BM:25 |
| `WBOT` | 4 (bottom/south wall bit) | BM:26 |
| `WLFT` | 8 (left/west wall bit) | BM:27 |
| `chestContentsList` | 31-entry `WeightedRandomChestContent[]` — full transcription in §8 | BM:28 |

(The named constants are declared but the algorithm uses the literals 1/2/4/8 plus border bits 16/32/64/128 directly.)

---

## 3. Top-level build sequence — `buildBasiliskMaze(world, X0, Y0, Z0)` (BM:30-37)

```
depth D = 20 + world.rand.nextInt(10)          // D ∈ [20,29]           (BM:31)
clearArea (X0+3, Y0-D-4, Z0-20)                                          (BM:32)
makeMaze  (X0+3, Y0-D-3, Z0-20, xw=10, zw=10, csz=3, b=0)                (BM:33)
openMaze  (X0+3, Y0-D-3, Z0-20, xw=10, zw=10, csz=3)                     (BM:34)
buildCastle(X0+3, Y0-D-4, Z0-20)                                         (BM:35)
makeEntrance(X0, Y0, Z0, D)                                              (BM:36)
```

Sub-origins used below:
- `C = B = (X0+3, Y0-D-4, Z0-20)` — clearArea / buildCastle origin.
- `M = (X0+3, Y0-D-3, Z0-20)` — maze origin (one block above castle floor level).

---

## 4. `clearArea(world, Cx, Cy, Cz)` (BM:254-276)

1. Main cavity (BM:258-268): for `i = 0..59`; height `hi = 5` if `i < 30`, else `hi = 7` (BM:259-262);
   for `j = 0..hi-1`, `k = 0..29`: set **air** at `(Cx+i, Cy+j, Cz+k)`.
   → maze half (west 30×30) cleared 5 high (`Cy..Cy+4`); castle half (east 30×30) cleared 7 high (`Cy..Cy+6`).
2. Antechamber strip (BM:269-275): for `i = 0..4`, `j = 0..5`, `k = 0..29`: air at `(Cx-i, Cy+j, Cz+k)`
   → clears `Cx-4..Cx`, 6 high, full 30 Z depth.

---

## 5. `makeMaze(world, Mx, My, Mz, xw=10, zw=10, csz=3, b=0)` (BM:39-110) — the algorithm, reproducibly

**Grid:** `gridw = 10`, `gridh = 10`, `cellsize = 3` (clamped to ≥3, BM:45-47). Grid x → world X, grid y → world Z.
Maze occupies world `X = Mx..Mx+29`, `Z = Mz..Mz+29`, walls at `Y = My..My+2` (3 high).

**Cell encoding** (`int[10][10] cells`, all initialized to `full = 15` = all 4 walls, BM:48-54):
- bit 1 = top wall (−Z side), bit 2 = right (+X), bit 4 = bottom (+Z), bit 8 = left (−X).
- Border "no neighbor" flags OR'd in (BM:55-74): left column (`x=0`) gets 128, right column (`x=9`) gets 32,
  top row (`y=0`) gets 16, bottom row (`y=9`) gets 64. Bits 16/32/64/128 block expansion beyond the border
  for the top/right/bottom/left directions respectively (checked in BM:152/156/160/164 and BM:175-190).

**RNG — CRITICAL:** the maze topology uses `Math.random()`, **not** the world's seeded Random:
`rnd(n) = (int)(Math.random() * n + 1.0)` → uniform in `[1, n]` (BM:232-234).
`rndElement(v)` picks index `rnd(v.size()) - 1` and **removes** the element (BM:236-241).
Consequence: the corridor layout is NOT reproducible from the world seed in the original. Only `depth`,
lava/teleport-trap positions, chest count, loot rolls, and mob yaw use `world.rand`.

**Randomized Prim's algorithm** (BM:75-92), exact order of operations:
1. `outlist` = all 100 cells as `Point(x, y)`, added x-major (outer loop x, inner y) (BM:78-82).
2. `current = rndElement(outlist)` (removed from outlist); add to `inlist` (BM:83-84).
3. `moveNbrs(current)` (BM:85, impl 173-191): for each direction whose border bit is clear
   (order: top 0x10, right 0x20, bottom 0x40, left 0x80), move that neighbor from `outlist` to `frontlist`
   (only if still present in outlist — `movePoint`, BM:193-199).
4. While `frontlist` non-empty (BM:86-92):
   a. `current = rndElement(frontlist)` (removed); add to `inlist`.
   b. `moveNbrs(current)` — moves its unvisited neighbors from outlist to frontlist.
   c. `dir = findInNbr(current)` (BM:147-171): start direction `d = rnd(4) - 1` ∈ [0,3], then scan up to
      4 directions cyclically `d = (d+1) % 4`; return the first direction (mapped 0→top=1, 1→right=2,
      2→bottom=4, 3→left=8) whose border bit is clear **and** whose neighbor is already in `inlist`; 0 if none.
   d. `removeWall(current, dir)` (BM:201-230): XOR `dir` bit off the current cell, and XOR the reciprocal bit
      off the neighbor (1↔4 with `y±1`, 2↔8 with `x±1`).

**Wall rasterization** (BM:94-109): for every cell `(x, y)` with a remaining wall bit, draw via `drawSide`:
- bit 1 (top): line `(x*3, y*3) → ((x+1)*3, y*3)`  (BM:97-99)
- bit 2 (right): line `((x+1)*3-1, y*3) → ((x+1)*3-1, (y+1)*3)`  (BM:100-102)
- bit 4 (bottom): line `(x*3, (y+1)*3-1) → ((x+1)*3, (y+1)*3-1)`  (BM:103-105)
- bit 8 (left): line `(x*3, y*3) → (x*3, (y+1)*3)`  (BM:106-108)

`drawSide` (BM:112-145): block = **obsidian** (`b == 0`; bedrock if `b != 0` — never used by this structure,
BM:114-117); endpoints normalized so from ≤ to; loop endpoints are **inclusive** but any local coordinate
`≥ 30` (`cellsize*grid`) is skipped (BM:131,139) — clamping everything to the 0..29 maze area. Each wall
column is 3 blocks tall: `(i+Mx, My, j+Mz)`, `My+1`, `My+2` (BM:132-134, 140-142).
Resulting corridors are 2 blocks wide (cellsize 3 minus 1-thick shared wall), 3 high.

---

## 6. `openMaze(world, Mx, My, Mz, 10, 10, 3)` — entrance/exit carving (BM:278-297)

- **West entrance** (BM:281-288): scan `i = 0..29` ascending; at the first `i` where the block just inside the
  west wall `(Mx+1, My, Mz+i)` is air, carve a 3-high air column through the wall at `(Mx, My..My+2, Mz+i)`; break.
  This opens the maze into the antechamber corridor (§7).
- **East exit** (BM:289-296): scan `i = 29..0` descending; at the first `i` where `(Mx+28, My, Mz+i)` is air,
  carve 3-high air at `(Mx+29, My..My+2, Mz+i)`; break. This opens into the castle (boss) chamber.

---

## 7. `buildCastle(world, Bx, By, Bz)` (BM:299-411) — `B = (X0+3, Y0-D-4, Z0-20)`

| # | What | Where (inclusive) | Block | Cite |
|---|---|---|---|---|
| 1 | Main floor 60×30 | `(Bx+0..59, By, Bz+0..29)` | obsidian | BM:303-307 |
| 2 | 80 lava traps in **maze-half** floor | `(Bx+1+rand(28), By, Bz+1+rand(28))` ×80, `rand(n)=world.rand.nextInt(n)` | lava (still, `field_150353_l`) | BM:308-310 |
| 3 | 20 teleport traps in **castle-half** floor | `(Bx+31+rand(28), By, Bz+1+rand(28))` ×20 | `OreSpawnMain.MyRTPBlock` → port `ModBlocks.BLOCK_TELEPORT` | BM:311-313 |
| 4 | Maze ceiling | `(Bx+0..29, By+4, Bz+0..29)` | bedrock | BM:314-318 |
| 5 | Castle ceiling (taller room) | `(Bx+30..59, By+6, Bz+0..29)` | bedrock | BM:319-323 |
| 6 | East wall, triple-thick | `(Bx+59, By+1..5, Bz+0..29)` obsidian; `(Bx+60)` and `(Bx+61)` bedrock | obsidian + bedrock×2 | BM:324-330 |
| 7 | Castle north wall | `(Bx+30..59, By+1..5, Bz)` obsidian; `Bz-1`, `Bz-2` bedrock | obsidian + bedrock×2 | BM:331-337 |
| 8 | Castle south wall | `(Bx+30..59, By+1..5, Bz+29)` obsidian; `Bz+30`, `Bz+31` bedrock | obsidian + bedrock×2 | BM:338-344 |
| 9 | Divider lip above maze/castle boundary | `(Bx+30, By+5, Bz+0..29)` | obsidian | BM:345-347 |
| 10 | Antechamber floor | `(Bx-4..Bx-1, By, Bz+0..29)` | sandstone | BM:348-352 |
| 11 | Antechamber ceiling | `(Bx-4..Bx-1, By+5, Bz+0..29)` | obsidian | BM:353-357 |
| 12 | Antechamber west wall | `(Bx-5, By+1..4, Bz+0..29)` | iron ore | BM:358-362 |
| 13 | Antechamber north wall | `(Bx-4..Bx, By+1..4, Bz-1)` | iron ore | BM:363-367 |
| 14 | Antechamber south wall | `(Bx-4..Bx, By+1..4, Bz+30)` | iron ore | BM:368-372 |
| 15 | 3 pillars | `(Bx-4, By+1..4, Bz)`, `(…, Bz+15)`, `(…, Bz+29)` | sandstone | BM:373-381 |
| 16 | 3 wall torches | `(Bx-3, By+3, Bz)`, `(…, Bz+15)`, `(…, Bz+29)` | `OreSpawnMain.ExtremeTorch` → port `ModBlocks.EXTREME_TORCH` | BM:382-384 |
| 17 | 3 redstone torches on divider | `(Bx+30, By+4, Bz+2)`, `(…, Bz+15)`, `(…, Bz+27)` | redstone torch (`field_150429_aA`) | BM:385-387 |
| 18 | Chests + torches | count `n = 2 + world.rand.nextInt(3)` ∈ **[2,4]** (BM:389); for `k = 0..n-1`: torch at `(Bx+58, By+4, Bz+2+2k)`, chest at `(Bx+58, By+1, Bz+2+2k)` | torch, chest | BM:390-393 |
| 19 | Chest fill | `WeightedRandomChestContent.func_76293_a(world.rand, chestContentsList, chest, 5 + world.rand.nextInt(6))` → **5–10 weighted stacks per chest**, each placed in a *random slot* (collisions overwrite) | — | BM:395 |
| 20 | 3 boss mobs | `spawnCreature(world, "Basilisk", Bx+45.0/46.0/47.0, By+1.01, Bz+15.0)`, each cast to `Basilisk` and `func_110163_bv()` (= `setPersistenceRequired()`) | entity, **not a spawner block** | BM:397-410 |

`spawnCreature` (BM:243-252): `EntityList.func_75620_a(name)` (createEntityByName) → `func_70012_b(x, y, z, world.rand.nextFloat()*360, 0)` (setLocationAndAngles, random yaw) → `func_72838_d` (spawnEntityInWorld) → `func_70642_aH()` (playLivingSound).

**There are NO mob-spawner (TileEntityMobSpawner) blocks in this structure** — only the 3 direct persistent Basilisk spawns.

---

## 8. Chest loot table — full transcription (BM:28)

Constructor semantics: `WeightedRandomChestContent(item, meta, minStack, maxStack, weight)`. All meta = 0.
Fill formula per chest: `count = 5 + world.rand.nextInt(6)` weighted pulls (5–10 stacks), each pull rolls a
stack size uniform in `[min, max]` and drops it in a random chest slot (BM:395).
Total weight = **495**.

| # | 1.7.10 item | Modern / port mapping (cite) | min | max | weight |
|---|---|---|---|---|---|
| 1 | `Items.field_151079_bi` | `minecraft:ender_pearl` | 3 | 6 | 15 |
| 2 | `Items.field_151045_i` | `minecraft:diamond` | 15 | 25 | 20 |
| 3 | `Items.field_151072_bj` | `minecraft:blaze_rod` | 4 | 12 | 15 |
| 4 | `OreSpawnMain.CageEmpty` (OSM:5409) | port `ModItems.CAGE_EMPTY` "cage_empty" (ModItems.java:778) | 3 | 10 | 20 |
| 5 | `OreSpawnMain.CagedGirlfriend` (OSM:5432) | **MISSING-IN-PORT** as a dedicated item — nearest: `ModItems.CAGED_MOB` "caged_mob" (ModItems.java:782) + `ModDataComponents.CAGED_ENTITY` set to the Girlfriend entity (ModDataComponents.java:23) | 2 | 4 | 15 |
| 6 | `Items.field_151042_j` | `minecraft:iron_ingot` | 2 | 20 | 20 |
| 7 | `Items.field_151043_k` | `minecraft:gold_ingot` | 4 | 16 | 20 |
| 8 | `OreSpawnMain.MyIngotUranium` (OSM:1605) | `ModItems.INGOT_URANIUM` (ModItems.java:114) | 2 | 8 | 20 |
| 9 | `OreSpawnMain.MyIngotTitanium` (OSM:1606) | `ModItems.INGOT_TITANIUM` (ModItems.java:117) | 2 | 6 | 20 |
| 10 | `OreSpawnMain.MySunFish` (OSM:1710) | `ModItems.SUN_FISH` (ModItems.java:381) | 2 | 8 | 20 |
| 11 | `OreSpawnMain.MyFireFish` (OSM:1709) | `ModItems.FIRE_FISH` (ModItems.java:378) | 3 | 8 | 20 |
| 12 | `OreSpawnMain.MyLavaEel` (OSM:1711) | `ModItems.LAVA_EEL` (ModItems.java:384) | 5 | 24 | 20 |
| 13 | `OreSpawnMain.MyCornDog` ("corndog_cooked", OSM:1847) | `ModItems.CORN_DOG` (ModItems.java:412) | 6 | 12 | 20 |
| 14 | `Items.field_151046_w` | `minecraft:diamond_pickaxe` | 1 | 1 | 15 |
| 15 | `Items.field_151048_u` | `minecraft:diamond_sword` | 1 | 1 | 15 |
| 16 | `OreSpawnMain.MyUltimatePickaxe` (OSM:1637) | `ModItems.ULTIMATE_PICKAXE` (ModItems.java:168) | 1 | 1 | 15 |
| 17 | `OreSpawnMain.MyUltimateSword` (OSM:1636) | `ModItems.ULTIMATE_SWORD` (ModItems.java:165) | 1 | 1 | 15 |
| 18 | `OreSpawnMain.MyUltimateFishingRod` (OSM:1707) | `ModItems.ULTIMATE_FISHING_ROD` (ModItems.java:320) | 1 | 1 | 15 |
| 19 | `OreSpawnMain.MyUltimateBow` (OSM:1705) | `ModItems.ULTIMATE_BOW` (ModItems.java:316) | 1 | 1 | 15 |
| 20 | `Items.field_151163_ad` | `minecraft:diamond_chestplate` | 1 | 1 | 15 |
| 21 | `Items.field_151161_ac` | `minecraft:diamond_helmet` | 1 | 1 | 15 |
| 22 | `Items.field_151173_ae` | `minecraft:diamond_leggings` | 1 | 1 | 15 |
| 23 | `Items.field_151175_af` | `minecraft:diamond_boots` | 1 | 1 | 15 |
| 24 | `OreSpawnMain.UltimateBody` (OSM:1785) | `ModItems.ULTIMATE_CHESTPLATE` (ModItems.java:575) | 1 | 1 | 15 |
| 25 | `OreSpawnMain.UltimateLegs` (OSM:1786) | `ModItems.ULTIMATE_LEGGINGS` (ModItems.java:578) | 1 | 1 | 15 |
| 26 | `OreSpawnMain.UltimateHelmet` (OSM:1784) | `ModItems.ULTIMATE_HELMET` (ModItems.java:572) | 1 | 1 | 15 |
| 27 | `OreSpawnMain.UltimateBoots` (OSM:1787) | `ModItems.ULTIMATE_BOOTS_ARMOR` (ModItems.java:581) | 1 | 1 | 15 |
| 28 | `OreSpawnMain.MyRuby` (OSM:1859) | `ModItems.RUBY` (ModItems.java:111) | 1 | 1 | **5** |
| 29 | `OreSpawnMain.MyThunderStaff` (OSM:1747) | `ModItems.THUNDER_STAFF` (ModItems.java:324) | 1 | 1 | **5** |
| 30 | `OreSpawnMain.MagicApple` (OSM:1925) | `ModItems.MAGIC_APPLE` (ModItems.java:506) | 1 | 1 | 15 |
| 31 | `Items.field_151153_ao` | `minecraft:golden_apple` | 2 | 4 | 15 |

---

## 9. `makeEntrance(world, X0, Y0, Z0, D)` (BM:413-458)

**A. Surface step-pyramid marker** (BM:417-424): `width = 8`; for `j = 8` down to `0`, at level
`Y0 + 8 - j` place a square **sandstone** ring: for `i = 0..(2j+3)`:
`(X0+i-j, ·, Z0-j)`, `(X0+i-j, ·, Z0+j+3)`, `(X0-j, ·, Z0+i-j)`, `(X0+j+3, ·, Z0+i-j)`.
→ 9 concentric rings shrinking from 20×20 at `Y0` (`X0-8..X0+11`, `Z0-8..Z0+11`) to 4×4 at `Y0+8`.

**B. Vertical shaft with spiral parkour column** (BM:425-457): `k = 0`; for `j = 8` down while `j > -D`
(so last layer `j = -D+1`), each layer:
1. 4×4 **bedrock** perimeter ring: `(X0+0..3, Y0+j, Z0)` & `(…, Z0+3)`; `(X0, Y0+j, Z0+0..3)` & `(X0+3, …)` (BM:427-432).
2. Interior 2×2 **air**: `(X0+1..2, Y0+j, Z0+1..2)` (BM:433-437).
3. One **obsidian** step per layer, spiraling with `k` (BM:438-454): `k=0 → (X0+1, Z0+1)`; `k=1 → (X0+2, Z0+1)`;
   `k=2 → (X0+2, Z0+2)`; `k=3 → (X0+1, Z0+2)`; then `k = (k+1) mod 4` (BM:455-456).

The shaft descends from `Y0+8` to `Y0-D+1`. Since the antechamber's obsidian ceiling sits at
`By+5 = Y0-D+1` (BM:353-357), the final layer's 2×2 air carve punches through that ceiling
(makeEntrance runs **after** buildCastle, BM:35-36), dropping the player ~5 blocks onto the sandstone
antechamber floor at `Y0-D-4`. The player then takes the west maze opening (§6), traverses the maze
east into the Basilisk chamber, loots the chests along the east wall.

---

## 10. Worldgen call context (OreSpawnWorld / dimension gating)

- `recently_placed` is a **static** cooldown shared by all OreSpawn structures, initialized to 50
  (OSW:30) and decremented once per `generate` call (per chunk) (OSW:37-39).
- The `addBasiliskMaze` call at OSW:82 is inside the `world.provider.dimensionId == OreSpawnMain.DimensionID2`
  branch (OSW:55). `DimensionID2 = BaseDimensionID + 1` (OSM:1596), registered with provider
  `WorldProviderOreSpawn2` (OSM:5381-5382), whose dimension name is `"Dimension-Extreme"` (WP2:19-21) —
  i.e. the **Mining dimension**, mapped in the port to `ModDimensionKeys.MINING` = `orespawn:mining`
  (src/main/java/danger/orespawn/ModDimensionKeys.java:27).
- **Gating rolls** (OSW:79-101): `if (recently_placed == 0 && random.nextInt(95) == 1)` then
  `i = random.nextInt(7)`; `i == 0` → `addBasiliskMaze` (OSW:81-83). Competing picks: 1 Kyuubi dungeon,
  2 beehive, 3 shadow dungeon, 4 AlienWTF, 5 EnderKnight, 6 LeonNest (OSW:84-101). If the outer gate fails,
  a GenericDungeon attempt runs instead (OSW:102-104).
  Effective per-chunk odds ≈ 1/95 × 1/7 ≈ **1 in 665**, further throttled by the 50-chunk cooldown.

### `addBasiliskMaze(world, random, chunkX, chunkZ)` (OSW:2573-2597) — ground-finding

- Initialize `lowestY = 128`, `lowestX = chunkX`, `lowestZ = chunkZ`, `found = false` (OSW:2574-2577).
- Scan a 6×6 sample of the chunk: `i, j ∈ {0, 3, 6, 9, 12, 15}` → `posX = chunkX+i`, `posZ = chunkZ+j`
  (OSW:2578-2581); for each column, `posY` from 128 down to 31 (OSW:2582), find the first (topmost) Y where
  `block(posX, posY+1, posZ) == air` **and** `block(posX, posY, posZ) != air` (surface test, OSW:2583).
- Keep the column whose surface is **lowest** across all 36 samples (`if posY >= lowestY continue`, OSW:2584-2588).
- If `found && lowestY > 40`: `buildBasiliskMaze(world, lowestX, lowestY - 2, lowestZ)` — the **−2 Y offset**
  sinks the pyramid's ground ring 2 blocks into the terrain — then `recently_placed = 50` (OSW:2593-2596).
- The `lowestY > 40` floor guarantees the deepest possible dungeon floor `Y0-D-4 ≥ 41-29-4 = 8` stays above bedrock.

### DungeonSpawnerBlock path (confirmation)

`DungeonSpawnerBlock.func_149674_a` (scheduled tick; server side; self-deletes first, DSB:46-51) rolls
`type = world.rand.nextInt(50)` (DSB:52); `type == 23` calls `OreSpawnMain.BMaze.buildBasiliskMaze(world,
clickedX, clickedY, clickedZ)` (DSB:122-124) — **the identical public method** `buildBasiliskMaze(World, int, int, int)`
(BM:30), just without the worldgen path's −2 offset or ground scan.

---

## 11. Block palette — modern mapping

| 1.7.10 field | Modern block | Used for | Port cite |
|---|---|---|---|
| `Blocks.field_150343_Z` | `minecraft:obsidian` | maze walls, floors, castle inner walls, ceilings, divider, spiral steps | vanilla |
| `Blocks.field_150357_h` | `minecraft:bedrock` | ceilings, outer wall shells, shaft rings | vanilla |
| `Blocks.field_150350_a` | `minecraft:air` | cavity, openings, shaft interior | vanilla |
| `Blocks.field_150353_l` | `minecraft:lava` (source) | 80 floor traps in maze half | vanilla |
| `Blocks.field_150322_A` | `minecraft:sandstone` | pyramid, antechamber floor, pillars | vanilla |
| `Blocks.field_150366_p` | `minecraft:iron_ore` | antechamber walls | vanilla |
| `Blocks.field_150429_aA` | `minecraft:redstone_torch` | 3 on divider lip | vanilla |
| `Blocks.field_150478_aa` | `minecraft:torch` | above each chest | vanilla |
| `Blocks.field_150486_ae` | `minecraft:chest` | 2–4 loot chests | vanilla |
| `OreSpawnMain.MyRTPBlock` ("blockteleport", OSM:1879) | port `ModBlocks.BLOCK_TELEPORT` "block_teleport" | 20 castle-floor teleport traps | ModBlocks.java:74-75 |
| `OreSpawnMain.ExtremeTorch` ("extremetorch", OSM:1927) | port `ModBlocks.EXTREME_TORCH` "extreme_torch" | 3 antechamber torches | ModBlocks.java:136 |

## 12. Mobs

| 1.7.10 | Count / placement | Port mapping |
|---|---|---|
| `"Basilisk"` (EntityList name), persistent via `func_110163_bv()` (BM:398-410) | 3, at `(Bx+45/46/47, By+1.01, Bz+15)` (center of the castle room), random yaw | `ModEntities.BASILISK` (ModEntities.java:49), class `danger.orespawn.entity.Basilisk` (src/main/java/danger/orespawn/entity/Basilisk.java); persistence → `setPersistenceRequired()` |

No spawner blocks are placed by this structure.

---

## 13. Total footprint (relative to build origin `(X0, Y0, Z0)`, `D ∈ [20, 29]`)

| Axis | Min | Max | Extent | Determined by |
|---|---|---|---|---|
| X | `X0 - 8` | `X0 + 64` | **73 blocks** | pyramid base ring `X0-j` at j=8 (BM:421) / east bedrock shell `Bx+61` (BM:328) |
| Z | `Z0 - 22` | `Z0 + 11` | **34 blocks** | castle north shell `Bz-2` (BM:335) / castle south shell `Bz+31` (BM:342); pyramid reaches only `Z0±8..+11` |
| Y | `Y0 - D - 4` (= `Y0-24 .. Y0-33`) | `Y0 + 8` | **`D + 13` = 33–42 blocks** | castle floor `By` (BM:305) / pyramid apex `Y0+8` (BM:419) |

Interior layout at depth (west → east): iron-ore antechamber (X `X0-2..X0+2`) → 30×30 obsidian maze
(X `X0+3..X0+32`) → 30×30 Basilisk chamber (X `X0+33..X0+61`), chests on its east wall at `X0+61` local... 
(absolute chest X = `Bx+58 = X0+61`), all spanning Z `Z0-20..Z0+9`.

---

## 14. Porting notes, surprises, MISSING-IN-PORT flags

1. **Non-seeded RNG**: maze topology uses `Math.random()` (BM:232-234) — layout is non-deterministic w.r.t.
   world seed in the original. Port decision needed: replicate the bug or thread the worldgen `RandomSource`
   through `rnd()` (recommended; preserve the exact call order in §5 for parity if seeded).
2. **`CagedGirlfriend` loot entry has no dedicated port item** (original OSM:5432). Nearest equivalent:
   `CAGED_MOB` + `CAGED_ENTITY` data component pointing at the Girlfriend entity (ModItems.java:782,
   ModDataComponents.java:23). Flagged MISSING-IN-PORT (item identity), not blocking.
3. **Chest fill uses random-slot placement** (`func_76293_a`) — later stacks can overwrite earlier ones, so
   a chest can end up with fewer than the rolled 5–10 stacks. A modern loot-table port with `set_count`
   pools reproduces intent, not this quirk.
4. **Lava traps under the maze, teleport traps under the boss room** — the RTP block randomly teleports;
   both are floor-level surprises, placed with `world.rand` after the floor (order matters if seeding).
5. **No structure/jigsaw framework** — pure imperative block writes with `setBlockFast` flag 2; a 1.21.1 port
   should mirror this as a runtime builder (like the port's existing `CrystalMaze.java` /
   `GenericDungeon.java` in src/main/java/danger/orespawn/world/) rather than NBT templates, because the
   maze is procedurally different every time. **No BasiliskMaze exists in the port yet** (src/main/java/danger/orespawn/world/ has CrystalMaze/GenericDungeon only).
6. `recently_placed` cooldown is global static across all structures and dimensions (OSW:30, 37-39,
   2595) — replicate as shared state per-server, not per-structure.
7. `drawSide`'s inclusive loops + `>= 30` clamp mean cell walls at grid edges overlap-share single wall
   columns; corridor width is 2. Off-by-one fidelity matters for openMaze's air probes at `Mx+1` / `Mx+28`.
8. DungeonSpawnerBlock's live-world invocation works unchanged because the builder both sets blocks and
   spawns entities immediately (no deferred spawner).
