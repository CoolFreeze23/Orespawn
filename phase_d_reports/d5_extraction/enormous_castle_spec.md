# Enormous Castle (King) / Enormous Castle Q (Queen) — Extraction Spec (1.7.10 → NeoForge 1.21.1)

Source of truth: `reference_1_7_10_source/sources/danger/orespawn/GenericDungeon.java`
— `makeEnormousCastle` (GD:191-375) + helpers `buildLevel` (GD:377-478), `addLevelDecorations`
(GD:480-725), `fill_chests` (GD:727-785); and `makeEnormousCastleQ` (GD:6393-6577) + `buildLevelQ`
(GD:6579-6680), `addLevelDecorationsQ` (GD:6682-6927), `fill_chestsQ` (GD:6929-6987).

All coordinates below are **relative to `(cposx, cposy, cposz)`**, the three int args of the make
methods. In worldgen, `cposy` is the Y of the grass block found by the ground scan (OSW:2209-2211),
so the 28×28 base plate is written at grass level.

Citation convention: `GD:NN` = GenericDungeon.java, `OSW:NN` = OreSpawnWorld.java,
`OSM:NN` = OreSpawnMain.java, `DSB:NN` = DungeonSpawnerBlock.java. Port citations:
`LDP:NN` = src/main/java/danger/orespawn/world/structure/LegacyDungeonPiece.java,
`MI:NN` = ModItems.java, `MB:NN` = ModBlocks.java, `ME:NN` = ModEntities.java.

Method bodies were cross-checked against the extraction dumps
`tmp/gd_castle_king.txt` (dump line N = GD line N+184) and `tmp/gd_castle_queen.txt`
(dump line N = GD line N+6389); offsets verified at GD:187/191/292-311/377/480/727 and
GD:6393-6404/6579/6682/6929 against the real file. Loot-list dump `tmp/gd_constants.txt`
(dump line N = GD line N+23) verified at GD:57-61/63.

Block writes: `FastSetBlock` (GD:187-189) → `OreSpawnMain.setBlockFast(world, x, y, z, block, 0, 2)`
(flag 2 = send-to-client, no neighbor updates; modern `level.setBlock(pos, state, Block.UPDATE_CLIENTS)`).
Exceptions: the 4 Extreme Torches use `world.func_147449_b` (setBlock **with** updates, GD:241-244 /
GD:6443-6446); spawners and chests use `world.func_147465_d(..., 0, 2)`.

---

## 1. Entry points

| Caller | Which variant | Coords passed | Cite |
|---|---|---|---|
| `OreSpawnWorld.addD4Castle` (worldgen, Islands dim) | 50/50: `nextInt(2)==1` → King, else Queen | `(posX, posY_grass, posZ)` | OSW:2203-2228, roll OSW:2219-2223 |
| `DungeonSpawnerBlock` type **2** | King (`makeEnormousCastle`) | player-placed block pos | DSB:59-61 |
| `DungeonSpawnerBlock` type **47** | Queen (`makeEnormousCastleQ`) | player-placed block pos | DSB:194-196 |

(NOTE: DSB type 12 is `makePlayPool` (DSB:89-91), **not** the castle — the castle types are 2 and 47.)
DSB mechanics: on placement, a scheduled tick is queued 400 ticks out (DSB:35-39); on that tick the
block deletes itself and the block above (DSB:50-51) then rolls `type = world.rand.nextInt(50)` (DSB:52).

Both make methods bail on the client: `if (world.field_72995_K) return;` (GD:199-201 / GD:6401-6403).

**Port entry points**: `LegacyDungeonPiece.DungeonType.KING_TOWER(40, 4, 95)` / `QUEEN_TOWER(40, 4, 95)`
(LDP:104-105), dispatched `case KING_TOWER -> generateChallengeTower(rng, true)` /
`QUEEN_TOWER -> ... false` (LDP:250-251, method LDP:1783). Registered as jigsaw-less structures
`orespawn:challenge_tower_king` / `_queen` (`data/orespawn/worldgen/structure/challenge_tower_king.json`,
`type: orespawn:legacy_dungeon`, `dungeon_type: KING_TOWER`, biome tag
`#orespawn:has_structure/challenge_tower_king` = `orespawn:island_biome`), placed via two independent
structure_sets (`random_spread`, spacing 44, separation 22, salt 84320 King / 84321 Queen).

---

## 2. Constants / setup (identical in both variants)

| Name | Value | Cite (King / Queen) |
|---|---|---|
| `width` | 28 | GD:195 / GD:6397 |
| `height` | 16 | GD:196 / GD:6398 |
| `platformwidth` | 11 | GD:197 / GD:6399 |
| `level` roll | `1 + world.rand.nextInt(6)`; then `if (level <= 3 && world.rand.nextInt(3) != 1) level += 3;` | GD:202-205 / GD:6404-6407 |

Level distribution: P(1)=P(2)=P(3)=1/18 each; P(4)=P(5)=P(6)=5/18 each. Only level=6
(~27.8%) builds all six floors and produces the reward-6 Royal chests (see §6/§7).

`width/2 = 14` is the room center used throughout.

---

## 3. `makeEnormousCastle` (King) — base castle, step by step (GD:191-375)

| # | What | Where (inclusive, rel. cpos) | Block / content | Cite |
|---|---|---|---|---|
| 1 | Clear envelope | x `-20..31`, y `+1..+25`, z `-4..31` | air | GD:206-212 |
| 2 | Base floor plate | x `0..27`, y `+0`, z `0..27` | **stone** (`field_150348_b`) | GD:213-218 |
| 3 | Base ceiling | x `0..27`, y `+16`, z `0..27` | bedrock | GD:219-224 |
| 4 | N/S walls (z=0, z=27) | x `0..27`, y `+1..+15` | **iron bars** (`field_150411_aY`) | GD:225-232 |
| 5 | E/W walls (x=0, x=27) | z `0..27`, y `+1..+15` | iron bars | GD:233-240 |
| 6 | 4 interior torches | `(1,1,1)`, `(1,1,26)`, `(26,1,1)`, `(26,1,26)` | `OreSpawnMain.ExtremeTorch` via `func_147449_b` (with updates) | GD:241-244 |
| 7 | Foundation skirt | x `-4..31`, z `-4..31` (only where outside `0..27` square), y `+0` | stone | GD:245-249 |
| 8 | Outer fence ring | perimeter `i==-4 \|\| k==-4 \|\| i==31 \|\| k==31`, y `+1` | **nether brick fence** (`field_150386_bk`) | GD:250-252 |
| 9 | 4 exterior corner spawner stacks | `(-3, 1+j, -3)`, `(-3, 1+j, 30)`, `(30, 1+j, -3)`, `(30, 1+j, 30)` for j=0..3 (y `+1..+4`), 16 spawners | mob spawner, mob = **"Terrible Terror"** | GD:254-275 |
| 10 | Central spawner column | `(14, 2, 14)`, `(14, 3, 14)`, `(14, 4, 14)` | mob spawner ×3, **"Emperor Scorpion"** | GD:276-290 |
| 11 | Six stacked floors | see §4 table | `buildLevel(...)` | GD:291-313 |
| 12 | West rooftop platform | x `-20..-10` (i=0..10 → `cposx+i-20`), y `+16`, z `+9..+19` (k=-5..5 → `cposz+k+14`) | **quartz block** (`field_150371_ca`); fence on rim at y `+17` except entry gap at i=0, k∈[-1,1] | GD:314-321 |
| 13 | Connector arm | x `-10..-3`, z `+12..+16` (k=-2..2): ends (i=-3, i=-10) get netherrack posts at k=±2 y `+17..+18` + **fire** on top y `+19`, middle air y `+17`; body quartz at y `+16` with fence at k=±2 y `+17` | quartz / netherrack / fire / fence | GD:322-339 |
| 14 | Descending stair (west) | i starts `-21`, one column per j from 16 down to 0 (i decrements each j → i `-21..-37`), z `+12..+16`: 6-high air clear above each step (y `j+1..j+6`); at j==0 netherrack posts k=±2 (y `+1..+2`) + fire (y `+3`), air middle; else quartz step at y `j`, fence at k=±2 y `j+1` | quartz / netherrack / fire / fence / air | GD:340-361 |
| 15 | Buried "Large Worm" ring (only if level ≥ 6) | 100 tries: `i,k = nextInt(84)`; skip if both ∈ `[21,63]`; place spawner at `(i-42+14, -1, k-42+14)` → x,z ∈ `-28..55` excluding the center band; y = **cposy−1** (below grade) | mob spawner, **"Large Worm"** (~74 expected placed; positions may repeat/overwrite) | GD:362-374 |

