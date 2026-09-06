package de.dertoaster.multihitboxlib.util;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.LongSupplier;

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
 *
 * <p>OPT-029 (2026-09-05): two OreSpawn names registered by
 * {@code danger.orespawn.entity.client.GeoReplacementCaches} through the {@link #counter} and
 * {@link #gauge} factories, so the same dump line measures the GeckoLib cache eviction of the
 * replaced renderers. {@code orespawn.geo.evictions} is a counter: the AnimatableManagers evicted in
 * the interval (one per replaced-renderer entity that left the client level, plus every manager
 * dropped by a client level unload); its increments are guarded by {@link #ENABLED} like every other
 * counter. {@code orespawn.geo.managers_held} is a gauge, appended AFTER the counters and never reset:
 * the managers currently held by every registered replacement cache. Expected reading: with N
 * replaced-renderer mobs in view {@code managers_held} equals the number that have been drawn and are
 * still in the client level, and it falls as they leave; before this slice it only ever rose (one
 * manager per entity id ever drawn, kept for the session). Gauges follow the counters in registration
 * order; the nine names above keep their names and their order.</p>
 *
 * <p>Phase G slice (d) (2026-09-06, the spawn-100 benchmark baseline, morehitboxes_evaluation.md
 * Section 5 "Item 13 baseline fold"): MHLib's own cost joins the dump. Four more CLIENT counters
 * follow the nine, in declaration order and therefore ahead of every name registered through
 * {@link #counter} (a static field of this class is constructed before any other class can call the
 * factory): {@code client.collector_ns} / {@code client.collector_alloc_bytes} (the span from
 * MHLib's Pre render hook to its Post render hook per multipart entity drawn -- GeckoLib's render of
 * the entity WITH the collector's per-bone work inside it; the headless
 * {@code QueenPartPlacementProbe --bench} isolates the collector), {@code net.c2s_bone_packets} /
 * {@code net.c2s_bone_bytes} (the master client's bone packets and their encoded payload length).
 * The dump-order pin ({@code GeoCacheEvictionTests.assertDumpOrder}) still holds: the nine names keep
 * the head of the dump, {@code orespawn.geo.evictions} follows the nine (index >= 9) and the gauge
 * follows every counter.</p>
 *
 * <p>The SERVER side gets its own list, dumped by {@code MHLibMod.onServerTick} every
 * {@link #DUMP_INTERVAL_TICKS} server ticks as
 * {@code MHLib counters (server, per 100 ticks): server_tick=N net.s2c_update_packets=... ...} and
 * reset by that handler alone -- an integrated server shares this JVM with its client, and one list
 * per side is what keeps the two dump handlers from resetting each other's values.
 * {@code net.s2c_update_packets} / {@code net.s2c_update_bytes} (both broadcast sites of
 * {@code MixinServerEntity.mixinSendDirtyEntityData}; one count per broadcast call, bytes = the
 * encoded payload), {@code net.set_master_packets} ({@code IMultipartEntity.setMasterUUID}),
 * {@code server.align_sub_parts_parts} / {@code server.align_synched_parts} (parts placed by the
 * two alignment loops), {@code server.part_setpos} ({@code MHLibPartEntity.setPos} on the server:
 * the alignment's call AND {@code updateLastPos}'s call from every part tick), and
 * {@code server.placement_ns} ({@code mhlibAiStep}'s server path plus
 * {@code ModernSpiderGait.feedParts}). Server sites guard with the same {@link #ENABLED} constant as the
 * client sites ({@code if (MHLibCounters.ENABLED) ...}, folded by the JIT when the property is unset, so
 * the disabled path costs nothing on either side -- slice (d) had OR'd a package-private test seam into the
 * server guard, one extra static boolean read per site; item 17 of the owner's 2026-09-06 rulings removed it:
 * no test seam in production code). The game-test server pins the increments because its run sets the
 * property ({@code build.gradle}, the {@code gameTestServer} run: {@code systemProperty 'mhlib.counters', 'true'}).</p>
 *
 * <p>Both dump handlers hand the values they just read to every {@link DumpListener} registered
 * through {@link #addDumpListener} (the in-game benchmark harness sums the intervals of a run this
 * way instead of resetting the counters underneath the dump).</p>
 */
public final class MHLibCounters {

	public static final String PROPERTY = "mhlib.counters";
	public static final boolean ENABLED = Boolean.getBoolean(PROPERTY);
	public static final int DUMP_INTERVAL_TICKS = 100;

	public static final String CLIENT_SIDE = "client";
	public static final String SERVER_SIDE = "server";

	private static final List<Counter> ALL = new CopyOnWriteArrayList<>();
	/** OPT-029: gauges in registration order; read at each dump, never reset. */
	private static final List<Gauge> GAUGES = new CopyOnWriteArrayList<>();
	/** Slice (d): the server-side counters, dumped and reset by the server tick handler alone. */
	private static final List<Counter> SERVER_ALL = new CopyOnWriteArrayList<>();
	private static final List<DumpListener> DUMP_LISTENERS = new CopyOnWriteArrayList<>();

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

	// ---- slice (d): the four client names after the nine (indices 9-12 of the client dump) ----

	/**
	 * Nanoseconds from MHLib's Pre render listener to its Post render listener, summed over every multipart
	 * entity drawn in the interval ({@link MHLibCollectorProbe}). Not the whole render: in GeckoLib 4.8.4's
	 * {@code GeoRenderer.defaultRender} the Pre event fires after {@code preRender} (offset 129) and the Post
	 * before {@code popPose} (235), {@code renderFinal} (260, the name tag and leash) and
	 * {@code doPostRenderCleanup} (266), so the span holds {@code actuallyRender} with the collector's per-bone
	 * work inside it, MHLib's own handler bodies, and any other mod's listeners registered between
	 * (refuter A, 2026-09-06). The collector alone is isolated by the headless companion.
	 */
	public static final Counter CLIENT_COLLECTOR_NS = new Counter("client.collector_ns");
	/** Bytes the render thread allocated over the same spans ({@code ThreadMXBean.getCurrentThreadAllocatedBytes}). */
	public static final Counter CLIENT_COLLECTOR_ALLOC_BYTES = new Counter("client.collector_alloc_bytes");
	/** {@code CPacketBoneInformation.send} calls (the master client's bone packets). */
	public static final Counter NET_C2S_BONE_PACKETS = new Counter("net.c2s_bone_packets");
	/** The encoded payload length of those packets ({@code CPacketBoneInformation.encodedLength}). */
	public static final Counter NET_C2S_BONE_BYTES = new Counter("net.c2s_bone_bytes");

	// ---- slice (d): the server list, in this order ----

	/** {@code MixinServerEntity.mixinSendDirtyEntityData} broadcasts (one per broadcast call, either send site). */
	public static final Counter NET_S2C_UPDATE_PACKETS = serverCounter("net.s2c_update_packets");
	/** The encoded payload length of those broadcasts ({@code SPacketUpdateMultipart.encodedLength}). */
	public static final Counter NET_S2C_UPDATE_BYTES = serverCounter("net.s2c_update_bytes");
	/** {@code SPacketSetMaster} broadcasts from {@code IMultipartEntity.setMasterUUID} (elections). */
	public static final Counter NET_SET_MASTER_PACKETS = serverCounter("net.set_master_packets");
	/** Parts placed by the {@code IMultipartEntity.alignSubParts} loop. */
	public static final Counter SERVER_ALIGN_SUB_PARTS_PARTS = serverCounter("server.align_sub_parts_parts");
	/** Parts placed by the {@code IMultipartEntity.alignSynchedSubParts} loop. */
	public static final Counter SERVER_ALIGN_SYNCHED_PARTS = serverCounter("server.align_synched_parts");
	/** {@code MHLibPartEntity.setPos} calls on the server (the alignment's and {@code updateLastPos}'s). */
	public static final Counter SERVER_PART_SETPOS = serverCounter("server.part_setpos");
	/** Nanoseconds inside {@code IMultipartEntity.mhlibAiStep}'s server path and {@code ModernSpiderGait.feedParts}. */
	public static final Counter SERVER_PLACEMENT_NS = serverCounter("server.placement_ns");

	private MHLibCounters() {
	}

	/**
	 * OPT-029: a counter declared outside this class (the OreSpawn GeckoLib cache eviction), registered
	 * into the dump after the built-in ones, in call order. The constructor stays private.
	 */
	public static Counter counter(String name) {
		return new Counter(name, ALL);
	}

	/** Slice (d): a counter of the SERVER list (dumped and reset by the server tick handler alone). */
	public static Counter serverCounter(String name) {
		return new Counter(name, SERVER_ALL);
	}

	/**
	 * OPT-029: a gauge -- a value read at each dump and never reset -- appended to the dump after every
	 * counter, in registration order.
	 */
	public static void gauge(String name, LongSupplier supplier) {
		GAUGES.add(new Gauge(name, supplier));
	}

	/** Every client-side counter in declaration order. */
	public static List<Counter> all() {
		return Collections.unmodifiableList(ALL);
	}

	/** Every server-side counter in declaration order (slice (d)). */
	public static List<Counter> serverAll() {
		return Collections.unmodifiableList(SERVER_ALL);
	}

	/** Every gauge in registration order (OPT-029). */
	public static List<Gauge> gauges() {
		return Collections.unmodifiableList(GAUGES);
	}

	/** Reads and zeroes every client counter, in declaration order; then reads every gauge, in registration order, resetting nothing. */
	public static Map<String, Long> sumAndResetAll() {
		final Map<String, Long> out = new LinkedHashMap<>();
		for (Counter counter : ALL) {
			out.put(counter.name(), counter.sumThenReset());
		}
		for (Gauge gauge : GAUGES) {
			out.put(gauge.name(), gauge.value());
		}
		return out;
	}

	/** Slice (d): reads and zeroes every server counter, in declaration order (no gauges on this side). */
	public static Map<String, Long> sumAndResetServer() {
		final Map<String, Long> out = new LinkedHashMap<>();
		for (Counter counter : SERVER_ALL) {
			out.put(counter.name(), counter.sumThenReset());
		}
		return out;
	}

	/** The INFO line the client tick handler logs: {@code MHLib counters (per 100 ticks): client_tick=N a=1 b=2 ...}. */
	public static String formatDump(int clientTick, Map<String, Long> values) {
		final StringBuilder sb = new StringBuilder(256);
		sb.append("MHLib counters (per ").append(DUMP_INTERVAL_TICKS).append(" ticks): client_tick=").append(clientTick);
		appendValues(sb, values);
		return sb.toString();
	}

	/** Slice (d): the INFO line the server tick handler logs: {@code MHLib counters (server, per 100 ticks): server_tick=N ...}. */
	public static String formatServerDump(int serverTick, Map<String, Long> values) {
		final StringBuilder sb = new StringBuilder(256);
		sb.append("MHLib counters (server, per ").append(DUMP_INTERVAL_TICKS).append(" ticks): server_tick=").append(serverTick);
		appendValues(sb, values);
		return sb.toString();
	}

	private static void appendValues(StringBuilder sb, Map<String, Long> values) {
		for (Map.Entry<String, Long> entry : values.entrySet()) {
			sb.append(' ').append(entry.getKey()).append('=').append(entry.getValue());
		}
	}

	/** Slice (d): registers a listener the two dump handlers call with the values they just read and reset. */
	public static void addDumpListener(DumpListener listener) {
		DUMP_LISTENERS.add(listener);
	}

	public static void removeDumpListener(DumpListener listener) {
		DUMP_LISTENERS.remove(listener);
	}

	/** Slice (d): called by the dump handlers after logging; {@code side} is {@link #CLIENT_SIDE} or {@link #SERVER_SIDE}. */
	public static void publishDump(String side, int tick, Map<String, Long> values) {
		for (DumpListener listener : DUMP_LISTENERS) {
			listener.onDump(side, tick, values);
		}
	}

	/** Slice (d): receives every dump of a side -- the values are the interval's, already reset in the counters. */
	@FunctionalInterface
	public interface DumpListener {
		void onDump(String side, int tick, Map<String, Long> values);
	}

	public static final class Counter {
		private final String name;
		private final LongAdder adder = new LongAdder();

		private Counter(String name) {
			this(name, ALL);
		}

		private Counter(String name, List<Counter> registry) {
			this.name = name;
			registry.add(this);
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

	/** OPT-029: a named value read at each dump; registered through {@link MHLibCounters#gauge}. */
	public static final class Gauge {
		private final String name;
		private final LongSupplier supplier;

		private Gauge(String name, LongSupplier supplier) {
			this.name = name;
			this.supplier = supplier;
		}

		public String name() {
			return this.name;
		}

		/** The current value; nothing is reset. */
		public long value() {
			return this.supplier.getAsLong();
		}
	}
}
