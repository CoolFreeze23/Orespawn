package danger.orespawn.g1;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import danger.orespawn.entity.client.MotionInputs;
import danger.orespawn.entity.client.animation.AttackingFlag;
import danger.orespawn.entity.client.animation.ContractLayers;
import danger.orespawn.entity.client.animation.KeyframeLayer;
import danger.orespawn.entity.client.animation.LocomotionKind;
import danger.orespawn.entity.client.animation.PhaseLockedKeyframeController;
import danger.orespawn.entity.client.animation.SplineRepair;
import danger.orespawn.entity.client.animation.TriggerMask;
import danger.orespawn.entity.client.animation.TriggeredClipController;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.instance.InstancedAnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.loading.json.raw.Model;
import software.bernie.geckolib.loading.json.typeadapter.KeyFramesAdapter;
import software.bernie.geckolib.loading.object.BakedAnimations;
import software.bernie.geckolib.loading.object.BakedModelFactory;
import software.bernie.geckolib.loading.object.GeometryTree;
import software.bernie.geckolib.model.GeoModel;

/**
 * THE BLEND'S PROOF where the harness has the seam (the weights slice): the production weighted contract ({@link
 * ContractLayers} over the production {@link PhaseLockedKeyframeController#weighted} layers and
 * the {@link TriggeredClipController}) driven through GeckoLib 4.8.4's own {@code
 * AnimationProcessor.tickAnimation} on a fixture rig in this headless JVM - the dedicated game-test server
 * cannot run a GeckoLib controller ({@code MolangQueries}, the kf17c / kf17d gates), so the pose-level rows live
 * here and {@code ContractWeightsTests} pins the arithmetic, the inputs and the registration on the
 * server.
 *
 * <p>The fixture: two frequency groups in the Beaver's form - a gait group {@code a b} at 3.7 rad/tick
 * (speed-scaled) and a tail group {@code c} at 0.5 - plus two free bones, {@code head} (keyed by {@code idle}) and
 * {@code d} (keyed by no locomotion clip); the clips {@code idle} (a 10 deg, head 5 deg, constants), {@code walk}
 * (a the 40 deg cosine, b -20 deg), {@code idle_tail} (c 5 deg), {@code walk_tail} (c the 9 deg cosine), {@code fly}
 * (a 30, b 30), {@code swim} (a -30), and the one-shot {@code hurt} (a 50 deg, head 20 deg, d 15 deg, 0.25 s).
 * Constants make a blend's expected value exact; the cosines are
 * measured from the TRANSCRIPTION path ({@link KeyframeLayer#controller}, the always-on form) on the same clips at
 * full amplitude, so the weighted rows compare the two production paths. The rows (every rotation in GeckoLib's
 * internal radians; the loader negates authored X keys):</p>
 * <ol>
 *   <li>at {@code limbSwingAmount} 0 the pose is the idle clips' (exact);</li>
 *   <li>at 1 it is the walk clips' - the transcription path's value at full amplitude (exact);</li>
 *   <li>at 0.5 it is the exact weighted blend {@code 0.5 idle + 0.5 walk} of every bone (1e-5);</li>
 *   <li>the fly weight ramps over 5 ticks from the flip (0, 0.2, 0.5, 1 at +0, +1, +2.5, +5) and the pose is
 *       {@code (1 - w) x ground + w x fly} (1e-5); a flip back mid-ramp turns from the value reached;</li>
 *   <li>swim in water, fly over swim: {@code swim} at 1 on the gait group, its share on the tail group falling to
 *       {@code walk_tail} (no {@code swim_tail}); with fly on top the fly weight wins;</li>
 *   <li>the trigger: {@code hurt} fired by the client-observed {@code hurtTime} edge replaces the gait on its three
 *       bones only - GeckoLib's 3-tick transition from the live pose to the clip's first key, the clip's value
 *       while it plays - while {@code b} and {@code c} keep the locomotion pose to the last bit against a twin
 *       that never saw the edge; after the clip, {@code a} and {@code head} (locomotion-keyed) cross back to the
 *       live locomotion pose over the 3-tick bone reset ({@code 0.5 last + 0.5 live} at +1.5, the live pose at +3,
 *       1e-5) and {@code d}, keyed by no locomotion layer, takes GeckoLib's own reset to bind with the same
 *       fraction; from +3.5 on, {@code a} and {@code head} equal the twin to the bit.</li>
 * </ol>
 * Usage: {@code WeightsBlendProbe} (no arguments); prints one line per check and exits 1 on any failure.
 */
