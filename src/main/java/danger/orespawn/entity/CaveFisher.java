package danger.orespawn.entity;

import danger.orespawn.MobStats;

import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.ai.BugMeleeAttackGoal;
import javax.annotation.Nullable;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

public class CaveFisher extends Monster {
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
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        // orig CaveFisher.java:193-228 — also preys on passive mobs (anything
        // that isn't an EntityMob/CaveFisher/EnderReaper/EnderKnight). Animal
        // targeting covers the non-monster living set in modern terms.
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Animal.class, true));
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
        return SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "cryo_hurt"));
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "cryo_death"));
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
