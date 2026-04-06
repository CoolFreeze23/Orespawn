package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import danger.orespawn.entity.Robot5;

public class ModelRobot5 extends EntityModel<Robot5> {
    private final ModelPart lwheel1;
    private final ModelPart lwheel2;
    private final ModelPart rwheel1;
    private final ModelPart rwheel2;
    private final ModelPart axle;
    private final ModelPart drivebox;
    private final ModelPart stand;
    private final ModelPart swivel;
    private final ModelPart barrel1;
    private final ModelPart barrel2;
    private final ModelPart ammobox;

    public ModelRobot5(ModelPart root) {
        this.lwheel1 = root.getChild("lwheel1");
        this.lwheel2 = root.getChild("lwheel2");
        this.rwheel1 = root.getChild("rwheel1");
        this.rwheel2 = root.getChild("rwheel2");
        this.axle = root.getChild("axle");
        this.drivebox = root.getChild("drivebox");
        this.stand = root.getChild("stand");
        this.swivel = root.getChild("swivel");
        this.barrel1 = root.getChild("barrel1");
        this.barrel2 = root.getChild("barrel2");
        this.ammobox = root.getChild("ammobox");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("lwheel1",
                CubeListBuilder.create().texOffs(0, 23).mirror()
                        .addBox(0.0F, -4.0F, -4.0F, 2, 8, 8),
                PartPose.offset(6.0F, 19.0F, 0.0F));

        partdefinition.addOrReplaceChild("lwheel2",
                CubeListBuilder.create().texOffs(0, 43).mirror()
                        .addBox(0.0F, -4.0F, -4.0F, 2, 8, 8),
                PartPose.offsetAndRotation(6.0F, 19.0F, 0.0F, 0.7853982F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("rwheel1",
                CubeListBuilder.create().texOffs(0, 23).mirror()
                        .addBox(0.0F, -4.0F, -4.0F, 2, 8, 8),
                PartPose.offset(-8.0F, 19.0F, 0.0F));

        partdefinition.addOrReplaceChild("rwheel2",
                CubeListBuilder.create().texOffs(0, 43).mirror()
                        .addBox(0.0F, -4.0F, -4.0F, 2, 8, 8),
                PartPose.offsetAndRotation(-8.0F, 19.0F, 0.0F, 0.7853982F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("axle",
                CubeListBuilder.create().texOffs(42, 0).mirror()
                        .addBox(-6.0F, -0.5F, -0.5F, 12, 1, 1),
                PartPose.offset(0.0F, 19.0F, 0.0F));

        partdefinition.addOrReplaceChild("drivebox",
                CubeListBuilder.create().texOffs(47, 4).mirror()
                        .addBox(-2.0F, -1.5F, -1.5F, 4, 3, 3),
                PartPose.offset(0.0F, 19.0F, 0.0F));

        partdefinition.addOrReplaceChild("stand",
                CubeListBuilder.create().texOffs(35, 0).mirror()
                        .addBox(-0.5F, 0.0F, -0.5F, 1, 18, 1),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("swivel",
                CubeListBuilder.create().texOffs(22, 0).mirror()
                        .addBox(-1.0F, 0.0F, -1.0F, 2, 1, 2),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("barrel1",
                CubeListBuilder.create().texOffs(24, 25).mirror()
                        .addBox(-1.0F, -2.0F, -10.0F, 2, 2, 13),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("barrel2",
                CubeListBuilder.create().texOffs(27, 43).mirror()
                        .addBox(-0.5F, -1.5F, -19.0F, 1, 1, 9),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("ammobox",
                CubeListBuilder.create().texOffs(0, 0).mirror()
                        .addBox(-2.0F, -2.0F, 3.0F, 4, 3, 5),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(Robot5 entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float wheelAngle;
        if (limbSwingAmount > 0.1F) {
            wheelAngle = limbSwing * 0.15F % ((float) Math.PI * 2);
            wheelAngle = Math.abs(wheelAngle);
        } else {
            wheelAngle = 0.0F;
        }
        this.lwheel1.xRot = wheelAngle;
        this.lwheel2.xRot = wheelAngle + 0.7853982F;
        this.rwheel1.xRot = wheelAngle;
        this.rwheel2.xRot = wheelAngle + 0.7853982F;

        float turretYaw = (float) Math.toRadians(netHeadYaw / 2.0);
        this.barrel1.yRot = turretYaw;
        this.barrel2.yRot = turretYaw;
        this.ammobox.yRot = turretYaw;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.lwheel1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lwheel2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rwheel1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rwheel2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.axle.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.drivebox.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.stand.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.swivel.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.barrel1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.barrel2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.ammobox.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
