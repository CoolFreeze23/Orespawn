package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Coin;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * orig RenderCoin.java + ClientProxyOreSpawn.java:491:
 * {@code new RenderCoin(new ModelCoin(0.22f), 0.75f, 0.125f)} - the shadow
 * passed to RenderLiving is {@code 0.75f * 0.125f} and preRenderCallback
 * scales by 0.125 (BUG-040).
 */
public class CoinRenderer extends MobRenderer<Coin, ModelCoin> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/coin.png");
    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "coin"), "main");
    /** orig RenderCoin.scale = 0.125f (third constructor argument). */
    public static final float SCALE = 0.125F;
    /** orig RenderLiving shadow = 0.75f * 0.125f. */
    public static final float SHADOW = 0.75F * 0.125F;

    public CoinRenderer(EntityRendererProvider.Context context) {
        super(context, new ModelCoin(context.bakeLayer(MODEL_LAYER)), SHADOW);
    }

    @Override
    protected void scale(Coin entity, PoseStack poseStack, float partialTick) {
        // orig RenderCoin.preRenderCallback: GL11.glScalef(scale, scale, scale) - same pipeline
        // position as LivingEntityRenderer.scale (after the (-1,-1,1) flip, before the -1.501 lift).
        poseStack.scale(SCALE, SCALE, SCALE);
    }

    @Override
    public ResourceLocation getTextureLocation(Coin entity) {
        return TEXTURE;
    }
}
