package danger.orespawn;

import danger.orespawn.block.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(OreSpawnMod.MOD_ID);

    // Overworld ores — plain non-volatile blocks in 1.7.10 (ITEM-001); only the
    // Crystal Dimension's CrystalCoal/CrystalCrystal explode on break.
    public static final DeferredBlock<Block> ORE_RUBY = BLOCKS.register("ore_ruby",
            // orig OreRuby.java:21-22 — hardness 10.0, resistance 4.0 (ITEM-002)
            () -> new OreRuby(BlockBehaviour.Properties.of().strength(10.0f, 4.0f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> ORE_AMETHYST = BLOCKS.register("ore_amethyst",
            // orig OreAmethyst.java:21-22 — hardness 10.0, resistance 4.0 (ITEM-002)
            () -> new OreRuby(BlockBehaviour.Properties.of().strength(10.0f, 4.0f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> ORE_URANIUM = BLOCKS.register("ore_uranium",
            // orig OreUranium.java:24-25 — hardness 10.0, resistance 1.0 (ITEM-002)
            () -> new OreUranium(BlockBehaviour.Properties.of().strength(10.0f, 1.0f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> ORE_TITANIUM = BLOCKS.register("ore_titanium",
            // orig OreTitanium.java:24-25 — hardness 15.0, resistance 5.0 (ITEM-002)
            () -> new OreTitanium(BlockBehaviour.Properties.of().strength(15.0f, 5.0f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> ORE_SALT = BLOCKS.register("ore_salt",
            // orig OreSalt.java:21-22 — hardness 5.0, resistance 2.0 (ITEM-002)
            () -> new OreSalt(BlockBehaviour.Properties.of().strength(5.0f, 2.0f).requiresCorrectToolForDrops()));

    // Storage blocks — BlockTitanium/BlockUranium add sparkle particles; BlockRuby supports mobzilla-scale strength buff
    public static final DeferredBlock<Block> BLOCK_RUBY = BLOCKS.register("block_ruby",
            // orig BlockRuby.java:23-26 — 4.0/4.0, light 0.4 = level 6 (ITEM-007)
            () -> new BlockRuby(BlockBehaviour.Properties.of().strength(4.0f, 4.0f).lightLevel(s -> 6).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> BLOCK_AMETHYST = BLOCKS.register("block_amethyst",
            // orig OreSpawnMain.java:1612 — blockamethyst is a BlockRuby: 4.0/4.0, light 6 (ITEM-007)
            () -> new TransparentCrystalBlock(BlockBehaviour.Properties.of().strength(4.0f, 4.0f).lightLevel(s -> 6).requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredBlock<Block> BLOCK_URANIUM = BLOCKS.register("block_uranium",
            // orig BlockUranium.java:19-22 — 5.0/5.0, light 0.2 = level 3 (audit's "4.0/4.0 light 6" was wrong; ITEM-007)
            () -> new BlockUranium(BlockBehaviour.Properties.of().strength(5.0f, 5.0f).lightLevel(s -> 3).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> BLOCK_TITANIUM = BLOCKS.register("block_titanium",
            // orig BlockTitanium.java:19-22 — 5.0/5.0, light 0.5 = level 7 (audit's "4.0/4.0 light 6" was wrong; ITEM-007)
            () -> new BlockTitanium(BlockBehaviour.Properties.of().strength(5.0f, 5.0f).lightLevel(s -> 7).requiresCorrectToolForDrops()));
    // Mobzilla scale block grants Strength on contact via BlockRuby(props, true) — ITEM-008
    public static final DeferredBlock<Block> BLOCK_MOBZILLA_SCALE = BLOCKS.register("block_mobzilla_scale",
            // orig OreSpawnMain.java:1609 — a BlockRuby: 4.0/4.0, light 6 (ITEM-007)
            () -> new BlockRuby(BlockBehaviour.Properties.of().strength(4.0f, 4.0f).lightLevel(s -> 6).requiresCorrectToolForDrops(), true));
    public static final DeferredBlock<Block> BLOCK_CRYSTAL_PINK = BLOCKS.register("block_crystal_pink",
            // orig BlockCrystal.java:17-20 — 4.0/4.0, light 0.4 = level 6 (ITEM-007)
            () -> new TransparentCrystalBlock(BlockBehaviour.Properties.of().strength(4.0f, 4.0f).lightLevel(s -> 6).requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredBlock<Block> BLOCK_TIGERS_EYE = BLOCKS.register("block_tigers_eye",
            // orig BlockCrystal.java:17-20 — 4.0/4.0, light 6 (ITEM-007)
            () -> new TransparentCrystalBlock(BlockBehaviour.Properties.of().strength(4.0f, 4.0f).lightLevel(s -> 6).requiresCorrectToolForDrops().noOcclusion()));
    // orig OreSpawnMain.java:1972-1973 — both are OreGenericEgg blocks (0.5/1.0,
    // gravel sound, 50% 5..9 XP on break), not plain 3.0/3.0 blocks (ITEM-010)
    public static final DeferredBlock<Block> BLOCK_ENDER_PEARL = BLOCKS.register("block_ender_pearl",
            () -> new OreGenericEgg(BlockBehaviour.Properties.of().strength(0.5f, 1.0f).sound(SoundType.GRAVEL)));
    public static final DeferredBlock<Block> BLOCK_EYE_OF_ENDER = BLOCKS.register("block_eye_of_ender",
            () -> new OreGenericEgg(BlockBehaviour.Properties.of().strength(0.5f, 1.0f).sound(SoundType.GRAVEL)));

    // Lavafoam — pushes entities away and damages at high speed
    public static final DeferredBlock<Block> LAVAFOAM = BLOCKS.register("lavafoam",
            // orig Lavafoam.java:23-27 — 5.0/5.0, slipperiness 1.1 (ITEM-009)
            () -> new Lavafoam(BlockBehaviour.Properties.of().strength(5.0f, 5.0f).friction(1.1f)));

    // Pizza (edible, shrinks per bite) & Duct tape (repairs held item, shrinks per use)
    public static final DeferredBlock<Block> PIZZA = BLOCKS.register("pizza",
            () -> new BlockPizza(BlockBehaviour.Properties.of().strength(0.5f, 0.5f)));
    public static final DeferredBlock<Block> DUCT_TAPE = BLOCKS.register("duct_tape",
            () -> new BlockDuctTape(BlockBehaviour.Properties.of().strength(0.5f, 0.5f)));

    // Teleport block
    public static final DeferredBlock<Block> BLOCK_TELEPORT = BLOCKS.register("block_teleport",
            () -> new RTPBlock(BlockBehaviour.Properties.of().strength(1.5f, 6.0f).sound(SoundType.STONE)));

    // Mole dirt — disappears on random tick, slows entities walking through it
    public static final DeferredBlock<Block> MOLE_DIRT = BLOCKS.register("mole_dirt",
            () -> new MoleDirtBlock(BlockBehaviour.Properties.of().strength(0.6f).sound(SoundType.GRAVEL)));

    // Crystal dimension blocks
    public static final DeferredBlock<Block> CRYSTAL_STONE = BLOCKS.register("crystal_stone",
            () -> new TransparentCrystalBlock(BlockBehaviour.Properties.of().strength(2.0f, 10.0f).noOcclusion()));
    // orig ctor signature is (id, lightValue, hardness, resistance) — the old port
    // misread light as hardness and invented light levels (ITEM-004).
    public static final DeferredBlock<Block> CRYSTAL_COAL = BLOCKS.register("crystal_coal",
            // orig OreSpawnMain.java:1865 — OreCrystal(id, 0.6f, 6.0f, 20.0f): light 0.6*15=9, strength 6.0/20.0
            () -> new OreCrystal(BlockBehaviour.Properties.of().strength(6.0f, 20.0f).lightLevel(s -> 9).noOcclusion()));
    public static final DeferredBlock<Block> CRYSTAL_GRASS = BLOCKS.register("crystal_grass",
            () -> new CrystalGrass(BlockBehaviour.Properties.of().strength(0.6f, 2.0f).noOcclusion()));
    public static final DeferredBlock<Block> CRYSTAL_CRYSTAL = BLOCKS.register("crystal_crystal",
            // orig OreSpawnMain.java:1867 — OreCrystalCrystal(id, 0.4f, 12.0f, 40.0f): light 0.4*15=6, strength 12.0/40.0;
            // volatile (1-in-10 explode, orig OreCrystalCrystal.java:59-64), firework sparkle
            () -> new OreCrystalCrystal(BlockBehaviour.Properties.of().strength(12.0f, 40.0f).lightLevel(s -> 6).noOcclusion(), true, false));
    public static final DeferredBlock<Block> TIGERS_EYE_ORE = BLOCKS.register("tigers_eye_ore",
            // orig OreSpawnMain.java:1868 — OreCrystalCrystal(id, 0.5f, 15.0f, 60.0f): light 0.5*15=7, strength 15.0/60.0;
            // non-volatile, flame sparkle (orig OreCrystalCrystal.java:40-44)
            () -> new OreCrystalCrystal(BlockBehaviour.Properties.of().strength(15.0f, 60.0f).lightLevel(s -> 7).noOcclusion(), false, true));
    public static final DeferredBlock<Block> CRYSTAL_PLANKS = BLOCKS.register("crystal_planks",
            () -> new TransparentCrystalBlock(BlockBehaviour.Properties.of().strength(1.5f, 4.0f).sound(SoundType.WOOD).noOcclusion()));

    // Phase 10 — Kyanite Ore (the Crystal Dimension's blue gem branch). 1.7.10 lang
    // mapped "Kyanite" to CrystalStone, but the canonical post-1.7.10 setup is a
    // dedicated ore that drops the kyanite gem (used for Kyanite armor + sword).
    // Generated by orespawn:add_features#kyanite_ore_in_crystal in the Crystal Dim.
    // Non-volatile (ITEM-001): the explode-on-break behavior belongs only to the
    // original CrystalCoal/CrystalCrystal; these Phase 10 additions never had it.
    public static final DeferredBlock<Block> ORE_KYANITE = BLOCKS.register("ore_kyanite",
            () -> new TransparentCrystalBlock(BlockBehaviour.Properties.of().strength(3.0f, 6.0f)
                    .lightLevel(s -> 6).requiresCorrectToolForDrops().noOcclusion()));

    // Phase 10 — Pink Tourmaline Ore. Replaces the legacy "smelt the storage block
    // for circular ingots" path with an actual ore source. Generated alongside
    // kyanite in the Crystal Dimension.
    public static final DeferredBlock<Block> ORE_PINK_TOURMALINE = BLOCKS.register("ore_pink_tourmaline",
            () -> new TransparentCrystalBlock(BlockBehaviour.Properties.of().strength(3.0f, 6.0f)
                    .lightLevel(s -> 6).requiresCorrectToolForDrops().noOcclusion()));

    // Phase 10 — Ancient Dried Egg block. Right-clicking with a water bucket
    // re-hydrates the fossilised egg inside and yields a random prehistoric
    // spawn egg from the canonical 1.12.2 dino pool (alosaurus / baryonyx /
    // camarasaurus / cryolophosaurus / pointysaurus / trex / nastysaurus /
    // velocityraptor / spyro). The water bucket is consumed and replaced by
    // an empty bucket. Drops itself on break (no XP, no destruction).
    public static final DeferredBlock<Block> ANCIENT_DRIED_EGG = BLOCKS.register("ancient_dried_egg",
            () -> new AncientDriedEggBlock(BlockBehaviour.Properties.of().strength(1.5f, 4.0f).sound(SoundType.GRAVEL)));
    // Creature-spawning stones — spawn mobs when broken via OreBasicStone
    public static final DeferredBlock<Block> CRYSTAL_RAT = BLOCKS.register("crystal_rat",
            () -> new OreBasicStone(BlockBehaviour.Properties.of().strength(2.5f, 14.0f).noOcclusion(), OreBasicStone.StoneType.RAT));
    public static final DeferredBlock<Block> CRYSTAL_FAIRY = BLOCKS.register("crystal_fairy",
            () -> new OreBasicStone(BlockBehaviour.Properties.of().strength(2.5f, 14.0f).noOcclusion(), OreBasicStone.StoneType.FAIRY));
    public static final DeferredBlock<Block> RED_ANT_TROLL = BLOCKS.register("red_ant_troll",
            () -> new OreBasicStone(BlockBehaviour.Properties.of().strength(2.5f, 14.0f), OreBasicStone.StoneType.RED_ANT_TROLL));
    public static final DeferredBlock<Block> TERMITE_TROLL = BLOCKS.register("termite_troll",
            () -> new OreBasicStone(BlockBehaviour.Properties.of().strength(2.5f, 14.0f), OreBasicStone.StoneType.TERMITE_TROLL));

    // Workbench & Furnace
    public static final DeferredBlock<Block> CRYSTAL_WORKBENCH = BLOCKS.register("crystal_workbench",
            () -> new CrystalWorkbenchBlock(BlockBehaviour.Properties.of().strength(1.0f, 5.0f).noOcclusion()));
    public static final DeferredBlock<Block> CRYSTAL_FURNACE = BLOCKS.register("crystal_furnace",
            // orig CrystalFurnace.java:46 — active light 0.6 = level 9 (ITEM-015)
            () -> new CrystalFurnace(BlockBehaviour.Properties.of().strength(2.0f, 10.0f).noOcclusion()
                    .lightLevel(s -> s.getValue(CrystalFurnace.LIT) ? 9 : 0)));
    public static final DeferredBlock<Block> CRYSTAL_FURNACE_ON = BLOCKS.register("crystal_furnace_on",
            // orig CrystalFurnace.java:46 — light 0.6 = level 9 (ITEM-015)
            () -> new Block(BlockBehaviour.Properties.of().strength(2.0f, 10.0f).lightLevel(s -> 9)));

    // Torch blocks — extend TorchBlock for proper placement/particles; extreme torch also summons Cephadrome
    public static final DeferredBlock<Block> EXTREME_TORCH = BLOCKS.register("extreme_torch",
            () -> new BlockExtremeTorch(BlockBehaviour.Properties.of().strength(0.0f).lightLevel(s -> 15).sound(SoundType.WOOD).noCollission()));
    public static final DeferredBlock<Block> CRYSTAL_TORCH = BLOCKS.register("crystal_torch",
            () -> new BlockCrystalTorch(BlockBehaviour.Properties.of().strength(0.0f).lightLevel(s -> 15).sound(SoundType.WOOD).noCollission()));

    // Repellents — repel every 10 ticks via scheduled ticks (ITEM-019);
    // target sets per orig KrakenRepellent.java:93-124 / CreeperRepellent.java:94-145
    public static final DeferredBlock<Block> KRAKEN_REPELLENT = BLOCKS.register("kraken_repellent",
            () -> new RepellentBlock(BlockBehaviour.Properties.of().strength(1.0f).lightLevel(s -> 12),
                    RepellentBlock.Variant.KRAKEN));
    public static final DeferredBlock<Block> CREEPER_REPELLENT = BLOCKS.register("creeper_repellent",
            () -> new RepellentBlock(BlockBehaviour.Properties.of().strength(1.0f).lightLevel(s -> 12),
                    RepellentBlock.Variant.CREEPER));

    // Island block
    public static final DeferredBlock<Block> ISLAND = BLOCKS.register("island",
            () -> new Block(BlockBehaviour.Properties.of().strength(1.0f).lightLevel(s -> 14)));

    // Boss summon eggs (1.7.10 OreGenericEgg port — 50% chance of 5..9 bonus XP
    // on break, orig OreGenericEgg.java:24-30). Generated as ultra-rare deep-cave
    // single-block veins so players slowly accumulate them. Mobzilla's part block
    // is crafted-only (Phase 5B), so it is intentionally NOT registered here.
    public static final DeferredBlock<Block> KRAKEN_SPAWN_BLOCK = BLOCKS.register("kraken_spawn_block",
            () -> new OreGenericEgg(BlockBehaviour.Properties.of().strength(0.5f, 1.0f).sound(SoundType.GRAVEL)));
    public static final DeferredBlock<Block> DRAGON_SPAWN_BLOCK = BLOCKS.register("dragon_spawn_block",
            () -> new OreGenericEgg(BlockBehaviour.Properties.of().strength(0.5f, 1.0f).sound(SoundType.GRAVEL)));

    // Spawner blocks
    // orig KingSpawnerBlock.java:66-67 / QueenSpawnerBlock.java:66-67 — spawn at
    // y+8, gated by TheKingEnable/TheQueenEnable (BOSS-005/BOSS-012).
    public static final DeferredBlock<Block> KING_SPAWNER = BLOCKS.register("king_spawner",
            () -> new BossSpawnerBlock(BlockBehaviour.Properties.of().strength(50.0f, 1200.0f).lightLevel(s -> 14),
                    ModEntities.THE_KING, 8, () -> danger.orespawn.OreSpawnConfig.THE_KING_ENABLE.get()));
    public static final DeferredBlock<Block> QUEEN_SPAWNER = BLOCKS.register("queen_spawner",
            () -> new BossSpawnerBlock(BlockBehaviour.Properties.of().strength(50.0f, 1200.0f).lightLevel(s -> 14),
                    ModEntities.THE_QUEEN, 8, () -> danger.orespawn.OreSpawnConfig.THE_QUEEN_ENABLE.get()));
    public static final DeferredBlock<Block> DUNGEON_SPAWNER = BLOCKS.register("dungeon_spawner",
            () -> new BossSpawnerBlock(BlockBehaviour.Properties.of().strength(50.0f, 1200.0f).lightLevel(s -> 14),
                    ModEntities.DUNGEON_BEAST));

    // Phase 11 — Random Dungeon delayed spawner. Placed by ItemRandomDungeon,
    // counts down for 200 ticks, then deletes itself and rolls a micro-dungeon.
    public static final DeferredBlock<Block> RANDOM_DUNGEON_BLOCK = BLOCKS.register("random_dungeon_block",
            () -> new RandomDungeonSpawnerBlock(BlockBehaviour.Properties.of()
                    .strength(-1.0f, 3600000.0f).lightLevel(s -> 14).noLootTable()));

    // Phase 11 — Extractor: hopper-driven 1-input/1-output processor for
    // orespawn:extracting recipes. Powered automation only, no GUI.
    public static final DeferredBlock<Block> EXTRACTOR = BLOCKS.register("extractor",
            () -> new Extractor(BlockBehaviour.Properties.of()
                    .strength(3.5f).requiresCorrectToolForDrops().sound(SoundType.METAL)));

    // Portal
    public static final DeferredBlock<Block> UTOPIA_PORTAL = BLOCKS.register("utopia_portal",
            () -> new UtopiaPortalBlock(BlockBehaviour.Properties.of().strength(-1.0f, 3600000.0f)
                    .noLootTable().lightLevel(s -> 11).noCollission()));

    // Leaves
    public static final DeferredBlock<Block> APPLE_LEAVES = BLOCKS.register("apple_leaves",
            () -> new BlockAppleLeaves(BlockBehaviour.Properties.of().strength(0.2f).noOcclusion().sound(SoundType.GRASS).randomTicks()));
    public static final DeferredBlock<Block> EXPERIENCE_LEAVES = BLOCKS.register("experience_leaves",
            () -> new BlockExperienceLeaves(BlockBehaviour.Properties.of().strength(0.2f).noOcclusion().sound(SoundType.GRASS).randomTicks()));
    public static final DeferredBlock<Block> SCARY_LEAVES = BLOCKS.register("scary_leaves",
            () -> new BlockScaryLeaves(BlockBehaviour.Properties.of().strength(0.2f).noOcclusion().sound(SoundType.GRASS).randomTicks(),
                    BlockScaryLeaves.Variant.SCARY));
    public static final DeferredBlock<Block> CHERRY_LEAVES = BLOCKS.register("cherry_leaves",
            () -> new BlockScaryLeaves(BlockBehaviour.Properties.of().strength(0.15f).noOcclusion().sound(SoundType.GRASS).randomTicks(),
                    BlockScaryLeaves.Variant.CHERRY));
    public static final DeferredBlock<Block> PEACH_LEAVES = BLOCKS.register("peach_leaves",
            () -> new BlockScaryLeaves(BlockBehaviour.Properties.of().strength(0.15f).noOcclusion().sound(SoundType.GRASS).randomTicks(),
                    BlockScaryLeaves.Variant.PEACH));
    public static final DeferredBlock<Block> CRYSTAL_LEAVES = BLOCKS.register("crystal_leaves",
            () -> new BlockCrystalLeaves(BlockBehaviour.Properties.of().strength(0.2f).noOcclusion().sound(SoundType.GRASS).randomTicks()));
    public static final DeferredBlock<Block> CRYSTAL_LEAVES_2 = BLOCKS.register("crystal_leaves_2",
            () -> new BlockCrystalLeaves(BlockBehaviour.Properties.of().strength(0.25f).noOcclusion().sound(SoundType.GRASS).randomTicks()));
    public static final DeferredBlock<Block> CRYSTAL_LEAVES_3 = BLOCKS.register("crystal_leaves_3",
            () -> new BlockCrystalLeaves(BlockBehaviour.Properties.of().strength(0.25f).noOcclusion().sound(SoundType.GRASS).randomTicks()));

    // Logs — sky tree cascades when broken; duplicator grows a tree on random tick
    public static final DeferredBlock<Block> SKY_TREE_LOG = BLOCKS.register("sky_tree_log",
            () -> new BlockSkyTreeLog(BlockBehaviour.Properties.of().strength(0.2f).sound(SoundType.WOOD)));
    public static final DeferredBlock<Block> DUPLICATOR_LOG = BLOCKS.register("duplicator_log",
            () -> new BlockDuplicatorLog(BlockBehaviour.Properties.of().strength(0.2f).sound(SoundType.WOOD)));
    public static final DeferredBlock<Block> CRYSTAL_TREE_LOG = BLOCKS.register("crystal_tree_log",
            () -> new TransparentCrystalBlock(BlockBehaviour.Properties.of().strength(0.2f).sound(SoundType.WOOD).noOcclusion()));

    // Flowers — MyBlockFlower extends BushBlock; pink↔black and blue↔scary swap at nightfall/dawn
    public static final DeferredBlock<Block> FLOWER_PINK = BLOCKS.register("flower_pink",
            () -> new MyBlockFlower(BlockBehaviour.Properties.of().strength(0.0f).noCollission().sound(SoundType.GRASS),
                    MyBlockFlower.FlowerVariant.PINK));
    public static final DeferredBlock<Block> FLOWER_BLUE = BLOCKS.register("flower_blue",
            () -> new MyBlockFlower(BlockBehaviour.Properties.of().strength(0.0f).noCollission().sound(SoundType.GRASS),
                    MyBlockFlower.FlowerVariant.BLUE));
    public static final DeferredBlock<Block> FLOWER_BLACK = BLOCKS.register("flower_black",
            () -> new MyBlockFlower(BlockBehaviour.Properties.of().strength(0.0f).noCollission().sound(SoundType.GRASS),
                    MyBlockFlower.FlowerVariant.BLACK));
    public static final DeferredBlock<Block> FLOWER_SCARY = BLOCKS.register("flower_scary",
            () -> new MyBlockFlower(BlockBehaviour.Properties.of().strength(0.0f).noCollission().sound(SoundType.GRASS),
                    MyBlockFlower.FlowerVariant.SCARY));
    public static final DeferredBlock<Block> CRYSTAL_FLOWER_RED = BLOCKS.register("crystal_flower_red",
            () -> new MyBlockFlower(BlockBehaviour.Properties.of().strength(0.0f).noCollission().sound(SoundType.GRASS)));
    public static final DeferredBlock<Block> CRYSTAL_FLOWER_GREEN = BLOCKS.register("crystal_flower_green",
            () -> new MyBlockFlower(BlockBehaviour.Properties.of().strength(0.0f).noCollission().sound(SoundType.GRASS)));
    public static final DeferredBlock<Block> CRYSTAL_FLOWER_BLUE = BLOCKS.register("crystal_flower_blue",
            () -> new MyBlockFlower(BlockBehaviour.Properties.of().strength(0.0f).noCollission().sound(SoundType.GRASS)));
    public static final DeferredBlock<Block> CRYSTAL_FLOWER_YELLOW = BLOCKS.register("crystal_flower_yellow",
            () -> new MyBlockFlower(BlockBehaviour.Properties.of().strength(0.0f).noCollission().sound(SoundType.GRASS)));

    // Saplings / Plants
    public static final DeferredBlock<Block> EXPERIENCE_PLANT = BLOCKS.register("experience_plant",
            () -> new BlockExperiencePlant(BlockBehaviour.Properties.of().strength(0.0f).noCollission().instabreak().sound(SoundType.GRASS)));
    public static final DeferredBlock<Block> EXPERIENCE_SAPLING = BLOCKS.register("experience_sapling",
            () -> new Block(BlockBehaviour.Properties.of().strength(0.0f).noCollission().sound(SoundType.GRASS)));
    public static final DeferredBlock<Block> CRYSTAL_SAPLING = BLOCKS.register("crystal_sapling",
            () -> new Block(BlockBehaviour.Properties.of().strength(0.0f).noCollission().sound(SoundType.GRASS)));
    public static final DeferredBlock<Block> CRYSTAL_SAPLING_2 = BLOCKS.register("crystal_sapling_2",
            () -> new Block(BlockBehaviour.Properties.of().strength(0.0f).noCollission().sound(SoundType.GRASS)));
    public static final DeferredBlock<Block> CRYSTAL_SAPLING_3 = BLOCKS.register("crystal_sapling_3",
            () -> new Block(BlockBehaviour.Properties.of().strength(0.0f).noCollission().sound(SoundType.GRASS)));

    // Crop blocks
    public static final DeferredBlock<Block> STRAWBERRY_PLANT = BLOCKS.register("strawberry_plant",
            () -> new BlockStrawberry(BlockBehaviour.Properties.of().strength(0.0f).noCollission().sound(SoundType.CROP).randomTicks()));
    public static final DeferredBlock<Block> RADISH_PLANT = BLOCKS.register("radish_plant",
            () -> new BlockRadish(BlockBehaviour.Properties.of().strength(0.0f).noCollission().sound(SoundType.CROP).randomTicks()));
    public static final DeferredBlock<Block> RICE_PLANT = BLOCKS.register("rice_plant",
            () -> new BlockRice(BlockBehaviour.Properties.of().strength(0.0f).noCollission().sound(SoundType.CROP).randomTicks()));
    public static final DeferredBlock<Block> BUTTERFLY_PLANT = BLOCKS.register("butterfly_plant",
            () -> new BlockButterflyPlant(BlockBehaviour.Properties.of().strength(0.0f).noCollission().sound(SoundType.CROP).randomTicks()));
    public static final DeferredBlock<Block> MOTH_PLANT = BLOCKS.register("moth_plant",
            () -> new BlockMothPlant(BlockBehaviour.Properties.of().strength(0.0f).noCollission().sound(SoundType.CROP).randomTicks()));
    public static final DeferredBlock<Block> MOSQUITO_PLANT = BLOCKS.register("mosquito_plant",
            () -> new BlockMosquitoPlant(BlockBehaviour.Properties.of().strength(0.0f).noCollission().sound(SoundType.CROP).randomTicks()));
    public static final DeferredBlock<Block> FIREFLY_PLANT = BLOCKS.register("firefly_plant",
            () -> new BlockFireflyPlant(BlockBehaviour.Properties.of().strength(0.0f).noCollission().sound(SoundType.CROP).randomTicks()));
    public static final DeferredBlock<Block> CORN_0 = BLOCKS.register("corn_0",
            () -> new BlockCorn(BlockBehaviour.Properties.of().strength(0.0f).noCollission().sound(SoundType.CROP).randomTicks()));
    public static final DeferredBlock<Block> CORN_1 = BLOCKS.register("corn_1",
            () -> new BlockCorn(BlockBehaviour.Properties.of().strength(0.0f).noCollission().sound(SoundType.CROP).randomTicks()));
    public static final DeferredBlock<Block> CORN_2 = BLOCKS.register("corn_2",
            () -> new BlockCorn(BlockBehaviour.Properties.of().strength(0.0f).noCollission().sound(SoundType.CROP).randomTicks()));
    public static final DeferredBlock<Block> CORN_3 = BLOCKS.register("corn_3",
            () -> new BlockCorn(BlockBehaviour.Properties.of().strength(0.0f).noCollission().sound(SoundType.CROP).randomTicks()));
    public static final DeferredBlock<Block> QUINOA_0 = BLOCKS.register("quinoa_0",
            () -> new BlockQuinoa(BlockBehaviour.Properties.of().strength(0.0f).noCollission().sound(SoundType.CROP).randomTicks()));
    public static final DeferredBlock<Block> QUINOA_1 = BLOCKS.register("quinoa_1",
            () -> new BlockQuinoa(BlockBehaviour.Properties.of().strength(0.0f).noCollission().sound(SoundType.CROP).randomTicks()));
    public static final DeferredBlock<Block> QUINOA_2 = BLOCKS.register("quinoa_2",
            () -> new BlockQuinoa(BlockBehaviour.Properties.of().strength(0.0f).noCollission().sound(SoundType.CROP).randomTicks()));
    public static final DeferredBlock<Block> QUINOA_3 = BLOCKS.register("quinoa_3",
            () -> new BlockQuinoa(BlockBehaviour.Properties.of().strength(0.0f).noCollission().sound(SoundType.CROP).randomTicks()));
    public static final DeferredBlock<Block> TOMATO_0 = BLOCKS.register("tomato_0",
            () -> new BlockTomato(BlockBehaviour.Properties.of().strength(0.0f).noCollission().sound(SoundType.CROP).randomTicks()));
    public static final DeferredBlock<Block> TOMATO_1 = BLOCKS.register("tomato_1",
            () -> new BlockTomato(BlockBehaviour.Properties.of().strength(0.0f).noCollission().sound(SoundType.CROP).randomTicks()));
    public static final DeferredBlock<Block> TOMATO_2 = BLOCKS.register("tomato_2",
            () -> new BlockTomato(BlockBehaviour.Properties.of().strength(0.0f).noCollission().sound(SoundType.CROP).randomTicks()));
    public static final DeferredBlock<Block> TOMATO_3 = BLOCKS.register("tomato_3",
            () -> new BlockTomato(BlockBehaviour.Properties.of().strength(0.0f).noCollission().sound(SoundType.CROP).randomTicks()));
    public static final DeferredBlock<Block> LETTUCE_0 = BLOCKS.register("lettuce_0",
            () -> new BlockLettuce(BlockBehaviour.Properties.of().strength(0.0f).noCollission().sound(SoundType.CROP).randomTicks()));
    public static final DeferredBlock<Block> LETTUCE_1 = BLOCKS.register("lettuce_1",
            () -> new BlockLettuce(BlockBehaviour.Properties.of().strength(0.0f).noCollission().sound(SoundType.CROP).randomTicks()));
    public static final DeferredBlock<Block> LETTUCE_2 = BLOCKS.register("lettuce_2",
            () -> new BlockLettuce(BlockBehaviour.Properties.of().strength(0.0f).noCollission().sound(SoundType.CROP).randomTicks()));
    public static final DeferredBlock<Block> LETTUCE_3 = BLOCKS.register("lettuce_3",
            () -> new BlockLettuce(BlockBehaviour.Properties.of().strength(0.0f).noCollission().sound(SoundType.CROP).randomTicks()));

    // Ant spawner blocks — spawn their respective ant type on random tick when sky is clear
    public static final DeferredBlock<Block> ANT_BLOCK = BLOCKS.register("ant_block",
            () -> new CrystalAntBlock(BlockBehaviour.Properties.of().strength(2.0f, 10.0f).noOcclusion(),
                    CrystalAntBlock.AntType.BLACK_ANT));
    public static final DeferredBlock<Block> RED_ANT_BLOCK = BLOCKS.register("red_ant_block",
            () -> new CrystalAntBlock(BlockBehaviour.Properties.of().strength(2.0f, 10.0f).noOcclusion(),
                    CrystalAntBlock.AntType.RED_ANT));
    public static final DeferredBlock<Block> TERMITE_BLOCK = BLOCKS.register("termite_block",
            () -> new CrystalAntBlock(BlockBehaviour.Properties.of().strength(2.0f, 10.0f).noOcclusion(),
                    CrystalAntBlock.AntType.TERMITE));
    public static final DeferredBlock<Block> CRYSTAL_TERMITE_BLOCK = BLOCKS.register("crystal_termite_block",
            () -> new CrystalAntBlock(BlockBehaviour.Properties.of().strength(2.0f, 10.0f).noOcclusion(),
                    CrystalAntBlock.AntType.CRYSTAL_TERMITE));
    public static final DeferredBlock<Block> RAINBOW_ANT_BLOCK = BLOCKS.register("rainbow_ant_block",
            () -> new CrystalAntBlock(BlockBehaviour.Properties.of().strength(2.0f, 10.0f).noOcclusion(),
                    CrystalAntBlock.AntType.RAINBOW_ANT));
    public static final DeferredBlock<Block> UNSTABLE_ANT_BLOCK = BLOCKS.register("unstable_ant_block",
            () -> new CrystalAntBlock(BlockBehaviour.Properties.of().strength(2.0f, 10.0f).noOcclusion(),
                    CrystalAntBlock.AntType.UNSTABLE_ANT));

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
