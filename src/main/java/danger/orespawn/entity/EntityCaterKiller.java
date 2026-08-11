package danger.orespawn.entity;

import danger.orespawn.MobStats;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.ai.BugMeleeAttackGoal;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import danger.orespawn.OreSpawnConfig;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class EntityCaterKiller extends Monster {
    // OPT-011: cached SoundEvents — identical createVariableRangeEvent ids,
    // allocated once per class instead of on every sound query.
    private static final SoundEvent SND_CATERKILLER_LIVING = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "caterkiller_living"));
    private static final SoundEvent SND_CATERKILLER_HIT = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "caterkiller_hit"));
    private static final SoundEvent SND_CATERKILLER_DEATH = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "caterkiller_death"));
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(EntityCaterKiller.class, EntityDataSerializers.INT);

    /**
     * Metamorphosis timer — orig CaterKiller.java:438-448: after 2400 ticks
     * below max health the Cater Killer transforms, spawning a Brutalfly and
     * 10 Butterflies with an explosion sound, then removes itself.
     */
    private int damagedDespawnTicker = 0;

    /**
     * Cobweb-trail throttle. The 1.7.10 Cater Killer spat cobwebs into the
     * tile underneath whichever player it was actively chasing, slowing the
     * pursued player and forcing them into ranged combat. We rate-limit
     * those setBlock calls so a long chase doesn't hammer chunk updates.
     */
    private int cobwebCooldown = 0;
    private static final int COBWEB_INTERVAL_TICKS = 40;

    /**
     * orig CaterKiller.java:450 — field_70134_J (isInWeb), raised by the
     * cobweb block's collision callback; the AI step consumes it to chew
     * the mob free. Modern cobwebs report through
     * {@link #makeStuckInBlock}, so the flag is mirrored there.
     */
    private boolean inWeb = false;


    public EntityCaterKiller(EntityType<? extends EntityCaterKiller> type, Level level) {
        super(type, level);
        this.xpReward = 200;
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        // orig CaterKiller.java:54-58 — half size when PlayNicely is on.
        return OreSpawnConfig.PLAY_NICELY.get()
                ? EntityDimensions.scalable(1.45f, 2.3f)
                : EntityDimensions.scalable(2.9f, 4.6f);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new BugMeleeAttackGoal(
                this, this::setAttacking, BugMeleeAttackGoal.Params.caterKiller()));
        this.goalSelector.addGoal(2, new MyEntityAIWanderALot(this, 16, 1.0));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public static AttributeSupplier.Builder createAttributes() {
        // orig OreSpawnMain.java:6481 — CaterKiller 450 HP / 32 ATK / 19 armor
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MobStats.CATERKILLER.maxHealth())
                .add(Attributes.MOVEMENT_SPEED, 0.35)
                .add(Attributes.ATTACK_DAMAGE, MobStats.CATERKILLER.attackDamage())
                .add(Attributes.ARMOR, MobStats.CATERKILLER.armor())
                .add(Attributes.FOLLOW_RANGE, 40.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, 0);
    }

    public int getAttacking() {
        return this.entityData.get(DATA_ATTACKING);
    }

    public void setAttacking(int value) {
        this.entityData.set(DATA_ATTACKING, value);
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        if (this.random.nextInt(3) == 0) {
            return SND_CATERKILLER_LIVING;
        }
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SND_CATERKILLER_HIT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SND_CATERKILLER_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 1.5f;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (super.doHurtTarget(target)) {
            if (target instanceof LivingEntity) {
                double knockbackStrength = 1.2;
                double verticalKnockback = 0.1;
                float angle = (float) Math.atan2(target.getZ() - this.getZ(), target.getX() - this.getX());
                if (target.isRemoved() || target instanceof Player) verticalKnockback *= 2.0;
                target.push(Math.cos(angle) * knockbackStrength, verticalKnockback, Math.sin(angle) * knockbackStrength);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        Entity attacker = source.getEntity();
        boolean ret = super.hurt(source, amount);
        if (attacker instanceof Mob mob) {
            this.setTarget(mob);
        }
        return ret;
    }

    @Override
    public void makeStuckInBlock(BlockState state, Vec3 motionMultiplier) {
        // orig CaterKiller.java:450 — only the cobweb raised field_70134_J
        // (BlockWeb.onEntityCollidedWithBlock -> setInWeb); berry-bush and
        // powder-snow style stuck states did not exist and must not trigger
        // the chew-free loop.
        if (state.is(Blocks.COBWEB)) {
            this.inWeb = true;
        }
        super.makeStuckInBlock(state, motionMultiplier);
    }

    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;
        super.customServerAiStep();

        // orig CaterKiller.java:438-448 — timed metamorphosis: while damaged,
        // count up; past 2400 ticks spawn 1 Brutalfly + 10 Butterflies with an
        // explosion sound and remove self (NO loot — this is not a death).
        // Note: the orig ticker never resets when healed (quirk kept).
        if (this.getHealth() + 1.0f < this.getMaxHealth()) {
            ++this.damagedDespawnTicker;
            if (this.damagedDespawnTicker > 2400) {
                if (this.level() instanceof ServerLevel serverLevel) {
                    EntityBrutalfly fly = ModEntities.ENTITY_BRUTALFLY.get().create(serverLevel);
                    if (fly != null) {
                        fly.moveTo(this.getX(), this.getY() + 4.0, this.getZ(),
                                this.random.nextFloat() * 360.0f, 0.0f);
                        serverLevel.addFreshEntity(fly);
                    }
                    this.playSound(SoundEvents.GENERIC_EXPLODE.value(), 1.0f,
                            this.random.nextFloat() * 0.2f + 0.9f);
                    for (int i = 0; i < 10; i++) {
                        EntityButterfly bf = ModEntities.ENTITY_BUTTERFLY.get().create(serverLevel);
                        if (bf == null) continue;
                        bf.moveTo(this.getX(), this.getY() + 1.0 + this.random.nextInt(4), this.getZ(),
                                this.random.nextFloat() * 360.0f, 0.0f);
                        serverLevel.addFreshEntity(bf);
                    }
                }
                this.discard();
                return;
            }
        }

        // orig CaterKiller.java:450-461 — web-self-clear: while flagged
        // in-web, every cobweb in the 5x6x5 box around the feet (x -2..2,
        // y -1..4, z -2..2, toward-zero int coords) is set to air, then the
        // flag drops. The original does NOT gate this on mobGriefing (only
        // the tree-eat at :521 is gated).
        if (this.inWeb) {
            int bx = (int) this.getX();
            int by = (int) this.getY();
            int bz = (int) this.getZ();
            for (int i = -2; i <= 2; ++i) {
                for (int j = -1; j < 5; ++j) {
                    for (int k = -2; k <= 2; ++k) {
                        BlockPos pos = new BlockPos(bx + i, by + j, bz + k);
                        if (!this.level().getBlockState(pos).is(Blocks.COBWEB)) continue;
                        this.level().setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            }
            this.inWeb = false;
        }

        if (this.cobwebCooldown > 0) --this.cobwebCooldown;

        LivingEntity target = this.getTarget();
        if (target instanceof Player chased && this.cobwebCooldown == 0
                && this.distanceToSqr(chased) < 144.0
                && this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
            tryDropCobwebUnder(chased);
            this.cobwebCooldown = COBWEB_INTERVAL_TICKS;
        }

        // orig CaterKiller.java:502-530 — tree-eat heal: triggers 1-in-8 while
        // damaged or 1-in-30 otherwise, gated on PlayNicely==0; nearest tree
        // block within 12 blocks; eaten at distSq<81 for a flat 2.0 heal.
        boolean eatRoll = (this.random.nextInt(8) == 0 && this.getHealth() < this.getMaxHealth())
                || this.random.nextInt(30) == 0;
        if (eatRoll && !OreSpawnConfig.PLAY_NICELY.get()) {
            BlockPos food = findNearestTreeBlock();
            if (food != null) {
                if (target == null) {
                    this.getNavigation().moveTo(food.getX(), food.getY(), food.getZ(), 1.0);
                }
                if (food.distSqr(this.blockPosition().above()) < 81.0) {
                    if (this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
                        this.level().setBlock(food, Blocks.AIR.defaultBlockState(), 2);
                    }
                    this.heal(2.0f);
                    if (this.random.nextInt(20) == 1) {
                        this.playSound(SoundEvents.PLAYER_BURP, 1.0f,
                                this.random.nextFloat() * 0.2f + 0.9f);
                    }
                }
            }
        }
    }

    /**
     * orig CaterKiller.java:380-427 {@code scan_it} — nearest leaf/vine/log
     * block in an expanding box capped at 12 horizontally / 9 vertically
     * around the head (y+1). Flat nearest-block scan over the same volume.
     */
    @Nullable
    private BlockPos findNearestTreeBlock() {
        BlockPos origin = this.blockPosition().above();
        BlockPos best = null;
        int bestDistSq = Integer.MAX_VALUE;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dx = -12; dx <= 12; ++dx) {
            for (int dy = -9; dy <= 9; ++dy) {
                for (int dz = -12; dz <= 12; ++dz) {
                    int distSq = dx * dx + dy * dy + dz * dz;
                    if (distSq >= bestDistSq) continue;
                    cursor.setWithOffset(origin, dx, dy, dz);
                    BlockState state = this.level().getBlockState(cursor);
                    if (state.is(BlockTags.LEAVES) || state.is(BlockTags.LOGS)
                            || state.is(Blocks.VINE)) {
                        bestDistSq = distSq;
                        best = cursor.immutable();
                    }
                }
            }
        }
        return best;
    }

    /**
     * Spit a cobweb into the floor tile directly under the chased player.
     * Only fires on solid-floor tiles with an air block immediately above
     * (so we don't overwrite the player's standing tile or melt structural
     * blocks). The cobweb naturally despawns to nothing on player break.
     */
    private void tryDropCobwebUnder(Player chased) {
        if (!(this.level() instanceof ServerLevel server)) return;
        BlockPos under = chased.blockPosition();
        BlockState atFeet = server.getBlockState(under);
        if (!atFeet.isAir() && atFeet.getBlock() != Blocks.COBWEB) return;
        BlockPos floor = under.below();
        BlockState floorState = server.getBlockState(floor);
        if (floorState.isAir() || !floorState.getFluidState().isEmpty()) return;
        server.setBlock(under, Blocks.COBWEB.defaultBlockState(), 3);
    }

    // Item drops are data-driven via loot_table/entities/cater_killer.json
    // (orig CaterKiller.java:160-324: jaw, painting, 10 leather, 6 raw beef,
    // 1-5 rolls of the d20 Ruby/Ultimate gear table). Non-item death
    // behavior (butterfly swarm) stays in die() below.
    @Override
    public void die(DamageSource cause) {
        // orig CaterKiller.java:325-327 — 25 Butterflies released on death.
        if (!this.level().isClientSide && this.level() instanceof ServerLevel serverLevel) {
            for (int i = 0; i < 25; i++) {
                EntityButterfly bf = ModEntities.ENTITY_BUTTERFLY.get().create(serverLevel);
                if (bf == null) continue;
                bf.moveTo(this.getX(), this.getY() + 1.0, this.getZ(),
                        this.random.nextFloat() * 360.0f, 0.0f);
                serverLevel.addFreshEntity(bf);
            }
        }
        super.die(cause);
    }

    /** orig CaterKiller.java:585-624 — spawner bypass; y>=50; 1-in-10 dice; daytime; air/leaves/logs clearance above; no other CaterKiller within 48/16/48. */
    @Override
    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level,
                                   net.minecraft.world.entity.MobSpawnType spawnType) {
        if (OriginalSpawnGates.nearOwnSpawner(this, level)) return true;
        if (this.getY() < 50.0) return false;
        if (this.getRandom().nextInt(10) != 0) return false;
        if (!OriginalSpawnGates.isDaytime(level)) return false;
        if (!OriginalSpawnGates.boxMatches(this, level, -1, 1, 1, 4, -1, 1,
                s -> s.isAir() || s.is(net.minecraft.tags.BlockTags.LEAVES) || s.is(net.minecraft.tags.BlockTags.LOGS))) return false;
        return !OriginalSpawnGates.anyOtherNearby(this, level, EntityCaterKiller.class, 48.0, 16.0, 48.0);
    }
}
