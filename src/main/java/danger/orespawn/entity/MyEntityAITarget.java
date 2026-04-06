package danger.orespawn.entity;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.player.Player;

public abstract class MyEntityAITarget extends Goal {
    protected Mob taskOwner;
    protected float targetDistance;
    protected boolean shouldCheckSight;
    private final boolean nearbyOnly;
    private int targetSearchStatus = 0;
    private int targetSearchDelay = 0;
    private int unseenTicks = 0;

    public MyEntityAITarget(Mob owner, float distance, boolean checkSight) {
        this(owner, distance, checkSight, false);
    }

    public MyEntityAITarget(Mob owner, float distance, boolean checkSight, boolean nearbyOnly) {
        this.taskOwner = owner;
        this.targetDistance = distance;
        this.shouldCheckSight = checkSight;
        this.nearbyOnly = nearbyOnly;
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = this.taskOwner.getTarget();
        if (target == null) return false;
        if (!target.isAlive()) {
            this.taskOwner.setTarget(null);
            return false;
        }
        if (this.taskOwner.distanceToSqr(target) > (double)(this.targetDistance * this.targetDistance)) {
            return false;
        }
        if (this.taskOwner instanceof TamableAnimal tame && tame.isTame()
                && target instanceof TamableAnimal otherTame && otherTame.isTame()) {
            return false;
        }
        if (this.shouldCheckSight) {
            if (this.taskOwner.getSensing().hasLineOfSight(target)) {
                this.unseenTicks = 0;
            } else if (++this.unseenTicks > 60) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void start() {
        this.targetSearchStatus = 0;
        this.targetSearchDelay = 0;
        this.unseenTicks = 0;
    }

    @Override
    public void stop() {
        this.taskOwner.setTarget(null);
    }

    protected boolean isSuitableTarget(LivingEntity target, boolean checkDifficulty) {
        if (target == null || target == this.taskOwner || !target.isAlive()) return false;

        if (this.taskOwner instanceof TamableAnimal tame && tame.isTame()) {
            if (target instanceof TamableAnimal otherTame && otherTame.isTame()) return false;
            if (target == tame.getOwner()) return false;
        }

        if (target instanceof Player) return false;
        if (target instanceof ZombifiedPiglin) return false;
        if (target instanceof Creeper) return true;
        if (target instanceof Ghast) return true;

        if (this.shouldCheckSight && !this.taskOwner.getSensing().hasLineOfSight(target)) {
            return false;
        }

        if (this.nearbyOnly) {
            if (--this.targetSearchDelay <= 0) {
                this.targetSearchStatus = 0;
            }
            if (this.targetSearchStatus == 0) {
                this.targetSearchStatus = canReach(target) ? 1 : 2;
            }
            if (this.targetSearchStatus == 2) return false;
        }
        return true;
    }

    private boolean canReach(LivingEntity target) {
        this.targetSearchDelay = 10 + this.taskOwner.getRandom().nextInt(5);
        var path = this.taskOwner.getNavigation().createPath(target, 0);
        if (path == null) return false;
        var endNode = path.getEndNode();
        if (endNode == null) return false;
        int dx = endNode.x - (int) target.getX();
        int dz = endNode.z - (int) target.getZ();
        return (double)(dx * dx + dz * dz) <= 2.25;
    }
}
