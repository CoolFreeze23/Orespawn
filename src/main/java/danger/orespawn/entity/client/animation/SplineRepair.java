package danger.orespawn.entity.client.animation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.EasingType;
import software.bernie.geckolib.animation.keyframe.BoneAnimation;
import software.bernie.geckolib.animation.keyframe.Keyframe;
import software.bernie.geckolib.animation.keyframe.KeyframeStack;
import software.bernie.geckolib.loading.math.MathValue;
import software.bernie.geckolib.loading.object.BakedAnimations;

/**
 * The Q9 repair (owner ruling 2026-09-06, scope addendum item 24 (5)-(14), Q9 (a) scoped): GeckoLib
 * 4.8.4's {@code catmullrom} keyframes are rebuilt at clip load with the textbook spline arguments,
 * in the replacement seam only ({@code OreSpawnGeoReplacementModel.getAnimation}); the Queen's native
 * model stays on stock semantics until her own ruling. A deliberate divergence from the library,
 * recorded (PARITY_NOTES, the PN entry drafted with the landing) and reported upstream.
 *
 * <p>The defect, from {@code javap -p -c} of the pinned jar ({@code geckolib-neoforge-1.21.1-4.8.4}):
 * {@code BakedAnimationsAdapter.buildKeyframeStack} builds consecutive keyframes as
 * {@code (length, start = the previous key's value, end = this key's value)} (offsets 418-450: the
 * start operand is {@code aload 6}, the previous iteration's value, once a previous pair exists at
 * 429-441) and {@code addSplineArgs} then gives every CATMULLROM keyframe two easing arguments:
 * {@code args[0] = i == 0 ? frame.startValue() : frames.get(i - 1).endValue()} (119-146) and
 * {@code args[1] = i + 1 < size ? frames.get(i + 1).endValue() : frame.endValue()} (147-183).
 * Because {@code frames.get(i - 1).endValue()} IS {@code frames.get(i).startValue()}, the evaluator
 * ({@code EasingType$CatmullRomEasing.apply} 69-113: {@code getPointOnSpline(t, args[0], start, end,
 * args[1])}, the textbook {@code 0.5 * (2 P1 + (P2 - P0) t + (2 P0 - 5 P1 + 4 P2 - P3) t^2 + (3 P1 - P0
 * - 3 P2 + P3) t^3)} at 0-69) sees P0 == P1 on every segment and P3 == P2 on the last: each segment
 * starts with half the chord slope instead of the neighbour-derived tangent - a kink at every key,
 * an O(h^2) error like linear interpolation with a worse constant (measured on the Beaver's gait:
 * not within 2.5e-3 rad by 97 keys per bone, where the repaired spline holds it at 15; the (e)
 * demonstration, {@code phase_g_reports/animation_contract/controller_design.md} section 7).</p>
 *
 * <p>The repair: for every CATMULLROM keyframe after the zero-length anchor GeckoLib puts first
 * (index 0, {@code start == end == v0}, only ever evaluated at t = 0), P0 becomes the key BEFORE the
 * segment's start ({@code frames.get(i - 1).startValue()}) and P3 the key after its end
 * ({@code frames.get(i + 1).endValue()}); at the ends a loop clip continues across its seam (a loop's
 * last key, at t = L, equals its first, so the key before it stands before t = 0 and the key after
 * t = 0 stands after t = L) and a play-once clip clamps to its own end points, exactly as GeckoLib
 * does. Only the easing arguments change - values, lengths, easing types, the loop type, the length,
 * the name and the sound / particle / instruction keyframes are the loaded ones, the neighbours'
 * {@code MathValue} objects are shared rather than copied (a Molang expression stays live, as in
 * GeckoLib's own arguments), and the evaluator is GeckoLib's. The pass is idempotent: it reads only
 * start / end values, which it never changes, so repairing a repaired clip yields the same arguments.
 * A stack of fewer than three keyframes (one segment) has no neighbours and is left alone.</p>
 */
public final class SplineRepair {
    private SplineRepair() {
    }

    /** Every clip of a loaded animation file, repaired; a new instance, the loaded one untouched. */
    public static BakedAnimations repair(BakedAnimations loaded) {
        Map<String, Animation> repaired = new LinkedHashMap<>();
        loaded.animations().forEach((name, animation) -> repaired.put(name, repair(animation)));
        return new BakedAnimations(repaired);
    }

    /** One clip, repaired; a new record over new bone animations, the loaded one untouched. */
    public static Animation repair(Animation animation) {
        boolean loop = animation.loopType() == Animation.LoopType.LOOP;
        BoneAnimation[] source = animation.boneAnimations();
        BoneAnimation[] bones = new BoneAnimation[source.length];
        for (int index = 0; index < source.length; index++) {
            BoneAnimation bone = source[index];
            bones[index] = new BoneAnimation(bone.boneName(),
                    stack(bone.rotationKeyFrames(), loop),
                    stack(bone.positionKeyFrames(), loop),
                    stack(bone.scaleKeyFrames(), loop));
        }
        return new Animation(animation.name(), animation.length(), animation.loopType(), bones, animation.keyFrames());
    }

    private static KeyframeStack<Keyframe<MathValue>> stack(KeyframeStack<Keyframe<MathValue>> stack, boolean loop) {
        return new KeyframeStack<>(axis(stack.xKeyframes(), loop), axis(stack.yKeyframes(), loop),
                axis(stack.zKeyframes(), loop));
    }

    /** One axis's keyframe list with every repairable CATMULLROM frame rebuilt; other frames are the same objects. */
    static List<Keyframe<MathValue>> axis(List<Keyframe<MathValue>> frames, boolean loop) {
        int count = frames.size();
        List<Keyframe<MathValue>> out = new ArrayList<>(count);
        for (int index = 0; index < count; index++) {
            Keyframe<MathValue> frame = frames.get(index);
            MathValue[] arguments = repairedArguments(frames, index, loop);
            if (arguments == null) {
                out.add(frame);
                continue;
            }
            out.add(new Keyframe<>(frame.length(), frame.startValue(), frame.endValue(), frame.easingType(),
                    List.of(arguments[0], arguments[1])));
        }
        return out;
    }

    /**
     * The textbook {@code [P0, P3]} for the segment at {@code index} of one axis's keyframe list, or
     * {@code null} when the repair leaves that frame alone (not CATMULLROM, the anchor at index 0, or
     * a list of fewer than three frames). The pins and the harness read this beside the loaded values.
     */
    public static MathValue[] repairedArguments(List<Keyframe<MathValue>> frames, int index, boolean loop) {
        int count = frames.size();
        Keyframe<MathValue> frame = frames.get(index);
        if (index == 0 || count < 3 || frame.easingType() != EasingType.CATMULLROM) {
            return null;
        }
        MathValue before;
        if (index >= 2) {
            before = frames.get(index - 1).startValue();
        } else {
            before = loop ? frames.get(count - 1).startValue() : frame.startValue();
        }
        MathValue after;
        if (index + 1 < count) {
            after = frames.get(index + 1).endValue();
        } else {
            after = loop ? frames.get(1).endValue() : frame.endValue();
        }
        return new MathValue[] {before, after};
    }
}
