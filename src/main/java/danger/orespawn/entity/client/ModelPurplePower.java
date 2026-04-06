package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import danger.orespawn.entity.PurplePower;

public class ModelPurplePower extends EntityModel<PurplePower> {
    private final ModelPart innerSpoke;
    private final ModelPart middleSpoke;
    private final ModelPart outerSpoke;

    public ModelPurplePower(ModelPart root) {
        this.innerSpoke = root.getChild("Shape1");
        this.middleSpoke = root.getChild("Shape2");
        this.outerSpoke = root.getChild("Shape3");
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

        this.innerSpoke.zRot = angle1;
        this.innerSpoke.xRot = angle2;
        this.middleSpoke.zRot = angle2;
        this.middleSpoke.yRot = angle3;
        this.outerSpoke.zRot = angle3;
        this.outerSpoke.xRot = angle1;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        for (int i = 0; i < 6; i++) {
            poseStack.pushPose();
            poseStack.mulPose(com.mojang.math.Axis.ZP.rotation(i * 1.0471976F));
            this.innerSpoke.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
            poseStack.popPose();
        }
        for (int i = 0; i < 6; i++) {
            poseStack.pushPose();
            poseStack.mulPose(com.mojang.math.Axis.ZP.rotation(i * 1.0471976F));
            this.middleSpoke.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
            poseStack.popPose();
        }
        for (int i = 0; i < 6; i++) {
            poseStack.pushPose();
            poseStack.mulPose(com.mojang.math.Axis.ZP.rotation(i * 1.0471976F));
            this.outerSpoke.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
            poseStack.popPose();
        }
    }
}
