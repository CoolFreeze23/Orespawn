package danger.orespawn.entity.client.animation;

import java.util.Map;
import java.util.Set;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.EasingType;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.keyframe.AnimationPoint;
import software.bernie.geckolib.animation.keyframe.AnimationPointQueue;
import software.bernie.geckolib.animation.keyframe.BoneAnimationQueue;
import software.bernie.geckolib.animation.state.BoneSnapshot;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;

/**
 * A GeckoLib controller whose clock is the entity's age, for one frequency group of a species'
 * classic trig animation authored as an editable looping clip.
 *
 * <p>Returned from the salvaged G3 lane (commit 0d238ba, {@code entity/animation/}) with the
 * first Tier-2 slice, under the Phase G rulings (FIX_LOG "PHASE G RULING - AMENDMENT 1" and its
 * ADDENDA; scope addendum item 24 (5)-(14), 2026-09-06). What it does, in order:</p>
 * <ol>
 *   <li>computes the classic float phase exactly as the compiled {@code setupAnim} does -
 *       {@code (float) ageInTicks * omega * wingspeed}, float arithmetic left to right, where
 *       {@code wingspeed} is the chain's second multiply (the Beaver's {@code ANIM_SPEED} 1.0F; a species'
 *       {@code wingspeed} field, e.g. {@code ageInTicks * 1.3f * 0.2f} - never pre-multiplied into
 *       {@code omega}, since {@code (age * 1.3f) * 0.2f} and {@code age * 0.26f} round differently) - and
 *       hands it to GeckoLib as the controller clock (ANIM-001: an entity first seen late must not restart
 *       at phase zero, so the ordinary relative {@code tickOffset} clock is never used);</li>
 *   <li>primes itself straight into RUNNING on its first call, bypassing GeckoLib 4.8.4's
 *       zero-length TRANSITIONING bootstrap ({@code AnimationController.process} offsets 242-324
 *       poll the first stage and feed a snapshot-to-first-key point for one frame) - without the
 *       prime that frame shows the clip at t = 0;</li>
 *   <li>maps the phase onto clip time in {@link #adjustTick} by reproducing {@code Mth.cos}'s
 *       table-index chain ({@code * 10430.378F + 16384F}, JVM {@code f2i}, {@code & 65535}, then
 *       the cosine quarter-turn), so the clip is sampled at exactly the phase the classic LUT would
 *       have used, {@code f2i} saturation at absurd ages included; the index is scaled by the clip's
 *       DECLARED length (ADDENDA (1) / P6: {@code animation_length} seconds x 20, read at prime
 *       time from {@code Animation.length()} - {@code BakedAnimationsAdapter.bakeAnimation} 1-19) -
 *       so the time-warp ratio is declaredLength / naturalPeriod and an artist stretching the clip
 *       changes nothing in-game (Q14 (a): every loop is authored at 1.0 s; the SPEC states the
 *       tempo);</li>
 *   <li>after GeckoLib has evaluated the clip's keyframes into per-bone queues, multiplies the named
 *       gait bones' rotation points by a per-frame amplitude ({@code limbSwingAmount}: Amendment 1
 *       point 3 - the clip is authored at full amplitude, the controller scales the delta) and
 *       collapses each to a constant point, which {@code AnimationProcessor.tickAnimation} then
 *       writes as {@code value + initialSnapshot} (offsets 323-341): the scaled quantity is the delta
 *       from the BIND pose;</li>
 *   <li>in the LAYERED (additive) mode - Q2 (a) / Q13 (a), the weight-blended locomotion layers -
 *       adds that scaled delta to the delta earlier layers wrote to the bone THIS frame instead of
 *       replacing it. GeckoLib drains each controller's queues right after its {@code process}
 *       ({@code tickAnimation} 87-410) and marks the bone changed (407), and clears every marker at
 *       the end of the frame (1250), so "earlier layers this frame" is exactly
 *       {@code bone.hasRotationChanged()}: a bone not yet marked carries LAST frame's write, which
 *       is never added (the (e) demo's scratch variant read the bone unconditionally; a persistent
 *       manager would have accumulated - the demo measured a fresh bake per sample and could not
 *       see it). Two controllers on one bone otherwise compose as last-registered-wins (measured
 *       1.412 rad off the sum; additive 5.2e-8 rad, {@code demo_results.json} G).</li>
 * </ol>
 *
 * <p>Event keyframes (sound / particle / custom-instruction) on a phase-locked loop fire ONCE per
 * manager lifetime: GeckoLib clears {@code executedKeyFrames} only in {@code resetEventKeyFrames},
 * behind {@code process}'s {@code adjustedTick >= animation.length()} loop gate, which
 * {@link #adjustTick} ({@code index * L / 65536 < L}) never reaches - whether the contract's package
 * validator rejects event keyframes on phase-locked loops or the controller clears the set on the
 * clip-clock cycle change is the owner's call (item 15 refuter A, D2: recorded OPEN; nothing here is
 * changed).</p>
 *
 * <p>One controller per frequency group, registered in the order the species' layers declare them
 * ({@link KeyframeLayer}); a bone belongs to exactly one locomotion group (Amendment 1 point 5). The
 * controller never resets GeckoLib's loop: {@link #adjustTick} always returns a time inside the clip,
 * so the ordinary loop reset - which cannot rebase an absolute clock - never fires. Instrumentation
 * ({@link #declaredClipTicks()}, {@link #lastClipTick()}, {@link #lastCosineIndex()},
 * {@link #clipTicksPerSourceTick()}) exists for the harness and the records; nothing in-game reads
 * it.</p>
 */
