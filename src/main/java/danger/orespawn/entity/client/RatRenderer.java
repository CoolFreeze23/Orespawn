package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityRat;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * orig RenderRat.java + ClientProxyOreSpawn.java:482:
 * {@code new RenderRat(new ModelRat(1.0f), 0.1f, 0.75f)} - RenderRat.java:23 passes
 * {@code par2 * par3} = 0.1f * 0.75f to RenderLiving as the shadow and :24 keeps
 * {@code scale = par3} = 0.75f for preRenderScale (ENT-S-092). The 1.0f model argument is only
 * ModelRat.wingspeed, not a size factor.
 */
public class RatRenderer extends MobRenderer<EntityRat, RatModel<EntityRat>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/rat.png");
    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "rat"), "main");

    /** orig RenderRat.scale = 0.75f (third constructor argument, ClientProxyOreSpawn.java:482). */
    public static final float SCALE = 0.75F;
    /** orig RenderLiving shadow = par2 * par3 = 0.1f * 0.75f (RenderRat.java:23). */
    public static final float SHADOW = 0.1F * 0.75F;

    public RatRenderer(EntityRendererProvider.Context context) {
        super(context, new RatModel<>(context.bakeLayer(MODEL_LAYER)), SHADOW);
    }

    @Override
    protected void scale(EntityRat entity, PoseStack poseStack, float partialTick) {
        // orig RenderRat.preRenderScale (func_77041_b, RenderRat.java:39-45):
        // GL11.glScalef(scale, scale, scale) unconditionally - same pipeline position as
        // LivingEntityRenderer.scale (after the (-1,-1,1) flip, before the -1.501 lift).
        poseStack.scale(SCALE, SCALE, SCALE);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityRat entity) {
        return TEXTURE;
    }
}
