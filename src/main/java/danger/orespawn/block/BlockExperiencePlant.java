package danger.orespawn.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import danger.orespawn.ModBlocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import com.mojang.serialization.MapCodec;

/**
 * Experience tree sapling ("experiencesapling"), ported from 1.7.10
 * {@code BlockExperiencePlant} + {@code Trees.ExperienceTree}.
 *
 * <p>Trigger (orig BlockExperiencePlant.java:42-51, {@code func_149674_a}):
 * every random tick, server side, a 1-in-10 roll replaces the plant with air
 * and calls {@code OreSpawnTrees.ExperienceTree(world, x, y-1, z)} — i.e. the
 * tree builder receives the SOIL position and builds the whole tree in one
 * call. This is the tree's ONLY trigger in 1.7.10; it has no natural worldgen
 * (WGEN-045 / trees_spec.md section 1 — the only caller in the entire original
 * tree is BlockExperiencePlant:50).</p>
 *
 * <p>The build runs at play time on a live {@link ServerLevel}, so the
 * original's mid-build world reads (the {@code make_leaves} air checks,
 * orig Trees.java:188) are legal here — this is NOT the chunk-stitching
 * worldgen regime of {@code structure_conversion_pattern.md} section 1 step 3.</p>
 *
 * <p>Geometry (orig Trees.java:294-333): 2x2 vanilla-oak trunk (the wood is
 * deliberately vanilla oak, only the leaves are custom — trees_spec.md
 * section 13.4), four large two-pronged droop branches at y+6, mid trunk to
 * y+18, four small branches at y+19, then a 5-10 block crown wrapped in
 * experience leaves. All writes use flag 2 (orig
 * {@code OreSpawnMain.setBlockFast} flags=2 — no neighbor updates).</p>
 *
 * <p>Adaptation: experience leaves are placed with {@code PERSISTENT=true}.
 * 1.7.10 leaf decay was metadata/neighbor driven and flag-2 writes left those
 * leaves stable; a modern {@link LeavesBlock} placed with flag 2 keeps its
 * default DISTANCE=7 (no neighbor updates run) and would decay on random
 * ticks. Same adaptation precedent as the Duplicator Tree's apple leaves
 * ({@link BlockDuplicatorLog}).</p>
 */
public class BlockExperiencePlant extends BushBlock {
    /** orig BlockExperiencePlant.java:46 — nextInt(10) != 1 means no growth this tick. */
    private static final int GROWTH_ROLL_BOUND = 10;
    private static final int GROWTH_SUCCESS_INDEX = 1;
    /** Particle animation (orig BlockExperiencePlant.java:33-40): roll bound and burst count. */
    private static final int ANIMATE_TICK_ROLL_BOUND = 20;
    private static final int ANIMATE_SUCCESS_INDEX = 1;
    private static final int HAPPY_VILLAGER_BURST_COUNT = 20;

    @Override
    protected MapCodec<? extends BlockExperiencePlant> codec() {
        return simpleCodec(BlockExperiencePlant::new);
    }

