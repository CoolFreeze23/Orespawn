package danger.orespawn.gametest;

import com.mojang.authlib.GameProfile;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityBrutalfly;
import danger.orespawn.entity.EntityDragonfly;
import danger.orespawn.entity.Mothra;
import danger.orespawn.entity.ai.DragonflyHuntGoal;
import danger.orespawn.entity.ai.MyEntityAINearestAttackableTargetGoal;
import io.netty.channel.embedded.EmbeddedChannel;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * ENT-S-139 — targeting ledger batch T4 (wave 3): tie-breaks and sorters. 1.7.10 sorted every custom scan with
 * {@code GenericTargetSorter} (orig GenericTargetSorter.java:18-35: the candidate's distance² from the hunter, halved for an
 * {@code EntityCreeper} (:21-23), divided by its silhouette {@code height * width} when that exceeds 1 (:24-26) — the first
 * operand's terms :20-26, the second's :27-33 — ties 0 (the three-way :34) so {@code Collections.sort}'s stability kept the
 * list's order) and took the first candidate the filter accepted; the port's
 * seventeen TF-035 remainders (Cephadrome, Cryolophosaurus, Dragonfly, Fairy, Frog, Gamma Metroid, Kyuubi, Leon's custom scan,
 * Lizard, Purple Power, Rat, Robot1, Spider Driver's two sorts, Stinky, Terrible Terror, Prince Teen, Triffid) sorted by plain
 * distance ({@code Comparator.comparingDouble(this::distanceToSqr)}) — now {@code new GenericTargetSorter(this)} through
 * {@code TargetSelection.firstMatch}, whose index tiebreak reproduces the stable sort's tie order (the ENT-S-108 shape). The
 * fourteen sites that already sorted with the sorter through the ENT-S-108 (nine) and ENT-S-135 (five) rebuilds are pinned here
 * for the first time; the Brutalfly / Mothra strafe finders' {@code <=} (the last equidistant player wins — orig
 * {@code World.func_72857_a}, bytecode-verified under ENT-S-105) is pinned; and the companions' own task sorts with the port of
 * orig {@code MyEntityAINearestAttackableTargetSorter} (:21-31 — a creeper's distance² halved, NO silhouette term) where it sorted
 * plain, the Valentine subclass keeping {@code MyValentineTargetSorter}'s plain distance (unobservable on a Player / Boyfriend scan,
 * unpinned).
 *
 * <p>A {@link GameTestGenerator} over {@link #rows()}, {@code targetsorterparitytests.s139_NN_<species>_<row>}, in the ledger's
 * T4 order. Per site up to three geometries through the site's own scan by reflection (the KrakenTargetingParityTests /
 * ScanSetParityTests shape — the private {@code findSomethingToAttack} and its kin, the Dragonfly goal's {@code findPrey}, the
 * companions' IMob goal asked {@code canUse()} with its pick read back), the hunter frozen (no AI, on the ground) at rel (20,1,24)
 * of the empty_large floor, the two candidates frozen on the same floor along −x (the nearer) and +x (the farther), line of sight
 * and the feet-to-feet distances asserted: {@code creeper_outranks_nearer} — a non-creeper nearer than a creeper (a pig where the
 * site admits one, else a Zombie: 6 against 7), the creeper the pick under orig's halving where plain distance took the
 * non-creeper; {@code big_silhouette_outranks_nearer} — a sub-1 silhouette nearer than an over-1 silhouette (a Silverfish 0.12
 * against a Ravager 4.29, a Chicken 0.28 against an Iron Golem 3.78 where the site refuses monsters, the Lizard's Chicken against
 * a Spider 1.26, the Frog's Cricket against a Mothra 10 — an EntityButterfly in both trees, the Dragonfly's Mosquito against a
 * Horse 2.23), the big one the pick under orig's division where plain distance took the small one;
 * {@code control_nearer_unweighted_wins} — two of one unweighted kind (pigs where admitted), the nearer the pick under both orders.
 * Every row recomputes orig's weights from the live entities (GenericTargetSorter.java:18-35 transcribed in
 * {@link #genericWeight}) and asserts, before the pick, that plain distance and orig's order disagree (or agree, in a control) and
 * — in the creeper and big-silhouette rows — that the site's own filter admits the NEARER candidate, by reflection
 * ({@link #assertAdmits}: {@code isSuitableTarget}, the Frog's {@code isInsectTarget}, the Dragonfly goal's {@code isPrey} beside
 * the asserted sight, the companions' {@code canAttack(nearer, targetConditions)} — the ScanSetParityTests.filter idiom), so a
 * row that stops discriminating, by its geometry or by a ladder that refuses the nearer candidate, fails on its precondition
 * instead of passing without discriminating. The Irukandji (players only): two STANDING survival players 5.0 west and 5.1 east
 * — the nearer is the pick, both weighed 1.08 (a control: 1.7.10's player was 0.6 × 1.8 in every pose but sleeping — sneaking set
 * a flag and the client camera drop, never {@code setSize} — so orig's sorter divided a sneaking player's distance² by 1.08 as a
 * standing one's; the port's pose-sized crouch box, 0.6 × 1.5 = 0.9 undivided, is a port-only divergence, ENT-S-140, not pinned
 * as 1.7.10's). The Brutalfly and Mothra strafe finders: two survival players at equal distance², the LAST
 * in the box list's order the pick (orig's {@code <=} replace, ENT-S-105 / ENT-S-135). The Spider Driver's mount pick: the nearer
 * of two unridden Spider Robots (every robot weighs the same, the sorter reduces to nearest — a control). The Boyfriend / Girlfriend
 * IMob task: a Zombie nearer than a Creeper — the Creeper the pick (orig MyEntityAINearestAttackableTargetSorter.java:21-31's
 * halving); a Silverfish nearer than a Ravager — the SILVERFISH the pick (no silhouette term: GenericTargetSorter would have taken
 * the Ravager, asserted); two Zombies — the nearer. Survival players are plain ServerPlayers (the framework mock's {@code isCreative()}
 * is hardcoded true). PlayNicely set false and restored in a finally on every path; players removed and every spawn discarded
 * there; the difficulty asserted, never flipped; no hit is pinned. Own batch (TEST-003).</p>
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class TargetSorterParityTests {

    private static final String BATCH = "targetSorterParity";
    private static final String TEST_PREFIX = "targetsorterparitytests.";
    /** Generated TestFunctions bypass the holder's template prefixing, so the template is named in full (PlayNicelyGateParityTests). */
    private static final String EMPTY_LARGE = OreSpawnMod.MOD_ID + ":empty_large";
    private static final int TIMEOUT_TICKS = 100;
    private static final String FINDING = "ENT-S-139";

    /** The hunter on the template floor (the sibling batches' spot): its feet at rel (20.5, 1.0, 24.5). */
    private static final BlockPos HUNTER_POS = new BlockPos(20, 1, 24);
    private static final Vec3 HUNTER_FEET = Vec3.atBottomCenterOf(HUNTER_POS);
    /** Candidate health, high enough that nothing incidental kills it. */
    private static final float PREY_HEALTH = 1000.0f;
    /** Tolerance on the geometry preconditions (a Vec3 spawn lands where it is told). */
    private static final double GEOMETRY_TOLERANCE = 1.0e-6;
    /** The strafe-tie players' offset along ±x: distance² 25 on both sides, exactly. */
    private static final double TIE_OFFSET = 5.0;

    // ------------------------------------------------------------------
    // Candidate kinds
    // ------------------------------------------------------------------

    private static final Supplier<EntityType<? extends Mob>> ZOMBIE = () -> EntityType.ZOMBIE;
    private static final Supplier<EntityType<? extends Mob>> CREEPER = () -> EntityType.CREEPER;
    private static final Supplier<EntityType<? extends Mob>> PIG = () -> EntityType.PIG;
    private static final Supplier<EntityType<? extends Mob>> SILVERFISH = () -> EntityType.SILVERFISH;
    private static final Supplier<EntityType<? extends Mob>> RAVAGER = () -> EntityType.RAVAGER;
    private static final Supplier<EntityType<? extends Mob>> CHICKEN = () -> EntityType.CHICKEN;
    private static final Supplier<EntityType<? extends Mob>> IRON_GOLEM = () -> EntityType.IRON_GOLEM;
    private static final Supplier<EntityType<? extends Mob>> SPIDER = () -> EntityType.SPIDER;
    private static final Supplier<EntityType<? extends Mob>> HORSE = () -> EntityType.HORSE;
    private static final Supplier<EntityType<? extends Mob>> CRICKET = () -> ModEntities.ENTITY_CRICKET.get();
    private static final Supplier<EntityType<? extends Mob>> MOSQUITO = () -> ModEntities.ENTITY_MOSQUITO.get();
    private static final Supplier<EntityType<? extends Mob>> MOTHRA = () -> ModEntities.MOTHRA.get();
    private static final Supplier<EntityType<? extends Mob>> SPIDER_ROBOT = () -> ModEntities.SPIDER_ROBOT.get();

    /** A nearer candidate along −x and a farther one along +x, feet to feet on the hunter's floor. */
    private record Pair(Supplier<EntityType<? extends Mob>> nearer, double nearerDist,
            Supplier<EntityType<? extends Mob>> farther, double fartherDist) {
    }

    private static Pair pair(Supplier<EntityType<? extends Mob>> nearer, double nearerDist,
            Supplier<EntityType<? extends Mob>> farther, double fartherDist) {
        return new Pair(nearer, nearerDist, farther, fartherDist);
    }

    /** How the site's pick is asked for. */
    private enum Scan {
        /** The hunter's private no-arg scan, by name, declared on the hunter's own class. */
        PRIVATE_METHOD,
        /** The Dragonfly: {@code EntityDragonfly.huntGoal}'s private {@code findPrey}. */
        DRAGONFLY_GOAL,
        /** The Boyfriend / Girlfriend: the IMob goal off the target selector, {@code canUse()} then its pick read back. */
        COMPANION_GOAL
    }

    /** Which orig sorter the site's scan carried — the weights a row recomputes. */
    private enum Sorter {
        /** orig GenericTargetSorter.java:18-35 — creeper halved, silhouette over 1 divides (each operand's terms: :20-26 / :27-33). */
        GENERIC,
        /** orig MyEntityAINearestAttackableTargetSorter.java:21-31 — creeper halved, no silhouette term. */
        COMPANION
    }

    /**
     * One site: the hunter, how its pick is asked for, the filter its scan walks (asserted on the nearer candidate before a weighted
     * pick, {@link #assertAdmits}; null for the companions, whose filter is the goal's {@code canAttack}), its scan box (for the
     * messages), the three geometries (a null pair skips the row: the site refuses creepers, or its scan cannot see a silhouette
     * over 1), both trees' cites.
     */
    private record Site(String species, Supplier<? extends EntityType<? extends Mob>> type, Scan scan, String method, String filter,
            Sorter sorter, String box, Pair creeper, Pair silhouette, Pair control, String orig, String port) {
    }

    /** A private-scan site whose filter is {@code isSuitableTarget(LivingEntity)} — every one but the Frog. */
    private static Site site(String species, Supplier<? extends EntityType<? extends Mob>> type, String method, String box,
            Pair creeper, Pair silhouette, Pair control, String orig, String port) {
        return site(species, type, method, "isSuitableTarget", box, creeper, silhouette, control, orig, port);
    }

    private static Site site(String species, Supplier<? extends EntityType<? extends Mob>> type, String method, String filter,
            String box, Pair creeper, Pair silhouette, Pair control, String orig, String port) {
        return new Site(species, type, Scan.PRIVATE_METHOD, method, filter, Sorter.GENERIC, box, creeper, silhouette, control, orig, port);
    }

    /** The default geometries: a pig 6 against a creeper 7; a Silverfish 6 against a Ravager 9; two pigs 5 and 8. */
    private static final Pair PIG_THEN_CREEPER = pair(PIG, 6.0, CREEPER, 7.0);
    private static final Pair ZOMBIE_THEN_CREEPER = pair(ZOMBIE, 6.0, CREEPER, 7.0);
    private static final Pair SILVERFISH_THEN_RAVAGER = pair(SILVERFISH, 6.0, RAVAGER, 9.0);
    private static final Pair CHICKEN_THEN_GOLEM = pair(CHICKEN, 6.0, IRON_GOLEM, 9.0);
    private static final Pair TWO_PIGS = pair(PIG, 5.0, PIG, 8.0);
    private static final Pair TWO_ZOMBIES = pair(ZOMBIE, 5.0, ZOMBIE, 8.0);

    /** The site table in the ledger's T4 order (§T4 of phase_g_reports/targeting_survey_2026-09-04.md). */
    private static List<Site> sites() {
        List<Site> s = new ArrayList<>();
        // (i) the seventeen TF-035 remainders — swapped by this fix
        s.add(site("cephadrome", ModEntities.CEPHADROME, "findSomethingToAttack", "16/20/16", ZOMBIE_THEN_CREEPER, SILVERFISH_THEN_RAVAGER, TWO_ZOMBIES,
                "Cephadrome.java:61 / :84 GenericTargetSorter, the sort at :580", "Cephadrome.java:120 (the field, used :433)"));
        s.add(site("cryolophosaurus", ModEntities.CRYOLOPHOSAURUS, "findSomethingToAttack", "9/2/9", PIG_THEN_CREEPER, SILVERFISH_THEN_RAVAGER, TWO_PIGS,
                "Cryolophosaurus.java:58, the sort at :218", "Cryolophosaurus.java:146"));
        s.add(new Site("dragonfly", ModEntities.ENTITY_DRAGONFLY, Scan.DRAGONFLY_GOAL, "findPrey", "isPrey", Sorter.GENERIC, "10/6/10", null,
                pair(MOSQUITO, 4.0, HORSE, 5.5), pair(MOSQUITO, 4.0, MOSQUITO, 7.0),
                "Dragonfly.java:45, the sort at :236 (the whitelist :213-228 admits no creeper; a horse weighs 2.23, a mosquito 0.04)",
                "entity/ai/DragonflyHuntGoal.java:103"));
        s.add(site("fairy", ModEntities.FAIRY, "findSomethingToAttack", "8/8/8", ZOMBIE_THEN_CREEPER, SILVERFISH_THEN_RAVAGER, pair(ZOMBIE, 5.0, ZOMBIE, 7.5),
                "Fairy.java:63, the sort at :243 (EntityMob only, :235)", "Fairy.java:92 (the field, used :160)"));
        s.add(site("frog", ModEntities.FROG, "findInsectTarget", "isInsectTarget", "8/3/8", null, pair(CRICKET, 3.0, MOTHRA, 7.0), pair(CRICKET, 3.0, CRICKET, 6.0),
                "Frog.java:55, the sort at :312 (the insect list admits EntityButterfly — Mothra extends it in both trees, silhouette 10 — and no creeper)",
                "Frog.java:263"));
        s.add(site("gammametroid", ModEntities.ENTITY_GAMMA_METROID, "findSomethingToAttack", "10/3/10", null, CHICKEN_THEN_GOLEM, TWO_PIGS,
                "GammaMetroid.java:59, the sort at :298 (EntityMob refused :277, so no creeper)", "EntityGammaMetroid.java:223"));
        s.add(site("kyuubi", ModEntities.ENTITY_KYUUBI, "findSomethingToAttack", "12/4/12", null, CHICKEN_THEN_GOLEM, TWO_PIGS,
                "Kyuubi.java:56, the sort at :209 (EntityMob refused :189, so no creeper)", "EntityKyuubi.java:137"));
        s.add(site("leon", ModEntities.ENTITY_LEON, "findSomethingToAttack", "20/20/20", ZOMBIE_THEN_CREEPER, SILVERFISH_THEN_RAVAGER, TWO_ZOMBIES,
                "Leon.java:63 / :97, the sort at :435 (the custom scan; the vanilla goal's own sorter is plain in both trees)", "EntityLeon.java:800"));
        s.add(site("lizard", ModEntities.LIZARD, "findSomethingToAttack", "12/4/12", null, pair(CHICKEN, 4.0, SPIDER, 4.3), pair(CHICKEN, 4.0, CHICKEN, 7.0),
                "Lizard.java:45 / :62, the sort at :340 (the ladder :316-327 admits spiders and chickens, no creeper; a Spider weighs 1.26, a Chicken 0.28)",
                "Lizard.java:74 (the field, used :156)"));
        s.add(site("purplepower", ModEntities.PURPLE_POWER, "findSomethingToAttack", "32/24/32", PIG_THEN_CREEPER, SILVERFISH_THEN_RAVAGER, TWO_PIGS,
                "PurplePower.java:35 / :44, the sort at :272", "PurplePower.java:45 (the field, used :203)"));
        s.add(site("rat", ModEntities.ENTITY_RAT, "findSomethingToAttack", "9/2/9", PIG_THEN_CREEPER, SILVERFISH_THEN_RAVAGER, TWO_PIGS,
                "Rat.java:46 / :63, the sort at :256", "EntityRat.java:232"));
        s.add(site("robot1", ModEntities.ROBOT_1, "findSomethingToAttack", "8/3/8", null, pair(CHICKEN, 5.0, IRON_GOLEM, 7.5), pair(PIG, 5.0, PIG, 7.5),
                "Robot1.java:33 / :44, the sort at :209 (EntityMob refused :192, so no creeper)", "Robot1.java:63 (the field, used :149)"));
        s.add(site("spiderdriver", ModEntities.SPIDER_DRIVER, "findSomethingToAttack", "35/15/35", pair(PIG, 6.5, CREEPER, 7.5), pair(SILVERFISH, 6.5, RAVAGER, 9.5),
                pair(PIG, 6.5, PIG, 8.5),
                "SpiderDriver.java:33, the combat sort at :164 (a non-player closer than 6 is refused, :156)", "SpiderDriver.java:54 (the field, used :175)"));
        s.add(site("stinky", ModEntities.ENTITY_STINKY, "findSomethingToAttack", "12/6/12", ZOMBIE_THEN_CREEPER, SILVERFISH_THEN_RAVAGER, TWO_ZOMBIES,
                "Stinky.java:48 / :76, the sort at :692 (EntityMob only, then the feet ray :699)", "EntityStinky.java:572"));
        s.add(site("terribleterror", ModEntities.ENTITY_TERRIBLE_TERROR, "findSomethingToAttack", "12/8/12", PIG_THEN_CREEPER, SILVERFISH_THEN_RAVAGER, TWO_PIGS,
                "TerribleTerror.java:47 / :56, the sort at :300", "EntityTerribleTerror.java:197"));
        s.add(site("theprinceteen", ModEntities.THE_PRINCE_TEEN, "findSomethingToAttack", "25/20/25", ZOMBIE_THEN_CREEPER, SILVERFISH_THEN_RAVAGER, TWO_ZOMBIES,
                "ThePrinceTeen.java:79 / :121, the sort at :544", "ThePrinceTeen.java:144 (the field, used :917)"));
        s.add(site("triffid", ModEntities.ENTITY_TRIFFID, "findSomethingToAttack", "10/8/10", null, SILVERFISH_THEN_RAVAGER, TWO_PIGS,
                "Triffid.java:42 / :54, the sort at :326 (EntityCreeper refused :291)", "EntityTriffid.java:248"));
        // (ii) the sites that already sorted with the sorter — ENT-S-135's five rebuilt loops and ENT-S-108's nine — pinned here
        s.add(site("caterkiller", ModEntities.ENTITY_CATER_KILLER, "findSomethingToAttack", "20/8/20", ZOMBIE_THEN_CREEPER, SILVERFISH_THEN_RAVAGER, TWO_ZOMBIES,
                "CaterKiller.java:43 / :62, the sort at :564", "EntityCaterKiller.java:88 (the field, used :393 — ENT-S-135's loop)"));
        s.add(site("cavefisher", ModEntities.CAVE_FISHER, "findSomethingToAttack", "10/3/10", null, CHICKEN_THEN_GOLEM, TWO_PIGS,
                "CaveFisher.java:38 / :49, the sort at :235 (EntityMob refused :218, so no creeper)", "CaveFisher.java:68 (the field, used :172 — ENT-S-108's loop)"));
        s.add(site("dungeonbeast", ModEntities.DUNGEON_BEAST, "findSomethingToAttack", "16/3/16", PIG_THEN_CREEPER, SILVERFISH_THEN_RAVAGER, TWO_PIGS,
                "DungeonBeast.java:42 / :53, the sort at :254", "DungeonBeast.java:59 (the field, used :159 — ENT-S-108's loop)"));
        s.add(site("emperorscorpion", ModEntities.ENTITY_EMPEROR_SCORPION, "findSomethingToAttack", "24/6/24", null, SILVERFISH_THEN_RAVAGER, TWO_PIGS,
                "EmperorScorpion.java:52 / :64, the sort at :508 (EntityCreeper refused :485)", "EntityEmperorScorpion.java:68 (the field, used :323 — ENT-S-108's loop)"));
        s.add(site("hammerhead", ModEntities.HAMMERHEAD, "findSomethingToAttack", "18/9/18", ZOMBIE_THEN_CREEPER, SILVERFISH_THEN_RAVAGER, TWO_ZOMBIES,
                "Hammerhead.java:38 / :48, the sort at :256", "Hammerhead.java:57 (the field, used :179 — ENT-S-135's loop)"));
        s.add(site("herculesbeetle", ModEntities.ENTITY_HERCULES_BEETLE, "findSomethingToAttack", "16/6/16", null, SILVERFISH_THEN_RAVAGER, TWO_PIGS,
                "HerculesBeetle.java:40 / :51, the sort at :421 (EntityCreeper refused :401)", "EntityHerculesBeetle.java:53 (the field, used :251 — ENT-S-108's loop)"));
        s.add(site("nastysaurus", ModEntities.NASTYSAURUS, "findSomethingToAttack", "32/8/32", PIG_THEN_CREEPER, SILVERFISH_THEN_RAVAGER, TWO_PIGS,
                "Nastysaurus.java:41 / :52, the sort at :283", "Nastysaurus.java:62 (the field, used :244 — ENT-S-108's loop)"));
        s.add(site("seamonster", ModEntities.SEA_MONSTER, "findSomethingToAttack", "16/4/16", ZOMBIE_THEN_CREEPER, SILVERFISH_THEN_RAVAGER, TWO_ZOMBIES,
                "SeaMonster.java:39 / :55, the sort at :518", "SeaMonster.java:72 (the field, used :269 — ENT-S-135's loop)"));
        s.add(site("seaviper", ModEntities.SEA_VIPER, "findSomethingToAttack", "18/4/18", ZOMBIE_THEN_CREEPER, SILVERFISH_THEN_RAVAGER, TWO_ZOMBIES,
                "SeaViper.java:42 / :59, the sort at :535", "SeaViper.java:79 (the field, used :340 — ENT-S-135's loop)"));
        s.add(site("spitbug", ModEntities.ENTITY_SPIT_BUG, "findSomethingToAttack", "12/7/12", null, SILVERFISH_THEN_RAVAGER, TWO_PIGS,
                "SpitBug.java:47 / :61, the sort at :375 (EntityCreeper refused :352)", "EntitySpitBug.java:62 (the field, used :278 — ENT-S-108's loop)"));
        s.add(site("trex", ModEntities.TREX, "findSomethingToAttack", "20/6/20", PIG_THEN_CREEPER, SILVERFISH_THEN_RAVAGER, TWO_PIGS,
                "TRex.java:40 / :50, the sort at :255", "TRex.java:43 (the field, used :227 — ENT-S-108's loop)"));
        s.add(site("trooperbug", ModEntities.ENTITY_TROOPER_BUG, "findSomethingToAttack", "12/7/12", null, SILVERFISH_THEN_RAVAGER, TWO_PIGS,
                "TrooperBug.java:50 / :63, the sort at :515 (EntityCreeper refused :492)", "EntityTrooperBug.java:64 (the field, used :312 — ENT-S-108's loop)"));
        s.add(site("urchin", ModEntities.URCHIN, "findSomethingToAttack", "16/3/16", PIG_THEN_CREEPER, SILVERFISH_THEN_RAVAGER, TWO_PIGS,
                "Urchin.java:43 / :55, the sort at :277", "Urchin.java:63 (the field, used :213 — ENT-S-108's loop)"));
        // (v) the companions' IMob task — orig MyEntityAINearestAttackableTargetSorter, halving only
        s.add(new Site("boyfriend", ModEntities.BOYFRIEND, Scan.COMPANION_GOAL, null, null, Sorter.COMPANION, "15/4/15", ZOMBIE_THEN_CREEPER,
                SILVERFISH_THEN_RAVAGER, TWO_ZOMBIES,
                "Boyfriend.java:141 -> MyEntityAINearestAttackableTarget.java:38 / :57 MyEntityAINearestAttackableTargetSorter (:21-31: creeper halved, no silhouette term)",
                "Boyfriend.registerGoals — the IMob MyEntityAINearestAttackableTargetGoal's targetOrder (entity/ai/MyEntityAINearestAttackableTargetGoal.java:110-112, used :126)"));
        s.add(new Site("girlfriend", ModEntities.GIRLFRIEND, Scan.COMPANION_GOAL, null, null, Sorter.COMPANION, "15/4/15", ZOMBIE_THEN_CREEPER,
                SILVERFISH_THEN_RAVAGER, TWO_ZOMBIES,
                "Girlfriend.java:167 -> MyEntityAINearestAttackableTarget.java:38 / :57 MyEntityAINearestAttackableTargetSorter (:21-31)",
                "Girlfriend.registerGoals — the IMob MyEntityAINearestAttackableTargetGoal's targetOrder (entity/ai/MyEntityAINearestAttackableTargetGoal.java:110-112, used :126)"));
        return s;
    }

    // ------------------------------------------------------------------
    // Rows
    // ------------------------------------------------------------------

    private enum Kind {
        /** A non-creeper nearer than a creeper: the creeper is the pick (orig's halving), where plain distance took the non-creeper — the site's filter asserted to admit the non-creeper first. */
        CREEPER_OUTRANKS_NEARER("creeper_outranks_nearer"),
        /** A sub-1 silhouette nearer than an over-1 one: the big one is the pick (orig's division), where plain distance took the small one — the site's filter asserted to admit the small one first. */
        BIG_SILHOUETTE_OUTRANKS_NEARER("big_silhouette_outranks_nearer"),
        /** The companions: a sub-1 silhouette nearer than an over-1 one: the SMALL one is the pick — orig's task sorter had no silhouette term (GenericTargetSorter would take the big one, asserted). */
        NO_SILHOUETTE_TERM("no_silhouette_term_nearer_small_wins"),
        /** Two of one unweighted kind: the nearer is the pick under both orders. */
        CONTROL("control_nearer_unweighted_wins");

        final String tag;

        Kind(String tag) {
            this.tag = tag;
        }
    }

    private record Row(int index, String name, Consumer<GameTestHelper> body) {
        String testName() {
            return TEST_PREFIX + String.format("s139_%02d_%s", this.index, this.name);
        }
    }

    /** The rows in the ledger's T4 order: per site its geometries, the specials at their species' place. */
    private static List<Row> rows() {
        List<Row> rows = new ArrayList<>();
        for (Site site : sites()) {
            if (site.creeper() != null) {
                add(rows, site.species() + "_" + Kind.CREEPER_OUTRANKS_NEARER.tag, h -> weightedRow(h, site, Kind.CREEPER_OUTRANKS_NEARER, site.creeper()));
            }
            if (site.silhouette() != null) {
                Kind kind = site.sorter() == Sorter.COMPANION ? Kind.NO_SILHOUETTE_TERM : Kind.BIG_SILHOUETTE_OUTRANKS_NEARER;
                add(rows, site.species() + "_" + kind.tag, h -> weightedRow(h, site, kind, site.silhouette()));
            }
            add(rows, site.species() + "_" + Kind.CONTROL.tag, h -> weightedRow(h, site, Kind.CONTROL, site.control()));
            if (site.species().equals("spiderdriver")) {
                add(rows, "spiderdriver_mount_pick_control_nearer_unridden_robot", TargetSorterParityTests::spiderDriverMountPick);
            }
            if (site.species().equals("herculesbeetle")) { // the ledger's order: the Irukandji follows the Hercules Beetle
                add(rows, "irukandji_295_two_standing_players_nearer_wins", TargetSorterParityTests::irukandjiTwoStandingRow);
                add(rows, "irukandji_" + Kind.CONTROL.tag, TargetSorterParityTests::irukandjiControlRow);
            }
        }
        // (iii) the strafe finders' <= — the last equidistant player wins
        add(rows, "brutalfly_215_strafe_tie_last_equidistant_player_wins", h -> strafeTieRow(h, ModEntities.ENTITY_BRUTALFLY.get(), EntityBrutalfly.class, 30.0, 20.0, 30.0,
                "Brutalfly.java:215 findNearestEntityWithinAABB(EntityPlayer.class, box 30/20/30) — World.func_72857_a replaces on <= (ENT-S-105 bytecode)",
                "EntityBrutalfly.java:348-360 findNearestPlayerInStrafeBox (ENT-S-135)"));
        add(rows, "mothra_224_stage1_tie_last_equidistant_player_wins", h -> strafeTieRow(h, ModEntities.MOTHRA.get(), Mothra.class, 25.0, 20.0, 25.0,
                "Mothra.java:224 findNearestEntityWithinAABB(EntityPlayer.class, box 25/20/25) — the same <= replace",
                "Mothra.java:293-305 findNearestPlayerInStrafeBox (ENT-S-135)"));
        return rows;
    }

    private static void add(List<Row> rows, String name, Consumer<GameTestHelper> body) {
        rows.add(new Row(rows.size() + 1, name, body));
    }

    /** One test per row in the {@code targetSorterParity} batch. */
    @GameTestGenerator
    public Collection<TestFunction> targetSorterRows() {
        List<TestFunction> functions = new ArrayList<>();
        for (Row row : rows()) {
            functions.add(new TestFunction(BATCH, row.testName(), EMPTY_LARGE, Rotation.NONE, TIMEOUT_TICKS, 0L, true,
                    helper -> {
                        row.body().accept(helper);
                        helper.succeed();
                    }));
        }
        return functions;
    }

    // ------------------------------------------------------------------
    // The weighted rows: two candidates, orig's weights recomputed, the pick asked of the site's scan
    // ------------------------------------------------------------------

    private static void weightedRow(GameTestHelper helper, Site site, Kind kind, Pair pair) {
        assertNotPeaceful(helper);
        final boolean prior = OreSpawnConfig.PLAY_NICELY.get();
        Mob hunter = null;
        Mob nearer = null;
        Mob farther = null;
        try {
            OreSpawnConfig.PLAY_NICELY.set(false);
            helper.assertTrue(!OreSpawnConfig.PLAY_NICELY.get(), "precondition: PLAY_NICELY.set(false) must read back false (" + FINDING + " test setup)");
            hunter = spawnHunter(helper, site);
            String where = site.port() + " (orig " + site.orig() + ")";
            nearer = spawnPrey(helper, pair.nearer().get(), HUNTER_FEET.add(-pair.nearerDist(), 0.0, 0.0));
            farther = spawnPrey(helper, pair.farther().get(), HUNTER_FEET.add(pair.fartherDist(), 0.0, 0.0));
            assertDistance(helper, hunter, nearer, pair.nearerDist());
            assertDistance(helper, hunter, farther, pair.fartherDist());
            assertSees(helper, hunter, nearer, describe(nearer) + " " + pair.nearerDist() + " blocks west");
            assertSees(helper, hunter, farther, describe(farther) + " " + pair.fartherDist() + " blocks east");
            double plainNearer = hunter.distanceToSqr(nearer);
            double plainFarther = hunter.distanceToSqr(farther);
            helper.assertTrue(plainNearer < plainFarther, "precondition: plain distance² ranks " + describe(nearer) + " (" + plainNearer + ") ahead of "
                    + describe(farther) + " (" + plainFarther + ") — the order HEAD's comparingDouble(distanceToSqr) took (" + FINDING + " test geometry)");
            double weightNearer = weight(site, hunter, nearer);
            double weightFarther = weight(site, hunter, farther);
            Mob expected;
            String why;
            switch (kind) {
                case CREEPER_OUTRANKS_NEARER -> {
                    helper.assertTrue(farther instanceof Creeper && !(nearer instanceof Creeper), "precondition: the farther candidate is the Creeper, the nearer is not ("
                            + FINDING + " test setup)");
                    helper.assertTrue(weightFarther < weightNearer, "precondition: under orig's sorter the Creeper's halved distance² (" + weightFarther
                            + ") ranks ahead of the nearer " + describe(nearer) + "'s (" + weightNearer + ") — the row discriminates (" + FINDING + " test geometry)");
                    assertAdmits(helper, hunter, site, nearer);
                    expected = farther;
                    why = "a creeper " + pair.fartherDist() + " off outranks a " + kindName(nearer) + " " + pair.nearerDist() + " off: orig halved a creeper's distance²";
                }
                case BIG_SILHOUETTE_OUTRANKS_NEARER -> {
                    helper.assertTrue(silhouette(nearer) <= 1.0 && silhouette(farther) > 1.0, "precondition: the nearer " + describe(nearer) + " weighs "
                            + silhouette(nearer) + " (undivided) and the farther " + describe(farther) + " " + silhouette(farther) + " (over 1: divided) (" + FINDING
                            + " test setup)");
                    helper.assertTrue(weightFarther < weightNearer, "precondition: under orig's sorter the " + kindName(farther) + "'s divided distance² ("
                            + weightFarther + ") ranks ahead of the nearer " + kindName(nearer) + "'s (" + weightNearer + ") — the row discriminates (" + FINDING
                            + " test geometry)");
                    assertAdmits(helper, hunter, site, nearer);
                    expected = farther;
                    why = "a " + kindName(farther) + " (silhouette " + String.format("%.2f", silhouette(farther)) + ") " + pair.fartherDist() + " off outranks a "
                            + kindName(nearer) + " (" + String.format("%.2f", silhouette(nearer)) + ") " + pair.nearerDist() + " off: orig divided a silhouette over 1";
                }
                case NO_SILHOUETTE_TERM -> {
                    helper.assertTrue(silhouette(nearer) <= 1.0 && silhouette(farther) > 1.0, "precondition: the nearer " + describe(nearer) + " weighs "
                            + silhouette(nearer) + " and the farther " + describe(farther) + " " + silhouette(farther) + " (" + FINDING + " test setup)");
                    helper.assertTrue(genericWeight(hunter, farther) < genericWeight(hunter, nearer), "precondition: GenericTargetSorter WOULD rank the "
                            + kindName(farther) + " (" + genericWeight(hunter, farther) + ") ahead of the " + kindName(nearer) + " (" + genericWeight(hunter, nearer)
                            + ") — the row tells the companion sorter from it (" + FINDING + " test geometry)");
                    helper.assertTrue(weightNearer < weightFarther, "precondition: orig's task sorter (no silhouette term) ranks the nearer " + kindName(nearer)
                            + " first (" + weightNearer + " < " + weightFarther + ") (" + FINDING + " test geometry)");
                    expected = nearer;
                    why = "a " + kindName(nearer) + " " + pair.nearerDist() + " off outranks a " + kindName(farther) + " (silhouette " + String.format("%.2f", silhouette(farther))
                            + ") " + pair.fartherDist() + " off: MyEntityAINearestAttackableTargetSorter has no silhouette term";
                }
                case CONTROL -> {
                    helper.assertTrue(nearer.getType() == farther.getType(), "precondition: the control pair is one kind (" + FINDING + " test setup)");
                    helper.assertTrue(weightNearer < weightFarther, "precondition: orig's sorter ranks the nearer " + kindName(nearer) + " first (" + weightNearer
                            + " < " + weightFarther + "), as plain distance does (" + FINDING + " test geometry)");
                    expected = nearer;
                    why = "two " + kindName(nearer) + "s " + pair.nearerDist() + " and " + pair.fartherDist() + " off: the nearer wins under both orders";
                }
                default -> throw new IllegalStateException(kind.name());
            }
            LivingEntity pick = pick(helper, hunter, site);
            helper.assertTrue(pick == expected, where + " — " + why + ": the " + hunter.getClass().getSimpleName() + "'s pick must be " + describe(expected)
                    + " (" + FINDING + "); got " + describe(pick));
        } finally {
            OreSpawnConfig.PLAY_NICELY.set(prior);
            discardQuietly(farther);
            discardQuietly(nearer);
            discardQuietly(hunter);
        }
    }

    /** orig's weight for the site's sorter, recomputed from the live entities. */
    private static double weight(Site site, Mob hunter, Entity target) {
        return site.sorter() == Sorter.COMPANION ? companionWeight(hunter, target) : genericWeight(hunter, target);
    }

    /** orig GenericTargetSorter.java:18-35 — one operand's terms (the first's, :20-26): distance² (:20), halved for an EntityCreeper (:21-23), divided by a silhouette over 1 (:24-26). */
    private static double genericWeight(Entity hunter, Entity target) {
        double distanceSq = hunter.distanceToSqr(target);
        if (target instanceof Creeper) distanceSq /= 2.0;
        double silhouette = silhouette(target);
        if (silhouette > 1.0) distanceSq /= silhouette;
        return distanceSq;
    }

    /** orig MyEntityAINearestAttackableTargetSorter.java:21-31 — distance² (:22), halved for an EntityCreeper (:23-25); no silhouette term. */
    private static double companionWeight(Entity hunter, Entity target) {
        double distanceSq = hunter.distanceToSqr(target);
        if (target instanceof Creeper) distanceSq /= 2.0;
        return distanceSq;
    }

    /** orig's {@code field_70131_O * field_70130_N} — height times width. */
    private static double silhouette(Entity target) {
        return (double) (target.getBbHeight() * target.getBbWidth());
    }

    // ------------------------------------------------------------------
    // The specials
    // ------------------------------------------------------------------

    /**
     * The Irukandji (Irukandji.java:32 / :47, the sort at :295; the filter :270-288 takes players only): two standing survival
     * players, 5.0 blocks west and 5.1 blocks east — the nearer is the pick under both orders, both weighed 1.08 (0.6 x 1.8, divided).
     * A control: 1.7.10's player was 0.6 x 1.8 in every pose but sleeping (0.2 x 0.2) — sneaking set a flag and the client camera
     * drop, {@code setSize} never ran for a crouch — so orig GenericTargetSorter.java:24 divided a sneaking player's distance² by
     * 1.08 exactly as a standing one's, and on players the sorter reduced to nearest. The port weighs the modern pose-sized box
     * (crouching 0.6 x 1.5 = 0.9, undivided; swimming / gliding 0.36): a crouching player 5.0 off loses to a standing one 5.1 off
     * here where 1.7.10 took the crouching one (23.15 against 24.08) — a port-only divergence at every sorter site, ENT-S-140, for
     * the owner's ruling; this row pins nothing of it (the ledger's own note on the row, "sneaking players (0.9, undivided) rank
     * differently", was that port-only reading).
     */
    private static void irukandjiTwoStandingRow(GameTestHelper helper) {
        assertNotPeaceful(helper);
        final boolean prior = OreSpawnConfig.PLAY_NICELY.get();
        Mob hunter = null;
        ServerPlayer nearer = null;
        ServerPlayer farther = null;
        try {
            OreSpawnConfig.PLAY_NICELY.set(false);
            hunter = spawnFrozen(helper, ModEntities.IRUKANDJI.get(), HUNTER_POS);
            nearer = survivalServerPlayerAt(helper, helper.absoluteVec(HUNTER_FEET.add(-5.0, 0.0, 0.0)));
            farther = survivalServerPlayerAt(helper, helper.absoluteVec(HUNTER_FEET.add(5.1, 0.0, 0.0)));
            helper.assertTrue(nearer.getPose() == Pose.STANDING && farther.getPose() == Pose.STANDING && silhouette(nearer) == silhouette(farther)
                    && silhouette(nearer) > 1.0, "precondition: both players stand (vanilla Player POSES: standing 0.6 x 1.8 = 1.08, over 1 — divided alike) ("
                            + FINDING + " test setup); " + silhouette(nearer) + " / " + silhouette(farther));
            assertDistance(helper, hunter, nearer, 5.0);
            assertDistance(helper, hunter, farther, 5.1);
            assertSees(helper, hunter, nearer, "the standing player 5.0 blocks west");
            assertSees(helper, hunter, farther, "the standing player 5.1 blocks east");
            helper.assertTrue(genericWeight(hunter, nearer) < genericWeight(hunter, farther) && hunter.distanceToSqr(nearer) < hunter.distanceToSqr(farther),
                    "precondition: both orders rank the nearer standing player first (" + genericWeight(hunter, nearer) + " < " + genericWeight(hunter, farther)
                            + ") (" + FINDING + " test geometry)");
            LivingEntity pick = (LivingEntity) invoke(hunter, hunter.getClass(), "findSomethingToAttack");
            helper.assertTrue(pick == nearer, "Irukandji.java:218-222 (orig Irukandji.java:295 Collections.sort(var5, this.TargetSorter) — GenericTargetSorter :32 / :47):"
                    + " two standing survival players 5.0 and 5.1 blocks off — the nearer is the pick under both orders, a uniform 1.08 silhouette dividing both"
                    + " (1.7.10's player silhouette was pose-independent; the port's crouch flip is ENT-S-140, not pinned here) (" + FINDING + "); got " + describe(pick));
        } finally {
            OreSpawnConfig.PLAY_NICELY.set(prior);
            removePlayer(helper, farther);
            removePlayer(helper, nearer);
            discardQuietly(hunter);
        }
    }

    /** The Irukandji's control: two standing survival players, 4.0 west and 5.5 east — the nearer is the pick under both orders. */
    private static void irukandjiControlRow(GameTestHelper helper) {
        assertNotPeaceful(helper);
        final boolean prior = OreSpawnConfig.PLAY_NICELY.get();
        Mob hunter = null;
        ServerPlayer nearer = null;
        ServerPlayer farther = null;
        try {
            OreSpawnConfig.PLAY_NICELY.set(false);
            hunter = spawnFrozen(helper, ModEntities.IRUKANDJI.get(), HUNTER_POS);
            nearer = survivalServerPlayerAt(helper, helper.absoluteVec(HUNTER_FEET.add(-4.0, 0.0, 0.0)));
            farther = survivalServerPlayerAt(helper, helper.absoluteVec(HUNTER_FEET.add(5.5, 0.0, 0.0)));
            assertDistance(helper, hunter, nearer, 4.0);
            assertDistance(helper, hunter, farther, 5.5);
            assertSees(helper, hunter, nearer, "the standing player 4.0 blocks west");
            assertSees(helper, hunter, farther, "the standing player 5.5 blocks east");
            helper.assertTrue(genericWeight(hunter, nearer) < genericWeight(hunter, farther) && hunter.distanceToSqr(nearer) < hunter.distanceToSqr(farther),
                    "precondition: both orders rank the nearer standing player first (" + FINDING + " test geometry)");
            LivingEntity pick = (LivingEntity) invoke(hunter, hunter.getClass(), "findSomethingToAttack");
            helper.assertTrue(pick == nearer, "Irukandji.java:218-222 (orig :295): two standing survival players 4.0 and 5.5 blocks off — the nearer is the pick under"
                    + " both orders (a uniform 1.08 silhouette) (" + FINDING + "); got " + describe(pick));
        } finally {
            OreSpawnConfig.PLAY_NICELY.set(prior);
            removePlayer(helper, farther);
            removePlayer(helper, nearer);
            discardQuietly(hunter);
        }
    }

    /**
     * The Brutalfly / Mothra strafe finder: two survival players at distance² 25 on either side of the frozen flier; the finder
     * replaces on {@code <=} (orig World.func_72857_a, ENT-S-105 / ENT-S-135), so the LAST of the two in the box list's order is
     * the pick — a strict {@code <} (HEAD's getNearestPlayer, the ledger's DIVERGES) would have kept the first. The box list is read
     * by the row the way the finder reads it, so any farther player of another cell in the list is harmless (only a nearer one
     * would be, and the row refuses that).
     */
    private static void strafeTieRow(GameTestHelper helper, EntityType<? extends Mob> type, Class<? extends Mob> declaring,
            double bx, double by, double bz, String orig, String port) {
        Mob flier = null;
        ServerPlayer west = null;
        ServerPlayer east = null;
        try {
            flier = spawnFrozen(helper, type, HUNTER_POS);
            west = survivalServerPlayerAt(helper, helper.absoluteVec(HUNTER_FEET.add(-TIE_OFFSET, 0.0, 0.0)));
            east = survivalServerPlayerAt(helper, helper.absoluteVec(HUNTER_FEET.add(TIE_OFFSET, 0.0, 0.0)));
            double westSq = flier.distanceToSqr(west);
            double eastSq = flier.distanceToSqr(east);
            helper.assertTrue(westSq == eastSq, "precondition: the two players stand at one distance² from the flier (" + westSq + " / " + eastSq + ") (" + FINDING
                    + " test geometry)");
            AABB box = flier.getBoundingBox().inflate(bx, by, bz);
            List<Player> listed = helper.getLevel().getEntitiesOfClass(Player.class, box);
            int westIndex = listed.indexOf(west);
            int eastIndex = listed.indexOf(east);
            helper.assertTrue(westIndex >= 0 && eastIndex >= 0, "precondition: both players are listed by the " + (int) bx + "/" + (int) by + "/" + (int) bz
                    + " strafe box (" + FINDING + " test geometry)");
            for (Player other : listed) {
                if (other != west && other != east) {
                    helper.assertTrue(flier.distanceToSqr(other) > westSq, "precondition: no other player of the level stands as near as the tie (" + FINDING
                            + " test setup); " + describe(other) + " at " + flier.distanceToSqr(other));
                }
            }
            ServerPlayer last = westIndex > eastIndex ? west : east;
            ServerPlayer first = last == west ? east : west;
            LivingEntity pick = (LivingEntity) invoke(flier, declaring, "findNearestPlayerInStrafeBox");
            helper.assertTrue(pick == last && pick != first, port + " (orig " + orig + "): of two equidistant players the LAST in the box list's order is the pick"
                    + " — the replace on <= — where a strict < keeps the first (" + FINDING + "); list order [" + describe(listed.get(Math.min(westIndex, eastIndex)))
                    + ", " + describe(listed.get(Math.max(westIndex, eastIndex))) + "], got " + describe(pick));
        } finally {
            removePlayer(helper, east);
            removePlayer(helper, west);
            discardQuietly(flier);
        }
    }

    /**
     * The Spider Driver's mount pick (SpiderDriver.java:33, the sort at :108 over SpiderRobot.class in a 25/15/25 box, the first
     * unridden): every Spider Robot weighs the same 3.25 x 2.25, so orig's sorter reduces to nearest — the nearer of two unridden
     * robots (8 blocks west, 12 blocks east) is the pick; a control that the swapped field serves the second sort as the first.
     */
    private static void spiderDriverMountPick(GameTestHelper helper) {
        assertNotPeaceful(helper);
        final boolean prior = OreSpawnConfig.PLAY_NICELY.get();
        Mob driver = null;
        Mob nearer = null;
        Mob farther = null;
        try {
            OreSpawnConfig.PLAY_NICELY.set(false);
            driver = spawnFrozen(helper, ModEntities.SPIDER_DRIVER.get(), HUNTER_POS);
            nearer = spawnPrey(helper, SPIDER_ROBOT.get(), HUNTER_FEET.add(-8.0, 0.0, 0.0));
            farther = spawnPrey(helper, SPIDER_ROBOT.get(), HUNTER_FEET.add(12.0, 0.0, 0.0));
            helper.assertTrue(!nearer.isVehicle() && !farther.isVehicle(), "precondition: both robots are unridden (" + FINDING + " test setup)");
            assertDistance(helper, driver, nearer, 8.0);
            assertDistance(helper, driver, farther, 12.0);
            helper.assertTrue(silhouette(nearer) == silhouette(farther) && genericWeight(driver, nearer) < genericWeight(driver, farther),
                    "precondition: the robots weigh the same and orig's sorter ranks the nearer first (" + FINDING + " test geometry)");
            LivingEntity pick = (LivingEntity) invoke(driver, driver.getClass(), "findSpiderRobot");
            helper.assertTrue(pick == nearer, "SpiderDriver.java:156 findSpiderRobot (orig SpiderDriver.java:108 Collections.sort(var5, this.TargetSorter) — the"
                    + " GenericTargetSorter field :33): the nearer of two unridden Spider Robots is the pick (" + FINDING + "); got " + describe(pick));
        } finally {
            OreSpawnConfig.PLAY_NICELY.set(prior);
            discardQuietly(farther);
            discardQuietly(nearer);
            discardQuietly(driver);
        }
    }

    // ------------------------------------------------------------------
    // Spawning, the pick, the preconditions
    // ------------------------------------------------------------------

    /** The site's hunter: frozen for a private scan (the Dragonfly's goal survives removeFreeWill in its field), with goals for the companions. */
    private static Mob spawnHunter(GameTestHelper helper, Site site) {
        if (site.scan() == Scan.COMPANION_GOAL) {
            return spawnCompanion(helper, site.type().get(), HUNTER_POS);
        }
        Mob hunter = spawnFrozen(helper, site.type().get(), HUNTER_POS);
        if (site.scan() == Scan.DRAGONFLY_GOAL) {
            helper.assertTrue(!OreSpawnConfig.DRAGONFLY_HORSE_FRIENDLY.get(), "precondition: dragonflyHorseFriendly is off (its default) — orig"
                    + " Dragonfly.java:228 admits a horse only then, and the horse is the row's over-1 silhouette (" + FINDING + " test setup; never flipped)");
        }
        if (hunter instanceof TamableAnimal tamable) {
            helper.assertTrue(!tamable.isTame(), "precondition: the " + hunter.getClass().getSimpleName() + " is wild — its scan or filter reads isTame (" + FINDING
                    + " test setup)");
        }
        helper.assertTrue(!hunter.isBaby(), "precondition: the " + hunter.getClass().getSimpleName() + " is grown — the Gamma Metroid's scan refuses as a child ("
                + FINDING + " test setup)");
        return hunter;
    }

    /** The site's pick. */
    private static LivingEntity pick(GameTestHelper helper, Mob hunter, Site site) {
        return switch (site.scan()) {
            case PRIVATE_METHOD -> (LivingEntity) invoke(hunter, hunter.getClass(), site.method());
            case DRAGONFLY_GOAL -> {
                Object goal = readField(hunter, EntityDragonfly.class, "huntGoal");
                helper.assertTrue(goal instanceof DragonflyHuntGoal, "precondition: EntityDragonfly.huntGoal is the DragonflyHuntGoal (" + FINDING + " test setup)");
                yield (LivingEntity) invoke(goal, DragonflyHuntGoal.class, "findPrey");
            }
            case COMPANION_GOAL -> {
                NearestAttackableTargetGoal<?> goal = goalOfType(helper, hunter, Mob.class);
                boolean can = goal.canUse();
                LivingEntity target = (LivingEntity) readField(goal, NearestAttackableTargetGoal.class, "target");
                helper.assertTrue(can == (target != null), "precondition: the IMob goal's canUse agrees with its pick (" + FINDING + " test setup); canUse=" + can
                        + ", pick " + describe(target));
                yield target;
            }
        };
    }

    /**
     * The site's own filter must admit the nearer candidate, or a weighted row's pick would be the farther one whether or not the
     * sorter weighed it: asserted by reflection before the pick (the ScanSetParityTests.filter idiom), so a later ladder change
     * that refuses the nearer candidate fails the row here instead of passing without discriminating. The pick's other legs — the
     * scan box, PlayNicely, the Stinky's feet ray beside its {@code isSuitableTarget} (EntityStinky.java:573), the Dragonfly goal's
     * own sight step ahead of {@code isPrey} — are the row's geometry, asserted with it. For the companions the call is the goal's
     * {@code canAttack(nearer, targetConditions)}: outcome-neutral — on a non-granted nearer candidate vanilla's reach cache is
     * primed to 1 within the tick (a {@code nextInt(5)} cache duration drawn, TargetGoal.canReach) and the granted Creeper the
     * pick expects never reaches it.
     */
    private static void assertAdmits(GameTestHelper helper, Mob hunter, Site site, LivingEntity nearer) {
        helper.assertTrue(admits(helper, hunter, site, nearer), "precondition: the " + hunter.getClass().getSimpleName() + "'s own filter (" + filterName(site)
                + ") admits the nearer " + describe(nearer) + " — the row discriminates only if the pick had it to refuse (" + FINDING + " test setup)");
    }

    /** The site's filter on one candidate, by reflection: the private method the scan's predicate names, or the companions' canAttack. */
    private static boolean admits(GameTestHelper helper, Mob hunter, Site site, LivingEntity candidate) {
        return switch (site.scan()) {
            case PRIVATE_METHOD -> (Boolean) invoke(hunter, hunter.getClass(), site.filter(), new Class<?>[] {LivingEntity.class}, candidate);
            case DRAGONFLY_GOAL -> {
                Object goal = readField(hunter, EntityDragonfly.class, "huntGoal");
                helper.assertTrue(goal instanceof DragonflyHuntGoal, "precondition: EntityDragonfly.huntGoal is the DragonflyHuntGoal (" + FINDING + " test setup)");
                yield (Boolean) invoke(goal, DragonflyHuntGoal.class, site.filter(), new Class<?>[] {LivingEntity.class}, candidate);
            }
            case COMPANION_GOAL -> {
                NearestAttackableTargetGoal<?> goal = goalOfType(helper, hunter, Mob.class);
                helper.assertTrue(goal instanceof MyEntityAINearestAttackableTargetGoal<?>, "precondition: the IMob goal is the port's"
                        + " MyEntityAINearestAttackableTargetGoal — its canAttack is the filter (" + FINDING + " test setup)");
                TargetingConditions conditions = (TargetingConditions) readField(goal, NearestAttackableTargetGoal.class, "targetConditions");
                yield (Boolean) invoke(goal, MyEntityAINearestAttackableTargetGoal.class, "canAttack",
                        new Class<?>[] {LivingEntity.class, TargetingConditions.class}, candidate, conditions);
            }
        };
    }

    private static String filterName(Site site) {
        return switch (site.scan()) {
            case PRIVATE_METHOD -> site.filter();
            case DRAGONFLY_GOAL -> "DragonflyHuntGoal." + site.filter();
            case COMPANION_GOAL -> "MyEntityAINearestAttackableTargetGoal.canAttack(candidate, targetConditions)";
        };
    }

    /** Frozen: goals stripped, noAi, on the ground, persistence set (the IgnoreListParityTests idiom). */
    private static Mob spawnFrozen(GameTestHelper helper, EntityType<? extends Mob> type, BlockPos pos) {
        Mob mob = helper.spawnWithNoFreeWill(type, pos);
        mob.setNoAi(true);
        mob.setOnGround(true);
        mob.setPersistenceRequired();
        return mob;
    }

    /**
     * A companion with its goals, on the ground (the nearbyOnly reach cache paths through GroundPathNavigation.canUpdatePath),
     * FOLLOW_RANGE raised to 40 for the path search behind that test (the ScanSetParityTests idiom), tamed (the PreyListParityTests
     * TAMED idiom — orig MyEntityAINearestAttackableTarget.java:44-49 refused an untamed owner; the port's gate is ENT-S-137's
     * residual, so the rows do not depend on it either way).
     */
    private static Mob spawnCompanion(GameTestHelper helper, EntityType<? extends Mob> type, BlockPos pos) {
        Mob mob = helper.spawn(type, pos);
        mob.setNoAi(true);
        mob.setOnGround(true);
        mob.setPersistenceRequired();
        mob.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(40.0);
        ((TamableAnimal) mob).setTame(true, false);
        return mob;
    }

    /** Frozen prey with 1000 HP at an exact spot on the floor: goals stripped, noAi, on the ground, persistence set. */
    private static Mob spawnPrey(GameTestHelper helper, EntityType<? extends Mob> type, Vec3 pos) {
        Mob prey = helper.spawnWithNoFreeWill(type, pos);
        prey.setNoAi(true);
        prey.setOnGround(true);
        prey.setPersistenceRequired();
        prey.getAttribute(Attributes.MAX_HEALTH).setBaseValue(PREY_HEALTH);
        prey.setHealth(PREY_HEALTH);
        return prey;
    }

    /** The IMob goal off the target selector: exactly one NearestAttackableTargetGoal of the target type (the ScanSetParityTests idiom). */
    private static NearestAttackableTargetGoal<?> goalOfType(GameTestHelper helper, Mob hunter, Class<?> targetType) {
        NearestAttackableTargetGoal<?> found = null;
        int count = 0;
        for (WrappedGoal wrapped : hunter.targetSelector.getAvailableGoals()) {
            if (wrapped.getGoal() instanceof NearestAttackableTargetGoal<?> nearest
                    && readField(nearest, NearestAttackableTargetGoal.class, "targetType") == targetType) {
                found = nearest;
                count++;
            }
        }
        helper.assertTrue(count == 1 && found != null, "precondition: " + hunter.getClass().getSimpleName() + " carries exactly one NearestAttackableTargetGoal<"
                + targetType.getSimpleName() + "> on its target selector — found " + count + " (" + FINDING + " test setup)");
        return found;
    }

    private static void assertNotPeaceful(GameTestHelper helper) {
        helper.assertTrue(helper.getLevel().getDifficulty() != Difficulty.PEACEFUL,
                "precondition: the game-test level runs at NORMAL, not Peaceful — several filters answer false there (" + FINDING + " test setup)");
    }

    private static void assertDistance(GameTestHelper helper, Mob hunter, Entity candidate, double expected) {
        double distance = Math.sqrt(hunter.distanceToSqr(candidate));
        helper.assertTrue(Math.abs(distance - expected) < GEOMETRY_TOLERANCE, "precondition: " + describe(candidate) + " stands " + expected
                + " from the hunter's feet, feet to feet — the distance both sorters measure (" + FINDING + " test geometry); measured " + distance);
    }

    private static void assertSees(GameTestHelper helper, Mob hunter, LivingEntity candidate, String why) {
        helper.assertTrue(hunter.hasLineOfSight(candidate), "precondition: the " + hunter.getClass().getSimpleName() + " (eye "
                + String.format("%.2f", hunter.getEyeHeight()) + " above its feet on the floor) must see " + why + " inside the barrier shell (" + FINDING
                + " test geometry)");
    }

    /**
     * A plain {@link ServerPlayer} put on the player list the way the framework's mock is, without the framework's override
     * (PlayNicelyGateParityTests.survivalServerPlayerAt): {@code GameTestHelper.makeMockServerPlayerInLevel} answers
     * {@code isCreative()} true whatever its mode; this one's abilities are the survival mode's (instabuild clear).
     */
    private static ServerPlayer survivalServerPlayerAt(GameTestHelper helper, Vec3 absolutePos) {
        MinecraftServer server = helper.getLevel().getServer();
        CommonListenerCookie cookie = CommonListenerCookie.createInitial(
                new GameProfile(UUID.randomUUID(), "test-survival-player"), false);
        ServerPlayer player = new ServerPlayer(server, helper.getLevel(), cookie.gameProfile(), cookie.clientInformation());
        Connection connection = new Connection(PacketFlow.SERVERBOUND);
        new EmbeddedChannel(connection);
        server.getPlayerList().placeNewPlayer(connection, player, cookie);
        player.setGameMode(GameType.SURVIVAL);
        player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000.0);
        player.setHealth(1000.0f);
        player.teleportTo(helper.getLevel(), absolutePos.x, absolutePos.y, absolutePos.z, 0.0f, 0.0f);
        player.setOnGround(true);
        return player;
    }

    private static void removePlayer(GameTestHelper helper, ServerPlayer player) {
        if (player != null) {
            helper.getLevel().getServer().getPlayerList().remove(player);
        }
    }

    private static void discardQuietly(Entity entity) {
        if (entity != null && !entity.isRemoved()) {
            entity.discard();
        }
    }

    private static Object invoke(Object target, Class<?> declaring, String name) {
        return invoke(target, declaring, name, new Class<?>[0]);
    }

    private static Object invoke(Object target, Class<?> declaring, String name, Class<?>[] types, Object... args) {
        String where = declaring.getSimpleName() + "." + name;
        try {
            Method method = declaring.getDeclaredMethod(name, types);
            method.setAccessible(true);
            return method.invoke(target, args);
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

    private static String kindName(Entity entity) {
        return entity.getType().toShortString();
    }

    private static String describe(Entity entity) {
        return entity == null ? "null" : entity.getClass().getSimpleName() + "#" + entity.getId();
    }
}
