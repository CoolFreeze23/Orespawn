# 07 — Worldgen, Dimensions, Biomes & Structures Audit

ORIGINAL = `reference_1_7_10_source\sources\danger\orespawn\` (1.7.10, CFR-decompiled).
PORT = `src\main\java\danger\orespawn\` + `src\main\resources\data\orespawn\` (NeoForge 1.21.1).

---

## Overworld ore & plant gen

ORIGINAL config defaults from `OreSpawnMain.get_orestats(...)` (rate = veins/chunk, clump = vein size, Y window), executed by `ChunkOreGenerator.generateOresInChunk` and `OreSpawnWorld.generateSurface/generateRuby`. PORT = datapack ore features + `neoforge/biome_modifier/add_ores.json` (`#minecraft:is_overworld`, step `underground_ores`).

| Ore | ORIGINAL (rate/clump/Y) | PORT (count/size/Y) | Status |
|---|---|---|---|
| Ruby | 10 / 1 / Y0–50; placed adjacent to lava pockets (`OreSpawnWorld.generateRuby`, lava-seek loop) | count 8 / size 2 / Y −64..50, standard ore (`placed_feature/ore_ruby.json`, `configured_feature/ore_ruby.json`) | DIVERGENT — vein count 10→8, clump 1→2, lava-adjacency mechanic dropped, floor extended to −64 |
| Block of Ruby ore | 1 / 2 / Y0–15 (`OreSpawnMain` BlockRuby_stats) | none | MISSING |
| Uranium | 3 / 4 / Y0–30 | count 5 / size 4 / Y −64..30 (`ore_uranium.json`) | DIVERGENT — 3→5 veins, floor −64 (size & cap match) |
| Titanium | 3 / 4 / Y0–20 | count 6 / size 4 / Y −64..20 (`ore_titanium.json`) | DIVERGENT — 3→6 veins, floor −64 |
| Amethyst | 2 / 6 / Y0–25 | count 4 / size 6 / Y −64..25 (`ore_amethyst.json`) | DIVERGENT — 2→4 veins, floor −64 |
| Salt | 5 / 12 / Y50–128 | count 5 / size 12 / Y 50..128 (`ore_salt.json`) | PORTED — exact match |
| Extra Diamond (4/6/Y0–30), Diamond Block (2/4/Y0–20), Emerald (4/6/Y0–40), Emerald Block (2/4/Y0–20), Gold (4/8/Y0–40), Gold Block (2/4/Y0–25) | `ChunkOreGenerator.generateOresInChunk` adds these on top of vanilla | no orespawn features for vanilla-ore boosts; only vanilla defaults | MISSING |
| SpawnOres (mob-spawn blocks) | 28 veins/chunk clump 4 Y50–128, +30 veins on 1/20 roll; pool ≈105 block types (7 OreSpawn mob blocks + 98 vanilla-mob spawn blocks) — `OreSpawnMain` SpawnOres stats + `ChunkOreGenerator` | only `kraken_spawn_block` + `dragon_spawn_block`, each 1/24 chunks, Y −56..−10 (`placed_feature/dragon_spawn_block.json`, `kraken_spawn_block.json`, `add_boss_spawn_blocks.json`); plus `add_ancient_dried_eggs.json` (1/12, Y −32..32) | PARTIAL — ~105-type spawn-ore system reduced to 2 boss blocks + eggs; density 28+/chunk → 1/24 chunks |
| RedAntTroll | 4+rand(4) (avg 5.5) / clump ~4 / Y5–50 | count 6 / size 4 / Y 5..50 (`red_ant_troll.json`) | PORTED — count 5.5avg≈6, Y exact |
| TermiteTroll | 4+rand(4) / ~4 / Y5–50 | count 6 / size 4 / Y 5..50 (`termite_troll.json`) | PORTED |

Plant/entity decoration (ORIGINAL `OreSpawnWorld.generateSurface`):