public final class PhaseLockedKeyframeController<T extends GeoAnimatable>
        extends AnimationController<T> {
    /** {@code Mth.cos}: {@code SIN[(int) (value * 10430.378F + 16384.0F) & 0xFFFF]}. */
    private static final float CLASSIC_TRIG_INDEX_SCALE = 10430.378F;
    private static final float CLASSIC_COSINE_INDEX_OFFSET = 16384.0F;
    private static final int CLASSIC_TRIG_INDEX_MASK = 65535;
    /** The LUT's size: one full turn of the classic phase. */
    public static final int CLASSIC_TRIG_INDEX_COUNT = 65536;
    private static final int CLASSIC_COSINE_QUARTER_TURN = 16384;

    /** Primitive-float state input; avoids an accidental double phase carrier. */
    @FunctionalInterface
    public interface StateFloatFunction<A extends GeoAnimatable> {
        float applyAsFloat(AnimationState<A> state);
    }

    private final float angularFrequencyRadiansPerSourceTick;
    /** The classic chain's second multiply ({@code ageInTicks * omega * wingspeed}); 1.0F when the chain has none. */
    private final float wingspeed;
    private final StateFloatFunction<T> sourceAgeTicks;
    private final Set<String> amplitudeScaledRotationBones;
    private final StateFloatFunction<T> rotationAmplitude;
    private final boolean additive;
    /** ADDENDA (1): the loaded clip's declared length in ticks, read at prime; NaN until primed. */
    private double declaredClipTicks = Double.NaN;
    private double lastClipTick = Double.NaN;
    private int lastCosineIndex = -1;

    private PhaseLockedKeyframeController(
            T animatable,
            String name,
            float angularFrequencyRadiansPerSourceTick,
            float wingspeed,
            StateFloatFunction<T> sourceAgeTicks,
            Set<String> amplitudeScaledRotationBones,
            StateFloatFunction<T> rotationAmplitude,
            boolean additive,
            AnimationStateHandler<T> stateHandler) {
        super(animatable, name, 0, stateHandler);
        validateAngularFrequency(angularFrequencyRadiansPerSourceTick);
        if (!Float.isFinite(wingspeed) || wingspeed <= 0.0F) {
            throw new IllegalArgumentException("Wingspeed must be finite and positive");
        }
        this.angularFrequencyRadiansPerSourceTick = angularFrequencyRadiansPerSourceTick;
        this.wingspeed = wingspeed;
        this.sourceAgeTicks = sourceAgeTicks;
        this.amplitudeScaledRotationBones = Set.copyOf(amplitudeScaledRotationBones);
        this.rotationAmplitude = rotationAmplitude;
        this.additive = additive;
    }

    /** A phase-locked controller whose authored channels stay unscaled and replace the bone (the salvaged shape); the chain's second multiply is 1.0F. */
    public static <T extends GeoAnimatable> PhaseLockedKeyframeController<T> unscaled(
            T animatable,
            String name,
            float angularFrequencyRadiansPerSourceTick,
            StateFloatFunction<T> sourceAgeTicks,
            AnimationStateHandler<T> stateHandler) {
        return unscaled(animatable, name, angularFrequencyRadiansPerSourceTick, 1.0F, sourceAgeTicks, stateHandler);
    }

    /** As {@link #unscaled(GeoAnimatable, String, float, StateFloatFunction, AnimationStateHandler)}, with the chain's second multiply ({@code wingspeed}). */
    public static <T extends GeoAnimatable> PhaseLockedKeyframeController<T> unscaled(
            T animatable,
            String name,
            float angularFrequencyRadiansPerSourceTick,
            float wingspeed,
            StateFloatFunction<T> sourceAgeTicks,
            AnimationStateHandler<T> stateHandler) {
        return new PhaseLockedKeyframeController<>(
                animatable, name, angularFrequencyRadiansPerSourceTick, wingspeed, sourceAgeTicks,
                Set.of(), state -> 1.0F, false, stateHandler);
    }

    /**
     * A phase-locked controller that multiplies only the named bones' authored rotation channels by a
     * per-frame amplitude; {@code additive} selects the LAYERED composition (class javadoc, item 5).
     * The chain's second multiply is 1.0F.
     */
    public static <T extends GeoAnimatable> PhaseLockedKeyframeController<T> amplitudeScaledRotations(
            T animatable,
            String name,
            float angularFrequencyRadiansPerSourceTick,
            StateFloatFunction<T> sourceAgeTicks,
            Set<String> amplitudeScaledRotationBones,
            StateFloatFunction<T> rotationAmplitude,
            boolean additive,
            AnimationStateHandler<T> stateHandler) {
        return amplitudeScaledRotations(animatable, name, angularFrequencyRadiansPerSourceTick, 1.0F, sourceAgeTicks,
                amplitudeScaledRotationBones, rotationAmplitude, additive, stateHandler);
    }

    /** As the eight-argument form, with the chain's second multiply ({@code wingspeed}). */
    public static <T extends GeoAnimatable> PhaseLockedKeyframeController<T> amplitudeScaledRotations(
            T animatable,
            String name,
            float angularFrequencyRadiansPerSourceTick,
            float wingspeed,
            StateFloatFunction<T> sourceAgeTicks,
            Set<String> amplitudeScaledRotationBones,
            StateFloatFunction<T> rotationAmplitude,
            boolean additive,
            AnimationStateHandler<T> stateHandler) {
        if (amplitudeScaledRotationBones.isEmpty()) {
            throw new IllegalArgumentException("Amplitude-scaled controller requires at least one rotation bone");
        }
        return new PhaseLockedKeyframeController<>(
                animatable, name, angularFrequencyRadiansPerSourceTick, wingspeed, sourceAgeTicks,
                amplitudeScaledRotationBones, rotationAmplitude, additive, stateHandler);
    }

    private static void validateAngularFrequency(float angularFrequencyRadiansPerSourceTick) {
        if (!Float.isFinite(angularFrequencyRadiansPerSourceTick) || angularFrequencyRadiansPerSourceTick <= 0.0F) {
            throw new IllegalArgumentException("Angular frequency must be finite and positive");
        }
    }

    /** The clip's declared length in ticks ({@code animation_length} x 20), NaN before the first frame primed it. */
    public double declaredClipTicks() {
        return this.declaredClipTicks;
    }

    /** The clip time the last {@link #adjustTick} produced, NaN before the first frame. */
    public double lastClipTick() {
        return this.lastClipTick;
    }

    /** The cosine LUT index the last {@link #adjustTick} selected (0..65535), -1 before the first frame. */
    public int lastCosineIndex() {
        return this.lastCosineIndex;
    }

    /** The time-warp ratio: clip ticks advanced per source tick, {@code declaredClipTicks * omega * wingspeed / 2 pi}. */
    public double clipTicksPerSourceTick() {
        return this.declaredClipTicks * ((double) this.angularFrequencyRadiansPerSourceTick * (double) this.wingspeed)
                / (2.0D * Math.PI);
    }

    public float angularFrequencyRadiansPerSourceTick() {
        return this.angularFrequencyRadiansPerSourceTick;
    }

    /** The classic chain's second multiply; 1.0F when the chain has none. */
    public float wingspeed() {
        return this.wingspeed;
    }

    public Set<String> amplitudeScaledRotationBones() {
        return this.amplitudeScaledRotationBones;
    }

    public boolean additive() {
        return this.additive;
    }

    /**
     * The classic phase-selection chain on its own, for the harness's wrap sampler: the cosine LUT
     * index {@code Mth.cos(phase)} reads, after the quarter-turn shift this controller applies -
     * {@code ((int) (phase * 10430.378F + 16384F) & 65535) - 16384) & 65535}.
     */
    public static int classicCosineIndex(float phaseRadians) {
        float classicIndexInput = phaseRadians * CLASSIC_TRIG_INDEX_SCALE;
        classicIndexInput = classicIndexInput + CLASSIC_COSINE_INDEX_OFFSET;
        int classicSineIndex = ((int) classicIndexInput) & CLASSIC_TRIG_INDEX_MASK;
        return (classicSineIndex - CLASSIC_COSINE_QUARTER_TURN) & CLASSIC_TRIG_INDEX_MASK;
    }

    /** The classic float phase for a source age: {@code (float) age * omega * wingspeed}, the compiled chain left to right. */
    public float classicPhase(float sourceAgeTick) {
        return classicPhase(sourceAgeTick, this.angularFrequencyRadiansPerSourceTick, this.wingspeed);
    }

    /**
     * The compiled chain on its own, for the harness: {@code (age * omega) * wingspeed} in float32, two
     * roundings in the classic order ({@code ModelBeaver.setupAnim}: {@code ageInTicks * 3.7F * ANIM_SPEED};
     * {@code BrutalflyModel.setupAnim}: {@code ageInTicks * 1.3f * this.wingspeed}).
     */
    public static float classicPhase(float sourceAgeTick, float omega, float wingspeed) {
        float phaseRadians = sourceAgeTick * omega;
        return phaseRadians * wingspeed;
    }

    @Override
    public void process(
            GeoModel<T> model,
            AnimationState<T> state,
            Map<String, GeoBone> bones,
            Map<String, BoneSnapshot> snapshots,
            double ignoredControllerClock,
            boolean crashWhenBoneMissing) {
        // Reproduce the classic model's bytecode before any widening: float age,
        // float frequency, then the legacy left-to-right * 1.0F operation.
        float sourceAgeTick = this.sourceAgeTicks.applyAsFloat(state);
        float phaseRadians = classicPhase(sourceAgeTick);
        if (!Float.isFinite(sourceAgeTick) || !Float.isFinite(phaseRadians)) {
            throw new IllegalStateException("Animation float age/phase must be finite");
        }

        primeFirstFrame(model, state);
        // A widened float is only a lossless carrier required by GeckoLib's
        // process signature. adjustTick rejects any non-float-exact carrier.
        super.process(model, state, bones, snapshots, (double) phaseRadians, crashWhenBoneMissing);

        if (this.amplitudeScaledRotationBones.isEmpty()) {
            return;
        }

        float amplitude = this.rotationAmplitude.applyAsFloat(state);
        if (!Float.isFinite(amplitude)) {
            throw new IllegalStateException("Animation rotation amplitude must be finite");
        }

        EasingType overrideEasing = this.overrideEasingTypeFunction.apply(this.animatable);
        for (String boneName : this.amplitudeScaledRotationBones) {
            BoneAnimationQueue queue = getBoneAnimationQueues().get(boneName);
            if (queue == null) {
                throw new IllegalStateException("Amplitude-scaled animation is missing bone " + boneName);
            }
            float baseX = 0.0F;
            float baseY = 0.0F;
            float baseZ = 0.0F;
            if (this.additive) {
                GeoBone bone = queue.bone();
                // LAYERED: only a bone an earlier controller wrote THIS frame carries a delta to add to;
                // an unmarked bone holds last frame's value (tickAnimation 407 marks, 1250 clears).
                if (bone.hasRotationChanged()) {
                    BoneSnapshot initial = bone.getInitialSnapshot();
                    baseX = bone.getRotX() - initial.getRotX();
                    baseY = bone.getRotY() - initial.getRotY();
                    baseZ = bone.getRotZ() - initial.getRotZ();
                }
            }
            scale(queue.rotationXQueue(), amplitude, baseX, overrideEasing);
            scale(queue.rotationYQueue(), amplitude, baseY, overrideEasing);
            scale(queue.rotationZQueue(), amplitude, baseZ, overrideEasing);
        }
    }

    /**
     * Bypasses GeckoLib 4.8.4's zero-length TRANSITIONING bootstrap. Without this prime, a
     * controller first seen late evaluates its target clip at t = 0 for one frame before the absolute
     * phase clock takes effect. Reads the clip's declared length (ADDENDA (1)) and refuses a clip
     * that does not loop in both the JSON and the controller stage (the contract pins the loop type
     * per role; the artist may not change it).
     */
    private void primeFirstFrame(GeoModel<T> model, AnimationState<T> state) {
        if (this.currentAnimation != null || this.currentRawAnimation != null) {
            return;
        }

        this.lastModel = model;
        PlayState initialState = handleAnimationState(state.withController(this));
        if (initialState == PlayState.STOP || this.animationQueue.isEmpty()) {
            return;
        }

        this.currentAnimation = this.animationQueue.poll();
        Animation loadedAnimation = this.currentAnimation.animation();
        double declared = loadedAnimation.length();
        if (!(declared > 0.0D) || !Double.isFinite(declared)) {
            throw new IllegalStateException(
                    "Phase-locked animation must declare a positive animation_length: " + loadedAnimation.name());
        }
        this.declaredClipTicks = declared;
        if (loadedAnimation.loopType() != Animation.LoopType.LOOP
                || this.currentAnimation.loopType() != Animation.LoopType.LOOP) {
            throw new IllegalStateException(
                    "Phase-locked animation must loop in both JSON and controller: " + loadedAnimation.name());
        }

        this.animationState = State.RUNNING;
        this.justStartedTransition = false;
        this.shouldResetTick = false;
    }

    @Override
    protected double adjustTick(double exactFloatPhaseCarrier) {
        // Preserve AnimationController's reset-state side effect while replacing
        // its relative tickOffset clock with the absolute legacy source phase.
        // Keeping the result inside the clip is essential: GeckoLib's ordinary
        // loop reset cannot rebase an absolute clock.
        this.shouldResetTick = false;
        float phaseRadians = (float) exactFloatPhaseCarrier;
        if (!Double.isFinite(exactFloatPhaseCarrier)
                || !Float.isFinite(phaseRadians)
                || (double) phaseRadians != exactFloatPhaseCarrier) {
            throw new IllegalStateException("Animation phase carrier must be an exactly widened float");
        }
        if (Double.isNaN(this.declaredClipTicks)) {
            return 0.0D; // not primed (no clip): the base class stops before using the tick
        }

        // The selected LUT index is translated into the corresponding time in the editable
        // clip of DECLARED length (ADDENDA (1)); the cosine is never evaluated here.
        int authoredCosineIndex = classicCosineIndex(phaseRadians);
        this.lastCosineIndex = authoredCosineIndex;
        this.lastClipTick = (double) authoredCosineIndex * this.declaredClipTicks / (double) CLASSIC_TRIG_INDEX_COUNT;
        return this.lastClipTick;
    }

    private static void scale(AnimationPointQueue queue, float amplitude, float base, EasingType overrideEasing) {
        // Index directly into GeckoLib's queue. A capturing replaceAll lambda or
        // iterator would add one allocation per XYZ queue on top of the single
        // replacement AnimationPoint that this blend actually needs.
        for (int pointIndex = 0; pointIndex < queue.size(); pointIndex++) {
            AnimationPoint point = queue.get(pointIndex);
            queue.set(pointIndex, scaled(point, amplitude, base, overrideEasing));
        }
    }

    private static AnimationPoint scaled(AnimationPoint point, float amplitude, float base, EasingType overrideEasing) {
        // Evaluate the authored spline exactly once with the controller's active
        // easing override, then collapse it to a constant point. This keeps gait
        // blending proportional without rebuilding Keyframes, Constants, or
        // easing-argument collections for every XYZ queue on every render.
        float authoredValue = (float) EasingType.lerpWithOverride(point, overrideEasing);
        float scaledValue = authoredValue * amplitude;
        scaledValue = scaledValue + base; // base is 0 unless LAYERED and an earlier layer wrote this frame
        return new AnimationPoint(
                null,
                point.currentTick(),
                point.transitionLength(),
                (double) scaledValue,
                (double) scaledValue);
    }
}
