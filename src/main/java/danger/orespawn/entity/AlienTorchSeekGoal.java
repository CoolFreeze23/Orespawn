package danger.orespawn.entity;

import danger.orespawn.ModBlocks;
import danger.orespawn.OreSpawnConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Faithful port of the 1.7.10 Alien torch hunt: driver at orig
 * Alien.java:328-343, shell scan at orig Alien.java:243-304 ({@code scan_it}).
 *
 * <p>The original logic lived in {@code func_70619_bc}, OUTSIDE the 1.7.10
 * AI-task mutex system — it rolled its dice every tick regardless of which
 * tasks held the movement mutex, issued a single {@code tryMoveToXYZ} and
 * (conditionally) removed the torch block in the same tick. This goal is
 * therefore flagless (it claims no {@link Goal.Flag}, so it never competes
 * with MoveThroughVillage/wander for the MOVE mutex) and one-shot
 * ({@link #canContinueToUse()} is always false): each successful roll
 * reproduces exactly one original scan tick.</p>
 *
 * <p>Original per-tick gating (orig Alien.java:314/:328): the torch branch is
 * the {@code else} of the 1-in-8 combat roll, so it can only fire on the
 * other 7/8 of ticks, and then only on a 1-in-30 roll with PlayNicely off.
 * The combat branch lives in {@link Alien#customServerAiStep()}, so the 1-in-8
 * gate is reproduced here with an independent roll of the same odds.</p>
 */
public class AlienTorchSeekGoal extends Goal {

    /** orig Alien.java:45 — sentinel meaning "no torch found yet". */
    private static final int NO_TORCH = 99999;

    private final Alien alien;
    private int closest = NO_TORCH; // orig Alien.java:45
    private int tx;                 // orig Alien.java:46-48
    private int ty;
    private int tz;

    public AlienTorchSeekGoal(Alien alien) {
        this.alien = alien;
        // No setFlags: the orig ran outside the task mutex system (see class doc).
    }

    @Override
    public boolean canUse() {
        // orig Alien.java:314 — torch branch is the else of the 1-in-8 combat roll.
        if (this.alien.getRandom().nextInt(8) == 0) return false;
        // orig Alien.java:328 — 1-in-30 cadence, PlayNicely must be off.
        if (this.alien.getRandom().nextInt(30) != 0) return false;
        if (OreSpawnConfig.PLAY_NICELY.get()) return false;

        // orig Alien.java:329-332
        this.closest = NO_TORCH;
        this.tx = 0;
        this.ty = 0;
        this.tz = 0;

        // orig Alien.java:333-336 — expanding cube shells r=2..14; the
        // "if (i < 10) continue; ++i;" body skips shells 11 and 13, and the
        // loop stops at the first shell containing a torch. Interior cells
        // are never probed, so a torch within 1 block on every axis is
        // invisible to the scan — original bug, kept. Truncating (int)
        // casts (not floor) are the orig's, kept for negative-coordinate parity.
        int x = (int) this.alien.getX();
        int y = (int) this.alien.getY();
        int z = (int) this.alien.getZ();
        for (int i = 2; i < 15 && !this.scanShell(x, y, z, i); ++i) {
            if (i < 10) continue;
            ++i;
        }
        return this.closest < NO_TORCH;
    }

    /** One-shot: the orig acted once per successful roll (see class doc). */
    @Override
    public boolean canContinueToUse() {
        return false;
    }

    @Override
    public void start() {
        // orig Alien.java:337-342 — path to the torch at speed 1.0; only the
        // block removal is mobGriefing-gated (the scan and pathing are not),
        // and it fires instantly when the torch is already within distSq<27
        // (~5.2 blocks) rather than on arrival. setBlock flag 2 (send to
        // client, no neighbor updates, no drops) mirrors func_147465_d(...,2).
        this.alien.getNavigation().moveTo(this.tx, this.ty, this.tz, 1.0);
        if (this.closest < 27
                && this.alien.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
            this.alien.level().setBlock(new BlockPos(this.tx, this.ty, this.tz),
                    Blocks.AIR.defaultBlockState(), 2);
        }
    }

    @Override
    public void stop() {
        // Intentionally does not cancel navigation: the orig's tryMoveToXYZ
        // persisted until another AI action re-pathed.
    }

    /**
     * orig Alien.java:243-304 ({@code scan_it}) with dx=dy=dz=r (the orig is
     * only ever called with equal radii). Probes the six faces of the cube
     * shell at radius r, keeping the closest torch by true distance².
     */
    private boolean scanShell(int x, int y, int z, int r) {
        int found = 0;
        // X faces (orig :249-266): i = y offset, j = z offset.
        for (int i = -r; i <= r; ++i) {
            for (int j = -r; j <= r; ++j) {
                if (this.checkTorch(x + r, y + i, z + j, r * r + j * j + i * i)) ++found;
                if (this.checkTorch(x - r, y + i, z + j, r * r + j * j + i * i)) ++found;
            }
        }
        // Y faces (orig :267-284): i = x offset, j = z offset.
        for (int i = -r; i <= r; ++i) {
            for (int j = -r; j <= r; ++j) {
                if (this.checkTorch(x + i, y + r, z + j, r * r + j * j + i * i)) ++found;
                if (this.checkTorch(x + i, y - r, z + j, r * r + j * j + i * i)) ++found;
            }
        }
        // Z faces (orig :285-302): i = x offset, j = y offset.
        for (int i = -r; i <= r; ++i) {
            for (int j = -r; j <= r; ++j) {
                if (this.checkTorch(x + i, y + j, z + r, r * r + j * j + i * i)) ++found;
                if (this.checkTorch(x + i, y + j, z - r, r * r + j * j + i * i)) ++found;
            }
        }
        return found != 0;
    }

    /** orig Alien.java:252-258 — update closest/tx/ty/tz when a strictly closer torch is seen. */
    private boolean checkTorch(int bx, int by, int bz, int distSq) {
        if (distSq >= this.closest) return false;
        if (!isTorch(this.alien.level().getBlockState(new BlockPos(bx, by, bz)))) return false;
        this.closest = distSq;
        this.tx = bx;
        this.ty = by;
        this.tz = bz;
        return true;
    }

    /**
     * orig Alien.java:252 — {@code bid == Blocks.torch || bid == OreSpawnMain.ExtremeTorch}.
     * The single 1.7.10 torch block became TORCH + WALL_TORCH in the modern
     * floor/wall split; soul torches did not exist in 1.7.10 and are not matched.
     */
    private static boolean isTorch(BlockState state) {
        return state.is(Blocks.TORCH)
                || state.is(Blocks.WALL_TORCH)
                || (ModBlocks.EXTREME_TORCH != null
                        && state.is(ModBlocks.EXTREME_TORCH.get()));
    }
}
