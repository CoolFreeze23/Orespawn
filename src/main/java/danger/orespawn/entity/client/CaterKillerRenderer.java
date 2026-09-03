package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityCaterKiller;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

/**
 * orig RenderCaterKiller.java + ClientProxyOreSpawn.java:499:
 * {@code new RenderCaterKiller(new ModelCaterKiller(0.22f), 1.0f, 1.25f)} - RenderCaterKiller.java:23
 * passes {@code par2 * par3} to RenderLiving as the shadow and :24 keeps {@code scale = par3};
 * preRenderScale :39-45 scales by scale/2 when getPlayNicely() != 0, else by scale (ENT-S-092).
 */
public class CaterKillerRenderer extends MobRenderer<EntityCaterKiller, CaterKillerModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/caterkiller.png");
    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "caterkiller"), "main");
    /** orig RenderCaterKiller.scale = 1.25f (third constructor argument). */
    public static final float SCALE = 1.25F;
    /** orig RenderLiving shadow = 1.0f * 1.25f. */
    public static final float SHADOW = 1.0F * 1.25F;

    public CaterKillerRenderer(EntityRendererProvider.Context context) {
        super(context, new CaterKillerModel(context.bakeLayer(MODEL_LAYER)), SHADOW);
    }

    @Override
    protected void scale(EntityCaterKiller entity, PoseStack poseStack, float partialTick) {
        // orig RenderCaterKiller.preRenderScale (:39-45, the preRenderCallback slot):
        // if (getPlayNicely() != 0) GL11.glScalef(scale / 2, ...) else GL11.glScalef(scale, scale, scale)
        // - same pipeline position as LivingEntityRenderer.scale (after the (-1,-1,1) flip, before
        // the -1.501 lift). orig getPlayNicely() (CaterKiller.java:84-86) read the datawatcher copy
        // of OreSpawnMain.PlayNicely; the port has no such accessor and reads the config directly,
        // as EntityCaterKiller.getDefaultDimensions (:84-88) already does for the hitbox.
        if (OreSpawnConfig.PLAY_NICELY.get()) {
            poseStack.scale(SCALE / 2.0F, SCALE / 2.0F, SCALE / 2.0F);
            return;
        }
        poseStack.scale(SCALE, SCALE, SCALE);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityCaterKiller entity) {
        return TEXTURE;
    }
}
