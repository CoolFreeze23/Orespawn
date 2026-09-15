package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Ghost;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

public class GhostRenderer extends MobRenderer<Ghost, GhostModel<Ghost>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/ghost.png");
    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "ghost"), "main");

    /** orig ClientProxyOreSpawn.java:409 new RenderGhost(new ModelGhost(), 0.0f, 0.65f): RenderLiving shadow = par2 * par3 (ENT-S-092). */
    public static final float SHADOW = 0F;
    /** orig preRenderCallback scale = par3 (ENT-S-092). */
    public static final float SCALE = 0.65F;

    public GhostRenderer(EntityRendererProvider.Context context) {
        super(context, new GhostModel<>(context.bakeLayer(MODEL_LAYER)), SHADOW);
    }

    @Override
    public ResourceLocation getTextureLocation(Ghost entity) {
        return TEXTURE;
    }

    // Route the Ghost through the translucent entity render pipeline so its
    // ghost.png alpha channel is respected. The default MobRenderer pipeline
    // uses RenderType.entityCutoutNoCull, which treats any pixel with alpha
    // < 1 as fully transparent — fine for mobs with hard edges, but it
    // destroys the semi-transparent sheet look we want for a ghost.
    // ENT-S-160 (a) (the remainder slice, 2026-09-15): the factory is the model's own function object
    // (GhostModel.RENDER_TYPE, the ENT-S-146 form), applied here to the texture exactly as the bare
    // RenderType.entityTranslucent(texture) call did, so the GeckoLib descriptor hands over the same object.
    @Override
    public RenderType getRenderType(Ghost entity, boolean visible, boolean visibleToPlayer, boolean glowing) {
        return GhostModel.RENDER_TYPE.apply(this.getTextureLocation(entity));
    }

    @Override
    protected void scale(Ghost entity, PoseStack poseStack, float partialTick) {
        // orig preRenderScale: GL11.glScalef(scale, scale, scale), the LivingEntityRenderer.scale slot
        poseStack.scale(SCALE, SCALE, SCALE);
    }
}
