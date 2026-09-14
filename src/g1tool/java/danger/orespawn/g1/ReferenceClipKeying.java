package danger.orespawn.g1;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import danger.orespawn.entity.client.animation.SplineRepair;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import software.bernie.geckolib.animation.EasingType;
import software.bernie.geckolib.animation.keyframe.AnimationPoint;
import software.bernie.geckolib.animation.keyframe.Keyframe;
import software.bernie.geckolib.loading.math.MathValue;
import software.bernie.geckolib.loading.math.value.Constant;

/**
 * The keying of a reference clip (owner 2026-09-14, second set revised, item 32 (6)): the dense per-tick samples of a
 * state are reduced to the FEWEST keys per bone and channel whose catmullrom curve stays within the tolerance of the
 * samples - 1 degree of rotation, 1/32 block (0.5 model units) of position - by the density search the exact
 * transcriptions use ({@link DensitySearch}, the loop of {@link KeyframeLeg}'s {@code density_search}), the closing key
 * ALWAYS kept, the keys written with {@code lerp_mode} catmullrom.
 *
 * <p>THE ONE ADAPTATION of the leg's search: the leg samples its cosine at any time, so its candidate keys sit at
 * {@code L k / (N - 1)}; a hook is posed once per tick with the entity RNG and the RenderInfo latches evolving per call,
 * so an intermediate time cannot be posed without changing the call sequence - the candidate keys here are the samples
 * themselves, uniform in SAMPLE INDEX ({@code round(i (N - 1) / (n - 1))}), both ends included (the first sample and the
 * closing key), the value at a key exactly the sample's. Every other part of the rule is the leg's: the fewest count per
 * independent channel (a bone's rotation, a bone's position: a key holds all three axes), the first count whose maximum
 * error over the dense samples holds the tolerance, the count below it recorded as the one that failed.</p>
 *
 * <p>THE EVALUATOR IN FORCE is GeckoLib 4.8.4's CATMULLROM with the spline arguments repaired at load
 * ({@link SplineRepair}: the textbook neighbours P0 / P3, a loop continuing across its seam) - the evaluator every
 * shipped keyframe clip of the replacement seam plays under. The search evaluates the same formula inline
 * ({@code EasingType.CatmullRomEasing.getPointOnSpline}: {@code 0.5 (2 P1 + (P2 - P0) t + (2 P0 - 5 P1 + 4 P2 - P3) t^2 +
 * (3 P1 - P0 - 3 P2 + P3) t^3)}, t the fraction of the segment, the neighbour rule of
 * {@link SplineRepair#repairedArguments} and GeckoLib's own P0 == P1 / P3 == P2 on a two-key channel) for speed, and the
 * FINAL keys of every channel are re-measured through GeckoLib's own objects - real {@link Keyframe}s built as
 * {@code BakedAnimationsAdapter.buildKeyframeStack} builds them (the zero-length anchor, then one segment per key), the
 * arguments repaired by {@link SplineRepair#repairedArguments}, the value at a tick from
 * {@link EasingType#apply(AnimationPoint)} on the frame GeckoLib's keyframe lookup selects - and the two must agree to
 * {@link #CROSSCHECK_EPSILON}; both maxima are recorded in the index.</p>
 */
final class ReferenceClipKeying {
    static final double ROTATION_TOLERANCE_DEGREES = 1.0D;
    /** 1/32 block: a block is 16 model units. */
    static final double UNITS_PER_BLOCK = 16.0D;
    static final double POSITION_TOLERANCE_BLOCKS = 1.0D / 32.0D;
    static final double POSITION_TOLERANCE_UNITS = POSITION_TOLERANCE_BLOCKS * UNITS_PER_BLOCK;
    static final String LERP_MODE = "catmullrom";
    static final int MIN_KEYS = 2;
    static final double CROSSCHECK_EPSILON = 1.0e-9D;
    static final String CHANNEL_ROTATION = "rotation";
    static final String CHANNEL_POSITION = "position";
    static final String SEARCH_RULE = "the density search of the exact transcriptions (KeyframeLeg's density_search through the shared "
            + "DensitySearch): per bone and channel the fewest keys - candidates uniform in sample index, both ends included (the "
            + "closing key always kept), the value at a key exactly the sample's - whose catmullrom curve (GeckoLib 4.8.4's CATMULLROM "
            + "with the spline arguments repaired at load, SplineRepair) stays within the tolerance of every dense sample; the count "
            + "below the fewest is the one that failed; a one-key state stays one key";

