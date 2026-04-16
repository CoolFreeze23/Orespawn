# OreSpawn 1.12.2 Reference (Decompiled)

This folder is a **read-only** 1:1 reference for the legacy OreSpawn 1.12.2 jar
(`Orespawn-1.12.2-V0.8-ConquerantFix.jar`, the ConquerantWolf maintenance fork).
It is the **primary architectural reference** for porting decisions made in our
1.21.1 NeoForge codebase under `src/main/java/danger/orespawn/`.

> **Do not modify anything in this folder.** Always treat it as read-only. Add
> notes in `ORESPAWN_PORTING_AUDIT.md` (project root) instead.

## Dual-Reference Policy (IMPORTANT)

We maintain **two** legacy reference trees in this repository and **both are
authoritative**. Use them for different things:

| Reach for…         | When you need…                                                 |
|--------------------|----------------------------------------------------------------|
| `reference_1_12_2_source/` (this folder) | Modern-ish patterns: vanilla JSON loot tables, vanilla JSON recipes, AI goal classes (`MyEntityAI*`), event-driven world decoration (`TERRAIN_GEN_BUS`), cross-dimension teleport helpers (`util/Teleport.java`), `RegistryEvent`-based registration (`util/handlers/RegistryHandler.java`), `ItemEnchantments` builder, and the master ID inventory (`OreSpawnConstants.java`, `@Deprecated` but an ideal porting checklist). Also the cleanest source for things that survived into the maintenance fork (Alien poison/torch logic, Worm burrow cycle, GenericDungeon spawner variety). |
| `reference_1_7_10_source/` | Content that was **stripped from 1.12.2 but still lives in the full OreSpawn design**: the multi-part boss framework (The King, The Queen, Godzilla), every custom dimension (Crystal, Utopia, Chaos, Village, Islands), the Fairy Tree / Battle Tower / Haunted House / Rotator Station / Urchin Spawner / Round Rotator / Fairy Castle Tree structure set, the full 146-entity roster, structure spawners (KingSpawner, QueenSpawner, DungeonSpawner), and anything involving `EntityDragonPart` / `IEntityMultiPart`. |

**Rule of thumb:** if the feature still exists in 1.12.2, port from 1.12.2
(cleaner decompilation, modern patterns). If 1.12.2 is missing it entirely
(check by searching this folder first), fall back to 1.7.10.

**Never** cite `OreSpawnConstants.java` as proof a feature is implemented —
that file lists every ID the full mod *was designed* to have, including things
stripped from both 1.12.2 and 1.7.10. It's a checklist, not a manifest.

### Things the 1.12.2 fork deliberately **strips** (so always use 1.7.10)

- Multi-part boss entities: **The King**, **The Queen**, **Godzilla**, **The Prince**, **The Princess**
- Dimensions: **Crystal**, **Utopia**, **Chaos**, **Village**, **Islands**
- Structures: Fairy Tree, Fairy Castle Tree, Battle Tower, Haunted House, Rotator Station, Round Rotator, Urchin Spawner, Irukandji Spawner
- Entities: Boyfriend, Girlfriend (playable NPC, not the overlay), Hammerhead, Leon, Kraken, Water Dragon, Cephadrome, Whale, Rubber Ducky, Cloud Shark, Ghost, Ghost Skelly, Rat, Fairy, Crystal Fairy, Dungeon Beast, Vortex, Peacock, Flounder, Skate, Rotator, Urchin, Irukandji, Crystal Cow, Sea Monster, Sea Viper, Molenoid, Cater Killer, Easter Bunny, Criminal, Villager (NPC mob), Jeffery, Spider Driver, Ant Robot, Spider Robot, Robot 1-5, Leaf Monster, Terrible Terror, Cliff Racer, Lurking Terror, Triffid, Pitch Black, Hercules, Stinky, Gazelle, Ostrich, Trooper Bug, Spit Bug, Bee, Chipmunk, Hydrolisk, Emperor Scorpion, Scorpion, Basilisk, Cryo, Hydro, Alo, Cama, Velo, Mothra (as a spawn), Dragon, Lizard
- Items: Bertha, Slice, Big Hammer, Squid Zooka, Rose Sword, Love, Chainsaw, Nightmare Sword, Rat Sword, Fairy Sword, Rose Sword, Instant Shelter, Instant Garden, Rock TNT, Duct Tape, Sifter, Wrench, Spider/Ant Robot Kits, Game Controller, Mobzilla armor, Royal armor, Queen armor, Lapis armor, Tiger's Eye armor, Peacock Feather armor, Crystal Pink/Wood/Stone tool sets, Queen Battle Axe, Poison Sword, Thunder Staff, Laser Ball, Ray Gun, Water Ball, Ultimate Arrow / Bow / Fishing Rod, Ice Ball, Magic Apple, Miners Dream, Experience Catcher, Zoo Keeper, Elevator, Step-Up/Down/Across, Kraken Repellent, Creeper Repellent, Creeper Launcher, random dungeon block, etc.
- Systems: GeckoLib animations (newer than both refs but planned for 1.21.1), Fairy taming/ownership, CreeperRepellent/KrakenRepellent block AoE, Pizza block, Crystal Workbench / Crystal Furnace tile entities, Critter Cage per-mob filled-cage item (we replaced this with a single `CAGED_MOB` + DataComponent in 1.21.1)

