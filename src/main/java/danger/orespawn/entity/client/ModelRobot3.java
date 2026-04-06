package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

public class ModelRobot3 extends EntityModel<Entity> {
    private final ModelPart rleg1;
    private final ModelPart lleg1;
    private final ModelPart rleg2;
    private final ModelPart lleg2;
    private final ModelPart hips;
    private final ModelPart waist1;
    private final ModelPart waist2;
    private final ModelPart body3;
    private final ModelPart lazer;
    private final ModelPart body2;
    private final ModelPart body1;
    private final ModelPart body4;
    private final ModelPart waist3;
    private final ModelPart larm3;
    private final ModelPart rarm3;
    private final ModelPart larm2;
    private final ModelPart rarm2;
    private final ModelPart larm1;
    private final ModelPart rarm1;

    public ModelRobot3(ModelPart root) {
        this.rleg1 = root.getChild("rleg1");
        this.lleg1 = root.getChild("lleg1");
        this.rleg2 = root.getChild("rleg2");
        this.lleg2 = root.getChild("lleg2");
        this.hips = root.getChild("hips");
        this.waist1 = root.getChild("waist1");
        this.waist2 = root.getChild("waist2");
        this.body3 = root.getChild("body3");
        this.lazer = root.getChild("lazer");
        this.body2 = root.getChild("body2");
        this.body1 = root.getChild("body1");
        this.body4 = root.getChild("body4");
        this.waist3 = root.getChild("waist3");
        this.larm3 = root.getChild("larm3");
        this.rarm3 = root.getChild("rarm3");
        this.larm2 = root.getChild("larm2");
        this.rarm2 = root.getChild("rarm2");
        this.larm1 = root.getChild("larm1");
        this.rarm1 = root.getChild("rarm1");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("rleg1",
                CubeListBuilder.create().texOffs(20, 100).mirror()
                        .addBox(-23.0F, 26.0F, -8.0F, 16, 29, 16),
                PartPose.offset(-9.0F, -31.0F, 0.0F));

        partdefinition.addOrReplaceChild("lleg1",
                CubeListBuilder.create().texOffs(20, 159).mirror()
                        .addBox(7.0F, 25.0F, -8.0F, 16, 29, 16),
                PartPose.offset(9.0F, -30.0F, 0.0F));

        partdefinition.addOrReplaceChild("rleg2",
                CubeListBuilder.create().texOffs(20, 212).mirror()
                        .addBox(-14.0F, 0.0F, -7.0F, 14, 29, 14),
                PartPose.offsetAndRotation(-9.0F, -31.0F, 0.0F, 0.0F, 0.0F, 0.2792527F));

        partdefinition.addOrReplaceChild("lleg2",
                CubeListBuilder.create().texOffs(20, 265).mirror()
                        .addBox(0.0F, 0.0F, -7.0F, 13, 29, 14),
                PartPose.offsetAndRotation(9.0F, -31.0F, 0.0F, 0.0F, 0.0F, -0.2792527F));

        partdefinition.addOrReplaceChild("hips",
                CubeListBuilder.create().texOffs(20, 316).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 18, 16, 16),
                PartPose.offset(-9.0F, -43.0F, -8.0F));

        partdefinition.addOrReplaceChild("waist1",
                CubeListBuilder.create().texOffs(20, 359).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 12, 12, 12),
                PartPose.offsetAndRotation(-6.0F, -55.0F, -4.0F, -0.1F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("waist2",
                CubeListBuilder.create().texOffs(20, 391).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 12, 12, 12),
                PartPose.offset(-6.0F, -67.0F, -4.0F));

