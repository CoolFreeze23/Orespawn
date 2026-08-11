package danger.orespawn.entity;

import danger.orespawn.MobStats;

import danger.orespawn.ModEntities;
import danger.orespawn.ModItems;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.ai.DinosaurMeleeAttackGoal;
import danger.orespawn.entity.ai.OwnerFollowAnyNavGoal;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.SmoothSwimmingLookControl;
import net.minecraft.world.entity.ai.control.SmoothSwimmingMoveControl;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;

public class WaterDragon extends TamableAnimal {
    // OPT-011: cached SoundEvents — identical createVariableRangeEvent ids,
    // allocated once per class instead of on every sound query.
    private static final SoundEvent SND_WATERDRAGON_HURT = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "waterdragon_hurt"));
    private static final SoundEvent SND_WATERDRAGON_DEATH = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "waterdragon_death"));
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(WaterDragon.class, EntityDataSerializers.INT);

    private static final double MOVE_SPEED_IN_WATER = 0.55;
    private static final double MOVE_SPEED_OUT_OF_WATER = 0.25;

    private int hurtTimer = 0;
    private int closestWaterDistance = 99999;
    private int targetX = 0, targetY = 0, targetZ = 0;

    /**
     * OPT-009: the speed genuinely varies (water/land), so the per-tick write
     * stays, but the AttributeInstance is resolved once instead of via a map
     * lookup every tick. Attribute instances live exactly as long as the entity
     * (a dimension change constructs a fresh entity), so this cannot go stale.
     */
    private final net.minecraft.world.entity.ai.attributes.AttributeInstance movementSpeedAttribute;

    public WaterDragon(EntityType<? extends WaterDragon> type, Level level) {
        super(type, level);
        this.movementSpeedAttribute = this.getAttribute(Attributes.MOVEMENT_SPEED);
        this.xpReward = 100;
        // Smooth swimming control mirrors vanilla Dolphin/Turtle idioms and
        // is a 1:1 behavioural upgrade from the 1.7.10 EntityAISwimming.
        this.moveControl = new SmoothSwimmingMoveControl(this, 85, 10, 0.02f, 0.1f, true);
        this.lookControl = new SmoothSwimmingLookControl(this, 10);
        this.setPathfindingMalus(PathType.WATER, 0.0f);
    }

    // AI: tamed WaterDragons follow and breed like a pet; wild ones bite on
    // retaliation via the WaterCanonAttackGoal (the TF-026 waterDragon melee
    // preset plus the restored orig ranged watercanon branch, ENT-S-074).
    // RandomSwimmingGoal keeps them gliding through water when idle rather
    // than beaching themselves against currents.
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new BreedGoal(this, 1.0));
        // TF-001/TEST-005 — vanilla FollowOwnerGoal's ctor rejects our
        // WaterBoundPathNavigation (1.21 addition), making the entity
        // unspawnable; OwnerFollowAnyNavGoal is the same goal minus that gate.
        // orig WaterDragon.java:71 — MyEntityAIFollowOwner(this, 2.0f, 10.0f, 2.0f).
        this.goalSelector.addGoal(2, new OwnerFollowAnyNavGoal(this, 2.0, 10.0f, 2.0f));
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.2, Ingredient.of(Items.COD), false));
        this.goalSelector.addGoal(4, new WaterCanonAttackGoal());
        this.goalSelector.addGoal(5, new RandomSwimmingGoal(this, 1.0, 30));
        this.goalSelector.addGoal(6, new MyEntityAIWanderALot(this, 16, 1.0));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        // orig OreSpawnMain.java:6492 — WaterDragon 150 HP / 20 ATK / 8 armor
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, MobStats.WATER_DRAGON.maxHealth())
                .add(Attributes.MOVEMENT_SPEED, MOVE_SPEED_IN_WATER)
                .add(Attributes.ATTACK_DAMAGE, MobStats.WATER_DRAGON.attackDamage())
                .add(Attributes.ARMOR, MobStats.WATER_DRAGON.armor())
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new WaterBoundPathNavigation(this, level);
    }

    // 1.21.1 makes LivingEntity#canBreatheUnderwater final — NeoForge
    // routes the override through IEntityExtension#canDrownInFluidType.
    @Override
    public boolean canDrownInFluidType(net.neoforged.neoforge.fluids.FluidType type) {
        return false;
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public void travel(net.minecraft.world.phys.Vec3 vec) {
        if (this.isEffectiveAi() && this.isInWater()) {
            this.moveRelative(this.getSpeed(), vec);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.9));
            if (this.getTarget() == null) {
                this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.005, 0.0));
            }
        } else {
            super.travel(vec);
        }
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
    public void aiStep() {
        super.aiStep();
        // OPT-009: cached instance; setBaseValue itself no-ops when unchanged.
        this.movementSpeedAttribute.setBaseValue(
                this.isInWater() ? MOVE_SPEED_IN_WATER : MOVE_SPEED_OUT_OF_WATER);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (super.mobInteract(player, hand).consumesAction()) {
            return InteractionResult.SUCCESS;
        }

        if (stack.is(Items.COD) && this.distanceToSqr(player) < 25.0) {
            if (!this.isTame()) {
                if (!this.level().isClientSide) {
                    if (this.random.nextInt(3) == 0) {
                        this.tame(player);
                        this.level().broadcastEntityEvent(this, (byte) 7);
                        this.heal(this.getMaxHealth() - this.getHealth());
                    } else {
                        this.level().broadcastEntityEvent(this, (byte) 6);
                    }
                }
            } else if (this.isOwnedBy(player)) {
                if (!this.level().isClientSide) {
                    this.level().broadcastEntityEvent(this, (byte) 7);
                }
                this.heal(this.getMaxHealth() - this.getHealth());
            }
            if (!player.getAbilities().instabuild) stack.shrink(1);
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        if (this.isTame() && stack.is(Blocks.GLASS.asItem())
                && this.distanceToSqr(player) < 25.0 && this.isOwnedBy(player)) {
            if (!this.level().isClientSide) {
                this.setTame(false, false);
                this.setOwnerUUID(null);
                this.setOrderedToSit(false);
                this.setInSittingPose(false);
                this.level().broadcastEntityEvent(this, (byte) 6);
            }
            if (!player.getAbilities().instabuild) stack.shrink(1);
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        if (this.isTame() && stack.is(Items.NAME_TAG)
                && this.distanceToSqr(player) < 16.0 && this.isOwnedBy(player)) {
            this.setCustomName(stack.getHoverName());
            if (!player.getAbilities().instabuild) stack.shrink(1);
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        if (this.isTame() && this.isOwnedBy(player) && this.distanceToSqr(player) < 25.0) {
            this.setOrderedToSit(!this.isOrderedToSit());
            this.setInSittingPose(this.isOrderedToSit());
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        return InteractionResult.PASS;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = target.hurt(this.damageSources().mobAttack(this),
                (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE));
        if (hit && target instanceof LivingEntity) {
            double knockbackStrength = 1.1;
            double upwardKnockback = 0.14;
            float angleToTarget = (float) Math.atan2(target.getZ() - this.getZ(), target.getX() - this.getX());
            if (target instanceof Player) upwardKnockback *= 2.0;
            target.push(Math.cos(angleToTarget) * knockbackStrength, upwardKnockback, Math.sin(angleToTarget) * knockbackStrength);
            return true;
        }
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.type().msgId().equals("cactus")) return false;
        Entity attacker = source.getEntity();
        if (attacker instanceof WaterDragon) return false;
        if (attacker instanceof AttackSquid) return false;
        // orig WaterDragon.java:476-478 — WaterBall-sourced damage is ignored,
        // so watercanon volleys (ENT-S-074) never turn dragons on each other.
        if (attacker instanceof WaterBall) return false;
        boolean ret = false;
        if (this.hurtTimer <= 0) {
            ret = super.hurt(source, amount);
            this.hurtTimer = 10;
        }
        if (attacker instanceof Mob mob) {
            this.setTarget(mob);
            this.getNavigation().moveTo(mob, 1.2);
        }
        return ret;
    }

    // Dry-out + water-seek behaviour is kept outside the new Goal because
    // it is essentially a "meta" survival loop (scan for the nearest water
    // block, limp-walk toward it, chip damage if nothing found). Moving
    // this to an independent goal would duplicate target-mutation state.
    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;
        super.customServerAiStep();

        if (this.hurtTimer > 0) --this.hurtTimer;

        if (!this.isInWater() && this.random.nextInt(25) == 0 && !this.isOrderedToSit()) {
            this.closestWaterDistance = 99999;
            this.targetX = 0; this.targetY = 0; this.targetZ = 0;
            for (int i = 1; i < 12; ++i) {
                int j = Math.min(i, 10);
                if (this.scanForWater((int) this.getX(), (int) this.getY() - 1, (int) this.getZ(), i, j, i)) break;
                if (i >= 5) ++i;
            }
            if (this.closestWaterDistance < 99999) {
                this.getNavigation().moveTo(this.targetX, this.targetY - 1, this.targetZ, 1.33);
            } else {
                if (this.random.nextInt(50) == 1) {
                    this.hurt(this.damageSources().dryOut(), 1.0f);
                }
                if (this.getHealth() <= 0.0f) {
                    this.discard();
                    return;
                }
            }
        }

        if (this.random.nextInt(100) == 1 && this.isInWater() && this.getHealth() < this.getMaxHealth()) {
            this.playSound(SoundEvents.GENERIC_SPLASH, 1.5f, this.random.nextFloat() * 0.2f + 0.9f);
            this.heal(1.0f);
        }
    }

    private boolean scanForWater(int x, int y, int z, int dx, int dy, int dz) {
        int found = 0;
        for (int i = -dy; i <= dy; ++i) for (int j = -dz; j <= dz; ++j) {
            found += checkWaterAt(x + dx, y + i, z + j, dx * dx + j * j + i * i);
            found += checkWaterAt(x - dx, y + i, z + j, dx * dx + j * j + i * i);
        }
        for (int i = -dx; i <= dx; ++i) for (int j = -dz; j <= dz; ++j) {
            found += checkWaterAt(x + i, y + dy, z + j, dy * dy + j * j + i * i);
            found += checkWaterAt(x + i, y - dy, z + j, dy * dy + j * j + i * i);
        }
        for (int i = -dx; i <= dx; ++i) for (int j = -dy; j <= dy; ++j) {
            found += checkWaterAt(x + i, y + j, z + dz, dz * dz + j * j + i * i);
            found += checkWaterAt(x + i, y + j, z - dz, dz * dz + j * j + i * i);
        }
        return found != 0;
    }

    private int checkWaterAt(int x, int y, int z, int dist) {
        BlockState state = this.level().getBlockState(new BlockPos(x, y, z));
        if (state.is(Blocks.WATER) && dist < this.closestWaterDistance) {
            this.closestWaterDistance = dist; this.targetX = x; this.targetY = y; this.targetZ = z;
            return 1;
        }
        return 0;
    }

    // Death drops are fully data-driven via loot_table/entities/water_dragon.json
    // (orig WaterDragon.java:278-445: scale, painting, 9-14 raw fish,
    // one d20 roll of the Ultimate/Iron gear table).

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() { return null; }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SND_WATERDRAGON_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SND_WATERDRAGON_DEATH;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(ModItems.CRYSTAL_APPLE.get());
    }

    /** orig WaterDragon.java:716-739 — "Water Dragon" spawner bypass; y>=50; daytime; no other WaterDragon within 16/5/16. */
    @Override
    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level,
                                   net.minecraft.world.entity.MobSpawnType spawnType) {
        if (OriginalSpawnGates.nearOwnSpawner(this, level)) return true;
        if (this.getY() < 50.0) return false;
        if (!OriginalSpawnGates.isDaytime(level)) return false;
        return !OriginalSpawnGates.anyOtherNearby(this, level, WaterDragon.class, 16.0, 5.0, 16.0);
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        WaterDragon baby = new WaterDragon(ModEntities.WATER_DRAGON.get(), level);
        UUID ownerUUID = this.getOwnerUUID();
        if (ownerUUID != null) {
            baby.setOwnerUUID(ownerUUID);
            baby.setTame(true, true);
        }
        return baby;
    }

    /**
     * ENT-S-074 — orig WaterDragon.java:597-648. The original single AI loop
     * mixed melee and ranged: past melee reach the dragon kept walking toward
     * the target at speed 1.0 (orig :607) AND ran watercanon() on the same
     * 1-in-5 cadence tick (orig :597,:608). The TF-001/TF-026 goal rebuild kept
     * only the melee half; this subclass restores the ranged half through the
     * {@code onOutOfMeleeRange} hook (the SpitBugAcidAttackGoal integration
     * pattern) so the rebuilt goal stack — and the nav-agnostic owner-follow —
     * stay untouched.
     */
    private class WaterCanonAttackGoal extends DinosaurMeleeAttackGoal {

        /** orig WaterDragon.java:52 — stream_count, the 8-round burst counter. */
        private int streamCount = 0;

        WaterCanonAttackGoal() {
            super(WaterDragon.this, WaterDragon.this::setAttacking,
                    DinosaurMeleeAttackGoal.Presets.waterDragon());
        }

        /** orig WaterDragon.java:620-648 — watercanon(e). */
        @Override
        protected void onOutOfMeleeRange(LivingEntity target, double distSq) {
            WaterDragon dragon = WaterDragon.this;
            double yoff = 1.75; // orig :621
            double xzoff = 1.5; // orig :622
            if (this.streamCount > 0) {
                dragon.setAttacking(2); // orig :625 — pose 2 opens the jaw (ModelWaterDragon)
                // orig :626-631 — 1 round in 15 adds a SmallFireball whose accel
                // is computed from the dragon's CENTER before the muzzle
                // reposition; both muzzle offsets use the head yaw here.
                if (dragon.random.nextInt(15) == 1) {
                    SmallFireball fireball = new SmallFireball(dragon.level(), dragon,
                            new Vec3(target.getX() - dragon.getX(),
                                    target.getY() + 0.75 - (dragon.getY() + yoff),
                                    target.getZ() - dragon.getZ()));
                    fireball.moveTo(
                            dragon.getX() - xzoff * Math.sin(Math.toRadians(dragon.yHeadRot)),
                            dragon.getY() + yoff,
                            dragon.getZ() + xzoff * Math.cos(Math.toRadians(dragon.yHeadRot)),
                            dragon.getYRot(), dragon.getXRot()); // orig :628
                    // orig :629 — "random.bow" 0.75f
                    dragon.level().playSound(null, dragon, SoundEvents.ARROW_SHOOT,
                            dragon.getSoundSource(), 0.75f,
                            1.0f / (dragon.random.nextFloat() * 0.4f + 0.8f));
                    dragon.level().addFreshEntity(fireball);
                }
                // orig :632-633 — every round fires an OWNERLESS WaterBall (the
                // 3-arg ctor has no shooter, exactly why hurt() exempts
                // WaterBall sources). The ctor receives the aim delta as a
                // throwaway position, immediately overwritten by moveTo; the
                // muzzle x offset uses the HEAD yaw but the z offset uses the
                // BODY yaw (orig field_70759_as vs field_70177_z) — an original
                // asymmetry kept bug-for-bug.
                WaterBall ball = new WaterBall(dragon.level(),
                        target.getX() - dragon.getX(),
                        target.getY() + 0.75 - (dragon.getY() + yoff),
                        target.getZ() - dragon.getZ());
                ball.moveTo(
                        dragon.getX() - xzoff * Math.sin(Math.toRadians(dragon.yHeadRot)),
                        dragon.getY() + yoff,
                        dragon.getZ() + xzoff * Math.cos(Math.toRadians(dragon.getYRot())),
                        dragon.yHeadRot, dragon.getXRot());
                // orig :634-638 — ballistic lift (horizontal distance * 0.2),
                // then shoot at the target's y+0.25 with 1.4 speed / 5.0 spread.
                double dx = target.getX() - ball.getX();
                double dy = target.getY() + 0.25 - ball.getY();
                double dz = target.getZ() - ball.getZ();
                float lift = Mth.sqrt((float) (dx * dx + dz * dz)) * 0.2f;
                ball.shoot(dx, dy + lift, dz, 1.4f, 5.0f);
                // orig :639 — "random.bow" 0.75f
                dragon.level().playSound(null, dragon, SoundEvents.ARROW_SHOOT,
                        dragon.getSoundSource(), 0.75f,
                        1.0f / (dragon.random.nextFloat() * 0.4f + 0.8f));
                dragon.level().addFreshEntity(ball);
                --this.streamCount; // orig :641
            } else {
                dragon.setAttacking(0); // orig :643
            }
            // orig :645-647 — an empty canon reloads 8 rounds 1 time in 4,
            // checked even on the tick the burst just ran dry.
            if (this.streamCount <= 0 && dragon.random.nextInt(4) == 1) {
                this.streamCount = 8;
            }
        }
    }
}
