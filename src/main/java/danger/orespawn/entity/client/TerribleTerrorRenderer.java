package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityTerribleTerror;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * orig RenderTerribleTerror.java + ClientProxyOreSpawn.java:457:
 * {@code new RenderTerribleTerror(new ModelTerribleTerror(), 0.45f, 0.75f)} - RenderTerribleTerror.java:23
 * passes {@code par2 * par3} = 0.45f * 0.75f to RenderLiving as the shadow and :24 keeps
 * {@code scale = par3} = 0.75f for preRenderScale (ENT-S-092).
 */
public class TerribleTerrorRenderer extends MobRenderer<EntityTerribleTerror, TerribleTerrorModel<EntityTerribleTerror>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/terribleterror.png");
    public static final ModelLayerLocation MODEL_LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "terribleterror"), "main");

    /** orig RenderTerribleTerror.scale = 0.75f (third constructor argument, ClientProxyOreSpawn.java:457). */
    public static final float SCALE = 0.75F;
    /** orig RenderLiving shadow = par2 * par3 = 0.45f * 0.75f (RenderTerribleTerror.java:23). */
    public static final float SHADOW = 0.45F * 0.75F;

    public TerribleTerrorRenderer(EntityRendererProvider.Context context) {
        super(context, new TerribleTerrorModel<>(context.bakeLayer(MODEL_LAYER)), SHADOW);
    }

    @Override
    protected void scale(EntityTerribleTerror entity, PoseStack poseStack, float partialTick) {
        // orig RenderTerribleTerror.preRenderScale (func_77041_b, RenderTerribleTerror.java:39-45):
        // GL11.glScalef(scale, scale, scale) unconditionally - same pipeline position as
        // LivingEntityRenderer.scale (after the (-1,-1,1) flip, before the -1.501 lift).
        poseStack.scale(SCALE, SCALE, SCALE);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityTerribleTerror entity) { return TEXTURE; }
}
