package danger.orespawn.entity;

import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class PurplePower extends Mob {
    private static final EntityDataAccessor<Integer> DATA_PURPLE_TYPE =
            SynchedEntityData.defineId(PurplePower.class, EntityDataSerializers.INT);

    private BlockPos currentFlightTarget = null;
    private final Comparator<Entity> targetSorter;
    private int purpleType = 0;

    public PurplePower(EntityType<? extends PurplePower> type, Level level) {
        super(type, level);
        this.xpReward = 35;
        this.noPhysics = true;
        this.targetSorter = Comparator.comparingDouble(this::distanceToSqr);
    }

    @Override
    protected void registerGoals() {}

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 1000.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.ATTACK_DAMAGE, 500.0)
                .add(Attributes.ARMOR, 25.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_PURPLE_TYPE, 0);
    }

    public void setPurpleType(int v) {
        if (this.level() == null || this.level().isClientSide()) return;
        this.purpleType = v;
        this.entityData.set(DATA_PURPLE_TYPE, v);
    }

    public int getPurpleType() { return this.entityData.get(DATA_PURPLE_TYPE); }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        Vec3 dm = this.getDeltaMovement();
        this.setDeltaMovement(dm.x, dm.y * 0.6, dm.z);

        if (this.level().isClientSide()) {
            if (this.getPurpleType() == 0 && this.getRandom().nextInt(4) == 1) {
                this.level().addParticle(ParticleTypes.FIREWORK, getX(), getY() + 1.25, getZ(),
                        (this.getRandom().nextFloat() - this.getRandom().nextFloat()) / 2.0,
                        (this.getRandom().nextFloat() - this.getRandom().nextFloat()) / 2.0,
                        (this.getRandom().nextFloat() - this.getRandom().nextFloat()) / 2.0);
            } else if (this.getRandom().nextInt(6) == 1) {
                this.level().addParticle(ParticleTypes.FIREWORK, getX(), getY() + 0.65, getZ(),
                        (this.getRandom().nextFloat() - this.getRandom().nextFloat()) / 5.0,
                        (this.getRandom().nextFloat() - this.getRandom().nextFloat()) / 5.0,
                        (this.getRandom().nextFloat() - this.getRandom().nextFloat()) / 5.0);
            }
            this.purpleType = this.getPurpleType();
        } else {
            this.setPurpleType(this.purpleType);
        }

        if (!this.level().isClientSide() && this.getRandom().nextInt(2500) == 1) {
            if (this.getPurpleType() == 10) {
                this.level().explode(null, getX(), getY() + 0.25, getZ(), 9.1f, Level.ExplosionInteraction.MOB);
            }
            this.discard();
        }
    }

    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;
        super.customServerAiStep();
        if (this.currentFlightTarget == null) {
            this.currentFlightTarget = this.blockPosition();
        }
        if (this.getRandom().nextInt(300) == 0 || this.currentFlightTarget.distSqr(this.blockPosition()) < 4) {
            int xdir = this.getRandom().nextInt(10) + 8;
            int zdir = this.getRandom().nextInt(10) + 8;
            if (this.getRandom().nextInt(2) == 0) xdir = -xdir;
            if (this.getRandom().nextInt(2) == 0) zdir = -zdir;
            this.currentFlightTarget = new BlockPos(
                    (int) this.getX() + xdir,
                    (int) this.getY() + this.getRandom().nextInt(20) - 10,
                    (int) this.getZ() + zdir);
        } else if (this.getRandom().nextInt(7) == 2) {
            LivingEntity e = findSomethingToAttack();
            if (e != null) {
                this.currentFlightTarget = new BlockPos((int) e.getX(), (int) (e.getY() + e.getBbHeight() / 2.0f), (int) e.getZ());
                double meleeRange = (4.0f + e.getBbWidth() / 2.0f);
                if (this.distanceToSqr(e) < meleeRange * meleeRange) {
                    this.doHurtTarget(e);
                    this.discard();
                }
            }
        }
        double var1 = this.currentFlightTarget.getX() + 0.5 - this.getX();
        double var3 = this.currentFlightTarget.getY() + 0.1 - this.getY();
        double var5 = this.currentFlightTarget.getZ() + 0.5 - this.getZ();
        Vec3 dm = this.getDeltaMovement();
        this.setDeltaMovement(
                dm.x + (Math.signum(var1) * 0.4 - dm.x) * 0.2,
                dm.y + (Math.signum(var3) * 0.7 - dm.y) * 0.2,
                dm.z + (Math.signum(var5) * 0.4 - dm.z) * 0.2
        );
        float var7 = (float) (Math.atan2(this.getDeltaMovement().z, this.getDeltaMovement().x) * 180.0 / Math.PI) - 90.0f;
        float var8 = Mth.wrapDegrees(var7 - this.getYRot());
        this.setYRot(this.getYRot() + var8 / 4.0f);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (target instanceof LivingEntity le) {
            int type = this.getPurpleType();
            if (type == 0 || type == 10) {
                le.setHealth(le.getHealth() / 4.0f - 1.0f);
                boolean ret = le.hurt(this.damageSources().mobAttack(this), le.getMaxHealth() / 8.0f);
                if (type == 10) {
                    this.level().explode(null, le.getX(), le.getY() - 0.25, le.getZ(), 9.1f, Level.ExplosionInteraction.MOB);
                }
                return ret;
            } else {
                le.setHealth(le.getHealth() * 15.0f / 16.0f);
                boolean ret = le.hurt(this.damageSources().mobAttack(this), 5.0f);
                if (type == 1) le.setRemainingFireTicks(200);
                if (type == 2) le.addEffect(new MobEffectInstance(MobEffects.HUNGER, 50, 0));
                if (type == 3) le.addEffect(new MobEffectInstance(MobEffects.POISON, 50, 0));
                return ret;
            }
        }
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        Entity e = source.getEntity();
        if (e instanceof Arrow) return false;
        float dm = Math.min(amount, 10.0f);
        boolean ret = super.hurt(source, dm);
        if (e != null && this.currentFlightTarget != null) {
            this.currentFlightTarget = new BlockPos((int) e.getX(), (int) (e.getY() + e.getBbHeight() / 2.0f), (int) e.getZ());
        }
        return ret;
    }

    @Override
    public boolean causeFallDamage(float dist, float mult, DamageSource source) {
        return false;
    }

    private LivingEntity findSomethingToAttack() {
        AABB searchBox = this.getBoundingBox().inflate(32.0, 24.0, 32.0);
        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class, searchBox);
        entities.sort(this.targetSorter);
        for (LivingEntity e : entities) {
            if (isSuitableTarget(e)) return e;
        }
        return null;
    }

    private boolean isSuitableTarget(LivingEntity target) {
        if (target == null || target == this || !target.isAlive()) return false;
        if (target instanceof Player p && p.getAbilities().instabuild) return false;
        int type = this.getPurpleType();
        if (target instanceof Player) {
            return type <= 0 || type == 10;
        }
        return true;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("PurpleType", this.purpleType);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.purpleType = tag.getInt("PurpleType");
    }
}
