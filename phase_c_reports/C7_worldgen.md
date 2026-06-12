# Phase C slice 7 — Worldgen (WGEN- series)

Scope: every `WGEN-` finding with no prior resolution — 14 DIVERGENT, 17 PARTIAL,
8 UNVERIFIED (39 handled), plus 11 MISSING findings skipped per ground rules
(WGEN-003, 004, 007, 013, 015, 021, 037, 038, 042, 044, 045 — Phase D scope,
no resolution line).

Outcome: **29 FIXED, 5 VERIFIED-CORRECT (009, 041, 046, 047, 050), 5 PARTIAL**
(005, 014, 018, 033, 036 — remainders named below with owners),
0 still-UNVERIFIED.

Every original value below was re-verified in the 1.7.10 CFR source before the
fix. Where the original used per-chunk `nextInt` rolls and the port uses placed
features / structure sets, the rate-equivalence math is shown inline
(random_spread expected density ≈ 1/spacing² chunks).

## Fixed findings

### Ore veins (WGEN-001, 002, 011, 024, 032)

The original ore loop (orig `OreSpawnWorld.java:805-877` overworld, identical
math in `ChunkOreGenerator.java:471-544` for Utopia/Mining/Village/Chaos) is:
`attempts = rate + nextInt(dice)`; truncating `÷3` (ores) or `÷2` (trolls) when
`LessOre != 0`; per attempt `y = nextInt(128)` REJECTED outside
`[mindepth, maxdepth]`. A vanilla `count` + `height_range` pair places every
attempt inside the window instead of discarding misses, so a new
`orespawn:vein_count` placement modifier (`world/OreSpawnVeinPlacement.java`)
reproduces the arithmetic verbatim, with `passes`/`less_ore_passes` covering
the Mining ×3 mechanic. Stats: orig `OreSpawnMain.java:1573-1585`.

