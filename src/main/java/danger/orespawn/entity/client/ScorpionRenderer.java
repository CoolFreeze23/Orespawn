package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityScorpion;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

/**
 * orig ClientProxyOreSpawn.java:433
 * {@code new RenderScorpion(new ModelScorpion(0.62f), 0.35f, 0.75f)}: RenderScorpion.java:22-25
 * passes {@code par2 * par3} to RenderLiving as the shadow and keeps {@code par3} as the
 * preRenderCallback scale (ENT-S-092). The 0.62f model argument is wingspeed, not a size.
 */
public class ScorpionRenderer extends MobRenderer<EntityScorpion, ScorpionModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/scorpion.png");
    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "scorpion"), "main");
    /** orig RenderScorpion.scale = 0.75f (third constructor argument). */
    public static final float SCALE = 0.75F;
    /** orig RenderLiving shadow = 0.35f * 0.75f. */
    public static final float SHADOW = 0.35F * 0.75F;

    public ScorpionRenderer(EntityRendererProvider.Context context) {
        super(context, new ScorpionModel(context.bakeLayer(MODEL_LAYER)), SHADOW);
    }

    @Override
    protected void scale(EntityScorpion entity, PoseStack poseStack, float partialTick) {
        // orig RenderScorpion.preRenderCallback (func_77041_b -> preRenderScale, RenderScorpion.java:39-45):
        // GL11.glScalef(scale, scale, scale) - same pipeline position as LivingEntityRenderer.scale
        // (after the (-1,-1,1) flip, before the -1.501 lift).
        poseStack.scale(SCALE, SCALE, SCALE);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityScorpion entity) {
        return TEXTURE;
    }
}
