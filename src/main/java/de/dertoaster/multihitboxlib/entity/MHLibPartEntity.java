package de.dertoaster.multihitboxlib.entity;

import java.util.Optional;

import de.dertoaster.multihitboxlib.api.IMHLibSizeCallback;
import de.dertoaster.multihitboxlib.api.IMultipartEntity;
import de.dertoaster.multihitboxlib.entity.hitbox.HitboxProfile;
import de.dertoaster.multihitboxlib.entity.hitbox.SubPartConfig;
import de.dertoaster.multihitboxlib.network.server.SPacketUpdateMultipart;
import de.dertoaster.multihitboxlib.util.BoneInformation;
import de.dertoaster.multihitboxlib.util.LazyLoadField;
import de.dertoaster.multihitboxlib.util.MHLibCounters;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.PartEntity;

//TODO: Refactor most of this into a abstract base class, base MHLibPartEntity is a AABB
public class MHLibPartEntity<T extends Entity> extends PartEntity<T> {

	private final SubPartConfig config;
	private EntityDimensions baseSize = EntityDimensions.fixed(1, 1);
	public static final EntityDimensions FALLBACK_SIZE = EntityDimensions.fixed(1, 1);

	protected int newPosRotationIncrements;
	protected double interpTargetX;
	protected double interpTargetY;
	protected double interpTargetZ;
	protected double interpTargetYaw;
	protected double interpTargetPitch;
	public float renderYawOffset;
	public float prevRenderYawOffset;

	public int deathTime;
	public int hurtTime;
	
	private boolean enabled = true;
	
	private final LazyLoadField<Boolean> isSynched = new LazyLoadField<>(this::isSynched, 5000);

	// ──────────────────────────────────────────────────────────────────
	// OPT-019: synched-bone flags precomputed at part construction from
	// the profile that created this part (createSubPartsFromProfile).
	// Invalidation story: none needed — a part only exists as long as its
	// parent entity, the parent's profile is fixed for the entity's
	// lifetime (per-entity cache in IMultipartEntity.getHitboxProfile +
	// immutable per-instance registry content), and the part layout
	// itself is already baked from that same profile at construction. A
	// datapack reload can only affect NEWLY constructed entities, whose
	// parts are precomputed from the new profile. null = not precomputed
	// (dynamic ICustomHitboxProfileSupplier parents) -> live lookup.
	// ──────────────────────────────────────────────────────────────────
	private Boolean precomputedListedAsSynchedBone = null;
	private Boolean precomputedIsSynched = null;

	private Optional<Tuple<Float, Float>> currentSizeModifier = Optional.empty();
	
	protected final Vec3 basePos;
	protected final Vec3 pivot;

	public MHLibPartEntity(T parent, final SubPartConfig properties, final EntityDimensions baseSize, final Vec3 basePosition, Vec3 pivot) {
		super(parent);
		this.config = properties;
		//this.baseSize = EntityDimensions.scalable(this.config.baseSize().x, this.config.baseSize().y);
		this.baseSize = baseSize;
		this.basePos = basePosition;
		this.pivot = pivot;
	}
	
	public SubPartConfig getConfig() {
		return this.config;
	}

	public void setPositionAndRotationDirect(double x, double y, double z, float yaw, float pitch, int posRotationIncrements) {
		interpTargetX = x;
		interpTargetY = y;
		interpTargetZ = z;
		interpTargetYaw = yaw;
		interpTargetPitch = pitch;
		newPosRotationIncrements = posRotationIncrements;
	}

