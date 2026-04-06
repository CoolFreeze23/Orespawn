package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.Ostrich;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class OstrichModel extends EntityModel<Ostrich> {
    private int ri1, ri2;
    private float rf1;
    private final ModelPart Body1;
    private final ModelPart body2;
    private final ModelPart LLeg1;
    private final ModelPart Rleg1;
    private final ModelPart LLeg2;
    private final ModelPart Lfoot1;
    private final ModelPart RLeg2;
    private final ModelPart Lfoot2;
    private final ModelPart Lfoot3;
    private final ModelPart LClaw1;
    private final ModelPart LClaw2;
    private final ModelPart LClaw3;
    private final ModelPart Lfoot4;
    private final ModelPart LClaw4;
    private final ModelPart Rfoot1;
    private final ModelPart Rfoot2;
    private final ModelPart Rclaw1;
    private final ModelPart Rfoot3;
    private final ModelPart Rclaw3;
    private final ModelPart Rfoot4;
    private final ModelPart Rclaw2;
    private final ModelPart Rclaw4;
    private final ModelPart Body3;
    private final ModelPart Tail1;
    private final ModelPart Tail2;
    private final ModelPart Tail3;
    private final ModelPart Body4;
    private final ModelPart head;
    private final ModelPart leftleg;
    private final ModelPart Neck1;
    private final ModelPart Head1;
    private final ModelPart mouth1;
    private final ModelPart neck2;
    private final ModelPart rightleg;
    private final ModelPart Lwing;
    private final ModelPart Rwing;
    private final ModelPart Hat1;
    private final ModelPart Hat2;

    public OstrichModel(ModelPart root) {
        this.Body1 = root.getChild("Body1");
        this.body2 = root.getChild("body2");
        this.LLeg1 = root.getChild("LLeg1");
        this.Rleg1 = root.getChild("Rleg1");
        this.LLeg2 = root.getChild("LLeg2");
        this.Lfoot1 = root.getChild("Lfoot1");
        this.RLeg2 = root.getChild("RLeg2");
        this.Lfoot2 = root.getChild("Lfoot2");
        this.Lfoot3 = root.getChild("Lfoot3");
        this.LClaw1 = root.getChild("LClaw1");
        this.LClaw2 = root.getChild("LClaw2");
        this.LClaw3 = root.getChild("LClaw3");
        this.Lfoot4 = root.getChild("Lfoot4");
        this.LClaw4 = root.getChild("LClaw4");
        this.Rfoot1 = root.getChild("Rfoot1");
        this.Rfoot2 = root.getChild("Rfoot2");
        this.Rclaw1 = root.getChild("Rclaw1");
        this.Rfoot3 = root.getChild("Rfoot3");
        this.Rclaw3 = root.getChild("Rclaw3");
        this.Rfoot4 = root.getChild("Rfoot4");
        this.Rclaw2 = root.getChild("Rclaw2");
        this.Rclaw4 = root.getChild("Rclaw4");
        this.Body3 = root.getChild("Body3");
        this.Tail1 = root.getChild("Tail1");
        this.Tail2 = root.getChild("Tail2");
        this.Tail3 = root.getChild("Tail3");
        this.Body4 = root.getChild("Body4");
        this.head = root.getChild("head");
        this.leftleg = root.getChild("leftleg");
        this.Neck1 = root.getChild("Neck1");
        this.Head1 = root.getChild("Head1");
        this.mouth1 = root.getChild("mouth1");
        this.neck2 = root.getChild("neck2");
        this.rightleg = root.getChild("rightleg");
        this.Lwing = root.getChild("Lwing");
        this.Rwing = root.getChild("Rwing");
        this.Hat1 = root.getChild("Hat1");
        this.Hat2 = root.getChild("Hat2");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("Body1",
                CubeListBuilder.create().texOffs(0, 28).mirror()
                        .addBox(-4.0F, 0.0F, 0.0F, 8, 9, 8),
                PartPose.offsetAndRotation(0.0F, 0.0F, -6.0F, -0.2230717F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("body2",
                CubeListBuilder.create().texOffs(25, 111).mirror()
                        .addBox(-4.0F, 0.0F, 0.0F, 8, 8, 8),
                PartPose.offset(0.0F, 2.0F, -1.0F));

        partdefinition.addOrReplaceChild("LLeg1",
                CubeListBuilder.create().texOffs(25, 70).mirror()
                        .addBox(-1.0F, 3.0F, -5.0F, 2, 7, 3),
                PartPose.offsetAndRotation(3.0F, 8.0F, 1.0F, 0.4833219F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("Rleg1",
                CubeListBuilder.create().texOffs(25, 70).mirror()
                        .addBox(-2.0F, 3.0F, -5.0F, 2, 7, 3),
                PartPose.offsetAndRotation(-2.0F, 8.0F, 1.0F, 0.4833219F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("LLeg2",
                CubeListBuilder.create().texOffs(29, 59).mirror()
                        .addBox(-1.0F, 7.0F, 4.0F, 2, 7, 3),
                PartPose.offsetAndRotation(3.0F, 8.0F, 1.0F, -0.4370552F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("Lfoot1",
                CubeListBuilder.create().texOffs(29, 50).mirror()
                        .addBox(-1.0F, 14.0F, -5.0F, 2, 2, 6),
                PartPose.offset(3.0F, 8.0F, 1.0F));

        partdefinition.addOrReplaceChild("RLeg2",
                CubeListBuilder.create().texOffs(29, 59).mirror()
                        .addBox(-2.0F, 7.0F, 4.0F, 2, 7, 3),
                PartPose.offsetAndRotation(-2.0F, 8.0F, 1.0F, -0.4370552F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("Lfoot2",
                CubeListBuilder.create().texOffs(0, 9).mirror()
                        .addBox(-1.0F, 15.0F, -4.0F, 2, 1, 5),
                PartPose.offsetAndRotation(3.0F, 8.0F, 1.0F, 0.0F, 0.2602503F, 0.0F));

        partdefinition.addOrReplaceChild("Lfoot3",
                CubeListBuilder.create().texOffs(0, 9).mirror()
                        .addBox(-1.0F, 15.0F, -4.0F, 2, 1, 5),
                PartPose.offsetAndRotation(3.0F, 8.0F, 1.0F, 0.0F, -0.260246F, 0.0F));

        partdefinition.addOrReplaceChild("LClaw1",
                CubeListBuilder.create().texOffs(16, 10).mirror()
                        .addBox(0.0F, 14.0F, -7.0F, 0, 2, 3),
                PartPose.offset(3.0F, 8.0F, 1.0F));

        partdefinition.addOrReplaceChild("LClaw2",
                CubeListBuilder.create().texOffs(19, 16).mirror()
                        .addBox(-0.5F, 15.0F, -5.0F, 0, 1, 3),
                PartPose.offsetAndRotation(3.0F, 8.0F, 1.0F, 0.0F, 0.260246F, 0.0F));

        partdefinition.addOrReplaceChild("LClaw3",
                CubeListBuilder.create().texOffs(19, 16).mirror()
                        .addBox(0.5F, 15.0F, -5.0F, 0, 1, 3),
                PartPose.offsetAndRotation(3.0F, 8.0F, 1.0F, 0.0F, -0.260246F, 0.0F));

        partdefinition.addOrReplaceChild("Lfoot4",
                CubeListBuilder.create().texOffs(0, 0).mirror()
                        .addBox(-1.0F, 14.0F, -1.0F, 2, 2, 4),
                PartPose.offset(3.0F, 8.0F, 1.0F));

        partdefinition.addOrReplaceChild("LClaw4",
                CubeListBuilder.create().texOffs(16, 10).mirror()
                        .addBox(0.0F, 14.0F, 2.0F, 0, 2, 3),
                PartPose.offset(3.0F, 8.0F, 1.0F));

        partdefinition.addOrReplaceChild("Rfoot1",
                CubeListBuilder.create().texOffs(29, 50).mirror()
                        .addBox(-2.0F, 14.0F, -5.0F, 2, 2, 6),
                PartPose.offset(-2.0F, 8.0F, 1.0F));

        partdefinition.addOrReplaceChild("Rfoot2",
                CubeListBuilder.create().texOffs(0, 0).mirror()
                        .addBox(-2.0F, 14.0F, -1.0F, 2, 2, 4),
                PartPose.offset(-2.0F, 8.0F, 1.0F));

        partdefinition.addOrReplaceChild("Rclaw1",
                CubeListBuilder.create().texOffs(16, 10).mirror()
                        .addBox(-1.0F, 14.0F, -7.0F, 0, 2, 3),
                PartPose.offset(-2.0F, 8.0F, 1.0F));

        partdefinition.addOrReplaceChild("Rfoot3",
                CubeListBuilder.create().texOffs(0, 9).mirror()
                        .addBox(-2.0F, 15.0F, -4.0F, 2, 1, 5),
                PartPose.offsetAndRotation(-2.0F, 8.0F, 1.0F, 0.0F, -0.260246F, 0.0F));

        partdefinition.addOrReplaceChild("Rclaw3",
                CubeListBuilder.create().texOffs(19, 16).mirror()
                        .addBox(-0.5F, 15.0F, -5.0F, 0, 1, 3),
                PartPose.offsetAndRotation(-2.0F, 8.0F, 1.0F, 0.0F, -0.260246F, 0.0F));

        partdefinition.addOrReplaceChild("Rfoot4",
                CubeListBuilder.create().texOffs(0, 9).mirror()
                        .addBox(-2.0F, 15.0F, -4.0F, 2, 1, 5),
                PartPose.offsetAndRotation(-2.0F, 8.0F, 1.0F, 0.0F, 0.2602503F, 0.0F));

        partdefinition.addOrReplaceChild("Rclaw2",
                CubeListBuilder.create().texOffs(19, 16).mirror()
                        .addBox(-1.5F, 15.0F, -5.0F, 0, 1, 3),
                PartPose.offsetAndRotation(-2.0F, 8.0F, 1.0F, 0.0F, 0.260246F, 0.0F));

        partdefinition.addOrReplaceChild("Rclaw4",
                CubeListBuilder.create().texOffs(16, 10).mirror()
                        .addBox(-1.0F, 14.0F, 2.0F, 0, 2, 3),
                PartPose.offset(-2.0F, 8.0F, 1.0F));

        partdefinition.addOrReplaceChild("Body3",
                CubeListBuilder.create().texOffs(17, 96).mirror()
                        .addBox(-3.0F, 0.0F, 0.0F, 6, 7, 3),
                PartPose.offset(0.0F, 2.0F, 6.0F));

        partdefinition.addOrReplaceChild("Tail1",
                CubeListBuilder.create().texOffs(33, 81).mirror()
                        .addBox(-2.0F, 0.0F, 0.0F, 4, 0, 14),
                PartPose.offsetAndRotation(0.0F, 3.0F, 9.0F, -0.5948578F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("Tail2",
                CubeListBuilder.create().texOffs(36, 97).mirror()
                        .addBox(-1.0F, 0.0F, 0.0F, 3, 0, 13),
                PartPose.offsetAndRotation(0.0F, 3.0F, 8.0F, -0.5948578F, 0.3346075F, 0.0F));

        partdefinition.addOrReplaceChild("Tail3",
                CubeListBuilder.create().texOffs(36, 97).mirror()
                        .addBox(-2.0F, 0.0F, 0.0F, 3, 0, 13),
                PartPose.offsetAndRotation(0.0F, 3.0F, 8.0F, -0.5948578F, -0.3346145F, 0.0F));

        partdefinition.addOrReplaceChild("Body4",
                CubeListBuilder.create().texOffs(17, 89).mirror()
                        .addBox(-2.0F, 0.0F, 0.0F, 4, 3, 3),
                PartPose.offsetAndRotation(0.0F, 6.0F, 7.0F, 1.003822F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(74, 48).mirror()
                        .addBox(-1.0F, -24.0F, -7.0F, 2, 2, 4),
                PartPose.offset(0.0F, 5.0F, -7.0F));

        partdefinition.addOrReplaceChild("leftleg",
                CubeListBuilder.create().texOffs(0, 16).mirror()
                        .addBox(-2.0F, 0.0F, -2.0F, 4, 6, 5),
                PartPose.offsetAndRotation(3.0F, 8.0F, 1.0F, -0.2974289F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("Neck1",
                CubeListBuilder.create().texOffs(79, 84).mirror()
                        .addBox(-1.5F, -21.0F, -2.0F, 3, 21, 3),
                PartPose.offsetAndRotation(0.0F, 5.0F, -7.0F, 0.0F, -0.0349066F, 0.0F));

        partdefinition.addOrReplaceChild("Head1",
                CubeListBuilder.create().texOffs(0, 70).mirror()
                        .addBox(-2.0F, -25.0F, -3.0F, 4, 4, 4),
                PartPose.offset(0.0F, 5.0F, -7.0F));

        partdefinition.addOrReplaceChild("mouth1",
                CubeListBuilder.create().texOffs(74, 64).mirror()
                        .addBox(-1.0F, -22.0F, -6.0F, 2, 1, 3),
                PartPose.offset(0.0F, 5.0F, -7.0F));

        partdefinition.addOrReplaceChild("neck2",
                CubeListBuilder.create().texOffs(0, 99).mirror()
                        .addBox(-1.0F, -2.0F, -2.0F, 2, 4, 3),
                PartPose.offset(0.0F, 5.0F, -6.9F));

        partdefinition.addOrReplaceChild("rightleg",
                CubeListBuilder.create().texOffs(0, 16).mirror()
                        .addBox(-3.0F, 0.0F, -2.0F, 4, 6, 5),
                PartPose.offsetAndRotation(-2.0F, 8.0F, 1.0F, -0.2974216F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("Lwing",
                CubeListBuilder.create().texOffs(0, 107).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 1, 7, 11),
                PartPose.offset(4.0F, 1.0F, -5.0F));

        partdefinition.addOrReplaceChild("Rwing",
                CubeListBuilder.create().texOffs(0, 107).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 1, 7, 11),
                PartPose.offset(-5.0F, 1.0F, -5.0F));

        partdefinition.addOrReplaceChild("Hat1",
                CubeListBuilder.create().texOffs(40, 0).mirror()
                        .addBox(-2.5F, -26.0F, -4.0F, 5, 1, 5),
                PartPose.offset(0.0F, 5.0F, -7.0F));

        partdefinition.addOrReplaceChild("Hat2",
                CubeListBuilder.create().texOffs(40, 0).mirror()
                        .addBox(-2.0F, -28.0F, -3.0F, 4, 2, 4),
                PartPose.offset(0.0F, 5.0F, -7.0F));

        return LayerDefinition.create(meshdefinition, 256, 128);
    }

    @Override
    public void setupAnim(Ostrich entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float hf = 0.0f;
        float newangle = 0.0f;
        float nextangle = 0.0f;
        float lspeed = 0.0f;
        lspeed = (float)((entity.xOld - entity.getX()) * (entity.xOld - entity.getX()) + (entity.zOld - entity.getZ()) * (entity.zOld - entity.getZ()));
        lspeed = (float)Math.sqrt(lspeed);
        newangle = Mth.cos((float)(ageInTicks * 1.25f * limbSwingAmount)) * (float)Math.PI * lspeed * 0.4f;
        if ((double)newangle > 0.5) {
        newangle = 0.75f;
        }
        if ((double)newangle < -0.5) {
        newangle = -0.75f;
        }
        this.leftleg.xRot = -0.297f + newangle;
        this.LLeg1.xRot = 0.483f + newangle;
        this.LLeg2.xRot = -0.437f + newangle;
        this.Lfoot1.xRot = newangle;
        this.Lfoot2.xRot = newangle;
        this.Lfoot3.xRot = newangle;
        this.Lfoot4.xRot = newangle;
        this.LClaw1.xRot = newangle;
        this.LClaw2.xRot = newangle;
        this.LClaw3.xRot = newangle;
        this.LClaw4.xRot = newangle;
        this.rightleg.xRot = -0.297f - newangle;
        this.Rleg1.xRot = 0.483f - newangle;
        this.RLeg2.xRot = -0.437f - newangle;
        this.Rfoot1.xRot = -newangle;
        this.Rfoot2.xRot = -newangle;
        this.Rfoot3.xRot = -newangle;
        this.Rfoot4.xRot = -newangle;
        this.Rclaw1.xRot = -newangle;
        this.Rclaw2.xRot = -newangle;
        this.Rclaw3.xRot = -newangle;
        this.Rclaw4.xRot = -newangle;
        this.Tail2.xRot = this.Tail1.xRot = -0.594f + Mth.cos((float)(ageInTicks * 0.05f)) * (float)Math.PI * 0.06f;
        this.Tail3.xRot = this.Tail1.xRot;
        this.Tail3.yRot = -0.334f + Mth.cos((float)(ageInTicks * 0.061f)) * (float)Math.PI * 0.08f;
        this.Tail2.yRot = 0.334f - Mth.cos((float)(ageInTicks * 0.072f)) * (float)Math.PI * 0.08f;
        if (!entity.getPassengers().isEmpty()) {
        netHeadYaw = (entity.yBodyRotO - entity.yBodyRot) * 20.0f;
        netHeadYaw = -netHeadYaw;
        rf1 += (netHeadYaw - rf1) / 60.0f;
        if (rf1 > 50.0f) {
        rf1 = 50.0f;
        }
        if (rf1 < -50.0f) {
        rf1 = -50.0f;
        }
        netHeadYaw = rf1;
        } else {
        netHeadYaw /= 2.0f;
        }
        if (entity.isInSittingPose()) {
        netHeadYaw = 0.0f;
        this.head.xRot = this.Head1.xRot = 3.1415f;
        this.mouth1.xRot = this.Head1.xRot;
        this.Neck1.xRot = this.Head1.xRot;
        this.Hat1.xRot = this.Head1.xRot;
        this.Hat2.xRot = this.Head1.xRot;
        } else {
        this.head.xRot = this.Head1.xRot = 0.0f;
        this.mouth1.xRot = this.Head1.xRot;
        this.Neck1.xRot = this.Head1.xRot;
        this.Hat1.xRot = this.Head1.xRot;
        this.Hat2.xRot = this.Head1.xRot;
        }
        this.head.yRot = this.Head1.yRot = (float)Math.toRadians(netHeadYaw) * 0.65f;
        this.mouth1.yRot = this.Head1.yRot;
        this.Hat1.yRot = this.Head1.yRot;
        this.Hat2.yRot = this.Head1.yRot;
        newangle = Mth.cos((float)(ageInTicks * 1.0f * limbSwingAmount)) * (float)Math.PI * 0.15f;
        nextangle = Mth.cos((float)((ageInTicks + 0.3f) * 1.0f * limbSwingAmount)) * (float)Math.PI * 0.15f;
        if (nextangle > 0.0f && newangle < 0.0f) {
        ri1 = 0;
        if (entity.getRandom().nextInt(3) == 1) {
        ri1 = 1;
        }
        }
        if (ri1 == 0) {
        newangle = 0.0f;
        }
        newangle = Math.abs(newangle);
        this.Lwing.zRot = -newangle;
        this.Lwing.yRot = newangle / 2.0f;
        this.Rwing.zRot = newangle;
        this.Rwing.yRot = -newangle / 2.0f;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.Body1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.body2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.LLeg1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Rleg1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.LLeg2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Lfoot1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.RLeg2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Lfoot2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Lfoot3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.LClaw1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.LClaw2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.LClaw3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Lfoot4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.LClaw4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Rfoot1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Rfoot2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Rclaw1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Rfoot3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Rclaw3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Rfoot4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Rclaw2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Rclaw4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Body3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Tail1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Tail2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Tail3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Body4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.head.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leftleg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Neck1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Head1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.mouth1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.neck2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rightleg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Lwing.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Rwing.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Hat1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Hat2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
