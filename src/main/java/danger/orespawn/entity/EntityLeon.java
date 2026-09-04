package danger.orespawn.entity;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
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
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.ai.TargetSelection;
import danger.orespawn.util.MyUtils;

/**
 * The consolidated Leon/Leonopteryx (TF-030). 1.7.10 has a single class
 * {@code Leon} registered under the entity name "Leonopteryx"
 * (orig OreSpawnMain.java:4377/4381); the port had split it into EntityLeon +
 * a divergent Leonopteryx twin. This one class now backs BOTH registry ids —
 * orespawn:leonopteryx (canonical) and orespawn:leon (save-compat alias) —
 * see ModEntities. The retired twin's inventions (ServerBossEvent boss bar,
 * MEAT-tag taming, 300/40 stats, invented flight constants) had no basis in
 * orig Leon.java and were dropped in favor of this faithful port.
 */
public class EntityLeon extends TamableAnimal
        implements danger.orespawn.network.RiderInputPayload.RideableFlyer {
    // OPT-011: cached SoundEvents — identical createVariableRangeEvent ids,
    // allocated once per class instead of on every sound query.
    private static final SoundEvent SND_LEON_LIVING = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "leon_living"));
    private static final SoundEvent SND_LEON_HIT = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "leon_hit"));
    private static final SoundEvent SND_LEON_DEATH = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "leon_death"));
    private static final SoundEvent SND_MOTHRAWINGS = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "mothrawings"));

    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(EntityLeon.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_ACTIVITY =
            SynchedEntityData.defineId(EntityLeon.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_BEING_RIDDEN =
            SynchedEntityData.defineId(EntityLeon.class, EntityDataSerializers.INT);

    /**
     * Ridden-flight tuning, number-for-number from orig Leon.java:741-889
     * (ridden branch of onLivingUpdate): hover probe 1.55 (orig :758-765 —
     * lift +0.03/+0.1, glide-fall 0.018), terrain scan 3 + v*7 @ 0.05/block
     * ×0.07 (orig :767-779), rise cap 2.0 (orig :780-782), yaw lag 1.85 above
     * 0.01 (orig :799-810), fly-up +0.035 + v*0.038 (orig :827-830), throttle
     * 0.028+0.06 ramped (max_speed 1.15 > 1.0 bonus gate, orig :843-846 with
     * max_speed at :703), reverse 0.35 @ -0.02 (orig :855-856), friction
     * 0.985/0.94/0.985 (orig :887-889).
     */
    private static final danger.orespawn.entity.ai.RiderFlightController.Config RIDER_FLIGHT_CONFIG =
            new danger.orespawn.entity.ai.RiderFlightController.Config(
                    true, 1.55, 0.03, 0.1, 0.018,
                    3, 7.0, 0.05, 0.07, false,
                    2.0,
                    1.85, 0.01, false,
                    false, 0.035, 0.038,
                    0.088, 1.15, -0.02, 0.35, true,
                    0.0, 0.985, 0.94);

    private final danger.orespawn.entity.ai.RiderFlightController riderFlight =
            new danger.orespawn.entity.ai.RiderFlightController(RIDER_FLIGHT_CONFIG);
    /** Held state of the rider's vertical keys (client-set for prediction, server-set via payload). */
    private boolean riderFlyUp = false;
    private boolean riderFlyDown = false;

    private final float moveSpeed = 0.25f;
    private int hurtTimer = 0;
    private int wingSound = 0;
    private boolean targetInSight = false;
    private int ownerFlying = 0;
    private int flyaway = 0;
    private int stuckCount = 0;
    private int lastX = 0;
    private int lastZ = 0;
    private int unstickTimer = 0;
    @Nullable
    private BlockPos currentFlightTarget = null;

    /**
     * Per-entity render scratch (orig Leon.java:64 {@code renderdata = new RenderInfo()},
     * re-created orig Leon.java:98, zeroed per spawn orig Leon.java:155-165, accessor
     * orig Leon.java:176-178). Mutated client-side by {@code LeonModel} for the
     * ridden-flight head/sail yaw filter (orig ModelLeon.java:1013-1024, field rf1 only);
     * never datawatcher-synced and never saved to NBT, exactly as the original. ENT-S-093.
     */
    private final danger.orespawn.entity.client.RenderInfo renderInfo =
            new danger.orespawn.entity.client.RenderInfo();

    public EntityLeon(EntityType<? extends EntityLeon> type, Level level) {
        super(type, level);
        // OPT-009: constant speed - assert the attribute base once here instead
        // of re-writing it every tick (same value the removed per-tick call set).
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(this.moveSpeed);
        this.xpReward = 300;
        // orig Leon.java:83 sets maxHurtResistantTime=10 alongside the 15-tick
        // hurt_timer gate (orig :306/:322, ported as hurtTimer). NOT ported:
        // LivingEntity.invulnerableDuration is final in 1.21.1, and the value
        // is unobservable anyway — the vanilla i-frame window (half of 10 or
        // of the default 20 = at most 10 ticks) is strictly inside the
        // 15-tick hurtTimer full block, so behavior is identical either way.
        this.setOrderedToSit(false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new FollowOwnerGoal(this, 1.1, 16.0f, 2.0f));
        this.goalSelector.addGoal(2, new TemptGoal(this, 1.25, Ingredient.of(Items.BEEF), false));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.75));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 9.0f));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
        // MOD-033 (T9 A2, petsDefendOwner; the tame rule joined 2026-09-05): the owner-defence pair and the tame rule
        // on the hunt below are modern only, a construction snapshot (the helper read ONCE here into a final the hunt's
        // selector captures — never read live; goals register in the Mob ctor, the BOSS-017 shape — a config change
        // applies to newly spawned Leons); orig Leon.java:92-95 registered no owner goals and its hunt carried no tame
        // term. Live here: flyWithRider reads the target slot first, so a tamed modern Leon avenges and defends its
        // owner, and its hunt does not overwrite a target it already holds.
        final boolean petsDefendOwner = OreSpawnConfig.petsDefendOwner();
        if (petsDefendOwner) {
            this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
            this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        }
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this)); // orig Leon.java:95 — both modes
        // orig Leon.java:92-94 — the EntityAINearestAttackableTarget task (EntityLiving.class, IMob selector) is
        // registered only when PlayNicely == 0 at construction; the port registers the goal always and reads the
        // flag live in its canUse, so it never starts while PlayNicely is on (ENT-S-115; the :391 filter gate is
        // ENT-S-110's, at isSuitableTarget).
        // orig Leon.java:93 IMob.mobSelector → Mob.class + instanceof Enemy (ENT-S-124, IMob convention), and'ed ahead
        // of the port's tame rule in modern only (a tamed Leon holding a target refuses every candidate — MOD-033, gated
        // here since the ENT-S-124 refutation closed 2026-09-04); classic is the bare Enemy test, exactly what orig :93's
        // task tested (an EntityLiving.class list through IMob.mobSelector, no further selector, no tame term).
        final Predicate<LivingEntity> huntSelector = petsDefendOwner
                ? e -> e instanceof Enemy && (!this.isTame() || this.getTarget() == null)
                : e -> e instanceof Enemy;
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Mob.class, 10, true, false, huntSelector) {
            @Override
            public boolean canUse() {
                if (OreSpawnConfig.PLAY_NICELY.get()) return false; // orig Leon.java:92-94 (ENT-S-115)
                return super.canUse();
            }
        });
    }

    public static AttributeSupplier.Builder createAttributes() {
        // Orig Leon ignores its "Leonopteryx" stats-table entry (150/20/8) and
        // hardcodes: HP 250 (orig Leon.java:169), ATK 55 (orig Leon.java:117),
        // armor 16 (orig Leon.java:192), speed 0.25 (orig Leon.java:81).
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 250.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.ATTACK_DAMAGE, 55.0)
                .add(Attributes.ARMOR, 16.0)
                .add(Attributes.FOLLOW_RANGE, 40.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, 0);
        builder.define(DATA_ACTIVITY, 0);
        builder.define(DATA_BEING_RIDDEN, 0);
    }

    public int getAttacking() {
        return this.entityData.get(DATA_ATTACKING);
    }

    public void setAttacking(int value) {
        if (this.level() != null && this.level().isClientSide) return;
        this.entityData.set(DATA_ATTACKING, value);
    }

    public int getActivity() {
        return this.entityData.get(DATA_ACTIVITY);
    }

    public void setActivity(int value) {
        if (this.level() != null && this.level().isClientSide) return;
        this.entityData.set(DATA_ACTIVITY, value);
    }

    public int getBeingRidden() {
        return this.entityData.get(DATA_BEING_RIDDEN);
    }

    public void setBeingRidden(int value) {
        if (this.level() != null && this.level().isClientSide) return;
        this.entityData.set(DATA_BEING_RIDDEN, value);
    }

    /** Mirrors orig Leon.java:176-178 {@code getRenderInfo()}. ENT-S-093. */
    public danger.orespawn.entity.client.RenderInfo getRenderInfo() {
        return this.renderInfo;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("LeonAttacking", this.getAttacking());
        tag.putInt("LeonActivity", this.getActivity());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setAttacking(tag.getInt("LeonAttacking"));
        this.setActivity(tag.getInt("LeonActivity"));
    }

    // ==================== Properties ====================

    public int mygetMaxHealth() {
        return 250;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    // ==================== Riding ====================

    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        if (!this.getPassengers().isEmpty()) {
            Entity first = this.getPassengers().get(0);
            if (first instanceof Player player && this.isOwnedBy(player)) {
                return player;
            }
        }
        return super.getControllingPassenger();
    }

    /**
     * Seats the rider 0.65 blocks ahead of center (orig Leon.java:943-948,
     * {@code updateRiderPosition} forward offset 0.65f) at the original mount
     * height of 3.75 (orig Leon.java:238-240, {@code func_70042_X}). The old
     * {@code getBbHeight() * 0.85} stand-in only equalled ~3.8 with the
     * pre-consolidation 4.5 hitbox; with the orig 8.25 height restored
     * (Leon.java:80) the explicit 3.75 is required.
     */
    @Override
    protected void positionRider(Entity passenger, Entity.MoveFunction callback) {
        if (!this.hasPassenger(passenger)) return;
        double rx = this.getX() - 0.65 * Math.sin(Math.toRadians(this.getYRot()));
        double ry = this.getY() + 3.75;
        double rz = this.getZ() + 0.65 * Math.cos(Math.toRadians(this.getYRot()));
        callback.accept(passenger, rx, ry, rz);
    }

    /**
     * Client-predicted rider FLIGHT (ENT-K-017): the riding client runs the
     * original Leon flight physics (orig Leon.java:741-889, constants in
     * {@link #RIDER_FLIGHT_CONFIG}) and syncs position to the server like a
     * vanilla horse. Replaces the interim ground-only 1.8× walk control.
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
     * Leon.java:827-830).</p>
     */
    @Override
    public void setRiderVerticalInput(boolean up, boolean down) {
        this.riderFlyUp = up;
        this.riderFlyDown = down;
    }

    @Override
    public boolean causeFallDamage(float dist, float mult, DamageSource source) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
    }

    @Override
    public boolean removeWhenFarAway(double dist) {
        if (this.isPersistenceRequired()) return false;
        if (this.getPassengers().size() > 0) return false;
        return !this.isTame();
    }

    // ==================== Sounds ====================

    @Override
    protected SoundEvent getAmbientSound() {
        if (this.isOrderedToSit()) return null;
        if (this.getActivity() == 1 && this.getPassengers().isEmpty()) {
            return SND_LEON_LIVING;
        }
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SND_LEON_HIT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SND_LEON_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 1.75f;
    }

    @Override
    public float getVoicePitch() {
        return 0.85f;
    }

    // ==================== Combat ====================

    /** orig Leon.java:275-301 ({@code func_70652_k}) — special-damage rules (ENT-K-018). */
    @Override
    public boolean doHurtTarget(Entity target) {
        // orig :279-288 — the Ender Dragon is hit through a dragon PART with an
        // attacker-less explosion-typed source (func_94539_a(null) +
        // func_94540_d) for 55: 1-in-6 rolls the head part (field_70986_h),
        // otherwise the body part (field_70987_i). No knockback on this branch.
        if (target instanceof EnderDragon dragon) {
            EnderDragonPart part = dragon.head;
            if (this.random.nextInt(6) != 1) {
                // Only `head` is a public field in 1.21.1; resolve the body
                // part by its name from the sub-entity array.
                for (EnderDragonPart sub : dragon.getSubEntities()) {
                    if ("body".equals(sub.name)) {
                        part = sub;
                        break;
                    }
                }
            }
            dragon.hurt(part, this.damageSources().explosion(null, null), 55.0f);
            return true;
        }
        if (target instanceof LivingEntity living) {
            // orig :290-292 — 4x damage vs the Kraken.
            float krakenFactor = target instanceof Kraken ? 4.0f : 1.0f;
            living.hurt(this.damageSources().mobAttack(this), krakenFactor * 55.0f);
            float angle = (float) Math.atan2(target.getZ() - this.getZ(), target.getX() - this.getX());
            double knockbackStrength = 1.25;
            double verticalKnockback = target.isRemoved() || target instanceof Player ? 0.3 : 0.15;
            target.push(Math.cos(angle) * knockbackStrength, verticalKnockback, Math.sin(angle) * knockbackStrength);
        }
        return true;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.hurtTimer > 0) return false;
        if (source.getMsgId().equals("inWall")) return false;
        if (!this.level().isClientSide) {
            this.setOrderedToSit(false);
            this.setActivity(1);
        }

        Entity attacker = source.getEntity();
        if (attacker instanceof EntityLeon) return false;
        if (this.isTame() && attacker instanceof Player) return false;

        boolean ret = super.hurt(source, amount);
        this.hurtTimer = 15;

        if (attacker instanceof LivingEntity living && !this.level().isClientSide) {
            this.setTarget(living);
            this.getNavigation().moveTo(living, 1.2);
        }
        return ret;
    }

    // ==================== Tick & AI ====================

    @Override
    public void tick() {
        super.tick();

        if (this.hurtTimer > 0) --this.hurtTimer;

        if (this.getActivity() == 1) {
            ++this.wingSound;
            if (this.wingSound > 20) {
                if (!this.level().isClientSide) {
                    this.level().playSound(null, this.blockPosition(),
                            SND_MOTHRAWINGS,
                            this.getSoundSource(), 0.5f, 1.0f);
                }
                this.wingSound = 0;
            }
        }

        if (this.isInWater()) {
            Vec3 motion = this.getDeltaMovement();
            this.setDeltaMovement(motion.x, motion.y + 0.07, motion.z);
        }

        if (this.level().isClientSide) return;

        alwaysDo();

        if (this.getActivity() == 0) return;

        if (!this.getPassengers().isEmpty()) {
            this.setBeingRidden(1);
            serverRiddenTick();
        } else {
            this.setBeingRidden(0);
            flyWithoutRider();
        }
    }

    /**
     * Server-side portion of the original ridden branch — everything except
     * movement (which is client-predicted in {@code travelRidden}): pushing
     * nearby entities (orig Leon.java:890-896, box 2.25/2.0/2.25), the mounted
     * auto-melee {@code fly_with_rider} (orig :345-375), and ejecting a
     * removed rider (orig :898-900).
     */
    private void serverRiddenTick() {
        if (this.isRemoved()) return;

        List<Entity> nearby = this.level().getEntities(this, this.getBoundingBox().inflate(2.25, 2.0, 2.25));
        for (Entity entity : nearby) {
            if (entity != this.getFirstPassenger() && !entity.isRemoved() && entity.isPushable()) {
                entity.push(this);
            }
        }

        flyWithRider();

        if (this.getFirstPassenger() != null && this.getFirstPassenger().isRemoved()) {
            this.ejectPassengers();
        }
    }

    /**
     * Mounted auto-melee (orig Leon.java:345-375 {@code fly_with_rider}):
     * 1-in-7 chance per tick outside Peaceful, re-acquires a target when the
     * current one is dead, and bites anything within {@code 9.0 + width/2}
     * blocks of the saddle.
     */
    private void flyWithRider() {
        if (this.isRemoved() || this.isOrderedToSit() || this.level().isClientSide) return;
        if (this.level().getDifficulty() == Difficulty.PEACEFUL) return;
        if (this.random.nextInt(7) != 1) return;

        LivingEntity target = this.getTarget();
        if (target != null && !target.isAlive()) {
            this.setTarget(null);
            target = null;
        }
        if (target == null) {
            target = findSomethingToAttack();
        }
        if (target != null) {
            this.setAttacking(1);
            float attackRange = 9.0f + target.getBbWidth() / 2.0f;
            if (this.distanceToSqr(target) < attackRange * attackRange) {
                this.doHurtTarget(target);
            }
        } else {
            this.setAttacking(0);
        }
    }

    @Override
    protected void customServerAiStep() {
        if (this.level().isClientSide) return;
        super.customServerAiStep();

        if (this.random.nextInt(200) == 1) {
            this.setTarget(null);
        }
    }

    private void alwaysDo() {
        if (this.level().isClientSide) return;

        if (!this.isOrderedToSit() && this.getActivity() == 0
                && this.getPassengers().isEmpty()
                && this.level().getDifficulty() != Difficulty.PEACEFUL
                && this.random.nextInt(10) == 1) {
            LivingEntity target = findSomethingToAttack();
            if (target != null) {
                this.setActivity(1);
            }
        }

        if (this.random.nextInt(250) == 1 && this.getHealth() < this.getMaxHealth()) {
            this.heal(2.0f);
        }

        if (this.isOrderedToSit()) return;

        this.ownerFlying = 0;
        if (this.isTame() && this.getOwner() != null
                && this.getPassengers().isEmpty() && !this.isOrderedToSit()) {
            Player owner = (Player) this.getOwner();
            if (owner.getAbilities().flying) {
                this.ownerFlying = 1;
                this.setActivity(1);
            }
            if (this.distanceToSqr(owner) > 400.0) {
                this.setActivity(1);
            }
        }

        if (this.random.nextInt(50) == 1 && !this.isOrderedToSit()
                && !this.targetInSight && this.getPassengers().isEmpty()) {
            if (this.random.nextInt(15) == 1) {
                this.setActivity(1);
            } else {
                this.setActivity(0);
            }
        }
    }

    private void flyWithoutRider() {
        if (this.level().isClientSide) return;
        if (this.isOrderedToSit()) return;
        if (!this.getPassengers().isEmpty()) return;

        boolean doNew = false;

        if (this.currentFlightTarget == null) {
            doNew = true;
            this.currentFlightTarget = this.blockPosition();
        }

        if (this.unstickTimer > 0) --this.unstickTimer;

        if (this.lastX == (int) this.getX() && this.lastZ == (int) this.getZ()) {
            ++this.stuckCount;
            if (this.stuckCount > 50) {
                this.stuckCount = 0;
                this.unstickTimer = 100;
                this.targetInSight = false;
                this.setAttacking(0);
                this.setActivity(1);
                doNew = true;
            }
        } else {
            this.stuckCount = 0;
            this.lastX = (int) this.getX();
            this.lastZ = (int) this.getZ();
        }

        Vec3 motion = this.getDeltaMovement();
        double dampedY;
        if (this.getY() < this.currentFlightTarget.getY() + 2.0) {
            dampedY = motion.y * 0.7;
        } else if (this.getY() > this.currentFlightTarget.getY() - 2.0) {
            dampedY = motion.y * 0.5;
        } else {
            dampedY = motion.y * 0.61;
        }
        this.setDeltaMovement(motion.x, dampedY, motion.z);

        if (this.random.nextInt(300) == 1) doNew = true;

        boolean tooFarFromOwner = false;
        double ownerX = this.getX();
        double ownerY = this.getY();
        double ownerZ = this.getZ();
        boolean hasOwner = false;

        if (this.isTame() && this.getOwner() != null) {
            LivingEntity owner = this.getOwner();
            hasOwner = true;
            ownerX = owner.getX();
            ownerY = owner.getY();
            ownerZ = owner.getZ();
            if (this.distanceToSqr(owner) > 144.0) {
                tooFarFromOwner = true;
                this.targetInSight = false;
                this.setAttacking(0);
                this.flyaway = 0;
                doNew = true;
            }
        }

        if (this.flyaway > 0) --this.flyaway;

        if (!tooFarFromOwner && this.unstickTimer == 0 && this.flyaway == 0
                && this.level().getDifficulty() != Difficulty.PEACEFUL
                && this.random.nextInt(8) == 1) {
            LivingEntity target = findSomethingToAttack();
            if (target != null) {
                if (this.isTame() && this.getHealth() / this.getMaxHealth() < 0.25f) {
                    this.setActivity(1);
                    this.setAttacking(0);
                    this.targetInSight = false;
                    doNew = false;
                    this.currentFlightTarget = new BlockPos(
                            (int) (this.getX() + (this.getX() - target.getX())),
                            (int) (this.getY() + 1),
                            (int) (this.getZ() + (this.getZ() - target.getZ())));
                } else {
                    this.setActivity(1);
                    this.setAttacking(1);
                    this.targetInSight = true;
                    this.currentFlightTarget = target.blockPosition().above();
                    doNew = false;
                    float attackRange = 7.0f + target.getBbWidth() / 2.0f;
                    if (this.distanceToSqr(target) < attackRange * attackRange) {
                        this.doHurtTarget(target);
                    }
                }
            } else {
                this.targetInSight = false;
                this.flyaway = 0;
                this.setAttacking(0);
            }
        }

        double distSq = this.currentFlightTarget.distSqr(this.blockPosition());
        if (distSq < 17.0) doNew = true;

        if (doNew && (!this.targetInSight || this.flyaway != 0)) {
            int keepTrying = 50;
            while (keepTrying > 0) {
                int gox = (int) this.getX();
                int goy = (int) this.getY();
                int goz = (int) this.getZ();
                int xdir, zdir;

                if (hasOwner && this.unstickTimer == 0) {
                    gox = (int) ownerX;
                    goy = (int) ownerY;
                    goz = (int) ownerZ;
                    if (this.ownerFlying == 0) {
                        xdir = this.random.nextInt(12) + 6;
                        zdir = this.random.nextInt(12) + 6;
                    } else {
                        xdir = this.random.nextInt(8);
                        zdir = this.random.nextInt(8);
                    }
                } else {
                    xdir = this.random.nextInt(20) + 6;
                    zdir = this.random.nextInt(20) + 6;
                }

                if (this.random.nextInt(2) == 1) xdir = -xdir;
                if (this.random.nextInt(2) == 1) zdir = -zdir;

                BlockPos newTarget = new BlockPos(
                        gox + xdir,
                        goy + this.random.nextInt(9 + this.ownerFlying * 2) - 4,
                        goz + zdir);

                if (this.level().getBlockState(newTarget).isAir()) {
                    this.currentFlightTarget = newTarget;
                    break;
                }
                --keepTrying;
            }
        }

        double speedFactor = 0.5;
        if (this.ownerFlying != 0) {
            speedFactor = 1.75;
            if (this.isTame() && this.getOwner() != null
                    && this.distanceToSqr(this.getOwner()) > 49.0) {
                speedFactor = 3.5;
            }
        }

        double dx = this.currentFlightTarget.getX() + 0.5 - this.getX();
        double dy = this.currentFlightTarget.getY() + 0.1 - this.getY();
        double dz = this.currentFlightTarget.getZ() + 0.5 - this.getZ();

        motion = this.getDeltaMovement();
        double newMx = motion.x + (Math.signum(dx) - motion.x) * 0.15 * speedFactor;
        double newMy = motion.y + (Math.signum(dy) - motion.y) * 0.21 * speedFactor;
        double newMz = motion.z + (Math.signum(dz) - motion.z) * 0.15 * speedFactor;
        this.setDeltaMovement(newMx, newMy, newMz);

        float targetYaw = (float) (Math.atan2(newMz, newMx) * 180.0 / Math.PI) - 90.0f;
        float yawDiff = Mth.wrapDegrees(targetYaw - this.getYRot());
        this.zza = (float) (0.75 * speedFactor);
        this.setYRot(this.getYRot() + yawDiff / 5.0f);
    }

    @Nullable
    private LivingEntity findSomethingToAttack() {
        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(20.0, 20.0, 20.0));
        // OPT-021: nearest-first pick without the full list sort; TargetSelection
        // preserves the removed sort's order and stable-tie semantics exactly.
        return TargetSelection.firstMatch(entities, Comparator.comparingDouble(this::distanceToSqr), this::isSuitableTarget);
    }

    /**
     * orig Leon.java:387-428. ENT-S-106: the shared {@code MyUtils.isIgnoreable}
     * screen (:403-405) sits after the null / self / dead checks (:394-402) and
     * ahead of line of sight (:406-408). ENT-S-107: the player branch's
     * {@code capabilities.isCreativeMode} (:417) is {@code Abilities.instabuild}
     * — the port's own Kraken / TheKing idiom — not {@code invulnerable}; the two
     * differ for a survival player made invulnerable by other means.
     * ENT-S-110: the PlayNicely gate (:391-393, {@code OreSpawnMain.PlayNicely != 0}
     * → false, read live as {@code OreSpawnConfig.PLAY_NICELY}) sits after Peaceful
     * (:388-390) and ahead of the null check (:394), and the untamed tail (:422-426)
     * grants only {@code MyUtils.isAttackableNonMob} targets ({@link #isAttackableNonMob},
     * the orig membership), everything else falling through to :427's false — the
     * port used to grant any living thing to an untamed Leon.
     */
    private boolean isSuitableTarget(LivingEntity target) {
        if (this.level().getDifficulty() == Difficulty.PEACEFUL) return false;
        if (OreSpawnConfig.PLAY_NICELY.get()) return false; // orig Leon.java:391-393 PlayNicely != 0 (ENT-S-110)
        if (target == null || target == this || !target.isAlive()) return false;
        if (MyUtils.isIgnoreable(target)) return false; // orig Leon.java:403-405 — the shared ignore screen (ENT-S-106)
        if (!this.getSensing().hasLineOfSight(target)) return false;
        if (target instanceof EntityLeon) return false;
        if (target instanceof Monster) return true;
        if (target instanceof Player player) {
            if (player.getAbilities().instabuild) return false; // orig Leon.java:417 isCreativeMode (ENT-S-107)
            return !this.isTame();
        }
        if (!this.isTame() && isAttackableNonMob(target)) return true; // orig Leon.java:422-426 — the untamed tail, attackable non-mobs only (ENT-S-110)
        return false; // orig Leon.java:427
    }

    /**
     * orig MyUtils.java:77-115 {@code isAttackableNonMob}, the membership the untamed
     * Leon's tail (orig Leon.java:422-426) grants, reproduced here in the orig order
     * (ENT-S-110). The port's {@code MyUtils.isAttackableNonMob} carries a different set
     * (EnderDragon / Kraken / Godzilla / GodzillaHead / Basilisk / Cephadrome / TheKing /
     * TheQueen), so it is not used here. Mapping: EntityMob → {@link Monster} (:78;
     * unreachable from the Leon, whose :412 has already granted every Monster), Mothra (:81;
     * unreachable too — an EntityButterfly in both trees, screened by :403), Leon →
     * EntityLeon (:84; unreachable, :409 refuses every Leon), Dragon (:87), Spyro →
     * EntitySpyro (:90), the royalty set (:93, {@code MyUtils.isRoyalty} — the same nine
     * members as orig MyUtils.java:46-75), GammaMetroid → EntityGammaMetroid (:96),
     * Cephadrome (:99), WaterDragon (:102), Girlfriend (:105), Boyfriend (:108),
     * EntityVillager → {@link Villager} (:111), Stinky → EntityStinky (:114).
     */
    private static boolean isAttackableNonMob(LivingEntity target) {
        return target instanceof Monster                // orig MyUtils.java:78 EntityMob
                || target instanceof Mothra             // orig MyUtils.java:81
                || target instanceof EntityLeon         // orig MyUtils.java:84 Leon
                || target instanceof Dragon             // orig MyUtils.java:87
                || target instanceof EntitySpyro        // orig MyUtils.java:90 Spyro
                || MyUtils.isRoyalty(target)            // orig MyUtils.java:93 isRoyalty (:46-75)
                || target instanceof EntityGammaMetroid // orig MyUtils.java:96 GammaMetroid
                || target instanceof Cephadrome         // orig MyUtils.java:99
                || target instanceof WaterDragon        // orig MyUtils.java:102
                || target instanceof Girlfriend         // orig MyUtils.java:105
                || target instanceof Boyfriend          // orig MyUtils.java:108
                || target instanceof Villager           // orig MyUtils.java:111 EntityVillager
                || target instanceof EntityStinky;      // orig MyUtils.java:114 Stinky
    }

    // ==================== Interaction ====================

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (stack.is(Blocks.DIAMOND_BLOCK.asItem()) && this.distanceToSqr(player) < 49.0) {
            if (!this.level().isClientSide) {
                if (!this.isTame()) {
                    this.tame(player);
                }
                this.setOrderedToSit(false);
                this.setInSittingPose(false);
                this.level().broadcastEntityEvent(this, (byte) 7);
                this.heal(this.getMaxHealth() - this.getHealth());
            }
            if (!player.getAbilities().instabuild) stack.shrink(1);
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        if (stack.is(Items.BEEF) && this.distanceToSqr(player) < 49.0) {
            if (!this.isTame()) {
                if (!this.level().isClientSide) {
                    if (this.random.nextInt(3) == 1) {
                        this.tame(player);
                        this.level().broadcastEntityEvent(this, (byte) 7);
                        this.heal(this.getMaxHealth() - this.getHealth());
                    } else {
                        this.level().broadcastEntityEvent(this, (byte) 6);
                    }
                }
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            } else if (this.isOwnedBy(player)) {
                if (!this.level().isClientSide) {
                    this.level().broadcastEntityEvent(this, (byte) 7);
                }
                if (this.getMaxHealth() > this.getHealth()) {
                    this.heal(this.getMaxHealth() - this.getHealth());
                }
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }
        }

        if (this.isTame() && this.isOwnedBy(player)) {
            if (stack.is(Blocks.DEAD_BUSH.asItem()) && this.distanceToSqr(player) < 49.0) { // orig Leon.java:1035 untame item is dead bush (field_150330_I)
                if (!this.level().isClientSide) {
                    this.setTame(false, false);
                    this.setOwnerUUID(null);
                    this.setOrderedToSit(false);
                    this.setInSittingPose(false);
                    this.level().broadcastEntityEvent(this, (byte) 6);
                }
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }

            if (stack.is(Items.NAME_TAG) && this.distanceToSqr(player) < 49.0) {
                this.setCustomName(stack.getHoverName());
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }

            if (stack.isEmpty() && this.distanceToSqr(player) < 49.0) {
                if (!this.level().isClientSide) {
                    player.startRiding(this);
                    this.setActivity(1);
                    this.setOrderedToSit(false);
                    this.setInSittingPose(false);
                }
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }

            if (!stack.isEmpty() && this.distanceToSqr(player) < 49.0
                    && this.getPassengers().isEmpty()) {
                this.setOrderedToSit(!this.isOrderedToSit());
                this.setInSittingPose(this.isOrderedToSit());
                if (this.isOrderedToSit()) {
                    this.setActivity(0);
                }
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.BEEF);
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return null;
    }


    /** orig Leon.java:452-478 — "Leonopteryx" spawner bypass; 1-in-16 dice; daytime; no other Leon within 48/16/48; y>=50. */
    @Override
    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level,
                                   net.minecraft.world.entity.MobSpawnType spawnType) {
        if (OriginalSpawnGates.nearOwnSpawner(this, level)) return true;
        if (this.getRandom().nextInt(16) != 0) return false;
        if (!OriginalSpawnGates.isDaytime(level)) return false;
        if (OriginalSpawnGates.anyOtherNearby(this, level, EntityLeon.class, 48.0, 16.0, 48.0)) return false;
        return this.getY() >= 50.0;
    }
}
