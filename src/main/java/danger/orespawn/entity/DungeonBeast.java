package danger.orespawn.entity;

import danger.orespawn.MobStats;

import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.ai.BugMeleeAttackGoal;
import danger.orespawn.util.MyUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
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
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        // orig DungeonBeast.java:210-212 — the shared ignore screen, ahead of line of
        // sight (:213), as the target goal's predicate (ENT-S-106).
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true,
                target -> !MyUtils.isIgnoreable(target)));
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
