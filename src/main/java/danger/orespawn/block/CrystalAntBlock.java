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
    private static final int MIN_ANTS_PER_TICK = 2;
    private static final int MAX_EXTRA_ANTS = 6;

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

        int antCount = random.nextInt(MAX_EXTRA_ANTS) + MIN_ANTS_PER_TICK;
        for (int i = 0; i < antCount; i++) {
            EntityType<? extends Mob> entityType = getEntityType();
            if (entityType == null) continue;
            BlockButterflyPlant.spawnCreature(level, entityType, above);
        }
    }

    private EntityType<? extends Mob> getEntityType() {
        return switch (antType) {
            case BLACK_ANT -> ModEntities.ENTITY_ANT.get();
            case RED_ANT -> ModEntities.ENTITY_RED_ANT.get();
            case UNSTABLE_ANT -> ModEntities.ENTITY_UNSTABLE_ANT.get();
            case TERMITE, CRYSTAL_TERMITE -> ModEntities.ENTITY_TERMITE.get();
            case RAINBOW_ANT -> ModEntities.ENTITY_RAINBOW_ANT.get();
        };
    }
}
