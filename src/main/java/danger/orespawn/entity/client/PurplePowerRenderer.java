package danger.orespawn.entity.client;

import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.PurplePower;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.core.BlockPos;
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
 * <p>
 * ENT-S-146 (2026-09-06): the orb draws translucent and fullbright as in 1.7.10. The blend state
 * and the colour are the model's ({@link ModelPurplePower}), the light is the renderer's
 * ({@link #getBlockLightLevel} / {@link #getSkyLightLevel} below).
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
        return textureFor(entity.getPurpleType());
    }

    /** The per-type texture table (orig RenderPurplePower.java:46 by getPurpleType), shared with the GeckoLib candidate so both renderers read one source. */
    public static ResourceLocation textureFor(int type) {
        return switch (type) {
            case 1 -> TEXTURE_2;
            case 2 -> TEXTURE_3;
            case 3 -> TEXTURE_4;
            case 10 -> TEXTURE_10;
            default -> TEXTURE;
        };
    }

    /**
     * ENT-S-146, orig ModelPurplePower.java:56 {@code OpenGlHelper.setLightmapTextureCoords(lightmapTexUnit, 240, 240)}:
     * the fullbright lightmap texel. In 1.21.1 the light is the renderer's, not the model's: the
     * dispatcher asks {@code EntityRenderer.getPackedLightCoords} (bytecode 0-24: {@code LightTexture
     * .pack(getBlockLightLevel(entity, pos), getSkyLightLevel(entity, pos))}) and hands the result to
     * {@code render}, which {@code LivingEntityRenderer.render} passes to {@code renderToBuffer} (606-621).
     * Both levels at {@link ModelPurplePower#LIGHT_LEVEL} pack to {@code 15 << 4 | 15 << 20} = 15728880 =
     * {@code LightTexture.FULL_BRIGHT}, whose lightmap texel is (240, 240) - orig's exact coordinates.
     * The {@code MagmaCubeRenderer} idiom ({@code getBlockLightLevel} returning 15, bytecode 0-2); the
     * GeckoLib candidate answers the same through its descriptor's {@code fullBright} hook. The render
     * type and the colour live in the model (orig :53-55; {@link ModelPurplePower#RENDER_TYPE},
     * {@link ModelPurplePower#COLOR}): vanilla {@code getRenderType}'s visible-body branch (17-30)
     * returns the model's function, so no override is needed here.
     */
    @Override
    protected int getBlockLightLevel(PurplePower entity, BlockPos pos) {
        return ModelPurplePower.LIGHT_LEVEL;
    }

    /** See {@link #getBlockLightLevel}: orig :56's second 240. */
    @Override
    protected int getSkyLightLevel(PurplePower entity, BlockPos pos) {
        return ModelPurplePower.LIGHT_LEVEL;
    }
}
