package danger.orespawn.world.feature;

import com.mojang.serialization.Codec;
import danger.orespawn.ModEntities;
import danger.orespawn.ModItems;
import net.minecraft.core.BlockPos;
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

/**
 * White House — direct port of the legacy
 * {@code GenericDungeon.makeWhiteHouse} (1.7.10 source line 5423). The
 * canonical structure is a simple 2-storey mansion with 4 corner Criminal
 * spawners and a centre treasure chest.
 *
 * <p>Modernized in 1.21.1 to:</p>
 * <ul>
 *   <li>Use {@link Heightmap.Types#WORLD_SURFACE_WG} so the floor sits
 *       flush regardless of natural terrain undulation.</li>
 *   <li>Bound-check the entire footprint up front so a partially-flat
 *       chunk doesn't half-build the structure.</li>
 *   <li>Use {@link Blocks#WHITE_TERRACOTTA} + quartz columns so the
 *       silhouette reads as the iconic neoclassical White House without
 *       requiring a custom block (legacy used vanilla white wool which
 *       burned trivially).</li>
 * </ul>
 */
public class WhiteHouseFeature extends Feature<NoneFeatureConfiguration> {
    private static final int WIDTH = 13;
    private static final int DEPTH = 11;
    private static final int HEIGHT = 7;

    public WhiteHouseFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        WorldGenLevel level = ctx.level();
        BlockPos surface = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG, ctx.origin());
        BlockPos origin = surface;

        if (origin.getY() + HEIGHT >= level.getMaxBuildHeight() - 4) return false;
        BlockState floorBelow = level.getBlockState(origin.below());
        if (floorBelow.isAir() || floorBelow.liquid()) return false;

        BlockState wall = Blocks.WHITE_TERRACOTTA.defaultBlockState();
        BlockState column = Blocks.QUARTZ_PILLAR.defaultBlockState();
        BlockState floor = Blocks.SMOOTH_QUARTZ.defaultBlockState();
        BlockState roof = Blocks.QUARTZ_BLOCK.defaultBlockState();

        // Footprint: floor + walls + flat roof
        for (int dx = 0; dx < WIDTH; dx++) {
            for (int dz = 0; dz < DEPTH; dz++) {
                level.setBlock(origin.offset(dx, 0, dz), floor, 2);
                level.setBlock(origin.offset(dx, HEIGHT - 1, dz), roof, 2);
                for (int dy = 1; dy < HEIGHT - 1; dy++) {
                    BlockPos pos = origin.offset(dx, dy, dz);
                    boolean isWall = dx == 0 || dx == WIDTH - 1 || dz == 0 || dz == DEPTH - 1;
                    if (isWall) {
                        level.setBlock(pos, wall, 2);
                    } else {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
        }

        // 4 entrance columns on the front face
        for (int colDx : new int[]{2, 4, 6, 8, 10}) {
            for (int dy = 1; dy < HEIGHT - 1; dy++) {
                level.setBlock(origin.offset(colDx, dy, 0), column, 2);
            }
        }
        // Door arch — knock 2 holes in the front wall for entry
        level.setBlock(origin.offset(WIDTH / 2, 1, 0), Blocks.AIR.defaultBlockState(), 2);
        level.setBlock(origin.offset(WIDTH / 2, 2, 0), Blocks.AIR.defaultBlockState(), 2);

        // 4 Criminal spawners — one in each corner of the interior
        // (lines 5648-5694 of legacy source: tileentitymobspawner.func_98272_a("Criminal"))
        BlockPos[] corners = {
                origin.offset(2, 1, 2),
                origin.offset(WIDTH - 3, 1, 2),
                origin.offset(2, 1, DEPTH - 3),
                origin.offset(WIDTH - 3, 1, DEPTH - 3)
        };
        for (BlockPos corner : corners) {
            level.setBlock(corner, Blocks.SPAWNER.defaultBlockState(), 2);
            if (level.getBlockEntity(corner) instanceof SpawnerBlockEntity spawner) {
                spawner.getSpawner().setEntityId(ModEntities.BAND_P.get(),
                        null, ctx.random(), corner);
            }
        }

        // Centre treasure chest with WhiteHouseContentsList equivalents.
        BlockPos chestPos = origin.offset(WIDTH / 2, 1, DEPTH / 2);
        level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), 2);
        if (level.getBlockEntity(chestPos) instanceof ChestBlockEntity chest) {
            chest.setItem(0, new ItemStack(ModItems.URANIUM_NUGGET.get(), 2 + ctx.random().nextInt(4)));
            chest.setItem(2, new ItemStack(ModItems.TITANIUM_NUGGET.get(), 2 + ctx.random().nextInt(4)));
            chest.setItem(4, new ItemStack(Items.EMERALD, 6 + ctx.random().nextInt(10)));
            chest.setItem(6, new ItemStack(Items.DIAMOND, 4 + ctx.random().nextInt(8)));
            chest.setItem(8, new ItemStack(Items.GOLD_INGOT, 6 + ctx.random().nextInt(10)));
            chest.setItem(10, new ItemStack(Items.COOKED_PORKCHOP, 6 + ctx.random().nextInt(10)));
            chest.setItem(13, new ItemStack(ModItems.BAND_P_SPAWN_EGG.get(), 4 + ctx.random().nextInt(6)));
        }
        return true;
    }
}
