package danger.orespawn.entity;

import java.util.Comparator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.entity.MobSpawnType;
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
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.monster.CaveSpider;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.entity.ai.TargetSelection;

public class Lizard extends TamableAnimal {
    // OPT-011: cached SoundEvents — identical createVariableRangeEvent ids,
    // allocated once per class instead of on every sound query.
    private static final SoundEvent SND_ALO_HURT = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "alo_hurt"));
    private static final SoundEvent SND_ALO_DEATH = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "alo_death"));
    private static final EntityDataAccessor<Byte> ATTACKING =
            SynchedEntityData.defineId(Lizard.class, EntityDataSerializers.BYTE);

    private final Comparator<Entity> targetSorter;
    public boolean shouldDespawn = true;
    private LivingEntity buddy = null;
    private int followTime = 0;
    private static final int NO_WATER_FOUND_SENTINEL = 99999;

    private int closestWaterDistanceSq = NO_WATER_FOUND_SENTINEL;
    private int targetX = 0;
    private int targetY = 0;
    private int targetZ = 0;

    public Lizard(EntityType<? extends Lizard> type, Level level) {
        super(type, level);
        this.xpReward = 15;
        this.targetSorter = Comparator.comparingDouble(this::distanceToSqr);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new FollowOwnerGoal(this, 2.0, 10.0f, 2.0f));
        this.goalSelector.addGoal(2, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.25, Ingredient.of(Items.COD), false));
        this.goalSelector.addGoal(4, new MyEntityAIWanderALot(this, 16, 1.0));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ATTACKING, (byte) 0);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 30.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ATTACK_DAMAGE, 6.0);
    }

    public int getAttacking() { return this.entityData.get(ATTACKING); }
    public void setAttacking(int val) { this.entityData.set(ATTACKING, (byte) val); }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getMsgId().equals("cactus")) return false;
        boolean ret = super.hurt(source, amount);
        Entity attacker = source.getEntity();
        if (attacker instanceof LivingEntity livingAttacker) this.setLastHurtByMob(livingAttacker);
        this.followTime = 0;
        return ret;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        return target.hurt(this.damageSources().mobAttack(this), 6.0f);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.is(Items.COD) && this.distanceToSqr(player) < 16.0) {
            if (!this.level().isClientSide) {
                this.buddy = player;
                this.followTime = 3000 + this.random.nextInt(2000);
            }
            if (!player.getAbilities().instabuild) stack.shrink(1);
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    private boolean isSuitableTarget(LivingEntity target) {
        if (this.level().getDifficulty() == Difficulty.PEACEFUL) return false;
        if (target == null || target == this || !target.isAlive()) return false;
        if (!this.getSensing().hasLineOfSight(target)) return false; // orig Lizard.java:313-315 — canSee, after the dead check and ahead of the prey ladder (:316-327) (ENT-S-118)
        if (target instanceof AttackSquid) return true; // orig Lizard.java:316-318 — AttackSquid heads the prey ladder (ENT-S-128)
        if (target instanceof Spider) return true;
        if (target instanceof CaveSpider) return true;
        if (target instanceof Chicken) return true;
        return false;
    }

    private LivingEntity findSomethingToAttack() {
        if (OreSpawnConfig.PLAY_NICELY.get()) return null; // orig Lizard.java:336-338 — PlayNicely != 0 returns null ahead of the revenge-first block (:344-350) and the scan (ENT-S-115)
        LivingEntity revenge = this.getLastHurtByMob();
        if (this.random.nextInt(100) == 0) this.setLastHurtByMob(null);
        if (revenge != null && revenge.isAlive()) return revenge;
        this.setLastHurtByMob(null);

        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(12.0, 4.0, 12.0));
        // OPT-021: nearest-first pick without the full list sort; TargetSelection
        // preserves the removed sort's order and stable-tie semantics exactly.
        return TargetSelection.firstMatch(entities, this.targetSorter, this::isSuitableTarget);
    }

    // orig Lizard.java:175-236 scan_it seeks water/flowing_water blocks (field_150355_j/field_150358_i), not fire
    private boolean scanForWater(int x, int y, int z, int dx, int dy, int dz) {
        int found = 0;
        for (int i = -dy; i <= dy; i++) {
            for (int j = -dz; j <= dz; j++) {
                BlockState state = this.level().getBlockState(new BlockPos(x + dx, y + i, z + j));
                if (state.is(Blocks.WATER)) {
                    int distSq = dx * dx + j * j + i * i;
                    if (distSq < this.closestWaterDistanceSq) {
                        this.closestWaterDistanceSq = distSq;
                        this.targetX = x + dx;
                        this.targetY = y + i;
                        this.targetZ = z + j;
                        found++;
                    }
                }
                state = this.level().getBlockState(new BlockPos(x - dx, y + i, z + j));
                if (state.is(Blocks.WATER)) {
                    int distSq = dx * dx + j * j + i * i;
                    if (distSq < this.closestWaterDistanceSq) {
                        this.closestWaterDistanceSq = distSq;
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

    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;
        super.customServerAiStep();

        if (this.followTime > 0) { this.followTime--; this.shouldDespawn = false; }
        else { this.shouldDespawn = true; }

        if (!this.isInWater() && this.random.nextInt(100) == 0) {
            this.closestWaterDistanceSq = NO_WATER_FOUND_SENTINEL;
            this.targetX = 0;
            this.targetY = 0;
            this.targetZ = 0;
            for (int i = 1; i < 14; i++) {
                int j = Math.min(i, 5);
                if (this.scanForWater((int) this.getX(), (int) this.getY() - 1, (int) this.getZ(), i, j, i)) break;
                if (i >= 5) i++;
            }
            if (this.closestWaterDistanceSq < NO_WATER_FOUND_SENTINEL) {
                this.getNavigation().moveTo(this.targetX, this.targetY - 1, this.targetZ, 1.33);
            }
        }

        if (this.getHealth() < 30.0f && this.random.nextInt(300) == 1) this.heal(1.0f);

        if (this.level().getDifficulty() != Difficulty.PEACEFUL && this.random.nextInt(10) == 1) {
            LivingEntity prey = this.findSomethingToAttack();
            if (prey != null) {
                this.followTime = 0;
                if (this.distanceToSqr(prey) < 12.0) {
                    this.setAttacking(1);
                    if (this.random.nextInt(4) == 0 || this.random.nextInt(5) == 1) {
                        this.doHurtTarget(prey);
                    }
                } else {
                    this.getNavigation().moveTo(prey, 1.2);
                }
            } else {
                if (this.buddy != null && !this.buddy.isRemoved() && this.random.nextInt(15) == 1) {
                    this.getNavigation().moveTo(this.buddy, 1.0);
                }
                this.setAttacking(0);
            }
        }

        if (this.buddy != null && !this.buddy.isRemoved() && this.followTime > 0 && this.random.nextInt(20) == 1) {
            this.getNavigation().moveTo(this.buddy, 1.0);
        }
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() { return null; }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SND_ALO_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SND_ALO_DEATH;
    }

    @Override
    protected float getSoundVolume() { return 1.0f; }
    @Override
    public boolean isFood(ItemStack stack) { return stack.is(Items.COD); }

    @Override
    public boolean removeWhenFarAway(double dist) {
        if (this.isBaby()) return false;
        if (this.isPersistenceRequired()) return false;
        if (this.isTame()) return false;
        return this.shouldDespawn;
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnType) {
        return this.getY() >= 50.0;
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return new Lizard(ModEntities.LIZARD.get(), this.level());
    }
}
