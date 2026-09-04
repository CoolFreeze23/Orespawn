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
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.entity.ai.TargetSelection;
import danger.orespawn.util.MyUtils;

/**
 * Robot5 — RoboSniper role.
 *
 * Long-range marksman (150 HP, 15 ATK, 4 ARM). Detection radius is a
 * 30×6×30 AABB — far larger than the other robots — and the fire
 * solution holds out to ~30 blocks (distSq < 900). On a successful
 * lock the sniper plants and fires a {@link LaserBall} every 20-tick
 * reload cycle; movement is suppressed inside 6 blocks so it doesn't
 * shuffle out of its firing line. Light armour (4) makes it a glass
 * cannon — the role pairs naturally with a RoboWarrior tank screen.
 * Registry ID kept as "robot_5" for save compat.
 */
public class Robot5 extends Monster {
    // OPT-011: cached SoundEvents — identical createVariableRangeEvent ids,
    // allocated once per class instead of on every sound query.
    private static final SoundEvent SND_ROBOT_LIVING = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "robot_living"));
    private static final SoundEvent SND_ROBOT_HURT = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "robot_hurt"));
    private static final SoundEvent SND_ROBOT_DEATH = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "robot_death"));
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(Robot5.class, EntityDataSerializers.INT);

    // TF-035: orig Robot5.java:39,50 — targets sort with GenericTargetSorter
    // (creeper-halved / big-silhouette-first), not plain distance.
    private final GenericTargetSorter targetSorter;
    private int reloadTicker = 0;
    private final float moveSpeed = 0.3f;

    public Robot5(EntityType<? extends Robot5> type, Level level) {
        super(type, level);
        this.xpReward = 20;
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
        // orig OreSpawnMain.java:6498 — Robot5 20 HP / 5 ATK / 6 armor
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MobStats.ROBOT5.maxHealth())
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ATTACK_DAMAGE, MobStats.ROBOT5.attackDamage())
                .add(Attributes.ARMOR, MobStats.ROBOT5.armor());
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
            if (this.reloadTicker < 15) this.setAttacking(0);
        }
        if (this.reloadTicker == 0) {
            LivingEntity target = this.getTarget();
            if (this.getRandom().nextInt(50) == 1) this.setTarget(null);
            if (target != null && !target.isAlive()) { this.setTarget(null); target = null; }
            if (target == null) target = findSomethingToAttack();
            this.reloadTicker = 20;
            if (target != null) {
                this.lookAt(target, 10.0f, 10.0f);
                double dsq = this.distanceToSqr(target);
                if (dsq < 900.0) {
                    this.setAttacking(1);
                    if (dsq > 36.0) {
                        this.getNavigation().moveTo(target, 0.5);
                    }
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
     * Long-range LaserBall shot. Speed 1.8 (faster than the gunner) and
     * spawned slightly higher (+1.5y) so the sniper "aims down range".
     * The shooter is recorded for kill-credit and friendly-fire checks.
     */
    private void fireLaserAt(LivingEntity target) {
        if (this.level().isClientSide) return;
        LaserBall projectile = new LaserBall(this.level(), this);
        projectile.setPos(this.getX(), this.getY() + 1.5, this.getZ());
        double dx = target.getX() - this.getX();
        double dy = target.getY() + target.getBbHeight() * 0.5 - (this.getY() + 1.5);
        double dz = target.getZ() - this.getZ();
        projectile.shoot(dx, dy, dz, 1.8f, 0.5f);
        // ENT-K-074: orig Robot5.java:245 — every shot plays "fireworks.launch"
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
        if (OreSpawnConfig.PLAY_NICELY.get()) return null; // orig Robot5.java:296-298 — PlayNicely != 0 returns null ahead of the scan (ENT-S-115)
        AABB searchBox = this.getBoundingBox().inflate(30.0, 6.0, 30.0);
        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class, searchBox);
        // OPT-021: nearest-first pick without the full list sort; TargetSelection
        // preserves the removed sort's order and stable-tie semantics exactly.
        return TargetSelection.firstMatch(entities, this.targetSorter, this::isSuitableTarget);
    }

    private boolean isSuitableTarget(LivingEntity target) {
        if (target == null || target == this || !target.isAlive()) return false;
        if (MyUtils.isIgnoreable(target)) return false; // orig Robot5.java:277-279 — the shared ignore screen (ENT-S-106)
        if (target instanceof Monster) return false;
        if (target instanceof Player p && p.getAbilities().instabuild) return false;
        return true;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        if (this.getRandom().nextInt(4) == 0)
            return SND_ROBOT_LIVING;
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource ds) {
        return SND_ROBOT_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SND_ROBOT_DEATH;
    }

    /** orig Robot5.java:317-351 — "Robo-Sniper" spawner bypass; y>=50; night; shorter (y+1..+2) air/short-grass clearance; darkness. */
    @Override
    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level,
                                   net.minecraft.world.entity.MobSpawnType spawnType) {
        if (OriginalSpawnGates.nearOwnSpawner(this, level)) return true;
        if (this.getY() < 50.0) return false;
        if (OriginalSpawnGates.isDaytime(level)) return false;
        if (!OriginalSpawnGates.boxMatches(this, level, -1, 1, 1, 2, -1, 0,
                s -> s.isAir() || s.is(net.minecraft.world.level.block.Blocks.SHORT_GRASS))) return false;
        return OriginalSpawnGates.isDarkEnough(this, level);
    }
}
