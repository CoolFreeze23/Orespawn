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
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
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
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
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
import danger.orespawn.util.SeasonalDates;

public class Girlfriend extends TamableAnimal implements RangedAttackMob {
    // OPT-011: cached SoundEvents — identical createVariableRangeEvent ids,
    // allocated once per class instead of on every sound query.
    private static final SoundEvent SND_O_FIGHT = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "o_fight"));
    private static final SoundEvent SND_O_TAUNT = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "o_taunt"));
    private static final SoundEvent SND_O_WOOHOO = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "o_woohoo"));
    private static final SoundEvent SND_O_HURT = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "o_hurt"));
    private static final SoundEvent SND_O_HAPPY = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "o_happy"));
    private static final SoundEvent SND_O_OW = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "o_ow"));
    private static final SoundEvent SND_O_DEATH_GIRLFRIEND = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "o_death_girlfriend"));
    private static final SoundEvent SND_O_DEATH_SINGLE = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "o_death_single"));
    private static final EntityDataAccessor<Integer> DATA_SKIN =
            SynchedEntityData.defineId(Girlfriend.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_VOICE =
            SynchedEntityData.defineId(Girlfriend.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_VOICE_ENABLE =
            SynchedEntityData.defineId(Girlfriend.class, EntityDataSerializers.INT);
    // orig Girlfriend.java:265-266 — feelingBetter (DataWatcher slot 25);
    // synched so the renderer can pick the valentine texture/scale client-side
    private static final EntityDataAccessor<Integer> DATA_FEELING_BETTER =
            SynchedEntityData.defineId(Girlfriend.class, EntityDataSerializers.INT);

    private static final int MAX_HEALTH = 80;
    // orig Girlfriend.java:569-574 (mygetMaxHealth) — 800 while valentine-angry
    private static final int VALENTINE_MAX_HEALTH = 800;
    private static final float MOVE_SPEED = 0.3f;
    private static final int MAX_SKINS = 41;
    private int autoHeal = 200;
    private int forceSync = 50;
    public int whichGirl;
    private int voice;
    private int voiceEnable = 1;
    // orig Girlfriend.java:272-325 weapon-melee state
    private int meleeCooldown = 0;
    private int fightSoundTicker = 0;
    private int tauntSoundTicker = 0;
    private int hadTarget = 0;
    // orig Girlfriend.java:71 — public so nearby girlfriends can copy each
    // other's dance state (orig MyEntityAIDance.java:127-137) and the ambient
    // voice can hush mid-dance (orig Girlfriend.java:858-860). No initializer
    // on purpose: registerGoals() assigns it during the Mob super-ctor, and a
    // field initializer would run afterwards and null it back out.
    public MyEntityAIDance Dance;

    public Girlfriend(EntityType<? extends Girlfriend> type, Level level) {
        super(type, level);
        // OPT-009: constant speed - assert the attribute base once here instead
        // of re-writing it every tick (same value the removed per-tick call set).
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(MOVE_SPEED);
        this.whichGirl = this.random.nextInt(MAX_SKINS);
        this.voice = this.random.nextInt(10);
        this.setTameSkin(this.whichGirl);
        this.setOrderedToSit(false);
        // Needed for the OpenDoorGoal (orig Girlfriend.java:159) to path
        // through doors — same idiom as Boyfriend (port Boyfriend.java:95-98).
        if (this.getNavigation() instanceof GroundPathNavigation groundNav) {
            groundNav.setCanOpenDoors(true);
        }
        // orig Girlfriend.java:142-144 — 2.5x8.0 giant on Feb 14 (until cured);
        // orig :198-200 (applyEntityAttributes) reads mygetMaxHealth → 800
        if (SeasonalDates.isValentines()) {
            this.applyValentineState();
            this.setHealth(this.getMaxHealth());
        }
    }

    /** True while the Feb-14 "giant angry girlfriend" mode is active. */
    public boolean isValentineAngry() {
        return SeasonalDates.isValentines() && this.entityData.get(DATA_FEELING_BETTER) == 0;
    }

    public int getFeelingBetter() {
        return this.entityData.get(DATA_FEELING_BETTER);
    }

    /**
     * orig Girlfriend.java:142-144,198-200,569-574 — size and max health track
     * the valentine-angry state (2.5x8.0 / 800 HP, else 0.5x1.6 / 80 HP).
     */
    private void applyValentineState() {
        this.refreshDimensions();
        int max = this.isValentineAngry() ? VALENTINE_MAX_HEALTH : MAX_HEALTH;
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(max);
        if (this.getHealth() > max) {
            this.setHealth(max);
        }
    }

    /** orig Girlfriend.java:142-144 — 2.5x8.0 while valentine-angry, else 0.5x1.6. */
    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        return this.isValentineAngry()
                ? EntityDimensions.scalable(2.5f, 8.0f)
                : super.getDefaultDimensions(pose);
    }

    /**
     * orig Girlfriend.java:600-607 — the client's force_sync loop shrank her
     * (func_70105_a(0.5, 1.6)) the tick it saw feelingBetter turn nonzero.
     * 1.21.1 equivalent: refresh dimensions whenever the synched flag changes,
     * so the cured giant shrinks on clients too (the server side already
     * refreshes in applyValentineState).
     */
    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
        super.onSyncedDataUpdated(accessor);
        if (DATA_FEELING_BETTER.equals(accessor)) {
            this.refreshDimensions();
        }
    }

    @Override
    protected void registerGoals() {
        // orig MyEntityAIFollowOwner.java:48 — Girlfriends never follow their
        // owner on Feb 14 (the giant-angry mode overrides companionship)
        this.goalSelector.addGoal(1, new FollowOwnerGoal(this, 1.4, 12.0f, 1.5f) {
            @Override
            public boolean canUse() {
                return !SeasonalDates.isValentines() && super.canUse();
            }
        });
        this.goalSelector.addGoal(2, new TemptGoal(this, 1.25, Ingredient.of(Items.POPPY), false));
        // orig Girlfriend.java:151-152 — MyEntityAIDance at priority 3, held in
        // the public Dance field (see its comment for why other girlfriends and
        // the ambient-voice check need the reference).
        this.Dance = new MyEntityAIDance(this);
        this.goalSelector.addGoal(3, this.Dance);
        // orig Girlfriend.java:153 — EntityAIArrowAttack(this, 1.25, 20t, 10.0f);
        // melee happens separately in customServerAiStep (orig func_70629_bd).
        this.goalSelector.addGoal(4, new RangedAttackGoal(this, 1.25, 20, 10.0f));
        this.goalSelector.addGoal(5, new FloatGoal(this));
        this.goalSelector.addGoal(6, new PanicGoal(this, 1.5)); // orig :155 — EntityAIPanic(1.5)
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 0.75));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(10, new OpenDoorGoal(this, true)); // orig :159
        // orig :160 — EntityAIMoveIndoors(11); documented 1.21.1 behavioral
        // match (roofed shelter at night/rain), see MoveIndoorsGoal.
        this.goalSelector.addGoal(11, new danger.orespawn.entity.ai.MoveIndoorsGoal(this));

        // orig Girlfriend.java:161-162 — MyValentineTarget(Player) prio 1 and
        // MyValentineTarget(Boyfriend) prio 2; active only while valentine-angry
        // (MyValentineTarget.java:47-56), fixed 16-block radius
        this.targetSelector.addGoal(1, new ValentineTargetGoal<>(this, Player.class));
        this.targetSelector.addGoal(2, new ValentineTargetGoal<>(this, Boyfriend.class));
        this.targetSelector.addGoal(3, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(4, new OwnerHurtTargetGoal(this));
        // orig Girlfriend.java:166-168 — the MyEntityAINearestAttackableTarget(EntityLiving.class, 15.0f, IMob selector)
        // task is registered only when PlayNicely == 0 at construction (the :163-165 EntityCreeper task has no port
        // counterpart; the :161-162 MyValentineTarget tasks above are ungated in orig); the port registers this goal
        // always and reads the flag live in its canUse, as the Jealousy goals below do — it never starts while
        // PlayNicely is on (ENT-S-115).
        // orig Girlfriend.java:167 IMob.mobSelector → Mob.class + instanceof Enemy; 10 / false are the 3-arg constructor's own randomInterval / mustReach (ENT-S-124, IMob convention)
        // — and orig's Mothra by name: an IMob in 1.7.10 (orig Mothra.java:52), an EntityButterfly with no Enemy here; the task's own
        // rules (orig MyEntityAITarget.java:88-128) follow in isMonsterPrey (ENT-S-128)
        this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, Mob.class, 10, true, false,
                e -> (e instanceof Enemy || e instanceof Mothra) && this.isMonsterPrey(e)) {
            @Override
            public boolean canUse() {
                if (OreSpawnConfig.PLAY_NICELY.get()) return false; // orig Girlfriend.java:166-168 (ENT-S-115)
                return super.canUse();
            }
        });
        // orig Girlfriend.java:169-174 — Jealousy(Girlfriend.class, 6.0f, 5, true)
        // @4 and (3.0f, 15, true) @5: she hunts UNTAMED rival girlfriends near
        // her owner (a tamed rival is never targeted, orig MyEntityAIJealousy
        // .java:43-45); the original's PlayNicely == 0 gate is checked
        // dynamically inside the goal — same idiom as Boyfriend (port
        // Boyfriend.java:120-127).
        this.targetSelector.addGoal(4, new danger.orespawn.entity.ai.JealousyTargetGoal<>(this, Girlfriend.class, 6.0, 5));
        this.targetSelector.addGoal(5, new danger.orespawn.entity.ai.JealousyTargetGoal<>(this, Girlfriend.class, 3.0, 15));
    }

    /**
     * orig MyEntityAITarget.java:78-129 {@code isSuitableTarget}, the filter of the monster task registered at orig
     * Girlfriend.java:167 (MyEntityAINearestAttackableTarget.java:56 lists {@code EntityLiving.class} through
     * {@code IMob.mobSelector} — the goal's Mob + Enemy selector, ENT-S-124, with orig's IMob Mothra named beside it —
     * then asks this per candidate), in the original's order: a tamed task owner takes no tamed pet (:88-91; its
     * owner, :92-94, is refused by vanilla {@code TamableAnimal.canAttack} inside the goal's {@code TargetingConditions});
     * a player only on Valentine's (:96-98 — no {@code Mob} candidate is a player, and the Valentine rule is MOD-036's
     * {@code ValentineTargetGoal}); EntityPigZombie refused (:99-101); EntityEnderman refused (:102-104); Mothra taken
     * granted by orig ahead of its sight step (:105-107 before :108 — here the goal's own line of sight, mustSee, still follows this predicate, so only a Mothra in sight is taken; deferred, ENT-S-128); EntityCreeper
     * (:111-113) and EntityGhast (:114-116) taken ahead of the nearbyOnly path check (:117-127 — the goal's
     * {@code mustReach}, false at this site); everything else taken (:128). The Ghast is still refused by the engine's
     * {@code Mob.canAttackType} through the goal's conditions (the ENT-S-124 disclosure, ENT-S-127). ENT-S-128.
     */
    private boolean isMonsterPrey(LivingEntity candidate) {
        if (this.isTame() && candidate instanceof TamableAnimal pet && pet.isTame()) return false; // orig MyEntityAITarget.java:88-91 (ENT-S-128)
        if (candidate instanceof ZombifiedPiglin) return false; // orig MyEntityAITarget.java:99-101 EntityPigZombie (ENT-S-128)
        if (candidate instanceof EnderMan) return false;        // orig MyEntityAITarget.java:102-104 EntityEnderman (ENT-S-128)
        if (candidate instanceof Mothra) return true;           // orig MyEntityAITarget.java:105-107 — granted ahead of orig's sight step :108; the goal's own line of sight (mustSee, ENT-S-124) still follows this predicate, so only a Mothra in sight is taken (deferred, ENT-S-128)
        if (candidate instanceof Creeper) return true;          // orig MyEntityAITarget.java:111-113 EntityCreeper (ENT-S-128)
        if (candidate instanceof Ghast) return true;            // orig MyEntityAITarget.java:114-116 EntityGhast — the engine's Mob.canAttackType still refuses it (ENT-S-127) (ENT-S-128)
        return true;                                            // orig MyEntityAITarget.java:128
    }

    /**
     * Port of orig MyValentineTarget.java:47-70 — a nearest-attackable-target
     * goal that only runs on Feb 14 while the Girlfriend has not been cured
     * (feelingBetter == 0). targetChance 0 = no dice roll; 16-block radius
     * (orig ctor par3, MyValentineTarget.java:26,39); owner excluded per the
     * 1.7.10 EntityAITarget tameable-owner check.
     */
    private static class ValentineTargetGoal<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {
        private final Girlfriend girlfriend;

        ValentineTargetGoal(Girlfriend girlfriend, Class<T> targetType) {
            // orig MyEntityAITarget.java:88-94 — a tamed task owner never
            // targets her owner or other tamed pets
            super(girlfriend, targetType, 0, true, true,
                    candidate -> candidate != girlfriend.getOwner()
                            && !(girlfriend.isTame()
                                    && candidate instanceof TamableAnimal pet && pet.isTame()));
            this.girlfriend = girlfriend;
        }

        @Override
        protected double getFollowDistance() {
            return 16.0;
        }

        @Override
        public boolean canUse() {
            return this.girlfriend.isValentineAngry() && super.canUse();
        }
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
        builder.define(DATA_FEELING_BETTER, 0);
    }

    public int getTameSkin() {
        return this.entityData.get(DATA_SKIN);
    }

    public void setTameSkin(int val) {
        this.entityData.set(DATA_SKIN, val);
        this.whichGirl = val;
    }

    public int getVoice() {
        return this.entityData.get(DATA_VOICE);
    }

    @Override
    public boolean removeWhenFarAway(double dist) {
        return false;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        --this.autoHeal;
        if (this.autoHeal <= 0) {
            if (this.getHealth() < this.getMaxHealth()) {
                this.heal(1.0f);
            }
            this.autoHeal = 100;
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
     * orig Girlfriend.java:272-325 (func_70629_bd) — held-weapon melee,
     * identical to the Boyfriend's (orig Boyfriend.java:239-289) except for the
     * "o_" voice lines and an extra 1-in-200 full target reset (orig :282-284).
     * See {@link Boyfriend#customServerAiStep} for the 1.7.10
     * attackTargetEntityWithCurrentItem → {@code Mob.doHurtTarget} mapping.
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
        if (this.random.nextInt(200) == 1) {
            this.setTarget(null);
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
                                        SND_O_FIGHT,
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
                                    SND_O_TAUNT,
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
                                SND_O_WOOHOO,
                                this.getSoundSource(), 0.4f, this.getVoicePitch());
                    }
                }
            }
        }
    }

    /**
     * orig Girlfriend.java:975-1008 (attackEntityWithRangedAttack) — identical
     * to the Boyfriend's (orig Boyfriend.java:874-907) except the shoe thrown
     * is a random type 2-5 instead of always 6. UltimateArrow: velocity 2.0,
     * inaccuracy 10.0, 1-in-4 crit, creative-only pickup, 1 bow durability;
     * Punch/Flame ride along via the firing-weapon stack. Shoes: velocity 1.8,
     * inaccuracy 4.0.
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
            Shoes shoes = new Shoes(this.level(), this, 2 + this.random.nextInt(4));
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

        if (stack.is(Items.POPPY) && this.distanceToSqr(player) < 16.0) {
            if (!this.isTame()) {
                if (!this.level().isClientSide) {
                    if (this.random.nextInt(3) == 0) {
                        this.tame(player);
                        this.level().broadcastEntityEvent(this, (byte) 7);
                        // orig Girlfriend.java:630 — heal to mygetMaxHealth(),
                        // which is 800 mid-valentine; the MAX_HEALTH constant
                        // under-healed (and negative-healed) the giant
                        this.heal(this.getMaxHealth() - this.getHealth());
                    } else {
                        this.level().broadcastEntityEvent(this, (byte) 6);
                    }
                }
            } else if (this.isOwnedBy(player)) {
                if (!this.level().isClientSide) {
                    this.level().broadcastEntityEvent(this, (byte) 7);
                }
                // orig Girlfriend.java:641-643 — heal to mygetMaxHealth() (800
                // mid-valentine), not the flat 80 constant
                this.heal(this.getMaxHealth() - this.getHealth());
            }
            if (!player.getAbilities().instabuild) stack.shrink(1);
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        if (stack.is(Items.DANDELION) && this.distanceToSqr(player) < 16.0) {
            if (!this.level().isClientSide) {
                this.whichGirl = (this.whichGirl + 1) % MAX_SKINS;
                this.setTameSkin(this.whichGirl);
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
        // orig Girlfriend.java:1078-1080 — a valentine giant can't suffocate
        // in walls (she is 8 blocks tall and clips ceilings constantly)
        if (source.getMsgId().equals("inWall") && SeasonalDates.isValentines()) {
            return false;
        }
        // orig Girlfriend.java:1081-1094 — hitting the valentine giant with the
        // Rose Sword: 1-in-4 chance to cure her (back to normal size/health,
        // target cleared, 10-19 Love drops); otherwise she drops 1 Love
        if (SeasonalDates.isValentines() && !this.level().isClientSide
                && this.getFeelingBetter() == 0
                && source.getEntity() instanceof Player attacker
                && attacker.getMainHandItem().is(ModItems.ROSE_SWORD.get())) {
            if (this.level().random.nextInt(4) == 1) {
                this.entityData.set(DATA_FEELING_BETTER, 1);
                this.setTarget(null);
                this.applyValentineState();
                int moreLove = this.level().random.nextInt(10);
                for (int i = 0; i < 10 + moreLove; ++i) {
                    this.dropItemRand(new ItemStack(ModItems.HEART.get()));
                }
            } else {
                this.dropItemRand(new ItemStack(ModItems.HEART.get()));
            }
        }
        return super.hurt(source, capped);
    }

    /** orig Girlfriend.java:916-919 (dropItemRand) — drop at y+1 with +/-0-3 x/z scatter. */
    private void dropItemRand(ItemStack stack) {
        net.minecraft.world.entity.item.ItemEntity drop = new net.minecraft.world.entity.item.ItemEntity(
                this.level(),
                this.getX() + this.random.nextInt(4) - this.random.nextInt(4),
                this.getY() + 1.0,
                this.getZ() + this.random.nextInt(4) - this.random.nextInt(4),
                stack);
        this.level().addFreshEntity(drop);
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        if (this.isOrderedToSit() || this.voiceEnable == 0) return null;
        // orig Girlfriend.java:858-860 — no ambient voice while dancing
        if (this.Dance != null && this.Dance.is_dancing != 0) return null;
        if (this.getRandom().nextInt(11) == 1) {
            if (this.isTame()) {
                // orig Girlfriend.java:886-889 — hurt voice also while valentine-angry
                if (this.getHealth() < this.getMaxHealth() || this.isValentineAngry()) {
                    return SND_O_HURT;
                }
                return SND_O_HAPPY;
            }
        }
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        if (this.voiceEnable == 0) return null;
        return SND_O_OW;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return this.isTame()
                ? SND_O_DEATH_GIRLFRIEND
                : SND_O_DEATH_SINGLE;
    }

    @Override
    protected float getSoundVolume() {
        return 0.3f;
    }

    @Override
    public float getVoicePitch() {
        return (float) (this.voice - 5) * 0.02f + 1.0f;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.POPPY);
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return null;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("GirlType", this.getTameSkin());
        tag.putInt("GirlVoice", this.voice);
        tag.putInt("GirlVoiceEnable", this.voiceEnable);
        // orig Girlfriend.java:236-241/265-266 — feelingBetter persists
        tag.putInt("feelingBetter", this.getFeelingBetter());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.whichGirl = tag.getInt("GirlType");
        this.setTameSkin(this.whichGirl);
        this.voice = tag.getInt("GirlVoice");
        this.voiceEnable = tag.getInt("GirlVoiceEnable");
        // orig Girlfriend.java:265-269 — restore feelingBetter and shrink an
        // already-cured girlfriend back to normal size
        this.entityData.set(DATA_FEELING_BETTER, tag.getInt("feelingBetter"));
        if (SeasonalDates.isValentines()) {
            this.applyValentineState();
        }
    }

    /** orig Girlfriend.java:1100-1115 — "Girlfriend" spawner bypass, else the vanilla creature rules. */
    @Override
    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level,
                                   net.minecraft.world.entity.MobSpawnType spawnType) {
        if (OriginalSpawnGates.nearOwnSpawner(this, level)) return true;
        return super.checkSpawnRules(level, spawnType);
    }
}
