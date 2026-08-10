package danger.orespawn.entity.ai;

import java.util.EnumSet;
import javax.annotation.Nullable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.PathType;

/**
 * TF-001 / TEST-005 — a line-for-line copy of vanilla 1.21.1
 * {@link net.minecraft.world.entity.ai.goal.FollowOwnerGoal} with its
 * constructor navigation-type gate removed. Vanilla 1.21 added a ctor check
 * that throws {@code IllegalArgumentException "Unsupported mob type for
 * FollowOwnerGoal"} for any navigation that is not Ground/FlyingPathNavigation,
 * which made every water-bound follower unspawnable at construction (the
 * WaterDragon ctor died before the entity ever entered the world). The
 * 1.7.10 follow-owner AI this ports (orig
 * reference_1_7_10_source/sources/danger/orespawn/MyEntityAIFollowOwner.java:29-37,
 * registered at WaterDragon.java:71) had no such restriction — any
 * PathNavigation could follow its owner. Everything below the ctor is
 * unmodified vanilla behaviour.
 */
public class OwnerFollowAnyNavGoal extends Goal {
    private final TamableAnimal tamable;
    @Nullable
    private LivingEntity owner;
    private final double speedModifier;
    private final PathNavigation navigation;
    private int timeToRecalcPath;
    private final float stopDistance;
    private final float startDistance;
    private float oldWaterCost;

    public OwnerFollowAnyNavGoal(TamableAnimal tamable, double speedModifier, float startDistance, float stopDistance) {
        this.tamable = tamable;
        this.speedModifier = speedModifier;
        this.navigation = tamable.getNavigation();
        this.startDistance = startDistance;
        this.stopDistance = stopDistance;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        // Vanilla FollowOwnerGoal throws here unless the navigation is
        // GroundPathNavigation or FlyingPathNavigation — deliberately omitted
        // (the whole point of this class; see the class Javadoc).
    }

    @Override
    public boolean canUse() {
        LivingEntity livingentity = this.tamable.getOwner();
        if (livingentity == null) {
            return false;
        } else if (this.tamable.unableToMoveToOwner()) {
            return false;
        } else if (this.tamable.distanceToSqr(livingentity) < (double) (this.startDistance * this.startDistance)) {
            return false;
        } else {
            this.owner = livingentity;
            return true;
        }
    }

    @Override
    public boolean canContinueToUse() {
        if (this.navigation.isDone()) {
            return false;
        } else {
            return this.tamable.unableToMoveToOwner() ? false : !(this.tamable.distanceToSqr(this.owner) <= (double) (this.stopDistance * this.stopDistance));
        }
    }

    @Override
    public void start() {
        this.timeToRecalcPath = 0;
        this.oldWaterCost = this.tamable.getPathfindingMalus(PathType.WATER);
        this.tamable.setPathfindingMalus(PathType.WATER, 0.0F);
    }

    @Override
    public void stop() {
        this.owner = null;
        this.navigation.stop();
        this.tamable.setPathfindingMalus(PathType.WATER, this.oldWaterCost);
    }

    @Override
    public void tick() {
        boolean flag = this.tamable.shouldTryTeleportToOwner();
        if (!flag) {
            this.tamable.getLookControl().setLookAt(this.owner, 10.0F, (float) this.tamable.getMaxHeadXRot());
        }

        if (--this.timeToRecalcPath <= 0) {
            this.timeToRecalcPath = this.adjustedTickDelay(10);
            if (flag) {
                this.tamable.tryToTeleportToOwner();
            } else {
                this.navigation.moveTo(this.owner, this.speedModifier);
            }
        }
    }
}
