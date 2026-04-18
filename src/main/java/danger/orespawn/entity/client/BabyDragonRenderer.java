package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.BabyDragon;
import danger.orespawn.entity.Dragon;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderer for {@link BabyDragon}. We can't directly parameterise
 * {@link MobRenderer} as {@code <BabyDragon, ModelDragon>} because
 * {@link ModelDragon} is typed at {@link Dragon} (Java's invariance on the
 * model generic blocks the BabyDragon-extends-Dragon relationship). The
 * trick: parameterise the renderer at the parent type {@code Dragon} so the
 * generics line up, and rely on the renderer registry's runtime dispatch
 * to feed only {@code BabyDragon} instances into this concrete renderer.
 * Each render call applies a 0.45× uniform scale on top of the standard
 * Dragon model so the baby visually reads as a half-size variant.
 */
public class BabyDragonRenderer extends MobRenderer<Dragon, ModelDragon> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/dragon.png");
    private static final ResourceLocation TEXTURE_WHITE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/white_dragon.png");

    public static final ModelLayerLocation MODEL_LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "baby_dragon"), "main");

    private static final float SCALE = 0.45f;

    public BabyDragonRenderer(EntityRendererProvider.Context context) {
        super(context, new ModelDragon(context.bakeLayer(MODEL_LAYER)), 0.6f);
    }

    @Override
    public void render(Dragon entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.scale(SCALE, SCALE, SCALE);
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(Dragon entity) {
        if (entity.getDragonType() != 0) {
            return TEXTURE_WHITE;
        }
        return TEXTURE;
    }
}
