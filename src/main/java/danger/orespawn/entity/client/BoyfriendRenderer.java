package danger.orespawn.entity.client;

import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Boyfriend;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;

public class BoyfriendRenderer extends HumanoidMobRenderer<Boyfriend, ModelBoyfriend> {

    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "boyfriend"), "main");

    public BoyfriendRenderer(EntityRendererProvider.Context context) {
        super(context, new ModelBoyfriend(context.bakeLayer(MODEL_LAYER)), 0.5f);
        this.addLayer(new HumanoidArmorLayer<>(this,
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),
                context.getModelManager()));
    }

    /**
     * orig Boyfriend.java:295-446 (getTexture) — while dry the FrogPrince
     * skins (is_prince 1/2) override everything, else the numbered dry skin;
     * while wet (wet_count &gt; 0) always the numbered swimshorts skin — even
     * a prince swims in shorts (original quirk, orig :388-444).
     */
    @Override
    public ResourceLocation getTextureLocation(Boyfriend entity) {
        if (entity.getWetCount() <= 0) {
            int prince = entity.getPrince();
            if (prince == 1) { // orig :298-300 (FrogPrince.png)
                return ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID,
                        "textures/entity/frogprince.png");
            }
            if (prince == 2) { // orig :301-303 (FrogPrince2.png)
                return ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID,
                        "textures/entity/frogprince2.png");
            }
            return ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID,
                    "textures/entity/boyfriend" + entity.getTameSkin() + ".png");
        }
        return ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID,
                "textures/entity/swimshorts" + entity.getWetTameSkin() + ".png");
    }
}