| Feature | ORIGINAL | PORT | Status |
|---|---|---|---|
| AntHill blocks (black/red/rainbow/unstable ant blocks on surface) | ~4%/chunk in plains-type biomes (`OreSpawnWorld.addAnts`, redfreq param; called for overworld at line 323 with redfreq=4) | No anthill feature. Ant blocks registered (`ModBlocks.java:291-306`) but never world-placed. Black/red ants natural-spawn instead (`add_overworld_creatures.json`: ant w12 1-4, red_ant w8 1-3) | PARTIAL — blocks unobtainable in survival worldgen; rainbow/unstable ants do NOT spawn naturally (see Portals) |
| Strawberry plants (forest biomes) | random surface patches in forest biomes | no feature | MISSING |
| Corn plants (plains, ~1%) | `OreSpawnWorld` corn gen in plains biomes | crop blocks exist (`BlockCorn.java`), no wild gen | MISSING |
| Tomato plants (plains) | wild patches | no wild gen | MISSING |
| Butterflies/moths/mosquitos (worldgen entity drops in forest/jungle/swamp/river) | spawned during chunk decoration | converted to spawn entries: `swarm_butterfly__*`, `swarm_luna_moth__*`, `swarm_mosquito__*`, `swarm_firefly__*`, `swarm_dragonfly__*` biome modifiers | PORTED (mechanism changed: decoration→natural spawning; weights not 1:1 comparable) |
| Overworld generic dungeons | 1/16 chunk, Y5–44, 50-chunk cooldown, `DisableOverworldDungeons` config (`OreSpawnWorld.java:284`, `addGenericDungeon` :2014-2029) | 1/16 chunk, Y5–44 (`OreSpawnChunkGenerator.placeDungeons:728-734`), AtomicInteger 50-chunk cooldown (:107), `DISABLE_OVERWORLD_DUNGEONS` config (:697) | PORTED — chance/Y/cooldown/config all match (contents diverge, see Structures) |

---

## Dimensions

All six dimensions exist in PORT (`data/orespawn/dimension/*.json` + `dimension_type/*.json`), all driven by one chunk generator `orespawn:orespawn` dispatching on `dimension_style` (`OreSpawnChunkGenerator.java:74-171`, `DimensionStyle.java`). Original numeric IDs 80–85 are replaced by resource keys (correct for 1.21.1).

Entity-portal targets all wired (see Portals). Original `WorldProviderOreSpawn*.setWorldTime` only skipped night when all players slept (`WorldProviderOreSpawn.java:30-50`) — standard sleep behavior; port `bed_works` varies per dim (see below).

### Utopia (orig dim 80, `WorldProviderOreSpawn` + `ChunkProviderOreSpawn`)

| Aspect | ORIGINAL | PORT | Status |
|---|---|---|---|
| Terrain | overworld-like noise, caves/ravines, no monsters in `getPossibleCreatures` | `orespawn:inland` noise (constant continentalness, no oceans), style UTOPIA = pass-through (`dimension/utopia.json`; generator :161-169) | PORTED (adapted) |
| Monster spawns | none (list nullified) | `utopia_plains.json` `"monster": []` | PORTED — exact |
| Creature/ambient spawns | Gazelle 10(2-4), Girlfriend 5(2-3), Boyfriend 5(2-3), RedCow 10(4-8), GoldCow 8(2-6), EnchantedCow 5(2-4), Chipmunk 3(1-2), Cockateil 10(2-4), GoldFish 1, Coin 2, Cricket 5, Frog 5; ambient Firefly 15(3-6), Butterfly 20(3-6), LunaMoth 10(1-5); water Whale 1, Flounder 2 (`BiomeGenUtopianPlains` default ctor) | biome JSON: girlfriend 5(2-3), boyfriend 5(2-3), chipmunk 3(1-2), cockateil 10(2-4), gold_fish 1, coin 2, cricket 5(4-6), frog 5(4-6); firefly 15(3-6), butterfly 20(3-6), luna_moth 10(1-5); whale 1, flounder 2(2-4) + `dim_utopia_locals.json`: gazelle 10(2-4), red_cow 10(4-8), gold_cow 8(2-6), enchanted_apple_cow 5(2-4) | PORTED — weights match; cricket/frog group sizes 4-6 UNVERIFIED vs original |
| Features | huge trees, apple trees, other trees (Sky/Wind/etc.), King/Queen Altar, veggies, ruby+generic dungeons (`OreSpawnWorld.java:42-51`) | trees via biome modifiers (see Trees); `king_altar`/`queen_altar` + `royal_tree_*` structures tagged `orespawn:utopia_plains`; generic dungeon 1/16 | PARTIAL — veggie patches missing; altar frequency changed (orig 1/2000 roll per chunk after tree-passes, `OreSpawnWorld.java:2550`; port random_spread spacing 64/separation 32, `structure_set/royal_altars.json`) |
| Day cycle / beds | sleep-skip logic; respawn allowed | `bed_works: true`, full day cycle | PORTED |

### Mining (orig dim 81, `WorldProviderOreSpawn2` + `ChunkProviderOreSpawn2`)

