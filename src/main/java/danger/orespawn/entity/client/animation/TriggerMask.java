package danger.orespawn.entity.client.animation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import software.bernie.geckolib.animation.state.BoneSnapshot;
import software.bernie.geckolib.cache.object.GeoBone;

/**
 * A triggered clip REPLACING the locomotion layers on the bones it animates (contract section 5.3), per
 * manager. Updated by the {@link TriggeredClipController} right after its own {@code process} (it is registered
 * first, so it runs before every locomotion layer of the frame) with the set of bones it writes this frame; read
 * by the weighted locomotion layers ({@link PhaseLockedKeyframeController}) bone by bone:
 * <ul>
 *   <li>a HELD bone - one the triggered clip writes this frame (its blend-in transition or its keyframes) - is left
 *       entirely to the clip: the locomotion layers drop their points for it ({@link #factor} 0, {@link #held});
 *       GeckoLib's own TRANSITIONING lerp from the bone's current pose to the clip's first key over
 *       {@code transitionLength} ticks IS the blend-in, so no ramp is needed on the way in;</li>
 *   <li>a RELEASED bone - held last frame, not written this frame (the play-once clip finished, or a clip that keys
 *       fewer bones replaced it) - takes THE BONE-RESET BLEND-OUT for {@code getBoneResetTime()} ticks: GeckoLib 4.8.4
 *       resets a bone no controller wrote this frame by {@code Mth.lerp(min((animTime - lastResetTick) /
 *       boneResetTime, 1), lastWrittenValue, bind)} ({@code AnimationProcessor.tickAnimation} 590-904), but it runs
 *       that loop AFTER every controller, so a locomotion layer that wrote the bone would have marked it and skipped
 *       the reset (contract section 5.3's "re-added on top of the reset" needs the seam to carry the reset's value):
 *       the mask reproduces the reset's value - {@link #releaseDelta}, {@code (1 - p) x} the clip's last pose as a
 *       delta from bind, {@code p} the reset's own fraction - as the first locomotion layer's base on the bone, and
 *       {@link #factor} ramps the locomotion weight in as {@code p} over the same ticks, so the bone crosses from the
 *       clip's last pose to the LIVE locomotion pose over {@code boneResetTime}: the visible result section 5.3
 *       promises. A bone no locomotion layer keys takes GeckoLib's own reset, the same numbers.</li>
 * </ul>
 * The fraction {@code p} is a function of the frame's {@code ageInTicks} since the release (ENT-S-147: a repeated
 * frame recomputes it, a skipped frame is caught up), the same clock GeckoLib's reset reads for a replaced entity
 * ({@code GeoModel.handleAnimations}: {@code animTime} telescopes to {@code tick + partialTick}).
 */
public final class TriggerMask {
    /** Contract section 5.3's proposed {@code boneResetTime}: the blend-out's length in ticks, {@code OreSpawnGeoReplacement.getBoneResetTime()}. */
    public static final double BONE_RESET_TICKS = 3.0D;
    /** Nine deltas from bind: rotation x y z, position x y z, scale x y z. */
    public static final int CHANNELS = 9;

    /** A released bone: the age of the first frame the clip no longer wrote it and the clip's last pose as deltas from bind. */
    private record Release(double stopAge, float[] lastDelta) {
    }

    private final Set<String> held = new HashSet<>();
    private final Map<String, Release> releasing = new HashMap<>();
    private double frameAge;

    /** The frame's clock, from {@link ContractLayers#beginFrame} before any controller runs. */
    public void beginFrame(double ageInTicks) {
        this.frameAge = ageInTicks;
    }

