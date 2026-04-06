package danger.orespawn.entity.client;

import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityLunaMoth;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

public class LunaMothRenderer extends MobRenderer<EntityLunaMoth, ButterflyModel<EntityLunaMoth>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/lunamoth.png");
    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "luna_moth"), "main");

    public LunaMothRenderer(EntityRendererProvider.Context context) {
        super(context, new ButterflyModel<>(context.bakeLayer(MODEL_LAYER)), 0.15f);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityLunaMoth entity) {
        return switch (entity.moth_type) {
            case 1 -> ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/eyemoth.png");
            case 2 -> ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/darkmoth.png");
            case 3 -> ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/firemoth.png");
            default -> TEXTURE;
        };
    }
}
