package danger.orespawn.entity.client;

import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityLeafMonster;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * orig RenderLeafMonster.java + ClientProxyOreSpawn.java:472:
 * {@code new RenderLeafMonster(new ModelLeafMonster(), 0.65f, 1.0f)}:
 * RenderLiving shadow = par2 * par3 (RenderLeafMonster.java:23), this.scale = par3
 * (:24), and preRenderScale (:39-41, wired through func_77041_b at :43-45) scales
 * by 1.0 (ENT-S-092).
 */
public class LeafMonsterRenderer extends MobRenderer<EntityLeafMonster, LeafMonsterModel<EntityLeafMonster>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/leafmonster.png");
    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "leafmonster"), "main");

    /** orig RenderLeafMonster.scale = 1.0f (third constructor argument, ClientProxyOreSpawn.java:472). */
    public static final float SCALE = 1.0F;
    /** orig RenderLiving shadow = 0.65f * 1.0f (RenderLeafMonster.java:23). */
    public static final float SHADOW = 0.65F * 1.0F;

    public LeafMonsterRenderer(EntityRendererProvider.Context context) {
        super(context, new LeafMonsterModel<>(context.bakeLayer(MODEL_LAYER)), SHADOW);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityLeafMonster entity) {
        return TEXTURE;
    }
}
