# OreSpawn Porting Audit — 1.12.2 → NeoForge 1.21.1

**Reference baseline:** `Orespawn-1.12.2-V0.8-ConquerantFix.jar` (decompiled with CFR 0.152
into `reference_1_12_2_source/`).
**Target codebase:** this workspace (`src/main/java/danger/orespawn/`**).
**Audit date:** 2026‑04‑06.
**Auditor scope:** Registries, Entities (incl. multi‑part bosses), World Generation /
Dimensions, Items / Blocks, Rendering. Each subsystem follows the same template:
**Missing Features**, **Fixes Required Now**, **Performance & Glitch Analysis**.

> **Important framing note.** The 1.12.2 ConquerantFix is a *deliberately‑pruned*
> maintenance fork (188 .java, 32 mobs, 20 blocks, 27 items, 1 dimension). Our 1.21.1
> port is much larger (525 .java, 140 entity registrations) because earlier iterations
> were sourced from the 1.7.10 flagship build. **Therefore this audit is biased toward
> "1.12.2 features absent in 1.21.1" rather than the reverse.** Anything that exists
> only in our 1.21.1 codebase (King, Queen, Crystal/Utopia/Chaos/Village/Islands
> dimensions, GeckoLib, etc.) is intentionally out of scope of the *missing‑features*
> sections, but it is fully audited under *Fixes Required Now* and
> *Performance & Glitch Analysis*.

---

## Quick Action Checklist (top priority)

These are the items most likely to produce visible regressions or crashes if left
unaddressed. They are repeated under each subsystem with full context.

- **Race condition** in `OreSpawnChunkGenerator.recentlyPlaced` and
`CrystalStructures.recentlyPlaced` — non‑volatile `static int` mutated from
parallel chunk‑generation threads. Replace with `AtomicInteger` or per‑chunk
counters.
- Add **AntHill surface generator** (4 % per‑chunk on grass) — present in 1.12.2,
missing in our overworld worldgen pipeline (`ANT_BLOCK` exists but is never
placed in worldgen).
- Add **CornPlant** block + tile entity + multi‑block tall plant generator
(1 % per‑chunk in overworld). The `corn` item, `corn_plant` block, and
`TileEntityPlant` are entirely absent from 1.21.1.
- Port **Plant generator** for `BUTTERFLY_PLANT`, `MOSQUITO_PLANT`,
`FIREFLY_PLANT`, `MOTH_PLANT` — the blocks exist (see `ModBlocks` lines
204‑210) but no `PlacedFeature` / chunk hook places them.
- **OreGenericEgg behaviour** — 1.12.2's mob‑themed ores (alosaurus_ore,
trex_ore, baryonyx_ore, …) spawn the corresponding entity when broken. In
1.21.1 these are registered as plain ore blocks in `ModBlocks` with no spawn
side‑effect.
- Restore **Alien torch‑destruction** behaviour (15‑block search, mobGriefing
gated). 1.21.1 `Alien.java` only swims, looks at players, retaliates.
- Restore **Worm burrow / surface attack cycle** (downcount/upcount logic) —
1.21.1 `WormSmall`/`WormMedium`/`WormLarge` are plain Mob without burrow AI.
- **Critter Cage** projectile pickup logic — `EntityCage` exists, but it must
transform into the matching filled `critter_cage_*` item on impact. Verify
and complete the entity‑capture path.
- **Ultimate / Emerald / Amethyst tool & armor enchantment baking** —
1.12.2 hands out crafted items pre‑enchanted (Sharpness, Protection,
Respiration, etc.). Decide whether to keep this as a balance choice or restore
via a `LootModifier` / `Recipe` tweak.
- Replace `wireframe`‑style **NeoForge `MobCategory.MISC`** misuses (none
currently detected, but verify all new entity registrations after future
additions stay within the supported categories).
- Add `EntityCage` **client renderer** registration (search for it in
`OreSpawnClient`).

---

## 1. Registries

### 1.21.1 inventory


