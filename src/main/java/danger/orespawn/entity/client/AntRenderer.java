package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityAnt;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

public class AntRenderer extends MobRenderer<EntityAnt, AntModel<EntityAnt>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/ant.png");
    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "ant"), "main");

    public AntRenderer(EntityRendererProvider.Context context) {
        super(context, new AntModel<>(context.bakeLayer(MODEL_LAYER)), 0.1f);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityAnt entity) {
        return TEXTURE;
    }
}
