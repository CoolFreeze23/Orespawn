package danger.orespawn.entity;

import java.util.Comparator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import danger.orespawn.ModBlocks;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.util.MyUtils;
import danger.orespawn.entity.ai.TargetSelection;

public class ThePrincess extends TamableAnimal {
    // OPT-011: cached SoundEvents — identical createVariableRangeEvent ids,
    // allocated once per class instead of on every sound query.
    private static final SoundEvent SND_ROAR = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "roar"));
    private static final SoundEvent SND_DUCK_HURT = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "duck_hurt"));
    private static final SoundEvent SND_CRYO_DEATH = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "cryo_death"));
    private static final EntityDataAccessor<Integer> DATA_ACTIVITY =
            SynchedEntityData.defineId(ThePrincess.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_FIRE =
            SynchedEntityData.defineId(ThePrincess.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(ThePrincess.class, EntityDataSerializers.INT);
    /** orig ThePrincess.java:111/117-123 (DataWatcher 23) — synched copy of attack_level for the client aura. */
    private static final EntityDataAccessor<Integer> DATA_POWER =
            SynchedEntityData.defineId(ThePrincess.class, EntityDataSerializers.INT);

    private final Comparator<Entity> targetSorter;
    private final float moveSpeed = 0.3f;
    private int head1ext = 0, head2ext = 0, head3ext = 0;
    private int head1dir = 1, head2dir = 1, head3dir = 1;
    private int okToGrow = 0;
    private int killCount = 0;
    private int fedCount = 0;
    private int dayCount = 0;
    private int isDay = 0;
    /** orig ThePrincess.java:75 — the magic charge meter behind the bloom/PurplePower system. */
    private int attackLevel = 1;
    /** orig ThePrincess.java:76 — power sync ticker (every 10 AI steps). */
    private int ticker = 0;
    /** orig ThePrincess.java:61 — set while the owner is creative-flying; speeds flight up. */
    private int ownerFlying = 0;
    /** orig ThePrincess.java:58 — the flight steering target (transient, like the original). */
    private BlockPos.MutableBlockPos currentFlightTarget = null;

    public ThePrincess(EntityType<? extends ThePrincess> type, Level level) {
        super(type, level);
        this.xpReward = 50;
        this.noPhysics = false;
        this.setOrderedToSit(false);
        // TF-035: orig ThePrincess.java:59/:93 — GenericTargetSorter.
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
        // orig ThePrincess.java:195 HP 400, :102 ATK 10, :335 armor 14, :81 speed 0.32.
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 400.0)
                .add(Attributes.MOVEMENT_SPEED, 0.32)
                .add(Attributes.ATTACK_DAMAGE, 10.0)
                .add(Attributes.ARMOR, 14.0)
                .add(Attributes.FOLLOW_RANGE, 24.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ACTIVITY, 1);
        builder.define(DATA_FIRE, 1);
        builder.define(DATA_ATTACKING, 0);
        builder.define(DATA_POWER, 1); // orig ThePrincess.java:111 — attack_level starts at 1.
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) { return false; }

    public int getActivity() { return this.entityData.get(DATA_ACTIVITY); }
    public void setActivity(int value) { this.entityData.set(DATA_ACTIVITY, value); }
    public int getAttacking() { return this.entityData.get(DATA_ATTACKING); }
    public void setAttacking(int value) { this.entityData.set(DATA_ATTACKING, value); }
    public int getSpyroFire() { return this.entityData.get(DATA_FIRE); }
    public void setSpyroFire(int value) { this.entityData.set(DATA_FIRE, value); }
    public int getPower() { return this.entityData.get(DATA_POWER); }
    public void setPower(int value) { this.entityData.set(DATA_POWER, value); }
    public int getHead1Ext() { return this.head1ext; }
    public int getHead2Ext() { return this.head2ext; }
    public int getHead3Ext() { return this.head3ext; }

    @Override
    public void tick() {
        super.tick();
        // orig ThePrincess.java:411 — activity 2 (flying) ghosts through terrain.
        // Safe now that do_movement is ported (resolves the BUG-010 interim
        // disable; PN-002).
        this.noPhysics = this.getActivity() == 2;

        int i;
        if (this.random.nextInt(10) == 1) { i = this.random.nextInt(3); this.head1dir = i == 0 ? 2 : i == 1 ? -2 : 0; }
        if (this.random.nextInt(10) == 1) { i = this.random.nextInt(3); this.head2dir = i == 0 ? 2 : i == 1 ? -2 : 0; }
        if (this.random.nextInt(10) == 1) { i = this.random.nextInt(3); this.head3dir = i == 0 ? 2 : i == 1 ? -2 : 0; }
        this.head1ext = Math.max(0, Math.min(60, this.head1ext + this.head1dir));
        this.head2ext = Math.max(0, Math.min(60, this.head2ext + this.head2dir));
        this.head3ext = Math.max(0, Math.min(60, this.head3ext + this.head3dir));

        // orig ThePrincess.java:469-476 — client-side firework-spark aura once
        // the power charge passes 400: 1-in-6 per tick, two sparks trailing
        // 0.25 behind, drift ±gaussian/7 plus 3× her horizontal motion.
        if (this.level().isClientSide && this.getPower() > 400 && this.random.nextInt(6) == 1) {
            float f = 0.25f;
            Vec3 motion = this.getDeltaMovement();
            for (i = 0; i < 2; ++i) {
                this.level().addParticle(ParticleTypes.FIREWORK,
                        this.getX() - f * Math.sin(Math.toRadians(this.getYRot())),
                        this.getY() + 0.4,
                        this.getZ() + f * Math.cos(Math.toRadians(this.getYRot())),
                        (this.random.nextGaussian() - this.random.nextGaussian()) / 7.0 + motion.x * 3.0,
                        (this.random.nextGaussian() - this.random.nextGaussian()) / 7.0,
                        (this.random.nextGaussian() - this.random.nextGaussian()) / 7.0 + motion.z * 3.0);
            }
        }
    }

    /**
     * orig ThePrincess.java:479-501 (func_70636_d) — buoyancy while swimming
     * and a 0.6 vertical damping factor while flying.
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

    /** orig ThePrincess.java:377-389 — melee strength 9.0 (not the attribute), kills counted. */
    @Override
    public boolean doHurtTarget(Entity target) {
        boolean result = target.hurt(this.damageSources().mobAttack(this), 9.0f);
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

        // orig ThePrincess.java:512-514 — periodically forgive the revenge target.
        if (this.random.nextInt(200) == 1) {
            this.setLastHurtByMob(null);
        }

        if (this.getActivity() != 2) {
            super.customServerAiStep();
        }

        // orig ThePrincess.java:518-521 — sync the power meter every 10 AI steps.
        ++this.ticker;
        if (this.ticker % 10 == 0) {
            this.setPower(this.attackLevel);
        }

        if (this.random.nextInt(200) == 1 && this.getHealth() < this.getMaxHealth()) {
            this.heal(1.0f);
        }

        if (!this.isTame()) {
            Player nearestPlayer = this.level().getNearestPlayer(this, 10.0);
            if (nearestPlayer != null) {
                this.tame(nearestPlayer);
                this.level().broadcastEntityEvent(this, (byte) 7);
                this.heal(this.getMaxHealth() - this.getHealth());
            }
        }

        // orig ThePrincess.java:532-628 — the power system: charge +1/tick
        // (+4 more in combat, zeroed while extinguished); at >500 either a
        // triple PurplePower volley (in combat) or a terraforming bloom.
        ++this.attackLevel;
        if (this.getAttacking() != 0) {
            this.attackLevel += 4;
        }
        if (this.getSpyroFire() == 0) {
            this.attackLevel = 0;
        }
        if (this.attackLevel > 500) {
            if (this.getAttacking() != 0) {
                this.firePurplePower();
            } else {
                this.bloom();
            }
            this.attackLevel = 1;
        }

        if (!this.isOrderedToSit()) {
            // orig ThePrincess.java:629-639 — activity cycling: 1/100 roll
            // re-picks the state, 1/20 of those start flying (2).
            if (this.getActivity() == 0) {
                this.setActivity(1);
            }
            if (this.random.nextInt(100) == 1) {
                this.setActivity(this.random.nextInt(20) == 1 ? 2 : 1);
            }
            // orig ThePrincess.java:640-647 — a creative-flying owner pulls the
            // princess into the air and marks fast-follow mode.
            this.ownerFlying = 0;
            LivingEntity owner = this.getOwner();
            if (this.isTame() && owner instanceof Player ownerPlayer && ownerPlayer.getAbilities().flying) {
                this.ownerFlying = 1;
                this.setActivity(2);
            }
            // orig ThePrincess.java:648-650 — grounded princess takes flight
            // when the owner is over 16 blocks away.
            if (this.getActivity() == 1 && this.isTame() && owner != null
                    && this.distanceToSqr(owner) > 256.0) {
                this.setActivity(2);
            }
            this.doMovement();
        } else {
            // orig ThePrincess.java:652-655 — a sitting princess breaks the sit
            // and flies after an owner who leaves her behind.
            LivingEntity owner = this.getOwner();
            if (this.isTame() && owner != null && this.distanceToSqr(owner) > 256.0) {
                this.setOrderedToSit(false);
                this.setInSittingPose(false);
                this.setActivity(2);
            }
        }

        // orig ThePrincess.java:656-669 — day counting (saved, though the
        // princess has no growth transform).
        if (this.isDay == 0) {
            this.isDay = 1;
            if (!this.level().isDay()) this.isDay = -1;
        } else {
            if (this.isDay == -1 && this.level().isDay()) ++this.dayCount;
            this.isDay = this.level().isDay() ? 1 : -1;
        }
    }

    /**
     * orig ThePrincess.java:540-549 — in combat, the charged power vents as
     * three PurplePower orbs spawned 1.5 blocks ahead, inheriting 3× her
     * horizontal motion, each with a random type 1-3.
     */
    private void firePurplePower() {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;
        double xzoff = 1.5;
        double yoff = 1.0;
        for (int i = 0; i < 3; ++i) {
            PurplePower power = ModEntities.PURPLE_POWER.get().create(serverLevel);
            if (power == null) continue;
            power.moveTo(this.getX() - xzoff * Math.sin(Math.toRadians(this.getYRot())),
                    this.getY() + yoff,
                    this.getZ() + xzoff * Math.cos(Math.toRadians(this.getYRot())),
                    this.random.nextFloat() * 360.0f, 0.0f);
            Vec3 motion = this.getDeltaMovement();
            power.setDeltaMovement(motion.x * 3.0, power.getDeltaMovement().y, motion.z * 3.0);
            power.setPurpleType(1 + this.random.nextInt(3));
            serverLevel.addFreshEntity(power);
        }
    }

    /**
     * orig ThePrincess.java:550-626 — at peace, the charge blooms the land:
     * five column probes that plant flowers on grass (2 vanilla + 6 OreSpawn
     * kinds), regrow grass on dirt, cover stone with dirt, cactus/dirt on
     * sand, and calm lava into water/flowing water (all under mobGriefing);
     * then up to two Butterflies or Birds hatch nearby.
     */
    private void bloom() {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;
        if (serverLevel.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
            probe: for (int m = 0; m < 5; ++m) {
                int i = this.random.nextInt(5) - this.random.nextInt(5);
                int k = this.random.nextInt(5) - this.random.nextInt(5);
                for (int j = -5; j < 5; ++j) {
                    BlockPos pos = new BlockPos((int) this.getX() + i, (int) this.getY() + j, (int) this.getZ() + k);
                    BlockPos above = pos.above();
                    BlockState state = serverLevel.getBlockState(pos);
                    if (state.is(Blocks.GRASS_BLOCK)) {
                        if (!serverLevel.isEmptyBlock(above)) continue probe;
                        int which = this.random.nextInt(8);
                        BlockState flower = switch (which) {
                            case 0 -> Blocks.POPPY.defaultBlockState();
                            case 1 -> Blocks.DANDELION.defaultBlockState();
                            case 2 -> ModBlocks.FLOWER_BLUE.get().defaultBlockState();
                            case 3 -> ModBlocks.FLOWER_PINK.get().defaultBlockState();
                            case 4 -> ModBlocks.CRYSTAL_FLOWER_RED.get().defaultBlockState();
                            case 5 -> ModBlocks.CRYSTAL_FLOWER_GREEN.get().defaultBlockState();
                            case 6 -> ModBlocks.CRYSTAL_FLOWER_BLUE.get().defaultBlockState();
                            default -> ModBlocks.CRYSTAL_FLOWER_YELLOW.get().defaultBlockState();
                        };
                        serverLevel.setBlockAndUpdate(above, flower);
                        continue probe;
                    }
                    if (state.is(Blocks.DIRT) && serverLevel.isEmptyBlock(above)) {
                        serverLevel.setBlockAndUpdate(pos, Blocks.GRASS_BLOCK.defaultBlockState());
                        continue probe;
                    }
                    if (state.is(Blocks.STONE) && serverLevel.isEmptyBlock(above)) {
                        serverLevel.setBlockAndUpdate(above, Blocks.DIRT.defaultBlockState());
                        continue probe;
                    }
                    if (state.is(Blocks.SAND) && serverLevel.isEmptyBlock(above)) {
                        if (this.random.nextInt(2) == 0) {
                            serverLevel.setBlockAndUpdate(above, Blocks.CACTUS.defaultBlockState());
                        } else {
                            serverLevel.setBlockAndUpdate(pos, Blocks.DIRT.defaultBlockState());
                        }
                        continue probe;
                    }
                    // orig :606-613 — 1.7.10 distinguished still lava (:606,
                    // becomes still water) and flowing lava (:610, becomes
                    // flowing water); modern lava is one block, mapped to water.
                    if (state.is(Blocks.LAVA) && serverLevel.isEmptyBlock(above)) {
                        serverLevel.setBlockAndUpdate(pos, Blocks.WATER.defaultBlockState());
                        continue probe;
                    }
                    if (state.isAir() && j > 0) continue probe;
                }
            }
        }
        // orig :618-625 — two hatch attempts into nearby air: Butterfly or Bird
        // (orig "Bird" registers the Cockateil class, OreSpawnMain.java:3831).
        for (int m = 0; m < 2; ++m) {
            int i = this.random.nextInt(4) - this.random.nextInt(4);
            int k = this.random.nextInt(4) - this.random.nextInt(4);
            int j = 1 + this.random.nextInt(4);
            BlockPos pos = new BlockPos((int) this.getX() + i, (int) this.getY() + j, (int) this.getZ() + k);
            if (!serverLevel.isEmptyBlock(pos)) continue;
            Mob hatchling = this.random.nextInt(2) == 0
                    ? ModEntities.ENTITY_BUTTERFLY.get().create(serverLevel)
                    : ModEntities.COCKATEIL.get().create(serverLevel);
            if (hatchling == null) continue;
            hatchling.moveTo(this.getX() + i, this.getY() + j, this.getZ() + k,
                    this.random.nextFloat() * 360.0f, 0.0f);
            serverLevel.addFreshEntity(hatchling);
            hatchling.playAmbientSound();
        }
    }

    /**
     * orig ThePrincess.java:672-812 ({@code do_movement}) — identical brain to
     * the baby Prince: a 1-in-7 combat roll (melee bite, or the canon trio
     * gated on a 0.5 rad head-bearing check), retreat-when-hurt for tame
     * princesses, and flight-target steering. Steering only runs while flying
     * (activity 2); on the ground (activity 1) the method exits after the
     * combat roll (orig :757-759).
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
        // orig :690-692 — 1-in-300 wanderlust re-roll while flying.
        if (this.getActivity() == 2 && this.random.nextInt(300) == 0) {
            doNew = true;
        }
        LivingEntity owner = this.getOwner();
        if (this.isTame() && owner != null) {
            hasOwner = true;
            ox = owner.getX();
            oy = owner.getY() + 1.0;
            oz = owner.getZ();
            // orig :699-704 — re-target when straying: >10 blocks normally,
            // >6 blocks when chasing a flying owner.
            if (this.distanceToSqr(owner) > 100.0) {
                doNew = true;
            }
            if (this.ownerFlying != 0 && this.distanceToSqr(owner) > 36.0) {
                doNew = true;
            }
        }
        // orig :706-756 — 1-in-7 combat roll outside Peaceful.
        if (this.random.nextInt(7) == 1 && this.level().getDifficulty() != Difficulty.PEACEFUL) {
            LivingEntity target = this.findSomethingToAttack();
            if (target != null) {
                if (this.isTame() && this.getHealth() / this.getMaxHealth() < 0.25f) {
                    // orig :709-713 — a badly hurt tame princess flees directly
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
                        // orig :721-751 — random head, and fire only once that head
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
        // orig :757-759 — grounded: vanilla AI moves the princess.
        if (this.getActivity() == 1) {
            return;
        }
        if (this.currentFlightTarget.distSqr(new net.minecraft.core.Vec3i(
                (int) this.getX(), (int) this.getY(), (int) this.getZ())) < 2.1) {
            doNew = true;
        }
        if (doNew) {
            // orig :763-794 — pick a random air block near self (or the owner),
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
        // orig :795-811 — signum steering toward the target; 1.75× speed chasing
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

    /** orig ThePrincess.java:814-843 ({@code isSuitableTarget}). */
    private boolean isSuitableTarget(LivingEntity target) {
        if (this.level().getDifficulty() == Difficulty.PEACEFUL) return false;
        if (target == null || target == this || !target.isAlive()) return false;
        if (!this.getSensing().hasLineOfSight(target)) return false;
        if (MyUtils.isRoyalty(target)) return false;
        if (target instanceof Monster) return true;
        // orig :836-842 — Mothra, dragonflies, and mosquitos are prey (a
        // shorter list than the Prince's: no butterflies or birds — she
        // hatches those).
        return target instanceof Mothra || target instanceof EntityDragonfly
                || target instanceof EntityMosquito;
    }

    /** orig ThePrincess.java:845-861 — nearest suitable prey in a 12/6/12 box. */
    private LivingEntity findSomethingToAttack() {
        if (OreSpawnConfig.PLAY_NICELY.get()) return null;
        AABB box = this.getBoundingBox().inflate(12.0, 6.0, 12.0);
        List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, box);
        // OPT-021: nearest-first pick without the full list sort; TargetSelection
        // preserves the removed sort's order and stable-tie semantics exactly.
        return TargetSelection.firstMatch(targets, this.targetSorter, this::isSuitableTarget);
    }

    /**
     * orig ThePrincess.java:863-881 ({@code firecanon}) — head 1: a big
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
     * orig ThePrincess.java:883-907 ({@code firecanonl}) — head 2: a ThunderBolt
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
     * orig ThePrincess.java:909-934 ({@code firecanoni}) — head 3: an
     * ice-making IceBall launched at 1.4f/4.0f with a 0.2×distance arc, then
     * tripled.
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

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (stack.is(Blocks.DIAMOND_BLOCK.asItem()) && this.distanceToSqr(player) < 16.0) {
            if (!this.level().isClientSide) {
                // orig ThePrincess.java:204-215 — tames unconditionally, so it
                // also transfers ownership.
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

        // orig ThePrincess.java:224-240 — any food heals nutrition × 10 (fixes
        // the flat 20.0 heal, BOSS-039; the original has no fed_count either).
        if (stack.has(net.minecraft.core.component.DataComponents.FOOD)
                && this.distanceToSqr(player) < 16.0 && this.isTame() && this.isOwnedBy(player)) {
            if (!this.level().isClientSide) {
                if (this.getMaxHealth() > this.getHealth()) {
                    net.minecraft.world.food.FoodProperties food =
                            stack.get(net.minecraft.core.component.DataComponents.FOOD);
                    this.heal(food.nutrition() * 10.0f);
                }
                this.level().broadcastEntityEvent(this, (byte) 7);
            }
            if (!player.getAbilities().instabuild) stack.shrink(1);
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        // orig ThePrincess.java:241-257 — an ice block extinguishes the fireballs.
        if (this.isTame() && this.isOwnedBy(player)
                && this.distanceToSqr(player) < 16.0
                && stack.is(Blocks.ICE.asItem())) {
            if (!this.level().isClientSide) {
                this.level().broadcastEntityEvent(this, (byte) 6);
                this.setSpyroFire(0);
                player.displayClientMessage(Component.literal("Princess fireballs extinguished."), false);
            }
            if (!player.getAbilities().instabuild) stack.shrink(1);
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        // orig ThePrincess.java:258-274 — flint & steel relights them.
        if (this.isTame() && this.isOwnedBy(player)
                && this.distanceToSqr(player) < 16.0
                && stack.is(Items.FLINT_AND_STEEL)) {
            if (!this.level().isClientSide) {
                this.level().broadcastEntityEvent(this, (byte) 6);
                this.setSpyroFire(1);
                player.displayClientMessage(Component.literal("Princess fireballs lit!"), false);
            }
            if (!player.getAbilities().instabuild) stack.shrink(1);
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        if (this.isTame() && this.isOwnedBy(player) && this.distanceToSqr(player) < 16.0) {
            this.setOrderedToSit(!this.isOrderedToSit());
            this.setInSittingPose(this.isOrderedToSit());
            if (this.isOrderedToSit()) this.setActivity(1);
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }

    @Override protected SoundEvent getAmbientSound() {
        if (this.isOrderedToSit() || this.getAttacking() == 0) return null;
        return SND_ROAR;
    }
    @Override protected SoundEvent getHurtSound(DamageSource s) { return SND_DUCK_HURT; }
    @Override protected SoundEvent getDeathSound() { return SND_CRYO_DEATH; }
    @Override protected float getSoundVolume() { return 0.6f; }
    @Override public boolean removeWhenFarAway(double d) { return false; }
    @Override public boolean isFood(ItemStack s) { return s.is(Items.BEEF); }
    @Nullable @Override public AgeableMob getBreedOffspring(ServerLevel l, AgeableMob o) { return null; }

    @Override public void addAdditionalSaveData(CompoundTag tag) { super.addAdditionalSaveData(tag); tag.putInt("PrincessActivity", getActivity()); tag.putInt("PrincessFire", entityData.get(DATA_FIRE)); tag.putInt("PrincessGrow", okToGrow); tag.putInt("PrincessKill", killCount); tag.putInt("PrincessFed", fedCount); tag.putInt("PrincessDay", dayCount); }
    @Override public void readAdditionalSaveData(CompoundTag tag) { super.readAdditionalSaveData(tag); setActivity(tag.getInt("PrincessActivity")); entityData.set(DATA_FIRE, tag.getInt("PrincessFire")); okToGrow = tag.getInt("PrincessGrow"); killCount = tag.getInt("PrincessKill"); fedCount = tag.getInt("PrincessFed"); dayCount = tag.getInt("PrincessDay"); }

    /** orig ThePrincess.java:369-371 — always allowed. */
    @Override
    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level,
                                   net.minecraft.world.entity.MobSpawnType spawnType) {
        return true;
    }
}
