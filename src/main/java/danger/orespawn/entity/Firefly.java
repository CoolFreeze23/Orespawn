package danger.orespawn.entity;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
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

public class Firefly extends AmbientCreature {
    int myBlink = 20 + this.random.nextInt(20);
    int blinker = 0;
    private BlockPos currentFlightTarget = null;

    public Firefly(EntityType<? extends Firefly> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 1.0)
                .add(Attributes.MOVEMENT_SPEED, 0.1)
                .add(Attributes.ATTACK_DAMAGE, 0.0);
    }

    public float getBlink() {
        return this.blinker < this.myBlink / 2 ? 240.0f : 0.0f;
    }

    @Override
    public void tick() {
        super.tick();
        Vec3 mot = this.getDeltaMovement();
        this.setDeltaMovement(mot.x, mot.y * 0.6, mot.z);
        this.blinker++;
        if (this.blinker > this.myBlink) this.blinker = 0;
        if (this.isPersistenceRequired()) return;
        long t = this.level().getDayTime() % 24000L;
        if (t > 11000L) return;
        if (this.random.nextInt(500) == 1) this.discard();
    }

    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;
        super.customServerAiStep();

        if (this.currentFlightTarget == null) {
            this.currentFlightTarget = this.blockPosition();
        }

        double distSq = this.currentFlightTarget.distSqr(this.blockPosition());
        if (this.random.nextInt(40) == 0 || distSq < 2.0) {
            for (int tries = 25; tries > 0; tries--) {
                BlockPos newTarget = new BlockPos(
                        (int) this.getX() + this.random.nextInt(4) - this.random.nextInt(4),
                        (int) this.getY() + this.random.nextInt(4) - 2,
                        (int) this.getZ() + this.random.nextInt(4) - this.random.nextInt(4));
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
        double mx = mot.x + (Math.signum(dx) * 0.2 - mot.x) * 0.1;
        double my = mot.y + (Math.signum(dy) * 0.7 - mot.y) * 0.1;
        double mz = mot.z + (Math.signum(dz) * 0.2 - mot.z) * 0.1;
        this.setDeltaMovement(mx, my, mz);

        float targetYaw = (float) (Math.atan2(mz, mx) * 180.0 / Math.PI) - 90.0f;
        float yawDiff = Mth.wrapDegrees(targetYaw - this.getYRot());
        this.zza = 0.2f;
        this.setYRot(this.getYRot() + yawDiff / 4.0f);
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() { return null; }
    @Override
    protected SoundEvent getHurtSound(DamageSource source) { return null; }
    @Override
    protected SoundEvent getDeathSound() { return null; }
    @Override
    protected float getSoundVolume() { return 0.0f; }

    @Override
    public boolean isPushable() { return true; }

    @Override
    public boolean causeFallDamage(float d, float m, DamageSource s) { return false; }

    @Override
    protected void checkFallDamage(double y, boolean g, BlockState st, BlockPos p) { }

    @Override
    public boolean removeWhenFarAway(double dist) {
        if (this.level().canSeeSky(this.blockPosition())) return false;
        return !this.isPersistenceRequired();
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnType) {
        if (!level.getBlockState(this.blockPosition()).isAir()) return false;
        if (this.level().canSeeSky(this.blockPosition())) return false;
        int buddies = this.level().getEntitiesOfClass(Firefly.class,
                this.getBoundingBox().inflate(20.0, 8.0, 20.0)).size();
        if (buddies > 10) return false;
        return this.getY() >= 50.0;
    }
}
