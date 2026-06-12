package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import danger.orespawn.entity.GiantRobot;

/**
 * GiantRobot model. The original (orig ModelGiantRobot.java:150-279) drew the
 * shared thigh/shin/foot parts twice — once per leg pose — and the shared
 * arm parts twice — once per arm pose — re-rendering the same ModelRenderers
 * at both positions, with a hip-bob walk cycle and a punch-windmill arm
 * animation gated on {@code getAttacking()} (orig ModelGiantRobot.java:230-240).
 * The pose values are recomputed every frame from the animation arguments
 * (orig scratch holder {@code RenderGiantRobotInfo} carries no cross-frame
 * state for this model), so the port computes them in {@link #setupAnim} and
 * replays the original's two-pass render order in {@link #renderToBuffer}.
 */
public class ModelGiantRobot extends EntityModel<GiantRobot> {
    /** orig ClientProxyOreSpawn.java:516 — {@code new ModelGiantRobot(0.25f)}. */
    private static final float WING_SPEED = 0.25f;
    /** orig ModelGiantRobot.java:41 — Hip rotation-point Y captured as {@code hipy}. */
    private static final float HIP_BASE_Y = -60.0f;

    private final ModelPart hip;
    private final ModelPart thigh;
    private final ModelPart shin;
    private final ModelPart foot1;
    private final ModelPart foot2;
    private final ModelPart foot3;
    private final ModelPart thigh2;
    private final ModelPart thigh3;
    private final ModelPart back1;
    private final ModelPart back2;
    private final ModelPart back3;
    private final ModelPart shoulders;
    private final ModelPart neck;
    private final ModelPart head;
    private final ModelPart arm1;
    private final ModelPart arm2;
    private final ModelPart arm3;
    private final ModelPart knuckles;

    public ModelGiantRobot(ModelPart root) {
        this.hip = root.getChild("hip");
        this.thigh = root.getChild("thigh");
        this.shin = root.getChild("shin");
        this.foot1 = root.getChild("foot1");
        this.foot2 = root.getChild("foot2");
        this.foot3 = root.getChild("foot3");
        this.thigh2 = root.getChild("thigh2");
        this.thigh3 = root.getChild("thigh3");
        this.back1 = root.getChild("back1");
        this.back2 = root.getChild("back2");
        this.back3 = root.getChild("back3");
        this.shoulders = root.getChild("shoulders");
        this.neck = root.getChild("neck");
        this.head = root.getChild("head");
        this.arm1 = root.getChild("arm1");
        this.arm2 = root.getChild("arm2");
        this.arm3 = root.getChild("arm3");
        this.knuckles = root.getChild("knuckles");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("hip",
                CubeListBuilder.create().texOffs(0, 0).mirror()
                        .addBox(-4.0F, -4.0F, -15.0F, 8, 8, 30),
                PartPose.offset(0.0F, -60.0F, 0.0F));

        partdefinition.addOrReplaceChild("thigh",
                CubeListBuilder.create().texOffs(0, 115).mirror()
                        .addBox(-3.0F, -3.0F, -3.0F, 6, 43, 6),
                PartPose.offset(0.0F, -58.0F, 0.0F));

        partdefinition.addOrReplaceChild("shin",
                CubeListBuilder.create().texOffs(0, 167).mirror()
                        .addBox(-3.0F, -3.0F, -3.0F, 6, 43, 6),
                PartPose.offset(0.0F, -18.0F, 0.0F));

        partdefinition.addOrReplaceChild("foot1",
                CubeListBuilder.create().texOffs(0, 282).mirror()
                        .addBox(-7.0F, 38.0F, -11.0F, 14, 4, 17),
                PartPose.offset(0.0F, -18.0F, 0.0F));

        partdefinition.addOrReplaceChild("foot2",
                CubeListBuilder.create().texOffs(0, 246).mirror()
                        .addBox(-6.0F, 19.0F, -8.0F, 12, 19, 13),
                PartPose.offset(0.0F, -18.0F, 0.0F));

        partdefinition.addOrReplaceChild("foot3",
                CubeListBuilder.create().texOffs(0, 219).mirror()
                        .addBox(-5.0F, 5.0F, -5.0F, 10, 14, 9),
                PartPose.offset(0.0F, -18.0F, 0.0F));

        partdefinition.addOrReplaceChild("thigh2",
                CubeListBuilder.create().texOffs(0, 43).mirror()
                        .addBox(-7.0F, -8.0F, -7.0F, 14, 24, 14),
                PartPose.offset(0.0F, -58.0F, 0.0F));

        partdefinition.addOrReplaceChild("thigh3",
                CubeListBuilder.create().texOffs(0, 84).mirror()
                        .addBox(-5.0F, 16.0F, -5.0F, 10, 17, 10),
                PartPose.offset(0.0F, -58.0F, 0.0F));

        partdefinition.addOrReplaceChild("back1",
                CubeListBuilder.create().texOffs(125, 138).mirror()
                        .addBox(-4.0F, -20.0F, -4.0F, 8, 24, 8),
                PartPose.offset(0.0F, -60.0F, 0.0F));

        partdefinition.addOrReplaceChild("back2",
                CubeListBuilder.create().texOffs(125, 95).mirror()
                        .addBox(-13.0F, -42.0F, -10.0F, 26, 24, 16),
                PartPose.offset(0.0F, -60.0F, 0.0F));

        partdefinition.addOrReplaceChild("back3",
                CubeListBuilder.create().texOffs(125, 43).mirror()
                        .addBox(-17.0F, -68.0F, -13.0F, 34, 26, 20),
                PartPose.offset(0.0F, -60.0F, 0.0F));

        partdefinition.addOrReplaceChild("shoulders",
                CubeListBuilder.create().texOffs(60, 200).mirror()
                        .addBox(-22.0F, -64.0F, -4.0F, 44, 8, 8),
                PartPose.offset(0.0F, -60.0F, 0.0F));

        partdefinition.addOrReplaceChild("neck",
                CubeListBuilder.create().texOffs(125, 29).mirror()
                        .addBox(-4.0F, -70.0F, -4.0F, 8, 2, 8),
                PartPose.offset(0.0F, -60.0F, 0.0F));

        partdefinition.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(127, 0).mirror()
                        .addBox(-7.0F, -82.0F, -7.0F, 14, 12, 14),
                PartPose.offset(0.0F, -60.0F, 0.0F));

