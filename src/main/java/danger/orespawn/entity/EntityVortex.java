package danger.orespawn.entity;

import danger.orespawn.MobStats;

import danger.orespawn.OreSpawnMod;
import java.util.Comparator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.Mth;
import danger.orespawn.entity.ai.TargetSelection;

public class EntityVortex extends Monster {
    // OPT-011: cached SoundEvents — identical createVariableRangeEvent ids,
    // allocated once per class instead of on every sound query.
    private static final SoundEvent SND_VORTEXLIVE = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "vortexlive"));
    private static final long DAY_LENGTH_TICKS = 24000L;
    private static final long DAYTIME_DESPAWN_BEFORE = 12000L;
    private static final double PULL_RANGE_DIST_SQ = 81.0;
    private static final double PULL_DISTANCE_SCALE = 10.0;
    private static final double PLAYER_VERTICAL_PULL_MULT = 2.0;
    private static final int WINDED_COOLDOWN_TICKS = 20;

    private BlockPos currentFlightTarget = null;
    private int windedCooldownTicks = 0;
    /**
     * orig Vortex.java:46 {@code was_spawnered} — set when the spawn-rule check
     * passes via the "Vortex" spawner bypass (orig :254); spawnered Vortexes are
     * exempt from far-away despawn (orig :64-72) and the daytime discard
     * (orig :134-143). Not persisted in the original either.
     */
    private int wasSpawnered = 0;
    /** orig Vortex.java:45 {@code busy_fighting} — refreshed every tick; guards both despawn paths. */
    private boolean busyFighting = false;

    // OPT-004 (ruled apply 2026-08-11): one cached pull target, rescanned every
    // 5 ticks, shared between tick() and customServerAiStep() — replaces the
    // ungated 16x10x16 AABB scans that ran 2-3x per tick. See currentPullTarget().
    private static final int TARGET_RESCAN_INTERVAL_TICKS = 5;
    @Nullable
    private LivingEntity cachedPullTarget = null;
    private int lastTargetScanTick = -TARGET_RESCAN_INTERVAL_TICKS;

    public EntityVortex(EntityType<? extends EntityVortex> type, Level level) {
        super(type, level);
        this.xpReward = 200;
    }

    public static AttributeSupplier.Builder createAttributes() {
        // orig OreSpawnMain.java:6500 — Vortex 150 HP / 26 ATK / 10 armor
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, MobStats.VORTEX.maxHealth())
                .add(Attributes.MOVEMENT_SPEED, 0.35)
                .add(Attributes.ATTACK_DAMAGE, MobStats.VORTEX.attackDamage())
                .add(Attributes.ARMOR, MobStats.VORTEX.armor());
    }

    @Override
    protected float getSoundVolume() {
        return 0.75f;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return SND_VORTEXLIVE;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return null;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SND_VORTEXLIVE;
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    /** orig Vortex.java:98-99 — empty collideWithEntity: the Vortex never shoves what it is pulling (ENT-S-089 item 2). */
    @Override
    protected void doPush(Entity entity) {
    }

    /** orig Vortex.java:221-223 — doesEntityNotTriggerPressurePlate -> true (ENT-S-089 item 5, ENT-S-090). */
    @Override
    public boolean isIgnoringBlockTriggers() {
        return true;
    }

    /** orig Vortex.java:78-80 — fixed voice pitch; vanilla would jitter +/-0.2 (ENT-S-089 item 6). */
    @Override
    public float getVoicePitch() {
        return 1.0f;
    }

    @Override
    public void tick() {
        super.tick();
        Vec3 motion = this.getDeltaMovement();
        this.setDeltaMovement(motion.x, motion.y * 0.6, motion.z);

        LivingEntity pullTarget = currentPullTarget(); // OPT-004: shared 5-tick cache
        this.busyFighting = pullTarget != null; // orig Vortex.java:113-116
        if (pullTarget != null && this.level().isClientSide) {
            for (int i = 0; i < 20; ++i) {
                double smokeRadius = this.random.nextDouble() * 3.5;
                double smokeHeightOffset = smokeRadius * smokeRadius;
                double dir = this.random.nextDouble() * 2.0 * Math.PI;
                double dx = Math.cos(dir - Math.PI) * smokeHeightOffset / 2.0;
                double dz = Math.sin(dir - Math.PI) * smokeHeightOffset / 2.0;
                this.level().addParticle(ParticleTypes.SMOKE,
                        this.getX() + dx, this.getY() + 0.75 + smokeHeightOffset, this.getZ() + dz,
                        // orig Vortex.java:122-124 — the offset uses dir - PI and the velocity the
                        // further += PI/2 on that same value: dir - PI/2 in the drawn angle (ENT-S-089 item 7)
                        Math.cos(dir - Math.PI / 2.0) * this.random.nextFloat() / 4.0,
                        this.random.nextFloat() / 2.0,
                        Math.sin(dir - Math.PI / 2.0) * this.random.nextFloat() / 4.0);
            }
        }

        if (this.random.nextInt(200) == 1) {
            this.heal(1.0f);
        }

        if (!this.level().isClientSide && !this.isPersistenceRequired() && !this.busyFighting
                && this.wasSpawnered == 0) {
            // orig Vortex.java:131-143 — daytime discard skipped when persistence is required
            // (name tag etc., ENT-S-089 item 4), while fighting, or when spawnered
            long dayTimeInCycle = this.level().getDayTime() % DAY_LENGTH_TICKS;
            if (dayTimeInCycle < DAYTIME_DESPAWN_BEFORE && this.random.nextInt(500) == 1) {
                this.discard();
            }
        }
    }

    /** orig Vortex.java:64-72 — no far-away despawn while fighting or when spawnered. */
    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        if (this.busyFighting) return false;
        return this.wasSpawnered == 0;
    }

    /**
     * orig Vortex.java:240-284 — "Vortex" spawner bypass (sets {@code was_spawnered});
     * 5x3x5 clear-air volume above; darkness; y>=50; night half of the day only;
     * 1-in-2 dice; no other Vortex within 20/16/20.
     */
    @Override
    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level,
                                   net.minecraft.world.entity.MobSpawnType spawnType) {
        if (OriginalSpawnGates.nearOwnSpawner(this, level)) {
            this.wasSpawnered = 1;
            return true;
        }
        if (!OriginalSpawnGates.airBox(this, level, -2, 2, 1, 3, -2, 2)) return false;
        if (!OriginalSpawnGates.isDarkEnough(this, level)) return false;
        if (this.getY() < 50.0) return false;
        if (level.dayTime() % DAY_LENGTH_TICKS < DAYTIME_DESPAWN_BEFORE) return false;
        if (this.random.nextInt(2) != 1) return false;
        return !OriginalSpawnGates.anyOtherNearby(this, level, EntityVortex.class, 20.0, 16.0, 20.0);
    }

    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;
        super.customServerAiStep();

        if (this.currentFlightTarget == null) {
            this.currentFlightTarget = this.blockPosition();
        }

        if (this.windedCooldownTicks > 0) {
            --this.windedCooldownTicks;
        }

        // orig Vortex.java:165-182 — retarget on the 1-in-300 roll or within sqrt(2.1) blocks of the
        // integer target (ENT-S-089 item 8: was 4.0 plus an invented stuck counter); each candidate is
        // WRITTEN before it is validated, so 50 failures leave the last candidate as the target (orig
        // quirk); a candidate must be air AND visible from the eye line (orig :146-148, item 3).
        double distSq = this.currentFlightTarget.distSqr(this.blockPosition());

        if (this.random.nextInt(300) == 0 || distSq < 2.1) {
            int keepTrying = 50;
            boolean blocked = true;
            while (blocked && keepTrying != 0) {
                int zdir = this.random.nextInt(14) + 10;
                int xdir = this.random.nextInt(14) + 10;
                if (this.random.nextInt(2) == 0) {
                    zdir = -zdir;
                }
                if (this.random.nextInt(2) == 0) {
                    xdir = -xdir;
                }
                this.currentFlightTarget = new BlockPos(
                        (int) this.getX() + xdir,
                        (int) this.getY() + this.random.nextInt(6) - 3,
                        (int) this.getZ() + zdir);
                blocked = !this.level().getBlockState(this.currentFlightTarget).isAir()
                        || !canSeeTarget(this.currentFlightTarget.getX(), this.currentFlightTarget.getY(),
                                this.currentFlightTarget.getZ());
                --keepTrying;
            }
        }

        LivingEntity currentTarget = currentPullTarget(); // OPT-004: shared 5-tick cache
        if (currentTarget != null) {
            this.currentFlightTarget = currentTarget.blockPosition();
            double distSqToTarget = this.distanceToSqr(currentTarget);
            if (distSqToTarget < PULL_RANGE_DIST_SQ && this.windedCooldownTicks == 0) {
                double angleAwayFromTarget = Math.atan2(this.getZ() - currentTarget.getZ(), this.getX() - currentTarget.getX());
                double verticalPullMultiplier = (currentTarget instanceof Player) ? PLAYER_VERTICAL_PULL_MULT : 1.0;
                double pullStrength = (PULL_DISTANCE_SCALE - Math.sqrt(distSqToTarget)) * 0.1;
                currentTarget.push(
                        Math.cos(angleAwayFromTarget) * pullStrength,
                        (PULL_DISTANCE_SCALE - Math.sqrt(distSqToTarget)) * 0.05 * verticalPullMultiplier,
                        Math.sin(angleAwayFromTarget) * pullStrength);
            }
            // orig Vortex.java:195-197 — plain melee on the 1-in-8 roll; the orig has
            // no launch attack (the pull above is its only displacement effect).
            double attackRange = (4.0 + currentTarget.getBbWidth() / 2.0);
            if (distSqToTarget < attackRange * attackRange && this.random.nextInt(8) == 2) {
                this.doHurtTarget(currentTarget);
            }
        }

        double dx = this.currentFlightTarget.getX() + 0.5 - this.getX();
        double dy = this.currentFlightTarget.getY() + 0.1 - this.getY();
        double dz = this.currentFlightTarget.getZ() + 0.5 - this.getZ();

        Vec3 motion = this.getDeltaMovement();
        double newMx = motion.x + (Math.signum(dx) * 0.4 - motion.x) * 0.2;
        double newMy = motion.y + (Math.signum(dy) * 0.7 - motion.y) * 0.2;
        double newMz = motion.z + (Math.signum(dz) * 0.4 - motion.z) * 0.2;
        this.setDeltaMovement(newMx, newMy, newMz);

        float targetYaw = (float) (Math.atan2(newMz, newMx) * 180.0 / Math.PI) - 90.0f;
        float yawDiff = Mth.wrapDegrees(targetYaw - this.getYRot());
        this.setYRot(this.getYRot() + yawDiff / 4.0f);
    }

    /**
     * orig Vortex.java:146-148 — {@code canSeeTarget}: no block between the eye line (y + 0.75) and the
     * block corner; the 1.7.10 ray trace used selection boxes and ignored liquids.
     */
    private boolean canSeeTarget(double x, double y, double z) {
        Vec3 eye = new Vec3(this.getX(), this.getY() + 0.75, this.getZ());
        return this.level().clip(new net.minecraft.world.level.ClipContext(eye, new Vec3(x, y, z),
                net.minecraft.world.level.ClipContext.Block.OUTLINE,
                net.minecraft.world.level.ClipContext.Fluid.NONE, this)).getType()
                == net.minecraft.world.phys.HitResult.Type.MISS;
    }

    @Override
    public boolean causeFallDamage(float dist, float mult, DamageSource source) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        Entity attacker = source.getEntity();
        boolean ret = super.hurt(source, amount);
        if (attacker != null && this.currentFlightTarget != null) {
            this.currentFlightTarget = attacker.blockPosition();
        }
        this.windedCooldownTicks = WINDED_COOLDOWN_TICKS;
        return ret;
    }

    /**
     * OPT-004 (ruled apply 2026-08-11): the target scan runs at most once every
     * {@value #TARGET_RESCAN_INTERVAL_TICKS} ticks and the result is shared
     * between {@link #tick()} (which only needs has-target for busyFighting and
     * the client particle burst) and {@link #customServerAiStep()} — on the
     * server both run in the same game tick (customServerAiStep first, inside
     * super.tick()) and see one scan, not two.
     * <p>Cache invalidation story: a cached target that has died or been
     * removed (killed, unloaded, changed dimension) is dropped IMMEDIATELY on
     * the next call — a dead target never lingers for the rest of the
     * interval, so busyFighting (which gates both despawn paths) and the pull
     * stop the same tick the target dies, exactly like the old per-tick scan.
     * Acquiring a fresh/replacement target, and re-checking suitability (line
     * of sight, creative toggle) of a live one, may lag by up to 5 ticks: the
     * pull/aggro/particle-onset latency the ruling accepts. The client-side
     * instance keeps its own independent cache for the particle check.
     */
    @Nullable
    private LivingEntity currentPullTarget() {
        if (this.cachedPullTarget != null
                && (this.cachedPullTarget.isRemoved() || !this.cachedPullTarget.isAlive())) {
            this.cachedPullTarget = null;
        }
        if (this.tickCount - this.lastTargetScanTick >= TARGET_RESCAN_INTERVAL_TICKS) {
            this.lastTargetScanTick = this.tickCount;
            this.cachedPullTarget = findSomethingToAttack();
        }
        return this.cachedPullTarget;
    }

    @Nullable
    private LivingEntity findSomethingToAttack() {
        // TF-035: orig Vortex.java:341-344 — PlayNicely disables Vortex
        // aggression entirely, and the scan sorts with GenericTargetSorter
        // (creeper-halved / big-mob-prioritized), not plain distance.
        if (danger.orespawn.OreSpawnConfig.PLAY_NICELY.get()) return null;
        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(16.0, 10.0, 16.0));
        // OPT-021: nearest-first pick without the full list sort; TargetSelection
        // preserves the removed sort's order and stable-tie semantics exactly.
        return TargetSelection.firstMatch(entities, new danger.orespawn.entity.ai.GenericTargetSorter(this), this::isSuitableTarget);
    }

    /**
     * TF-035: orig Vortex.java:290-339 — beyond self/dead/LoS/creative, the
     * original ignores everything in MyUtils.isIgnoreable plus ten explicit
     * classes: Vortex, Rotator, Mothra, Brutalfly, Peacock, CrystalCow,
     * Irukandji, Skate, Whale, Flounder, Urchin.
     */
    private boolean isSuitableTarget(LivingEntity target) {
        if (target == null || target == this || !target.isAlive()) return false;
        if (danger.orespawn.util.MyUtils.isIgnoreable(target)) return false;
        if (!this.getSensing().hasLineOfSight(target)) return false;
        if (target instanceof Player p && p.getAbilities().instabuild) return false;
        if (target instanceof EntityVortex || target instanceof EntityRotator
                || target instanceof Mothra || target instanceof EntityBrutalfly
                || target instanceof Peacock || target instanceof CrystalCow
                || target instanceof Irukandji || target instanceof Skate
                || target instanceof Whale || target instanceof Flounder
                || target instanceof Urchin) {
            return false;
        }
        return true;
    }
}