    private ReferenceClipKeying() {
    }

    /** One keyed channel: the sample indices kept as keys and the search's numbers. */
    record KeyedChannel(String bone, String channel, int[] indices, double maxError, double oneFewerError, double geckolibMaxError) {
        int keys() {
            return this.indices.length;
        }
    }

    /** A state's keyed clip: every bone's rotation channel, the positioned bones' position channels, the totals. */
    record Keyed(List<Double> ticks, Map<String, KeyedChannel> rotation, Map<String, KeyedChannel> position, int samples,
                 int keysMin, int keysMax, long keysTotal, double maxRotationError, double maxPositionError,
                 double geckolibMaxRotationError, double geckolibMaxPositionError) {
        /** The clip's {@code bones} object: keys at the kept samples, {@code lerp_mode} catmullrom. */
        JsonObject bones(Map<String, List<double[]>> rotationSamples, Map<String, List<double[]>> positionSamples) {
            JsonObject bones = new JsonObject();
            for (Map.Entry<String, KeyedChannel> keyed : this.rotation.entrySet()) {
                JsonObject bone = new JsonObject();
                bone.add(CHANNEL_ROTATION, keys(this.ticks, rotationSamples.get(keyed.getKey()), keyed.getValue().indices()));
                KeyedChannel positioned = this.position.get(keyed.getKey());
                if (positioned != null) {
                    bone.add(CHANNEL_POSITION, keys(this.ticks, positionSamples.get(keyed.getKey()), positioned.indices()));
                }
                bones.add(keyed.getKey(), bone);
            }
            return bones;
        }

        JsonObject json() {
            JsonObject out = new JsonObject();
            out.addProperty("search", SEARCH_RULE);
            out.addProperty("lerp_mode", LERP_MODE);
            out.addProperty("rotation_tolerance_degrees", ROTATION_TOLERANCE_DEGREES);
            out.addProperty("position_tolerance_blocks", POSITION_TOLERANCE_BLOCKS);
            out.addProperty("position_tolerance_units", POSITION_TOLERANCE_UNITS);
            out.addProperty("dense_samples", this.samples);
            out.addProperty("keys_per_bone_min", this.keysMin);
            out.addProperty("keys_per_bone_max", this.keysMax);
            out.addProperty("keys_total", this.keysTotal);
            out.addProperty("max_error_degrees", ReferenceClipSampler.round(this.maxRotationError));
            out.addProperty("max_error_units", ReferenceClipSampler.round(this.maxPositionError));
            out.addProperty("max_error_blocks", ReferenceClipSampler.round(this.maxPositionError / UNITS_PER_BLOCK));
            out.addProperty("max_error_degrees_geckolib", ReferenceClipSampler.round(this.geckolibMaxRotationError));
            out.addProperty("max_error_units_geckolib", ReferenceClipSampler.round(this.geckolibMaxPositionError));
            out.addProperty("within_tolerance", this.maxRotationError <= ROTATION_TOLERANCE_DEGREES
                    && this.maxPositionError <= POSITION_TOLERANCE_UNITS);
            // The confirmation the leg records per group, here as the clip's worst case: the largest error the count below a
            // channel's fewest measured (every such count failed its tolerance by construction of the search).
            double worstOneFewer = Double.NaN;
            int channelsAtMinimum = 0;
            for (Map<String, KeyedChannel> channels : List.of(this.rotation, this.position)) {
                for (KeyedChannel channel : channels.values()) {
                    if (Double.isNaN(channel.oneFewerError())) {
                        channelsAtMinimum++;
                    } else if (Double.isNaN(worstOneFewer) || channel.oneFewerError() > worstOneFewer) {
                        worstOneFewer = channel.oneFewerError();
                    }
                }
            }
            out.addProperty("one_fewer_max_error", Double.isNaN(worstOneFewer) ? null : ReferenceClipSampler.round(worstOneFewer));
            out.addProperty("channels_at_minimum_keys", channelsAtMinimum);
            out.addProperty("channels", this.rotation.size() + this.position.size());
            return out;
        }
    }

