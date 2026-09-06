package de.dertoaster.multihitboxlib.mixin.entity;


import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.LinkedTransferQueue;
import java.util.function.BiConsumer;

import javax.annotation.Nullable;

import net.neoforged.neoforge.entity.PartEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import de.dertoaster.multihitboxlib.api.IMHLibFieldAccessor;
import de.dertoaster.multihitboxlib.api.IMultipartEntity;
import de.dertoaster.multihitboxlib.entity.MHLibPartEntity;
import de.dertoaster.multihitboxlib.entity.hitbox.HitboxProfile;
import de.dertoaster.multihitboxlib.network.client.CPacketBoneInformation;
import de.dertoaster.multihitboxlib.util.BoneInformation;
import de.dertoaster.multihitboxlib.util.RenderTickGate;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity implements IMultipartEntity<LivingEntity>, IMHLibFieldAccessor<LivingEntity> {

	@Unique
	public Map<String, MHLibPartEntity<LivingEntity>> partMap = new HashMap<>();
	@Unique
	public Map<String, BoneInformation> syncDataMap = new HashMap<>();
	
	@Unique
	private PartEntity<?>[] partArray;
	
	@Unique
	private Optional<CPacketBoneInformation.Builder> boneInformationBuilder = Optional.empty();
	
	@Unique
	private final Queue<UUID> trackerQueue = new LinkedTransferQueue<>();
	@Unique
	private int _mhlibTicksSinceLastSync = 0;
	
	@Override
	public int getTicksSinceLastSync() {
		return this._mhlibTicksSinceLastSync;
	}

	@Override
	public Queue<UUID> getTrackerQueue() {
		return this.trackerQueue;
	}
	
	@Unique
	@Nullable
	private UUID masterUUID = null;

	// ──────────────────────────────────────────────────────────────────
	// OPT-001: per-entity hitbox-profile cache. INTENTIONALLY NO FIELD
	// INITIALIZERS: mixin field initializers are merged into the
	// LivingEntity constructor body, but getHitboxProfile() can already
	// run during the Entity super-constructor (EntityEvent.Size fires
	// from Entity#<init>) and populate these fields — an initializer
	// would then clobber that first write. JVM defaults (null / 0) are
	// the "unset" state; a null cachedHitboxProfile always forces a
	// resolve. Invalidation: the stored generation is checked against
	// MHLibDatapackLoaders.getProfileCacheGeneration(), which is bumped
	// on every datapack reload (/reload, server start) and server stop.
	// ──────────────────────────────────────────────────────────────────
	@Unique
	@Nullable
	private Optional<HitboxProfile> mhlibCachedHitboxProfile;
	@Unique
	private int mhlibCachedHitboxProfileGeneration;

	// ──────────────────────────────────────────────────────────────────
	// OPT-003 (ruled 2026-08-11): client-side change-only bone-streaming
	// state (see IMultipartEntity.updateSynching for the throttle and its
	// invalidation story: self-invalidating field-by-field diff, 8-tick
	// keepalive under the server's 10-tick master timeout, nulled on
	// mastership change). INTENTIONALLY NO FIELD INITIALIZERS — same
	// mixin-constructor-merge rationale as the OPT-001 fields above; the
	// JVM defaults (null / 0) are the "nothing sent yet" state, and a
	// null last-sent map always forces the next built payload out.
	// ──────────────────────────────────────────────────────────────────
	@Unique
	@Nullable
	private Map<String, BoneInformation> mhlibLastSentBoneInformation;
	@Unique
	private int mhlibTicksSinceLastBoneInfoSend;

	// ──────────────────────────────────────────────────────────────────
	// BUG-044 (ruled 2026-09-04): per-entity render-tick stamp (see
	// IMHLibFieldAccessor). Initialised to RenderTickGate.UNSTAMPED (-1);
	// the initializer is merged into the LivingEntity constructor, which
	// is safe here because nothing reads the stamp before the first client
	// render pass (unlike the OPT-001/OPT-003 fields above).
	// ──────────────────────────────────────────────────────────────────
	@Unique
	private int mhlibRenderTickStamp = RenderTickGate.UNSTAMPED;

	// ──────────────────────────────────────────────────────────────────
	// OPT-013 / MHLib harvest 3 (2026-09-06): conservative cull bounds (see
	// IMHLibFieldAccessor). INTENTIONALLY NO FIELD INITIALIZERS: the radius
	// is written by mhlibOnConstructor at the LivingEntity constructor's
	// TAIL, after any merged initializer would run, and the JVM defaults
	// (0.0 / null) are the "no profile" / "not yet ticked" states anyway.
	// ──────────────────────────────────────────────────────────────────
	@Unique
	private double mhlibCullRadius;
	@Unique
	@Nullable
	private AABB mhlibCullBox;

	public MixinLivingEntity(EntityType<?> pEntityType, Level pLevel) {
		super(pEntityType, pLevel);
	}
	
	@Inject(
			method = "<init>(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/Level;)V",
			at = @At("TAIL")
			)
	private void mixinConstructor(CallbackInfo ci) {
		this.mhlibOnConstructor();
	}
	
	/*@Inject(
			method = "hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z",
			at = @At("HEAD"),
			cancellable = true
	)
	private void mixinHurt(DamageSource pSource, float pAmount, CallbackInfoReturnable<Boolean> cir) {
		if(!(this.HITBOX_PROFILE != null && this.HITBOX_PROFILE.isPresent())) {
			return;
		}
		if (pSource.is(DamageTypes.OUT_OF_WORLD)) {
			return;
		}
		
		if (pSource.isCreativePlayer()) {
			//return;
		}
		
		if (this.HITBOX_PROFILE != null && this.HITBOX_PROFILE.isPresent() && !this.HITBOX_PROFILE.get().mainHitboxConfig().canReceiveDamage()) {
			if (!this.hurtFromPart) {
				cir.setReturnValue(false);
				cir.cancel();
			}
		}
	}*/
	
	// After ticking all parts => call alignment code
	// Bind to interface to potentially cancel auto ticking and alignment
	@Inject(
			method = "aiStep",
			at = @At("TAIL")
	)
	private void mixinAiStep(CallbackInfo ci) {
		if (!this.isMultipartEntity()) {
			return;
		}

		this.mhlibAiStep();
	}
	
	// In tick method => intercept and tick the subparts
	@Inject(
			method = "tick",
			at = @At("TAIL")
	)
	private void mixinTick(CallbackInfo ci) {
		if(!this.isMultipartEntity()) {
			return;
		}
		
		this.tickParts(this.partMap.values());
	}

	/*
	 * TODO: Rewrite Packets for new Packet system
	 */
	@Override
	public synchronized boolean tryAddBoneInformation(String boneName, boolean hidden, Vec3 position, Vec3 scaling, Vec3 rotation) {
		return IMultipartEntity.super.tryAddBoneInformation(boneName, hidden, position, scaling, rotation);
    }
	
	// Before the constructor gets called => intercept the entityType and modify it's "size" argument to use the mainHitboxSize

	// Intercept defineSynchedData and add our master data

	// Intercept object creation, then create the hitbox profile

	// Add special hurt method => in interface => used for when subparts where damaged

	// Call this after the object has been created => method in interface, needs to be cancellable

	// Override setID to also set the id of the parts
	@Override
	public void setId(int pId) {
		// Attention: First call super, then MHLib!
		super.setId(pId);
		this.mhlibSetID(pId);
	}

	@Override
	public boolean isMultipartEntity() {
		return super.isMultipartEntity() || !this.partMap.values().isEmpty();
	}
	
	@Override
	@Nullable
	public PartEntity<?>[] getParts() {
		return this.mhLibGetParts();
	}

	// Also make sure to modify the result of isMultipartEntity to be correct

	@Inject(
			method = "isPickable()Z",
			at = @At("RETURN"),
			cancellable = true
	)
	private void mixinIsPickable(CallbackInfoReturnable<Boolean> cir) {
		cir.setReturnValue(this.mhLibIsPickable(cir.getReturnValue()));
	}

	// OPT-013 / MHLib harvest 3 (2026-09-06): the frustum box of a profiled entity is the box the CLIENT's
	// tickParts (and a gait-fed species' post-mirror re-cache) cached -- the body box, the position +- the
	// scaled rest-pose reach, and the parts' live boxes, unioned -- returned WHOLESALE: for a profiled entity
	// the value LivingEntity's body computed (its dragon-head inflate, and beneath it Entity's, where OreSpawn's
	// client EntityCullingMixin adds the oversized-weapon inflate) is discarded (refuter A, 2026-09-06: no
	// profiled species poses a dragon head or holds a weapon today; recorded, not handled). Every other
	// LivingEntity, and every entity on the server (nothing is cached there -- getBoundingBoxForCulling is a
	// client call), gets `original` untouched. A MixinExtras return modifier rather than the evaluation's
	// merged override: no CallbackInfoReturnable per entity per frame, LivingEntity's body kept (a merged
	// method would have been an implicit overwrite of it), and the injection is covered by the config's
	// defaultRequire and pinned to its two return sites (the dragon-head branch and the super call) by allow = expect = 2 (require is a MINIMUM: an
	// over-match applies silently -- refuter A, 2026-09-06). Design after MoreHitboxes'
	// EntityMixin.changeCullBox; no code taken.
	@ModifyReturnValue(
			method = "getBoundingBoxForCulling()Lnet/minecraft/world/phys/AABB;",
			at = @At("RETURN"),
			allow = 2,
			expect = 2
	)
	private AABB mixinGetBoundingBoxForCulling(AABB original) {
		final AABB cached = this.mhlibCullBox;
		return cached != null ? cached : original;
	}

	// MHLib access stuff
	@Override
	public PartEntity<?>[] _mhlibAccess_getPartArray() {
		return this.partArray;
	}

	@Override
	public void _mhlibAccess_setPartArray(final PartEntity<?>[] value) {
		this.partArray = value;
	}

	@Override
	public Queue<UUID> _mhlibAccess_getTrackerQueue() {
		return this.trackerQueue;
	}

	@Override
	public int _mhlibAccess_getTicksSinceLastSynch() {
		return this._mhlibTicksSinceLastSync;
	}

	@Override
	public void _mhlibAccess_setTicksSinceLastSynch(int value) {
		this._mhlibTicksSinceLastSync = value;
	}

	@Override
	public Map<String, MHLibPartEntity<LivingEntity>> _mhlibAccess_getPartMap() {
		return this.partMap;
	}

	@Override
	public void _mhlibAccess_setPartMap(Map<String, MHLibPartEntity<LivingEntity>> value) {
		this.partMap = value;
	}

	@Override
	public Map<String, BoneInformation> _mhlibAccess_getSynchMap() {
		return this.syncDataMap;
	}

	@Override
	public UUID _mhlibAccess_getMasterUUID() {
		return this.masterUUID;
	}

	@Override
	public void _mhlibAccess_setMasterUUID(UUID value) {
		this.masterUUID = value;
	}

	@Override
	public Optional<CPacketBoneInformation.Builder> _mlibAccess_getBoneInfoBuilder() {
		return this.boneInformationBuilder;
	}

	@Override
	public void _mlibAccess_setBoneInfoBuilder(Optional<CPacketBoneInformation.Builder> value) {
		this.boneInformationBuilder = value;
	}

	// OPT-001: per-entity profile cache accessors (see field comment above).
	@Override
	@Nullable
	public Optional<HitboxProfile> _mhlibAccess_getCachedHitboxProfile() {
		return this.mhlibCachedHitboxProfile;
	}

	@Override
	public int _mhlibAccess_getCachedHitboxProfileGeneration() {
		return this.mhlibCachedHitboxProfileGeneration;
	}

	@Override
	public void _mhlibAccess_setCachedHitboxProfile(Optional<HitboxProfile> profile, int generation) {
		this.mhlibCachedHitboxProfile = profile;
		this.mhlibCachedHitboxProfileGeneration = generation;
	}

	// OPT-003: change-only bone-streaming state accessors (see field comment above).
	@Override
	@Nullable
	public Map<String, BoneInformation> _mhlibAccess_getLastSentBoneInformation() {
		return this.mhlibLastSentBoneInformation;
	}

	@Override
	public void _mhlibAccess_setLastSentBoneInformation(@Nullable Map<String, BoneInformation> value) {
		this.mhlibLastSentBoneInformation = value;
	}

	@Override
	public int _mhlibAccess_getTicksSinceLastBoneInfoSend() {
		return this.mhlibTicksSinceLastBoneInfoSend;
	}

	@Override
	public void _mhlibAccess_setTicksSinceLastBoneInfoSend(int value) {
		this.mhlibTicksSinceLastBoneInfoSend = value;
	}

	// BUG-044: per-entity render-tick stamp accessors (see the field comment above).
	@Override
	public int _mhlibAccess_getRenderTickStamp() {
		return this.mhlibRenderTickStamp;
	}

	@Override
	public void _mhlibAccess_setRenderTickStamp(int value) {
		this.mhlibRenderTickStamp = value;
	}

	// OPT-013 / harvest 3: cull-bounds accessors (see the field comment above).
	@Override
	public double _mhlibAccess_getCullRadius() {
		return this.mhlibCullRadius;
	}

	@Override
	public void _mhlibAccess_setCullRadius(double value) {
		this.mhlibCullRadius = value;
	}

	@Override
	@Nullable
	public AABB _mhlibAccess_getCullBox() {
		return this.mhlibCullBox;
	}

	@Override
	public void _mhlibAccess_setCullBox(@Nullable AABB value) {
		this.mhlibCullBox = value;
	}

}
