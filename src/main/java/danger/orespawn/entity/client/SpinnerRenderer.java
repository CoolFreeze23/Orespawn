package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemDisplayContext;

/**
 * The original's spinning projectiles (ENT-S-189): RenderSpinner (RenderShoe, RenderCage, RenderItemUrchin) and
 * RenderThrownRock drew the projectile's item as a quad facing the camera, turned about the view axis by the entity's
 * pitch ({@code glRotatef(rotationPitch, 0, 0, 1)} after the billboard, RenderSpinner.func_77026_a), and every one of
 * those entities turns its pitch each tick: the shoes and the cage 20 degrees, the thrown rock, the sunspot urchin, the
 * water ball and the ink sack 30, the laser ball and the ice ball, the acid and the dead Irukandji after it, 50. The
 * ports keep that turn in their x rotation; vanilla's ThrownItemRenderer, which drew them before, never reads it. The
 * rest is ThrownItemRenderer's: the item at the ground transform, not drawn in its first two ticks near the camera.
 */
public class SpinnerRenderer<T extends Entity & ItemSupplier> extends EntityRenderer<T> {
    private final ItemRenderer itemRenderer;

    public SpinnerRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(T entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer,
                       int packedLight) {
        if (entity.tickCount >= 2 || !(this.entityRenderDispatcher.camera.getEntity().distanceToSqr(entity) < 12.25)) {
            poseStack.pushPose();
            poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
            // the original read the pitch itself, not between ticks: its entities set the last tick's to the same
            poseStack.mulPose(Axis.ZP.rotationDegrees(entity.getXRot()));
            this.itemRenderer.renderStatic(entity.getItem(), ItemDisplayContext.GROUND, packedLight, OverlayTexture.NO_OVERLAY,
                    poseStack, buffer, entity.level(), entity.getId());
            poseStack.popPose();
            super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
        }
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
