package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityMantis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

public class MantisRenderer extends MobRenderer<EntityMantis, MantisModel<EntityMantis>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/mantis.png");
    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "mantis"), "main");

    public MantisRenderer(EntityRendererProvider.Context context) {
        super(context, new MantisModel<>(context.bakeLayer(MODEL_LAYER)), 0.8f);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityMantis entity) {
        return TEXTURE;
    }
}
