package danger.orespawn.entity;

import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;

public class MyEntityAIWander extends Goal {
    private static final int WANDER_ROLL_RANGE = 90;
    private static final int HORIZONTAL_WANDER_RANGE = 10;
    private static final int VERTICAL_WANDER_RANGE = 7;
    private static final int OWNER_VERTICAL_MATCH_BLOCKS = 2;

    private final PathfinderMob entity;
    private double targetX;
    private double targetY;
    private double targetZ;
    private final float speed;

    public MyEntityAIWander(PathfinderMob mob, float speed) {
        this.entity = mob;
        this.speed = speed;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (this.entity.getRandom().nextInt(WANDER_ROLL_RANGE) != 0) {
            return false;
        }
        if (this.entity instanceof TamableAnimal tamable && tamable.isOrderedToSit()) {
            return false;
        }
        Vec3 wanderTarget = DefaultRandomPos.getPos(this.entity, HORIZONTAL_WANDER_RANGE, VERTICAL_WANDER_RANGE);
        if (wanderTarget == null) {
            return false;
        }
        this.targetX = wanderTarget.x;
        this.targetY = wanderTarget.y;
        this.targetZ = wanderTarget.z;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.entity instanceof TamableAnimal tamable) {
            LivingEntity owner = tamable.getOwner();
            if (owner != null
                    && (int) tamable.getZ() == (int) owner.getZ()
                    && (int) tamable.getX() == (int) owner.getX()
                    && (int) tamable.getY() < (int) owner.getY() + OWNER_VERTICAL_MATCH_BLOCKS
                    && (int) tamable.getY() > (int) owner.getY() - OWNER_VERTICAL_MATCH_BLOCKS) {
                return false;
            }
        }
        return !this.entity.getNavigation().isDone();
    }

    @Override
    public void start() {
        this.entity.getNavigation().moveTo(this.targetX, this.targetY, this.targetZ, this.speed);
    }
}
