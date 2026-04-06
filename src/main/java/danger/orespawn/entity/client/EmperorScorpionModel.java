package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.EntityEmperorScorpion;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class EmperorScorpionModel extends EntityModel<EntityEmperorScorpion> {
    private int ri1, ri2;
    private final ModelPart Head;
    private final ModelPart Seg1;
    private final ModelPart Seg2;
    private final ModelPart Seg3;
    private final ModelPart Seg4;
    private final ModelPart Seg5;
    private final ModelPart Seg6;
    private final ModelPart Seg7;
    private final ModelPart Seg8;
    private final ModelPart Tailseg1;
    private final ModelPart Tailseg2;
    private final ModelPart Tailseg3;
    private final ModelPart Tailseg4;
    private final ModelPart Tailseg5;
    private final ModelPart Tailseg6;
    private final ModelPart Tailseg7;
    private final ModelPart Tailseg8;
    private final ModelPart Stinger1;
    private final ModelPart Stinger2;
    private final ModelPart Stinger3;
    private final ModelPart LeftShoulder;
    private final ModelPart LeftArmSeg1;
    private final ModelPart LeftArmSeg2;
    private final ModelPart LeftArmSeg3;
    private final ModelPart LeftArmSeg4;
    private final ModelPart RightShoulder;
    private final ModelPart RightArmSeg1;
    private final ModelPart RightArmSeg2;
    private final ModelPart RightArmSeg3;
    private final ModelPart RightArmSeg4;
    private final ModelPart RightPincer;
    private final ModelPart LeftPincer;
    private final ModelPart Lefteye;
    private final ModelPart Righteye;
    private final ModelPart RightMandible;
    private final ModelPart LeftMandible;
    private final ModelPart RightManPart2;
    private final ModelPart LeftManPart2;
    private final ModelPart Leg1Seg1;
    private final ModelPart Leg1Seg2;
    private final ModelPart Leg1Seg3;
    private final ModelPart Leg1Seg4;
    private final ModelPart Leg1Seg5;
    private final ModelPart Leg2Seg1;
    private final ModelPart Leg2Seg2;
    private final ModelPart Leg2Seg3;
    private final ModelPart Leg2Seg4;
    private final ModelPart Leg2Seg5;
    private final ModelPart Leg3Seg1;
    private final ModelPart Leg3Seg2;
    private final ModelPart Leg3Seg3;
    private final ModelPart Leg3Seg4;
    private final ModelPart Leg3Seg5;
    private final ModelPart Leg4Seg1;
    private final ModelPart Leg4Seg2;
    private final ModelPart Leg4Seg3;
    private final ModelPart Leg4Seg4;
    private final ModelPart Leg4Seg5;
    private final ModelPart Leg5Seg1;
    private final ModelPart Leg5Seg2;
    private final ModelPart Leg5Seg3;
    private final ModelPart Leg5Seg4;
    private final ModelPart Leg5Seg5;
    private final ModelPart Leg6Seg1;
    private final ModelPart Leg6Seg2;
    private final ModelPart Leg6Seg3;
    private final ModelPart Leg6Seg4;
    private final ModelPart Leg6Seg5;
    private final ModelPart Leg7Seg1;
    private final ModelPart Leg7Seg2;
    private final ModelPart Leg7Seg3;
    private final ModelPart Leg7Seg4;
    private final ModelPart Leg7Seg5;
    private final ModelPart Leg8Seg1;
    private final ModelPart Leg8Seg2;
    private final ModelPart Leg8Seg3;
    private final ModelPart Leg8Seg4;
    private final ModelPart Leg8Seg5;

    public EmperorScorpionModel(ModelPart root) {
        this.Head = root.getChild("Head");
        this.Seg1 = root.getChild("Seg1");
        this.Seg2 = root.getChild("Seg2");
        this.Seg3 = root.getChild("Seg3");
        this.Seg4 = root.getChild("Seg4");
        this.Seg5 = root.getChild("Seg5");
        this.Seg6 = root.getChild("Seg6");
        this.Seg7 = root.getChild("Seg7");
        this.Seg8 = root.getChild("Seg8");
        this.Tailseg1 = root.getChild("Tailseg1");
        this.Tailseg2 = root.getChild("Tailseg2");
        this.Tailseg3 = root.getChild("Tailseg3");
        this.Tailseg4 = root.getChild("Tailseg4");
        this.Tailseg5 = root.getChild("Tailseg5");
        this.Tailseg6 = root.getChild("Tailseg6");
        this.Tailseg7 = root.getChild("Tailseg7");
        this.Tailseg8 = root.getChild("Tailseg8");
        this.Stinger1 = root.getChild("Stinger1");
        this.Stinger2 = root.getChild("Stinger2");
        this.Stinger3 = root.getChild("Stinger3");
        this.LeftShoulder = root.getChild("LeftShoulder");
        this.LeftArmSeg1 = root.getChild("LeftArmSeg1");
        this.LeftArmSeg2 = root.getChild("LeftArmSeg2");
        this.LeftArmSeg3 = root.getChild("LeftArmSeg3");
        this.LeftArmSeg4 = root.getChild("LeftArmSeg4");
        this.RightShoulder = root.getChild("RightShoulder");
        this.RightArmSeg1 = root.getChild("RightArmSeg1");
        this.RightArmSeg2 = root.getChild("RightArmSeg2");
        this.RightArmSeg3 = root.getChild("RightArmSeg3");
        this.RightArmSeg4 = root.getChild("RightArmSeg4");
        this.RightPincer = root.getChild("RightPincer");
        this.LeftPincer = root.getChild("LeftPincer");
        this.Lefteye = root.getChild("Lefteye");
        this.Righteye = root.getChild("Righteye");
        this.RightMandible = root.getChild("RightMandible");
        this.LeftMandible = root.getChild("LeftMandible");
        this.RightManPart2 = root.getChild("RightManPart2");
        this.LeftManPart2 = root.getChild("LeftManPart2");
        this.Leg1Seg1 = root.getChild("Leg1Seg1");
        this.Leg1Seg2 = root.getChild("Leg1Seg2");
        this.Leg1Seg3 = root.getChild("Leg1Seg3");
        this.Leg1Seg4 = root.getChild("Leg1Seg4");
        this.Leg1Seg5 = root.getChild("Leg1Seg5");
        this.Leg2Seg1 = root.getChild("Leg2Seg1");
        this.Leg2Seg2 = root.getChild("Leg2Seg2");
        this.Leg2Seg3 = root.getChild("Leg2Seg3");
        this.Leg2Seg4 = root.getChild("Leg2Seg4");
        this.Leg2Seg5 = root.getChild("Leg2Seg5");
        this.Leg3Seg1 = root.getChild("Leg3Seg1");
        this.Leg3Seg2 = root.getChild("Leg3Seg2");
        this.Leg3Seg3 = root.getChild("Leg3Seg3");
        this.Leg3Seg4 = root.getChild("Leg3Seg4");
        this.Leg3Seg5 = root.getChild("Leg3Seg5");
        this.Leg4Seg1 = root.getChild("Leg4Seg1");
        this.Leg4Seg2 = root.getChild("Leg4Seg2");
        this.Leg4Seg3 = root.getChild("Leg4Seg3");
        this.Leg4Seg4 = root.getChild("Leg4Seg4");
        this.Leg4Seg5 = root.getChild("Leg4Seg5");
        this.Leg5Seg1 = root.getChild("Leg5Seg1");
        this.Leg5Seg2 = root.getChild("Leg5Seg2");
        this.Leg5Seg3 = root.getChild("Leg5Seg3");
        this.Leg5Seg4 = root.getChild("Leg5Seg4");
        this.Leg5Seg5 = root.getChild("Leg5Seg5");
        this.Leg6Seg1 = root.getChild("Leg6Seg1");
        this.Leg6Seg2 = root.getChild("Leg6Seg2");
        this.Leg6Seg3 = root.getChild("Leg6Seg3");
        this.Leg6Seg4 = root.getChild("Leg6Seg4");
        this.Leg6Seg5 = root.getChild("Leg6Seg5");
        this.Leg7Seg1 = root.getChild("Leg7Seg1");
        this.Leg7Seg2 = root.getChild("Leg7Seg2");
        this.Leg7Seg3 = root.getChild("Leg7Seg3");
        this.Leg7Seg4 = root.getChild("Leg7Seg4");
        this.Leg7Seg5 = root.getChild("Leg7Seg5");
        this.Leg8Seg1 = root.getChild("Leg8Seg1");
        this.Leg8Seg2 = root.getChild("Leg8Seg2");
        this.Leg8Seg3 = root.getChild("Leg8Seg3");
        this.Leg8Seg4 = root.getChild("Leg8Seg4");
        this.Leg8Seg5 = root.getChild("Leg8Seg5");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("Head",
                CubeListBuilder.create().texOffs(0, 104).mirror()
                        .addBox(-9.0F, -4.0F, -16.0F, 18, 8, 16),
                PartPose.offset(0.0F, 13.0F, -8.0F));

        partdefinition.addOrReplaceChild("Seg1",
                CubeListBuilder.create().texOffs(0, 78).mirror()
                        .addBox(-9.0F, -4.0F, 0.0F, 18, 8, 4),
                PartPose.offset(0.0F, 13.0F, -8.0F));

        partdefinition.addOrReplaceChild("Seg2",
                CubeListBuilder.create().texOffs(0, 65).mirror()
                        .addBox(-8.5F, -4.1F, 4.0F, 17, 8, 4),
                PartPose.offset(0.0F, 13.0F, -8.0F));

        partdefinition.addOrReplaceChild("Seg3",
                CubeListBuilder.create().texOffs(0, 50).mirror()
                        .addBox(-9.5F, -4.0F, 8.0F, 19, 8, 5),
                PartPose.offset(0.0F, 13.0F, -8.0F));

        partdefinition.addOrReplaceChild("Seg4",
                CubeListBuilder.create().texOffs(0, 35).mirror()
                        .addBox(-9.0F, -4.1F, 13.0F, 18, 8, 6),
                PartPose.offset(0.0F, 13.0F, -8.0F));

        partdefinition.addOrReplaceChild("Seg5",
                CubeListBuilder.create().texOffs(45, 91).mirror()
                        .addBox(-8.5F, -4.0F, 19.0F, 17, 8, 3),
                PartPose.offset(0.0F, 13.0F, -8.0F));

        partdefinition.addOrReplaceChild("Seg6",
                CubeListBuilder.create().texOffs(45, 79).mirror()
                        .addBox(-8.0F, -4.1F, 22.0F, 16, 8, 3),
                PartPose.offset(0.0F, 13.0F, -8.0F));

        partdefinition.addOrReplaceChild("Seg7",
                CubeListBuilder.create().texOffs(43, 66).mirror()
                        .addBox(-7.0F, -4.0F, 25.0F, 14, 8, 3),
                PartPose.offset(0.0F, 13.0F, -8.0F));

        partdefinition.addOrReplaceChild("Seg8",
                CubeListBuilder.create().texOffs(49, 53).mirror()
                        .addBox(-5.5F, -4.1F, 28.0F, 11, 8, 2),
                PartPose.offset(0.0F, 13.0F, -8.0F));

        partdefinition.addOrReplaceChild("Tailseg1",
                CubeListBuilder.create().texOffs(92, 0).mirror()
                        .addBox(-4.0F, -1.0F, 0.0F, 8, 4, 10),
                PartPose.offsetAndRotation(0.0F, 13.0F, 20.0F, 0.5948578F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("Tailseg2",
                CubeListBuilder.create().texOffs(90, 15).mirror()
                        .addBox(-3.5F, -2.0F, 0.0F, 7, 4, 12),
                PartPose.offsetAndRotation(0.0F, 10.0F, 27.0F, 1.07818F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("Tailseg3",
                CubeListBuilder.create().texOffs(96, 32).mirror()
                        .addBox(-3.0F, -2.0F, 1.0F, 6, 4, 10),
                PartPose.offsetAndRotation(0.0F, 2.0F, 32.0F, 1.710216F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("Tailseg4",
                CubeListBuilder.create().texOffs(96, 47).mirror()
                        .addBox(-2.5F, -2.0F, 0.0F, 5, 4, 11),
                PartPose.offsetAndRotation(0.0F, -7.0F, 31.0F, 2.267895F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("Tailseg5",
                CubeListBuilder.create().texOffs(98, 63).mirror()
                        .addBox(-2.0F, -2.0F, 0.0F, 4, 4, 11),
                PartPose.offsetAndRotation(0.0F, -14.0F, 25.0F, 2.899932F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("Tailseg6",
                CubeListBuilder.create().texOffs(98, 79).mirror()
                        .addBox(-2.0F, -2.0F, 0.0F, 4, 4, 11),
                PartPose.offsetAndRotation(0.0F, -17.0F, 16.0F, -2.602503F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("Tailseg7",
                CubeListBuilder.create().texOffs(94, 95).mirror()
                        .addBox(-3.0F, -2.0F, 0.0F, 6, 4, 11),
                PartPose.offsetAndRotation(0.0F, -12.0F, 8.0F, -0.2230717F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("Tailseg8",
                CubeListBuilder.create().texOffs(102, 111).mirror()
                        .addBox(-4.0F, -2.0F, 4.0F, 8, 4, 5),
                PartPose.offsetAndRotation(0.0F, -12.0F, 8.0F, -0.2230717F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("Stinger1",
                CubeListBuilder.create().texOffs(83, 0).mirror()
                        .addBox(-0.5F, -0.5F, 0.0F, 1, 1, 3),
                PartPose.offsetAndRotation(0.0F, -10.0F, 18.0F, 0.2230717F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("Stinger2",
                CubeListBuilder.create().texOffs(83, 0).mirror()
                        .addBox(-0.5F, -0.5F, 0.0F, 1, 1, 3),
                PartPose.offsetAndRotation(0.0F, -10.5F, 20.5F, -0.2602503F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("Stinger3",
                CubeListBuilder.create().texOffs(79, 5).mirror()
                        .addBox(-0.5F, -0.5F, 0.0F, 1, 1, 5),
                PartPose.offsetAndRotation(0.0F, -10.0F, 23.0F, -0.8551081F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("LeftShoulder",
                CubeListBuilder.create().texOffs(69, 103).mirror()
                        .addBox(-3.0F, -3.0F, -4.0F, 6, 6, 4),
                PartPose.offsetAndRotation(7.0F, 13.0F, -22.0F, 0.0F, -0.8551081F, 0.0F));

        partdefinition.addOrReplaceChild("LeftArmSeg1",
                CubeListBuilder.create().texOffs(55, 0).mirror()
                        .addBox(-3.0F, -3.0F, -10.0F, 4, 6, 13),
                PartPose.offsetAndRotation(10.0F, 13.0F, -24.0F, 0.0F, -2.044824F, 0.0F));

        partdefinition.addOrReplaceChild("LeftArmSeg2",
                CubeListBuilder.create().texOffs(130, 0).mirror()
                        .addBox(-7.0F, -3.0F, -17.0F, 8, 6, 17),
                PartPose.offsetAndRotation(19.0F, 13.0F, -22.0F, 0.0F, -0.7435722F, 0.0F));

        partdefinition.addOrReplaceChild("LeftArmSeg3",
                CubeListBuilder.create().texOffs(130, 50).mirror()
                        .addBox(-3.0F, -3.0F, -24.0F, 4, 6, 24),
                PartPose.offsetAndRotation(29.0F, 13.0F, -33.0F, 0.0F, 0.3717861F, 0.0F));

        partdefinition.addOrReplaceChild("LeftArmSeg4",
                CubeListBuilder.create().texOffs(181, 0).mirror()
                        .addBox(-3.0F, -3.0F, -14.0F, 8, 6, 12),
                PartPose.offsetAndRotation(29.0F, 13.0F, -33.0F, 0.0F, 1.487144F, 0.0F));

        partdefinition.addOrReplaceChild("RightShoulder",
                CubeListBuilder.create().texOffs(69, 103).mirror()
                        .addBox(-3.0F, -3.0F, -4.0F, 6, 6, 4),
                PartPose.offsetAndRotation(-7.0F, 13.0F, -22.0F, 0.0F, 0.8551066F, 0.0F));

        partdefinition.addOrReplaceChild("RightArmSeg1",
                CubeListBuilder.create().texOffs(55, 0).mirror()
                        .addBox(-1.0F, -3.0F, -10.0F, 4, 6, 13),
                PartPose.offsetAndRotation(-10.0F, 13.0F, -24.0F, 0.0F, 2.044828F, 0.0F));

        partdefinition.addOrReplaceChild("RightArmSeg2",
                CubeListBuilder.create().texOffs(130, 0).mirror()
                        .addBox(-1.0F, -3.0F, -17.0F, 8, 6, 17),
                PartPose.offsetAndRotation(-19.0F, 13.0F, -22.0F, 0.0F, 0.7435801F, 0.0F));

        partdefinition.addOrReplaceChild("RightArmSeg3",
                CubeListBuilder.create().texOffs(130, 50).mirror()
                        .addBox(-1.0F, -3.0F, -24.0F, 4, 6, 24),
                PartPose.offsetAndRotation(-29.0F, 13.0F, -33.0F, 0.0F, -0.37179F, 0.0F));

        partdefinition.addOrReplaceChild("RightArmSeg4",
                CubeListBuilder.create().texOffs(181, 0).mirror()
                        .addBox(-5.0F, -3.0F, -14.0F, 8, 6, 12),
                PartPose.offsetAndRotation(-29.0F, 13.0F, -33.0F, 0.0F, -1.487143F, 0.0F));

        partdefinition.addOrReplaceChild("RightPincer",
                CubeListBuilder.create().texOffs(130, 24).mirror()
                        .addBox(-1.0F, -3.0F, -19.0F, 2, 6, 19),
                PartPose.offsetAndRotation(-17.0F, 13.0F, -33.0F, 0.0F, -0.0743611F, 0.0F));

        partdefinition.addOrReplaceChild("LeftPincer",
                CubeListBuilder.create().texOffs(130, 24).mirror()
                        .addBox(-1.0F, -3.0F, -19.0F, 2, 6, 19),
                PartPose.offsetAndRotation(17.0F, 13.0F, -33.0F, 0.0F, 0.0743685F, 0.0F));

        partdefinition.addOrReplaceChild("Lefteye",
                CubeListBuilder.create().texOffs(0, 113).mirror()
                        .addBox(-0.5F, -5.0F, -7.5F, 3, 2, 3),
                PartPose.offsetAndRotation(0.0F, 13.0F, -8.0F, 0.0F, 0.0F, 0.2974289F));

        partdefinition.addOrReplaceChild("Righteye",
                CubeListBuilder.create().texOffs(0, 113).mirror()
                        .addBox(-2.5F, -5.0F, -7.5F, 3, 2, 3),
                PartPose.offsetAndRotation(0.0F, 13.0F, -8.0F, 0.0F, 0.0F, -0.2974216F));

        partdefinition.addOrReplaceChild("RightMandible",
                CubeListBuilder.create().texOffs(76, 55).mirror()
                        .addBox(-2.0F, -3.0F, -4.0F, 4, 4, 4),
                PartPose.offsetAndRotation(-2.0F, 13.0F, -23.0F, 0.1115358F, 0.3346075F, 0.0F));

        partdefinition.addOrReplaceChild("LeftMandible",
                CubeListBuilder.create().texOffs(76, 55).mirror()
                        .addBox(-2.0F, -3.0F, -4.0F, 4, 4, 4),
                PartPose.offsetAndRotation(2.0F, 13.0F, -23.0F, 0.111544F, -0.3346145F, 0.0F));

        partdefinition.addOrReplaceChild("RightManPart2",
                CubeListBuilder.create().texOffs(82, 64).mirror()
                        .addBox(-0.5F, -0.5F, -6.0F, 1, 1, 6),
                PartPose.offsetAndRotation(-3.0F, 11.0F, -26.0F, 1.189716F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("LeftManPart2",
                CubeListBuilder.create().texOffs(82, 64).mirror()
                        .addBox(-0.5F, -0.5F, -6.0F, 1, 1, 6),
                PartPose.offsetAndRotation(3.0F, 11.0F, -26.0F, 1.188848F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("Leg1Seg1",
                CubeListBuilder.create().texOffs(20, 20).mirror()
                        .addBox(0.0F, -1.5F, -2.0F, 4, 3, 4),
                PartPose.offset(9.0F, 13.0F, -10.0F));

        partdefinition.addOrReplaceChild("Leg1Seg2",
                CubeListBuilder.create().texOffs(21, 0).mirror()
                        .addBox(0.0F, -1.5F, -1.5F, 13, 3, 3),
                PartPose.offsetAndRotation(12.0F, 13.0F, -10.0F, 0.0F, 0.0F, -0.9294576F));

        partdefinition.addOrReplaceChild("Leg1Seg3",
                CubeListBuilder.create().texOffs(15, 8).mirror()
                        .addBox(0.0F, -1.5F, -1.5F, 13, 3, 3),
                PartPose.offsetAndRotation(18.0F, 3.0F, -10.0F, 0.0F, 0.0F, 0.6320361F));

        partdefinition.addOrReplaceChild("Leg1Seg4",
                CubeListBuilder.create().texOffs(0, 14).mirror()
                        .addBox(0.0F, -1.5F, -1.5F, 3, 10, 3),
                PartPose.offset(26.0F, 12.0F, -10.0F));

        partdefinition.addOrReplaceChild("Leg1Seg5",
                CubeListBuilder.create().texOffs(0, 0).mirror()
                        .addBox(0.0F, -1.5F, -1.5F, 7, 3, 3),
                PartPose.offsetAndRotation(27.0F, 19.0F, -10.0F, 0.0F, 0.0F, 0.669215F));

        partdefinition.addOrReplaceChild("Leg2Seg1",
                CubeListBuilder.create().texOffs(20, 20).mirror()
                        .addBox(0.0F, -1.5F, -2.0F, 4, 3, 4),
                PartPose.offset(8.5F, 13.0F, -4.0F));

        partdefinition.addOrReplaceChild("Leg2Seg2",
                CubeListBuilder.create().texOffs(21, 0).mirror()
                        .addBox(0.0F, -1.5F, -1.5F, 13, 3, 3),
                PartPose.offsetAndRotation(12.0F, 13.0F, -4.0F, 0.0F, 0.0F, -0.9294576F));

        partdefinition.addOrReplaceChild("Leg2Seg3",
                CubeListBuilder.create().texOffs(15, 8).mirror()
                        .addBox(0.0F, -1.5F, -1.5F, 13, 3, 3),
                PartPose.offsetAndRotation(18.0F, 3.0F, -4.0F, 0.0F, 0.0F, 0.6320361F));

        partdefinition.addOrReplaceChild("Leg2Seg4",
                CubeListBuilder.create().texOffs(0, 14).mirror()
                        .addBox(0.0F, -1.5F, -1.5F, 3, 10, 3),
                PartPose.offset(26.0F, 12.0F, -4.0F));

        partdefinition.addOrReplaceChild("Leg2Seg5",
                CubeListBuilder.create().texOffs(0, 0).mirror()
                        .addBox(0.0F, -1.5F, -1.5F, 7, 3, 3),
                PartPose.offsetAndRotation(27.0F, 19.0F, -4.0F, 0.0F, 0.0F, 0.669215F));

        partdefinition.addOrReplaceChild("Leg3Seg1",
                CubeListBuilder.create().texOffs(20, 20).mirror()
                        .addBox(0.0F, -1.5F, -2.0F, 4, 3, 4),
                PartPose.offset(9.5F, 13.0F, 2.0F));

        partdefinition.addOrReplaceChild("Leg3Seg2",
                CubeListBuilder.create().texOffs(21, 0).mirror()
                        .addBox(0.0F, -1.5F, -1.5F, 13, 3, 3),
                PartPose.offsetAndRotation(12.0F, 13.0F, 2.0F, 0.0F, 0.0F, -0.9294576F));

        partdefinition.addOrReplaceChild("Leg3Seg3",
                CubeListBuilder.create().texOffs(15, 8).mirror()
                        .addBox(0.0F, -1.5F, -1.5F, 13, 3, 3),
                PartPose.offsetAndRotation(18.0F, 3.0F, 2.0F, 0.0F, 0.0F, 0.6320361F));

        partdefinition.addOrReplaceChild("Leg3Seg4",
                CubeListBuilder.create().texOffs(0, 14).mirror()
                        .addBox(0.0F, -1.5F, -1.5F, 3, 10, 3),
                PartPose.offset(26.0F, 12.0F, 2.0F));

        partdefinition.addOrReplaceChild("Leg3Seg5",
                CubeListBuilder.create().texOffs(0, 0).mirror()
                        .addBox(0.0F, -1.5F, -1.5F, 7, 3, 3),
                PartPose.offsetAndRotation(27.0F, 19.0F, 2.0F, 0.0F, 0.0F, 0.669215F));

        partdefinition.addOrReplaceChild("Leg4Seg1",
                CubeListBuilder.create().texOffs(20, 20).mirror()
                        .addBox(0.0F, -1.5F, -2.0F, 4, 3, 4),
                PartPose.offset(9.0F, 13.0F, 8.0F));

        partdefinition.addOrReplaceChild("Leg4Seg2",
                CubeListBuilder.create().texOffs(21, 0).mirror()
                        .addBox(0.0F, -1.5F, -1.5F, 13, 3, 3),
                PartPose.offsetAndRotation(12.0F, 13.0F, 8.0F, 0.0F, 0.0F, -0.9294576F));

        partdefinition.addOrReplaceChild("Leg4Seg3",
                CubeListBuilder.create().texOffs(15, 8).mirror()
                        .addBox(0.0F, -1.5F, -1.5F, 13, 3, 3),
                PartPose.offsetAndRotation(18.0F, 3.0F, 8.0F, 0.0F, 0.0F, 0.6320361F));

        partdefinition.addOrReplaceChild("Leg4Seg4",
                CubeListBuilder.create().texOffs(0, 14).mirror()
                        .addBox(0.0F, -1.5F, -1.5F, 3, 10, 3),
                PartPose.offset(26.0F, 12.0F, 8.0F));

        partdefinition.addOrReplaceChild("Leg4Seg5",
                CubeListBuilder.create().texOffs(0, 0).mirror()
                        .addBox(0.0F, -1.5F, -1.5F, 7, 3, 3),
                PartPose.offsetAndRotation(27.0F, 19.0F, 8.0F, 0.0F, 0.0F, 0.669215F));

        partdefinition.addOrReplaceChild("Leg5Seg1",
                CubeListBuilder.create().texOffs(20, 20).mirror()
                        .addBox(-4.0F, -1.5F, -2.0F, 4, 3, 4),
                PartPose.offset(-9.0F, 13.0F, -10.0F));

        partdefinition.addOrReplaceChild("Leg5Seg2",
                CubeListBuilder.create().texOffs(21, 0).mirror()
                        .addBox(-13.0F, -1.5F, -1.5F, 13, 3, 3),
                PartPose.offsetAndRotation(-12.0F, 14.0F, -10.0F, 0.0F, 0.0F, 0.9294653F));

        partdefinition.addOrReplaceChild("Leg5Seg3",
                CubeListBuilder.create().texOffs(15, 8).mirror()
                        .addBox(-13.0F, -1.5F, -1.5F, 13, 3, 3),
                PartPose.offsetAndRotation(-18.0F, 4.0F, -10.0F, 0.0F, 0.0F, -0.6320364F));

        partdefinition.addOrReplaceChild("Leg5Seg4",
                CubeListBuilder.create().texOffs(0, 14).mirror()
                        .addBox(-3.0F, -1.5F, -1.5F, 3, 10, 3),
                PartPose.offset(-26.0F, 12.0F, -10.0F));

        partdefinition.addOrReplaceChild("Leg5Seg5",
                CubeListBuilder.create().texOffs(0, 0).mirror()
                        .addBox(0.0F, -1.5F, -1.5F, 7, 3, 3),
                PartPose.offsetAndRotation(-27.0F, 19.0F, -10.0F, 0.0F, 0.0F, 2.240008F));

        partdefinition.addOrReplaceChild("Leg6Seg1",
                CubeListBuilder.create().texOffs(20, 20).mirror()
                        .addBox(-4.0F, -1.5F, -2.0F, 4, 3, 4),
                PartPose.offset(-8.5F, 13.0F, -4.0F));

        partdefinition.addOrReplaceChild("Leg6Seg2",
                CubeListBuilder.create().texOffs(21, 0).mirror()
                        .addBox(-13.0F, -1.5F, -1.5F, 13, 3, 3),
                PartPose.offsetAndRotation(-12.0F, 14.0F, -4.0F, 0.0F, 0.0F, 0.9294576F));

        partdefinition.addOrReplaceChild("Leg6Seg3",
                CubeListBuilder.create().texOffs(15, 8).mirror()
                        .addBox(-13.0F, -1.5F, -1.5F, 13, 3, 3),
                PartPose.offsetAndRotation(-18.0F, 4.0F, -4.0F, 0.0F, 0.0F, -0.6320361F));

        partdefinition.addOrReplaceChild("Leg6Seg4",
                CubeListBuilder.create().texOffs(0, 14).mirror()
                        .addBox(-3.0F, -1.5F, -1.5F, 3, 10, 3),
                PartPose.offset(-26.0F, 12.0F, -4.0F));

        partdefinition.addOrReplaceChild("Leg6Seg5",
                CubeListBuilder.create().texOffs(0, 0).mirror()
                        .addBox(0.0F, -1.5F, -1.5F, 7, 3, 3),
                PartPose.offsetAndRotation(-27.0F, 19.0F, -4.0F, 0.0F, 0.0F, 2.240008F));

        partdefinition.addOrReplaceChild("Leg7Seg1",
                CubeListBuilder.create().texOffs(20, 20).mirror()
                        .addBox(-4.0F, -1.5F, -2.0F, 4, 3, 4),
                PartPose.offset(-9.5F, 13.0F, 2.0F));

        partdefinition.addOrReplaceChild("Leg7Seg2",
                CubeListBuilder.create().texOffs(21, 0).mirror()
                        .addBox(-13.0F, -1.5F, -1.5F, 13, 3, 3),
                PartPose.offsetAndRotation(-12.0F, 14.0F, 2.0F, 0.0F, 0.0F, 0.9294576F));

        partdefinition.addOrReplaceChild("Leg7Seg3",
                CubeListBuilder.create().texOffs(15, 8).mirror()
                        .addBox(-13.0F, -1.5F, -1.5F, 13, 3, 3),
                PartPose.offsetAndRotation(-18.0F, 4.0F, 2.0F, 0.0F, 0.0F, -0.6320361F));

        partdefinition.addOrReplaceChild("Leg7Seg4",
                CubeListBuilder.create().texOffs(0, 14).mirror()
                        .addBox(-3.0F, -1.5F, -1.5F, 3, 10, 3),
                PartPose.offset(-26.0F, 12.0F, 2.0F));

        partdefinition.addOrReplaceChild("Leg7Seg5",
                CubeListBuilder.create().texOffs(0, 0).mirror()
                        .addBox(0.0F, -1.5F, -1.5F, 7, 3, 3),
                PartPose.offsetAndRotation(-27.0F, 19.0F, 2.0F, 0.0F, 0.0F, 2.240008F));

        partdefinition.addOrReplaceChild("Leg8Seg1",
                CubeListBuilder.create().texOffs(20, 20).mirror()
                        .addBox(-4.0F, -1.5F, -2.0F, 4, 3, 4),
                PartPose.offset(-9.0F, 13.0F, 8.0F));

        partdefinition.addOrReplaceChild("Leg8Seg2",
                CubeListBuilder.create().texOffs(21, 0).mirror()
                        .addBox(-12.0F, -1.5F, -1.5F, 13, 3, 3),
                PartPose.offsetAndRotation(-12.0F, 14.0F, 8.0F, 0.0F, 0.0F, 0.9294576F));

        partdefinition.addOrReplaceChild("Leg8Seg3",
                CubeListBuilder.create().texOffs(15, 8).mirror()
                        .addBox(-13.0F, -1.5F, -1.5F, 13, 3, 3),
                PartPose.offsetAndRotation(-18.0F, 4.0F, 8.0F, 0.0F, 0.0F, -0.6320361F));

        partdefinition.addOrReplaceChild("Leg8Seg4",
                CubeListBuilder.create().texOffs(0, 14).mirror()
                        .addBox(-3.0F, -1.5F, -1.5F, 3, 10, 3),
                PartPose.offset(-26.0F, 12.0F, 8.0F));

        partdefinition.addOrReplaceChild("Leg8Seg5",
                CubeListBuilder.create().texOffs(0, 0).mirror()
                        .addBox(0.0F, -1.5F, -1.5F, 7, 3, 3),
                PartPose.offsetAndRotation(-27.0F, 19.0F, 8.0F, 0.0F, 0.0F, 2.240008F));

        return LayerDefinition.create(meshdefinition, 256, 128);
    }

    @Override
    public void setupAnim(EntityEmperorScorpion entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float newangle = 0.0f;
        float upangle = 0.0f;
        float nextangle = 0.0f;
        float pi4 = 1.570795f;
        newangle = Mth.cos((float)(ageInTicks * 2.0f * limbSwingAmount)) * (float)Math.PI * 0.12f * limbSwingAmount;
        nextangle = Mth.cos((float)((ageInTicks + 0.1f) * 2.0f * limbSwingAmount)) * (float)Math.PI * 0.12f * limbSwingAmount;
        upangle = 0.0f;
        if (nextangle > newangle) {
        upangle = 0.47f * limbSwingAmount - Math.abs(newangle);
        }
        this.doLeftLeg(this.Leg1Seg1, this.Leg1Seg2, this.Leg1Seg3, this.Leg1Seg4, this.Leg1Seg5, newangle, upangle);
        this.doRightLeg(this.Leg5Seg1, this.Leg5Seg2, this.Leg5Seg3, this.Leg5Seg4, this.Leg5Seg5, -newangle, upangle);
        newangle = Mth.cos((float)(ageInTicks * 2.0f * limbSwingAmount - 1.0f * pi4)) * (float)Math.PI * 0.12f * limbSwingAmount;
        nextangle = Mth.cos((float)((ageInTicks + 0.1f) * 2.0f * limbSwingAmount - 1.0f * pi4)) * (float)Math.PI * 0.12f * limbSwingAmount;
        upangle = 0.0f;
        if (nextangle > newangle) {
        upangle = 0.47f * limbSwingAmount - Math.abs(newangle);
        }
        this.doLeftLeg(this.Leg2Seg1, this.Leg2Seg2, this.Leg2Seg3, this.Leg2Seg4, this.Leg2Seg5, newangle, upangle);
        this.doRightLeg(this.Leg6Seg1, this.Leg6Seg2, this.Leg6Seg3, this.Leg6Seg4, this.Leg6Seg5, -newangle, upangle);
        newangle = Mth.cos((float)(ageInTicks * 2.0f * limbSwingAmount - 2.0f * pi4)) * (float)Math.PI * 0.12f * limbSwingAmount;
        nextangle = Mth.cos((float)((ageInTicks + 0.1f) * 2.0f * limbSwingAmount - 2.0f * pi4)) * (float)Math.PI * 0.12f * limbSwingAmount;
        upangle = 0.0f;
        if (nextangle > newangle) {
        upangle = 0.47f * limbSwingAmount - Math.abs(newangle);
        }
        this.doLeftLeg(this.Leg3Seg1, this.Leg3Seg2, this.Leg3Seg3, this.Leg3Seg4, this.Leg3Seg5, newangle, upangle);
        this.doRightLeg(this.Leg7Seg1, this.Leg7Seg2, this.Leg7Seg3, this.Leg7Seg4, this.Leg7Seg5, -newangle, upangle);
        newangle = Mth.cos((float)(ageInTicks * 2.0f * limbSwingAmount - 3.0f * pi4)) * (float)Math.PI * 0.12f * limbSwingAmount;
        nextangle = Mth.cos((float)((ageInTicks + 0.1f) * 2.0f * limbSwingAmount - 3.0f * pi4)) * (float)Math.PI * 0.12f * limbSwingAmount;
        upangle = 0.0f;
        if (nextangle > newangle) {
        upangle = 0.47f * limbSwingAmount - Math.abs(newangle);
        }
        this.doLeftLeg(this.Leg4Seg1, this.Leg4Seg2, this.Leg4Seg3, this.Leg4Seg4, this.Leg4Seg5, newangle, upangle);
        this.doRightLeg(this.Leg8Seg1, this.Leg8Seg2, this.Leg8Seg3, this.Leg8Seg4, this.Leg8Seg5, -newangle, upangle);
        newangle = entity.getAttacking() == 0 ? Mth.cos((float)(ageInTicks * 0.5f * limbSwingAmount)) * (float)Math.PI * 0.05f : Mth.cos((float)(ageInTicks * 2.5f * limbSwingAmount)) * (float)Math.PI * 0.15f;
        this.LeftManPart2.zRot = newangle;
        this.RightManPart2.zRot = -newangle;
        newangle = Mth.cos((float)(ageInTicks * 3.0f * limbSwingAmount)) * (float)Math.PI * 0.15f;
        nextangle = Mth.cos((float)((ageInTicks + 0.1f) * 3.0f * limbSwingAmount)) * (float)Math.PI * 0.15f;
        if (nextangle > 0.0f && newangle < 0.0f) {
        ri1 = 0;
        if (entity.getAttacking() == 0) {
        ri1 = entity.getRandom().nextInt(20);
        ri2 = entity.getRandom().nextInt(25);
        } else {
        ri1 = entity.getRandom().nextInt(4);
        ri2 = entity.getRandom().nextInt(3);
        }
        }
        if (ri1 == 1 || ri1 == 3) {
        this.doLeftClaw(newangle);
        } else {
        this.doLeftClaw(0.0f);
        }
        if (ri1 == 2 || ri1 == 3) {
        this.doRightClaw(newangle);
        } else {
        this.doRightClaw(0.0f);
        }
        if (ri2 == 1) {
        this.doTail(newangle);
        } else {
        this.doTail(0.0f);
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.Head.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Seg1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Seg2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Seg3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Seg4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Seg5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Seg6.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Seg7.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Seg8.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Tailseg1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Tailseg2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Tailseg3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Tailseg4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Tailseg5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Tailseg6.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Tailseg7.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Tailseg8.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Stinger1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Stinger2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Stinger3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.LeftShoulder.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.LeftArmSeg1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.LeftArmSeg2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.LeftArmSeg3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.LeftArmSeg4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.RightShoulder.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.RightArmSeg1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.RightArmSeg2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.RightArmSeg3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.RightArmSeg4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.RightPincer.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.LeftPincer.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Lefteye.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Righteye.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.RightMandible.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.LeftMandible.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.RightManPart2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.LeftManPart2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Leg1Seg1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Leg1Seg2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Leg1Seg3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Leg1Seg4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Leg1Seg5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Leg2Seg1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Leg2Seg2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Leg2Seg3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Leg2Seg4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Leg2Seg5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Leg3Seg1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Leg3Seg2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Leg3Seg3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Leg3Seg4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Leg3Seg5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Leg4Seg1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Leg4Seg2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Leg4Seg3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Leg4Seg4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Leg4Seg5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Leg5Seg1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Leg5Seg2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Leg5Seg3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Leg5Seg4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Leg5Seg5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Leg6Seg1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Leg6Seg2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Leg6Seg3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Leg6Seg4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Leg6Seg5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Leg7Seg1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Leg7Seg2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Leg7Seg3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Leg7Seg4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Leg7Seg5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Leg8Seg1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Leg8Seg2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Leg8Seg3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Leg8Seg4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Leg8Seg5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }

    private void doLeftLeg(ModelPart seg1, ModelPart seg2, ModelPart seg3, ModelPart seg4, ModelPart seg5, float angle, float upangle) {
        seg2.yRot = angle;
        seg3.yRot = angle;
        seg4.yRot = angle;
        seg5.yRot = angle;
        seg3.z = (float)((double)seg2.z - Math.sin(angle) * 6.0);
        seg3.x = (float)((double)seg2.x - Math.abs(Math.sin(angle) * 6.0) + 6.0);
        seg4.z = (float)((double)seg3.z - Math.sin(angle) * 9.0);
        seg4.x = (float)((double)seg3.x - Math.abs(Math.sin(angle) * 9.0) + 9.0);
        seg5.z = (float)((double)seg4.z - Math.sin(angle) * 1.0);
        seg5.x = (float)((double)seg4.x - Math.abs(Math.sin(angle) * 1.0) + 1.0);
        seg2.zRot = -upangle - 0.929f;
        seg3.zRot = -upangle + 0.632f;
        seg3.y = seg2.y + (float)(11.5 * Math.sin(seg2.zRot));
        seg4.y = seg3.y + (float)(11.5 * Math.sin(seg3.zRot));
        seg5.y = seg4.y + 6.5f;
    }

    private void doRightLeg(ModelPart seg1, ModelPart seg2, ModelPart seg3, ModelPart seg4, ModelPart seg5, float angle, float upangle) {
        seg2.yRot = angle;
        seg3.yRot = angle;
        seg4.yRot = angle;
        seg5.yRot = -angle;
        seg3.z = (float)((double)seg2.z + Math.sin(angle) * 6.0);
        seg3.x = (float)((double)seg2.x + Math.abs(Math.sin(angle) * 6.0) - 6.0);
        seg4.z = (float)((double)seg3.z + Math.sin(angle) * 9.0);
        seg4.x = (float)((double)seg3.x + Math.abs(Math.sin(angle) * 9.0) - 9.0);
        seg5.z = (float)((double)seg4.z + Math.sin(angle) * 1.0);
        seg5.x = (float)((double)seg4.x + Math.abs(Math.sin(angle) * 1.0) - 1.0);
        seg2.zRot = upangle + 0.929f;
        seg3.zRot = upangle - 0.632f;
        seg3.y = seg2.y - (float)(11.5 * Math.sin(seg2.zRot));
        seg4.y = seg3.y - (float)(11.5 * Math.sin(seg3.zRot));
        seg5.y = seg4.y + 6.5f;
    }

    private void doLeftClaw(float angle) {
        this.LeftArmSeg1.yRot = -1.57f + angle;
        this.LeftArmSeg2.z = (float)(-22.0 - Math.cos(this.LeftArmSeg1.yRot) * 12.0);
        this.LeftArmSeg3.z = this.LeftArmSeg2.z - 11.0f;
        this.LeftArmSeg4.z = this.LeftArmSeg2.z - 11.0f;
        this.LeftPincer.z = this.LeftArmSeg2.z - 11.0f;
        this.LeftArmSeg3.yRot = 0.074f + angle;
        this.LeftPincer.yRot = 0.371f - angle;
    }

    private void doRightClaw(float angle) {
        this.RightArmSeg1.yRot = 1.57f - angle;
        this.RightArmSeg2.z = (float)(-22.0 - Math.cos(this.RightArmSeg1.yRot) * 12.0);
        this.RightArmSeg3.z = this.RightArmSeg2.z - 11.0f;
        this.RightArmSeg4.z = this.RightArmSeg2.z - 11.0f;
        this.RightPincer.z = this.RightArmSeg2.z - 11.0f;
        this.RightArmSeg3.yRot = -0.074f - angle;
        this.RightPincer.yRot = -0.371f + angle;
    }

    private void doTail(float angle) {
        this.Tailseg1.xRot = 0.594f + angle;
        this.Tailseg2.xRot = this.Tailseg1.xRot + 0.48399997f + angle;
        this.Tailseg2.y = (float)((double)this.Tailseg1.y - Math.sin(this.Tailseg1.xRot) * 9.0);
        this.Tailseg2.z = (float)((double)this.Tailseg1.z + Math.cos(this.Tailseg1.xRot) * 9.0);
        this.Tailseg3.xRot = this.Tailseg2.xRot + 0.6320001f + angle;
        this.Tailseg3.y = (float)((double)this.Tailseg2.y - Math.sin(this.Tailseg2.xRot) * 10.0);
        this.Tailseg3.z = (float)((double)this.Tailseg2.z + Math.cos(this.Tailseg2.xRot) * 10.0);
        this.Tailseg4.xRot = this.Tailseg3.xRot + 0.5569999f - angle;
        this.Tailseg4.y = (float)((double)this.Tailseg3.y - Math.sin(this.Tailseg3.xRot) * 10.0);
        this.Tailseg4.z = (float)((double)this.Tailseg3.z + Math.cos(this.Tailseg3.xRot) * 10.0);
        this.Tailseg5.xRot = this.Tailseg4.xRot + 0.63199997f - angle;
        this.Tailseg5.y = (float)((double)this.Tailseg4.y - Math.sin(this.Tailseg4.xRot) * 10.0);
        this.Tailseg5.z = (float)((double)this.Tailseg4.z + Math.cos(this.Tailseg4.xRot) * 10.0);
        this.Tailseg6.xRot = this.Tailseg5.xRot + -5.501f - angle * 3.0f / 2.0f - 0.4f;
        this.Tailseg6.y = (float)((double)this.Tailseg5.y - Math.sin(this.Tailseg5.xRot) * 10.0);
        this.Tailseg6.z = (float)((double)this.Tailseg5.z + Math.cos(this.Tailseg5.xRot) * 10.0);
        this.Tailseg7.xRot = this.Tailseg6.xRot + -2.822f - angle * 2.5f - 2.2f;
        this.Tailseg7.y = (float)((double)this.Tailseg6.y - Math.sin(this.Tailseg6.xRot) * 10.0);
        this.Tailseg7.z = (float)((double)this.Tailseg6.z + Math.cos(this.Tailseg6.xRot) * 10.0);
        this.Tailseg8.xRot = this.Tailseg7.xRot;
        this.Tailseg8.y = this.Tailseg7.y;
        this.Tailseg8.z = this.Tailseg7.z;
        this.Stinger1.xRot = this.Tailseg7.xRot + 0.0f + angle * 0.66f;
        this.Stinger1.y = (float)((double)this.Tailseg7.y - Math.sin(this.Tailseg7.xRot) * 10.0);
        this.Stinger1.z = (float)((double)this.Tailseg7.z + Math.cos(this.Tailseg7.xRot) * 10.0);
        this.Stinger2.xRot = this.Stinger1.xRot + -0.48f + angle;
        this.Stinger2.y = (float)((double)this.Stinger1.y - Math.sin(this.Stinger1.xRot) * 3.0);
        this.Stinger2.z = (float)((double)this.Stinger1.z + Math.cos(this.Stinger1.xRot) * 3.0);
        this.Stinger3.xRot = this.Stinger2.xRot + -1.01f + angle * 1.7f;
        this.Stinger3.y = (float)((double)this.Stinger2.y - Math.sin(this.Stinger2.xRot) * 3.0);
        this.Stinger3.z = (float)((double)this.Stinger2.z + Math.cos(this.Stinger2.xRot) * 3.0);
    }
}
