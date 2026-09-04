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
import danger.orespawn.OreSpawnConfig;
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
     * orig TerribleTerror.java:216-293 — anything alive and visible except the spared kinds and creative
     * players. The species chain (:229-285) in the original's order: RockBase (:229), TerribleTerror
     * (:232), EnderReaper (:235), Mothra (:238), LurkingTerror (:241), CloudShark (:244), Rotator (:247),
     * Bee (:250), Mantis (:253), LeafMonster (:256), CreepingHorror (:259), Triffid (:262), PitchBlack
     * (:265), Dragon (:268), Island (:271), IslandToo (:274), EntityButterfly (:277), Firefly (:280), and a
     * second Triffid step (:283, unreachable behind :262 — not repeated). ENT-S-128: the port list had
     * dropped Mothra, LurkingTerror, Bee, Mantis, LeafMonster, Triffid, Dragon, EntityButterfly and
     * Firefly — nine kinds hunted that 1.7.10 spared — restored here (the port Mothra is an
     * EntityButterfly, refused by :238 and :277 alike). ENT-S-109: the player branch's
     * {@code capabilities.isCreativeMode} (orig :286-291) is {@code Abilities.instabuild} — the ENT-S-107
     * mapping — not {@code invulnerable}.
     */
    private boolean isSuitableTarget(LivingEntity target) {
        if (target == null || target == this || !target.isAlive()) return false;
        if (!this.getSensing().hasLineOfSight(target)) return false;
        if (target instanceof RockBase) return false;               // orig TerribleTerror.java:229-231
        if (target instanceof EntityTerribleTerror) return false;   // orig :232-234 TerribleTerror
        if (target instanceof EnderReaper) return false;            // orig :235-237
        if (target instanceof Mothra) return false;                 // orig :238-240 (ENT-S-128)
        if (target instanceof EntityLurkingTerror) return false;    // orig :241-243 LurkingTerror (ENT-S-128)
        if (target instanceof CloudShark) return false;             // orig :244-246
        if (target instanceof EntityRotator) return false;          // orig :247-249 Rotator
        if (target instanceof EntityBee) return false;              // orig :250-252 Bee (ENT-S-128)
        if (target instanceof EntityMantis) return false;           // orig :253-255 Mantis (ENT-S-128)
        if (target instanceof EntityLeafMonster) return false;      // orig :256-258 LeafMonster (ENT-S-128)
        if (target instanceof CreepingHorror) return false;         // orig :259-261
        if (target instanceof EntityTriffid) return false;          // orig :262-264 Triffid (ENT-S-128; :283 repeats it)
        if (target instanceof PitchBlack) return false;             // orig :265-267
        if (target instanceof Dragon) return false;                 // orig :268-270 (ENT-S-128)
        if (target instanceof Island) return false;                 // orig :271-273
        if (target instanceof IslandToo) return false;              // orig :274-276
        if (target instanceof EntityButterfly) return false;        // orig :277-279 (ENT-S-128)
        if (target instanceof Firefly) return false;                // orig :280-282 (ENT-S-128)
        if (target instanceof Player player) {
            return !player.getAbilities().instabuild; // orig TerribleTerror.java:286-291 isCreativeMode (ENT-S-109)
        }
        return true;                                                // orig :292
    }

    private LivingEntity findSomethingToAttack() {
        if (OreSpawnConfig.PLAY_NICELY.get()) return null; // orig TerribleTerror.java:296-298 — PlayNicely != 0 returns null ahead of the scan (ENT-S-115)
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