public final class WeightsBlendProbe {
    private static final float GAIT_W = 3.7F;
    private static final float TAIL_W = 0.5F;
    private static final double TOLERANCE = 1.0e-5D;
    private static final List<KeyframeLayer> GROUPS = List.of(
            new KeyframeLayer("gait", KeyframeLayer.WALK, GAIT_W, Set.of("a", "b"), true),
            new KeyframeLayer("tail", KeyframeLayer.walkClip("tail"), TAIL_W, Set.of("c"), false));
    private static final List<String> BONES = List.of("a", "b", "c", "head", "d");
    private static final String RIG = """
            {"format_version": "1.12.0", "minecraft:geometry": [{"description": {
            "identifier": "geometry.orespawn.g1.weights_probe", "texture_width": 16, "texture_height": 16},
            "bones": [
            {"name": "a", "pivot": [0, 0, 0], "cubes": [{"origin": [-1, 0, -1], "size": [2, 2, 2], "uv": [0, 0]}]},
            {"name": "b", "pivot": [0, 2, 0], "cubes": [{"origin": [-1, 2, -1], "size": [2, 2, 2], "uv": [0, 4]}]},
            {"name": "c", "pivot": [0, 4, 0], "cubes": [{"origin": [-1, 4, -1], "size": [2, 2, 2], "uv": [0, 8]}]},
            {"name": "head", "pivot": [0, 6, 0], "cubes": [{"origin": [-1, 6, -1], "size": [2, 2, 2], "uv": [0, 12]}]},
            {"name": "d", "pivot": [0, 8, 0], "cubes": [{"origin": [-1, 8, -1], "size": [2, 2, 2], "uv": [4, 0]}]}
            ]}]}
            """;

    private static int failures;

    private WeightsBlendProbe() {
    }

