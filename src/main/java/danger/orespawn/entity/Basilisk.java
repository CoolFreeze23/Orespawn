package danger.orespawn.entity;

import java.util.Comparator;
import java.util.List;
import danger.orespawn.ModItems;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public class Basilisk extends Monster {
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(Basilisk.class, EntityDataSerializers.INT);

    private final Comparator<Entity> targetSorter;
    private int hurtTimer = 0;
    private final float moveSpeed = 0.4f;

    public Basilisk(EntityType<? extends Basilisk> type, Level level) {
        super(type, level);
        this.xpReward = 150;
        this.targetSorter = Comparator.comparingDouble(this::distanceToSqr);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 500.0)
                .add(Attributes.MOVEMENT_SPEED, 0.4)
                .add(Attributes.ATTACK_DAMAGE, 25.0)
                .add(Attributes.FOLLOW_RANGE, 40.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8)
                .add(Attributes.ARMOR, 8.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, 0);
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

    public int mygetMaxHealth() {
        return 500;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        if (this.getRandom().nextInt(2) == 0) {
            return SoundEvents.RAVAGER_ROAR;
        }
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.RAVAGER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.RAVAGER_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 1.0f;
    }

    @Override
    public float getVoicePitch() {
        return 1.0f;
    }

    private void enchantItem(ItemStack stack, ResourceKey<Enchantment> key, int enchLevel) {
        this.level().registryAccess()
                .lookup(Registries.ENCHANTMENT)
                .flatMap(reg -> reg.get(key))
                .ifPresent(holder -> stack.enchant(holder, enchLevel));
    }

    private ItemStack dropItemRand(ItemStack stack) {
        double ox = this.getX() + this.getRandom().nextInt(4) - this.getRandom().nextInt(4);
        double oy = this.getY() + 1.0;
        double oz = this.getZ() + this.getRandom().nextInt(4) - this.getRandom().nextInt(4);
        ItemEntity itemEntity = new ItemEntity(this.level(), ox, oy, oz, stack);
        this.level().addFreshEntity(itemEntity);
        return stack;
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        dropItemRand(new ItemStack(ModItems.BASILISK_SCALE.get(), 1));
        dropItemRand(new ItemStack(Items.GOLDEN_APPLE, 1));
        int emeraldCount = 12 + this.getRandom().nextInt(6);
        for (int i = 0; i < emeraldCount; i++) {
            dropItemRand(new ItemStack(Items.EMERALD, 1));
        }
        int goldCount = 8 + this.getRandom().nextInt(5);
        for (int i = 0; i < goldCount; i++) {
            dropItemRand(new ItemStack(Items.GOLD_INGOT, 1));
        }
        int bonusCount = 3 + this.getRandom().nextInt(5);
        for (int i = 0; i < bonusCount; i++) {
            int roll = this.getRandom().nextInt(15);
            ItemStack is;
            switch (roll) {
                case 1 -> dropItemRand(new ItemStack(Items.EMERALD, 1));
                case 2 -> dropItemRand(new ItemStack(Items.EMERALD_BLOCK, 1));
                case 3 -> {
                    is = dropItemRand(new ItemStack(ModItems.EMERALD_SWORD.get()));
                    if (this.getRandom().nextInt(6) == 1) enchantItem(is, Enchantments.SHARPNESS, 1 + this.getRandom().nextInt(5));
                    if (this.getRandom().nextInt(6) == 1) enchantItem(is, Enchantments.SMITE, 1 + this.getRandom().nextInt(5));
                    if (this.getRandom().nextInt(6) == 1) enchantItem(is, Enchantments.BANE_OF_ARTHROPODS, 1 + this.getRandom().nextInt(5));
                    if (this.getRandom().nextInt(6) == 1) enchantItem(is, Enchantments.KNOCKBACK, 1 + this.getRandom().nextInt(5));
                    if (this.getRandom().nextInt(2) == 1) enchantItem(is, Enchantments.UNBREAKING, 2 + this.getRandom().nextInt(4));
                    if (this.getRandom().nextInt(6) == 1) enchantItem(is, Enchantments.FIRE_ASPECT, 1 + this.getRandom().nextInt(5));
                }
                case 4 -> {
                    is = dropItemRand(new ItemStack(ModItems.EMERALD_SHOVEL.get()));
                    if (this.getRandom().nextInt(2) == 1) enchantItem(is, Enchantments.UNBREAKING, 2 + this.getRandom().nextInt(4));
                    if (this.getRandom().nextInt(6) == 1) enchantItem(is, Enchantments.EFFICIENCY, 1 + this.getRandom().nextInt(5));
                }
                case 5 -> {
                    is = dropItemRand(new ItemStack(ModItems.EMERALD_PICKAXE.get()));
                    if (this.getRandom().nextInt(2) == 1) enchantItem(is, Enchantments.UNBREAKING, 2 + this.getRandom().nextInt(4));
                    if (this.getRandom().nextInt(6) == 1) enchantItem(is, Enchantments.EFFICIENCY, 1 + this.getRandom().nextInt(5));
                    if (this.getRandom().nextInt(6) == 1) enchantItem(is, Enchantments.SILK_TOUCH, 1);
                }
                case 6 -> {
                    is = dropItemRand(new ItemStack(ModItems.EMERALD_AXE.get()));
                    if (this.getRandom().nextInt(2) == 1) enchantItem(is, Enchantments.UNBREAKING, 2 + this.getRandom().nextInt(4));
                    if (this.getRandom().nextInt(6) == 1) enchantItem(is, Enchantments.EFFICIENCY, 1 + this.getRandom().nextInt(5));
                }
                case 7 -> {
                    is = dropItemRand(new ItemStack(ModItems.EMERALD_HOE.get()));
                    if (this.getRandom().nextInt(2) == 1) enchantItem(is, Enchantments.UNBREAKING, 2 + this.getRandom().nextInt(4));
                    if (this.getRandom().nextInt(6) == 1) enchantItem(is, Enchantments.EFFICIENCY, 1 + this.getRandom().nextInt(5));
                }
                // TODO: Cases 8-11 drop Emerald armor (helmet, chestplate, leggings, boots)
                // with Protection, Projectile Protection, Fire Protection, Blast Protection,
                // Unbreaking, Respiration, Aqua Affinity, Feather Falling enchants.
                // Add when emerald armor items are registered in ModItems.
                default -> dropItemRand(new ItemStack(Items.EMERALD, 1));
            }
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (super.doHurtTarget(target)) {
            if (target instanceof LivingEntity living) {
                if (this.getRandom().nextInt(3) == 0) {
                    living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 0));
                }
                double ks = 1.5;
                double inair = 0.15;
                float angle = (float) Math.atan2(target.getZ() - this.getZ(), target.getX() - this.getX());
                if (target.isRemoved() || target instanceof Player) {
                    inair *= 2.0;
                }
                target.push(Math.cos(angle) * ks, inair, Math.sin(angle) * ks);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.hurtTimer > 0) {
            return false;
        }
        this.hurtTimer = 30;
        return super.hurt(source, amount);
    }

    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;
        super.customServerAiStep();

        if (this.hurtTimer > 0) {
            --this.hurtTimer;
        }

        if (this.getRandom().nextInt(5) == 0) {
            LivingEntity target = findSomethingToAttack();
            if (target != null) {
                this.lookAt(target, 10.0f, 10.0f);
                double distSq = this.distanceToSqr(target);
                float attackRange = 6.0f + target.getBbWidth() / 2.0f;
                if (distSq < attackRange * attackRange) {
                    this.setAttacking(1);
                    if (this.getRandom().nextInt(3) == 0 || this.getRandom().nextInt(4) == 1) {
                        this.doHurtTarget(target);
                    }
                } else {
                    this.getNavigation().moveTo(target, 1.25);
                }
                if (target instanceof LivingEntity) {
                    target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 5));
                }
            } else {
                this.setAttacking(0);
            }
        }

        if (this.getRandom().nextInt(75) == 1 && this.getHealth() < this.mygetMaxHealth()) {
            this.heal(1.0f);
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.isRemoved()) return;
        if (this.getRandom().nextInt(200) == 0) {
            this.heal(1.0f);
        }
    }

    private boolean isSuitableTarget(LivingEntity target) {
        if (target == null || target == this || !target.isAlive()) return false;
        if (!this.getSensing().hasLineOfSight(target)) return false;
        if (target instanceof Basilisk) return false;
        if (target instanceof Player player) {
            if (player.getAbilities().instabuild) return false;
        }
        return true;
    }

    private LivingEntity findSomethingToAttack() {
        AABB searchBox = this.getBoundingBox().inflate(24.0, 7.0, 24.0);
        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class, searchBox);
        entities.sort(this.targetSorter);
        for (LivingEntity entity : entities) {
            if (isSuitableTarget(entity)) return entity;
        }
        return null;
    }

    public final int getAttacking() {
        return this.entityData.get(DATA_ATTACKING);
    }

    public final void setAttacking(int value) {
        this.entityData.set(DATA_ATTACKING, value);
    }
}
