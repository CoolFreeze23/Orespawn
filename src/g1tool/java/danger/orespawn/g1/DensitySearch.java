package danger.orespawn.g1;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.ToDoubleFunction;

/**
 * The keyframe density search of the exact transcriptions (the keyframe reference leg, {@link KeyframeLeg} - the harness
 * of owner 2026-09-06, Q9 / Q10: "the fewest keys per bone holding the tolerance under the evaluator in force"), as ONE
 * loop both the leg and the reference-clip sampler run (owner 2026-09-14, second set revised, item 32 (6): a reference
 * clip's keys are chosen by the density search the exact transcriptions use - reused, not a new one).
 *
 * <p>The rule: for {@code keys = minKeys .. maxKeys}, the caller evaluates the candidate at that key count - a UNIFORM
 * key schedule with both ends included, the truth sampled exactly at the keys, the keyed curve under the evaluator in
 * force measured against the dense truth - and reports the maximum error per independent CHANNEL (a frequency group in
 * the leg: disjoint bones; a bone's rotation or position channel in the sampler). A channel takes the FIRST key count
 * whose error holds its tolerance ({@code fewest}); the loop stops as soon as every channel has one. The count just
 * below a channel's fewest is, by construction, the one that failed - the leg confirms that on its full schedule, the
 * sampler records the table row. The table keeps every candidate's errors in evaluation order, so the leg's JSON is the
 * same as before the extraction.</p>
 */
final class DensitySearch {
    private DensitySearch() {
    }

    /** One candidate's maximum error per channel at {@code keys} keys; {@code pending} names the channels still searching. */
    @FunctionalInterface
    interface Candidate {
        Map<String, Double> maxErrors(int keys, Set<String> pending);
    }

    /**
     * The search's result: the fewest key count per channel that reached its tolerance (a channel absent here never
     * reached it within {@code maxKeys}), the table of every evaluated key count's errors per channel (evaluation order),
     * and the last key count evaluated.
     */
    record Result(Map<String, Integer> fewest, Map<Integer, Map<String, Double>> table, int lastKeys) {
        boolean everyChannelReached(List<String> channels) {
            return this.fewest.keySet().containsAll(channels);
        }

        /** The error the count below a channel's fewest measured (the confirmation), or NaN when the fewest is the minimum. */
        double oneFewerError(String channel) {
            Integer keys = this.fewest.get(channel);
            if (keys == null) {
                return Double.NaN;
            }
            Map<String, Double> row = this.table.get(keys - 1);
            return row == null || !row.containsKey(channel) ? Double.NaN : row.get(channel);
        }
    }

    static Result search(int minKeys, int maxKeys, List<String> channels, ToDoubleFunction<String> tolerance, Candidate candidate) {
        if (minKeys < 1 || maxKeys < minKeys) {
            throw new IllegalArgumentException("density search: min_keys " + minKeys + " and max_keys " + maxKeys);
        }
        Map<String, Integer> fewest = new LinkedHashMap<>();
        Map<Integer, Map<String, Double>> table = new LinkedHashMap<>();
        Set<String> pending = new LinkedHashSet<>(channels);
        int last = minKeys;
        for (int keys = minKeys; keys <= maxKeys; keys++) {
            last = keys;
            Map<String, Double> errors = candidate.maxErrors(keys, pending);
            Map<String, Double> row = new LinkedHashMap<>();
            for (String channel : channels) {
                Double error = errors.get(channel);
                if (error == null) {
                    continue;
                }
                row.put(channel, error);
                if (!fewest.containsKey(channel) && error <= tolerance.applyAsDouble(channel)) {
                    fewest.put(channel, keys);
                    pending.remove(channel);
                }
            }
            table.put(keys, row);
            if (fewest.size() == channels.size()) {
                break;
            }
        }
        return new Result(fewest, table, last);
    }
}
