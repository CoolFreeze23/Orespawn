# 06 — Blocks & Items Audit (OreSpawn 1.7.10 → NeoForge 1.21.1)

Legend: ORIG = `reference_1_7_10_source/sources/danger/orespawn/`, PORT = `src/main/java/danger/orespawn/`.
1.7.10 light values are 0.0–1.0 (×15 = light level). Statuses: PORTED / PARTIAL / MISSING / DIVERGENT / UNVERIFIED.

## Blocks

### Ores (overworld)

| Block | ORIG hardness/res (file:line) | PORT hardness/res (file:line) | Behavior | Status |
|---|---|---|---|---|
| OreRuby | 10.0/4.0 `OreRuby.java:21-22`; drops 1-2 ruby + XP in-class | 3.0/3.0 `ModBlocks.java:19`; loot table | PORT uses `OreCrystal` class → **1/3 chance to EXPLODE on break** (`block/OreCrystal.java:49`); original OreRuby never exploded (only crystal-dim CrystalCoal did) | DIVERGENT |
| OreAmethyst | 10.0/4.0 `OreAmethyst.java` ctor; drops 1-2 + XP | 3.0/3.0 `ModBlocks.java:21` | Same wrong explosion behavior via `OreCrystal` | DIVERGENT |
| OreUranium | 10.0/1.0, light 0 `OreUranium.java:24-26`; sparkle on interact/tick; XP drops only below Y40 | 3.0/3.0 `ModBlocks.java:23`; sparkle `block/OreUranium.java:27` (DUST_PLUME) | Hardness/res changed; Y-level-gated XP drop lost (JSON loot is Y-independent) | DIVERGENT |
| OreTitanium | 15.0/5.0 `OreTitanium.java` ctor; sparkle; Y<40 XP | 3.0/3.0 `ModBlocks.java:25`; sparkle `block/OreTitanium.java:26` | Same as uranium | DIVERGENT |
| OreSalt | 5.0/2.0 `OreSalt.java:21-22`; damages ants on contact | 2.0/2.0 `ModBlocks.java:27`; ant damage in `block/OreSalt.java:15` (stepOn+entityInside) | Values diverge, behavior ported | DIVERGENT (values) |
| ORE_KYANITE / ORE_PINK_TOURMALINE | n/a (gems came from CrystalCrystal smelting) | 3.0/6.0 `ModBlocks.java:87,94`, also get OreCrystal explosion | Port-only additions ("Phase 10") | NEW (flag: explosion behavior inherited) |

### Crystal-dimension blocks

Original `OreCrystal`/`OreCrystalCrystal` ctor signature is `(id, lightValue, hardness, resistance)`. The port read these as `(hardness, resistance)` — a systematic parameter shift:

| Block | ORIG light / hard / res (OreSpawnMain.java) | PORT (ModBlocks.java) | Status |
|---|---|---|---|
| CrystalCoal | light 0.6(=9), hard 6.0, res 20.0 (`:1865`) | strength(0.6, 6.0), light 8 (`:72`) — hardness 10× too low, res 6 vs 20, light 8 vs 9 | DIVERGENT (param shift) |
| CrystalCrystal | light 0.4(=6), hard 12.0, res 40.0 (`:1867`) | strength(0.4, 12.0), light 12 (`:76`) | DIVERGENT (param shift) |
| TigersEye ore | light 0.5(=7.5), hard 15.0, res 60.0 (`:1868`) | strength(0.5, 15.0), light 12 (`:78`) | DIVERGENT (param shift) |
| CrystalStone | 2.0/10.0 (`:1864`) | 2.0/10.0 (`:70`) | PORTED |
| CrystalGrass | 0.6/2.0 (`:1866`) | 0.6/2.0 (`:74`), `block/CrystalGrass.java` | PORTED (plant-sustain UNVERIFIED) |
| CrystalPlanks (CrystalWood) | 1.5/4.0 (`:1869`) | 1.5/4.0 (`:80`) | PORTED |
| CrystalCrystal explode | volatile variant 1/10 explode on break (`OreCrystalCrystal.java`) | `block/OreCrystalCrystal.java` isVolatile 1/10; FIREWORK/FLAME sparkle | PORTED |
| CrystalRat/CrystalFairy | 2.5/14.0 (`:1875-1876`); break → 1-10 rats / 1-6 fairies | 2.5/14.0 (`:107,109`); same counts `block/OreBasicStone.java:55+` | PORTED |
| RedAntTroll/TermiteTroll | 2.5/14.0 (`:1877-1878`); break → **15-20** mobs (`OreBasicStone.java:24+`) | 2.5/14.0 (`:111,113`); spawns **3-5** (`block/OreBasicStone.java`), Silk Touch bypass ADDED | DIVERGENT (count nerf + new silk-touch escape) |

### Storage / decorative blocks

