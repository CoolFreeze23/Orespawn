package danger.orespawn.world.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import java.util.Random;

/**
 * Round Tree &mdash; <b>Audit Part 1 new feature</b>.
 *
 * <p>Authentic byte-for-byte port of legacy
 * {@code danger.orespawn.ItemMagicApple.MakeBigRoundTree(world, x, y, z, ID, leafID, stepID, tree_type, t_radius, chunk)}
 * (1.7.10 source, lines 624&ndash;694) and its {@code MakeRoundBranch}
 * helper (lines 696&ndash;722).</p>
 *
 * <p><b>Legacy geometry (verified ItemMagicApple.java:624-694):</b></p>
 * <ol>
 *   <li>{@code rad = t_radius} (3&ndash;5 in worldgen, see legacy
 *       OreSpawnWorld.java:1872 &rarr; {@code 6 - nextInt(3)}).</li>
 *   <li>For each degree i in [0, 360): drop a vertical {@code OAK_LOG}
 *       column from {@code (x + rad*sin(i), y, z + rad*cos(i))} down
 *       up to 20 blocks (the foot anchors / underground trunk).</li>
 *   <li>Loop {@code while (rad > 0)} growing upward, shrinking the
 *       radius by {@code 0.01 * nextInt(15)} each step:
 *       <ul>
 *         <li>Stamp an {@code OAK_LOG} ring at the current radius.</li>
 *         <li>If {@code cury > rad}, branch out via
 *             {@link #makeRoundBranch} at a random angle every
 *             {@code 80 + nextInt(80)} degrees of total branch
 *             rotation.</li>
 *         <li>Every 6 Y steps when {@code rad > 3}, fill the disk
 *             with {@code OAK_LOG} (creates the "tiered platform"
 *             look the user described).</li>
 *         <li>If the centre of the disk lands on air, plug a
 *             {@code DIAMOND_BLOCK} top cap there (legacy line 692,
 *             {@code Blocks.field_150484_ah}).</li>
 *       </ul>
 *   </li>
 * </ol>
 *
 * <p><b>Spawn frequency (verified OreSpawnWorld.java:1830-1880,
 * addHugeTree):</b> {@code random.nextInt(50) != 0} gating, then
 * {@code rand_treetype = nextInt(100)} bracket selection
 * &rarr; <b>{@code rand_treetype in [1, 15]}</b> (15% of triggers)
 * picks the Round Tree branch &rarr; net <b>~1 round tree per 333
 * chunks</b> in the Utopia dimension only.</p>
 */
public class RoundTreeFeature extends Feature<NoneFeatureConfiguration> {
    public RoundTreeFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        WorldGenLevel level = ctx.level();
        Random random = new Random(ctx.random().nextLong());