| Aspect | ORIGINAL | PORT | Status |
|---|---|---|---|
| Terrain | Extreme Hills biome, overworld noise, lakes (water+lava), vanilla dungeons ×8/chunk, mineshafts, strongholds, scattered features | `orespawn:inland` noise, style MINING = pass-through; `mining_biome.json` has spring_water/spring_lava; no vanilla dungeon/mineshaft/stronghold sets for this biome | PARTIAL — caves/springs yes; mineshafts/strongholds/8×vanilla-dungeons missing |
| Ore density | `generateOresInChunk` called up to 3× when `LessOre==0` (triple OreSpawn ores) + multiple extra `generateRuby` calls + extra diamond/gold | `mining_biome.json` features: full vanilla ore suite + orespawn ruby/amethyst/uranium/titanium/salt at **1×** overworld rates | DIVERGENT — Mining dim was the 3× ore dimension; port gives identical density to overworld |
| Monster spawns | Alosaurus 8(1-2), TRex 6(1-2), Nastysaurus 6(1-2), Pointysaurus 10(4-8), GammaMetroid 35(4-7), Alien 35(2-3), CaveFisher 35(4-8), Cryolophosaurus 26(4-7), Spyro 5(1-2) + biome defaults (`ChunkProviderOreSpawn2.java:374-399`) | rat 30(4-8), cave_fisher 10(1-3), molenoid 5(1-2), worms 8/4/2, creeping_horror 5(1-3), scorpion 3(1-2) (`mining_biome.json`) | DIVERGENT — entire dino/alien/metroid roster replaced by cave-critter list; CaveFisher 35(4-8)→10(1-3) |
| Ambient spawns | VelocityRaptor 1(2-4), Dragonfly 2(1-3), Camarasaurus 1(2-4), Baryonyx 2(4-8) (`ChunkProviderOreSpawn2.java:410-419`) | firefly 5(1-3) only | MISSING (originals not present in this dim) |
| Structures | BasiliskMaze, KyuubiDungeon, BeeHive (`addBeeHive`, OreSpawnWorld:2031-2057), ShadowDungeon, AlienWTF, EnderKnight, LeonNest, generic dungeon fallback | `shadow_dungeon` (spacing 26/13), `wtf_alien_dungeon` (26/13), `leonopteryx_nest` (26/13) tagged `orespawn:mining_biome`; generic dungeon 1/16; BeeHive moved to overworld forests | PARTIAL — BasiliskMaze, KyuubiDungeon, EnderKnight dungeon missing; BeeHive relocated |

### Village Mania (orig dim 82, `WorldProviderOreSpawn3` + `ChunkProviderOreSpawn3`)

| Aspect | ORIGINAL | PORT | Status |
|---|---|---|---|
| Terrain | overworld noise + lakes + vanilla dungeons/mineshafts/strongholds | `orespawn:inland`, style VILLAGE = pass-through ("identical to DEFAULT for now", `DimensionStyle.java:50-52`) | PARTIAL |
| **Villages** | `MapGenMoreVillages`: spacing 9, separation 7 (`MapGenMoreVillages.java:11-12`; vanilla 32/8) — villages every ~9 chunks | **no village generation at all**: no `minecraft:villages` structure-set override, `orespawn:village_biome` not in any `has_structure/village` tag (no `data/minecraft/tags/worldgen` overrides exist) | MISSING — the dimension's namesake feature is absent |
| Mob spawns | Robot1-5, Jeffery, SpiderDriver, Godzilla, Girlfriend, Boyfriend, RedCow, GoldCow, EnchantedCow, Butterfly, LunaMoth, Chipmunk, Cockateil, Tshirt, Coin, Criminal (`BiomeGenUtopianPlains.setVillageCreatures`) | `village_biome.json`: robot_1 25(4-8), robot_2 16(2-8), robot_3 12(2-4), robot_4 8(1-2), robot_5 20(4-8), giant_robot 8(1-2), band_p 15(1-2); girlfriend/boyfriend/beaver; firefly/butterfly/luna_moth/chipmunk/cockateil + `dim_village_locals.json`: red_cow 8, gold_cow 6, enchanted_apple_cow 4, gazelle 10, tshirt 2, coin 2 | PARTIAL — Jeffery, SpiderDriver, Godzilla, Criminal missing; giant_robot/band_p added; robot weights UNVERIFIED vs original exact values |
| Structures | mosquitos, ants (redfreq 4), apple trees, generic dungeon, DamselInDistress, SpiderHangout, RedAntHangout (`OreSpawnWorld.java:118-128`) | `greenhouse` (48/24), `robot_lab` (44/22), `white_house` (48/24) structures tagged village_biome; generic dungeon | PARTIAL — Damsel/SpiderHangout/RedAntHangout missing; Greenhouse/RobotLab/WhiteHouse were originally Islands-dim (D4) structures relocated to Village |

