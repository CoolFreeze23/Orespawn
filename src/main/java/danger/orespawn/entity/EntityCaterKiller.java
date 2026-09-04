package danger.orespawn.entity;

import danger.orespawn.MobStats;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.ai.BugMeleeAttackGoal;
import danger.orespawn.entity.ai.GenericTargetSorter;
import danger.orespawn.entity.ai.TargetSelection;
import danger.orespawn.util.MyUtils;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import danger.orespawn.OreSpawnConfig;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class EntityCaterKiller extends Monster {
    // OPT-011: cached SoundEvents — identical createVariableRangeEvent ids,
    // allocated once per class instead of on every sound query.
    private static final SoundEvent SND_CATERKILLER_LIVING = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "caterkiller_living"));
    private static final SoundEvent SND_CATERKILLER_HIT = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "caterkiller_hit"));
    private static final SoundEvent SND_CATERKILLER_DEATH = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "caterkiller_death"));
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(EntityCaterKiller.class, EntityDataSerializers.INT);

    /**
     * Metamorphosis timer — orig CaterKiller.java:438-448: after 2400 ticks
     * below max health the Cater Killer transforms, spawning a Brutalfly and
     * 10 Butterflies with an explosion sound, then removes itself.
     */
    private int damagedDespawnTicker = 0;

    /**
     * Cobweb-trail throttle. The 1.7.10 Cater Killer spat cobwebs into the
     * tile underneath whichever player it was actively chasing, slowing the
     * pursued player and forcing them into ranged combat. We rate-limit
     * those setBlock calls so a long chase doesn't hammer chunk updates.
     */
    private int cobwebCooldown = 0;
    private static final int COBWEB_INTERVAL_TICKS = 40;

    /**
     * orig CaterKiller.java:450 — field_70134_J (isInWeb), raised by the
     * cobweb block's collision callback; the AI step consumes it to chew
     * the mob free. Modern cobwebs report through
     * {@link #makeStuckInBlock}, so the flag is mirrored there.
     */
    private boolean inWeb = false;

    /** orig CaterKiller.java:68 — the revenge task; the pass's 1-in-200 release ends it (see {@link RevengeGoal}); assigned in registerGoals (the Mob constructor). ENT-S-129. */
    private RevengeGoal revengeGoal;

    /**
     * orig CaterKiller.java:43 {@code TargetSorter}, :62 {@code new GenericTargetSorter(this)} — the shared weighted-distance
     * order (creepers halved, big silhouettes first) the scan sorts its candidates by (:564; the ledger's T4 row, closed by the
     * sorter here). ENT-S-135.
     */
    private final GenericTargetSorter targetSorter = new GenericTargetSorter(this);

    /**
     * The last pick the pass handed to the target slot. 1.7.10 stored the scan's pick nowhere — the pass acted on it for
     * that tick and re-derived it on the next (orig CaterKiller.java:471-473); only the stored attack target persisted
     * ({@link #hurt}'s :96-98 store and the :68 revenge task), read ahead of the scan (:463). The port stores the pick so the
     * melee goal can consume it and the next pass can tell its own occupant from a stored one: under this mark it is
     * re-derived every pass (replaced, or cleared when not found again), never sticky; a target set by any other path is
     * left alone (the ENT-S-108 slot rule; see {@link #setTarget}). The vanilla {@code NearestAttackableTargetGoal<Player>}
     * that held the pick until vanilla's release (the ledger's "vanilla hold" of ENT-S-129 / ENT-S-132) is gone with the
     * scan's return. ENT-S-135.
     */
    @Nullable
    private LivingEntity scanPick;


    public EntityCaterKiller(EntityType<? extends EntityCaterKiller> type, Level level) {
        super(type, level);
        this.xpReward = 200;
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        // orig CaterKiller.java:54-58 — half size when PlayNicely is on.
        return OreSpawnConfig.PLAY_NICELY.get()
                ? EntityDimensions.scalable(1.45f, 2.3f)
                : EntityDimensions.scalable(2.9f, 4.6f);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new BugMeleeAttackGoal(
                this, this::setAttacking, BugMeleeAttackGoal.Params.caterKiller()));
        this.goalSelector.addGoal(2, new MyEntityAIWanderALot(this, 16, 1.0));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.revengeGoal = new RevengeGoal();
        this.targetSelector.addGoal(1, this.revengeGoal); // orig CaterKiller.java:68 — EntityAIHurtByTarget(this, false), released by the pass's 1-in-200 (ENT-S-129)
        // orig CaterKiller.java:68 registers no target-search task: prey is found by the 1-in-4 EntityLivingBase box scan of
        // :462-500 / :559-574, restored in customServerAiStep / findSomethingToAttack (ENT-S-135) — every living thing in the
        // 20/8/20 box through orig's ladder (:533-557: the MyCanSee block walk, creative = Abilities.instabuild at :548
        // (ENT-S-107 / ENT-S-132), CaterKiller, EntityMob, isAttackableNonMob), sorted by the GenericTargetSorter, at orig's
        // cadence. The port's players-only NearestAttackableTargetGoal<Player> (a FOLLOW_RANGE 40 sphere, ≈ 1-in-10; its
        // conditions rebuilt by ENT-S-132, its live PlayNicely canUse by ENT-S-115 — both now inside the scan at their orig
        // positions, :548 and :560-562) is gone; its vanilla hold with it — the scan's pick is re-derived every pass under
        // the ownership mark (ENT-S-129).
    }

    public static AttributeSupplier.Builder createAttributes() {
        // orig OreSpawnMain.java:6481 — CaterKiller 450 HP / 32 ATK / 19 armor
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MobStats.CATERKILLER.maxHealth())
                .add(Attributes.MOVEMENT_SPEED, 0.35)
                .add(Attributes.ATTACK_DAMAGE, MobStats.CATERKILLER.attackDamage())
                .add(Attributes.ARMOR, MobStats.CATERKILLER.armor())
                .add(Attributes.FOLLOW_RANGE, 40.0);
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

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        if (this.random.nextInt(3) == 0) {
            return SND_CATERKILLER_LIVING;
        }
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SND_CATERKILLER_HIT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SND_CATERKILLER_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 1.5f;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (super.doHurtTarget(target)) {
            if (target instanceof LivingEntity) {
                double knockbackStrength = 1.2;
                double verticalKnockback = 0.1;
                float angle = (float) Math.atan2(target.getZ() - this.getZ(), target.getX() - this.getX());
                if (target.isRemoved() || target instanceof Player) verticalKnockback *= 2.0;
                target.push(Math.cos(angle) * knockbackStrength, verticalKnockback, Math.sin(angle) * knockbackStrength);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        Entity attacker = source.getEntity();
        boolean ret = super.hurt(source, amount);
        // orig CaterKiller.java:96-98 — an EntityLiving attacker becomes the stored target, read ahead of the scan (:463); the
        // revenge task stores any other living attacker: the scan's mark on a pick that turned on the Cater Killer ends
        // exactly when this hit stores it — the Mob store below, or super.hurt's lastHurtByMob record of this tick; a hit
        // that stores nothing keeps the pick transient (ENT-S-129, the ownership convention; ENT-S-135)
        if (attacker != null && attacker == this.scanPick && (attacker instanceof Mob
                || (this.getLastHurtByMob() == attacker && this.getLastHurtByMobTimestamp() == this.tickCount))) this.scanPick = null;
        if (attacker instanceof Mob mob) {
            this.setTarget(mob);
        }
        return ret;
    }

    /**
     * A change of occupant by any other path — the revenge goal's start or stop, a hurt store, the melee goal's stop, an
     * event handler — ends the scan's ownership of the slot; a re-assert of the occupant already there keeps it:
     * {@code TargetGoal.canContinueToUse} re-sets the mob's CURRENT target on every cleanup pass while the revenge goal
     * runs, and an every-set clear turned the scan's own pick into a sticky one (ENT-S-117 refuter B's window). The
     * port-wide convention ruled in ENT-S-129 (the Water Dragon's ENT-S-117 form); the hurt hand-off is in {@link #hurt}.
     * ENT-S-135.
     */
    @Override
    public void setTarget(@Nullable LivingEntity target) {
        LivingEntity before = this.getTarget();
        super.setTarget(target);
        if (this.getTarget() != before) this.scanPick = null; // ENT-S-129: the mark ends on a change of occupant only
    }

    /**
     * orig CaterKiller.java:68 {@code EntityAIHurtByTarget(this, false)} — the revenge task whose attack target the pass read
     * ahead of the scan (:463) and released on its roll (:468-470); 1.7.10's task ended when its attack target
     * was nulled ({@code EntityAITarget.continueExecuting}), where vanilla's {@code TargetGoal} re-asserts its own memory
     * into an emptied slot — so the pass's release also drops that memory ({@link #release}). The hold itself stays
     * vanilla's. ENT-S-129.
     */
    private final class RevengeGoal extends HurtByTargetGoal {
        RevengeGoal() {
            super(EntityCaterKiller.this);
        }

        /** orig :468-470 {@code setAttackTarget(null)} ended the task: the goal's memory goes with the slot. */
        /** The goal's own memory: the pass's 1-in-200 may clear only what this goal stored, as orig :468-470 could only clear a stored attacker (ENT-S-129 refuter A). */
        LivingEntity held() {
            return this.targetMob;
        }

        void release() {
            this.targetMob = null;
        }
    }

    @Override
    public void makeStuckInBlock(BlockState state, Vec3 motionMultiplier) {
        // orig CaterKiller.java:450 — only the cobweb raised field_70134_J
        // (BlockWeb.onEntityCollidedWithBlock -> setInWeb); berry-bush and
        // powder-snow style stuck states did not exist and must not trigger
        // the chew-free loop.
        if (state.is(Blocks.COBWEB)) {
            this.inWeb = true;
        }
        super.makeStuckInBlock(state, motionMultiplier);
    }

    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;
        super.customServerAiStep();

        // orig CaterKiller.java:438-448 — timed metamorphosis: while damaged,
        // count up; past 2400 ticks spawn 1 Brutalfly + 10 Butterflies with an
        // explosion sound and remove self (NO loot — this is not a death).
        // Note: the orig ticker never resets when healed (quirk kept).
        if (this.getHealth() + 1.0f < this.getMaxHealth()) {
            ++this.damagedDespawnTicker;
            if (this.damagedDespawnTicker > 2400) {
                if (this.level() instanceof ServerLevel serverLevel) {
                    EntityBrutalfly fly = ModEntities.ENTITY_BRUTALFLY.get().create(serverLevel);
                    if (fly != null) {
                        fly.moveTo(this.getX(), this.getY() + 4.0, this.getZ(),
                                this.random.nextFloat() * 360.0f, 0.0f);
                        serverLevel.addFreshEntity(fly);
                    }
                    this.playSound(SoundEvents.GENERIC_EXPLODE.value(), 1.0f,
                            this.random.nextFloat() * 0.2f + 0.9f);
                    for (int i = 0; i < 10; i++) {
                        EntityButterfly bf = ModEntities.ENTITY_BUTTERFLY.get().create(serverLevel);
                        if (bf == null) continue;
                        bf.moveTo(this.getX(), this.getY() + 1.0 + this.random.nextInt(4), this.getZ(),
                                this.random.nextFloat() * 360.0f, 0.0f);
                        serverLevel.addFreshEntity(bf);
                    }
                }
                this.discard();
                return;
            }
        }

        // orig CaterKiller.java:450-461 — web-self-clear: while flagged
        // in-web, every cobweb in the 5x6x5 box around the feet (x -2..2,
        // y -1..4, z -2..2, toward-zero int coords) is set to air, then the
        // flag drops. The original does NOT gate this on mobGriefing (only
        // the tree-eat at :521 is gated).
        if (this.inWeb) {
            int bx = (int) this.getX();
            int by = (int) this.getY();
            int bz = (int) this.getZ();
            for (int i = -2; i <= 2; ++i) {
                for (int j = -1; j < 5; ++j) {
                    for (int k = -2; k <= 2; ++k) {
                        BlockPos pos = new BlockPos(bx + i, by + j, bz + k);
                        if (!this.level().getBlockState(pos).is(Blocks.COBWEB)) continue;
                        this.level().setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            }
            this.inWeb = false;
        }

        // orig CaterKiller.java:462-500 — the 1-in-4 pass: the stored attack target read (:463), dropped dead (:464-467, the
        // slot and the pass-local `e`), then the 1-in-200 `setAttackTarget(null)` (:468-470) — rolled inside the pass, after
        // the read, on the slot alone: `e` stands, so the cleared target is still engaged this pass (ENT-S-129) and the scan
        // of :471-473 (`if (e == null)`) waits for the next pass (ENT-S-135); the rest of the block (:474-499: look, reach
        // (5 + w/2)^2, the nextInt(3)==0 || nextInt(4)==1 swing, the chase at 1.25, the cobweb, setAttacking) is
        // Params.caterKiller's melee goal, fed through the target slot. The melee goal's every-tick 1-in-200 is gone (ENT-S-129).
        if (this.random.nextInt(4) == 0) {                                   // orig :462
            LivingEntity stored = this.getTarget();                          // orig :463
            if (stored != null && !stored.isAlive()) {                       // orig :464-467
                this.setTarget(null); stored = null;                         // orig :465-466 — the slot and the pass-local e; the dead drop alone empties e
            }
            if (this.random.nextInt(200) == 0 && this.getTarget() != null && this.getTarget() == this.revengeGoal.held()) { // orig :468-470 — the roll spent every pass, the clear only on the revenge goal's own target (a goal-held player pick is not a stored attacker; refuter A)
                this.setTarget(null);
                this.revengeGoal.release();                                  // 1.7.10's task ended on a nulled attack target; vanilla's TargetGoal would re-assert its memory (ENT-S-129)
            }
            LivingEntity current = stored;                                   // orig :471 — `if (e == null)` tests the pass-local e, not a re-read of the slot: a stored attacker (the :96-98 store, the :68 revenge task) stands and the scan does not run — the one the 1-in-200 just cleared included (a re-read scanned in the clearing pass; T3b refuter B, D1); the scan's own pick was never stored (:471-473), so it is re-derived every pass under the mark (ENT-S-108 / ENT-S-129)
            if (current == null || current == this.scanPick) {
                LivingEntity pick = this.findSomethingToAttack();            // orig :472 — the 20/8/20 box, sorted, the first the filter accepts (ENT-S-135)
                if (pick != current) super.setTarget(pick);                  // super: the scan's own set keeps its ownership; replaced, or cleared when the scan came back empty (orig :497-499 stood down)
                // Re-read the slot rather than trusting `pick`: a LivingChangeTargetEvent handler may have substituted or
                // cancelled the set, and a stale scanPick would stall the scan (ENT-S-108 refuter hardening, 2026-09-04).
                this.scanPick = this.getTarget();
            }
        }

        if (this.cobwebCooldown > 0) --this.cobwebCooldown;

        LivingEntity target = this.getTarget();
        if (target instanceof Player chased && this.cobwebCooldown == 0
                && this.distanceToSqr(chased) < 144.0
                && this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
            tryDropCobwebUnder(chased);
            this.cobwebCooldown = COBWEB_INTERVAL_TICKS;
        }

        // orig CaterKiller.java:502-530 — tree-eat heal: triggers 1-in-8 while
        // damaged or 1-in-30 otherwise, gated on PlayNicely==0; nearest tree
        // block within 12 blocks; eaten at distSq<81 for a flat 2.0 heal.
        boolean eatRoll = (this.random.nextInt(8) == 0 && this.getHealth() < this.getMaxHealth())
                || this.random.nextInt(30) == 0;
        if (eatRoll && !OreSpawnConfig.PLAY_NICELY.get()) {
            BlockPos food = findNearestTreeBlock();
            if (food != null) {
                if (target == null) {
                    this.getNavigation().moveTo(food.getX(), food.getY(), food.getZ(), 1.0);
                }
                if (food.distSqr(this.blockPosition().above()) < 81.0) {
                    if (this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
                        this.level().setBlock(food, Blocks.AIR.defaultBlockState(), 2);
                    }
                    this.heal(2.0f);
                    if (this.random.nextInt(20) == 1) {
                        this.playSound(SoundEvents.PLAYER_BURP, 1.0f,
                                this.random.nextFloat() * 0.2f + 0.9f);
                    }
                }
            }
        }
    }

    /**
     * orig CaterKiller.java:559-574 {@code findSomethingToAttack}: nothing under PlayNicely (:560-562); every
     * {@code EntityLivingBase} whose box meets the Cater Killer's box grown by 20/8/20 (:563, {@code getEntitiesWithinAABB} —
     * every living thing, where HEAD's goal scanned players only, and a BOX where that was a FOLLOW_RANGE 40 sphere); sorted
     * by the {@link GenericTargetSorter} (:564); the first the filter accepts wins (:565-572), else null (:573).
     * {@link TargetSelection#firstMatch} is that sort-and-loop, stable ties included (OPT-021). ENT-S-135.
     */
    @Nullable
    private LivingEntity findSomethingToAttack() {
        if (OreSpawnConfig.PLAY_NICELY.get()) return null;                        // orig :560-562
        List<LivingEntity> candidates = this.level().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(20.0, 8.0, 20.0));                  // orig :563
        return TargetSelection.firstMatch(candidates, this.targetSorter, this::isSuitableTarget); // orig :564-572
    }

    /**
     * orig CaterKiller.java:533-557 {@code isSuitableTarget}, in the original's order: null / self / dead (:534-542), the
     * Cater Killer's own sight test {@link #myCanSee} (:543-545 — not vanilla's eye ray), then the player branch — creative
     * refused (:546-549, {@code isCreativeMode} = {@code Abilities.instabuild}, the ENT-S-107 mapping ENT-S-132 carried on
     * the goal) — a Cater Killer refused AFTER the player branch (:550-552), any {@code EntityMob} taken (:553-555, the port's
     * {@code Monster}), and the shared attackable-non-mob grant list last (:556, {@link MyUtils#isAttackableNonMob}, orig's
     * membership since ENT-S-128). ENT-S-135.
     */
    private boolean isSuitableTarget(LivingEntity target) {
        if (target == null || target == this || !target.isAlive()) return false;    // orig :534-542
        if (!this.myCanSee(target)) return false;                                   // orig :543-545 — MyCanSee, the hand-rolled block walk
        if (target instanceof Player player) return !player.getAbilities().instabuild; // orig :546-549
        if (target instanceof EntityCaterKiller) return false;                      // orig :550-552 — after the player branch
        if (target instanceof Monster) return true;                                 // orig :553-555 (EntityMob)
        return MyUtils.isAttackableNonMob(target);                                  // orig :556
    }

    /**
     * orig CaterKiller.java:626-676 {@code MyCanSee} — the Cater Killer's own sight test, a hand-rolled block march (the
     * King's / Queen's / Molenoid's class of walk, ITEM-070's): from a point 2.5 blocks ahead of the body along the entity
     * yaw (:627, :629-630) at y + 3 (:632) toward the target's mid-height (:634-636), ten steps normalised so no axis advances
     * more than one block per step (:637-669), each sample cast with {@code (int)} — truncation toward zero, the original's
     * own (BUG-027, VERIFIED-CORRECT faithful; MOD-024 lists the floor as a modern opt-in) — and passed only through air,
     * cobweb, tallgrass and leaves (:672: {@code Blocks.air}, {@code web} → COBWEB, {@code tallgrass} → SHORT_GRASS + FERN
     * per the Molenoid / Camarasaurus precedent, and {@code Blocks.leaves} — 1.7.10's block of the oak / spruce / birch /
     * jungle variants; its sibling {@code leaves2} (acacia, dark oak) is not on the list, so those and the modern leaf types
     * occlude the walk, as in 1.7.10 — a literal transcription, disclosed). Vanilla's eye-to-eye ray (the ENT-S-121
     * convention) never stood here: HEAD's goal read {@code Sensing}. ENT-S-135.
     */
    private boolean myCanSee(LivingEntity e) {
        double xzoff = 2.5;                                                  // orig :627
        int nblks = 10;                                                      // orig :628
        double cx = this.getX() - xzoff * Math.sin(Math.toRadians(this.getYRot())); // orig :629
        double cz = this.getZ() + xzoff * Math.cos(Math.toRadians(this.getYRot())); // orig :630
        float startx = (float) cx;                                           // orig :631
        float starty = (float) (this.getY() + 3.0);                          // orig :632
        float startz = (float) cz;                                           // orig :633
        float dx = (float) ((e.getX() - (double) startx) / 10.0);            // orig :634
        float dy = (float) ((e.getY() + (double) (e.getBbHeight() / 2.0f) - (double) starty) / 10.0); // orig :635
        float dz = (float) ((e.getZ() - (double) startz) / 10.0);            // orig :636
        if ((double) Math.abs(dx) > 1.0) {                                   // orig :637-647
            dy /= Math.abs(dx);
            dz /= Math.abs(dx);
            nblks = (int) ((float) nblks * Math.abs(dx));
            if (dx > 1.0f) dx = 1.0f;
            if (dx < -1.0f) dx = -1.0f;
        }
        if ((double) Math.abs(dy) > 1.0) {                                   // orig :648-658
            dx /= Math.abs(dy);
            dz /= Math.abs(dy);
            nblks = (int) ((float) nblks * Math.abs(dy));
            if (dy > 1.0f) dy = 1.0f;
            if (dy < -1.0f) dy = -1.0f;
        }
        if ((double) Math.abs(dz) > 1.0) {                                   // orig :659-669
            dy /= Math.abs(dz);
            dx /= Math.abs(dz);
            nblks = (int) ((float) nblks * Math.abs(dz));
            if (dz > 1.0f) dz = 1.0f;
            if (dz < -1.0f) dz = -1.0f;
        }
        for (int i = 0; i < nblks; ++i) {                                    // orig :670-674
            startx += dx;
            starty += dy;
            startz += dz;
            BlockState state = this.level().getBlockState(new BlockPos((int) startx, (int) starty, (int) startz)); // orig :671 — pre-increment, then the (int) cast (BUG-027)
            if (state.isAir() || state.is(Blocks.COBWEB) || state.is(Blocks.SHORT_GRASS) || state.is(Blocks.FERN)
                    || state.is(Blocks.OAK_LEAVES) || state.is(Blocks.SPRUCE_LEAVES) || state.is(Blocks.BIRCH_LEAVES)
                    || state.is(Blocks.JUNGLE_LEAVES)) continue;                                                     // orig :672 — air, web, tallgrass, leaves
            return false;                                                    // orig :673
        }
        return true;                                                         // orig :675
    }

    /**
     * orig CaterKiller.java:380-427 {@code scan_it} — nearest leaf/vine/log
     * block in an expanding box capped at 12 horizontally / 9 vertically
     * around the head (y+1). Flat nearest-block scan over the same volume.
     */
    @Nullable
    private BlockPos findNearestTreeBlock() {
        BlockPos origin = this.blockPosition().above();
        BlockPos best = null;
        int bestDistSq = Integer.MAX_VALUE;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dx = -12; dx <= 12; ++dx) {
            for (int dy = -9; dy <= 9; ++dy) {
                for (int dz = -12; dz <= 12; ++dz) {
                    int distSq = dx * dx + dy * dy + dz * dz;
                    if (distSq >= bestDistSq) continue;
                    cursor.setWithOffset(origin, dx, dy, dz);
                    BlockState state = this.level().getBlockState(cursor);
                    if (state.is(BlockTags.LEAVES) || state.is(BlockTags.LOGS)
                            || state.is(Blocks.VINE)) {
                        bestDistSq = distSq;
                        best = cursor.immutable();
                    }
                }
            }
        }
        return best;
    }

    /**
     * Spit a cobweb into the floor tile directly under the chased player.
     * Only fires on solid-floor tiles with an air block immediately above
     * (so we don't overwrite the player's standing tile or melt structural
     * blocks). The cobweb naturally despawns to nothing on player break.
     */
    private void tryDropCobwebUnder(Player chased) {
        if (!(this.level() instanceof ServerLevel server)) return;
        BlockPos under = chased.blockPosition();
        BlockState atFeet = server.getBlockState(under);
        if (!atFeet.isAir() && atFeet.getBlock() != Blocks.COBWEB) return;
        BlockPos floor = under.below();
        BlockState floorState = server.getBlockState(floor);
        if (floorState.isAir() || !floorState.getFluidState().isEmpty()) return;
        server.setBlock(under, Blocks.COBWEB.defaultBlockState(), 3);
    }

    // Item drops are data-driven via loot_table/entities/cater_killer.json
    // (orig CaterKiller.java:160-324: jaw, painting, 10 leather, 6 raw beef,
    // 1-5 rolls of the d20 Ruby/Ultimate gear table). Non-item death
    // behavior (butterfly swarm) stays in die() below.
    @Override
    public void die(DamageSource cause) {
        // orig CaterKiller.java:325-327 — 25 Butterflies released on death.
        if (!this.level().isClientSide && this.level() instanceof ServerLevel serverLevel) {
            for (int i = 0; i < 25; i++) {
                EntityButterfly bf = ModEntities.ENTITY_BUTTERFLY.get().create(serverLevel);
                if (bf == null) continue;
                bf.moveTo(this.getX(), this.getY() + 1.0, this.getZ(),
                        this.random.nextFloat() * 360.0f, 0.0f);
                serverLevel.addFreshEntity(bf);
            }
        }
        super.die(cause);
    }

    /** orig CaterKiller.java:585-624 — spawner bypass; y>=50; 1-in-10 dice; daytime; air/leaves/logs clearance above; no other CaterKiller within 48/16/48. */
    @Override
    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level,
                                   net.minecraft.world.entity.MobSpawnType spawnType) {
        if (OriginalSpawnGates.nearOwnSpawner(this, level)) return true;
        if (this.getY() < 50.0) return false;
        if (this.getRandom().nextInt(10) != 0) return false;
        if (!OriginalSpawnGates.isDaytime(level)) return false;
        if (!OriginalSpawnGates.boxMatches(this, level, -1, 1, 1, 4, -1, 1,
                s -> s.isAir() || s.is(net.minecraft.tags.BlockTags.LEAVES) || s.is(net.minecraft.tags.BlockTags.LOGS))) return false;
        return !OriginalSpawnGates.anyOtherNearby(this, level, EntityCaterKiller.class, 48.0, 16.0, 48.0);
    }
}
