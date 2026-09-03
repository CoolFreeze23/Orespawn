package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Cockateil;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * orig ClientProxyOreSpawn.java:430 (Cockateil) and :431 (RubyBird), identical arguments:
 * {@code new RenderCockateil(new ModelCockateil(1.0f), 0.3f, 0.75f)}: RenderCockateil.java:21-24
 * passes {@code par2 * par3} to RenderLiving as the shadow and keeps {@code par3} as the
 * preRenderCallback scale (ENT-S-092). The 1.0f model argument is wingspeed, not a size.
 * This renderer is shared by Cockateil and RubyBird in the port too (OreSpawnClient).
 */
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
    /** orig RenderCockateil.scale = 0.75f (third constructor argument). */
    public static final float SCALE = 0.75F;
    /** orig RenderLiving shadow = 0.3f * 0.75f. */
    public static final float SHADOW = 0.3F * 0.75F;

    public CockateilRenderer(EntityRendererProvider.Context context) {
        super(context, new ModelCockateil(context.bakeLayer(MODEL_LAYER)), SHADOW);
    }

    @Override
    protected void scale(Cockateil entity, PoseStack poseStack, float partialTick) {
        // orig RenderCockateil.preRenderCallback (func_77041_b -> preRenderScale, RenderCockateil.java:38-44):
        // GL11.glScalef(scale, scale, scale) - same pipeline position as LivingEntityRenderer.scale
        // (after the (-1,-1,1) flip, before the -1.501 lift).
        poseStack.scale(SCALE, SCALE, SCALE);
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
