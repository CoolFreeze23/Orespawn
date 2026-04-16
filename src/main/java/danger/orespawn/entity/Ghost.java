package danger.orespawn.entity;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import danger.orespawn.OreSpawnMod;

/**
 * Ghost — hauntingly hovers toward the nearest player and inflicts small
 * contact damage. 1.7.10's EntityAmbientCreature subclass had no damage at
 * all; Phase 4D (dimension locals) intentionally upgrades it to a
 * MobCategory.MONSTER with a weak touch attack so Chaos dimension spawns
 * feel threatening. The flight/hover AI is kept identical to 1.7.10 so
 * the silhouette and movement cadence are preserved.
 */
public class Ghost extends AmbientCreature {
    private static final double CONTACT_DAMAGE_RANGE_SQ = 1.5 * 1.5;
    private static final int ATTACK_COOLDOWN_TICKS = 20;

    private BlockPos currentFlightTarget = null;
    private int attackCooldown = 0;

    public Ghost(EntityType<? extends Ghost> type, Level level) {
        super(type, level);
        this.xpReward = 5;
        this.noPhysics = true;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 12.0f));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 2.0)
                .add(Attributes.MOVEMENT_SPEED, 0.1)
                .add(Attributes.ATTACK_DAMAGE, 2.0)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        return target.hurt(this.damageSources().mobAttack(this),
                (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE));
    }

    @Override
    public void tick() {
        if (this.isPersistenceRequired()) this.noPhysics = false;
        super.tick();
        Vec3 motion = this.getDeltaMovement();
        this.setDeltaMovement(motion.x, motion.y * 0.65, motion.z);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getMsgId().equals("inWall")) return false;
        return super.hurt(source, amount);
    }

    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;
        super.customServerAiStep();

        if (this.attackCooldown > 0) this.attackCooldown--;

        // Contact damage: if a player is within ~1.5 blocks, drain them.
        // 1.7.10 Ghosts had no hurt logic; the Phase 4D Chaos-dimension
        // brief promotes them to weak hostile mobs, so damage is tied to
        // physical proximity rather than a MeleeAttackGoal to preserve
        // the drift-only movement profile. We prefer goalSelector's target
        // but fall back to nearest-player detection so the damage still
        // triggers even though AmbientCreature doesn't fully exercise the
        // aggro pipeline.
        if (this.attackCooldown == 0) {
            LivingEntity aggroTarget = this.getTarget();
            if (aggroTarget == null) {
                aggroTarget = this.level().getNearestPlayer(this, Math.sqrt(CONTACT_DAMAGE_RANGE_SQ));
            }
            if (aggroTarget != null && aggroTarget.isAlive()
                    && this.distanceToSqr(aggroTarget) <= CONTACT_DAMAGE_RANGE_SQ) {
                this.doHurtTarget(aggroTarget);
                this.attackCooldown = ATTACK_COOLDOWN_TICKS;
            }
        }

        if (this.currentFlightTarget == null) {
            this.currentFlightTarget = this.blockPosition();
        }

        double distSq = this.currentFlightTarget.distSqr(this.blockPosition());
        if (this.random.nextInt(40) == 1 || distSq < 2.0) {
            Player target = this.level().getNearestPlayer(this, 16.0);
            if (target != null) {
                this.currentFlightTarget = new BlockPos(
                        (int) target.getX() + this.random.nextInt(3) - this.random.nextInt(3),
                        (int) target.getY() + 1,
                        (int) target.getZ() + this.random.nextInt(3) - this.random.nextInt(3));
            } else {
                int firstAirOffsetAbove;
                for (firstAirOffsetAbove = 0; firstAirOffsetAbove < 3; firstAirOffsetAbove++) {
                    if (this.level().getBlockState(new BlockPos((int) this.getX(),
                            (int) this.getY() + firstAirOffsetAbove, (int) this.getZ())).isAir()) {
                        break;
                    }
                }
                int belowOffset;
                for (belowOffset = -1; belowOffset >= -3; belowOffset--) {
                    if (!this.level().getBlockState(new BlockPos((int) this.getX(),
                            (int) this.getY() + belowOffset, (int) this.getZ())).isAir()) {
                        break;
                    }
                }
                this.currentFlightTarget = new BlockPos(
                        (int) this.getX() + this.random.nextInt(10) - this.random.nextInt(10),
                        (int) this.getY() + firstAirOffsetAbove + belowOffset + this.random.nextInt(4) + 1,
                        (int) this.getZ() + this.random.nextInt(10) - this.random.nextInt(10));
            }
        }

        double dx = this.currentFlightTarget.getX() + 0.5 - this.getX();
        double dy = this.currentFlightTarget.getY() + 0.1 - this.getY();
        double dz = this.currentFlightTarget.getZ() + 0.5 - this.getZ();
        Vec3 motion = this.getDeltaMovement();
        double mx = motion.x + (Math.signum(dx) * 0.1 - motion.x) * 0.05;
        double my = motion.y + (Math.signum(dy) * 0.7 - motion.y) * 0.1;
        double mz = motion.z + (Math.signum(dz) * 0.1 - motion.z) * 0.05;
        this.setDeltaMovement(mx, my, mz);

        float targetYaw = (float) (Math.atan2(mz, mx) * 180.0 / Math.PI) - 90.0f;
        float yawDiff = Mth.wrapDegrees(targetYaw - this.getYRot());
        this.zza = 0.05f;
        this.setYRot(this.getYRot() + yawDiff / 6.0f);
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        if (this.random.nextInt(2) == 0) {
            return SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "ghost_sound"));
        }
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) { return null; }
    @Override
    protected SoundEvent getDeathSound() { return null; }
    @Override
    protected float getSoundVolume() { return 0.3f; }
    @Override
    public boolean isPushable() { return false; }
    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) { return false; }
    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) { }

    @Override
    public boolean removeWhenFarAway(double dist) {
        return !this.isPersistenceRequired();
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnType) {
        return !level.canSeeSky(this.blockPosition());
    }
}
