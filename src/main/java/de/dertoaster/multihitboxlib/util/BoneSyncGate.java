package de.dertoaster.multihitboxlib.util;

import java.util.function.BooleanSupplier;

import de.dertoaster.multihitboxlib.api.IMultipartEntity;

/**
 * ENT-S-174: the whole send decision of the bone stream's client side ({@code IMultipartEntity#updateSynching},
 * client branch, a switch over {@link #decide} and {@link #shouldSend}), pure so the server-side gametests drive the very
 * functions the client calls, the way {@link RenderTickGate} pins the collection gate.
 *
 *
 * <p>{@link #mayBuild}: the client whose player the server elected master builds and sends as before; any other client
 * sends only its first packet after a client-visible master change (the last-sent cache is null: every
 * {@code SPacketSetMaster} it receives nulls it) and then nothing. {@code CPacketHandlerBoneInformation} drops every
 * packet whose sender is not the server's current master, yet before this gate every other client tracking a bone-synced
 * creature re-sent it an empty packet every 8 ticks. The one packet after a master change is kept on purpose: the server
 * does not reset its silence counter when it elects ({@code ticksSinceLastSynch}), so after a master's silence it elects
 * afresh every tick, broadcasting each choice, until a packet arrives from the player it names at that moment; a
 * client's view of the master trails the server's by the latency, so the packet that settles a rotation can come from a
 * client that no longer believes itself master. Every broadcast nulls the cache, so during a rotation every tracking
 * client still answers every other tick, exactly as before; in the steady state, with no broadcast arriving, the other
 * clients fall silent.</p>
 *
 * <p>{@link #intervalAllowsSend}: a profile's {@code bone-sync-interval} spaces the master's sends; on a held tick the
 * builder is dropped and the next tick's collection replaces it, so every packet that goes out carries the newest pose.
 * The first payload after a master change goes out at once, as OPT-003 has it. The keepalive tick is always eligible,
 * because the 8-tick keepalive is at least every interval the codec admits (1..8, and clamped here besides), so the
 * server's 10-tick master timeout still never fires from protocol silence.</p>
 *
 * <p>{@link #shouldSend}: OPT-003's rule for a built packet, unchanged - the first after a master change, the keepalive,
 * or a payload that differs from the last one sent.</p>
 */
public final class BoneSyncGate {

	/** What the client branch of {@code updateSynching} does with the entity this tick. */
	public enum Step {
		/** Not the master and nothing owed: drop any builder, send nothing. */
		SILENT,
		/** No builder: start one (the collector fills it; an entity not drawn this tick leaves it empty). */
		START,
		/** A builder, inside the profile's interval: drop it; the next tick's collection replaces it. */
		HOLD,
		/** A builder, the interval run out: build the packet and let {@link #shouldSend} decide. */
		BUILD
	}

	private BoneSyncGate() {
	}

	/**
	 * @param localPlayerIsMaster    this client's player is the master the server last named for the entity
	 * @param firstAfterMasterChange no packet has gone out for the entity since this client last heard of an election (or
	 *                               since it began tracking it): the last-sent cache is null
	 * @param builderPresent         a bone packet builder is pending for the entity
	 * @param ticksSinceLastSend     ticks since the last send, this tick included, clamped at the keepalive
	 * @param interval               the profile's bone-sync-interval
	 */
	public static Step decide(final boolean localPlayerIsMaster, final boolean firstAfterMasterChange, final boolean builderPresent,
			final int ticksSinceLastSend, final int interval) {
		if (!mayBuild(localPlayerIsMaster, firstAfterMasterChange)) {
			return Step.SILENT;
		}
		if (!builderPresent) {
			return Step.START;
		}
		if (!intervalAllowsSend(firstAfterMasterChange, ticksSinceLastSend, interval)) {
			return Step.HOLD;
		}
		return Step.BUILD;
	}

	/** Whether this client builds (and may send) a bone packet for the entity this tick; see the class comment. */
	public static boolean mayBuild(final boolean localPlayerIsMaster, final boolean firstAfterMasterChange) {
		return localPlayerIsMaster || firstAfterMasterChange;
	}

	/** Whether this tick may send under the profile's interval; OPT-003's rule then decides whether it does. */
	public static boolean intervalAllowsSend(final boolean firstAfterMasterChange, final int ticksSinceLastSend, final int interval) {
		return firstAfterMasterChange || ticksSinceLastSend >= Math.max(1, Math.min(interval, IMultipartEntity.BONE_INFORMATION_KEEPALIVE_TICKS));
	}

	/**
	 * OPT-003's rule for a built packet: sent when it is the first after a master change, when the keepalive is due, or
	 * when {@code unchangedSinceLastSend} (asked only when the first two do not already decide) says it differs.
	 */
	public static boolean shouldSend(final boolean firstAfterMasterChange, final int ticksSinceLastSend, final BooleanSupplier unchangedSinceLastSend) {
		return firstAfterMasterChange || ticksSinceLastSend >= IMultipartEntity.BONE_INFORMATION_KEEPALIVE_TICKS
				|| !unchangedSinceLastSend.getAsBoolean();
	}
}
