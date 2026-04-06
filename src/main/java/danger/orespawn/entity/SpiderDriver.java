package danger.orespawn.entity;

import java.util.Comparator;
import java.util.List;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
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

public class SpiderDriver extends Spider {
    private final Comparator<Entity> targetSorter;

    public SpiderDriver(EntityType<? extends SpiderDriver> type, Level level) {
        super(type, level);
        this.targetSorter = Comparator.comparingDouble(this::distanceToSqr);
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

        if (this.level().getDifficulty() != Difficulty.PEACEFUL && this.random.nextInt(4) == 0 && this.getVehicle() != null) {
            LivingEntity target = this.findSomethingToAttack();
            if (target != null) {
                this.getLookControl().setLookAt(target, 10.0f, 10.0f);
            }
        }
    }

    private LivingEntity findSpiderRobot() {
        List<SpiderRobot> robots = this.level().getEntitiesOfClass(SpiderRobot.class,
                this.getBoundingBox().inflate(25.0, 15.0, 25.0));
        robots.sort(this.targetSorter);
        for (SpiderRobot robot : robots) {
            if (!robot.isVehicle()) return robot;
        }
        return null;
    }

    private boolean isSuitableTarget(LivingEntity target) {
        if (this.level().getDifficulty() == Difficulty.PEACEFUL) return false;
        if (target == null || target == this || !target.isAlive()) return false;
        if (target instanceof SpiderRobot || target instanceof SpiderDriver) return false;
        if (target instanceof Spider || target instanceof CaveSpider) return false;
        if (target instanceof Player p) return !p.getAbilities().instabuild;
        return !(this.distanceToSqr(target) < 36.0);
    }

    private LivingEntity findSomethingToAttack() {
        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(35.0, 15.0, 35.0));
        entities.sort(this.targetSorter);
        for (LivingEntity e : entities) {
            if (this.isSuitableTarget(e)) return e;
        }
        return null;
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnType) {
        List<SpiderRobot> nearby = level.getEntitiesOfClass(SpiderRobot.class,
                this.getBoundingBox().inflate(24.0, 12.0, 24.0));
        if (!nearby.isEmpty()) return true;
        return super.checkSpawnRules(level, spawnType);
    }
}
