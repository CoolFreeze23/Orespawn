package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.SeaViper;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class ModelSeaViper extends EntityModel<SeaViper> {
    private final float wingspeed = 1.0f;
    private final ModelPart TailTip;
    private final ModelPart Neck;
    private final ModelPart tBase;
    private final ModelPart t2;
    private final ModelPart t3;
    private final ModelPart t4;
    private final ModelPart t5;
    private final ModelPart t6;
    private final ModelPart t7;
    private final ModelPart t8;
    private final ModelPart t9;
    private final ModelPart t10;
    private final ModelPart t12;
    private final ModelPart t11;
    private final ModelPart t13;
    private final ModelPart t14;
    private final ModelPart t15;
    private final ModelPart t16;
    private final ModelPart t17;
    private final ModelPart t18;
    private final ModelPart t19;
    private final ModelPart t20;
    private final ModelPart t21;
    private final ModelPart MouthBottom;
    private final ModelPart ToungBase;
    private final ModelPart MiddleTounge;
    private final ModelPart EyeRight;
    private final ModelPart EyeLeft;
    private final ModelPart MouthTop;
    private final ModelPart Head;
    private final ModelPart FangRight;
    private final ModelPart FangLeft;
    private final ModelPart ForkRight;
    private final ModelPart ForkLeft;

    public ModelSeaViper(ModelPart root) {
        this.TailTip = root.getChild("TailTip");
        this.Neck = root.getChild("Neck");
        this.tBase = root.getChild("tBase");
        this.t2 = root.getChild("t2");
        this.t3 = root.getChild("t3");
        this.t4 = root.getChild("t4");
        this.t5 = root.getChild("t5");
        this.t6 = root.getChild("t6");
        this.t7 = root.getChild("t7");
        this.t8 = root.getChild("t8");
        this.t9 = root.getChild("t9");
        this.t10 = root.getChild("t10");
        this.t12 = root.getChild("t12");
        this.t11 = root.getChild("t11");
        this.t13 = root.getChild("t13");
        this.t14 = root.getChild("t14");
        this.t15 = root.getChild("t15");
        this.t16 = root.getChild("t16");
        this.t17 = root.getChild("t17");
        this.t18 = root.getChild("t18");
        this.t19 = root.getChild("t19");
        this.t20 = root.getChild("t20");
        this.t21 = root.getChild("t21");
        this.MouthBottom = root.getChild("MouthBottom");
        this.ToungBase = root.getChild("ToungBase");
        this.MiddleTounge = root.getChild("MiddleTounge");
        this.EyeRight = root.getChild("EyeRight");
        this.EyeLeft = root.getChild("EyeLeft");
        this.MouthTop = root.getChild("MouthTop");
        this.Head = root.getChild("Head");
        this.FangRight = root.getChild("FangRight");
        this.FangLeft = root.getChild("FangLeft");
        this.ForkRight = root.getChild("ForkRight");
        this.ForkLeft = root.getChild("ForkLeft");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("TailTip",
                CubeListBuilder.create().texOffs(0, 90).mirror().addBox(-1.0F, -1.0F, 0.0F, 2, 5, 10),
                PartPose.offsetAndRotation(1.0F, 20.0F, 120.0F, 0.0F, -0.6981317F, 0.0F));

        partdefinition.addOrReplaceChild("Neck",
                CubeListBuilder.create().texOffs(60, 60).mirror().addBox(-4.0F, -4.0F, -10.0F, 8, 8, 10),
                PartPose.offsetAndRotation(0.0F, 4.5F, -33.0F, -0.2617994F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("tBase",
                CubeListBuilder.create().texOffs(0, 31).mirror().addBox(-4.0F, -4.0F, 0.0F, 8, 8, 10),
                PartPose.offsetAndRotation(0.0F, 4.0F, -34.0F, -0.5235988F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("t2",
                CubeListBuilder.create().texOffs(0, 31).mirror().addBox(-4.0F, -4.0F, 0.0F, 8, 8, 10),
                PartPose.offsetAndRotation(0.0F, 7.0F, -27.0F, -1.047198F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("t3",
                CubeListBuilder.create().texOffs(0, 31).mirror().addBox(-4.0F, -4.0F, 0.0F, 8, 8, 10),
                PartPose.offsetAndRotation(0.0F, 14.0F, -24.0F, -0.5235988F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("t4",
                CubeListBuilder.create().texOffs(0, 31).mirror().addBox(-4.0F, -4.0F, 0.0F, 8, 8, 10),
                PartPose.offsetAndRotation(0.0F, 19.0F, -17.0F, -0.0872665F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("t5",
                CubeListBuilder.create().texOffs(0, 31).mirror().addBox(-4.0F, -4.0F, 0.0F, 8, 8, 10),
                PartPose.offset(0.0F, 20.0F, -9.0F));

        partdefinition.addOrReplaceChild("t6",
                CubeListBuilder.create().texOffs(0, 31).mirror().addBox(-4.0F, -4.0F, 0.0F, 8, 8, 10),
                PartPose.offsetAndRotation(0.0F, 20.0F, -1.0F, 0.0F, 0.3490659F, 0.0F));

        partdefinition.addOrReplaceChild("t7",
                CubeListBuilder.create().texOffs(0, 31).mirror().addBox(-4.0F, -4.0F, 0.0F, 8, 8, 10),
                PartPose.offsetAndRotation(2.0F, 20.0F, 6.0F, 0.0F, 0.6981317F, 0.0F));

        partdefinition.addOrReplaceChild("t8",
                CubeListBuilder.create().texOffs(0, 31).mirror().addBox(-4.0F, -4.0F, 0.0F, 8, 8, 10),
                PartPose.offsetAndRotation(7.0F, 20.0F, 12.0F, 0.0F, 0.3490659F, 0.0F));

        partdefinition.addOrReplaceChild("t9",
                CubeListBuilder.create().texOffs(0, 31).mirror().addBox(-4.0F, -4.0F, 0.0F, 8, 8, 10),
                PartPose.offset(10.0F, 20.0F, 20.0F));

        partdefinition.addOrReplaceChild("t10",
                CubeListBuilder.create().texOffs(0, 31).mirror().addBox(-4.0F, -4.0F, 0.0F, 8, 8, 10),
                PartPose.offsetAndRotation(10.0F, 20.0F, 28.0F, 0.0F, -0.3490659F, 0.0F));

        partdefinition.addOrReplaceChild("t12",
                CubeListBuilder.create().texOffs(0, 31).mirror().addBox(-4.0F, -4.0F, 0.0F, 8, 8, 10),
                PartPose.offsetAndRotation(2.0F, 20.0F, 42.0F, 0.0F, -0.6981317F, 0.0F));

        partdefinition.addOrReplaceChild("t11",
                CubeListBuilder.create().texOffs(0, 31).mirror().addBox(-4.0F, -4.0F, 0.0F, 8, 8, 10),
                PartPose.offsetAndRotation(8.0F, 20.0F, 35.0F, 0.0F, -0.6981317F, 0.0F));

        partdefinition.addOrReplaceChild("t13",
                CubeListBuilder.create().texOffs(0, 31).mirror().addBox(-4.0F, -4.0F, 0.0F, 8, 8, 10),
                PartPose.offsetAndRotation(-4.0F, 20.0F, 48.0F, 0.0F, -0.3490659F, 0.0F));

        partdefinition.addOrReplaceChild("t14",
                CubeListBuilder.create().texOffs(0, 51).mirror().addBox(-3.0F, -3.0F, 0.0F, 6, 7, 10),
                PartPose.offset(-8.0F, 20.0F, 56.0F));

        partdefinition.addOrReplaceChild("t15",
                CubeListBuilder.create().texOffs(0, 51).mirror().addBox(-3.0F, -3.0F, 0.0F, 6, 7, 10),
                PartPose.offsetAndRotation(-8.0F, 20.0F, 65.0F, 0.0F, 0.3490659F, 0.0F));

        partdefinition.addOrReplaceChild("t16",
                CubeListBuilder.create().texOffs(0, 51).mirror().addBox(-3.0F, -3.0F, 0.0F, 6, 7, 10),
                PartPose.offsetAndRotation(-5.0F, 20.0F, 73.0F, 0.0F, 0.6981317F, 0.0F));

        partdefinition.addOrReplaceChild("t17",
                CubeListBuilder.create().texOffs(0, 70).mirror().addBox(-2.0F, -2.0F, 0.0F, 4, 6, 10),
                PartPose.offsetAndRotation(1.0F, 20.0F, 80.0F, 0.0F, 0.6981317F, 0.0F));

        partdefinition.addOrReplaceChild("t18",
                CubeListBuilder.create().texOffs(0, 70).mirror().addBox(-2.0F, -2.0F, 0.0F, 4, 6, 10),
                PartPose.offsetAndRotation(7.0F, 20.0F, 87.0F, 0.0F, 0.3490659F, 0.0F));

        partdefinition.addOrReplaceChild("t19",
                CubeListBuilder.create().texOffs(0, 70).mirror().addBox(-2.0F, -2.0F, 0.0F, 4, 6, 10),
                PartPose.offset(10.0F, 20.0F, 95.0F));

        partdefinition.addOrReplaceChild("t20",
                CubeListBuilder.create().texOffs(0, 90).mirror().addBox(-1.0F, -1.0F, 0.0F, 2, 5, 10),
                PartPose.offsetAndRotation(10.0F, 20.0F, 104.0F, 0.0F, -0.3490659F, 0.0F));

        partdefinition.addOrReplaceChild("t21",
                CubeListBuilder.create().texOffs(0, 90).mirror().addBox(-1.0F, -1.0F, 0.0F, 2, 5, 10),
                PartPose.offsetAndRotation(7.0F, 20.0F, 113.0F, 0.0F, -0.6981317F, 0.0F));

        partdefinition.addOrReplaceChild("MouthBottom",
                CubeListBuilder.create().texOffs(58, 78).mirror().addBox(-4.0F, 0.0F, -12.0F, 8, 2, 12),
                PartPose.offsetAndRotation(0.0F, 4.0F, -42.0F, 0.5235988F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("ToungBase",
                CubeListBuilder.create().texOffs(70, 17).mirror().addBox(-1.0F, -2.0F, -11.0F, 2, 1, 6),
                PartPose.offsetAndRotation(0.0F, 6.0F, -40.0F, 0.2617994F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("MiddleTounge",
                CubeListBuilder.create().texOffs(70, 10).mirror().addBox(-1.0F, -1.0F, -17.0F, 2, 1, 6),
                PartPose.offsetAndRotation(0.0F, 6.0F, -40.0F, 0.1745329F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("EyeRight",
                CubeListBuilder.create().texOffs(96, 60).mirror().addBox(-7.0F, -7.0F, -3.0F, 1, 3, 4),
                PartPose.offsetAndRotation(0.0F, 6.0F, -40.0F, 0.3490659F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("EyeLeft",
                CubeListBuilder.create().texOffs(50, 60).mirror().addBox(6.0F, -7.0F, -3.0F, 1, 3, 4),
                PartPose.offsetAndRotation(0.0F, 6.0F, -40.0F, 0.3490659F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("MouthTop",
                CubeListBuilder.create().texOffs(52, 24).mirror().addBox(-5.0F, -6.0F, -16.0F, 10, 6, 16),
                PartPose.offset(0.0F, 6.0F, -40.0F));

        partdefinition.addOrReplaceChild("Head",
                CubeListBuilder.create().texOffs(60, 46).mirror().addBox(-6.0F, -8.0F, -6.0F, 12, 8, 6),
                PartPose.offset(0.0F, 6.0F, -40.0F));

        partdefinition.addOrReplaceChild("FangRight",
                CubeListBuilder.create().texOffs(92, 18).mirror().addBox(-4.0F, -3.0F, -15.0F, 1, 5, 1),
                PartPose.offsetAndRotation(0.0F, 6.0F, -40.0F, 0.1745329F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("FangLeft",
                CubeListBuilder.create().texOffs(60, 18).mirror().addBox(3.0F, -3.0F, -15.0F, 1, 5, 1),
                PartPose.offsetAndRotation(0.0F, 6.0F, -40.0F, 0.1745329F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("ForkRight",
                CubeListBuilder.create().texOffs(60, 3).mirror().addBox(6.0F, 0.6F, -21.0F, 2, 1, 6),
                PartPose.offsetAndRotation(0.0F, 6.0F, -40.0F, 0.0872665F, 0.4363323F, 0.0F));

        partdefinition.addOrReplaceChild("ForkLeft",
                CubeListBuilder.create().texOffs(80, 3).mirror().addBox(-8.0F, 0.6F, -21.0F, 2, 1, 6),
                PartPose.offsetAndRotation(0.0F, 6.0F, -40.0F, 0.0872665F, -0.4363323F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(SeaViper entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float newangle = 0.0f;
        if (limbSwingAmount < 0.0f) {
        limbSwingAmount = 0.0f;
        }
        this.tBase.yRot = newangle = Mth.cos((float)(ageInTicks * 1.3f * limbSwingAmount)) * (float)Math.PI * 0.1f * limbSwingAmount;
        this.doseg(this.tBase, this.t2, 2.0f, limbSwingAmount, ageInTicks);
        this.doseg(this.t2, this.t3, 2.0f, limbSwingAmount, ageInTicks);
        this.doseg(this.t3, this.t4, 3.0f, limbSwingAmount, ageInTicks);
        this.doseg(this.t4, this.t5, 4.0f, limbSwingAmount, ageInTicks);
        this.doseg(this.t5, this.t6, 5.0f, limbSwingAmount, ageInTicks);
        this.doseg(this.t6, this.t7, 6.0f, limbSwingAmount, ageInTicks);
        this.doseg(this.t7, this.t8, 7.0f, limbSwingAmount, ageInTicks);
        this.doseg(this.t8, this.t9, 8.0f, limbSwingAmount, ageInTicks);
        this.doseg(this.t9, this.t10, 9.0f, limbSwingAmount, ageInTicks);
        this.doseg(this.t10, this.t11, 10.0f, limbSwingAmount, ageInTicks);
        this.doseg(this.t11, this.t12, 11.0f, limbSwingAmount, ageInTicks);
        this.doseg(this.t12, this.t13, 12.0f, limbSwingAmount, ageInTicks);
        this.doseg(this.t13, this.t14, 13.0f, limbSwingAmount, ageInTicks);
        this.doseg(this.t14, this.t15, 14.0f, limbSwingAmount, ageInTicks);
        this.doseg(this.t15, this.t16, 15.0f, limbSwingAmount, ageInTicks);
        this.doseg(this.t16, this.t17, 16.0f, limbSwingAmount, ageInTicks);
        this.doseg(this.t17, this.t18, 17.0f, limbSwingAmount, ageInTicks);
        this.doseg(this.t18, this.t19, 18.0f, limbSwingAmount, ageInTicks);
        this.doseg(this.t19, this.t20, 19.0f, limbSwingAmount, ageInTicks);
        this.doseg(this.t20, this.t21, 20.0f, limbSwingAmount, ageInTicks);
        this.doseg(this.t21, this.TailTip, 21.0f, limbSwingAmount, ageInTicks);
        if (entity.getAttacking() != 0) {
        newangle = Mth.cos((float)(ageInTicks * 1.7f * limbSwingAmount)) * (float)Math.PI * 0.17f;
        this.MouthBottom.xRot = 0.65f + newangle;
        newangle = Mth.cos((float)(ageInTicks * 4.7f * limbSwingAmount)) * (float)Math.PI * 0.07f;
        this.ToungBase.xRot = 0.261f + newangle;
        this.MiddleTounge.xRot = 0.174f + newangle;
        this.ForkLeft.xRot = 0.087f + newangle;
        this.ForkRight.xRot = 0.087f + newangle;
        this.ForkLeft.zRot = this.ForkRight.zRot = (newangle = Mth.cos((float)(ageInTicks * 1.5f * limbSwingAmount)) * (float)Math.PI * 0.05f);
        this.MiddleTounge.zRot = this.ForkRight.zRot;
        this.ToungBase.zRot = this.ForkRight.zRot;
        } else {
        newangle = Mth.cos((float)(ageInTicks * 0.2f * limbSwingAmount)) * (float)Math.PI * 0.02f;
        this.MouthBottom.xRot = 0.45f + newangle;
        newangle = Mth.cos((float)(ageInTicks * 1.7f * limbSwingAmount)) * (float)Math.PI * 0.03f;
        this.ToungBase.xRot = 0.261f + newangle;
        this.MiddleTounge.xRot = 0.174f + newangle;
        this.ForkLeft.xRot = 0.087f + newangle;
        this.ForkRight.xRot = 0.087f + newangle;
        this.ForkLeft.zRot = this.ForkRight.zRot = (newangle = Mth.cos((float)(ageInTicks * 0.5f * limbSwingAmount)) * (float)Math.PI * 0.05f);
        this.MiddleTounge.zRot = this.ForkRight.zRot;
        this.ToungBase.zRot = this.ForkRight.zRot;
        }
        this.EyeLeft.yRot = this.EyeRight.yRot = (newangle = (float)Math.toRadians(netHeadYaw) * 0.5f);
        this.MouthTop.yRot = this.EyeRight.yRot;
        this.Head.yRot = this.EyeRight.yRot;
        this.FangLeft.yRot = this.FangRight.yRot = newangle;
        this.MouthBottom.yRot = newangle;
        this.MouthBottom.z = this.Head.z - (float)Math.cos(this.Head.yRot) * 2.0f;
        this.MouthBottom.x = this.Head.x - (float)Math.sin(this.Head.yRot) * 2.0f;
        this.ToungBase.yRot = newangle;
        this.MiddleTounge.yRot = newangle;
        this.ForkLeft.yRot = newangle - 0.436f;
        this.ForkRight.yRot = newangle + 0.436f;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.TailTip.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Neck.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tBase.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.t2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.t3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.t4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.t5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.t6.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.t7.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.t8.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.t9.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.t10.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.t12.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.t11.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.t13.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.t14.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.t15.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.t16.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.t17.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.t18.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.t19.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.t20.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.t21.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.MouthBottom.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.ToungBase.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.MiddleTounge.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.EyeRight.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.EyeLeft.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.MouthTop.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Head.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.FangRight.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.FangLeft.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.ForkRight.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.ForkLeft.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }

    private void doseg(ModelPart inn, ModelPart notinn, float segIndex, float limbSwingAmount, float ageInTicks) {
        float pi4 = 0.7853982f;
        notinn.z = (float)((double)inn.z + (double)((float)Math.cos(inn.yRot)) * (9.0 * Math.abs(Math.cos(inn.xRot))));
        notinn.x = (float)((double)inn.x + (double)((float)Math.sin(inn.yRot) * 9.0f) * Math.abs(Math.cos(inn.xRot)));
        float newangle = Mth.cos((float)(ageInTicks * 1.3f * this.wingspeed - pi4 * segIndex)) * (float)Math.PI * 0.2f * limbSwingAmount;
        float a = Mth.cos((float)(-(pi4 * segIndex)));
        notinn.yRot = newangle + a - a * limbSwingAmount;
    }
}
