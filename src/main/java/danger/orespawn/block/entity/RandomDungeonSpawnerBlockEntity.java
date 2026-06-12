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
 *   <li>Tick 0 → 400: emit firework-spark particles for visual feedback
 *       (orig DungeonSpawnerBlock.java:35-40 schedules the build 400 ticks
 *       after placement — ITEM-020).</li>
 *   <li>Tick 400: remove self + the block above (orig :50-51), roll
 *       {@code nextInt(50)} against the table-driven structure pool
 *       (orig :52-202 — 50 outcomes, FairyTree → RedAntHangout).</li>
 * </ol>
 *
 * <p>Pool status: only the orig type 21 (MyDungeon.makeDungeon → generic
 * dungeon) and type 22 (RubyDungeon.makeDungeon) builders are ported so far;
 * unported indices currently fall back to the generic dungeon. As WGEN-042
 * ports more of the 50 structures, register them in {@link #buildForType}.</p>
 *
 * <p>Persisted across save/load via {@code Delay} NBT, so chunk unload + reload
 * during the countdown doesn't reset the timer.</p>
 */
public class RandomDungeonSpawnerBlockEntity extends BlockEntity {
    // orig DungeonSpawnerBlock.java:39 — world.scheduleBlockUpdate(..., 400)
    private static final int TOTAL_DELAY = 400; // 20 seconds at 20 TPS
    // orig DungeonSpawnerBlock.java:52 — nextInt(50) outcome roll
    private static final int STRUCTURE_POOL_SIZE = 50;
    private static final int TYPE_GENERIC_DUNGEON = 21;
    private static final int TYPE_RUBY_DUNGEON = 22;

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
        // orig DungeonSpawnerBlock.java:50-51 — spawner AND the block above → air
        server.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        server.setBlock(pos.above(), Blocks.AIR.defaultBlockState(), 3);

        // orig DungeonSpawnerBlock.java:52 — one roll over the 50-entry pool
        int type = server.random.nextInt(STRUCTURE_POOL_SIZE);
        boolean placed = buildForType(server, pos, type);

        if (placed) {
            server.playSound(null, pos, SoundEvents.GENERIC_EXPLODE.value(),
                    SoundSource.BLOCKS, 1.0f, 0.7f);
            server.sendParticles(net.minecraft.core.particles.ParticleTypes.EXPLOSION_EMITTER,
                    pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5,
                    1, 0, 0, 0, 0);
        }
    }

    /**
     * Table-driven outcome pool keyed by the ORIGINAL type index
     * (orig DungeonSpawnerBlock.java:52-202). Ported entries: 21 (generic
     * dungeon), 22 (ruby dungeon). Every still-unported index falls back to
     * the generic dungeon until WGEN-042 lands those structures — register
     * new builders here as they are ported.
     */
    private static boolean buildForType(ServerLevel server, BlockPos pos, int type) {
        return switch (type) {
            case TYPE_RUBY_DUNGEON -> GenericDungeon.placeRubyDungeonAt(server, server.random, pos);
            case TYPE_GENERIC_DUNGEON -> GenericDungeon.placeGenericDungeonAt(server, server.random, pos);
            // Interim fallback for the 48 not-yet-ported structures (Phase D / WGEN-042)
            default -> GenericDungeon.placeGenericDungeonAt(server, server.random, pos);
        };
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
