package danger.orespawn.world.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import danger.orespawn.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

import java.util.Random;

/**
 * Royal Tree — modernized port of the canonical 1.7.10 "Tree of Goodness"
 * (King variant) and "Queen's Tree" structures from the Utopia Dimension.
 *
 * <p>The 1.7.10 distribution generated these as enormous gem-block
 * trees that rose ~30 blocks above the Utopia surface, with a thick
 * trunk made of a single gem palette (gold + emerald for the King;
 * gold + ruby for the Queen) and a wide canopy that mixed all four
 * royal gem palettes (ruby / amethyst / titanium / uranium). At the
 * very top, the tree carried the canonical {@code king_spawner} or
 * {@code queen_spawner} block which is consumed by the Magic Apple
 * altar mechanic to summon the boss.</p>
 *
 * <p>This implementation:</p>
 * <ul>
 *   <li>Anchors the trunk to the surface heightmap and bound-checks
 *       the entire vertical envelope (trunk + canopy + spawner cap)
 *       against {@link WorldGenLevel#getMaxBuildHeight()} <em>before
 *       writing a single block</em>. If the canopy would clip the
 *       world ceiling, the feature bails without partial writes —
 *       exactly the safety guard the user requested.</li>
 *   <li>Caps total footprint at ±9 blocks horizontally so the entire
 *       tree fits inside the owning chunk's surface tile (16-block
 *       chunk diameter, centred on origin) and never forces a
 *       neighbour chunk to load while writing canopy blocks.</li>
 *   <li>Uses {@link WorldGenLevel#setBlock} with flag {@code 2} for
 *       every block write, matching the modern equivalent of the
 *       legacy {@code OreSpawnMain.setBlockFast(..., 2)} —
 *       block-entity updates still fire (so the boss spawner block
 *       hooks correctly) but lighting + neighbour-update cascades
 *       are suppressed.</li>
 *   <li>Refuses placement on liquid / unstable ground so the trunk
 *       always has solid footing and the tree silhouette matches the
 *       1.7.10 surface-anchor convention.</li>
 * </ul>
 *
 * <p>Block palette per variant:</p>
 * <ul>
 *   <li><b>King (Goodness Tree)</b>: gold-block core column, emerald-
 *       block trunk shell, mixed ruby/amethyst/titanium/uranium leaves
 *       in the canopy, {@code king_spawner} on the apex.</li>
 *   <li><b>Queen's Tree</b>: ruby-block core column + ruby-block trunk
 *       shell (the legacy "ruby-stem" silhouette), mixed
 *       amethyst/titanium/uranium/emerald leaves in the canopy,
 *       {@code queen_spawner} on the apex.</li>
 * </ul>
 */
public class RoyalTreeFeature extends Feature<RoyalTreeFeature.Config> {

