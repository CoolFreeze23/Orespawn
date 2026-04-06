package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.EntityMolenoid;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class MolenoidModel extends EntityModel<EntityMolenoid> {
    private final ModelPart body;
    private final ModelPart shoulders;
    private final ModelPart head1;
    private final ModelPart head2;
    private final ModelPart head3;
    private final ModelPart nosestar1;
    private final ModelPart nosestar2;
    private final ModelPart nosestar3;
    private final ModelPart larm;
    private final ModelPart lhand;
    private final ModelPart lclaw1;
    private final ModelPart lclaw2;
    private final ModelPart lclaw3;
    private final ModelPart lclaw4;
    private final ModelPart nosestar4;
    private final ModelPart nosestar5;
    private final ModelPart nosestar6;
    private final ModelPart butt;
    private final ModelPart tail;
    private final ModelPart lleg;
    private final ModelPart lfoot;
    private final ModelPart ltoe1;
    private final ModelPart ltoe2;
    private final ModelPart ltoe3;
    private final ModelPart ltoe4;
    private final ModelPart rarm;
    private final ModelPart rhand;
    private final ModelPart rclaw1;
    private final ModelPart rclaw2;
    private final ModelPart rclaw3;
    private final ModelPart rclaw4;
    private final ModelPart rleg;
    private final ModelPart rfoot;
    private final ModelPart rtoe1;
    private final ModelPart rtoe2;
    private final ModelPart rtoe3;
    private final ModelPart rtoe4;

    public MolenoidModel(ModelPart root) {
        this.body = root.getChild("body");
        this.shoulders = root.getChild("shoulders");
        this.head1 = root.getChild("head1");
        this.head2 = root.getChild("head2");
        this.head3 = root.getChild("head3");
        this.nosestar1 = root.getChild("nosestar1");
        this.nosestar2 = root.getChild("nosestar2");
        this.nosestar3 = root.getChild("nosestar3");
        this.larm = root.getChild("larm");
        this.lhand = root.getChild("lhand");
        this.lclaw1 = root.getChild("lclaw1");
        this.lclaw2 = root.getChild("lclaw2");
        this.lclaw3 = root.getChild("lclaw3");
        this.lclaw4 = root.getChild("lclaw4");
        this.nosestar4 = root.getChild("nosestar4");
        this.nosestar5 = root.getChild("nosestar5");
        this.nosestar6 = root.getChild("nosestar6");
        this.butt = root.getChild("butt");
        this.tail = root.getChild("tail");
        this.lleg = root.getChild("lleg");
        this.lfoot = root.getChild("lfoot");
        this.ltoe1 = root.getChild("ltoe1");
        this.ltoe2 = root.getChild("ltoe2");
        this.ltoe3 = root.getChild("ltoe3");
        this.ltoe4 = root.getChild("ltoe4");
        this.rarm = root.getChild("rarm");
        this.rhand = root.getChild("rhand");
        this.rclaw1 = root.getChild("rclaw1");
        this.rclaw2 = root.getChild("rclaw2");
        this.rclaw3 = root.getChild("rclaw3");
        this.rclaw4 = root.getChild("rclaw4");
        this.rleg = root.getChild("rleg");
        this.rfoot = root.getChild("rfoot");
        this.rtoe1 = root.getChild("rtoe1");
        this.rtoe2 = root.getChild("rtoe2");
        this.rtoe3 = root.getChild("rtoe3");
        this.rtoe4 = root.getChild("rtoe4");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 176).mirror()
                        .addBox(-16.0F, 0.0F, 0.0F, 32, 20, 56),
                PartPose.offset(0.0F, 1.0F, 6.0F));

        partdefinition.addOrReplaceChild("shoulders",
                CubeListBuilder.create().texOffs(0, 143).mirror()
                        .addBox(-17.0F, 0.0F, 0.0F, 34, 17, 10),
                PartPose.offset(0.0F, 3.0F, -4.0F));

        partdefinition.addOrReplaceChild("head1",
                CubeListBuilder.create().texOffs(0, 114).mirror()
                        .addBox(-14.0F, 0.0F, 0.0F, 28, 14, 10),
                PartPose.offset(0.0F, 5.0F, -14.0F));

        partdefinition.addOrReplaceChild("head2",
                CubeListBuilder.create().texOffs(0, 89).mirror()
                        .addBox(-11.0F, 0.0F, 0.0F, 22, 10, 10),
                PartPose.offset(0.0F, 6.0F, -24.0F));

        partdefinition.addOrReplaceChild("head3",
                CubeListBuilder.create().texOffs(0, 67).mirror()
                        .addBox(-4.0F, 0.0F, 0.0F, 8, 8, 10),
                PartPose.offset(0.0F, 7.0F, -34.0F));

        partdefinition.addOrReplaceChild("nosestar1",
                CubeListBuilder.create().texOffs(0, 32).mirror()
                        .addBox(-0.5F, -8.0F, 0.0F, 1, 16, 1),
                PartPose.offset(0.0F, 11.0F, -35.0F));

        partdefinition.addOrReplaceChild("nosestar2",
                CubeListBuilder.create().texOffs(20, 32).mirror()
                        .addBox(-0.5F, -8.0F, 0.0F, 1, 16, 1),
                PartPose.offsetAndRotation(0.0F, 11.0F, -35.0F, 0.0F, 0.0F, 1.047198F));

        partdefinition.addOrReplaceChild("nosestar3",
                CubeListBuilder.create().texOffs(40, 32).mirror()
                        .addBox(-0.5F, -8.0F, 0.0F, 1, 16, 1),
                PartPose.offsetAndRotation(0.0F, 11.0F, -35.0F, 0.0F, 0.0F, -1.047198F));

        partdefinition.addOrReplaceChild("larm",
                CubeListBuilder.create().texOffs(80, 0).mirror()
                        .addBox(0.0F, 0.0F, -2.0F, 17, 11, 5),
                PartPose.offsetAndRotation(13.0F, 8.0F, 0.0F, 0.0F, 0.6283185F, 0.0F));

        partdefinition.addOrReplaceChild("lhand",
                CubeListBuilder.create().texOffs(80, 20).mirror()
                        .addBox(0.0F, 0.0F, -2.0F, 12, 14, 4),
                PartPose.offset(25.0F, 7.0F, -9.0F));

        partdefinition.addOrReplaceChild("lclaw1",
                CubeListBuilder.create().texOffs(80, 42).mirror()
                        .addBox(0.0F, 0.0F, -1.0F, 13, 3, 2),
                PartPose.offsetAndRotation(35.0F, 20.0F, -9.0F, 0.0F, -0.1745329F, 0.0F));

        partdefinition.addOrReplaceChild("lclaw2",
                CubeListBuilder.create().texOffs(80, 52).mirror()
                        .addBox(0.0F, 0.0F, -1.0F, 13, 3, 2),
                PartPose.offsetAndRotation(35.0F, 15.0F, -9.0F, 0.0F, -0.1745329F, 0.0F));

        partdefinition.addOrReplaceChild("lclaw3",
                CubeListBuilder.create().texOffs(80, 62).mirror()
                        .addBox(0.0F, 0.0F, -1.0F, 13, 3, 2),
                PartPose.offsetAndRotation(35.0F, 10.0F, -9.0F, 0.0F, -0.1745329F, 0.0F));

        partdefinition.addOrReplaceChild("lclaw4",
                CubeListBuilder.create().texOffs(80, 72).mirror()
                        .addBox(0.0F, 0.0F, -1.0F, 13, 3, 2),
                PartPose.offsetAndRotation(35.0F, 5.0F, -9.0F, 0.0F, -0.1745329F, 0.0F));

        partdefinition.addOrReplaceChild("nosestar4",
                CubeListBuilder.create().texOffs(10, 32).mirror()
                        .addBox(-0.5F, -8.0F, 0.0F, 1, 16, 1),
                PartPose.offsetAndRotation(0.0F, 11.0F, -35.0F, 0.0F, 0.0F, 0.5235988F));

        partdefinition.addOrReplaceChild("nosestar5",
                CubeListBuilder.create().texOffs(30, 32).mirror()
                        .addBox(-0.5F, -8.0F, 0.0F, 1, 16, 1),
                PartPose.offsetAndRotation(0.0F, 11.0F, -35.0F, 0.0F, 0.0F, 1.570796F));

        partdefinition.addOrReplaceChild("nosestar6",
                CubeListBuilder.create().texOffs(50, 32).mirror()
                        .addBox(-0.5F, -8.0F, 0.0F, 1, 16, 1),
                PartPose.offsetAndRotation(0.0F, 11.0F, -35.0F, 0.0F, 0.0F, -0.5235988F));

        partdefinition.addOrReplaceChild("butt",
                CubeListBuilder.create().texOffs(196, 215).mirror()
                        .addBox(-11.0F, 0.0F, 0.0F, 22, 11, 5),
                PartPose.offset(0.0F, 6.0F, 62.0F));

        partdefinition.addOrReplaceChild("tail",
                CubeListBuilder.create().texOffs(196, 200).mirror()
                        .addBox(-2.0F, 0.0F, 0.0F, 4, 3, 5),
                PartPose.offset(0.0F, 7.0F, 67.0F));

        partdefinition.addOrReplaceChild("lleg",
                CubeListBuilder.create().texOffs(90, 80).mirror()
                        .addBox(0.0F, 0.0F, -2.0F, 17, 11, 5),
                PartPose.offsetAndRotation(14.0F, 9.0F, 58.0F, 0.0F, 0.6283185F, 0.0F));

        partdefinition.addOrReplaceChild("lfoot",
                CubeListBuilder.create().texOffs(90, 100).mirror()
                        .addBox(0.0F, 0.0F, -2.0F, 12, 14, 4),
                PartPose.offset(26.0F, 8.0F, 49.0F));

        partdefinition.addOrReplaceChild("ltoe1",
                CubeListBuilder.create().texOffs(90, 120).mirror()
                        .addBox(0.0F, 0.0F, -1.0F, 13, 3, 2),
                PartPose.offsetAndRotation(36.0F, 21.0F, 48.0F, 0.0F, -0.2617994F, 0.0F));

        partdefinition.addOrReplaceChild("ltoe2",
                CubeListBuilder.create().texOffs(90, 130).mirror()
                        .addBox(0.0F, 0.0F, -1.0F, 13, 3, 2),
                PartPose.offsetAndRotation(36.0F, 16.0F, 48.0F, 0.0F, -0.2617994F, 0.0F));

        partdefinition.addOrReplaceChild("ltoe3",
                CubeListBuilder.create().texOffs(90, 140).mirror()
                        .addBox(0.0F, 0.0F, -1.0F, 13, 3, 2),
                PartPose.offsetAndRotation(36.0F, 11.0F, 48.0F, 0.0F, -0.2617994F, 0.0F));

        partdefinition.addOrReplaceChild("ltoe4",
                CubeListBuilder.create().texOffs(90, 150).mirror()
                        .addBox(0.0F, 0.0F, -1.0F, 13, 3, 2),
                PartPose.offsetAndRotation(36.0F, 6.0F, 48.0F, 0.0F, -0.2617994F, 0.0F));

        partdefinition.addOrReplaceChild("rarm",
                CubeListBuilder.create().texOffs(130, 0).mirror()
                        .addBox(-17.0F, 0.0F, -2.0F, 17, 11, 5),
                PartPose.offsetAndRotation(-14.0F, 8.0F, 0.0F, 0.0F, -0.6283185F, 0.0F));

        partdefinition.addOrReplaceChild("rhand",
                CubeListBuilder.create().texOffs(130, 20).mirror()
                        .addBox(-12.0F, 0.0F, -2.0F, 12, 14, 4),
                PartPose.offset(-26.0F, 7.0F, -9.0F));

        partdefinition.addOrReplaceChild("rclaw1",
                CubeListBuilder.create().texOffs(130, 42).mirror()
                        .addBox(-13.0F, 0.0F, -1.0F, 13, 3, 2),
                PartPose.offsetAndRotation(-36.0F, 20.0F, -9.0F, 0.0F, 0.1745329F, 0.0F));

        partdefinition.addOrReplaceChild("rclaw2",
                CubeListBuilder.create().texOffs(130, 52).mirror()
                        .addBox(-13.0F, 0.0F, -1.0F, 13, 3, 2),
                PartPose.offsetAndRotation(-36.0F, 15.0F, -9.0F, 0.0F, 0.1745329F, 0.0F));

        partdefinition.addOrReplaceChild("rclaw3",
                CubeListBuilder.create().texOffs(130, 62).mirror()
                        .addBox(-13.0F, 0.0F, -1.0F, 13, 3, 2),
                PartPose.offsetAndRotation(-36.0F, 10.0F, -9.0F, 0.0F, 0.1745329F, 0.0F));

        partdefinition.addOrReplaceChild("rclaw4",
                CubeListBuilder.create().texOffs(130, 72).mirror()
                        .addBox(-13.0F, 0.0F, -1.0F, 13, 3, 2),
                PartPose.offsetAndRotation(-36.0F, 5.0F, -9.0F, 0.0F, 0.1745329F, 0.0F));

        partdefinition.addOrReplaceChild("rleg",
                CubeListBuilder.create().texOffs(150, 80).mirror()
                        .addBox(-17.0F, 0.0F, -2.0F, 17, 11, 5),
                PartPose.offsetAndRotation(-14.0F, 9.0F, 58.0F, 0.0F, -0.6283185F, 0.0F));

        partdefinition.addOrReplaceChild("rfoot",
                CubeListBuilder.create().texOffs(150, 100).mirror()
                        .addBox(-12.0F, 0.0F, -2.0F, 12, 14, 4),
                PartPose.offset(-26.0F, 8.0F, 49.0F));

        partdefinition.addOrReplaceChild("rtoe1",
                CubeListBuilder.create().texOffs(150, 120).mirror()
                        .addBox(-13.0F, 0.0F, -1.0F, 13, 3, 2),
                PartPose.offsetAndRotation(-36.0F, 21.0F, 48.0F, 0.0F, 0.2617994F, 0.0F));

        partdefinition.addOrReplaceChild("rtoe2",
                CubeListBuilder.create().texOffs(150, 130).mirror()
                        .addBox(-13.0F, 0.0F, -1.0F, 13, 3, 2),
                PartPose.offsetAndRotation(-36.0F, 16.0F, 48.0F, 0.0F, 0.2617994F, 0.0F));

        partdefinition.addOrReplaceChild("rtoe3",
                CubeListBuilder.create().texOffs(150, 140).mirror()
                        .addBox(-13.0F, 0.0F, -1.0F, 13, 3, 2),
                PartPose.offsetAndRotation(-36.0F, 11.0F, 48.0F, 0.0F, 0.2617994F, 0.0F));

        partdefinition.addOrReplaceChild("rtoe4",
                CubeListBuilder.create().texOffs(150, 150).mirror()
                        .addBox(-13.0F, 0.0F, -1.0F, 13, 3, 2),
                PartPose.offsetAndRotation(-36.0F, 6.0F, 48.0F, 0.0F, 0.2617994F, 0.0F));

        return LayerDefinition.create(meshdefinition, 256, 256);
    }

    @Override
    public void setupAnim(EntityMolenoid entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float newangle = 0.0f;
        newangle = entity.getAttacking() != 0 ? Mth.cos((float)(ageInTicks * 1.7f * limbSwingAmount)) * (float)Math.PI * 0.25f : (limbSwingAmount > 0.1f ? Mth.cos((float)(ageInTicks * 1.3f * limbSwingAmount)) * (float)Math.PI * 0.25f * limbSwingAmount : 0.0f);
        this.larm.yRot = newangle + 0.628f;
        this.lhand.z = this.larm.z - (float)Math.sin(this.larm.yRot) * 15.0f;
        this.lhand.x = this.larm.x + (float)Math.cos(this.larm.yRot) * 15.0f;
        this.lhand.yRot = newangle * 1.25f;
        this.lclaw1.z = this.lhand.z - (float)Math.sin(this.lhand.yRot) * 10.0f;
        this.lclaw1.x = this.lhand.x + (float)Math.cos(this.lhand.yRot) * 10.0f;
        this.lclaw1.yRot = newangle * 1.5f - 0.174f;
        this.lclaw2.z = this.lclaw1.z;
        this.lclaw2.x = this.lclaw1.x;
        this.lclaw2.yRot = this.lclaw1.yRot;
        this.lclaw3.z = this.lclaw1.z;
        this.lclaw3.x = this.lclaw1.x;
        this.lclaw3.yRot = this.lclaw1.yRot;
        this.lclaw4.z = this.lclaw1.z;
        this.lclaw4.x = this.lclaw1.x;
        this.lclaw4.yRot = this.lclaw1.yRot;
        this.rarm.yRot = newangle - 0.628f;
        this.rhand.z = this.rarm.z + (float)Math.sin(this.rarm.yRot) * 15.0f;
        this.rhand.x = this.rarm.x - (float)Math.cos(this.rarm.yRot) * 15.0f;
        this.rhand.yRot = newangle * 1.25f;
        this.rclaw1.z = this.rhand.z + (float)Math.sin(this.rhand.yRot) * 10.0f;
        this.rclaw1.x = this.rhand.x - (float)Math.cos(this.rhand.yRot) * 10.0f;
        this.rclaw1.yRot = newangle * 1.5f + 0.174f;
        this.rclaw2.z = this.rclaw1.z;
        this.rclaw2.x = this.rclaw1.x;
        this.rclaw2.yRot = this.rclaw1.yRot;
        this.rclaw3.z = this.rclaw1.z;
        this.rclaw3.x = this.rclaw1.x;
        this.rclaw3.yRot = this.rclaw1.yRot;
        this.rclaw4.z = this.rclaw1.z;
        this.rclaw4.x = this.rclaw1.x;
        this.rclaw4.yRot = this.rclaw1.yRot;
        newangle = limbSwingAmount > 0.1f ? Mth.cos((float)(ageInTicks * 1.3f * limbSwingAmount)) * (float)Math.PI * 0.25f * limbSwingAmount : 0.0f;
        this.lleg.yRot = -newangle + 0.628f;
        this.lfoot.z = this.lleg.z - (float)Math.sin(this.lleg.yRot) * 15.0f;
        this.lfoot.x = this.lleg.x + (float)Math.cos(this.lleg.yRot) * 15.0f;
        this.lfoot.yRot = -newangle * 1.25f;
        this.ltoe1.z = this.lfoot.z - (float)Math.sin(this.lfoot.yRot) * 10.0f;
        this.ltoe1.x = this.lfoot.x + (float)Math.cos(this.lfoot.yRot) * 10.0f;
        this.ltoe1.yRot = -newangle * 1.5f - 0.261f;
        this.ltoe2.z = this.ltoe1.z;
        this.ltoe2.x = this.ltoe1.x;
        this.ltoe2.yRot = this.ltoe1.yRot;
        this.ltoe3.z = this.ltoe1.z;
        this.ltoe3.x = this.ltoe1.x;
        this.ltoe3.yRot = this.ltoe1.yRot;
        this.ltoe4.z = this.ltoe1.z;
        this.ltoe4.x = this.ltoe1.x;
        this.ltoe4.yRot = this.ltoe1.yRot;
        this.rleg.yRot = -newangle - 0.628f;
        this.rfoot.z = this.rleg.z + (float)Math.sin(this.rleg.yRot) * 15.0f;
        this.rfoot.x = this.rleg.x - (float)Math.cos(this.rleg.yRot) * 15.0f;
        this.rfoot.yRot = -newangle * 1.25f;
        this.rtoe1.z = this.rfoot.z + (float)Math.sin(this.rfoot.yRot) * 10.0f;
        this.rtoe1.x = this.rfoot.x - (float)Math.cos(this.rfoot.yRot) * 10.0f;
        this.rtoe1.yRot = -newangle * 1.5f + 0.261f;
        this.rtoe2.z = this.rtoe1.z;
        this.rtoe2.x = this.rtoe1.x;
        this.rtoe2.yRot = this.rtoe1.yRot;
        this.rtoe3.z = this.rtoe1.z;
        this.rtoe3.x = this.rtoe1.x;
        this.rtoe3.yRot = this.rtoe1.yRot;
        this.rtoe4.z = this.rtoe1.z;
        this.rtoe4.x = this.rtoe1.x;
        this.rtoe4.yRot = this.rtoe1.yRot;
        this.nosestar1.zRot = newangle = Mth.cos((float)(ageInTicks * 0.1f * limbSwingAmount)) * (float)Math.PI;
        this.nosestar2.zRot = newangle + 0.523f;
        this.nosestar3.zRot = newangle + 1.047f;
        this.nosestar4.zRot = newangle + 1.57f;
        this.nosestar5.zRot = newangle - 1.047f;
        this.nosestar6.zRot = newangle - 0.523f;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.shoulders.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.head1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.head2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.head3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.nosestar1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.nosestar2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.nosestar3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.larm.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lhand.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lclaw1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lclaw2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lclaw3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lclaw4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.nosestar4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.nosestar5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.nosestar6.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.butt.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tail.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lleg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lfoot.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.ltoe1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.ltoe2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.ltoe3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.ltoe4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rarm.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rhand.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rclaw1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rclaw2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rclaw3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rclaw4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rleg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rfoot.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rtoe1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rtoe2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rtoe3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rtoe4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
