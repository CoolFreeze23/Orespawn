package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.EntityDragonfly;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class DragonflyModel<T extends EntityDragonfly> extends EntityModel<T> {
    private final ModelPart body;
    private final ModelPart lfwing;
    private final ModelPart headPart;
    private final ModelPart shape4;
    private final ModelPart shape5;
    private final ModelPart rjaw;
    private final ModelPart ljaw;
    private final ModelPart tail1;
    private final ModelPart tail2;
    private final ModelPart leg1;
    private final ModelPart leg2;
    private final ModelPart leg3;
    private final ModelPart leg4;
    private final ModelPart lrwing;
    private final ModelPart rfwing;
    private final ModelPart rrwing;

    public DragonflyModel(ModelPart root) {
        this.body = root.getChild("body");
        this.lfwing = root.getChild("lfwing");
        this.headPart = root.getChild("headpart");
        this.shape4 = root.getChild("shape4");
        this.shape5 = root.getChild("shape5");
        this.rjaw = root.getChild("rjaw");
        this.ljaw = root.getChild("ljaw");
        this.tail1 = root.getChild("tail1");
        this.tail2 = root.getChild("tail2");
        this.leg1 = root.getChild("leg1");
        this.leg2 = root.getChild("leg2");
        this.leg3 = root.getChild("leg3");
        this.leg4 = root.getChild("leg4");
        this.lrwing = root.getChild("lrwing");
        this.rfwing = root.getChild("rfwing");
        this.rrwing = root.getChild("rrwing");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(0.0F, 0.0F, 0.0F, 5, 4, 7), PartPose.offset(0.0F, 16.0F, 0.0F));
        root.addOrReplaceChild("lfwing", CubeListBuilder.create().texOffs(0, 33).mirror().addBox(0.0F, 0.0F, 0.0F, 10, 1, 3), PartPose.offsetAndRotation(5.0F, 16.0F, 1.0F, 0.0F, 0.4886922F, 0.0F));
        root.addOrReplaceChild("headpart", CubeListBuilder.create().texOffs(0, 13).mirror().addBox(-2.0F, 0.0F, -4.0F, 4, 3, 4), PartPose.offsetAndRotation(2.5F, 16.0F, -1.0F, 0.4886922F, 0.0F, 0.0F));
        root.addOrReplaceChild("shape4", CubeListBuilder.create().texOffs(9, 21).mirror().addBox(0.0F, 0.0F, 0.0F, 1, 2, 3), PartPose.offsetAndRotation(1.0F, 18.0F, -6.0F, 0.4886922F, 0.1745329F, 0.0F));
        root.addOrReplaceChild("shape5", CubeListBuilder.create().texOffs(0, 21).mirror().addBox(0.0F, 0.0F, 0.0F, 1, 2, 3), PartPose.offsetAndRotation(3.0F, 18.0F, -6.0F, 0.4886922F, -0.1745329F, 0.0F));
        root.addOrReplaceChild("rjaw", CubeListBuilder.create().texOffs(0, 27).mirror().addBox(-1.0F, 0.0F, 0.0F, 1, 3, 1), PartPose.offsetAndRotation(2.0F, 19.0F, -5.0F, 0.4363323F, 0.1745329F, 0.0F));
        root.addOrReplaceChild("ljaw", CubeListBuilder.create().texOffs(5, 27).mirror().addBox(0.0F, 0.0F, 0.0F, 1, 3, 1), PartPose.offsetAndRotation(3.0F, 19.0F, -5.0F, 0.4363323F, -0.1745329F, 0.0F));
        root.addOrReplaceChild("tail1", CubeListBuilder.create().texOffs(25, 0).mirror().addBox(-1.0F, 0.0F, 0.0F, 3, 3, 7), PartPose.offset(2.0F, 16.0F, 7.0F));
        root.addOrReplaceChild("tail2", CubeListBuilder.create().texOffs(25, 11).mirror().addBox(0.0F, 0.0F, 0.0F, 1, 2, 9), PartPose.offset(2.0F, 16.0F, 14.0F));
        root.addOrReplaceChild("leg1", CubeListBuilder.create().texOffs(23, 0).mirror().addBox(-1.0F, 0.0F, 0.0F, 1, 4, 1), PartPose.offsetAndRotation(1.0F, 18.0F, 0.0F, -0.2792527F, 0.0F, 0.3490659F));
        root.addOrReplaceChild("leg2", CubeListBuilder.create().texOffs(18, 0).mirror().addBox(0.0F, 0.0F, 0.0F, 1, 4, 1), PartPose.offsetAndRotation(4.0F, 18.0F, 0.0F, -0.2792527F, 0.0F, -0.3490659F));
        root.addOrReplaceChild("leg3", CubeListBuilder.create().texOffs(9, 53).mirror().addBox(0.0F, 0.0F, 0.0F, 3, 1, 1), PartPose.offsetAndRotation(5.0F, 19.5F, 3.0F, 0.0F, 0.0F, 0.6457718F));
        root.addOrReplaceChild("leg4", CubeListBuilder.create().texOffs(0, 53).mirror().addBox(-3.0F, 0.0F, 0.0F, 3, 1, 1), PartPose.offsetAndRotation(0.0F, 19.5F, 3.0F, 0.0F, 0.0F, -0.6457718F));
        root.addOrReplaceChild("lrwing", CubeListBuilder.create().texOffs(0, 38).mirror().addBox(0.0F, 0.0F, -3.0F, 10, 1, 3), PartPose.offsetAndRotation(5.0F, 16.0F, 6.0F, 0.0F, -0.3839724F, 0.0F));
        root.addOrReplaceChild("rfwing", CubeListBuilder.create().texOffs(0, 48).mirror().addBox(-10.0F, 0.0F, 0.0F, 10, 1, 3), PartPose.offsetAndRotation(0.0F, 16.0F, 1.0F, 0.0F, -0.4886922F, 0.0F));
        root.addOrReplaceChild("rrwing", CubeListBuilder.create().texOffs(0, 43).mirror().addBox(-10.0F, 0.0F, -3.0F, 10, 1, 3), PartPose.offsetAndRotation(0.0F, 16.0F, 6.0F, 0.0F, 0.3839724F, 0.0F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float wingAngle = Mth.cos(ageInTicks * 1.3f) * (float) Math.PI * 0.25f;
        this.lfwing.zRot = wingAngle;
        this.rfwing.zRot = -wingAngle;
        this.lrwing.zRot = wingAngle + 3.14f;
        this.rrwing.zRot = -wingAngle + 3.14f;
        float jawAngle = Mth.cos(ageInTicks * 0.3f) * (float) Math.PI * 0.1f;
        this.ljaw.xRot = jawAngle + 0.4363323f;
        this.rjaw.xRot = -jawAngle + 0.4363323f;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        lfwing.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        headPart.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        shape4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        shape5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        rjaw.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        ljaw.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        tail1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        tail2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        leg1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        leg2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        leg3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        leg4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        lrwing.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        rfwing.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        rrwing.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
