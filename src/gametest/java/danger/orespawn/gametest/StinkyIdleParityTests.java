package danger.orespawn.gametest;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntitySpyro;
import danger.orespawn.entity.EntityStinky;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.PlayLevelSoundEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * ENT-S-119 and ENT-S-123 (the T5/T6 wave): the flying Stinky's 1.7.10 idle tick and the two flyers' flight-target ray,
 * restored in classic.
 * <ul>
 *   <li>orig Stinky.java:582-607 — inside {@code do_movement}, reached only through :511's {@code !isSitting()}: on the
 *       1-in-50 roll under {@code PlayNicely == 0} (:583, the ENT-S-116 gate) the Stinky hunts COAL ORE with
 *       {@code scan_it((int) x, (int) y + 1, (int) z, i, j, i)} (:593; :435-496 probes the six faces of the shell —
 *       +x :443, −x :451, +y :461, −y :469, +z :479, −z :487 — keeping the nearest by squared distance from the scan
 *       origin, :444) over the shells i = 1, 2, 3, 4, 6, 8 (:588-596: the first shell that finds anything breaks; past
 *       i = 4 the trailing {@code ++i} skips 5 and 7), navigates to the pick at 1.25 (:598) and, when {@code closest < 12}
 *       (:599 — from the origin, not the Stinky's block), sets it to air (:600), heals 1.0 (:601) and burps at 0.5 /
 *       pitch {@code nextFloat * 0.2 + 1.5} (:602). Ahead of that branch {@code do_movement} runs the 1-in-7 idle attack
 *       pass for every activity (:568-581: the roll, then {@code != PEACEFUL}, then {@code findSomethingToAttack}; a find
 *       flips to activity 2, sets the flight target onto the prey and bites inside (3 + w/2)²). Port
 *       {@code EntityStinky.eatCoalOre} / {@code scanIt} behind the ENT-S-116 gate line, moved inside the not-sitting
 *       block after {@code doMovement()}; the pass restored in {@code doMovement} ahead of its activity-1 return.</li>
 *   <li>orig Stinky.java:640 / Spyro.java:647 — the flight-target pick refuses an air candidate that fails
 *       {@code canSeeTarget(x, y, z)} (the ENT-S-118 feet ray from {@code posY + 0.75} to the candidate's block corner);
 *       port {@code EntityStinky.doMovement} / {@code EntitySpyro.doMovement}, {@code && canSeeTarget(...)} on the
 *       {@code isAir()} acceptance.</li>
 * </ul>
 *
 * <p>A {@link GameTestGenerator} over {@link #sites()}, one synchronous {@link TestFunction} per row,
 * {@code stinkyidleparitytests.s119_NN_<tag>} / {@code s123_NN_<tag>}, the ENT-S-116 shape: the Stinky frozen at rel
 * (20, 1, 24) on the floor of the 48x16x48 empty_large (goals stripped, noAi, persistent), {@link #HEALTH_DEFICIT} below
 * max so the heal reads as +1, its {@code closest} pre-set to a sentinel so a drive that must not run the eat is told
 * from one that ran it and found nothing, its navigation swapped for a {@link RecordingNavigation} (a frozen mob has
 * never touched the ground, so the real {@code PathNavigation} refuses every path and leaves nothing to read), the
 * port's {@code customServerAiStep} driven once by reflection under pinned rolls (the VortexParityTests.ForcedRoll seam
 * beneath a {@link RollLog} that records the bounds in order — orig's idle tick spends 200, 100, 100 (+20), 7, 50 of
 * the world random, the port of the entity random, ENT-S-093) with a {@link BurpEar} on the NeoForge bus (the
 * StructureTestsC PlayLevelSoundEvent seam: orig :602's {@code playSound} is the port's {@code Entity.playSound} →
 * {@code ServerLevel.playSeededSound} → {@code PlayLevelSoundEvent.AtPosition}). The eat rows place coal ore (or a
 * flower) relative to the scan origin {@code ((int) x, (int) y + 1, (int) z)} and read the blocks, the health,
 * {@code closest}, the recorded {@code moveTo}, the burp and the roll order after the drive. The idle-pass rows put a
 * frozen Zombie (an EntityMob, the prey orig's {@code isSuitableTarget} takes — a pig is not) 3 blocks east, inside the
 * bite reach, and pin the 1-in-7 to fire, to fire under PEACEFUL ({@code MinecraftServer.setDifficulty(PEACEFUL, true)},
 * restored in a finally) and to miss. The flight-ray rows drive the private {@code doMovement} in activity 2 with the
 * pick pinned to one candidate — (x + 6, y + 1, z + 6): xdir = zdir = 6, both signs kept, the Stinky's
 * {@code nextInt(6) - 2} and the Spyro's {@code nextInt(9) - 4} pinned to +1 — and a one-block stone wall on the ray,
 * refused with the wall and accepted with it razed (the ENT-S-118 shape). Every row fails with its port line reverted.
 * {@code PLAY_NICELY} is set false for the drive (the eat and the scan are off under it, ENT-S-116 / ENT-S-115) and
 * restored in a finally; blocks razed, spawns discarded and the listener unregistered there. Own batch (TEST-003).</p>
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class StinkyIdleParityTests {

    private static final String BATCH = "stinkyIdleParity";
    private static final String TEST_PREFIX = "stinkyidleparitytests.";
    /** Generated TestFunctions bypass the holder's template prefixing, so the template is named in full (SightStepParityTests). */
    private static final String EMPTY_LARGE = OreSpawnMod.MOD_ID + ":empty_large";
    private static final int TIMEOUT_TICKS = 100;
    private static final String FINDING = "ENT-S-119";
    private static final String FINDING_RAY = "ENT-S-123";

    /** The Stinky's spot, rel to the structure block (PlayNicelyGriefingGateTests.HUNTER_POS); the scan origin is one above its block. */
    private static final BlockPos HUNTER_POS = new BlockPos(20, 1, 24);
    /** The Zombie 3 blocks east: distSq 9 of the Stinky, inside orig Stinky.java:577's bite reach (3 + w/2)² = 10.89 for its width 0.6. */
    private static final BlockPos PREY_POS = new BlockPos(23, 1, 24);
    /** Prey health, high enough that nothing incidental kills it (the SightStepParityTests idiom). */
    private static final float PREY_HEALTH = 1000.0f;
    /** How far below max the Stinky starts, so the eat's heal(1.0f) (orig Stinky.java:601) reads as +1. */
    private static final float HEALTH_DEFICIT = 10.0f;
    private static final float HEAL_AMOUNT = 1.0f;
    private static final float HEALTH_EPSILON = 1.0e-3f;
    /** orig Stinky.java:584 / :597 — {@code closest} reset to 99999 by the routine; still 99999 after a scan that found nothing. */
    private static final int NOT_FOUND = 99999;
    /** Written into {@code closest} before every drive: a drive that never ran the eat leaves it, one that ran it overwrites it. */
    private static final int CLOSEST_SENTINEL = 4242;
    /** orig Stinky.java:598 — the navigation speed of the eat's moveTo. */
    private static final double EAT_SPEED = 1.25;
    /** orig Stinky.java:602 — "random.burp" at 0.5, pitch nextFloat() * 0.2 + 1.5. */
    private static final float BURP_VOLUME = 0.5f;
    private static final float BURP_PITCH_MIN = 1.5f;
    private static final float BURP_PITCH_MAX = 1.7f;
    /** orig's idle tick in activity 1 with nothing to attack: 200, 100, 100, 7, 50 (the +20 only when the 1-in-100 flip fires; pinned quiet). */
    private static final List<Integer> IDLE_ROLLS = List.of(200, 100, 100, 7, 50);
    /** The idle tick once the pass flipped the activity to 2: no eat roll (orig :582's {@code activity == 1} is read after the pass). */
    private static final List<Integer> IDLE_ROLLS_FLIPPED = List.of(200, 100, 100, 7);
    /** A sitting Stinky: the 1-in-200 and the self-heal roll only — the whole not-sitting block (orig :511) is skipped. */
    private static final List<Integer> SITTING_ROLLS = List.of(200, 100);
    /** The flight pick pinned to one candidate: xdir = zdir = 6 (nextInt(5) → 0), both signs kept (nextInt(2) → 1), y + 1. */
    private static final BlockPos PICK_OFFSET = new BlockPos(6, 1, 6);
    /** The one-block wall: the flyer's block + (3, 0, 3), which the ray from (x + 0.5, y + 0.75, z + 0.5) to the candidate's corner (x + 6, y + 1, z + 6) crosses at 0.86..0.91 of that block's height. */
    private static final BlockPos WALL_OFFSET = new BlockPos(3, 0, 3);
    /** The Stinky's flight pick: 300 → 0 (retarget), 7 → 0 (the pass quiet), 5 → 0, 2 → 1, 6 → 3 (nextInt(6) - 2 = +1). */
    private static final int[] STINKY_PICK_ROLLS = {300, 0, 7, 0, 5, 0, 2, 1, 6, 3};
    /** The Spyro's flight pick: 300 → 0, 6 → 0 (its 1-in-6 pass quiet), 5 → 0, 2 → 1, 9 → 5 (nextInt(9) - 4 = +1). */
    private static final int[] SPYRO_PICK_ROLLS = {300, 0, 6, 0, 5, 0, 2, 1, 9, 5};

    // ------------------------------------------------------------------
    // The site table, in orig file order
    // ------------------------------------------------------------------

    /** One row: {@link #run} sets up, drives and asserts; {@link #cleanUp} runs in the finally, tolerant of a set-up that never finished. */
    private interface Probe {
        void run(GameTestHelper helper, Site site);

        void cleanUp(GameTestHelper helper);
    }

    /** One orig site and the port site that carries it. */
    private record Site(int index, String tag, String finding, String orig, String port, String effect, Supplier<Probe> probe) {
        String testName() {
            return TEST_PREFIX + String.format("%s_%02d_%s", this.finding.equals(FINDING_RAY) ? "s123" : "s119", this.index, this.tag);
        }

        String where() {
            return this.port + " (orig " + this.orig + ")";
        }
    }

    /** One block placed relative to the scan origin before the drive. */
    private record Placed(BlockPos offset, Block block) {
    }

    private static Placed coal(int dx, int dy, int dz) {
        return new Placed(new BlockPos(dx, dy, dz), Blocks.COAL_ORE);
    }

    private static List<Site> sites() {
        List<Site> sites = new ArrayList<>();
        // The scan (orig :593 over scan_it :435-496), shell by shell — origin = ((int) x, (int) y + 1, (int) z)
        sites.add(new Site(1, "stinky_443_shell1_px_eat_heal_burp", FINDING, "Stinky.java:593 with :443 and :598-602", "EntityStinky.eatCoalOre (shell 1, the +x face)",
                "the coal ore at origin + (1, 0, 0), distSq 1, found, walked to at 1.25, eaten to air, the heal of 1 and the burp at 0.5 / 1.5..1.7",
                () -> EatProbe.eaten(1, new BlockPos(1, 0, 0), coal(1, 0, 0))));
        sites.add(new Site(2, "stinky_469_shell1_ny_own_block_eat", FINDING, "Stinky.java:593 with :469 and :599", "EntityStinky.eatCoalOre (shell 1, the −y face)",
                "the coal ore at origin + (0, −1, 0) — the Stinky's own block, distSq 1 of the origin one above it — found and eaten",
                () -> EatProbe.eaten(1, new BlockPos(0, -1, 0), coal(0, -1, 0))));
        sites.add(new Site(3, "stinky_479_shell2_pz_eat", FINDING, "Stinky.java:593 with :479", "EntityStinky.eatCoalOre (shell 2, the +z face)",
                "the coal ore at origin + (0, 0, 2), distSq 4, found by shell 2 and eaten",
                () -> EatProbe.eaten(4, new BlockPos(0, 0, 2), coal(0, 0, 2))));
        sites.add(new Site(4, "stinky_461_shell2_py_eat", FINDING, "Stinky.java:593 with :461 (dy = min(i, 2))", "EntityStinky.eatCoalOre (shell 2, the +y face)",
                "the coal ore at origin + (0, 2, 0), distSq 4, found by shell 2's +y face at y + 2 and eaten",
                () -> EatProbe.eaten(4, new BlockPos(0, 2, 0), coal(0, 2, 0))));
        sites.add(new Site(5, "stinky_451_shell3_nx_eat", FINDING, "Stinky.java:593 with :451", "EntityStinky.eatCoalOre (shell 3, the −x face)",
                "the coal ore at origin + (−3, 0, 0), distSq 9, found by shell 3 and eaten (9 < 12)",
                () -> EatProbe.eaten(9, new BlockPos(-3, 0, 0), coal(-3, 0, 0))));
        sites.add(new Site(6, "stinky_599_shell4_px_found_not_eaten", FINDING, "Stinky.java:593 with :443, :598-599", "EntityStinky.eatCoalOre (shell 4, the eat radius)",
                "the coal ore at origin + (4, 0, 0), distSq 16, found by shell 4 and walked to at 1.25 but left standing (16 is not < 12): no heal, no burp",
                () -> EatProbe.found(16, new BlockPos(4, 0, 0), coal(4, 0, 0))));
        sites.add(new Site(7, "stinky_594_shell5_skipped", FINDING, "Stinky.java:594-595 (i < 4 continue; ++i)", "EntityStinky.eatCoalOre (the shell sequence 1, 2, 3, 4, 6, 8)",
                "the coal ore at origin + (5, 0, 0) — on shell 5's +x face only — never scanned: closest stays 99999, no walk, the block standing",
                () -> EatProbe.nothing(coal(5, 0, 0))));
        sites.add(new Site(8, "stinky_487_shell6_nz_found", FINDING, "Stinky.java:593 with :487", "EntityStinky.eatCoalOre (shell 6, the −z face)",
                "the coal ore at origin + (0, 0, −6), distSq 36, found by shell 6 and walked to, left standing",
                () -> EatProbe.found(36, new BlockPos(0, 0, -6), coal(0, 0, -6))));
        sites.add(new Site(9, "stinky_594_shell7_skipped", FINDING, "Stinky.java:594-595 (i < 4 continue; ++i)", "EntityStinky.eatCoalOre (the shell sequence 1, 2, 3, 4, 6, 8)",
                "the coal ore at origin + (7, 0, 0) — on shell 7's +x face only — never scanned: closest stays 99999, no walk, the block standing",
                () -> EatProbe.nothing(coal(7, 0, 0))));
        sites.add(new Site(10, "stinky_443_shell8_px_found", FINDING, "Stinky.java:588 (i < 9) with :443", "EntityStinky.eatCoalOre (shell 8, the +x face)",
                "the coal ore at origin + (8, 0, 0), distSq 64, found by the last shell and walked to, left standing",
                () -> EatProbe.found(64, new BlockPos(8, 0, 0), coal(8, 0, 0))));
        sites.add(new Site(11, "stinky_444_nearest_by_distsq_wins", FINDING, "Stinky.java:444 (d < closest)", "EntityStinky.scanIt (the nearest, not the first)",
                "two coal ores on shell 2's +x face — origin + (2, −1, −2), distSq 9, scanned first, and origin + (2, 0, 0), distSq 4, scanned later —"
                        + " the nearer one taken and eaten, the farther one standing",
                () -> EatProbe.eaten(4, new BlockPos(2, 0, 0), coal(2, -1, -2), coal(2, 0, 0))));
        sites.add(new Site(12, "stinky_443_flower_ignored", FINDING, "Stinky.java:443 (== Blocks.field_150365_q)", "EntityStinky.isCoalOre (coal ore, not flowers)",
                "a dandelion at origin + (1, −1, 0) — the old port's site, shell 1's +x face — ignored: closest stays 99999, no walk, the flower standing, no heal, no burp",
                () -> EatProbe.nothing(new Placed(new BlockPos(1, -1, 0), Blocks.DANDELION))));
        // The sitting gate (orig :511) and the idle attack pass (orig :568-581)
        sites.add(new Site(13, "stinky_511_sitting_no_eat", FINDING, "Stinky.java:511 (!isSitting() → do_movement) with :582-583", "EntityStinky.customServerAiStep (the call inside the not-sitting block)",
                "a sitting Stinky with the coal ore at origin + (1, 0, 0) in reach: the eat never runs (closest untouched), the block standing, no heal, no burp, the rolls 200 and 100 only",
                () -> EatProbe.sitting(coal(1, 0, 0))));
        sites.add(new Site(14, "stinky_568_idle_pass_fires", FINDING, "Stinky.java:568-581", "EntityStinky.doMovement (the 1-in-7 idle attack pass)",
                "activity 1, the 1-in-7 pinned to fire with a Zombie 3 blocks east: activity 2, the flight target on the Zombie's block one up, the Zombie bitten, no eat roll after the flip",
                () -> new IdlePassProbe(IdlePass.FIRES)));
        sites.add(new Site(15, "stinky_568_idle_pass_peaceful", FINDING, "Stinky.java:568 (nextInt(7) == 1 && difficulty != PEACEFUL)", "EntityStinky.doMovement (the pass under PEACEFUL)",
                "the 1-in-7 pinned to fire under PEACEFUL: the roll spent, the scan not run — activity 1, the flight target and the Zombie untouched, the eat roll reached",
                () -> new IdlePassProbe(IdlePass.PEACEFUL)));
        sites.add(new Site(16, "stinky_568_idle_pass_missed", FINDING, "Stinky.java:568 (nextInt(7) == 1)", "EntityStinky.doMovement (the pass on a missed roll)",
                "the 1-in-7 pinned to miss with the Zombie in reach: the roll spent, nothing else — activity 1, the flight target and the Zombie untouched, the eat roll reached",
                () -> new IdlePassProbe(IdlePass.MISSED)));
        // ENT-S-123 — the flight-target ray (orig Stinky.java:640, Spyro.java:647)
        sites.add(new Site(17, "stinky_640_flight_ray_wall", FINDING_RAY, "Stinky.java:640 with :317-319", "EntityStinky.doMovement (the flight-target pick's canSeeTarget)",
                "the pinned air candidate (x + 6, y + 1, z + 6) refused behind a one-block wall, the flight target left where it was; accepted with the wall razed",
                () -> new FlightRayProbe(false)));
        sites.add(new Site(18, "spyro_647_flight_ray_wall", FINDING_RAY, "Spyro.java:647 with :436-438", "EntitySpyro.doMovement (the flight-target pick's canSeeTarget)",
                "the pinned air candidate (x + 6, y + 1, z + 6) refused behind a one-block wall, the flight target left where it was; accepted with the wall razed",
                () -> new FlightRayProbe(true)));
        sites.add(new Site(19, "stinky_599_eat_radius_from_scan_origin", FINDING, "Stinky.java:599 (closest < 12, from the scan origin)", "EntityStinky.eatCoalOre (the eat radius base)",
                "the coal ore at origin + (3, 1, 1), distSq 11 from the scan origin (shell 3's +x face at dy = 2) — eaten, healed and burped; from the Stinky's own block it is 14, so a radius measured from the entity block (HEAD's base) would leave it standing (refuter T1)",
                () -> EatProbe.eaten(11, new BlockPos(3, 1, 1), coal(3, 1, 1))));
        return sites;
    }

    /** One test per row: 19 TestFunctions in the {@code stinkyIdleParity} batch. */
    @GameTestGenerator
    public Collection<TestFunction> stinkyIdleSites() {
        List<TestFunction> functions = new ArrayList<>();
        for (Site site : sites()) {
            functions.add(new TestFunction(BATCH, site.testName(), EMPTY_LARGE, Rotation.NONE, TIMEOUT_TICKS, 0L, true,
                    helper -> run(helper, site)));
        }
        return functions;
    }

    // ------------------------------------------------------------------
    // Runner: the flag down for the drive, everything restored in the finally
    // ------------------------------------------------------------------

    private static void run(GameTestHelper helper, Site site) {
        final boolean prior = OreSpawnConfig.PLAY_NICELY.get();
        Probe probe = site.probe().get();
        try {
            OreSpawnConfig.PLAY_NICELY.set(false);
            helper.assertTrue(!OreSpawnConfig.PLAY_NICELY.get(),
                    "precondition: PLAY_NICELY.set(false) must read back false — the eat (ENT-S-116) and the scan (ENT-S-115) are off"
                            + " under it, which is not what these rows test (" + site.finding() + " test setup)");
            probe.run(helper, site);
        } finally {
            OreSpawnConfig.PLAY_NICELY.set(prior);
            probe.cleanUp(helper);
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------
    // The eat shape: blocks around the scan origin, one idle tick, the block / health / closest / walk / burp / rolls read
    // ------------------------------------------------------------------

    /**
     * One idle tick of a frozen Stinky in activity 1 (or sitting) with blocks placed relative to the scan origin; after
     * the drive {@code closest} must read {@link #expectClosest}, the navigation must have been pointed at the pick
     * ({@link #expectPick}, an offset from the origin; null = never), the pick eaten to air when {@link #expectEaten}
     * (every other placed block standing), the health up by 1 and one burp heard exactly when eaten, and the roll
     * bounds in orig's order.
     */
    private static final class EatProbe implements Probe {
        private final List<Placed> placed;
        private final int expectClosest;
        private final BlockPos expectPick;
        private final boolean expectEaten;
        private final boolean sitting;

        private EntityStinky stinky;
        private BlockPos origin;
        private final List<BlockPos> placedAbs = new ArrayList<>();
        private RecordingNavigation navigation;
        private RollLog log;
        private BurpEar ear;
        private float healthBefore;

        private EatProbe(List<Placed> placed, int expectClosest, BlockPos expectPick, boolean expectEaten, boolean sitting) {
            this.placed = placed;
            this.expectClosest = expectClosest;
            this.expectPick = expectPick;
            this.expectEaten = expectEaten;
            this.sitting = sitting;
        }

        /** Found at {@code distSq} (under 12) and eaten. */
        static EatProbe eaten(int distSq, BlockPos pick, Placed... placed) {
            return new EatProbe(List.of(placed), distSq, pick, true, false);
        }

        /** Found at {@code distSq} (12 or more), walked to, left standing. */
        static EatProbe found(int distSq, BlockPos pick, Placed... placed) {
            return new EatProbe(List.of(placed), distSq, pick, false, false);
        }

        /** The scan ran and found nothing: closest 99999, no walk, everything standing. */
        static EatProbe nothing(Placed... placed) {
            return new EatProbe(List.of(placed), NOT_FOUND, null, false, false);
        }

        /** Sitting: the eat never ran — closest keeps its sentinel, no walk, everything standing. */
        static EatProbe sitting(Placed... placed) {
            return new EatProbe(List.of(placed), CLOSEST_SENTINEL, null, false, true);
        }

        @Override
        public void run(GameTestHelper helper, Site site) {
            this.stinky = spawnFrozen(helper, ModEntities.ENTITY_STINKY.get(), HUNTER_POS);
            this.stinky.setActivity(1);
            helper.assertTrue(this.stinky.activity == 1 && this.stinky.getActivity() == 1,
                    "precondition: activity 1 — orig Stinky.java:582 runs the eat inside activity == 1 only (" + FINDING + " test setup)");
            helper.assertTrue(!this.stinky.isTame(),
                    "precondition: a fresh Stinky is untamed — no owner-flying or far-owner flip to 2 (" + FINDING + " test setup)");
            this.stinky.setOrderedToSit(this.sitting);
            helper.assertTrue(this.stinky.isOrderedToSit() == this.sitting,
                    "precondition: setOrderedToSit(" + this.sitting + ") must read back — orig :511 runs do_movement, and the eat inside it,"
                            + " only while not sitting (" + FINDING + " test setup)");
            float max = this.stinky.getMaxHealth();
            this.healthBefore = max - HEALTH_DEFICIT;
            this.stinky.setHealth(this.healthBefore);
            helper.assertTrue(this.stinky.getHealth() == this.healthBefore && this.healthBefore < max,
                    "precondition: the Stinky starts " + HEALTH_DEFICIT + " below its max " + max + ", so the eat's heal(1.0f) (orig :601)"
                            + " reads as +1 — got " + this.stinky.getHealth() + " (" + FINDING + " test setup)");
            writeField(this.stinky, EntityStinky.class, "closest", CLOSEST_SENTINEL);
            this.navigation = installRecordingNavigation(this.stinky);
            this.origin = scanOrigin(this.stinky);
            for (Placed one : this.placed) {
                BlockPos abs = this.origin.offset(one.offset());
                helper.assertTrue(helper.getBounds().contains(Vec3.atCenterOf(abs)), "precondition: origin + " + one.offset().toShortString()
                        + " = rel " + rel(helper, abs).toShortString() + " lies inside the structure (" + FINDING + " test geometry)");
                helper.assertTrue(helper.getLevel().getBlockState(abs).isAir(), "precondition: origin + " + one.offset().toShortString()
                        + " (rel " + rel(helper, abs).toShortString() + ") is air before the row places its block (" + FINDING + " test geometry)");
                helper.getLevel().setBlock(abs, one.block().defaultBlockState(), 3);
                this.placedAbs.add(abs);
                helper.assertTrue(helper.getLevel().getBlockState(abs).is(one.block()), "precondition: " + name(one.block()) + " must stand at origin + "
                        + one.offset().toShortString() + " (rel " + rel(helper, abs).toShortString() + ") before the drive (" + FINDING + " test setup)");
            }
            this.log = new RollLog(rolls(200, 0, 100, 0, 7, 0, 50, 0));
            replaceRandom(this.stinky, this.log);
            this.ear = new BurpEar(this.stinky);
            try {
                invoke(this.stinky, EntityStinky.class, "customServerAiStep");
            } finally {
                this.ear.close();
            }

            int closest = readInt(this.stinky, "closest");
            String trace = trace(helper, closest);
            helper.assertTrue(closest == this.expectClosest, site.where() + ": closest must read " + describeClosest(this.expectClosest)
                    + " — orig " + site.orig() + ": " + site.effect() + " — saw " + trace + " (" + FINDING + ")");
            if (this.expectPick == null) {
                helper.assertTrue(this.navigation.calls == 0, site.where() + ": nothing found (or the eat never run), so orig :597-598 never points"
                        + " the navigation — saw " + trace + " (" + FINDING + ")");
            } else {
                BlockPos pick = this.origin.offset(this.expectPick);
                helper.assertTrue(this.navigation.calls == 1 && Vec3.atLowerCornerOf(pick).equals(this.navigation.target)
                                && this.navigation.speed == EAT_SPEED,
                        site.where() + ": orig :598 points the navigation at the pick " + pick.toShortString() + " (rel " + rel(helper, pick).toShortString()
                                + ", origin + " + this.expectPick.toShortString() + ") at " + EAT_SPEED + ", once — saw " + trace + " (" + FINDING + ")");
            }
            for (int i = 0; i < this.placed.size(); i++) {
                Placed one = this.placed.get(i);
                BlockPos abs = this.placedAbs.get(i);
                BlockState state = helper.getLevel().getBlockState(abs);
                boolean eatenOne = this.expectEaten && one.offset().equals(this.expectPick);
                if (eatenOne) {
                    helper.assertTrue(state.isAir(), site.where() + ": the pick at origin + " + one.offset().toShortString() + " (rel "
                            + rel(helper, abs).toShortString() + ") is inside the eat radius, so orig :600 sets it to air — saw " + trace + " (" + FINDING + ")");
                } else {
                    helper.assertTrue(state.is(one.block()), site.where() + ": " + name(one.block()) + " at origin + " + one.offset().toShortString() + " (rel "
                            + rel(helper, abs).toShortString() + ") must still stand — " + (this.expectEaten ? "only the nearest pick is eaten"
                            : this.expectPick != null ? "a pick at distSq 12 or more is walked to, not eaten (orig :599)" : "nothing is eaten") + " — saw " + trace + " (" + FINDING + ")");
                }
            }
            float expectedHealth = this.expectEaten ? this.healthBefore + HEAL_AMOUNT : this.healthBefore;
            helper.assertTrue(Math.abs(this.stinky.getHealth() - expectedHealth) < HEALTH_EPSILON, site.where() + ": the health must be "
                    + expectedHealth + " (" + (this.expectEaten ? "orig :601 heals 1.0 with the eat" : "no eat, no heal") + ") — saw " + trace + " (" + FINDING + ")");
            if (this.expectEaten) {
                helper.assertTrue(this.ear.count == 1 && this.ear.volume == BURP_VOLUME && this.ear.pitch >= BURP_PITCH_MIN && this.ear.pitch < BURP_PITCH_MAX,
                        site.where() + ": orig :602 plays random.burp once with the eat at " + BURP_VOLUME + ", pitch " + BURP_PITCH_MIN + ".." + BURP_PITCH_MAX
                                + " (the port's PLAYER_BURP through PlayLevelSoundEvent.AtPosition) — saw " + trace + " (" + FINDING + ")");
            } else {
                helper.assertTrue(this.ear.count == 0, site.where() + ": no eat, so no burp (orig :602 sits inside the :599 radius test) — saw " + trace + " (" + FINDING + ")");
            }
            List<Integer> expectedRolls = this.sitting ? SITTING_ROLLS : IDLE_ROLLS;
            helper.assertTrue(this.log.bounds.equals(expectedRolls), site.where() + ": the tick's roll bounds must be " + expectedRolls + " in that order — orig "
                    + (this.sitting ? ":501-510 before the :511 sitting gate" : ":501-508, then do_movement's :568 pass, then the :583 eat roll") + " — saw " + trace + " (" + FINDING + ")");
        }

        private String trace(GameTestHelper helper, int closest) {
            StringBuilder blocks = new StringBuilder();
            for (int i = 0; i < this.placed.size(); i++) {
                Placed one = this.placed.get(i);
                BlockState state = helper.getLevel().getBlockState(this.placedAbs.get(i));
                if (i > 0) blocks.append(", ");
                blocks.append(name(one.block())).append(" at origin + ").append(one.offset().toShortString()).append(' ')
                        .append(state.is(one.block()) ? "standing" : state.isAir() ? "gone (air)" : "replaced by " + state);
            }
            return "closest " + closest + ", moveTo " + (this.navigation.calls == 0 ? "never" : this.navigation.target + " at " + this.navigation.speed
                    + " x" + this.navigation.calls) + ", blocks [" + blocks + "], health " + this.healthBefore + " -> " + this.stinky.getHealth()
                    + ", burps " + this.ear.burps + ", rolls " + this.log.bounds + ", activity " + this.stinky.activity;
        }

        @Override
        public void cleanUp(GameTestHelper helper) {
            if (this.ear != null) {
                this.ear.close();
            }
            for (BlockPos abs : this.placedAbs) {
                helper.getLevel().setBlock(abs, Blocks.AIR.defaultBlockState(), 3);
            }
            discardQuietly(this.stinky);
        }
    }

    private static String describeClosest(int closest) {
        if (closest == CLOSEST_SENTINEL) return "the untouched sentinel " + CLOSEST_SENTINEL + " (the eat never ran)";
        if (closest == NOT_FOUND) return NOT_FOUND + " (scanned, nothing found)";
        return "distSq " + closest;
    }

    // ------------------------------------------------------------------
    // The idle attack pass: a Zombie in reach, the 1-in-7 pinned three ways
    // ------------------------------------------------------------------

    private enum IdlePass { FIRES, PEACEFUL, MISSED }

    /**
     * orig Stinky.java:568-581 — one idle tick in activity 1 with a frozen Zombie 3 blocks east (inside the 12/6/12 box,
     * the eye line and the feet ray clear, distSq 9 inside the (3 + w/2)² bite reach). {@link IdlePass#FIRES}: the pass
     * flips to activity 2, sets the flight target to {@code ((int) x, (int) (y + 1), (int) z)} of the prey, bites it, and
     * the eat roll is never reached. {@link IdlePass#PEACEFUL}: the roll is spent, the difficulty stops the scan.
     * {@link IdlePass#MISSED}: the roll is spent, nothing else. In both quiet cases the activity stays 1, the flight
     * target and the Zombie are untouched and the eat roll follows (nothing to eat: closest 99999).
     */
    private static final class IdlePassProbe implements Probe {
        private final IdlePass mode;
        private EntityStinky stinky;
        private Mob prey;
        private MinecraftServer server;
        private Difficulty priorDifficulty;
        private boolean difficultyTouched;
        private BurpEar ear;

        IdlePassProbe(IdlePass mode) {
            this.mode = mode;
        }

        @Override
        public void run(GameTestHelper helper, Site site) {
            this.stinky = spawnFrozen(helper, ModEntities.ENTITY_STINKY.get(), HUNTER_POS);
            this.stinky.setActivity(1);
            helper.assertTrue(this.stinky.activity == 1 && this.stinky.getActivity() == 1 && !this.stinky.isTame() && !this.stinky.isOrderedToSit(),
                    "precondition: activity 1, untamed, not sitting — the idle state the pass runs from (" + FINDING + " test setup)");
            writeField(this.stinky, EntityStinky.class, "closest", CLOSEST_SENTINEL);
            installRecordingNavigation(this.stinky);
            this.prey = spawnPrey(helper, EntityType.ZOMBIE, PREY_POS);
            helper.assertTrue(this.prey instanceof Monster, "precondition: a Zombie is a Monster, the EntityMob prey orig Stinky.java:668 takes (" + FINDING + " test setup)");
            float reach = 3.0f + this.prey.getBbWidth() / 2.0f;
            helper.assertTrue(this.stinky.distanceToSqr(this.prey) < (double) (reach * reach), "precondition: the Zombie at rel " + PREY_POS.toShortString()
                    + " is inside the bite reach (3 + w/2)² = " + (reach * reach) + " (orig :577) — distSq " + this.stinky.distanceToSqr(this.prey) + " (" + FINDING + " test geometry)");
            this.stinky.getSensing().tick();
            helper.assertTrue(this.stinky.hasLineOfSight(this.prey), "precondition: the Stinky sees the Zombie on the open floor — the eye line of isSuitableTarget (" + FINDING + " test geometry)");
            BlockPos flightBefore = this.stinky.blockPosition();
            writeField(this.stinky, EntityStinky.class, "currentFlightTarget", flightBefore);
            if (this.mode == IdlePass.PEACEFUL) {
                this.server = helper.getLevel().getServer();
                this.priorDifficulty = helper.getLevel().getDifficulty();
                this.difficultyTouched = true;
                this.server.setDifficulty(Difficulty.PEACEFUL, true);
                helper.assertTrue(helper.getLevel().getDifficulty() == Difficulty.PEACEFUL,
                        "precondition: MinecraftServer.setDifficulty(PEACEFUL, true) must show through level.getDifficulty() (" + FINDING + " test setup)");
            } else {
                helper.assertTrue(helper.getLevel().getDifficulty() != Difficulty.PEACEFUL,
                        "precondition: the game-test level runs at NORMAL, not Peaceful (" + FINDING + " test setup)");
            }
            RollLog log = new RollLog(rolls(200, 0, 100, 0, 7, this.mode == IdlePass.MISSED ? 0 : 1, 50, 0));
            replaceRandom(this.stinky, log);
            this.ear = new BurpEar(this.stinky);
            try {
                invoke(this.stinky, EntityStinky.class, "customServerAiStep");
            } finally {
                this.ear.close();
            }

            BlockPos flightAfter = (BlockPos) readField(this.stinky, EntityStinky.class, "currentFlightTarget");
            int closest = readInt(this.stinky, "closest");
            String trace = "activity " + this.stinky.activity + " / synched " + this.stinky.getActivity() + ", flight target " + flightBefore.toShortString()
                    + " -> " + flightAfter.toShortString() + ", Zombie health " + PREY_HEALTH + " -> " + this.prey.getHealth() + ", closest " + closest
                    + ", rolls " + log.bounds + ", burps " + this.ear.burps + ", difficulty " + helper.getLevel().getDifficulty();
            if (this.mode == IdlePass.FIRES) {
                BlockPos onPrey = BlockPos.containing(this.prey.getX(), this.prey.getY() + 1.0, this.prey.getZ());
                helper.assertTrue(this.stinky.activity == 2 && this.stinky.getActivity() == 2, site.where() + ": orig :574 flips to activity 2 on a find"
                        + " — saw " + trace + " (" + FINDING + ")");
                helper.assertTrue(flightAfter.equals(onPrey), site.where() + ": orig :575 sets the flight target to the prey's block one up, "
                        + onPrey.toShortString() + " — saw " + trace + " (" + FINDING + ")");
                helper.assertTrue(this.prey.getHealth() < PREY_HEALTH, site.where() + ": orig :577-578 bites the prey inside (3 + w/2)² — saw " + trace + " (" + FINDING + ")");
                helper.assertTrue(log.bounds.equals(IDLE_ROLLS_FLIPPED), site.where() + ": after the flip orig :582's activity == 1 is false, so the eat roll is not spent"
                        + " — the bounds must be " + IDLE_ROLLS_FLIPPED + " — saw " + trace + " (" + FINDING + ")");
                helper.assertTrue(closest == CLOSEST_SENTINEL, site.where() + ": the eat never ran after the flip (closest untouched) — saw " + trace + " (" + FINDING + ")");
            } else {
                String why = this.mode == IdlePass.PEACEFUL ? "orig :568 spends the roll first and then stops on PEACEFUL — no scan, no flip"
                        : "orig :568's roll missed — no scan, no flip";
                helper.assertTrue(this.stinky.activity == 1 && this.stinky.getActivity() == 1, site.where() + ": " + why + " — the activity stays 1 — saw " + trace + " (" + FINDING + ")");
                helper.assertTrue(flightAfter.equals(flightBefore), site.where() + ": " + why + " — the flight target stays — saw " + trace + " (" + FINDING + ")");
                helper.assertTrue(this.prey.getHealth() == PREY_HEALTH, site.where() + ": " + why + " — the Zombie is not bitten — saw " + trace + " (" + FINDING + ")");
                helper.assertTrue(log.bounds.equals(IDLE_ROLLS), site.where() + ": the 1-in-7 is spent (orig :568 rolls before it reads the difficulty) and the"
                        + " eat roll follows in activity 1 — the bounds must be " + IDLE_ROLLS + " — saw " + trace + " (" + FINDING + ")");
                helper.assertTrue(closest == NOT_FOUND, site.where() + ": the eat ran after the quiet pass and found nothing (closest 99999) — saw " + trace + " (" + FINDING + ")");
            }
            helper.assertTrue(this.ear.count == 0, site.where() + ": nothing eaten, so no burp — saw " + trace + " (" + FINDING + ")");
        }

        @Override
        public void cleanUp(GameTestHelper helper) {
            if (this.ear != null) {
                this.ear.close();
            }
            if (this.difficultyTouched) {
                this.server.setDifficulty(this.priorDifficulty, true);
            }
            discardQuietly(this.prey);
            discardQuietly(this.stinky);
        }
    }

    // ------------------------------------------------------------------
    // ENT-S-123: the flight-target pick behind a one-block wall
    // ------------------------------------------------------------------

    /**
     * orig Stinky.java:640 / Spyro.java:647 — the flyer in activity 2, untamed, its flight target set to its own block
     * (so the pick runs: the target is within 2.1 of it), the pick's rolls pinned to the one candidate
     * {@code ((int) x + 6, (int) y + 1, (int) z + 6)} for all 50 tries; a stone at the flyer's block + (3, 0, 3) sits on
     * the ray from {@code (x, y + 0.75, z)} to the candidate's corner ({@code canSeeTarget} asserted false with it and true
     * without it — the control that only the ray decides). With the wall the private {@code doMovement} must leave the
     * flight target where it was (every try refused); with the wall razed it must take the candidate.
     */
    private static final class FlightRayProbe implements Probe {
        private final boolean spyro;
        private TamableAnimal flyer;
        private BlockPos wall;
        private String trace = "(not driven)";

        FlightRayProbe(boolean spyro) {
            this.spyro = spyro;
        }

        @Override
        public void run(GameTestHelper helper, Site site) {
            if (this.spyro) {
                EntitySpyro spawned = spawnFrozen(helper, ModEntities.ENTITY_SPYRO.get(), HUNTER_POS);
                spawned.setActivity(2);
                this.flyer = spawned;
                helper.assertTrue(spawned.activity == 2 && spawned.getActivity() == 2, "precondition: activity 2 — orig Spyro.java's flight runs outside activity 1 (" + FINDING_RAY + " test setup)");
            } else {
                EntityStinky spawned = spawnFrozen(helper, ModEntities.ENTITY_STINKY.get(), HUNTER_POS);
                spawned.setActivity(2);
                this.flyer = spawned;
                helper.assertTrue(spawned.activity == 2 && spawned.getActivity() == 2, "precondition: activity 2 — orig Stinky.java:582-607 returns before the flight in activity 1 (" + FINDING_RAY + " test setup)");
            }
            helper.assertTrue(!this.flyer.isTame() && !this.flyer.isOrderedToSit(), "precondition: untamed (the ownerless pick: xdir/zdir = nextInt(5) + 6) and not sitting (" + FINDING_RAY + " test setup)");
            BlockPos base = new BlockPos((int) this.flyer.getX(), (int) this.flyer.getY(), (int) this.flyer.getZ());
            BlockPos candidate = base.offset(PICK_OFFSET);
            this.wall = this.flyer.blockPosition().offset(WALL_OFFSET);
            helper.assertTrue(helper.getBounds().contains(Vec3.atCenterOf(candidate)) && helper.getBounds().contains(Vec3.atCenterOf(this.wall)),
                    "precondition: the candidate (rel " + rel(helper, candidate).toShortString() + ") and the wall (rel " + rel(helper, this.wall).toShortString()
                            + ") lie inside the structure (" + FINDING_RAY + " test geometry)");
            helper.assertTrue(helper.getLevel().getBlockState(candidate).isAir() && helper.getLevel().getBlockState(this.wall).isAir(),
                    "precondition: the candidate and the wall's spot are air before the row starts — the candidate must be air, so only the ray can refuse it (" + FINDING_RAY + " test geometry)");
            BlockPos sentinel = this.flyer.blockPosition();
            writeField(this.flyer, this.flyer.getClass(), "currentFlightTarget", sentinel);
            int[] pins = this.spyro ? SPYRO_PICK_ROLLS : STINKY_PICK_ROLLS;
            String name = this.flyer.getClass().getSimpleName();

            helper.getLevel().setBlock(this.wall, Blocks.STONE.defaultBlockState(), 3);
            helper.assertTrue(helper.getLevel().getBlockState(this.wall).is(Blocks.STONE), "precondition: the stone wall stands at rel " + rel(helper, this.wall).toShortString() + " (" + FINDING_RAY + " test setup)");
            helper.assertTrue(!canSee(this.flyer, candidate), "precondition: " + name + ".canSeeTarget refuses the candidate's corner " + candidate.toShortString()
                    + " through the wall at rel " + rel(helper, this.wall).toShortString() + " (" + FINDING_RAY + " test geometry)");
            replaceRandom(this.flyer, rolls(pins));
            invoke(this.flyer, this.flyer.getClass(), "doMovement");
            BlockPos afterWalled = (BlockPos) readField(this.flyer, this.flyer.getClass(), "currentFlightTarget");
            this.trace = "currentFlightTarget " + sentinel.toShortString() + " -> " + afterWalled.toShortString() + " with the wall at rel "
                    + rel(helper, this.wall).toShortString() + " (candidate " + candidate.toShortString() + ", rel " + rel(helper, candidate).toShortString() + ")";
            helper.assertTrue(afterWalled.equals(sentinel), site.where() + " with the wall: orig " + site.orig() + " turns an air candidate the ray cannot reach"
                    + " into stone, so all 50 tries at the pinned candidate are refused and the flight target stays — saw " + this.trace + " (" + FINDING_RAY + ")");

            helper.getLevel().setBlock(this.wall, Blocks.AIR.defaultBlockState(), 3);
            helper.assertTrue(canSee(this.flyer, candidate), "control: " + name + ".canSeeTarget accepts the candidate's corner once the wall is razed (" + FINDING_RAY + " test geometry)");
            replaceRandom(this.flyer, rolls(pins));
            invoke(this.flyer, this.flyer.getClass(), "doMovement");
            BlockPos afterOpen = (BlockPos) readField(this.flyer, this.flyer.getClass(), "currentFlightTarget");
            this.trace = "currentFlightTarget " + sentinel.toShortString() + " -> " + afterOpen.toShortString() + " with the wall razed (candidate "
                    + candidate.toShortString() + ")";
            helper.assertTrue(afterOpen.equals(candidate), "control: with the wall razed " + site.where() + " must take the same candidate on the first try"
                    + " — the ray, not the pins or the air test, refused it — saw " + this.trace + " (" + FINDING_RAY + ")");
        }

        @Override
        public void cleanUp(GameTestHelper helper) {
            if (this.wall != null) {
                helper.getLevel().setBlock(this.wall, Blocks.AIR.defaultBlockState(), 3);
            }
            discardQuietly(this.flyer);
        }
    }

    // ------------------------------------------------------------------
    // Seams
    // ------------------------------------------------------------------

    /**
     * A {@link GroundPathNavigation} that records {@code moveTo(x, y, z, speed)} instead of pathing: a frozen mob has
     * never touched the ground, so the real navigation's {@code canUpdatePath} refuses every path and the eat's walk
     * (orig Stinky.java:598) would leave nothing to read. Installed into {@code Mob.navigation} by reflection.
     */
    private static final class RecordingNavigation extends GroundPathNavigation {
        Vec3 target;
        double speed = Double.NaN;
        int calls;

        RecordingNavigation(Mob mob, Level level) {
            super(mob, level);
        }

        @Override
        public boolean moveTo(double x, double y, double z, double speedModifier) {
            this.target = new Vec3(x, y, z);
            this.speed = speedModifier;
            this.calls++;
            return true;
        }
    }

    private static RecordingNavigation installRecordingNavigation(Mob mob) {
        RecordingNavigation navigation = new RecordingNavigation(mob, mob.level());
        writeField(mob, Mob.class, "navigation", navigation);
        if (mob.getNavigation() != navigation) {
            throw new IllegalStateException("Mob.getNavigation() does not answer the installed RecordingNavigation");
        }
        return navigation;
    }

    /**
     * Every PLAYER_BURP the level plays within 4 blocks of the Stinky while open — the StructureTestsC PlayLevelSoundEvent
     * seam: orig :602's {@code playSound("random.burp", …)} is the port's {@code Entity.playSound}, which reaches
     * {@code ServerLevel.playSeededSound} and its {@code PlayLevelSoundEvent.AtPosition} before the broadcast.
     */
    private static final class BurpEar {
        final List<String> burps = new ArrayList<>();
        int count;
        float volume = Float.NaN;
        float pitch = Float.NaN;
        private final Consumer<PlayLevelSoundEvent> listener;
        private boolean open;

        BurpEar(Entity around) {
            Vec3 centre = around.position();
            this.listener = event -> {
                if (event instanceof PlayLevelSoundEvent.AtPosition at && event.getSound() != null
                        && event.getSound().value() == SoundEvents.PLAYER_BURP && at.getPosition().distanceTo(centre) < 4.0) {
                    this.count++;
                    this.volume = event.getOriginalVolume();
                    this.pitch = event.getOriginalPitch();
                    this.burps.add(String.format("burp %.2f / %.4f", event.getOriginalVolume(), event.getOriginalPitch()));
                }
            };
            NeoForge.EVENT_BUS.addListener(PlayLevelSoundEvent.class, this.listener);
            this.open = true;
        }

        void close() {
            if (this.open) {
                NeoForge.EVENT_BUS.unregister(this.listener);
                this.open = false;
            }
        }
    }

    /** Records the bound of every {@code nextInt(bound)} in order and answers from the pinned chain beneath (the ProactiveHuntParityTests CountingRandom idiom, ordered). */
    static final class RollLog implements RandomSource {
        private final RandomSource delegate;
        final List<Integer> bounds = new ArrayList<>();

        RollLog(RandomSource delegate) {
            this.delegate = delegate;
        }

        @Override
        public RandomSource fork() {
            return new RollLog(this.delegate.fork());
        }

        @Override
        public PositionalRandomFactory forkPositional() {
            return this.delegate.forkPositional();
        }

        @Override
        public void setSeed(long seed) {
            this.delegate.setSeed(seed);
        }

        @Override
        public int nextInt() {
            return this.delegate.nextInt();
        }

        @Override
        public int nextInt(int upper) {
            this.bounds.add(upper);
            return this.delegate.nextInt(upper);
        }

        @Override
        public long nextLong() {
            return this.delegate.nextLong();
        }

        @Override
        public boolean nextBoolean() {
            return this.delegate.nextBoolean();
        }

        @Override
        public float nextFloat() {
            return this.delegate.nextFloat();
        }

        @Override
        public double nextDouble() {
            return this.delegate.nextDouble();
        }

        @Override
        public double nextGaussian() {
            return this.delegate.nextGaussian();
        }
    }

    // ------------------------------------------------------------------
    // Helpers (the PlayNicelyGriefingGateTests / SightStepParityTests idiom)
    // ------------------------------------------------------------------

    /**
     * The structure-relative position of an absolute block. Not {@code GameTestHelper.relativePos}: the framework's
     * method mirrors both horizontal axes under {@code Rotation.NONE} (PlayNicelyGriefingGateTests.rel).
     */
    private static BlockPos rel(GameTestHelper helper, BlockPos absolute) {
        return absolute.subtract(helper.absolutePos(BlockPos.ZERO));
    }

    /** orig Stinky.java:593 — the scan origin {@code ((int) posX, (int) posY + 1, (int) posZ)}, the truncating casts kept. */
    private static BlockPos scanOrigin(Entity entity) {
        return new BlockPos((int) entity.getX(), (int) entity.getY() + 1, (int) entity.getZ());
    }

    /** Frozen at its spot: goals stripped, noAi, persistence set (the PlayNicelyGriefingGateTests idiom). */
    private static <E extends Mob> E spawnFrozen(GameTestHelper helper, EntityType<E> type, BlockPos pos) {
        E mob = helper.spawnWithNoFreeWill(type, pos);
        mob.setNoAi(true);
        mob.setPersistenceRequired();
        return mob;
    }

    /** Frozen prey with 1000 HP, so nothing incidental kills it (SightStepParityTests). */
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
        writeField(entity, Entity.class, "random", forced);
    }

    /** The flyer's private feet ray {@code canSeeTarget(double, double, double)} aimed at the block's corner, as the pick aims it (orig Stinky.java:640 / Spyro.java:647). */
    private static boolean canSee(Mob flyer, BlockPos block) {
        String where = flyer.getClass().getSimpleName() + ".canSeeTarget";
        try {
            Method method = flyer.getClass().getDeclaredMethod("canSeeTarget", double.class, double.class, double.class);
            method.setAccessible(true);
            return (Boolean) method.invoke(flyer, (double) block.getX(), (double) block.getY(), (double) block.getZ());
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

    private static Object readField(Object owner, Class<?> declaring, String name) {
        try {
            Field field = declaring.getDeclaredField(name);
            field.setAccessible(true);
            return field.get(owner);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("cannot read " + declaring.getSimpleName() + "." + name, exception);
        }
    }

    private static int readInt(EntityStinky stinky, String name) {
        return (Integer) readField(stinky, EntityStinky.class, name);
    }

    private static void writeField(Object owner, Class<?> declaring, String name, Object value) {
        try {
            Field field = declaring.getDeclaredField(name);
            field.setAccessible(true);
            field.set(owner, value);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("cannot write " + declaring.getSimpleName() + "." + name, exception);
        }
    }

    private static String name(Block block) {
        return BuiltInRegistries.BLOCK.getKey(block).toString();
    }
}
