package danger.orespawn.entity;

import danger.orespawn.MobStats;

import java.util.Comparator;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.ai.DinosaurMeleeAttackGoal;

public class Cryolophosaurus extends Monster {
    private final float moveSpeed = 0.25f;

    public Cryolophosaurus(EntityType<? extends Cryolophosaurus> type, Level level) {
        super(type, level);
        this.xpReward = 10;
    }

    // orig Cryolophosaurus.java:141-211 — proactively hunts (1-in-5 scan over
    // a 9×2×9 box, see customServerAiStep) in addition to retaliating via
    // HurtByTargetGoal; lashes out with timid dice (nextInt(12)/nextInt(14))
    // and forgives its revenge target on a 1-in-200 roll.
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.35));
        this.goalSelector.addGoal(2, new DinosaurMeleeAttackGoal(this, this::legacySetAttacking,
                DinosaurMeleeAttackGoal.Presets.cryolophosaurus()));
        this.goalSelector.addGoal(3, new MyEntityAIWanderALot(this, 10, 1.0));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    // Placeholder for the DinosaurMeleeAttackGoal "setAttacking" callback.
    // Cryolophosaurus has no DATA_ATTACKING watcher (it never needed one in
    // 1.7.10), so this is a no-op. Kept as a method ref for interface parity.
    private void legacySetAttacking(int value) {}

    public static AttributeSupplier.Builder createAttributes() {
        // orig OreSpawnMain.java:6482 — Cryolophosaurus 10 HP / 3 ATK / 1 armor
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MobStats.CRYOLOPHOSAURUS.maxHealth())
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.ATTACK_DAMAGE, MobStats.CRYOLOPHOSAURUS.attackDamage())
                .add(Attributes.ARMOR, MobStats.CRYOLOPHOSAURUS.armor())
                .add(Attributes.FOLLOW_RANGE, 16.0);
    }

    @Override
    public boolean removeWhenFarAway(double dist) {
        return !this.isPersistenceRequired();
    }

    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;
        super.customServerAiStep();
        // orig Cryolophosaurus.java:147-149 — 1-in-200: forgive the attacker.
        if (this.random.nextInt(200) == 1) {
            this.setLastHurtByMob(null);
        }
        // orig Cryolophosaurus.java:150-155 — 1-in-5 proactive hunt: nearest
        // suitable prey in a 9×2×9 box; chase at 1.25 and bite at distSq<5
        // with the timid 1-in-12 / 1-in-14 dice.
        if (this.random.nextInt(5) == 1) {
            LivingEntity prey = this.findSomethingToAttack();
            if (prey != null) {
                this.getNavigation().moveTo(prey, 1.25);
                if (this.distanceToSqr(prey) < 5.0
                        && (this.random.nextInt(12) == 0 || this.random.nextInt(14) == 1)) {
                    this.doHurtTarget(prey);
                }
            }
        }
    }

    /** orig Cryolophosaurus.java:158-211 — prey exclusion list. */
    private boolean isSuitableTarget(LivingEntity candidate) {
        if (candidate == null || candidate == this || !candidate.isAlive()) return false;
        if (!this.getSensing().hasLineOfSight(candidate)) return false;
        if (candidate instanceof Alosaurus || candidate instanceof TRex
                || candidate instanceof Cryolophosaurus
                || candidate instanceof Ghost || candidate instanceof GhostSkelly
                || candidate instanceof CaveFisher || candidate instanceof EntityGammaMetroid
                || candidate instanceof EntityButterfly || candidate instanceof Firefly
                || candidate instanceof EntityMosquito || candidate instanceof RockBase) {
            return false;
        }
        if (candidate instanceof Player player && player.getAbilities().invulnerable) return false;
        return true;
    }

    /** orig Cryolophosaurus.java:213-234 — 9×2×9 scan, nearest first, PlayNicely-gated. */
    @Nullable
    private LivingEntity findSomethingToAttack() {
        if (OreSpawnConfig.PLAY_NICELY.get()) return null;
        List<LivingEntity> candidates = this.level().getEntitiesOfClass(
                LivingEntity.class, this.getBoundingBox().inflate(9.0, 2.0, 9.0));
        candidates.sort(Comparator.comparingDouble(this::distanceToSqr));
        for (LivingEntity candidate : candidates) {
            if (this.isSuitableTarget(candidate)) return candidate;
        }
        return null;
    }

    @Override
    public void tick() {
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(this.moveSpeed);
        super.tick();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        if (this.random.nextInt(6) == 0) {
            return SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "cryo_living"));
        }
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
        return 0.75f;
    }

    /** orig Cryolophosaurus.java:231-236 — darkness, then night OR y<=50 (daytime cave spawns allowed). */
    @Override
    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level,
                                   net.minecraft.world.entity.MobSpawnType spawnType) {
        if (!OriginalSpawnGates.isDarkEnough(this, level)) return false;
        return !OriginalSpawnGates.isDaytime(level) || this.getY() <= 50.0;
    }
}
