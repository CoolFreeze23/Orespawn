package danger.orespawn.world.feature;

import com.mojang.serialization.Codec;
import danger.orespawn.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Beehive — small mossy-cobble box with a Bee spawner inside, perched on
 * forest/jungle tree canopies. Replaces the legacy 1.7.10
 * {@code OreSpawnTrees.makeBeeHive} micro-dungeon with a feature pipeline
 * variant. Smaller and faster to place than the full structure system would
 * allow, no structure-pool jigsaw plumbing required.
 */
public class BeehiveFeature extends Feature<NoneFeatureConfiguration> {
    public BeehiveFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        WorldGenLevel level = ctx.level();
        BlockPos origin = ctx.origin();

        // Place at canopy height (top of leaves) — biggest visual presence
        BlockPos surface = level.getHeightmapPos(
                Heightmap.Types.WORLD_SURFACE_WG, origin).above(2);

        BlockState mossy = Blocks.MOSSY_COBBLESTONE.defaultBlockState();
        BlockState honey = Blocks.HONEYCOMB_BLOCK.defaultBlockState();

        // 3x2x3 hive shell
        for (int dy = 0; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos pos = surface.offset(dx, dy, dz);
                    boolean isShell = (dx == -1 || dx == 1 || dz == -1 || dz == 1 || dy == 0 || dy == 1);
                    if (isShell) {
                        BlockState shell = ctx.random().nextInt(3) == 0 ? honey : mossy;
                        level.setBlock(pos, shell, 2);
                    } else {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
        }

        // Spawner buried in the centre top tile
        BlockPos spawnerPos = surface.above();
        level.setBlock(spawnerPos, Blocks.SPAWNER.defaultBlockState(), 2);
        if (level.getBlockEntity(spawnerPos) instanceof SpawnerBlockEntity spawner) {
            spawner.getSpawner().setEntityId(ModEntities.ENTITY_BEE.get(), null,
                    ctx.random(), spawnerPos);
        }
        level.setBlock(spawnerPos.above(), mossy, 2);

        return true;
    }
}
