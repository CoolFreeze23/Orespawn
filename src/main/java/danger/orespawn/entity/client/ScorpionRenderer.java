package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityScorpion;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

public class ScorpionRenderer extends MobRenderer<EntityScorpion, ScorpionModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/scorpion.png");
    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "scorpion"), "main");

    public ScorpionRenderer(EntityRendererProvider.Context context) {
        super(context, new ScorpionModel(context.bakeLayer(MODEL_LAYER)), 0.3f);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityScorpion entity) {
        return TEXTURE;
    }
}
