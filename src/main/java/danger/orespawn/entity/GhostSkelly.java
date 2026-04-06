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

public class GhostSkelly extends AmbientCreature {
    private BlockPos currentFlightTarget = null;

    public GhostSkelly(EntityType<? extends GhostSkelly> type, Level level) {
        super(type, level);
        this.xpReward = 10;
        this.noPhysics = true;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 5.0)
                .add(Attributes.MOVEMENT_SPEED, 0.1)
                .add(Attributes.ATTACK_DAMAGE, 0.0);
    }

    @Override
    public void tick() {
        if (this.isPersistenceRequired()) this.noPhysics = false;
        super.tick();
        Vec3 mot = this.getDeltaMovement();
        this.setDeltaMovement(mot.x, mot.y * 0.65, mot.z);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getMsgId().equals("inWall")) return false;
        return super.hurt(source, amount);
    }

    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;
        super.customServerAiStep();

        if (this.currentFlightTarget == null) {
            this.currentFlightTarget = this.blockPosition();
        }

        double distSq = this.currentFlightTarget.distSqr(this.blockPosition());
        if (this.random.nextInt(40) == 1 || distSq < 2.0) {
            Player target = this.level().getNearestPlayer(this, 16.0);
            if (target != null) {
                this.currentFlightTarget = new BlockPos(
                        (int) target.getX() + this.random.nextInt(3) - this.random.nextInt(3),
                        (int) (target.getY() + 1.0),
                        (int) target.getZ() + this.random.nextInt(3) - this.random.nextInt(3));
            } else {
                int i = 0, j = 0;
                for (i = 0; i < 3; i++) {
                    if (this.level().getBlockState(new BlockPos((int) this.getX(), (int) this.getY() + i, (int) this.getZ())).isAir()) break;
                }
                for (j = -1; j >= -3; j--) {
                    if (!this.level().getBlockState(new BlockPos((int) this.getX(), (int) this.getY() + j, (int) this.getZ())).isAir()) break;
                }
                this.currentFlightTarget = new BlockPos(
                        (int) this.getX() + this.random.nextInt(10) - this.random.nextInt(10),
                        (int) this.getY() + i + j + this.random.nextInt(4) + 1,
                        (int) this.getZ() + this.random.nextInt(10) - this.random.nextInt(10));
            }
        }

        double dx = this.currentFlightTarget.getX() + 0.5 - this.getX();
        double dy = this.currentFlightTarget.getY() + 0.1 - this.getY();
        double dz = this.currentFlightTarget.getZ() + 0.5 - this.getZ();
        Vec3 mot = this.getDeltaMovement();
        double mx = mot.x + (Math.signum(dx) * 0.1 - mot.x) * 0.05;
        double my = mot.y + (Math.signum(dy) * 0.7 - mot.y) * 0.1;
        double mz = mot.z + (Math.signum(dz) * 0.1 - mot.z) * 0.05;
        this.setDeltaMovement(mx, my, mz);

        float targetYaw = (float) (Math.atan2(mz, mx) * 180.0 / Math.PI) - 90.0f;
        float yawDiff = Mth.wrapDegrees(targetYaw - this.getYRot());
        this.zza = 0.05f;
        this.setYRot(this.getYRot() + yawDiff / 6.0f);
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        if (this.random.nextInt(2) == 0) {
            return SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "chain_rattles"));
        }
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) { return null; }
    @Override
    protected SoundEvent getDeathSound() { return null; }
    @Override
    protected float getSoundVolume() { return 0.5f; }
    @Override
    public boolean isPushable() { return false; }
    @Override
    public boolean causeFallDamage(float d, float m, DamageSource s) { return false; }
    @Override
    protected void checkFallDamage(double y, boolean g, BlockState st, BlockPos p) { }

    @Override
    public boolean removeWhenFarAway(double dist) {
        return !this.isPersistenceRequired();
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnType) {
        return !this.level().canSeeSky(this.blockPosition());
    }
}