### Islands (orig dim 83, `WorldProviderOreSpawn4` + `ChunkProviderOreSpawn4`)

| Aspect | ORIGINAL | PORT | Status |
|---|---|---|---|
| Terrain | flat plane: bedrock y0 + dirt + grass; islands built dynamically by Island/IslandToo **entities** rising and printing terrain (`Island.java:64-79`: small radius 3-6/depth 2-4, 1/40 large radius 6-10/depth 3-6) | `minecraft:floating_islands` noise (`dimension/islands.json`), style ISLANDS; Island/IslandToo entities spawn (weight 1 each, `dim_islands_locals.json`) | DIVERGENT — static sky-island noise replaces flat plane; island-builder entities retained on top |
| Scraggly trees | `ChunkProviderOreSpawn4.addScragglyTrees`, 1–10 attempts/chunk on air-over-grass | 1+rand(10) attempts, air-over-grass, 4-7 trunk + 3×3 cap (`OreSpawnChunkGenerator.applyIslandsSurface:198-216`) | PORTED |
| Mob spawns | Butterfly, Cockateil, LunaMoth, Firefly, Dragon, Stinky, CliffRacer, CloudShark, GoldFish, CreepingHorror, TerribleTerror, LurkingTerror, PitchBlack, LeafMonster, EnderReaper, HerculesBeetle (`setIslandCreatures`) | `island_biome.json` monsters: creeping_horror 60(4-8), terrible_terror 25(3-6), lurking_terror 1, pitch_black 15(3-6), leaf_monster 35(2-4), ender_reaper 25(2-4), hercules_beetle 5(1-2); creatures dragon 1(1-2), gold_fish 5(2-4); ambient butterfly/cockateil/luna_moth/firefly + locals: island, island_too, stinky 2, cliff_racer 20(3-6), cloud_shark 1 | PORTED — full roster present; weights UNVERIFIED vs original exact numbers |
| D4 structures | D4Castle, D4GenericDungeon, D4EnderCastle, D4IncaPyramid, D4RobotLab, D4Mini, D4RubyDungeon, D4CephadromeAltar, D4Greenhouse, D4NightmareRookery, D4StinkyHouse, D4WhiteHouse, Pumpkin, D4Rainbow, D4CloudShark, UnstableAnts (`OreSpawnWorld.java:134-198`) | none tagged `island_biome` (RobotLab/Greenhouse/WhiteHouse moved to Village) | MISSING — ~13 island structures absent; unstable-ant placement absent |

### Crystal (orig dim 84, `WorldProviderOreSpawn5` + `ChunkProviderOreSpawn5`)