### Things 1.12.2 does **better** than 1.7.10 (always use 1.12.2)

- Vanilla JSON `loot_tables/generic_dungeon.json` (1.7.10 used `WeightedRandomChestContent` Java arrays)
- Vanilla JSON recipes for titanium/uranium ingot + block (1.7.10 used `GameRegistry.addRecipe(...)` lambdas)
- Deobfuscated AI classes (`MyEntityAI*`) vs. 1.7.10's `func_*`/`field_*` mess
- `util/Teleport.java` centralised cross-dimension player transfer (1.7.10 duplicated the logic per entity)
- `@EventBusSubscriber`-style `RegistryHandler` for items/blocks (1.7.10 used static init + `GameRegistry.registerItem`)
- `ItemEnchantments` fluent builder for pre-enchanted gear (1.7.10 hard-coded per-item)

### Things 1.7.10 and 1.12.2 both have (prefer 1.12.2)

Everything else. When both have a feature, 1.12.2 wins because CFR produces cleaner output and the class layout is closer to 1.21.1's Mojang names.

## Layout

```
reference_1_12_2_source/
├── INDEX.md                         <- this file
├── _tools/
│   └── cfr-0.152.jar                <- decompiler used (Java 21 compatible)
├── sources/                         <- decompiled .java
│   ├── summary.txt                  <- CFR summary
│   └── danger/orespawn/
│       ├── OreSpawnMain.java        <- @Mod entry point
│       ├── OreSpawnConstants.java
│       ├── GirlfriendOverlayGui.java
│       ├── blocks/                  (20 files) blocks + plant/ore/troll/spawn-egg blocks
│       ├── commands/                (1 file)   /dimensionTeleport
│       ├── entity/                  (98 files) 32 mobs + projectile + render/model subdirs
│       │   ├── render/              renderers (legacy GL11)
│       │   └── model/               ModelBase classes (boxes + hardcoded animation)
│       ├── events/                  (1 file)
│       ├── init/                    (6 files)  ModEntities, ModBlocks, ModItems, ModBiomes,
│       │                                       ModDimensions, EntitySpawns
│       ├── items/                   (27 files) tools, armor, eggs, critter cages, food, seeds
│       ├── proxy/                   (2 files)  CommonProxy + ClientProxy (sided proxy pattern)
│       ├── recipes/                 (2 files)  CraftingRecipes, SmeltingRecipes
│       ├── tabs/                    (1 file)   OrespawnTab (creative tab)
│       ├── util/                    (15 files) ai/, handlers/, premium/, ItemEnchantments
│       └── world/                   (12 files) ChunkGeneratorMiningDimension, dungeon,
│                                               world generators, biome
├── _index_1_21_1_entities.txt       <- list of all entity classes in our 1.21.1 repo
└── assets/                          <- raw jar contents (textures, models, lang, sounds, etc.)
    ├── pack.mcmeta
    ├── mcmod.info
    ├── META-INF/
    ├── data/orespawn/loot_tables/   <- 1.13+ loot path (only generic_dungeon.json)
    └── assets/orespawn/
        ├── lang/en_us.lang
        ├── recipes/                 <- crafting JSONs
        ├── blockstates/             <- 1.12.2 blockstate JSONs
        ├── models/block/            <- block models
        ├── models/item/             <- item models
        ├── textures/                <- PNG textures
        ├── sounds/                  <- ogg sound files
        └── sounds.json
```

## Counts at a glance

