package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Robot1;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

/**
 * orig RenderRobot1.java + ClientProxyOreSpawn.java:439:
 * {@code new RenderRobot1(new ModelRobot1(2.0f), 0.3f, 1.0f)}:
 * RenderLiving shadow = par2 * par3 (RenderRobot1.java:23), this.scale = par3
 * (:24), preRenderScale glScalef(scale) (:39-41, via func_77041_b :43-45) (ENT-S-092).
 * The ModelRobot1(2.0f) argument is wingspeed only, not a size.
 */
public class Robot1Renderer extends MobRenderer<Robot1, ModelRobot1> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/robot1.png");

    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "robot1"), "main");

    /** orig RenderRobot1.scale = 1.0f (third constructor argument, ClientProxyOreSpawn.java:439): identity, no scale() override needed. */
    public static final float SCALE = 1.0F;
    /** orig RenderLiving shadow = 0.3f * 1.0f (RenderRobot1.java:23). */
    public static final float SHADOW = 0.3F * 1.0F;

    public Robot1Renderer(EntityRendererProvider.Context context) {
        super(context, new ModelRobot1(context.bakeLayer(MODEL_LAYER)), SHADOW);
    }

    @Override
    public ResourceLocation getTextureLocation(Robot1 entity) {
        return TEXTURE;
    }
}
