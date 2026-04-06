package danger.orespawn.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;

import java.util.EnumSet;

public class MyEntityAIFollowOwner extends Goal {
    private final TamableAnimal pet;
    private LivingEntity owner;
    private final Level level;
    private final double followSpeed;
    private final PathNavigation navigation;
    private int timeToRecalcPath;
    private final float maxDist;
    private final float minDist;
    private boolean oldWaterCost;

    public MyEntityAIFollowOwner(TamableAnimal pet, double speed, float maxDist, float minDist) {
        this.pet = pet;
        this.level = pet.level();
        this.followSpeed = speed;
        this.navigation = pet.getNavigation();
        this.minDist = minDist;
        this.maxDist = maxDist;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity owner = this.pet.getOwner();
        if (owner == null) return false;
        this.owner = owner;
        if (this.pet.isOrderedToSit()) return false;

        if (this.pet.getY() < 60.0 || !this.pet.level().isDay()) {
            if (this.pet.distanceToSqr(owner) > (double)(this.maxDist / 2.0f * (this.maxDist / 2.0f))) {
                return true;
            }
        }
        return this.pet.distanceToSqr(owner) >= (double)(this.maxDist * this.maxDist);
    }

    @Override
    public boolean canContinueToUse() {
        if (this.pet.isOrderedToSit()) return false;
        if (this.navigation.isDone()) return false;

        if (this.owner != null) {
            if ((int) this.pet.getZ() == (int) this.owner.getZ()
                    && (int) this.pet.getX() == (int) this.owner.getX()
                    && (int) this.pet.getY() < (int) this.owner.getY() + 2
                    && (int) this.pet.getY() > (int) this.owner.getY() - 2) {
                return false;
            }
        }
        return this.pet.distanceToSqr(this.owner) > (double)(this.minDist * this.minDist);
    }

    @Override
    public void start() {
        this.timeToRecalcPath = 0;
        this.oldWaterCost = this.pet.getNavigation().canFloat();
        this.pet.getNavigation().setCanFloat(false);
    }

    @Override
    public void stop() {
        this.owner = null;
        this.navigation.stop();
        this.pet.getNavigation().setCanFloat(this.oldWaterCost);
    }

    @Override
    public void tick() {
        this.pet.getLookControl().setLookAt(this.owner, 10.0f, (float) this.pet.getMaxHeadXRot());
        if (!this.pet.isOrderedToSit() && --this.timeToRecalcPath <= 0) {
            this.timeToRecalcPath = 10;
            if (!this.navigation.moveTo(this.owner, this.followSpeed)
                    && this.pet.distanceToSqr(this.owner) >= 144.0) {
                int x = (int) this.owner.getX() - 2;
                int z = (int) this.owner.getZ() - 2;
                int y = (int) this.owner.getY();
                for (int dx = 0; dx <= 4; ++dx) {
                    for (int dz = 0; dz <= 4; ++dz) {
                        if (dx >= 1 && dz >= 1 && dx <= 3 && dz <= 3) continue;
                        BlockPos pos = new BlockPos(x + dx, y, z + dz);
                        if (!this.level.getBlockState(pos.below()).isSolid()
                                || !this.level.getBlockState(pos).isAir()
                                || !this.level.getBlockState(pos.above()).isAir()) continue;
                        this.pet.moveTo(x + dx + 0.5, y, z + dz + 0.5, this.pet.getYRot(), this.pet.getXRot());
                        this.navigation.stop();
                        return;
                    }
                }
            }
        }
    }
}
