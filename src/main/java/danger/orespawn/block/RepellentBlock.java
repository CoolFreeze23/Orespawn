package danger.orespawn.block;

import com.mojang.serialization.MapCodec;
import danger.orespawn.entity.EntityAnt;
import danger.orespawn.entity.Kraken;
import danger.orespawn.entity.PurplePower;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Torch-like block that repels matching entities within a 20-block radius,
 * force proportional to (20 − distance).
 *
 * <p>1.7.10 cadence (ITEM-019): orig KrakenRepellent.java:58-75 /
 * CreeperRepellent.java:59-76 — tickRate 10; onBlockAdded, onNeighborBlockChange
 * and every updateTick re-schedule the next update, so the repel scan runs every
 * 10 game ticks (0.5 s), not on randomTick (~68 s average).</p>
 */
public class RepellentBlock extends Block {

    /** Which original repellent block this instance reproduces. */
    public enum Variant { KRAKEN, CREEPER }

    // orig KrakenRepellent.java:99-106 / CreeperRepellent.java:100-107
    private static final double REPEL_RADIUS = 20.0;
    private static final double REPEL_STRENGTH = 0.4;
    /** orig KrakenRepellent.java:58-60 — tickRate(world) == 10. */
    private static final int REPEL_INTERVAL_TICKS = 10;
    /** orig KrakenRepellent.java:82 — scan box y from −10 to +40 (Kraken floats high above). */
    private static final double KRAKEN_BOX_DOWN = 10.0;
    private static final double KRAKEN_BOX_UP = 40.0;
    /** orig KrakenRepellent.java:95 — the Kraken's distance is measured 15 blocks below it (its victim hold point). */
    private static final double KRAKEN_Y_OFFSET = 15.0;
    /** orig CreeperRepellent.java:83 — scan box y ±10. */
    private static final double CREEPER_BOX_VERTICAL = 10.0;

    // Shape parity (asset-audit follow-up): orig KrakenRepellent.java:21-22 /
    // CreeperRepellent.java:22-23 extend BlockTorch, so the block is a standing
    // torch — mirror vanilla TorchBlock/BaseTorchBlock's shape usage:
    // Block.box(6, 0, 6, 10, 10, 10) outline and NO collision (vanilla torches
    // get that via Properties.noCollission(); enforced here in-class since the
    // registration properties live in ModBlocks). Wall placement (BlockTorch
    // metadata 1-4 in 1.7.10) remains unimplemented — logged separately.
    private static final VoxelShape SHAPE = Block.box(6.0, 0.0, 6.0, 10.0, 10.0, 10.0);

    private final Variant variant;

    public RepellentBlock(BlockBehaviour.Properties properties, Variant variant) {
        super(properties);
        this.variant = variant;
    }

    /** Vanilla standing-torch outline (see SHAPE comment). */
    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    /** Torches never collide — orig BlockTorch parent had no collision box. */
    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected MapCodec<? extends RepellentBlock> codec() {
        return simpleCodec(p -> new RepellentBlock(p, Variant.KRAKEN));
    }

    /** orig func_149726_b (onBlockAdded) — schedule the first repel tick. */
    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        level.scheduleTick(pos, this, REPEL_INTERVAL_TICKS);
    }

    /** orig func_149695_a (onNeighborBlockChange) — also re-schedules. */
    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        level.scheduleTick(pos, this, REPEL_INTERVAL_TICKS);
    }

    /** orig func_149674_a (updateTick) — repel, then re-schedule in 10 ticks. */
    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        repel(level, pos);
        level.scheduleTick(pos, this, REPEL_INTERVAL_TICKS);
    }

    private void repel(ServerLevel level, BlockPos pos) {
        AABB area = switch (variant) {
            case KRAKEN -> new AABB(
                    pos.getX() - REPEL_RADIUS, pos.getY() - KRAKEN_BOX_DOWN, pos.getZ() - REPEL_RADIUS,
                    pos.getX() + REPEL_RADIUS, pos.getY() + KRAKEN_BOX_UP, pos.getZ() + REPEL_RADIUS);
            case CREEPER -> new AABB(
                    pos.getX() - REPEL_RADIUS, pos.getY() - CREEPER_BOX_VERTICAL, pos.getZ() - REPEL_RADIUS,
                    pos.getX() + REPEL_RADIUS, pos.getY() + CREEPER_BOX_VERTICAL, pos.getZ() + REPEL_RADIUS);
        };

        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, area)) {
            switch (variant) {
                case KRAKEN -> {
                    // orig KrakenRepellent.java:93-108 — Kraken (dy measured 15 below) + EntityAnt
                    if (entity instanceof Kraken) {
                        push(entity, pos, KRAKEN_Y_OFFSET);
                    }
                    if (entity instanceof EntityAnt) {
                        push(entity, pos, 0.0);
                    }
                }
                case CREEPER -> {
                    // orig CreeperRepellent.java:94-145 — Creeper + EntityAnt + PurplePower;
                    // quirk preserved: a type-10 PurplePower ABORTS the whole scan (orig :128-130 `return`)
                    if (entity instanceof Creeper) {
                        push(entity, pos, 0.0);
                    }
                    if (entity instanceof EntityAnt) {
                        push(entity, pos, 0.0);
                    }
                    if (entity instanceof PurplePower purple) {
                        if (purple.getPurpleType() == 10) {
                            return;
                        }
                        push(entity, pos, 0.0);
                    }
                }
            }
        }
    }

    /** Force = clamp(20 − dist, 0..20) × 0.4 applied horizontally away from the block. */
    private static void push(LivingEntity entity, BlockPos pos, double entityYOffset) {
        double dx = entity.getX() - pos.getX();
        double dy = entity.getY() - entityYOffset - pos.getY();
        double dz = entity.getZ() - pos.getZ();
        double force = REPEL_RADIUS - Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (force > REPEL_RADIUS) force = REPEL_RADIUS;
        if (force <= 0) return;
        force *= REPEL_STRENGTH;

        double angle = Math.atan2(entity.getX() - pos.getX(), entity.getZ() - pos.getZ());
        entity.setDeltaMovement(entity.getDeltaMovement().add(
                force * Math.sin(angle), 0, force * Math.cos(angle)));
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.7;
        double z = pos.getZ() + 0.5;

        level.addParticle(ParticleTypes.SMOKE, x, y + 0.21, z, 0, 0, 0);
        level.addParticle(ParticleTypes.FLAME, x, y + 0.21, z, 0, 0, 0);
        level.addParticle(ParticleTypes.DUST_PLUME, x, y + 0.21, z, 0, 0, 0);
    }
}
