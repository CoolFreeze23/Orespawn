package danger.orespawn.entity.client;

import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityButterfly;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

public class ButterflyRenderer extends MobRenderer<EntityButterfly, ButterflyModel<EntityButterfly>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/butterfly.png");
    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "butterfly"), "main");

    public ButterflyRenderer(EntityRendererProvider.Context context) {
        // wingspeed 1.0f — orig ClientProxyOreSpawn.java:405 (new ModelButterfly(1.0f))
        super(context, new ButterflyModel<>(context.bakeLayer(MODEL_LAYER), 1.0f), 0.15f);
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