        BlockPos surface = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG, ctx.origin());
        BlockState below = level.getBlockState(surface.below());
        if (!(below.is(Blocks.GRASS_BLOCK) || below.is(Blocks.DIRT))) return false;

        // Legacy radius spread (OreSpawnWorld.java:1872 + 1849).
        double rad = 6 - random.nextInt(3);
        if (surface.getY() + 60 >= level.getMaxBuildHeight() - 2) return false;

        BlockState log = Blocks.OAK_LOG.defaultBlockState();
        // QA fix: persistent + DISTANCE=1 so the canopy never decays.
        BlockState leaves = Blocks.OAK_LEAVES.defaultBlockState()
                .setValue(LeavesBlock.PERSISTENT, true)
                .setValue(LeavesBlock.DISTANCE, 1);
        BlockState diamondCap = Blocks.DIAMOND_BLOCK.defaultBlockState();

        float fx = surface.getX() + 0.5f;
        float fz = surface.getZ() + 0.5f;

        // Phase 1: foot anchors (legacy lines 637-651).
        for (int i = 0; i < 360; i++) {
            float fcurx = (float) (rad * Math.sin(Math.toRadians(i)));
            float fcurz = (float) (rad * Math.cos(Math.toRadians(i)));
            for (int j = 0; j < 20; j++) {
                int yy = surface.getY() - j;
                if (yy <= level.getMinBuildHeight()) break;
                BlockPos pos = new BlockPos((int) (fx + fcurx), yy, (int) (fz + fcurz));
                BlockState here = level.getBlockState(pos);
                // Legacy isBoringBaseBlock check: stop when we hit terrain.
                if (!here.isAir() && !here.is(Blocks.GRASS_BLOCK)) break;
                level.setBlock(pos, log, 2);
            }
        }

        // Phase 2: spiral trunk + ring growth (legacy lines 652-693).
        int cury = 1;
        int ibranch = 0;
        while (rad > 0.0) {
            for (int i = 0; i < 360; i++) {
                float fcurx = (float) (rad * Math.sin(Math.toRadians(i)));
                float fcurz = (float) (rad * Math.cos(Math.toRadians(i)));
                BlockPos pos = new BlockPos((int) (fx + fcurx), surface.getY() + cury, (int) (fz + fcurz));
                level.setBlock(pos, log, 2);
            }
            if (cury > (int) rad) {
                ibranch += 80 + random.nextInt(80);
                if (ibranch > 360) ibranch -= 360;
                int ibranchlen = (int) (rad * 5.0) + random.nextInt((int) rad + 2);
                float fcurx = (float) (rad * Math.sin(Math.toRadians(ibranch)));
                float fcurz = (float) (rad * Math.cos(Math.toRadians(ibranch)));
                makeRoundBranch(level, ibranch, ibranchlen, (int) rad + 1,
                        fx + fcurx, surface.getY() + cury, fz + fcurz, log, leaves, random);
            }
            // Every 6 Y steps when rad > 3, fill the disc (legacy lines 676-690).
            if (cury % 6 == 0 && rad > 3.0) {
                for (double dr = rad - 0.25; dr > 0.0; dr -= 0.25) {
                    for (int i = 0; i < 360; i++) {
                        float fcurx = (float) (dr * Math.sin(Math.toRadians(i)));
                        float fcurz = (float) (dr * Math.cos(Math.toRadians(i)));
                        BlockPos pos = new BlockPos((int) (fx + fcurx), surface.getY() + cury, (int) (fz + fcurz));
                        level.setBlock(pos, log, 2);
                    }
                }
            }
            rad -= 0.01 * random.nextInt(15);
            cury++;
            if (rad > 0.0) continue;
            // Final iteration: cap with diamond block (legacy line 692).
            BlockPos cap = new BlockPos((int) fx, surface.getY() + cury, (int) fz);
            if (level.getBlockState(cap).isAir()) {
                level.setBlock(cap, diamondCap, 2);
            }
        }
        return true;
    }

    /** Direct port of {@code MakeRoundBranch} (ItemMagicApple.java:696-722). */
    private static void makeRoundBranch(WorldGenLevel level, int iangle, int branchlen, int width,
                                         float startx, int starty, float startz,
                                         BlockState log, BlockState leaves, Random random) {
        double deltadir = 0.06283185200000001; // ~3.6 degrees per step
        double deltamag = 0.35f;
        double currad = iangle;
        float fcurx = startx;
        float fcurz = startz;
        for (int i = 0; i < branchlen; i++) {
            currad += (random.nextDouble() - 0.5) * deltadir;
            fcurx += (float) (deltamag * Math.sin(Math.toRadians(currad)));
            fcurz += (float) (deltamag * Math.cos(Math.toRadians(currad)));
            int ix = (int) fcurx;
            int iz = (int) fcurz;
            BlockPos branchPos = new BlockPos(ix, starty, iz);
            level.setBlock(branchPos, log, 2);
            // Leaf canopy at the branch tip (every step).
            BlockPos leafPos = branchPos.above();
            if (level.getBlockState(leafPos).isAir()) {
                level.setBlock(leafPos, leaves, 2);
            }
        }
    }
}
