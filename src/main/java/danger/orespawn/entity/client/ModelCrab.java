package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.Crab;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

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
                CubeListBuilder.create().texOffs(0, 450).mirror().addBox(-38.0F, -5.0F, -8.0F, 76, 10, 48),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("body2",
                CubeListBuilder.create().texOffs(0, 406).mirror().addBox(-32.0F, -10.0F, -10.0F, 64, 5, 34),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("leg1",
                CubeListBuilder.create().texOffs(128, 0).mirror().addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4),
                PartPose.offsetAndRotation(36.0F, 3.0F, 0.0F, -1.343904F, -1.500983F, 0.0F));

        partdefinition.addOrReplaceChild("body3",
                CubeListBuilder.create().texOffs(0, 357).mirror().addBox(0.0F, 0.0F, 0.0F, 8, 4, 40),
                PartPose.offset(38.0F, -5.0F, -6.0F));

        partdefinition.addOrReplaceChild("body4",
                CubeListBuilder.create().texOffs(100, 357).mirror().addBox(0.0F, 0.0F, 0.0F, 8, 4, 40),
                PartPose.offset(-46.0F, -5.0F, -6.0F));

        partdefinition.addOrReplaceChild("leg2",
                CubeListBuilder.create().texOffs(128, 20).mirror().addBox(-1.0F, 10.0F, -6.0F, 3, 16, 3),
                PartPose.offsetAndRotation(36.0F, 3.0F, 0.0F, -0.9599311F, -1.500983F, 0.0F));

        partdefinition.addOrReplaceChild("leg3",
                CubeListBuilder.create().texOffs(128, 43).mirror().addBox(0.0F, 21.0F, -15.0F, 2, 16, 2),
                PartPose.offsetAndRotation(36.0F, 3.0F, 0.0F, -0.5759587F, -1.500983F, 0.0F));

        partdefinition.addOrReplaceChild("body5",
                CubeListBuilder.create().texOffs(0, 339).mirror().addBox(-25.0F, 0.0F, 0.0F, 50, 4, 10),
                PartPose.offset(0.0F, -4.0F, 40.0F));

        partdefinition.addOrReplaceChild("body6",
                CubeListBuilder.create().texOffs(124, 342).mirror().addBox(-14.0F, 0.0F, 0.0F, 28, 3, 4),
                PartPose.offset(0.0F, -10.0F, -14.0F));

        partdefinition.addOrReplaceChild("Leye1",
                CubeListBuilder.create().texOffs(62, 0).mirror().addBox(-0.5F, -12.0F, -0.5F, 1, 12, 1),
                PartPose.offsetAndRotation(9.0F, -9.0F, -11.0F, 0.0F, 0.0F, 0.4886922F));

        partdefinition.addOrReplaceChild("Reye1",
                CubeListBuilder.create().texOffs(40, 0).mirror().addBox(-0.5F, -12.0F, -0.5F, 1, 12, 1),
                PartPose.offsetAndRotation(-9.0F, -9.0F, -11.0F, 0.0F, 0.0F, -0.4886922F));

        partdefinition.addOrReplaceChild("Leye2",
                CubeListBuilder.create().texOffs(50, 0).mirror().addBox(-1.0F, -14.0F, -1.0F, 2, 2, 2),
                PartPose.offsetAndRotation(9.0F, -9.0F, -11.0F, 0.0F, 0.0F, 0.4886922F));

        partdefinition.addOrReplaceChild("Reye2",
                CubeListBuilder.create().texOffs(26, 0).mirror().addBox(-1.0F, -14.0F, -1.0F, 2, 2, 2),
                PartPose.offsetAndRotation(-9.0F, -9.0F, -11.0F, 0.0F, 0.0F, -0.4886922F));

        partdefinition.addOrReplaceChild("Lclaw1",
                CubeListBuilder.create().texOffs(0, 80).mirror().addBox(-4.0F, 0.0F, -14.0F, 8, 4, 18),
                PartPose.offsetAndRotation(31.0F, -2.0F, -8.0F, 0.0F, -0.4886922F, 0.0F));

        partdefinition.addOrReplaceChild("Lclaw2",
                CubeListBuilder.create().texOffs(0, 105).mirror().addBox(-7.0F, -3.0F, -12.0F, 17, 6, 16),
                PartPose.offsetAndRotation(37.0F, 0.0F, -20.0F, 0.0F, -0.1745329F, 0.0F));

        partdefinition.addOrReplaceChild("Lclaw3",
                CubeListBuilder.create().texOffs(0, 131).mirror().addBox(0.0F, -5.0F, -25.0F, 17, 10, 30),
                PartPose.offsetAndRotation(37.0F, 0.0F, -31.0F, 0.0F, -0.4537856F, 0.0F));

        partdefinition.addOrReplaceChild("Lclaw4",
                CubeListBuilder.create().texOffs(0, 175).mirror().addBox(2.0F, -3.0F, -32.0F, 11, 5, 12),
                PartPose.offsetAndRotation(37.0F, 0.0F, -31.0F, 0.0F, -0.3490659F, 0.0F));

        partdefinition.addOrReplaceChild("Lclaw5",
                CubeListBuilder.create().texOffs(0, 197).mirror().addBox(-4.0F, -3.0F, -27.0F, 7, 5, 32),
                PartPose.offsetAndRotation(36.0F, 0.0F, -31.0F, 0.0F, 0.3839724F, 0.0F));

        partdefinition.addOrReplaceChild("Rclaw1",
                CubeListBuilder.create().texOffs(102, 78).mirror().addBox(-4.0F, 0.0F, -14.0F, 8, 4, 18),
                PartPose.offsetAndRotation(-31.0F, -2.0F, -8.0F, 0.0F, 0.4886922F, 0.0F));

        partdefinition.addOrReplaceChild("Rclaw2",
                CubeListBuilder.create().texOffs(103, 106).mirror().addBox(-10.0F, -3.0F, -12.0F, 17, 6, 16),
                PartPose.offsetAndRotation(-37.0F, 0.0F, -20.0F, 0.0F, 0.1745329F, 0.0F));

        partdefinition.addOrReplaceChild("Rclaw3",
                CubeListBuilder.create().texOffs(100, 131).mirror().addBox(-17.0F, -5.0F, -25.0F, 17, 10, 30),
                PartPose.offsetAndRotation(-37.0F, 0.0F, -31.0F, 0.0F, 0.4537856F, 0.0F));

        partdefinition.addOrReplaceChild("Rclaw4",
                CubeListBuilder.create().texOffs(101, 175).mirror().addBox(-13.0F, -3.0F, -32.0F, 11, 5, 12),
                PartPose.offsetAndRotation(-37.0F, 0.0F, -31.0F, 0.0F, 0.3490659F, 0.0F));

        partdefinition.addOrReplaceChild("Rclaw5",
                CubeListBuilder.create().texOffs(100, 197).mirror().addBox(-4.0F, -3.0F, -27.0F, 7, 5, 32),
                PartPose.offsetAndRotation(-36.0F, 0.0F, -31.0F, 0.0F, -0.3839724F, 0.0F));

        partdefinition.addOrReplaceChild("Rmouth",
                CubeListBuilder.create().texOffs(0, 28).mirror().addBox(0.0F, 0.0F, -0.5F, 6, 3, 1),
                PartPose.offsetAndRotation(-7.0F, 0.0F, -7.5F, 0.0F, 0.3665191F, 0.0F));

        partdefinition.addOrReplaceChild("Lmouth",
                CubeListBuilder.create().texOffs(0, 19).mirror().addBox(-6.0F, 0.0F, -0.5F, 6, 3, 1),
                PartPose.offsetAndRotation(7.0F, 0.0F, -7.5F, 0.0F, -0.3665191F, 0.0F));

        return LayerDefinition.create(meshdefinition, 256, 512);
    }

    @Override
    public void setupAnim(Crab entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
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
        this.leg3.z = 10.0f;
        this.leg2.z = 10.0f;
        this.leg1.z = 10.0f;
        this.leg2.yRot = this.leg3.yRot = (float)(-1.5707963267948966 - (double)(Mth.cos((float)(ageInTicks * 1.7f)) * (float)Math.PI * 0.15f * limbSwingAmount));
        this.leg1.yRot = this.leg3.yRot;
        this.leg3.z = 20.0f;
        this.leg2.z = 20.0f;
        this.leg1.z = 20.0f;
        this.leg2.yRot = this.leg3.yRot = (float)(-1.5707963267948966 + (double)(Mth.cos((float)(ageInTicks * 1.7f)) * (float)Math.PI * 0.15f * limbSwingAmount));
        this.leg1.yRot = this.leg3.yRot;
        this.leg3.z = 30.0f;
        this.leg2.z = 30.0f;
        this.leg1.z = 30.0f;
        this.leg2.yRot = this.leg3.yRot = (float)(-1.5707963267948966 - (double)(Mth.cos((float)(ageInTicks * 1.7f)) * (float)Math.PI * 0.15f * limbSwingAmount));
        this.leg1.yRot = this.leg3.yRot;
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
        this.leg3.z = 10.0f;
        this.leg2.z = 10.0f;
        this.leg1.z = 10.0f;
        this.leg2.yRot = this.leg3.yRot = (float)(-(-1.5707963267948966 - (double)(Mth.cos((float)(ageInTicks * 1.7f)) * (float)Math.PI * 0.15f * limbSwingAmount)));
        this.leg1.yRot = this.leg3.yRot;
        this.leg3.z = 20.0f;
        this.leg2.z = 20.0f;
        this.leg1.z = 20.0f;
        this.leg2.yRot = this.leg3.yRot = (float)(-(-1.5707963267948966 + (double)(Mth.cos((float)(ageInTicks * 1.7f)) * (float)Math.PI * 0.15f * limbSwingAmount)));
        this.leg1.yRot = this.leg3.yRot;
        this.leg3.z = 30.0f;
        this.leg2.z = 30.0f;
        this.leg1.z = 30.0f;
        this.leg2.yRot = this.leg3.yRot = (float)(-(-1.5707963267948966 - (double)(Mth.cos((float)(ageInTicks * 1.7f)) * (float)Math.PI * 0.15f * limbSwingAmount)));
        this.leg1.yRot = this.leg3.yRot;
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

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.leg1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
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
