package danger.orespawn.entity;

import danger.orespawn.OreSpawnMod;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
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

import java.util.List;

public class EntityTermite extends EntityAnt {
    private int attackDelay = 20;
    private int closest = 99999;
    private int tx = 0;
    private int ty = 0;
    private int tz = 0;

    public EntityTermite(EntityType<? extends EntityTermite> type, Level level) {
        super(type, level);
        this.moveSpeed = 0.2;
        this.xpReward = 1;
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
        this.attackDelay = 20;

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

    private boolean scanIt(int x, int y, int z, int dx, int dy, int dz) {
        int found = 0;
        for (int i = -dy; i <= dy; i++) {
            for (int j = -dz; j <= dz; j++) {
                BlockState state = this.level().getBlockState(new BlockPos(x + dx, y + i, z + j));
                if (isWood(state)) {
                    int d = dx * dx + j * j + i * i;
                    if (d < this.closest) {
                        this.closest = d;
                        this.tx = x + dx;
                        this.ty = y + i;
                        this.tz = z + j;
                        found++;
                    }
                }
                state = this.level().getBlockState(new BlockPos(x - dx, y + i, z + j));
                if (isWood(state)) {
                    int d = dx * dx + j * j + i * i;
                    if (d < this.closest) {
                        this.closest = d;
                        this.tx = x - dx;
                        this.ty = y + i;
                        this.tz = z + j;
                        found++;
                    }
                }
            }
        }
        for (int i = -dx; i <= dx; i++) {
            for (int j = -dz; j <= dz; j++) {
                BlockState state = this.level().getBlockState(new BlockPos(x + i, y + dy, z + j));
                if (isWood(state)) {
                    int d = dy * dy + j * j + i * i;
                    if (d < this.closest) {
                        this.closest = d;
                        this.tx = x + i;
                        this.ty = y + dy;
                        this.tz = z + j;
                        found++;
                    }
                }
                state = this.level().getBlockState(new BlockPos(x + i, y - dy, z + j));
                if (isWood(state)) {
                    int d = dy * dy + j * j + i * i;
                    if (d < this.closest) {
                        this.closest = d;
                        this.tx = x + i;
                        this.ty = y - dy;
                        this.tz = z + j;
                        found++;
                    }
                }
            }
        }
        for (int i = -dx; i <= dx; i++) {
            for (int j = -dy; j <= dy; j++) {
                BlockState state = this.level().getBlockState(new BlockPos(x + i, y + j, z + dz));
                if (isWood(state)) {
                    int d = dz * dz + j * j + i * i;
                    if (d < this.closest) {
                        this.closest = d;
                        this.tx = x + i;
                        this.ty = y + j;
                        this.tz = z + dz;
                        found++;
                    }
                }
                state = this.level().getBlockState(new BlockPos(x + i, y + j, z - dz));
                if (isWood(state)) {
                    int d = dz * dz + j * j + i * i;
                    if (d < this.closest) {
                        this.closest = d;
                        this.tx = x + i;
                        this.ty = y + j;
                        this.tz = z - dz;
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

        if (this.random.nextInt(200) == 1) {
            this.closest = 99999;
            this.tx = 0;
            this.ty = 0;
            this.tz = 0;
            for (int i = 1; i < 8; i++) {
                int j = Math.min(i, 4);
                if (this.scanIt((int) this.getX(), (int) this.getY() + 1, (int) this.getZ(), i, j, i)) break;
                if (i >= 5) i++;
            }
            if (this.closest < 99999) {
                this.getNavigation().moveTo(this.tx, this.ty, this.tz, 1.0);
                if (this.closest < 6) {
                    BlockPos targetPos = new BlockPos(this.tx, this.ty, this.tz);
                    if (this.level().getGameRules().getBoolean(net.minecraft.world.level.GameRules.RULE_MOBGRIEFING)) {
                        if (this.random.nextInt(3) != 0) {
                            this.level().setBlock(targetPos, Blocks.DIRT.defaultBlockState(), 2);
                        } else {
                            this.level().setBlock(targetPos, Blocks.AIR.defaultBlockState(), 2);
                        }
                    }
                    this.heal(1.0f);
                    int buddies = this.level().getEntitiesOfClass(EntityTermite.class,
                            this.getBoundingBox().inflate(3.0, 3.0, 3.0)).size();
                    // TODO: spawn more termites when termite entity type is registered
                }
            }
        }
        super.customServerAiStep();
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnType) {
        if (this.getY() < 50.0) return false;
        return findBuddies() <= 4;
    }

    @Override
    protected int findBuddies() {
        return this.level().getEntitiesOfClass(EntityTermite.class,
                this.getBoundingBox().inflate(20.0, 10.0, 20.0)).size();
    }
}
