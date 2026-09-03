package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityLurkingTerror;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * orig RenderLurkingTerror.java + ClientProxyOreSpawn.java:461:
 * {@code new RenderLurkingTerror(new ModelLurkingTerror(), 0.45f, 0.85f)} - RenderLurkingTerror.java:23
 * passes {@code par2 * par3} to RenderLiving as the shadow and :24 keeps {@code scale = par3};
 * preRenderScale :39-41 scales by scale unconditionally (ENT-S-092).
 */
public class LurkingTerrorRenderer extends MobRenderer<EntityLurkingTerror, LurkingTerrorModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/lurkingterror.png");
    public static final ModelLayerLocation MODEL_LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "lurkingterror"), "main");
    /** orig RenderLurkingTerror.scale = 0.85f (third constructor argument). */
    public static final float SCALE = 0.85F;
    /** orig RenderLiving shadow = 0.45f * 0.85f. */
    public static final float SHADOW = 0.45F * 0.85F;

    public LurkingTerrorRenderer(EntityRendererProvider.Context context) {
        super(context, new LurkingTerrorModel(context.bakeLayer(MODEL_LAYER)), SHADOW);
    }

    @Override
    protected void scale(EntityLurkingTerror entity, PoseStack poseStack, float partialTick) {
        // orig RenderLurkingTerror.preRenderScale (:39-41, the preRenderCallback slot):
        // GL11.glScalef(scale, scale, scale) - same pipeline position as LivingEntityRenderer.scale
        // (after the (-1,-1,1) flip, before the -1.501 lift).
        poseStack.scale(SCALE, SCALE, SCALE);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityLurkingTerror entity) { return TEXTURE; }
}
