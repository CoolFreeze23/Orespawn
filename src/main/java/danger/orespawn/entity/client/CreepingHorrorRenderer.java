package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.CreepingHorror;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * orig RenderCreepingHorror.java + ClientProxyOreSpawn.java:456:
 * {@code new RenderCreepingHorror(new ModelCreepingHorror(), 0.45f, 0.75f)} - RenderCreepingHorror.java:23
 * passes {@code par2 * par3} = 0.45f * 0.75f to RenderLiving as the shadow and :24 keeps
 * {@code scale = par3} = 0.75f for preRenderScale (ENT-S-092).
 */
public class CreepingHorrorRenderer extends MobRenderer<CreepingHorror, ModelCreepingHorror> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/creepinghorror.png");

    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "creeping_horror"), "main");

    /** orig RenderCreepingHorror.scale = 0.75f (third constructor argument, ClientProxyOreSpawn.java:456). */
    public static final float SCALE = 0.75F;
    /** orig RenderLiving shadow = par2 * par3 = 0.45f * 0.75f (RenderCreepingHorror.java:23). */
    public static final float SHADOW = 0.45F * 0.75F;

    public CreepingHorrorRenderer(EntityRendererProvider.Context context) {
        super(context, new ModelCreepingHorror(context.bakeLayer(MODEL_LAYER)), SHADOW);
    }

    @Override
    protected void scale(CreepingHorror entity, PoseStack poseStack, float partialTick) {
        // orig RenderCreepingHorror.preRenderScale (func_77041_b, RenderCreepingHorror.java:39-45):
        // GL11.glScalef(scale, scale, scale) unconditionally - same pipeline position as
        // LivingEntityRenderer.scale (after the (-1,-1,1) flip, before the -1.501 lift).
        poseStack.scale(SCALE, SCALE, SCALE);
    }

    @Override
    public ResourceLocation getTextureLocation(CreepingHorror entity) {
        return TEXTURE;
    }
}
