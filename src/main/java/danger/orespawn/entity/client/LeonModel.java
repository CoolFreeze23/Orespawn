package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.EntityLeon;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class LeonModel extends EntityModel<EntityLeon> {
    private float rf1;
    private final ModelPart chest;
    private final ModelPart neck_1;
    private final ModelPart neck_2;
    private final ModelPart neck_3;
    private final ModelPart abdomen;
    private final ModelPart head;
    private final ModelPart upper_jaw;
    private final ModelPart bottom_jaw;
    private final ModelPart chest_ridge;
    private final ModelPart upper_sail_1;
    private final ModelPart upper_sail2_;
    private final ModelPart upper_sail3;
    private final ModelPart lower_sail1;
    private final ModelPart lower_sail2;
    private final ModelPart lower_sail_3;
    private final ModelPart eye_ridge_L;
    private final ModelPart eye_ridge_R;
    private final ModelPart anntena_1_L;
    private final ModelPart anntena_1_R;
    private final ModelPart anntena_2_L;
    private final ModelPart anntena_2_R;
    private final ModelPart arm_1_L;
    private final ModelPart arm_2_L;
    private final ModelPart wing_1_L;
    private final ModelPart wing_2_L;
    private final ModelPart arm_1_R;
    private final ModelPart arm_2_R;
    private final ModelPart wing_1_R;
    private final ModelPart wing_2_R;
    private final ModelPart leg_1_L;
    private final ModelPart leg_1_R;
    private final ModelPart leg_2_L;
    private final ModelPart leg_2_R;
    private final ModelPart footL;
    private final ModelPart footR;
    private final ModelPart wing_3_L;
    private final ModelPart wing_3_R;
    private final ModelPart wing_4_L;
    private final ModelPart wing_4_R;
    private final ModelPart claw_L;
    private final ModelPart claw_R;
    private final ModelPart claw_L2;
    private final ModelPart claw_R_2;
    private final ModelPart wing_5_L;
    private final ModelPart wing_6_L;
    private final ModelPart wing_7_L;
    private final ModelPart wing_5_R;
    private final ModelPart wing_6_R;
    private final ModelPart wing_7_R;
    private final ModelPart fchest;
    private final ModelPart fneck_1;
    private final ModelPart fneck_2;
    private final ModelPart fneck_3;
    private final ModelPart fabdomen;
    private final ModelPart fhead;
    private final ModelPart fupper_jaw;
    private final ModelPart fbottom_jaw;
    private final ModelPart fchest_ridge;
    private final ModelPart fupper_sail_1;
    private final ModelPart fupper_sail2_;
    private final ModelPart fupper_sail3;
    private final ModelPart flower_sail1;
    private final ModelPart flower_sail2;
    private final ModelPart flower_sail_3;
    private final ModelPart feye_ridge_L;
    private final ModelPart feye_ridge_R;
    private final ModelPart fanntena_1_L;
    private final ModelPart fanntena_1_R;
    private final ModelPart fanntena_2_L;
    private final ModelPart fanntena_2_R;
    private final ModelPart farm_1_L;
    private final ModelPart farm_2_L;
    private final ModelPart fwing_1_L;
    private final ModelPart fwing_2_L;
    private final ModelPart farm_1_R;
    private final ModelPart farm_2_R;
    private final ModelPart fwing_1_R;
    private final ModelPart fwing_2_R;
    private final ModelPart fleg_1_L;
    private final ModelPart fleg_1_R;
    private final ModelPart fleg_2_L;
    private final ModelPart fleg_2_R;
    private final ModelPart ffootL;
    private final ModelPart ffootR;
    private final ModelPart fwing_3_L;
    private final ModelPart fwing_3_R;
    private final ModelPart fwing_4_L;
    private final ModelPart fwing_4_R;
    private final ModelPart fclaw_L;
    private final ModelPart fclaw_R;
    private final ModelPart fclaw_L2;
    private final ModelPart fclaw_R_2;
    private final ModelPart fwing_5_L;
    private final ModelPart fwing_6_L;
    private final ModelPart fwing_7_L;
    private final ModelPart fwing_5_R;
    private final ModelPart fwing_6_R;
    private final ModelPart fwing_7_R;

    public LeonModel(ModelPart root) {
        this.chest = root.getChild("chest");
        this.neck_1 = root.getChild("neck_1");
        this.neck_2 = root.getChild("neck_2");
        this.neck_3 = root.getChild("neck_3");
        this.abdomen = root.getChild("abdomen");
        this.head = root.getChild("head");
        this.upper_jaw = root.getChild("upper_jaw");
        this.bottom_jaw = root.getChild("bottom_jaw");
        this.chest_ridge = root.getChild("chest_ridge");
        this.upper_sail_1 = root.getChild("upper_sail_1");
        this.upper_sail2_ = root.getChild("upper_sail2_");
        this.upper_sail3 = root.getChild("upper_sail3");
        this.lower_sail1 = root.getChild("lower_sail1");
        this.lower_sail2 = root.getChild("lower_sail2");
        this.lower_sail_3 = root.getChild("lower_sail_3");
        this.eye_ridge_L = root.getChild("eye_ridge_L");
        this.eye_ridge_R = root.getChild("eye_ridge_R");
        this.anntena_1_L = root.getChild("anntena_1_L");
        this.anntena_1_R = root.getChild("anntena_1_R");
        this.anntena_2_L = root.getChild("anntena_2_L");
        this.anntena_2_R = root.getChild("anntena_2_R");
        this.arm_1_L = root.getChild("arm_1_L");
        this.arm_2_L = root.getChild("arm_2_L");
        this.wing_1_L = root.getChild("wing_1_L");
        this.wing_2_L = root.getChild("wing_2_L");
        this.arm_1_R = root.getChild("arm_1_R");
        this.arm_2_R = root.getChild("arm_2_R");
        this.wing_1_R = root.getChild("wing_1_R");
        this.wing_2_R = root.getChild("wing_2_R");
        this.leg_1_L = root.getChild("leg_1_L");
        this.leg_1_R = root.getChild("leg_1_R");
        this.leg_2_L = root.getChild("leg_2_L");
        this.leg_2_R = root.getChild("leg_2_R");
        this.footL = root.getChild("footL");
        this.footR = root.getChild("footR");
        this.wing_3_L = root.getChild("wing_3_L");
        this.wing_3_R = root.getChild("wing_3_R");
        this.wing_4_L = root.getChild("wing_4_L");
        this.wing_4_R = root.getChild("wing_4_R");
        this.claw_L = root.getChild("claw_L");
        this.claw_R = root.getChild("claw_R");
        this.claw_L2 = root.getChild("claw_L2");
        this.claw_R_2 = root.getChild("claw_R_2");
        this.wing_5_L = root.getChild("wing_5_L");
        this.wing_6_L = root.getChild("wing_6_L");
        this.wing_7_L = root.getChild("wing_7_L");
        this.wing_5_R = root.getChild("wing_5_R");
        this.wing_6_R = root.getChild("wing_6_R");
        this.wing_7_R = root.getChild("wing_7_R");
        this.fchest = root.getChild("fchest");
        this.fneck_1 = root.getChild("fneck_1");
        this.fneck_2 = root.getChild("fneck_2");
        this.fneck_3 = root.getChild("fneck_3");
        this.fabdomen = root.getChild("fabdomen");
        this.fhead = root.getChild("fhead");
        this.fupper_jaw = root.getChild("fupper_jaw");
        this.fbottom_jaw = root.getChild("fbottom_jaw");
        this.fchest_ridge = root.getChild("fchest_ridge");
        this.fupper_sail_1 = root.getChild("fupper_sail_1");
        this.fupper_sail2_ = root.getChild("fupper_sail2_");
        this.fupper_sail3 = root.getChild("fupper_sail3");
        this.flower_sail1 = root.getChild("flower_sail1");
        this.flower_sail2 = root.getChild("flower_sail2");
        this.flower_sail_3 = root.getChild("flower_sail_3");
        this.feye_ridge_L = root.getChild("feye_ridge_L");
        this.feye_ridge_R = root.getChild("feye_ridge_R");
        this.fanntena_1_L = root.getChild("fanntena_1_L");
        this.fanntena_1_R = root.getChild("fanntena_1_R");
        this.fanntena_2_L = root.getChild("fanntena_2_L");
        this.fanntena_2_R = root.getChild("fanntena_2_R");
        this.farm_1_L = root.getChild("farm_1_L");
        this.farm_2_L = root.getChild("farm_2_L");
        this.fwing_1_L = root.getChild("fwing_1_L");
        this.fwing_2_L = root.getChild("fwing_2_L");
        this.farm_1_R = root.getChild("farm_1_R");
        this.farm_2_R = root.getChild("farm_2_R");
        this.fwing_1_R = root.getChild("fwing_1_R");
        this.fwing_2_R = root.getChild("fwing_2_R");
        this.fleg_1_L = root.getChild("fleg_1_L");
        this.fleg_1_R = root.getChild("fleg_1_R");
        this.fleg_2_L = root.getChild("fleg_2_L");
        this.fleg_2_R = root.getChild("fleg_2_R");
        this.ffootL = root.getChild("ffootL");
        this.ffootR = root.getChild("ffootR");
        this.fwing_3_L = root.getChild("fwing_3_L");
        this.fwing_3_R = root.getChild("fwing_3_R");
        this.fwing_4_L = root.getChild("fwing_4_L");
        this.fwing_4_R = root.getChild("fwing_4_R");
        this.fclaw_L = root.getChild("fclaw_L");
        this.fclaw_R = root.getChild("fclaw_R");
        this.fclaw_L2 = root.getChild("fclaw_L2");
        this.fclaw_R_2 = root.getChild("fclaw_R_2");
        this.fwing_5_L = root.getChild("fwing_5_L");
        this.fwing_6_L = root.getChild("fwing_6_L");
        this.fwing_7_L = root.getChild("fwing_7_L");
        this.fwing_5_R = root.getChild("fwing_5_R");
        this.fwing_6_R = root.getChild("fwing_6_R");
        this.fwing_7_R = root.getChild("fwing_7_R");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("chest",
                CubeListBuilder.create().texOffs(80, 0).mirror()
                        .addBox(-8.0F, -9.5F, -9.5F, 16, 19, 19),
                PartPose.offsetAndRotation(0.0F, -2.0F, -7.0F, -0.4363323F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("neck_1",
                CubeListBuilder.create().texOffs(106, 68).mirror()
                        .addBox(-5.5F, -7.0F, -9.0F, 11, 14, 11),
                PartPose.offsetAndRotation(0.0F, -6.0F, -13.0F, -0.8726646F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("neck_2",
                CubeListBuilder.create().texOffs(71, 69).mirror()
                        .addBox(-4.0F, -5.0F, -8.0F, 8, 10, 9),
                PartPose.offsetAndRotation(0.0F, -12.0F, -17.0F, -1.064651F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("neck_3",
                CubeListBuilder.create().texOffs(102, 94).mirror()
                        .addBox(-3.0F, -4.0F, -17.0F, 6, 8, 18),
                PartPose.offsetAndRotation(0.0F, -19.0F, -21.0F, -1.029744F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("abdomen",
                CubeListBuilder.create().texOffs(96, 39).mirror()
                        .addBox(-5.0F, -2.0F, 1.0F, 10, 11, 17),
                PartPose.offsetAndRotation(0.0F, -5.0F, 4.0F, -0.6457718F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(61, 49).mirror()
                        .addBox(-4.0F, -2.0F, -4.0F, 8, 8, 9),
                PartPose.offsetAndRotation(0.0F, -32.0F, -29.0F, -1.413717F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("upper_jaw",
                CubeListBuilder.create().texOffs(83, 89).mirror()
                        .addBox(-3.0F, 4.0F, -4.0F, 6, 13, 5),
                PartPose.offsetAndRotation(0.0F, -32.0F, -29.0F, -1.37881F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("bottom_jaw",
                CubeListBuilder.create().texOffs(85, 108).mirror()
                        .addBox(-2.5F, -1.0F, -1.5F, 5, 12, 3),
                PartPose.offsetAndRotation(0.0F, -28.0F, -34.0F, -1.413717F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("chest_ridge",
                CubeListBuilder.create().texOffs(113, 129).mirror()
                        .addBox(-2.0F, 7.0F, -3.0F, 4, 3, 17),
                PartPose.offsetAndRotation(0.0F, -2.0F, -7.0F, -0.6283185F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("upper_sail_1",
                CubeListBuilder.create().texOffs(76, 110).mirror()
                        .addBox(-1.0F, -17.0F, -16.0F, 2, 14, 2),
                PartPose.offsetAndRotation(0.0F, -32.0F, -29.0F, 0.2443461F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("upper_sail2_",
                CubeListBuilder.create().texOffs(63, 110).mirror()
                        .addBox(-0.5F, -15.0F, -16.0F, 1, 12, 5),
                PartPose.offsetAndRotation(0.0F, -32.0F, -29.0F, 0.1396263F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("upper_sail3",
                CubeListBuilder.create().texOffs(0, 82).mirror()
                        .addBox(0.0F, -1.5F, -18.0F, 0, 9, 13),
                PartPose.offsetAndRotation(0.0F, -32.0F, -29.0F, -0.7504916F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("lower_sail1",
                CubeListBuilder.create().texOffs(0, 2).mirror()
                        .addBox(-1.0F, 0.0F, -10.0F, 2, 11, 2),
                PartPose.offsetAndRotation(0.0F, -28.0F, -34.0F, 0.1919862F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("lower_sail2",
                CubeListBuilder.create().texOffs(52, 94).mirror()
                        .addBox(-0.5F, 0.5F, -9.0F, 1, 9, 4),
                PartPose.offsetAndRotation(0.0F, -28.0F, -34.0F, 0.296706F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("lower_sail_3",
                CubeListBuilder.create().texOffs(66, 90).mirror()
                        .addBox(0.0F, 1.5F, -4.0F, 0, 9, 7),
                PartPose.offsetAndRotation(0.0F, -28.0F, -34.0F, -0.4886922F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("eye_ridge_L",
                CubeListBuilder.create().texOffs(0, 68).mirror()
                        .addBox(0.0F, -4.0F, -5.0F, 5, 2, 11),
                PartPose.offsetAndRotation(0.0F, -32.0F, -29.0F, 0.2094395F, 0.5585054F, 0.2268928F));

        partdefinition.addOrReplaceChild("eye_ridge_R",
                CubeListBuilder.create().texOffs(0, 68).mirror()
                        .addBox(-5.0F, -4.0F, -5.0F, 5, 2, 11),
                PartPose.offsetAndRotation(0.0F, -32.0F, -29.0F, 0.2094395F, -0.5585054F, -0.2268928F));

        partdefinition.addOrReplaceChild("anntena_1_L",
                CubeListBuilder.create().texOffs(0, 40).mirror()
                        .addBox(3.0F, -4.2F, 5.0F, 2, 2, 5),
                PartPose.offsetAndRotation(0.0F, -32.0F, -29.0F, 0.2443461F, 0.3665191F, 0.2268928F));

        partdefinition.addOrReplaceChild("anntena_1_R",
                CubeListBuilder.create().texOffs(0, 40).mirror()
                        .addBox(-5.0F, -4.2F, 5.0F, 2, 2, 5),
                PartPose.offsetAndRotation(0.0F, -32.0F, -29.0F, 0.2443461F, -0.3665191F, -0.2268928F));

        partdefinition.addOrReplaceChild("anntena_2_L",
                CubeListBuilder.create().texOffs(46, 91).mirror()
                        .addBox(5.0F, -6.0F, 7.0F, 1, 1, 17),
                PartPose.offsetAndRotation(0.0F, -32.0F, -29.0F, 0.0698132F, 0.1396263F, 0.2268928F));

        partdefinition.addOrReplaceChild("anntena_2_R",
                CubeListBuilder.create().texOffs(46, 91).mirror()
                        .addBox(-6.0F, -6.0F, 7.0F, 1, 1, 17),
                PartPose.offsetAndRotation(0.0F, -32.0F, -29.0F, 0.0698132F, -0.1396263F, -0.2268928F));

        partdefinition.addOrReplaceChild("arm_1_L",
                CubeListBuilder.create().texOffs(77, 150).mirror()
                        .addBox(0.0F, -1.0F, -1.0F, 3, 18, 5),
                PartPose.offsetAndRotation(8.0F, -8.0F, -10.0F, -0.0698132F, 0.0F, -0.7679449F));

        partdefinition.addOrReplaceChild("arm_2_L",
                CubeListBuilder.create().texOffs(102, 150).mirror()
                        .addBox(-0.5F, 0.0F, -1.0F, 2, 24, 3),
                PartPose.offsetAndRotation(20.0F, 3.0F, -10.0F, -0.4712389F, 0.0F, -0.4886922F));

        partdefinition.addOrReplaceChild("wing_1_L",
                CubeListBuilder.create().texOffs(0, 33).mirror()
                        .addBox(1.5F, -1.0F, 3.0F, 1, 19, 15),
                PartPose.offsetAndRotation(8.0F, -8.0F, -10.0F, -0.1745329F, -0.1919862F, -0.7504916F));

        partdefinition.addOrReplaceChild("wing_2_L",
                CubeListBuilder.create().texOffs(33, 50).mirror()
                        .addBox(0.0F, -1.0F, 1.0F, 1, 23, 17),
                PartPose.offsetAndRotation(20.0F, 3.0F, -10.0F, -0.5235988F, -0.0349066F, -0.4712389F));

        partdefinition.addOrReplaceChild("arm_1_R",
                CubeListBuilder.create().texOffs(77, 127).mirror()
                        .addBox(-3.0F, -1.0F, -1.0F, 3, 18, 5),
                PartPose.offsetAndRotation(-8.0F, -8.0F, -10.0F, -0.0698132F, 0.0F, 0.7679449F));

        partdefinition.addOrReplaceChild("arm_2_R",
                CubeListBuilder.create().texOffs(102, 123).mirror()
                        .addBox(-1.5F, 0.0F, -1.0F, 2, 24, 3),
                PartPose.offsetAndRotation(-20.0F, 3.0F, -10.0F, -0.4712389F, 0.0F, 0.4886922F));

        partdefinition.addOrReplaceChild("wing_1_R",
                CubeListBuilder.create().texOffs(24, 150).mirror()
                        .addBox(-2.5F, -1.0F, 3.0F, 1, 19, 15),
                PartPose.offsetAndRotation(-8.0F, -8.0F, -10.0F, -0.1745329F, 0.1919862F, 0.7504916F));

        partdefinition.addOrReplaceChild("wing_2_R",
                CubeListBuilder.create().texOffs(150, 50).mirror()
                        .addBox(-1.0F, -1.0F, 0.0F, 1, 23, 17),
                PartPose.offsetAndRotation(-20.0F, 3.0F, -10.0F, -0.5235988F, 0.0349066F, 0.4712389F));

        partdefinition.addOrReplaceChild("leg_1_L",
                CubeListBuilder.create().texOffs(0, 104).mirror()
                        .addBox(0.0F, -3.0F, -4.0F, 3, 15, 7),
                PartPose.offsetAndRotation(5.0F, 5.0F, 10.0F, -0.6108652F, 0.0F, -0.3316126F));

        partdefinition.addOrReplaceChild("leg_1_R",
                CubeListBuilder.create().texOffs(0, 149).mirror()
                        .addBox(-3.0F, -3.0F, -4.0F, 3, 15, 7),
                PartPose.offsetAndRotation(-6.0F, 5.0F, 10.0F, -0.6108652F, 0.0F, 0.3316126F));

        partdefinition.addOrReplaceChild("leg_2_L",
                CubeListBuilder.create().texOffs(21, 108).mirror()
                        .addBox(1.0F, 0.0F, -3.0F, 2, 14, 4),
                PartPose.offsetAndRotation(8.0F, 13.0F, 6.0F, 0.6108652F, 0.0F, -0.1745329F));

        partdefinition.addOrReplaceChild("leg_2_R",
                CubeListBuilder.create().texOffs(21, 108).mirror()
                        .addBox(-2.0F, 0.0F, -3.0F, 2, 14, 4),
                PartPose.offsetAndRotation(-10.0F, 13.0F, 6.0F, 0.6108652F, 0.0F, 0.1745329F));

        partdefinition.addOrReplaceChild("footL",
                CubeListBuilder.create().texOffs(50, 29).mirror()
                        .addBox(-2.0F, -1.0F, -8.0F, 4, 2, 9),
                PartPose.offset(12.0F, 24.0F, 11.0F));

        partdefinition.addOrReplaceChild("footR",
                CubeListBuilder.create().texOffs(50, 29).mirror()
                        .addBox(-1.0F, 1.0F, -8.0F, 4, 2, 9),
                PartPose.offset(-14.0F, 22.0F, 11.0F));

        partdefinition.addOrReplaceChild("wing_3_L",
                CubeListBuilder.create().texOffs(0, 0).mirror()
                        .addBox(-7.5F, 0.0F, -5.0F, 16, 1, 26),
                PartPose.offsetAndRotation(-5.0F, 0.0F, 12.0F, -0.4886922F, -0.5235988F, 0.4014257F));

        partdefinition.addOrReplaceChild("wing_3_R",
                CubeListBuilder.create().texOffs(150, 0).mirror()
                        .addBox(-8.5F, 0.0F, -5.0F, 16, 1, 26),
                PartPose.offsetAndRotation(4.0F, 0.0F, 12.0F, -0.4886922F, 0.5235988F, -0.4014257F));

        partdefinition.addOrReplaceChild("wing_4_L",
                CubeListBuilder.create().texOffs(8, 117).mirror()
                        .addBox(-1.5F, -0.5F, -2.0F, 3, 1, 31),
                PartPose.offsetAndRotation(6.0F, 6.0F, 24.0F, -0.6283185F, -0.0174533F, 0.0F));

        partdefinition.addOrReplaceChild("wing_4_R",
                CubeListBuilder.create().texOffs(8, 117).mirror()
                        .addBox(-1.5F, -0.5F, -2.0F, 3, 1, 31),
                PartPose.offsetAndRotation(-7.0F, 6.0F, 24.0F, -0.6283185F, 0.0174533F, 0.0F));

        partdefinition.addOrReplaceChild("claw_L",
                CubeListBuilder.create().texOffs(0, 129).mirror()
                        .addBox(0.0F, -1.0F, -9.0F, 1, 2, 10),
                PartPose.offsetAndRotation(30.0F, 23.0F, -20.0F, 0.0F, 0.1570796F, 0.0F));

        partdefinition.addOrReplaceChild("claw_R",
                CubeListBuilder.create().texOffs(0, 129).mirror()
                        .addBox(0.0F, -1.0F, -9.0F, 1, 2, 10),
                PartPose.offsetAndRotation(-31.0F, 23.0F, -20.0F, 0.0F, -0.1570796F, 0.0F));

        partdefinition.addOrReplaceChild("claw_L2",
                CubeListBuilder.create().texOffs(18, 38).mirror()
                        .addBox(0.0F, -2.5F, -6.0F, 1, 2, 7),
                PartPose.offsetAndRotation(-30.0F, 23.0F, -28.0F, 0.5061455F, -0.2792527F, 0.0F));

        partdefinition.addOrReplaceChild("claw_R_2",
                CubeListBuilder.create().texOffs(18, 38).mirror()
                        .addBox(-1.0F, -2.5F, -6.0F, 1, 2, 7),
                PartPose.offsetAndRotation(30.0F, 23.0F, -28.0F, 0.5061455F, 0.2792527F, 0.0F));

        partdefinition.addOrReplaceChild("wing_5_L",
                CubeListBuilder.create().texOffs(46, 10).mirror()
                        .addBox(-1.0F, -3.0F, -1.0F, 1, 8, 31),
                PartPose.offsetAndRotation(31.0F, 21.0F, -19.0F, 0.6806784F, 0.0523599F, -0.2792527F));

        partdefinition.addOrReplaceChild("wing_6_L",
                CubeListBuilder.create().texOffs(46, 10).mirror()
                        .addBox(-1.0F, -3.0F, -1.0F, 1, 8, 31),
                PartPose.offsetAndRotation(31.0F, 21.0F, -19.0F, 0.4537856F, 0.2443461F, -0.3665191F));

        partdefinition.addOrReplaceChild("wing_7_L",
                CubeListBuilder.create().texOffs(46, 10).mirror()
                        .addBox(-1.0F, -3.0F, -1.0F, 1, 8, 31),
                PartPose.offsetAndRotation(-30.0F, 21.0F, -19.0F, 0.1396263F, -0.3316126F, 0.4014257F));

        partdefinition.addOrReplaceChild("wing_5_R",
                CubeListBuilder.create().texOffs(46, 10).mirror()
                        .addBox(-1.0F, -3.0F, -1.0F, 1, 8, 31),
                PartPose.offsetAndRotation(-30.0F, 21.0F, -19.0F, 0.6806784F, -0.0523599F, 0.2792527F));

        partdefinition.addOrReplaceChild("wing_6_R",
                CubeListBuilder.create().texOffs(46, 10).mirror()
                        .addBox(-1.0F, -3.0F, -1.0F, 1, 8, 31),
                PartPose.offsetAndRotation(-30.0F, 21.0F, -19.0F, 0.4537856F, -0.2443461F, 0.3665191F));

        partdefinition.addOrReplaceChild("wing_7_R",
                CubeListBuilder.create().texOffs(46, 10).mirror()
                        .addBox(-1.0F, -3.0F, -1.0F, 1, 8, 31),
                PartPose.offsetAndRotation(31.0F, 21.0F, -19.0F, 0.1396263F, 0.3316126F, -0.4014257F));

        partdefinition.addOrReplaceChild("fchest",
                CubeListBuilder.create().texOffs(80, 0).mirror()
                        .addBox(-8.0F, -9.5F, -9.5F, 16, 19, 19),
                PartPose.offsetAndRotation(0.0F, -2.0F, -7.0F, -0.4363323F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("fneck_1",
                CubeListBuilder.create().texOffs(106, 68).mirror()
                        .addBox(-5.5F, -7.0F, -9.0F, 11, 14, 11),
                PartPose.offsetAndRotation(0.0F, -6.0F, -13.0F, -0.8726646F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("fneck_2",
                CubeListBuilder.create().texOffs(71, 69).mirror()
                        .addBox(-4.0F, -5.0F, -8.0F, 8, 10, 9),
                PartPose.offsetAndRotation(0.0F, -12.0F, -17.0F, -1.064651F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("fneck_3",
                CubeListBuilder.create().texOffs(102, 94).mirror()
                        .addBox(-3.0F, -4.0F, -17.0F, 6, 8, 18),
                PartPose.offsetAndRotation(0.0F, -19.0F, -21.0F, -1.029744F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("fabdomen",
                CubeListBuilder.create().texOffs(96, 39).mirror()
                        .addBox(-5.0F, -2.0F, 1.0F, 10, 11, 17),
                PartPose.offsetAndRotation(0.0F, -5.0F, 4.0F, -0.6457718F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("fhead",
                CubeListBuilder.create().texOffs(61, 49).mirror()
                        .addBox(-4.0F, -2.0F, -4.0F, 8, 8, 9),
                PartPose.offsetAndRotation(0.0F, -32.0F, -29.0F, -1.413717F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("fupper_jaw",
                CubeListBuilder.create().texOffs(83, 89).mirror()
                        .addBox(-3.0F, 4.0F, -4.0F, 6, 13, 5),
                PartPose.offsetAndRotation(0.0F, -32.0F, -29.0F, -1.37881F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("fbottom_jaw",
                CubeListBuilder.create().texOffs(85, 108).mirror()
                        .addBox(-2.5F, -1.0F, -1.5F, 5, 12, 3),
                PartPose.offsetAndRotation(0.0F, -28.0F, -34.0F, -1.413717F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("fchest_ridge",
                CubeListBuilder.create().texOffs(113, 129).mirror()
                        .addBox(-2.0F, 7.0F, -3.0F, 4, 3, 17),
                PartPose.offsetAndRotation(0.0F, -2.0F, -7.0F, -0.6283185F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("fupper_sail_1",
                CubeListBuilder.create().texOffs(76, 110).mirror()
                        .addBox(-1.0F, -17.0F, -16.0F, 2, 14, 2),
                PartPose.offsetAndRotation(0.0F, -32.0F, -29.0F, 0.2443461F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("fupper_sail2_",
                CubeListBuilder.create().texOffs(63, 110).mirror()
                        .addBox(-0.5F, -15.0F, -16.0F, 1, 12, 5),
                PartPose.offsetAndRotation(0.0F, -32.0F, -29.0F, 0.1396263F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("fupper_sail3",
                CubeListBuilder.create().texOffs(0, 82).mirror()
                        .addBox(0.0F, -1.5F, -18.0F, 0, 9, 13),
                PartPose.offsetAndRotation(0.0F, -32.0F, -29.0F, -0.7504916F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("flower_sail1",
                CubeListBuilder.create().texOffs(0, 2).mirror()
                        .addBox(-1.0F, 0.0F, -10.0F, 2, 11, 2),
                PartPose.offsetAndRotation(0.0F, -28.0F, -34.0F, 0.1919862F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("flower_sail2",
                CubeListBuilder.create().texOffs(52, 94).mirror()
                        .addBox(-0.5F, 0.5F, -9.0F, 1, 9, 4),
                PartPose.offsetAndRotation(0.0F, -28.0F, -34.0F, 0.296706F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("flower_sail_3",
                CubeListBuilder.create().texOffs(66, 90).mirror()
                        .addBox(0.0F, 1.5F, -4.0F, 0, 9, 7),
                PartPose.offsetAndRotation(0.0F, -28.0F, -34.0F, -0.4886922F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("feye_ridge_L",
                CubeListBuilder.create().texOffs(0, 68).mirror()
                        .addBox(0.0F, -4.0F, -5.0F, 5, 2, 11),
                PartPose.offsetAndRotation(0.0F, -32.0F, -29.0F, 0.2094395F, 0.5585054F, 0.2268928F));

        partdefinition.addOrReplaceChild("feye_ridge_R",
                CubeListBuilder.create().texOffs(0, 68).mirror()
                        .addBox(-5.0F, -4.0F, -5.0F, 5, 2, 11),
                PartPose.offsetAndRotation(0.0F, -32.0F, -29.0F, 0.2094395F, -0.5585054F, -0.2268928F));

        partdefinition.addOrReplaceChild("fanntena_1_L",
                CubeListBuilder.create().texOffs(0, 40).mirror()
                        .addBox(3.0F, -4.2F, 5.0F, 2, 2, 5),
                PartPose.offsetAndRotation(0.0F, -32.0F, -29.0F, 0.2443461F, 0.3665191F, 0.2268928F));

        partdefinition.addOrReplaceChild("fanntena_1_R",
                CubeListBuilder.create().texOffs(0, 40).mirror()
                        .addBox(-5.0F, -4.2F, 5.0F, 2, 2, 5),
                PartPose.offsetAndRotation(0.0F, -32.0F, -29.0F, 0.2443461F, -0.3665191F, -0.2268928F));

        partdefinition.addOrReplaceChild("fanntena_2_L",
                CubeListBuilder.create().texOffs(46, 91).mirror()
                        .addBox(5.0F, -6.0F, 7.0F, 1, 1, 17),
                PartPose.offsetAndRotation(0.0F, -32.0F, -29.0F, 0.0698132F, 0.1396263F, 0.2268928F));

        partdefinition.addOrReplaceChild("fanntena_2_R",
                CubeListBuilder.create().texOffs(46, 91).mirror()
                        .addBox(-6.0F, -6.0F, 7.0F, 1, 1, 17),
                PartPose.offsetAndRotation(0.0F, -32.0F, -29.0F, 0.0698132F, -0.1396263F, -0.2268928F));

        partdefinition.addOrReplaceChild("farm_1_L",
                CubeListBuilder.create().texOffs(77, 150).mirror()
                        .addBox(0.0F, -1.0F, -1.0F, 3, 18, 5),
                PartPose.offsetAndRotation(8.0F, -8.0F, -10.0F, -0.0698132F, 0.0F, -0.7679449F));

        partdefinition.addOrReplaceChild("farm_2_L",
                CubeListBuilder.create().texOffs(102, 150).mirror()
                        .addBox(-0.5F, 0.0F, -1.0F, 2, 24, 3),
                PartPose.offsetAndRotation(20.0F, 3.0F, -10.0F, -0.4712389F, 0.0F, -0.4886922F));

        partdefinition.addOrReplaceChild("fwing_1_L",
                CubeListBuilder.create().texOffs(0, 33).mirror()
                        .addBox(1.5F, -1.0F, 3.0F, 1, 19, 15),
                PartPose.offsetAndRotation(8.0F, -8.0F, -10.0F, -0.1745329F, -0.1919862F, -0.7504916F));

        partdefinition.addOrReplaceChild("fwing_2_L",
                CubeListBuilder.create().texOffs(33, 50).mirror()
                        .addBox(0.0F, -1.0F, 1.0F, 1, 23, 17),
                PartPose.offsetAndRotation(20.0F, 3.0F, -10.0F, -0.5235988F, -0.0349066F, -0.4712389F));

        partdefinition.addOrReplaceChild("farm_1_R",
                CubeListBuilder.create().texOffs(77, 127).mirror()
                        .addBox(-3.0F, -1.0F, -1.0F, 3, 18, 5),
                PartPose.offsetAndRotation(-8.0F, -8.0F, -10.0F, -0.0698132F, 0.0F, 0.7679449F));

        partdefinition.addOrReplaceChild("farm_2_R",
                CubeListBuilder.create().texOffs(102, 123).mirror()
                        .addBox(-1.5F, 0.0F, -1.0F, 2, 24, 3),
                PartPose.offsetAndRotation(-20.0F, 3.0F, -10.0F, -0.4712389F, 0.0F, 0.4886922F));

        partdefinition.addOrReplaceChild("fwing_1_R",
                CubeListBuilder.create().texOffs(24, 150).mirror()
                        .addBox(-2.5F, -1.0F, 3.0F, 1, 19, 15),
                PartPose.offsetAndRotation(-8.0F, -8.0F, -10.0F, -0.1745329F, 0.1919862F, 0.7504916F));

        partdefinition.addOrReplaceChild("fwing_2_R",
                CubeListBuilder.create().texOffs(150, 50).mirror()
                        .addBox(-1.0F, -1.0F, 0.0F, 1, 23, 17),
                PartPose.offsetAndRotation(-20.0F, 3.0F, -10.0F, -0.5235988F, 0.0349066F, 0.4712389F));

        partdefinition.addOrReplaceChild("fleg_1_L",
                CubeListBuilder.create().texOffs(0, 104).mirror()
                        .addBox(0.0F, -3.0F, -4.0F, 3, 15, 7),
                PartPose.offsetAndRotation(5.0F, 5.0F, 10.0F, -0.6108652F, 0.0F, -0.3316126F));

        partdefinition.addOrReplaceChild("fleg_1_R",
                CubeListBuilder.create().texOffs(0, 149).mirror()
                        .addBox(-3.0F, -3.0F, -4.0F, 3, 15, 7),
                PartPose.offsetAndRotation(-6.0F, 5.0F, 10.0F, -0.6108652F, 0.0F, 0.3316126F));

        partdefinition.addOrReplaceChild("fleg_2_L",
                CubeListBuilder.create().texOffs(21, 108).mirror()
                        .addBox(1.0F, 0.0F, -3.0F, 2, 14, 4),
                PartPose.offsetAndRotation(8.0F, 13.0F, 6.0F, 0.6108652F, 0.0F, -0.1745329F));

        partdefinition.addOrReplaceChild("fleg_2_R",
                CubeListBuilder.create().texOffs(21, 108).mirror()
                        .addBox(-2.0F, 0.0F, -3.0F, 2, 14, 4),
                PartPose.offsetAndRotation(-10.0F, 13.0F, 6.0F, 0.6108652F, 0.0F, 0.1745329F));

        partdefinition.addOrReplaceChild("ffootL",
                CubeListBuilder.create().texOffs(50, 29).mirror()
                        .addBox(-2.0F, -1.0F, -8.0F, 4, 2, 9),
                PartPose.offset(12.0F, 24.0F, 11.0F));

        partdefinition.addOrReplaceChild("ffootR",
                CubeListBuilder.create().texOffs(50, 29).mirror()
                        .addBox(-1.0F, 1.0F, -8.0F, 4, 2, 9),
                PartPose.offset(-14.0F, 22.0F, 11.0F));

        partdefinition.addOrReplaceChild("fwing_3_L",
                CubeListBuilder.create().texOffs(0, 0).mirror()
                        .addBox(-7.5F, 0.0F, -5.0F, 16, 1, 26),
                PartPose.offsetAndRotation(-5.0F, 0.0F, 12.0F, -0.4886922F, -0.5235988F, 0.4014257F));

        partdefinition.addOrReplaceChild("fwing_3_R",
                CubeListBuilder.create().texOffs(150, 0).mirror()
                        .addBox(-8.5F, 0.0F, -5.0F, 16, 1, 26),
                PartPose.offsetAndRotation(4.0F, 0.0F, 12.0F, -0.4886922F, 0.5235988F, -0.4014257F));

        partdefinition.addOrReplaceChild("fwing_4_L",
                CubeListBuilder.create().texOffs(8, 117).mirror()
                        .addBox(-1.5F, -0.5F, -2.0F, 3, 1, 31),
                PartPose.offsetAndRotation(6.0F, 6.0F, 24.0F, -0.6283185F, -0.0174533F, 0.0F));

        partdefinition.addOrReplaceChild("fwing_4_R",
                CubeListBuilder.create().texOffs(8, 117).mirror()
                        .addBox(-1.5F, -0.5F, -2.0F, 3, 1, 31),
                PartPose.offsetAndRotation(-7.0F, 6.0F, 24.0F, -0.6283185F, 0.0174533F, 0.0F));

        partdefinition.addOrReplaceChild("fclaw_L",
                CubeListBuilder.create().texOffs(0, 129).mirror()
                        .addBox(0.0F, -1.0F, -9.0F, 1, 2, 10),
                PartPose.offsetAndRotation(30.0F, 23.0F, -20.0F, 0.0F, 0.1570796F, 0.0F));

        partdefinition.addOrReplaceChild("fclaw_R",
                CubeListBuilder.create().texOffs(0, 129).mirror()
                        .addBox(0.0F, -1.0F, -9.0F, 1, 2, 10),
                PartPose.offsetAndRotation(-31.0F, 23.0F, -20.0F, 0.0F, -0.1570796F, 0.0F));

        partdefinition.addOrReplaceChild("fclaw_L2",
                CubeListBuilder.create().texOffs(18, 38).mirror()
                        .addBox(0.0F, -2.5F, -6.0F, 1, 2, 7),
                PartPose.offsetAndRotation(-30.0F, 23.0F, -28.0F, 0.5061455F, -0.2792527F, 0.0F));

        partdefinition.addOrReplaceChild("fclaw_R_2",
                CubeListBuilder.create().texOffs(18, 38).mirror()
                        .addBox(-1.0F, -2.5F, -6.0F, 1, 2, 7),
                PartPose.offsetAndRotation(30.0F, 23.0F, -28.0F, 0.5061455F, 0.2792527F, 0.0F));

        partdefinition.addOrReplaceChild("fwing_5_L",
                CubeListBuilder.create().texOffs(46, 10).mirror()
                        .addBox(-1.0F, -3.0F, -1.0F, 1, 8, 31),
                PartPose.offsetAndRotation(31.0F, 21.0F, -19.0F, 0.6806784F, 0.0523599F, -0.2792527F));

        partdefinition.addOrReplaceChild("fwing_6_L",
                CubeListBuilder.create().texOffs(46, 10).mirror()
                        .addBox(-1.0F, -3.0F, -1.0F, 1, 8, 31),
                PartPose.offsetAndRotation(31.0F, 21.0F, -19.0F, 0.4537856F, 0.2443461F, -0.3665191F));

        partdefinition.addOrReplaceChild("fwing_7_L",
                CubeListBuilder.create().texOffs(46, 10).mirror()
                        .addBox(-1.0F, -3.0F, -1.0F, 1, 8, 31),
                PartPose.offsetAndRotation(-30.0F, 21.0F, -19.0F, 0.1396263F, -0.3316126F, 0.4014257F));

        partdefinition.addOrReplaceChild("fwing_5_R",
                CubeListBuilder.create().texOffs(46, 10).mirror()
                        .addBox(-1.0F, -3.0F, -1.0F, 1, 8, 31),
                PartPose.offsetAndRotation(-30.0F, 21.0F, -19.0F, 0.6806784F, -0.0523599F, 0.2792527F));

        partdefinition.addOrReplaceChild("fwing_6_R",
                CubeListBuilder.create().texOffs(46, 10).mirror()
                        .addBox(-1.0F, -3.0F, -1.0F, 1, 8, 31),
                PartPose.offsetAndRotation(-30.0F, 21.0F, -19.0F, 0.4537856F, -0.2443461F, 0.3665191F));

        partdefinition.addOrReplaceChild("fwing_7_R",
                CubeListBuilder.create().texOffs(46, 10).mirror()
                        .addBox(-1.0F, -3.0F, -1.0F, 1, 8, 31),
                PartPose.offsetAndRotation(31.0F, 21.0F, -19.0F, 0.1396263F, 0.3316126F, -0.4014257F));

        return LayerDefinition.create(meshdefinition, 256, 256);
    }

    @Override
    public void setupAnim(EntityLeon entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float newangle = 0.0f;
        float newangle2 = 0.0f;
        float newangle3 = 0.0f;
        float spd = 1.0f;
        float amp = 1.0f;
        if ((double)limbSwingAmount > 0.1) {
        newangle = Mth.cos((float)(ageInTicks * 1.8f * limbSwingAmount)) * (float)Math.PI * 0.25f * limbSwingAmount;
        newangle2 = Mth.cos((float)(ageInTicks * 0.9f * limbSwingAmount)) * (float)Math.PI * 0.25f * limbSwingAmount;
        } else {
        newangle = 0.0f;
        newangle2 = Mth.cos((float)(ageInTicks * 0.9f * limbSwingAmount)) * (float)Math.PI * 0.02f;
        if (entity.isInSittingPose()) {
        newangle2 = 0.0f;
        }
        }
        if (entity.getActivity() == 0) {
        this.leg_1_L.xRot = -0.611f + newangle;
        this.leg_1_R.xRot = -0.611f - newangle;
        this.leg_2_L.xRot = 0.611f + newangle;
        this.leg_2_L.z = (float)((double)this.leg_1_L.z + Math.sin(this.leg_1_L.xRot) * 9.0);
        this.leg_2_L.y = (float)((double)this.leg_1_L.y + Math.cos(this.leg_1_L.xRot) * 9.0);
        this.leg_2_R.xRot = 0.611f - newangle;
        this.leg_2_R.z = (float)((double)this.leg_1_R.z + Math.sin(this.leg_1_R.xRot) * 9.0);
        this.leg_2_R.y = (float)((double)this.leg_1_R.y + Math.cos(this.leg_1_R.xRot) * 9.0);
        this.footL.z = (float)((double)this.leg_2_L.z + Math.sin(this.leg_2_L.xRot) * 13.0);
        this.footL.y = (float)((double)this.leg_2_L.y + Math.cos(this.leg_2_L.xRot) * 13.0);
        this.footR.z = (float)((double)this.leg_2_R.z + Math.sin(this.leg_2_R.xRot) * 11.0);
        this.footR.y = (float)((double)this.leg_2_R.y + Math.cos(this.leg_2_R.xRot) * 11.0);
        this.wing_3_R.yRot = 0.523f - newangle / 10.0f;
        this.wing_3_L.yRot = -0.523f - newangle / 10.0f;
        this.arm_1_L.xRot = -0.07f - (newangle /= 2.0f);
        this.arm_1_R.xRot = -0.07f + newangle;
        this.wing_1_L.xRot = -0.17f - newangle;
        this.wing_1_R.xRot = -0.17f + newangle;
        this.arm_2_L.xRot = -0.471f - newangle;
        this.wing_2_L.xRot = -0.523f - newangle;
        this.arm_2_L.z = this.wing_2_L.z = (float)((double)this.arm_1_L.z + Math.sin(this.arm_1_L.xRot) * 11.0);
        this.arm_2_L.y = this.wing_2_L.y = (float)((double)this.arm_1_L.y + Math.cos(this.arm_1_L.xRot) * 11.0);
        this.wing_5_L.xRot = 0.68f + newangle2 / 2.0f;
        this.wing_6_L.xRot = 0.453f + newangle2 / 4.0f;
        this.wing_7_R.xRot = 0.119f + newangle2 / 8.0f;
        this.wing_5_L.z = (float)((double)this.arm_2_L.z + Math.sin(this.arm_2_L.xRot) * 20.0);
        this.wing_5_L.y = (float)((double)this.arm_2_L.y + Math.cos(this.arm_2_L.xRot) * 20.0);
        this.wing_6_L.z = this.wing_5_L.z;
        this.wing_6_L.y = this.wing_5_L.y;
        this.wing_7_R.z = this.wing_5_L.z;
        this.wing_7_R.y = this.wing_5_L.y;
        this.claw_L.z = this.wing_5_L.z - 1.0f;
        this.claw_R_2.z = this.wing_5_L.z - 9.0f;
        this.claw_L.y = this.wing_5_L.y + 2.0f;
        this.claw_R_2.y = this.wing_5_L.y + 2.0f;
        this.arm_2_R.xRot = -0.471f + newangle;
        this.wing_2_R.xRot = -0.523f + newangle;
        this.arm_2_R.z = this.wing_2_R.z = (float)((double)this.arm_1_R.z + Math.sin(this.arm_1_R.xRot) * 11.0);
        this.arm_2_R.y = this.wing_2_R.y = (float)((double)this.arm_1_R.y + Math.cos(this.arm_1_R.xRot) * 11.0);
        this.wing_5_R.xRot = 0.68f + newangle2 / 2.0f;
        this.wing_6_R.xRot = 0.453f + newangle2 / 4.0f;
        this.wing_7_L.xRot = 0.119f + newangle2 / 8.0f;
        this.wing_5_R.z = (float)((double)this.arm_2_R.z + Math.sin(this.arm_2_R.xRot) * 20.0);
        this.wing_5_R.y = (float)((double)this.arm_2_R.y + Math.cos(this.arm_2_R.xRot) * 20.0);
        this.wing_6_R.z = this.wing_5_R.z;
        this.wing_6_R.y = this.wing_5_R.y;
        this.wing_7_L.z = this.wing_5_R.z;
        this.wing_7_L.y = this.wing_5_R.y;
        this.claw_R.z = this.wing_5_R.z - 1.0f;
        this.claw_L2.z = this.wing_5_R.z - 9.0f;
        this.claw_R.y = this.wing_5_R.y + 2.0f;
        this.claw_L2.y = this.wing_5_R.y + 2.0f;
        newangle2 = Mth.cos((float)(ageInTicks * 0.6f * limbSwingAmount)) * (float)Math.PI * 0.02f;
        this.chest_ridge.xRot = this.chest.xRot = -0.436f + newangle2 / 8.0f;
        this.bottom_jaw.xRot = -1.308f + newangle2 / 2.0f;
        this.lower_sail1.xRot = 0.297f + newangle2 / 2.0f;
        this.lower_sail2.xRot = 0.384f + newangle2 / 2.0f;
        this.lower_sail_3.xRot = -0.384f + newangle2 / 2.0f;
        this.upper_sail2_.yRot = this.upper_sail3.yRot = (newangle = (float)Math.toRadians(netHeadYaw) * 0.5f);
        this.upper_sail_1.yRot = this.upper_sail3.yRot;
        this.upper_jaw.yRot = this.upper_sail3.yRot;
        this.head.yRot = this.upper_sail3.yRot;
        this.eye_ridge_L.yRot = 0.558f + newangle;
        this.anntena_1_L.yRot = 0.366f + newangle;
        this.anntena_2_L.yRot = 0.139f + newangle;
        this.eye_ridge_R.yRot = -0.558f + newangle;
        this.anntena_1_R.yRot = -0.366f + newangle;
        this.anntena_2_R.yRot = -0.139f + newangle;
        this.lower_sail2.yRot = this.lower_sail_3.yRot = newangle;
        this.lower_sail1.yRot = this.lower_sail_3.yRot;
        this.bottom_jaw.yRot = this.lower_sail_3.yRot;
        this.bottom_jaw.z = (float)((double)this.head.z - Math.cos(newangle) * 5.0);
        this.bottom_jaw.x = (float)((double)this.head.x - Math.sin(newangle) * 5.0);
        } else {
        if (entity.getAttacking() != 0) {
        spd = 1.7f;
        amp = 1.4f;
        }
        newangle2 = Mth.cos((float)(ageInTicks * 1.6f * limbSwingAmount * spd)) * (float)Math.PI * 0.06f;
        this.fchest.xRot = newangle2 / 8.0f;
        this.fchest_ridge.xRot = -0.18f + this.fchest.xRot;
        this.fchest.y = entity.getBeingRidden() == 0 ? (float)(-2.0 + Math.sin(newangle2) * 10.0 * (double)amp) : -2.0f;
        this.fchest_ridge.y = this.fchest.y;
        this.fabdomen.xRot = 0.0f;
        this.fabdomen.z = (float)((double)this.fchest.z + Math.cos(this.fchest.xRot) * 8.0);
        this.fwing_3_R.y = this.fabdomen.y = (float)((double)this.fchest.y - Math.sin(this.fchest.xRot) * 8.0 - 6.0);
        this.fwing_3_L.y = this.fabdomen.y;
        this.fwing_3_R.zRot = 0.0f;
        this.fwing_3_R.xRot = 0.0f;
        this.fwing_3_L.zRot = 0.0f;
        this.fwing_3_L.xRot = 0.0f;
        this.fwing_3_R.yRot = 0.785f;
        this.fwing_3_L.yRot = -0.785f;
        this.fwing_4_R.y = this.fabdomen.y + 0.55f;
        this.fwing_4_L.y = this.fabdomen.y + 0.55f;
        this.fwing_4_R.z = this.fabdomen.z + 26.0f;
        this.fwing_4_L.z = this.fabdomen.z + 26.0f;
        this.fwing_4_R.x = this.fabdomen.z + 8.0f;
        this.fwing_4_L.x = this.fabdomen.z - 9.0f;
        this.fwing_4_R.xRot = newangle2 / 10.0f;
        this.fwing_4_L.xRot = -newangle2 / 10.0f;
        if (entity.getAttacking() == 0) {
        newangle = 1.5707964f;
        this.fleg_1_L.y = this.fabdomen.y + 5.0f;
        this.fleg_1_R.y = this.fabdomen.y + 5.0f;
        this.fleg_1_L.xRot = -0.1f + newangle;
        this.fleg_1_R.xRot = -0.1f + newangle;
        this.fleg_2_L.xRot = 0.1f + newangle;
        this.fleg_2_L.z = (float)((double)this.fleg_1_L.z + Math.sin(this.fleg_1_L.xRot) * 9.0);
        this.fleg_2_L.y = (float)((double)this.fleg_1_L.y + Math.cos(this.fleg_1_L.xRot) * 9.0);
        this.fleg_2_R.xRot = 0.1f + newangle;
        this.fleg_2_R.z = (float)((double)this.fleg_1_R.z + Math.sin(this.fleg_1_R.xRot) * 9.0);
        this.fleg_2_R.y = (float)((double)this.fleg_1_R.y + Math.cos(this.fleg_1_R.xRot) * 9.0);
        this.ffootL.z = (float)((double)this.fleg_2_L.z + Math.sin(this.fleg_2_L.xRot) * 13.0);
        this.ffootL.y = (float)((double)this.fleg_2_L.y + Math.cos(this.fleg_2_L.xRot) * 13.0);
        this.ffootR.z = (float)((double)this.fleg_2_R.z + Math.sin(this.fleg_2_R.xRot) * 11.0);
        this.ffootR.y = (float)((double)this.fleg_2_R.y + Math.cos(this.fleg_2_R.xRot) * 11.0);
        this.ffootL.xRot = (float)Math.PI;
        this.ffootR.xRot = (float)Math.PI;
        this.fleg_2_L.x = this.fleg_1_L.x;
        this.ffootL.x = this.fleg_1_L.x;
        this.fleg_2_R.x = this.fleg_1_R.x;
        this.ffootR.x = this.fleg_1_R.x;
        } else {
        newangle = -0.7853982f;
        newangle3 = Mth.cos((float)(ageInTicks * 3.6f * limbSwingAmount)) * (float)Math.PI * 0.1f;
        this.fleg_1_L.y = this.fabdomen.y + 5.0f;
        this.fleg_1_R.y = this.fabdomen.y + 5.0f;
        this.fleg_1_L.xRot = -0.1f + newangle + newangle3;
        this.fleg_1_R.xRot = -0.1f + newangle - newangle3;
        this.fleg_2_L.xRot = 0.2f + newangle + newangle3 * 3.0f / 2.0f;
        this.fleg_2_L.z = (float)((double)this.fleg_1_L.z + Math.sin(this.fleg_1_L.xRot) * 9.0);
        this.fleg_2_L.y = (float)((double)this.fleg_1_L.y + Math.cos(this.fleg_1_L.xRot) * 9.0);
        this.fleg_2_R.xRot = 0.2f + newangle - newangle3 * 3.0f / 2.0f;
        this.fleg_2_R.z = (float)((double)this.fleg_1_R.z + Math.sin(this.fleg_1_R.xRot) * 9.0);
        this.fleg_2_R.y = (float)((double)this.fleg_1_R.y + Math.cos(this.fleg_1_R.xRot) * 9.0);
        this.ffootL.z = (float)((double)this.fleg_2_L.z + Math.sin(this.fleg_2_L.xRot) * 13.0);
        this.ffootL.y = (float)((double)this.fleg_2_L.y + Math.cos(this.fleg_2_L.xRot) * 13.0);
        this.ffootR.z = (float)((double)this.fleg_2_R.z + Math.sin(this.fleg_2_R.xRot) * 11.0);
        this.ffootR.y = (float)((double)this.fleg_2_R.y + Math.cos(this.fleg_2_R.xRot) * 11.0);
        this.ffootL.xRot = -0.7853982f + newangle3 * 2.0f;
        this.ffootR.xRot = -0.7853982f - newangle3 * 2.0f;
        this.fleg_2_L.x = 7.0f;
        this.ffootL.x = 11.0f;
        this.fleg_2_R.x = -9.0f;
        this.ffootR.x = -13.0f;
        }
        newangle = Mth.cos((float)(ageInTicks * 1.6f * limbSwingAmount * spd)) * (float)Math.PI * 0.26f * amp;
        this.farm_1_L.zRot = (float)(-1.5707963267948966 - (double)newangle);
        this.farm_1_R.zRot = (float)(1.5707963267948966 + (double)newangle);
        this.fwing_1_L.zRot = (float)(-1.5707963267948966 - (double)newangle);
        this.fwing_1_R.zRot = (float)(1.5707963267948966 + (double)newangle);
        this.farm_2_L.zRot = (float)(-1.5707963267948966 - (double)(newangle * 1.3f));
        this.fwing_2_L.zRot = (float)(-1.5707963267948966 - (double)(newangle * 1.3f));
        this.farm_2_L.x = this.fwing_2_L.x = (float)((double)this.farm_1_L.x + Math.cos(newangle) * 14.0);
        this.farm_2_L.y = this.fwing_2_L.y = (float)((double)this.farm_1_L.y - Math.sin(newangle) * 14.0);
        this.fwing_5_L.x = (float)((double)this.farm_2_L.x + Math.cos(newangle * 1.3f) * 20.0);
        this.fwing_5_L.y = (float)((double)this.farm_2_L.y - Math.sin(newangle * 1.3f) * 20.0);
        this.fwing_6_L.x = this.fwing_5_L.x;
        this.fwing_6_L.y = this.fwing_5_L.y;
        this.fwing_7_R.x = this.fwing_5_L.x;
        this.fwing_7_R.y = this.fwing_5_L.y;
        this.fclaw_L.x = this.fwing_5_L.x;
        this.fclaw_R_2.x = this.fwing_5_L.x;
        this.fclaw_L.y = this.fwing_5_L.y;
        this.fclaw_R_2.y = this.fwing_5_L.y;
        this.fwing_5_L.zRot = (float)(-1.5707963267948966 - (double)(newangle * 1.65f));
        this.fwing_6_L.zRot = (float)(-1.5707963267948966 - (double)(newangle * 1.65f));
        this.fwing_7_R.zRot = (float)(-1.5707963267948966 - (double)(newangle * 1.65f));
        this.fwing_7_R.xRot = -1.5707964f;
        this.fwing_6_L.xRot = -1.1780972f;
        this.fwing_5_L.xRot = -0.7853982f;
        this.farm_2_R.zRot = (float)(1.5707963267948966 + (double)(newangle * 1.3f));
        this.fwing_2_R.zRot = (float)(1.5707963267948966 + (double)(newangle * 1.3f));
        this.farm_2_R.x = this.fwing_2_R.x = (float)((double)this.farm_1_R.x - Math.cos(newangle) * 14.0);
        this.farm_2_R.y = this.fwing_2_R.y = (float)((double)this.farm_1_R.y - Math.sin(newangle) * 14.0);
        this.fwing_5_R.x = (float)((double)this.farm_2_R.x - Math.cos(newangle * 1.3f) * 20.0);
        this.fwing_5_R.y = (float)((double)this.farm_2_R.y - Math.sin(newangle * 1.3f) * 20.0);
        this.fwing_6_R.x = this.fwing_5_R.x;
        this.fwing_6_R.y = this.fwing_5_R.y;
        this.fwing_7_L.x = this.fwing_5_R.x;
        this.fwing_7_L.y = this.fwing_5_R.y;
        this.fclaw_R.x = this.fwing_5_R.x;
        this.fclaw_L2.x = this.fwing_5_R.x;
        this.fclaw_R.y = this.fwing_5_R.y;
        this.fclaw_L2.y = this.fwing_5_R.y;
        this.fwing_5_R.zRot = (float)(1.5707963267948966 + (double)(newangle * 1.65f));
        this.fwing_6_R.zRot = (float)(1.5707963267948966 + (double)(newangle * 1.65f));
        this.fwing_7_L.zRot = (float)(1.5707963267948966 + (double)(newangle * 1.65f));
        this.fwing_7_L.xRot = -1.5707964f;
        this.fwing_6_R.xRot = -1.1780972f;
        this.fwing_5_R.xRot = -0.7853982f;
        this.fneck_1.xRot = -newangle / 12.0f;
        this.fneck_1.z = (float)((double)this.fchest.z - Math.cos(this.fchest.xRot) * 10.0);
        this.fneck_1.y = (float)((double)this.fchest.y + Math.sin(this.fchest.xRot) * 8.0 - 1.0);
        this.fneck_2.xRot = -newangle / 10.0f;
        this.fneck_2.z = (float)((double)this.fneck_1.z - Math.cos(this.fneck_1.xRot) * 7.0);
        this.fneck_2.y = (float)((double)this.fneck_1.y + Math.sin(this.fneck_1.xRot) * 6.0 - 1.0);
        this.fneck_3.xRot = -newangle / 8.0f;
        this.fneck_3.z = (float)((double)this.fneck_2.z - Math.cos(this.fneck_2.xRot) * 7.0);
        this.fneck_3.y = (float)((double)this.fneck_2.y + Math.sin(this.fneck_2.xRot) * 5.0);
        this.fhead.z = (float)((double)this.fneck_3.z - Math.cos(this.fneck_3.xRot) * 16.0);
        this.fhead.y = (float)((double)this.fneck_3.y + Math.sin(this.fneck_3.xRot) * 15.0);
        this.fupper_jaw.z = this.fhead.z;
        this.fupper_sail_1.z = this.fhead.z;
        this.fupper_sail2_.z = this.fhead.z;
        this.fupper_sail3.z = this.fhead.z;
        this.feye_ridge_L.z = this.fhead.z;
        this.fanntena_1_L.z = this.fhead.z;
        this.fanntena_2_L.z = this.fhead.z;
        this.feye_ridge_R.z = this.fhead.z;
        this.fanntena_1_R.z = this.fhead.z;
        this.fanntena_2_R.z = this.fhead.z;
        this.fbottom_jaw.z = this.fhead.z - 5.0f;
        this.flower_sail1.z = this.fhead.z - 5.0f;
        this.flower_sail2.z = this.fhead.z - 5.0f;
        this.flower_sail_3.z = this.fhead.z - 5.0f;
        this.fupper_jaw.y = this.fhead.y;
        this.fupper_sail_1.y = this.fhead.y;
        this.fupper_sail2_.y = this.fhead.y;
        this.fupper_sail3.y = this.fhead.y;
        this.feye_ridge_L.y = this.fhead.y;
        this.fanntena_1_L.y = this.fhead.y;
        this.fanntena_2_L.y = this.fhead.y;
        this.feye_ridge_R.y = this.fhead.y;
        this.fanntena_1_R.y = this.fhead.y;
        this.fanntena_2_R.y = this.fhead.y;
        this.fbottom_jaw.y = this.fhead.y + 4.0f;
        this.flower_sail1.y = this.fhead.y + 4.0f;
        this.flower_sail2.y = this.fhead.y + 4.0f;
        this.flower_sail_3.y = this.fhead.y + 4.0f;
        if (entity.getBeingRidden() == 0) {
        newangle = (float)Math.toRadians(netHeadYaw) * 0.5f;
        } else {
        netHeadYaw = (entity.yBodyRotO - entity.yBodyRot) * 8.0f;
        netHeadYaw = -netHeadYaw;
        rf1 += (netHeadYaw - rf1) / 60.0f;
        if (rf1 > 50.0f) {
        rf1 = 50.0f;
        }
        if (rf1 < -50.0f) {
        rf1 = -50.0f;
        }
        netHeadYaw = rf1;
        newangle = (float)Math.toRadians(netHeadYaw) * 0.5f;
        }
        this.fupper_sail2_.yRot = this.fupper_sail3.yRot = newangle;
        this.fupper_sail_1.yRot = this.fupper_sail3.yRot;
        this.fupper_jaw.yRot = this.fupper_sail3.yRot;
        this.fhead.yRot = this.fupper_sail3.yRot;
        this.feye_ridge_L.yRot = 0.558f + newangle;
        this.fanntena_1_L.yRot = 0.366f + newangle;
        this.fanntena_2_L.yRot = 0.139f + newangle;
        this.feye_ridge_R.yRot = -0.558f + newangle;
        this.fanntena_1_R.yRot = -0.366f + newangle;
        this.fanntena_2_R.yRot = -0.139f + newangle;
        this.flower_sail2.yRot = this.flower_sail_3.yRot = newangle;
        this.flower_sail1.yRot = this.flower_sail_3.yRot;
        this.fbottom_jaw.yRot = this.flower_sail_3.yRot;
        this.fbottom_jaw.z = (float)((double)this.fhead.z - Math.cos(newangle) * 5.0);
        this.fbottom_jaw.x = (float)((double)this.fhead.x - Math.sin(newangle) * 5.0);
        float tf1 = 1.605f;
        float tf2 = 1.6919999f;
        float tf3 = 0.92399997f;
        if (entity.getAttacking() == 0) {
        this.fbottom_jaw.xRot = -1.308f + newangle2 / 2.0f;
        } else {
        newangle2 = Mth.cos((float)(ageInTicks * 2.6f * limbSwingAmount)) * (float)Math.PI * 0.16f;
        this.fbottom_jaw.xRot = -0.9f + newangle2;
        }
        this.flower_sail1.xRot = this.fbottom_jaw.xRot + tf1;
        this.flower_sail2.xRot = this.fbottom_jaw.xRot + tf2;
        this.flower_sail_3.xRot = this.fbottom_jaw.xRot + tf3;
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.chest.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.neck_1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.neck_2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.neck_3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.abdomen.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.head.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.upper_jaw.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.bottom_jaw.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.chest_ridge.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.upper_sail_1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.upper_sail2_.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.upper_sail3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lower_sail1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lower_sail2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lower_sail_3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.eye_ridge_L.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.eye_ridge_R.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.anntena_1_L.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.anntena_1_R.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.anntena_2_L.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.anntena_2_R.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.arm_1_L.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.arm_2_L.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.wing_1_L.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.wing_2_L.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.arm_1_R.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.arm_2_R.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.wing_1_R.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.wing_2_R.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg_1_L.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg_1_R.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg_2_L.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg_2_R.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.footL.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.footR.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.wing_3_L.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.wing_3_R.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.wing_4_L.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.wing_4_R.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.claw_L.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.claw_R.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.claw_L2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.claw_R_2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.wing_5_L.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.wing_6_L.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.wing_7_L.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.wing_5_R.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.wing_6_R.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.wing_7_R.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.fchest.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.fneck_1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.fneck_2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.fneck_3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.fabdomen.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.fhead.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.fupper_jaw.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.fbottom_jaw.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.fchest_ridge.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.fupper_sail_1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.fupper_sail2_.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.fupper_sail3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.flower_sail1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.flower_sail2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.flower_sail_3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.feye_ridge_L.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.feye_ridge_R.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.fanntena_1_L.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.fanntena_1_R.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.fanntena_2_L.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.fanntena_2_R.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.farm_1_L.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.farm_2_L.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.fwing_1_L.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.fwing_2_L.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.farm_1_R.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.farm_2_R.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.fwing_1_R.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.fwing_2_R.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.fleg_1_L.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.fleg_1_R.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.fleg_2_L.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.fleg_2_R.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.ffootL.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.ffootR.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.fwing_3_L.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.fwing_3_R.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.fwing_4_L.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.fwing_4_R.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.fclaw_L.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.fclaw_R.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.fclaw_L2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.fclaw_R_2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.fwing_5_L.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.fwing_6_L.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.fwing_7_L.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.fwing_5_R.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.fwing_6_R.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.fwing_7_R.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
