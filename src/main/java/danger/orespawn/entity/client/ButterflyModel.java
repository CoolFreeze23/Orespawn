package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.EntityButterfly;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class ButterflyModel<T extends EntityButterfly> extends EntityModel<T> {
    private final ModelPart body;
    private final ModelPart wingLeft;
    private final ModelPart wingRight;

    public ButterflyModel(ModelPart root) {
        this.body = root.getChild("body");
        this.wingLeft = root.getChild("wing_left");
        this.wingRight = root.getChild("wing_right");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-0.5F, -0.5F, -3.0F, 1, 1, 6), PartPose.offset(0.0F, 22.0F, 0.0F));
        root.addOrReplaceChild("wing_left", CubeListBuilder.create().texOffs(0, 8)
                .addBox(0.0F, 0.0F, -2.0F, 5, 0, 4), PartPose.offset(0.5F, 21.5F, 0.0F));
        root.addOrReplaceChild("wing_right", CubeListBuilder.create().texOffs(0, 12)
                .addBox(-5.0F, 0.0F, -2.0F, 5, 0, 4), PartPose.offset(-0.5F, 21.5F, 0.0F));

        return LayerDefinition.create(mesh, 32, 32);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float wingFlap = Mth.cos(ageInTicks * 1.5F) * 0.8F;
        this.wingLeft.zRot = -wingFlap - 0.2F;
        this.wingRight.zRot = wingFlap + 0.2F;
    }

    @Override
    public void renderToBuffer(PoseStack ps, VertexConsumer vc, int light, int overlay, int color) {
        body.render(ps, vc, light, overlay, color);
        wingLeft.render(ps, vc, light, overlay, color);
        wingRight.render(ps, vc, light, overlay, color);
    }
}
