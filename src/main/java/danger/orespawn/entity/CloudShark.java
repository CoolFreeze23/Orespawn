package danger.orespawn.entity;

import danger.orespawn.MobStats;

import danger.orespawn.OreSpawnConfig;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.ai.GenericTargetSorter;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public class CloudShark extends Monster {
    private static final int MIN_CRUISE_ALTITUDE = 120;
    private static final int MAX_CRUISE_ALTITUDE = 140;
    private static final int ALTITUDE_BIAS_UP = 2;
    private static final int ALTITUDE_BIAS_DOWN = -2;

    private BlockPos currentFlightTarget = null;
    // orig CloudShark.java:37,45 — TargetSorter field built once in the ctor.
    private final GenericTargetSorter targetSorter = new GenericTargetSorter(this);

    public CloudShark(EntityType<? extends CloudShark> type, Level level) {
        super(type, level);
        this.xpReward = 5;
    }

    @Override
    protected void registerGoals() {
    }

    public static AttributeSupplier.Builder createAttributes() {
        // orig OreSpawnMain.java:6512 — CloudShark 15 HP / 6 ATK / 5 armor
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MobStats.CLOUD_SHARK.maxHealth())
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ATTACK_DAMAGE, MobStats.CLOUD_SHARK.attackDamage())
                .add(Attributes.ARMOR, MobStats.CLOUD_SHARK.armor());
    }

    @Override
    public void tick() {
        super.tick();
        Vec3 motion = this.getDeltaMovement();
        this.setDeltaMovement(motion.x, motion.y * 0.6, motion.z);
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurtApplied = super.hurt(source, amount);
        Entity damageSourceEntity = source.getEntity();
        if (damageSourceEntity != null && this.currentFlightTarget != null) {
            this.currentFlightTarget = damageSourceEntity.blockPosition();
        }
        return hurtApplied;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        // orig CloudShark.java:61-66 — func_70692_ba permits despawning only
        // while it is NOT daytime; by day the shark persists regardless of
        // player distance.
        if (this.isPersistenceRequired()) return false; // orig :62-64
        return !this.level().isDay(); // orig :65
    }

    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;
        super.customServerAiStep();

        if (this.currentFlightTarget == null) {
            this.currentFlightTarget = this.blockPosition();
        }

        int verticalAltitudeBias = 0;
        if ((int) this.getY() < MIN_CRUISE_ALTITUDE) verticalAltitudeBias = ALTITUDE_BIAS_UP;
        if ((int) this.getY() > MAX_CRUISE_ALTITUDE) verticalAltitudeBias = ALTITUDE_BIAS_DOWN;

        if (this.random.nextInt(300) == 0 || this.currentFlightTarget.closerToCenterThan(this.position(), 2.1)) {
            int pickNewTargetAttempts = 50;
            boolean found = false;
            while (!found && pickNewTargetAttempts > 0) {
                int xdir = this.random.nextInt(10) + 8;
                int zdir = this.random.nextInt(10) + 8;
                if (this.random.nextInt(2) == 0) zdir = -zdir;
                if (this.random.nextInt(2) == 0) xdir = -xdir;
                BlockPos newTarget = new BlockPos(
                        (int) this.getX() + xdir,
                        (int) this.getY() + this.random.nextInt(5) - 2 + verticalAltitudeBias,
                        (int) this.getZ() + zdir);
                if (this.level().getBlockState(newTarget).is(Blocks.AIR)) {
                    this.currentFlightTarget = newTarget;
                    found = true;
                }
                --pickNewTargetAttempts;
            }
        }

        // orig CloudShark.java:153-162 — 1-in-9 tick: hunt any suitable prey,
        // steer straight at it, and bite once inside 3 blocks (distSq < 9).
        if (this.random.nextInt(9) == 2) {
            LivingEntity prey = this.findSomethingToAttack();
            if (prey != null) {
                this.currentFlightTarget = prey.blockPosition();
                if (this.distanceToSqr(prey) < 9.0) {
                    this.doHurtTarget(prey);
                }
            }
        }

        double deltaX = this.currentFlightTarget.getX() + 0.5 - this.getX();
        double deltaY = this.currentFlightTarget.getY() + 0.1 - this.getY();
        double deltaZ = this.currentFlightTarget.getZ() + 0.5 - this.getZ();
        Vec3 motion = this.getDeltaMovement();
        double mx = motion.x + (Math.signum(deltaX) * 0.5 - motion.x) * 0.3;
        double my = motion.y + (Math.signum(deltaY) * 0.7 - motion.y) * 0.2;
        double mz = motion.z + (Math.signum(deltaZ) * 0.5 - motion.z) * 0.3;
        this.setDeltaMovement(mx, my, mz);

        float targetYawDegrees = (float) (Math.atan2(mz, mx) * 180.0 / Math.PI) - 90.0f;
        float yawDelta = Mth.wrapDegrees(targetYawDegrees - this.getYRot());
        this.setYRot(this.getYRot() + yawDelta / 4.0f);
    }

    /**
     * orig CloudShark.java:245-261 — PlayNicely disables hunting outright;
     * otherwise a 12x10x12 box scan sorted by GenericTargetSorter (TF-035:
     * creeper-halved / big-silhouette-prioritized, orig :250) returns the
     * first suitable prey.
     */
    @Nullable
    private LivingEntity findSomethingToAttack() {
        if (OreSpawnConfig.PLAY_NICELY.get()) return null; // orig :246-248
        List<LivingEntity> candidates = this.level().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(12.0, 10.0, 12.0)); // orig :249
        candidates.sort(this.targetSorter); // orig :250
        for (LivingEntity candidate : candidates) {
            if (this.isSuitableTarget(candidate)) return candidate;
        }
        return null;
    }

    /**
     * orig CloudShark.java:202-243 — the prey whitelist: butterflies,
     * cockateils, mosquitos, fireflies, non-creative players, goldfish and
     * cliff racers. Rock monsters and ants are refused before any prey check,
     * and everything else falls through to false. A creative player skips the
     * player branch (orig :233-238) and falls through to the trailing
     * CliffRacer instanceof, so it is never targeted.
     */
    private boolean isSuitableTarget(LivingEntity target) {
        if (target == null) return false; // orig :203-205
        if (target == this) return false; // orig :206-208
        if (!target.isAlive()) return false; // orig :209-211
        if (!this.getSensing().hasLineOfSight(target)) return false; // orig :212-214
        if (target instanceof RockBase) return false; // orig :215-217
        if (target instanceof EntityAnt) return false; // orig :218-220
        if (target instanceof EntityButterfly) return true; // orig :221-223
        if (target instanceof Cockateil) return true; // orig :224-226
        if (target instanceof EntityMosquito) return true; // orig :227-229
        if (target instanceof Firefly) return true; // orig :230-232
        if (target instanceof Player p && !p.getAbilities().instabuild) return true; // orig :233-238
        if (target instanceof GoldFish) return true; // orig :239-241
        return target instanceof EntityCliffRacer; // orig :242
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.GENERIC_SPLASH;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "little_splat"));
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "big_splat"));
    }

    @Override
    protected float getSoundVolume() {
        return 0.25f;
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnType) {
        return true;
    }
}
