package danger.orespawn.entity;

import danger.orespawn.MobStats;

import java.util.Comparator;
import java.util.List;

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
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.ai.TargetSelection;

public class EntityTerribleTerror extends Monster {
    // OPT-011: cached SoundEvents — identical createVariableRangeEvent ids,
    // allocated once per class instead of on every sound query.
    private static final SoundEvent SND_TERRIBLETERROR_LIVING = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "terribleterror_living"));
    private static final SoundEvent SND_TERRIBLETERROR_HIT = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "terribleterror_hit"));
    private static final SoundEvent SND_TERRIBLETERROR_DEAD = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "terribleterror_dead"));
    private BlockPos currentFlightTarget = null;

    public EntityTerribleTerror(EntityType<? extends EntityTerribleTerror> type, Level level) {
        super(type, level);
        this.xpReward = 10;
        this.noPhysics = false;
    }

    @Override
    protected void registerGoals() {
    }

    public static AttributeSupplier.Builder createAttributes() {
        // orig OreSpawnMain.java:6520 — TerribleTerror 10 HP / 5 ATK / 3 armor
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, MobStats.TERRIBLE_TERROR.maxHealth())
                .add(Attributes.MOVEMENT_SPEED, 0.1)
                .add(Attributes.ATTACK_DAMAGE, MobStats.TERRIBLE_TERROR.attackDamage())
                .add(Attributes.ARMOR, MobStats.TERRIBLE_TERROR.armor());
    }

    @Override
    public boolean removeWhenFarAway(double dist) {
        if (this.isPersistenceRequired()) return false;
        return this.level().isDay();
    }

    @Override
    public void tick() {
        super.tick();
        Vec3 motion = this.getDeltaMovement();
        this.setDeltaMovement(motion.x, motion.y * 0.6, motion.z);

        if (!this.level().isClientSide && !this.isPersistenceRequired()) {
            long t = this.level().getDayTime() % 24000L;
            if (t < 12000L && this.random.nextInt(400) == 1) {
                this.discard();
            }
        }
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean ret = super.hurt(source, amount);
        Entity attacker = source.getEntity();
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

        if (this.random.nextInt(100) == 0 || this.currentFlightTarget.closerToCenterThan(this.position(), 2.1)) {
            int keepTrying = 50;
            boolean found = false;
            while (!found && keepTrying > 0) {
                int xdir = this.random.nextInt(5) + 5;
                int zdir = this.random.nextInt(5) + 5;
                if (this.random.nextInt(2) == 0) zdir = -zdir;
                if (this.random.nextInt(2) == 0) xdir = -xdir;
                BlockPos newTarget = BlockPos.containing(
                        this.getX() + xdir,
                        this.getY() + this.random.nextInt(5) - 2,
                        this.getZ() + zdir);
                if (this.level().getBlockState(newTarget).isAir()) {
                    this.currentFlightTarget = newTarget;
                    found = true;
                }
                --keepTrying;
            }
        } else if (this.random.nextInt(9) == 0) {
            LivingEntity target = this.findSomethingToAttack();
            if (target != null) {
                this.currentFlightTarget = BlockPos.containing(target.getX(), target.getY() + 1, target.getZ());
                if (this.distanceToSqr(target) < 6.0) {
                    this.doHurtTarget(target);
                }
            }
        }

        double dx = this.currentFlightTarget.getX() + 0.4 - this.getX();
        double dy = this.currentFlightTarget.getY() + 0.1 - this.getY();
        double dz = this.currentFlightTarget.getZ() + 0.4 - this.getZ();
        Vec3 motion = this.getDeltaMovement();
        double mx = motion.x + (Math.signum(dx) * 0.4 - motion.x) * 0.3;
        double my = motion.y + (Math.signum(dy) * 0.7 - motion.y) * 0.2;
        double mz = motion.z + (Math.signum(dz) * 0.4 - motion.z) * 0.3;
        this.setDeltaMovement(mx, my, mz);

        float yaw = (float) (Math.atan2(mz, mx) * 180.0 / Math.PI) - 90.0f;
        float yawDelta = Mth.wrapDegrees(yaw - this.getYRot());
        this.setYRot(this.getYRot() + yawDelta / 4.0f);
    }

    /**
     * orig TerribleTerror.java:216-293 — anything alive and visible except its own kind,
     * the listed OreSpawn species and creative players. ENT-S-109: the player branch's
     * {@code capabilities.isCreativeMode} (orig :286-291) is {@code Abilities.instabuild}
     * — the ENT-S-107 mapping — not {@code invulnerable}.
     */
    private boolean isSuitableTarget(LivingEntity target) {
        if (target == null || target == this || !target.isAlive()) return false;
        if (!this.getSensing().hasLineOfSight(target)) return false;
        if (target instanceof EntityTerribleTerror) return false;
        if (target instanceof RockBase) return false;
        if (target instanceof EnderReaper) return false;
        if (target instanceof CloudShark) return false;
        if (target instanceof EntityRotator) return false;
        if (target instanceof PitchBlack) return false;
        if (target instanceof CreepingHorror) return false;
        if (target instanceof Island) return false;
        if (target instanceof IslandToo) return false;
        if (target instanceof Player player) {
            return !player.getAbilities().instabuild; // orig TerribleTerror.java:286-291 isCreativeMode (ENT-S-109)
        }
        return true;
    }

    private LivingEntity findSomethingToAttack() {
        AABB searchBox = this.getBoundingBox().inflate(12.0, 8.0, 12.0);
        List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, searchBox);
        // OPT-021: nearest-first pick without the full list sort; TargetSelection
        // preserves the removed sort's order and stable-tie semantics exactly.
        return TargetSelection.firstMatch(targets, Comparator.comparingDouble(this::distanceToSqr), this::isSuitableTarget);
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return SND_TERRIBLETERROR_LIVING;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SND_TERRIBLETERROR_HIT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SND_TERRIBLETERROR_DEAD;
    }

    @Override
    protected float getSoundVolume() {
        return 0.45f;
    }

    /** orig TerribleTerror.java:193-214 — "Terrible Terror" spawner bypass (x/z -2..+1); darkness; night; Chaos always allowed, otherwise y<=40. */
    @Override
    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level,
                                   net.minecraft.world.entity.MobSpawnType spawnType) {
        if (OriginalSpawnGates.nearOwnSpawner(this, level, -2, 1, 0, 4)) return true;
        if (!OriginalSpawnGates.isDarkEnough(this, level)) return false;
        if (OriginalSpawnGates.isDaytime(level)) return false;
        if (danger.orespawn.ModDimensionKeys.isIn(level, danger.orespawn.ModDimensionKeys.CHAOS)) return true;
        return this.getY() <= 40.0;
    }
}
