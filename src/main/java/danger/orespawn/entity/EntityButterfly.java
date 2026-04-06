package danger.orespawn.entity;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class EntityButterfly extends AmbientCreature {
    private static final EntityDataAccessor<Integer> BUTTERFLY_TYPE =
            SynchedEntityData.defineId(EntityButterfly.class, EntityDataSerializers.INT);
    private BlockPos currentFlightTarget = null;

    public EntityButterfly(EntityType<? extends EntityButterfly> type, Level level) {
        super(type, level);
        this.setRandomButterflyType();
    }

    private void setRandomButterflyType() {
        this.entityData.set(BUTTERFLY_TYPE, this.random.nextInt(4));
    }

    public int getButterflyType() {
        return this.entityData.get(BUTTERFLY_TYPE);
    }

    public void setButterflyType(int type) {
        this.entityData.set(BUTTERFLY_TYPE, type);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(BUTTERFLY_TYPE, 0);
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
        Vec3 mot = this.getDeltaMovement();
        this.setDeltaMovement(mot.x, mot.y * 0.6, mot.z);
    }

    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;
        super.customServerAiStep();

        if (this.currentFlightTarget == null) {
            this.currentFlightTarget = this.blockPosition();
        }

        double distSq = this.currentFlightTarget.distSqr(this.blockPosition());
        if (this.random.nextInt(100) == 0 || distSq < 4.0) {
            for (int tries = 25; tries > 0; tries--) {
                BlockPos newTarget = new BlockPos(
                        (int) this.getX() + this.random.nextInt(7) - this.random.nextInt(7),
                        (int) this.getY() + this.random.nextInt(6) - 2,
                        (int) this.getZ() + this.random.nextInt(7) - this.random.nextInt(7)
                );
                if (this.level().getBlockState(newTarget).isAir()) {
                    this.currentFlightTarget = newTarget;
                    break;
                }
            }
        }

        double dx = this.currentFlightTarget.getX() + 0.5 - this.getX();
        double dy = this.currentFlightTarget.getY() + 0.1 - this.getY();
        double dz = this.currentFlightTarget.getZ() + 0.5 - this.getZ();

        Vec3 mot = this.getDeltaMovement();
        double mx = mot.x + (Math.signum(dx) * 0.5 - mot.x) * 0.1;
        double my = mot.y + (Math.signum(dy) * 0.7 - mot.y) * 0.1;
        double mz = mot.z + (Math.signum(dz) * 0.5 - mot.z) * 0.1;
        this.setDeltaMovement(mx, my, mz);

        float targetYaw = (float) (Math.atan2(mz, mx) * 180.0 / Math.PI) - 90.0f;
        float yawDiff = Mth.wrapDegrees(targetYaw - this.getYRot());
        this.zza = 0.5f;
        this.setYRot(this.getYRot() + yawDiff);
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return null;
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
        return 0.0f;
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
        BlockState state = level.getBlockState(this.blockPosition());
        if (!state.isAir()) return false;
        if (!this.level().canSeeSky(this.blockPosition())) return false;
        return this.getY() >= 50.0;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("ButterflyType", this.getButterflyType());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setButterflyType(tag.getInt("ButterflyType"));
    }
}
