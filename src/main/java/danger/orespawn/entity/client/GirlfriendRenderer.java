package danger.orespawn.entity.client;

import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Girlfriend;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;

public class GirlfriendRenderer extends HumanoidMobRenderer<Girlfriend, ModelGirlfriend> {

    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "girlfriend"), "main");

    public GirlfriendRenderer(EntityRendererProvider.Context context) {
        super(context, new ModelGirlfriend(context.bakeLayer(MODEL_LAYER)), 0.5f);
        this.addLayer(new HumanoidArmorLayer<>(this,
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),
                context.getModelManager()));
    }

    @Override
    public ResourceLocation getTextureLocation(Girlfriend entity) {
        int skin = entity.getTameSkin();
        return ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID,
                "textures/entity/girlfriend" + skin + ".png");
    }
}
