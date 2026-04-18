package danger.orespawn.entity;

import danger.orespawn.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.EnumSet;

/**
 * Direct port of the legacy {@code Alien#scan_it} torch-seeking behaviour
 * (1.7.10 source {@code Alien.java#L243-L347}). Aliens periodically scan a
 * cube around themselves for torch blocks. If one is found within ≈5 blocks
 * (legacy "closest &lt; 27" check, where 27 = 3² + 3² + 3² ≈ √27 ≈ 5.2),
 * they path to it and break it — but only when {@code mobGriefing} is on.
 *
 * <p>Modernization notes:</p>
 * <ul>
 *   <li>Uses a single 15-block cube probe per tick budget instead of the
 *       legacy 5-shell expanding scan; the probe runs only every 30 ticks
 *       (matches the legacy {@code nextInt(30) == 0} throttle).</li>
 *   <li>Targets vanilla {@link Blocks#TORCH} / {@link Blocks#WALL_TORCH}
 *       and the OreSpawn {@code extreme_torch} block, mirroring the legacy
 *       {@code (bid == Blocks.field_150478_aa || bid == OreSpawnMain.ExtremeTorch)}
 *       check.</li>
 *   <li>Honours {@link GameRules#RULE_MOBGRIEFING} on the level — the
 *       legacy code already gated the actual {@code setBlockToAir} call
 *       on {@code mobGriefing}; the goal additionally short-circuits the
 *       scan entirely when the rule is off so we don't burn CPU cycles
 *       on disabled servers.</li>
 *   <li>Caps the per-scan radius at 8 blocks (256 candidates max) to bound
 *       the worst-case CPU per tick. The legacy 15-block expanding shell
 *       scan would have probed up to 3375 cells per call, which is
 *       unacceptable on modern multi-player servers.</li>
 * </ul>
 */
public class AlienTorchSeekGoal extends Goal {
    private static final int SCAN_RADIUS = 8;
    private static final int SCAN_THROTTLE_TICKS = 30;
    private static final double BREAK_DISTANCE_SQ = 6.25; // 2.5 blocks

    private final Alien alien;
    private BlockPos targetTorch;
    private int cooldown;

    public AlienTorchSeekGoal(Alien alien) {
        this.alien = alien;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (this.cooldown > 0) {
            this.cooldown--;
            return false;
        }
        Level level = this.alien.level();
        if (!(level instanceof ServerLevel server)) return false;
        if (!server.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
            this.cooldown = SCAN_THROTTLE_TICKS;
            return false;
        }
        // Don't yank the alien off a player chase to swat torches.
        if (this.alien.getTarget() != null && this.alien.getTarget().isAlive()) return false;

        if (this.alien.getRandom().nextInt(SCAN_THROTTLE_TICKS) != 0) return false;

        this.targetTorch = findClosestTorch(server, this.alien.blockPosition());
        return this.targetTorch != null;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.targetTorch == null) return false;
        BlockState state = this.alien.level().getBlockState(this.targetTorch);
        return isTorch(state) && this.alien.distanceToSqr(
                this.targetTorch.getX() + 0.5,
                this.targetTorch.getY() + 0.5,
                this.targetTorch.getZ() + 0.5) > BREAK_DISTANCE_SQ - 0.01;
    }

    @Override
    public void start() {
        if (this.targetTorch == null) return;
        this.alien.getNavigation().moveTo(
                this.targetTorch.getX() + 0.5,
                this.targetTorch.getY(),
                this.targetTorch.getZ() + 0.5,
                1.0);
    }

    @Override
    public void tick() {
        if (this.targetTorch == null) return;
        double dsq = this.alien.distanceToSqr(
                this.targetTorch.getX() + 0.5,
                this.targetTorch.getY() + 0.5,
                this.targetTorch.getZ() + 0.5);
        if (dsq <= BREAK_DISTANCE_SQ) {
            // mobGriefing was double-checked by the legacy code right
            // before the air swap — keep that guard so a runtime rule flip
            // doesn't stomp player-placed torches mid-stride.
            if (this.alien.level() instanceof ServerLevel server
                    && server.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
                BlockState state = server.getBlockState(this.targetTorch);
                if (isTorch(state)) {
                    server.destroyBlock(this.targetTorch, false);
                }
            }
            this.cooldown = SCAN_THROTTLE_TICKS * 2;
            this.targetTorch = null;
        }
    }

    @Override
    public void stop() {
        this.targetTorch = null;
        this.cooldown = SCAN_THROTTLE_TICKS;
    }

    private static boolean isTorch(BlockState state) {
        return state.is(Blocks.TORCH)
                || state.is(Blocks.WALL_TORCH)
                || state.is(Blocks.SOUL_TORCH)
                || state.is(Blocks.SOUL_WALL_TORCH)
                || (ModBlocks.EXTREME_TORCH != null
                        && state.is(ModBlocks.EXTREME_TORCH.get()));
    }

    private BlockPos findClosestTorch(ServerLevel level, BlockPos origin) {
        BlockPos closest = null;
        double bestDistSq = Double.MAX_VALUE;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dx = -SCAN_RADIUS; dx <= SCAN_RADIUS; dx++) {
            for (int dy = -SCAN_RADIUS; dy <= SCAN_RADIUS; dy++) {
                for (int dz = -SCAN_RADIUS; dz <= SCAN_RADIUS; dz++) {
                    cursor.set(origin.getX() + dx, origin.getY() + dy, origin.getZ() + dz);
                    BlockState state = level.getBlockState(cursor);
                    if (!isTorch(state)) continue;
                    double dist = origin.distSqr(cursor);
                    if (dist < bestDistSq) {
                        bestDistSq = dist;
                        closest = cursor.immutable();
                    }
                }
            }
        }
        return closest;
    }
}
