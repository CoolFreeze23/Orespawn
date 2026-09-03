package danger.orespawn.entity.client;

import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.PurplePower;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * orig RenderPurplePower.java + ClientProxyOreSpawn.java:506:
 * {@code new RenderPurplePower(new ModelPurplePower(1.0f), 0.3f, 2.75f)}:
 * RenderLiving shadow = par2 * par3 (RenderPurplePower.java:26) (ENT-S-092).
 * <p>
 * The 2.75f third argument only ever reached the shadow: RenderPurplePower stores it
 * in this.scale (:27) and defines preRenderScale (:38-44: 2.75, or 0.55 when
 * getPurpleType() != 0), but never overrides func_77041_b (preRenderCallback) - its
 * only overrides are renderPurplePower (:30), func_76986_a (:34) and func_110775_a
 * (:46) - so vanilla RendererLivingEntity's empty preRenderCallback ran and every
 * purple type drew at world scale 1.0. The former render() override here that
 * shrank types != 0 to 0.55 transcribed that dead body and is removed.
 */
public class PurplePowerRenderer extends MobRenderer<PurplePower, ModelPurplePower> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/purplepowertexture.png");
    private static final ResourceLocation TEXTURE_2 =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/purplepowertexture2.png");
    private static final ResourceLocation TEXTURE_3 =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/purplepowertexture3.png");
    private static final ResourceLocation TEXTURE_4 =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/purplepowertexture4.png");
    private static final ResourceLocation TEXTURE_10 =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/purplepowertexture10.png");

    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "purplepower"), "main");

    /**
     * Effective orig world scale = 1.0 for every purple type: RenderPurplePower.preRenderScale
     * (:38-44, this.scale = 2.75f / 0.55f by type) is never wired to func_77041_b, so no
     * scale() override is needed and none is declared.
     */
    public static final float SCALE = 1.0F;
    /** orig RenderLiving shadow = 0.3f * 2.75f. */
    public static final float SHADOW = 0.3F * 2.75F;

    public PurplePowerRenderer(EntityRendererProvider.Context context) {
        super(context, new ModelPurplePower(context.bakeLayer(MODEL_LAYER)), SHADOW);
    }

    @Override
    public ResourceLocation getTextureLocation(PurplePower entity) {
        int type = entity.getPurpleType();
        return switch (type) {
            case 1 -> TEXTURE_2;
            case 2 -> TEXTURE_3;
            case 3 -> TEXTURE_4;
            case 10 -> TEXTURE_10;
            default -> TEXTURE;
        };
    }
}
