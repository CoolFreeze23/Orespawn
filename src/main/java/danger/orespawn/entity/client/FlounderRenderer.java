package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Flounder;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

/**
 * orig RenderFlounder.java + ClientProxyOreSpawn.java:483:
 * {@code new RenderFlounder(new ModelFlounder(), 0.1f, 1.0f)}:
 * RenderLiving shadow = par2 * par3 (RenderFlounder.java:23), this.scale = par3
 * (:24), and preRenderScale (:39-45, wired through func_77041_b at :47-49) draws
 * children at scale / 2 (ENT-S-092).
 */
public class FlounderRenderer extends MobRenderer<Flounder, ModelFlounder> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/flounder.png");

    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "flounder"), "main");

    /** orig RenderFlounder.scale = 1.0f (third constructor argument). */
    public static final float SCALE = 1.0F;
    /** orig RenderLiving shadow = 0.1f * 1.0f. */
    public static final float SHADOW = 0.1F * 1.0F;

    public FlounderRenderer(EntityRendererProvider.Context context) {
        super(context, new ModelFlounder(context.bakeLayer(MODEL_LAYER)), SHADOW);
    }

    @Override
    protected void scale(Flounder entity, PoseStack poseStack, float partialTick) {
        // orig RenderFlounder.preRenderScale (RenderFlounder.java:39-45):
        //   if (par1Entity != null && par1Entity.isChild()) { GL11.glScalef(scale / 2.0f, ...); return; }
        //   GL11.glScalef(scale, scale, scale);
        // Same pipeline position as LivingEntityRenderer.scale (after the (-1,-1,1) flip,
        // before the -1.501 lift). The entity is never null here.
        float s = entity.isBaby() ? SCALE / 2.0F : SCALE;
        poseStack.scale(s, s, s);
    }

    @Override
    public ResourceLocation getTextureLocation(Flounder entity) {
        return TEXTURE;
    }
}
