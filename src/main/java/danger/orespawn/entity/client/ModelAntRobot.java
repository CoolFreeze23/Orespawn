package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.AntRobot;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class ModelAntRobot extends EntityModel<AntRobot> {
    private final ModelPart Leg1;
    private final ModelPart Leg2;
    private final ModelPart Leg3;
    private final ModelPart Foot1;
    private final ModelPart Foot2;
    private final ModelPart Foot3;
    private final ModelPart Foot4;
    private final ModelPart Foot5;
    private final ModelPart Foot6;
    private final ModelPart Foot7;
    private final ModelPart Body;
    private final ModelPart Abdomen;
    private final ModelPart Head;
    private final ModelPart Jet1;
    private final ModelPart Jet2;
    private final ModelPart Hip1;
    private final ModelPart Hip2;
    private final ModelPart LJaw1;
    private final ModelPart RJaw1;
    private final ModelPart LJaw2;
    private final ModelPart RJaw2;
    private final ModelPart LAntenna;
    private final ModelPart RAntenna;
    private final ModelPart Hip3;
    private final ModelPart Hip4;
    private final ModelPart Hip5;
    private final ModelPart Hip6;

    public ModelAntRobot(ModelPart root) {
        this.Leg1 = root.getChild("Leg1");
        this.Leg2 = root.getChild("Leg2");
        this.Leg3 = root.getChild("Leg3");
        this.Foot1 = root.getChild("Foot1");
        this.Foot2 = root.getChild("Foot2");
        this.Foot3 = root.getChild("Foot3");
        this.Foot4 = root.getChild("Foot4");
        this.Foot5 = root.getChild("Foot5");
        this.Foot6 = root.getChild("Foot6");
        this.Foot7 = root.getChild("Foot7");
        this.Body = root.getChild("Body");
        this.Abdomen = root.getChild("Abdomen");
        this.Head = root.getChild("Head");
        this.Jet1 = root.getChild("Jet1");
        this.Jet2 = root.getChild("Jet2");
        this.Hip1 = root.getChild("Hip1");
        this.Hip2 = root.getChild("Hip2");
        this.LJaw1 = root.getChild("LJaw1");
        this.RJaw1 = root.getChild("RJaw1");
        this.LJaw2 = root.getChild("LJaw2");
        this.RJaw2 = root.getChild("RJaw2");
        this.LAntenna = root.getChild("LAntenna");
        this.RAntenna = root.getChild("RAntenna");
        this.Hip3 = root.getChild("Hip3");
        this.Hip4 = root.getChild("Hip4");
        this.Hip5 = root.getChild("Hip5");
        this.Hip6 = root.getChild("Hip6");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("Leg1",
                CubeListBuilder.create().texOffs(19, 40).mirror().addBox(-1.5F, -1.5F, 0.0F, 3, 3, 50),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.7853982F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("Leg2",
                CubeListBuilder.create().texOffs(19, 41).mirror().addBox(-1.0F, -1.0F, 0.0F, 2, 2, 50),
                PartPose.offset(0.0F, -35.0F, 35.0F));

        partdefinition.addOrReplaceChild("Leg3",
                CubeListBuilder.create().texOffs(20, 42).mirror().addBox(-0.5F, -0.5F, 0.0F, 1, 1, 50),
                PartPose.offsetAndRotation(0.0F, -35.0F, 85.0F, -0.7853982F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("Foot1",
                CubeListBuilder.create().texOffs(28, 0).mirror().addBox(-2.5F, -0.5F, 50.0F, 5, 1, 2),
                PartPose.offsetAndRotation(0.0F, -35.0F, 85.0F, -0.7853982F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("Foot2",
                CubeListBuilder.create().texOffs(30, 4).mirror().addBox(1.5F, -0.5F, 52.0F, 1, 1, 3),
                PartPose.offsetAndRotation(0.0F, -35.0F, 85.0F, -0.7853982F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("Foot3",
                CubeListBuilder.create().texOffs(44, 0).mirror().addBox(-0.5F, -0.5F, 52.0F, 1, 1, 5),
                PartPose.offsetAndRotation(0.0F, -35.0F, 85.0F, -0.7853982F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("Foot4",
                CubeListBuilder.create().texOffs(30, 9).mirror().addBox(-2.5F, -0.5F, 52.0F, 1, 1, 3),
                PartPose.offsetAndRotation(0.0F, -35.0F, 85.0F, -0.7853982F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("Foot5",
                CubeListBuilder.create().texOffs(40, 8).mirror().addBox(-0.5F, -2.5F, 50.0F, 1, 5, 2),
                PartPose.offsetAndRotation(0.0F, -35.0F, 85.0F, -0.7853982F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("Foot6",
                CubeListBuilder.create().texOffs(48, 9).mirror().addBox(-0.5F, -2.5F, 52.0F, 1, 1, 2),
                PartPose.offsetAndRotation(0.0F, -35.0F, 85.0F, -0.7853982F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("Foot7",
                CubeListBuilder.create().texOffs(48, 14).mirror().addBox(-0.5F, 1.5F, 52.0F, 1, 1, 2),
                PartPose.offsetAndRotation(0.0F, -35.0F, 85.0F, -0.7853982F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("Body",
                CubeListBuilder.create().texOffs(0, 151).mirror().addBox(-11.0F, 0.0F, -16.0F, 22, 14, 32),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("Abdomen",
                CubeListBuilder.create().texOffs(0, 199).mirror().addBox(-15.0F, -10.0F, 16.0F, 30, 22, 34),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("Head",
                CubeListBuilder.create().texOffs(0, 120).mirror().addBox(-7.0F, 4.0F, -34.0F, 14, 11, 18),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("Jet1",
                CubeListBuilder.create().texOffs(78, 0).mirror().addBox(0.0F, 0.0F, 0.0F, 6, 6, 18),
                PartPose.offset(8.0F, -12.0F, 35.0F));

        partdefinition.addOrReplaceChild("Jet2",
                CubeListBuilder.create().texOffs(78, 0).mirror().addBox(0.0F, 0.0F, 0.0F, 6, 6, 18),
                PartPose.offset(-14.0F, -12.0F, 35.0F));

        partdefinition.addOrReplaceChild("Hip1",
                CubeListBuilder.create().texOffs(0, 96).mirror().addBox(0.0F, 0.0F, 0.0F, 6, 6, 6),
                PartPose.offset(11.0F, 9.0F, -3.0F));

        partdefinition.addOrReplaceChild("Hip2",
                CubeListBuilder.create().texOffs(0, 96).mirror().addBox(0.0F, 0.0F, 0.0F, 6, 6, 6),
                PartPose.offset(-17.0F, 9.0F, -3.0F));

        partdefinition.addOrReplaceChild("LJaw1",
                CubeListBuilder.create().texOffs(0, 33).mirror().addBox(-2.0F, 0.0F, -2.0F, 17, 1, 4),
                PartPose.offsetAndRotation(5.0F, 13.0F, -33.0F, 0.0F, 0.8901179F, 0.0F));

        partdefinition.addOrReplaceChild("RJaw1",
                CubeListBuilder.create().texOffs(0, 33).mirror().addBox(-2.0F, 0.0F, -2.0F, 17, 1, 4),
                PartPose.offsetAndRotation(-5.0F, 13.0F, -33.0F, 0.0F, 2.216568F, 0.0F));

        partdefinition.addOrReplaceChild("LJaw2",
                CubeListBuilder.create().texOffs(0, 27).mirror().addBox(12.0F, 0.0F, 5.0F, 17, 1, 3),
                PartPose.offsetAndRotation(5.0F, 13.0F, -33.0F, 0.0F, 1.37881F, 0.0F));

        partdefinition.addOrReplaceChild("RJaw2",
                CubeListBuilder.create().texOffs(0, 27).mirror().addBox(12.0F, 0.0F, -8.0F, 17, 1, 3),
                PartPose.offsetAndRotation(-5.0F, 13.0F, -33.0F, 0.0F, 1.745329F, 0.0F));

        partdefinition.addOrReplaceChild("LAntenna",
                CubeListBuilder.create().texOffs(70, 0).mirror().addBox(-0.5F, -12.0F, -0.5F, 1, 12, 1),
                PartPose.offsetAndRotation(0.0F, 4.0F, -32.0F, 0.0F, 0.0F, 0.5410521F));

        partdefinition.addOrReplaceChild("RAntenna",
                CubeListBuilder.create().texOffs(70, 0).mirror().addBox(-0.5F, -12.0F, -0.5F, 1, 12, 1),
                PartPose.offsetAndRotation(0.0F, 4.0F, -32.0F, 0.0F, 0.0F, -0.5410521F));

        partdefinition.addOrReplaceChild("Hip3",
                CubeListBuilder.create().texOffs(0, 96).mirror().addBox(0.0F, 0.0F, 0.0F, 6, 6, 6),
                PartPose.offset(-17.0F, 9.0F, 10.0F));

        partdefinition.addOrReplaceChild("Hip4",
                CubeListBuilder.create().texOffs(0, 96).mirror().addBox(0.0F, 0.0F, 0.0F, 6, 6, 6),
                PartPose.offset(11.0F, 9.0F, 10.0F));

        partdefinition.addOrReplaceChild("Hip5",
                CubeListBuilder.create().texOffs(0, 96).mirror().addBox(0.0F, 0.0F, 0.0F, 6, 6, 6),
                PartPose.offset(11.0F, 9.0F, -16.0F));

        partdefinition.addOrReplaceChild("Hip6",
                CubeListBuilder.create().texOffs(0, 96).mirror().addBox(0.0F, 0.0F, 0.0F, 6, 6, 6),
                PartPose.offset(-17.0F, 9.0F, -16.0F));

        return LayerDefinition.create(meshdefinition, 128, 256);
    }

    @Override
    public void setupAnim(AntRobot entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        RenderSpiderRobotInfo r = null;
        r = entity.getRenderSpiderRobotInfo();
        for (int i = 0; i < 6; ++i) {
        this.Leg2.yRot = this.Leg3.yRot = r.ydisplayangle[i];
        this.Leg1.yRot = this.Leg3.yRot;
        this.Foot1.yRot = r.ydisplayangle[i];
        this.Foot2.yRot = r.ydisplayangle[i];
        this.Foot3.yRot = r.ydisplayangle[i];
        this.Foot4.yRot = r.ydisplayangle[i];
        this.Foot5.yRot = r.ydisplayangle[i];
        this.Foot6.yRot = r.ydisplayangle[i];
        this.Foot7.yRot = r.ydisplayangle[i];
        this.Leg1.xRot = (float)r.p1xangle[i] + r.uddisplayangle[i];
        this.Leg2.xRot = (float)r.p2xangle[i] + r.uddisplayangle[i];
        this.Foot1.xRot = this.Leg3.xRot = (float)r.p3xangle[i] + r.uddisplayangle[i];
        this.Foot2.xRot = this.Leg3.xRot;
        this.Foot3.xRot = this.Leg3.xRot;
        this.Foot4.xRot = this.Leg3.xRot;
        this.Foot5.xRot = this.Leg3.xRot;
        this.Foot6.xRot = this.Leg3.xRot;
        this.Foot7.xRot = this.Leg3.xRot;
        this.Leg1.x = -((float)Math.cos(r.ymid[i])) * r.legoff[i] * 16.0f;
        this.Leg1.z = (float)Math.sin(r.ymid[i]) * r.legoff[i] * 16.0f;
        this.Leg1.y = r.yoff[i] * -16.0f;
        this.Leg2.y = this.Leg1.y - (float)Math.sin(this.Leg1.xRot) * 49.0f;
        this.Leg2.z = this.Leg1.z + (float)Math.cos(this.Leg1.xRot) * (float)Math.cos(this.Leg1.yRot) * 49.0f;
        this.Leg2.x = this.Leg1.x + (float)Math.cos(this.Leg1.xRot) * (float)Math.sin(this.Leg1.yRot) * 49.0f;
        this.Leg3.y = this.Leg2.y - (float)Math.sin(this.Leg2.xRot) * 49.0f;
        this.Leg3.z = this.Leg2.z + (float)Math.cos(this.Leg2.xRot) * (float)Math.cos(this.Leg2.yRot) * 49.0f;
        this.Foot1.x = this.Leg3.x = this.Leg2.x + (float)Math.cos(this.Leg2.xRot) * (float)Math.sin(this.Leg2.yRot) * 49.0f;
        this.Foot1.y = this.Leg3.y;
        this.Foot1.z = this.Leg3.z;
        this.Foot2.x = this.Leg3.x;
        this.Foot2.y = this.Leg3.y;
        this.Foot2.z = this.Leg3.z;
        this.Foot3.x = this.Leg3.x;
        this.Foot3.y = this.Leg3.y;
        this.Foot3.z = this.Leg3.z;
        this.Foot4.x = this.Leg3.x;
        this.Foot4.y = this.Leg3.y;
        this.Foot4.z = this.Leg3.z;
        this.Foot5.x = this.Leg3.x;
        this.Foot5.y = this.Leg3.y;
        this.Foot5.z = this.Leg3.z;
        this.Foot6.x = this.Leg3.x;
        this.Foot6.y = this.Leg3.y;
        this.Foot6.z = this.Leg3.z;
        this.Foot7.x = this.Leg3.x;
        this.Foot7.y = this.Leg3.y;
        this.Foot7.z = this.Leg3.z;
        }
        if (entity.getAttacking() == 0) {
        this.LJaw1.yRot = 0.89f;
        this.LJaw2.yRot = 1.378f;
        this.RJaw1.yRot = 2.216f;
        this.RJaw2.yRot = 1.745f;
        this.LAntenna.xRot = Mth.cos((float)((float)r.gpcounter * 0.35f)) * (float)Math.PI * 0.05f;
        this.LAntenna.zRot = 0.54f + Mth.cos((float)((float)r.gpcounter * 0.25f)) * (float)Math.PI * 0.05f;
        this.RAntenna.xRot = Mth.cos((float)((float)r.gpcounter * 0.3f)) * (float)Math.PI * 0.05f;
        this.RAntenna.zRot = -0.54f + Mth.cos((float)((float)r.gpcounter * 0.45f)) * (float)Math.PI * 0.05f;
        } else {
        float newangle = Mth.cos((float)((float)r.gpcounter * 0.25f)) * (float)Math.PI * 0.22f;
        this.LJaw1.yRot = newangle + 0.89f;
        this.LJaw2.yRot = newangle + 1.378f;
        this.RJaw1.yRot = -newangle + 2.216f;
        this.RJaw2.yRot = 1.745f - newangle;
        this.LAntenna.xRot = Mth.cos((float)((float)r.gpcounter * 0.45f)) * (float)Math.PI * 0.1f;
        this.LAntenna.zRot = 0.54f + Mth.cos((float)((float)r.gpcounter * 0.35f)) * (float)Math.PI * 0.1f;
        this.RAntenna.xRot = Mth.cos((float)((float)r.gpcounter * 0.4f)) * (float)Math.PI * 0.1f;
        this.RAntenna.zRot = -0.54f + Mth.cos((float)((float)r.gpcounter * 0.55f)) * (float)Math.PI * 0.1f;
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.Leg1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Leg2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Leg3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Foot1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Foot2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Foot3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Foot4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Foot5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Foot6.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Foot7.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Abdomen.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Head.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Jet1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Jet2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Hip1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Hip2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Hip3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Hip4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Hip5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Hip6.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.LJaw1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.RJaw1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.LJaw2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.RJaw2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.LAntenna.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.RAntenna.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
