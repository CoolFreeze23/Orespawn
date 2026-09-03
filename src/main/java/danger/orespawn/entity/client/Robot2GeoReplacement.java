package danger.orespawn.entity.client;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Robot2;
import danger.orespawn.entity.pose.Robot2Pose;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Robot2: {@link ModelRobot2#setupAnim} verbatim on the converted rig
 * (Tier 3, code-driven per Amendment 1), including the per-entity
 * {@link RenderInfo} zero-crossing latch and its RNG re-roll, with
 * {@link Robot2Renderer}'s 1.0 x 1.0 shadow (ENT-S-092, from
 * ClientProxyOreSpawn.java:440 / RenderRobot2.java:23-24).
 */
public final class Robot2GeoReplacement extends OreSpawnGeoReplacement<Robot2> {
    private static final GeoReplacementDescriptor<Robot2> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.ROBOT_2.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            Robot2.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/robot2.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/robot2.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/robot2.png"),
            Robot2Renderer.SHADOW) {
    };

    public Robot2GeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // Intentionally none: the pose is code-driven below.
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        Robot2Pose entity = inputs.subject(Robot2Pose.class);
        float limbSwingAmount = inputs.limbSwingAmount();
        float ageInTicks = inputs.ageInTicks();
        float netHeadYaw = inputs.netHeadYaw();

        float walkAngle = limbSwingAmount > 0.1F
                ? Mth.cos(ageInTicks * 0.3F) * (float) Math.PI * 0.12F * limbSwingAmount
                : 0.0F;
        rotateX(processor, "lleg1", walkAngle);
        rotateX(processor, "lleg2", walkAngle);
        rotateX(processor, "rleg1", -walkAngle);
        rotateX(processor, "rleg2", -walkAngle);

        rotateY(processor, "head", (float) Math.toRadians(netHeadYaw));

        RenderInfo r = entity.getRenderInfo();
        float newangle = Mth.sin((float) Math.toRadians(ageInTicks * 20.0F));
        float nextangle = Mth.sin((float) Math.toRadians(ageInTicks * 20.0F + 1.5F));
        if (nextangle > 0.0F && newangle < 0.0F) {
            r.ri1 = 0;
            if (entity.getAttacking() != 0) {
                while (r.ri1 == 0) {
                    r.ri1 = entity.getRandom().nextInt(4);
                }
            }
        }

        float armAngle = (float) Math.toRadians(ageInTicks * 20.0F);
        float rightArm = r.ri1 == 1 || r.ri1 == 3 ? armAngle : 0.0F;
        rotateX(processor, "rarm1", rightArm);
        rotateX(processor, "rarm2", rightArm);
        rotateX(processor, "rarm3", rightArm);
        float leftArm = r.ri1 == 2 || r.ri1 == 3 ? armAngle : 0.0F;
        rotateX(processor, "larm1", leftArm);
        rotateX(processor, "larm2", leftArm);
        rotateX(processor, "larm3", leftArm);
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<Robot2, Robot2GeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new Robot2GeoReplacement());
        }
    }
}
