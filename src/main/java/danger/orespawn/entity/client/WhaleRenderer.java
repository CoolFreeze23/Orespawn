package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Whale;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

/**
 * orig RenderWhale.java + ClientProxyOreSpawn.java:484:
 * {@code new RenderWhale(new ModelWhale(), 0.1f, 1.0f)}: RenderLiving shadow = par2 * par3
 * (RenderWhale.java:23), this.scale = par3 (:24), and preRenderScale (:39-45, wired through
 * func_77041_b at :47-49) draws children at scale / 2 (ENT-S-092).
 */
public class WhaleRenderer extends MobRenderer<Whale, ModelWhale> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/whale.png");

    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "whale"), "main");

    /** orig RenderWhale.scale = 1.0f (third constructor argument, ClientProxyOreSpawn.java:484). */
    private static final float SCALE = 1.0f;
    /** orig RenderLiving shadow = 0.1f * 1.0f (RenderWhale.java:23). */
    public static final float SHADOW = 0.1F * 1.0F;

    public WhaleRenderer(EntityRendererProvider.Context context) {
        super(context, new ModelWhale(context.bakeLayer(MODEL_LAYER)), SHADOW);
    }

    @Override
    public void render(Whale entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        if (entity.isBaby()) {
            poseStack.scale(SCALE / 2.0f, SCALE / 2.0f, SCALE / 2.0f);
        } else {
            poseStack.scale(SCALE, SCALE, SCALE);
        }
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(Whale entity) {
        return TEXTURE;
    }
}
