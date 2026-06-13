package danger.orespawn.entity;

import danger.orespawn.MobStats;

import danger.orespawn.OreSpawnMod;
import java.util.Comparator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.Mth;

public class EntityMantis extends Monster {
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(EntityMantis.class, EntityDataSerializers.INT);

    private BlockPos currentFlightTarget = null;
    private int stuckCount = 0;
    private int lastX = 0;
    private int lastZ = 0;
    private Entity retaliationTarget = null;

    public EntityMantis(EntityType<? extends EntityMantis> type, Level level) {
        super(type, level);
        this.xpReward = 100;
    }

    @Override
    protected void registerGoals() {
        // Mantis retains its bespoke flight AI inside customServerAiStep (it's
        // the only flying hostile in Phase 4B and its aerial pathing doesn't
        // map cleanly onto the BugMeleeAttackGoal framework). We still wire
        // up modern target acquisition here so vanilla "hurt-by" retaliation
        // and proximity aggression use getTarget() — the legacy
        // retaliationTarget field can now be backed by the proper target
        // slot, letting it appear correctly in the boss pathfinding HUD.
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public static AttributeSupplier.Builder createAttributes() {
        // orig OreSpawnMain.java:6467 — Mantis 120 HP / 16 ATK / 10 armor;
        // speed 0.32 hardcoded in orig Mantis.java:68.
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MobStats.MANTIS.maxHealth())
                .add(Attributes.MOVEMENT_SPEED, 0.32)
                .add(Attributes.ATTACK_DAMAGE, MobStats.MANTIS.attackDamage())
                .add(Attributes.ARMOR, MobStats.MANTIS.armor());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, 0);
    }

    public int getAttacking() {
        return this.entityData.get(DATA_ATTACKING);
    }

    public void setAttacking(int value) {
        this.entityData.set(DATA_ATTACKING, value);
    }

    @Override
    protected float getSoundVolume() {
        return 0.35f;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "beebuzz"));
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "dragonfly_hurt"));
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "alo_death"));
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
        if (this.isInWater() && this.random.nextInt(20) == 1) {
            this.doHurtTarget(this);
        }
    }

    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;
        super.customServerAiStep();

        if (this.lastX == (int) this.getX() && this.lastZ == (int) this.getZ()) {
            ++this.stuckCount;
        } else {
            this.stuckCount = 0;
            this.lastX = (int) this.getX();
            this.lastZ = (int) this.getZ();
        }

        if (this.currentFlightTarget == null) {
            this.currentFlightTarget = this.blockPosition();
        }

        double distSq = this.currentFlightTarget.distSqr(this.blockPosition());

        if (this.stuckCount > 50 || this.random.nextInt(300) == 0 || distSq < 4.0) {
            this.stuckCount = 0;
            int keepTrying = 50;
            while (keepTrying > 0) {
                int xdir = this.random.nextInt(9) + 4;
                int zdir = this.random.nextInt(9) + 4;
                if (this.random.nextInt(2) == 0) zdir = -zdir;
                if (this.random.nextInt(2) == 0) xdir = -xdir;

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
        } else if (this.random.nextInt(8) == 0) {
            LivingEntity target = (LivingEntity) this.retaliationTarget;
            if (target != null && target.isRemoved()) target = null;
            if (target == null) target = findSomethingToAttack();

            if (target != null) {
                this.setAttacking(1);
                this.currentFlightTarget = target.blockPosition().above();
                double dist = this.distanceToSqr(target);
                double range = (5.0 + target.getBbWidth() / 2.0) * (5.0 + target.getBbWidth() / 2.0);
                if (dist < range) {
                    this.doHurtTarget(target);
                }
            } else {
                this.setAttacking(0);
            }
        }

        double dx = this.currentFlightTarget.getX() + 0.5 - this.getX();
        double dy = this.currentFlightTarget.getY() + 0.1 - this.getY();
        double dz = this.currentFlightTarget.getZ() + 0.5 - this.getZ();

        Vec3 motion = this.getDeltaMovement();
        double newMx = motion.x + (Math.signum(dx) * 0.5 - motion.x) * 0.3;
        double newMy = motion.y + (Math.signum(dy) * 0.7 - motion.y) * 0.2;
        double newMz = motion.z + (Math.signum(dz) * 0.5 - motion.z) * 0.3;
        this.setDeltaMovement(newMx, newMy, newMz);

        float targetYaw = (float) (Math.atan2(newMz, newMx) * 180.0 / Math.PI) - 90.0f;
        float yawDiff = Mth.wrapDegrees(targetYaw - this.getYRot());
        this.setYRot(this.getYRot() + yawDiff / 4.0f);

        if (this.random.nextInt(100) == 1) {
            this.heal(1.0f);
        }
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
        boolean ret = super.hurt(source, amount);
        Entity attacker = source.getEntity();
        if (attacker instanceof LivingEntity && this.currentFlightTarget != null) {
            this.retaliationTarget = attacker;
            this.currentFlightTarget = attacker.blockPosition();
        }
        return ret;
    }

    // Death drops are fully data-driven via loot_table/entities/mantis.json
    // (orig Mantis.java:129-150: 2 mantis claws, painting, 2-11 gold nugget,
    // 1-3 uranium nugget, 1-3 titanium nugget, 2-4 diamond).

    @Nullable
    private LivingEntity findSomethingToAttack() {
        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(16.0, 8.0, 16.0));
        entities.sort(Comparator.comparingDouble(this::distanceToSqr));
        for (LivingEntity candidate : entities) {
            if (isSuitableTarget(candidate)) return candidate;
        }
        return null;
    }

    private boolean isSuitableTarget(LivingEntity target) {
        if (target == null || target == this || !target.isAlive()) return false;
        if (!this.getSensing().hasLineOfSight(target)) return false;
        if (target.isInWater()) return false;
        if (target instanceof EntityMantis) return false;
        if (target instanceof EntityBee) return false;
        if (target instanceof Player p) return !p.getAbilities().invulnerable;
        if (target instanceof Monster) return true;
        return false;
    }

    /** orig Mantis.java:263-302 — spawner bypass; clear-air volume; extra 1-in-6 dice in Chaos; y>=50; daytime; no other Mantis within 32/16/32. */
    @Override
    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level,
                                   net.minecraft.world.entity.MobSpawnType spawnType) {
        if (OriginalSpawnGates.nearOwnSpawner(this, level, -2, 2, 1, 3)) return true;
        if (!OriginalSpawnGates.airBox(this, level, -2, 1, 1, 5, -2, 1)) return false;
        if (danger.orespawn.ModDimensionKeys.isIn(level, danger.orespawn.ModDimensionKeys.CHAOS)
                && this.getRandom().nextInt(6) != 0) return false;
        if (this.getY() < 50.0) return false;
        if (!OriginalSpawnGates.isDaytime(level)) return false;
        return !OriginalSpawnGates.anyOtherNearby(this, level, EntityMantis.class, 32.0, 16.0, 32.0);
    }
}
