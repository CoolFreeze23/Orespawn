package danger.orespawn.entity;

import java.util.Comparator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import danger.orespawn.OreSpawnMod;

public class ThePrinceAdult extends TamableAnimal {
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(ThePrinceAdult.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_ACTIVITY =
            SynchedEntityData.defineId(ThePrinceAdult.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_FIRE =
            SynchedEntityData.defineId(ThePrinceAdult.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_HEAD1 =
            SynchedEntityData.defineId(ThePrinceAdult.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_HEAD2 =
            SynchedEntityData.defineId(ThePrinceAdult.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_HEAD3 =
            SynchedEntityData.defineId(ThePrinceAdult.class, EntityDataSerializers.INT);

    private final Comparator<Entity> targetSorter;
    private final float moveSpeed = 0.36f;
    private int hurtTimer = 0;
    private int head1dir = 1, head2dir = 1, head3dir = 1;

    public ThePrinceAdult(EntityType<? extends ThePrinceAdult> type, Level level) {
        super(type, level);
        this.xpReward = 3000;
        this.noPhysics = false;
        this.setOrderedToSit(false);
        this.targetSorter = Comparator.comparingDouble(this::distanceToSqr);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new FollowOwnerGoal(this, 1.1, 12.0f, 2.0f));
        this.goalSelector.addGoal(2, new TemptGoal(this, 1.25, Ingredient.of(Items.BEEF), false));
        this.goalSelector.addGoal(3, new MyEntityAIWander(this, 0.75f));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Mob.class, 20.0f));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Monster.class, true));
        this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 3000.0)
                .add(Attributes.MOVEMENT_SPEED, 0.36)
                .add(Attributes.ATTACK_DAMAGE, 100.0)
                .add(Attributes.FOLLOW_RANGE, 64.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.9);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, 0);
        builder.define(DATA_ACTIVITY, 0);
        builder.define(DATA_FIRE, 1);
        builder.define(DATA_HEAD1, 0);
        builder.define(DATA_HEAD2, 0);
        builder.define(DATA_HEAD3, 0);
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    public int getAttacking() { return this.entityData.get(DATA_ATTACKING); }
    public void setAttacking(int v) { this.entityData.set(DATA_ATTACKING, v); }
    public int getActivity() { return this.entityData.get(DATA_ACTIVITY); }
    public void setActivity(int v) { this.entityData.set(DATA_ACTIVITY, v); }
    public int getHead1Ext() { return this.entityData.get(DATA_HEAD1); }
    public int getHead2Ext() { return this.entityData.get(DATA_HEAD2); }
    public int getHead3Ext() { return this.entityData.get(DATA_HEAD3); }

    @Override
    public void tick() {
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(this.moveSpeed);
        super.tick();
        if (this.hurtTimer > 0) --this.hurtTimer;

        if (this.random.nextInt(100) == 1 && this.getHealth() < this.getMaxHealth()) {
            this.heal(5.0f);
        }

        int i;
        if (this.random.nextInt(10) == 1) {
            i = this.random.nextInt(3);
            this.head1dir = i == 0 ? 2 : i == 1 ? -2 : 0;
        }
        if (this.random.nextInt(10) == 1) {
            i = this.random.nextInt(3);
            this.head2dir = i == 0 ? 2 : i == 1 ? -2 : 0;
        }
        if (this.random.nextInt(10) == 1) {
            i = this.random.nextInt(3);
            this.head3dir = i == 0 ? 2 : i == 1 ? -2 : 0;
        }
        int h1 = Math.max(0, Math.min(60, this.getHead1Ext() + this.head1dir));
        int h2 = Math.max(0, Math.min(60, this.getHead2Ext() + this.head2dir));
        int h3 = Math.max(0, Math.min(60, this.getHead3Ext() + this.head3dir));
        if (!this.level().isClientSide) {
            this.entityData.set(DATA_HEAD1, h1);
            this.entityData.set(DATA_HEAD2, h2);
            this.entityData.set(DATA_HEAD3, h3);
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        return target.hurt(this.damageSources().mobAttack(this), 100.0f);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.hurtTimer > 0) return false;
        if (source.getMsgId().equals("cactus")) return false;
        boolean ret = super.hurt(source, amount);
        this.hurtTimer = 25;
        Entity e = source.getEntity();
        if (e instanceof LivingEntity living) {
            this.setTarget(living);
            this.getNavigation().moveTo(living, 1.2);
        }
        return ret;
    }

    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;
        super.customServerAiStep();

        if (this.random.nextInt(7) == 1) {
            LivingEntity target = this.getTarget();
            if (target != null && !target.isAlive()) {
                this.setTarget(null);
                target = null;
            }
            if (target == null) {
                target = findSomethingToAttack();
            }
            if (target != null) {
                this.getNavigation().moveTo(target, 1.5);
                this.lookAt(target, 10.0f, 10.0f);
                this.setAttacking(1);
                double maxdist = 8.0 + target.getBbWidth() / 2.0;
                if (this.distanceToSqr(target) < maxdist * maxdist) {
                    this.doHurtTarget(target);
                }
            } else {
                this.setAttacking(0);
            }
        }
    }

    private LivingEntity findSomethingToAttack() {
        AABB searchBox = this.getBoundingBox().inflate(32.0, 16.0, 32.0);
        List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, searchBox);
        targets.sort(Comparator.comparingDouble(this::distanceToSqr));
        for (LivingEntity target : targets) {
            if (target == this || !target.isAlive()) continue;
            if (!this.getSensing().hasLineOfSight(target)) continue;
            // TODO: check for royalty/allies
            if (target instanceof Monster) return target;
        }
        return null;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.has(net.minecraft.core.component.DataComponents.FOOD)
                && this.distanceToSqr(player) < 25.0) {
            if (!this.level().isClientSide) {
                this.heal(this.getMaxHealth() - this.getHealth());
            }
            if (!player.getAbilities().instabuild) stack.shrink(1);
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "roar"));
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "alo_hurt"));
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "alo_death"));
    }

    @Override protected float getSoundVolume() { return 1.5f; }
    @Override public boolean removeWhenFarAway(double dist) { return false; }
    @Override public boolean isFood(ItemStack stack) { return stack.is(Items.BEEF); }

    @Nullable @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) { return null; }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("PrinceActivity", this.getActivity());
        tag.putInt("PrinceAttacking", this.getAttacking());
        tag.putInt("PrinceFire", this.entityData.get(DATA_FIRE));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setActivity(tag.getInt("PrinceActivity"));
        this.setAttacking(tag.getInt("PrinceAttacking"));
        this.entityData.set(DATA_FIRE, tag.getInt("PrinceFire"));
    }
}