| Block | ORIG | PORT | Status |
|---|---|---|---|
| BlockRuby / BlockAmethyst | 4.0/4.0, light 0.4(=6) `BlockRuby.java:23-25` | 5.0/6.0, **no light** `ModBlocks.java:31,33` | DIVERGENT (both values + light removed) |
| BlockUranium / BlockTitanium | 4.0/4.0 family values (`BlockUranium.java`/`BlockTitanium.java`) | 5.0/6.0 `ModBlocks.java:35,37` | DIVERGENT |
| BlockMobzillaScale | BlockRuby variant: applies **Strength** effect on contact (`BlockRuby.java`) | applies **FIRE_RESISTANCE** (`block/BlockRuby.java:23+`, isMobzillaScale), 5.0/6.0 | DIVERGENT (wrong effect) |
| BlockCrystalPink / BlockTigersEye | BlockCrystal 4.0/4.0, light 0.4 (`BlockCrystal.java`) | 5.0/6.0, no light `ModBlocks.java:42,44` | DIVERGENT |
| Lavafoam | 5.0/5.0, **slipperiness 1.1** `Lavafoam.java:23-26`; pushes + speed-scaled damage | 0.5/0.5 `ModBlocks.java:52`; push/damage ported `block/Lavafoam.java:29`; no friction | DIVERGENT (hardness 10×, slipperiness lost) |
| BlockEnderPearl / BlockEyeOfEnder | registered in OreSpawnMain | 3.0/3.0 `ModBlocks.java:46,48` | PORTED (values UNVERIFIED vs orig ctor) |

### Functional blocks

| Block | ORIG | PORT | Status |
|---|---|---|---|
| BlockPizza | default hardness; eat slice via right- **and left-click**, 4 food/0.2 sat per slice (`BlockPizza.java:30+`) | 0.5/0.5; BITES property; right-click only, 4/0.2 (`block/BlockPizza.java:34`) | PARTIAL (left-click eat missing) |
| BlockDuctTape | repairs held item on right/left-click (`BlockDuctTape.java`) | USES property, right-click repair (`block/BlockDuctTape.java`) | PARTIAL (left-click missing) |
| RTPBlock | teleports on **stepOn** (`func_149724_b`, `RTPBlock.java:25`); explosion fx | `entityInside` (`block/RTPBlock.java:33`); 1.5/6.0 `ModBlocks.java:62` — full-cube block may never fire entityInside | DIVERGENT (trigger likely broken) |
| MoleDirtBlock | hardness 0.6 (`OreSpawnMain.java:1883`); random-despawn; slows entities; lowered collision box (`MoleDirtBlock.java:39`) | 0.6 `ModBlocks.java:66`; despawn+slow ported (`block/MoleDirtBlock.java:22`); full-cube collision | PARTIAL (collision shape) |
| CrystalWorkbench | 1.0/5.0 (`:1870`); opens GUI 1 (`CrystalWorkbench.java`) | 1.0/5.0 `ModBlocks.java:117`; opens `CrystalWorkbenchMenu` (`block/CrystalWorkbenchBlock.java:29`) | PORTED |
| CrystalFurnace (block) | 2.0/10.0 (`:1871`); active light 0.6(=9) (`CrystalFurnace.java:48`) | 2.0/10.0 `ModBlocks.java:119`; LIT light 13 (`:122`) | PARTIAL (lit light 13 vs 9) |
| CrystalFurnace (BE) | cook 150t (`TileEntityCrystalFurnace.java:175`); custom fuels: CrystalCoal 20000, CrystalTreeLog 800, CrystalPlanks 400 | cook **100t** (`gui/CrystalFurnaceBlockEntity.java:45` CRYSTAL_SMELT_DURATION_TICKS=100); fuel = vanilla `getBurnTime(SMELTING)` only — crystal fuels have no registered burn time | DIVERGENT (speed) + PARTIAL (custom fuels unusable) |
| ExtremeTorch | light 1.0(=15) (`OreSpawnMain.java:1927`); summons Cephadrome **randomly nearby** when on EyeOfEnder block (`BlockExtremeTorch.java`) | light 15 `ModBlocks.java:126`; summons at torch pos (`block/BlockExtremeTorch.java:41`) | PARTIAL (spawn position) |
| CrystalTorch | custom canPlaceTorchOn allowing crystal blocks (`BlockCrystalTorch.java`) | std TorchBlock, particles ported (`block/BlockCrystalTorch.java:24`); placement logic absent | PARTIAL |
| KrakenRepellent | BlockTorch; repels Kraken+EntityAnt within 20 blocks, force ∝ distance (`KrakenRepellent.java:82-124`) | `RepellentBlock` predicate repel on **randomTick** (`block/RepellentBlock.java:31,44-61`), light 12 `ModBlocks.java:132` | PARTIAL (repel cadence: randomTick ≈ every ~68s avg vs original frequent tick; radius/targets need exact match) |
| CreeperRepellent | analogous (`CreeperRepellent.java`) | `ModBlocks.java:135` RepellentBlock | PARTIAL (same caveat) |
| DungeonSpawnerBlock | reed-like; after 400 ticks spawns 1 of **50** structures (`DungeonSpawnerBlock.java:46+`) | `RandomDungeonSpawnerBlockEntity`: 200 ticks, **2** dungeon variants (`block/entity/RandomDungeonSpawnerBlockEntity.java`), indestructible `ModBlocks.java:166` | DIVERGENT (50→2 variants, 400→200t) |
| KingSpawnerBlock | (skipped — covered elsewhere) | `ModBlocks.java:153-159` BossSpawnerBlock 50/1200 light 14 | — |
| IslandBlock | reed-like, random tick spawns Island/IslandToo then self-removes (`IslandBlock.java`) | `block/IslandBlock.java` equivalent | PORTED |
| PortalBlock | simple portal (`PortalBlock.java`) | `UtopiaPortalBlock` entityInside teleport, 80t cooldown (`block/UtopiaPortalBlock.java:39`), unbreakable `ModBlocks.java:176` | PORTED (modernized via DimensionTransition) |
| OreGenericEgg (kraken/dragon eggs) | 0.5/1.0 (`OreGenericEgg.java:18-19`); 50% chance **5-11 XP** on break | 0.5/1.0 `ModBlocks.java:147,149`; 50% chance drops **5-11 extra copies of the egg block** (`block/OreGenericEgg.java:38`) | DIVERGENT (XP → item dupe; exploitable) |
| RockBlock | block form of Rock mob (`RockBlock.java`) | no corresponding block found in ModBlocks | MISSING (mob may cover it — UNVERIFIED) |
| ZooCage (block aspects) | cage blocks/entities (`ZooCage.java`) | replaced by `ZooCageItem`/`EmptyCageItem`/`CagedMobItem` + EntityCage | PARTIAL (block form dropped, capture flow modernized) |

