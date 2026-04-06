package danger.orespawn.entity;

import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.resources.ResourceLocation;
import danger.orespawn.OreSpawnMod;

public class PitchBlack extends Monster {
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(PitchBlack.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_ACTIVITY =
            SynchedEntityData.defineId(PitchBlack.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_SCALE =
            SynchedEntityData.defineId(PitchBlack.class, EntityDataSerializers.INT);

    private BlockPos currentFlightTarget = null;
    private final Comparator<Entity> targetSorter;
    private int damageTicker = 0;
    private int wingSound = 0;

    public PitchBlack(EntityType<? extends PitchBlack> type, Level level) {
        super(type, level);
        this.xpReward = 200;
        this.targetSorter = Comparator.comparingDouble(this::distanceToSqr);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 10.0f));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 500.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ATTACK_DAMAGE, 50.0)
                .add(Attributes.ARMOR, 6.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, 0);
        builder.define(DATA_ACTIVITY, 0);
        builder.define(DATA_SCALE, 5);
    }

    public int getAttacking() { return this.entityData.get(DATA_ATTACKING); }
    public void setAttacking(int v) { this.entityData.set(DATA_ATTACKING, v); }
    public int getActivity() { return this.entityData.get(DATA_ACTIVITY); }
    public void setActivity(int v) { this.entityData.set(DATA_ACTIVITY, v); }

    public float getPitchBlackScale() {
        return this.entityData.get(DATA_SCALE) / 10.0f;
    }

    public void setPitchBlackScale(float v) {
        this.entityData.set(DATA_SCALE, (int) (v * 10.0001f));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setPitchBlackScale(tag.getFloat("Fscale"));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("Fscale", this.getPitchBlackScale());
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (target instanceof LivingEntity le) {
            float scale = this.getPitchBlackScale();
            boolean ret = le.hurt(this.damageSources().mobAttack(this), 50.0f * scale);
            if (ret) {
                double ks = 1.15 * scale;
                double inair = 0.08 * scale;
                float f3 = (float) Math.atan2(le.getZ() - this.getZ(), le.getX() - this.getX());
                if (le.isRemoved() || le instanceof Player) inair *= 2.0;
                le.push(Math.cos(f3) * ks, inair, Math.sin(f3) * ks);
            }
            return ret;
        }
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        ++this.wingSound;
        if (this.wingSound > 20) {
            if (!this.level().isClientSide()) {
                this.playSound(SoundEvent.createVariableRangeEvent(
                        ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "MothraWings")), 1.0f, 1.0f);
            }
            this.wingSound = 0;
        }
        Vec3 dm = this.getDeltaMovement();
        this.setDeltaMovement(dm.x, dm.y * 0.6, dm.z);
    }

    @Override
    protected void customServerAiStep() {
        if (this.damageTicker > 0) --this.damageTicker;
        if (this.getActivity() == 0) {
            super.customServerAiStep();
            if (this.getRandom().nextInt(10) == 1) {
                Entity e = findSomethingToAttack();
                if (e != null) {
                    this.setActivity(1);
                    this.getNavigation().stop();
                }
            }
            return;
        }
        if (this.isRemoved()) return;
        if (this.currentFlightTarget == null) {
            this.currentFlightTarget = this.blockPosition();
        }
        float scale = this.getPitchBlackScale();
        if (this.getRandom().nextInt(150) == 0
                || this.currentFlightTarget.distSqr(this.blockPosition()) < 4) {
            int xdir = this.getRandom().nextInt(20) + (int) (5 * scale);
            int zdir = this.getRandom().nextInt(20) + (int) (5 * scale);
            if (this.getRandom().nextInt(2) == 0) xdir = -xdir;
            if (this.getRandom().nextInt(2) == 0) zdir = -zdir;
            this.currentFlightTarget = new BlockPos(
                    (int) this.getX() + xdir,
                    (int) this.getY() + this.getRandom().nextInt(11) - 5,
                    (int) this.getZ() + zdir);
        } else if (this.getRandom().nextInt(8) == 0) {
            Entity e = findSomethingToAttack();
            if (e != null) {
                double d1 = 5.0 + e.getBbWidth() / 2.0f + scale;
                this.setAttacking(1);
                this.currentFlightTarget = new BlockPos((int) e.getX(), (int) (e.getY() + 2), (int) e.getZ());
                if (this.distanceToSqr(e) < d1 * d1) {
                    this.doHurtTarget(e);
                }
            } else {
                this.setAttacking(0);
            }
        }
        double var1 = this.currentFlightTarget.getX() + 0.4 - this.getX();
        double var3 = this.currentFlightTarget.getY() + 0.1 - this.getY();
        double var5 = this.currentFlightTarget.getZ() + 0.4 - this.getZ();
        double myspeed = 0.5f + scale / 10.0f;
        Vec3 dm = this.getDeltaMovement();
        this.setDeltaMovement(
                dm.x + (Math.signum(var1) * myspeed - dm.x) * 0.33,
                dm.y + (Math.signum(var3) * 0.7 - dm.y) * 0.2,
                dm.z + (Math.signum(var5) * myspeed - dm.z) * 0.33
        );
        float var7 = (float) (Math.atan2(this.getDeltaMovement().z, this.getDeltaMovement().x) * 180.0 / Math.PI) - 90.0f;
        float var8 = Mth.wrapDegrees(var7 - this.getYRot());
        this.setYRot(this.getYRot() + var8 / 5.0f);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.damageTicker > 0) return false;
        this.damageTicker = 20;
        boolean ret = super.hurt(source, amount);
        Entity e = source.getEntity();
        if (e != null && this.currentFlightTarget != null) {
            this.currentFlightTarget = new BlockPos((int) e.getX(), (int) (e.getY() + 2), (int) e.getZ());
        }
        this.setActivity(1);
        this.getNavigation().stop();
        return ret;
    }

    @Override
    public boolean causeFallDamage(float dist, float mult, DamageSource source) {
        return false;
    }

    private Entity findSomethingToAttack() {
        float scale = this.getPitchBlackScale();
        double d1 = 16.0 + scale * 6.0;
        double d2 = 10.0 + scale * 4.0;
        AABB searchBox = this.getBoundingBox().inflate(d1, d2, d1);
        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class, searchBox);
        entities.sort(this.targetSorter);
        for (LivingEntity e : entities) {
            if (isSuitableTarget(e)) return e;
        }
        return null;
    }

    private boolean isSuitableTarget(LivingEntity target) {
        if (target == null || target == this || !target.isAlive()) return false;
        if (target instanceof PitchBlack) return false;
        if (target instanceof Player p && p.getAbilities().instabuild) return false;
        return true;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        if (this.getRandom().nextInt(5) != 2) return null;
        return SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "pitchblack_living"));
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource ds) {
        return SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "pitchblack_hit"));
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "pitchblack_dead"));
    }
}
