package danger.orespawn.gametest;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.OreSpawnMod;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * ENT-S-118 (targeting ledger T2, wave 2): the 1.7.10 line-of-sight steps the port's target filters had dropped,
 * restored at the orig positions. Two shapes:
 * <ul>
 *   <li>the eye-to-eye {@code canSee} step — orig {@code getEntitySenses().canSee(e)} inside {@code isSuitableTarget}
 *       (AntRobot's hunt and stomp filters, Fairy, GiantRobot, Lizard, PitchBlack, PurplePower, Robot1-5, SpiderDriver)
 *       or inside the players-only pick (Irukandji, Skate), transcribed as the port's
 *       {@code getSensing().hasLineOfSight(target)} (the ENT-S-108 idiom; vanilla's eye-to-eye COLLIDER clip);</li>
 *   <li>the feet ray — orig {@code canSeeTarget(e.posX, e.posY, e.posZ)} in the scan loop of Spyro, Stinky, ThePrince and
 *       ThePrincess: a second, block-only ray from 0.75 above the hunter's feet to the candidate's own position, evaluated
 *       after the whole filter passes, transcribed as a private {@code canSeeTarget} ({@code ClipContext.Block.OUTLINE},
 *       {@code Fluid.NONE} — 1.7.10's {@code rayTraceBlocks(start, end, false)} tested every collidable block on its
 *       selection bounds and never stopped on liquid, the mapping ENT-S-089 recorded for the Vortex's copy of the helper)
 *       and'ed onto the scan's predicate.</li>
 * </ul>
 *
 * <p>One generated test per port site — a {@link GameTestGenerator} over {@link #sites()} in orig file order, each a
 * {@link Probe} on the CreativeMappingParityTests / PitchBlackAllyTests floor geometry: the hunter frozen at rel
 * (20, 1, 24) on the floor of the 48x16x48 empty_large, the prey 8 blocks east on the same floor (a pig where the
 * hunter takes any living thing, a Zombie where it takes Monsters only, a Chicken for the Lizard, a survival mock
 * player 5 blocks off for the two players-only picks). Eye-to-eye sites: a one-block-thick stone wall raised
 * midway (x = 24, three wide, 14 tall — above the Giant Robot's eye at rel ~9.3) and shown to break
 * {@code hasLineOfSight}; the private filter (or the inline pick, through {@code customServerAiStep} with its rolls
 * pinned) must refuse the prey behind it, and accept the same prey once the wall is razed — so the row fails with the
 * port line reverted. Feet-ray sites: a one-block parapet on the floor row in front of the Zombie's feet at
 * (27, 1, 24), which the eye line clears (both {@code hasLineOfSight} and the full {@code isSuitableTarget} are
 * asserted to pass over it — the control that the eye-to-eye line alone would accept) while the feet ray from
 * (20.5, 1.75, 24.5) to (28.5, 1.0, 24.5) crosses it at y 1.14..1.05: {@code findSomethingToAttack} must answer null
 * with the parapet and the Zombie without it. A second row per feet-ray hunter pins the ray's mapping with a short
 * grass parapet — no collision shape, a selection shape — which 1.7.10's selection-bounds ray stopped on.</p>
 *
 * <p>Synchronous; the hunter's per-tick sight cache ({@code Sensing}, never ticked on a {@code setNoAi} mob) is cleared
 * between the occluded and open drives. {@code PLAY_NICELY} is set false for the drive (the scans and picks answer null
 * under it, ENT-S-115) and restored in a finally, so the batch is this class alone (TEST-003). Spawns are discarded,
 * mock players removed and occluders razed in the finally.</p>
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class SightStepParityTests {

    private static final String BATCH = "sightStepParity";
    private static final String TEST_PREFIX = "sightstepparitytests.";
    /** Generated TestFunctions bypass the holder's template prefixing, so the template is named in full (IgnoreScreenParityTests). */
    private static final String EMPTY_LARGE = OreSpawnMod.MOD_ID + ":empty_large";
    private static final int TIMEOUT_TICKS = 100;
    private static final String FINDING = "ENT-S-118";

    /** The hunter on the template floor. */
    private static final BlockPos HUNTER_POS = new BlockPos(20, 1, 24);
    /** 8 blocks east: inside every scan box, inside the Ant Robot's 6..9 stomp ring (orig AntRobot.java:977-986), past the Spider Driver's 6-block refusal (orig :156). */
    private static final BlockPos PREY_POS = new BlockPos(28, 1, 24);
    /** 5 blocks east (block-centred): inside the Irukandji's 6-block and the Skate's 10-block player sphere, beyond the wall. */
    private static final Vec3 PLAYER_POS = new Vec3(25.5, 1.0, 24.5);
    /** The wall: one block thick at x = 24 (midway), z 23..25, y 1..14 — under the barrier ceiling at rel 17, above every hunter's eye. */
    private static final int WALL_X = 24;
    private static final int WALL_Z_MIN = 23;
    private static final int WALL_Z_MAX = 25;
    private static final int WALL_Y_MIN = 1;
    private static final int WALL_Y_MAX = 14;
    /** The parapet: the floor-row block in front of the prey's feet. The feet ray (20.5, 1.75, 24.5) → (28.5, 1.0, 24.5) crosses x 27..28 at y 1.14..1.05. */
    private static final BlockPos PARAPET_POS = new BlockPos(27, 1, 24);
    /** Prey health, high enough that nothing incidental kills it. */
    private static final float PREY_HEALTH = 1000.0f;
    /** The Irukandji / Skate step: the water hunt (1-in-10) quiet, the 1-in-8 pick firing, the bite (1-in-4) quiet. */
    private static final int[] PICK_ROLLS = {10, 1, 8, 1, 4, 1};

    private static final Supplier<EntityType<? extends Mob>> PIG = () -> EntityType.PIG;
    private static final Supplier<EntityType<? extends Mob>> ZOMBIE = () -> EntityType.ZOMBIE;
    private static final Supplier<EntityType<? extends Mob>> CHICKEN = () -> EntityType.CHICKEN;

    // ------------------------------------------------------------------
    // The site table, in orig file order
    // ------------------------------------------------------------------

    /**
     * One port site. {@link #setUp} spawns the hunter and its prey on the open floor (line of sight asserted);
     * {@link #drive} raises or razes the site's occluder, clears the sight cache, asserts the occluder's geometry
     * and drives the site once, answering whether the prey was accepted; {@link #trace} names what the last drive
     * saw; {@link #cleanUp} razes the occluder and discards the spawns, tolerant of a set-up that never finished.
     */
    private interface Probe {
        void setUp(GameTestHelper helper);

        boolean drive(GameTestHelper helper, boolean occluded);

        String trace();

        void cleanUp(GameTestHelper helper);
    }

    /** One orig sight step and the port site that carries it. */
    private record Site(int index, String tag, String orig, String port, String effect, Supplier<Probe> probe) {
        String testName() {
            return TEST_PREFIX + String.format("s118_%02d_%s", this.index, this.tag);
        }

        String where() {
            return this.port + " (orig " + this.orig + ")";
        }
    }

    private static List<Site> sites() {
        List<Site> sites = new ArrayList<>();
        // AntRobot — orig :974-976 (the stomp filter) and :1047-1049 (the hunt filter)
        sites.add(new Site(1, "antrobot_974_stomp_sight", "AntRobot.java:974-976", "AntRobot.feetIsSuitableTarget",
                "the stomp filter accepting a pig 8 blocks off (inside the 6..9 ring)",
                () -> new FilterProbe(ModEntities.ANT_ROBOT, PIG, "feetIsSuitableTarget", FilterProbe.Ring.STOMP)));
        sites.add(new Site(2, "antrobot_1047_hunt_sight", "AntRobot.java:1047-1049", "AntRobot.isSuitableTarget",
                "the hunt filter accepting a pig 8 blocks off",
                () -> new FilterProbe(ModEntities.ANT_ROBOT, PIG, "isSuitableTarget", FilterProbe.Ring.NONE)));
        // Fairy — orig :232-234
        sites.add(new Site(3, "fairy_232_sight", "Fairy.java:232-234", "Fairy.isSuitableTarget",
                "the filter accepting a Zombie 8 blocks off",
                () -> new FilterProbe(ModEntities.FAIRY, ZOMBIE, "isSuitableTarget", FilterProbe.Ring.NONE)));
        // GiantRobot — orig :327-329
        sites.add(new Site(4, "giantrobot_327_sight", "GiantRobot.java:327-329", "GiantRobot.isSuitableTarget",
                "the filter accepting a pig 8 blocks off",
                () -> new FilterProbe(ModEntities.GIANT_ROBOT, PIG, "isSuitableTarget", FilterProbe.Ring.NONE)));
        // Irukandji — orig :280-282 (inside the players-only filter; the port's pick is inline)
        sites.add(new Site(5, "irukandji_280_sight", "Irukandji.java:280-282", "Irukandji.customServerAiStep (the inline pick)",
                "the pick storing a survival player 5 blocks off as the target",
                () -> new InlinePickProbe(ModEntities.IRUKANDJI)));
        // Lizard — orig :313-315
        sites.add(new Site(6, "lizard_313_sight", "Lizard.java:313-315", "Lizard.isSuitableTarget",
                "the filter accepting a Chicken 8 blocks off",
                () -> new FilterProbe(ModEntities.LIZARD, CHICKEN, "isSuitableTarget", FilterProbe.Ring.NONE)));
        // PitchBlack — orig :501-503
        sites.add(new Site(7, "pitchblack_501_sight", "PitchBlack.java:501-503", "PitchBlack.isSuitableTarget",
                "the filter accepting a pig 8 blocks off",
                () -> new FilterProbe(ModEntities.PITCH_BLACK, PIG, "isSuitableTarget", FilterProbe.Ring.NONE)));
        // PurplePower — orig :251-253
        sites.add(new Site(8, "purplepower_251_sight", "PurplePower.java:251-253", "PurplePower.isSuitableTarget",
                "the filter accepting a pig 8 blocks off",
                () -> new FilterProbe(ModEntities.PURPLE_POWER, PIG, "isSuitableTarget", FilterProbe.Ring.NONE)));
        // Robot1..5 — orig :189-191 / :366-368 / :306-308 / :370-372 / :280-282
        sites.add(new Site(9, "robot1_189_sight", "Robot1.java:189-191", "Robot1.isSuitableTarget",
                "the filter accepting a pig 8 blocks off",
                () -> new FilterProbe(ModEntities.ROBOT_1, PIG, "isSuitableTarget", FilterProbe.Ring.NONE)));
        sites.add(new Site(10, "robot2_366_sight", "Robot2.java:366-368", "Robot2.isSuitableTarget",
                "the filter accepting a pig 8 blocks off",
                () -> new FilterProbe(ModEntities.ROBOT_2, PIG, "isSuitableTarget", FilterProbe.Ring.NONE)));
        sites.add(new Site(11, "robot3_306_sight", "Robot3.java:306-308", "Robot3.isSuitableTarget",
                "the filter accepting a pig 8 blocks off",
                () -> new FilterProbe(ModEntities.ROBOT_3, PIG, "isSuitableTarget", FilterProbe.Ring.NONE)));
        sites.add(new Site(12, "robot4_370_sight", "Robot4.java:370-372", "Robot4.isSuitableTarget",
                "the filter accepting a pig 8 blocks off",
                () -> new FilterProbe(ModEntities.ROBOT_4, PIG, "isSuitableTarget", FilterProbe.Ring.NONE)));
        sites.add(new Site(13, "robot5_280_sight", "Robot5.java:280-282", "Robot5.isSuitableTarget",
                "the filter accepting a pig 8 blocks off",
                () -> new FilterProbe(ModEntities.ROBOT_5, PIG, "isSuitableTarget", FilterProbe.Ring.NONE)));
        // Skate — orig :272-274 (inside the players-only filter; the port's pick is inline)
        sites.add(new Site(14, "skate_272_sight", "Skate.java:272-274", "Skate.customServerAiStep (the inline pick)",
                "the pick storing a survival player 5 blocks off as the target",
                () -> new InlinePickProbe(ModEntities.SKATE)));
        // SpiderDriver — orig :149-151
        sites.add(new Site(15, "spiderdriver_149_sight", "SpiderDriver.java:149-151", "SpiderDriver.isSuitableTarget",
                "the filter accepting a pig 8 blocks off (past the 6-block refusal of orig :156)",
                () -> new FilterProbe(ModEntities.SPIDER_DRIVER, PIG, "isSuitableTarget", FilterProbe.Ring.SPIDER_DRIVER)));
        // Spyro — orig :709 (the loop's feet ray) with :436-438 (the helper)
        sites.add(new Site(16, "spyro_709_feet_ray", "Spyro.java:709 with :436-438", "EntitySpyro.findSomethingToAttack (the feet ray)",
                "the scan returning a Zombie 8 blocks off whose feet the ray reaches",
                () -> new FeetRayProbe(ModEntities.ENTITY_SPYRO, Blocks.STONE, false)));
        sites.add(new Site(17, "spyro_436_ray_selection_box", "Spyro.java:436-438 (rayTraceBlocks(start, end, false): selection bounds, no liquid stop)",
                "EntitySpyro.canSeeTarget (through findSomethingToAttack)",
                "the scan returning a Zombie 8 blocks off once the collision-less grass in front of its feet is gone",
                () -> new FeetRayProbe(ModEntities.ENTITY_SPYRO, Blocks.SHORT_GRASS, true)));
        // Stinky — orig :699 with :317-319
        sites.add(new Site(18, "stinky_699_feet_ray", "Stinky.java:699 with :317-319", "EntityStinky.findSomethingToAttack (the feet ray)",
                "the scan returning a Zombie 8 blocks off whose feet the ray reaches",
                () -> new FeetRayProbe(ModEntities.ENTITY_STINKY, Blocks.STONE, false)));
        sites.add(new Site(19, "stinky_317_ray_selection_box", "Stinky.java:317-319 (rayTraceBlocks(start, end, false): selection bounds, no liquid stop)",
                "EntityStinky.canSeeTarget (through findSomethingToAttack)",
                "the scan returning a Zombie 8 blocks off once the collision-less grass in front of its feet is gone",
                () -> new FeetRayProbe(ModEntities.ENTITY_STINKY, Blocks.SHORT_GRASS, true)));
        // ThePrince — orig :776 with :416-418
        sites.add(new Site(20, "theprince_776_feet_ray", "ThePrince.java:776 with :416-418", "ThePrince.findSomethingToAttack (the feet ray)",
                "the scan returning a Zombie 8 blocks off whose feet the ray reaches",
                () -> new FeetRayProbe(ModEntities.THE_PRINCE, Blocks.STONE, false)));
        sites.add(new Site(21, "theprince_416_ray_selection_box", "ThePrince.java:416-418 (rayTraceBlocks(start, end, false): selection bounds, no liquid stop)",
                "ThePrince.canSeeTarget (through findSomethingToAttack)",
                "the scan returning a Zombie 8 blocks off once the collision-less grass in front of its feet is gone",
                () -> new FeetRayProbe(ModEntities.THE_PRINCE, Blocks.SHORT_GRASS, true)));
        // ThePrincess — orig :857 with :404-406
        sites.add(new Site(22, "theprincess_857_feet_ray", "ThePrincess.java:857 with :404-406", "ThePrincess.findSomethingToAttack (the feet ray)",
                "the scan returning a Zombie 8 blocks off whose feet the ray reaches",
                () -> new FeetRayProbe(ModEntities.THE_PRINCESS, Blocks.STONE, false)));
        sites.add(new Site(23, "theprincess_404_ray_selection_box", "ThePrincess.java:404-406 (rayTraceBlocks(start, end, false): selection bounds, no liquid stop)",
                "ThePrincess.canSeeTarget (through findSomethingToAttack)",
                "the scan returning a Zombie 8 blocks off once the collision-less grass in front of its feet is gone",
                () -> new FeetRayProbe(ModEntities.THE_PRINCESS, Blocks.SHORT_GRASS, true)));
        return sites;
    }

    /** One test per port site: 23 TestFunctions in the {@code sightStepParity} batch. */
    @GameTestGenerator
    public Collection<TestFunction> sightStepSites() {
        List<TestFunction> functions = new ArrayList<>();
        for (Site site : sites()) {
            functions.add(new TestFunction(BATCH, site.testName(), EMPTY_LARGE, Rotation.NONE, TIMEOUT_TICKS, 0L, true,
                    helper -> run(helper, site)));
        }
        return functions;
    }

    // ------------------------------------------------------------------
    // Runner: refused behind the occluder, accepted with it razed
    // ------------------------------------------------------------------

    private static void run(GameTestHelper helper, Site site) {
        helper.assertTrue(helper.getLevel().getDifficulty() != Difficulty.PEACEFUL,
                "precondition: the game-test level runs at NORMAL, not Peaceful — several of these filters refuse everything"
                        + " on Peaceful (" + FINDING + " test setup)");
        final boolean prior = OreSpawnConfig.PLAY_NICELY.get();
        Probe probe = site.probe().get();
        try {
            OreSpawnConfig.PLAY_NICELY.set(false);
            helper.assertTrue(!OreSpawnConfig.PLAY_NICELY.get(),
                    "precondition: PLAY_NICELY.set(false) must read back false — the scans and picks answer null under it"
                            + " (ENT-S-115), which is not the step under test (" + FINDING + " test setup)");
            probe.setUp(helper);
            helper.assertTrue(!probe.drive(helper, true), site.where() + " with the occluder in place: orig " + site.orig()
                    + " refuses a candidate the block ray cannot reach, so the same hunter and prey that show " + site.effect()
                    + " on the open floor must be refused — saw " + probe.trace() + " (" + FINDING + ")");
            helper.assertTrue(probe.drive(helper, false), "control: with the occluder razed " + site.where() + " must show "
                    + site.effect() + " — the ray, not the rest of the chain, is what refused it — saw " + probe.trace()
                    + " (" + FINDING + ")");
        } finally {
            OreSpawnConfig.PLAY_NICELY.set(prior);
            probe.cleanUp(helper);
        }
        helper.succeed();
    }

    /** A hunter and one prey (a mob or a mock player), both frozen; the base of every probe. */
    private abstract static class HunterProbe implements Probe {
        Mob hunter;
        Mob prey;
        ServerPlayer player;
        String trace = "(not driven)";

        LivingEntity preyEntity() {
            return this.player != null ? this.player : this.prey;
        }

        @Override
        public String trace() {
            return this.trace;
        }

        @Override
        public void cleanUp(GameTestHelper helper) {
            discardQuietly(this.prey);
            removePlayer(helper, this.player);
            discardQuietly(this.hunter);
        }
    }

    // ------------------------------------------------------------------
    // The eye-to-eye shape: the one-block wall midway breaks the eye line
    // ------------------------------------------------------------------

    /** The wall raised or razed, the sight cache cleared, and the geometry asserted: no eye line through the wall, the eye line back without it. */
    private abstract static class WallProbe extends HunterProbe {
        void arrange(GameTestHelper helper, boolean walled) {
            setWall(helper, walled);
            this.hunter.getSensing().tick();
            clearSightMemo(this.hunter); // ENT-S-122: rows 1, 2 and 7 — the Ant Robot's and the Nightmare's own sight memo, cleared between the drives as the vanilla cache is
            boolean sees = this.hunter.hasLineOfSight(preyEntity());
            helper.assertTrue(sees == !walled, "precondition: the " + this.hunter.getClass().getSimpleName() + " (eye "
                    + String.format("%.2f", this.hunter.getEyeHeight()) + " above its feet) must " + (walled ? "not see the "
                    : "see the ") + preyEntity().getClass().getSimpleName() + " 8 blocks east " + (walled ? "through the one-block"
                    + " stone wall at x = 24 (z 23..25, y 1..14)" : "once the wall is razed") + " (" + FINDING + " test geometry)");
        }

        @Override
        public void cleanUp(GameTestHelper helper) {
            if (this.hunter != null) {
                setWall(helper, false);
            }
            super.cleanUp(helper);
        }
    }

    /** A private filter invoked by reflection on the frozen prey; the hunter frozen. */
    private static final class FilterProbe extends WallProbe {
        enum Ring { NONE, STOMP, SPIDER_DRIVER }

        private final Supplier<? extends EntityType<? extends Mob>> hunterType;
        private final Supplier<? extends EntityType<? extends Mob>> preyType;
        private final String method;
        private final Ring ring;

        FilterProbe(Supplier<? extends EntityType<? extends Mob>> hunterType, Supplier<? extends EntityType<? extends Mob>> preyType,
                    String method, Ring ring) {
            this.hunterType = hunterType;
            this.preyType = preyType;
            this.method = method;
            this.ring = ring;
        }

        @Override
        public void setUp(GameTestHelper helper) {
            this.hunter = spawnFrozen(helper, this.hunterType.get(), HUNTER_POS);
            this.prey = spawnPrey(helper, this.preyType.get(), PREY_POS);
            if (this.ring == Ring.STOMP) {
                double dist = this.hunter.distanceTo(this.prey);
                helper.assertTrue(dist >= 6.0 && dist <= 9.0, "precondition: the pig must stand inside the stomp ring 6..9"
                        + " (orig AntRobot.java:977-986), so the ring is not what refuses it — at " + dist + " (" + FINDING + " test geometry)");
            } else if (this.ring == Ring.SPIDER_DRIVER) {
                double distSq = this.hunter.distanceToSqr(this.prey);
                helper.assertTrue(distSq >= 36.0, "precondition: the pig must stand 6 or more blocks off (orig SpiderDriver.java:156"
                        + " refuses distSq < 36), so the range is not what refuses it — at distSq " + distSq + " (" + FINDING + " test geometry)");
            }
            assertSees(helper, this.hunter, this.prey);
        }

        @Override
        public boolean drive(GameTestHelper helper, boolean occluded) {
            arrange(helper, occluded);
            boolean accepted = invokeFilter(this.hunter, this.method, this.prey);
            this.trace = this.method + "(" + describe(this.prey) + ") -> " + accepted + (occluded ? " behind the wall" : " with the wall razed");
            return accepted;
        }
    }

    /**
     * orig Irukandji.java:280-282 / Skate.java:272-274 (port customServerAiStep, the inline players-only pick): the
     * water hunt pinned quiet, the 1-in-8 pick pinned to fire; a survival mock player 5 blocks off (inside the
     * 6 / 10-block sphere, beyond the wall) is found, sighted and stored as the target on the open floor and not stored
     * behind the wall. The slot is emptied before each drive; 5 blocks is beyond melee reach, so the bite roll is moot.
     */
    private static final class InlinePickProbe extends WallProbe {
        private final Supplier<? extends EntityType<? extends Mob>> hunterType;

        InlinePickProbe(Supplier<? extends EntityType<? extends Mob>> hunterType) {
            this.hunterType = hunterType;
        }

        @Override
        public void setUp(GameTestHelper helper) {
            this.hunter = spawnFrozen(helper, this.hunterType.get(), HUNTER_POS);
            this.player = playerAt(helper, GameType.SURVIVAL, helper.absoluteVec(PLAYER_POS));
            helper.assertTrue(!this.player.getAbilities().invulnerable && !this.player.getAbilities().instabuild,
                    "precondition: the SURVIVAL mock player is neither invulnerable nor instabuild, so the orig creative"
                            + " rule (Irukandji.java:283-286 / Skate.java:275-278) admits it (" + FINDING + " test setup)");
            replaceRandom(this.hunter, rolls(PICK_ROLLS));
            helper.assertTrue(this.hunter.getTarget() == null,
                    "precondition: no stored target, so the pick is the only source (" + FINDING + " test setup)");
            assertSees(helper, this.hunter, this.player);
        }

        @Override
        public boolean drive(GameTestHelper helper, boolean occluded) {
            arrange(helper, occluded);
            this.hunter.setTarget(null);
            invoke(this.hunter, this.hunter.getClass(), "customServerAiStep");
            LivingEntity after = this.hunter.getTarget();
            this.trace = "target after the step " + describe(after) + (occluded ? " behind the wall" : " with the wall razed");
            return after == this.player;
        }
    }

    // ------------------------------------------------------------------
    // The feet-ray shape: the eye line clears a parapet in front of the prey's feet, the feet ray does not
    // ------------------------------------------------------------------

    /**
     * orig Spyro.java:709 / Stinky.java:699 / ThePrince.java:776 / ThePrincess.java:857 with their {@code canSeeTarget}
     * (:436-438 / :317-319 / :416-418 / :404-406): a Zombie 8 blocks east on the floor, a one-block parapet on the floor
     * row in front of its feet at (27, 1, 24). The eye line from the hunter's eye (rel 1.43..2.06) to the Zombie's
     * (rel 2.74) crosses the parapet column at y 2.49 or higher — the parapet's top is 2.0 — so {@code hasLineOfSight} and the full
     * {@code isSuitableTarget} — the eye-to-eye step among them — keep accepting the Zombie (asserted in both phases);
     * the feet ray from (20.5, 1.75, 24.5) to the Zombie's position (28.5, 1.0, 24.5) crosses it at y 1.14..1.05, so
     * {@code findSomethingToAttack} must answer null with the parapet and the Zombie without it. With the short-grass
     * parapet ({@code selectionOnly}) the block's collision shape is asserted empty and its selection shape not: the
     * 1.7.10 ray ({@code rayTraceBlocks(start, end, false)} → {@code func_147447_a(…, false, false, false)}) tested every
     * collidable block on its selection bounds, so it stopped on grass; the port ray must too (OUTLINE, not COLLIDER).
     */
    private static final class FeetRayProbe extends HunterProbe {
        private final Supplier<? extends EntityType<? extends Mob>> hunterType;
        private final Block occluder;
        private final boolean selectionOnly;

        FeetRayProbe(Supplier<? extends EntityType<? extends Mob>> hunterType, Block occluder, boolean selectionOnly) {
            this.hunterType = hunterType;
            this.occluder = occluder;
            this.selectionOnly = selectionOnly;
        }

        @Override
        public void setUp(GameTestHelper helper) {
            this.hunter = spawnFrozen(helper, this.hunterType.get(), HUNTER_POS);
            this.prey = spawnPrey(helper, EntityType.ZOMBIE, PREY_POS);
            helper.assertTrue(this.prey instanceof Monster, "precondition: a Zombie is a Monster, the EntityMob prey of orig (" + FINDING + " test setup)");
            double floorY = helper.absolutePos(HUNTER_POS).getY();
            helper.assertTrue(this.hunter.getY() == floorY && this.prey.getY() == floorY,
                    "precondition: hunter and Zombie both stand exactly on rel y 1.0 (abs " + floorY + "), so the feet ray runs from rel y 1.75"
                            + " to rel y 1.0 — never entering the floor row — and crosses the parapet column x 27..28 at rel y 1.14..1.05 — saw "
                            + this.hunter.getY() + " / " + this.prey.getY() + " (" + FINDING + " test geometry)");
            assertSees(helper, this.hunter, this.prey);
            helper.assertTrue(invokeFilter(this.hunter, "isSuitableTarget", this.prey), "precondition: " + this.hunter.getClass().getSimpleName()
                    + ".isSuitableTarget accepts the Zombie on the open floor, so the feet ray is the only step left to refuse it (" + FINDING + " test setup)");
        }

        @Override
        public boolean drive(GameTestHelper helper, boolean occluded) {
            helper.setBlock(PARAPET_POS, occluded ? this.occluder : Blocks.AIR);
            if (occluded && this.selectionOnly) {
                BlockState state = helper.getBlockState(PARAPET_POS);
                BlockPos abs = helper.absolutePos(PARAPET_POS);
                helper.assertTrue(state.getCollisionShape(helper.getLevel(), abs).isEmpty() && !state.getShape(helper.getLevel(), abs).isEmpty(),
                        "precondition: the parapet block (" + state + ") has no collision shape and a non-empty selection shape, so it"
                                + " tells a selection-bounds ray from a collider ray (" + FINDING + " test setup)");
            }
            this.hunter.getSensing().tick();
            String name = this.hunter.getClass().getSimpleName();
            String phase = occluded ? " with the parapet (" + this.occluder + ") at (27, 1, 24)" : " with the parapet razed";
            helper.assertTrue(this.hunter.hasLineOfSight(this.prey), "control: the eye line of the " + name + " (eye "
                    + String.format("%.2f", this.hunter.getEyeHeight()) + " above its feet) to the Zombie's eyes must clear the floor-row parapet"
                    + phase + " (" + FINDING + " test geometry)");
            helper.assertTrue(invokeFilter(this.hunter, "isSuitableTarget", this.prey), "control: " + name + ".isSuitableTarget — the eye-to-eye"
                    + " canSee step among its terms — must still accept the Zombie" + phase + ", so only the feet ray can refuse it (" + FINDING + ")");
            Object found = invoke(this.hunter, this.hunter.getClass(), "findSomethingToAttack");
            this.trace = "findSomethingToAttack -> " + describe((Entity) found) + phase;
            return found == this.prey;
        }

        @Override
        public void cleanUp(GameTestHelper helper) {
            if (this.hunter != null) {
                helper.setBlock(PARAPET_POS, Blocks.AIR);
            }
            super.cleanUp(helper);
        }
    }

    // ------------------------------------------------------------------
    // Helpers (the PlayNicelyGateParityTests / PitchBlackAllyTests idiom)
    // ------------------------------------------------------------------

    private static void setWall(GameTestHelper helper, boolean present) {
        Block block = present ? Blocks.STONE : Blocks.AIR;
        for (int y = WALL_Y_MIN; y <= WALL_Y_MAX; y++) {
            for (int z = WALL_Z_MIN; z <= WALL_Z_MAX; z++) {
                helper.setBlock(new BlockPos(WALL_X, y, z), block);
            }
        }
    }

    private static void assertSees(GameTestHelper helper, Mob hunter, LivingEntity prey) {
        helper.assertTrue(hunter.hasLineOfSight(prey), "precondition: the " + hunter.getClass().getSimpleName()
                + " (eye " + String.format("%.2f", hunter.getEyeHeight()) + " above its feet on the floor) must see the "
                + prey.getClass().getSimpleName() + " on the open floor inside the barrier shell (" + FINDING + " test geometry)");
    }

    /** Frozen on the floor: goals stripped, noAi, persistence set (the IgnoreListParityTests idiom). */
    private static Mob spawnFrozen(GameTestHelper helper, EntityType<? extends Mob> type, BlockPos pos) {
        Mob mob = helper.spawnWithNoFreeWill(type, pos);
        mob.setNoAi(true);
        mob.setPersistenceRequired();
        return mob;
    }

    /** Frozen prey with 1000 HP, so nothing incidental kills it. */
    private static Mob spawnPrey(GameTestHelper helper, EntityType<? extends Mob> type, BlockPos pos) {
        Mob prey = spawnFrozen(helper, type, pos);
        prey.getAttribute(Attributes.MAX_HEALTH).setBaseValue(PREY_HEALTH);
        prey.setHealth(PREY_HEALTH);
        return prey;
    }

    private static void discardQuietly(Entity entity) {
        if (entity != null && !entity.isRemoved()) {
            entity.discard();
        }
    }

    /**
     * ENT-S-122: the Ant Robot (rows 1, 2) and the Nightmare (row 7) hold their filters' sight verdicts in a memo of
     * their own that only their state boundary clears (AntRobot.customServerAiStep unridden, PitchBlack's activity-0
     * branch), which a frozen hunter never reaches; between the two drives the memo is cleared by reflection, as the
     * vanilla cache is by {@code getSensing().tick()}. Hunters without the memo are untouched.
     */
    private static void clearSightMemo(Mob hunter) {
        for (String name : new String[] {"sightMemoSeen", "sightMemoUnseen"}) {
            try {
                Field field = hunter.getClass().getDeclaredField(name);
                field.setAccessible(true);
                ((it.unimi.dsi.fastutil.ints.IntOpenHashSet) field.get(hunter)).clear();
            } catch (NoSuchFieldException absent) {
                return;
            } catch (ReflectiveOperationException exception) {
                throw new IllegalStateException("cannot clear " + hunter.getClass().getSimpleName() + "." + name, exception);
            }
        }
    }

    /**
     * A mock ServerPlayer in the level with an explicit game mode (the game-test server defaults to CREATIVE). The
     * framework's mock answers {@code isCreative()} true whatever its mode; the two picks read the abilities
     * ({@code instabuild}), which the game mode does set. Deprecated mock-player factory tolerated the way
     * PlayNicelyGateParityTests and PeacefulGateParityTests do.
     */
    @SuppressWarnings({"removal", "deprecation"})
    private static ServerPlayer playerAt(GameTestHelper helper, GameType mode, Vec3 absolutePos) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(mode);
        player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000.0);
        player.setHealth(1000.0f);
        player.teleportTo(helper.getLevel(), absolutePos.x, absolutePos.y, absolutePos.z, 0.0f, 0.0f);
        return player;
    }

    private static void removePlayer(GameTestHelper helper, ServerPlayer player) {
        if (player != null) {
            helper.getLevel().getServer().getPlayerList().remove(player);
        }
    }

    /** A seeded random with each (bound, answer) pair pinned — the VortexParityTests.ForcedRoll seam, chained as PlayNicelyGateParityTests.rolls. */
    private static RandomSource rolls(int... boundAnswerPairs) {
        RandomSource source = RandomSource.create(1234L);
        for (int i = 0; i < boundAnswerPairs.length; i += 2) {
            source = new VortexParityTests.ForcedRoll(source, boundAnswerPairs[i], boundAnswerPairs[i + 1]);
        }
        return source;
    }

    /** Same seam as VortexParityTests.forceDiscardRoll: swap {@code Entity.random} for a forced source. */
    private static void replaceRandom(Entity entity, RandomSource forced) {
        try {
            Field field = Entity.class.getDeclaredField("random");
            field.setAccessible(true);
            field.set(entity, forced);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("cannot replace Entity.random", exception);
        }
    }

    /** The hunter's private one-arg filter — {@code isSuitableTarget(LivingEntity)} or the Ant Robot's {@code feetIsSuitableTarget} (the PitchBlackAllyTests idiom). */
    private static boolean invokeFilter(Mob hunter, String name, LivingEntity candidate) {
        String where = hunter.getClass().getSimpleName() + "." + name;
        try {
            Method method = hunter.getClass().getDeclaredMethod(name, LivingEntity.class);
            method.setAccessible(true);
            return (Boolean) method.invoke(hunter, candidate);
        } catch (InvocationTargetException exception) {
            throw new IllegalStateException(where + " threw", exception.getCause());
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("cannot invoke " + where, exception);
        }
    }

    private static Object invoke(Object target, Class<?> declaring, String name) {
        String where = declaring.getSimpleName() + "." + name;
        try {
            Method method = declaring.getDeclaredMethod(name);
            method.setAccessible(true);
            return method.invoke(target);
        } catch (InvocationTargetException exception) {
            throw new IllegalStateException(where + " threw", exception.getCause());
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("cannot invoke " + where, exception);
        }
    }

    private static String describe(Entity entity) {
        return entity == null ? "null" : entity.getClass().getSimpleName() + "#" + entity.getId();
    }
}
