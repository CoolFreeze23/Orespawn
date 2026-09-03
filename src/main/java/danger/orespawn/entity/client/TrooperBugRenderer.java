package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityTrooperBug;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

/**
 * orig RenderTrooperBug.java + ClientProxyOreSpawn.java:451:
 * {@code new RenderTrooperBug(new ModelTrooperBug(0.22f), 0.95f, 1.1f)} - RenderTrooperBug.java:22-24
 * passes {@code par2 * par3} to RenderLiving as the shadow and keeps {@code scale = par3};
 * preRenderScale (RenderTrooperBug.java:39-45) scales by 1.1 unconditionally (ENT-S-092).
 */
public class TrooperBugRenderer extends MobRenderer<EntityTrooperBug, TrooperBugModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/trooperbug.png");
    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "trooperbug"), "main");
    /** orig RenderTrooperBug.scale = 1.1f (third constructor argument, ClientProxyOreSpawn.java:451). */
    public static final float SCALE = 1.1F;
    /** orig RenderLiving shadow = 0.95f * 1.1f (RenderTrooperBug.java:23, ClientProxyOreSpawn.java:451). */
    public static final float SHADOW = 0.95F * 1.1F;

    public TrooperBugRenderer(EntityRendererProvider.Context context) {
        super(context, new TrooperBugModel(context.bakeLayer(MODEL_LAYER)), SHADOW);
    }

    @Override
    protected void scale(EntityTrooperBug entity, PoseStack poseStack, float partialTick) {
        // orig RenderTrooperBug.preRenderCallback: GL11.glScalef(scale, scale, scale) - same pipeline
        // position as LivingEntityRenderer.scale (after the (-1,-1,1) flip, before the -1.501 lift).
        poseStack.scale(SCALE, SCALE, SCALE);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityTrooperBug entity) {
        return TEXTURE;
    }
}
