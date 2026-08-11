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

    // OPT-013: evaluated for replacement with a finite inflated cull box and
    // intentionally left as-is. The animated model envelope (GeckoLib/MHLib
    // bone-driven parts, code-model limb rotations) is not statically provable
    // from any constant in this codebase, and an under-sized box would visibly
    // pop the boss out at the screen edge — a behavior change. Keeping
    // unconditional true is the strictly-neutral choice.
    @Override
    public boolean shouldRender(DungeonBeast entity, Frustum frustum, double x, double y, double z) {
        return true;
    }

    @Override
    public ResourceLocation getTextureLocation(DungeonBeast entity) {
        return TEXTURE;
    }
}
