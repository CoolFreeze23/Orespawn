package danger.orespawn.gametest;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.TheQueen;
import de.dertoaster.multihitboxlib.api.IMultipartEntity;
import de.dertoaster.multihitboxlib.entity.MHLibPartEntity;
import de.dertoaster.multihitboxlib.entity.hitbox.HitboxProfile;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.neoforged.neoforge.entity.PartEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * ENT-S-095 batch 3: The Queen's PlayNicely hitbox. orig TheQueen.java:78-82
 * sizes the boss in the constructor — {@code setSize(22.0f, 24.0f)} with
 * {@code OreSpawnMain.PlayNicely == 0} (:78-79), {@code setSize(5.5f, 6.0f)}
 * otherwise (:80-82) — and never again, so the box is a construction-time
 * snapshot exactly like TheKing's (BOSS-017) and the Kraken's (ENT-S-096).
 *
 * <p>Why this needs its own pin: the Queen is an MHLib multipart entity and
 * MHLib's {@code EntityEventHandler.onEntitySizeEvent} replaces her dimensions
 * with the profile main size [22, 24] on every {@code EntityEvent.Size}, which
 * made the 5.5x6 box dead in both modes. The fix keeps the profile main size at
 * [22, 24] (main-size law: it must equal the registered EntityType dims) and
 * applies {@code TheQueen#mhlibGetEntitySizeScale} (1.0 / 0.25) both to the main
 * box ({@code TheQueen.PlayNicelySizeHook}, after MHLib's handler) and — through
 * MHLib's own {@code IMHLibSizeCallback} resolution — to every part's size,
 * pivot and fallback offset.
 *
 * <p>Pinned here: (a) a Queen constructed with the flag off is 22x24 and one
 * constructed with it on is 5.5x6, both as {@code getBbWidth/Height} and as the
 * live {@link AABB}; (b) the MHLib side reports the same box: the resolved
 * profile's main size is [22, 24] in both modes and
 * {@code IMultipartEntity#mhlibGetEntitySizeInternally} (the vendored
 * resolution path, IMultipartEntity.java:382-393) returns 1.0 / 0.25, so
 * profile main size x entity scale equals the live box; (c) after the parts
 * have been aligned by {@code alignSynchedSubParts} (the fallback path — no
 * client bone stream exists in a headless test) every one of the ten part
 * boxes of a PlayNicely Queen is exactly one quarter of its hostile twin's;
 * (d) a live config flip leaves the constructor snapshot in place, the way the
 * King's and the Kraken's do.
 *
 * <p>Template: empty_large is 48x16x48; the 22x24 box at y=8 tops out above the
 * template exactly like the 15-tall Kraken in KrakenPlayNicelyTests (POS_A
 * (24, 8, 24), which passed) — only the origin has to be inside, and the Queen
 * has {@code noPhysics} anyway.
 *
 * <p>Batches (TEST-003): the flag is GLOBAL. The two synchronous
 * set-spawn-assert-restore tests share {@code queenPlayNicelyDims}; the parts
 * test restores the flag before its first tick (the entity scale reads the
 * constructor snapshot, not the live flag) but waits on ticks, so it gets its
 * own batch. Every mutation is restored in a {@code finally}.</p>
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class QueenPlayNicelyDimsTests {

    /** orig TheQueen.java:78-79 {@code func_70105_a(22.0f, 24.0f)}; ModEntities .sized(22, 24); profile main-hitbox.size [22, 24]. */
    private static final float FULL_WIDTH = 22.0f;
    private static final float FULL_HEIGHT = 24.0f;
    /** orig TheQueen.java:80-82 {@code func_70105_a(5.5f, 6.0f)} = 22/4 x 24/4. */
    private static final float NICE_WIDTH = 5.5f;
    private static final float NICE_HEIGHT = 6.0f;
    /** TheQueen.mhlibGetEntitySizeScale: 1.0 hostile, 0.25 while playNicelyShrunk. */
    private static final double FULL_SCALE = 1.0D;
    private static final double NICE_SCALE = 0.25D;
    private static final int PART_COUNT = 10;
    private static final float EPS = 1e-4f;

    /** empty_large is 48x16x48; the 24-tall box tops out above the template, which only the framework's cleanup cares about (origin inside) — Kraken precedent. */
    private static final BlockPos POS_A = new BlockPos(24, 8, 24);
    private static final BlockPos POS_B = new BlockPos(12, 8, 12);
    /** alignSynchedSubParts runs at the TAIL of LivingEntity.aiStep every tick, AI or not; a few ticks of margin. */
    private static final int ALIGN_TICKS = 3;

    private static TheQueen spawnFrozen(GameTestHelper helper, BlockPos pos) {
        TheQueen queen = helper.spawnWithNoFreeWill(ModEntities.THE_QUEEN.get(), pos);
        queen.setNoAi(true);
        queen.setPersistenceRequired();
        return queen;
    }

    private static void assertDims(GameTestHelper helper, TheQueen queen, float width, float height, String why) {
        AABB box = queen.getBoundingBox();
        helper.assertTrue(Math.abs(queen.getBbWidth() - width) < EPS
                        && Math.abs(queen.getBbHeight() - height) < EPS,
                why + " — expected " + width + "x" + height + ", got "
                        + queen.getBbWidth() + "x" + queen.getBbHeight() + " (ENT-S-095 batch 3)");
        helper.assertTrue(Math.abs(box.getXsize() - width) < EPS
                        && Math.abs(box.getYsize() - height) < EPS
                        && Math.abs(box.getZsize() - width) < EPS,
                why + " — bounding box not refreshed to " + width + "x" + height + "x" + width + ", got "
                        + box.getXsize() + "x" + box.getYsize() + "x" + box.getZsize() + " (ENT-S-095 batch 3)");
    }

    /**
     * The MHLib view of the main box: the resolved profile's main size (the
     * main-size law keeps it at [22, 24] in both modes) times the entity scale the
     * vendored resolution path returns must be the live box.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void assertMhlibMainBox(GameTestHelper helper, TheQueen queen, double expectedScale,
                                           float width, float height, String why) {
        Object asObject = queen;
        helper.assertTrue(asObject instanceof IMultipartEntity<?>,
                why + " — TheQueen is not an IMultipartEntity (MHLib LivingEntity mixin missing)");
        IMultipartEntity multipart = (IMultipartEntity) asObject;
        Optional<HitboxProfile> profile = multipart.getHitboxProfile();
        helper.assertTrue(profile != null && profile.isPresent(),
                why + " — no hitbox profile resolved for orespawn:the_queen");
        Vec2 main = profile.get().mainHitboxConfig().baseSize();
        helper.assertTrue(Math.abs(main.x - FULL_WIDTH) < EPS && Math.abs(main.y - FULL_HEIGHT) < EPS,
                why + " — profile main-hitbox.size drifted from [22, 24] (main-size law), got ["
                        + main.x + ", " + main.y + "]");
        double internal = multipart.mhlibGetEntitySizeInternally(queen);
        helper.assertTrue(Math.abs(internal - expectedScale) < EPS,
                why + " — IMultipartEntity.mhlibGetEntitySizeInternally must return " + expectedScale
                        + " (IMHLibSizeCallback), got " + internal);
        double direct = queen.mhlibGetEntitySizeScale(queen);
        helper.assertTrue(Math.abs(direct - expectedScale) < EPS,
                why + " — TheQueen.mhlibGetEntitySizeScale must return " + expectedScale + ", got " + direct);
        helper.assertTrue(Math.abs(main.x * internal - width) < EPS && Math.abs(main.y * internal - height) < EPS,
                why + " — MHLib main box (profile main size x entity scale) = " + (main.x * internal) + "x"
                        + (main.y * internal) + " does not report the live " + width + "x" + height);
    }

    private static Map<String, MHLibPartEntity<?>> partsByName(GameTestHelper helper, TheQueen queen, String label) {
        PartEntity<?>[] parts = queen.getParts();
        helper.assertTrue(parts != null && parts.length == PART_COUNT,
                label + " must carry the profile's " + PART_COUNT + " MHLib parts, got "
                        + (parts == null ? 0 : parts.length));
        Map<String, MHLibPartEntity<?>> byName = new LinkedHashMap<>();
        for (PartEntity<?> part : parts) {
            helper.assertTrue(part instanceof MHLibPartEntity<?>, label + " part is not an MHLibPartEntity: " + part);
            MHLibPartEntity<?> mhlibPart = (MHLibPartEntity<?>) part;
            byName.put(mhlibPart.getConfigName(), mhlibPart);
        }
        helper.assertTrue(byName.size() == PART_COUNT, label + " part names are not distinct: " + byName.keySet());
        return byName;
    }

    /** Flag off at construction: orig TheQueen.java:78-79 {@code func_70105_a(22.0f, 24.0f)}. */
    @GameTest(template = "empty_large", batch = "queenPlayNicelyDims")
    public void s095_queen_hitbox_full_22x24_when_play_nicely_off(GameTestHelper helper) {
        final boolean prior = OreSpawnConfig.PLAY_NICELY.get();
        TheQueen queen = null;
        try {
            OreSpawnConfig.PLAY_NICELY.set(false);
            queen = spawnFrozen(helper, POS_A);
            assertDims(helper, queen, FULL_WIDTH, FULL_HEIGHT,
                    "Queen constructed with playNicely=false must be 22x24 (orig TheQueen.java:78-79)");
            assertMhlibMainBox(helper, queen, FULL_SCALE, FULL_WIDTH, FULL_HEIGHT,
                    "hostile Queen MHLib main box");
        } finally {
            OreSpawnConfig.PLAY_NICELY.set(prior);
            if (queen != null) queen.discard();
        }
        helper.succeed();
    }

    /** Flag on at construction: orig TheQueen.java:80-82 {@code func_70105_a(5.5f, 6.0f)} — the box MHLib's Size hook used to overwrite. */
    @GameTest(template = "empty_large", batch = "queenPlayNicelyDims")
    public void s095_queen_hitbox_nice_5_5x6_when_play_nicely_on(GameTestHelper helper) {
        final boolean prior = OreSpawnConfig.PLAY_NICELY.get();
        TheQueen queen = null;
        try {
            OreSpawnConfig.PLAY_NICELY.set(true);
            queen = spawnFrozen(helper, POS_A);
            assertDims(helper, queen, NICE_WIDTH, NICE_HEIGHT,
                    "Queen constructed with playNicely=true must be 5.5x6 (orig TheQueen.java:80-82)");
            assertMhlibMainBox(helper, queen, NICE_SCALE, NICE_WIDTH, NICE_HEIGHT,
                    "nice Queen MHLib main box");
        } finally {
            OreSpawnConfig.PLAY_NICELY.set(prior);
            if (queen != null) queen.discard();
        }
        helper.succeed();
    }

    /**
     * Both modes side by side across ticks: the ten MHLib part boxes of a
     * PlayNicely Queen are exactly one quarter of the hostile twin's once
     * {@code alignSynchedSubParts} has applied the entity scale (fallback path,
     * BoneInformation.scale(entityScale) -> MHLibPartEntity.setScaling), and a
     * live flag flip in either direction keeps both constructor snapshots
     * (orig TheQueen.java:78-82 sizes only in the constructor; King BOSS-017 /
     * Kraken ENT-S-096 precedent). Own batch: waits on ticks.
     */
    @GameTest(template = "empty_large", timeoutTicks = 100, batch = "queenPlayNicelyParts")
    public void s095_queen_parts_quarter_size_and_live_flip_keeps_snapshot(GameTestHelper helper) {
        final boolean prior = OreSpawnConfig.PLAY_NICELY.get();
        TheQueen full = null;
        TheQueen nice = null;
        boolean armed = false;
        try {
            OreSpawnConfig.PLAY_NICELY.set(false);
            full = spawnFrozen(helper, POS_A);
            OreSpawnConfig.PLAY_NICELY.set(true);
            nice = spawnFrozen(helper, POS_B);
            assertDims(helper, full, FULL_WIDTH, FULL_HEIGHT,
                    "Queen constructed with playNicely=false must be 22x24 (orig TheQueen.java:78-79)");
            assertDims(helper, nice, NICE_WIDTH, NICE_HEIGHT,
                    "Queen constructed with playNicely=true must be 5.5x6 (orig TheQueen.java:80-82)");

            // Flag is true: the full Queen keeps 22x24 through an explicit refresh (the Size event re-fires).
            full.refreshDimensions();
            assertDims(helper, full, FULL_WIDTH, FULL_HEIGHT,
                    "flipping playNicely on must not shrink a live Queen (ctor snapshot, orig TheQueen.java:78-82)");
            // Flag off: the nice Queen keeps 5.5x6 through an explicit refresh.
            OreSpawnConfig.PLAY_NICELY.set(false);
            nice.refreshDimensions();
            assertDims(helper, nice, NICE_WIDTH, NICE_HEIGHT,
                    "flipping playNicely off must not grow a live nice Queen (ctor snapshot, orig TheQueen.java:78-82)");
            assertMhlibMainBox(helper, nice, NICE_SCALE, NICE_WIDTH, NICE_HEIGHT,
                    "nice Queen MHLib main box after a flip");
            // The entity scale reads the snapshot, so nothing below depends on the live flag: restore it now.
            OreSpawnConfig.PLAY_NICELY.set(prior);

            final TheQueen heldFull = full;
            final TheQueen heldNice = nice;
            helper.runAfterDelay(ALIGN_TICKS, () -> {
                try {
                    assertDims(helper, heldFull, FULL_WIDTH, FULL_HEIGHT,
                            "full Queen resized across " + ALIGN_TICKS + " ticks (ctor snapshot)");
                    assertDims(helper, heldNice, NICE_WIDTH, NICE_HEIGHT,
                            "nice Queen resized across " + ALIGN_TICKS + " ticks (ctor snapshot)");
                    Map<String, MHLibPartEntity<?>> fullParts = partsByName(helper, heldFull, "hostile Queen");
                    Map<String, MHLibPartEntity<?>> niceParts = partsByName(helper, heldNice, "nice Queen");
                    for (Map.Entry<String, MHLibPartEntity<?>> entry : fullParts.entrySet()) {
                        MHLibPartEntity<?> fullPart = entry.getValue();
                        MHLibPartEntity<?> nicePart = niceParts.get(entry.getKey());
                        helper.assertTrue(nicePart != null, "nice Queen has no part named " + entry.getKey());
                        EntityDimensions fullDims = fullPart.getDimensions(Pose.STANDING);
                        EntityDimensions niceDims = nicePart.getDimensions(Pose.STANDING);
                        helper.assertTrue(fullDims.width() > 0.0f && fullDims.height() > 0.0f,
                                "hostile part " + entry.getKey() + " has no size: " + fullDims);
                        helper.assertTrue(Math.abs(niceDims.width() - fullDims.width() * NICE_SCALE) < EPS
                                        && Math.abs(niceDims.height() - fullDims.height() * NICE_SCALE) < EPS,
                                "nice Queen part " + entry.getKey() + " must be 0.25x the hostile part ("
                                        + fullDims.width() + "x" + fullDims.height() + "), got "
                                        + niceDims.width() + "x" + niceDims.height()
                                        + " — the entity scale did not reach alignSynchedSubParts (ENT-S-095 batch 3)");
                        AABB fullBox = fullPart.getBoundingBox();
                        AABB niceBox = nicePart.getBoundingBox();
                        helper.assertTrue(Math.abs(niceBox.getXsize() - fullBox.getXsize() * NICE_SCALE) < EPS
                                        && Math.abs(niceBox.getYsize() - fullBox.getYsize() * NICE_SCALE) < EPS
                                        && Math.abs(niceBox.getZsize() - fullBox.getZsize() * NICE_SCALE) < EPS,
                                "nice Queen part " + entry.getKey() + " AABB is not 0.25x the hostile part's: "
                                        + niceBox + " vs " + fullBox + " (ENT-S-095 batch 3)");
                        helper.assertTrue(Math.abs(fullBox.getXsize() - fullDims.width()) < EPS
                                        && Math.abs(fullBox.getYsize() - fullDims.height()) < EPS,
                                "hostile Queen part " + entry.getKey() + " AABB " + fullBox
                                        + " does not match its dimensions " + fullDims);
                    }
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
}