### Plants, crops, leaves, logs

| Block | ORIG | PORT | Status |
|---|---|---|---|
| AntBlock family (ant/red ant/termite/crystal termite/rainbow/unstable) | BlockGrass; random tick spawns 2-7 ants when not raining + sky visible (`AntBlock.java`, `CrystalAntBlock.java`) | same 2-7, not-raining + air-above check (`block/CrystalAntBlock.java:31`), `ModBlocks.java:89-94` | PORTED (sky-visible → air-above approximation) |
| BlockStrawberry / BlockRice / BlockRadish / BlockQuinoa / BlockTomato / BlockLettuce | BlockCrops 4-stage; class drop logic | `CropBlock` subclasses; drops via `loot_table/blocks/*.json` | PARTIAL (growth ported; per-stage drop counts UNVERIFIED against JSON) |
| BlockCorn | multi-block stalk, height capped 4-7, lower-stalk progression (`BlockCorn.java:49+`) | grows upward **without height cap** (`block/BlockCorn.java:64`) | DIVERGENT (infinite stacking) |
| BlockButterflyPlant / Moth / Mosquito / Firefly | spawn respective insects on random tick by time-of-day | ported (`block/BlockButterflyPlant.java` etc.) | PORTED |
| BlockExperiencePlant | XP plant | `block/BlockExperiencePlant`-equivalent + EXPERIENCE_SAPLING | PORTED |
| BlockAppleLeaves | drops apples/golden/magic apples; night transform to ScaryLeaves **only in DimensionID4** (`BlockAppleLeaves.java:59`) | transform at night in **any dimension** (`block/BlockAppleLeaves.java:53`); drops via loot | DIVERGENT (dimension lock lost) |
| BlockExperienceLeaves | XP bottles + orbs on break | ported (`block/BlockExperienceLeaves.java`) | PORTED |
| BlockScaryLeaves (+cherry/peach) | cherry/peach drops; day transform | ported, strength 0.15-0.2 `ModBlocks.java:185-191` | PORTED |
| BlockCrystalLeaves 1-3 | crystal leaves | `ModBlocks.java:194-198` | PORTED |
| BlockSkyTreeLog | cascading break of adjacent logs (`BlockSkyTreeLog.java`) | recursive `breakAdjacentLogs` (`block/BlockSkyTreeLog.java:10`) | PORTED |
| BlockDuplicatorLog | random tick calls `OreSpawnTrees.DuplicatorTree` (`BlockDuplicatorLog.java:37`) | sapling/item duplication + tree growth, gated `DUPLICATOR_TREE_ENABLE` (`block/BlockDuplicatorLog.java:48`) | PARTIAL (documented re-interpretation) |
| BlockCrystalTreeLog | special textures | ported | PORTED |
| MyBlockFlower (pink/black, blue/scary) | day/night variant swap | ported (`block/MyBlockFlower.java`) | PORTED |

## Items

### Food values (nutrition / saturation) — all verified against `OreSpawnMain.java:1709-1924` and `ModItems.java:370-463`