| Aspect | ORIGINAL | PORT | Status |
|---|---|---|---|
| Terrain palette | custom noise: CrystalStone base, CrystalGrass surface y59-64, water y63, bedrock y0 | vanilla inland noise + full rewrite to CrystalStone/CrystalGrass + shallow-water fill ≥Y56 (`OreSpawnChunkGenerator.replaceTerrain:295-337`, `fillShallowWater:345-377`) | PORTED (adapted) |
| CrystalMaze | every chunk at Y25: 4×4 cells ×4 blocks, 3 high, bedrock walls/floor/ceiling, 4 ceiling + 1 floor CrystalStone holes (`CrystalMaze.java` orig) | every chunk at Y25, identical Prim's-maze, bedrock, 4+1 CrystalStone holes (`world/CrystalMaze.java:30-77`) | PORTED — exact |
| Crystal trees | `addCrystalTrees`: 1/5 chunk, type roll 1/5, 0-7 trees, tall (10-22 trunk) + scraggly | `CrystalTreeGenerator.generate:21-43`: 1/5 chunk, `nextInt(5)` type, `nextInt(8)` count, tall 10+rand(12) | PORTED |
| Pink Tourmaline columns | 1/30 chunk, 1-10 columns from Y30-35 | 1/30, 1-10 patches, Y30-35 (`generatePinkTourmaline:395-431`) | PORTED |
| Tiger's Eye columns | 1/30 chunk, 1-5 columns Y5-10 | 1/30, 1-5, Y5-10 (`generateTigersEye:437-468`) | PORTED |
| Crystal "ores" (spawn blocks) | 25+rand(30) patches/chunk (+30 on 1/20), Y>45, pool of 11 spawn blocks (Urchin, Flounder, Skate, Rotator, Peacock, Fairy, DungeonBeast, Vortex, Rat, Whale, Irukandji); CrystalCoal 3+rand(8); CrystalRat 15+rand(20) Y<25; CrystalFairy 12+rand(20) Y<25 | identical counts/Y-gates (`generateCrystalOres:474-519`) BUT 9 of 11 spawn-block types are CRYSTAL_STONE placeholders — only CRYSTAL_FAIRY & CRYSTAL_RAT real (`getSpawnBlockStates:527-542`) | PARTIAL — frequencies exact, 9/11 block types not yet implemented |
| Flowers/rice/quinoa/termites | flowers 1/3 chunk 1-13; rice 1/10 ×5; quinoa 1/20 ×5; termite blocks 1/40 ×3 | identical (`placeCrystalFlowers:598`, `placeRice:628`, `placeQuinoa:651`, `placeCrystalTermites:674`) | PORTED — exact |
| Extra datapack ores | n/a (Kyanite **is** CrystalStone, Pink Tourmaline/TigersEye are the column formations) | `add_crystal_dim_ores.json` additionally injects `ore_kyanite` (6×size6, Y−32..80) and `ore_pink_tourmaline` (6×size6) as standard veins | DIVERGENT — new ore veins with no 1.7.10 counterpart (double-generation of tourmaline) |
| Mob spawns | monsters Rotator 4(1-2), Vortex 3(1-2), Urchin 15(2-4), DungeonBeast 30(4-6), Rat 40(4-6); CrystalCow, Fairy 10(4-8), Peacock 5(4-8), Mantis; water Whale/Crab/Flounder/Irukandji/Skate/Frog (`setCrystalCreatures`) | `crystal_plains.json`: rotator 4(1-2), vortex 3(1-2), urchin 15(2-4), dungeon_beast 30(4-6), rat 40(4-6), crab 1(1-2), mantis 1; water whale 1(1-2), flounder 5(6-8), irukandji 4(2-3), skate 2(3-6), frog 1(3-5) + `dim_crystal_locals.json`: fairy 10(4-8), peacock 5(4-8), crystal_cow 1(1-4), ruby_bird 6(2-4) | PORTED — monster weights exact; ruby_bird added (not in original list) |
| Structures | FairyTree 1/5 (else Termites/RotatorStation 1/150/Urchin 1/180/HauntedHouse 1/230/RoundRotator 1/150/BattleTower 1/280), Irukandji 1/80, maze chests & spawners, rocks 1/4 | identical chances (`CrystalStructures.java:44-96` + per-method gates :106,367,403,462,534,594,685,706) with 50-chunk AtomicInteger cooldown | PORTED — frequencies exact; chest loot approximated (inline ItemStack pickers, `fillCrystalChest:838+`) vs original WeightedRandomChestContent lists → loot DIVERGENT in detail |
| Entry requirement | via Termite, empty hand AND **empty inventory** (`Termite.java` orig) | empty hand only (inherits `EntityAnt.mobInteract:103-134`; `EntityTermite.java` overrides nothing) | DIVERGENT — empty-inventory rule dropped |
| Redundant structure JSONs | n/a | `crystal_maze` structure_set (spacing 1/0) + `crystal_battle_tower` set (17/8) + placed_features `crystal_maze.json` (1/4), `crystal_battle_tower.json` (1/220), `crystal_tree*.json` exist **in addition to** the chunk-generator code paths | DIVERGENT — risk of double generation (maze: every chunk via code + 1/4 via feature + structure set) |

### Chaos (orig dim 85, `WorldProviderOreSpawn6` + `ChunkProviderOreSpawn6`)

| Aspect | ORIGINAL | PORT | Status |
|---|---|---|---|
| Terrain | nether-noise terrain, 128 high, stone base (nether-style caverns), scraggly trees | `orespawn:inland` overworld-style noise, style CHAOS = pass-through ("future: hellish flora", `DimensionStyle.java:48-49`); no scraggly trees for CHAOS | DIVERGENT — completely different terrain algorithm |
| Ores | `generateOresInChunk` (full OreSpawn ore suite) | `chaos_biome.json` features: vanilla ores + ruby/amethyst/uranium/titanium/salt | PORTED (1× density) |
| Mob spawns | `setChaosCreatures`: ~55 entries (Butterfly, Moth, Cockateil, Firefly, CliffRacer, CloudShark, GoldFish, Fairy, Baryonyx, Bee, Cassowary, Dragonfly, Peacock, StinkBug, Ostrich, Chipmunk, Beaver, cows, Vortex, PitchBlack, TerribleTerror, Alosaurus, Basilisk, Robot1-5, CaterKiller, CaveFisher, CreepingHorror, Cryo, Urchin, DungeonBeast, EmperorScorpion, EnderKnight, EnderReaper, Hammerhead, Hercules, TrooperBug, Molenoid, Mothra, Brutalfly, Rat, Rotator, Scorpion, SpitBug, Nastysaurus, TRex, LeafMonster, Pointysaurus, Leon, Mantis, LurkingTerror, GammaMetroid) | `chaos_biome.json` 37 monsters + beaver/baryonyx + 4 ambient + `dim_chaos_locals.json` (ghost 15, ghost_skelly 10, mothra 1, cliff_racer 30, cloud_shark 2, gold_fish 10, fairy 5, vampire_butterfly 18) | PARTIAL — most roster present; Bee, Cassowary, Dragonfly, Peacock, StinkBug, Ostrich, cows, Hydrolisc missing; ghosts/vampire_butterfly added; per-entry weights UNVERIFIED vs original |
| Features | butterflies/moths, veggies, ants ×2 (`OreSpawnWorld.java:103-107`) | generic dungeon 1/16; `challenge_tower_king/queen` structures (spacing 36/18) tagged chaos_biome (new) | PARTIAL — veggies/ants missing; challenge towers added |
| Sky | `ambient_light` n/a (1.7.10 custom fog/sky in provider — exact colors UNVERIFIED) | `ambient_light: 0.3`, `has_raids: true` | UNVERIFIED — original sky/fog constants not extracted |

