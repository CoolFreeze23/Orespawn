package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.Fairy;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class FairyModel extends EntityModel<Fairy> {
    private final ModelPart head;
    private final ModelPart chest;
    private final ModelPart waist;
    private final ModelPart hips;
    private final ModelPart lleg1;
    private final ModelPart lleg2;
    private final ModelPart rleg;
    private final ModelPart b1;
    private final ModelPart b2;
    private final ModelPart larm;
    private final ModelPart rarm;
    private final ModelPart lwing2;
    private final ModelPart lwing1;
    private final ModelPart rwing2;
    private final ModelPart rwing1;

    public FairyModel(ModelPart root) {
        this.head = root.getChild("head");
        this.chest = root.getChild("chest");
        this.waist = root.getChild("waist");
        this.hips = root.getChild("hips");
        this.lleg1 = root.getChild("lleg1");
        this.lleg2 = root.getChild("lleg2");
        this.rleg = root.getChild("rleg");
        this.b1 = root.getChild("b1");
        this.b2 = root.getChild("b2");
        this.larm = root.getChild("larm");
        this.rarm = root.getChild("rarm");
        this.lwing2 = root.getChild("lwing2");
        this.lwing1 = root.getChild("lwing1");
        this.rwing2 = root.getChild("rwing2");
        this.rwing1 = root.getChild("rwing1");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 0).mirror()
                        .addBox(-2.5F, -5.0F, -2.5F, 5, 5, 5),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("chest",
                CubeListBuilder.create().texOffs(31, 5).mirror()
                        .addBox(-3.5F, 0.0F, -1.0F, 7, 4, 3),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("waist",
                CubeListBuilder.create().texOffs(33, 13).mirror()
                        .addBox(-2.5F, 4.0F, -1.0F, 5, 3, 3),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("hips",
                CubeListBuilder.create().texOffs(31, 20).mirror()
                        .addBox(-3.0F, 7.0F, -1.0F, 6, 4, 4),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("lleg1",
                CubeListBuilder.create().texOffs(53, 8).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 2, 7, 2),
                PartPose.offsetAndRotation(1.0F, 10.0F, 0.0F, -0.7853982F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("lleg2",
                CubeListBuilder.create().texOffs(53, 18).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 2, 8, 2),
                PartPose.offsetAndRotation(1.0F, 15.0F, -5.0F, 0.7679449F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("rleg",
                CubeListBuilder.create().texOffs(51, 30).mirror()
                        .addBox(-3.0F, 0.0F, 0.0F, 2, 13, 2),
                PartPose.offset(0.0F, 11.0F, 0.0F));

        partdefinition.addOrReplaceChild("b1",
                CubeListBuilder.create().texOffs(42, 1).mirror()
                        .addBox(1.0F, 1.0F, -2.0F, 2, 2, 1),
                PartPose.offset(0.0F, 1.0F, 0.0F));

        partdefinition.addOrReplaceChild("b2",
                CubeListBuilder.create().texOffs(32, 1).mirror()
                        .addBox(-3.0F, 2.0F, -2.0F, 2, 2, 1),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("larm",
                CubeListBuilder.create().texOffs(7, 14).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 1, 10, 1),
                PartPose.offsetAndRotation(3.0F, 0.0F, 0.0F, -0.0174533F, 0.0F, -0.122173F));

        partdefinition.addOrReplaceChild("rarm",
                CubeListBuilder.create().texOffs(2, 14).mirror()
                        .addBox(-1.0F, 0.0F, 0.0F, 1, 10, 1),
                PartPose.offsetAndRotation(-3.0F, 0.0F, 0.0F, -0.0174533F, 0.0F, 0.122173F));

        partdefinition.addOrReplaceChild("lwing2",
                CubeListBuilder.create().texOffs(0, 47).mirror()
                        .addBox(0.0F, -9.0F, 0.0F, 26, 16, 0),
                PartPose.offsetAndRotation(2.0F, 0.0F, 2.0F, 0.0F, -0.5934119F, 0.0F));

        partdefinition.addOrReplaceChild("lwing1",
                CubeListBuilder.create().texOffs(0, 30).mirror()
                        .addBox(0.0F, -7.0F, 0.0F, 24, 16, 0),
                PartPose.offsetAndRotation(2.0F, 3.0F, 2.0F, 0.0F, -0.8203047F, 0.0F));

        partdefinition.addOrReplaceChild("rwing2",
                CubeListBuilder.create().texOffs(0, 30).mirror()
                        .addBox(0.0F, -7.0F, 0.0F, 24, 16, 0),
                PartPose.offsetAndRotation(-2.0F, 3.0F, 2.0F, 0.0F, -2.356194F, 0.0F));

        partdefinition.addOrReplaceChild("rwing1",
                CubeListBuilder.create().texOffs(0, 47).mirror()
                        .addBox(0.0F, -9.0F, 0.0F, 26, 16, 0),
                PartPose.offsetAndRotation(-2.0F, 0.0F, 2.0F, 0.0F, -2.548181F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(Fairy entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.lwing1.yRot = -0.6f + Mth.cos((float)(ageInTicks * limbSwingAmount)) * (float)Math.PI * 0.35f;
        this.rwing1.yRot = -2.55f - Mth.cos((float)(ageInTicks * limbSwingAmount)) * (float)Math.PI * 0.35f;
        this.lwing2.yRot = -0.6f + Mth.cos((float)(ageInTicks * limbSwingAmount * 0.85f)) * (float)Math.PI * 0.25f;
        this.rwing2.yRot = -2.55f - Mth.cos((float)(ageInTicks * limbSwingAmount * 0.85f)) * (float)Math.PI * 0.25f;
        this.head.yRot = (float)Math.toRadians(netHeadYaw) * 0.45f;
        if (this.head.yRot > 0.45f) {
        this.head.yRot = 0.45f;
        }
        if (this.head.yRot < -0.45f) {
        this.head.yRot = -0.45f;
        }
        this.head.xRot = (float)Math.toRadians(headPitch);
        this.larm.xRot = -0.2f + Mth.cos((float)(ageInTicks * limbSwingAmount * 0.15f)) * (float)Math.PI * 0.05f;
        this.rarm.xRot = -0.2f + Mth.cos((float)(ageInTicks * limbSwingAmount * 0.12f)) * (float)Math.PI * 0.05f;
        this.larm.zRot = -0.15f + Mth.cos((float)(ageInTicks * limbSwingAmount * 0.1f)) * (float)Math.PI * 0.03f;
        this.rarm.zRot = 0.15f + Mth.cos((float)(ageInTicks * limbSwingAmount * 0.11f)) * (float)Math.PI * 0.03f;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.lwing2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lwing1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rwing2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rwing1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.head.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.chest.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.waist.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.hips.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lleg1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lleg2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rleg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.b1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.b2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.larm.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rarm.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
