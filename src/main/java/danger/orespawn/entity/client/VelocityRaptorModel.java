package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.VelocityRaptor;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class VelocityRaptorModel<T extends VelocityRaptor> extends EntityModel<T> {
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart tail;
    private final ModelPart legLeft;
    private final ModelPart legRight;
    private final ModelPart armLeft;
    private final ModelPart armRight;

    public VelocityRaptorModel(ModelPart root) {
        this.head = root.getChild("head");
        this.body = root.getChild("body");
        this.tail = root.getChild("tail");
        this.legLeft = root.getChild("leg_left");
        this.legRight = root.getChild("leg_right");
        this.armLeft = root.getChild("arm_left");
        this.armRight = root.getChild("arm_right");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-3.0F, -4.0F, -8.0F, 6, 5, 8), PartPose.offset(0.0F, 8.0F, -5.0F));
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 13)
                .addBox(-4.0F, -4.0F, -5.0F, 8, 8, 12), PartPose.offset(0.0F, 10.0F, 0.0F));
        root.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(0, 33)
                .addBox(-2.0F, -2.0F, 0.0F, 4, 3, 10), PartPose.offset(0.0F, 10.0F, 7.0F));
        root.addOrReplaceChild("leg_left", CubeListBuilder.create().texOffs(40, 13)
                .addBox(-1.5F, 0.0F, -2.0F, 3, 10, 3), PartPose.offset(3.0F, 14.0F, 2.0F));
        root.addOrReplaceChild("leg_right", CubeListBuilder.create().texOffs(40, 13)
                .addBox(-1.5F, 0.0F, -2.0F, 3, 10, 3), PartPose.offset(-3.0F, 14.0F, 2.0F));
        root.addOrReplaceChild("arm_left", CubeListBuilder.create().texOffs(40, 0)
                .addBox(0.0F, -1.0F, -1.0F, 1, 5, 2), PartPose.offset(4.0F, 10.0F, -4.0F));
        root.addOrReplaceChild("arm_right", CubeListBuilder.create().texOffs(46, 0)
                .addBox(-1.0F, -1.0F, -1.0F, 1, 5, 2), PartPose.offset(-4.0F, 10.0F, -4.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
        this.head.xRot = headPitch * ((float) Math.PI / 180F);

        float legSwing = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        this.legLeft.xRot = legSwing;
        this.legRight.xRot = -legSwing;

        this.armLeft.xRot = -0.3F + Mth.cos(ageInTicks * 0.1F) * 0.1F;
        this.armRight.xRot = -0.3F - Mth.cos(ageInTicks * 0.1F) * 0.1F;

        this.tail.yRot = Mth.cos(ageInTicks * 0.1F) * 0.15F;
    }

    @Override
    public void renderToBuffer(PoseStack ps, VertexConsumer vc, int light, int overlay, int color) {
        head.render(ps, vc, light, overlay, color);
        body.render(ps, vc, light, overlay, color);
        tail.render(ps, vc, light, overlay, color);
        legLeft.render(ps, vc, light, overlay, color);
        legRight.render(ps, vc, light, overlay, color);
        armLeft.render(ps, vc, light, overlay, color);
        armRight.render(ps, vc, light, overlay, color);
    }
}
