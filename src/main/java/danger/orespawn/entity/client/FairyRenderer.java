package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Fairy;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

/**
 * orig RenderFairy.java + ClientProxyOreSpawn.java:477:
 * {@code new RenderFairy(new ModelFairy(1.5f), 0.1f, 0.35f)} - RenderLiving shadow = par2 * par3
 * (RenderFairy.java:22) and preRenderScale scales by par3 = 0.35 (RenderFairy.java:23,38-44).
 * The ModelFairy(1.5f) argument is not a size (ENT-S-092).
 */
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
    /** orig RenderFairy.scale = 0.35f (third constructor argument, ClientProxyOreSpawn.java:477). */
    public static final float SCALE = 0.35F;
    /** orig RenderLiving shadow = 0.1f * 0.35f (RenderFairy.java:22). */
    public static final float SHADOW = 0.1F * 0.35F;

    public FairyRenderer(EntityRendererProvider.Context context) {
        super(context, new FairyModel(context.bakeLayer(MODEL_LAYER)), SHADOW);
    }

    @Override
    protected void scale(Fairy entity, PoseStack poseStack, float partialTick) {
        // orig RenderFairy.preRenderScale: GL11.glScalef(scale, scale, scale) - same pipeline
        // position as LivingEntityRenderer.scale (after the (-1,-1,1) flip, before the -1.501 lift).
        poseStack.scale(SCALE, SCALE, SCALE);
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
