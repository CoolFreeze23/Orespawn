package danger.orespawn.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class BlockTitanium extends Block {
    public BlockTitanium(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(20) != 0) return;
        sparkle(level, pos, random);
    }

    private void sparkle(Level level, BlockPos pos, RandomSource random) {
        double offset = 0.0625;
        for (int face = 0; face < 6; face++) {
            double x = pos.getX() + random.nextFloat();
            double y = pos.getY() + random.nextFloat();
            double z = pos.getZ() + random.nextFloat();

            if (face == 0 && !level.getBlockState(pos.above()).canOcclude()) y = pos.getY() + 1 + offset;
            if (face == 1 && !level.getBlockState(pos.below()).canOcclude()) y = pos.getY() - offset;
            if (face == 2 && !level.getBlockState(pos.south()).canOcclude()) z = pos.getZ() + 1 + offset;
            if (face == 3 && !level.getBlockState(pos.north()).canOcclude()) z = pos.getZ() - offset;
            if (face == 4 && !level.getBlockState(pos.east()).canOcclude()) x = pos.getX() + 1 + offset;
            if (face == 5 && !level.getBlockState(pos.west()).canOcclude()) x = pos.getX() - offset;

            if (x < pos.getX() || x > pos.getX() + 1 || y < 0 || y > pos.getY() + 1 || z < pos.getZ() || z > pos.getZ() + 1) {
                int which = random.nextInt(3);
                if (which == 0) level.addParticle(ParticleTypes.FLAME, x, y, z, 0, 0, 0);
                if (which == 1) level.addParticle(ParticleTypes.SMOKE, x, y, z, 0, 0, 0);
                if (which == 2) level.addParticle(ParticleTypes.DUST_PLUME, x, y, z, 0, 0, 0);
            }
        }
    }
}
