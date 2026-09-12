package danger.orespawn.gametest;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.client.BrutalflyGeoReplacement;
import danger.orespawn.entity.client.BrutalflyRenderer;
import danger.orespawn.entity.client.CliffRacerGeoReplacement;
import danger.orespawn.entity.client.CliffRacerRenderer;
import danger.orespawn.entity.client.CockateilGeoReplacement;
import danger.orespawn.entity.client.CockateilRenderer;
import danger.orespawn.entity.client.DragonflyGeoReplacement;
import danger.orespawn.entity.client.DragonflyRenderer;
import danger.orespawn.entity.client.MosquitoGeoReplacement;
import danger.orespawn.entity.client.MosquitoRenderer;
import danger.orespawn.entity.client.OreSpawnGeoReplacement;
import danger.orespawn.entity.client.RubyBirdGeoReplacement;
import danger.orespawn.entity.client.TshirtGeoReplacement;
import danger.orespawn.entity.client.TshirtRenderer;
import danger.orespawn.entity.client.animation.KeyframeLayer;
import danger.orespawn.entity.client.animation.PhaseLockedKeyframeController;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.GsonHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.loading.json.typeadapter.KeyFramesAdapter;
import software.bernie.geckolib.loading.object.BakedAnimations;

/**
 * The first Tier-2 slice (2026-09-13): what the dedicated game-test server can pin of the seven simple-cyclic
 * descriptors behind the dev switch - no GeckoLib ticking (a controller's {@code process} initialises
 * {@code MolangQueries}, which this server refuses: the kf17d finding; the harness's keyframe reference leg
 * carries the pose facts). Own batch {@code t2Seam} (TEST-003).
 * <ul>
 * <li>{@code t2_001}: every new descriptor constructs registry-free on this server (the lazy entity-type
 *     suppliers, OPT-029 R0) and declares its frequency groups exactly - count, clip names, omegas, the chain's
 *     wingspeed, bones, none gait-scaled; the Ruby Bird declares the Cockateil's layers (one rig, two consumers).</li>
 * <li>{@code t2_002}: each shipped {@code <name>.animation.json} bakes through GeckoLib's own loader and carries
 *     {@code idle} keying no bone plus exactly one looping clip per declared group, every group bone keyed.</li>
 * <li>{@code t2_003}: the gate as ruled (owner 2026-09-12, item 12; contract section 2.1): under the modern keys a
 *     ONE-group species (its clip the bare {@code walk}) registers its one phase-locked layer on the declared
 *     controller with the declared omega and wingspeed; a MULTI-group species without a gait group (no bare
 *     {@code walk}: {@code walk_<group>} only) registers NOTHING - the classic hook stays its path until the owner
 *     rules which group carries the bare name (the presented question's pin).</li>
 * <li>{@code t2_004}: the render facts the 4c precedent pinned in code - each descriptor's shadow radius is its
 *     classic renderer's constant (ENT-S-092), the Cockateil and Ruby Bird sharing the Cockateil renderer's, and
 *     the two consumers share one geo, clip file and layer list.</li>
 * </ul>
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class T2SeamTests {
    private static final String BATCH = "t2Seam";
    private static final String CLIPS = "/assets/orespawn/animations/entity/";

    /** One species' declared transcription: the descriptor, its shipped clip, and the groups the classic model has. */
    private record Species(String name, OreSpawnGeoReplacement<?> replacement, String clipFile, boolean bareWalk,
                           List<String> groups, List<Float> omegas, List<Float> wingspeeds, List<Set<String>> bones) {
    }

    private static List<Species> species() {
        List<Species> out = new ArrayList<>();
        out.add(new Species("tshirt", new TshirtGeoReplacement(), "tshirt", true, List.of("turn"), List.of(0.05F),
                List.of(0.22F), List.of(Set.of("Shape1", "Shape2"))));
        out.add(new Species("mosquito", new MosquitoGeoReplacement(), "mosquito", true, List.of("wings"), List.of(3.0F),
                List.of(1.0F), List.of(Set.of("rightwing1", "rightwing2", "leftwing1", "leftwing2"))));
        out.add(new Species("cliff_racer", new CliffRacerGeoReplacement(), "cliffracer", true, List.of("wings"), List.of(1.3F),
                List.of(1.0F), List.of(Set.of("lwing", "rwing"))));
        out.add(new Species("brutalfly", new BrutalflyGeoReplacement(), "brutalfly", true, List.of("wings"), List.of(1.3F),
                List.of(0.2F), List.of(Set.of("rightwing", "rightwing2", "rightwing3", "rightwing4", "rightwing5", "rightwing6",
                        "leftwing", "leftwing2", "leftwing3", "leftwing4", "leftwing5", "leftwing6"))));
        out.add(new Species("dragonfly", new DragonflyGeoReplacement(), "dragonfly", false, List.of("wings", "jaws"),
                List.of(1.3F, 0.3F), List.of(2.0F, 2.0F),
                List.of(Set.of("lfwing", "rfwing", "lrwing", "rrwing"), Set.of("ljaw", "rjaw"))));
        List<String> birdGroups = List.of("wings", "tail", "feather1", "feather2", "feather3");
        List<Float> birdOmegas = List.of(1.5F, 0.3F, 1.1F, 1.2F, 1.3F);
        List<Float> birdWingspeeds = List.of(1.0F, 1.0F, 1.0F, 1.0F, 1.0F);
        List<Set<String>> birdBones = List.of(Set.of("lwing1", "lwing2", "rwing1", "rwing2"),
                Set.of("tailfeather1", "tailfeather2", "tailfeather3"), Set.of("feather1"), Set.of("feather2"), Set.of("feather3"));
        out.add(new Species("cockateil", new CockateilGeoReplacement(), "cockateil", false, birdGroups, birdOmegas, birdWingspeeds, birdBones));
        out.add(new Species("ruby_bird", new RubyBirdGeoReplacement(), "cockateil", false, birdGroups, birdOmegas, birdWingspeeds, birdBones));
        return out;
    }

    // ------------------------------------------------------------------ row 1: the descriptors and their groups

    @GameTest(template = "empty", batch = BATCH)
    public static void t2_001_every_descriptor_constructs_registry_free_and_declares_its_groups(GameTestHelper helper) {
        List<Species> all = species();
        helper.assertTrue(all.size() == 7, "the seven Tier-2 descriptors of the first slice (six rigs; the Cockateil rig has two consumers)");
        for (Species species : all) {
            List<KeyframeLayer> layers = species.replacement().keyframeLayers();
            helper.assertTrue(layers.size() == species.groups().size(),
                    species.name() + " declares " + species.groups().size() + " frequency groups (found " + layers.size() + ")");
            Set<String> claimed = new HashSet<>();
            for (int index = 0; index < layers.size(); index++) {
                KeyframeLayer layer = layers.get(index);
                String group = species.groups().get(index);
                String expectedClip = species.bareWalk() ? KeyframeLayer.WALK : KeyframeLayer.walkClip(group);
                helper.assertTrue(layer.group().equals(group), species.name() + " layer " + index + " is group " + group);
                helper.assertTrue(layer.clip().equals(expectedClip), species.name() + "/" + group + " clip is " + expectedClip
                        + " (" + (species.bareWalk() ? "a one-group species: the bare name" : "a multi-group species: walk_<group>") + ")");
                helper.assertTrue(layer.angularFrequencyRadiansPerTick() == species.omegas().get(index),
                        species.name() + "/" + group + " omega " + species.omegas().get(index));
                helper.assertTrue(layer.wingspeed() == species.wingspeeds().get(index),
                        species.name() + "/" + group + " wingspeed " + species.wingspeeds().get(index));
                helper.assertTrue(layer.bones().equals(species.bones().get(index)),
                        species.name() + "/" + group + " bones " + species.bones().get(index));
                helper.assertTrue(!layer.gaitScaled(), species.name() + "/" + group + " is unscaled (no gait group in the class)");
                for (String bone : layer.bones()) {
                    helper.assertTrue(claimed.add(bone), species.name() + ": bone " + bone + " in one group only (Amendment 1 point 5)");
                }
            }
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

    // ------------------------------------------------------------------ row 3: the gate as ruled

    @GameTest(template = "empty", batch = BATCH)
    public static void t2_003_gate_registers_the_bare_walk_species_only(GameTestHelper helper) {
        Flags flags = Flags.read();
        try {
            OreSpawnConfig.MODERN_ENABLED.set(true);
            OreSpawnConfig.MODERN_ARTIST_ANIMATIONS.set(true);
            OreSpawnConfig.MODERN_CLASSIC_ANIMATION_SPECIES.set(List.of());
            for (Species species : species()) {
                BakedAnimations shipped = bakeClips(resource(CLIPS + species.clipFile() + ".animation.json"));
                AnimatableManager.ControllerRegistrar registrar = registrar();
                int registered = species.replacement().registerKeyframeLayers(registrar, shipped);
                if (species.bareWalk()) {
                    helper.assertTrue(registered == 1 && registrar.controllers().size() == 1,
                            species.name() + ": a one-group species ships idle + the bare walk, so the gate registers its one layer");
                    KeyframeLayer layer = species.replacement().keyframeLayers().get(0);
                    AnimationController<? extends GeoAnimatable> controller = registrar.controllers().get(0);
                    helper.assertTrue(controller instanceof PhaseLockedKeyframeController<?> locked
                                    && locked.getName().equals("keyframe:walk")
                                    && locked.angularFrequencyRadiansPerSourceTick() == layer.angularFrequencyRadiansPerTick()
                                    && locked.wingspeed() == layer.wingspeed()
                                    && !locked.additive() && locked.amplitudeScaledRotationBones().isEmpty(),
                            species.name() + ": the phase-locked keyframe:walk with omega " + layer.angularFrequencyRadiansPerTick()
                                    + " x wingspeed " + layer.wingspeed() + ", unscaled");
                } else {
                    helper.assertTrue(registered == 0 && registrar.controllers().isEmpty(),
                            species.name() + ": a multi-group species without a bare walk registers nothing (the gate as ruled: "
                                    + "idle AND walk; walk_<group> is not walk) - the classic hook stays its path");
                }
                // The config gate still stands in front of a one-group species.
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

    // ------------------------------------------------------------------ row 4: the render facts and the shared rig

    @GameTest(template = "empty", batch = BATCH)
    public static void t2_004_shadow_radii_and_the_shared_rig(GameTestHelper helper) {
        Map<String, Float> shadows = Map.of(
                "tshirt", TshirtRenderer.SHADOW, "mosquito", MosquitoRenderer.SHADOW, "cliff_racer", CliffRacerRenderer.SHADOW,
                "brutalfly", BrutalflyRenderer.SHADOW, "dragonfly", DragonflyRenderer.SHADOW,
                "cockateil", CockateilRenderer.SHADOW, "ruby_bird", CockateilRenderer.SHADOW);
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
}
