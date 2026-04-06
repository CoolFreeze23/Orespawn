package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.EntityScorpion;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class ScorpionModel extends EntityModel<EntityScorpion> {
    private int ri1, ri2;
    private final ModelPart body;
    private final ModelPart tail1;
    private final ModelPart tail2;
    private final ModelPart tail3;
    private final ModelPart tail4;
    private final ModelPart tail5;
    private final ModelPart tail6;
    private final ModelPart lleg1;
    private final ModelPart rleg1;
    private final ModelPart rleg2;
    private final ModelPart lleg3;
    private final ModelPart rleg4;
    private final ModelPart rleg3;
    private final ModelPart lleg4;
    private final ModelPart lleg2;
    private final ModelPart head;
    private final ModelPart larm2;
    private final ModelPart rarm2;
    private final ModelPart larm1;
    private final ModelPart rarm1;
    private final ModelPart lclaw;
    private final ModelPart rclaw;

    public ScorpionModel(ModelPart root) {
        this.body = root.getChild("body");
        this.tail1 = root.getChild("tail1");
        this.tail2 = root.getChild("tail2");
        this.tail3 = root.getChild("tail3");
        this.tail4 = root.getChild("tail4");
        this.tail5 = root.getChild("tail5");
        this.tail6 = root.getChild("tail6");
        this.lleg1 = root.getChild("lleg1");
        this.rleg1 = root.getChild("rleg1");
        this.rleg2 = root.getChild("rleg2");
        this.lleg3 = root.getChild("lleg3");
        this.rleg4 = root.getChild("rleg4");
        this.rleg3 = root.getChild("rleg3");
        this.lleg4 = root.getChild("lleg4");
        this.lleg2 = root.getChild("lleg2");
        this.head = root.getChild("head");
        this.larm2 = root.getChild("larm2");
        this.rarm2 = root.getChild("rarm2");
        this.larm1 = root.getChild("larm1");
        this.rarm1 = root.getChild("rarm1");
        this.lclaw = root.getChild("lclaw");
        this.rclaw = root.getChild("rclaw");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 0).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 6, 4, 8),
                PartPose.offset(-3.0F, 17.0F, -4.0F));

        partdefinition.addOrReplaceChild("tail1",
                CubeListBuilder.create().texOffs(28, 0).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 4, 4, 5),
                PartPose.offsetAndRotation(-2.0F, 17.0F, 3.0F, 0.2617994F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("tail2",
                CubeListBuilder.create().texOffs(46, 0).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 3, 3, 5),
                PartPose.offsetAndRotation(-1.5F, 16.8F, 6.0F, 1.029744F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("tail3",
                CubeListBuilder.create().texOffs(62, 0).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 3, 3, 4),
                PartPose.offsetAndRotation(-1.5F, 14.5F, 8.0F, 1.727876F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("tail4",
                CubeListBuilder.create().texOffs(0, 17).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 2, 2, 5),
                PartPose.offsetAndRotation(-1.0F, 12.0F, 9.0F, 2.513274F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("tail5",
                CubeListBuilder.create().texOffs(70, 7).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 2, 2, 4),
                PartPose.offsetAndRotation(-1.0F, 9.0F, 6.0F, 3.141593F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("tail6",
                CubeListBuilder.create().texOffs(62, 7).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 1, 1, 3),
                PartPose.offsetAndRotation(-0.5F, 8.0F, 2.0F, 3.141593F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("lleg1",
                CubeListBuilder.create().texOffs(0, 12).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 11, 2, 2),
                PartPose.offsetAndRotation(2.0F, 18.0F, -3.0F, 0.0F, 0.4886922F, 0.3665191F));

        partdefinition.addOrReplaceChild("rleg1",
                CubeListBuilder.create().texOffs(0, 12).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 11, 2, 2),
                PartPose.offsetAndRotation(-2.0F, 18.0F, -1.0F, 0.0F, 2.6529F, -0.3665191F));

        partdefinition.addOrReplaceChild("rleg2",
                CubeListBuilder.create().texOffs(0, 12).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 11, 2, 2),
                PartPose.offsetAndRotation(-2.0F, 18.0F, 1.0F, 0.0F, 2.897247F, -0.3665191F));

        partdefinition.addOrReplaceChild("lleg3",
                CubeListBuilder.create().texOffs(0, 12).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 11, 2, 2),
                PartPose.offsetAndRotation(2.0F, 18.0F, 1.0F, 0.0F, -0.2443461F, 0.3665191F));

        partdefinition.addOrReplaceChild("rleg4",
                CubeListBuilder.create().texOffs(0, 12).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 11, 2, 2),
                PartPose.offsetAndRotation(-2.0F, 18.0F, 5.0F, 0.0F, -2.6529F, -0.3665191F));

        partdefinition.addOrReplaceChild("rleg3",
                CubeListBuilder.create().texOffs(0, 12).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 11, 2, 2),
                PartPose.offsetAndRotation(-2.0F, 18.0F, 3.0F, 0.0F, -2.897247F, -0.3665191F));

        partdefinition.addOrReplaceChild("lleg4",
                CubeListBuilder.create().texOffs(0, 12).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 11, 2, 2),
                PartPose.offsetAndRotation(2.0F, 18.0F, 3.0F, 0.0F, -0.4886922F, 0.3665191F));

        partdefinition.addOrReplaceChild("lleg2",
                CubeListBuilder.create().texOffs(0, 12).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 11, 2, 2),
                PartPose.offsetAndRotation(2.0F, 18.0F, -1.0F, 0.0F, 0.2443461F, 0.3665191F));

        partdefinition.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(28, 9).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 5, 3, 4),
                PartPose.offset(-2.5F, 17.5F, -8.0F));

        partdefinition.addOrReplaceChild("larm2",
                CubeListBuilder.create().texOffs(46, 8).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 6, 2, 2),
                PartPose.offsetAndRotation(1.0F, 18.0F, -6.0F, 0.0F, 0.5235988F, 0.1745329F));

        partdefinition.addOrReplaceChild("rarm2",
                CubeListBuilder.create().texOffs(46, 8).mirror()
                        .addBox(0.0F, 0.0F, -2.0F, 6, 2, 2),
                PartPose.offsetAndRotation(-1.0F, 18.0F, -6.0F, 0.0F, 2.617994F, -0.1745329F));

        partdefinition.addOrReplaceChild("larm1",
                CubeListBuilder.create().texOffs(70, 13).mirror()
                        .addBox(-2.0F, 0.0F, -3.0F, 2, 2, 3),
                PartPose.offsetAndRotation(7.0F, 19.0F, -7.2F, 0.1745329F, 0.1745329F, 0.0F));

        partdefinition.addOrReplaceChild("rarm1",
                CubeListBuilder.create().texOffs(70, 13).mirror()
                        .addBox(0.0F, 0.0F, -3.0F, 2, 2, 3),
                PartPose.offsetAndRotation(-7.0F, 19.0F, -7.2F, 0.1745329F, -0.1745329F, 0.0F));

        partdefinition.addOrReplaceChild("lclaw",
                CubeListBuilder.create().texOffs(46, 12).mirror()
                        .addBox(-3.0F, 0.0F, -4.0F, 3, 2, 4),
                PartPose.offsetAndRotation(7.0F, 19.0F, -10.0F, 0.0174533F, 0.3839724F, 0.1396263F));

        partdefinition.addOrReplaceChild("rclaw",
                CubeListBuilder.create().texOffs(46, 12).mirror()
                        .addBox(0.0F, 0.0F, -4.0F, 3, 2, 4),
                PartPose.offsetAndRotation(-7.0F, 19.0F, -10.0F, 0.0174533F, -0.3839724F, 0.1396263F));

        return LayerDefinition.create(meshdefinition, 88, 24);
    }

    @Override
    public void setupAnim(EntityScorpion entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float newangle = 0.0f;
        float upangle = 0.0f;
        float nextangle = 0.0f;
        float pi4 = 1.570795f;
        newangle = Mth.cos((float)(ageInTicks * 2.0f * limbSwingAmount)) * (float)Math.PI * 0.12f * limbSwingAmount;
        this.lleg1.yRot = newangle + 0.49f;
        this.rleg1.yRot = -newangle + 2.65f;
        newangle = Mth.cos((float)(ageInTicks * 2.0f * limbSwingAmount - 1.0f * pi4)) * (float)Math.PI * 0.12f * limbSwingAmount;
        this.lleg2.yRot = newangle + 0.24f;
        this.rleg2.yRot = -newangle + 2.9f;
        newangle = Mth.cos((float)(ageInTicks * 2.0f * limbSwingAmount - 2.0f * pi4)) * (float)Math.PI * 0.12f * limbSwingAmount;
        this.lleg3.yRot = newangle - 0.24f;
        this.rleg3.yRot = -newangle - 2.9f;
        newangle = Mth.cos((float)(ageInTicks * 2.0f * limbSwingAmount - 3.0f * pi4)) * (float)Math.PI * 0.12f * limbSwingAmount;
        this.lleg4.yRot = newangle - 0.49f;
        this.rleg4.yRot = -newangle - 2.65f;
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
        this.body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tail1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tail2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tail3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tail4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tail5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tail6.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lleg1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rleg1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rleg2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lleg3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rleg4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rleg3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lleg4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lleg2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.head.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.larm2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rarm2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.larm1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rarm1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lclaw.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rclaw.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }

    private void doLeftClaw(float angle) {
        this.larm2.yRot = 0.52f + angle;
        this.larm1.z = (float)((double)this.larm2.z - Math.sin(this.larm2.yRot) * 4.5);
        this.lclaw.z = this.larm1.z - 3.0f;
        this.lclaw.yRot = 0.381f - angle;
    }

    private void doRightClaw(float angle) {
        this.rarm2.yRot = 2.61f - angle;
        this.rarm1.z = (float)((double)this.rarm2.z - Math.sin(this.rarm2.yRot) * 4.5);
        this.rclaw.z = this.rarm1.z - 3.0f;
        this.rclaw.yRot = -0.381f + angle;
    }

    private void doTail(float angle) {
        this.tail1.xRot = 0.26f + angle;
        this.tail2.xRot = this.tail1.xRot + 0.76900005f + angle;
        this.tail2.y = (float)((double)this.tail1.y - Math.sin(this.tail1.xRot) * 4.0);
        this.tail2.z = (float)((double)this.tail1.z + Math.cos(this.tail1.xRot) * 4.0);
        this.tail3.xRot = this.tail2.xRot + 0.701f + angle;
        this.tail3.y = (float)((double)this.tail2.y - Math.sin(this.tail2.xRot) * 4.0);
        this.tail3.z = (float)((double)this.tail2.z + Math.cos(this.tail2.xRot) * 4.0);
        this.tail4.xRot = this.tail3.xRot + -5.501f - angle * 3.0f / 2.0f - 0.4f;
        this.tail4.y = (float)((double)this.tail3.y - Math.sin(this.tail3.xRot) * 3.0);
        this.tail4.z = (float)((double)this.tail3.z + Math.cos(this.tail3.xRot) * 3.0);
        this.tail5.y = (float)((double)this.tail4.y - Math.sin(this.tail4.xRot) * 4.0);
        this.tail5.z = (float)((double)this.tail4.z + Math.cos(this.tail4.xRot) * 4.0);
        this.tail6.y = (float)((double)this.tail5.y - Math.sin(this.tail5.xRot) * 4.0);
        this.tail6.z = (float)((double)this.tail5.z + Math.cos(this.tail5.xRot) * 4.0);
    }
}
