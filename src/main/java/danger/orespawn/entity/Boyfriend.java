package danger.orespawn.entity;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
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
import danger.orespawn.item.ItemOreSpawnArmor;

public class Boyfriend extends TamableAnimal implements RangedAttackMob {
    // OPT-011: cached SoundEvents — identical createVariableRangeEvent ids,
    // allocated once per class instead of on every sound query.
    private static final SoundEvent SND_B_FIGHT = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "b_fight"));
    private static final SoundEvent SND_B_TAUNT = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "b_taunt"));
    private static final SoundEvent SND_B_WOOHOO = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "b_woohoo"));
    private static final SoundEvent SND_B_WATER = orespawnSound("b_water");
    private static final SoundEvent SND_B_THUNDER = orespawnSound("b_thunder");
    private static final SoundEvent SND_B_RAIN = orespawnSound("b_rain");
    private static final SoundEvent SND_B_DARK = orespawnSound("b_dark");
    private static final SoundEvent SND_B_HURT = orespawnSound("b_hurt");
    private static final SoundEvent SND_BB_HAPPY = orespawnSound("bb_happy");
    private static final SoundEvent SND_B_HAPPY = orespawnSound("b_happy");
    private static final SoundEvent SND_B_OW = orespawnSound("b_ow");
    private static final SoundEvent SND_B_DEATH_BOYFRIEND = orespawnSound("b_death_boyfriend");
    private static final SoundEvent SND_B_DEATH_SINGLE = orespawnSound("b_death_single");

    private static final EntityDataAccessor<Integer> DATA_SKIN =
            SynchedEntityData.defineId(Boyfriend.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_VOICE =
            SynchedEntityData.defineId(Boyfriend.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_VOICE_ENABLE =
            SynchedEntityData.defineId(Boyfriend.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_WET_SKIN =
            SynchedEntityData.defineId(Boyfriend.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_IS_PRINCE =
            SynchedEntityData.defineId(Boyfriend.class, EntityDataSerializers.INT);

    private static final int MAX_HEALTH = 80;
    private static final float MOVE_SPEED = 0.3f;
    private static final int MAX_SKINS = 28;
    private static final int MAX_WET_SKINS = 18; // orig Boyfriend.java:121 — 18 swimshorts textures
    private int autoHeal = 200;
    private int forceSync = 50;
    public int whichGuy;
    // orig Boyfriend.java:58-59 — wet-skin state; wetCount is simulated on
    // BOTH sides every tick (orig func_70636_d ran on both), so the renderer
    // reads it directly with no extra sync.
    public int whichWetGuy;
    public int wetCount = 0;
    // orig Boyfriend.java:69 — 1/2 = FrogPrince skins (set by a kissed Frog).
    private int isPrince = 0;
    private int voice;
    private int voiceEnable = 1;
    // orig Boyfriend.java:239-289 weapon-melee state
    private int meleeCooldown = 0;
    private int fightSoundTicker = 0;
    private int tauntSoundTicker = 0;
    private int hadTarget = 0;

    public Boyfriend(EntityType<? extends Boyfriend> type, Level level) {
        super(type, level);
        // OPT-009: constant speed - assert the attribute base once here instead
        // of re-writing it every tick (same value the removed per-tick call set).
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(MOVE_SPEED);
        this.whichGuy = this.random.nextInt(MAX_SKINS);
        this.whichWetGuy = this.random.nextInt(MAX_WET_SKINS); // orig Boyfriend.java:121,157
        this.voice = this.random.nextInt(10);
        this.setTameSkin(this.whichGuy);
        this.setWetTameSkin(this.whichWetGuy);
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
        // orig Boyfriend.java:140-142 — the MyEntityAINearestAttackableTarget(EntityLiving.class, 15.0f, IMob selector)
        // task is registered only when PlayNicely == 0 at construction (the :137-139 EntityCreeper task has no port
        // counterpart); the port registers this goal always and reads the flag live in its canUse, as the Jealousy
        // goals below do — it never starts while PlayNicely is on (ENT-S-115).
        // orig Boyfriend.java:141 IMob.mobSelector → Mob.class + instanceof Enemy; 10 / false are the 3-arg constructor's own randomInterval / mustReach (ENT-S-124, IMob convention)
        // — and orig's Mothra by name: an IMob in 1.7.10 (orig Mothra.java:52), an EntityButterfly with no Enemy here; the task's own
        // rules (orig MyEntityAITarget.java:88-128) follow in isMonsterPrey (ENT-S-128)
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Mob.class, 10, true, false,
                e -> (e instanceof Enemy || e instanceof Mothra) && this.isMonsterPrey(e)) {
            @Override
            public boolean canUse() {
                if (OreSpawnConfig.PLAY_NICELY.get()) return false; // orig Boyfriend.java:140-142 (ENT-S-115)
                return super.canUse();
            }

            @Override
            protected double getFollowDistance() {
                return 15.0; // orig Boyfriend.java:141 targetDistance 15.0f — MyEntityAINearestAttackableTarget.java:36/:56 the box (15/4/15) and MyEntityAITarget.java:52 the hold beyond 15², where vanilla reads the FOLLOW_RANGE attribute (16); the Dragon's ENT-S-117 idiom; closes the T3c box row for this goal (ENT-S-129)
            }
        });
        // orig :146-147 — Jealousy(Boyfriend.class, 6.0f, 5, true) @4 and
        // (3.0f, 15, true) @5; a tamed rival is never targeted; PlayNicely
        // gates dynamically inside the goal (ENT-A-054).
        this.targetSelector.addGoal(4, new danger.orespawn.entity.ai.JealousyTargetGoal<>(this, Boyfriend.class, 6.0, 5));
        this.targetSelector.addGoal(5, new danger.orespawn.entity.ai.JealousyTargetGoal<>(this, Boyfriend.class, 3.0, 15));
    }

    /**
     * orig MyEntityAITarget.java:78-129 {@code isSuitableTarget}, the filter of the monster task registered at orig
     * Boyfriend.java:141 (MyEntityAINearestAttackableTarget.java:56 lists {@code EntityLiving.class} through
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
        builder.define(DATA_WET_SKIN, 0); // orig Boyfriend.java:157-158
        builder.define(DATA_IS_PRINCE, 0); // orig Boyfriend.java:162
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

    public int getWetTameSkin() { // orig Boyfriend.java:461-463
        return this.entityData.get(DATA_WET_SKIN);
    }

    public void setWetTameSkin(int skinIndex) { // orig Boyfriend.java:465-468
        this.entityData.set(DATA_WET_SKIN, skinIndex);
        this.whichWetGuy = skinIndex;
    }

    /**
     * orig Boyfriend.java:291-293 — only sets the server-side field; the
     * 20-tick forceSync loop (orig :510-523) is what publishes it to clients.
     * Called by a kissed Frog (orig Frog.java:135).
     */
    public void setPrince(int prince) {
        this.isPrince = prince;
    }

    public int getPrince() {
        return this.entityData.get(DATA_IS_PRINCE);
    }

    /** Renderer hook — swimshorts skin shows while this is positive (orig Boyfriend.java:296,388). */
    public int getWetCount() {
        return this.wetCount;
    }

    /**
     * orig Boyfriend.java:179-193 (func_70658_aO) — armor is the sum of worn
     * ArmorItem defense values clamped to 8..23: even a bare Boyfriend blocks
     * like iron armor, and a full Ultimate set is capped at 23.
     */
    @Override
    public int getArmorValue() {
        int total = 0;
        for (ItemStack stack : this.getAllSlots()) {
            if (stack.getItem() instanceof ArmorItem armor) {
                total += armor.getDefense();
            }
        }
        return Mth.clamp(total, 8, 23);
    }

    @Override
    public boolean removeWhenFarAway(double dist) {
        return false;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        // orig Boyfriend.java:498-502 — water soaks him for 500 ticks; both
        // sides run this, so the client picks the wet texture with no sync.
        if (this.isInWater()) {
            this.wetCount = 500;
        } else if (this.wetCount > 0) {
            --this.wetCount;
        }
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
                this.entityData.set(DATA_IS_PRINCE, this.isPrince); // orig :516
                this.setOrderedToSit(this.isOrderedToSit());
            } else {
                this.voice = this.getVoice();
                this.voiceEnable = this.entityData.get(DATA_VOICE_ENABLE);
                this.isPrince = this.entityData.get(DATA_IS_PRINCE); // orig :521
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
                                        SND_B_FIGHT,
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
                                    SND_B_TAUNT,
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
                                SND_B_WOOHOO,
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

        // orig Boyfriend.java:536-566 — cooked beef OR cooked peacock
        // (OreSpawnMain.MyPeacock, orig OreSpawnMain.java:1873 "cookedpeacock"):
        // 1-in-3 tame chance while untamed; a full heal from the owner after.
        if ((stack.is(Items.COOKED_BEEF) || stack.is(ModItems.COOKED_PEACOCK.get()))
                && this.distanceToSqr(player) < 16.0) {
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

        // orig :567-581 — a dead bush from the owner breaks up the relationship.
        if (this.isTame() && stack.is(Items.DEAD_BUSH)
                && this.distanceToSqr(player) < 16.0 && this.isOwnedBy(player)) {
            if (!this.level().isClientSide) {
                this.setTame(false, false);
                this.setOwnerUUID(null);
                this.level().broadcastEntityEvent(this, (byte) 6);
            }
            if (!player.getAbilities().instabuild) stack.shrink(1);
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        // orig :582-596 — a ruby turns his voice off...
        if (this.isTame() && stack.is(ModItems.RUBY.get())
                && this.distanceToSqr(player) < 16.0 && this.isOwnedBy(player)) {
            if (!this.level().isClientSide) {
                this.voiceEnable = 0;
                this.entityData.set(DATA_VOICE_ENABLE, this.voiceEnable);
                this.level().broadcastEntityEvent(this, (byte) 7);
            }
            if (!player.getAbilities().instabuild) stack.shrink(1);
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        // orig :597-611 — ...and an amethyst turns it back on.
        if (this.isTame() && stack.is(ModItems.AMETHYST_GEM.get())
                && this.distanceToSqr(player) < 16.0 && this.isOwnedBy(player)) {
            if (!this.level().isClientSide) {
                this.voiceEnable = 1;
                this.entityData.set(DATA_VOICE_ENABLE, this.voiceEnable);
                this.level().broadcastEntityEvent(this, (byte) 7);
            }
            if (!player.getAbilities().instabuild) stack.shrink(1);
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        // orig :612-640 — leather or a peacock feather cycles the outfit:
        // the swimshorts set while wet (a dip in water also refreshes the
        // 500-tick wet timer), the dry set otherwise.
        if (this.isTame() && (stack.is(Items.LEATHER) || stack.is(ModItems.PEACOCK_FEATHER.get()))
                && this.distanceToSqr(player) < 16.0 && this.isOwnedBy(player)) {
            if (!this.level().isClientSide) {
                if (this.wetCount > 0 || this.isInWater()) {
                    this.whichWetGuy = (this.whichWetGuy + 1) % MAX_WET_SKINS; // orig :615-618
                    this.setWetTameSkin(this.whichWetGuy);
                    this.level().broadcastEntityEvent(this, (byte) 7);
                    if (this.isInWater()) {
                        this.wetCount = 500; // orig :621-623
                    }
                } else {
                    this.whichGuy = (this.whichGuy + 1) % MAX_SKINS; // orig :625-628
                    this.setTameSkin(this.whichGuy);
                    this.level().broadcastEntityEvent(this, (byte) 7);
                }
            }
            if (!player.getAbilities().instabuild) stack.shrink(1);
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        if (this.isTame() && !stack.isEmpty() && this.isOwnedBy(player)
                && this.distanceToSqr(player) < 16.0) {
            FoodProperties food = stack.getFoodProperties(this);
            if (food != null) {
                // orig :642-656 — any food heals 5x its nutrition.
                if (!this.level().isClientSide && this.getHealth() < this.getMaxHealth()) {
                    this.heal(food.nutrition() * 5);
                    this.level().broadcastEntityEvent(this, (byte) 7);
                }
                if (!player.getAbilities().instabuild) stack.shrink(1);
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }

            // orig :657-698 — any other item goes INTO HIS HAND as a whole
            // stack (the player gets his old held item back, if any). A
            // diamond in hand means "guard here" (sit); anything else stands
            // him up. Only OreSpawn Emerald/Amethyst/Ultimate armor auto-slots
            // (vanilla armor stays held, orig :674-696), and only when the
            // player's hand had nothing to take back.
            if (!this.level().isClientSide) {
                this.level().broadcastEntityEvent(this, (byte) 7); // orig :658-661
                ItemStack prevHeld = this.getMainHandItem();
                this.setItemSlot(EquipmentSlot.MAINHAND, stack.copy()); // orig :663
                if (stack.is(Items.DIAMOND)) { // orig :664-668
                    this.setOrderedToSit(true);
                    this.setInSittingPose(true);
                } else {
                    this.setOrderedToSit(false);
                    this.setInSittingPose(false);
                }
                if (!prevHeld.isEmpty()) {
                    player.setItemInHand(hand, prevHeld); // orig :669-670
                } else {
                    player.setItemInHand(hand, ItemStack.EMPTY); // orig :672
                    if (stack.getItem() instanceof ItemOreSpawnArmor) { // orig :674
                        EquipmentSlot armorSlot = null;
                        if (stack.is(ModItems.EMERALD_HELMET.get()) || stack.is(ModItems.AMETHYST_HELMET.get())
                                || stack.is(ModItems.ULTIMATE_HELMET.get())) {
                            armorSlot = EquipmentSlot.HEAD; // orig :676-680
                        } else if (stack.is(ModItems.EMERALD_CHESTPLATE.get()) || stack.is(ModItems.AMETHYST_CHESTPLATE.get())
                                || stack.is(ModItems.ULTIMATE_CHESTPLATE.get())) {
                            armorSlot = EquipmentSlot.CHEST; // orig :681-685
                        } else if (stack.is(ModItems.EMERALD_LEGGINGS.get()) || stack.is(ModItems.AMETHYST_LEGGINGS.get())
                                || stack.is(ModItems.ULTIMATE_LEGGINGS.get())) {
                            armorSlot = EquipmentSlot.LEGS; // orig :686-690
                        } else if (stack.is(ModItems.EMERALD_BOOTS_ARMOR.get()) || stack.is(ModItems.AMETHYST_BOOTS_ARMOR.get())
                                || stack.is(ModItems.ULTIMATE_BOOTS_ARMOR.get())) {
                            armorSlot = EquipmentSlot.FEET; // orig :691-695
                        }
                        if (armorSlot != null) {
                            // orig :677-679 pattern — the new piece moves to its
                            // armor slot and the displaced piece lands in his hand.
                            ItemStack oldPiece = this.getItemBySlot(armorSlot);
                            this.setItemSlot(armorSlot, stack.copy());
                            this.setItemSlot(EquipmentSlot.MAINHAND, oldPiece);
                        }
                    }
                }
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        // orig :701-714 — a diamond block re-claims a TAMED Boyfriend for
        // whoever is holding it: the original has no ownership check here, so
        // this is how you steal someone else's boyfriend. (Unreachable for
        // the current owner — the give-item branch above consumes it first,
        // same as the original's ordering.)
        if (this.isTame() && stack.is(Items.DIAMOND_BLOCK) && this.distanceToSqr(player) < 16.0) {
            if (!this.level().isClientSide) {
                this.setOrderedToSit(false); // orig :702
                this.setInSittingPose(false);
                this.setTame(true, false); // orig :703
                this.setOwnerUUID(player.getUUID()); // orig :704
                this.level().broadcastEntityEvent(this, (byte) 7);
            }
            if (!player.getAbilities().instabuild) stack.shrink(1);
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        if (this.isTame() && stack.isEmpty() && this.distanceToSqr(player) < 16.0 && this.isOwnedBy(player)) {
            if (!this.level().isClientSide) {
                // orig :725-746 — an empty hand takes his gear back one piece
                // at a time (held item first, then boots up to helmet).
                ItemStack toReturn = findEquippedItem();
                if (toReturn != null) {
                    player.setItemInHand(hand, toReturn);
                    this.setOrderedToSit(false); // orig :743
                    this.setInSittingPose(false);
                    this.level().broadcastEntityEvent(this, (byte) 6);
                } else {
                    // orig :747-754 — nothing left to hand over: stand up and
                    // report health in chat.
                    this.setOrderedToSit(false);
                    this.setInSittingPose(false);
                    this.level().broadcastEntityEvent(this, (byte) 7);
                    player.sendSystemMessage(Component.literal(
                            String.format("I have %d health. Thanks for asking!", (int) this.getHealth())));
                }
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }

    @Nullable
    private ItemStack findEquippedItem() {
        // orig Boyfriend.java:726-739 — slot order 0..4: held, boots, legs,
        // chest, helmet.
        EquipmentSlot[] slots = { EquipmentSlot.MAINHAND, EquipmentSlot.FEET, EquipmentSlot.LEGS,
                EquipmentSlot.CHEST, EquipmentSlot.HEAD };
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

    /**
     * orig Boyfriend.java:839-872 (func_70628_a) — only a TAMED Boyfriend
     * drops anything beyond controllers: 2-6 poppies plus every equipped
     * piece at its full count. No super call: the original never rolled
     * vanilla-style equipment drop chances, and an untamed Boyfriend takes
     * his gear to the grave. Game controllers (10-35, orig :848-853) come
     * from the loot table.
     */
    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        if (this.isTame()) {
            int poppies = this.random.nextInt(5) + 2; // orig :841-843 — 2-6
            for (int i = 0; i < poppies; i++) {
                this.spawnAtLocation(new ItemStack(Items.POPPY)); // orig :844-846
            }
            // orig :854-871 — held item first, then boots/legs/chest/helmet.
            EquipmentSlot[] slots = { EquipmentSlot.MAINHAND, EquipmentSlot.FEET, EquipmentSlot.LEGS,
                    EquipmentSlot.CHEST, EquipmentSlot.HEAD };
            for (EquipmentSlot slot : slots) {
                ItemStack gear = this.getItemBySlot(slot);
                if (!gear.isEmpty()) {
                    this.spawnAtLocation(gear.copy());
                    this.setItemSlot(slot, ItemStack.EMPTY);
                }
            }
        }
    }

    private static SoundEvent orespawnSound(String name) {
        return SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, name));
    }

    /**
     * orig Boyfriend.java:768-812 (func_70639_aQ) — situational ambient voice.
     * Silent while sitting or muted; bro mode drops half of all rolls. On a
     * 1-in-11 roll: quiet while fighting, "b_water" when swimming; then 3 rolls
     * in 4 consider the surroundings (silent below y=60, "b_thunder"/"b_rain"
     * in weather, "b_dark" 1-in-3 under a night sky); anything that falls
     * through lands on the tamed lines — "b_hurt" when damaged, else
     * "bb_happy" in bro mode or "b_happy".
     */
    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        if (this.isOrderedToSit() || this.voiceEnable == 0) return null;
        if (OreSpawnConfig.BOYFRIEND_BRO_MODE.get() && this.getRandom().nextInt(2) == 1) {
            return null; // orig :772-774
        }
        if (this.getRandom().nextInt(11) == 1) {
            if (this.getTarget() != null) return null; // orig :776-779
            if (this.isInWater()) return SND_B_WATER; // orig :780-782
            if (this.getRandom().nextInt(4) != 0) { // orig :783
                if (this.getY() < 60.0) return null; // orig :784-786
                if (this.level().isThundering()) return SND_B_THUNDER; // orig :787-789
                if (this.level().isRaining()) return SND_B_RAIN; // orig :790-792
                if (!this.level().isDay() && this.level().canSeeSky(this.blockPosition())) { // orig :793
                    return this.getRandom().nextInt(3) == 0 ? SND_B_DARK : null; // orig :794-798
                }
            }
            if (this.isTame()) { // orig :800-808
                if (this.getHealth() < this.getMaxHealth()) return SND_B_HURT;
                if (OreSpawnConfig.BOYFRIEND_BRO_MODE.get()) return SND_BB_HAPPY; // orig :804-806
                return SND_B_HAPPY;
            }
        }
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        if (this.voiceEnable == 0) return null;
        if (OreSpawnConfig.BOYFRIEND_BRO_MODE.get() && this.getRandom().nextInt(2) == 1) {
            return null; // orig Boyfriend.java:818-820 — bro mode drops half the "ow"s
        }
        return SND_B_OW;
    }

    @Override
    protected SoundEvent getDeathSound() {
        if (OreSpawnConfig.BOYFRIEND_BRO_MODE.get()) {
            return null; // orig Boyfriend.java:825-827 — bros die silently
        }
        return this.isTame() ? SND_B_DEATH_BOYFRIEND : SND_B_DEATH_SINGLE;
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
    // friendly-fire gate was removed in Phase D3: the original's bro_mode
    // config (orig OreSpawnMain.java:1481, default 0) only gates VOICE
    // behavior — restored above in getAmbientSound/getHurtSound/getDeathSound.
    // The invented combat meaning is archived in MODERNIZATION_NOTES MOD-010.)

    @Override
    public boolean isFood(ItemStack stack) {
        // orig Boyfriend.java:760-762 (isWheat/func_70877_b) — cooked beef or
        // cooked peacock.
        return stack.is(Items.COOKED_BEEF) || stack.is(ModItems.COOKED_PEACOCK.get());
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
        tag.putInt("WetGuyType", this.getWetTameSkin()); // orig Boyfriend.java:219 ("WetGirlType")
        tag.putInt("GuyVoice", this.voice);
        tag.putInt("GuyVoiceEnable", this.voiceEnable);
        // orig :222 saved the WATCHER value, not the field, so a prince kissed
        // fewer than ~50 ticks before a save loses his crown; kept as-is.
        tag.putInt("IsPrince", this.entityData.get(DATA_IS_PRINCE));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.whichGuy = tag.getInt("GuyType");
        this.setTameSkin(this.whichGuy);
        this.whichWetGuy = tag.getInt("WetGuyType"); // orig Boyfriend.java:229-230
        this.setWetTameSkin(this.whichWetGuy);
        this.voice = tag.getInt("GuyVoice");
        this.voiceEnable = tag.getInt("GuyVoiceEnable");
        this.isPrince = tag.getInt("IsPrince"); // orig Boyfriend.java:235-236
        this.entityData.set(DATA_IS_PRINCE, this.isPrince);
    }

    /** orig Boyfriend.java:978-993 — "Boyfriend" spawner bypass, else the vanilla creature rules. */
    @Override
    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level,
                                   net.minecraft.world.entity.MobSpawnType spawnType) {
        if (OriginalSpawnGates.nearOwnSpawner(this, level)) return true;
        return super.checkSpawnRules(level, spawnType);
    }
}