PORTED (exact match, 35 items): popcorn 1/0.5, buttered 2/0.6, buttered-salted 3/0.75, popcorn bag 10/1.25, butter 1/0.5, corn dog 16/2.5, raw corn dog 4/0.6, butter candy 4/0.5, cooked bacon 14/1.5, raw bacon 8/1.0, cooked crab 6/0.75, raw crab 4/0.25, cheese 4/0.5, salad 10/0.95, BLT 12/0.95, crabby patty 16/2.35, cooked peacock 12/1.4, raw peacock 6/0.7, strawberry 2/0.65, cherries 3/0.45, peach 4/0.55, crystal apple 5/0.85, heart 8/0.95, radish 2/0.45, rice 5/0.65, corn 6/0.75, quinoa 7/0.85, tomato 4/0.55, lettuce 3/0.45, fire/sun/lava-eel/spark fish and 6 generic fish nutrition/sat all match.

**Food effects — DIVERGENT/MISSING:**

| Item | ORIG effect (`ItemSunFish.java:24-48`, fish classes) | PORT | Status |
|---|---|---|---|
| Sun Fish | Fire Res **6000t (300s)** | 600t (30s) `item/ItemSunFish.java:19` | DIVERGENT (10×) |
| Fire Fish | Fire Res 1200t (`ItemFireFish.java:26`) | 600t `item/ItemFireFish.java:19` | DIVERGENT (½) |
| Lava Eel | Fire Res 600t (`ItemLavaEel.java:26`) | 1200t `item/ItemLavaEel.java:19` | DIVERGENT (2×) |
| Spark Fish | Fire Res 100t (`ItemSparkFish.java:26`) | 600t (`item/ItemSparkFish.java`) | DIVERGENT (6×) |
| Generic fish | 25% Hunger **20t** (`ItemGenericFish.java:24-25`) | 25% Hunger 200t `item/ItemGenericFish.java:18-19` | DIVERGENT (10×) |
| Butter Candy | Speed+Jump 2000t (`ItemSunFish.java:29-32`) | plain food `ModItems.java:408` | MISSING (effects) |
| Cooked Bacon | Regen+Strength 2000t (`:33-36`) | plain food `ModItems.java:410` | MISSING |
| Crystal Apple | Regen+Strength 3000t (`:37-40`) | plain food `ModItems.java:436` | MISSING |
| Heart ("Love") | Regen IV+Str III+FireRes III+Resist II 6000t, Speed/Jump 5000t (`:41-48`) | plain food `ModItems.java:438` | MISSING |

### Special swords / melee