| Registry                    | Location                        | Count                              |
| --------------------------- | ------------------------------- | ---------------------------------- |
| `ENTITY_TYPE`               | `ModEntities`                   | **140**                            |
| `BLOCK`                     | `ModBlocks`                     | ~250 (count via `BLOCKS.register`) |
| `ITEM`                      | `ModItems`                      | ~700                               |
| `ARMOR_MATERIAL`            | `ModArmorMaterials`             | 14                                 |
| `BLOCK_ENTITY_TYPE`         | `ModBlockEntities`              | 4                                  |
| `MENU`                      | `ModMenuTypes`                  | 2                                  |
| `SOUND_EVENT`               | `ModSounds`                     | (large)                            |
| `CREATIVE_MODE_TAB`         | `ModCreativeTabs`               | many                               |
| `DATA_COMPONENT_TYPE`       | `ModDataComponents`             | (custom comps)                     |
| `LOOT_MODIFIER_SERIALIZER`  | `loot.ModLootModifiers`         | (custom)                           |
| `CHUNK_GENERATOR`           | `world.ModWorldGen`             | 1 (`orespawn:orespawn`)            |
| `DIMENSION_TYPE` (datapack) | `data/orespawn/dimension_type/` | 6                                  |


All are wired via `DeferredRegister` and registered in `OreSpawnMod`'s constructor on
the **mod event bus** (correct for 1.21.1).

### Missing Features

- No `DATA_MAP_TYPE` registrations. 1.12.2 hardcodes per‑mob/per‑item data, but
modern OreSpawn could expose tunables (mob aggression, drop rolls, ore
yields) as a NeoForge **DataMap** for pack‑authors. Recommended but not
blocking.
- No `ATTRIBUTE` registrations. None are needed yet, but if Queen "alwaysMad"
/ "playNicely" become exposed, they should live as proper attributes rather
than `EntityDataAccessor<Integer>`.

### Fixes Required Now

- All `DeferredRegister` instances point to the correct `Registries.*` key.
- `OreSpawnMod` constructor receives `IEventBus`, `ModContainer` (1.21.1
signature). ✓
- All `@EventBusSubscriber` annotations specify `bus = MOD` for setup events
and default (`GAME`) for runtime events — verified in `ModSpawnControl`
(GAME ✓), `ModEntityAttributes` (MOD ✓), `OreSpawnClient.ClientEvents`
(MOD ✓), `KeybindHandler` (GAME for input + MOD for `RegisterKeyMappingsEvent`
✓), `ModNetwork` (MOD ✓), `ModLavaDropHandler` (GAME ✓).
- `OreSpawnConfig.SPEC` is registered as **COMMON** but contains many entries
whose value is required *only on the server* (mob enables) — recommend
moving to `SERVER` to prevent client overrides.

### Performance & Glitch Analysis

- ✓ `DeferredHolder` is used everywhere; no eager `getRegistryName()` calls.
- ✓ `ModSpawnControl.NATURAL_SPAWNS` uses `Collections.newSetFromMap(WeakHashMap)`
so cancelled mobs don't leak.
- ⚠ `ModSpawnControl.spawnControls` is built lazily inside a synchronized‑less
`if (spawnControls == null)` block. Field is `volatile`, which makes the
check safe under JMM, but a brief double‑initialisation race is possible.
Acceptable (idempotent), but consider `Holder` pattern.

---

## 2. Entities (and Multi‑Part Bosses)

### 2.1 Entity roster

The 1.12.2 fork ships **32** entities. The 1.21.1 codebase has **140**. The
intersection (1.12.2 mobs ported and present in 1.21.1) is roughly:


