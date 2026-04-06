package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.CaveFisher;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class ModelCaveFisher extends EntityModel<CaveFisher> {
    private int ri1, ri2;
    private final ModelPart Nose;
    private final ModelPart EyeLeft;
    private final ModelPart HeadMid;
    private final ModelPart HeadEnd;
    private final ModelPart TailTuft;
    private final ModelPart EyeRight;
    private final ModelPart BodyTopLeft4;
    private final ModelPart BodyTopRight4;
    private final ModelPart BodyTopLeft1;
    private final ModelPart BodyTopRight1;
    private final ModelPart BodyTopRight2;
    private final ModelPart BodyTopLeft2;
    private final ModelPart BodyTopRight3;
    private final ModelPart BodyTopLeft3;
    private final ModelPart HeadBase;
    private final ModelPart TailBase;
    private final ModelPart BodyLow2;
    private final ModelPart BodyLow1;
    private final ModelPart Spine5;
    private final ModelPart Spine1;
    private final ModelPart Spine2;
    private final ModelPart Spine3;
    private final ModelPart Spine4;
    private final ModelPart RightArmSeg4;
    private final ModelPart LeftArmSeg1;
    private final ModelPart LeftArmSeg3;
    private final ModelPart RightArmSeg2;
    private final ModelPart RightArmSeg1;
    private final ModelPart LeftArmSeg5;
    private final ModelPart LeftArmSeg2;
    private final ModelPart LeftClawTop;
    private final ModelPart RightArmSeg3;
    private final ModelPart RightArmSeg5;
    private final ModelPart LeftArmSeg4;
    private final ModelPart LeftClawBase;
    private final ModelPart RightClawBase;
    private final ModelPart LeftClawLow;
    private final ModelPart RightClawTop;
    private final ModelPart RightClawLow;
    private final ModelPart LBLeg1;
    private final ModelPart LBLeg3;
    private final ModelPart RBLeg1;
    private final ModelPart RBLeg3;
    private final ModelPart LBLeg2;
    private final ModelPart RBLeg2;
    private final ModelPart LBLeg4;
    private final ModelPart RBLeg4;
    private final ModelPart RBLeg5;
    private final ModelPart LBLeg6;
    private final ModelPart RBLeg6;
    private final ModelPart LBLeg5;
    private final ModelPart RFLeg1;
    private final ModelPart RFLeg2;
    private final ModelPart RFLeg3;
    private final ModelPart RFLeg4;
    private final ModelPart RFLeg5;
    private final ModelPart RFLeg6;
    private final ModelPart RMLeg1;
    private final ModelPart RMLeg2;
    private final ModelPart RMLeg3;
    private final ModelPart RMLeg4;
    private final ModelPart RMLeg5;
    private final ModelPart RMLeg6;
    private final ModelPart LFLeg1;
    private final ModelPart LFLeg2;
    private final ModelPart LFLeg3;
    private final ModelPart LFLeg4;
    private final ModelPart LFLeg5;
    private final ModelPart LFLeg6;
    private final ModelPart LMLeg1;
    private final ModelPart LMLeg2;
    private final ModelPart LMLeg4;
    private final ModelPart LMLeg3;
    private final ModelPart LMLeg5;
    private final ModelPart LMLeg6;

    public ModelCaveFisher(ModelPart root) {
        this.Nose = root.getChild("Nose");
        this.EyeLeft = root.getChild("EyeLeft");
        this.HeadMid = root.getChild("HeadMid");
        this.HeadEnd = root.getChild("HeadEnd");
        this.TailTuft = root.getChild("TailTuft");
        this.EyeRight = root.getChild("EyeRight");
        this.BodyTopLeft4 = root.getChild("BodyTopLeft4");
        this.BodyTopRight4 = root.getChild("BodyTopRight4");
        this.BodyTopLeft1 = root.getChild("BodyTopLeft1");
        this.BodyTopRight1 = root.getChild("BodyTopRight1");
        this.BodyTopRight2 = root.getChild("BodyTopRight2");
        this.BodyTopLeft2 = root.getChild("BodyTopLeft2");
        this.BodyTopRight3 = root.getChild("BodyTopRight3");
        this.BodyTopLeft3 = root.getChild("BodyTopLeft3");
        this.HeadBase = root.getChild("HeadBase");
        this.TailBase = root.getChild("TailBase");
        this.BodyLow2 = root.getChild("BodyLow2");
        this.BodyLow1 = root.getChild("BodyLow1");
        this.Spine5 = root.getChild("Spine5");
        this.Spine1 = root.getChild("Spine1");
        this.Spine2 = root.getChild("Spine2");
        this.Spine3 = root.getChild("Spine3");
        this.Spine4 = root.getChild("Spine4");
        this.RightArmSeg4 = root.getChild("RightArmSeg4");
        this.LeftArmSeg1 = root.getChild("LeftArmSeg1");
        this.LeftArmSeg3 = root.getChild("LeftArmSeg3");
        this.RightArmSeg2 = root.getChild("RightArmSeg2");
        this.RightArmSeg1 = root.getChild("RightArmSeg1");
        this.LeftArmSeg5 = root.getChild("LeftArmSeg5");
        this.LeftArmSeg2 = root.getChild("LeftArmSeg2");
        this.LeftClawTop = root.getChild("LeftClawTop");
        this.RightArmSeg3 = root.getChild("RightArmSeg3");
        this.RightArmSeg5 = root.getChild("RightArmSeg5");
        this.LeftArmSeg4 = root.getChild("LeftArmSeg4");
        this.LeftClawBase = root.getChild("LeftClawBase");
        this.RightClawBase = root.getChild("RightClawBase");
        this.LeftClawLow = root.getChild("LeftClawLow");
        this.RightClawTop = root.getChild("RightClawTop");
        this.RightClawLow = root.getChild("RightClawLow");
        this.LBLeg1 = root.getChild("LBLeg1");
        this.LBLeg3 = root.getChild("LBLeg3");
        this.RBLeg1 = root.getChild("RBLeg1");
        this.RBLeg3 = root.getChild("RBLeg3");
        this.LBLeg2 = root.getChild("LBLeg2");
        this.RBLeg2 = root.getChild("RBLeg2");
        this.LBLeg4 = root.getChild("LBLeg4");
        this.RBLeg4 = root.getChild("RBLeg4");
        this.RBLeg5 = root.getChild("RBLeg5");
        this.LBLeg6 = root.getChild("LBLeg6");
        this.RBLeg6 = root.getChild("RBLeg6");
        this.LBLeg5 = root.getChild("LBLeg5");
        this.RFLeg1 = root.getChild("RFLeg1");
        this.RFLeg2 = root.getChild("RFLeg2");
        this.RFLeg3 = root.getChild("RFLeg3");
        this.RFLeg4 = root.getChild("RFLeg4");
        this.RFLeg5 = root.getChild("RFLeg5");
        this.RFLeg6 = root.getChild("RFLeg6");
        this.RMLeg1 = root.getChild("RMLeg1");
        this.RMLeg2 = root.getChild("RMLeg2");
        this.RMLeg3 = root.getChild("RMLeg3");
        this.RMLeg4 = root.getChild("RMLeg4");
        this.RMLeg5 = root.getChild("RMLeg5");
        this.RMLeg6 = root.getChild("RMLeg6");
        this.LFLeg1 = root.getChild("LFLeg1");
        this.LFLeg2 = root.getChild("LFLeg2");
        this.LFLeg3 = root.getChild("LFLeg3");
        this.LFLeg4 = root.getChild("LFLeg4");
        this.LFLeg5 = root.getChild("LFLeg5");
        this.LFLeg6 = root.getChild("LFLeg6");
        this.LMLeg1 = root.getChild("LMLeg1");
        this.LMLeg2 = root.getChild("LMLeg2");
        this.LMLeg4 = root.getChild("LMLeg4");
        this.LMLeg3 = root.getChild("LMLeg3");
        this.LMLeg5 = root.getChild("LMLeg5");
        this.LMLeg6 = root.getChild("LMLeg6");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("Nose",
                CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-0.5F, -0.5F, -12.0F, 1, 1, 6),
                PartPose.offset(0.0F, 19.0F, -4.0F));

        partdefinition.addOrReplaceChild("EyeLeft",
                CubeListBuilder.create().texOffs(0, 28).mirror().addBox(0.5F, -2.5F, -2.5F, 3, 2, 2),
                PartPose.offset(0.0F, 19.0F, -4.0F));

        partdefinition.addOrReplaceChild("HeadMid",
                CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-2.5F, -1.5F, -5.0F, 5, 3, 2),
                PartPose.offset(0.0F, 19.0F, -4.0F));

        partdefinition.addOrReplaceChild("HeadEnd",
                CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-2.0F, -1.0F, -6.0F, 4, 2, 1),
                PartPose.offset(0.0F, 19.0F, -4.0F));

        partdefinition.addOrReplaceChild("TailTuft",
                CubeListBuilder.create().texOffs(0, 23).mirror().addBox(-2.0F, -1.0F, 3.0F, 4, 1, 2),
                PartPose.offset(0.0F, 19.0F, 10.0F));

        partdefinition.addOrReplaceChild("EyeRight",
                CubeListBuilder.create().texOffs(0, 28).mirror().addBox(-3.5F, -2.5F, -2.5F, 3, 2, 2),
                PartPose.offset(0.0F, 19.0F, -4.0F));

        partdefinition.addOrReplaceChild("BodyTopLeft4",
                CubeListBuilder.create().texOffs(0, 0).mirror().addBox(0.0F, 0.0F, 0.0F, 6, 3, 4),
                PartPose.offsetAndRotation(0.0F, 16.2F, 7.0F, 0.1047198F, 0.1047198F, 0.1047198F));

        partdefinition.addOrReplaceChild("BodyTopRight4",
                CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-5.0F, 0.0F, 0.0F, 6, 3, 4),
                PartPose.offsetAndRotation(-1.0F, 16.2F, 7.0F, 0.1047198F, -0.1047198F, -0.1047198F));

        partdefinition.addOrReplaceChild("BodyTopLeft1",
                CubeListBuilder.create().texOffs(0, 0).mirror().addBox(0.0F, 0.0F, 0.0F, 5, 3, 4),
                PartPose.offsetAndRotation(0.0F, 16.0F, -4.0F, 0.1745329F, 0.1745329F, 0.1047198F));

        partdefinition.addOrReplaceChild("BodyTopRight1",
                CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-5.0F, 0.0F, 0.0F, 5, 3, 4),
                PartPose.offsetAndRotation(0.0F, 16.0F, -4.0F, 0.1745329F, -0.1745329F, -0.1047198F));

        partdefinition.addOrReplaceChild("BodyTopRight2",
                CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-5.0F, 0.0F, 0.0F, 7, 3, 4),
                PartPose.offsetAndRotation(-1.0F, 16.0F, -1.0F, 0.2094395F, -0.1745329F, -0.1047198F));

        partdefinition.addOrReplaceChild("BodyTopLeft2",
                CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-1.0F, 0.0F, 0.0F, 7, 3, 4),
                PartPose.offsetAndRotation(0.0F, 16.0F, -1.0F, 0.2094395F, 0.1745329F, 0.1047198F));

        partdefinition.addOrReplaceChild("BodyTopRight3",
                CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-5.0F, 0.0F, 0.0F, 6, 3, 4),
                PartPose.offsetAndRotation(-1.0F, 16.0F, 3.0F, 0.1396263F, -0.1396263F, -0.1047198F));

        partdefinition.addOrReplaceChild("BodyTopLeft3",
                CubeListBuilder.create().texOffs(0, 0).mirror().addBox(0.0F, 0.0F, 0.0F, 6, 3, 4),
                PartPose.offsetAndRotation(0.0F, 16.0F, 3.0F, 0.1396263F, 0.1396263F, 0.1047198F));

        partdefinition.addOrReplaceChild("HeadBase",
                CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-3.0F, -2.0F, -3.0F, 6, 4, 3),
                PartPose.offset(0.0F, 19.0F, -4.0F));

        partdefinition.addOrReplaceChild("TailBase",
                CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-3.0F, -2.0F, 0.0F, 6, 3, 3),
                PartPose.offset(0.0F, 19.0F, 10.0F));

        partdefinition.addOrReplaceChild("BodyLow2",
                CubeListBuilder.create().texOffs(34, 0).mirror().addBox(0.0F, 0.0F, 0.0F, 8, 2, 7),
                PartPose.offset(-4.0F, 18.3F, 3.0F));

        partdefinition.addOrReplaceChild("BodyLow1",
                CubeListBuilder.create().texOffs(34, 0).mirror().addBox(0.0F, 0.0F, 0.0F, 8, 2, 7),
                PartPose.offset(-4.0F, 18.7F, -4.0F));

        partdefinition.addOrReplaceChild("Spine5",
                CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-0.5F, 0.0F, 0.0F, 1, 1, 4),
                PartPose.offset(0.0F, 16.0F, 8.6F));

        partdefinition.addOrReplaceChild("Spine1",
                CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-0.5F, 0.0F, 0.0F, 1, 1, 4),
                PartPose.offsetAndRotation(0.0F, 16.0F, -4.2F, 0.2443461F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("Spine2",
                CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-0.5F, 0.0F, 0.0F, 1, 1, 5),
                PartPose.offsetAndRotation(0.0F, 16.0F, -1.2F, 0.3141593F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("Spine3",
                CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-0.5F, 0.0F, 0.0F, 1, 1, 6),
                PartPose.offsetAndRotation(0.0F, 16.0F, 1.8F, 0.2792527F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("Spine4",
                CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-0.5F, 0.0F, 0.0F, 1, 1, 8),
                PartPose.offsetAndRotation(0.0F, 16.0F, 3.8F, 0.1745329F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("RightArmSeg4",
                CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-3.2F, -1.0F, -10.5F, 2, 2, 4),
                PartPose.offsetAndRotation(-4.7F, 17.5F, -3.0F, 0.0F, 0.0872665F, 0.0F));

        partdefinition.addOrReplaceChild("LeftArmSeg1",
                CubeListBuilder.create().texOffs(0, 13).mirror().addBox(-0.5F, -0.5F, -4.0F, 1, 1, 4),
                PartPose.offsetAndRotation(4.7F, 17.5F, -3.0F, 0.0F, -0.5235988F, 0.0F));

        partdefinition.addOrReplaceChild("LeftArmSeg3",
                CubeListBuilder.create().texOffs(0, 13).mirror().addBox(1.0F, -0.5F, -8.0F, 1, 1, 3),
                PartPose.offsetAndRotation(4.7F, 17.5F, -3.0F, 0.0F, -0.1745329F, 0.0F));

        partdefinition.addOrReplaceChild("RightArmSeg2",
                CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-1.5F, -1.0F, -6.0F, 2, 2, 4),
                PartPose.offsetAndRotation(-4.7F, 17.5F, -3.0F, 0.0F, 0.3490659F, 0.0F));

        partdefinition.addOrReplaceChild("RightArmSeg1",
                CubeListBuilder.create().texOffs(0, 13).mirror().addBox(-0.5F, -0.5F, -4.0F, 1, 1, 4),
                PartPose.offsetAndRotation(-4.7F, 17.5F, -3.0F, 0.0F, 0.5235988F, 0.0F));

        partdefinition.addOrReplaceChild("LeftArmSeg5",
                CubeListBuilder.create().texOffs(0, 13).mirror().addBox(2.4F, -0.5F, -12.0F, 1, 1, 3),
                PartPose.offset(4.7F, 17.5F, -3.0F));

        partdefinition.addOrReplaceChild("LeftArmSeg2",
                CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-0.5F, -1.0F, -6.0F, 2, 2, 4),
                PartPose.offsetAndRotation(4.7F, 17.5F, -3.0F, 0.0F, -0.3490659F, 0.0F));

        partdefinition.addOrReplaceChild("LeftClawTop",
                CubeListBuilder.create().texOffs(15, 15).mirror().addBox(1.8F, 4.7F, -15.0F, 2, 2, 5),
                PartPose.offsetAndRotation(4.7F, 17.5F, -3.0F, -0.5410521F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("RightArmSeg3",
                CubeListBuilder.create().texOffs(0, 13).mirror().addBox(-2.0F, -0.5F, -8.0F, 1, 1, 3),
                PartPose.offsetAndRotation(-4.7F, 17.5F, -3.0F, 0.0F, 0.1745329F, 0.0F));

        partdefinition.addOrReplaceChild("RightArmSeg5",
                CubeListBuilder.create().texOffs(0, 13).mirror().addBox(-3.6F, -0.5F, -12.0F, 1, 1, 3),
                PartPose.offset(-4.7F, 17.5F, -3.0F));

        partdefinition.addOrReplaceChild("LeftArmSeg4",
                CubeListBuilder.create().texOffs(0, 0).mirror().addBox(1.1F, -1.0F, -10.5F, 2, 2, 4),
                PartPose.offsetAndRotation(4.7F, 17.5F, -3.0F, 0.0F, -0.0872665F, 0.0F));

        partdefinition.addOrReplaceChild("LeftClawBase",
                CubeListBuilder.create().texOffs(0, 0).mirror().addBox(1.8F, -1.0F, -13.0F, 2, 2, 2),
                PartPose.offset(4.7F, 17.5F, -3.0F));

        partdefinition.addOrReplaceChild("RightClawBase",
                CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-4.2F, -1.0F, -13.0F, 2, 2, 2),
                PartPose.offset(-4.7F, 17.5F, -3.0F));

        partdefinition.addOrReplaceChild("LeftClawLow",
                CubeListBuilder.create().texOffs(25, 25).mirror().addBox(1.8F, -4.3F, -15.0F, 2, 1, 4),
                PartPose.offsetAndRotation(4.7F, 17.5F, -3.0F, 0.3490659F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("RightClawTop",
                CubeListBuilder.create().texOffs(15, 15).mirror().addBox(-4.2F, 4.7F, -15.0F, 2, 2, 5),
                PartPose.offsetAndRotation(-4.7F, 17.5F, -3.0F, -0.5410521F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("RightClawLow",
                CubeListBuilder.create().texOffs(25, 25).mirror().addBox(-4.2F, -4.3F, -15.0F, 2, 1, 4),
                PartPose.offsetAndRotation(-4.7F, 17.5F, -3.0F, 0.3490659F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("LBLeg1",
                CubeListBuilder.create().texOffs(0, 13).mirror().addBox(0.5F, -0.5F, -0.5F, 3, 1, 1),
                PartPose.offsetAndRotation(5.0F, 18.0F, 8.5F, 0.0F, 0.0F, -0.4363323F));

        partdefinition.addOrReplaceChild("LBLeg3",
                CubeListBuilder.create().texOffs(2, 0).mirror().addBox(5.1F, -1.5F, -1.0F, 3, 1, 2),
                PartPose.offsetAndRotation(5.0F, 18.0F, 8.5F, 0.0F, 0.0F, -0.5759587F));

        partdefinition.addOrReplaceChild("RBLeg1",
                CubeListBuilder.create().texOffs(0, 13).mirror().addBox(-3.5F, -0.5F, -0.5F, 3, 1, 1),
                PartPose.offsetAndRotation(-5.0F, 18.0F, 8.5F, 0.0F, 0.0F, 0.4363323F));

        partdefinition.addOrReplaceChild("RBLeg3",
                CubeListBuilder.create().texOffs(2, 0).mirror().addBox(-8.1F, -1.5F, -1.0F, 3, 1, 2),
                PartPose.offsetAndRotation(-5.0F, 18.0F, 8.5F, 0.0F, 0.0F, 0.5759587F));

        partdefinition.addOrReplaceChild("LBLeg2",
                CubeListBuilder.create().texOffs(0, 0).mirror().addBox(2.5F, 0.5F, -1.0F, 3, 2, 2),
                PartPose.offsetAndRotation(5.0F, 18.0F, 8.5F, 0.0F, 0.0F, -0.9599311F));

        partdefinition.addOrReplaceChild("RBLeg2",
                CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-5.5F, 0.5F, -1.0F, 3, 2, 2),
                PartPose.offsetAndRotation(-5.0F, 18.0F, 8.5F, 0.0F, 0.0F, 0.9599311F));

        partdefinition.addOrReplaceChild("LBLeg4",
                CubeListBuilder.create().texOffs(0, 13).mirror().addBox(5.0F, -3.0F, -0.5F, 1, 3, 1),
                PartPose.offsetAndRotation(5.0F, 18.0F, 8.5F, 0.0F, 0.0F, -0.2094395F));

        partdefinition.addOrReplaceChild("RBLeg4",
                CubeListBuilder.create().texOffs(0, 13).mirror().addBox(-6.0F, -3.0F, -0.5F, 1, 3, 1),
                PartPose.offsetAndRotation(-5.0F, 18.0F, 8.5F, 0.0F, 0.0F, 0.2094395F));

        partdefinition.addOrReplaceChild("RBLeg5",
                CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-6.4F, -1.0F, -1.0F, 2, 6, 2),
                PartPose.offsetAndRotation(-5.0F, 18.0F, 8.5F, 0.0F, 0.0F, 0.1047198F));

        partdefinition.addOrReplaceChild("LBLeg6",
                CubeListBuilder.create().texOffs(0, 13).mirror().addBox(5.5F, 3.0F, -0.5F, 1, 3, 1),
                PartPose.offset(5.0F, 18.0F, 8.5F));

        partdefinition.addOrReplaceChild("RBLeg6",
                CubeListBuilder.create().texOffs(0, 13).mirror().addBox(-6.5F, 3.0F, -0.5F, 1, 3, 1),
                PartPose.offset(-5.0F, 18.0F, 8.5F));

        partdefinition.addOrReplaceChild("LBLeg5",
                CubeListBuilder.create().texOffs(0, 0).mirror().addBox(4.6F, -1.0F, -1.0F, 2, 6, 2),
                PartPose.offsetAndRotation(5.0F, 18.0F, 8.5F, 0.0F, 0.0F, -0.1047198F));

        partdefinition.addOrReplaceChild("RFLeg1",
                CubeListBuilder.create().texOffs(0, 13).mirror().addBox(-3.5F, -0.5F, -0.5F, 3, 1, 1),
                PartPose.offsetAndRotation(-5.0F, 18.0F, 0.5F, 0.0F, 0.0F, 0.4363323F));

        partdefinition.addOrReplaceChild("RFLeg2",
                CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-5.5F, 0.5F, -1.0F, 3, 2, 2),
                PartPose.offsetAndRotation(-5.0F, 18.0F, 0.5F, 0.0F, 0.0F, 0.9599311F));

        partdefinition.addOrReplaceChild("RFLeg3",
                CubeListBuilder.create().texOffs(2, 0).mirror().addBox(-8.1F, -1.5F, -1.0F, 3, 1, 2),
                PartPose.offsetAndRotation(-5.0F, 18.0F, 0.5F, 0.0F, 0.0F, 0.5759587F));

        partdefinition.addOrReplaceChild("RFLeg4",
                CubeListBuilder.create().texOffs(0, 13).mirror().addBox(-6.0F, -3.0F, -0.5F, 1, 3, 1),
                PartPose.offsetAndRotation(-5.0F, 18.0F, 0.5F, 0.0F, 0.0F, 0.2094395F));

        partdefinition.addOrReplaceChild("RFLeg5",
                CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-6.4F, -1.0F, -1.0F, 2, 6, 2),
                PartPose.offsetAndRotation(-5.0F, 18.0F, 0.5F, 0.0F, 0.0F, 0.1047198F));

        partdefinition.addOrReplaceChild("RFLeg6",
                CubeListBuilder.create().texOffs(0, 13).mirror().addBox(-6.5F, 3.0F, -0.5F, 1, 3, 1),
                PartPose.offset(-5.0F, 18.0F, 0.5F));

        partdefinition.addOrReplaceChild("RMLeg1",
                CubeListBuilder.create().texOffs(0, 13).mirror().addBox(-3.5F, -0.5F, -0.5F, 3, 1, 1),
                PartPose.offsetAndRotation(-5.0F, 18.0F, 4.5F, 0.0F, 0.0F, 0.4363323F));

        partdefinition.addOrReplaceChild("RMLeg2",
                CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-5.5F, 0.5F, -1.0F, 3, 2, 2),
                PartPose.offsetAndRotation(-5.0F, 18.0F, 4.5F, 0.0F, 0.0F, 0.9599311F));

        partdefinition.addOrReplaceChild("RMLeg3",
                CubeListBuilder.create().texOffs(2, 0).mirror().addBox(-8.1F, -1.5F, -1.0F, 3, 1, 2),
                PartPose.offsetAndRotation(-5.0F, 18.0F, 4.5F, 0.0F, 0.0F, 0.5759587F));

        partdefinition.addOrReplaceChild("RMLeg4",
                CubeListBuilder.create().texOffs(0, 13).mirror().addBox(-6.0F, -3.0F, -0.5F, 1, 3, 1),
                PartPose.offsetAndRotation(-5.0F, 18.0F, 4.5F, 0.0F, 0.0F, 0.2094395F));

        partdefinition.addOrReplaceChild("RMLeg5",
                CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-6.4F, -1.0F, -1.0F, 2, 6, 2),
                PartPose.offsetAndRotation(-5.0F, 18.0F, 4.5F, 0.0F, 0.0F, 0.1047198F));

        partdefinition.addOrReplaceChild("RMLeg6",
                CubeListBuilder.create().texOffs(0, 13).mirror().addBox(-6.5F, 3.0F, -0.5F, 1, 3, 1),
                PartPose.offset(-5.0F, 18.0F, 4.5F));

        partdefinition.addOrReplaceChild("LFLeg1",
                CubeListBuilder.create().texOffs(0, 13).mirror().addBox(0.5F, -0.5F, -0.5F, 3, 1, 1),
                PartPose.offsetAndRotation(5.0F, 18.0F, 0.5F, 0.0F, 0.0F, -0.4363323F));

        partdefinition.addOrReplaceChild("LFLeg2",
                CubeListBuilder.create().texOffs(0, 0).mirror().addBox(2.5F, 0.5F, -1.0F, 3, 2, 2),
                PartPose.offsetAndRotation(5.0F, 18.0F, 0.5F, 0.0F, 0.0F, -0.9599311F));

        partdefinition.addOrReplaceChild("LFLeg3",
                CubeListBuilder.create().texOffs(2, 0).mirror().addBox(5.1F, -1.5F, -1.0F, 3, 1, 2),
                PartPose.offsetAndRotation(5.0F, 18.0F, 0.5F, 0.0F, 0.0F, -0.5759587F));

        partdefinition.addOrReplaceChild("LFLeg4",
                CubeListBuilder.create().texOffs(0, 13).mirror().addBox(5.0F, -3.0F, -0.5F, 1, 3, 1),
                PartPose.offsetAndRotation(5.0F, 18.0F, 0.5F, 0.0F, 0.0F, -0.2094395F));

        partdefinition.addOrReplaceChild("LFLeg5",
                CubeListBuilder.create().texOffs(0, 0).mirror().addBox(4.6F, -1.0F, -1.0F, 2, 6, 2),
                PartPose.offsetAndRotation(5.0F, 18.0F, 0.5F, 0.0F, 0.0F, -0.1047198F));

        partdefinition.addOrReplaceChild("LFLeg6",
                CubeListBuilder.create().texOffs(0, 13).mirror().addBox(5.5F, 3.0F, -0.5F, 1, 3, 1),
                PartPose.offset(5.0F, 18.0F, 0.5F));

        partdefinition.addOrReplaceChild("LMLeg1",
                CubeListBuilder.create().texOffs(0, 13).mirror().addBox(0.5F, -0.5F, -0.5F, 3, 1, 1),
                PartPose.offsetAndRotation(5.0F, 18.0F, 4.5F, 0.0F, 0.0F, -0.4363323F));

        partdefinition.addOrReplaceChild("LMLeg2",
                CubeListBuilder.create().texOffs(0, 0).mirror().addBox(2.5F, 0.5F, -1.0F, 3, 2, 2),
                PartPose.offsetAndRotation(5.0F, 18.0F, 4.5F, 0.0F, 0.0F, -0.9599311F));

        partdefinition.addOrReplaceChild("LMLeg4",
                CubeListBuilder.create().texOffs(0, 13).mirror().addBox(5.0F, -3.0F, -0.5F, 1, 3, 1),
                PartPose.offsetAndRotation(5.0F, 18.0F, 4.5F, 0.0F, 0.0F, -0.2094395F));

        partdefinition.addOrReplaceChild("LMLeg3",
                CubeListBuilder.create().texOffs(2, 0).mirror().addBox(5.1F, -1.5F, -1.0F, 3, 1, 2),
                PartPose.offsetAndRotation(5.0F, 18.0F, 4.5F, 0.0F, 0.0F, -0.5759587F));

        partdefinition.addOrReplaceChild("LMLeg5",
                CubeListBuilder.create().texOffs(0, 0).mirror().addBox(4.6F, -1.0F, -1.0F, 2, 6, 2),
                PartPose.offsetAndRotation(5.0F, 18.0F, 4.5F, 0.0F, 0.0F, -0.1047198F));

        partdefinition.addOrReplaceChild("LMLeg6",
                CubeListBuilder.create().texOffs(0, 13).mirror().addBox(5.5F, 3.0F, -0.5F, 1, 3, 1),
                PartPose.offset(5.0F, 18.0F, 4.5F));

        return LayerDefinition.create(meshdefinition, 64, 32);
    }

    @Override
    public void setupAnim(CaveFisher entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float newangle = 0.0f;
        float upangle = 0.0f;
        float nextangle = 0.0f;
        float pi4 = 1.570795f;
        this.LFLeg1.yRot = newangle = Mth.cos((float)(ageInTicks * 2.0f * limbSwingAmount)) * (float)Math.PI * 0.12f * limbSwingAmount;
        this.LFLeg2.yRot = newangle;
        this.LFLeg3.yRot = newangle;
        this.LFLeg4.yRot = newangle;
        this.LFLeg5.yRot = newangle;
        this.LFLeg6.yRot = newangle;
        this.RFLeg1.yRot = -newangle;
        this.RFLeg2.yRot = -newangle;
        this.RFLeg3.yRot = -newangle;
        this.RFLeg4.yRot = -newangle;
        this.RFLeg5.yRot = -newangle;
        this.RFLeg6.yRot = -newangle;
        this.LMLeg1.yRot = newangle = Mth.cos((float)(ageInTicks * 2.0f * limbSwingAmount - 1.0f * pi4)) * (float)Math.PI * 0.12f * limbSwingAmount;
        this.LMLeg2.yRot = newangle;
        this.LMLeg3.yRot = newangle;
        this.LMLeg4.yRot = newangle;
        this.LMLeg5.yRot = newangle;
        this.LMLeg6.yRot = newangle;
        this.RMLeg1.yRot = -newangle;
        this.RMLeg2.yRot = -newangle;
        this.RMLeg3.yRot = -newangle;
        this.RMLeg4.yRot = -newangle;
        this.RMLeg5.yRot = -newangle;
        this.RMLeg6.yRot = -newangle;
        this.LBLeg1.yRot = newangle = Mth.cos((float)(ageInTicks * 2.0f * limbSwingAmount - 2.0f * pi4)) * (float)Math.PI * 0.12f * limbSwingAmount;
        this.LBLeg2.yRot = newangle;
        this.LBLeg3.yRot = newangle;
        this.LBLeg4.yRot = newangle;
        this.LBLeg5.yRot = newangle;
        this.LBLeg6.yRot = newangle;
        this.RBLeg1.yRot = -newangle;
        this.RBLeg2.yRot = -newangle;
        this.RBLeg3.yRot = -newangle;
        this.RBLeg4.yRot = -newangle;
        this.RBLeg5.yRot = -newangle;
        this.RBLeg6.yRot = -newangle;
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
        this.doRightClaw(newangle);
        } else {
        this.doLeftClaw(0.0f);
        this.doRightClaw(0.0f);
        }
    }

    private void doLeftClaw(float angle) {
        this.LeftClawBase.yRot = -0.3f + angle;
        this.LeftClawTop.yRot = -0.3f + angle * 1.5f;
        this.LeftClawLow.yRot = -0.3f - angle;
    }

    private void doRightClaw(float angle) {
        this.RightClawBase.yRot = 0.3f - angle;
        this.RightClawTop.yRot = 0.3f - angle * 1.5f;
        this.RightClawLow.yRot = 0.3f + angle;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.Nose.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.EyeLeft.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.HeadMid.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.HeadEnd.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.TailTuft.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.EyeRight.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.BodyTopLeft4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.BodyTopRight4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.BodyTopLeft1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.BodyTopRight1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.BodyTopRight2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.BodyTopLeft2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.BodyTopRight3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.BodyTopLeft3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.HeadBase.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.TailBase.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.BodyLow2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.BodyLow1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Spine5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Spine1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Spine2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Spine3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Spine4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.RightArmSeg4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.LeftArmSeg1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.LeftArmSeg3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.RightArmSeg2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.RightArmSeg1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.LeftArmSeg5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.LeftArmSeg2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.LeftClawTop.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.RightArmSeg3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.RightArmSeg5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.LeftArmSeg4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.LeftClawBase.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.RightClawBase.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.LeftClawLow.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.RightClawTop.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.RightClawLow.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.LBLeg1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.LBLeg3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.RBLeg1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.RBLeg3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.LBLeg2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.RBLeg2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.LBLeg4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.RBLeg4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.RBLeg5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.LBLeg6.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.RBLeg6.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.LBLeg5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.RFLeg1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.RFLeg2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.RFLeg3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.RFLeg4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.RFLeg5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.RFLeg6.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.RMLeg1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.RMLeg2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.RMLeg3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.RMLeg4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.RMLeg5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.RMLeg6.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.LFLeg1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.LFLeg2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.LFLeg3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.LFLeg4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.LFLeg5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.LFLeg6.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.LMLeg1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.LMLeg2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.LMLeg4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.LMLeg3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.LMLeg5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.LMLeg6.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
