package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.Crab;
import danger.orespawn.entity.pose.CrabPose;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
/**
 * BUG-041 stage 2 (2026-09-13): the 1.7.10 export sets {@code mirror = true} AFTER {@code addBox} (orig ModelCrab.java:
 * 25 stores, all inert - 1.7.10's ModelBox reads the flag in its constructor, law 11 from Mojang's 1.7.10 jar),
 * so the original rendered UNMIRRORED. The port's 25 {@code .mirror()} calls preceded {@code addBox} and flipped
 * every face's U: dropped port-wide as the EnderReaper precedent was (5354420); geometry unchanged; proven by the
 * reference-geometry leg.
 *
 * <p>ANIM-025, THE DRAW FIX (the fourth Tier-2 slice T2d): 1.7.10's {@code render} (orig ModelCrab.java:195-289)
 * set the three leg parts' rotation point and yaw and DREW them at each of eight poses - four on the left side (x
 * 36) at z 0 / 10 / 20 / 30 with yaw -pi/2 + a, -pi/2 - a, -pi/2 + a, -pi/2 - a, then four on the right (x -36) with the
 * yaw negated, {@code a = cos(f2 * 1.7f) * PI * 0.15f * f1} - eight three-segment legs. The port had moved the eight
 * re-poses into {@code setupAnim} and drew each part ONCE at the last pose (one leg). Now {@link #poseFrom} keeps the
 * two inputs those poses read ({@code f1} = limbSwingAmount, {@code f2} = ageInTicks) and {@link
 * #renderToBuffer} re-poses and draws {@code leg1}, {@code leg2}, {@code leg3} eight times from them with the
 * classic's own expressions, in the original's order, before the body parts - as the original did. A fresh model (no
 * {@code setupAnim} yet) draws the legs at a swing of 0: yaw -+pi/2 exactly, the reference entry's declared bind
 * transforms ({@code reference_crab}, {@code render_instances}, twenty-four leg bones). orig :310-311 drew
 * {@code leg2} and {@code leg3} a NINTH time at the eighth pose - a coincident duplicate (the same pose, the same
 * triangles) the decided declaration's count of eight per part does not carry; not reproduced.</p>
 *
 */

public class ModelCrab extends EntityModel<Crab> {
    private final ModelPart body1;
    private final ModelPart body2;
    private final ModelPart leg1;
    private final ModelPart body3;
    private final ModelPart body4;
    private final ModelPart leg2;
    private final ModelPart leg3;
    private final ModelPart body5;
    private final ModelPart body6;
    private final ModelPart Leye1;
    private final ModelPart Reye1;
    private final ModelPart Leye2;
    private final ModelPart Reye2;
    private final ModelPart Lclaw1;
    private final ModelPart Lclaw2;
    private final ModelPart Lclaw3;
    private final ModelPart Lclaw4;
    private final ModelPart Lclaw5;
    private final ModelPart Rclaw1;
    private final ModelPart Rclaw2;
    private final ModelPart Rclaw3;
    private final ModelPart Rclaw4;
    private final ModelPart Rclaw5;
    private final ModelPart Rmouth;
    private final ModelPart Lmouth;
    /**
     * The two inputs 1.7.10's {@code render} read for the eight leg poses (orig ModelCrab.java:199-274: {@code f1} the
     * walking speed, {@code f2} the age), kept by {@link #poseFrom} for {@link #renderToBuffer}'s draw loop (ANIM-025). A
     * fresh model holds 0 / 0: the legs at a swing of 0, the bind.
     */
    private float legLimbSwingAmount;
    private float legAgeInTicks;

