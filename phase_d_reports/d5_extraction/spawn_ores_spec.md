# SpawnOres System + Water-Bucket Egg Recipes — Extraction Spec (1.7.10 → NeoForge 1.21.1)

Covers audit findings **WGEN-005** (the ~105-type SpawnOres worldgen pool) and the
**ITEM-062 remainder** (the 116 water-bucket spawn-block→spawn-egg conversion recipes,
OreSpawnMain.java:2665-3021).

Source of truth: `reference_1_7_10_source/sources/danger/orespawn/` —
`OreSpawnMain.java` (block fields :534-652, registration :1981-2099, recipes :2665-3021,
ore stats :1579), `OreGenericEgg.java` (44 lines), `ChunkOreGenerator.java` (652 lines),
`OreSpawnWorld.java` (:32-224, :349-804), `ChunkProviderOreSpawn5.java` (:572-659).

Citation convention: `OSM:NN` = OreSpawnMain.java, `OGE:NN` = OreGenericEgg.java,
`COG:NN` = ChunkOreGenerator.java, `OSW:NN` = OreSpawnWorld.java,
`CP:NN`/`CP2:NN`/`CP3:NN`/`CP5:NN`/`CP6:NN` = ChunkProviderOreSpawn[N].java.
Port citations: `MB:NN` = src/main/java/danger/orespawn/ModBlocks.java,
`MI:NN` = ModItems.java, `POGE:NN` = src/main/java/danger/orespawn/block/OreGenericEgg.java,
`ADE:NN` = block/AncientDriedEggBlock.java, `OSCG:NN` = world/OreSpawnChunkGenerator.java.

---

## 1. Entry points / system overview

| Path | Caller | What it does |
|---|---|---|
| Overworld (dim 0) worldgen | `OreSpawnWorld.generate` case 0 (OSW:214-216) → `generateOres(world, random, chunkX*16, chunkZ*16)` (OSW:349) | IWorldGenerator registered with weight 10 (OSM:5375) — runs during chunk population after vanilla ore gen |
| OreSpawn dimension chunk build | `OreSpawnMain.Chunker.generateOresInChunk(...)` (COG:15) called from CP:176, CP2:191-195, CP3:179, CP6:197 | direct chunk writes during **raw terrain build**, before any decoration |
| Crystal dimension (separate pool) | `ChunkProviderOreSpawn5.generateCrystalOres` (CP5:572) | 11-entry egg-ore pool in CrystalStone — already extracted as WGEN-023, ported at OSCG:470-539 |
| Crafting | 116 shapeless water-bucket recipes + 3 part-combine recipes, OSM:2665-3021 | spawn-ore block + water bucket → 1 spawn egg (§6) |

`OreSpawnMain.Chunker` is a single shared `ChunkOreGenerator` instance: declared OSM:533,
constructed OSM:5787. There is **no ground scan and no DungeonSpawnerBlock trigger** for this
system — it is pure ore-vein worldgen plus crafting (DungeonSpawnerBlock types 0-49 all build
structures; none place spawn ores).

---

## 2. THE POOL — master table (all 119 spawn-ore blocks + registration + recipes + port mapping)

Every spawn-ore block is a `new OreGenericEgg(BaseBlockID + n)` (ctor arg is a dead 1.6-era
block-ID, unused — OGE:16; `BaseBlockID` default 2700, OSM:1464). Unlocalized name is set with
`func_149663_c` (= the texture key, see §7), registry name via
`GameRegistry.registerBlock(block, "OreSpawn_XxxSpawnBlock")`.

**Pool slot legend** — `c N`: case N of the overworld/dims common pool `random.nextInt(98)`
switch (OSW:406-801 / COG:72-467, identical order); `R N`: case N of the rare pool
`random.nextInt(7)` switch (OSW:371-402 / COG:37-68); `C N`: case N of the Crystal-dim
`random.nextInt(11)` switch (CP5:586-633); `—`: **generates nowhere**.

**Recipe** — line of the `GameRegistry.addShapelessRecipe` call (OSM); all 116 water recipes
are `shapeless{ water bucket + spawn block } → output` (§6). `9×part` = the block is the
*output* of a nine-part combine recipe instead.

**Port egg item** — `MI:NN` = existing `ModItems` spawn-egg field; `vanilla:` = 1.21.1 vanilla
spawn egg; **NONE** = no port item exists (flagged in §9).