    public static void main(String[] args) {
        Map<String, Animation> clips = new LinkedHashMap<>();
        clips.put("idle", constant("idle", Map.of("a", 10.0D, "head", 5.0D), 1.0D, true));
        clips.put("walk", cosineWith("walk", "a", 40.0D, Map.of("b", -20.0D), 1.0D));
        clips.put("idle_tail", constant("idle_tail", Map.of("c", 5.0D), 1.0D, true));
        clips.put("walk_tail", cosineWith("walk_tail", "c", 9.0D, Map.of(), 1.0D));
        clips.put("fly", constant("fly", Map.of("a", 30.0D, "b", 30.0D), 1.0D, true));
        clips.put("swim", constant("swim", Map.of("a", -30.0D), 1.0D, true));
        clips.put("hurt", constant("hurt", Map.of("a", 50.0D, "head", 20.0D, "d", 15.0D), 0.25D, false));
        BakedAnimations artist = SplineRepair.repair(new BakedAnimations(clips));
        // The transcription reference: the same walk-family clips, idle keying no bone, on the always-on path.
        Map<String, Animation> transcriptionClips = new LinkedHashMap<>();
        transcriptionClips.put("idle", constant("idle", Map.of(), 1.0D, true));
        transcriptionClips.put("walk", clips.get("walk"));
        transcriptionClips.put("walk_tail", clips.get("walk_tail"));
        BakedAnimations transcription = SplineRepair.repair(new BakedAnimations(transcriptionClips));
        check("the transcription file is recognised (idle keys no bone, walk-family clips only)",
                ContractLayers.isTranscription(transcription, GROUPS) && !ContractLayers.isTranscription(artist, GROUPS));

        Weighted weighted = new Weighted(artist);
        Weighted twin = new Weighted(artist);
        Transcription reference = new Transcription(transcription);
        ContractLayers contract = weighted.contract;
        List<String> layerNames = new ArrayList<>();
        for (ContractLayers.Layer layer : contract.layers()) {
            layerNames.add(layer.clip());
        }
        System.out.println("layers: " + layerNames + "; triggers: " + contract.triggers().keySet()
                + "; controllers: " + weighted.manager.getAnimationControllers().keySet());
        check("the trigger controller is registered first and the six locomotion layers after it",
                weighted.manager.getAnimationControllers().keySet().iterator().next().equals(TriggeredClipController.NAME)
                        && contract.layers().size() == 6);

        double idleA = internal(10.0D);
        double idleHead = internal(5.0D);
        double idleC = internal(5.0D);
        double walkB = internal(-20.0D);
        double flyA = internal(30.0D);
        double flyB = internal(30.0D);
        double swimA = internal(-30.0D);
        double hurtA = internal(50.0D);
        double hurtHead = internal(20.0D);
        double hurtD = internal(15.0D);

        // Row 1: at rest the pose is the idle clips'.
        for (double age = 100.0D; age <= 103.0D; age += 0.5D) {
            Map<String, Float> pose = weighted.frame(age, 0.0F, false, false, 0);
            expect("row 1 rest a = idle (age " + age + ")", pose.get("a"), idleA, 0.0D);
            expect("row 1 rest b = bind", pose.get("b"), 0.0D, 0.0D);
            expect("row 1 rest c = idle_tail", pose.get("c"), idleC, 0.0D);
            expect("row 1 rest head = idle's head", pose.get("head"), idleHead, 0.0D);
        }
        // Row 2: at full stride the pose is the walk clips' - the transcription path's value.
        for (double age = 103.5D; age <= 106.5D; age += 0.5D) {
            Map<String, Float> pose = weighted.frame(age, 1.0F, false, false, 0);
            Map<String, Float> full = reference.frame(age, 1.0F);
            expect("row 2 walk a = transcription walk (age " + age + ")", pose.get("a"), full.get("a"), 0.0D);
            expect("row 2 walk b = walk's b", pose.get("b"), walkB, 0.0D);
            expect("row 2 walk c = transcription walk_tail", pose.get("c"), full.get("c"), 0.0D);
            expect("row 2 walk head = bind (walk keys no head)", pose.get("head"), 0.0D, 0.0D);
        }
        // Row 3: at half stride the exact weighted blend.
        for (double age = 107.0D; age <= 110.0D; age += 0.5D) {
            Map<String, Float> pose = weighted.frame(age, 0.5F, false, false, 0);
            Map<String, Float> full = reference.frame(age, 1.0F);
            expect("row 3 blend a = 0.5 idle + 0.5 walk (age " + age + ")", pose.get("a"), 0.5D * idleA + 0.5D * full.get("a"), TOLERANCE);
            expect("row 3 blend b = 0.5 walk b", pose.get("b"), 0.5D * walkB, TOLERANCE);
            expect("row 3 blend c = 0.5 idle_tail + 0.5 walk_tail", pose.get("c"), 0.5D * idleC + 0.5D * full.get("c"), TOLERANCE);
            expect("row 3 blend head = 0.5 idle head", pose.get("head"), 0.5D * idleHead, TOLERANCE);
            expect("row 3 weights idle / walk", contract.weight("idle"), 0.5D, 0.0D);
            expect("row 3 weights walk", contract.weight("walk"), 0.5D, 0.0D);
        }
        // Row 4: the fly ramp from the flip at 111.
        double[] flyAges = {111.0D, 112.0D, 113.5D, 116.0D, 117.0D};
        double[] flyWeights = {0.0D, 0.2D, 0.5D, 1.0D, 1.0D};
        for (int index = 0; index < flyAges.length; index++) {
            double age = flyAges[index];
            Map<String, Float> pose = weighted.frame(age, 0.5F, false, true, 0);
            Map<String, Float> full = reference.frame(age, 1.0F);
            double w = flyWeights[index];
            expect("row 4 fly weight at +" + (age - 111.0D), contract.weights().fly(), w, 1.0e-6D);
            double groundA = 0.5D * idleA + 0.5D * full.get("a");
            expect("row 4 fly a = (1 - w) ground + w fly (age " + age + ")", pose.get("a"), (1.0D - w) * groundA + w * flyA, TOLERANCE);
            expect("row 4 fly b", pose.get("b"), (1.0D - w) * 0.5D * walkB + w * flyB, TOLERANCE);
            // no fly_tail: the fly share falls to walk_tail (section 2.4), so c = (1 - w)(0.5 idle_tail + 0.5 walk_tail) + w walk_tail
            expect("row 4 fly c (fly share falls to walk_tail)", pose.get("c"),
                    (1.0D - w) * (0.5D * idleC + 0.5D * full.get("c")) + w * full.get("c"), TOLERANCE);
        }
        Map<String, Float> repeated = weighted.frame(117.0D, 0.5F, false, true, 0);
        expect("row 4 a repeated frame recomputes the same weight (ENT-S-147)", contract.weights().fly(), 1.0D, 0.0D);
        expect("row 4 a repeated frame recomputes the same pose", repeated.get("a"), flyA, TOLERANCE);
        weighted.frame(118.0D, 0.5F, false, false, 0);
        expect("row 4 flip back: the ramp turns from 1 at +0", contract.weights().fly(), 1.0D, 0.0D);
        weighted.frame(120.5D, 0.5F, false, false, 0);
        expect("row 4 flip back: 0.5 at +2.5", contract.weights().fly(), 0.5D, 1.0e-6D);
        weighted.frame(123.0D, 0.5F, false, false, 0);
        expect("row 4 flip back: 0 at +5", contract.weights().fly(), 0.0D, 0.0D);
        // Row 5: swim, then fly over swim.
        for (double age = 124.0D; age <= 129.0D; age += 1.0D) {
            weighted.frame(age, 0.5F, true, false, 0);
        }
        Map<String, Float> swimming = weighted.frame(130.0D, 0.5F, true, false, 0);
        Map<String, Float> full130 = reference.frame(130.0D, 1.0F);
        expect("row 5 swim weight 1 after 5 ticks in water", contract.weights().swim(), 1.0D, 0.0D);
        expect("row 5 swim a = swim's a", swimming.get("a"), swimA, TOLERANCE);
        expect("row 5 swim b = bind (swim keys no b; walk at 0)", swimming.get("b"), 0.0D, TOLERANCE);
        expect("row 5 swim c = walk_tail (the swim share falls to walk_tail)", swimming.get("c"), full130.get("c"), TOLERANCE);
        for (double age = 131.0D; age <= 135.0D; age += 1.0D) {
            weighted.frame(age, 0.5F, true, true, 0);
        }
        Map<String, Float> flyingWet = weighted.frame(136.0D, 0.5F, true, true, 0);
        expect("row 5 fly over swim: fly weight 1", contract.weights().fly(), 1.0D, 0.0D);
        expect("row 5 fly over swim: swim weight 0", contract.weights().swim(), 0.0D, 0.0D);
        expect("row 5 fly over swim: a = fly's a", flyingWet.get("a"), flyA, TOLERANCE);
        expect("row 5 fly over swim: b = fly's b", flyingWet.get("b"), flyB, TOLERANCE);
        for (double age = 137.0D; age <= 142.0D; age += 1.0D) {
            weighted.frame(age, 0.5F, false, false, 0);
            twin.frame(age, 0.5F, false, false, 0);
        }
        // Row 6: the trigger. Both harnesses walk on the ground at half stride at half-tick frames; only one sees the
        // hurt edge at 150 (hurtTime 10, then vanilla's countdown). GeckoLib's relative clock starts the clip on the
        // first PROCESSED frame at or past the transition's end, so the frames step every half tick: the transition
        // 150 -> 153, the 5-tick clip 153 -> 158 (the first frame it writes nothing), the blend-out 158 -> 161.
        TriggeredClipController<?> triggers = (TriggeredClipController<?>) weighted.manager.getAnimationControllers().get(TriggeredClipController.NAME);
        Map<Double, Map<String, Float>> poses = new TreeMap<>();
        Map<Double, Map<String, Float>> twins = new TreeMap<>();
        Map<Double, Boolean> playing = new TreeMap<>();
        Map<Double, Set<String>> heldAt = new TreeMap<>();
        Map<Double, Set<String>> releasingAt = new TreeMap<>();
        for (double age = 142.5D; age <= 164.0D; age += 0.5D) {
            int hurtTime = age < 150.0D ? 0 : (int) Math.max(0.0D, 10.0D - Math.floor(age - 150.0D));
            poses.put(age, weighted.frame(age, 0.5F, false, false, hurtTime));
            twins.put(age, twin.frame(age, 0.5F, false, false, 0));
            playing.put(age, triggers.isPlayingTriggeredAnimation());
            heldAt.put(age, contract.mask().heldBones());
            releasingAt.put(age, contract.mask().releasingBones());
        }
        double snapshotA = poses.get(149.5D).get("a");
        double snapshotHead = poses.get(149.5D).get("head");
        check("row 6 the hurtTime edge fired the trigger at +0 and it plays", playing.get(150.0D));
        check("row 6 nothing played before the edge", !playing.get(149.5D) && heldAt.get(149.5D).isEmpty());
        check("row 6 the mask holds a, head and d while the clip plays", heldAt.get(150.0D).equals(Set.of("a", "head", "d"))
                && heldAt.get(155.0D).equals(Set.of("a", "head", "d")));
        expect("row 6 blend-in at +0: a = the live pose snapshot", poses.get(150.0D).get("a"), snapshotA, TOLERANCE);
        expect("row 6 blend-in at +0: head = its live pose snapshot (idle keys head)", poses.get(150.0D).get("head"), snapshotHead, TOLERANCE);
        expect("row 6 blend-in at +0: d = bind (no locomotion layer keys d)", poses.get(150.0D).get("d"), 0.0D, TOLERANCE);
        expect("row 6 blend-in at +1.5: a halfway from the snapshot to hurt's a", poses.get(151.5D).get("a"), 0.5D * snapshotA + 0.5D * hurtA, TOLERANCE);
        expect("row 6 blend-in at +1.5: head halfway from its snapshot to hurt's head", poses.get(151.5D).get("head"), 0.5D * snapshotHead + 0.5D * hurtHead, TOLERANCE);
        expect("row 6 blend-in at +1.5: d halfway from bind to hurt's d", poses.get(151.5D).get("d"), 0.5D * hurtD, TOLERANCE);
        expect("row 6 playing at +5: a = hurt's a", poses.get(155.0D).get("a"), hurtA, TOLERANCE);
        expect("row 6 playing at +5: head = hurt's head", poses.get(155.0D).get("head"), hurtHead, TOLERANCE);
        expect("row 6 playing at +5: d = hurt's d", poses.get(155.0D).get("d"), hurtD, TOLERANCE);
        check("row 6 at +8 the clip has finished (the first frame it writes nothing)", !playing.get(158.0D) && playing.get(157.5D));
        check("row 6 at +8 a, head and d are releasing and nothing is held", releasingAt.get(158.0D).equals(Set.of("a", "head", "d")) && heldAt.get(158.0D).isEmpty());
        expect("row 6 blend-out at +0: a = the clip's last pose", poses.get(158.0D).get("a"), hurtA, TOLERANCE);
        expect("row 6 blend-out at +0: head = the clip's last pose", poses.get(158.0D).get("head"), hurtHead, TOLERANCE);
        expect("row 6 blend-out at +0: d = the clip's last pose (GeckoLib's own reset at 0)", poses.get(158.0D).get("d"), hurtD, TOLERANCE);
        expect("row 6 blend-out at +1.5: a = 0.5 last + 0.5 live locomotion", poses.get(159.5D).get("a"), 0.5D * hurtA + 0.5D * twins.get(159.5D).get("a"), TOLERANCE);
        expect("row 6 blend-out at +1.5: head = 0.5 last + 0.5 live locomotion", poses.get(159.5D).get("head"), 0.5D * hurtHead + 0.5D * twins.get(159.5D).get("head"), TOLERANCE);
        expect("row 6 blend-out at +1.5: d = 0.5 last + 0.5 bind (GeckoLib's own reset, the same fraction)", poses.get(159.5D).get("d"), 0.5D * hurtD, TOLERANCE);
        expect("row 6 blend-out at +3: a = the live locomotion pose", poses.get(161.0D).get("a"), twins.get(161.0D).get("a"), TOLERANCE);
        expect("row 6 blend-out at +3: head = the live locomotion pose", poses.get(161.0D).get("head"), twins.get(161.0D).get("head"), TOLERANCE);
        expect("row 6 blend-out at +3: d = bind", poses.get(161.0D).get("d"), 0.0D, TOLERANCE);
        check("row 6 at +3 nothing is releasing", releasingAt.get(161.0D).isEmpty());
        for (double age = 142.5D; age <= 164.0D; age += 0.5D) {
            expect("row 6 b keeps the locomotion pose to the bit (twin) at " + age, poses.get(age).get("b"), twins.get(age).get("b"), 0.0D);
            expect("row 6 c keeps the locomotion pose to the bit (twin) at " + age, poses.get(age).get("c"), twins.get(age).get("c"), 0.0D);
        }
        for (double age = 161.5D; age <= 164.0D; age += 0.5D) {
            expect("row 6 after the release: a = twin exactly at " + age, poses.get(age).get("a"), twins.get(age).get("a"), 0.0D);
            expect("row 6 after the release: head = twin exactly at " + age, poses.get(age).get("head"), twins.get(age).get("head"), 0.0D);
        }
        // Row 7: a second hurt edge at 170 plays the clip again from the live pose (GeckoLib's STOPPED -> TRANSITIONING
        // path on a controller that already played once), and the release returns the bones to the twin.
        poses.clear();
        twins.clear();
        playing.clear();
        for (double age = 164.5D; age <= 184.0D; age += 0.5D) {
            int hurtTime = age < 170.0D ? 0 : (int) Math.max(0.0D, 10.0D - Math.floor(age - 170.0D));
            poses.put(age, weighted.frame(age, 0.5F, false, false, hurtTime));
            twins.put(age, twin.frame(age, 0.5F, false, false, 0));
            playing.put(age, triggers.isPlayingTriggeredAnimation());
        }
        check("row 7 a second edge at 170 plays the clip again", playing.get(170.0D) && !playing.get(169.5D) && playing.get(177.5D) && !playing.get(178.0D));
        expect("row 7 blend-in at +0: a = the live pose snapshot", poses.get(170.0D).get("a"), poses.get(169.5D).get("a"), TOLERANCE);
        expect("row 7 blend-in at +1.5: a halfway from the snapshot to hurt's a", poses.get(171.5D).get("a"), 0.5D * poses.get(169.5D).get("a") + 0.5D * hurtA, TOLERANCE);
        expect("row 7 playing at +5: a = hurt's a", poses.get(175.0D).get("a"), hurtA, TOLERANCE);
        expect("row 7 blend-out at +1.5: a = 0.5 last + 0.5 live locomotion", poses.get(179.5D).get("a"), 0.5D * hurtA + 0.5D * twins.get(179.5D).get("a"), TOLERANCE);
        expect("row 7 blend-out at +1.5: d = 0.5 last + 0.5 bind", poses.get(179.5D).get("d"), 0.5D * hurtD, TOLERANCE);
        expect("row 7 blend-out at +3: a = the live locomotion pose", poses.get(181.0D).get("a"), twins.get(181.0D).get("a"), TOLERANCE);
        for (double age = 164.5D; age <= 184.0D; age += 0.5D) {
            expect("row 7 b keeps the locomotion pose to the bit (twin) at " + age, poses.get(age).get("b"), twins.get(age).get("b"), 0.0D);
        }
        for (double age = 181.5D; age <= 184.0D; age += 0.5D) {
            expect("row 7 after the release: a = twin exactly at " + age, poses.get(age).get("a"), twins.get(age).get("a"), 0.0D);
        }
        // The ramps live in the manager's extra data.
        check("the ramps are in the manager's extra data", weighted.manager.getData(ContractLayers.RAMPS) == contract.ramps());

        System.out.println(failures == 0 ? "WEIGHTS BLEND PROBE PASS: every row within " + TOLERANCE + " rad"
                : "WEIGHTS BLEND PROBE FAIL: " + failures + " check(s)");
        System.exit(failures == 0 ? 0 : 1);
    }

