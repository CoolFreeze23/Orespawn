package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.EntityLeon;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class LeonModel<T extends EntityLeon> extends EntityModel<T> {
    private final ModelPart chest, neck1, neck2, abdomen;
    private final ModelPart head, upperJaw, bottomJaw;
    private final ModelPart arm1L, arm1R, wing3L, wing3R;
    private final ModelPart leg1L, leg1R, leg2L, leg2R;
    private final ModelPart footL, footR;

    public LeonModel(ModelPart root) {
        this.chest = root.getChild("chest");
        this.neck1 = root.getChild("neck1");
        this.neck2 = root.getChild("neck2");
        this.abdomen = root.getChild("abdomen");
        this.head = root.getChild("head");
        this.upperJaw = root.getChild("upperjaw");
        this.bottomJaw = root.getChild("bottomjaw");
        this.arm1L = root.getChild("arm1l");
        this.arm1R = root.getChild("arm1r");
        this.wing3L = root.getChild("wing3l");
        this.wing3R = root.getChild("wing3r");
        this.leg1L = root.getChild("leg1l");
        this.leg1R = root.getChild("leg1r");
        this.leg2L = root.getChild("leg2l");
        this.leg2R = root.getChild("leg2r");
        this.footL = root.getChild("footl");
        this.footR = root.getChild("footr");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("chest", CubeListBuilder.create().texOffs(80, 0).mirror().addBox(-8.0F, -9.5F, -9.5F, 16, 19, 19), PartPose.offsetAndRotation(0.0F, -2.0F, -7.0F, -0.4363323F, 0.0F, 0.0F));
        root.addOrReplaceChild("neck1", CubeListBuilder.create().texOffs(106, 68).mirror().addBox(-5.5F, -7.0F, -9.0F, 11, 14, 11), PartPose.offsetAndRotation(0.0F, -6.0F, -13.0F, -0.8726646F, 0.0F, 0.0F));
        root.addOrReplaceChild("neck2", CubeListBuilder.create().texOffs(71, 69).mirror().addBox(-4.0F, -5.0F, -8.0F, 8, 10, 9), PartPose.offsetAndRotation(0.0F, -12.0F, -17.0F, -1.064651F, 0.0F, 0.0F));
        root.addOrReplaceChild("abdomen", CubeListBuilder.create().texOffs(96, 39).mirror().addBox(-5.0F, -2.0F, 1.0F, 10, 11, 17), PartPose.offsetAndRotation(0.0F, -5.0F, 4.0F, -0.6457718F, 0.0F, 0.0F));
        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(61, 49).mirror().addBox(-4.0F, -2.0F, -4.0F, 8, 8, 9), PartPose.offsetAndRotation(0.0F, -32.0F, -29.0F, -1.413717F, 0.0F, 0.0F));
        root.addOrReplaceChild("upperjaw", CubeListBuilder.create().texOffs(83, 89).mirror().addBox(-3.0F, 4.0F, -4.0F, 6, 13, 5), PartPose.offsetAndRotation(0.0F, -32.0F, -29.0F, -1.37881F, 0.0F, 0.0F));
        root.addOrReplaceChild("bottomjaw", CubeListBuilder.create().texOffs(85, 108).mirror().addBox(-2.5F, -1.0F, -1.5F, 5, 12, 3), PartPose.offsetAndRotation(0.0F, -28.0F, -34.0F, -1.413717F, 0.0F, 0.0F));
        root.addOrReplaceChild("arm1l", CubeListBuilder.create().texOffs(77, 150).mirror().addBox(0.0F, -1.0F, -1.0F, 3, 18, 5), PartPose.offsetAndRotation(8.0F, -8.0F, -10.0F, -0.0698132F, 0.0F, -0.7679449F));
        root.addOrReplaceChild("arm1r", CubeListBuilder.create().texOffs(77, 127).mirror().addBox(-3.0F, -1.0F, -1.0F, 3, 18, 5), PartPose.offsetAndRotation(-8.0F, -8.0F, -10.0F, -0.0698132F, 0.0F, 0.7679449F));
        root.addOrReplaceChild("wing3l", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-7.5F, 0.0F, -5.0F, 16, 1, 26), PartPose.offsetAndRotation(-5.0F, 0.0F, 12.0F, -0.4886922F, -0.5235988F, 0.4014257F));
        root.addOrReplaceChild("wing3r", CubeListBuilder.create().texOffs(150, 0).mirror().addBox(-8.5F, 0.0F, -5.0F, 16, 1, 26), PartPose.offsetAndRotation(4.0F, 0.0F, 12.0F, -0.4886922F, 0.5235988F, -0.4014257F));
        root.addOrReplaceChild("leg1l", CubeListBuilder.create().texOffs(0, 104).mirror().addBox(0.0F, -3.0F, -4.0F, 3, 15, 7), PartPose.offsetAndRotation(5.0F, 5.0F, 10.0F, -0.6108652F, 0.0F, -0.3316126F));
        root.addOrReplaceChild("leg1r", CubeListBuilder.create().texOffs(0, 149).mirror().addBox(-3.0F, -3.0F, -4.0F, 3, 15, 7), PartPose.offsetAndRotation(-6.0F, 5.0F, 10.0F, -0.6108652F, 0.0F, 0.3316126F));
        root.addOrReplaceChild("leg2l", CubeListBuilder.create().texOffs(21, 108).mirror().addBox(1.0F, 0.0F, -3.0F, 2, 14, 4), PartPose.offsetAndRotation(8.0F, 13.0F, 6.0F, 0.6108652F, 0.0F, -0.1745329F));
        root.addOrReplaceChild("leg2r", CubeListBuilder.create().texOffs(21, 108).mirror().addBox(-2.0F, 0.0F, -3.0F, 2, 14, 4), PartPose.offsetAndRotation(-10.0F, 13.0F, 6.0F, 0.6108652F, 0.0F, 0.1745329F));
        root.addOrReplaceChild("footl", CubeListBuilder.create().texOffs(50, 29).mirror().addBox(-2.0F, -1.0F, -8.0F, 4, 2, 9), PartPose.offset(12.0F, 24.0F, 11.0F));
        root.addOrReplaceChild("footr", CubeListBuilder.create().texOffs(50, 29).mirror().addBox(-1.0F, 1.0F, -8.0F, 4, 2, 9), PartPose.offset(-14.0F, 22.0F, 11.0F));
        return LayerDefinition.create(mesh, 256, 256);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float wingAngle = Mth.cos(ageInTicks * 1.6f) * (float) Math.PI * 0.06f;
        float legAngle = limbSwingAmount > 0.1f ? Mth.cos(ageInTicks * 1.8f) * (float) Math.PI * 0.25f * limbSwingAmount : 0.0f;
        this.leg1L.xRot = -0.611f + legAngle;
        this.leg1R.xRot = -0.611f - legAngle;
        this.arm1L.xRot = -0.07f - legAngle / 2.0f;
        this.arm1R.xRot = -0.07f + legAngle / 2.0f;
        this.wing3R.yRot = 0.523f - legAngle / 10.0f;
        this.wing3L.yRot = -0.523f - legAngle / 10.0f;
        this.head.yRot = (float) Math.toRadians(netHeadYaw) * 0.5f;
        this.upperJaw.yRot = this.head.yRot;
        this.bottomJaw.yRot = this.head.yRot;
        this.bottomJaw.xRot = -1.308f + wingAngle / 2.0f;
    }

    @Override
    public void renderToBuffer(PoseStack ps, VertexConsumer vc, int light, int overlay, int color) {
        chest.render(ps, vc, light, overlay, color);
        neck1.render(ps, vc, light, overlay, color);
        neck2.render(ps, vc, light, overlay, color);
        abdomen.render(ps, vc, light, overlay, color);
        head.render(ps, vc, light, overlay, color);
        upperJaw.render(ps, vc, light, overlay, color);
        bottomJaw.render(ps, vc, light, overlay, color);
        arm1L.render(ps, vc, light, overlay, color);
        arm1R.render(ps, vc, light, overlay, color);
        wing3L.render(ps, vc, light, overlay, color);
        wing3R.render(ps, vc, light, overlay, color);
        leg1L.render(ps, vc, light, overlay, color);
        leg1R.render(ps, vc, light, overlay, color);
        leg2L.render(ps, vc, light, overlay, color);
        leg2R.render(ps, vc, light, overlay, color);
        footL.render(ps, vc, light, overlay, color);
        footR.render(ps, vc, light, overlay, color);
    }
}
