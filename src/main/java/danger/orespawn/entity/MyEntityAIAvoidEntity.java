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
    private final PathfinderMob mob;
    private final double farSpeed;
    private final double nearSpeed;
    private Entity closestEntity;
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
            this.closestEntity = this.mob.level().getNearestPlayer(this.mob, this.avoidDistance);
            if (this.closestEntity == null) return false;
        } else {
            List<? extends LivingEntity> list = this.mob.level().getEntitiesOfClass(
                    this.avoidClass,
                    this.mob.getBoundingBox().inflate(this.avoidDistance, 3.0, this.avoidDistance));
            if (list.isEmpty()) return false;
            this.closestEntity = list.get(0);
        }

        Vec3 away = DefaultRandomPos.getPosAway(this.mob, 16, 7,
                new Vec3(this.closestEntity.getX(), this.closestEntity.getY(), this.closestEntity.getZ()));
        if (away == null) return false;
        if (this.closestEntity.distanceToSqr(away.x, away.y, away.z)
                < this.closestEntity.distanceToSqr(this.mob)) {
            return false;
        }
        this.path = this.mob.getNavigation().createPath(away.x, away.y, away.z, 0);
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
        this.closestEntity = null;
    }

    @Override
    public void tick() {
        if (this.mob.distanceToSqr(this.closestEntity) < 49.0) {
            this.mob.getNavigation().setSpeedModifier(this.nearSpeed);
        } else {
            this.mob.getNavigation().setSpeedModifier(this.farSpeed);
        }
    }
}
