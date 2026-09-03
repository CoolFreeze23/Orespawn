package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.WaterDragon;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

/**
 * orig RenderWaterDragon.java + ClientProxyOreSpawn.java:436:
 * {@code new RenderWaterDragon(new ModelWaterDragon(0.5f), 0.85f, 1.1f)}:
 * RenderLiving shadow = par2 * par3 (RenderWaterDragon.java:23), this.scale = par3
 * (:24), and preRenderScale (:39-45, wired through func_77041_b at :47-49) draws
 * children at scale / 2 (ENT-S-092). The ModelWaterDragon(0.5f) argument is
 * wingspeed only, not a scale.
 */
public class WaterDragonRenderer extends MobRenderer<WaterDragon, ModelWaterDragon> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/waterdragon.png");

    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "waterdragon"), "main");

    /** orig RenderWaterDragon.scale = 1.1f (third constructor argument). */
    public static final float SCALE = 1.1F;
    /** orig RenderLiving shadow = 0.85f * 1.1f. */
    public static final float SHADOW = 0.85F * 1.1F;

    public WaterDragonRenderer(EntityRendererProvider.Context context) {
        super(context, new ModelWaterDragon(context.bakeLayer(MODEL_LAYER)), SHADOW);
    }

    @Override
    protected void scale(WaterDragon entity, PoseStack poseStack, float partialTick) {
        // orig RenderWaterDragon.preRenderScale (RenderWaterDragon.java:39-45):
        //   if (par1Entity != null && par1Entity.isChild()) { GL11.glScalef(scale / 2.0f, ...); return; }
        //   GL11.glScalef(scale, scale, scale);
        // Same pipeline position as LivingEntityRenderer.scale (after the (-1,-1,1) flip,
        // before the -1.501 lift). The entity is never null here.
        float s = entity.isBaby() ? SCALE / 2.0F : SCALE;
        poseStack.scale(s, s, s);
    }

    @Override
    public ResourceLocation getTextureLocation(WaterDragon entity) {
        return TEXTURE;
    }
}
