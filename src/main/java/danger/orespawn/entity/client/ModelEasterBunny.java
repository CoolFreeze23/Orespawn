package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.EasterBunny;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class ModelEasterBunny extends EntityModel<EasterBunny> {
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart earLeft;
    private final ModelPart earRight;
    private final ModelPart legLeft;
    private final ModelPart legRight;
    private final ModelPart tail;

    public ModelEasterBunny(ModelPart root) {
        this.body = root.getChild("body");
        this.head = root.getChild("head");
        this.earLeft = root.getChild("ear_left");
        this.earRight = root.getChild("ear_right");
        this.legLeft = root.getChild("leg_left");
        this.legRight = root.getChild("leg_right");
        this.tail = root.getChild("tail");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -3.0F, -3.0F, 6, 5, 6), PartPose.offset(0.0F, 20.0F, 0.0F));
        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 11).addBox(-2.0F, -3.0F, -2.0F, 4, 4, 4), PartPose.offset(0.0F, 18.0F, -3.0F));
        root.addOrReplaceChild("ear_left", CubeListBuilder.create().texOffs(24, 0).addBox(-0.5F, -5.0F, 0.0F, 1, 4, 1), PartPose.offset(1.5F, 16.0F, -2.0F));
        root.addOrReplaceChild("ear_right", CubeListBuilder.create().texOffs(24, 0).addBox(-0.5F, -5.0F, 0.0F, 1, 4, 1), PartPose.offset(-1.5F, 16.0F, -2.0F));
        root.addOrReplaceChild("leg_left", CubeListBuilder.create().texOffs(16, 11).addBox(-1.0F, 0.0F, -1.0F, 2, 2, 2), PartPose.offset(1.5F, 22.0F, 1.0F));
        root.addOrReplaceChild("leg_right", CubeListBuilder.create().texOffs(16, 11).addBox(-1.0F, 0.0F, -1.0F, 2, 2, 2), PartPose.offset(-1.5F, 22.0F, 1.0F));
        root.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(24, 5).addBox(-1.0F, -1.0F, 0.0F, 2, 2, 2), PartPose.offset(0.0F, 20.0F, 3.0F));
        return LayerDefinition.create(mesh, 32, 32);
    }

    @Override
    public void setupAnim(EasterBunny entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
        this.head.xRot = headPitch * ((float) Math.PI / 180F);
        this.earLeft.xRot = this.head.xRot;
        this.earLeft.yRot = this.head.yRot;
        this.earRight.xRot = this.head.xRot;
        this.earRight.yRot = this.head.yRot;
        this.legLeft.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        this.legRight.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;
    }

    @Override
    public void renderToBuffer(PoseStack ps, VertexConsumer vc, int light, int overlay, int color) {
        this.body.render(ps, vc, light, overlay, color);
        this.head.render(ps, vc, light, overlay, color);
        this.earLeft.render(ps, vc, light, overlay, color);
        this.earRight.render(ps, vc, light, overlay, color);
        this.legLeft.render(ps, vc, light, overlay, color);
        this.legRight.render(ps, vc, light, overlay, color);
        this.tail.render(ps, vc, light, overlay, color);
    }
}
