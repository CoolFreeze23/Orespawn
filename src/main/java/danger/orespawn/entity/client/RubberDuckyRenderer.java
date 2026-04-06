package danger.orespawn.entity.client;

import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityRubberDucky;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class RubberDuckyRenderer extends MobRenderer<EntityRubberDucky, RubberDuckyModel<EntityRubberDucky>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/rubberducky.png");
    public static final ModelLayerLocation MODEL_LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "rubberducky"), "main");

    public RubberDuckyRenderer(EntityRendererProvider.Context context) {
        super(context, new RubberDuckyModel<>(context.bakeLayer(MODEL_LAYER)), 0.25f);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityRubberDucky entity) { return TEXTURE; }
}
