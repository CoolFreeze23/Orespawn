package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.Crab;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class ModelCrab extends EntityModel<Crab> {
    private final ModelPart body;
    private final ModelPart clawRight;
    private final ModelPart clawLeft;
    private final ModelPart leg1;
    private final ModelPart leg2;
    private final ModelPart leg3;
    private final ModelPart leg4;
    private final ModelPart leg5;
    private final ModelPart leg6;

    public ModelCrab(ModelPart root) {
        this.body = root.getChild("body");
        this.clawRight = root.getChild("claw_right");
        this.clawLeft = root.getChild("claw_left");
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
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, -4.0F, -4.0F, 12, 6, 8), PartPose.offset(0.0F, 18.0F, 0.0F));
        root.addOrReplaceChild("claw_right", CubeListBuilder.create().texOffs(0, 14).addBox(-4.0F, -2.0F, -2.0F, 4, 6, 4), PartPose.offset(-6.0F, 16.0F, -2.0F));
        root.addOrReplaceChild("claw_left", CubeListBuilder.create().texOffs(0, 14).mirror().addBox(0.0F, -2.0F, -2.0F, 4, 6, 4), PartPose.offset(6.0F, 16.0F, -2.0F));
        root.addOrReplaceChild("leg1", CubeListBuilder.create().texOffs(32, 0).addBox(-1.0F, 0.0F, -0.5F, 1, 4, 1), PartPose.offset(-6.0F, 20.0F, -2.0F));
        root.addOrReplaceChild("leg2", CubeListBuilder.create().texOffs(32, 0).addBox(-1.0F, 0.0F, -0.5F, 1, 4, 1), PartPose.offset(-6.0F, 20.0F, 0.0F));
        root.addOrReplaceChild("leg3", CubeListBuilder.create().texOffs(32, 0).addBox(-1.0F, 0.0F, -0.5F, 1, 4, 1), PartPose.offset(-6.0F, 20.0F, 2.0F));
        root.addOrReplaceChild("leg4", CubeListBuilder.create().texOffs(32, 0).mirror().addBox(0.0F, 0.0F, -0.5F, 1, 4, 1), PartPose.offset(6.0F, 20.0F, -2.0F));
        root.addOrReplaceChild("leg5", CubeListBuilder.create().texOffs(32, 0).mirror().addBox(0.0F, 0.0F, -0.5F, 1, 4, 1), PartPose.offset(6.0F, 20.0F, 0.0F));
        root.addOrReplaceChild("leg6", CubeListBuilder.create().texOffs(32, 0).mirror().addBox(0.0F, 0.0F, -0.5F, 1, 4, 1), PartPose.offset(6.0F, 20.0F, 2.0F));
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void setupAnim(Crab entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float swing = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        this.leg1.xRot = swing;
        this.leg2.xRot = -swing;
        this.leg3.xRot = swing;
        this.leg4.xRot = -swing;
        this.leg5.xRot = swing;
        this.leg6.xRot = -swing;
        this.clawRight.xRot = Mth.cos(ageInTicks * 0.15F) * 0.15F;
        this.clawLeft.xRot = -Mth.cos(ageInTicks * 0.15F) * 0.15F;
    }

    @Override
    public void renderToBuffer(PoseStack ps, VertexConsumer vc, int light, int overlay, int color) {
        this.body.render(ps, vc, light, overlay, color);
        this.clawRight.render(ps, vc, light, overlay, color);
        this.clawLeft.render(ps, vc, light, overlay, color);
        this.leg1.render(ps, vc, light, overlay, color);
        this.leg2.render(ps, vc, light, overlay, color);
        this.leg3.render(ps, vc, light, overlay, color);
        this.leg4.render(ps, vc, light, overlay, color);
        this.leg5.render(ps, vc, light, overlay, color);
        this.leg6.render(ps, vc, light, overlay, color);
    }
}
