package danger.orespawn.block;

import danger.orespawn.ModBlocks;
import danger.orespawn.OreSpawnConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * Duplicator Log — 1.7.10 port. Two behaviors are layered:
 * <ol>
 *   <li><b>Tree growth</b> (preserved): occasionally spawns a small leaf-capped
 *   trunk above itself when sky is open, mirroring legacy
 *   {@code OreSpawnTrees.DuplicatorTree}. Toggleable via
 *   {@link OreSpawnConfig#DUPLICATOR_TREE_ENABLE}.</li>
 *   <li><b>Adjacency duplication</b> (Phase 11): on each random tick, scans
 *   the 6 neighbouring positions for sapling blocks and any item entities
 *   floating in a 1-block AABB around the log. With low probability it
 *   duplicates the sapling onto a free side or grows the dropped item stack
 *   by one. This is the closest the legacy code ever got to actually
 *   "duplicating" — its name was always somewhat aspirational.</li>
 * </ol>
 */
public class BlockDuplicatorLog extends Block {
    private static final float ADJACENT_DUPE_CHANCE = 0.20f;

    public BlockDuplicatorLog(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.isClientSide()) return;

        // Behavior A: adjacency duplication (always on; cheap)
        tryDuplicateAdjacent(level, pos, random);

        // Behavior B: legacy tree growth — opt-in via config
        if (!OreSpawnConfig.DUPLICATOR_TREE_ENABLE.get()) return;
        if (random.nextInt(5) != 0) return;

        BlockPos above = pos.above();
        if (!level.isEmptyBlock(above)) return;

        int height = 4 + random.nextInt(3);
        BlockState logState = ModBlocks.DUPLICATOR_LOG.get().defaultBlockState();

        for (int y = 1; y <= height; y++) {
            BlockPos logPos = pos.above(y);
            if (level.isEmptyBlock(logPos) || level.getBlockState(logPos).canBeReplaced()) {
                level.setBlock(logPos, logState, 3);
            }
        }

        BlockState leafState = Blocks.OAK_LEAVES.defaultBlockState();
        int leafStart = height - 2;
        for (int dy = leafStart; dy <= height + 1; dy++) {
            int radius = (dy <= height - 1) ? 2 : 1;
            if (dy == height + 1) radius = 0;
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx == 0 && dz == 0 && dy <= height) continue;
                    BlockPos leafPos = pos.above(dy).offset(dx, 0, dz);
                    if (level.isEmptyBlock(leafPos) || level.getBlockState(leafPos).canBeReplaced()) {
                        level.setBlock(leafPos, leafState, 3);
                    }
                }
            }
        }
    }

    private void tryDuplicateAdjacent(ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextFloat() >= ADJACENT_DUPE_CHANCE) return;

        // Sapling duplication: copy onto a free horizontal neighbour
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos neighbour = pos.relative(dir);
            BlockState neighbourState = level.getBlockState(neighbour);
            if (!(neighbourState.getBlock() instanceof SaplingBlock)) continue;

            for (Direction copyDir : Direction.Plane.HORIZONTAL) {
                if (copyDir == dir) continue;
                BlockPos target = pos.relative(copyDir);
                if (!level.isEmptyBlock(target)) continue;
                BlockPos belowTarget = target.below();
                BlockState below = level.getBlockState(belowTarget);
                if (below.is(Blocks.GRASS_BLOCK) || below.is(Blocks.DIRT)
                        || below.is(Blocks.PODZOL) || below.is(Blocks.COARSE_DIRT)) {
                    level.setBlock(target, neighbourState, 3);
                    return; // one duplication per tick
                }
            }
        }

        // Item duplication: grow a dropped stack by 1 if it sits adjacent
        AABB region = new AABB(pos).inflate(1.0);
        List<ItemEntity> nearby = level.getEntitiesOfClass(ItemEntity.class, region,
                ItemEntity::isAlive);
        if (nearby.isEmpty()) return;
        ItemEntity victim = nearby.get(random.nextInt(nearby.size()));
        ItemStack stack = victim.getItem();
        if (stack.getCount() < stack.getMaxStackSize()) {
            stack.grow(1);
            victim.setItem(stack); // ensure sync
        }
    }
}
