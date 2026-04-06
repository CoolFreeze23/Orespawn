package danger.orespawn.entity;

import danger.orespawn.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class MyEntityAIDance extends Goal {
    private final TamableAnimal pet;
    private final Level level;
    public int ticker = 0;
    public int dance_move = 0;
    public int is_dancing = 0;

    public MyEntityAIDance(TamableAnimal pet) {
        this.pet = pet;
        this.level = pet.level();
    }

    public boolean isDanceBlock(Block block) {
        return block == Blocks.JUKEBOX || block == Blocks.NOTE_BLOCK || block == Blocks.BEACON
                || block == ModBlocks.BLOCK_RUBY.get()
                || block == ModBlocks.BLOCK_AMETHYST.get()
                || block == ModBlocks.BLOCK_TITANIUM.get()
                || block == ModBlocks.BLOCK_URANIUM.get();
    }

    @Override
    public boolean canUse() {
        if (this.pet.isOrderedToSit()) return false;
        long t = this.level.getDayTime() % 24000L;
        if (t < 14000L || t > 22000L) return false;

        int count = 0;
        for (int i = -3; i < 4; ++i) {
            for (int j = -3; j < 4; ++j) {
                BlockPos pos = new BlockPos((int) this.pet.getX() + i, (int) this.pet.getY() - 1, (int) this.pet.getZ() + j);
                Block block = this.level.getBlockState(pos).getBlock();
                if (isDanceBlock(block)) count++;
            }
        }
        return count != 0;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.pet.isOrderedToSit()) return false;
        long t = this.level.getDayTime() % 24000L;
        if (t < 14000L || t > 22000L) return false;

        int count = 0;
        int ix = 0, iz = 0;
        for (int i = -3; i < 4; ++i) {
            for (int j = -3; j < 4; ++j) {
                BlockPos pos = new BlockPos((int) this.pet.getX() + i, (int) this.pet.getY() - 1, (int) this.pet.getZ() + j);
                Block block = this.level.getBlockState(pos).getBlock();
                if (isDanceBlock(block)) {
                    count++;
                    ix += i;
                    iz += j;
                }
            }
        }
        if (count == 0) return false;
        ix /= count;
        iz /= count;
        if (count < 40) {
            this.pet.getNavigation().moveTo(
                    (int) this.pet.getX() + ix, (int) this.pet.getY(), (int) this.pet.getZ() + iz, 1.0);
        } else if (this.level.random.nextInt(3) == 1) {
            this.pet.getNavigation().moveTo(
                    (int) this.pet.getX(), (int) this.pet.getY(), (int) this.pet.getZ(), 1.0);
        }
        this.is_dancing = 1;
        return true;
    }

    @Override
    public void start() {
        this.pet.setShiftKeyDown(false);
        this.ticker = 0;
        this.dance_move = 0;
        this.is_dancing = 1;

        int count = 0;
        int ix = 0, iz = 0;
        for (int i = -3; i < 4; ++i) {
            for (int j = -3; j < 4; ++j) {
                BlockPos pos = new BlockPos((int) this.pet.getX() + i, (int) this.pet.getY() - 1, (int) this.pet.getZ() + j);
                if (isDanceBlock(this.level.getBlockState(pos).getBlock())) {
                    count++;
                    ix += i;
                    iz += j;
                }
            }
        }
        if (count > 0) {
            ix /= count;
            iz /= count;
            if (count < 40) {
                this.pet.getNavigation().moveTo(
                        (int) this.pet.getX() + ix, (int) this.pet.getY(), (int) this.pet.getZ() + iz, 1.0);
            }
        }
    }

    @Override
    public void stop() {
        this.pet.setShiftKeyDown(false);
        this.ticker = 0;
        this.dance_move = 0;
        this.is_dancing = 0;
    }

    @Override
    public void tick() {
        int cycle = 20;
        int halfc = cycle / 2;
        int mover = cycle * 8;

        ++this.ticker;
        if (this.dance_move == 0) {
            this.dance_move = 1 + this.level.random.nextInt(10);
            this.pet.setDeltaMovement(this.pet.getDeltaMovement().multiply(0, 1, 0));
            this.ticker = 0;
            this.pet.setShiftKeyDown(false);
        }

        switch (this.dance_move) {
            case 1 -> { moveIt(0); if (this.ticker > mover) this.dance_move = 0; }
            case 2 -> { moveIt(1); if (this.ticker > mover) this.dance_move = 0; }
            case 3 -> {
                this.pet.setShiftKeyDown(this.ticker % cycle >= halfc);
                if (this.ticker > mover) this.dance_move = 0;
            }
            case 4 -> {
                if (this.ticker % halfc == 1) {
                    this.pet.swing(this.pet.getUsedItemHand());
                    this.pet.setDeltaMovement(this.pet.getDeltaMovement().add(0, 0.25, 0));
                }
                if (this.ticker > mover) this.dance_move = 0;
            }
            case 5 -> {
                if (this.ticker % halfc == 1) this.pet.swing(this.pet.getUsedItemHand());
                moveIt(0);
                if (this.ticker > mover) this.dance_move = 0;
            }
            case 6 -> {
                if (this.ticker % halfc == 1) this.pet.swing(this.pet.getUsedItemHand());
                moveIt(1);
                if (this.ticker > mover) this.dance_move = 0;
            }
            case 7 -> {
                this.pet.setShiftKeyDown(this.ticker % cycle >= halfc);
                moveIt(0); moveIt(2);
                if (this.ticker > mover) this.dance_move = 0;
            }
            case 8 -> {
                this.pet.setShiftKeyDown(this.ticker % cycle >= halfc);
                moveIt(1); moveIt(2);
                if (this.ticker > mover) this.dance_move = 0;
            }
            case 9 -> {
                this.pet.setShiftKeyDown(this.ticker % cycle >= halfc);
                if (this.ticker % halfc == 1) this.pet.swing(this.pet.getUsedItemHand());
                moveIt(0); moveIt(3);
                if (this.ticker > mover) this.dance_move = 0;
            }
            case 10 -> {
                if (this.ticker % cycle < halfc) {
                    this.pet.setShiftKeyDown(false);
                    this.pet.setDeltaMovement(this.pet.getDeltaMovement().add(0, 0.25, 0));
                } else {
                    this.pet.setShiftKeyDown(true);
                }
                if (this.ticker % halfc == 1) this.pet.swing(this.pet.getUsedItemHand());
                moveIt(1); moveIt(3);
                if (this.ticker > mover) this.dance_move = 0;
            }
            default -> this.dance_move = 0;
        }
    }

    private void moveIt(int dir) {
        int cycle = 20;
        float dirx = 0, dirz = 0, dirYaw = 0, dirYawH = 0;
        switch (dir) {
            case 0 -> dirx = 0.02f;
            case 1 -> dirz = 0.02f;
            case 2 -> dirYaw = 10.0f;
            case 3 -> dirYawH = 10.0f;
        }
        int t = this.ticker % cycle;
        if (t >= cycle / 2) { dirx = -dirx; dirz = -dirz; dirYaw = -dirYaw; dirYawH = -dirYawH; }
        t = this.ticker % (cycle / 2);
        if (t >= cycle / 4) { dirYaw = -dirYaw; dirYawH = -dirYawH; }

        Vec3 mot = this.pet.getDeltaMovement();
        this.pet.setDeltaMovement(mot.x + dirx, mot.y, mot.z + dirz);
        this.pet.setYRot(this.pet.getYRot() + dirYaw);
        this.pet.yHeadRot += dirYawH;
    }
}
