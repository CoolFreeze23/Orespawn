package danger.orespawn.entity.client;

import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityLurkingTerror;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class LurkingTerrorRenderer extends MobRenderer<EntityLurkingTerror, LurkingTerrorModel<EntityLurkingTerror>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/lurkingterror.png");
    public static final ModelLayerLocation MODEL_LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "lurkingterror"), "main");

    public LurkingTerrorRenderer(EntityRendererProvider.Context context) {
        super(context, new LurkingTerrorModel<>(context.bakeLayer(MODEL_LAYER)), 1.0f);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityLurkingTerror entity) { return TEXTURE; }
}
