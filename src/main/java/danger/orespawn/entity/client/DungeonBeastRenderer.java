package danger.orespawn.entity.client;

import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.DungeonBeast;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class DungeonBeastRenderer extends MobRenderer<DungeonBeast, ModelDungeonBeast> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/dungeonbeast.png");

    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "dungeon_beast"), "main");

    public DungeonBeastRenderer(EntityRendererProvider.Context context) {
        super(context, new ModelDungeonBeast(context.bakeLayer(MODEL_LAYER)), 0.5f);
    }

    @Override
    public boolean shouldRender(DungeonBeast entity, Frustum frustum, double x, double y, double z) {
        return true;
    }

    @Override
    public ResourceLocation getTextureLocation(DungeonBeast entity) {
        return TEXTURE;
    }
}