Spawner tile entities are fetched and assigned via
`tileentitymobspawner.func_145881_a().func_98272_a(name)` (setEntityName), e.g. GD:256-259.

### Floor stack — `buildLevel` calls (GD:291-313)

`j` starts at `height`=16 and accumulates each floor's height **whether or not the floor is built**
(the `j +=` lines are outside the `if (level >= n)` guards):

| Floor | Guard | Origin (rel.) | width | height | pw | "Outside" mob | stepside | stepoff | holelen | decor | Y span (rel.) | Cite |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 1 | always | `(+1, +16, +1)` | 26 | 10 | 4 | "Cloud Shark" | 1 | −1 | 5 | 1 | 16..26 | GD:291-292 |
| 2 | level≥2 | `(+1, +26, +1)` | 26 | 10 | 4 | "Lurking Terror" | 0 | 0 | 4 | 2 | 26..36 | GD:293-296 |
| 3 | level≥3 | `(+2, +36, +2)` | 24 | 9 | 4 | "Rotator" | 1 | 1 | 4 | 3 | 36..45 | GD:297-300 |
| 4 | level≥4 | `(+2, +45, +2)` | 24 | 9 | 3 | "Bee" | 0 | 0 | 4 | 4 | 45..54 | GD:301-304 |
| 5 | level≥5 | `(+3, +54, +3)` | 22 | 8 | 3 | "Mantis" | 1 | 1 | 4 | 5 | 54..62 | GD:305-308 |
| 6 | level≥6 | `(+3, +62, +3)` | 22 | 16 | 3 | "Mothra" | 0 | 0 | 3 | 6 | 62..78 | GD:309-313 |

---

## 4. `buildLevel(world, ox, oy, oz, width, height, pw, critter, stepside, stepoff, holelen, decor, level)` (GD:377-478)

All coordinates below relative to the **floor origin** `(ox, oy, oz)` from the table above.

| # | What | Where (inclusive) | Block | Cite |
|---|---|---|---|---|
| A | Hollow footprint + gallery margin | x,z `-pw .. width+pw-1`, y `+1..height-1` | air | GD:381-387 |
| B | Floor slab | x,z `0..width-1`, y `+0` | bedrock | GD:388-393 |
| C | Ceiling slab | x,z `0..width-1`, y `+height` | bedrock | GD:394-399 |
| D | N/S walls (z=0, z=width−1) | x `0..width-1`, y `+1..height-1` | bedrock | GD:400-407 |
| E | E/W walls (x=0, x=width−1) | z `0..width-1`, y `+1..height-1`; **corner columns k==0 and k==width−1 use `Blocks.field_150340_R` (gold block)** instead of bedrock | bedrock + gold accents | GD:408-419 |
| F | Gallery skirt + fence | y `+0` **stone** where outside the `0..width-1` square, out to ±pw; fence at the outermost rim (`i or k == -pw or width+pw-1`) at y `+1` | stone / nether brick fence | GD:420-428 |
| G | Exterior diagonal stair | start `i = width/2 - height/2`; for y `+1..height-1`: one **stone** block at `(i, y, k)` where k = **−1** if stepside≠0 else **width**; `i++` each step | stone | GD:429-440 |
| H | Landing hole in THIS floor's slab (skipped when stepoff<0) | k = `-1-stepoff` (stepside==0) or `width+stepoff` (stepside≠0); carve air at `(width/2 + l, +0, k)` for l = 0..holelen−1 | air | GD:441-454 |
| I | 4 corner "Outside"-mob spawner stacks | `(-(pw-1), 1+j, -(pw-1))`, `(-(pw-1), 1+j, width+pw-2)`, `(width+pw-2, 1+j, -(pw-1))`, `(width+pw-2, 1+j, width+pw-2)` for j=0..3 → y `+1..+4`; 16 spawners of `critter` | mob spawner | GD:455-476 |
| J | Interior decoration + chests | — | `addLevelDecorations(ox, oy, oz, width, height, decor, level)` | GD:477 |

Stair/hole chaining (computed from §3 args; castle-relative): F1 stair on z=`+0` (north gallery),
climbs into F2's floor hole at z=`+0`, x `+14..+17`, y `+26`; F2 stair z=`+27` → F3 hole z=`+27`,
x `+14..+17`, y `+36`; F3 stair z=`+1` → F4 hole z=`+1`, y `+45`; F4 stair z=`+26` → F5 hole z=`+26`,
y `+54`; F5 stair z=`+2` → F6 hole z=`+2`, x `+14..+16` (holelen 3), y `+62`. F1 has **no** hole below
it (stepoff=−1) — entry to F1's gallery is from the rooftop platform/arm at y `+16..17`.

**No ladders or climbable blocks are placed anywhere in the tower** (verified: zero ladder references
in GD:191-786 / 6393-6987). The interior rooms are sealed bedrock boxes with 1×1 ceiling/floor holes.

Corner spawner stacks per floor (castle-relative x/z corners): F1/F2: `{-2, 29}`; F3: `{-1, 28}`;
F4: `{0, 27}`; F5/F6: `{1, 26}`.

---

## 5. `addLevelDecorations(world, ox, oy, oz, width, height, decor, difficulty)` (GD:480-725)

`difficulty` = the castle-wide `level` roll. Defaults: `reward = 1`, `critter = "Alosaurus"` (GD:483-484).

### decor == 6 — Nightmare cap (GD:485-541) — floor 6 only

