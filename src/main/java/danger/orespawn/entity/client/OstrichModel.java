package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.Ostrich;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class OstrichModel<T extends Ostrich> extends EntityModel<T> {
    private final ModelPart head;
    private final ModelPart neck;
    private final ModelPart body;
    private final ModelPart legLeft;
    private final ModelPart legRight;

    public OstrichModel(ModelPart root) {
        this.head = root.getChild("head");
        this.neck = root.getChild("neck");
        this.body = root.getChild("body");
        this.legLeft = root.getChild("leg_left");
        this.legRight = root.getChild("leg_right");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-2.0F, -4.0F, -2.0F, 4, 4, 4), PartPose.offset(0.0F, 4.0F, -2.0F));
        root.addOrReplaceChild("neck", CubeListBuilder.create().texOffs(16, 0)
                .addBox(-1.0F, -8.0F, -1.0F, 2, 10, 2), PartPose.offset(0.0F, 12.0F, -3.0F));
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 10)
                .addBox(-4.0F, -4.0F, -4.0F, 8, 8, 10), PartPose.offset(0.0F, 12.0F, 0.0F));
        root.addOrReplaceChild("leg_left", CubeListBuilder.create().texOffs(36, 10)
                .addBox(-1.0F, 0.0F, -1.0F, 2, 12, 2), PartPose.offset(3.0F, 12.0F, 1.0F));
        root.addOrReplaceChild("leg_right", CubeListBuilder.create().texOffs(44, 10)
                .addBox(-1.0F, 0.0F, -1.0F, 2, 12, 2), PartPose.offset(-3.0F, 12.0F, 1.0F));

        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
        this.head.xRot = headPitch * ((float) Math.PI / 180F);

        float legSwing = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        this.legLeft.xRot = legSwing;
        this.legRight.xRot = -legSwing;
    }

    @Override
    public void renderToBuffer(PoseStack ps, VertexConsumer vc, int light, int overlay, int color) {
        head.render(ps, vc, light, overlay, color);
        neck.render(ps, vc, light, overlay, color);
        body.render(ps, vc, light, overlay, color);
        legLeft.render(ps, vc, light, overlay, color);
        legRight.render(ps, vc, light, overlay, color);
    }
}
