package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.BandP;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class BandPRenderer extends MobRenderer<BandP, ModelBandP> {
    private static final ResourceLocation TEXTURE_BANDIT =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/bandp_bandit.png");
    private static final ResourceLocation TEXTURE_PIRATE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/bandp_pirate.png");

    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "bandp"), "main");

    public BandPRenderer(EntityRendererProvider.Context context) {
        super(context, new ModelBandP(context.bakeLayer(MODEL_LAYER)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(BandP entity) {
        return entity.getWhat() == 0 ? TEXTURE_BANDIT : TEXTURE_PIRATE;
    }
}