| ID | Original (citation → value) | Old port | New port | Notes |
|----|------------------------------|----------|----------|-------|
| WGEN-001 | Uranium rate 3 + nextInt(9), Y0-30, ÷3 (`OreSpawnWorld.java:805-816`, stats `:1575`); Titanium 3+d9 Y0-20 ÷3 (`:818-829`); Amethyst 2+d12 Y0-25 ÷3 (`:831-842`); Salt 5+d9 Y50-127 ÷3 (`:844-855`; `nextInt(128)` caps at 127); RedAntTroll & TermiteTroll 4+d4 Y5-50 ÷2 size 4 (`:857-877`) | inflated counts, Y-floors at −64, plain `count`+`height_range` | `vein_count` with the exact rate/dice/window/divisor per ore (`placed_feature/ore_*.json`, `red_ant_troll.json`, `termite_troll.json`) | Clump sizes already matched stats (4/4/6/12/4/4). Utopia/Village/Chaos get the same single-pass features plus the troll features (ChunkOreGenerator has them; the old port omitted trolls outside the overworld) and **lose `ore_ruby`** — `generateOresInChunk` has no ruby pass; ruby is overworld/Mining/Nether only. |
| WGEN-002 | Ruby: rate 10 + nextInt(5) attempts, y=nextInt(128) rejected >50, then descend y→6 looking for lava(still/flowing) directly above stone; replace the stone with ONE ruby ore (`OreSpawnWorld.java:879-892`) | invented `minecraft:ore` vein size 2 | `configured_feature/ore_ruby.json` → `orespawn:ruby_lava_seek` `{attempts_base 10, attempts_spread 5, max_y 50}`; placed with biome check only (feature consumes the chunk origin) | Feature ported in `world/feature/RubyLavaSeekFeature.java`. |
| WGEN-011 | Mining runs `generateOresInChunk` once, then twice more only when LessOre==0 (`ChunkProviderOreSpawn2.java:191-195`); `generateRuby` ×1 (+×2 LessOre==0, rate 10+nextInt(7), `OreSpawnWorld.java:57-63,330-347`); lapis boost 45× size-7 + 25× size-4, y=nextInt(128) accepted <50, whole block skipped when LessOre≠0 (`:64-77`) | single-pass, no lapis boost | `*_mining` placed features with `passes:3, less_ore_passes:1`; `ore_ruby_mining` (spread 7) under `less_ore_count{count:3, less_ore_count:1}`; `ore_lapis_mining_large/small` with `vein_count{45/25, Y0-49, less_ore_passes:0}`; mining_biome step6 swapped to the `_mining` variants + trolls + lapis | Vanilla-ore tripling (diamond/gold/etc. boost block) is WGEN-004 (MISSING, Phase D). |
| WGEN-024 | n/a — no pink-tourmaline or kyanite veins in 1.7.10 | both `ore_kyanite` and `ore_pink_tourmaline` generating in Crystal | `ore_pink_tourmaline` configured+placed JSONs deleted; `add_crystal_dim_ores.json` now lists only `ore_kyanite` | Kyanite kept as the documented crafting-chain exception — **PARITY_NOTES PN-009**. |
| WGEN-032 | Nether: Lavafoam 15+nextInt(10) veins size 6 in netherrack, ÷3 LessOre, y=nextInt(108)+10; Ruby 5+nextInt(5) size 2, same Y band (`OreSpawnWorld.java:243-271`) | absent | `lavafoam_nether` / `ore_ruby_nether` ore features, placed with `less_ore_count{uniform 15-24 / 5-9, divisor 3}` + height_range 10-117, wired by `add_nether_ores.json` (#minecraft:is_nether) | Y band has no rejection in orig, so plain height_range is exact. Nether mosquito/ant entity spawns are covered by the existing `add_nether_spawns.json`. |

### Surface decorations (WGEN-006, 008, 030, 048)

| ID | Original (citation → value) | Old port | New port | Notes |
|----|------------------------------|----------|----------|-------|
| WGEN-006 | `addAnts` (`OreSpawnWorld.java:1472-1507`): 1/30 chunk gate, 4 attempts, scan Y100→41 for grass; 1/redfreq → special anthill (nextInt(4): red/rainbow/unstable/termite) else black ant block. Calls: overworld redfreq 4 (`:323`), Utopia 4 (via generateSurface `:41`), Village 4 (`:118`), Mining 2 ×2 (`:106-107`), Chaos 2 (`:206`) | never world-placed | `orespawn:anthill` feature (`AnthillFeature.java`); `anthill` (redfreq 4) via `add_anthills.json` #is_overworld + utopia/village biome step9; `anthill_mining` (redfreq 2, count 2); `anthill_chaos` (redfreq 2) | |
| WGEN-008 | Utopia veggies: `addVeggies` in generateSurface (`:279`) + a second call when no huge tree (`:46`); King/Queen Altar `nextInt(2000)==1`, 50/50 king/queen (`:2549-2571`) | no veggie patches; royal_altars 48/24 (=1/2304) | `veggie_patch_utopia` (count 2) in utopia step9; `royal_altars` spacing 45/22 (45²=2025 ≈ 1/2000) | Second veggie call is conditional on `!addHugeTree` (1/50 gate) in orig — count 2 overstates by ~2 %; noted as the closest datapack expression. |
| WGEN-030 | Chaos surface: butterflies/moths (entity spawns), `addVeggies` (`:205`), `addAnts(…,2)` (`:206`) — and NO dungeons/towers (`:203-208`) | veggies/ants missing; invented generic dungeon + challenge towers in chaos | `veggie_patch` + `anthill_chaos` in chaos step9; towers re-tagged to Islands (WGEN-043); chunk-generator dungeon dispatch already excludes Chaos | `addVeggies` gate is 1/15 with 8 attempts (`:1882-1921`), crop picker carrots/potatoes/radish/lettuce/melon-stem(1/10)/duplicator-sapling(no-op, tree is WGEN-044) — `VeggiePatchFeature.java`. |
| WGEN-048 | Rainbow/unstable ant blocks obtainable from anthills (`:1488-1499`); Islands `addUnstableAnts` 1/30 gate, 3 attempts, scan Y20→3 (`:1572-1588`) | rainbow/unstable ant blocks never generated → Village/Islands unreachable in survival | anthill picker covers both; `unstable_anthill` feature in island_biome step9 (`UnstableAnthillFeature.java`) | |

### Dimensions (WGEN-010, 016, 019, 028, 031)

| ID | Original (citation → value) | Old port | New port | Notes |
|----|------------------------------|----------|----------|-------|
| WGEN-010 | Mining populates vanilla mineshafts/strongholds/scattered features (`ChunkProviderOreSpawn2.java:439-445`), water lakes 1/4, lava lakes 1/8 biased-bottom, 8 monster rooms per chunk (vanilla populate body) | absent | mining/village added to `minecraft:has_structure/mineshaft` + `stronghold` tags; `lake_water_dim` (1/4), `lake_lava_dim` (1/8), `monster_room_dim` (count 8) in both biomes | Scattered features are biome-gated (extreme-hills host has none); snow/ice n/a. |
| WGEN-016 | Village populates like Mining minus the ore passes ×3 (`ChunkProviderOreSpawn3.java:179` + populate), plus ants(4) `:118`, apple trees `:119`, generic dungeon `:120` | dimension style no-op | WGEN-010 mechanisms + `anthill` + `magic_apple_tree` in village step9 + generic dungeon 1/16 in the chunk-generator dispatch | Actual village buildings = WGEN-015 (MISSING, Phase D). |
| WGEN-019 | Islands: flat plane — bedrock Y0, dirt Y1-6, grass Y7 (`ChunkProviderOreSpawn4.java:30-32`) | `minecraft:floating_islands` noise | `noise_settings/islands.json` (y_clamped_gradient density, surface rule bedrock@0/grass@7); island_biome carvers/vanilla features emptied | |
| WGEN-028 | Chaos: nether-shaped 128-high stone terrain, grass/dirt surface band ~Y60-65 (`ChunkProviderOreSpawn6.java`) | overworld noise | `noise_settings/chaos.json` (nether base_3d_noise router adapted, stone default, grass/dirt band rule) | |
| WGEN-031 | `WorldProviderOreSpawn6` overrides NO sky/fog members — vanilla visuals | audit suspected custom constants | verified vanilla; `dimension_type/chaos.json` aligned min_y 0 / height 256 / ambient_light 0.0 | **Audit error** (see below). |

### Spawn rosters (WGEN-009, 012, 017, 020, 029)

Authority: `BiomeGenUtopianPlains.java` — ctor `:88-140` (Utopia),
`setIslandCreatures :142-199` (lists reset), `setVillageCreatures :272-332`
(lists NOT reset — Utopia ctor + 1.7.10 `BiomeGenBase` vanilla defaults
remain), `setChaosCreatures :334-516` (reset); Mining overlay
`ChunkProviderOreSpawn2.java:374-429` on top of vanilla Extreme Hills
(`WorldProviderOreSpawn2` uses `WorldChunkManagerHell(extremeHills)`).
The 1.7.10 defaults (sheep 12 / pig 10 / chicken 10 / cow 8; spider/zombie/
skeleton/creeper/slime 100, enderman 10, witch 5; squid 10; bat 10) are now
present in Utopia, Village and Mining, exactly as the un-cleared lists were.
The `dim_*_locals` biome modifiers were deleted: they duplicated several
biome-JSON entries (silently doubling those weights) and carried inventions.

| ID | Original (citation → value) | Old port | New port | Notes |
|----|------------------------------|----------|----------|-------|
| WGEN-009 | Cricket 5(4,6) ambient (`:132`), Frog 5(4,6) water (`:135`) | cricket 5(4-6), frog 5(4-6) | unchanged | VERIFIED-CORRECT. |
| WGEN-012 | Mining monsters: Alosaurus 8(1,2), TRex 6(1,2), Nastysaurus 6(1,2), Pointysaurus 10(4,8), GammaMetroid 35(4,7), Alien 35(2,3), CaveFisher 35(4,8), Cryolophosaurus 26(4,7), Spyro 5(1,2) + vanilla defaults (`CP2:374-399`); ambient VelocityRaptor 1(2,4), Dragonfly 2(1,3), Camarasaurus 1(2,4), Baryonyx 2(4,8) (`:409-419`) | rat 30, worms 8/4/2, molenoid 5, creeping_horror 5, scorpion 3, cave_fisher 10(1-3), leonopteryx, firefly — mostly invented | full original roster + vanilla defaults (`mining_biome.json` spawners); inventions removed | |
| WGEN-017 | Village = vanilla defaults + Utopia ctor + `setVillageCreatures` additions: Robot1 25(4,8)…Robot5 20(4,8), GiantRobot 8(1,2), SpiderDriver 20(3,5), Godzilla 2(1,1), BandP 15(1,2), second girlfriend/boyfriend/cow/butterfly/moth/chipmunk/cockateil/firefly entries, Tshirt 2(1,1), Coin 2(1,1) | robots + giant_robot + band_p only; invented beaver; missing the inherited Utopia layer, spider_driver, godzilla | full three-layer roster (`village_biome.json`) | **Audit errors:** Jeffery IS `giant_robot` (JefferyEnable→GiantRobot `:289`) and Criminal IS `band_p` (CriminalEnable→BandP `:330`) — neither was missing. Duplicate same-type entries (e.g. girlfriend 5+1) kept as two entries, matching orig. |
| WGEN-020 | Islands roster `:142-199` (16 entries, e.g. CreepingHorror 60(4,8), CliffRacer 20(3,6), Dragon 1(1,2)) | weights matched but terrible_terror/ender_reaper double-counted via `dim_islands_locals` | weights verified; duplicates consolidated into `island_biome.json` | `island`/`island_too` weight-1 creature entries kept — they are the port's mechanism for the original `addIslands` floating-island entities (placement itself WGEN-021, Phase D). |
| WGEN-029 | Chaos roster `:334-516` (~55 entries) | missing bee/cassowary/dragonfly/peacock/stink_bug/ostrich/chipmunk/red+gold+enchanted cows; alosaurus group 2-3; invented vampire_butterfly; 9 locals duplicates doubling weights | full original roster (`chaos_biome.json`); alosaurus 1(1,1); inventions/duplicates removed | |

### Structures & dungeons (WGEN-022, 025, 026, 027, 034, 035, 039, 040, 043)

| ID | Original (citation → value) | Old port | New port | Notes |
|----|------------------------------|----------|----------|-------|
| WGEN-022 | Greenhouse (i==13), RobotLab (i==9), WhiteHouse (i==16) of the Islands D4 `nextInt(100)==0` + `nextInt(19)` rotation (`OreSpawnWorld.java:134-177`) → each 1/1900 | tagged `orespawn:village_biome`, 48/24, 44/22, 48/24 | tags → `orespawn:island_biome`, all spacing 44/22 (44²=1936 ≈ 1900) | |
| WGEN-025 | Weighted chest lists for FairyTree/maze/battle-tower chests (`Trees.java:19`, `GenericDungeon.java:32-36`, `OreSpawnWorld.java:1762`) | inline approximated ItemStack pickers | data-driven `loot_table/chests/crystal_chest`, `crystal_chest_maze`, `battle_tower_{rat,dungeon_beast,urchin,rotator,vortex}` referenced from `CrystalStructures` | Fixed-content chests (rotator station, urchin spawner: eggs + crystal coal) keep their explicit original item lists — those were never weighted. |
| WGEN-026 | Termite travel: every main-inventory slot empty ("Empty your inventory!") and no armor ("Take off your armor!"), checked only when heading TO DimensionID5 (`Termite.java:96-107`) | empty-hand check only | `EntityTermite.mobInteract` enforces empty inventory + offhand + armor with the original messages; return trip unchecked | |
| WGEN-027 | Crystal maze/towers placed solely by chunk-generator code | code path PLUS `crystal_maze` structure set spacing 1 (every chunk!) PLUS placed features; battle tower set 17/8 + feature; dangling crystal_tree*/crystal_flowers JSONs | all redundant structure_set/structure/placed/configured JSONs + tags deleted; `OreSpawnChunkGenerator`+`CrystalStructures` is the single mechanism | |
| WGEN-034 | Generic dungeon spawner: `nextInt(12)` ladder Scorpion/Alien/Cryolophosaurus/"WTF?"/Kyuubi/Bee/Cloud Shark/Lurking Terror/Terrible Terror/Rotator/Rat/Dungeon Beast (`GenericDungeon.java:141-177`); chest = `chestContentsList`, 5+nextInt(7) rolls (`:183`) | 11-mob invented pool; vanilla `simple_dungeon` loot | exact 12-mob ladder ("WTF?" → alien_boss) in `world/GenericDungeon.java:53-66`; `chests/generic_dungeon.json` (91 ported entries, rolls 5-11) | The level1-5 lists (`:57-61`) belong to the multi-floor castle dungeons (`fill_chests :727`), which are Phase D structures (WGEN-042) — not the cobble-box dungeon. |
| WGEN-035 | `addRubyDungeon`: 1/15 gate, 8 attempts scanning chunk+nextInt(8) from Y50 down to lava; called ONLY from Utopia (`OreSpawnWorld.java:49,1998-2012`); chest = `RubyBirdDungeon.java:18` list (CageEmpty/Ruby/Bacon/ButterCandy/full ruby kit/ThunderStaff), 4+nextInt(7) rolls | Crystal-dim only at fixed Y10-19; vanilla loot | Utopia-only lava-seek placement (`GenericDungeon.tryPlaceRubyDungeon`); `chests/ruby_dungeon.json` (14 entries, rolls 4-10) | **Audit note:** the "re-enable for overworld" fix guidance is wrong — `addRubyDungeon` has exactly one call site (Utopia, `:49`). |
| WGEN-039 | Mining rotation: `recently_placed==0 && nextInt(95)==1`, then `nextInt(7)` (`OreSpawnWorld.java:79-101`) → each structure ≈ 1/665 chunks | shadow/wtf/leon sets 32/16 (=1/1024, ~1.5× too rare) | spacing 26/13 (26²=676 ≈ 665) | Was UNVERIFIED — odds now extracted, sets corrected. (Audit's claimed "26/13 port value" was itself wrong; actual was 32/16.) |
| WGEN-040 | BeeHive: Mining rotation slot i==2 (`:88`), lowest-grass placement (`addBeeHive:2031-2057`) | overworld #is_forest/#is_jungle, set 32/16 | tag → `orespawn:mining_biome`, 26/13 (same 1/665 math) | The overworld forest skep remains the separate `small_beehive` structure — that one is original (`addANest` 50/50, see WGEN-041). |
| WGEN-041 | `addANest` (`OreSpawnWorld.java:999-1021`): `nextInt(230)==0` gate, Forest/ForestHills/Jungle/JungleHills/Birch biomes only, 50/50 SmallBeeHive vs MantisHive → each ≈ 1/460 eligible chunks | `small_beehive` + `mantis_nest` sets 21/10, forest/jungle tags | unchanged | VERIFIED-CORRECT — 21² = 441 ≈ 460 (within 4 %); biome tags cover the orig name list. |
| WGEN-043 | Challenge towers EXIST in 1.7.10: `makeEnormousCastle`/`makeEnormousCastleQ` (`GenericDungeon.java:191/6393`), placed by `addD4Castle` in the Islands rotation i<3, 50/50 King/Queen (`OreSpawnWorld.java:134-138, 2203-2228`) → per tower 1/100 × 3/19 × 1/2 = 1/1267 | tagged chaos_biome | tags → `orespawn:island_biome`; spacing 36/18 kept (36²=1296 ≈ 1267) | **Audit error** (see below). |

### Trees (WGEN-046, 047)

| ID | Original (citation → value) | Old port | New port | Notes |
|----|------------------------------|----------|----------|-------|
| WGEN-046 | `Trees.ScragglyTreeWithBranches` (`Trees.java:418`) has NO call sites (dead code); `Trees.SmallTree` (`:335`) is called only by the IslandToo entity (`IslandToo.java:196,419`) — there are no overworld decoration call sites | only Islands/Crystal/Chaos chunk-provider scraggly variants (correct) | unchanged | VERIFIED-CORRECT / **audit error** (see below). IslandToo's tree-planting behaviour → Phase D entity-behaviour owner. |
| WGEN-047 | `addOtherTrees` 1/30 gate × 50/50 wind-vs-sky, ≤4 wind / ≤3 sky per gated chunk (`OreSpawnWorld.java:2508-2547`); `addHugeTree` 1/50 gate, round-tree branch = rand 1..15 of 100 → 1/333 (`:1830-1874`); `addAppleTrees` gate 1/(15+freq), freq=dist-based 0..14, harmonic mean ≈ 1/21.2, howmany 2+nextInt(2+(15-freq)/2) (`:1792-1828`) | rarity 60 count 4 (wind), 60/3 (sky), 333 (round), 21/4 (apple) | unchanged | VERIFIED-CORRECT — all four match the folded math. |

### Portals (WGEN-049)

| ID | Original (citation → value) | Old port | New port | Notes |
|----|------------------------------|----------|----------|-------|
| WGEN-049 | `OreSpawnTeleporter.justPutMe:151-163` — on player teleport, all `EntityTameable` in a 48×24×48 box around the departure point that are owned by the player and not sitting are sent to the same destination | player only | `EntityAnt.mobInteract` collects `TamableAnimal` matches in the same box and runs each through the same `DimensionTransition` | |
| WGEN-050 | orig `PortalBlock.java` is an empty stub — no portal block ever generated or functional; all travel is entity-based | port-added `UtopiaPortalBlock` (creative-only, entityInside teleport) | unchanged | VERIFIED-CORRECT as a documented port addition — never world-generated, no recipe; see **PARITY_NOTES PN-011**. |

### Crystal spawn blocks (WGEN-023)

| ID | Original (citation → value) | Old port | New port | Notes |
|----|------------------------------|----------|----------|-------|
| WGEN-023 | 11 `OreGenericEgg` "egg ore" blocks (oreurchin … oreirukandji, `OreSpawnMain.java` BaseBlockID+250…) — gravel-type blocks that drop themselves plus a 50 % roll of 5+nextInt(3)+nextInt(3) XP (`OreGenericEgg.java`) | 9 of 11 were CrystalStone placeholders; 2 wrongly mapped to the mob-spawning CrystalRat/CrystalFairy OreBasicStones | 11 `OreGenericEgg` blocks registered with original textures/blockstates/loot/lang and returned from `OreSpawnChunkGenerator.getSpawnBlockStates` | **Audit error:** the audit's "break-to-spawn behaviour" claim is wrong — `OreGenericEgg` never spawned mobs (that's `OreBasicStone`); it drops XP. |

## Audit errors found (with proof)

1. **WGEN-017** — claimed Jeffery and Criminal are missing from Village. The
   port's `giant_robot` IS Jeffery (`JefferyEnable` gates `GiantRobot`,
   `BiomeGenUtopianPlains.java:288-290`) and `band_p` IS the Criminal entry
   (`CriminalEnable` gates `BandP.class`, `:329-331`).