        partdefinition.addOrReplaceChild("arm1",
                CubeListBuilder.create().texOffs(77, 250).mirror()
                        .addBox(-6.0F, -6.0F, -6.0F, 12, 21, 12),
                PartPose.offset(28.0F, -120.0F, 0.0F));

        partdefinition.addOrReplaceChild("arm2",
                CubeListBuilder.create().texOffs(73, 300).mirror()
                        .addBox(-4.0F, 15.0F, -4.0F, 8, 24, 8),
                PartPose.offset(28.0F, -120.0F, 0.0F));

        partdefinition.addOrReplaceChild("arm3",
                CubeListBuilder.create().texOffs(61, 350).mirror()
                        .addBox(-3.0F, -3.0F, -3.0F, 6, 33, 6),
                PartPose.offset(28.0F, -81.0F, 0.0F));

        partdefinition.addOrReplaceChild("knuckles",
                CubeListBuilder.create().texOffs(56, 400).mirror()
                        .addBox(-7.0F, 30.0F, -5.0F, 14, 12, 10),
                PartPose.offset(28.0F, -81.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 256, 512);
    }

    // Walk/attack pose values recomputed in setupAnim each frame and consumed
    // by the two-pass limb rendering in renderToBuffer (mirrors the original's
    // RenderGiantRobotInfo scratch, orig ModelGiantRobot.java:162-167).
    private float thighAngle0;
    private float thighAngle1;
    private float shinAngle0;
    private float shinAngle1;
    private float armA1Angle;
    private float armA2Angle;
    private float armB1Angle;
    private float armB2Angle;

