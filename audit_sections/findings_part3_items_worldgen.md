# Findings — Blocks, Items, Recipes, Worldgen

Source: `audit_sections/06_blocks_items.md` (ITEM-###) and `audit_sections/07_worldgen.md` (WGEN-###).
ORIG = `reference_1_7_10_source/sources/danger/orespawn/`, PORT = `src/main/java/danger/orespawn/` + `src/main/resources/data/`.
Only MISSING / PARTIAL / DIVERGENT / UNVERIFIED items are listed; fully PORTED items are omitted.

---

# PART A — Blocks, Items, Recipes (file 06)

## Overworld ore blocks

### ITEM-001 — OreRuby/OreAmethyst: explosion behavior wrongly applied to overworld ores (systemic)
- **Status:** DIVERGENT
- **Original:** `OreRuby.java:21-22`, `OreAmethyst.java` ctor — overworld ruby/amethyst ores never exploded; only crystal-dimension CrystalCoal had volatile break behavior
- **Port:** `block/OreCrystal.java:49` — `ModBlocks` registers RUBY_ORE (`ModBlocks.java:19`), AMETHYST_ORE (`:21`), and ORE_KYANITE/ORE_PINK_TOURMALINE (`:87,94`) as `OreCrystal` → 1/3 chance to explode on break
- **Fix:** Register overworld ruby/amethyst (and kyanite/pink tourmaline) as plain `DropExperienceBlock`/dedicated non-volatile classes; reserve the `OreCrystal` explode-on-break behavior for crystal-dimension CrystalCoal only.

### ITEM-002 — Overworld ores: hardness/resistance values (systemic)
- **Status:** DIVERGENT
- **Original:** per-class ctors — values in table below
- **Port:** `ModBlocks.java:19,21,23,25,27` — values in table below
- **Fix:** Set `strength(hardness, resistance)` in `ModBlocks` to the original values:

| Block | ORIG hard/res (file:line) | PORT hard/res | Target |
|---|---|---|---|
| OreRuby | 10.0/4.0 (`OreRuby.java:21-22`) | 3.0/3.0 (`ModBlocks.java:19`) | 10.0/4.0 |
| OreAmethyst | 10.0/4.0 (`OreAmethyst.java` ctor) | 3.0/3.0 (`:21`) | 10.0/4.0 |
| OreUranium | 10.0/1.0 (`OreUranium.java:24-26`) | 3.0/3.0 (`:23`) | 10.0/1.0 |
| OreTitanium | 15.0/5.0 (`OreTitanium.java` ctor) | 3.0/3.0 (`:25`) | 15.0/5.0 |
| OreSalt | 5.0/2.0 (`OreSalt.java:21-22`) | 2.0/2.0 (`:27`) | 5.0/2.0 |

### ITEM-003 — OreUranium/OreTitanium: Y-level-gated XP drop lost
- **Status:** DIVERGENT
- **Original:** `OreUranium.java:24-26`, `OreTitanium.java` — XP drops only when broken below Y40
- **Port:** `loot_table/blocks/*.json` via `ModBlocks.java:23,25` — JSON loot/XP is Y-independent
- **Fix:** Override `getExpDrop` in `block/OreUranium.java`/`block/OreTitanium.java` to return XP only when `pos.getY() < 40` (return 0 otherwise).

## Crystal-dimension blocks

### ITEM-004 — Crystal ores: constructor parameter shift (systemic)
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:1865-1868` — ctor signature `(id, lightValue, hardness, resistance)`
- **Port:** `ModBlocks.java:72,76,78` — light/hardness values were read as `(hardness, resistance)`, dropping resistance and inventing light levels
- **Fix:** Re-register the three blocks with correct values from the table:

| Block | ORIG light/hard/res (`OreSpawnMain.java`) | PORT current | Target |
|---|---|---|---|
| CrystalCoal | light 9 (0.6), hard 6.0, res 20.0 (`:1865`) | strength(0.6, 6.0), light 8 (`:72`) | strength(6.0, 20.0), light 9 |
| CrystalCrystal | light 6 (0.4), hard 12.0, res 40.0 (`:1867`) | strength(0.4, 12.0), light 12 (`:76`) | strength(12.0, 40.0), light 6 |
| TigersEye ore | light 7 (0.5×15=7.5), hard 15.0, res 60.0 (`:1868`) | strength(0.5, 15.0), light 12 (`:78`) | strength(15.0, 60.0), light 7 or 8 |

### ITEM-005 — RedAntTroll/TermiteTroll: spawn count nerf + Silk Touch escape
- **Status:** DIVERGENT
- **Original:** `OreBasicStone.java:24+` (regs `OreSpawnMain.java:1877-1878`) — breaking spawns **15-20** mobs; no Silk Touch bypass
- **Port:** `block/OreBasicStone.java` (regs `ModBlocks.java:111,113`) — spawns **3-5**; Silk Touch bypass added
- **Fix:** Change spawn count to `15 + random.nextInt(6)` (15-20) and remove the Silk Touch bypass (or keep it only behind a documented config flag).

### ITEM-006 — CrystalGrass: plant-sustain behavior
- **Status:** UNVERIFIED
- **Original:** `OreSpawnMain.java:1866` + CrystalGrass class — should sustain crystal-dim plants (flowers/rice/quinoa)
- **Port:** `ModBlocks.java:74`, `block/CrystalGrass.java` — strength matches; whether crystal plants can be placed/survive on it was not compared
- **Fix:** Unverified because the audit only compared strength values. Resolve by testing `canSurvive`/`mayPlaceOn` for crystal flowers, rice, and quinoa on CrystalGrass and comparing to original `canPlaceBlockOn` logic.

## Storage / decorative blocks

### ITEM-007 — Gem storage blocks: strength + light emission (systemic)
- **Status:** DIVERGENT
- **Original:** `BlockRuby.java:23-25`, `BlockUranium.java`, `BlockTitanium.java`, `BlockCrystal.java` — all 4.0/4.0 with light 6 (0.4)
- **Port:** `ModBlocks.java:31,33,35,37,42,44` — 5.0/6.0, no light
- **Fix:** For BLOCK_RUBY, BLOCK_AMETHYST, BLOCK_URANIUM, BLOCK_TITANIUM, BLOCK_CRYSTAL_PINK, BLOCK_TIGERS_EYE: set `strength(4.0F, 4.0F).lightLevel(s -> 6)`.

### ITEM-008 — BlockMobzillaScale: wrong contact effect
- **Status:** DIVERGENT
- **Original:** `BlockRuby.java` (mobzilla variant) — applies **Strength** effect on contact
- **Port:** `block/BlockRuby.java:23+` (isMobzillaScale branch) — applies **FIRE_RESISTANCE**; also 5.0/6.0 strength
- **Fix:** Change the isMobzillaScale effect to `MobEffects.DAMAGE_BOOST` (Strength); also align strength per ITEM-007.

### ITEM-009 — Lavafoam: hardness 10× off, slipperiness lost
- **Status:** DIVERGENT
- **Original:** `Lavafoam.java:23-26` — 5.0/5.0, slipperiness **1.1**, pushes entities + speed-scaled damage
- **Port:** `ModBlocks.java:52`, `block/Lavafoam.java:29` — 0.5/0.5, no friction set; push/damage ported
- **Fix:** Set `strength(5.0F, 5.0F).friction(1.1F)` on the Lavafoam registration.

### ITEM-010 — BlockEnderPearl/BlockEyeOfEnder: strength values
- **Status:** UNVERIFIED
- **Original:** registered in `OreSpawnMain` — ctor values not extracted by the audit
- **Port:** `ModBlocks.java:46,48` — 3.0/3.0
- **Fix:** Unverified because original ctor hardness/resistance were never read. Resolve by opening the original BlockEnderPearl/BlockEyeOfEnder classes and comparing to the port's 3.0/3.0.

## Functional blocks

### ITEM-011 — BlockPizza: left-click eating missing
- **Status:** PARTIAL
- **Original:** `BlockPizza.java:30+` — eat slice via right- **and** left-click, 4 food/0.2 sat per slice
- **Port:** `block/BlockPizza.java:34` — right-click only (nutrition values match)
- **Fix:** Add a left-click handler (`attack()` override or `PlayerInteractEvent.LeftClickBlock` listener) that consumes a slice identically to the right-click path.

### ITEM-012 — BlockDuctTape: left-click repair missing
- **Status:** PARTIAL
- **Original:** `BlockDuctTape.java` — repairs held item on right- and left-click
- **Port:** `block/BlockDuctTape.java` — right-click repair only (USES property)
- **Fix:** Add left-click repair path mirroring the right-click logic (same event approach as ITEM-011).

### ITEM-013 — RTPBlock: teleport trigger likely dead
- **Status:** DIVERGENT
- **Original:** `RTPBlock.java:25` — teleports on **stepOn** (`func_149724_b`) with explosion fx
- **Port:** `block/RTPBlock.java:33`, `ModBlocks.java:62` — uses `entityInside`, but the block is a full cube so `entityInside` may never fire
- **Fix:** Replace `entityInside` with a `stepOn` override (`Block#stepOn`), which fires for entities standing on full cubes.

### ITEM-014 — MoleDirtBlock: collision shape
- **Status:** PARTIAL
- **Original:** `MoleDirtBlock.java:39` — lowered collision box (entities sink in); despawn + slow behavior
- **Port:** `block/MoleDirtBlock.java:22`, `ModBlocks.java:66` — despawn/slow ported, but full-cube collision
- **Fix:** Override `getCollisionShape` to return a lowered box (e.g. `Block.box(0,0,0,16,14,16)` matching the original offset) so entities sink like soul sand.

### ITEM-015 — CrystalFurnace (block): lit light level
- **Status:** PARTIAL
- **Original:** `CrystalFurnace.java:48` — active light 0.6 = level 9
- **Port:** `ModBlocks.java:122` — LIT light 13
- **Fix:** Change lit light level from 13 to 9 in the `lightLevel` lambda.

### ITEM-016 — CrystalFurnace (BE): cook speed + custom fuels unusable
- **Status:** DIVERGENT
- **Original:** `TileEntityCrystalFurnace.java:175` — cook time 150t; custom fuel values: CrystalCoal 20000, CrystalTreeLog 800, CrystalPlanks 400
- **Port:** `gui/CrystalFurnaceBlockEntity.java:45` — `CRYSTAL_SMELT_DURATION_TICKS=100`; fuel = vanilla `getBurnTime(SMELTING)` only, so crystal fuels have burn time 0
- **Fix:** Set cook duration to 150; register burn times (FurnaceFuel events / item `burnTime`) for CrystalCoal=20000, CrystalTreeLog=800, CrystalPlanks=400, or check them explicitly in the BE's fuel lookup.

### ITEM-017 — ExtremeTorch: Cephadrome spawn position
- **Status:** PARTIAL
- **Original:** `BlockExtremeTorch.java` — summons Cephadrome **randomly nearby** when torch is on an EyeOfEnder block
- **Port:** `block/BlockExtremeTorch.java:41` — summons at the torch position itself
- **Fix:** Offset the spawn position by a random nearby delta (match original random offsets) instead of spawning at the torch block.

### ITEM-018 — CrystalTorch: placement logic absent
- **Status:** PARTIAL
- **Original:** `BlockCrystalTorch.java` — custom `canPlaceTorchOn` allowing placement on crystal blocks
- **Port:** `block/BlockCrystalTorch.java:24` — standard `TorchBlock` (particles ported), no custom placement support
- **Fix:** Override `canSurvive`/support check to also accept crystal blocks (CrystalStone, CrystalCrystal, etc.) as valid supports.

### ITEM-019 — Kraken/Creeper Repellent: repel cadence too slow (systemic)
- **Status:** PARTIAL
- **Original:** `KrakenRepellent.java:82-124`, `CreeperRepellent.java` — repels Kraken+EntityAnt (resp. creepers) within 20 blocks every frequent tick, force ∝ distance
- **Port:** `block/RepellentBlock.java:31,44-61`, `ModBlocks.java:132,135` — repel runs on **randomTick** (~every 68s average)
- **Fix:** Convert to a BlockEntity ticker (or scheduled tick re-queue every 10-20t) applying the same predicate repel; verify radius=20 and target sets (Kraken+Ant / Creeper) match the originals.

### ITEM-020 — DungeonSpawnerBlock: 50 structures → 2, 400t → 200t
- **Status:** DIVERGENT
- **Original:** `DungeonSpawnerBlock.java:46+` — after 400 ticks spawns 1 of **50** structure types (FairyTree → RedAntHangout list)
- **Port:** `block/entity/RandomDungeonSpawnerBlockEntity.java:63-72`, `ModBlocks.java:166` — 200-tick countdown, then 1-in-4 ruby dungeon else generic dungeon (2 outcomes)
- **Fix:** Restore the 400-tick delay and expand the outcome pool toward the original 50-entry structure list as structures are ported (see WGEN-042); at minimum make the pool table-driven so new structures register into it.

### ITEM-021 — OreGenericEgg: XP bonus became item-dupe exploit
- **Status:** DIVERGENT
- **Original:** `OreGenericEgg.java:18-19` — 50% chance to drop **5-11 XP** on break
- **Port:** `block/OreGenericEgg.java:38`, `ModBlocks.java:147,149` — 50% chance drops **5-11 extra copies of the egg block** (infinite egg duplication)
- **Fix:** Replace the extra-item drop with `popExperience(level, pos, 5 + random.nextInt(7))`.

### ITEM-022 — RockBlock: block form absent
- **Status:** MISSING
- **Original:** `RockBlock.java` — block form of the Rock mob
- **Port:** absent — no corresponding block in `ModBlocks` (Rock mob/items may cover it — UNVERIFIED)
- **Fix:** Determine whether the Rock entity replicates the disguised-block behavior; if not, register a RockBlock with the original stats and the wake-to-mob behavior.

### ITEM-023 — ZooCage: block form dropped
- **Status:** PARTIAL
- **Original:** `ZooCage.java` — cage blocks/entities
- **Port:** `ZooCageItem`/`EmptyCageItem`/`CagedMobItem` + EntityCage — capture flow modernized, block form dropped
- **Fix:** Accept the item-based modernization as design; if block parity is required, add a placed-cage block that renders/holds the captured mob NBT. Document the decision either way.

## Plants, crops, leaves

### ITEM-024 — Crop blocks: per-stage drop counts unconfirmed
- **Status:** PARTIAL
- **Original:** `BlockStrawberry`/`BlockRice`/`BlockRadish`/`BlockQuinoa`/`BlockTomato`/`BlockLettuce` — 4-stage `BlockCrops` with in-class drop logic
- **Port:** `CropBlock` subclasses + `loot_table/blocks/*.json` — growth ported; per-stage drop counts UNVERIFIED against JSON
- **Fix:** Diff each original class's `quantityDropped`/stage logic against the six loot JSONs; set count ranges and age conditions in the JSONs to match.

### ITEM-025 — BlockCorn: stalk height cap lost
- **Status:** DIVERGENT
- **Original:** `BlockCorn.java:49+` — multi-block stalk, height capped at 4-7, lower-stalk progression
- **Port:** `block/BlockCorn.java:64` — grows upward without any height cap (infinite stacking)
- **Fix:** Add a height check in the growth path: count stalk blocks below and stop growth at a per-plant cap of `4 + random(4)` (4-7).

### ITEM-026 — BlockAppleLeaves: night transform dimension lock lost
- **Status:** DIVERGENT
- **Original:** `BlockAppleLeaves.java:59` — night transform to ScaryLeaves **only in DimensionID4** (Islands)
- **Port:** `block/BlockAppleLeaves.java:53` — transforms at night in **any** dimension
- **Fix:** Gate the transform on `level.dimension() == ModDimensions.ISLANDS` before swapping to ScaryLeaves.

### ITEM-027 — BlockDuplicatorLog: behavior re-interpreted
- **Status:** PARTIAL
- **Original:** `BlockDuplicatorLog.java:37` — random tick calls `OreSpawnTrees.DuplicatorTree`
- **Port:** `block/BlockDuplicatorLog.java:48` — sapling/item duplication + tree growth, gated by `DUPLICATOR_TREE_ENABLE` (documented re-interpretation)
- **Fix:** Port the original `Trees.DuplicatorTree` generator (see WGEN-044) and have the log's random tick invoke it, keeping the config gate.

## Food effects

### ITEM-028 — Fish foods: effect durations all wrong (systemic)
- **Status:** DIVERGENT
- **Original:** fish item classes — durations in table below
- **Port:** `item/Item*Fish.java` — durations in table below
- **Fix:** Set each effect duration to the original value:

| Item | ORIG (file:line) | PORT (file:line) | Target |
|---|---|---|---|
| Sun Fish | Fire Res 6000t (`ItemSunFish.java:24-48`) | 600t (`item/ItemSunFish.java:19`) | Fire Res 6000t |
| Fire Fish | Fire Res 1200t (`ItemFireFish.java:26`) | 600t (`item/ItemFireFish.java:19`) | Fire Res 1200t |
| Lava Eel | Fire Res 600t (`ItemLavaEel.java:26`) | 1200t (`item/ItemLavaEel.java:19`) | Fire Res 600t |
| Spark Fish | Fire Res 100t (`ItemSparkFish.java:26`) | 600t (`item/ItemSparkFish.java`) | Fire Res 100t |
| Generic fish | 25% Hunger 20t (`ItemGenericFish.java:24-25`) | 25% Hunger 200t (`item/ItemGenericFish.java:18-19`) | Hunger 20t (keep 25%) |

### ITEM-029 — Special foods: potion effects entirely missing (systemic)
- **Status:** MISSING
- **Original:** `ItemSunFish.java:29-48` (shared effect dispatcher) — effects in table below
- **Port:** `ModItems.java:408,410,436,438` — registered as plain foods with no effects
- **Fix:** Add `FoodProperties.effect(...)` entries (or `finishUsingItem` overrides) per table:

| Item | Required effects (ORIG `ItemSunFish.java`) |
|---|---|
| Butter Candy (`:29-32`) | Speed + Jump Boost, 2000t |
| Cooked Bacon (`:33-36`) | Regeneration + Strength, 2000t |
| Crystal Apple (`:37-40`) | Regeneration + Strength, 3000t |
| Heart "Love" (`:41-48`) | Regen IV + Strength III + Fire Res III + Resistance II 6000t; Speed + Jump Boost 5000t |

## Special swords / melee

### ITEM-030 — Ultimate Sword: Unbreaking/Fire Aspect levels swapped
- **Status:** PARTIAL
- **Original:** `UltimateSword.java:44-59` — baked Unbreaking **3**, Fire Aspect **2** (Sharp/Smite/Bane=magic 5, KB 3, Looting 3 all correct)
- **Port:** `item/UltimateSword.java:32-39` — Unbreaking **2**, Fire Aspect **3**
- **Fix:** Swap the two values: Unbreaking 3, Fire Aspect 2.

### ITEM-031 — Big Bertha: Fire Aspect replaced by Sweeping Edge; reach added
- **Status:** PARTIAL
- **Original:** `Bertha.java:35-43` — baked KB5 / Bane1 / **Fire Aspect 1**; dur 9000 (`:31`); no reach bonus
- **Port:** `ModItems.java:272-275`, `item/Bertha.java:49` — KB5 / Bane1 / **Sweeping Edge 1**; +2.0 reach and kill-counter tooltip added
- **Fix:** Replace Sweeping Edge 1 with Fire Aspect 1 in the baked enchant list; decide (and document) whether the added reach/tooltip stay as deliberate enhancements.

### ITEM-032 — Slice: baked enchants missing
- **Status:** DIVERGENT
- **Original:** `Bertha.java` clone — dur 9000, same baked enchants as Bertha (KB5/Bane1/FireAspect1)
- **Port:** `ModItems.java:276-278`, `item/Slice.java` — no baked enchants passed
- **Fix:** Pass the Bertha enchant set (KB5, Bane1, FireAspect1) to the Slice registration.

### ITEM-033 — Royal Guardian Sword: wrong enchant identity + durability
- **Status:** DIVERGENT
- **Original:** `Bertha.java:36-37` (field_77347_r = unbreaking) — baked **Unbreaking 5**; dur **9000** override; dmg 750, hitType 2
- **Port:** `ModItems.java:279-282` — baked **Sharpness 5**; dur 10000 (tier)
- **Fix:** Change baked enchant from Sharpness 5 to Unbreaking 5; override durability to 9000 (or adjust the ROYAL tier use only for this item).

### ITEM-034 — Attitude Adjuster: durability 9000 → 2000
- **Status:** PARTIAL
- **Original:** `Bertha.java:31` durability override path — dur **9000** (dmg 86, no enchants, hitType 3 all correct)
- **Port:** `ModItems.java` HAMMY tier — dur 2000
- **Fix:** Override item durability to 9000 (original used the Bertha 9000 override despite tier 2000).

### ITEM-035 — Queen Battle Axe: wrong class lineage (enchants, shockwave, durability)
- **Status:** DIVERGENT
- **Original:** `UltimateSword` class — baked Looting 3 + Unbreaking 3; dur 3000; no shockwave; dmg 666
- **Port:** `ModItems.java:287-290` — built on Bertha class: Sharpness 5 + swing shockwave; dur 2200 (tier)
- **Fix:** Rebase on the UltimateSword-style item: baked Looting3 + Unbreaking3, durability 3000, remove shockwave.

### ITEM-036 — Battle Axe: wrong enchants, added shockwave, durability
- **Status:** DIVERGENT
- **Original:** `UltimateSword.java:56-58` — baked Looting 3 + Unbreaking 3; dur 3000; dmg 50
- **Port:** `ModItems.java:283-286` — KB5/Bane1/Sweeping1 via Bertha class + shockwave; dur 1500
- **Fix:** Same as ITEM-035: Looting3 + Unbreaking3, durability 3000, no shockwave.

### ITEM-037 — Chainsaw: signature mechanics missing, wrong enchants
- **Status:** DIVERGENT
- **Original:** `UltimateSword.java:63-394` — **no** baked enchants; left-click AoE (5-block radius, dmg 56), crushes wood/leaves in 11×16×11 on block break, saw sound + particles
- **Port:** `ModItems.java:291-294` — KB5/Bane1/Sweeping1 + Bertha shockwave; no AoE, no tree-crushing, no sound
- **Fix:** Remove baked enchants and shockwave; implement left-click AoE damage (r=5, dmg 56), 11×16×11 wood/leaf crush on block break, and the saw sound/particle loop.

### ITEM-038 — Nightmare Sword: durability + enchant identity
- **Status:** DIVERGENT
- **Original:** `NightmareSword.java:26,30-34` — dur **1200** override; baked Sharp1 / KB3 / **Fire Aspect 1**
- **Port:** `item/NightmareSword.java:22-24` — dur 1800 (tier); Sharp1 / KB3 / **Sweeping 1**
- **Fix:** Override durability to 1200; replace Sweeping 1 with Fire Aspect 1.

### ITEM-039 — Poison Sword: Weakness replaced by Hunger
- **Status:** DIVERGENT
- **Original:** `PoisonSword.java:50-59` — on-hit Poison + Wither + **Weakness**, 10-19s each
- **Port:** `item/PoisonSword.java:30-37` — Poison + Wither + **Hunger**
- **Fix:** Replace `MobEffects.HUNGER` with `MobEffects.WEAKNESS` in the on-hit effect list.

### ITEM-040 — Experience Sword: enchant identity, durability, armor-XP tick
- **Status:** DIVERGENT
- **Original:** `ExperienceSword.java:30-139` — dur **1400**; baked Sharp2 + **Unbreaking 3**; +10 XP/hit; bonus dmg = playerLevel/2; inventory tick grants XP while wearing Experience armor (`:63-103`)
- **Port:** `item/ExperienceSword.java:27-28,33-61` — dur 1300 (tier); Sharp2 + **Looting 3**; XP/bonus dmg ported; armor-XP tick MISSING
- **Fix:** Durability 1400; swap Looting3 → Unbreaking3; implement `inventoryTick` granting XP when the holder wears Experience armor pieces (per original rates, see also ITEM-057).

### ITEM-041 — Big Hammer: durability 9000 → 2000
- **Status:** PARTIAL
- **Original:** `BigHammer.java:25` — dur **9000** (launch-up behavior ported correctly)
- **Port:** `item/BigHammer.java:16-22` — dur 2000 (tier)
- **Fix:** Override durability to 9000.

### ITEM-042 — Mantis Claw: durability 1000 → 2000
- **Status:** PARTIAL
- **Original:** `MantisClaw.java:25` — dur **1000** (lifesteal -1/+1 ported correctly)
- **Port:** `item/MantisClaw.java:16-23` — dur 2000 (tier)
- **Fix:** Override durability to 1000.

## Ranged / gadgets

### ITEM-043 — Ultimate Bow: Power level + fire model
- **Status:** DIVERGENT
- **Original:** `UltimateBow.java:29-34,46-64` — baked **Power 5** (fixed), Flame 3, Punch 2, Infinity 1; fires **instantly** at velocity 3.0, 1/4 crit chance
- **Port:** `item/UltimateBow.java:28-32,37-58` — Power = `UltimateBowDamage` config (**10**); requires charge-up, crit at full pull or 1/4
- **Fix:** Bake Power 5 (config-independent) or default `UltimateBowDamage` to 5; restore instant-fire at velocity 3.0 with 1/4 crit (no charge), or document the charge model as a deliberate change.

### ITEM-044 — Skate Bow: behavior not compared
- **Status:** UNVERIFIED
- **Original:** `SkateBow.java` — recipe + custom arrows
- **Port:** `item/SkateBow.java` — dur 300
- **Fix:** Unverified because the audit never diffed firing behavior/arrow type/durability. Resolve by comparing `SkateBow.java` (orig) projectile, velocity, and durability against the port class. Recipe is separately MISSING (ITEM-060).

### ITEM-045 — Ultimate Fishing Rod: hook behavior not compared
- **Status:** UNVERIFIED
- **Original:** dur 3000 fishing rod with possible custom hook logic
- **Port:** `ModItems.java:319` — dur 3000
- **Fix:** Unverified because only durability was checked. Resolve by diffing the original rod class's hook/loot behavior against the port item.

### ITEM-046 — Magic Apple: structure geometry condensed
- **Status:** PARTIAL
- **Original:** `ItemMagicApple` usage — tree gen 80/19/1 rolls, King (diamond cap) / Queen (amethyst cap) spawns
- **Port:** `item/ItemMagicApple.java:69-141` — same rolls + boss triggers, condensed tree geometry
- **Fix:** If 1:1 parity is required, port the original tree-build geometry; otherwise document the condensed geometry as accepted (rolls/triggers already match).

### ITEM-047 — Instant Garden: layout and crop set replaced
- **Status:** DIVERGENT
- **Original:** `InstantGarden.java:26-147` — 18×15 plot with radish/lettuce/carrot/water/potato/wheat/tomato/corn/strawberry/reeds/melon rows, 10-high clearing
- **Port:** `item/InstantGarden.java:20-62` — 11×11 farmland with wheat/carrot/potato + fence
- **Fix:** Rebuild to the original 18×15 row layout (including mod crops radish/lettuce/tomato/corn/strawberry, water channels, reeds, melon) with 10-high clear.

### ITEM-048 — Instant Shelter: size, materials, loot replaced
- **Status:** DIVERGENT
- **Original:** `InstantShelter.java:28-150` — 7×7×~5 cobble-floor/plank shelter, direction-aware, door, chest with original contents
- **Port:** `item/InstantShelter.java:24-75` — 5×5×5 all-oak box; crafting table/furnace/chest (bread, torch, coal, wood pick, wood sword)
- **Fix:** Rebuild to the original 7×7 directional cobble/plank design with door and the original chest loot list.

### ITEM-049 — StepUp/StepDown/StepAcross: pathing semantics changed
- **Status:** DIVERGENT
- **Original:** `StepUp.java:26-99` (and Down/Across) — 8-way including diagonals; cobble path stops at obstruction (max 33); **ExtremeTorch** every 8 blocks; explosion fx
- **Port:** `item/StepUp.java:22-60` — 4-way cardinal only; always 33 long; vanilla torch every 3; stone sound; consumes item even in creative
- **Fix:** Add diagonal directions (8-way), stop at first obstruction, place ExtremeTorch every 8 blocks, restore explosion fx, and skip consumption in creative.

### ITEM-050 — ZooKeeper: persistence became AI-freeze
- **Status:** DIVERGENT
- **Original:** `ItemZooKeeper.java:44` — makes mob **persistent** (`func_110163_bv`, no despawn); dur 1, damage 2/use
- **Port:** `item/ItemZooKeeper.java:22` — sets **NoAi(true)**, freezing the mob; dur 256
- **Fix:** Replace `setNoAi(true)` with `setPersistenceRequired()`; restore original durability semantics (tiny durability, 2 damage per use).

### ITEM-051 — Sifter: loot tables gutted
- **Status:** DIVERGENT
- **Original:** `ItemSifter.java:35-471` — dur 600; sifts **water** (160-entry table: 6 mod fish, 4 shoes, ruby/amethyst/diamond...) plus sand/gravel/dirt/grass 60-entry tables incl. salt/scales/mod flowers
- **Port:** `item/ItemSifter.java:24-69` — dur 256; dirt/sand/gravel/soul-sand only; vanilla-only 100-roll table; no water sifting
- **Fix:** Restore dur 600; re-implement the original per-substrate weighted tables (water 160-table + four 60-tables) as data-driven loot tables including mod items.

### ITEM-052 — Wrench: ownership rules + kit damage lost
- **Status:** PARTIAL
- **Original:** `ItemWrench.java:29-80` — dur 100; SpiderRobot disassembles freely; AntRobot requires owner or HP<50%; resulting kit keeps damage
- **Port:** `item/ItemWrench.java:23-46` — dur 256; disassembles either robot unconditionally; kit damage not carried
- **Fix:** Restore dur 100; add the AntRobot owner-or-HP<50% guard; copy remaining HP into the dropped kit's damage value.

### ITEM-053 — Projectile/misc items: damage values not compared
- **Status:** UNVERIFIED
- **Original:** ItemRock ×12, Water-/Laser-/Ice-Ball, Acid, Irukandji, SunspotUrchin, NetherLost, robot kits, Coin, shoes, game controller, spawn eggs
- **Port:** registered with same stack sizes; behavior classes exist
- **Fix:** Unverified because per-projectile damage/velocity values were never diffed. Resolve by comparing each projectile entity/item class's damage constants between codebases.

## Armor materials

### ITEM-054 — All 14 armor sets: durability ~1/15th and enchantability wrong (systemic)
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:1489-1502` (`get_armorstats(name, durability, head, chest, leg, boot, enchantability, ...)`) + `EnumHelper.addArmorMaterial` at `:1770-1783` — durability is a per-slot **multiplier** (helmet ×11, chest ×16, legs ×15, boots ×13; e.g. Ultimate helmet = 200×11 = 2200)
- **Port:** `ModItems.java:529-736` — durability multipliers ≈ orig/16 (Ultimate 13, Royal 125, Mobzilla 63, Queen 94); `ModArmorMaterials.java:43-98` — passes original *durability* as `enchantmentValue` (Ultimate 200, Royal 2000, Mobzilla 1000, Queen 1500, Amethyst 100...). Defense values are correct; the comment at `ModArmorMaterials.java:37-41` misstates what `ModItems` does.
- **Fix:** 1.21.1 `ArmorItem.Type.getDurability(mult)` uses the same per-slot bases (11/16/15/13), so set each set's durability multiplier to the **original durability value** and each `enchantmentValue` to the **original enchantability**, per this table:

| Set | Durability multiplier (target) | Enchantability (target) |
|---|---|---|
| Ultimate | 200 | 100 |
| Royal | 2000 | 200 |
| Queen | 1500 | 150 |
| Mobzilla | 1000 | 150 |
| Amethyst | 100 | 40 |
| Emerald | 60 | 40 |
| Experience | 70 | 50 |
| Moth Scale | 50 | 50 |
| Lava Eel | 40 | 35 |
| Pink | 50 | 40 |
| Tigers Eye | 80 | 55 |
| Peacock | 40 | 30 |
| Ruby | 90 | 40 |
| Lapis | 60 | 60 |

### ITEM-055 — ArmorStats record: positionally mislabeled fields
- **Status:** DIVERGENT
- **Original:** `ArmorStats.java:13-20` (orig) — enchant parameter order: resp, aqua, prot, fire, blast, proj, unb, fall
- **Port:** `ArmorStats.java:10-17` — field names (fireProtection/blastProtection/.../thorns) hold values from the wrong positions; values were copied in original order, and only the hardcoded `ENCHANT_TABLE` is used at runtime, so this is currently latent
- **Fix:** Rename the record components to the original order (respiration, aquaAffinity, protection, fireProtection, blastProtection, projectileProtection, unbreaking, featherFalling) so any future consumer reads correct values.

### ITEM-056 — Baked armor enchants: 4 sets diverge (systemic)
- **Status:** DIVERGENT
- **Original:** `ItemOreSpawnArmor.java:81-153` driven by stats at `OreSpawnMain.java:1491-1502` (order: resp, aqua, prot, fire, blast, proj, unb, fall)
- **Port:** `item/ItemOreSpawnArmor.java:28-117` `ENCHANT_TABLE`
- **Fix:** Correct the four rows of `ENCHANT_TABLE` to the original values (Royal/Peacock/Lapis/Experience and the no-enchant sets are already exact):

| Set | Correct values (ORIG) | Port error |
|---|---|---|
| Ultimate (`:1494`) | Resp2, Aqua3, Prot5, Fire5, Blast5, Proj5, Unb0, Fall3 | adds Unbreaking 3 → remove |
| Mobzilla (`:1498`) | Resp0, Aqua0, Prot10, Fire10, Blast10, Proj10, Unb5, Fall10 | adds Resp1/Aqua2 → remove |
| Moth Scale (`:1492`) | Prot3, Fire3, Blast3, Fall5, all else 0 | adds Unbreaking 3 → remove |
| Lava Eel (`:1493`) | Resp1, Aqua2, Prot3, Fire2, Blast10, Proj0, Unb0, Fall2 | Prot 2→3, add Fire Prot 2, remove Unbreaking 3 |

### ITEM-057 — Experience armor: XP-generation set effect missing
- **Status:** MISSING
- **Original:** `ExperienceSword.java:63-103` — ticking Experience Sword grants XP while the player wears Experience armor
- **Port:** absent — no XP-generation tick anywhere
- **Fix:** Implement the inventory-tick XP grant in the port's ExperienceSword (or a player-tick handler) scaling with worn Experience armor pieces, matching original tick rates/amounts. (Same root as ITEM-040.)

### ITEM-058 — Peacock boots glide: config gating changed
- **Status:** PARTIAL
- **Original:** `ItemOreSpawnArmor.java:343-358` — Peacock boots glide worked regardless of `RoyalGlideEnable`
- **Port:** `item/ItemOreSpawnArmor.java:160-189` — all glide (incl. Peacock) gated on `ROYAL_GLIDE_ENABLE`
- **Fix:** Exempt the Peacock-boots glide path from the `ROYAL_GLIDE_ENABLE` check (or document the unified gate as intentional).

## Recipes

### ITEM-059 — Uranium/Titanium smelting: 9× output inflation
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:3092,3094` — ore smelts to a **nugget**, XP 0.3
- **Port:** `recipe/uranium_ingot_smelting.json:3-5` (+ titanium analog) — ore smelts to a full **ingot**, XP 0.7
- **Fix:** Change both smelting JSONs' result to the nugget item and `"experience": 0.3` (ingot = 9 nuggets via crafting, as original).

### ITEM-060 — Skate Bow recipe absent
- **Status:** MISSING
- **Original:** `OreSpawnMain.java:3160` — Skate Bow crafting recipe
- **Port:** absent — no `skate_bow` JSON in `data/orespawn/recipe/`
- **Fix:** Add `recipe/skate_bow.json` reproducing the original shaped pattern/ingredients from `:3160`.

### ITEM-061 — Crystal planks → chest / piston conversions absent
- **Status:** MISSING
- **Original:** `OreSpawnMain.java:3083-3085,3209` — crystal planks craft into vanilla chest / piston
- **Port:** absent — no such JSONs; port instead has oak_door/iron_door conversions (divergent substitutions)
- **Fix:** Add recipe JSONs for chest and piston accepting crystal planks (tag or explicit item); review whether the door conversions should remain.

### ITEM-062 — Bulk recipe correspondence not diffed
- **Status:** UNVERIFIED
- **Original:** 381 registrations (189 shaped + 176 shapeless + 16 smelting), `OreSpawnMain.java:3000s`
- **Port:** 236 JSONs in `data/orespawn/recipe/` — only ~20 spot-checked
- **Fix:** Unverified because a full 381↔236 correspondence diff was never run. Resolve by scripting an extraction of all original `addRecipe`/`addShapelessRecipe`/`addSmelting` calls and matching each against a port JSON (mirrored left/center/right variants count as one).

## Dispenser behaviors

### ITEM-063 — All 8 dispenser behaviors unimplemented (systemic)
- **Status:** MISSING
- **Original:** 8 classes: `MyDispenserBehaviorAcid`, `MyDispenserBehaviorIceball`, `MyDispenserBehaviorLaserball`, `MyDispenserBehaviorRock`, `MyDispenserBehaviorWDCharge`, `MyDispenserBehaviorDeadIrukandji`, `MyDispenserBehaviorArrow` (Irukandji arrow), `MyDispenserBehaviorSunspotUrchin`
- **Port:** `ModDispenserBehaviors.java:3-7` — empty stub ("will be added when entity projectile constructors are finalized")
- **Fix:** Implement `DispenserBlock.registerBehavior` for all 8 items, each spawning its projectile entity with the original velocity/offsets: acid, iceball, laserball, rock, WD charge, dead irukandji, irukandji arrow, sunspot urchin.

## Config

### ITEM-064 — LESS_ORE defined but not wired
- **Status:** PARTIAL
- **Original:** `LessOre` config gates ore-generation multiplier (notably Mining dim 3× passes)
- **Port:** `OreSpawnConfig.java:139-141` — `LESS_ORE` exists with explicit TODO, affects nothing
- **Fix:** Wire `LESS_ORE` into the datapack/feature pipeline (e.g. select between normal and reduced placed-feature sets, and gate the Mining-dim density per WGEN-011).

### ITEM-065 — Per-tier weapon/armor/ore stat overrides missing
- **Status:** MISSING
- **Original:** `OreSpawnMain.java:1491-1517` — `get_weaponstats`/`get_armorstats`/`get_orestats` exposed every stat number to the config file
- **Port:** absent — `WeaponStats`/`ArmorStats` records hardcoded
- **Fix:** Either add config bindings that override the record defaults at registration time, or document hardcoding as a deliberate platform decision (datapacks cover ore stats).

---

# PART B — Worldgen, Dimensions, Structures (file 07)

## Overworld ore generation

### WGEN-001 — OreSpawn ore vein counts inflated, Y-floors extended to −64 (systemic)
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.get_orestats` defaults, executed by `ChunkOreGenerator.generateOresInChunk`
- **Port:** `data/orespawn/worldgen/placed_feature/ore_*.json` + `configured_feature/ore_*.json`
- **Fix:** Set placed-feature `count` and height range to original values (salt already exact):

| Ore | ORIG rate/clump/Y | PORT count/size/Y | Target |
|---|---|---|---|
| Ruby (`ore_ruby.json`) | 10 / 1 / Y0–50 | 8 / 2 / −64..50 | count 10, size 1, Y 0..50 |
| Uranium (`ore_uranium.json`) | 3 / 4 / Y0–30 | 5 / 4 / −64..30 | count 3, Y 0..30 |
| Titanium (`ore_titanium.json`) | 3 / 4 / Y0–20 | 6 / 4 / −64..20 | count 3, Y 0..20 |
| Amethyst (`ore_amethyst.json`) | 2 / 6 / Y0–25 | 4 / 6 / −64..25 | count 2, Y 0..25 |

(If the deepslate layer should keep ores, document the −64 floor as a deliberate 1.21 adaptation instead.)

### WGEN-002 — Ruby: lava-adjacency placement mechanic dropped
- **Status:** DIVERGENT
- **Original:** `OreSpawnWorld.generateRuby` — rubies placed adjacent to lava pockets via a lava-seek loop
- **Port:** `placed_feature/ore_ruby.json` — standard random ore placement
- **Fix:** Implement a custom feature (or use a `block_predicate`-filtered placement) that only places ruby ore next to lava source blocks, mirroring the lava-seek loop.

### WGEN-003 — Block-of-Ruby ore generation absent
- **Status:** MISSING
- **Original:** `OreSpawnMain` BlockRuby_stats — 1 vein / clump 2 / Y0–15
- **Port:** absent — no feature for ruby-block veins
- **Fix:** Add configured+placed features generating `block_ruby` veins: count 1, size 2, Y 0..15, and include them in `add_ores.json`.

### WGEN-004 — Vanilla-ore boost generation absent
- **Status:** MISSING
- **Original:** `ChunkOreGenerator.generateOresInChunk` — extra Diamond 4/6/Y0–30, Diamond Block 2/4/Y0–20, Emerald 4/6/Y0–40, Emerald Block 2/4/Y0–20, Gold 4/8/Y0–40, Gold Block 2/4/Y0–25 on top of vanilla
- **Port:** absent — only vanilla defaults generate
- **Fix:** Add six configured/placed features with the listed rate/clump/Y values and register via the `add_ores` biome modifier.

### WGEN-005 — SpawnOres system reduced from ~105 block types to 2
- **Status:** PARTIAL
- **Original:** `OreSpawnMain` SpawnOres stats + `ChunkOreGenerator` — 28 veins/chunk clump 4 Y50–128 (+30 veins on a 1/20 roll) over a pool of ≈105 spawn-block types (7 OreSpawn + 98 vanilla-mob)
- **Port:** `placed_feature/dragon_spawn_block.json`, `kraken_spawn_block.json` (each 1/24 chunks, Y −56..−10) + `add_ancient_dried_eggs.json` (1/12, Y −32..32)
- **Fix:** Decide scope: full parity needs the spawn-block pool restored (custom feature picking from the weighted 105-type pool at 28+/chunk, Y50–128); otherwise document the 2-boss-block reduction as a deliberate redesign.

### WGEN-006 — AntHill surface blocks never world-placed
- **Status:** PARTIAL
- **Original:** `OreSpawnWorld.addAnts` (called at `:323`, redfreq=4) — ~4%/chunk anthill blocks (black/red/rainbow/unstable) in plains-type biomes
- **Port:** ant blocks registered (`ModBlocks.java:291-306`) but no placement feature; black/red ants natural-spawn instead (`add_overworld_creatures.json`)
- **Fix:** Add an anthill placed feature (~4%/chunk, plains biomes) placing the four ant-block types; this also restores rainbow/unstable ant access (see WGEN-048).

### WGEN-007 — Wild crop patches (strawberry/corn/tomato) absent
- **Status:** MISSING
- **Original:** `OreSpawnWorld.generateSurface` — strawberry patches in forest biomes; corn (~1%) and tomato patches in plains
- **Port:** absent — crop blocks exist but have no wild generation
- **Fix:** Add three random-patch configured/placed features (strawberry → `#is_forest`; corn ~1%/chunk and tomato → plains) via biome modifiers.

## Utopia dimension

### WGEN-008 — Utopia: veggie patches missing, altar frequency changed
- **Status:** PARTIAL
- **Original:** `OreSpawnWorld.java:42-51` — veggie patches among features; King/Queen Altar 1/2000 chunk roll after tree passes (`:2550`)
- **Port:** trees/altars present but no veggie patches; altars use `structure_set/royal_altars.json` random_spread spacing 64 / separation 32
- **Fix:** Add Utopia veggie-patch features (same crop set as overworld wild gen); tune royal_altars spacing if 64/32 measurably differs from the 1/2000-roll density in practice.

### WGEN-009 — Utopia: cricket/frog spawn group sizes
- **Status:** UNVERIFIED
- **Original:** `BiomeGenUtopianPlains` default ctor — Cricket 5, Frog 5 (original min/max group sizes not extracted)
- **Port:** `utopia_plains.json` — cricket 5(4-6), frog 5(4-6)
- **Fix:** Unverified because the original group min/max were not read. Resolve by checking the `BiomeGenUtopianPlains` SpawnListEntry args for cricket/frog and aligning the JSON group sizes.

## Mining dimension

### WGEN-010 — Mining: vanilla dungeons/mineshafts/strongholds absent
- **Status:** PARTIAL
- **Original:** `ChunkProviderOreSpawn2` — vanilla dungeons ×8/chunk, mineshafts, strongholds, scattered features
- **Port:** `mining_biome.json` — caves/springs only; no vanilla structure sets apply to this biome
- **Fix:** Add `mining_biome` to the vanilla `has_structure` tags for mineshaft/stronghold, and add a monster-room-style feature at 8 attempts/chunk to the biome's features.

### WGEN-011 — Mining: 3× ore density lost
- **Status:** DIVERGENT
- **Original:** `ChunkProviderOreSpawn2` — `generateOresInChunk` called up to 3× when `LessOre==0`, plus extra `generateRuby` and extra diamond/gold passes
- **Port:** `mining_biome.json` — same 1× rates as overworld
- **Fix:** Create mining-specific placed features at 3× count (gated by `LESS_ORE`, see ITEM-064) plus the extra ruby/diamond/gold passes, and reference them only from `mining_biome.json`.

### WGEN-012 — Mining: dino/alien monster roster replaced
- **Status:** DIVERGENT
- **Original:** `ChunkProviderOreSpawn2.java:374-399` — Alosaurus 8(1-2), TRex 6(1-2), Nastysaurus 6(1-2), Pointysaurus 10(4-8), GammaMetroid 35(4-7), Alien 35(2-3), CaveFisher 35(4-8), Cryolophosaurus 26(4-7), Spyro 5(1-2) + biome defaults
- **Port:** `mining_biome.json` — rat 30(4-8), cave_fisher 10(1-3), molenoid 5(1-2), worms 8/4/2, creeping_horror 5(1-3), scorpion 3(1-2)
- **Fix:** Replace the monster list in `mining_biome.json` with the original roster/weights/groups (CaveFisher 35(4-8) etc.), keeping only entities that exist in the port; track unported entities separately.

### WGEN-013 — Mining: ambient spawns absent
- **Status:** MISSING
- **Original:** `ChunkProviderOreSpawn2.java:410-419` — VelocityRaptor 1(2-4), Dragonfly 2(1-3), Camarasaurus 1(2-4), Baryonyx 2(4-8)
- **Port:** `mining_biome.json` — firefly 5(1-3) only
- **Fix:** Add the four original ambient/creature entries with the listed weights/groups to `mining_biome.json` (for ported entities).

### WGEN-014 — Mining: BasiliskMaze/KyuubiDungeon/EnderKnight dungeon absent; BeeHive relocated
- **Status:** PARTIAL
- **Original:** `OreSpawnWorld:2031-2057` + Mining hooks — BasiliskMaze, KyuubiDungeon, BeeHive, ShadowDungeon, AlienWTF, EnderKnight, LeonNest, generic dungeon
- **Port:** only `shadow_dungeon`, `wtf_alien_dungeon`, `leonopteryx_nest` (each set 26/13) + generic dungeon; BeeHive moved to overworld forests
- **Fix:** Port BasiliskMaze (WGEN-037), KyuubiDungeon and EnderKnightDungeon (WGEN-042) as mining_biome structures; restore BeeHive to Mining or document relocation (WGEN-040).

## Village Mania dimension

### WGEN-015 — Village dimension generates no villages
- **Status:** MISSING
- **Original:** `MapGenMoreVillages.java:11-12` — spacing 9 / separation 7 (vanilla 32/8 → ~12× denser), enabled in dim 82 (`ChunkProviderOreSpawn3`); plus `BiomeManager.addVillageBiome` in `WorldProviderOreSpawn3`
- **Port:** absent — no `minecraft:villages` structure-set override, `orespawn:village_biome` not in any `has_structure/village` tag, no `data/minecraft/tags/worldgen` overrides at all
- **Fix:** Add `orespawn:village_biome` to `data/minecraft/tags/worldgen/biome/has_structure/village_plains.json` (override) and add a `data/minecraft/worldgen/structure_set/villages.json` override (or dimension-scoped set) with spacing 9 / separation 7.

### WGEN-016 — Village: dimension style is a no-op placeholder
- **Status:** PARTIAL
- **Original:** `ChunkProviderOreSpawn3` — overworld noise + lakes + vanilla dungeons/mineshafts/strongholds
- **Port:** `DimensionStyle.java:50-52` — style VILLAGE = pass-through ("identical to DEFAULT for now")
- **Fix:** Add lakes/springs and vanilla underground structures (mineshaft/stronghold/monster rooms) to `village_biome.json` / its tags, mirroring WGEN-010.

### WGEN-017 — Village: mob roster gaps and unverified weights
- **Status:** PARTIAL
- **Original:** `BiomeGenUtopianPlains.setVillageCreatures` — Robot1-5, Jeffery, SpiderDriver, Godzilla, Girlfriend, Boyfriend, cows, Butterfly, LunaMoth, Chipmunk, Cockateil, Tshirt, Coin, Criminal
- **Port:** `village_biome.json` + `dim_village_locals.json` — robots/cows/etc. present; **Jeffery, SpiderDriver, Godzilla, Criminal missing**; giant_robot/band_p added; robot weights UNVERIFIED vs original
- **Fix:** Add spawn entries for Jeffery, SpiderDriver, Godzilla, Criminal once those entities exist; diff robot_1-5 weights/groups against `setVillageCreatures` exact values and align.

### WGEN-018 — Village: DamselInDistress/SpiderHangout/RedAntHangout structures absent
- **Status:** PARTIAL
- **Original:** `OreSpawnWorld.java:118-128` — mosquitos, ants (redfreq 4), apple trees, generic dungeon, DamselInDistress, SpiderHangout, RedAntHangout
- **Port:** `greenhouse` (48/24), `robot_lab` (44/22), `white_house` (48/24) tagged village_biome + generic dungeon — the three original Village structures absent (the three present ones were Islands-dim structures, see WGEN-022)
- **Fix:** Port DamselInDistress, SpiderHangout, RedAntHangout as structures tagged `orespawn:village_biome` with sets approximating their original per-chunk roll densities.

## Islands dimension

### WGEN-019 — Islands: flat-plane terrain replaced by floating-islands noise
- **Status:** DIVERGENT
- **Original:** `ChunkProviderOreSpawn4` + `Island.java:64-79` — flat plane (bedrock y0 + dirt + grass); islands built dynamically by Island/IslandToo entities (small r3-6/d2-4, 1/40 large r6-10/d3-6)
- **Port:** `dimension/islands.json` — `minecraft:floating_islands` noise; island-builder entities additionally spawn (`dim_islands_locals.json`)
- **Fix:** Either restore the flat-plane generator (custom flat noise settings: bedrock+dirt+grass) so entity-built islands are the sole terrain, or remove/retune one of the two systems — currently both run, which neither matches original look nor static-noise intent.

### WGEN-020 — Islands: spawn weights not verified
- **Status:** UNVERIFIED
- **Original:** `setIslandCreatures` — full roster (Dragon, Stinky, CliffRacer, CloudShark, terrors, etc.); exact weights not extracted
- **Port:** `island_biome.json` + locals — full roster present with stated weights
- **Fix:** Unverified because original weight/group numbers were never read. Resolve by extracting `setIslandCreatures` SpawnListEntry args and diffing against the biome JSON.

### WGEN-021 — Islands: ~13 D4 structures absent
- **Status:** MISSING
- **Original:** `OreSpawnWorld.java:134-198` — D4Castle, D4GenericDungeon, D4EnderCastle, D4IncaPyramid, D4RobotLab, D4Mini, D4RubyDungeon, D4CephadromeAltar, D4Greenhouse, D4NightmareRookery, D4StinkyHouse, D4WhiteHouse, Pumpkin, D4Rainbow, D4CloudShark, UnstableAnts placement
- **Port:** absent — nothing tagged `island_biome` (RobotLab/Greenhouse/WhiteHouse were moved to Village)
- **Fix:** Port the D4 structure builders as jigsaw/legacy-piece structures tagged `orespawn:island_biome` with sets matching the original per-chunk roll rates; restore unstable-ant block placement.

### WGEN-022 — Greenhouse/RobotLab/WhiteHouse relocated Islands → Village
- **Status:** DIVERGENT
- **Original:** D4 (Islands) structures (`OreSpawnWorld.java:134-198`)
- **Port:** Village-dim structure sets 48/24, 44/22, 48/24
- **Fix:** Re-tag the three structures to `orespawn:island_biome` (or duplicate into both dims if the Village placement is desired), restoring Islands as their home.

## Crystal dimension

### WGEN-023 — Crystal spawn-block ores: 9 of 11 types are placeholders
- **Status:** PARTIAL
- **Original:** Crystal ore pass — pool of 11 spawn blocks (Urchin, Flounder, Skate, Rotator, Peacock, Fairy, DungeonBeast, Vortex, Rat, Whale, Irukandji), 25+rand(30)/chunk Y>45
- **Port:** `OreSpawnChunkGenerator.getSpawnBlockStates:527-542` — frequencies exact, but 9/11 types emit CRYSTAL_STONE placeholders; only CRYSTAL_FAIRY and CRYSTAL_RAT are real
- **Fix:** Register the 9 missing crystal spawn-block variants (Urchin, Flounder, Skate, Rotator, Peacock, DungeonBeast, Vortex, Whale, Irukandji) with break-to-spawn behavior and substitute them into `getSpawnBlockStates`.

### WGEN-024 — Crystal: extra kyanite/pink-tourmaline veins double-generate
- **Status:** DIVERGENT
- **Original:** n/a — Kyanite *is* CrystalStone; Pink Tourmaline/TigersEye exist only as column formations
- **Port:** `add_crystal_dim_ores.json` — injects `ore_kyanite` (6×size6, Y−32..80) and `ore_pink_tourmaline` (6×size6) as standard veins on top of the column generators
- **Fix:** Remove `add_crystal_dim_ores.json` (or the tourmaline entry at minimum) to eliminate the no-counterpart veins and tourmaline double-generation; keep kyanite only if it stays as a deliberate "Phase 10" addition — document it.

### WGEN-025 — Crystal structures: chest loot only approximated
- **Status:** DIVERGENT
- **Original:** WeightedRandomChestContent lists per structure (FairyTree/RotatorStation/Urchin/HauntedHouse/RoundRotator/BattleTower, maze chests)
- **Port:** `CrystalStructures.fillCrystalChest:838+` — inline ItemStack pickers approximating the loot
- **Fix:** Transcribe each original weighted chest list into a data-driven loot table (`loot_table/chests/crystal_*.json`) and reference them from the structure fill code.

### WGEN-026 — Crystal entry: empty-inventory requirement dropped (Termite portal)
- **Status:** DIVERGENT
- **Original:** `Termite.java` — travel requires empty hand **and completely empty inventory**
- **Port:** `EntityTermite.java:46-48` — inherits `EntityAnt.mobInteract:103-134` empty-hand check only
- **Fix:** Override `mobInteract` in `EntityTermite` to additionally require `player.getInventory()` be empty (the dimension's intended "bring nothing in" rule).

### WGEN-027 — Crystal: redundant structure JSONs risk double generation
- **Status:** DIVERGENT
- **Original:** n/a — maze/towers placed solely by chunk-generator code
- **Port:** `crystal_maze` structure_set (1/0) + `crystal_battle_tower` set (17/8) + placed_features `crystal_maze.json` (1/4), `crystal_battle_tower.json` (1/220), `crystal_tree*.json` exist **in addition to** the code paths (`CrystalStructures.java`, `CrystalMaze`)
- **Fix:** Delete the redundant structure-set/placed-feature JSONs (maze especially: every chunk via code + 1/4 feature + structure set) so each structure has exactly one placement mechanism.

## Chaos dimension

### WGEN-028 — Chaos: nether-style terrain replaced by overworld noise
- **Status:** DIVERGENT
- **Original:** `ChunkProviderOreSpawn6` — nether-noise terrain, 128 high, stone base with nether-style caverns, scraggly trees
- **Port:** `orespawn:inland` overworld noise, style CHAOS = pass-through (`DimensionStyle.java:48-49`); no scraggly trees
- **Fix:** Point `dimension/chaos.json` at nether-like noise settings (e.g. derived from `minecraft:nether` with stone palette, height 128) and add the scraggly-tree pass for CHAOS.

### WGEN-029 — Chaos: spawn roster gaps and unverified weights
- **Status:** PARTIAL
- **Original:** `setChaosCreatures` — ~55 entries
- **Port:** `chaos_biome.json` (37 monsters + others) + `dim_chaos_locals.json` — **Bee, Cassowary, Dragonfly, Peacock, StinkBug, Ostrich, cows, Hydrolisc missing**; ghosts/vampire_butterfly added; per-entry weights UNVERIFIED
- **Fix:** Add the eight missing entity spawn entries (for ported entities); extract original weights from `setChaosCreatures` and align the JSON.

### WGEN-030 — Chaos: veggie/ant features missing; challenge towers added
- **Status:** PARTIAL
- **Original:** `OreSpawnWorld.java:103-107` — butterflies/moths, veggies, ants ×2
- **Port:** generic dungeon 1/16 + `challenge_tower_king/queen` (36/18, no 1.7.10 counterpart — see WGEN-043)
- **Fix:** Add veggie-patch and ant-block features (×2 density) to `chaos_biome.json`, reusing the overworld features from WGEN-006/007.

### WGEN-031 — Chaos: sky/fog constants not compared
- **Status:** UNVERIFIED
- **Original:** `WorldProviderOreSpawn6` — custom fog/sky colors (constants not extracted)
- **Port:** `dimension_type/chaos.json` — `ambient_light: 0.3`, `has_raids: true`
- **Fix:** Unverified because the original provider's fog/sky color values were never read. Resolve by extracting `getFogColor`/sky color from `WorldProviderOreSpawn6` and configuring matching dimension special effects.

## Nether / End additions

### WGEN-032 — Nether: Lavafoam ore + ruby generation absent
- **Status:** PARTIAL
- **Original:** `OreSpawnWorld.generateNether` — Lavafoam ore, ruby, nether ants, nether mosquitos
- **Port:** `add_nether_spawns.json` (spawns only) — no lavafoam/ruby nether features
- **Fix:** Add nether-targeted configured/placed features for Lavafoam and ruby veins and register via a `#minecraft:is_nether` biome modifier.

### WGEN-033 — End: Hospital and Ender Castle structures absent
- **Status:** PARTIAL
- **Original:** `OreSpawnWorld.generateEnd` — End ants, End knights/reapers, Hospital, Ender Castle
- **Port:** `add_end_spawns.json` (spawns only) — no Hospital/EnderCastle structures
- **Fix:** Port Hospital and EnderCastle as structures tagged to End biomes with sets approximating original densities (see also WGEN-042).

## Structures & dungeons

### WGEN-034 — Generic Dungeon: spawner pool swapped, custom loot replaced by vanilla
- **Status:** DIVERGENT
- **Original:** `GenericDungeon.makeDungeon` + lists — spawner pool of 12 (Scorpion, Alien, Cryolophosaurus, WTF?, Kyuubi, Bee, CloudShark, LurkingTerror, TerribleTerror, Rotator, Rat, DungeonBeast); custom level1-5 chest lists
- **Port:** `world/GenericDungeon.java:22-34,121-126` — pool of 11 (Alien, CaveFisher, DungeonBeast, Scorpion, EmperorScorpion, TrooperBug, CaterKiller, Molenoid, Basilisk, StinkBug, Triffid); chest = vanilla `simple_dungeon` loot
- **Fix:** Restore the original 12-mob spawner pool (for ported entities) and transcribe the level1-5 chest lists into orespawn loot tables referenced by dungeon depth/level.

### WGEN-035 — Ruby Dungeon: placement model and loot changed
- **Status:** DIVERGENT
- **Original:** `OreSpawnWorld.addRubyDungeon:1998-2012` — 1/15 chunk, placed at **lava contact** Y5-50, in any dim that called it; chest: CageEmpty/Ruby/Bacon/ButterCandy/full ruby tool+armor set/ThunderStaff (`RubyBirdDungeon.java`)
- **Port:** `OreSpawnChunkGenerator:717-726`, `GenericDungeon.tryPlaceRubyDungeon:69-98` — Crystal dim only, fixed Y10-19 band; chest = vanilla `simple_dungeon`
- **Fix:** Restore lava-adjacent placement at Y5-50 and re-enable for overworld/Utopia callers; create a ruby-gear loot table (cage, ruby, bacon, butter candy, ruby tools+armor, thunder staff) and use it.

### WGEN-036 — DungeonSpawnerBlock: structure pool 50 → 2
- **Status:** PARTIAL
- **Original:** `DungeonSpawnerBlock.java` — on tick spawns 1 of **50** structures (FairyTree → RedAntHangout list)
- **Port:** `RandomDungeonSpawnerBlockEntity.java:63-72` — 2 outcomes (ruby 1/4 else generic)
- **Fix:** Same root as ITEM-020 — expand the outcome table as structures land (WGEN-021/042); restore the 400t timer.

### WGEN-037 — BasiliskMaze absent
- **Status:** MISSING
- **Original:** `BasiliskMaze.java` — maze + Basilisk spawner + chest (diamond 15-25, gold 4-16, iron 2-20, CagedGirlfriend, uranium, titanium, fish, corn dog), Mining dim
- **Port:** absent — no code or JSON match
- **Fix:** Port BasiliskMaze as a mining_biome structure (legacy-piece or code path) with its spawner and a loot table transcribing the listed chest contents.

### WGEN-038 — NightmareDungeon absent
- **Status:** MISSING
- **Original:** `NightmareDungeon.java` — 25×12×25, RTPBlock floor, EmperorScorpion-or-Nightmare spawner, 2 chests of Ultimate/Experience/Amethyst gear + Bertha/Slice; reached via RTP mechanic
- **Port:** absent — only the `NightmareSword.java` item exists
- **Fix:** Port NightmareDungeon generation (triggered from the RTP teleport target, which also requires the ITEM-013 RTPBlock fix) with its spawner and gear-chest loot tables.

### WGEN-039 — Shadow/AlienWTF/LeonNest: frequency equivalence unverified
- **Status:** UNVERIFIED
- **Original:** Mining-dim per-chunk roll placements (`addShadowDungeon` etc., exact roll values not extracted)
- **Port:** structure sets `shadow_dungeon`/`wtf_alien_dungeon`/`leonopteryx_nest`, each spacing 26 / separation 13
- **Fix:** Unverified because the original per-chunk roll odds were never extracted. Resolve by reading the three `add*` methods' roll constants and converting to equivalent random_spread spacing.

### WGEN-040 — BeeHive: relocated Mining → overworld forests
- **Status:** DIVERGENT
- **Original:** `OreSpawnWorld.addBeeHive:2031-2057` — Mining dim, lowest-grass-spot algorithm
- **Port:** `structure_set/beehive.json` + `BeehiveFeature.java` — overworld `#is_forest`/`is_jungle`, set 24/12 + feature 1/60
- **Fix:** Re-tag beehive placement to `orespawn:mining_biome` (optionally keeping forests too, documented), and consider the lowest-grass-spot site selection for fidelity.

### WGEN-041 — MantisNest: placement basis unverified
- **Status:** UNVERIFIED
- **Original:** placed via dungeon spawner / overworld hooks — exact original placement basis not extracted
- **Port:** overworld forests/jungles, set 24/12 + feature 1/80
- **Fix:** Unverified because the original placement call sites/odds were not pinned down. Resolve by locating MantisNest placement in `OreSpawnWorld`/`DungeonSpawnerBlock` and comparing rates/biomes.

### WGEN-042 — ~25+ structure types absent (systemic)
- **Status:** MISSING
- **Original:** placed by OreSpawnWorld/D4 hooks & DungeonSpawnerBlock (loot lists in `GenericDungeon.java`): D4Castle, EnderCastle, IncaPyramid, Mini, CephadromeAltar, NightmareRookery, StinkyHouse, Rainbow, CloudShark dungeon, Pumpkin, BouncyCastle, MonsterIsland, GirlfriendIsland, PlayPool, WaterDragonLair, GoldFishBowl, Graveyard, SpitBugLair, Igloo, KyuubiDungeon, EnderKnightDungeon, Hospital, DamselInDistress, SpiderHangout, RedAntHangout, FrogPond, RubberDuckyPond, QueenAltar(D4), EnormousCastle(Q)
- **Port:** absent — only 17 structure JSONs + 2 dungeon code paths exist
- **Fix:** Port these builders incrementally (legacy-piece transcription like the royal altars), prioritizing those wired to gameplay (KyuubiDungeon/EnderKnightDungeon for Mining, Hospital/EnderCastle for End, D4 set for Islands); register each into the DungeonSpawnerBlock pool (ITEM-020/WGEN-036) as it lands.

### WGEN-043 — Challenge Towers: no 1.7.10 counterpart found
- **Status:** UNVERIFIED
- **Original:** none found in 1.7.10 source (possibly 1.12.2-era content)
- **Port:** `challenge_tower_king/queen` structures, chaos_biome sets 36/18 (KING_TOWER/QUEEN_TOWER 40,4,95)
- **Fix:** Unverified provenance. Resolve by checking 1.12.2 OreSpawn sources (or port docs) for the towers; if intentional new content, document; if not, remove from chaos_biome.

## Trees

### WGEN-044 — DuplicatorTree generator absent
- **Status:** MISSING
- **Original:** `Trees.DuplicatorTree` — sapling/worldgen tree generator
- **Port:** absent — no feature or code (BlockDuplicatorLog re-interprets behavior, ITEM-027)
- **Fix:** Port `Trees.DuplicatorTree` as a `Feature`/TreeGrower wired to the duplicator sapling and the log's random tick.

### WGEN-045 — ExperienceTree generator absent
- **Status:** MISSING
- **Original:** `Trees.ExperienceTree`
- **Port:** absent — no feature/code (EXPERIENCE_SAPLING exists but uses a different/placeholder grower per file 06)
- **Fix:** Port `Trees.ExperienceTree` geometry as the grower for the experience sapling and any worldgen placement it had.

### WGEN-046 — SmallTree / ScragglyTreeWithBranches (overworld variants) absent
- **Status:** PARTIAL
- **Original:** `Trees.java` — overworld SmallTree and ScragglyTreeWithBranches variants
- **Port:** only Islands/Crystal scraggly variants exist
- **Fix:** Port the two overworld variants from `Trees.java` and wire them to their original overworld decoration call sites.

### WGEN-047 — Utopia tree frequencies not verified (Sky/Wind/Round/MagicApple)
- **Status:** UNVERIFIED
- **Original:** `OreSpawnWorld.addOtherTrees` (:2508+), `addHugeTree` (:1830-1863), `addAppleTrees` (:1792) — per-chunk roll values not extracted
- **Port:** `sky_tree.json`/`wind_tree.json` rarity 60, `round_tree.json` rarity 333, `magic_apple_tree.json` rarity 25 (utopia_plains)
- **Fix:** Unverified because original roll constants were never extracted. Resolve by reading the three methods' roll values and converting to equivalent rarity-filter values.

## Portals & teleporters

### WGEN-048 — Village/Islands unreachable in survival (rainbow/unstable ants)
- **Status:** PARTIAL
- **Original:** rainbow/unstable ants obtainable via anthill blocks placed by worldgen (`OreSpawnWorld.addAnts`)
- **Port:** portal code works (`EntityRainbowAnt.java:20` → VILLAGE, `EntityUnstableAnt.java:20` → ISLANDS) but neither ant has a natural spawn entry and their ant blocks never world-generate
- **Fix:** Restore anthill worldgen (WGEN-006) including rainbow/unstable ant blocks, or add natural spawn entries for both ants, so both dimensions are survival-reachable.

### WGEN-049 — Portal landing: tamed pets left behind
- **Status:** PARTIAL
- **Original:** `OreSpawnTeleporter.justPutMe` — scans Y1-180 for solid ground + 3 air; **teleports tamed pets too**
- **Port:** `EntityAnt.findSafeY:142-162` — top-down scan 256→min for solid + 2 air, fallback Y64; no pet co-teleport
- **Fix:** After teleporting the player, find nearby owned/tamed entities (same radius as original) and move them through the same `DimensionTransition`.

### WGEN-050 — Utopia Portal Block: no original counterpart
- **Status:** UNVERIFIED
- **Original:** `PortalBlock.java` is empty in 1.7.10 — travel was entity-based only
- **Port:** `UtopiaPortalBlock.java` — entityInside teleport, fixed y=max(min+1,64), unbreakable (`ModBlocks.java:176`)
- **Fix:** Unverified intent (new addition). Resolve by confirming with the port's design notes whether a placeable Utopia portal is wanted; if yes document it, if not remove the block or hide it from creative.

---

## Totals

| Status | ITEM (file 06) | WGEN (file 07) | Total |
|---|---|---|---|
| DIVERGENT | 33 | 14 | 47 |
| PARTIAL | 19 | 17 | 36 |
| MISSING | 7 | 11 | 18 |
| UNVERIFIED | 6 | 8 | 14 |
| **Total** | **65** | **50** | **115** |
