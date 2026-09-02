package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityMosquito;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

public class MosquitoRenderer extends MobRenderer<EntityMosquito, MosquitoModel<EntityMosquito>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/mosquito.png");
    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "mosquito"), "main");

    /** orig ClientProxyOreSpawn.java:408 new RenderMosquito(new ModelMosquito(), 0.3f, 0.5f): RenderLiving shadow = par2 * par3 (ENT-S-092). */
    public static final float SHADOW = 0.15F;
    /** orig preRenderCallback scale = par3 (ENT-S-092). */
    public static final float SCALE = 0.5F;

    public MosquitoRenderer(EntityRendererProvider.Context context) {
        super(context, new MosquitoModel<>(context.bakeLayer(MODEL_LAYER)), SHADOW);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityMosquito entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(EntityMosquito entity, PoseStack poseStack, float partialTick) {
        // orig preRenderScale: GL11.glScalef(scale, scale, scale), the LivingEntityRenderer.scale slot
        poseStack.scale(SCALE, SCALE, SCALE);
    }
}
