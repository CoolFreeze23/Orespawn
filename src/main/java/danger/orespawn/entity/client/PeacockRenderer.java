package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Peacock;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * orig RenderPeacock.java + ClientProxyOreSpawn.java:478:
 * {@code new RenderPeacock(new ModelPeacock(0.75f), 0.25f, 1.0f)}:
 * RenderLiving shadow = par2 * par3 (RenderPeacock.java:23), this.scale = par3
 * (:24), and preRenderScale (:39-45, wired through func_77041_b at :47-49) draws
 * children at scale / 2 (ENT-S-092). The ModelPeacock(0.75f) argument is a model
 * parameter, not a scale.
 */
public class PeacockRenderer extends MobRenderer<Peacock, ModelPeacock> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/peacock.png");

    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "peacock"), "main");

    /** orig RenderPeacock.scale = 1.0f (third constructor argument, ClientProxyOreSpawn.java:478). */
    public static final float SCALE = 1.0F;
    /** orig RenderLiving shadow = 0.25f * 1.0f (RenderPeacock.java:23). */
    public static final float SHADOW = 0.25F * 1.0F;

    public PeacockRenderer(EntityRendererProvider.Context context) {
        super(context, new ModelPeacock(context.bakeLayer(MODEL_LAYER)), SHADOW);
    }

    @Override
    public void render(Peacock entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        if (entity.isBaby()) {
            poseStack.scale(0.5f, 0.5f, 0.5f);
        }
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(Peacock entity) {
        return TEXTURE;
    }
}
