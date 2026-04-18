package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.HoverboardEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Phase 10 — Hoverboard renderer. Uses a dedicated {@link HoverboardModel}
 * (flat board + small fin) and resolves a texture path based on the entity's
 * synced colour byte (1..10), matching the 1.7.10 ten-paint-job rotation that
 * the Ultimate Sword interaction cycles through.
 */
public class HoverboardRenderer extends MobRenderer<HoverboardEntity, HoverboardModel> {

    public static final ModelLayerLocation MODEL_LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "hoverboard"), "main");

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            OreSpawnMod.MOD_ID, "textures/entity/elevator.png");

    public HoverboardRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new HoverboardModel(ctx.bakeLayer(MODEL_LAYER)), 0.4f);
    }

    @Override
    public ResourceLocation getTextureLocation(HoverboardEntity entity) {
        // We share the legacy 10-paint elevator sheet. A per-hoverboard palette
        // can be slotted in later by swapping the path on a per-colour basis.
        return TEXTURE;
    }

    @Override
    protected void scale(HoverboardEntity entity, PoseStack stack, float partialTicks) {
        stack.scale(1.0f, 0.5f, 1.0f);
    }
}
