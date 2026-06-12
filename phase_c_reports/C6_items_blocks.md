# Phase C slice 6 — Items & blocks (ITEM- series)

Scope: every `ITEM-` finding with no prior resolution — 33 DIVERGENT, 19 PARTIAL,
6 UNVERIFIED (58 handled), plus 7 MISSING findings skipped per ground rules
(ITEM-022, 029, 057, 060, 061, 063, 065 — Phase D scope, no resolution line).

Outcome: **53 FIXED, 5 PARTIAL** (020, 023, 053, 062, 064 — each remainder named
below with its owner), 0 still-UNVERIFIED.

Every original value below was re-verified in the 1.7.10 CFR source before the fix.

## Fixed findings

### Blocks (ores, storage, functional, plants)

| ID | Original (citation → value) | Old port | New port | Notes |
|----|------------------------------|----------|----------|-------|
| ITEM-001 | orig `OreRuby.java` / `OreAmethyst.java` ctors — plain ores, never explode | `ModBlocks` registered overworld ruby/amethyst as `OreCrystal` (1/3 explode-on-break) | new non-volatile `block/OreRuby.java` class; `OreCrystal` volatility reserved for crystal-dim CrystalCoal (`ModBlocks.java:14,106`) | Kyanite/pink-tourmaline ores are part of the port-added kyanite system (see Port-additions note below). |
| ITEM-002 | orig `OreRuby.java:21-22` 10.0/4.0 · `OreAmethyst` 10.0/4.0 · `OreUranium.java:24-25` 10.0/1.0 · `OreTitanium.java:24-25` 15.0/5.0 · `OreSalt.java:21-22` 5.0/2.0 | all 3.0/3.0 (salt 2.0/2.0) | per-block orig values (`ModBlocks.java:17-29`) | |
| ITEM-003 | orig `OreUranium.java:104` / `OreTitanium.java:104` — XP only below Y40 | Y-independent XP | `getExpDrop` gated on `pos.getY() < 40` (`block/OreUranium.java:26`, `OreTitanium.java:25`) | |
| ITEM-004 | orig `OreSpawnMain.java:1865-1868` — CrystalCoal light 9 / 6.0 / 20.0; CrystalCrystal light 6 / 12.0 / 40.0; TigersEye light 7 / 15.0 / 60.0 | ctor light misread as hardness: strength(0.6,6.0) light 8, strength(0.4,12.0) light 12, strength(0.5,15.0) light 12 | orig strength + light values (`ModBlocks.java:85+`) | Audit's diagnosis (parameter shift) confirmed in source. |
| ITEM-005 | orig `OreBasicStone.java:24+` — breaking spawns 15-20 mobs, no Silk Touch escape | 3-5 mobs, Silk Touch bypass invented | `15 + nextInt(6)`, bypass removed (`block/OreBasicStone.java:80`) | |
| ITEM-006 | orig `CrystalGrass.java:64-66` — `canSustainPlant` returns true unconditionally | no override (vanilla soil rules) | `canSustainPlant → TriState.TRUE` (`block/CrystalGrass.java:30`) | Was UNVERIFIED — verified divergent, fixed. |
| ITEM-007 | orig `BlockRuby.java:23-26` 4.0/4.0 light 6; **`BlockUranium.java:19-22` 5.0/5.0 light 3**; **`BlockTitanium.java:19-22` 5.0/5.0 light 7**; `BlockCrystal.java:17-20` 4.0/4.0 light 6 | 5.0/6.0, no light | per-block orig values (`ModBlocks.java:34-53`) | **Audit error:** uranium/titanium storage blocks are NOT 4.0/4.0 light 6 — orig is 5.0/5.0 with light 0.2→3 and 0.5→7 respectively. |
| ITEM-008 | orig mobzilla-variant `BlockRuby` — Strength on contact | FIRE_RESISTANCE | `MobEffects.DAMAGE_BOOST` 200t (`block/BlockRuby.java:17`) | Strength values aligned per ITEM-007. |
| ITEM-009 | orig `Lavafoam.java:23-27` — 5.0/5.0, slipperiness 1.1 | 0.5/0.5, default friction | `strength(5,5).friction(1.1)` (`ModBlocks.java:64`) | Push + speed-scaled damage were already ported. |
| ITEM-010 | orig BlockEnderPearl/BlockEyeOfEnder — gravel-type blocks, 50% chance of 5..9 XP on break | plain 3.0/3.0 blocks, no XP | registered as `OreGenericEgg` with gravel sound + XP roll (`ModBlocks.java:56`) | Was UNVERIFIED — verified divergent, fixed. |
| ITEM-011 | orig `BlockPizza.java:30+` — eat slice on right- AND left-click, 4 food / 0.2 sat | right-click only | `attack()` override shares the eat-slice path (`block/BlockPizza.java:59`) | |
| ITEM-012 | orig `BlockDuctTape.java` — repair on right- and left-click | right-click only | `attack()` override shares the repair path (`block/BlockDuctTape.java:60`) | |
| ITEM-013 | orig `RTPBlock.java:25` — teleports on stepOn | `entityInside` (never fires on a full cube) | `stepOn` override (`block/RTPBlock.java:39`) | |
| ITEM-014 | orig `MoleDirtBlock.java:39` — lowered collision box, entities sink | full-cube collision | `getCollisionShape` 14/16 box (`block/MoleDirtBlock.java:25`) | |
| ITEM-015 | orig `CrystalFurnace.java:46` — lit light 0.6 → level 9 | lit light 13 | level 9 (`ModBlocks.java:141-145`) | |
| ITEM-016 | orig `TileEntityCrystalFurnace.java:175` cook 150t; `:267-275` fuels CrystalCoal 20000 / CrystalTreeLog 800 / CrystalPlanks 400 | cook 100t, vanilla fuel lookup only (crystal fuels burn 0) | cook 150t + explicit crystal fuel table (`gui/CrystalFurnaceBlockEntity.java:35-37,221`) | |
| ITEM-017 | orig `BlockExtremeTorch.java:72-79` — Cephadrome spawn-spot search at random nearby offsets | spawned at the torch position | orig random-offset search ported (`block/BlockExtremeTorch.java:37`) | |
| ITEM-018 | orig `BlockCrystalTorch.java` — placeable on crystal blocks | vanilla `TorchBlock` support rules | support check accepts CrystalStone/CrystalGrass/CrystalTreeLog/CrystalPlanks (`block/BlockCrystalTorch.java:29`) | |
| ITEM-019 | orig `KrakenRepellent.java:58-75,82-124` — repel every frequent tick, radius 20, Kraken+Ant / Creeper | randomTick (~68 s average) | scheduled-tick requeue every 10t, orig radius + target sets (`block/RepellentBlock.java:23`) | |
| ITEM-021 | orig `OreGenericEgg.java:24-30` — 50% chance of **5 + nextInt(3) + nextInt(3) = 5..9 XP** | 50% chance of 5..9 extra egg-block items (infinite dupe) | `popExperience(5..9)` via `spawnAfterBreak` (`block/OreGenericEgg.java:33`) | **Audit error:** audit claimed "5-11 XP"; the orig range is 5..9. |
| ITEM-024 | orig BlockStrawberry/Rice/Radish/Quinoa/Tomato/Lettuce — per-stage `quantityDropped` counts | loot JSONs with invented seed+crop count ranges | 19 loot JSONs rewritten to orig per-stage counts (`loot_table/blocks/*.json`) | |
| ITEM-025 | orig `BlockCorn.java:49+` — stalk height capped 4-7 | unbounded upward growth | height cap `4 + random(4)` counting stalk below (`block/BlockCorn.java`) | |
| ITEM-026 | orig `BlockAppleLeaves.java:59` — night transform to ScaryLeaves only in DimensionID4 (Islands) | transformed in any dimension | gated on the Islands dimension (`block/BlockAppleLeaves.java`) | |
| ITEM-027 | orig `BlockDuplicatorLog.java:32-39` + `Trees.java:121-182` — incremental 1-block-per-tick tree build + 5×5 block duplication | re-interpreted sapling/item duplication | orig `DuplicatorTree` ported verbatim: soil check, 3-log trunk, apple-leaf cap + 3×3 ring, then 20/20-try block copy in the 5×5 (`block/BlockDuplicatorLog.java`) | `DUPLICATOR_TREE_ENABLE` config gate kept (orig `enableduplicatortree`). |

