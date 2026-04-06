package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Robot4;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

public class Robot4Renderer extends MobRenderer<Robot4, ModelRobot4> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/robot4.png");

    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "robot4"), "main");

    public Robot4Renderer(EntityRendererProvider.Context context) {
        super(context, new ModelRobot4(context.bakeLayer(MODEL_LAYER)), 1.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(Robot4 entity) {
        return TEXTURE;
    }
}