        partdefinition.addOrReplaceChild("body3",
                CubeListBuilder.create().texOffs(20, 426).mirror()
                        .addBox(-23.0F, -25.0F, 10.0F, 47, 47, 25),
                PartPose.offsetAndRotation(0.0F, -88.0F, -10.0F, 0.2F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("lazer",
                CubeListBuilder.create().texOffs(20, 50).mirror()
                        .addBox(-8.0F, -8.0F, -22.0F, 17, 16, 22),
                PartPose.offsetAndRotation(0.0F, -88.0F, -11.0F, 0.4F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("body2",
                CubeListBuilder.create().texOffs(101, 103).mirror()
                        .addBox(9.0F, -24.0F, -12.0F, 15, 47, 47),
                PartPose.offsetAndRotation(0.0F, -88.0F, -11.0F, 0.2F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("body1",
                CubeListBuilder.create().texOffs(101, 210).mirror()
                        .addBox(-23.0F, -24.0F, -12.0F, 15, 47, 47),
                PartPose.offsetAndRotation(0.0F, -88.0F, -11.0F, 0.2F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("body4",
                CubeListBuilder.create().texOffs(101, 321).mirror()
                        .addBox(-8.0F, -24.0F, -12.0F, 18, 16, 22),
                PartPose.offsetAndRotation(0.0F, -88.0F, -11.0F, 0.2F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("waist3",
                CubeListBuilder.create().texOffs(99, 375).mirror()
                        .addBox(0.0F, 0.0F, -1.0F, 12, 17, 12),
                PartPose.offsetAndRotation(-6.0F, -83.0F, -6.0F, 0.2F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("larm3",
                CubeListBuilder.create().texOffs(121, 54).mirror()
                        .addBox(0.0F, -10.0F, -9.0F, 20, 18, 18),
                PartPose.offsetAndRotation(24.0F, -92.0F, 2.0F, 1.0F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("rarm3",
                CubeListBuilder.create().texOffs(26, 8).mirror()
                        .addBox(-20.0F, -9.0F, -9.0F, 20, 18, 18),
                PartPose.offsetAndRotation(-23.0F, -92.0F, 2.0F, 1.0F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("larm2",
                CubeListBuilder.create().texOffs(207, 47).mirror()
                        .addBox(3.0F, 8.0F, -7.0F, 14, 29, 14),
                PartPose.offsetAndRotation(24.0F, -92.0F, 2.0F, 1.0F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("rarm2",
                CubeListBuilder.create().texOffs(161, 372).mirror()
                        .addBox(-17.0F, 9.0F, -7.0F, 14, 29, 14),
                PartPose.offsetAndRotation(-23.0F, -92.0F, 2.0F, 1.0F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("larm1",
                CubeListBuilder.create().texOffs(185, 433).mirror()
                        .addBox(0.0F, -12.0F, 30.0F, 14, 37, 14),
                PartPose.offsetAndRotation(27.0F, -92.0F, 2.0F, -1.0F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("rarm1",
                CubeListBuilder.create().texOffs(239, 105).mirror()
                        .addBox(-17.0F, -12.0F, 30.0F, 14, 37, 14),
                PartPose.offsetAndRotation(-23.0F, -92.0F, 2.0F, -1.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 512, 512);
    }

    @Override
    public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float walkAngle = limbSwingAmount > 0.1F
                ? Mth.cos(limbSwing * 0.55F) * (float) Math.PI * 0.12F * limbSwingAmount
                : 0.0F;
        this.lleg1.xRot = walkAngle;
        this.lleg2.xRot = walkAngle;
        this.rleg1.xRot = -walkAngle;
        this.rleg2.xRot = -walkAngle;

        this.lazer.yRot = (float) Math.toRadians(netHeadYaw / 2.0);

        float armSwing = Mth.cos(ageInTicks * 1.0F) * (float) Math.PI * 0.15F;
        this.rarm1.xRot = armSwing - 1.0F;
        this.rarm2.xRot = armSwing + 1.0F;
        this.rarm3.xRot = armSwing + 1.0F;
        this.larm1.xRot = armSwing - 1.0F;
        this.larm2.xRot = armSwing + 1.0F;
        this.larm3.xRot = armSwing + 1.0F;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.rleg1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lleg1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rleg2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lleg2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.hips.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.waist1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.waist2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.body3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lazer.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.body2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.body1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.body4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.waist3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.larm3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rarm3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.larm2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rarm2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.larm1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rarm1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