| Subsystem            | Files |
|----------------------|------:|
| Entities (top-level) |    32 |
| Entity renderers     |    32 |
| Entity models        |    32 |
| Block classes        |    20 |
| Item classes         |    27 |
| World generators     |    12 |
| AI helpers           |     6 |
| Init / handlers      |    11 |
| **Total .java**      | **188** |

## What this fork *does* contain

- **Mining Dimension** (one custom dimension; `_mining` ID, no rain, perpetual day-cycle skip)
- **32 mob entities**: Alien, Alosaurus, TRex, Baryonyx, Camarasaurus, Pointysaurus,
  Cryolophosaurus, Nastysaurus, VelocityRaptor, CaveFisher, Worms (4 sizes),
  Ant + RedAnt, Termite, Bird, Butterfly, Firefly, Mosquito, Dragonfly, Mantis,
  Mothra, Brutalfly, Moth, Kyuubi, Beaver, Cassowary, RedCow, StinkBug, Spyro,
  GammaMetroid, EntityCage (projectile)
- **Generic dungeon**: 12×12×6 cobble box with mob spawner + chest, picks Alien/GammaMetroid/Cryolophosaurus
- **Critter Cage system**: `EntityCage` projectile + `CritterCage` items (empty + per-mob filled cages)
- **OreGenericEgg** spawn-egg blocks: 30+ ore blocks that spawn an entity when broken
- **Plant blocks**: corn, butterfly, mosquito, firefly, moth — placed via `Decorate(GRASS)` event
- **AntHill block**: 4% chance per chunk on grass (DecorateBiomeEvent.GRASS)
- **CornPlantGenerator**: tile-entity-backed multi-block tall corn (1% chance / chunk)
- **LiquidGenerator**: random water/lava patches in Mining Dimension (10% / chunk)
- **WorldGenOres**: 28 OreGenericEgg ores at Y 40-80 (size 5-9, 3 chances/chunk),
  Uranium/Titanium at Y 3-20, Amethyst at Y 3-15
- **Tools**: Ultimate / Emerald / Amethyst tier sets (sword/pickaxe/axe/shovel/hoe)
- **Armor**: Ultimate / Emerald / Amethyst / Moth full sets
- **Food**: ItemCorn, MothScale, MantisClaw
- **Premium / IP-protection**: `PremiumChecker` + `GirlfriendOverlayGui` (NOT to be ported)

## What this fork does **NOT** contain

(important — these are absent from the 1.12.2 baseline but exist in 1.21.1):

- Multi-part boss entities (no Queen, no King)
- Crystal / Utopia / Chaos / Village / Islands dimensions
- The vast 1.7.10 entity roster (no Boyfriend, Girlfriend, ThePrince, Robots,
  Hammerhead, Kraken, Godzilla, EnderKnight, Whale, etc.)
- GeckoLib / animated models
- Modern Data Maps / Tags
- Loot table JSON for any mob (only `generic_dungeon` exists)
- Capabilities / DataComponents
- Custom workbench/furnace UIs
- Rideable mounts beyond the bare entity hierarchy

## How to use this reference when porting

1. **Find the legacy source**: `reference_1_12_2_source/sources/danger/orespawn/<package>/<Class>.java`
2. **Check obfuscated names**: CFR cannot resolve some Forge-obfuscated identifiers.
   Common substitutions:
   - `func_70105_a(w, h)` → `setSize(w, h)` (1.21.1 = `EntityType.Builder.sized()`)
   - `func_110147_ax()`   → `applyEntityAttributes()` (1.21.1 = `createAttributes()`)
   - `func_180501_a(pos, state, flag)` → `setBlockState(pos, state, flag)` (1.21.1 = `level.setBlock(...)`)
   - `field_70170_p` → `world` (1.21.1 = `this.level()`)
   - `field_70163_u` → `posY` (1.21.1 = `this.getY()`)
   - `field_70146_Z` → `rand` (1.21.1 = `this.getRandom()`)
3. **Cross-reference with the audit**: see `ORESPAWN_PORTING_AUDIT.md` in repo root.

## Re-running the decompile

```pwsh
java -jar reference_1_12_2_source\_tools\cfr-0.152.jar `
     "C:\Users\Alvin\Downloads\Orespawn-1.12.2-V0.8-ConquerantFix.jar" `
     --outputdir reference_1_12_2_source\sources `
     --caseinsensitivefs true --silent true
```

Decompile completes in <15 seconds; output is byte-for-byte deterministic.