    /** GeckoLib's internal value of an authored rotation-X key in degrees: the loader negates X (BakedAnimationsAdapter 215-224). */
    private static double internal(double authoredDegrees) {
        return (float) Math.toRadians(-authoredDegrees);
    }

    private static void expect(String what, double actual, double expected, double tolerance) {
        double delta = Math.abs(actual - expected);
        boolean pass = delta <= tolerance;
        if (!pass) {
            failures++;
        }
        System.out.println(String.format(Locale.ROOT, "%s %s: actual %.7f expected %.7f (delta %.3g, tolerance %.1g)",
                pass ? "PASS" : "FAIL", what, actual, expected, delta, tolerance));
    }

    private static void check(String what, boolean pass) {
        if (!pass) {
            failures++;
        }
        System.out.println((pass ? "PASS " : "FAIL ") + what);
    }

    // ------------------------------------------------------------------ clips and rig (the KeyframeLegTests idiom)

    private static BakedAnimations bakeClips(String json) {
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        return KeyFramesAdapter.GEO_GSON.fromJson(GsonHelper.getAsJsonObject(root, "animations"), BakedAnimations.class);
    }

    private static Animation bake(String name, JsonObject clip) {
        JsonObject animations = new JsonObject();
        animations.add(name, clip);
        JsonObject root = new JsonObject();
        root.addProperty("format_version", "1.8.0");
        root.add("animations", animations);
        Animation animation = bakeClips(root.toString()).getAnimation(name);
        if (animation == null) {
            throw new IllegalStateException("GeckoLib baked no clip " + name);
        }
        return animation;
    }

