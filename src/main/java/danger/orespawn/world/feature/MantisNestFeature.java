package danger.orespawn.world.feature;

import com.mojang.serialization.Codec;
import danger.orespawn.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Mantis Nest — small surface-level mob spawner cluster. Generates a 3x3
 * mossy/cracked-stone hump on the ground with a Mantis spawner buried just
 * beneath the centre tile. Inspired by 1.7.10's
 * {@code OreSpawnTrees.makeMantisHive} micro-dungeon, but trimmed down so it
 * can ride on a {@link Feature} pipeline without needing a full structure.
 */
public class MantisNestFeature extends Feature<NoneFeatureConfiguration> {
    public MantisNestFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        WorldGenLevel level = ctx.level();
        BlockPos origin = ctx.origin();

        // Step up to surface
        BlockPos surface = level.getHeightmapPos(
                net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE_WG, origin);

        // Reject ocean / not-grass surfaces
        BlockState below = level.getBlockState(surface.below());
        if (!(below.is(Blocks.GRASS_BLOCK) || below.is(Blocks.SAND) || below.is(Blocks.DIRT))) {
            return false;
        }

        BlockState mossy = Blocks.MOSSY_COBBLESTONE.defaultBlockState();
        BlockState crackedStone = Blocks.CRACKED_STONE_BRICKS.defaultBlockState();

        // 3x3 cobbled platform 1 deep at surface
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos floor = surface.offset(dx, -1, dz);
                level.setBlock(floor, ctx.random().nextInt(3) == 0 ? crackedStone : mossy, 2);
            }
        }

        // Centre hump: spawner pillar + mossy roof
        BlockPos centre = surface;
        level.setBlock(centre, Blocks.SPAWNER.defaultBlockState(), 2);
        if (level.getBlockEntity(centre) instanceof SpawnerBlockEntity spawner) {
            spawner.getSpawner().setEntityId(ModEntities.ENTITY_MANTIS.get(), null,
                    ctx.random(), centre);
        }
        level.setBlock(centre.above(), mossy, 2);

        // Spit a few mantis claw decorations around as natural debris
        for (int dx = -1; dx <= 1; dx += 2) {
            for (int dz = -1; dz <= 1; dz += 2) {
                BlockPos torchPos = surface.offset(dx, 0, dz);
                if (level.getBlockState(torchPos).isAir()) {
                    level.setBlock(torchPos, Blocks.COBWEB.defaultBlockState(), 2);
                }
            }
        }

        return true;
    }
}
