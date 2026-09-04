package danger.orespawn.entity;

import java.util.Comparator;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.CaveSpider;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.entity.ai.TargetSelection;
import danger.orespawn.util.MyUtils;

public class SpiderDriver extends Spider {

    /**
     * Stable id for the riding-state armor modifier (ENT-S-017/018). The
     * original hard-overrode getTotalArmorValue (orig SpiderDriver.java:96-101):
     * 8 armor while MOUNTED on its robot, 20 while on foot. Vanilla spiders
     * have 0 base armor, so the full value is carried by this modifier.
     */
    private static final ResourceLocation ARMOR_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "spider_driver_armor");
    /** orig SpiderDriver.java:97-99 — armor while riding the SpiderRobot. */
    private static final double MOUNTED_ARMOR = 8.0;
    /** orig SpiderDriver.java:100 — armor while on foot. */
    private static final double DISMOUNTED_ARMOR = 20.0;

    private final Comparator<Entity> targetSorter;
    /** Melee swing cooldown while mounted (orig attackTime = 16, orig SpiderDriver.java:88). */
    private int mountedAttackCooldown = 0;

    public SpiderDriver(EntityType<? extends SpiderDriver> type, Level level) {
        super(type, level);
        this.targetSorter = Comparator.comparingDouble(this::distanceToSqr);
    }

    /**
     * Keeps the riding-state armor modifier in sync (orig
     * SpiderDriver.java:96-101 returned 8 mounted / 20 on foot). Re-checked
     * every server tick so it also covers spawn, world-load, and forced
     * dismounts without needing every ride-state entry point overridden.
     */
    private void updateArmorModifier() {
        AttributeInstance armor = this.getAttribute(Attributes.ARMOR);
        if (armor == null) return;
        double expected = this.getVehicle() != null ? MOUNTED_ARMOR : DISMOUNTED_ARMOR;
        AttributeModifier existing = armor.getModifier(ARMOR_MODIFIER_ID);
        if (existing == null || existing.amount() != expected) {
            armor.removeModifier(ARMOR_MODIFIER_ID);
            armor.addPermanentModifier(new AttributeModifier(
                    ARMOR_MODIFIER_ID, expected, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new PanicGoal(this, 1.5));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.65));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Spider.createAttributes();
    }

    @Override
    public boolean removeWhenFarAway(double dist) {
        if (this.isPersistenceRequired()) return false;
        return this.getVehicle() == null;
    }

    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;
        super.customServerAiStep();

        updateArmorModifier();
        if (this.mountedAttackCooldown > 0) {
            --this.mountedAttackCooldown;
        }

        if (this.level().getDifficulty() != Difficulty.PEACEFUL && this.random.nextInt(5) == 0 && this.getVehicle() == null) {
            LivingEntity robot = this.findSpiderRobot();
            if (robot != null) {
                this.getLookControl().setLookAt(robot, 10.0f, 10.0f);
                double reachSq = (4.0 + robot.getBbWidth() / 2.0) * (4.0 + robot.getBbWidth() / 2.0);
                if (this.distanceToSqr(robot) < reachSq) {
                    this.startRiding(robot);
                } else {
                    this.getNavigation().moveTo(robot, 0.55);
                }
            }
        }

        // Mounted combat (ENT-S-018/019, orig SpiderDriver.java:74-83 + :86-94):
        // 1-in-4 chance per tick the driver picks a target and looks at it;
        // if the target is outside 11 + width/2 blocks it steers its robot
        // toward it (orig goThisWay 0.35*cos/0.35*sin), and any target that
        // comes within melee reach gets the spider bite plus a 1-in-2 Poison
        // (60 ticks) on a 16-tick swing timer (orig attackEntity :86-94).
        if (this.level().getDifficulty() != Difficulty.PEACEFUL && this.random.nextInt(4) == 0 && this.getVehicle() != null) {
            LivingEntity target = this.findSomethingToAttack();
            if (target != null) {
                this.getLookControl().setLookAt(target, 10.0f, 10.0f);

                double driveRange = 11.0 + target.getBbWidth() / 2.0;
                if (this.distanceToSqr(target) >= driveRange * driveRange
                        && this.getVehicle() instanceof SpiderRobot robot) {
                    // orig :76-82 — point the robot's motion straight at the prey.
                    double heading = Math.atan2(target.getZ() - this.getZ(), target.getX() - this.getX());
                    robot.setDeltaMovement(
                            0.35 * Math.cos(heading),
                            robot.getDeltaMovement().y,
                            0.35 * Math.sin(heading));
                }

                if (this.mountedAttackCooldown <= 0 && this.distanceTo(target) < 2.0f) {
                    this.mountedAttackCooldown = 16;
                    this.doHurtTarget(target);
                    if (this.random.nextInt(2) == 0) {
                        target.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 0), this);
                    }
                }
            }
        }
    }

    private LivingEntity findSpiderRobot() {
        if (OreSpawnConfig.PLAY_NICELY.get()) return null; // orig SpiderDriver.java:104-106 — PlayNicely != 0 returns null ahead of the mount scan (ENT-S-115)
        List<SpiderRobot> robots = this.level().getEntitiesOfClass(SpiderRobot.class,
                this.getBoundingBox().inflate(25.0, 15.0, 25.0));
        // OPT-021: nearest-first pick without the full list sort; TargetSelection
        // preserves the removed sort's order and stable-tie semantics exactly.
        return TargetSelection.firstMatch(robots, this.targetSorter, robot -> !robot.isVehicle());
    }

    private boolean isSuitableTarget(LivingEntity target) {
        if (this.level().getDifficulty() == Difficulty.PEACEFUL) return false;
        if (target == null || target == this || !target.isAlive()) return false;
        if (MyUtils.isIgnoreable(target)) return false; // orig SpiderDriver.java:134-136 — the shared ignore screen (ENT-S-106)
        if (target instanceof SpiderRobot || target instanceof SpiderDriver) return false;
        if (target instanceof Spider || target instanceof CaveSpider) return false;
        if (!this.getSensing().hasLineOfSight(target)) return false; // orig SpiderDriver.java:149-151 — canSee, after the four kind refusals (:137-148) and ahead of the player branch (:152-155) (ENT-S-118)
        if (target instanceof Player p) return !p.getAbilities().instabuild;
        return !(this.distanceToSqr(target) < 36.0);
    }

    private LivingEntity findSomethingToAttack() {
        if (OreSpawnConfig.PLAY_NICELY.get()) return null; // orig SpiderDriver.java:160-162 — PlayNicely != 0 returns null ahead of the combat scan (ENT-S-115)
        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(35.0, 15.0, 35.0));
        // OPT-021: nearest-first pick without the full list sort (see above).
        return TargetSelection.firstMatch(entities, this.targetSorter, this::isSuitableTarget);
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnType) {
        List<SpiderRobot> nearby = level.getEntitiesOfClass(SpiderRobot.class,
                this.getBoundingBox().inflate(24.0, 12.0, 24.0));
        if (!nearby.isEmpty()) return true;
        return super.checkSpawnRules(level, spawnType);
    }
}
