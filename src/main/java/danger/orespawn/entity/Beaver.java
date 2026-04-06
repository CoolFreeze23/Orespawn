package danger.orespawn.entity;

import danger.orespawn.ModEntities;
import danger.orespawn.ModItems;
import danger.orespawn.OreSpawnMod;
import java.util.Comparator;
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
    private static final int MAX_HEALTH = 15;
    private static final double MOVE_SPEED = 0.2;

    private int closest = 99999;
    private int tx = 0;
    private int ty = 0;
    private int tz = 0;

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
        // TODO: add OreSpawn custom tree blocks (MyDT, MySkyTreeLog)
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
        if (isWoodAt(pos) && dist < this.closest) {
            this.closest = dist;
            this.tx = x;
            this.ty = y;
            this.tz = z;
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
            this.closest = 99999;
            this.tx = 0;
            this.ty = 0;
            this.tz = 0;
            for (int i = 1; i < 11; ++i) {
                int j = Math.min(i, 2);
                if (this.scanForWood(
                        (int) this.getX(), (int) this.getY() + 1, (int) this.getZ(),
                        i, j, i)) {
                    break;
                }
                if (i >= 6) ++i;
            }
            if (this.closest < 99999) {
                this.getNavigation().moveTo(this.tx, this.ty, this.tz, 1.0);
                if (this.closest < 12) {
                    if (this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
                        BlockPos targetPos = new BlockPos(this.tx, this.ty, this.tz);
                        this.level().setBlock(targetPos, Blocks.AIR.defaultBlockState(), 2);
                        this.breakRecursor(this.level(), targetPos, targetPos, 0);
                    }
                    this.heal(1.0f);
                    this.playSound(
                            SoundEvent.createVariableRangeEvent(
                                    ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "chainsaw")),
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
        List<Beaver> buddies = this.level().getEntitiesOfClass(Beaver.class,
                this.getBoundingBox().inflate(16.0, 6.0, 16.0));
        buddies.sort(Comparator.comparingDouble(this::distanceToSqr));
        return buddies.stream()
                .filter(b -> b != this)
                .findFirst()
                .orElse(null);
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    public boolean isPushedByFluid() {
        return true;
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnType) {
        if (this.getY() < 50.0) return false;
        if (this.getY() > 100.0) return false;
        BlockState below = this.level().getBlockState(this.blockPosition().below());
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
        return SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "scorpion_hit"));
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "cryo_death"));
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
