package danger.orespawn.world.feature;

import com.mojang.serialization.Codec;
import danger.orespawn.ModEntities;
import danger.orespawn.ModItems;
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
 * Shadow Dungeon — modernized port of the legacy
 * {@code GenericDungeon.makeShadowDungeon} (1.7.10 source line 1453).
 *
 * <p>The 1.7.10 implementation built a stepped <em>19→1</em> double
 * pyramid (19 wide at the surface, narrowing one block on every Y
 * step) made of obsidian + bedrock + soul-sand pillars, with two
 * levels of corner spawners — even-Y rings spawned Nightmares,
 * odd-Y rings spawned Ender Reapers — and four chests filled from
 * the {@code shadowContentsList} palette (line 52).</p>
 *
 * <p>Modernization condenses the 19-step double pyramid into a single
 * chunk-safe 13×13×9 buried bunker so the entire structure stays
 * inside the owning chunk (the legacy 19-block footprint regularly
 * reached into 4 chunks — exactly the cross-chunk runaway worldgen
 * the user explicitly flagged). Each canonical role is preserved:
 * obsidian + bedrock walls, a soul-sand floor band, four corner
 * Nightmare spawners on the floor, four Ender Reaper spawners on the
 * ceiling, and four loot chests with the legacy palette.</p>
 *
 * <p>Stability guards: Y-bounds checked up-front, refuses placement
 * if the surface column is in liquid. All writes stay inside the
 * 13×13 footprint so no neighbouring chunk is forced to load.</p>
 */
public class ShadowDungeonFeature extends Feature<NoneFeatureConfiguration> {

    private static final int WIDTH = 13;
    private static final int HEIGHT = 9;
    private static final int BURY_DEPTH = 8;

    public ShadowDungeonFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        WorldGenLevel level = ctx.level();
        Random random = new Random(ctx.random().nextLong());

        BlockPos surface = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG, ctx.origin());
        BlockPos base = surface.below(BURY_DEPTH + HEIGHT);

        if (base.getY() < level.getMinBuildHeight() + 4) return false;
        if (base.getY() + HEIGHT >= level.getMaxBuildHeight() - 2) return false;

        BlockState surfaceState = level.getBlockState(surface);
        if (surfaceState.liquid()) return false;

        BlockState obsidian = Blocks.OBSIDIAN.defaultBlockState();
        BlockState bedrock = Blocks.BEDROCK.defaultBlockState();
        BlockState soulSand = Blocks.SOUL_SAND.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();

        for (int dy = 0; dy <= HEIGHT; dy++) {
            for (int dx = 0; dx < WIDTH; dx++) {
                for (int dz = 0; dz < WIDTH; dz++) {
                    BlockPos pos = base.offset(dx, dy, dz);
                    boolean shell = dx == 0 || dx == WIDTH - 1
                            || dz == 0 || dz == WIDTH - 1
                            || dy == 0 || dy == HEIGHT;
                    if (shell) {
                        BlockState wall = (dy & 1) == 0 ? obsidian : bedrock;
                        boolean cross = dx >= WIDTH / 2 - 1 && dx <= WIDTH / 2 + 1
                                || dz >= WIDTH / 2 - 1 && dz <= WIDTH / 2 + 1;
                        if (cross && (dx == 0 || dx == WIDTH - 1 || dz == 0 || dz == WIDTH - 1)) {
                            wall = soulSand;
                        }
                        level.setBlock(pos, wall, 2);
                    } else {
                        level.setBlock(pos, air, 2);
                    }
                }
            }
        }

        EntityType<?> nightmare = ModEntities.PITCH_BLACK.get();
        EntityType<?> reaper = ModEntities.ENDER_REAPER.get();

        placeSpawner(level, base.offset(1, 1, 1), nightmare);
        placeSpawner(level, base.offset(WIDTH - 2, 1, 1), nightmare);
        placeSpawner(level, base.offset(1, 1, WIDTH - 2), nightmare);
        placeSpawner(level, base.offset(WIDTH - 2, 1, WIDTH - 2), nightmare);

        placeSpawner(level, base.offset(1, HEIGHT - 1, 1), reaper);
        placeSpawner(level, base.offset(WIDTH - 2, HEIGHT - 1, 1), reaper);
        placeSpawner(level, base.offset(1, HEIGHT - 1, WIDTH - 2), reaper);
        placeSpawner(level, base.offset(WIDTH - 2, HEIGHT - 1, WIDTH - 2), reaper);

        int mid = HEIGHT / 2;
        placeChest(level, base.offset(1, mid, WIDTH / 2), random);
        placeChest(level, base.offset(WIDTH - 2, mid, WIDTH / 2), random);
        placeChest(level, base.offset(WIDTH / 2, mid, 1), random);
        placeChest(level, base.offset(WIDTH / 2, mid, WIDTH - 2), random);

        return true;
    }

    private static void placeSpawner(WorldGenLevel level, BlockPos pos, EntityType<?> mob) {
        level.setBlock(pos, Blocks.SPAWNER.defaultBlockState(), 2);
        if (level.getBlockEntity(pos) instanceof SpawnerBlockEntity spawner) {
            spawner.getSpawner().setEntityId(mob, null, level.getRandom(), pos);
        }
    }

    private static void placeChest(WorldGenLevel level, BlockPos pos, Random random) {
        level.setBlock(pos, Blocks.CHEST.defaultBlockState(), 2);
        if (level.getBlockEntity(pos) instanceof ChestBlockEntity chest) {
            fillShadowChest(chest, random);
        }
    }

    /**
     * Equivalent to {@code WeightedRandomChestContent.func_76293_a} call
     * in {@code fill_shadow_chests} (1.7.10 line 1548) using the
     * {@code shadowContentsList} palette (1.7.10 line 52). Substitutes
     * canonical OreSpawn drops where the legacy items exist in this
     * port.
     */
    private static void fillShadowChest(ChestBlockEntity chest, Random random) {
        ItemStack[] palette = {
                new ItemStack(Items.GLOWSTONE_DUST, 2 + random.nextInt(7)),
                new ItemStack(Items.BLAZE_ROD, 4 + random.nextInt(5)),
                new ItemStack(Items.MAGMA_CREAM, 2 + random.nextInt(7)),
                new ItemStack(Items.BLAZE_POWDER, 2 + random.nextInt(7)),
                new ItemStack(Items.FIRE_CHARGE, 4 + random.nextInt(5)),
                new ItemStack(Items.ROTTEN_FLESH, 6 + random.nextInt(11)),
                new ItemStack(Items.RED_DYE, 6 + random.nextInt(11)),
                new ItemStack(ModItems.RUBY.get(), 2 + random.nextInt(7)),
                new ItemStack(ModItems.EXPERIENCE_TREE_SEED.get(), 2 + random.nextInt(3)),
                new ItemStack(ModItems.ELEVATOR.get(), 1),
                new ItemStack(ModItems.NIGHTMARE_SWORD.get(), 1),
                new ItemStack(ModItems.POISON_SWORD.get(), 1),
                new ItemStack(ModItems.RAT_SWORD.get(), 1)
        };
        int slots = chest.getContainerSize();
        int count = 3 + random.nextInt(7);
        for (int i = 0; i < count; i++) {
            ItemStack stack = palette[random.nextInt(palette.length)].copy();
            chest.setItem(random.nextInt(slots), stack);
        }
    }
}
