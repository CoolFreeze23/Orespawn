package danger.orespawn.entity.client.animation;

import danger.orespawn.entity.client.MotionInputs;
import danger.orespawn.entity.client.animation.PhaseLockedKeyframeController.StateFloatFunction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.keyframe.BoneAnimation;
import software.bernie.geckolib.constant.dataticket.DataTicket;
import software.bernie.geckolib.loading.object.BakedAnimations;

/**
 * THE WEIGHTED CONTRACT of one ARTIST-delivered species, per manager (the weights slice, and as decided; {@code
 * contract_design.md} sections 2.1, 2.4, 3, 3.1, 4 and 5; {@code controller_design.md} sections 3 and 5):
 * the locomotion clips of every frequency group playing TOGETHER, each at a weight, plus the triggered clips on
 * their one controller.
 *
 * <p>THE CONSTRAINT (item 26 (7)): every exact transcription stays bit-exact to its classic hook - weights apply to
 * ARTIST clips only, and transcription groups are always-on. The line between the two is the FILE:
 * {@link #isTranscription} is true for the generator's output ({@code tools/keyframe_clip.py}: an {@code idle} that
 * keys no bone plus the declared groups' walk-family clips, or a subset of them) and such a file registers the
 * always-on layers exactly as before this slice ({@link KeyframeLayer#controller}: the gait group scaled by
 * {@code limbSwingAmount} and additive, every other group unscaled, weight 1, never blended, never replaced - the
 * harness's keyframe leg is the proof); any other file is an artist delivery - an {@code idle} that keys a bone, a
 * {@code fly} / {@code swim} / {@code idle_<group>} / {@code aggro_idle} / {@code calm_idle}, a triggered clip - and
 * registers the weighted contract below (P1: no parity proof applies to artist clips; an in-game look accepts
 * them).</p>
 *
 * <p>THE LAYERS: for every declared frequency group ({@link KeyframeLayer}: the SPEC's tempo table) and every
 * locomotion state whose clip the file carries and keys at least one bone - the bare names on the primary group
 * (the one whose clip is the bare {@code walk}), {@code <state>_<group>} on every other (section 2.1's naming rule)
 * - one {@link PhaseLockedKeyframeController#weighted weighted} phase-locked controller at the group's tempo, its
 * every keyed channel scaled by the state's weight and composed additively: the loops of one group share the clock
 * {@code ageInTicks x omega}, so a blend between them is phase-coherent (section 5.3). The state weights are the
 * products of {@link LocomotionWeights}; a state whose clip a group lacks hands its weight down section 2.4's
 * fallback chain - {@code fly -> walk -> idle}, {@code swim -> walk -> idle}, {@code walk -> idle} (a missing
 * {@code walk_<group>} takes {@code idle_<group>}, then nothing: the bones hold bind), the aggro share
 * {@code aggro_idle -> calm_idle -> idle} and the calm share {@code calm_idle -> idle} - so every present clip's weight
 * is the sum of the shares that fall to it and the shares always sum to one.</p>
 *
 * <p>THE FRAME: {@link #beginFrame} runs once per frame from the replaced renderer's model, before any controller
 * ({@code OreSpawnGeoReplacementModel.handleAnimations}): it reads the frame's {@link MotionInputs}, advances the
 * three ramps ({@link WeightRamp}, stored in the manager's extra data under {@link #RAMPS}), computes every layer's
 * weight, and fires the client-observed triggers on their edges (sections 4.1, 4.2, 4.4 transport 2), keyed on the
 * last-seen values the same extra data holds - so a repeated frame fires nothing twice and a skipped frame is
 * caught up (ENT-S-147).</p>
 */
public final class ContractLayers {
    /** The per-manager ramps and edge memories, in the manager's extra data (contract section 3.1's "a float per weight"). */
    public static final DataTicket<WeightRamps> RAMPS = new DataTicket<>("orespawn_contract_ramps", WeightRamps.class);
    public static final String IDLE = KeyframeLayer.IDLE;
    public static final String WALK = KeyframeLayer.WALK;
    public static final String SWIM = "swim";
    public static final String FLY = "fly";
    public static final String AGGRO_IDLE = "aggro_idle";
    public static final String CALM_IDLE = "calm_idle";
    public static final String ATTACK = "attack";
    public static final String HURT = "hurt";
    public static final String DEATH = "death";
    /** The contract's locomotion states, in layer order. */
    public static final List<String> LOCOMOTION_STATES = List.of(IDLE, WALK, SWIM, FLY, AGGRO_IDLE, CALM_IDLE);
    /** Section 2.4's fallback chains: the state that carries a share, and the clips that may play it, in order. */
    private static final Map<String, List<String>> FALLBACK = Map.of(
            FLY, List.of(FLY, WALK, IDLE),
            SWIM, List.of(SWIM, WALK, IDLE),
            WALK, List.of(WALK, IDLE),
            AGGRO_IDLE, List.of(AGGRO_IDLE, CALM_IDLE, IDLE),
            CALM_IDLE, List.of(CALM_IDLE, IDLE));
    /** The shares, in the order they are summed. */
    private static final List<String> SHARES = List.of(FLY, SWIM, WALK, AGGRO_IDLE, CALM_IDLE);
    /** Section 4.5's variations: one-shots by design, rolled by the idle loop - not triggers (not landed in this slice). */
    private static final Pattern IDLE_ALT = Pattern.compile("idle_alt_\\d+");
    private static final Logger LOGGER = LoggerFactory.getLogger("orespawn");
    /** The locomotion clips (one per loaded file) already reported for a wrong loop type or a missing length (loud once, the DrawOrder form). */
    private static final Set<Animation> WRONG_LOOP_LOGGED = ConcurrentHashMap.newKeySet();