    private static JsonObject rotationKeys(double[] degrees, String lerp, double seconds) {
        JsonObject rotation = new JsonObject();
        int segments = degrees.length - 1;
        for (int index = 0; index < degrees.length; index++) {
            JsonObject key = new JsonObject();
            JsonArray post = new JsonArray();
            post.add(degrees[index]);
            post.add(0);
            post.add(0);
            key.add("post", post);
            key.addProperty("lerp_mode", lerp);
            rotation.add(index == 0 ? "0.0" : String.format(Locale.ROOT, "%.10f", seconds * index / segments), key);
        }
        return rotation;
    }

    /** Constant rotation-X clips per bone (two equal keys), {@code loop} as given. */
    private static Animation constant(String name, Map<String, Double> degreesByBone, double seconds, boolean loop) {
        JsonObject bones = new JsonObject();
        for (Map.Entry<String, Double> entry : new TreeMap<>(degreesByBone).entrySet()) {
            JsonObject bone = new JsonObject();
            bone.add("rotation", rotationKeys(new double[] {entry.getValue(), entry.getValue()}, "linear", seconds));
            bones.add(entry.getKey(), bone);
        }
        JsonObject clip = new JsonObject();
        clip.addProperty("loop", loop);
        clip.addProperty("animation_length", seconds);
        clip.add("bones", bones);
        return bake(name, clip);
    }

