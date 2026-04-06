package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Robot3;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

public class Robot3Renderer extends MobRenderer<Robot3, ModelRobot3> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/robot3.png");

    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "robot3"), "main");

    public Robot3Renderer(EntityRendererProvider.Context context) {
        super(context, new ModelRobot3(context.bakeLayer(MODEL_LAYER)), 2.0f);
    }

    @Override
    public ResourceLocation getTextureLocation(Robot3 entity) {
        return TEXTURE;
    }
}