2. **WGEN-023** — claimed "break-to-spawn behavior". `OreGenericEgg.java`
   drops 5..9 XP on a 50 % roll and never spawns mobs; mob-on-break is the
   unrelated `OreBasicStone` class.
3. **WGEN-031** — suspected custom Chaos sky/fog. `WorldProviderOreSpawn6`
   overrides no fog/sky members; visuals are vanilla.
4. **WGEN-035** — fix guidance says "re-enable for overworld/Utopia callers";
   `addRubyDungeon` has exactly one call site, Utopia (`OreSpawnWorld.java:49`).
5. **WGEN-039** — described the port sets as 26/13; they were actually 32/16
   (now corrected TO 26/13 per the extracted odds).
6. **WGEN-043** — claimed "no 1.7.10 counterpart found". The towers are
   `GenericDungeon.makeEnormousCastle` (line 191) / `makeEnormousCastleQ`
   (line 6393), world-placed by `addD4Castle` (`OreSpawnWorld.java:2203-2228`)
   in the **Islands** dimension — so the port content is original, only its
   chaos placement was wrong.
7. **WGEN-046** — claimed overworld call sites exist for SmallTree /
   ScragglyTreeWithBranches. `Trees.ScragglyTreeWithBranches` is dead code
   (zero callers) and `Trees.SmallTree` is only used by the IslandToo entity.

