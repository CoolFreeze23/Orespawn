package danger.orespawn.gametest;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Beaver;
import danger.orespawn.entity.client.BeaverGeoReplacement;
import danger.orespawn.entity.client.BrutalflyGeoReplacement;
import danger.orespawn.entity.client.CliffRacerGeoReplacement;
import danger.orespawn.entity.client.CockateilGeoReplacement;
import danger.orespawn.entity.client.DragonflyGeoReplacement;
import danger.orespawn.entity.client.FireflyGeoReplacement;
import danger.orespawn.entity.client.GeoReplacementDescriptor;
import danger.orespawn.entity.client.GoldFishGeoReplacement;
import danger.orespawn.entity.client.MosquitoGeoReplacement;
import danger.orespawn.entity.client.MotionInputs;
import danger.orespawn.entity.client.OreSpawnGeoReplacement;
import danger.orespawn.entity.client.RubyBirdGeoReplacement;
import danger.orespawn.entity.client.TshirtGeoReplacement;
import danger.orespawn.entity.client.animation.AttackingFlag;
import danger.orespawn.entity.client.animation.ContractLayers;
import danger.orespawn.entity.client.animation.KeyframeLayer;
import danger.orespawn.entity.client.animation.LocomotionKind;
import danger.orespawn.entity.client.animation.LocomotionWeights;
import danger.orespawn.entity.client.animation.PhaseLockedKeyframeController;
import danger.orespawn.entity.client.animation.TriggerMask;
import danger.orespawn.entity.client.animation.TriggeredClipController;
import danger.orespawn.entity.client.animation.WeightRamp;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.loading.json.raw.Model;
import software.bernie.geckolib.loading.json.typeadapter.KeyFramesAdapter;
import software.bernie.geckolib.loading.object.BakedAnimations;
import software.bernie.geckolib.loading.object.BakedModelFactory;
import software.bernie.geckolib.loading.object.GeometryTree;

