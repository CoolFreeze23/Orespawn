package danger.orespawn.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;

import java.util.EnumSet;

public class MyEntityAIFollowOwner extends Goal {
    private static final double LOW_ALTITUDE_FOLLOW_THRESHOLD_Y = 60.0;
    private static final int PATH_RECALC_INTERVAL_TICKS = 10;
    private static final float LOOK_AT_RANGE_DEGREES = 10.0f;
    /** Squared horizontal distance (12 blocks) beyond which teleport search runs. */
    private static final double TELEPORT_ATTEMPT_DISTANCE_SQ = 144.0;
    private static final int TELEPORT_SEARCH_OFFSET_BASE = -2;
    private static final int TELEPORT_SEARCH_SIZE = 4;
    private static final int TELEPORT_INNER_MIN = 1;
    private static final int TELEPORT_INNER_MAX = 3;
    private static final int OWNER_VERTICAL_MATCH_BLOCKS = 2;

    private final TamableAnimal pet;
    private LivingEntity owner;
    private final Level level;
    private final double followSpeed;
    private final PathNavigation navigation;
    private int ticksUntilPathRecalc;
    private final float maxDist;
    private final float minDist;
    private boolean previousCanFloat;

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

        float halfMaxDist = this.maxDist / 2.0f;
        double relaxedFollowDistSq = (double) (halfMaxDist * halfMaxDist);

        if (this.pet.getY() < LOW_ALTITUDE_FOLLOW_THRESHOLD_Y || !this.pet.level().isDay()) {
            if (this.pet.distanceToSqr(owner) > relaxedFollowDistSq) {
                return true;
            }
        }
        return this.pet.distanceToSqr(owner) >= (double) (this.maxDist * this.maxDist);
    }

    @Override
    public boolean canContinueToUse() {
        if (this.pet.isOrderedToSit()) return false;
        if (this.navigation.isDone()) return false;

        if (this.owner != null) {
            if ((int) this.pet.getZ() == (int) this.owner.getZ()
                    && (int) this.pet.getX() == (int) this.owner.getX()
                    && (int) this.pet.getY() < (int) this.owner.getY() + OWNER_VERTICAL_MATCH_BLOCKS
                    && (int) this.pet.getY() > (int) this.owner.getY() - OWNER_VERTICAL_MATCH_BLOCKS) {
                return false;
            }
        }
        return this.pet.distanceToSqr(this.owner) > (double) (this.minDist * this.minDist);
    }

    @Override
    public void start() {
        this.ticksUntilPathRecalc = 0;
        this.previousCanFloat = this.pet.getNavigation().canFloat();
        this.pet.getNavigation().setCanFloat(false);
    }

    @Override
    public void stop() {
        this.owner = null;
        this.navigation.stop();
        this.pet.getNavigation().setCanFloat(this.previousCanFloat);
    }

    @Override
    public void tick() {
        this.pet.getLookControl().setLookAt(this.owner, LOOK_AT_RANGE_DEGREES, (float) this.pet.getMaxHeadXRot());
        if (!this.pet.isOrderedToSit() && --this.ticksUntilPathRecalc <= 0) {
            this.ticksUntilPathRecalc = PATH_RECALC_INTERVAL_TICKS;
            if (!this.navigation.moveTo(this.owner, this.followSpeed)
                    && this.pet.distanceToSqr(this.owner) >= TELEPORT_ATTEMPT_DISTANCE_SQ) {
                int baseX = (int) this.owner.getX() + TELEPORT_SEARCH_OFFSET_BASE;
                int baseZ = (int) this.owner.getZ() + TELEPORT_SEARCH_OFFSET_BASE;
                int baseY = (int) this.owner.getY();
                for (int dx = 0; dx <= TELEPORT_SEARCH_SIZE; ++dx) {
                    for (int dz = 0; dz <= TELEPORT_SEARCH_SIZE; ++dz) {
                        if (dx >= TELEPORT_INNER_MIN && dz >= TELEPORT_INNER_MIN
                                && dx <= TELEPORT_INNER_MAX && dz <= TELEPORT_INNER_MAX) {
                            continue;
                        }
                        BlockPos pos = new BlockPos(baseX + dx, baseY, baseZ + dz);
                        if (!this.level.getBlockState(pos.below()).isSolid()
                                || !this.level.getBlockState(pos).isAir()
                                || !this.level.getBlockState(pos.above()).isAir()) {
                            continue;
                        }
                        this.pet.moveTo(
                                baseX + dx + 0.5,
                                baseY,
                                baseZ + dz + 0.5,
                                this.pet.getYRot(),
                                this.pet.getXRot());
                        this.navigation.stop();
                        return;
                    }
                }
            }
        }
    }
}
