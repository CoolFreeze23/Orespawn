package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Robot2;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

public class Robot2Renderer extends MobRenderer<Robot2, ModelRobot2> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/robot2.png");

    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "robot2"), "main");

    public Robot2Renderer(EntityRendererProvider.Context context) {
        super(context, new ModelRobot2(context.bakeLayer(MODEL_LAYER)), 1.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(Robot2 entity) {
        return TEXTURE;
    }
}
