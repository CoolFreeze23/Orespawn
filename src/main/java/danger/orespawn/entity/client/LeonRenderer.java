package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityLeon;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderer for the consolidated Leon/Leonopteryx (TF-030) — serves BOTH
 * registry ids (orespawn:leonopteryx canonical, orespawn:leon alias), matching
 * the single 1.7.10 RenderLeon (orig ClientProxyOreSpawn.java:500 —
 * {@code new RenderLeon(new ModelLeon(0.22f), 1.0f, 1.75f)}).
 */
public class LeonRenderer extends MobRenderer<EntityLeon, LeonModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/leon.png");
    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "leon"), "main");

    /**
     * orig RenderLeon render scale — ClientProxyOreSpawn.java:500 passes 1.75f,
     * applied in RenderLeon.java:39-41 {@code preRenderScale}:
     * {@code GL11.glScalef((float)this.scale, (float)this.scale, (float)this.scale)}.
     */
    private static final float SCALE = 1.75f;

    public LeonRenderer(EntityRendererProvider.Context context) {
        // Shadow 1.75f = orig shadowSize par2 * par3 = 1.0f * 1.75f
        // (RenderLeon.java:22-25 ctor, ClientProxyOreSpawn.java:500).
        super(context, new LeonModel(context.bakeLayer(MODEL_LAYER)), 1.75f);
    }

    @Override
    protected void scale(EntityLeon entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(SCALE, SCALE, SCALE);
    }

    @Override
    public boolean shouldRender(EntityLeon entity, Frustum frustum, double x, double y, double z) {
        // The Leon geometry (1.75x-scaled ~8-block wingspan) extends far beyond
        // the registered hitbox; skip frustum culling so the boss doesn't pop
        // out at screen edges. Carried over from the retired interim
        // LeonopteryxRenderer.
        return true;
    }

    @Override
    public ResourceLocation getTextureLocation(EntityLeon entity) {
        return TEXTURE;
    }
}
