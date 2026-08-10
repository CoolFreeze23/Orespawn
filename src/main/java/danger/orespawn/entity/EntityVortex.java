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

public class EntityVortex extends Monster {
    private static final long DAY_LENGTH_TICKS = 24000L;
    private static final long DAYTIME_DESPAWN_BEFORE = 12000L;
    private static final double PULL_RANGE_DIST_SQ = 81.0;
    private static final double PULL_DISTANCE_SCALE = 10.0;
    private static final double PLAYER_VERTICAL_PULL_MULT = 2.0;
    private static final int WINDED_COOLDOWN_TICKS = 20;

    private BlockPos currentFlightTarget = null;
    private int lastX = 0;
    private int lastY = 0;
    private int lastZ = 0;
    private int stuckCount = 0;
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
        return SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "vortexlive"));
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return null;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "vortexlive"));
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        Vec3 motion = this.getDeltaMovement();
        this.setDeltaMovement(motion.x, motion.y * 0.6, motion.z);

        LivingEntity pullTarget = findSomethingToAttack();
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
                        Math.cos(dir + Math.PI / 2.0) * this.random.nextFloat() / 4.0,
                        this.random.nextFloat() / 2.0,
                        Math.sin(dir + Math.PI / 2.0) * this.random.nextFloat() / 4.0);
            }
        }

        if (this.random.nextInt(200) == 1) {
            this.heal(1.0f);
        }

        if (!this.level().isClientSide && !this.busyFighting && this.wasSpawnered == 0) {
            // orig Vortex.java:132-143 — daytime discard skipped while fighting or when spawnered
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

        if (this.lastX == (int) this.getX() && this.lastY == (int) this.getY() && this.lastZ == (int) this.getZ()) {
            ++this.stuckCount;
        } else {
            this.stuckCount = 0;
            this.lastX = (int) this.getX();
            this.lastY = (int) this.getY();
            this.lastZ = (int) this.getZ();
        }

        if (this.currentFlightTarget == null) {
            this.currentFlightTarget = this.blockPosition();
        }

        if (this.windedCooldownTicks > 0) {
            --this.windedCooldownTicks;
        }

        double distSq = this.currentFlightTarget.distSqr(this.blockPosition());

        if (this.stuckCount > 30 || this.random.nextInt(300) == 0 || distSq < 4.0) {
            this.stuckCount = 0;
            int keepTrying = 50;
            while (keepTrying > 0) {
                int xdir = (this.random.nextInt(14) + 10) * (this.random.nextInt(2) == 0 ? -1 : 1);
                int zdir = (this.random.nextInt(14) + 10) * (this.random.nextInt(2) == 0 ? -1 : 1);

                BlockPos newTarget = new BlockPos(
                        (int) this.getX() + xdir,
                        (int) this.getY() + this.random.nextInt(6) - 3,
                        (int) this.getZ() + zdir);

                if (this.level().getBlockState(newTarget).isAir()) {
                    this.currentFlightTarget = newTarget;
                    break;
                }
                --keepTrying;
            }
        }

        LivingEntity currentTarget = findSomethingToAttack();
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

    @Nullable
    private LivingEntity findSomethingToAttack() {
        // TF-035: orig Vortex.java:341-344 — PlayNicely disables Vortex
        // aggression entirely, and the scan sorts with GenericTargetSorter
        // (creeper-halved / big-mob-prioritized), not plain distance.
        if (danger.orespawn.OreSpawnConfig.PLAY_NICELY.get()) return null;
        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(16.0, 10.0, 16.0));
        entities.sort(new danger.orespawn.entity.ai.GenericTargetSorter(this));
        for (LivingEntity candidate : entities) {
            if (isSuitableTarget(candidate)) return candidate;
        }
        return null;
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
