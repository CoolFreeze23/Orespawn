package danger.orespawn.entity.ai;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

/**
 * OPT-016 / OPT-021: drop-in replacements for the pervasive
 * "collect &rarr; {@code List.sort(comparator)} &rarr; take the first (matching)
 * entry" targeting idiom, avoiding the full O(n log n) sort when only one
 * element is ultimately consumed.
 *
 * <h2>Equivalence contract (what makes the swap behavior-neutral)</h2>
 * <ul>
 *   <li>{@link #first} is exactly {@code list.sort(order); return list.get(0)}:
 *       a single-pass minimum using a strict {@code compare(...) < 0} test, so
 *       equal-weight ties keep the <em>first-encountered</em> element — the
 *       same element a stable {@link List#sort} would have placed first
 *       (getEntities* list order is the encounter order the sort started from).</li>
 *   <li>{@link #firstMatch} is exactly {@code list.sort(order)} followed by a
 *       loop returning the first element the predicate accepts. Candidates are
 *       produced lazily in ascending {@code (order, original index)} order via
 *       an index heap; the index tiebreak reproduces the stable sort's tie
 *       order bit-for-bit, and the predicate is invoked on exactly the same
 *       elements, in exactly the same order, as the sorted loop would have —
 *       so side-effecting or expensive predicates (line-of-sight rays,
 *       {@code headFound} flags) observe an identical call sequence.</li>
 * </ul>
 *
 * <p>Cost: {@code first} is O(n) compares. {@code firstMatch} is O(n) heapify
 * plus O(log n) per rejected candidate — never worse than the sort it
 * replaces, even when every candidate is rejected.</p>
 *
 * <p>Comparators are evaluated on live entities within a single method call
 * (no world ticks in between), so weights are as stable here as they were
 * during the removed sort.</p>
 */
public final class TargetSelection {

    private TargetSelection() {
    }

    /**
     * Order-equivalent to {@code list.sort(order); return list.isEmpty() ? null : list.get(0);}
     * (first-encountered element wins comparator ties, matching stable-sort order).
     */
    public static <T> T first(List<T> candidates, Comparator<? super T> order) {
        int n = candidates.size();
        if (n == 0) return null;
        T best = candidates.get(0);
        for (int i = 1; i < n; i++) {
            T candidate = candidates.get(i);
            if (order.compare(candidate, best) < 0) {
                best = candidate;
            }
        }
        return best;
    }

    /**
     * Order-equivalent to sorting {@code candidates} with {@code order} (stable)
     * and returning the first element accepted by {@code filter}, or null.
     * The predicate sees the same elements in the same order as the sorted loop.
     */
    public static <T> T firstMatch(List<T> candidates, Comparator<? super T> order, Predicate<? super T> filter) {
        int n = candidates.size();
        if (n == 0) return null;
        int[] heap = new int[n];
        for (int i = 0; i < n; i++) {
            heap[i] = i;
        }
        for (int i = (n >>> 1) - 1; i >= 0; i--) {
            siftDown(heap, i, n, candidates, order);
        }
        int size = n;
        while (size > 0) {
            T candidate = candidates.get(heap[0]);
            if (filter.test(candidate)) return candidate;
            size--;
            heap[0] = heap[size];
            siftDown(heap, 0, size, candidates, order);
        }
        return null;
    }

    private static <T> void siftDown(int[] heap, int hole, int size, List<T> candidates, Comparator<? super T> order) {
        int node = heap[hole];
        while (true) {
            int child = (hole << 1) + 1;
            if (child >= size) break;
            int right = child + 1;
            if (right < size && less(heap[right], heap[child], candidates, order)) {
                child = right;
            }
            if (!less(heap[child], node, candidates, order)) break;
            heap[hole] = heap[child];
            hole = child;
        }
        heap[hole] = node;
    }

    /** Total order (comparator, original index): the index tiebreak mirrors stable-sort tie order. */
    private static <T> boolean less(int a, int b, List<T> candidates, Comparator<? super T> order) {
        int cmp = order.compare(candidates.get(a), candidates.get(b));
        return cmp < 0 || (cmp == 0 && a < b);
    }
}
