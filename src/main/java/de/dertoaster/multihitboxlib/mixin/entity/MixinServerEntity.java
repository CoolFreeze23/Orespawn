package de.dertoaster.multihitboxlib.mixin.entity;

import java.util.List;
import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import de.dertoaster.multihitboxlib.api.IMultipartEntity;
import de.dertoaster.multihitboxlib.entity.MHLibPartEntity;
import de.dertoaster.multihitboxlib.entity.hitbox.HitboxProfile;
import de.dertoaster.multihitboxlib.network.server.SPacketUpdateMultipart;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.entity.PartEntity;
import net.neoforged.neoforge.network.PacketDistributor;

@Mixin(ServerEntity.class)
public abstract class MixinServerEntity {

	@Shadow
	private Entity entity;

	// ──────────────────────────────────────────────────────────────────
	// OPT-002: change-only multipart sync. The legacy hook broadcast a
	// full SPacketUpdateMultipart (pos/rot/size for all parts + list +
	// record allocs) to every tracking player on EVERY
	// sendDirtyEntityData pass (every updateInterval ticks, plus every
	// impulse/dirty tick — effectively per tick for an active boss),
	// even when nothing had moved. This mixin keeps the exact legacy
	// call cadence and only decides, per pass, whether the payload is
	// provably redundant.
	//
	// (The "tick" wording below means "sendDirtyEntityData pass"; the
	// skip logic is cadence-agnostic.)
	//
	// Send policy (bit-identical for moving bosses, skips only provably
	// unchanged frames):
	//  1. Any difference in any field writeData() captures — or any part
	//     with dirty synched entity data, or a part-count mismatch —
	//     forces a fresh compile+send, exactly like the legacy path
	//     (exact primitive equality, no epsilon; NaN counts as changed).
	//  2. When everything is provably unchanged, the stored payload is
	//     re-broadcast (bit-identical bytes — every field was just
	//     verified equal and dirty=false) for a linger window of
	//     max(10, part-update-steps, synched-part-update-steps) ticks.
	//     The linger guarantees the client's interpolation counter —
	//     which readData resets to update-steps on every received packet
	//     — is fully drained by identical packets before we go silent,
	//     so the client part settles on exactly the same target it
	//     converges to under the legacy per-tick stream.
	//  3. Only after the linger window do we skip sends entirely; the
	//     first changed frame resumes at legacy cadence.
	//
	// Invalidation story for the cached payload: it is compared
	// field-by-field against live part state every tick (self-
	// invalidating on any change) and reset when a new player starts
	// tracking (addPairing below), which forces a full broadcast on the
	// next tick — the same tick the legacy every-tick stream would have
	// first reached that player. Existing trackers receiving that
	// redundant broadcast is within the legacy behavior envelope (they
	// received identical packets every tick anyway).
	// ──────────────────────────────────────────────────────────────────
	@Unique
	private List<SPacketUpdateMultipart.PartDataHolder> mhlib$lastSentPartData;
	@Unique
	private int mhlib$unchangedDataTicks;

	@Unique
	private static final int MHLIB$MIN_LINGER_TICKS = 10;

