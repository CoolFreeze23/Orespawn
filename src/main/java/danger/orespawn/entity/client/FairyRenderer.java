package danger.orespawn.entity.client;

import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Fairy;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

public class FairyRenderer extends MobRenderer<Fairy, FairyModel> {
    private static final ResourceLocation[] TEXTURES = {
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/fairytexture.png"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/fairytexture2.png"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/fairytexture3.png"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/fairytexture4.png"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/fairytexture5.png"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/fairytexture6.png"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/fairytexture7.png"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/fairytexture8.png"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/fairytexture9.png"),
    };
    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "fairy"), "main");

    public FairyRenderer(EntityRendererProvider.Context context) {
        super(context, new FairyModel(context.bakeLayer(MODEL_LAYER)), 0.15f);
    }

    @Override
    public ResourceLocation getTextureLocation(Fairy entity) {
        int type = entity.getFairyType();
        if (type >= 0 && type < TEXTURES.length) return TEXTURES[type];
        return TEXTURES[0];
    }

    // Fairy sprites use gradient wing alpha; the cutout pipeline would
    // chop those pixels off. Translucent keeps the fade-out intact and
    // matches the 1.7.10 fairy aesthetic.
    @Override
    public RenderType getRenderType(Fairy entity, boolean visible, boolean visibleToPlayer, boolean glowing) {
        return RenderType.entityTranslucent(this.getTextureLocation(entity));
    }
}
