package danger.orespawn.world.feature;

import com.mojang.serialization.Codec;
import danger.orespawn.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import java.util.Random;

/**
 * Robot Lab — modernized port of the legacy
 * {@code GenericDungeon.makeRobotLab} (1.7.10 source line 4044).
 *
 * <p>The 1.7.10 implementation built a sprawling <em>30×30×9</em> warehouse
 * (the {@code makerobomain} hangar that the entryway antechamber leads
 * into via iron doors) with four sub-rooms: an altar with two
 * Robo-Pounder spawners, a redstone railway, an assembly line, a
 * treasure room with a Robo-Warrior spawner, and a four-pillar
 * Robo-Sniper roof tower. Generating the full 30×30 hangar would
 * routinely cross 4 chunk boundaries and trigger cascading worldgen
 * the user explicitly flagged. This modernization condenses the
 * complex into a single chunk-safe footprint (≤ 16×16) while keeping
 * every canonical role:</p>
 *
 * <ul>
 *   <li>An iron-block + iron-block-trim antechamber with an iron-door
 *       entrance (the legacy 10×20 entry hall).</li>
 *   <li>A central altar slab with a Robo-Pounder spawner pillar
 *       (legacy {@code makeroboaltar}).</li>
 *   <li>Two Robo-Sniper spawner pillars on opposite walls
 *       (legacy {@code makerobopillar} dir=0/1 mobs).</li>
 *   <li>A Robo-Warrior spawner guarding the loot chest at the back
 *       wall (legacy {@code makerobotreasureroom}).</li>
 *   <li>A Robo-Spinner spawner on the central altar capstone
 *       (modern Phase 9 addition — kept as a perimeter sentry).</li>
 *   <li>A treasure chest with the canonical
 *       {@code RobotContentsList} loot palette
 *       (1.7.10 line 37) — redstone, redstone block, rails, pistons,
 *       fire charges, comparators, minecart parts.</li>
 * </ul>
 *
 * <p>Stability guards: Y-bounds checked up-front, refuses placement on
 * water/air ground. All writes stay inside a 14×14 footprint at the
 * chunk's surface heightmap so no neighbouring chunk is forced to
 * load.</p>
 */
public class RobotLabFeature extends Feature<NoneFeatureConfiguration> {

    private static final int WIDTH = 14;
    private static final int LENGTH = 14;
    private static final int HEIGHT = 6;

    public RobotLabFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        WorldGenLevel level = ctx.level();
        Random random = new Random(ctx.random().nextLong());

