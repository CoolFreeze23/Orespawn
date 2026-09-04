package danger.orespawn.entity;

import danger.orespawn.MobStats;

import danger.orespawn.OreSpawnConfig;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.ai.BugMeleeAttackGoal;
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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class DungeonBeast extends Monster {
    // OPT-011: cached SoundEvents — identical createVariableRangeEvent ids,
    // allocated once per class instead of on every sound query.
    private static final SoundEvent SND_DBHIT = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "dbhit"));
    private static final SoundEvent SND_DBDEAD = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "dbdead"));
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(DungeonBeast.class, EntityDataSerializers.INT);

    private static final float MOVE_SPEED = 0.29f;

    /**
     * Per-entity render scratch (orig DungeonBeast.java:43 {@code renderdata = new RenderInfo()},
     * re-newed orig :54, zeroed in entityInit orig :69-83, accessor orig :98-100). Mutated
     * client-side by {@code ModelDungeonBeast} for the jaw-flap latch
     * (orig ModelDungeonBeast.java:546-573); never datawatcher-synced. The original's
     * setRenderInfo (orig :102-111) copied the instance onto itself and is omitted, as in Kraken.
     * ENT-S-093.
     */
    private final danger.orespawn.entity.client.RenderInfo renderInfo =
            new danger.orespawn.entity.client.RenderInfo();

    /**
     * orig DungeonBeast.java:42 {@code TargetSorter}, :53 {@code new GenericTargetSorter(this)}
     * — the shared weighted-distance order (creepers halved, big silhouettes first) the scan
     * sorts its candidates by (:254). ENT-S-108.
     */
    private final GenericTargetSorter targetSorter = new GenericTargetSorter(this);

    public DungeonBeast(EntityType<? extends DungeonBeast> type, Level level) {
        super(type, level);
        // OPT-009: constant speed - assert the attribute base once here instead
        // of re-writing it every tick (same value the removed per-tick call set).
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(MOVE_SPEED);
        this.xpReward = 60;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new BugMeleeAttackGoal(
                this, this::setAttacking, BugMeleeAttackGoal.Params.dungeonBeast()));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        // orig DungeonBeast.java:59 — EntityAIHurtByTarget(this, false), whose attack target nothing in orig read: the
        // pass (:172-185) acted on the scan's pick alone, so the revenge task was inert. The port's slot is read by the
        // melee goal every tick, so a registered revenge goal would chase the attacker where 1.7.10 never retaliated:
        // not registered (ENT-S-129; ENT-S-108's D2).
        // orig DungeonBeast.java:59 registers no target-search task: prey is found by the
        // 1-in-8 EntityLivingBase box scan of :172-173 / :249-265, restored in
        // customServerAiStep / findSomethingToAttack (ENT-S-108). The port's
        // players-only NearestAttackableTargetGoal is gone.
    }

    public static AttributeSupplier.Builder createAttributes() {
        // orig OreSpawnMain.java:6501 — DungeonBeast 65 HP / 12 ATK / 6 armor
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MobStats.DUNGEON_BEAST.maxHealth())
                .add(Attributes.MOVEMENT_SPEED, MOVE_SPEED)
                .add(Attributes.ATTACK_DAMAGE, MobStats.DUNGEON_BEAST.attackDamage())
                .add(Attributes.FOLLOW_RANGE, 24.0)
                .add(Attributes.ARMOR, MobStats.DUNGEON_BEAST.armor());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, 0);
    }

    public final int getAttacking() {
        return this.entityData.get(DATA_ATTACKING);
    }

    public final void setAttacking(int value) {
        this.entityData.set(DATA_ATTACKING, value);
    }

    /** Mirrors orig DungeonBeast.java:98-100 {@code getRenderInfo()}. ENT-S-093. */
    public danger.orespawn.entity.client.RenderInfo getRenderInfo() {
        return this.renderInfo;
    }

    /**
     * orig DungeonBeast.java:167-187 {@code updateAITasks}: nothing while dead (:168-170),
     * super (:171), then on the 1-in-8 tick (:172) the scan (:173). The rest of that block
     * (:174-185: reach distSq &lt; 8, the nextInt(7)==0 || nextInt(8)==1 swing, the chase at
     * 1.2, setAttacking) is {@code BugMeleeAttackGoal.Params.dungeonBeast()}, fed through the
     * target slot by {@link #selectTarget}. ENT-S-108.
     */
    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;            // orig DungeonBeast.java:168-170
        super.customServerAiStep();              // orig :171
        if (this.random.nextInt(8) == 0) {       // orig :172
            this.selectTarget();                 // orig :173 findSomethingToAttack
        }
    }

    /**
     * orig DungeonBeast.java:173: on the cadence tick the prey is whatever {@link #findSomethingToAttack} returns right now —
     * the original kept no target of its own and read none (its :59 revenge task set an attack target nothing
     * consumed), so a candidate that had left the 16/3/16 box or line of sight was simply not found next time
     * (:183-185, setAttacking(0)). The port's single target slot feeds the melee goal, so every pass hands the scan's
     * answer to the slot — replaced, or cleared when the scan comes back empty — and nothing else fills it: the
     * ENT-S-108 ownership mark and the registered revenge goal are gone, because orig scanned every pass regardless
     * of any stored target and never retaliated (ENT-S-129).
     */
    private void selectTarget() {
        LivingEntity pick = this.findSomethingToAttack();           // orig :173
        if (pick != this.getTarget()) this.setTarget(pick);        // the slot refreshed for the goal: this pass's answer, or empty (ENT-S-129)
    }

    /**
     * orig DungeonBeast.java:249-265 {@code findSomethingToAttack}: nothing under PlayNicely
     * (:250-252); every {@code EntityLivingBase} whose box meets the hunter's box grown by
     * 16/3/16 (:253, {@code getEntitiesWithinAABB} — players included, itself included);
     * sorted by the {@link GenericTargetSorter} (:254); the first the filter accepts wins
     * (:255-263), else null (:264). {@link TargetSelection#firstMatch} is that sort-and-loop,
     * stable ties included (OPT-021). ENT-S-108.
     */
    @Nullable
    private LivingEntity findSomethingToAttack() {
        if (OreSpawnConfig.PLAY_NICELY.get()) return null;                        // orig :250-252
        List<LivingEntity> candidates = this.level().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(16.0, 3.0, 16.0));                  // orig :253
        return TargetSelection.firstMatch(candidates, this.targetSorter, this::isSuitableTarget); // orig :254-264
    }

    /**
     * orig DungeonBeast.java:200-247 {@code isSuitableTarget}, in the original's order: null /
     * self / dead (:201-209), the shared ignore screen (:210-212, ENT-S-106), line of sight
     * (:213-215), then the species chain — Rat (:216-218), DungeonBeast (:219-221), Rotator
     * (:222-224), Peacock (:225-227), Irukandji (:228-230), Skate (:231-233), Whale
     * (:234-236), Flounder (:237-239) — and the player branch, creative refused (:240-245,
     * {@code isCreativeMode} = {@code Abilities.instabuild}); everything else that lives is
     * prey (:246). ENT-S-108.
     */
    private boolean isSuitableTarget(LivingEntity target) {
        if (target == null || target == this || !target.isAlive()) return false;    // orig :201-209
        if (MyUtils.isIgnoreable(target)) return false;                             // orig :210-212
        if (!this.getSensing().hasLineOfSight(target)) return false;                // orig :213-215
        if (target instanceof EntityRat) return false;                              // orig :216-218 Rat
        if (target instanceof DungeonBeast) return false;                           // orig :219-221
        if (target instanceof EntityRotator) return false;                          // orig :222-224 Rotator
        if (target instanceof Peacock) return false;                                // orig :225-227
        if (target instanceof Irukandji) return false;                              // orig :228-230
        if (target instanceof Skate) return false;                                  // orig :231-233
        if (target instanceof Whale) return false;                                  // orig :234-236
        if (target instanceof Flounder) return false;                               // orig :237-239
        if (target instanceof Player player && player.getAbilities().instabuild) return false; // orig :240-245
        return true;                                                                // orig :246
    }

    @Override
    public boolean removeWhenFarAway(double dist) {
        return !this.isPersistenceRequired();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SND_DBHIT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SND_DBDEAD;
    }

    @Override
    protected float getSoundVolume() {
        return 0.8f;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getMsgId().equals("inWall") || source.getMsgId().equals("cactus")) {
            return false;
        }
        return super.hurt(source, amount);
    }

    /** orig DungeonBeast.java:275-312 — "Dungeon Beast" spawner bypass; darkness; in Crystal only 25<=y<=28 with >=6 air blocks in the 3x3 ring one above the feet. */
    @Override
    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level,
                                   net.minecraft.world.entity.MobSpawnType spawnType) {
        if (OriginalSpawnGates.nearOwnSpawner(this, level)) return true;
        if (!OriginalSpawnGates.isDarkEnough(this, level)) return false;
        if (danger.orespawn.ModDimensionKeys.isIn(level, danger.orespawn.ModDimensionKeys.CRYSTAL)) {
            if (this.getY() > 28.0 || this.getY() < 25.0) return false;
            int sc = 0;
            net.minecraft.core.BlockPos feet = this.blockPosition();
            for (int dz = -1; dz <= 1; dz++) {
                for (int dx = -1; dx <= 1; dx++) {
                    if (level.getBlockState(feet.offset(dx, 1, dz)).isAir()) sc++;
                }
            }
            if (sc < 6) return false;
        }
        return true;
    }
}
