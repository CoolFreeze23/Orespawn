package danger.orespawn.entity.ai;

import danger.orespawn.entity.EntityLunaMoth;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Luna Moth flight behaviour.
 *
 * <p>Extends {@link ButterflyIslandsHuntGoal} — the butterfly flight with the Islands vampire hunt in the retarget's
 * else branch, which orig EntityLunaMoth.java:117-122 inherited through {@code super.updateAITasks()} (the moth hunted
 * as a type-1 butterfly does, orig EntityButterfly.java:161-169, beside its own torch loop); the moth's own preset
 * drives the flight and the torch-seeking retarget below stands (ENT-S-141) — with the iconic 1.7.10 "moths are
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
public class LunaMothFlightGoal extends ButterflyIslandsHuntGoal {
    private static final int TORCH_SCAN_RARE_CHANCE = 10;
    private static final int TORCH_SCAN_MIN_RADIUS = 2;
    private static final int TORCH_SCAN_MAX_RADIUS = 15;
    private static final int TORCH_SCAN_SKIP_AFTER = 6;
    private static final int NO_MATCH = Integer.MAX_VALUE;

    public LunaMothFlightGoal(EntityLunaMoth moth) {
        super(moth, Params.lunaMoth()); // the butterfly hunt goal over the moth's preset (ENT-S-141)
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