| # | Field (decl OSM:) | unloc/texture (ctor OSM:) | Registry name (reg OSM:) | Mob (en_US name at recipe site) | Pool slot | Recipe OSM: | 1.7.10 egg output | Port block | Port egg item |
|---|---|---|---|---|---|---|---|---|---|
| 1 | MySpiderSpawnBlock :534 | orespider :6236 | OreSpawn_SpiderSpawnBlock :1981 | Spider | c 0 | 2667 | vanilla spawn_egg meta 52 | MISSING-IN-PORT | vanilla:spider_spawn_egg |
| 2 | MyBatSpawnBlock :535 | orebat :6237 | OreSpawn_BatSpawnBlock :1982 | Bat | c 1 | 2670 | spawn_egg meta 65 | MISSING-IN-PORT | vanilla:bat_spawn_egg |
| 3 | MyCowSpawnBlock :536 | orecow :6238 | OreSpawn_CowSpawnBlock :1983 | Cow | c 2 | 2673 | spawn_egg meta 92 | MISSING-IN-PORT | vanilla:cow_spawn_egg |
| 4 | MyPigSpawnBlock :537 | orepig :6239 | OreSpawn_PigSpawnBlock :1984 | Pig | c 3 | 2676 | spawn_egg meta 90 | MISSING-IN-PORT | vanilla:pig_spawn_egg |
| 5 | MySquidSpawnBlock :538 | oresquid :6240 | OreSpawn_SquidSpawnBlock :1985 | Squid | c 4 | 2679 | spawn_egg meta 94 | MISSING-IN-PORT | vanilla:squid_spawn_egg |
| 6 | MyChickenSpawnBlock :539 | orechicken :6241 | OreSpawn_ChickenSpawnBlock :1986 | Chicken | c 5 | 2682 | spawn_egg meta 93 | MISSING-IN-PORT | vanilla:chicken_spawn_egg |
| 7 | MyCreeperSpawnBlock :540 | orecreeper :6242 | OreSpawn_CreeperSpawnBlock :1987 | Creeper | c 6 | 2685 | spawn_egg meta 50 | MISSING-IN-PORT | vanilla:creeper_spawn_egg |
| 8 | MySkeletonSpawnBlock :541 | oreskeleton :6243 | OreSpawn_SkeletonSpawnBlock :1988 | Skeleton | c 7 | 2688 | spawn_egg meta 51 | MISSING-IN-PORT | vanilla:skeleton_spawn_egg |
| 9 | MyZombieSpawnBlock :542 | orezombie :6244 | OreSpawn_ZombieSpawnBlock :1989 | Zombie | c 8 | 2691 | spawn_egg meta 54 | MISSING-IN-PORT | vanilla:zombie_spawn_egg |
| 10 | MySlimeSpawnBlock :543 | oreslime :6245 | OreSpawn_SlimeSpawnBlock :1990 | Slime | c 9 | 2694 | spawn_egg meta 55 | MISSING-IN-PORT | vanilla:slime_spawn_egg |
| 11 | MyGhastSpawnBlock :544 | oreghast :6246 | OreSpawn_GhastSpawnBlock :1991 | Ghast | c 10 | 2697 | spawn_egg meta 56 | MISSING-IN-PORT | vanilla:ghast_spawn_egg |
| 12 | MyZombiePigmanSpawnBlock :545 | orezombiepigman :6247 | OreSpawn_ZombiePigmanSpawnBlock :1992 | Zombie Pigman | c 11 | 2700 | spawn_egg meta 57 | MISSING-IN-PORT | vanilla:zombified_piglin_spawn_egg |
| 13 | MyEndermanSpawnBlock :546 | oreenderman :6248 | OreSpawn_EndermanSpawnBlock :1993 | Enderman | c 12 | 2703 | spawn_egg meta 58 | MISSING-IN-PORT | vanilla:enderman_spawn_egg |
| 14 | MyCaveSpiderSpawnBlock :547 | orecavespider :6249 | OreSpawn_CaveSpiderSpawnBlock :1994 | Cave Spider | c 13 | 2706 | spawn_egg meta 59 | MISSING-IN-PORT | vanilla:cave_spider_spawn_egg |
| 15 | MySilverfishSpawnBlock :548 | oresilverfish :6250 | OreSpawn_SilverfishSpawnBlock :1995 | Silverfish | c 14 | 2709 | spawn_egg meta 60 | MISSING-IN-PORT | vanilla:silverfish_spawn_egg |
| 16 | MyMagmaCubeSpawnBlock :549 | oremagmacube :6251 | OreSpawn_MagmaCubeSpawnBlock :1996 | Magma Cube | c 15 | 2712 | spawn_egg meta 62 | MISSING-IN-PORT | vanilla:magma_cube_spawn_egg |
| 17 | MyWitchSpawnBlock :550 | orewitch :6252 | OreSpawn_WitchSpawnBlock :1997 | Witch | c 16 | 2715 | spawn_egg meta 66 | MISSING-IN-PORT | vanilla:witch_spawn_egg |
| 18 | MySheepSpawnBlock :551 | oresheep :6253 | OreSpawn_SheepSpawnBlock :1998 | Sheep | c 17 | 2718 | spawn_egg meta 91 | MISSING-IN-PORT | vanilla:sheep_spawn_egg |
| 19 | MyWolfSpawnBlock :552 | orewolf :6254 | OreSpawn_WolfSpawnBlock :1999 | Wolf | c 18 | 2721 | spawn_egg meta 95 | MISSING-IN-PORT | vanilla:wolf_spawn_egg |
| 20 | MyMooshroomSpawnBlock :553 | oremooshroom :6255 | OreSpawn_MooshroomSpawnBlock :2000 | Mooshroom | c 19 | 2724 | spawn_egg meta 96 | MISSING-IN-PORT | vanilla:mooshroom_spawn_egg |
| 21 | MyOcelotSpawnBlock :554 | oreocelot :6256 | OreSpawn_OcelotSpawnBlock :2029 | Ocelot | c 20 | 2727 | spawn_egg meta 98 | MISSING-IN-PORT | vanilla:ocelot_spawn_egg |
| 22 | MyBlazeSpawnBlock :555 | oreblaze :6257 | OreSpawn_BlazeSpawnBlock :2031 | Blaze | c 21 | 2730 | spawn_egg meta 61 | MISSING-IN-PORT | vanilla:blaze_spawn_egg |
| 23 | MyWitherSkeletonSpawnBlock :556 | orewitherskeleton :6258 | OreSpawn_WitherSkeletonSpawnBlock :2030 | Wither Skeleton | c 22 | 2733 | custom `WitherSkeletonEgg` item | MISSING-IN-PORT | vanilla:wither_skeleton_spawn_egg |
| 24 | MyEnderDragonSpawnBlock :557 | oreenderdragon :6259 | OreSpawn_EnderDragonSpawnBlock :2028 | Ender Dragon | c 23 | 2736 | custom `EnderDragonEgg` item | MISSING-IN-PORT | vanilla:ender_dragon_spawn_egg (exists since 1.20.5 — verified in the 1.21.1 client jar; earlier NONE claim corrected 2026-08-08, D5) |
| 25 | MySnowGolemSpawnBlock :558 | oresnowgolem :6260 | OreSpawn_SnowGolemSpawnBlock :2027 | Snow Golem | c 24 | 2739 | custom `SnowGolemEgg` item | MISSING-IN-PORT | vanilla:snow_golem_spawn_egg (exists since 1.20.5; NONE claim corrected 2026-08-08, D5) |
| 26 | MyIronGolemSpawnBlock :559 | oreirongolem :6261 | OreSpawn_IronGolemSpawnBlock :2026 | Iron Golem | c 25 | 2742 | custom `IronGolemEgg` item | MISSING-IN-PORT | vanilla:iron_golem_spawn_egg (exists since 1.20.5; NONE claim corrected 2026-08-08, D5) |
| 27 | MyWitherBossSpawnBlock :560 | orewitherboss :6262 | OreSpawn_WitherBossSpawnBlock :2001 | Wither Boss | c 26 | 2745 | custom `WitherBossEgg` **meta 64** (quirk) | MISSING-IN-PORT | vanilla:wither_spawn_egg (exists since 1.20.5; NONE claim corrected 2026-08-08, D5) |
| 28 | MyGirlfriendSpawnBlock :561 | oregirlfriend :6263 | OreSpawn_GirlfriendSpawnBlock :2002 | Girlfriend | c 27 | 2748 | `GirlfriendEgg` | MISSING-IN-PORT | MI:967 GIRLFRIEND_SPAWN_EGG |
| 29 | MyBoyfriendSpawnBlock :562 | oreboyfriend :6264 | OreSpawn_BoyfriendSpawnBlock :2003 | Boyfriend | c 85 | 2751 | `BoyfriendEgg` | MISSING-IN-PORT | MI:957 BOYFRIEND_SPAWN_EGG |
| 30 | MyRedCowSpawnBlock :563 | oreredcow :6265 | OreSpawn_RedCowSpawnBlock :2004 | Apple Cow | c 28 | 2754 | `RedCowEgg` | MISSING-IN-PORT | MI:1031 APPLE_COW_SPAWN_EGG (also MI:1017 RED_COW_SPAWN_EGG — pick one) |
| 31 | MyCrystalCowSpawnBlock :564 | orecrystalcow :6266 | OreSpawn_CrystalCowSpawnBlock :2005 | Crystal Cow | **—** (in no pool!) | 2757 | `CrystalCowEgg` | MISSING-IN-PORT | MI:1021 CRYSTAL_COW_SPAWN_EGG |
| 32 | MyVillagerSpawnBlock :565 | orevillager :6267 | OreSpawn_VillagerSpawnBlock :2006 | Villager | c 95 | 2760 | spawn_egg meta 120 | MISSING-IN-PORT | vanilla:villager_spawn_egg |
| 33 | MyGoldCowSpawnBlock :566 | oregoldcow :6268 | OreSpawn_GoldCowSpawnBlock :2007 | Golden Apple Cow | c 29 | 2763 | `GoldCowEgg` | MISSING-IN-PORT | MI:1023 GOLD_COW_SPAWN_EGG (also MI:1036 GOLDEN_APPLE_COW_SPAWN_EGG — pick one) |
| 34 | MyEnchantedCowSpawnBlock :567 | oreenchantedcow :6269 | OreSpawn_EnchantedCowSpawnBlock :2008 | Enchanted Golden Apple Cow | c 30 | 2766 | `EnchantedCowEgg` | MISSING-IN-PORT | MI:1050 ENCHANTED_APPLE_COW_SPAWN_EGG |
| 35 | MyMOTHRASpawnBlock :568 | oreMOTHRA :6270 | OreSpawn_MOTHRASpawnBlock :2009 | MOTHRA | c 31 | 2769 | `MOTHRAEgg` | MISSING-IN-PORT | MI:1009 MOTHRA_SPAWN_EGG |
| 36 | MyAloSpawnBlock :569 | orealosaurus :6277 | OreSpawn_AloSpawnBlock :2010 | Alosaurus | c 32 | 2772 | `AloEgg` | MISSING-IN-PORT | MI:797 ALOSAURUS_SPAWN_EGG |
| 37 | MyCryoSpawnBlock :570 | orecryolophosaurus :6278 | OreSpawn_CryoSpawnBlock :2011 | Cryolophosaurus | c 33 | 2775 | `CryoEgg` | MISSING-IN-PORT | MI:813 CRYOLOPHOSAURUS_SPAWN_EGG |
| 38 | MyCamaSpawnBlock :571 | orecamarasaurus :6279 | OreSpawn_CamaSpawnBlock :2012 | Camarasaurus | c 34 | 2778 | `CamaEgg` | MISSING-IN-PORT | MI:959 CAMARASAURUS_SPAWN_EGG |
| 39 | MyVeloSpawnBlock :572 | orevelocityraptor :6280 | OreSpawn_VeloSpawnBlock :2013 | Velocity Raptor | c 35 | 2781 | `VeloEgg` | MISSING-IN-PORT | MI:991 VELOCITY_RAPTOR_SPAWN_EGG |
| 40 | MyHydroSpawnBlock :573 | orehydrolisc :6281 | OreSpawn_HydroSpawnBlock :2014 | Hydrolisc | c 36 | 2784 | `HydroEgg` | MISSING-IN-PORT | MI:969 HYDROLISC_SPAWN_EGG |
| 41 | MyBasilSpawnBlock :574 | orebasilisc :6282 | OreSpawn_BasilSpawnBlock :2015 | Basilisk | c 37 | 2787 | `BasilEgg` | MISSING-IN-PORT | MI:803 BASILISK_SPAWN_EGG |
| 42 | MyDragonflySpawnBlock :575 | oredragonfly :6283 | OreSpawn_DragonflySpawnBlock :2016 | Dragonfly | c 38 | 2790 | `DragonflyEgg` | MISSING-IN-PORT | MI:945 DRAGONFLY_SPAWN_EGG |
| 43 | MyEmperorScorpionSpawnBlock :576 | oreemperorscorpion :6284 | OreSpawn_EmperorScorpionSpawnBlock :2017 | Emperor Scorpion | c 39 | 2793 | `EmperorScorpionEgg` | MISSING-IN-PORT | MI:871 EMPEROR_SCORPION_SPAWN_EGG |
| 44 | MyScorpionSpawnBlock :577 | orescorpion :6285 | OreSpawn_ScorpionSpawnBlock :2018 | Scorpion | c 40 | 2796 | `ScorpionEgg` | MISSING-IN-PORT | MI:889 SCORPION_SPAWN_EGG |
| 45 | MyCaveFisherSpawnBlock :578 | orecavefisher :6286 | OreSpawn_CaveFisherSpawnBlock :2019 | Cave Fisher | c 41 | 2799 | `CaveFisherEgg` | MISSING-IN-PORT | MI:805 CAVE_FISHER_SPAWN_EGG |
| 46 | MySpyroSpawnBlock :579 | orespyro :6287 | OreSpawn_SpyroSpawnBlock :2020 | Baby Dragon | c 42 | 2802 | `SpyroEgg` | MISSING-IN-PORT | MI:963 BABY_DRAGON_SPAWN_EGG (also MI:979 SPYRO_SPAWN_EGG — pick one) |
| 47 | MyBaryonyxSpawnBlock :580 | orebaryonyx :6288 | OreSpawn_BaryonyxSpawnBlock :2021 | Baryonyx | c 43 | 2805 | `BaryonyxEgg` | MISSING-IN-PORT | MI:907 BARYONYX_SPAWN_EGG |
| 48 | MyGammaMetroidSpawnBlock :581 | oregammametroid :6289 | OreSpawn_GammaMetroidSpawnBlock :2022 | "WTF?" (Gamma Metroid) | c 44 | 2808 | `GammaMetroidEgg` | MISSING-IN-PORT | MI:965 GAMMA_METROID_SPAWN_EGG |
| 49 | MyCockateilSpawnBlock :582 | orecockateil :6290 | OreSpawn_CockateilSpawnBlock :2023 | Bird (Cockateil) | c 45 | 2811 | `CockateilEgg` | MISSING-IN-PORT | MI:923 COCKATEIL_SPAWN_EGG |
| 50 | MyKyuubiSpawnBlock :583 | orekyuubi :6291 | OreSpawn_KyuubiSpawnBlock :2024 | Kyuubi | c 46 | 2814 | `KyuubiEgg` | MISSING-IN-PORT | MI:875 KYUUBI_SPAWN_EGG |
| 51 | MyAlienSpawnBlock :584 | orealien :6292 | OreSpawn_AlienSpawnBlock :2025 | Alien | c 47 | 2817 | `AlienEgg` | MISSING-IN-PORT | MI:793 ALIEN_SPAWN_EGG |
| 52 | MyAttackSquidSpawnBlock :585 | oreattacksquid :6293 | OreSpawn_AttackSquidSpawnBlock :2032 | Attack Squid | c 48 | 2820 | `AttackSquidEgg` | MISSING-IN-PORT | MI:799 ATTACK_SQUID_SPAWN_EGG |
| 53 | MyWaterDragonSpawnBlock :586 | orewaterdragon :6294 | OreSpawn_WaterDragonSpawnBlock :2033 | WaterDragon | c 49 | 2823 | `WaterDragonEgg` | MISSING-IN-PORT | MI:993 WATER_DRAGON_SPAWN_EGG |
| 54 | MyKrakenSpawnBlock :587 | orekraken :6297 | OreSpawn_KrakenSpawnBlock :2035 | Kraken | c 50 | 2826 | `KrakenEgg` | **MB:158 KRAKEN_SPAWN_BLOCK** | MI:859 KRAKEN_SPAWN_EGG |
| 55 | MyLizardSpawnBlock :588 | orelizard :6298 | OreSpawn_LizardSpawnBlock :2036 | Lizard | c 51 | 2829 | `LizardEgg` | MISSING-IN-PORT | MI:973 LIZARD_SPAWN_EGG |
| 56 | MyCephadromeSpawnBlock :589 | orecephadrome :6295 | OreSpawn_CephadromeSpawnBlock :2034 | Cephadrome | c 52 | 2832 | `CephadromeEgg` | MISSING-IN-PORT | MI:1015 CEPHADROME_SPAWN_EGG |
| 57 | MyDragonSpawnBlock :590 | oredragon :6296 | OreSpawn_DragonSpawnBlock :2037 | Dragon | c 53 | 2835 | `DragonEgg` | **MB:160 DRAGON_SPAWN_BLOCK** | MI:961 DRAGON_SPAWN_EGG |
| 58 | MyBeeSpawnBlock :591 | orebee :6299 | OreSpawn_BeeSpawnBlock :2038 | Bee | c 54 | 2838 | `BeeEgg` | MISSING-IN-PORT | MI:865 BEE_SPAWN_EGG |
| 59 | MyHorseSpawnBlock :592 | orehorse :6300 | OreSpawn_HorseSpawnBlock :2039 | Horse | c 55 | 2841 | spawn_egg meta 100 | MISSING-IN-PORT | vanilla:horse_spawn_egg |
| 60 | MyTrooperBugSpawnBlock :593 | oretrooper :6301 | OreSpawn_TrooperBugSpawnBlock :2040 | Jumpy Bug (Trooper) | c 56 | 2844 | `TrooperBugEgg` | MISSING-IN-PORT | MI:897 TROOPER_BUG_SPAWN_EGG |
| 61 | MySpitBugSpawnBlock :594 | orespit :6302 | OreSpawn_SpitBugSpawnBlock :2041 | Spit Bug | c 57 | 2847 | `SpitBugEgg` | MISSING-IN-PORT | MI:891 SPIT_BUG_SPAWN_EGG |
| 62 | MyStinkBugSpawnBlock :595 | orestink :6303 | OreSpawn_StinkBugSpawnBlock :2042 | Stink Bug | c 58 | 2850 | `StinkBugEgg` | MISSING-IN-PORT | MI:951 STINK_BUG_SPAWN_EGG |
| 63 | MyOstrichSpawnBlock :596 | oreostrich :6304 | OreSpawn_OstrichSpawnBlock :2043 | Ostrich | c 59 | 2853 | `OstrichEgg` | MISSING-IN-PORT | MI:975 OSTRICH_SPAWN_EGG |
| 64 | MyGazelleSpawnBlock :597 | oregazelle :6305 | OreSpawn_GazelleSpawnBlock :2044 | Gazelle | c 60 | 2856 | `GazelleEgg` | MISSING-IN-PORT | MI:931 GAZELLE_SPAWN_EGG |
| 65 | MyChipmunkSpawnBlock :598 | orechipmunk :6306 | OreSpawn_ChipmunkSpawnBlock :2045 | Chipmunk | c 61 | 2859 | `ChipmunkEgg` | MISSING-IN-PORT | MI:913 CHIPMUNK_SPAWN_EGG |
| 66 | MyCreepingHorrorSpawnBlock :599 | orecreepinghorror :6307 | OreSpawn_CreepingHorrorSpawnBlock :2046 | Creeping Horror | c 62 | 2862 | `CreepingHorrorEgg` | MISSING-IN-PORT | MI:811 CREEPING_HORROR_SPAWN_EGG |
| 67 | MyTerribleTerrorSpawnBlock :600 | oreterribleterror :6308 | OreSpawn_TerribleTerrorSpawnBlock :2047 | Terrible Terror | c 63 | 2865 | `TerribleTerrorEgg` | MISSING-IN-PORT | MI:893 TERRIBLE_TERROR_SPAWN_EGG |
| 68 | MyCliffRacerSpawnBlock :601 | orecliffracer :6309 | OreSpawn_CliffRacerSpawnBlock :2048 | Cliff Racer | c 64 | 2868 | `CliffRacerEgg` | MISSING-IN-PORT | MI:941 CLIFF_RACER_SPAWN_EGG |
| 69 | MyTriffidSpawnBlock :602 | oretriffid :6310 | OreSpawn_TriffidSpawnBlock :2049 | Triffid | c 65 | 2871 | `TriffidEgg` | MISSING-IN-PORT | MI:895 TRIFFID_SPAWN_EGG |
| 70 | MyPitchBlackSpawnBlock :603 | orenightmare :6311 | OreSpawn_PitchBlackSpawnBlock :2050 | Nightmare | c 66 | 2874 | `PitchBlackEgg` | MISSING-IN-PORT | MI:831 PITCH_BLACK_SPAWN_EGG |
| 71 | MyLurkingTerrorSpawnBlock :604 | orelurkingterror :6312 | OreSpawn_LurkingTerrorSpawnBlock :2051 | Lurking Terror | c 67 | 2877 | `LurkingTerrorEgg` | MISSING-IN-PORT | MI:879 LURKING_TERROR_SPAWN_EGG |
| 72 | MyGodzillaPartSpawnBlock :605 | oregodzillapart :6313 | OreSpawn_GodzillaPartSpawnBlock :2052 | Mobzilla (egg **part**) | c 68 | 9×part→#73 :2886 | (no water recipe) | MISSING-IN-PORT | n/a (block-only) |
| 73 | MyGodzillaSpawnBlock :606 | oregodzilla :6314 | OreSpawn_GodzillaSpawnBlock :2053 | Mobzilla (full egg) | **c 69** (full egg IS in pool) | 2889 | `GodzillaEgg` | MISSING-IN-PORT | MI:857 GODZILLA_SPAWN_EGG |
| 74 | MyTheKingPartSpawnBlock :607 | orethekingpart :6341 | OreSpawn_TheKingPartSpawnBlock :2054 | The King (egg **part**) | c 86 | 9×part→#76 :2892 | (no water recipe) | MISSING-IN-PORT | n/a (block-only) |
| 75 | MyTheQueenPartSpawnBlock :608 | orethequeenpart :6343 | OreSpawn_TheQueenPartSpawnBlock :2056 | The Queen (egg **part**) | c 97 | 9×part→#77 :2898 | (no water recipe) | MISSING-IN-PORT | n/a (block-only) |
| 76 | MyTheKingSpawnBlock :609 | oretheking :6342 | OreSpawn_TheKingSpawnBlock :2055 | The King (full egg) | **—** (craft-only) | 2895 | `TheKingEgg` | MISSING-IN-PORT | MI:861 THE_KING_SPAWN_EGG |
| 77 | MyTheQueenSpawnBlock :610 | orethequeen :6344 | OreSpawn_TheQueenSpawnBlock :2057 | The Queen (full egg) | **—** (craft-only) | 2901 | `TheQueenEgg` | MISSING-IN-PORT | MI:863 THE_QUEEN_SPAWN_EGG |
| 78 | MySmallWormSpawnBlock :611 | oresmallworm :6315 | OreSpawn_SmallWormSpawnBlock :2058 | Small Worm | c 70 | 2904 | `SmallWormEgg` | MISSING-IN-PORT | MI:901 WORM_SMALL_SPAWN_EGG |
| 79 | MyMediumWormSpawnBlock :612 | oremediumworm :6316 | OreSpawn_MediumWormSpawnBlock :2059 | Medium Worm | c 71 | 2907 | `MediumWormEgg` | MISSING-IN-PORT | MI:903 WORM_MEDIUM_SPAWN_EGG |
| 80 | MyLargeWormSpawnBlock :613 | orelargeworm :6317 | OreSpawn_LargeWormSpawnBlock :2060 | Large Worm | c 72 | 2910 | `LargeWormEgg` | MISSING-IN-PORT | MI:905 WORM_LARGE_SPAWN_EGG |
| 81 | MyCassowarySpawnBlock :614 | orecassowary :6318 | OreSpawn_CassowarySpawnBlock :2061 | Cassowary | c 73 | 2913 | `CassowaryEgg` | MISSING-IN-PORT | MI:911 CASSOWARY_SPAWN_EGG |
| 82 | MyCloudSharkSpawnBlock :615 | orecloudshark :6319 | OreSpawn_CloudSharkSpawnBlock :2062 | Cloud Shark | c 74 | 2916 | `CloudSharkEgg` | MISSING-IN-PORT | MI:807 CLOUD_SHARK_SPAWN_EGG |
| 83 | MyGoldFishSpawnBlock :616 | oregoldfish :6320 | OreSpawn_GoldFishSpawnBlock :2063 | Gold Fish | c 75 | 2919 | `GoldFishEgg` | MISSING-IN-PORT | MI:933 GOLD_FISH_SPAWN_EGG |
| 84 | MyLeafMonsterSpawnBlock :617 | oreleafmonster :6321 | OreSpawn_LeafMonsterSpawnBlock :2064 | Leaf Monster | c 76 | 2922 | `LeafMonsterEgg` | MISSING-IN-PORT | MI:877 LEAF_MONSTER_SPAWN_EGG |
| 85 | MyTshirtSpawnBlock :618 | oretshirt :6322 | OreSpawn_TshirtSpawnBlock :2065 | T-Shirt | c 77 | 2925 | `TshirtEgg` | MISSING-IN-PORT | MI:921 TSHIRT_SPAWN_EGG |
| 86 | MyEnderKnightSpawnBlock :619 | oreenderknight :6323 | OreSpawn_EnderKnightSpawnBlock :2066 | Ender Knight | c 78 | 2880 | `EnderKnightEgg` | MISSING-IN-PORT | MI:817 ENDER_KNIGHT_SPAWN_EGG |
| 87 | MyEnderReaperSpawnBlock :620 | oreenderreaper :6324 | OreSpawn_EnderReaperSpawnBlock :2067 | Ender Reaper | c 79 | 2883 | `EnderReaperEgg` | MISSING-IN-PORT | MI:819 ENDER_REAPER_SPAWN_EGG |
| 88 | MyBeaverSpawnBlock :621 | orebeaver :6325 | OreSpawn_BeaverSpawnBlock :2068 | Beaver | c 80 | 2928 | `BeaverEgg` | MISSING-IN-PORT | MI:909 BEAVER_SPAWN_EGG |
| 89 | MyUrchinSpawnBlock :622 | oreurchin :6326 | OreSpawn_UrchinSpawnBlock :2069 | Urchin | C 0 | 2931 | `UrchinEgg` | **MB:169 ORE_URCHIN** | MI:855 URCHIN_SPAWN_EGG |
| 90 | MyFlounderSpawnBlock :623 | oreflounder :6327 | OreSpawn_FlounderSpawnBlock :2070 | Flounder | C 1 | 2934 | `FlounderEgg` | **MB:171 ORE_FLOUNDER** | MI:927 FLOUNDER_SPAWN_EGG |
| 91 | MySkateSpawnBlock :624 | oreskate :6328 | OreSpawn_SkateSpawnBlock :2071 | Skate | C 2 | 2937 | `SkateEgg` | **MB:173 ORE_SKATE** | MI:851 SKATE_SPAWN_EGG |
| 92 | MyRotatorSpawnBlock :625 | orerotator :6329 | OreSpawn_RotatorSpawnBlock :2072 | Rotator | C 3 | 2940 | `RotatorEgg` | **MB:175 ORE_ROTATOR** | MI:887 ROTATOR_SPAWN_EGG |
| 93 | MyPeacockSpawnBlock :626 | orepeacock :6330 | OreSpawn_PeacockSpawnBlock :2073 | Peacock | C 4 | 2943 | `PeacockEgg` | **MB:177 ORE_PEACOCK** | MI:935 PEACOCK_SPAWN_EGG |
| 94 | MyFairySpawnBlock :627 | orefairy :6331 | OreSpawn_FairySpawnBlock :2074 | Fairy | C 5 | 2946 | `FairyEgg` | **MB:179 ORE_FAIRY** | MI:1001 FAIRY_SPAWN_EGG |
| 95 | MyDungeonBeastSpawnBlock :628 | oredungeonbeast :6332 | OreSpawn_DungeonBeastSpawnBlock :2075 | Dungeon Beast | C 6 | 2949 | `DungeonBeastEgg` | **MB:181 ORE_DUNGEON_BEAST** | MI:815 DUNGEON_BEAST_SPAWN_EGG |
| 96 | MyVortexSpawnBlock :629 | orevortex :6333 | OreSpawn_VortexSpawnBlock :2076 | Vortex | C 7 | 2952 | `VortexEgg` | **MB:183 ORE_VORTEX** | MI:899 VORTEX_SPAWN_EGG |
| 97 | MyRatSpawnBlock :630 | orerat :6334 | OreSpawn_RatSpawnBlock :2077 | Rat | C 8 | 2955 | `RatEgg` | **MB:185 ORE_RAT** | MI:885 RAT_SPAWN_EGG |
| 98 | MyWhaleSpawnBlock :631 | orewhale :6335 | OreSpawn_WhaleSpawnBlock :2078 | Whale | C 9 | 2958 | `WhaleEgg` | **MB:187 ORE_WHALE** | MI:937 WHALE_SPAWN_EGG |
| 99 | MyIrukandjiSpawnBlock :632 | oreirukandji :6336 | OreSpawn_IrukandjiSpawnBlock :2079 | Irukandji | C 10 | 2961 | `IrukandjiEgg` | **MB:189 ORE_IRUKANDJI** | MI:827 IRUKANDJI_SPAWN_EGG |
| 100 | MyTRexSpawnBlock :633 | oretrex :6337 | OreSpawn_TRexSpawnBlock :2080 | T. Rex | c 81 | 2964 | `TRexEgg` | MISSING-IN-PORT | MI:853 TREX_SPAWN_EGG |
| 101 | MyHerculesSpawnBlock :634 | orehercules :6338 | OreSpawn_HerculesSpawnBlock :2081 | Hercules Beetle | c 82 | 2967 | `HerculesEgg` | MISSING-IN-PORT | MI:873 HERCULES_BEETLE_SPAWN_EGG |
| 102 | MyMantisSpawnBlock :635 | oremantis :6339 | OreSpawn_MantisSpawnBlock :2082 | Mantis | c 83 | 2970 | `MantisEgg` | MISSING-IN-PORT | MI:881 MANTIS_SPAWN_EGG |
| 103 | MyStinkySpawnBlock :636 | orestinky :6340 | OreSpawn_StinkySpawnBlock :2083 | Stinky | c 84 | 2973 | `StinkyEgg` | MISSING-IN-PORT | MI:981 STINKY_SPAWN_EGG |
| 104 | MyEasterBunnySpawnBlock :637 | oreeasterbunny :6345 | OreSpawn_EasterBunnySpawnBlock :2084 | Easter Bunny | c 87 | 2976 | `EasterBunnyEgg` | MISSING-IN-PORT | MI:925 EASTER_BUNNY_SPAWN_EGG |
| 105 | MyCaterKillerSpawnBlock :638 | orecaterkiller :6346 | OreSpawn_CaterKillerSpawnBlock :2085 | CaterKiller | c 88 | 3003 | `CaterKillerEgg` | MISSING-IN-PORT | MI:869 CATER_KILLER_SPAWN_EGG |
| 106 | MyMolenoidSpawnBlock :639 | oremolenoid :6347 | OreSpawn_MolenoidSpawnBlock :2086 | Molenoid | c 89 | 3006 | `MolenoidEgg` | MISSING-IN-PORT | MI:883 MOLENOID_SPAWN_EGG |
| 107 | MySeaMonsterSpawnBlock :640 | oreseamonster :6348 | OreSpawn_SeaMonsterSpawnBlock :2087 | Sea Monster | c 90 | 3009 | `SeaMonsterEgg` | MISSING-IN-PORT | MI:847 SEA_MONSTER_SPAWN_EGG |
| 108 | MySeaViperSpawnBlock :641 | oreseaviper :6349 | OreSpawn_SeaViperSpawnBlock :2088 | Sea Viper | c 91 | 3012 | `SeaViperEgg` | MISSING-IN-PORT | MI:849 SEA_VIPER_SPAWN_EGG |
| 109 | MyLeonSpawnBlock :642 | oreleon :6350 | OreSpawn_LeonSpawnBlock :2089 | Leonopteryx | c 92 | 3021 | `LeonEgg` | MISSING-IN-PORT | MI:971 LEON_SPAWN_EGG (MI:833 LEONOPTERYX_SPAWN_EGG also exists) |
| 110 | MyHammerheadSpawnBlock :643 | orehammerhead :6351 | OreSpawn_HammerheadSpawnBlock :2090 | Hammerhead | c 93 | 3018 | `HammerheadEgg` | MISSING-IN-PORT | MI:825 HAMMERHEAD_SPAWN_EGG |
| 111 | MyRubberDuckySpawnBlock :644 | orerubberducky :6352 | OreSpawn_RubberDuckySpawnBlock :2091 | Rubber Ducky | c 94 | 3015 | `RubberDuckyEgg` | MISSING-IN-PORT | MI:977 RUBBER_DUCKY_SPAWN_EGG |
| 112 | MyCriminalSpawnBlock :645 | orecriminal :6353 | OreSpawn_CriminalSpawnBlock :2092 | Criminal | c 96 | 2979 | `CriminalEgg` | MISSING-IN-PORT | **NONE** (no CRIMINAL_SPAWN_EGG in ModItems) |
| 113 | MyBrutalflySpawnBlock :646 | orebrutalfly :6354 | OreSpawn_BrutalflySpawnBlock :2093 | Brutalfly | R 0 | 2982 | `BrutalflyEgg` | MISSING-IN-PORT | MI:867 BRUTALFLY_SPAWN_EGG |
| 114 | MyNastysaurusSpawnBlock :647 | orenastysaurus :6355 | OreSpawn_NastysaurusSpawnBlock :2094 | Nastysaurus | R 1 | 2985 | `NastysaurusEgg` | MISSING-IN-PORT | MI:829 NASTYSAURUS_SPAWN_EGG |
| 115 | MyPointysaurusSpawnBlock :648 | orepointysaurus :6356 | OreSpawn_PointysaurusSpawnBlock :2095 | Pointysaurus | R 2 | 2988 | `PointysaurusEgg` | MISSING-IN-PORT | MI:835 POINTYSAURUS_SPAWN_EGG |
| 116 | MyCricketSpawnBlock :649 | orecricket :6357 | OreSpawn_CricketSpawnBlock :2096 | Cricket | R 3 | 2991 | `CricketEgg` | MISSING-IN-PORT | MI:943 CRICKET_SPAWN_EGG |
| 117 | MyFrogSpawnBlock :650 | orefrog :6358 | OreSpawn_FrogSpawnBlock :2097 | Frog | R 4 | 2994 | `FrogEgg` | MISSING-IN-PORT | MI:929 FROG_SPAWN_EGG |
| 118 | MySpiderDriverSpawnBlock :651 | orespiderdriver :6359 | OreSpawn_SpiderDriverSpawnBlock :2098 | Spider Driver | R 5 | 2997 | `SpiderDriverEgg` | MISSING-IN-PORT | MI:1019 SPIDER_DRIVER_SPAWN_EGG |
| 119 | MyCrabSpawnBlock :652 | orecrab :6360 | OreSpawn_CrabSpawnBlock :2099 | Crab | R 6 | 3000 | `CrabEgg` | MISSING-IN-PORT | MI:809 CRAB_SPAWN_EGG |

