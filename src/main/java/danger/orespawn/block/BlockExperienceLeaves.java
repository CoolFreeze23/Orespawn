package danger.orespawn.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.projectile.ThrownExperienceBottle;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class BlockExperienceLeaves extends LeavesBlock {
    private static final long TICKS_PER_DAY = 24000L;
    private static final long XP_DROP_WINDOW_START = 14000L;
    private static final long XP_DROP_WINDOW_END = 22000L;
    private static final int XP_BOTTLE_DROP_ROLL_BOUND = 65;
    private static final int XP_BOTTLE_DROP_SUCCESS_INDEX = 1;
    private static final int THROWN_BOTTLE_ROLL_BOUND = 75;
    private static final int THROWN_BOTTLE_SUCCESS_INDEX = 1;
    private static final double THROWN_BOTTLE_HORIZONTAL_SPREAD = 2.0;
    private static final double THROWN_BOTTLE_VERTICAL_IMPULSE = -0.1;

    private static final long PARTICLE_WINDOW_START = 13000L;
    private static final long PARTICLE_WINDOW_END = 23000L;
    private static final long DAWN_BLEND_END = 14000L;
    private static final long DUSK_BLEND_START = 22000L;
    private static final int TWILIGHT_RATE_DIVISOR = 2;
    private static final int ABOVE_PARTICLE_BASE_INTERVAL = 200;
    private static final int BELOW_PARTICLE_BASE_INTERVAL = 40;
    private static final int ABOVE_PARTICLE_BURST_COUNT = 10;
    private static final int BELOW_PARTICLE_BURST_COUNT = 4;
    private static final double ABOVE_PARTICLE_Y = 1.25;
    private static final double BELOW_PARTICLE_Y = -1.25;

    public BlockExperienceLeaves(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.randomTick(state, level, pos, random);
        if (level.isClientSide()) return;

        long timeOfDayTicks = level.getDayTime() % TICKS_PER_DAY;
        if (timeOfDayTicks < XP_DROP_WINDOW_START || timeOfDayTicks > XP_DROP_WINDOW_END) return;

        BlockPos above = pos.above();
        BlockPos below = pos.below();

        if (random.nextInt(XP_BOTTLE_DROP_ROLL_BOUND) == XP_BOTTLE_DROP_SUCCESS_INDEX && level.getBlockState(above).is(Blocks.AIR)) {
            Block.popResource(level, above, new ItemStack(Items.EXPERIENCE_BOTTLE));
        }

        if (random.nextInt(THROWN_BOTTLE_ROLL_BOUND) == THROWN_BOTTLE_SUCCESS_INDEX && level.getBlockState(below).is(Blocks.AIR)) {
            ThrownExperienceBottle bottle = new ThrownExperienceBottle(level, pos.getX(), pos.getY() - 1, pos.getZ());
            bottle.setDeltaMovement(
                    (random.nextFloat() - random.nextFloat()) / THROWN_BOTTLE_HORIZONTAL_SPREAD,
                    THROWN_BOTTLE_VERTICAL_IMPULSE,
                    (random.nextFloat() - random.nextFloat()) / THROWN_BOTTLE_HORIZONTAL_SPREAD
            );
            level.addFreshEntity(bottle);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);

        long timeOfDayTicks = level.getDayTime() % TICKS_PER_DAY;
        if (timeOfDayTicks < PARTICLE_WINDOW_START || timeOfDayTicks > PARTICLE_WINDOW_END) return;

        int twilightRollBonus = 0;
        if (timeOfDayTicks < DAWN_BLEND_END) {
            twilightRollBonus = (int) ((DAWN_BLEND_END - timeOfDayTicks) / TWILIGHT_RATE_DIVISOR);
        }
        if (timeOfDayTicks > DUSK_BLEND_START) {
            twilightRollBonus = (int) ((timeOfDayTicks - DUSK_BLEND_START) / TWILIGHT_RATE_DIVISOR);
        }

        BlockPos above = pos.above();
        BlockPos below = pos.below();

        if (random.nextInt(ABOVE_PARTICLE_BASE_INTERVAL + twilightRollBonus) == 1 && level.getBlockState(above).is(Blocks.AIR)) {
            for (int p = 0; p < ABOVE_PARTICLE_BURST_COUNT; p++) {
                level.addParticle(ParticleTypes.FIREWORK,
                        pos.getX() + 0.5, pos.getY() + ABOVE_PARTICLE_Y, pos.getZ() + 0.5,
                        random.nextGaussian(), Math.abs(random.nextGaussian()), random.nextGaussian());
            }
        }

        if (random.nextInt(BELOW_PARTICLE_BASE_INTERVAL + twilightRollBonus) == 1 && level.getBlockState(below).is(Blocks.AIR)) {
            for (int p = 0; p < BELOW_PARTICLE_BURST_COUNT; p++) {
                level.addParticle(ParticleTypes.FIREWORK,
                        pos.getX() + 0.5, pos.getY() + BELOW_PARTICLE_Y, pos.getZ() + 0.5,
                        random.nextFloat() - random.nextFloat(),
                        -Math.abs(random.nextFloat()),
                        random.nextFloat() - random.nextFloat());
            }
        }
    }
}
