package danger.orespawn.entity;

import java.util.Comparator;
import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.resources.ResourceLocation;
import danger.orespawn.OreSpawnMod;

public class Robot4 extends Monster {
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(Robot4.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_SHIELDING =
            SynchedEntityData.defineId(Robot4.class, EntityDataSerializers.INT);

    private final Comparator<Entity> targetSorter;
    private int reloadTicker = 0;
    private int wasAttackedTicker = 0;
    private final float moveSpeed = 0.34f;

    public Robot4(EntityType<? extends Robot4> type, Level level) {
        super(type, level);
        this.xpReward = 120;
        this.targetSorter = Comparator.comparingDouble(this::distanceToSqr);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 750.0)
                .add(Attributes.MOVEMENT_SPEED, 0.34)
                .add(Attributes.ATTACK_DAMAGE, 40.0)
                .add(Attributes.ARMOR, 10.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, 0);
        builder.define(DATA_SHIELDING, 0);
    }

    public int getAttacking() { return this.entityData.get(DATA_ATTACKING); }
    public void setAttacking(int value) { this.entityData.set(DATA_ATTACKING, value); }
    public int getShielding() { return this.entityData.get(DATA_SHIELDING); }
    public void setShielding(int value) { this.entityData.set(DATA_SHIELDING, value); }

    @Override
    public void jumpFromGround() {
        Vec3 dm = this.getDeltaMovement();
        this.setDeltaMovement(dm.x, dm.y + 0.25, dm.z);
        super.jumpFromGround();
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide()) {
            if (this.getRandom().nextInt(3) == 1) {
                double angle = Math.toRadians(this.getYRot() + 180.0f);
                this.level().addParticle(ParticleTypes.SMOKE,
                        getX() - 1.25 * Math.sin(angle), getY() + 3.0, getZ() + 1.25 * Math.cos(angle),
                        0, this.getRandom().nextFloat() / 2.0, 0);
            }
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (target instanceof LivingEntity le) {
            double ks = 2.0;
            double inair = 0.12;
            float f3 = (float) Math.atan2(le.getZ() - this.getZ(), le.getX() - this.getX());
            if (le.isRemoved() || le instanceof Player) inair *= 2.0;
            le.push(Math.cos(f3) * ks, inair, Math.sin(f3) * ks);
        }
        return super.doHurtTarget(target);
    }

    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;
        super.customServerAiStep();
        if (this.reloadTicker > 0) --this.reloadTicker;
        if (this.wasAttackedTicker > 0) --this.wasAttackedTicker;
        if (this.reloadTicker == 0 && this.getRandom().nextInt(8) == 1) {
            LivingEntity e = this.getTarget();
            if (this.getRandom().nextInt(50) == 1) this.setTarget(null);
            if (e != null && !e.isAlive()) { this.setTarget(null); e = null; }
            if (e == null) e = findSomethingToAttack();
            if (e != null) {
                this.lookAt(e, 10.0f, 10.0f);
                if (this.distanceToSqr(e) < 256.0) {
                    double meleeRange = (3.0f + e.getBbWidth() / 2.0f);
                    if (this.distanceToSqr(e) < meleeRange * meleeRange) {
                        this.doHurtTarget(e);
                    }
                    this.setAttacking(1);
                    this.reloadTicker = 10;
                    this.getNavigation().moveTo(e, 0.75);
                }
            }
        }
        if (this.reloadTicker <= 0 && this.wasAttackedTicker <= 0) {
            this.setAttacking(0);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getMsgId().equals("cactus")) return false;
        if (this.getShielding() != 0 || this.wasAttackedTicker != 0) return false;
        this.wasAttackedTicker = 65;
        this.setAttacking(1);
        boolean ret = super.hurt(source, amount);
        Entity e = source.getEntity();
        if (e instanceof Mob mob) {
            this.setTarget(mob);
            this.getNavigation().moveTo(mob, 1.2);
        }
        return ret;
    }

    private LivingEntity findSomethingToAttack() {
        AABB searchBox = this.getBoundingBox().inflate(16.0, 4.0, 16.0);
        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class, searchBox);
        entities.sort(this.targetSorter);
        for (LivingEntity e : entities) {
            if (isSuitableTarget(e)) return e;
        }
        return null;
    }

    private boolean isSuitableTarget(LivingEntity target) {
        if (target == null || target == this || !target.isAlive()) return false;
        if (target instanceof Monster) return false;
        if (target instanceof Player p && p.getAbilities().instabuild) return false;
        return true;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        if (this.getRandom().nextInt(4) == 0)
            return SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "robot_living"));
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource ds) {
        return SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "robot_hurt"));
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "robot_death"));
    }
}
