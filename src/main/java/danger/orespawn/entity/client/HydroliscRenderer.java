package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityHydrolisc;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * orig RenderHydrolisc.java + ClientProxyOreSpawn.java:422:
 * {@code new RenderHydrolisc(new ModelHydrolisc(0.65f), 0.65f, 0.65f)} - RenderLiving shadow = par2 * par3
 * (RenderHydrolisc.java:23) and preRenderScale scales by par3 = 0.65, or par3 / 2 when the entity
 * is a child (RenderHydrolisc.java:24,39-49). The ModelHydrolisc(0.65f) argument is only wingspeed,
 * not a size (ENT-S-092).
 */
public class HydroliscRenderer extends MobRenderer<EntityHydrolisc, HydroliscModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/hydrolisc.png");
    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "hydrolisc"), "main");
    /** orig RenderHydrolisc.scale = 0.65f (third constructor argument, ClientProxyOreSpawn.java:422). */
    public static final float SCALE = 0.65F;
    /** orig RenderLiving shadow = 0.65f * 0.65f (RenderHydrolisc.java:23). */
    public static final float SHADOW = 0.65F * 0.65F;

    public HydroliscRenderer(EntityRendererProvider.Context context) {
        super(context, new HydroliscModel(context.bakeLayer(MODEL_LAYER)), SHADOW);
    }

    @Override
    protected void scale(EntityHydrolisc entity, PoseStack poseStack, float partialTick) {
        // orig RenderHydrolisc.preRenderScale (RenderHydrolisc.java:39-45): a child (func_70631_g_ = isChild)
        // gets GL11.glScalef(scale / 2.0f, ...), otherwise GL11.glScalef(scale, scale, scale) - same pipeline
        // position as LivingEntityRenderer.scale (after the (-1,-1,1) flip, before the -1.501 lift).
        if (entity != null && entity.isBaby()) {
            poseStack.scale(SCALE / 2.0F, SCALE / 2.0F, SCALE / 2.0F);
            return;
        }
        poseStack.scale(SCALE, SCALE, SCALE);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityHydrolisc entity) {
        return TEXTURE;
    }
}