### Foods

| ID | Original (citation → value) | Old port | New port | Notes |
|----|------------------------------|----------|----------|-------|
| ITEM-028 | orig fish classes — SunFish Fire Res 6000t (`ItemSunFish.java:24-48`), FireFish 1200t (`ItemFireFish.java:26`), LavaEel 600t (`ItemLavaEel.java:26`), SparkFish 100t (`ItemSparkFish.java:26`), generic fish 25% Hunger **20t** (`ItemGenericFish.java:24-25`) | 600/600/1200/600/200t | orig durations; generic-fish roll restored to `nextInt(4)==1` (`item/Item*Fish.java`) | |

### Melee weapons

| ID | Original (citation → value) | Old port | New port | Notes |
|----|------------------------------|----------|----------|-------|
| ITEM-030 | orig `UltimateSword.java:44-59` — Unbreaking 3, Fire Aspect 2 (magic 5 / KB3 / Looting 3 correct) | Unbreaking 2, Fire Aspect 3 | values swapped back (`ModItems.java:158`) | |
| ITEM-031 | orig `Bertha.java:35-43` — KB5 / Bane1 / Fire Aspect 1; dur 9000; no reach bonus, no tooltip | Sweeping Edge 1; +2.0 reach + kill-counter tooltip (inventions) | Fire Aspect 1; reach/tooltip removed; Bertha-class `getMaxDamage`=9000 (`item/Bertha.java:38-48`, `ModItems.java:271`) | |
| ITEM-032 | orig `OreSpawnMain.java:1646` — Slice is a plain Bertha clone (KB5/Bane1/FireAspect1, dur 9000) | bespoke `Slice.java` with no baked enchants | `Slice.java` deleted; registered as a Bertha clone (`ModItems.java:276-278`) | |
| ITEM-033 | orig `Bertha.java:36-37` — Royal Guardian Sword bakes **Unbreaking 5**; dur 9000; hitType 2 | Sharpness 5; dur 10000 (tier) | Unbreaking 5, Bertha-class dur 9000 (`ModItems.java:282`) | |
| ITEM-034 | orig `Bertha.java:31,38,91-93` — Attitude Adjuster dur 9000, no baked enchants, hitType 3 | dur 2000 (HAMMY tier) | Bertha-class dur 9000 (`ModItems.java:302`) | |
| ITEM-035 | orig — Queen Battle Axe is an `UltimateSword`-class item: Looting 3 + Unbreaking 3, dur 3000, no shockwave | Bertha-based: Sharpness 5 + shockwave, dur 2200 | rebased on UltimateSword class, orig enchants, dur 3000 (`ModItems.java:293`) | |
| ITEM-036 | orig `UltimateSword.java:56-58` — Battle Axe: Looting 3 + Unbreaking 3, dur 3000 | KB5/Bane1/Sweeping1 + shockwave, dur 1500 | orig enchants, no shockwave, dur 3000 (`ModItems.java:288`) | |
| ITEM-037 | orig `UltimateSword.java:63-394` — Chainsaw: NO baked enchants; left-click AoE r=5 dmg 56; 11×16×11 wood/leaf crush on break; saw sound/particles | Bertha enchants + shockwave; no AoE/crush/sound | new `item/Chainsaw.java` with the orig AoE, tree-crush and sound loop; enchants/shockwave removed (`ModItems.java:298`) | |
| ITEM-038 | orig `NightmareSword.java:26,30-34` — dur 1200; Sharp1 / KB3 / Fire Aspect 1 | dur 1800; Sweeping 1 | dur 1200 override; Fire Aspect 1 (`item/NightmareSword.java`) | |
| ITEM-039 | orig `PoisonSword.java:50-59` — on-hit Poison + Wither + **Weakness** (10-19 s each) | Hunger instead of Weakness | `MobEffects.WEAKNESS` (`item/PoisonSword.java`) | |
| ITEM-040 | orig `ExperienceSword.java:30-139` — dur 1400; Sharp2 + **Unbreaking 3**; armor-XP inventory tick (`:63-103`) | dur 1300; Sharp2 + Looting 3; no armor tick | dur 1400, Unbreaking 3, armor-XP tick ported (`item/ExperienceSword.java`) | The armor tick also closes the gameplay gap behind MISSING ITEM-057 (whose own finding stays open per ground rules). |
| ITEM-041 | orig `BigHammer.java:25` — dur 9000 | dur 2000 (tier) | `getMaxDamage`=9000 (`item/BigHammer.java`) | |
| ITEM-042 | orig `MantisClaw.java:25` — dur 1000 | dur 2000 (tier) | `getMaxDamage`=1000 (`item/MantisClaw.java`) | |

