package danger.orespawn.entity.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Luna Moth flight behaviour.
 *
 * <p>Extends {@link AmbientFlightGoal} with the iconic 1.7.10 "moths are
 * attracted to torches" behaviour. When the mob is under cover (sky not
 * visible) there is a 1-in-10 chance per retarget to expand a cube search
 * up to 15 blocks and path toward the nearest {@link Blocks#TORCH} or
 * {@link Blocks#WALL_TORCH}.
 *
 * <p>Cost bound: worst-case cube scan is {@code (15+15) × 2 × (15+1) × (15+1)}
 * = ~15 K block lookups per scan, but scans happen at most every ~100
 * ticks with an additional 1-in-10 gate, so the amortised rate is
 * ~15 lookups per tick per moth. Aborts the radius expansion as soon as
 * any torch is found in the current shell (see 1.7.10 source
 * {@code EntityLunaMoth.java:scanForTorches}).
 */
public class LunaMothFlightGoal extends AmbientFlightGoal {
    private static final int TORCH_SCAN_RARE_CHANCE = 10;
    private static final int TORCH_SCAN_MIN_RADIUS = 2;
    private static final int TORCH_SCAN_MAX_RADIUS = 15;
    private static final int TORCH_SCAN_SKIP_AFTER = 6;
    private static final int NO_MATCH = Integer.MAX_VALUE;

    public LunaMothFlightGoal(Mob mob) {
        super(mob, Params.lunaMoth());
    }

    @Override
    protected BlockPos pickRetarget() {
        // Outdoor moths always random-wander — matches 1.7.10.
        if (this.mob.level().canSeeSky(this.mob.blockPosition())) {
            return super.pickRetarget();
        }
        if (this.mob.getRandom().nextInt(TORCH_SCAN_RARE_CHANCE) != 0) {
            return super.pickRetarget();
        }
        BlockPos closestTorch = findClosestTorch();
        if (closestTorch != null) return closestTorch.above();
        return super.pickRetarget();
    }

    /**
     * Expanding shell search for the nearest torch. Mirrors the 1.7.10
     * algorithm which also mirrors + scans both positive and negative
     * X shells per radius step.
     */
    private BlockPos findClosestTorch() {
        int baseX = (int) this.mob.getX();
        int baseY = (int) this.mob.getY();
        int baseZ = (int) this.mob.getZ();
        int bestDistSq = NO_MATCH;
        BlockPos best = null;
        int radius = TORCH_SCAN_MIN_RADIUS;
        while (radius < TORCH_SCAN_MAX_RADIUS) {
            for (int iy = -radius; iy <= radius; iy++) {
                for (int jz = -radius; jz <= radius; jz++) {
                    BlockPos plusX = new BlockPos(baseX + radius, baseY + iy, baseZ + jz);
                    BlockPos minusX = new BlockPos(baseX - radius, baseY + iy, baseZ + jz);
                    int distSq = radius * radius + iy * iy + jz * jz;
                    BlockState sp = this.mob.level().getBlockState(plusX);
                    if ((sp.is(Blocks.TORCH) || sp.is(Blocks.WALL_TORCH)) && distSq < bestDistSq) {
                        bestDistSq = distSq;
                        best = plusX;
                    }
                    BlockState sn = this.mob.level().getBlockState(minusX);
                    if ((sn.is(Blocks.TORCH) || sn.is(Blocks.WALL_TORCH)) && distSq < bestDistSq) {
                        bestDistSq = distSq;
                        best = minusX;
                    }
                }
            }
            if (best != null) return best;
            // 1.7.10 quirk — after an early miss the shell step doubles so
            // we don't spend all our budget at close range. Preserved here
            // so moth behaviour is unchanged from the legacy mod.
            if (radius >= TORCH_SCAN_SKIP_AFTER) radius++;
            radius++;
        }
        return null;
    }
}
