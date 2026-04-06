package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityCaterKiller;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

public class CaterKillerRenderer extends MobRenderer<EntityCaterKiller, CaterKillerModel<EntityCaterKiller>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/caterkiller.png");
    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "caterkiller"), "main");

    public CaterKillerRenderer(EntityRendererProvider.Context context) {
        super(context, new CaterKillerModel<>(context.bakeLayer(MODEL_LAYER)), 1.2f);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityCaterKiller entity) {
        return TEXTURE;
    }
}