### Ranged / gadgets / tools

| ID | Original (citation → value) | Old port | New port | Notes |
|----|------------------------------|----------|----------|-------|
| ITEM-043 | orig `UltimateBow.java:29-34,46-64` — baked Power **5** (fixed); instant fire at velocity 3.0; flat 1-in-4 crit; 1 durability/shot | Power = `UltimateBowDamage` config (10); charge-up model; full-pull crit | fixed Power 5; instant release at 3.0; `nextInt(4)==1` crit (`item/UltimateBow.java:26-58`) | `UltimateBowDamage` config still drives UltimateArrow damage per ENT-S-045 (orig behavior). |
| ITEM-044 | orig `SkateBow.java:39-48` — pull formula (f²+2f)/3 cap 1.75; creative OR Infinity skips ammo | no Infinity bypass; no pull cap | orig formula + Infinity bypass (`item/SkateBow.java:30-47`) | Was UNVERIFIED — verified divergent, fixed. Recipe remains MISSING ITEM-060. |
| ITEM-045 | orig `UltimateFishingRod.java:33` — baked **Unbreaking 2** only; dur 3000 | baked Luck of the Sea 3 + Lure 2 (inventions) | Unbreaking 2 (`item/UltimateFishingRod.java:26`) | Was UNVERIFIED — verified divergent, fixed. |
| ITEM-046 | orig `ItemMagicApple.java` — 80/19/1 tree rolls; King (diamond cap) / Queen (amethyst cap) | same rolls, condensed tree geometry | rolls/triggers verified line-by-line; condensed geometry kept and documented (audit option B; `item/ItemMagicApple.java` Javadoc) | |
| ITEM-047 | orig `InstantGarden.java:26-147` — 18×15 directional plot: 11 crop rows (radish/lettuce/carrot/potato/wheat/tomato/corn/strawberry), 3 cobble-lined water channels, sand+reeds row, melon row, 10-high clearing, GENERIC_EXPLODE | 11×11 farmland wheat/carrot/potato + fence | orig plot rebuilt row-for-row with directional `deltaX/deltaZ` placement (`item/InstantGarden.java`) | |
| ITEM-048 | orig `InstantShelter.java:28-150` — 7×7 directional shelter: cobble floor, plank walls, glass band, 2-high doorway, furnace/crafting/chest with 14-slot loot list (compass→chest) | 5×5 oak box with 5-item chest | orig structure + furnishing + exact chest contents (`item/InstantShelter.java`) | |
| ITEM-049 | orig `StepUp.java:26-99` (+Down/Across) — 8-way heading from yaw; stops at first obstruction (max 33); ExtremeTorch every 8; explosion sound + particles; creative keeps item | 4-way; fixed 33; vanilla torch every 3; stone sound; consumed in creative | orig 8-way `headingDeltas`, obstruction stop, ExtremeTorch cadence, explosion fx, creative bypass (`item/StepUp.java` + shared helpers) | |
| ITEM-050 | orig `ItemZooKeeper.java:44-45` — `setPersistenceRequired`; dur 1; 2 damage/use; smoke/poof/reddust + explode sound | `setNoAi(true)` freeze; dur 256 | orig persistence + dur 1 + effects (`item/ItemZooKeeper.java`, `ModItems.java`) | |
| ITEM-051 | orig `ItemSifter.java:35-471` — dur 600, stack 1; water 160-roll table (41 winners incl. mod fish/shoes/gems) + sand/gravel/dirt/grass 60-roll tables | dur 256; vanilla-only 100-roll; no water sifting | all five orig tables case-for-case incl. the grass-table fallthrough quirk (`item/ItemSifter.java`) | |
| ITEM-052 | orig `ItemWrench.java:29-80` — dur 100; AntRobot needs owned-or-HP<50% (then `setOwned`); kit damage = missing HP; kit re-spawn restores health/name/owned | dur 256; unconditional disassembly; no damage carry | orig guards + kit damage carry-through + kit spawn restore (`item/ItemWrench.java`, `ItemSpiderRobotKit.java`, kit durabilities = MobStats maxHealth) | |

