package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntitySpitBug;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

public class SpitBugRenderer extends MobRenderer<EntitySpitBug, SpitBugModel<EntitySpitBug>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/spitbug.png");
    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "spitbug"), "main");

    public SpitBugRenderer(EntityRendererProvider.Context context) {
        super(context, new SpitBugModel<>(context.bakeLayer(MODEL_LAYER)), 0.6f);
    }

    @Override
    public ResourceLocation getTextureLocation(EntitySpitBug entity) {
        return TEXTURE;
    }
}