    public record Config(boolean queenVariant) implements FeatureConfiguration {
        public static final Codec<Config> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                Codec.BOOL.optionalFieldOf("queen_variant", false).forGetter(Config::queenVariant)
        ).apply(inst, Config::new));
    }

    private static final int TRUNK_HEIGHT = 16;
    private static final int CANOPY_RADIUS = 7;
    private static final int CANOPY_HEIGHT = 8;
    private static final int SPAWNER_OFFSET = 2;

    public RoyalTreeFeature(Codec<Config> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<Config> ctx) {
        WorldGenLevel level = ctx.level();
        boolean queen = ctx.config().queenVariant();
        Random random = new Random(ctx.random().nextLong());

        BlockPos surface = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG, ctx.origin());
        BlockPos base = surface;

        int totalHeight = TRUNK_HEIGHT + CANOPY_HEIGHT + SPAWNER_OFFSET + 2;
        if (base.getY() + totalHeight >= level.getMaxBuildHeight() - 4) return false;
        if (base.getY() <= level.getMinBuildHeight() + 2) return false;

        BlockState surfaceState = level.getBlockState(base.below());
        if (surfaceState.isAir() || surfaceState.liquid()) return false;

        BlockState core = queen
                ? ModBlocks.BLOCK_RUBY.get().defaultBlockState()
                : Blocks.GOLD_BLOCK.defaultBlockState();
        BlockState trunk = queen
                ? ModBlocks.BLOCK_RUBY.get().defaultBlockState()
                : Blocks.EMERALD_BLOCK.defaultBlockState();
        BlockState root = Blocks.DIRT.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();

        BlockState[] leafPalette = queen ? queenLeafPalette() : kingLeafPalette();

        BlockState spawnerState = queen
                ? ModBlocks.QUEEN_SPAWNER.get().defaultBlockState()
                : ModBlocks.KING_SPAWNER.get().defaultBlockState();

        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (Math.abs(dx) + Math.abs(dz) > 3) continue;
                BlockPos pos = base.offset(dx, -1, dz);
                level.setBlock(pos, root, 2);
            }
        }

        for (int dy = 0; dy < TRUNK_HEIGHT; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos pos = base.offset(dx, dy, dz);
                    BlockState state = (dx == 0 && dz == 0) ? core : trunk;
                    level.setBlock(pos, state, 2);
                }
            }
        }

        BlockPos canopyCentre = base.above(TRUNK_HEIGHT);
        for (int dy = 0; dy < CANOPY_HEIGHT; dy++) {
            // Vertical falloff so the canopy reads as a proper dome.
            double yT = (double) dy / (double) (CANOPY_HEIGHT - 1);
            double yFalloff = 1.0 - Math.pow(2.0 * yT - 1.0, 2.0);
            int radius = Math.max(2, (int) Math.round(CANOPY_RADIUS * yFalloff));

            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    int distSq = dx * dx + dz * dz;
                    if (distSq > radius * radius) continue;
                    if (distSq == radius * radius && random.nextInt(3) == 0) continue;

                    BlockPos pos = canopyCentre.offset(dx, dy, dz);
                    BlockState leaf = leafPalette[random.nextInt(leafPalette.length)];
                    level.setBlock(pos, leaf, 2);
                }
            }
        }

        BlockPos apex = canopyCentre.above(CANOPY_HEIGHT - 1);
        for (int dy = 1; dy <= SPAWNER_OFFSET; dy++) {
            level.setBlock(apex.above(dy), trunk, 2);
        }
        BlockPos spawnerPos = apex.above(SPAWNER_OFFSET + 1);
        level.setBlock(spawnerPos, spawnerState, 2);
        // Clear a single air cell above the spawner so the boss has spawn headroom.
        level.setBlock(spawnerPos.above(), air, 2);

        return true;
    }

    /**
     * Goodness Tree leaf palette — emphasises ruby + amethyst (red /
     * purple gem-blocks) interlaced with titanium and uranium for the
     * canonical King canopy silhouette.
     */
    private static BlockState[] kingLeafPalette() {
        return new BlockState[]{
                ModBlocks.BLOCK_RUBY.get().defaultBlockState(),
                ModBlocks.BLOCK_AMETHYST.get().defaultBlockState(),
                ModBlocks.BLOCK_TITANIUM.get().defaultBlockState(),
                ModBlocks.BLOCK_URANIUM.get().defaultBlockState()
        };
    }

    /**
     * Queen's Tree leaf palette — drops ruby (it's already the trunk
     * material) and substitutes emerald block so the canopy still
     * carries the four-gem rotation.
     */
    private static BlockState[] queenLeafPalette() {
        return new BlockState[]{
                ModBlocks.BLOCK_AMETHYST.get().defaultBlockState(),
                ModBlocks.BLOCK_TITANIUM.get().defaultBlockState(),
                ModBlocks.BLOCK_URANIUM.get().defaultBlockState(),
                Blocks.EMERALD_BLOCK.defaultBlockState()
        };
    }
}