## PARTIAL findings and Phase D owners

| ID | Fixed half | Remainder → owner |
|----|------------|-------------------|
| WGEN-005 | reduction documented (PN-010) | ~105-type SpawnOres pool at 28+/chunk Y50-128 → Phase D spawn-block pool (with WGEN-042) |
| WGEN-014 | BeeHive restored to Mining (WGEN-040); rotation frequencies corrected (WGEN-039) | BasiliskMaze → WGEN-037; KyuubiDungeon, EnderKnightDungeon → WGEN-042 (Phase D structures) |
| WGEN-018 | mis-homed Islands structures removed from Village (WGEN-022) | DamselInDistress, SpiderHangout, RedAntHangout → WGEN-042 (Phase D structures) |
| WGEN-033 | End spawns verified present (`add_end_spawns.json`) | Hospital, EnderCastle → WGEN-042 (Phase D structures) |
| WGEN-036 | 400-tick timer verified faithful (orig `DungeonSpawnerBlock.java:39`) | 50-outcome table → blocked on WGEN-021/037/038/042 (Phase D structures) |

## Skipped MISSING findings (Phase D, no resolution line)

WGEN-003 (Block-of-Ruby gen), WGEN-004 (vanilla-ore boost), WGEN-007 (wild
strawberry/corn/tomato patches), WGEN-013 (Mining ambient extras), WGEN-015
(villages), WGEN-021 (~13 D4 structures), WGEN-037 (BasiliskMaze), WGEN-038
(NightmareDungeon), WGEN-042 (~25+ structures systemic), WGEN-044
(DuplicatorTree generator), WGEN-045 (ExperienceTree generator).

