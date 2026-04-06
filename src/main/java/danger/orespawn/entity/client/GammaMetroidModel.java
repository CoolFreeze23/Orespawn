package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.EntityGammaMetroid;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class GammaMetroidModel<T extends EntityGammaMetroid> extends EntityModel<T> {
    private final ModelPart shell1, shell2, shell3, shell4;
    private final ModelPart head, beakUpper, beakLower;
    private final ModelPart leftTusk, middleTusk, rightTusk;
    private final ModelPart core, bellyInside, bellyOutside;
    private final ModelPart lfUpperLeg, lfLowerLeg, lrUpperLeg, lrLowerLeg;
    private final ModelPart rfUpperLeg, rfLowerLeg, rrUpperLeg, rrLowerLeg;

    public GammaMetroidModel(ModelPart root) {
        this.shell1 = root.getChild("shell1");
        this.shell2 = root.getChild("shell2");
        this.shell3 = root.getChild("shell3");
        this.shell4 = root.getChild("shell4");
        this.head = root.getChild("head");
        this.beakUpper = root.getChild("beakupper");
        this.beakLower = root.getChild("beaklower");
        this.leftTusk = root.getChild("lefttusk");
        this.middleTusk = root.getChild("middletusk");
        this.rightTusk = root.getChild("righttusk");
        this.core = root.getChild("core");
        this.bellyInside = root.getChild("bellyinside");
        this.bellyOutside = root.getChild("bellyoutside");
        this.lfUpperLeg = root.getChild("lfupperleg");
        this.lfLowerLeg = root.getChild("lflowerleg");
        this.lrUpperLeg = root.getChild("lrupperleg");
        this.lrLowerLeg = root.getChild("lrlowerleg");
        this.rfUpperLeg = root.getChild("rfupperleg");
        this.rfLowerLeg = root.getChild("rflowerleg");
        this.rrUpperLeg = root.getChild("rrupperleg");
        this.rrLowerLeg = root.getChild("rrlowerleg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("shell1", CubeListBuilder.create().texOffs(64, 0).mirror().addBox(-10.0F, -10.0F, 2.0F, 19, 19, 12), PartPose.offsetAndRotation(0.0F, 4.0F, -7.0F, 0.0F, 0.0F, 0.7853982F));
        root.addOrReplaceChild("shell2", CubeListBuilder.create().texOffs(0, 30).mirror().addBox(-9.0F, -9.0F, 0.0F, 16, 16, 8), PartPose.offsetAndRotation(0.0F, 4.5F, 5.0F, -0.5235988F, 0.3665191F, 0.715585F));
        root.addOrReplaceChild("shell3", CubeListBuilder.create().texOffs(128, 0).mirror().addBox(-6.0F, -6.0F, 0.0F, 12, 12, 7), PartPose.offsetAndRotation(0.0F, 7.0F, 10.0F, -0.9599311F, 0.6283185F, 0.5235988F));
        root.addOrReplaceChild("shell4", CubeListBuilder.create().texOffs(48, 34).mirror().addBox(0.0F, 0.0F, 0.0F, 6, 6, 8), PartPose.offsetAndRotation(-3.0F, 9.0F, 13.0F, -0.2792527F, 0.0F, 0.0F));
        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(48, 48).mirror().addBox(0.0F, 0.0F, 0.0F, 16, 8, 6), PartPose.offset(-8.0F, -1.0F, -11.0F));
        root.addOrReplaceChild("beakupper", CubeListBuilder.create().texOffs(114, 44).mirror().addBox(-3.0F, 0.0F, -3.0F, 6, 4, 6), PartPose.offsetAndRotation(0.0F, 5.0F, -11.0F, 0.1047198F, 0.7853982F, 0.1047198F));
        root.addOrReplaceChild("beaklower", CubeListBuilder.create().texOffs(120, 54).mirror().addBox(-1.5F, 0.0F, -1.5F, 3, 6, 3), PartPose.offsetAndRotation(0.0F, 9.0F, -12.0F, 0.1396263F, 0.7853982F, 0.1396263F));
        root.addOrReplaceChild("lefttusk", CubeListBuilder.create().texOffs(76, 50).mirror().addBox(0.0F, 0.0F, -12.0F, 2, 2, 12), PartPose.offsetAndRotation(5.0F, 6.0F, -10.0F, 0.1047198F, 0.0872665F, 0.0F));
        root.addOrReplaceChild("middletusk", CubeListBuilder.create().texOffs(76, 50).mirror().addBox(-1.0F, 0.0F, -12.0F, 2, 2, 12), PartPose.offsetAndRotation(0.0F, -2.0F, -10.0F, 0.122173F, 0.0F, 0.0F));
        root.addOrReplaceChild("righttusk", CubeListBuilder.create().texOffs(76, 50).mirror().addBox(-2.0F, 0.0F, -12.0F, 2, 2, 12), PartPose.offsetAndRotation(-5.0F, 6.0F, -10.0F, 0.1047198F, -0.0872665F, 0.0F));
        root.addOrReplaceChild("core", CubeListBuilder.create().texOffs(82, 33).mirror().addBox(-3.0F, 0.0F, -3.0F, 6, 6, 6), PartPose.offsetAndRotation(0.0F, 8.0F, 3.0F, -0.122173F, 0.0F, 0.0F));
        root.addOrReplaceChild("bellyinside", CubeListBuilder.create().texOffs(150, 3).mirror().addBox(-8.0F, -1.0F, -8.0F, 16, 1, 16), PartPose.offsetAndRotation(0.0F, 8.0F, 2.0F, -0.122173F, 0.0F, 0.0F));
        root.addOrReplaceChild("bellyoutside", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-8.0F, -6.0F, -8.0F, 16, 14, 16), PartPose.offsetAndRotation(0.0F, 8.0F, 2.0F, -0.122173F, 0.0F, 0.0F));
        root.addOrReplaceChild("lfupperleg", CubeListBuilder.create().texOffs(64, 0).mirror().addBox(0.0F, 0.0F, -1.5F, 3, 8, 3), PartPose.offsetAndRotation(8.0F, 8.0F, -2.0F, -0.1745329F, 0.0F, -0.6632251F));
        root.addOrReplaceChild("lflowerleg", CubeListBuilder.create().texOffs(48, 0).mirror().addBox(-1.0F, 0.0F, -1.0F, 2, 11, 2), PartPose.offsetAndRotation(14.0F, 13.0F, -3.5F, -0.2617994F, 0.1396263F, 0.0F));
        root.addOrReplaceChild("lrupperleg", CubeListBuilder.create().texOffs(64, 0).mirror().addBox(-1.0F, 0.0F, -1.5F, 3, 8, 3), PartPose.offsetAndRotation(8.0F, 9.0F, 7.0F, 0.1745329F, 0.0F, -0.8203047F));
        root.addOrReplaceChild("lrlowerleg", CubeListBuilder.create().texOffs(48, 0).mirror().addBox(-1.0F, 0.0F, -1.0F, 2, 11, 2), PartPose.offsetAndRotation(14.0F, 14.0F, 8.5F, 0.3141593F, -0.1570796F, -0.2792527F));
        root.addOrReplaceChild("rfupperleg", CubeListBuilder.create().texOffs(64, 0).mirror().addBox(-3.0F, 0.0F, -1.5F, 3, 8, 3), PartPose.offsetAndRotation(-8.0F, 8.0F, -2.0F, -0.1745329F, 0.0F, 0.6632251F));
        root.addOrReplaceChild("rflowerleg", CubeListBuilder.create().texOffs(48, 0).mirror().addBox(-1.0F, 0.0F, -1.0F, 2, 11, 2), PartPose.offsetAndRotation(-14.0F, 13.0F, -3.5F, -0.2617994F, -0.1396263F, 0.0F));
        root.addOrReplaceChild("rrupperleg", CubeListBuilder.create().texOffs(64, 0).mirror().addBox(-2.0F, 0.0F, -1.5F, 3, 8, 3), PartPose.offsetAndRotation(-8.0F, 9.0F, 7.0F, 0.1745329F, 0.0F, 0.8203047F));
        root.addOrReplaceChild("rrlowerleg", CubeListBuilder.create().texOffs(48, 0).mirror().addBox(-1.0F, 0.0F, -1.0F, 2, 11, 2), PartPose.offsetAndRotation(-14.0F, 14.0F, 8.5F, 0.3141593F, 0.1570796F, 0.2792527F));
        return LayerDefinition.create(mesh, 256, 64);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float tuskAngle = Mth.cos(ageInTicks * 0.81f) * (float) Math.PI * 0.08f;
        this.leftTusk.xRot = tuskAngle;
        this.rightTusk.xRot = tuskAngle;
        this.middleTusk.xRot = tuskAngle;
        float shellAngle = Mth.cos(ageInTicks * 0.4f) * (float) Math.PI * 0.05f;
        this.shell1.xRot = shellAngle / 4.0f;
        float beakAngle = Math.abs(Mth.cos(ageInTicks * 0.75f) * (float) Math.PI * 0.1f);
        this.beakLower.xRot = beakAngle + 0.14f;
    }

    @Override
    public void renderToBuffer(PoseStack ps, VertexConsumer vc, int light, int overlay, int color) {
        core.render(ps, vc, light, overlay, color);
        shell1.render(ps, vc, light, overlay, color);
        shell2.render(ps, vc, light, overlay, color);
        shell3.render(ps, vc, light, overlay, color);
        shell4.render(ps, vc, light, overlay, color);
        head.render(ps, vc, light, overlay, color);
        beakUpper.render(ps, vc, light, overlay, color);
        beakLower.render(ps, vc, light, overlay, color);
        leftTusk.render(ps, vc, light, overlay, color);
        middleTusk.render(ps, vc, light, overlay, color);
        rightTusk.render(ps, vc, light, overlay, color);
        lfUpperLeg.render(ps, vc, light, overlay, color);
        lfLowerLeg.render(ps, vc, light, overlay, color);
        lrUpperLeg.render(ps, vc, light, overlay, color);
        lrLowerLeg.render(ps, vc, light, overlay, color);
        rfUpperLeg.render(ps, vc, light, overlay, color);
        rfLowerLeg.render(ps, vc, light, overlay, color);
        rrUpperLeg.render(ps, vc, light, overlay, color);
        rrLowerLeg.render(ps, vc, light, overlay, color);
        bellyInside.render(ps, vc, light, overlay, color);
        bellyOutside.render(ps, vc, light, overlay, color);
    }
}
