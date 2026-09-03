package de.dertoaster.multihitboxlib.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.LongAdder;

/**
 * OPT-028 / BUG-044 (2026-09-04): instrumentation counters for the client bone-collection path,
 * compiled in and ACTIVE ONLY under {@code -Dmhlib.counters=true}. {@link #ENABLED} is a
 * {@code static final} read once at class init; every call site is written
 * {@code if (MHLibCounters.ENABLED) MHLibCounters.X.increment();} so the JIT folds the guarded
 * increment away when the property is unset -- zero cost in normal play. The client tick handler
 * ({@code MHLibClient.onClientTick}) dumps and resets every counter every
 * {@link #DUMP_INTERVAL_TICKS} client ticks as one INFO line:
 * {@code MHLib counters (per 100 ticks): client_tick=N client.frames=... client.recursive_start=... ...}.
 *
 * <p>Expected reading with one Queen in view (phase_g_reports/morehitboxes_evaluation.md, Section 5):
 * {@code client.recursive_start / client.frames} = 220 with the bare-name {@code renderRecursively}
 * selectors (the typed method AND GeckoLib's synthetic bridge hooked, OPT-028) and 110 with the
 * descriptor-exact selectors; {@code client.bones_visited / client.frames} = 110 either way.</p>
 */
public final class MHLibCounters {

	public static final String PROPERTY = "mhlib.counters";
	public static final boolean ENABLED = Boolean.getBoolean(PROPERTY);
	public static final int DUMP_INTERVAL_TICKS = 100;

	private static final List<Counter> ALL = new ArrayList<>();

	/** Collector pre-render passes: one per multipart entity rendered per frame (= rendered frames with one Queen in view). */
	public static final Counter CLIENT_FRAMES = new Counter("client.frames");
	/** Passes the render-tick gate let collect (BUG-044): about one per game tick per rendered multipart entity. */
	public static final Counter CLIENT_COLLECTING_PASSES = new Counter("client.collecting_passes");
	/** {@code IMHLibExtendedRenderLayer.onRenderRecursivelyStart} invocations (the renderRecursively HEAD hook). */
	public static final Counter CLIENT_RECURSIVE_START = new Counter("client.recursive_start");
	/** {@code IMHLibExtendedRenderLayer.onRenderRecursivelyEnd} invocations (the renderRecursively TAIL hook). */
	public static final Counter CLIENT_RECURSIVE_END = new Counter("client.recursive_end");
	/** {@code IBoneInformationCollectorLayerCommonLogic.onRenderBone} entries: every bone of every GeckoLib entity carrying the layer. */
	public static final Counter CLIENT_BONES_VISITED = new Counter("client.bones_visited");
	/** {@code GeoBone.getWorldPosition()} reads made by {@code GeckolibBoneInformationCollectorLayer.getBoneWorldPosition} (three per call). */
	public static final Counter CLIENT_WORLD_POS_READS = new Counter("client.world_pos_reads");
	/** {@code GeckolibBoneInformationCollectorLayer.foldBodyYaw} calls. */
	public static final Counter CLIENT_FOLDS = new Counter("client.folds");
	/** {@code IMultipartEntity.tryAddBoneInformation} successes: bones the master client's packet builder took. */
	public static final Counter CLIENT_BONE_INFOS_BUILT = new Counter("client.bone_infos_built");
	/** {@code MHLibPartEntity.applyInformation} calls on the client (the trust-client apply). */
	public static final Counter CLIENT_APPLY_INFORMATION = new Counter("client.apply_information");

	private MHLibCounters() {
	}

	/** Every counter in declaration order. */
	public static List<Counter> all() {
		return Collections.unmodifiableList(ALL);
	}

	/** Reads and zeroes every counter, in declaration order. */
	public static Map<String, Long> sumAndResetAll() {
		final Map<String, Long> out = new LinkedHashMap<>();
		for (Counter counter : ALL) {
			out.put(counter.name(), counter.sumThenReset());
		}
		return out;
	}

	/** The INFO line the client tick handler logs: {@code MHLib counters (per 100 ticks): client_tick=N a=1 b=2 ...}. */
	public static String formatDump(int clientTick, Map<String, Long> values) {
		final StringBuilder sb = new StringBuilder(256);
		sb.append("MHLib counters (per ").append(DUMP_INTERVAL_TICKS).append(" ticks): client_tick=").append(clientTick);
		for (Map.Entry<String, Long> entry : values.entrySet()) {
			sb.append(' ').append(entry.getKey()).append('=').append(entry.getValue());
		}
		return sb.toString();
	}

	public static final class Counter {
		private final String name;
		private final LongAdder adder = new LongAdder();

		private Counter(String name) {
			this.name = name;
			ALL.add(this);
		}

		public String name() {
			return this.name;
		}

		/** Call sites guard with {@link MHLibCounters#ENABLED} so the disabled path costs nothing. */
		public void increment() {
			this.adder.increment();
		}

		public void add(long delta) {
			this.adder.add(delta);
		}

		public long sum() {
			return this.adder.sum();
		}

		public long sumThenReset() {
			return this.adder.sumThenReset();
		}
	}
}