    /**
     * The keying of one state: {@code ticks} the sample times (every whole tick then the closing key; one entry for a
     * one-key state), {@code rotation} every bone's authored rotation per sample, {@code position} the positioned bones'
     * authored position per sample (the channels the clip carries).
     */
    static Keyed key(List<Double> ticks, Map<String, List<double[]>> rotation, Map<String, List<double[]>> position) {
        int samples = ticks.size();
        double[] times = ticks.stream().mapToDouble(Double::doubleValue).toArray();
        List<String> channels = new ArrayList<>();
        Map<String, List<double[]>> byChannel = new LinkedHashMap<>();
        Map<String, Double> tolerances = new LinkedHashMap<>();
        for (Map.Entry<String, List<double[]>> bone : rotation.entrySet()) {
            String channel = bone.getKey() + "/" + CHANNEL_ROTATION;
            channels.add(channel);
            byChannel.put(channel, bone.getValue());
            tolerances.put(channel, ROTATION_TOLERANCE_DEGREES);
        }
        for (Map.Entry<String, List<double[]>> bone : position.entrySet()) {
            String channel = bone.getKey() + "/" + CHANNEL_POSITION;
            channels.add(channel);
            byChannel.put(channel, bone.getValue());
            tolerances.put(channel, POSITION_TOLERANCE_UNITS);
        }
        Map<String, int[]> indices = new LinkedHashMap<>();
        Map<String, Double> errors = new LinkedHashMap<>();
        Map<String, Double> oneFewer = new LinkedHashMap<>();
        if (samples <= 1) {
            for (String channel : channels) {
                indices.put(channel, new int[]{0});
                errors.put(channel, 0.0D);
                oneFewer.put(channel, Double.NaN);
            }
        } else {
            DensitySearch.Result searched = DensitySearch.search(Math.min(MIN_KEYS, samples), samples, channels, tolerances::get,
                    (keys, pending) -> candidate(keys, pending, times, byChannel));
            for (String channel : channels) {
                Integer fewest = searched.fewest().get(channel);
                if (fewest == null) {
                    throw new IllegalStateException(channel + ": the density search reached no key count within the tolerance, although every "
                            + "sample as a key has error 0 - the evaluator is broken");
                }
                indices.put(channel, uniformIndices(fewest, samples));
                errors.put(channel, searched.table().get(fewest).get(channel));
                oneFewer.put(channel, searched.oneFewerError(channel));
            }
        }
        Map<String, KeyedChannel> rotationKeyed = new TreeMap<>();
        Map<String, KeyedChannel> positionKeyed = new TreeMap<>();
        double maxRotation = 0.0D;
        double maxPosition = 0.0D;
        double geckoRotation = 0.0D;
        double geckoPosition = 0.0D;
        int keysMin = Integer.MAX_VALUE;
        int keysMax = 0;
        long keysTotal = 0L;
        for (String channel : channels) {
            int slash = channel.lastIndexOf('/');
            String bone = channel.substring(0, slash);
            String kind = channel.substring(slash + 1);
            int[] kept = indices.get(channel);
            double gecko = samples <= 1 ? 0.0D : geckolibMaxError(times, byChannel.get(channel), kept);
            double inline = errors.get(channel);
            if (Math.abs(gecko - inline) > CROSSCHECK_EPSILON) {
                throw new IllegalStateException(channel + ": the inline catmullrom evaluator (max error " + inline + ") and GeckoLib's own "
                        + "(" + gecko + ") disagree beyond " + CROSSCHECK_EPSILON);
            }
            KeyedChannel keyed = new KeyedChannel(bone, kind, kept, inline, oneFewer.get(channel), gecko);
            if (CHANNEL_ROTATION.equals(kind)) {
                rotationKeyed.put(bone, keyed);
                maxRotation = Math.max(maxRotation, inline);
                geckoRotation = Math.max(geckoRotation, gecko);
            } else {
                positionKeyed.put(bone, keyed);
                maxPosition = Math.max(maxPosition, inline);
                geckoPosition = Math.max(geckoPosition, gecko);
            }
            keysMin = Math.min(keysMin, kept.length);
            keysMax = Math.max(keysMax, kept.length);
            keysTotal += kept.length;
        }
        if (channels.isEmpty()) {
            keysMin = 0;
        }
        return new Keyed(ticks, rotationKeyed, positionKeyed, samples, keysMin, keysMax, keysTotal, maxRotation, maxPosition,
                geckoRotation, geckoPosition);
    }