    @Override
    public void setupAnim(GiantRobot entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        // orig ModelGiantRobot.java:158-161 — movescale = limbSwingAmount * 0.65 clamped to 1.
        float movescale = limbSwingAmount * 0.65f;
        if (movescale > 1.0f) {
            movescale = 1.0f;
        }

        // orig ModelGiantRobot.java:162-167 — hip sway and two-phase thigh/shin angles.
        float hipXAngle = (float) (Math.cos(-ageInTicks * WING_SPEED) * Math.PI * 0.1f * movescale);
        float hipYAngle = (float) (Math.sin(-ageInTicks * WING_SPEED) * Math.PI * 0.1f * movescale);
        this.thighAngle0 = (float) (Math.cos((double) (-ageInTicks * WING_SPEED) + Math.PI / 2.0) * Math.PI * 0.15f * movescale)
                - (float) (0.19634954084936207 * movescale);
        this.thighAngle1 = (float) (Math.cos((double) (-ageInTicks * WING_SPEED) + Math.PI + Math.PI / 2.0) * Math.PI * 0.15f * movescale)
                - (float) (0.19634954084936207 * movescale);
        this.shinAngle0 = (float) ((double) ((float) (Math.cos((double) (-ageInTicks * WING_SPEED) + Math.PI) * Math.PI * 0.2f * movescale))
                + 0.6283185400806344 * movescale);
        this.shinAngle1 = (float) ((double) ((float) (Math.cos(-ageInTicks * WING_SPEED) * Math.PI * 0.2f * movescale))
                + 0.6283185400806344 * movescale);

        // orig ModelGiantRobot.java:168-171 — hip bob (4px at twice the walk frequency) and hip rotation.
        float bob = (float) (Math.cos(-ageInTicks * WING_SPEED * 2.0f) * movescale);
        this.hip.y = HIP_BASE_Y + bob * 4.0f;
        this.hip.xRot = hipXAngle;
        this.hip.yRot = (float) ((double) hipYAngle + Math.PI / 2.0);

        // orig ModelGiantRobot.java:227-240 — arms follow the thigh swing when idle,
        // windmill punch + shoulder twist when getAttacking() != 0.
        float shoulderAngle = -hipYAngle;
        this.armA1Angle = this.armA2Angle = this.thighAngle1;
        this.armB1Angle = this.armB2Angle = this.thighAngle0;
        if (entity.getAttacking() != 0) {
            shoulderAngle = (float) (-(Math.sin(ageInTicks * WING_SPEED * 2.0f) * Math.PI * (double) 0.2f));
            this.armA1Angle = (float) ((double) ((float) (Math.sin(ageInTicks * WING_SPEED * 2.0f) * Math.PI / 5.0)) - 0.7853981633974483);
            this.armA2Angle = (float) ((double) (-this.armA1Angle) + Math.PI);
            this.armA1Angle = (float) ((double) this.armA1Angle + 0.6283185307179586);
            this.armA2Angle = (float) ((double) this.armA2Angle + 0.6283185307179586);
            this.armB1Angle = (float) ((double) ((float) (-(Math.sin(ageInTicks * WING_SPEED * 2.0f) * Math.PI / 5.0))) - 0.7853981633974483);
            this.armB2Angle = (float) ((double) (-this.armB1Angle) + Math.PI);
            this.armB1Angle = (float) ((double) this.armB1Angle + 0.6283185307179586);
            this.armB2Angle = (float) ((double) this.armB2Angle + 0.6283185307179586);
        }

        // orig ModelGiantRobot.java:241-242 — torso counter-twist.
        this.back3.yRot = shoulderAngle / 2.0f;
        this.shoulders.yRot = shoulderAngle;

        // orig ModelGiantRobot.java:267-272 — torso parts ride the hip bob; head look.
        this.back2.y = this.back3.y = this.hip.y;
        this.back1.y = this.back3.y;
        this.neck.y = this.head.y = this.hip.y;
        this.shoulders.y = this.head.y;
        this.head.yRot = (float) Math.toRadians(netHeadYaw);
        this.head.xRot = (float) Math.toRadians(headPitch) / 3.0f;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        // orig ModelGiantRobot.java:172 — hip first.
        hip.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);

