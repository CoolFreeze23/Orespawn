# Nightmare Structures — Extraction Spec (Phase D5)

Source of truth: `reference_1_7_10_source/sources/danger/orespawn/` (CFR 0.152 decompile of
orespawn-1.7.10-20.3). Port registries: `src/main/java/danger/orespawn/`.
All paths below are relative to `C:\Homework\Projects\Orespawn\` unless absolute.

Covers two distinct structures that share the "Nightmare" name:

1. **`NightmareDungeon`** (`NightmareDungeon.java`, 95 lines) — a standalone dungeon class that is
   **dead code** in 1.7.10 (proof in §1).
2. **`makeNightmareRookery`** (`GenericDungeon.java:5242-5312`) — the structure that actually
   generates in-world (Islands dimension, D4) and via DungeonSpawnerBlock type 38.

Legacy vanilla constructor reference: `WeightedRandomChestContent(Item item, int meta, int min, int max, int weight)` —
argument order in the 1.7.10 decompile is `(item, meta, minStack, maxStack, weight)`.

---

## 1. Dead-code verification: `NightmareDungeon` is never instantiated

Search performed: ripgrep for `NightmareDungeon` over the **entire**
`reference_1_7_10_source/` tree (all 586 sources, assets, mcmod.info — every file, not just
`sources/danger/orespawn`).

**Every hit, exhaustively:**

| # | File | Line | Text |
|---|------|------|------|
| 1 | `reference_1_7_10_source/sources/danger/orespawn/NightmareDungeon.java` | 17 | `public class NightmareDungeon {` |

That is the **only** occurrence in the tree. There is:

- no `new NightmareDungeon` anywhere;
- no field or local of type `NightmareDungeon` in any other file;
- no `import danger.orespawn.NightmareDungeon` in any other file (the flat package means no
  import would even be needed — and no qualified use exists either);
- no string reference (`"NightmareDungeon"`) in any source, asset, or metadata file.

Cross-checks that rule out indirect invocation:

- `DungeonSpawnerBlock.java` dispatches only through `OreSpawnMain.MyDungeon.make*` methods
  (a `GenericDungeon` instance); type 38 calls `makeNightmareRookery`, not this class
  (`DungeonSpawnerBlock.java:167-169`).
- `OreSpawnWorld.java`'s only "Nightmare" placement is `addD4NightmareRookery`
  (`OreSpawnWorld.java:2253`), which calls `OreSpawnMain.MyDungeon.makeNightmareRookery`
  (`OreSpawnWorld.java:2269`).

**Verdict: `NightmareDungeon` is never instantiated and is unreachable dead code in 1.7.10.**
Its `makeDungeon` can only run if some code constructs the class, and nothing does. Extracted
below for the record only; it should NOT be ported as generating content (at most preserved as
a documented no-op / optional easter egg).

---

## 2. `NightmareDungeon.makeDungeon` — full spec (dead code, for the record)

File: `reference_1_7_10_source/sources/danger/orespawn/NightmareDungeon.java`

### 2.1 Geometry (all from `makeDungeon`, lines 28-89)

Anchor: method args `(world, cposx, cposy, cposz)` = the **minimum corner** (structure extends
+X/+Z/+Y from it).

| Element | Detail | Citation |
|---|---|---|
| Footprint | `width = 25` (both X and Z), `height = 12` — a 25×25×12 box | NightmareDungeon.java:32-33 |
| Air fill | Entire 25×25×12 volume set to air (`Blocks.field_150350_a`) first | NightmareDungeon.java:34-40 |
| Floor (j=0) | 25×25 layer of `OreSpawnMain.MyRTPBlock` (random-teleport block) | NightmareDungeon.java:41-46 |
| Ceiling (j=height-1=11) | 25×25 layer of bedrock (`Blocks.field_150357_h`) | NightmareDungeon.java:47-52 |
| Walls Z=0 and Z=24 | full height ×25 length, each block 50/50 bedrock/obsidian via `setThisBlock` | NightmareDungeon.java:53-60 |
| Walls X=0 and X=24 | full height ×25 length, same 50/50 bedrock/obsidian | NightmareDungeon.java:61-68 |
| Wall randomizer | `world.rand.nextInt(2)==1` → bedrock (`field_150357_h`), else obsidian (`field_150343_Z`) | NightmareDungeon.java:20-26 |
| Block placement | `FastSetBlock` → `OreSpawnMain.setBlockFast(world, x, y, z, block, 0, 3)` (meta 0, flags 3) | NightmareDungeon.java:91-93 |

Note: the wall loops **overwrite** the floor (j=0) and ceiling (j=11) blocks along the perimeter,
since the wall loops run j = 0..height-1 (lines 54, 62) — perimeter columns are 50/50
bedrock/obsidian top to bottom. There is NO doorway/entrance carved; the box is sealed.

### 2.2 Spawner (center)

| Property | Value | Citation |
|---|---|---|
| Position | `(cposx + 12, cposy + 1, cposz + 12)` (`width/2 = 12`, 1 above floor) | NightmareDungeon.java:69 |
| Block | vanilla mob spawner `Blocks.field_150474_ac`, meta 0, flags 2 | NightmareDungeon.java:69 |
| Mob | `world.rand.nextInt(2)==1` → `"Emperor Scorpion"`, else `"Nightmare"` (50/50) | NightmareDungeon.java:72-76 |

Mob-name → class → port mapping:

| 1.7.10 spawner string | 1.7.10 class | Registration citation | Port entity | Port citation |
|---|---|---|---|---|
| `"Emperor Scorpion"` | `EmperorScorpion` | OreSpawnMain.java:3783 | `ModEntities.ENTITY_EMPEROR_SCORPION` (`orespawn:emperor_scorpion`) | src/main/java/danger/orespawn/ModEntities.java:221-223 |
| `"Nightmare"` | `PitchBlack` | OreSpawnMain.java:4023 | `ModEntities.PITCH_BLACK` (`orespawn:pitch_black`) | src/main/java/danger/orespawn/ModEntities.java:113-115 |

### 2.3 Chests (two, diagonal of center)

| Property | Value | Citation |
|---|---|---|
| Chest 1 | `(cposx + 13, cposy + 1, cposz + 13)` (center +1,+1), `Blocks.field_150486_ae` meta 0 flags 2 | NightmareDungeon.java:79-80 |
| Chest 2 | `(cposx + 11, cposy + 1, cposz + 11)` (center −1,−1) | NightmareDungeon.java:84-85 |
| Fill count | `4 + world.rand.nextInt(7)` = **4-10 weighted stacks per chest** | NightmareDungeon.java:82, 87 |
| Table | `chestContentsList` (below) for both chests | NightmareDungeon.java:82, 87 |

### 2.4 `chestContentsList` — FULL 31-entry loot table

Declared: NightmareDungeon.java:18 (single line; every entry below is from that line).
Format: `(item, min, max, weight)` — all metas are 0. Total weight = **660**.

| # | 1.7.10 field | min | max | weight | Port item (registry name) | Port citation (src/main/java/danger/orespawn/ModItems.java) |
|---|---|---|---|---|---|---|
| 1 | `OreSpawnMain.CageEmpty` (orig reg OreSpawnMain.java:5409 "cageempty") | 3 | 10 | 20 | `CAGE_EMPTY` (`cage_empty`) | :778-779 |
| 2 | `ExperienceBody` | 1 | 1 | 25 | `EXPERIENCE_CHESTPLATE` (`experience_chestplate`) | :645-647 |
| 3 | `ExperienceLegs` | 1 | 1 | 25 | `EXPERIENCE_LEGGINGS` (`experience_leggings`) | :648-650 |
| 4 | `ExperienceHelmet` | 1 | 1 | 25 | `EXPERIENCE_HELMET` (`experience_helmet`) | :642-644 |
| 5 | `ExperienceBoots` | 1 | 1 | 25 | `EXPERIENCE_BOOTS` (`experience_boots`) | :651-653 |
| 6 | `MyExperienceSword` (orig OreSpawnMain.java:1657 "experiencesword") | 1 | 1 | 25 | `EXPERIENCE_SWORD` (`experience_sword`) | :300-301 |
| 7 | `UltimateBody` | 1 | 1 | 25 | `ULTIMATE_CHESTPLATE` (`ultimate_chestplate`) | :575-577 |
| 8 | `UltimateLegs` | 1 | 1 | 25 | `ULTIMATE_LEGGINGS` (`ultimate_leggings`) | :578-580 |
| 9 | `UltimateHelmet` | 1 | 1 | 25 | `ULTIMATE_HELMET` (`ultimate_helmet`) | :572-574 |
| 10 | `UltimateBoots` | 1 | 1 | 25 | `ULTIMATE_BOOTS_ARMOR` (`ultimate_boots`) | :581-583 |
| 11 | `MyUltimateSword` | 1 | 1 | 25 | `ULTIMATE_SWORD` (`ultimate_sword`) | :165-167 |
| 12 | `MyUltimatePickaxe` | 1 | 1 | 25 | `ULTIMATE_PICKAXE` (`ultimate_pickaxe`) | :168-169 |
| 13 | `MyUltimateShovel` | 1 | 1 | 25 | `ULTIMATE_SHOVEL` (`ultimate_shovel`) | :170-171 |
| 14 | `MyUltimateHoe` | 1 | 1 | 25 | `ULTIMATE_HOE` (`ultimate_hoe`) | :172-173 |
| 15 | `MyUltimateAxe` | 1 | 1 | 25 | `ULTIMATE_AXE` (`ultimate_axe`) | :174-175 |
| 16 | `MyUltimateBow` (orig OreSpawnMain.java:1705 "ultimatebow") | 1 | 1 | 25 | `ULTIMATE_BOW` (`ultimate_bow`) | :316-317 |
| 17 | `MyBertha` (orig OreSpawnMain.java:1645 "berthasmall" = Big Bertha) | 1 | 1 | 25 | `BIG_BERTHA` (`big_bertha`) | :265-268 |
| 18 | `MySlice` (orig OreSpawnMain.java:1646 "slicesmall") | 1 | 1 | 25 | `SLICE` (`slice`) | :271-274 |
| 19 | `MyAmethyst` (orig OreSpawnMain.java:1861 "amethyst") | 2 | 8 | 15 | `AMETHYST_GEM` (`amethyst_gem`) | :112 |
| 20 | `MyBacon` (orig OreSpawnMain.java:1850 "cookedbacon") | 6 | 12 | 20 | `COOKED_BACON` (`cooked_bacon`) | :423 |
| 21 | `MyButterCandy` (orig OreSpawnMain.java:1849 "buttercandy") | 6 | 12 | 20 | `BUTTER_CANDY` (`butter_candy`) | :417 |
| 22 | `MyAmethystPickaxe` | 1 | 1 | 15 | `AMETHYST_PICKAXE` (`amethyst_pickaxe`) | :192-193 |
| 23 | `MyAmethystShovel` | 1 | 1 | 15 | `AMETHYST_SHOVEL` (`amethyst_shovel`) | :194-195 |
| 24 | `MyAmethystHoe` | 1 | 1 | 15 | `AMETHYST_HOE` (`amethyst_hoe`) | :196-197 |
| 25 | `MyAmethystAxe` | 1 | 1 | 15 | `AMETHYST_AXE` (`amethyst_axe`) | :198-199 |
| 26 | `MyAmethystSword` | 1 | 1 | 15 | `AMETHYST_SWORD` (`amethyst_sword`) | :190-191 |
| 27 | `AmethystBody` | 1 | 1 | 15 | `AMETHYST_CHESTPLATE` (`amethyst_chestplate`) | :673-675 |
| 28 | `AmethystLegs` | 1 | 1 | 15 | `AMETHYST_LEGGINGS` (`amethyst_leggings`) | :676-678 |
| 29 | `AmethystHelmet` | 1 | 1 | 15 | `AMETHYST_HELMET` (`amethyst_helmet`) | :670-672 |
| 30 | `AmethystBoots` | 1 | 1 | 15 | `AMETHYST_BOOTS_ARMOR` (`amethyst_boots`) | :679-681 |
| 31 | `MyThunderStaff` (orig OreSpawnMain.java:1747 "thunderstaff") | 1 | 1 | 5 | `THUNDER_STAFF` (`thunder_staff`) | :324-325 |

No MISSING-IN-PORT entries — all 31 items have port equivalents.

Block mapping for the structure itself:

| 1.7.10 block | Port equivalent | Citation |
|---|---|---|
| `OreSpawnMain.MyRTPBlock` (orig reg OreSpawnMain.java:1879 "blockteleport") | `ModBlocks.BLOCK_TELEPORT` (`block_teleport`, class `RTPBlock`) | src/main/java/danger/orespawn/ModBlocks.java:74-75 |
| `Blocks.field_150357_h` | `minecraft:bedrock` | vanilla |
| `Blocks.field_150343_Z` | `minecraft:obsidian` | vanilla |
| `Blocks.field_150350_a` | `minecraft:air` | vanilla |
| `Blocks.field_150474_ac` | `minecraft:spawner` | vanilla |
| `Blocks.field_150486_ae` | `minecraft:chest` | vanilla |

---

## 3. `GenericDungeon.makeNightmareRookery` — the live structure

File: `reference_1_7_10_source/sources/danger/orespawn/GenericDungeon.java`, lines 5242-5312.
Helpers: `getChestTileEntity` (GenericDungeon.java:75), `getSpawnerTileEntity`
(GenericDungeon.java:86), `FastSetBlock` (GenericDungeon.java:187 → `OreSpawnMain.setBlockFast`,
meta 0, flags 3).

### 3.1 Overall shape

Two identical "spike ridge" passes (loop 1: lines 5252-5281; loop 2: lines 5282-5311), each
building a wandering line of 26 random-height stone pillars. Every pillar tall enough gets a
Nightmare spawner on top with a chest directly beneath it. The result is a cluster of jagged
stone spires ("rookery") crowned with Nightmare spawners.

Constants (identical for both passes):

| Property | Value | Citation |
|---|---|---|
| Mob | `monster = "Nightmare"` (all spawners) | GenericDungeon.java:5246 |
| Loot list | `this.NightmareRookeryContentsList` | GenericDungeon.java:5247 |
| X sweep | `i = -5 .. 20` inclusive (26 columns per pass) | GenericDungeon.java:5252, 5282 |
| Z wander | `k += rand.nextInt(3) - 1` per column (−1/0/+1 drunkard's walk, cumulative; k starts at 0 and is **NOT reset** between the two passes — pass 2 continues from pass 1's final k) | GenericDungeon.java:5249, 5253, 5283 |
| Pillar height | `h = rand.nextInt(20) + 1` (1-20 blocks) | GenericDungeon.java:5254, 5284 |
| Pillar core | column of `Blocks.field_150348_b` (stone) at `(cposx+i, cposy+j, cposz+k)`, j = 0..h-1 | GenericDungeon.java:5256, 5286 |
| Side bulge | at each j, each of the four neighbors (+X, −X, +Z, −Z) independently gets a stone block when `rand.nextInt(j + 5) == 1` (bulge probability shrinks with height: 1/5 at base → 1/24 near top) | GenericDungeon.java:5257-5268, 5287-5298 |

### 3.2 Spawner + chest cap (per pillar, both passes)

Trigger: only when the column reaches `j >= 18` (i.e. `h` rolled 19 or 20; expected ≈ 2/20 = 10%
of pillars per pass, ≈ 5.2 capped spires per structure on average). Once triggered the inner
loop breaks (`continue block0`/`block2`) so each pillar caps at most once.

| Element | Position | Detail | Citation |
|---|---|---|---|
| Spawner | `(cposx+i, cposy+j+2, cposz+k)` (2 above the trigger height — floats 1 air gap above the chest) | vanilla spawner block, mob `"Nightmare"` (= `PitchBlack`, OreSpawnMain.java:4023 → port `ModEntities.PITCH_BLACK`, ModEntities.java:113-115) | GenericDungeon.java:5270-5274 (pass 2: 5300-5304) |
| Chest | `(cposx+i, cposy+j+1, cposz+k)` (directly on top of the pillar, under the spawner) | vanilla chest | GenericDungeon.java:5275-5276 (pass 2: 5305-5306) |
| Chest fill | `4 + rand.nextInt(5)` = **4-8 weighted stacks** from `NightmareRookeryContentsList` | GenericDungeon.java:5278 (pass 2: 5308) |

### 3.3 Footprint extents

- X: `cposx − 5` .. `cposx + 20` core, ±1 more from side bulges → **cposx−6 .. cposx+21** (28 wide).
- Z: random walk of 26 steps per pass, cumulative across 52 steps; theoretically `cposz − 52` ..
  `cposz + 52` (±1 bulge), typically within ≈ ±10. Unbounded by any clamp
  (GenericDungeon.java:5253, 5283).
- Y: `cposy` .. `cposy + 21` max (pillar to j=19, spawner at j+2=21).
- The placement pre-check (§4) only clears X `−5..24`, Z `−4..4` at Y+18, so the Z walk can and
  will exceed the checked envelope — original behavior, preserve as-is.

### 3.4 `NightmareRookeryContentsList` — FULL 10-entry loot table

Declared: GenericDungeon.java:29 (single line). Format `(item, min, max, weight)`, all metas 0.
Total weight = **270**.

| # | 1.7.10 entry | min | max | weight | Port item | Port citation |
|---|---|---|---|---|---|---|
| 1 | `OreSpawnMain.MyDeadStinkBug` (orig reg OreSpawnMain.java:1951 "deadstinkbug") | 4 | 10 | 35 | `ModItems.DEAD_STINK_BUG` (`dead_stink_bug`) | src/main/java/danger/orespawn/ModItems.java:161 |
| 2 | `Item.func_150898_a(OreSpawnMain.MyFlowerBlackBlock)` (block-item of Black Flower) | 4 | 10 | 35 | `ModItems.FLOWER_BLACK_ITEM` (`flower_black`, block `ModBlocks.FLOWER_BLACK` ModBlocks.java:258) | src/main/java/danger/orespawn/ModItems.java:93 |
| 3 | `Item.func_150898_a(OreSpawnMain.MyFlowerScaryBlock)` (block-item of Scary Flower) | 4 | 10 | 35 | `ModItems.FLOWER_SCARY_ITEM` (`flower_scary`, block `ModBlocks.FLOWER_SCARY` ModBlocks.java:261) | src/main/java/danger/orespawn/ModItems.java:94 |
| 4 | `OreSpawnMain.PitchBlackEgg` (orig reg OreSpawnMain.java:5567 "eggnightmare") | 4 | 10 | 25 | `ModItems.PITCH_BLACK_SPAWN_EGG` (`pitch_black_spawn_egg`; mapping confirmed by port comment "orig PitchBlackEgg" at entity/EasterBunny.java:157) | src/main/java/danger/orespawn/ModItems.java:831-832 |
| 5 | `OreSpawnMain.AntRobotKit` (orig reg OreSpawnMain.java:1724 "antrobotkit") | 1 | 1 | 10 | `ModItems.ANT_ROBOT_KIT` (`ant_robot_kit`) | src/main/java/danger/orespawn/ModItems.java:525-528 |
| 6 | `OreSpawnMain.SpiderRobotKit` (orig reg OreSpawnMain.java:1723 "spiderrobotkit") | 1 | 1 | 10 | `ModItems.SPIDER_ROBOT_KIT` (`spider_robot_kit`) | src/main/java/danger/orespawn/ModItems.java:521-524 |
| 7 | `Items.field_151103_aS` (bone) | 6 | 16 | 25 | `minecraft:bone` | vanilla |
| 8 | `Items.field_151007_F` (string) | 6 | 16 | 25 | `minecraft:string` | vanilla |
| 9 | `Items.field_151078_bh` (rotten flesh) | 3 | 10 | 35 | `minecraft:rotten_flesh` | vanilla |
| 10 | `Items.field_151062_by` (bottle o' enchanting) | 4 | 10 | 35 | `minecraft:experience_bottle` | vanilla |

No MISSING-IN-PORT entries.

Block mapping: pillar/bulge block `Blocks.field_150348_b` = `minecraft:stone`; spawner/chest vanilla.

---

## 4. World placement of the Nightmare Rookery

File: `reference_1_7_10_source/sources/danger/orespawn/OreSpawnWorld.java`.

### 4.1 Dimension + odds (decorate hook)

The D4 structure roll (OreSpawnWorld.java:132-178) runs only when
`world.provider.dimensionId == OreSpawnMain.DimensionID4` (OreSpawnWorld.java:132).

**DimensionID4 = the Islands dimension**: `DimensionID4 = BaseDimensionID + 3`
(OreSpawnMain.java:1598), provider `WorldProviderOreSpawn4` (OreSpawnMain.java:5385-5386) whose
name is `"Dimension-Islands"` (WorldProviderOreSpawn4.java:22-23). Port key:
`ModDimensionKeys.ISLANDS` = `orespawn:islands` (src/main/java/danger/orespawn/ModDimensionKeys.java:20,29).
(Note: reference INDEX.md loosely calls WorldProviderOreSpawn4 "Utopia" — the provider source and
the port's ModDimensionKeys both say Islands; trust the source.)

Roll chain per chunk (OreSpawnWorld.java:134-164):

1. `recently_placed == 0` — global structure cooldown counter, initialized 50
   (OreSpawnWorld.java:30), decremented once per decorate call (OreSpawnWorld.java:38), reset to
   50 after any successful structure placement (e.g. OreSpawnWorld.java:2270).
2. `random.nextInt(100) == 0` — 1/100 gate (OreSpawnWorld.java:134).
3. `D4BigSpaceCheck(world, chunkX*16, 7, chunkZ*16)` — requires every block at Y=11 (posY 7 + 4)
   in X −25..39, Z −25..29 to be air, vanilla log, `MyAppleLeaves`, or `MyScaryLeaves`
   (OreSpawnWorld.java:2655-2664).
4. `i = random.nextInt(19)`; **`i == 14` selects the Nightmare Rookery** (OreSpawnWorld.java:135,
   162-164) — 1/19 within the gate, i.e. ≈ 1 in 1900 eligible chunks before cooldown/space
   effects.

### 4.2 `addD4NightmareRookery` (OreSpawnWorld.java:2253-2274)

| Step | Detail | Citation |
|---|---|---|
| LessLag gate | if `OreSpawnMain.LessLag != 0` (config "LessLag", default 0, OreSpawnMain.java:1471) then `random.nextInt(2) != 0` aborts (50% skip) | OreSpawnWorld.java:2254-2256 |
| Position | `posX = chunkX + rand.nextInt(8)`, `posZ = chunkZ + rand.nextInt(8)` (chunk-corner blocks + 0-7) | OreSpawnWorld.java:2257-2258 |
| Ground scan | `posY` from 20 down to 5; needs `world.getBlock(posX,posY,posZ) == Blocks.field_150349_c` (grass block) | OreSpawnWorld.java:2259-2261 |
| Sky check | for X −5..24, Z −4..4: block at `posY + 18` must be air, else return false | OreSpawnWorld.java:2262-2268 |
| Build | `OreSpawnMain.MyDungeon.makeNightmareRookery(world, posX, posY, posZ)` — anchored **at grass level** (not +1) | OreSpawnWorld.java:2269 |
| Cooldown | `recently_placed = 50` | OreSpawnWorld.java:2270 |

### 4.3 DungeonSpawnerBlock trigger

`DungeonSpawnerBlock` **type 38** invokes
`OreSpawnMain.MyDungeon.makeNightmareRookery(world, clickedX, clickedY, clickedZ)`
(DungeonSpawnerBlock.java:167-169).

### 4.4 Port status

No `Rookery` / `makeNightmare` code exists anywhere in `src/main/java` yet (ripgrep verified) —
the rookery is **entirely unported**; this spec is the implementation source.

---

## 5. Surprises / porting notes

1. **`NightmareDungeon` is dead code** (§1) — do not wire it into worldgen; the 31-entry loot
   table above is recorded only for completeness (it is a strictly richer variant of similar
   dungeon tables and could optionally back a data-pack loot table if the structure is ever
   revived).
2. **NightmareDungeon's box is sealed** — no entrance is carved, and the RTP-block floor
   teleports anything standing on it (class `RTPBlock`); clearly designed as a trap room.
3. **Rookery Z drift carries across passes** — `k` is not reset before the second 26-column pass
   (GenericDungeon.java:5283 reuses the pass-1 final `k`), so pass 2 starts where pass 1's ridge
   ended. Preserve for parity.
4. **Rookery spawner floats** — spawner at `j+2` with chest at `j+1` leaves the spawner sitting
   directly on the chest (no air gap between chest top and spawner; the "gap" is chest height).
5. **Placement scans Y 20→5 for grass** — Islands-dimension terrain is low; a naive port that
   scans normal overworld heights will never place it.
6. **INDEX.md dimension label is wrong for D4** (says Utopia); provider source says
   `"Dimension-Islands"` (WorldProviderOreSpawn4.java:23). Port targets `orespawn:islands`.
