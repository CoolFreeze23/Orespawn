package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.Lizard;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class LizardModel<T extends Lizard> extends EntityModel<T> {
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart tail;
    private final ModelPart legFrontLeft;
    private final ModelPart legFrontRight;
    private final ModelPart legBackLeft;
    private final ModelPart legBackRight;

    public LizardModel(ModelPart root) {
        this.head = root.getChild("head");
        this.body = root.getChild("body");
        this.tail = root.getChild("tail");
        this.legFrontLeft = root.getChild("leg_front_left");
        this.legFrontRight = root.getChild("leg_front_right");
        this.legBackLeft = root.getChild("leg_back_left");
        this.legBackRight = root.getChild("leg_back_right");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-3.0F, -2.0F, -6.0F, 6, 4, 6), PartPose.offset(0.0F, 17.0F, -5.0F));
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 10)
                .addBox(-4.0F, -3.0F, -5.0F, 8, 5, 10), PartPose.offset(0.0F, 17.0F, 0.0F));
        root.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(0, 25)
                .addBox(-1.5F, -1.0F, 0.0F, 3, 2, 8), PartPose.offset(0.0F, 17.0F, 5.0F));
        root.addOrReplaceChild("leg_front_left", CubeListBuilder.create().texOffs(36, 10)
                .addBox(-1.0F, 0.0F, -1.0F, 2, 7, 2), PartPose.offset(4.0F, 17.0F, -3.0F));
        root.addOrReplaceChild("leg_front_right", CubeListBuilder.create().texOffs(36, 10)
                .addBox(-1.0F, 0.0F, -1.0F, 2, 7, 2), PartPose.offset(-4.0F, 17.0F, -3.0F));
        root.addOrReplaceChild("leg_back_left", CubeListBuilder.create().texOffs(36, 10)
                .addBox(-1.0F, 0.0F, -1.0F, 2, 7, 2), PartPose.offset(4.0F, 17.0F, 4.0F));
        root.addOrReplaceChild("leg_back_right", CubeListBuilder.create().texOffs(36, 10)
                .addBox(-1.0F, 0.0F, -1.0F, 2, 7, 2), PartPose.offset(-4.0F, 17.0F, 4.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
        this.head.xRot = headPitch * ((float) Math.PI / 180F);

        float legSwing = Mth.cos(limbSwing * 0.6662F) * 1.2F * limbSwingAmount;
        this.legFrontLeft.xRot = legSwing;
        this.legFrontRight.xRot = -legSwing;
        this.legBackLeft.xRot = -legSwing;
        this.legBackRight.xRot = legSwing;

        this.tail.yRot = Mth.cos(ageInTicks * 0.15F) * 0.3F;
    }

    @Override
    public void renderToBuffer(PoseStack ps, VertexConsumer vc, int light, int overlay, int color) {
        head.render(ps, vc, light, overlay, color);
        body.render(ps, vc, light, overlay, color);
        tail.render(ps, vc, light, overlay, color);
        legFrontLeft.render(ps, vc, light, overlay, color);
        legFrontRight.render(ps, vc, light, overlay, color);
        legBackLeft.render(ps, vc, light, overlay, color);
        legBackRight.render(ps, vc, light, overlay, color);
    }
}
