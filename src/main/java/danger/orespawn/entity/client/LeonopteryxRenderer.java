package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Leonopteryx;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Leonopteryx renderer — re-uses the generic {@link ButterflyModel} body
 * (originally authored for Mothra) at a much larger scale so the wing-flap
 * silhouette reads correctly for the Mining-Dimension flying boss.
 *
 * <p>The model layer is registered as
 * {@code orespawn:leonopteryx#main}. We could share Mothra's layer, but a
 * dedicated layer keeps the door open to swapping in a bespoke
 * {@code ModelLeonopteryx} in a future visuals pass without churning every
 * other entity that uses {@code ButterflyModel}.</p>
 */
public class LeonopteryxRenderer extends MobRenderer<Leonopteryx, ButterflyModel<Leonopteryx>> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/leonopteryx.png");

    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(
                    ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "leonopteryx"), "main");

    public LeonopteryxRenderer(EntityRendererProvider.Context context) {
        super(context, new ButterflyModel<>(context.bakeLayer(MODEL_LAYER)), 2.0f);
    }

    @Override
    protected void scale(Leonopteryx entity, PoseStack poseStack, float partialTick) {
        // 4x scale matches the 4x2 hitbox so the visual wingspan and the
        // physical AABB stay in agreement.
        poseStack.scale(4.0f, 4.0f, 4.0f);
    }

    @Override
    public boolean shouldRender(Leonopteryx entity, Frustum frustum, double x, double y, double z) {
        return true;
    }

    @Override
    public ResourceLocation getTextureLocation(Leonopteryx entity) {
        return TEXTURE;
    }
}