    /** One weighted locomotion layer: a state's clip on one frequency group, over the bones the clip keys. */
    public record Layer(KeyframeLayer group, String state, String clip, Set<String> bones) {
        public Layer {
            bones = Set.copyOf(bones);
        }

        /** The controller's name in the manager's map: {@code keyframe:<clip>}, the transcription layers' form. */
        public String controllerName() {
            return "keyframe:" + this.clip;
        }
    }

    /** The per-manager state the ramps and edge detectors keep between frames. */
    public static final class WeightRamps {
        public final WeightRamp fly = new WeightRamp();
        public final WeightRamp swim = new WeightRamp();
        public final WeightRamp aggro = new WeightRamp();
        private int lastHurtTime;
        private int lastDeathTime;
        private int lastAttacking;

        public int lastHurtTime() {
            return this.lastHurtTime;
        }

        public int lastDeathTime() {
            return this.lastDeathTime;
        }

        public int lastAttacking() {
            return this.lastAttacking;
        }
    }

    private final LocomotionKind locomotion;
    private final AttackingFlag attackingFlag;
    private final List<Layer> layers;
    /** Per layer: the shares (of {@link #SHARES}) that fall to it through the fallback chains. */
    private final List<List<String>> contributors;
    private final Map<String, Animation> triggers;
    private final TriggerMask mask = new TriggerMask();
    private final WeightRamps ramps = new WeightRamps();
    private final float[] frameWeights;
    private LocomotionWeights weights = LocomotionWeights.REST;
    private MotionInputs lastInputs;
    private AnimatableManager<?> manager;

    private ContractLayers(LocomotionKind locomotion, AttackingFlag attackingFlag, List<Layer> layers,
                           List<List<String>> contributors, Map<String, Animation> triggers) {
        this.locomotion = locomotion;
        this.attackingFlag = attackingFlag;
        this.layers = List.copyOf(layers);
        this.contributors = List.copyOf(contributors);
        this.triggers = Collections.unmodifiableMap(new LinkedHashMap<>(triggers));
        this.frameWeights = new float[layers.size()];
    }

    /**
     * True for the generator's transcription file (or a subset of it): {@code idle} present and keying NO bone, and
     * every clip in the file either that {@code idle} or one of the declared groups' walk-family clips. Such a file
     * plays always-on, bit-exact to the classic hook; anything else is an artist delivery.
     */
    public static boolean isTranscription(BakedAnimations clips, List<KeyframeLayer> groups) {
        Animation idle = clips.getAnimation(IDLE);
        if (idle == null || idle.boneAnimations().length != 0) {
            return false;
        }
        Set<String> transcription = new LinkedHashSet<>();
        transcription.add(IDLE);
        for (KeyframeLayer group : groups) {
            transcription.add(group.clip());
        }
        return transcription.containsAll(clips.animations().keySet());
    }

    /** The bones a clip keys (any channel), in the clip's order. */
    public static Set<String> keyedBones(Animation animation) {
        Set<String> bones = new LinkedHashSet<>();
        for (BoneAnimation bone : animation.boneAnimations()) {
            bones.add(bone.boneName());
        }
        return bones;
    }

    /** The clip name of a locomotion state on a group: bare on the primary group, {@code <state>_<group>} elsewhere. */
    public static String stateClip(String state, KeyframeLayer group) {
        return group.clip().equals(WALK) ? state : state + "_" + group.group();
    }