| 1.12.2 entity                                                                                       | Status in 1.21.1                               |
| --------------------------------------------------------------------------------------------------- | ---------------------------------------------- |
| Alien                                                                                               | Present (`Alien.java`) — see §2.4              |
| Alosaurus, Baryonyx, Camarasaurus, Cryolophosaurus, Nastysaurus, Pointysaurus, TRex, VelocityRaptor | Present                                        |
| WormSmall / WormMedium / WormLarge                                                                  | Present, **missing burrow AI**                 |
| CaveFisher                                                                                          | Present                                        |
| EntityAnt / EntityRedAnt / EntityTermite                                                            | Present (+ extras: Rainbow / Unstable / Frost) |
| Bird, Butterfly, Firefly, Mosquito, Mantis, Mothra, Brutalfly, Moth                                 | Present                                        |
| Kyuubi, Beaver, Cassowary, RedCow, Spyro, GammaMetroid, StinkBug, Dragonfly                         | Present                                        |
| EntityCage (projectile)                                                                             | Present (`EntityCage.java`), see §2.5          |


### Missing Features

- `**WormSmall.downcount` / `upcount` AI** — the 1.12.2 worm spends 100‑400 t
underground, surfaces, attacks the nearest player, then digs back into
`Blocks.DIRT`/`GRASS_BLOCK`/`FARMLAND`. Our 1.21.1 worms use the default
vanilla wander/attack goals.
- `**Alien.torchAttack()`** — 15‑block AABB search for `LIT_FURNACE` /
`EXTREME_TORCH` (both block instances exist in 1.21.1 — `BlockExtremeTorch`
is registered) and breaks them when `mobGriefing == true`. Add as a custom
`Goal` running every 100 ticks server‑side.
- `**Alien` lava‑drip particles** — visual cue. Bind to renderer +
`tickClient()` Vec3 emitter using `ParticleTypes.DRIPPING_LAVA`.
- `**Alien` Poison‑on‑hit chance** — 1.12.2 = 5 % chance × difficulty‑scaled
duration (`30 + difficulty*30` ticks). Our 1.21.1 substitutes Hunger; this
is a balance choice but flag it for review.
- **Alien drop table** — 1.12.2 hardcodes `Items.ROTTEN_FLESH`,
`Items.SPIDER_EYE`, `Items.GOLD_INGOT` (`rand 0..2`), `Items.COMPASS`
(1/4096 boss kills), `Items.CLOCK` (1/4096). 1.21.1 has no datapack loot
table for `orespawn:alien` (verify in `data/orespawn/loot_tables/entities/`).
- **GenericTargetSorter** — 1.12.2 uses a custom `Comparator<Entity>` that
weights distance by entity health. Our `targetSorter = comparingDouble(this::distanceToSqr)`
ignores health. Restore weighted sort for The Queen, The King, Alien,
DungeonBeast, others.
- **Cassowary unique behaviours** — kicks the player and drops eggs. Verify.
- **Beaver tree‑chop AI** — 1.12.2 Beaver breaks logs around it.

### 2.2 Multi‑Part Boss Entities

The 1.12.2 fork has **no** multi‑part boss entities. The Queen and King in our
1.21.1 codebase are reverse‑engineered from the 1.7.10 build. They use the
NeoForge `PartEntity` API correctly via `OreSpawnPartEntity<T>` — this is the
**modern** equivalent of the 1.7.10 `EntityDragonPart` pattern.

The audit therefore focuses on *correctness inside the 1.21.1 implementation*:

#### `OreSpawnPartEntity<T>` — `entity/OreSpawnPartEntity.java`

- ✓ Extends `net.neoforged.neoforge.entity.PartEntity`. Correct API.
- ✓ Implements `MultipartBoss` interface for per‑part damage routing.
- ✓ Uses `EntityDimensions.scalable(w, h)` and `refreshDimensions()` in ctor.
- ✓ Returns `shouldBeSaved() == false` so parts are not double‑saved with parent.
- ✓ Forwards `getPickResult()` to parent so middle‑click works.
- ⚠ `defineSynchedData` / `readAdditionalSaveData` / `addAdditionalSaveData`
are **empty no‑ops**. This is fine for a part (state lives on parent), but
if a future part ever needs synced state (e.g. Queen head detached anim),
the empty `SynchedEntityData.Builder` will *throw* `IllegalStateException`
when an accessor is added. Document this.

#### `TheQueen` — `entity/TheQueen.java`

