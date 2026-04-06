package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.EntityBrutalfly;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class BrutalflyModel<T extends EntityBrutalfly> extends EntityModel<T> {
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart wingLeft;
    private final ModelPart wingRight;
    private final ModelPart tail;
    private final ModelPart leg1;
    private final ModelPart leg2;
    private final ModelPart leg3;
    private final ModelPart leg4;
    private final ModelPart leg5;
    private final ModelPart leg6;

    public BrutalflyModel(ModelPart root) {
        this.body = root.getChild("body");
        this.head = root.getChild("head");
        this.wingLeft = root.getChild("wing_left");
        this.wingRight = root.getChild("wing_right");
        this.tail = root.getChild("tail");
        this.leg1 = root.getChild("leg1");
        this.leg2 = root.getChild("leg2");
        this.leg3 = root.getChild("leg3");
        this.leg4 = root.getChild("leg4");
        this.leg5 = root.getChild("leg5");
        this.leg6 = root.getChild("leg6");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-8.0F, -5.0F, -12.0F, 16, 10, 24), PartPose.offset(0.0F, 12.0F, 0.0F));
        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 34)
                .addBox(-5.0F, -5.0F, -8.0F, 10, 10, 8), PartPose.offset(0.0F, 10.0F, -12.0F));
        root.addOrReplaceChild("wing_left", CubeListBuilder.create().texOffs(0, 52)
                .addBox(0.0F, 0.0F, -6.0F, 20, 1, 16), PartPose.offset(8.0F, 7.0F, 0.0F));
        root.addOrReplaceChild("wing_right", CubeListBuilder.create().texOffs(0, 52).mirror()
                .addBox(-20.0F, 0.0F, -6.0F, 20, 1, 16), PartPose.offset(-8.0F, 7.0F, 0.0F));
        root.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(56, 0)
                .addBox(-3.0F, -3.0F, 0.0F, 6, 6, 12), PartPose.offset(0.0F, 12.0F, 12.0F));
        root.addOrReplaceChild("leg1", CubeListBuilder.create().texOffs(36, 34)
                .addBox(-1.0F, 0.0F, -1.0F, 2, 6, 2), PartPose.offset(-5.0F, 17.0F, -8.0F));
        root.addOrReplaceChild("leg2", CubeListBuilder.create().texOffs(36, 34)
                .addBox(-1.0F, 0.0F, -1.0F, 2, 6, 2), PartPose.offset(5.0F, 17.0F, -8.0F));
        root.addOrReplaceChild("leg3", CubeListBuilder.create().texOffs(36, 34)
                .addBox(-1.0F, 0.0F, -1.0F, 2, 6, 2), PartPose.offset(-5.0F, 17.0F, 0.0F));
        root.addOrReplaceChild("leg4", CubeListBuilder.create().texOffs(36, 34)
                .addBox(-1.0F, 0.0F, -1.0F, 2, 6, 2), PartPose.offset(5.0F, 17.0F, 0.0F));
        root.addOrReplaceChild("leg5", CubeListBuilder.create().texOffs(36, 34)
                .addBox(-1.0F, 0.0F, -1.0F, 2, 6, 2), PartPose.offset(-5.0F, 17.0F, 8.0F));
        root.addOrReplaceChild("leg6", CubeListBuilder.create().texOffs(36, 34)
                .addBox(-1.0F, 0.0F, -1.0F, 2, 6, 2), PartPose.offset(5.0F, 17.0F, 8.0F));

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
        this.head.xRot = headPitch * ((float) Math.PI / 180F);
        float wingFlap = Mth.cos(ageInTicks * 1.5F) * 0.4F;
        this.wingLeft.zRot = -0.2F + wingFlap;
        this.wingRight.zRot = 0.2F - wingFlap;
        float legSwing = Mth.cos(limbSwing * 0.6662F) * 0.5F * limbSwingAmount;
        this.leg1.xRot = legSwing;
        this.leg2.xRot = -legSwing;
        this.leg3.xRot = -legSwing;
        this.leg4.xRot = legSwing;
        this.leg5.xRot = legSwing;
        this.leg6.xRot = -legSwing;
    }

    @Override
    public void renderToBuffer(PoseStack ps, VertexConsumer vc, int light, int overlay, int color) {
        body.render(ps, vc, light, overlay, color);
        head.render(ps, vc, light, overlay, color);
        wingLeft.render(ps, vc, light, overlay, color);
        wingRight.render(ps, vc, light, overlay, color);
        tail.render(ps, vc, light, overlay, color);
        leg1.render(ps, vc, light, overlay, color);
        leg2.render(ps, vc, light, overlay, color);
        leg3.render(ps, vc, light, overlay, color);
        leg4.render(ps, vc, light, overlay, color);
        leg5.render(ps, vc, light, overlay, color);
        leg6.render(ps, vc, light, overlay, color);
    }
}