    /** A full-amplitude nine-key catmullrom cosine on {@code bone} plus constants on the others, a 1.0 s loop. */
    private static Animation cosineWith(String name, String bone, double amplitudeDegrees, Map<String, Double> constants, double seconds) {
        double[] degrees = new double[9];
        for (int index = 0; index < 9; index++) {
            degrees[index] = amplitudeDegrees * Math.cos(2.0D * Math.PI * index / 8);
        }
        JsonObject bones = new JsonObject();
        JsonObject cosine = new JsonObject();
        cosine.add("rotation", rotationKeys(degrees, "catmullrom", seconds));
        bones.add(bone, cosine);
        for (Map.Entry<String, Double> entry : new TreeMap<>(constants).entrySet()) {
            JsonObject other = new JsonObject();
            other.add("rotation", rotationKeys(new double[] {entry.getValue(), entry.getValue()}, "linear", seconds));
            bones.add(entry.getKey(), other);
        }
        JsonObject clip = new JsonObject();
        clip.addProperty("loop", true);
        clip.addProperty("animation_length", seconds);
        clip.add("bones", bones);
        return bake(name, clip);
    }

    private static BakedGeoModel bakeRig() {
        Model model = KeyFramesAdapter.GEO_GSON.fromJson(RIG, Model.class);
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

    // ------------------------------------------------------------------ the synthetic pipeline

    private static final class ProbeAnimatable implements GeoAnimatable {
        private final AnimatableInstanceCache cache = new InstancedAnimatableInstanceCache(this);
        private final List<AnimationController<ProbeAnimatable>> controllers = new ArrayList<>();

        @Override
        public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
            this.controllers.forEach(registrar::add);
        }

        @Override
        public AnimatableInstanceCache getAnimatableInstanceCache() {
            return this.cache;
        }

        @Override
        public double getTick(Object relatedObject) {
            return 0.0D;
        }

        /** The replacement's value ({@code OreSpawnGeoReplacement.getBoneResetTime}): the 3-tick blend-out. */
        @Override
        public double getBoneResetTime() {
            return TriggerMask.BONE_RESET_TICKS;
        }
    }

