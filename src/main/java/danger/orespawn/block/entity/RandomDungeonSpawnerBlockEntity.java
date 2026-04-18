package danger.orespawn.block.entity;

import danger.orespawn.ModBlockEntities;
import danger.orespawn.world.GenericDungeon;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Countdown BlockEntity for {@link danger.orespawn.block.RandomDungeonSpawnerBlock}.
 *
 * <p>Strategy:
 * <ol>
 *   <li>Tick 0 → 200: emit firework-spark particles for visual feedback.</li>
 *   <li>Tick 200: remove self, then call into {@link GenericDungeon#tryPlaceGenericDungeon}
 *       (or {@code tryPlaceRubyDungeon} on a 1-in-4 roll) using the block's position
 *       as the structure origin. Both methods are worldgen-style and work fine on
 *       the live {@link ServerLevel}.</li>
 * </ol>
 *
 * <p>Persisted across save/load via {@code Delay} NBT, so chunk unload + reload
 * during the countdown doesn't reset the timer.</p>
 */
public class RandomDungeonSpawnerBlockEntity extends BlockEntity {
    private static final int TOTAL_DELAY = 200; // 10 seconds at 20 TPS

    private int delay = TOTAL_DELAY;

    public RandomDungeonSpawnerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RANDOM_DUNGEON_SPAWNER_BE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, RandomDungeonSpawnerBlockEntity be) {
        if (!(level instanceof ServerLevel server)) return;

        // Particle/sound feedback every 20 ticks
        if (be.delay % 20 == 0) {
            server.sendParticles(net.minecraft.core.particles.ParticleTypes.FIREWORK,
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    8, 0.3, 0.3, 0.3, 0.05);
            server.playSound(null, pos, SoundEvents.FIREWORK_ROCKET_LAUNCH,
                    SoundSource.BLOCKS, 0.4f, 1.5f);
        }

        be.delay--;
        if (be.delay <= 0) {
            be.detonate(server, pos);
        }
        be.setChanged();
    }

    private void detonate(ServerLevel server, BlockPos pos) {
        server.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);

        // 1-in-4 chance for the ruby variant; otherwise generic mossy dungeon
        boolean rubyVariant = server.random.nextInt(4) == 0;
        boolean placed = rubyVariant
                ? GenericDungeon.tryPlaceRubyDungeon(server, server.random, pos)
                : GenericDungeon.tryPlaceGenericDungeon(server, server.random, pos);

        if (!placed) {
            // Fallback: try the other variant in case the first failed terrain checks
            placed = rubyVariant
                    ? GenericDungeon.tryPlaceGenericDungeon(server, server.random, pos)
                    : GenericDungeon.tryPlaceRubyDungeon(server, server.random, pos);
        }

        if (placed) {
            server.playSound(null, pos, SoundEvents.GENERIC_EXPLODE.value(),
                    SoundSource.BLOCKS, 1.0f, 0.7f);
            server.sendParticles(net.minecraft.core.particles.ParticleTypes.EXPLOSION_EMITTER,
                    pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5,
                    1, 0, 0, 0, 0);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("Delay", this.delay);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.delay = tag.contains("Delay") ? tag.getInt("Delay") : TOTAL_DELAY;
    }
}
