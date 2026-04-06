package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.EntityRotator;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class RotatorModel<T extends EntityRotator> extends EntityModel<T> {
    private final ModelPart shape1;
    private final ModelPart shape2;
    private final ModelPart shape3;

    public RotatorModel(ModelPart root) {
        this.shape1 = root.getChild("shape1");
        this.shape2 = root.getChild("shape2");
        this.shape3 = root.getChild("shape3");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("shape1", CubeListBuilder.create().texOffs(0, 12).mirror().addBox(-2.0F, 3.9F, 0.0F, 4, 1, 1), PartPose.offset(0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("shape2", CubeListBuilder.create().texOffs(0, 7).mirror().addBox(-4.0F, 7.6F, 0.0F, 8, 2, 2), PartPose.offset(0.0F, 0.0F, -0.5F));
        root.addOrReplaceChild("shape3", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-7.0F, 13.7F, 0.0F, 14, 3, 3), PartPose.offset(0.0F, 0.0F, -1.0F));
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float spin = (ageInTicks * 2.0f) % 360.0f * ((float) Math.PI / 180.0f);
        this.shape1.zRot = spin;
        this.shape2.zRot = spin * 1.5f;
        this.shape3.zRot = spin * 2.0f;
    }

    @Override
    public void renderToBuffer(PoseStack ps, VertexConsumer vc, int light, int overlay, int color) {
        shape1.render(ps, vc, light, overlay, color);
        shape2.render(ps, vc, light, overlay, color);
        shape3.render(ps, vc, light, overlay, color);
    }
}
