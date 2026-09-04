package danger.orespawn.entity;

import java.util.Comparator;
import java.util.List;
import danger.orespawn.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.ai.GenericTargetSorter;
import danger.orespawn.entity.ai.TargetSelection;

public class Cephadrome extends PathfinderMob
        implements danger.orespawn.network.RiderInputPayload.RideableFlyer {
    // OPT-011: cached SoundEvents — identical createVariableRangeEvent ids,
    // allocated once per class instead of on every sound query.
    private static final SoundEvent SND_MOTHRAWINGS = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "mothrawings"));
    private static final SoundEvent SND_ALO_HURT = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "alo_hurt"));
    private static final SoundEvent SND_ALO_DEATH = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "alo_death"));
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(Cephadrome.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_ACTIVITY =
            SynchedEntityData.defineId(Cephadrome.class, EntityDataSerializers.INT);
    // TF-032: the Cephadrome has no persistent tame state. Feeding any of
    // the three raw meats (orig Cephadrome.java:878 — beef / chicken /
    // porkchop) heals to full and arms a one-ride `wasfed` gate consumed
    // on mount (orig :903). A prior revision added a wiki-derived
    // porkchop-only tame flag on top; removed as non-source behavior
    // (see MODERNIZATION_NOTES MOD-021 for the archived variant).

    /**
     * Ridden-flight tuning, number-for-number from orig Cephadrome.java:703-835
     * (ridden branch of onLivingUpdate): hover probe 1.55 with the strong
     * +0.07/+0.1 lift and 0.018 glide-fall (orig :720-727), terrain scan
     * 2 + v*6 @ 0.04/block ×0.09 (orig :728-741), rise cap 2.0 (orig :742-744),
     * yaw lag 1.5 above 0.1 (orig :760-771), pitch inverts while rising
     * (orig :776 — 360 - 2v), fly-up +0.04 + v*0.05 (orig :786-789), throttle
     * 0.03+0.05 applied instantly — no deltasmooth ramp (max_speed 1.15 > 0.85
     * bonus gate, orig :800-809 with max_speed at :673), reverse 0.35 @ -0.03
     * (orig :807-808), friction 0.985/0.94/0.985 (orig :833-835).
     */
    private static final danger.orespawn.entity.ai.RiderFlightController.Config RIDER_FLIGHT_CONFIG =
            new danger.orespawn.entity.ai.RiderFlightController.Config(
                    true, 1.55, 0.07, 0.1, 0.018,
                    2, 6.0, 0.04, 0.09, false,
                    2.0,
                    1.5, 0.1, true,
                    false, 0.04, 0.05,
                    0.08, 1.15, -0.03, 0.35, false,
                    0.0, 0.985, 0.94);

    private final danger.orespawn.entity.ai.RiderFlightController riderFlight =
            new danger.orespawn.entity.ai.RiderFlightController(RIDER_FLIGHT_CONFIG);
    /** Held state of the rider's vertical keys (client-set for prediction, server-set via payload). */
    private boolean riderFlyUp = false;
    private boolean riderFlyDown = false;

    private final Comparator<Entity> targetSorter;
    private final float moveSpeed = 0.25f;
    private int hurtTimer = 0;
    private int wasfed = 0;
    /** orig Cephadrome.java:66 wing_sound — ridden wing-beat cadence counter. */
    private int wingSoundTicks = 0;
    private int shouldattack = 0;
    private int hitByPlayer = 0;
    private int badmood = 0;
    /**
     * Per-entity render scratch (orig Cephadrome.java:62 {@code renderdata = new RenderInfo()},
     * re-newed in the ctor at orig :85, zeroed in entityInit at orig :139-149; accessor
     * orig :156-158). Mutated client-side by {@code ModelCephadrome} for the ridden-flight
     * neck-yaw latch rf1 (orig ModelCephadrome.java:469-479); never datawatcher-synced.
     * ENT-S-093.
     */
    private final danger.orespawn.entity.client.RenderInfo renderInfo =
            new danger.orespawn.entity.client.RenderInfo();

    private static final float MELEE_DAMAGE = 70.0f;
    private static final double KNOCKBACK_HORIZONTAL = 2.5;
    private static final double KNOCKBACK_VERTICAL = 0.35;
    private static final double PLAYER_OR_REMOVED_VERTICAL_MULTIPLIER = 2.0;
    private static final float LOW_HEALTH_FRACTION_FOR_PLAYER_AGGRO = 9.0f / 10.0f;

    public Cephadrome(EntityType<? extends Cephadrome> type, Level level) {
        super(type, level);
        // OPT-009: constant speed - assert the attribute base once here instead
        // of re-writing it every tick (same value the removed per-tick call set).
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(this.moveSpeed);
        this.xpReward = 200;
        this.targetSorter = new GenericTargetSorter(this); // orig Cephadrome.java:84 — GenericTargetSorter (the field :61; a creeper's distance² halved, a silhouette over 1 divides), the sort at :580; plain distance was the TF-035 remainder (ENT-S-139)
    }

    /** Mirrors orig Cephadrome.java:156-158 {@code getRenderInfo()} (ENT-S-093). */
    public danger.orespawn.entity.client.RenderInfo getRenderInfo() {
        return this.renderInfo;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MyEntityAIWanderALot(this, 16, 1.0));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 9.0f));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 300.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.ATTACK_DAMAGE, 70.0)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.ARMOR, 16.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, 0);
        builder.define(DATA_ACTIVITY, 0);
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) return;

        // ENT-A-083: orig Cephadrome.java:652-659 — while ridden (activity 1)
        // a wing beat plays every 22 ticks at 0.5 volume, server side.
        if (this.getActivity() == 1) {
            if (++this.wingSoundTicks > 22) {
                this.level().playSound(null, this,
                        SND_MOTHRAWINGS,
                        this.getSoundSource(), 0.5f, 1.0f);
                this.wingSoundTicks = 0;
            }
        }

        // orig Cephadrome.java:661-663 — with PlayNicely off, every Cephadrome
        // counts as fed, so any player can hop on without offering meat first.
        if (!danger.orespawn.OreSpawnConfig.PLAY_NICELY.get()) {
            this.wasfed = 1;
        }

        // Flying while ridden (no gravity fights with the rider-flight physics).
        this.setNoGravity(this.isVehicle() && this.getControllingPassenger() != null);

        if (this.isVehicle() && this.getControllingPassenger() != null) {
            serverRiddenTick();
        }
    }

    // ==================== Riding ====================

    /**
     * Any mounted player steers — the original had no tame/ownership gate on
     * control, only the {@code wasfed} gate on mounting (orig
     * Cephadrome.java:893-904).
     */
    @javax.annotation.Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        if (!this.getPassengers().isEmpty() && this.getPassengers().get(0) instanceof Player player) {
            return player;
        }
        return super.getControllingPassenger();
    }

    /**
     * Seats the rider 0.75 blocks ahead of center, 2.5 up (orig
     * Cephadrome.java:852-857 with mounted y-offset 2.5 at :211-213).
     */
    @Override
    protected void positionRider(Entity passenger, Entity.MoveFunction callback) {
        if (!this.hasPassenger(passenger)) return;
        double rx = this.getX() - 0.75 * Math.sin(Math.toRadians(this.getYRot()));
        double ry = this.getY() + 2.5;
        double rz = this.getZ() + 0.75 * Math.cos(Math.toRadians(this.getYRot()));
        callback.accept(passenger, rx, ry, rz);
    }

    /**
     * Client-predicted ridden flight: the riding client runs the original
     * hovering sand-shark physics (orig Cephadrome.java:703-835, constants in
     * {@link #RIDER_FLIGHT_CONFIG}) and syncs position like a vanilla horse.
     */
    @Override
    protected void tickRidden(Player rider, Vec3 travelVector) {
        super.tickRidden(rider, travelVector);
        if (this.isControlledByLocalInstance()) {
            this.riderFlight.tick(this, rider, this.riderFlyUp, this.riderFlyDown);
        }
    }

    /**
     * Skips vanilla travel while player-ridden: {@link #tickRidden} already
     * applied the full move via {@code RiderFlightController}, so running
     * vanilla travel too would integrate the motion twice.
     */
    @Override
    public void travel(Vec3 travelVector) {
        if (this.isControlledByLocalInstance() && this.getControllingPassenger() instanceof Player) {
            return;
        }
        super.travel(travelVector);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Consumed by {@link #tickRidden}; modern per-player equivalent of
     * the original's global {@code OreSpawnMain.flyup_keystate} poll (orig
     * Cephadrome.java:786-789).</p>
     */
    @Override
    public void setRiderVerticalInput(boolean up, boolean down) {
        this.riderFlyUp = up;
        this.riderFlyDown = down;
    }

    /**
     * Server-side portion of the original ridden branch — pushing nearby
     * entities (orig Cephadrome.java:836-842, box 2.25/2.0/2.25) and ejecting
     * a removed rider (orig :843-845). The Cephadrome has no mounted
     * auto-attack in the original.
     */
    private void serverRiddenTick() {
        if (this.isRemoved()) return;

        List<Entity> nearby = this.level().getEntities(this, this.getBoundingBox().inflate(2.25, 2.0, 2.25));
        for (Entity entity : nearby) {
            if (entity != this.getFirstPassenger() && !entity.isRemoved() && entity.isPushable()) {
                entity.push(this);
            }
        }

        if (this.getFirstPassenger() != null && this.getFirstPassenger().isRemoved()) {
            this.ejectPassengers();
        }
    }

    @Override
    public boolean removeWhenFarAway(double dist) {
        // orig Cephadrome.java:932-937 — never despawn while ridden.
        if (this.isPersistenceRequired()) return false;
        return !this.isVehicle();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        if (this.getActivity() != 1 && this.random.nextInt(6) == 1) {
            return SND_MOTHRAWINGS;
        }
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SND_ALO_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SND_ALO_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 1.5f;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (!(target instanceof LivingEntity)) {
            return false;
        }
        // orig Cephadrome.java:409-419 — Ender Dragon takes 70 directly to a
        // body part via an explosion-typed source; no knockback applied.
        if (target instanceof EnderDragon dragon) {
            return dragon.hurt(this.damageSources().explosion(this, this), MELEE_DAMAGE);
        }
        // orig Cephadrome.java:421-424 — Kraken takes ×1.5 damage.
        float damage = target instanceof Kraken ? MELEE_DAMAGE * 1.5f : MELEE_DAMAGE;
        boolean hurtApplied = target.hurt(this.damageSources().mobAttack(this), damage);
        double verticalKnockback = KNOCKBACK_VERTICAL;
        float yawToTarget = (float) Math.atan2(target.getZ() - this.getZ(), target.getX() - this.getX());
        if (target.isRemoved() || target instanceof Player) {
            verticalKnockback *= PLAYER_OR_REMOVED_VERTICAL_MULTIPLIER;
        }
        target.push(
                Math.cos(yawToTarget) * KNOCKBACK_HORIZONTAL,
                verticalKnockback,
                Math.sin(yawToTarget) * KNOCKBACK_HORIZONTAL);
        return hurtApplied;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.hurtTimer > 0) return false;
        if (source.getMsgId().equals("cactus")) return false;

        boolean hurtApplied = super.hurt(source, amount);
        this.hurtTimer = 25;
        Entity attacker = source.getEntity();
        if (attacker instanceof LivingEntity livingAttacker) {
            this.setTarget(livingAttacker);
            this.getNavigation().moveTo(livingAttacker, 1.2);
        }
        if (attacker instanceof Player && this.getHealth() < this.getMaxHealth() * LOW_HEALTH_FRACTION_FOR_PLAYER_AGGRO) {
            this.hitByPlayer = 1;
        }
        return hurtApplied;
    }

    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;
        if (this.hurtTimer > 0) --this.hurtTimer;

        if (this.random.nextInt(100) == 1 && this.getHealth() < this.getMaxHealth()) {
            this.heal(2.0f);
        }

        super.customServerAiStep();

        // orig Cephadrome.java:488 — the 1-in-7 roll, then `difficulty != PEACEFUL`: the whole hunt
        // block is skipped on Peaceful, a stored revenge target included (ENT-S-114; the filter's own
        // guard at orig :516 is ENT-S-113).
        if (this.random.nextInt(7) == 1 && this.level().getDifficulty() != Difficulty.PEACEFUL) {
            LivingEntity target = this.getTarget();
            if (target != null && !target.isAlive()) {
                this.setTarget(null);
                target = null;
            }
            if (target == null) {
                target = this.findSomethingToAttack();
            }
            if (target != null) {
                this.getNavigation().moveTo(target, 1.7);
                this.lookAt(target, 10.0f, 10.0f);
                this.setAttacking(1);
                double meleeRange = 6.0 + target.getBbWidth() / 2.0;
                if (this.distanceToSqr(target) < meleeRange * meleeRange) {
                    this.doHurtTarget(target);
                }
            } else if (this.getAttacking() != 0) {
                this.setAttacking(0);
            }
        }
    }

    /**
     * ENT-S-107: the player branch's {@code capabilities.isCreativeMode} (orig
     * Cephadrome.java:557) is {@code Abilities.instabuild} — the port's own
     * Kraken / TheKing idiom — not {@code invulnerable}; the two differ for a
     * survival player made invulnerable by other means. The hit-by-player /
     * bad-mood / should-attack gates that follow are orig :560-570.
     *
     * <p>ENT-S-113: orig :516-518 answers false on PEACEFUL before any other
     * check, and orig :566-569 spends {@code shouldattack} — the stalk flag the
     * refused mount arms (orig :899, {@link #mobInteract}) — on the one answer
     * it grants, so an unfed shark stalks the would-be rider for a single scan;
     * both restored at the orig positions.</p>
     */
    private boolean isSuitableTarget(LivingEntity target) {
        if (this.level().getDifficulty() == Difficulty.PEACEFUL) return false; // orig Cephadrome.java:516-518 (ENT-S-113)
        if (target == null || target == this || !target.isAlive()) return false;
        if (!this.getSensing().hasLineOfSight(target)) return false;
        if (target instanceof Cephadrome) return false;
        if (target instanceof Monster) return true;
        // orig Cephadrome.java:537-554 — Mothra always a target; Leon /
        // GammaMetroid / WaterDragon only while untamed; EnderDragon always.
        if (target instanceof Mothra) return true;
        if (target instanceof EntityLeon leon) return !leon.isTame();
        if (target instanceof EntityGammaMetroid metroid) return !metroid.isTame();
        if (target instanceof WaterDragon waterDragon) return !waterDragon.isTame();
        if (target instanceof EnderDragon) return true;
        if (target instanceof Player player) {
            if (player.getAbilities().instabuild) return false; // orig Cephadrome.java:557 isCreativeMode (ENT-S-107)
            if (this.hitByPlayer != 0) return true;              // orig Cephadrome.java:560-562
            if (this.badmood != 0) return true;                  // orig Cephadrome.java:563-565
            if (this.shouldattack > 0) {                         // orig Cephadrome.java:566-569 — spent on this answer (ENT-S-113)
                this.shouldattack = 0;
                return true;
            }
            return false;                                        // orig Cephadrome.java:570
        }
        return false;
    }

    private LivingEntity findSomethingToAttack() {
        if (danger.orespawn.OreSpawnConfig.PLAY_NICELY.get()) return null; // orig Cephadrome.java:576-578 — PlayNicely != 0 returns null ahead of the scan (ENT-S-115)
        AABB searchBox = this.getBoundingBox().inflate(16.0, 20.0, 16.0);
        List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, searchBox);
        // OPT-021: nearest-first pick without the full list sort; TargetSelection
        // preserves the removed sort's order and stable-tie semantics exactly.
        return TargetSelection.firstMatch(targets, this.targetSorter, this::isSuitableTarget);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        // Feed gate (orig Cephadrome.java:878-891): any of the three RAW
        // meats — beef / chicken / porkchop — within 5 blocks heals to
        // full, arms the one-ride `wasfed` flag, calms the shark, and
        // bursts heart particles (orig playTameEffect(true), :884). One
        // item is consumed outside creative. There is no tame state.
        if ((stack.is(Items.BEEF) || stack.is(Items.CHICKEN) || stack.is(Items.PORKCHOP))
                && this.distanceToSqr(player) < 25.0) {
            if (!this.level().isClientSide) {
                this.heal(this.getMaxHealth() - this.getHealth());
                this.spawnFeedHearts();
            }
            this.wasfed = 1;
            this.shouldattack = 0;
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        // Empty hand: mount (orig Cephadrome.java:893-904). A shark that has
        // not been fed ({@code wasfed == 0}) refuses, stalks the would-be
        // rider (navigate 1.2 + shouldattack), and stays grounded; a fed one
        // accepts and consumes the "fed" state so each ride needs a fresh meal
        // (orig :903; PlayNicely off re-feeds every tick, see tick()).
        if (stack.isEmpty() && this.distanceToSqr(player) < 25.0) {
            if (this.isVehicle() && this.getFirstPassenger() != player) {
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }
            if (!this.level().isClientSide) {
                if (this.wasfed == 0) {
                    this.getNavigation().moveTo(player, 1.2);
                    this.shouldattack = 1;
                    return InteractionResult.PASS;
                }
                player.startRiding(this);
                this.wasfed = 0;
                this.setActivity(1);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }

    public int getAttacking() {
        return this.entityData.get(DATA_ATTACKING);
    }

    public void setAttacking(int value) {
        if (this.level() != null && this.level().isClientSide) return;
        this.entityData.set(DATA_ATTACKING, (int) (byte) value);
    }

    public int getActivity() {
        return this.entityData.get(DATA_ACTIVITY);
    }

    public void setActivity(int value) {
        if (this.level() != null && this.level().isClientSide) return;
        this.entityData.set(DATA_ACTIVITY, (int) (byte) value);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("CephaWasFed", this.wasfed);
        tag.putInt("CephaAttacking", this.getAttacking());
        tag.putInt("CephaActivity", this.getActivity());
        tag.putInt("CephaHitByPlayer", this.hitByPlayer);
        tag.putInt("CephaBadMood", this.badmood);
    }

    /**
     * Heart-particle burst on a successful feed — ports the original's
     * {@code playTameEffect(true)} call (orig Cephadrome.java:884, effect
     * body :858-870): 20 hearts with gaussian velocity 0.08 scattered
     * ±2.5 blocks horizontally and 0.5-2.0 above the body. Server side.
     */
    private void spawnFeedHearts() {
        if (!(this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel)) return;
        for (int i = 0; i < 20; i++) {
            double vx = this.random.nextGaussian() * 0.08;
            double vy = this.random.nextGaussian() * 0.08;
            double vz = this.random.nextGaussian() * 0.08;
            serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.HEART,
                    this.getX() + (this.random.nextFloat() - this.random.nextFloat()) * 2.5,
                    this.getY() + 0.5 + this.random.nextFloat() * 1.5,
                    this.getZ() + (this.random.nextFloat() - this.random.nextFloat()) * 2.5,
                    1, vx, vy, vz, 0.0);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.wasfed = tag.getInt("CephaWasFed");
        this.hitByPlayer = tag.getInt("CephaHitByPlayer");
        this.badmood = tag.getInt("CephaBadMood");
        this.setAttacking(tag.getInt("CephaAttacking"));
        this.setActivity(tag.getInt("CephaActivity"));
    }

    /**
     * orig Cephadrome.java:593-630 — spawner bypass (flags {@code badmood} so the
     * spawnered shark is born hostile); daytime; y&gt;=50; clear air above; no other
     * Cephadrome within 16/6/16.
     */
    @Override
    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level,
                                   net.minecraft.world.entity.MobSpawnType spawnType) {
        if (OriginalSpawnGates.nearOwnSpawner(this, level)) {
            this.badmood = 1;
            return true;
        }
        if (!OriginalSpawnGates.isDaytime(level)) return false;
        if (this.getY() < 50.0) return false;
        if (!OriginalSpawnGates.airBox(this, level, -2, 1, 1, 4, -2, 1)) return false;
        return !OriginalSpawnGates.anyOtherNearby(this, level, Cephadrome.class, 16.0, 6.0, 16.0);
    }
}
