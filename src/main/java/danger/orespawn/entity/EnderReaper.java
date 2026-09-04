package danger.orespawn.entity;

import danger.orespawn.MobStats;
import danger.orespawn.util.OreSpawnSight;
import danger.orespawn.OreSpawnConfig;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public class EnderReaper extends Monster {
    private static final EntityDataAccessor<Boolean> DATA_SCREAMING =
            SynchedEntityData.defineId(EnderReaper.class, EntityDataSerializers.BOOLEAN);


    public EnderReaper(EntityType<? extends EnderReaper> type, Level level) {
        super(type, level);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        // orig EnderReaper.java — no AI tasks: EntityMob.attackEntityFrom set the legacy loop's entityToAttack (V10), held
        // until dead, creative or the daylight roll (:111-115) — no range, no sight memory, gone once nulled; the port's
        // revenge goal holds by that rule (holdsLegacyTarget) (ENT-S-129)
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this) {
            @Override
            public boolean canContinueToUse() {
                return EnderReaper.this.holdsLegacyTarget(); // ENT-S-129
            }
        });
        // orig EnderReaper.java:67 — unprovoked player targeting runs through the
        // pumpkin-stare test (shouldAttackPlayer), never proximity alone.
        // orig EnderReaper.java:62-64 — findPlayerToAttack (func_70782_k) answers null under PlayNicely (PlayNicely
        // != 0), ahead of the nearest-player pick and the stare test; the port's pick is this goal, so the flag is
        // read live in its canUse: the goal never starts while PlayNicely is on (ENT-S-115).
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false,
                e -> e instanceof Player p && this.shouldAttackPlayer(p)) {
            @Override
            public boolean canUse() {
                if (OreSpawnConfig.PLAY_NICELY.get()) return false; // orig EnderReaper.java:62-64 (ENT-S-115)
                return super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return EnderReaper.this.holdsLegacyTarget(); // orig EnderReaper.java:111-115 with V10 — held until dead, creative or the daylight roll: no FOLLOW_RANGE (81) release, no 60-tick unseen memory (ENT-S-129)
            }
        });
    }

    /**
     * orig EnderReaper.java — the legacy (non-AI) loop's hold of {@code entityToAttack} ({@code EntityCreature
     * .updateEntityActionState}, the ledger's V10): kept while alive and not creative, at any range and through any
     * sight loss, until the daylight roll (:111-115) nulls it; nulled, it is gone (no re-assert). Both port target
     * goals hold by this rule; vanilla's {@code canAttack} is the creative / spectator screen. ENT-S-129.
     */
    private boolean holdsLegacyTarget() {
        LivingEntity held = this.getTarget();
        return held != null && held.isAlive() && this.canAttack(held);
    }

    // orig EnderReaper.java:83-93 — pumpkin-stare gate. A pumpkin on the head
    // (:84-87; the wearable 1.7.10 pumpkin block maps to the modern carved
    // pumpkin) hides the player entirely; otherwise the reaper attacks only when
    // the player's look vector lines up with the reaper's mid-height
    // (d1 > 1.0 - 0.025/d0, :88-91) and the player has line of sight to it (:92).
    // Same math as vanilla EnderMan.isLookingAtMe.
    boolean shouldAttackPlayer(Player player) {
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD); // orig :84 armor slot 3
        if (helmet.is(Blocks.CARVED_PUMPKIN.asItem())) {
            return false; // orig :85-87
        }
        Vec3 look = player.getViewVector(1.0f).normalize(); // orig :88
        Vec3 toReaper = new Vec3(
                this.getX() - player.getX(),
                this.getY() + this.getBbHeight() / 2.0f - player.getEyeY(),
                this.getZ() - player.getZ()); // orig :89 — bb minY + height/2 vs player eye
        double dist = toReaper.length(); // orig :90
        double dot = look.dot(toReaper.normalize()); // orig :91
        return dot > 1.0 - 0.025 / dist && OreSpawnSight.canSee(player, this); // orig :92 — player.canEntityBeSeen(this): the PLAYER's ray under the 1.7.10 convention, routed by hand because the receiver-gated mixin cannot reach a player receiver (ENT-S-121, refuter B)
    }

    public static AttributeSupplier.Builder createAttributes() {
        // orig OreSpawnMain.java:6508 — EnderReaper 90 HP / 18 ATK / 8 armor
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MobStats.ENDER_REAPER.maxHealth())
                .add(Attributes.MOVEMENT_SPEED, 0.37)
                .add(Attributes.ATTACK_DAMAGE, MobStats.ENDER_REAPER.attackDamage())
                .add(Attributes.ARMOR, MobStats.ENDER_REAPER.armor())
                .add(Attributes.FOLLOW_RANGE, 81.0)
                .add(Attributes.STEP_HEIGHT, 1.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_SCREAMING, false);
    }

    public boolean isScreaming() {
        return this.entityData.get(DATA_SCREAMING);
    }

    public void setScreaming(boolean val) {
        this.entityData.set(DATA_SCREAMING, val);
    }

    @Override
    public void aiStep() {
        if (this.isInWaterRainOrBubble()) {
            this.hurt(this.damageSources().drown(), 1.0f);
        }

        if (!this.level().isClientSide) {
            for (int i = 0; i < 2; ++i) {
                this.level().addParticle(ParticleTypes.PORTAL,
                        this.getX() + (this.random.nextDouble() - 0.5) * this.getBbWidth(),
                        this.getY() + this.random.nextDouble() * this.getBbHeight() - 0.25,
                        this.getZ() + (this.random.nextDouble() - 0.5) * this.getBbWidth(),
                        (this.random.nextDouble() - 0.5) * 2.0, -this.random.nextDouble(),
                        (this.random.nextDouble() - 0.5) * 2.0);
            }
        }

        // orig EnderReaper.java:111-115 — daylight escape: server-side, in
        // daytime, when brightness > 0.5 and the sky is visible overhead, a
        // brightness-scaled dice (rand*30 < (f-0.4)*2) drops the target, stops
        // screaming, and teleports away.
        if (this.level().isDay() && !this.level().isClientSide) {
            float brightness = this.getLightLevelDependentMagicValue(); // orig :111 func_70013_c(1.0f)
            if (brightness > 0.5f
                    && this.level().canSeeSky(this.blockPosition())
                    && this.random.nextFloat() * 30.0f < (brightness - 0.4f) * 2.0f) {
                this.setTarget(null);
                this.setScreaming(false);
                teleportRandomly();
            }
        }

        // orig EnderReaper.java:116-119 — wet (func_70026_G) OR burning
        // (func_70027_ad) → stop screaming and teleport.
        if (this.isInWaterRainOrBubble() || this.isOnFire()) {
            this.setScreaming(false);
            teleportRandomly();
        }
        super.aiStep();
    }

    protected boolean teleportRandomly() {
        double x = this.getX() + (this.random.nextDouble() - 0.5) * 64.0;
        double y = this.getY() + (this.random.nextInt(64) - 32);
        double z = this.getZ() + (this.random.nextDouble() - 0.5) * 64.0;
        return this.randomTeleport(x, y, z, true);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) return false;
        this.setScreaming(true);
        if (source.is(DamageTypes.INDIRECT_MAGIC) || source.getDirectEntity() != source.getEntity()) {
            for (int i = 0; i < 16; ++i) {
                if (this.teleportRandomly()) return true;
            }
        }
        return super.hurt(source, amount);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.isScreaming() ? SoundEvents.ENDERMAN_SCREAM : SoundEvents.ENDERMAN_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENDERMAN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENDERMAN_DEATH;
    }

    /** orig EnderReaper.java:253-279 — "Ender Reaper" spawner bypass; darkness; night; y>=30; no other EnderReaper within 16/8/16. */
    @Override
    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level,
                                   net.minecraft.world.entity.MobSpawnType spawnType) {
        if (OriginalSpawnGates.nearOwnSpawner(this, level)) return true;
        if (!OriginalSpawnGates.isDarkEnough(this, level)) return false;
        if (OriginalSpawnGates.isDaytime(level)) return false;
        if (this.getY() < 30.0) return false;
        return !OriginalSpawnGates.anyOtherNearby(this, level, EnderReaper.class, 16.0, 8.0, 16.0);
    }
}
