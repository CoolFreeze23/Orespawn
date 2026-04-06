package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.Firefly;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class FireflyModel<T extends Firefly> extends EntityModel<T> {
    private final ModelPart body;
    private final ModelPart wingLeft;
    private final ModelPart wingRight;
    private final ModelPart glow;

    public FireflyModel(ModelPart root) {
        this.body = root.getChild("body");
        this.wingLeft = root.getChild("wing_left");
        this.wingRight = root.getChild("wing_right");
        this.glow = root.getChild("glow");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-1.0F, -1.0F, -2.0F, 2, 2, 4), PartPose.offset(0.0F, 22.0F, 0.0F));
        root.addOrReplaceChild("wing_left", CubeListBuilder.create().texOffs(0, 6)
                .addBox(0.0F, 0.0F, -1.5F, 3, 0, 3), PartPose.offset(1.0F, 21.0F, 0.0F));
        root.addOrReplaceChild("wing_right", CubeListBuilder.create().texOffs(0, 9)
                .addBox(-3.0F, 0.0F, -1.5F, 3, 0, 3), PartPose.offset(-1.0F, 21.0F, 0.0F));
        root.addOrReplaceChild("glow", CubeListBuilder.create().texOffs(12, 0)
                .addBox(-1.0F, -1.0F, 0.0F, 2, 2, 2), PartPose.offset(0.0F, 22.0F, 2.0F));

        return LayerDefinition.create(mesh, 32, 16);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float wingFlap = Mth.cos(ageInTicks * 2.5F) * 0.7F;
        this.wingLeft.zRot = -wingFlap - 0.2F;
        this.wingRight.zRot = wingFlap + 0.2F;
    }

    @Override
    public void renderToBuffer(PoseStack ps, VertexConsumer vc, int light, int overlay, int color) {
        body.render(ps, vc, light, overlay, color);
        wingLeft.render(ps, vc, light, overlay, color);
        wingRight.render(ps, vc, light, overlay, color);
        glow.render(ps, vc, light, overlay, color);
    }
}
