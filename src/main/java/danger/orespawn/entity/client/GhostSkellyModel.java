package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.GhostSkelly;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class GhostSkellyModel<T extends GhostSkelly> extends EntityModel<T> {
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart armLeft;
    private final ModelPart armRight;

    public GhostSkellyModel(ModelPart root) {
        this.body = root.getChild("body");
        this.head = root.getChild("head");
        this.armLeft = root.getChild("arm_left");
        this.armRight = root.getChild("arm_right");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8), PartPose.offset(0.0F, 8.0F, 0.0F));
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 16)
                .addBox(-4.0F, 0.0F, -2.0F, 8, 16, 4), PartPose.offset(0.0F, 8.0F, 0.0F));
        root.addOrReplaceChild("arm_left", CubeListBuilder.create().texOffs(24, 16)
                .addBox(0.0F, -1.0F, -1.0F, 2, 12, 2), PartPose.offset(4.0F, 9.0F, 0.0F));
        root.addOrReplaceChild("arm_right", CubeListBuilder.create().texOffs(32, 16)
                .addBox(-2.0F, -1.0F, -1.0F, 2, 12, 2), PartPose.offset(-4.0F, 9.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
        float bob = Mth.sin(ageInTicks * 0.08F) * 0.5F;
        this.body.y = 8.0F + bob;
        this.head.y = 8.0F + bob;
        this.armLeft.y = 9.0F + bob;
        this.armRight.y = 9.0F + bob;

        float armSwing = Mth.sin(ageInTicks * 0.15F) * 0.3F;
        this.armLeft.xRot = -1.2F + armSwing;
        this.armRight.xRot = -1.2F - armSwing;
    }

    @Override
    public void renderToBuffer(PoseStack ps, VertexConsumer vc, int light, int overlay, int color) {
        head.render(ps, vc, light, overlay, color);
        body.render(ps, vc, light, overlay, color);
        armLeft.render(ps, vc, light, overlay, color);
        armRight.render(ps, vc, light, overlay, color);
    }
}
