package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.Island;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class ModelIsland extends EntityModel<Island> {
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart leg1;
    private final ModelPart leg2;
    private final ModelPart leg3;
    private final ModelPart leg4;

    public ModelIsland(ModelPart root) {
        this.body = root.getChild("body");
        this.head = root.getChild("head");
        this.leg1 = root.getChild("leg1");
        this.leg2 = root.getChild("leg2");
        this.leg3 = root.getChild("leg3");
        this.leg4 = root.getChild("leg4");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition part = mesh.getRoot();
        part.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-12.0F, -8.0F, -12.0F, 24, 8, 24), PartPose.offset(0.0F, 12.0F, 0.0F));
        part.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 32).addBox(-4.0F, -4.0F, -6.0F, 8, 8, 6), PartPose.offset(0.0F, 10.0F, -12.0F));
        part.addOrReplaceChild("leg1", CubeListBuilder.create().texOffs(72, 0).addBox(-3.0F, 0.0F, -3.0F, 6, 12, 6), PartPose.offset(-8.0F, 12.0F, -8.0F));
        part.addOrReplaceChild("leg2", CubeListBuilder.create().texOffs(72, 0).addBox(-3.0F, 0.0F, -3.0F, 6, 12, 6), PartPose.offset(8.0F, 12.0F, -8.0F));
        part.addOrReplaceChild("leg3", CubeListBuilder.create().texOffs(72, 0).addBox(-3.0F, 0.0F, -3.0F, 6, 12, 6), PartPose.offset(-8.0F, 12.0F, 8.0F));
        part.addOrReplaceChild("leg4", CubeListBuilder.create().texOffs(72, 0).addBox(-3.0F, 0.0F, -3.0F, 6, 12, 6), PartPose.offset(8.0F, 12.0F, 8.0F));
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void setupAnim(Island entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float angle = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        this.leg1.xRot = angle;
        this.leg2.xRot = -angle;
        this.leg3.xRot = -angle;
        this.leg4.xRot = angle;
        this.head.xRot = headPitch * ((float) Math.PI / 180F);
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        this.body.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.head.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.leg1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.leg2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.leg3.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.leg4.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