    public ModelCrab(ModelPart root) {
        this.body1 = root.getChild("body1");
        this.body2 = root.getChild("body2");
        this.leg1 = root.getChild("leg1");
        this.body3 = root.getChild("body3");
        this.body4 = root.getChild("body4");
        this.leg2 = root.getChild("leg2");
        this.leg3 = root.getChild("leg3");
        this.body5 = root.getChild("body5");
        this.body6 = root.getChild("body6");
        this.Leye1 = root.getChild("Leye1");
        this.Reye1 = root.getChild("Reye1");
        this.Leye2 = root.getChild("Leye2");
        this.Reye2 = root.getChild("Reye2");
        this.Lclaw1 = root.getChild("Lclaw1");
        this.Lclaw2 = root.getChild("Lclaw2");
        this.Lclaw3 = root.getChild("Lclaw3");
        this.Lclaw4 = root.getChild("Lclaw4");
        this.Lclaw5 = root.getChild("Lclaw5");
        this.Rclaw1 = root.getChild("Rclaw1");
        this.Rclaw2 = root.getChild("Rclaw2");
        this.Rclaw3 = root.getChild("Rclaw3");
        this.Rclaw4 = root.getChild("Rclaw4");
        this.Rclaw5 = root.getChild("Rclaw5");
        this.Rmouth = root.getChild("Rmouth");
        this.Lmouth = root.getChild("Lmouth");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("body1",
                CubeListBuilder.create().texOffs(0, 450).addBox(-38.0F, -5.0F, -8.0F, 76, 10, 48),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("body2",
                CubeListBuilder.create().texOffs(0, 406).addBox(-32.0F, -10.0F, -10.0F, 64, 5, 34),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("leg1",
                CubeListBuilder.create().texOffs(128, 0).addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4),
                PartPose.offsetAndRotation(36.0F, 3.0F, 0.0F, -1.343904F, -1.500983F, 0.0F));

        partdefinition.addOrReplaceChild("body3",
                CubeListBuilder.create().texOffs(0, 357).addBox(0.0F, 0.0F, 0.0F, 8, 4, 40),
                PartPose.offset(38.0F, -5.0F, -6.0F));

        partdefinition.addOrReplaceChild("body4",
                CubeListBuilder.create().texOffs(100, 357).addBox(0.0F, 0.0F, 0.0F, 8, 4, 40),
                PartPose.offset(-46.0F, -5.0F, -6.0F));

        partdefinition.addOrReplaceChild("leg2",
                CubeListBuilder.create().texOffs(128, 20).addBox(-1.0F, 10.0F, -6.0F, 3, 16, 3),
                PartPose.offsetAndRotation(36.0F, 3.0F, 0.0F, -0.9599311F, -1.500983F, 0.0F));

        partdefinition.addOrReplaceChild("leg3",
                CubeListBuilder.create().texOffs(128, 43).addBox(0.0F, 21.0F, -15.0F, 2, 16, 2),
                PartPose.offsetAndRotation(36.0F, 3.0F, 0.0F, -0.5759587F, -1.500983F, 0.0F));

        partdefinition.addOrReplaceChild("body5",
                CubeListBuilder.create().texOffs(0, 339).addBox(-25.0F, 0.0F, 0.0F, 50, 4, 10),
                PartPose.offset(0.0F, -4.0F, 40.0F));

        partdefinition.addOrReplaceChild("body6",
                CubeListBuilder.create().texOffs(124, 342).addBox(-14.0F, 0.0F, 0.0F, 28, 3, 4),
                PartPose.offset(0.0F, -10.0F, -14.0F));

        partdefinition.addOrReplaceChild("Leye1",
                CubeListBuilder.create().texOffs(62, 0).addBox(-0.5F, -12.0F, -0.5F, 1, 12, 1),
                PartPose.offsetAndRotation(9.0F, -9.0F, -11.0F, 0.0F, 0.0F, 0.4886922F));

        partdefinition.addOrReplaceChild("Reye1",
                CubeListBuilder.create().texOffs(40, 0).addBox(-0.5F, -12.0F, -0.5F, 1, 12, 1),
                PartPose.offsetAndRotation(-9.0F, -9.0F, -11.0F, 0.0F, 0.0F, -0.4886922F));

        partdefinition.addOrReplaceChild("Leye2",
                CubeListBuilder.create().texOffs(50, 0).addBox(-1.0F, -14.0F, -1.0F, 2, 2, 2),
                PartPose.offsetAndRotation(9.0F, -9.0F, -11.0F, 0.0F, 0.0F, 0.4886922F));

        partdefinition.addOrReplaceChild("Reye2",
                CubeListBuilder.create().texOffs(26, 0).addBox(-1.0F, -14.0F, -1.0F, 2, 2, 2),
                PartPose.offsetAndRotation(-9.0F, -9.0F, -11.0F, 0.0F, 0.0F, -0.4886922F));

        partdefinition.addOrReplaceChild("Lclaw1",
                CubeListBuilder.create().texOffs(0, 80).addBox(-4.0F, 0.0F, -14.0F, 8, 4, 18),
                PartPose.offsetAndRotation(31.0F, -2.0F, -8.0F, 0.0F, -0.4886922F, 0.0F));

        partdefinition.addOrReplaceChild("Lclaw2",
                CubeListBuilder.create().texOffs(0, 105).addBox(-7.0F, -3.0F, -12.0F, 17, 6, 16),
                PartPose.offsetAndRotation(37.0F, 0.0F, -20.0F, 0.0F, -0.1745329F, 0.0F));

        partdefinition.addOrReplaceChild("Lclaw3",
                CubeListBuilder.create().texOffs(0, 131).addBox(0.0F, -5.0F, -25.0F, 17, 10, 30),
                PartPose.offsetAndRotation(37.0F, 0.0F, -31.0F, 0.0F, -0.4537856F, 0.0F));

        partdefinition.addOrReplaceChild("Lclaw4",
                CubeListBuilder.create().texOffs(0, 175).addBox(2.0F, -3.0F, -32.0F, 11, 5, 12),
                PartPose.offsetAndRotation(37.0F, 0.0F, -31.0F, 0.0F, -0.3490659F, 0.0F));

        partdefinition.addOrReplaceChild("Lclaw5",
                CubeListBuilder.create().texOffs(0, 197).addBox(-4.0F, -3.0F, -27.0F, 7, 5, 32),
                PartPose.offsetAndRotation(36.0F, 0.0F, -31.0F, 0.0F, 0.3839724F, 0.0F));

        partdefinition.addOrReplaceChild("Rclaw1",
                CubeListBuilder.create().texOffs(102, 78).addBox(-4.0F, 0.0F, -14.0F, 8, 4, 18),
                PartPose.offsetAndRotation(-31.0F, -2.0F, -8.0F, 0.0F, 0.4886922F, 0.0F));

        partdefinition.addOrReplaceChild("Rclaw2",
                CubeListBuilder.create().texOffs(103, 106).addBox(-10.0F, -3.0F, -12.0F, 17, 6, 16),
                PartPose.offsetAndRotation(-37.0F, 0.0F, -20.0F, 0.0F, 0.1745329F, 0.0F));

        partdefinition.addOrReplaceChild("Rclaw3",
                CubeListBuilder.create().texOffs(100, 131).addBox(-17.0F, -5.0F, -25.0F, 17, 10, 30),
                PartPose.offsetAndRotation(-37.0F, 0.0F, -31.0F, 0.0F, 0.4537856F, 0.0F));

        partdefinition.addOrReplaceChild("Rclaw4",
                CubeListBuilder.create().texOffs(101, 175).addBox(-13.0F, -3.0F, -32.0F, 11, 5, 12),
                PartPose.offsetAndRotation(-37.0F, 0.0F, -31.0F, 0.0F, 0.3490659F, 0.0F));

        partdefinition.addOrReplaceChild("Rclaw5",
                CubeListBuilder.create().texOffs(100, 197).addBox(-4.0F, -3.0F, -27.0F, 7, 5, 32),
                PartPose.offsetAndRotation(-36.0F, 0.0F, -31.0F, 0.0F, -0.3839724F, 0.0F));

        partdefinition.addOrReplaceChild("Rmouth",
                CubeListBuilder.create().texOffs(0, 28).addBox(0.0F, 0.0F, -0.5F, 6, 3, 1),
                PartPose.offsetAndRotation(-7.0F, 0.0F, -7.5F, 0.0F, 0.3665191F, 0.0F));

        partdefinition.addOrReplaceChild("Lmouth",
                CubeListBuilder.create().texOffs(0, 19).addBox(-6.0F, 0.0F, -0.5F, 6, 3, 1),
                PartPose.offsetAndRotation(7.0F, 0.0F, -7.5F, 0.0F, -0.3665191F, 0.0F));

        return LayerDefinition.create(meshdefinition, 256, 512);
    }

    @Override
    public void setupAnim(Crab entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        // The body lives in poseFrom so the parity harness can drive it from a declared state without a live
        // entity (the Slice 4b form; the hooks): the entity satisfies the interface.
        poseFrom(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
    }

    /**
     * The classic pose over what the model reads from its entity (orig ModelCrab.java:275 {@code getAttacking()}). The
     * eight leg poses live in {@link #renderToBuffer}'s draw loop, where 1.7.10 wrote them between its draws (orig
     * :199-274); this keeps the two inputs they read (ANIM-025, the Crab's slice).
     */
    public void poseFrom(CrabPose entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.legLimbSwingAmount = limbSwingAmount;
        this.legAgeInTicks = ageInTicks;
        if (entity.getAttacking() == 0) {
        this.Leye1.xRot = this.Leye2.xRot = Mth.cos((float)(ageInTicks * 0.35f)) * (float)Math.PI * 0.05f;
        this.Leye1.zRot = this.Leye2.zRot = 0.54f + Mth.cos((float)(ageInTicks * 0.25f)) * (float)Math.PI * 0.05f;
        this.Reye1.xRot = this.Reye2.xRot = Mth.cos((float)(ageInTicks * 0.3f)) * (float)Math.PI * 0.05f;
        this.Reye1.zRot = this.Reye2.zRot = -0.54f + Mth.cos((float)(ageInTicks * 0.45f)) * (float)Math.PI * 0.05f;
        this.Lmouth.yRot = -0.72f + Mth.cos((float)(ageInTicks * 0.25f)) * (float)Math.PI * 0.05f;
        this.Rmouth.yRot = 0.72f - Mth.cos((float)(ageInTicks * 0.25f)) * (float)Math.PI * 0.05f;
        float newangle = Mth.cos((float)(ageInTicks * 0.15f)) * (float)Math.PI * 0.03f;
        this.Lclaw3.yRot = -0.453f + newangle;
        this.Lclaw4.yRot = -0.349f + newangle;
        this.Lclaw5.yRot = 0.384f - newangle;
        newangle = Mth.cos((float)(ageInTicks * 0.13f)) * (float)Math.PI * 0.02f;
        this.Rclaw3.yRot = 0.453f + newangle;
        this.Rclaw4.yRot = 0.349f + newangle;
        this.Rclaw5.yRot = -0.384f - newangle;
        } else {
        this.Leye1.xRot = this.Leye2.xRot = Mth.cos((float)(ageInTicks * 0.45f)) * (float)Math.PI * 0.1f;
        this.Leye1.zRot = this.Leye2.zRot = 0.54f + Mth.cos((float)(ageInTicks * 0.35f)) * (float)Math.PI * 0.1f;
        this.Reye1.xRot = this.Reye2.xRot = Mth.cos((float)(ageInTicks * 0.4f)) * (float)Math.PI * 0.1f;
        this.Reye1.zRot = this.Reye2.zRot = -0.54f + Mth.cos((float)(ageInTicks * 0.55f)) * (float)Math.PI * 0.1f;
        this.Lmouth.yRot = -0.72f + Mth.cos((float)(ageInTicks * 0.45f)) * (float)Math.PI * 0.15f;
        this.Rmouth.yRot = 0.72f - Mth.cos((float)(ageInTicks * 0.45f)) * (float)Math.PI * 0.15f;
        float newangle = Mth.cos((float)(ageInTicks * 0.35f)) * (float)Math.PI * 0.13f;
        this.Lclaw3.yRot = -0.453f + newangle;
        this.Lclaw4.yRot = -0.349f + newangle;
        this.Lclaw5.yRot = 0.384f - newangle;
        newangle = Mth.cos((float)(ageInTicks * 0.43f)) * (float)Math.PI * 0.12f;
        this.Rclaw3.yRot = 0.453f + newangle;
        this.Rclaw4.yRot = 0.349f + newangle;
        this.Rclaw5.yRot = -0.384f - newangle;
        }
    }

    /**
     * orig ModelCrab.java:199-274 (ANIM-025, the Crab's slice): the three leg parts re-posed and drawn at each of the eight
     * poses - the classic's own expressions from the inputs {@link #poseFrom} kept ({@code f1} = limbSwingAmount, {@code f2}
     * = ageInTicks) - then the body parts in the original's order (orig :306-329, less the ninth coincident draw of
     * {@code leg2} / {@code leg3} at :310-311: the same pose, the same triangles; the reference declaration's count is eight).
     */
    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        float limbSwingAmount = this.legLimbSwingAmount;
        float ageInTicks = this.legAgeInTicks;
        // pose 1 of 8 (orig :199-212): the left side, z 0, yaw -pi/2 + a
        this.leg3.x = 36.0f;
        this.leg2.x = 36.0f;
        this.leg1.x = 36.0f;
        this.leg3.y = 3.0f;
        this.leg2.y = 3.0f;
        this.leg1.y = 3.0f;
        this.leg3.z = 0.0f;
        this.leg2.z = 0.0f;
        this.leg1.z = 0.0f;
        this.leg2.yRot = this.leg3.yRot = (float)(-1.5707963267948966 + (double)(Mth.cos((float)(ageInTicks * 1.7f)) * (float)Math.PI * 0.15f * limbSwingAmount));
        this.leg1.yRot = this.leg3.yRot;
        this.leg1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        // pose 2 of 8 (orig :213-220): z 10, yaw -pi/2 - a
        this.leg3.z = 10.0f;
        this.leg2.z = 10.0f;
        this.leg1.z = 10.0f;
        this.leg2.yRot = this.leg3.yRot = (float)(-1.5707963267948966 - (double)(Mth.cos((float)(ageInTicks * 1.7f)) * (float)Math.PI * 0.15f * limbSwingAmount));
        this.leg1.yRot = this.leg3.yRot;
        this.leg1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        // pose 3 of 8 (orig :221-228): z 20, yaw -pi/2 + a
        this.leg3.z = 20.0f;
        this.leg2.z = 20.0f;
        this.leg1.z = 20.0f;
        this.leg2.yRot = this.leg3.yRot = (float)(-1.5707963267948966 + (double)(Mth.cos((float)(ageInTicks * 1.7f)) * (float)Math.PI * 0.15f * limbSwingAmount));
        this.leg1.yRot = this.leg3.yRot;
        this.leg1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        // pose 4 of 8 (orig :229-236): z 30, yaw -pi/2 - a
        this.leg3.z = 30.0f;
        this.leg2.z = 30.0f;
        this.leg1.z = 30.0f;
        this.leg2.yRot = this.leg3.yRot = (float)(-1.5707963267948966 - (double)(Mth.cos((float)(ageInTicks * 1.7f)) * (float)Math.PI * 0.15f * limbSwingAmount));
        this.leg1.yRot = this.leg3.yRot;
        this.leg1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        // pose 5 of 8 (orig :237-250): the right side (x -36), z 0, yaw -(-pi/2 + a)
        this.leg3.x = -36.0f;
        this.leg2.x = -36.0f;
        this.leg1.x = -36.0f;
        this.leg3.y = 3.0f;
        this.leg2.y = 3.0f;
        this.leg1.y = 3.0f;
        this.leg3.z = 0.0f;
        this.leg2.z = 0.0f;
        this.leg1.z = 0.0f;
        this.leg2.yRot = this.leg3.yRot = (float)(-(-1.5707963267948966 + (double)(Mth.cos((float)(ageInTicks * 1.7f)) * (float)Math.PI * 0.15f * limbSwingAmount)));
        this.leg1.yRot = this.leg3.yRot;
        this.leg1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        // pose 6 of 8 (orig :251-258): z 10, yaw -(-pi/2 - a)
        this.leg3.z = 10.0f;
        this.leg2.z = 10.0f;
        this.leg1.z = 10.0f;
        this.leg2.yRot = this.leg3.yRot = (float)(-(-1.5707963267948966 - (double)(Mth.cos((float)(ageInTicks * 1.7f)) * (float)Math.PI * 0.15f * limbSwingAmount)));
        this.leg1.yRot = this.leg3.yRot;
        this.leg1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        // pose 7 of 8 (orig :259-266): z 20, yaw -(-pi/2 + a)
        this.leg3.z = 20.0f;
        this.leg2.z = 20.0f;
        this.leg1.z = 20.0f;
        this.leg2.yRot = this.leg3.yRot = (float)(-(-1.5707963267948966 + (double)(Mth.cos((float)(ageInTicks * 1.7f)) * (float)Math.PI * 0.15f * limbSwingAmount)));
        this.leg1.yRot = this.leg3.yRot;
        this.leg1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        // pose 8 of 8 (orig :267-274): z 30, yaw -(-pi/2 - a)
        this.leg3.z = 30.0f;
        this.leg2.z = 30.0f;
        this.leg1.z = 30.0f;
        this.leg2.yRot = this.leg3.yRot = (float)(-(-1.5707963267948966 - (double)(Mth.cos((float)(ageInTicks * 1.7f)) * (float)Math.PI * 0.15f * limbSwingAmount)));
        this.leg1.yRot = this.leg3.yRot;
        this.leg1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        // orig :306-329: the body parts (the ninth draw of leg2 / leg3 at :310-311 not reproduced, above)
        this.body1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.body2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.body3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.body4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.body5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.body6.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Leye1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Reye1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Leye2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Reye2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Lclaw1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Lclaw2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Lclaw3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Lclaw4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Lclaw5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Rclaw1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Rclaw2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Rclaw3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Rclaw4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Rclaw5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Rmouth.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Lmouth.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
