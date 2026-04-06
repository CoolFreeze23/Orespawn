package danger.orespawn.entity;

import danger.orespawn.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public class MyEntityAIDance extends Goal {
    private static final long DAY_LENGTH_TICKS = 24000L;
    private static final long DANCE_TIME_START = 14000L;
    private static final long DANCE_TIME_END = 22000L;
    private static final int SCAN_OFFSET_MIN = -3;
    private static final int SCAN_OFFSET_MAX = 4;
    private static final int BLOCKS_BELOW_FEET = 1;
    private static final int MANY_DANCE_BLOCKS_THRESHOLD = 40;
    private static final int RANDOM_STAY_PUT_DENOMINATOR = 3;
    private static final int RANDOM_STAY_PUT_HIT = 1;
    private static final double NAVIGATION_SPEED = 1.0;
    private static final int DANCE_TICK_CYCLE = 20;
    private static final int DANCE_MOVE_DURATION_CYCLES = 8;
    private static final int DANCE_VARIANT_COUNT = 10;
    private static final float STRAFE_SPEED = 0.02f;
    private static final float YAW_STEP_DEGREES = 10.0f;
    private static final double VERTICAL_NUDGE = 0.25;

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
        long timeOfDay = this.level.getDayTime() % DAY_LENGTH_TICKS;
        if (timeOfDay < DANCE_TIME_START || timeOfDay > DANCE_TIME_END) return false;

        int danceBlockCount = countDanceBlocksInRange();
        return danceBlockCount != 0;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.pet.isOrderedToSit()) return false;
        long timeOfDay = this.level.getDayTime() % DAY_LENGTH_TICKS;
        if (timeOfDay < DANCE_TIME_START || timeOfDay > DANCE_TIME_END) return false;

        int danceBlockCount = 0;
        int weightedOffsetX = 0;
        int weightedOffsetZ = 0;
        for (int offsetX = SCAN_OFFSET_MIN; offsetX < SCAN_OFFSET_MAX; ++offsetX) {
            for (int offsetZ = SCAN_OFFSET_MIN; offsetZ < SCAN_OFFSET_MAX; ++offsetZ) {
                BlockPos pos = new BlockPos(
                        (int) this.pet.getX() + offsetX,
                        (int) this.pet.getY() - BLOCKS_BELOW_FEET,
                        (int) this.pet.getZ() + offsetZ);
                Block block = this.level.getBlockState(pos).getBlock();
                if (isDanceBlock(block)) {
                    danceBlockCount++;
                    weightedOffsetX += offsetX;
                    weightedOffsetZ += offsetZ;
                }
            }
        }
        if (danceBlockCount == 0) return false;
        weightedOffsetX /= danceBlockCount;
        weightedOffsetZ /= danceBlockCount;
        if (danceBlockCount < MANY_DANCE_BLOCKS_THRESHOLD) {
            this.pet.getNavigation().moveTo(
                    (int) this.pet.getX() + weightedOffsetX,
                    (int) this.pet.getY(),
                    (int) this.pet.getZ() + weightedOffsetZ,
                    NAVIGATION_SPEED);
        } else if (this.level.random.nextInt(RANDOM_STAY_PUT_DENOMINATOR) == RANDOM_STAY_PUT_HIT) {
            this.pet.getNavigation().moveTo(
                    (int) this.pet.getX(), (int) this.pet.getY(), (int) this.pet.getZ(), NAVIGATION_SPEED);
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

        int danceBlockCount = 0;
        int weightedOffsetX = 0;
        int weightedOffsetZ = 0;
        for (int offsetX = SCAN_OFFSET_MIN; offsetX < SCAN_OFFSET_MAX; ++offsetX) {
            for (int offsetZ = SCAN_OFFSET_MIN; offsetZ < SCAN_OFFSET_MAX; ++offsetZ) {
                BlockPos pos = new BlockPos(
                        (int) this.pet.getX() + offsetX,
                        (int) this.pet.getY() - BLOCKS_BELOW_FEET,
                        (int) this.pet.getZ() + offsetZ);
                if (isDanceBlock(this.level.getBlockState(pos).getBlock())) {
                    danceBlockCount++;
                    weightedOffsetX += offsetX;
                    weightedOffsetZ += offsetZ;
                }
            }
        }
        if (danceBlockCount > 0) {
            weightedOffsetX /= danceBlockCount;
            weightedOffsetZ /= danceBlockCount;
            if (danceBlockCount < MANY_DANCE_BLOCKS_THRESHOLD) {
                this.pet.getNavigation().moveTo(
                        (int) this.pet.getX() + weightedOffsetX,
                        (int) this.pet.getY(),
                        (int) this.pet.getZ() + weightedOffsetZ,
                        NAVIGATION_SPEED);
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

    private int countDanceBlocksInRange() {
        int count = 0;
        for (int offsetX = SCAN_OFFSET_MIN; offsetX < SCAN_OFFSET_MAX; ++offsetX) {
            for (int offsetZ = SCAN_OFFSET_MIN; offsetZ < SCAN_OFFSET_MAX; ++offsetZ) {
                BlockPos pos = new BlockPos(
                        (int) this.pet.getX() + offsetX,
                        (int) this.pet.getY() - BLOCKS_BELOW_FEET,
                        (int) this.pet.getZ() + offsetZ);
                Block block = this.level.getBlockState(pos).getBlock();
                if (isDanceBlock(block)) count++;
            }
        }
        return count;
    }

    @Override
    public void tick() {
        int halfCycle = DANCE_TICK_CYCLE / 2;
        int moveDurationTicks = DANCE_TICK_CYCLE * DANCE_MOVE_DURATION_CYCLES;

        ++this.ticker;
        if (this.dance_move == 0) {
            this.dance_move = 1 + this.level.random.nextInt(DANCE_VARIANT_COUNT);
            this.pet.setDeltaMovement(this.pet.getDeltaMovement().multiply(0, 1, 0));
            this.ticker = 0;
            this.pet.setShiftKeyDown(false);
        }

        switch (this.dance_move) {
            case 1 -> {
                applyDirectionalDanceMove(DanceDirection.POS_X);
                if (this.ticker > moveDurationTicks) this.dance_move = 0;
            }
            case 2 -> {
                applyDirectionalDanceMove(DanceDirection.POS_Z);
                if (this.ticker > moveDurationTicks) this.dance_move = 0;
            }
            case 3 -> {
                this.pet.setShiftKeyDown(this.ticker % DANCE_TICK_CYCLE >= halfCycle);
                if (this.ticker > moveDurationTicks) this.dance_move = 0;
            }
            case 4 -> {
                if (this.ticker % halfCycle == 1) {
                    this.pet.swing(this.pet.getUsedItemHand());
                    this.pet.setDeltaMovement(this.pet.getDeltaMovement().add(0, VERTICAL_NUDGE, 0));
                }
                if (this.ticker > moveDurationTicks) this.dance_move = 0;
            }
            case 5 -> {
                if (this.ticker % halfCycle == 1) this.pet.swing(this.pet.getUsedItemHand());
                applyDirectionalDanceMove(DanceDirection.POS_X);
                if (this.ticker > moveDurationTicks) this.dance_move = 0;
            }
            case 6 -> {
                if (this.ticker % halfCycle == 1) this.pet.swing(this.pet.getUsedItemHand());
                applyDirectionalDanceMove(DanceDirection.POS_Z);
                if (this.ticker > moveDurationTicks) this.dance_move = 0;
            }
            case 7 -> {
                this.pet.setShiftKeyDown(this.ticker % DANCE_TICK_CYCLE >= halfCycle);
                applyDirectionalDanceMove(DanceDirection.POS_X);
                applyDirectionalDanceMove(DanceDirection.YAW);
                if (this.ticker > moveDurationTicks) this.dance_move = 0;
            }
            case 8 -> {
                this.pet.setShiftKeyDown(this.ticker % DANCE_TICK_CYCLE >= halfCycle);
                applyDirectionalDanceMove(DanceDirection.POS_Z);
                applyDirectionalDanceMove(DanceDirection.YAW);
                if (this.ticker > moveDurationTicks) this.dance_move = 0;
            }
            case 9 -> {
                this.pet.setShiftKeyDown(this.ticker % DANCE_TICK_CYCLE >= halfCycle);
                if (this.ticker % halfCycle == 1) this.pet.swing(this.pet.getUsedItemHand());
                applyDirectionalDanceMove(DanceDirection.POS_X);
                applyDirectionalDanceMove(DanceDirection.HEAD_YAW);
                if (this.ticker > moveDurationTicks) this.dance_move = 0;
            }
            case 10 -> {
                if (this.ticker % DANCE_TICK_CYCLE < halfCycle) {
                    this.pet.setShiftKeyDown(false);
                    this.pet.setDeltaMovement(this.pet.getDeltaMovement().add(0, VERTICAL_NUDGE, 0));
                } else {
                    this.pet.setShiftKeyDown(true);
                }
                if (this.ticker % halfCycle == 1) this.pet.swing(this.pet.getUsedItemHand());
                applyDirectionalDanceMove(DanceDirection.POS_Z);
                applyDirectionalDanceMove(DanceDirection.HEAD_YAW);
                if (this.ticker > moveDurationTicks) this.dance_move = 0;
            }
            default -> this.dance_move = 0;
        }
    }

    private enum DanceDirection {
        POS_X,
        POS_Z,
        YAW,
        HEAD_YAW
    }

    private void applyDirectionalDanceMove(DanceDirection direction) {
        float strafeX = 0;
        float strafeZ = 0;
        float bodyYawDelta = 0;
        float headYawDelta = 0;
        switch (direction) {
            case POS_X -> strafeX = STRAFE_SPEED;
            case POS_Z -> strafeZ = STRAFE_SPEED;
            case YAW -> bodyYawDelta = YAW_STEP_DEGREES;
            case HEAD_YAW -> headYawDelta = YAW_STEP_DEGREES;
        }
        int phase = this.ticker % DANCE_TICK_CYCLE;
        if (phase >= DANCE_TICK_CYCLE / 2) {
            strafeX = -strafeX;
            strafeZ = -strafeZ;
            bodyYawDelta = -bodyYawDelta;
            headYawDelta = -headYawDelta;
        }
        phase = this.ticker % (DANCE_TICK_CYCLE / 2);
        if (phase >= DANCE_TICK_CYCLE / 4) {
            bodyYawDelta = -bodyYawDelta;
            headYawDelta = -headYawDelta;
        }

        Vec3 motion = this.pet.getDeltaMovement();
        this.pet.setDeltaMovement(motion.x + strafeX, motion.y, motion.z + strafeZ);
        this.pet.setYRot(this.pet.getYRot() + bodyYawDelta);
        this.pet.yHeadRot += headYawDelta;
    }
}
