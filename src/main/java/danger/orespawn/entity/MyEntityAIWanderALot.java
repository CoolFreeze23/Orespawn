package danger.orespawn.entity;

import java.util.EnumSet;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;

public class MyEntityAIWanderALot extends Goal {
    private final PathfinderMob entity;
    private double xPosition;
    private double yPosition;
    private double zPosition;
    private final double speed;
    private final int xzRange;
    private int busy = 0;

    public MyEntityAIWanderALot(PathfinderMob mob, int xzRange, double speed) {
        this.entity = mob;
        this.xzRange = xzRange;
        this.speed = speed;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    public void setBusy(int i) {
        this.busy = i;
    }

    @Override
    public boolean canUse() {
        if (this.busy != 0) return false;
        if (this.entity.getRandom().nextInt(30) != 0) return false;
        if (this.entity instanceof TamableAnimal tamable && tamable.isOrderedToSit()) return false;
        Vec3 target = DefaultRandomPos.getPos(this.entity, this.xzRange, 7);
        if (target == null) return false;
        this.xPosition = target.x;
        this.yPosition = target.y;
        this.zPosition = target.z;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return !this.entity.getNavigation().isDone();
    }

    @Override
    public void start() {
        this.entity.getNavigation().moveTo(this.xPosition, this.yPosition, this.zPosition, this.speed);
    }
}
