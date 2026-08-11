package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.PitchBlack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

public class PitchBlackRenderer extends MobRenderer<PitchBlack, ModelPitchBlack> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/pitchblack.png");
    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "pitchblack"), "main");

    public PitchBlackRenderer(EntityRendererProvider.Context context) {
        super(context, new ModelPitchBlack(context.bakeLayer(MODEL_LAYER)), 2.0f);
    }

    @Override
    public void render(PitchBlack entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        // Scale the rendered Nightmare to match its discrete size tier so the
        // 1.7.10-faithful 5-stage size lottery is visible at a glance. The
        // hitbox is scaled in PitchBlack#getDefaultDimensions, so the visual
        // and physical sizes stay locked together.
        poseStack.pushPose();
        float scale = entity.getPitchBlackScale();
        poseStack.scale(scale, scale, scale);
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        poseStack.popPose();
    }

    // OPT-013: evaluated for replacement with a finite inflated cull box and
    // intentionally left as-is. The animated model envelope (GeckoLib/MHLib
    // bone-driven parts, code-model limb rotations) is not statically provable
    // from any constant in this codebase, and an under-sized box would visibly
    // pop the boss out at the screen edge — a behavior change. Keeping
    // unconditional true is the strictly-neutral choice.
    @Override
    public boolean shouldRender(PitchBlack entity, Frustum frustum, double x, double y, double z) {
        return true;
    }

    @Override
    public ResourceLocation getTextureLocation(PitchBlack entity) {
        return TEXTURE;
    }
}
