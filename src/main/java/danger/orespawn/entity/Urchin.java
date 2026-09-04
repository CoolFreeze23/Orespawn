package danger.orespawn.entity;

import danger.orespawn.MobStats;

import danger.orespawn.ModItems;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.ai.GenericTargetSorter;
import danger.orespawn.entity.ai.TargetSelection;
import danger.orespawn.util.MyUtils;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
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

public class Urchin extends Monster {
    // OPT-011: cached SoundEvents — identical createVariableRangeEvent ids,
    // allocated once per class instead of on every sound query.
    private static final SoundEvent SND_KYUUBI_LIVING = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "kyuubi_living"));
    private static final SoundEvent SND_GLASSHIT = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "glasshit"));
    private static final SoundEvent SND_GLASSDEAD = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "glassdead"));
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(Urchin.class, EntityDataSerializers.INT);

    private static final double MOVE_SPEED = 0.3;

    /**
     * orig Urchin.java:46 {@code was_spawnered} — set when the spawn check passes
     * via the "Crystal Urchin" spawner bypass (orig :312); spawnered Urchins skip
     * the far-away despawn (orig :87-92) and the daytime discard (orig :94-107).
     * Not persisted in the original either.
     */
    private int wasSpawnered = 0;

    /**
     * orig Urchin.java:43 {@code TargetSorter}, :55 {@code new GenericTargetSorter(this)} —
     * the shared weighted-distance order (creepers halved, big silhouettes first) the scan
     * sorts its candidates by (:277). ENT-S-108.
     */
    private final GenericTargetSorter targetSorter = new GenericTargetSorter(this);

    public Urchin(EntityType<? extends Urchin> type, Level level) {
        super(type, level);
        this.xpReward = 20;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MyEntityAIWanderALot(this, 14, 1.0));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
        // orig Urchin.java:61 — EntityAIHurtByTarget(this, false), whose attack target nothing in orig read: the pass
        // (:195-208) acted on the scan's pick alone, so the revenge task was inert. The port's pass reads the slot its
        // scan fills, so a registered revenge goal would chase the attacker where 1.7.10 never retaliated: not
        // registered (ENT-S-129; ENT-S-108's D2).
    }

    public static AttributeSupplier.Builder createAttributes() {
        // orig OreSpawnMain.java:6484 — Urchin 25 HP / 10 ATK / 4 armor
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MobStats.URCHIN.maxHealth())
                .add(Attributes.MOVEMENT_SPEED, MOVE_SPEED)
                .add(Attributes.ATTACK_DAMAGE, MobStats.URCHIN.attackDamage())
                .add(Attributes.ARMOR, MobStats.URCHIN.armor());
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
        target.igniteForSeconds(5);
        return super.doHurtTarget(target);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.type().msgId().equals("cactus")) return false;
        return super.hurt(source, amount);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.random.nextInt(3) == 1 && this.level().isClientSide) {
            this.level().addParticle(ParticleTypes.FLAME,
                    this.getX(), this.getY() + 0.75, this.getZ(),
                    0.0, this.random.nextFloat() / 10.0, 0.0);
            if (this.isInWater() && this.random.nextInt(5) == 1) {
                this.level().addParticle(ParticleTypes.SMOKE,
                        this.getX(), this.getY() + 1.75, this.getZ(),
                        0.0, this.random.nextFloat() / 10.0, 0.0);
                this.level().addParticle(ParticleTypes.LARGE_SMOKE,
                        this.getX(), this.getY() + 2.0, this.getZ(),
                        0.0, this.random.nextFloat() / 10.0, 0.0);
            }
        }
        if (this.isInWater() && this.random.nextInt(5) == 1 && !this.level().isClientSide) {
            this.doHurtTarget(this);
        }
    }

    @Override
    public void tick() {
        super.tick();
        // orig Urchin.java:94-107 — daytime discard skipped when spawnered
        long timeOfDay = this.level().getDayTime() % 24000L;
        if (timeOfDay < 12000L && this.random.nextInt(400) == 1 && !this.level().isClientSide
                && this.wasSpawnered == 0) {
            this.discard();
        }
    }

    /** orig Urchin.java:87-92 — spawnered Urchins never despawn from distance. */
    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return this.wasSpawnered == 0;
    }

    /**
     * orig Urchin.java:190-210 {@code updateAITasks}: nothing while dead (:191-193), super
     * (:194), then on the 1-in-8 tick (:195) the scan (:196, {@link #selectTarget}) and the
     * melee on its pick (:197-208: reach distSq &lt; 8, setAttacking, the swing dice, the
     * chase at 1.2). ENT-S-108: the port's players-only {@code getNearestPlayer(16)} scan is
     * gone; the 16/3/16 EntityLivingBase box scan of :272-288 stands in its place, through
     * the target slot this melee block reads.
     */
    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;            // orig Urchin.java:191-193
        super.customServerAiStep();              // orig :194

        if (this.random.nextInt(8) == 0) {       // orig :195
            this.selectTarget();                 // orig :196 findSomethingToAttack
            LivingEntity target = this.getTarget();
            if (target != null && target.isAlive()) {          // orig :197
                if (this.distanceToSqr(target) < 8.0) {        // orig :198
                    this.setAttacking(1);                      // orig :199
                    if (this.random.nextInt(7) == 0) {         // orig :200
                        this.doHurtTarget(target);             // orig :201
                    }
                } else {
                    this.getNavigation().moveTo(target, 1.2);  // orig :204
                }
            } else {
                this.setAttacking(0);                          // orig :207
            }
        }
    }

    /**
     * orig Urchin.java:196: on the cadence tick the prey is whatever {@link #findSomethingToAttack} returns right now —
     * the original kept no target of its own and read none (its :61 revenge task set an attack target nothing
     * consumed), so a candidate that had left the 16/3/16 box or line of sight was simply not found next time
     * (:206-208, setAttacking(0)). The port's single target slot feeds the melee goal, so every pass hands the scan's
     * answer to the slot — replaced, or cleared when the scan comes back empty — and nothing else fills it: the
     * ENT-S-108 ownership mark and the registered revenge goal are gone, because orig scanned every pass regardless
     * of any stored target and never retaliated (ENT-S-129).
     */
    private void selectTarget() {
        LivingEntity pick = this.findSomethingToAttack();           // orig :196
        if (pick != this.getTarget()) this.setTarget(pick);        // the slot refreshed for the goal: this pass's answer, or empty (ENT-S-129)
    }

    /**
     * orig Urchin.java:272-288 {@code findSomethingToAttack}: nothing under PlayNicely
     * (:273-275); every {@code EntityLivingBase} whose box meets the hunter's box grown by
     * 16/3/16 (:276, {@code getEntitiesWithinAABB} — players included, itself included);
     * sorted by the {@link GenericTargetSorter} (:277); the first the filter accepts wins
     * (:278-286), else null (:287). {@link TargetSelection#firstMatch} is that sort-and-loop,
     * stable ties included (OPT-021). ENT-S-108.
     */
    @Nullable
    private LivingEntity findSomethingToAttack() {
        if (OreSpawnConfig.PLAY_NICELY.get()) return null;                        // orig :273-275
        List<LivingEntity> candidates = this.level().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(16.0, 3.0, 16.0));                  // orig :276
        return TargetSelection.firstMatch(candidates, this.targetSorter, this::isSuitableTarget); // orig :277-287
    }

    /**
     * orig Urchin.java:220-270 {@code isSuitableTarget}, in the original's order: null / self /
     * dead (:221-229), the shared ignore screen (:230-232, ENT-S-106), line of sight
     * (:233-235), then the species chain — Vortex (:236-238), Rotator (:239-241), Peacock
     * (:242-244), CrystalCow (:245-247), Irukandji (:248-250), Skate (:251-253), Whale
     * (:254-256), Flounder (:257-259), Urchin (:260-262) — and the player branch, creative
     * refused (:263-268, {@code isCreativeMode} = {@code Abilities.instabuild}); everything
     * else that lives is prey (:269). ENT-S-108.
     */
    private boolean isSuitableTarget(LivingEntity target) {
        if (target == null || target == this || !target.isAlive()) return false;    // orig :221-229
        if (MyUtils.isIgnoreable(target)) return false;                             // orig :230-232
        if (!this.getSensing().hasLineOfSight(target)) return false;                // orig :233-235
        if (target instanceof EntityVortex) return false;                           // orig :236-238 Vortex
        if (target instanceof EntityRotator) return false;                          // orig :239-241 Rotator
        if (target instanceof Peacock) return false;                                // orig :242-244
        if (target instanceof CrystalCow) return false;                             // orig :245-247
        if (target instanceof Irukandji) return false;                              // orig :248-250
        if (target instanceof Skate) return false;                                  // orig :251-253
        if (target instanceof Whale) return false;                                  // orig :254-256
        if (target instanceof Flounder) return false;                               // orig :257-259
        if (target instanceof Urchin) return false;                                 // orig :260-262
        if (target instanceof Player player && player.getAbilities().instabuild) return false; // orig :263-268
        return true;                                                                // orig :269
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return SND_KYUUBI_LIVING;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SND_GLASSHIT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SND_GLASSDEAD;
    }

    @Override
    protected float getSoundVolume() {
        return 1.1f;
    }

    /**
     * orig Urchin.java:298-332 — "Crystal Urchin" spawner bypass (x/z -2..+2,
     * y +1..+3, sets {@code was_spawnered}); >=6 air blocks in the 3x3 ring one
     * above the feet; darkness; night half of the day only.
     */
    @Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnType) {
        if (OriginalSpawnGates.nearOwnSpawner(this, level, -2, 2, 1, 3)) {
            this.wasSpawnered = 1;
            return true;
        }
        int sc = 0;
        BlockPos feet = this.blockPosition();
        for (int dz = -1; dz <= 1; dz++) {
            for (int dx = -1; dx <= 1; dx++) {
                if (level.getBlockState(feet.offset(dx, 1, dz)).isAir()) sc++;
            }
        }
        if (sc < 6) return false;
        if (!OriginalSpawnGates.isDarkEnough(this, level)) return false;
        return level.dayTime() % 24000L >= 13000L;
    }
}