| Item | ORIG (file:line) | PORT (file:line) | Status |
|---|---|---|---|
| Ultimate Sword | dur 3000; baked Sharp/Smite/Bane = magic(5), KB 3, Looting 3, Unbreaking 3, FireAspect **2** (`UltimateSword.java:44-59`, magic=5 `OreSpawnMain.java:1518`) | dur 3000 ✓; Sharp cfg(5) ✓, Smite 5 ✓, Bane 5 ✓, KB 3 ✓, Looting 3 ✓, **Unbreaking 2**, **FireAspect 3** (`item/UltimateSword.java:32-39`) | PARTIAL (2 levels swapped) |
| Big Bertha | dmg 496+4=500; dur **9000** (override `Bertha.java:31`); baked KB5/Bane1/**FireAspect1** (`Bertha.java:35-43`); BerthaHit shockwave on swing; PvP block cfg | 500 ✓ (tier 496+3+1); dur 9000 (tier); KB5/Bane1/**SweepingEdge1** (`ModItems.java:272-275`); shockwave ✓ (`item/Bertha.java:49`); +2.0 reach ADDED; kill-counter tooltip ADDED | PARTIAL (FireAspect→Sweeping; reach added) |
| Slice | Bertha clone, dur 9000, same enchants | `item/Slice.java`, no baked enchants passed (`ModItems.java:276-278`) | DIVERGENT (baked enchants missing) |
| Royal Guardian Sword | dmg 746+4=750; baked **Unbreaking 5** (`Bertha.java:36-37`, field_77347_r=unbreaking); hitType 2; dur 9000 override | 750 ✓; **SHARPNESS 5** (`ModItems.java:279-282`); hitType 2 ✓; dur 10000 (tier) | DIVERGENT (enchant identity + durability) |
| Attitude Adjuster (Hammy) | dmg 82+4=86; no baked enchants; hitType 3; dur 9000 | 86 ✓; none ✓; hitType 3 ✓; dur 2000 (tier) | PARTIAL (durability 9000→2000) |
| Queen Battle Axe | dmg 662+4=666; UltimateSword class → Battle-Axe enchants Looting3+Unbreaking3, dur 3000 | 666 ✓; Bertha class → SHARPNESS 5 + shockwave (`ModItems.java:287-290`); dur 2200 | DIVERGENT (enchants, added shockwave, durability) |
| Battle Axe | dmg 46+4=50; baked Looting3+Unbreaking3 (`UltimateSword.java:56-58`); dur 3000 | 50 ✓; KB5/Bane1/Sweeping1 via Bertha + shockwave (`ModItems.java:283-286`); dur 1500 | DIVERGENT |
| Chainsaw | dmg 56+4=60; **no enchants**; AoE damage on left-click (5-blk radius, dmg 56), crushes wood/leaves 11×16×11 on block break, saw sound+particles (`UltimateSword.java:63-394`) | 60 ✓; KB5/Bane1/Sweeping1 + Bertha shockwave (`ModItems.java:291-294`); no AoE, no tree-crushing, no sound | DIVERGENT (signature mechanics missing) |
| Nightmare Sword | dur **1200** override (`NightmareSword.java:26`); baked Sharp1/KB3/**FireAspect1** (`:30-34`) | dur 1800 (tier); Sharp1/KB3/**Sweeping1** (`item/NightmareSword.java:22-24`) | DIVERGENT (durability + enchant) |
| Poison Sword | dur 1300; Sharp1; on-hit Poison+Wither+**Weakness** 10-19s (`PoisonSword.java:50-59`) | dur 1300 ✓; Sharp1 ✓; Poison+Wither+**Hunger** (`item/PoisonSword.java:30-37`) | DIVERGENT (Weakness→Hunger) |
| Rat Sword | dur 1300; spawns 1-6 owned Rats on hit (`RatSword.java:41-54`) | same (`item/RatSword.java:19-37`) | PORTED |
| Fairy Sword | dur 1300; spawns 1-3 owned Fairies (`FairySword.java:41-54`) | same (`item/FairySword.java`) | PORTED |
| Experience Sword | dur **1400**; baked Sharp2+**Unbreaking3**; +10 XP per hit; bonus dmg = playerLevel/2; ticking grants XP while wearing Experience armor (`ExperienceSword.java:30-139`) | dur 1300 (tier); Sharp2+**Looting3** (`item/ExperienceSword.java:27-28`); XP/bonus dmg ✓ (`:33-61`); armor-XP tick MISSING | PARTIAL/DIVERGENT |
| Big Hammer | dur **9000** (`BigHammer.java:25`); launches target up (`:33-40`) | dur 2000 (tier); launch ✓ (`item/BigHammer.java:16-22`) | PARTIAL (durability) |
| Mantis Claw | dur **1000** (`MantisClaw.java:25`); lifesteal -1/+1 (`:33-41`) | dur 2000 (tier); lifesteal ✓ (`item/MantisClaw.java:16-23`) | PARTIAL (durability) |
| Rose Sword | EmeraldSword from roses | `ModItems.java:311` plain emerald-tier sword | PORTED |
| Emerald/Ruby/Amethyst/Ultimate/CrystalWood/CrystalStone/CrystalPink/TigersEye tools (5 each) | EnumHelper tiers, sword dmg = tier+4, pickaxe +2 | tiers verified in `ModToolTiers.java` = `WeaponStats` constants (all 15 stat sets match); sword total = tier+4 ✓, pickaxe +2 ✓, shovel +2.5 (orig +1), axe +6 (orig +3) | PORTED (sword/pick exact; shovel/axe follow 1.21 vanilla convention) |
| Kyanite tools/armor | n/a | `ModItems.java:243-252` etc. | NEW |

### Ranged / gadgets

| Item | ORIG | PORT | Status |
|---|---|---|---|
| Ultimate Bow | dur 1000; baked **Power 5**, Flame 3, Punch 2, Infinity 1 (`UltimateBow.java:29-34`); fires instantly at velocity 3.0, 1/4 crit (`:46-64`) | dur 1000 ✓; **Power = UltimateBowDamage cfg (10)** (`item/UltimateBow.java:28-32`), Flame3/Punch2/Inf1 ✓; requires charge-up, crit at full pull or 1/4 (`:37-58`) | DIVERGENT (Power 5→10; instant-fire → charge) |
| Skate Bow | recipe + custom arrows (`SkateBow.java`) | `item/SkateBow.java` dur 300 | UNVERIFIED (behavior not compared) |
| Ultimate Fishing Rod | dur 3000 | dur 3000 `ModItems.java:319` | UNVERIFIED (hook behavior) |
| Ray Gun | dur 50; LaserBall ×3 speed, recoil 1.5/0.3, fireworks sound 3.5/0.5; **no cooldown** (`ItemRayGun.java:26-46`) | dur 50 ✓; identical projectile/recoil/sound (`item/ItemRayGun.java:37-50`); +10t cooldown (documented) (`:22`) | PORTED (deliberate cooldown added) |
| Thunder Staff | dur 50; ThunderBolt ×3; self-repairs 1 dur/50t during thunderstorm (`ItemThunderStaff.java:28-56`) | identical + thunder sound added (`item/ItemThunderStaff.java:23-55`) | PORTED |
| SquidZooka | dur 100; launches Attack Squid at 3.6 speed, offsets 2.5/1.65 (`ItemSquidZooka.java:31-56`) | identical (`item/ItemSquidZooka.java:22-65`) | PORTED |
| Creeper Launcher | left-click creeper → launch 4.5 up, 6× particles, consumes 1 (`ItemCreeperLauncher.java:24-50`) | identical + right-click path added (`item/ItemCreeperLauncher.java:21-57`) | PORTED |
| Miner's Dream | 64-long 5×5 tunnel, cobble/crystal caps, torch every 5, ID-list junk (`ItemMinersDream.java:26-114`) | same geometry, tag-driven junk + ore preservation, MINERS_DREAM_EXPENSIVE diamond cost (`item/ItemMinersDream.java:48-191`) | PORTED (documented modernization) |
| Magic Apple | tree gen 80/19/1 rolls, King (diamond cap)/Queen (amethyst cap) spawns | same rolls + boss triggers, condensed geometry (`item/ItemMagicApple.java:69-141`) | PARTIAL (geometry condensed, triggers preserved) |
| Instant Garden | 18×15 plot: radish/lettuce/carrot/water/potato/wheat/tomato/corn/strawberry/reeds/melon rows, 10-high clear (`InstantGarden.java:26-147`) | 11×11 farmland with wheat/carrot/potato + fence (`item/InstantGarden.java:20-62`) | DIVERGENT (layout & crop set) |
| Instant Shelter | 7×7×~5 cobble floor/plank shelter, directional, chest+contents, door (`InstantShelter.java:28-150`) | 5×5×5 all-oak box, crafting table/furnace/chest(bread,torch,coal,wood pick,wood sword) (`item/InstantShelter.java:24-75`) | DIVERGENT (size/materials/loot) |
| StepUp/StepDown/StepAcross | 8-way (incl. diagonals); cobble path until obstruction (max 33); **ExtremeTorch** every 8; explosion fx (`StepUp.java:26-99` etc.) | 4-way cardinal; always 33; vanilla torch every 3; stone-place sound; consumes in creative too (`item/StepUp.java:22-60`) | DIVERGENT (diagonals, obstruction stop, torch type/interval) |
| Elevator | spawns "Hoverboard" entity (`ItemElevator.java:25-35`) | spawns ELEVATOR entity (`item/ItemElevator.java:20-36`); separate HoverboardItem added | PORTED |
| ZooKeeper | makes mob **persistent** (no despawn) `func_110163_bv` (`ItemZooKeeper.java:44`); dur 1, damage 2/use | sets **NoAi(true)** — freezes mob (`item/ItemZooKeeper.java:22`); dur 256 | DIVERGENT (persistence → AI-freeze) |
| Sifter | dur 600; sift water(160-table: 6 mod fish, 4 shoes, ruby/amethyst/diamond...), sand/gravel/dirt/grass 60-tables incl. salt/scales/mod flowers (`ItemSifter.java:35-471`) | dur 256; dirt/sand/gravel/soul-sand only, vanilla-only 100-roll table, no water sifting (`item/ItemSifter.java:24-69`) | DIVERGENT (loot tables gutted) |
| CritterCage | empty cage throws EntityCage; filled cage spawns stored mob, returns empty (`CritterCage.java:35-66+`) | EmptyCageItem capture + CagedMobItem with full NBT (`item/CagedMobItem.java:56-118`); ZOO_CAGE_2..10 legacy | PORTED (modernized, NBT-preserving) |
| Experience Catcher | converts ONE orb (≥3 XP, 80%) → XP bottle+string+stick; re-drops itself otherwise (`ExperienceCatcher.java:29-65`) | vacuums ALL orbs in r=3 → emeralds/gold/diamonds by XP math (`item/ExperienceCatcher.java:24-61`) | DIVERGENT (entirely different mechanic) |
| Wrench | dur 100; SpiderRobot free disassembly; AntRobot requires owned or HP<50%; kit keeps damage (`ItemWrench.java:29-80`) | dur 256; disassembles either unconditionally, no damage carry (`item/ItemWrench.java:23-46`) | PARTIAL (claim rules + kit damage lost) |
| IngotUranium / IngotTitanium | plain items (`IngotUranium.java`) | plain items (port comment claims radiation; none implemented either side) | PORTED |
| ItemRock ×12 / Water-, Laser-, Ice-Ball / Acid / Irukandji / SunspotUrchin / NetherLost / robot kits / Coin / shoes / game controller / spawn eggs | various | registered with same stack sizes; behavior classes exist | UNVERIFIED (per-projectile damage values not compared) |

## Tool/Armor tiers

### Tool tiers — `WeaponStats` (PORT `WeaponStats.java`) vs `OreSpawnMain.java:1503-1517`

All 15 stat sets (harvestLevel/maxUses/efficiency/damage/enchantability) verified identical: ULTIMATE 10/3000/15/36/100, NIGHTMARE 3/1800/12/26/60, BERTHA 3/9000/15/496/100, CRYSTALWOOD 2/300/3/2/15, CRYSTALSTONE 3/800/6/5/45, PINK 4/1100/10/7/65, TIGERSEYE 4/1600/12/8/75, RUBY 5/1500/11/16/85, AMETHYST 4/2000/11/11/70, EMERALD 3/1300/10/6/75, ROYAL 3/10000/15/746/150, HAMMY 5/2000/15/82/100, BATTLE 3/1500/15/46/75, CHAINSAW 3/1500/10/56/75, QUEENBATTLE 3/2200/15/662/100. **PORTED** (`ModToolTiers.java` consumes these). Note: original config could override every stat (`get_weaponstats`); port hardcodes — see Config.

### Armor — `ArmorStats`/`ModArmorMaterials` vs `OreSpawnMain.java:1491-1502,1770-1783`

- **Defense per piece: all 14 sets PORTED** (e.g. Ultimate 6/12/10/6, Royal 8/14/12/8, Queen 9/16/14/9 — `ModArmorMaterials.java:43-98` defenseMap matches head/chest/leg/boot exactly).
- **Durability: DIVERGENT for all 14 sets.** ORIG `EnumHelper.addArmorMaterial(durability)` is a per-slot multiplier (helmet×11, chest×16, legs×15, boots×13): Ultimate helmet = 200×11 = **2200**. PORT `ModItems.java:529-736` uses multipliers ≈ orig/16 (Ultimate 13 → helmet **143**; Royal 125 vs 2000 → chest 2000 vs 32000; Mobzilla 63 vs 1000; Queen 94 vs 1500). Port armor has ~1/15th the original durability. The comment at `ModArmorMaterials.java:37-41` claims the opposite of what `ModItems` actually does.
- **Enchantability: DIVERGENT for all 14 sets.** PORT passes original *durability* as `ArmorMaterial` enchantmentValue (`ModArmorMaterials.java:44` Ultimate=200 vs orig ench 100; `:89` Royal=2000 vs 200; `:48` Mobzilla=1000 vs 150; `:97` Queen=1500 vs 150; Amethyst 100 vs 40, etc.).
- **Toughness/knockback resistance** (`2.0-4.0F / 0.1F` on top tiers): port-only additions (no 1.7.10 equivalent) — NEW.
- **Port `ArmorStats.java:10-17` record field names are positionally mislabeled** vs `ArmorStats.java:13-20` (orig order: resp, aqua, prot, fire, blast, proj, unb, fall — port names fireProtection/blastProtection/.../thorns hold the wrong values). Values themselves copied correctly in original order; only the hardcoded `ENCHANT_TABLE` is used at runtime.

### Baked armor enchants — `ItemOreSpawnArmor` ENCHANT_TABLE (`item/ItemOreSpawnArmor.java:28-117`) vs orig per-stat enchants (`ItemOreSpawnArmor.java:81-153` + stats `OreSpawnMain.java:1491-1502`)

| Set | Verdict |
|---|---|
| Royal | PORTED (Prot/Fire/Blast/Proj 10, Unb 5, Resp 1, Aqua 2, Fall 10 — exact) |
| Peacock | PORTED (Feather Falling 10 only) |
| Lapis | PORTED (Prot1/Proj1/Resp1/Aqua1) |
| Experience | PORTED (Prot2/Blast1/Fall1) |
| Ultimate | DIVERGENT: port adds Unbreaking 3 (orig 0); rest exact |
| Mobzilla | DIVERGENT: port adds Resp 1/Aqua 2 (orig 0/0); rest exact |
| Moth Scale | DIVERGENT: port adds Unbreaking 3 (orig 0); Prot3/Fire3/Blast3/Fall5 ✓ |
| Lava Eel | DIVERGENT: Prot 2 vs 3, Fire Prot 2 MISSING, Unbreaking 3 added; Blast10/Resp1/Aqua2/Fall2 ✓ |
| Emerald/Ruby/Amethyst/Pink/TigersEye/Queen | PORTED (no enchants either side) |

### Set effects

- Royal/Peacock chest+boots glide cap -0.1, Queen -0.25, fall damage reset: PORTED (`item/ItemOreSpawnArmor.java:160-189` vs `ItemOreSpawnArmor.java:343-358`). Minor: orig Peacock boots glide ignored RoyalGlideEnable; port gates everything on it.
- Experience-armor XP generation (driven by Experience Sword tick, `ExperienceSword.java:63-103`): MISSING in port.

## Recipes

ORIG: 189 `addRecipe` + 176 `addShapelessRecipe` + 16 `addSmelting` = 381 registrations (many are mirrored left/center/right duplicates of one recipe). PORT: 236 JSONs in `data/orespawn/recipe/`.

- Spot-checks PORTED: `ultimate_sword.json` (+`_left`/`_right`) = T/U/I columns vs `OreSpawnMain.java:3150-3152` exact; ultimate tools, emerald/crystal-tier tools, slice (Bertha+iron `:3289`), attitude_adjuster (`:3281`), battle_axe (`:3282`), chainsaw (`:3283`), queen_battle_axe (`:3284`), nightmare_sword (`:3162`), experience/poison/rat/fairy swords (`:3187-3194`), crystal furnace (`:3082`), fish smeltings, bacon/corndog/peacock/crab cooking, salt_from_ore — present.
- **DIVERGENT — uranium/titanium smelting:** ORIG ore → **nugget** XP 0.3 (`OreSpawnMain.java:3092,3094`); PORT ore → full **ingot** XP 0.7 (`recipe/uranium_ingot_smelting.json:3-5`, titanium analog). 9× output inflation.
- **MISSING:** `skate_bow` recipe (orig `OreSpawnMain.java:3160`, no port JSON). Crystal planks→vanilla chest / piston conversions (`:3083-3085,3209`) not found as port JSONs (port has oak_door/iron_door conversions instead — DIVERGENT substitutions).
- NEW (no orig equivalent): kyanite set, extractor + extracting_*, cage_empty_crystal, ray_gun_repair, conversion recipes (pink_tourmaline↔ingot, lumber↔planks), random_dungeon.
- Full 381-line correspondence not exhaustively diffed: remaining recipe rows UNVERIFIED.

## Dispenser behaviors

ORIG has 8 dispenser behaviors: `MyDispenserBehaviorAcid`, `MyDispenserBehaviorIceball`, `MyDispenserBehaviorLaserball`, `MyDispenserBehaviorRock`, `MyDispenserBehaviorWDCharge`, `MyDispenserBehaviorDeadIrukandji`, `MyDispenserBehaviorArrow` (Irukandji arrow), `MyDispenserBehaviorSunspotUrchin`.

PORT `ModDispenserBehaviors.java:3-7` is an **empty stub** ("will be added when entity projectile constructors are finalized").

**Status: all 8 MISSING.**

## Config

ORIG: `OreSpawnMain.java` config (`config.get(...)`). PORT: `OreSpawnConfig.java`.

- PORTED: ~45 mob enable toggles (mosquito→dragonfly, `OreSpawnConfig.java:83-126`), MothraPeaceful, plus tweaks LessOre/LessLag/RatPlayerFriendly/RatPetFriendly/NightmareSize/IslandSpeedFactor/IslandSizeFactor/GinormousEmeraldTreeEnable/GuiOverlayEnable/UltimateSwordPvp/BigBerthaPvp/BoyfriendBroMode/DuplicatorTreeEnable/RoyalGlideEnable/DragonflyHorseFriendly/PlayNicely/MinersDreamExpensive/DisableOverworldDungeons/FullPowerKingEnable (`:141-159` vs `OreSpawnMain.java:1485-1488`), UltimateSwordMagic default 5 ✓, UltimateBowDamage default 10 ✓ (`:163-164` vs `:1518-1519`).
- PARTIAL: `LESS_ORE` defined but explicitly not wired (TODO `OreSpawnConfig.java:139-141`).
- MISSING: per-tier **weapon/armor/ore stat overrides** — orig `get_weaponstats`/`get_armorstats`/`get_orestats` exposed every number (`OreSpawnMain.java:1491-1517`); port hardcodes records.
- N/A (obsolete by platform): BaseBlockID/BaseItemID/dimension-ID/biome-ID settings.
- NEW: MOBZILLA_SINGLE_SPAWN, MOTHRA_REQUIRES_SPAWNER (documented as 1.7.10-behavior re-creations).

## Summary

| Status | Blocks | Items (incl. foods) | Tiers/Armor | Recipes | Dispensers | Config | Total |
|---|---|---|---|---|---|---|---|
| PORTED | 21 | 52 | 24 | ~20 spot-checked | 0 | 22 | ~139 |
| PARTIAL | 11 | 6 | 4 | — | 0 | 1 | 22 |
| DIVERGENT | 20 | 19 | 32 (14 durability + 14 enchantability + 4 enchant sets) | 2 | 0 | 0 | 73 |
| MISSING | 1 | 5 | 1 (Experience set effect) | 2 | 8 | 1 | 18 |
| UNVERIFIED | 4 | 14 | 0 | bulk un-diffed | 0 | 0 | 18+ |

Most serious: dispenser stub (8 missing), armor durability/enchantability systematic divergence (28 values), OreCrystal explosion applied to overworld ores, RTPBlock trigger likely dead, ExperienceCatcher and Sifter complete redesigns, uranium/titanium smelting inflation, OreGenericEgg item-dupe, special-sword enchant identity errors, dungeon spawner 50→2.
