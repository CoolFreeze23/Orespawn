package danger.orespawn.entity;

import danger.orespawn.MobStats;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.ai.TargetSelection;

public class EntityRotator extends Monster {
    // OPT-011: cached SoundEvents — identical createVariableRangeEvent ids,
    // allocated once per class instead of on every sound query.
    private static final SoundEvent SND_VORTEXLIVE = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "vortexlive"));
    private static final SoundEvent SND_GLASSHIT = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "glasshit"));
    private static final SoundEvent SND_GLASSDEAD = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "glassdead"));
    private BlockPos currentFlightTarget = null;
    private int busyFighting = 0;
    /**
     * orig Rotator.java:52 {@code was_spawnered} — set when the spawn-rule check
     * finds an own-type spawner nearby (orig :269); spawnered Rotators are exempt
     * from despawning (orig :108) and from the random daytime discard (orig :166).
     * Like the original, not persisted to NBT.
     */
    private int wasSpawnered = 0;

    /**
     * Per-entity render scratch (orig Rotator.java:50 {@code renderdata = new RenderInfo()},
     * accessor orig Rotator.java:86-88). {@code rf1} is the accumulating gyroscope
     * fan angle advanced 2°/frame by the model (orig ModelRotator.java:75-78).
     */
    private final danger.orespawn.entity.client.RenderInfo renderInfo =
            new danger.orespawn.entity.client.RenderInfo();

    /** Mirrors orig Rotator.java:86-88 {@code getRenderInfo()}. */
    public danger.orespawn.entity.client.RenderInfo getRenderInfo() {
        return this.renderInfo;
    }

    public EntityRotator(EntityType<? extends EntityRotator> type, Level level) {
        super(type, level);
        this.xpReward = 35;
        this.noPhysics = true;
    }

    @Override
    protected void registerGoals() {
    }

    public static AttributeSupplier.Builder createAttributes() {
        // orig OreSpawnMain.java:6499 — Rotator 35 HP / 10 ATK / 8 armor
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, MobStats.ROTATOR.maxHealth())
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.ATTACK_DAMAGE, MobStats.ROTATOR.attackDamage())
                .add(Attributes.ARMOR, MobStats.ROTATOR.armor());
    }

    @Override
    public boolean removeWhenFarAway(double dist) {
        if (this.isPersistenceRequired()) return false;
        if (this.busyFighting != 0) return false;
        // orig Rotator.java:108 — spawner-spawned Rotators never despawn
        return this.wasSpawnered == 0;
    }

    @Override
    public void tick() {
        super.tick();
        Vec3 motion = this.getDeltaMovement();
        this.setDeltaMovement(motion.x, motion.y * 0.6, motion.z);

        if (this.level().isClientSide && this.random.nextInt(10) == 1) {
            this.level().addParticle(ParticleTypes.FIREWORK,
                    this.getX(), this.getY() + 1.4, this.getZ(),
                    (this.random.nextFloat() - this.random.nextFloat()) / 4.0,
                    (this.random.nextFloat() - this.random.nextFloat()) / 4.0,
                    (this.random.nextFloat() - this.random.nextFloat()) / 4.0);
        }

        this.busyFighting = 0;
        LivingEntity target = this.findSomethingToAttack();
        if (target != null) {
            if (this.level().isClientSide) {
                double angle = Math.atan2(target.getZ() - this.getZ(), target.getX() - this.getX());
                this.level().addParticle(ParticleTypes.FIREWORK,
                        this.getX(), this.getY() + 1.4, this.getZ(),
                        Math.cos(angle), (target.getY() - this.getY()) / 10.0, Math.sin(angle));
            }
            this.busyFighting = 1;
        }

        if (!this.level().isClientSide && !this.isPersistenceRequired() && this.busyFighting == 0
                && this.wasSpawnered == 0) { // orig Rotator.java:166 — spawnered Rotators skip the daytime discard
            long dayTimeInCycle = this.level().getDayTime() % 24000L;
            if (dayTimeInCycle < 12000L && this.random.nextInt(400) == 1) {
                this.discard();
            }
        }
    }

    /**
     * orig Rotator.java:255-288 — spawner bypass (x/z -2..+2, y +1..+3; flags
     * {@code was_spawnered}); darkness; clear air above; night half of the day only.
     */
    @Override
    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level,
                                   net.minecraft.world.entity.MobSpawnType spawnType) {
        if (OriginalSpawnGates.nearOwnSpawner(this, level, -2, 2, 1, 3)) {
            this.wasSpawnered = 1;
            return true;
        }
        if (!OriginalSpawnGates.isDarkEnough(this, level)) return false;
        if (!OriginalSpawnGates.airBox(this, level, -1, 1, 1, 2, -1, 1)) return false;
        return level.dayTime() % 24000L >= 12000L;
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        Entity attacker = source.getEntity();
        if (attacker instanceof AbstractArrow) {
            return false;
        }
        boolean ret = super.hurt(source, amount);
        if (attacker != null && this.currentFlightTarget != null) {
            this.currentFlightTarget = attacker.blockPosition();
        }
        return ret;
    }

    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;
        super.customServerAiStep();

        if (this.currentFlightTarget == null) {
            this.currentFlightTarget = this.blockPosition();
        }

        if (this.random.nextInt(300) == 0 || this.currentFlightTarget.closerToCenterThan(this.position(), 2.1)) {
            int keepTrying = 50;
            boolean found = false;
            while (!found && keepTrying > 0) {
                int xdir = this.random.nextInt(10) + 8;
                int zdir = this.random.nextInt(10) + 8;
                if (this.random.nextInt(2) == 0) zdir = -zdir;
                if (this.random.nextInt(2) == 0) xdir = -xdir;
                BlockPos newTarget = BlockPos.containing(
                        this.getX() + xdir,
                        this.getY() + this.random.nextInt(6) - 3,
                        this.getZ() + zdir);
                if (this.level().getBlockState(newTarget).isAir()) {
                    this.currentFlightTarget = newTarget;
                    found = true;
                }
                --keepTrying;
            }
        } else if (this.random.nextInt(9) == 2) {
            LivingEntity target = this.findSomethingToAttack();
            if (target != null) {
                double angle = Math.atan2(target.getZ() - this.getZ(), target.getX() - this.getX());
                this.currentFlightTarget = BlockPos.containing(
                        target.getX() + 2.5 * Math.cos(angle + Math.PI / 2),
                        target.getY(),
                        target.getZ() + 2.5 * Math.sin(angle + Math.PI / 2));
                if (this.distanceToSqr(target) < 9.0) {
                    this.doHurtTarget(target);
                }
            }
        }

        double dx = this.currentFlightTarget.getX() + 0.5 - this.getX();
        double dy = this.currentFlightTarget.getY() + 0.1 - this.getY();
        double dz = this.currentFlightTarget.getZ() + 0.5 - this.getZ();
        Vec3 motion = this.getDeltaMovement();
        double mx = motion.x + (Math.signum(dx) * 0.4 - motion.x) * 0.2;
        double my = motion.y + (Math.signum(dy) * 0.7 - motion.y) * 0.2;
        double mz = motion.z + (Math.signum(dz) * 0.4 - motion.z) * 0.2;
        this.setDeltaMovement(mx, my, mz);

        float yaw = (float) (Math.atan2(mz, mx) * 180.0 / Math.PI) - 90.0f;
        float yawDelta = Mth.wrapDegrees(yaw - this.getYRot());
        this.setYRot(this.getYRot() + yawDelta / 4.0f);
    }

    /**
     * orig Rotator.java:294-365 — beyond null/self/dead (:295-303),
     * MyUtils.isIgnoreable (:304-306), line of sight (:307-309), and creative
     * players (:310-315), the original spares sixteen species besides itself:
     * Termite (:316), Vortex (:319), DungeonBeast (:325), Peacock (:328),
     * CrystalCow (:331), Irukandji (:334), Skate (:337), Whale (:340),
     * Flounder (:343), Urchin (:346), TerribleTerror (:349), LurkingTerror (:352),
     * CloudShark (:355), Mothra (:358), Bee (:361), Mantis (:364). Everything
     * else — players included — is fair game.
     */
    private boolean isSuitableTarget(LivingEntity target) {
        if (target == null || target == this || !target.isAlive()) return false;
        if (danger.orespawn.util.MyUtils.isIgnoreable(target)) return false; // orig :304
        if (!this.getSensing().hasLineOfSight(target)) return false; // orig :307
        if (target instanceof Player p && p.getAbilities().instabuild) return false; // orig :310-315 creative
        if (target instanceof EntityTermite) return false;        // orig :316
        if (target instanceof EntityVortex) return false;         // orig :319
        if (target instanceof EntityRotator) return false;        // orig :322
        if (target instanceof DungeonBeast) return false;         // orig :325
        if (target instanceof Peacock) return false;              // orig :328
        if (target instanceof CrystalCow) return false;           // orig :331
        if (target instanceof Irukandji) return false;            // orig :334
        if (target instanceof Skate) return false;                // orig :337
        if (target instanceof Whale) return false;                // orig :340
        if (target instanceof Flounder) return false;             // orig :343
        if (target instanceof Urchin) return false;               // orig :346
        if (target instanceof EntityTerribleTerror) return false; // orig :349
        if (target instanceof EntityLurkingTerror) return false;  // orig :352
        if (target instanceof CloudShark) return false;           // orig :355
        if (target instanceof Mothra) return false;               // orig :358
        if (target instanceof EntityBee) return false;            // orig :361
        return !(target instanceof EntityMantis);                 // orig :364
    }

    private LivingEntity findSomethingToAttack() {
        // TF-035: orig Rotator.java:367-383 — PlayNicely disables Rotator aggression
        // entirely (:368-370), and the scan sorts with GenericTargetSorter
        // (field :49, ctor :60, sort :372), not plain distance.
        if (danger.orespawn.OreSpawnConfig.PLAY_NICELY.get()) return null;
        AABB searchBox = this.getBoundingBox().inflate(12.0, 10.0, 12.0);
        List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, searchBox);
        // OPT-021: nearest-first pick without the full list sort; TargetSelection
        // preserves the removed sort's order and stable-tie semantics exactly.
        return TargetSelection.firstMatch(targets, new danger.orespawn.entity.ai.GenericTargetSorter(this), this::isSuitableTarget);
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return SND_VORTEXLIVE;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SND_GLASSHIT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SND_GLASSDEAD;
    }

    @Override
    protected float getSoundVolume() {
        return 0.75f;
    }
}
