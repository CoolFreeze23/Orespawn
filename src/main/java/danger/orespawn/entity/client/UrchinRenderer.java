package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Urchin;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

/**
 * orig RenderUrchin.java + ClientProxyOreSpawn.java:487:
 * {@code new RenderUrchin(new ModelUrchin(1.0f), 0.35f, 1.25f)} - RenderUrchin.java:23 passes
 * {@code par2 * par3} to RenderLiving as the shadow and :24 keeps {@code scale = par3};
 * preRenderScale :39-41 scales by scale unconditionally (ENT-S-092).
 */
public class UrchinRenderer extends MobRenderer<Urchin, ModelUrchin> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/urchin.png");

    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "urchin"), "main");
    /** orig RenderUrchin.scale = 1.25f (third constructor argument). */
    public static final float SCALE = 1.25F;
    /** orig RenderLiving shadow = 0.35f * 1.25f. */
    public static final float SHADOW = 0.35F * 1.25F;

    public UrchinRenderer(EntityRendererProvider.Context context) {
        super(context, new ModelUrchin(context.bakeLayer(MODEL_LAYER)), SHADOW);
    }

    @Override
    protected void scale(Urchin entity, PoseStack poseStack, float partialTick) {
        // orig RenderUrchin.preRenderScale (:39-41, the preRenderCallback slot):
        // GL11.glScalef(scale, scale, scale) - same pipeline position as LivingEntityRenderer.scale
        // (after the (-1,-1,1) flip, before the -1.501 lift).
        poseStack.scale(SCALE, SCALE, SCALE);
    }

    @Override
    public ResourceLocation getTextureLocation(Urchin entity) {
        return TEXTURE;
    }
}
