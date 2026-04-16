package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.QueenHead;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderer for the deprecated 1.7.10-era {@code QueenHead} sidecar entity.
 *
 * <p>Analogous to {@link KingHeadRenderer}: {@code QueenHead} was an invisible
 * helper entity in 1.7.10 that followed The Queen and served as a proxy hitbox
 * while its renderer was an empty stub. In 1.21.1 the multi-part hitbox work
 * has moved onto The Queen's {@link danger.orespawn.entity.OreSpawnPartEntity}
 * (see Phase 1 of the boss pipeline), so this renderer:
 * <ul>
 *   <li>Refuses to draw via {@link #shouldRender} returning {@code false}.</li>
 *   <li>Exists to keep NeoForge happy when decoding legacy save NBT.</li>
 * </ul>
 */
public class QueenHeadRenderer extends MobRenderer<QueenHead, ModelQueenHead> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/queenhead.png");
    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "queenhead"), "main");

    public QueenHeadRenderer(EntityRendererProvider.Context context) {
        super(context, new ModelQueenHead(context.bakeLayer(MODEL_LAYER)), 0.0f);
    }

    @Override
    public boolean shouldRender(QueenHead entity, Frustum frustum, double x, double y, double z) {
        return false;
    }

    @Override
    public void render(QueenHead entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        // No-op. Matches 1.7.10 RenderQueenHead empty-body stubs.
    }

    @Override
    public ResourceLocation getTextureLocation(QueenHead entity) {
        return TEXTURE;
    }
}
