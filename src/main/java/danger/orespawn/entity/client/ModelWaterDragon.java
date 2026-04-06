package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.WaterDragon;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class ModelWaterDragon extends EntityModel<WaterDragon> {
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart neck;
    private final ModelPart tail;
    private final ModelPart wingLeft;
    private final ModelPart wingRight;
    private final ModelPart leg1;
    private final ModelPart leg2;
    private final ModelPart leg3;
    private final ModelPart leg4;

    public ModelWaterDragon(ModelPart root) {
        this.body = root.getChild("body");
        this.head = root.getChild("head");
        this.neck = root.getChild("neck");
        this.tail = root.getChild("tail");
        this.wingLeft = root.getChild("wing_left");
        this.wingRight = root.getChild("wing_right");
        this.leg1 = root.getChild("leg1");
        this.leg2 = root.getChild("leg2");
        this.leg3 = root.getChild("leg3");
        this.leg4 = root.getChild("leg4");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition part = mesh.getRoot();
        part.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -6.0F, -10.0F, 16, 12, 20), PartPose.offset(0.0F, 10.0F, 0.0F));
        part.addOrReplaceChild("neck", CubeListBuilder.create().texOffs(0, 32).addBox(-3.0F, -3.0F, -8.0F, 6, 6, 8), PartPose.offset(0.0F, 8.0F, -10.0F));
        part.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 46).addBox(-4.0F, -4.0F, -8.0F, 8, 8, 8), PartPose.offset(0.0F, 7.0F, -18.0F));
        part.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(52, 0).addBox(-3.0F, -3.0F, 0.0F, 6, 6, 16), PartPose.offset(0.0F, 10.0F, 10.0F));
        part.addOrReplaceChild("wing_left", CubeListBuilder.create().texOffs(0, 62).addBox(0.0F, -1.0F, -4.0F, 12, 1, 8), PartPose.offset(8.0F, 6.0F, 0.0F));
        part.addOrReplaceChild("wing_right", CubeListBuilder.create().texOffs(0, 62).mirror().addBox(-12.0F, -1.0F, -4.0F, 12, 1, 8), PartPose.offset(-8.0F, 6.0F, 0.0F));
        part.addOrReplaceChild("leg1", CubeListBuilder.create().texOffs(72, 0).addBox(-2.0F, 0.0F, -2.0F, 4, 8, 4), PartPose.offset(-6.0F, 16.0F, -6.0F));
        part.addOrReplaceChild("leg2", CubeListBuilder.create().texOffs(72, 0).addBox(-2.0F, 0.0F, -2.0F, 4, 8, 4), PartPose.offset(6.0F, 16.0F, -6.0F));
        part.addOrReplaceChild("leg3", CubeListBuilder.create().texOffs(72, 0).addBox(-2.0F, 0.0F, -2.0F, 4, 8, 4), PartPose.offset(-6.0F, 16.0F, 6.0F));
        part.addOrReplaceChild("leg4", CubeListBuilder.create().texOffs(72, 0).addBox(-2.0F, 0.0F, -2.0F, 4, 8, 4), PartPose.offset(6.0F, 16.0F, 6.0F));
        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(WaterDragon entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float legSwing = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        this.leg1.xRot = legSwing;
        this.leg2.xRot = -legSwing;
        this.leg3.xRot = -legSwing;
        this.leg4.xRot = legSwing;
        this.tail.yRot = Mth.cos(ageInTicks * 0.1F) * 0.2F;
        this.wingLeft.zRot = -Mth.cos(ageInTicks * 0.15F) * 0.2F;
        this.wingRight.zRot = Mth.cos(ageInTicks * 0.15F) * 0.2F;
        this.head.xRot = headPitch * ((float) Math.PI / 180F);
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        this.body.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.neck.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.head.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.tail.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.wingLeft.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.wingRight.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.leg1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.leg2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.leg3.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.leg4.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
