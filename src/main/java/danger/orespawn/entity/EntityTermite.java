package danger.orespawn.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** Hostile termite that eats wood — teleports players to the Crystal dimension (DimensionID5). */
public class EntityTermite extends EntityAnt {
    private static final int NO_CLOSEST_MATCH = 99999;
    private static final int ATTACK_DELAY_TICKS = 20;
    private static final int WOOD_SCAN_INTERVAL_CHANCE = 200;
    private static final int WOOD_SCAN_MAX_RADIUS = 8;
    private static final int WOOD_SCAN_VERTICAL_CAP = 4;
    private static final int WOOD_SCAN_SKIP_AFTER = 5;
    private static final int CLOSE_ENOUGH_TO_EAT_DIST_SQ = 6;
    private static final int MIN_SPAWN_Y = 50;
    private static final int MAX_NEARBY_CLUSTER = 4;

    private int attackDelay = ATTACK_DELAY_TICKS;
    private int closestWoodDistSq = NO_CLOSEST_MATCH;
    private int targetX = 0;
    private int targetY = 0;
    private int targetZ = 0;

    public EntityTermite(EntityType<? extends EntityTermite> type, Level level) {
        super(type, level);
        this.moveSpeed = 0.2;
        this.xpReward = 1;
    }

    @Override
    protected ResourceKey<Level> getTargetDimension() {
        return CRYSTAL;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new PanicGoal(this, 1.4));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(2, new MyEntityAIWanderALot(this, 8, 1.0));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 5.0)
                .add(Attributes.MOVEMENT_SPEED, 0.2)
                .add(Attributes.ATTACK_DAMAGE, 2.0);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (this.random.nextInt(15) != 0) return false;
        if (this.level().getDifficulty() == Difficulty.PEACEFUL) return false;
        return target.hurt(this.damageSources().mobAttack(this), 1.0f);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.isRemoved()) return;

        if (this.attackDelay > 0) --this.attackDelay;
        if (this.attackDelay > 0) return;
        this.attackDelay = ATTACK_DELAY_TICKS;

        if (this.level().getDifficulty() == Difficulty.PEACEFUL) return;

        Player nearby = this.level().getNearestPlayer(this, 1.5);
        if (nearby != null) {
            this.doHurtTarget(nearby);
        }
    }

    public boolean isWood(BlockState state) {
        return state.is(Blocks.OAK_LOG) || state.is(Blocks.SPRUCE_LOG) ||
                state.is(Blocks.BIRCH_LOG) || state.is(Blocks.JUNGLE_LOG) ||
                state.is(Blocks.ACACIA_LOG) || state.is(Blocks.DARK_OAK_LOG) ||
                state.is(Blocks.OAK_PLANKS) || state.is(Blocks.SPRUCE_PLANKS) ||
                state.is(Blocks.BIRCH_PLANKS) || state.is(Blocks.JUNGLE_PLANKS) ||
                state.is(Blocks.ACACIA_PLANKS) || state.is(Blocks.DARK_OAK_PLANKS) ||
                state.is(Blocks.OAK_FENCE) || state.is(Blocks.BOOKSHELF) ||
                state.is(Blocks.CRAFTING_TABLE) || state.is(Blocks.CHEST);
    }

    private boolean scanForWoodBlocks(int x, int y, int z, int dx, int dy, int dz) {
        int found = 0;
        for (int i = -dy; i <= dy; i++) {
            for (int j = -dz; j <= dz; j++) {
                BlockState state = this.level().getBlockState(new BlockPos(x + dx, y + i, z + j));
                if (isWood(state)) {
                    int distSqToWood = dx * dx + j * j + i * i;
                    if (distSqToWood < this.closestWoodDistSq) {
                        this.closestWoodDistSq = distSqToWood;
                        this.targetX = x + dx;
                        this.targetY = y + i;
                        this.targetZ = z + j;
                        found++;
                    }
                }
                state = this.level().getBlockState(new BlockPos(x - dx, y + i, z + j));
                if (isWood(state)) {
                    int distSqToWood = dx * dx + j * j + i * i;
                    if (distSqToWood < this.closestWoodDistSq) {
                        this.closestWoodDistSq = distSqToWood;
                        this.targetX = x - dx;
                        this.targetY = y + i;
                        this.targetZ = z + j;
                        found++;
                    }
                }
            }
        }
        for (int i = -dx; i <= dx; i++) {
            for (int j = -dz; j <= dz; j++) {
                BlockState state = this.level().getBlockState(new BlockPos(x + i, y + dy, z + j));
                if (isWood(state)) {
                    int distSqToWood = dy * dy + j * j + i * i;
                    if (distSqToWood < this.closestWoodDistSq) {
                        this.closestWoodDistSq = distSqToWood;
                        this.targetX = x + i;
                        this.targetY = y + dy;
                        this.targetZ = z + j;
                        found++;
                    }
                }
                state = this.level().getBlockState(new BlockPos(x + i, y - dy, z + j));
                if (isWood(state)) {
                    int distSqToWood = dy * dy + j * j + i * i;
                    if (distSqToWood < this.closestWoodDistSq) {
                        this.closestWoodDistSq = distSqToWood;
                        this.targetX = x + i;
                        this.targetY = y - dy;
                        this.targetZ = z + j;
                        found++;
                    }
                }
            }
        }
        for (int i = -dx; i <= dx; i++) {
            for (int j = -dy; j <= dy; j++) {
                BlockState state = this.level().getBlockState(new BlockPos(x + i, y + j, z + dz));
                if (isWood(state)) {
                    int distSqToWood = dz * dz + j * j + i * i;
                    if (distSqToWood < this.closestWoodDistSq) {
                        this.closestWoodDistSq = distSqToWood;
                        this.targetX = x + i;
                        this.targetY = y + j;
                        this.targetZ = z + dz;
                        found++;
                    }
                }
                state = this.level().getBlockState(new BlockPos(x + i, y + j, z - dz));
                if (isWood(state)) {
                    int distSqToWood = dz * dz + j * j + i * i;
                    if (distSqToWood < this.closestWoodDistSq) {
                        this.closestWoodDistSq = distSqToWood;
                        this.targetX = x + i;
                        this.targetY = y + j;
                        this.targetZ = z - dz;
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
        if (this.random.nextInt(200) == 1) {
            this.setTarget(null);
        }

        if (this.random.nextInt(WOOD_SCAN_INTERVAL_CHANCE) == 1) {
            this.closestWoodDistSq = NO_CLOSEST_MATCH;
            this.targetX = 0;
            this.targetY = 0;
            this.targetZ = 0;
            for (int i = 1; i < WOOD_SCAN_MAX_RADIUS; i++) {
                int j = Math.min(i, WOOD_SCAN_VERTICAL_CAP);
                if (this.scanForWoodBlocks((int) this.getX(), (int) this.getY() + 1, (int) this.getZ(), i, j, i)) break;
                if (i >= WOOD_SCAN_SKIP_AFTER) i++;
            }
            if (this.closestWoodDistSq < NO_CLOSEST_MATCH) {
                this.getNavigation().moveTo(this.targetX, this.targetY, this.targetZ, 1.0);
                if (this.closestWoodDistSq < CLOSE_ENOUGH_TO_EAT_DIST_SQ) {
                    BlockPos targetPos = new BlockPos(this.targetX, this.targetY, this.targetZ);
                    if (this.level().getGameRules().getBoolean(net.minecraft.world.level.GameRules.RULE_MOBGRIEFING)) {
                        if (this.random.nextInt(3) != 0) {
                            this.level().setBlock(targetPos, Blocks.DIRT.defaultBlockState(), 2);
                        } else {
                            this.level().setBlock(targetPos, Blocks.AIR.defaultBlockState(), 2);
                        }
                    }
                    this.heal(1.0f);
                }
            }
        }
        super.customServerAiStep();
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnType) {
        if (this.getY() < MIN_SPAWN_Y) return false;
        return level.getEntitiesOfClass(EntityTermite.class,
                this.getBoundingBox().inflate(20.0, 10.0, 20.0)).size() <= MAX_NEARBY_CLUSTER;
    }

    @Override
    protected int findBuddies() {
        return this.level().getEntitiesOfClass(EntityTermite.class,
                this.getBoundingBox().inflate(20.0, 10.0, 20.0)).size();
    }
}
