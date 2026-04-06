package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.EntityBee;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class BeeModel<T extends EntityBee> extends EntityModel<T> {
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart wingLeft;
    private final ModelPart wingRight;
    private final ModelPart leg1;
    private final ModelPart leg2;
    private final ModelPart leg3;
    private final ModelPart leg4;
    private final ModelPart leg5;
    private final ModelPart leg6;
    private final ModelPart stinger;

    public BeeModel(ModelPart root) {
        this.body = root.getChild("body");
        this.head = root.getChild("head");
        this.wingLeft = root.getChild("wing_left");
        this.wingRight = root.getChild("wing_right");
        this.leg1 = root.getChild("leg1");
        this.leg2 = root.getChild("leg2");
        this.leg3 = root.getChild("leg3");
        this.leg4 = root.getChild("leg4");
        this.leg5 = root.getChild("leg5");
        this.leg6 = root.getChild("leg6");
        this.stinger = root.getChild("stinger");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-4.0F, -4.0F, -6.0F, 8, 8, 12),
                PartPose.offset(0.0F, 14.0F, 0.0F));

        root.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 20)
                        .addBox(-3.0F, -3.0F, -4.0F, 6, 6, 4),
                PartPose.offset(0.0F, 13.0F, -6.0F));

        root.addOrReplaceChild("wing_left",
                CubeListBuilder.create().texOffs(28, 0)
                        .addBox(0.0F, 0.0F, -3.0F, 10, 1, 8),
                PartPose.offset(4.0F, 10.0F, 0.0F));

        root.addOrReplaceChild("wing_right",
                CubeListBuilder.create().texOffs(28, 0).mirror()
                        .addBox(-10.0F, 0.0F, -3.0F, 10, 1, 8),
                PartPose.offset(-4.0F, 10.0F, 0.0F));

        root.addOrReplaceChild("leg1",
                CubeListBuilder.create().texOffs(0, 30)
                        .addBox(-1.0F, 0.0F, -1.0F, 2, 4, 2),
                PartPose.offset(-3.0F, 18.0F, -4.0F));

        root.addOrReplaceChild("leg2",
                CubeListBuilder.create().texOffs(0, 30)
                        .addBox(-1.0F, 0.0F, -1.0F, 2, 4, 2),
                PartPose.offset(3.0F, 18.0F, -4.0F));

        root.addOrReplaceChild("leg3",
                CubeListBuilder.create().texOffs(0, 30)
                        .addBox(-1.0F, 0.0F, -1.0F, 2, 4, 2),
                PartPose.offset(-3.0F, 18.0F, 0.0F));

        root.addOrReplaceChild("leg4",
                CubeListBuilder.create().texOffs(0, 30)
                        .addBox(-1.0F, 0.0F, -1.0F, 2, 4, 2),
                PartPose.offset(3.0F, 18.0F, 0.0F));

        root.addOrReplaceChild("leg5",
                CubeListBuilder.create().texOffs(0, 30)
                        .addBox(-1.0F, 0.0F, -1.0F, 2, 4, 2),
                PartPose.offset(-3.0F, 18.0F, 4.0F));

        root.addOrReplaceChild("leg6",
                CubeListBuilder.create().texOffs(0, 30)
                        .addBox(-1.0F, 0.0F, -1.0F, 2, 4, 2),
                PartPose.offset(3.0F, 18.0F, 4.0F));

        root.addOrReplaceChild("stinger",
                CubeListBuilder.create().texOffs(20, 20)
                        .addBox(-1.0F, -1.0F, 0.0F, 2, 2, 4),
                PartPose.offset(0.0F, 14.0F, 6.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
        this.head.xRot = headPitch * ((float) Math.PI / 180F);

        float wingFlap = Mth.cos(ageInTicks * 2.0F) * 0.5F;
        this.wingLeft.zRot = -0.3F + wingFlap;
        this.wingRight.zRot = 0.3F - wingFlap;

        float legSwing = Mth.cos(limbSwing * 0.6662F) * 0.5F * limbSwingAmount;
        this.leg1.xRot = legSwing;
        this.leg2.xRot = -legSwing;
        this.leg3.xRot = -legSwing;
        this.leg4.xRot = legSwing;
        this.leg5.xRot = legSwing;
        this.leg6.xRot = -legSwing;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        body.render(poseStack, buffer, packedLight, packedOverlay, color);
        head.render(poseStack, buffer, packedLight, packedOverlay, color);
        wingLeft.render(poseStack, buffer, packedLight, packedOverlay, color);
        wingRight.render(poseStack, buffer, packedLight, packedOverlay, color);
        leg1.render(poseStack, buffer, packedLight, packedOverlay, color);
        leg2.render(poseStack, buffer, packedLight, packedOverlay, color);
        leg3.render(poseStack, buffer, packedLight, packedOverlay, color);
        leg4.render(poseStack, buffer, packedLight, packedOverlay, color);
        leg5.render(poseStack, buffer, packedLight, packedOverlay, color);
        leg6.render(poseStack, buffer, packedLight, packedOverlay, color);
        stinger.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