    /** The candidate keys at {@code keys}: the sample indices uniform in index, both ends included. */
    static int[] uniformIndices(int keys, int samples) {
        int[] out = new int[keys];
        for (int i = 0; i < keys; i++) {
            out[i] = keys == 1 ? 0 : (int) Math.round((double) i * (samples - 1) / (keys - 1));
        }
        out[keys - 1] = samples - 1;  // the closing key, always
        return out;
    }

    /** One candidate over the channels still searching: the max error per channel of the curve through the uniform keys. */
    private static Map<String, Double> candidate(int keys, Set<String> pending, double[] times, Map<String, List<double[]>> byChannel) {
        int[] indices = uniformIndices(keys, times.length);
        Map<String, Double> out = new LinkedHashMap<>();
        for (String channel : pending) {
            out.put(channel, inlineMaxError(times, byChannel.get(channel), indices));
        }
        return out;
    }

    /** GeckoLib's catmullrom ({@code EasingType.CatmullRomEasing.getPointOnSpline}), inline. */
    static double spline(double t, double p0, double p1, double p2, double p3) {
        return 0.5D * (2.0D * p1 + (p2 - p0) * t + (2.0D * p0 - 5.0D * p1 + 4.0D * p2 - p3) * t * t
                + (3.0D * p1 - p0 - 3.0D * p2 + p3) * t * t * t);
    }

    /**
     * The maximum |curve - sample| over every sample and the three axes for keys at {@code indices}: the neighbour rule of
     * {@link SplineRepair#repairedArguments} on a loop (three keys or more), GeckoLib's own P0 == P1 / P3 == P2 on two;
     * at a key the curve is the key (the next segment's start, or the last segment's end), so only the samples strictly
     * inside a segment are measured.
     */
    static double inlineMaxError(double[] times, List<double[]> samples, int[] indices) {
        int keys = indices.length;
        double worst = 0.0D;
        for (int axis = 0; axis < 3; axis++) {
            for (int s = 0; s + 1 < keys; s++) {
                int from = indices[s];
                int to = indices[s + 1];
                if (to - from < 2) {
                    continue;
                }
                double p1 = samples.get(from)[axis];
                double p2 = samples.get(to)[axis];
                double p0;
                double p3;
                if (keys >= 3) {
                    p0 = s >= 1 ? samples.get(indices[s - 1])[axis] : samples.get(indices[keys - 2])[axis];
                    p3 = s + 2 < keys ? samples.get(indices[s + 2])[axis] : samples.get(indices[1])[axis];
                } else {
                    p0 = p1;
                    p3 = p2;
                }
                double length = times[to] - times[from];
                for (int j = from + 1; j < to; j++) {
                    double t = (times[j] - times[from]) / length;
                    worst = Math.max(worst, Math.abs(spline(t, p0, p1, p2, p3) - samples.get(j)[axis]));
                }
            }
        }
        return worst;
    }

