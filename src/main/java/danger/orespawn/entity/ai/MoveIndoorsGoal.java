package danger.orespawn.entity.ai;

import java.util.EnumSet;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;

/**
 * Closest 1.21.1 behavioral match for the 1.7.10 {@code EntityAIMoveIndoors}
 * used by the companions (orig Boyfriend.java:136 / Girlfriend.java:160,
 * priority 11). The vanilla original pathed villager-like mobs through the
 * nearest village DOOR to an interior point at night or in rain; the
 * village/door framework it depended on was removed in the 1.14 village
 * rework, so no direct equivalent exists. Mapping decision (documented per
 * IMPLEMENTATION_PLAN ground rule 5): reproduce the observable effect — at
 * night or in rain the companion seeks a nearby ROOFED position (a spot it
 * cannot see the sky from) and idles there; by day/clear weather the goal is
 * inactive. Same lowest-priority slot so it never preempts combat, follow,
 * panic, or tempt behavior.
 */
public class MoveIndoorsGoal extends Goal {

    /** How far around the mob we sample for a sheltered spot (matches the orig door-search radius scale). */
    private static final int SHELTER_SEARCH_RADIUS = 12;
    private static final int SAMPLE_ATTEMPTS = 10;

    private final PathfinderMob mob;
    private BlockPos shelterPos;

    public MoveIndoorsGoal(PathfinderMob mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    /** Active only at night or in rain, like the 1.7.10 goal, and only when currently exposed. */
    @Override
    public boolean canUse() {
        if (this.mob.level().isDay() && !this.mob.level().isRaining()) return false;
        if (this.mob.isPassenger()) return false;
        BlockPos feet = this.mob.blockPosition();
        if (!this.mob.level().canSeeSky(feet)) return false; // already indoors
        this.shelterPos = findShelter();
        return this.shelterPos != null;
    }

    @Override
    public void start() {
        if (this.shelterPos != null) {
            this.mob.getNavigation().moveTo(
                    this.shelterPos.getX() + 0.5, this.shelterPos.getY(), this.shelterPos.getZ() + 0.5, 1.0);
        }
    }

    @Override
    public boolean canContinueToUse() {
        return !this.mob.getNavigation().isDone();
    }

    @Override
    public void stop() {
        this.shelterPos = null;
    }

    /** Random-samples nearby standable positions that cannot see the sky. */
    private BlockPos findShelter() {
        BlockPos feet = this.mob.blockPosition();
        for (int i = 0; i < SAMPLE_ATTEMPTS; i++) {
            BlockPos candidate = feet.offset(
                    this.mob.getRandom().nextInt(SHELTER_SEARCH_RADIUS * 2 + 1) - SHELTER_SEARCH_RADIUS,
                    this.mob.getRandom().nextInt(5) - 2,
                    this.mob.getRandom().nextInt(SHELTER_SEARCH_RADIUS * 2 + 1) - SHELTER_SEARCH_RADIUS);
            if (this.mob.level().canSeeSky(candidate)) continue;
            if (!this.mob.level().getBlockState(candidate).isAir()
                    || !this.mob.level().getBlockState(candidate.above()).isAir()) continue;
            if (!this.mob.level().getBlockState(candidate.below()).isSolid()) continue;
            return candidate;
        }
        return null;
    }
}
