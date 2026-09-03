package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Robot3;
import danger.orespawn.entity.pose.Robot3Pose;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Robot3: {@link ModelRobot3#setupAnim} verbatim on the converted rig
 * (Tier 3, code-driven per Amendment 1), including the per-entity
 * {@link RenderInfo} cosine zero-crossing latch, with {@link Robot3Renderer}'s
 * 0.5 render scale and its 1.0 x 0.5 shadow (ENT-S-092, from
 * ClientProxyOreSpawn.java:441 / RenderRobot3.java:23-24,39-45).
 */
public final class Robot3GeoReplacement extends OreSpawnGeoReplacement<Robot3> {
    private static final GeoReplacementDescriptor<Robot3> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.ROBOT_3.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            Robot3.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/robot3.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/robot3.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/robot3.png"),
            Robot3Renderer.SHADOW) {
        @Override
        public void applyScale(Robot3 entity, PoseStack poseStack, float partialTick) {
            poseStack.scale(Robot3Renderer.SCALE, Robot3Renderer.SCALE, Robot3Renderer.SCALE);
        }
    };

    public Robot3GeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // Intentionally none: the pose is code-driven below.
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        Robot3Pose entity = inputs.subject(Robot3Pose.class);
        float limbSwingAmount = inputs.limbSwingAmount();
        float ageInTicks = inputs.ageInTicks();
        float netHeadYaw = inputs.netHeadYaw();

        float walkAngle = limbSwingAmount > 0.1F
                ? Mth.cos(ageInTicks * 0.55F) * (float) Math.PI * 0.12F * limbSwingAmount
                : 0.0F;
        rotateX(processor, "lleg1", walkAngle);
        rotateX(processor, "lleg2", walkAngle);
        rotateX(processor, "rleg1", -walkAngle);
        rotateX(processor, "rleg2", -walkAngle);

        rotateY(processor, "lazer", (float) Math.toRadians(netHeadYaw / 2.0));

        RenderInfo r = entity.getRenderInfo();
        float armSwing = Mth.cos(ageInTicks * 1.0F) * (float) Math.PI * 0.15F;
        float nextangle = Mth.cos((ageInTicks + 0.3F) * 1.0F) * (float) Math.PI * 0.15F;
        if (nextangle > 0.0F && armSwing < 0.0F) {
            r.ri1 = entity.getAttacking() != 0 ? 1 : 0;
        }
        if (r.ri1 == 0) {
            armSwing = 0.0F;
        }

        rotateX(processor, "rarm1", armSwing - 1.0F);
        rotateX(processor, "rarm2", armSwing + 1.0F);
        rotateX(processor, "rarm3", armSwing + 1.0F);
        rotateX(processor, "larm1", armSwing - 1.0F);
        rotateX(processor, "larm2", armSwing + 1.0F);
        rotateX(processor, "larm3", armSwing + 1.0F);
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<Robot3, Robot3GeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new Robot3GeoReplacement());
        }
    }
}
