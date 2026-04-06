package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.Godzilla;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class ModelGodzilla extends EntityModel<Godzilla> {
    private static final float ANIM_SPEED = 1.0F;

    private final ModelPart LToe1;
    private final ModelPart LToe2;
    private final ModelPart LToe3;
    private final ModelPart LToe4;
    private final ModelPart LToe5;
    private final ModelPart LToe6;
    private final ModelPart LToe7;
    private final ModelPart LToe8;
    private final ModelPart LToe9;
    private final ModelPart RToe1;
    private final ModelPart RToe2;
    private final ModelPart RToe3;
    private final ModelPart RToe4;
    private final ModelPart RToe5;
    private final ModelPart RToe6;
    private final ModelPart RToe7;
    private final ModelPart RToe8;
    private final ModelPart RToe9;
    private final ModelPart LThigh;
    private final ModelPart LUpperLeg;
    private final ModelPart LLowerLeg;
    private final ModelPart RThigh;
    private final ModelPart RLegUpper;
    private final ModelPart RLegLower;
    private final ModelPart BodyBottom;
    private final ModelPart BodyCenter;
    private final ModelPart BodyTop;
    private final ModelPart Neck;
    private final ModelPart Head;
    private final ModelPart TopJaw;
    private final ModelPart LowerJaw;
    private final ModelPart TailBase;
    private final ModelPart Tail2;
    private final ModelPart Tail3;
    private final ModelPart Tail4;
    private final ModelPart Tail5;
    private final ModelPart Tail6;
    private final ModelPart Tail7;
    private final ModelPart TailTip;
    private final ModelPart RShoulder;
    private final ModelPart RUpperArm;
    private final ModelPart RLowerArm;
    private final ModelPart RHand;
    private final ModelPart RThumbBase;
    private final ModelPart RThumbTip;
    private final ModelPart RIndexBase;
    private final ModelPart RIndexTip;
    private final ModelPart R3rdFingerBase;
    private final ModelPart R3rdFingerTip;
    private final ModelPart LShoulder;
    private final ModelPart LUpperArm;
    private final ModelPart LLowerArm;
    private final ModelPart LHand;
    private final ModelPart LThumbBase;
    private final ModelPart LThumbTip;
    private final ModelPart LIndexBase;
    private final ModelPart LIndexTip;
    private final ModelPart L3rdFingerBase;
    private final ModelPart L3rdFingerTip;
    private final ModelPart Lspikes1;
    private final ModelPart Rspikes1;
    private final ModelPart Lspike2;
    private final ModelPart Rspike2;
    private final ModelPart Lspike3;
    private final ModelPart Rspike3;
    private final ModelPart Lspike4;
    private final ModelPart Rspike4;
    private final ModelPart Lspike5;
    private final ModelPart Rspike5;
    private final ModelPart Spike6;
    private final ModelPart Spikes7;

    public ModelGodzilla(ModelPart root) {
        this.LToe1 = root.getChild("LToe1");
        this.LToe2 = root.getChild("LToe2");
        this.LToe3 = root.getChild("LToe3");
        this.LToe4 = root.getChild("LToe4");
        this.LToe5 = root.getChild("LToe5");
        this.LToe6 = root.getChild("LToe6");
        this.LToe7 = root.getChild("LToe7");
        this.LToe8 = root.getChild("LToe8");
        this.LToe9 = root.getChild("LToe9");
        this.RToe1 = root.getChild("RToe1");
        this.RToe2 = root.getChild("RToe2");
        this.RToe3 = root.getChild("RToe3");
        this.RToe4 = root.getChild("RToe4");
        this.RToe5 = root.getChild("RToe5");
        this.RToe6 = root.getChild("RToe6");
        this.RToe7 = root.getChild("RToe7");
        this.RToe8 = root.getChild("RToe8");
        this.RToe9 = root.getChild("RToe9");
        this.LThigh = root.getChild("LThigh");
        this.LUpperLeg = root.getChild("LUpperLeg");
        this.LLowerLeg = root.getChild("LLowerLeg");
        this.RThigh = root.getChild("RThigh");
        this.RLegUpper = root.getChild("RLegUpper");
        this.RLegLower = root.getChild("RLegLower");
        this.BodyBottom = root.getChild("BodyBottom");
        this.BodyCenter = root.getChild("BodyCenter");
        this.BodyTop = root.getChild("BodyTop");
        this.Neck = root.getChild("Neck");
        this.Head = root.getChild("Head");
        this.TopJaw = root.getChild("TopJaw");
        this.LowerJaw = root.getChild("LowerJaw");
        this.TailBase = root.getChild("TailBase");
        this.Tail2 = root.getChild("Tail2");
        this.Tail3 = root.getChild("Tail3");
        this.Tail4 = root.getChild("Tail4");
        this.Tail5 = root.getChild("Tail5");
        this.Tail6 = root.getChild("Tail6");
        this.Tail7 = root.getChild("Tail7");
        this.TailTip = root.getChild("TailTip");
        this.RShoulder = root.getChild("RShoulder");
        this.RUpperArm = root.getChild("RUpperArm");
        this.RLowerArm = root.getChild("RLowerArm");
        this.RHand = root.getChild("RHand");
        this.RThumbBase = root.getChild("RThumbBase");
        this.RThumbTip = root.getChild("RThumbTip");
        this.RIndexBase = root.getChild("RIndexBase");
        this.RIndexTip = root.getChild("RIndexTip");
        this.R3rdFingerBase = root.getChild("R3rdFingerBase");
        this.R3rdFingerTip = root.getChild("R3rdFingerTip");
        this.LShoulder = root.getChild("LShoulder");
        this.LUpperArm = root.getChild("LUpperArm");
        this.LLowerArm = root.getChild("LLowerArm");
        this.LHand = root.getChild("LHand");
        this.LThumbBase = root.getChild("LThumbBase");
        this.LThumbTip = root.getChild("LThumbTip");
        this.LIndexBase = root.getChild("LIndexBase");
        this.LIndexTip = root.getChild("LIndexTip");
        this.L3rdFingerBase = root.getChild("L3rdFingerBase");
        this.L3rdFingerTip = root.getChild("L3rdFingerTip");
        this.Lspikes1 = root.getChild("Lspikes1");
        this.Rspikes1 = root.getChild("Rspikes1");
        this.Lspike2 = root.getChild("Lspike2");
        this.Rspike2 = root.getChild("Rspike2");
        this.Lspike3 = root.getChild("Lspike3");
        this.Rspike3 = root.getChild("Rspike3");
        this.Lspike4 = root.getChild("Lspike4");
        this.Rspike4 = root.getChild("Rspike4");
        this.Lspike5 = root.getChild("Lspike5");
        this.Rspike5 = root.getChild("Rspike5");
        this.Spike6 = root.getChild("Spike6");
        this.Spikes7 = root.getChild("Spikes7");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        // --- Left toes ---
        partdefinition.addOrReplaceChild("LToe1",
                CubeListBuilder.create().texOffs(45, 1002).mirror()
                        .addBox(-5.0F, -2.0F, -40.0F, 10, 10, 6),
                PartPose.offsetAndRotation(54.0F, 16.0F, 6.0F, 0.0F, 0.7853982F, 0.0F));

        partdefinition.addOrReplaceChild("LToe2",
                CubeListBuilder.create().texOffs(0, 1002).mirror()
                        .addBox(-7.0F, -6.0F, -34.0F, 14, 14, 8),
                PartPose.offsetAndRotation(54.0F, 16.0F, 6.0F, 0.0F, 0.7853982F, 0.0F));

        partdefinition.addOrReplaceChild("LToe3",
                CubeListBuilder.create().texOffs(0, 955).mirror()
                        .addBox(-8.0F, -8.0F, -26.0F, 16, 16, 30),
                PartPose.offsetAndRotation(54.0F, 16.0F, 6.0F, 0.0F, 0.7853982F, 0.0F));

        partdefinition.addOrReplaceChild("LToe4",
                CubeListBuilder.create().texOffs(45, 1002).mirror()
                        .addBox(-5.0F, -2.0F, -40.0F, 10, 10, 6),
                PartPose.offset(54.0F, 16.0F, 6.0F));

        partdefinition.addOrReplaceChild("LToe5",
                CubeListBuilder.create().texOffs(0, 1002).mirror()
                        .addBox(-7.0F, -6.0F, -34.0F, 14, 14, 8),
                PartPose.offset(54.0F, 16.0F, 6.0F));

        partdefinition.addOrReplaceChild("LToe6",
                CubeListBuilder.create().texOffs(92, 955).mirror()
                        .addBox(-8.0F, -8.0F, -26.0F, 16, 16, 36),
                PartPose.offset(54.0F, 16.0F, 6.0F));

        partdefinition.addOrReplaceChild("LToe7",
                CubeListBuilder.create().texOffs(45, 1002).mirror()
                        .addBox(-5.0F, -2.0F, -40.0F, 10, 10, 6),
                PartPose.offsetAndRotation(54.0F, 16.0F, 6.0F, 0.0F, -0.7853982F, 0.0F));

        partdefinition.addOrReplaceChild("LToe8",
                CubeListBuilder.create().texOffs(0, 1002).mirror()
                        .addBox(-7.0F, -6.0F, -34.0F, 14, 14, 8),
                PartPose.offsetAndRotation(54.0F, 16.0F, 6.0F, 0.0F, -0.7853982F, 0.0F));

        partdefinition.addOrReplaceChild("LToe9",
                CubeListBuilder.create().texOffs(0, 955).mirror()
                        .addBox(-8.0F, -8.0F, -26.0F, 16, 16, 30),
                PartPose.offsetAndRotation(54.0F, 16.0F, 6.0F, 0.0F, -0.7853982F, 0.0F));

        // --- Right toes ---
        partdefinition.addOrReplaceChild("RToe1",
                CubeListBuilder.create().texOffs(45, 1002).mirror()
                        .addBox(-5.0F, -2.0F, -40.0F, 10, 10, 6),
                PartPose.offsetAndRotation(-54.0F, 16.0F, 6.0F, 0.0F, -0.7853982F, 0.0F));

        partdefinition.addOrReplaceChild("RToe2",
                CubeListBuilder.create().texOffs(0, 1002).mirror()
                        .addBox(-7.0F, -6.0F, -34.0F, 14, 14, 8),
                PartPose.offsetAndRotation(-54.0F, 16.0F, 6.0F, 0.0F, -0.7853982F, 0.0F));

        partdefinition.addOrReplaceChild("RToe3",
                CubeListBuilder.create().texOffs(0, 955).mirror()
                        .addBox(-8.0F, -8.0F, -26.0F, 16, 16, 30),
                PartPose.offsetAndRotation(-54.0F, 16.0F, 6.0F, 0.0F, -0.7853982F, 0.0F));

        partdefinition.addOrReplaceChild("RToe4",
                CubeListBuilder.create().texOffs(45, 1002).mirror()
                        .addBox(-5.0F, -2.0F, -40.0F, 10, 10, 6),
                PartPose.offset(-54.0F, 16.0F, 6.0F));

        partdefinition.addOrReplaceChild("RToe5",
                CubeListBuilder.create().texOffs(0, 1002).mirror()
                        .addBox(-7.0F, -6.0F, -34.0F, 14, 14, 8),
                PartPose.offset(-54.0F, 16.0F, 6.0F));

        partdefinition.addOrReplaceChild("RToe6",
                CubeListBuilder.create().texOffs(92, 955).mirror()
                        .addBox(-8.0F, -8.0F, -26.0F, 16, 16, 36),
                PartPose.offset(-54.0F, 16.0F, 6.0F));

        partdefinition.addOrReplaceChild("RToe7",
                CubeListBuilder.create().texOffs(45, 1002).mirror()
                        .addBox(-5.0F, -2.0F, -40.0F, 10, 10, 6),
                PartPose.offsetAndRotation(-54.0F, 16.0F, 6.0F, 0.0F, 0.7853982F, 0.0F));

        partdefinition.addOrReplaceChild("RToe8",
                CubeListBuilder.create().texOffs(0, 1002).mirror()
                        .addBox(-7.0F, -6.0F, -34.0F, 14, 14, 8),
                PartPose.offsetAndRotation(-54.0F, 16.0F, 6.0F, 0.0F, 0.7853982F, 0.0F));

        partdefinition.addOrReplaceChild("RToe9",
                CubeListBuilder.create().texOffs(0, 955).mirror()
                        .addBox(-8.0F, -8.0F, -26.0F, 16, 16, 30),
                PartPose.offsetAndRotation(-54.0F, 16.0F, 6.0F, 0.0F, 0.7853982F, 0.0F));

        // --- Left leg ---
        partdefinition.addOrReplaceChild("LThigh",
                CubeListBuilder.create().texOffs(192, 350).mirror()
                        .addBox(0.0F, -14.0F, -21.0F, 28, 28, 42),
                PartPose.offsetAndRotation(40.0F, -91.0F, 2.0F, -0.5585054F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("LUpperLeg",
                CubeListBuilder.create().texOffs(152, 420).mirror()
                        .addBox(-16.0F, -52.0F, -16.0F, 32, 52, 32),
                PartPose.offsetAndRotation(56.0F, -36.0F, -5.0F, -0.1745329F, -0.3926991F, -0.0872665F));

        partdefinition.addOrReplaceChild("LLowerLeg",
                CubeListBuilder.create().texOffs(202, 556).mirror()
                        .addBox(-15.0F, -62.0F, -15.0F, 30, 62, 30),
                PartPose.offsetAndRotation(54.0F, 14.0F, 6.0F, 0.1745329F, -0.1308997F, 0.0F));

        // --- Right leg ---
        partdefinition.addOrReplaceChild("RThigh",
                CubeListBuilder.create().texOffs(192, 350).mirror()
                        .addBox(-28.0F, -14.0F, -21.0F, 28, 28, 42),
                PartPose.offsetAndRotation(-40.0F, -91.0F, 2.0F, -0.5585054F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("RLegUpper",
                CubeListBuilder.create().texOffs(152, 420).mirror()
                        .addBox(-16.0F, -52.0F, -16.0F, 32, 52, 32),
                PartPose.offsetAndRotation(-56.0F, -36.0F, -5.0F, -0.1745329F, 0.3926991F, 0.0872665F));

        partdefinition.addOrReplaceChild("RLegLower",
                CubeListBuilder.create().texOffs(200, 646).mirror()
                        .addBox(-15.0F, -62.0F, -15.0F, 30, 62, 30),
                PartPose.offsetAndRotation(-54.0F, 16.0F, 6.0F, 0.1745329F, 0.1308997F, 0.0F));

        // --- Body ---
        partdefinition.addOrReplaceChild("BodyBottom",
                CubeListBuilder.create().texOffs(0, 104).mirror()
                        .addBox(-40.0F, 0.0F, -36.0F, 80, 64, 72),
                PartPose.offsetAndRotation(0.0F, -112.0F, -20.0F, 0.8726646F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("BodyCenter",
                CubeListBuilder.create().texOffs(0, 0).mirror()
                        .addBox(-36.0F, -32.0F, -32.0F, 72, 40, 64),
                PartPose.offsetAndRotation(0.0F, -112.0F, -20.0F, 1.134464F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("BodyTop",
                CubeListBuilder.create().texOffs(0, 0).mirror()
                        .addBox(-36.0F, -32.0F, -32.0F, 72, 40, 64),
                PartPose.offsetAndRotation(0.0F, -126.0F, -50.0F, 1.308997F, 0.0F, 0.0F));

        // --- Neck and head ---
        partdefinition.addOrReplaceChild("Neck",
                CubeListBuilder.create().texOffs(0, 720).mirror()
                        .addBox(-23.0F, -23.0F, -32.0F, 46, 46, 32),
                PartPose.offsetAndRotation(0.0F, -144.0F, -71.0F, -0.0698132F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("Head",
                CubeListBuilder.create().texOffs(0, 808).mirror()
                        .addBox(-17.0F, -18.0F, -40.0F, 34, 36, 40),
                PartPose.offsetAndRotation(0.0F, -156.0F, -98.0F, 0.0872665F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("TopJaw",
                CubeListBuilder.create().texOffs(0, 892).mirror()
                        .addBox(-14.0F, -8.0F, -73.0F, 28, 26, 33),
                PartPose.offsetAndRotation(0.0F, -156.0F, -98.0F, 0.0872665F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("LowerJaw",
                CubeListBuilder.create().texOffs(272, 0).mirror()
                        .addBox(-13.0F, -5.0F, -50.0F, 26, 11, 50),
                PartPose.offsetAndRotation(0.0F, -142.0F, -109.0F, 0.5235988F, 0.0F, 0.0F));

        // --- Tail ---
        partdefinition.addOrReplaceChild("TailBase",
                CubeListBuilder.create().texOffs(0, 240).mirror()
                        .addBox(-32.0F, 0.0F, -29.0F, 64, 40, 58),
                PartPose.offsetAndRotation(0.0F, -73.0F, 26.0F, 0.7853982F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("Tail2",
                CubeListBuilder.create().texOffs(0, 338).mirror()
                        .addBox(-25.0F, 0.0F, -23.0F, 50, 36, 46),
                PartPose.offsetAndRotation(0.0F, -48.0F, 48.0F, 0.6981317F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("Tail3",
                CubeListBuilder.create().texOffs(0, 420).mirror()
                        .addBox(-20.0F, 0.0F, -18.0F, 40, 36, 36),
                PartPose.offsetAndRotation(0.0F, -24.0F, 66.0F, 0.8726646F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("Tail4",
                CubeListBuilder.create().texOffs(0, 492).mirror()
                        .addBox(-16.0F, 0.0F, -14.0F, 32, 42, 28),
                PartPose.offsetAndRotation(0.0F, -3.0F, 87.0F, 1.134464F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("Tail5",
                CubeListBuilder.create().texOffs(0, 556).mirror()
                        .addBox(-13.0F, 0.0F, -11.0F, 26, 42, 22),
                PartPose.offsetAndRotation(0.0F, 12.0F, 116.0F, 1.53589F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("Tail6",
                CubeListBuilder.create().texOffs(0, 614).mirror()
                        .addBox(-10.0F, 0.0F, -9.0F, 20, 32, 18),
                PartPose.offsetAndRotation(0.0F, 14.0F, 154.0F, 1.53589F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("Tail7",
                CubeListBuilder.create().texOffs(0, 658).mirror()
                        .addBox(-8.0F, 0.0F, -7.0F, 16, 22, 14),
                PartPose.offsetAndRotation(0.0F, 16.0F, 185.0F, 1.53589F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("TailTip",
                CubeListBuilder.create().texOffs(0, 694).mirror()
                        .addBox(-6.0F, 0.0F, -5.0F, 12, 21, 10),
                PartPose.offsetAndRotation(0.0F, 18.0F, 203.0F, 1.53589F, 0.0F, 0.0F));

        // --- Right shoulder and arm ---
        partdefinition.addOrReplaceChild("RShoulder",
                CubeListBuilder.create().texOffs(304, 96).mirror()
                        .addBox(-16.0F, -32.0F, -32.0F, 16, 42, 46),
                PartPose.offsetAndRotation(-36.0F, -130.0F, -42.0F, 1.308997F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("RUpperArm",
                CubeListBuilder.create().texOffs(304, 184).mirror()
                        .addBox(-54.0F, -13.0F, -13.0F, 54, 26, 26),
                PartPose.offsetAndRotation(-38.0F, -130.0F, -52.0F, 0.0F, -0.2617994F, -0.3490659F));

        partdefinition.addOrReplaceChild("RLowerArm",
                CubeListBuilder.create().texOffs(245, 240).mirror()
                        .addBox(-48.0F, -11.0F, -11.0F, 48, 22, 22),
                PartPose.offsetAndRotation(-80.0F, -115.0F, -61.0F, 0.0F, -0.7853982F, -0.2617994F));

        partdefinition.addOrReplaceChild("RHand",
                CubeListBuilder.create().texOffs(245, 292).mirror()
                        .addBox(-13.0F, -13.0F, -13.0F, 26, 26, 26),
                PartPose.offsetAndRotation(-115.0F, -100.0F, -99.0F, -1.071467F, 2.007129F, 0.1745329F));

        partdefinition.addOrReplaceChild("RThumbBase",
                CubeListBuilder.create().texOffs(424, 57).mirror()
                        .addBox(2.0F, 1.0F, -32.0F, 8, 8, 20),
                PartPose.offsetAndRotation(-115.0F, -100.0F, -99.0F, 0.0F, -0.1047198F, 0.0F));

        partdefinition.addOrReplaceChild("RThumbTip",
                CubeListBuilder.create().texOffs(422, 18).mirror()
                        .addBox(5.0F, 1.0F, -43.0F, 8, 8, 12),
                PartPose.offset(-115.0F, -100.0F, -99.0F));

        partdefinition.addOrReplaceChild("RIndexBase",
                CubeListBuilder.create().texOffs(424, 57).mirror()
                        .addBox(-4.0F, -9.0F, -34.0F, 8, 8, 20),
                PartPose.offsetAndRotation(-115.0F, -100.0F, -99.0F, -0.2792527F, 0.1570796F, 0.0F));

        partdefinition.addOrReplaceChild("RIndexTip",
                CubeListBuilder.create().texOffs(422, 18).mirror()
                        .addBox(-4.0F, -12.0F, -43.0F, 8, 8, 12),
                PartPose.offsetAndRotation(-115.0F, -100.0F, -99.0F, -0.2094395F, 0.1745329F, 0.0F));

        partdefinition.addOrReplaceChild("R3rdFingerBase",
                CubeListBuilder.create().texOffs(424, 57).mirror()
                        .addBox(-11.0F, -3.0F, -30.0F, 8, 8, 20),
                PartPose.offsetAndRotation(-115.0F, -100.0F, -99.0F, 0.122173F, 0.6457718F, 0.0F));

        partdefinition.addOrReplaceChild("R3rdFingerTip",
                CubeListBuilder.create().texOffs(422, 18).mirror()
                        .addBox(-10.0F, 0.0F, -41.0F, 8, 8, 12),
                PartPose.offsetAndRotation(-115.0F, -100.0F, -99.0F, 0.0F, 0.6806784F, 0.0F));

        // --- Left shoulder and arm ---
        partdefinition.addOrReplaceChild("LShoulder",
                CubeListBuilder.create().texOffs(304, 96).mirror()
                        .addBox(0.0F, -32.0F, -32.0F, 16, 42, 46),
                PartPose.offsetAndRotation(36.0F, -130.0F, -42.0F, 1.308997F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("LUpperArm",
                CubeListBuilder.create().texOffs(304, 184).mirror()
                        .addBox(0.0F, -13.0F, -13.0F, 54, 26, 26),
                PartPose.offsetAndRotation(38.0F, -130.0F, -52.0F, 0.0F, 0.296706F, 0.3490659F));

        partdefinition.addOrReplaceChild("LLowerArm",
                CubeListBuilder.create().texOffs(245, 240).mirror()
                        .addBox(0.0F, -11.0F, -11.0F, 48, 22, 22),
                PartPose.offsetAndRotation(80.0F, -115.0F, -61.0F, 0.0F, 0.7853982F, 0.2617994F));

        partdefinition.addOrReplaceChild("LHand",
                CubeListBuilder.create().texOffs(245, 292).mirror()
                        .addBox(-13.0F, -13.0F, -13.0F, 26, 26, 26),
                PartPose.offsetAndRotation(115.0F, -100.0F, -99.0F, 0.9599311F, 1.308997F, 0.1745329F));

        partdefinition.addOrReplaceChild("LThumbBase",
                CubeListBuilder.create().texOffs(424, 57).mirror()
                        .addBox(-8.0F, -2.0F, -32.0F, 8, 8, 20),
                PartPose.offsetAndRotation(115.0F, -100.0F, -98.0F, 0.1396263F, 0.2617994F, 0.0F));

        partdefinition.addOrReplaceChild("LThumbTip",
                CubeListBuilder.create().texOffs(422, 18).mirror()
                        .addBox(-12.0F, 2.0F, -40.0F, 8, 8, 12),
                PartPose.offsetAndRotation(115.0F, -100.0F, -99.0F, 0.0F, 0.1396263F, 0.0F));

        partdefinition.addOrReplaceChild("LIndexBase",
                CubeListBuilder.create().texOffs(424, 57).mirror()
                        .addBox(-4.0F, -13.0F, -32.0F, 8, 8, 20),
                PartPose.offsetAndRotation(115.0F, -100.0F, -99.0F, -0.1570796F, -0.1396263F, 0.0F));

        partdefinition.addOrReplaceChild("LIndexTip",
                CubeListBuilder.create().texOffs(422, 18).mirror()
                        .addBox(-1.0F, -18.0F, -41.0F, 8, 8, 12),
                PartPose.offsetAndRotation(115.0F, -100.0F, -99.0F, 0.0F, -0.0349066F, 0.0F));

        partdefinition.addOrReplaceChild("L3rdFingerBase",
                CubeListBuilder.create().texOffs(424, 57).mirror()
                        .addBox(4.0F, -5.0F, -33.0F, 8, 8, 20),
                PartPose.offsetAndRotation(115.0F, -100.0F, -99.0F, 0.2617994F, -0.4712389F, 0.0F));

        partdefinition.addOrReplaceChild("L3rdFingerTip",
                CubeListBuilder.create().texOffs(422, 18).mirror()
                        .addBox(9.0F, 2.0F, -42.0F, 8, 8, 12),
                PartPose.offsetAndRotation(115.0F, -100.0F, -99.0F, 0.0349066F, -0.3316126F, 0.0F));

        // --- Back spikes ---
        partdefinition.addOrReplaceChild("Lspikes1",
                CubeListBuilder.create().texOffs(500, 0).mirror()
                        .addBox(0.0F, -10.0F, 0.0F, 0, 10, 11),
                PartPose.offsetAndRotation(5.0F, -168.0F, -86.0F, -0.0872665F, 0.0F, -0.0174533F));

        partdefinition.addOrReplaceChild("Rspikes1",
                CubeListBuilder.create().texOffs(500, 0).mirror()
                        .addBox(0.0F, -10.0F, 0.0F, 0, 10, 11),
                PartPose.offsetAndRotation(-5.0F, -168.0F, -86.0F, -0.0872665F, 0.0F, -0.0174533F));

        partdefinition.addOrReplaceChild("Lspike2",
                CubeListBuilder.create().texOffs(500, 30).mirror()
                        .addBox(0.0F, -25.0F, 0.0F, 0, 25, 21),
                PartPose.offsetAndRotation(10.0F, -162.0F, -63.0F, -0.2617994F, 0.0F, -0.0174533F));

        partdefinition.addOrReplaceChild("Rspike2",
                CubeListBuilder.create().texOffs(500, 30).mirror()
                        .addBox(0.0F, -25.0F, 0.0F, 0, 25, 21),
                PartPose.offsetAndRotation(-10.0F, -162.0F, -63.0F, -0.2617994F, 0.0F, -0.0174533F));

        partdefinition.addOrReplaceChild("Lspike3",
                CubeListBuilder.create().texOffs(500, 80).mirror()
                        .addBox(0.0F, -45.0F, 0.0F, 0, 45, 34),
                PartPose.offsetAndRotation(14.0F, -153.0F, -32.0F, -0.4363323F, 0.0F, -0.0174533F));

        partdefinition.addOrReplaceChild("Rspike3",
                CubeListBuilder.create().texOffs(500, 80).mirror()
                        .addBox(0.0F, -45.0F, 0.0F, 0, 45, 34),
                PartPose.offsetAndRotation(-14.0F, -153.0F, -32.0F, -0.4363323F, 0.0F, -0.0174533F));

        partdefinition.addOrReplaceChild("Lspike4",
                CubeListBuilder.create().texOffs(500, 165).mirror()
                        .addBox(0.0F, -50.0F, 0.0F, 0, 50, 36),
                PartPose.offsetAndRotation(18.0F, -131.0F, 13.0F, -0.715585F, 0.0F, -0.0174533F));

        partdefinition.addOrReplaceChild("Rspike4",
                CubeListBuilder.create().texOffs(500, 165).mirror()
                        .addBox(0.0F, -50.0F, 0.0F, 0, 50, 36),
                PartPose.offsetAndRotation(-18.0F, -131.0F, 13.0F, -0.715585F, 0.0F, -0.0174533F));

        partdefinition.addOrReplaceChild("Lspike5",
                CubeListBuilder.create().texOffs(500, 255).mirror()
                        .addBox(12.0F, -67.0F, 5.0F, 0, 39, 27),
                PartPose.offsetAndRotation(0.0F, -73.0F, 26.0F, -0.7853982F, 0.0F, -0.0174533F));

        partdefinition.addOrReplaceChild("Rspike5",
                CubeListBuilder.create().texOffs(500, 255).mirror()
                        .addBox(-12.0F, -67.0F, 5.0F, 0, 39, 27),
                PartPose.offsetAndRotation(0.0F, -73.0F, 26.0F, -0.7853982F, 0.0F, -0.0174533F));

        partdefinition.addOrReplaceChild("Spike6",
                CubeListBuilder.create().texOffs(500, 325).mirror()
                        .addBox(0.0F, -48.0F, 11.0F, 0, 25, 21),
                PartPose.offsetAndRotation(0.0F, -48.0F, 48.0F, -0.8901179F, 0.0F, -0.0174533F));

        partdefinition.addOrReplaceChild("Spikes7",
                CubeListBuilder.create().texOffs(500, 376).mirror()
                        .addBox(0.0F, -29.0F, 20.0F, 0, 10, 11),
                PartPose.offsetAndRotation(0.0F, -24.0F, 66.0F, -0.7504916F, 0.0F, -0.0174533F));

        return LayerDefinition.create(meshdefinition, 1024, 1024);
    }

    @Override
    public void setupAnim(Godzilla entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float pi4 = 0.7853982F;
        float clawZ = 6.0F;
        float clawY = 16.0F;
        float clawZamp = 35.0F;
        float clawYamp = 18.0F;

        float newangle;
        float newangle2;
        float t1;
        float t2;

        // ---- Left leg walking ----
        if (limbSwingAmount > 0.001F) {
            newangle = Mth.cos(ageInTicks * 0.75F * ANIM_SPEED);
            newangle2 = Mth.cos(ageInTicks * 0.75F * ANIM_SPEED + pi4);
            t1 = Mth.sin(ageInTicks * 0.75F * ANIM_SPEED);
        } else {
            newangle = 0.0F;
            newangle2 = 0.0F;
            t1 = 0.0F;
        }

        if (t1 > 0.0F) {
            t2 = t1 * clawYamp * limbSwingAmount;
            LToe1.y = clawY - t2;
        } else {
            LToe1.y = clawY;
        }
        LToe1.z = clawZ + clawZamp * newangle * limbSwingAmount;

        LToe2.z = LToe3.z = LToe4.z = LToe5.z = LToe6.z = LToe7.z = LToe8.z = LToe9.z = LToe1.z;
        LToe2.y = LToe3.y = LToe4.y = LToe5.y = LToe6.y = LToe7.y = LToe8.y = LToe9.y = LToe1.y;

        LLowerLeg.z = LToe1.z;
        LLowerLeg.y = LToe1.y;
        LLowerLeg.xRot = 0.22F + newangle * (float) Math.PI * 0.09F * limbSwingAmount;

        LUpperLeg.xRot = -0.17F + newangle2 * (float) Math.PI * 0.15F * limbSwingAmount;
        LUpperLeg.y = LLowerLeg.y - Mth.cos(LLowerLeg.xRot) * 55.0F;
        LUpperLeg.z = LLowerLeg.z - Mth.sin(LLowerLeg.xRot) * 55.0F;

        LThigh.xRot = -0.558F + newangle2 * (float) Math.PI * 0.1F * limbSwingAmount;
        LThigh.z = 2.0F + clawZamp * newangle * limbSwingAmount / 4.0F;

        // ---- Right leg walking (phase shifted by PI) ----
        if (limbSwingAmount > 0.001F) {
            newangle = Mth.cos(ageInTicks * 0.75F * ANIM_SPEED + pi4 * 4.0F);
            newangle2 = Mth.cos(ageInTicks * 0.75F * ANIM_SPEED + pi4 * 5.0F);
            t1 = Mth.sin(ageInTicks * 0.75F * ANIM_SPEED + pi4 * 4.0F);
        } else {
            newangle = 0.0F;
            newangle2 = 0.0F;
            t1 = 0.0F;
        }

        if (t1 > 0.0F) {
            t2 = t1 * clawYamp * limbSwingAmount;
            RToe1.y = clawY - t2;
        } else {
            RToe1.y = clawY;
        }
        RToe1.z = clawZ + clawZamp * newangle * limbSwingAmount;

        RToe2.z = RToe3.z = RToe4.z = RToe5.z = RToe6.z = RToe7.z = RToe8.z = RToe9.z = RToe1.z;
        RToe2.y = RToe3.y = RToe4.y = RToe5.y = RToe6.y = RToe7.y = RToe8.y = RToe9.y = RToe1.y;

        RLegLower.z = RToe1.z;
        RLegLower.y = RToe1.y;
        RLegLower.xRot = 0.22F + newangle * (float) Math.PI * 0.09F * limbSwingAmount;

        RLegUpper.xRot = -0.17F + newangle2 * (float) Math.PI * 0.15F * limbSwingAmount;
        RLegUpper.y = RLegLower.y - Mth.cos(RLegLower.xRot) * 55.0F;
        RLegUpper.z = RLegLower.z - Mth.sin(RLegLower.xRot) * 55.0F;

        RThigh.xRot = -0.558F + newangle2 * (float) Math.PI * 0.1F * limbSwingAmount;
        RThigh.z = 2.0F + clawZamp * newangle * limbSwingAmount / 4.0F;

        // Reset all toe rotations
        LToe1.xRot = LToe2.xRot = LToe3.xRot = LToe4.xRot = LToe5.xRot = 0.0F;
        LToe6.xRot = LToe7.xRot = LToe8.xRot = LToe9.xRot = 0.0F;
        RToe1.xRot = RToe2.xRot = RToe3.xRot = RToe4.xRot = RToe5.xRot = 0.0F;
        RToe6.xRot = RToe7.xRot = RToe8.xRot = RToe9.xRot = 0.0F;

        // ---- Tail ----
        boolean attacking = entity.getAttacking() != 0;
        newangle = attacking
                ? Mth.cos(ageInTicks * ANIM_SPEED * 1.75F) * (float) Math.PI * 0.2F
                : Mth.cos(ageInTicks * ANIM_SPEED * 0.75F) * (float) Math.PI * 0.05F;
        doTail(newangle);

        // ---- Head rotation ----
        newangle = (float) Math.toRadians(netHeadYaw) * 0.55F;
        Head.yRot = newangle;
        TopJaw.yRot = newangle;
        LowerJaw.yRot = newangle;
        LowerJaw.z = Head.z - Mth.cos(Head.yRot) * 11.0F;
        LowerJaw.x = Head.x - Mth.sin(Head.yRot) * 11.0F;
        TopJaw.xRot = Head.xRot = (float) Math.toRadians(headPitch);

        // ---- Jaw animation ----
        float jawAngle = attacking
                ? Mth.cos(ageInTicks * ANIM_SPEED * 1.5F) * (float) Math.PI * 0.12F
                : 0.0F;
        LowerJaw.xRot = 0.52F + jawAngle + TopJaw.xRot;

        // ---- Arm animation ----
        float armAngle = attacking
                ? Mth.sin(ageInTicks * ANIM_SPEED * 1.75F) * (float) Math.PI * 0.16F
                : Mth.sin(ageInTicks * ANIM_SPEED * 0.1F) * (float) Math.PI * 0.02F;
        newangle = armAngle;
        newangle2 = armAngle;

        // Left arm chain
        LUpperArm.yRot = 0.65F + newangle;
        LLowerArm.yRot = 0.78F + newangle * 3.0F / 2.0F;
        LLowerArm.z = LUpperArm.z - Mth.sin(LUpperArm.yRot) * 50.0F;
        LLowerArm.x = LUpperArm.x + Mth.cos(LUpperArm.yRot) * 50.0F;
        LLowerArm.y = LUpperArm.y - Mth.sin(LUpperArm.yRot) * 10.0F + 18.0F;

        LHand.z = LLowerArm.z - Mth.sin(LLowerArm.yRot) * 45.0F;
        LHand.x = LLowerArm.x + Mth.cos(LLowerArm.yRot) * 45.0F;
        LHand.y = LLowerArm.y - Mth.sin(LLowerArm.yRot) * 10.0F + 15.0F;

        LThumbBase.z = L3rdFingerBase.z = LIndexBase.z = LHand.z;
        LThumbTip.z = L3rdFingerTip.z = LIndexTip.z = LHand.z;
        LThumbBase.y = L3rdFingerBase.y = LIndexBase.y = LHand.y;
        LThumbTip.y = L3rdFingerTip.y = LIndexTip.y = LHand.y;
        LThumbBase.x = L3rdFingerBase.x = LIndexBase.x = LHand.x;
        LThumbTip.x = L3rdFingerTip.x = LIndexTip.x = LHand.x;

        LHand.yRot = 1.308F + newangle * 2.0F;
        LIndexBase.yRot = -0.139F + newangle * 2.0F;
        LIndexTip.yRot = -0.034F + newangle * 2.0F;
        LThumbBase.yRot = 0.261F + newangle;
        LThumbTip.yRot = 0.139F + newangle;
        L3rdFingerBase.yRot = -0.471F + newangle * 3.0F;
        L3rdFingerTip.yRot = -0.331F + newangle * 3.0F;

        // Right arm chain
        RUpperArm.yRot = -0.65F - newangle2;
        RLowerArm.yRot = -0.78F - newangle2 * 3.0F / 2.0F;
        RLowerArm.z = RUpperArm.z + Mth.sin(RUpperArm.yRot) * 50.0F;
        RLowerArm.x = RUpperArm.x - Mth.cos(RUpperArm.yRot) * 50.0F;
        RLowerArm.y = RUpperArm.y + Mth.sin(RUpperArm.yRot) * 10.0F + 18.0F;

        RHand.z = RLowerArm.z + Mth.sin(RLowerArm.yRot) * 45.0F;
        RHand.x = RLowerArm.x - Mth.cos(RLowerArm.yRot) * 45.0F;
        RHand.y = RLowerArm.y + Mth.sin(RLowerArm.yRot) * 10.0F + 15.0F;

        RThumbBase.z = R3rdFingerBase.z = RIndexBase.z = RHand.z;
        RThumbTip.z = R3rdFingerTip.z = RIndexTip.z = RHand.z;
        RThumbBase.y = R3rdFingerBase.y = RIndexBase.y = RHand.y;
        RThumbTip.y = R3rdFingerTip.y = RIndexTip.y = RHand.y;
        RThumbBase.x = R3rdFingerBase.x = RIndexBase.x = RHand.x;
        RThumbTip.x = R3rdFingerTip.x = RIndexTip.x = RHand.x;

        RHand.yRot = -2.0F - newangle2 * 2.0F;
        RIndexBase.yRot = 0.157F - newangle2 * 2.0F;
        RIndexTip.yRot = 0.174F - newangle2 * 2.0F;
        RThumbBase.yRot = -0.104F - newangle2;
        RThumbTip.yRot = 0.001F - newangle2;
        R3rdFingerTip.yRot = 0.68F - newangle2 * 3.0F;
        R3rdFingerBase.yRot = 0.645F - newangle2 * 3.0F;
    }

    private void doTail(float angle) {
        TailBase.yRot = angle * 0.25F;
        Lspike5.yRot = TailBase.yRot;
        Rspike5.yRot = TailBase.yRot;

        Tail2.yRot = angle * 0.5F;
        Tail2.z = TailBase.z + Mth.cos(TailBase.yRot) * 25.0F;
        Tail2.x = TailBase.x + Mth.sin(TailBase.yRot) * 25.0F;

        Spike6.yRot = Tail2.yRot;
        Spike6.z = Tail2.z;
        Spike6.x = Tail2.x;

        Tail3.yRot = angle * 0.75F;
        Tail3.z = Tail2.z + Mth.cos(Tail2.yRot) * 20.0F;
        Tail3.x = Tail2.x + Mth.sin(Tail2.yRot) * 20.0F;

        Spikes7.yRot = Tail3.yRot;
        Spikes7.z = Tail3.z;
        Spikes7.x = Tail3.x;

        Tail4.yRot = angle * 1.25F;
        Tail4.z = Tail3.z + Mth.cos(Tail3.yRot) * 20.0F;
        Tail4.x = Tail3.x + Mth.sin(Tail3.yRot) * 20.0F;

        Tail5.yRot = angle * 1.5F;
        Tail5.z = Tail4.z + Mth.cos(Tail4.yRot) * 25.0F;
        Tail5.x = Tail4.x + Mth.sin(Tail4.yRot) * 25.0F;

        Tail6.yRot = angle * 1.75F;
        Tail6.z = Tail5.z + Mth.cos(Tail5.yRot) * 27.0F;
        Tail6.x = Tail5.x + Mth.sin(Tail5.yRot) * 27.0F;

        Tail7.yRot = angle * 2.0F;
        Tail7.z = Tail6.z + Mth.cos(Tail6.yRot) * 28.0F;
        Tail7.x = Tail6.x + Mth.sin(Tail6.yRot) * 28.0F;

        TailTip.yRot = angle * 2.25F;
        TailTip.z = Tail7.z + Mth.cos(Tail7.yRot) * 18.0F;
        TailTip.x = Tail7.x + Mth.sin(Tail7.yRot) * 18.0F;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        LToe1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        LToe2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        LToe3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        LToe4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        LToe5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        LToe6.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        LToe7.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        LToe8.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        LToe9.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        RToe1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        RToe2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        RToe3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        RToe4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        RToe5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        RToe6.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        RToe7.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        RToe8.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        RToe9.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        LThigh.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        LUpperLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        LLowerLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        RThigh.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        RLegUpper.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        RLegLower.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        BodyBottom.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        BodyCenter.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        BodyTop.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Neck.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Head.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        TopJaw.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        LowerJaw.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        TailBase.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tail2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tail3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tail4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tail5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tail6.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tail7.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        TailTip.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        RShoulder.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        RUpperArm.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        RLowerArm.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        RHand.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        RThumbBase.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        RThumbTip.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        RIndexBase.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        RIndexTip.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        R3rdFingerBase.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        R3rdFingerTip.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        LShoulder.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        LUpperArm.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        LLowerArm.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        LHand.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        LThumbBase.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        LThumbTip.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        LIndexBase.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        LIndexTip.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        L3rdFingerBase.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        L3rdFingerTip.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Lspikes1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Rspikes1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Lspike2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Rspike2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Lspike3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Rspike3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Lspike4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Rspike4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Lspike5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Rspike5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Spike6.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Spikes7.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
