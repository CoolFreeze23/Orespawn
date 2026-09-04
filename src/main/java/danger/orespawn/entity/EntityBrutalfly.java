package danger.orespawn.entity;

import danger.orespawn.MobStats;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.ai.GenericTargetSorter;
import danger.orespawn.util.MyUtils;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.Mth;
import danger.orespawn.entity.ai.TargetSelection;

public class EntityBrutalfly extends Monster {
    // OPT-011: cached SoundEvents — identical createVariableRangeEvent ids,
    // allocated once per class instead of on every sound query.
    private static final SoundEvent SND_MOTHRAWINGS = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "mothrawings"));
    private BlockPos currentFlightTarget = null;
    private int lastX = 0;
    private int lastZ = 0;
    private int lastY = 0;
    private int stuckCount = 0;
    private int wingSound = 0;
    private int healthTicker = 100;

    public EntityBrutalfly(EntityType<? extends EntityBrutalfly> type, Level level) {
        super(type, level);
        this.xpReward = 100;
    }

    public static AttributeSupplier.Builder createAttributes() {
        // orig OreSpawnMain.java:6470 — Brutalfly 110 HP / 10 ATK / 6 armor;
        // speed 0.35 matches orig Brutalfly.java:51.
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MobStats.BRUTALFLY.maxHealth())
                .add(Attributes.MOVEMENT_SPEED, 0.35)
                .add(Attributes.ATTACK_DAMAGE, MobStats.BRUTALFLY.attackDamage())
                .add(Attributes.ARMOR, MobStats.BRUTALFLY.armor());
    }

    @Override
    protected float getSoundVolume() {
        return 1.5f;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        // orig Brutalfly.java:102-104 — func_70621_aR returns null: the
        // Brutalfly is silent when hurt (only its death explodes).
        return null;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.GENERIC_EXPLODE.value();
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        Vec3 motion = this.getDeltaMovement();
        this.setDeltaMovement(motion.x, motion.y * 0.6, motion.z);

        ++this.wingSound;
        if (this.wingSound > 30) {
            if (!this.level().isClientSide) {
                this.level().playSound(null, this.blockPosition(),
                        SND_MOTHRAWINGS,
                        this.getSoundSource(), 1.0f, 1.0f);
            }
            this.wingSound = 0;
        }

        --this.healthTicker;
        if (this.healthTicker <= 0) {
            if (this.getHealth() < this.getMaxHealth()) {
                this.heal(1.0f);
            }
            this.healthTicker = 100;
        }
    }

    /**
     * orig Brutalfly.java:147-149 — clear-path probe from the eye line
     * (y + 0.75) to a point, used to validate flight-target candidates.
     */
    public boolean canSeeTarget(double targetX, double targetY, double targetZ) {
        Vec3 from = new Vec3(this.getX(), this.getY() + 0.75, this.getZ());
        Vec3 to = new Vec3(targetX, targetY, targetZ);
        HitResult result = this.level().clip(new ClipContext(
                from, to, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, this)); // selection bounds, no liquid stop, as orig rayTraceBlocks(start, end, false) — the ENT-S-089 mapping (ENT-S-121)
        return result.getType() == HitResult.Type.MISS;
    }

    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;
        super.customServerAiStep();

        if (this.lastX == (int) this.getX() && this.lastY == (int) this.getY() && this.lastZ == (int) this.getZ()) {
            ++this.stuckCount;
        } else {
            this.stuckCount = 0;
            this.lastX = (int) this.getX();
            this.lastY = (int) this.getY();
            this.lastZ = (int) this.getZ();
        }

        if (this.currentFlightTarget == null) {
            this.currentFlightTarget = this.blockPosition();
        }

        double distSq = this.currentFlightTarget.distSqr(this.blockPosition());

        if (this.stuckCount > 30 || this.random.nextInt(200) == 0 || distSq < 9.0) {
            // orig Brutalfly.java:175-191 — terrain-descent scan: probe the
            // nine columns at x/z offsets {-5, 0, +5} up to 19 blocks down for
            // the first non-air block; when even the nearest ground over all
            // columns is more than 10 below, bias the new flight target down
            // by (dist - 10 + 1) so the Brutalfly re-hugs the terrain.
            int dist = 20;
            for (int i = -5; i <= 5; i += 5) {
                for (int j = -5; j <= 5; j += 5) {
                    for (int k = 1; k < 20; ++k) {
                        BlockPos probe = new BlockPos(
                                (int) this.getX() + j, (int) this.getY() - k, (int) this.getZ() + i);
                        if (this.level().getBlockState(probe).isAir()) continue;
                        if (k < dist) dist = k;
                        break;
                    }
                }
            }
            int down = dist > 10 ? dist - 10 + 1 : 0;

            int keepTrying = 30;
            while (keepTrying > 0) {
                int xdir = this.random.nextInt(2) == 0 ? -1 : 1;
                int zdir = this.random.nextInt(2) == 0 ? -1 : 1;
                // orig Brutalfly.java:202-203 — z offset is drawn before x.
                int newz = (this.random.nextInt(20) + 8) * zdir;
                int newx = (this.random.nextInt(20) + 8) * xdir;

                BlockPos newTarget = new BlockPos(
                        (int) this.getX() + newx,
                        (int) this.getY() + this.random.nextInt(7) - 1 - down,
                        (int) this.getZ() + newz);

                // orig Brutalfly.java:193-210 — each candidate is written into
                // currentFlightTarget before validation (func_71571_b mutates
                // in place), so exhausting all 30 retries leaves the last
                // failed candidate as the target; a candidate only ends the
                // loop when it is air AND visible from the eye line.
                this.currentFlightTarget = newTarget;
                if (this.level().getBlockState(newTarget).isAir()
                        && this.canSeeTarget(newTarget.getX(), newTarget.getY(), newTarget.getZ())) {
                    break;
                }
                --keepTrying;
            }
            this.stuckCount = 0;
        }

        // orig Brutalfly.java:155,168-170 — barrage odds 1-in-3, 1-in-2 on Hard.
        int shoot = this.level().getDifficulty() == Difficulty.HARD ? 2 : 3;

        if (this.random.nextInt(6) == 0) {
            // orig Brutalfly.java:213-227 — players are only ever strafed with
            // fireballs (no melee path against players). ENT-S-109: the strafe's
            // creative test (orig :217 capabilities.isCreativeMode) is
            // Abilities.instabuild — the ENT-S-107 mapping — not invulnerable.
            Player target = this.level().getNearestPlayer(this, 30.0);
            if (target != null && !target.getAbilities().instabuild && this.getSensing().hasLineOfSight(target)) { // orig :217 (ENT-S-109)
                this.currentFlightTarget = target.blockPosition().above(4);
                if (this.random.nextInt(shoot) == 0) {
                    this.attackWithSomething(target);
                }
            }

            // orig Brutalfly.java:228-241 — mobs get fireballs beyond distSq 25,
            // melee inside it.
            if (target == null && this.random.nextInt(3) == 0) {
                LivingEntity mobTarget = findSomethingToAttack();
                if (mobTarget != null) {
                    this.currentFlightTarget = mobTarget.blockPosition().above(5);
                    if (this.distanceToSqr(mobTarget) > 25.0) {
                        if (this.random.nextInt(shoot) == 0) {
                            this.attackWithSomething(mobTarget);
                        }
                    } else {
                        this.doHurtTarget(mobTarget);
                    }
                }
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
        this.setYRot(this.getYRot() + yawDiff / 8.0f);
    }

    /**
     * orig Brutalfly.java:369-406 (attackWithSomething) — difficulty-keyed
     * fireball from a muzzle 2.25 blocks ahead, aimed at the target's y+0.55:
     * Easy = vanilla SmallFireball; Normal = 50/50 SmallFireball or
     * BetterFireball; otherwise BetterFireball. Small fireballs play the bow
     * sound at 0.75 volume, BetterFireballs the fuse sound at 1.0; every shot
     * self-heals 1 HP when below max.
     */
    private void attackWithSomething(LivingEntity target) {
        double xzoff = 2.25;
        double yoff = 0.0;
        double cx = this.getX() - xzoff * Math.sin(Math.toRadians(this.getYRot()));
        double cz = this.getZ() + xzoff * Math.cos(Math.toRadians(this.getYRot()));
        Vec3 accel = new Vec3(target.getX() - cx,
                target.getY() + 0.55 - (this.getY() + yoff),
                target.getZ() - cz);

        boolean small = this.level().getDifficulty() == Difficulty.EASY
                || (this.level().getDifficulty() == Difficulty.NORMAL && this.random.nextInt(2) == 0);
        if (small) {
            SmallFireball fireball = new SmallFireball(this.level(), this, accel);
            fireball.setPos(cx, this.getY() + yoff, cz);
            this.level().playSound(null, this, SoundEvents.ARROW_SHOOT, this.getSoundSource(),
                    0.75f, 1.0f / (this.random.nextFloat() * 0.4f + 0.8f));
            this.level().addFreshEntity(fireball);
        } else {
            BetterFireball fireball = new BetterFireball(this.level(), this, accel);
            fireball.setPos(cx, this.getY() + yoff, cz);
            fireball.setNotMe();
            this.level().playSound(null, this, SoundEvents.TNT_PRIMED, this.getSoundSource(),
                    1.0f, 1.0f / (this.random.nextFloat() * 0.4f + 0.8f));
            this.level().addFreshEntity(fireball);
        }
        if (this.getHealth() < this.getMaxHealth()) {
            this.heal(1.0f);
        }
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
        Entity attacker = source.getEntity();
        if (attacker instanceof EntityBrutalfly) return false;
        boolean ret = super.hurt(source, amount);
        if (attacker != null && this.currentFlightTarget != null) {
            this.currentFlightTarget = attacker.blockPosition().above(2);
        }
        return ret;
    }

    // Item drops are data-driven via loot_table/entities/brutalfly.json
    // (orig Brutalfly.java:339-353: 53 gold nuggets). Non-item death
    // behavior (explosion puffs + 20 Butterflies) stays in code below.
    @Override
    public void die(DamageSource source) {
        super.die(source);
        // orig Brutalfly.java:341-352 — 20 "largeexplode" particles and
        // 20 Butterflies released on death.
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
                EntityButterfly butterfly = ModEntities.ENTITY_BUTTERFLY.get().create(serverLevel);
                if (butterfly != null) {
                    butterfly.moveTo(this.getX() + 0.5, this.getY() + 1.0, this.getZ() + 0.5,
                            this.random.nextFloat() * 360.0f, 0.0f);
                    serverLevel.addFreshEntity(butterfly);
                }
            }
        }
    }

    @Nullable
    private LivingEntity findSomethingToAttack() {
        // orig Brutalfly.java:444-446 — PlayNicely disables the hunt entirely.
        if (OreSpawnConfig.PLAY_NICELY.get()) return null;
        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(25.0, 20.0, 25.0));
        // TF-035: orig Brutalfly.java:50,60,448 — candidates sort with
        // GenericTargetSorter (creeper-halved / big-mob-prioritized), not
        // plain distance.
        // OPT-021: nearest-first pick without the full list sort; TargetSelection
        // preserves the removed sort's order and stable-tie semantics exactly.
        return TargetSelection.firstMatch(entities, new GenericTargetSorter(this), this::isSuitableTarget);
    }

    /**
     * orig Brutalfly.java:408-441 — the mob-hunt filter: monsters yes, players unless
     * creative, nothing else. ENT-S-109: the player branch's
     * {@code capabilities.isCreativeMode} (orig :436-439) is {@code Abilities.instabuild}
     * — the ENT-S-107 mapping — not {@code invulnerable}.
     */
    private boolean isSuitableTarget(LivingEntity target) {
        if (target == null || target == this || !target.isAlive()) return false;
        if (target instanceof EntityBrutalfly) return false;
        // orig Brutalfly.java:421-426 — Mothra and the Vortex are fellow
        // fliers the Brutalfly never hunts.
        if (target instanceof Mothra) return false;
        if (target instanceof EntityVortex) return false;
        // orig Brutalfly.java:427-429 — shared ignore list (rock bases, ants and
        // termites, butterflies, mosquitoes, dragonflies, fireflies, crickets,
        // cockatiels, ghosts, ghost skellies, elevators; ENT-S-101 restored the
        // 1.7.10 membership, so fairies, cave fishers and coins are prey again).
        if (MyUtils.isIgnoreable(target)) return false;
        if (!this.getSensing().hasLineOfSight(target)) return false;
        if (target instanceof Monster) return true;
        if (target instanceof Player p) return !p.getAbilities().instabuild; // orig Brutalfly.java:436-439 isCreativeMode (ENT-S-109)
        return false;
    }

    /** orig Brutalfly.java:290-329 — spawner bypass; y>=70; darkness; night; 6x9x8 clear-air volume; no other Brutalfly within 64/32/64. */
    @Override
    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level,
                                   net.minecraft.world.entity.MobSpawnType spawnType) {
        if (OriginalSpawnGates.nearOwnSpawner(this, level, -2, 2, 1, 3)) return true;
        if (this.getY() < 70.0) return false;
        if (!OriginalSpawnGates.isDarkEnough(this, level)) return false;
        if (OriginalSpawnGates.isDaytime(level)) return false;
        if (!OriginalSpawnGates.airBox(this, level, -3, 2, 1, 9, -4, 3)) return false;
        return !OriginalSpawnGates.anyOtherNearby(this, level, EntityBrutalfly.class, 64.0, 32.0, 64.0);
    }

    /** orig Brutalfly.java:265 — func_145773_az -> true: never presses plates or tripwires (ENT-S-090). */
    @Override
    public boolean isIgnoringBlockTriggers() {
        return true;
    }
}
