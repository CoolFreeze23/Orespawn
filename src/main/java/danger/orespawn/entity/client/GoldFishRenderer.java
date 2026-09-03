package danger.orespawn.entity.client;

import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.GoldFish;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

/**
 * orig RenderGoldFish.java + ClientProxyOreSpawn.java:470:
 * {@code new RenderGoldFish(new ModelGoldFish(0.7f), 0.2f, 1.0f)}:
 * RenderLiving shadow = par2 * par3 (RenderGoldFish.java:23), this.scale = par3
 * (:24), preRenderScale (:39-41, via func_77041_b :43-45) scales by 1.0 with no
 * child branch (ENT-S-092). The ModelGoldFish(0.7f) argument is wingspeed only,
 * not a scale.
 */
public class GoldFishRenderer extends MobRenderer<GoldFish, ModelGoldFish> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/goldfish.png");

    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "goldfish"), "main");

    /** orig RenderGoldFish.scale = 1.0f (third constructor argument, ClientProxyOreSpawn.java:470). */
    public static final float SCALE = 1.0F;
    /** orig RenderLiving shadow = 0.2f * 1.0f (RenderGoldFish.java:23). */
    public static final float SHADOW = 0.2F * 1.0F;

    public GoldFishRenderer(EntityRendererProvider.Context context) {
        super(context, new ModelGoldFish(context.bakeLayer(MODEL_LAYER)), SHADOW);
    }

    @Override
    public ResourceLocation getTextureLocation(GoldFish entity) {
        return TEXTURE;
    }
}
