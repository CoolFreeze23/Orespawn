package danger.orespawn.entity;

import java.util.EnumSet;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;

public class MyEntityAIWanderALot extends Goal {
    private static final int WANDER_ROLL_RANGE = 30;
    private static final int VERTICAL_WANDER_RANGE = 7;

    private final PathfinderMob entity;
    private double targetX;
    private double targetY;
    private double targetZ;
    private final double speed;
    private final int xzRange;
    /** Non-zero suppresses starting new wander paths (e.g. while busy with another action). */
    private int wanderSuppressionFlag = 0;

    public MyEntityAIWanderALot(PathfinderMob mob, int xzRange, double speed) {
        this.entity = mob;
        this.xzRange = xzRange;
        this.speed = speed;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    public void setBusy(int suppressionValue) {
        this.wanderSuppressionFlag = suppressionValue;
    }

    @Override
    public boolean canUse() {
        if (this.wanderSuppressionFlag != 0) return false;
        if (this.entity.getRandom().nextInt(WANDER_ROLL_RANGE) != 0) return false;
        if (this.entity instanceof TamableAnimal tamable && tamable.isOrderedToSit()) return false;
        Vec3 wanderTarget = DefaultRandomPos.getPos(this.entity, this.xzRange, VERTICAL_WANDER_RANGE);
        if (wanderTarget == null) return false;
        this.targetX = wanderTarget.x;
        this.targetY = wanderTarget.y;
        this.targetZ = wanderTarget.z;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return !this.entity.getNavigation().isDone();
    }

    @Override
    public void start() {
        this.entity.getNavigation().moveTo(this.targetX, this.targetY, this.targetZ, this.speed);
    }
}
