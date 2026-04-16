package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.KingHead;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderer for the deprecated 1.7.10-era {@code KingHead} sidecar entity.
 *
 * <p>In 1.7.10 OreSpawn, {@code KingHead} was an invisible helper entity that
 * followed The King and acted as a proxy hitbox. Its legacy render class
 * {@code RenderKingHead} had every method reduced to an empty body (see
 * {@code reference_1_7_10_source/sources/danger/orespawn/RenderKingHead.java})
 * so the sidecar never drew any visible geometry.
 *
 * <p>In our 1.21.1 port the multi-part hitbox is provided by
 * {@link danger.orespawn.entity.OreSpawnPartEntity} attached to The King itself
 * (Phase 1 of the boss pipeline). {@code KingHead} is retained ONLY for
 * backward-compatible NBT decoding of old saves, so this renderer must:
 * <ul>
 *   <li>Refuse to draw via {@link #shouldRender} returning {@code false}.</li>
 *   <li>Exist so NeoForge can register the entity type client-side without
 *       warnings when an old save is loaded.</li>
 * </ul>
 * The model / layer definition are retained as 1x1 no-op geometry (see
 * {@link ModelKingHead} whose {@code renderToBuffer} is empty).
 */
public class KingHeadRenderer extends MobRenderer<KingHead, ModelKingHead> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/kinghead.png");
    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "kinghead"), "main");

    public KingHeadRenderer(EntityRendererProvider.Context context) {
        super(context, new ModelKingHead(context.bakeLayer(MODEL_LAYER)), 0.0f);
    }

    @Override
    public boolean shouldRender(KingHead entity, Frustum frustum, double x, double y, double z) {
        return false;
    }

    @Override
    public void render(KingHead entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        // No-op. Matches 1.7.10 RenderKingHead which reduced every method body
        // to an empty statement. Rendering is handled entirely by the parent
        // TheKing entity's renderer and its PartEntity framework.
    }

    @Override
    public ResourceLocation getTextureLocation(KingHead entity) {
        return TEXTURE;
    }
}
