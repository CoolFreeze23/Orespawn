package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.Entity;

public class ModelElevator extends EntityModel<Entity> {
    private final ModelPart shape1;
    private final ModelPart shape2;
    private final ModelPart shape3;
    private final ModelPart shape4;
    private final ModelPart shape5;

    public ModelElevator(ModelPart root) {
        this.shape1 = root.getChild("shape1");
        this.shape2 = root.getChild("shape2");
        this.shape3 = root.getChild("shape3");
        this.shape4 = root.getChild("shape4");
        this.shape5 = root.getChild("shape5");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("shape1",
                CubeListBuilder.create().texOffs(0, 0).mirror()
                        .addBox(-4.0F, 0.0F, -8.0F, 8, 1, 16),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("shape2",
                CubeListBuilder.create().texOffs(0, 18).mirror()
                        .addBox(-3.0F, 0.0F, -9.0F, 6, 1, 1),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("shape3",
                CubeListBuilder.create().texOffs(0, 21).mirror()
                        .addBox(-1.0F, 0.0F, -10.0F, 2, 1, 1),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("shape4",
                CubeListBuilder.create().texOffs(17, 18).mirror()
                        .addBox(-3.0F, 0.0F, 8.0F, 6, 1, 1),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("shape5",
                CubeListBuilder.create().texOffs(17, 21).mirror()
                        .addBox(-1.0F, 0.0F, 9.0F, 2, 1, 1),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        shape1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        shape2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        shape3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        shape4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        shape5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
