package danger.orespawn.entity.client;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Robot4;
import danger.orespawn.entity.pose.Robot4Pose;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Robot4: {@link ModelRobot4#setupAnim} verbatim on the converted rig
 * (Tier 3, code-driven per Amendment 1), including the cannon assembly's pivot
 * follow (position writes) and the client-local {@code setShielding} side effect
 * kept bug-for-bug (ENT-K-070), with {@link Robot4Renderer}'s 1.0 x 1.0 shadow
 * (ENT-S-092, from ClientProxyOreSpawn.java:442 / RenderRobot4.java:23-24).
 */
public final class Robot4GeoReplacement extends OreSpawnGeoReplacement<Robot4> {
    private static final GeoReplacementDescriptor<Robot4> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.ROBOT_4.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            Robot4.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/robot4.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/robot4.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/robot4.png"),
            Robot4Renderer.SHADOW) {
    };

    private static final String[] CANNON_PARTS = {
            "cannonbase", "cannonend", "leftcannonpiece", "topcannonpiece", "rightcannonpiece",
            "bottomcannonpiece", "glowycannonbit1", "glowycannonbit2", "glowycannonbit3",
            "glowycannonbit4", "glowycannonbit5", "cannonammo",
    };

    public Robot4GeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // Intentionally none: the pose is code-driven below.
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        Robot4Pose entity = inputs.subject(Robot4Pose.class);
        float limbSwingAmount = inputs.limbSwingAmount();
        float ageInTicks = inputs.ageInTicks();
        float netHeadYaw = inputs.netHeadYaw();

        float walkAngle = limbSwingAmount > 0.1F
                ? Mth.cos(ageInTicks * 0.5F) * (float) Math.PI * 0.15F * limbSwingAmount
                : 0.0F;

        rotateX(processor, "leftfootfront", walkAngle);
        rotateX(processor, "leftfootbase", walkAngle);
        rotateX(processor, "leftfootback", walkAngle);
        rotateX(processor, "leftfoottip", walkAngle);
        rotateX(processor, "leftshin", walkAngle);
        rotateX(processor, "leftcalf", walkAngle + 0.175F);
        rotateX(processor, "leftkneegaurd", walkAngle + 0.63F);
        rotateX(processor, "leftthigh", walkAngle - 0.175F);

        rotateX(processor, "rightfootfront", -walkAngle);
        rotateX(processor, "rightfoottip", -walkAngle);
        rotateX(processor, "rightfootbase", -walkAngle);
        rotateX(processor, "rightfootback", -walkAngle);
        rotateX(processor, "rightshin", -walkAngle);
        rotateX(processor, "rightcalf", -walkAngle + 0.175F);
        rotateX(processor, "rightkneegaurd", -walkAngle + 0.63F);
        rotateX(processor, "rightthigh", -walkAngle - 0.175F);

        rotateY(processor, "head", (float) Math.toRadians(netHeadYaw / 1.5));

        float amp = 0.7853982F;
        float armAngle;
        if (entity.getAttacking() != 0) {
            armAngle = Mth.cos((float) Math.toRadians(ageInTicks % 360.0F) * 6.0F) * amp;
            armAngle = Math.abs(armAngle);
            armAngle += 0.75F;
        } else {
            armAngle = 0.0F;
        }
        if (armAngle > amp / 3.0F) {
            entity.setShielding(1);
        } else {
            entity.setShielding(0);
        }

        rotateX(processor, "rightsholder", -armAngle);
        rotateX(processor, "rightsholdergaurd", -armAngle - 0.21F);
        rotateX(processor, "sheildbase", -armAngle + 1.047F);
        rotateX(processor, "sheildtip", -armAngle + 1.047F);
        rotateX(processor, "rightupperarm", -armAngle - 0.21F);
        rotateX(processor, "rightlowerarm", -armAngle + 1.047F);
        rotateX(processor, "sheildend", -armAngle + 1.04F);
        rotateX(processor, "sholdergaurdtip", -armAngle - 0.21F);

        float cannonAngle = entity.getAttacking() != 0 ? 0.85F : 0.0F;
        float leftUpperArmRot = -cannonAngle - 0.21F;
        rotateX(processor, "leftsholder", -cannonAngle);
        rotateX(processor, "leftupperarm", leftUpperArmRot);
        rotateX(processor, "cannonbase", -cannonAngle - 0.7F);
        rotateX(processor, "cannonend", -cannonAngle - 0.7F);
        rotateX(processor, "leftcannonpiece", -cannonAngle - 0.7F);
        rotateX(processor, "topcannonpiece", -cannonAngle - 0.7F);
        rotateX(processor, "rightcannonpiece", -cannonAngle - 0.7F);
        rotateX(processor, "bottomcannonpiece", -cannonAngle - 0.7F);
        rotateX(processor, "glowycannonbit1", -cannonAngle + 0.17F);
        rotateX(processor, "glowycannonbit2", -cannonAngle + 0.17F);
        rotateX(processor, "glowycannonbit3", -cannonAngle + 0.08F);
        rotateX(processor, "glowycannonbit4", -cannonAngle + 0.08F);
        rotateX(processor, "glowycannonbit5", -cannonAngle);
        rotateX(processor, "cannonammo", -cannonAngle - 0.7F);

        // The cannon assembly follows the upper arm: pivot = shoulder + (cos, sin)(upper-arm angle) * 14.
        // The classic model reads leftsholder.y/z (never written, so the bind pivot) and the
        // leftupperarm.xRot it just assigned.
        float[] shoulder = classicPosition(bone(processor, "leftsholder"));
        float cannonY = (float) ((double) shoulder[1] + Math.cos(leftUpperArmRot) * 14.0);
        float cannonZ = (float) ((double) shoulder[2] + Math.sin(leftUpperArmRot) * 14.0);
        for (String part : CANNON_PARTS) {
            float[] current = classicPosition(bone(processor, part));
            moveTo(processor, part, current[0], cannonY, cannonZ);
        }
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<Robot4, Robot4GeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new Robot4GeoReplacement());
        }
    }
}
