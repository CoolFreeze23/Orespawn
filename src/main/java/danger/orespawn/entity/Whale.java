package danger.orespawn.entity;

import danger.orespawn.ModEntities;
import danger.orespawn.ModItems;
import danger.orespawn.OreSpawnMod;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class Whale extends Animal {
    private static final int MAX_HEALTH = 100;
    private static final double MOVE_SPEED = 0.35;

    private int spray = 0;
    private int sprayTimer = 0;
    private int closest = 99999;
    private int tx = 0;
    private int ty = 0;
    private int tz = 0;

    public Whale(EntityType<? extends Whale> type, Level level) {
        super(type, level);
        this.xpReward = 40;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(2, new TemptGoal(this, 1.2, Ingredient.of(Items.COD), false));
        this.goalSelector.addGoal(4, new PanicGoal(this, 1.5));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 12.0f));
        this.goalSelector.addGoal(6, new MyEntityAIWander(this, 1.0f));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.MOVEMENT_SPEED, MOVE_SPEED)
                .add(Attributes.ATTACK_DAMAGE, 0.0);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.spray == 0) {
            if (this.sprayTimer > 0) {
                --this.sprayTimer;
            }
            if (this.sprayTimer == 0) {
                this.sprayTimer = 250 + this.random.nextInt(250);
                this.spray = 25 + this.random.nextInt(25);
            }
        }

        if (this.level().isClientSide && this.spray > 0) {
            for (int i = 0; i < 20; ++i) {
                double d = this.random.nextDouble() * 0.75;
                d *= d;
                double dir = this.random.nextDouble() * 2.0 * Math.PI;
                double dx = Math.cos(dir - Math.PI) * d / 2.0;
                double dz = Math.sin(dir - Math.PI) * d / 2.0;
                dir += Math.PI / 2.0;
                if (i < 10) {
                    this.level().addParticle(ParticleTypes.BUBBLE,
                            this.getX() + dx, this.getY() + 1.0 + d, this.getZ() + dz,
                            Math.cos(dir) * this.random.nextFloat() / 4.0,
                            this.random.nextFloat() * 2.0,
                            Math.sin(dir) * this.random.nextFloat() / 4.0);
                } else {
                    this.level().addParticle(ParticleTypes.SPLASH,
                            this.getX() + dx, this.getY() + 1.0 + d, this.getZ() + dz,
                            Math.cos(dir) * this.random.nextFloat() / 4.0,
                            this.random.nextFloat() * 2.0,
                            Math.sin(dir) * this.random.nextFloat() / 4.0);
                }
            }
            --this.spray;
        }

        if (this.random.nextInt(200) == 1) {
            this.heal(1.0f);
        }
    }

    @Override
    public boolean isPushedByFluid() {
        return true;
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (this.isRemoved()) return;

        if (this.random.nextInt(200) == 1) {
            this.setTarget(null);
        }

        if (!this.isInWater() && this.random.nextInt(20) == 0) {
            this.closest = 99999;
            this.tx = 0;
            this.ty = 0;
            this.tz = 0;
            for (int i = 1; i < 11; ++i) {
                int j = Math.min(i, 4);
                if (this.scanForWater(
                        (int) this.getX(), (int) this.getY() - 1, (int) this.getZ(),
                        i, j, i)) {
                    break;
                }
                if (i >= 5) ++i;
            }
            if (this.closest < 99999) {
                this.getNavigation().moveTo(this.tx, this.ty - 1, this.tz, 1.0);
            } else {
                if (this.random.nextInt(25) == 1) {
                    this.hurt(this.damageSources().dryOut(), 4.0f);
                }
                if (this.getHealth() <= 0.0f) {
                    this.discard();
                    return;
                }
            }
        }

        if (this.isInWater() && this.random.nextInt(50) == 0) {
            this.playSound(SoundEvents.GENERIC_SPLASH, 1.0f,
                    this.random.nextFloat() * 0.2f + 0.9f);
            this.heal(1.0f);
        }
    }

    private boolean scanForWater(int x, int y, int z, int dx, int dy, int dz) {
        int found = 0;
        for (int i = -dy; i <= dy; ++i) {
            for (int j = -dz; j <= dz; ++j) {
                found += checkWaterAt(x + dx, y + i, z + j, dx * dx + j * j + i * i);
                found += checkWaterAt(x - dx, y + i, z + j, dx * dx + j * j + i * i);
            }
        }
        for (int i = -dx; i <= dx; ++i) {
            for (int j = -dz; j <= dz; ++j) {
                found += checkWaterAt(x + i, y + dy, z + j, dy * dy + j * j + i * i);
                found += checkWaterAt(x + i, y - dy, z + j, dy * dy + j * j + i * i);
            }
        }
        for (int i = -dx; i <= dx; ++i) {
            for (int j = -dy; j <= dy; ++j) {
                found += checkWaterAt(x + i, y + j, z + dz, dz * dz + j * j + i * i);
                found += checkWaterAt(x + i, y + j, z - dz, dz * dz + j * j + i * i);
            }
        }
        return found != 0;
    }

    private int checkWaterAt(int x, int y, int z, int dist) {
        BlockState state = this.level().getBlockState(new BlockPos(x, y, z));
        if (state.is(Blocks.WATER) && dist < this.closest) {
            this.closest = dist;
            this.tx = x;
            this.ty = y;
            this.tz = z;
            return 1;
        }
        return 0;
    }

    private int findBuddies() {
        return this.level().getEntitiesOfClass(Whale.class,
                this.getBoundingBox().inflate(32.0, 8.0, 32.0)).size();
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnType) {
        if (this.getY() < 50.0) return false;
        long dayTime = this.level().getDayTime() % 24000L;
        if (dayTime >= 13000L) return false;
        if (this.random.nextInt(50) != 1) return false;
        return this.findBuddies() <= 0;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.GENERIC_SPLASH;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "little_splat"));
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "big_splat"));
    }

    @Override
    protected float getSoundVolume() {
        return 0.9f;
    }

    @Override
    public float getVoicePitch() {
        return 0.5f;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(ModItems.CRYSTAL_APPLE.get());
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return new Whale(ModEntities.WHALE.get(), level);
    }
}
