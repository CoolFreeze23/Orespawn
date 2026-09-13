package danger.orespawn.gametest;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.client.AntGeoReplacement;
import danger.orespawn.entity.client.AntRenderer;
import danger.orespawn.entity.client.BeeGeoReplacement;
import danger.orespawn.entity.client.BeeRenderer;
import danger.orespawn.entity.client.BrutalflyGeoReplacement;
import danger.orespawn.entity.client.BrutalflyRenderer;
import danger.orespawn.entity.client.CannonFodderGeoReplacement;
import danger.orespawn.entity.client.CaterKillerGeoReplacement;
import danger.orespawn.entity.client.CaterKillerRenderer;
import danger.orespawn.entity.client.CliffRacerGeoReplacement;
import danger.orespawn.entity.client.CliffRacerRenderer;
import danger.orespawn.entity.client.CloudSharkGeoReplacement;
import danger.orespawn.entity.client.CockateilGeoReplacement;
import danger.orespawn.entity.client.CockateilRenderer;
import danger.orespawn.entity.client.CricketGeoReplacement;
import danger.orespawn.entity.client.CricketRenderer;
import danger.orespawn.entity.client.DragonflyGeoReplacement;
import danger.orespawn.entity.client.DragonflyRenderer;
import danger.orespawn.entity.client.DrawOrder;
import danger.orespawn.entity.client.FaceOrder;
import danger.orespawn.entity.client.FairyGeoReplacement;
import danger.orespawn.entity.client.FairyRenderer;
import danger.orespawn.entity.client.FireflyGeoReplacement;
import danger.orespawn.entity.client.FireflyRenderer;
import danger.orespawn.entity.client.GammaMetroidGeoReplacement;
import danger.orespawn.entity.client.GammaMetroidRenderer;
import danger.orespawn.entity.client.GoldFishGeoReplacement;
import danger.orespawn.entity.client.GoldFishRenderer;
import danger.orespawn.entity.client.HerculesBeetleGeoReplacement;
import danger.orespawn.entity.client.HerculesBeetleRenderer;
import danger.orespawn.entity.client.IrukandjiGeoReplacement;
import danger.orespawn.entity.client.IrukandjiRenderer;
import danger.orespawn.entity.client.MosquitoGeoReplacement;
import danger.orespawn.entity.client.MosquitoRenderer;
import danger.orespawn.entity.client.OreSpawnGeoReplacement;
import danger.orespawn.entity.client.OreSpawnGeoReplacementModel;
import danger.orespawn.entity.client.PoseInputs;
import danger.orespawn.entity.client.RainbowAntGeoReplacement;
import danger.orespawn.entity.client.RainbowAntRenderer;
import danger.orespawn.entity.client.RedAntGeoReplacement;
import danger.orespawn.entity.client.RedAntRenderer;
import danger.orespawn.entity.client.RubberDuckyGeoReplacement;
import danger.orespawn.entity.client.RubberDuckyRenderer;
import danger.orespawn.entity.client.RubyBirdGeoReplacement;
import danger.orespawn.entity.client.SkateGeoReplacement;
import danger.orespawn.entity.client.SkateRenderer;
import danger.orespawn.entity.client.TermiteGeoReplacement;
import danger.orespawn.entity.client.TermiteRenderer;
import danger.orespawn.entity.client.TerribleTerrorGeoReplacement;
import danger.orespawn.entity.client.TerribleTerrorRenderer;
import danger.orespawn.entity.client.TshirtGeoReplacement;
import danger.orespawn.entity.client.TshirtRenderer;
import danger.orespawn.entity.client.UnstableAntGeoReplacement;
import danger.orespawn.entity.client.UnstableAntRenderer;
import danger.orespawn.entity.client.WormLargeGeoReplacement;
import danger.orespawn.entity.client.WormLargeRenderer;
import danger.orespawn.entity.client.WormMediumGeoReplacement;
import danger.orespawn.entity.client.WormMediumRenderer;
import danger.orespawn.entity.client.WormSmallGeoReplacement;
import danger.orespawn.entity.client.WormSmallRenderer;
import danger.orespawn.entity.client.animation.KeyframeLayer;
import danger.orespawn.entity.client.animation.PhaseLockedKeyframeController;
import danger.orespawn.entity.pose.BeePose;
import danger.orespawn.entity.pose.CaterKillerPose;
import danger.orespawn.entity.pose.HerculesBeetlePose;
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
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
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
 *     OPT-029 R0) and declares its frequency groups exactly - count, clip names under the naming rule (the gait
 *     group's clip, else the SPEC's primary group's, is the bare {@code walk}; every other group's is
 *     {@code walk_<group>}: owner 2026-09-13, addendum item 26 (2), contract section 2.1 amended), omegas, the
 *     chain's wingspeed, bones, gait scaling on the Ant's legs and nowhere else; a shared rig's consumers declare the
 *     owner's layers (the Ruby Bird the Cockateil's, the four other ants the Ant's).</li>
 * <li>{@code t2_002}: each shipped {@code <name>.animation.json} bakes through GeckoLib's own loader and carries
 *     {@code idle} keying no bone plus exactly one looping clip per declared group, every group bone keyed.</li>
 * <li>{@code t2_003} (re-pinned by the second slice under the naming rule): under the modern keys EVERY landed species
 *     registers its declared layers under the default keys - one phase-locked controller per layer named
 *     {@code keyframe:<clip>} with the declared omega and wingspeed, exactly one of them {@code keyframe:walk} (the
 *     primary group's), the gait group's amplitude-scaled and additive over its bones (the Ant family), every other
 *     unscaled - so the Dragonfly, Cockateil and Ruby Bird gates the first slice pinned CLOSED are now OPEN; the
 *     config gate still closes them all.</li>
 * <li>{@code t2_005} (the third Tier-2 slice, 2026-09-13; owner's third set item 8 under Amendment 2): the fifteen HOOK
 *     species - each descriptor constructs registry-free and declares NO keyframe layer, its shipped animation file bakes
 *     to no clip (the s4 hook rigs' empty file), the self-gate registers nothing under the modern keys, its shadow is the
 *     classic renderer's (ENT-S-092), the classic face order is required exactly where the shipped geo carries it
 *     (TEST-007: the Cloud Shark, Bee, Fairy and Terrible Terror), and the classic hook poses a fresh bake of the shipped
 *     geo through {@code OreSpawnGeoReplacement.pose} on explicit {@code PoseInputs} - the pose source is the hook.</li>
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
                    helper.assertTrue(claimed.add(bone), species.name() + ": bone " + bone + " in one group only (Amendment 1 point 5)");
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
                                + layers.size() + "; found " + registered + ") - the naming rule opened it (owner 2026-09-13, item 26 (2))");
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
            helper.assertTrue(all.size() == 15, "the fifteen hook descriptors of the third Tier-2 slice (owner 2026-09-13, third set item 8)");
            for (HookSpecies species : all) {
                helper.assertTrue(species.replacement().keyframeLayers().isEmpty(),
                        species.name() + " declares no keyframe layer: on the hook until an artist delivers idle and walk (Amendment 2)");
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
        } finally {
            flags.restore();
        }
        helper.succeed();
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
