package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Mothra;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

public class MothraRenderer extends MobRenderer<Mothra, ButterflyModel<Mothra>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/eyemoth.png");
    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "mothra"), "main");

    public MothraRenderer(EntityRendererProvider.Context context) {
        super(context, new ButterflyModel<>(context.bakeLayer(MODEL_LAYER)), 1.5f);
    }

    @Override
    protected void scale(Mothra entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(5.0f, 5.0f, 5.0f);
    }

    @Override
    public ResourceLocation getTextureLocation(Mothra entity) {
        return TEXTURE;
    }
}
