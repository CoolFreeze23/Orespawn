package de.dertoaster.multihitboxlib.util;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

/**
 * Phase G slice (d) (2026-09-06): the per-entity render span behind {@code client.collector_ns} and
 * {@code client.collector_alloc_bytes} (morehitboxes_evaluation.md Section 5: "wrap
 * {@code GeoRenderEvent.Entity.Pre} to {@code Post} in {@code GeckolibEntityRenderEventHandler}
 * with {@code System.nanoTime()} and {@code ThreadMXBean.getCurrentThreadAllocatedBytes()}").
 *
 * <p>{@link #begin} is called by MHLib's Pre render hook after its multipart check and {@link #end}
 * at the end of its Post render hook, on both the GeoEntity and the replaced-entity path, keyed on
 * the entity being drawn. The span therefore covers GeckoLib's render of the entity WITH the
 * collector's per-bone work inside it (the HEAD/TAIL hooks, {@code onRenderBone}) plus MHLib's own
 * handler bodies; it does not isolate the collector from the draw -- the headless companion
 * ({@code QueenPartPlacementProbe --bench}) does that. One slot, no nesting: a Pre for another entity
 * before the first entity's Post replaces the slot, and the first Post then finds a different key and
 * accounts nothing (an undercount, never a foreign span). Every call site is guarded by
 * {@link MHLibCounters#ENABLED}; the class itself is common code with no Minecraft import, so the
 * game tests drive it directly.</p>
 *
 * <p>Allocated bytes come from {@code com.sun.management.ThreadMXBean.getCurrentThreadAllocatedBytes()}
 * (HotSpot; JDK 14+), the calling thread's own allocation counter. The {@code com.sun.management}
 * type is only touched inside {@link #detectAllocationSupport} and the guarded getter, so a runtime
 * without the {@code jdk.management} module loads this class and reports zero bytes.</p>
 */
public final class MHLibCollectorProbe {

	private static final ThreadMXBean THREADS = ManagementFactory.getThreadMXBean();
	private static final boolean ALLOCATION_SUPPORTED = detectAllocationSupport();

	private static Object openKey;
	private static long openNanos;
	private static long openAllocated;

	private MHLibCollectorProbe() {
	}

	private static boolean detectAllocationSupport() {
		try {
			return THREADS instanceof com.sun.management.ThreadMXBean sun
					&& sun.isThreadAllocatedMemorySupported() && sun.isThreadAllocatedMemoryEnabled();
		} catch (RuntimeException | LinkageError unavailable) {
			return false;
		}
	}

	public static boolean allocationSupported() {
		return ALLOCATION_SUPPORTED;
	}

	/** The calling thread's allocated bytes so far, or 0 when unsupported. */
	public static long currentThreadAllocatedBytes() {
		if (!ALLOCATION_SUPPORTED) {
			return 0L;
		}
		return ((com.sun.management.ThreadMXBean) THREADS).getCurrentThreadAllocatedBytes();
	}

	/** Every thread's allocated bytes so far (JDK 21 {@code getTotalThreadAllocatedBytes}), or -1 when unsupported. */
	public static long totalThreadAllocatedBytes() {
		if (!ALLOCATION_SUPPORTED) {
			return -1L;
		}
		try {
			return ((com.sun.management.ThreadMXBean) THREADS).getTotalThreadAllocatedBytes();
		} catch (RuntimeException | LinkageError unavailable) {
			return -1L;
		}
	}

	/** Opens the span for {@code key} (the entity being drawn); replaces any span left open. */
	public static void begin(Object key) {
		openKey = key;
		openNanos = System.nanoTime();
		openAllocated = currentThreadAllocatedBytes();
	}

	/**
	 * Closes the span for {@code key}: when it is the open one, its nanoseconds go to
	 * {@link MHLibCounters#CLIENT_COLLECTOR_NS} and its allocated bytes to
	 * {@link MHLibCounters#CLIENT_COLLECTOR_ALLOC_BYTES}; otherwise nothing is accounted. Returns
	 * whether the span was accounted.
	 */
	public static boolean end(Object key) {
		if (key == null || openKey != key) {
			return false;
		}
		final long nanos = System.nanoTime() - openNanos;
		final long allocated = currentThreadAllocatedBytes() - openAllocated;
		openKey = null;
		MHLibCounters.CLIENT_COLLECTOR_NS.add(Math.max(0L, nanos));
		MHLibCounters.CLIENT_COLLECTOR_ALLOC_BYTES.add(Math.max(0L, allocated));
		return true;
	}

	/** Whether a span is open for {@code key} (test observability). */
	public static boolean isOpen(Object key) {
		return key != null && openKey == key;
	}
}
