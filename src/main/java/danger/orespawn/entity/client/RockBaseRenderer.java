package danger.orespawn.entity.client;

import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.RockBase;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class RockBaseRenderer extends MobRenderer<RockBase, ModelRockBase> {
    private static final ResourceLocation TEXTURE_1 =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/rocktexture.png");
    private static final ResourceLocation TEXTURE_3 =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/rockredtexture.png");
    private static final ResourceLocation TEXTURE_4 =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/rockgreentexture.png");
    private static final ResourceLocation TEXTURE_5 =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/rockbluetexture.png");
    private static final ResourceLocation TEXTURE_6 =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/rockpurpletexture.png");
    private static final ResourceLocation TEXTURE_8 =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/rocktnttexture.png");
    private static final ResourceLocation TEXTURE_9 =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/rockcrystaltexture.png");
    private static final ResourceLocation TEXTURE_10 =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/rockcrystalgreentexture.png");
    private static final ResourceLocation TEXTURE_11 =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/rockcrystalbluetexture.png");
    private static final ResourceLocation TEXTURE_12 =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/rockcrystaltnttexture.png");

    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "rockbase"), "main");

    public RockBaseRenderer(EntityRendererProvider.Context context) {
        super(context, new ModelRockBase(context.bakeLayer(MODEL_LAYER)), 0.3f);
    }

    @Override
    public ResourceLocation getTextureLocation(RockBase entity) {
        int rt = entity.getRockType();
        return switch (rt) {
            case 3 -> TEXTURE_3;
            case 4 -> TEXTURE_4;
            case 5 -> TEXTURE_5;
            case 6 -> TEXTURE_6;
            case 8 -> TEXTURE_8;
            case 9 -> TEXTURE_9;
            case 10 -> TEXTURE_10;
            case 11 -> TEXTURE_11;
            case 12 -> TEXTURE_12;
            default -> TEXTURE_1;
        };
    }
}
