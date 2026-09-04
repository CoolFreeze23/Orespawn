package danger.orespawn.entity;

import danger.orespawn.entity.pose.Robot2Pose;
import danger.orespawn.MobStats;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.entity.ai.GenericTargetSorter;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.resources.ResourceLocation;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.ai.TargetSelection;
import danger.orespawn.util.MyUtils;

/**
 * Robot2 — the Robo-Pounder (orig Robot2.java:416 spawner tag).
 *
 * Heavy front-line melee chassis (200 HP / 22 ATK / 18 armor, orig
 * OreSpawnMain.java:6495) whose signature move is terrain destruction:
 * a landed swing rips 6 blocks out from under the target
 * (orig Robot2.java:304-309) and every in-range attack tick shreds a
 * ±6.5 x/z, +8.5 y envelope of terrain around the robot itself
 * (orig :263-272, :310), all gated on the mobGriefing gamerule and
 * PlayNicely-off. While idle it can also throw a 50-tick "just for fun"
 * tantrum, tearing up scenery with no target at all (orig :320-335).
 * ENT-K-063: this griefing had been relocated to the port's Robot4 —
 * restored here to keep each robot's original identity. Registry ID
 * kept as "robot_2" for save compat.
 */
public class Robot2 extends Monster implements Robot2Pose {
    // OPT-011: cached SoundEvents — identical createVariableRangeEvent ids,
    // allocated once per class instead of on every sound query.
    private static final SoundEvent SND_ROBOT_LIVING = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "robot_living"));
    private static final SoundEvent SND_ROBOT_HURT = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "robot_hurt"));
    private static final SoundEvent SND_ROBOT_DEATH = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "robot_death"));
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(Robot2.class, EntityDataSerializers.INT);

    // TF-035: orig Robot2.java:38,50 — targets sort with GenericTargetSorter
    // (creeper-halved / big-silhouette-first), not plain distance.
    private final GenericTargetSorter targetSorter;
    /** orig Robot2.java:40 {@code just_for_fun} — idle-tantrum countdown. */
    private int justForFun = 0;
    private final float moveSpeed = 0.3f;

    /**
     * Per-entity render scratch (orig Robot2.java:39 {@code renderdata = new RenderInfo()}).
     * {@code ri1} selects which arm(s) windmill while attacking
     * (orig ModelRobot2.java:142-170); mutated client-side by the model.
     */
    private final danger.orespawn.entity.client.RenderInfo renderInfo =
            new danger.orespawn.entity.client.RenderInfo();

    /** Mirrors orig Robot2.java {@code getRenderInfo()}. */
    public danger.orespawn.entity.client.RenderInfo getRenderInfo() {
        return this.renderInfo;
    }

    public Robot2(EntityType<? extends Robot2> type, Level level) {
        super(type, level);
        this.xpReward = 100;
        this.targetSorter = new GenericTargetSorter(this);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 10.0f));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        // orig Robot2.java:57 — EntityAIHurtByTarget(this, false): no call for help and no same-kind exemption (a Robot2
        // attacker is stored like any other, :338-351); the port-only setAlertOthers (every Robot2 within FOLLOW_RANGE x 10
        // with no target retargeted) and the Robot2.class damage exemption are gone (ENT-S-129)
        this.revengeGoal = new RevengeGoal();
        this.targetSelector.addGoal(1, this.revengeGoal); // released by the pass's 1-in-50, as orig's task ended on a nulled attack target (ENT-S-129 refuter A)
    }

    /** orig Robot2.java:57 {@code EntityAIHurtByTarget(this, false)} — the revenge task the pass's 1-in-50 (:281-283) ended by nulling the attack target. ENT-S-129. */
    private RevengeGoal revengeGoal;

    /**
     * 1.7.10's {@code EntityAIHurtByTarget} ended when the attack target was nulled ({@code EntityAITarget.continueExecuting});
     * vanilla's {@code TargetGoal} re-asserts its own memory into an emptied slot, so the pass's release also drops that
     * memory ({@link #release}). The hold itself stays vanilla's. ENT-S-129 (refuter A).
     */
    private final class RevengeGoal extends HurtByTargetGoal {
        RevengeGoal() {
            super(Robot2.this);
        }

        void release() {
            this.targetMob = null;
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        // orig OreSpawnMain.java:6495 — Robot2 200 HP / 22 ATK / 18 armor
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MobStats.ROBOT2.maxHealth())
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ATTACK_DAMAGE, MobStats.ROBOT2.attackDamage())
                .add(Attributes.ARMOR, MobStats.ROBOT2.armor());
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
    public void jumpFromGround() {
        Vec3 velocity = this.getDeltaMovement();
        this.setDeltaMovement(velocity.x, velocity.y + 0.25, velocity.z);
        super.jumpFromGround();
    }

    /**
     * ENT-K-063: orig Robot2.java:232-261 — pull one random block out from
     * under the target's feet (x/z jittered by two nextFloat rolls, y - 1).
     * Obsidian, bedrock, quartz block, spawners, redstone blocks, iron
     * blocks and chests are spared; anything else is set to air with NO
     * drops, gated on the mobGriefing gamerule. Coordinates truncate with
     * an (int) cast exactly as the original did, so at negative coords the
     * probe lands one block off — kept bug-for-bug.
     */
    private void destroyBlock(LivingEntity target) {
        double x = target.getX() + this.getRandom().nextFloat() - this.getRandom().nextFloat();
        double y = target.getY() - 1.0;
        double z = target.getZ() + this.getRandom().nextFloat() - this.getRandom().nextFloat();
        BlockPos pos = new BlockPos((int) x, (int) y, (int) z);
        var state = this.level().getBlockState(pos);
        Block block = state.getBlock();
        // orig Robot2.java:237-257 — protected-block early returns.
        if (block == Blocks.OBSIDIAN || block == Blocks.BEDROCK
                || block == Blocks.QUARTZ_BLOCK || block == Blocks.SPAWNER
                || block == Blocks.REDSTONE_BLOCK || block == Blocks.IRON_BLOCK
                || block == Blocks.CHEST) {
            return;
        }
        // orig Robot2.java:258-260 — non-air + mobGriefing → replaced by air.
        if (!state.isAir() && this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
            this.level().setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        }
    }

    /**
     * ENT-K-063: orig Robot2.java:263-272 — 50 random probes in a ±6.5 x/z,
     * +0.1..+8.6 y envelope around the Pounder; each probe on a non-air,
     * non-protected block vaporizes it (no drops), mobGriefing-gated. Same
     * protected list and (int)-truncation quirk as {@link #destroyBlock}.
     */
    private void destroyNearbyBlocks() {
        for (int i = 0; i < 50; ++i) {
            double x = this.getX() + this.getRandom().nextFloat() * 6.5 - this.getRandom().nextFloat() * 6.5;
            double y = this.getY() + 0.1 + this.getRandom().nextFloat() * 8.5;
            double z = this.getZ() + this.getRandom().nextFloat() * 6.5 - this.getRandom().nextFloat() * 6.5;
            BlockPos pos = new BlockPos((int) x, (int) y, (int) z);
            var state = this.level().getBlockState(pos);
            Block block = state.getBlock();
            if (block == Blocks.OBSIDIAN || block == Blocks.BEDROCK
                    || block == Blocks.QUARTZ_BLOCK || block == Blocks.SPAWNER
                    || block == Blocks.REDSTONE_BLOCK || block == Blocks.IRON_BLOCK
                    || block == Blocks.CHEST || state.isAir()
                    || !this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
                continue;
            }
            this.level().setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        }
    }

    /** ENT-K-063: orig Robot2.java:274-336 — attack + griefing think loop. */
    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;
        super.customServerAiStep();
        // orig :279 — think-tick runs on a 1-in-6 roll, PlayNicely off only.
        if (this.getRandom().nextInt(6) == 1 && !OreSpawnConfig.PLAY_NICELY.get()) {
            LivingEntity target = this.getTarget();
            if (this.getRandom().nextInt(50) == 1) { this.setTarget(null); this.revengeGoal.release(); } // orig :281-283 — the task ended on the nulled target; vanilla's TargetGoal would re-assert its memory (ENT-S-129 refuter A)
            if (target != null && !target.isAlive()) { this.setTarget(null); target = null; }
            if (target == null) target = findSomethingToAttack();
            if (target != null) {
                // orig :292-299 — swings/griefs only when the target sits
                // within 1.25 rad of the body facing (yRot + 90).
                double rr = Math.atan2(target.getZ() - this.getZ(), target.getX() - this.getX());
                double rhdir = Math.toRadians((this.getYRot() + 90.0f) % 360.0f);
                double pi = 3.1415926545; // orig :294 — truncated pi constant
                double rdd = Math.abs(rr - rhdir) % (pi * 2.0);
                if (rdd > pi) rdd -= pi * 2.0;
                rdd = Math.abs(rdd);
                this.lookAt(target, 10.0f, 10.0f); // orig :300
                if (rdd < 1.25) {
                    double meleeRange = 5.0f + target.getBbWidth() / 2.0f;
                    if (this.distanceToSqr(target) < meleeRange * meleeRange) {
                        this.setAttacking(1);
                        // orig :304-309 — two swing dice; a landed swing also
                        // rips 6 blocks out from under the target.
                        if (this.getRandom().nextInt(5) == 0 || this.getRandom().nextInt(6) == 1) {
                            this.doHurtTarget(target);
                            for (int i = 0; i < 6; ++i) {
                                this.destroyBlock(target);
                            }
                        }
                        // orig :310 — every in-range think-tick tears up the
                        // surrounding terrain, whether or not the swing landed.
                        this.destroyNearbyBlocks();
                    }
                } else {
                    this.setAttacking(0);
                }
                this.getNavigation().moveTo(target, 1.0);
            } else {
                this.setAttacking(0);
            }
        }
        // orig :320-335 — "just for fun": while idle (PlayNicely off) a
        // 1-in-450 roll starts a 50-tick tantrum; the Pounder holds its
        // attack pose and shreds nearby terrain on a 1-in-3 roll per tick.
        if (this.getAttacking() == 0 && !OreSpawnConfig.PLAY_NICELY.get()) {
            if (this.getRandom().nextInt(450) == 1) this.justForFun = 50;
            if (this.justForFun > 0) --this.justForFun;
            if (this.justForFun > 0) {
                this.setAttacking(1);
                if (this.getRandom().nextInt(3) == 1) {
                    this.destroyNearbyBlocks();
                }
            } else {
                this.setAttacking(0);
            }
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getMsgId().equals("cactus")) return false;
        boolean ret = super.hurt(source, amount);
        Entity attacker = source.getEntity();
        if (attacker instanceof Mob mob) {
            this.setTarget(mob);
            this.getNavigation().moveTo(mob, 1.2);
        }
        return ret;
    }

    private LivingEntity findSomethingToAttack() {
        // orig Robot2.java:382-384 — PlayNicely disables acquisition entirely.
        if (OreSpawnConfig.PLAY_NICELY.get()) return null;
        AABB searchBox = this.getBoundingBox().inflate(14.0, 3.0, 14.0);
        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class, searchBox);
        // OPT-021: nearest-first pick without the full list sort; TargetSelection
        // preserves the removed sort's order and stable-tie semantics exactly.
        // (was: .sort orig :386 — GenericTargetSorter)
        return TargetSelection.firstMatch(entities, this.targetSorter, this::isSuitableTarget);
    }

    private boolean isSuitableTarget(LivingEntity target) {
        if (target == null || target == this || !target.isAlive()) return false;
        if (MyUtils.isIgnoreable(target)) return false; // orig Robot2.java:363-365 — the shared ignore screen (ENT-S-106)
        if (!this.getSensing().hasLineOfSight(target)) return false; // orig Robot2.java:366-368 — canSee, after the ignore screen and ahead of the EntityMob refusal (:369) (ENT-S-118)
        if (target instanceof Monster) return false;
        if (target instanceof Player p && p.getAbilities().instabuild) return false;
        return true;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        if (this.getRandom().nextInt(4) == 0)
            return SND_ROBOT_LIVING;
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource ds) {
        return SND_ROBOT_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SND_ROBOT_DEATH;
    }

    /** orig Robot2.java:403-437 — "Robo-Pounder" spawner bypass; y>=50; night; air/short-grass clearance above; darkness. */
    @Override
    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level,
                                   net.minecraft.world.entity.MobSpawnType spawnType) {
        if (OriginalSpawnGates.nearOwnSpawner(this, level)) return true;
        if (this.getY() < 50.0) return false;
        if (OriginalSpawnGates.isDaytime(level)) return false;
        if (!OriginalSpawnGates.boxMatches(this, level, -1, 1, 1, 5, -1, 0,
                s -> s.isAir() || s.is(net.minecraft.world.level.block.Blocks.SHORT_GRASS))) return false;
        return OriginalSpawnGates.isDarkEnough(this, level);
    }
}
