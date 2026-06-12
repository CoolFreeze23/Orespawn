package danger.orespawn.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import danger.orespawn.ModBlocks;
import danger.orespawn.ModEntities;
import danger.orespawn.entity.Cephadrome;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class BlockExtremeTorch extends TorchBlock {
    private static final double FLAME_CENTER_XZ = 0.5;
    private static final double FLAME_CENTER_Y = 0.7;

    public BlockExtremeTorch(BlockBehaviour.Properties properties) {
        super(ParticleTypes.FLAME, properties);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        double x = pos.getX() + FLAME_CENTER_XZ;
        double y = pos.getY() + FLAME_CENTER_Y;
        double z = pos.getZ() + FLAME_CENTER_XZ;

        level.addParticle(ParticleTypes.SMOKE, x, y, z, 0, 0, 0);
        level.addParticle(ParticleTypes.FLAME, x, y, z, 0, 0, 0);
        level.addParticle(ParticleTypes.DUST_PLUME, x, y, z, 0, 0, 0);
    }

    // orig BlockExtremeTorch.java:72-79 — Cephadrome spawn-spot search (ITEM-017):
    // 100 tries, ±4 blocks ± nextInt(3) jitter horizontally, y−2..y+2 vertically,
    // needs solid ground + 2 air blocks. No spawn (and no torch removal) if none found.
    private static final int SPAWN_SEARCH_TRIES = 100;
    private static final int SPAWN_BASE_OFFSET = 4;
    private static final int SPAWN_JITTER_BOUND = 3;
    private static final int SPAWN_VERTICAL_RADIUS = 2;

    /**
     * Summons a Cephadrome at a random nearby valid spot when the torch is
     * placed on an Eye-of-Ender block — orig BlockExtremeTorch.java:66-101.
     * The torch only consumes itself when a spawn spot was actually found.
     */
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);

        BlockState belowState = level.getBlockState(pos.below());
        if (!belowState.is(ModBlocks.BLOCK_EYE_OF_ENDER.get())) return;

        int spawnX = pos.getX();
        int spawnY = pos.getY();
        int spawnZ = pos.getZ();
        boolean found = false;

        for (int tries = 0; tries < SPAWN_SEARCH_TRIES && !found; tries++) {
            spawnX = level.random.nextInt(2) == 0
                    ? pos.getX() + SPAWN_BASE_OFFSET + level.random.nextInt(SPAWN_JITTER_BOUND) - level.random.nextInt(SPAWN_JITTER_BOUND)
                    : pos.getX() - SPAWN_BASE_OFFSET + level.random.nextInt(SPAWN_JITTER_BOUND) - level.random.nextInt(SPAWN_JITTER_BOUND);
            spawnZ = level.random.nextInt(2) == 0
                    ? pos.getZ() + SPAWN_BASE_OFFSET + level.random.nextInt(SPAWN_JITTER_BOUND) - level.random.nextInt(SPAWN_JITTER_BOUND)
                    : pos.getZ() - SPAWN_BASE_OFFSET + level.random.nextInt(SPAWN_JITTER_BOUND) - level.random.nextInt(SPAWN_JITTER_BOUND);
            for (spawnY = pos.getY() - SPAWN_VERTICAL_RADIUS; spawnY <= pos.getY() + SPAWN_VERTICAL_RADIUS; spawnY++) {
                BlockPos feet = new BlockPos(spawnX, spawnY, spawnZ);
                if (level.getBlockState(feet.below()).isSolid()
                        && level.getBlockState(feet).isAir()
                        && level.getBlockState(feet.above()).isAir()) {
                    found = true;
                    break;
                }
            }
        }

        if (!found) return;

        if (!level.isClientSide) {
            Cephadrome ceph = ModEntities.CEPHADROME.get().create(level);
            if (ceph != null) {
                // orig :84 — spawn at the found spot (y+0.01), random yaw
                ceph.moveTo(spawnX + 0.5, spawnY + 0.01, spawnZ + 0.5, level.random.nextFloat() * 360.0f, 0.0F);
                level.addFreshEntity(ceph);
                ceph.playAmbientSound();
            }
        }
        level.playSound(null, pos, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 1.0f,
                level.random.nextFloat() * 0.2f + 0.9f);
        // orig :97 — the torch turns to air only on a successful summon
        level.removeBlock(pos, false);
    }
}
