package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.ThePrincess;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

public class ThePrincessRenderer extends MobRenderer<ThePrincess, ModelThePrincess> {

    /** orig RenderThePrincess.java:21 texture2, ThePrincesstexture2.png (theprincess2.png, byte-identical). */
    public static final ResourceLocation ATTACK_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/theprincess2.png");
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/theprincess.png");

    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "theprincess"), "main");

    /** orig RenderThePrincess.java:25 {@code this.scale = par3} = 0.7f (ClientProxyOreSpawn.java:511); public since the remainder slice (2026-09-15) so the GeckoLib descriptor reads the renderer's constant (the T2d form). */
    public static final float SCALE = 0.7f;

    public ThePrincessRenderer(EntityRendererProvider.Context context) {
        super(context, new ModelThePrincess(context.bakeLayer(MODEL_LAYER)), 0.7f * 0.7f);
        // the original's blended wing membranes (ModelThePrincess.java:470-487), a second pass after the opaque one
        this.addLayer(new WingMembraneLayer<>(this, ModelThePrincess::renderWingMembranes, ModelThePrincess.WING_MEMBRANE_RENDER_TYPE,
                ModelThePrincess.WING_MEMBRANE_COLOR));
    }

    @Override
    public void render(ThePrincess entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.scale(SCALE, SCALE, SCALE);
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(ThePrincess entity) {
        // orig RenderThePrincess.java:48-52: ThePrincesstexture2.png while she attacks
        return entity.getAttacking() != 0 ? ATTACK_TEXTURE : TEXTURE;
    }
}
