package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import danger.orespawn.entity.GiantRobot;

public class ModelGiantRobot extends EntityModel<GiantRobot> {
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

    @Override
    public void setupAnim(GiantRobot entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.yRot = (float) Math.toRadians(netHeadYaw);
        this.head.xRot = (float) Math.toRadians(headPitch) / 3.0F;

        // Subtle idle arm sway so the model doesn't look completely frozen
        float idleSway = Mth.cos(ageInTicks * 0.05F) * 0.04F;
        this.arm1.xRot = idleSway;
        this.arm2.xRot = idleSway;
        this.arm3.xRot = idleSway;
        this.knuckles.xRot = idleSway;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        hip.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        thigh.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        shin.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        foot1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        foot2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        foot3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        thigh2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        thigh3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        back1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        back2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        back3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        shoulders.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        neck.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        head.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        arm1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        arm2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        arm3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        knuckles.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