### Nether / End additions

| Feature | ORIGINAL | PORT | Status |
|---|---|---|---|
| Nether: Lavafoam ore, ruby, nether ants, nether mosquitos (`OreSpawnWorld.generateNether`) | yes | `add_nether_spawns.json` exists (spawns); no lavafoam/ruby nether features found | PARTIAL |
| End: End ants, End knights/reapers, Hospital, Ender Castle (`OreSpawnWorld.generateEnd`) | yes | `add_end_spawns.json` (spawns only); no Hospital/EnderCastle structures | PARTIAL — structures missing |

---

## Structures & dungeons

| Structure | ORIGINAL | PORT | Status |
|---|---|---|---|
| Generic Dungeon | 1/16 chunk Y5-44; 12×6×12 cobble/mossy shell; spawner pool of 12: Scorpion, Alien, Cryolophosaurus, WTF?, Kyuubi, Bee, CloudShark, LurkingTerror, TerribleTerror, Rotator, Rat, DungeonBeast; custom level1-5 chest lists (`GenericDungeon.makeDungeon` + lists) | same shell/chance/Y (`world/GenericDungeon.java:36-67`); spawner pool of 11: Alien, CaveFisher, DungeonBeast, Scorpion, EmperorScorpion, TrooperBug, CaterKiller, Molenoid, Basilisk, StinkBug, Triffid (:22-34); chest = vanilla `simple_dungeon` loot table (:121-126) | DIVERGENT — spawner pool swapped (Kyuubi/Bee/CloudShark/Cryo/WTF?/Rotator/Rat/Terrors out; EmperorScorpion/Basilisk/Triffid etc. in); custom loot replaced by vanilla table |
| Ruby (RubyBird) Dungeon | 1/15 chunk, placed at lava contact Y5-50 (`OreSpawnWorld.addRubyDungeon:1998-2012`); 10×5×10; RubyBird spawner; chest: CageEmpty/Ruby/Bacon/ButterCandy/full ruby tool+armor set/ThunderStaff (`RubyBirdDungeon.java` chestContentsList) | Crystal dim only, 1/15 chunk Y10-19 (`OreSpawnChunkGenerator:717-726`); 10×5×10, walls 2/3 ruby ore (`GenericDungeon.tryPlaceRubyDungeon:69-98`); RubyBird spawner ✓; chest = vanilla `simple_dungeon` | DIVERGENT — moved from lava-seek (any dim it was called) to Crystal-only at fixed Y band; ruby-gear loot table missing |
| DungeonSpawnerBlock | on tick, spawns 1 of **50** structures (FairyTree → RedAntHangout list in `DungeonSpawnerBlock.java`) | `RandomDungeonSpawnerBlockEntity.java:63-72`: 200-tick countdown then 1-in-4 ruby else generic dungeon — **2** outcomes | PARTIAL — 50→2 structure pool |
| BasiliskMaze | maze + Basilisk spawner + chest (diamond 15-25, gold 4-16, iron 2-20, CagedGirlfriend, uranium, titanium, fish, corn dog) in Mining dim (`BasiliskMaze.java`) | not present (no code/JSON match for basilisk maze) | MISSING |
| NightmareDungeon | 25×12×25, RTPBlock floor, EmperorScorpion-or-Nightmare spawner, 2 chests of Ultimate/Experience/Amethyst gear + Bertha/Slice (`NightmareDungeon.java`); triggered via RTP mechanic | no NightmareDungeon (only `NightmareSword.java` item) | MISSING |
| CrystalMaze | every Crystal chunk (see Crystal dim) | ported exactly | PORTED |
| Crystal structures (FairyTree/RotatorStation/Urchin/HauntedHouse/RoundRotator/BattleTower) | chances 1/5, 1/150, 1/180, 1/230, 1/150, 1/280 | identical (`CrystalStructures.java`) | PORTED (loot approximated) |
| ShadowDungeon | Mining dim, `addShadowDungeon` w/ cooldown | structure `shadow_dungeon`, set spacing 26/sep 13, mining_biome, dungeon_type SHADOW (`LegacyDungeonPiece.java:72`) | PORTED (frequency mechanism changed: per-chunk roll → random_spread; exact orig roll UNVERIFIED) |
| AlienWTF Dungeon | Mining dim | `wtf_alien_dungeon` set 26/13, ALIEN_WTF(20,25,6) | PORTED (same caveat) |
| LeonNest | Mining dim `addLeonNest` | `leonopteryx_nest` set 26/13 | PORTED (same caveat) |
| BeeHive / SmallBeeHive | Mining dim, lowest-grass-spot algorithm (`OreSpawnWorld.addBeeHive:2031-2057`) | overworld `#minecraft:is_forest`/`is_jungle`, set 24/12 + placed_feature 1/60 (`structure_set/beehive.json`, `BeehiveFeature.java`) | DIVERGENT — moved Mining→overworld forests |
| MantisNest | overworld/dim placement via dungeon spawner | overworld forests/jungles, set 24/12 + feature 1/80 | PORTED (placement basis UNVERIFIED vs orig) |
| King/Queen Altar | Utopia, 1/2000 chunk roll, 50/50 king/queen, cooldown 100 (`OreSpawnWorld.addKingAltar:2549-2571`) | structures `king_altar`/`queen_altar`, shared set spacing 64/sep 32, utopia_plains; byte-level port of makeKingAltar/makeQueenAltar incl. portrait pixel arrays (`LegacyDungeonPiece#generateRoyalAltar`, noted at `OreSpawnChunkGenerator.java:269-283`) | PORTED — frequency mechanism differs (1/2000 roll ≈ random_spread 64/32 order-of-magnitude similar) |
| Greenhouse / RobotLab / WhiteHouse | Islands (D4) structures | Village dim structure sets 48/24, 44/22, 48/24 | DIVERGENT — relocated Islands→Village |
| D4Castle, EnderCastle, IncaPyramid, Mini, CephadromeAltar, NightmareRookery, StinkyHouse, Rainbow, CloudShark dungeon, Pumpkin, BouncyCastle, MonsterIsland, GirlfriendIsland, PlayPool, WaterDragonLair, GoldFishBowl, Graveyard, SpitBugLair, Igloo, KyuubiDungeon, EnderKnightDungeon, Hospital, DamselInDistress, SpiderHangout, RedAntHangout, FrogPond, RubberDuckyPond, QueenAltar(D4), EnormousCastle(Q) | placed by OreSpawnWorld/D4 hooks & DungeonSpawnerBlock (loot lists in `GenericDungeon.java`) | absent (only 17 structure JSONs + 2 dungeon code paths exist) | MISSING — ≈25+ structure types |
| Challenge Towers (KING_TOWER/QUEEN_TOWER 40,4,95) | n/a in 1.7.10 (1.12.2-era?) | chaos_biome sets 36/18 | UNVERIFIED vs 1.7.10 (no original counterpart found — possible new/1.12.2 content) |

