package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Beaver;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

/**
 * orig RenderBeaver.java + ClientProxyOreSpawn.java:475:
 * {@code new RenderBeaver(new ModelBeaver(0.5f), 0.15f, 0.75f)} - RenderLiving
 * shadow = par2 * par3 (RenderBeaver.java:23) and preRenderCallback scales by
 * par3 = 0.75, or par3 / 2 for a child (RenderBeaver.java:24,39-49) (ENT-S-092).
 */
public class BeaverRenderer extends MobRenderer<Beaver, ModelBeaver> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/beaver.png");

    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "beaver"), "main");

    /** orig RenderLiving shadow = 0.15f * 0.75f (ClientProxyOreSpawn.java:475, RenderBeaver.java:23). */
    public static final float SHADOW = 0.15F * 0.75F;
    /** orig RenderBeaver.scale = 0.75f (third constructor argument, RenderBeaver.java:24). */
    public static final float SCALE = 0.75F;

    public BeaverRenderer(EntityRendererProvider.Context context) {
        super(context, new ModelBeaver(context.bakeLayer(MODEL_LAYER)), SHADOW);
    }

    @Override
    protected void scale(Beaver entity, PoseStack poseStack, float partialTick) {
        // orig RenderBeaver.preRenderScale (RenderBeaver.java:39-45): if (isChild) glScalef(scale / 2.0f)
        // else glScalef(scale) - same pipeline position as LivingEntityRenderer.scale (after the
        // (-1,-1,1) flip, before the -1.501 lift).
        if (entity.isBaby()) {
            poseStack.scale(SCALE / 2.0F, SCALE / 2.0F, SCALE / 2.0F);
            return;
        }
        poseStack.scale(SCALE, SCALE, SCALE);
    }

    @Override
    public ResourceLocation getTextureLocation(Beaver entity) {
        return TEXTURE;
    }
}
