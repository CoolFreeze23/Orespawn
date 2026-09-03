package danger.orespawn.gametest;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Kraken;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * ENT-S-096: the Kraken's PlayNicely hitbox. orig Kraken.java:70-76 sizes the
 * boss in the constructor — {@code setSize(4.0f, 15.0f)} with
 * {@code OreSpawnMain.PlayNicely == 0} (:72-73), {@code setSize(1.3333334f,
 * 5.0f)} otherwise (:74-75) — and never again, so the box is a
 * construction-time snapshot exactly like TheKing's (orig TheKing.java:85-89,
 * BOSS-017 port {@code TheKing#getDefaultDimensions}). The renderer's /3
 * branch (orig RenderKraken.java:39-45) instead follows the DataWatcher copy
 * of the flag (orig Kraken.java:97 init, :111-113 accessor, :914 per-AI-step
 * sync), so a live config flip changes what is drawn but not what is hit.
 *
 * <p>Pinned here: (a) a Kraken constructed with the flag off is 4x15 and one
 * constructed with it on is 1.3333334x5 (the exact float the original used,
 * orig Kraken.java:75), both as {@code getBbWidth/Height} and as the live
 * {@link AABB}; (b) flipping the flag on a live Kraken — even through an
 * explicit {@code refreshDimensions()} and across ticks — leaves the snapshot
 * box in place, the way the King's does; (c) the synced datum behind
 * {@code getPlayNicely()} tracks the live flag once the AI steps run.
 *
 * <p>Batches (TEST-003): the flag is GLOBAL. The two synchronous set-spawn-
 * assert-restore tests share {@code krakenPlayNicely}; each test that holds
 * the flag flipped across ticks gets its own batch, since same-batch tests
 * run concurrently. Every mutation is restored in a {@code finally}.</p>
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class KrakenPlayNicelyTests {

    /** orig Kraken.java:73 {@code setSize(4.0f, 15.0f)}. */
    private static final float FULL_WIDTH = 4.0f;
    private static final float FULL_HEIGHT = 15.0f;
    /** orig Kraken.java:75 {@code setSize(1.3333334f, 5.0f)} — the original's exact float. */
    private static final float NICE_WIDTH = 1.3333334f;
    private static final float NICE_HEIGHT = 5.0f;
    private static final float EPS = 1e-4f;

    /** empty_large is 48x16x48; the 15-tall box tops out above the template, which only the framework's cleanup cares about (origin inside). */
    private static final BlockPos POS_A = new BlockPos(24, 8, 24);
    private static final BlockPos POS_B = new BlockPos(12, 8, 12);
    private static final int HOLD_TICKS = 5;
    private static final int SYNC_TICKS = 3;

    private static Kraken spawnFrozen(GameTestHelper helper, BlockPos pos) {
        Kraken kraken = helper.spawnWithNoFreeWill(ModEntities.KRAKEN.get(), pos);
        kraken.setNoAi(true);
        kraken.setPersistenceRequired();
        return kraken;
    }

    private static void assertDims(GameTestHelper helper, Kraken kraken, float width, float height, String why) {
        AABB box = kraken.getBoundingBox();
        helper.assertTrue(Math.abs(kraken.getBbWidth() - width) < EPS
                        && Math.abs(kraken.getBbHeight() - height) < EPS,
                why + " — expected " + width + "x" + height + ", got "
                        + kraken.getBbWidth() + "x" + kraken.getBbHeight() + " (ENT-S-096)");
        helper.assertTrue(Math.abs(box.getXsize() - width) < EPS
                        && Math.abs(box.getYsize() - height) < EPS
                        && Math.abs(box.getZsize() - width) < EPS,
                why + " — bounding box not refreshed to " + width + "x" + height + "x" + width + ", got "
                        + box.getXsize() + "x" + box.getYsize() + "x" + box.getZsize() + " (ENT-S-096)");
    }

    /** Flag off at construction: orig Kraken.java:72-73 {@code setSize(4.0f, 15.0f)}. */
    @GameTest(template = "empty_large", batch = "krakenPlayNicely")
    public void s096_kraken_hitbox_full_4x15_when_play_nicely_off(GameTestHelper helper) {
        final boolean prior = OreSpawnConfig.PLAY_NICELY.get();
        Kraken kraken = null;
        try {
            OreSpawnConfig.PLAY_NICELY.set(false);
            kraken = spawnFrozen(helper, POS_A);
            assertDims(helper, kraken, FULL_WIDTH, FULL_HEIGHT,
                    "Kraken constructed with playNicely=false must be 4x15 (orig Kraken.java:72-73)");
        } finally {
            OreSpawnConfig.PLAY_NICELY.set(prior);
            if (kraken != null) kraken.discard();
        }
        helper.succeed();
    }

    /** Flag on at construction: orig Kraken.java:74-75 {@code setSize(1.3333334f, 5.0f)}. */
    @GameTest(template = "empty_large", batch = "krakenPlayNicely")
    public void s096_kraken_hitbox_nice_1_3333334x5_when_play_nicely_on(GameTestHelper helper) {
        final boolean prior = OreSpawnConfig.PLAY_NICELY.get();
        Kraken kraken = null;
        try {
            OreSpawnConfig.PLAY_NICELY.set(true);
            kraken = spawnFrozen(helper, POS_A);
            assertDims(helper, kraken, NICE_WIDTH, NICE_HEIGHT,
                    "Kraken constructed with playNicely=true must be 1.3333334x5 (orig Kraken.java:74-75)");
        } finally {
            OreSpawnConfig.PLAY_NICELY.set(prior);
            if (kraken != null) kraken.discard();
        }
        helper.succeed();
    }

    /**
     * Live flip keeps the constructor snapshot, the way the King's does (orig
     * TheKing.java:85-89 / port TheKing constructor: "the hitbox never resizes
     * afterwards even if the config flips"): orig Kraken.java:70-76 only ever
     * calls setSize from the constructor. Both directions are checked, through
     * an explicit refreshDimensions() and again after {@value #HOLD_TICKS}
     * ticks with the flag held flipped. Own batch: the flag stays flipped
     * across the hold window.
     */
    @GameTest(template = "empty_large", timeoutTicks = 100, batch = "krakenPlayNicelyFlip")
    public void s096_kraken_live_flip_keeps_constructor_snapshot(GameTestHelper helper) {
        final boolean prior = OreSpawnConfig.PLAY_NICELY.get();
        Kraken full = null;
        Kraken nice = null;
        boolean armed = false;
        try {
            OreSpawnConfig.PLAY_NICELY.set(false);
            full = spawnFrozen(helper, POS_A);
            OreSpawnConfig.PLAY_NICELY.set(true);
            nice = spawnFrozen(helper, POS_B);
            assertDims(helper, full, FULL_WIDTH, FULL_HEIGHT,
                    "Kraken constructed with playNicely=false must be 4x15 (orig Kraken.java:72-73)");
            assertDims(helper, nice, NICE_WIDTH, NICE_HEIGHT,
                    "Kraken constructed with playNicely=true must be 1.3333334x5 (orig Kraken.java:74-75)");

            // Flag is now true: the full Kraken must keep 4x15 through an explicit refresh.
            full.refreshDimensions();
            assertDims(helper, full, FULL_WIDTH, FULL_HEIGHT,
                    "flipping playNicely on must not shrink a live Kraken (ctor snapshot, orig Kraken.java:70-76 / King BOSS-017)");

            // Flag off again: the nice Kraken must keep 1.3333334x5 through an explicit refresh.
            OreSpawnConfig.PLAY_NICELY.set(false);
            nice.refreshDimensions();
            assertDims(helper, nice, NICE_WIDTH, NICE_HEIGHT,
                    "flipping playNicely off must not grow a live nice Kraken (ctor snapshot, orig Kraken.java:70-76 / King BOSS-017)");
            full.refreshDimensions();
            assertDims(helper, full, FULL_WIDTH, FULL_HEIGHT,
                    "full Kraken drifted after a round-trip flip (ctor snapshot, orig Kraken.java:70-76)");

            // Hold the flag flipped (true) across ticks so vanilla's own per-tick paths run against it.
            OreSpawnConfig.PLAY_NICELY.set(true);
            final Kraken heldFull = full;
            final Kraken heldNice = nice;
            helper.runAfterDelay(HOLD_TICKS, () -> {
                try {
                    assertDims(helper, heldFull, FULL_WIDTH, FULL_HEIGHT,
                            "full Kraken resized while playNicely was held true for " + HOLD_TICKS
                                    + " ticks (ctor snapshot, orig Kraken.java:70-76 / King BOSS-017)");
                    assertDims(helper, heldNice, NICE_WIDTH, NICE_HEIGHT,
                            "nice Kraken resized while playNicely was held true for " + HOLD_TICKS
                                    + " ticks (ctor snapshot, orig Kraken.java:70-76 / King BOSS-017)");
                } finally {
                    OreSpawnConfig.PLAY_NICELY.set(prior);
                    heldFull.discard();
                    heldNice.discard();
                }
                helper.succeed();
            });
            armed = true;
        } finally {
            if (!armed) {
                OreSpawnConfig.PLAY_NICELY.set(prior);
                if (full != null) full.discard();
                if (nice != null) nice.discard();
            }
        }
    }

    /**
     * The renderer's input: {@code getPlayNicely()} is the synced watcher copy
     * (orig Kraken.java:97/:111-113) refreshed every AI step (orig :914, port
     * customServerAiStep — hence AI left ON here), so it must follow the live
     * flag in both directions even though the hitbox does not. Own batch: the
     * flag is held true across the first sync window. The Kraken is discarded
     * well before its 10-tick weather timer (orig :171) fires.
     */
    @GameTest(template = "empty_large", timeoutTicks = 100, batch = "krakenPlayNicelyDatum")
    public void s096_kraken_play_nicely_datum_tracks_live_flag(GameTestHelper helper) {
        final boolean prior = OreSpawnConfig.PLAY_NICELY.get();
        final Kraken kraken;
        try {
            OreSpawnConfig.PLAY_NICELY.set(true);
            kraken = helper.spawn(ModEntities.KRAKEN.get(), POS_A);
            kraken.setPersistenceRequired();
        } catch (RuntimeException e) {
            // a spawn failure must not leak the flag into later batches (TEST-003)
            OreSpawnConfig.PLAY_NICELY.set(prior);
            throw e;
        }
        final Runnable cleanup = () -> {
            OreSpawnConfig.PLAY_NICELY.set(prior);
            kraken.discard();
        };
        helper.runAfterDelay(SYNC_TICKS, () -> {
            boolean flipped = false;
            try {
                helper.assertTrue(kraken.getPlayNicely() == 1,
                        "getPlayNicely() must read 1 after " + SYNC_TICKS + " AI steps with playNicely=true"
                                + " (orig Kraken.java:914 watcher sync), got " + kraken.getPlayNicely() + " (ENT-S-096)");
                OreSpawnConfig.PLAY_NICELY.set(false);
                flipped = true;
            } finally {
                if (!flipped) cleanup.run();
            }
            helper.runAfterDelay(SYNC_TICKS, () -> {
                try {
                    helper.assertTrue(kraken.getPlayNicely() == 0,
                            "getPlayNicely() must read 0 after " + SYNC_TICKS + " AI steps with playNicely=false"
                                    + " (orig Kraken.java:914 watcher sync), got " + kraken.getPlayNicely() + " (ENT-S-096)");
                } finally {
                    cleanup.run();
                }
                helper.succeed();
            });
        });
    }
}
