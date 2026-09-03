package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Dragon;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

/**
 * orig RenderDragon.java + ClientProxyOreSpawn.java:447:
 * {@code new RenderDragon(new ModelDragon(0.65f), 1.25f, 1.0f)}: RenderLiving shadow = par2 * par3
 * (RenderDragon.java:24), this.scale = par3 (:25), and preRenderScale (:40-42, via func_77041_b)
 * scales by 1.0 unconditionally (ENT-S-092). The ModelDragon(0.65f) argument is wingspeed only,
 * not a size.
 */
public class DragonRenderer extends MobRenderer<Dragon, ModelDragon> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/dragon.png");
    private static final ResourceLocation TEXTURE_WHITE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/white_dragon.png");

    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "dragon"), "main");

    /** orig RenderDragon.scale = 1.0f (third constructor argument, ClientProxyOreSpawn.java:447). */
    private static final float SCALE = 1.0f;
    /** orig RenderLiving shadow = 1.25f * 1.0f (RenderDragon.java:24). */
    public static final float SHADOW = 1.25F * 1.0F;

    public DragonRenderer(EntityRendererProvider.Context context) {
        super(context, new ModelDragon(context.bakeLayer(MODEL_LAYER)), SHADOW);
    }

    @Override
    public void render(Dragon entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.scale(SCALE, SCALE, SCALE);
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(Dragon entity) {
        if (entity.getDragonType() != 0) {
            return TEXTURE_WHITE;
        }
        return TEXTURE;
    }
}
