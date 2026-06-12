package danger.orespawn.block;

import com.mojang.serialization.MapCodec;
import danger.orespawn.entity.TheKing;
import danger.orespawn.entity.TheQueen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

/**
 * One-use boss summoner block, ported from the 1.7.10 King/Queen spawner
 * blocks (orig KingSpawnerBlock.java / QueenSpawnerBlock.java):
 *
 * <ul>
 *   <li>placement schedules a 100-tick fuse (orig KingSpawnerBlock.java:51-56,
 *       func_149726_b → func_147464_a(x,y,z,this,100)); the block also keeps
 *       random ticking as a fallback, mirroring orig's setTickRandomly(true)
 *       at :30;</li>
 *   <li>on detonation the block and the block above it are replaced with air
 *       (orig :69-70) and the boss spawns {@code spawnYOffset} blocks up
 *       (orig :67 — y+8 for King/Queen) with a random yaw and its living
 *       sound played (orig :85-87);</li>
 *   <li>King/Queen get {@code setGuardMode(1)} so they leash to the spawn
 *       point as a home anchor (orig :88);</li>
 *   <li>the spawn is gated by the boss enable config (orig :66 —
 *       TheKingEnable/TheQueenEnable); a disabled spawner still consumes
 *       itself without spawning, exactly like the original.</li>
 * </ul>
 */
public class BossSpawnerBlock extends Block {
    private static final int PARTICLE_BURST_COUNT = 20;
    /** orig KingSpawnerBlock.java:55 — 100-tick scheduled fuse. */
    private static final int FUSE_TICKS = 100;

    private final Supplier<? extends EntityType<?>> entityTypeSupplier;
    private final int spawnYOffset;
    private final Supplier<Boolean> enableGate;

    public BossSpawnerBlock(BlockBehaviour.Properties properties, Supplier<? extends EntityType<?>> entityTypeSupplier) {
        this(properties, entityTypeSupplier, 1, () -> true);
    }

    public BossSpawnerBlock(BlockBehaviour.Properties properties, Supplier<? extends EntityType<?>> entityTypeSupplier,
                            int spawnYOffset, Supplier<Boolean> enableGate) {
        super(properties);
        this.entityTypeSupplier = entityTypeSupplier;
        this.spawnYOffset = spawnYOffset;
        this.enableGate = enableGate;
    }

    @Override
    protected MapCodec<? extends BossSpawnerBlock> codec() {
        return simpleCodec(p -> new BossSpawnerBlock(p, () -> EntityType.PIG));
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide) {
            // orig KingSpawnerBlock.java:51-56 — 100-tick fuse on placement.
            level.scheduleTick(pos, this, FUSE_TICKS);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        detonate(level, pos, random);
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        detonate(level, pos, random);
    }

    /** orig KingSpawnerBlock.java:62-71 (func_149674_a) + spawn helper :81-91. */
    private void detonate(ServerLevel level, BlockPos pos, RandomSource random) {
        // orig :69-70 — the spawner and the block above it become air even
        // when the enable gate suppresses the spawn.
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
        level.setBlock(pos.above(), Blocks.AIR.defaultBlockState(), 2);

        if (!this.enableGate.get()) {
            return;
        }

        Entity entity = entityTypeSupplier.get().create(level);
        if (entity != null) {
            // orig :67/:85 — boss appears spawnYOffset blocks up at a random yaw.
            entity.moveTo(pos.getX() + 0.5, pos.getY() + this.spawnYOffset, pos.getZ() + 0.5,
                    random.nextFloat() * 360.0f, 0.0f);
            if (entity instanceof Mob mob) {
                mob.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.EVENT, null);
            }
            level.addFreshEntity(entity);
            if (entity instanceof Mob mob) {
                // orig :87 — func_70642_aH (playLivingSound) on spawn.
                mob.playAmbientSound();
            }
            // orig :88 — guard mode 1 anchors the boss to its spawn point.
            if (entity instanceof TheKing king) {
                king.setGuardMode(1);
            } else if (entity instanceof TheQueen queen) {
                queen.setGuardMode(1);
            }
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(20) != 0) return;
        for (int i = 0; i < PARTICLE_BURST_COUNT; i++) {
            level.addParticle(ParticleTypes.FIREWORK,
                    pos.getX() + random.nextFloat(),
                    pos.getY() + random.nextFloat(),
                    pos.getZ() + random.nextFloat(),
                    0, 0, 0);
        }
    }
}
