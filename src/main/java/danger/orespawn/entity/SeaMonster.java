package danger.orespawn.entity;

import danger.orespawn.MobStats;

import danger.orespawn.ModItems;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.entity.ai.GenericTargetSorter;
import danger.orespawn.entity.ai.TargetSelection;
import danger.orespawn.util.MyUtils;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class SeaMonster extends Monster {
    // OPT-011: cached SoundEvents — identical createVariableRangeEvent ids,
    // allocated once per class instead of on every sound query.
    private static final SoundEvent SND_SEAMONSTER_LIVING = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "seamonster_living"));
    private static final SoundEvent SND_SEAMONSTER_HIT = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "seamonster_hit"));
    private static final SoundEvent SND_SEAMONSTER_DEATH = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "seamonster_death"));
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(SeaMonster.class, EntityDataSerializers.INT);

    private static final double MOVE_SPEED = 0.25;

    private int hurtCooldown = 0;
    private float dynamicMoveSpeed = 0.25f;
    private int closestWaterDistance = 99999;
    private int targetX = 0, targetY = 0, targetZ = 0;

    /**
     * OPT-009: the speed genuinely varies (water/land), so the per-tick write
     * stays, but the AttributeInstance is resolved once instead of via a map
     * lookup every tick. Attribute instances live exactly as long as the entity
     * (a dimension change constructs a fresh entity), so this cannot go stale.
     */
    private final net.minecraft.world.entity.ai.attributes.AttributeInstance movementSpeedAttribute;

    /**
     * orig SeaMonster.java:39 {@code TargetSorter}, :55 {@code new GenericTargetSorter(this)} — the shared weighted-distance
     * order (creepers halved, big silhouettes first) the scan sorts its candidates by (:518; the ledger's T4 row, closed by the
     * sorter here). ENT-S-135.
     */
    private final GenericTargetSorter targetSorter = new GenericTargetSorter(this);

    /**
     * The last pick the pass handed to the target slot. 1.7.10 stored the scan's pick nowhere — the pass acted on it
     * for that tick and re-derived it on the next (orig SeaMonster.java:527-532); only a target stored by {@link #hurt} or the revenge
     * task persisted, answered ahead of the scan while alive (:522-525). The port stores the pick so the next pass
     * can tell its own occupant from a stored one: under this mark it is re-derived every pass (replaced, or cleared
     * when not found again), never sticky; a target set by any other path is left alone (the ENT-S-108 slot rule;
     * see {@link #setTarget}). ENT-S-129.
     */
    @Nullable
    private LivingEntity scanPick;

    public SeaMonster(EntityType<? extends SeaMonster> type, Level level) {
        super(type, level);
        this.xpReward = 150;
        this.movementSpeedAttribute = this.getAttribute(Attributes.MOVEMENT_SPEED);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MyEntityAIWanderALot(this, 16, 1.0));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 10.0f));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Mob.class, 8.0f));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        // orig OreSpawnMain.java:6493 — SeaMonster 110 HP / 14 ATK / 8 armor
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MobStats.SEA_MONSTER.maxHealth())
                .add(Attributes.MOVEMENT_SPEED, MOVE_SPEED)
                .add(Attributes.ATTACK_DAMAGE, MobStats.SEA_MONSTER.attackDamage())
                .add(Attributes.ARMOR, MobStats.SEA_MONSTER.armor());
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
    public void aiStep() {
        super.aiStep();
        // orig SeaMonster.java:126 — onLivingUpdate sets moveSpeed to 0.55 in water / 0.25 on land,
        // and orig SeaMonster.java:93 — onUpdate writes it into the MOVEMENT_SPEED attribute every tick.
        this.dynamicMoveSpeed = this.isInWater() ? 0.55f : 0.25f;
        // OPT-009: cached instance; setBaseValue itself no-ops when unchanged.
        this.movementSpeedAttribute.setBaseValue(this.dynamicMoveSpeed);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (super.doHurtTarget(target)) {
            if (target instanceof LivingEntity) {
                double knockbackStrength = 0.6;
                double upwardKnockback = 0.1;
                float angleToTarget = (float) Math.atan2(target.getZ() - this.getZ(), target.getX() - this.getX());
                if (target instanceof Player) upwardKnockback *= 2.0;
                target.push(Math.cos(angleToTarget) * knockbackStrength, upwardKnockback, Math.sin(angleToTarget) * knockbackStrength);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.type().msgId().equals("cactus")) return false;
        Entity attacker = source.getEntity();
        boolean ret = false;
        if (this.hurtCooldown <= 0) {
            ret = super.hurt(source, amount);
            this.hurtCooldown = 8;
        }
        // orig SeaMonster.java:357-364 — an EntityLiving attacker becomes the stored target, read ahead of the scan
        // (:522-525); the revenge task stores any other living attacker: the scan's mark on a pick that turned on the
        // monster ends exactly when this hit stores it — the Mob store below, or super.hurt's lastHurtByMob record of
        // this tick; a hit the hurt timer swallowed stores nothing through super.hurt and keeps the mark (ENT-S-129)
        if (attacker != null && attacker == this.scanPick && (attacker instanceof Mob
                || (this.getLastHurtByMob() == attacker && this.getLastHurtByMobTimestamp() == this.tickCount))) this.scanPick = null;
        if (attacker instanceof Mob mob) {
            if (attacker instanceof SeaMonster) return false;
            this.setTarget(mob);
            this.getNavigation().moveTo(mob, 1.2);
        }
        return ret;
    }

    /**
     * A change of occupant by any other path — the revenge goal's start or stop, a hurt store, the melee goal's
     * forget roll, an event handler — ends the scan's ownership of the slot; a re-assert of the occupant already
     * there keeps it: {@code TargetGoal.canContinueToUse} re-sets the mob's CURRENT target on every cleanup pass
     * while the revenge goal runs, and an every-set clear turned the scan's own pick — placed on the pass that
     * dropped a dead revenge target — into a sticky one (ENT-S-117 refuter B's window). The port-wide convention
     * ruled in ENT-S-129 (the Water Dragon's ENT-S-117 form); the hurt hand-off is in {@link #hurt}. ENT-S-108.
     */
    @Override
    public void setTarget(@Nullable LivingEntity target) {
        LivingEntity before = this.getTarget();
        super.setTarget(target);
        if (this.getTarget() != before) this.scanPick = null; // ENT-S-129: the mark ends on a change of occupant only
    }

    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;
        super.customServerAiStep();

        if (this.hurtCooldown > 0) --this.hurtCooldown;

        if (!this.isInWater() && this.random.nextInt(25) == 0) {
            this.closestWaterDistance = 99999;
            this.targetX = 0; this.targetY = 0; this.targetZ = 0;
            for (int i = 1; i < 12; ++i) {
                int j = Math.min(i, 10);
                if (this.scanForWater((int) this.getX(), (int) this.getY() - 1, (int) this.getZ(), i, j, i)) break;
                if (i >= 5) ++i;
            }
            if (this.closestWaterDistance < 99999) {
                this.getNavigation().moveTo(this.targetX, this.targetY - 1, this.targetZ, 1.33);
            } else {
                if (this.random.nextInt(40) == 1) {
                    this.hurt(this.damageSources().dryOut(), 1.0f);
                }
                if (this.getHealth() <= 0.0f) {
                    this.discard();
                    return;
                }
            }
        }

        if (this.random.nextInt(5) == 1) {
            // orig SeaMonster.java:514-516 — findSomethingToAttack answers null under PlayNicely ahead of its stored-target
            // read (:522-525) and its scan (:527-532); the port's pick is this inline block, gated as a whole (ENT-S-115).
            boolean playNicely = OreSpawnConfig.PLAY_NICELY.get();
            LivingEntity target = playNicely ? null : this.getTarget();      // orig :522 — the stored target, read inside the gated method
            if (target != null && target == this.scanPick) {
                target = null;                                               // orig :527-532 — the scan's pick was never stored: its occupant is re-derived by this pass (ENT-S-129)
            } else if (target != null && !target.isAlive()) {
                this.setTarget(null);                                        // orig :523-526 — a live stored target is answered ahead of the scan; a dead one is cleared (ENT-S-129)
                target = null;
            }
            if (target == null && !playNicely) {
                target = this.findSomethingToAttack();                       // orig :517-518, :527-532 — the 16/4/16 box, sorted, the first the filter accepts (ENT-S-135); HEAD's getNearestPlayer(this, 16.0) was a players-only sphere
                // the scan's answer handed to the slot under the ownership mark — re-derived next pass, cleared when not
                // found again (ENT-S-129, the ENT-S-108 slot rule)
                if (target != this.getTarget()) this.setTarget(target);
                this.scanPick = this.getTarget();
            }
            if (target != null && target.isAlive()) {
                this.lookAt(target, 10.0f, 10.0f);
                double range = (4.0 + target.getBbWidth() / 2.0) * (4.0 + target.getBbWidth() / 2.0);
                if (this.distanceToSqr(target) < range) {
                    this.setAttacking(1);
                    if (this.random.nextInt(4) == 0) {
                        this.doHurtTarget(target);
                    }
                } else {
                    this.getNavigation().moveTo(target, 1.0);
                }
            } else {
                this.setAttacking(0);
            }
        }

        if (this.random.nextInt(120) == 1 && this.isInWater() && this.getHealth() < this.getMaxHealth()) {
            this.playSound(SoundEvents.GENERIC_SPLASH, 1.5f, this.random.nextFloat() * 0.2f + 0.9f);
            this.heal(1.0f);
        }
    }

    /**
     * orig SeaMonster.java:513-533 {@code findSomethingToAttack}: nothing under PlayNicely (:514-516); every
     * {@code EntityLivingBase} whose box meets the monster's box grown by 16/4/16 (:517, {@code getEntitiesWithinAABB} —
     * every living thing, where HEAD's {@code getNearestPlayer(this, 16.0)} scanned players only, and a BOX where that was a
     * sphere of 16 from the position); sorted by the {@link GenericTargetSorter} (:518); the first the filter accepts wins
     * (:527-532), else null (:533). {@link TargetSelection#firstMatch} is that sort-and-loop, stable ties included (OPT-021).
     * Orig's stored-target read between the sort and the loop (:522-526) is the pass's, ahead of this call (ENT-S-129).
     * ENT-S-135.
     */
    @Nullable
    private LivingEntity findSomethingToAttack() {
        if (OreSpawnConfig.PLAY_NICELY.get()) return null;                        // orig :514-516
        List<LivingEntity> candidates = this.level().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(16.0, 4.0, 16.0));                  // orig :517
        return TargetSelection.firstMatch(candidates, this.targetSorter, this::isSuitableTarget); // orig :518, :527-532
    }

    /**
     * orig SeaMonster.java:487-511 {@code isSuitableTarget}, in the original's order: null / self / dead (:488-496), line of
     * sight (:497-499), then the player branch — creative refused (:500-503, {@code isCreativeMode} =
     * {@code Abilities.instabuild}) — a Sea Monster refused (:504-506), any {@code EntityMob} taken (:507-509, the port's
     * {@code Monster}), and the shared attackable-non-mob grant list last (:510, {@link MyUtils#isAttackableNonMob}, orig's
     * membership since ENT-S-128). ENT-S-135.
     */
    private boolean isSuitableTarget(LivingEntity target) {
        if (target == null || target == this || !target.isAlive()) return false;    // orig :488-496
        if (!this.getSensing().hasLineOfSight(target)) return false;                // orig :497-499 — canSee, the eye-to-eye block ray
        if (target instanceof Player player) return !player.getAbilities().instabuild; // orig :500-503
        if (target instanceof SeaMonster) return false;                             // orig :504-506
        if (target instanceof Monster) return true;                                 // orig :507-509 (EntityMob)
        return MyUtils.isAttackableNonMob(target);                                  // orig :510
    }

    private boolean scanForWater(int x, int y, int z, int dx, int dy, int dz) {
        int found = 0;
        for (int i = -dy; i <= dy; ++i) for (int j = -dz; j <= dz; ++j) {
            found += checkWaterAt(x + dx, y + i, z + j, dx * dx + j * j + i * i);
            found += checkWaterAt(x - dx, y + i, z + j, dx * dx + j * j + i * i);
        }
        for (int i = -dx; i <= dx; ++i) for (int j = -dz; j <= dz; ++j) {
            found += checkWaterAt(x + i, y + dy, z + j, dy * dy + j * j + i * i);
            found += checkWaterAt(x + i, y - dy, z + j, dy * dy + j * j + i * i);
        }
        for (int i = -dx; i <= dx; ++i) for (int j = -dy; j <= dy; ++j) {
            found += checkWaterAt(x + i, y + j, z + dz, dz * dz + j * j + i * i);
            found += checkWaterAt(x + i, y + j, z - dz, dz * dz + j * j + i * i);
        }
        return found != 0;
    }

    private int checkWaterAt(int x, int y, int z, int dist) {
        BlockState state = this.level().getBlockState(new BlockPos(x, y, z));
        if (state.is(Blocks.WATER) && dist < this.closestWaterDistance) {
            this.closestWaterDistance = dist; this.targetX = x; this.targetY = y; this.targetZ = z;
            return 1;
        }
        return 0;
    }

    // Death drops are fully data-driven via loot_table/entities/sea_monster.json
    // (orig SeaMonster.java:170-322: scale, painting, 9-14 raw fish,
    // one d20 roll of the Iron gear table).

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        if (this.random.nextInt(3) == 0) {
            return SND_SEAMONSTER_LIVING;
        }
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SND_SEAMONSTER_HIT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SND_SEAMONSTER_DEATH;
    }

    /** orig SeaMonster.java:544-570 — "Sea Monster" spawner bypass; y>=50; night; darkness; no other SeaMonster within 16/5/16. */
    @Override
    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level,
                                   net.minecraft.world.entity.MobSpawnType spawnType) {
        if (OriginalSpawnGates.nearOwnSpawner(this, level)) return true;
        if (this.getY() < 50.0) return false;
        if (OriginalSpawnGates.isDaytime(level)) return false;
        if (!OriginalSpawnGates.isDarkEnough(this, level)) return false;
        return !OriginalSpawnGates.anyOtherNearby(this, level, SeaMonster.class, 16.0, 5.0, 16.0);
    }
}
