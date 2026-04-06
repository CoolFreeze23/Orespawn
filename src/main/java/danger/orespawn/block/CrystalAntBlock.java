package danger.orespawn.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import danger.orespawn.ModEntities;

/**
 * Ant/termite spawner block. Different instances spawn different ant types.
 */
public class CrystalAntBlock extends Block {

    public enum AntType { BLACK_ANT, RED_ANT, UNSTABLE_ANT, TERMITE, CRYSTAL_TERMITE, RAINBOW_ANT }

    private final AntType antType;

    public CrystalAntBlock(BlockBehaviour.Properties properties, AntType antType) {
        super(properties);
        this.antType = antType;
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.isClientSide()) return;
        if (level.isRaining()) return;

        BlockPos above = pos.above();
        if (!level.getBlockState(above).is(Blocks.AIR)) return;

        int howmany = random.nextInt(6) + 2;
        for (int i = 0; i < howmany; i++) {
            EntityType<? extends Mob> entityType = getEntityType();
            if (entityType == null) continue;
            // TODO: Check per-ant-type enable config
            BlockButterflyPlant.spawnCreature(level, entityType, above);
        }
    }

    private EntityType<? extends Mob> getEntityType() {
        // TODO: Return the actual entity type from ModEntities based on antType
        // switch (antType) {
        //     case BLACK_ANT: return ModEntities.ANT.get();
        //     case RED_ANT: return ModEntities.RED_ANT.get();
        //     case UNSTABLE_ANT: return ModEntities.UNSTABLE_ANT.get();
        //     case TERMITE: return ModEntities.TERMITE.get();
        //     case CRYSTAL_TERMITE: return ModEntities.TERMITE.get();
        //     case RAINBOW_ANT: return ModEntities.RAINBOW_ANT.get();
        // }
        return null;
    }
}
