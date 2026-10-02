package danger.orespawn.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * TEST-020 — the game-test grid's origin on request. {@code GameTestServer.startTests} lays the suite's structure
 * grid out from a random origin, x and z uniform in ±14,999,992 (NeoForm GameTestServer.java), and logs it ("N tests
 * are now running at position x, y, z!"). With the development property {@code orespawn.gametest.origin} set to
 * {@code x,z} (the gameTestServer run passes it from {@code -PgametestOrigin=x,z}) the grid starts at that x and z
 * instead, the y as the framework sets it, so a run can be repeated at the grid origin of one that failed (with the same
 * tests and batches every row then stands where it stood; a row's own "failed at" position is not the grid origin).
 * The build gives such a run an empty world of its own, so no earlier run's builds stand there. Without the property
 * nothing changes; the class is applied only where the game-test server runs. Registered in
 * {@code orespawn.mixins.json} (common side).
 */
@Mixin(GameTestServer.class)
public abstract class GameTestOriginMixin {

    private static final int LIMIT = 14_999_992;

    @ModifyVariable(method = "startTests", at = @At("STORE"), ordinal = 0)
    private BlockPos orespawn$requestedOrigin(BlockPos origin) {
        String requested = System.getProperty("orespawn.gametest.origin");
        if (requested == null || requested.isBlank()) {
            return origin;
        }
        String[] xz = requested.split(",", -1);
        try {
            if (xz.length != 2) {
                throw new NumberFormatException("two values expected");
            }
            int x = Integer.parseInt(xz[0].trim());
            int z = Integer.parseInt(xz[1].trim());
            if (x < -LIMIT || x > LIMIT || z < -LIMIT || z > LIMIT) {
                throw new NumberFormatException("outside ±" + LIMIT);
            }
            return new BlockPos(x, origin.getY(), z);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("orespawn.gametest.origin must be x,z, two whole numbers within ±" + LIMIT
                    + " (the grid origin a game-test log names); got \"" + requested + "\" (" + e.getMessage() + ")", e);
        }
    }
}