## Files changed

Java:
- `world/OreSpawnVeinPlacement.java` (new) — exact 1.7.10 ore-loop placement
- `world/ModWorldGen.java` — `vein_count` registration
- `world/feature/RubyLavaSeekFeature.java`, `AnthillFeature.java`,
  `UnstableAnthillFeature.java`, `VeggiePatchFeature.java` (new) +
  `ModFeatures.java` registrations
- `world/GenericDungeon.java` — rewritten (12-mob pool, loot keys, Utopia
  lava-seek ruby placement, Islands D4 dispatch, spawner-block entry points)
- `world/OreSpawnChunkGenerator.java` — dungeon dispatch + 11 egg
  `getSpawnBlockStates`
- `entity/EntityTermite.java` — empty-inventory/armor gate
- `entity/EntityAnt.java` — pet co-teleport
- `ModBlocks/ModItems/ModCreativeTabs` — 11 `OreGenericEgg` blocks
- `block/entity/RandomDungeonSpawnerBlockEntity.java` — new placement overloads

Datapack (high-level):
- `worldgen/placed_feature/` — all `ore_*`/troll veins rebuilt on `vein_count`;
  `_mining` variants; nether lavafoam/ruby; lapis boost; anthills; veggie
  patches; unstable anthill; crystal maze/tower/tree/flower JSONs deleted