/**
 * THE WEIGHTS SLICE (and as decided): what the dedicated game-test server can pin of the weighted contract -
 * the arithmetic, the ramps, the inputs, the registration and the mask. GeckoLib's controller processing is
 * client-only ({@code MolangQueries}; the kf17c / kf17d gates), so the pose-level rows - the exact blend, the ramp
 * on the bones, the trigger replacing the gait and blending out to the bone reset - are the harness's ({@code
 * g1tool WeightsBlendProbe}, run headlessly by the build). The fixture: the Beaver's three declared
 * groups over {@code contract_fixture.animation.json} (a test resource: an {@code idle} keying the four feet
 * and the head, constant {@code walk} / {@code walk_teeth} / {@code walk_tail} / {@code idle_tail} / {@code fly} /
 * {@code swim} and the one-shots {@code hurt}, {@code attack}, {@code death}) on a Beaver-typed FLYER replacement,
 * so {@code flying} can be read from a real entity.
 * <ul>
 * <li>{@code cw_001}: the weight products - at {@code limbSwingAmount} 0 the idle weight is 1, at 1 the walk
 *     weight, at 0.5 the exact half blend; {@code fly > swim > walk / idle} through the products, which sum to one;
 *     the aggro split of the idle weight; out-of-range inputs refused; on the fixture, every clip's weight is the
 *     sum of the shares that fall to it (the fly share falling to {@code walk_tail}, the teeth's idle share to
 *     nothing).</li>
 * <li>{@code cw_002}: the five-tick ramps as a function of the age since the flip - a repeated frame recomputes
 *     the same value, a skipped frame is caught up, a flip mid-ramp turns from the value reached, the first
 *     advance takes the boolean's value outright; the ramps live in the manager's extra data under
 *     {@link ContractLayers#RAMPS}; {@link ContractLayers#of} finds the contract from the manager's controllers.</li>
 * <li>{@code cw_003}: the transcription rule and the registration - the SHIPPED Beaver file is a transcription and
 *     registers exactly the three always-on layers it did before this slice (the gait layer amplitude-scaled and
 *     additive, the others unscaled: {@code kf_001} / {@code kf_002} / {@code t2_003} unchanged); the fixture is an
 *     artist file and registers the trigger controller FIRST (the keys {@code hurt}, {@code attack}, {@code death})
 *     and then the seven weighted layers in group-then-state order, each over the bones its clip keys; the config
 *     gate still stands; a partial artist file registers its layers alone; the nine transcription species' SPEC
 *     locomotion kinds.</li>
 * <li>{@code cw_004} (two ticks): the {@link MotionInputs} read from a real frozen Beaver - the age from
 *     {@code tickCount + partialTick}, {@code limbSwingAmount} from the state, {@code flying} = the SPEC's flyer AND
 *     {@code !onGround()} (false for the walker, true for the flyer once the mob is off the ground), {@code hurtTime}
 *     from the entity, and {@code inWater} true two ticks after water is set at its feet; the client-observed
 *     {@code hurtTime} edge fires {@code hurt} on the manager's trigger controller.</li>
 * <li>{@code cw_005}: the trigger mask - a held bone's factor 0, a released bone's factor the bone-reset fraction
 *     of the age since the release with the reset's value as its base ({@code (1 - p) x} the last pose), gone at
 *     {@code p = 1}, held again if the clip writes it mid-release; {@code getBoneResetTime()} is the 3-tick
 *     {@link TriggerMask#BONE_RESET_TICKS} on every replacement.</li>
 * </ul>
 * Synchronous rows but row 4 (two ticks; its Beaver discarded in its finally); row 3 flips the config flags and
 * restores them in a finally, so the class sits in its own batch {@code contractWeights} (TEST-003).
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class ContractWeightsTests {
    private static final String BATCH = "contractWeights";
    private static final String SHIPPED_CLIP = "/assets/orespawn/animations/entity/beaver.animation.json";
    private static final String FIXTURE_CLIP = "/orespawn_gametest/keyframes/contract_fixture.animation.json";
    private static final BlockPos BEAVER_POS = new BlockPos(2, 1, 2); // inside the template: 24 blocks out sat in a chunk that never ticked, so the water was never seen
    private static final Set<String> GAIT_BONES = Set.of("rff", "lrf", "lff", "rrf");
    /** A two-bone rig for the mask row. */
    private static final String RIG = """
            {"format_version": "1.12.0", "minecraft:geometry": [{"description": {
            "identifier": "geometry.orespawn.gametest.contract_weights", "texture_width": 16, "texture_height": 16},
            "bones": [
            {"name": "a", "pivot": [0, 0, 0], "cubes": [{"origin": [-1, 0, -1], "size": [2, 2, 2], "uv": [0, 0]}]},
            {"name": "b", "pivot": [0, 2, 0], "cubes": [{"origin": [-1, 2, -1], "size": [2, 2, 2], "uv": [0, 4]}]}
            ]}]}
            """;

    /** The Beaver's three groups on a FLYER replacement (the SPEC's word), Beaver-typed so a real Beaver is its entity. */
    private static final class FixtureFlyer extends OreSpawnGeoReplacement<Beaver> {
        private static final List<KeyframeLayer> LAYERS = List.of(
                new KeyframeLayer("gait", KeyframeLayer.WALK, 3.7F, GAIT_BONES, true),
                new KeyframeLayer("teeth", KeyframeLayer.walkClip("teeth"), 2.7F, Set.of("teeth"), false),
                new KeyframeLayer("tail", KeyframeLayer.walkClip("tail"), 0.5F, Set.of("tail"), false));
        private static final GeoReplacementDescriptor<Beaver> DESCRIPTOR = new GeoReplacementDescriptor<>(
                () -> ModEntities.BEAVER.get(), Beaver.class,
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/beaver.geo.json"),
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "gametest/contract_fixture.animation.json"),
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/beaver.png"), 0.5F) {
        };

        FixtureFlyer() {
            super(DESCRIPTOR);
        }

        @Override
        public List<KeyframeLayer> keyframeLayers() {
            return LAYERS;
        }

        @Override
        public LocomotionKind locomotion() {
            return LocomotionKind.FLYER;
        }
    }

    // ------------------------------------------------------------------ row 1: the weight products

    @GameTest(template = "empty", batch = BATCH)
    public static void cw_001_weight_products_at_amplitude_0_half_1_and_the_priority_fly_swim_walk_idle(GameTestHelper helper) {
        LocomotionWeights rest = LocomotionWeights.REST;
        helper.assertTrue(rest.idle() == 1.0F && rest.walk() == 0.0F && rest.swim() == 0.0F && rest.fly() == 0.0F
                && rest.aggroIdle() == 0.0F && rest.calmIdle() == 1.0F, "at rest: w_idle 1, the calm share all of it");
        LocomotionWeights half = new LocomotionWeights(0.5F, 0.0F, 0.0F, 0.0F);
        helper.assertTrue(half.idle() == 0.5F && half.walk() == 0.5F && half.fly() == 0.0F && half.swim() == 0.0F,
                "limbSwingAmount 0.5 on the ground: the exact half blend of idle and walk (w_move continuous, no threshold)");
        LocomotionWeights stride = new LocomotionWeights(1.0F, 0.0F, 0.0F, 0.0F);
        helper.assertTrue(stride.walk() == 1.0F && stride.idle() == 0.0F, "limbSwingAmount 1: the walk weight is 1");
        LocomotionWeights swimming = new LocomotionWeights(0.5F, 1.0F, 0.0F, 0.0F);
        helper.assertTrue(swimming.swim() == 1.0F && swimming.walk() == 0.0F && swimming.idle() == 0.0F && swimming.fly() == 0.0F,
                "in water: swim 1 over walk / idle whatever the stride");
        LocomotionWeights flyingWet = new LocomotionWeights(0.5F, 1.0F, 1.0F, 0.0F);
        helper.assertTrue(flyingWet.fly() == 1.0F && flyingWet.swim() == 0.0F && flyingWet.walk() == 0.0F && flyingWet.idle() == 0.0F,
                "fly over swim: the fly weight wins (the products' priority fly > swim > walk / idle)");
        LocomotionWeights mixed = new LocomotionWeights(0.5F, 0.5F, 0.5F, 0.0F);
        helper.assertTrue(close(mixed.fly(), 0.5D) && close(mixed.swim(), 0.25D) && close(mixed.walk(), 0.125D) && close(mixed.idle(), 0.125D),
                "mid-ramps: fly 0.5, swim 0.25, walk 0.125, idle 0.125");
        for (LocomotionWeights weights : List.of(rest, half, stride, swimming, flyingWet, mixed,
                new LocomotionWeights(0.3F, 0.7F, 0.2F, 0.4F), new LocomotionWeights(0.9F, 0.1F, 0.8F, 1.0F))) {
            double sum = (double) weights.fly() + weights.swim() + weights.walk() + weights.idle();
            helper.assertTrue(Math.abs(sum - 1.0D) <= 1.0e-6D, "the four locomotion weights sum to one: " + weights + " -> " + sum);
            helper.assertTrue(Math.abs((double) weights.aggroIdle() + weights.calmIdle() - weights.idle()) <= 1.0e-6D,
                    "the aggro and calm shares split the idle weight: " + weights);
        }
        LocomotionWeights aggro = new LocomotionWeights(0.5F, 0.0F, 0.0F, 0.5F);
        helper.assertTrue(aggro.aggroIdle() == 0.25F && aggro.calmIdle() == 0.25F, "w_aggro 0.5 at half stride: aggro_idle 0.25, calm_idle 0.25");
        for (float bad : new float[] {-0.01F, 1.01F, Float.NaN}) {
            boolean refused = false;
            try {
                new LocomotionWeights(bad, 0.0F, 0.0F, 0.0F);
            } catch (IllegalArgumentException expected) {
                refused = true;
            }
            helper.assertTrue(refused, "a weight outside [0, 1] is refused: " + bad);
        }

        // On the fixture: every clip's weight is the sum of the shares that fall to it (contract section 2.4).
        FixtureFlyer flyer = new FixtureFlyer();
        BakedAnimations fixture = bakeClips(resource(FIXTURE_CLIP));
        ContractLayers contract = flyer.contractLayers(fixture);
        List<String> clips = new ArrayList<>();
        for (ContractLayers.Layer layer : contract.layers()) {
            clips.add(layer.clip());
        }
        helper.assertTrue(clips.equals(List.of("idle", "walk", "swim", "fly", "walk_teeth", "idle_tail", "walk_tail")),
                "the fixture's seven layers in group-then-state order: " + clips);
        contract.beginFrame(null, MotionInputs.of(100.0F, 0.0F, false, false));
        helper.assertTrue(contract.weight("idle") == 1.0F && contract.weight("walk") == 0.0F && contract.weight("idle_tail") == 1.0F
                        && contract.weight("walk_tail") == 0.0F && contract.weight("walk_teeth") == 0.0F,
                "at rest: idle and idle_tail at 1; walk_teeth at 0 (no idle_teeth delivered: the teeth hold bind)");
        contract.beginFrame(null, MotionInputs.of(100.0F, 0.5F, false, false));
        helper.assertTrue(contract.weight("idle") == 0.5F && contract.weight("walk") == 0.5F && contract.weight("walk_teeth") == 0.5F
                        && contract.weight("idle_tail") == 0.5F && contract.weight("walk_tail") == 0.5F && contract.weight("fly") == 0.0F,
                "at half stride: the half blend on every group");
        contract.beginFrame(null, MotionInputs.of(100.0F, 0.5F, false, true)); helper.assertTrue(contract.weights().fly() == 0.0F, "the flip at 100: the fly ramp starts at 0 (section 3.1's five-tick smoothing; cw_002 pins the ramp itself)");
        contract.beginFrame(null, MotionInputs.of(105.0F, 0.5F, false, true)); helper.assertTrue(contract.weights().fly() == 1.0F, "five ticks after the flip: the fly weight 1 (only a manager BUILT in flight takes it outright - cw_002)");
        helper.assertTrue(contract.weight("fly") == 1.0F && contract.weight("idle") == 0.0F && contract.weight("walk") == 0.0F
                        && contract.weight("walk_tail") == 1.0F && contract.weight("idle_tail") == 0.0F && contract.weight("walk_teeth") == 1.0F,
                "in flight: fly at 1 on the gait group; no fly_tail / fly_teeth, so the fly share falls to walk_tail and walk_teeth (section 2.4)");
        helper.assertTrue(contract.contributors(3).equals(List.of("fly")) && contract.contributors(6).equals(List.of("fly", "swim", "walk"))
                        && contract.contributors(5).equals(List.of("aggro_idle", "calm_idle")) && contract.contributors(4).equals(List.of("fly", "swim", "walk")),
                "the shares per layer: fly -> fly; walk_tail <- fly, swim, walk; idle_tail <- the aggro and calm shares; walk_teeth <- fly, swim, walk");
        helper.succeed();
    }

    // ------------------------------------------------------------------ row 2: the ramps on the clip clock

    @GameTest(template = "empty", batch = BATCH)
    public static void cw_002_fly_and_swim_weights_ramp_over_five_ticks_from_the_flip_on_the_clip_clock(GameTestHelper helper) {
        WeightRamp ramp = new WeightRamp();
        helper.assertTrue(ramp.advance(false, 100.0F) == 0.0F, "the first advance takes the boolean's value: false -> 0");
        helper.assertTrue(ramp.advance(true, 100.0F) == 0.0F && ramp.flipAge() == 100.0F, "the flip at 100: still 0 at +0");
        helper.assertTrue(close(ramp.advance(true, 101.0F), 0.2D), "0.2 at +1");
        helper.assertTrue(close(ramp.advance(true, 102.5F), 0.5D), "0.5 at +2.5");
        helper.assertTrue(close(ramp.advance(true, 102.5F), 0.5D), "a repeated frame recomputes the same weight (ENT-S-147: a function of the age since the flip)");
        helper.assertTrue(ramp.advance(true, 105.0F) == 1.0F, "1 at +5");
        helper.assertTrue(ramp.advance(true, 107.0F) == 1.0F, "1 after");
        helper.assertTrue(ramp.advance(false, 107.0F) == 1.0F && ramp.flipAge() == 107.0F, "the flip back at 107: 1 at +0");
        helper.assertTrue(close(ramp.advance(false, 109.5F), 0.5D), "0.5 at +2.5 down");
        helper.assertTrue(close(ramp.advance(true, 109.5F), 0.5D), "a flip mid-ramp turns from the value reached (0.5)");
        helper.assertTrue(close(ramp.advance(true, 111.0F), 0.8D), "0.8 at +1.5 up from 0.5");
        helper.assertTrue(ramp.advance(true, 112.0F) == 1.0F, "1 at +2.5 up from 0.5");
        WeightRamp skipped = new WeightRamp();
        skipped.advance(false, 0.0F);
        helper.assertTrue(skipped.advance(true, 10.0F) == 0.0F && close(skipped.advance(true, 14.0F), 0.8D),
                "a skipped frame is caught up: 0.8 at +4 whatever frames were rendered between");
        helper.assertTrue(new WeightRamp().advance(true, 50.0F) == 1.0F, "a manager built in flight starts on fly (the first advance takes the value outright)");
        helper.assertTrue(WeightRamp.RAMP_TICKS == 5.0F, "contract section 3.1: five ticks per direction");

        // Through the contract on a real manager: the ramps in the manager's extra data, the contract found from the controllers.
        FixtureFlyer flyer = new FixtureFlyer();
        BakedAnimations fixture = bakeClips(resource(FIXTURE_CLIP));
        ContractLayers contract = flyer.contractLayers(fixture);
        AnimatableManager.ControllerRegistrar registrar = registrar();
        contract.register(registrar, flyer, state -> (float) state.getAnimationTick());
        AnimatableManager<FixtureFlyer> manager = flyer.getAnimatableInstanceCache().getManagerForId(11L);
        helper.assertTrue(manager.getAnimationControllers().isEmpty() && ContractLayers.of(manager) == null,
                "a fresh manager holds no controller on this server and no contract");
        for (AnimationController<? extends GeoAnimatable> controller : registrar.controllers()) {
            manager.addController(controller);
        }
        helper.assertTrue(ContractLayers.of(manager) == contract, "the contract is found from the manager's controllers");
        helper.assertTrue(manager.getData(ContractLayers.RAMPS) == null, "no ramps before the first frame");
        contract.beginFrame(manager, MotionInputs.of(100.0F, 0.5F, false, false));
        helper.assertTrue(manager.getData(ContractLayers.RAMPS) == contract.ramps(), "the ramps live in the manager's extra data");
        helper.assertTrue(contract.weights().fly() == 0.0F, "on the ground");
        float[] ages = {101.0F, 102.0F, 103.5F, 106.0F, 107.0F};
        double[] expected = {0.0D, 0.2D, 0.5D, 1.0D, 1.0D};
        for (int index = 0; index < ages.length; index++) {
            contract.beginFrame(manager, MotionInputs.of(ages[index], 0.5F, false, true));
            helper.assertTrue(close(contract.weights().fly(), expected[index]),
                    String.format(Locale.ROOT, "the fly weight %.1f at +%.1f from the flip (found %.4f)", expected[index], ages[index] - 101.0F, contract.weights().fly()));
            helper.assertTrue(close(contract.weight("fly"), expected[index]) && close(contract.weight("walk"), 0.5D * (1.0D - expected[index])),
                    "the fly layer's weight and the walk layer's (1 - w_fly) x 0.5");
        }
        contract.beginFrame(manager, MotionInputs.of(107.0F, 0.5F, true, true));
        helper.assertTrue(contract.weights().swim() == 0.0F && contract.ramps().swim.flipAge() == 107.0F, "water at 107: the swim ramp flips, fly still wins");
        contract.beginFrame(manager, MotionInputs.of(112.0F, 0.5F, true, true));
        helper.assertTrue(contract.ramps().swim.value() == 1.0F && contract.weights().swim() == 0.0F && contract.weights().fly() == 1.0F,
                "the swim ramp at 1 but the swim weight 0 under fly (the products' priority)");
        contract.beginFrame(manager, MotionInputs.of(112.0F, 0.5F, true, false));
        contract.beginFrame(manager, MotionInputs.of(117.0F, 0.5F, true, false));
        helper.assertTrue(contract.weights().fly() == 0.0F && contract.weights().swim() == 1.0F && contract.weight("swim") == 1.0F,
                "landed in water: swim at 1 after the fly ramp ran down");
        helper.succeed();
    }

    // ------------------------------------------------------------------ row 3: the transcription rule and the registration

    @GameTest(template = "empty", batch = BATCH)
    public static void cw_003_transcription_file_registers_the_always_on_layers_and_an_artist_file_the_weighted_contract(GameTestHelper helper) {
        BeaverGeoReplacement beaver = new BeaverGeoReplacement();
        BakedAnimations shipped = bakeClips(resource(SHIPPED_CLIP));
        BakedAnimations fixture = bakeClips(resource(FIXTURE_CLIP));
        helper.assertTrue(ContractLayers.isTranscription(shipped, beaver.keyframeLayers()),
                "the SHIPPED beaver.animation.json is the transcription: idle keys no bone, walk / walk_teeth / walk_tail");
        helper.assertTrue(!ContractLayers.isTranscription(fixture, beaver.keyframeLayers()), "the fixture is an artist file");
        BakedAnimations idleAndWalk = new BakedAnimations(Map.of(KeyframeLayer.IDLE, shipped.getAnimation(KeyframeLayer.IDLE),
                KeyframeLayer.WALK, shipped.getAnimation(KeyframeLayer.WALK)));
        helper.assertTrue(ContractLayers.isTranscription(idleAndWalk, beaver.keyframeLayers()), "a subset of the transcription is a transcription");
        BakedAnimations transcriptionPlusHurt = new BakedAnimations(Map.of(KeyframeLayer.IDLE, shipped.getAnimation(KeyframeLayer.IDLE),
                KeyframeLayer.WALK, shipped.getAnimation(KeyframeLayer.WALK), "hurt", fixture.getAnimation("hurt")));
        helper.assertTrue(!ContractLayers.isTranscription(transcriptionPlusHurt, beaver.keyframeLayers()),
                "a triggered clip beside the transcription makes an artist file");
        BakedAnimations realIdle = new BakedAnimations(Map.of(KeyframeLayer.IDLE, fixture.getAnimation(KeyframeLayer.IDLE),
                KeyframeLayer.WALK, shipped.getAnimation(KeyframeLayer.WALK)));
        helper.assertTrue(!ContractLayers.isTranscription(realIdle, beaver.keyframeLayers()), "an idle that keys a bone makes an artist file");

        Flags flags = Flags.read();
        try {
            OreSpawnConfig.MODERN_ENABLED.set(true);
            OreSpawnConfig.MODERN_ARTIST_ANIMATIONS.set(true);
            OreSpawnConfig.MODERN_CLASSIC_ANIMATION_SPECIES.set(List.of());
            // The transcription: exactly the three always-on layers, as before this slice (kf_001 / kf_002 / t2_003).
            AnimatableManager.ControllerRegistrar transcription = registrar();
            helper.assertTrue(beaver.registerKeyframeLayers(transcription, shipped) == 3 && transcription.controllers().size() == 3,
                    "the shipped transcription registers its three layers");
            String[] names = {"keyframe:walk", "keyframe:walk_teeth", "keyframe:walk_tail"};
            for (int index = 0; index < 3; index++) {
                PhaseLockedKeyframeController<?> layer = (PhaseLockedKeyframeController<?>) transcription.controllers().get(index);
                helper.assertTrue(layer.getName().equals(names[index]) && !layer.weighted() && layer.contract() == null
                                && layer.additive() == (index == 0) && layer.amplitudeScaledRotationBones().equals(index == 0 ? GAIT_BONES : Set.of()),
                        names[index] + ": the always-on transcription form (the gait layer amplitude-scaled and additive, the others unscaled), never weighted");
            }
            // The artist file: the trigger controller first, then the seven weighted layers.
            AnimatableManager.ControllerRegistrar artist = registrar();
            int registered = beaver.registerKeyframeLayers(artist, fixture);
            helper.assertTrue(registered == 8 && artist.controllers().size() == 8, "the fixture registers the trigger controller and seven weighted layers (found " + registered + ")");
            AnimationController<? extends GeoAnimatable> first = artist.controllers().get(0);
            helper.assertTrue(first instanceof TriggeredClipController<?> triggers && triggers.getName().equals(TriggeredClipController.NAME)
                            && triggers.keys().equals(Set.of("hurt", "attack", "death")) && new ArrayList<>(triggers.keys()).equals(List.of("attack", "death", "hurt")),
                    "the trigger controller is registered first with the file's one-shots as its keys, in name order (the baked map keeps no file order)");
            String[] weightedNames = {"keyframe:idle", "keyframe:walk", "keyframe:swim", "keyframe:fly", "keyframe:walk_teeth", "keyframe:idle_tail", "keyframe:walk_tail"};
            float[] omegas = {3.7F, 3.7F, 3.7F, 3.7F, 2.7F, 0.5F, 0.5F};
            List<Set<String>> keyed = List.of(Set.of("rff", "lrf", "lff", "rrf", "head"), GAIT_BONES, Set.of("rff"), Set.of("rff", "lff"),
                    Set.of("teeth"), Set.of("tail"), Set.of("tail"));
            ContractLayers contract = null;
            for (int index = 0; index < 7; index++) {
                AnimationController<? extends GeoAnimatable> controller = artist.controllers().get(index + 1);
                helper.assertTrue(controller instanceof PhaseLockedKeyframeController<?> layer && layer.getName().equals(weightedNames[index])
                                && layer.weighted() && layer.additive() && layer.contract() != null
                                && layer.angularFrequencyRadiansPerSourceTick() == omegas[index] && layer.wingspeed() == 1.0F
                                && layer.amplitudeScaledRotationBones().equals(keyed.get(index)),
                        weightedNames[index] + ": a weighted phase-locked layer at its group's tempo over the bones its clip keys");
                PhaseLockedKeyframeController<?> layer = (PhaseLockedKeyframeController<?>) controller;
                helper.assertTrue(contract == null || layer.contract() == contract, "every weighted layer shares the one per-manager contract");
                contract = layer.contract();
            }
            helper.assertTrue(contract != null && ((TriggeredClipController<?>) first).layers() == contract, "the trigger controller shares it too");
            helper.assertTrue(contract.triggers().keySet().equals(Set.of("hurt", "attack", "death"))
                            && contract.triggers().get("death").loopType() == Animation.LoopType.HOLD_ON_LAST_FRAME,
                    "the death clip holds its last frame; hurt and attack play once");
            helper.assertTrue(contract.locomotion() == LocomotionKind.WALKER && contract.attackingFlag() == AttackingFlag.NONE,
                    "the Beaver's contract: a walker without an attacking flag");
            // The config gate stands in front of the weighted contract as it does the transcription.
            OreSpawnConfig.MODERN_ARTIST_ANIMATIONS.set(false);
            helper.assertTrue(beaver.registerKeyframeLayers(registrar(), fixture) == 0, "artistAnimations off: nothing registers for an artist file either");
            OreSpawnConfig.MODERN_ARTIST_ANIMATIONS.set(true);
            OreSpawnConfig.MODERN_CLASSIC_ANIMATION_SPECIES.set(List.of("orespawn:beaver"));
            helper.assertTrue(beaver.registerKeyframeLayers(registrar(), fixture) == 0, "the exclusion list: the classic source for an artist file");
            OreSpawnConfig.MODERN_CLASSIC_ANIMATION_SPECIES.set(List.of());
            // A partial artist delivery: idle (keying bones) and walk alone register two weighted layers and no trigger controller.
            AnimatableManager.ControllerRegistrar partial = registrar();
            helper.assertTrue(beaver.registerKeyframeLayers(partial, realIdle) == 2 && partial.controllers().size() == 2
                            && partial.controllers().get(0).getName().equals("keyframe:idle") && partial.controllers().get(1).getName().equals("keyframe:walk"),
                    "a real idle with the transcription's walk: the two weighted layers of the gait group, the other groups holding bind");
            helper.assertTrue(beaver.registerKeyframeLayers(registrar(), idleAndWalk) == 1, "the transcription's idle + walk: the gait layer alone, as before");
            helper.assertTrue(beaver.registerKeyframeLayers(registrar(), transcriptionPlusHurt) == 2,
                    "the transcription's idle + walk + hurt: an artist file - the trigger controller and the weighted walk (the bone-less idle registers no layer)");
            helper.assertTrue(beaver.registerKeyframeLayers(registrar(), new BakedAnimations(Map.of("hurt", fixture.getAnimation("hurt"), KeyframeLayer.WALK, shipped.getAnimation(KeyframeLayer.WALK)))) == 0,
                    "no idle: the gate stays shut before any of this");
        } finally {
            flags.restore();
        }
        // The SPEC's locomotion word on the fifteen transcription species (tools/artist_specs/<registry>.json).
        helper.assertTrue(beaver.locomotion() == LocomotionKind.WALKER && new BrutalflyGeoReplacement().locomotion() == LocomotionKind.FLYER
                        && new CliffRacerGeoReplacement().locomotion() == LocomotionKind.FLYER && new CockateilGeoReplacement().locomotion() == LocomotionKind.FLYER
                        && new RubyBirdGeoReplacement().locomotion() == LocomotionKind.FLYER && new DragonflyGeoReplacement().locomotion() == LocomotionKind.FLYER
                        && new FireflyGeoReplacement().locomotion() == LocomotionKind.FLYER && new MosquitoGeoReplacement().locomotion() == LocomotionKind.FLYER
                        && new GoldFishGeoReplacement().locomotion() == LocomotionKind.SWIMMER && new TshirtGeoReplacement().locomotion() == LocomotionKind.STATIONARY,
                "the seeds' locomotion kinds: the seven flyers, the Gold Fish a swimmer, the T-shirt stationary, the Beaver (and the Ant rig) walkers");
        helper.assertTrue(beaver.attackingFlag() == AttackingFlag.NONE && beaver.getBoneResetTime() == TriggerMask.BONE_RESET_TICKS,
                "no attacking flag wired; the bone reset time is the contract's 3 ticks");
        helper.succeed();
    }

    // ------------------------------------------------------------------ row 4: the motion inputs from a real entity

    @GameTest(template = "empty", batch = BATCH, timeoutTicks = 40)
    public static void cw_004_motion_inputs_are_read_from_the_entity_flying_swimming_and_the_hurt_edge(GameTestHelper helper) {
        Beaver beaver = spawnFrozen(helper, ModEntities.BEAVER.get(), BEAVER_POS);
        BeaverGeoReplacement walker = new BeaverGeoReplacement();
        FixtureFlyer flyer = new FixtureFlyer();
        try {
            beaver.tickCount = 137;
            AnimationState<BeaverGeoReplacement> walkerState = new AnimationState<>(walker, 0.0F, 0.37F, 0.25F, true);
            walkerState.setData(DataTickets.ENTITY, beaver);
            MotionInputs onGround = walker.motionInputs(walkerState);
            helper.assertTrue(onGround.subject() == beaver && onGround.ageInTicks() == 137.25F && onGround.limbSwingAmount() == 0.37F,
                    "the subject, the age (tickCount + partialTick) and the limb-swing amount from the state");
            helper.assertTrue(!onGround.flying() && !onGround.inWater() && onGround.hurtTime() == 0 && onGround.deathTime() == 0 && onGround.attacking() == 0,
                    "on the ground, dry, unhurt, alive, no attacking flag: every boolean false");
            AnimationState<FixtureFlyer> flyerState = new AnimationState<>(flyer, 0.0F, 0.0F, 0.5F, false);
            flyerState.setData(DataTickets.ENTITY, beaver);
            helper.assertTrue(!flyer.motionInputs(flyerState).flying(), "a flyer on the ground is not flying");
            beaver.setOnGround(false);
            helper.assertTrue(flyer.motionInputs(flyerState).flying(), "the SPEC's flyer AND !onGround(): flying");
            helper.assertTrue(!walker.motionInputs(walkerState).flying(), "a walker off the ground is still not flying (the SPEC's word gates it)");
            beaver.setOnGround(true);
            beaver.hurtTime = 10;
            helper.assertTrue(walker.motionInputs(walkerState).hurtTime() == 10, "hurtTime from the entity");

            // The client-observed edge fires hurt on the manager's trigger controller.
            BakedAnimations fixture = bakeClips(resource(FIXTURE_CLIP));
            ContractLayers contract = flyer.contractLayers(fixture);
            AnimatableManager.ControllerRegistrar registrar = registrar();
            contract.register(registrar, flyer, state -> (float) state.getAnimationTick());
            AnimatableManager<FixtureFlyer> manager = flyer.getAnimatableInstanceCache().getManagerForId(13L);
            for (AnimationController<? extends GeoAnimatable> controller : registrar.controllers()) {
                manager.addController(controller);
            }
            TriggeredClipController<?> triggers = (TriggeredClipController<?>) manager.getAnimationControllers().get(TriggeredClipController.NAME);
            beaver.hurtTime = 0;
            flyer.beginContractFrame(manager, flyerState);
            helper.assertTrue(triggers.getTriggeredAnimation() == null && contract.ramps().lastHurtTime() == 0, "unhurt: no trigger");
            beaver.hurtTime = 10;
            flyer.beginContractFrame(manager, flyerState);
            helper.assertTrue(triggers.getTriggeredAnimation() != null && contract.ramps().lastHurtTime() == 10,
                    "the hurtTime rising edge (0 -> 10) fires hurt on the trigger controller");
            beaver.hurtTime = 9;
            triggers.stop();
            flyer.beginContractFrame(manager, flyerState);
            helper.assertTrue(contract.ramps().lastHurtTime() == 9, "the countdown is not an edge");
            helper.assertTrue(contract.lastInputs().subject() == beaver && contract.lastInputs().hurtTime() == 9, "the frame's inputs are the entity's");

            // Water at the mob's feet: two ticks later the entity reports itself in water.
            helper.setBlock(BEAVER_POS, Blocks.WATER);
            helper.runAfterDelay(2L, () -> {
                try {
                    helper.assertTrue(beaver.isInWater(), "the Beaver stands in water");
                    MotionInputs wet = walker.motionInputs(walkerState);
                    helper.assertTrue(wet.inWater(), "inWater = Entity.isInWater() (contract section 3)");
                    contract.beginFrame(manager, flyer.motionInputs(flyerState));
                    helper.assertTrue(contract.ramps().swim.target() && contract.ramps().swim.flipAge() == (float) beaver.tickCount + 0.5F,
                            "the swim ramp flips on the frame the water is seen, at the frame's age");
                } finally {
                    if (!beaver.isRemoved()) {
                        beaver.discard();
                    }
                }
                helper.succeed();
            });
        } catch (RuntimeException failure) {
            if (!beaver.isRemoved()) {
                beaver.discard();
            }
            throw failure;
        }
    }

    // ------------------------------------------------------------------ row 5: the trigger mask

    @GameTest(template = "empty", batch = BATCH)
    public static void cw_005_trigger_mask_replaces_the_gait_on_the_trigger_bones_and_blends_out_over_the_bone_reset(GameTestHelper helper) {
        BakedGeoModel baked = bakeRig(RIG);
        Map<String, GeoBone> bones = bones(baked); bones.values().forEach(GeoBone::saveInitialSnapshot); Map<String, software.bernie.geckolib.animation.state.BoneSnapshot> snapshots = new java.util.HashMap<>(); // the processor's registerGeoBone saves the initial snapshot in production
        TriggerMask mask = new TriggerMask();
        helper.assertTrue(mask.factor("a") == 1.0F && !mask.held("a") && !mask.releasing("a"), "an untouched bone: factor 1");
        mask.beginFrame(100.0D);
        mask.update(Set.of("a"), snapshots, bones);
        helper.assertTrue(mask.held("a") && mask.factor("a") == 0.0F && !mask.held("b") && mask.factor("b") == 1.0F,
                "the clip's bone is held (the locomotion layers leave it alone); the other bone keeps its full weight");
        software.bernie.geckolib.animation.state.BoneSnapshot snapshotA = software.bernie.geckolib.animation.state.BoneSnapshot.copy(bones.get("a").getInitialSnapshot()); snapshotA.updateRotation(0.3F, 0.0F, 0.0F);
        snapshotA.updateOffset(0.0F, 2.0F, 0.0F); snapshots.put("a", snapshotA); // the MANAGER'S snapshot, not the shared bake's bone (the weights slice)
        mask.beginFrame(101.0D);
        mask.update(Set.of(), snapshots, bones);
        helper.assertTrue(!mask.held("a") && mask.releasing("a") && mask.factor("a") == 0.0F, "released at 101: factor 0 at +0");
        float[] delta = mask.releaseDelta("a");
        helper.assertTrue(delta[0] == 0.3F && delta[4] == 2.0F && delta[1] == 0.0F && delta[6] == 0.0F,
                "the base at +0 is the clip's last pose as deltas from bind (rotation x 0.3, position y 2)");
        mask.beginFrame(102.5D);
        helper.assertTrue(mask.factor("a") == 0.5F && mask.releaseDelta("a")[0] == 0.15F && mask.releaseDelta("a")[4] == 1.0F,
                "at +1.5 of the 3-tick reset: factor 0.5, base half the last pose - GeckoLib's own lerp(p, last, bind)");
        mask.beginFrame(102.5D);
        helper.assertTrue(mask.factor("a") == 0.5F, "a repeated frame recomputes the same fraction");
        mask.beginFrame(104.0D);
        helper.assertTrue(mask.factor("a") == 1.0F && mask.releaseDelta("a")[0] == 0.0F, "at +3: factor 1, base 0");
        mask.update(Set.of(), snapshots, bones);
        helper.assertTrue(!mask.releasing("a") && mask.releasingBones().isEmpty(), "the release ends at p = 1");
        helper.assertTrue(mask.releaseDelta("b")[0] == 0.0F && mask.releaseDelta("a")[0] == 0.0F, "no base for a bone that is not releasing");
        // Held again mid-release: a second trigger on the same bone.
        mask.beginFrame(110.0D);
        mask.update(Set.of("a", "b"), snapshots, bones);
        mask.beginFrame(111.0D);
        mask.update(Set.of("b"), snapshots, bones);
        helper.assertTrue(mask.releasing("a") && mask.held("b") && mask.heldBones().equals(Set.of("b")) && mask.releasingBones().equals(Set.of("a")),
                "a released while b stays held");
        mask.beginFrame(112.0D);
        mask.update(Set.of("a", "b"), snapshots, bones);
        helper.assertTrue(mask.held("a") && !mask.releasing("a") && mask.factor("a") == 0.0F, "written again mid-release: held again, no base");
        helper.assertTrue(TriggerMask.BONE_RESET_TICKS == 3.0D && TriggerMask.CHANNELS == 9, "the contract's 3-tick bone reset over nine channels");
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

    private static boolean close(double a, double b) {
        return Math.abs(a - b) <= 1.0e-6D;
    }

    private static AnimatableManager.ControllerRegistrar registrar() {
        return new AnimatableManager.ControllerRegistrar(new ArrayList<>());
    }

    private static String resource(String path) {
        try (InputStream stream = ContractWeightsTests.class.getResourceAsStream(path)) {
            if (stream == null) {
                throw new IllegalStateException("missing classpath resource " + path);
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (java.io.IOException failure) {
            throw new IllegalStateException(failure);
        }
    }

    private static BakedAnimations bakeClips(String json) {
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        return KeyFramesAdapter.GEO_GSON.fromJson(GsonHelper.getAsJsonObject(root, "animations"), BakedAnimations.class);
    }

    private static BakedGeoModel bakeRig(String json) {
        Model model = KeyFramesAdapter.GEO_GSON.fromJson(json, Model.class);
        return BakedModelFactory.DEFAULT_FACTORY.constructGeoModel(GeometryTree.fromModel(model));
    }

    private static Map<String, GeoBone> bones(BakedGeoModel model) {
        Map<String, GeoBone> bones = new TreeMap<>();
        for (GeoBone bone : model.topLevelBones()) {
            collect(bone, bones);
        }
        return bones;
    }

    private static void collect(GeoBone bone, Map<String, GeoBone> bones) {
        bones.put(bone.getName(), bone);
        bone.getChildBones().forEach(child -> collect(child, bones));
    }

    private static <M extends Mob> M spawnFrozen(GameTestHelper helper, EntityType<M> type, BlockPos pos) {
        M mob = helper.spawn(type, pos);
        mob.setNoAi(true);
        mob.setPersistenceRequired();
        mob.setNoGravity(true);
        mob.setOnGround(true);
        return mob;
    }
}
