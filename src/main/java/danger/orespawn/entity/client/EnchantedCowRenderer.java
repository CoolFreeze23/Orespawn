package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EnchantedCow;
import net.minecraft.client.model.CowModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/**
 * Renders the Enchanted Cow with the gold cow base texture plus an
 * enchantment glint overlay, mirroring the original 1.7.10 "enchanted
 * golden apple cow" visual style.
 */
public class EnchantedCowRenderer extends MobRenderer<EnchantedCow, CowModel<EnchantedCow>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/gold_cow.png");

    public EnchantedCowRenderer(EntityRendererProvider.Context context) {
        super(context, new CowModel<>(context.bakeLayer(ModelLayers.COW)), 0.7f);
        this.addLayer(new EnchantGlintLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(EnchantedCow entity) {
        return TEXTURE;
    }

    /**
     * Render layer that draws the enchantment glint shimmer over the cow model,
     * giving it the characteristic purple-tinted enchantment effect.
     */
    private static class EnchantGlintLayer extends RenderLayer<EnchantedCow, CowModel<EnchantedCow>> {
        public EnchantGlintLayer(RenderLayerParent<EnchantedCow, CowModel<EnchantedCow>> parent) {
            super(parent);
        }

        @Override
        public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                           EnchantedCow entity, float limbSwing, float limbSwingAmount,
                           float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
            VertexConsumer glintConsumer = buffer.getBuffer(RenderType.entityGlintDirect());
            this.getParentModel().renderToBuffer(poseStack, glintConsumer, packedLight,
                    OverlayTexture.NO_OVERLAY, -1);
        }
    }
}
