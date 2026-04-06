package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import danger.orespawn.entity.Robot4;

public class ModelRobot4 extends EntityModel<Robot4> {
    private final ModelPart leftfootfront;
    private final ModelPart leftfootbase;
    private final ModelPart leftfootback;
    private final ModelPart leftfoottip;
    private final ModelPart leftshin;
    private final ModelPart leftcalf;
    private final ModelPart leftkneegaurd;
    private final ModelPart leftthigh;
    private final ModelPart rightfootfront;
    private final ModelPart rightfoottip;
    private final ModelPart rightfootbase;
    private final ModelPart rightfootback;
    private final ModelPart rightshin;
    private final ModelPart rightcalf;
    private final ModelPart rightkneegaurd;
    private final ModelPart rightthigh;
    private final ModelPart hips;
    private final ModelPart stomach;
    private final ModelPart chest;
    private final ModelPart neck;
    private final ModelPart head;
    private final ModelPart righttopspinebase;
    private final ModelPart lefttopspinebase;
    private final ModelPart righttopspinetip;
    private final ModelPart lefttopspinetip;
    private final ModelPart middlerightspinebase;
    private final ModelPart middleleftspinebase;
    private final ModelPart middleleftspinetip;
    private final ModelPart middlerightspinetip;
    private final ModelPart torso;
    private final ModelPart rightsholder;
    private final ModelPart leftsholder;
    private final ModelPart rightsholdergaurd;
    private final ModelPart sheildbase;
    private final ModelPart sheildtip;
    private final ModelPart rightupperarm;
    private final ModelPart rightlowerarm;
    private final ModelPart sheildend;
    private final ModelPart leftupperarm;
    private final ModelPart sholdergaurdtip;
    private final ModelPart cannonbase;
    private final ModelPart cannonend;
    private final ModelPart leftcannonpiece;
    private final ModelPart topcannonpiece;
    private final ModelPart rightcannonpiece;
    private final ModelPart bottomcannonpiece;
    private final ModelPart glowycannonbit1;
    private final ModelPart glowycannonbit2;
    private final ModelPart glowycannonbit3;
    private final ModelPart glowycannonbit4;
    private final ModelPart glowycannonbit5;
    private final ModelPart cannonammo;
    private final ModelPart lowerrightspinebase;
    private final ModelPart lowerleftspinebase;
    private final ModelPart lowerrightspinetip;
    private final ModelPart lowerleftspinetip;

