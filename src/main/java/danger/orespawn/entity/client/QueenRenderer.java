package danger.orespawn.entity.client;

import danger.orespawn.entity.TheQueen;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * Geckolib renderer for {@link TheQueen}. Replaces the legacy
 * four-pass {@code TheQueenRenderer} (opaque body / translucent
 * wing membranes / fullbright power cubes / fullbright eyes) with
 * a single Geckolib-driven render call.
 *
 * <p>The dynamic blue→red texture swap on phase-shift is owned by
 * {@link QueenModel#getTextureResource}, so this class only needs
 * to wire up the model + a generous shadow radius matching her
 * 16×12 hitbox.</p>
 *
 * <p>Frustum culling is forced off via {@link #shouldRender}: the
 * Queen's part-entity hitboxes can extend ~30 blocks from her
 * root AABB on a fully-extended wing or tail swing, and a missed
 * cull would let her invisibly clip through the player's screen
 * edge during the boss fight.</p>
 *
 * <h2>MultiHitBoxLib bone tracking</h2>
 *
 * <p>This class deliberately stays as plain {@link GeoEntityRenderer}
 * rather than extending an MHLib-specific subclass. MHLib's
 * {@code MixinGeoEntityRenderer} (priority MAX_VALUE) injects at the
 * TAIL of every {@code GeoEntityRenderer} constructor and calls
 * {@code addRenderLayer(new GeckolibBoneInformationCollectorLayer(self))},
 * so the bone-tracking layer is attached automatically once super(...)
 * returns. The same mixin also wraps {@code renderRecursively} HEAD/TAIL
 * to invoke the layer's {@code onRenderRecursivelyStart/End} hooks,
 * which read each synced bone's world position via
 * {@code GeoBone#getWorldPosition} and ship a {@code CPacketBoneInformation}
 * server-ward where {@code MHLibPartEntity} positions are reconciled.</p>
 *
 * <p>Net result: zero MHLib code in this file, full pixel-perfect
 * bone-tracked hitbox sync.</p>
 */
public class QueenRenderer extends GeoEntityRenderer<TheQueen> {

    public QueenRenderer(EntityRendererProvider.Context context) {
        super(context, new QueenModel());
        this.shadowRadius = 3.0f;
    }

    @Override
    public boolean shouldRender(TheQueen entity, Frustum frustum, double cameraX, double cameraY, double cameraZ) {
        return true;
    }

    /**
     * BOSS-017: orig RenderTheQueen.java:40-46 — a PlayNicely Queen renders
     * at scale/4, tracking the live synced flag. GeckoLib pathway: scale the
     * pose before the model renders; MHLib's bone-tracked hitbox parts
     * follow the scaled bones, so the nice Queen's hit surfaces shrink too.
     */
    @Override
    public void preRender(com.mojang.blaze3d.vertex.PoseStack poseStack, TheQueen animatable,
                          software.bernie.geckolib.cache.object.BakedGeoModel model,
                          net.minecraft.client.renderer.MultiBufferSource bufferSource,
                          com.mojang.blaze3d.vertex.VertexConsumer buffer, boolean isReRender,
                          float partialTick, int packedLight, int packedOverlay,
                          int colour) {
        if (animatable.getPlayNicely() != 0) {
            poseStack.scale(0.25f, 0.25f, 0.25f);
        }
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender,
                partialTick, packedLight, packedOverlay, colour);
    }
}
