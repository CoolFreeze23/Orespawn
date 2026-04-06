package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.EntityEmperorScorpion;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class EmperorScorpionModel<T extends EntityEmperorScorpion> extends EntityModel<T> {
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart leg1;
    private final ModelPart leg2;
    private final ModelPart leg3;
    private final ModelPart leg4;
    private final ModelPart leg5;
    private final ModelPart leg6;
    private final ModelPart leg7;
    private final ModelPart leg8;
    private final ModelPart clawLeft;
    private final ModelPart clawRight;
    private final ModelPart tail1;
    private final ModelPart tail2;
    private final ModelPart tail3;
    private final ModelPart stinger;

    public EmperorScorpionModel(ModelPart root) {
        this.body = root.getChild("body");
        this.head = root.getChild("head");
        this.leg1 = root.getChild("leg1");
        this.leg2 = root.getChild("leg2");
        this.leg3 = root.getChild("leg3");
        this.leg4 = root.getChild("leg4");
        this.leg5 = root.getChild("leg5");
        this.leg6 = root.getChild("leg6");
        this.leg7 = root.getChild("leg7");
        this.leg8 = root.getChild("leg8");
        this.clawLeft = root.getChild("claw_left");
        this.clawRight = root.getChild("claw_right");
        this.tail1 = root.getChild("tail1");
        this.tail2 = root.getChild("tail2");
        this.tail3 = root.getChild("tail3");
        this.stinger = root.getChild("stinger");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-12.0F, -5.0F, -16.0F, 24, 10, 32), PartPose.offset(0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 42)
                .addBox(-8.0F, -6.0F, -10.0F, 16, 10, 10), PartPose.offset(0.0F, -1.0F, -16.0F));

        root.addOrReplaceChild("leg1", CubeListBuilder.create().texOffs(0, 62)
                .addBox(-2.0F, 0.0F, -2.0F, 4, 18, 4), PartPose.offset(-10.0F, 5.0F, -12.0F));
        root.addOrReplaceChild("leg2", CubeListBuilder.create().texOffs(0, 62)
                .addBox(-2.0F, 0.0F, -2.0F, 4, 18, 4), PartPose.offset(10.0F, 5.0F, -12.0F));
        root.addOrReplaceChild("leg3", CubeListBuilder.create().texOffs(0, 62)
                .addBox(-2.0F, 0.0F, -2.0F, 4, 18, 4), PartPose.offset(-10.0F, 5.0F, -4.0F));
        root.addOrReplaceChild("leg4", CubeListBuilder.create().texOffs(0, 62)
                .addBox(-2.0F, 0.0F, -2.0F, 4, 18, 4), PartPose.offset(10.0F, 5.0F, -4.0F));
        root.addOrReplaceChild("leg5", CubeListBuilder.create().texOffs(0, 62)
                .addBox(-2.0F, 0.0F, -2.0F, 4, 18, 4), PartPose.offset(-10.0F, 5.0F, 4.0F));
        root.addOrReplaceChild("leg6", CubeListBuilder.create().texOffs(0, 62)
                .addBox(-2.0F, 0.0F, -2.0F, 4, 18, 4), PartPose.offset(10.0F, 5.0F, 4.0F));
        root.addOrReplaceChild("leg7", CubeListBuilder.create().texOffs(0, 62)
                .addBox(-2.0F, 0.0F, -2.0F, 4, 18, 4), PartPose.offset(-10.0F, 5.0F, 12.0F));
        root.addOrReplaceChild("leg8", CubeListBuilder.create().texOffs(0, 62)
                .addBox(-2.0F, 0.0F, -2.0F, 4, 18, 4), PartPose.offset(10.0F, 5.0F, 12.0F));

        root.addOrReplaceChild("claw_left", CubeListBuilder.create().texOffs(52, 42)
                .addBox(-10.0F, -3.0F, -12.0F, 10, 6, 12), PartPose.offset(-10.0F, -1.0F, -16.0F));
        root.addOrReplaceChild("claw_right", CubeListBuilder.create().texOffs(52, 42).mirror()
                .addBox(0.0F, -3.0F, -12.0F, 10, 6, 12), PartPose.offset(10.0F, -1.0F, -16.0F));

        root.addOrReplaceChild("tail1", CubeListBuilder.create().texOffs(80, 0)
                .addBox(-4.0F, -4.0F, 0.0F, 8, 8, 10), PartPose.offset(0.0F, -2.0F, 16.0F));
        root.addOrReplaceChild("tail2", CubeListBuilder.create().texOffs(80, 18)
                .addBox(-3.0F, -3.0F, 0.0F, 6, 6, 10), PartPose.offset(0.0F, -8.0F, 22.0F));
        root.addOrReplaceChild("tail3", CubeListBuilder.create().texOffs(80, 34)
                .addBox(-2.0F, -2.0F, 0.0F, 4, 4, 8), PartPose.offset(0.0F, -14.0F, 28.0F));
        root.addOrReplaceChild("stinger", CubeListBuilder.create().texOffs(104, 34)
                .addBox(-1.5F, -1.5F, 0.0F, 3, 3, 6), PartPose.offset(0.0F, -18.0F, 34.0F));

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
        this.head.xRot = headPitch * ((float) Math.PI / 180F);

        float legSwing = Mth.cos(limbSwing * 0.6662F) * 0.5F * limbSwingAmount;
        this.leg1.xRot = legSwing;
        this.leg2.xRot = -legSwing;
        this.leg3.xRot = -legSwing;
        this.leg4.xRot = legSwing;
        this.leg5.xRot = legSwing;
        this.leg6.xRot = -legSwing;
        this.leg7.xRot = -legSwing;
        this.leg8.xRot = legSwing;

        float clawAnim = Mth.cos(ageInTicks * 0.1F) * 0.15F;
        this.clawLeft.yRot = 0.4F - clawAnim;
        this.clawRight.yRot = -0.4F + clawAnim;

        float tailSway = Mth.cos(ageInTicks * 0.06F) * 0.1F;
        this.tail1.xRot = -0.35F;
        this.tail2.xRot = -0.45F + tailSway;
        this.tail3.xRot = -0.5F + tailSway;
        this.stinger.xRot = -0.3F;
    }

    @Override
    public void renderToBuffer(PoseStack ps, VertexConsumer vc, int light, int overlay, int color) {
        body.render(ps, vc, light, overlay, color);
        head.render(ps, vc, light, overlay, color);
        leg1.render(ps, vc, light, overlay, color);
        leg2.render(ps, vc, light, overlay, color);
        leg3.render(ps, vc, light, overlay, color);
        leg4.render(ps, vc, light, overlay, color);
        leg5.render(ps, vc, light, overlay, color);
        leg6.render(ps, vc, light, overlay, color);
        leg7.render(ps, vc, light, overlay, color);
        leg8.render(ps, vc, light, overlay, color);
        clawLeft.render(ps, vc, light, overlay, color);
        clawRight.render(ps, vc, light, overlay, color);
        tail1.render(ps, vc, light, overlay, color);
        tail2.render(ps, vc, light, overlay, color);
        tail3.render(ps, vc, light, overlay, color);
        stinger.render(ps, vc, light, overlay, color);
    }
}
