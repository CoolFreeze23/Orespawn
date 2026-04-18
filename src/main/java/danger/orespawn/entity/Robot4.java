package danger.orespawn.entity;

import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
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
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.resources.ResourceLocation;
import danger.orespawn.OreSpawnMod;

/**
 * Robot4 — RoboPounder role.
 *
 * Heavy melee assault platform (750 HP, 40 ATK, 10 ARM) with a soft
 * shielding state ({@link #setShielding}) that fully nullifies one
 * incoming hit per attack window — emulating the 1.7.10 "armored stance"
 * pattern. Doubles vertical knockback on player hits so the slam reads
 * as a hammer-blow rather than a tap. The Pounder is also the source of
 * the throttled ground-tearing terrain destruction implemented in
 * {@link #aiStep()}: when actively attacking and adjacent to a non-air
 * block in front of it, it shatters the block once every
 * {@link #POUND_BLOCK_INTERVAL_TICKS} ticks (Mobzilla-style throttling
 * to keep TPS stable when several Pounders are active simultaneously).
 * Registry ID kept as "robot_4" for save compat.
 */
public class Robot4 extends Monster {
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(Robot4.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_SHIELDING =
            SynchedEntityData.defineId(Robot4.class, EntityDataSerializers.INT);

    private final Comparator<Entity> targetSorter;
    private int reloadTicker = 0;
    private int wasAttackedTicker = 0;
    private int poundCooldown = 0;
    private final float moveSpeed = 0.34f;

    /**
     * Number of ticks between successive ground-tear sweeps. Mobzilla's
     * own crushBlocks runs every server tick during the slam frame; for
     * the Pounder we space sweeps out to ~30 ticks (1.5 s) so a Pounder
     * army cannot turn a chunk into hashed-up cobblestone debris faster
     * than the chunk can replicate to clients. With ~5 Pounders active
     * and {@link #BLOCKS_PER_SWING} blocks per swing this caps at
     * ~33 setBlock calls/sec across the group, well below the natural
     * NeoForge chunk-update budget. Adjust if Pounders are ever spawned
     * in raid-scale waves.
     */
    private static final int POUND_BLOCK_INTERVAL_TICKS = 30;
    /** Blocks shattered per ground-tear sweep (front-arc, two-block tall). */
    private static final int BLOCKS_PER_SWING = 4;

    public Robot4(EntityType<? extends Robot4> type, Level level) {
        super(type, level);
        this.xpReward = 120;
        this.targetSorter = Comparator.comparingDouble(this::distanceToSqr);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 750.0)
                .add(Attributes.MOVEMENT_SPEED, 0.34)
                .add(Attributes.ATTACK_DAMAGE, 40.0)
                .add(Attributes.ARMOR, 10.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, 0);
        builder.define(DATA_SHIELDING, 0);
    }

    public int getAttacking() { return this.entityData.get(DATA_ATTACKING); }
    public void setAttacking(int value) { this.entityData.set(DATA_ATTACKING, value); }
    public int getShielding() { return this.entityData.get(DATA_SHIELDING); }
    public void setShielding(int value) { this.entityData.set(DATA_SHIELDING, value); }

