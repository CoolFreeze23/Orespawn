package danger.orespawn.entity;

import java.util.Comparator;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
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
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import danger.orespawn.ModItems;
import danger.orespawn.OreSpawnMod;

public class EntityMolenoid extends Monster {
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(EntityMolenoid.class, EntityDataSerializers.INT);

    private final float moveSpeed = 0.35f;

    public EntityMolenoid(EntityType<? extends EntityMolenoid> type, Level level) {
        super(type, level);
        this.xpReward = 40;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MyEntityAIWanderALot(this, 16, 1.0));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 100.0)
                .add(Attributes.MOVEMENT_SPEED, 0.35)
                .add(Attributes.ATTACK_DAMAGE, 10.0)
                .add(Attributes.FOLLOW_RANGE, 16.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, 0);
    }

    public int getAttacking() {
        return this.entityData.get(DATA_ATTACKING);
    }

    public void setAttacking(int val) {
        this.entityData.set(DATA_ATTACKING, val);
    }

    @Override
    public boolean removeWhenFarAway(double dist) {
        return !this.isPersistenceRequired();
    }

    @Override
    public void tick() {
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(this.moveSpeed);
        super.tick();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getMsgId().equals("inWall")) {
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean ret = super.doHurtTarget(target);
        if (ret && target instanceof LivingEntity living) {
            double ks = 0.8;
            double inair = 0.1;
            float angle = (float) Math.atan2(target.getZ() - this.getZ(), target.getX() - this.getX());
            if (target.isRemoved() || target instanceof Player) {
                inair *= 2.0;
            }
            target.push(Math.cos(angle) * ks, inair, Math.sin(angle) * ks);
        }
        return ret;
    }

    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;
        super.customServerAiStep();

        if (this.random.nextInt(4) == 0) {
            LivingEntity target = this.findSomethingToAttack();
            if (target != null) {
                this.lookAt(target, 10.0f, 10.0f);
                double distSq = this.distanceToSqr(target);
                float attackRange = 6.0f + target.getBbWidth() / 2.0f;
                if (distSq < (double)(attackRange * attackRange)) {
                    this.setAttacking(1);
                    if (distSq < 16.0 && (this.random.nextInt(4) == 0 || this.random.nextInt(5) == 1)) {
                        this.doHurtTarget(target);
                    } else {
                        throwBlocksAtTarget(target);
                    }
                } else {
                    this.getNavigation().moveTo(target, 1.25);
                }
            } else {
                this.setAttacking(0);
            }
        }

        if (!this.level().isClientSide) {
            clearPathBehind();
        }
    }

    private void throwBlocksAtTarget(LivingEntity target) {
        int count = 1 + this.random.nextInt(4);
        for (int k = 0; k < count; k++) {
            double dx = target.getX() + (this.random.nextFloat() - this.random.nextFloat()) * 2.0;
            double dz = target.getZ() + (this.random.nextFloat() - this.random.nextFloat()) * 2.0;
            for (int i = 4; i > -3; i--) {
                BlockPos checkAbove = BlockPos.containing(dx, target.getY() + i + 1, dz);
                BlockPos checkAt = BlockPos.containing(dx, target.getY() + i, dz);
                if (this.level().getBlockState(checkAbove).isAir() && !this.level().getBlockState(checkAt).isAir()) {
                    if (this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
                        this.level().setBlock(checkAbove, Blocks.DIRT.defaultBlockState(), 2);
                    }
                    break;
                }
            }
        }
    }

    private void clearPathBehind() {
        double dx = this.getX() - 3.0 * Math.sin(Math.toRadians(this.yBodyRot));
        double dz = this.getZ() + 3.0 * Math.cos(Math.toRadians(this.yBodyRot));
        dx += (this.random.nextFloat() - this.random.nextFloat()) * 3.0;
        dz += (this.random.nextFloat() - this.random.nextFloat()) * 3.0;

        if (this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
            for (int i = 0; i < 3; i++) {
                BlockPos pos = BlockPos.containing(dx, this.getY() + i, dz);
                if (this.level().getBlockState(pos).is(Blocks.DIRT) ||
                        this.level().getBlockState(pos).is(Blocks.GRASS_BLOCK) ||
                        this.level().getBlockState(pos).is(Blocks.GRAVEL) ||
                        this.level().getBlockState(pos).is(Blocks.SAND)) {
                    this.level().setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                }
            }
        }
    }

    private boolean isSuitableTarget(LivingEntity target) {
        if (target == null || target == this || !target.isAlive()) return false;
        if (!this.getSensing().hasLineOfSight(target)) return false;
        if (target instanceof EntityMolenoid) return false;
        if (target instanceof Player player) {
            return !player.getAbilities().invulnerable;
        }
        return true;
    }

    private LivingEntity findSomethingToAttack() {
        AABB searchBox = this.getBoundingBox().inflate(12.0, 6.0, 12.0);
        List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, searchBox);
        targets.sort(Comparator.comparingDouble(this::distanceToSqr));
        for (LivingEntity target : targets) {
            if (this.isSuitableTarget(target)) return target;
        }
        return null;
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        this.spawnAtLocation(new ItemStack(ModItems.MOLENOID_NOSE.get(), 1));
        this.spawnAtLocation(new ItemStack(Items.NAME_TAG, 1));
        for (int i = 0; i < 10; i++) {
            this.spawnAtLocation(new ItemStack(Items.LEATHER, 1));
        }
        for (int i = 0; i < 6; i++) {
            this.spawnAtLocation(new ItemStack(Items.BONE, 1));
        }
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        if (this.random.nextInt(3) == 0) {
            return SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "molenoid_living"));
        }
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "molenoid_hit"));
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "molenoid_death"));
    }

    @Override
    protected float getSoundVolume() {
        return 1.1f;
    }
}
