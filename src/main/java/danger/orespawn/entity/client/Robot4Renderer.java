package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Robot4;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

/**
 * orig RenderRobot4.java + ClientProxyOreSpawn.java:442:
 * {@code new RenderRobot4(new ModelRobot4(1.0f), 1.0f, 1.0f)}:
 * RenderLiving shadow = par2 * par3 (RenderRobot4.java:23), this.scale = par3
 * (:24), preRenderScale glScalef(scale) (:39-41, via func_77041_b :43-45) (ENT-S-092).
 * The ModelRobot4(1.0f) argument is wingspeed only, not a size.
 */
public class Robot4Renderer extends MobRenderer<Robot4, ModelRobot4> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/robot4.png");

    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "robot4"), "main");

    /** orig RenderRobot4.scale = 1.0f (third constructor argument, ClientProxyOreSpawn.java:442): identity, no scale() override needed. */
    public static final float SCALE = 1.0F;
    /** orig RenderLiving shadow = 1.0f * 1.0f (RenderRobot4.java:23). */
    public static final float SHADOW = 1.0F * 1.0F;

    public Robot4Renderer(EntityRendererProvider.Context context) {
        super(context, new ModelRobot4(context.bakeLayer(MODEL_LAYER)), SHADOW);
    }

    @Override
    public ResourceLocation getTextureLocation(Robot4 entity) {
        return TEXTURE;
    }
}
