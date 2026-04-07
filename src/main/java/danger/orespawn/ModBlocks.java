package danger.orespawn;

import danger.orespawn.block.*;
import danger.orespawn.entity.EntityAnt;
import danger.orespawn.entity.Kraken;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(OreSpawnMod.MOD_ID);

    // Ores
    public static final DeferredBlock<Block> ORE_RUBY = BLOCKS.register("ore_ruby",
            () -> new Block(BlockBehaviour.Properties.of().strength(3.0f, 3.0f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> ORE_AMETHYST = BLOCKS.register("ore_amethyst",
            () -> new Block(BlockBehaviour.Properties.of().strength(3.0f, 3.0f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> ORE_URANIUM = BLOCKS.register("ore_uranium",
            () -> new Block(BlockBehaviour.Properties.of().strength(3.0f, 3.0f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> ORE_TITANIUM = BLOCKS.register("ore_titanium",
            () -> new Block(BlockBehaviour.Properties.of().strength(3.0f, 3.0f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> ORE_SALT = BLOCKS.register("ore_salt",
            () -> new Block(BlockBehaviour.Properties.of().strength(2.0f, 2.0f).requiresCorrectToolForDrops()));

    // Storage blocks
    public static final DeferredBlock<Block> BLOCK_RUBY = BLOCKS.register("block_ruby",
            () -> new Block(BlockBehaviour.Properties.of().strength(5.0f, 6.0f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> BLOCK_AMETHYST = BLOCKS.register("block_amethyst",
            () -> new Block(BlockBehaviour.Properties.of().strength(5.0f, 6.0f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> BLOCK_URANIUM = BLOCKS.register("block_uranium",
            () -> new Block(BlockBehaviour.Properties.of().strength(5.0f, 6.0f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> BLOCK_TITANIUM = BLOCKS.register("block_titanium",
            () -> new Block(BlockBehaviour.Properties.of().strength(5.0f, 6.0f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> BLOCK_MOBZILLA_SCALE = BLOCKS.register("block_mobzilla_scale",
            () -> new Block(BlockBehaviour.Properties.of().strength(5.0f, 6.0f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> BLOCK_CRYSTAL_PINK = BLOCKS.register("block_crystal_pink",
            () -> new Block(BlockBehaviour.Properties.of().strength(5.0f, 6.0f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> BLOCK_TIGERS_EYE = BLOCKS.register("block_tigers_eye",
            () -> new Block(BlockBehaviour.Properties.of().strength(5.0f, 6.0f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> BLOCK_ENDER_PEARL = BLOCKS.register("block_ender_pearl",
            () -> new Block(BlockBehaviour.Properties.of().strength(3.0f, 3.0f)));
    public static final DeferredBlock<Block> BLOCK_EYE_OF_ENDER = BLOCKS.register("block_eye_of_ender",
            () -> new Block(BlockBehaviour.Properties.of().strength(3.0f, 3.0f)));

    // Lavafoam
    public static final DeferredBlock<Block> LAVAFOAM = BLOCKS.register("lavafoam",
            () -> new Block(BlockBehaviour.Properties.of().strength(0.5f, 0.5f)));

    // Pizza & Duct tape
    public static final DeferredBlock<Block> PIZZA = BLOCKS.register("pizza",
            () -> new Block(BlockBehaviour.Properties.of().strength(0.5f, 0.5f)));
    public static final DeferredBlock<Block> DUCT_TAPE = BLOCKS.register("duct_tape",
            () -> new Block(BlockBehaviour.Properties.of().strength(0.5f, 0.5f)));

    // Teleport block
    public static final DeferredBlock<Block> BLOCK_TELEPORT = BLOCKS.register("block_teleport",
            () -> new RTPBlock(BlockBehaviour.Properties.of().strength(1.5f, 6.0f).sound(SoundType.STONE)));

    // Mole dirt
    public static final DeferredBlock<Block> MOLE_DIRT = BLOCKS.register("mole_dirt",
            () -> new Block(BlockBehaviour.Properties.of().strength(0.6f).sound(SoundType.GRAVEL)));

    // Crystal dimension blocks
    public static final DeferredBlock<Block> CRYSTAL_STONE = BLOCKS.register("crystal_stone",
            () -> new Block(BlockBehaviour.Properties.of().strength(2.0f, 10.0f)));
    public static final DeferredBlock<Block> CRYSTAL_COAL = BLOCKS.register("crystal_coal",
            () -> new Block(BlockBehaviour.Properties.of().strength(0.6f, 6.0f).lightLevel(s -> 8)));
    public static final DeferredBlock<Block> CRYSTAL_GRASS = BLOCKS.register("crystal_grass",
            () -> new Block(BlockBehaviour.Properties.of().strength(0.6f, 2.0f)));
    public static final DeferredBlock<Block> CRYSTAL_CRYSTAL = BLOCKS.register("crystal_crystal",
            () -> new Block(BlockBehaviour.Properties.of().strength(0.4f, 12.0f).lightLevel(s -> 12)));
    public static final DeferredBlock<Block> TIGERS_EYE_ORE = BLOCKS.register("tigers_eye_ore",
            () -> new Block(BlockBehaviour.Properties.of().strength(0.5f, 15.0f).lightLevel(s -> 12)));
    public static final DeferredBlock<Block> CRYSTAL_PLANKS = BLOCKS.register("crystal_planks",
            () -> new Block(BlockBehaviour.Properties.of().strength(1.5f, 4.0f).sound(SoundType.WOOD)));
    public static final DeferredBlock<Block> CRYSTAL_RAT = BLOCKS.register("crystal_rat",
            () -> new Block(BlockBehaviour.Properties.of().strength(2.5f, 14.0f)));
    public static final DeferredBlock<Block> CRYSTAL_FAIRY = BLOCKS.register("crystal_fairy",
            () -> new Block(BlockBehaviour.Properties.of().strength(2.5f, 14.0f)));
    public static final DeferredBlock<Block> RED_ANT_TROLL = BLOCKS.register("red_ant_troll",
            () -> new Block(BlockBehaviour.Properties.of().strength(2.5f, 14.0f)));
    public static final DeferredBlock<Block> TERMITE_TROLL = BLOCKS.register("termite_troll",
            () -> new Block(BlockBehaviour.Properties.of().strength(2.5f, 14.0f)));

    // Workbench & Furnace
    public static final DeferredBlock<Block> CRYSTAL_WORKBENCH = BLOCKS.register("crystal_workbench",
            () -> new Block(BlockBehaviour.Properties.of().strength(1.0f, 5.0f)));
    public static final DeferredBlock<Block> CRYSTAL_FURNACE = BLOCKS.register("crystal_furnace",
            () -> new CrystalFurnace(BlockBehaviour.Properties.of().strength(2.0f, 10.0f)
                    .lightLevel(s -> s.getValue(CrystalFurnace.LIT) ? 13 : 0)));
    public static final DeferredBlock<Block> CRYSTAL_FURNACE_ON = BLOCKS.register("crystal_furnace_on",
            () -> new Block(BlockBehaviour.Properties.of().strength(2.0f, 10.0f).lightLevel(s -> 13)));

    // Torch blocks
    public static final DeferredBlock<Block> EXTREME_TORCH = BLOCKS.register("extreme_torch",
            () -> new Block(BlockBehaviour.Properties.of().strength(0.0f).lightLevel(s -> 15).sound(SoundType.WOOD).noCollission()));
    public static final DeferredBlock<Block> CRYSTAL_TORCH = BLOCKS.register("crystal_torch",
            () -> new Block(BlockBehaviour.Properties.of().strength(0.0f).lightLevel(s -> 15).sound(SoundType.WOOD).noCollission()));

    // Repellents
    public static final DeferredBlock<Block> KRAKEN_REPELLENT = BLOCKS.register("kraken_repellent",
            () -> new RepellentBlock(BlockBehaviour.Properties.of().strength(1.0f).lightLevel(s -> 12),
                    e -> e instanceof Kraken || e instanceof EntityAnt));
    public static final DeferredBlock<Block> CREEPER_REPELLENT = BLOCKS.register("creeper_repellent",
            () -> new RepellentBlock(BlockBehaviour.Properties.of().strength(1.0f).lightLevel(s -> 12),
                    e -> e instanceof Creeper || e instanceof EntityAnt));

    // Island block
    public static final DeferredBlock<Block> ISLAND = BLOCKS.register("island",
            () -> new Block(BlockBehaviour.Properties.of().strength(1.0f).lightLevel(s -> 14)));

    // Spawner blocks
    public static final DeferredBlock<Block> KING_SPAWNER = BLOCKS.register("king_spawner",
            () -> new BossSpawnerBlock(BlockBehaviour.Properties.of().strength(50.0f, 1200.0f).lightLevel(s -> 14),
                    ModEntities.THE_KING));
    public static final DeferredBlock<Block> QUEEN_SPAWNER = BLOCKS.register("queen_spawner",
            () -> new BossSpawnerBlock(BlockBehaviour.Properties.of().strength(50.0f, 1200.0f).lightLevel(s -> 14),
                    ModEntities.THE_QUEEN));
    public static final DeferredBlock<Block> DUNGEON_SPAWNER = BLOCKS.register("dungeon_spawner",
            () -> new BossSpawnerBlock(BlockBehaviour.Properties.of().strength(50.0f, 1200.0f).lightLevel(s -> 14),
                    ModEntities.DUNGEON_BEAST));

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

    // Logs
    public static final DeferredBlock<Block> SKY_TREE_LOG = BLOCKS.register("sky_tree_log",
            () -> new Block(BlockBehaviour.Properties.of().strength(0.2f).sound(SoundType.WOOD)));
    public static final DeferredBlock<Block> DUPLICATOR_LOG = BLOCKS.register("duplicator_log",
            () -> new Block(BlockBehaviour.Properties.of().strength(0.2f).sound(SoundType.WOOD)));
    public static final DeferredBlock<Block> CRYSTAL_TREE_LOG = BLOCKS.register("crystal_tree_log",
            () -> new Block(BlockBehaviour.Properties.of().strength(0.2f).sound(SoundType.WOOD)));

    // Flowers
    public static final DeferredBlock<Block> FLOWER_PINK = BLOCKS.register("flower_pink",
            () -> new Block(BlockBehaviour.Properties.of().strength(0.0f).noCollission().sound(SoundType.GRASS)));
    public static final DeferredBlock<Block> FLOWER_BLUE = BLOCKS.register("flower_blue",
            () -> new Block(BlockBehaviour.Properties.of().strength(0.0f).noCollission().sound(SoundType.GRASS)));
    public static final DeferredBlock<Block> FLOWER_BLACK = BLOCKS.register("flower_black",
            () -> new Block(BlockBehaviour.Properties.of().strength(0.0f).noCollission().sound(SoundType.GRASS)));
    public static final DeferredBlock<Block> FLOWER_SCARY = BLOCKS.register("flower_scary",
            () -> new Block(BlockBehaviour.Properties.of().strength(0.0f).noCollission().sound(SoundType.GRASS)));
    public static final DeferredBlock<Block> CRYSTAL_FLOWER_RED = BLOCKS.register("crystal_flower_red",
            () -> new Block(BlockBehaviour.Properties.of().strength(0.0f).noCollission().sound(SoundType.GRASS)));
    public static final DeferredBlock<Block> CRYSTAL_FLOWER_GREEN = BLOCKS.register("crystal_flower_green",
            () -> new Block(BlockBehaviour.Properties.of().strength(0.0f).noCollission().sound(SoundType.GRASS)));
    public static final DeferredBlock<Block> CRYSTAL_FLOWER_BLUE = BLOCKS.register("crystal_flower_blue",
            () -> new Block(BlockBehaviour.Properties.of().strength(0.0f).noCollission().sound(SoundType.GRASS)));
    public static final DeferredBlock<Block> CRYSTAL_FLOWER_YELLOW = BLOCKS.register("crystal_flower_yellow",
            () -> new Block(BlockBehaviour.Properties.of().strength(0.0f).noCollission().sound(SoundType.GRASS)));

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

    // Ant blocks
    public static final DeferredBlock<Block> ANT_BLOCK = BLOCKS.register("ant_block",
            () -> new Block(BlockBehaviour.Properties.of().strength(2.0f, 10.0f)));
    public static final DeferredBlock<Block> RED_ANT_BLOCK = BLOCKS.register("red_ant_block",
            () -> new Block(BlockBehaviour.Properties.of().strength(2.0f, 10.0f)));
    public static final DeferredBlock<Block> TERMITE_BLOCK = BLOCKS.register("termite_block",
            () -> new Block(BlockBehaviour.Properties.of().strength(2.0f, 10.0f)));
    public static final DeferredBlock<Block> CRYSTAL_TERMITE_BLOCK = BLOCKS.register("crystal_termite_block",
            () -> new Block(BlockBehaviour.Properties.of().strength(2.0f, 10.0f)));
    public static final DeferredBlock<Block> RAINBOW_ANT_BLOCK = BLOCKS.register("rainbow_ant_block",
            () -> new Block(BlockBehaviour.Properties.of().strength(2.0f, 10.0f)));
    public static final DeferredBlock<Block> UNSTABLE_ANT_BLOCK = BLOCKS.register("unstable_ant_block",
            () -> new Block(BlockBehaviour.Properties.of().strength(2.0f, 10.0f)));

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