    @Override
    public void jumpFromGround() {
        Vec3 velocity = this.getDeltaMovement();
        this.setDeltaMovement(velocity.x, velocity.y + 0.25, velocity.z);
        super.jumpFromGround();
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide()) {
            if (this.getRandom().nextInt(3) == 1) {
                double angle = Math.toRadians(this.getYRot() + 180.0f);
                this.level().addParticle(ParticleTypes.SMOKE,
                        getX() - 1.25 * Math.sin(angle), getY() + 3.0, getZ() + 1.25 * Math.cos(angle),
                        0, this.getRandom().nextFloat() / 2.0, 0);
            }
            return;
        }
        if (this.poundCooldown > 0) --this.poundCooldown;
        if (this.getAttacking() != 0 && this.poundCooldown == 0
                && this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
            poundGroundInFront();
            this.poundCooldown = POUND_BLOCK_INTERVAL_TICKS;
        }
    }

    /**
     * Throttled ground-tearing sweep. Selects up to {@link #BLOCKS_PER_SWING}
     * non-air, breakable blocks in the 3x2x3 column directly in front of the
     * Pounder (front arc derived from yRot, two-block tall to cover legs +
     * waist height) and shatters them with the standard "drop loot" path.
     *
     * Block whitelist mirrors {@link Godzilla#isCrushable}: bedrock,
     * end portal frames, and other unbreakable blocks are skipped so the
     * Pounder can't escape arenas or grief world boundaries. We also skip
     * tile entities (chests, spawners, beacons) to keep the destruction
     * "terrain only" -- losing player loot to a wandering robot would feel
     * brutal in a way 1.7.10 never intended.
     *
     * Called once per {@link #POUND_BLOCK_INTERVAL_TICKS} ticks per Pounder
     * (server-side only, gated behind doMobGriefing).
     */
    private void poundGroundInFront() {
        double yawRad = Math.toRadians(this.getYRot());
        double fx = -Math.sin(yawRad);
        double fz =  Math.cos(yawRad);
        // Anchor swing two blocks ahead at chest height -- the arms in the
        // 1.7.10 model swung roughly that far forward of the body pivot.
        double cx = this.getX() + fx * 2.0;
        double cz = this.getZ() + fz * 2.0;
        int baseY = (int) Math.floor(this.getY());
        int broken = 0;
        outer:
        for (int dy = 0; dy <= 1 && broken < BLOCKS_PER_SWING; dy++) {
            for (int dx = -1; dx <= 1 && broken < BLOCKS_PER_SWING; dx++) {
                for (int dz = -1; dz <= 1 && broken < BLOCKS_PER_SWING; dz++) {
                    BlockPos pos = BlockPos.containing(cx + dx, baseY + dy, cz + dz);
                    if (!isShatterable(pos)) continue;
                    Block block = this.level().getBlockState(pos).getBlock();
                    this.level().destroyBlock(pos, true, this);
                    // Replace with air explicitly in case destroyBlock is
                    // intercepted by a mod that converts the call to a
                    // no-op while still firing the event.
                    this.level().setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                    broken++;
                    if (broken >= BLOCKS_PER_SWING) break outer;
                    if (block == Blocks.OBSIDIAN) {
                        // Obsidian counts double against the swing budget --
                        // tougher block, fewer follow-up breaks per cycle.
                        broken++;
                    }
                }
            }
        }
    }

    /**
     * Mirrors Godzilla.isCrushable's intent: skip air, fluids, blocks with
     * a tile entity, and the standard "indestructible" set so Pounders stay
     * inside their intended arenas.
     */
    private boolean isShatterable(BlockPos pos) {
        var state = this.level().getBlockState(pos);
        if (state.isAir()) return false;
        if (!state.getFluidState().isEmpty()) return false;
        if (state.hasBlockEntity()) return false;
        float hardness = state.getDestroySpeed(this.level(), pos);
        if (hardness < 0.0f) return false; // bedrock / barrier
        Block b = state.getBlock();
        if (b == Blocks.BEDROCK || b == Blocks.BARRIER || b == Blocks.END_PORTAL_FRAME
                || b == Blocks.END_PORTAL || b == Blocks.NETHER_PORTAL
                || b == Blocks.COMMAND_BLOCK || b == Blocks.STRUCTURE_BLOCK
                || b == Blocks.STRUCTURE_VOID || b == Blocks.JIGSAW) return false;
        return true;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (target instanceof LivingEntity livingTarget) {
            double knockbackStrength = 2.0;
            double upwardKnockback = 0.12;
            float angleToTarget = (float) Math.atan2(livingTarget.getZ() - this.getZ(), livingTarget.getX() - this.getX());
            if (livingTarget.isRemoved() || livingTarget instanceof Player) upwardKnockback *= 2.0;
            livingTarget.push(Math.cos(angleToTarget) * knockbackStrength, upwardKnockback, Math.sin(angleToTarget) * knockbackStrength);
        }
        return super.doHurtTarget(target);
    }

    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;
        super.customServerAiStep();
        if (this.reloadTicker > 0) --this.reloadTicker;
        if (this.wasAttackedTicker > 0) --this.wasAttackedTicker;
        if (this.reloadTicker == 0 && this.getRandom().nextInt(8) == 1) {
            LivingEntity target = this.getTarget();
            if (this.getRandom().nextInt(50) == 1) this.setTarget(null);
            if (target != null && !target.isAlive()) { this.setTarget(null); target = null; }
            if (target == null) target = findSomethingToAttack();
            if (target != null) {
                this.lookAt(target, 10.0f, 10.0f);
                if (this.distanceToSqr(target) < 256.0) {
                    double meleeRange = (3.0f + target.getBbWidth() / 2.0f);
                    if (this.distanceToSqr(target) < meleeRange * meleeRange) {
                        this.doHurtTarget(target);
                    }
                    this.setAttacking(1);
                    this.reloadTicker = 10;
                    this.getNavigation().moveTo(target, 0.75);
                }
            }
        }
        if (this.reloadTicker <= 0 && this.wasAttackedTicker <= 0) {
            this.setAttacking(0);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getMsgId().equals("cactus")) return false;
        if (this.getShielding() != 0 || this.wasAttackedTicker != 0) return false;
        this.wasAttackedTicker = 65;
        this.setAttacking(1);
        boolean ret = super.hurt(source, amount);
        Entity attacker = source.getEntity();
        if (attacker instanceof Mob mob) {
            this.setTarget(mob);
            this.getNavigation().moveTo(mob, 1.2);
        }
        return ret;
    }

    private LivingEntity findSomethingToAttack() {
        AABB searchBox = this.getBoundingBox().inflate(16.0, 4.0, 16.0);
        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class, searchBox);
        entities.sort(this.targetSorter);
        for (LivingEntity e : entities) {
            if (isSuitableTarget(e)) return e;
        }
        return null;
    }

    private boolean isSuitableTarget(LivingEntity target) {
        if (target == null || target == this || !target.isAlive()) return false;
        if (target instanceof Monster) return false;
        if (target instanceof Player p && p.getAbilities().instabuild) return false;
        return true;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        if (this.getRandom().nextInt(4) == 0)
            return SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "robot_living"));
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource ds) {
        return SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "robot_hurt"));
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "robot_death"));
    }
}
