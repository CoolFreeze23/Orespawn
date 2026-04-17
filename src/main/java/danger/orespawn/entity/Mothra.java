package danger.orespawn.entity;

import java.util.Comparator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.BossEvent;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.OreSpawnMod;
import net.neoforged.neoforge.entity.PartEntity;

public class Mothra extends EntityButterfly implements OreSpawnPartEntity.MultipartBoss {
    private BlockPos currentFlightTarget = null;
    private int lastX = 0, lastZ = 0, lastY = 0;
    private int stuckCount = 0;
    private int wingSound = 0;
    private int healthTicker = 100;
    private final Comparator<Entity> targetSorter;

    private final OreSpawnPartEntity<Mothra> bodyPart;
    private final OreSpawnPartEntity<Mothra> wingLeft;
    private final OreSpawnPartEntity<Mothra> wingRight;
    private final OreSpawnPartEntity<Mothra> headPart;
    private final PartEntity<?>[] allParts;

    private final ServerBossEvent bossEvent = new ServerBossEvent(
            Component.literal("Mothra"), BossEvent.BossBarColor.YELLOW, BossEvent.BossBarOverlay.PROGRESS);

    public Mothra(EntityType<? extends Mothra> type, Level level) {
        super(type, level);
        this.xpReward = 100;
        this.targetSorter = Comparator.comparingDouble(this::distanceToSqr);

        this.bodyPart  = new OreSpawnPartEntity<>(this, "body",  4.0f, 3.0f);
        this.wingLeft  = new OreSpawnPartEntity<>(this, "wingL", 5.0f, 1.5f);
        this.wingRight = new OreSpawnPartEntity<>(this, "wingR", 5.0f, 1.5f);
        this.headPart  = new OreSpawnPartEntity<>(this, "head",  2.0f, 2.0f);
        this.allParts = new PartEntity<?>[]{ bodyPart, wingLeft, wingRight, headPart };
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
    public boolean isMultipartEntity() {
        return true;
    }

    @Override
    public PartEntity<?>[] getParts() {
        return this.allParts;
    }

    @Override
    public void setId(int id) {
        super.setId(id);
        for (int i = 0; i < allParts.length; i++) {
            allParts[i].setId(id + i + 1);
        }
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean hurtFromPart(OreSpawnPartEntity<?> part, DamageSource source, float amount) {
        String partName = part.getPartName();
        float multiplied = switch (partName) {
            case "head" -> amount;
            case "body" -> amount * 0.5f;
            default -> amount * 0.25f + 1.0f;
        };
        return this.hurt(source, multiplied);
    }

    private void positionPart(OreSpawnPartEntity<Mothra> part, double offsetX, double offsetY, double offsetZ) {
        float yawRad = this.yBodyRot * Mth.DEG_TO_RAD;
        double sin = Mth.sin(yawRad);
        double cos = Mth.cos(yawRad);
        double rx = offsetX * cos - offsetZ * sin;
        double rz = offsetX * sin + offsetZ * cos;
        part.setPos(this.getX() + rx, this.getY() + offsetY, this.getZ() + rz);
    }

    @Override
    public void tick() {
        Vec3[] oldPos = new Vec3[allParts.length];
        for (int i = 0; i < allParts.length; i++) {
            oldPos[i] = new Vec3(allParts[i].getX(), allParts[i].getY(), allParts[i].getZ());
        }

        super.tick();
        positionPart(bodyPart,    0.0,  1.0,  0.0);
        positionPart(headPart,    0.0,  2.0, -3.0);
        positionPart(wingLeft,   -6.0,  1.5,  0.0);
        positionPart(wingRight,   6.0,  1.5,  0.0);

        for (int i = 0; i < allParts.length; i++) {
            allParts[i].xo = oldPos[i].x;
            allParts[i].yo = oldPos[i].y;
            allParts[i].zo = oldPos[i].z;
            allParts[i].xOld = oldPos[i].x;
            allParts[i].yOld = oldPos[i].y;
            allParts[i].zOld = oldPos[i].z;
        }

        Vec3 motion = this.getDeltaMovement();
        this.setDeltaMovement(motion.x, motion.y * 0.6, motion.z);

        this.wingSound++;
        if (this.wingSound > 30) {
            if (!this.level().isClientSide) {
                this.playSound(SoundEvent.createVariableRangeEvent(
                        ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "mothrawings")), 1.0f, 1.0f);
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
        // Config-driven peaceful mode: Mothra won't attack anything
        if (OreSpawnConfig.MOTHRA_PEACEFUL.get()) return false;
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
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    @Override
    protected void customServerAiStep() {
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
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
        } else if (this.random.nextInt(10) == 0 && this.level().getDifficulty() != Difficulty.PEACEFUL
                && !OreSpawnConfig.MOTHRA_PEACEFUL.get()) {
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

    /**
     * 1.7.10 fidelity: Mothra spawned only when a vanilla mob spawner block
     * tagged {@code EntityId="Mothra"} sat within ±2 X/Z and +1..+3 Y of the
     * spawn point — even though her {@code addSpawn} entries listed Nether and
     * Mesa biomes. We mirror that gating here, but relax the NBT requirement
     * to "any spawner block" because in 1.21.1 spawner contents are stored as
     * weighted spawn potentials, and we don't ship a Mothra-specific spawner
     * block. The {@code MOTHRA_REQUIRES_SPAWNER} config lets servers disable
     * the gate if they want unconditional biome spawning.
     */
    @Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnType) {
        if (spawnType == MobSpawnType.SPAWN_EGG || spawnType == MobSpawnType.MOB_SUMMONED
                || spawnType == MobSpawnType.COMMAND || spawnType == MobSpawnType.EVENT) {
            return super.checkSpawnRules(level, spawnType);
        }
        List<Mothra> nearby = level.getEntitiesOfClass(Mothra.class,
                this.getBoundingBox().inflate(64.0, 32.0, 64.0));
        if (!nearby.isEmpty()) return false;

        if (OreSpawnConfig.MOTHRA_REQUIRES_SPAWNER.get()) {
            BlockPos here = this.blockPosition();
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    for (int dy = 1; dy <= 3; dy++) {
                        if (level.getBlockState(here.offset(dx, dy, dz)).is(Blocks.SPAWNER)) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        if (this.getY() < 70.0) return false;
        return level.canSeeSky(this.blockPosition());
    }
}
