package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.Peacock;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class ModelPeacock extends EntityModel<Peacock> {
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart legRight;
    private final ModelPart legLeft;
    private final ModelPart tailFan;
    private final ModelPart wingRight;
    private final ModelPart wingLeft;

    public ModelPeacock(ModelPart root) {
        this.head = root.getChild("head");
        this.body = root.getChild("body");
        this.legRight = root.getChild("leg_right");
        this.legLeft = root.getChild("leg_left");
        this.tailFan = root.getChild("tail_fan");
        this.wingRight = root.getChild("wing_right");
        this.wingLeft = root.getChild("wing_left");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -6.0F, -2.0F, 4, 6, 4), PartPose.offset(0.0F, 14.0F, -4.0F));
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 10).addBox(-3.0F, -3.0F, -4.0F, 6, 6, 8), PartPose.offset(0.0F, 17.0F, 0.0F));
        root.addOrReplaceChild("leg_right", CubeListBuilder.create().texOffs(28, 0).addBox(-0.5F, 0.0F, -0.5F, 1, 4, 1), PartPose.offset(-1.5F, 20.0F, 0.0F));
        root.addOrReplaceChild("leg_left", CubeListBuilder.create().texOffs(28, 0).mirror().addBox(-0.5F, 0.0F, -0.5F, 1, 4, 1), PartPose.offset(1.5F, 20.0F, 0.0F));
        root.addOrReplaceChild("tail_fan", CubeListBuilder.create().texOffs(0, 24).addBox(-6.0F, -8.0F, 0.0F, 12, 10, 1), PartPose.offsetAndRotation(0.0F, 17.0F, 4.0F, 0.7854F, 0.0F, 0.0F));
        root.addOrReplaceChild("wing_right", CubeListBuilder.create().texOffs(20, 10).addBox(-1.0F, 0.0F, -1.0F, 1, 4, 5), PartPose.offset(-3.0F, 15.0F, -1.0F));
        root.addOrReplaceChild("wing_left", CubeListBuilder.create().texOffs(20, 10).mirror().addBox(0.0F, 0.0F, -1.0F, 1, 4, 5), PartPose.offset(3.0F, 15.0F, -1.0F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(Peacock entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
        this.head.xRot = headPitch * ((float) Math.PI / 180F);
        this.legRight.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        this.legLeft.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;
        if (entity.getBlink() > 0) {
            this.tailFan.xRot = 0.1F;
        } else {
            this.tailFan.xRot = 0.7854F;
        }
    }

    @Override
    public void renderToBuffer(PoseStack ps, VertexConsumer vc, int light, int overlay, int color) {
        this.head.render(ps, vc, light, overlay, color);
        this.body.render(ps, vc, light, overlay, color);
        this.legRight.render(ps, vc, light, overlay, color);
        this.legLeft.render(ps, vc, light, overlay, color);
        this.tailFan.render(ps, vc, light, overlay, color);
        this.wingRight.render(ps, vc, light, overlay, color);
        this.wingLeft.render(ps, vc, light, overlay, color);
    }
}
