package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.BandP;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * orig RenderBandP.java + ClientProxyOreSpawn.java:504:
 * {@code new RenderBandP(new ModelBandP(0.4f), 1.0f, 1.0f)}: RenderLiving shadow = par2 * par3
 * (RenderBandP.java:23), this.scale = par3 (:24) and preRenderScale (:39-40, wired through
 * func_77041_b at :43-44) scales by 1.0 = identity (ENT-S-092). The ModelBandP(0.4f) argument is
 * wingspeed only, not a scale.
 */
public class BandPRenderer extends MobRenderer<BandP, ModelBandP> {
    private static final ResourceLocation TEXTURE_BANDIT =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/bandp_bandit.png");
    private static final ResourceLocation TEXTURE_PIRATE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/bandp_pirate.png");

    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "bandp"), "main");

    /** orig RenderBandP.scale = 1.0f (third constructor argument, ClientProxyOreSpawn.java:504). */
    public static final float SCALE = 1.0F;
    /** orig RenderLiving shadow = 1.0f * 1.0f (RenderBandP.java:23). */
    public static final float SHADOW = 1.0F * 1.0F;

    public BandPRenderer(EntityRendererProvider.Context context) {
        super(context, new ModelBandP(context.bakeLayer(MODEL_LAYER)), SHADOW);
    }

    @Override
    public ResourceLocation getTextureLocation(BandP entity) {
        return entity.getWhat() == 0 ? TEXTURE_BANDIT : TEXTURE_PIRATE;
    }
}
