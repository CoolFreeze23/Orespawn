package danger.orespawn.entity.client.animation;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.keyframe.BoneAnimationQueue;
import software.bernie.geckolib.animation.state.BoneSnapshot;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;

/**
 * The contract's triggered clips on ONE ordinary GeckoLib controller (contract section 2.2): every
 * play-once clip the species' file carries under the trigger keys the SPEC declares ({@code attack},
 * {@code hurt}, {@code death} - {@code hold_on_last_frame} - and the SPEC's code-triggered extras, each under its
 * own name), registered with {@code triggerableAnim} and fired by {@code
 * AnimatableManager.tryTriggerAnimation("triggers", key)} - from the client-observed edges
 * ({@link ContractLayers#beginFrame}: the {@code hurtTime} rising edge, the first {@code deathTime}, an EVENT
 * flag's rising edge) or from GeckoLib's own packet path ({@code GeoReplacedEntity.triggerAnim} -
 * {@code EntityAnimTriggerPacket} - {@code manager.tryTriggerAnimation}, the salvage's pinned order).
 *
 * <p>GeckoLib 4.8.4 does the blend-in natively: {@code tryTriggerAnimation} sets {@code triggeredAnimation}
 * (offsets 1-53), the next {@code process} builds the queue and enters TRANSITIONING, and for
 * {@code transitionLength} ticks ({@link #TRANSITION_TICKS}, section 5.3's proposed default) feeds every bone the
 * clip keys a point from the bone's CURRENT pose - the manager's snapshot, i.e. the live locomotion pose - to the
 * clip's first key ({@code process} 241-324, {@code BoneAnimationQueue.addNextRotation}: {@code snapshot - initial}
 * to {@code animationStartValue}). The state handler answers STOP, so between triggers the controller sits STOPPED
 * and writes nothing; several triggers in one frame: the last {@code tryTriggerAnimation} wins (4.8.4 replaces
 * {@code triggeredAnimation} outright), so the edge detectors fire in ascending priority - {@code attack},
 * {@code hurt}, {@code death} (section 2.2).</p>
 *
 * <p>Registered FIRST in the manager, so it runs before every locomotion layer of the frame: after its own
 * {@code process} it reads which bones it will write this frame (the queues GeckoLib drains right after,
 * {@code tickAnimation} 87-410) and hands them to the {@link TriggerMask} - the replace policy and the bone-reset
 * blend-out live there.</p>
 */
public final class TriggeredClipController<T extends GeoAnimatable> extends AnimationController<T> {
    /** The controller's name in the manager's map and the {@code controllerName} of every trigger call. */
    public static final String NAME = "triggers";
    /** Contract section 5.3's proposed blend-in: GeckoLib's transition from the current pose to the clip's first key. */
    public static final int TRANSITION_TICKS = 3;

    private final ContractLayers owner;
    private final TriggerMask mask;
    private final Set<String> keys;
    private final Set<String> writing = new HashSet<>();

    TriggeredClipController(T animatable, ContractLayers owner, TriggerMask mask, Map<String, Animation> triggered) {
        super(animatable, NAME, TRANSITION_TICKS, state -> PlayState.STOP);
        this.owner = owner;
        this.mask = mask;
        Set<String> registered = new LinkedHashSet<>();
        triggered.forEach((key, animation) -> {
            RawAnimation stage = animation.loopType() == Animation.LoopType.HOLD_ON_LAST_FRAME
                    ? RawAnimation.begin().thenPlayAndHold(key)
                    : RawAnimation.begin().thenPlay(key);
            triggerableAnim(key, stage);
            registered.add(key);
        });
        this.keys = Collections.unmodifiableSet(registered);
    }

    /** The per-manager contract state this controller belongs to. */
    public ContractLayers layers() {
        return this.owner;
    }

    /** The trigger keys registered, in the file's order. */
    public Set<String> keys() {
        return this.keys;
    }

    /** The bones the triggered clip wrote on the last processed frame. */
    public Set<String> writingBones() {
        return Set.copyOf(this.writing);
    }

    @Override
    public void process(GeoModel<T> model, AnimationState<T> state, Map<String, GeoBone> bones,
                        Map<String, BoneSnapshot> snapshots, double seekTime, boolean crashWhenBoneMissing) {
        super.process(model, state, bones, snapshots, seekTime, crashWhenBoneMissing);
        this.writing.clear();
        for (BoneAnimationQueue queue : getBoneAnimationQueues().values()) {
            if (writes(queue)) {
                this.writing.add(queue.bone().getName());
            }
        }
        this.mask.update(this.writing, snapshots, bones);
    }

    /** A queue GeckoLib will write to its bone this frame: any of the nine channel queues holds a point. */
    private static boolean writes(BoneAnimationQueue queue) {
        return !queue.rotationXQueue().isEmpty() || !queue.rotationYQueue().isEmpty() || !queue.rotationZQueue().isEmpty()
                || !queue.positionXQueue().isEmpty() || !queue.positionYQueue().isEmpty() || !queue.positionZQueue().isEmpty()
                || !queue.scaleXQueue().isEmpty() || !queue.scaleYQueue().isEmpty() || !queue.scaleZQueue().isEmpty();
    }
}
