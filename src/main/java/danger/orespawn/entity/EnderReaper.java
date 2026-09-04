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
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
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
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public class EnderReaper extends Monster {
    private static final EntityDataAccessor<Boolean> DATA_SCREAMING =
            SynchedEntityData.defineId(EnderReaper.class, EntityDataSerializers.BOOLEAN);

    /** orig EnderReaper.java:31 {@code teleportDelay} — the ticks a target beyond distSq 256 has been held (:131), reset at :130 / :132 / :136; never saved, as orig. ENT-S-141. */
    private int teleportDelay;
    /** orig EnderReaper.java:32 {@code stareTimer} — the pick's stare-sound cadence (:68-73: the sound at 0, reset past 5; cleared at :77 when the nearest player does not stare); never saved, as orig. ENT-S-141. */
    private int stareTimer;

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
            protected boolean canAttack(LivingEntity t, TargetingConditions c) {
                // orig td.bq :155-182 — the revenge memory held any living attacker but a creative one (ENT-S-107: instabuild); vanilla's canAttack would also refuse abilities.invulnerable (ENT-S-132, T8 refuter D1)
                return t != null && t.isAlive() && !t.isSpectator() && !(t instanceof Player p && p.getAbilities().instabuild);
            }

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
        // orig td.bq :155-182 (V10) — the legacy loop's pick took the nearest player of ANY mode (:65) and the same
        // tick nulled a creative EntityPlayerMP target, so a creative starer shadowed a farther survival one: the
        // conditions are rebuilt non-combat (vanilla's forCombat refused abilities.invulnerable — creative, spectator
        // or hand-toggled — inside canAttack, ahead of the pick) with the stare test as the selector and no creative
        // term, and the drop sits after the pick in canUse, orig isCreative -> Abilities.instabuild (ENT-S-107).
        // orig EnderReaper.java:61-81 with td.bq — findPlayerToAttack ran on EVERY target-less tick of the legacy loop: no
        // acquisition roll (HEAD's 3-arg form drew 1-in-5 per goal pass; interval 0 here, mustSee / no mustReach kept), the engine's
        // every-other-tick goal pass completed to every tick by customServerAiStep (ENT-S-141); its scan set is the ONE nearest player
        // of any mode within 81 (:65, a plain sphere from the position), then :67's stare test on that player alone — findTarget below (ENT-S-135).
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 0, true, false, null) {
            {
                this.targetConditions = TargetingConditions.forNonCombat()
                        .selector(e -> e instanceof Player p && EnderReaper.this.shouldAttackPlayer(p)); // orig EnderReaper.java:67 (ENT-S-132); no range term — the 81 is the nearest search's own bound (:65), unscaled by visibility (ENT-S-135)
            }

            @Override
            protected void findTarget() {
                // orig EnderReaper.java:65 — World.getClosestPlayerToEntity(this, 81.0): the single nearest player of ANY mode within 81
                // of the reaper's position (plain distSq, strict <, first wins ties; no alive / creative screen — mc1710 ahb.a(sa,D)),
                // THEN :67's shouldAttackPlayer on that one player: a nearer non-staring player shadows a farther starer
                // (nearest-then-filter), where vanilla's findTarget took the nearest player the conditions ADMIT (filter-then-nearest).
                // The conditions keep the stare selector, the alive / non-spectator screen and the mob-side sight ray (ENT-S-132);
                // spectators are skipped by the search too — the port's convention for a state 1.7.10 lacked (ENT-S-132, iv). ENT-S-135.
                Player nearest = this.mob.level().getNearestPlayer(this.mob.getX(), this.mob.getY(), this.mob.getZ(),
                        this.getFollowDistance(), EntitySelector.NO_SPECTATORS);
                if (nearest != null) { // orig :66
                    if (this.targetConditions.test(this.mob, nearest)) { // orig :67 — the stare test on that one player
                        // orig EnderReaper.java:68-74 — the pick's own side effects, on every target-less tick of the legacy loop: the stare
                        // sound at the player on the first tick of a held stare and every sixth after (:68-73 — stareTimer 0 → the sound,
                        // past 5 → reset), then screaming on (:74) — set even for the creative starer canUse nulls the same tick, as
                        // td.bq left it (ENT-S-141)
                        if (EnderReaper.this.stareTimer == 0) {
                            this.mob.level().playSound(null, nearest.getX(), nearest.getY(), nearest.getZ(),
                                    SoundEvents.ENDERMAN_STARE, SoundSource.HOSTILE, 1.0f, 1.0f); // orig :69 — playSoundAtEntity(player, "mob.endermen.stare", 1.0f, 1.0f)
                        }
                        if (EnderReaper.this.stareTimer++ == 5) {
                            EnderReaper.this.stareTimer = 0; // orig :71-73
                        }
                        EnderReaper.this.setScreaming(true); // orig :74
                        this.target = nearest; // orig :75
                        return;
                    }
                    EnderReaper.this.stareTimer = 0; // orig :77 — the nearest player is not staring: the cadence and the scream reset
                    EnderReaper.this.setScreaming(false); // orig :78
                }
                this.target = null; // orig :80
            }

            @Override
            public boolean canUse() {
                if (OreSpawnConfig.PLAY_NICELY.get()) return false; // orig EnderReaper.java:62-64 (ENT-S-115)
                if (!super.canUse()) return false; // orig EnderReaper.java:65-67 — the nearest starer, of any mode, is the pick
                if (this.target instanceof Player p && p.getAbilities().instabuild) { // orig td.bq :155-182 — a creative EntityPlayerMP target is nulled the same tick it was picked: nothing is hunted this pass, the farther survival starer stays shadowed (ENT-S-132)
                    this.target = null;
                    return false;
                }
                return true;
            }

            @Override
            public boolean canContinueToUse() {
                return EnderReaper.this.holdsLegacyTarget(); // orig EnderReaper.java:111-115 with V10 — held until dead, creative or the daylight roll: no FOLLOW_RANGE (81) release, no 60-tick unseen memory (ENT-S-129)
            }
        });
    }

    /**
     * orig EnderReaper.java:61-81 with td.bq — the legacy loop ({@code EntityLivingBase.onLivingUpdate} → {@code
     * updateEntityActionState} on a mob with no AI tasks) asked the pick, the creative drop and the hold on EVERY server tick;
     * vanilla's {@code Mob.serverAiStep} evaluates the target goals on its every-other-tick pass alone ({@code (tickCount + id) % 2
     * == 0}, or the first two ticks) and only ticks the running ones on the other tick, which left the pair's pick at half orig's
     * cadence (ENT-S-135's disclosed residual). On the tick the engine skips, the same full target-selector pass runs from here —
     * after the engine's pass slot and the navigation, as the engine's own pass sits ahead of {@code customServerAiStep} — so the
     * goals (T8's / T3b's, untouched) are asked once every tick, as orig's loop was. ENT-S-141.
     */
    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        int i = this.tickCount + this.getId(); // Mob.serverAiStep's own parity test
        if (i % 2 != 0 && this.tickCount > 1) {
            this.targetSelector.tick();
        }
    }

    /**
     * orig EnderReaper.java — the legacy (non-AI) loop's hold of {@code entityToAttack} ({@code EntityCreature
     * .updateEntityActionState}, the ledger's V10): kept while alive and not creative, at any range and through any
     * sight loss, until the daylight roll (:111-115) nulls it; nulled, it is gone (no re-assert). Both port target
     * goals hold by this rule (ENT-S-129). The creative drop is td.bq :155-182's {@code isCreative} — orig's
     * game-type read, {@code Abilities.instabuild} under the ENT-S-107 mapping — not vanilla's {@code canAttack}
     * ({@code Player.canBeSeenAsEnemy} = {@code !abilities.invulnerable}: creative, spectator or hand-toggled, which
     * dropped an invulnerable survival player 1.7.10 kept); a spectator, a state 1.7.10 had no counterpart for, stays
     * refused as at HEAD (ENT-S-132).
     */
    private boolean holdsLegacyTarget() {
        LivingEntity held = this.getTarget();
        return held != null && held.isAlive() && !held.isSpectator() // orig td.bq :107-152 isEntityAlive; the spectator screen kept from HEAD's canAttack (no 1.7.10 state)
                && !(held instanceof Player p && p.getAbilities().instabuild); // orig td.bq :155-182 — EntityPlayerMP && isCreative -> entityToAttack = null (ENT-S-132)
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

        // orig EnderReaper.java:124-138 — the stare-driven teleports, read off the target the previous tick's pick / hold left
        // (orig's onLivingUpdate ran ahead of super's legacy loop; this aiStep ahead of super's goal pass), server-side and alive
        // (:124): a target that is a player staring (:126, shouldAttackPlayer) within distSq < 16 (:127) → teleportRandomly (:128),
        // the far counter reset (:130); any other target beyond distSq 256 (:131) counts a tick and, past 30, teleports toward it
        // (teleportToEntity), the counter reset on a landing (:132) — the counter holds while such a target is within 16 blocks;
        // no target → screaming off, the counter reset (:134-137). Orig :120-123 between (isJumping = false, faceEntity) are the
        // legacy loop's steering — the port's look and melee controls — not this row. ENT-S-141.
        if (!this.level().isClientSide && this.isAlive()) {
            LivingEntity target = this.getTarget();
            if (target != null) {
                if (target instanceof Player player && this.shouldAttackPlayer(player)) {
                    if (target.distanceToSqr(this) < 16.0) {
                        this.teleportRandomly();
                    }
                    this.teleportDelay = 0;
                } else if (target.distanceToSqr(this) > 256.0 && this.teleportDelay++ >= 30 && this.teleportToEntity(target)) {
                    this.teleportDelay = 0;
                }
            } else {
                this.setScreaming(false);
                this.teleportDelay = 0;
            }
        }
        super.aiStep();
    }

    protected boolean teleportRandomly() {
        double x = this.getX() + (this.random.nextDouble() - 0.5) * 64.0;
        double y = this.getY() + (this.random.nextInt(64) - 32);
        double z = this.getZ() + (this.random.nextDouble() - 0.5) * 64.0;
        return this.randomTeleport(x, y, z, true);
    }

    /**
     * orig EnderReaper.java:149-157 {@code teleportToEntity} — the 1.7.10 Enderman's teleport toward a far target: the unit vector
     * from the target to the reaper's mid-height (:150-151 — {@code bb.minY + height / 2 - target.posY + target.eyeHeight}, the
     * expression's own sign order kept; {@code posY} is the port's {@code getY()}), then the spot 16 blocks along it from the
     * reaper (:152-155: x and z jittered by ±4, y by {@code nextInt(16) - 8}), landed through the same search as the random
     * teleport (:156 {@code teleportTo}, the port's {@code randomTeleport}). Reached from {@link #aiStep}'s far branch (:131). ENT-S-141.
     */
    protected boolean teleportToEntity(Entity target) {
        Vec3 vec = new Vec3(this.getX() - target.getX(),
                this.getBoundingBox().minY + this.getBbHeight() / 2.0f - target.getY() + target.getEyeHeight(),
                this.getZ() - target.getZ()); // orig :150
        vec = vec.normalize(); // orig :151
        double d0 = 16.0; // orig :152
        double d1 = this.getX() + (this.random.nextDouble() - 0.5) * 8.0 - vec.x * d0; // orig :153
        double d2 = this.getY() + (this.random.nextInt(16) - 8) - vec.y * d0; // orig :154
        double d3 = this.getZ() + (this.random.nextDouble() - 0.5) * 8.0 - vec.z * d0; // orig :155
        return this.randomTeleport(d1, d2, d3, true); // orig :156 teleportTo — the port's mapping, as teleportRandomly's
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
