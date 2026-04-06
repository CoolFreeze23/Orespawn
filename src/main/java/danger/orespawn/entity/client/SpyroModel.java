package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.EntitySpyro;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class SpyroModel extends EntityModel<EntitySpyro> {
    private final ModelPart RightFrontPaw;
    private final ModelPart WingLeft;
    private final ModelPart LegRightFrontTop;
    private final ModelPart LegRightFrontBottom;
    private final ModelPart LegRightBackTop;
    private final ModelPart LegRightBackBottom;
    private final ModelPart RightBackPaw;
    private final ModelPart LegLeftFrontTop;
    private final ModelPart SnoutRight;
    private final ModelPart LeftFrontPaw;
    private final ModelPart LegLeftBackTop;
    private final ModelPart LegLeftBackBottom;
    private final ModelPart LeftBackPaw;
    private final ModelPart LegLeftFrontBottom;
    private final ModelPart TailPieceSmall;
    private final ModelPart JawPiece;
    private final ModelPart HeadPieceBottom;
    private final ModelPart HeadPieceTop;
    private final ModelPart HornRightBottom;
    private final ModelPart HornLeftBottom;
    private final ModelPart HornRightTop;
    private final ModelPart HornLeftTop;
    private final ModelPart Torso;
    private final ModelPart SnoutLeft;
    private final ModelPart WingPieceLeft;
    private final ModelPart WingRight;
    private final ModelPart WingPieceRight;
    private final ModelPart Neck;
    private final ModelPart TailBack;
    private final ModelPart TailFront;
    private final ModelPart ScaleBackHead;
    private final ModelPart TailPieceLarge;
    private final ModelPart ScaleTailPiece;
    private final ModelPart ScaleHead;
    private final ModelPart ScaleTop1;
    private final ModelPart ScaleBackPiece1;
    private final ModelPart ScaleBackPiece2;

    public SpyroModel(ModelPart root) {
        this.RightFrontPaw = root.getChild("RightFrontPaw");
        this.WingLeft = root.getChild("WingLeft");
        this.LegRightFrontTop = root.getChild("LegRightFrontTop");
        this.LegRightFrontBottom = root.getChild("LegRightFrontBottom");
        this.LegRightBackTop = root.getChild("LegRightBackTop");
        this.LegRightBackBottom = root.getChild("LegRightBackBottom");
        this.RightBackPaw = root.getChild("RightBackPaw");
        this.LegLeftFrontTop = root.getChild("LegLeftFrontTop");
        this.SnoutRight = root.getChild("SnoutRight");
        this.LeftFrontPaw = root.getChild("LeftFrontPaw");
        this.LegLeftBackTop = root.getChild("LegLeftBackTop");
        this.LegLeftBackBottom = root.getChild("LegLeftBackBottom");
        this.LeftBackPaw = root.getChild("LeftBackPaw");
        this.LegLeftFrontBottom = root.getChild("LegLeftFrontBottom");
        this.TailPieceSmall = root.getChild("TailPieceSmall");
        this.JawPiece = root.getChild("JawPiece");
        this.HeadPieceBottom = root.getChild("HeadPieceBottom");
        this.HeadPieceTop = root.getChild("HeadPieceTop");
        this.HornRightBottom = root.getChild("HornRightBottom");
        this.HornLeftBottom = root.getChild("HornLeftBottom");
        this.HornRightTop = root.getChild("HornRightTop");
        this.HornLeftTop = root.getChild("HornLeftTop");
        this.Torso = root.getChild("Torso");
        this.SnoutLeft = root.getChild("SnoutLeft");
        this.WingPieceLeft = root.getChild("WingPieceLeft");
        this.WingRight = root.getChild("WingRight");
        this.WingPieceRight = root.getChild("WingPieceRight");
        this.Neck = root.getChild("Neck");
        this.TailBack = root.getChild("TailBack");
        this.TailFront = root.getChild("TailFront");
        this.ScaleBackHead = root.getChild("ScaleBackHead");
        this.TailPieceLarge = root.getChild("TailPieceLarge");
        this.ScaleTailPiece = root.getChild("ScaleTailPiece");
        this.ScaleHead = root.getChild("ScaleHead");
        this.ScaleTop1 = root.getChild("ScaleTop1");
        this.ScaleBackPiece1 = root.getChild("ScaleBackPiece1");
        this.ScaleBackPiece2 = root.getChild("ScaleBackPiece2");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("RightFrontPaw",
                CubeListBuilder.create().texOffs(12, 31).mirror()
                        .addBox(0.0F, 5.0F, -4.0F, 2, 1, 4),
                PartPose.offset(3.0F, 18.0F, -2.0F));

        partdefinition.addOrReplaceChild("WingLeft",
                CubeListBuilder.create().texOffs(2, 51).mirror()
                        .addBox(-10.0F, -1.0F, -2.0F, 10, 0, 4),
                PartPose.offsetAndRotation(-1.0F, 16.0F, 0.0F, 0.1745329F, 0.0F, -0.1745329F));

        partdefinition.addOrReplaceChild("LegRightFrontTop",
                CubeListBuilder.create().texOffs(20, 19).mirror()
                        .addBox(0.0F, 0.0F, -2.0F, 2, 3, 3),
                PartPose.offsetAndRotation(3.0F, 18.0F, -2.0F, -0.0872665F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("LegRightFrontBottom",
                CubeListBuilder.create().texOffs(0, 25).mirror()
                        .addBox(0.0F, 2.0F, -1.5F, 2, 4, 2),
                PartPose.offsetAndRotation(3.0F, 18.0F, -2.0F, -0.1745329F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("LegRightBackTop",
                CubeListBuilder.create().texOffs(30, 19).mirror()
                        .addBox(0.0F, 0.0F, -2.0F, 2, 3, 3),
                PartPose.offsetAndRotation(3.0F, 18.0F, 3.0F, 0.1396263F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("LegRightBackBottom",
                CubeListBuilder.create().texOffs(16, 25).mirror()
                        .addBox(0.0F, 2.0F, -1.0F, 2, 4, 2),
                PartPose.offsetAndRotation(3.0F, 18.0F, 3.0F, -0.1745329F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("RightBackPaw",
                CubeListBuilder.create().texOffs(36, 31).mirror()
                        .addBox(0.0F, 5.0F, -3.0F, 2, 1, 4),
                PartPose.offset(3.0F, 18.0F, 3.0F));

        partdefinition.addOrReplaceChild("LegLeftFrontTop",
                CubeListBuilder.create().texOffs(0, 19).mirror()
                        .addBox(-2.0F, 0.0F, -1.0F, 2, 3, 3),
                PartPose.offsetAndRotation(-2.0F, 18.0F, -3.0F, -0.0872665F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("SnoutRight",
                CubeListBuilder.create().texOffs(48, 2).mirror()
                        .addBox(1.0F, -3.0F, -5.0F, 1, 1, 1),
                PartPose.offset(1.0F, 16.0F, -3.0F));

        partdefinition.addOrReplaceChild("LeftFrontPaw",
                CubeListBuilder.create().texOffs(0, 31).mirror()
                        .addBox(-2.0F, 5.0F, -3.0F, 2, 1, 4),
                PartPose.offset(-2.0F, 18.0F, -3.0F));

        partdefinition.addOrReplaceChild("LegLeftBackTop",
                CubeListBuilder.create().texOffs(10, 19).mirror()
                        .addBox(-2.0F, 0.0F, -2.0F, 2, 3, 3),
                PartPose.offsetAndRotation(-2.0F, 18.0F, 3.0F, 0.1396263F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("LegLeftBackBottom",
                CubeListBuilder.create().texOffs(24, 25).mirror()
                        .addBox(-2.0F, 2.0F, -1.0F, 2, 4, 2),
                PartPose.offsetAndRotation(-2.0F, 18.0F, 3.0F, -0.1745329F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("LeftBackPaw",
                CubeListBuilder.create().texOffs(24, 31).mirror()
                        .addBox(-2.0F, 5.0F, -3.0F, 2, 1, 4),
                PartPose.offset(-2.0F, 18.0F, 3.0F));

        partdefinition.addOrReplaceChild("LegLeftFrontBottom",
                CubeListBuilder.create().texOffs(8, 25).mirror()
                        .addBox(-2.0F, 2.0F, -0.5F, 2, 4, 2),
                PartPose.offsetAndRotation(-2.0F, 18.0F, -3.0F, -0.1745329F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("TailPieceSmall",
                CubeListBuilder.create().texOffs(28, 36).mirror()
                        .addBox(0.0F, -0.5F, 4.0F, 1, 1, 1),
                PartPose.offsetAndRotation(0.0F, 16.0F, 7.0F, 0.1745329F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("JawPiece",
                CubeListBuilder.create().texOffs(52, 0).mirror()
                        .addBox(-2.0F, -1.0F, -4.0F, 3, 1, 3),
                PartPose.offsetAndRotation(1.0F, 16.0F, -3.0F, 0.1745329F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("HeadPieceBottom",
                CubeListBuilder.create().texOffs(30, 7).mirror()
                        .addBox(-3.0F, -2.0F, -5.0F, 5, 2, 6),
                PartPose.offset(1.0F, 16.0F, -3.0F));

        partdefinition.addOrReplaceChild("HeadPieceTop",
                CubeListBuilder.create().texOffs(30, 0).mirror()
                        .addBox(-3.0F, -5.0F, -3.0F, 5, 3, 4),
                PartPose.offset(1.0F, 16.0F, -3.0F));

        partdefinition.addOrReplaceChild("HornRightBottom",
                CubeListBuilder.create().texOffs(8, 14).mirror()
                        .addBox(0.0F, -6.0F, -3.5F, 2, 3, 2),
                PartPose.offsetAndRotation(1.0F, 16.0F, -3.0F, -0.7853982F, 0.7853982F, 0.0F));

        partdefinition.addOrReplaceChild("HornLeftBottom",
                CubeListBuilder.create().texOffs(0, 14).mirror()
                        .addBox(-2.75F, -6.5F, -3.0F, 2, 3, 2),
                PartPose.offsetAndRotation(1.0F, 16.0F, -3.0F, -0.7853982F, -0.7853982F, 0.0F));

        partdefinition.addOrReplaceChild("HornRightTop",
                CubeListBuilder.create().texOffs(20, 14).mirror()
                        .addBox(0.5F, -9.0F, -3.0F, 1, 3, 1),
                PartPose.offsetAndRotation(1.0F, 16.0F, -3.0F, -0.7853982F, 0.7853982F, 0.0F));

        partdefinition.addOrReplaceChild("HornLeftTop",
                CubeListBuilder.create().texOffs(16, 14).mirror()
                        .addBox(-2.2F, -9.5F, -2.5F, 1, 3, 1),
                PartPose.offsetAndRotation(1.0F, 16.0F, -3.0F, -0.7853982F, -0.7853982F, 0.0F));

        partdefinition.addOrReplaceChild("Torso",
                CubeListBuilder.create().texOffs(0, 0).mirror()
                        .addBox(-2.0F, -2.0F, -5.0F, 5, 4, 10),
                PartPose.offset(0.0F, 19.0F, 0.0F));

        partdefinition.addOrReplaceChild("SnoutLeft",
                CubeListBuilder.create().texOffs(48, 0).mirror()
                        .addBox(-3.0F, -3.0F, -5.0F, 1, 1, 1),
                PartPose.offset(1.0F, 16.0F, -3.0F));

        partdefinition.addOrReplaceChild("WingPieceLeft",
                CubeListBuilder.create().texOffs(4, 42).mirror()
                        .addBox(-1.0F, -2.0F, -1.0F, 1, 2, 1),
                PartPose.offsetAndRotation(0.0F, 17.2F, 0.0F, 0.1745329F, 0.0F, -0.1745329F));

        partdefinition.addOrReplaceChild("WingRight",
                CubeListBuilder.create().texOffs(2, 45).mirror()
                        .addBox(0.0F, -1.0F, -2.0F, 10, 0, 4),
                PartPose.offsetAndRotation(2.0F, 16.0F, 0.0F, 0.1745329F, 0.0F, 0.1745329F));

        partdefinition.addOrReplaceChild("WingPieceRight",
                CubeListBuilder.create().texOffs(0, 42).mirror()
                        .addBox(-1.0F, -2.0F, 0.0F, 1, 2, 1),
                PartPose.offsetAndRotation(2.0F, 17.5F, -1.0F, 0.1745329F, 0.0F, 0.1745329F));

        partdefinition.addOrReplaceChild("Neck",
                CubeListBuilder.create().texOffs(52, 7).mirror()
                        .addBox(-1.0F, -2.0F, -1.0F, 3, 3, 3),
                PartPose.offsetAndRotation(0.0F, 17.0F, -4.0F, 0.4537856F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("TailBack",
                CubeListBuilder.create().texOffs(0, 36).mirror()
                        .addBox(-1.0F, -1.0F, -1.0F, 2, 2, 4),
                PartPose.offsetAndRotation(0.5F, 17.5F, 5.0F, 0.4537856F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("TailFront",
                CubeListBuilder.create().texOffs(12, 36).mirror()
                        .addBox(0.0F, 0.0F, -1.0F, 1, 1, 4),
                PartPose.offsetAndRotation(0.0F, 16.0F, 7.0F, 0.2617994F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("ScaleBackHead",
                CubeListBuilder.create().texOffs(38, 36).mirror()
                        .addBox(-1.0F, -3.0F, 2.0F, 1, 2, 1),
                PartPose.offset(1.0F, 16.0F, -4.0F));

        partdefinition.addOrReplaceChild("TailPieceLarge",
                CubeListBuilder.create().texOffs(22, 36).mirror()
                        .addBox(0.0F, -1.0F, 2.0F, 1, 2, 2),
                PartPose.offsetAndRotation(0.0F, 16.0F, 7.0F, 0.1745329F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("ScaleTailPiece",
                CubeListBuilder.create().texOffs(48, 36).mirror()
                        .addBox(-0.5F, -2.0F, 0.2F, 1, 1, 2),
                PartPose.offsetAndRotation(0.5F, 17.5F, 5.0F, 0.4537856F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("ScaleHead",
                CubeListBuilder.create().texOffs(42, 36).mirror()
                        .addBox(-1.0F, -6.0F, 0.0F, 1, 2, 2),
                PartPose.offset(1.0F, 16.0F, -3.0F));

        partdefinition.addOrReplaceChild("ScaleTop1",
                CubeListBuilder.create().texOffs(48, 36).mirror()
                        .addBox(-1.0F, -6.0F, -4.0F, 1, 1, 2),
                PartPose.offset(1.0F, 16.0F, -2.0F));

        partdefinition.addOrReplaceChild("ScaleBackPiece1",
                CubeListBuilder.create().texOffs(48, 36).mirror()
                        .addBox(0.0F, -1.0F, -1.0F, 1, 1, 2),
                PartPose.offset(0.0F, 17.0F, 0.0F));

        partdefinition.addOrReplaceChild("ScaleBackPiece2",
                CubeListBuilder.create().texOffs(48, 36).mirror()
                        .addBox(0.0F, -1.0F, -1.0F, 1, 1, 2),
                PartPose.offset(0.0F, 17.0F, 3.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(EntitySpyro entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float ws = limbSwingAmount;
        int currentActivity = entity.getActivity();

        float newangle = (double) limbSwingAmount > 0.1 ? Mth.cos(ageInTicks * 2.3F * ws) * (float) Math.PI * 0.4F * limbSwingAmount : 0.0F;
        if (currentActivity == 3) {
            newangle *= 0.5F;
        }
        this.WingLeft.zRot = newangle;
        this.WingRight.zRot = -newangle;

        newangle = (double) limbSwingAmount > 0.1 ? Mth.cos(ageInTicks * 2.0F * ws) * (float) Math.PI * 0.25F * limbSwingAmount : 0.0F;
        if (currentActivity == 3) {
            newangle = 0.0F;
        }

        if (currentActivity != 2) {
            this.LegRightFrontTop.xRot = newangle - 0.087F;
            this.LegRightFrontBottom.xRot = newangle - 0.17F;
            this.RightFrontPaw.xRot = newangle;
            this.LegLeftFrontTop.xRot = -newangle - 0.087F;
            this.LegLeftFrontBottom.xRot = -newangle - 0.17F;
            this.LeftFrontPaw.xRot = -newangle;
            this.LegRightBackBottom.xRot = -newangle + 0.139F;
            this.LegRightBackTop.xRot = -newangle - 0.174F;
            this.RightBackPaw.xRot = -newangle;
            this.LegLeftBackBottom.xRot = newangle + 0.139F;
            this.LegLeftBackTop.xRot = newangle - 0.174F;
            this.LeftBackPaw.xRot = newangle;
        } else {
            newangle = -1.0F;
            this.LegRightFrontTop.xRot = newangle - 0.087F;
            this.LegRightFrontBottom.xRot = newangle - 0.17F;
            this.RightFrontPaw.xRot = newangle;
            this.LegLeftFrontTop.xRot = newangle - 0.087F;
            this.LegLeftFrontBottom.xRot = newangle - 0.17F;
            this.LeftFrontPaw.xRot = newangle;
            newangle = 1.0F;
            this.LegRightBackBottom.xRot = newangle + 0.139F;
            this.LegRightBackTop.xRot = newangle - 0.174F;
            this.RightBackPaw.xRot = newangle;
            this.LegLeftBackBottom.xRot = newangle + 0.139F;
            this.LegLeftBackTop.xRot = newangle - 0.174F;
            this.LeftBackPaw.xRot = newangle;
        }

        newangle = Mth.cos(ageInTicks * 1.2F * ws) * (float) Math.PI * 0.25F;
        if (entity.isInSittingPose() || currentActivity == 3) {
            newangle = 0.0F;
        }
        this.TailBack.yRot = newangle;
        this.ScaleTailPiece.yRot = newangle;
        this.TailFront.z = this.TailBack.z + (float) Math.cos(this.TailBack.yRot) * 3.0F;
        this.TailFront.x = this.TailBack.x + (float) Math.sin(this.TailBack.yRot) * 3.0F - 0.5F;
        this.TailFront.yRot = newangle * 1.6F;
        this.TailPieceLarge.z = this.TailFront.z;
        this.TailPieceLarge.x = this.TailFront.x;
        this.TailPieceLarge.yRot = this.TailFront.yRot;
        this.TailPieceSmall.z = this.TailFront.z;
        this.TailPieceSmall.x = this.TailFront.x;
        this.TailPieceSmall.yRot = this.TailFront.yRot;

        this.HeadPieceTop.yRot = (float) Math.toRadians(netHeadYaw);
        this.HeadPieceBottom.yRot = (float) Math.toRadians(netHeadYaw);
        this.JawPiece.yRot = (float) Math.toRadians(netHeadYaw);
        this.SnoutRight.yRot = (float) Math.toRadians(netHeadYaw);
        this.SnoutLeft.yRot = (float) Math.toRadians(netHeadYaw);
        this.ScaleTop1.yRot = (float) Math.toRadians(netHeadYaw);
        this.ScaleHead.yRot = (float) Math.toRadians(netHeadYaw);
        this.ScaleBackHead.yRot = (float) Math.toRadians(netHeadYaw);
        this.HornRightBottom.yRot = (float) Math.toRadians(netHeadYaw) + 0.785F;
        this.HornRightTop.yRot = (float) Math.toRadians(netHeadYaw) + 0.785F;
        this.HornLeftBottom.yRot = (float) Math.toRadians(netHeadYaw) - 0.785F;
        this.HornLeftTop.yRot = (float) Math.toRadians(netHeadYaw) - 0.785F;

        this.HeadPieceTop.xRot = (float) Math.toRadians(headPitch);
        this.HeadPieceBottom.xRot = (float) Math.toRadians(headPitch);
        this.JawPiece.xRot = (float) Math.toRadians(headPitch);
        this.SnoutRight.xRot = (float) Math.toRadians(headPitch);
        this.SnoutLeft.xRot = (float) Math.toRadians(headPitch);
        this.ScaleTop1.xRot = (float) Math.toRadians(headPitch);
        this.ScaleHead.xRot = (float) Math.toRadians(headPitch);
        this.ScaleBackHead.xRot = (float) Math.toRadians(headPitch);
        this.HornRightBottom.xRot = (float) Math.toRadians(headPitch) - 0.785F;
        this.HornRightTop.xRot = (float) Math.toRadians(headPitch) - 0.785F;
        this.HornLeftBottom.xRot = (float) Math.toRadians(headPitch) - 0.785F;
        this.HornLeftTop.xRot = (float) Math.toRadians(headPitch) - 0.785F;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.RightFrontPaw.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.WingLeft.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.LegRightFrontTop.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.LegRightFrontBottom.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.LegRightBackTop.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.LegRightBackBottom.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.RightBackPaw.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.LegLeftFrontTop.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.SnoutRight.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.LeftFrontPaw.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.LegLeftBackTop.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.LegLeftBackBottom.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.LeftBackPaw.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.LegLeftFrontBottom.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.TailPieceSmall.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.JawPiece.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.HeadPieceBottom.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.HeadPieceTop.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.HornRightBottom.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.HornLeftBottom.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.HornRightTop.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.HornLeftTop.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Torso.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.SnoutLeft.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.WingPieceLeft.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.WingRight.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.WingPieceRight.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Neck.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.TailBack.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.TailFront.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.ScaleBackHead.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.TailPieceLarge.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.ScaleTailPiece.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.ScaleHead.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.ScaleTop1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.ScaleBackPiece1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.ScaleBackPiece2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
