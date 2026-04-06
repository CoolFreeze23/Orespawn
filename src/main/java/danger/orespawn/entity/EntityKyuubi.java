package danger.orespawn.entity;

import java.util.Comparator;
import java.util.List;
import javax.annotation.Nullable;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
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
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import danger.orespawn.OreSpawnMod;

public class EntityKyuubi extends Monster {
    private final float moveSpeed = 0.25f;

    public EntityKyuubi(EntityType<? extends EntityKyuubi> type, Level level) {
        super(type, level);
        this.xpReward = 30;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.35));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 10.0f));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 30.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.ATTACK_DAMAGE, 3.0);
    }

    @Override
    public boolean removeWhenFarAway(double dist) {
        return !this.isPersistenceRequired();
    }

    @Override
    public void tick() {
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(this.moveSpeed);
        super.tick();
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.random.nextInt(10) == 1) {
            if (this.level().isClientSide) {
                this.level().addParticle(ParticleTypes.DUST_PLUME,
                        this.getX(), this.getY() + 2.0, this.getZ(), 0.0, 0.0, 0.0);
                this.level().addParticle(ParticleTypes.LAVA,
                        this.getX(), this.getY() + 2.0, this.getZ(), 0.0, 0.0, 0.0);
            }
            this.setRemainingFireTicks(5 * 20);
            if (this.isInWater()) {
                this.doHurtTarget(this);
                if (this.level().isClientSide) {
                    this.level().addParticle(ParticleTypes.SMOKE,
                            this.getX(), this.getY() + 1.75, this.getZ(), 0.0, 0.0, 0.0);
                    this.level().addParticle(ParticleTypes.LARGE_SMOKE,
                            this.getX(), this.getY() + 2.0, this.getZ(), 0.0, 0.0, 0.0);
                }
            }
        }
    }

    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;
        super.customServerAiStep();

        if (this.random.nextInt(200) == 1) {
            this.setTarget(null);
        }

        if (this.random.nextInt(10) == 1) {
            LivingEntity target = findSomethingToAttack();
            if (target != null) {
                this.lookAt(target, 10.0f, 10.0f);
                this.getNavigation().moveTo(target, 1.25);
                if (this.distanceToSqr(target) < 64.0
                        && (this.random.nextInt(6) == 0 || this.random.nextInt(8) == 1)) {
                    double dirX = target.getX() - this.getX();
                    double dirY = target.getY() + 0.75 - (this.getY() + 1.25);
                    double dirZ = target.getZ() - this.getZ();
                    SmallFireball fireball = new SmallFireball(this.level(), this, new Vec3(dirX, dirY, dirZ));
                    fireball.setPos(this.getX(), this.getY() + 1.25, this.getZ());
                    this.level().addFreshEntity(fireball);
                    this.playSound(SoundEvents.ARROW_SHOOT, 0.75f,
                            1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));
                }
            }
        }
    }

    @Nullable
    private LivingEntity findSomethingToAttack() {
        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(12.0, 4.0, 12.0));
        entities.sort(Comparator.comparingDouble(this::distanceToSqr));
        for (LivingEntity candidate : entities) {
            if (isSuitableTarget(candidate)) return candidate;
        }
        return null;
    }

    private boolean isSuitableTarget(LivingEntity target) {
        if (target == null || target == this || !target.isAlive()) return false;
        if (!this.getSensing().hasLineOfSight(target)) return false;
        if (target instanceof Monster) return false;
        if (target instanceof Player player) {
            return !player.getAbilities().invulnerable;
        }
        return true;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        for (int i = 0; i < 10; i++) {
            this.spawnAtLocation(Items.GOLD_INGOT);
        }
        for (int i = 0; i < 3; i++) {
            this.spawnAtLocation(Items.TNT);
        }
        for (int i = 0; i < 4; i++) {
            this.spawnAtLocation(Items.REDSTONE_BLOCK);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "kyuubi_living"));
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "alo_hurt"));
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "alo_death"));
    }

    @Override
    protected float getSoundVolume() {
        return 0.75f;
    }
}
