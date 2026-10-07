package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

import java.util.function.Function;

/**
 * A model's blended parts, drawn after its opaque pass under the same pose: the 1.7.10 models' {@code GL_BLEND} block
 * at the end of their render ({@code glBlendFunc(SRC_ALPHA, ONE_MINUS_SRC_ALPHA)} and a {@code glColor4f} tint around
 * the wing membranes), which one cutout pass drew opaque and untinted. The layer runs inside the living renderer's
 * own chain, so the pass needs no transform of its own; no hurt overlay, as on the King's membranes.
 */
public final class WingMembraneLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    /** The model's blended pass: the membrane parts alone, on the consumer, light, overlay and colour given. */
    @FunctionalInterface
    public interface Pass<M> {
        void render(M model, PoseStack poseStack, VertexConsumer consumer, int light, int overlay, int color);
    }

    private final Pass<M> pass;
    private final Function<ResourceLocation, RenderType> renderType;
    private final int color;

    public WingMembraneLayer(RenderLayerParent<T, M> parent, Pass<M> pass, Function<ResourceLocation, RenderType> renderType,
                             int color) {
        super(parent);
        this.pass = pass;
        this.renderType = renderType;
        this.color = color;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffers, int packedLight, T entity, float limbSwing,
                       float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        if (entity.isInvisible()) return;
        VertexConsumer consumer = buffers.getBuffer(renderType.apply(getTextureLocation(entity)));
        pass.render(getParentModel(), poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY, color);
    }
}
