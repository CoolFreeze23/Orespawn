package danger.orespawn.entity;

import java.util.Comparator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.Difficulty;
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
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.ai.TargetSelection;

public class ThePrince extends TamableAnimal {
    // OPT-011: cached SoundEvents — identical createVariableRangeEvent ids,
    // allocated once per class instead of on every sound query.
    private static final SoundEvent SND_ROAR = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "roar"));
    private static final SoundEvent SND_DUCK_HURT = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "duck_hurt"));
    private static final SoundEvent SND_CRYO_DEATH = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "cryo_death"));
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
    /** orig ThePrince.java:309-314 ({@code set_ok_to_grow}) — called on diamond regression from the teen. */
    public void setOkToGrow() {
        this.okToGrow = 1;
        this.killCount = 0;
        this.fedCount = 0;
        this.dayCount = 0;
    }

    /** orig ThePrince.java:63 — set while the owner is creative-flying; speeds flight up. */
    private int ownerFlying = 0;
    /** orig ThePrince.java:60 — the flight steering target (transient, like the original). */
    private BlockPos.MutableBlockPos currentFlightTarget = null;

    public ThePrince(EntityType<? extends ThePrince> type, Level level) {
        super(type, level);
        this.xpReward = 50;
        this.noPhysics = false;
        this.setOrderedToSit(false);
        // TF-035: orig ThePrince.java:61/:93 — GenericTargetSorter.
        this.targetSorter = new danger.orespawn.entity.ai.GenericTargetSorter(this);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new FollowOwnerGoal(this, 1.15, 12.0f, 2.0f));
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.25, Ingredient.of(Items.BEEF), false));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Mob.class, 6.0f));
        this.goalSelector.addGoal(5, new MyEntityAIWander(this, 0.75f));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Monster.class, 10, true, false, e -> this.isTame()));
    }

    public static AttributeSupplier.Builder createAttributes() {
        // orig ThePrince.java:186 HP 500, :102 ATK 10, :347 armor 16, :81 speed 0.32 — all confirmed.
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
        // orig ThePrince.java:423 — activity 2 (flying) ghosts through terrain.
        // Safe now that do_movement is ported: flight steering pulls the prince
        // to air-block targets and the 1/100 activity re-roll (orig :533-539)
        // restores physics (resolves the BUG-010 interim disable; PN-002).
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

    /**
     * orig ThePrince.java:483-505 (func_70636_d) — buoyancy while swimming and
     * a 0.6 vertical damping factor while flying. (The original's syncit
     * DataWatcher refresh is obsolete: SynchedEntityData handles it.)
     */
    @Override
    public void aiStep() {
        super.aiStep();
        if (this.isInWater()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0, 0.07, 0.0));
        }
        if (this.getActivity() == 2) {
            Vec3 motion = this.getDeltaMovement();
            this.setDeltaMovement(motion.x, motion.y * 0.6, motion.z);
        }
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

        // orig ThePrince.java:513-515 — periodically forgive the revenge target
        // (setRevengeTarget(null), not the attack target).
        if (this.random.nextInt(200) == 1) {
            this.setLastHurtByMob(null);
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

        if (!this.isOrderedToSit()) {
            // orig ThePrince.java:529-539 — activity cycling: 1/100 roll re-picks
            // the state, 1/20 of those start flying (2), otherwise land (1).
            if (this.getActivity() == 0) {
                this.setActivity(1);
            }
            if (this.random.nextInt(100) == 1) {
                this.setActivity(this.random.nextInt(20) == 1 ? 2 : 1);
            }
            // orig ThePrince.java:540-547 — a creative-flying owner pulls the
            // prince into the air and marks fast-follow mode.
            this.ownerFlying = 0;
            LivingEntity owner = this.getOwner();
            if (this.isTame() && owner instanceof Player ownerPlayer && ownerPlayer.getAbilities().flying) {
                this.ownerFlying = 1;
                this.setActivity(2);
            }
            // orig ThePrince.java:548-550 — grounded prince takes flight when the
            // owner is over 16 blocks away.
            if (this.getActivity() == 1 && this.isTame() && owner != null
                    && this.distanceToSqr(owner) > 256.0) {
                this.setActivity(2);
            }
            this.doMovement();
        } else {
            // orig ThePrince.java:552-555 — a sitting prince breaks the sit and
            // flies after an owner who leaves it behind.
            LivingEntity owner = this.getOwner();
            if (this.isTame() && owner != null && this.distanceToSqr(owner) > 256.0) {
                this.setOrderedToSit(false);
                this.setInSittingPose(false);
                this.setActivity(2);
            }
        }

        // orig ThePrince.java:556-568 — natural growth has no ok_to_grow gate;
        // the counters alone decide (closes BOSS-021).
        if (this.killCount > 25 && this.fedCount > 10 && this.dayCount > 10) {
            this.transformToTeen();
            return;
        }

        if (this.isDay == 0) {
            this.isDay = 1;
            if (!this.level().isDay()) this.isDay = -1;
        } else {
            if (this.isDay == -1 && this.level().isDay()) ++this.dayCount;
            this.isDay = this.level().isDay() ? 1 : -1;
        }
    }

    /**
     * orig ThePrince.java:585-725 ({@code do_movement}) — the per-tick brain:
     * a 1-in-7 combat roll (melee bite, or the fire/lightning/ice canon trio
     * gated on a 0.5 rad head-bearing check), retreat-when-hurt for tame
     * princes, and flight-target steering. Steering only runs while flying
     * (activity 2); on the ground (activity 1) the method exits after the
     * combat roll (orig :670-672) and vanilla goals drive movement.
     */
    private void doMovement() {
        boolean doNew = false;
        boolean hasOwner = false;
        double ox = 0.0;
        double oy = 0.0;
        double oz = 0.0;
        if (this.currentFlightTarget == null) {
            doNew = true;
            this.currentFlightTarget = new BlockPos.MutableBlockPos(
                    (int) this.getX(), (int) this.getY(), (int) this.getZ());
        }
        // orig :603-605 — 1-in-300 wanderlust re-roll while flying.
        if (this.getActivity() == 2 && this.random.nextInt(300) == 0) {
            doNew = true;
        }
        LivingEntity owner = this.getOwner();
        if (this.isTame() && owner != null) {
            hasOwner = true;
            ox = owner.getX();
            oy = owner.getY() + 1.0;
            oz = owner.getZ();
            // orig :612-617 — re-target when straying: >10 blocks normally,
            // >6 blocks when chasing a flying owner.
            if (this.distanceToSqr(owner) > 100.0) {
                doNew = true;
            }
            if (this.ownerFlying != 0 && this.distanceToSqr(owner) > 36.0) {
                doNew = true;
            }
        }
        // orig :619-669 — 1-in-7 combat roll outside Peaceful.
        if (this.random.nextInt(7) == 1 && this.level().getDifficulty() != Difficulty.PEACEFUL) {
            LivingEntity target = this.findSomethingToAttack();
            if (target != null) {
                if (this.isTame() && this.getHealth() / this.getMaxHealth() < 0.25f) {
                    // orig :622-626 — a badly hurt tame prince flees directly
                    // away from the threat.
                    this.setActivity(2);
                    this.setAttacking(0);
                    doNew = false;
                    this.currentFlightTarget.set(
                            (int) (this.getX() + (this.getX() - target.getX())),
                            (int) (this.getY() + 1.0),
                            (int) (this.getZ() + (this.getZ() - target.getZ())));
                } else {
                    this.setActivity(2);
                    this.setAttacking(1);
                    this.currentFlightTarget.set(
                            (int) target.getX(), (int) (target.getY() + 1.0), (int) target.getZ());
                    doNew = false;
                    float meleeRange = 3.0f + target.getBbWidth() / 2.0f;
                    double distSq = this.distanceToSqr(target);
                    if (distSq < (double) (meleeRange * meleeRange)) {
                        this.doHurtTarget(target);
                    } else if (distSq > 25.0 && distSq < 144.0 && !this.isInWater()
                            && this.getSpyroFire() != 0
                            && (this.random.nextInt(3) == 0 || this.random.nextInt(4) == 1)) {
                        // orig :634-663 — random head, and fire only once that head
                        // has swung to within 0.5 rad of the target bearing.
                        int which = this.random.nextInt(3);
                        double heading = Math.atan2(target.getZ() - this.getZ(), target.getX() - this.getX());
                        double facing = Math.toRadians((this.getYRot() + 90.0f) % 360.0f);
                        double diff = Math.abs(heading - facing) % (Math.PI * 2.0);
                        if (diff > Math.PI) {
                            diff -= Math.PI * 2.0;
                        }
                        if (Math.abs(diff) < 0.5) {
                            if (which == 0) {
                                this.firecanon(target);
                            } else if (which == 1) {
                                this.firecanonl(target);
                            } else {
                                this.firecanoni(target);
                            }
                        }
                    }
                }
            } else {
                this.setAttacking(0);
            }
        }
        // orig :670-672 — grounded: vanilla AI moves the prince.
        if (this.getActivity() == 1) {
            return;
        }
        if (this.currentFlightTarget.distSqr(new net.minecraft.core.Vec3i(
                (int) this.getX(), (int) this.getY(), (int) this.getZ())) < 2.1) {
            doNew = true;
        }
        if (doNew) {
            // orig :676-707 — pick a random air block near self (or the owner),
            // tighter spread when the owner is flying; up to 10 tries.
            int keepTrying = 10;
            boolean foundAir = false;
            while (!foundAir && keepTrying != 0) {
                int gox = (int) this.getX();
                int goy = (int) this.getY();
                int goz = (int) this.getZ();
                int xdir;
                int zdir;
                if (hasOwner) {
                    gox = (int) ox;
                    goy = (int) oy;
                    goz = (int) oz;
                    if (this.ownerFlying == 0) {
                        zdir = this.random.nextInt(4) + 6;
                        xdir = this.random.nextInt(4) + 6;
                    } else {
                        zdir = this.random.nextInt(8);
                        xdir = this.random.nextInt(8);
                    }
                } else {
                    zdir = this.random.nextInt(5) + 6;
                    xdir = this.random.nextInt(5) + 6;
                }
                if (this.random.nextInt(2) == 0) {
                    zdir = -zdir;
                }
                if (this.random.nextInt(2) == 0) {
                    xdir = -xdir;
                }
                this.currentFlightTarget.set(gox + xdir,
                        goy + (this.random.nextInt(6 + this.ownerFlying * 2) - 2), goz + zdir);
                foundAir = this.level().isEmptyBlock(this.currentFlightTarget);
                --keepTrying;
            }
        }
        // orig :708-724 — signum steering toward the target; 1.75× speed chasing
        // a flying owner, 3.5× when also more than 7 blocks behind.
        double speedFactor = 1.0;
        double dx = this.currentFlightTarget.getX() + 0.5 - this.getX();
        double dy = this.currentFlightTarget.getY() + 0.1 - this.getY();
        double dz = this.currentFlightTarget.getZ() + 0.5 - this.getZ();
        if (this.ownerFlying != 0) {
            speedFactor = 1.75;
            if (this.isTame() && owner != null && this.distanceToSqr(owner) > 49.0) {
                speedFactor = 3.5;
            }
        }
        Vec3 motion = this.getDeltaMovement();
        double mx = motion.x + (Math.signum(dx) * 0.5 - motion.x) * 0.15 * speedFactor;
        double my = motion.y + (Math.signum(dy) * 0.7 - motion.y) * 0.21 * speedFactor;
        double mz = motion.z + (Math.signum(dz) * 0.5 - motion.z) * 0.15 * speedFactor;
        this.setDeltaMovement(mx, my, mz);
        float targetYaw = (float) (Math.atan2(mz, mx) * 180.0 / Math.PI) - 90.0f;
        float yawDelta = Mth.wrapDegrees(targetYaw - this.getYRot());
        this.zza = (float) (0.75 * speedFactor);
        this.setYRot(this.getYRot() + yawDelta / 3.0f);
    }

    /**
     * orig ThePrince.java:782-800 ({@code firecanon}) — head 1: a big
     * BetterFireball (halved to small 1-in-2) from muzzle xz 3.0 / y 1.0,
     * aim jittered ±5/±3/±5 blocks, "random.bow" sound.
     */
    private void firecanon(LivingEntity target) {
        double yoff = 1.0;
        double xzoff = 3.0;
        double cx = this.getX() - xzoff * Math.sin(Math.toRadians(this.getYRot()));
        double cz = this.getZ() + xzoff * Math.cos(Math.toRadians(this.getYRot()));
        float r1 = 5.0f * (this.random.nextFloat() - this.random.nextFloat());
        float r2 = 3.0f * (this.random.nextFloat() - this.random.nextFloat());
        float r3 = 5.0f * (this.random.nextFloat() - this.random.nextFloat());
        Vec3 dir = new Vec3(target.getX() - cx + r1,
                target.getY() + target.getBbHeight() / 2.0f - (this.getY() + yoff) + r2,
                target.getZ() - cz + r3);
        BetterFireball bf = new BetterFireball(this.level(), this, dir);
        bf.moveTo(cx, this.getY() + yoff, cz, this.getYRot(), 0.0f);
        bf.setBig();
        if (this.random.nextInt(2) == 1) {
            bf.setSmall();
        }
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.ARROW_SHOOT, this.getSoundSource(), 1.0f,
                1.0f / (this.random.nextFloat() * 0.4f + 0.8f));
        this.level().addFreshEntity(bf);
    }

    /**
     * orig ThePrince.java:802-826 ({@code firecanonl}) — head 2: a ThunderBolt
     * launched at 1.4f/4.0f with a 0.2×distance arc, then tripled.
     */
    private void firecanonl(LivingEntity target) {
        double yoff = 1.0;
        double xzoff = 3.0;
        double cx = this.getX() - xzoff * Math.sin(Math.toRadians(this.getYRot()));
        double cz = this.getZ() + xzoff * Math.cos(Math.toRadians(this.getYRot()));
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.ARROW_SHOOT, this.getSoundSource(), 1.0f,
                1.0f / (this.random.nextFloat() * 0.4f + 0.8f));
        ThunderBolt tb = new ThunderBolt(this.level(), cx, this.getY() + yoff, cz);
        tb.moveTo(cx, this.getY() + yoff, cz, 0.0f, 0.0f);
        double dx = target.getX() - tb.getX();
        double dy = target.getY() + 0.25 - tb.getY();
        double dz = target.getZ() - tb.getZ();
        double arc = Math.sqrt(dx * dx + dz * dz) * 0.2f;
        tb.shoot(dx, dy + arc, dz, 1.4f, 4.0f);
        tb.setDeltaMovement(tb.getDeltaMovement().scale(3.0));
        this.level().addFreshEntity(tb);
    }

    /**
     * orig ThePrince.java:828-853 ({@code firecanoni}) — head 3: an ice-making
     * IceBall launched at 1.4f/4.0f with a 0.2×distance arc, then tripled.
     */
    private void firecanoni(LivingEntity target) {
        double yoff = 1.0;
        double xzoff = 3.0;
        double cx = this.getX() - xzoff * Math.sin(Math.toRadians(this.getYRot()));
        double cz = this.getZ() + xzoff * Math.cos(Math.toRadians(this.getYRot()));
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.ARROW_SHOOT, this.getSoundSource(), 1.0f,
                1.0f / (this.random.nextFloat() * 0.4f + 0.8f));
        IceBall ib = new IceBall(ModEntities.ICE_BALL.get(), this.level());
        ib.setOwner(this);
        ib.enableIceCreation();
        ib.moveTo(cx, this.getY() + yoff, cz, 0.0f, 0.0f);
        double dx = target.getX() - ib.getX();
        double dy = target.getY() + 0.25 - ib.getY();
        double dz = target.getZ() - ib.getZ();
        double arc = Math.sqrt(dx * dx + dz * dz) * 0.2f;
        ib.shoot(dx, dy + arc, dz, 1.4f, 4.0f);
        ib.setDeltaMovement(ib.getDeltaMovement().scale(3.0));
        this.level().addFreshEntity(ib);
    }

    private void transformToTeen() {
        if (this.level().isClientSide || !(this.level() instanceof ServerLevel serverLevel)) return;
        ThePrinceTeen teen = ModEntities.THE_PRINCE_TEEN.get().create(serverLevel);
        if (teen == null) return;
        teen.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
        if (this.isTame() && this.getOwnerUUID() != null) {
            Player owner = this.level().getPlayerByUUID(this.getOwnerUUID());
            if (owner != null) {
                teen.tame(owner);
            } else {
                // Owner offline: tame(null) would NPE (BUG-004). Growth is timer-driven
                // and must not require the owner online — carry the UUID over directly.
                teen.setOwnerUUID(this.getOwnerUUID());
                teen.setTame(true, true);
            }
        }
        serverLevel.addFreshEntity(teen);
        this.discard();
    }

    private boolean isSuitableTarget(LivingEntity target) {
        if (this.level().getDifficulty() == Difficulty.PEACEFUL) return false; // orig :728-730
        if (target == null || target == this || !target.isAlive()) return false;
        if (!this.getSensing().hasLineOfSight(target)) return false;
        if (MyUtils.isRoyalty(target)) return false;
        if (target instanceof Monster) return true; // orig ThePrince.java:746-747
        // orig ThePrince.java:749-761 — Mothra and the insects are PREY, not excluded.
        return target instanceof Mothra || target instanceof EntityButterfly
                || target instanceof Cockateil || target instanceof EntityDragonfly
                || target instanceof EntityMosquito;
    }

    private LivingEntity findSomethingToAttack() {
        if (OreSpawnConfig.PLAY_NICELY.get()) return null; // orig ThePrince.java:765-767
        AABB searchBox = this.getBoundingBox().inflate(12.0, 6.0, 12.0);
        List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, searchBox);
        // OPT-021: nearest-first pick without the full list sort; TargetSelection
        // preserves the removed sort's order and stable-tie semantics exactly.
        return TargetSelection.firstMatch(targets, this.targetSorter, this::isSuitableTarget);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (stack.is(Blocks.DIAMOND_BLOCK.asItem()) && this.distanceToSqr(player) < 16.0) {
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

        // orig ThePrince.java:267-286 — DIAMOND (not gold ingot) triggers the
        // teen transform when ok_to_grow is set; the original has no cake
        // shortcut on the baby (BOSS-020).
        if (stack.is(Items.DIAMOND) && this.isOwnedBy(player)
                && this.distanceToSqr(player) < 16.0 && this.okToGrow != 0) {
            if (!this.level().isClientSide) {
                this.transformToTeen();
            }
            if (!player.getAbilities().instabuild) stack.shrink(1);
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        if (this.isTame() && this.isOwnedBy(player)
                && this.distanceToSqr(player) < 16.0
                && stack.has(net.minecraft.core.component.DataComponents.FOOD)) {
            if (!this.level().isClientSide) {
                if (this.getMaxHealth() > this.getHealth()) {
                    // orig ThePrince.java:219 — any food heals healAmount × 10.
                    FoodProperties food = stack.get(net.minecraft.core.component.DataComponents.FOOD);
                    this.heal(food.nutrition() * 10.0f);
                }
                this.level().broadcastEntityEvent(this, (byte) 7);
                ++this.fedCount;
            }
            if (!player.getAbilities().instabuild) stack.shrink(1);
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        // orig ThePrince.java:233-249 — an ice block extinguishes the fireballs.
        if (this.isTame() && this.isOwnedBy(player)
                && this.distanceToSqr(player) < 16.0
                && stack.is(Blocks.ICE.asItem())) {
            if (!this.level().isClientSide) {
                this.level().broadcastEntityEvent(this, (byte) 6);
                this.setSpyroFire(0);
                player.displayClientMessage(Component.literal("Prince fireballs extinguished."), false);
            }
            if (!player.getAbilities().instabuild) stack.shrink(1);
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        // orig ThePrince.java:250-266 — flint & steel relights them.
        if (this.isTame() && this.isOwnedBy(player)
                && this.distanceToSqr(player) < 16.0
                && stack.is(Items.FLINT_AND_STEEL)) {
            if (!this.level().isClientSide) {
                this.level().broadcastEntityEvent(this, (byte) 6);
                this.setSpyroFire(1);
                player.displayClientMessage(Component.literal("Prince fireballs lit!"), false);
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
        return SND_ROAR;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SND_DUCK_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SND_CRYO_DEATH;
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

    /** orig ThePrince.java:381-383 — always allowed. */
    @Override
    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level,
                                   net.minecraft.world.entity.MobSpawnType spawnType) {
        return true;
    }
}