    private static final class ClipModel extends GeoModel<ProbeAnimatable> {
        private static final ResourceLocation PROBE = ResourceLocation.fromNamespaceAndPath("orespawn", "g1/weights_blend_probe");
        private final BakedAnimations clips;

        ClipModel(BakedAnimations clips) {
            this.clips = clips;
        }

        @Override
        public Animation getAnimation(ProbeAnimatable animatable, String name) {
            Animation animation = this.clips.getAnimation(name);
            if (animation == null) {
                throw new IllegalStateException("weights blend probe: clip missing: " + name);
            }
            return animation;
        }

        @Override
        public ResourceLocation getModelResource(ProbeAnimatable animatable) {
            return PROBE;
        }

        @Override
        public ResourceLocation getTextureResource(ProbeAnimatable animatable) {
            return PROBE;
        }

        @Override
        public ResourceLocation getAnimationResource(ProbeAnimatable animatable) {
            return PROBE;
        }
    }

    /** The production weighted contract on one persistent manager, ticked through GeckoLib's processor. */
    private static final class Weighted {
        private final ProbeAnimatable animatable = new ProbeAnimatable();
        private final ClipModel model;
        private final Map<String, GeoBone> bones;
        private final ContractLayers contract;
        private final AnimatableManager<ProbeAnimatable> manager;

