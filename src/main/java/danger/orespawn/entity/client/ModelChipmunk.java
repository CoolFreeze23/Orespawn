package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.Chipmunk;
import danger.orespawn.entity.EntityCannonFodder;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class ModelChipmunk extends EntityModel<Chipmunk> {
    private static final float ANIM_SPEED = 1.0F;

    private final ModelPart cheek2;
    private final ModelPart leg1;
    private final ModelPart leg2;
    private final ModelPart leg3;
    private final ModelPart leg4;
    private final ModelPart tail2;
    private final ModelPart neck;
    private final ModelPart head;
    private final ModelPart mouthUnder;
    private final ModelPart cheek1;
    private final ModelPart ear2;
    private final ModelPart nose;
    private final ModelPart ear1;
    private final ModelPart body;
    private final ModelPart bodyTail;
    private final ModelPart tail1;
    private final ModelPart hat1;
    private final ModelPart hat2;

    public ModelChipmunk(ModelPart root) {
        this.cheek2 = root.getChild("cheek2");
        this.leg1 = root.getChild("leg1");
        this.leg2 = root.getChild("leg2");
        this.leg3 = root.getChild("leg3");
        this.leg4 = root.getChild("leg4");
        this.tail2 = root.getChild("tail2");
        this.neck = root.getChild("neck");
        this.head = root.getChild("head");
        this.mouthUnder = root.getChild("mouth_under");
        this.cheek1 = root.getChild("cheek1");
        this.ear2 = root.getChild("ear2");
        this.nose = root.getChild("nose");
        this.ear1 = root.getChild("ear1");
        this.body = root.getChild("body");
        this.bodyTail = root.getChild("body_tail");
        this.tail1 = root.getChild("tail1");
        this.hat1 = root.getChild("hat1");
        this.hat2 = root.getChild("hat2");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("cheek2",
                CubeListBuilder.create().texOffs(14, 0).mirror()
                        .addBox(0.5F, -1.5F, -3.5F, 2, 2, 2),
                PartPose.offset(0.0F, 20.0F, -3.0F));

        partdefinition.addOrReplaceChild("leg1",
                CubeListBuilder.create().texOffs(22, 7).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 1, 1, 1),
                PartPose.offset(-2.0F, 23.0F, -4.0F));

        partdefinition.addOrReplaceChild("leg2",
                CubeListBuilder.create().texOffs(22, 9).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 1, 1, 1),
                PartPose.offset(1.0F, 23.0F, -4.0F));

        partdefinition.addOrReplaceChild("leg3",
                CubeListBuilder.create().texOffs(22, 11).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 1, 1, 1),
                PartPose.offset(1.0F, 23.0F, 0.0F));

        partdefinition.addOrReplaceChild("leg4",
                CubeListBuilder.create().texOffs(22, 13).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 1, 1, 1),
                PartPose.offset(-2.0F, 23.0F, 0.0F));

        partdefinition.addOrReplaceChild("tail2",
                CubeListBuilder.create().texOffs(28, 15).mirror()
                        .addBox(-0.5F, 1.0F, 2.5F, 3, 3, 4),
                PartPose.offsetAndRotation(-1.0F, 20.0F, 1.0F, 0.7662421F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("neck",
                CubeListBuilder.create().texOffs(26, 9).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 3, 2, 4),
                PartPose.offsetAndRotation(-1.5F, 22.0F, -5.0F, 1.570796F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 0).mirror()
                        .addBox(-2.0F, -3.0F, 0.0F, 4, 4, 3),
                PartPose.offsetAndRotation(0.0F, 20.0F, -3.0F, 1.570796F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("mouth_under",
                CubeListBuilder.create().texOffs(20, 4).mirror()
                        .addBox(-1.0F, -1.9F, -3.8F, 2, 2, 1),
                PartPose.offset(0.0F, 20.0F, -3.0F));

        partdefinition.addOrReplaceChild("cheek1",
                CubeListBuilder.create().texOffs(22, 0).mirror()
                        .addBox(-2.5F, -1.5F, -3.5F, 2, 2, 2),
                PartPose.offset(0.0F, 20.0F, -3.0F));

        partdefinition.addOrReplaceChild("ear2",
                CubeListBuilder.create().texOffs(18, 11).mirror()
                        .addBox(1.0F, 0.0F, 3.0F, 1, 1, 1),
                PartPose.offsetAndRotation(0.0F, 20.0F, -3.0F, 1.570796F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("nose",
                CubeListBuilder.create().texOffs(18, 7).mirror()
                        .addBox(-0.5F, -2.0F, -4.2F, 1, 1, 1),
                PartPose.offset(0.0F, 20.0F, -3.0F));

        partdefinition.addOrReplaceChild("ear1",
                CubeListBuilder.create().texOffs(18, 9).mirror()
                        .addBox(-2.0F, 0.0F, 3.0F, 1, 1, 1),
                PartPose.offsetAndRotation(0.0F, 20.0F, -3.0F, 1.570796F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 7).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 4, 3, 5),
                PartPose.offset(-2.0F, 20.0F, -4.0F));

        partdefinition.addOrReplaceChild("body_tail",
                CubeListBuilder.create().texOffs(0, 15).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 5, 4, 3),
                PartPose.offset(-2.5F, 19.0F, -1.0F));

        partdefinition.addOrReplaceChild("tail1",
                CubeListBuilder.create().texOffs(16, 15).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 2, 2, 4),
                PartPose.offsetAndRotation(-1.0F, 20.0F, 1.0F, 0.3064968F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("hat1",
                CubeListBuilder.create().texOffs(40, 0).mirror()
                        .addBox(-2.5F, -4.0F, -4.0F, 5, 1, 5),
                PartPose.offset(0.0F, 20.0F, -3.0F));

        partdefinition.addOrReplaceChild("hat2",
                CubeListBuilder.create().texOffs(40, 0).mirror()
                        .addBox(-2.0F, -6.0F, -3.0F, 4, 2, 4),
                PartPose.offset(0.0F, 20.0F, -3.0F));

        return LayerDefinition.create(meshdefinition, 64, 32);
    }

    @Override
    public void setupAnim(Chipmunk entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float newangle = limbSwingAmount > 0.1F
                ? Mth.cos(ageInTicks * 2.3F * ANIM_SPEED) * (float) Math.PI * 0.25F * limbSwingAmount
                : 0.0F;
        this.leg1.xRot = newangle;
        this.leg3.xRot = newangle;
        this.leg2.xRot = -newangle;
        this.leg4.xRot = -newangle;

        float headYawRad = (float) Math.toRadians(netHeadYaw) * 0.45F;
        this.nose.yRot = headYawRad;
        this.head.yRot = headYawRad;
        this.ear1.yRot = headYawRad;
        this.ear2.yRot = headYawRad;
        this.mouthUnder.yRot = headYawRad;
        this.cheek1.yRot = headYawRad;
        this.cheek2.yRot = headYawRad;
        this.hat1.yRot = headYawRad;
        this.hat2.yRot = headYawRad;

        if (!entity.isSitting()) {
            this.tail1.xRot = 0.306F + Mth.cos(ageInTicks * 0.25F) * (float) Math.PI * 0.06F;
            newangle = Mth.cos(ageInTicks * 1.3F * ANIM_SPEED) * (float) Math.PI * 0.25F * limbSwingAmount;
            this.tail1.xRot += newangle;
            this.tail2.xRot = 0.306F + this.tail1.xRot;
        }

        this.hat1.visible = entity instanceof EntityCannonFodder cf && cf.getIsActivated() != 0;
        this.hat2.visible = entity instanceof EntityCannonFodder cf && cf.getIsActivated() > 1;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.cheek2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tail2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.neck.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.head.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.mouthUnder.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.cheek1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.ear2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.nose.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.ear1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.bodyTail.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tail1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.hat1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.hat2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
