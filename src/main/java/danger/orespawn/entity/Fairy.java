package danger.orespawn.entity;

import java.util.Comparator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import danger.orespawn.OreSpawnMod;

public class Fairy extends AmbientCreature {
    private static final EntityDataAccessor<Integer> FAIRY_TYPE =
            SynchedEntityData.defineId(Fairy.class, EntityDataSerializers.INT);
    int myBlink = 20 + this.random.nextInt(20);
    int blinker = 0;
    private BlockPos currentFlightTarget = null;
    private String myowner = null;
    private final Comparator<Entity> targetSorter;

    public Fairy(EntityType<? extends Fairy> type, Level level) {
        super(type, level);
        this.setFairyType(this.random.nextInt(9));
        this.targetSorter = Comparator.comparingDouble(this::distanceToSqr);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new LookAtPlayerGoal(this, Mob.class, 8.0f));
        this.goalSelector.addGoal(1, new RandomLookAroundGoal(this));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(FAIRY_TYPE, 0);
    }

    public int getFairyType() { return this.entityData.get(FAIRY_TYPE); }
    public void setFairyType(int type) { this.entityData.set(FAIRY_TYPE, type); }

    public float getBlink() {
        return this.blinker < this.myBlink / 2 ? 240.0f : 0.0f;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 40.0)
                .add(Attributes.MOVEMENT_SPEED, 0.1)
                .add(Attributes.ATTACK_DAMAGE, 3.0);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (this.level().getDifficulty() == Difficulty.PEACEFUL) return false;
        return target.hurt(this.damageSources().mobAttack(this), 2.0f);
    }

    @Override
    public void tick() {
        super.tick();
        Vec3 mot = this.getDeltaMovement();
        this.setDeltaMovement(mot.x, mot.y * 0.6, mot.z);
        this.blinker++;
        if (this.blinker > this.myBlink) this.blinker = 0;

        long t = this.level().getDayTime() % 24000L;
        if (t < 12000L) return;

        if (this.level().isClientSide && this.random.nextInt(5) == 0 && this.getBlink() > 1.0f) {
            this.level().addParticle(ParticleTypes.FIREWORK,
                    this.getX(), this.getY() - 0.15, this.getZ(),
                    (this.random.nextFloat() - this.random.nextFloat()) / 8.0,
                    -this.random.nextFloat() / 8.0,
                    (this.random.nextFloat() - this.random.nextFloat()) / 8.0);
        }
    }

    private boolean isSuitableTarget(LivingEntity target) {
        if (this.level().getDifficulty() == Difficulty.PEACEFUL) return false;
        if (target == null || target == this || !target.isAlive()) return false;
        return target instanceof Monster;
    }

    private LivingEntity findSomethingToAttack() {
        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(8.0, 8.0, 8.0));
        entities.sort(this.targetSorter);
        for (LivingEntity e : entities) {
            if (this.isSuitableTarget(e)) return e;
        }
        return null;
    }

    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;
        super.customServerAiStep();

        if (this.currentFlightTarget == null) {
            this.currentFlightTarget = this.blockPosition();
        }

        double distSq = this.currentFlightTarget.distSqr(this.blockPosition());
        if (this.random.nextInt(200) == 0 || distSq < 2.5) {
            for (int tries = 25; tries > 0; tries--) {
                int xdir = this.random.nextInt(8); if (this.random.nextInt(2) == 0) xdir = -xdir;
                int zdir = this.random.nextInt(8); if (this.random.nextInt(2) == 0) zdir = -zdir;
                BlockPos newTarget = new BlockPos(
                        (int) this.getX() + xdir,
                        (int) this.getY() + this.random.nextInt(5) - 2,
                        (int) this.getZ() + zdir);
                if (this.level().getBlockState(newTarget).isAir()) {
                    this.currentFlightTarget = newTarget;
                    break;
                }
            }
        } else if (this.random.nextInt(12) == 0 && this.level().getDifficulty() != Difficulty.PEACEFUL) {
            LivingEntity e = this.findSomethingToAttack();
            if (e != null) {
                this.currentFlightTarget = new BlockPos((int) e.getX(), (int) e.getY() + 1, (int) e.getZ());
                if (this.distanceToSqr(e) < 6.0) {
                    this.doHurtTarget(e);
                }
            }
        } else if (this.myowner != null) {
            Player p = this.level().getPlayerByUUID(java.util.UUID.fromString(this.myowner));
            if (p != null) {
                if (this.distanceToSqr(p) > 64.0) {
                    this.currentFlightTarget = new BlockPos(
                            (int) p.getX() + this.random.nextInt(3) - this.random.nextInt(3),
                            (int) p.getY() + 1,
                            (int) p.getZ() + this.random.nextInt(3) - this.random.nextInt(3));
                }
                if (this.distanceToSqr(p) > 256.0) {
                    this.teleportTo(p.getX() + this.random.nextFloat() - this.random.nextFloat(),
                            p.getY(), p.getZ() + this.random.nextFloat() - this.random.nextFloat());
                }
            }
        }

        if (this.random.nextInt(250) == 1) this.heal(1.0f);

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

    @Override
    protected float getSoundVolume() { return 0.25f; }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() { return null; }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "rat_hit"));
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "big_splat"));
    }

    @Override
    public boolean isPushable() { return true; }

    @Override
    public boolean causeFallDamage(float dist, float mult, DamageSource source) { return false; }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) { }

    @Override
    public boolean removeWhenFarAway(double dist) {
        if (this.isPersistenceRequired()) return false;
        return this.myowner == null;
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnType) {
        int airCount = 0;
        for (int k = -1; k <= 1; k++) {
            for (int j = -1; j <= 1; j++) {
                if (level.getBlockState(new BlockPos((int) this.getX() + j, (int) this.getY(), (int) this.getZ() + k)).isAir())
                    airCount++;
            }
        }
        if (airCount < 6) return false;
        return this.getY() >= 50.0;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("MyOwner", this.myowner != null ? this.myowner : "null");
        tag.putInt("FairyType", this.getFairyType());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.myowner = tag.getString("MyOwner");
        if ("null".equals(this.myowner)) this.myowner = null;
        this.setFairyType(tag.getInt("FairyType"));
    }
}
