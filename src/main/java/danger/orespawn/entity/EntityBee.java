package danger.orespawn.entity;

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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.Mth;

public class EntityBee extends Monster {
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(EntityBee.class, EntityDataSerializers.INT);

    private BlockPos currentFlightTarget = null;
    private int stuckCount = 0;
    private int lastX = 0;
    private int lastZ = 0;
    @Nullable
    private LivingEntity retaliationTarget = null;

    public EntityBee(EntityType<? extends EntityBee> type, Level level) {
        super(type, level);
        this.xpReward = 25;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 30.0)
                .add(Attributes.MOVEMENT_SPEED, 0.32)
                .add(Attributes.ATTACK_DAMAGE, 6.0);
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
        return 0.25f;
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
    public boolean doHurtTarget(Entity target) {
        boolean result = target.hurt(this.damageSources().mobAttack(this), (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE));
        if (this.random.nextInt(3) == 1 && target instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.POISON, 50, 0));
        }
        return result;
    }

    @Override
    public void tick() {
        super.tick();
        Vec3 motion = this.getDeltaMovement();
        this.setDeltaMovement(motion.x, motion.y * 0.6, motion.z);
        if (this.isInWater() && this.random.nextInt(4) == 1) {
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
        } else if (this.random.nextInt(15) == 0) {
            LivingEntity target = this.retaliationTarget;
            if (target != null && target.isRemoved()) target = null;
            if (target == null) target = findSomethingToAttack();

            if (target != null) {
                this.setAttacking(1);
                this.currentFlightTarget = target.blockPosition().above();
                if (this.distanceToSqr(target) < 16.0) {
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
        if (attacker instanceof LivingEntity livingAttacker && this.currentFlightTarget != null) {
            this.retaliationTarget = livingAttacker;
            this.currentFlightTarget = attacker.blockPosition();
        }
        return ret;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        int count = 2 + this.random.nextInt(10);
        for (int i = 0; i < count; i++) {
            this.spawnAtLocation(Items.SPIDER_EYE);
        }
        count = 2 + this.random.nextInt(10);
        for (int i = 0; i < count; i++) {
            this.spawnAtLocation(new ItemStack(Blocks.RED_MUSHROOM));
        }
    }

    @Nullable
    private LivingEntity findSomethingToAttack() {
        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(10.0, 6.0, 10.0));
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
        if (target instanceof Player p) return !p.getAbilities().invulnerable;
        if (target instanceof Villager) return true;
        return false;
    }
}
