package danger.orespawn.entity;

import danger.orespawn.OreSpawnMod;
import java.util.Comparator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.Mth;

public class EntityVortex extends Monster {
    private BlockPos currentFlightTarget = null;
    private int lastX = 0;
    private int lastY = 0;
    private int lastZ = 0;
    private int stuckCount = 0;
    private int winded = 0;

    public EntityVortex(EntityType<? extends EntityVortex> type, Level level) {
        super(type, level);
        this.xpReward = 200;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 200.0)
                .add(Attributes.MOVEMENT_SPEED, 0.35)
                .add(Attributes.ATTACK_DAMAGE, 20.0);
    }

    @Override
    protected float getSoundVolume() {
        return 0.75f;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "vortexlive"));
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return null;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "vortexlive"));
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        Vec3 motion = this.getDeltaMovement();
        this.setDeltaMovement(motion.x, motion.y * 0.6, motion.z);

        LivingEntity e = findSomethingToAttack();
        if (e != null && this.level().isClientSide) {
            for (int i = 0; i < 20; ++i) {
                double d = this.random.nextDouble() * 3.5;
                d *= d;
                double dir = this.random.nextDouble() * 2.0 * Math.PI;
                double dx = Math.cos(dir - Math.PI) * d / 2.0;
                double dz = Math.sin(dir - Math.PI) * d / 2.0;
                this.level().addParticle(ParticleTypes.SMOKE,
                        this.getX() + dx, this.getY() + 0.75 + d, this.getZ() + dz,
                        Math.cos(dir + Math.PI / 2.0) * this.random.nextFloat() / 4.0,
                        this.random.nextFloat() / 2.0,
                        Math.sin(dir + Math.PI / 2.0) * this.random.nextFloat() / 4.0);
            }
        }

        if (this.random.nextInt(200) == 1) {
            this.heal(1.0f);
        }

        if (!this.level().isClientSide) {
            long t = this.level().getDayTime() % 24000L;
            if (t < 12000L && this.random.nextInt(500) == 1) {
                this.discard();
            }
        }
    }

    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;
        super.customServerAiStep();

        if (this.lastX == (int) this.getX() && this.lastY == (int) this.getY() && this.lastZ == (int) this.getZ()) {
            ++this.stuckCount;
        } else {
            this.stuckCount = 0;
            this.lastX = (int) this.getX();
            this.lastY = (int) this.getY();
            this.lastZ = (int) this.getZ();
        }

        if (this.currentFlightTarget == null) {
            this.currentFlightTarget = this.blockPosition();
        }

        if (this.winded > 0) {
            --this.winded;
        }

        double distSq = this.currentFlightTarget.distSqr(this.blockPosition());

        if (this.stuckCount > 30 || this.random.nextInt(300) == 0 || distSq < 4.0) {
            this.stuckCount = 0;
            int keepTrying = 50;
            while (keepTrying > 0) {
                int xdir = (this.random.nextInt(14) + 10) * (this.random.nextInt(2) == 0 ? -1 : 1);
                int zdir = (this.random.nextInt(14) + 10) * (this.random.nextInt(2) == 0 ? -1 : 1);

                BlockPos newTarget = new BlockPos(
                        (int) this.getX() + xdir,
                        (int) this.getY() + this.random.nextInt(6) - 3,
                        (int) this.getZ() + zdir);

                if (this.level().getBlockState(newTarget).isAir()) {
                    this.currentFlightTarget = newTarget;
                    break;
                }
                --keepTrying;
            }
        }

        LivingEntity e = findSomethingToAttack();
        if (e != null) {
            this.currentFlightTarget = e.blockPosition();
            double d = this.distanceToSqr(e);
            if (d < 81.0 && this.winded == 0) {
                double a = Math.atan2(this.getZ() - e.getZ(), this.getX() - e.getX());
                double pm = (e instanceof Player) ? 2.0 : 1.0;
                double pullStrength = (10.0 - Math.sqrt(d)) * 0.1;
                e.push(
                        Math.cos(a) * pullStrength,
                        (10.0 - Math.sqrt(d)) * 0.05 * pm,
                        Math.sin(a) * pullStrength);
            }
            double attackRange = (4.0 + e.getBbWidth() / 2.0);
            if (d < attackRange * attackRange && this.random.nextInt(8) == 2) {
                this.doHurtTarget(e);
            }
        }

        double dx = this.currentFlightTarget.getX() + 0.5 - this.getX();
        double dy = this.currentFlightTarget.getY() + 0.1 - this.getY();
        double dz = this.currentFlightTarget.getZ() + 0.5 - this.getZ();

        Vec3 motion = this.getDeltaMovement();
        double newMx = motion.x + (Math.signum(dx) * 0.4 - motion.x) * 0.2;
        double newMy = motion.y + (Math.signum(dy) * 0.7 - motion.y) * 0.2;
        double newMz = motion.z + (Math.signum(dz) * 0.4 - motion.z) * 0.2;
        this.setDeltaMovement(newMx, newMy, newMz);

        float targetYaw = (float) (Math.atan2(newMz, newMx) * 180.0 / Math.PI) - 90.0f;
        float yawDiff = Mth.wrapDegrees(targetYaw - this.getYRot());
        this.setYRot(this.getYRot() + yawDiff / 4.0f);
    }

    @Override
    public boolean causeFallDamage(float dist, float mult, DamageSource source) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        Entity attacker = source.getEntity();
        boolean ret = super.hurt(source, amount);
        if (attacker != null && this.currentFlightTarget != null) {
            this.currentFlightTarget = attacker.blockPosition();
        }
        this.winded = 20;
        return ret;
    }

    @Nullable
    private LivingEntity findSomethingToAttack() {
        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(16.0, 10.0, 16.0));
        entities.sort(Comparator.comparingDouble(this::distanceToSqr));
        for (LivingEntity e : entities) {
            if (isSuitableTarget(e)) return e;
        }
        return null;
    }

    private boolean isSuitableTarget(LivingEntity target) {
        if (target == null || target == this || !target.isAlive()) return false;
        if (target instanceof EntityVortex) return false;
        if (!this.getSensing().hasLineOfSight(target)) return false;
        if (target instanceof Player p) return !p.getAbilities().invulnerable;
        return true;
    }
}
