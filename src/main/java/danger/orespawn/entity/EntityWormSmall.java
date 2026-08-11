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

public class EntityWormSmall extends Monster {
    // OPT-011: cached SoundEvents — identical createVariableRangeEvent ids,
    // allocated once per class instead of on every sound query.
    private static final SoundEvent SND_LITTLE_SPLAT = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "little_splat"));
    public int upcount = 50;
    public int downcount = 0;

    public EntityWormSmall(EntityType<? extends EntityWormSmall> type, Level level) {
        super(type, level);
        this.xpReward = 0;
        this.noPhysics = true;
    }

    public static AttributeSupplier.Builder createAttributes() {
        // orig OreSpawnMain.java:6504 — WormSmall 10 HP / 3 ATK / 0 armor
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, MobStats.WORM_SMALL.maxHealth())
                .add(Attributes.MOVEMENT_SPEED, 0.1)
                .add(Attributes.ATTACK_DAMAGE, MobStats.WORM_SMALL.attackDamage())
                .add(Attributes.ARMOR, MobStats.WORM_SMALL.armor());
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
        return null;
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
        this.setDeltaMovement(motion.x, motion.y * 0.75, motion.z);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide) return;

        Player target = this.level().getNearestPlayer(this, 8.0);

        // orig WormSmall.java:94 — the up/down burrow cycle also runs (without
        // aiming) when PlayNicely is enabled, even with no player nearby
        if (target != null || danger.orespawn.OreSpawnConfig.PLAY_NICELY.get()) {
            if (this.upcount > 0) {
                --this.upcount;
                if (this.upcount == 0) {
                    this.downcount = 100 + this.random.nextInt(150);
                }
                if (target != null) { // orig :101-103
                    this.pointAtEntity(target);
                }

                BlockPos checkPos = BlockPos.containing(this.getX(), this.getY() + 0.25, this.getZ());
                BlockState state = this.level().getBlockState(checkPos);
                if (!isAirOrTallGrass(state)) {
                    checkSurfaceBlock(state); // orig WormSmall.java:107-110
                    Vec3 motion = this.getDeltaMovement();
                    this.setDeltaMovement(motion.x, motion.y + 0.15, motion.z);
                    this.setPos(this.getX(), this.getY() + 0.1, this.getZ());
                }
            } else {
                if (this.downcount > 0) {
                    --this.downcount;
                } else {
                    this.upcount = 25 + this.random.nextInt(50);
                }
                BlockPos checkPos = BlockPos.containing(this.getX(), this.getY() + 2, this.getZ());
                BlockState state = this.level().getBlockState(checkPos);
                if (!isAirOrTallGrass(state)) {
                    checkSurfaceBlock(state); // orig WormSmall.java:124-127
                    Vec3 motion = this.getDeltaMovement();
                    this.setDeltaMovement(motion.x, motion.y + 0.2, motion.z);
                    this.setPos(this.getX(), this.getY() + 0.05, this.getZ());
                }
            }
        } else {
            this.upcount = this.random.nextInt(50);
            this.downcount = 0;
            BlockPos checkPos = BlockPos.containing(this.getX(), this.getY() + 2, this.getZ());
            BlockState state = this.level().getBlockState(checkPos);
            if (!isAirOrTallGrass(state)) {
                checkSurfaceBlock(state); // orig WormSmall.java:139-142
                Vec3 motion = this.getDeltaMovement();
                this.setDeltaMovement(motion.x, motion.y + 0.1, motion.z);
                this.setPos(this.getX(), this.getY() + 0.05, this.getZ());
            }
        }

        Vec3 motion = this.getDeltaMovement();
        this.setDeltaMovement(0, motion.y - 0.01, 0);
    }

    /** orig WormSmall.java:104-106 etc. — tall grass counts as air for surfacing. */
    private static boolean isAirOrTallGrass(BlockState state) {
        return state.isAir() || state.is(net.minecraft.world.level.block.Blocks.SHORT_GRASS);
    }

    /**
     * orig WormSmall.java:108-110/125-127/140-142 — a small worm that surfaces
     * through anything but grass block, dirt, or stone dies on the spot.
     */
    private void checkSurfaceBlock(BlockState state) {
        if (!state.is(net.minecraft.world.level.block.Blocks.GRASS_BLOCK)
                && !state.is(net.minecraft.world.level.block.Blocks.DIRT)
                && !state.is(net.minecraft.world.level.block.Blocks.STONE)) {
            this.discard();
        }
    }

    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;
        super.customServerAiStep();

        // orig WormSmall.java:176-178 — no thieving when PlayNicely
        if (danger.orespawn.OreSpawnConfig.PLAY_NICELY.get()) return;

        // orig :179 — func_72857_a: nearest player inside the bounding box
        // inflated 1.5/4.0/1.5. The 4-block VERTICAL reach matters (a player
        // standing on a ledge above the worm is still robbed), so a spherical
        // getNearestPlayer(1.5) is not equivalent. Spectators postdate 1.7.10
        // and are skipped, matching the vanilla NO_SPECTATORS query baseline.
        Player target = null;
        double bestDistSq = Double.MAX_VALUE;
        for (Player p : this.level().getEntitiesOfClass(Player.class,
                this.getBoundingBox().inflate(1.5, 4.0, 1.5))) {
            if (p.isSpectator()) continue;
            double distSq = this.distanceToSqr(p);
            if (distSq < bestDistSq) {
                bestDistSq = distSq;
                target = p;
            }
        }
        // orig :180-182 — creative players are nulled before any aiming
        if (target != null && !target.getAbilities().instabuild) {
            this.pointAtEntity(target);
            if (this.upcount > 0 && this.random.nextInt(15) == 1) {
                this.doHurtTarget(target);
                // orig :188-195 — 1-in-6: steal the boots, damage by
                // remainingDurability/20 (min 1), scatter 3 blocks up at ±0-4 x/z
                if (this.random.nextInt(6) == 1) {
                    net.minecraft.world.item.ItemStack boots =
                            target.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.FEET);
                    if (!boots.isEmpty()) {
                        target.setItemSlot(net.minecraft.world.entity.EquipmentSlot.FEET,
                                net.minecraft.world.item.ItemStack.EMPTY);
                        if (boots.isDamageableItem()) {
                            int remaining = boots.getMaxDamage() - boots.getDamageValue();
                            int hit = remaining > 20 ? remaining / 20 : 1;
                            boots.setDamageValue(boots.getDamageValue() + hit);
                        }
                        if (!boots.isDamageableItem() || boots.getDamageValue() < boots.getMaxDamage()) {
                            net.minecraft.world.entity.item.ItemEntity drop =
                                    new net.minecraft.world.entity.item.ItemEntity(this.level(),
                                            this.getX() + this.random.nextInt(5) - this.random.nextInt(5),
                                            this.getY() + 3.0,
                                            this.getZ() + this.random.nextInt(5) - this.random.nextInt(5),
                                            boots);
                            this.level().addFreshEntity(drop);
                        }
                    }
                }
            }
        }
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

    /** orig WormSmall.java:214-216 — night only. */
    @Override
    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level,
                                   net.minecraft.world.entity.MobSpawnType spawnType) {
        return !OriginalSpawnGates.isDaytime(level);
    }
}