	/*
	 * TODO: Rewrite Packets for new Packet system
	 */
	@Inject(
			method = "sendDirtyEntityData()V",
			at = @At("HEAD")
	)
	private void mixinSendDirtyEntityData(CallbackInfo co) {
		if (this.entity.isMultipartEntity() && this.entity instanceof IMultipartEntity<?> ime) {
			// 2.0 S5 (owner-ratified): locally-mirrored profiles skip the
			// STEADY-STATE stream only. The ratified pairing-time seed is
			// implemented by the cache check: addPairing nulls the cache, so
			// a null cache here means a tracker was just gained (or the
			// entity is brand new) and exactly ONE full compile+broadcast
			// below runs as the seed before the gate holds again.
			// (Independent review S5: an unconditional early return had NO
			// seed at all — addPairing itself sends nothing.) Past the seed,
			// gated entities send no part packets: opting out via
			// mhlibShouldStreamParts asserts the clients position parts from
			// a LOCAL mirror (the modern spider's gait replay), and part
			// synched-data or dimension changes do NOT stream.
			if (this.mhlib$lastSentPartData != null && !ime.mhlibShouldStreamParts()) {
				return;
			}
			final List<SPacketUpdateMultipart.PartDataHolder> lastSent = this.mhlib$lastSentPartData;
			if (lastSent != null && this.mhlib$partDataUnchanged(lastSent)) {
				if (this.mhlib$unchangedDataTicks >= this.mhlib$lingerTicks(ime)) {
					// OPT-002: provably identical payload beyond the linger
					// window — skipping the send is unobservable.
					return;
				}
				this.mhlib$unchangedDataTicks++;
				// Linger: re-broadcast the stored payload; bit-identical to
				// what compileList would produce this tick (verified above).
				PacketDistributor.sendToPlayersTrackingEntity(this.entity,
						new SPacketUpdateMultipart(this.entity.getId(), this.entity, lastSent));
				return;
			}
			this.mhlib$unchangedDataTicks = 0;
			SPacketUpdateMultipart updatePacket = new SPacketUpdateMultipart(this.entity);
			this.mhlib$lastSentPartData = updatePacket.data();
			//MHLibPackets.MHLIB_NETWORK.send(PacketDistributor.TRACKING_ENTITY.with(() -> this.entity), updatePacket);
			PacketDistributor.sendToPlayersTrackingEntity(this.entity, updatePacket);
		}
	}

	/**
	 * OPT-002: true only when a freshly compiled payload would be
	 * bit-identical to {@code lastSent}. Mirrors compileList's iteration
	 * exactly: getParts() order, filtered to MHLibPartEntity, compared by
	 * index. Any mismatch (count, order, field value, dirty entity data)
	 * reports "changed" and forces the legacy send path.
	 */
	@Unique
	private boolean mhlib$partDataUnchanged(final List<SPacketUpdateMultipart.PartDataHolder> lastSent) {
		final PartEntity<?>[] parts = this.entity.getParts();
		if (parts == null) {
			// compileList would fail here just as it always did — treat as
			// changed so the legacy path (and its behavior) is preserved.
			return false;
		}
		int index = 0;
		for (final PartEntity<?> part : parts) {
			if (!(part instanceof MHLibPartEntity<?> mhLibPart)) {
				continue;
			}
			if (index >= lastSent.size() || !mhLibPart.mhlibDataUnchangedSince(lastSent.get(index))) {
				return false;
			}
			index++;
		}
		return index == lastSent.size();
	}

	/**
	 * OPT-002: linger window sizing. At least MIN_LINGER_TICKS, and never
	 * smaller than the profile's interpolation step counts, so the last
	 * data-bearing packet's client-side interpolation is always fully
	 * drained by identical resends before sends stop.
	 */
	@Unique
	private int mhlib$lingerTicks(final IMultipartEntity<?> ime) {
		int linger = MHLIB$MIN_LINGER_TICKS;
		final Optional<HitboxProfile> profile = ime.getHitboxProfile();
		if (profile.isPresent()) {
			linger = Math.max(linger, Math.max(profile.get().partUpdateSteps(), profile.get().synchedPartUpdateSteps()));
		}
		return linger;
	}

	/**
	 * OPT-002: a newly paired tracker has received no part state yet —
	 * drop the cached payload so the next sendDirtyEntityData tick does a
	 * full compile+broadcast, matching the tick on which the legacy
	 * every-tick stream would have first reached the new player.
	 */
	@Inject(
			method = "addPairing",
			at = @At("TAIL")
	)
	private void mixinAddPairing(ServerPlayer player, CallbackInfo ci) {
		this.mhlib$lastSentPartData = null;
		this.mhlib$unchangedDataTicks = 0;
	}

}
