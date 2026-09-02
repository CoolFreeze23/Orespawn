package danger.orespawn.entity;

import danger.orespawn.MobStats;

import danger.orespawn.OreSpawnMod;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class EntityWormMedium extends Monster {
    // OPT-011: cached SoundEvents — identical createVariableRangeEvent ids,
    // allocated once per class instead of on every sound query.
    private static final SoundEvent SND_LITTLE_SPLAT = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "little_splat"));
    private static final SoundEvent SND_BIG_SPLAT = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "big_splat"));

    /**
     * OPT-007 (ruled apply 2026-08-11, NEUTRAL HALF ONLY): the per-tick
     * {@code TargetingConditions.forNonCombat()} allocation in aiStep is
     * hoisted to a shared static final. It is never mutated after construction
     * (no range()/selector() calls anywhere) and test() is stateless, so
     * sharing one instance across all medium worms is exact — the same pattern
     * vanilla uses for its own static TargetingConditions.
     * <p>The finding's other half — computing the small-worm/player scans once
     * per tick shared between aiStep and customServerAiStep — has no identical
     * query pair left in this class: the TF-035 vertical-reach rework made
     * customServerAiStep's queries (raw small-worm isEmpty in 8/8/8; nearest
     * non-spectator player in the 2.25/8.0/2.25 box) semantically different
     * from aiStep's forNonCombat nearest-small and spherical-8 player scans,
     * and merging them could change which worm/player is selected — not
     * neutral, so each site keeps its own scan. Both stay same-tick fresh:
     * the audit's every-2-4-ticks throttle was DECLINED by the ruling (worm
     * responsiveness is the contract).
     */
    private static final net.minecraft.world.entity.ai.targeting.TargetingConditions NON_COMBAT_TARGETING =
            net.minecraft.world.entity.ai.targeting.TargetingConditions.forNonCombat();

    public int upcount = 0;
    public int downcount = 0;

    public EntityWormMedium(EntityType<? extends EntityWormMedium> type, Level level) {
        super(type, level);
        this.xpReward = 0;
        this.noPhysics = true;
    }

    public static AttributeSupplier.Builder createAttributes() {
        // orig OreSpawnMain.java:6505 — WormMedium 30 HP / 10 ATK / 8 armor
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, MobStats.WORM_MEDIUM.maxHealth())
                .add(Attributes.MOVEMENT_SPEED, 0.1)
                .add(Attributes.ATTACK_DAMAGE, MobStats.WORM_MEDIUM.attackDamage())
                .add(Attributes.ARMOR, MobStats.WORM_MEDIUM.armor());
    }

    @Override
    protected float getSoundVolume() {
        return 0.5f;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SND_LITTLE_SPLAT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SND_BIG_SPLAT;
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    public void pointAtEntity(LivingEntity targetEntity) {
        double dx = targetEntity.getX() - this.getX();
        double dz = targetEntity.getZ() - this.getZ();
        float angle = (float) (Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0f;
        this.setYRot(angle);
        this.yBodyRot = angle;
    }

    @Override
    public void tick() {
        if (this.isVehicle()) {
            this.noPhysics = false;
        }
        super.tick();
        Vec3 motion = this.getDeltaMovement();
        this.setDeltaMovement(motion.x, motion.y * 0.65, motion.z);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide) return;

        EntityWormSmall nearbySmall = this.level().getNearestEntity(
                EntityWormSmall.class, NON_COMBAT_TARGETING, // OPT-007: hoisted static final
                this, this.getX(), this.getY(), this.getZ(),
                this.getBoundingBox().inflate(8.0));

        Player target = null;
        if (nearbySmall == null) {
            target = this.level().getNearestPlayer(this, 8.0);
        }

        if (nearbySmall == null && target != null) {
            if (this.upcount > 0) {
                --this.upcount;
                if (this.upcount == 0) {
                    this.downcount = 100 + this.random.nextInt(150);
                }
                this.pointAtEntity(target);

                BlockPos checkPos = BlockPos.containing(this.getX(), this.getY() + 0.25, this.getZ());
                BlockState state = this.level().getBlockState(checkPos);
                if (!state.isAir()) {
                    Vec3 motion = this.getDeltaMovement();
                    this.setDeltaMovement(motion.x, motion.y + 0.2, motion.z);
                    this.setPos(this.getX(), this.getY() + 0.1, this.getZ());
                }
            } else {
                if (this.downcount > 0) {
                    --this.downcount;
                } else {
                    this.upcount = 25 + this.random.nextInt(75);
                }
                BlockPos checkPos = BlockPos.containing(this.getX(), this.getY() + 3, this.getZ());
                BlockState state = this.level().getBlockState(checkPos);
                if (!state.isAir()) {
                    Vec3 motion = this.getDeltaMovement();
                    this.setDeltaMovement(motion.x, motion.y + 0.1, motion.z);
                    this.setPos(this.getX(), this.getY() + 0.05, this.getZ());
                }
            }
        } else {
            this.upcount = this.random.nextInt(50);
            this.downcount = 0;
            BlockPos checkPos = BlockPos.containing(this.getX(), this.getY() + 3, this.getZ());
            BlockState state = this.level().getBlockState(checkPos);
            if (!state.isAir()) {
                Vec3 motion = this.getDeltaMovement();
                this.setDeltaMovement(motion.x, motion.y + 0.1, motion.z);
                this.setPos(this.getX(), this.getY() + 0.05, this.getZ());
            }
        }

        Vec3 motion = this.getDeltaMovement();
        this.setDeltaMovement(0, motion.y - 0.01, 0);
    }

    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;
        super.customServerAiStep();

        // orig WormMedium.java:186-188 — no attacking/thieving when PlayNicely
        if (danger.orespawn.OreSpawnConfig.PLAY_NICELY.get()) return;

        // orig :189-192 — stand down while any small worm is within 8/8/8
        if (!this.level().getEntitiesOfClass(EntityWormSmall.class,
                this.getBoundingBox().inflate(8.0, 8.0, 8.0)).isEmpty()) {
            return;
        }

        // orig :193 — func_72857_a: nearest player inside the bounding box
        // inflated 2.25/8.0/2.25. The 8-block VERTICAL reach matters (the worm
        // robs players well above its 2-block body), so a spherical
        // getNearestPlayer(2.25) is not equivalent. Spectators postdate 1.7.10
        // and are skipped, matching the vanilla NO_SPECTATORS query baseline.
        Player target = null;
        double bestDistSq = Double.MAX_VALUE;
        for (Player p : this.level().getEntitiesOfClass(Player.class,
                this.getBoundingBox().inflate(2.25, 8.0, 2.25))) {
            if (p.isSpectator()) continue;
            double distSq = this.distanceToSqr(p);
            if (distSq < bestDistSq) {
                bestDistSq = distSq;
                target = p;
            }
        }
        // orig :194-196 — CREATIVE players are nulled (field_75098_d, i.e.
        // instabuild — not the invulnerable flag) before any aiming
        if (target != null && target.getAbilities().instabuild) {
            target = null;
        }
        if (target != null) {
            this.pointAtEntity(target); // orig :198
            // orig :199-200 — 1-in-15 per-tick swing while surfaced (upcount > 0)
            if (this.upcount > 0 && this.random.nextInt(15) == 1) {
                this.doHurtTarget(target);
                // orig :201-221 — 1-in-6 per swing: rip off the boots, or the
                // leggings when the feet slot is already bare
                if (this.random.nextInt(6) == 1) {
                    if (!stealAndScatter(target, net.minecraft.world.entity.EquipmentSlot.FEET)) {
                        stealAndScatter(target, net.minecraft.world.entity.EquipmentSlot.LEGS);
                    }
                }
            }
        }
    }

    /**
     * orig WormMedium.java:202-219 — the stolen stack is removed from the slot,
     * damaged by remainingDurability/15 (min 1, orig :205-206/:214-215), and
     * flung 3 blocks up at a ±0-4 x/z scatter (orig :208/:217). A stack that
     * breaks from the durability hit vanishes (orig func_77972_a zeroes the
     * stack before the EntityItem spawns) — same convention as WormSmall/20
     * and WormLarge/10.
     */
    private boolean stealAndScatter(Player target, net.minecraft.world.entity.EquipmentSlot slot) {
        net.minecraft.world.item.ItemStack stack = target.getItemBySlot(slot);
        if (stack.isEmpty()) return false;
        target.setItemSlot(slot, net.minecraft.world.item.ItemStack.EMPTY);
        if (stack.isDamageableItem()) {
            int remaining = stack.getMaxDamage() - stack.getDamageValue();
            int hit = remaining > 15 ? remaining / 15 : 1;
            stack.setDamageValue(stack.getDamageValue() + hit);
            if (stack.getDamageValue() >= stack.getMaxDamage()) {
                return true;
            }
        }
        net.minecraft.world.entity.item.ItemEntity drop = new net.minecraft.world.entity.item.ItemEntity(
                this.level(),
                this.getX() + this.random.nextInt(5) - this.random.nextInt(5),
                this.getY() + 3.0,
                this.getZ() + this.random.nextInt(5) - this.random.nextInt(5),
                stack);
        this.level().addFreshEntity(drop);
        return true;
    }

    @Override
    public boolean causeFallDamage(float dist, float mult, DamageSource source) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getMsgId().equals("inWall")) {
            return false;
        }
        return super.hurt(source, amount);
    }

    // Death drops are fully data-driven via loot_table/entities/worm_medium.json
    // (orig WormMedium.java:265-273: 2 rotten flesh, 2 leather).

    /** orig WormMedium.java:240-242 — night only. */
    @Override
    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level,
                                   net.minecraft.world.entity.MobSpawnType spawnType) {
        return !OriginalSpawnGates.isDaytime(level);
    }

    /** orig WormMedium.java:236 — func_145773_az -> true: never presses plates or tripwires (ENT-S-090). */
    @Override
    public boolean isIgnoringBlockTriggers() {
        return true;
    }
}
