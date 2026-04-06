package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.EnderReaper;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class ModelEnderReaper extends EntityModel<EnderReaper> {
    private final ModelPart rwing1;
    private final ModelPart lwing1;
    private final ModelPart Shape3;
    private final ModelPart Shape4;
    private final ModelPart Shape5;
    private final ModelPart Shape6;
    private final ModelPart Shape7;
    private final ModelPart Shape8;
    private final ModelPart Shape9;
    private final ModelPart Shape10;
    private final ModelPart Shape11;
    private final ModelPart Shape12;
    private final ModelPart Shape13;
    private final ModelPart Shape14;
    private final ModelPart Shape15;
    private final ModelPart Shape16;
    private final ModelPart Shape17;
    private final ModelPart Shape18;
    private final ModelPart Shape19;
    private final ModelPart Shape20;
    private final ModelPart Shape21;
    private final ModelPart Shape22;
    private final ModelPart Shape23;
    private final ModelPart Shape24;
    private final ModelPart Shape25;
    private final ModelPart Shape26;
    private final ModelPart Shape27;
    private final ModelPart Shape28;
    private final ModelPart Shape29;
    private final ModelPart Shape30;
    private final ModelPart Shape31;
    private final ModelPart Shape32;
    private final ModelPart Shape33;
    private final ModelPart Shape34;
    private final ModelPart Shape35;
    private final ModelPart Shape36;
    private final ModelPart Shape37;
    private final ModelPart Shape38;
    private final ModelPart Shape39;
    private final ModelPart Shape40;
    private final ModelPart Shape41;
    private final ModelPart Shape42;
    private final ModelPart Shape43;
    private final ModelPart Shape44;
    private final ModelPart Shape45;
    private final ModelPart Shape46;
    private final ModelPart Shape47;
    private final ModelPart Shape48;
    private final ModelPart Shape49;
    private final ModelPart rarm2;
    private final ModelPart rarm3;
    private final ModelPart relbow;
    private final ModelPart rarm1;
    private final ModelPart Shape54;
    private final ModelPart larm3;
    private final ModelPart larm2;
    private final ModelPart lelbow;
    private final ModelPart larm1;
    private final ModelPart scythe1;
    private final ModelPart scythe2;
    private final ModelPart scythe3;
    private final ModelPart head;
    private final ModelPart lwing3;
    private final ModelPart lwing2;
    private final ModelPart rwing3;
    private final ModelPart rwing2;

    public ModelEnderReaper(ModelPart root) {
        this.rwing1 = root.getChild("rwing1");
        this.lwing1 = root.getChild("lwing1");
        this.Shape3 = root.getChild("Shape3");
        this.Shape4 = root.getChild("Shape4");
        this.Shape5 = root.getChild("Shape5");
        this.Shape6 = root.getChild("Shape6");
        this.Shape7 = root.getChild("Shape7");
        this.Shape8 = root.getChild("Shape8");
        this.Shape9 = root.getChild("Shape9");
        this.Shape10 = root.getChild("Shape10");
        this.Shape11 = root.getChild("Shape11");
        this.Shape12 = root.getChild("Shape12");
        this.Shape13 = root.getChild("Shape13");
        this.Shape14 = root.getChild("Shape14");
        this.Shape15 = root.getChild("Shape15");
        this.Shape16 = root.getChild("Shape16");
        this.Shape17 = root.getChild("Shape17");
        this.Shape18 = root.getChild("Shape18");
        this.Shape19 = root.getChild("Shape19");
        this.Shape20 = root.getChild("Shape20");
        this.Shape21 = root.getChild("Shape21");
        this.Shape22 = root.getChild("Shape22");
        this.Shape23 = root.getChild("Shape23");
        this.Shape24 = root.getChild("Shape24");
        this.Shape25 = root.getChild("Shape25");
        this.Shape26 = root.getChild("Shape26");
        this.Shape27 = root.getChild("Shape27");
        this.Shape28 = root.getChild("Shape28");
        this.Shape29 = root.getChild("Shape29");
        this.Shape30 = root.getChild("Shape30");
        this.Shape31 = root.getChild("Shape31");
        this.Shape32 = root.getChild("Shape32");
        this.Shape33 = root.getChild("Shape33");
        this.Shape34 = root.getChild("Shape34");
        this.Shape35 = root.getChild("Shape35");
        this.Shape36 = root.getChild("Shape36");
        this.Shape37 = root.getChild("Shape37");
        this.Shape38 = root.getChild("Shape38");
        this.Shape39 = root.getChild("Shape39");
        this.Shape40 = root.getChild("Shape40");
        this.Shape41 = root.getChild("Shape41");
        this.Shape42 = root.getChild("Shape42");
        this.Shape43 = root.getChild("Shape43");
        this.Shape44 = root.getChild("Shape44");
        this.Shape45 = root.getChild("Shape45");
        this.Shape46 = root.getChild("Shape46");
        this.Shape47 = root.getChild("Shape47");
        this.Shape48 = root.getChild("Shape48");
        this.Shape49 = root.getChild("Shape49");
        this.rarm2 = root.getChild("rarm2");
        this.rarm3 = root.getChild("rarm3");
        this.relbow = root.getChild("relbow");
        this.rarm1 = root.getChild("rarm1");
        this.Shape54 = root.getChild("Shape54");
        this.larm3 = root.getChild("larm3");
        this.larm2 = root.getChild("larm2");
        this.lelbow = root.getChild("lelbow");
        this.larm1 = root.getChild("larm1");
        this.scythe1 = root.getChild("scythe1");
        this.scythe2 = root.getChild("scythe2");
        this.scythe3 = root.getChild("scythe3");
        this.head = root.getChild("head");
        this.lwing3 = root.getChild("lwing3");
        this.lwing2 = root.getChild("lwing2");
        this.rwing3 = root.getChild("rwing3");
        this.rwing2 = root.getChild("rwing2");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("rwing1",
                CubeListBuilder.create().texOffs(20, 430).mirror().addBox(0.0F, 0.0F, 0.0F, 0, 50, 17),
                PartPose.offsetAndRotation(-4.0F, -6.9F, 8.5F, 1.745F, -0.785F, 0.0F));

        partdefinition.addOrReplaceChild("lwing1",
                CubeListBuilder.create().texOffs(20, 350).mirror().addBox(0.0F, 0.0F, 0.0F, 0, 50, 17),
                PartPose.offsetAndRotation(4.0F, -6.9F, 8.5F, 1.745F, 0.785F, 0.0F));

        partdefinition.addOrReplaceChild("Shape3",
                CubeListBuilder.create().texOffs(20, 320).mirror().addBox(-4.0F, 0.0F, -2.0F, 2, 12, 1),
                PartPose.offset(3.0F, -14.0F, 10.0F));

        partdefinition.addOrReplaceChild("Shape4",
                CubeListBuilder.create().texOffs(40, 320).mirror().addBox(-4.0F, 0.0F, -2.0F, 2, 6, 1),
                PartPose.offsetAndRotation(3.0F, -2.0F, 10.0F, -0.247F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("Shape5",
                CubeListBuilder.create().texOffs(20, 310).mirror().addBox(-4.0F, 0.0F, -2.0F, 1, 3, 1),
                PartPose.offsetAndRotation(3.5F, 4.0F, 8.0F, -0.768F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("Shape6",
                CubeListBuilder.create().texOffs(20, 292).mirror().addBox(-4.0F, 0.0F, -2.0F, 2, 6, 2),
                PartPose.offsetAndRotation(3.0F, -12.0F, 7.5F, -2.356F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("Shape7",
                CubeListBuilder.create().texOffs(20, 280).mirror().addBox(-4.0F, 0.0F, -2.0F, 4, 1, 1),
                PartPose.offset(5.0F, -14.0F, 10.0F));

        partdefinition.addOrReplaceChild("Shape8",
                CubeListBuilder.create().texOffs(20, 269).mirror().addBox(-4.0F, 0.0F, -2.0F, 4, 1, 1),
                PartPose.offset(-1.0F, -14.0F, 10.0F));

        partdefinition.addOrReplaceChild("Shape9",
                CubeListBuilder.create().texOffs(20, 257).mirror().addBox(-4.0F, 0.0F, -2.0F, 4, 1, 1),
                PartPose.offset(-1.0F, -12.0F, 10.0F));

        partdefinition.addOrReplaceChild("Shape10",
                CubeListBuilder.create().texOffs(20, 246).mirror().addBox(-4.0F, 0.0F, -2.0F, 4, 1, 1),
                PartPose.offset(-1.0F, -10.0F, 10.0F));

        partdefinition.addOrReplaceChild("Shape11",
                CubeListBuilder.create().texOffs(20, 237).mirror().addBox(-4.0F, 0.0F, -2.0F, 4, 1, 1),
                PartPose.offset(-1.0F, -8.0F, 10.0F));

        partdefinition.addOrReplaceChild("Shape12",
                CubeListBuilder.create().texOffs(20, 228).mirror().addBox(-4.0F, 0.0F, -2.0F, 2, 1, 1),
                PartPose.offset(1.0F, -6.0F, 10.0F));

        partdefinition.addOrReplaceChild("Shape13",
                CubeListBuilder.create().texOffs(20, 219).mirror().addBox(-4.0F, 0.0F, -2.0F, 3, 1, 1),
                PartPose.offset(1.0F, -4.0F, 10.0F));

        partdefinition.addOrReplaceChild("Shape14",
                CubeListBuilder.create().texOffs(20, 209).mirror().addBox(-4.0F, 0.0F, -2.0F, 1, 1, 1),
                PartPose.offset(3.5F, -14.0F, 11.0F));

        partdefinition.addOrReplaceChild("Shape15",
                CubeListBuilder.create().texOffs(20, 201).mirror().addBox(-4.0F, 0.0F, -2.0F, 1, 1, 1),
                PartPose.offset(3.5F, -12.0F, 11.0F));

        partdefinition.addOrReplaceChild("Shape16",
                CubeListBuilder.create().texOffs(20, 194).mirror().addBox(-4.0F, 0.0F, -2.0F, 1, 1, 1),
                PartPose.offset(3.5F, -10.0F, 11.0F));

        partdefinition.addOrReplaceChild("Shape17",
                CubeListBuilder.create().texOffs(20, 185).mirror().addBox(-4.0F, 0.0F, -2.0F, 1, 1, 1),
                PartPose.offset(3.5F, -8.0F, 11.0F));

        partdefinition.addOrReplaceChild("Shape18",
                CubeListBuilder.create().texOffs(20, 175).mirror().addBox(-4.0F, 0.0F, -2.0F, 1, 1, 1),
                PartPose.offset(3.5F, -6.0F, 11.0F));

        partdefinition.addOrReplaceChild("Shape19",
                CubeListBuilder.create().texOffs(20, 165).mirror().addBox(-4.0F, 0.0F, -2.0F, 1, 1, 1),
                PartPose.offset(3.5F, -4.0F, 11.0F));

        partdefinition.addOrReplaceChild("Shape20",
                CubeListBuilder.create().texOffs(20, 155).mirror().addBox(-4.0F, 0.0F, -2.0F, 4, 1, 1),
                PartPose.offset(5.0F, -12.0F, 10.0F));

        partdefinition.addOrReplaceChild("Shape21",
                CubeListBuilder.create().texOffs(20, 146).mirror().addBox(-4.0F, 0.0F, -2.0F, 4, 1, 1),
                PartPose.offset(5.0F, -10.0F, 10.0F));

        partdefinition.addOrReplaceChild("Shape22",
                CubeListBuilder.create().texOffs(20, 139).mirror().addBox(-4.0F, 0.0F, -2.0F, 4, 1, 1),
                PartPose.offset(5.0F, -8.0F, 10.0F));

        partdefinition.addOrReplaceChild("Shape23",
                CubeListBuilder.create().texOffs(20, 132).mirror().addBox(-4.0F, 0.0F, -2.0F, 3, 1, 1),
                PartPose.offset(5.0F, -6.0F, 10.0F));

        partdefinition.addOrReplaceChild("Shape24",
                CubeListBuilder.create().texOffs(20, 124).mirror().addBox(-4.0F, 0.0F, -2.0F, 2, 1, 1),
                PartPose.offset(5.0F, -4.0F, 10.0F));

        partdefinition.addOrReplaceChild("Shape25",
                CubeListBuilder.create().texOffs(20, 114).mirror().addBox(-4.0F, 0.0F, -2.0F, 1, 1, 3),
                PartPose.offset(6.0F, -4.0F, 8.0F));

        partdefinition.addOrReplaceChild("Shape26",
                CubeListBuilder.create().texOffs(20, 106).mirror().addBox(-4.0F, 0.0F, -2.0F, 2, 1, 1),
                PartPose.offset(5.0F, -4.0F, 8.0F));

        partdefinition.addOrReplaceChild("Shape27",
                CubeListBuilder.create().texOffs(20, 94).mirror().addBox(-4.0F, 0.0F, -2.0F, 1, 1, 5),
                PartPose.offset(7.0F, -6.0F, 6.0F));

        partdefinition.addOrReplaceChild("Shape28",
                CubeListBuilder.create().texOffs(20, 83).mirror().addBox(-4.0F, 0.0F, -2.0F, 1, 1, 5),
                PartPose.offset(8.0F, -8.0F, 5.0F));

        partdefinition.addOrReplaceChild("Shape29",
                CubeListBuilder.create().texOffs(20, 70).mirror().addBox(-4.0F, 0.0F, -2.0F, 1, 1, 6),
                PartPose.offset(8.0F, -10.0F, 4.0F));

        partdefinition.addOrReplaceChild("Shape30",
                CubeListBuilder.create().texOffs(20, 59).mirror().addBox(-4.0F, 0.0F, -2.0F, 1, 1, 6),
                PartPose.offset(8.0F, -12.0F, 4.0F));

        partdefinition.addOrReplaceChild("Shape31",
                CubeListBuilder.create().texOffs(20, 47).mirror().addBox(-4.0F, 0.0F, -2.0F, 1, 1, 6),
                PartPose.offset(8.0F, -14.0F, 4.0F));

        partdefinition.addOrReplaceChild("Shape32",
                CubeListBuilder.create().texOffs(20, 37).mirror().addBox(-4.0F, 0.0F, -2.0F, 2, 1, 1),
                PartPose.offset(6.0F, -6.0F, 6.0F));

        partdefinition.addOrReplaceChild("Shape33",
                CubeListBuilder.create().texOffs(20, 29).mirror().addBox(-4.0F, 0.0F, -2.0F, 4, 1, 1),
                PartPose.offset(5.0F, -8.0F, 5.0F));

        partdefinition.addOrReplaceChild("Shape34",
                CubeListBuilder.create().texOffs(40, 312).mirror().addBox(-4.0F, 0.0F, -2.0F, 4, 1, 1),
                PartPose.offset(5.0F, -10.0F, 4.0F));

        partdefinition.addOrReplaceChild("Shape35",
                CubeListBuilder.create().texOffs(40, 301).mirror().addBox(-4.0F, 0.0F, -2.0F, 4, 1, 1),
                PartPose.offset(5.0F, -12.0F, 4.0F));

        partdefinition.addOrReplaceChild("Shape36",
                CubeListBuilder.create().texOffs(40, 291).mirror().addBox(-4.0F, 0.0F, -2.0F, 4, 1, 1),
                PartPose.offset(5.0F, -14.0F, 4.0F));

        partdefinition.addOrReplaceChild("Shape37",
                CubeListBuilder.create().texOffs(40, 278).mirror().addBox(-4.0F, 0.0F, -2.0F, 1, 1, 3),
                PartPose.offset(1.0F, -4.0F, 8.0F));

        partdefinition.addOrReplaceChild("Shape38",
                CubeListBuilder.create().texOffs(40, 265).mirror().addBox(-4.0F, 0.0F, -2.0F, 1, 1, 5),
                PartPose.offset(0.0F, -6.0F, 6.0F));

        partdefinition.addOrReplaceChild("Shape39",
                CubeListBuilder.create().texOffs(40, 251).mirror().addBox(-4.0F, 0.0F, -2.0F, 1, 1, 6),
                PartPose.offset(-1.0F, -8.0F, 5.0F));

        partdefinition.addOrReplaceChild("Shape40",
                CubeListBuilder.create().texOffs(40, 235).mirror().addBox(-4.0F, 0.0F, -2.0F, 1, 1, 6),
                PartPose.offset(-1.0F, -10.0F, 4.0F));

        partdefinition.addOrReplaceChild("Shape41",
                CubeListBuilder.create().texOffs(40, 222).mirror().addBox(-4.0F, 0.0F, -2.0F, 1, 1, 6),
                PartPose.offset(-1.0F, -12.0F, 4.0F));

        partdefinition.addOrReplaceChild("Shape42",
                CubeListBuilder.create().texOffs(40, 209).mirror().addBox(-4.0F, 0.0F, -2.0F, 1, 1, 6),
                PartPose.offset(-1.0F, -14.0F, 4.0F));

        partdefinition.addOrReplaceChild("Shape43",
                CubeListBuilder.create().texOffs(40, 200).mirror().addBox(-4.0F, 0.0F, -2.0F, 2, 1, 1),
                PartPose.offset(1.0F, -4.0F, 8.0F));

        partdefinition.addOrReplaceChild("Shape44",
                CubeListBuilder.create().texOffs(40, 189).mirror().addBox(-4.0F, 0.0F, -2.0F, 2, 1, 1),
                PartPose.offset(0.0F, -6.0F, 6.0F));

        partdefinition.addOrReplaceChild("Shape45",
                CubeListBuilder.create().texOffs(40, 180).mirror().addBox(-4.0F, 0.0F, -2.0F, 4, 1, 1),
                PartPose.offset(-1.0F, -8.0F, 5.0F));

        partdefinition.addOrReplaceChild("Shape46",
                CubeListBuilder.create().texOffs(40, 170).mirror().addBox(-4.0F, 0.0F, -2.0F, 4, 1, 1),
                PartPose.offset(-1.0F, -10.0F, 4.0F));

        partdefinition.addOrReplaceChild("Shape47",
                CubeListBuilder.create().texOffs(40, 161).mirror().addBox(-4.0F, 0.0F, -2.0F, 4, 1, 1),
                PartPose.offset(-1.0F, -12.0F, 4.0F));

        partdefinition.addOrReplaceChild("Shape48",
                CubeListBuilder.create().texOffs(40, 151).mirror().addBox(-4.0F, 0.0F, -2.0F, 4, 1, 1),
                PartPose.offset(-1.0F, -14.0F, 4.0F));

        partdefinition.addOrReplaceChild("Shape49",
                CubeListBuilder.create().texOffs(40, 140).mirror().addBox(0.0F, 0.0F, 0.0F, 3, 2, 3),
                PartPose.offsetAndRotation(-7.5F, -15.5F, 3.0F, 0.0F, 0.0F, 0.524F));

        partdefinition.addOrReplaceChild("rarm2",
                CubeListBuilder.create().texOffs(40, 122).mirror().addBox(-4.0F, 0.0F, -2.0F, 1, 12, 1),
                PartPose.offsetAndRotation(-5.0F, -11.5F, 8.0F, 0.0F, -0.5F, 0.524F));

        partdefinition.addOrReplaceChild("rarm3",
                CubeListBuilder.create().texOffs(49, 122).mirror().addBox(-4.0F, 0.0F, -2.0F, 1, 12, 1),
                PartPose.offsetAndRotation(-4.0F, -11.5F, 6.0F, 0.0F, -0.5F, 0.524F));

        partdefinition.addOrReplaceChild("relbow",
                CubeListBuilder.create().texOffs(40, 111).mirror().addBox(0.0F, 0.0F, 0.0F, 2, 2, 3),
                PartPose.offsetAndRotation(-11.0F, -3.5F, 3.0F, 0.0F, -0.5F, 0.524F));

        partdefinition.addOrReplaceChild("rarm1",
                CubeListBuilder.create().texOffs(40, 91).mirror().addBox(-2.0F, -1.0F, -1.0F, 1, 11, 2),
                PartPose.offsetAndRotation(-10.5F, -2.0F, 2.5F, -0.76F, 0.0F, 0.3F));

        partdefinition.addOrReplaceChild("Shape54",
                CubeListBuilder.create().texOffs(40, 78).mirror().addBox(0.0F, 0.0F, 0.0F, 3, 2, 3),
                PartPose.offsetAndRotation(5.0F, -14.0F, 3.0F, 0.0F, 0.0F, -0.524F));

        partdefinition.addOrReplaceChild("larm3",
                CubeListBuilder.create().texOffs(40, 58).mirror().addBox(-4.0F, 0.0F, -2.0F, 1, 12, 1),
                PartPose.offsetAndRotation(9.5F, -15.0F, 3.0F, 0.0F, 0.5F, -0.524F));

        partdefinition.addOrReplaceChild("larm2",
                CubeListBuilder.create().texOffs(40, 35).mirror().addBox(-4.0F, 0.0F, -2.0F, 1, 12, 1),
                PartPose.offsetAndRotation(10.5F, -15.0F, 5.0F, 0.0F, 0.5F, -0.524F));

        partdefinition.addOrReplaceChild("lelbow",
                CubeListBuilder.create().texOffs(55, 38).mirror().addBox(0.0F, 0.0F, 0.0F, 2, 2, 4),
                PartPose.offsetAndRotation(10.0F, -3.0F, 3.0F, 0.0F, 0.5F, -0.524F));

        partdefinition.addOrReplaceChild("larm1",
                CubeListBuilder.create().texOffs(56, 53).mirror().addBox(0.0F, 0.0F, -1.0F, 1, 9, 2),
                PartPose.offsetAndRotation(12.0F, -3.0F, 2.5F, 0.0F, -0.6F, -0.3F));

        partdefinition.addOrReplaceChild("scythe1",
                CubeListBuilder.create().texOffs(57, 70).mirror().addBox(0.0F, -39.0F, 1.0F, 1, 39, 1),
                PartPose.offsetAndRotation(-17.0F, 6.0F, -2.0F, 0.0F, 0.0F, 1.0F));

        partdefinition.addOrReplaceChild("scythe2",
                CubeListBuilder.create().texOffs(58, 118).mirror().addBox(0.0F, -39.0F, 1.0F, 16, 6, 0),
                PartPose.offsetAndRotation(-17.0F, 6.0F, -2.0F, 0.0F, 0.0F, 1.0F));

        partdefinition.addOrReplaceChild("scythe3",
                CubeListBuilder.create().texOffs(61, 133).mirror().addBox(9.0F, -34.0F, 1.0F, 7, 5, 0),
                PartPose.offsetAndRotation(-17.0F, 6.0F, -2.0F, 0.0F, 0.0F, 1.0F));

        partdefinition.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(58, 145).mirror().addBox(-3.0F, -6.0F, -3.0F, 6, 6, 5),
                PartPose.offset(0.0F, -16.0F, 4.0F));

        partdefinition.addOrReplaceChild("lwing3",
                CubeListBuilder.create().texOffs(71, 58).mirror().addBox(-0.5F, 0.0F, 0.0F, 1, 19, 3),
                PartPose.offsetAndRotation(4.0F, -11.7F, 8.5F, 2.356F, 0.785F, 0.0F));

        partdefinition.addOrReplaceChild("lwing2",
                CubeListBuilder.create().texOffs(58, 168).mirror().addBox(-0.5F, 11.0F, -2.0F, 1, 19, 3),
                PartPose.offsetAndRotation(4.0F, -23.9F, 8.5F, 1.745F, 0.785F, 0.0F));

        partdefinition.addOrReplaceChild("rwing3",
                CubeListBuilder.create().texOffs(71, 88).mirror().addBox(-0.5F, 0.0F, 0.0F, 1, 19, 3),
                PartPose.offsetAndRotation(-4.0F, -11.7F, 8.5F, 2.356F, -0.785F, 0.0F));

        partdefinition.addOrReplaceChild("rwing2",
                CubeListBuilder.create().texOffs(73, 168).mirror().addBox(-0.5F, 12.0F, -2.0F, 1, 19, 3),
                PartPose.offsetAndRotation(-4.0F, -23.9F, 8.5F, 1.745F, -0.785F, 0.0F));

        return LayerDefinition.create(meshdefinition, 512, 512);
    }

    @Override
    public void setupAnim(EnderReaper entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float newangle = 0.0f;
        newangle = (double)limbSwingAmount > 0.1 ? Mth.cos((float)(ageInTicks * 1.3f * limbSwingAmount)) * (float)Math.PI * 0.25f * limbSwingAmount : 0.0f;
        this.scythe2.zRot = this.scythe1.zRot = 1.0f - Math.abs(newangle);
        this.scythe3.zRot = this.scythe1.zRot;
        if (entity.isScreaming()) {
        newangle = Mth.cos((float)(ageInTicks * 1.9f * limbSwingAmount)) * (float)Math.PI * 0.25f;
        this.scythe2.zRot = this.scythe1.zRot = 1.0f + newangle;
        this.scythe3.zRot = this.scythe1.zRot;
        this.larm1.xRot = -0.436f;
        this.larm1.yRot = -0.488f;
        newangle = Mth.cos((float)(ageInTicks * 2.7f * limbSwingAmount)) * (float)Math.PI * 0.3f;
        } else {
        this.larm1.xRot = -2.436f;
        this.larm1.yRot = 1.0f;
        newangle = Mth.cos((float)(ageInTicks * 0.7f * limbSwingAmount)) * (float)Math.PI * 0.06f;
        }
        this.lwing2.yRot = this.lwing3.yRot = 0.785f + newangle;
        this.lwing1.yRot = this.lwing3.yRot;
        this.rwing2.yRot = this.rwing3.yRot = -0.785f - newangle;
        this.rwing1.yRot = this.rwing3.yRot;
        this.head.yRot = (float)Math.toRadians(netHeadYaw) * 0.45f;
        if (this.head.yRot > 0.45f) {
        this.head.yRot = 0.45f;
        }
        if (this.head.yRot < -0.45f) {
        this.head.yRot = -0.45f;
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.rwing1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lwing1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Shape3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Shape4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Shape5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Shape6.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Shape7.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Shape8.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Shape9.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Shape10.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Shape11.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Shape12.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Shape13.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Shape14.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Shape15.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Shape16.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Shape17.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Shape18.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Shape19.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Shape20.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Shape21.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Shape22.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Shape23.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Shape24.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Shape25.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Shape26.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Shape27.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Shape28.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Shape29.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Shape30.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Shape31.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Shape32.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Shape33.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Shape34.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Shape35.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Shape36.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Shape37.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Shape38.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Shape39.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Shape40.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Shape41.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Shape42.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Shape43.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Shape44.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Shape45.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Shape46.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Shape47.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Shape48.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Shape49.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rarm2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rarm3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.relbow.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rarm1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Shape54.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.larm3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.larm2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lelbow.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.larm1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.scythe1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.scythe2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.scythe3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.head.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lwing3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lwing2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rwing3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rwing2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
