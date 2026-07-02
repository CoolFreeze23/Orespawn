package danger.orespawn.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import danger.orespawn.ModEntities;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class LaserBall extends ThrowableProjectile {
    private static final int MAX_LIFETIME_TICKS = 200;
    private static final float ROTATION_STEP_DEGREES = 50.0f;
    private static final float FULL_ROTATION_DEGREES = 360.0f;
    private static final float BASE_DAMAGE = 16.0f;
    private static final int FIRE_DURATION_ON_HIT_SECONDS = 1;
    private static final float EXPLOSION_POWER = 3.0f;
    private static final float EXPLODE_SOUND_VOLUME = 0.5f;
    private static final int PARTICLE_COUNT_SPECIAL = 10;
    private static final int PARTICLE_COUNT_ICE = 2;
    private static final int PARTICLE_COUNT_DEFAULT = 4;
    private static final float IRUKANDJI_DAMAGE = 100.0f;

    private float visualRotationDegrees = 0.0f;
    private boolean isSpecial = false;
    private boolean isIceball = false;
    private boolean isAcid = false;
    private boolean isIrukandji = false;
    private int ticksAlive = 0;

    public LaserBall(EntityType<? extends ThrowableProjectile> type, Level level) {
        super(type, level);
    }

    public LaserBall(Level level, LivingEntity shooter) {
        super(ModEntities.LASER_BALL.get(), shooter, level);
    }

    public LaserBall(Level level, double x, double y, double z) {
        super(ModEntities.LASER_BALL.get(), level);
        this.setPos(x, y, z);
    }

    // Subclass plumbing so Acid/IceBall/DeadIrukandji keep their own entity type
    // (renderer registration is keyed by type).
    protected LaserBall(EntityType<? extends ThrowableProjectile> type, LivingEntity shooter, Level level) {
        super(type, shooter, level);
    }

    protected LaserBall(EntityType<? extends ThrowableProjectile> type, Level level, double x, double y, double z) {
        super(type, level);
        this.setPos(x, y, z);
    }

    public void setSpecial() { this.isSpecial = true; }
    public void setIceBall() { this.isIceball = true; }
    public void setAcid() { this.isAcid = true; }
    public void setIrukandji() { this.isIrukandji = true; this.isAcid = true; }
    /** Legacy parameter ignored; enables iceball behavior. */
    public void setIceMaker(int unusedVariant) { this.isIceball = true; }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (this.level().isClientSide) return;
        Entity target = result.getEntity();

        if (this.isIrukandji) {
            target.hurt(this.damageSources().thrown(this, this.getOwner()), IRUKANDJI_DAMAGE);
            this.discard();
            return;
        }

        // orig LaserBall.java:83-92 — acid-type balls never damage the acid
        // bugs that spit them (TrooperBug/SpitBug); the ball just vanishes.
        if (this.isAcid && (target instanceof EntityTrooperBug || target instanceof EntitySpitBug)) {
            this.discard();
            return;
        }

        // orig LaserBall.java:93-114 — non-ice, non-acid balls never damage the
        // robot family.
        if (!this.isIceball && !this.isAcid
                && (target instanceof Robot2 || target instanceof Robot3
                        || target instanceof Robot4 || target instanceof Robot5
                        || target instanceof GiantRobot)) {
            this.discard();
            return;
        }

        // orig LaserBall.java:115-125 — non-acid balls spare ridden dragons, and
        // iceballs spare every non-type-0 dragon.
        if (target instanceof Dragon dragon && !this.isAcid) {
            if (dragon.isVehicle()) {
                this.discard();
                return;
            }
            if (dragon.getDragonType() != 0 && this.isIceball) {
                this.discard();
                return;
            }
        }

        // orig LaserBall.java:126-132 — non-acid balls spare mounted players.
        if (target instanceof Player player && !this.isAcid && player.isPassenger()) {
            this.discard();
            return;
        }

        target.hurt(this.damageSources().thrown(this, this.getOwner()), BASE_DAMAGE);
        if (!this.isIceball) {
            target.igniteForSeconds(FIRE_DURATION_ON_HIT_SECONDS);
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide && !this.isAcid) {
            if (this.isSpecial || this.isIceball) {
                boolean canGrief = this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
                this.level().explode(this, this.getX(), this.getY(), this.getZ(), EXPLOSION_POWER,
                        canGrief, Level.ExplosionInteraction.MOB);
            }
            this.playSound(SoundEvents.GENERIC_EXPLODE.value(), EXPLODE_SOUND_VOLUME,
                    1.0f + (this.random.nextFloat() - this.random.nextFloat()) * 0.5f);
        }
        this.discard();
    }

    @Override
    public void tick() {
        ++this.ticksAlive;
        if (this.ticksAlive > MAX_LIFETIME_TICKS) {
            this.discard();
            return;
        }
        super.tick();
        this.visualRotationDegrees += ROTATION_STEP_DEGREES;
        if (this.visualRotationDegrees > FULL_ROTATION_DEGREES) this.visualRotationDegrees -= FULL_ROTATION_DEGREES;
        this.setXRot(this.visualRotationDegrees);

        if (this.level().isClientSide && !this.isAcid) {
            int particleCount = this.isSpecial ? PARTICLE_COUNT_SPECIAL
                    : (this.isIceball ? PARTICLE_COUNT_ICE : PARTICLE_COUNT_DEFAULT);
            for (int particleIndex = 0; particleIndex < particleCount; ++particleIndex) {
                this.level().addParticle(ParticleTypes.FIREWORK,
                        this.getX(), this.getY(), this.getZ(),
                        this.random.nextGaussian() / 2.0,
                        this.random.nextGaussian() / 2.0,
                        this.random.nextGaussian() / 2.0);
            }
        }
    }
}