        Weighted(BakedAnimations clips) {
            this.model = new ClipModel(clips);
            BakedGeoModel baked = bakeRig();
            this.model.getAnimationProcessor().setActiveModel(baked);
            this.bones = bones(baked);
            this.contract = ContractLayers.build(GROUPS, clips, LocomotionKind.FLYER, AttackingFlag.NONE);
            AnimatableManager.ControllerRegistrar registrar = new AnimatableManager.ControllerRegistrar(new ArrayList<>());
            this.contract.register(registrar, this.animatable, state -> (float) state.getAnimationTick());
            for (AnimationController<? extends GeoAnimatable> controller : registrar.controllers()) {
                @SuppressWarnings("unchecked")
                AnimationController<ProbeAnimatable> typed = (AnimationController<ProbeAnimatable>) controller;
                this.animatable.controllers.add(typed);
            }
            this.manager = new AnimatableManager<>(this.animatable);
        }

        /** One frame: the model's per-frame hook (beginFrame on the frame's inputs), then GeckoLib's processor. */
        Map<String, Float> frame(double age, float limbSwingAmount, boolean inWater, boolean flying, int hurtTime) {
            MotionInputs inputs = MotionInputs.of((float) age, limbSwingAmount, inWater, flying).withSignals(0, hurtTime, 0);
            this.contract.beginFrame(this.manager, inputs);
            AnimationState<ProbeAnimatable> state = new AnimationState<>(this.animatable, 0.0F, limbSwingAmount, 0.0F, limbSwingAmount != 0.0F);
            state.animationTick = age;
            this.model.getAnimationProcessor().tickAnimation(this.animatable, this.model, this.manager, age, state, true);
            return pose();
        }

        Map<String, Float> pose() {
            Map<String, Float> pose = new TreeMap<>();
            for (String bone : BONES) {
                pose.put(bone, this.bones.get(bone).getRotX());
            }
            return pose;
        }
    }

    /** The transcription path: the always-on layers ({@link KeyframeLayer#controller}) on the same clips. */
    private static final class Transcription {
        private final ProbeAnimatable animatable = new ProbeAnimatable();
        private final ClipModel model;
        private final Map<String, GeoBone> bones;
        private final AnimatableManager<ProbeAnimatable> manager;

        Transcription(BakedAnimations clips) {
            this.model = new ClipModel(clips);
            BakedGeoModel baked = bakeRig();
            this.model.getAnimationProcessor().setActiveModel(baked);
            this.bones = bones(baked);
            for (KeyframeLayer layer : GROUPS) {
                this.animatable.controllers.add(layer.controller(this.animatable, state -> (float) state.getAnimationTick(),
                        AnimationState::getLimbSwingAmount));
            }
            this.manager = new AnimatableManager<>(this.animatable);
        }

        Map<String, Float> frame(double age, float limbSwingAmount) {
            AnimationState<ProbeAnimatable> state = new AnimationState<>(this.animatable, 0.0F, limbSwingAmount, 0.0F, limbSwingAmount != 0.0F);
            state.animationTick = age;
            this.model.getAnimationProcessor().tickAnimation(this.animatable, this.model, this.manager, age, state, true);
            Map<String, Float> pose = new TreeMap<>();
            for (String bone : BONES) {
                pose.put(bone, this.bones.get(bone).getRotX());
            }
            return pose;
        }
    }
}