    /**
     * The weighted contract for a species' declared groups over an artist file: one layer per group and present
     * state (a clip keying no bone counts as absent), the triggered clips (every non-looping clip that is not a
     * locomotion clip of some group and not an {@code idle_alt_N}).
     */
    public static ContractLayers build(List<KeyframeLayer> groups, BakedAnimations clips, LocomotionKind locomotion,
                                       AttackingFlag attackingFlag) {
        List<Layer> layers = new ArrayList<>();
        List<List<String>> contributors = new ArrayList<>();
        Set<String> locomotionClips = new LinkedHashSet<>();
        for (KeyframeLayer group : groups) {
            Map<String, Layer> present = new LinkedHashMap<>();
            for (String state : LOCOMOTION_STATES) {
                String clipName = stateClip(state, group);
                locomotionClips.add(clipName);
                Animation clip = clips.getAnimation(clipName);
                if (clip == null) {
                    continue;
                }
                if (!playableLoop(clipName, clip)) {
                    // A locomotion clip delivered with the wrong loop type or without a positive animation_length
                    // (the checker rejects the first and warns on the second): loud once, then treated as absent -
                    // the phase-locked prime would refuse it on the render thread, and a resource pack must never
                    // crash the client (the missing-key policy's form, the weights slice: a pose-only idle
                    // exported without animation_length bakes to length 0). The helper playableLoop (the end
                    // of this class) names the failed condition in its one ERROR line; the transcription loop in
                    // OreSpawnGeoReplacement.registerKeyframeLayers screens the same way, so neither path
                    // reaches the prime with a clip it would refuse.
                    continue;
                }
                Set<String> bones = keyedBones(clip);
                if (bones.isEmpty()) {
                    continue;
                }
                present.put(state, new Layer(group, state, clipName, bones));
            }
            Map<String, List<String>> sharesByState = new LinkedHashMap<>();
            for (String share : SHARES) {
                for (String candidate : FALLBACK.get(share)) {
                    if (present.containsKey(candidate)) {
                        sharesByState.computeIfAbsent(candidate, key -> new ArrayList<>()).add(share);
                        break;
                    }
                }
            }
            for (Map.Entry<String, Layer> entry : present.entrySet()) {
                layers.add(entry.getValue());
                contributors.add(List.copyOf(sharesByState.getOrDefault(entry.getKey(), List.of())));
            }
        }
        Map<String, Animation> triggers = new java.util.TreeMap<>(); // by name: GeckoLib's baked map keeps no file order (cw_003)
        for (Map.Entry<String, Animation> entry : clips.animations().entrySet()) {
            String name = entry.getKey();
            if (locomotionClips.contains(name) || IDLE_ALT.matcher(name).matches()
                    || entry.getValue().loopType() == Animation.LoopType.LOOP) {
                continue;
            }
            triggers.put(name, entry.getValue());
        }
        return new ContractLayers(locomotion, attackingFlag, layers, contributors, triggers);
    }

    /**
     * Registers the trigger controller FIRST (when the file carries a triggered clip), then every locomotion layer
     * in group-then-state order; returns how many controllers were added. {@code ageTicks} is the phase-locked
     * clock's reader (the replacement's entity age; the harness's explicit input).
     */
    public <T extends GeoAnimatable> int register(AnimatableManager.ControllerRegistrar controllers, T animatable,
                                                  StateFloatFunction<T> ageTicks) {
        int registered = 0;
        if (!this.triggers.isEmpty()) {
            controllers.add(new TriggeredClipController<>(animatable, this, this.mask, this.triggers));
            registered++;
        }
        for (int index = 0; index < this.layers.size(); index++) {
            Layer layer = this.layers.get(index);
            int slot = index;
            RawAnimation loop = RawAnimation.begin().thenLoop(layer.clip());
            controllers.add(PhaseLockedKeyframeController.weighted(animatable, layer.controllerName(),
                    layer.group().angularFrequencyRadiansPerTick(), layer.group().wingspeed(), ageTicks, layer.bones(),
                    state -> this.frameWeights[slot], this.mask, this, state -> state.setAndContinue(loop)));
            registered++;
        }
        return registered;
    }

    /**
     * Once per frame, before any controller: the ramps advanced on the frame's clock, every layer's weight
     * computed, the client-observed triggers fired on their edges. Idempotent for a repeated frame.
     */
    public void beginFrame(AnimatableManager<?> manager, MotionInputs inputs) {
        this.manager = manager;
        if (manager != null && manager.getData(RAMPS) != this.ramps) {
            manager.setData(RAMPS, this.ramps);
        }
        float age = inputs.ageInTicks();
        this.lastInputs = inputs;
        this.mask.beginFrame(age);
        float fly = this.ramps.fly.advance(this.locomotion.flyer() && inputs.flying(), age);
        float swim = this.ramps.swim.advance(inputs.inWater(), age);
        float aggro = this.ramps.aggro.advance(this.attackingFlag == AttackingFlag.STATE && inputs.attacking() != 0, age);
        this.weights = new LocomotionWeights(inputs.limbSwingAmount(), swim, fly, aggro);
        for (int index = 0; index < this.layers.size(); index++) {
            float weight = 0.0F;
            for (String share : this.contributors.get(index)) {
                weight += share(share);
            }
            this.frameWeights[index] = weight;
        }
        // The client-observed triggers, in ascending priority (the last tryTriggerAnimation of a frame wins).
        if (this.attackingFlag == AttackingFlag.EVENT && inputs.attacking() != 0 && this.ramps.lastAttacking == 0) {
            fire(ATTACK);
        }
        this.ramps.lastAttacking = inputs.attacking();
        if (inputs.hurtTime() > this.ramps.lastHurtTime) {
            fire(HURT);
        }
        this.ramps.lastHurtTime = inputs.hurtTime();
        if (inputs.deathTime() > 0 && this.ramps.lastDeathTime == 0) {
            fire(DEATH);
        }
        this.ramps.lastDeathTime = inputs.deathTime();
    }