### Armor

| ID | Original (citation → value) | Old port | New port | Notes |
|----|------------------------------|----------|----------|-------|
| ITEM-054 | orig `OreSpawnMain.java:1489-1502` `get_armorstats` + `addArmorMaterial:1770-1783` — durability is a per-slot multiplier (Ultimate 200, Mobzilla 1000, Royal 2000, Queen 1500…); enchantability separate (Ultimate 100, Mobzilla 150, Royal 200, Queen 150…) | durability multipliers ≈ orig/16; enchantability = orig durability | all 14 sets: orig durability multiplier in `ModItems.java`, orig enchantability in `ModArmorMaterials.java` | Kyanite set is a port addition outside the audited 14 (see Port-additions note). |
| ITEM-055 | orig `ArmorStats.java:13-20` — field order resp, aqua, prot, fire, blast, proj, unb, fall | record components mislabeled; invented `thorns` field | components renamed to orig order; `thorns` removed (`ArmorStats.java`) | Latent (only ENCHANT_TABLE used at runtime) but now future-safe. |
| ITEM-056 | orig `OreSpawnMain.java:1491-1502` — Ultimate: no Unbreaking; Mobzilla helmet: no Respiration/Aqua; Moth Scale: no Unbreaking; Lava Eel: Prot 3 + Fire Prot 2, no Unbreaking | four sets carried invented/wrong baked enchants | `ENCHANT_TABLE` corrected for all four (`item/ItemOreSpawnArmor.java`) | |
| ITEM-058 | orig `ItemOreSpawnArmor.java:343-358` — every worn royal/peacock/queen piece ticks the glide; **Peacock boots glide is NOT config-gated** (orig :347), Royal & Queen boots need `RoyalGlideEnable`; caps -0.1 / -0.25 | chestplate-only tick; everything behind `ROYAL_GLIDE_ENABLE` | per-piece tick; peacock ungated, royal/queen gated; orig fall caps (`item/ItemOreSpawnArmor.java:150+`) | |

