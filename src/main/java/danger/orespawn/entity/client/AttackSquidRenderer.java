package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.AttackSquid;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

/**
 * orig RenderAttackSquid.java + ClientProxyOreSpawn.java:437:
 * {@code new RenderAttackSquid(new ModelAttackSquid(1.0f), 0.25f, 0.9f)} - RenderLiving shadow = par2 * par3
 * (RenderAttackSquid.java:23) and preRenderScale scales by 0.9 (RenderAttackSquid.java:39-41) (ENT-S-092).
 */
public class AttackSquidRenderer extends MobRenderer<AttackSquid, ModelAttackSquid> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/attacksquid.png");

    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "attacksquid"), "main");

    /** orig RenderAttackSquid.scale = 0.9f (third constructor argument, RenderAttackSquid.java:24). */
    public static final float SCALE = 0.9F;
    /** orig RenderLiving shadow = 0.25f * 0.9f (RenderAttackSquid.java:23). */
    public static final float SHADOW = 0.25F * 0.9F;

    public AttackSquidRenderer(EntityRendererProvider.Context context) {
        super(context, new ModelAttackSquid(context.bakeLayer(MODEL_LAYER)), SHADOW);
    }

    @Override
    protected void scale(AttackSquid entity, PoseStack poseStack, float partialTick) {
        // orig RenderAttackSquid.preRenderScale: GL11.glScalef(scale, scale, scale) - same pipeline
        // position as LivingEntityRenderer.scale (after the (-1,-1,1) flip, before the -1.501 lift).
        poseStack.scale(SCALE, SCALE, SCALE);
    }

    @Override
    public ResourceLocation getTextureLocation(AttackSquid entity) {
        return TEXTURE;
    }
}