	@Override
	public void tick() {
		updateLastPos();
		super.tick();
		if (this.newPosRotationIncrements > 0) {
			double d0 = this.getX() + (this.interpTargetX - this.getX()) / (double) this.newPosRotationIncrements;
			double d2 = this.getY() + (this.interpTargetY - this.getY()) / (double) this.newPosRotationIncrements;
			double d4 = this.getZ() + (this.interpTargetZ - this.getZ()) / (double) this.newPosRotationIncrements;
			double d6 = Mth.wrapDegrees(this.interpTargetYaw - (double) this.getYRot());
			this.setYRot((float) ((double) this.getYRot() + d6 / (double) this.newPosRotationIncrements));
			this.setXRot((float) ((double) this.getXRot() + (this.interpTargetPitch - (double) this.getXRot()) / (double) this.newPosRotationIncrements));
			--this.newPosRotationIncrements;
			this.setPos(d0, d2, d4);
			this.setRot(this.getYRot(), this.getXRot());
		} else if (this.newPosRotationIncrements == 0) {
			this.setPos(this.interpTargetX, this.interpTargetY, this.interpTargetZ);
			this.setYRot((float) this.interpTargetYaw);
			this.setXRot((float) this.interpTargetPitch);
			this.setRot(this.getYRot(), this.getXRot());
		}
		if (this.newPosRotationIncrements == 0) {
			this.newPosRotationIncrements = -1;
		}

		while (this.getYRot() - yRotO < -180F)
			yRotO -= 360F;
		while (this.getYRot() - yRotO >= 180F)
			yRotO += 360F;

		while (renderYawOffset - prevRenderYawOffset < -180F)
			prevRenderYawOffset -= 360F;
		while (renderYawOffset - prevRenderYawOffset >= 180F)
			prevRenderYawOffset += 360F;

		while (this.getXRot() - xRotO < -180F)
			xRotO -= 360F;
		while (this.getXRot() - xRotO >= 180F)
			xRotO += 360F;
	}
	
	/**
	 * OPT-019: seeds the precomputed synched flags from the profile this
	 * part was created from. Called once per part by
	 * {@code IMultipartEntity.createSubPartsFromProfile}; see the field
	 * comment for why no later invalidation is required.
	 */
	public void mhlibPrecomputeSynchedFlags(final HitboxProfile profile) {
		final boolean listed = profile.synchedBones().contains(this.getConfigName());
		this.precomputedListedAsSynchedBone = listed;
		// Mirrors isSynched(): profile present (it created us) && syncToModel && listed.
		this.precomputedIsSynched = profile.syncToModel() && listed;
	}

	/**
	 * OPT-019: constant-time replacement for the per-part-per-tick
	 * "profile.isPresent() && profile.synchedBones().contains(name)" check
	 * in {@code alignSubParts} (which did a registry lookup plus a linear
	 * list scan). Falls back to the bit-identical live check when the
	 * flags were not precomputed.
	 */
	public final boolean mhlibIsListedAsSynchedBone() {
		final Boolean precomputed = this.precomputedListedAsSynchedBone;
		if (precomputed != null) {
			return precomputed;
		}
		if (this.getParent() instanceof IMultipartEntity<?> ime) {
			final Optional<HitboxProfile> profile = ime.getHitboxProfile();
			return profile.isPresent() && profile.get().synchedBones().contains(this.getConfigName());
		}
		return false;
	}

	public final boolean isSynched() {
		// OPT-019: precomputed fast path (see field comment for why this
		// cannot go stale); legacy live lookup otherwise.
		final Boolean precomputed = this.precomputedIsSynched;
		if (precomputed != null) {
			return precomputed;
		}
		if (this.getParent() instanceof IMultipartEntity<?> ime) {
			// OPT-001: hoisted — one profile fetch instead of three.
			final Optional<HitboxProfile> profile = ime.getHitboxProfile();
			if (!profile.isPresent()) {
				return false;
			}
			if (!profile.get().syncToModel()) {
				return false;
			}
			if (profile.get().synchedBones().contains(this.getConfigName())) {
				return true;
			}
		}
		return false;
	}

	public SPacketUpdateMultipart.PartDataHolder writeData() {
		return new SPacketUpdateMultipart.PartDataHolder(
				this.getX(),
				this.getY(),
				this.getZ(),
				this.getYRot(),
				this.getXRot(),
				this.baseSize.width(),
				this.baseSize.height(),
				this.baseSize.fixed(),
				getEntityData().isDirty(),
				getEntityData().isDirty() ? getEntityData().packDirty() : null);

	}

	/**
	 * OPT-002: allocation-free change probe for the multipart update
	 * broadcast (MixinServerEntity). Returns true only when a
	 * {@link #writeData()} call right now would produce a payload
	 * bit-identical to {@code last}: every field compared here is exactly
	 * the field writeData captures, compared with primitive equality (no
	 * epsilon — NaN or any real difference reports "changed" and forces a
	 * send). The entity-data dirty probe uses {@code isDirty()} only, so
	 * this check has no side effects ({@code packDirty()} is left to the
	 * actual send path).
	 */
	public final boolean mhlibDataUnchangedSince(final SPacketUpdateMultipart.PartDataHolder last) {
		if (this.getEntityData().isDirty()) {
			return false;
		}
		return last.x() == this.getX()
				&& last.y() == this.getY()
				&& last.z() == this.getZ()
				&& last.yRot() == this.getYRot()
				&& last.xRot() == this.getXRot()
				&& last.width() == this.baseSize.width()
				&& last.height() == this.baseSize.height()
				&& last.fixed() == this.baseSize.fixed();
	}

