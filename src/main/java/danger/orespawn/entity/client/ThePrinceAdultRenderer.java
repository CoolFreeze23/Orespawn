package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.ThePrinceAdult;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

public class ThePrinceAdultRenderer extends MobRenderer<ThePrinceAdult, ModelThePrinceAdult> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/theprinceadult.png");

    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "theprinceadult"), "main");

    private static final float SCALE = 1.0f;

    public ThePrinceAdultRenderer(EntityRendererProvider.Context context) {
        super(context, new ModelThePrinceAdult(context.bakeLayer(MODEL_LAYER)), 1.2f);
        // the original's blended wing membranes (ModelThePrinceAdult.java:1184-1200), a second pass after the opaque one
        this.addLayer(new WingMembraneLayer<>(this, ModelThePrinceAdult::renderWingMembranes, ModelThePrinceAdult.WING_MEMBRANE_RENDER_TYPE,
                ModelThePrinceAdult.WING_MEMBRANE_COLOR));
    }

    @Override
    public void render(ThePrinceAdult entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.scale(SCALE, SCALE, SCALE);
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(ThePrinceAdult entity) {
        return TEXTURE;
    }
}
