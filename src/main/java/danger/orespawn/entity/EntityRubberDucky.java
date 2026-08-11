package danger.orespawn.entity;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;

public class EntityRubberDucky extends TamableAnimal {
    // OPT-011: cached SoundEvents — identical createVariableRangeEvent ids,
    // allocated once per class instead of on every sound query.
    private static final SoundEvent SND_DUCK_HURT = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "duck_hurt"));
    private static final EntityDataAccessor<Integer> DATA_KILL_COUNT =
            SynchedEntityData.defineId(EntityRubberDucky.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(EntityRubberDucky.class, EntityDataSerializers.INT);

    private static final int NO_CLOSEST_MATCH = 99999;

    private int killCount = 0;
    private int died = 0;
    /**
     * orig RubberDucky.java:51 {@code buddy} — another duck adopted at 1-in-10
     * while scanning targets (orig :446-448); the duck drifts toward it (orig
     * :404-412). Cleared on every fresh target scan (orig :470). Not persisted,
     * matching the original.
     */
    @Nullable
    private LivingEntity buddy = null;
    private int closestWaterDistSq = NO_CLOSEST_MATCH;
    private int targetX = 0;
    private int targetY = 0;
    private int targetZ = 0;

    public EntityRubberDucky(EntityType<? extends EntityRubberDucky> type, Level level) {
        super(type, level);
        // OPT-009: constant speed - assert the attribute base once here instead
        // of re-writing it every tick (same value the removed per-tick call set).
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.22);
        this.xpReward = 15;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(2, new FollowOwnerGoal(this, 2.0, 10.0f, 2.0f));
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.25, Ingredient.of(Items.COD), false)); // orig RubberDucky.java:242 raw fish (field_151115_aP)
        this.goalSelector.addGoal(4, new MyEntityAIWanderALot(this, 16, 1.0));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, LivingEntity.class, 6.0f));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 5.0)
                .add(Attributes.MOVEMENT_SPEED, 0.22)
                .add(Attributes.ATTACK_DAMAGE, 6.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_KILL_COUNT, 0);
        builder.define(DATA_ATTACKING, 0);
    }

    public int getKillCount() {
        return this.entityData.get(DATA_KILL_COUNT);
    }

    public void setKillCount(int val) {
        this.entityData.set(DATA_KILL_COUNT, val);
        this.killCount = val;
    }

    public int getAttacking() {
        return this.entityData.get(DATA_ATTACKING);
    }

    public void setAttacking(int val) {
        this.entityData.set(DATA_ATTACKING, val);
    }

    @Override
    public boolean removeWhenFarAway(double dist) {
        if (this.isBaby()) return false;
        if (this.isPersistenceRequired()) return false;
        if (this.isTame()) return false;
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.isInWater()) {
            Vec3 motion = this.getDeltaMovement();
            double newY = motion.y + 0.1;
            if (newY < -0.05) newY = -0.05;
            this.setDeltaMovement(motion.x, newY, motion.z);
        }
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        Entity attacker = source.getEntity();
        boolean ret = super.hurt(source, amount);
        this.setOrderedToSit(false);

        if (!this.level().isClientSide && attacker instanceof Player &&
                (this.isDeadOrDying() || this.getHealth() <= 0) && this.died == 0) {
            this.died = 1;
            ++this.killCount;
            this.setKillCount(this.killCount);
            if (this.killCount < 10) {
                for (int m = 0; m < 20; m++) {
                    int i = this.random.nextInt(3);
                    if (this.random.nextInt(2) == 1) i = -i;
                    int k = this.random.nextInt(3);
                    if (this.random.nextInt(2) == 1) k = -k;
                    for (int j = 3; j > -3; j--) {
                        BlockPos above = BlockPos.containing(this.getX() + i, this.getY() + j + 1, this.getZ() + k);
                        BlockPos at = BlockPos.containing(this.getX() + i, this.getY() + j, this.getZ() + k);
                        if (this.level().getBlockState(above).isAir() && !this.level().getBlockState(at).isAir()) {
                            if (this.level() instanceof ServerLevel serverLevel) {
                                EntityRubberDucky duck = ModEntities.ENTITY_RUBBER_DUCKY.get().create(serverLevel);
                                if (duck != null) {
                                    duck.moveTo(this.getX() + i + 1, this.getY() + j + 1, this.getZ() + k,
                                            this.random.nextFloat() * 360.0f, 0.0f);
                                    duck.setKillCount(this.killCount);
                                    serverLevel.addFreshEntity(duck);
                                }
                            }
                            return ret;
                        }
                    }
                }
            }
        }
        return ret;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        // orig RubberDucky.java:242-272 — raw fish (field_151115_aP) tames 1-in-2 / heals when tamed
        if (stack.is(Items.COD) && player.distanceToSqr(this) < 16.0) {
            if (!this.isTame()) {
                if (!this.level().isClientSide) {
                    if (this.random.nextInt(2) == 0) {
                        this.tame(player);
                        this.level().broadcastEntityEvent(this, (byte) 7);
                        this.heal(5.0f - this.getHealth());
                    } else {
                        this.level().broadcastEntityEvent(this, (byte) 6);
                    }
                }
            } else if (this.isOwnedBy(player)) {
                this.heal(5.0f - this.getHealth());
            }
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        // orig RubberDucky.java:273-287 — dead bush untames an owned duck
        if (this.isTame() && this.isOwnedBy(player)
                && stack.is(net.minecraft.world.level.block.Blocks.DEAD_BUSH.asItem())
                && player.distanceToSqr(this) < 16.0) {
            if (!this.level().isClientSide) {
                this.setTame(false, false);
                this.setOwnerUUID(null);
                this.setOrderedToSit(false);
                this.level().broadcastEntityEvent(this, (byte) 6);
            }
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        if (this.isTame() && this.isOwnedBy(player) && player.distanceToSqr(this) < 16.0) {
            this.setOrderedToSit(!this.isOrderedToSit());
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }

    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;
        super.customServerAiStep();

        if (!this.isInWater() && this.random.nextInt(50) == 0) {
            seekWater();
        }

        if (this.killCount > 0 && this.random.nextInt(200) == 1) {
            --this.killCount;
            this.setKillCount(this.killCount);
        }

        if (this.getHealth() < 5.0f && this.random.nextInt(300) == 1) {
            this.heal(1.0f);
        }

        if (this.level().getDifficulty() != Difficulty.PEACEFUL && this.random.nextInt(5) == 1) {
            LivingEntity target = this.findSomethingToAttack();
            if (target != null) {
                if (this.distanceToSqr(target) < 12.0) {
                    this.setAttacking(1);
                    if (this.random.nextInt(4) == 0 || this.random.nextInt(5) == 1) {
                        this.doHurtTarget(target);
                    }
                } else {
                    this.getNavigation().moveTo(target, 1.2);
                }
            } else {
                // orig RubberDucky.java:404-407 — with nothing to fight, a 1-in-15
                // roll paddles toward the buddy duck at speed 1.0.
                if (this.buddy != null && !this.buddy.isRemoved() && this.random.nextInt(15) == 1) {
                    this.getNavigation().moveTo(this.buddy, 1.0);
                }
                this.setAttacking(0);
            }
        }

        // orig RubberDucky.java:410-412 — independent 1-in-20 buddy-follow bias,
        // applied every AI tick even outside the combat roll.
        if (this.buddy != null && !this.buddy.isRemoved() && this.random.nextInt(20) == 1) {
            this.getNavigation().moveTo(this.buddy, 1.0);
        }
    }

    private void seekWater() {
        this.closestWaterDistSq = NO_CLOSEST_MATCH;
        this.targetX = this.targetY = this.targetZ = 0;
        for (int i = 1; i < 14; i++) {
            int j = Math.min(i, 5);
            if (scanForWater((int) this.getX(), (int) this.getY() - 1, (int) this.getZ(), i, j, i)) break;
            if (i >= 5) i++;
        }
        if (this.closestWaterDistSq < NO_CLOSEST_MATCH) {
            this.getNavigation().moveTo(this.targetX, this.targetY - 1, this.targetZ, 1.33);
        }
    }

    private boolean scanForWater(int x, int y, int z, int dx, int dy, int dz) {
        int found = 0;
        for (int i = -dy; i <= dy; i++) {
            for (int j = -dz; j <= dz; j++) {
                BlockPos pos = new BlockPos(x + dx, y + i, z + j);
                if (isWater(pos)) {
                    int distSqToWater = dx * dx + j * j + i * i;
                    if (distSqToWater < this.closestWaterDistSq) {
                        this.closestWaterDistSq = distSqToWater;
                        this.targetX = x + dx;
                        this.targetY = y + i;
                        this.targetZ = z + j;
                        found++;
                    }
                }
                pos = new BlockPos(x - dx, y + i, z + j);
                if (isWater(pos)) {
                    int distSqToWater = dx * dx + j * j + i * i;
                    if (distSqToWater < this.closestWaterDistSq) {
                        this.closestWaterDistSq = distSqToWater;
                        this.targetX = x - dx;
                        this.targetY = y + i;
                        this.targetZ = z + j;
                        found++;
                    }
                }
            }
        }
        return found != 0;
    }

    private boolean isWater(BlockPos pos) {
        return this.level().getBlockState(pos).is(Blocks.WATER);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        float attackDamage = this.getKillCount() >= 5 ? 2.0f : 1.0f;
        return target.hurt(this.damageSources().mobAttack(this), attackDamage);
    }

    /**
     * orig RubberDucky.java:424-454 — ducks always hunt squids: AttackSquid
     * (:440-442) and vanilla EntitySquid (:443-445) are suitable regardless of
     * kill count. A fellow RubberDucky is never a target, but a 1-in-10 roll
     * adopts it as the {@code buddy} (:446-448) — a side effect of the scan,
     * exactly as in the original. Players only become prey at killCount >= 5,
     * creative excepted (:449-452); everything else is ignored (:453).
     */
    private boolean isSuitableTarget(LivingEntity target) {
        if (this.level().getDifficulty() == Difficulty.PEACEFUL) return false; // orig :425-427
        if (target == null || target == this || !target.isAlive()) return false;
        if (!this.getSensing().hasLineOfSight(target)) return false;
        if (target instanceof AttackSquid) return true; // orig :440-442
        if (target instanceof Squid) return true;       // orig :443-445 EntitySquid
        if (target instanceof EntityRubberDucky && this.random.nextInt(10) == 1) {
            this.buddy = target; // orig :446-448 — adopt, do not attack
        }
        if (this.getKillCount() >= 5 && target instanceof Player player) {
            return !player.getAbilities().instabuild; // orig :449-452 creative check
        }
        return false;
    }

    private LivingEntity findSomethingToAttack() {
        // TF-035: orig RubberDucky.java:456-478 — PlayNicely disables duck
        // aggression entirely (:457-459), and the scan sorts with
        // GenericTargetSorter (field :49, ctor :69, sort :461), not plain
        // distance. The current attack target is kept while it lives (:465-468);
        // otherwise both it and the buddy are cleared before rescanning (:469-470).
        if (danger.orespawn.OreSpawnConfig.PLAY_NICELY.get()) return null;
        AABB searchBox = this.getBoundingBox().inflate(8.0, 4.0, 8.0);
        List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, searchBox);
        LivingEntity current = this.getTarget();
        if (current != null && current.isAlive()) return current;
        this.setTarget(null);
        this.buddy = null;
        // OPT-021: nearest-first pick without the full list sort (the removed
        // sort ran before the keep-current early-return but only ordered the
        // local list, so selecting here is observably identical); TargetSelection
        // preserves the sort's order and stable-tie semantics exactly.
        return danger.orespawn.entity.ai.TargetSelection.firstMatch(targets,
                new danger.orespawn.entity.ai.GenericTargetSorter(this), this::isSuitableTarget);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Killcount", this.killCount);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.killCount = tag.getInt("Killcount");
        this.setKillCount(this.killCount);
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.WHEAT);
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return new EntityRubberDucky(ModEntities.ENTITY_RUBBER_DUCKY.get(), level);
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        if (this.random.nextInt(10) == 1) {
            return SND_DUCK_HURT;
        }
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SND_DUCK_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SND_DUCK_HURT;
    }

    @Override
    protected float getSoundVolume() {
        return 0.8f;
    }

    /** orig RubberDucky.java:508-526 — "Rubber Ducky" spawner bypass; y>=50; daytime. */
    @Override
    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level,
                                   net.minecraft.world.entity.MobSpawnType spawnType) {
        if (OriginalSpawnGates.nearOwnSpawner(this, level)) return true;
        if (this.getY() < 50.0) return false;
        return OriginalSpawnGates.isDaytime(level);
    }
}
