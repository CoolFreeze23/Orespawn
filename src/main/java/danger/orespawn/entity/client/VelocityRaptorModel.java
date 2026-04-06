package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.VelocityRaptor;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class VelocityRaptorModel extends EntityModel<VelocityRaptor> {
    private final ModelPart hf3;
    private final ModelPart hf4;
    private final ModelPart hf2;
    private final ModelPart hf1;
    private final ModelPart lff2;
    private final ModelPart lff1;
    private final ModelPart lff3;
    private final ModelPart rff2;
    private final ModelPart rff3;
    private final ModelPart rff1;
    private final ModelPart tf4;
    private final ModelPart tf1;
    private final ModelPart Shape1;
    private final ModelPart neck;
    private final ModelPart head1;
    private final ModelPart lf1;
    private final ModelPart lf2;
    private final ModelPart head2;
    private final ModelPart tail1;
    private final ModelPart tail2;
    private final ModelPart bl1;
    private final ModelPart br1;
    private final ModelPart bl2;
    private final ModelPart br2;
    private final ModelPart bl3;
    private final ModelPart br3;
    private final ModelPart rf1;
    private final ModelPart rf2;
    private final ModelPart tf2;
    private final ModelPart tf3;
    private final ModelPart bl4;
    private final ModelPart br4;
    private final ModelPart Hat1;
    private final ModelPart Hat2;

    public VelocityRaptorModel(ModelPart root) {
        this.hf3 = root.getChild("hf3");
        this.hf4 = root.getChild("hf4");
        this.hf2 = root.getChild("hf2");
        this.hf1 = root.getChild("hf1");
        this.lff2 = root.getChild("lff2");
        this.lff1 = root.getChild("lff1");
        this.lff3 = root.getChild("lff3");
        this.rff2 = root.getChild("rff2");
        this.rff3 = root.getChild("rff3");
        this.rff1 = root.getChild("rff1");
        this.tf4 = root.getChild("tf4");
        this.tf1 = root.getChild("tf1");
        this.Shape1 = root.getChild("Shape1");
        this.neck = root.getChild("neck");
        this.head1 = root.getChild("head1");
        this.lf1 = root.getChild("lf1");
        this.lf2 = root.getChild("lf2");
        this.head2 = root.getChild("head2");
        this.tail1 = root.getChild("tail1");
        this.tail2 = root.getChild("tail2");
        this.bl1 = root.getChild("bl1");
        this.br1 = root.getChild("br1");
        this.bl2 = root.getChild("bl2");
        this.br2 = root.getChild("br2");
        this.bl3 = root.getChild("bl3");
        this.br3 = root.getChild("br3");
        this.rf1 = root.getChild("rf1");
        this.rf2 = root.getChild("rf2");
        this.tf2 = root.getChild("tf2");
        this.tf3 = root.getChild("tf3");
        this.bl4 = root.getChild("bl4");
        this.br4 = root.getChild("br4");
        this.Hat1 = root.getChild("Hat1");
        this.Hat2 = root.getChild("Hat2");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("hf3",
                CubeListBuilder.create().texOffs(0, 0).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 0, 1, 3),
                PartPose.offsetAndRotation(0.0F, 7.0F, -2.0F, 0.4537856F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("hf4",
                CubeListBuilder.create().texOffs(0, 0).mirror()
                        .addBox(0.0F, -0.2F, 0.0F, 0, 1, 3),
                PartPose.offsetAndRotation(0.0F, 8.0F, -1.5F, 0.2443461F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("hf2",
                CubeListBuilder.create().texOffs(0, 0).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 0, 1, 3),
                PartPose.offsetAndRotation(0.0F, 7.0F, -3.5F, 0.6632251F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("hf1",
                CubeListBuilder.create().texOffs(0, 1).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 0, 1, 2),
                PartPose.offsetAndRotation(0.0F, 7.0F, -4.5F, 0.9424778F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("lff2",
                CubeListBuilder.create().texOffs(0, 6).mirror()
                        .addBox(0.5F, 2.5F, 3.0F, 0, 1, 3),
                PartPose.offsetAndRotation(2.0F, 14.0F, 1.0F, -0.4537856F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("lff1",
                CubeListBuilder.create().texOffs(0, 6).mirror()
                        .addBox(0.5F, 2.0F, 2.0F, 0, 1, 3),
                PartPose.offsetAndRotation(2.0F, 14.0F, 1.0F, -0.2792527F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("lff3",
                CubeListBuilder.create().texOffs(0, 6).mirror()
                        .addBox(0.5F, 1.0F, 4.0F, 0, 1, 3),
                PartPose.offsetAndRotation(2.0F, 14.0F, 1.0F, -1.047198F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("rff2",
                CubeListBuilder.create().texOffs(0, 6).mirror()
                        .addBox(-0.5F, 2.5F, 3.0F, 0, 1, 3),
                PartPose.offsetAndRotation(-2.0F, 14.0F, 1.0F, -0.4537856F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("rff3",
                CubeListBuilder.create().texOffs(0, 6).mirror()
                        .addBox(-0.5F, 1.0F, 4.0F, 0, 1, 3),
                PartPose.offsetAndRotation(-2.0F, 14.0F, 1.0F, -1.047198F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("rff1",
                CubeListBuilder.create().texOffs(0, 6).mirror()
                        .addBox(-0.5F, 2.0F, 2.0F, 0, 1, 3),
                PartPose.offsetAndRotation(-2.0F, 14.0F, 1.0F, -0.2792527F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("tf4",
                CubeListBuilder.create().texOffs(0, 3).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 0, 1, 3),
                PartPose.offsetAndRotation(0.0F, 11.0F, 25.0F, -0.5410521F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("tf1",
                CubeListBuilder.create().texOffs(0, 3).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 0, 1, 3),
                PartPose.offsetAndRotation(0.0F, 11.0F, 19.0F, -0.5410521F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("Shape1",
                CubeListBuilder.create().texOffs(0, 0).mirror()
                        .addBox(-2.0F, 0.0F, 0.0F, 4, 7, 11),
                PartPose.offset(0.0F, 10.0F, 0.0F));

        partdefinition.addOrReplaceChild("neck",
                CubeListBuilder.create().texOffs(0, 19).mirror()
                        .addBox(-1.0F, -7.0F, -2.0F, 2, 8, 3),
                PartPose.offsetAndRotation(0.0F, 12.0F, 2.0F, 1.082104F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("head1",
                CubeListBuilder.create().texOffs(0, 49).mirror()
                        .addBox(-2.0F, 0.0F, -7.0F, 3, 4, 7),
                PartPose.offset(0.5F, 7.0F, -1.0F));

        partdefinition.addOrReplaceChild("lf1",
                CubeListBuilder.create().texOffs(0, 31).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 1, 3, 2),
                PartPose.offsetAndRotation(2.0F, 14.0F, 1.0F, 0.2792527F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("lf2",
                CubeListBuilder.create().texOffs(16, 19).mirror()
                        .addBox(0.0F, 1.0F, 2.0F, 1, 4, 1),
                PartPose.offsetAndRotation(2.0F, 14.0F, 1.0F, -0.4363323F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("head2",
                CubeListBuilder.create().texOffs(20, 0).mirror()
                        .addBox(-1.0F, 0.0F, -10.0F, 2, 4, 4),
                PartPose.offset(0.0F, 7.0F, -1.0F));

        partdefinition.addOrReplaceChild("tail1",
                CubeListBuilder.create().texOffs(0, 38).mirror()
                        .addBox(-1.0F, 0.0F, 0.0F, 2, 5, 4),
                PartPose.offset(0.0F, 10.0F, 11.0F));

        partdefinition.addOrReplaceChild("tail2",
                CubeListBuilder.create().texOffs(26, 11).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 1, 2, 10),
                PartPose.offset(-0.5F, 10.0F, 15.0F));

        partdefinition.addOrReplaceChild("bl1",
                CubeListBuilder.create().texOffs(22, 24).mirror()
                        .addBox(-1.0F, 0.0F, 0.0F, 2, 6, 4),
                PartPose.offset(2.0F, 13.0F, 6.0F));

        partdefinition.addOrReplaceChild("br1",
                CubeListBuilder.create().texOffs(36, 0).mirror()
                        .addBox(-1.0F, 0.0F, 0.0F, 2, 6, 4),
                PartPose.offset(-2.0F, 13.0F, 6.0F));

        partdefinition.addOrReplaceChild("bl2",
                CubeListBuilder.create().texOffs(12, 26).mirror()
                        .addBox(-1.0F, 5.0F, -3.0F, 2, 5, 2),
                PartPose.offsetAndRotation(2.0F, 13.0F, 6.0F, 0.4886922F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("br2",
                CubeListBuilder.create().texOffs(13, 36).mirror()
                        .addBox(-1.0F, 5.0F, -3.0F, 2, 5, 2),
                PartPose.offsetAndRotation(-2.0F, 13.0F, 6.0F, 0.4886922F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("bl3",
                CubeListBuilder.create().texOffs(28, 39).mirror()
                        .addBox(-1.0F, 9.0F, -1.0F, 2, 2, 4),
                PartPose.offset(2.0F, 13.0F, 6.0F));

        partdefinition.addOrReplaceChild("br3",
                CubeListBuilder.create().texOffs(18, 45).mirror()
                        .addBox(-1.0F, 9.0F, -1.0F, 2, 2, 4),
                PartPose.offset(-2.0F, 13.0F, 6.0F));

        partdefinition.addOrReplaceChild("rf1",
                CubeListBuilder.create().texOffs(35, 31).mirror()
                        .addBox(-1.0F, 0.0F, 0.0F, 1, 3, 2),
                PartPose.offsetAndRotation(-2.0F, 14.0F, 1.0F, 0.2792527F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("rf2",
                CubeListBuilder.create().texOffs(11, 19).mirror()
                        .addBox(-1.0F, 1.0F, 2.0F, 1, 4, 1),
                PartPose.offsetAndRotation(-2.0F, 14.0F, 1.0F, -0.4363323F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("tf2",
                CubeListBuilder.create().texOffs(0, 3).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 0, 1, 3),
                PartPose.offsetAndRotation(0.0F, 11.0F, 21.0F, -0.5410521F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("tf3",
                CubeListBuilder.create().texOffs(0, 3).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 0, 1, 3),
                PartPose.offsetAndRotation(0.0F, 11.0F, 23.0F, -0.5410521F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("bl4",
                CubeListBuilder.create().texOffs(31, 10).mirror()
                        .addBox(-1.0F, 6.0F, -5.0F, 1, 3, 1),
                PartPose.offsetAndRotation(2.0F, 13.0F, 6.0F, 0.6283185F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("br4",
                CubeListBuilder.create().texOffs(31, 15).mirror()
                        .addBox(0.0F, 6.0F, -5.0F, 1, 3, 1),
                PartPose.offsetAndRotation(-2.0F, 13.0F, 6.0F, 0.6283185F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("Hat1",
                CubeListBuilder.create().texOffs(50, 0).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 4, 1, 5),
                PartPose.offset(-2.0F, 6.0F, -6.0F));

        partdefinition.addOrReplaceChild("Hat2",
                CubeListBuilder.create().texOffs(50, 0).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 3, 2, 3),
                PartPose.offset(-1.5F, 4.0F, -4.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(VelocityRaptor entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float hf = 0.0f;
        float newangle = 0.0f;
        newangle = (double)limbSwingAmount > 0.1 ? Mth.cos((float)(ageInTicks * 1.3f * limbSwingAmount)) * (float)Math.PI * 0.25f * limbSwingAmount : 0.0f;
        this.bl1.xRot = newangle;
        this.bl2.xRot = newangle + 0.488f;
        this.bl3.xRot = newangle;
        this.bl4.xRot = newangle + 0.628f;
        this.br1.xRot = -newangle;
        this.br2.xRot = -newangle + 0.488f;
        this.br3.xRot = -newangle;
        this.br4.xRot = -newangle + 0.628f;
        hf = entity.getHealth() / entity.getMaxHealth();
        this.hf1.yRot = newangle = Mth.cos((float)(ageInTicks * 1.25f * limbSwingAmount * hf)) * (float)Math.PI * 0.1f * hf;
        this.hf2.yRot = -newangle;
        this.hf3.yRot = newangle;
        this.hf4.yRot = -newangle;
        newangle = Mth.cos((float)(ageInTicks * 0.3f)) * (float)Math.PI * 0.05f;
        this.lf1.xRot = newangle + 0.279f;
        this.lf2.xRot = newangle - 0.436f;
        this.lff1.xRot = newangle - 0.279f;
        this.lff2.xRot = newangle - 0.453f;
        this.lff3.xRot = newangle - 1.047f;
        this.rf1.xRot = -newangle + 0.279f;
        this.rf2.xRot = -newangle - 0.436f;
        this.rff1.xRot = -newangle - 0.279f;
        this.rff2.xRot = -newangle - 0.453f;
        this.rff3.xRot = -newangle - 1.047f;
        this.lff1.yRot = newangle = Mth.cos((float)(ageInTicks * 1.3f * limbSwingAmount)) * (float)Math.PI * 0.1f;
        this.lff2.yRot = -newangle;
        this.lff3.yRot = newangle;
        this.rff1.yRot = -newangle;
        this.rff2.yRot = newangle;
        this.rff3.yRot = -newangle;
        newangle = entity.isInSittingPose() ? 0.0f : Mth.cos((float)(ageInTicks * 1.4f * limbSwingAmount * hf)) * (float)Math.PI * 0.25f * hf;
        this.tf1.zRot = newangle;
        this.tf2.zRot = -newangle;
        this.tf3.zRot = newangle;
        this.tf4.zRot = -newangle;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.hf3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.hf4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.hf2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.hf1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tf1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tf2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tf3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tf4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lf1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lf2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lff2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lff1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lff3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rf1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rf2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rff2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rff3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rff1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.bl1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.bl2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.bl3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.bl4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.br1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.br2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.br3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.br4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Shape1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.neck.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.head1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.head2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tail1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tail2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Hat1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Hat2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
