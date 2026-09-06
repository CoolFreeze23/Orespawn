package danger.orespawn.entity.client.animation;

import danger.orespawn.entity.client.animation.PhaseLockedKeyframeController.StateFloatFunction;
import java.util.Set;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;

/**
 * One frequency group of a species' standard animation contract
 * ({@code phase_g_reports/animation_contract/contract_design.md} sections 1, 2.1 and 5.1, ruled
 * 2026-09-06): the group's name, the clip that animates it, its natural angular frequency in
 * radians per age tick (the tempo the SPEC states; the artist never sets it), the bones it owns
 * (a partition across a species' groups; should two layers ever key one bone, only the LATER
 * layer's additive flag decides: an additive layer adds its scaled delta to whatever earlier layers
 * wrote the bone this frame, a non-additive layer replaces it - and the additive base is read only
 * for the layer's amplitude-scaled bones ({@code amplitudeScaledRotationBones}), so a gait clip that
 * keys a bone outside its group writes that bone non-additively, last-registered-wins; item 15
 * refuter A, D3) and whether it is the gait group, whose delta the controller scales by
 * {@code limbSwingAmount} (Amendment 1 point 3). The gait group's clip carries the bare name
 * {@link #WALK}; every other group's is {@link #walkClip} ({@code walk_<group>}).
 *
 * <p>A layer is data; {@link #controller} builds the {@link PhaseLockedKeyframeController} for it on
 * whatever animatable and state readers the caller supplies - the shipped replacement hands its own
 * entity readers, the headless harness explicit inputs (the S4 doctrine: the same production code on
 * plain inputs).</p>
 *
 * @param group                          the frequency group's name (the SPEC's tempo table row)
 * @param clip                           the clip name in the species' {@code .animation.json}
 * @param angularFrequencyRadiansPerTick the classic formula's {@code omega} ({@code cos(ageInTicks * omega)})
 * @param bones                          the rig bones this group animates
 * @param gaitScaled                     true for the gait group: the delta is scaled by {@code limbSwingAmount}
 */
public record KeyframeLayer(String group, String clip, float angularFrequencyRadiansPerTick, Set<String> bones,
                            boolean gaitScaled) {
    /** The contract's standing loop: no target, not moving. Its presence (or {@link #WALK}'s) makes a species an artist species. */
    public static final String IDLE = "idle";
    /** The contract's ground-locomotion loop on the gait group. */
    public static final String WALK = "walk";

    public KeyframeLayer {
        if (group == null || group.isBlank()) {
            throw new IllegalArgumentException("keyframe layer needs a group name");
        }
        if (clip == null || clip.isBlank()) {
            throw new IllegalArgumentException("keyframe layer " + group + " needs a clip name");
        }
        if (!Float.isFinite(angularFrequencyRadiansPerTick) || angularFrequencyRadiansPerTick <= 0.0F) {
            throw new IllegalArgumentException("keyframe layer " + group + " needs a finite positive angular frequency");
        }
        if (bones == null || bones.isEmpty()) {
            throw new IllegalArgumentException("keyframe layer " + group + " needs at least one bone");
        }
        bones = Set.copyOf(bones);
    }

    /** The contract's group-clip name for a non-gait group of the walk: {@code walk_<group>}. */
    public static String walkClip(String group) {
        return WALK + "_" + group;
    }

    /** The controller's name in the manager's map: {@code keyframe:<clip>}. */
    public String controllerName() {
        return "keyframe:" + this.clip;
    }

    /** The group's natural period in age ticks, {@code 2 pi / omega}. */
    public double naturalPeriodTicks() {
        return 2.0D * Math.PI / this.angularFrequencyRadiansPerTick;
    }

    /**
     * The phase-locked controller for this layer: a looping stage on {@link #clip}; the gait group's
     * rotation delta scaled by {@code limbSwingAmount} and composed additively (the weight-blended
     * locomotion layers, Q2 (a) / Q13 (a)); every other group plays its clip unscaled.
     */
    public <T extends GeoAnimatable> PhaseLockedKeyframeController<T> controller(
            T animatable, StateFloatFunction<T> ageTicks, StateFloatFunction<T> limbSwingAmount) {
        RawAnimation loop = RawAnimation.begin().thenLoop(this.clip);
        AnimationController.AnimationStateHandler<T> handler = state -> state.setAndContinue(loop);
        if (this.gaitScaled) {
            return PhaseLockedKeyframeController.amplitudeScaledRotations(animatable, controllerName(),
                    this.angularFrequencyRadiansPerTick, ageTicks, this.bones, limbSwingAmount, true, handler);
        }
        return PhaseLockedKeyframeController.unscaled(animatable, controllerName(),
                this.angularFrequencyRadiansPerTick, ageTicks, handler);
    }
}
