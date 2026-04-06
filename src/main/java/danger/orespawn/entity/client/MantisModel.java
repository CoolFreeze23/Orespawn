package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.EntityMantis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class MantisModel<T extends EntityMantis> extends EntityModel<T> {
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart leg1;
    private final ModelPart leg2;
    private final ModelPart leg3;
    private final ModelPart leg4;
    private final ModelPart armLeft;
    private final ModelPart armRight;
    private final ModelPart wingLeft;
    private final ModelPart wingRight;

    public MantisModel(ModelPart root) {
        this.body = root.getChild("body");
        this.head = root.getChild("head");
        this.leg1 = root.getChild("leg1");
        this.leg2 = root.getChild("leg2");
        this.leg3 = root.getChild("leg3");
        this.leg4 = root.getChild("leg4");
        this.armLeft = root.getChild("arm_left");
        this.armRight = root.getChild("arm_right");
        this.wingLeft = root.getChild("wing_left");
        this.wingRight = root.getChild("wing_right");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-5.0F, -4.0F, -12.0F, 10, 8, 24), PartPose.offset(0.0F, -4.0F, 0.0F));
        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 32)
                .addBox(-5.0F, -5.0F, -8.0F, 10, 10, 8), PartPose.offset(0.0F, -10.0F, -12.0F));

        root.addOrReplaceChild("leg1", CubeListBuilder.create().texOffs(0, 50)
                .addBox(-1.5F, 0.0F, -1.5F, 3, 20, 3), PartPose.offset(-5.0F, 2.0F, -6.0F));
        root.addOrReplaceChild("leg2", CubeListBuilder.create().texOffs(0, 50)
                .addBox(-1.5F, 0.0F, -1.5F, 3, 20, 3), PartPose.offset(5.0F, 2.0F, -6.0F));
        root.addOrReplaceChild("leg3", CubeListBuilder.create().texOffs(0, 50)
                .addBox(-1.5F, 0.0F, -1.5F, 3, 20, 3), PartPose.offset(-5.0F, 2.0F, 6.0F));
        root.addOrReplaceChild("leg4", CubeListBuilder.create().texOffs(0, 50)
                .addBox(-1.5F, 0.0F, -1.5F, 3, 20, 3), PartPose.offset(5.0F, 2.0F, 6.0F));

        root.addOrReplaceChild("arm_left", CubeListBuilder.create().texOffs(36, 32)
                .addBox(-2.0F, 0.0F, -2.0F, 4, 22, 4), PartPose.offset(-5.0F, -6.0F, -12.0F));
        root.addOrReplaceChild("arm_right", CubeListBuilder.create().texOffs(36, 32)
                .addBox(-2.0F, 0.0F, -2.0F, 4, 22, 4), PartPose.offset(5.0F, -6.0F, -12.0F));

        root.addOrReplaceChild("wing_left", CubeListBuilder.create().texOffs(44, 0)
                .addBox(0.0F, 0.0F, -4.0F, 16, 1, 18), PartPose.offset(5.0F, -8.0F, -6.0F));
        root.addOrReplaceChild("wing_right", CubeListBuilder.create().texOffs(44, 0).mirror()
                .addBox(-16.0F, 0.0F, -4.0F, 16, 1, 18), PartPose.offset(-5.0F, -8.0F, -6.0F));

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
        this.head.xRot = headPitch * ((float) Math.PI / 180F);

        float legSwing = Mth.cos(limbSwing * 0.6662F) * 0.5F * limbSwingAmount;
        this.leg1.xRot = legSwing;
        this.leg2.xRot = -legSwing;
        this.leg3.xRot = -legSwing;
        this.leg4.xRot = legSwing;

        float armSwing = Mth.cos(ageInTicks * 0.08F) * 0.2F;
        this.armLeft.xRot = -0.6F + armSwing;
        this.armRight.xRot = -0.6F - armSwing;
    }

    @Override
    public void renderToBuffer(PoseStack ps, VertexConsumer vc, int light, int overlay, int color) {
        body.render(ps, vc, light, overlay, color);
        head.render(ps, vc, light, overlay, color);
        leg1.render(ps, vc, light, overlay, color);
        leg2.render(ps, vc, light, overlay, color);
        leg3.render(ps, vc, light, overlay, color);
        leg4.render(ps, vc, light, overlay, color);
        armLeft.render(ps, vc, light, overlay, color);
        armRight.render(ps, vc, light, overlay, color);
        wingLeft.render(ps, vc, light, overlay, color);
        wingRight.render(ps, vc, light, overlay, color);
    }
}
