package de.dertoaster.multihitboxlib.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import de.dertoaster.multihitboxlib.api.IMHLibFieldAccessor;
import de.dertoaster.multihitboxlib.api.IMultipartEntity;
import de.dertoaster.multihitboxlib.entity.MHLibPartEntity;
import de.dertoaster.multihitboxlib.entity.hitbox.HitboxProfile;
import de.dertoaster.multihitboxlib.util.MHLibCounters;
import de.dertoaster.multihitboxlib.util.RenderTickGate;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

public interface IBoneInformationCollectorLayerCommonLogic<T extends Object> {

	/**
	 * Model-bone collection only does anything for a profile that names at
	 * least one bone and either syncs bones to the server or trusts the client
	 * to place its own parts. Solver-fed profiles (sync-with-model=false,
	 * trust-client=false, empty list) must be a strict no-op.
	 */
	public static boolean shouldCollectModelBones(
			boolean syncToModel, boolean trustClient, List<String> synchedBones) {
		return (syncToModel || trustClient) && synchedBones != null && !synchedBones.isEmpty();
	}

	public static boolean shouldCollectModelBones(HitboxProfile profile) {
		return profile != null
				&& shouldCollectModelBones(profile.syncToModel(), profile.trustClient(), profile.synchedBones());
	}

	public static boolean shouldCollectModelBones(Entity entity) {
		if (entity == null || !entity.isMultipartEntity()
				|| !(entity instanceof IMultipartEntity<?> multipartEntity)) {
			return false;
		}

		Optional<HitboxProfile> profile = multipartEntity.getHitboxProfile();
		return profile.isPresent() && shouldCollectModelBones(profile.get());
	}
	
	// ──────────────────────────────────────────────────────────────────
	// BUG-044 (ruled 2026-09-04): the once-per-game-tick collection gate
	// is a PER-ENTITY render-tick stamp (IMHLibFieldAccessor
	// _mhlibAccess_get/setRenderTickStamp, RenderTickGate.UNSTAMPED until
	// the first collecting pass), decided ONCE per render pass in
	// onPreRender(Entity) and advanced in onPostRender(Entity); onRenderBone
	// only reads the decision. The former per-LAYER `currentTick` (one
	// layer per renderer instance, advanced to tickCount + 1 only on
	// equality) wedged for good on a frame during which the entity ticked
	// twice, and with two entities sharing a renderer followed whichever
	// entity last matched and starved the other. Gate rule:
	// util.RenderTickGate (stamp < tickCount).
	// ──────────────────────────────────────────────────────────────────
	// Portions derived from MoreHitboxes by DarkPred (https://github.com/DarkPred/MoreHitboxes, commit 88899b3), MIT License — see LICENSE-MoreHitboxes.txt

	/** Whether the render pass in flight collects bone information for the entity being drawn (set by {@link #beginRenderPass}). */
	public boolean isCollectingPass();
	public void setCollectingPass(boolean collecting);

	/**
	 * Decides the pass from the entity's render-tick stamp and tick count and keeps the decision on
	 * the layer for every {@link #onRenderBone} of this pass. Pure in its inputs, so the headless
	 * {@code QueenPartPlacementProbe} drives it with fake entities.
	 */
	public default boolean beginRenderPass(int renderTickStamp, int tickCount) {
		final boolean collecting = RenderTickGate.shouldCollect(renderTickStamp, tickCount);
		this.setCollectingPass(collecting);
		if (MHLibCounters.ENABLED && collecting) {
			MHLibCounters.CLIENT_COLLECTING_PASSES.increment();
		}
		return collecting;
	}

	/**
	 * Ends the pass and returns the stamp the entity carries afterwards: advanced to the tick that
	 * collected when the pass collected, unchanged otherwise.
	 */
	public default int endRenderPass(int renderTickStamp, int tickCount) {
		final boolean collected = this.isCollectingPass();
		this.setCollectingPass(false);
		return collected ? RenderTickGate.advance(tickCount) : renderTickStamp;
	}
	
