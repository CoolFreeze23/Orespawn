package danger.orespawn.entity;

import java.util.Comparator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import danger.orespawn.OreSpawnMod;

public class Mothra extends EntityButterfly {
    private BlockPos currentFlightTarget = null;
    private int lastX = 0, lastZ = 0, lastY = 0;
    private int stuckCount = 0;
    private int wingSound = 0;
    private int healthTicker = 100;
    private final Comparator<Entity> targetSorter;

    public Mothra(EntityType<? extends Mothra> type, Level level) {
        super(type, level);
        this.xpReward = 100;
        this.targetSorter = Comparator.comparingDouble(this::distanceToSqr);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 500.0)
                .add(Attributes.MOVEMENT_SPEED, 0.35)
                .add(Attributes.ATTACK_DAMAGE, 30.0)
                .add(Attributes.FOLLOW_RANGE, 48.0);
    }

    @Override
    public boolean removeWhenFarAway(double dist) {
        return !this.isPersistenceRequired();
    }

    @Override
    protected float getSoundVolume() { return 1.5f; }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() { return null; }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) { return null; }

    @Override
    protected SoundEvent getDeathSound() { return SoundEvents.GENERIC_EXPLODE.value(); }

    @Override
    public boolean isPushable() { return true; }

    @Override
    public void tick() {
        super.tick();
        Vec3 motion = this.getDeltaMovement();
        this.setDeltaMovement(motion.x, motion.y * 0.6, motion.z);

        this.wingSound++;
        if (this.wingSound > 30) {
            if (!this.level().isClientSide) {
                this.playSound(SoundEvent.createVariableRangeEvent(
                        ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "MothraWings")), 1.0f, 1.0f);
            }
            this.wingSound = 0;
        }
        this.healthTicker--;
        if (this.healthTicker <= 0) {
            if (this.getHealth() < this.getMaxHealth()) this.heal(1.0f);
            this.healthTicker = 200;
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        Entity attacker = source.getEntity();
        if (attacker instanceof Mothra) return false;
        boolean ret = super.hurt(source, amount);
        if (attacker != null && this.currentFlightTarget != null) {
            this.currentFlightTarget = new BlockPos((int) attacker.getX(), (int) attacker.getY() + 2, (int) attacker.getZ());
        }
        return ret;
    }

    private boolean isSuitableTarget(LivingEntity target) {
        if (this.level().getDifficulty() == Difficulty.PEACEFUL) return false;
        if (target == null || target == this || !target.isAlive()) return false;
        if (target instanceof Mothra) return false;
        if (target instanceof Player p && p.getAbilities().instabuild) return false;
        return true;
    }

    private LivingEntity findSomethingToAttack() {
        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(15.0, 20.0, 15.0));
        entities.sort(this.targetSorter);
        for (LivingEntity targetEntity : entities) {
            if (this.isSuitableTarget(targetEntity)) return targetEntity;
        }
        return null;
    }

    private void attackWithFireball(LivingEntity target) {
        if (this.level().getDifficulty() == Difficulty.PEACEFUL) return;
        double xzoff = 2.25;
        double cx = this.getX() - xzoff * Math.sin(Math.toRadians(this.getYRot()));
        double cz = this.getZ() + xzoff * Math.cos(Math.toRadians(this.getYRot()));
        SmallFireball fireball = new SmallFireball(this.level(), this,
                new Vec3(target.getX() - cx, target.getY() + 0.55 - this.getY(), target.getZ() - cz));
        fireball.setPos(cx, this.getY(), cz);
        this.playSound(SoundEvents.BLAZE_SHOOT, 0.75f, 1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));
        this.level().addFreshEntity(fireball);
    }

    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;

        if ((int) this.getX() == this.lastX && (int) this.getY() == this.lastY && (int) this.getZ() == this.lastZ) {
            this.stuckCount++;
        } else {
            this.stuckCount = 0;
            this.lastX = (int) this.getX(); this.lastY = (int) this.getY(); this.lastZ = (int) this.getZ();
        }

        if (this.currentFlightTarget == null) {
            this.currentFlightTarget = this.blockPosition();
        }

        double distSq = this.currentFlightTarget.distSqr(this.blockPosition());
        if (this.stuckCount > 50 || this.random.nextInt(300) == 0 || distSq < 9.0) {
            for (int tries = 50; tries > 0; tries--) {
                int xdir = this.random.nextInt(2) == 0 ? 1 : -1;
                int zdir = this.random.nextInt(2) == 0 ? 1 : -1;
                int newx = (this.random.nextInt(20) + 8) * xdir;
                int newz = (this.random.nextInt(20) + 8) * zdir;
                BlockPos target = new BlockPos(
                        (int) this.getX() + newx,
                        (int) this.getY() + this.random.nextInt(7) - 1,
                        (int) this.getZ() + newz);
                if (this.level().getBlockState(target).isAir()) {
                    this.currentFlightTarget = target;
                    break;
                }
            }
            this.stuckCount = 0;
        } else if (this.random.nextInt(10) == 0 && this.level().getDifficulty() != Difficulty.PEACEFUL) {
            Player target = this.level().getNearestPlayer(this, 25.0);
            if (target != null && !target.getAbilities().instabuild) {
                this.currentFlightTarget = new BlockPos((int) target.getX(), (int) target.getY() + 4, (int) target.getZ());
                if (this.random.nextInt(3) == 0) this.attackWithFireball(target);
            }
            if (target == null && this.random.nextInt(3) == 0) {
                LivingEntity hostile = this.findSomethingToAttack();
                if (hostile != null) {
                    this.currentFlightTarget = new BlockPos((int) hostile.getX(), (int) hostile.getY() + 5, (int) hostile.getZ());
                    if (this.random.nextInt(3) == 0) this.attackWithFireball(hostile);
                }
            }
        }

        double dx = this.currentFlightTarget.getX() + 0.5 - this.getX();
        double dy = this.currentFlightTarget.getY() + 0.1 - this.getY();
        double dz = this.currentFlightTarget.getZ() + 0.5 - this.getZ();
        Vec3 motion = this.getDeltaMovement();
        double mx = motion.x + (Math.signum(dx) * 0.5 - motion.x) * 0.30001;
        double my = motion.y + (Math.signum(dy) * 0.7 - motion.y) * 0.20001;
        double mz = motion.z + (Math.signum(dz) * 0.5 - motion.z) * 0.30001;
        this.setDeltaMovement(mx, my, mz);

        float targetYaw = (float) (Math.atan2(mz, mx) * 180.0 / Math.PI) - 90.0f;
        float yawDiff = Mth.wrapDegrees(targetYaw - this.getYRot());
        this.zza = 1.0f;
        this.setYRot(this.getYRot() + yawDiff / 4.0f);
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        this.spawnAtLocation(Items.NETHER_STAR);
        for (int i = 0; i < 53; i++) this.spawnAtLocation(Items.EXPERIENCE_BOTTLE);
        for (int i = 0; i < 3; i++) this.spawnAtLocation(Items.EMERALD);
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnType) {
        if (this.getY() < 70.0) return false;
        if (!this.level().canSeeSky(this.blockPosition())) return false;
        List<Mothra> nearby = this.level().getEntitiesOfClass(Mothra.class,
                this.getBoundingBox().inflate(64.0, 32.0, 64.0));
        return nearby.isEmpty();
    }
}
