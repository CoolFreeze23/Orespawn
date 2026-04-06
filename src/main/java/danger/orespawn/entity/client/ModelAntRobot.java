package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.Entity;

public class ModelAntRobot extends EntityModel<Entity> {
    private final ModelPart body;
    private final ModelPart abdomen;
    private final ModelPart head;
    private final ModelPart jet1;
    private final ModelPart jet2;
    private final ModelPart hip1;
    private final ModelPart hip2;
    private final ModelPart hip3;
    private final ModelPart hip4;
    private final ModelPart hip5;
    private final ModelPart hip6;
    private final ModelPart lJaw1;
    private final ModelPart rJaw1;
    private final ModelPart lJaw2;
    private final ModelPart rJaw2;
    private final ModelPart lAntenna;
    private final ModelPart rAntenna;

    public ModelAntRobot(ModelPart root) {
        this.body = root.getChild("body");
        this.abdomen = root.getChild("abdomen");
        this.head = root.getChild("head");
        this.jet1 = root.getChild("jet1");
        this.jet2 = root.getChild("jet2");
        this.hip1 = root.getChild("hip1");
        this.hip2 = root.getChild("hip2");
        this.hip3 = root.getChild("hip3");
        this.hip4 = root.getChild("hip4");
        this.hip5 = root.getChild("hip5");
        this.hip6 = root.getChild("hip6");
        this.lJaw1 = root.getChild("lJaw1");
        this.rJaw1 = root.getChild("rJaw1");
        this.lJaw2 = root.getChild("lJaw2");
        this.rJaw2 = root.getChild("rJaw2");
        this.lAntenna = root.getChild("lAntenna");
        this.rAntenna = root.getChild("rAntenna");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 151).mirror()
                        .addBox(-11.0F, 0.0F, -16.0F, 22, 14, 32),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("abdomen",
                CubeListBuilder.create().texOffs(0, 199).mirror()
                        .addBox(-15.0F, -10.0F, 16.0F, 30, 22, 34),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 120).mirror()
                        .addBox(-7.0F, 4.0F, -34.0F, 14, 11, 18),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("jet1",
                CubeListBuilder.create().texOffs(78, 0).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 6, 6, 18),
                PartPose.offset(8.0F, -12.0F, 35.0F));

        partdefinition.addOrReplaceChild("jet2",
                CubeListBuilder.create().texOffs(78, 0).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 6, 6, 18),
                PartPose.offset(-14.0F, -12.0F, 35.0F));

        partdefinition.addOrReplaceChild("hip1",
                CubeListBuilder.create().texOffs(0, 96).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 6, 6, 6),
                PartPose.offset(11.0F, 9.0F, -3.0F));

        partdefinition.addOrReplaceChild("hip2",
                CubeListBuilder.create().texOffs(0, 96).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 6, 6, 6),
                PartPose.offset(-17.0F, 9.0F, -3.0F));

        partdefinition.addOrReplaceChild("hip3",
                CubeListBuilder.create().texOffs(0, 96).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 6, 6, 6),
                PartPose.offset(-17.0F, 9.0F, 10.0F));

        partdefinition.addOrReplaceChild("hip4",
                CubeListBuilder.create().texOffs(0, 96).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 6, 6, 6),
                PartPose.offset(11.0F, 9.0F, 10.0F));

        partdefinition.addOrReplaceChild("hip5",
                CubeListBuilder.create().texOffs(0, 96).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 6, 6, 6),
                PartPose.offset(11.0F, 9.0F, -16.0F));

        partdefinition.addOrReplaceChild("hip6",
                CubeListBuilder.create().texOffs(0, 96).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 6, 6, 6),
                PartPose.offset(-17.0F, 9.0F, -16.0F));

        partdefinition.addOrReplaceChild("lJaw1",
                CubeListBuilder.create().texOffs(0, 33).mirror()
                        .addBox(-2.0F, 0.0F, -2.0F, 17, 1, 4),
                PartPose.offsetAndRotation(5.0F, 13.0F, -33.0F, 0.0F, 0.8901179F, 0.0F));

        partdefinition.addOrReplaceChild("rJaw1",
                CubeListBuilder.create().texOffs(0, 33).mirror()
                        .addBox(-2.0F, 0.0F, -2.0F, 17, 1, 4),
                PartPose.offsetAndRotation(-5.0F, 13.0F, -33.0F, 0.0F, 2.216568F, 0.0F));

        partdefinition.addOrReplaceChild("lJaw2",
                CubeListBuilder.create().texOffs(0, 27).mirror()
                        .addBox(12.0F, 0.0F, 5.0F, 17, 1, 3),
                PartPose.offsetAndRotation(5.0F, 13.0F, -33.0F, 0.0F, 1.37881F, 0.0F));

        partdefinition.addOrReplaceChild("rJaw2",
                CubeListBuilder.create().texOffs(0, 27).mirror()
                        .addBox(12.0F, 0.0F, -8.0F, 17, 1, 3),
                PartPose.offsetAndRotation(-5.0F, 13.0F, -33.0F, 0.0F, 1.745329F, 0.0F));

        partdefinition.addOrReplaceChild("lAntenna",
                CubeListBuilder.create().texOffs(70, 0).mirror()
                        .addBox(-0.5F, -12.0F, -0.5F, 1, 12, 1),
                PartPose.offsetAndRotation(0.0F, 4.0F, -32.0F, 0.0F, 0.0F, 0.5410521F));

        partdefinition.addOrReplaceChild("rAntenna",
                CubeListBuilder.create().texOffs(70, 0).mirror()
                        .addBox(-0.5F, -12.0F, -0.5F, 1, 12, 1),
                PartPose.offsetAndRotation(0.0F, 4.0F, -32.0F, 0.0F, 0.0F, -0.5410521F));

        return LayerDefinition.create(meshdefinition, 128, 256);
    }

    @Override
    public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        abdomen.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        head.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        jet1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        jet2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        hip1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        hip2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        hip3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        hip4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        hip5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        hip6.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        lJaw1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        rJaw1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        lJaw2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        rJaw2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        lAntenna.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        rAntenna.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
