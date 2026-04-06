package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import danger.orespawn.entity.PurplePower;

public class ModelPurplePower extends EntityModel<PurplePower> {
    private final ModelPart Shape1;
    private final ModelPart Shape2;
    private final ModelPart Shape3;

    public ModelPurplePower(ModelPart root) {
        this.Shape1 = root.getChild("Shape1");
        this.Shape2 = root.getChild("Shape2");
        this.Shape3 = root.getChild("Shape3");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("Shape1",
                CubeListBuilder.create().texOffs(0, 12).mirror()
                        .addBox(-2.0F, -0.5F, -0.5F, 4, 1, 1),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("Shape2",
                CubeListBuilder.create().texOffs(0, 7).mirror()
                        .addBox(-4.0F, -0.5F, -0.5F, 8, 1, 1),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("Shape3",
                CubeListBuilder.create().texOffs(0, 0).mirror()
                        .addBox(-7.0F, -0.5F, -0.5F, 14, 1, 1),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 32);
    }

    @Override
    public void setupAnim(PurplePower entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float angle1 = (float) Math.toRadians(ageInTicks * 7.3F);
        float angle2 = (float) Math.toRadians(ageInTicks * 5.1F);
        float angle3 = (float) Math.toRadians(ageInTicks * 3.7F);

        this.Shape1.zRot = angle1;
        this.Shape1.xRot = angle2;
        this.Shape2.zRot = angle2;
        this.Shape2.yRot = angle3;
        this.Shape3.zRot = angle3;
        this.Shape3.xRot = angle1;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        for (int i = 0; i < 6; i++) {
            poseStack.pushPose();
            poseStack.mulPose(com.mojang.math.Axis.ZP.rotation(i * 1.0471976F));
            this.Shape1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
            poseStack.popPose();
        }
        for (int i = 0; i < 6; i++) {
            poseStack.pushPose();
            poseStack.mulPose(com.mojang.math.Axis.ZP.rotation(i * 1.0471976F));
            this.Shape2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
            poseStack.popPose();
        }
        for (int i = 0; i < 6; i++) {
            poseStack.pushPose();
            poseStack.mulPose(com.mojang.math.Axis.ZP.rotation(i * 1.0471976F));
            this.Shape3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
            poseStack.popPose();
        }
    }
}
