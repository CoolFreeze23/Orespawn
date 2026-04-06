package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.Skate;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class ModelSkate extends EntityModel<Skate> {
    private final ModelPart body;
    private final ModelPart wingLeft;
    private final ModelPart wingRight;
    private final ModelPart tail;

    public ModelSkate(ModelPart root) {
        this.body = root.getChild("body");
        this.wingLeft = root.getChild("wing_left");
        this.wingRight = root.getChild("wing_right");
        this.tail = root.getChild("tail");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition part = mesh.getRoot();
        part.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -1.0F, -4.0F, 6, 2, 8), PartPose.offset(0.0F, 23.0F, 0.0F));
        part.addOrReplaceChild("wing_left", CubeListBuilder.create().texOffs(0, 10).addBox(0.0F, 0.0F, -3.0F, 6, 1, 6), PartPose.offset(3.0F, 23.0F, 0.0F));
        part.addOrReplaceChild("wing_right", CubeListBuilder.create().texOffs(0, 10).mirror().addBox(-6.0F, 0.0F, -3.0F, 6, 1, 6), PartPose.offset(-3.0F, 23.0F, 0.0F));
        part.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(20, 0).addBox(-0.5F, -0.5F, 0.0F, 1, 1, 6), PartPose.offset(0.0F, 23.0F, 4.0F));
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void setupAnim(Skate entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float flapAngle = Mth.cos(ageInTicks * 0.3F) * 0.2F;
        this.wingLeft.zRot = -flapAngle;
        this.wingRight.zRot = flapAngle;
        this.tail.yRot = Mth.cos(ageInTicks * 0.2F) * 0.15F;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        this.body.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.wingLeft.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.wingRight.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.tail.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