        // orig ModelGiantRobot.java:173-199 — first leg; :200-226 — second leg
        // re-rendered from the same shared parts with mirrored hip offsets.
        renderLeg(poseStack, vertexConsumer, packedLight, packedOverlay, color, thighAngle0, shinAngle0, 1.0f);
        renderLeg(poseStack, vertexConsumer, packedLight, packedOverlay, color, thighAngle1, shinAngle1, -1.0f);

        // orig ModelGiantRobot.java:243-254 — right arm; :255-266 — left arm
        // re-rendered from the same shared parts with mirrored shoulder offsets.
        renderArm(poseStack, vertexConsumer, packedLight, packedOverlay, color, armA1Angle, armA2Angle, 1.0f);
        renderArm(poseStack, vertexConsumer, packedLight, packedOverlay, color, armB1Angle, armB2Angle, -1.0f);

        // orig ModelGiantRobot.java:273-278 — torso, shoulders, neck, head last.
        back1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        back2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        back3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        shoulders.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        neck.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        head.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }

    /**
     * Poses and renders the shared thigh/shin/foot parts for one leg.
     * Mirrors orig ModelGiantRobot.java:173-199 (sign +1, first leg) and
     * :200-226 (sign -1, second leg — all three hip offsets negated).
     */
    private void renderLeg(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color,
                           float thighAngle, float shinAngle, float sign) {
        this.thigh2.xRot = this.thigh3.xRot = thighAngle;
        this.thigh.xRot = this.thigh3.xRot;
        this.thigh2.y = this.thigh3.y = this.hip.y - sign * Mth.sin(this.hip.xRot) * 13.0f;
        this.thigh.y = this.thigh3.y;
        this.thigh2.z = this.thigh3.z = this.hip.z + sign * Mth.cos(this.hip.xRot) * Mth.cos(this.hip.yRot) * 13.0f;
        this.thigh.z = this.thigh3.z;
        this.thigh2.x = this.thigh3.x = this.hip.x + sign * Mth.cos(this.hip.xRot) * Mth.sin(this.hip.yRot) * 13.0f;
        this.thigh.x = this.thigh3.x;
        thigh.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        thigh2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        thigh3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);

        this.shin.xRot = shinAngle;
        this.shin.y = this.thigh.y + Mth.cos(this.thigh.xRot) * 40.0f;
        this.shin.z = this.thigh.z + Mth.sin(this.thigh.xRot) * 40.0f;
        this.shin.x = this.thigh.x;
        shin.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);

        this.foot2.xRot = this.foot3.xRot = shinAngle;
        this.foot1.xRot = this.foot3.xRot;
        this.foot2.y = this.foot3.y = this.shin.y;
        this.foot1.y = this.foot3.y;
        this.foot2.z = this.foot3.z = this.shin.z;
        this.foot1.z = this.foot3.z;
        this.foot2.x = this.foot3.x = this.shin.x;
        this.foot1.x = this.foot3.x;
        foot1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        foot2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        foot3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }

    /**
     * Poses and renders the shared arm parts for one side.
     * Mirrors orig ModelGiantRobot.java:243-254 (sign +1: x offset +26,
     * z offset {@code -sin(shoulders.yRot)*26}) and :255-266 (sign -1).
     */
    private void renderArm(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color,
                           float upperAngle, float lowerAngle, float sign) {
        this.arm1.y = this.arm2.y = this.hip.y - 60.0f;
        this.arm1.x = this.arm2.x = this.hip.x + sign * 26.0f;
        this.arm1.z = this.arm2.z = this.shoulders.z - sign * Mth.sin(this.shoulders.yRot) * 26.0f;
        this.arm1.xRot = this.arm2.xRot = upperAngle;
        arm1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        arm2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);

        // orig ModelGiantRobot.java:249 — forearm leads the upper arm by -0.19634954 rad.
        this.arm3.xRot = this.knuckles.xRot = (float) ((double) lowerAngle - 0.19634954084936207);
        this.arm3.y = this.knuckles.y = this.arm1.y + Mth.cos(this.arm1.xRot) * 41.0f;
        this.arm3.z = this.knuckles.z = this.arm1.z + Mth.sin(this.arm1.xRot) * 41.0f;
        this.arm3.x = this.knuckles.x = this.arm1.x;
        arm3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        knuckles.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
