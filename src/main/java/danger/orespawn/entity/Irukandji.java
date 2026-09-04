package danger.orespawn.entity;

import danger.orespawn.MobStats;
import danger.orespawn.ModItems;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.entity.ai.GenericTargetSorter;
import danger.orespawn.entity.ai.TargetSelection;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
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

public class Irukandji extends Monster {
    // OPT-011: cached SoundEvents — identical createVariableRangeEvent ids,
    // allocated once per class instead of on every sound query.
    private static final SoundEvent SND_LITTLE_SPLAT = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "little_splat"));
    private static final SoundEvent SND_RATDEAD = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "ratdead"));
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(Irukandji.class, EntityDataSerializers.INT);

    private static final double MOVE_SPEED = 0.15;
    private static final int NO_WATER_FOUND_SENTINEL = 99999;
    private static final float EMPTY_HAND_RETALIATION_DAMAGE = 200.0f;

    private int closestWaterDistanceSq = NO_WATER_FOUND_SENTINEL;
    private int targetX = 0;
    private int targetY = 0;
    private int targetZ = 0;

    /**
     * orig Irukandji.java:32 {@code TargetSorter}, :47 {@code new GenericTargetSorter(this)} — the shared weighted-distance
     * order (creepers halved, big silhouettes first) the scan sorts its candidates by (:295). For standing players it reduces
     * to nearest (a uniform 1.08 silhouette); a sneaking player's 0.9 is undivided and ranks differently (the ledger's T4 row,
     * closed by the sorter here). ENT-S-135.
     */
    private final GenericTargetSorter targetSorter = new GenericTargetSorter(this);

    /**
     * The last pick the pass handed to the target slot. 1.7.10 stored the scan's pick nowhere — the pass acted on it
     * for that tick and re-derived it on the next (orig Irukandji.java:304-309); only a target stored by {@link #hurt} or the revenge
     * task persisted, answered ahead of the scan while alive (:299-302). The port stores the pick so the next pass
     * can tell its own occupant from a stored one: under this mark it is re-derived every pass (replaced, or cleared
     * when not found again), never sticky; a target set by any other path is left alone (the ENT-S-108 slot rule;
     * see {@link #setTarget}). ENT-S-129.
     */
    @Nullable
    private LivingEntity scanPick;

    public Irukandji(EntityType<? extends Irukandji> type, Level level) {
        super(type, level);
        this.xpReward = 50;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MyEntityAIWander(this, 1.0f));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        // orig OreSpawnMain.java:6509 — Irukandji 1 HP / 20 ATK / 0 armor.
        // (The 200.0f is only the empty-hand retaliation, orig Irukandji.java:96.)
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MobStats.IRUKANDJI.maxHealth())
                .add(Attributes.MOVEMENT_SPEED, MOVE_SPEED)
                .add(Attributes.ATTACK_DAMAGE, MobStats.IRUKANDJI.attackDamage())
                .add(Attributes.ARMOR, MobStats.IRUKANDJI.armor());
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
    public boolean hurt(DamageSource source, float amount) {
        if (this.isRemoved()) return false;
        Entity damager = source.getEntity();
        if (damager instanceof Irukandji) return false;
        if (damager instanceof Player player && player.getMainHandItem().isEmpty()) {
            player.hurt(this.damageSources().mobAttack(this), EMPTY_HAND_RETALIATION_DAMAGE);
            return false;
        }
        if (damager instanceof Mob mob) {
            if (mob == this.scanPick) this.scanPick = null; // orig Irukandji.java:135-156 — the attacker is the STORED target from here (read ahead of the scan, :299-302): the scan's mark on a pick that turned on the jelly ends with the store (ENT-S-129)
            this.setTarget(mob);
            this.getNavigation().moveTo(mob, 1.2);
        }
        boolean ret = super.hurt(source, amount);
        if (damager != null && damager == this.scanPick && this.getLastHurtByMob() == damager
                && this.getLastHurtByMobTimestamp() == this.tickCount) this.scanPick = null; // the revenge goal's store of any other living attacker — super.hurt's lastHurtByMob of this tick — ends it the same way; a hit that stores nothing keeps the pick transient (ENT-S-129)
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

        if (!this.isInWater() && this.random.nextInt(10) == 0) {
            this.closestWaterDistanceSq = NO_WATER_FOUND_SENTINEL;
            this.targetX = 0;
            this.targetY = 0;
            this.targetZ = 0;
            for (int i = 1; i < 12; ++i) {
                int j = Math.min(i, 5);
                if (this.scanForWater((int) this.getX(), (int) this.getY() - 1, (int) this.getZ(), i, j, i)) break;
                if (i >= 5) ++i;
            }
            if (this.closestWaterDistanceSq < NO_WATER_FOUND_SENTINEL) {
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

        if (this.random.nextInt(8) == 1) {
            // orig Irukandji.java:291-293 — findSomethingToAttack answers null under PlayNicely ahead of its stored-target
            // read (:299-302) and its scan (:304-309); the port's pick is this inline block, gated as a whole (ENT-S-115).
            boolean playNicely = OreSpawnConfig.PLAY_NICELY.get();
            LivingEntity target = playNicely ? null : this.getTarget();      // orig :299 — the stored target, read inside the gated method
            if (target != null && target == this.scanPick) {
                target = null;                                               // orig :304-309 — the scan's pick was never stored: its occupant is re-derived by this pass (ENT-S-129)
            } else if (target != null && !target.isAlive()) {
                this.setTarget(null);                                        // orig :300-303 — a live stored target is answered ahead of the scan; a dead one is cleared (ENT-S-129)
                target = null;
            }
            if (target == null && !playNicely) {
                target = this.findSomethingToAttack();                       // orig :294-295, :304-309 — the 6/4/6 box, sorted, the first the filter accepts (ENT-S-135); HEAD's getNearestPlayer(this, 6.0) was a sphere
                // the scan's answer handed to the slot under the ownership mark — re-derived next pass, cleared when not
                // found again (ENT-S-129, the ENT-S-108 slot rule)
                if (target != this.getTarget()) this.setTarget(target);
                this.scanPick = this.getTarget();
            }
            if (target != null && target.isAlive()) {
                if (this.distanceToSqr(target) < 3.0) {
                    this.setAttacking(1);
                    if (this.random.nextInt(4) == 0 || this.random.nextInt(5) == 1) { // orig Irukandji.java:258 — nextInt(4) == 0 || nextInt(5) == 1: two dice, the second drawn only when the first misses (ENT-S-141)
                        this.doHurtTarget(target);
                    }
                } else {
                    this.getNavigation().moveTo(target, 1.2);
                }
            } else {
                this.setAttacking(0);
            }
        }
    }

    /**
     * orig Irukandji.java:290-310 {@code findSomethingToAttack}: nothing under PlayNicely (:291-293); every
     * {@code EntityLivingBase} whose box meets the jelly's box grown by 6/4/6 (:294, {@code getEntitiesWithinAABB} — a BOX,
     * where HEAD's {@code getNearestPlayer(this, 6.0)} was a sphere of 6 from the position: a player at a box corner, ~8 blocks
     * off, is prey; one 5 blocks straight up is not); sorted by the {@link GenericTargetSorter} (:295); the first the filter
     * accepts wins (:304-309), else null (:310). {@link TargetSelection#firstMatch} is that sort-and-loop, stable ties included
     * (OPT-021). Orig's stored-target read between the sort and the loop (:299-303) is the pass's, ahead of this call
     * (ENT-S-129). ENT-S-135.
     */
    @Nullable
    private LivingEntity findSomethingToAttack() {
        if (OreSpawnConfig.PLAY_NICELY.get()) return null;                        // orig :291-293
        List<LivingEntity> candidates = this.level().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(6.0, 4.0, 6.0));                    // orig :294
        return TargetSelection.firstMatch(candidates, this.targetSorter, this::isSuitableTarget); // orig :295, :304-309
    }

    /**
     * orig Irukandji.java:270-288 {@code isSuitableTarget}, in the original's order: null / self / dead (:271-279), line of
     * sight (:280-282, ENT-S-118), then the player branch — creative refused (:283-286, {@code isCreativeMode} =
     * {@code Abilities.instabuild}); nothing else is prey (:287). ENT-S-135.
     */
    private boolean isSuitableTarget(LivingEntity target) {
        if (target == null || target == this || !target.isAlive()) return false;    // orig :271-279
        if (!this.getSensing().hasLineOfSight(target)) return false;                // orig :280-282 — canSee, the eye-to-eye block ray, ahead of the creative check (ENT-S-118)
        if (target instanceof Player player) return !player.getAbilities().instabuild; // orig :283-286
        return false;                                                               // orig :287
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
        if (state.is(Blocks.WATER) && dist < this.closestWaterDistanceSq) {
            this.closestWaterDistanceSq = dist;
            this.targetX = x;
            this.targetY = y;
            this.targetZ = z;
            return 1;
        }
        return 0;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SND_LITTLE_SPLAT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SND_RATDEAD;
    }

    @Override
    protected float getSoundVolume() {
        return 0.25f;
    }

    /** orig Irukandji.java:326-337 — y>=50; daytime; 1-in-60 dice; at most 2 buddies within 16/8/16. */
    @Override
    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level,
                                   net.minecraft.world.entity.MobSpawnType spawnType) {
        if (this.getY() < 50.0) return false;
        if (!OriginalSpawnGates.isDaytime(level)) return false;
        if (this.random.nextInt(60) != 1) return false;
        return OriginalSpawnGates.countBuddies(this, level, Irukandji.class, 16.0, 8.0, 16.0) <= 2;
    }
}
