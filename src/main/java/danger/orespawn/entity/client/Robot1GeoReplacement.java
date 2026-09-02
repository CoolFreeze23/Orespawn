package danger.orespawn.entity.client;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Robot1;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationProcessor;

/** GeckoLib Robot1: {@link ModelRobot1#setupAnim} verbatim on the converted rig (Tier 3, code-driven per Amendment 1). */
public final class Robot1GeoReplacement extends OreSpawnGeoReplacement<Robot1> {
    private static final GeoReplacementDescriptor<Robot1> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.ROBOT_1.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            Robot1.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/robot1.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/robot1.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/robot1.png"),
            0.5F) {
    };

    public Robot1GeoReplacement() {
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
        float ageInTicks = inputs.ageInTicks();

        float walkAngle = limbSwingAmount > 0.1F
                ? Mth.cos(limbSwing * 1.5F) * (float) Math.PI * 0.75F * limbSwingAmount
                : 0.0F;
        rotateX(processor, "rfoot", -walkAngle);
        rotateX(processor, "lfoot", walkAngle);

        float keyRot = (float) Math.toRadians(ageInTicks * 0.75F);
        rotateZ(processor, "key1", keyRot);
        rotateZ(processor, "key2", keyRot);
        rotateZ(processor, "key3", keyRot);
        rotateZ(processor, "key4", keyRot);
        rotateZ(processor, "key5", keyRot);
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<Robot1, Robot1GeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new Robot1GeoReplacement());
        }
    }
}
