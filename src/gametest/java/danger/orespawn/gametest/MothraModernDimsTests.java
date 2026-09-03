package danger.orespawn.gametest;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Mothra;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * MOD-029 (ACCEPTED 2026-09-03: "modern-mode default; classic keeps 5x2"):
 * Mothra's root hitbox under the modern config. orig Mothra.java:65
 * {@code setSize(5.0f, 2.0f)} is the classic box (ENT-S-095 batch 2, pinned
 * with the master forced off by
 * {@code HitboxDimsParityTests#s095_mothra_dims_both_modes}); the port's
 * original {@code .sized(6.0f, 3.0f)} is now the modern-mode box, reached only
 * while BOTH {@code [modern].enabled} (master, default true since the owner's
 * ruling of 2026-09-04; it was introduced with default false the day before) and
 * {@code [modern].mothraWideRootHitbox} (sub-key, default true) are on, and
 * snapshotted in the constructor the way the King's and Kraken's PlayNicely
 * boxes are (BOSS-017, orig TheKing.java:85-89; port
 * {@code Mothra#getDefaultDimensions}). Mothra has no PlayNicely size branch
 * (orig :63-70), so each mode must read the same box in both PlayNicely
 * states.
 *
 * <p>Pinned here, a fresh entity per state (constructor snapshot): (a) modern
 * off -> 5x2 in both PlayNicely states, even with the sub-key on; (b) modern
 * on -> 6x3 in both PlayNicely states; (c) sub-key off while modern is on ->
 * 5x2 (the sub-key is a real switch, not decoration); (d) flipping either key
 * on a live Mothra, through an explicit {@code refreshDimensions()}, leaves the
 * snapshot box in place in both directions. Both {@code getBbWidth/Height}
 * and the live {@link AABB} are checked, as in KrakenPlayNicelyTests.</p>
 *
 * <p>Batch (TEST-003): the three flags are GLOBAL. Every test here is
 * synchronous within one tick -- set, spawn, assert, restore in a
 * {@code finally} (the KrakenPlayNicelyTests idiom) -- so they share one own
 * batch, {@code mothraModernDims}, and never hold a flag across a tick.
 * Template {@code empty_large} is 48x16x48 with the spawn point at
 * (24, 8, 24).</p>
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class MothraModernDimsTests {

    /** orig Mothra.java:65 {@code setSize(5.0f, 2.0f)} -- the classic (registered) box. */
    private static final float CLASSIC_WIDTH = 5.0f;
    private static final float CLASSIC_HEIGHT = 2.0f;
    /** MOD-029: the port's original {@code .sized(6.0f, 3.0f)}, now the modern-mode box. */
    private static final float MODERN_WIDTH = 6.0f;
    private static final float MODERN_HEIGHT = 3.0f;
    private static final float EPS = 1e-4f;

    /** empty_large is 48x16x48; both boxes fit the template with room to spare. */
    private static final BlockPos POS_A = new BlockPos(24, 8, 24);
    private static final BlockPos POS_B = new BlockPos(12, 8, 12);

    /** The three global flags this class flips, read once per test and restored in every {@code finally}. */
    private record Flags(boolean modernEnabled, boolean wideRoot, boolean playNicely) {
        static Flags read() {
            return new Flags(OreSpawnConfig.MODERN_ENABLED.get(),
                    OreSpawnConfig.MODERN_MOTHRA_WIDE_ROOT_HITBOX.get(),
                    OreSpawnConfig.PLAY_NICELY.get());
        }

        void restore() {
            OreSpawnConfig.MODERN_ENABLED.set(this.modernEnabled);
            OreSpawnConfig.MODERN_MOTHRA_WIDE_ROOT_HITBOX.set(this.wideRoot);
            OreSpawnConfig.PLAY_NICELY.set(this.playNicely);
        }
    }

    private static void setFlags(boolean modernEnabled, boolean wideRoot, boolean playNicely) {
        OreSpawnConfig.MODERN_ENABLED.set(modernEnabled);
        OreSpawnConfig.MODERN_MOTHRA_WIDE_ROOT_HITBOX.set(wideRoot);
        OreSpawnConfig.PLAY_NICELY.set(playNicely);
    }

    private static Mothra spawnFrozen(GameTestHelper helper, BlockPos pos) {
        Mothra mothra = helper.spawnWithNoFreeWill(ModEntities.MOTHRA.get(), pos);
        mothra.setNoAi(true);
        mothra.setPersistenceRequired();
        return mothra;
    }

    private static void discard(Mothra... mothras) {
        for (Mothra mothra : mothras) {
            if (mothra != null) mothra.discard();
        }
    }

    private static void assertDims(GameTestHelper helper, Mothra mothra, float width, float height, String why) {
        AABB box = mothra.getBoundingBox();
        helper.assertTrue(Math.abs(mothra.getBbWidth() - width) < EPS
                        && Math.abs(mothra.getBbHeight() - height) < EPS,
                why + " -- expected " + width + "x" + height + ", got "
                        + mothra.getBbWidth() + "x" + mothra.getBbHeight() + " (MOD-029)");
        helper.assertTrue(Math.abs(box.getXsize() - width) < EPS
                        && Math.abs(box.getYsize() - height) < EPS
                        && Math.abs(box.getZsize() - width) < EPS,
                why + " -- bounding box not " + width + "x" + height + "x" + width + ", got "
                        + box.getXsize() + "x" + box.getYsize() + "x" + box.getZsize() + " (MOD-029)");
    }

    /**
     * Pins one modern configuration under BOTH PlayNicely states with a fresh
     * Mothra per state (ctor snapshot): playNicely=false -> spawn -> assert;
     * playNicely=true -> refreshDimensions() the first (live read: Mothra has
     * no PlayNicely size branch, orig :63-70) and spawn a second -> assert
     * both; discard both and restore all three flags in a finally.
     */
    private static void pinBothPlayNicelyStates(GameTestHelper helper, boolean modernEnabled, boolean wideRoot,
                                                float width, float height, String mode) {
        final Flags prior = Flags.read();
        Mothra normal = null;
        Mothra nice = null;
        try {
            setFlags(modernEnabled, wideRoot, false);
            normal = spawnFrozen(helper, POS_A);
            assertDims(helper, normal, width, height,
                    mode + ": Mothra constructed with playNicely=false must be " + width + "x" + height);

            OreSpawnConfig.PLAY_NICELY.set(true);
            normal.refreshDimensions();
            assertDims(helper, normal, width, height,
                    mode + ": refreshDimensions() with playNicely=true must not change Mothra"
                            + " (no PlayNicely size branch, orig Mothra.java:63-70)");
            nice = spawnFrozen(helper, POS_B);
            assertDims(helper, nice, width, height,
                    mode + ": Mothra constructed with playNicely=true must be " + width + "x" + height);
        } finally {
            prior.restore();
            discard(normal, nice);
        }
        helper.succeed();
    }

    /** Classic: master off (sub-key left on, its default) -> the registered 1.7.10 5x2 (orig Mothra.java:65). */
    @GameTest(template = "empty_large", batch = "mothraModernDims")
    public void mod029_mothra_classic_5x2_when_modern_off_both_play_nicely_states(GameTestHelper helper) {
        pinBothPlayNicelyStates(helper, false, true, CLASSIC_WIDTH, CLASSIC_HEIGHT,
                "classic (modern.enabled=false, mothraWideRootHitbox=true)");
    }

    /** Modern: master on and sub-key on -> the port's original 6x3 (MOD-029 modern-mode default). */
    @GameTest(template = "empty_large", batch = "mothraModernDims")
    public void mod029_mothra_modern_6x3_when_modern_on_both_play_nicely_states(GameTestHelper helper) {
        pinBothPlayNicelyStates(helper, true, true, MODERN_WIDTH, MODERN_HEIGHT,
                "modern (modern.enabled=true, mothraWideRootHitbox=true)");
    }

    /** The sub-key is a real switch: master on but sub-key off -> classic 5x2. */
    @GameTest(template = "empty_large", batch = "mothraModernDims")
    public void mod029_mothra_sub_key_off_keeps_classic_5x2_while_modern_on(GameTestHelper helper) {
        pinBothPlayNicelyStates(helper, true, false, CLASSIC_WIDTH, CLASSIC_HEIGHT,
                "modern master on, sub-key off (modern.enabled=true, mothraWideRootHitbox=false)");
    }

    /**
     * Live flips keep the constructor snapshot in both directions (BOSS-017:
     * orig TheKing.java:85-89; port TheKing/Kraken/Mothra#getDefaultDimensions),
     * through an explicit refreshDimensions(): a classic Mothra stays 5x2 once
     * the master is on, a modern Mothra stays 6x3 once the master or the
     * sub-key is off. Synchronous, so it shares the batch.
     */
    @GameTest(template = "empty_large", batch = "mothraModernDims")
    public void mod029_mothra_live_flip_keeps_constructor_snapshot(GameTestHelper helper) {
        final Flags prior = Flags.read();
        Mothra classic = null;
        Mothra modern = null;
        try {
            setFlags(false, true, false);
            classic = spawnFrozen(helper, POS_A);
            assertDims(helper, classic, CLASSIC_WIDTH, CLASSIC_HEIGHT,
                    "Mothra constructed with modern.enabled=false must be 5x2 (orig Mothra.java:65)");

            OreSpawnConfig.MODERN_ENABLED.set(true);
            modern = spawnFrozen(helper, POS_B);
            assertDims(helper, modern, MODERN_WIDTH, MODERN_HEIGHT,
                    "Mothra constructed with modern.enabled=true must be 6x3 (MOD-029)");

            // Master is on now: the classic Mothra must keep 5x2 through an explicit refresh.
            classic.refreshDimensions();
            assertDims(helper, classic, CLASSIC_WIDTH, CLASSIC_HEIGHT,
                    "turning modern.enabled on must not widen a live classic Mothra (ctor snapshot, BOSS-017)");

            // Master off again: the modern Mothra must keep 6x3 through an explicit refresh.
            OreSpawnConfig.MODERN_ENABLED.set(false);
            modern.refreshDimensions();
            assertDims(helper, modern, MODERN_WIDTH, MODERN_HEIGHT,
                    "turning modern.enabled off must not shrink a live modern Mothra (ctor snapshot, BOSS-017)");

            // Sub-key off with the master on: still the snapshot on both.
            setFlags(true, false, false);
            modern.refreshDimensions();
            classic.refreshDimensions();
            assertDims(helper, modern, MODERN_WIDTH, MODERN_HEIGHT,
                    "turning mothraWideRootHitbox off must not shrink a live modern Mothra (ctor snapshot, BOSS-017)");
            assertDims(helper, classic, CLASSIC_WIDTH, CLASSIC_HEIGHT,
                    "classic Mothra drifted after a round-trip flip (ctor snapshot, BOSS-017)");
        } finally {
            prior.restore();
            discard(classic, modern);
        }
        helper.succeed();
    }
}
