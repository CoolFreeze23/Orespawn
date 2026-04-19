package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EnchantedAppleCow;
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
 * Renders the Enchanted Apple Cow with the {@code enchanted_apple_cow.png}
 * body texture plus the vanilla enchantment glint shimmer (an inner
 * {@link EnchantGlintLayer} re-renders the model through
 * {@link RenderType#entityGlintDirect()}) so the entity reads as the
 * "enchanted" tier in the apple-cow ladder at a glance — same visual
 * language as the spawn egg's {@code isFoil = true} hover effect.
 */
public class EnchantedAppleCowRenderer extends MobRenderer<EnchantedAppleCow, CowModel<EnchantedAppleCow>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/enchanted_apple_cow.png");

    public EnchantedAppleCowRenderer(EntityRendererProvider.Context context) {
        super(context, new CowModel<>(context.bakeLayer(ModelLayers.COW)), 0.7f);
        this.addLayer(new EnchantGlintLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(EnchantedAppleCow entity) {
        return TEXTURE;
    }

    private static class EnchantGlintLayer extends RenderLayer<EnchantedAppleCow, CowModel<EnchantedAppleCow>> {
        public EnchantGlintLayer(RenderLayerParent<EnchantedAppleCow, CowModel<EnchantedAppleCow>> parent) {
            super(parent);
        }

        @Override
        public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                           EnchantedAppleCow entity, float limbSwing, float limbSwingAmount,
                           float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
            VertexConsumer glintConsumer = buffer.getBuffer(RenderType.entityGlintDirect());
            this.getParentModel().renderToBuffer(poseStack, glintConsumer, packedLight,
                    OverlayTexture.NO_OVERLAY, -1);
        }
    }
}
