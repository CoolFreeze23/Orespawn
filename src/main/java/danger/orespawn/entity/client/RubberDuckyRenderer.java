package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityRubberDucky;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * orig RenderRubberDucky.java + ClientProxyOreSpawn.java:502:
 * {@code new RenderRubberDucky(new ModelRubberDucky(1.0f), 0.15f, 0.75f)} - RenderRubberDucky.java:24
 * passes {@code par2 * par3} to RenderLiving as the shadow and :25 keeps {@code scale = par3};
 * preRenderScale :40-46 scales by scale/2 for a child, else by scale (ENT-S-092).
 */
public class RubberDuckyRenderer extends MobRenderer<EntityRubberDucky, RubberDuckyModel<EntityRubberDucky>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/rubberducky.png");
    public static final ModelLayerLocation MODEL_LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "rubberducky"), "main");
    /** orig RenderRubberDucky.scale = 0.75f (third constructor argument). */
    public static final float SCALE = 0.75F;
    /** orig RenderLiving shadow = 0.15f * 0.75f. */
    public static final float SHADOW = 0.15F * 0.75F;

    public RubberDuckyRenderer(EntityRendererProvider.Context context) {
        super(context, new RubberDuckyModel<>(context.bakeLayer(MODEL_LAYER)), SHADOW);
    }

    @Override
    protected void scale(EntityRubberDucky entity, PoseStack poseStack, float partialTick) {
        // orig RenderRubberDucky.preRenderScale (:40-46, the preRenderCallback slot): a child
        // (func_70631_g_ = isChild) gets GL11.glScalef(scale / 2, ...), otherwise
        // GL11.glScalef(scale, scale, scale) - same pipeline position as LivingEntityRenderer.scale
        // (after the (-1,-1,1) flip, before the -1.501 lift).
        if (entity.isBaby()) {
            poseStack.scale(SCALE / 2.0F, SCALE / 2.0F, SCALE / 2.0F);
            return;
        }
        poseStack.scale(SCALE, SCALE, SCALE);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityRubberDucky entity) { return TEXTURE; }
}