### Recipes / config

| ID | Original (citation → value) | Old port | New port | Notes |
|----|------------------------------|----------|----------|-------|
| ITEM-059 | orig `OreSpawnMain.java:3092,3094` — ore smelts to a **nugget**, XP 0.3 | smelted to a full ingot, XP 0.7 | `uranium_nugget_smelting.json` / `titanium_nugget_smelting.json` (old ingot-smelting JSONs deleted) | Ingot = 9 nuggets via crafting, as orig. |

## VERIFIED-CORRECT / audit errors

No finding resolved as VERIFIED-CORRECT outright, but four audit claims were
wrong and the fixes follow the source, not the audit:

- **ITEM-007** — audit: all gem storage blocks 4.0/4.0 light 6. Orig
  `BlockUranium.java:19-22` and `BlockTitanium.java:19-22` are **5.0/5.0** with
  light **0.2→3** and **0.5→7** respectively (cited in `ModBlocks.java:40-43`).
- **ITEM-021** — audit: orig drops "5-11 XP". Orig `OreGenericEgg.java:26` is
  `5 + nextInt(3) + nextInt(3)` = **5..9** XP.
- **ITEM-034 context** — the audit family treats "Hammy" as a distinct tier item;
  orig `MyHammy`'s display name is "Attitude Adjuster" (orig
  `OreSpawnMain.java` LanguageRegistry line), confirming both share the Bertha
  9000-durability class override.
