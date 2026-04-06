package danger.orespawn.entity;

import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;

public class MyEntityAIWander extends Goal {
    private final PathfinderMob entity;
    private double xPosition;
    private double yPosition;
    private double zPosition;
    private final float speed;

    public MyEntityAIWander(PathfinderMob mob, float speed) {
        this.entity = mob;
        this.speed = speed;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (this.entity.getRandom().nextInt(90) != 0) {
            return false;
        }
        if (this.entity instanceof TamableAnimal tamable && tamable.isOrderedToSit()) {
            return false;
        }
        Vec3 target = DefaultRandomPos.getPos(this.entity, 10, 7);
        if (target == null) {
            return false;
        }
        this.xPosition = target.x;
        this.yPosition = target.y;
        this.zPosition = target.z;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.entity instanceof TamableAnimal tamable) {
            LivingEntity owner = tamable.getOwner();
            if (owner != null
                    && (int) tamable.getZ() == (int) owner.getZ()
                    && (int) tamable.getX() == (int) owner.getX()
                    && (int) tamable.getY() < (int) owner.getY() + 2
                    && (int) tamable.getY() > (int) owner.getY() - 2) {
                return false;
            }
        }
        return !this.entity.getNavigation().isDone();
    }

    @Override
    public void start() {
        this.entity.getNavigation().moveTo(this.xPosition, this.yPosition, this.zPosition, this.speed);
    }
}
