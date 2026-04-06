package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.AttackSquid;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class ModelAttackSquid extends EntityModel<AttackSquid> {
    private final ModelPart tent1;
    private final ModelPart tent2;
    private final ModelPart tent3;
    private final ModelPart tent4;
    private final ModelPart tent5;
    private final ModelPart tent6;
    private final ModelPart tent7;
    private final ModelPart body;
    private final ModelPart tent8;

    public ModelAttackSquid(ModelPart root) {
        this.tent1 = root.getChild("tent1");
        this.tent2 = root.getChild("tent2");
        this.tent3 = root.getChild("tent3");
        this.tent4 = root.getChild("tent4");
        this.tent5 = root.getChild("tent5");
        this.tent6 = root.getChild("tent6");
        this.tent7 = root.getChild("tent7");
        this.body = root.getChild("body");
        this.tent8 = root.getChild("tent8");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("tent1",
                CubeListBuilder.create().texOffs(0, 18).mirror().addBox(-1.0F, 0.0F, -1.0F, 2, 9, 2),
                PartPose.offsetAndRotation(5.0F, 15.0F, -1.0F, -0.9250245F, -1.745329F, 0.0F));

        partdefinition.addOrReplaceChild("tent2",
                CubeListBuilder.create().texOffs(0, 18).mirror().addBox(-8.0F, -1.0F, -1.0F, 8, 2, 2),
                PartPose.offsetAndRotation(-2.0F, 15.0F, -3.0F, -0.1745329F, -0.6632251F, -0.2443461F));

        partdefinition.addOrReplaceChild("tent3",
                CubeListBuilder.create().texOffs(0, 18).mirror().addBox(-1.0F, 0.0F, -1.0F, 2, 10, 2),
                PartPose.offsetAndRotation(1.0F, 15.0F, -4.0F, -1.134464F, 0.3316126F, 0.0F));

        partdefinition.addOrReplaceChild("tent4",
                CubeListBuilder.create().texOffs(0, 18).mirror().addBox(-1.0F, 0.0F, -1.0F, 2, 10, 2),
                PartPose.offsetAndRotation(-3.0F, 15.0F, -1.0F, 0.5585054F, -1.692969F, 0.0F));

        partdefinition.addOrReplaceChild("tent5",
                CubeListBuilder.create().texOffs(0, 18).mirror().addBox(-1.0F, 0.0F, -1.0F, 2, 10, 2),
                PartPose.offsetAndRotation(1.0F, 15.0F, 3.0F, 0.5410521F, 0.2268928F, 0.0F));

        partdefinition.addOrReplaceChild("tent6",
                CubeListBuilder.create().texOffs(0, 18).mirror().addBox(-1.0F, -1.0F, 0.0F, 2, 2, 8),
                PartPose.offsetAndRotation(-2.0F, 15.0F, 2.0F, -0.418879F, -0.6806784F, 0.0F));

        partdefinition.addOrReplaceChild("tent7",
                CubeListBuilder.create().texOffs(0, 18).mirror().addBox(0.0F, -1.0F, -1.0F, 8, 2, 2),
                PartPose.offsetAndRotation(3.0F, 15.0F, 1.0F, -0.1919862F, -0.6632251F, 0.418879F));

        partdefinition.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-4.0F, -10.0F, -4.0F, 8, 10, 8),
                PartPose.offsetAndRotation(1.0F, 16.0F, -1.0F, -0.1919862F, -0.6806784F, 0.0F));

        partdefinition.addOrReplaceChild("tent8",
                CubeListBuilder.create().texOffs(0, 18).mirror().addBox(-1.0F, -1.0F, -8.0F, 2, 2, 8),
                PartPose.offsetAndRotation(3.0F, 15.0F, -4.0F, 0.1919862F, -0.6806784F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 32);
    }

    @Override
    public void setupAnim(AttackSquid entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float newangleA = 0.0f;
        float newangleB = 0.0f;
        float newangle8 = 0.0f;
        float newangle1 = 0.0f;
        float newangle2 = 0.0f;
        float newangle3 = 0.0f;
        float newangle4 = 0.0f;
        float newangle5 = 0.0f;
        float newangle6 = 0.0f;
        float newangle7 = 0.0f;
        float pi4 = 0.7853982f;
        if ((double)limbSwingAmount > 0.1) {
        newangleA = Mth.cos((float)(ageInTicks * 0.25f * limbSwingAmount)) * (float)Math.PI * 0.04f * limbSwingAmount;
        newangleB = Mth.cos((float)(ageInTicks * 0.39f * limbSwingAmount)) * (float)Math.PI * 0.04f * limbSwingAmount;
        newangle1 = Mth.cos((float)(ageInTicks * 1.2f * limbSwingAmount)) * (float)Math.PI * 0.4f * limbSwingAmount;
        newangle2 = Mth.cos((float)(ageInTicks * 1.1f * limbSwingAmount)) * (float)Math.PI * 0.4f * limbSwingAmount;
        newangle3 = Mth.cos((float)(ageInTicks * 1.0f * limbSwingAmount)) * (float)Math.PI * 0.4f * limbSwingAmount;
        newangle4 = Mth.cos((float)(ageInTicks * 1.9f * limbSwingAmount)) * (float)Math.PI * 0.4f * limbSwingAmount;
        newangle5 = Mth.cos((float)(ageInTicks * 1.8f * limbSwingAmount)) * (float)Math.PI * 0.4f * limbSwingAmount;
        newangle6 = Mth.cos((float)(ageInTicks * 1.7f * limbSwingAmount)) * (float)Math.PI * 0.4f * limbSwingAmount;
        newangle7 = Mth.cos((float)(ageInTicks * 1.6f * limbSwingAmount)) * (float)Math.PI * 0.4f * limbSwingAmount;
        newangle8 = Mth.cos((float)(ageInTicks * 1.5f * limbSwingAmount)) * (float)Math.PI * 0.4f * limbSwingAmount;
        } else {
        newangleA = Mth.cos((float)(ageInTicks * 0.25f * limbSwingAmount)) * (float)Math.PI * 0.01f;
        newangleB = Mth.cos((float)(ageInTicks * 0.39f * limbSwingAmount)) * (float)Math.PI * 0.01f;
        newangle1 = Mth.cos((float)(ageInTicks * 1.2f * limbSwingAmount)) * (float)Math.PI * 0.1f;
        newangle2 = Mth.cos((float)(ageInTicks * 1.1f * limbSwingAmount)) * (float)Math.PI * 0.1f;
        newangle3 = Mth.cos((float)(ageInTicks * 1.0f * limbSwingAmount)) * (float)Math.PI * 0.1f;
        newangle4 = Mth.cos((float)(ageInTicks * 1.9f * limbSwingAmount)) * (float)Math.PI * 0.1f;
        newangle5 = Mth.cos((float)(ageInTicks * 1.8f * limbSwingAmount)) * (float)Math.PI * 0.1f;
        newangle6 = Mth.cos((float)(ageInTicks * 1.7f * limbSwingAmount)) * (float)Math.PI * 0.1f;
        newangle7 = Mth.cos((float)(ageInTicks * 1.6f * limbSwingAmount)) * (float)Math.PI * 0.1f;
        newangle8 = Mth.cos((float)(ageInTicks * 1.5f * limbSwingAmount)) * (float)Math.PI * 0.1f;
        }
        this.tent1.xRot = newangle1 - 1.03f;
        this.tent7.zRot = newangle2 + 0.37f;
        this.tent5.xRot = newangle3 + 0.6f;
        this.tent6.xRot = newangle4 - 0.48f;
        this.tent4.xRot = newangle5 + 0.63f;
        this.tent2.zRot = newangle6 - 0.26f;
        this.tent3.xRot = newangle7 - 1.03f;
        this.tent8.xRot = newangle8 + 0.43f;
        this.body.xRot = newangleA;
        this.body.zRot = newangleB;
        this.body.yRot = newangleA = (float)Math.toRadians(netHeadYaw) * 0.75f;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.tent1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tent2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tent3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tent4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tent5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tent6.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tent7.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tent8.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