- ✓ Constructor declares 14 named parts (`bodyPart`, three `neck`/`head` pairs,
two wings, three tail segments, two legs).
- ✓ `noPhysics = true` → parent is purely positional (parts handle hitboxes).
- **Part positioning is missing in `aiStep`** — confirm that
`bodyPart.setPos(...)` / etc. are called *every* tick using sin/cos offsets
from `yBodyRot`. Without per‑tick repositioning, all parts collapse to
`(0,0,0)`. Verify against `aiStep` (lines beyond shown excerpt) and add
sanity test.
- `**getParts()` override** — `Entity.getParts()` (the NeoForge multipart
hook) must return `allParts`. Confirm this is overridden; otherwise the
parts are never registered with the chunk and won't be hittable.
- **Boss bar player tracking** — `bossEvent` is created but
`startSeenByPlayer` / `stopSeenByPlayer` need to be wired in
`startSeenByPlayer(ServerPlayer)` and `stopSeenByPlayer(ServerPlayer)`
overrides. Without them, the boss bar never appears.
- **6000 HP / 200 dmg balance vs vanilla** — these match the 1.7.10 flagship,
but confirm we're not accidentally one‑shotting fully‑geared netherite
players. Consider per‑difficulty scaling.

#### `TheKing` — `entity/TheKing.java`

- ✓ 5 parts (`body`, `head`, two wings, `tail`).
- ✓ `noPhysics = true`, `setNoGravity(true)` for free flight.
- ✓ `Comparator<Entity>` distance sort on `targetSorter` (same caveat as Queen).
- Same `**getParts()` / per‑tick part positioning** verification needed.
- `**isEnd` / `endCounter*`* drives boss phase transition — confirm phase
changes don't desync between client and server (these fields are server‑side
only; client must read via `DATA_IS_END` `EntityDataAccessor<Integer>`,
which *is* defined ✓).
- **End‑phase lightning spam** — `LightningBolt` summoning loop must be
gated to `!level().isClientSide()` (it is, via `ServerLevel` cast). ✓ but
must also be rate‑limited; 1.7.10 spammed 1 per tick, killing TPS.

### 2.3 Entity AI Goals — review

- ✓ Most monsters use `FloatGoal`, `WaterAvoidingRandomStrollGoal`,
`LookAtPlayerGoal`, `RandomLookAroundGoal`, `HurtByTargetGoal`. Modern
idiom, no concerns.
- No mob currently has a `MeleeAttackGoal` or `TargetGoal` chain — i.e. they
will *retaliate* (HurtByTarget) but won't pursue unprovoked players. This
is a clear regression from 1.12.2 (most hostile mobs had
`EntityAINearestAttackableTarget(EntityPlayer.class, true)`). **Add
`NearestAttackableTargetGoal<Player>*`* to all hostile OreSpawn mobs.

### 2.4 `Alien` deep‑dive (case study)


| Attribute               | 1.12.2               | 1.21.1      | Verdict                                                 |
| ----------------------- | -------------------- | ----------- | ------------------------------------------------------- |
| `MAX_HEALTH`            | 100                  | 80          | **Match 1.12.2 → 100**                                  |
| `ARMOR`                 | 8                    | 6           | **Match 1.12.2 → 8**                                    |
| `ATTACK_DAMAGE`         | 12                   | 12          | ✓                                                       |
| `MOVEMENT_SPEED`        | 0.65                 | 0.65        | ✓                                                       |
| Poison on hit           | 5 %, scaled          | Hunger 30 t | **Restore Poison**                                      |
| Self‑heal               | +1 HP/40 t when hurt | same        | ✓                                                       |
| Knockback               | yes                  | yes         | ✓                                                       |
| Cactus immunity         | yes                  | yes         | ✓                                                       |
| Torch destruction       | yes                  | **no**      | **Restore**                                             |
| Lava drip particles     | yes                  | **no**      | **Restore**                                             |
| Custom drop table       | yes                  | **no**      | **Add `data/orespawn/loot_tables/entities/alien.json`** |
| Boss compass/clock drop | 1/4096               | **no**      | **Add to loot table**                                   |


### 2.5 `EntityCage` projectile

