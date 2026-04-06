package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.EntityKyuubi;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class KyuubiModel<T extends EntityKyuubi> extends EntityModel<T> {
    private final ModelPart head, chest, body;
    private final ModelPart lfArmUpper, lfArmLower, rtArmUpper, rtArmLower;
    private final ModelPart lfLegUpper, lfLegLower, rtLegUpper, rtLegLower;
    private final ModelPart tail0, tail1, tail2, tail3, tail4;

    public KyuubiModel(ModelPart root) {
        this.head = root.getChild("head");
        this.chest = root.getChild("chest");
        this.body = root.getChild("body");
        this.lfArmUpper = root.getChild("lfarmupper");
        this.lfArmLower = root.getChild("lfarmlower");
        this.rtArmUpper = root.getChild("rtarmupper");
        this.rtArmLower = root.getChild("rtarmlower");
        this.lfLegUpper = root.getChild("lflegupper");
        this.lfLegLower = root.getChild("lfleglower");
        this.rtLegUpper = root.getChild("rtlegupper");
        this.rtLegLower = root.getChild("rtleglower");
        this.tail0 = root.getChild("tail0");
        this.tail1 = root.getChild("tail1");
        this.tail2 = root.getChild("tail2");
        this.tail3 = root.getChild("tail3");
        this.tail4 = root.getChild("tail4");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(168, 0).addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8), PartPose.offsetAndRotation(0.0F, 0.0F, 6.0F, 0.2230705F, 3.141593F, 0.0F));
        root.addOrReplaceChild("chest", CubeListBuilder.create().texOffs(170, 17).addBox(-4.0F, 0.0F, -2.0F, 8, 7, 6), PartPose.offsetAndRotation(0.0F, 0.0F, 5.0F, -0.8551081F, 0.0F, 0.0F));
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(170, 31).addBox(-4.0F, 0.0F, -3.0F, 8, 7, 6), PartPose.offsetAndRotation(0.0F, 5.0F, 1.0F, -0.2974289F, 0.0F, 0.0F));
        root.addOrReplaceChild("lfarmupper", CubeListBuilder.create().texOffs(205, 16).addBox(-3.0F, -2.0F, -2.0F, 4, 7, 4), PartPose.offsetAndRotation(-5.0F, 2.0F, 3.0F, 0.0F, 0.0F, 0.3020292F));
        root.addOrReplaceChild("lfarmlower", CubeListBuilder.create().texOffs(208, 31).addBox(-2.0F, 0.0F, -2.0F, 4, 7, 4), PartPose.offsetAndRotation(-7.0F, 6.0F, 2.0F, 0.4833219F, 0.0F, 0.0F));
        root.addOrReplaceChild("rtarmupper", CubeListBuilder.create().texOffs(142, 16).addBox(-1.0F, -2.0F, -2.0F, 4, 7, 4), PartPose.offsetAndRotation(5.0F, 2.0F, 2.0F, 0.0F, 0.0F, -0.302028F));
        root.addOrReplaceChild("rtarmlower", CubeListBuilder.create().texOffs(136, 29).addBox(-2.0F, 0.0F, -2.0F, 4, 7, 4), PartPose.offsetAndRotation(7.0F, 6.0F, 2.0F, 0.4833219F, 0.0F, 0.0F));
        root.addOrReplaceChild("lflegupper", CubeListBuilder.create().texOffs(188, 46).addBox(-2.0F, 0.0F, -2.0F, 4, 7, 4), PartPose.offsetAndRotation(-2.0F, 12.0F, 0.0F, 0.260246F, 0.0F, 0.2602503F));
        root.addOrReplaceChild("lfleglower", CubeListBuilder.create().texOffs(205, 55).addBox(-2.0F, 0.0F, -3.0F, 4, 6, 4), PartPose.offsetAndRotation(-3.0F, 18.0F, 2.0F, -0.4461433F, 0.0F, 0.0F));
        root.addOrReplaceChild("rtlegupper", CubeListBuilder.create().texOffs(168, 46).addBox(-2.0F, 0.0F, -2.0F, 4, 7, 4), PartPose.offsetAndRotation(2.0F, 12.0F, 0.0F, 0.5948578F, 0.0F, -0.260246F));
        root.addOrReplaceChild("rtleglower", CubeListBuilder.create().texOffs(149, 53).addBox(-2.0F, -1.0F, -3.0F, 4, 7, 4), PartPose.offsetAndRotation(3.0F, 18.0F, 4.0F, -0.1487144F, 0.0F, 0.0F));
        root.addOrReplaceChild("tail0", CubeListBuilder.create().texOffs(0, 46).addBox(-1.0F, -1.0F, -3.0F, 4, 4, 3), PartPose.offset(-1.0F, 9.0F, -2.0F));
        root.addOrReplaceChild("tail1", CubeListBuilder.create().texOffs(9, 36).addBox(-1.5F, -1.5F, -3.0F, 3, 3, 3), PartPose.offsetAndRotation(0.0F, 10.0F, -4.333333F, -0.2602503F, 0.0F, 0.0F));
        root.addOrReplaceChild("tail2", CubeListBuilder.create().texOffs(20, 43).addBox(-2.0F, -2.0F, -5.0F, 4, 4, 5), PartPose.offsetAndRotation(0.0F, 10.0F, -7.0F, -0.7807508F, 0.0F, 0.0F));
        root.addOrReplaceChild("tail3", CubeListBuilder.create().texOffs(38, 42).addBox(-2.5F, -2.0F, -5.0F, 5, 5, 5), PartPose.offsetAndRotation(0.0F, 6.5F, -10.0F, -0.96F, 0.0F, 0.0F));
        root.addOrReplaceChild("tail4", CubeListBuilder.create().texOffs(59, 41).addBox(-3.0F, -3.0F, -6.0F, 6, 6, 6), PartPose.offsetAndRotation(0.0F, 3.0F, -12.0F, -0.22F, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, 512, 256);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float legAngle = limbSwingAmount > 0.1f ? Mth.cos(ageInTicks * 1.1f) * (float) Math.PI * 0.2f * limbSwingAmount : 0.0f;
        this.rtLegUpper.xRot = 0.59f + legAngle;
        this.lfLegUpper.xRot = 0.26f - legAngle;
        float armAngle = Mth.cos(ageInTicks * 1.1f) * (float) Math.PI * 0.08f * limbSwingAmount;
        this.rtArmUpper.xRot = armAngle;
        this.lfArmUpper.xRot = -armAngle;
        this.head.yRot = (float) Math.toRadians(netHeadYaw) + (float) Math.PI;
        float tailAngle = Mth.cos(ageInTicks * 0.9f) * (float) Math.PI * 0.2f;
        this.tail1.yRot = tailAngle;
    }

    @Override
    public void renderToBuffer(PoseStack ps, VertexConsumer vc, int light, int overlay, int color) {
        head.render(ps, vc, light, overlay, color);
        chest.render(ps, vc, light, overlay, color);
        body.render(ps, vc, light, overlay, color);
        lfArmUpper.render(ps, vc, light, overlay, color);
        lfArmLower.render(ps, vc, light, overlay, color);
        rtArmUpper.render(ps, vc, light, overlay, color);
        rtArmLower.render(ps, vc, light, overlay, color);
        lfLegUpper.render(ps, vc, light, overlay, color);
        lfLegLower.render(ps, vc, light, overlay, color);
        rtLegUpper.render(ps, vc, light, overlay, color);
        rtLegLower.render(ps, vc, light, overlay, color);
        tail0.render(ps, vc, light, overlay, color);
        tail1.render(ps, vc, light, overlay, color);
        tail2.render(ps, vc, light, overlay, color);
        tail3.render(ps, vc, light, overlay, color);
        tail4.render(ps, vc, light, overlay, color);
    }
}
