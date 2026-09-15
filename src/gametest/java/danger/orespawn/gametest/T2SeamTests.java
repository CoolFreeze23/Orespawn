package danger.orespawn.gametest;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.client.AlienBossGeoReplacement;
import danger.orespawn.entity.client.AlienGeoReplacement;
import danger.orespawn.entity.client.AlienRenderer;
import danger.orespawn.entity.client.AlosaurusGeoReplacement;
import danger.orespawn.entity.client.AntGeoReplacement;
import danger.orespawn.entity.client.AntRenderer;
import danger.orespawn.entity.client.AttackSquidGeoReplacement;
import danger.orespawn.entity.client.AttackSquidRenderer;
import danger.orespawn.entity.client.BabyDragonGeoReplacement;
import danger.orespawn.entity.client.BandPGeoReplacement;
import danger.orespawn.entity.client.BandPRenderer;
import danger.orespawn.entity.client.BaryonyxGeoReplacement;
import danger.orespawn.entity.client.BasiliskGeoReplacement;
import danger.orespawn.entity.client.BasiliskRenderer;
import danger.orespawn.entity.client.BeeGeoReplacement;
import danger.orespawn.entity.client.BeeRenderer;
import danger.orespawn.entity.client.BrutalflyGeoReplacement;
import danger.orespawn.entity.client.BrutalflyRenderer;
import danger.orespawn.entity.client.CamarasaurusGeoReplacement;
import danger.orespawn.entity.client.CannonFodderGeoReplacement;
import danger.orespawn.entity.client.CassowaryGeoReplacement;
import danger.orespawn.entity.client.CaterKillerGeoReplacement;
import danger.orespawn.entity.client.CaterKillerRenderer;
import danger.orespawn.entity.client.CaveFisherGeoReplacement;
import danger.orespawn.entity.client.CaveFisherRenderer;
import danger.orespawn.entity.client.CephadromeGeoReplacement;
import danger.orespawn.entity.client.ChipmunkGeoReplacement;
import danger.orespawn.entity.client.ChipmunkRenderer;
import danger.orespawn.entity.client.CliffRacerGeoReplacement;
import danger.orespawn.entity.client.CliffRacerRenderer;
import danger.orespawn.entity.client.CloudSharkGeoReplacement;
import danger.orespawn.entity.client.CockateilGeoReplacement;
import danger.orespawn.entity.client.CockateilRenderer;
import danger.orespawn.entity.client.CrabGeoReplacement;
import danger.orespawn.entity.client.CrabRenderer;
import danger.orespawn.entity.client.CreepingHorrorGeoReplacement;
import danger.orespawn.entity.client.CreepingHorrorRenderer;
import danger.orespawn.entity.client.CricketGeoReplacement;
import danger.orespawn.entity.client.CricketRenderer;
import danger.orespawn.entity.client.CryolophosaurusGeoReplacement;
import danger.orespawn.entity.client.DragonGeoReplacement;
import danger.orespawn.entity.client.DragonRenderer;
import danger.orespawn.entity.client.DragonflyGeoReplacement;
import danger.orespawn.entity.client.DragonflyRenderer;
import danger.orespawn.entity.client.DrawOrder;
import danger.orespawn.entity.client.DungeonBeastGeoReplacement;
import danger.orespawn.entity.client.DungeonBeastRenderer;
import danger.orespawn.entity.client.EasterBunnyGeoReplacement;
import danger.orespawn.entity.client.EasterBunnyRenderer;
import danger.orespawn.entity.client.EmperorScorpionGeoReplacement;
import danger.orespawn.entity.client.EmperorScorpionRenderer;
import danger.orespawn.entity.client.EnderKnightGeoReplacement;
import danger.orespawn.entity.client.EnderKnightRenderer;
import danger.orespawn.entity.client.EnderReaperGeoReplacement;
import danger.orespawn.entity.client.EnderReaperRenderer;
import danger.orespawn.entity.client.FaceOrder;
import danger.orespawn.entity.client.FairyGeoReplacement;
import danger.orespawn.entity.client.FairyRenderer;
import danger.orespawn.entity.client.FireflyGeoReplacement;
import danger.orespawn.entity.client.FireflyRenderer;
import danger.orespawn.entity.client.FlounderGeoReplacement;
import danger.orespawn.entity.client.FlounderRenderer;
import danger.orespawn.entity.client.FrogGeoReplacement;
import danger.orespawn.entity.client.FrogRenderer;
import danger.orespawn.entity.client.GammaMetroidGeoReplacement;
import danger.orespawn.entity.client.GammaMetroidRenderer;
import danger.orespawn.entity.client.GazelleGeoReplacement;
import danger.orespawn.entity.client.GazelleRenderer;
import danger.orespawn.entity.client.GeoReplacementDescriptor;
import danger.orespawn.entity.client.GhostSkellyGeoReplacement;
import danger.orespawn.entity.client.GhostSkellyRenderer;
import danger.orespawn.entity.client.GiantRobotGeoReplacement;
import danger.orespawn.entity.client.GiantRobotRenderer;
import danger.orespawn.entity.client.GodzillaGeoReplacement;
import danger.orespawn.entity.client.GodzillaRenderer;
import danger.orespawn.entity.client.GoldFishGeoReplacement;
import danger.orespawn.entity.client.GoldFishRenderer;
import danger.orespawn.entity.client.HammerheadGeoReplacement;
import danger.orespawn.entity.client.HammerheadRenderer;
import danger.orespawn.entity.client.HerculesBeetleGeoReplacement;
import danger.orespawn.entity.client.HerculesBeetleRenderer;
import danger.orespawn.entity.client.HydroliscGeoReplacement;
import danger.orespawn.entity.client.HydroliscRenderer;
import danger.orespawn.entity.client.IrukandjiGeoReplacement;
import danger.orespawn.entity.client.IrukandjiRenderer;
import danger.orespawn.entity.client.JefferyGeoReplacement;
import danger.orespawn.entity.client.KrakenGeoReplacement;
import danger.orespawn.entity.client.KrakenRenderer;
import danger.orespawn.entity.client.KyuubiGeoReplacement;
import danger.orespawn.entity.client.KyuubiRenderer;
import danger.orespawn.entity.client.LeafMonsterGeoReplacement;
import danger.orespawn.entity.client.LeafMonsterRenderer;
import danger.orespawn.entity.client.LeonGeoReplacement;
import danger.orespawn.entity.client.LeonopteryxGeoReplacement;
import danger.orespawn.entity.client.LizardGeoReplacement;
import danger.orespawn.entity.client.LizardRenderer;
import danger.orespawn.entity.client.MantisGeoReplacement;
import danger.orespawn.entity.client.MantisRenderer;
import danger.orespawn.entity.client.MolenoidGeoReplacement;
import danger.orespawn.entity.client.MolenoidRenderer;
import danger.orespawn.entity.client.MosquitoGeoReplacement;
import danger.orespawn.entity.client.MosquitoRenderer;
import danger.orespawn.entity.client.NastysaurusGeoReplacement;
import danger.orespawn.entity.client.OreSpawnGeoReplacement;
import danger.orespawn.entity.client.OreSpawnGeoReplacementModel;
import danger.orespawn.entity.client.OstrichGeoReplacement;
import danger.orespawn.entity.client.OstrichRenderer;
import danger.orespawn.entity.client.PeacockGeoReplacement;
import danger.orespawn.entity.client.PeacockRenderer;
import danger.orespawn.entity.client.PitchBlackGeoReplacement;
import danger.orespawn.entity.client.PitchBlackRenderer;
import danger.orespawn.entity.client.PointysaurusGeoReplacement;
import danger.orespawn.entity.client.PoseInputs;
import danger.orespawn.entity.client.RainbowAntGeoReplacement;
import danger.orespawn.entity.client.RainbowAntRenderer;
import danger.orespawn.entity.client.RatGeoReplacement;
import danger.orespawn.entity.client.RatRenderer;
import danger.orespawn.entity.client.RedAntGeoReplacement;
import danger.orespawn.entity.client.RedAntRenderer;
import danger.orespawn.entity.client.RenderInfo;
import danger.orespawn.entity.client.RubberDuckyGeoReplacement;
import danger.orespawn.entity.client.RubberDuckyRenderer;
import danger.orespawn.entity.client.RubyBirdGeoReplacement;
import danger.orespawn.entity.client.SeaMonsterGeoReplacement;
import danger.orespawn.entity.client.SeaMonsterRenderer;
import danger.orespawn.entity.client.SeaViperGeoReplacement;
import danger.orespawn.entity.client.SeaViperRenderer;
import danger.orespawn.entity.client.SkateGeoReplacement;
import danger.orespawn.entity.client.SkateRenderer;
import danger.orespawn.entity.client.SpitBugGeoReplacement;
import danger.orespawn.entity.client.SpitBugRenderer;
import danger.orespawn.entity.client.SpyroGeoReplacement;
import danger.orespawn.entity.client.SpyroRenderer;
import danger.orespawn.entity.client.StinkBugGeoReplacement;
import danger.orespawn.entity.client.StinkBugRenderer;
import danger.orespawn.entity.client.StinkyGeoReplacement;
import danger.orespawn.entity.client.StinkyRenderer;
import danger.orespawn.entity.client.TRexGeoReplacement;
import danger.orespawn.entity.client.TRexRenderer;
import danger.orespawn.entity.client.TermiteGeoReplacement;
import danger.orespawn.entity.client.TermiteRenderer;
import danger.orespawn.entity.client.TerribleTerrorGeoReplacement;
import danger.orespawn.entity.client.TerribleTerrorRenderer;
import danger.orespawn.entity.client.ThePrinceAdultGeoReplacement;
import danger.orespawn.entity.client.ThePrinceGeoReplacement;
import danger.orespawn.entity.client.TriffidGeoReplacement;
import danger.orespawn.entity.client.TriffidRenderer;
import danger.orespawn.entity.client.TrooperBugGeoReplacement;
import danger.orespawn.entity.client.TrooperBugRenderer;
import danger.orespawn.entity.client.TshirtGeoReplacement;
import danger.orespawn.entity.client.TshirtRenderer;
import danger.orespawn.entity.client.UnstableAntGeoReplacement;
import danger.orespawn.entity.client.UnstableAntRenderer;
import danger.orespawn.entity.client.UrchinGeoReplacement;
import danger.orespawn.entity.client.UrchinRenderer;
import danger.orespawn.entity.client.VelocityRaptorGeoReplacement;
import danger.orespawn.entity.client.VelocityRaptorRenderer;
import danger.orespawn.entity.client.WhaleGeoReplacement;
import danger.orespawn.entity.client.WhaleRenderer;
import danger.orespawn.entity.client.WormLargeGeoReplacement;
import danger.orespawn.entity.client.WormLargeRenderer;
import danger.orespawn.entity.client.WormMediumGeoReplacement;
import danger.orespawn.entity.client.WormMediumRenderer;
import danger.orespawn.entity.client.WormSmallGeoReplacement;
import danger.orespawn.entity.client.WormSmallRenderer;
import danger.orespawn.entity.client.animation.KeyframeLayer;
import danger.orespawn.entity.client.animation.PhaseLockedKeyframeController;
import danger.orespawn.entity.pose.AlienPose;
import danger.orespawn.entity.pose.AlosaurusPose;
import danger.orespawn.entity.pose.BasiliskPose;
import danger.orespawn.entity.pose.BeePose;
import danger.orespawn.entity.pose.CamarasaurusPose;
import danger.orespawn.entity.pose.CaterKillerPose;
import danger.orespawn.entity.pose.CaveFisherPose;
import danger.orespawn.entity.pose.CephadromePose;
import danger.orespawn.entity.pose.ChipmunkPose;
import danger.orespawn.entity.pose.CrabPose;
import danger.orespawn.entity.pose.DragonPose;
import danger.orespawn.entity.pose.DungeonBeastPose;
import danger.orespawn.entity.pose.EmperorScorpionPose;
import danger.orespawn.entity.pose.EnderKnightPose;
import danger.orespawn.entity.pose.EnderReaperPose;
import danger.orespawn.entity.pose.FrogPose;
import danger.orespawn.entity.pose.GazellePose;
import danger.orespawn.entity.pose.GhostSkellyPose;
import danger.orespawn.entity.pose.GiantRobotPose;
import danger.orespawn.entity.pose.GodzillaPose;
import danger.orespawn.entity.pose.HammerheadPose;
import danger.orespawn.entity.pose.HerculesBeetlePose;
import danger.orespawn.entity.pose.HydroliscPose;
import danger.orespawn.entity.pose.KrakenPose;
import danger.orespawn.entity.pose.LeafMonsterPose;
import danger.orespawn.entity.pose.LeonPose;
import danger.orespawn.entity.pose.LizardPose;
import danger.orespawn.entity.pose.MantisPose;
import danger.orespawn.entity.pose.MolenoidPose;
import danger.orespawn.entity.pose.NastysaurusPose;
import danger.orespawn.entity.pose.OstrichPose;
import danger.orespawn.entity.pose.PeacockPose;
import danger.orespawn.entity.pose.PitchBlackPose;
import danger.orespawn.entity.pose.PointysaurusPose;
import danger.orespawn.entity.pose.RatPose;
import danger.orespawn.entity.pose.SeaMonsterPose;
import danger.orespawn.entity.pose.SeaViperPose;
import danger.orespawn.entity.pose.SpitBugPose;
import danger.orespawn.entity.pose.SpyroPose;
import danger.orespawn.entity.pose.StinkyPose;
import danger.orespawn.entity.pose.TRexPose;
import danger.orespawn.entity.pose.ThePrinceAdultPose;
import danger.orespawn.entity.pose.ThePrincePose;
import danger.orespawn.entity.pose.TriffidPose;
import danger.orespawn.entity.pose.TrooperBugPose;
import danger.orespawn.entity.pose.UrchinPose;
import danger.orespawn.entity.pose.VelocityRaptorPose;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationProcessor;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.loading.json.raw.Model;
import software.bernie.geckolib.loading.json.typeadapter.KeyFramesAdapter;
import software.bernie.geckolib.loading.object.BakedAnimations;
import software.bernie.geckolib.loading.object.BakedModelFactory;
import software.bernie.geckolib.loading.object.GeometryTree;

