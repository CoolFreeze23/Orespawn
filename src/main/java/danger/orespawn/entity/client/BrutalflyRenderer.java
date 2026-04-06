package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityBrutalfly;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

public class BrutalflyRenderer extends MobRenderer<EntityBrutalfly, BrutalflyModel<EntityBrutalfly>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/brutalfly.png");
    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "brutalfly"), "main");

    public BrutalflyRenderer(EntityRendererProvider.Context context) {
        super(context, new BrutalflyModel<>(context.bakeLayer(MODEL_LAYER)), 1.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityBrutalfly entity) {
        return TEXTURE;
    }
}
