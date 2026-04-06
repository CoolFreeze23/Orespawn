package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.Entity;

public class ModelSpiderRobot extends EntityModel<Entity> {
    private final ModelPart bodyCenter;
    private final ModelPart abdomen;
    private final ModelPart head;
    private final ModelPart ljaw1;
    private final ModelPart rjaw1;
    private final ModelPart ljaw2;
    private final ModelPart rjaw2;
    private final ModelPart ljaw3;
    private final ModelPart rjaw3;
    private final ModelPart tail;
    private final ModelPart headSpike1;
    private final ModelPart headSpike2;
    private final ModelPart hip1;
    private final ModelPart hip2;
    private final ModelPart hip3;
    private final ModelPart hip4;
    private final ModelPart hip5;
    private final ModelPart hip6;
    private final ModelPart hip7;
    private final ModelPart hip8;

    public ModelSpiderRobot(ModelPart root) {
        this.bodyCenter = root.getChild("bodyCenter");
        this.abdomen = root.getChild("abdomen");
        this.head = root.getChild("head");
        this.ljaw1 = root.getChild("ljaw1");
        this.rjaw1 = root.getChild("rjaw1");
        this.ljaw2 = root.getChild("ljaw2");
        this.rjaw2 = root.getChild("rjaw2");
        this.ljaw3 = root.getChild("ljaw3");
        this.rjaw3 = root.getChild("rjaw3");
        this.tail = root.getChild("tail");
        this.headSpike1 = root.getChild("headSpike1");
        this.headSpike2 = root.getChild("headSpike2");
        this.hip1 = root.getChild("hip1");
        this.hip2 = root.getChild("hip2");
        this.hip3 = root.getChild("hip3");
        this.hip4 = root.getChild("hip4");
        this.hip5 = root.getChild("hip5");
        this.hip6 = root.getChild("hip6");
        this.hip7 = root.getChild("hip7");
        this.hip8 = root.getChild("hip8");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("bodyCenter",
                CubeListBuilder.create().texOffs(0, 321).mirror()
                        .addBox(-18.0F, -12.0F, -21.0F, 36, 24, 51),
                PartPose.offset(0.0F, -4.0F, 0.0F));

        partdefinition.addOrReplaceChild("abdomen",
                CubeListBuilder.create().texOffs(0, 398).mirror()
                        .addBox(-24.0F, -30.0F, 29.0F, 48, 40, 73),
                PartPose.offset(0.0F, -4.0F, 0.0F));

        partdefinition.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 256).mirror()
                        .addBox(-15.0F, -16.0F, -57.0F, 30, 26, 36),
                PartPose.offset(0.0F, -4.0F, 0.0F));

        partdefinition.addOrReplaceChild("ljaw1",
                CubeListBuilder.create().texOffs(75, 26).mirror()
                        .addBox(-4.0F, 0.0F, -4.0F, 8, 3, 8),
                PartPose.offset(14.0F, -3.0F, -56.0F));

        partdefinition.addOrReplaceChild("rjaw1",
                CubeListBuilder.create().texOffs(75, 26).mirror()
                        .addBox(-4.0F, 0.0F, -4.0F, 8, 3, 8),
                PartPose.offset(-14.0F, -3.0F, -56.0F));

        partdefinition.addOrReplaceChild("ljaw2",
                CubeListBuilder.create().texOffs(63, 40).mirror()
                        .addBox(0.0F, 1.0F, -3.0F, 21, 2, 6),
                PartPose.offsetAndRotation(14.0F, -3.0F, -56.0F, 0.0F, 0.7504916F, 0.0F));

        partdefinition.addOrReplaceChild("rjaw2",
                CubeListBuilder.create().texOffs(63, 40).mirror()
                        .addBox(0.0F, 1.0F, -3.0F, 21, 2, 6),
                PartPose.offsetAndRotation(-14.0F, -3.0F, -56.0F, 0.0F, 2.303835F, 0.0F));

        partdefinition.addOrReplaceChild("ljaw3",
                CubeListBuilder.create().texOffs(0, 18).mirror()
                        .addBox(11.0F, 2.0F, 14.0F, 23, 1, 4),
                PartPose.offsetAndRotation(14.0F, -3.0F, -56.0F, 0.0F, 1.710423F, 0.0F));

        partdefinition.addOrReplaceChild("rjaw3",
                CubeListBuilder.create().texOffs(0, 18).mirror()
                        .addBox(11.0F, 2.0F, -17.0F, 23, 1, 4),
                PartPose.offsetAndRotation(-14.0F, -3.0F, -56.0F, 0.0F, 1.413717F, 0.0F));

        partdefinition.addOrReplaceChild("tail",
                CubeListBuilder.create().texOffs(130, 0).mirror()
                        .addBox(-5.0F, -5.0F, -5.0F, 10, 10, 49),
                PartPose.offset(0.0F, -32.0F, 69.0F));

        partdefinition.addOrReplaceChild("headSpike1",
                CubeListBuilder.create().texOffs(74, 0).mirror()
                        .addBox(-1.0F, -1.0F, -10.0F, 2, 2, 21),
                PartPose.offset(6.0F, -20.0F, -60.0F));

        partdefinition.addOrReplaceChild("headSpike2",
                CubeListBuilder.create().texOffs(74, 0).mirror()
                        .addBox(-1.0F, -1.0F, -10.0F, 2, 2, 21),
                PartPose.offset(-6.0F, -20.0F, -60.0F));

        partdefinition.addOrReplaceChild("hip1",
                CubeListBuilder.create().texOffs(70, 60).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 10, 10, 10),
                PartPose.offset(22.0F, -3.0F, 44.0F));

        partdefinition.addOrReplaceChild("hip2",
                CubeListBuilder.create().texOffs(70, 60).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 10, 10, 10),
                PartPose.offset(-32.0F, -3.0F, 44.0F));

        partdefinition.addOrReplaceChild("hip3",
                CubeListBuilder.create().texOffs(70, 60).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 10, 10, 10),
                PartPose.offset(16.0F, -1.0F, 12.0F));

        partdefinition.addOrReplaceChild("hip4",
                CubeListBuilder.create().texOffs(70, 60).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 10, 10, 10),
                PartPose.offset(-26.0F, -1.0F, 12.0F));

        partdefinition.addOrReplaceChild("hip5",
                CubeListBuilder.create().texOffs(70, 60).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 10, 10, 10),
                PartPose.offset(16.0F, -1.0F, -11.0F));

        partdefinition.addOrReplaceChild("hip6",
                CubeListBuilder.create().texOffs(70, 60).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 10, 10, 10),
                PartPose.offset(-26.0F, -1.0F, -11.0F));

        partdefinition.addOrReplaceChild("hip7",
                CubeListBuilder.create().texOffs(70, 60).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 10, 10, 10),
                PartPose.offset(13.0F, -3.0F, -33.0F));

        partdefinition.addOrReplaceChild("hip8",
                CubeListBuilder.create().texOffs(70, 60).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 10, 10, 10),
                PartPose.offset(-23.0F, -3.0F, -33.0F));

        return LayerDefinition.create(meshdefinition, 256, 512);
    }

    @Override
    public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        bodyCenter.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        abdomen.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        head.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        ljaw1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        rjaw1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        ljaw2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        rjaw2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        ljaw3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        rjaw3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        tail.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        headSpike1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        headSpike2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        hip1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        hip2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        hip3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        hip4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        hip5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        hip6.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        hip7.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        hip8.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
