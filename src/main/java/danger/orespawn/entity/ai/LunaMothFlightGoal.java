package danger.orespawn.entity.ai;

import danger.orespawn.ModBlocks;
import danger.orespawn.entity.EntityLunaMoth;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Luna Moth flight behaviour — the port of orig EntityLunaMoth.java:117-156 ({@code updateAITasks}).
 *
 * <p>Extends {@link ButterflyIslandsHuntGoal} — the butterfly flight with the Islands vampire hunt in the retarget's
 * else branch, which orig EntityLunaMoth.java:117-122 inherited through {@code super.updateAITasks()} (the moth hunted
 * as a type-1 butterfly does, orig EntityButterfly.java:161-169, beside its own torch loop; ENT-S-141) — over the moth's
 * OWN flight numbers ({@link AmbientFlightGoal.Params#lunaMoth()}: the wander inside {@code nextInt(10) - nextInt(10)} on
 * x and z and {@code nextInt(6) - 2} on y over 25 tries, :128-132; the retarget {@code nextInt(100) == 0 || cell distSq
 * < 4.0f}, :126; the steering 0.5 / 0.68 / 0.5 with the blend 0.1f and moveForward 0.75f, :146-155) and with the torch
 * scan where 1.7.10 ran it: the retarget's ELSE branch (:133 — {@code else if (!worldObj.isDaytime() &&
 * rand.nextInt(10) == 0)}), at night, on a 1-in-10, on every tick the retarget did NOT fire — the iconic "moths are
 * attracted to torches" behaviour. {@link #onRetargetSkipped} is that branch (the hook {@link ButterflyIslandsHuntGoal#tick}
 * runs after the hunt, as orig's moth loop ran after {@code super.updateAITasks()}); HEAD had put the scan inside the
 * retarget under a covered sky ({@code !canSeeSky}), never by the time of day, and flew the moth on the butterfly's preset
 * (7 / 0.7 / 0.5) (ENT-S-143). The single flight target and the one retarget roll for orig's two loops (the butterfly's
 * on its private target, the moth's on its own) are the pre-existing single-target extraction, disclosed under ENT-S-141
 * and ENT-S-143.
 *
 * <p>The scan (:134-144): {@code closest} 99999 and the pick cleared (:134-137), the six-face shells i = 2..14 around the
 * truncated cell {@code ((int) posX, (int) posY, (int) posZ)} — the loop :138-141: {@code scan_it(x, y, z, i, i, i)}, the
 * first shell that finds a torch breaks, and past i = 6 the trailing {@code ++i} skips every other radius (2, 3, 4, 5, 6, 8,
 * 10, 12, 14); a torch found sets the flight target ABOVE it (:142-144, {@code (tx, ty + 1, tz)}). {@link #scanIt} is orig
 * :54-115 {@code scan_it} face for face (the ±x faces over y ± dy, z ± dz; the ±y faces over x ± dx, z ± dz; the ±z faces
 * over x ± dx, y ± dy — the + face probed before the − face at each step, a strictly nearer torch by squared distance from
 * the origin taking {@code closest} and {@code tx/ty/tz}; the EntityStinky.scanIt / AlienTorchSeekGoal shape). HEAD's
 * {@code findClosestTorch} walked the ±x faces alone (a torch straight ahead on z was never found).
 *
 * <p>The torch test — orig :63 / :70 / :81 / :88 / :99 / :106 {@code bid == Blocks.torch || bid == OreSpawnMain.ExtremeTorch}:
 * the single 1.7.10 torch block became TORCH + WALL_TORCH in the modern floor / wall split (soul torches did not exist in
 * 1.7.10 and are not matched — the AlienTorchSeekGoal mapping), and the mod's own Extreme Torch ({@link ModBlocks#EXTREME_TORCH}).
 *
 * <p>Cost bound: worst case the nine shells' six faces (about 15 K block reads) on a 1-in-10 night tick that did not
 * retarget — as 1.7.10's; aborts at the first shell that finds a torch.
 */
public class LunaMothFlightGoal extends ButterflyIslandsHuntGoal {
    /** orig EntityLunaMoth.java:133 — the torch scan's roll, {@code nextInt(10) == 0}. */
    private static final int TORCH_SCAN_ROLL_BOUND = 10;
    /** orig EntityLunaMoth.java:138 — the first shell radius. */
    private static final int TORCH_SCAN_MIN_RADIUS = 2;
    /** orig EntityLunaMoth.java:138 — {@code i < 15}: the last shell is 14. */
    private static final int TORCH_SCAN_LIMIT = 15;
    /** orig EntityLunaMoth.java:139-140 — past i = 6 the trailing {@code ++i} skips every other radius. */
    private static final int TORCH_SCAN_SKIP_FROM = 6;
    /** orig EntityLunaMoth.java:134 / :142 — {@code closest} reset to 99999; a find is anything nearer. */
    private static final int NO_MATCH = 99999;

    /** orig EntityLunaMoth.java:20-23 — {@code closest}, {@code tx}, {@code ty}, {@code tz}: the scan's nearest torch. */
    private int closest = NO_MATCH;
    private int tx;
    private int ty;
    private int tz;

    public LunaMothFlightGoal(EntityLunaMoth moth) {
        super(moth, Params.lunaMoth()); // the butterfly hunt goal over the moth's own preset (ENT-S-141; the numbers ENT-S-143's)
    }

    /**
     * orig EntityLunaMoth.java:133-145 — the retarget's else branch: at night ({@code !worldObj.isDaytime()}, the port's
     * {@code !level().isDay()} — the OriginalSpawnGates.isDaytime mapping) on a 1-in-10 (:133), the shells 2..14 with the
     * step doubled past 6 (:138-141), the flight target set above the nearest torch found (:142-144). Runs on every tick the
     * 1-in-100 / near-target retarget did not fire, after the inherited Islands hunt (ENT-S-143).
     */
    @Override
    protected void onRetargetSkipped() {
        if (!this.mob.level().isDay() && this.mob.getRandom().nextInt(TORCH_SCAN_ROLL_BOUND) == 0) { // orig :133
            this.closest = NO_MATCH; // orig :134
            this.tz = 0;             // orig :135
            this.ty = 0;             // orig :136
            this.tx = 0;             // orig :137
            int x = (int) this.mob.getX(); // orig :138 — the (int) casts (BUG-027)
            int y = (int) this.mob.getY();
            int z = (int) this.mob.getZ();
            for (int i = TORCH_SCAN_MIN_RADIUS; i < TORCH_SCAN_LIMIT && !this.scanIt(x, y, z, i, i, i); ++i) { // orig :138
                if (i < TORCH_SCAN_SKIP_FROM) continue; // orig :139
                ++i;                                    // orig :140
            }
            if (this.closest < NO_MATCH) {                                    // orig :142
                this.flightTarget = new BlockPos(this.tx, this.ty + 1, this.tz); // orig :143 — above the torch
            }
        }
    }

    /**
     * orig EntityLunaMoth.java:54-115 {@code scan_it}: the six faces of the (dx, dy, dz) shell around (x, y, z) — the ±x
     * faces over y ± dy, z ± dz (:60-77), the ±y faces over x ± dx, z ± dz (:78-95), the ±z faces over x ± dx, y ± dy
     * (:96-113), the + face probed before the − face at each step; a torch nearer than {@code closest} by squared distance
     * from (x, y, z) takes {@code closest} and {@code tx/ty/tz}; true when any probe took it (:114).
     */
    private boolean scanIt(int x, int y, int z, int dx, int dy, int dz) {
        int d;
        int found = 0;
        for (int i = -dy; i <= dy; ++i) {
            for (int j = -dz; j <= dz; ++j) {
                if (this.isTorch(x + dx, y + i, z + j) && (d = dx * dx + j * j + i * i) < this.closest) { // orig :62-69
                    this.closest = d;
                    this.tx = x + dx;
                    this.ty = y + i;
                    this.tz = z + j;
                    ++found;
                }
                if (this.isTorch(x - dx, y + i, z + j) && (d = dx * dx + j * j + i * i) < this.closest) { // orig :70-76
                    this.closest = d;
                    this.tx = x - dx;
                    this.ty = y + i;
                    this.tz = z + j;
                    ++found;
                }
            }
        }
        for (int i = -dx; i <= dx; ++i) {
            for (int j = -dz; j <= dz; ++j) {
                if (this.isTorch(x + i, y + dy, z + j) && (d = dy * dy + j * j + i * i) < this.closest) { // orig :80-87
                    this.closest = d;
                    this.tx = x + i;
                    this.ty = y + dy;
                    this.tz = z + j;
                    ++found;
                }
                if (this.isTorch(x + i, y - dy, z + j) && (d = dy * dy + j * j + i * i) < this.closest) { // orig :88-94
                    this.closest = d;
                    this.tx = x + i;
                    this.ty = y - dy;
                    this.tz = z + j;
                    ++found;
                }
            }
        }
        for (int i = -dx; i <= dx; ++i) {
            for (int j = -dy; j <= dy; ++j) {
                if (this.isTorch(x + i, y + j, z + dz) && (d = dz * dz + j * j + i * i) < this.closest) { // orig :98-105
                    this.closest = d;
                    this.tx = x + i;
                    this.ty = y + j;
                    this.tz = z + dz;
                    ++found;
                }
                if (this.isTorch(x + i, y + j, z - dz) && (d = dz * dz + j * j + i * i) < this.closest) { // orig :106-112
                    this.closest = d;
                    this.tx = x + i;
                    this.ty = y + j;
                    this.tz = z - dz;
                    ++found;
                }
            }
        }
        return found != 0; // orig :114
    }

    /**
     * orig EntityLunaMoth.java:63 (and :70 / :81 / :88 / :99 / :106) — {@code bid == Blocks.torch || bid == OreSpawnMain.ExtremeTorch}:
     * TORCH + WALL_TORCH for 1.7.10's one torch block (the AlienTorchSeekGoal mapping) and the mod's Extreme Torch.
     */
    private boolean isTorch(int bx, int by, int bz) {
        BlockState state = this.mob.level().getBlockState(new BlockPos(bx, by, bz));
        return state.is(Blocks.TORCH)
                || state.is(Blocks.WALL_TORCH)
                || (ModBlocks.EXTREME_TORCH != null && state.is(ModBlocks.EXTREME_TORCH.get()));
    }
}
