package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.Lizard;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class LizardModel extends EntityModel<Lizard> {
    private final ModelPart BodyBack;
    private final ModelPart TopBackLeftLeg;
    private final ModelPart TailTip;
    private final ModelPart BodyFront;
    private final ModelPart TailBase1;
    private final ModelPart Tail2;
    private final ModelPart Tail3;
    private final ModelPart Tail4;
    private final ModelPart Neck;
    private final ModelPart TopFrontLeftLeg;
    private final ModelPart TopBackRightLeg;
    private final ModelPart BottomBackRightLeg;
    private final ModelPart TopFrontRightLeg;
    private final ModelPart BottomBackLeftLeg;
    private final ModelPart BottomFrontRightLeg;
    private final ModelPart BottomFrontLeftLeg;
    private final ModelPart BodyCenter;
    private final ModelPart Toe7;
    private final ModelPart Toe6;
    private final ModelPart BackLeftFoot;
    private final ModelPart Toe4;
    private final ModelPart Toe5;
    private final ModelPart BackRightFoot;
    private final ModelPart Toe8;
    private final ModelPart Toe1;
    private final ModelPart FrontLeftFoot;
    private final ModelPart Toe3;
    private final ModelPart Toe2;
    private final ModelPart FrontRightFoot;
    private final ModelPart FinRidge7;
    private final ModelPart FinRidge6;
    private final ModelPart FinRidge5;
    private final ModelPart FinRidge4;
    private final ModelPart FinRidge3;
    private final ModelPart FinRidge2;
    private final ModelPart FinRidge1;
    private final ModelPart Fin10;
    private final ModelPart Fin9;
    private final ModelPart Fin8;
    private final ModelPart Fin7;
    private final ModelPart Fin6;
    private final ModelPart Fin5;
    private final ModelPart Fin3;
    private final ModelPart Fin2;
    private final ModelPart Tooth11;
    private final ModelPart Tooth10;
    private final ModelPart Tooth8;
    private final ModelPart Tooth7;
    private final ModelPart Tooth6;
    private final ModelPart Tooth5;
    private final ModelPart Tooth4;
    private final ModelPart Tooth3;
    private final ModelPart Tooth2;
    private final ModelPart CenterRightNose;
    private final ModelPart CenterLeftNose;
    private final ModelPart Tooth1;
    private final ModelPart BottomNose;
    private final ModelPart TopNose;
    private final ModelPart JawTop;
    private final ModelPart CenterMiddleNose;
    private final ModelPart RightEye;
    private final ModelPart LeftEye;
    private final ModelPart Tooth16;
    private final ModelPart Tooth15;
    private final ModelPart Tooth14;
    private final ModelPart Tooth13;
    private final ModelPart Tooth12;
    private final ModelPart Tooth9;
    private final ModelPart BottomJaw;
    private final ModelPart Hat1;
    private final ModelPart Hat2;

    public LizardModel(ModelPart root) {
        this.BodyBack = root.getChild("BodyBack");
        this.TopBackLeftLeg = root.getChild("TopBackLeftLeg");
        this.TailTip = root.getChild("TailTip");
        this.BodyFront = root.getChild("BodyFront");
        this.TailBase1 = root.getChild("TailBase1");
        this.Tail2 = root.getChild("Tail2");
        this.Tail3 = root.getChild("Tail3");
        this.Tail4 = root.getChild("Tail4");
        this.Neck = root.getChild("Neck");
        this.TopFrontLeftLeg = root.getChild("TopFrontLeftLeg");
        this.TopBackRightLeg = root.getChild("TopBackRightLeg");
        this.BottomBackRightLeg = root.getChild("BottomBackRightLeg");
        this.TopFrontRightLeg = root.getChild("TopFrontRightLeg");
        this.BottomBackLeftLeg = root.getChild("BottomBackLeftLeg");
        this.BottomFrontRightLeg = root.getChild("BottomFrontRightLeg");
        this.BottomFrontLeftLeg = root.getChild("BottomFrontLeftLeg");
        this.BodyCenter = root.getChild("BodyCenter");
        this.Toe7 = root.getChild("Toe7");
        this.Toe6 = root.getChild("Toe6");
        this.BackLeftFoot = root.getChild("BackLeftFoot");
        this.Toe4 = root.getChild("Toe4");
        this.Toe5 = root.getChild("Toe5");
        this.BackRightFoot = root.getChild("BackRightFoot");
        this.Toe8 = root.getChild("Toe8");
        this.Toe1 = root.getChild("Toe1");
        this.FrontLeftFoot = root.getChild("FrontLeftFoot");
        this.Toe3 = root.getChild("Toe3");
        this.Toe2 = root.getChild("Toe2");
        this.FrontRightFoot = root.getChild("FrontRightFoot");
        this.FinRidge7 = root.getChild("FinRidge7");
        this.FinRidge6 = root.getChild("FinRidge6");
        this.FinRidge5 = root.getChild("FinRidge5");
        this.FinRidge4 = root.getChild("FinRidge4");
        this.FinRidge3 = root.getChild("FinRidge3");
        this.FinRidge2 = root.getChild("FinRidge2");
        this.FinRidge1 = root.getChild("FinRidge1");
        this.Fin10 = root.getChild("Fin10");
        this.Fin9 = root.getChild("Fin9");
        this.Fin8 = root.getChild("Fin8");
        this.Fin7 = root.getChild("Fin7");
        this.Fin6 = root.getChild("Fin6");
        this.Fin5 = root.getChild("Fin5");
        this.Fin3 = root.getChild("Fin3");
        this.Fin2 = root.getChild("Fin2");
        this.Tooth11 = root.getChild("Tooth11");
        this.Tooth10 = root.getChild("Tooth10");
        this.Tooth8 = root.getChild("Tooth8");
        this.Tooth7 = root.getChild("Tooth7");
        this.Tooth6 = root.getChild("Tooth6");
        this.Tooth5 = root.getChild("Tooth5");
        this.Tooth4 = root.getChild("Tooth4");
        this.Tooth3 = root.getChild("Tooth3");
        this.Tooth2 = root.getChild("Tooth2");
        this.CenterRightNose = root.getChild("CenterRightNose");
        this.CenterLeftNose = root.getChild("CenterLeftNose");
        this.Tooth1 = root.getChild("Tooth1");
        this.BottomNose = root.getChild("BottomNose");
        this.TopNose = root.getChild("TopNose");
        this.JawTop = root.getChild("JawTop");
        this.CenterMiddleNose = root.getChild("CenterMiddleNose");
        this.RightEye = root.getChild("RightEye");
        this.LeftEye = root.getChild("LeftEye");
        this.Tooth16 = root.getChild("Tooth16");
        this.Tooth15 = root.getChild("Tooth15");
        this.Tooth14 = root.getChild("Tooth14");
        this.Tooth13 = root.getChild("Tooth13");
        this.Tooth12 = root.getChild("Tooth12");
        this.Tooth9 = root.getChild("Tooth9");
        this.BottomJaw = root.getChild("BottomJaw");
        this.Hat1 = root.getChild("Hat1");
        this.Hat2 = root.getChild("Hat2");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("BodyBack",
                CubeListBuilder.create().texOffs(92, 48).mirror()
                        .addBox(-4.0F, -4.0F, 0.0F, 8, 8, 8),
                PartPose.offset(0.0F, 14.0F, 0.0F));

        partdefinition.addOrReplaceChild("TopBackLeftLeg",
                CubeListBuilder.create().texOffs(54, 32).mirror()
                        .addBox(0.0F, -2.0F, -2.0F, 8, 3, 3),
                PartPose.offsetAndRotation(3.0F, 13.0F, 2.0F, 0.0F, 0.0F, 0.2617994F));

        partdefinition.addOrReplaceChild("TailTip",
                CubeListBuilder.create().texOffs(100, 118).mirror()
                        .addBox(-1.0F, -1.0F, 0.0F, 2, 2, 8),
                PartPose.offset(0.0F, 23.0F, 41.0F));

        partdefinition.addOrReplaceChild("BodyFront",
                CubeListBuilder.create().texOffs(92, 16).mirror()
                        .addBox(-4.0F, -4.0F, -8.0F, 8, 8, 8),
                PartPose.offset(0.0F, 14.0F, -8.0F));

        partdefinition.addOrReplaceChild("TailBase1",
                CubeListBuilder.create().texOffs(88, 64).mirror()
                        .addBox(-3.0F, -3.0F, 0.0F, 6, 6, 14),
                PartPose.offsetAndRotation(0.0F, 14.0F, 7.0F, -0.2617994F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("Tail2",
                CubeListBuilder.create().texOffs(95, 84).mirror()
                        .addBox(-2.0F, -2.0F, 0.0F, 4, 4, 10),
                PartPose.offsetAndRotation(0.0F, 17.0F, 19.0F, -0.5235988F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("Tail3",
                CubeListBuilder.create().texOffs(100, 98).mirror()
                        .addBox(-1.0F, -1.0F, 0.0F, 2, 2, 8),
                PartPose.offsetAndRotation(0.0F, 21.0F, 26.0F, -0.2617994F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("Tail4",
                CubeListBuilder.create().texOffs(100, 108).mirror()
                        .addBox(-1.0F, -1.0F, 0.0F, 2, 2, 8),
                PartPose.offset(0.0F, 23.0F, 33.0F));

        partdefinition.addOrReplaceChild("Neck",
                CubeListBuilder.create().texOffs(100, 9).mirror()
                        .addBox(-3.0F, -2.0F, -2.0F, 6, 5, 2),
                PartPose.offset(0.0F, 12.0F, -16.0F));

        partdefinition.addOrReplaceChild("TopFrontLeftLeg",
                CubeListBuilder.create().texOffs(26, 12).mirror()
                        .addBox(0.0F, -2.0F, -2.0F, 8, 3, 3),
                PartPose.offsetAndRotation(3.0F, 13.0F, -12.0F, 0.0F, 0.0F, 0.2617994F));

        partdefinition.addOrReplaceChild("TopBackRightLeg",
                CubeListBuilder.create().texOffs(26, 32).mirror()
                        .addBox(-8.0F, -2.0F, -2.0F, 8, 3, 3),
                PartPose.offsetAndRotation(-3.0F, 13.0F, 2.0F, 0.0F, 0.0F, -0.2617994F));

        partdefinition.addOrReplaceChild("BottomBackRightLeg",
                CubeListBuilder.create().texOffs(25, 26).mirror()
                        .addBox(-12.0F, -8.0F, -2.0F, 9, 3, 3),
                PartPose.offsetAndRotation(-3.0F, 13.0F, 2.0F, 0.0F, 0.0F, -1.308997F));

        partdefinition.addOrReplaceChild("TopFrontRightLeg",
                CubeListBuilder.create().texOffs(54, 12).mirror()
                        .addBox(-8.0F, -2.0F, -2.0F, 8, 3, 3),
                PartPose.offsetAndRotation(-3.0F, 13.0F, -12.0F, 0.0F, 0.0F, -0.2617994F));

        partdefinition.addOrReplaceChild("BottomBackLeftLeg",
                CubeListBuilder.create().texOffs(53, 26).mirror()
                        .addBox(3.0F, -8.0F, -2.0F, 9, 3, 3),
                PartPose.offsetAndRotation(3.0F, 13.0F, 2.0F, 0.0F, 0.0F, 1.308997F));

        partdefinition.addOrReplaceChild("BottomFrontRightLeg",
                CubeListBuilder.create().texOffs(53, 18).mirror()
                        .addBox(-12.0F, -8.0F, -2.0F, 9, 3, 3),
                PartPose.offsetAndRotation(-3.0F, 13.0F, -12.0F, 0.0F, 0.0F, -1.308997F));

        partdefinition.addOrReplaceChild("BottomFrontLeftLeg",
                CubeListBuilder.create().texOffs(25, 18).mirror()
                        .addBox(3.0F, -8.0F, -2.0F, 9, 3, 3),
                PartPose.offsetAndRotation(3.0F, 13.0F, -12.0F, 0.0F, 0.0F, 1.308997F));

        partdefinition.addOrReplaceChild("BodyCenter",
                CubeListBuilder.create().texOffs(92, 32).mirror()
                        .addBox(-4.0F, -4.0F, -4.0F, 8, 8, 8),
                PartPose.offset(0.0F, 14.0F, -4.0F));

        partdefinition.addOrReplaceChild("Toe7",
                CubeListBuilder.create().texOffs(104, 0).mirror()
                        .addBox(10.0F, 10.0F, -5.0F, 1, 1, 1),
                PartPose.offset(3.0F, 13.0F, 2.0F));

        partdefinition.addOrReplaceChild("Toe6",
                CubeListBuilder.create().texOffs(108, 0).mirror()
                        .addBox(8.0F, 10.0F, -5.0F, 1, 1, 1),
                PartPose.offset(3.0F, 13.0F, 2.0F));

        partdefinition.addOrReplaceChild("BackLeftFoot",
                CubeListBuilder.create().texOffs(20, 0).mirror()
                        .addBox(7.0F, 9.0F, -4.0F, 4, 2, 6),
                PartPose.offset(3.0F, 13.0F, 2.0F));

        partdefinition.addOrReplaceChild("Toe4",
                CubeListBuilder.create().texOffs(80, 0).mirror()
                        .addBox(-11.0F, 10.0F, -5.0F, 1, 1, 1),
                PartPose.offset(-3.0F, 13.0F, 2.0F));

        partdefinition.addOrReplaceChild("Toe5",
                CubeListBuilder.create().texOffs(84, 0).mirror()
                        .addBox(-9.0F, 10.0F, -5.0F, 1, 1, 1),
                PartPose.offset(-3.0F, 13.0F, 2.0F));

        partdefinition.addOrReplaceChild("BackRightFoot",
                CubeListBuilder.create().texOffs(60, 0).mirror()
                        .addBox(-11.0F, 9.0F, -4.0F, 4, 2, 6),
                PartPose.offset(-3.0F, 13.0F, 2.0F));

        partdefinition.addOrReplaceChild("Toe8",
                CubeListBuilder.create().texOffs(100, 0).mirror()
                        .addBox(10.0F, 10.0F, -5.0F, 1, 1, 1),
                PartPose.offset(3.0F, 13.0F, -12.0F));

        partdefinition.addOrReplaceChild("Toe1",
                CubeListBuilder.create().texOffs(96, 0).mirror()
                        .addBox(8.0F, 10.0F, -5.0F, 1, 1, 1),
                PartPose.offset(3.0F, 13.0F, -12.0F));

        partdefinition.addOrReplaceChild("FrontLeftFoot",
                CubeListBuilder.create().texOffs(40, 0).mirror()
                        .addBox(7.0F, 9.0F, -4.0F, 4, 2, 6),
                PartPose.offset(3.0F, 13.0F, -12.0F));

        partdefinition.addOrReplaceChild("Toe3",
                CubeListBuilder.create().texOffs(88, 0).mirror()
                        .addBox(-11.0F, 10.0F, -5.0F, 1, 1, 1),
                PartPose.offset(-3.0F, 13.0F, -12.0F));

        partdefinition.addOrReplaceChild("Toe2",
                CubeListBuilder.create().texOffs(92, 0).mirror()
                        .addBox(-9.0F, 10.0F, -5.0F, 1, 1, 1),
                PartPose.offset(-3.0F, 13.0F, -12.0F));

        partdefinition.addOrReplaceChild("FrontRightFoot",
                CubeListBuilder.create().texOffs(0, 0).mirror()
                        .addBox(-11.0F, 9.0F, -4.0F, 4, 2, 6),
                PartPose.offset(-3.0F, 13.0F, -12.0F));

        partdefinition.addOrReplaceChild("FinRidge7",
                CubeListBuilder.create().texOffs(0, 99).mirror()
                        .addBox(0.0F, -13.0F, 0.0F, 2, 13, 1),
                PartPose.offsetAndRotation(-1.0F, 10.0F, -4.5F, -0.9666439F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("FinRidge6",
                CubeListBuilder.create().texOffs(6, 98).mirror()
                        .addBox(0.0F, -13.0F, 0.0F, 2, 13, 1),
                PartPose.offsetAndRotation(-1.0F, 10.0F, -4.0F, -0.5205006F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("FinRidge5",
                CubeListBuilder.create().texOffs(12, 99).mirror()
                        .addBox(0.0F, -13.0F, 0.0F, 2, 13, 1),
                PartPose.offset(-1.0F, 10.0F, -4.0F));

        partdefinition.addOrReplaceChild("FinRidge4",
                CubeListBuilder.create().texOffs(6, 114).mirror()
                        .addBox(0.0F, -13.0F, 0.0F, 2, 13, 1),
                PartPose.offsetAndRotation(-1.0F, 10.0F, -3.5F, 0.9666439F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("FinRidge3",
                CubeListBuilder.create().texOffs(12, 115).mirror()
                        .addBox(0.0F, -13.0F, 0.0F, 2, 13, 1),
                PartPose.offsetAndRotation(-1.0F, 10.0F, -4.0F, 0.5205006F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("FinRidge2",
                CubeListBuilder.create().texOffs(0, 84).mirror()
                        .addBox(0.0F, -13.0F, 0.0F, 2, 13, 1),
                PartPose.offsetAndRotation(-1.0F, 10.0F, -4.5F, -1.375609F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("FinRidge1",
                CubeListBuilder.create().texOffs(0, 114).mirror()
                        .addBox(0.0F, -13.0F, 0.0F, 2, 13, 1),
                PartPose.offsetAndRotation(-1.0F, 10.0F, -3.5F, 1.412787F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("Fin10",
                CubeListBuilder.create().texOffs(0, 58).mirror()
                        .addBox(0.0F, -13.0F, -2.0F, 0, 11, 6),
                PartPose.offsetAndRotation(0.0F, 10.5F, -5.0F, 0.2094395F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("Fin9",
                CubeListBuilder.create().texOffs(7, 84).mirror()
                        .addBox(0.0F, -11.0F, 0.0F, 0, 11, 3),
                PartPose.offsetAndRotation(0.0F, 10.0F, -5.0F, 1.570796F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("Fin8",
                CubeListBuilder.create().texOffs(12, 34).mirror()
                        .addBox(0.0F, -7.0F, -4.0F, 0, 7, 4),
                PartPose.offsetAndRotation(0.0F, 10.0F, 1.0F, -1.570796F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("Fin7",
                CubeListBuilder.create().texOffs(12, 46).mirror()
                        .addBox(0.0F, -8.0F, -4.0F, 0, 8, 4),
                PartPose.offsetAndRotation(0.0F, 10.0F, 1.0F, -1.033256F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("Fin6",
                CubeListBuilder.create().texOffs(0, 31).mirror()
                        .addBox(0.0F, -10.0F, -4.0F, 0, 10, 4),
                PartPose.offsetAndRotation(0.0F, 10.0F, -1.0F, -0.7267386F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("Fin5",
                CubeListBuilder.create().texOffs(30, 59).mirror()
                        .addBox(0.0F, -12.0F, -5.0F, 0, 11, 6),
                PartPose.offsetAndRotation(0.0F, 10.0F, -2.0F, -0.3003206F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("Fin3",
                CubeListBuilder.create().texOffs(14, 60).mirror()
                        .addBox(0.0F, -12.0F, -3.0F, 0, 12, 6),
                PartPose.offsetAndRotation(0.0F, 10.0F, -4.0F, 0.7073231F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("Fin2",
                CubeListBuilder.create().texOffs(14, 79).mirror()
                        .addBox(0.0F, -12.0F, -4.0F, 0, 11, 6),
                PartPose.offsetAndRotation(0.0F, 10.0F, -4.0F, 1.048747F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("Tooth11",
                CubeListBuilder.create().texOffs(24, 110).mirror()
                        .addBox(3.0F, 3.0F, -8.0F, 1, 1, 1),
                PartPose.offset(0.0F, 12.0F, -18.0F));

        partdefinition.addOrReplaceChild("Tooth10",
                CubeListBuilder.create().texOffs(24, 106).mirror()
                        .addBox(3.0F, 3.0F, -10.0F, 1, 1, 1),
                PartPose.offset(0.0F, 12.0F, -18.0F));

        partdefinition.addOrReplaceChild("Tooth8",
                CubeListBuilder.create().texOffs(28, 95).mirror()
                        .addBox(3.0F, 3.0F, -14.0F, 1, 1, 1),
                PartPose.offset(0.0F, 12.0F, -18.0F));

        partdefinition.addOrReplaceChild("Tooth7",
                CubeListBuilder.create().texOffs(70, 106).mirror()
                        .addBox(-4.0F, 3.0F, -10.0F, 1, 1, 1),
                PartPose.offset(0.0F, 12.0F, -18.0F));

        partdefinition.addOrReplaceChild("Tooth6",
                CubeListBuilder.create().texOffs(70, 102).mirror()
                        .addBox(-4.0F, 3.0F, -12.0F, 1, 1, 1),
                PartPose.offset(0.0F, 12.0F, -18.0F));

        partdefinition.addOrReplaceChild("Tooth5",
                CubeListBuilder.create().texOffs(66, 95).mirror()
                        .addBox(-4.0F, 3.0F, -14.0F, 1, 1, 1),
                PartPose.offset(0.0F, 12.0F, -18.0F));

        partdefinition.addOrReplaceChild("Tooth4",
                CubeListBuilder.create().texOffs(60, 95).mirror()
                        .addBox(1.0F, 3.0F, -14.0F, 1, 1, 1),
                PartPose.offset(0.0F, 12.0F, -18.0F));

        partdefinition.addOrReplaceChild("Tooth3",
                CubeListBuilder.create().texOffs(34, 95).mirror()
                        .addBox(-2.0F, 3.0F, -14.0F, 1, 1, 1),
                PartPose.offset(0.0F, 12.0F, -18.0F));

        partdefinition.addOrReplaceChild("Tooth2",
                CubeListBuilder.create().texOffs(70, 110).mirror()
                        .addBox(-4.0F, 3.0F, -8.0F, 1, 1, 1),
                PartPose.offset(0.0F, 12.0F, -18.0F));

        partdefinition.addOrReplaceChild("CenterRightNose",
                CubeListBuilder.create().texOffs(40, 88).mirror()
                        .addBox(-4.0F, 0.0F, -14.0F, 1, 1, 1),
                PartPose.offset(0.0F, 12.0F, -18.0F));

        partdefinition.addOrReplaceChild("CenterLeftNose",
                CubeListBuilder.create().texOffs(54, 88).mirror()
                        .addBox(3.0F, 0.0F, -14.0F, 1, 1, 1),
                PartPose.offset(0.0F, 12.0F, -18.0F));

        partdefinition.addOrReplaceChild("Tooth1",
                CubeListBuilder.create().texOffs(24, 102).mirror()
                        .addBox(3.0F, 3.0F, -12.0F, 1, 1, 1),
                PartPose.offset(0.0F, 12.0F, -18.0F));

        partdefinition.addOrReplaceChild("BottomNose",
                CubeListBuilder.create().texOffs(40, 90).mirror()
                        .addBox(-4.0F, 1.0F, -14.0F, 8, 2, 1),
                PartPose.offset(0.0F, 12.0F, -18.0F));

        partdefinition.addOrReplaceChild("TopNose",
                CubeListBuilder.create().texOffs(40, 84).mirror()
                        .addBox(-4.0F, -3.0F, -14.0F, 8, 3, 1),
                PartPose.offset(0.0F, 12.0F, -18.0F));

        partdefinition.addOrReplaceChild("JawTop",
                CubeListBuilder.create().texOffs(28, 97).mirror()
                        .addBox(-4.0F, -3.0F, -13.0F, 8, 6, 13),
                PartPose.offset(0.0F, 12.0F, -18.0F));

        partdefinition.addOrReplaceChild("CenterMiddleNose",
                CubeListBuilder.create().texOffs(46, 88).mirror()
                        .addBox(-1.0F, 0.0F, -14.0F, 2, 1, 1),
                PartPose.offset(0.0F, 12.0F, -18.0F));

        partdefinition.addOrReplaceChild("RightEye",
                CubeListBuilder.create().texOffs(116, 10).mirror()
                        .addBox(-2.0F, -4.0F, -4.0F, 2, 2, 1),
                PartPose.offsetAndRotation(0.0F, 12.0F, -18.0F, 0.0F, 0.7853982F, 0.3490659F));

        partdefinition.addOrReplaceChild("LeftEye",
                CubeListBuilder.create().texOffs(94, 10).mirror()
                        .addBox(0.0F, -4.0F, -4.0F, 2, 2, 1),
                PartPose.offsetAndRotation(0.0F, 12.0F, -18.0F, 0.0F, -0.7853982F, -0.3490659F));

        partdefinition.addOrReplaceChild("Tooth16",
                CubeListBuilder.create().texOffs(24, 97).mirror()
                        .addBox(3.0F, -1.0F, -10.0F, 1, 1, 1),
                PartPose.offsetAndRotation(0.0F, 14.0F, -19.0F, 0.5235988F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("Tooth15",
                CubeListBuilder.create().texOffs(70, 97).mirror()
                        .addBox(-4.0F, -1.0F, -10.0F, 1, 1, 1),
                PartPose.offsetAndRotation(0.0F, 14.0F, -19.0F, 0.5235988F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("Tooth14",
                CubeListBuilder.create().texOffs(42, 95).mirror()
                        .addBox(-2.0F, -1.0F, -10.0F, 1, 1, 1),
                PartPose.offsetAndRotation(0.0F, 14.0F, -19.0F, 0.5235988F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("Tooth13",
                CubeListBuilder.create().texOffs(52, 95).mirror()
                        .addBox(1.0F, -1.0F, -10.0F, 1, 1, 1),
                PartPose.offsetAndRotation(0.0F, 14.0F, -19.0F, 0.5235988F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("Tooth12",
                CubeListBuilder.create().texOffs(24, 114).mirror()
                        .addBox(3.0F, -1.0F, -7.0F, 1, 1, 1),
                PartPose.offsetAndRotation(0.0F, 14.0F, -19.0F, 0.5235988F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("Tooth9",
                CubeListBuilder.create().texOffs(70, 114).mirror()
                        .addBox(-4.0F, -1.0F, -7.0F, 1, 1, 1),
                PartPose.offsetAndRotation(0.0F, 14.0F, -19.0F, 0.5235988F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("BottomJaw",
                CubeListBuilder.create().texOffs(31, 116).mirror()
                        .addBox(-4.0F, 0.0F, -10.0F, 8, 2, 10),
                PartPose.offsetAndRotation(0.0F, 14.0F, -19.0F, 0.5235988F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("Hat1",
                CubeListBuilder.create().texOffs(30, 40).mirror()
                        .addBox(-2.0F, -4.0F, -6.0F, 4, 1, 6),
                PartPose.offset(0.0F, 12.0F, -18.0F));

        partdefinition.addOrReplaceChild("Hat2",
                CubeListBuilder.create().texOffs(30, 40).mirror()
                        .addBox(-1.5F, -6.0F, -4.0F, 3, 2, 4),
                PartPose.offset(0.0F, 12.0F, -18.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(Lizard entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float newangle = 0.0f;
        newangle = (double)limbSwingAmount > 0.1 ? Mth.cos((float)(ageInTicks * 1.0f * limbSwingAmount)) * (float)Math.PI * 0.25f * limbSwingAmount : 0.0f;
        this.TopFrontLeftLeg.yRot = newangle;
        this.BottomFrontLeftLeg.xRot = newangle;
        this.FrontLeftFoot.yRot = newangle;
        this.Toe8.yRot = newangle;
        this.Toe1.yRot = newangle;
        this.TopFrontRightLeg.yRot = newangle;
        this.BottomFrontRightLeg.xRot = -newangle;
        this.FrontRightFoot.yRot = newangle;
        this.Toe3.yRot = newangle;
        this.Toe2.yRot = newangle;
        this.TopBackLeftLeg.yRot = -newangle;
        this.BottomBackLeftLeg.xRot = -newangle;
        this.BackLeftFoot.yRot = -newangle;
        this.Toe7.yRot = -newangle;
        this.Toe6.yRot = -newangle;
        this.TopBackRightLeg.yRot = -newangle;
        this.BottomBackRightLeg.xRot = newangle;
        this.BackRightFoot.yRot = -newangle;
        this.Toe4.yRot = -newangle;
        this.Toe5.yRot = -newangle;
        this.BottomJaw.xRot = entity.getAttacking() != 0 ? 0.52f + Mth.cos((float)(ageInTicks * 0.45f)) * 0.35f : 0.25f;
        this.Tooth9.xRot = this.BottomJaw.xRot;
        this.Tooth15.xRot = this.BottomJaw.xRot;
        this.Tooth14.xRot = this.BottomJaw.xRot;
        this.Tooth13.xRot = this.BottomJaw.xRot;
        this.Tooth16.xRot = this.BottomJaw.xRot;
        this.Tooth12.xRot = this.BottomJaw.xRot;
        newangle = Mth.cos((float)(ageInTicks * 0.25f * limbSwingAmount)) * (float)Math.PI * 0.05f;
        if (entity.getAttacking() != 0) {
        newangle = Mth.cos((float)(ageInTicks * 1.25f * limbSwingAmount)) * (float)Math.PI * 0.35f;
        }
        this.TailBase1.yRot = newangle * 0.25f;
        this.Tail2.z = this.TailBase1.z + (float)Math.cos(this.TailBase1.yRot) * 12.0f;
        this.Tail2.x = this.TailBase1.x + (float)Math.sin(this.TailBase1.yRot) * 12.0f;
        this.Tail2.yRot = newangle * 0.5f;
        this.Tail3.z = this.Tail2.z + (float)Math.cos(this.Tail2.yRot) * 9.0f;
        this.Tail3.x = this.Tail2.x + (float)Math.sin(this.Tail2.yRot) * 9.0f;
        this.Tail3.yRot = newangle * 0.75f;
        this.Tail4.z = this.Tail3.z + (float)Math.cos(this.Tail3.yRot) * 7.0f;
        this.Tail4.x = this.Tail3.x + (float)Math.sin(this.Tail3.yRot) * 7.0f;
        this.Tail4.yRot = newangle * 1.0f;
        this.TailTip.z = this.Tail4.z + (float)Math.cos(this.Tail4.yRot) * 7.0f;
        this.TailTip.x = this.Tail4.x + (float)Math.sin(this.Tail4.yRot) * 7.0f;
        this.TailTip.yRot = newangle * 1.25f;
        this.Neck.yRot = (float)Math.toRadians(netHeadYaw) * 0.25f;
        this.JawTop.z = this.Neck.z - (float)Math.cos(this.Neck.yRot) * 2.0f;
        this.JawTop.x = this.Neck.x - (float)Math.sin(this.Neck.yRot) * 2.0f;
        this.JawTop.yRot = (float)Math.toRadians(netHeadYaw) * 0.5f;
        this.TopNose.z = this.JawTop.z;
        this.TopNose.x = this.JawTop.x;
        this.TopNose.yRot = this.JawTop.yRot;
        this.BottomNose.z = this.JawTop.z;
        this.BottomNose.x = this.JawTop.x;
        this.BottomNose.yRot = this.JawTop.yRot;
        this.CenterRightNose.z = this.JawTop.z;
        this.CenterRightNose.x = this.JawTop.x;
        this.CenterRightNose.yRot = this.JawTop.yRot;
        this.CenterMiddleNose.z = this.JawTop.z;
        this.CenterMiddleNose.x = this.JawTop.x;
        this.CenterMiddleNose.yRot = this.JawTop.yRot;
        this.CenterLeftNose.z = this.JawTop.z;
        this.CenterLeftNose.x = this.JawTop.x;
        this.CenterLeftNose.yRot = this.JawTop.yRot;
        this.RightEye.z = this.JawTop.z;
        this.RightEye.x = this.JawTop.x;
        this.RightEye.yRot = this.JawTop.yRot + 0.78f;
        this.LeftEye.z = this.JawTop.z;
        this.LeftEye.x = this.JawTop.x;
        this.LeftEye.yRot = this.JawTop.yRot - 0.78f;
        this.Tooth11.z = this.JawTop.z;
        this.Tooth11.x = this.JawTop.x;
        this.Tooth11.yRot = this.JawTop.yRot;
        this.Tooth10.z = this.JawTop.z;
        this.Tooth10.x = this.JawTop.x;
        this.Tooth10.yRot = this.JawTop.yRot;
        this.Tooth1.z = this.JawTop.z;
        this.Tooth1.x = this.JawTop.x;
        this.Tooth1.yRot = this.JawTop.yRot;
        this.Tooth8.z = this.JawTop.z;
        this.Tooth8.x = this.JawTop.x;
        this.Tooth8.yRot = this.JawTop.yRot;
        this.Tooth4.z = this.JawTop.z;
        this.Tooth4.x = this.JawTop.x;
        this.Tooth4.yRot = this.JawTop.yRot;
        this.Tooth3.z = this.JawTop.z;
        this.Tooth3.x = this.JawTop.x;
        this.Tooth3.yRot = this.JawTop.yRot;
        this.Tooth5.z = this.JawTop.z;
        this.Tooth5.x = this.JawTop.x;
        this.Tooth5.yRot = this.JawTop.yRot;
        this.Tooth6.z = this.JawTop.z;
        this.Tooth6.x = this.JawTop.x;
        this.Tooth6.yRot = this.JawTop.yRot;
        this.Tooth7.z = this.JawTop.z;
        this.Tooth7.x = this.JawTop.x;
        this.Tooth7.yRot = this.JawTop.yRot;
        this.Tooth2.z = this.JawTop.z;
        this.Tooth2.x = this.JawTop.x;
        this.Tooth2.yRot = this.JawTop.yRot;
        this.Hat1.z = this.JawTop.z;
        this.Hat1.x = this.JawTop.x;
        this.Hat1.yRot = this.JawTop.yRot;
        this.Hat2.z = this.JawTop.z;
        this.Hat2.x = this.JawTop.x;
        this.Hat2.yRot = this.JawTop.yRot;
        this.BottomJaw.z = this.Neck.z - (float)Math.cos(this.Neck.yRot) * 3.0f;
        this.BottomJaw.x = this.Neck.x - (float)Math.sin(this.Neck.yRot) * 3.0f;
        this.BottomJaw.yRot = (float)Math.toRadians(netHeadYaw) * 0.5f;
        this.Tooth9.z = this.BottomJaw.z;
        this.Tooth9.x = this.BottomJaw.x;
        this.Tooth9.yRot = this.BottomJaw.yRot;
        this.Tooth16.z = this.BottomJaw.z;
        this.Tooth16.x = this.BottomJaw.x;
        this.Tooth16.yRot = this.BottomJaw.yRot;
        this.Tooth15.z = this.BottomJaw.z;
        this.Tooth15.x = this.BottomJaw.x;
        this.Tooth15.yRot = this.BottomJaw.yRot;
        this.Tooth14.z = this.BottomJaw.z;
        this.Tooth14.x = this.BottomJaw.x;
        this.Tooth14.yRot = this.BottomJaw.yRot;
        this.Tooth13.z = this.BottomJaw.z;
        this.Tooth13.x = this.BottomJaw.x;
        this.Tooth13.yRot = this.BottomJaw.yRot;
        this.Tooth12.z = this.BottomJaw.z;
        this.Tooth12.x = this.BottomJaw.x;
        this.Tooth12.yRot = this.BottomJaw.yRot;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.BodyBack.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.TopBackLeftLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.TailTip.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.BodyFront.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.TailBase1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Tail2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Tail3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Tail4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Neck.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.TopFrontLeftLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.TopBackRightLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.BottomBackRightLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.TopFrontRightLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.BottomBackLeftLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.BottomFrontRightLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.BottomFrontLeftLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.BodyCenter.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Toe7.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Toe6.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.BackLeftFoot.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Toe4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Toe5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.BackRightFoot.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Toe8.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Toe1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.FrontLeftFoot.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Toe3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Toe2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.FrontRightFoot.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.FinRidge7.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.FinRidge6.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.FinRidge5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.FinRidge4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.FinRidge3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.FinRidge2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.FinRidge1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Tooth11.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Tooth10.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Tooth8.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Tooth7.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Tooth6.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Tooth5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Tooth4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Tooth3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Tooth2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.CenterRightNose.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.CenterLeftNose.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Tooth1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.BottomNose.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.TopNose.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.JawTop.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.CenterMiddleNose.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.RightEye.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.LeftEye.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Tooth16.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Tooth15.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Tooth14.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Tooth13.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Tooth12.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Tooth9.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.BottomJaw.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Hat1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Hat2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Fin10.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Fin9.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Fin8.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Fin7.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Fin6.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Fin5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Fin3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Fin2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
