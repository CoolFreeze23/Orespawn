package danger.orespawn.gametest;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Godzilla;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * ENT-S-095 batch 2 (owner ruling 2026-09-03): the PlayNicely-branched boss
 * hitbox, pinned in BOTH modes as a constructor snapshot. Godzilla sizes
 * itself in the constructor exactly like the Kraken and the King: orig
 * Godzilla.java:71-75 {@code func_70105_a(9.9f, 25.0f)} when
 * {@code OreSpawnMain.PlayNicely == 0} (:72), {@code func_70105_a(2.475f, 6.25f)}
 * otherwise (:74) -- and never again, so the port snapshots the flag at
 * construction (Godzilla ctor, BOSS-017) and {@code Godzilla#getDefaultDimensions}
 * returns the matching box for life. The port had registered and returned
 * 10x25 against its own 9.9 comment (ModEntities / Godzilla.java); the ruling
 * restores 9.9x25, and the quarter-size nice box 2.475x6.25 already matched.
 *
 * <p>Idiom (KrakenPlayNicelyTests): each mode spawns a FRESH boss with the
 * flag preset -- a ctor snapshot cannot be re-read on a live entity -- asserts
 * {@code getBbWidth/Height} and the live {@link AABB} extents within
 * {@value #EPS}, and restores the flag in a {@code finally}. Batch (TEST-003):
 * the flag is GLOBAL; both tests are synchronous set-spawn-assert-restore and
 * share this class's own batch, {@code bossDimsPlayNicely}. Template
 * {@code empty_large} is 48x16x48; the 25-tall box tops out above it, which
 * only the framework's cleanup cares about (origin inside) -- the same
 * situation as the 15-tall Kraken pin.</p>
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class BossDimsPlayNicelyTests {

    /** orig Godzilla.java:72 {@code func_70105_a(9.9f, 25.0f)}. */
    private static final float GODZILLA_FULL_WIDTH = 9.9f;
    private static final float GODZILLA_FULL_HEIGHT = 25.0f;
    /** orig Godzilla.java:74 {@code func_70105_a(2.475f, 6.25f)} -- the 1.7.10 quarter of 9.9x25. */
    private static final float GODZILLA_NICE_WIDTH = 2.475f;
    private static final float GODZILLA_NICE_HEIGHT = 6.25f;
    private static final float EPS = 1e-4f;

    /** empty_large is 48x16x48; feet at y=8, the 9.9-wide box spans x/z 19.05..28.95, inside the template. */
    private static final BlockPos POS = new BlockPos(24, 8, 24);

    private static Godzilla spawnFrozen(GameTestHelper helper, BlockPos pos) {
        Godzilla godzilla = helper.spawnWithNoFreeWill(ModEntities.GODZILLA.get(), pos);
        godzilla.setNoAi(true);
        godzilla.setPersistenceRequired();
        return godzilla;
    }

    private static void assertDims(GameTestHelper helper, Godzilla godzilla, float width, float height, String why) {
        AABB box = godzilla.getBoundingBox();
        helper.assertTrue(Math.abs(godzilla.getBbWidth() - width) < EPS
                        && Math.abs(godzilla.getBbHeight() - height) < EPS,
                why + " -- expected " + width + "x" + height + ", got "
                        + godzilla.getBbWidth() + "x" + godzilla.getBbHeight() + " (ENT-S-095)");
        helper.assertTrue(Math.abs(box.getXsize() - width) < EPS
                        && Math.abs(box.getYsize() - height) < EPS
                        && Math.abs(box.getZsize() - width) < EPS,
                why + " -- bounding box not refreshed to " + width + "x" + height + "x" + width + ", got "
                        + box.getXsize() + "x" + box.getYsize() + "x" + box.getZsize() + " (ENT-S-095)");
    }

    /** Flag off at construction: orig Godzilla.java:71-72 {@code func_70105_a(9.9f, 25.0f)}; the port's uncited 10x25 was the finding. */
    @GameTest(template = "empty_large", batch = "bossDimsPlayNicely")
    public void s095_godzilla_hitbox_full_9_9x25_when_play_nicely_off(GameTestHelper helper) {
        final boolean prior = OreSpawnConfig.PLAY_NICELY.get();
        Godzilla godzilla = null;
        try {
            OreSpawnConfig.PLAY_NICELY.set(false);
            godzilla = spawnFrozen(helper, POS);
            assertDims(helper, godzilla, GODZILLA_FULL_WIDTH, GODZILLA_FULL_HEIGHT,
                    "Godzilla constructed with playNicely=false must be 9.9x25 (orig Godzilla.java:72 func_70105_a(9.9f, 25.0f); owner ruling 2026-09-03 restored the port's 10x25)");
        } finally {
            OreSpawnConfig.PLAY_NICELY.set(prior);
            if (godzilla != null) godzilla.discard();
        }
        helper.succeed();
    }

    /** Flag on at construction: orig Godzilla.java:73-74 {@code func_70105_a(2.475f, 6.25f)} -- the quarter of 9.9x25. */
    @GameTest(template = "empty_large", batch = "bossDimsPlayNicely")
    public void s095_godzilla_hitbox_nice_2_475x6_25_when_play_nicely_on(GameTestHelper helper) {
        final boolean prior = OreSpawnConfig.PLAY_NICELY.get();
        Godzilla godzilla = null;
        try {
            OreSpawnConfig.PLAY_NICELY.set(true);
            godzilla = spawnFrozen(helper, POS);
            assertDims(helper, godzilla, GODZILLA_NICE_WIDTH, GODZILLA_NICE_HEIGHT,
                    "Godzilla constructed with playNicely=true must be 2.475x6.25 (orig Godzilla.java:74 func_70105_a(2.475f, 6.25f), the 1.7.10 quarter of :72)");
        } finally {
            OreSpawnConfig.PLAY_NICELY.set(prior);
            if (godzilla != null) godzilla.discard();
        }
        helper.succeed();
    }
}