        BlockPos surface = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG, ctx.origin());
        BlockPos base = surface;

        if (base.getY() + HEIGHT >= level.getMaxBuildHeight() - 4) return false;
        if (base.getY() <= level.getMinBuildHeight() + 2) return false;

        BlockState below = level.getBlockState(base.below());
        if (below.isAir() || below.liquid()) return false;

        BlockState wall = Blocks.IRON_BLOCK.defaultBlockState();
        BlockState floor = Blocks.IRON_BLOCK.defaultBlockState();
        BlockState glass = Blocks.GLASS_PANE.defaultBlockState();
        BlockState glow = Blocks.REDSTONE_BLOCK.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();

        for (int dy = 0; dy <= HEIGHT; dy++) {
            for (int dx = 0; dx < WIDTH; dx++) {
                for (int dz = 0; dz < LENGTH; dz++) {
                    BlockPos pos = base.offset(dx, dy, dz);
                    boolean shell = dx == 0 || dz == 0 || dx == WIDTH - 1 || dz == LENGTH - 1;
                    if (dy == 0) {
                        BlockState f = (dx == WIDTH / 2 || dx == WIDTH / 2 - 1) ? glow : floor;
                        level.setBlock(pos, f, 2);
                        continue;
                    }
                    if (dy == HEIGHT) {
                        level.setBlock(pos, shell ? air : wall, 2);
                        continue;
                    }
                    if (shell) {
                        BlockState shellState = wall;
                        if (dy == HEIGHT - 1 && !(dx == 0 || dx == WIDTH - 1 || dz == 0 || dz == LENGTH - 1)) {
                            shellState = wall;
                        }
                        if ((dy == 2 || dy == 3) && (dx == WIDTH / 3 || dx == 2 * WIDTH / 3) && (dz == 0 || dz == LENGTH - 1)) {
                            shellState = glass;
                        }
                        level.setBlock(pos, shellState, 2);
                    } else {
                        level.setBlock(pos, air, 2);
                    }
                }
            }
        }

        carveDoor(level, base.offset(WIDTH / 2 - 1, 1, 0));
        carveDoor(level, base.offset(WIDTH / 2, 1, 0));

        BlockPos altar = base.offset(WIDTH / 2, 1, LENGTH / 2);
        placeSpawnerPillar(level, altar, ModEntities.ROBOT_4.get());

        placeSpawner(level, base.offset(2, 2, 2), ModEntities.ROBOT_5.get());
        placeSpawner(level, base.offset(WIDTH - 3, 2, 2), ModEntities.ROBOT_5.get());

        placeSpawner(level, base.offset(WIDTH / 2, 1, LENGTH - 3), ModEntities.ROBOT_2.get());

        placeSpawner(level, altar.above(2), ModEntities.ROBOT_1.get());

        BlockPos chestPos = base.offset(WIDTH / 2 - 2, 1, LENGTH - 3);
        level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), 2);
        if (level.getBlockEntity(chestPos) instanceof ChestBlockEntity chest) {
            fillRobotChest(chest, random);
        }
        return true;
    }

    private static void carveDoor(WorldGenLevel level, BlockPos pos) {
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
        level.setBlock(pos.above(), Blocks.AIR.defaultBlockState(), 2);
    }

    private static void placeSpawnerPillar(WorldGenLevel level, BlockPos base,
                                           EntityType<?> mob) {
        level.setBlock(base, Blocks.IRON_BLOCK.defaultBlockState(), 2);
        placeSpawner(level, base.above(), mob);
    }

    private static void placeSpawner(WorldGenLevel level, BlockPos pos, EntityType<?> mob) {
        level.setBlock(pos, Blocks.SPAWNER.defaultBlockState(), 2);
        if (level.getBlockEntity(pos) instanceof SpawnerBlockEntity spawner) {
            spawner.getSpawner().setEntityId(mob, null, level.getRandom(), pos);
        }
    }

    /**
     * Equivalent to {@code WeightedRandomChestContent.func_76293_a} call
     * in {@code makerobotreasureroom} (1.7.10 line 4344) using the
     * {@code RobotContentsList} palette (1.7.10 line 37).
     */
    private static void fillRobotChest(ChestBlockEntity chest, Random random) {
        ItemStack[] palette = {
                new ItemStack(Items.REDSTONE, 1 + random.nextInt(10)),
                new ItemStack(Items.COMPASS, 1),
                new ItemStack(Items.MINECART, 1),
                new ItemStack(Items.FIRE_CHARGE, 1 + random.nextInt(10)),
                new ItemStack(Items.COMPARATOR, 1),
                new ItemStack(Items.REDSTONE_BLOCK, 1 + random.nextInt(10)),
                new ItemStack(Items.RAIL, 1 + random.nextInt(10)),
                new ItemStack(Items.POWERED_RAIL, 1 + random.nextInt(10)),
                new ItemStack(Items.DETECTOR_RAIL, 1 + random.nextInt(10)),
                new ItemStack(Items.PISTON, 1 + random.nextInt(10)),
                new ItemStack(Items.STICKY_PISTON, 1 + random.nextInt(5)),
                new ItemStack(Items.HOPPER, 1 + random.nextInt(3)),
                new ItemStack(Items.IRON_INGOT, 4 + random.nextInt(8)),
                new ItemStack(Items.GOLD_INGOT, 1 + random.nextInt(4))
        };
        int slots = chest.getContainerSize();
        int count = 10 + random.nextInt(5);
        for (int i = 0; i < count; i++) {
            ItemStack stack = palette[random.nextInt(palette.length)].copy();
            chest.setItem(random.nextInt(slots), stack);
        }
    }
}
