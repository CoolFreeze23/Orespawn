package danger.orespawn.entity;

import danger.orespawn.MobStats;

import danger.orespawn.OreSpawnMod;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.entity.ai.GenericTargetSorter;
import danger.orespawn.entity.ai.TargetSelection;
import danger.orespawn.util.MyUtils;
import java.util.List;
import javax.annotation.Nullable;
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
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

public class Hammerhead extends Monster {
    // OPT-011: cached SoundEvents — identical createVariableRangeEvent ids,
    // allocated once per class instead of on every sound query.
    private static final SoundEvent SND_HAMMERHEAD_LIVING = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "hammerhead_living"));
    private static final SoundEvent SND_ALO_HURT = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "alo_hurt"));
    private static final SoundEvent SND_HAMMERHEAD_DEATH = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "hammerhead_death"));
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(Hammerhead.class, EntityDataSerializers.INT);

    private static final double MOVE_SPEED = 0.35;

    // No boss bar: the orig Hammerhead.java is a plain EntityMob with no
    // BossStatus hooks — the port's ServerBossEvent was an invention (ENT-D-055).

    private LivingEntity revengeTarget = null;

    /**
     * orig Hammerhead.java:38 {@code TargetSorter}, :48 {@code new GenericTargetSorter(this)} — the shared weighted-distance
     * order (creepers halved, big silhouettes first) the scan sorts its candidates by (:256; the ledger's T4 row, closed by the
     * sorter here). ENT-S-135.
     */
    private final GenericTargetSorter targetSorter = new GenericTargetSorter(this);

    public Hammerhead(EntityType<? extends Hammerhead> type, Level level) {
        super(type, level);
        this.xpReward = 350;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MyEntityAIWanderALot(this, 16, 1.0));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Mob.class, 8.0f));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        // orig Hammerhead.java:54 — EntityAIHurtByTarget(this, false), whose attack target nothing in orig read: the pass
        // (:194-221) consumed rt (:182-184) alone. The port's pass reads the slot as a fallback (ENT-S-115, refuter B2),
        // so a registered revenge goal would re-supply an attacker rt had forgotten (:198-201) until vanilla's own
        // release: not registered — in play the slot stays empty and the fallback finds nothing (ENT-S-129).
    }

    public static AttributeSupplier.Builder createAttributes() {
        // orig OreSpawnMain.java:6477 — Hammerhead 240 HP / 75 ATK / 20 armor;
        // speed 0.35 matches orig Hammerhead.java:39.
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MobStats.HAMMERHEAD.maxHealth())
                .add(Attributes.MOVEMENT_SPEED, MOVE_SPEED)
                .add(Attributes.ATTACK_DAMAGE, MobStats.HAMMERHEAD.attackDamage())
                .add(Attributes.ARMOR, MobStats.HAMMERHEAD.armor());
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
    public boolean doHurtTarget(Entity target) {
        if (super.doHurtTarget(target)) {
            if (target instanceof LivingEntity living) {
                double knockbackHorizontal = 1.1;
                double knockbackVertical = 0.85;
                float pushAngle = (float) Math.atan2(target.getZ() - this.getZ(), target.getX() - this.getX());
                if (target instanceof Player) knockbackVertical *= 2.0;
                target.push(Math.cos(pushAngle) * knockbackHorizontal, knockbackVertical, Math.sin(pushAngle) * knockbackHorizontal);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.type().msgId().equals("cactus")) return false;
        boolean ret = super.hurt(source, amount);
        Entity attacker = source.getEntity();
        if (attacker instanceof LivingEntity living) {
            this.revengeTarget = living;
        }
        return ret;
    }

    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;
        super.customServerAiStep();

        if (this.random.nextInt(3) == 1) {
            boolean playNicely = OreSpawnConfig.PLAY_NICELY.get();
            LivingEntity currentTarget = this.revengeTarget;
            if (playNicely) currentTarget = null; // orig Hammerhead.java:194-196 — `e = null`: the pass's copy of the revenge target is blanked, `rt` itself kept (ENT-S-115)
            if (currentTarget != null) {
                if (!currentTarget.isAlive() || this.random.nextInt(250) == 1) {
                    currentTarget = null;
                    this.revengeTarget = null;
                }
                if (currentTarget != null && !this.getSensing().hasLineOfSight(currentTarget)) currentTarget = null; // orig Hammerhead.java:203-205 — rt out of sight is skipped for the pass and kept (ENT-S-129)
            }
            if (currentTarget == null && !playNicely) { // port-only read of the slot (HurtByTargetGoal's channel): under the flag orig's pass consulted nothing (:194-209) and set attacking 0 (:219-221), so the read is gated with the pass; the slot itself is untouched (ENT-S-115, refuter B2)
                currentTarget = this.getTarget();
            }
            if (currentTarget == null) {                                            // orig Hammerhead.java:207-209
                currentTarget = this.findSomethingToAttack();                       // orig :208 — the 18/9/18 living box, sorted, the first the filter accepts; null under PlayNicely inside (:252-254, ENT-S-115); HEAD's getNearestPlayer(this, 18.0) was a players-only sphere (ENT-S-135)
            }
            if (currentTarget != null && currentTarget.isAlive()) {
                this.lookAt(currentTarget, 10.0f, 10.0f);
                double distSq = this.distanceToSqr(currentTarget);
                double meleeRangeSq = (7.0 + currentTarget.getBbWidth() / 2.0) * (7.0 + currentTarget.getBbWidth() / 2.0);
                if (distSq < meleeRangeSq) {
                    this.setAttacking(1);
                    if (this.random.nextInt(3) == 1 || this.random.nextInt(4) == 1) { // orig Hammerhead.java:213 — nextInt(3) == 1 || nextInt(4) == 1: two dice, the second drawn only when the first misses (ENT-S-141)
                        this.doHurtTarget(currentTarget);
                    }
                } else {
                    this.getNavigation().moveTo(currentTarget, 1.25);
                }
            } else {
                this.setAttacking(0);
            }
        }
    }

    /**
     * orig Hammerhead.java:251-268 {@code findSomethingToAttack}: nothing under PlayNicely (:252-254); every
     * {@code EntityLivingBase} whose box meets the shark's box grown by 18/9/18 (:255, {@code getEntitiesWithinAABB} —
     * every living thing, where HEAD's {@code getNearestPlayer(this, 18.0)} scanned players only, and a BOX where that was a
     * sphere of 18 from the position); sorted by the {@link GenericTargetSorter} (:256); the first the filter accepts wins
     * (:257-266), else null (:267). {@link TargetSelection#firstMatch} is that sort-and-loop, stable ties included (OPT-021).
     * The pick is the pass's for that tick alone, never stored (:207-218), as at HEAD. ENT-S-135.
     */
    @Nullable
    private LivingEntity findSomethingToAttack() {
        if (OreSpawnConfig.PLAY_NICELY.get()) return null;                        // orig :252-254
        List<LivingEntity> candidates = this.level().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(18.0, 9.0, 18.0));                  // orig :255
        return TargetSelection.firstMatch(candidates, this.targetSorter, this::isSuitableTarget); // orig :256-266
    }

    /**
     * orig Hammerhead.java:225-249 {@code isSuitableTarget}, in the original's order: null / self / dead (:226-234), line of
     * sight (:235-237), a Hammerhead refused (:238-240), then the player branch — creative refused (:241-244,
     * {@code isCreativeMode} = {@code Abilities.instabuild}) — any {@code EntityMob} taken (:245-247, the port's
     * {@code Monster}), and the shared attackable-non-mob grant list last (:248, {@link MyUtils#isAttackableNonMob}, orig's
     * membership since ENT-S-128). ENT-S-135.
     */
    private boolean isSuitableTarget(LivingEntity target) {
        if (target == null || target == this || !target.isAlive()) return false;    // orig :226-234
        if (!this.getSensing().hasLineOfSight(target)) return false;                // orig :235-237 — canSee, the eye-to-eye block ray
        if (target instanceof Hammerhead) return false;                             // orig :238-240
        if (target instanceof Player player) return !player.getAbilities().instabuild; // orig :241-244
        if (target instanceof Monster) return true;                                 // orig :245-247 (EntityMob)
        return MyUtils.isAttackableNonMob(target);                                  // orig :248
    }

    // Death drops are fully data-driven via loot_table/entities/hammerhead.json
    // (orig Hammerhead.java:126-149: 8 xp bottle, 10 experience catcher,
    // 16 creeper launcher, 4 creeper repellent, 6 raw beef, 2 experience
    // tree seed; the 1/3 "MyHammy" drop is a MISSING-ITEM in the port).

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        if (this.random.nextInt(3) == 0) {
            return SND_HAMMERHEAD_LIVING;
        }
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SND_ALO_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SND_HAMMERHEAD_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 1.2f;
    }

    /** orig Hammerhead.java:277-316 — "Hammerhead" spawner bypass; darkness; y>=50; night; clear-air column; no other Hammerhead within 16/8/16. */
    @Override
    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level,
                                   net.minecraft.world.entity.MobSpawnType spawnType) {
        if (OriginalSpawnGates.nearOwnSpawner(this, level)) return true;
        if (!OriginalSpawnGates.isDarkEnough(this, level)) return false;
        if (this.getY() < 50.0) return false;
        if (OriginalSpawnGates.isDaytime(level)) return false;
        if (!OriginalSpawnGates.airBox(this, level, -1, 0, 1, 5, -1, 0)) return false;
        return !OriginalSpawnGates.anyOtherNearby(this, level, Hammerhead.class, 16.0, 8.0, 16.0);
    }
}