    /**
     * The same maximum through GeckoLib's own objects: per axis the keyframe list as {@code BakedAnimationsAdapter} builds
     * it (the zero-length anchor, then one CATMULLROM segment per key with GeckoLib's own arguments), every repairable
     * frame rebuilt with {@link SplineRepair#repairedArguments} (a loop), the value at a sample from
     * {@link EasingType#apply(AnimationPoint)} on the frame GeckoLib's keyframe lookup selects.
     */
    static double geckolibMaxError(double[] times, List<double[]> samples, int[] indices) {
        int keys = indices.length;
        double worst = 0.0D;
        for (int axis = 0; axis < 3; axis++) {
            List<Keyframe<MathValue>> frames = new ArrayList<>();
            MathValue first = new Constant(samples.get(indices[0])[axis]);
            frames.add(new Keyframe<>(0.0D, first, first, EasingType.CATMULLROM, List.of(first, first)));
            for (int s = 0; s + 1 < keys; s++) {
                MathValue start = new Constant(samples.get(indices[s])[axis]);
                MathValue end = new Constant(samples.get(indices[s + 1])[axis]);
                MathValue after = new Constant(samples.get(indices[Math.min(s + 2, keys - 1)])[axis]);
                frames.add(new Keyframe<>(times[indices[s + 1]] - times[indices[s]], start, end, EasingType.CATMULLROM,
                        List.of(start, after)));  // GeckoLib's own arguments (the previous end, the next end)
            }
            List<Keyframe<MathValue>> repaired = new ArrayList<>(frames.size());
            for (int index = 0; index < frames.size(); index++) {
                Keyframe<MathValue> frame = frames.get(index);
                MathValue[] arguments = SplineRepair.repairedArguments(frames, index, true);
                repaired.add(arguments == null ? frame
                        : new Keyframe<>(frame.length(), frame.startValue(), frame.endValue(), frame.easingType(), List.of(arguments[0], arguments[1])));
            }
            for (int j = 0; j < samples.size(); j++) {
                double tick = times[j] - times[indices[0]];
                // GeckoLib's keyframe lookup (AnimationController.getCurrentKeyFrameLocation): the first frame whose cumulative
                // end lies past the tick, else the last frame.
                double total = 0.0D;
                Keyframe<MathValue> frame = repaired.get(repaired.size() - 1);
                double current = tick - (totalLength(repaired) - frame.length());
                for (Keyframe<MathValue> candidate : repaired) {
                    total += candidate.length();
                    if (total > tick) {
                        frame = candidate;
                        current = tick - (total - candidate.length());
                        break;
                    }
                }
                double value = frame.easingType().apply(new AnimationPoint(frame, current, frame.length(), frame.startValue().get(), frame.endValue().get()));
                worst = Math.max(worst, Math.abs(value - samples.get(j)[axis]));
            }
        }
        return worst;
    }

    private static double totalLength(List<Keyframe<MathValue>> frames) {
        double total = 0.0D;
        for (Keyframe<MathValue> frame : frames) {
            total += frame.length();
        }
        return total;
    }

    private static JsonObject keys(List<Double> ticks, List<double[]> values, int[] indices) {
        JsonObject out = new JsonObject();
        for (int index : indices) {
            JsonObject key = new JsonObject();
            JsonArray post = new JsonArray();
            for (int axis = 0; axis < 3; axis++) {
                post.add(values.get(index)[axis]);
            }
            key.add("post", post);
            key.addProperty("lerp_mode", LERP_MODE);
            out.add(ReferenceClipSampler.timeKey(ticks.get(index)), key);
        }
        return out;
    }
}
