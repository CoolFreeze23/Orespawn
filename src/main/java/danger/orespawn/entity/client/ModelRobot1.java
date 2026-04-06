package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

public class ModelRobot1 extends EntityModel<Entity> {
    private final ModelPart Shape1;
    private final ModelPart Shape2;
    private final ModelPart Shape2a;
    private final ModelPart Shape3;
    private final ModelPart Shape4;
    private final ModelPart Shape5;
    private final ModelPart Shape6;
    private final ModelPart Shape7;
    private final ModelPart Shape8;
    private final ModelPart Shape9;
    private final ModelPart Shape10;
    private final ModelPart Shape11;
    private final ModelPart Shape12;
    private final ModelPart Shape13;
    private final ModelPart Shape14;
    private final ModelPart Shape15;
    private final ModelPart Shape15a;
    private final ModelPart Shape16;
    private final ModelPart Shape17;
    private final ModelPart Shape18;
    private final ModelPart rfoot;
    private final ModelPart lfoot;
    private final ModelPart key1;
    private final ModelPart key2;
    private final ModelPart key3;
    private final ModelPart key4;
    private final ModelPart key5;

    public ModelRobot1(ModelPart root) {
        this.Shape1 = root.getChild("Shape1");
        this.Shape2 = root.getChild("Shape2");
        this.Shape2a = root.getChild("Shape2a");
        this.Shape3 = root.getChild("Shape3");
        this.Shape4 = root.getChild("Shape4");
        this.Shape5 = root.getChild("Shape5");
        this.Shape6 = root.getChild("Shape6");
        this.Shape7 = root.getChild("Shape7");
        this.Shape8 = root.getChild("Shape8");
        this.Shape9 = root.getChild("Shape9");
        this.Shape10 = root.getChild("Shape10");
        this.Shape11 = root.getChild("Shape11");
        this.Shape12 = root.getChild("Shape12");
        this.Shape13 = root.getChild("Shape13");
        this.Shape14 = root.getChild("Shape14");
        this.Shape15 = root.getChild("Shape15");
        this.Shape15a = root.getChild("Shape15a");
        this.Shape16 = root.getChild("Shape16");
        this.Shape17 = root.getChild("Shape17");
        this.Shape18 = root.getChild("Shape18");
        this.rfoot = root.getChild("rfoot");
        this.lfoot = root.getChild("lfoot");
        this.key1 = root.getChild("key1");
        this.key2 = root.getChild("key2");
        this.key3 = root.getChild("key3");
        this.key4 = root.getChild("key4");
        this.key5 = root.getChild("key5");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("Shape1",
                CubeListBuilder.create().texOffs(0, 0).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 3, 9, 3),
                PartPose.offset(-1.0F, 13.0F, -1.0F));

