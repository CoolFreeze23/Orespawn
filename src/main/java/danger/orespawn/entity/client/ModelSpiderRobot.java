package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.SpiderRobot;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class ModelSpiderRobot extends EntityModel<SpiderRobot> {
    private final ModelPart Leg1p1;
    private final ModelPart Leg1p2;
    private final ModelPart Leg1p3;
    private final ModelPart Foot;
    private final ModelPart FootSpike1;
    private final ModelPart FootSpike2;
    private final ModelPart FootSpike3;
    private final ModelPart FootSpike4;
    private final ModelPart AnkleSpike1;
    private final ModelPart AnkleSpike2;
    private final ModelPart AnkleSpike3;
    private final ModelPart AnkleSpike4;
    private final ModelPart LowerKnee;
    private final ModelPart UpperKnee;
    private final ModelPart LegBump1;
    private final ModelPart LegBump2;
    private final ModelPart LowerKnee2;
    private final ModelPart UpperKnee2;
    private final ModelPart HipJoint;
    private final ModelPart BodyCenter;
    private final ModelPart Abdomen;
    private final ModelPart Head;
    private final ModelPart Ljaw1;
    private final ModelPart Rjaw1;
    private final ModelPart Ljaw2;
    private final ModelPart Rjaw2;
    private final ModelPart Ljaw3;
    private final ModelPart Rjaw3;
    private final ModelPart Tail;
    private final ModelPart HeadSpike1;
    private final ModelPart HeadSpike2;
    private final ModelPart Hip1;
    private final ModelPart Hip2;
    private final ModelPart Hip3;
    private final ModelPart Hip4;
    private final ModelPart Hip5;
    private final ModelPart Hip6;
    private final ModelPart Hip7;
    private final ModelPart Hip8;

    public ModelSpiderRobot(ModelPart root) {
        this.Leg1p1 = root.getChild("Leg1p1");
        this.Leg1p2 = root.getChild("Leg1p2");
        this.Leg1p3 = root.getChild("Leg1p3");
        this.Foot = root.getChild("Foot");
        this.FootSpike1 = root.getChild("FootSpike1");
        this.FootSpike2 = root.getChild("FootSpike2");
        this.FootSpike3 = root.getChild("FootSpike3");
        this.FootSpike4 = root.getChild("FootSpike4");
        this.AnkleSpike1 = root.getChild("AnkleSpike1");
        this.AnkleSpike2 = root.getChild("AnkleSpike2");
        this.AnkleSpike3 = root.getChild("AnkleSpike3");
        this.AnkleSpike4 = root.getChild("AnkleSpike4");
        this.LowerKnee = root.getChild("LowerKnee");
        this.UpperKnee = root.getChild("UpperKnee");
        this.LegBump1 = root.getChild("LegBump1");
        this.LegBump2 = root.getChild("LegBump2");
        this.LowerKnee2 = root.getChild("LowerKnee2");
        this.UpperKnee2 = root.getChild("UpperKnee2");
        this.HipJoint = root.getChild("HipJoint");
        this.BodyCenter = root.getChild("BodyCenter");
        this.Abdomen = root.getChild("Abdomen");
        this.Head = root.getChild("Head");
        this.Ljaw1 = root.getChild("Ljaw1");
        this.Rjaw1 = root.getChild("Rjaw1");
        this.Ljaw2 = root.getChild("Ljaw2");
        this.Rjaw2 = root.getChild("Rjaw2");
        this.Ljaw3 = root.getChild("Ljaw3");
        this.Rjaw3 = root.getChild("Rjaw3");
        this.Tail = root.getChild("Tail");
        this.HeadSpike1 = root.getChild("HeadSpike1");
        this.HeadSpike2 = root.getChild("HeadSpike2");
        this.Hip1 = root.getChild("Hip1");
        this.Hip2 = root.getChild("Hip2");
        this.Hip3 = root.getChild("Hip3");
        this.Hip4 = root.getChild("Hip4");
        this.Hip5 = root.getChild("Hip5");
        this.Hip6 = root.getChild("Hip6");
        this.Hip7 = root.getChild("Hip7");
        this.Hip8 = root.getChild("Hip8");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("Leg1p1",
                CubeListBuilder.create().texOffs(0, 149).mirror().addBox(-2.0F, -2.0F, 0.0F, 4, 4, 100),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.7853982F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("Leg1p2",
                CubeListBuilder.create().texOffs(0, 149).mirror().addBox(-1.5F, -1.5F, 0.0F, 3, 3, 100),
                PartPose.offset(0.0F, -70.0F, 70.0F));

        partdefinition.addOrReplaceChild("Leg1p3",
                CubeListBuilder.create().texOffs(0, 149).mirror().addBox(-1.0F, -1.0F, 0.0F, 2, 2, 100),
                PartPose.offsetAndRotation(0.0F, -70.0F, 169.0F, -0.7853982F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("Foot",
                CubeListBuilder.create().texOffs(0, 28).mirror().addBox(-3.0F, -3.0F, 93.0F, 6, 6, 6),
                PartPose.offsetAndRotation(0.0F, -70.0F, 169.0F, -0.7853982F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("FootSpike1",
                CubeListBuilder.create().texOffs(29, 27).mirror().addBox(2.0F, 2.0F, 99.0F, 1, 1, 5),
                PartPose.offsetAndRotation(0.0F, -70.0F, 169.0F, -0.7853982F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("FootSpike2",
                CubeListBuilder.create().texOffs(29, 34).mirror().addBox(-3.0F, 2.0F, 99.0F, 1, 1, 5),
                PartPose.offsetAndRotation(0.0F, -70.0F, 169.0F, -0.7853982F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("FootSpike3",
                CubeListBuilder.create().texOffs(43, 27).mirror().addBox(2.0F, -3.0F, 99.0F, 1, 1, 5),
                PartPose.offsetAndRotation(0.0F, -70.0F, 169.0F, -0.7853982F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("FootSpike4",
                CubeListBuilder.create().texOffs(43, 34).mirror().addBox(-3.0F, -3.0F, 99.0F, 1, 1, 5),
                PartPose.offsetAndRotation(0.0F, -70.0F, 169.0F, -0.7853982F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("AnkleSpike1",
                CubeListBuilder.create().texOffs(1, 42).mirror().addBox(3.0F, -10.0F, 92.0F, 1, 20, 1),
                PartPose.offsetAndRotation(0.0F, -70.0F, 169.0F, -0.7853982F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("AnkleSpike2",
                CubeListBuilder.create().texOffs(7, 42).mirror().addBox(-4.0F, -10.0F, 92.0F, 1, 20, 1),
                PartPose.offsetAndRotation(0.0F, -70.0F, 169.0F, -0.7853982F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("AnkleSpike3",
                CubeListBuilder.create().texOffs(14, 42).mirror().addBox(-10.0F, 3.0F, 92.0F, 20, 1, 1),
                PartPose.offsetAndRotation(0.0F, -70.0F, 169.0F, -0.7853982F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("AnkleSpike4",
                CubeListBuilder.create().texOffs(14, 46).mirror().addBox(-10.0F, -4.0F, 92.0F, 20, 1, 1),
                PartPose.offsetAndRotation(0.0F, -70.0F, 169.0F, -0.7853982F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("LowerKnee",
                CubeListBuilder.create().texOffs(14, 49).mirror().addBox(-1.5F, -1.5F, -1.0F, 3, 3, 15),
                PartPose.offsetAndRotation(0.0F, -70.0F, 169.0F, -0.7853982F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("UpperKnee",
                CubeListBuilder.create().texOffs(0, 69).mirror().addBox(-2.5F, -2.5F, 81.0F, 5, 5, 20),
                PartPose.offset(0.0F, -70.0F, 70.0F));

        partdefinition.addOrReplaceChild("LegBump1",
                CubeListBuilder.create().texOffs(52, 50).mirror().addBox(-0.5F, -2.0F, 80.0F, 1, 1, 1),
                PartPose.offsetAndRotation(0.0F, -70.0F, 169.0F, -0.7853982F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("LegBump2",
                CubeListBuilder.create().texOffs(52, 54).mirror().addBox(-0.5F, -2.0F, 70.0F, 1, 1, 1),
                PartPose.offsetAndRotation(0.0F, -70.0F, 169.0F, -0.7853982F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("LowerKnee2",
                CubeListBuilder.create().texOffs(0, 96).mirror().addBox(-2.5F, -2.5F, -1.0F, 5, 5, 15),
                PartPose.offset(0.0F, -70.0F, 70.0F));

        partdefinition.addOrReplaceChild("UpperKnee2",
                CubeListBuilder.create().texOffs(0, 119).mirror().addBox(-3.0F, -3.0F, 81.0F, 6, 6, 20),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.7853982F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("HipJoint",
                CubeListBuilder.create().texOffs(0, 149).mirror().addBox(-4.0F, -4.0F, 0.0F, 8, 8, 16),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.7853982F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("BodyCenter",
                CubeListBuilder.create().texOffs(0, 321).mirror().addBox(-18.0F, -12.0F, -21.0F, 36, 24, 51),
                PartPose.offset(0.0F, -4.0F, 0.0F));

        partdefinition.addOrReplaceChild("Abdomen",
                CubeListBuilder.create().texOffs(0, 398).mirror().addBox(-24.0F, -30.0F, 29.0F, 48, 40, 73),
                PartPose.offset(0.0F, -4.0F, 0.0F));

        partdefinition.addOrReplaceChild("Head",
                CubeListBuilder.create().texOffs(0, 256).mirror().addBox(-15.0F, -16.0F, -57.0F, 30, 26, 36),
                PartPose.offset(0.0F, -4.0F, 0.0F));

        partdefinition.addOrReplaceChild("Ljaw1",
                CubeListBuilder.create().texOffs(75, 26).mirror().addBox(-4.0F, 0.0F, -4.0F, 8, 3, 8),
                PartPose.offset(14.0F, -3.0F, -56.0F));

        partdefinition.addOrReplaceChild("Rjaw1",
                CubeListBuilder.create().texOffs(75, 26).mirror().addBox(-4.0F, 0.0F, -4.0F, 8, 3, 8),
                PartPose.offset(-14.0F, -3.0F, -56.0F));

        partdefinition.addOrReplaceChild("Ljaw2",
                CubeListBuilder.create().texOffs(63, 40).mirror().addBox(0.0F, 1.0F, -3.0F, 21, 2, 6),
                PartPose.offsetAndRotation(14.0F, -3.0F, -56.0F, 0.0F, 0.7504916F, 0.0F));

        partdefinition.addOrReplaceChild("Rjaw2",
                CubeListBuilder.create().texOffs(63, 40).mirror().addBox(0.0F, 1.0F, -3.0F, 21, 2, 6),
                PartPose.offsetAndRotation(-14.0F, -3.0F, -56.0F, 0.0F, 2.303835F, 0.0F));

        partdefinition.addOrReplaceChild("Ljaw3",
                CubeListBuilder.create().texOffs(0, 18).mirror().addBox(11.0F, 2.0F, 14.0F, 23, 1, 4),
                PartPose.offsetAndRotation(14.0F, -3.0F, -56.0F, 0.0F, 1.710423F, 0.0F));

        partdefinition.addOrReplaceChild("Rjaw3",
                CubeListBuilder.create().texOffs(0, 18).mirror().addBox(11.0F, 2.0F, -17.0F, 23, 1, 4),
                PartPose.offsetAndRotation(-14.0F, -3.0F, -56.0F, 0.0F, 1.413717F, 0.0F));

        partdefinition.addOrReplaceChild("Tail",
                CubeListBuilder.create().texOffs(130, 0).mirror().addBox(-5.0F, -5.0F, -5.0F, 10, 10, 49),
                PartPose.offset(0.0F, -32.0F, 69.0F));

        partdefinition.addOrReplaceChild("HeadSpike1",
                CubeListBuilder.create().texOffs(74, 0).mirror().addBox(-1.0F, -1.0F, -10.0F, 2, 2, 21),
                PartPose.offset(6.0F, -20.0F, -60.0F));

        partdefinition.addOrReplaceChild("HeadSpike2",
                CubeListBuilder.create().texOffs(74, 0).mirror().addBox(-1.0F, -1.0F, -10.0F, 2, 2, 21),
                PartPose.offset(-6.0F, -20.0F, -60.0F));

        partdefinition.addOrReplaceChild("Hip1",
                CubeListBuilder.create().texOffs(70, 60).mirror().addBox(0.0F, 0.0F, 0.0F, 10, 10, 10),
                PartPose.offset(22.0F, -3.0F, 44.0F));

        partdefinition.addOrReplaceChild("Hip2",
                CubeListBuilder.create().texOffs(70, 60).mirror().addBox(0.0F, 0.0F, 0.0F, 10, 10, 10),
                PartPose.offset(-32.0F, -3.0F, 44.0F));

        partdefinition.addOrReplaceChild("Hip3",
                CubeListBuilder.create().texOffs(70, 60).mirror().addBox(0.0F, 0.0F, 0.0F, 10, 10, 10),
                PartPose.offset(16.0F, -1.0F, 12.0F));

        partdefinition.addOrReplaceChild("Hip4",
                CubeListBuilder.create().texOffs(70, 60).mirror().addBox(0.0F, 0.0F, 0.0F, 10, 10, 10),
                PartPose.offset(-26.0F, -1.0F, 12.0F));

        partdefinition.addOrReplaceChild("Hip5",
                CubeListBuilder.create().texOffs(70, 60).mirror().addBox(0.0F, 0.0F, 0.0F, 10, 10, 10),
                PartPose.offset(16.0F, -1.0F, -11.0F));

        partdefinition.addOrReplaceChild("Hip6",
                CubeListBuilder.create().texOffs(70, 60).mirror().addBox(0.0F, 0.0F, 0.0F, 10, 10, 10),
                PartPose.offset(-26.0F, -1.0F, -11.0F));

        partdefinition.addOrReplaceChild("Hip7",
                CubeListBuilder.create().texOffs(70, 60).mirror().addBox(0.0F, 0.0F, 0.0F, 10, 10, 10),
                PartPose.offset(13.0F, -3.0F, -33.0F));

        partdefinition.addOrReplaceChild("Hip8",
                CubeListBuilder.create().texOffs(70, 60).mirror().addBox(0.0F, 0.0F, 0.0F, 10, 10, 10),
                PartPose.offset(-23.0F, -3.0F, -33.0F));

        return LayerDefinition.create(meshdefinition, 256, 512);
    }

    @Override
    public void setupAnim(SpiderRobot entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        RenderSpiderRobotInfo r = null;
        r = entity.getRenderSpiderRobotInfo();
        for (int i = 0; i < 8; ++i) {
        this.Leg1p2.yRot = this.Leg1p3.yRot = r.ydisplayangle[i];
        this.Leg1p1.yRot = this.Leg1p3.yRot;
        this.Foot.yRot = r.ydisplayangle[i];
        this.FootSpike1.yRot = r.ydisplayangle[i];
        this.FootSpike2.yRot = r.ydisplayangle[i];
        this.FootSpike3.yRot = r.ydisplayangle[i];
        this.FootSpike4.yRot = r.ydisplayangle[i];
        this.AnkleSpike1.yRot = r.ydisplayangle[i];
        this.AnkleSpike2.yRot = r.ydisplayangle[i];
        this.AnkleSpike3.yRot = r.ydisplayangle[i];
        this.AnkleSpike4.yRot = r.ydisplayangle[i];
        this.LowerKnee.yRot = r.ydisplayangle[i];
        this.UpperKnee.yRot = r.ydisplayangle[i];
        this.LegBump1.yRot = r.ydisplayangle[i];
        this.LegBump2.yRot = r.ydisplayangle[i];
        this.LowerKnee2.yRot = r.ydisplayangle[i];
        this.UpperKnee2.yRot = r.ydisplayangle[i];
        this.HipJoint.yRot = r.ydisplayangle[i];
        this.UpperKnee2.xRot = this.Leg1p1.xRot = (float)r.p1xangle[i] + r.uddisplayangle[i];
        this.HipJoint.xRot = this.Leg1p1.xRot;
        this.UpperKnee.xRot = this.Leg1p2.xRot = (float)r.p2xangle[i] + r.uddisplayangle[i];
        this.LowerKnee2.xRot = this.Leg1p2.xRot;
        this.Foot.xRot = this.Leg1p3.xRot = (float)r.p3xangle[i] + r.uddisplayangle[i];
        this.FootSpike1.xRot = this.Leg1p3.xRot;
        this.FootSpike2.xRot = this.Leg1p3.xRot;
        this.FootSpike3.xRot = this.Leg1p3.xRot;
        this.FootSpike4.xRot = this.Leg1p3.xRot;
        this.AnkleSpike1.xRot = this.Leg1p3.xRot;
        this.AnkleSpike2.xRot = this.Leg1p3.xRot;
        this.AnkleSpike3.xRot = this.Leg1p3.xRot;
        this.AnkleSpike4.xRot = this.Leg1p3.xRot;
        this.LegBump1.xRot = this.Leg1p3.xRot;
        this.LegBump2.xRot = this.Leg1p3.xRot;
        this.LowerKnee.xRot = this.Leg1p3.xRot;
        this.Leg1p1.x = -((float)Math.cos(r.ymid[i])) * r.legoff[i] * 16.0f;
        this.Leg1p1.z = (float)Math.sin(r.ymid[i]) * r.legoff[i] * 16.0f;
        this.Leg1p1.y = r.yoff[i] * -16.0f;
        this.UpperKnee2.x = this.Leg1p1.x;
        this.UpperKnee2.y = this.Leg1p1.y;
        this.UpperKnee2.z = this.Leg1p1.z;
        this.HipJoint.x = this.Leg1p1.x;
        this.HipJoint.y = this.Leg1p1.y;
        this.HipJoint.z = this.Leg1p1.z;
        this.Leg1p2.y = this.Leg1p1.y - (float)Math.sin(this.Leg1p1.xRot) * 99.0f;
        this.Leg1p2.z = this.Leg1p1.z + (float)Math.cos(this.Leg1p1.xRot) * (float)Math.cos(this.Leg1p1.yRot) * 99.0f;
        this.UpperKnee.x = this.Leg1p2.x = this.Leg1p1.x + (float)Math.cos(this.Leg1p1.xRot) * (float)Math.sin(this.Leg1p1.yRot) * 99.0f;
        this.UpperKnee.y = this.Leg1p2.y;
        this.UpperKnee.z = this.Leg1p2.z;
        this.LowerKnee2.x = this.Leg1p2.x;
        this.LowerKnee2.y = this.Leg1p2.y;
        this.LowerKnee2.z = this.Leg1p2.z;
        this.Leg1p3.y = this.Leg1p2.y - (float)Math.sin(this.Leg1p2.xRot) * 99.0f;
        this.Leg1p3.z = this.Leg1p2.z + (float)Math.cos(this.Leg1p2.xRot) * (float)Math.cos(this.Leg1p2.yRot) * 99.0f;
        this.Foot.x = this.Leg1p3.x = this.Leg1p2.x + (float)Math.cos(this.Leg1p2.xRot) * (float)Math.sin(this.Leg1p2.yRot) * 99.0f;
        this.Foot.y = this.Leg1p3.y;
        this.Foot.z = this.Leg1p3.z;
        this.FootSpike1.x = this.Leg1p3.x;
        this.FootSpike1.y = this.Leg1p3.y;
        this.FootSpike1.z = this.Leg1p3.z;
        this.FootSpike2.x = this.Leg1p3.x;
        this.FootSpike2.y = this.Leg1p3.y;
        this.FootSpike2.z = this.Leg1p3.z;
        this.FootSpike3.x = this.Leg1p3.x;
        this.FootSpike3.y = this.Leg1p3.y;
        this.FootSpike3.z = this.Leg1p3.z;
        this.FootSpike4.x = this.Leg1p3.x;
        this.FootSpike4.y = this.Leg1p3.y;
        this.FootSpike4.z = this.Leg1p3.z;
        this.AnkleSpike1.x = this.Leg1p3.x;
        this.AnkleSpike1.y = this.Leg1p3.y;
        this.AnkleSpike1.z = this.Leg1p3.z;
        this.AnkleSpike2.x = this.Leg1p3.x;
        this.AnkleSpike2.y = this.Leg1p3.y;
        this.AnkleSpike2.z = this.Leg1p3.z;
        this.AnkleSpike3.x = this.Leg1p3.x;
        this.AnkleSpike3.y = this.Leg1p3.y;
        this.AnkleSpike3.z = this.Leg1p3.z;
        this.AnkleSpike4.x = this.Leg1p3.x;
        this.AnkleSpike4.y = this.Leg1p3.y;
        this.AnkleSpike4.z = this.Leg1p3.z;
        this.LegBump1.x = this.Leg1p3.x;
        this.LegBump1.y = this.Leg1p3.y;
        this.LegBump1.z = this.Leg1p3.z;
        this.LegBump2.x = this.Leg1p3.x;
        this.LegBump2.y = this.Leg1p3.y;
        this.LegBump2.z = this.Leg1p3.z;
        this.LowerKnee.x = this.Leg1p3.x;
        this.LowerKnee.y = this.Leg1p3.y;
        this.LowerKnee.z = this.Leg1p3.z;
        }
        if (entity.getAttacking() == 0) {
        this.Ljaw1.yRot = 0.0f;
        this.Ljaw2.yRot = 0.75f;
        this.Ljaw3.yRot = 1.71f;
        this.Rjaw1.yRot = 0.0f;
        this.Rjaw2.yRot = 2.3f;
        this.Rjaw3.yRot = 1.41f;
        } else {
        float newangle;
        this.Ljaw1.yRot = newangle = Mth.cos((float)((float)r.gpcounter * 0.25f)) * (float)Math.PI * 0.22f;
        this.Ljaw2.yRot = newangle + 0.75f;
        this.Ljaw3.yRot = newangle + 1.71f;
        this.Rjaw1.yRot = -newangle;
        this.Rjaw2.yRot = 2.3f - newangle;
        this.Rjaw3.yRot = 1.41f - newangle;
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.Leg1p1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Leg1p2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Leg1p3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Foot.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.FootSpike1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.FootSpike2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.FootSpike3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.FootSpike4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.AnkleSpike1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.AnkleSpike2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.AnkleSpike3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.AnkleSpike4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.LowerKnee.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.UpperKnee.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.LegBump1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.LegBump2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.LowerKnee2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.UpperKnee2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.HipJoint.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.BodyCenter.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Abdomen.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Head.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Ljaw1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Rjaw1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Ljaw2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Rjaw2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Ljaw3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Rjaw3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Tail.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.HeadSpike1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.HeadSpike2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Hip1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Hip2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Hip3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Hip4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Hip5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Hip6.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Hip7.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Hip8.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
