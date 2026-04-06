package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityStinkBug;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

public class StinkBugRenderer extends MobRenderer<EntityStinkBug, StinkBugModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/stinkbug.png");
    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "stinkbug"), "main");

    public StinkBugRenderer(EntityRendererProvider.Context context) {
        super(context, new StinkBugModel(context.bakeLayer(MODEL_LAYER)), 0.2f);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityStinkBug entity) {
        return TEXTURE;
    }
}
