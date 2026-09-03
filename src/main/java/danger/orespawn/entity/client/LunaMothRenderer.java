package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityLunaMoth;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

/**
 * orig RenderButterfly.java + ClientProxyOreSpawn.java:407:
 * {@code new RenderButterfly(new ModelButterfly(0.75f), 0.4f, 1.5f)} - RenderLiving shadow = par2 * par3
 * (RenderButterfly.java:25) and preRenderScale scales by par3 = 1.5 (RenderButterfly.java:26,41-47).
 * The ModelButterfly(0.75f) argument is only wingspeed, not a size (ENT-S-092).
 */
public class LunaMothRenderer extends MobRenderer<EntityLunaMoth, ButterflyModel<EntityLunaMoth>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/lunamoth.png");
    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "luna_moth"), "main");
    /** orig RenderButterfly.scale = 1.5f (third constructor argument, ClientProxyOreSpawn.java:407). */
    public static final float SCALE = 1.5F;
    /** orig RenderLiving shadow = 0.4f * 1.5f (RenderButterfly.java:25). */
    public static final float SHADOW = 0.4F * 1.5F;

    public LunaMothRenderer(EntityRendererProvider.Context context) {
        // wingspeed 0.75f — orig ClientProxyOreSpawn.java:407 (new ModelButterfly(0.75f))
        super(context, new ButterflyModel<>(context.bakeLayer(MODEL_LAYER), 0.75f), SHADOW);
    }

    @Override
    protected void scale(EntityLunaMoth entity, PoseStack poseStack, float partialTick) {
        // 1.5f — orig ClientProxyOreSpawn.java:407 third arg, applied via orig RenderButterfly.java:26,42 (glScalef)
        poseStack.scale(SCALE, SCALE, SCALE);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityLunaMoth entity) {
        return switch (entity.moth_type) {
            case 1 -> ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/eyemoth.png");
            case 2 -> ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/darkmoth.png");
            case 3 -> ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/firemoth.png");
            default -> TEXTURE;
        };
    }
}
