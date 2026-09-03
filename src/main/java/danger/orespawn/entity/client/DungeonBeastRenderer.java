package danger.orespawn.entity.client;

import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.DungeonBeast;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * orig RenderDungeonBeast.java + ClientProxyOreSpawn.java:481:
 * {@code new RenderDungeonBeast(new ModelDungeonBeast(0.62f), 0.25f, 1.0f)}: RenderLiving shadow = par2 * par3
 * (RenderDungeonBeast.java:23), this.scale = par3 (:24), and preRenderScale (:39-41, wired through
 * func_77041_b at :43-45) scales by 1.0 (ENT-S-092). The ModelDungeonBeast(0.62f) argument is not a scale.
 */
public class DungeonBeastRenderer extends MobRenderer<DungeonBeast, ModelDungeonBeast> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/dungeonbeast.png");

    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "dungeon_beast"), "main");

    /** orig RenderDungeonBeast.scale = 1.0f (third constructor argument, ClientProxyOreSpawn.java:481). */
    public static final float SCALE = 1.0F;
    /** orig RenderLiving shadow = 0.25f * 1.0f (RenderDungeonBeast.java:23). */
    public static final float SHADOW = 0.25F * 1.0F;

    public DungeonBeastRenderer(EntityRendererProvider.Context context) {
        super(context, new ModelDungeonBeast(context.bakeLayer(MODEL_LAYER)), SHADOW);
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