    private float share(String state) {
        return switch (state) {
            case FLY -> this.weights.fly();
            case SWIM -> this.weights.swim();
            case WALK -> this.weights.walk();
            case AGGRO_IDLE -> this.weights.aggroIdle();
            case CALM_IDLE -> this.weights.calmIdle();
            default -> throw new IllegalStateException("no share for state " + state);
        };
    }

    /** Fires a trigger key on this manager's trigger controller, if the file carries that clip. */
    public boolean fire(String key) {
        if (this.manager == null || !this.triggers.containsKey(key)) {
            return false;
        }
        this.manager.tryTriggerAnimation(TriggeredClipController.NAME, key);
        return true;
    }

    /** The contract state a manager's controllers belong to, or null for a manager without the weighted contract. */
    public static ContractLayers of(AnimatableManager<?> manager) {
        for (AnimationController<?> controller : manager.getAnimationControllers().values()) {
            if (controller instanceof TriggeredClipController<?> triggers) {
                return triggers.layers();
            }
            if (controller instanceof PhaseLockedKeyframeController<?> layer && layer.contract() != null) {
                return layer.contract();
            }
        }
        return null;
    }

    public List<Layer> layers() {
        return this.layers;
    }

    /** The shares of {@link #SHARES} that fall to layer {@code index} through the fallback chains. */
    public List<String> contributors(int index) {
        return this.contributors.get(index);
    }

    public Map<String, Animation> triggers() {
        return this.triggers;
    }

    public TriggerMask mask() {
        return this.mask;
    }

    public WeightRamps ramps() {
        return this.ramps;
    }

    public LocomotionKind locomotion() {
        return this.locomotion;
    }

    public AttackingFlag attackingFlag() {
        return this.attackingFlag;
    }

    /** The frame's locomotion weights, from the last {@link #beginFrame}. */
    public LocomotionWeights weights() {
        return this.weights;
    }

    /** The frame's inputs, from the last {@link #beginFrame}; null before the first. */
    public MotionInputs lastInputs() {
        return this.lastInputs;
    }

    /** The weight layer {@code index} carries this frame. */
    public float weight(int index) {
        return this.frameWeights[index];
    }

    /** The weight the named clip carries this frame; 0 for a clip no layer plays. */
    public float weight(String clip) {
        for (int index = 0; index < this.layers.size(); index++) {
            if (this.layers.get(index).clip().equals(clip)) {
                return this.frameWeights[index];
            }
        }
        return 0.0F;
    }

    /**
     * A locomotion clip the phase-locked prime can play: a LOOP with a positive, finite declared length; null (absent)
     * is false without a word. Anything else is reported once per loaded clip (ERROR, naming the failed condition) and
     * treated as absent - the prime would throw on the render thread, and a resource pack must never crash the client
     * (the weights slice: a pose-only idle exported without animation_length bakes to length 0, which the package
     * checker only warns about). Both registration paths screen with this - the weighted contract's and the
     * transcription loop's in OreSpawnGeoReplacement.registerKeyframeLayers.
     */
    public static boolean playableLoop(String clipName, Animation clip) {
        if (clip == null) {
            return false;
        }
        String problem = null;
        if (clip.loopType() != Animation.LoopType.LOOP) {
            problem = "does not loop in its file";
        } else if (!(clip.length() > 0.0D) || !Double.isFinite(clip.length())) {
            problem = "declares no positive animation_length (" + clip.length() + ")";
        }
        if (problem == null) {
            return true;
        }
        if (WRONG_LOOP_LOGGED.add(clip)) {
            LOGGER.error("Phase G animation contract: locomotion clip '{}' {} - the contract plays it as a loop; the clip"
                    + " is ignored until its file is fixed (the package checker rejects a wrong loop type and warns on a"
                    + " missing animation_length)", clipName, problem);
        }
        return false;
    }
}
