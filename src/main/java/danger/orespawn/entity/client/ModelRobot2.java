package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import danger.orespawn.entity.Robot2;

public class ModelRobot2 extends EntityModel<Robot2> {
    private final ModelPart rleg1;
    private final ModelPart rleg2;
    private final ModelPart Shape3;
    private final ModelPart lleg2;
    private final ModelPart lleg1;
    private final ModelPart Shape6;
    private final ModelPart Shape7;
    private final ModelPart Shape8;
    private final ModelPart rarm3;
    private final ModelPart rarm2;
    private final ModelPart rarm1;
    private final ModelPart larm3;
    private final ModelPart larm2;
    private final ModelPart larm1;
    private final ModelPart head;

    public ModelRobot2(ModelPart root) {
        this.rleg1 = root.getChild("rleg1");
        this.rleg2 = root.getChild("rleg2");
        this.Shape3 = root.getChild("Shape3");
        this.lleg2 = root.getChild("lleg2");
        this.lleg1 = root.getChild("lleg1");
        this.Shape6 = root.getChild("Shape6");
        this.Shape7 = root.getChild("Shape7");
        this.Shape8 = root.getChild("Shape8");
        this.rarm3 = root.getChild("rarm3");
        this.rarm2 = root.getChild("rarm2");
        this.rarm1 = root.getChild("rarm1");
        this.larm3 = root.getChild("larm3");
        this.larm2 = root.getChild("larm2");
        this.larm1 = root.getChild("larm1");
        this.head = root.getChild("head");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("rleg1",
                CubeListBuilder.create().texOffs(10, 250).mirror()
                        .addBox(-14.0F, 24.0F, -7.0F, 16, 24, 16),
                PartPose.offset(-10.0F, -24.0F, 0.0F));

        partdefinition.addOrReplaceChild("rleg2",
                CubeListBuilder.create().texOffs(10, 150).mirror()
                        .addBox(-12.0F, 0.0F, -6.0F, 12, 24, 12),
                PartPose.offset(-10.0F, -24.0F, 1.0F));

        partdefinition.addOrReplaceChild("Shape3",
                CubeListBuilder.create().texOffs(10, 50).mirror()
                        .addBox(-4.0F, 0.0F, -2.0F, 26, 8, 12),
                PartPose.offset(-9.0F, -32.0F, -3.0F));

        partdefinition.addOrReplaceChild("lleg2",
                CubeListBuilder.create().texOffs(10, 200).mirror()
                        .addBox(0.0F, 0.0F, -6.0F, 12, 24, 12),
                PartPose.offset(10.0F, -24.0F, 1.0F));

        partdefinition.addOrReplaceChild("lleg1",
                CubeListBuilder.create().texOffs(10, 300).mirror()
                        .addBox(-2.0F, 24.0F, -7.0F, 16, 24, 16),
                PartPose.offset(10.0F, -24.0F, 0.0F));

        partdefinition.addOrReplaceChild("Shape6",
                CubeListBuilder.create().texOffs(10, 100).mirror()
                        .addBox(-4.0F, -8.0F, -3.0F, 8, 8, 8),
                PartPose.offset(0.0F, -32.0F, 0.0F));

        partdefinition.addOrReplaceChild("Shape7",
                CubeListBuilder.create().texOffs(10, 350).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 26, 8, 12),
                PartPose.offset(-13.0F, -48.0F, -5.0F));

        partdefinition.addOrReplaceChild("Shape8",
                CubeListBuilder.create().texOffs(16, 400).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 44, 18, 14),
                PartPose.offset(-22.0F, -66.0F, -6.0F));

        partdefinition.addOrReplaceChild("rarm3",
                CubeListBuilder.create().texOffs(100, 100).mirror()
                        .addBox(-16.0F, -16.0F, -7.0F, 16, 24, 17),
                PartPose.offset(-22.0F, -58.0F, 0.0F));

        partdefinition.addOrReplaceChild("rarm2",
                CubeListBuilder.create().texOffs(100, 200).mirror()
                        .addBox(-14.0F, 8.0F, -5.0F, 12, 24, 12),
                PartPose.offset(-22.0F, -58.0F, 0.0F));

        partdefinition.addOrReplaceChild("rarm1",
                CubeListBuilder.create().texOffs(100, 300).mirror()
                        .addBox(-14.0F, 32.0F, -5.0F, 12, 24, 12),
                PartPose.offset(-22.0F, -58.0F, 0.0F));

        partdefinition.addOrReplaceChild("larm3",
                CubeListBuilder.create().texOffs(100, 50).mirror()
                        .addBox(0.0F, -16.0F, -7.0F, 16, 24, 17),
                PartPose.offset(22.0F, -58.0F, 0.0F));

        partdefinition.addOrReplaceChild("larm2",
                CubeListBuilder.create().texOffs(100, 150).mirror()
                        .addBox(2.0F, 8.0F, -5.0F, 12, 24, 12),
                PartPose.offset(21.0F, -58.0F, 0.0F));

        partdefinition.addOrReplaceChild("larm1",
                CubeListBuilder.create().texOffs(100, 250).mirror()
                        .addBox(2.0F, 32.0F, -5.0F, 12, 24, 12),
                PartPose.offset(21.0F, -58.0F, 0.0F));

        partdefinition.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(50, 10).mirror()
                        .addBox(-7.0F, -12.0F, -5.0F, 15, 12, 10),
                PartPose.offset(0.0F, -66.0F, 1.0F));

        return LayerDefinition.create(meshdefinition, 256, 512);
    }

    @Override
    public void setupAnim(Robot2 entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float walkAngle = limbSwingAmount > 0.1F
                ? Mth.cos(limbSwing * 0.3F) * (float) Math.PI * 0.12F * limbSwingAmount
                : 0.0F;
        this.lleg1.xRot = walkAngle;
        this.lleg2.xRot = walkAngle;
        this.rleg1.xRot = -walkAngle;
        this.rleg2.xRot = -walkAngle;

        this.head.yRot = (float) Math.toRadians(netHeadYaw);

        float armAngle = (float) Math.toRadians(ageInTicks * 20.0F);
        this.rarm1.xRot = armAngle;
        this.rarm2.xRot = armAngle;
        this.rarm3.xRot = armAngle;
        this.larm1.xRot = armAngle;
        this.larm2.xRot = armAngle;
        this.larm3.xRot = armAngle;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.rleg1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rleg2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Shape3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lleg2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lleg1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Shape6.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Shape7.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Shape8.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rarm3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rarm2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rarm1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.larm3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.larm2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.larm1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.head.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
