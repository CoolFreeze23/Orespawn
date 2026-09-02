package danger.orespawn.entity.client;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Robot5;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationProcessor;

/** GeckoLib Robot5: {@link ModelRobot5#setupAnim} verbatim on the converted rig (Tier 3, code-driven per Amendment 1). */
public final class Robot5GeoReplacement extends OreSpawnGeoReplacement<Robot5> {
    private static final GeoReplacementDescriptor<Robot5> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.ROBOT_5.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            Robot5.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/robot5.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/robot5.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/robot5.png"),
            0.5F) {
    };

    public Robot5GeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // Intentionally none: the pose is code-driven below.
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        float limbSwing = inputs.limbSwing();
        float limbSwingAmount = inputs.limbSwingAmount();
        float netHeadYaw = inputs.netHeadYaw();

        float wheelAngle;
        if (limbSwingAmount > 0.1F) {
            wheelAngle = limbSwing * 0.15F % ((float) Math.PI * 2);
            wheelAngle = Math.abs(wheelAngle);
        } else {
            wheelAngle = 0.0F;
        }
        rotateX(processor, "lwheel1", wheelAngle);
        rotateX(processor, "lwheel2", wheelAngle + 0.7853982F);
        rotateX(processor, "rwheel1", wheelAngle);
        rotateX(processor, "rwheel2", wheelAngle + 0.7853982F);

        float turretYaw = (float) Math.toRadians(netHeadYaw / 2.0);
        rotateY(processor, "barrel1", turretYaw);
        rotateY(processor, "barrel2", turretYaw);
        rotateY(processor, "ammobox", turretYaw);
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<Robot5, Robot5GeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new Robot5GeoReplacement());
        }
    }
}
