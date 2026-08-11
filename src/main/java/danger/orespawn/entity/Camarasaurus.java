package danger.orespawn.entity;

import javax.annotation.Nullable;
import danger.orespawn.entity.ai.MoveIndoorsGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import danger.orespawn.OreSpawnMod;
public class Camarasaurus extends TamableAnimal {
    // OPT-011: cached SoundEvents — identical createVariableRangeEvent ids,
    // allocated once per class instead of on every sound query.
    private static final SoundEvent SND_CRYO_HURT = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "cryo_hurt"));
    private static final SoundEvent SND_CRYO_DEATH = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "cryo_death"));

    private static final int MAX_HEALTH = 20;
    private static final int NO_FOOD_FOUND_SENTINEL = 99999;
    private static final int GRAZE_INTERACTION_RANGE_SQ = 12;

    private final float moveSpeed = 0.2f;
    private int closestFoodDistSq = NO_FOOD_FOUND_SENTINEL;
    private int targetFoodX = 0;
    private int targetFoodY = 0;
    private int targetFoodZ = 0;

    public Camarasaurus(EntityType<? extends Camarasaurus> type, Level level) {
        super(type, level);
        // OPT-009: constant speed - assert the attribute base once here instead
        // of re-writing it every tick (same value the removed per-tick call set).
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(this.moveSpeed);
        this.xpReward = 5;
        this.setOrderedToSit(false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(2, new FollowOwnerGoal(this, 2.0, 10.0f, 2.0f));
        this.goalSelector.addGoal(3, new AvoidEntityGoal<>(this, Monster.class, 8.0f, 1.0, 1.4));
        this.goalSelector.addGoal(4, new TemptGoal(this, 1.2, Ingredient.of(Items.APPLE), false));
        this.goalSelector.addGoal(5, new PanicGoal(this, 1.5));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(7, new MyEntityAIWander(this, 1.0f));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        // orig Camarasaurus.java:62 — goal 9 EntityAIMoveIndoors (roofed-shelter
        // mapping documented in MoveIndoorsGoal). The orig registers NO target
        // goals (:53-62, tasks only) — a passive pet that never retaliates or
        // defends its owner, so no owner-combat/hurt-by goals here.
        this.goalSelector.addGoal(9, new MoveIndoorsGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.MOVEMENT_SPEED, 0.2)
                .add(Attributes.ATTACK_DAMAGE, 1.0);
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        float damage = fallDistance - 3.0f;
        if (damage > 0.0f) {
            if (damage > 2.0f) damage = 2.0f;
            this.hurt(this.damageSources().fall(), damage);
        }
        return false;
    }

    private boolean isEdibleBlock(Block block) {
        // orig Camarasaurus.java:114 — tree browser, not crop raider: leaves
        // (field_150362_t = oak/spruce/birch/jungle only; acacia/dark-oak lived
        // in leaves2 and were NOT edible — quirk kept), vine (field_150395_bd),
        // tallgrass incl. fern (field_150329_H), cactus (field_150434_aF) and
        // double_plant variants (field_150398_cm).
        return block == Blocks.OAK_LEAVES || block == Blocks.SPRUCE_LEAVES
                || block == Blocks.BIRCH_LEAVES || block == Blocks.JUNGLE_LEAVES
                || block == Blocks.VINE
                || block == Blocks.SHORT_GRASS || block == Blocks.FERN
                || block == Blocks.CACTUS
                || block == Blocks.SUNFLOWER || block == Blocks.LILAC
                || block == Blocks.ROSE_BUSH || block == Blocks.PEONY
                || block == Blocks.TALL_GRASS || block == Blocks.LARGE_FERN;
    }

    private boolean scanForFood(int x, int y, int z, int dx, int dy, int dz) {
        int found = 0;
        for (int i = -dy; i <= dy; ++i) {
            for (int j = -dz; j <= dz; ++j) {
                found += checkFoodAt(x + dx, y + i, z + j, dx * dx + j * j + i * i);
                found += checkFoodAt(x - dx, y + i, z + j, dx * dx + j * j + i * i);
            }
        }
        for (int i = -dx; i <= dx; ++i) {
            for (int j = -dz; j <= dz; ++j) {
                found += checkFoodAt(x + i, y + dy, z + j, dy * dy + j * j + i * i);
                found += checkFoodAt(x + i, y - dy, z + j, dy * dy + j * j + i * i);
            }
        }
        for (int i = -dx; i <= dx; ++i) {
            for (int j = -dy; j <= dy; ++j) {
                found += checkFoodAt(x + i, y + j, z + dz, dz * dz + j * j + i * i);
                found += checkFoodAt(x + i, y + j, z - dz, dz * dz + j * j + i * i);
            }
        }
        return found != 0;
    }

    private int checkFoodAt(int x, int y, int z, int dist) {
        BlockPos pos = new BlockPos(x, y, z);
        Block block = this.level().getBlockState(pos).getBlock();
        if (isEdibleBlock(block) && dist < this.closestFoodDistSq) {
            this.closestFoodDistSq = dist;
            this.targetFoodX = x;
            this.targetFoodY = y;
            this.targetFoodZ = z;
            return 1;
        }
        return 0;
    }

    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;

        if (this.random.nextInt(200) == 1) {
            this.setTarget(null);
        }
        super.customServerAiStep();

        if (!this.isOrderedToSit()) {
            boolean needsFood = this.random.nextInt(20) == 0
                    && (int) this.getHealth() < MAX_HEALTH;
            boolean randomGraze = this.random.nextInt(250) == 0;

            if (needsFood || randomGraze) {
                this.closestFoodDistSq = NO_FOOD_FOUND_SENTINEL;
                this.targetFoodX = 0;
                this.targetFoodY = 0;
                this.targetFoodZ = 0;
                for (int i = 1; i < 11; ++i) {
                    int j = Math.min(i, 2);
                    if (this.scanForFood(
                            (int) this.getX(), (int) this.getY() + 1, (int) this.getZ(),
                            i, j, i)) {
                        break;
                    }
                    if (i >= 6) ++i;
                }
                if (this.closestFoodDistSq < NO_FOOD_FOUND_SENTINEL) {
                    this.getNavigation().moveTo(this.targetFoodX, this.targetFoodY, this.targetFoodZ, 1.0);
                    if (this.closestFoodDistSq < GRAZE_INTERACTION_RANGE_SQ) {
                        if (this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
                            this.level().setBlock(
                                    new BlockPos(this.targetFoodX, this.targetFoodY, this.targetFoodZ),
                                    Blocks.AIR.defaultBlockState(), 2);
                        }
                        this.heal(1.0f);
                        this.playSound(SoundEvents.PLAYER_BURP, 1.0f,
                                this.random.nextFloat() * 0.2f + 0.9f);
                    }
                }
            }
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (stack.is(Items.APPLE) && this.distanceToSqr(player) < 16.0) {
            if (!this.isTame()) {
                if (!this.level().isClientSide) {
                    if (this.random.nextInt(2) == 0) {
                        this.tame(player);
                        this.level().broadcastEntityEvent(this, (byte) 7);
                        this.heal(MAX_HEALTH - this.getHealth());
                    } else {
                        this.level().broadcastEntityEvent(this, (byte) 6);
                    }
                }
            } else if (this.isOwnedBy(player)) {
                if (!this.level().isClientSide) {
                    this.level().broadcastEntityEvent(this, (byte) 7);
                }
                if (MAX_HEALTH > this.getHealth()) {
                    this.heal(MAX_HEALTH - this.getHealth());
                }
            }
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        if (this.isTame() && stack.is(Items.NAME_TAG)
                && this.distanceToSqr(player) < 16.0
                && this.isOwnedBy(player)) {
            this.setCustomName(stack.getHoverName());
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        // orig Camarasaurus.java:219-278 — interact is apple-tame, name tag,
        // then sit toggle; the original is NOT rideable (no riding code at all).
        if (this.isTame() && this.isOwnedBy(player)
                && this.distanceToSqr(player) < 16.0) {
            this.setOrderedToSit(!this.isOrderedToSit());
            this.setInSittingPose(this.isOrderedToSit());
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        return super.mobInteract(player, hand);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SND_CRYO_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SND_CRYO_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 0.4f;
    }

    @Override
    public float getVoicePitch() {
        return this.isBaby()
                ? (this.random.nextFloat() - this.random.nextFloat()) * 0.1f + 1.5f
                : (this.random.nextFloat() - this.random.nextFloat()) * 0.1f + 1.0f;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.APPLE);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        // Exposes the tame flag to the loot-table NBT predicate so the
        // tamed-only poppy drop (orig Camarasaurus.java:303-312) can stay
        // data-driven in camarasaurus.json.
        tag.putBoolean("OreSpawnTamed", this.isTame());
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return new Camarasaurus((EntityType<? extends Camarasaurus>) this.getType(), level);
    }

    /** orig Camarasaurus.java:78-83 — y>=50 and daytime. */
    @Override
    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level,
                                   net.minecraft.world.entity.MobSpawnType spawnType) {
        if (this.getY() < 50.0) return false;
        return OriginalSpawnGates.isDaytime(level);
    }
}
