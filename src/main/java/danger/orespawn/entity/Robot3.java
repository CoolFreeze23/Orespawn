package danger.orespawn.entity;

import danger.orespawn.MobStats;
import danger.orespawn.entity.ai.GenericTargetSorter;

import java.util.List;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.resources.ResourceLocation;
import danger.orespawn.OreSpawnMod;

/**
 * Robot3 — RoboGunner role.
 *
 * Mid-range projectile platform (300 HP, 20 ATK, 6 ARM). Tracks targets
 * out to ~16 blocks and fires {@link LaserBall} projectiles on a 35-tick
 * reload cycle whenever line-of-sight resolves. The shot inherits the
 * gunner as its owner so player-friendly fire / kill credit work
 * correctly. Movement closes the gap to maintain firing solution but
 * never goes below ~0.5x speed so the gunner doesn't crowd melee
 * RoboWarriors. Registry ID kept as "robot_3" for save compat.
 */
public class Robot3 extends Monster {
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(Robot3.class, EntityDataSerializers.INT);

    // TF-035: orig Robot3.java:39,51 — targets sort with GenericTargetSorter
    // (creeper-halved / big-silhouette-first), not plain distance.
    private final GenericTargetSorter targetSorter;
    private int reloadTicker = 0;
    private final float moveSpeed = 0.35f;

    /**
     * Per-entity render scratch (orig Robot3.java {@code renderdata = new RenderInfo()}).
     * {@code ri1} latches the attack arm-swing state at animation zero crossings
     * (orig ModelRobot3.java:169-180); mutated client-side by the model.
     */
    private final danger.orespawn.entity.client.RenderInfo renderInfo =
            new danger.orespawn.entity.client.RenderInfo();

    /** Mirrors orig Robot3.java {@code getRenderInfo()}. */
    public danger.orespawn.entity.client.RenderInfo getRenderInfo() {
        return this.renderInfo;
    }

    public Robot3(EntityType<? extends Robot3> type, Level level) {
        super(type, level);
        this.xpReward = 60;
        this.targetSorter = new GenericTargetSorter(this);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        // orig OreSpawnMain.java:6496 — Robot3 80 HP / 16 ATK / 14 armor
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MobStats.ROBOT3.maxHealth())
                .add(Attributes.MOVEMENT_SPEED, 0.35)
                .add(Attributes.ATTACK_DAMAGE, MobStats.ROBOT3.attackDamage())
                .add(Attributes.ARMOR, MobStats.ROBOT3.armor());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, 0);
    }

    public int getAttacking() { return this.entityData.get(DATA_ATTACKING); }
    public void setAttacking(int value) { this.entityData.set(DATA_ATTACKING, value); }

    @Override
    public void jumpFromGround() {
        Vec3 velocity = this.getDeltaMovement();
        this.setDeltaMovement(velocity.x, velocity.y + 0.25, velocity.z);
        super.jumpFromGround();
    }

    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;
        super.customServerAiStep();
        if (this.reloadTicker > 0) {
            --this.reloadTicker;
            if (this.reloadTicker < 25) this.setAttacking(0);
        }
        if (this.reloadTicker == 0) {
            LivingEntity target = this.getTarget();
            if (this.getRandom().nextInt(50) == 1) this.setTarget(null);
            if (target != null && !target.isAlive()) { this.setTarget(null); target = null; }
            if (target == null) target = findSomethingToAttack();
            this.reloadTicker = 35;
            if (target != null) {
                this.lookAt(target, 10.0f, 10.0f);
                if (this.distanceToSqr(target) < 256.0) {
                    this.setAttacking(1);
                    this.getNavigation().moveTo(target, 0.5);
                    if (this.getSensing().hasLineOfSight(target)) {
                        fireLaserAt(target);
                    }
                }
            } else {
                this.setAttacking(0);
            }
        }
    }

    /**
     * Spawn a server-side LaserBall aimed at the target. Bullet speed 1.6,
     * launched from +1.4 above the gunner's feet so it doesn't intersect
     * its own bounding box. Throwable physics handles arc and impact
     * damage; we just provide direction and momentum.
     */
    private void fireLaserAt(LivingEntity target) {
        if (this.level().isClientSide) return;
        LaserBall projectile = new LaserBall(this.level(), this);
        projectile.setPos(this.getX(), this.getY() + 1.4, this.getZ());
        double dx = target.getX() - this.getX();
        double dy = target.getY() + target.getBbHeight() * 0.5 - (this.getY() + 1.4);
        double dz = target.getZ() - this.getZ();
        projectile.shoot(dx, dy, dz, 1.6f, 1.0f);
        // ENT-K-067: orig Robot3.java:273 — every shot plays "fireworks.launch"
        // at 3.0 volume / 1.0 pitch (vanilla firework-rocket launch report).
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.HOSTILE, 3.0f, 1.0f);
        this.level().addFreshEntity(projectile);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getMsgId().equals("cactus")) return false;
        return super.hurt(source, amount);
    }

    private LivingEntity findSomethingToAttack() {
        AABB searchBox = this.getBoundingBox().inflate(16.0, 3.0, 16.0);
        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class, searchBox);
        entities.sort(this.targetSorter);
        for (LivingEntity e : entities) {
            if (isSuitableTarget(e)) return e;
        }
        return null;
    }

    private boolean isSuitableTarget(LivingEntity target) {
        if (target == null || target == this || !target.isAlive()) return false;
        if (target instanceof Monster) return false;
        if (target instanceof Player p && p.getAbilities().instabuild) return false;
        return true;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        if (this.getRandom().nextInt(4) == 0)
            return SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "robot_living"));
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource ds) {
        return SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "robot_hurt"));
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "robot_death"));
    }

    /** orig Robot3.java:343-360 — y>=50; night; air/short-grass clearance above; darkness. */
    @Override
    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level,
                                   net.minecraft.world.entity.MobSpawnType spawnType) {
        if (this.getY() < 50.0) return false;
        if (OriginalSpawnGates.isDaytime(level)) return false;
        if (!OriginalSpawnGates.boxMatches(this, level, -1, 1, 1, 5, -1, 0,
                s -> s.isAir() || s.is(net.minecraft.world.level.block.Blocks.SHORT_GRASS))) return false;
        return OriginalSpawnGates.isDarkEnough(this, level);
    }
}