    private static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 16, 14);

    public BlockExperiencePlant(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        Block block = state.getBlock();
        return block == Blocks.GRASS_BLOCK || block == Blocks.DIRT || block == Blocks.FARMLAND;
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.isClientSide()) return;
        // orig BlockExperiencePlant.java:46 — 1-in-10 per random tick
        if (random.nextInt(GROWTH_ROLL_BOUND) != GROWTH_SUCCESS_INDEX) return;

        // orig BlockExperiencePlant.java:49-50 — plant becomes air (flag 2), then
        // the tree is built from the SOIL position (y-1)
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
        experienceTree(level, random, pos.getX(), pos.getY() - 1, pos.getZ());
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        // orig BlockExperiencePlant.java:33-40 (func_149734_b) — 1/20 roll, 20 particles
        if (random.nextInt(ANIMATE_TICK_ROLL_BOUND) != ANIMATE_SUCCESS_INDEX) return;
        for (int p = 0; p < HAPPY_VILLAGER_BURST_COUNT; p++) {
            level.addParticle(ParticleTypes.HAPPY_VILLAGER,
                    pos.getX() + random.nextFloat(),
                    pos.getY() + random.nextFloat(),
                    pos.getZ() + random.nextFloat(),
                    0, 0, 0);
        }
    }

    // =====================================================================
    // Faithful port of Trees.ExperienceTree (orig Trees.java:294-333) and its
    // helpers make_leaves (:184-194), grow_branch (:245-292),
    // grow_small_branch (:196-243). Resolves WGEN-045 (the previous geometry
    // here was a self-declared placeholder).
    // =====================================================================

    /**
     * orig Trees.java:294-333 — {@code (x,y,z)} is the SOIL position (the
     * caller passes the plant's y-1, orig BlockExperiencePlant.java:50).
     * Single-shot build: soil gate, 2x2 trunk in three sections, four large
     * branches at y+6, four small branches at y+19, leafy crown.
     */
    private static void experienceTree(ServerLevel level, RandomSource random, int x, int y, int z) {
        // orig Trees.java:298-301 — block AT (x,y,z) must be grass/dirt/farmland
        BlockState soil = level.getBlockState(new BlockPos(x, y, z));
        if (!soil.is(Blocks.GRASS_BLOCK) && !soil.is(Blocks.DIRT) && !soil.is(Blocks.FARMLAND)) {
            return;
        }

        BlockState log = Blocks.OAK_LOG.defaultBlockState();

        // orig Trees.java:302-308 — lower trunk: 2x2 oak, y+1..y+5
        for (int j = 1; j < 6; j++) {
            for (int i = 0; i < 2; i++) {
                for (int k = 0; k < 2; k++) {
                    level.setBlock(new BlockPos(x + i, y + j, z + k), log, 2);
                }
            }
        }

        // orig Trees.java:309-312 — four large branches from the trunk corners at
        // y+6 (that trunk layer is filled by each branch's first log; the 2x2
        // loops deliberately skip j==6)
        growBranch(level, random, x, y + 6, z, 0, 1, 1, 1);
        growBranch(level, random, x + 1, y + 6, z, 1, 0, 1, -1);
        growBranch(level, random, x, y + 6, z + 1, -1, 0, -1, 1);
        growBranch(level, random, x + 1, y + 6, z + 1, 0, -1, -1, -1);

        // orig Trees.java:313-319 — mid trunk: 2x2 oak, y+7..y+18
        for (int j = 7; j < 19; j++) {
            for (int i = 0; i < 2; i++) {
                for (int k = 0; k < 2; k++) {
                    level.setBlock(new BlockPos(x + i, y + j, z + k), log, 2);
                }
            }
        }

        // orig Trees.java:320-323 — four small branches from the corners at y+19
        growSmallBranch(level, random, x, y + 19, z, 0, 1, -1, 1);
        growSmallBranch(level, random, x + 1, y + 19, z, 1, 0, 1, 1);
        growSmallBranch(level, random, x, y + 19, z + 1, -1, 0, -1, -1);
        growSmallBranch(level, random, x + 1, y + 19, z + 1, 0, -1, 1, -1);

        // orig Trees.java:324-332 — crown: 2x2 oak for 5+nextInt(6) layers
        // (y+19..y+28 max), leaves around every crown log
        int grow = 5 + random.nextInt(6);
        for (int j = 19; j < 19 + grow; j++) {
            for (int i = 0; i < 2; i++) {
                for (int k = 0; k < 2; k++) {
                    level.setBlock(new BlockPos(x + i, y + j, z + k), log, 2);
                    makeLeaves(level, x + i, y + j, z + k);
                }
            }
        }
    }

    /**
     * orig Trees.java:184-194 ({@code make_leaves}) — 7x7x3 box (l1,l2 in
     * -3..3, l3 in 0..2) of experience leaves, AIR CELLS ONLY (world read per
     * cell, orig :188 — legal on a live ServerLevel). Called at every placed
     * branch/crown log, so leaves never overwrite wood placed earlier.
     */
    private static void makeLeaves(ServerLevel level, int x, int y, int z) {
        // PERSISTENT=true adaptation — see class Javadoc.
        BlockState leaves = ModBlocks.EXPERIENCE_LEAVES.get().defaultBlockState()
                .setValue(LeavesBlock.PERSISTENT, true);
        for (int l1 = -3; l1 <= 3; l1++) {
            for (int l2 = -3; l2 <= 3; l2++) {
                for (int l3 = 0; l3 <= 2; l3++) {
                    BlockPos pos = new BlockPos(x + l1, y + l3, z + l2);
                    if (level.getBlockState(pos).isAir()) {
                        level.setBlock(pos, leaves, 2);
                    }
                }
            }
        }
    }

    /**
     * orig Trees.java:245-292 ({@code grow_branch}) — five segments; every
     * placed log also gets {@link #makeLeaves} at itself:
     * <ol>
     *   <li>rising, 5+nextInt(4) steps: up 1, out (xdir,zdir) per step (:253-260);
     *       the seg-1 endpoint (i2,k2) is the fork point;</li>
     *   <li>level main arm, 6+nextInt(5) steps out (xdir,zdir) (:261-267);</li>
     *   <li>level fork, 6+nextInt(5) steps out (xxdir,zzdir) from the fork
     *       point at the same height (:268-274);</li>
     *   <li>after dropping one ({@code j2 = --j}, :275): drooping main,
     *       4+nextInt(4) steps out and down 1 per step (:276-283);</li>
     *   <li>drooping fork, 4+nextInt(4) steps likewise (:284-291).</li>
     * </ol>
     */
    private static void growBranch(ServerLevel level, RandomSource random,
                                   int x, int y, int z, int xdir, int zdir, int xxdir, int zzdir) {
        BlockState log = Blocks.OAK_LOG.defaultBlockState();
        int i = x, j = y, k = z;
        int i2, k2, j2;

        // orig Trees.java:253-260 — seg 1 (rising), 5+nextInt(4)
        int grow = 5 + random.nextInt(4);
        for (int n = 0; n < grow; n++) {
            level.setBlock(new BlockPos(i, j, k), log, 2);
            makeLeaves(level, i, j, k);
            j++;
            i += xdir;
            k += zdir;
        }
        i2 = i;
        k2 = k;

        // orig Trees.java:261-267 — seg 2 (level, main arm), 6+nextInt(5)
        grow = 6 + random.nextInt(5);
        for (int n = 0; n < grow; n++) {
            level.setBlock(new BlockPos(i, j, k), log, 2);
            makeLeaves(level, i, j, k);
            i += xdir;
            k += zdir;
        }

        // orig Trees.java:268-274 — seg 3 (level, fork), 6+nextInt(5)
        grow = 6 + random.nextInt(5);
        for (int n = 0; n < grow; n++) {
            level.setBlock(new BlockPos(i2, j, k2), log, 2);
            makeLeaves(level, i2, j, k2);
            i2 += xxdir;
            k2 += zzdir;
        }

        // orig Trees.java:275 — both droop segments start one below seg 2/3's level
        j--;
        j2 = j;

        // orig Trees.java:276-283 — seg 4 (drooping, main), 4+nextInt(4)
        grow = 4 + random.nextInt(4);
        for (int n = 0; n < grow; n++) {
            level.setBlock(new BlockPos(i, j, k), log, 2);
            makeLeaves(level, i, j, k);
            i += xdir;
            k += zdir;
            j--;
        }

        // orig Trees.java:284-291 — seg 5 (drooping, fork), 4+nextInt(4)
        grow = 4 + random.nextInt(4);
        for (int n = 0; n < grow; n++) {
            level.setBlock(new BlockPos(i2, j2, k2), log, 2);
            makeLeaves(level, i2, j2, k2);
            i2 += xxdir;
            k2 += zzdir;
            j2--;
        }
    }

    /**
     * orig Trees.java:196-243 ({@code grow_small_branch}) — identical
     * five-segment structure and cursor math to {@link #growBranch}, smaller
     * rolls: seg1 4+nextInt(2) (:204), seg2 4+nextInt(3) (:212), seg3
     * 4+nextInt(3) (:219), seg4 3+nextInt(3) (:227), seg5 3+nextInt(3) (:235).
     */
    private static void growSmallBranch(ServerLevel level, RandomSource random,
                                        int x, int y, int z, int xdir, int zdir, int xxdir, int zzdir) {
        BlockState log = Blocks.OAK_LOG.defaultBlockState();
        int i = x, j = y, k = z;
        int i2, k2, j2;

        // orig Trees.java:204-211 — seg 1 (rising), 4+nextInt(2)
        int grow = 4 + random.nextInt(2);
        for (int n = 0; n < grow; n++) {
            level.setBlock(new BlockPos(i, j, k), log, 2);
            makeLeaves(level, i, j, k);
            j++;
            i += xdir;
            k += zdir;
        }
        i2 = i;
        k2 = k;

        // orig Trees.java:212-218 — seg 2 (level, main arm), 4+nextInt(3)
        grow = 4 + random.nextInt(3);
        for (int n = 0; n < grow; n++) {
            level.setBlock(new BlockPos(i, j, k), log, 2);
            makeLeaves(level, i, j, k);
            i += xdir;
            k += zdir;
        }

        // orig Trees.java:219-225 — seg 3 (level, fork), 4+nextInt(3)
        grow = 4 + random.nextInt(3);
        for (int n = 0; n < grow; n++) {
            level.setBlock(new BlockPos(i2, j, k2), log, 2);
            makeLeaves(level, i2, j, k2);
            i2 += xxdir;
            k2 += zzdir;
        }

        // orig Trees.java:226 — j2 = --j
        j--;
        j2 = j;

        // orig Trees.java:227-234 — seg 4 (drooping, main), 3+nextInt(3)
        grow = 3 + random.nextInt(3);
        for (int n = 0; n < grow; n++) {
            level.setBlock(new BlockPos(i, j, k), log, 2);
            makeLeaves(level, i, j, k);
            i += xdir;
            k += zdir;
            j--;
        }

        // orig Trees.java:235-242 — seg 5 (drooping, fork), 3+nextInt(3)
        grow = 3 + random.nextInt(3);
        for (int n = 0; n < grow; n++) {
            level.setBlock(new BlockPos(i2, j2, k2), log, 2);
            makeLeaves(level, i2, j2, k2);
            i2 += xxdir;
            k2 += zzdir;
            j2--;
        }
    }
}
