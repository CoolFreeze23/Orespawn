package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityStinkBug;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

public class StinkBugRenderer extends MobRenderer<EntityStinkBug, StinkBugModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/stinkbug.png");
    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "stinkbug"), "main");

    /** orig ClientProxyOreSpawn.java:453 new RenderStinkBug(new ModelStinkBug(0.75f), 0.35f, 0.85f): RenderLiving shadow = par2 * par3 (ENT-S-092). */
    public static final float SHADOW = 0.2975F;
    /** orig preRenderCallback scale = par3 (ENT-S-092). */
    public static final float SCALE = 0.85F;

    public StinkBugRenderer(EntityRendererProvider.Context context) {
        super(context, new StinkBugModel(context.bakeLayer(MODEL_LAYER)), SHADOW);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityStinkBug entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(EntityStinkBug entity, PoseStack poseStack, float partialTick) {
        // orig preRenderScale: GL11.glScalef(scale, scale, scale), the LivingEntityRenderer.scale slot
        poseStack.scale(SCALE, SCALE, SCALE);
    }
}
