package danger.orespawn.entity.client;

import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityMolenoid;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class MolenoidRenderer extends MobRenderer<EntityMolenoid, MolenoidModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/molenoid.png");
    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "molenoid"), "main");

    public MolenoidRenderer(EntityRendererProvider.Context context) {
        super(context, new MolenoidModel(context.bakeLayer(MODEL_LAYER)), 2.0f);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityMolenoid entity) {
        return TEXTURE;
    }
}
