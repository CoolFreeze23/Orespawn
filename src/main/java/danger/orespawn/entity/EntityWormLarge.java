package danger.orespawn.entity;

import danger.orespawn.MobStats;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class EntityWormLarge extends Monster {
    // OPT-011: cached SoundEvents — identical createVariableRangeEvent ids,
    // allocated once per class instead of on every sound query.
    private static final SoundEvent SND_BIG_SPLAT = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "big_splat"));
    private static final SoundEvent SND_ALO_DEATH = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "alo_death"));
    private int wormsSpawned = 0;

    // OPT-007 (ruled apply 2026-08-11, neutral half only): per-tick shared
    // nearest-player result; see nearestPlayerWithin8().
    @Nullable
    private Player cachedNearestPlayer = null;
    private int nearestPlayerScanTick = -1;

    public EntityWormLarge(EntityType<? extends EntityWormLarge> type, Level level) {
        super(type, level);
        this.xpReward = 2050;
        this.noPhysics = true;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        // orig OreSpawnMain.java:6506 — WormLarge 90 HP / 18 ATK / 14 armor
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, MobStats.WORM_LARGE.maxHealth())
                .add(Attributes.MOVEMENT_SPEED, 0.2)
                .add(Attributes.ATTACK_DAMAGE, MobStats.WORM_LARGE.attackDamage())
                .add(Attributes.ARMOR, MobStats.WORM_LARGE.armor());
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
        return SND_BIG_SPLAT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SND_ALO_DEATH;
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

    /**
     * OPT-007 (ruled apply 2026-08-11, NEUTRAL HALF ONLY): aiStep and
     * customServerAiStep both ran the IDENTICAL
     * {@code getNearestPlayer(this, 8.0)} scan every server tick; the result
     * is now computed once per tick — lazily, by whichever caller runs first
     * (customServerAiStep, inside super.aiStep(), unless its PlayNicely /
     * segment-nearby guards return early) — and shared by the other.
     * <p>Invalidation story: the tick stamp IS the invalidation — the cached
     * value can never survive into the next tick, so same-tick freshness is
     * preserved exactly, as the ruling requires. The audit's every-2-4-ticks
     * throttle was DECLINED by the same ruling (worm responsiveness is the
     * contract) and is deliberately not implemented. Only delta vs the old
     * duplicate scans: the second caller sees the first caller's result
     * instead of re-querying after the sub-block intra-tick travel movement.
     */
    @Nullable
    private Player nearestPlayerWithin8() {
        if (this.tickCount != this.nearestPlayerScanTick) {
            this.nearestPlayerScanTick = this.tickCount;
            this.cachedNearestPlayer = this.level().getNearestPlayer(this, 8.0);
        }
        return this.cachedNearestPlayer;
    }

    @Override
    public void tick() {
        if (this.isVehicle()) {
            this.noPhysics = false;
        }
        super.tick();
        Vec3 motion = this.getDeltaMovement();
        this.setDeltaMovement(motion.x, motion.y * 0.85, motion.z);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide) return;

        Player target = this.nearestPlayerWithin8(); // OPT-007: shared per-tick scan

        if (target != null) {
            this.pointAtEntity(target);
            BlockPos checkPos = BlockPos.containing(this.getX(), this.getY(), this.getZ());
            BlockState state = this.level().getBlockState(checkPos);
            if (!state.isAir()) {
                Vec3 motion = this.getDeltaMovement();
                this.setDeltaMovement(motion.x, motion.y + 0.25, motion.z);
                this.setPos(this.getX(), this.getY() + 0.1, this.getZ());
            } else {
                this.noPhysics = false;
            }
        } else {
            this.noPhysics = true;
            BlockPos checkPos = BlockPos.containing(this.getX(), this.getY() + 3.5, this.getZ());
            BlockState state = this.level().getBlockState(checkPos);
            if (!state.isAir()) {
                Vec3 motion = this.getDeltaMovement();
                this.setDeltaMovement(motion.x, motion.y + 0.1, motion.z);
                this.setPos(this.getX(), this.getY() + 0.05, this.getZ());
            }
        }

        if (this.noPhysics) {
            Vec3 motion = this.getDeltaMovement();
            this.setDeltaMovement(0, motion.y - 0.01, 0);
        }

        if (this.wormsSpawned == 0) {
            this.wormsSpawned = 1;
            if (this.level() instanceof ServerLevel serverLevel) {
                for (int i = 0; i < 20; ++i) {
                    spawnWorm(serverLevel, ModEntities.ENTITY_WORM_SMALL.get(),
                            this.getX() + this.random.nextInt(6) - this.random.nextInt(6),
                            this.getY(),
                            this.getZ() + this.random.nextInt(6) - this.random.nextInt(6));
                    spawnWorm(serverLevel, ModEntities.ENTITY_WORM_MEDIUM.get(),
                            this.getX() + this.random.nextInt(5) - this.random.nextInt(5),
                            this.getY(),
                            this.getZ() + this.random.nextInt(5) - this.random.nextInt(5));
                }
            }
        }
    }

    private void spawnWorm(ServerLevel level, EntityType<? extends Monster> type, double x, double y, double z) {
        Monster worm = type.create(level);
        if (worm != null) {
            worm.moveTo(x, y, z, this.random.nextFloat() * 360.0f, 0.0f);
            level.addFreshEntity(worm);
        }
    }

    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;
        if (!this.noPhysics) {
            super.customServerAiStep();
        }

        // orig WormLarge.java:192-198 — no hunting while PlayNicely, or while any
        // WormMedium segment is within 8 blocks
        if (danger.orespawn.OreSpawnConfig.PLAY_NICELY.get()) return;
        if (!this.level().getEntitiesOfClass(EntityWormMedium.class,
                this.getBoundingBox().inflate(8.0, 8.0, 8.0)).isEmpty()) {
            return;
        }

        // orig :199-202 — nearest non-creative player within 8 blocks
        Player target = this.nearestPlayerWithin8(); // OPT-007: shared per-tick scan
        if (target != null && !target.getAbilities().instabuild) {
            this.pointAtEntity(target);
            this.getNavigation().moveTo(target.getX(), target.getY(), target.getZ(), 1.0);
            if (this.random.nextInt(10) == 1 && this.distanceTo(target) < 3.0f) {
                this.doHurtTarget(target);
                // orig :210-230 — 1-in-4: steal the helmet, or the chestplate if bare-headed
                if (this.random.nextInt(4) == 1) {
                    if (!stealAndScatter(target, net.minecraft.world.entity.EquipmentSlot.HEAD)) {
                        stealAndScatter(target, net.minecraft.world.entity.EquipmentSlot.CHEST);
                    }
                }
                // orig :231-238 — independent 1-in-4: steal the held item
                if (this.random.nextInt(4) == 1) {
                    stealAndScatter(target, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
                }
            }
        }
    }

    /**
     * orig WormLarge.java:210-238 — the stolen stack is removed from the slot,
     * damaged by remainingDurability/10 (min 1), and flung 3 blocks up at a
     * ±0-4 x/z scatter. A stack that breaks from the durability hit vanishes
     * (orig func_77972_a zeroes the stack before the EntityItem spawns).
     */
    private boolean stealAndScatter(Player target, net.minecraft.world.entity.EquipmentSlot slot) {
        net.minecraft.world.item.ItemStack stack = target.getItemBySlot(slot);
        if (stack.isEmpty()) return false;
        target.setItemSlot(slot, net.minecraft.world.item.ItemStack.EMPTY);
        if (stack.isDamageableItem()) {
            int remaining = stack.getMaxDamage() - stack.getDamageValue();
            int hit = remaining > 10 ? remaining / 10 : 1;
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
        if (!this.noPhysics) {
            return super.causeFallDamage(dist, mult, source);
        }
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
        if (!this.noPhysics) {
            super.checkFallDamage(y, onGround, state, pos);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getMsgId().equals("inWall")) {
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("wormsSpawned", this.wormsSpawned);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.wormsSpawned = tag.getInt("wormsSpawned");
    }

    // Death drops are fully data-driven via loot_table/entities/worm_large.json
    // (orig WormLarge.java:352-377: worm tooth, painting, 6 rotten flesh, 6 leather,
    // 8 dirt, 16 gold nugget, 5 diamond, 4 uranium nugget, 4 titanium nugget).

    /**
     * orig WormLarge.java:263-309 — "Large Worm" spawner bypass (sets
     * {@code wormsSpawned} so the segment chain is not re-spawned); y&gt;=50; no
     * other WormLarge within 32/8/32; 13x13 column of SOLID ground from y-2 down
     * to y-8 (no air pockets); 13x13 column of pure air from y+2 up to y+8.
     */
    @Override
    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level, MobSpawnType spawnType) {
        if (OriginalSpawnGates.nearOwnSpawner(this, level)) {
            this.wormsSpawned = 1;
            return true;
        }
        if (this.getY() < 50.0) return false;
        if (OriginalSpawnGates.anyOtherNearby(this, level, EntityWormLarge.class, 32.0, 8.0, 32.0)) return false;
        if (!OriginalSpawnGates.boxMatches(this, level, -6, 6, -8, -2, -6, 6,
                s -> !s.isAir())) return false;
        return OriginalSpawnGates.airBox(this, level, -6, 6, 2, 8, -6, 6);
    }

    /** orig WormLarge.java:259 — func_145773_az -> true: never presses plates or tripwires (ENT-S-090). */
    @Override
    public boolean isIgnoringBlockTriggers() {
        return true;
    }
}
