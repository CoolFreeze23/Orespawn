package danger.orespawn.entity.client;

import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityButterfly;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

/**
 * orig RenderButterfly.java + ClientProxyOreSpawn.java:405:
 * {@code new RenderButterfly(new ModelButterfly(1.0f), 0.3f, 1.0f)} - RenderLiving shadow = par2 * par3
 * (RenderButterfly.java:25) and preRenderScale scales by par3 = 1.0 (RenderButterfly.java:26,41-47).
 * The ModelButterfly(1.0f) argument is only wingspeed, not a size (ENT-S-092).
 */
public class ButterflyRenderer extends MobRenderer<EntityButterfly, ButterflyModel<EntityButterfly>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/butterfly.png");
    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "butterfly"), "main");
    /** orig RenderButterfly.scale = 1.0f (third constructor argument, ClientProxyOreSpawn.java:405). */
    public static final float SCALE = 1.0F;
    /** orig RenderLiving shadow = 0.3f * 1.0f (RenderButterfly.java:25). */
    public static final float SHADOW = 0.3F * 1.0F;

    public ButterflyRenderer(EntityRendererProvider.Context context) {
        // wingspeed 1.0f — orig ClientProxyOreSpawn.java:405 (new ModelButterfly(1.0f))
        super(context, new ButterflyModel<>(context.bakeLayer(MODEL_LAYER), 1.0f), SHADOW);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityButterfly entity) {
        int type = entity.getButterflyType();
        return switch (type) {
            case 1 -> ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/butterfly2.png");
            case 2 -> ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/butterfly3.png");
            case 3 -> ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/butterfly4.png");
            default -> TEXTURE;
        };
    }
}
