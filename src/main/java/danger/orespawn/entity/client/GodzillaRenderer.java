package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Godzilla;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

public class GodzillaRenderer extends MobRenderer<Godzilla, ModelGodzilla> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/godzillatexture.png");
    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "godzilla"), "main");

    public GodzillaRenderer(EntityRendererProvider.Context context) {
        super(context, new ModelGodzilla(context.bakeLayer(MODEL_LAYER)), 5.0f);
    }

    @Override
    protected void scale(Godzilla entity, PoseStack poseStack, float partialTick) {
        // BOSS-017: orig RenderGodzilla.java:39-45 — /4 while PlayNicely.
        float effectiveScale = entity.getPlayNicely() != 0 ? 3.0F / 4.0F : 3.0F;
        poseStack.scale(effectiveScale, effectiveScale, effectiveScale);
    }

    // OPT-013: evaluated for replacement with a finite inflated cull box and
    // intentionally left as-is. The animated model envelope (GeckoLib/MHLib
    // bone-driven parts, code-model limb rotations) is not statically provable
    // from any constant in this codebase, and an under-sized box would visibly
    // pop the boss out at the screen edge — a behavior change. Keeping
    // unconditional true is the strictly-neutral choice.
    @Override
    public boolean shouldRender(Godzilla entity, Frustum frustum, double x, double y, double z) {
        return true;
    }

    @Override
    public ResourceLocation getTextureLocation(Godzilla entity) {
        return TEXTURE;
    }
}
