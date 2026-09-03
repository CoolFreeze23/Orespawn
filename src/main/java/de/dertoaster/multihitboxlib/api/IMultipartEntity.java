package de.dertoaster.multihitboxlib.api;

import de.dertoaster.multihitboxlib.entity.MHLibPartEntity;
import de.dertoaster.multihitboxlib.entity.hitbox.HitboxProfile;
import de.dertoaster.multihitboxlib.entity.hitbox.SubPartConfig;
import de.dertoaster.multihitboxlib.init.MHLibDatapackLoaders;
import de.dertoaster.multihitboxlib.network.client.CPacketBoneInformation;
import de.dertoaster.multihitboxlib.network.server.SPacketSetMaster;
import de.dertoaster.multihitboxlib.util.BoneInformation;
import de.dertoaster.multihitboxlib.util.ClientOnlyMethods;
import de.dertoaster.multihitboxlib.util.MHLibCounters;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import net.neoforged.neoforge.entity.PartEntity;
import net.neoforged.neoforge.network.PacketDistributor;


public interface IMultipartEntity<T extends Entity> {
	
	public default boolean hurt(PartEntity<T> subPart, DamageSource source, float damage) {
		if(!(this instanceof Entity)) {
			throw new IllegalStateException("implementing class must extend " + Entity.class.descriptorString());
		}
		Entity entity = (Entity)this;
		// ──────────────────────────────────────────────────────────────
		// 2.0 S4 (vendored change, Queen-safe): ENVIRONMENTAL (source-less)
		// damage arriving VIA a part is dropped when the profile's MAIN
		// hitbox can receive damage itself. Rationale: on such profiles
		// (the spider) parts are directed-attack surfaces and the
		// environment already acts on the body — part boxes ticking their
		// own baseTick (lava, etc.) otherwise open damage channels the
		// classic single-box entity never had (review: dangling shins over
		// lava). Profiles whose main hitbox CANNOT receive damage
		// (TheQueen) keep routing everything: parts are their ONLY damage
		// channel, environment included — behavior unchanged.
		// ──────────────────────────────────────────────────────────────
		if (source.getEntity() == null && source.getDirectEntity() == null) {
			final Optional<HitboxProfile> profile = this.getHitboxProfile();
			if (profile != null && profile.isPresent()
					&& profile.get().mainHitboxConfig().canReceiveDamage()) {
				return false;
			}
		}
		return entity.hurt(source, damage);
	}
	
	public boolean callSuperHurt(DamageSource source, float damage);
	
