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
import danger.orespawn.util.MyUtils;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import danger.orespawn.OreSpawnMod;

public class ThePrince extends TamableAnimal {
    private static final EntityDataAccessor<Integer> DATA_ACTIVITY =
            SynchedEntityData.defineId(ThePrince.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_FIRE =
            SynchedEntityData.defineId(ThePrince.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(ThePrince.class, EntityDataSerializers.INT);

    private final Comparator<Entity> targetSorter;
    private final float moveSpeed = 0.32f;
    private int head1ext = 0;
    private int head2ext = 0;
    private int head3ext = 0;
    private int head1dir = 1;
    private int head2dir = 1;
    private int head3dir = 1;
    private int okToGrow = 0;
    private int killCount = 0;
    private int fedCount = 0;
    private int dayCount = 0;
    private int isDay = 0;

    public ThePrince(EntityType<? extends ThePrince> type, Level level) {
        super(type, level);
        this.xpReward = 50;
        this.noPhysics = false;
        this.setOrderedToSit(false);
        this.targetSorter = Comparator.comparingDouble(this::distanceToSqr);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new FollowOwnerGoal(this, 1.15, 12.0f, 2.0f));
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.25, Ingredient.of(Items.BEEF), false));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Mob.class, 6.0f));
        this.goalSelector.addGoal(5, new MyEntityAIWander(this, 0.75f));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 500.0)
                .add(Attributes.MOVEMENT_SPEED, 0.32)
                .add(Attributes.ATTACK_DAMAGE, 10.0)
                .add(Attributes.ARMOR, 16.0)
                .add(Attributes.FOLLOW_RANGE, 24.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ACTIVITY, 1);
        builder.define(DATA_FIRE, 1);
        builder.define(DATA_ATTACKING, 0);
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    public int getActivity() {
        return this.entityData.get(DATA_ACTIVITY);
    }

    public void setActivity(int value) {
        this.entityData.set(DATA_ACTIVITY, value);
    }

    public int getSpyroFire() {
        return this.entityData.get(DATA_FIRE);
    }

    public void setSpyroFire(int value) {
        this.entityData.set(DATA_FIRE, value);
    }

    public int getAttacking() {
        return this.entityData.get(DATA_ATTACKING);
    }

    public void setAttacking(int value) {
        this.entityData.set(DATA_ATTACKING, value);
    }

    public int getHead1Ext() { return this.head1ext; }
    public int getHead2Ext() { return this.head2ext; }
    public int getHead3Ext() { return this.head3ext; }

    @Override
    public void tick() {
        super.tick();
        this.noPhysics = this.getActivity() == 2;

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
        this.head1ext = Math.max(0, Math.min(60, this.head1ext + this.head1dir));
        this.head2ext = Math.max(0, Math.min(60, this.head2ext + this.head2dir));
        this.head3ext = Math.max(0, Math.min(60, this.head3ext + this.head3dir));
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        float damage = 10.0f;
        boolean result = target.hurt(this.damageSources().mobAttack(this), damage);
        if (target instanceof LivingEntity living && living.getHealth() <= 0.0f) {
            ++this.killCount;
        }
        return result;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getMsgId().equals("inWall") || source.getMsgId().equals("cactus")) return false;
        boolean ret = super.hurt(source, amount);
        this.setOrderedToSit(false);
        this.setActivity(2);
        return ret;
    }

    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;

        if (this.random.nextInt(200) == 1) {
            this.setTarget(null);
        }

        if (this.getActivity() != 2) {
            super.customServerAiStep();
        }

        if (this.random.nextInt(200) == 1 && this.getHealth() < this.getMaxHealth()) {
            this.heal(1.0f);
        }

        if (!this.isTame()) {
            Player player = this.level().getNearestPlayer(this, 10.0);
            if (player != null) {
                this.tame(player);
                this.level().broadcastEntityEvent(this, (byte) 7);
                this.heal(this.getMaxHealth() - this.getHealth());
            }
        }

        if (!this.isOrderedToSit() && this.random.nextInt(7) == 1) {
            LivingEntity target = this.findSomethingToAttack();
            if (target != null) {
                this.setActivity(2);
                this.setAttacking(1);
                double attackRange = (3.0 + target.getBbWidth() / 2.0) * (3.0 + target.getBbWidth() / 2.0);
                if (this.distanceToSqr(target) < attackRange) {
                    this.doHurtTarget(target);
                }
            } else {
                this.setAttacking(0);
            }
        }

        if (this.isDay == 0) {
            this.isDay = 1;
            if (!this.level().isDay()) this.isDay = -1;
        } else {
            if (this.isDay == -1 && this.level().isDay()) ++this.dayCount;
            this.isDay = this.level().isDay() ? 1 : -1;
        }
    }

    private boolean isSuitableTarget(LivingEntity target) {
        if (target == null || target == this || !target.isAlive()) return false;
        if (!this.getSensing().hasLineOfSight(target)) return false;
        if (MyUtils.isRoyalty(target)) return false;
        if (target instanceof Monster) return true;
        if (target instanceof Mothra || target instanceof EntityButterfly || target instanceof Cockateil || target instanceof EntityDragonfly || target instanceof EntityMosquito) return false;
        return false;
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
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (stack.is(Blocks.BEACON.asItem()) && this.distanceToSqr(player) < 16.0) {
            if (!this.level().isClientSide) {
                this.tame(player);
                this.level().broadcastEntityEvent(this, (byte) 7);
                this.heal(this.getMaxHealth() - this.getHealth());
                this.okToGrow = 1;
                this.killCount = 1000;
                this.fedCount = 1000;
                this.dayCount = 1000;
            }
            if (!player.getAbilities().instabuild) stack.shrink(1);
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        if (this.isTame() && this.isOwnedBy(player)
                && this.distanceToSqr(player) < 16.0
                && stack.has(net.minecraft.core.component.DataComponents.FOOD)) {
            if (!this.level().isClientSide) {
                if (this.getMaxHealth() > this.getHealth()) {
                    this.heal(20.0f);
                }
                this.level().broadcastEntityEvent(this, (byte) 7);
                ++this.fedCount;
            }
            if (!player.getAbilities().instabuild) stack.shrink(1);
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        if (this.isTame() && this.isOwnedBy(player)
                && this.distanceToSqr(player) < 16.0) {
            this.setOrderedToSit(!this.isOrderedToSit());
            this.setInSittingPose(this.isOrderedToSit());
            if (this.isOrderedToSit()) {
                this.setActivity(1);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        return super.mobInteract(player, hand);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        if (this.isOrderedToSit() || this.getAttacking() == 0) return null;
        return SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "roar"));
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "duck_hurt"));
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "cryo_death"));
    }

    @Override
    protected float getSoundVolume() { return 0.6f; }

    @Override
    public boolean removeWhenFarAway(double dist) { return false; }

    @Override
    public boolean isFood(ItemStack stack) { return stack.is(Items.BEEF); }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) { return null; }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("SpyroActivity", this.getActivity());
        tag.putInt("SpyroFire", this.getSpyroFire());
        tag.putInt("SpyroGrow", this.okToGrow);
        tag.putInt("SpyroKill", this.killCount);
        tag.putInt("SpyroFed", this.fedCount);
        tag.putInt("SpyroDay", this.dayCount);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setActivity(tag.getInt("SpyroActivity"));
        this.setSpyroFire(tag.getInt("SpyroFire"));
        this.okToGrow = tag.getInt("SpyroGrow");
        this.killCount = tag.getInt("SpyroKill");
        this.fedCount = tag.getInt("SpyroFed");
        this.dayCount = tag.getInt("SpyroDay");
    }
}
