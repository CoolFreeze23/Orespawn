package danger.orespawn.gametest;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.OreSpawnConfig.SpiderMovement;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.AntRobot;
import danger.orespawn.entity.SpiderRobot;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.function.BooleanSupplier;

/**
 * Master-override ruling (2026-09-04), verbatim: "modern.enabled: master
 * override only. Off forces all modern features off; on defers to existing
 * per-feature keys, which keep their names. New features register under
 * [modern]."
 *
 * <p>Pins the effective-value helpers in {@link OreSpawnConfig} against that
 * truth table -- master off forces the classic/off value whatever the
 * per-feature key says; master on defers to the key in both directions --
 * for every modern feature key: {@code tweaks.spiderMovement}
 * ({@link OreSpawnConfig#spiderMovement()}), {@code tweaks.mountCamera}
 * ({@link OreSpawnConfig#mountCamera()}), {@code tweaks.phase14ContentEnable}
 * ({@link OreSpawnConfig#phase14ContentEnable()}) and
 * {@code modern.mothraWideRootHitbox}
 * ({@link OreSpawnConfig#mothraWideRootHitbox()}); plus the routed
 * construction read -- robots constructed with the master OFF and the key
 * at MODERN come out CLASSIC (no gait controller, false synced flag, zero
 * MHLib parts), and MODERN once the master is on.</p>
 *
 * <p>Batch (TEST-003): the flags are GLOBAL. Every test here is synchronous
 * within one tick -- set, assert (spawn, assert, discard), restore in a
 * {@code finally} (the MothraModernDimsTests / KrakenPlayNicelyTests idiom)
 * -- so they share one own batch, {@code modernMasterOverride}, and never
 * hold a flag across a tick. Template {@code empty_large} is 48x16x48.</p>
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class ModernMasterOverrideTests {

    /** empty_large is 48x16x48; the four robots sit a quadrant apart. */
    private static final BlockPos SPIDER_OFF = new BlockPos(12, 2, 12);
    private static final BlockPos ANT_OFF = new BlockPos(36, 2, 12);
    private static final BlockPos SPIDER_ON = new BlockPos(12, 2, 36);
    private static final BlockPos ANT_ON = new BlockPos(36, 2, 36);

    /** Every global flag this class flips, read once per test and restored in every {@code finally}. */
    private record Flags(boolean modernEnabled, SpiderMovement spiderMovement, boolean mountCamera,
                         boolean phase14Content, boolean mothraWideRoot) {
        static Flags read() {
            return new Flags(OreSpawnConfig.MODERN_ENABLED.get(),
                    OreSpawnConfig.SPIDER_MOVEMENT.get(),
                    OreSpawnConfig.MOUNT_CAMERA.get(),
                    OreSpawnConfig.PHASE14_CONTENT_ENABLE.get(),
                    OreSpawnConfig.MODERN_MOTHRA_WIDE_ROOT_HITBOX.get());
        }

        void restore() {
            OreSpawnConfig.SPIDER_MOVEMENT.set(this.spiderMovement);
            OreSpawnConfig.MOUNT_CAMERA.set(this.mountCamera);
            OreSpawnConfig.PHASE14_CONTENT_ENABLE.set(this.phase14Content);
            OreSpawnConfig.MODERN_MOTHRA_WIDE_ROOT_HITBOX.set(this.mothraWideRoot);
            OreSpawnConfig.MODERN_ENABLED.set(this.modernEnabled);
        }
    }

    private static void discard(Entity... entities) {
        for (Entity entity : entities) {
            if (entity != null) entity.discard();
        }
    }

    /** One row of the spiderMovement truth table: set master + key, read the effective mode, restore. */
    private static void pinSpiderMovement(GameTestHelper helper, boolean master, SpiderMovement key,
                                          SpiderMovement expected) {
        final Flags prior = Flags.read();
        try {
            OreSpawnConfig.MODERN_ENABLED.set(master);
            OreSpawnConfig.SPIDER_MOVEMENT.set(key);
            SpiderMovement effective = OreSpawnConfig.spiderMovement();
            helper.assertTrue(effective == expected,
                    "modern.enabled=" + master + ", tweaks.spiderMovement=" + key
                            + " must read " + expected + ", got " + effective + " (master override)");
        } finally {
            prior.restore();
        }
        helper.succeed();
    }

    /** One row of a boolean feature's truth table: set master + key, read the effective value, restore. */
    private static void pinBooleanFeature(GameTestHelper helper, String keyName, ModConfigSpec.BooleanValue key,
                                          BooleanSupplier effective, boolean master, boolean keyValue,
                                          boolean expected) {
        final Flags prior = Flags.read();
        try {
            OreSpawnConfig.MODERN_ENABLED.set(master);
            key.set(keyValue);
            boolean got = effective.getAsBoolean();
            helper.assertTrue(got == expected,
                    "modern.enabled=" + master + ", " + keyName + "=" + keyValue
                            + " must read " + expected + ", got " + got + " (master override)");
        } finally {
            prior.restore();
        }
        helper.succeed();
    }

    // ---- tweaks.spiderMovement -> OreSpawnConfig.spiderMovement() ----

    /** Master off forces CLASSIC even with the key at MODERN (the key's own default). */
    @GameTest(template = "empty", batch = "modernMasterOverride")
    public void master_off_spider_movement_modern_key_reads_classic(GameTestHelper helper) {
        pinSpiderMovement(helper, false, SpiderMovement.MODERN, SpiderMovement.CLASSIC);
    }

    /** Master on defers to the key: MODERN reads MODERN. */
    @GameTest(template = "empty", batch = "modernMasterOverride")
    public void master_on_spider_movement_modern_key_reads_modern(GameTestHelper helper) {
        pinSpiderMovement(helper, true, SpiderMovement.MODERN, SpiderMovement.MODERN);
    }

    /** Master on defers to the key: CLASSIC reads CLASSIC (the master never forces modern). */
    @GameTest(template = "empty", batch = "modernMasterOverride")
    public void master_on_spider_movement_classic_key_reads_classic(GameTestHelper helper) {
        pinSpiderMovement(helper, true, SpiderMovement.CLASSIC, SpiderMovement.CLASSIC);
    }

    // ---- tweaks.mountCamera -> OreSpawnConfig.mountCamera() ----

    /** Master off forces the camera off even with the key on (the key's own default). */
    @GameTest(template = "empty", batch = "modernMasterOverride")
    public void master_off_mount_camera_key_on_reads_off(GameTestHelper helper) {
        pinBooleanFeature(helper, "tweaks.mountCamera", OreSpawnConfig.MOUNT_CAMERA,
                OreSpawnConfig::mountCamera, false, true, false);
    }

    /** Master on defers to the key: on reads on. */
    @GameTest(template = "empty", batch = "modernMasterOverride")
    public void master_on_mount_camera_key_on_reads_on(GameTestHelper helper) {
        pinBooleanFeature(helper, "tweaks.mountCamera", OreSpawnConfig.MOUNT_CAMERA,
                OreSpawnConfig::mountCamera, true, true, true);
    }

    /** Master on defers to the key: off reads off. */
    @GameTest(template = "empty", batch = "modernMasterOverride")
    public void master_on_mount_camera_key_off_reads_off(GameTestHelper helper) {
        pinBooleanFeature(helper, "tweaks.mountCamera", OreSpawnConfig.MOUNT_CAMERA,
                OreSpawnConfig::mountCamera, true, false, false);
    }

    // ---- tweaks.phase14ContentEnable -> OreSpawnConfig.phase14ContentEnable() ----

    /** Master off forces the wiki-only content off even with the key on. */
    @GameTest(template = "empty", batch = "modernMasterOverride")
    public void master_off_phase14_content_key_on_reads_off(GameTestHelper helper) {
        pinBooleanFeature(helper, "tweaks.phase14ContentEnable", OreSpawnConfig.PHASE14_CONTENT_ENABLE,
                OreSpawnConfig::phase14ContentEnable, false, true, false);
    }

    /** Master on defers to the key: on reads on. */
    @GameTest(template = "empty", batch = "modernMasterOverride")
    public void master_on_phase14_content_key_on_reads_on(GameTestHelper helper) {
        pinBooleanFeature(helper, "tweaks.phase14ContentEnable", OreSpawnConfig.PHASE14_CONTENT_ENABLE,
                OreSpawnConfig::phase14ContentEnable, true, true, true);
    }

    /** Master on defers to the key: off (the key's own default) reads off. */
    @GameTest(template = "empty", batch = "modernMasterOverride")
    public void master_on_phase14_content_key_off_reads_off(GameTestHelper helper) {
        pinBooleanFeature(helper, "tweaks.phase14ContentEnable", OreSpawnConfig.PHASE14_CONTENT_ENABLE,
                OreSpawnConfig::phase14ContentEnable, true, false, false);
    }

    // ---- modern.mothraWideRootHitbox -> OreSpawnConfig.mothraWideRootHitbox() ----

    /** MOD-029's sub-key is off while the master is off, even with the key on (its default). */
    @GameTest(template = "empty", batch = "modernMasterOverride")
    public void master_off_mothra_wide_root_key_on_reads_off(GameTestHelper helper) {
        pinBooleanFeature(helper, "modern.mothraWideRootHitbox", OreSpawnConfig.MODERN_MOTHRA_WIDE_ROOT_HITBOX,
                OreSpawnConfig::mothraWideRootHitbox, false, true, false);
    }

    // ---- the routed construction read (SpiderRobot / AntRobot ctor-tail snapshot) ----

    /**
     * The construction snapshot goes through {@code spiderMovement()}: with
     * the master OFF and the key at MODERN both robots construct CLASSIC --
     * no gait controller, a false synced flag, ZERO MHLib parts (the D3
     * zero-parts law); with the master ON (key unchanged) they construct
     * MODERN -- gait controller, synced flag, 8 spider / 6 ant leg parts.
     * Fresh entities per state (BOSS-017 snapshot), discarded before the
     * flags are restored.
     */
    @GameTest(template = "empty_large", batch = "modernMasterOverride")
    public void master_off_constructs_classic_robots_despite_modern_key(GameTestHelper helper) {
        final Flags prior = Flags.read();
        SpiderRobot spiderOff = null;
        AntRobot antOff = null;
        SpiderRobot spiderOn = null;
        AntRobot antOn = null;
        try {
            OreSpawnConfig.MODERN_ENABLED.set(false);
            OreSpawnConfig.SPIDER_MOVEMENT.set(SpiderMovement.MODERN);
            spiderOff = helper.spawnWithNoFreeWill(ModEntities.SPIDER_ROBOT.get(), SPIDER_OFF);
            antOff = helper.spawnWithNoFreeWill(ModEntities.ANT_ROBOT.get(), ANT_OFF);
            helper.assertTrue(spiderOff.getModernGait() == null && !spiderOff.isModernMovement(),
                    "master off + spiderMovement=MODERN must construct a CLASSIC spider (no gait controller)");
            helper.assertTrue(spiderOff.getParts() == null || spiderOff.getParts().length == 0,
                    "master off + spiderMovement=MODERN must construct ZERO spider parts, got "
                            + (spiderOff.getParts() == null ? 0 : spiderOff.getParts().length));
            helper.assertTrue(antOff.getModernGait() == null && !antOff.isModernMovement(),
                    "master off + spiderMovement=MODERN must construct a CLASSIC ant (no gait controller)");
            helper.assertTrue(antOff.getParts() == null || antOff.getParts().length == 0,
                    "master off + spiderMovement=MODERN must construct ZERO ant parts, got "
                            + (antOff.getParts() == null ? 0 : antOff.getParts().length));

            OreSpawnConfig.MODERN_ENABLED.set(true);
            spiderOn = helper.spawnWithNoFreeWill(ModEntities.SPIDER_ROBOT.get(), SPIDER_ON);
            antOn = helper.spawnWithNoFreeWill(ModEntities.ANT_ROBOT.get(), ANT_ON);
            helper.assertTrue(spiderOn.getModernGait() != null && spiderOn.isModernMovement(),
                    "master on + spiderMovement=MODERN must construct a MODERN spider (gait controller + synced flag)");
            helper.assertTrue(spiderOn.getParts() != null && spiderOn.getParts().length == 8,
                    "master on + spiderMovement=MODERN must construct 8 spider leg parts, got "
                            + (spiderOn.getParts() == null ? 0 : spiderOn.getParts().length));
            helper.assertTrue(antOn.getModernGait() != null && antOn.isModernMovement(),
                    "master on + spiderMovement=MODERN must construct a MODERN ant (gait controller + synced flag)");
            helper.assertTrue(antOn.getParts() != null && antOn.getParts().length == 6,
                    "master on + spiderMovement=MODERN must construct 6 ant leg parts, got "
                            + (antOn.getParts() == null ? 0 : antOn.getParts().length));

            // The classic pair keeps its snapshot through the master flip (BOSS-017).
            helper.assertTrue(spiderOff.getModernGait() == null && !spiderOff.isModernMovement(),
                    "turning the master on must not retrofit a live classic spider (construction snapshot)");
            helper.assertTrue(antOff.getModernGait() == null && !antOff.isModernMovement(),
                    "turning the master on must not retrofit a live classic ant (construction snapshot)");
        } finally {
            prior.restore();
            discard(spiderOff, antOff, spiderOn, antOn);
        }
        helper.succeed();
    }
}
