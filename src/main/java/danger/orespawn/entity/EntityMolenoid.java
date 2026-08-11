package danger.orespawn.entity;

import danger.orespawn.MobStats;
import danger.orespawn.ModBlocks;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.entity.ai.GenericTargetSorter;
import danger.orespawn.util.MyUtils;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.ai.TargetSelection;

public class EntityMolenoid extends Monster {
    // OPT-011: cached SoundEvents — identical createVariableRangeEvent ids,
    // allocated once per class instead of on every sound query.
    private static final SoundEvent SND_MOLENOID_LIVING = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "molenoid_living"));
    private static final SoundEvent SND_MOLENOID_HIT = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "molenoid_hit"));
    private static final SoundEvent SND_MOLENOID_DEATH = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "molenoid_death"));
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(EntityMolenoid.class, EntityDataSerializers.INT);

    private static final double KNOCKBACK_STRENGTH = 0.8;
    private static final double KNOCKBACK_VERTICAL = 0.1;
    private static final double KNOCKBACK_VERTICAL_PLAYER_MULT = 2.0;

    private final float moveSpeed = 0.35f;

    public EntityMolenoid(EntityType<? extends EntityMolenoid> type, Level level) {
        super(type, level);
        // OPT-009: constant speed - assert the attribute base once here instead
        // of re-writing it every tick (same value the removed per-tick call set).
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(this.moveSpeed);
        this.xpReward = 40;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MyEntityAIWanderALot(this, 16, 1.0));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        // orig OreSpawnMain.java:6478 — Molenoid 200 HP / 18 ATK / 12 armor;
        // speed 0.35 matches orig Molenoid.java:39.
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, MobStats.MOLENOID.maxHealth())
                .add(Attributes.MOVEMENT_SPEED, 0.35)
                .add(Attributes.ATTACK_DAMAGE, MobStats.MOLENOID.attackDamage())
                .add(Attributes.ARMOR, MobStats.MOLENOID.armor())
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

    public void setAttacking(int val) {
        this.entityData.set(DATA_ATTACKING, val);
    }

    @Override
    public boolean removeWhenFarAway(double dist) {
        return !this.isPersistenceRequired();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getMsgId().equals("inWall")) {
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean ret = super.doHurtTarget(target);
        if (ret && target instanceof LivingEntity) {
            double verticalKnockback = KNOCKBACK_VERTICAL;
            float angle = (float) Math.atan2(target.getZ() - this.getZ(), target.getX() - this.getX());
            if (target.isRemoved() || target instanceof Player) {
                verticalKnockback *= KNOCKBACK_VERTICAL_PLAYER_MULT;
            }
            target.push(
                    Math.cos(angle) * KNOCKBACK_STRENGTH,
                    verticalKnockback,
                    Math.sin(angle) * KNOCKBACK_STRENGTH);
        }
        return ret;
    }

    @Override
    protected void customServerAiStep() {
        // orig Molenoid.java:170 — the target is method-scoped on purpose: the
        // dig-clear "dir" below (orig :230-238) reads whatever THIS tick's
        // 1-in-4 targeting roll found, and stays null on the other ticks.
        LivingEntity target = null;
        if (this.isRemoved()) return;
        super.customServerAiStep();

        if (this.random.nextInt(4) == 0) {
            target = this.findSomethingToAttack();
            if (target != null) {
                this.lookAt(target, 10.0f, 10.0f); // orig :178
                double distSq = this.distanceToSqr(target);
                float attackRange = 6.0f + target.getBbWidth() / 2.0f; // orig :179
                if (distSq < (double)(attackRange * attackRange)) {
                    this.setAttacking(1);
                    // orig :181 — double dice roll for the melee bite inside 4 blocks.
                    if (distSq < 16.0 && (this.random.nextInt(4) == 0 || this.random.nextInt(5) == 1)) {
                        this.doHurtTarget(target);
                    } else if (!OreSpawnConfig.PLAY_NICELY.get()) {
                        // orig :183 — the block lob is gated on PlayNicely only;
                        // the original never consults mobGriefing here.
                        throwBlocksAtTarget(target);
                    }
                } else {
                    this.getNavigation().moveTo(target, 1.25); // orig :198
                }
            } else {
                this.setAttacking(0);
            }
        }

        // orig :204-206 client-side early return — customServerAiStep already
        // only runs server-side, so the world mutation below is safe.

        // orig :207-225 — digging spoil: on a coin flip, with probability
        // proportional to current ground speed (odds = 100*spd/moveSpeed,
        // orig :208-214), kick up a mole-dirt mound ~6 blocks BEHIND the head
        // (+6*sin/-6*cos is the reverse of the yaw look vector, orig :215-216)
        // with +-3 jitter, snapped onto the first air-over-solid seam scanning
        // y+5 down to y-3 (orig :219-223). PlayNicely-gated, never
        // mobGriefing-gated.
        if (this.random.nextInt(2) == 0) {
            Vec3 motion = this.getDeltaMovement();
            double spd = Math.sqrt(motion.x * motion.x + motion.z * motion.z); // orig :209-211
            if (spd > (double) this.moveSpeed) {
                spd = this.moveSpeed; // orig :211-213
            }
            int odds = (int)(100.0 * spd / (double) this.moveSpeed); // orig :214
            if (odds > 0 && this.random.nextInt(100) < odds && !OreSpawnConfig.PLAY_NICELY.get()) {
                double dx = this.getX() + 6.0 * Math.sin(Math.toRadians(this.yHeadRot)); // orig :215
                double dz = this.getZ() - 6.0 * Math.cos(Math.toRadians(this.yHeadRot)); // orig :216
                dx += (this.random.nextFloat() - this.random.nextFloat()) * 3.0;
                dz += (this.random.nextFloat() - this.random.nextFloat()) * 3.0;
                for (int i = 4; i > -4; i--) { // orig :219
                    BlockPos above = BlockPos.containing(dx, this.getY() + i + 1, dz);
                    BlockPos at = BlockPos.containing(dx, this.getY() + i, dz);
                    if (!this.level().getBlockState(above).isAir()
                            || this.level().getBlockState(at).isAir()) continue;
                    // orig :221 — MyMoleDirtBlock via func_147449_b (default flag 3).
                    this.level().setBlock(above, ModBlocks.MOLE_DIRT.get().defaultBlockState(), 3);
                    break;
                }
            }
        }

        clearPathAhead(target);
    }

    /**
     * orig Molenoid.java:183-196 — 1-4 lobs of MOLE DIRT (not vanilla dirt)
     * land on the surface around the target: x/z jitter +-2 (orig :186-189),
     * settled on the first air-over-solid seam scanning target.y+5 down to
     * target.y-2 (orig :190-193). The caller holds the PlayNicely gate
     * (orig :183); mobGriefing is intentionally NOT checked — the original
     * griefs regardless (the port's old mobGriefing gate and vanilla DIRT
     * were inventions, removed for ENT-K-035).
     */
    private void throwBlocksAtTarget(LivingEntity target) {
        int count = 1 + this.random.nextInt(4); // orig :184
        for (int k = 0; k < count; k++) {
            double dx = target.getX() + (this.random.nextFloat() - this.random.nextFloat()) * 2.0;
            double dz = target.getZ() + (this.random.nextFloat() - this.random.nextFloat()) * 2.0;
            for (int i = 4; i > -3; i--) { // orig :190
                BlockPos checkAbove = BlockPos.containing(dx, target.getY() + i + 1, dz);
                BlockPos checkAt = BlockPos.containing(dx, target.getY() + i, dz);
                if (this.level().getBlockState(checkAbove).isAir() && !this.level().getBlockState(checkAt).isAir()) {
                    // orig :192 — MyMoleDirtBlock via func_147449_b (default flag 3).
                    this.level().setBlock(checkAbove, ModBlocks.MOLE_DIRT.get().defaultBlockState(), 3);
                    break;
                }
            }
        }
    }

    /**
     * orig Molenoid.java:226-248 — the actual digging: every AI tick a 3-high
     * column ~3 blocks AHEAD of the head (-3*sin/+3*cos IS the yaw look
     * vector; ENT-K-035 — the port had named this "clearPathBehind" and never
     * placed spoil) with +-3 jitter is cleared. The column base follows this
     * tick's target: offsets y+1..y+3 by default (dir=1, orig :230), y+2..y+4
     * when the target is higher (dir=2, orig :232-234), y+0..y+2 when lower
     * (dir=0, orig :235-237) — so the mole digs up or down toward prey.
     * Natural cover — dirt, grass block, gravel, sand and classic leaves
     * (orig :242) — clears only with mobGriefing on; its own mole dirt clears
     * unconditionally (orig :245-246). The whole section is PlayNicely-gated
     * (orig :239). field_150362_t is the old 4-variant leaves block —
     * oak/spruce/birch/jungle only; acacia/dark oak lived in leaves2 and are
     * NOT cleared (quirk kept, same mapping as Camarasaurus.isEdibleBlock).
     */
    private void clearPathAhead(@Nullable LivingEntity target) {
        double dx = this.getX() - 3.0 * Math.sin(Math.toRadians(this.yHeadRot)); // orig :226
        double dz = this.getZ() + 3.0 * Math.cos(Math.toRadians(this.yHeadRot)); // orig :227
        dx += (this.random.nextFloat() - this.random.nextFloat()) * 3.0;
        dz += (this.random.nextFloat() - this.random.nextFloat()) * 3.0;

        int dir = 1; // orig :230
        if (target != null) {
            if ((int) target.getY() > (int) this.getY()) dir = 2; // orig :232-234
            if ((int) target.getY() < (int) this.getY()) dir = 0; // orig :235-237
        }
        if (OreSpawnConfig.PLAY_NICELY.get()) return; // orig :239
        for (int i = dir; i < dir + 3; i++) { // orig :240
            BlockPos pos = BlockPos.containing(dx, this.getY() + i, dz);
            BlockState state = this.level().getBlockState(pos);
            if ((state.is(Blocks.DIRT) || state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.GRAVEL)
                    || state.is(Blocks.SAND) || state.is(Blocks.OAK_LEAVES) || state.is(Blocks.SPRUCE_LEAVES)
                    || state.is(Blocks.BIRCH_LEAVES) || state.is(Blocks.JUNGLE_LEAVES))
                    && this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
                this.level().setBlock(pos, Blocks.AIR.defaultBlockState(), 3); // orig :242-244
            }
            if (state.is(ModBlocks.MOLE_DIRT.get())) {
                this.level().setBlock(pos, Blocks.AIR.defaultBlockState(), 3); // orig :245-246, no mobGriefing gate
            }
        }
    }

    /**
     * orig Molenoid.java:251-275 — prey: non-creative players (:264-267,
     * decided before any species branch), any monster (:271-273), and the
     * shared attackable-non-mob boss list (:274); fellow Molenoids never
     * (:268-270). Everything else — animals, villagers — is IGNORED (the
     * port's old blanket "return true" was an invention). The sight check is
     * MyCanSee (:261-263), not vanilla line-of-sight: the mole sees through
     * diggable ground, which is what makes it dig toward buried/surface prey
     * at all (ENT-K-035 behavior chain).
     */
    private boolean isSuitableTarget(LivingEntity target) {
        if (target == null || target == this || !target.isAlive()) return false;
        if (!this.myCanSee(target)) return false;
        // orig :264-267 — field_75098_d is isCreativeMode, i.e. instabuild.
        if (target instanceof Player player) {
            return !player.getAbilities().instabuild;
        }
        if (target instanceof EntityMolenoid) return false;
        if (target instanceof Monster) return true; // orig :271-273 (EntityMob)
        return MyUtils.isAttackableNonMob(target);  // orig :274
    }

    private LivingEntity findSomethingToAttack() {
        // orig Molenoid.java:278-280 — PlayNicely disables aggression entirely.
        if (OreSpawnConfig.PLAY_NICELY.get()) return null;
        AABB searchBox = this.getBoundingBox().inflate(12.0, 6.0, 12.0);
        List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, searchBox);
        // TF-035: orig Molenoid.java:38,47 — targets sort with GenericTargetSorter
        // (creeper-halved / big-silhouette-first, sorted at :282), not plain distance.
        // OPT-021: nearest-first pick without the full list sort; TargetSelection
        // preserves the removed sort's order and stable-tie semantics exactly.
        return TargetSelection.firstMatch(targets, new GenericTargetSorter(this), this::isSuitableTarget);
    }

    /**
     * orig Molenoid.java:344-394 — MyCanSee: a hand-rolled block march from a
     * point 2 blocks ahead of the body (entity yaw, orig :347-348) toward the
     * target's mid-height, ~10 steps normalized so no axis advances more than
     * 1 block per step (orig :355-387), treating air, mole dirt, dirt, grass
     * block, tallgrass, sand and gravel as transparent (orig :390). Vanilla
     * line-of-sight would blind a buried mole; this raycast is why it can
     * "smell" prey through the very ground clearPathAhead digs.
     * field_150329_H (tallgrass incl. fern) maps to SHORT_GRASS + FERN per
     * the Camarasaurus precedent.
     */
    private boolean myCanSee(LivingEntity e) {
        double xzoff = 2.0; // orig :345
        int nblks = 10;     // orig :346
        double cx = this.getX() - xzoff * Math.sin(Math.toRadians(this.getYRot())); // orig :347
        double cz = this.getZ() + xzoff * Math.cos(Math.toRadians(this.getYRot())); // orig :348
        float startx = (float) cx;
        float starty = (float)(this.getY() + 1.0); // orig :350
        float startz = (float) cz;
        float dx = (float)((e.getX() - (double) startx) / 10.0);
        float dy = (float)((e.getY() + (double)(e.getBbHeight() / 2.0f) - (double) starty) / 10.0);
        float dz = (float)((e.getZ() - (double) startz) / 10.0);
        if ((double) Math.abs(dx) > 1.0) { // orig :355-365
            dy /= Math.abs(dx);
            dz /= Math.abs(dx);
            nblks = (int)((float) nblks * Math.abs(dx));
            if (dx > 1.0f) dx = 1.0f;
            if (dx < -1.0f) dx = -1.0f;
        }
        if ((double) Math.abs(dy) > 1.0) { // orig :366-376
            dx /= Math.abs(dy);
            dz /= Math.abs(dy);
            nblks = (int)((float) nblks * Math.abs(dy));
            if (dy > 1.0f) dy = 1.0f;
            if (dy < -1.0f) dy = -1.0f;
        }
        if ((double) Math.abs(dz) > 1.0) { // orig :377-387
            dy /= Math.abs(dz);
            dx /= Math.abs(dz);
            nblks = (int)((float) nblks * Math.abs(dz));
            if (dz > 1.0f) dz = 1.0f;
            if (dz < -1.0f) dz = -1.0f;
        }
        for (int i = 0; i < nblks; ++i) { // orig :388-392
            startx += dx;
            starty += dy;
            startz += dz;
            BlockState state = this.level().getBlockState(BlockPos.containing(startx, starty, startz));
            if (state.isAir() || state.is(ModBlocks.MOLE_DIRT.get()) || state.is(Blocks.DIRT)
                    || state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.SHORT_GRASS) || state.is(Blocks.FERN)
                    || state.is(Blocks.SAND) || state.is(Blocks.GRAVEL)) continue;
            return false;
        }
        return true;
    }

    // Death drops are fully data-driven via loot_table/entities/molenoid.json
    // (orig Molenoid.java:125-135: molenoid nose, painting, 10 gold nugget, 6 raw beef).

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        if (this.random.nextInt(3) == 0) {
            return SND_MOLENOID_LIVING;
        }
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SND_MOLENOID_HIT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SND_MOLENOID_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 1.1f;
    }

    /** orig Molenoid.java:303-342 — spawner bypass; darkness; y>=50; night; clear air above; no other Molenoid within 16/8/16. */
    @Override
    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level,
                                   net.minecraft.world.entity.MobSpawnType spawnType) {
        if (OriginalSpawnGates.nearOwnSpawner(this, level)) return true;
        if (!OriginalSpawnGates.isDarkEnough(this, level)) return false;
        if (this.getY() < 50.0) return false;
        if (OriginalSpawnGates.isDaytime(level)) return false;
        if (!OriginalSpawnGates.airBox(this, level, -1, 0, 1, 3, -1, 0)) return false;
        return !OriginalSpawnGates.anyOtherNearby(this, level, EntityMolenoid.class, 16.0, 8.0, 16.0);
    }
}
