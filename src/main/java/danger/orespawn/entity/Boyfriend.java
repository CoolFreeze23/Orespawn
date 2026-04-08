package danger.orespawn.entity;

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
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.OreSpawnMod;

public class Boyfriend extends TamableAnimal {
    private static final EntityDataAccessor<Integer> DATA_SKIN =
            SynchedEntityData.defineId(Boyfriend.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_VOICE =
            SynchedEntityData.defineId(Boyfriend.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_VOICE_ENABLE =
            SynchedEntityData.defineId(Boyfriend.class, EntityDataSerializers.INT);

    private static final int MAX_HEALTH = 80;
    private static final float MOVE_SPEED = 0.3f;
    private static final int MAX_SKINS = 28;
    private int autoHeal = 200;
    private int forceSync = 50;
    public int whichGuy;
    private int voice;
    private int voiceEnable = 1;

    public Boyfriend(EntityType<? extends Boyfriend> type, Level level) {
        super(type, level);
        this.whichGuy = this.random.nextInt(MAX_SKINS);
        this.voice = this.random.nextInt(10);
        this.setTameSkin(this.whichGuy);
        this.setOrderedToSit(false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FollowOwnerGoal(this, 1.4, 12.0f, 1.5f));
        this.goalSelector.addGoal(2, new TemptGoal(this, 1.25, Ingredient.of(Items.DIAMOND), false));
        this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.25, true));
        this.goalSelector.addGoal(5, new FloatGoal(this));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 0.75));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Monster.class, true));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return TamableAnimal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.MOVEMENT_SPEED, MOVE_SPEED)
                .add(Attributes.ATTACK_DAMAGE, 8.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_SKIN, 0);
        builder.define(DATA_VOICE, 0);
        builder.define(DATA_VOICE_ENABLE, 1);
    }

    public int getTameSkin() {
        return this.entityData.get(DATA_SKIN);
    }

    public void setTameSkin(int skinIndex) {
        this.entityData.set(DATA_SKIN, skinIndex);
        this.whichGuy = skinIndex;
    }

    public int getVoice() {
        return this.entityData.get(DATA_VOICE);
    }

    @Override
    public boolean removeWhenFarAway(double dist) {
        return false;
    }

    @Override
    public void tick() {
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(MOVE_SPEED);
        super.tick();
    }

    @Override
    public void aiStep() {
        super.aiStep();
        --this.autoHeal;
        if (this.autoHeal <= 0) {
            if (this.getHealth() < this.getMaxHealth()) {
                this.heal(1.0f);
            }
            this.autoHeal = 150;
        }
        --this.forceSync;
        if (this.forceSync <= 0) {
            this.forceSync = 20;
            if (!this.level().isClientSide) {
                this.entityData.set(DATA_VOICE, this.voice);
                this.entityData.set(DATA_VOICE_ENABLE, this.voiceEnable);
                this.setOrderedToSit(this.isOrderedToSit());
            } else {
                this.voice = this.getVoice();
                this.voiceEnable = this.entityData.get(DATA_VOICE_ENABLE);
            }
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (stack.is(Items.DIAMOND) && this.distanceToSqr(player) < 16.0) {
            if (!this.isTame()) {
                if (!this.level().isClientSide) {
                    if (this.random.nextInt(3) == 0) {
                        this.tame(player);
                        this.level().broadcastEntityEvent(this, (byte) 7);
                        this.heal(MAX_HEALTH - this.getHealth());
                    } else {
                        this.level().broadcastEntityEvent(this, (byte) 6);
                    }
                }
            } else if (this.isOwnedBy(player)) {
                if (!this.level().isClientSide) {
                    this.level().broadcastEntityEvent(this, (byte) 7);
                }
                this.heal(MAX_HEALTH - this.getHealth());
            }
            if (!player.getAbilities().instabuild) stack.shrink(1);
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        if (stack.is(Items.DANDELION) && this.distanceToSqr(player) < 16.0) {
            if (!this.level().isClientSide) {
                this.whichGuy = (this.whichGuy + 1) % MAX_SKINS;
                this.setTameSkin(this.whichGuy);
                this.level().broadcastEntityEvent(this, (byte) 7);
            }
            if (!player.getAbilities().instabuild) stack.shrink(1);
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        if (this.isTame() && this.isOwnedBy(player) && this.distanceToSqr(player) < 16.0) {
            if (!stack.isEmpty()) {
                FoodProperties food = stack.getFoodProperties(this);
                if (food != null) {
                    if (!this.level().isClientSide && this.getHealth() < this.getMaxHealth()) {
                        this.heal(food.nutrition() * 5);
                        this.level().broadcastEntityEvent(this, (byte) 7);
                    }
                    if (!player.getAbilities().instabuild) stack.shrink(1);
                    return InteractionResult.sidedSuccess(this.level().isClientSide);
                }

                if (!this.level().isClientSide) {
                    if (stack.getItem() instanceof ArmorItem armorItem) {
                        EquipmentSlot slot = armorItem.getEquipmentSlot();
                        ItemStack existing = this.getItemBySlot(slot);
                        this.setItemSlot(slot, stack.copy());
                        if (!existing.isEmpty()) {
                            player.setItemInHand(hand, existing);
                        } else {
                            stack.shrink(1);
                        }
                    } else {
                        ItemStack currentWeapon = this.getMainHandItem();
                        this.setItemSlot(EquipmentSlot.MAINHAND, stack.copy());
                        if (!currentWeapon.isEmpty()) {
                            player.setItemInHand(hand, currentWeapon);
                        } else {
                            stack.shrink(1);
                        }
                    }
                    this.level().broadcastEntityEvent(this, (byte) 7);
                }
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }

            if (stack.isEmpty()) {
                if (!this.level().isClientSide) {
                    ItemStack toReturn = findEquippedItem();
                    if (toReturn != null) {
                        player.setItemInHand(hand, toReturn);
                        this.level().broadcastEntityEvent(this, (byte) 6);
                    } else {
                        this.setOrderedToSit(!this.isOrderedToSit());
                        this.setInSittingPose(this.isOrderedToSit());
                    }
                }
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }
        }
        return super.mobInteract(player, hand);
    }

    @Nullable
    private ItemStack findEquippedItem() {
        EquipmentSlot[] slots = { EquipmentSlot.MAINHAND, EquipmentSlot.HEAD, EquipmentSlot.CHEST,
                EquipmentSlot.LEGS, EquipmentSlot.FEET };
        for (EquipmentSlot slot : slots) {
            ItemStack item = this.getItemBySlot(slot);
            if (!item.isEmpty()) {
                this.setItemSlot(slot, ItemStack.EMPTY);
                return item;
            }
        }
        return null;
    }

    @Override
    public boolean causeFallDamage(float dist, float mult, DamageSource source) {
        float damage = dist - 3.0f;
        if (damage > 0.0f) {
            if (damage > 3.0f) damage = 3.0f;
            this.hurt(this.damageSources().fall(), damage);
        }
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        float capped = Math.min(amount, 10.0f);
        if (source.getMsgId().equals("cactus")) return false;
        return super.hurt(source, capped);
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        if (this.isOrderedToSit() || this.voiceEnable == 0) return null;
        if (this.getRandom().nextInt(11) == 1) {
            if (this.isTame()) {
                if (this.getHealth() < this.getMaxHealth()) {
                    return SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "b_hurt"));
                }
                return SoundEvent.createVariableRangeEvent(
                        ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "b_happy"));
            }
        }
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        if (this.voiceEnable == 0) return null;
        return SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "b_ow"));
    }

    @Override
    protected SoundEvent getDeathSound() {
        return this.isTame()
                ? SoundEvent.createVariableRangeEvent(
                        ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "b_death_boyfriend"))
                : SoundEvent.createVariableRangeEvent(
                        ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "b_death_single"));
    }

    @Override
    protected float getSoundVolume() {
        return 0.3f;
    }

    @Override
    public float getVoicePitch() {
        return (float) (this.voice - 5) * 0.02f + 1.0f;
    }

    // BOYFRIEND_BRO_MODE: when enabled, the boyfriend refuses to attack other tamed
    // mobs that share the same owner — preventing friendly-fire between the player's
    // pets. This overrides the vanilla TamableAnimal targeting check.
    @Override
    public boolean wantsToAttack(LivingEntity target, LivingEntity owner) {
        if (OreSpawnConfig.BOYFRIEND_BRO_MODE.get() && target instanceof TamableAnimal tamable) {
            if (tamable.isTame() && tamable.getOwner() == owner) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.DIAMOND);
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return null;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("GuyType", this.getTameSkin());
        tag.putInt("GuyVoice", this.voice);
        tag.putInt("GuyVoiceEnable", this.voiceEnable);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.whichGuy = tag.getInt("GuyType");
        this.setTameSkin(this.whichGuy);
        this.voice = tag.getInt("GuyVoice");
        this.voiceEnable = tag.getInt("GuyVoiceEnable");
    }
}
