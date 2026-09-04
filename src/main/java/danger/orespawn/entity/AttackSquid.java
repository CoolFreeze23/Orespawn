package danger.orespawn.entity;

import danger.orespawn.MobStats;

import danger.orespawn.OreSpawnConfig;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.ai.GenericTargetSorter;
import danger.orespawn.entity.ai.TargetSelection;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.monster.CaveSpider;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class AttackSquid extends Monster {
    // OPT-011: cached SoundEvents — identical createVariableRangeEvent ids,
    // allocated once per class instead of on every sound query.
    private static final SoundEvent SND_SQUID_HURT = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "squid_hurt"));
    private static final SoundEvent SND_SQUID_DEATH = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "squid_death"));

    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(AttackSquid.class, EntityDataSerializers.INT);

    private static final double MOVE_SPEED = 0.25;
    private static final int NO_WATER_FOUND_SENTINEL = 99999;

    private int wasshot = 0;

    /**
     * orig AttackSquid.java:54 {@code TargetSorter}, :70 {@code new GenericTargetSorter(this)} — the shared
     * weighted-distance order (creepers halved, big silhouettes first) the hunt sorts its candidates by
     * (:609). ENT-S-117.
     */
    private final GenericTargetSorter targetSorter = new GenericTargetSorter(this);

    /**
     * orig AttackSquid.java:55 {@code buddy}: another Attack Squid the filter met and adopted on a 1-in-5 roll
     * (:595-600); with nothing to attack the squid swims after it at 1.0 (:515-517). Never persisted (orig
     * wrote WasShot only, :635-643; MOD-022 transient combat state). ENT-S-117.
     */
    @Nullable
    private LivingEntity buddy = null;

    private int closestWaterDistSq = NO_WATER_FOUND_SENTINEL;
    private int targetX = 0;
    private int targetY = 0;
    private int targetZ = 0;

    public AttackSquid(EntityType<? extends AttackSquid> type, Level level) {
        super(type, level);
        this.xpReward = 15;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MyEntityAIWanderALot(this, 16, 1.0));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        // orig OreSpawnMain.java:6510 — AttackSquid 10 HP / 8 ATK / 0 armor
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MobStats.ATTACK_SQUID.maxHealth())
                .add(Attributes.MOVEMENT_SPEED, MOVE_SPEED)
                .add(Attributes.ATTACK_DAMAGE, MobStats.ATTACK_SQUID.attackDamage())
                .add(Attributes.ARMOR, MobStats.ATTACK_SQUID.armor());
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

    public void setWasShot() {
        this.wasshot = 250;
    }

    @Override
    public boolean isPushedByFluid() {
        return true;
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        if (this.wasshot != 0) return false;
        return super.causeFallDamage(fallDistance, multiplier, source);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isRemoved()) return false;
        Entity attacker = source.getEntity();
        if (attacker instanceof AttackSquid) return false;
        // orig AttackSquid.java:373-378 — the water faction's own weapons
        // never wound a squid: damage whose true source is a WaterBall or a
        // WaterDragon is discarded before any retaliation. The original read
        // func_76346_g (the modern getEntity()); under the modern split
        // source model a projectile travels as the DIRECT entity with its
        // shooter as the true source, so the WaterBall check covers both
        // seams to keep "WaterBall fire never lands" — the original also
        // blanked that target-side (orig WaterBall.java:47-52).
        if (attacker instanceof WaterBall || source.getDirectEntity() instanceof WaterBall) return false;
        if (attacker instanceof WaterDragon) return false;
        if (attacker instanceof Mob mob) {
            this.setTarget(mob);
            this.getNavigation().moveTo(mob, 1.2);
        }
        return super.hurt(source, amount);
    }

    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;
        super.customServerAiStep();

        if (this.wasshot > 0) {
            --this.wasshot;
            if (this.wasshot == 0) {
                this.discard();
                return;
            }
            if (this.wasshot < 240) {
                this.setNoGravity(false);
            }
        }

        if (!this.isInWater() && this.random.nextInt(10) == 0) {
            this.closestWaterDistSq = NO_WATER_FOUND_SENTINEL;
            this.targetX = 0;
            this.targetY = 0;
            this.targetZ = 0;
            for (int i = 1; i < 12; ++i) {
                int j = Math.min(i, 5);
                if (this.scanForWater((int) this.getX(), (int) this.getY() - 1, (int) this.getZ(), i, j, i)) break;
                if (i >= 5) ++i;
            }
            if (this.closestWaterDistSq < NO_WATER_FOUND_SENTINEL) {
                this.getNavigation().moveTo(this.targetX, this.targetY - 1, this.targetZ, 1.33);
            } else {
                if (this.random.nextInt(25) == 1) {
                    this.hurt(this.damageSources().dryOut(), 1.0f);
                }
                if (this.getHealth() <= 0.0f) {
                    this.discard();
                    return;
                }
            }
        }

        if (this.random.nextInt(10) == 1) {                                  // orig AttackSquid.java:502
            LivingEntity target = this.findSomethingToAttack();              // orig :503 — the live stored target, else the hunt's transient pick (ENT-S-117)
            if (target != null) {                                            // orig :504
                if (this.distanceToSqr(target) < 9.0) {                      // orig :505
                    this.setAttacking(1);                                    // orig :506
                    // orig AttackSquid.java:507 — double melee roll (~40%/tick)
                    if (this.random.nextInt(4) == 0 || this.random.nextInt(5) == 1) {
                        this.doHurtTarget(target);                           // orig :508
                    }
                } else {
                    this.getNavigation().moveTo(target, 1.2);                // orig :511
                    this.watercanon(target);                                 // orig :512
                }
            } else {
                if (this.buddy != null) {                                    // orig :515-517 — nothing to attack: swim after the adopted buddy at 1.0 (ENT-S-117)
                    this.getNavigation().moveTo(this.buddy, 1.0);
                }
                this.setAttacking(0);                                        // orig :518
            }
        }
    }

    /**
     * orig AttackSquid.java:604-625 {@code findSomethingToAttack}: nothing under PlayNicely (:605-607, read live
     * as {@code OreSpawnConfig.PLAY_NICELY}); every {@code EntityLivingBase} whose box meets the squid's box
     * grown by 10/4/10 (:608 — players and the squid itself included), sorted by the {@link GenericTargetSorter}
     * (:609); a live stored target — the attacker {@link #hurt} stored (:386) or the revenge task's (:75) — is
     * answered ahead of the loop (:613-616), a dead or empty slot cleared (:617); else the first candidate the
     * filter accepts (:618-623), else null (:624). The pick is never stored: :622 returns it and :503 acts on
     * it for that tick alone — the port's Alien shape (orig Alien.java:367-386). {@link TargetSelection#firstMatch}
     * is the sort-and-loop, stable ties and the filter's call sequence — the buddy roll of :596 included —
     * preserved (OPT-021); orig sorted before reading the slot (:609, :613), a side-effect-free reorder.
     * ENT-S-117.
     */
    @Nullable
    private LivingEntity findSomethingToAttack() {
        if (OreSpawnConfig.PLAY_NICELY.get()) return null;                                   // orig :605-607
        List<LivingEntity> candidates = this.level().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(10.0, 4.0, 10.0));                             // orig :608
        LivingEntity current = this.getTarget();                                              // orig :613
        if (current != null && current.isAlive()) return current;                             // orig :614-616
        this.setTarget(null);                                                                 // orig :617
        return TargetSelection.firstMatch(candidates, this.targetSorter, this::isSuitableTarget); // orig :609, :618-624
    }

    /**
     * orig AttackSquid.java:551-602 {@code isSuitableTarget}, in the original's order: null / self / dead
     * (:552-560), line of sight (:561-563), a player when not creative (:564-567 — {@code capabilities.isCreativeMode},
     * the port's {@code Abilities.instabuild}, ENT-S-107), then the whitelist — Girlfriend (:568), Boyfriend
     * (:571), Zombie (:574), Villager (:577), Spider (:580), CaveSpider (:583 — a Spider subclass in both trees,
     * so the step never decides), Lizard (:592) — Ghost (:586) and GhostSkelly (:589) refused, another Attack
     * Squid refused and adopted as the {@link #buddy} on a 1-in-5 roll (:595-600; orig rolled the world random,
     * the port the entity's — the ENT-S-093 stream convention), and anything else that lives only while the
     * squid is a Squid Zooka shot ({@code wasshot != 0}, :601 — only ItemSquidZooka sets it, orig :42 / port :44;
     * the survey's "Kraken-launched" was wrong in both trees). ENT-S-117.
     */
    private boolean isSuitableTarget(LivingEntity target) {
        if (target == null || target == this || !target.isAlive()) return false;             // orig :552-560
        if (!this.getSensing().hasLineOfSight(target)) return false;                          // orig :561-563
        if (target instanceof Player player) return !player.getAbilities().instabuild;        // orig :564-567
        if (target instanceof Girlfriend) return true;                                        // orig :568-570
        if (target instanceof Boyfriend) return true;                                         // orig :571-573
        if (target instanceof Zombie) return true;                                            // orig :574-576
        if (target instanceof Villager) return true;                                          // orig :577-579
        if (target instanceof Spider) return true;                                            // orig :580-582
        if (target instanceof CaveSpider) return true;                                        // orig :583-585
        if (target instanceof Ghost) return false;                                            // orig :586-588
        if (target instanceof GhostSkelly) return false;                                      // orig :589-591
        if (target instanceof Lizard) return true;                                            // orig :592-594
        if (target instanceof AttackSquid) {                                                  // orig :595-600
            if (this.random.nextInt(5) == 1) this.buddy = target;                             // orig :596-598
            return false;                                                                     // orig :599
        }
        return this.wasshot != 0;                                                             // orig :601
    }

    /**
     * orig AttackSquid.java:523-549 — 1-in-5 per call, spits an InkSack
     * (1-in-3) or WaterBall from a muzzle 1.2 blocks ahead / 1.0 up, aimed at
     * the target's y+0.25 with a 0.2-per-horizontal-block arc, velocity 1.4,
     * inaccuracy 5.0. The original mixed head yaw for the muzzle X offset and
     * body yaw for Z (orig :529,539); that quirk is preserved.
     */
    private void watercanon(LivingEntity target) {
        double yoff = 1.0;
        double xzoff = 1.2;
        if (this.random.nextInt(5) != 1) return;

        double muzzleX = this.getX() - xzoff * Math.sin(Math.toRadians(this.yHeadRot));
        double muzzleY = this.getY() + yoff;
        double muzzleZ = this.getZ() + xzoff * Math.cos(Math.toRadians(this.getYRot()));
        ThrowableProjectile projectile = this.random.nextInt(3) == 1
                ? new InkSack(this.level(), this)
                : new WaterBall(this.level(), this);
        projectile.setPos(muzzleX, muzzleY, muzzleZ);

        double dx = target.getX() - this.getX();
        double dy = target.getY() + 0.25 - muzzleY;
        double dz = target.getZ() - this.getZ();
        double arc = Math.sqrt(dx * dx + dz * dz) * 0.2;
        projectile.shoot(dx, dy + arc, dz, 1.4f, 5.0f);
        this.level().playSound(null, this, SoundEvents.ARROW_SHOOT, this.getSoundSource(),
                0.75f, 1.0f / (this.random.nextFloat() * 0.4f + 0.8f));
        this.level().addFreshEntity(projectile);
    }

    private boolean scanForWater(int x, int y, int z, int dx, int dy, int dz) {
        int found = 0;
        for (int i = -dy; i <= dy; ++i) {
            for (int j = -dz; j <= dz; ++j) {
                found += checkWaterAt(x + dx, y + i, z + j, dx * dx + j * j + i * i);
                found += checkWaterAt(x - dx, y + i, z + j, dx * dx + j * j + i * i);
            }
        }
        for (int i = -dx; i <= dx; ++i) {
            for (int j = -dz; j <= dz; ++j) {
                found += checkWaterAt(x + i, y + dy, z + j, dy * dy + j * j + i * i);
                found += checkWaterAt(x + i, y - dy, z + j, dy * dy + j * j + i * i);
            }
        }
        for (int i = -dx; i <= dx; ++i) {
            for (int j = -dy; j <= dy; ++j) {
                found += checkWaterAt(x + i, y + j, z + dz, dz * dz + j * j + i * i);
                found += checkWaterAt(x + i, y + j, z - dz, dz * dz + j * j + i * i);
            }
        }
        return found != 0;
    }

    private int checkWaterAt(int x, int y, int z, int dist) {
        BlockState state = this.level().getBlockState(new BlockPos(x, y, z));
        if (state.is(Blocks.WATER) && dist < this.closestWaterDistSq) {
            this.closestWaterDistSq = dist;
            this.targetX = x;
            this.targetY = y;
            this.targetZ = z;
            return 1;
        }
        return 0;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("WasShot", this.wasshot);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.wasshot = tag.getInt("WasShot");
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SND_SQUID_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SND_SQUID_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 1.0f;
    }

    // Death drops are fully data-driven via loot_table/entities/attack_squid.json
    // (orig AttackSquid.java:169-344: one d50 roll of the Gold gear table,
    // plus 1-3 raw fish).

    /** orig AttackSquid.java:645-651 — y&gt;=50 and daytime (the pre-D1 port had this inverted: y&lt;50 + canSeeSky; ENT-A-021). */
    @Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnType) {
        if (this.getY() < 50.0) return false;
        return OriginalSpawnGates.isDaytime(level);
    }
}
