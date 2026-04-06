package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.Hammerhead;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class ModelHammerhead extends EntityModel<Hammerhead> {
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart tail;
    private final ModelPart leg1;
    private final ModelPart leg2;
    private final ModelPart leg3;
    private final ModelPart leg4;

    public ModelHammerhead(ModelPart root) {
        this.body = root.getChild("body");
        this.head = root.getChild("head");
        this.tail = root.getChild("tail");
        this.leg1 = root.getChild("leg1");
        this.leg2 = root.getChild("leg2");
        this.leg3 = root.getChild("leg3");
        this.leg4 = root.getChild("leg4");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition part = mesh.getRoot();
        part.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, -8.0F, -8.0F, 12, 12, 16), PartPose.offset(0.0F, 12.0F, 0.0F));
        part.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 28).addBox(-8.0F, -4.0F, -4.0F, 16, 6, 6), PartPose.offset(0.0F, 8.0F, -8.0F));
        part.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(40, 0).addBox(-2.0F, -3.0F, 0.0F, 4, 6, 8), PartPose.offset(0.0F, 10.0F, 8.0F));
        part.addOrReplaceChild("leg1", CubeListBuilder.create().texOffs(56, 0).addBox(-2.0F, 0.0F, -2.0F, 4, 8, 4), PartPose.offset(-4.0F, 16.0F, -5.0F));
        part.addOrReplaceChild("leg2", CubeListBuilder.create().texOffs(56, 0).addBox(-2.0F, 0.0F, -2.0F, 4, 8, 4), PartPose.offset(4.0F, 16.0F, -5.0F));
        part.addOrReplaceChild("leg3", CubeListBuilder.create().texOffs(56, 0).addBox(-2.0F, 0.0F, -2.0F, 4, 8, 4), PartPose.offset(-4.0F, 16.0F, 5.0F));
        part.addOrReplaceChild("leg4", CubeListBuilder.create().texOffs(56, 0).addBox(-2.0F, 0.0F, -2.0F, 4, 8, 4), PartPose.offset(4.0F, 16.0F, 5.0F));
        return LayerDefinition.create(mesh, 128, 64);
    }

    @Override
    public void setupAnim(Hammerhead entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float angle = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        this.leg1.xRot = angle;
        this.leg2.xRot = -angle;
        this.leg3.xRot = -angle;
        this.leg4.xRot = angle;
        this.tail.yRot = Mth.cos(ageInTicks * 0.15F) * 0.2F;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        this.body.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.head.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.tail.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.leg1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.leg2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.leg3.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.leg4.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
