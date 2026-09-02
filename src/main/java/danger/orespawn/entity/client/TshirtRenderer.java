package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityTshirt;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class TshirtRenderer extends MobRenderer<EntityTshirt, TshirtModel<EntityTshirt>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/tshirt.png");
    public static final ModelLayerLocation MODEL_LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "tshirt"), "main");

    /** orig ClientProxyOreSpawn.java:418 new RenderTshirt(new ModelTshirt(0.22f), 1.0f, 0.33f): RenderLiving shadow = par2 * par3 (ENT-S-092). */
    public static final float SHADOW = 1.0F * 0.33F;
    /** orig preRenderCallback scale = par3 (ENT-S-092). */
    public static final float SCALE = 0.33F;

    public TshirtRenderer(EntityRendererProvider.Context context) {
        super(context, new TshirtModel<>(context.bakeLayer(MODEL_LAYER)), SHADOW);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityTshirt entity) { return TEXTURE; }

    @Override
    protected void scale(EntityTshirt entity, PoseStack poseStack, float partialTick) {
        // orig RenderTshirt.preRenderScale: GL11.glScalef(scale, scale, scale)
        poseStack.scale(SCALE, SCALE, SCALE);
    }
}
