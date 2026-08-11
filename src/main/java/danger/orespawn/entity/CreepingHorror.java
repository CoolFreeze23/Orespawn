package danger.orespawn.entity;

import danger.orespawn.MobStats;

import java.util.Comparator;
import java.util.List;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveThroughVillageGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.ai.GenericTargetSorter;
import danger.orespawn.entity.ai.TargetSelection;

public class CreepingHorror extends Monster {
    // OPT-011: cached SoundEvents — identical createVariableRangeEvent ids,
    // allocated once per class instead of on every sound query.
    private static final SoundEvent SND_CREEPINGHORROR_LIVING = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "creepinghorror_living"));
    private static final SoundEvent SND_CREEPINGHORROR_HIT = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "creepinghorror_hit"));
    private static final SoundEvent SND_CREEPINGHORROR_DEAD = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "creepinghorror_dead"));
    private static final ResourceKey<Level> CHAOS_DIM = ResourceKey.create(
            Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath("orespawn", "chaos"));
    private static final long DAYTIME_TICKS = 24000L;
    /** Past this tick-of-day, the random daytime despawn logic does not run. */
    private static final long DAYTIME_DESPAWN_CUTOFF = 11000L;

    private final Comparator<Entity> targetSorter;
    private static final float MOVE_SPEED = 0.25f;

    public CreepingHorror(EntityType<? extends CreepingHorror> type, Level level) {
        super(type, level);
        // OPT-009: constant speed - assert the attribute base once here instead
        // of re-writing it every tick (same value the removed per-tick call set).
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(MOVE_SPEED);
        this.xpReward = 5;
        // TF-035: orig CreepingHorror.java:42,58 — scans sort with
        // GenericTargetSorter (creeper-halved / big-silhouette-prioritized),
        // not plain distance.
        this.targetSorter = new GenericTargetSorter(this);
    }

    @Override
    protected void registerGoals() {
        // orig CreepingHorror.java:51-57 — swim, panic 1.35, MoveThroughVillage@2,
        // wander@3, watch-player 8.0 @4, look-idle@5; HurtBy on the target selector.
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.35));
        // Mapping decision (orig :53 — EntityAIMoveThroughVillage(this, 1.0, false)):
        // the 1.7.10 goal walked the village door graph, which the 1.14 village
        // rework removed. Vanilla MoveThroughVillageGoal is the honest modern
        // equivalent — it wanders between sections of the POI-based village
        // instead. Speed 1.0 and onlyAtNight=false are the orig arguments;
        // distanceToPoi 4 is the vanilla Zombie value (the 1.7.10 ctor had no
        // such knob) and the horror cannot deal with doors.
        this.goalSelector.addGoal(2, new MoveThroughVillageGoal(this, 1.0, false, 4, () -> false));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        // orig OreSpawnMain.java:6513 — CreepingHorror 10 HP / 3 ATK / 2 armor
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MobStats.CREEPING_HORROR.maxHealth())
                .add(Attributes.MOVEMENT_SPEED, MOVE_SPEED)
                .add(Attributes.ATTACK_DAMAGE, MobStats.CREEPING_HORROR.attackDamage())
                .add(Attributes.ARMOR, MobStats.CREEPING_HORROR.armor())
                .add(Attributes.FOLLOW_RANGE, 16.0);
    }

    @Override
    public boolean removeWhenFarAway(double dist) {
        return !this.isPersistenceRequired();
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnType) {
        // orig CreepingHorror.java:220-228 — only spawns in darkness, at
        // night, and either in the Chaos dimension (DimensionID6) or at y<=15.
        if (level instanceof ServerLevelAccessor server) {
            if (!Monster.isDarkEnoughToSpawn(server, this.blockPosition(), this.getRandom())) {
                return false;
            }
            if (server.getLevel().isDay()) return false;
            return server.getLevel().dimension() == CHAOS_DIM || this.getY() <= 15.0;
        }
        return super.checkSpawnRules(level, spawnType);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.isPersistenceRequired()) return;
        long timeOfDay = this.level().getDayTime() % DAYTIME_TICKS;
        if (timeOfDay > DAYTIME_DESPAWN_CUTOFF) return;
        if (this.getRandom().nextInt(500) == 1) {
            this.discard();
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SND_CREEPINGHORROR_LIVING;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SND_CREEPINGHORROR_HIT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SND_CREEPINGHORROR_DEAD;
    }

    @Override
    protected float getSoundVolume() {
        return 0.65f;
    }

    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;
        super.customServerAiStep();

        if (this.getRandom().nextInt(200) == 1) {
            this.setTarget(null);
        }
        if (this.getRandom().nextInt(5) == 1) {
            LivingEntity currentTarget = findSomethingToAttack();
            if (currentTarget != null) {
                this.getNavigation().moveTo(currentTarget, 1.25);
                if (this.distanceToSqr(currentTarget) < 5.0) {
                    if (this.random.nextInt(12) == 0 || this.random.nextInt(14) == 1) {
                        this.doHurtTarget(currentTarget);
                    }
                }
            }
        }
    }

    private LivingEntity findSomethingToAttack() {
        // orig CreepingHorror.java:203-205 — PlayNicely disables aggression entirely.
        if (danger.orespawn.OreSpawnConfig.PLAY_NICELY.get()) return null;
        List<LivingEntity> list = this.level().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(16.0, 4.0, 16.0));
        // OPT-021: nearest-first pick without the full list sort; TargetSelection
        // preserves the removed sort's order and stable-tie semantics exactly.
        return TargetSelection.firstMatch(list, this.targetSorter, this::isSuitableTarget);
    }

    /**
     * orig CreepingHorror.java:147-200 — attacks anything it can SEE (:157)
     * except its own kind (:160) and the mod's scenery/ally mobs: RockBase
     * (:163), EnderReaper (:166), LeafMonster (:169), Dragon (:172),
     * TerribleTerror (:175), LurkingTerror (:178), PitchBlack (:181),
     * Firefly (:184), Island (:187) and IslandToo (:190). Creative players
     * are exempt (:193-198); everything else is fair game (:199).
     */
    private boolean isSuitableTarget(LivingEntity target) {
        if (target == null || target == this || !target.isAlive()) return false;
        if (!this.getSensing().hasLineOfSight(target)) return false;
        if (target instanceof CreepingHorror) return false;
        if (target instanceof RockBase) return false;
        if (target instanceof EnderReaper) return false;
        if (target instanceof EntityLeafMonster) return false;
        if (target instanceof Dragon) return false;
        if (target instanceof EntityTerribleTerror) return false;
        if (target instanceof EntityLurkingTerror) return false;
        if (target instanceof PitchBlack) return false;
        if (target instanceof Firefly) return false;
        if (target instanceof Island) return false;
        if (target instanceof IslandToo) return false;
        if (target instanceof Player player) return !player.getAbilities().instabuild;
        return true;
    }
}