- `worldgen/configured_feature/` — `ore_ruby*` → `ruby_lava_seek`; anthill/
  veggie configs; nether ores; lapis; pink tourmaline + crystal dups deleted
- `worldgen/noise_settings/islands.json`, `chaos.json` (new) + dimension/
  dimension_type wiring
- `worldgen/biome/` — five biome spawner rosters rebuilt; feature steps wired
  (ores, lakes, monster rooms, anthills, veggies, apple trees)
- `worldgen/structure_set/` — spacing fixes (shadow/wtf/leon/beehive 26/13,
  greenhouse/white_house 44/22, royal_altars 45/22), crystal sets deleted
- `tags/worldgen/biome/has_structure/` — re-tags (beehive→mining,
  greenhouse/robot_lab/white_house/challenge towers→islands)
- `neoforge/biome_modifier/` — `add_nether_ores`, `add_anthills`,
  `add_veggie_patches` (new); `add_crystal_dim_ores` trimmed; `dim_*_locals`
  deleted (consolidated into biome JSONs)
- `loot_table/chests/` — generic_dungeon, ruby_dungeon, crystal_chest(:maze),
  battle_tower_* (slice-C7 dungeon/crystal loot)
- `PARITY_NOTES.md` — PN-009/010/011

Tools (repeatable generators): `tools/gen_ore_features.py`,
`gen_surface_features.py`, `gen_spawn_rosters.py`, `fix_structure_sets.py`,
`gen_egg_block_assets.py`, `apply_c7_resolutions.py`.

## Build

`.\gradlew.bat build --console=plain` — **BUILD SUCCESSFUL** (2026-06-12).