---

## Trees

| Tree | ORIGINAL trigger/size | PORT | Status |
|---|---|---|---|
| FairyTree / FairyCastleTree | Crystal dim 1/5 chunk, castle variant 1/5; trunk 5+rand(5), castle platforms 3+rand(3) (`Trees.FairyTree/FairyCastleTree`) | `CrystalStructures.tryPlaceFairyTree:102-143`, grow 5+rand(5), castle j=3+rand(3) (:274-324) | PORTED |
| Crystal trees (tall/scraggly) | `ChunkProviderOreSpawn5.addCrystalTrees` | `CrystalTreeGenerator` (1/5 chunk, identical rolls) | PORTED |
| SkyTree | Utopia via `OreSpawnWorld.addOtherTrees` (roll values in :2508+) | `SkyTreeFeature` + `sky_tree.json` 1/25... rarity 60, utopia_plains (`add_sky_tree.json`) | PORTED — frequency mapping UNVERIFIED (orig per-chunk roll not extracted) |
| WindTree | Utopia `addOtherTrees` | `wind_tree.json` rarity 60, utopia_plains | PORTED (same caveat) |
| RoundTree (Giant Oak) | Utopia `addHugeTree` (tree_type rolls, `OreSpawnWorld.java:1830-1863`) | `round_tree.json` rarity 333, utopia_plains | PORTED (frequency UNVERIFIED) |
| Magic Apple trees | Utopia `addAppleTrees` (`OreSpawnWorld.java:1792`) | `magic_apple_tree.json` rarity 25, utopia_plains | PORTED (frequency UNVERIFIED) |
| Scraggly trees (Islands) | 1-10/chunk | ported in generator | PORTED |
| DuplicatorTree | `Trees.DuplicatorTree` (sapling/worldgen) | no feature/code | MISSING |
| ExperienceTree | `Trees.ExperienceTree` | no feature/code | MISSING |
| SmallTree / ScragglyTreeWithBranches (overworld variants) | `Trees.java` | only Islands/Crystal scraggly variants | PARTIAL |

