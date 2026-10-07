package danger.orespawn.world.carver;

import danger.orespawn.OreSpawnMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.carver.CanyonCarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CanyonWorldCarver;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.carver.CaveCarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CaveWorldCarver;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Function;

/**
 * The caves and ravines of the dimensions built on the 1.7.10 terrain (Utopia, the Village and Mining): vanilla's cave
 * and canyon carvers, carving as the original's MapGenCaves and MapGenRavine did. The terrain there has no aquifers (every
 * empty cell below Y63 is still water, as 1.7.10 filled it), and a vanilla carver asks the aquifer what a carved cell
 * holds, which would flood every cave below Y63; the original carved air, lava at the carver's lava level and below,
 * and left a stretch of tunnel uncarved when water stood on the shell of its box (orig MapGenCaves.generateCaveNode,
 * MapGenRavine.func_151540_a: the same water check). These carvers do both.
 */
public final class LegacyCarvers {
    public static final DeferredRegister<WorldCarver<?>> CARVERS = DeferredRegister.create(Registries.CARVER, OreSpawnMod.MOD_ID);

    public static final DeferredHolder<WorldCarver<?>, Cave> CAVE =
            CARVERS.register("legacy_cave", () -> new Cave(CaveCarverConfiguration.CODEC));
    public static final DeferredHolder<WorldCarver<?>, Canyon> CANYON =
            CARVERS.register("legacy_canyon", () -> new Canyon(CanyonCarverConfiguration.CODEC));

    /** Air in every carved cell; the carvers' own lava level comes first. */
    private static final Aquifer DRY = new Aquifer() {
        @Override
        public BlockState computeSubstance(DensityFunction.FunctionContext context, double substance) {
            return substance > 0.0 ? null : Blocks.AIR.defaultBlockState();
        }

        @Override
        public boolean shouldScheduleFluidUpdate() {
            return false;
        }
    };

    private LegacyCarvers() {
    }

    public static void register(IEventBus eventBus) {
        CARVERS.register(eventBus);
    }

    /**
     * Whether water stands on the shell of the box the original checked before carving a stretch: x and z from one
     * below the ellipsoid's floor to one past its ceiling, inside the chunk; y from one under its bottom to one over
     * its top, clamped to 1 and 120; every height at the box's edge columns, the top and bottom inside.
     */
    static boolean waterOnShell(ChunkAccess chunk, double x, double y, double z, double horizontalRadius, double verticalRadius) {
        int minX = chunk.getPos().getMinBlockX();
        int minZ = chunk.getPos().getMinBlockZ();
        int x0 = Math.max(Mth.floor(x - horizontalRadius) - minX - 1, 0);
        int x1 = Math.min(Mth.floor(x + horizontalRadius) - minX + 1, 16);
        int y0 = Math.max(Mth.floor(y - verticalRadius) - 1, 1);
        int y1 = Math.min(Mth.floor(y + verticalRadius) + 1, 120);
        int z0 = Math.max(Mth.floor(z - horizontalRadius) - minZ - 1, 0);
        int z1 = Math.min(Mth.floor(z + horizontalRadius) - minZ + 1, 16);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int bx = x0; bx < x1; bx++) {
            for (int bz = z0; bz < z1; bz++) {
                boolean edge = bx == x0 || bx == x1 - 1 || bz == z0 || bz == z1 - 1;
                for (int by = y1 + 1; by >= y0 - 1; by--) {
                    if (!edge && by != y1 + 1 && by != y0 - 1) continue;
                    if (by < chunk.getMinBuildHeight() || by >= chunk.getMaxBuildHeight()) continue;
                    if (chunk.getFluidState(cursor.set(minX + bx, by, minZ + bz)).is(FluidTags.WATER)) return true;
                }
            }
        }
        return false;
    }

    /** Vanilla's cave carver, dry and stopping at water as the original's. */
    public static final class Cave extends CaveWorldCarver {
        Cave(com.mojang.serialization.Codec<CaveCarverConfiguration> codec) {
            super(codec);
        }

        @Override
        public boolean carve(CarvingContext context, CaveCarverConfiguration config, ChunkAccess chunk,
                             Function<BlockPos, Holder<Biome>> biomes, RandomSource random, Aquifer aquifer, ChunkPos chunkPos,
                             CarvingMask mask) {
            return super.carve(context, config, chunk, biomes, random, DRY, chunkPos, mask);
        }

        @Override
        protected boolean carveEllipsoid(CarvingContext context, CaveCarverConfiguration config, ChunkAccess chunk,
                                         Function<BlockPos, Holder<Biome>> biomes, Aquifer aquifer, double x, double y,
                                         double z, double horizontalRadius, double verticalRadius, CarvingMask mask,
                                         WorldCarver.CarveSkipChecker skip) {
            if (waterOnShell(chunk, x, y, z, horizontalRadius, verticalRadius)) return false;
            return super.carveEllipsoid(context, config, chunk, biomes, aquifer, x, y, z, horizontalRadius, verticalRadius,
                    mask, skip);
        }
    }

    /** Vanilla's canyon carver, dry and stopping at water as the original's ravines. */
    public static final class Canyon extends CanyonWorldCarver {
        Canyon(com.mojang.serialization.Codec<CanyonCarverConfiguration> codec) {
            super(codec);
        }

        @Override
        public boolean carve(CarvingContext context, CanyonCarverConfiguration config, ChunkAccess chunk,
                             Function<BlockPos, Holder<Biome>> biomes, RandomSource random, Aquifer aquifer, ChunkPos chunkPos,
                             CarvingMask mask) {
            return super.carve(context, config, chunk, biomes, random, DRY, chunkPos, mask);
        }

        @Override
        protected boolean carveEllipsoid(CarvingContext context, CanyonCarverConfiguration config, ChunkAccess chunk,
                                         Function<BlockPos, Holder<Biome>> biomes, Aquifer aquifer, double x, double y,
                                         double z, double horizontalRadius, double verticalRadius, CarvingMask mask,
                                         WorldCarver.CarveSkipChecker skip) {
            if (waterOnShell(chunk, x, y, z, horizontalRadius, verticalRadius)) return false;
            return super.carveEllipsoid(context, config, chunk, biomes, aquifer, x, y, z, horizontalRadius, verticalRadius,
                    mask, skip);
        }
    }
}
