package danger.orespawn.gametest;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.util.MyUtils;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * ENT-S-108: the nine hunters whose 1.7.10 target search scanned every
 * {@code EntityLivingBase} in a box around the hunter and took the first, in
 * GenericTargetSorter order, that its own exclusion chain let through —
 * CaveFisher (10/3/10, orig CaveFisher.java:234), DungeonBeast (16/3/16, :253),
 * EmperorScorpion (24/6/24, :507), HerculesBeetle (16/6/16, :420), Nastysaurus
 * (32/8/32, :282), SpitBug (12/7/12, :374), TRex (20/6/20, :254), TrooperBug
 * (12/7/12, :514), Urchin (16/3/16, :276) — where the port had run a vanilla
 * {@code NearestAttackableTargetGoal} on {@code Player.class} (the CaveFisher:
 * Player and Animal goals; the Urchin: {@code getNearestPlayer}). The owner ruled
 * parity: each hunter carries the original scan again as a private
 * {@code findSomethingToAttack()} over a private
 * {@code isSuitableTarget(LivingEntity)} in the original's check order, on the
 * original's tick cadence, its pick handed to the class's melee goal through the
 * target slot.
 *
 * <p>A {@link GameTestGenerator} over the nine-row table in {@link #hunters()},
 * five probes per hunter, each reaching the scan by reflection on a frozen
 * hunter (goals stripped, noAi) whose prey stands in clear line of sight, with
 * PlayNicely off as a precondition:</p>
 * <ol>
 *   <li><b>living_prey_selected</b> — a non-player living thing 8 blocks off
 *       (a pig; for the CaveFisher a snow golem, which is neither of the two
 *       classes its old goals scanned) is the pick, and no
 *       {@code NearestAttackableTargetGoal} remains on the target selector.</li>
 *   <li><b>ignore_list_species_not_selected</b> — the hunter's ENT-S-106 list
 *       species (its IgnoreScreenParityTests row) on the prey spot is not picked;
 *       a pig on the same spot is.</li>
 *   <li><b>creative_player_not_selected</b> — a creative mock player on the prey
 *       spot is not picked (every one of the nine chains refuses
 *       {@code isCreativeMode}, the port's {@code Abilities.instabuild}); the same
 *       player set to survival is.</li>
 *   <li><b>box_pinned</b> — a pig just outside the original box on each of +x,
 *       +y and +z is not picked and one just inside each of those edges is, so
 *       the port's box is the original's on every axis (the hunter stands in the
 *       template's south-west corner so the widest box, the Nastysaurus' 32,
 *       keeps its +x/+z probes inside the 48x16x48 empty_large and its +y probe
 *       under the barrier ceiling at rel 17).</li>
 *   <li><b>excluded_species_not_selected</b> — one species of the hunter's own
 *       original exclusion chain, not on the shared list, on the prey spot is not
 *       picked; a pig on the same spot is.</li>
 * </ol>
 *
 * <p>Synchronous, no global state touched. Own batch (TEST-003). Geometry as in
 * IgnoreScreenParityTests: hunter at (20,1,24), prey at (28,1,24) on the floor of
 * empty_large, the mock player of the KrakenTargetingParityTests kind (the
 * game-test server defaults it to CREATIVE, so the mode is always set
 * explicitly). Spawns are frozen and discarded in a finally; the mock player is
 * removed from the player list.</p>
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class TargetScanParityTests {

    private static final String BATCH = "targetScanParity";
    private static final String TEST_PREFIX = "targetscanparitytests.";
    /** Generated TestFunctions bypass the holder's template prefixing, so the template is named in full. */
    private static final String EMPTY_LARGE = OreSpawnMod.MOD_ID + ":empty_large";
    private static final int TIMEOUT_TICKS = 100;

    /** Hunter and prey 8 blocks apart on the floor, clear line of sight (the ENT-S-106 geometry). */
    private static final BlockPos HUNTER_POS = new BlockPos(20, 1, 24);
    private static final BlockPos PREY_POS = new BlockPos(28, 1, 24);
    private static final Vec3 PLAYER_POS = new Vec3(28.5, 1.0, 24.5);
    /** box_pinned: the hunter in the south-west corner, so every +x / +z probe stays inside the template. */
    private static final BlockPos CORNER_HUNTER_POS = new BlockPos(4, 1, 4);
    /** A probe pig's centre sits this far past the box edge (outside) or short of it (inside); a pig is 0.9 wide. */
    private static final double OUTSIDE_MARGIN = 1.0;
    private static final double INSIDE_MARGIN = 0.5;

    private static final Supplier<EntityType<? extends Mob>> PIG = () -> EntityType.PIG;
    private static final Supplier<EntityType<? extends Mob>> SNOW_GOLEM = () -> EntityType.SNOW_GOLEM;
    private static final Supplier<EntityType<? extends Mob>> ZOMBIE = () -> EntityType.ZOMBIE;
    private static final Supplier<EntityType<? extends Mob>> CREEPER = () -> EntityType.CREEPER;
    private static final String PIG_WHY = "a vanilla pig";

    /** One hunter: its orig scan, box and chain, and the species each probe uses. */
    private static final class Hunter {
        final int index;
        final String tag;
        final Supplier<? extends EntityType<? extends Mob>> type;
        final String origFile;
        final String scanLine;
        final double boxX;
        final double boxY;
        final double boxZ;
        final String filterLines;
        final String creativeLines;
        final String oldPath;
        Supplier<? extends EntityType<? extends Mob>> prey = PIG;
        String preyWhy = PIG_WHY;
        Supplier<? extends EntityType<? extends Mob>> ignored;
        String ignoredWhy;
        String ignoreLines;
        Supplier<? extends EntityType<? extends Mob>> excluded;
        String excludedWhy;
        String excludedLines;

        Hunter(int index, String tag, Supplier<? extends EntityType<? extends Mob>> type, String origFile,
               String scanLine, double boxX, double boxY, double boxZ, String filterLines, String creativeLines,
               String oldPath) {
            this.index = index;
            this.tag = tag;
            this.type = type;
            this.origFile = origFile;
            this.scanLine = scanLine;
            this.boxX = boxX;
            this.boxY = boxY;
            this.boxZ = boxZ;
            this.filterLines = filterLines;
            this.creativeLines = creativeLines;
            this.oldPath = oldPath;
        }

        Hunter prey(Supplier<? extends EntityType<? extends Mob>> type, String why) {
            this.prey = type;
            this.preyWhy = why;
            return this;
        }

        Hunter ignored(Supplier<? extends EntityType<? extends Mob>> type, String why, String lines) {
            this.ignored = type;
            this.ignoredWhy = why;
            this.ignoreLines = lines;
            return this;
        }

        Hunter excluded(Supplier<? extends EntityType<? extends Mob>> type, String why, String lines) {
            this.excluded = type;
            this.excludedWhy = why;
            this.excludedLines = lines;
            return this;
        }

        String orig(String lines) {
            return "orig " + this.origFile + lines;
        }

        String box() {
            return (int) this.boxX + "/" + (int) this.boxY + "/" + (int) this.boxZ;
        }
    }

    // ------------------------------------------------------------------
    // The nine hunters, in orig file order
    // ------------------------------------------------------------------

    private static List<Hunter> hunters() {
        List<Hunter> hunters = new ArrayList<>();
        hunters.add(new Hunter(1, "cavefisher", ModEntities.CAVE_FISHER, "CaveFisher.java", ":234", 10.0, 3.0, 10.0,
                ":193-228", ":221-226", "NearestAttackableTargetGoal pair on Player.class and Animal.class")
                .prey(SNOW_GOLEM, "a vanilla snow golem (a golem: neither a Player nor an Animal, the port's two old goal classes, and no EntityMob for orig :218)")
                .ignored(ModEntities.ENTITY_CRICKET, "a cricket (orig MyUtils.java:136, an Animal)", ":203-205")
                .excluded(ZOMBIE, "a vanilla Zombie (an EntityMob)", ":218-220"));
        hunters.add(new Hunter(2, "dungeonbeast", ModEntities.DUNGEON_BEAST, "DungeonBeast.java", ":253", 16.0, 3.0, 16.0,
                ":200-247", ":240-245", "NearestAttackableTargetGoal on Player.class")
                .ignored(ModEntities.COCKATEIL, "a Cockateil (orig MyUtils.java:139)", ":210-212")
                .excluded(ModEntities.ENTITY_RAT, "a Rat", ":216-218"));
        hunters.add(new Hunter(3, "emperorscorpion", ModEntities.ENTITY_EMPEROR_SCORPION, "EmperorScorpion.java", ":507", 24.0, 6.0, 24.0,
                ":460-501", ":494-499", "NearestAttackableTargetGoal on Player.class")
                .ignored(ModEntities.ENTITY_DRAGONFLY, "a dragonfly (orig MyUtils.java:130)", ":473-475")
                .excluded(ModEntities.ENTITY_SCORPION, "a Scorpion", ":488-490"));
        hunters.add(new Hunter(4, "herculesbeetle", ModEntities.ENTITY_HERCULES_BEETLE, "HerculesBeetle.java", ":420", 16.0, 6.0, 16.0,
                ":385-414", ":407-412", "NearestAttackableTargetGoal on Player.class")
                .ignored(ModEntities.ENTITY_BUTTERFLY, "a butterfly (orig MyUtils.java:124)", ":395-397")
                .excluded(CREEPER, "a vanilla Creeper", ":401-403"));
        hunters.add(new Hunter(5, "nastysaurus", ModEntities.NASTYSAURUS, "Nastysaurus.java", ":282", 32.0, 8.0, 32.0,
                ":246-276", ":271-274", "NearestAttackableTargetGoal on Player.class")
                .ignored(ModEntities.ENTITY_TERMITE, "a Termite (orig MyUtils.java:142)", ":256-258")
                .excluded(ModEntities.CRYOLOPHOSAURUS, "a Cryolophosaurus", ":262-264"));
        hunters.add(new Hunter(6, "spitbug", ModEntities.ENTITY_SPIT_BUG, "SpitBug.java", ":374", 12.0, 7.0, 12.0,
                ":324-368", ":361-366", "NearestAttackableTargetGoal on Player.class")
                .ignored(ModEntities.GHOST, "a Ghost (orig MyUtils.java:145)", ":334-336")
                .excluded(ModEntities.ENTITY_TROOPER_BUG, "a TrooperBug", ":358-360"));
        hunters.add(new Hunter(7, "trex", ModEntities.TREX, "TRex.java", ":254", 20.0, 6.0, 20.0,
                ":216-248", ":241-246", "NearestAttackableTargetGoal on Player.class")
                .ignored(ModEntities.GHOST_SKELLY, "a GhostSkelly (orig MyUtils.java:148)", ":226-228")
                .excluded(ModEntities.VELOCITY_RAPTOR, "a VelocityRaptor", ":238-240"));
        hunters.add(new Hunter(8, "trooperbug", ModEntities.ENTITY_TROOPER_BUG, "TrooperBug.java", ":514", 12.0, 7.0, 12.0,
                ":464-508", ":501-506", "NearestAttackableTargetGoal on Player.class")
                .ignored(ModEntities.ENTITY_MOSQUITO, "a mosquito (orig MyUtils.java:127)", ":474-476")
                .excluded(ModEntities.ENTITY_SPIT_BUG, "a SpitBug", ":498-500"));
        hunters.add(new Hunter(9, "urchin", ModEntities.URCHIN, "Urchin.java", ":276", 16.0, 3.0, 16.0,
                ":220-270", ":263-268", "players-only getNearestPlayer(16) scan")
                .ignored(ModEntities.ENTITY_BUTTERFLY, "a butterfly (orig MyUtils.java:124)", ":230-232")
                .excluded(ModEntities.CRYSTAL_COW, "a CrystalCow", ":245-247"));
        return hunters;
    }

    /** Five probes per hunter: 45 TestFunctions in the {@code targetScanParity} batch. */
    @GameTestGenerator
    public Collection<TestFunction> targetScanSites() {
        List<TestFunction> functions = new ArrayList<>();
        for (Hunter hunter : hunters()) {
            add(functions, hunter, "living_prey_selected", helper -> assertLivingPreySelected(helper, hunter));
            add(functions, hunter, "ignore_list_species_not_selected", helper -> assertIgnoredSpeciesNotSelected(helper, hunter));
            add(functions, hunter, "creative_player_not_selected", helper -> assertCreativePlayerNotSelected(helper, hunter));
            add(functions, hunter, "box_pinned", helper -> assertBoxPinned(helper, hunter));
            add(functions, hunter, "excluded_species_not_selected", helper -> assertExcludedSpeciesNotSelected(helper, hunter));
        }
        return functions;
    }

    private static void add(List<TestFunction> functions, Hunter hunter, String probe, Consumer<GameTestHelper> body) {
        String name = TEST_PREFIX + String.format("s108_%02d_%s_%s", hunter.index, hunter.tag, probe);
        functions.add(new TestFunction(BATCH, name, EMPTY_LARGE, Rotation.NONE, TIMEOUT_TICKS, 0L, true, helper -> {
            body.accept(helper);
            helper.succeed();
        }));
    }

    // ------------------------------------------------------------------
    // Probes
    // ------------------------------------------------------------------

    /**
     * (a) A non-player living thing inside the box, in sight, is the pick — the old
     * players-only path never saw it — and the old goal is gone from the target selector.
     */
    private static void assertLivingPreySelected(GameTestHelper helper, Hunter site) {
        Mob hunter = null;
        Mob prey = null;
        try {
            hunter = spawnWithGoals(helper, site.type.get(), HUNTER_POS);
            String name = hunter.getClass().getSimpleName();
            helper.assertTrue(nearestAttackableTargetGoals(hunter).isEmpty(), name + " must carry no"
                    + " NearestAttackableTargetGoal on its target selector any more: " + site.orig(site.scanLine)
                    + " searches by an EntityLivingBase box scan, and the port's " + site.oldPath + " gave way to it"
                    + " (ENT-S-108)");
            assertPlayNicelyOff(helper, site);
            prey = spawnFrozen(helper, site.prey.get(), PREY_POS);
            helper.assertTrue(!MyUtils.isIgnoreable(prey), "precondition: " + site.preyWhy + " must not be on the"
                    + " shared ignore list (orig MyUtils.java:117-152) (ENT-S-108 test setup)");
            assertSeen(helper, hunter, prey, site.preyWhy);
            LivingEntity pick = scan(hunter);
            helper.assertTrue(pick == prey, name + ".findSomethingToAttack (" + site.orig(site.scanLine) + "): "
                    + site.preyWhy + " 8 blocks off, inside the " + site.box() + " box and in sight, must be the pick —"
                    + " 1.7.10 scanned EntityLivingBase.class, the port's " + site.oldPath + " never saw it"
                    + " (ENT-S-108); got " + describe(pick));
        } finally {
            if (prey != null) prey.discard();
            if (hunter != null) hunter.discard();
        }
    }

    /** (b) The hunter's ENT-S-106 list species on the prey spot is not picked; a pig on the same spot is. */
    private static void assertIgnoredSpeciesNotSelected(GameTestHelper helper, Hunter site) {
        Mob hunter = null;
        Mob species = null;
        Mob control = null;
        try {
            hunter = spawnFrozen(helper, site.type.get(), HUNTER_POS);
            String name = hunter.getClass().getSimpleName();
            assertPlayNicelyOff(helper, site);
            species = spawnFrozen(helper, site.ignored.get(), PREY_POS);
            helper.assertTrue(MyUtils.isIgnoreable(species), "precondition: " + site.ignoredWhy
                    + " must be on the shared list (orig MyUtils.java:117-152) (ENT-S-108 test setup)");
            assertSeen(helper, hunter, species, site.ignoredWhy);
            LivingEntity pick = scan(hunter);
            helper.assertTrue(pick == null, name + ".findSomethingToAttack (" + site.orig(site.scanLine) + "): "
                    + site.ignoredWhy + " alone in the " + site.box() + " box must leave the scan empty — the shared"
                    + " ignore screen (" + site.orig(site.ignoreLines) + ") refuses it (ENT-S-108); got " + describe(pick));
            species.discard();
            species = null;
            control = spawnFrozen(helper, site.prey.get(), PREY_POS);
            assertSeen(helper, hunter, control, site.preyWhy);
            pick = scan(hunter);
            helper.assertTrue(pick == control, "control: " + name + ".findSomethingToAttack must pick " + site.preyWhy
                    + " on the same spot, so " + site.ignoredWhy + " was refused by the screen and not by geometry"
                    + " or sight (ENT-S-108); got " + describe(pick));
        } finally {
            if (control != null) control.discard();
            if (species != null) species.discard();
            if (hunter != null) hunter.discard();
        }
    }

    /** (c) A creative mock player on the prey spot is not picked; the same player in survival is. */
    private static void assertCreativePlayerNotSelected(GameTestHelper helper, Hunter site) {
        Mob hunter = null;
        ServerPlayer player = null;
        try {
            hunter = spawnFrozen(helper, site.type.get(), HUNTER_POS);
            String name = hunter.getClass().getSimpleName();
            assertPlayNicelyOff(helper, site);
            player = playerAt(helper, GameType.CREATIVE, helper.absoluteVec(PLAYER_POS));
            helper.assertTrue(player.getAbilities().instabuild, "precondition: a creative player has instabuild set"
                    + " (ENT-S-108 test setup)");
            assertSeen(helper, hunter, player, "the mock player");
            LivingEntity pick = scan(hunter);
            helper.assertTrue(pick == null, name + ".findSomethingToAttack (" + site.orig(site.scanLine) + "): a"
                    + " creative player alone in the " + site.box() + " box must leave the scan empty — the player"
                    + " branch (" + site.orig(site.creativeLines) + ", capabilities.isCreativeMode → Abilities.instabuild)"
                    + " refuses it (ENT-S-108); got " + describe(pick));
            player.setGameMode(GameType.SURVIVAL);
            helper.assertTrue(!player.getAbilities().instabuild, "precondition: the same player set to survival has"
                    + " instabuild clear (ENT-S-108 test setup)");
            pick = scan(hunter);
            helper.assertTrue(pick == player, "control: " + name + ".findSomethingToAttack must pick the same player"
                    + " once in survival on the same spot, so creative mode alone refused it (ENT-S-108); got "
                    + describe(pick));
        } finally {
            removePlayer(helper, player);
            if (hunter != null) hunter.discard();
        }
    }

    /**
     * (d) The box is the original's on every axis: a pig whose box lies just past the
     * +x, +y or +z edge of {@code getBoundingBox().inflate(box)} is not picked, one just
     * short of that edge is.
     */
    private static void assertBoxPinned(GameTestHelper helper, Hunter site) {
        Mob hunter = null;
        Mob pig = null;
        try {
            hunter = spawnFrozen(helper, site.type.get(), CORNER_HUNTER_POS);
            String name = hunter.getClass().getSimpleName();
            assertPlayNicelyOff(helper, site);
            AABB box = hunter.getBoundingBox().inflate(site.boxX, site.boxY, site.boxZ);
            double x = hunter.getX();
            double y = hunter.getY();
            double z = hunter.getZ();
            Probe[] probes = {
                    new Probe("+x", false, new Vec3(box.maxX + OUTSIDE_MARGIN, y, z)),
                    new Probe("+x", true, new Vec3(box.maxX - INSIDE_MARGIN, y, z)),
                    new Probe("+y", false, new Vec3(x, box.maxY + OUTSIDE_MARGIN, z)),
                    new Probe("+y", true, new Vec3(x, box.maxY - INSIDE_MARGIN, z)),
                    new Probe("+z", false, new Vec3(x, y, box.maxZ + OUTSIDE_MARGIN)),
                    new Probe("+z", true, new Vec3(x, y, box.maxZ - INSIDE_MARGIN)),
            };
            for (Probe probe : probes) {
                pig = spawnFrozen(helper, PIG.get(), PREY_POS);
                pig.moveTo(probe.at.x, probe.at.y, probe.at.z, 0.0f, 0.0f);
                String where = PIG_WHY + " " + (probe.inside ? "just inside" : "just past") + " the " + probe.axis
                        + " edge of the " + site.box() + " box (" + site.orig(site.scanLine) + ")";
                helper.assertTrue(box.intersects(pig.getBoundingBox()) == probe.inside, "precondition: " + where
                        + " must " + (probe.inside ? "" : "not ") + "meet the box (ENT-S-108 test geometry)");
                assertSeen(helper, hunter, pig, where);
                LivingEntity pick = scan(hunter);
                if (probe.inside) {
                    helper.assertTrue(pick == pig, name + ".findSomethingToAttack: " + where + ", in sight, must be"
                            + " the pick — the port's box must reach as far as the original's (ENT-S-108); got "
                            + describe(pick));
                } else {
                    helper.assertTrue(pick == null, name + ".findSomethingToAttack: " + where + ", in sight, must"
                            + " leave the scan empty — the port's box must reach no further than the original's"
                            + " (ENT-S-108); got " + describe(pick));
                }
                pig.discard();
                pig = null;
            }
        } finally {
            if (pig != null) pig.discard();
            if (hunter != null) hunter.discard();
        }
    }

    /** (e) A species of the hunter's own orig exclusion chain on the prey spot is not picked; a pig on the same spot is. */
    private static void assertExcludedSpeciesNotSelected(GameTestHelper helper, Hunter site) {
        Mob hunter = null;
        Mob species = null;
        Mob control = null;
        try {
            hunter = spawnFrozen(helper, site.type.get(), HUNTER_POS);
            String name = hunter.getClass().getSimpleName();
            assertPlayNicelyOff(helper, site);
            species = spawnFrozen(helper, site.excluded.get(), PREY_POS);
            helper.assertTrue(!MyUtils.isIgnoreable(species), "precondition: " + site.excludedWhy + " must not be on"
                    + " the shared ignore list, so only the hunter's own chain can refuse it (ENT-S-108 test setup)");
            assertSeen(helper, hunter, species, site.excludedWhy);
            LivingEntity pick = scan(hunter);
            helper.assertTrue(pick == null, name + ".findSomethingToAttack (" + site.orig(site.scanLine) + "): "
                    + site.excludedWhy + " alone in the " + site.box() + " box must leave the scan empty — the"
                    + " hunter's own chain (" + site.orig(site.excludedLines) + ", inside " + site.orig(site.filterLines)
                    + ") refuses it (ENT-S-108); got " + describe(pick));
            species.discard();
            species = null;
            control = spawnFrozen(helper, site.prey.get(), PREY_POS);
            assertSeen(helper, hunter, control, site.preyWhy);
            pick = scan(hunter);
            helper.assertTrue(pick == control, "control: " + name + ".findSomethingToAttack must pick " + site.preyWhy
                    + " on the same spot, so " + site.excludedWhy + " was refused by the chain and not by geometry"
                    + " or sight (ENT-S-108); got " + describe(pick));
        } finally {
            if (control != null) control.discard();
            if (species != null) species.discard();
            if (hunter != null) hunter.discard();
        }
    }

    /** One box_pinned placement: which edge, which side of it, and the pig's centre. */
    private record Probe(String axis, boolean inside, Vec3 at) { }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private static void assertPlayNicelyOff(GameTestHelper helper, Hunter site) {
        helper.assertTrue(!OreSpawnConfig.PLAY_NICELY.get(), "precondition: PlayNicely must be off — "
                + site.orig(site.scanLine) + "'s scan returns nothing under it (ENT-S-108 test setup)");
    }

    private static void assertSeen(GameTestHelper helper, Mob hunter, LivingEntity prey, String why) {
        helper.assertTrue(hunter.hasLineOfSight(prey), "precondition: " + hunter.getClass().getSimpleName()
                + " (eye " + String.format("%.2f", hunter.getEyeHeight()) + " above its feet) must see " + why
                + " inside the barrier shell (ENT-S-108 test geometry)");
    }

    /** Frozen: goals stripped, noAi, persistence set (the IgnoreListParityTests idiom). */
    private static Mob spawnFrozen(GameTestHelper helper, EntityType<? extends Mob> type, BlockPos pos) {
        Mob mob = helper.spawnWithNoFreeWill(type, pos);
        mob.setNoAi(true);
        mob.setPersistenceRequired();
        return mob;
    }

    /** With its registered goals (the target selector is under test) but no AI, so nothing runs. */
    private static Mob spawnWithGoals(GameTestHelper helper, EntityType<? extends Mob> type, BlockPos pos) {
        Mob mob = helper.spawn(type, pos);
        mob.setNoAi(true);
        mob.setPersistenceRequired();
        return mob;
    }

    private static List<NearestAttackableTargetGoal<?>> nearestAttackableTargetGoals(Mob hunter) {
        List<NearestAttackableTargetGoal<?>> goals = new ArrayList<>();
        for (WrappedGoal wrapped : hunter.targetSelector.getAvailableGoals()) {
            if (wrapped.getGoal() instanceof NearestAttackableTargetGoal<?> goal) {
                goals.add(goal);
            }
        }
        return goals;
    }

    /**
     * A mock ServerPlayer in the level with an explicit game mode (the game-test server
     * defaults to CREATIVE, GameTestServer.java:85). Health is raised so nothing incidental
     * can kill it. Deprecated mock-player factory tolerated the way CreativeMappingParityTests
     * and KrakenTargetingParityTests do.
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

    /** The hunter's private no-arg {@code findSomethingToAttack()} — the port's shape of the orig scan. */
    private static LivingEntity scan(Mob hunter) {
        String name = hunter.getClass().getSimpleName() + ".findSomethingToAttack";
        try {
            Method method = hunter.getClass().getDeclaredMethod("findSomethingToAttack");
            method.setAccessible(true);
            return (LivingEntity) method.invoke(hunter);
        } catch (InvocationTargetException exception) {
            throw new IllegalStateException(name + " threw", exception.getCause());
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("cannot invoke " + name, exception);
        }
    }

    private static String describe(LivingEntity pick) {
        return pick == null ? "null" : pick.getClass().getSimpleName() + "#" + pick.getId();
    }
}
