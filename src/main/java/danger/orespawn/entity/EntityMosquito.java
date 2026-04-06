package danger.orespawn.entity;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import danger.orespawn.OreSpawnMod;

public class EntityMosquito extends AmbientCreature {
    private static final double VERTICAL_DRAG = 0.6;
    private static final double NEAR_TARGET_DIST_SQ = 3.0;
    private static final int RETARGET_RARE_CHANCE = 20;
    private static final int SEEK_PLAYER_RARE_CHANCE = 4;
    private static final double PLAYER_SEEK_RANGE = 10.0;
    private static final int WANDER_ATTEMPTS = 50;
    private static final int WANDER_RANGE = 6;
    private static final int WANDER_Y_BIAS = 2;
    private static final double STEER_XY = 0.5;
    private static final double STEER_Y = 0.7;
    private static final double STEER_BLEND = 0.1;
    private static final float FORWARD_SPEED = 0.3f;

    private BlockPos currentFlightTarget = null;

    public EntityMosquito(EntityType<? extends EntityMosquito> type, Level level) {
        super(type, level);
        this.xpReward = 5;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 2.0)
                .add(Attributes.MOVEMENT_SPEED, 0.1)
                .add(Attributes.ATTACK_DAMAGE, 0.0);
    }

    @Override
    public void tick() {
        super.tick();
        Vec3 motion = this.getDeltaMovement();
        this.setDeltaMovement(motion.x, motion.y * VERTICAL_DRAG, motion.z);
    }

    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;
        super.customServerAiStep();

        if (this.currentFlightTarget == null) {
            this.currentFlightTarget = this.blockPosition();
        }

        double distSq = this.currentFlightTarget.distSqr(this.blockPosition());
        if (this.random.nextInt(RETARGET_RARE_CHANCE) == 0 || distSq < NEAR_TARGET_DIST_SQ) {
            Player target = null;
            if (this.random.nextInt(SEEK_PLAYER_RARE_CHANCE) == 0) {
                target = this.level().getNearestPlayer(this, PLAYER_SEEK_RANGE);
                if (target != null) {
                    this.currentFlightTarget = new BlockPos(
                            (int) target.getX(), (int) target.getY() + 2, (int) target.getZ());
                }
            }
            if (target == null) {
                for (int tries = WANDER_ATTEMPTS; tries > 0; tries--) {
                    BlockPos newTarget = new BlockPos(
                            (int) this.getX() + this.random.nextInt(WANDER_RANGE) - this.random.nextInt(WANDER_RANGE),
                            (int) this.getY() + this.random.nextInt(WANDER_RANGE) - WANDER_Y_BIAS,
                            (int) this.getZ() + this.random.nextInt(WANDER_RANGE) - this.random.nextInt(WANDER_RANGE));
                    if (this.level().getBlockState(newTarget).isAir()) {
                        this.currentFlightTarget = newTarget;
                        break;
                    }
                }
            }
        }

        double dx = this.currentFlightTarget.getX() + 0.5 - this.getX();
        double dy = this.currentFlightTarget.getY() + 0.1 - this.getY();
        double dz = this.currentFlightTarget.getZ() + 0.5 - this.getZ();
        Vec3 motion = this.getDeltaMovement();
        double mx = motion.x + (Math.signum(dx) * STEER_XY - motion.x) * STEER_BLEND;
        double my = motion.y + (Math.signum(dy) * STEER_Y - motion.y) * STEER_BLEND;
        double mz = motion.z + (Math.signum(dz) * STEER_XY - motion.z) * STEER_BLEND;
        this.setDeltaMovement(mx, my, mz);

        float targetYaw = (float) (Math.atan2(mz, mx) * 180.0 / Math.PI) - 90.0f;
        float yawDiff = Mth.wrapDegrees(targetYaw - this.getYRot());
        this.zza = FORWARD_SPEED;
        this.setYRot(this.getYRot() + yawDiff);
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "mosquito"));
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return null;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return null;
    }

    @Override
    protected float getSoundVolume() {
        return 0.4f;
    }

    @Override
    public boolean causeFallDamage(float dist, float mult, DamageSource source) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
    }

    @Override
    public boolean removeWhenFarAway(double dist) {
        return !this.isPersistenceRequired();
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnType) {
        return true;
    }
}