- **ITEM-061 (skipped MISSING)** — audit: orig 3083-3085 craft "chest / piston"
  from crystal planks. Orig `:3084` is six planks in a 2×3 **door** pattern
  producing `Items.wooden_door` — there is no piston conversion. Recorded here
  for the Phase D implementer.

## ITEM-062 — bulk recipe correspondence (UNVERIFIED → PARTIAL)

Script: `phase_c_reports/scripts/recipe_diff.py`; full table:
`phase_c_reports/C6_recipe_diff.md`.

- 381 original registrations (189 shaped + 176 shapeless + 16 smelting) parsed
  from `orig OreSpawnMain.java`; shifted left/center/right shaped variants
  collapsed → **328 logical recipes**.
- **201 verified or fixed to match.** 59 recipe JSONs were rewritten from the
  original definitions (ultimate tools/armor back to titanium/uranium/iron,
  nightmare/rose/experience/poison/rat/fairy swords' real ingredients, magic
  apple redstone blocks, miners dream cactus/redstone/gunpowder, step items'
  gunpowder, repellents' dead-stink-bug/green-goo + extreme-torch cores, wrench
  iron, squid zooka 3 ink + 6 iron, instant garden/shelter, nether lost
  netherrack, experience catcher glass bottle, zoo cages iron **blocks**,
  cage empty iron+sticks, lapis armor from lapis **blocks**, mobzilla armor
  from godzilla scales, crystal workbench/sticks/torch back to shapeless,
  salt & popcorn back to **smelting**, tigers-eye smelting XP 0.3, all six
  fish→cooked-cod smeltings verified, duct tape item recipe restored).
- **16 invented recipe JSONs removed** (no original counterpart): ray gun
  crafting (orig only has a damaged-rod recharge), royal armor ×4 +
  royal guardian sword (orig royal gear is boss-drop only), ant/spider robot
  kits (wrench-only), pizza block, island block, lavafoam, extreme torch
  shaped variant, book-from-peacock, iron-door-from-string, duct_tape block
  recipe (replaced by the orig duct_tape_item recipe).
- **2 unfixable in data (Phase D):** the second Miners-Dream pattern variant
  (orig `:3364`) and the RayGun/SquidZooka damaged-item recharge recipes
  (orig `:3524,3527`) — vanilla recipe JSON cannot express max-damage
  wildcard ingredients.
