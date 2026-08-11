package danger.orespawn.entity;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import danger.orespawn.ModEntities;
import danger.orespawn.ModItems;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.joml.Vector3f;

public class LaserBall extends ThrowableProjectile implements ItemSupplier {
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
    // orig LaserBall.java:141-144 — impact burst runs mx=10 rounds, doubled to
    // 20 when the ball is special (the special type's extra effect, ENT-K-010).
    private static final int IMPACT_BURST_COUNT = 10;
    private static final int IMPACT_BURST_COUNT_SPECIAL = 20;

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

    /**
     * ENTITY_NOOP_RENDERER/laser_ball — orig RenderItemUrchin drew spinner tile 81
     * (orig LaserBall.java:26 my_index=81), pixel-identical to the laser_ball item
     * sprite (textures/items/laserball.png); feeds vanilla ThrownItemRenderer.
     * IceBall/Acid/DeadIrukandji override this with their own tiles (84/85/86).
     */
    @Override
    public ItemStack getItem() {
        return new ItemStack(ModItems.LASER_BALL.get());
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
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        // orig LaserBall.java:137-139 — an irukandji-type ball that hits no
        // entity drops one MyIrukandji item (registered "OreSpawn_Irukandji",
        // unlocalized "deadirukandji" — orig OreSpawnMain.java:1750,2299; the
        // port's dead_irukandji) instead of just vanishing.
        // func_145779_a(item, 1) == spawnAtLocation at y-offset 0.
        if (!this.level().isClientSide && this.isIrukandji) {
            this.spawnAtLocation(ModItems.DEAD_IRUKANDJI.get());
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        // orig LaserBall.java:78-132 — every spared-target branch (irukandji
        // direct hit, acid bugs, robots, ridden dragons, mounted players)
        // discards the ball and RETURNS before the impact-effects block, so a
        // spared hit produces no burst, no sound and no explosion. onHitEntity
        // marks those paths by discarding; mirror the early return here.
        if (this.isRemoved()) return;
        // orig LaserBall.java:140 — acid-type balls (incl. irukandji, which
        // sets the acid flag too) skip all impact effects.
        if (this.isAcid) {
            this.discard();
            return;
        }
        if (this.level() instanceof ServerLevel serverLevel) {
            // orig LaserBall.java:141-149 — burst of smoke + largesmoke +
            // fireworksSpark each round; 10 rounds, 20 when special.
            int burst = this.isSpecial ? IMPACT_BURST_COUNT_SPECIAL : IMPACT_BURST_COUNT;
            for (int i = 0; i < burst; ++i) {
                // orig LaserBall.java:146 — "smoke"; the original Z coordinate
                // only ADDS nextFloat() (its "- nextFloat()" term is missing),
                // skewing the smoke cloud toward +Z. Bug reproduced.
                serverLevel.sendParticles(ParticleTypes.SMOKE,
                        this.getX() + this.random.nextFloat() - this.random.nextFloat(),
                        this.getY() + this.random.nextFloat() - this.random.nextFloat(),
                        this.getZ() + this.random.nextFloat(),
                        1, 0.0, 0.0, 0.0, 0.0);
                // orig LaserBall.java:147 — "largesmoke", symmetric offsets,
                // zero velocity.
                serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE,
                        this.getX() + this.random.nextFloat() - this.random.nextFloat(),
                        this.getY() + this.random.nextFloat() - this.random.nextFloat(),
                        this.getZ() + this.random.nextFloat() - this.random.nextFloat(),
                        1, 0.0, 0.0, 0.0, 0.0);
                // orig LaserBall.java:148 — "fireworksSpark" launched from the
                // exact impact point with full-gaussian velocity (count=0 makes
                // the offset args the particle velocity).
                serverLevel.sendParticles(ParticleTypes.FIREWORK,
                        this.getX(), this.getY(), this.getZ(),
                        0, this.random.nextGaussian(), this.random.nextGaussian(),
                        this.random.nextGaussian(), 1.0);
            }
            // orig LaserBall.java:150 — "random.explode" at 0.5 volume,
            // 1.0 ± 0.5 pitch, for EVERY non-acid impact (not only exploders).
            this.playSound(SoundEvents.GENERIC_EXPLODE.value(), EXPLODE_SOUND_VOLUME,
                    1.0f + (this.random.nextFloat() - this.random.nextFloat()) * 0.5f);
            // orig LaserBall.java:151-153 — special OR iceball balls explode at
            // power 3.0. Block damage followed the mobGriefing gamerule via the
            // isSmoking arg (ExplosionInteraction.MOB reproduces that);
            // isFlaming was hardcoded false in orig createExplosion, so the
            // explosion never starts fires.
            if (this.isSpecial || this.isIceball) {
                this.level().explode(this, this.getX(), this.getY(), this.getZ(),
                        EXPLOSION_POWER, false, Level.ExplosionInteraction.MOB);
            }
        }
        this.discard(); // orig LaserBall.java:155
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
                if (this.isIceball) continue; // orig LaserBall.java:182 — iceballs trail no reddust
                // orig LaserBall.java:183 — non-iceball balls also trail
                // "reddust". In 1.7.10 the three trailing args of
                // spawnParticle("reddust", ...) were COLOR multipliers
                // (EntityReddustFX r/g/b), not velocity, so gaussian/10 gave
                // randomly-tinted near-black specks rather than red dust —
                // reproduced with a per-particle dust color. Bug kept.
                this.level().addParticle(new DustParticleOptions(new Vector3f(
                                (float) (this.random.nextGaussian() / 10.0),
                                (float) (this.random.nextGaussian() / 10.0),
                                (float) (this.random.nextGaussian() / 10.0)), 1.0f),
                        this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
            }
        }
    }
}