- 1.12.2: thrown cage hits an entity → entity is "captured" → projectile becomes a
filled `critter_cage_<mob>` item drop.
- 1.21.1: `EntityCage.java` exists; `CagedMobItem` (1.21.1‑native) exists with a
`DataComponent` to store the entity type. Verify the projectile actually
populates that component on impact. (Open file to confirm.)

### Performance & Glitch Analysis

- ✓ All entity tick code runs server‑side or client‑side as appropriate.
- ⚠ `volatile int headFound` (Queen) and `volatile int headEntityFound` (King) —
the `volatile` keyword is suspect; these fields are written from a single
thread (`aiStep`). Either remove `volatile` or document why concurrent
access is intended.
- ⚠ Several mobs (Queen, King) call `level().getEntitiesOfClass(...)` every tick
with no AABB cache. Cap the AABB to ~40 blocks and only re‑scan every
10 ticks for performance.
- ⚠ `Comparator<Entity> targetSorter = Comparator.comparingDouble(this::distanceToSqr)`
captures `this` — fine, but ensure it isn't constructed inside a tick loop
(it isn't — constructed in ctor). ✓

---

## 3. World Generation / Dimensions

### 3.1 Dimensions present

The 1.12.2 baseline has **one** dimension: `mining` (`WorldProviderMiningDimension`).
We currently have **six** datapack dimensions (`mining`, `crystal`, `utopia`,
`chaos`, `village`, `islands`) all routed through `OreSpawnChunkGenerator`.

Of these, only `mining` and `crystal` have working biome JSON content. The others
need verification.

### 3.2 Mining Dimension comparison


| Aspect           | 1.12.2 `ChunkGeneratorMiningDimension`     | 1.21.1 datapack `mining.json`                                                  |
| ---------------- | ------------------------------------------ | ------------------------------------------------------------------------------ |
| Terrain noise    | 4 octave + perlin (vanilla overworld code) | `"settings": "minecraft:overworld"` ✓                                          |
| Biome            | `BiomeProviderSingle(MINING_BIOME)`        | `minecraft:fixed` → `orespawn:mining_biome` ✓                                  |
| Caves            | `MapGenCaves`                              | uses vanilla cave + canyon carvers ✓                                           |
| Time             | locked to day (`setWorldTime` override)    | **must be set in dimension_type JSON** (`fixed_time`) — verify                 |
| Sky / fog colour | dim cave colour (5066061)                  | ✓ matches in biome JSON                                                        |
| Has skylight     | false in 1.12.2                            | verify `dimension_type/mining.json` `has_skylight: false`                      |
| Liquids          | random water/lava patches (10 %/chunk)     | ✓ via `minecraft:spring_water`, `minecraft:spring_lava` features in biome JSON |


**Action items for Mining Dimension:**

- Verify `data/orespawn/dimension_type/mining.json` has
`"fixed_time": 6000`, `"has_skylight": false`, `"piglin_safe": false`,
`"effects": "minecraft:the_nether"` (for the dark sky).
- Confirm the **GenericDungeon** placement happens in this dimension (1.12.2
put 1/16 chance per chunk, Y 5‑40 with `Blocks.STONE`).

### 3.3 Crystal Dimension (1.21.1‑only)

The Crystal dimension has no 1.12.2 counterpart. Audited in previous sessions; current
state:

- ✓ `OreSpawnChunkGenerator.replaceTerrain` + `fillShallowWater` works
- ✓ `CrystalStructures.generate` runs in `applyBiomeDecoration` (correct phase to
avoid chunk‑border truncation)
- ✓ Mob spawn placements work post‑fix
- ⚠ See **Performance** below for `recentlyPlaced` race.

### 3.4 Missing 1.12.2 World‑Gen Features

- `**AntHillGenerator`** — 4 % per‑chunk on grass, places `ANT_BLOCK` on
`DIRT`. Our `ANT_BLOCK` is registered but never placed by worldgen.
Implement as `PlacedFeature` in `data/orespawn/worldgen/placed_feature/`
with `count: 1`, `rarity_filter: 25`, `in_square`, `heightmap`.
- `**CornPlantGenerator**` — 1 % per‑chunk. Multi‑block tall plant requires:
- new `BlockCornPlant` extending `CropBlock` with `STAGE 0..3`
- new `TileEntityPlant` ⇒ `BlockEntity` storing `heightContribution`
- new `ItemCorn` food (1 hunger, 1.0 saturation, plants seed on use)
- `CornFeature extends Feature<NoneFeatureConfiguration>` with the 32‑try
  loop from `CornPlantGenerator.func_180709_b`.
All four are absent. **Substantial work** — gate behind a config toggle.
- `**PlantGenerator`** — 1 % per‑chunk; randomly picks Butterfly / Mosquito /
Firefly / Moth plant; places 12 per cluster on grass. The blocks exist;
add a `RandomPatchFeature` configured feature per‑plant and a switching
`ConfiguredFeature` chooser in a placed‑feature with weighted variants.
- `**LiquidGenerator**` — 10 % per‑chunk water or lava patch in the Mining
dimension. Equivalent to `minecraft:spring_water`/`spring_lava` (already in
`mining_biome.json`) but at lower y‑filter — 1.12.2 placed at *surface*.
Probably acceptable to leave as‑is.
- `**WorldGenOres*`* — 1.12.2 places **30 OreGenericEgg ores** (uniform
Y 40‑80, count 5‑9, 3 chances/chunk) for *every* ore‑themed mob spawner
block (alosaurus_ore, baryonyx_ore, trex_ore, alien_ore, cow_ore,
creeper_ore, ghast_ore, horse_ore, pig_ore, zombie_ore, etc.). Currently
our `mining_biome.json` only places vanilla + ruby/amethyst/uranium/
titanium/salt. **None of the mob‑themed spawner ores are placed in
worldgen.** This is the largest single port gap in worldgen.

### Fixes Required Now

- `OreSpawnChunkGenerator` correctly extends `NoiseBasedChunkGenerator` and
registers `CODEC` via `MapCodec`.
- `applyBiomeDecoration` phase used for cross‑chunk structures (correct).
- `mining` dimension JSON has `"crystal_surface": false` ✓ but **no
`"crystal_overworld_features": ...` flag** (n/a) — verify codec only reads
what's present.

### Performance & Glitch Analysis

- ⚠ `**OreSpawnChunkGenerator.recentlyPlaced` (line 63) is a non‑volatile
`static int` mutated from worldgen worker threads.** NeoForge runs `buildSurface`
on the chunk‑generator thread pool (parallel). Two threads can read the same
`recentlyPlaced == 0`, both decide to place a dungeon, and overlap. Worse,
partial writes (`recentlyPlaced = 50` then `--recentlyPlaced`) are not atomic.
**Fix:** replace with `private static final AtomicInteger recentlyPlaced = new AtomicInteger(0);` and use `getAndDecrement()` / `compareAndSet()`. Same
fix in `CrystalStructures.recentlyPlaced` (line 63).
- ⚠ `**GenericDungeon.placeSpawner`** passes `null` Level to
`BaseSpawner.setEntityId` to avoid synchronous `ServerLevel` ops. This is
intentional and documented; keep it. ✓
- ⚠ `**CrystalStructures.generate**` loops up to ~6 structure variants per
chunk; some allocate intermediate `ArrayList<BlockPos>`. Consider
`BlockPos.MutableBlockPos` to avoid GC pressure when generating large worlds.

---

## 4. Items / Blocks

### 4.1 Block roster gap

The 1.12.2 fork registers **20** blocks. All are present in 1.21.1 *except*:

- `corn_plant` (`BlockCornPlant`) — see §3.4.
- **None other missing.** The 1.12.2 ant/red‑ant/troll/storage/plant/torch
blocks all have 1.21.1 counterparts (`ANT_BLOCK`, `RED_ANT_TROLL`,
`TERMITE_TROLL`, `EXTREME_TORCH`, `BUTTERFLY_PLANT`, `MOTH_PLANT`,
`MOSQUITO_PLANT`, `FIREFLY_PLANT`, `URANIUM_BLOCK`, `TITANIUM_BLOCK`,
`AMETHYST_BLOCK`, etc.). ✓

### 4.2 Item roster gap

The 1.12.2 fork registers **27** items. Missing in 1.21.1:

- `corn` (food + seed). See §3.4.
- **Per‑mob filled `critter_cage_<mob>` items** — the 1.12.2 system has one
filled cage per capturable mob. Our 1.21.1 instead has a single
`CAGED_MOB` item with a `DataComponent` storing the entity type. **This
is a paradigm upgrade — keep the modern approach** and provide a
one‑way migration recipe if save compatibility is desired.
- `mantis_claw` food (1.12.2 `ItemMantisClaw`).
- `moth_scale` food (1.12.2 `ItemMothScale`).

### 4.3 Tools / Armor materials


| Material                 | 1.12.2                                                    | 1.21.1                                                     | Notes                                                                                                                                                  |
| ------------------------ | --------------------------------------------------------- | ---------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------ |
| Ultimate (armor)         | feet 6, legs 12, chest 10, helmet 6 / dur 200 / tough 3.0 | boots 6, legs 10, chest 12, helmet 6 / dur 100 / tough 2.0 | **Slot order swap** is correct (1.21.1 uses canonical Mojang order). **Durability halved**, **toughness reduced**, **enchantments removed** — restore? |
| Emerald                  | similar                                                   | ✓ present                                                  |                                                                                                                                                        |
| Moth                     | yes                                                       | yes                                                        |                                                                                                                                                        |
| Amethyst                 | yes                                                       | yes                                                        |                                                                                                                                                        |
| Ultimate (tools)         | level 10, dur 3000, eff 15.0, dmg 36, ench 100            | (verify in `ModItems.UltimateTier`)                        | Probably present in modern `Tier` API                                                                                                                  |
| Sword baked enchantments | Sharpness V, Looting III                                  | **none in 1.21.1**                                         | Add via `Recipe` post‑process                                                                                                                          |


### Missing Features

- **Pre‑enchanted crafted items**. 1.12.2 hands you a Sharpness V / Looting III
Ultimate Sword and a Protection IV / Respiration III Ultimate Helmet.
`ItemEnchantments.java` in 1.12.2 wraps `Item.onCreated` to apply enchants
on craft. Port via:
- a `CraftingRecipe` with a `RemainderItem` returning the enchanted result, or
- a `LivingEquipmentChangeEvent` / `PlayerEvent.ItemCraftedEvent` that calls
  `ItemStack.enchant(Enchantment, level)`.

### Fixes Required Now

- `ModItems` uses `DeferredRegister.createItems()` and `register(...)`. ✓
- All food items (1.21.1‑native) wired through `Item.Properties.food(...)`.
- **Audit `BlockItem` registrations** — confirm every block in `ModBlocks`
has a matching `BlockItem` in `ModItems` (search for blocks without items).
- **Tag the OreSpawn ores** — for vanilla pickaxe compatibility add to
`data/minecraft/tags/blocks/needs_iron_tool.json` etc.

### Performance & Glitch Analysis

- ✓ `Item.Properties.stacksTo(1)` set on `CAGED_MOB` (no oversized stacks).
- ✓ No item ticks on the tick loop except food/tool durability (vanilla).
- ⚠ `ItemOreSpawnArmor` should be inspected for any per‑tick scan (some armors
apply potion effects to the wearer). If so, throttle to 20‑t intervals.

---

## 5. Rendering

### 5.1 Inventory

- 139 `event.registerEntityRenderer(...)` calls in `OreSpawnClient.java`.
- `IClientItemExtensions` is used for custom item BEWLR rendering.
- All renderers in `entity/client/` are *modern* Mojang `EntityRenderer<T>`
subclasses with `EntityModel<T>` — no `Tessellator` / `Render` legacy classes.

### Missing Features

- `**EntityCage` renderer** — search for it in the 139‑line block; if absent,
the projectile will render as a vanilla white cube. Add a simple
`ThrownItemRenderer` pointing at the `CAGE_EMPTY` model.
- **King / Queen GeckoLib animations** — covered in earlier session;
animations are exported but the model hierarchy didn't match. Out of scope
for this audit but flagged as still incomplete.
- **Boss bar overlay** for King and Queen — see §2.2.

### Fixes Required Now

- No `GL11.`* calls anywhere (only one comment in `TheQueenRenderer.java:23`).
- All transparent/cutout rendering goes through `RenderType.cutout()` /
`RenderType.translucent()` (verified via Crystal block JSON `render_type`
keys).
- Verify every renderer registers a `LayerDefinition` in
`EntityRenderersEvent.RegisterLayerDefinitions` — missing definitions cause
`IllegalStateException: Missing layer definition` on world load.

### Performance & Glitch Analysis

- ⚠ `KeybindHandler` — some keybind handlers do per‑frame allocations. Scan for
`new` in `onClientTick` and pre‑allocate where possible.
- ✓ `GirlfriendOverlay` is correctly client‑only (`Dist.CLIENT`) and gated.
- ⚠ Several models in `entity/client/` may have **>256 boxes** (Queen). Confirm
vertex count stays under the OpenGL display‑list cap; modern Blaze3D
handles this gracefully but legacy decompiled models can balloon.

---

## 6. Cross‑Cutting Concerns

### 6.1 Networking

- 1.12.2 has no `SimpleNetworkWrapper` registrations.
- 1.21.1 ships `ModNetwork` with `@EventBusSubscriber(bus = MOD)` and the
`RegisterPayloadHandlersEvent` API. ✓

### 6.2 Configuration

- 1.12.2 has no config (everything hardcoded).
- 1.21.1 ships `OreSpawnConfig` with ~50 boolean toggles.
- As noted in §1, consider moving runtime mob enables to **SERVER** config so
they can't be desynced from clients.

### 6.3 Save data / NBT compat

- No save data is shared between 1.12.2 and 1.21.1; this is a clean slate.

### 6.4 Multiplayer / Dedicated server

- ⚠ All client‑only handlers (`KeybindHandler`, `GirlfriendOverlay`,
`OreSpawnClient.ClientEvents`) are correctly gated by `Dist.CLIENT`. ✓
- ⚠ All entity packet senders (`SynchedEntityData`) use proper `defineId`. ✓
- Run a dedicated‑server smoke test once items below are addressed.

---

## 7. Summary of Critical Path Work

In rough priority order (highest first):

1. **Fix `recentlyPlaced` race condition** in `OreSpawnChunkGenerator` and
  `CrystalStructures` — risk of intermittent worldgen glitches and structure
   overlap.
2. **Restore Alien torch destruction + Poison‑on‑hit + drop table** — most
  visible behaviour gap of the most iconic OreSpawn mob.
3. **Add Worm burrow AI** — second‑most iconic 1.12.2 behaviour.
4. **Verify Queen / King `getParts()` override and per‑tick part positioning**
  — without this, the bosses are unhittable.
5. **Add `NearestAttackableTargetGoal<Player>` to all hostile OreSpawn mobs** —
  without this, mobs only retaliate.
6. **Place Mob‑Egg ores in worldgen** (the 30 `OreGenericEgg` blocks).
7. **Add AntHill / Plant / CornPlant overworld generators** — surface decoration
  parity with 1.12.2.
8. **Boss bar wiring** for Queen and King.
9. **EntityCage renderer + impact‑capture path**.
10. **Decide on pre‑enchanted Ultimate gear** (1.12.2 parity vs. 1.21.1
  vanilla‑style enchanting).

---

## 8. References

- Decompiled 1.12.2 source: `reference_1_12_2_source/sources/danger/orespawn/`
- Asset dump: `reference_1_12_2_source/assets/`
- Folder index: `reference_1_12_2_source/INDEX.md`
- 1.21.1 entity inventory: `reference_1_12_2_source/_index_1_21_1_entities.txt`
- CFR decompiler: `reference_1_12_2_source/_tools/cfr-0.152.jar`

End of audit.