package danger.orespawn.entity.client;

import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.RockBase;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * orig RenderRockBase.java + ClientProxyOreSpawn.java:505:
 * {@code new RenderRockBase(new ModelRockBase(1.0f), 0.0f, 1.0f)}: RenderLiving shadow = par2 * par3
 * (RenderRockBase.java:34) = 0.0, so 1.7.10 drew no shadow at all; this.scale = par3 (:35) and
 * preRenderScale (:50-51, wired through func_77041_b at :54-55) scales by 1.0 = identity (ENT-S-092).
 */
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

    /** orig RenderRockBase.scale = 1.0f (third constructor argument, ClientProxyOreSpawn.java:505). */
    public static final float SCALE = 1.0F;
    /** orig RenderLiving shadow = 0.0f * 1.0f (RenderRockBase.java:34): no shadow drawn. */
    public static final float SHADOW = 0.0F * 1.0F;

    public RockBaseRenderer(EntityRendererProvider.Context context) {
        super(context, new ModelRockBase(context.bakeLayer(MODEL_LAYER)), SHADOW);
    }

    @Override
    public ResourceLocation getTextureLocation(RockBase entity) {
        return textureFor(entity.getRockType());
    }

    /** The 1.7.10 per-type texture table, shared with the GeckoLib candidate so both renderers read one source. */
    public static ResourceLocation textureFor(int rt) {
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
