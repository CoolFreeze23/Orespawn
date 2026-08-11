package de.dertoaster.multihitboxlib.api;

import de.dertoaster.multihitboxlib.entity.MHLibPartEntity;
import de.dertoaster.multihitboxlib.entity.hitbox.HitboxProfile;
import de.dertoaster.multihitboxlib.entity.hitbox.SubPartConfig;
import de.dertoaster.multihitboxlib.init.MHLibDatapackLoaders;
import de.dertoaster.multihitboxlib.network.client.CPacketBoneInformation;
import de.dertoaster.multihitboxlib.network.server.SPacketSetMaster;
import de.dertoaster.multihitboxlib.util.BoneInformation;
import de.dertoaster.multihitboxlib.util.ClientOnlyMethods;
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
	
	public default void createSubPartsFromProfile(final HitboxProfile profile, final T parentEntity, final BiConsumer<String, MHLibPartEntity<T>> storageFunction) {
		// OPT-019: precompute each part's synched-bone flags from the very
		// profile that creates it. Safe because the profile an entity
		// resolves is fixed for its lifetime (per-entity cache + immutable
		// registry content, see getHitboxProfile) — EXCEPT for a dynamic
		// ICustomHitboxProfileSupplier, which could legally return a
		// different profile per call; those keep the live lookup path
		// (no implementor exists in this codebase, this is API hygiene).
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
				access._mhlibAccess_getSynchMap().clear();
			}

		} else {
			throw new IllegalStateException("Access interface not implemented");
		}
	}

	/*
	 * Updates and resets the master entity on server side.
	 * On client, if a boneinfobuilder is present, it compiles a packet and sends it to the server, afterwise, the builder gets cleared
	 */
	public default <E extends Entity & IMultipartEntity<?>> void updateSynching(E entity) {
		if (!entity.level().isClientSide()) {
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
			// System.out.println("Beginning bone information collection...");
			if (this.getBoneInfoBuilder().isPresent()) {
				// Send packet
				// System.out.println("Sending bone information...");
				CPacketBoneInformation packet = this.getBoneInfoBuilder().get().build();
				packet.send();
				this.clearBoneInfoBuilder();
			} else {
				// System.out.println("creating new packet");
				CPacketBoneInformation.Builder builder = CPacketBoneInformation.builder(entity);
				this.setBoneInfoBuilderContent(builder);
			}
		}
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