    /**
     * From the triggered-clip controller after its {@code process}: the bones it writes THIS frame. A bone held and not
     * written now is released at this age with the MANAGER'S snapshot of it - this entity's own last write, never the shared bake's GeoBone, which holds whichever entity rendered last (the weights slice); a bone written again while releasing is held again.
     */
    public void update(Set<String> writing, Map<String, BoneSnapshot> snapshots, Map<String, GeoBone> bones) {
        for (String name : this.held) {
            if (!writing.contains(name)) {
                BoneSnapshot last = snapshots.get(name);
                GeoBone bone = bones.get(name);
                if (last != null && bone != null) {
                    this.releasing.put(name, new Release(this.frameAge, deltaFromBind(last, bone.getInitialSnapshot())));
                }
            }
        }
        this.held.clear();
        this.held.addAll(writing);
        for (String name : writing) {
            this.releasing.remove(name);
        }
        this.releasing.values().removeIf(release -> fraction(release) >= 1.0D);
    }

    /** True while the triggered clip writes the bone this frame: the locomotion layers leave it alone. */
    public boolean held(String bone) {
        return this.held.contains(bone);
    }

    /** True while the bone crosses back from the clip's last pose to the locomotion pose. */
    public boolean releasing(String bone) {
        return this.releasing.containsKey(bone);
    }

    /** The locomotion layers' factor on the bone this frame: 0 held, the reset's fraction while releasing, else 1. */
    public float factor(String bone) {
        if (this.held.contains(bone)) {
            return 0.0F;
        }
        Release release = this.releasing.get(bone);
        return release == null ? 1.0F : (float) fraction(release);
    }

    /**
     * The bone-reset's value this frame as nine deltas from bind, {@code (1 - p) x lastDelta} - what GeckoLib's reset
     * would write ({@code lerp(p, last, bind)} minus bind) - for the first locomotion layer on a releasing bone; a
     * zero vector for any other bone.
     */
    public float[] releaseDelta(String bone) {
        float[] delta = new float[CHANNELS];
        Release release = this.releasing.get(bone);
        if (release == null) {
            return delta;
        }
        float remaining = (float) (1.0D - fraction(release));
        for (int channel = 0; channel < CHANNELS; channel++) {
            delta[channel] = release.lastDelta()[channel] * remaining;
        }
        return delta;
    }

    /** The bones the triggered clip writes this frame. */
    public Set<String> heldBones() {
        return Set.copyOf(this.held);
    }

    /** The bones crossing back this frame. */
    public Set<String> releasingBones() {
        return Set.copyOf(this.releasing.keySet());
    }

    private double fraction(Release release) {
        return Math.min(Math.max(this.frameAge - release.stopAge(), 0.0D) / BONE_RESET_TICKS, 1.0D);
    }

    /** The bone's current transform as deltas from its bake (rotation and offset relative to the initial snapshot, scale relative to it too). */
    public static float[] deltaFromBind(GeoBone bone) {
        BoneSnapshot initial = bone.getInitialSnapshot();
        return new float[] {
                bone.getRotX() - initial.getRotX(), bone.getRotY() - initial.getRotY(), bone.getRotZ() - initial.getRotZ(),
                bone.getPosX() - initial.getOffsetX(), bone.getPosY() - initial.getOffsetY(), bone.getPosZ() - initial.getOffsetZ(),
                bone.getScaleX() - initial.getScaleX(), bone.getScaleY() - initial.getScaleY(), bone.getScaleZ() - initial.getScaleZ(),
        };
    }

    /** The manager's snapshot of a bone as deltas from its bake: this entity's own last write (the shared bake's GeoBone holds whichever entity rendered last). */
    public static float[] deltaFromBind(BoneSnapshot last, BoneSnapshot initial) {
        return new float[] {
                last.getRotX() - initial.getRotX(), last.getRotY() - initial.getRotY(), last.getRotZ() - initial.getRotZ(),
                last.getOffsetX() - initial.getOffsetX(), last.getOffsetY() - initial.getOffsetY(), last.getOffsetZ() - initial.getOffsetZ(),
                last.getScaleX() - initial.getScaleX(), last.getScaleY() - initial.getScaleY(), last.getScaleZ() - initial.getScaleZ(),
        };
    }
}
