package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.EntityCannonFodder;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class CannonFodderModel<T extends EntityCannonFodder> extends EntityModel<T> {
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart legFrontLeft;
    private final ModelPart legFrontRight;
    private final ModelPart legBackLeft;
    private final ModelPart legBackRight;

    public CannonFodderModel(ModelPart root) {
        this.head = root.getChild("head");
        this.body = root.getChild("body");
        this.legFrontLeft = root.getChild("leg_front_left");
        this.legFrontRight = root.getChild("leg_front_right");
        this.legBackLeft = root.getChild("leg_back_left");
        this.legBackRight = root.getChild("leg_back_right");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-3.0F, -4.0F, -6.0F, 6, 6, 6), PartPose.offset(0.0F, 12.0F, -4.0F));
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 12)
                .addBox(-4.0F, -3.0F, -4.0F, 8, 6, 12), PartPose.offset(0.0F, 12.0F, 0.0F));
        root.addOrReplaceChild("leg_front_left", CubeListBuilder.create().texOffs(0, 30)
                .addBox(-1.0F, 0.0F, -1.0F, 2, 9, 2), PartPose.offset(3.0F, 15.0F, -3.0F));
        root.addOrReplaceChild("leg_front_right", CubeListBuilder.create().texOffs(8, 30)
                .addBox(-1.0F, 0.0F, -1.0F, 2, 9, 2), PartPose.offset(-3.0F, 15.0F, -3.0F));
        root.addOrReplaceChild("leg_back_left", CubeListBuilder.create().texOffs(16, 30)
                .addBox(-1.0F, 0.0F, -1.0F, 2, 9, 2), PartPose.offset(3.0F, 15.0F, 7.0F));
        root.addOrReplaceChild("leg_back_right", CubeListBuilder.create().texOffs(24, 30)
                .addBox(-1.0F, 0.0F, -1.0F, 2, 9, 2), PartPose.offset(-3.0F, 15.0F, 7.0F));

        return LayerDefinition.create(mesh, 128, 64);
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
    }

    @Override
    public void renderToBuffer(PoseStack ps, VertexConsumer vc, int light, int overlay, int color) {
        head.render(ps, vc, light, overlay, color);
        body.render(ps, vc, light, overlay, color);
        legFrontLeft.render(ps, vc, light, overlay, color);
        legFrontRight.render(ps, vc, light, overlay, color);
        legBackLeft.render(ps, vc, light, overlay, color);
        legBackRight.render(ps, vc, light, overlay, color);
    }
}
