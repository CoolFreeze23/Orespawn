package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.SeaMonster;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

/**
 * orig RenderSeaMonster.java + ClientProxyOreSpawn.java:496:
 * {@code new RenderSeaMonster(new ModelSeaMonster(0.5f), 1.0f, 1.0f)} - RenderLiving shadow = par2 * par3
 * (RenderSeaMonster.java:23) and preRenderScale scales by par3 = 1.0 (RenderSeaMonster.java:24,39-45).
 * The ModelSeaMonster(0.5f) argument is only wingspeed, not a size (ENT-S-092).
 */
public class SeaMonsterRenderer extends MobRenderer<SeaMonster, ModelSeaMonster> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/seamonster.png");

    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "seamonster"), "main");

    /** orig RenderSeaMonster.scale = 1.0f (third constructor argument, ClientProxyOreSpawn.java:496). */
    public static final float SCALE = 1.0F;
    /** orig RenderLiving shadow = 1.0f * 1.0f (RenderSeaMonster.java:23). */
    public static final float SHADOW = 1.0F * 1.0F;

    public SeaMonsterRenderer(EntityRendererProvider.Context context) {
        super(context, new ModelSeaMonster(context.bakeLayer(MODEL_LAYER)), SHADOW);
    }

    @Override
    protected void scale(SeaMonster entity, PoseStack poseStack, float partialTick) {
        // orig RenderSeaMonster.preRenderScale (RenderSeaMonster.java:39-41, via func_77041_b :43-45):
        // GL11.glScalef(scale, scale, scale), unconditional - same pipeline position as
        // LivingEntityRenderer.scale (after the (-1,-1,1) flip, before the -1.501 lift).
        poseStack.scale(SCALE, SCALE, SCALE);
    }

    // OPT-013: evaluated for replacement with a finite inflated cull box and
    // intentionally left as-is. The animated model envelope (GeckoLib/MHLib
    // bone-driven parts, code-model limb rotations) is not statically provable
    // from any constant in this codebase, and an under-sized box would visibly
    // pop the boss out at the screen edge — a behavior change. Keeping
    // unconditional true is the strictly-neutral choice.
    @Override
    public boolean shouldRender(SeaMonster entity, Frustum frustum, double x, double y, double z) {
        return true;
    }

    @Override
    public ResourceLocation getTextureLocation(SeaMonster entity) {
        return TEXTURE;
    }
}