Two additional `OreGenericEgg` instances are **not** spawn ores at all — decorative storage
blocks that reuse the class (and therefore its XP-on-break quirk):
`MyEnderPearlBlock` ("blockenderpearl", OSM:1972, reg OSM:2186 "OreSpawn_EnderPearlBlock",
name "Ender-Pearl Block" OSM:3067) and `MyEyeOfEnderBlock` ("blockeyeofender", OSM:1973,
name "Eye-of-Ender Block" OSM:3069). The port already reuses its `OreGenericEgg` for these
(POGE:11-12).

**Membership check** (119 blocks): 98 common-pool (c 0-97) + 7 rare-pool (R 0-6) = **105
worldgen types** (WGEN-005's "~105"); 11 Crystal-pool (C 0-10, already ported); 3 that
generate nowhere (#31 CrystalCow, #76 TheKing, #77 TheQueen — obtainable only by crafting
9 parts / creative).

---

## 3. Pool selection — exact weights and order

Both the overworld path (`OreSpawnWorld.generateOres`, OSW:349) and the dimension path
(`ChunkOreGenerator.generateOresInChunk`, COG:15) use the **identical** two-tier roll per
vein; only the vein-count roll differs:

```
if (SpawnOres_stats.rate > 0):                      // OSW:355 / COG:21
  patchy = rate + random.nextInt(20)                 // OVERWORLD: OSW:356  → 28..47
  patchy = rate + random.nextInt(30)                 // DIMENSIONS: COG:22 → 28..57
  if (random.nextInt(20) == 0) patchy += 30          // 1-in-20 bonus:  OSW:357-359 / COG:23-25
  if (LessOre != 0) patchy /= 3                      // OSW:360-362 / COG:26-28
  repeat patchy times:                               // OSW:363 / COG:29
    x = 3 + chunkBlockX + nextInt(10)                // OSW:366 / COG:32  (offsets 3..12)
    y = nextInt(128)                                 // OSW:367 / COG:33  (0..127)
    z = 3 + chunkBlockZ + nextInt(10)                // OSW:368 / COG:34
    if (y > maxdepth(128) || y < mindepth(50)) SKIP  // OSW:369 / COG:35 — vein DISCARDED, not rerolled
    if (nextInt(104) < 7):                           // 7/104 ≈ 6.73% — RARE pool, OSW:370 / COG:36
        b = switch(nextInt(7)) { R0..R6 }            // OSW:371-402 / COG:37-68
    else:                                            // 97/104 — COMMON pool
        b = switch(nextInt(98)) { c0..c97 }          // OSW:406-801 / COG:72-467
    place vein of b, size = clumpsize (4)            // OSW:403,802 / COG:69,468
```

- **Per-type odds per rolled vein**: common type = (97/104)·(1/98) ≈ 0.952%;
  rare type = (7/104)·(1/7) ≈ 0.962%. The pool is essentially uniform across all 105 types.
- **Y filter discards**: only 78 of 128 possible Y values pass (50..127), so ≈ 60.9% of
  rolled veins actually place. `maxdepth = 128` is unreachable (`nextInt(128)` ≤ 127).
- **`SpawnOres_stats`** (OSM:1579): `get_orestats(config, "SpawnOres", rate=28, clumpsize=4,
  min=50, max=128)`. Config clamps (OSM:6098-6129): rate ∈ [14, 56], clumpsize ∈ [2, 8]
  (never < 1), mindepth ≥ 0, maxdepth ≥ 0, and if `maxdepth−mindepth < 10` both reset to
  defaults. `OreStats` is a plain 4-int struct (OreStats.java:6-11).
- Expected placed veins/chunk (defaults, LessOre=0): overworld
  (28+9.5+1.5)·(78/128) ≈ **23.8**; dimensions (28+14.5+1.5)·(78/128) ≈ **26.8**.

---

## 4. THE BLOCK — original `OreGenericEgg` behavior vs port

### 4.1 Original (OreGenericEgg.java, whole file)

| Property | Value | Cite |
|---|---|---|
| Superclass / material | `Block`, `Material.field_151578_c` (ground/dirt) | OGE:14-17 |
| Hardness | 0.5 (`func_149711_c(0.5f)`) | OGE:18 |
| Resistance | 1.0 (`func_149752_b(1.0f)`) | OGE:19 |
| Step sound | `Block.field_149767_g` = gravel | OGE:20 |
| Creative tab | `CreativeTabs.field_78030_b` = Building Blocks | OGE:21 |
| Tool/harvest level | none set → breakable by hand, always drops | (absent) |
| Drops | itself, count 1 (vanilla `super.func_149690_a`) | OGE:25 |
| Bonus XP | `j1 = 5 + nextInt(3) + nextInt(3)` (5..9); dropped only `if nextInt(2)==1` (50%) via `func_149657_c` | OGE:26-29 |
| Silk Touch | 1.7.10 silk-harvest path bypasses `func_149690_a` → same block drop, **no XP** | (vanilla harvest chain) |
| Opaque / normal cube | `func_149662_c` / `func_149686_d` both `true` | OGE:32-38 |
| Icon | `"OreSpawn:" + unlocalizedName.substring(5)` → per-mob texture, e.g. `tile.orespider` → `orespider.png` | OGE:40-43 |
| ctor arg `oldid` | ignored (legacy block ID) | OGE:16 |
| **Mob spawning** | **NONE.** Breaking a spawn ore never spawns a mob — there is no break/harvest event handler anywhere in the mod (grep for HarvestDropsEvent/BreakEvent: 0 hits). The mob comes only from crafting the egg (§6) and using it. | OGE (whole file) |

### 4.2 Port class (already exists — new blocks must reuse it)

`danger.orespawn.block.OreGenericEgg` (POGE:26-40): overrides `spawnAfterBreak` —
`if (dropExperience && random.nextInt(2)==1) popExperience(5 + nextInt(3) + nextInt(3))`
(POGE:36-38); `dropExperience` is false for Silk Touch, matching 1.7.10 (POGE:22-24).
Registered with `BlockBehaviour.Properties.of().strength(0.5f, 1.0f).sound(SoundType.GRAVEL)`
(e.g. MB:158-159, MB:169-170) — hardness 0.5 / resistance 1.0 / gravel = exact original.
BlockItem via `ITEMS.registerSimpleBlockItem` (MI:60, MI:63). The earlier item-duplication
bug (dropping 5..9 extra blocks instead of XP) was removed under **ITEM-021** (POGE:21-24).

---

## 5. WORLDGEN — placement mechanics, dimensions, ordering

### 5.1 Overworld (dimension 0)

- Dispatch: `OreSpawnWorld.generate` switch on `world.provider.dimensionId`, `case 0:` calls
  `generateSurface` then `generateOres` (OSW:209-218). Registered as IWorldGenerator with
  **weight 10** (OSM:5375) → runs during population, after vanilla ore gen.
- Vein placement: `new WorldGenMinable(b, SpawnOres_stats.clumpsize).generate(...)`
  (OSW:403, OSW:802) — the 2-arg WorldGenMinable **replaces `Blocks.stone` only**; clump
  size 4.
- Nether (case −1 → `generateNether`, OSW:210-212, 243-272) and End (case 1 →
  `generateEnd`, OSW:219-222, 226-241) get **no** spawn ores.

### 5.2 OreSpawn dimensions (ChunkOreGenerator path)

`generateOresInChunk` is called on the freshly-built `Chunk` **during terrain generation**
(before caves are already carved — after cave/ravine gen, before decoration), with direct
chunk array writes:

| Dimension (INDEX.md:115-119 naming) | Dim ID (OSM:1595-1600, base 80 OSM:1467) | Call site | Calls/chunk |
|---|---|---|---|
| WorldProviderOreSpawn (dim 1) | 80 | CP:176 | 1 |
| Mining dimension | 81 | CP2:191, +2 more at CP2:192-195 **iff LessOre == 0** | 1 or **3** |
| Chaos dimension | 82 | CP3:179 | 1 |
| Utopia dimension | 83 | — (ChunkProviderOreSpawn4 never calls it) | 0 |
| Crystal dimension | 84 | — (uses its own `generateCrystalOres`, CP5:572-659) | 0 |
| Island dimension | 85 | CP6:197 | 1 |

- Vein placement: `generateBlockOre(world, random, x, y, z, chunk, b, clumpsize)`
  (COG:612-649) — an inline copy of WorldGenMinable's ellipsoid walk operating on
  chunk-local coordinates; replaces **only `Blocks.field_150348_b` (stone)** (COG:642),
  writes with metadata 0 via `setBlockIDWithMetadataInChunk` (COG:643).
- The Crystal dimension's separate 11-type pool: 25+nextInt(30) patches (+30 on 1/20),
  Y must be **> 45**, clump 4, replaces **CrystalStone** (CP5:577-634).

### 5.3 Order relative to other ore gen

SpawnOres is the **first** block of both generators, before every other ore:
SpawnOres (OSW:355 / COG:21) → Uranium (OSW:805 / COG:471) → Titanium (COG:484) →
Amethyst (COG:497) → Salt (COG:510) → RedAntTroll + TermiteTroll blocks (COG:523-544)
→ (only if LessOre==0) vanilla Diamond/BlockDiamond/Emerald/BlockEmerald/Gold/BlockGold/
BlockRuby extras (COG:545-609).

### 5.4 What the port currently does instead (Phase C redesign — PN-010)

Documented at `PARITY_NOTES.md:121-129` (PN-010): "SpawnOres pool reduced to boss spawn
blocks + ancient dried eggs (WGEN-005, Phase C7) … a deliberate redesign kept for Phase C
… Owner: Phase D spawn-block pool restoration."

| Port artifact | Content |
|---|---|
| `data/orespawn/worldgen/configured_feature/dragon_spawn_block.json` (kraken identical) | `minecraft:ore`, size 4, targets `#minecraft:stone_ore_replaceables` + `#minecraft:deepslate_ore_replaceables` → the spawn block |
| `data/orespawn/worldgen/placed_feature/dragon_spawn_block.json`, `kraken_spawn_block.json` | custom `orespawn:less_ore_count` placement; count weighted list `{0: 7738, 1: 2262}`, less-ore list `{0: 9266, 1: 734}`; `in_square`; `height_range` uniform **Y 50..127**; `biome` |
| `data/orespawn/neoforge/biome_modifier/add_boss_spawn_blocks.json` | adds both features to `#minecraft:is_overworld`, step `underground_ores` |
| `…/placed_feature/dragon_spawn_block_dim.json`, `_mining.json` (same for kraken) | dimension variants for the port's custom dims |
| `data/orespawn/worldgen/configured_feature/ancient_dried_egg.json` + `placed_feature/ancient_dried_egg.json` | size-**1** ore in `#stone_ore_replaceables`; `rarity_filter` 1/12 chunks, Y −32..32; biome modifier `add_ancient_dried_eggs.json` (`#minecraft:is_overworld`, step `underground_decoration`) |
| `world/OreSpawnChunkGenerator.generateCrystalOres` (OSCG:470-539) | full-parity Crystal-dim port: 25+nextInt(30) (+30 on 1/20) patches, Y>45, clump 4 in CrystalStone, exact 11-slot pool order (OSCG:525-539) |

The 2262/10000 weight is exactly the original per-chunk expectation of *one* common-pool
type: 39.0 mean veins × (78/128 Y-pass) × (97/104)·(1/98) ≈ 0.2262; the less-ore weight
734/10000 matches the `patchy/3` variant (≈ 0.0734). So kraken/dragon frequency is
already parity-correct; the other **103 pool members simply do not generate** (§9).

---

## 6. THE RECIPES — water-bucket conversions (ITEM-062 remainder)

### 6.1 Shape — exactly as registered

Every conversion is `GameRegistry.addShapelessRecipe(output, waterBucket, spawnBlockStack)`
— a **2-ingredient shapeless** recipe: `new ItemStack(Items.field_151131_as)` (water bucket)
+ `new ItemStack((Block)MyXxxSpawnBlock)` → 1 egg item (first at OSM:2665-2667, pattern
identical through OSM:3021). In 1.7.10 the water bucket's container item is the empty
bucket, so crafting **returns an empty bucket in the grid** — the water is consumed, the
bucket is not. Output count is always 1. The `LanguageRegistry.addNameForObject` line
directly above each recipe gives the block its "Ancient Dried … Spawn Egg" display name
(e.g. OSM:2666 "Ancient Dried Spider Spawn Egg").

**The complete 116-recipe table is the master table in §2** (columns "Recipe OSM:",
"1.7.10 egg output", "Port egg item") — every field in §2 was transcribed from
OSM:2665-3021. Recipe count check: 116 occurrences of the water-bucket item in
OreSpawnMain.java (grep `field_151131_as` = 116) = 119 spawn blocks − 3 part blocks.

Outputs come in two flavors:
1. **Vanilla mobs** → `Items.field_151063_bx` (vanilla spawn egg) with entity-ID metadata
   (e.g. `new ItemStack(Items.field_151063_bx, 1, 52)` = spider, OSM:2667). Metas used:
   50-52, 54-62, 65, 66, 90-96, 98, 100, 120 (see §2 rows 1-22, 32, 59).
2. **OreSpawn mobs** → the mod's own egg item (e.g. `new ItemStack(KrakenEgg)`, OSM:2826).
   Quirk: `WitherBossEgg` is created with damage value 64 (`new ItemStack(WitherBossEgg,
   1, 64)`, OSM:2745) — meaningless on an unstacked custom item, transcribed verbatim.

### 6.2 The three 9-part combine recipes (no water)

| Output block | Recipe | Cite |
|---|---|---|
| `MyGodzillaSpawnBlock` (full Mobzilla egg) | shapeless: 9 × `MyGodzillaPartSpawnBlock` | OSM:2886 |
| `MyTheKingSpawnBlock` | shapeless: 9 × `MyTheKingPartSpawnBlock` | OSM:2892 |
| `MyTheQueenSpawnBlock` | shapeless: 9 × `MyTheQueenPartSpawnBlock` | OSM:2898 |

(The full blocks then each have their own water recipe → GodzillaEgg / TheKingEgg /
TheQueenEgg, OSM:2889/2895/2901.)

### 6.3 Port status

`data/orespawn/recipe/` contains **zero** egg-conversion or part-combine recipes (searched
for `water_bucket`, `*egg*`, `*spawn*`: no hits). FIX_LOG.md:602-605 records ITEM-062 as
PARTIAL: "116 spawn-block→egg water-bucket conversions … blocked on WGEN-005's ~105-type
SpawnOres pool → D5." The port's only rehydration mechanic is `AncientDriedEggBlock`
(ADE:62-98): **right-click** (not crafting) with a water bucket consumes the block,
swaps the bucket for an empty one, and gives 1 random egg from a 7-entry dino pool
(ADE:48-56: Alosaurus, Cryolophosaurus, Nastysaurus, Pointysaurus, TRex, VelocityRaptor,
BabyDragon). Note the ModBlocks doc-comment (MB:106-111) claims a 9-entry pool including
baryonyx and camarasaurus — the code's DINO_POOL has only 7; comment and code disagree.

For NeoForge 1.21.1 the conversions should be `minecraft:crafting_shapeless` JSONs:
`ingredients: [minecraft:water_bucket, orespawn:<spawn_block_item>]`, result 1 ×
`<spawn egg>`; the water bucket's crafting remainder (empty bucket) is automatic in
modern vanilla, matching 1.7.10 container-item behavior with no extra code.

---

## 7. ASSETS — pattern new blocks must follow

Original 1.7.10: each block had its **own per-mob texture**, name = unlocalized name minus
the `tile.` prefix (OGE:40-43). All texture files confirmed present in
`reference_1_7_10_source/assets/orespawn/textures/blocks/` (`orespider.png`,
`oreurchin.png`, `oretheking.png`, `oregodzillapart.png`, `orecrystalcow.png`, …).

**The port has already copied every one of these textures** — 125 `ore*.png` files in
`src/main/resources/assets/orespawn/textures/blocks/` matching the 125 in the
reference assets (verified by full filename diff). One rename: reference `oreMOTHRA.png`
is `oremothra.png` in the port (1.21 resource locations must be lowercase) — the MOTHRA
model JSON must therefore reference `orespawn:blocks/oremothra`, not the literal 1.7.10
name. New blocks therefore need **no new textures** — only the four JSON files per block,
following the existing egg-ore pattern exactly:

1. `assets/orespawn/blockstates/<name>.json` — single variant:
   ```json
   { "variants": { "": { "model": "orespawn:block/<name>" } } }
   ```
   (as in `blockstates/ore_urchin.json`, `blockstates/kraken_spawn_block.json`)
2. `assets/orespawn/models/block/<name>.json` — `minecraft:block/cube_all` with the
   **1.7.10 texture name (no underscores)**:
   ```json
   { "parent": "minecraft:block/cube_all", "textures": { "all": "orespawn:blocks/oreurchin" } }
   ```
   (`models/block/ore_urchin.json`; kraken uses `orespawn:blocks/orekraken`)
3. `assets/orespawn/models/item/<name>.json` — `{ "parent": "orespawn:block/<name>" }`
   (`models/item/ore_urchin.json`)
4. `assets/orespawn/lang/en_us.json` — port names the crystal ores `"<Mob> Egg"`
   (`"block.orespawn.ore_urchin": "Urchin Egg"`). NOTE: the 1.7.10 display names were
   `"Ancient Dried <Mob> Spawn Egg"` (OSM:2666 et seq.) — for line-by-line parity the
   new overworld blocks should use the original "Ancient Dried … Spawn Egg" strings from
   the master table's recipe-site names; flag: the existing 11 crystal blocks deviate.

Java side: register block in ModBlocks with
`new OreGenericEgg(BlockBehaviour.Properties.of().strength(0.5f, 1.0f).sound(SoundType.GRAVEL))`
(MB:169-170 pattern) and BlockItem in ModItems via `registerSimpleBlockItem` (MI:63 pattern).

---

## 8. LOOT TABLES — pattern for new blocks

Existing egg-ore tables (all in `data/orespawn/loot_table/blocks/`):

- `ore_urchin.json` (and the other 10 crystal ores): 1 roll, `minecraft:item` self-drop,
  condition `minecraft:survives_explosion`:
  ```json
  { "type": "minecraft:block",
    "pools": [ { "rolls": 1,
      "entries": [ { "type": "minecraft:item", "name": "orespawn:ore_urchin" } ],
      "conditions": [ { "condition": "minecraft:survives_explosion" } ] } ] }
  ```
- `kraken_spawn_block.json` / `dragon_spawn_block.json`: identical minus the
  `survives_explosion` condition (plain self-drop).

New blocks should use the `ore_urchin.json` form (self-drop + survives_explosion). This is
faithful: the original always dropped itself with no fortune/silk special-casing (OGE:25);
XP comes from the block class, not the loot table (§4.2).

---

## 9. DungeonSpawnerBlock trigger type

**None.** The SpawnOres system has no DungeonSpawnerBlock pathway — the DSB
`type = world.rand.nextInt(50)` cases all build structures, and no case places spawn-ore
veins. Spawn ores enter the world only via §5 worldgen; spawn eggs only via §6 crafting.

---

## 10. Surprises / porting notes / MISSING-IN-PORT flags

1. **MISSING-IN-PORT (WGEN-005 core):** 103 of the 105 overworld/dim pool types have no
   port block — only c 50 Kraken (MB:158) and c 53 Dragon (MB:160) exist from the 98-pool,
   and none of the 7 rare-pool blocks exist. The random-pool worldgen itself (the
   nextInt(98)/nextInt(7) switches, OSW:370-801 / COG:36-467) is entirely absent; the port
   replaces it with 2 fixed features + the redesigned `ancient_dried_egg` (PN-010,
   PARITY_NOTES.md:121-129). The 11 Crystal-dim types are at full parity (OSCG:470-539).
2. **MISSING-IN-PORT (ITEM-062 remainder):** all 116 water-bucket recipes (§6) and all 3
   nine-part combine recipes (§6.2). No part blocks exist either (Mobzilla part block
   deliberately unregistered, MB:156-157).
3. **RESOLVED (D5) — the five "blocked" recipes are all expressible.** Vanilla 1.21.1
   HAS ender_dragon/snow_golem/iron_golem/wither spawn eggs (added in 1.20.5; verified
   against the client jar's lang index — the earlier "no vanilla egg" claim was wrong),
   and Criminal = BandP (audit correction WGEN-017, C7) so `CriminalEgg` →
   `band_p_spawn_egg`. All 116 water recipes generated in D5.
4. **Breaking a spawn ore spawns nothing** — the block only drops itself + 50% 5..9 XP
   (OGE:24-30). A common misconception; the port comment MB:167-168 already documents it.
5. **The full Mobzilla egg block is minable from worldgen** (common pool c 69, COG:352),
   bypassing the 9-part combine — while TheKing/TheQueen only place their *part* blocks
   (c 86 / c 97); their full eggs are craft-only. Asymmetric but original.
6. **MyCrystalCowSpawnBlock generates nowhere** (registered OSM:2005, recipe OSM:2757, in
   no pool switch — grep confirms only decl/reg/recipe references). Original-faithful
   dead worldgen entry; obtainable only via creative.
7. **Overworld rolls fewer veins than the custom dims**: `rate + nextInt(20)` (OSW:356)
   vs `rate + nextInt(30)` (COG:22). Both share the 1/20 "+30" bonus roll and the
   `LessOre/3` divisor. The Mining dimension additionally triples the whole pass when
   LessOre==0 (CP2:191-195).
8. **Y band is a discard filter, not a re-roll** (OSW:369 / COG:35): effective vein count
   is patchy × 78/128. `maxdepth=128` is unreachable (`nextInt(128)` ≤ 127) — the real
   band is **Y 50..127 inclusive**, which the port's height_range JSON (50..127) already
   encodes correctly.
9. The port's `less_ore_count` weights (2262/734 per 10000) reproduce the original
   *expected count* of exactly one common-pool member per chunk (derivation §5.4) — reuse
   these numbers verbatim for any additional pool members restored as data-driven features.
10. `MyEnderPearlBlock`/`MyEyeOfEnderBlock` are `OreGenericEgg`s (OSM:1972-1973), so these
    decorative blocks also pop 5..9 XP on break 50% of the time — original quirk, already
    preserved by the port class reuse (POGE:11-12).
11. Port lang deviation: existing crystal egg-ores are named `"<Mob> Egg"` instead of the
    original `"Ancient Dried <Mob> Spawn Egg"` (§7.4).
12. `AncientDriedEggBlock` internal inconsistency: MB:106-111 comment promises a 9-egg
    pool, ADE:48-56 implements 7 (missing baryonyx, camarasaurus) — worth a one-line fix
    or comment correction during D5.
13. Registry-name mapping for restored blocks: 1.7.10 `OreSpawn_XxxSpawnBlock` →
    port convention is snake_case; follow the existing precedent `kraken_spawn_block` /
    `dragon_spawn_block` (boss eggs) and `ore_<mob>` (crystal egg ores). For the 103 new
    pool blocks the `<mob>_spawn_block` form matches the two existing overworld members.
