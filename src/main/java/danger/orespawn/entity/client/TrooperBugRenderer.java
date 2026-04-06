package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityTrooperBug;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

public class TrooperBugRenderer extends MobRenderer<EntityTrooperBug, TrooperBugModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/trooperbug.png");
    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "trooperbug"), "main");

    public TrooperBugRenderer(EntityRendererProvider.Context context) {
        super(context, new TrooperBugModel(context.bakeLayer(MODEL_LAYER)), 1.0f);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityTrooperBug entity) {
        return TEXTURE;
    }
}
