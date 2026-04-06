package danger.orespawn.entity;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.Path;

public abstract class MyEntityAITarget extends Goal {
    private static final int MAX_TICKS_WITHOUT_LINE_OF_SIGHT = 60;
    private static final int PATH_RECHECK_DELAY_MIN = 10;
    private static final int PATH_RECHECK_DELAY_SPREAD = 5;
    /** Squared horizontal distance from path end to target (1.5 blocks) considered reachable. */
    private static final double REACHABLE_END_NODE_DIST_SQ = 2.25;
    private static final int REACHABILITY_UNKNOWN = 0;
    private static final int REACHABILITY_YES = 1;
    private static final int REACHABILITY_NO = 2;

    protected Mob taskOwner;
    protected float targetDistance;
    protected boolean shouldCheckSight;
    private final boolean nearbyOnly;
    private int reachabilityState = REACHABILITY_UNKNOWN;
    private int ticksUntilPathRecheck = 0;
    private int ticksSinceLineOfSight = 0;

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
        if (this.taskOwner.distanceToSqr(target) > (double) (this.targetDistance * this.targetDistance)) {
            return false;
        }
        if (this.taskOwner instanceof TamableAnimal tame && tame.isTame()
                && target instanceof TamableAnimal otherTame && otherTame.isTame()) {
            return false;
        }
        if (this.shouldCheckSight) {
            if (this.taskOwner.getSensing().hasLineOfSight(target)) {
                this.ticksSinceLineOfSight = 0;
            } else if (++this.ticksSinceLineOfSight > MAX_TICKS_WITHOUT_LINE_OF_SIGHT) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void start() {
        this.reachabilityState = REACHABILITY_UNKNOWN;
        this.ticksUntilPathRecheck = 0;
        this.ticksSinceLineOfSight = 0;
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
            if (--this.ticksUntilPathRecheck <= 0) {
                this.reachabilityState = REACHABILITY_UNKNOWN;
            }
            if (this.reachabilityState == REACHABILITY_UNKNOWN) {
                this.reachabilityState = canPathfindToTarget(target) ? REACHABILITY_YES : REACHABILITY_NO;
            }
            if (this.reachabilityState == REACHABILITY_NO) return false;
        }
        return true;
    }

    private boolean canPathfindToTarget(LivingEntity target) {
        this.ticksUntilPathRecheck = PATH_RECHECK_DELAY_MIN + this.taskOwner.getRandom().nextInt(PATH_RECHECK_DELAY_SPREAD);
        Path path = this.taskOwner.getNavigation().createPath(target, 0);
        if (path == null) return false;
        var endNode = path.getEndNode();
        if (endNode == null) return false;
        int deltaX = endNode.x - (int) target.getX();
        int deltaZ = endNode.z - (int) target.getZ();
        return (double) (deltaX * deltaX + deltaZ * deltaZ) <= REACHABLE_END_NODE_DIST_SQ;
    }
}
