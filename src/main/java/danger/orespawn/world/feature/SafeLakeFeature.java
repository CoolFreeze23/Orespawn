package danger.orespawn.world.feature;

import com.mojang.serialization.Codec;
import danger.orespawn.world.GenerationRange;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.LakeFeature;

/**
 * Chunk-border-safe copy of the deprecated vanilla {@link LakeFeature},
 * used for the port's classic 1.7.10-style water/lava lakes in the Village
 * and Mining dimensions (C7 parity — {@code lake_water_dim} /
 * {@code lake_lava_dim}).
 *
 * <p><b>Why this exists (TEST-003, 2026-08-10):</b> vanilla removed classic
 * lakes from worldgen in 1.19, leaving {@code LakeFeature}'s final
 * ice-freeze pass with a latent chunk-border defect: it samples
 * {@code getBiome(origin + 0..15)}, and {@link
 * net.minecraft.world.level.biome.BiomeManager}'s zoom fuzz can push that
 * lookup up to ~4 blocks further — a lake whose {@code in_square} origin
 * hugs the +x/+z chunk corner can therefore query a biome TWO chunks out,
 * beyond the radius the FEATURES stage guarantees, throwing
 * {@code IllegalStateException: Requested chunk unavailable during world
 * generation} and killing the integrated server (reproduced twice in the
 * Village dimension, 2026-08-10 test session).</p>
 *
 * <p>The body below is a line-for-line copy of the decompiled 1.21.1
 * {@code LakeFeature.place} (same RNG draw sequence, same writes) with ONE
 * change: the freeze-check biome lookup is clamped to the origin's chunk
 * (the ice itself still places at the true position). In the single-biome
 * Village/Mining dimensions the clamped sample resolves to the identical
 * biome, so behavior is unchanged; in any multi-biome use the freeze check
 * would approximate the biome by at most 15 blocks — documented delta.</p>
 *
 * <p>Second change: the lake first sinks through air to the ground, as 1.7.10's WorldGenLakes does
 * ({@code while (y > 5 && world.isAirBlock(x, y, z)) --y}), so the original's lakes rolled at any height over the land
 * lie on it rather than fail in the air.</p>
 *
 * <p>Third: no lake in village land. The original's Village skips both its lakes in a chunk whose population window
 * (16 blocks square, from 8 in) a village's box meets ({@code !flag}, ChunkProviderOreSpawn3.java:288, 292 and 298);
 * here a lake is skipped when a piece of a structure in {@code #orespawn:lakes_avoid} (the Village's villages) meets
 * that window. The villages here are 1.21's, several times the original's, so their pieces are read rather than their
 * whole box; their starts are found through the references of the chunks the window meets, the chunk and its east,
 * south and south-east neighbours.</p>
 *
 * <p>Fourth: the grass step 1.7.10's WorldGenLakes has and 1.21's dropped: the dirt under the lake's open part turned
 * to grass where the sky's light reaches the cell over it (the light as the original had it while decorating,
 * {@link danger.orespawn.world.LegacyLight}).</p>
 */