        partdefinition.addOrReplaceChild("Shape2",
                CubeListBuilder.create().texOffs(0, 0).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 1, 9, 5),
                PartPose.offset(0.0F, 13.0F, -2.0F));

        partdefinition.addOrReplaceChild("Shape2a",
                CubeListBuilder.create().texOffs(0, 0).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 5, 9, 1),
                PartPose.offset(-2.0F, 13.0F, 0.0F));

        partdefinition.addOrReplaceChild("Shape3",
                CubeListBuilder.create().texOffs(0, 0).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 7, 7, 3),
                PartPose.offset(-3.0F, 14.0F, -1.0F));

        partdefinition.addOrReplaceChild("Shape4",
                CubeListBuilder.create().texOffs(0, 0).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 3, 7, 7),
                PartPose.offset(-1.0F, 14.0F, -3.0F));

        partdefinition.addOrReplaceChild("Shape5",
                CubeListBuilder.create().texOffs(0, 0).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 5, 7, 5),
                PartPose.offset(-2.0F, 14.0F, -2.0F));

        partdefinition.addOrReplaceChild("Shape6",
                CubeListBuilder.create().texOffs(0, 0).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 5, 5, 7),
                PartPose.offset(-2.0F, 15.0F, -3.0F));

        partdefinition.addOrReplaceChild("Shape7",
                CubeListBuilder.create().texOffs(0, 0).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 1, 5, 1),
                PartPose.offset(0.0F, 15.0F, 4.0F));

        partdefinition.addOrReplaceChild("Shape8",
                CubeListBuilder.create().texOffs(0, 0).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 7, 5, 5),
                PartPose.offset(-3.0F, 15.0F, -2.0F));

        partdefinition.addOrReplaceChild("Shape9",
                CubeListBuilder.create().texOffs(0, 0).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 9, 5, 1),
                PartPose.offset(-4.0F, 15.0F, 0.0F));

        partdefinition.addOrReplaceChild("Shape10",
                CubeListBuilder.create().texOffs(0, 0).mirror()
                        .addBox(0.0F, 0.0F, 1.0F, 3, 3, 8),
                PartPose.offset(-1.0F, 16.0F, -4.0F));

        partdefinition.addOrReplaceChild("Shape11",
                CubeListBuilder.create().texOffs(0, 0).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 9, 3, 3),
                PartPose.offset(-4.0F, 16.0F, -1.0F));

        partdefinition.addOrReplaceChild("Shape12",
                CubeListBuilder.create().texOffs(0, 0).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 7, 3, 7),
                PartPose.offset(-3.0F, 16.0F, -3.0F));

        partdefinition.addOrReplaceChild("Shape13",
                CubeListBuilder.create().texOffs(0, 0).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 9, 1, 5),
                PartPose.offset(-4.0F, 17.0F, -2.0F));

        partdefinition.addOrReplaceChild("Shape14",
                CubeListBuilder.create().texOffs(0, 0).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 5, 1, 1),
                PartPose.offset(-2.0F, 17.0F, 4.0F));

        partdefinition.addOrReplaceChild("Shape15",
                CubeListBuilder.create().texOffs(32, 0).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 2, 3, 1),
                PartPose.offset(-2.0F, 15.0F, -4.0F));

        partdefinition.addOrReplaceChild("Shape15a",
                CubeListBuilder.create().texOffs(32, 0).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 2, 3, 1),
                PartPose.offset(1.0F, 15.0F, -4.0F));

        partdefinition.addOrReplaceChild("Shape16",
                CubeListBuilder.create().texOffs(45, 0).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 3, 1, 3),
                PartPose.offset(-1.0F, 12.0F, -1.0F));

        partdefinition.addOrReplaceChild("Shape17",
                CubeListBuilder.create().texOffs(33, 7).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 1, 2, 1),
                PartPose.offset(0.0F, 10.0F, 0.0F));

        partdefinition.addOrReplaceChild("Shape18",
                CubeListBuilder.create().texOffs(33, 7).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 1, 2, 1),
                PartPose.offsetAndRotation(1.7F, 8.733334F, 0.0F, 0.0F, 0.0F, 0.9667472F));

        partdefinition.addOrReplaceChild("rfoot",
                CubeListBuilder.create().texOffs(46, 8).mirror()
                        .addBox(0.0F, 3.0F, -2.0F, 2, 2, 4),
                PartPose.offset(-3.0F, 19.0F, 0.0F));

        partdefinition.addOrReplaceChild("lfoot",
                CubeListBuilder.create().texOffs(46, 8).mirror()
                        .addBox(0.0F, 3.0F, -2.0F, 2, 2, 4),
                PartPose.offset(2.0F, 19.0F, 0.0F));

        partdefinition.addOrReplaceChild("key1",
                CubeListBuilder.create().texOffs(46, 8).mirror()
                        .addBox(-0.5F, -0.5F, 0.0F, 1, 1, 3),
                PartPose.offset(0.5F, 17.5F, 5.0F));

        partdefinition.addOrReplaceChild("key2",
                CubeListBuilder.create().texOffs(46, 8).mirror()
                        .addBox(-0.5F, -1.5F, 1.0F, 1, 3, 1),
                PartPose.offset(0.5F, 17.5F, 5.0F));

        partdefinition.addOrReplaceChild("key3",
                CubeListBuilder.create().texOffs(46, 8).mirror()
                        .addBox(-0.5F, -2.5F, 1.0F, 1, 1, 2),
                PartPose.offset(0.5F, 17.5F, 5.0F));

        partdefinition.addOrReplaceChild("key4",
                CubeListBuilder.create().texOffs(46, 8).mirror()
                        .addBox(-0.5F, 1.5F, 1.0F, 1, 1, 2),
                PartPose.offset(0.5F, 17.5F, 5.0F));

        partdefinition.addOrReplaceChild("key5",
                CubeListBuilder.create().texOffs(46, 8).mirror()
                        .addBox(-0.5F, -1.5F, 3.0F, 1, 3, 1),
                PartPose.offset(0.5F, 17.5F, 5.0F));

        return LayerDefinition.create(meshdefinition, 64, 32);
    }

    @Override
    public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float walkAngle = limbSwingAmount > 0.1F
                ? Mth.cos(limbSwing * 1.5F) * (float) Math.PI * 0.75F * limbSwingAmount
                : 0.0F;
        this.rfoot.xRot = -walkAngle;
        this.lfoot.xRot = walkAngle;

        float keyRot = (float) Math.toRadians(ageInTicks * 0.75F);
        this.key1.zRot = keyRot;
        this.key2.zRot = keyRot;
        this.key3.zRot = keyRot;
        this.key4.zRot = keyRot;
        this.key5.zRot = keyRot;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.Shape1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Shape2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Shape2a.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Shape3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Shape4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Shape5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Shape6.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Shape7.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Shape8.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Shape9.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Shape10.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Shape11.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Shape12.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Shape13.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Shape14.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Shape15.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Shape15a.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Shape16.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Shape17.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Shape18.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rfoot.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lfoot.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.key1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.key2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.key3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.key4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.key5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
