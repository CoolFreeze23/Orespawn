package danger.orespawn.entity;

import danger.orespawn.ModEntities;
import danger.orespawn.ModItems;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.ai.GenericTargetSorter;
import danger.orespawn.entity.ai.TargetSelection;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class Beaver extends Animal {
    // OPT-011: cached SoundEvents — identical createVariableRangeEvent ids,
    // allocated once per class instead of on every sound query.
    private static final SoundEvent SND_CHAINSAW = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "chainsaw"));
    private static final SoundEvent SND_SCORPION_HIT = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "scorpion_hit"));
    private static final SoundEvent SND_CRYO_DEATH = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "cryo_death"));

    private static final int MAX_HEALTH = 15;
    private static final double MOVE_SPEED = 0.2;
    private static final int NO_WOOD_FOUND_SENTINEL = 99999;
    private static final int WOOD_CHOP_RANGE_SQ = 12;

    private int closestWoodDistSq = NO_WOOD_FOUND_SENTINEL;
    private int targetWoodX = 0;
    private int targetWoodY = 0;
    private int targetWoodZ = 0;

    public Beaver(EntityType<? extends Beaver> type, Level level) {
        super(type, level);
        this.xpReward = 5;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(2, new AvoidEntityGoal<>(this, Monster.class, 8.0f, 1.0, 1.5));
        this.goalSelector.addGoal(4, new PanicGoal(this, 1.5));
        this.goalSelector.addGoal(5, new AvoidEntityGoal<>(this, Player.class, 8.0f, 1.0, 1.5));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(7, new MyEntityAIWanderALot(this, 10, 1.0));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.MOVEMENT_SPEED, MOVE_SPEED)
                .add(Attributes.ATTACK_DAMAGE, 1.0);
    }

    public boolean isWood(Block block) {
        BlockState state = block.defaultBlockState();
        if (state.is(BlockTags.LOGS)) return true;
        if (state.is(BlockTags.PLANKS)) return true;
        return block == Blocks.HAY_BLOCK;
    }

    private boolean isWoodAt(BlockPos pos) {
        return isWood(this.level().getBlockState(pos).getBlock());
    }

    private boolean scanForWood(int x, int y, int z, int dx, int dy, int dz) {
        int found = 0;
        for (int i = -dy; i <= dy; ++i) {
            for (int j = -dz; j <= dz; ++j) {
                found += checkWoodAt(x + dx, y + i, z + j, dx * dx + j * j + i * i);
                found += checkWoodAt(x - dx, y + i, z + j, dx * dx + j * j + i * i);
            }
        }
        for (int i = -dx; i <= dx; ++i) {
            for (int j = -dz; j <= dz; ++j) {
                found += checkWoodAt(x + i, y + dy, z + j, dy * dy + j * j + i * i);
                found += checkWoodAt(x + i, y - dy, z + j, dy * dy + j * j + i * i);
            }
        }
        for (int i = -dx; i <= dx; ++i) {
            for (int j = -dy; j <= dy; ++j) {
                found += checkWoodAt(x + i, y + j, z + dz, dz * dz + j * j + i * i);
                found += checkWoodAt(x + i, y + j, z - dz, dz * dz + j * j + i * i);
            }
        }
        return found != 0;
    }

    private int checkWoodAt(int x, int y, int z, int dist) {
        BlockPos pos = new BlockPos(x, y, z);
        if (isWoodAt(pos) && dist < this.closestWoodDistSq) {
            this.closestWoodDistSq = dist;
            this.targetWoodX = x;
            this.targetWoodY = y;
            this.targetWoodZ = z;
            return 1;
        }
        return 0;
    }

    public void breakRecursor(Level level, BlockPos pos, BlockPos fromPos, int recursion) {
        if (recursion > 200) return;
        for (int dx = -1; dx <= 1; ++dx) {
            for (int dy = -1; dy <= 1; ++dy) {
                for (int dz = -1; dz <= 1; ++dz) {
                    if (dx == 0 && dy == 0 && dz == 0) continue;
                    BlockPos checkPos = pos.offset(dx, dy, dz);
                    if (checkPos.equals(fromPos)) continue;
                    if (recursion > 0
                            && checkPos.getX() >= fromPos.getX() - 1
                            && checkPos.getX() <= fromPos.getX() + 1
                            && checkPos.getY() >= fromPos.getY() - 1
                            && checkPos.getY() <= fromPos.getY() + 1
                            && checkPos.getZ() >= fromPos.getZ() - 1
                            && checkPos.getZ() <= fromPos.getZ() + 1) {
                        continue;
                    }
                    BlockState state = level.getBlockState(checkPos);
                    if (!isWood(state.getBlock())) continue;
                    level.setBlock(checkPos, Blocks.AIR.defaultBlockState(), 2);
                    Block.popResource(level, checkPos, new ItemStack(state.getBlock()));
                    this.breakRecursor(level, checkPos, pos, recursion + 1);
                }
            }
        }
    }

    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;

        if (this.random.nextInt(200) == 1) {
            this.setTarget(null);
        }

        boolean needsFood = this.random.nextInt(30) == 0
                && (int) this.getHealth() < MAX_HEALTH;
        boolean randomChop = this.random.nextInt(350) == 1;

        if (needsFood || randomChop) {
            this.closestWoodDistSq = NO_WOOD_FOUND_SENTINEL;
            this.targetWoodX = 0;
            this.targetWoodY = 0;
            this.targetWoodZ = 0;
            for (int i = 1; i < 11; ++i) {
                int j = Math.min(i, 2);
                if (this.scanForWood(
                        (int) this.getX(), (int) this.getY() + 1, (int) this.getZ(),
                        i, j, i)) {
                    break;
                }
                if (i >= 6) ++i;
            }
            if (this.closestWoodDistSq < NO_WOOD_FOUND_SENTINEL) {
                this.getNavigation().moveTo(this.targetWoodX, this.targetWoodY, this.targetWoodZ, 1.0);
                if (this.closestWoodDistSq < WOOD_CHOP_RANGE_SQ) {
                    if (this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
                        BlockPos targetPos = new BlockPos(this.targetWoodX, this.targetWoodY, this.targetWoodZ);
                        this.level().setBlock(targetPos, Blocks.AIR.defaultBlockState(), 2);
                        this.breakRecursor(this.level(), targetPos, targetPos, 0);
                    }
                    this.heal(1.0f);
                    this.playSound(
                            SND_CHAINSAW,
                            1.0f, this.random.nextFloat() * 0.2f + 0.9f);
                }
            }
        }

        if (this.random.nextInt(200) == 1) {
            Beaver buddy = this.findBuddy();
            if (buddy != null) {
                this.getNavigation().moveTo(buddy.getX(), buddy.getY(), buddy.getZ(), 0.5);
            }
        }

        super.customServerAiStep();
    }

    @Nullable
    private Beaver findBuddy() {
        // orig Beaver.java:38,51,219-231 — sorts with GenericTargetSorter and takes the
        // FIRST entry of a getEntitiesWithinAABB query, which includes this beaver itself;
        // at distance 0 it always ranks first, so the 1-in-200 "visit a buddy" stroll
        // pathed to the beaver's own position. Kept, bug and all, for parity.
        List<Beaver> buddies = this.level().getEntitiesOfClass(Beaver.class,
                this.getBoundingBox().inflate(16.0, 6.0, 16.0));
        // OPT-021: single-pass min instead of sort+get(0) — same element, same
        // stable-tie order; the self-at-distance-0 parity bug is preserved.
        return TargetSelection.first(buddies, new GenericTargetSorter(this));
    }

    @Override
    public boolean isPushedByFluid() {
        return true;
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnType) {
        if (this.getY() < 50.0) return false;
        if (this.getY() > 100.0) return false;
        BlockState below = level.getBlockState(this.blockPosition().below());
        return below.is(Blocks.DIRT) || below.is(Blocks.GRASS_BLOCK)
                || below.is(Blocks.SHORT_GRASS) || below.is(Blocks.OAK_LEAVES);
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SND_SCORPION_HIT;
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
        return stack.is(ModItems.CRYSTAL_APPLE.get());
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return new Beaver(ModEntities.BEAVER.get(), level);
    }
}
