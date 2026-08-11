package danger.orespawn.entity;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
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
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import danger.orespawn.ModItems;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.OreSpawnMod;

public class Boyfriend extends TamableAnimal implements RangedAttackMob {
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
    // orig Boyfriend.java:239-289 weapon-melee state
    private int meleeCooldown = 0;
    private int fightSoundTicker = 0;
    private int tauntSoundTicker = 0;
    private int hadTarget = 0;

    public Boyfriend(EntityType<? extends Boyfriend> type, Level level) {
        super(type, level);
        this.whichGuy = this.random.nextInt(MAX_SKINS);
        this.voice = this.random.nextInt(10);
        this.setTameSkin(this.whichGuy);
        this.setOrderedToSit(false);
        // Needed for the OpenDoorGoal (orig Boyfriend.java:135) to path through doors.
        if (this.getNavigation() instanceof GroundPathNavigation groundNav) {
            groundNav.setCanOpenDoors(true);
        }
    }

    @Override
    protected void registerGoals() {
        // orig Boyfriend.java:127-148 — complete as of ENT-A-054 (E3).
        this.goalSelector.addGoal(1, new FollowOwnerGoal(this, 1.4, 12.0f, 1.5f));
        this.goalSelector.addGoal(2, new TemptGoal(this, 1.25,
                Ingredient.of(Items.COOKED_BEEF), false)); // orig :128 — cooked beef
        // orig :129 — EntityAIArrowAttack(this, 1.25, 20t, 10.0f); melee happens
        // separately in customServerAiStep (orig func_70629_bd).
        this.goalSelector.addGoal(4, new RangedAttackGoal(this, 1.25, 20, 10.0f));
        this.goalSelector.addGoal(5, new FloatGoal(this));
        this.goalSelector.addGoal(6, new PanicGoal(this, 1.5)); // orig :131
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 0.75));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(10, new OpenDoorGoal(this, true)); // orig :135
        // orig :136 — EntityAIMoveIndoors(11); documented 1.21.1 behavioral
        // match (roofed-shelter at night/rain), see MoveIndoorsGoal.
        this.goalSelector.addGoal(11, new danger.orespawn.entity.ai.MoveIndoorsGoal(this));

        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Monster.class, true));
        // orig :146-147 — Jealousy(Boyfriend.class, 6.0f, 5, true) @4 and
        // (3.0f, 15, true) @5; a tamed rival is never targeted; PlayNicely
        // gates dynamically inside the goal (ENT-A-054).
        this.targetSelector.addGoal(4, new danger.orespawn.entity.ai.JealousyTargetGoal<>(this, Boyfriend.class, 6.0, 5));
        this.targetSelector.addGoal(5, new danger.orespawn.entity.ai.JealousyTargetGoal<>(this, Boyfriend.class, 3.0, 15));
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

    /**
     * orig Boyfriend.java:239-289 (func_70629_bd) — held-weapon melee.
     * Attacks with the held item every 25 ticks inside 4 blocks (10 with Big
     * Bertha), plays "b_fight" every 3rd swing and "b_taunt" every 300 ticks
     * while closing in (unless holding the Ultimate Bow), and celebrates with
     * "b_woohoo" once the target is gone.
     *
     * <p>1.7.10's attackTargetEntityWithCurrentItem (orig :913-952) manually
     * summed the attack attribute, held-item enchant damage, knockback enchant
     * + sprint knockback, and Fire Aspect ignite. In 1.21.1 that exact
     * player-style pipeline is what {@code Mob.doHurtTarget} runs (attribute +
     * data-driven enchant damage/knockback/post-attack effects), so the port
     * delegates to it.</p>
     */
    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        ItemStack stack = this.getMainHandItem();
        LivingEntity victim = this.getTarget();
        if (OreSpawnConfig.PLAY_NICELY.get()) {
            victim = null;
        }
        if (this.random.nextInt(100) == 1) {
            this.setLastHurtByMob(null);
        }
        if (!stack.isEmpty() && !this.isOrderedToSit()) {
            if (victim != null) {
                float dist = this.distanceTo(victim);
                if (dist < 4.0f || (stack.is(ModItems.BIG_BERTHA.get()) && dist < 10.0f)) {
                    --this.meleeCooldown;
                    if (this.meleeCooldown <= 0) {
                        this.meleeCooldown = 25;
                        this.swing(InteractionHand.MAIN_HAND);
                        this.doHurtTarget(victim);
                        --this.fightSoundTicker;
                        if (this.fightSoundTicker <= 0) {
                            if (this.voiceEnable != 0) {
                                this.level().playSound(null, this,
                                        SoundEvent.createVariableRangeEvent(
                                                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "b_fight")),
                                        this.getSoundSource(), 0.5f, this.getVoicePitch());
                            }
                            this.fightSoundTicker = 3;
                        }
                        this.hadTarget = 1;
                    }
                } else if (dist < 7.0f && !stack.is(ModItems.ULTIMATE_BOW.get())) {
                    --this.tauntSoundTicker;
                    if (this.tauntSoundTicker <= 0) {
                        if (this.voiceEnable != 0) {
                            this.level().playSound(null, this,
                                    SoundEvent.createVariableRangeEvent(
                                            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "b_taunt")),
                                    this.getSoundSource(), 0.5f, this.getVoicePitch());
                        }
                        this.tauntSoundTicker = 300;
                    }
                    this.getNavigation().moveTo(victim, 1.25);
                }
            } else {
                this.fightSoundTicker = 0;
                this.meleeCooldown = 0;
                if (this.hadTarget != 0) {
                    this.hadTarget = 0;
                    if (this.voiceEnable != 0) {
                        this.level().playSound(null, this,
                                SoundEvent.createVariableRangeEvent(
                                        ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "b_woohoo")),
                                this.getSoundSource(), 0.4f, this.getVoicePitch());
                    }
                }
            }
        }
    }

    /**
     * orig Boyfriend.java:874-907 (attackEntityWithRangedAttack) — fires an
     * UltimateArrow (velocity 2.0, inaccuracy 10.0, 1-in-4 crit, creative-only
     * pickup, 1 bow durability) when holding the Ultimate Bow, else throws a
     * type-6 Shoes projectile (velocity 1.8, inaccuracy 4.0). Punch knockback
     * and Flame ignite (orig :886-891) ride along automatically because the
     * held bow is passed as the arrow's firing weapon.
     */
    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        if (this.swinging) {
            return;
        }
        ItemStack stack = this.getMainHandItem();
        if (stack.is(ModItems.ULTIMATE_BOW.get())) {
            UltimateArrow arrow = new UltimateArrow(this.level(), this, stack);
            double dx = target.getX() - this.getX();
            double dy = target.getY(1.0 / 3.0) - arrow.getY();
            double dz = target.getZ() - this.getZ();
            double horiz = Math.sqrt(dx * dx + dz * dz);
            arrow.shoot(dx, dy + horiz * 0.2, dz, 2.0f, 10.0f);
            if (this.random.nextInt(4) == 1) {
                arrow.setCritArrow(true);
            }
            stack.hurtAndBreak(1, this, EquipmentSlot.MAINHAND);
            this.level().playSound(null, this, SoundEvents.ARROW_SHOOT, this.getSoundSource(),
                    1.0f, 1.0f / (this.random.nextFloat() * 0.4f + 1.2f) + 0.5f);
            arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
            this.level().addFreshEntity(arrow);
        } else {
            Shoes shoes = new Shoes(this.level(), this, 6);
            double dx = target.getX() - this.getX();
            double dy = target.getY() + target.getEyeHeight() - 1.1 - shoes.getY();
            double dz = target.getZ() - this.getZ();
            double horiz = Math.sqrt(dx * dx + dz * dz) * 0.2;
            shoes.shoot(dx, dy + horiz, dz, 1.8f, 4.0f);
            this.level().playSound(null, this, SoundEvents.ARROW_SHOOT, this.getSoundSource(),
                    0.75f, 1.0f / (this.random.nextFloat() * 0.4f + 0.8f));
            this.level().addFreshEntity(shoes);
        }
        this.swing(InteractionHand.MAIN_HAND);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (stack.is(Items.COOKED_BEEF) && this.distanceToSqr(player) < 16.0) {
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

    // (A Phase-10 wantsToAttack override that used BOYFRIEND_BRO_MODE as a
    // friendly-fire gate was removed here in Phase D3: the original's bro_mode
    // config (orig OreSpawnMain.java:1481) only gates VOICE behavior — silence
    // rolls and the bb_happy line, orig Boyfriend.java:772/804/818/825 — which
    // is ENT-A-058's scope. The invented combat meaning is archived in
    // MODERNIZATION_NOTES MOD-010.)

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.COOKED_BEEF);
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

    /** orig Boyfriend.java:978-993 — "Boyfriend" spawner bypass, else the vanilla creature rules. */
    @Override
    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level,
                                   net.minecraft.world.entity.MobSpawnType spawnType) {
        if (OriginalSpawnGates.nearOwnSpawner(this, level)) return true;
        return super.checkSpawnRules(level, spawnType);
    }
}