	public default void setMasterUUID(UUID id) {
		if(!(this instanceof Entity)) {
			throw new IllegalStateException("implementing class must extend " + Entity.class.descriptorString());
		}
		Entity entity = (Entity)this;
		if (this instanceof IMHLibFieldAccessor<?> access) {
			if (entity.level().isClientSide()) {
				// System.out.println("Clientside, not setting master!");
				// TODO: Why are we just setting it here anywa?!
				access._mhlibAccess_setMasterUUID(id);
				// OPT-003 (ruled 2026-08-11): any client-visible mastership
				// change invalidates the change-only streaming cache (see
				// updateSynching). The server's election loop re-rotates
				// every tick until the elected master's first packet arrives
				// (ticksSinceLastSynch is NOT reset by election), so the
				// first payload after a master change must go out at legacy
				// timing — a null last-sent cache forces exactly that.
				access._mhlibAccess_setLastSentBoneInformation(null);
				return;
			}
			access._mhlibAccess_setMasterUUID(id);
			// ──────────────────────────────────────────────────────────
			// CRITICAL: Do NOT broadcast SPacketSetMaster when id is null.
			//
			// Upstream MHL (1.21-NeoForge branch) had a latent NPE here:
			// the rotate-tracker logic in mhlibAiStep() calls
			// setMasterUUID(null) every time the current bone-info
			// master goes 10+ ticks without sending sync data, then
			// IMMEDIATELY re-calls setMasterUUID(<next queued UUID>).
			// The first call (with null) reached this branch and tried
			// to encode SPacketSetMaster.masterUUID via
			// UtilityCodecs.UUID_STRING_CODEC, which is non-nullable.
			// Result on 1.21.1 NeoForge: every player tracking *any*
			// MHL-bound entity got disconnected on world join with
			// "Failed to encode packet 'clientbound/minecraft:custom_payload'".
			//
			// Skipping the broadcast when id==null is safe: the
			// follow-up setMasterUUID(realUUID) call will fire its own
			// packet on the same tick, and the local field has already
			// been updated above so the server's view is consistent.
			// ──────────────────────────────────────────────────────────
			if (id == null) {
				// OPT-003 (ruled 2026-08-11): a server-side master reset only
				// happens when the bone stream is dead (the 10-tick timeout
				// in updateSynching, or the master stopped tracking) — the
				// retained sync map (see mhlibAiStep) is no longer backed by
				// a live stream, so drop it here; synched parts fall back to
				// config offsets exactly as the legacy per-tick-cleared map
				// behaved during genuine master silence.
				access._mhlibAccess_getSynchMap().clear();
				return;
			}
			SPacketSetMaster masterPacket = new SPacketSetMaster(this);
			//MHLibPackets.send(masterPacket, PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity));
			PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, masterPacket);
		} else {
			throw new IllegalStateException("Access interface not implemented");
		}
	}
	
	@Nullable
	public default UUID getMasterUUID() {
		if (this instanceof IMHLibFieldAccessor<?> access) {
			return access._mhlibAccess_getMasterUUID();
		} else {
			throw new IllegalStateException("Access interface not implemented");
		}
	}
	
	public default void processBoneInformation(final Map<String, BoneInformation> boneInformation) {
		if (this instanceof IMHLibFieldAccessor<?> access) {
			// OPT-003 (ruled 2026-08-11): every accepted packet REPLACES the
			// retained sync map wholesale (clear-then-put). The payload is
			// the master's complete synched-bone state for its tick, so a
			// bone absent from it (e.g. the master stopped rendering the
			// entity and now streams empty payloads) must fall back to
			// config offsets — just like the legacy per-tick-cleared map
			// did. Retention between packets lives in mhlibAiStep.
			access._mhlibAccess_getSynchMap().clear();
			// Process the bones...
			for (Map.Entry<String, BoneInformation> entry : boneInformation.entrySet()) {
				Optional<MHLibPartEntity<T>> optPart = this.getPartByName(entry.getKey());
				optPart.ifPresent(part -> {
					access._mhlibAccess_getSynchMap().put(part.getConfigName(), entry.getValue());
				});
			}
			access._mhlibAccess_setTicksSinceLastSynch(0);
		} else {
			throw new IllegalStateException("Access interface not implemented");
		}
	}
	
	public default boolean syncWithModel() {
		return this.getHitboxProfile().isPresent() && this.getHitboxProfile().get().syncToModel();
	}

	/**
	 * 2.0 S5 (vendored change, owner-RATIFIED ruling): whether the per-tick
	 * {@code SPacketUpdateMultipart} stream serves this entity's trackers.
	 * Profiles that never sync with the model AND grant zero client
	 * deviation are locally mirrored by their clients — every streamed
	 * position is overwritten on arrival (~4 KB/s per tracked robot of
	 * dead traffic). Contract (corrected by the S5 independent review):
	 * a gated entity sends exactly ONE full compile per gained tracker —
	 * the pairing-time seed, implemented in MixinServerEntity as a
	 * null-cache pass-through after addPairing — and NOTHING else; part
	 * synched entity data and part dimension changes do not stream past
	 * the seed. Returning false therefore asserts "my clients position
	 * (and re-dimension) parts from a local mirror" — true for the modern
	 * spider, whose client replays the gait and places parts itself.
	 * TheQueen (sync-to-model) streams unchanged.
	 */
	public default boolean mhlibShouldStreamParts() {
		final Optional<HitboxProfile> profile = this.getHitboxProfile();
		if (profile == null || !profile.isPresent()) {
			return true;
		}
		if (profile.get().syncToModel()) {
			return true;
		}
		for (SubPartConfig partConfig : profile.get().partConfigs()) {
			if (partConfig.maxDeviationFromServer() > 0.0D) {
				return true;
			}
		}
		return false;
	}
	
	public default void createSubPartsFromProfile(final HitboxProfile profile, final T parentEntity, final BiConsumer<String, MHLibPartEntity<T>> storageFunction) {
		// OPT-019: precompute each part's synched-bone flags from the very
		// profile that creates it. Safe because the profile an entity
		// resolves is fixed for its lifetime (per-entity cache + immutable
		// registry content, see getHitboxProfile) — EXCEPT for a dynamic
		// ICustomHitboxProfileSupplier, which could legally return a
		// different profile per call; those keep the live lookup path
		// (SpiderRobot is the codebase's implementor since 2.0 S4 — its
		// mode-gated supplier is exactly the dynamic case this guards).
		final boolean stableProfile = !(this instanceof ICustomHitboxProfileSupplier);
		int subPartNumber = 0;
		for(SubPartConfig spc : profile.partConfigs()) {
			MHLibPartEntity<T> part = this.createNewPartFrom(spc, parentEntity, subPartNumber);
			subPartNumber++;
			if (stableProfile) {
				part.mhlibPrecomputeSynchedFlags(profile);
			}

			storageFunction.accept(spc.name(), part);
		}
	}
	
	public default MHLibPartEntity<T> createNewPartFrom(SubPartConfig spc, final T parentEntity, final int subPartNumber) {
		return spc.hitboxType().createPartEntity(spc, parentEntity, subPartNumber);
	}
	
	public default Optional<MHLibPartEntity<T>> getPartByName(final String name) {
		if (this instanceof IMHLibFieldAccessor<?> access) {
			if (access._mhlibAccess_getPartMap() == null) {
				return Optional.empty();
			}
			return Optional.ofNullable((MHLibPartEntity<T>) access._mhlibAccess_getPartMap().getOrDefault(name, null));
		} else {
			throw new IllegalStateException("Access interface not implemented");
		}
	}
	
	public Queue<UUID> getTrackerQueue();
	public int getTicksSinceLastSync();
	
	public default Optional<CPacketBoneInformation.Builder> getBoneInfoBuilder() {
		if (this instanceof IMHLibFieldAccessor<?> access) {
			return access._mlibAccess_getBoneInfoBuilder();
		} else {
			throw new IllegalStateException("Access interface not implemented");
		}
	}
	public default void clearBoneInfoBuilder() {
		if (this instanceof IMHLibFieldAccessor<?> access) {
			access._mlibAccess_setBoneInfoBuilder(Optional.empty());
		} else {
			throw new IllegalStateException("Access interface not implemented");
		}
	}
	public default void setBoneInfoBuilderContent(CPacketBoneInformation.Builder builder) {
		if (this instanceof IMHLibFieldAccessor<?> access) {
			access._mlibAccess_setBoneInfoBuilder(Optional.ofNullable(builder));
		} else {
			throw new IllegalStateException("Access interface not implemented");
		}
	}

	public default void alignSubParts(T entity, final Collection<MHLibPartEntity<T>> parts) {
		final double curX = entity.getX();
		final double curY = entity.getY();
		final double curZ = entity.getZ();

		final float rotX = (float) (this.mhlibGetEntityRotationXForPartOffset() + Math.toRadians(entity.getXRot()));
		// TODO: Unsure what to do with this as this could mess up the position if we just add the y rot to it...
		// ... Otherwise this is for non synched parts, so it should be alright
		final float rotY = (float) (this.mhlibGetEntityRotationYForPartOffset() + Math.toRadians(entity.getYRot()));
		final float rotZ = this.mhlibGetEntityRotationZForPartOffset();

		final double entityScale = this.mhlibGetEntitySizeInternally(entity);

		// ──────────────────────────────────────────────────────────────
		// OPT-019: the former per-part Vec3 chain
		// (xRot -> yRot -> zRot -> scale -> add -> subtract, ~6 Vec3
		// allocs per part per tick) is folded into inline double math.
		// Neutrality: the sin/cos factors are hoisted once per call —
		// Vec3.xRot/yRot/zRot recompute Mth.cos/Mth.sin per part from the
		// SAME constant angles, and Mth trig is a pure table lookup, so
		// the factors are bit-identical. They are kept as floats so each
		// float*double promotion matches Vec3's arithmetic exactly, the
		// operand order below mirrors Vec3.xRot/yRot/zRot/scale/add
		// verbatim, and IEEE-754 defines a - b == a + (-b), which makes
		// the final "(rotated*scale + cur) - pivot" identical to the old
		// add().subtract() chain. Scaling is applied through the
		// value-reusing setScaling(float, float) overload (same float
		// casts as the old new Vec3 -> Tuple path).
		// ──────────────────────────────────────────────────────────────
		final float cosRotX = Mth.cos(rotX);
		final float sinRotX = Mth.sin(rotX);
		final float cosRotY = Mth.cos(rotY);
		final float sinRotY = Mth.sin(rotY);
		final float cosRotZ = Mth.cos(rotZ);
		final float sinRotZ = Mth.sin(rotZ);
		final float scaleA = (float) entityScale;

		for(MHLibPartEntity<T> part : parts) {
			// OPT-019: precomputed at part construction (profile content is
			// immutable per entity lifetime); falls back to the identical
			// live profile check when no precomputed flag exists.
			if (part.mhlibIsListedAsSynchedBone()) {
				continue;
			}
			final Vec3 base = part.getConfigPositionOffset();
			// Vec3.xRot(rotX)
			final double x1 = base.x;
			final double y1 = base.y * cosRotX + base.z * sinRotX;
			final double z1 = base.z * cosRotX - base.y * sinRotX;
			// .yRot(rotY)
			final double x2 = x1 * cosRotY + z1 * sinRotY;
			final double z2 = z1 * cosRotY - x1 * sinRotY;
			// .zRot(rotZ)
			final double x3 = x2 * cosRotZ + y1 * sinRotZ;
			final double y3 = y1 * cosRotZ - x2 * sinRotZ;
			// .scale(entityScale), .add(cur...), .subtract(pivot)
			final Vec3 pivot = part.getPivot();

			part.setScaling(scaleA, scaleA);
			part.setPos(
					(x3 * entityScale + curX) - pivot.x,
					(y3 * entityScale + curY) - pivot.y,
					(z2 * entityScale + curZ) - pivot.z);
		}
	}
	
	public default void alignSynchedSubParts(T entity, final BiFunction<String, BoneInformation, BoneInformation> infoRetrievalFunction) {
		final double curX = entity.getX();
		final double curY = entity.getY();
		final double curZ = entity.getZ();

		// Rotations for the fallback position
		final float rotX = (float) (this.mhlibGetEntityRotationXForPartOffset() + Math.toRadians(entity.getXRot()));
		// TODO: Unsure what to do with this as this could mess up the position if we just add the y rot to it...
		// ... Otherwise this is for non synched parts, so it should be alright
		final float rotY = (float) (this.mhlibGetEntityRotationYForPartOffset() + Math.toRadians(entity.getYRot()));
		final float rotZ = this.mhlibGetEntityRotationZForPartOffset();

		final double entityScale = this.mhlibGetEntitySizeInternally(entity);

		// OPT-001: hoisted — one cached profile fetch for the whole pass
		// (the profile cannot change within a tick; see getHitboxProfile).
		final HitboxProfile profile = this.getHitboxProfile().get();

		// Evaluate model data
		for (String syncedBone : profile.synchedBones()) {
			//System.out.println("Synching bone: " + syncedBone);
			Optional<MHLibPartEntity<T>> optPart = this.getPartByName(syncedBone);
			if (optPart.isEmpty()) {
				//System.out.println("No part found!");
				continue;
			}
			MHLibPartEntity<T> part = optPart.get();

			// ──────────────────────────────────────────────────────────
			// OPT-019: the fallback BoneInformation (plus its rotated
			// Vec3 offset chain) used to be built for EVERY synced bone
			// every tick, only to be discarded whenever the sync map had
			// data. We now probe with a null fallback and construct the
			// fallback lazily, only when the map lacks the bone.
			// Neutrality: the only retrieval function ever passed in is
			// syncMap::getOrDefault (mhlibAiStep) on a HashMap whose
			// values are never null, so apply(bone, null) == null exactly
			// when apply(bone, fallback) would have returned the
			// fallback — and in that case the fallback constructed below
			// is identical to the legacy eager one.
			// ──────────────────────────────────────────────────────────
			BoneInformation bi = infoRetrievalFunction.apply(syncedBone, null);
			if (bi == null) {
				Vec3 partOffset = part.getConfigPositionOffset();
				partOffset = partOffset.xRot(rotX);
				partOffset = partOffset.yRot(rotY);
				partOffset = partOffset.zRot(rotZ);

				partOffset = partOffset.scale(entityScale);

				//System.out.println("SynchedDataMap contents: " + this.syncDataMap.keySet().toString());
				bi = new BoneInformation(
						syncedBone,
						false,
						part.getConfig() != null ? partOffset.add(curX, curY, curZ) : Vec3.ZERO,
						BoneInformation.DEFAULT_SCALING,
						part.getConfig() != null ? part.getConfig().hitboxType().getBaseRotation() : Vec3.ZERO
				);
			}
			bi = bi.scale(entityScale);
			//System.out.println("Sync data: " + bi.toString());

			part.applyInformation(bi);
		}
	}

	default double mhlibGetEntitySizeInternally(T entity) {
		if (this instanceof IMHLibSizeCallback sc) {
			return sc.mhlibGetEntitySizeScale(entity);
		} else {
			if(entity instanceof AgeableMob am) {
				if(am.isBaby()) {
					return 0.5D;
				}
			}
			return 1;
		}
	}

	public default <E extends Entity & IMultipartEntity<?>> void mhlibAiStep() {
		if (this instanceof IMHLibFieldAccessor access) {
			E e = (E)this;
			// First, send packet if present or handle leader stuff
			this.updateSynching(e);

			if (e.level().isClientSide()) {
				return;
			}

			// Won't align synched parts
			this.alignSubParts((T)(Object)this, access._mhlibAccess_getPartMap().values());

			// Now, handle synched parts
			// OPT-001: hoisted single lookup (was two); same-tick value cannot change.
			final Optional<HitboxProfile> profile = this.getHitboxProfile();
			if (profile.isPresent() && profile.get().syncToModel()) {
				Map<String, BoneInformation> syncMap = access._mhlibAccess_getSynchMap();
				this.alignSynchedSubParts((T)(Object)this, syncMap::getOrDefault);
				// ──────────────────────────────────────────────────────
				// OPT-003 (ruled 2026-08-11): the sync map is RETAINED
				// between packets instead of being cleared here every
				// tick. Legacy could clear it because the master client
				// re-sent an (identical) packet every tick; the master now
				// skips provably unchanged payloads (see updateSynching),
				// so a skipped tick must re-apply the last received bone
				// state — which is exactly, bit-identically, what the
				// legacy identical-packet stream produced on such ticks.
				// Cache invalidation story: the map is replaced wholesale
				// by every accepted master packet (processBoneInformation)
				// and dropped on a server-side master reset
				// (setMasterUUID(null): the 10-tick timeout or the master
				// untracking), after which alignment falls back to config
				// offsets exactly as the legacy cleared-map path did
				// during genuine master silence.
				// ──────────────────────────────────────────────────────
			}

		} else {
			throw new IllegalStateException("Access interface not implemented");
		}
	}

	/**
	 * OPT-003 (ruled 2026-08-11): keepalive interval for the master client's
	 * change-only bone streaming (see updateSynching). A provably unchanged
	 * payload is re-sent at least this often so the server's 10-tick master
	 * timeout below can never fire from protocol silence — 8 leaves a 2-tick
	 * margin under that timeout for tick alignment and latency jitter.
	 */
	public static final int BONE_INFORMATION_KEEPALIVE_TICKS = 8;

	/*
	 * Updates and resets the master entity on server side.
	 * On client, if a boneinfobuilder is present, it compiles a packet and sends it to the server, afterwise, the builder gets cleared
	 * OPT-003 (ruled 2026-08-11): the client send is now change-only with a keepalive — see the comment in the client branch.
	 */
	public default <E extends Entity & IMultipartEntity<?>> void updateSynching(E entity) {
		if (!entity.level().isClientSide()) {
			// ──────────────────────────────────────────────────────────
			// 2.0 S4 (vendored change, Queen-neutral): profiles that never
			// stream bones (sync-with-model = false, e.g. the solver-fed
			// spider legs) have no use for master election. Without this
			// gate the 10-tick answer timeout below re-elected a master and
			// broadcast SPacketSetMaster forever — one packet per ~10 ticks
			// per tracked boneless multipart entity — pure churn against
			// the change-only traffic law. sync-with-model = true entities
			// (TheQueen) take the original path unchanged; the
			// HitboxPartTests election test pins BOTH behaviors.
			// ──────────────────────────────────────────────────────────
			if (!this.syncWithModel()) {
				return;
			}
			// If you already have a master, let's check them...
			// If there was no packet for quite some time => elect a new master
			// System.out.println("Checking master answer time...");
			if (this.getMasterUUID() != null && this.getTicksSinceLastSync() >= 10) {
				// System.out.println("Master found and too long answer time!");
				if (this.getTrackerQueue().contains(this.getMasterUUID())) {
					this.getTrackerQueue().remove(this.getMasterUUID());
				}
				this.getTrackerQueue().add(this.getMasterUUID());
				this.setMasterUUID(null);
				// System.out.println("Master was reset!");
			}
			
			// If you don't have a master anyway, elect a new one
			if (this.getMasterUUID() == null) {
				// System.out.println("Setting new master...");
				if (!this.getTrackerQueue().isEmpty()) {
					this.setMasterUUID(this.getTrackerQueue().poll());
				}
			}
		}
		else {
			// 2.0 S4 (vendored, Queen-neutral): mirror the server gate on the
			// CLIENT branch — without it every client tracking a boneless-
			// profile multipart built and keepalive-sent an EMPTY
			// CPacketBoneInformation every 8 ticks per entity (the server
			// handler drops them for such profiles: guaranteed-useless C2S
			// traffic the first gate's own rationale condemns).
			if (!this.syncWithModel()) {
				return;
			}
			if (!(this instanceof IMHLibFieldAccessor<?> access)) {
				throw new IllegalStateException("Access interface not implemented");
			}
			// ──────────────────────────────────────────────────────────
			// OPT-003 (ruled 2026-08-11): change-only bone streaming from
			// the master client, with a keepalive. Legacy built and sent a
			// CPacketBoneInformation effectively every client tick (a
			// populated one per rendered tick on the master, an empty one
			// every other tick otherwise) purely so the server's 10-tick
			// master timeout never fired. The freshly built payload is now
			// diffed against the last packet actually sent:
			//
			//  1. Any difference — bone set, hidden flag, or any position/
			//     scale/rotation component (exact primitive equality, no
			//     epsilon; NaN compares as changed and fails open to a
			//     send, mirroring OPT-002's server-side
			//     MHLibPartEntity#mhlibDataUnchangedSince) — sends
			//     immediately: moving/animating bones stream at the legacy
			//     cadence with bit-identical payloads.
			//  2. A provably identical payload is skipped, but never for
			//     longer than BONE_INFORMATION_KEEPALIVE_TICKS (8) ticks —
			//     the keepalive re-sends the identical packet with a
			//     2-tick margin under the server's 10-tick master timeout
			//     (server branch above), so master election never rotates
			//     from protocol silence.
			//
			// Throttle invalidation story: the last-sent cache is
			// (a) compared field-by-field against every built payload, so
			// it self-invalidates on any change; (b) force-expired by the
			// keepalive counter (clamped at the interval so it cannot
			// overflow); (c) nulled on every client-visible mastership
			// change (setMasterUUID client branch), which pushes the first
			// payload after an election out at legacy timing. The server
			// covers skipped ticks with the retained sync map — see
			// mhlibAiStep / processBoneInformation.
			// ──────────────────────────────────────────────────────────
			final int sinceLastSend = Math.min(access._mhlibAccess_getTicksSinceLastBoneInfoSend() + 1, BONE_INFORMATION_KEEPALIVE_TICKS);
			access._mhlibAccess_setTicksSinceLastBoneInfoSend(sinceLastSend);
			// System.out.println("Beginning bone information collection...");
			if (this.getBoneInfoBuilder().isPresent()) {
				// Send packet
				// System.out.println("Sending bone information...");
				CPacketBoneInformation packet = this.getBoneInfoBuilder().get().build();
				final Map<String, BoneInformation> lastSent = access._mhlibAccess_getLastSentBoneInformation();
				if (lastSent == null
						|| sinceLastSend >= BONE_INFORMATION_KEEPALIVE_TICKS
						|| !mhlibBoneInformationUnchanged(lastSent, packet.boneInformation())) {
					packet.send();
					access._mhlibAccess_setLastSentBoneInformation(packet.boneInformation());
					access._mhlibAccess_setTicksSinceLastBoneInfoSend(0);
				}
				// else: payload proven bit-identical to the last-sent packet
				// inside the keepalive window — the send is skipped.
				this.clearBoneInfoBuilder();
			} else {
				// System.out.println("creating new packet");
				CPacketBoneInformation.Builder builder = CPacketBoneInformation.builder(entity);
				this.setBoneInfoBuilderContent(builder);
			}
		}
	}

	/**
	 * OPT-003 (ruled 2026-08-11): true only when {@code current} would put the
	 * server into exactly the state {@code lastSent} already did — same bone
	 * set and every field of every {@link BoneInformation} identical. Field
	 * comparison uses exact primitive equality (no epsilon; NaN reports
	 * "changed" and fails open to a send), mirroring the OPT-002 server-side
	 * probe {@code MHLibPartEntity#mhlibDataUnchangedSince}.
	 */
	private static boolean mhlibBoneInformationUnchanged(final Map<String, BoneInformation> lastSent, final Map<String, BoneInformation> current) {
		if (lastSent.size() != current.size()) {
			return false;
		}
		for (Map.Entry<String, BoneInformation> entry : current.entrySet()) {
			final BoneInformation prev = lastSent.get(entry.getKey());
			final BoneInformation cur = entry.getValue();
			// name() needs no explicit compare: compileMap keys every entry by
			// name(), so matching keys imply matching names.
			if (prev == null
					|| prev.hidden() != cur.hidden()
					|| !mhlibVecUnchanged(prev.worldPos(), cur.worldPos())
					|| !mhlibVecUnchanged(prev.scale(), cur.scale())
					|| !mhlibVecUnchanged(prev.rotation(), cur.rotation())) {
				return false;
			}
		}
		return true;
	}

	/**
	 * OPT-003: exact primitive equality — NaN != NaN reports "changed" (fail
	 * open to a send); no epsilon, per the OPT-002 diffing philosophy.
	 */
	private static boolean mhlibVecUnchanged(final Vec3 a, final Vec3 b) {
		return a.x == b.x && a.y == b.y && a.z == b.z;
	}
	
	public default void tickParts(final Collection<MHLibPartEntity<T>> parts) {
		for(MHLibPartEntity<T> part : parts) {
			part.tick();
		}
		if (this instanceof IMHLibFieldAccessor access) {
			access._mhlibAccess_setTicksSinceLastSynch(access._mhlibAccess_getTicksSinceLastSynch() + 1);
		} else {
			throw new IllegalStateException("Access interface not implemented");
		}
	}

	public default float mhlibGetEntityRotationXForPartOffset() {
		return 0;
	}
	
	public default float mhlibGetEntityRotationYForPartOffset() {
		return 0;
	}
	
	public default float mhlibGetEntityRotationZForPartOffset() {
		return 0;
	}
	
	public default Optional<HitboxProfile> getHitboxProfile() {
		if (this instanceof ICustomHitboxProfileSupplier ichps) {
			// Never cached: a custom supplier may be dynamic, so it keeps the
			// exact legacy call-through semantics (null falls through below).
			Optional<HitboxProfile> resTmp = ichps.getHitboxProfile();
			if (resTmp != null) {
				return resTmp;
			}
		}
		if (this instanceof Entity ent && ent.level() != null) {
			// ──────────────────────────────────────────────────────────
			// OPT-001: per-entity memoization of the datapack profile
			// lookup. The uncached path (registry getKey + datapack map
			// walk + Optional allocs) ran per part per tick on the server
			// and per bone per frame on the client. An entity's
			// level().registryAccess() is fixed for its lifetime, and
			// datapack-registry content is immutable per registry
			// instance, so the resolved Optional can only become stale
			// across a datapack reload — which is covered by the
			// generation stamp: MHLibDatapackLoaders bumps its generation
			// on AddReloadListenerEvent (server start + /reload) and
			// ServerStoppedEvent, forcing a re-resolve here. The
			// generation is read BEFORE resolving, so a reload racing
			// this lookup can only cause a redundant re-resolve, never a
			// stale serve.
			// ──────────────────────────────────────────────────────────
			if (this instanceof IMHLibFieldAccessor<?> access) {
				final int generation = MHLibDatapackLoaders.getProfileCacheGeneration();
				final Optional<HitboxProfile> cached = access._mhlibAccess_getCachedHitboxProfile();
				if (cached != null && access._mhlibAccess_getCachedHitboxProfileGeneration() == generation) {
					return cached;
				}
				final Optional<HitboxProfile> resolved = MHLibDatapackLoaders.getHitboxProfile(ent.getType(), ent.level().registryAccess());
				access._mhlibAccess_setCachedHitboxProfile(resolved, generation);
				return resolved;
			}
			EntityType<?> type = ent.getType();
			return MHLibDatapackLoaders.getHitboxProfile(type, ent.level().registryAccess());
		}
		return Optional.empty();
	}
	
	public default boolean tryAddBoneInformation(String boneName, boolean hidden, Vec3 position, Vec3 scaling, Vec3 rotation) {
		IMHLibFieldAccessor access = (IMHLibFieldAccessor) this;
		Entity entity = (Entity)this;

		if (!entity.level().isClientSide()) {
			return false;
		}
		UUID myMaster = this.getMasterUUID();
		if (myMaster == null || !myMaster.equals(ClientOnlyMethods.getClientPlayer().getUUID())) {
			return false;
		}
		// OPT-001: hoisted — this method ran up to five profile lookups per
		// bone per frame on the master client; the value is stable within a
		// call, so one cached fetch is equivalent.
		final Optional<HitboxProfile> optProfile = this.getHitboxProfile();
		if (optProfile.isEmpty()) {
			return false;
		}
		final HitboxProfile profile = optProfile.get();
		if (!profile.syncToModel()) {
			return false;
		}
		if (access._mlibAccess_getBoneInfoBuilder().isEmpty()) {
			//return false;
			CPacketBoneInformation.Builder builder = CPacketBoneInformation.builder(entity);
			access._mlibAccess_setBoneInfoBuilder(Optional.of(builder));
		}

		Optional<CPacketBoneInformation.Builder> optBuilder = access._mlibAccess_getBoneInfoBuilder();
		CPacketBoneInformation.Builder builder = optBuilder.get();
		try {
			builder = builder.addInfo(boneName).hidden(hidden).position(position).scaling(scaling).rotation(rotation).done();
		} catch(IllegalStateException ise) {
			return false;
		}
		if (MHLibCounters.ENABLED) {
			MHLibCounters.CLIENT_BONE_INFOS_BUILT.increment();
		}

		// Now, if we have the freedom ... directly apply the information...
		if (profile.trustClient()) {
			Optional<MHLibPartEntity<T>> optPart = this.getPartByName(boneName);
			if (optPart.isPresent() && optPart.get().isSynched()) {
				final double distance = Math.abs(optPart.get().position().distanceToSqr(position));
				if (distance <= optPart.get().getConfig().maxDeviationFromServer()) {
					// You may
					optPart.get().setPositionAndRotationDirect(position.x(), position.y(), position.z(), (float)rotation.y(), (float)rotation.x(), profile.synchedPartUpdateSteps());
				}
			}
		}

		return true;
	}
	
	public default void mhLibOnStartTrackingEvent(ServerPlayer sp) {
		// 2.0 S4 (vendored, Queen-neutral): boneless profiles never stream
		// bones, so tracking-start must not elect a master or broadcast
		// SPacketSetMaster — the same rationale as updateSynching's gate
		// (the review found this hook was the leak the first gate missed).
		if (!this.syncWithModel()) {
			return;
		}
		IMHLibFieldAccessor access = (IMHLibFieldAccessor) this;
		// System.out.println("Adding tracker: " + sp.getUUID() != null ? sp.getUUID().toString() : "NONE");
		if (!access._mhlibAccess_getTrackerQueue().contains(sp.getUUID())) {
			access._mhlibAccess_getTrackerQueue().add(sp.getUUID());
		}
		if (this.getMasterUUID() == null) {
			Queue<UUID> q = access._mhlibAccess_getTrackerQueue();
			this.setMasterUUID(q.poll());
		}
	}

	public default void mhLibOnStopTrackingEvent(ServerPlayer sp) {
		// 2.0 S4: same gate as start-tracking (see above).
		if (!this.syncWithModel()) {
			return;
		}
		if (this.getTrackerQueue().contains(sp.getUUID())) {
			this.getTrackerQueue().remove(sp.getUUID());
		}
		if (this.getMasterUUID() != null && this.getMasterUUID().equals(sp.getUUID())) {
			this.setMasterUUID(null);
		}
		if (this.getMasterUUID() == null && this.getTrackerQueue().size() > 0) {
			this.setMasterUUID(this.getTrackerQueue().poll());
		}
	}
	
	public default void processSetMasterPacket(final SPacketSetMaster packet) {
		if(this instanceof Entity entity) {
			if (entity.level().isClientSide()) {
				this.setMasterUUID(packet.masterUUID());
			}
		} else {
			throw new IllegalStateException("This interface may only be implemented on entities!");
		}
	}

	public default void mhlibSetID(int id) {
		if (this instanceof Entity entity) {
			// NEVER DO THIS, this will cause a infintie recursion loop!
			//entity.setId(id);

			if (entity.isMultipartEntity() && entity.getParts() != null) {
				for (int i = 0; i < entity.getParts().length; i++) {
					entity.getParts()[i].setId(id + i + 1);
				}
			}
		} else {
			throw new IllegalStateException("Interface must be implemented on at least Entity.class!");
		}
	}

	public default boolean mhLibIsPickable(boolean entityClassResult) {
		// OPT-001: hoisted — isPickable is queried per raycast; one cached
		// profile fetch replaces three (value stable within the call).
		final Optional<HitboxProfile> profile = this.getHitboxProfile();
		if(profile != null && profile.isPresent()) {
			return entityClassResult && profile.get().mainHitboxConfig().canReceiveDamage();
		}
		return entityClassResult;
	}

	public default PartEntity<?>[] mhLibGetParts() {
		if (this instanceof IMHLibFieldAccessor access) {
			return access._mhlibAccess_getPartArray();
		} else {
			throw new IllegalStateException("Access interface not implemented");
		}
	}

	public default void mhlibOnConstructor() {
		IMHLibFieldAccessor access = (IMHLibFieldAccessor) this;
		Entity entity = (Entity) this;
		// Load base profile
		// this.HITBOX_PROFILE = MHLibDatapackLoaders.getHitboxProfile(this.getType());

		// OPT-018: this hook runs for EVERY LivingEntity constructed
		// JVM-wide (vanilla mobs included) and used to trigger up to four
		// registry-key + datapack-map lookups. It now resolves the profile
		// ONCE — through the per-entity cache (OPT-001), whose miss path
		// hits the EntityType-memoized static cache in
		// MHLibDatapackLoaders, so non-multipart mobs bail on a cached
		// Optional.empty(). Collapsing the redundant isPresent() branches
		// is neutral: they were all dominated by the early return below.
		final Optional<HitboxProfile> optProfile = this.getHitboxProfile();
		if(!optProfile.isPresent()) {
			return;
		}
		final HitboxProfile profile = optProfile.get();

		// Initialize map and array
		int partCount = profile.partConfigs().size();

		Map<String, MHLibPartEntity<T>> partMap = new Object2ObjectArrayMap<>(partCount);
		PartEntity<?>[] partArray = new PartEntity<?>[partCount];

		// At last, create the parts themselves
		final BiConsumer<String, MHLibPartEntity<T>> storageFunction = (str, part) -> {
			int id = 0;
			while(partArray[id] != null) {
				id++;
			}
			partArray[id] = part;
			partMap.put(str, part);
		};
		this.createSubPartsFromProfile(profile, (T)((Object)this), storageFunction);

		access._mhlibAccess_setPartMap(partMap);
		access._mhlibAccess_setPartArray(partArray);

		if (entity.isMultipartEntity() && entity.getParts() != null) {
			// TODO: AccessTransformer
			entity.setId(Entity.ENTITY_COUNTER.getAndAdd(entity.getParts().length + 1) + 1);
		}
	}
}
