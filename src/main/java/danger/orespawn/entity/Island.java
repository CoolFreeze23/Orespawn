package danger.orespawn.entity;

import danger.orespawn.OreSpawnMod;
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

public class Island extends Animal {
    private float dir = 0.0f;
    private float speed = 0.1f;
    private int radius = 5;
    private int depth = 3;
    private int timer = 73;
    private int justSpawned = 1;
    private int ticker = 0;
    private int once = 1;
    private double myX, myY, myZ;
    private int dirchange;

    public Island(EntityType<? extends Island> type, Level level) {
        super(type, level);
        this.ticker = level.random.nextInt(50);
        this.dirchange = this.random.nextInt(2500);
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
            this.dir = this.random.nextFloat() * (float) Math.PI;
            if (this.random.nextInt(2) == 1) this.dir *= -1.0f;
            if (this.random.nextInt(40) != 1) {
                this.radius = 3 + this.random.nextInt(4);
                this.depth = 2 + this.random.nextInt(3);
                this.speed = this.random.nextFloat() / 50.0f;
            } else {
                this.radius = 6 + this.random.nextInt(5);
                this.depth = 3 + this.random.nextInt(4);
                this.speed = this.random.nextFloat() / 200.0f;
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
            this.dir = this.random.nextFloat() * (float) Math.PI;
            if (this.random.nextInt(2) == 1) this.dir *= -1.0f;
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
        double deltadir = 0.10471975333333333;
        double deltamag = 0.35;
        for (int i = 0; i < this.depth; ++i) {
            double tradius = (double) this.radius / (i + 1);
            for (double curdir = -Math.PI; curdir < Math.PI; curdir += deltadir) {
                for (double h = 0.75; h < tradius; h += deltamag) {
                    int ix = (int) (this.getX() + Math.cos(curdir + this.dir) * h);
                    int iz = (int) (this.getZ() + Math.sin(curdir + this.dir) * h);
                    BlockPos pos = new BlockPos(ix, (int) this.getY() - i + 1, iz);
                    if (i == 0) {
                        BlockState state = this.level().getBlockState(pos);
                        if (state.is(Blocks.AIR)) {
                            this.level().setBlock(pos, Blocks.SAND.defaultBlockState(), 3);
                        } else if (state.is(Blocks.BEDROCK)) {
                            this.discard();
                            return;
                        }
                    } else {
                        if (this.random.nextInt(10) == 1) {
                            this.level().setBlock(pos, Blocks.GRAVEL.defaultBlockState(), 3);
                        } else {
                            this.level().setBlock(pos, Blocks.SANDSTONE.defaultBlockState(), 3);
                        }
                    }
                }
            }
        }
        int xoff = this.getX() < 0 ? -1 : 0;
        int zoff = this.getZ() < 0 ? -1 : 0;
        this.level().setBlock(new BlockPos((int) this.getX() + xoff, (int) this.getY(), (int) this.getZ() + zoff),
                Blocks.AIR.defaultBlockState(), 3);
    }

    private void updateIsland() {
        this.myX += this.speed * Math.cos(this.dir);
        this.myZ += this.speed * Math.sin(this.dir);
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
        tag.putInt("Idepth", this.depth);
        tag.putInt("Iradius", this.radius);
        tag.putFloat("Ispeed", this.speed);
        tag.putFloat("Idir", this.dir);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.justSpawned = tag.getInt("JustSpawned");
        this.depth = tag.getInt("Idepth");
        this.radius = tag.getInt("Iradius");
        this.speed = tag.getFloat("Ispeed");
        this.dir = tag.getFloat("Idir");
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
