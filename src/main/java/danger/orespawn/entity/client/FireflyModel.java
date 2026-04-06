package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.Firefly;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class FireflyModel extends EntityModel<Firefly> {
    private final ModelPart body;
    private final ModelPart wing_left;
    private final ModelPart wing_right;
    private final ModelPart head;
    private final ModelPart mouth;
    private final ModelPart eye_left;
    private final ModelPart eye_right;
    private final ModelPart front_leg_left_;
    private final ModelPart front_leg_right;
    private final ModelPart back_leg_left;
    private final ModelPart back_leg_right;
    private final ModelPart TailLight;

    public FireflyModel(ModelPart root) {
        this.body = root.getChild("body");
        this.wing_left = root.getChild("wing_left");
        this.wing_right = root.getChild("wing_right");
        this.head = root.getChild("head");
        this.mouth = root.getChild("mouth");
        this.eye_left = root.getChild("eye_left");
        this.eye_right = root.getChild("eye_right");
        this.front_leg_left_ = root.getChild("front_leg_left_");
        this.front_leg_right = root.getChild("front_leg_right");
        this.back_leg_left = root.getChild("back_leg_left");
        this.back_leg_right = root.getChild("back_leg_right");
        this.TailLight = root.getChild("TailLight");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(38, 12).mirror()
                        .addBox(-3.0F, -3.0F, -3.0F, 5, 5, 5),
                PartPose.offset(-1.0F, 9.0F, -1.0F));

        partdefinition.addOrReplaceChild("wing_left",
                CubeListBuilder.create().texOffs(46, 0).mirror()
                        .addBox(0.0F, -6.0F, 0.0F, 0, 6, 2),
                PartPose.offsetAndRotation(1.0F, 6.0F, -2.0F, 0.0F, 0.0174533F, 0.6981317F));

        partdefinition.addOrReplaceChild("wing_right",
                CubeListBuilder.create().texOffs(53, 0).mirror()
                        .addBox(0.0F, -6.0F, 0.0F, 0, 6, 2),
                PartPose.offsetAndRotation(-4.0F, 6.0F, -2.0F, 0.0F, 0.0F, -0.6981317F));

        partdefinition.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(3, 14).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 3, 3, 3),
                PartPose.offsetAndRotation(-3.0F, 7.0F, -7.0F, 0.2230717F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("mouth",
                CubeListBuilder.create().texOffs(26, 15).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 1, 1, 3),
                PartPose.offsetAndRotation(-2.0F, 9.0F, -8.0F, 0.2117115F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("eye_left",
                CubeListBuilder.create().texOffs(18, 12).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 1, 2, 2),
                PartPose.offsetAndRotation(-1.0F, 6.5F, -6.0F, 0.0174533F, 0.2602503F, -0.2230717F));

        partdefinition.addOrReplaceChild("eye_right",
                CubeListBuilder.create().texOffs(18, 18).mirror()
                        .addBox(1.0F, -0.6F, -0.6F, 1, 2, 2),
                PartPose.offsetAndRotation(-4.0F, 6.5F, -6.0F, 0.0F, -0.2602503F, 0.2230717F));

        partdefinition.addOrReplaceChild("front_leg_left_",
                CubeListBuilder.create().texOffs(32, 0).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 1, 5, 1),
                PartPose.offsetAndRotation(-1.0F, 10.0F, -3.0F, -0.2792527F, 0.0F, -0.2792527F));

        partdefinition.addOrReplaceChild("front_leg_right",
                CubeListBuilder.create().texOffs(22, 0).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 1, 5, 1),
                PartPose.offsetAndRotation(-3.0F, 10.0F, -3.0F, -0.2792527F, 0.0F, 0.2792527F));

        partdefinition.addOrReplaceChild("back_leg_left",
                CubeListBuilder.create().texOffs(11, 0).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 1, 5, 1),
                PartPose.offsetAndRotation(-1.0F, 10.0F, -1.0F, 0.2792527F, 0.0F, -0.2792527F));

        partdefinition.addOrReplaceChild("back_leg_right",
                CubeListBuilder.create().texOffs(2, 0).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 1, 5, 1),
                PartPose.offsetAndRotation(-3.0F, 10.0F, -1.0F, 0.2792527F, 0.0F, 0.2792527F));

        partdefinition.addOrReplaceChild("TailLight",
                CubeListBuilder.create().texOffs(10, 27).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 3, 3, 4),
                PartPose.offset(-3.0F, 6.0F, 1.0F));

        return LayerDefinition.create(meshdefinition, 64, 128);
    }

    @Override
    public void setupAnim(Firefly entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.wing_left.zRot = 1.11f + Mth.cos((float)(ageInTicks * limbSwingAmount)) * (float)Math.PI * 0.35f;
        this.wing_right.zRot = -1.11f - Mth.cos((float)(ageInTicks * limbSwingAmount)) * (float)Math.PI * 0.35f;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.wing_left.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.wing_right.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.head.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.mouth.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.eye_left.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.eye_right.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.front_leg_left_.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.front_leg_right.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.back_leg_left.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.back_leg_right.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.TailLight.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
