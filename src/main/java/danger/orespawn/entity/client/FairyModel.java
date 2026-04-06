package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.Fairy;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class FairyModel<T extends Fairy> extends EntityModel<T> {
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart wingLeft;
    private final ModelPart wingRight;
    private final ModelPart legLeft;
    private final ModelPart legRight;

    public FairyModel(ModelPart root) {
        this.head = root.getChild("head");
        this.body = root.getChild("body");
        this.wingLeft = root.getChild("wing_left");
        this.wingRight = root.getChild("wing_right");
        this.legLeft = root.getChild("leg_left");
        this.legRight = root.getChild("leg_right");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-2.0F, -4.0F, -2.0F, 4, 4, 4), PartPose.offset(0.0F, 18.0F, 0.0F));
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 8)
                .addBox(-1.5F, 0.0F, -1.0F, 3, 4, 2), PartPose.offset(0.0F, 18.0F, 0.0F));
        root.addOrReplaceChild("wing_left", CubeListBuilder.create().texOffs(16, 0)
                .addBox(0.0F, -3.0F, 0.0F, 4, 6, 0), PartPose.offset(1.5F, 19.0F, 1.0F));
        root.addOrReplaceChild("wing_right", CubeListBuilder.create().texOffs(24, 0)
                .addBox(-4.0F, -3.0F, 0.0F, 4, 6, 0), PartPose.offset(-1.5F, 19.0F, 1.0F));
        root.addOrReplaceChild("leg_left", CubeListBuilder.create().texOffs(0, 14)
                .addBox(-0.5F, 0.0F, -0.5F, 1, 2, 1), PartPose.offset(1.0F, 22.0F, 0.0F));
        root.addOrReplaceChild("leg_right", CubeListBuilder.create().texOffs(4, 14)
                .addBox(-0.5F, 0.0F, -0.5F, 1, 2, 1), PartPose.offset(-1.0F, 22.0F, 0.0F));

        return LayerDefinition.create(mesh, 32, 32);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
        this.head.xRot = headPitch * ((float) Math.PI / 180F);

        float wingFlap = Mth.cos(ageInTicks * 2.0F) * 0.6F;
        this.wingLeft.yRot = wingFlap + 0.3F;
        this.wingRight.yRot = -wingFlap - 0.3F;

        float legSwing = Mth.cos(limbSwing * 0.6662F) * limbSwingAmount;
        this.legLeft.xRot = legSwing;
        this.legRight.xRot = -legSwing;
    }

    @Override
    public void renderToBuffer(PoseStack ps, VertexConsumer vc, int light, int overlay, int color) {
        head.render(ps, vc, light, overlay, color);
        body.render(ps, vc, light, overlay, color);
        wingLeft.render(ps, vc, light, overlay, color);
        wingRight.render(ps, vc, light, overlay, color);
        legLeft.render(ps, vc, light, overlay, color);
        legRight.render(ps, vc, light, overlay, color);
    }
}