/**
 * The Tier-2 slices (2026-09-13): what the dedicated game-test server can pin of the fourteen Tier-2 descriptors behind
 * the dev switch - the first slice's seven (six simple-cyclic rigs, the Cockateil rig with two consumers), the second
 * slice's Firefly and Gold Fish (rejoined once ENT-S-161 landed; the Cloud Shark waits on the visual leg's tie rule) and
 * the Ant rig's five consumers (the first gait-scaled Tier-2 rig after the Beaver) - no GeckoLib ticking (a controller's {@code process} initialises
 * {@code MolangQueries}, which this server refuses: the kf17d finding; the harness's keyframe reference leg carries the
 * pose facts). Own batch {@code t2Seam} (TEST-003).
 * <ul>
 * <li>{@code t2_001}: every descriptor constructs registry-free on this server (the lazy entity-type suppliers,
 * OPT-029 R0) and declares its frequency groups exactly - count, clip names under the naming rule (the gait
 * group's clip, else the SPEC's primary group's, is the bare {@code walk}; every other group's is
 * {@code walk_<group>}: contract section 2.1 amended), omegas, the chain's wingspeed, bones, gait scaling on
 * the Ant's legs and nowhere else; a shared rig's consumers declare the owner's layers (the Ruby Bird the
 * Cockateil's, the four other ants the Ant's).</li>
 * <li>{@code t2_002}: each shipped {@code <name>.animation.json} bakes through GeckoLib's own loader and carries
 *     {@code idle} keying no bone plus exactly one looping clip per declared group, every group bone keyed.</li>
 * <li>{@code t2_003} (re-pinned by the second slice under the naming rule): under the modern keys EVERY landed species
 *     registers its declared layers under the default keys - one phase-locked controller per layer named
 *     {@code keyframe:<clip>} with the declared omega and wingspeed, exactly one of them {@code keyframe:walk} (the
 *     primary group's), the gait group's amplitude-scaled and additive over its bones (the Ant family), every other
 *     unscaled - so the Dragonfly, Cockateil and Ruby Bird gates the first slice pinned CLOSED are now OPEN; the
 *     config gate still closes them all.</li>
 * <li>{@code t2_005} (the third Tier-2 slice): the fifteen HOOK species - each descriptor constructs registry-free and
 * declares NO keyframe layer, its shipped animation file bakes to no clip (the s4 hook rigs' empty file), the self-gate
 * registers nothing under the modern keys, its shadow is the classic renderer's (ENT-S-092), the classic face order is
 * required exactly where the shipped geo carries it (TEST-007: the Cloud Shark, Bee, Fairy and Terrible Terror),
 * and the classic hook poses a fresh bake of the shipped geo through {@code OreSpawnGeoReplacement.pose} on explicit
 * {@code PoseInputs} - the pose source is the hook.</li>
 * <li>{@code t2_006} (the fourth Tier-2 slice T2d, on the hooks already written): the thirteen HOOK species landed on
 * their hooks - the Crab with its draw fix (its twenty-four explicit-form leg clones posed by the hook), the Kyuubi, Leaf
 * Monster, Alosaurus, Attack Squid, Band P, Baryonyx, Camarasaurus, Cassowary, Creeping Horror, Cryolophosaurus, Easter
 * Bunny and Flounder - each pinned exactly as {@code t2_005} pins the third slice's (no layer, an empty file, nothing
 * registered, the classic shadow, the face order on the Baryonyx and Creeping Horror, the hook moving a named bone off
 * its bind at age 7; the four entity-reading hooks on a declared subject - the Leaf Monster's attacking, since its
 * rest pose is the still bush).</li>
 * <li>{@code t2_007} (the fifth Tier-2 slice T2e, on the hooks already written): the fourteen HOOK species landed on
 * their hooks - the Pointysaurus, Whale, Molenoid, Rat, Spit Bug, Stink Bug, Trooper Bug, Velocity Raptor, Ghost Skelly,
 * Hydrolisc, Lizard, Mantis, Cave Fisher and Chipmunk (the Lurking Terror held on the visual leg, its register line) -
 * each pinned exactly as {@code t2_005} pins the third slice's (no layer, an empty file, nothing registered, the classic
 * shadow, the face order on the Trooper Bug, Velocity Raptor and Lizard (their zero-thickness cubes) and on the Ghost
 * Skelly (a blending rig, the Fairy form), the hook moving a named bone off its bind at age 7; the twelve
 * entity-reading hooks on a declared rest subject - a fresh one per RenderInfo-reading hook, so no latch scratch is
 * shared between species).</li>
 * <li>{@code t2_008} (the sixth Tier-2 slice T2f, on the hooks already written): the twelve HOOK species landed on
 * their hooks - the Ender Knight, Ender Reaper, Frog, Gazelle, Nastysaurus, Peacock, Sea Viper, Urchin, Ostrich,
 * Spyro, Stinky and Triffid (the Dungeon Beast held on its whole-model render transform and the Scorpion on the visual
 * leg, their register lines) - each pinned exactly as {@code t2_005} pins the third slice's (no layer, an empty file,
 * nothing registered, the classic shadow, the face order on the Ender Knight, Ender Reaper, Peacock, Ostrich, Spyro, Stinky
 * and Triffid (their zero-thickness cubes), the hook moving a named bone off its bind at age 7; every hook of the
 * slice reads its entity, so each poses on its own declared rest subject - a fresh one per species, so no RenderInfo
 * latch scratch is shared).</li>
 * <li>{@code t2_009} (the constant render transform, TEST-013): the descriptor's {@code renderTransform()} - the
 * identity by default; the Dungeon Beast's classic YP 90 and the Kraken's XP 90 declared in the classic
 * renderer's own terms - and its SLOT form, what {@code
 * OreSpawnGeoReplacedEntityRenderer.applyRotations} multiplies onto the pose stack: the rotation conjugated through
 * the seam frame (S_y R_y(90) S_y = R_y(90): the bake flips nothing in x; the Kraken's about the classic origin
 * 1.501 up), measured on a pose stack against the analysis's closed form and, on sample points, the classic chain (the
 * classic flip and lift, the classic rotation) against the seam chain (the slot, then the seam frame).</li>
 * <li>{@code t2_012} (the first Tier-1 slice T1a, on the hooks already written, hitbox profiles excluded): the
 * seventeen HOOK registries landed on their hooks - the Kraken, Dungeon Beast, Basilisk, Godzilla, Hammerhead, T-Rex, Leon
 * and Leonopteryx, Cephadrome, Dragon and Baby Dragon, Giant Robot and Jeffery, Pitch Black, Sea Monster, The Prince and
 * The Prince Adult (the King held on its second translucent membrane pass, the Butterfly rig on the Mothra's pair-contested
 * cap, their register lines) - each pinned exactly as {@code t2_005} pins the third slice's (no layer, an empty file,
 * nothing registered, the classic shadow, the face order on the Godzilla, Leon pair, Cephadrome, Dragon pair and Prince
 * (their zero-thickness cubes), the hook moving a named bone off its bind at age 7; every hook of the slice reads its entity,
 * so each poses on its own declared rest subject), plus the slice's own facts: the three shared consumers draw their rig's
 * geo, the Dungeon Beast's shipped geo carries none of its four undrawn toes (TEST-013), the Giant Robot pair's geo carries
 * the twenty-two render-instance clones (the Crab's form) and the Kraken's and Dungeon Beast's descriptors declare their
 * constant render transform ({@code t2_009} pins its conjugation).</li>
 *
 * <li>{@code t2_004}: the render facts the 4c precedent pinned in code - each descriptor's shadow radius is its
 *     classic renderer's constant (ENT-S-092; the Ant family's {@code 0.1 / 0.15 x SCALE} products where the classic
 *     renderer declares no SHADOW), the Cockateil and Ruby Bird sharing the Cockateil renderer's - and each shared
 *     rig's consumers share one geo, clip file and layer list.</li>
 * </ul>
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class T2SeamTests {
    private static final String BATCH = "t2Seam";
    private static final String CLIPS = "/assets/orespawn/animations/entity/";
    private static final String GEO = "/assets/orespawn/geo/entity/";
    private static final Set<String> ANT_GAIT_BONES = Set.of("llegtop1", "llegbot1", "llegtop2", "llegbot2", "llegtop3", "llegbot3",
            "rlegtop1", "rlegbot1", "rlegtop2", "rlegbot2", "rlegtop3", "rlegbot3");

    /**
     * One species' declared transcription: the descriptor, its shipped clip file, the group carrying the bare walk
     * (the gait group, else the SPEC's primary group), the gait group (null for a species without one) and the groups
     * the classic model has.
     */
    private record Species(String name, OreSpawnGeoReplacement<?> replacement, String clipFile, String primaryGroup,
                           String gaitGroup, List<String> groups, List<Float> omegas, List<Float> wingspeeds,
                           List<Set<String>> bones) {
        String expectedClip(String group) {
            return group.equals(this.primaryGroup) ? KeyframeLayer.WALK : KeyframeLayer.walkClip(group);
        }

        boolean expectedGaitScaled(String group) {
            return group.equals(this.gaitGroup);
        }
    }

    private static List<Species> species() {
        List<Species> out = new ArrayList<>();
        // the first Tier-2 slice: six simple-cyclic rigs, seven descriptors
        out.add(new Species("tshirt", new TshirtGeoReplacement(), "tshirt", "turn", null, List.of("turn"), List.of(0.05F),
                List.of(0.22F), List.of(Set.of("Shape1", "Shape2"))));
        out.add(new Species("mosquito", new MosquitoGeoReplacement(), "mosquito", "wings", null, List.of("wings"), List.of(3.0F),
                List.of(1.0F), List.of(Set.of("rightwing1", "rightwing2", "leftwing1", "leftwing2"))));
        out.add(new Species("cliff_racer", new CliffRacerGeoReplacement(), "cliffracer", "wings", null, List.of("wings"), List.of(1.3F),
                List.of(1.0F), List.of(Set.of("lwing", "rwing"))));
        out.add(new Species("brutalfly", new BrutalflyGeoReplacement(), "brutalfly", "wings", null, List.of("wings"), List.of(1.3F),
                List.of(0.2F), List.of(Set.of("rightwing", "rightwing2", "rightwing3", "rightwing4", "rightwing5", "rightwing6",
                        "leftwing", "leftwing2", "leftwing3", "leftwing4", "leftwing5", "leftwing6"))));
        out.add(new Species("dragonfly", new DragonflyGeoReplacement(), "dragonfly", "wings", null, List.of("wings", "jaws"),
                List.of(1.3F, 0.3F), List.of(2.0F, 2.0F),
                List.of(Set.of("lfwing", "rfwing", "lrwing", "rrwing"), Set.of("ljaw", "rjaw"))));
        List<String> birdGroups = List.of("wings", "tail", "feather1", "feather2", "feather3");
        List<Float> birdOmegas = List.of(1.5F, 0.3F, 1.1F, 1.2F, 1.3F);
        List<Float> birdWingspeeds = List.of(1.0F, 1.0F, 1.0F, 1.0F, 1.0F);
        List<Set<String>> birdBones = List.of(Set.of("lwing1", "lwing2", "rwing1", "rwing2"),
                Set.of("tailfeather1", "tailfeather2", "tailfeather3"), Set.of("feather1"), Set.of("feather2"), Set.of("feather3"));
        out.add(new Species("cockateil", new CockateilGeoReplacement(), "cockateil", "wings", null, birdGroups, birdOmegas, birdWingspeeds, birdBones));
        out.add(new Species("ruby_bird", new RubyBirdGeoReplacement(), "cockateil", "wings", null, birdGroups, birdOmegas, birdWingspeeds, birdBones));
        // the second Tier-2 slice: the two rigs rejoined under ENT-S-161 (the primary group of a multi-group species is
        // the SPEC's primary group - `primary_group`, the first when absent; the Cloud Shark waits on the visual leg's tie rule), and the Ant rig's five consumers
        // (a gait group: the twelve legs)
        out.add(new Species("firefly", new FireflyGeoReplacement(), "firefly", "wings", null, List.of("wings"), List.of(2.5F),
                List.of(1.0F), List.of(Set.of("wing_left", "wing_right"))));
        out.add(new Species("gold_fish", new GoldFishGeoReplacement(), "goldfish", "pectoral1", null,
                List.of("pectoral1", "pectoral2", "pectoral3", "pectoral4", "bottomfins", "jaw"),
                List.of(1.3F, 1.2F, 1.1F, 1.0F, 1.7F, 0.7F), List.of(0.7F, 0.7F, 0.7F, 0.7F, 0.7F, 0.7F),
                List.of(Set.of("Pectoralfin1"), Set.of("Pectoralfin2"), Set.of("Pectoralfin3"), Set.of("Pectoralfin4"),
                        Set.of("Bottomfin1", "Bottomfin2"), Set.of("Jaw"))));
        List<String> antGroups = List.of("gait", "jaws");
        List<Float> antOmegas = List.of(2.7F, 0.4F);
        List<Float> antWingspeeds = List.of(1.0F, 1.0F);
        List<Set<String>> antBones = List.of(ANT_GAIT_BONES, Set.of("jawsl", "jawsr"));
        out.add(new Species("ant", new AntGeoReplacement(), "ant", "gait", "gait", antGroups, antOmegas, antWingspeeds, antBones));
        out.add(new Species("rainbow_ant", new RainbowAntGeoReplacement(), "ant", "gait", "gait", antGroups, antOmegas, antWingspeeds, antBones));
        out.add(new Species("red_ant", new RedAntGeoReplacement(), "ant", "gait", "gait", antGroups, antOmegas, antWingspeeds, antBones));
        out.add(new Species("termite", new TermiteGeoReplacement(), "ant", "gait", "gait", antGroups, antOmegas, antWingspeeds, antBones));
        out.add(new Species("unstable_ant", new UnstableAntGeoReplacement(), "ant", "gait", "gait", antGroups, antOmegas, antWingspeeds, antBones));
        return out;
    }

    // ------------------------------------------------------------------ row 1: the descriptors and their groups

    @GameTest(template = "empty", batch = BATCH)
    public static void t2_001_every_descriptor_constructs_registry_free_and_declares_its_groups(GameTestHelper helper) {
        List<Species> all = species();
        helper.assertTrue(all.size() == 14, "the fourteen Tier-2 descriptors of the two slices (nine rigs; the Cockateil rig has two consumers, the Ant rig five)");
        for (Species species : all) {
            List<KeyframeLayer> layers = species.replacement().keyframeLayers();
            helper.assertTrue(layers.size() == species.groups().size(),
                    species.name() + " declares " + species.groups().size() + " frequency groups (found " + layers.size() + ")");
            Set<String> claimed = new HashSet<>();
            int bareWalks = 0;
            for (int index = 0; index < layers.size(); index++) {
                KeyframeLayer layer = layers.get(index);
                String group = species.groups().get(index);
                String expectedClip = species.expectedClip(group);
                helper.assertTrue(layer.group().equals(group), species.name() + " layer " + index + " is group " + group);
                helper.assertTrue(layer.clip().equals(expectedClip), species.name() + "/" + group + " clip is " + expectedClip
                        + " (the naming rule: the bare walk on the " + (species.gaitGroup() != null ? "gait" : "primary") + " group "
                        + species.primaryGroup() + ", walk_<group> elsewhere)");
                helper.assertTrue(layer.angularFrequencyRadiansPerTick() == species.omegas().get(index),
                        species.name() + "/" + group + " omega " + species.omegas().get(index));
                helper.assertTrue(layer.wingspeed() == species.wingspeeds().get(index),
                        species.name() + "/" + group + " wingspeed " + species.wingspeeds().get(index));
                helper.assertTrue(layer.bones().equals(species.bones().get(index)),
                        species.name() + "/" + group + " bones " + species.bones().get(index));
                helper.assertTrue(layer.gaitScaled() == species.expectedGaitScaled(group), species.name() + "/" + group
                        + (species.expectedGaitScaled(group) ? " is the gait group, scaled by limbSwingAmount" : " is unscaled (not the gait group)"));
                if (layer.clip().equals(KeyframeLayer.WALK)) {
                    bareWalks++;
                }
                for (String bone : layer.bones()) {
                    helper.assertTrue(claimed.add(bone), species.name() + ": bone " + bone + " in one group only");
                }
            }
            helper.assertTrue(bareWalks == 1, species.name() + ": exactly one group carries the bare walk (found " + bareWalks + ")");
            helper.assertTrue(new AnimatableManager<>(species.replacement()).getAnimationControllers().isEmpty(),
                    species.name() + ": the production registerControllers registers nothing on this server (no bake)");
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------ row 2: the shipped clip files

    @GameTest(template = "empty", batch = BATCH)
    public static void t2_002_every_shipped_clip_bakes_idle_plus_one_clip_per_group(GameTestHelper helper) {
        for (Species species : species()) {
            BakedAnimations shipped = bakeClips(resource(CLIPS + species.clipFile() + ".animation.json"));
            Set<String> expected = new HashSet<>();
            expected.add(KeyframeLayer.IDLE);
            for (KeyframeLayer layer : species.replacement().keyframeLayers()) {
                expected.add(layer.clip());
            }
            helper.assertTrue(shipped.animations().keySet().equals(expected),
                    species.name() + ": " + species.clipFile() + ".animation.json carries exactly " + expected + " (found "
                            + shipped.animations().keySet() + ")");
            Animation idle = shipped.getAnimation(KeyframeLayer.IDLE);
            helper.assertTrue(idle.boneAnimations().length == 0 && idle.loopType() == Animation.LoopType.LOOP,
                    species.name() + ": the transcription's idle keys no bone and loops");
            for (KeyframeLayer layer : species.replacement().keyframeLayers()) {
                Animation clip = shipped.getAnimation(layer.clip());
                helper.assertTrue(clip.loopType() == Animation.LoopType.LOOP, species.name() + "/" + layer.clip() + " loops");
                helper.assertTrue(clip.length() == 20.0D, species.name() + "/" + layer.clip() + " is authored at 1.0 s (Q14 (a))");
                Set<String> keyed = new HashSet<>();
                for (var bone : clip.boneAnimations()) {
                    keyed.add(bone.boneName());
                }
                helper.assertTrue(keyed.equals(layer.bones()),
                        species.name() + "/" + layer.clip() + " keys exactly its group's bones " + layer.bones() + " (found " + keyed + ")");
            }
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------ row 3: the gate under the naming rule

    @GameTest(template = "empty", batch = BATCH)
    public static void t2_003_gate_registers_every_landed_species_declared_layers(GameTestHelper helper) {
        Flags flags = Flags.read();
        try {
            OreSpawnConfig.MODERN_ENABLED.set(true);
            OreSpawnConfig.MODERN_ARTIST_ANIMATIONS.set(true);
            OreSpawnConfig.MODERN_CLASSIC_ANIMATION_SPECIES.set(List.of());
            for (Species species : species()) {
                BakedAnimations shipped = bakeClips(resource(CLIPS + species.clipFile() + ".animation.json"));
                List<KeyframeLayer> layers = species.replacement().keyframeLayers();
                AnimatableManager.ControllerRegistrar registrar = registrar();
                int registered = species.replacement().registerKeyframeLayers(registrar, shipped);
                helper.assertTrue(registered == layers.size() && registrar.controllers().size() == layers.size(),
                        species.name() + ": the shipped file carries idle AND the bare walk, so the gate registers every declared layer ("
                                + layers.size() + "; found " + registered + ") - the naming rule opened it");
                int bareWalks = 0;
                for (int index = 0; index < layers.size(); index++) {
                    KeyframeLayer layer = layers.get(index);
                    AnimationController<? extends GeoAnimatable> controller = registrar.controllers().get(index);
                    Set<String> scaledBones = layer.gaitScaled() ? layer.bones() : Set.of();
                    helper.assertTrue(controller instanceof PhaseLockedKeyframeController<?> locked
                                    && locked.getName().equals(layer.controllerName())
                                    && locked.angularFrequencyRadiansPerSourceTick() == layer.angularFrequencyRadiansPerTick()
                                    && locked.wingspeed() == layer.wingspeed()
                                    && locked.additive() == layer.gaitScaled()
                                    && locked.amplitudeScaledRotationBones().equals(scaledBones),
                            species.name() + "/" + layer.group() + ": the phase-locked " + layer.controllerName() + " with omega "
                                    + layer.angularFrequencyRadiansPerTick() + " x wingspeed " + layer.wingspeed()
                                    + (layer.gaitScaled() ? ", amplitude-scaled and additive over " + layer.bones() : ", unscaled"));
                    if (layer.clip().equals(KeyframeLayer.WALK)) {
                        bareWalks++;
                        helper.assertTrue(layer.group().equals(species.primaryGroup()),
                                species.name() + ": keyframe:walk is the " + (species.gaitGroup() != null ? "gait" : "primary") + " group "
                                        + species.primaryGroup() + "'s (found " + layer.group() + ")");
                    }
                }
                helper.assertTrue(bareWalks == 1, species.name() + ": exactly one keyframe:walk among the registered layers");
                // The config gate still stands in front of every species.
                OreSpawnConfig.MODERN_ARTIST_ANIMATIONS.set(false);
                helper.assertTrue(species.replacement().registerKeyframeLayers(registrar(), shipped) == 0,
                        species.name() + ": artistAnimations off registers nothing");
                OreSpawnConfig.MODERN_ARTIST_ANIMATIONS.set(true);
            }
        } finally {
            flags.restore();
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------ row 5: the hook species of the third slice

    /**
     * One hook species of the third Tier-2 slice: the descriptor, its shipped geo / animation file stem, the classic
     * renderer's shadow, whether the classic face order is required (a zero-thickness cube), the pose subject the hook
     * asks for (the rest state; null for a hook that reads no entity) and a bone the hook moves off its bind.
     */
    private record HookSpecies(String name, OreSpawnGeoReplacement<?> replacement, String file, float shadow, boolean faceOrder,
                               Object subject, String movingBone) {
    }

    /** The declared rest state of the three hooks that read {@code getAttacking()} (the harness's ProbeSubject at rest). */
    private static final class RestSubject implements BeePose, CaterKillerPose, HerculesBeetlePose {
        @Override
        public int getAttacking() {
            return 0;
        }
    }

    private static List<HookSpecies> hookSpecies() {
        RestSubject rest = new RestSubject();
        return List.of(
                // orig RenderCloudShark.java:22-23 super(model, par2 * par3) 0.5f x 1.0f: the literal the classic renderer passes (no SHADOW constant)
                new HookSpecies("cloud_shark", new CloudSharkGeoReplacement(), "cloudshark", 0.5F, true, null, "leftfin"),
                new HookSpecies("bee", new BeeGeoReplacement(), "bee", BeeRenderer.SHADOW, true, rest, "WingLeft"),
                new HookSpecies("fairy", new FairyGeoReplacement(), "fairy", FairyRenderer.SHADOW, true, null, "lwing1"),
                new HookSpecies("gamma_metroid", new GammaMetroidGeoReplacement(), "gammametroid", GammaMetroidRenderer.SHADOW, false, null, "lefttusk"),
                new HookSpecies("irukandji", new IrukandjiGeoReplacement(), "irukandji", IrukandjiRenderer.SHADOW, false, null, "t11"),
                new HookSpecies("skate", new SkateGeoReplacement(), "skate", SkateRenderer.SHADOW, false, null, "Shape1"),
                new HookSpecies("rubber_ducky", new RubberDuckyGeoReplacement(), "rubberducky", RubberDuckyRenderer.SHADOW, false, null, "lwing"),
                new HookSpecies("terrible_terror", new TerribleTerrorGeoReplacement(), "terribleterror", TerribleTerrorRenderer.SHADOW, true, null, "wing1"),
                new HookSpecies("worm_large", new WormLargeGeoReplacement(), "wormlarge", WormLargeRenderer.SHADOW, false, null, "tailtip"),
                new HookSpecies("worm_medium", new WormMediumGeoReplacement(), "wormmedium", WormMediumRenderer.SHADOW, false, null, "tail"),
                new HookSpecies("worm_small", new WormSmallGeoReplacement(), "wormsmall", WormSmallRenderer.SHADOW, false, null, "tail"),
                // CannonFodderRenderer's constructor passes the literal 0.4f (no SHADOW constant; a port-authored rig with no 1.7.10 pin)
                new HookSpecies("cannon_fodder", new CannonFodderGeoReplacement(), "cannonfodder", 0.4F, false, null, "leg_front_left"),
                new HookSpecies("cater_killer", new CaterKillerGeoReplacement(), "caterkiller", CaterKillerRenderer.SHADOW, false, rest, "ljaw"),
                new HookSpecies("cricket", new CricketGeoReplacement(), "cricket", CricketRenderer.SHADOW, false, null, "lfleg"),
                new HookSpecies("hercules_beetle", new HerculesBeetleGeoReplacement(), "herculesbeetle", HerculesBeetleRenderer.SHADOW, false, rest, "lfleg1"));
    }

    @GameTest(template = "empty", batch = BATCH)
    public static void t2_005_hook_species_declare_no_layer_register_nothing_and_pose_through_their_hooks(GameTestHelper helper) {
        Flags flags = Flags.read();
        try {
            OreSpawnConfig.MODERN_ENABLED.set(true);
            OreSpawnConfig.MODERN_ARTIST_ANIMATIONS.set(true);
            OreSpawnConfig.MODERN_CLASSIC_ANIMATION_SPECIES.set(List.of());
            List<HookSpecies> all = hookSpecies();
            helper.assertTrue(all.size() == 15, "the fifteen hook descriptors of the third Tier-2 slice");
            for (HookSpecies species : all) {
                assertHookSpecies(helper, species);
            }
        } finally {
            flags.restore();
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------ row 6: the hook species of the fourth slice (T2d)

    /**
     * The declared state of the four T2d hooks that read their entity: the Crab's, Alosaurus's and Leaf Monster's attacking flag
     * and the Camarasaurus's health ratio (the harness's ProbeSubject: full health, 20 / 20).
     */
    private static final class HookSubject implements CrabPose, AlosaurusPose, LeafMonsterPose, CamarasaurusPose {
        private final int attacking;

        private HookSubject(int attacking) {
            this.attacking = attacking;
        }

        @Override
        public int getAttacking() {
            return this.attacking;
        }

        @Override
        public float getHealth() {
            return 20.0F;
        }

        @Override
        public float getMaxHealth() {
            return 20.0F;
        }
    }

    private static List<HookSpecies> hookSpeciesT2d() {
        HookSubject rest = new HookSubject(0);
        HookSubject attacking = new HookSubject(1);
        return List.of(
                // the Crab: the eight leg poses land on the explicit-form clones (leg1__i0 is draw 0, the left side's front leg)
                new HookSpecies("crab", new CrabGeoReplacement(), "crab", CrabRenderer.SHADOW, false, rest, "leg1__i0"),
                new HookSpecies("kyuubi", new KyuubiGeoReplacement(), "kyuubi", KyuubiRenderer.SHADOW, false, null, "tail1"),
                // the Leaf Monster at rest is the still bush (every rotation 0): the attacking branch moves its legs
                new HookSpecies("leaf_monster", new LeafMonsterGeoReplacement(), "leafmonster", LeafMonsterRenderer.SHADOW, false, attacking, "lleg"),
                // AlosaurusRenderer's constructor passes the literal 1.0f (no SHADOW constant)
                new HookSpecies("alosaurus", new AlosaurusGeoReplacement(), "alosaurus", 1.0F, false, rest, "rightleg"),
                new HookSpecies("attack_squid", new AttackSquidGeoReplacement(), "attacksquid", AttackSquidRenderer.SHADOW, false, null, "tent1"),
                new HookSpecies("band_p", new BandPGeoReplacement(), "bandp", BandPRenderer.SHADOW, false, null, "lleg"),
                // BaryonyxRenderer's constructor passes the literal 1.0f (no SHADOW constant); twenty-six zero-thickness cubes
                new HookSpecies("baryonyx", new BaryonyxGeoReplacement(), "baryonyx", 1.0F, true, null, "shape24"),
                // orig RenderCamarasaurus.java:23 super(model, par2 * par3) 0.65f x 0.65f: the product the classic renderer passes
                new HookSpecies("camarasaurus", new CamarasaurusGeoReplacement(), "camarasaurus", 0.65F * 0.65F, false, rest, "FLegupleft"),
                // orig RenderCassowary.java:23 super(model, par2 * par3) 0.5f x 1.0f: the literal the classic renderer passes
                new HookSpecies("cassowary", new CassowaryGeoReplacement(), "cassowary", 0.5F, false, null, "leg1"),
                new HookSpecies("creeping_horror", new CreepingHorrorGeoReplacement(), "creepinghorror", CreepingHorrorRenderer.SHADOW, true, null, "leg1"),
                // orig RenderCryolophosaurus.java:23 super(model, par2 * par3) 0.75f x 0.5f: the product the classic renderer passes
                new HookSpecies("cryolophosaurus", new CryolophosaurusGeoReplacement(), "cryolophosaurus", 0.75F * 0.5F, false, null, "rightleg"),
                new HookSpecies("easter_bunny", new EasterBunnyGeoReplacement(), "easterbunny", EasterBunnyRenderer.SHADOW, false, null, "lleg"),
                new HookSpecies("flounder", new FlounderGeoReplacement(), "flounder", FlounderRenderer.SHADOW, false, null, "lfin"));
    }

    @GameTest(template = "empty", batch = BATCH)
    public static void t2_006_fourth_slice_hook_species_declare_no_layer_register_nothing_and_pose_through_their_hooks(GameTestHelper helper) {
        Flags flags = Flags.read();
        try {
            OreSpawnConfig.MODERN_ENABLED.set(true);
            OreSpawnConfig.MODERN_ARTIST_ANIMATIONS.set(true);
            OreSpawnConfig.MODERN_CLASSIC_ANIMATION_SPECIES.set(List.of());
            List<HookSpecies> all = hookSpeciesT2d();
            helper.assertTrue(all.size() == 13, "the thirteen hook descriptors landed by the fourth Tier-2 slice");
            for (HookSpecies species : all) {
                assertHookSpecies(helper, species);
            }
        } finally {
            flags.restore();
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------ row 7: the hook species of the fifth slice (T2e)

    /**
     * The declared rest state of the twelve T2e hooks that read their entity: the attacking flag 0, full health (the
     * harness's ProbeSubject: 20 / 20), not sitting, a fresh RenderInfo latch and the entity RNG seeded 0 (the probe's rest
     * subject). One instance per RenderInfo-reading hook (the Ghost Skelly and Cave Fisher write the
     * latch's scratch), so the species do not share it.
     */
    private static final class RestSubjectT2e implements PointysaurusPose, MolenoidPose, RatPose, SpitBugPose, TrooperBugPose,
            VelocityRaptorPose, GhostSkellyPose, HydroliscPose, LizardPose, MantisPose, CaveFisherPose,
            ChipmunkPose {
        private final RenderInfo renderInfo = new RenderInfo();
        private final RandomSource random = RandomSource.create(0L);

        @Override
        public int getAttacking() {
            return 0;
        }

        @Override
        public float getHealth() {
            return 20.0F;
        }

        @Override
        public float getMaxHealth() {
            return 20.0F;
        }

        @Override
        public boolean isInSittingPose() {
            return false;
        }

        @Override
        public RenderInfo getRenderInfo() {
            return this.renderInfo;
        }

        @Override
        public RandomSource getRandom() {
            return this.random;
        }
    }

    private static List<HookSpecies> hookSpeciesT2e() {
        return List.of(
                // PointysaurusRenderer's constructor passes the literal 1.0f (no SHADOW constant)
                new HookSpecies("pointysaurus", new PointysaurusGeoReplacement(), "pointysaurus", 1.0F, false, new RestSubjectT2e(), "lfleg"),
                new HookSpecies("whale", new WhaleGeoReplacement(), "whale", WhaleRenderer.SHADOW, false, null, "lfin2"),
                new HookSpecies("molenoid", new MolenoidGeoReplacement(), "molenoid", MolenoidRenderer.SHADOW, false, new RestSubjectT2e(), "lleg"),
                new HookSpecies("rat", new RatGeoReplacement(), "rat", RatRenderer.SHADOW, false, new RestSubjectT2e(), "rfleg"),
                new HookSpecies("spit_bug", new SpitBugGeoReplacement(), "spitbug", SpitBugRenderer.SHADOW, false, new RestSubjectT2e(), "leg2"),
                new HookSpecies("stink_bug", new StinkBugGeoReplacement(), "stinkbug", StinkBugRenderer.SHADOW, false, null, "f1"),
                // the Trooper Bug's two zero-thickness upper-arm parts; the antenna moves on the resting branch
                new HookSpecies("trooper_bug", new TrooperBugGeoReplacement(), "trooperbug", TrooperBugRenderer.SHADOW, true, new RestSubjectT2e(), "antenna2part2"),
                // the Velocity Raptor's fourteen zero-thickness feathers
                new HookSpecies("velocity_raptor", new VelocityRaptorGeoReplacement(), "velocityraptor", VelocityRaptorRenderer.SHADOW, true, new RestSubjectT2e(), "bl1"),
                // the Ghost Skelly at rest: the head latch holds 0, the arm groups sway; a blending rig, so the classic face order
                // is required although no cube is zero-thickness (the Fairy form, ENT-S-146)
                new HookSpecies("ghost_skelly", new GhostSkellyGeoReplacement(), "ghostskelly", GhostSkellyRenderer.SHADOW, true, new RestSubjectT2e(), "larm"),
                new HookSpecies("hydrolisc", new HydroliscGeoReplacement(), "hydrolisc", HydroliscRenderer.SHADOW, false, new RestSubjectT2e(), "lf1"),
                // the Lizard's eight zero-thickness fins (never written by the hook)
                new HookSpecies("lizard", new LizardGeoReplacement(), "lizard", LizardRenderer.SHADOW, true, new RestSubjectT2e(), "TopFrontLeftLeg"),
                new HookSpecies("mantis", new MantisGeoReplacement(), "mantis", MantisRenderer.SHADOW, false, new RestSubjectT2e(), "lfwing"),
                new HookSpecies("cave_fisher", new CaveFisherGeoReplacement(), "cavefisher", CaveFisherRenderer.SHADOW, false, new RestSubjectT2e(), "LFLeg1"),
                new HookSpecies("chipmunk", new ChipmunkGeoReplacement(), "chipmunk", ChipmunkRenderer.SHADOW, false, new RestSubjectT2e(), "leg1"));
    }

    @GameTest(template = "empty", batch = BATCH)
    public static void t2_007_fifth_slice_hook_species_declare_no_layer_register_nothing_and_pose_through_their_hooks(GameTestHelper helper) {
        Flags flags = Flags.read();
        try {
            OreSpawnConfig.MODERN_ENABLED.set(true);
            OreSpawnConfig.MODERN_ARTIST_ANIMATIONS.set(true);
            OreSpawnConfig.MODERN_CLASSIC_ANIMATION_SPECIES.set(List.of());
            List<HookSpecies> all = hookSpeciesT2e();
            helper.assertTrue(all.size() == 14, "the fourteen hook descriptors landed by the fifth Tier-2 slice (the Lurking Terror held on the visual leg)");
            for (HookSpecies species : all) {
                assertHookSpecies(helper, species);
            }
        } finally {
            flags.restore();
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------ row 8: the hook species of the sixth slice (T2f)

    /**
     * The declared rest state of the twelve T2f hooks (every one reads its entity): the attacking flag 0, not screaming,
     * not singing, no vertical velocity, not crouching, the display flag 0, not sitting, not activated, not ridden, at rest
     * on the origin with no movement delta, activity 0, closed; a fresh RenderInfo latch and the entity / level RNG seeded
     * 0 (the probe's rest subject). One instance per species (the Nastysaurus and the Ostrich write the latch's scratch).
     */
    private static final class RestSubjectT2f implements EnderKnightPose, EnderReaperPose, FrogPose, GazellePose, NastysaurusPose,
            PeacockPose, SeaViperPose, UrchinPose, OstrichPose, SpyroPose, StinkyPose, TriffidPose {
        private final RenderInfo renderInfo = new RenderInfo();
        private final RandomSource random = RandomSource.create(0L);

        @Override
        public boolean isScreaming() {
            return false;
        }

        @Override
        public int getSinging() {
            return 0;
        }

        @Override
        public Vec3 getDeltaMovement() {
            return Vec3.ZERO;
        }

        @Override
        public boolean isCrouching() {
            return false;
        }

        @Override
        public RenderInfo getRenderInfo() {
            return this.renderInfo;
        }

        @Override
        public int getAttacking() {
            return 0;
        }

        @Override
        public RandomSource getLevelRandom() {
            return this.random;
        }

        @Override
        public RandomSource getRandom() {
            return this.random;
        }

        @Override
        public int getBlink() {
            return 0;
        }

        @Override
        public boolean isInSittingPose() {
            return false;
        }

        @Override
        public int getIsActivated() {
            return 0;
        }

        @Override
        public boolean isVehicle() {
            return false;
        }

        @Override
        public double getX() {
            return 0.0D;
        }

        @Override
        public double getZ() {
            return 0.0D;
        }

        @Override
        public double xOld() {
            return 0.0D;
        }

        @Override
        public double zOld() {
            return 0.0D;
        }

        @Override
        public float getYRot() {
            return 0.0F;
        }

        @Override
        public float yRotO() {
            return 0.0F;
        }

        @Override
        public int getActivity() {
            return 0;
        }

        @Override
        public int getOpenClosed() {
            return 0;
        }
    }

    private static List<HookSpecies> hookSpeciesT2f() {
        return List.of(
                // the Ender Knight's zero-thickness cape piece; the left foot swings on the threshold gait
                new HookSpecies("ender_knight", new EnderKnightGeoReplacement(), "enderknight", EnderKnightRenderer.SHADOW, true, new RestSubjectT2f(), "lfoot1"),
                // the Ender Reaper's four zero-thickness parts; the scythe rolls on 1 - |gait|
                new HookSpecies("ender_reaper", new EnderReaperGeoReplacement(), "enderreaper", EnderReaperRenderer.SHADOW, true, new RestSubjectT2f(), "scythe1"),
                new HookSpecies("frog", new FrogGeoReplacement(), "frog", FrogRenderer.SHADOW, false, new RestSubjectT2f(), "lfleg"),
                new HookSpecies("gazelle", new GazelleGeoReplacement(), "gazelle", GazelleRenderer.SHADOW, false, new RestSubjectT2f(), "lfleg1"),
                // NastysaurusRenderer's constructor passes the literal 1.0f * 1.5f (no SHADOW constant)
                new HookSpecies("nastysaurus", new NastysaurusGeoReplacement(), "nastysaurus", 1.0F * 1.5F, false, new RestSubjectT2f(), "leftleg3"),
                // the Peacock's ten zero-thickness feathers (folded at rest); the legs swing on the threshold gait
                new HookSpecies("peacock", new PeacockGeoReplacement(), "peacock", PeacockRenderer.SHADOW, true, new RestSubjectT2f(), "lleg"),
                new HookSpecies("sea_viper", new SeaViperGeoReplacement(), "seaviper", SeaViperRenderer.SHADOW, false, new RestSubjectT2f(), "tBase"),
                // the Urchin's centre spins (age x 0.02) mod 2 pi at rest
                new HookSpecies("urchin", new UrchinGeoReplacement(), "urchin", UrchinRenderer.SHADOW, false, new RestSubjectT2f(), "center"),
                // the Ostrich's eleven zero-thickness parts; the tail sways on the 0.05 cosine (the gait reads the movement delta, 0 here)
                new HookSpecies("ostrich", new OstrichGeoReplacement(), "ostrich", OstrichRenderer.SHADOW, true, new RestSubjectT2f(), "Tail1"),
                // the Spyro's and Stinky's two zero-thickness wings; ws = limbSwingAmount = 1 here, so the wings beat
                new HookSpecies("spyro", new SpyroGeoReplacement(), "spyro", SpyroRenderer.SHADOW, true, new RestSubjectT2f(), "WingLeft"),
                new HookSpecies("stinky", new StinkyGeoReplacement(), "stinky", StinkyRenderer.SHADOW, true, new RestSubjectT2f(), "Rwing"),
                // the Triffid's eleven zero-thickness leaf tips (never written); the first leaf link folds by the closed constant
                new HookSpecies("triffid", new TriffidGeoReplacement(), "triffid", TriffidRenderer.SHADOW, true, new RestSubjectT2f(), "l1"));
    }

    @GameTest(template = "empty", batch = BATCH)
    public static void t2_008_sixth_slice_hook_species_declare_no_layer_register_nothing_and_pose_through_their_hooks(GameTestHelper helper) {
        Flags flags = Flags.read();
        try {
            OreSpawnConfig.MODERN_ENABLED.set(true);
            OreSpawnConfig.MODERN_ARTIST_ANIMATIONS.set(true);
            OreSpawnConfig.MODERN_CLASSIC_ANIMATION_SPECIES.set(List.of());
            List<HookSpecies> all = hookSpeciesT2f();
            helper.assertTrue(all.size() == 12, "the twelve hook descriptors landed by the sixth Tier-2 slice (the Dungeon Beast and the Scorpion held)");
            for (HookSpecies species : all) {
                assertHookSpecies(helper, species);
            }
        } finally {
            flags.restore();
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------ row 11: the hook species of the FK slice (the hierarchies)

    /**
     * The declared rest state of the FK slice's two hooks (both read their entity): the attacking flag 0, a fresh RenderInfo
     * latch and the entity RNG seeded 0 (the probe's rest subject). One instance per species (both hooks write the latch's
     * scratch when their selector rhythm crosses zero).
     */
    private static final class RestSubjectFk implements AlienPose, EmperorScorpionPose {
        private final RenderInfo renderInfo = new RenderInfo();
        private final RandomSource random = RandomSource.create(0L);

        @Override
        public int getAttacking() {
            return 0;
        }

        @Override
        public RenderInfo getRenderInfo() {
            return this.renderInfo;
        }

        @Override
        public RandomSource getRandom() {
            return this.random;
        }
    }

    /** The FK slice's shipped geos parent every declared chain child (the converter's hierarchy form): the link count per geo stem. */
    private static final Map<String, Integer> FK_LINKS = Map.of("emperorscorpion", 52, "alien", 26);

    private static List<HookSpecies> hookSpeciesFk() {
        return List.of(
                // the Emperor Scorpion: Leg1Seg2 (a chain child of Leg1Seg1) yaws on the four-phase gait at limbSwingAmount 1
                new HookSpecies("emperor_scorpion", new EmperorScorpionGeoReplacement(), "emperorscorpion", EmperorScorpionRenderer.SHADOW, false, new RestSubjectFk(), "Leg1Seg2"),
                // the Alien rig's two registries on one geo: tail2 (a chain child of tail1) yaws on the slow tail sway (ri2 0 at rest)
                new HookSpecies("alien", new AlienGeoReplacement(), "alien", AlienRenderer.SHADOW, false, new RestSubjectFk(), "tail2"),
                new HookSpecies("alien_boss", new AlienBossGeoReplacement(), "alien", AlienRenderer.SHADOW, false, new RestSubjectFk(), "tail2"));
    }

    /**
     * THE FK SLICE (under the hierarchy rules): the t2_005 pins on the three hook descriptors (registry-free construction,
     * no keyframe layer, the empty clip file, the self-gate registering nothing, the classic shadow, no face-order key, the
     * shipped geo baked through GeckoLib's loader and sorted into the G2 key by DrawOrder.apply - which accepts a key
     * only when it is a pre-order of the bake's tree: a hierarchy rig draws parent-first - and the classic hook moving a
     * bone off its bind at age 7) and the hierarchy itself: the shipped geo parents exactly the declared chain children
     * (the Emperor Scorpion's 52 links, the Alien's 26; one geo for the Alien's two registries), and the moving bone is a
     * CHAIN CHILD whose local rotation FlatRig resolved from the classic flat pose.
     */
    @GameTest(template = "empty", batch = BATCH)
    public static void t2_011_fk_slice_hook_species_are_parent_child_hierarchies_posed_through_their_hooks(GameTestHelper helper) {
        Flags flags = Flags.read();
        try {
            OreSpawnConfig.MODERN_ENABLED.set(true);
            OreSpawnConfig.MODERN_ARTIST_ANIMATIONS.set(true);
            OreSpawnConfig.MODERN_CLASSIC_ANIMATION_SPECIES.set(List.of());
            List<HookSpecies> all = hookSpeciesFk();
            helper.assertTrue(all.size() == 3, "the three hook descriptors landed by the FK slice (the Emperor Scorpion, the Alien, the Alien Boss)");
            for (HookSpecies species : all) {
                assertHookSpecies(helper, species);
                JsonObject geo = JsonParser.parseString(resource(GEO + species.file() + ".geo.json")).getAsJsonObject();
                int parented = 0;
                for (JsonElement bone : geo.getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject().getAsJsonArray("bones")) {
                    if (bone.getAsJsonObject().has("parent")) {
                        parented++;
                    }
                }
                helper.assertTrue(parented == FK_LINKS.get(species.file()),
                        species.name() + ": the shipped geo parents " + FK_LINKS.get(species.file()) + " chain children (found " + parented + ")");
            }
            helper.assertTrue(new AlienGeoReplacement().descriptor().modelResource().equals(new AlienBossGeoReplacement().descriptor().modelResource()),
                    "the Alien Boss draws the Alien's geo (one hierarchy for the two registries)");
        } finally {
            flags.restore();
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------ row 9: the constant render transform's slot

    /**
     * The constant render transform's conjugation, measured on matrices (TEST-013; the seam's {@code
     * OreSpawnGeoReplacedEntityRenderer.applyRotations} path: {@code
     * descriptor.renderTransform.applySlot(poseStack)} after the entity yaw and the descriptor's own rotations, then the
     * height compensation). RE-PINNED UNDER TEST-015: the classic path rotates INSIDE {@code renderToBuffer}, after
     * {@code LivingEntityRenderer.render}'s {@code scale(-1, -1, 1)} flip (21.1.223 offsets 395-400) and {@code
     * translate(0, -1.501, 0)} lift (413-417) - M; the slot runs in entity space before the seam's chain, which after
     * the slot is {@code translate(0, comp, 0) translate(0, 0.01, 0) B} with B the bake map {@code (x, y, z) ->
     * (-x, 1.5 - y, z)} (the converter's Bedrock convention and the baker's x negation, {@code
     * BakedModelFactory$Builtin.constructCube} 98-139) - and that product IS M, so the seam frame F is M itself and the
     * slot carries {@code M C M^-1}: {@code S R_y(90) S = R_y(-90)} for the Dungeon Beast's YP 90 (S = diag(-1, -1, 1) is
     * a half turn about Z, which reverses the sense of a rotation about Y; before TEST-015, with F the y flip alone, the
     * slot was YP +90 and the seam drew the rig facing the other way in the entity frame the classic mirrors), and
     * translate(0, 1.501, 1.501) R_x(-90) for the Kraken's XP 90 (unchanged: the mirror in x commutes with a rotation
     * about X, the mirror in y reverses it; the rotation about the classic origin, 1.501 up). Asserted two ways: the
     * slot matrix against the closed form, and the two chains on sample points - M, C against slot, M (the frame is the
     * bytecode's; t2_010 measures that the seam's chain after the slot is M).
     */
    @GameTest(template = "empty", batch = BATCH)
    public static void t2_009_constant_render_transform_slot_is_the_conjugated_classic_rotation(GameTestHelper helper) {
        GeoReplacementDescriptor.RenderTransform none = new AntGeoReplacement().descriptor().renderTransform();
        helper.assertTrue(none.isIdentity(), "a descriptor that declares no render transform answers the identity (the default)");
        helper.assertTrue(new Matrix4f().equals(none.slotMatrix(), 0.0F), "the identity's slot form is the identity matrix");

        GeoReplacementDescriptor.RenderTransform beast = new DungeonBeastGeoReplacement().descriptor().renderTransform();
        helper.assertTrue(beast.equals(GeoReplacementDescriptor.RenderTransform.rotationDegrees(0.0F, 90.0F, 0.0F)),
                "the Dungeon Beast declares the classic renderToBuffer's YP 90 (ModelDungeonBeast.java:535, orig :574)");
        assertSlot(helper, "dungeon_beast", beast, new Matrix4f().rotateY((float) Math.toRadians(-90.0)));

        GeoReplacementDescriptor.RenderTransform kraken = new KrakenGeoReplacement().descriptor().renderTransform();
        helper.assertTrue(kraken.equals(GeoReplacementDescriptor.RenderTransform.rotationDegrees(90.0F, 0.0F, 0.0F)),
                "the Kraken declares the classic renderToBuffer's XP 90 (ModelKraken.java:733, orig :1137)");
        assertSlot(helper, "kraken", kraken,
                new Matrix4f().translate(0.0F, 1.501F, 1.501F).rotateX((float) Math.toRadians(-90.0)));
        helper.succeed();
    }

    /** The slot matrix equals the analysis's closed form, and the classic and seam chains agree on sample points. */
    private static void assertSlot(GameTestHelper helper, String name, GeoReplacementDescriptor.RenderTransform transform,
                                   Matrix4f analysis) {
        Matrix4f slot = transform.slotMatrix();
        helper.assertTrue(analysis.equals(slot, 1.0e-5F),
                name + ": the slot form is the conjugated rotation the order analysis gives, " + analysis + " (found "
                        + slot + ")");
        // The chains on matrices (the pose-stack calls post-multiply exactly as these do; PoseStack is a client class the
        // dedicated-server gametests cannot load): the seam frame M then the classic rotation, against the slot then M.
        Matrix4f frame = GeoReplacementDescriptor.RenderTransform.seamFrame();
        Matrix4f classicChain = new Matrix4f(frame).mul(transform.classicMatrix());
        Matrix4f seamChain = new Matrix4f(slot).mul(frame);
        for (Vector4f point : List.of(new Vector4f(0.0F, 0.0F, 0.0F, 1.0F), new Vector4f(1.0F, 2.0F, 3.0F, 1.0F),
                new Vector4f(-0.5F, 1.25F, -2.0F, 1.0F))) {
            Vector4f left = new Vector4f(point).mul(classicChain);
            Vector4f right = new Vector4f(point).mul(seamChain);
            helper.assertTrue(left.distance(right) < 1.0e-5F, name + ": the classic chain (flip, lift, then the classic rotation) "
                    + "and the seam chain (the slot, then the seam frame) place " + point + " alike: " + left + " vs " + right);
        }
    }

    // ------------------------------------------------------------------ row 10: the seam's chain is the classic's (TEST-015)

    /**
     * THE MIRROR FIXED AT THE SOURCE AND THE SEAM'S HEIGHT COMPENSATION, measured on matrices (TEST-015). From
     * the bytecode: the classic chain carries a ModelPart-space point p to {@code M p}, {@code M = scale(-1, -1,
     * 1) translate(0, -1.501, 0)} ({@code LivingEntityRenderer.render} 395-400 / 413-417,
     * 21.1.223); the seam's chain after the descriptor slot is the height compensation ({@code
     * OreSpawnGeoReplacedEntityRenderer.applyRotations}), GeckoLib's {@code translate(0, 0.01,
     * 0)} ({@code GeoReplacedEntityRenderer.actuallyRender} 722-727, 4.8.4) and the bake, which places a converted cube's
     * corner at {@code B p = (-x, 1.5 - y, z)} (the converter's Bedrock convention: the cube keeps its ModelPart x and
     * its y is measured up from the 24-unit datum; the baker negates x, {@code
     * BakedModelFactory$Builtin.constructCube} 98-139). Pinned: (1) the frame constant the record holds is M (a rig
     * point mirrors in x and y and rises 1.501); (2) with the compensation the seam's chain equals M on sample points
     * to float precision - the two renderers draw every rig in the same place; (3) WITHOUT it the two chains differ
     * by exactly the 0.009 blocks of height every seam rig carried before this landing (and by nothing else), the
     * measured residual the compensation closes; (4) the bake map itself mirrors x: a point at ModelPart x = +2
     * bakes to -2, where the classic's flip also draws it - the left-right mirror of TEST-015 closed at the source.
     */
    @GameTest(template = "empty", batch = BATCH)
    public static void t2_010_seam_chain_equals_the_classic_chain_with_the_height_compensation(GameTestHelper helper) {
        Matrix4f classic = new Matrix4f().scale(-1.0F, -1.0F, 1.0F).translate(0.0F, -1.501F, 0.0F);
        helper.assertTrue(classic.equals(GeoReplacementDescriptor.RenderTransform.seamFrame(), 0.0F),
                "the seam frame F is vanilla's own M: scale(-1, -1, 1) then translate(0, -1.501, 0)");
        float compensation = GeoReplacementDescriptor.RenderTransform.SEAM_HEIGHT_COMPENSATION;
        helper.assertTrue(Math.abs(compensation + 0.009F) < 1.0e-6F,
                "the compensation is 1.501 - 1.5 - 0.01 = -0.009 blocks (found " + compensation + ")");
        Matrix4f bake = GeoReplacementDescriptor.RenderTransform.bakeOfClassic();
        Matrix4f seam = new Matrix4f().translate(0.0F, compensation, 0.0F).translate(0.0F, 0.01F, 0.0F).mul(bake);
        Matrix4f seamUncompensated = new Matrix4f().translate(0.0F, 0.01F, 0.0F).mul(bake);
        for (Vector4f point : List.of(new Vector4f(0.0F, 0.0F, 0.0F, 1.0F), new Vector4f(2.0F, 0.5F, -1.0F, 1.0F),
                new Vector4f(-0.75F, 1.5F, 0.25F, 1.0F), new Vector4f(0.125F, -0.375F, 3.0F, 1.0F))) {
            Vector4f left = new Vector4f(point).mul(classic);
            Vector4f right = new Vector4f(point).mul(seam);
            helper.assertTrue(left.distance(right) < 1.0e-6F, "with the compensation the seam's chain places " + point
                    + " where the classic chain does: " + left + " vs " + right);
            Vector4f drifted = new Vector4f(point).mul(seamUncompensated);
            helper.assertTrue(Math.abs(drifted.x - left.x) < 1.0e-6F && Math.abs(drifted.z - left.z) < 1.0e-6F
                            && Math.abs((drifted.y - left.y) - 0.009F) < 1.0e-6F,
                    "without the compensation the seam's chain sits exactly 0.009 blocks above the classic's at " + point
                            + " and nowhere else: " + drifted + " vs " + left);
        }
        Vector4f right = new Vector4f(2.0F, 0.0F, 0.0F, 1.0F).mul(bake);
        Vector4f classicRight = new Vector4f(2.0F, 0.0F, 0.0F, 1.0F).mul(classic);
        helper.assertTrue(Math.abs(right.x + 2.0F) < 1.0e-6F && Math.abs(classicRight.x + 2.0F) < 1.0e-6F,
                "a point at ModelPart x = +2 bakes to x = -2, the side the classic's flip draws it on (found " + right.x
                        + " and " + classicRight.x + ")");
        helper.succeed();
    }

    // ------------------------------------------------------------------ row 12: the hook registries of the first Tier-1 slice (T1a)

    /**
     * The declared rest state of the seventeen T1a hooks (every one reads its entity): the attacking flag 0, activity 0, not
     * sitting, not ordered to sit, not ridden, at rest on the origin with no movement delta and no yaw delta, the head
     * extensions 0, the Pitch Black's size tier 1.0; a fresh RenderInfo latch and the entity RNG seeded 0 (the probe's rest
     * subject). One instance per registry (the Kraken, the Dungeon Beast and the Pitch Black write the latch's scratch).
     */
    private static final class RestSubjectT1a implements KrakenPose, DungeonBeastPose, BasiliskPose, GodzillaPose, HammerheadPose,
            TRexPose, LeonPose, CephadromePose, DragonPose, GiantRobotPose, PitchBlackPose, SeaMonsterPose, ThePrincePose,
            ThePrinceAdultPose {
        private final RenderInfo renderInfo = new RenderInfo();
        private final RandomSource random = RandomSource.create(0L);

        @Override
        public RenderInfo getRenderInfo() {
            return this.renderInfo;
        }

        @Override
        public int getAttacking() {
            return 0;
        }

        @Override
        public RandomSource getRandom() {
            return this.random;
        }

        @Override
        public int getActivity() {
            return 0;
        }

        @Override
        public int getBeingRidden() {
            return 0;
        }

        @Override
        public boolean isInSittingPose() {
            return false;
        }

        @Override
        public boolean isOrderedToSit() {
            return false;
        }

        @Override
        public float getYRot() {
            return 0.0F;
        }

        @Override
        public float getYRotO() {
            return 0.0F;
        }

        @Override
        public float yRotO() {
            return 0.0F;
        }

        @Override
        public double getX() {
            return 0.0D;
        }

        @Override
        public double getZ() {
            return 0.0D;
        }

        @Override
        public double xOld() {
            return 0.0D;
        }

        @Override
        public double zOld() {
            return 0.0D;
        }

        @Override
        public float getPitchBlackScale() {
            return 1.0F;
        }

        @Override
        public int getHead1Ext() {
            return 0;
        }

        @Override
        public int getHead2Ext() {
            return 0;
        }

        @Override
        public int getHead3Ext() {
            return 0;
        }
    }

    private static List<HookSpecies> hookSpeciesT1a() {
        return List.of(
                // the Kraken: the right fin rolls on the 0.43 cosine (the whole-model XP 90 is the descriptor's render transform, t2_009)
                new HookSpecies("kraken", new KrakenGeoReplacement(), "kraken", KrakenRenderer.SHADOW, false, new RestSubjectT1a(), "Finright"),
                // the Dungeon Beast: the right heel rolls on the gait at limbSwingAmount 1 (the four undrawn toes are not in the geo)
                new HookSpecies("dungeon_beast", new DungeonBeastGeoReplacement(), "dungeonbeast", DungeonBeastRenderer.SHADOW, false, new RestSubjectT1a(), "rheel"),
                new HookSpecies("basilisk", new BasiliskGeoReplacement(), "basilisk", BasiliskRenderer.SHADOW, false, new RestSubjectT1a(), "body1"),
                // the Godzilla's twelve zero-thickness back spikes; the left lower leg pitches on the threshold gait
                new HookSpecies("godzilla", new GodzillaGeoReplacement(), "godzilla", GodzillaRenderer.SHADOW, true, new RestSubjectT1a(), "LLowerLeg"),
                new HookSpecies("hammerhead", new HammerheadGeoReplacement(), "hammerhead", HammerheadRenderer.SHADOW, false, new RestSubjectT1a(), "leg_1"),
                new HookSpecies("trex", new TRexGeoReplacement(), "trex", TRexRenderer.SHADOW, false, new RestSubjectT1a(), "rightleg"),
                // the Leon rig's two registries on one geo (four zero-thickness sails); orig RenderLeon.java:22-25 shadow 1.0f x 1.75f, the
                // literal LeonRenderer's constructor passes (no SHADOW constant): the equal literal
                new HookSpecies("leon", new LeonGeoReplacement(), "leon", 1.75F, true, new RestSubjectT1a(), "leg_1_L"),
                new HookSpecies("leonopteryx", new LeonopteryxGeoReplacement(), "leon", 1.75F, true, new RestSubjectT1a(), "leg_1_L"),
                // orig RenderCephadrome.java:23 shadow 1.25f x 1.0f: the literal CephadromeRenderer's constructor passes (no SHADOW constant)
                new HookSpecies("cephadrome", new CephadromeGeoReplacement(), "cephadrome", 1.25F, true, new RestSubjectT1a(), "tail1"),
                // the Dragon rig's two registries on one geo (the zero-thickness tail fin); the Baby Dragon's constructor literal 0.6f
                new HookSpecies("dragon", new DragonGeoReplacement(), "dragon", DragonRenderer.SHADOW, true, new RestSubjectT1a(), "wing1"),
                new HookSpecies("baby_dragon", new BabyDragonGeoReplacement(), "dragon", 0.6F, true, new RestSubjectT1a(), "wing1"),
                // the Giant Robot rig's two registries on one geo of twenty-nine bones (the twenty-two render-instance clones); the hip yaws
                new HookSpecies("giant_robot", new GiantRobotGeoReplacement(), "giantrobot", GiantRobotRenderer.SHADOW, false, new RestSubjectT1a(), "hip"),
                new HookSpecies("jeffery", new JefferyGeoReplacement(), "giantrobot", GiantRobotRenderer.SHADOW, false, new RestSubjectT1a(), "hip"),
                new HookSpecies("pitch_black", new PitchBlackGeoReplacement(), "pitchblack", PitchBlackRenderer.SHADOW, false, new RestSubjectT1a(), "wing1"),
                new HookSpecies("sea_monster", new SeaMonsterGeoReplacement(), "seamonster", SeaMonsterRenderer.SHADOW, false, new RestSubjectT1a(), "BottomJaw"),
                // ThePrinceRenderer's constructor passes the literal 0.75f * 0.75f (no SHADOW constant); six zero-thickness wing parts
                new HookSpecies("the_prince", new ThePrinceGeoReplacement(), "theprince", 0.75F * 0.75F, true, new RestSubjectT1a(), "Rwing"),
                // ThePrinceAdultRenderer's constructor passes the literal 1.2f (no SHADOW constant)
                new HookSpecies("the_prince_adult", new ThePrinceAdultGeoReplacement(), "theprinceadult", 1.2F, false, new RestSubjectT1a(), "LUpperLeg"));
    }

    /** The Dungeon Beast's four compiled-but-never-drawn toe parts (TEST-013): omitted from the shipped geo and its draw-order key. */
    private static final List<String> DUNGEON_BEAST_UNDRAWN = List.of("ltoe1", "ltoe3", "rtoe1", "rtoe3");
    /** The Giant Robot's eleven shared leg and arm parts, each a pair of render-instance clones in the shipped geo (the Crab's form). */
    private static final List<String> GIANT_ROBOT_CLONED = List.of("thigh", "thigh2", "thigh3", "shin", "foot1", "foot2", "foot3", "arm1",
            "arm2", "arm3", "knuckles");

    @GameTest(template = "empty", batch = BATCH)
    public static void t2_012_first_tier1_slice_hook_registries_declare_no_layer_register_nothing_and_pose_through_their_hooks(GameTestHelper helper) {
        Flags flags = Flags.read();
        try {
            OreSpawnConfig.MODERN_ENABLED.set(true);
            OreSpawnConfig.MODERN_ARTIST_ANIMATIONS.set(true);
            OreSpawnConfig.MODERN_CLASSIC_ANIMATION_SPECIES.set(List.of());
            List<HookSpecies> all = hookSpeciesT1a();
            helper.assertTrue(all.size() == 17, "the seventeen hook registries landed by the first Tier-1 slice (the King and the Butterfly rig held)");
            for (HookSpecies species : all) {
                assertHookSpecies(helper, species);
            }
            List<String> beast = DrawOrder.read(JsonParser.parseString(resource(GEO + "dungeonbeast.geo.json")).getAsJsonObject());
            for (String toe : DUNGEON_BEAST_UNDRAWN) {
                helper.assertTrue(!beast.contains(toe), "the Dungeon Beast's shipped geo omits the undrawn toe " + toe + " (TEST-013)");
            }
            helper.assertTrue(beast.size() == 60, "the Dungeon Beast's draw-order key names its sixty drawn bones (found " + beast.size() + ")");
            List<String> robot = DrawOrder.read(JsonParser.parseString(resource(GEO + "giantrobot.geo.json")).getAsJsonObject());
            for (String part : GIANT_ROBOT_CLONED) {
                helper.assertTrue(robot.contains(part + "__i0") && robot.contains(part + "__i1") && !robot.contains(part),
                        "the Giant Robot's shipped geo carries the two render-instance clones of " + part + " and not the part itself");
            }
            helper.assertTrue(new LeonGeoReplacement().descriptor().modelResource().equals(new LeonopteryxGeoReplacement().descriptor().modelResource()),
                    "the Leonopteryx draws the Leon's geo (one rig for the two registries)");
            helper.assertTrue(new DragonGeoReplacement().descriptor().modelResource().equals(new BabyDragonGeoReplacement().descriptor().modelResource()),
                    "the Baby Dragon draws the Dragon's geo (one rig for the two registries)");
            helper.assertTrue(new GiantRobotGeoReplacement().descriptor().modelResource().equals(new JefferyGeoReplacement().descriptor().modelResource()),
                    "Jeffery draws the Giant Robot's geo (one rig for the two registries)");
            helper.assertTrue(!new KrakenGeoReplacement().descriptor().renderTransform().isIdentity()
                            && !new DungeonBeastGeoReplacement().descriptor().renderTransform().isIdentity(),
                    "the Kraken and the Dungeon Beast declare their constant render transform (TEST-013; t2_009 pins its conjugation)");
        } finally {
            flags.restore();
        }
        helper.succeed();
    }

    /** The t2_005 pins on one hook species (shared by the third, fourth, fifth and sixth slices', the FK slice's and the first Tier-1 slice's rows). */
    private static void assertHookSpecies(GameTestHelper helper, HookSpecies species) {
        helper.assertTrue(species.replacement().keyframeLayers().isEmpty(),
                species.name() + " declares no keyframe layer: on the hook until an artist delivers idle and walk");
        BakedAnimations shipped = bakeClips(resource(CLIPS + species.file() + ".animation.json"));
        helper.assertTrue(shipped.animations().isEmpty(),
                species.name() + ": the shipped " + species.file() + ".animation.json carries no clip (the s4 hook rigs' empty file)");
        helper.assertTrue(species.replacement().registerKeyframeLayers(registrar(), shipped) == 0,
                species.name() + ": under the modern keys the self-gate registers nothing - no layer declared, no clip shipped");
        helper.assertTrue(new AnimatableManager<>(species.replacement()).getAnimationControllers().isEmpty(),
                species.name() + ": the production registerControllers registers nothing on this server");
        helper.assertTrue(species.replacement().descriptor().shadowRadius() == species.shadow(),
                species.name() + ": the descriptor's shadow radius is the classic renderer's " + species.shadow() + " (ENT-S-092)");
        helper.assertTrue(species.replacement().descriptor().cubeFaceOrderRequired() == species.faceOrder(),
                species.name() + (species.faceOrder() ? " requires the classic face order (a zero-thickness cube, TEST-007)" : " requires no face-order key"));
        // The shipped geo bakes through GeckoLib's own loader, sorted into the G2 draw order and (where required) the
        // classic face order by the production statics, and the classic hook poses it off its bind.
        String geoJson = resource(GEO + species.file() + ".geo.json");
        JsonObject geo = JsonParser.parseString(geoJson).getAsJsonObject();
        List<String> drawOrder = DrawOrder.read(geo);
        helper.assertTrue(!drawOrder.isEmpty(), species.name() + ": the shipped geo carries orespawn:bone_draw_order (the G2 contract)");
        Map<String, List<List<Direction>>> faceOrder = FaceOrder.read(geo);
        helper.assertTrue(faceOrder.isEmpty() != species.faceOrder(),
                species.name() + ": the shipped geo carries orespawn:cube_face_order exactly where the descriptor requires it");
        BakedGeoModel baked = bakeRig(geoJson);
        DrawOrder.apply(baked, drawOrder);
        if (!faceOrder.isEmpty()) {
            FaceOrder.apply(baked, faceOrder);
        }
        AnimationProcessor<?> processor = poseThroughHook(species.replacement(), baked,
                new PoseInputs(species.subject(), 7.0F, 3.0F, 1.0F, 0.0F, 0.0F));
        GeoBone bone = processor.getBone(species.movingBone());
        helper.assertTrue(bone != null, species.name() + ": the shipped geo carries the bone " + species.movingBone());
        GeoBone bind = bakeRig(geoJson).getBone(species.movingBone()).orElseThrow();
        helper.assertTrue(bone.getRotX() != bind.getRotX() || bone.getRotY() != bind.getRotY() || bone.getRotZ() != bind.getRotZ(),
                species.name() + ": the classic hook moved " + species.movingBone() + " off its bind at age 7 (the pose source is the hook)");
    }

    // ------------------------------------------------------------------ row 4: the render facts and the shared rigs

    @GameTest(template = "empty", batch = BATCH)
    public static void t2_004_shadow_radii_and_the_shared_rigs(GameTestHelper helper) {
        Map<String, Float> shadows = Map.ofEntries(
                Map.entry("tshirt", TshirtRenderer.SHADOW), Map.entry("mosquito", MosquitoRenderer.SHADOW),
                Map.entry("cliff_racer", CliffRacerRenderer.SHADOW), Map.entry("brutalfly", BrutalflyRenderer.SHADOW),
                Map.entry("dragonfly", DragonflyRenderer.SHADOW), Map.entry("cockateil", CockateilRenderer.SHADOW),
                Map.entry("ruby_bird", CockateilRenderer.SHADOW), Map.entry("firefly", FireflyRenderer.SHADOW),
                Map.entry("gold_fish", GoldFishRenderer.SHADOW),
                // orig RenderAnt.java:22 super(model, par2 * par3): the classic renderers pass 0.1f / 0.15f x their SCALE (no SHADOW constant)
                Map.entry("ant", 0.1F * AntRenderer.SCALE), Map.entry("rainbow_ant", 0.1F * RainbowAntRenderer.SCALE),
                Map.entry("red_ant", 0.15F * RedAntRenderer.SCALE), Map.entry("termite", 0.15F * TermiteRenderer.SCALE),
                Map.entry("unstable_ant", 0.1F * UnstableAntRenderer.SCALE));
        for (Species species : species()) {
            float expected = shadows.get(species.name());
            helper.assertTrue(species.replacement().descriptor().shadowRadius() == expected,
                    species.name() + ": the descriptor's shadow radius is the classic renderer's " + expected + " (ENT-S-092)");
        }
        CockateilGeoReplacement cockateil = new CockateilGeoReplacement();
        RubyBirdGeoReplacement rubyBird = new RubyBirdGeoReplacement();
        helper.assertTrue(cockateil.descriptor().modelResource().equals(rubyBird.descriptor().modelResource())
                        && cockateil.descriptor().animationResource().equals(rubyBird.descriptor().animationResource())
                        && cockateil.keyframeLayers() == rubyBird.keyframeLayers(),
                "one rig, two consumers: the Ruby Bird shares the Cockateil's geo, clip file and layer list under its own descriptor");
        helper.assertTrue(cockateil.descriptor().shadowRadius() == CockateilRenderer.SHADOW
                        && rubyBird.descriptor().shadowRadius() == CockateilRenderer.SHADOW,
                "both consumers carry the shared CockateilRenderer shadow (orig ClientProxyOreSpawn.java:430-431, identical registrations)");
        AntGeoReplacement ant = new AntGeoReplacement();
        for (OreSpawnGeoReplacement<?> consumer : List.of(new RainbowAntGeoReplacement(), new RedAntGeoReplacement(),
                new TermiteGeoReplacement(), new UnstableAntGeoReplacement())) {
            helper.assertTrue(ant.descriptor().modelResource().equals(consumer.descriptor().modelResource())
                            && ant.descriptor().animationResource().equals(consumer.descriptor().animationResource())
                            && ant.keyframeLayers() == consumer.keyframeLayers(),
                    "one rig, five consumers: " + consumer.getClass().getSimpleName()
                            + " shares the Ant's geo, clip file and layer list under its own descriptor (orig ClientProxyOreSpawn.java:412-415, 476)");
            helper.assertTrue(!ant.descriptor().textureResource().equals(consumer.descriptor().textureResource()),
                    consumer.getClass().getSimpleName() + " draws its own texture over the shared rig");
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------ helpers (the KeyframeLegTests idiom)

    private record Flags(boolean modernEnabled, boolean artistAnimations, List<? extends String> classicSpecies) {
        static Flags read() {
            return new Flags(OreSpawnConfig.MODERN_ENABLED.get(), OreSpawnConfig.MODERN_ARTIST_ANIMATIONS.get(),
                    List.copyOf(OreSpawnConfig.MODERN_CLASSIC_ANIMATION_SPECIES.get()));
        }

        void restore() {
            OreSpawnConfig.MODERN_CLASSIC_ANIMATION_SPECIES.set(this.classicSpecies);
            OreSpawnConfig.MODERN_ARTIST_ANIMATIONS.set(this.artistAnimations);
            OreSpawnConfig.MODERN_ENABLED.set(this.modernEnabled);
        }
    }

    private static AnimatableManager.ControllerRegistrar registrar() {
        return new AnimatableManager.ControllerRegistrar(new ArrayList<>());
    }

    private static String resource(String path) {
        try (InputStream stream = T2SeamTests.class.getResourceAsStream(path)) {
            if (stream == null) {
                throw new IllegalStateException("missing classpath resource " + path);
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (java.io.IOException failure) {
            throw new IllegalStateException(failure);
        }
    }

    /** FileLoader.loadAnimationsFile 51-69: the document's animations member through KeyFramesAdapter.GEO_GSON. */
    private static BakedAnimations bakeClips(String json) {
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        return KeyFramesAdapter.GEO_GSON.fromJson(GsonHelper.getAsJsonObject(root, "animations"), BakedAnimations.class);
    }

    /** The shipped geo through GeckoLib's own loader and factory (the KeyframeLegTests idiom). */
    private static BakedGeoModel bakeRig(String json) {
        Model model = KeyFramesAdapter.GEO_GSON.fromJson(json, Model.class);
        return BakedModelFactory.DEFAULT_FACTORY.constructGeoModel(GeometryTree.fromModel(model));
    }

    /** The production hook on explicit inputs over a fresh bake: the S4CandidateRuntime form, registry-free. */
    private static <E extends Entity> AnimationProcessor<?> poseThroughHook(OreSpawnGeoReplacement<E> replacement, BakedGeoModel baked,
                                                                           PoseInputs inputs) {
        OreSpawnGeoReplacementModel<E, OreSpawnGeoReplacement<E>> model = new OreSpawnGeoReplacementModel<>(replacement.descriptor());
        model.getAnimationProcessor().setActiveModel(baked);
        replacement.pose(model.getAnimationProcessor(), inputs);
        return model.getAnimationProcessor();
    }
}
