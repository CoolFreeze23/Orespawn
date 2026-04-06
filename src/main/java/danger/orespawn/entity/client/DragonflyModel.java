package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.EntityDragonfly;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class DragonflyModel extends EntityModel<EntityDragonfly> {
    private final ModelPart Shape1;
    private final ModelPart lfwing;
    private final ModelPart Shape3;
    private final ModelPart Shape4;
    private final ModelPart Shape5;
    private final ModelPart rjaw;
    private final ModelPart ljaw;
    private final ModelPart tail1;
    private final ModelPart tail2;
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
    private final ModelPart lrwing;
    private final ModelPart rfwing;
    private final ModelPart rrwing;

    public DragonflyModel(ModelPart root) {
        this.Shape1 = root.getChild("Shape1");
        this.lfwing = root.getChild("lfwing");
        this.Shape3 = root.getChild("Shape3");
        this.Shape4 = root.getChild("Shape4");
        this.Shape5 = root.getChild("Shape5");
        this.rjaw = root.getChild("rjaw");
        this.ljaw = root.getChild("ljaw");
        this.tail1 = root.getChild("tail1");
        this.tail2 = root.getChild("tail2");
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
        this.lrwing = root.getChild("lrwing");
        this.rfwing = root.getChild("rfwing");
        this.rrwing = root.getChild("rrwing");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("Shape1",
                CubeListBuilder.create().texOffs(0, 0).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 5, 4, 7),
                PartPose.offset(0.0F, 16.0F, 0.0F));

        partdefinition.addOrReplaceChild("lfwing",
                CubeListBuilder.create().texOffs(0, 33).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 10, 1, 3),
                PartPose.offsetAndRotation(5.0F, 16.0F, 1.0F, 0.0F, 0.4886922F, 0.0F));

        partdefinition.addOrReplaceChild("Shape3",
                CubeListBuilder.create().texOffs(0, 13).mirror()
                        .addBox(-2.0F, 0.0F, -4.0F, 4, 3, 4),
                PartPose.offsetAndRotation(2.5F, 16.0F, -1.0F, 0.4886922F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("Shape4",
                CubeListBuilder.create().texOffs(9, 21).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 1, 2, 3),
                PartPose.offsetAndRotation(1.0F, 18.0F, -6.0F, 0.4886922F, 0.1745329F, 0.0F));

        partdefinition.addOrReplaceChild("Shape5",
                CubeListBuilder.create().texOffs(0, 21).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 1, 2, 3),
                PartPose.offsetAndRotation(3.0F, 18.0F, -6.0F, 0.4886922F, -0.1745329F, 0.0F));

        partdefinition.addOrReplaceChild("rjaw",
                CubeListBuilder.create().texOffs(0, 27).mirror()
                        .addBox(-1.0F, 0.0F, 0.0F, 1, 3, 1),
                PartPose.offsetAndRotation(2.0F, 19.0F, -5.0F, 0.4363323F, 0.1745329F, 0.0F));

        partdefinition.addOrReplaceChild("ljaw",
                CubeListBuilder.create().texOffs(5, 27).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 1, 3, 1),
                PartPose.offsetAndRotation(3.0F, 19.0F, -5.0F, 0.4363323F, -0.1745329F, 0.0F));

        partdefinition.addOrReplaceChild("tail1",
                CubeListBuilder.create().texOffs(25, 0).mirror()
                        .addBox(-1.0F, 0.0F, 0.0F, 3, 3, 7),
                PartPose.offset(2.0F, 16.0F, 7.0F));

        partdefinition.addOrReplaceChild("tail2",
                CubeListBuilder.create().texOffs(25, 11).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 1, 2, 9),
                PartPose.offset(2.0F, 16.0F, 14.0F));

        partdefinition.addOrReplaceChild("Shape10",
                CubeListBuilder.create().texOffs(23, 0).mirror()
                        .addBox(-1.0F, 0.0F, 0.0F, 1, 4, 1),
                PartPose.offsetAndRotation(1.0F, 18.0F, 0.0F, -0.2792527F, 0.0F, 0.3490659F));

        partdefinition.addOrReplaceChild("Shape11",
                CubeListBuilder.create().texOffs(40, 0).mirror()
                        .addBox(0.0F, 0.0F, -4.0F, 1, 1, 4),
                PartPose.offset(-1.0F, 21.0F, 0.0F));

        partdefinition.addOrReplaceChild("Shape12",
                CubeListBuilder.create().texOffs(18, 12).mirror()
                        .addBox(-1.0F, 0.0F, 0.0F, 1, 3, 1),
                PartPose.offsetAndRotation(0.0F, 21.0F, -4.0F, 0.0F, 0.0F, -0.1919862F));

        partdefinition.addOrReplaceChild("Shape13",
                CubeListBuilder.create().texOffs(18, 0).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 1, 4, 1),
                PartPose.offsetAndRotation(4.0F, 18.0F, 0.0F, -0.2792527F, 0.0F, -0.3490659F));

        partdefinition.addOrReplaceChild("Shape14",
                CubeListBuilder.create().texOffs(51, 0).mirror()
                        .addBox(0.0F, 0.0F, -4.0F, 1, 1, 4),
                PartPose.offset(5.0F, 21.0F, 0.0F));

        partdefinition.addOrReplaceChild("Shape15",
                CubeListBuilder.create().texOffs(13, 12).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 1, 3, 1),
                PartPose.offsetAndRotation(5.0F, 21.0F, -4.0F, 0.0F, 0.0F, 0.1919862F));

        partdefinition.addOrReplaceChild("Shape16",
                CubeListBuilder.create().texOffs(9, 53).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 3, 1, 1),
                PartPose.offsetAndRotation(5.0F, 19.5F, 3.0F, 0.0F, 0.0F, 0.6457718F));

        partdefinition.addOrReplaceChild("Shape17",
                CubeListBuilder.create().texOffs(0, 56).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 1, 3, 1),
                PartPose.offset(6.0F, 21.0F, 3.0F));

        partdefinition.addOrReplaceChild("Shape18",
                CubeListBuilder.create().texOffs(0, 53).mirror()
                        .addBox(-3.0F, 0.0F, 0.0F, 3, 1, 1),
                PartPose.offsetAndRotation(0.0F, 19.5F, 3.0F, 0.0F, 0.0F, -0.6457718F));

        partdefinition.addOrReplaceChild("Shape19",
                CubeListBuilder.create().texOffs(5, 56).mirror()
                        .addBox(-1.0F, 0.0F, 0.0F, 1, 3, 1),
                PartPose.offset(-1.0F, 21.0F, 3.0F));

        partdefinition.addOrReplaceChild("Shape20",
                CubeListBuilder.create().texOffs(9, 61).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 3, 1, 1),
                PartPose.offsetAndRotation(4.0F, 19.5F, 6.0F, 0.0F, -0.6457718F, 0.5061455F));

        partdefinition.addOrReplaceChild("Shape21",
                CubeListBuilder.create().texOffs(0, 61).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 3, 1, 1),
                PartPose.offsetAndRotation(1.5F, 19.5F, 7.0F, 0.0F, -2.391101F, 0.5061455F));

        partdefinition.addOrReplaceChild("Shape22",
                CubeListBuilder.create().texOffs(0, 0).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 1, 3, 1),
                PartPose.offset(-1.0F, 21.0F, 7.5F));

        partdefinition.addOrReplaceChild("Shape23",
                CubeListBuilder.create().texOffs(0, 13).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 1, 3, 1),
                PartPose.offset(5.0F, 21.0F, 7.5F));

        partdefinition.addOrReplaceChild("lrwing",
                CubeListBuilder.create().texOffs(0, 38).mirror()
                        .addBox(0.0F, 0.0F, -3.0F, 10, 1, 3),
                PartPose.offsetAndRotation(5.0F, 16.0F, 6.0F, 0.0F, -0.3839724F, 0.0F));

        partdefinition.addOrReplaceChild("rfwing",
                CubeListBuilder.create().texOffs(0, 48).mirror()
                        .addBox(-10.0F, 0.0F, 0.0F, 10, 1, 3),
                PartPose.offsetAndRotation(0.0F, 16.0F, 1.0F, 0.0F, -0.4886922F, 0.0F));

        partdefinition.addOrReplaceChild("rrwing",
                CubeListBuilder.create().texOffs(0, 43).mirror()
                        .addBox(-10.0F, 0.0F, -3.0F, 10, 1, 3),
                PartPose.offsetAndRotation(0.0F, 16.0F, 6.0F, 0.0F, 0.3839724F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(EntityDragonfly entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float newangle = 0.0f;
        this.lfwing.zRot = newangle = Mth.cos((float)(ageInTicks * 1.3f * limbSwingAmount)) * (float)Math.PI * 0.25f;
        this.rfwing.zRot = -newangle;
        this.lrwing.zRot = newangle + 3.14f;
        this.rrwing.zRot = -newangle + 3.14f;
        this.ljaw.xRot = newangle = Mth.cos((float)(ageInTicks * 0.3f * limbSwingAmount)) * (float)Math.PI * 0.1f;
        this.rjaw.xRot = -newangle;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.Shape1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lfwing.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Shape3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Shape4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Shape5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rjaw.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.ljaw.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tail1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tail2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
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
        this.lrwing.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rfwing.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rrwing.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
