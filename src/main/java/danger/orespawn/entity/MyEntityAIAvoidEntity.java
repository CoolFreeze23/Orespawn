package danger.orespawn.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;

public class MyEntityAIAvoidEntity extends Goal {
    private static final int PATHFIND_HORIZONTAL_RANGE = 16;
    private static final int PATHFIND_VERTICAL_RANGE = 7;
    private static final double ENTITY_SCAN_VERTICAL_HALF_EXTENT = 3.0;
    /** Squared distance (7 blocks) at which the mob switches to near speed. */
    private static final double NEAR_AVOID_DISTANCE_SQ = 49.0;

    private final PathfinderMob mob;
    private final double farSpeed;
    private final double nearSpeed;
    private Entity entityToAvoid;
    private final float avoidDistance;
    private Path path;
    private final Class<? extends LivingEntity> avoidClass;

    public MyEntityAIAvoidEntity(PathfinderMob mob, Class<? extends LivingEntity> avoidClass,
                                  float distance, double farSpeed, double nearSpeed) {
        this.mob = mob;
        this.avoidClass = avoidClass;
        this.avoidDistance = distance;
        this.farSpeed = farSpeed;
        this.nearSpeed = nearSpeed;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (this.avoidClass == Player.class) {
            if (this.mob instanceof TamableAnimal tame && tame.isTame()) {
                return false;
            }
            this.entityToAvoid = this.mob.level().getNearestPlayer(this.mob, this.avoidDistance);
            if (this.entityToAvoid == null) return false;
        } else {
            List<? extends LivingEntity> nearbyThreats = this.mob.level().getEntitiesOfClass(
                    this.avoidClass,
                    this.mob.getBoundingBox().inflate(
                            this.avoidDistance, ENTITY_SCAN_VERTICAL_HALF_EXTENT, this.avoidDistance));
            if (nearbyThreats.isEmpty()) return false;
            this.entityToAvoid = nearbyThreats.get(0);
        }

        Vec3 fleePos = DefaultRandomPos.getPosAway(this.mob, PATHFIND_HORIZONTAL_RANGE, PATHFIND_VERTICAL_RANGE,
                new Vec3(this.entityToAvoid.getX(), this.entityToAvoid.getY(), this.entityToAvoid.getZ()));
        if (fleePos == null) return false;
        if (this.entityToAvoid.distanceToSqr(fleePos.x, fleePos.y, fleePos.z)
                < this.entityToAvoid.distanceToSqr(this.mob)) {
            return false;
        }
        this.path = this.mob.getNavigation().createPath(fleePos.x, fleePos.y, fleePos.z, 0);
        return this.path != null;
    }

    @Override
    public boolean canContinueToUse() {
        return !this.mob.getNavigation().isDone();
    }

    @Override
    public void start() {
        this.mob.getNavigation().moveTo(this.path, this.farSpeed);
    }

    @Override
    public void stop() {
        this.entityToAvoid = null;
    }

    @Override
    public void tick() {
        if (this.mob.distanceToSqr(this.entityToAvoid) < NEAR_AVOID_DISTANCE_SQ) {
            this.mob.getNavigation().setSpeedModifier(this.nearSpeed);
        } else {
            this.mob.getNavigation().setSpeedModifier(this.farSpeed);
        }
    }
}