	public void readData(SPacketUpdateMultipart.PartDataHolder data) {
		int updateSteps = 3;
		if (this.getParent() instanceof IMultipartEntity<?> ime) {
			// OPT-001: hoisted — one profile fetch instead of up to four
			// per received packet; the value is stable within a call.
			final Optional<HitboxProfile> profile = ime.getHitboxProfile();
			updateSteps = profile.isPresent() ? profile.get().partUpdateSteps() : updateSteps;
			if (this.isSynched.get()) {
				updateSteps = profile.isPresent() ? profile.get().synchedPartUpdateSteps() : updateSteps;
			}
		}
		
		this.setPositionAndRotationDirect(data.x(), data.y(), data.z(), data.yRot(), data.xRot(), Math.max(updateSteps, 0));
		final float w = data.width();
		final float h = data.height();
		this.baseSize = (data.fixed() ? EntityDimensions.fixed(w, h) : EntityDimensions.scalable(w, h));
		this.refreshDimensions();
		if (data.dirty())
			getEntityData().assignValues(data.data());
	}

	public final void updateLastPos() {
		this.setPos(getX(), getY(), getZ());
		yRotO = this.getYRot();
		xRotO = this.getXRot();
		this.tickCount++;
	}

	@Override
	public void setPos(double pX, double pY, double pZ) {
		super.setPosRaw(pX, pY, pZ);
		this.setOldPosAndRot();

		this.setBoundingBox(this.getDimensions(Pose.STANDING).makeBoundingBox(pX, pY, pZ));
		// recalculates the scaling
		this.getDimensions(Pose.STANDING);
	}

	public Vec3 getConfigPositionOffset() {
		return this.basePos;
	}
	
