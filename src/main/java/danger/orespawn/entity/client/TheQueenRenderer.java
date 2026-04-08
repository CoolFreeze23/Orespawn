package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.TheQueen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;
public class TheQueenRenderer extends MobRenderer<TheQueen, ModelTheQueen> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/thequeentexture.png");
    private static final ResourceLocation TEXTURE_HAPPY =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/thequeentexture2.png");
    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "the_queen"), "main");
    private static final float SCALE = 3.0F;
    // Original GL: glColor4f(0.75, 0.75, 0.75, 0.55) packed as ARGB int
    private static final int WING_MEMBRANE_COLOR = (140 << 24) | (191 << 16) | (191 << 8) | 191;
    private static final int FULLBRIGHT = 0xF000F0;

    public TheQueenRenderer(EntityRendererProvider.Context context) {
        super(context, new ModelTheQueen(context.bakeLayer(MODEL_LAYER)), 3.0F);
    }

    @Override
    protected void scale(TheQueen entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(SCALE, SCALE, SCALE);
    }

    @Override
    public void render(TheQueen entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        // Pass 1: opaque parts via the standard MobRenderer pipeline
        // (calls model.renderToBuffer which only renders opaque geometry)
        super.render(entity, entityYaw, partialTicks, poseStack, bufferSource, packedLight);

        // Passes 2-4 share the same entity-space transform that super.render
        // already popped. We must rebuild it: translate to entity, scale, flip.
        ResourceLocation tex = getTextureLocation(entity);
        poseStack.pushPose();
        this.setupEntityTransform(entity, poseStack, entityYaw, partialTicks);
        ModelTheQueen mdl = this.getModel();

        // Pass 2: translucent wing membranes (alpha 0.55, tinted 0.75 gray)
        VertexConsumer translucentVC = bufferSource.getBuffer(RenderType.entityTranslucent(tex));
        mdl.renderWingMembranes(poseStack, translucentVC, packedLight,
                OverlayTexture.NO_OVERLAY, WING_MEMBRANE_COLOR);

        // Pass 3: fullbright power cubes
        VertexConsumer cubeVC = bufferSource.getBuffer(RenderType.entityCutoutNoCull(tex));
        mdl.renderPowerCubes(poseStack, cubeVC, FULLBRIGHT,
                OverlayTexture.NO_OVERLAY, -1, entity.getPower());

        // Pass 4: eyes (opaque, full brightness for glow effect)
        VertexConsumer eyeVC = bufferSource.getBuffer(RenderType.entityCutoutNoCull(tex));
        mdl.renderEyes(poseStack, eyeVC, FULLBRIGHT, OverlayTexture.NO_OVERLAY, -1);

        poseStack.popPose();
    }

    /**
     * Reproduces the entity-to-model-space transform that LivingEntityRenderer
     * applies inside its render() call: translate, rotate yaw, scale, and
     * flip the model upside-down (Minecraft models render Y-inverted).
     */
    private void setupEntityTransform(TheQueen entity, PoseStack poseStack,
                                      float entityYaw, float partialTicks) {
        float bodyYaw = net.minecraft.util.Mth.rotLerp(partialTicks,
                entity.yBodyRotO, entity.yBodyRot);
        poseStack.translate(0.0F, 1.501F, 0.0F);
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180.0F - bodyYaw));
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        this.scale(entity, poseStack, partialTicks);
        poseStack.translate(0.0F, -1.501F, 0.0F);
    }

    @Override
    public boolean shouldRender(TheQueen entity, Frustum frustum, double x, double y, double z) {
        return true;
    }

    @Override
    public ResourceLocation getTextureLocation(TheQueen entity) {
        return entity.isHappy() ? TEXTURE_HAPPY : TEXTURE;
    }
}