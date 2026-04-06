package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import danger.orespawn.entity.Robot1;

public class ModelRobot1 extends EntityModel<Robot1> {
    private final ModelPart centerColumn;
    private final ModelPart strutZ;
    private final ModelPart strutX;
    private final ModelPart lowerTorso;
    private final ModelPart sideTorso;
    private final ModelPart midTorso;
    private final ModelPart upperTorso;
    private final ModelPart backPeg;
    private final ModelPart pelvisBlock;
    private final ModelPart waistSlab;
    private final ModelPart frontBar;
    private final ModelPart midBar;
    private final ModelPart upperCube;
    private final ModelPart collarPlate;
    private final ModelPart rearDetail;
    private final ModelPart finLeft;
    private final ModelPart finRight;
    private final ModelPart topCap;
    private final ModelPart rodUpright;
    private final ModelPart rodTilted;
    private final ModelPart rfoot;
    private final ModelPart lfoot;
    private final ModelPart key1;
    private final ModelPart key2;
    private final ModelPart key3;
    private final ModelPart key4;
    private final ModelPart key5;

    public ModelRobot1(ModelPart root) {
        this.centerColumn = root.getChild("Shape1");
        this.strutZ = root.getChild("Shape2");
        this.strutX = root.getChild("Shape2a");
        this.lowerTorso = root.getChild("Shape3");
        this.sideTorso = root.getChild("Shape4");
        this.midTorso = root.getChild("Shape5");
        this.upperTorso = root.getChild("Shape6");
        this.backPeg = root.getChild("Shape7");
        this.pelvisBlock = root.getChild("Shape8");
        this.waistSlab = root.getChild("Shape9");
        this.frontBar = root.getChild("Shape10");
        this.midBar = root.getChild("Shape11");
        this.upperCube = root.getChild("Shape12");
        this.collarPlate = root.getChild("Shape13");
        this.rearDetail = root.getChild("Shape14");
        this.finLeft = root.getChild("Shape15");
        this.finRight = root.getChild("Shape15a");
        this.topCap = root.getChild("Shape16");
        this.rodUpright = root.getChild("Shape17");
        this.rodTilted = root.getChild("Shape18");
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
    public void setupAnim(Robot1 entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
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
        this.centerColumn.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.strutZ.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.strutX.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lowerTorso.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.sideTorso.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.midTorso.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.upperTorso.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.backPeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.pelvisBlock.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.waistSlab.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.frontBar.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.midBar.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.upperCube.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.collarPlate.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rearDetail.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.finLeft.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.finRight.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.topCap.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rodUpright.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rodTilted.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rfoot.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lfoot.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.key1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.key2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.key3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.key4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.key5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