	public String getConfigName() {
		return this.config.name();
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {

	}

	@Override
	protected void readAdditionalSaveData(CompoundTag pCompound) {

	}

	@Override
	protected void addAdditionalSaveData(CompoundTag pCompound) {

	}

	public boolean hasCustomRenderer() {
		return false;
	}

	@Override
	public boolean isInvisible() {
		// Return true, otherwise the hitbox renders twice for whatever reason
		return true;
	}

	@Override
	public boolean canBeCollidedWith() {
		return this.config.collidable();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public boolean hurt(DamageSource pSource, float pAmount) {
		if (!this.config.canReceiveDamage()) {
			return false;
		}
		
		if (this.isInvulnerableTo(pSource)) {
			return false;
		}
		
		if (!this.isPartEnabled()) {
			return false;
		}
		
		pAmount *= this.config.damageModifier();
		if (this.getParent() instanceof IMultipartEntity<?> ime && ime != null) {
			return ((IMultipartEntity) ime).hurt(this, pSource, pAmount);
		} else {
			return super.hurt(pSource, pAmount);
		}
	}

	// TODO: Find replacement, seems to be removed though
	/*@Override
	protected AABB getBoundingBoxForPose(Pose pPose) {
		return this.getBoundingBox();
	}*/

	@Override
	public EntityDimensions getDimensions(Pose pPose) {
		if (this.baseSize == null) {
			return FALLBACK_SIZE;
		}
		if (this.currentSizeModifier != null && this.currentSizeModifier.isPresent()) {
			return this.baseSize.scale(this.currentSizeModifier.get().getA(), this.currentSizeModifier.get().getB());
		}
		return this.baseSize;
	}

	public boolean is(Entity pEntity) {
		return this == pEntity || this.getParent() == pEntity;
	}

	@Override
	public boolean isPickable() {
		// ──────────────────────────────────────────────────────────────
		// 2.0 S4 (vendored change): pickability must not require hard
		// collision. Upstream tied isPickable to collidable alone, which
		// silently excluded every non-collidable part from melee crosshair
		// picking (GameRenderer.pick), ALL projectiles (canBeHitByProjectile
		// = isAlive && isPickable) and ray traces — making
		// can-receive-damage:true unreachable for such parts. A part is now
		// pickable when it is a collider OR a damage surface. Queen-neutral:
		// all her parts are collidable:true (unchanged truth table); the
		// spider's non-collidable damage-surface legs become hittable, which
		// is the whole point (S4 pick-path law test pins this).
		// ──────────────────────────────────────────────────────────────
		return (this.config.collidable() || this.config.canReceiveDamage()) && this.isPartEnabled();
	}

	@Override
	public InteractionResult interact(Player player, InteractionHand hand) {
		// ──────────────────────────────────────────────────────────────
		// 2.0 S6a (vendored change; sitting finding F-1, owner-ratified):
		// parts forward INTERACTIONS to the parent's full interact chain,
		// the same way S4 forwards damage. Without this, every pickable
		// part is an interaction-dead surface: vanilla routes the click
		// packet to the part (getEntityOrPart), Entity.interact returns
		// PASS, and the click dies — a modern robot had FEWER working
		// mount-click angles than classic, whose rays passed through
		// legless air to the body box. Forwarding through the PARENT'S
		// interact() (not mobInteract directly) preserves the whole
		// vanilla chain (leads, spawn eggs, then mobInteract). Ratified
		// as a deliberate better-than-classic delta: legs become
		// clickable mount surfaces. Neutrality: TheQueen has no
		// mobInteract, so her part clicks route to the same vanilla
		// default her body clicks always had; King/Godzilla parts are
		// OreSpawnPartEntity, not this class — untouched.
		// ──────────────────────────────────────────────────────────────
		return this.getParent().interact(player, hand);
	}

	public void setHidden(boolean hidden) {
		this.enabled = !hidden;
	}

	public boolean isPartEnabled() {
		return this.enabled;
	}

	public void setScaling(Vec3 scale) {
		this.setScaling((float)scale.x(), (float)scale.y());
	}

	/**
	 * OPT-019: value-based variant of {@link #setScaling(Vec3)} that skips
	 * the Tuple + Optional allocation when the scale is unchanged (the
	 * common case: alignSubParts re-applies a constant entity scale every
	 * tick). Neutral: {@code getDimensions} only reads getA()/getB(), so
	 * reusing a tuple holding the exact same float values is
	 * indistinguishable; the float casts match the legacy
	 * {@code new Tuple<>((float)scale.x(), (float)scale.y())} path, and a
	 * NaN never compares equal, so it can only re-store, never wrongly
	 * reuse. Nothing mutates the stored tuple (setA/setB are unused).
	 */
	public void setScaling(float horizontal, float vertical) {
		if (this.currentSizeModifier != null && this.currentSizeModifier.isPresent()) {
			final Tuple<Float, Float> current = this.currentSizeModifier.get();
			if (current.getA() == horizontal && current.getB() == vertical) {
				return;
			}
		}
		this.currentSizeModifier = Optional.of(new Tuple<Float, Float>(horizontal, vertical));
	}

	public Vec3 getPivot() {
		return this.pivot;
	}

	public void applyInformation(Vec3 worldPos, Vec3 scale, Vec3 rotation, boolean hidden) {
		if (MHLibCounters.ENABLED && this.level().isClientSide()) {
			MHLibCounters.CLIENT_APPLY_INFORMATION.increment();
		}
		Vec3 pivot = this.getPivot();
		if (pivot != Vec3.ZERO) {
			pivot = pivot.xRot((float) (rotation.x())).yRot((float) (rotation.y())).zRot((float) (rotation.z()));
		}
		if (this.getParent() instanceof IMHLibSizeCallback sc) {
			pivot = pivot.scale(sc.mhlibGetEntitySizeScale(this.getParent()));
		}
		this.setScaling(scale);
		// Subtract pivot from worldpos so we are at the correct position
		// keep in mind that the pivot was rotated before to match the given rotation!
		this.setPos(worldPos.subtract(pivot));
		this.setXRot((float) (rotation.x()));
		this.setYRot((float) (rotation.y()));
		this.setHidden(hidden);
	}

	public void applyInformation(BoneInformation bi) {
		applyInformation(bi.worldPos(), bi.scale(), bi.rotation(), bi.hidden());
	}

	public void setEnabled(boolean value) {
		this.enabled = value;
	}
}