public class SafeLakeFeature extends Feature<LakeFeature.Configuration> {
    private static final BlockState AIR = Blocks.CAVE_AIR.defaultBlockState();
    /** The structures a lake keeps out of, as the original's Village kept its lakes out of its villages. */
    public static final net.minecraft.tags.TagKey<net.minecraft.world.level.levelgen.structure.Structure> LAKES_AVOID =
            net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.STRUCTURE,
                    net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("orespawn", "lakes_avoid"));

    public SafeLakeFeature(Codec<LakeFeature.Configuration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<LakeFeature.Configuration> context) {
        BlockPos blockpos = context.origin();
        WorldGenLevel worldgenlevel = context.level();
        RandomSource randomsource = context.random();
        LakeFeature.Configuration lakefeature$configuration = context.config();
        // Post-in_square origin sits inside the decorated chunk — its bounds
        // are the safe window for biome lookups (see class Javadoc).
        int chunkMinX = context.origin().getX() & ~15;
        int chunkMinZ = context.origin().getZ() & ~15;
        if (inVillageLand(worldgenlevel, chunkMinX >> 4, chunkMinZ >> 4)) {
            return false;
        }
        // 1.7.10's WorldGenLakes first sinks through air to the ground (while y > 5 and the block is air), so a lake
        // rolled over the land lies on it; 1.21's copy left that to the placement, and these lakes' placement draws Y.
        int bottom = GenerationRange.bottom(context.chunkGenerator(), worldgenlevel);
        while (blockpos.getY() > bottom + 5 && worldgenlevel.isEmptyBlock(blockpos)) {
            blockpos = blockpos.below();
        }
        if (blockpos.getY() <= bottom + 4) {
            return false;
        } else {
            blockpos = blockpos.below(4);
            boolean[] aboolean = new boolean[2048];
            int i = randomsource.nextInt(4) + 4;

            for (int j = 0; j < i; j++) {
                double d0 = randomsource.nextDouble() * 6.0 + 3.0;
                double d1 = randomsource.nextDouble() * 4.0 + 2.0;
                double d2 = randomsource.nextDouble() * 6.0 + 3.0;
                double d3 = randomsource.nextDouble() * (16.0 - d0 - 2.0) + 1.0 + d0 / 2.0;
                double d4 = randomsource.nextDouble() * (8.0 - d1 - 4.0) + 2.0 + d1 / 2.0;
                double d5 = randomsource.nextDouble() * (16.0 - d2 - 2.0) + 1.0 + d2 / 2.0;

                for (int l = 1; l < 15; l++) {
                    for (int i1 = 1; i1 < 15; i1++) {
                        for (int j1 = 1; j1 < 7; j1++) {
                            double d6 = ((double)l - d3) / (d0 / 2.0);
                            double d7 = ((double)j1 - d4) / (d1 / 2.0);
                            double d8 = ((double)i1 - d5) / (d2 / 2.0);
                            double d9 = d6 * d6 + d7 * d7 + d8 * d8;
                            if (d9 < 1.0) {
                                aboolean[(l * 16 + i1) * 8 + j1] = true;
                            }
                        }
                    }
                }
            }

            BlockState blockstate1 = lakefeature$configuration.fluid().getState(randomsource, blockpos);

            for (int k1 = 0; k1 < 16; k1++) {
                for (int k = 0; k < 16; k++) {
                    for (int l2 = 0; l2 < 8; l2++) {
                        boolean flag = !aboolean[(k1 * 16 + k) * 8 + l2]
                            && (
                                k1 < 15 && aboolean[((k1 + 1) * 16 + k) * 8 + l2]
                                    || k1 > 0 && aboolean[((k1 - 1) * 16 + k) * 8 + l2]
                                    || k < 15 && aboolean[(k1 * 16 + k + 1) * 8 + l2]
                                    || k > 0 && aboolean[(k1 * 16 + (k - 1)) * 8 + l2]
                                    || l2 < 7 && aboolean[(k1 * 16 + k) * 8 + l2 + 1]
                                    || l2 > 0 && aboolean[(k1 * 16 + k) * 8 + (l2 - 1)]
                            );
                        if (flag) {
                            BlockState blockstate3 = worldgenlevel.getBlockState(blockpos.offset(k1, l2, k));
                            if (l2 >= 4 && blockstate3.liquid()) {
                                return false;
                            }

                            if (l2 < 4 && !blockstate3.isSolid() && worldgenlevel.getBlockState(blockpos.offset(k1, l2, k)) != blockstate1) {
                                return false;
                            }
                        }
                    }
                }
            }

            for (int l1 = 0; l1 < 16; l1++) {
                for (int i2 = 0; i2 < 16; i2++) {
                    for (int i3 = 0; i3 < 8; i3++) {
                        if (aboolean[(l1 * 16 + i2) * 8 + i3]) {
                            BlockPos blockpos1 = blockpos.offset(l1, i3, i2);
                            if (this.canReplaceBlock(worldgenlevel.getBlockState(blockpos1))) {
                                boolean flag1 = i3 >= 4;
                                worldgenlevel.setBlock(blockpos1, flag1 ? AIR : blockstate1, 2);
                                if (flag1) {
                                    worldgenlevel.scheduleTick(blockpos1, AIR.getBlock(), 0);
                                    this.markAboveForPostProcessing(worldgenlevel, blockpos1);
                                }
                            }
                        }
                    }
                }
            }

            // 1.7.10's WorldGenLakes then turns the dirt under the lake's open part to grass where the sky's light
            // reaches the cell over it (to mycelium in a biome topped with mycelium, which these dimensions have none of)
            for (int gx = 0; gx < 16; gx++) {
                for (int gz = 0; gz < 16; gz++) {
                    for (int gy = 4; gy < 8; gy++) {
                        if (!aboolean[(gx * 16 + gz) * 8 + gy]) continue;
                        BlockPos under = blockpos.offset(gx, gy - 1, gz);
                        if (worldgenlevel.getBlockState(under).is(Blocks.DIRT)
                                && danger.orespawn.world.LegacyLight.sky(worldgenlevel, under.getX(), under.getY() + 1, under.getZ()) > 0) {
                            worldgenlevel.setBlock(under, Blocks.GRASS_BLOCK.defaultBlockState(), 2);
                        }
                    }
                }
            }

            BlockState blockstate2 = lakefeature$configuration.barrier().getState(randomsource, blockpos);
            if (!blockstate2.isAir()) {
                for (int j2 = 0; j2 < 16; j2++) {
                    for (int j3 = 0; j3 < 16; j3++) {
                        for (int l3 = 0; l3 < 8; l3++) {
                            boolean flag2 = !aboolean[(j2 * 16 + j3) * 8 + l3]
                                && (
                                    j2 < 15 && aboolean[((j2 + 1) * 16 + j3) * 8 + l3]
                                        || j2 > 0 && aboolean[((j2 - 1) * 16 + j3) * 8 + l3]
                                        || j3 < 15 && aboolean[(j2 * 16 + j3 + 1) * 8 + l3]
                                        || j3 > 0 && aboolean[(j2 * 16 + (j3 - 1)) * 8 + l3]
                                        || l3 < 7 && aboolean[(j2 * 16 + j3) * 8 + l3 + 1]
                                        || l3 > 0 && aboolean[(j2 * 16 + j3) * 8 + (l3 - 1)]
                                );
                            if (flag2 && (l3 < 4 || randomsource.nextInt(2) != 0)) {
                                BlockState blockstate = worldgenlevel.getBlockState(blockpos.offset(j2, l3, j3));
                                if (blockstate.isSolid() && !blockstate.is(BlockTags.LAVA_POOL_STONE_CANNOT_REPLACE)) {
                                    BlockPos blockpos3 = blockpos.offset(j2, l3, j3);
                                    worldgenlevel.setBlock(blockpos3, blockstate2, 2);
                                    this.markAboveForPostProcessing(worldgenlevel, blockpos3);
                                }
                            }
                        }
                    }
                }
            }

            if (blockstate1.getFluidState().is(FluidTags.WATER)) {
                for (int k2 = 0; k2 < 16; k2++) {
                    for (int k3 = 0; k3 < 16; k3++) {
                        int i4 = 4;
                        BlockPos blockpos2 = blockpos.offset(k2, 4, k3);
                        // THE ONE CHANGE vs vanilla (class Javadoc): sample the
                        // biome at a position clamped into the decorated chunk
                        // so BiomeManager's fuzz can never leave the guaranteed
                        // region; the freeze itself still targets blockpos2.
                        BlockPos biomeSample = new BlockPos(
                                Mth.clamp(blockpos2.getX(), chunkMinX, chunkMinX + 15),
                                blockpos2.getY(),
                                Mth.clamp(blockpos2.getZ(), chunkMinZ, chunkMinZ + 15));
                        if (shouldFreezeAt(worldgenlevel, blockpos2, biomeSample)
                            && this.canReplaceBlock(worldgenlevel.getBlockState(blockpos2))) {
                            worldgenlevel.setBlock(blockpos2, Blocks.ICE.defaultBlockState(), 2);
                        }
                    }
                }
            }

            return true;
        }
    }

    private boolean canReplaceBlock(BlockState state) {
        return !state.is(BlockTags.FEATURES_CANNOT_REPLACE);
    }

    /**
     * Inlined copy of {@code Biome.shouldFreeze(level, pos, mustBeAtEdge=false)}.
     *
     * <p><b>Why not call shouldFreeze directly (crash 2026-08-20, Village dim):</b>
     * Serene Seasons redirects the {@code warmEnoughToRain} call <i>inside</i>
     * {@code Biome.shouldFreeze} to a seasonal hook that performs its own
     * {@code level.getBiome(pos)} on the raw position — undoing this class's
     * clamped-sample guarantee and throwing {@code IllegalStateException:
     * Requested chunk unavailable during world generation} when the lake hugs
     * the decorated chunk's +x/+z corner. Inlining the vanilla logic keeps all
     * biome/temperature sampling on the clamped position and out of reach of
     * call-site redirects. Delta: generation-time ice ignores the current
     * season (base climate only), which is the saner behavior for worldgen
     * anyway.</p>
     */
    private static boolean shouldFreezeAt(WorldGenLevel level, BlockPos pos, BlockPos biomeSample) {
        Biome biome = level.getBiome(biomeSample).value();
        if (biome.warmEnoughToRain(biomeSample)) {
            return false;
        }
        if (pos.getY() < level.getMinBuildHeight() || pos.getY() >= level.getMaxBuildHeight()) {
            return false;
        }
        if (level.getBrightness(LightLayer.BLOCK, pos) >= 10) {
            return false;
        }
        BlockState state = level.getBlockState(pos);
        return level.getFluidState(pos).getType() == Fluids.WATER && state.getBlock() instanceof LiquidBlock;
    }

    /**
     * Whether a piece of a structure lakes keep out of meets the chunk's population window, the 16 x 16 blocks from eight
     * in: its starts are the ones the chunk and its east, south and south-east neighbours reference, the chunks the
     * window meets.
     */
    public static boolean inVillageLand(WorldGenLevel level, int chunkX, int chunkZ) {
        net.minecraft.core.Registry<net.minecraft.world.level.levelgen.structure.Structure> structures =
                level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.STRUCTURE);
        int minX = (chunkX << 4) + 8, minZ = (chunkZ << 4) + 8, maxX = minX + 15, maxZ = minZ + 15;
        for (int dx = 0; dx <= 1; dx++) {
            for (int dz = 0; dz <= 1; dz++) {
                for (var reference : level.getChunk(chunkX + dx, chunkZ + dz).getAllReferences().entrySet()) {
                    if (reference.getValue().isEmpty() || !structures.wrapAsHolder(reference.getKey()).is(LAKES_AVOID)) {
                        continue;
                    }
                    for (long origin : reference.getValue()) {
                        net.minecraft.world.level.ChunkPos start = new net.minecraft.world.level.ChunkPos(origin);
                        // a generating region holds the chunks a few round it only; a start beyond them is left out
                        // (the Village's villages start within eight chunks of every chunk they reach)
                        if (!level.hasChunk(start.x, start.z)) continue;
                        net.minecraft.world.level.chunk.ChunkAccess chunk = level.getChunk(start.x, start.z,
                                net.minecraft.world.level.chunk.status.ChunkStatus.STRUCTURE_STARTS, false);
                        var village = chunk == null ? null : chunk.getStartForStructure(reference.getKey());
                        if (village == null || !village.isValid()) continue;
                        for (var piece : village.getPieces()) {
                            var box = piece.getBoundingBox();
                            if (box.maxX() >= minX && box.minX() <= maxX && box.maxZ() >= minZ && box.minZ() <= maxZ) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
}
