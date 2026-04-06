package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.EntityHerculesBeetle;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class HerculesBeetleModel extends EntityModel<EntityHerculesBeetle> {
    private final ModelPart body1;
    private final ModelPart body2;
    private final ModelPart head1;
    private final ModelPart head2;
    private final ModelPart head3;
    private final ModelPart head4;
    private final ModelPart head5;
    private final ModelPart head6;
    private final ModelPart head8;
    private final ModelPart jaw1;
    private final ModelPart jaw2;
    private final ModelPart jaw3;
    private final ModelPart jaw4;
    private final ModelPart head7;
    private final ModelPart lfleg1;
    private final ModelPart lfleg2;
    private final ModelPart lfleg3;
    private final ModelPart lmleg1;
    private final ModelPart lmleg2;
    private final ModelPart lmleg3;
    private final ModelPart lrleg1;
    private final ModelPart lrleg2;
    private final ModelPart lrleg3;
    private final ModelPart jaw5;
    private final ModelPart jaw6;
    private final ModelPart jaw7;
    private final ModelPart jaw8;
    private final ModelPart rfleg1;
    private final ModelPart rfleg2;
    private final ModelPart rfleg3;
    private final ModelPart rmleg1;
    private final ModelPart rmleg2;
    private final ModelPart rmleg3;
    private final ModelPart rrleg1;
    private final ModelPart rrleg2;
    private final ModelPart rrleg3;
    private final ModelPart jaw9;

    public HerculesBeetleModel(ModelPart root) {
        this.body1 = root.getChild("body1");
        this.body2 = root.getChild("body2");
        this.head1 = root.getChild("head1");
        this.head2 = root.getChild("head2");
        this.head3 = root.getChild("head3");
        this.head4 = root.getChild("head4");
        this.head5 = root.getChild("head5");
        this.head6 = root.getChild("head6");
        this.head8 = root.getChild("head8");
        this.jaw1 = root.getChild("jaw1");
        this.jaw2 = root.getChild("jaw2");
        this.jaw3 = root.getChild("jaw3");
        this.jaw4 = root.getChild("jaw4");
        this.head7 = root.getChild("head7");
        this.lfleg1 = root.getChild("lfleg1");
        this.lfleg2 = root.getChild("lfleg2");
        this.lfleg3 = root.getChild("lfleg3");
        this.lmleg1 = root.getChild("lmleg1");
        this.lmleg2 = root.getChild("lmleg2");
        this.lmleg3 = root.getChild("lmleg3");
        this.lrleg1 = root.getChild("lrleg1");
        this.lrleg2 = root.getChild("lrleg2");
        this.lrleg3 = root.getChild("lrleg3");
        this.jaw5 = root.getChild("jaw5");
        this.jaw6 = root.getChild("jaw6");
        this.jaw7 = root.getChild("jaw7");
        this.jaw8 = root.getChild("jaw8");
        this.rfleg1 = root.getChild("rfleg1");
        this.rfleg2 = root.getChild("rfleg2");
        this.rfleg3 = root.getChild("rfleg3");
        this.rmleg1 = root.getChild("rmleg1");
        this.rmleg2 = root.getChild("rmleg2");
        this.rmleg3 = root.getChild("rmleg3");
        this.rrleg1 = root.getChild("rrleg1");
        this.rrleg2 = root.getChild("rrleg2");
        this.rrleg3 = root.getChild("rrleg3");
        this.jaw9 = root.getChild("jaw9");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("body1",
                CubeListBuilder.create().texOffs(0, 30).mirror()
                        .addBox(-8.0F, 0.0F, 0.0F, 16, 16, 23),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("body2",
                CubeListBuilder.create().texOffs(80, 41).mirror()
                        .addBox(-6.0F, 0.0F, 0.0F, 12, 12, 4),
                PartPose.offset(0.0F, 3.0F, 23.0F));

        partdefinition.addOrReplaceChild("head1",
                CubeListBuilder.create().texOffs(0, 71).mirror()
                        .addBox(-9.0F, 0.0F, 0.0F, 18, 16, 12),
                PartPose.offsetAndRotation(0.0F, -1.0F, -10.0F, -0.122173F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("head2",
                CubeListBuilder.create().texOffs(0, 100).mirror()
                        .addBox(-7.0F, 0.0F, 0.0F, 14, 10, 6),
                PartPose.offsetAndRotation(0.0F, -2.0F, -16.0F, -0.122173F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("head3",
                CubeListBuilder.create().texOffs(0, 117).mirror()
                        .addBox(-5.0F, 0.0F, 0.0F, 10, 6, 9),
                PartPose.offsetAndRotation(0.0F, -3.0F, -25.0F, -0.122173F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("head4",
                CubeListBuilder.create().texOffs(0, 133).mirror()
                        .addBox(-4.0F, 0.0F, 0.0F, 8, 4, 12),
                PartPose.offsetAndRotation(0.0F, -4.0F, -37.0F, -0.122173F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("head5",
                CubeListBuilder.create().texOffs(0, 150).mirror()
                        .addBox(-3.0F, 0.0F, 0.0F, 6, 3, 21),
                PartPose.offset(0.0F, -4.0F, -58.0F));

        partdefinition.addOrReplaceChild("head6",
                CubeListBuilder.create().texOffs(0, 175).mirror()
                        .addBox(-2.0F, 0.0F, 0.0F, 4, 2, 14),
                PartPose.offsetAndRotation(0.0F, -2.0F, -72.0F, 0.122173F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("head8",
                CubeListBuilder.create().texOffs(6, 193).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 1, 3, 1),
                PartPose.offsetAndRotation(0.0F, -2.0F, -46.0F, -0.2094395F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("jaw1",
                CubeListBuilder.create().texOffs(114, 0).mirror()
                        .addBox(-3.0F, -3.0F, -4.0F, 6, 7, 5),
                PartPose.offsetAndRotation(0.0F, 12.0F, -12.0F, 0.122173F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("jaw2",
                CubeListBuilder.create().texOffs(115, 14).mirror()
                        .addBox(-2.5F, -3.0F, -27.0F, 5, 5, 23),
                PartPose.offsetAndRotation(0.0F, 12.0F, -12.0F, 0.122173F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("jaw3",
                CubeListBuilder.create().texOffs(115, 43).mirror()
                        .addBox(-1.5F, 0.0F, -44.0F, 3, 5, 18),
                PartPose.offset(0.0F, 12.0F, -12.0F));

        partdefinition.addOrReplaceChild("jaw4",
                CubeListBuilder.create().texOffs(115, 70).mirror()
                        .addBox(-0.5F, -2.0F, -45.0F, 1, 5, 1),
                PartPose.offset(0.0F, 12.0F, -12.0F));

        partdefinition.addOrReplaceChild("head7",
                CubeListBuilder.create().texOffs(0, 193).mirror()
                        .addBox(-0.5F, 0.0F, 0.0F, 1, 4, 1),
                PartPose.offsetAndRotation(0.0F, -2.0F, -73.0F, 0.122173F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("lfleg1",
                CubeListBuilder.create().texOffs(60, 0).mirror()
                        .addBox(0.0F, 0.0F, -0.5F, 10, 3, 3),
                PartPose.offsetAndRotation(6.0F, 15.0F, -5.0F, 0.0F, 0.3490659F, 0.0872665F));

        partdefinition.addOrReplaceChild("lfleg2",
                CubeListBuilder.create().texOffs(60, 8).mirror()
                        .addBox(10.0F, -1.0F, 0.0F, 11, 2, 2),
                PartPose.offsetAndRotation(6.0F, 15.0F, -5.0F, 0.0F, 0.3490659F, 0.2617994F));

        partdefinition.addOrReplaceChild("lfleg3",
                CubeListBuilder.create().texOffs(60, 14).mirror()
                        .addBox(21.0F, -2.0F, 0.5F, 10, 1, 1),
                PartPose.offsetAndRotation(6.0F, 15.0F, -5.0F, 0.0F, 0.3490659F, 0.3490659F));

        partdefinition.addOrReplaceChild("lmleg1",
                CubeListBuilder.create().texOffs(60, 0).mirror()
                        .addBox(0.0F, 0.0F, -0.5F, 10, 3, 3),
                PartPose.offsetAndRotation(6.0F, 15.0F, 0.0F, 0.0F, 0.0F, 0.0872665F));

        partdefinition.addOrReplaceChild("lmleg2",
                CubeListBuilder.create().texOffs(60, 8).mirror()
                        .addBox(10.0F, -1.0F, 0.0F, 11, 2, 2),
                PartPose.offsetAndRotation(6.0F, 15.0F, 0.0F, 0.0F, 0.0F, 0.2617994F));

        partdefinition.addOrReplaceChild("lmleg3",
                CubeListBuilder.create().texOffs(60, 14).mirror()
                        .addBox(21.0F, -2.0F, 0.5F, 10, 1, 1),
                PartPose.offsetAndRotation(6.0F, 15.0F, 0.0F, 0.0F, 0.0F, 0.3490659F));

        partdefinition.addOrReplaceChild("lrleg1",
                CubeListBuilder.create().texOffs(60, 0).mirror()
                        .addBox(0.0F, 0.0F, -0.5F, 10, 3, 3),
                PartPose.offsetAndRotation(6.0F, 15.0F, 5.0F, 0.0F, -0.3490659F, 0.0872665F));

        partdefinition.addOrReplaceChild("lrleg2",
                CubeListBuilder.create().texOffs(60, 8).mirror()
                        .addBox(10.0F, -1.0F, 0.0F, 11, 2, 2),
                PartPose.offsetAndRotation(6.0F, 15.0F, 5.0F, 0.0F, -0.3490659F, 0.2617994F));

        partdefinition.addOrReplaceChild("lrleg3",
                CubeListBuilder.create().texOffs(60, 14).mirror()
                        .addBox(21.0F, -2.0F, 0.5F, 10, 1, 1),
                PartPose.offsetAndRotation(6.0F, 15.0F, 5.0F, 0.0F, -0.3490659F, 0.3490659F));

        partdefinition.addOrReplaceChild("jaw5",
                CubeListBuilder.create().texOffs(115, 78).mirror()
                        .addBox(2.0F, -2.0F, -9.0F, 2, 3, 3),
                PartPose.offsetAndRotation(0.0F, 12.0F, -12.0F, 0.122173F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("jaw6",
                CubeListBuilder.create().texOffs(127, 78).mirror()
                        .addBox(-4.0F, -2.0F, -9.0F, 2, 3, 3),
                PartPose.offsetAndRotation(0.0F, 12.0F, -12.0F, 0.122173F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("jaw7",
                CubeListBuilder.create().texOffs(115, 86).mirror()
                        .addBox(5.0F, 1.0F, -6.0F, 9, 1, 1),
                PartPose.offsetAndRotation(0.0F, 12.0F, -12.0F, 0.0F, 0.5585054F, 0.2268928F));

        partdefinition.addOrReplaceChild("jaw8",
                CubeListBuilder.create().texOffs(115, 89).mirror()
                        .addBox(-14.0F, 1.0F, -6.0F, 9, 1, 1),
                PartPose.offsetAndRotation(0.0F, 12.0F, -12.0F, 0.0F, -0.5585054F, -0.2268928F));

        partdefinition.addOrReplaceChild("rfleg1",
                CubeListBuilder.create().texOffs(30, 0).mirror()
                        .addBox(-10.0F, 0.0F, -0.5F, 10, 3, 3),
                PartPose.offsetAndRotation(-6.0F, 15.0F, -5.0F, 0.0F, -0.3490659F, -0.0872665F));

        partdefinition.addOrReplaceChild("rfleg2",
                CubeListBuilder.create().texOffs(30, 8).mirror()
                        .addBox(-21.0F, -1.0F, 0.0F, 11, 2, 2),
                PartPose.offsetAndRotation(-6.0F, 15.0F, -5.0F, 0.0F, -0.3490659F, -0.2617994F));

        partdefinition.addOrReplaceChild("rfleg3",
                CubeListBuilder.create().texOffs(30, 14).mirror()
                        .addBox(-31.0F, -2.0F, 0.5F, 10, 1, 1),
                PartPose.offsetAndRotation(-6.0F, 15.0F, -5.0F, 0.0F, -0.3490659F, -0.3490659F));

        partdefinition.addOrReplaceChild("rmleg1",
                CubeListBuilder.create().texOffs(30, 0).mirror()
                        .addBox(-10.0F, 0.0F, -0.5F, 10, 3, 3),
                PartPose.offsetAndRotation(-6.0F, 15.0F, 0.0F, 0.0F, 0.0F, -0.0872665F));

        partdefinition.addOrReplaceChild("rmleg2",
                CubeListBuilder.create().texOffs(30, 8).mirror()
                        .addBox(-21.0F, -1.0F, 0.0F, 11, 2, 2),
                PartPose.offsetAndRotation(-6.0F, 15.0F, 0.0F, 0.0F, 0.0F, -0.2617994F));

        partdefinition.addOrReplaceChild("rmleg3",
                CubeListBuilder.create().texOffs(30, 14).mirror()
                        .addBox(-31.0F, -2.0F, 0.5F, 10, 1, 1),
                PartPose.offsetAndRotation(-6.0F, 15.0F, 0.0F, 0.0F, 0.0F, -0.3490659F));

        partdefinition.addOrReplaceChild("rrleg1",
                CubeListBuilder.create().texOffs(30, 0).mirror()
                        .addBox(-10.0F, 0.0F, -0.5F, 10, 3, 3),
                PartPose.offsetAndRotation(-6.0F, 15.0F, 5.0F, 0.0F, 0.3490659F, -0.0872665F));

        partdefinition.addOrReplaceChild("rrleg2",
                CubeListBuilder.create().texOffs(30, 8).mirror()
                        .addBox(-21.0F, -1.0F, 0.0F, 11, 2, 2),
                PartPose.offsetAndRotation(-6.0F, 15.0F, 5.0F, 0.0F, 0.3490659F, -0.2617994F));

        partdefinition.addOrReplaceChild("rrleg3",
                CubeListBuilder.create().texOffs(30, 14).mirror()
                        .addBox(-31.0F, -2.0F, 0.5F, 10, 1, 1),
                PartPose.offsetAndRotation(-6.0F, 15.0F, 5.0F, 0.0F, 0.3490659F, -0.3490659F));

        partdefinition.addOrReplaceChild("jaw9",
                CubeListBuilder.create().texOffs(121, 70).mirror()
                        .addBox(-0.5F, -12.0F, -25.0F, 1, 5, 1),
                PartPose.offsetAndRotation(0.0F, 12.0F, -12.0F, 0.3141593F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 256, 256);
    }

    @Override
    public void setupAnim(EntityHerculesBeetle entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float newangle = 0.0f;
        newangle = Mth.cos((float)(ageInTicks * limbSwingAmount * 0.45f)) * (float)Math.PI * 0.12f * limbSwingAmount;
        this.lfleg3.yRot = this.lfleg2.yRot = (this.lfleg1.yRot = 0.349f + newangle);
        this.lmleg3.yRot = this.lmleg2.yRot = (this.lmleg1.yRot = -newangle);
        this.lrleg3.yRot = this.lrleg2.yRot = (this.lrleg1.yRot = -0.349f + newangle);
        this.rfleg3.yRot = this.rfleg2.yRot = (this.rfleg1.yRot = -0.349f + newangle);
        this.rmleg3.yRot = this.rmleg2.yRot = (this.rmleg1.yRot = -newangle);
        this.rrleg3.yRot = this.rrleg2.yRot = (this.rrleg1.yRot = 0.349f + newangle);
        newangle = entity.getAttacking() == 0 ? Mth.cos((float)(ageInTicks * 0.051f * limbSwingAmount)) * (float)Math.PI * 0.01f : Mth.cos((float)(ageInTicks * 0.51f * limbSwingAmount)) * (float)Math.PI * 0.07f;
        this.jaw1.xRot = 0.122f + newangle;
        this.jaw2.xRot = 0.122f + newangle;
        this.jaw3.xRot = 0.0f + newangle;
        this.jaw4.xRot = 0.0f + newangle;
        this.jaw5.xRot = 0.122f + newangle;
        this.jaw6.xRot = 0.122f + newangle;
        this.jaw7.xRot = 0.0f + newangle;
        this.jaw8.xRot = 0.0f + newangle;
        this.jaw9.xRot = 0.314f + newangle;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.body1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.body2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.head1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.head2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.head3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.head4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.head5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.head6.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.head8.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.jaw1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.jaw2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.jaw3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.jaw4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.head7.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lfleg1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lfleg2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lfleg3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lmleg1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lmleg2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lmleg3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lrleg1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lrleg2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lrleg3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.jaw5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.jaw6.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.jaw7.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.jaw8.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rfleg1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rfleg2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rfleg3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rmleg1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rmleg2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rmleg3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rrleg1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rrleg2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rrleg3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.jaw9.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
