package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.EntityMantis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class MantisModel extends EntityModel<EntityMantis> {
    private final ModelPart lfleg1;
    private final ModelPart lfleg2;
    private final ModelPart lfleg3;
    private final ModelPart lfleg4;
    private final ModelPart lrleg1;
    private final ModelPart lrleg2;
    private final ModelPart lrleg3;
    private final ModelPart lrleg4;
    private final ModelPart abdomen;
    private final ModelPart thorax;
    private final ModelPart neck1;
    private final ModelPart neck2;
    private final ModelPart head1;
    private final ModelPart head2;
    private final ModelPart leye;
    private final ModelPart reye;
    private final ModelPart lantenna;
    private final ModelPart rantenna;
    private final ModelPart larm1;
    private final ModelPart larm2;
    private final ModelPart larm3;
    private final ModelPart lfwing;
    private final ModelPart rfwing;
    private final ModelPart lrwing;
    private final ModelPart rrwing;
    private final ModelPart rarm1;
    private final ModelPart rarm2;
    private final ModelPart rarm3;
    private final ModelPart rlfleg3;
    private final ModelPart rfleg4;
    private final ModelPart rfleg2;
    private final ModelPart rfleg1;
    private final ModelPart rrleg3;
    private final ModelPart rrleg4;
    private final ModelPart rrleg2;
    private final ModelPart rrleg1;

    public MantisModel(ModelPart root) {
        this.lfleg1 = root.getChild("lfleg1");
        this.lfleg2 = root.getChild("lfleg2");
        this.lfleg3 = root.getChild("lfleg3");
        this.lfleg4 = root.getChild("lfleg4");
        this.lrleg1 = root.getChild("lrleg1");
        this.lrleg2 = root.getChild("lrleg2");
        this.lrleg3 = root.getChild("lrleg3");
        this.lrleg4 = root.getChild("lrleg4");
        this.abdomen = root.getChild("abdomen");
        this.thorax = root.getChild("thorax");
        this.neck1 = root.getChild("neck1");
        this.neck2 = root.getChild("neck2");
        this.head1 = root.getChild("head1");
        this.head2 = root.getChild("head2");
        this.leye = root.getChild("leye");
        this.reye = root.getChild("reye");
        this.lantenna = root.getChild("lantenna");
        this.rantenna = root.getChild("rantenna");
        this.larm1 = root.getChild("larm1");
        this.larm2 = root.getChild("larm2");
        this.larm3 = root.getChild("larm3");
        this.lfwing = root.getChild("lfwing");
        this.rfwing = root.getChild("rfwing");
        this.lrwing = root.getChild("lrwing");
        this.rrwing = root.getChild("rrwing");
        this.rarm1 = root.getChild("rarm1");
        this.rarm2 = root.getChild("rarm2");
        this.rarm3 = root.getChild("rarm3");
        this.rlfleg3 = root.getChild("rlfleg3");
        this.rfleg4 = root.getChild("rfleg4");
        this.rfleg2 = root.getChild("rfleg2");
        this.rfleg1 = root.getChild("rfleg1");
        this.rrleg3 = root.getChild("rrleg3");
        this.rrleg4 = root.getChild("rrleg4");
        this.rrleg2 = root.getChild("rrleg2");
        this.rrleg1 = root.getChild("rrleg1");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("lfleg1",
                CubeListBuilder.create().texOffs(28, 35).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 1, 10, 1),
                PartPose.offsetAndRotation(27.0F, 16.0F, -3.0F, 0.0F, 0.0F, -0.6283185F));

        partdefinition.addOrReplaceChild("lfleg2",
                CubeListBuilder.create().texOffs(0, 32).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 1, 22, 1),
                PartPose.offsetAndRotation(21.0F, -5.0F, -3.0F, 0.0F, 0.0F, -0.2792527F));

        partdefinition.addOrReplaceChild("lfleg3",
                CubeListBuilder.create().texOffs(64, 2).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 20, 1, 1),
                PartPose.offsetAndRotation(2.0F, -5.0F, 0.0F, 0.0F, 0.1570796F, 0.0F));

        partdefinition.addOrReplaceChild("lfleg4",
                CubeListBuilder.create().texOffs(64, 20).mirror()
                        .addBox(15.0F, 0.0F, -2.0F, 4, 1, 5),
                PartPose.offsetAndRotation(2.0F, -5.0F, 0.0F, 0.0F, 0.1570796F, 0.0F));

        partdefinition.addOrReplaceChild("lrleg1",
                CubeListBuilder.create().texOffs(35, 35).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 1, 10, 1),
                PartPose.offsetAndRotation(32.0F, 18.0F, 11.0F, 0.0F, 0.0F, -0.8726646F));

        partdefinition.addOrReplaceChild("lrleg2",
                CubeListBuilder.create().texOffs(14, 32).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 1, 22, 1),
                PartPose.offsetAndRotation(21.0F, 0.0F, 11.0F, 0.0F, 0.0F, -0.5410521F));

        partdefinition.addOrReplaceChild("lrleg3",
                CubeListBuilder.create().texOffs(64, 11).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 20, 1, 1),
                PartPose.offsetAndRotation(2.0F, 0.0F, 8.0F, 0.0F, -0.1570796F, 0.0F));

        partdefinition.addOrReplaceChild("lrleg4",
                CubeListBuilder.create().texOffs(64, 36).mirror()
                        .addBox(15.0F, 0.0F, -2.0F, 4, 1, 5),
                PartPose.offsetAndRotation(2.0F, 0.0F, 8.0F, 0.0F, -0.1570796F, 0.0F));

        partdefinition.addOrReplaceChild("abdomen",
                CubeListBuilder.create().texOffs(118, 0).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 9, 5, 53),
                PartPose.offsetAndRotation(-4.0F, -11.0F, 0.0F, -0.5061455F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("thorax",
                CubeListBuilder.create().texOffs(145, 62).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 15, 3, 13),
                PartPose.offsetAndRotation(-7.0F, -14.0F, -12.0F, -0.2443461F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("neck1",
                CubeListBuilder.create().texOffs(145, 82).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 9, 1, 15),
                PartPose.offsetAndRotation(-4.0F, -15.0F, -27.0F, -0.0698132F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("neck2",
                CubeListBuilder.create().texOffs(40, 150).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 3, 1, 2),
                PartPose.offset(-1.0F, -15.0F, -29.0F));

        partdefinition.addOrReplaceChild("head1",
                CubeListBuilder.create().texOffs(0, 150).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 2, 6, 1),
                PartPose.offsetAndRotation(0.0F, -16.0F, -30.0F, 0.0F, 0.0F, 0.1396263F));

        partdefinition.addOrReplaceChild("head2",
                CubeListBuilder.create().texOffs(10, 150).mirror()
                        .addBox(-2.0F, 0.0F, 0.0F, 2, 6, 1),
                PartPose.offsetAndRotation(0.0F, -16.0F, -30.0F, 0.0F, 0.0F, -0.1745329F));

        partdefinition.addOrReplaceChild("leye",
                CubeListBuilder.create().texOffs(20, 150).mirror()
                        .addBox(1.0F, 0.0F, -0.5F, 2, 2, 1),
                PartPose.offsetAndRotation(0.0F, -16.0F, -30.0F, 0.0F, 0.0F, 0.1396263F));

        partdefinition.addOrReplaceChild("reye",
                CubeListBuilder.create().texOffs(30, 150).mirror()
                        .addBox(-3.0F, 0.0F, -0.5F, 2, 2, 1),
                PartPose.offsetAndRotation(0.0F, -16.0F, -30.0F, 0.0F, 0.0F, -0.1745329F));

        partdefinition.addOrReplaceChild("lantenna",
                CubeListBuilder.create().texOffs(53, 150).mirror()
                        .addBox(0.0F, -20.0F, 0.0F, 1, 20, 1),
                PartPose.offsetAndRotation(0.0F, -16.0F, -30.0F, 0.0F, 0.0F, 0.2792527F));

        partdefinition.addOrReplaceChild("rantenna",
                CubeListBuilder.create().texOffs(60, 150).mirror()
                        .addBox(-1.0F, -20.0F, 0.0F, 1, 20, 1),
                PartPose.offsetAndRotation(0.0F, -16.0F, -30.0F, 0.0F, 0.0F, -0.2792527F));

        partdefinition.addOrReplaceChild("larm1",
                CubeListBuilder.create().texOffs(51, 0).mirror()
                        .addBox(0.0F, 0.0F, -1.0F, 1, 23, 4),
                PartPose.offsetAndRotation(2.0F, -14.0F, -23.0F, 0.0349066F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("larm2",
                CubeListBuilder.create().texOffs(30, 0).mirror()
                        .addBox(0.0F, -18.0F, -2.0F, 1, 18, 2),
                PartPose.offsetAndRotation(2.0F, 8.0F, -22.0F, 0.5585054F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("larm3",
                CubeListBuilder.create().texOffs(16, 0).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 1, 21, 1),
                PartPose.offset(2.0F, -7.0F, -33.0F));

        partdefinition.addOrReplaceChild("lfwing",
                CubeListBuilder.create().texOffs(0, 67).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 48, 1, 12),
                PartPose.offsetAndRotation(2.0F, -11.0F, 0.0F, -0.2268928F, 0.0F, -0.6981317F));

        partdefinition.addOrReplaceChild("rfwing",
                CubeListBuilder.create().texOffs(0, 83).mirror()
                        .addBox(-48.0F, 0.0F, 0.0F, 48, 1, 12),
                PartPose.offsetAndRotation(-1.0F, -11.0F, 0.0F, -0.2268928F, 0.0F, 0.6981317F));

        partdefinition.addOrReplaceChild("lrwing",
                CubeListBuilder.create().texOffs(0, 100).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 42, 1, 17),
                PartPose.offsetAndRotation(2.0F, -6.0F, 10.0F, -0.2268928F, 0.0F, -0.3490659F));

        partdefinition.addOrReplaceChild("rrwing",
                CubeListBuilder.create().texOffs(0, 122).mirror()
                        .addBox(-42.0F, 0.0F, 0.0F, 42, 1, 17),
                PartPose.offsetAndRotation(-1.0F, -6.0F, 10.0F, -0.2268928F, 0.0F, 0.3490659F));

        partdefinition.addOrReplaceChild("rarm1",
                CubeListBuilder.create().texOffs(38, 0).mirror()
                        .addBox(0.0F, 0.0F, -1.0F, 1, 23, 4),
                PartPose.offsetAndRotation(-1.0F, -14.0F, -23.0F, 0.0349066F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("rarm2",
                CubeListBuilder.create().texOffs(22, 0).mirror()
                        .addBox(0.0F, -18.0F, -2.0F, 1, 18, 2),
                PartPose.offsetAndRotation(-1.0F, 8.0F, -22.0F, 0.5585054F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("rarm3",
                CubeListBuilder.create().texOffs(10, 0).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 1, 21, 1),
                PartPose.offset(-1.0F, -7.0F, -33.0F));

        partdefinition.addOrReplaceChild("rlfleg3",
                CubeListBuilder.create().texOffs(64, 6).mirror()
                        .addBox(-20.0F, 0.0F, 0.0F, 20, 1, 1),
                PartPose.offsetAndRotation(-1.0F, -5.0F, 0.0F, 0.0F, -0.1570796F, 0.0F));

        partdefinition.addOrReplaceChild("rfleg4",
                CubeListBuilder.create().texOffs(64, 28).mirror()
                        .addBox(-19.0F, 0.0F, -2.0F, 4, 1, 5),
                PartPose.offsetAndRotation(-1.0F, -5.0F, 0.0F, 0.0F, -0.1570796F, 0.0F));

        partdefinition.addOrReplaceChild("rfleg2",
                CubeListBuilder.create().texOffs(7, 32).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 1, 22, 1),
                PartPose.offsetAndRotation(-21.0F, -5.0F, -3.0F, 0.0F, 0.0F, 0.2792527F));

        partdefinition.addOrReplaceChild("rfleg1",
                CubeListBuilder.create().texOffs(42, 35).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 1, 10, 1),
                PartPose.offsetAndRotation(-27.0F, 16.0F, -3.0F, 0.0F, 0.0F, 0.6283185F));

        partdefinition.addOrReplaceChild("rrleg3",
                CubeListBuilder.create().texOffs(64, 16).mirror()
                        .addBox(-20.0F, 0.0F, 0.0F, 20, 1, 1),
                PartPose.offsetAndRotation(-1.0F, 0.0F, 8.0F, 0.0F, 0.1570796F, 0.0F));

        partdefinition.addOrReplaceChild("rrleg4",
                CubeListBuilder.create().texOffs(64, 44).mirror()
                        .addBox(-19.0F, 0.0F, -2.0F, 4, 1, 5),
                PartPose.offsetAndRotation(-1.0F, 0.0F, 8.0F, 0.0F, 0.1570796F, 0.0F));

        partdefinition.addOrReplaceChild("rrleg2",
                CubeListBuilder.create().texOffs(21, 32).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 1, 22, 1),
                PartPose.offsetAndRotation(-21.0F, 0.0F, 11.0F, 0.0F, 0.0F, 0.5410521F));

        partdefinition.addOrReplaceChild("rrleg1",
                CubeListBuilder.create().texOffs(49, 35).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 1, 10, 1),
                PartPose.offsetAndRotation(-32.0F, 18.0F, 11.0F, 0.0F, 0.0F, 0.8726646F));

        return LayerDefinition.create(meshdefinition, 256, 256);
    }

    @Override
    public void setupAnim(EntityMantis entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float a1;
        float newangle = 0.0f;
        newangle = Mth.cos((float)(ageInTicks * 0.9f * limbSwingAmount)) * (float)Math.PI * 0.25f;
        this.lfwing.zRot = -0.698f - newangle;
        this.rfwing.zRot = 0.698f + newangle;
        newangle = Mth.cos((float)(ageInTicks * 0.9f * limbSwingAmount)) * (float)Math.PI * 0.35f;
        this.lrwing.zRot = -0.349f + newangle;
        this.rrwing.zRot = 0.349f - newangle;
        if (entity.getAttacking() == 0) {
        newangle = Mth.cos((float)(ageInTicks * 0.051f * limbSwingAmount)) * (float)Math.PI * 0.013f;
        a1 = -0.2f;
        } else {
        newangle = Mth.cos((float)(ageInTicks * 0.51f * limbSwingAmount)) * (float)Math.PI * 0.25f;
        a1 = -0.698f;
        }
        this.larm1.xRot = a1 + newangle;
        this.larm2.z = (float)((double)(this.larm1.z + 1.0f) + Math.sin(this.larm1.xRot) * 22.0);
        this.larm2.y = (float)((double)this.larm1.y + Math.cos(this.larm1.xRot) * 22.0);
        this.larm2.xRot = -a1 - newangle;
        this.larm3.z = (float)((double)(this.larm2.z + 1.0f) - Math.sin(this.larm2.xRot) * 17.0);
        this.larm3.y = (float)((double)this.larm2.y - Math.cos(this.larm2.xRot) * 17.0);
        this.larm3.xRot = a1 + newangle;
        this.rarm1.xRot = a1 - newangle;
        this.rarm2.z = (float)((double)(this.rarm1.z + 1.0f) + Math.sin(this.rarm1.xRot) * 22.0);
        this.rarm2.y = (float)((double)this.rarm1.y + Math.cos(this.rarm1.xRot) * 22.0);
        this.rarm2.xRot = -a1 + newangle;
        this.rarm3.z = (float)((double)(this.rarm2.z + 1.0f) - Math.sin(this.rarm2.xRot) * 17.0);
        this.rarm3.y = (float)((double)this.rarm2.y - Math.cos(this.rarm2.xRot) * 17.0);
        this.rarm3.xRot = a1 - newangle;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.lfleg1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lfleg2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lfleg3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lfleg4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lrleg1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lrleg2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lrleg3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lrleg4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.abdomen.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.thorax.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.neck1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.neck2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.head1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.head2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leye.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.reye.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lantenna.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rantenna.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.larm1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.larm2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.larm3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lfwing.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rfwing.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lrwing.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rrwing.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rarm1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rarm2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rarm3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rlfleg3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rfleg4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rfleg2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rfleg1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rrleg3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rrleg4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rrleg2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rrleg1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