---

## Portals & teleporters

Original has no portal blocks (`PortalBlock.java` is empty); travel is right-click on entities, using `OreSpawnTeleporter.justPutMe` (scan Y1-180 for solid ground + 3 air; teleports tamed pets too).

| Portal | ORIGINAL | PORT | Status |
|---|---|---|---|
| Black Ant → Utopia (toggle ↔ Overworld), empty hand | `EntityAnt.java` | `EntityAnt.mobInteract:103-134`, target UTOPIA (:80), empty-hand check (:106), toggle (:111-113) | PORTED |
| Red Ant → Mining | `EntityRedAnt.java` | `EntityRedAnt.java:28` returns MINING | PORTED |
| Rainbow Ant → Village | `EntityRainbowAnt.java` | `EntityRainbowAnt.java:20` returns VILLAGE | PORTED (code) — but rainbow ants have **no natural spawn** and rainbow_ant_block is never world-generated → Village unreachable in survival | PARTIAL overall |
| Unstable Ant → Islands | `EntityUnstableAnt.java` | `EntityUnstableAnt.java:20` returns ISLANDS | PORTED (code) — same reachability problem (no spawn entry, no worldgen block) | PARTIAL overall |
| Termite → Crystal, empty hand + **empty inventory** | `Termite.java` | `EntityTermite.java:46-48` returns CRYSTAL; inherits empty-hand-only check | DIVERGENT — inventory-must-be-empty requirement dropped |
| Butterfly → Chaos | `EntityButterfly.java` | `EntityButterfly.java:42` CHAOS key | PORTED |
| Landing logic | scan Y1-180 upward for safe spot; brings tamed pets | `EntityAnt.findSafeY:142-162` top-down scan 256→min for solid + 2 air, fallback Y64; no pet co-teleport | PARTIAL — pets left behind |
| Utopia Portal Block (step-on) | n/a in 1.7.10 | `UtopiaPortalBlock.java` (entityInside, fixed y=max(min+1,64)) | new addition (no original counterpart) — UNVERIFIED intent |

---

## Villages

| Aspect | ORIGINAL | PORT | Status |
|---|---|---|---|
| MapGenMoreVillages frequency | spacing 9 / min separation 7 (`MapGenMoreVillages.java:11-12`) vs vanilla 32/8 → ~12× denser villages, enabled in dim 82 (`ChunkProviderOreSpawn3`) | nothing: no `minecraft:villages` structure-set override, village_biome not added to `#minecraft:village` has_structure tags, `DimensionStyle.VILLAGE` is explicitly a no-op placeholder (`DimensionStyle.java:50-52`) | MISSING |
| Village biome registration (`BiomeManager.addVillageBiome` in `WorldProviderOreSpawn3`) | yes | n/a mechanism; not replicated | MISSING |

---

## Summary

Counts (table rows above): **PORTED 30 · PARTIAL 16 · MISSING 17 · DIVERGENT 15 · UNVERIFIED 3** (rows with dual labels counted by their headline status; sub-caveats noted inline).

Strongest areas: Crystal dimension (terrain, maze, trees, flora, structure frequencies match 1:1), entity-portal wiring, overworld dungeon gate (1/16, Y5-44, 50-cooldown, config), salt/troll ores, Utopia spawn lists.

Weakest areas: Village dimension has no villages; Islands lost its ~13 unique structures; DungeonSpawnerBlock pool 50→2; dungeon loot tables replaced with vanilla `simple_dungeon`; Mining lost its 3× ore density and dino/alien spawn roster; ~25 structure types missing overall; Village/Islands unreachable in survival (no rainbow/unstable ant spawns or blocks).
