package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.GodzillaHead;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderer for the deprecated 1.7.10-era {@code GodzillaHead} sidecar entity.
 *
 * <p>In 1.7.10 OreSpawn, {@code GodzillaHead} (the boss whose public display name
 * is "Mobzilla", see {@link danger.orespawn.entity.Godzilla}) was an invisible
 * helper entity that followed the parent every tick and acted as a proxy
 * hitbox for ranged damage / targeting. Its legacy renderer
 * {@code RenderGodzillaHead} drew no visible geometry.
 *
 * <p>In our 1.21.1 port the multi-part hitbox is provided by
 * {@link danger.orespawn.entity.OreSpawnPartEntity} instances attached to
 * {@code Godzilla} itself (Phase 1 of the boss pipeline). {@code GodzillaHead}
 * is retained ONLY for NBT backward compatibility with old saves, so this
 * renderer must:
 * <ul>
 *   <li>Refuse to draw via {@link #shouldRender} returning {@code false}.</li>
 *   <li>Still be registered so NeoForge can resolve the entity type
 *       client-side when old saves are loaded.</li>
 * </ul>
 * Mirrors the same pattern used for {@link KingHeadRenderer} and
 * {@link QueenHeadRenderer}.
 */
public class GodzillaHeadRenderer extends MobRenderer<GodzillaHead, ModelGodzillaHead> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/godzillahead.png");
    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "godzillahead"), "main");

    public GodzillaHeadRenderer(EntityRendererProvider.Context context) {
        super(context, new ModelGodzillaHead(context.bakeLayer(MODEL_LAYER)), 0.0f);
    }

    @Override
    public boolean shouldRender(GodzillaHead entity, Frustum frustum, double x, double y, double z) {
        return false;
    }

    @Override
    public void render(GodzillaHead entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        // No-op. Matches 1.7.10 RenderGodzillaHead which drew no geometry.
        // Hitboxes/visuals are handled by Godzilla's OreSpawnPartEntity array
        // and Godzilla's own renderer.
    }

    @Override
    public ResourceLocation getTextureLocation(GodzillaHead entity) {
        return TEXTURE;
    }
}