| # | What | Where | Content | Cite |
|---|---|---|---|---|
| 1 | 4 rooftop fire beacons | corners `(0/width-1, +height, 0/width-1)` netherrack, `+height+1` **fire** | netherrack + fire | GD:486-493 |
| 2 | Ceiling center hole | `(width/2, +height, width/2)` | air | GD:494 |
| 3 | 4 rooftop **"Nightmare"** spawners | `(width/2±1, +height+2, width/2)` and `(width/2, +height+2, width/2±1)` | mob spawner ×4 | GD:495-514 |
| 4 | Dirt fill | x,z `1..width-2`, y `+1..+4` | dirt | GD:515-521 |
| 5 | 3 buried **"Large Worm"** spawners | `(width/2, +2..+4, width/2)` | mob spawner ×3 | GD:522-536 |
| 6 | Air shaft | `(1, +0..+9, 1)` (punches through dirt fill and this floor's slab) | air | GD:537-539 |
| 7 | Chests | `fill_chests(ox, oy+4, oz, width, height, 6, reward=1)` — chests sit **on top of the dirt fill** at y `+5` | see §7 | GD:540 |

### decor == 5..1 — central spawner room (each floor's "Inside" mob)

Common geometry (identical code repeated per decor branch; cites given for decor=5, others parallel):

| # | What | Where | Cite (decor=5 / 4 / 3 / 2 / 1) |
|---|---|---|---|
| 1 | 2 stacked spawners of `critter` | `(width/2, +2, width/2)`, `(width/2, +3, width/2)` | GD:551-560 / 584-593 / 621-630 / 662-671 / 702-711 |
| 2 | Bedrock cage shaft | `(width/2±1, +1..+4, width/2)` + `(width/2, +1..+4, width/2±1)` | GD:561-566 / 594-599 / 631-636 / 672-677 / 712-717 |
| 3 | Floor hole (1×1 air in slab, y `+0`) | decor 5: `(width-2, width-2)`; decor 4: `(1, 1)`; decor 3: `(width-2, width-2)`; decor 2: `(1, 1)`; decor 1: **none** | GD:567 / 600 / 637 / 678 / — |
| 4 | Ceiling hole (1×1 air, y `+height`) | decor 5: `(1, 1)`; decor 4: `(width-2, width-2)`; decor 3: `(1, 1)`; decor 2: `(width-2, width-2)`; decor 1: `(1, 1)` | GD:568 / 601 / 638 / 679 / 722 |
| 5 | decor 1 only: 4 RTP teleport-trap blocks | `(width/2±1, +1, width/2±1)` diagonal cells | GD:718-721 (`OreSpawnMain.MyRTPBlock`) |
| 6 | Chests | `fill_chests(ox, oy, oz, width, height, decor, reward)` | GD:570 / 603 / 640 / 681 / 723 |

### King "Inside" mob & reward matrix (GD:485-724)

`critter` / `reward` per (decor, difficulty). Cells only reachable when difficulty ≥ decor
(the floor isn't built otherwise). Cites: decor5 GD:542-550; decor4 GD:571-583; decor3 GD:604-620;
decor2 GD:641-661; decor1 GD:682-701 (`reward = difficulty` at GD:701).

| decor \ difficulty | 1 | 2 | 3 | 4 | 5 | 6 |
|---|---|---|---|---|---|---|
| 6 (floor 6 cap) | — | — | — | — | — | Nightmare cap, reward=1 |
| 5 (floor 5) | — | — | — | — | Alosaurus r1 | T. Rex r2 |
| 4 (floor 4) | — | — | — | Alosaurus r1 | T. Rex r2 | Basilisk r3 |
| 3 (floor 3) | — | — | Alosaurus r1 | T. Rex r2 | Basilisk r3 | Hercules Beetle r4 |
| 2 (floor 2) | — | Alosaurus r1 | T. Rex r2 | Basilisk r3 | Hercules Beetle r4 | Jumpy Bug r5 |
| 1 (floor 1) | Alosaurus r1 | T. Rex r2 | Basilisk r3 | Hercules Beetle r4 | Jumpy Bug r5 | **Hammerhead r6 → Royal loot** |

---

## 6. `fill_chests(world, ox, oy, oz, width, height, decor, reward)` (GD:727-785)

List select (GD:729-742): reward 1→`level1ContentsList`, 2→`level2ContentsList`, 3→`level3ContentsList`,
4→`level4ContentsList`, 5→`level5ContentsList` (reward 6 bypasses lists entirely).

Four chests, each placed with `func_147465_d(..., chest, 0, 2)` then facing metadata via
`func_72921_c(x, y, z, meta, 3)` (1.7.10 chest meta: 2=N, 3=S, 4=W, 5=E — each faces the room center):

| Chest | Position (floor-rel.) | Facing meta | reward==6 contents (slot: item) | else | Cite |
|---|---|---|---|---|---|
| West | `(1, +1, width/2)` | 5 (east) | slot 1: `ThePrinceEgg` | weighted fill | GD:743-752 |
| East | `(width-2, +1, width/2)` | 4 (west) | slot 1: `RoyalHelmet`, slot 2: `RoyalBody` | weighted fill | GD:753-763 |
| North | `(width/2, +1, 1)` | 3 (south) | slot 1: `RoyalLegs`, slot 2: `RoyalBoots` | weighted fill | GD:764-774 |
| South | `(width/2, +1, width-2)` | 2 (north) | slot 1: `MyRoyal` (Royal Guardian Sword, "royalsmall", OSM:1647) | weighted fill | GD:775-784 |

Weighted fill = `WeightedRandomChestContent.func_76293_a(world.rand, list, chest, 5 + world.rand.nextInt(7))`
→ **5–11 weighted stacks per chest**, each in a random slot (collisions overwrite), e.g. GD:750.

reward==6 occurs **only** on floor 1 (decor=1) of a level-6 tower — the tower's prize floor.

---

## 7. `makeEnormousCastleQ` (Queen, GD:6393-6577) — diff vs King

Geometry, loop bounds, constants, floor stack, platform/arm/stair shapes, worm ring, chest positions,
decor-room geometry and the reward cascade are **byte-identical** to the King (verified against
`tmp/gd_castle_queen.txt`; e.g. clear GD:6408-6414 == GD:206-212, floors GD:6493-6515 == GD:291-313,
worm ring GD:6564-6576 == GD:362-374). The complete difference list:

| # | King | Queen | Cites (King → Queen) |
|---|---|---|---|
| 1 | Base floor plate **stone** | **obsidian** (`field_150343_Z`) | GD:216 → GD:6418 |
| 2 | Foundation skirt **stone** | **obsidian** | GD:248 → GD:6450 |
| 3 | Exterior corner stacks "**Terrible Terror**" | "**Lurking Terror**" | GD:258-274 → GD:6461-6476 |
| 4 | Floor "Outside" ladder: Cloud Shark / Lurking Terror / Rotator / Bee / Mantis / Mothra | **Rotator / Bee / Mantis / Mothra / Brutalfly / Vortex** (same stepside/stepoff/holelen/decor args) | GD:292-311 → GD:6494-6513 |
| 5 | Platform/arm/stair block **quartz block** | **`OreSpawnMain.MyBlockAmethystBlock`** | GD:317/335/356 → GD:6519/6537/6558 |
| 6 | `buildLevel` E/W accent columns **gold block** | **`OreSpawnMain.MyBlockRubyBlock`** (in `buildLevelQ`) | GD:412 → GD:6614 |
| 7 | `buildLevel` gallery skirt + diagonal stair **stone** | **obsidian** | GD:423/434/437 → GD:6625/6636/6639 |
| 8 | "Inside" mob ladder base Alosaurus→T. Rex→Basilisk→Hercules→Jumpy Bug→**Hammerhead** | base **T. Rex→Nastysaurus**→Basilisk→Hercules→Jumpy Bug→**CaterKiller** (decor1/diff6, GD:6900-6902); default critter "T. Rex" (GD:6686) | GD:683-701 → GD:6885-6902 (and parallel shifts in decor 2-5: GD:6744-6772 / 6773-6805 / 6806-6842 / 6843-6883) |
| 9 | reward==6 chest items: ThePrinceEgg / Royal armor / MyRoyal | **ThePrincessEgg / Queen armor / MyRoyal** (same sword) | GD:747-780 → GD:6949-6982 (`fill_chestsQ` GD:6929-6987) |
| 10 | All decors call `fill_chests` | decor **1** calls `fill_chestsQ` (GD:6925); decors 2-6 still call the King's `fill_chests` (GD:6742/6771/6804/6841/6882) — behaviorally identical there since reward<6 on those floors | GD:540... → GD:6742... |

Queen decor==6 cap is identical (same "Nightmare" + "Large Worm" spawners, GD:6687-6743).
Queen decor matrix = King matrix with Alosaurus→T. Rex, T. Rex→Nastysaurus, Hammerhead→CaterKiller.

---

## 8. Loot tables — FULL transcriptions

Constructor semantics: `WeightedRandomChestContent(item, meta, minStack, maxStack, weight)`; meta 0
everywhere. Fill per chest: 5-11 weighted pulls, random slots (§6).

### 8.1 `level1ContentsList` (GD:57) — reward 1. Total weight **165**.

| # | 1.7.10 item | Port mapping (cite) | min | max | wt |
|---|---|---|---|---|---|
| 1 | `Items.field_151166_bC` | `minecraft:emerald` | 2 | 8 | 15 |
| 2 | `OreSpawnMain.MinersDream` (OSM:1926) | `ModItems.MINERS_DREAM` (MI:508) | 4 | 8 | 15 |
| 3 | `MyEmeraldPickaxe` (OSM:1653) | `EMERALD_PICKAXE` (MI:204) | 1 | 1 | 15 |
| 4 | `MyEmeraldShovel` (OSM:1654) | `EMERALD_SHOVEL` (MI:206) | 1 | 1 | 15 |
| 5 | `MyEmeraldHoe` (OSM:1655) | `EMERALD_HOE` (MI:208) | 1 | 1 | 15 |
| 6 | `MyEmeraldAxe` (OSM:1656) | `EMERALD_AXE` (MI:210) | 1 | 1 | 15 |
| 7 | `MyEmeraldSword` (OSM:1652) | `EMERALD_SWORD` (MI:202) | 1 | 1 | 15 |
| 8 | `EmeraldBody` (OSM:1797) | `EMERALD_CHESTPLATE` (MI:631) | 1 | 1 | 15 |
| 9 | `EmeraldLegs` (OSM:1796-1799 set) | `EMERALD_LEGGINGS` (MI:634) | 1 | 1 | 15 |
| 10 | `EmeraldHelmet` (OSM:1796) | `EMERALD_HELMET` (MI:628) | 1 | 1 | 15 |
| 11 | `EmeraldBoots` (OSM:1796-1799 set) | `EMERALD_BOOTS_ARMOR` (MI:637) | 1 | 1 | 15 |

### 8.2 `level2ContentsList` (GD:58) — reward 2. Total weight **235**.

| # | 1.7.10 item | Port mapping | min | max | wt |
|---|---|---|---|---|---|
| 1 | `Items.field_151062_by` (bottle o' enchanting) | `minecraft:experience_bottle` | 2 | 8 | 15 |
| 2 | `Items.field_151062_by` (duplicate entry) | `minecraft:experience_bottle` | 2 | 8 | 15 |
| 3 | `CreeperLauncher` (OSM:1726) | `CREEPER_LAUNCHER` (MI:328) | 2 | 10 | 15 |
| 4 | `CrystalPinkHelmet` (OSM:1812) | `PINK_HELMET` (MI:684) | 1 | 1 | 10 |
| 5 | `CrystalPinkBody` (OSM:1812-1815 set) | `PINK_CHESTPLATE` (MI:687) | 1 | 1 | 10 |
| 6 | `CrystalPinkLegs` (set) | `PINK_LEGGINGS` (MI:690) | 1 | 1 | 10 |
| 7 | `CrystalPinkBoots` (set) | `PINK_BOOTS` (MI:693) | 1 | 1 | 10 |
| 8 | `MyFairySword` (OSM:1660) | `FAIRY_SWORD` (MI:306) | 1 | 1 | 15 |
| 9 | `MyEmeraldPickaxe` (OSM:1653) | `EMERALD_PICKAXE` (MI:204) | 1 | 1 | 15 |
| 10 | `MyEmeraldShovel` (OSM:1654) | `EMERALD_SHOVEL` (MI:206) | 1 | 1 | 15 |
| 11 | `MyEmeraldHoe` (OSM:1655) | `EMERALD_HOE` (MI:208) | 1 | 1 | 15 |
| 12 | `MyEmeraldAxe` (OSM:1656) | `EMERALD_AXE` (MI:210) | 1 | 1 | 15 |
| 13 | `MyEmeraldSword` (OSM:1652) | `EMERALD_SWORD` (MI:202) | 1 | 1 | 15 |
| 14 | `ExperienceBody` (OSM:1801) | `EXPERIENCE_CHESTPLATE` (MI:645) | 1 | 1 | 15 |
| 15 | `ExperienceLegs` (set ~OSM:1800-1803) | `EXPERIENCE_LEGGINGS` (MI:648) | 1 | 1 | 15 |
| 16 | `ExperienceHelmet` (set) | `EXPERIENCE_HELMET` (MI:642) | 1 | 1 | 15 |
| 17 | `ExperienceBoots` (set) | `EXPERIENCE_BOOTS` (MI:651) | 1 | 1 | 15 |

### 8.3 `level3ContentsList` (GD:59) — reward 3. Total weight **235**.

| # | 1.7.10 item | Port mapping | min | max | wt |
|---|---|---|---|---|---|
| 1 | `MySquidZooka` (OSM:1759) | `SQUID_ZOOKA` (MI:326) | 1 | 1 | 15 |
| 2 | `MyRatSword` (OSM:1659) | `RAT_SWORD` (MI:304) | 1 | 1 | 15 |
| 3 | `MyAmethyst` (OSM:1861) | `AMETHYST_GEM` (MI:112) | 2 | 8 | 15 |
| 4 | `Items.field_151100_aR` meta 0 (dye — meta 0 = **ink sac**) | `minecraft:ink_sac` | 2 | 8 | 15 |
| 5 | `TigersEyeHelmet` (OSM:1816) | `TIGERSEYE_HELMET` (MI:698) | 1 | 1 | 10 |
| 6 | `TigersEyeBody` (OSM:1816-1819 set) | `TIGERSEYE_CHESTPLATE` (MI:701) | 1 | 1 | 10 |
| 7 | `TigersEyeLegs` (set) | `TIGERSEYE_LEGGINGS` (MI:704) | 1 | 1 | 10 |
| 8 | `TigersEyeBoots` (set) | `TIGERSEYE_BOOTS` (MI:707) | 1 | 1 | 10 |
| 9 | `MyAmethystPickaxe` (OSM:1672) | `AMETHYST_PICKAXE` (MI:192) | 1 | 1 | 15 |
| 10 | `MyAmethystShovel` (OSM:1674) | `AMETHYST_SHOVEL` (MI:194) | 1 | 1 | 15 |
| 11 | `MyAmethystHoe` (OSM:1676) | `AMETHYST_HOE` (MI:196) | 1 | 1 | 15 |
| 12 | `MyAmethystAxe` (OSM:1677) | `AMETHYST_AXE` (MI:198) | 1 | 1 | 15 |
| 13 | `MyAmethystSword` (OSM:1671) | `AMETHYST_SWORD` (MI:190) | 1 | 1 | 15 |
| 14 | `AmethystBody` (OSM:1809) | `AMETHYST_CHESTPLATE` (MI:673) | 1 | 1 | 15 |
| 15 | `AmethystLegs` (set ~OSM:1808-1811) | `AMETHYST_LEGGINGS` (MI:676) | 1 | 1 | 15 |
| 16 | `AmethystHelmet` (set) | `AMETHYST_HELMET` (MI:670) | 1 | 1 | 15 |
| 17 | `AmethystBoots` (set) | `AMETHYST_BOOTS_ARMOR` (MI:679) | 1 | 1 | 15 |

### 8.4 `level4ContentsList` (GD:60) — reward 4. 17 entries, all weight 15. Total **255**.

| # | 1.7.10 item | Port mapping | min | max | wt |
|---|---|---|---|---|---|
| 1 | `MyRuby` (OSM:1859) | `RUBY` (MI:111) | 2 | 8 | 15 |
| 2 | `MagicApple` (OSM:1925) | `MAGIC_APPLE` (MI:506) | 1 | 1 | 15 |
| 3 | `MyRayGun` (OSM:1746) | `RAY_GUN` (MI:322) | 1 | 1 | 15 |
| 4 | `CreeperRepellent` (block, as item) | `ModBlocks.CREEPER_REPELLENT` (MB:146) | 4 | 10 | 15 |
| 5 | `KrakenRepellent` (block, as item) | `ModBlocks.KRAKEN_REPELLENT` (MB:143) | 4 | 10 | 15 |
| 6 | `MyExperienceCatcher` (OSM:1948) | `EXPERIENCE_CATCHER` (MI:514) | 4 | 10 | 15 |
| 7 | `ZooKeeper` (OSM:1725) | `ZOO_KEEPER` (MI:529) | 10 | 16 | 15 |
| 8 | `MyRubyPickaxe` (OSM:1664) | `RUBY_PICKAXE` (MI:180) | 1 | 1 | 15 |
| 9 | `MyRubyShovel` (OSM:1666) | `RUBY_SHOVEL` (MI:182) | 1 | 1 | 15 |
| 10 | `MyRubyHoe` (OSM:1668) | `RUBY_HOE` (MI:184) | 1 | 1 | 15 |
| 11 | `MyRubyAxe` (OSM:1669) | `RUBY_AXE` (MI:186) | 1 | 1 | 15 |
| 12 | `MyRubySword` (OSM:1663) | `RUBY_SWORD` (MI:178) | 1 | 1 | 15 |
| 13 | `MyThunderStaff` (OSM:1747) | `THUNDER_STAFF` (MI:324) | 1 | 1 | 15 |
| 14 | `RubyBody` (OSM:1805) | `RUBY_CHESTPLATE` (MI:659) | 1 | 1 | 15 |
| 15 | `RubyLegs` (set ~OSM:1804-1807) | `RUBY_LEGGINGS` (MI:662) | 1 | 1 | 15 |
| 16 | `RubyHelmet` (set) | `RUBY_HELMET` (MI:656) | 1 | 1 | 15 |
| 17 | `RubyBoots` (set) | `RUBY_BOOTS_ARMOR` (MI:665) | 1 | 1 | 15 |

### 8.5 `level5ContentsList` (GD:61) — reward 5. 87 entries. Total weight **1285**
(2 swords ×15 + 82 eggs ×15 + EasterBunnyEgg ×5 + 2 kits ×10).

All entries in declaration order. Unless noted, min=1, max=4, weight=15.

| # | 1.7.10 item | Port mapping (MI line) | min | max | wt |
|---|---|---|---|---|---|
| 1 | `MyNightmareSword` (OSM:1644) | `NIGHTMARE_SWORD` (MI:262) | 1 | 1 | 15 |
| 2 | `MyPoisonSword` (OSM:1658) | `POISON_SWORD` (MI:302) | 1 | 1 | 15 |
| 3 | `WitherSkeletonEgg` | `minecraft:wither_skeleton_spawn_egg` | 1 | 4 | 15 |
| 4 | `EnderDragonEgg` | `minecraft:ender_dragon_spawn_egg` | 1 | 4 | 15 |
| 5 | `SnowGolemEgg` | `minecraft:snow_golem_spawn_egg` | 1 | 4 | 15 |
| 6 | `IronGolemEgg` | `minecraft:iron_golem_spawn_egg` | 1 | 4 | 15 |
| 7 | `WitherBossEgg` | `minecraft:wither_spawn_egg` | 1 | 4 | 15 |
| 8 | `RedCowEgg` | `RED_COW_SPAWN_EGG` (MI:1017) | 1 | 4 | 15 |
| 9 | `GoldCowEgg` | `GOLD_COW_SPAWN_EGG` (MI:1023) | 1 | 4 | 15 |
| 10 | `EnchantedCowEgg` | `ENCHANTED_APPLE_COW_SPAWN_EGG` (MI:1050) | 1 | 4 | 15 |
| 11 | `MOTHRAEgg` | `MOTHRA_SPAWN_EGG` (MI:1009) | 1 | 4 | 15 |
| 12 | `AloEgg` | `ALOSAURUS_SPAWN_EGG` (MI:797) | 1 | 4 | 15 |
| 13 | `CryoEgg` | `CRYOLOPHOSAURUS_SPAWN_EGG` (MI:813) | 1 | 4 | 15 |
| 14 | `CamaEgg` | `CAMARASAURUS_SPAWN_EGG` (MI:959) | 1 | 4 | 15 |
| 15 | `VeloEgg` | `VELOCITY_RAPTOR_SPAWN_EGG` (MI:991) | 1 | 4 | 15 |
| 16 | `HydroEgg` | `HYDROLISC_SPAWN_EGG` (MI:969) | 1 | 4 | 15 |
| 17 | `BasilEgg` | `BASILISK_SPAWN_EGG` (MI:803) | 1 | 4 | 15 |
| 18 | `DragonflyEgg` | `DRAGONFLY_SPAWN_EGG` (MI:945) | 1 | 4 | 15 |
| 19 | `EmperorScorpionEgg` | `EMPEROR_SCORPION_SPAWN_EGG` (MI:871) | 1 | 4 | 15 |
| 20 | `ScorpionEgg` | `SCORPION_SPAWN_EGG` (MI:889) | 1 | 4 | 15 |
| 21 | `CaveFisherEgg` | `CAVE_FISHER_SPAWN_EGG` (MI:805) | 1 | 4 | 15 |
| 22 | `SpyroEgg` | `SPYRO_SPAWN_EGG` (MI:979) | 1 | 4 | 15 |
| 23 | `BaryonyxEgg` | `BARYONYX_SPAWN_EGG` (MI:907) | 1 | 4 | 15 |
| 24 | `CockateilEgg` | `COCKATEIL_SPAWN_EGG` (MI:923) | 1 | 4 | 15 |
| 25 | `GammaMetroidEgg` | `GAMMA_METROID_SPAWN_EGG` (MI:965) | 1 | 4 | 15 |
| 26 | `KyuubiEgg` | `KYUUBI_SPAWN_EGG` (MI:875) | 1 | 4 | 15 |
| 27 | `AlienEgg` | `ALIEN_SPAWN_EGG` (MI:793) | 1 | 4 | 15 |
| 28 | `AttackSquidEgg` | `ATTACK_SQUID_SPAWN_EGG` (MI:799) | 1 | 4 | 15 |
| 29 | `WaterDragonEgg` | `WATER_DRAGON_SPAWN_EGG` (MI:993) | 1 | 4 | 15 |
| 30 | `CephadromeEgg` | `CEPHADROME_SPAWN_EGG` (MI:1015) | 1 | 4 | 15 |
| 31 | `KrakenEgg` | `KRAKEN_SPAWN_EGG` (MI:859) | 1 | 4 | 15 |
| 32 | `LizardEgg` | `LIZARD_SPAWN_EGG` (MI:973) | 1 | 4 | 15 |
| 33 | `DragonEgg` | `DRAGON_SPAWN_EGG` (MI:961) | 1 | 4 | 15 |
| 34 | `BeeEgg` | `BEE_SPAWN_EGG` (MI:865) | 1 | 4 | 15 |
| 35 | `TrooperBugEgg` | `TROOPER_BUG_SPAWN_EGG` (MI:897) | 1 | 4 | 15 |
| 36 | `SpitBugEgg` | `SPIT_BUG_SPAWN_EGG` (MI:891) | 1 | 4 | 15 |
| 37 | `StinkBugEgg` | `STINK_BUG_SPAWN_EGG` (MI:951) | 1 | 4 | 15 |
| 38 | `OstrichEgg` | `OSTRICH_SPAWN_EGG` (MI:975) | 1 | 4 | 15 |
| 39 | `GazelleEgg` | `GAZELLE_SPAWN_EGG` (MI:931) | 1 | 4 | 15 |
| 40 | `ChipmunkEgg` | `CHIPMUNK_SPAWN_EGG` (MI:913) | 1 | 4 | 15 |
| 41 | `CreepingHorrorEgg` | `CREEPING_HORROR_SPAWN_EGG` (MI:811) | 1 | 4 | 15 |
| 42 | `TerribleTerrorEgg` | `TERRIBLE_TERROR_SPAWN_EGG` (MI:893) | 1 | 4 | 15 |
| 43 | `CliffRacerEgg` | `CLIFF_RACER_SPAWN_EGG` (MI:941) | 1 | 4 | 15 |
| 44 | `TriffidEgg` | `TRIFFID_SPAWN_EGG` (MI:895) | 1 | 4 | 15 |
| 45 | `PitchBlackEgg` | `PITCH_BLACK_SPAWN_EGG` (MI:831) | 1 | 4 | 15 |
| 46 | `LurkingTerrorEgg` | `LURKING_TERROR_SPAWN_EGG` (MI:879) | 1 | 4 | 15 |
| 47 | `SmallWormEgg` | `WORM_SMALL_SPAWN_EGG` (MI:901) | 1 | 4 | 15 |
| 48 | `MediumWormEgg` | `WORM_MEDIUM_SPAWN_EGG` (MI:903) | 1 | 4 | 15 |
| 49 | `LargeWormEgg` | `WORM_LARGE_SPAWN_EGG` (MI:905) | 1 | 4 | 15 |
| 50 | `TRexEgg` | `TREX_SPAWN_EGG` (MI:853) | 1 | 4 | 15 |
| 51 | `GodzillaEgg` | `GODZILLA_SPAWN_EGG` (MI:857) | 1 | 4 | 15 |
| 52 | `MantisEgg` | `MANTIS_SPAWN_EGG` (MI:881) | 1 | 4 | 15 |
| 53 | `HerculesEgg` | `HERCULES_BEETLE_SPAWN_EGG` (MI:873) | 1 | 4 | 15 |
| 54 | `VortexEgg` | `VORTEX_SPAWN_EGG` (MI:899) | 1 | 4 | 15 |
| 55 | `RatEgg` | `RAT_SPAWN_EGG` (MI:885) | 1 | 4 | 15 |
| 56 | `DungeonBeastEgg` | `DUNGEON_BEAST_SPAWN_EGG` (MI:815) | 1 | 4 | 15 |
| 57 | `FairyEgg` | `FAIRY_SPAWN_EGG` (MI:1001) | 1 | 4 | 15 |
| 58 | `WhaleEgg` | `WHALE_SPAWN_EGG` (MI:937) | 1 | 4 | 15 |
| 59 | `SkateEgg` | `SKATE_SPAWN_EGG` (MI:851) | 1 | 4 | 15 |
| 60 | `IrukandjiEgg` | `IRUKANDJI_SPAWN_EGG` (MI:827) | 1 | 4 | 15 |
| 61 | `Robot1Egg` | `ROBOT_1_SPAWN_EGG` (MI:837) | 1 | 4 | 15 |
| 62 | `Robot2Egg` | `ROBOT_2_SPAWN_EGG` (MI:839) | 1 | 4 | 15 |
| 63 | `Robot3Egg` | `ROBOT_3_SPAWN_EGG` (MI:841) | 1 | 4 | 15 |
| 64 | `Robot4Egg` | `ROBOT_4_SPAWN_EGG` (MI:843) | 1 | 4 | 15 |
| 65 | `Robot5Egg` | `ROBOT_5_SPAWN_EGG` (MI:845) | 1 | 4 | 15 |
| 66 | `CriminalEgg` | `BAND_P_SPAWN_EGG` (`band_p_spawn_egg`, MI:801) — 1.7.10 "Criminal" IS the port's BandP per the Phase C7 audit correction WGEN-017 ("Criminal=band_p"); resolved 2026-08-08, D5 | 1 | 4 | 15 |
| 67 | `CoinEgg` | `COIN_SPAWN_EGG` (MI:917) | 1 | 4 | 15 |
| 68 | `BoyfriendEgg` | `BOYFRIEND_SPAWN_EGG` (MI:957) | 1 | 4 | 15 |
| 69 | `EasterBunnyEgg` | `EASTER_BUNNY_SPAWN_EGG` (MI:925) | 1 | 4 | **5** |
| 70 | `MolenoidEgg` | `MOLENOID_SPAWN_EGG` (MI:883) | 1 | 4 | 15 |
| 71 | `SeaMonsterEgg` | `SEA_MONSTER_SPAWN_EGG` (MI:847) | 1 | 4 | 15 |
| 72 | `SeaViperEgg` | `SEA_VIPER_SPAWN_EGG` (MI:849) | 1 | 4 | 15 |
| 73 | `CaterKillerEgg` | `CATER_KILLER_SPAWN_EGG` (MI:869) | 1 | 4 | 15 |
| 74 | `LeonEgg` | `LEON_SPAWN_EGG` (MI:971) | 1 | 4 | 15 |
| 75 | `HammerheadEgg` | `HAMMERHEAD_SPAWN_EGG` (MI:825) | 1 | 4 | 15 |
| 76 | `RubberDuckyEgg` | `RUBBER_DUCKY_SPAWN_EGG` (MI:977) | 1 | 4 | 15 |
| 77 | `NastysaurusEgg` | `NASTYSAURUS_SPAWN_EGG` (MI:829) | 1 | 4 | 15 |
| 78 | `PointysaurusEgg` | `POINTYSAURUS_SPAWN_EGG` (MI:835) | 1 | 4 | 15 |
| 79 | `BrutalflyEgg` | `BRUTALFLY_SPAWN_EGG` (MI:867) | 1 | 4 | 15 |
| 80 | `CricketEgg` | `CRICKET_SPAWN_EGG` (MI:943) | 1 | 4 | 15 |
| 81 | `FrogEgg` | `FROG_SPAWN_EGG` (MI:929) | 1 | 4 | 15 |
| 82 | `AntRobotKit` | `ANT_ROBOT_KIT` (MI:525) | 1 | 1 | **10** |
| 83 | `SpiderRobotKit` | `SPIDER_ROBOT_KIT` (MI:521) | 1 | 1 | **10** |
| 84 | `JefferyEgg` | `JEFFERY_SPAWN_EGG` (MI:823) | 1 | 4 | 15 |
| 85 | `SpiderDriverEgg` | `SPIDER_DRIVER_SPAWN_EGG` (MI:1019) | 1 | 4 | 15 |
| 86 | `CrabEgg` | `CRAB_SPAWN_EGG` (MI:809) | 1 | 4 | 15 |
| 87 | `CassowaryEgg` | `CASSOWARY_SPAWN_EGG` (MI:911) | 1 | 4 | 15 |

### 8.6 reward==6 fixed items (no weighted list)

| 1.7.10 item (cite) | Port mapping |
|---|---|
| `ThePrinceEgg` "eggtheprince" (OSM:5616, `ItemSpawnEgg`) | `PRINCE_EGG` "prince_egg" (MI:786, plain item) — note port also has `THE_PRINCE_SPAWN_EGG` (MI:983) |
| `ThePrincessEgg` "eggtheprincess" (OSM:5630) | `PRINCESS_EGG` "princess_egg" (MI:790, plain item); `THE_PRINCESS_SPAWN_EGG` also exists (MI:987) |
| `RoyalHelmet` / `RoyalBody` / `RoyalLegs` / `RoyalBoots` (OSM:1828-1831) | `ROYAL_HELMET`/`ROYAL_CHESTPLATE`/`ROYAL_LEGGINGS`/`ROYAL_BOOTS` (MI:726/729/732/735) |
| `QueenHelmet` / `QueenBody` / `QueenLegs` / `QueenBoots` (OSM:1836-1839) | `QUEEN_HELMET`/`QUEEN_CHESTPLATE`/`QUEEN_LEGGINGS`/`QUEEN_BOOTS` (MI:754/757/760/763) |
| `MyRoyal` — Bertha-class sword, tier `toolROYAL`, "royalsmall" (OSM:1647) | `ROYAL_GUARDIAN_SWORD` (MI:276) |

---

## 9. Block palette — modern mapping

| 1.7.10 field | Modern block | Used for | Cite |
|---|---|---|---|
| `Blocks.field_150350_a` | `minecraft:air` | clears, holes, shafts | GD:209 etc. |
| `Blocks.field_150348_b` | `minecraft:stone` | King base floor, skirts, floor stairs | GD:216/248/423/434 |
| `Blocks.field_150343_Z` | `minecraft:obsidian` | Queen base floor, skirts, floor stairs | GD:6418/6450/6625/6636 |
| `Blocks.field_150357_h` | `minecraft:bedrock` | base ceiling; floor slabs/ceilings/walls; decor cage shafts | GD:222/391/397/403/410 etc. |
| `Blocks.field_150411_aY` | `minecraft:iron_bars` | base-room cage walls | GD:228-238 |
| `Blocks.field_150386_bk` | `minecraft:nether_brick_fence` | all fence rims | GD:251/426/319 etc. |
| `Blocks.field_150474_ac` | `minecraft:spawner` | all mob spawners | GD:256 etc. |
| `Blocks.field_150486_ae` | `minecraft:chest` | 4 chests per floor | GD:743 etc. |
| `Blocks.field_150371_ca` | `minecraft:quartz_block` | King platform/arm/stair | GD:317/335/356 |
| `Blocks.field_150424_aL` | `minecraft:netherrack` | fire-post bases (arm/stair ends, decor-6 corners) | GD:330-332/486-492 |
| `Blocks.field_150480_ab` | `minecraft:fire` | fire atop posts and decor-6 corners | GD:332/487 etc. |
| `Blocks.field_150340_R` | `minecraft:gold_block` | King floor E/W corner accent columns | GD:412 |
| `Blocks.field_150346_d` | `minecraft:dirt` | decor-6 fill | GD:518 |
| `OreSpawnMain.ExtremeTorch` "extremetorch" (OSM:1927) | `ModBlocks.EXTREME_TORCH` "extreme_torch" (MB:136) | 4 base-room torches | GD:241-244 |
| `OreSpawnMain.MyRTPBlock` "blockteleport" (OSM:1879) | `ModBlocks.BLOCK_TELEPORT` (MB:74) | 4 teleport traps on prize floor (decor 1) | GD:718-721 |
| `OreSpawnMain.MyBlockAmethystBlock` | `ModBlocks.BLOCK_AMETHYST` (MB:36) | Queen platform/arm/stair | GD:6519/6537/6558 |
| `OreSpawnMain.MyBlockRubyBlock` | `ModBlocks.BLOCK_RUBY` (MB:33) | Queen floor accent columns | GD:6614 |

---

## 10. Mob mapping table

All spawns are **spawner blocks** (no direct entity spawns in either variant). Spawner mob names
are 1.7.10 `EntityList` names set via `func_98272_a`.

| Spawner name | 1.7.10 class (OSM registration) | Port EntityType (ME line) | Used by |
|---|---|---|---|
| "Terrible Terror" | `TerribleTerror` (OSM:3999) | `ENTITY_TERRIBLE_TERROR` (ME:266) | King exterior corners |
| "Lurking Terror" | `LurkingTerror` (OSM:4031) | `ENTITY_LURKING_TERROR` (ME:238) | Queen exterior corners; King floor 2 |
| "Emperor Scorpion" | `EmperorScorpion` (OSM:3783) | `ENTITY_EMPEROR_SCORPION` (ME:221) | central column, both |
| "Cloud Shark" | `CloudShark` (OSM:4095) | `CLOUD_SHARK` (ME:60) | King floor 1 |
| "Rotator" | `Rotator` (OSM:4173) | `ENTITY_ROTATOR` (ME:254) | King F3 / Queen F1 |
| "Bee" | `Bee` (OSM:3639) | `ENTITY_BEE` (ME:204) | King F4 / Queen F2 |
| "Mantis" | `Mantis` (OSM:4245) | `ENTITY_MANTIS` (ME:242) | King F5 / Queen F3 |
| "Mothra" | `Mothra` (OSM:3647) | `MOTHRA` (ME:562) | King F6 / Queen F4 |
| "Brutalfly" | `Brutalfly` (OSM:4425) | `ENTITY_BRUTALFLY` (ME:209) | Queen F5 |
| "Vortex" | `Vortex` (OSM:4181) | `ENTITY_VORTEX` (ME:278) | Queen F6 |
| "Large Worm" | `WormLarge` (OSM:4079) | `ENTITY_WORM_LARGE` (ME:294) | buried ring + decor-6 column, both |
| "Nightmare" | **`PitchBlack`** (OSM:4023 — "Nightmare" is PitchBlack's EntityList name) | `PITCH_BLACK` (ME:113) | decor-6 rooftop ×4, both |
| "Alosaurus" | `Alosaurus` (OSM:3727) | `ALOSAURUS` (ME:33) | King inside ladder |
| "T. Rex" | `TRex` (OSM:4261) | `TREX` (ME:153) | both inside ladders |
| "Basilisk" | `Basilisk` (OSM:3743) | `BASILISK` (ME:49) | both inside ladders |
| "Hercules Beetle" | `HerculesBeetle` (OSM:4253) | `ENTITY_HERCULES_BEETLE` (ME:225) | both inside ladders |
| "Jumpy Bug" | **`TrooperBug`** (OSM:3943 — "Jumpy Bug" is TrooperBug's name) | `ENTITY_TROOPER_BUG` (ME:274) — **but port uses `ENTITY_SPIT_BUG` (see §13.3)** | both inside ladders |
| "Hammerhead" | `Hammerhead` (OSM:4385) | `HAMMERHEAD` (ME:101) | King prize floor |
| "Nastysaurus" | `Nastysaurus` (OSM:4433) | `NASTYSAURUS` (ME:109) | Queen inside ladder |
| "CaterKiller" | `CaterKiller` (OSM:4361) | `ENTITY_CATER_KILLER` (ME:215) | Queen prize floor |

Spawner count for a level-6 tower: 16 (base corners) + 3 (scorpion) + 6×16 (floor corners) +
5×2 (inside columns, decor 1-5) + 4 (Nightmare) + 3 (decor-6 worms) + ~74 (buried ring) ≈ **206**.

---

## 11. Footprint extents (relative to `(cposx, cposy, cposz)`)

| Axis | Min | Max | Extent | Determined by |
|---|---|---|---|---|
| X | `-37` | `+31` | **69** | stair bottom column i=−37 at j=0 (GD:340-361) / skirt+fence `width+3`=31 (GD:245-252) |
| Z | `-4` | `+31` | **36** | skirt/fence ring (GD:245-252); stair corridor only z `+12..+16` |
| Y | `-1` (level≥6 worm ring, GD:362-374) else `+0` | `+80` | **82** | base plate y+0 (GD:214) / decor-6 Nightmare spawners at floor-6 top: 62+16+2 = `+80` (GD:495-514); fire at `+79` |

Level<6 towers top out lower (e.g. level 4: floor-4 ceiling at `+54`). The buried Large Worm ring
(level 6 only) extends x,z to `-28..+55` at y `-1`. The air-clear envelope (GD:206-212) covers only
y `+1..+25`; each floor clears its own interior (GD:381-387).

---

## 12. Worldgen call context (Islands dimension, D4)

### 12.1 Dimension

`DimensionID4 = BaseDimensionID + 3` (OSM:1598), provider `WorldProviderOreSpawn4` (OSM:5385-5386)
"Dimension-Islands" → port `ModDimensionKeys.ISLANDS` = `orespawn:islands` (ModDimensionKeys.java:29).

### 12.2 `recently_placed` cooldown (shared)

Static, initialized 50 (OSW:30); decremented once per `generate` call per chunk when > 0
(OSW:37-39); set back to 50 by every successful structure placement, incl. `addD4Castle` (OSW:2224).
Global across all OreSpawn structures and dimensions.

### 12.3 D4 dispatch — FULL `i = random.nextInt(19)` outcome table (OSW:132-178)

Outer gate (OSW:134): `recently_placed == 0 && random.nextInt(100) == 0 &&
D4BigSpaceCheck(world, chunkX*16, 7, chunkZ*16)`.

`D4BigSpaceCheck(posX, 7, posZ)` (OSW:2655-2664): every block in x `posX-25..posX+39`,
z `posZ-25..posZ+29` at **y = 11** (posY+4) must be air, vanilla log (`field_150364_r`),
`OreSpawnMain.MyAppleLeaves`, or `OreSpawnMain.MyScaryLeaves`, else fail.

| i (OSW:135) | Method called | Cite |
|---|---|---|
| 0, 1, 2 | `addD4Castle` (Enormous Castle King/Queen) — **3/19 share** | OSW:136-137 |
| 3, 4, 5, 6 | `addD4GenericDungeon` — 4/19 | OSW:138-139 |
| 7 | `addD4EnderCastle` | OSW:141-143 |
| 8 | `addD4IncaPyramid` | OSW:144-146 |
| 9 | `addD4RobotLab` | OSW:147-149 |
| 10 | `addD4Mini` | OSW:150-152 |
| 11 | `addD4RubyDungeon` | OSW:153-155 |
| 12 | `addD4CephadromeAltar` | OSW:156-158 |
| 13 | `addD4Greenhouse` | OSW:159-161 |
| 14 | `addD4NightmareRookery` | OSW:162-164 |
| 15 | `addD4StinkyHouse` | OSW:165-167 |
| 16 | `addD4WhiteHouse` | OSW:168-170 |
| 17 | `addPumpkin` | OSW:171-173 |
| 18 | `addD4Rainbow` | OSW:174-176 |

After the gated block, unconditionally each chunk: `random.nextInt(300)==0` → `addD4CloudShark`
(OSW:179-181); `addUnstableAnts`, `addIslands`, `addD4Rocks` (OSW:182-184).

### 12.4 `addD4Castle(world, random, chunkX, chunkZ)` (OSW:2203-2228)

1. **LessLag gate** (OSW:2204-2206): `if (OreSpawnMain.LessLag != 0 && random.nextInt(2) != 0) return false;`
   — with LessLag on (1 or 2), half of all attempts abort. `LessLag` declared OSM:471, read from
   config `tweaks/LessLag` default 0 (OSM:1471).
2. Position: `posX = chunkX + nextInt(8)`, `posZ = chunkZ + nextInt(8)` (OSW:2207-2208) —
   note args are already `chunkX*16` block coords.
3. Ground scan (OSW:2209-2211): `posY` from **20 down to 5**, find first block == **grass**
   (`field_150349_c`); if none, return false.
4. Space check (OSW:2212-2218): every block in x `posX-20..posX+32`, z `posZ-4..posZ+32` at
   **y = posY+18** must be air, else return false (mirrors the −20 west stair arm + the 28+4 base).
5. Variant roll (OSW:2219-2223): `random.nextInt(2) == 1` → `makeEnormousCastle(posX, posY, posZ)`
   else `makeEnormousCastleQ(posX, posY, posZ)`.
6. `recently_placed = 50; return true` (OSW:2224-2225).

Effective per-chunk odds ≈ 1/100 × 3/19 (≈ 1 in 633) × cooldown × space/ground checks
(× 1/2 with LessLag).

### 12.5 Port worldgen wiring

King and Queen are **independent** structure sets (`challenge_tower_king.json` /
`challenge_tower_queen.json`: `random_spread` spacing 44, separation 22, salts 84320/84321),
biome-gated to `orespawn:island_biome` via `#orespawn:has_structure/challenge_tower_*`; the legacy
Feature wrapper was deleted because the ~90-block tower exceeds the 24-block WorldGenRegion write
window (ModFeatures.java:39-52). No `recently_placed`, `LessLag`, grass-scan, or air-box analog —
placement discipline comes from structure-set spacing instead.

---

## 13. Surprises / porting notes / MISSING-IN-PORT flags

1. **"Nightmare" spawners are PitchBlack.** The EntityList name "Nightmare" (GD:498 etc.) is
   registered to `PitchBlack.class` (OSM:4023-4027) — the port's `PITCH_BLACK` spawners
   (LDP:2126-2133) are correct despite the name.
2. **DSB types are 2 and 47, not 12.** Type 12 is `makePlayPool` (DSB:89-91). King = type 2
   (DSB:59-61), Queen = type 47 (DSB:194-196), roll `nextInt(50)` (DSB:52).
3. **PORT MISMATCH — "Jumpy Bug"**: legacy "Jumpy Bug" is `TrooperBug.class` (OSM:3943-3947), and
   the port has `ENTITY_TROOPER_BUG` (ME:274), but `LegacyDungeonPiece` maps Jumpy Bug to
   `ENTITY_SPIT_BUG` (LDP:2252, 2258, 2290, 2296 — commented "Jumpy Bug → SpitBug"). Legacy "Spit
   Bug" is a different entity (OSM:3951). Should be `ENTITY_TROOPER_BUG` for fidelity.
4. **PORT DEVIATION — level locked to 6** (`int level = 6;` LDP:1807, "QA Fix (Endgame Loot Gate)" comment LDP:1791-1806): legacy rolls
   level 1-6 with P(6)=5/18 (GD:202-205); the port always builds the full tower + Royal loot.
   Deliberate, documented in-code; still a behavior difference (all towers max-height, prize
   guaranteed, and the `nextInt(6)`/`nextInt(3)` RNG draws are skipped).
5. **PORT DEVIATION — scaffolding added**: legacy places **no climbable blocks**; the port adds
   scaffolding columns under every ceiling hole (LDP:2222-2226) and in the decor-6 shaft
   (LDP:2157-2160). Legacy players needed their own blocks/ender pearls.
6. **PORT PARTIAL — chest loot**: `fillChallengeContents` (LDP:2397-2458) replaces the five
   weighted lists with 7-10-item unweighted palettes. Divergences: level-2 exp bottles → ender
   pearls; level-3 `MyAmethyst` → vanilla `AMETHYST_SHARD` (port's own `AMETHYST_GEM` MI:112
   exists), ink-sac dye → `LAPIS_LAZULI`, and SQUID_ZOOKA + all 4 amethyst armor pieces dropped;
   level-1/2 lose shovel/hoe/axe (level-2 also loses all 4 Experience armor pieces);
   level-4 loses RAY_GUN, EXPERIENCE_CATCHER, ZOO_KEEPER, THUNDER_STAFF and 3 of 5 tools
   (pickaxe/sword kept); level-5's 83-spawn-egg jackpot list is reduced to 3 eggs
   (wither skeleton / iron golem / Mothra) + invented `NETHERITE_INGOT` /
   `ENCHANTED_GOLDEN_APPLE` entries. Weights (5/10/15 in the lists) are ignored — uniform pick.
   Flagged as **MISSING-IN-PORT (faithful level1-5 lists)**; §8 above is the authoritative source
   for a faithful re-fill.
7. **RESOLVED (D5) — `CriminalEgg`** (level5 #66): 1.7.10 "Criminal" is the port's BandP (audit
   correction WGEN-017, Phase C7), so `CriminalEgg` → `band_p_spawn_egg` (MI:801). All 83 egg
   entries therefore have port counterparts (§8.5).
8. **Prince/Princess egg identity**: legacy `ThePrinceEgg`/`ThePrincessEgg` are functional spawn
   eggs (OSM:5616/5630); the port chest uses plain trophy items `PRINCE_EGG`/`PRINCESS_EGG`
   (MI:786/790) even though `THE_PRINCE_SPAWN_EGG`/`THE_PRINCESS_SPAWN_EGG` exist (MI:983/987).
   Decide which is canonical.
9. **Queen calls the King's `fill_chests` for decors 2-6** (GD:6742/6771/6804/6841/6882), only
   decor 1 uses `fill_chestsQ` (GD:6925). Harmless (reward<6 there), but a faithful port should
   reproduce the call graph, or at least the outcome.
10. **Buried Large Worm minefield**: level-6 towers scatter ~74 "Large Worm" spawners at y = cposy−1
    in an 84×84 ring **outside** the castle (GD:362-374) — far beyond the visible footprint and the
    port structure's bounding box. Port reproduces it (LDP:1973-1986); ensure the structure's
    bounding box/piece stitching covers x,z −28..+55 or the ring will be clipped at chunk borders.
11. **`j` accumulates even for unbuilt floors** (GD:293-313): heights are added outside the level
    guards, so a level-3 tower still "reserves" the upper air — only relevant if re-deriving Y math.
12. **Torch placement uses neighbor-updating setBlock** (`func_147449_b`, GD:241-244) while
    everything else uses flag-2 fast writes — Extreme Torches must survive a block-update check.
13. **Chest facing metadata** (GD:744/754/765/776): meta 5/4/3/2 makes each chest face the room
    center; a port using default chest states loses the facing.
14. **50/50 King-vs-Queen single roll** (OSW:2219) became two independent structure sets in the
    port — both variants can generate near each other; combined density is governed by spacing 44
    per set rather than one shared roll + global cooldown.