- **125 originals have no port recipe (Phase D):** dominated by the ~110
  spawn-block + water-bucket → spawn-egg conversions (the orig `*SpawnBlock`
  ore blocks don't exist in the port yet) plus godzilla/king/queen part→spawn
  block assemblies, chest from crystal planks (ITEM-061), skate bow (ITEM-060),
  bucket from pink tourmaline ingots (orig `:3224`), cobweb from string
  (orig `:5115`), bed from crystal planks + peacock feathers (orig `:3291`),
  raw corn dog (orig `:3325`), sky-tree-log → planks (orig `:5363`), and the
  crystal-pink CageEmpty variant (orig `:5352`).
- **Port-additions kept (documented):** the kyanite/pink-tourmaline/extractor
  system (18 recipe JSONs: kyanite tools+armor, extractor, `orespawn:extracting`
  recipes, crystal-wood lumber/stick intermediates). In 1.7.10 "Kyanite" is the
  *display name* of CrystalStone and pink tourmaline of CrystalPinkIngot — the
  separate item system is a deliberate port addition already flagged by
  WGEN-024; its disposition belongs there, not to this slice.

## PARTIAL / deferred (owner)

- **ITEM-020** — 400-tick fuse, spawner+block-above clearing and the
  table-driven `nextInt(50)` roll are restored; only the generic (type 21) and
  ruby (type 22) dungeon builders exist so far, other indices fall back to
  generic. Remaining 48 structure builders → **WGEN-042 (Phase D)**.
- **ITEM-023** — item-based capture flow (ZooCageItem/EmptyCage/CagedMob)
  accepted as the documented modernization (this section is the documentation
  the audit asked for); a placed-cage block form that renders the captured mob
  → **Phase D** if block parity is demanded.
- **ITEM-053** — all projectile damage/velocity values verified number-by-number
  (ThrownRock ×12 types incl. type-8/12 explosions, WaterBall 2/5 + 1-in-10
  item drop restored, LaserBall 16/100 + immunity lists, IceBall/Acid/
  DeadIrukandji lineage, SunspotUrchin 3/6 + fire placement restored);
  invented LaserBall/IceBall cooldowns and the invented Coin item (+ loot
  table/model/lang/tab entries) removed; `ItemDeadIrukandji` throw behavior
  created. Remainder: **Shoes** (thrown Shoes entity ×4 variants) and
  **GameController** throwable → **Phase D** (new entities + renderers).
- **ITEM-062** — see section above; absent recipe families → **Phase D**.
- **ITEM-064** — `lessOre` now wired through the new
  `orespawn:less_ore_count` placement modifier (`LessOreCountPlacement`,
  registered in `ModWorldGen`): uranium/titanium/amethyst/salt veins ÷3
  (orig `OreSpawnWorld.java:807-848`), red-ant/termite troll blocks ÷2
  (orig `:857-870`). Mining-dimension density gating (orig
  `ChunkOreGenerator.java:26,473-545`) → **WGEN-011 (Phase D)**.

## Skipped (MISSING — Phase D, per ground rules)

ITEM-022 (RockBlock), ITEM-029 (special-food potion effects), ITEM-057
(Experience-armor set effect — note the orig logic lives in
`ExperienceSword.java:63-103` and was incidentally ported with ITEM-040),
ITEM-060 (Skate Bow recipe), ITEM-061 (crystal-planks conversions — see audit
error note above), ITEM-063 (dispenser behaviors), ITEM-065 (per-tier stat
overrides).

## Files changed

Java (blocks): `ModBlocks.java`, `block/OreRuby.java` (new), `OreBasicStone`,
`OreCrystal`, `OreCrystalCrystal`, `OreGenericEgg`, `OreTitanium`, `OreUranium`,
`BlockRuby`, `CrystalGrass`, `Lavafoam` (reg), `BlockPizza`, `BlockDuctTape`,
`RTPBlock`, `MoleDirtBlock`, `BlockExtremeTorch`, `BlockCrystalTorch`,
`RepellentBlock`, `BlockCorn`, `BlockAppleLeaves`, `BlockDuplicatorLog`,
`entity/RandomDungeonSpawnerBlockEntity`, `gui/CrystalFurnaceBlockEntity`.

Java (items): `ModItems.java`, `ModArmorMaterials.java`, `ArmorStats.java`,
`ModCreativeTabs.java`, `item/` — `UltimateSword`, `Bertha`, `NightmareSword`,
`PoisonSword`, `ExperienceSword`, `BigHammer`, `MantisClaw`, `Chainsaw` (new),
`UltimateBow`, `SkateBow`, `UltimateFishingRod`, `ItemMagicApple`,
`InstantGarden`, `InstantShelter`, `StepUp/StepDown/StepAccross`,
`ItemZooKeeper`, `ItemSifter`, `ItemWrench`, `ItemSpiderRobotKit`, `ItemRock`,
`ItemLaserBall`, `ItemIceBall`, `ItemNetherLost`, `ItemDeadIrukandji` (new),
`ItemOreSpawnArmor`, fish food items; deleted `Slice.java`, `ItemCoin.java`.

Java (entities, item-interaction scope only): `WaterBall` (item drop),
`SunspotUrchin` (fire placement).

Java (worldgen/config): `OreSpawnConfig.java`,
`world/LessOreCountPlacement.java` (new), `world/ModWorldGen.java`.

Data: 19 crop loot JSONs; 59 recipe JSONs rewritten + 2 added
(`duct_tape_item`, `popcorn_smelting`/`salt_smelting` renames,
`uranium_nugget_smelting`, `titanium_nugget_smelting`) + 16 deleted;
6 placed-feature JSONs (`less_ore_count`); `coin` model/loot/lang removed.

Tooling: `phase_c_reports/scripts/recipe_diff.py`, `recipe_fix.py` (+ diff
output `C6_recipe_diff.md`).

## Build status

`.\gradlew.bat build --console=plain` — **BUILD SUCCESSFUL** (2026-06-12).
