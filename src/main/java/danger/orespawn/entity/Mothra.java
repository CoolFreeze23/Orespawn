package danger.orespawn.entity;

import danger.orespawn.MobStats;

import java.util.Comparator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.ai.GenericTargetSorter;
import danger.orespawn.util.MyUtils;
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
        // TF-035: orig Mothra.java:60,70 — targets sort by the shared
        // GenericTargetSorter (creepers and large mobs outrank closer small
        // ones), not by plain distance.
        this.targetSorter = new GenericTargetSorter(this);

        this.bodyPart  = new OreSpawnPartEntity<>(this, "body",  4.0f, 3.0f);
        this.wingLeft  = new OreSpawnPartEntity<>(this, "wingL", 5.0f, 1.5f);
        this.wingRight = new OreSpawnPartEntity<>(this, "wingR", 5.0f, 1.5f);
        this.headPart  = new OreSpawnPartEntity<>(this, "head",  2.0f, 2.0f);
        this.allParts = new PartEntity<?>[]{ bodyPart, wingLeft, wingRight, headPart };
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                // orig OreSpawnMain.java:6469 — Mothra 150 HP / 12 ATK / 8 armor
                .add(Attributes.MAX_HEALTH, MobStats.MOTHRA.maxHealth())
                .add(Attributes.MOVEMENT_SPEED, 0.35)
                .add(Attributes.ATTACK_DAMAGE, MobStats.MOTHRA.attackDamage())
                .add(Attributes.ARMOR, MobStats.MOTHRA.armor())
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

    /**
     * orig Mothra.java:424-483 (isSuitableTarget) — anything alive qualifies
     * except: peaceful difficulty, self, dead things, the shared ignore list,
     * targets out of line of sight, fellow OreSpawn fliers/hunters (Mothra,
     * Brutalfly, Vortex, VelocityRaptor, Cryolophosaurus, TerribleTerror,
     * LurkingTerror, CloudShark, Rotator, Bee, Mantis) and creative players.
     * The MothraPeaceful option is NOT checked here — the original gated it
     * in the AI step (:222) and in attackWithSomething (:382), which the port
     * mirrors, so behavior is identical with the flag on.
     */
    private boolean isSuitableTarget(LivingEntity target) {
        if (this.level().getDifficulty() == Difficulty.PEACEFUL) return false; // orig :425-427
        if (target == null || target == this || !target.isAlive()) return false; // orig :428-436
        if (MyUtils.isIgnoreable(target)) return false;              // orig :437-439
        if (!this.getSensing().hasLineOfSight(target)) return false; // orig :440-442
        if (target instanceof Mothra) return false;                  // orig :443-445
        if (target instanceof EntityBrutalfly) return false;         // orig :446-448
        if (target instanceof EntityVortex) return false;            // orig :449-451
        if (target instanceof VelocityRaptor) return false;          // orig :452-454
        if (target instanceof Cryolophosaurus) return false;         // orig :455-457
        if (target instanceof EntityTerribleTerror) return false;    // orig :458-460
        if (target instanceof EntityLurkingTerror) return false;     // orig :461-463
        if (target instanceof CloudShark) return false;              // orig :464-466
        if (target instanceof EntityRotator) return false;           // orig :467-469
        if (target instanceof EntityBee) return false;               // orig :470-472
        if (target instanceof EntityMantis) return false;            // orig :473-475
        if (target instanceof Player p && p.getAbilities().instabuild) return false; // orig :476-481
        return true;
    }

    private LivingEntity findSomethingToAttack() {
        // orig Mothra.java:486-488 — PlayNicely servers get no mob-vs-mob hunts
        if (OreSpawnConfig.PLAY_NICELY.get()) return null;
        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(15.0, 20.0, 15.0));
        entities.sort(this.targetSorter);
        for (LivingEntity targetEntity : entities) {
            if (this.isSuitableTarget(targetEntity)) return targetEntity;
        }
        return null;
    }

    /**
     * orig Mothra.java:379-422 (attackWithSomething) — difficulty-keyed
     * fireball from a muzzle 2.25 blocks ahead, aimed at the target's y+0.55:
     * Easy = vanilla SmallFireball; Normal = 50/50 SmallFireball or
     * BetterFireball; otherwise (Hard) always BetterFireball. BetterFireballs
     * get setNotMe() so they spare players/Dragons/Mothra on sweep. Small
     * fireballs play the bow sound at 0.75 volume, BetterFireballs the fuse
     * sound at 1.0; every shot self-heals 1 HP when below max.
     */
    private void attackWithFireball(LivingEntity target) {
        // orig Mothra.java:382-387 — MothraPeaceful option and peaceful
        // difficulty both silence the attack even when a target was found.
        if (OreSpawnConfig.MOTHRA_PEACEFUL.get()) return;
        if (this.level().getDifficulty() == Difficulty.PEACEFUL) return;
        double xzoff = 2.25; // orig Mothra.java:380
        double yoff = 0.0;   // orig Mothra.java:381
        double cx = this.getX() - xzoff * Math.sin(Math.toRadians(this.getYRot()));
        double cz = this.getZ() + xzoff * Math.cos(Math.toRadians(this.getYRot()));
        Vec3 accel = new Vec3(target.getX() - cx,
                target.getY() + 0.55 - (this.getY() + yoff),
                target.getZ() - cz);

        // orig Mothra.java:390-418 — Easy always small, Normal coin-flips
        // (nextInt(2)==0 small), the remaining branch (Hard) is always Better.
        boolean small = this.level().getDifficulty() == Difficulty.EASY
                || (this.level().getDifficulty() == Difficulty.NORMAL && this.random.nextInt(2) == 0);
        if (small) {
            SmallFireball fireball = new SmallFireball(this.level(), this, accel);
            fireball.setPos(cx, this.getY() + yoff, cz);
            // orig Mothra.java:394 — "random.bow", 0.75f
            this.level().playSound(null, this, SoundEvents.ARROW_SHOOT, this.getSoundSource(),
                    0.75f, 1.0f / (this.random.nextFloat() * 0.4f + 0.8f));
            this.level().addFreshEntity(fireball);
        } else {
            BetterFireball fireball = new BetterFireball(this.level(), this, accel);
            fireball.setPos(cx, this.getY() + yoff, cz);
            fireball.setNotMe(); // orig Mothra.java:407,415
            // orig Mothra.java:408,416 — "random.fuse", 1.0f
            this.level().playSound(null, this, SoundEvents.TNT_PRIMED, this.getSoundSource(),
                    1.0f, 1.0f / (this.random.nextFloat() * 0.4f + 0.8f));
            this.level().addFreshEntity(fireball);
        }
        // orig Mothra.java:419-421 — firing regenerates 1 HP when hurt
        if (this.getHealth() < this.getMaxHealth()) {
            this.heal(1.0f);
        }
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

        // orig Mothra.java:165,178-180 — attack roll is 1-in-3 by default,
        // tightened to 1-in-2 on Hard (the other half of the difficulty variant).
        int shoot = this.level().getDifficulty() == Difficulty.HARD ? 2 : 3;

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
                if (this.random.nextInt(shoot) == 0) this.attackWithFireball(target); // orig Mothra.java:229
            }
            if (target == null && this.random.nextInt(3) == 0) {
                LivingEntity hostile = this.findSomethingToAttack();
                if (hostile != null) {
                    this.currentFlightTarget = new BlockPos((int) hostile.getX(), (int) hostile.getY() + 5, (int) hostile.getZ());
                    if (this.random.nextInt(shoot) == 0) this.attackWithFireball(hostile); // orig Mothra.java:242
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

    // Item drops are data-driven via loot_table/entities/mothra.json
    // (orig Mothra.java:341-363: painting, 53 gold nuggets, 25 moth scales,
    // 3 blaze rods, 1 nether star). Non-item death behavior stays here.
    @Override
    public void die(DamageSource source) {
        super.die(source);
        // orig Mothra.java:344-362 — 20 "largeexplode" particles and
        // 20 Moths released on death (port Moth = luna_moth).
        if (this.level() instanceof ServerLevel serverLevel) {
            for (int i = 0; i < 20; ++i) {
                double ox = (this.random.nextFloat() - 0.5f) * 8.0f;
                double oy = (this.random.nextFloat() - 0.5f) * 4.0f;
                double oz = (this.random.nextFloat() - 0.5f) * 8.0f;
                serverLevel.sendParticles(ParticleTypes.EXPLOSION,
                        this.getX() + ox, this.getY() + 2.0 + oy, this.getZ() + oz,
                        1, 0.0, 0.0, 0.0, 0.0);
            }
            for (int i = 0; i < 20; ++i) {
                EntityLunaMoth moth = ModEntities.ENTITY_LUNA_MOTH.get().create(serverLevel);
                if (moth != null) {
                    moth.moveTo(this.getX() + 0.5, this.getY() + 1.0, this.getZ() + 0.5,
                            this.random.nextFloat() * 360.0f, 0.0f);
                    serverLevel.addFreshEntity(moth);
                }
            }
        }
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