	public void calcScales(T bone);
	public void calcRotations(T bone);
	public String getBoneName(T bone);
	public Vec3 getBoneWorldPosition(T bone);
	public boolean isBoneHidden(T bone);

	public Vec3 getScaleVector();
	public void setScales(int x, int y, int z);
	
	public Vec3 getRotationVector();
	public void setRotations(int x, int y, int z);
	
	public default void onRenderBone(PoseStack poseStack, Entity entity, T bone, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
		if (MHLibCounters.ENABLED) {
			MHLibCounters.CLIENT_BONES_VISITED.increment();
		}
		// Only collect once per tick! (BUG-044: decided per pass from the entity's stamp, see onPreRender(Entity))
		if (entity != null && entity.isMultipartEntity() && entity instanceof IMultipartEntity<?> ime && ime.getHitboxProfile().isPresent()) {
			HitboxProfile hitboxProfile = ime.getHitboxProfile().get();
			// Replaced renderers still receive the vendored collector layer, but a
			// profile that neither syncs nor trusts client bones has nothing to
			// collect and must pay no bone/world-transform cost.
			if (!shouldCollectModelBones(hitboxProfile)) {
				return;
			}

			final Vec3 worldPos = this.getBoneWorldPosition(bone);
			this.calcScales(bone);
			this.calcRotations(bone);

			if (hitboxProfile.synchedBones().contains(this.getBoneName(bone))) {
				if (hitboxProfile.syncToModel()) {
					if (this.isCollectingPass()) {
						ime.tryAddBoneInformation(this.getBoneName(bone), this.isBoneHidden(bone), worldPos, this.getScaleVector(), this.getRotationVector());
						//System.out.println("RenderRecursively: " + worldPos.toString());
						//ime.getPartByName(bone.getName()).get().setPos(worldPos);
					}
				}
				// After we collected stuff, we set the position directly if we trust the client...
				// Unsafe but honestly, mixins are a thing. Nobody can stop anyone else from installing a clientside mod that moves all hitboxes out of place...
				if (hitboxProfile.trustClient()) {
					Optional<? extends MHLibPartEntity<?>> optPart = ime.getPartByName(this.getBoneName(bone));
					if (optPart.isPresent()) {
						MHLibPartEntity<?> part = optPart.get();
						part.applyInformation(worldPos, this.getScaleVector(), this.getRotationVector(), this.isBoneHidden(bone));
					}
				}
			}
		}
	}
	
	/**
	 * Once per rendered entity BEFORE its bones, from the Pre events of both the GeoEntity and the
	 * replaced-entity path (GeckolibEntityRenderEventHandler), keyed on the actual entity.
	 */
	public default void onPreRender(Entity animatable) {
		if (animatable instanceof LivingEntity le && le.isMultipartEntity() && animatable instanceof IMHLibFieldAccessor<?> access) {
			if (MHLibCounters.ENABLED) {
				MHLibCounters.CLIENT_FRAMES.increment();
			}
			this.beginRenderPass(access._mhlibAccess_getRenderTickStamp(), le.tickCount);
		} else {
			this.setCollectingPass(false);
		}
	}

	/**
	 * Once per rendered entity AFTER its bones (both paths): stores the advanced stamp on the entity
	 * when this pass collected, then resets the running scale and rotation.
	 */
	public default void onPostRender(Entity animatable) {
		if (!(animatable instanceof LivingEntity le)) {
			this.setCollectingPass(false);
			return;
		}

		if (le.isMultipartEntity() && animatable instanceof IMHLibFieldAccessor<?> access) {
			access._mhlibAccess_setRenderTickStamp(this.endRenderPass(access._mhlibAccess_getRenderTickStamp(), le.tickCount));
		} else {
			this.setCollectingPass(false);
		}

		this.setScales(1, 1, 1);
		this.setRotations(0, 0, 0);
	}
}
