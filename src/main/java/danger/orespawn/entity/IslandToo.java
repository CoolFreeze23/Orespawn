package danger.orespawn.entity;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class IslandToo extends Animal {
    private int dir = 0;
    private float speed = 0.1f;
    private int width = 5;
    private int depth = 3;
    private int length = 10;
    private int timer = 42;
    private int justSpawned = 1;
    private int ticker = 0;
    private int once = 1;
    private double myX, myY, myZ;
    private int dirchange = 0;
    private int blocktype = 0;

    public IslandToo(EntityType<? extends IslandToo> type, Level level) {
        super(type, level);
        this.ticker = level.random.nextInt(50);
        this.dirchange = this.random.nextInt(5000);
    }

    @Override
    protected void registerGoals() {
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 50.0)
                .add(Attributes.MOVEMENT_SPEED, 0.0);
    }

    @Override
    public void tick() {
        super.tick();
        this.setDeltaMovement(Vec3.ZERO);

        if (this.level().isClientSide) return;

        if (this.once != 0) {
            this.myX = this.getX();
            this.myY = this.getY();
            this.myZ = this.getZ();
            this.once = 0;
        }

        if (this.justSpawned != 0) {
            this.dir = this.random.nextInt(4);
            if (this.random.nextInt(40) != 1) {
                this.length = this.width = 1 + this.random.nextInt(5);
                this.depth = 1 + this.random.nextInt(4);
                this.speed = this.random.nextFloat() / 40.0f;
            } else {
                this.length = this.width = 5 + this.random.nextInt(8);
                this.depth = 3 + this.random.nextInt(6);
                this.speed = this.random.nextFloat() / 150.0f;
            }
            this.createIsland();
            this.ticker = this.random.nextInt(50);
            this.dirchange = this.random.nextInt(10000);
        }

        ++this.ticker;
        if (this.ticker >= this.timer) {
            this.updateIsland();
            this.ticker = 0;
        }

        --this.dirchange;
        if (this.dirchange <= 0) {
            this.dirchange = this.random.nextInt(5000);
            this.dir = this.random.nextInt(4);
        }

        this.justSpawned = 0;
    }

    @Override
    public void aiStep() {
        if (this.level().isClientSide) super.aiStep();
    }

    @Override
    protected void customServerAiStep() {
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    private void createIsland() {
        int xoff = this.getX() < 0 ? 1 : 0;
        int zoff = this.getZ() < 0 ? 1 : 0;
        for (int k = 0; k <= this.depth; ++k) {
            int il = this.length / (this.depth - k + 1);
            if (il < 1) il = 1;
            for (int i = -il; i <= il; ++i) {
                for (int j = -il; j <= il; ++j) {
                    int ix = (int) this.getX() + j - xoff;
                    int iz = (int) this.getZ() + i - zoff;
                    BlockPos pos = new BlockPos(ix, (int) this.getY() + k, iz);
                    if (k == this.depth) {
                        BlockState state = this.level().getBlockState(pos);
                        if (state.is(Blocks.AIR)) {
                            this.level().setBlock(pos, Blocks.GRASS_BLOCK.defaultBlockState(), 3);
                        } else if (state.is(Blocks.BEDROCK)) {
                            this.discard();
                            return;
                        }
                    } else {
                        this.level().setBlock(pos, Blocks.STONE.defaultBlockState(), 3);
                    }
                }
            }
        }
        this.level().setBlock(new BlockPos((int) this.getX() - xoff, (int) this.getY(), (int) this.getZ() - zoff),
                Blocks.AIR.defaultBlockState(), 3);
    }

    private void updateIsland() {
        if (this.dir == 0) this.myZ -= this.speed;
        else if (this.dir == 1) this.myZ += this.speed;
        else if (this.dir == 2) this.myX += this.speed;
        else this.myX -= this.speed;

        int mx = (int) this.myX;
        int mz = (int) this.myZ;
        int px = (int) this.getX();
        int pz = (int) this.getZ();

        if (mx != px || mz != pz) {
            this.setPos(this.myX, this.getY(), this.myZ);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("JustSpawned", this.justSpawned);
        tag.putInt("Iwidth", this.width);
        tag.putInt("Idepth", this.depth);
        tag.putInt("Ilength", this.length);
        tag.putFloat("Ispeed", this.speed);
        tag.putInt("Idir", this.dir);
        tag.putInt("Iblocktype", this.blocktype);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.justSpawned = tag.getInt("JustSpawned");
        this.width = tag.getInt("Iwidth");
        this.depth = tag.getInt("Idepth");
        this.length = tag.getInt("Ilength");
        this.speed = tag.getFloat("Ispeed");
        this.dir = tag.getInt("Idir");
        this.blocktype = tag.getInt("Iblocktype");
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return false;
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return null;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() { return null; }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) { return null; }

    @Override
    protected SoundEvent getDeathSound() { return null; }
}
