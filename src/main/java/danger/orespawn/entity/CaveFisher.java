package danger.orespawn.entity;

import danger.orespawn.MobStats;

import danger.orespawn.OreSpawnConfig;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.ai.BugMeleeAttackGoal;
import danger.orespawn.entity.ai.GenericTargetSorter;
import danger.orespawn.entity.ai.TargetSelection;
import danger.orespawn.entity.pose.CaveFisherPose;
import danger.orespawn.util.MyUtils;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
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

public class CaveFisher extends Monster implements CaveFisherPose {
    // OPT-011: cached SoundEvents — identical createVariableRangeEvent ids,
    // allocated once per class instead of on every sound query.
    private static final SoundEvent SND_CRYO_HURT = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "cryo_hurt"));
    private static final SoundEvent SND_CRYO_DEATH = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "cryo_death"));

    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(CaveFisher.class, EntityDataSerializers.INT);

    private static final double MOVE_SPEED = 0.2;
    /**
     * Cave fishers are cave-dwellers — the 1.7.10 mod restricted their
     * natural spawning to Y <= 50. We keep that gate in
     * {@link #checkSpawnRules(LevelAccessor, MobSpawnType)}; the biome
     * modifier JSON lets them appear in any overworld biome but
     * the spawn-location logic enforces the low-Y restriction.
     */
    private static final double MAX_NATURAL_SPAWN_Y = 50.0;

    /**
     * Per-entity render scratch (orig CaveFisher.java:39 {@code renderdata = new RenderInfo()},
     * re-created :50, zeroed in entityInit :68-78; accessor orig :94-96). Mutated
     * client-side by {@code ModelCaveFisher} for the claw-snap latch
     * (orig ModelCaveFisher.java:593-613); never datawatcher-synced. ENT-S-093.
     */
    private final danger.orespawn.entity.client.RenderInfo renderInfo =
            new danger.orespawn.entity.client.RenderInfo();

    /**
     * orig CaveFisher.java:38 {@code TargetSorter}, :49 {@code new GenericTargetSorter(this)} —
     * the shared weighted-distance order (creepers halved, big silhouettes first) the scan
     * sorts its candidates by (:235). ENT-S-108.
     */
    private final GenericTargetSorter targetSorter = new GenericTargetSorter(this);

    public CaveFisher(EntityType<? extends CaveFisher> type, Level level) {
        super(type, level);
        this.xpReward = 10;
    }

    @Override
    protected void registerGoals() {
        // orig CaveFisher.java:51-55 — swim, wander(14), watch-player(8),
        // look-idle; no ceiling/ambush behavior exists in the original
        // (its :163-183 AI is a flat 1-in-8 ground scan handled by
        // BugMeleeAttackGoal.Params.caveFisher()).
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new BugMeleeAttackGoal(
                this, this::setAttacking, BugMeleeAttackGoal.Params.caveFisher()));
        this.goalSelector.addGoal(2, new MyEntityAIWanderALot(this, 14, 1.0));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        // orig CaveFisher.java:55 — EntityAIHurtByTarget(this, false), whose attack target nothing in orig read: the pass
        // (:168-181) acted on the scan's pick alone, so the revenge task was inert. The port's slot is read by the melee
        // goal every tick, so a registered revenge goal would chase the attacker where 1.7.10 never retaliated: not
        // registered (ENT-S-129; ENT-S-108's D2).
        // orig CaveFisher.java:55 registers no target-search task: prey is found by the
        // 1-in-8 EntityLivingBase box scan of :168-169 / :230-246, restored in
        // customServerAiStep / findSomethingToAttack (ENT-S-108). The port's
        // NearestAttackableTargetGoal<Player> and <Animal> pair (which missed water
        // animals, ambient creatures, villagers, golems and OreSpawn's own non-Monster
        // species, and ranked by plain distance) is gone.
    }

    public static AttributeSupplier.Builder createAttributes() {
        // orig OreSpawnMain.java:6511 — CaveFisher 10 HP / 4 ATK / 4 armor
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MobStats.CAVE_FISHER.maxHealth())
                .add(Attributes.MOVEMENT_SPEED, MOVE_SPEED)
                .add(Attributes.ATTACK_DAMAGE, MobStats.CAVE_FISHER.attackDamage())
                .add(Attributes.ARMOR, MobStats.CAVE_FISHER.armor())
                .add(Attributes.FOLLOW_RANGE, 16.0);
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

    /** Mirrors orig CaveFisher.java:94-96 {@code getRenderInfo()}. ENT-S-093. */
    @Override
    public danger.orespawn.entity.client.RenderInfo getRenderInfo() {
        return this.renderInfo;
    }

    /**
     * orig CaveFisher.java:163-183 {@code updateAITasks}: nothing while dead (:164-166),
     * super (:167), then on the 1-in-8 tick (:168) the scan (:169). The rest of that block
     * (:170-181: reach distSq &lt; 8, the nextInt(7)==0 || nextInt(8)==1 swing, the chase at
     * 1.2, setAttacking) is {@code BugMeleeAttackGoal.Params.caveFisher()}, fed through the
     * target slot by {@link #selectTarget}. ENT-S-108.
     */
    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;            // orig CaveFisher.java:164-166
        super.customServerAiStep();              // orig :167
        if (this.random.nextInt(8) == 0) {       // orig :168
            this.selectTarget();                 // orig :169 findSomethingToAttack
        }
    }

    /**
     * orig CaveFisher.java:169: on the cadence tick the prey is whatever {@link #findSomethingToAttack} returns right now —
     * the original kept no target of its own and read none (its :55 revenge task set an attack target nothing
     * consumed), so a candidate that had left the 10/3/10 box or line of sight was simply not found next time
     * (:179-181, setAttacking(0)). The port's single target slot feeds the melee goal, so every pass hands the scan's
     * answer to the slot — replaced, or cleared when the scan comes back empty — and nothing else fills it: the
     * ENT-S-108 ownership mark and the registered revenge goal are gone, because orig scanned every pass regardless
     * of any stored target and never retaliated (ENT-S-129).
     */
    private void selectTarget() {
        LivingEntity pick = this.findSomethingToAttack();           // orig :169
        if (pick != this.getTarget()) this.setTarget(pick);        // the slot refreshed for the goal: this pass's answer, or empty (ENT-S-129)
    }

    /**
     * orig CaveFisher.java:230-246 {@code findSomethingToAttack}: nothing under PlayNicely
     * (:231-233); every {@code EntityLivingBase} whose box meets the hunter's box grown by
     * 10/3/10 (:234, {@code getEntitiesWithinAABB} — players included, itself included);
     * sorted by the {@link GenericTargetSorter} (:235); the first the filter accepts wins
     * (:236-244), else null (:245). {@link TargetSelection#firstMatch} is that sort-and-loop,
     * stable ties included (OPT-021). ENT-S-108.
     */
    @Nullable
    private LivingEntity findSomethingToAttack() {
        if (OreSpawnConfig.PLAY_NICELY.get()) return null;                        // orig :231-233
        List<LivingEntity> candidates = this.level().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(10.0, 3.0, 10.0));                  // orig :234
        return TargetSelection.firstMatch(candidates, this.targetSorter, this::isSuitableTarget); // orig :235-245
    }

    /**
     * orig CaveFisher.java:193-228 {@code isSuitableTarget}, in the original's order: null /
     * self / dead (:194-202), the shared ignore screen (:203-205, ENT-S-106), line of sight
     * (:206-208), then the species chain — CaveFisher (:209-211), EnderReaper (:212-214),
     * EnderKnight (:215-217), any {@code EntityMob} (:218-220, 1.21.1 {@link Monster}) — and
     * the player branch, creative refused (:221-226, {@code isCreativeMode} =
     * {@code Abilities.instabuild}); everything else that lives is prey (:227). ENT-S-108.
     */
    private boolean isSuitableTarget(LivingEntity target) {
        if (target == null || target == this || !target.isAlive()) return false;    // orig :194-202
        if (MyUtils.isIgnoreable(target)) return false;                             // orig :203-205
        if (!this.getSensing().hasLineOfSight(target)) return false;                // orig :206-208
        if (target instanceof CaveFisher) return false;                             // orig :209-211
        if (target instanceof EnderReaper) return false;                            // orig :212-214
        if (target instanceof EnderKnight) return false;                            // orig :215-217
        if (target instanceof Monster) return false;                                // orig :218-220 EntityMob
        if (target instanceof Player player && player.getAbilities().instabuild) return false; // orig :221-226
        return true;                                                                // orig :227
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // orig CaveFisher.java:185-191 — immune to cactus damage only.
        if (source.type().msgId().equals("cactus")) return false;
        return super.hurt(source, amount);
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SND_CRYO_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SND_CRYO_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 1.5f;
    }

    /** orig CaveFisher.java:256-275 — spawner bypass (x/z -2..+1); darkness (restored, ENT-SYS-002); y&lt;=50. */
    @Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnType) {
        if (OriginalSpawnGates.nearOwnSpawner(this, level, -2, 1, 0, 4)) return true;
        if (!OriginalSpawnGates.isDarkEnough(this, level)) return false;
        return this.getY() <= MAX_NATURAL_SPAWN_Y;
    }
}