    public ModelRobot4(ModelPart root) {
        this.leftfootfront = root.getChild("leftfootfront");
        this.leftfootbase = root.getChild("leftfootbase");
        this.leftfootback = root.getChild("leftfootback");
        this.leftfoottip = root.getChild("leftfoottip");
        this.leftshin = root.getChild("leftshin");
        this.leftcalf = root.getChild("leftcalf");
        this.leftkneegaurd = root.getChild("leftkneegaurd");
        this.leftthigh = root.getChild("leftthigh");
        this.rightfootfront = root.getChild("rightfootfront");
        this.rightfoottip = root.getChild("rightfoottip");
        this.rightfootbase = root.getChild("rightfootbase");
        this.rightfootback = root.getChild("rightfootback");
        this.rightshin = root.getChild("rightshin");
        this.rightcalf = root.getChild("rightcalf");
        this.rightkneegaurd = root.getChild("rightkneegaurd");
        this.rightthigh = root.getChild("rightthigh");
        this.hips = root.getChild("hips");
        this.stomach = root.getChild("stomach");
        this.chest = root.getChild("chest");
        this.neck = root.getChild("neck");
        this.head = root.getChild("head");
        this.righttopspinebase = root.getChild("righttopspinebase");
        this.lefttopspinebase = root.getChild("lefttopspinebase");
        this.righttopspinetip = root.getChild("righttopspinetip");
        this.lefttopspinetip = root.getChild("lefttopspinetip");
        this.middlerightspinebase = root.getChild("middlerightspinebase");
        this.middleleftspinebase = root.getChild("middleleftspinebase");
        this.middleleftspinetip = root.getChild("middleleftspinetip");
        this.middlerightspinetip = root.getChild("middlerightspinetip");
        this.torso = root.getChild("torso");
        this.rightsholder = root.getChild("rightsholder");
        this.leftsholder = root.getChild("leftsholder");
        this.rightsholdergaurd = root.getChild("rightsholdergaurd");
        this.sheildbase = root.getChild("sheildbase");
        this.sheildtip = root.getChild("sheildtip");
        this.rightupperarm = root.getChild("rightupperarm");
        this.rightlowerarm = root.getChild("rightlowerarm");
        this.sheildend = root.getChild("sheildend");
        this.leftupperarm = root.getChild("leftupperarm");
        this.sholdergaurdtip = root.getChild("sholdergaurdtip");
        this.cannonbase = root.getChild("cannonbase");
        this.cannonend = root.getChild("cannonend");
        this.leftcannonpiece = root.getChild("leftcannonpiece");
        this.topcannonpiece = root.getChild("topcannonpiece");
        this.rightcannonpiece = root.getChild("rightcannonpiece");
        this.bottomcannonpiece = root.getChild("bottomcannonpiece");
        this.glowycannonbit1 = root.getChild("glowycannonbit1");
        this.glowycannonbit2 = root.getChild("glowycannonbit2");
        this.glowycannonbit3 = root.getChild("glowycannonbit3");
        this.glowycannonbit4 = root.getChild("glowycannonbit4");
        this.glowycannonbit5 = root.getChild("glowycannonbit5");
        this.cannonammo = root.getChild("cannonammo");
        this.lowerrightspinebase = root.getChild("lowerrightspinebase");
        this.lowerleftspinebase = root.getChild("lowerleftspinebase");
        this.lowerrightspinetip = root.getChild("lowerrightspinetip");
        this.lowerleftspinetip = root.getChild("lowerleftspinetip");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("leftfootfront",
                CubeListBuilder.create().texOffs(20, 50).mirror()
                        .addBox(-6.0F, 22.0F, -9.0F, 8, 5, 7),
                PartPose.offset(-8.0F, -3.0F, 6.0F));

        partdefinition.addOrReplaceChild("leftfootbase",
                CubeListBuilder.create().texOffs(20, 100).mirror()
                        .addBox(-4.0F, 22.0F, -4.0F, 4, 5, 5),
                PartPose.offset(-8.0F, -3.0F, 6.0F));

        partdefinition.addOrReplaceChild("leftfootback",
                CubeListBuilder.create().texOffs(20, 150).mirror()
                        .addBox(-4.5F, 22.0F, 1.0F, 5, 5, 4),
                PartPose.offset(-8.0F, -3.0F, 6.0F));

        partdefinition.addOrReplaceChild("leftfoottip",
                CubeListBuilder.create().texOffs(20, 200).mirror()
                        .addBox(-4.5F, 23.0F, -12.0F, 5, 4, 3),
                PartPose.offset(-8.0F, -3.0F, 6.0F));

        partdefinition.addOrReplaceChild("leftshin",
                CubeListBuilder.create().texOffs(20, 250).mirror()
                        .addBox(-5.0F, 10.0F, -9.0F, 6, 13, 6),
                PartPose.offsetAndRotation(-8.0F, -3.0F, 6.0F, 0.1745329F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("leftcalf",
                CubeListBuilder.create().texOffs(20, 300).mirror()
                        .addBox(-6.0F, 10.0F, -9.0F, 8, 8, 9),
                PartPose.offsetAndRotation(-8.0F, -3.0F, 6.0F, 0.1745329F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("leftkneegaurd",
                CubeListBuilder.create().texOffs(20, 350).mirror()
                        .addBox(-5.5F, 4.0F, -14.0F, 7, 7, 1),
                PartPose.offsetAndRotation(-8.0F, -3.0F, 6.0F, 0.6283185F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("leftthigh",
                CubeListBuilder.create().texOffs(20, 400).mirror()
                        .addBox(-5.0F, 0.0F, -4.0F, 6, 13, 8),
                PartPose.offsetAndRotation(-8.0F, -3.0F, 6.0F, -0.1745329F, 0.1745329F, 0.0F));

        partdefinition.addOrReplaceChild("rightfootfront",
                CubeListBuilder.create().texOffs(20, 450).mirror()
                        .addBox(0.0F, 22.0F, -9.0F, 8, 5, 7),
                PartPose.offset(5.0F, -3.0F, 6.0F));

        partdefinition.addOrReplaceChild("rightfoottip",
                CubeListBuilder.create().texOffs(100, 50).mirror()
                        .addBox(1.5F, 23.0F, -12.0F, 5, 4, 3),
                PartPose.offset(5.0F, -3.0F, 6.0F));

        partdefinition.addOrReplaceChild("rightfootbase",
                CubeListBuilder.create().texOffs(100, 150).mirror()
                        .addBox(2.0F, 22.0F, -4.0F, 4, 5, 5),
                PartPose.offset(5.0F, -3.0F, 6.0F));

        partdefinition.addOrReplaceChild("rightfootback",
                CubeListBuilder.create().texOffs(100, 100).mirror()
                        .addBox(1.5F, 22.0F, 1.0F, 5, 5, 4),
                PartPose.offset(5.0F, -3.0F, 6.0F));

        partdefinition.addOrReplaceChild("rightshin",
                CubeListBuilder.create().texOffs(100, 200).mirror()
                        .addBox(1.0F, 10.0F, -9.0F, 6, 13, 6),
                PartPose.offsetAndRotation(5.0F, -3.0F, 6.0F, 0.1745329F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("rightcalf",
                CubeListBuilder.create().texOffs(100, 250).mirror()
                        .addBox(0.0F, 10.0F, -10.0F, 8, 8, 9),
                PartPose.offsetAndRotation(5.0F, -3.0F, 6.0F, 0.1745329F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("rightkneegaurd",
                CubeListBuilder.create().texOffs(100, 300).mirror()
                        .addBox(0.5F, 4.0F, -15.0F, 7, 7, 1),
                PartPose.offsetAndRotation(5.0F, -3.0F, 6.0F, 0.6283185F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("rightthigh",
                CubeListBuilder.create().texOffs(100, 400)
                        .addBox(0.0F, 0.0F, -5.0F, 6, 13, 8),
                PartPose.offsetAndRotation(5.0F, -3.0F, 6.0F, -0.1745329F, -0.1745329F, 0.0F));

        partdefinition.addOrReplaceChild("hips",
                CubeListBuilder.create().texOffs(100, 350).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 14, 7, 8),
                PartPose.offsetAndRotation(-8.0F, -3.0F, 2.0F, 0.1396263F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("stomach",
                CubeListBuilder.create().texOffs(100, 450).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 12, 6, 7),
                PartPose.offsetAndRotation(-7.0F, -9.0F, 2.0F, 0.1396263F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("chest",
                CubeListBuilder.create().texOffs(200, 50).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 18, 12, 13),
                PartPose.offsetAndRotation(-10.0F, -23.0F, -4.0F, 0.2443461F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("neck",
                CubeListBuilder.create().texOffs(200, 100).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 6, 7, 6),
                PartPose.offsetAndRotation(-4.0F, -22.0F, -7.0F, 0.8726646F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(200, 150).mirror()
                        .addBox(-3.0F, -3.0F, -5.0F, 6, 6, 8),
                PartPose.offsetAndRotation(-1.0F, -26.0F, -5.0F, 0.5235988F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("righttopspinebase",
                CubeListBuilder.create().texOffs(200, 200).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 2, 8, 2),
                PartPose.offsetAndRotation(3.0F, -29.0F, 5.0F, -0.1396263F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("lefttopspinebase",
                CubeListBuilder.create().texOffs(200, 250).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 2, 8, 2),
                PartPose.offsetAndRotation(-7.0F, -29.0F, 5.0F, -0.1396263F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("righttopspinetip",
                CubeListBuilder.create().texOffs(200, 300).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 1, 8, 1),
                PartPose.offsetAndRotation(3.5F, -35.0F, 8.0F, -0.3316126F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("lefttopspinetip",
                CubeListBuilder.create().texOffs(200, 350).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 1, 8, 1),
                PartPose.offsetAndRotation(-6.5F, -35.0F, 8.0F, -0.3316126F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("middlerightspinebase",
                CubeListBuilder.create().texOffs(200, 400).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 2, 8, 2),
                PartPose.offsetAndRotation(-6.0F, -25.0F, 14.0F, -0.6981317F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("middleleftspinebase",
                CubeListBuilder.create().texOffs(200, 450).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 2, 8, 2),
                PartPose.offsetAndRotation(2.0F, -25.0F, 14.0F, -0.6981317F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("middleleftspinetip",
                CubeListBuilder.create().texOffs(300, 50).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 1, 7, 1),
                PartPose.offsetAndRotation(2.5F, -28.0F, 18.0F, -0.7853982F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("middlerightspinetip",
                CubeListBuilder.create().texOffs(300, 100).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 1, 7, 1),
                PartPose.offsetAndRotation(-5.5F, -28.0F, 18.0F, -0.7853982F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("torso",
                CubeListBuilder.create().texOffs(300, 150).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 14, 6, 10),
                PartPose.offsetAndRotation(-8.0F, -13.0F, 0.0F, 0.1396263F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("rightsholder",
                CubeListBuilder.create().texOffs(300, 200).mirror()
                        .addBox(0.0F, -3.0F, -3.0F, 6, 6, 6),
                PartPose.offset(7.0F, -18.0F, 4.0F));

        partdefinition.addOrReplaceChild("leftsholder",
                CubeListBuilder.create().texOffs(300, 250).mirror()
                        .addBox(-6.0F, -3.0F, -3.0F, 6, 6, 6),
                PartPose.offset(-9.0F, -18.0F, 4.0F));

        partdefinition.addOrReplaceChild("rightsholdergaurd",
                CubeListBuilder.create().texOffs(300, 300).mirror()
                        .addBox(8.0F, -4.0F, -4.0F, 4, 12, 9),
                PartPose.offsetAndRotation(7.0F, -18.0F, 4.0F, -0.2094395F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("sheildbase",
                CubeListBuilder.create().texOffs(300, 350).mirror()
                        .addBox(8.0F, -4.0F, -30.0F, 3, 12, 19),
                PartPose.offsetAndRotation(7.0F, -18.0F, 4.0F, 1.047198F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("sheildtip",
                CubeListBuilder.create().texOffs(300, 400).mirror()
                        .addBox(9.0F, -2.0F, -34.0F, 3, 8, 4),
                PartPose.offsetAndRotation(6.0F, -18.0F, 4.0F, 1.047198F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("rightupperarm",
                CubeListBuilder.create().texOffs(300, 450).mirror()
                        .addBox(3.0F, -1.0F, -4.0F, 6, 13, 6),
                PartPose.offsetAndRotation(7.0F, -18.0F, 4.0F, -0.2094395F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("rightlowerarm",
                CubeListBuilder.create().texOffs(350, 50).mirror()
                        .addBox(3.0F, 0.0F, -25.0F, 6, 6, 14),
                PartPose.offsetAndRotation(7.0F, -18.0F, 4.0F, 1.047198F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("sheildend",
                CubeListBuilder.create().texOffs(350, 100).mirror()
                        .addBox(8.0F, -1.0F, -11.0F, 3, 8, 4),
                PartPose.offsetAndRotation(7.0F, -18.0F, 4.0F, 1.047198F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("leftupperarm",
                CubeListBuilder.create().texOffs(350, 200).mirror()
                        .addBox(-9.0F, -1.0F, -4.0F, 6, 15, 6),
                PartPose.offsetAndRotation(-9.0F, -18.0F, 4.0F, -0.2094395F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("sholdergaurdtip",
                CubeListBuilder.create().texOffs(350, 250).mirror()
                        .addBox(10.0F, -3.0F, -7.0F, 2, 5, 3),
                PartPose.offsetAndRotation(7.0F, -18.0F, 4.0F, -0.2094395F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("cannonbase",
                CubeListBuilder.create().texOffs(350, 300).mirror()
                        .addBox(-4.0F, 0.0F, -4.0F, 8, 12, 8),
                PartPose.offsetAndRotation(-15.0F, -5.0F, 1.0F, -0.6981317F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("cannonend",
                CubeListBuilder.create().texOffs(350, 400).mirror()
                        .addBox(-3.0F, 11.0F, -3.0F, 6, 4, 6),
                PartPose.offsetAndRotation(-15.0F, -5.0F, 1.0F, -0.6981317F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("leftcannonpiece",
                CubeListBuilder.create().texOffs(20, 20).mirror()
                        .addBox(-5.0F, 11.0F, -1.5F, 3, 6, 3),
                PartPose.offsetAndRotation(-15.0F, -5.0F, 1.0F, -0.6981317F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("topcannonpiece",
                CubeListBuilder.create().texOffs(40, 20).mirror()
                        .addBox(-1.5F, 11.0F, -5.0F, 3, 6, 3),
                PartPose.offsetAndRotation(-15.0F, -5.0F, 1.0F, -0.6981317F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("rightcannonpiece",
                CubeListBuilder.create().texOffs(80, 20).mirror()
                        .addBox(2.0F, 11.0F, -1.5F, 3, 6, 3),
                PartPose.offsetAndRotation(-15.0F, -5.0F, 1.0F, -0.6981317F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("bottomcannonpiece",
                CubeListBuilder.create().texOffs(100, 20).mirror()
                        .addBox(-1.5F, 11.0F, 2.0F, 3, 6, 3),
                PartPose.offsetAndRotation(-15.0F, -5.0F, 1.0F, -0.6981317F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("glowycannonbit1",
                CubeListBuilder.create().texOffs(150, 20).mirror()
                        .addBox(-3.5F, 0.0F, -11.0F, 2, 5, 2),
                PartPose.offsetAndRotation(-15.0F, -5.0F, 1.0F, 0.1745329F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("glowycannonbit2",
                CubeListBuilder.create().texOffs(200, 20).mirror()
                        .addBox(1.5F, 0.0F, -11.0F, 2, 5, 2),
                PartPose.offsetAndRotation(-15.0F, -5.0F, 1.0F, 0.1745329F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("glowycannonbit3",
                CubeListBuilder.create().texOffs(250, 20).mirror()
                        .addBox(-3.0F, -2.0F, -8.0F, 2, 5, 2),
                PartPose.offsetAndRotation(-15.0F, -5.0F, 1.0F, 0.0872665F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("glowycannonbit4",
                CubeListBuilder.create().texOffs(300, 20).mirror()
                        .addBox(1.0F, -2.0F, -8.0F, 2, 5, 2),
                PartPose.offsetAndRotation(-15.0F, -5.0F, 1.0F, 0.0872665F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("glowycannonbit5",
                CubeListBuilder.create().texOffs(350, 20).mirror()
                        .addBox(-1.0F, -5.0F, -5.0F, 2, 5, 2),
                PartPose.offset(-15.0F, -5.0F, 1.0F));

        partdefinition.addOrReplaceChild("cannonammo",
                CubeListBuilder.create().texOffs(400, 400).mirror()
                        .addBox(-6.0F, 3.0F, 0.0F, 5, 5, 5),
                PartPose.offsetAndRotation(-15.0F, -5.0F, 1.0F, -0.6981317F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("lowerrightspinebase",
                CubeListBuilder.create().texOffs(400, 450).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 2, 8, 2),
                PartPose.offsetAndRotation(4.0F, -19.0F, 15.0F, -1.047198F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("lowerleftspinebase",
                CubeListBuilder.create().texOffs(360, 450).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 2, 8, 2),
                PartPose.offsetAndRotation(-8.0F, -19.0F, 15.0F, -1.047198F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("lowerrightspinetip",
                CubeListBuilder.create().texOffs(250, 100).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 1, 7, 1),
                PartPose.offsetAndRotation(4.5F, -21.0F, 20.0F, -1.134464F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("lowerleftspinetip",
                CubeListBuilder.create().texOffs(150, 100).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 1, 7, 1),
                PartPose.offsetAndRotation(-7.5F, -21.0F, 20.0F, -1.134464F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 512, 512);
    }

    @Override
    public void setupAnim(Robot4 entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float walkAngle = limbSwingAmount > 0.1F
                ? Mth.cos(limbSwing * 0.5F) * (float) Math.PI * 0.15F * limbSwingAmount
                : 0.0F;

        this.leftfootfront.xRot = walkAngle;
        this.leftfootbase.xRot = walkAngle;
        this.leftfootback.xRot = walkAngle;
        this.leftfoottip.xRot = walkAngle;
        this.leftshin.xRot = walkAngle + 0.175F;
        this.leftcalf.xRot = walkAngle + 0.175F;
        this.leftkneegaurd.xRot = walkAngle + 0.63F;
        this.leftthigh.xRot = walkAngle - 0.175F;

        this.rightfootfront.xRot = -walkAngle;
        this.rightfoottip.xRot = -walkAngle;
        this.rightfootbase.xRot = -walkAngle;
        this.rightfootback.xRot = -walkAngle;
        this.rightshin.xRot = -walkAngle + 0.175F;
        this.rightcalf.xRot = -walkAngle + 0.175F;
        this.rightkneegaurd.xRot = -walkAngle + 0.63F;
        this.rightthigh.xRot = -walkAngle - 0.175F;

        this.head.yRot = (float) Math.toRadians(netHeadYaw / 1.5);

        float armAngle = Mth.cos((float) Math.toRadians(ageInTicks % 360.0F) * 6.0F) * 0.7853982F;
        armAngle = Math.abs(armAngle) + 0.75F;

        this.rightsholder.xRot = -armAngle;
        this.rightsholdergaurd.xRot = -armAngle - 0.21F;
        this.sheildbase.xRot = -armAngle + 1.047F;
        this.sheildtip.xRot = -armAngle + 1.047F;
        this.rightupperarm.xRot = -armAngle - 0.21F;
        this.rightlowerarm.xRot = -armAngle + 1.047F;
        this.sheildend.xRot = -armAngle + 1.04F;
        this.sholdergaurdtip.xRot = -armAngle - 0.21F;

        float cannonAngle = 0.85F;
        this.leftsholder.xRot = -cannonAngle;
        this.leftupperarm.xRot = -cannonAngle - 0.21F;
        this.cannonbase.xRot = -cannonAngle - 0.7F;
        this.cannonend.xRot = -cannonAngle - 0.7F;
        this.leftcannonpiece.xRot = -cannonAngle - 0.7F;
        this.topcannonpiece.xRot = -cannonAngle - 0.7F;
        this.rightcannonpiece.xRot = -cannonAngle - 0.7F;
        this.bottomcannonpiece.xRot = -cannonAngle - 0.7F;
        this.glowycannonbit1.xRot = -cannonAngle + 0.17F;
        this.glowycannonbit2.xRot = -cannonAngle + 0.17F;
        this.glowycannonbit3.xRot = -cannonAngle + 0.08F;
        this.glowycannonbit4.xRot = -cannonAngle + 0.08F;
        this.glowycannonbit5.xRot = -cannonAngle;
        this.cannonammo.xRot = -cannonAngle - 0.7F;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.leftfootfront.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leftfootbase.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leftfootback.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leftfoottip.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leftshin.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leftcalf.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leftkneegaurd.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leftthigh.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rightfootfront.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rightfoottip.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rightfootbase.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rightfootback.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rightshin.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rightcalf.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rightkneegaurd.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rightthigh.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.hips.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.stomach.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.chest.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.neck.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.head.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.righttopspinebase.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lefttopspinebase.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.righttopspinetip.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lefttopspinetip.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.middlerightspinebase.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.middleleftspinebase.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.middleleftspinetip.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.middlerightspinetip.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.torso.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rightsholder.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leftsholder.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rightsholdergaurd.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.sheildbase.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.sheildtip.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rightupperarm.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rightlowerarm.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.sheildend.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leftupperarm.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.sholdergaurdtip.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.cannonbase.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.cannonend.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leftcannonpiece.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.topcannonpiece.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rightcannonpiece.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.bottomcannonpiece.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.glowycannonbit1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.glowycannonbit2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.glowycannonbit3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.glowycannonbit4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.glowycannonbit5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.cannonammo.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lowerrightspinebase.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lowerleftspinebase.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lowerrightspinetip.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lowerleftspinetip.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
