package danger.orespawn.entity.ai;

import java.util.Comparator;
import java.util.List;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.entity.EntityDragonfly;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;

/**
 * Dragonfly flight + hunt behaviour.
 *
 * <p>Extends {@link AmbientFlightGoal} with the 1.7.10 prey-hunting branch
 * from {@code Dragonfly.customServerAiStep()}: ~1 in 12 retarget rolls
 * the dragonfly scans its 10x6x10 AABB for the closest small living
 * entity (bbWidth < 0.6, not a player, not another dragonfly, with line
 * of sight), paths to it, and calls {@code doHurtTarget} whenever it
 * comes within 6 blocks. Players are explicitly excluded from the prey
 * list — dragonflies are a biome-flavour mob, not an aggro source — and
 * horses are excluded when the config toggle
 * {@link OreSpawnConfig#DRAGONFLY_HORSE_FRIENDLY} is on.
 *
 * <p>Main-thread budget: the {@code getEntitiesOfClass} scan is gated
 * behind the AmbientFlightGoal retarget cadence ({@link Params#dragonfly})
 * AND a 1-in-12 roll on top, so on average a dragonfly does one
 * neighbour scan roughly every 3600 ticks (~3 minutes). Well within
 * the per-tick budget even with hundreds of flyers.
 */
public class DragonflyHuntGoal extends AmbientFlightGoal {
    private static final double HUNT_HURT_DIST_SQ = 6.0;
    private static final int HUNT_ROLL_CHANCE = 12;
    private static final double SCAN_X = 10.0, SCAN_Y = 6.0, SCAN_Z = 10.0;
    private static final float MAX_PREY_WIDTH = 0.6f;

    private final EntityDragonfly dragonfly;

    public DragonflyHuntGoal(EntityDragonfly dragonfly) {
        super(dragonfly, Params.dragonfly());
        this.dragonfly = dragonfly;
    }

    @Override
    public void tick() {
        super.tick();
        // Melee when in bite range of the current target, matching 1.7.10.
        // We only hurt a genuine LivingEntity target, not the synthetic
        // wander BlockPos targets from the base class.
        LivingEntity target = (this.dragonfly.getTarget() != null)
                ? this.dragonfly.getTarget() : null;
        if (target != null && this.dragonfly.distanceToSqr(target) < HUNT_HURT_DIST_SQ) {
            this.dragonfly.doHurtTarget(target);
        }
    }

    @Override
    protected BlockPos pickRetarget() {
        if (this.mob.getRandom().nextInt(HUNT_ROLL_CHANCE) == 0) {
            LivingEntity prey = findPrey();
            if (prey != null) {
                this.dragonfly.setTarget(prey);
                return prey.blockPosition().above();
            }
        }
        return super.pickRetarget();
    }

    private LivingEntity findPrey() {
        List<LivingEntity> candidates = this.mob.level().getEntitiesOfClass(LivingEntity.class,
                this.mob.getBoundingBox().inflate(SCAN_X, SCAN_Y, SCAN_Z));
        candidates.sort(Comparator.comparingDouble(this.mob::distanceToSqr));
        for (LivingEntity candidate : candidates) {
            if (candidate == this.mob || !candidate.isAlive()) continue;
            if (candidate instanceof EntityDragonfly) continue;
            if (candidate instanceof Player) continue;
            if (OreSpawnConfig.DRAGONFLY_HORSE_FRIENDLY.get()
                    && candidate instanceof AbstractHorse) continue;
            if (candidate.getBbWidth() > MAX_PREY_WIDTH) continue;
            if (!this.dragonfly.getSensing().hasLineOfSight(candidate)) continue;
            return candidate;
        }
        return null;
    }
}
