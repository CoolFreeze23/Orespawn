package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.EntityBee;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class BeeModel extends EntityModel<EntityBee> {
    private final ModelPart Sting;
    private final ModelPart Abdomnem1;
    private final ModelPart Abdomnem2;
    private final ModelPart Abdomnem3;
    private final ModelPart Abdomnem4;
    private final ModelPart Abdomnem5;
    private final ModelPart MainBody;
    private final ModelPart Neck;
    private final ModelPart Head;
    private final ModelPart WingRight;
    private final ModelPart WingLeft;
    private final ModelPart RA1;
    private final ModelPart LA1;
    private final ModelPart LA2;
    private final ModelPart RA2;
    private final ModelPart RA3;
    private final ModelPart LA3;
    private final ModelPart LeftPom;
    private final ModelPart RightPom;
    private final ModelPart LeftPincerExtra;
    private final ModelPart LeftPincerMain;
    private final ModelPart RightPincerMain;
    private final ModelPart RightPincerExtra;

    public BeeModel(ModelPart root) {
        this.Sting = root.getChild("Sting");
        this.Abdomnem1 = root.getChild("Abdomnem1");
        this.Abdomnem2 = root.getChild("Abdomnem2");
        this.Abdomnem3 = root.getChild("Abdomnem3");
        this.Abdomnem4 = root.getChild("Abdomnem4");
        this.Abdomnem5 = root.getChild("Abdomnem5");
        this.MainBody = root.getChild("MainBody");
        this.Neck = root.getChild("Neck");
        this.Head = root.getChild("Head");
        this.WingRight = root.getChild("WingRight");
        this.WingLeft = root.getChild("WingLeft");
        this.RA1 = root.getChild("RA1");
        this.LA1 = root.getChild("LA1");
        this.LA2 = root.getChild("LA2");
        this.RA2 = root.getChild("RA2");
        this.RA3 = root.getChild("RA3");
        this.LA3 = root.getChild("LA3");
        this.LeftPom = root.getChild("LeftPom");
        this.RightPom = root.getChild("RightPom");
        this.LeftPincerExtra = root.getChild("LeftPincerExtra");
        this.LeftPincerMain = root.getChild("LeftPincerMain");
        this.RightPincerMain = root.getChild("RightPincerMain");
        this.RightPincerExtra = root.getChild("RightPincerExtra");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("Sting",
                CubeListBuilder.create().texOffs(68, 0).mirror()
                        .addBox(-1.0F, 0.0F, -1.0F, 2, 10, 2),
                PartPose.offsetAndRotation(0.0F, 16.0F, 1.0F, -0.7853982F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("Abdomnem1",
                CubeListBuilder.create().texOffs(64, 12).mirror()
                        .addBox(-2.0F, 0.0F, 0.0F, 4, 8, 4),
                PartPose.offsetAndRotation(0.0F, 9.0F, 2.0F, -0.5235988F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("Abdomnem2",
                CubeListBuilder.create().texOffs(60, 24).mirror()
                        .addBox(-3.0F, 0.0F, 0.0F, 6, 6, 6),
                PartPose.offset(0.0F, 5.0F, 0.0F));

        partdefinition.addOrReplaceChild("Abdomnem3",
                CubeListBuilder.create().texOffs(56, 36).mirror()
                        .addBox(-4.0F, 0.0F, 0.0F, 8, 7, 8),
                PartPose.offsetAndRotation(0.0F, 1.0F, -2.0F, 0.2617994F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("Abdomnem4",
                CubeListBuilder.create().texOffs(53, 51).mirror()
                        .addBox(-5.0F, 0.0F, 0.0F, 10, 12, 10),
                PartPose.offsetAndRotation(0.0F, -6.0F, -8.0F, 0.5934119F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("Abdomnem5",
                CubeListBuilder.create().texOffs(48, 73).mirror()
                        .addBox(-6.0F, 0.0F, 0.0F, 12, 12, 12),
                PartPose.offsetAndRotation(0.0F, -6.0F, -15.0F, 1.099557F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("MainBody",
                CubeListBuilder.create().texOffs(48, 97).mirror()
                        .addBox(-6.0F, 0.0F, -6.0F, 12, 14, 12),
                PartPose.offsetAndRotation(0.0F, -12.0F, -24.0F, 1.48353F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("Neck",
                CubeListBuilder.create().texOffs(55, 123).mirror()
                        .addBox(-4.0F, -4.0F, -8.0F, 8, 8, 8),
                PartPose.offset(0.0F, -12.0F, -23.0F));

        partdefinition.addOrReplaceChild("Head",
                CubeListBuilder.create().texOffs(51, 139).mirror()
                        .addBox(-5.0F, -5.0F, -10.0F, 10, 10, 10),
                PartPose.offsetAndRotation(0.0F, -13.0F, -28.0F, 0.2617994F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("WingRight",
                CubeListBuilder.create().texOffs(0, 91).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 0, 8, 24),
                PartPose.offsetAndRotation(-4.0F, -14.0F, -15.0F, -0.7853982F, -0.5235988F, 2.617994F));

        partdefinition.addOrReplaceChild("WingLeft",
                CubeListBuilder.create().texOffs(96, 91).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 0, 8, 24),
                PartPose.offsetAndRotation(3.0F, -14.0F, -15.0F, -0.7853982F, 0.5235988F, -2.617994F));

        partdefinition.addOrReplaceChild("RA1",
                CubeListBuilder.create().texOffs(47, 152).mirror()
                        .addBox(0.0F, -6.0F, -1.0F, 1, 6, 1),
                PartPose.offsetAndRotation(-3.0F, -17.0F, -31.0F, 0.2617994F, 0.5235988F, 0.0F));

        partdefinition.addOrReplaceChild("LA1",
                CubeListBuilder.create().texOffs(91, 152).mirror()
                        .addBox(0.0F, -6.0F, -1.0F, 1, 6, 1),
                PartPose.offsetAndRotation(2.0F, -17.0F, -32.0F, 0.2617994F, -0.5235988F, 0.0F));

        partdefinition.addOrReplaceChild("LA2",
                CubeListBuilder.create().texOffs(91, 145).mirror()
                        .addBox(0.0F, -11.0F, 0.0F, 1, 6, 1),
                PartPose.offsetAndRotation(2.0F, -17.0F, -32.0F, 0.4363323F, -0.6108652F, 0.0F));

        partdefinition.addOrReplaceChild("RA2",
                CubeListBuilder.create().texOffs(47, 145).mirror()
                        .addBox(0.0F, -11.0F, 0.0F, 1, 6, 1),
                PartPose.offsetAndRotation(-3.0F, -17.0F, -31.0F, 0.4363323F, 0.6108652F, 0.0F));

        partdefinition.addOrReplaceChild("RA3",
                CubeListBuilder.create().texOffs(47, 138).mirror()
                        .addBox(0.0F, -16.0F, 2.0F, 1, 6, 1),
                PartPose.offsetAndRotation(-3.0F, -17.0F, -31.0F, 0.6108652F, 0.6981317F, 0.0F));

        partdefinition.addOrReplaceChild("LA3",
                CubeListBuilder.create().texOffs(91, 138).mirror()
                        .addBox(0.0F, -16.0F, 2.0F, 1, 6, 1),
                PartPose.offsetAndRotation(2.0F, -17.0F, -32.0F, 0.6108652F, -0.6981317F, 0.0F));

        partdefinition.addOrReplaceChild("LeftPom",
                CubeListBuilder.create().texOffs(89, 134).mirror()
                        .addBox(4.0F, -16.0F, -6.0F, 2, 2, 2),
                PartPose.offset(2.0F, -17.0F, -32.0F));

        partdefinition.addOrReplaceChild("RightPom",
                CubeListBuilder.create().texOffs(45, 134).mirror()
                        .addBox(-5.0F, -16.0F, -7.0F, 2, 2, 2),
                PartPose.offset(-3.0F, -17.0F, -31.0F));

        partdefinition.addOrReplaceChild("LeftPincerExtra",
                CubeListBuilder.create().texOffs(71, 166).mirror()
                        .addBox(-2.0F, 0.0F, -6.0F, 2, 1, 2),
                PartPose.offsetAndRotation(2.0F, -8.0F, -36.0F, 0.1745329F, -0.1745329F, 0.0F));

        partdefinition.addOrReplaceChild("LeftPincerMain",
                CubeListBuilder.create().texOffs(71, 159).mirror()
                        .addBox(0.0F, 0.0F, -6.0F, 2, 1, 6),
                PartPose.offsetAndRotation(2.0F, -8.0F, -36.0F, 0.1745329F, -0.1745329F, 0.0F));

        partdefinition.addOrReplaceChild("RightPincerMain",
                CubeListBuilder.create().texOffs(55, 159).mirror()
                        .addBox(0.0F, 0.0F, -6.0F, 2, 1, 6),
                PartPose.offsetAndRotation(-4.0F, -8.0F, -36.0F, 0.1745329F, 0.1745329F, 0.0F));

        partdefinition.addOrReplaceChild("RightPincerExtra",
                CubeListBuilder.create().texOffs(63, 166).mirror()
                        .addBox(2.0F, 0.0F, -6.0F, 2, 1, 2),
                PartPose.offsetAndRotation(-4.0F, -8.0F, -36.0F, 0.1745329F, 0.1745329F, 0.0F));

        return LayerDefinition.create(meshdefinition, 256, 256);
    }

    @Override
    public void setupAnim(EntityBee entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float newangle = 0.0f;
        newangle = Mth.cos((float)(ageInTicks * 1.1f * limbSwingAmount)) * (float)Math.PI * 0.3f;
        this.WingLeft.zRot = -1.745f - newangle;
        this.WingRight.zRot = 1.754f + newangle;
        newangle = Mth.cos((float)(ageInTicks * 0.3f * limbSwingAmount)) * (float)Math.PI * 0.1f;
        this.LeftPincerMain.yRot = -0.274f + newangle;
        this.LeftPincerExtra.yRot = -0.274f + newangle;
        this.RightPincerMain.yRot = 0.274f - newangle;
        this.RightPincerExtra.yRot = 0.274f - newangle;
        newangle = Mth.cos((float)(ageInTicks * 0.21f * limbSwingAmount)) * (float)Math.PI * 0.06f;
        this.LA1.xRot = 0.261f + newangle;
        this.LA2.xRot = 0.436f + newangle;
        this.LA3.xRot = 0.611f + newangle;
        this.LeftPom.xRot = newangle;
        newangle = Mth.cos((float)(ageInTicks * 0.27f * limbSwingAmount)) * (float)Math.PI * 0.06f;
        this.RA1.xRot = 0.261f + newangle;
        this.RA2.xRot = 0.436f + newangle;
        this.RA3.xRot = 0.611f + newangle;
        this.RightPom.xRot = newangle;
        this.LA1.zRot = newangle = Mth.cos((float)(ageInTicks * 0.31f * limbSwingAmount)) * (float)Math.PI * 0.06f;
        this.LA2.zRot = newangle;
        this.LA3.zRot = newangle;
        this.LeftPom.zRot = newangle;
        this.RA1.zRot = newangle = Mth.cos((float)(ageInTicks * 0.37f * limbSwingAmount)) * (float)Math.PI * 0.06f;
        this.RA2.zRot = newangle;
        this.RA3.zRot = newangle;
        this.RightPom.zRot = newangle;
        newangle = entity.getAttacking() == 0 ? Mth.cos((float)(ageInTicks * 0.021f * limbSwingAmount)) * (float)Math.PI * 0.023f : Mth.cos((float)(ageInTicks * 0.11f * limbSwingAmount)) * (float)Math.PI * 0.055f;
        this.Abdomnem5.xRot = 1.099f + newangle;
        this.Abdomnem4.xRot = this.Abdomnem5.xRot + newangle - 0.35f;
        this.Abdomnem4.y = (float)((double)this.Abdomnem5.y + Math.cos(this.Abdomnem5.xRot) * 10.0);
        this.Abdomnem4.z = (float)((double)this.Abdomnem5.z + Math.sin(this.Abdomnem5.xRot) * 10.0);
        this.Abdomnem3.xRot = this.Abdomnem4.xRot + newangle - 0.35f;
        this.Abdomnem3.y = (float)((double)this.Abdomnem4.y + Math.cos(this.Abdomnem4.xRot) * 10.0);
        this.Abdomnem3.z = (float)((double)this.Abdomnem4.z + Math.sin(this.Abdomnem4.xRot) * 10.0);
        this.Abdomnem2.xRot = this.Abdomnem3.xRot + newangle - 0.35f;
        this.Abdomnem2.y = (float)((double)this.Abdomnem3.y + Math.cos(this.Abdomnem3.xRot) * 6.0);
        this.Abdomnem2.z = (float)((double)this.Abdomnem3.z + Math.sin(this.Abdomnem3.xRot) * 6.0);
        this.Abdomnem1.xRot = this.Abdomnem2.xRot + newangle - 0.35f;
        this.Abdomnem1.y = (float)((double)this.Abdomnem2.y + Math.cos(this.Abdomnem2.xRot) * 5.0);
        this.Abdomnem1.z = (float)((double)this.Abdomnem2.z + Math.sin(this.Abdomnem2.xRot) * 5.0);
        this.Sting.xRot = this.Abdomnem1.xRot + newangle - 0.35f;
        this.Sting.y = (float)((double)this.Abdomnem1.y + Math.cos(this.Abdomnem1.xRot) * 7.0);
        this.Sting.z = 1.0f + (float)((double)this.Abdomnem1.z + Math.sin(this.Abdomnem1.xRot) * 7.0);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.Sting.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Abdomnem1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Abdomnem2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Abdomnem3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Abdomnem4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Abdomnem5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.MainBody.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Neck.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Head.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.WingRight.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.WingLeft.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.RA1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.LA1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.LA2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.RA2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.RA3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.LA3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.LeftPom.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.RightPom.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.LeftPincerExtra.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.LeftPincerMain.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.RightPincerMain.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.RightPincerExtra.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
