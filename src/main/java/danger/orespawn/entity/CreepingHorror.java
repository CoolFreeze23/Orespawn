package danger.orespawn.entity;

import java.util.Comparator;
import java.util.List;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import danger.orespawn.OreSpawnMod;

public class CreepingHorror extends Monster {
    private static final long DAYTIME_TICKS = 24000L;
    /** Past this tick-of-day, the random daytime despawn logic does not run. */
    private static final long DAYTIME_DESPAWN_CUTOFF = 11000L;

    private final Comparator<Entity> targetSorter;
    private static final float MOVE_SPEED = 0.25f;
    private static final int MAX_HEALTH = 20;
    private static final int ATTACK_DAMAGE = 6;

    public CreepingHorror(EntityType<? extends CreepingHorror> type, Level level) {
        super(type, level);
        this.xpReward = 5;
        this.targetSorter = Comparator.comparingDouble(this::distanceToSqr);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.35));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.MOVEMENT_SPEED, MOVE_SPEED)
                .add(Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE)
                .add(Attributes.FOLLOW_RANGE, 16.0);
    }

    @Override
    public boolean removeWhenFarAway(double dist) {
        return !this.isPersistenceRequired();
    }

    @Override
    public void tick() {
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(MOVE_SPEED);
        super.tick();
        if (this.isPersistenceRequired()) return;
        long timeOfDay = this.level().getDayTime() % DAYTIME_TICKS;
        if (timeOfDay > DAYTIME_DESPAWN_CUTOFF) return;
        if (this.getRandom().nextInt(500) == 1) {
            this.discard();
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "creepinghorror_living"));
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "creepinghorror_hit"));
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "creepinghorror_dead"));
    }

    @Override
    protected float getSoundVolume() {
        return 0.65f;
    }

    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;
        super.customServerAiStep();

        if (this.getRandom().nextInt(200) == 1) {
            this.setTarget(null);
        }
        if (this.getRandom().nextInt(5) == 1) {
            LivingEntity currentTarget = findSomethingToAttack();
            if (currentTarget != null) {
                this.getNavigation().moveTo(currentTarget, 1.25);
                if (this.distanceToSqr(currentTarget) < 5.0) {
                    if (this.random.nextInt(12) == 0 || this.random.nextInt(14) == 1) {
                        this.doHurtTarget(currentTarget);
                    }
                }
            }
        }
    }

    private LivingEntity findSomethingToAttack() {
        List<LivingEntity> list = this.level().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(16.0, 4.0, 16.0));
        list.sort(this.targetSorter);
        for (LivingEntity candidate : list) {
            if (isSuitableTarget(candidate)) return candidate;
        }
        return null;
    }

    private boolean isSuitableTarget(LivingEntity target) {
        if (target == null || target == this || !target.isAlive()) return false;
        if (target instanceof CreepingHorror) return false;
        if (target instanceof Player player) return !player.getAbilities().instabuild;
        return true;
    }
}
