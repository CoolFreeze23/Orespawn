package danger.orespawn.entity.client;

import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EnderKnight;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * orig RenderEnderKnight.java + ClientProxyOreSpawn.java:473:
 * {@code new RenderEnderKnight(new ModelEnderKnight(0.21f), 0.3f, 1.0f)}:
 * RenderLiving shadow = par2 * par3 (RenderEnderKnight.java:23), this.scale = par3
 * (:24), and preRenderScale (:39-41, wired through func_77041_b at :43-45) scales
 * by 1.0 (ENT-S-092). The ModelEnderKnight(0.21f) argument is wingspeed only, not a scale.
 */
public class EnderKnightRenderer extends MobRenderer<EnderKnight, ModelEnderKnight> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/enderknight.png");

    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "ender_knight"), "main");

    /** orig RenderEnderKnight.scale = 1.0f (third constructor argument, ClientProxyOreSpawn.java:473). */
    public static final float SCALE = 1.0F;
    /** orig RenderLiving shadow = 0.3f * 1.0f (RenderEnderKnight.java:23). */
    public static final float SHADOW = 0.3F * 1.0F;

    public EnderKnightRenderer(EntityRendererProvider.Context context) {
        super(context, new ModelEnderKnight(context.bakeLayer(MODEL_LAYER)), SHADOW);
    }

    @Override
    public ResourceLocation getTextureLocation(EnderKnight entity) {
        return TEXTURE;
    }
}
