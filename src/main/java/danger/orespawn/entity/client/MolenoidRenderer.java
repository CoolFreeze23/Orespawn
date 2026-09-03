package danger.orespawn.entity.client;

import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityMolenoid;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * orig RenderMolenoid.java + ClientProxyOreSpawn.java:495:
 * {@code new RenderMolenoid(new ModelMolenoid(0.5f), 1.0f, 1.0f)}: RenderLiving shadow = par2 * par3
 * (RenderMolenoid.java:23), this.scale = par3 (:24), and preRenderScale (:39-41, wired through
 * func_77041_b at :43-45) scales by 1.0 (ENT-S-092). The ModelMolenoid(0.5f) argument is not a scale.
 */
public class MolenoidRenderer extends MobRenderer<EntityMolenoid, MolenoidModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/molenoid.png");
    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "molenoid"), "main");

    /** orig RenderMolenoid.scale = 1.0f (third constructor argument, ClientProxyOreSpawn.java:495). */
    public static final float SCALE = 1.0F;
    /** orig RenderLiving shadow = 1.0f * 1.0f (RenderMolenoid.java:23). */
    public static final float SHADOW = 1.0F * 1.0F;

    public MolenoidRenderer(EntityRendererProvider.Context context) {
        super(context, new MolenoidModel(context.bakeLayer(MODEL_LAYER)), SHADOW);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityMolenoid entity) {
        return TEXTURE;
    }
}
