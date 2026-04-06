package danger.orespawn.entity.client;

import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Cockateil;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class CockateilRenderer extends MobRenderer<Cockateil, ModelCockateil> {
    private static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/bird1.png"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/bird2.png"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/bird3.png"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/bird4.png"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/bird5.png"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/bird6.png")
    };

    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "cockateil"), "main");

    public CockateilRenderer(EntityRendererProvider.Context context) {
        super(context, new ModelCockateil(context.bakeLayer(MODEL_LAYER)), 0.3f);
    }

    @Override
    public ResourceLocation getTextureLocation(Cockateil entity) {
        int type = entity.getBirdType();
        if (type >= 0 && type < TEXTURES.length) {
            return TEXTURES[type];
        }
        return TEXTURES[0];
    }
}
