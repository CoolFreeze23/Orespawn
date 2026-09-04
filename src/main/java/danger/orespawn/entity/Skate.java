package danger.orespawn.entity;

import danger.orespawn.MobStats;

import danger.orespawn.OreSpawnMod;
import danger.orespawn.OreSpawnConfig;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class Skate extends Monster {
    // OPT-011: cached SoundEvents — identical createVariableRangeEvent ids,
    // allocated once per class instead of on every sound query.
    private static final SoundEvent SND_RATDEAD = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "ratdead"));
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(Skate.class, EntityDataSerializers.INT);

    private static final double MOVE_SPEED = 0.25;

    private int closestWaterDistance = 99999;
    private int targetX = 0, targetY = 0, targetZ = 0;

    /**
     * The last pick the pass handed to the target slot. 1.7.10 stored the scan's pick nowhere — the pass acted on it
     * for that tick and re-derived it on the next (orig Skate.java:296-301); only a target stored by {@link #hurt} or the revenge
     * task persisted, answered ahead of the scan while alive (:291-294). The port stores the pick so the next pass
     * can tell its own occupant from a stored one: under this mark it is re-derived every pass (replaced, or cleared
     * when not found again), never sticky; a target set by any other path is left alone (the ENT-S-108 slot rule;
     * see {@link #setTarget}). ENT-S-129.
     */
    @Nullable
    private LivingEntity scanPick;

    public Skate(EntityType<? extends Skate> type, Level level) {
        super(type, level);
        this.xpReward = 10;
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
        // orig OreSpawnMain.java:6519 — Skate 8 HP / 8 ATK / 4 armor
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MobStats.SKATE.maxHealth())
                .add(Attributes.MOVEMENT_SPEED, MOVE_SPEED)
                .add(Attributes.ATTACK_DAMAGE, MobStats.SKATE.attackDamage())
                .add(Attributes.ARMOR, MobStats.SKATE.armor());
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
        Entity attacker = source.getEntity();
        if (attacker instanceof Skate) return false;
        if (attacker instanceof Mob mob) {
            if (mob == this.scanPick) this.scanPick = null; // orig Skate.java:138-145 — the attacker is the STORED target from here (read ahead of the scan, :291-294): the scan's mark on a pick that turned on the skate ends with the store (ENT-S-129)
            this.setTarget(mob);
            this.getNavigation().moveTo(mob, 1.2);
        }
        boolean ret = super.hurt(source, amount);
        if (attacker != null && attacker == this.scanPick && this.getLastHurtByMob() == attacker
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
            this.closestWaterDistance = 99999;
            this.targetX = 0; this.targetY = 0; this.targetZ = 0;
            for (int i = 1; i < 12; ++i) {
                int j = Math.min(i, 5);
                if (this.scanForWater((int) this.getX(), (int) this.getY() - 1, (int) this.getZ(), i, j, i)) break;
                if (i >= 5) ++i;
            }
            if (this.closestWaterDistance < 99999) {
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
            // orig Skate.java:283-285 — findSomethingToAttack answers null under PlayNicely ahead of its stored-target
            // read (:291-294) and its scan (:296-301); the port's pick is this inline block, gated as a whole (ENT-S-115).
            boolean playNicely = OreSpawnConfig.PLAY_NICELY.get();
            LivingEntity target = playNicely ? null : this.getTarget();      // orig :291 — the stored target, read inside the gated method
            if (target != null && target == this.scanPick) {
                target = null;                                               // orig :296-301 — the scan's pick was never stored: its occupant is re-derived by this pass (ENT-S-129)
            } else if (target != null && !target.isAlive()) {
                this.setTarget(null);                                        // orig :292-295 — a live stored target is answered ahead of the scan; a dead one is cleared (ENT-S-129)
                target = null;
            }
            if (target == null && !playNicely) {
                Player nearest = this.level().getNearestPlayer(this, 10.0);
                // orig Skate.java:272-274 — canSee, the eye-to-eye block ray, ahead of the creative check (:275-278) (ENT-S-118)
                if (nearest != null && this.getSensing().hasLineOfSight(nearest) && !nearest.getAbilities().instabuild) {
                    target = nearest;
                }
                // the scan's answer handed to the slot under the ownership mark — re-derived next pass, cleared when not
                // found again (ENT-S-129, the ENT-S-108 slot rule)
                if (target != this.getTarget()) this.setTarget(target);
                this.scanPick = this.getTarget();
            }
            if (target != null && target.isAlive()) {
                if (this.distanceToSqr(target) < 4.0) {
                    this.setAttacking(1);
                    if (this.random.nextInt(4) == 0) {
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

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() { return null; }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) { return null; }

    @Override
    protected SoundEvent getDeathSound() {
        return SND_RATDEAD;
    }

    @Override
    protected float getSoundVolume() { return 0.33f; }

    /** orig Skate.java:318-329 — y>=50; daytime; 1-in-30 dice; at most 6 buddies within 16/8/16. */
    @Override
    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level,
                                   net.minecraft.world.entity.MobSpawnType spawnType) {
        if (this.getY() < 50.0) return false;
        if (!OriginalSpawnGates.isDaytime(level)) return false;
        if (this.random.nextInt(30) != 1) return false;
        return OriginalSpawnGates.countBuddies(this, level, Skate.class, 16.0, 8.0, 16.0) <= 6;
    }
}
