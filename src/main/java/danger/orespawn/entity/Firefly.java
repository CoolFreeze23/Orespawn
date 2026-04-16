package danger.orespawn.entity;

import danger.orespawn.entity.ai.AmbientFlightGoal;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Firefly — tiny luminous ambient mob.
 *
 * <p>Flight is delegated to {@link AmbientFlightGoal} with the firefly
 * preset (small 4-block range, slow 0.2 forward speed, quarter-turn yaw
 * damping for a gentle bobbing feel). The legacy 1.7.10 inline flight
 * loop has been removed in favour of the Goal system so vanilla goals
 * can coexist cleanly.
 *
 * <p>Non-AI responsibilities kept here:
 * <ul>
 *   <li>{@link #blinker}: server-side light-blink timer (client-only visual
 *       driven by {@link #getBlink()}).</li>
 *   <li>Daytime despawn: 1-in-500 discard roll each tick while the day is
 *       past tick 11000, matching 1.7.10 behaviour.</li>
 *   <li>{@code canSeeSky} despawn suppression in {@link #removeWhenFarAway}
 *       so sheltered fireflies stick around for cave lighting.</li>
 * </ul>
 */
public class Firefly extends AmbientCreature {
    private static final long DAY_LENGTH_TICKS = 24000L;
    private static final long NIGHTFALL_START_TICK = 11000L;

    int myBlink = 20 + this.random.nextInt(20);
    int blinker = 0;

    public Firefly(EntityType<? extends Firefly> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 1.0)
                .add(Attributes.MOVEMENT_SPEED, 0.1)
                .add(Attributes.ATTACK_DAMAGE, 0.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(8, new AmbientFlightGoal(this, AmbientFlightGoal.Params.firefly()));
    }

    public float getBlink() {
        return this.blinker < this.myBlink / 2 ? 240.0f : 0.0f;
    }

    @Override
    public void tick() {
        super.tick();
        Vec3 motion = this.getDeltaMovement();
        this.setDeltaMovement(motion.x, motion.y * 0.6, motion.z);
        this.blinker++;
        if (this.blinker > this.myBlink) this.blinker = 0;
        if (this.isPersistenceRequired()) return;
        long dayTime = this.level().getDayTime() % DAY_LENGTH_TICKS;
        if (dayTime > NIGHTFALL_START_TICK) return;
        if (this.random.nextInt(500) == 1) this.discard();
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() { return null; }
    @Override
    protected SoundEvent getHurtSound(DamageSource source) { return null; }
    @Override
    protected SoundEvent getDeathSound() { return null; }
    @Override
    protected float getSoundVolume() { return 0.0f; }

    @Override
    public boolean isPushable() { return true; }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) { return false; }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) { }

    @Override
    public boolean removeWhenFarAway(double dist) {
        if (this.level().canSeeSky(this.blockPosition())) return false;
        return !this.isPersistenceRequired();
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnType) {
        if (!level.getBlockState(this.blockPosition()).isAir()) return false;
        if (level.canSeeSky(this.blockPosition())) return false;
        int buddies = level.getEntitiesOfClass(Firefly.class,
                this.getBoundingBox().inflate(20.0, 8.0, 20.0)).size();
        if (buddies > 10) return false;
        return this.getY() >= 50.0;
    }
}
