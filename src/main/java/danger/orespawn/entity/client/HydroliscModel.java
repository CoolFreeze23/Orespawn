package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.EntityHydrolisc;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class HydroliscModel<T extends EntityHydrolisc> extends EntityModel<T> {
    private final ModelPart body0, body1, body2;
    private final ModelPart head1, head2, head3;
    private final ModelPart tail1, tail2, tail3;
    private final ModelPart feather1, feather2, feather3;
    private final ModelPart lf1, rf1, lb1, rb1;

    public HydroliscModel(ModelPart root) {
        this.body0 = root.getChild("body0");
        this.body1 = root.getChild("body1");
        this.body2 = root.getChild("body2");
        this.head1 = root.getChild("head1");
        this.head2 = root.getChild("head2");
        this.head3 = root.getChild("head3");
        this.tail1 = root.getChild("tail1");
        this.tail2 = root.getChild("tail2");
        this.tail3 = root.getChild("tail3");
        this.feather1 = root.getChild("feather1");
        this.feather2 = root.getChild("feather2");
        this.feather3 = root.getChild("feather3");
        this.lf1 = root.getChild("lf1");
        this.rf1 = root.getChild("rf1");
        this.lb1 = root.getChild("lb1");
        this.rb1 = root.getChild("rb1");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body0", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-1.0F, 14.0F, -13.0F, 4, 3, 10), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0523599F, 0.0F, 0.0F));
        root.addOrReplaceChild("body1", CubeListBuilder.create().texOffs(0, 79).mirror().addBox(-2.0F, 16.0F, -7.0F, 4, 2, 5), PartPose.offset(1.0F, -1.0F, 2.0F));
        root.addOrReplaceChild("body2", CubeListBuilder.create().texOffs(0, 99).mirror().addBox(-2.0F, 14.0F, 0.0F, 6, 4, 10), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.0523599F, 0.0F, 0.0F));
        root.addOrReplaceChild("head1", CubeListBuilder.create().texOffs(38, 41).mirror().addBox(0.0F, 0.0F, 0.0F, 4, 3, 4), PartPose.offsetAndRotation(-1.0F, 15.0F, -15.0F, 0.1396263F, 0.0F, 0.0F));
        root.addOrReplaceChild("head2", CubeListBuilder.create().texOffs(19, 80).mirror().addBox(-1.0F, 16.0F, -16.0F, 4, 1, 5), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.1047198F, 0.0F, 0.0F));
        root.addOrReplaceChild("head3", CubeListBuilder.create().texOffs(38, 50).mirror().addBox(0.0F, 0.0F, 0.0F, 4, 2, 8), PartPose.offsetAndRotation(-1.0F, 15.0F, -13.0F, 0.5235988F, 0.0F, 0.0F));
        root.addOrReplaceChild("tail1", CubeListBuilder.create().texOffs(9, 18).mirror().addBox(-1.0F, -1.0F, -3.0F, 2, 8, 2), PartPose.offsetAndRotation(1.0F, 15.0F, 9.0F, 1.095163F, 0.0F, 0.0F));
        root.addOrReplaceChild("tail2", CubeListBuilder.create().texOffs(29, 3).mirror().addBox(-1.0F, 0.0F, -0.8F, 2, 8, 2), PartPose.offsetAndRotation(1.0F, 20.0F, 13.53333F, 1.392442F, 0.0F, 0.0F));
        root.addOrReplaceChild("tail3", CubeListBuilder.create().texOffs(39, 0).mirror().addBox(-1.0F, -1.0F, -2.0F, 2, 8, 2), PartPose.offsetAndRotation(1.0F, 20.0F, 21.0F, 1.72705F, 0.0F, 0.0F));
        root.addOrReplaceChild("feather1", CubeListBuilder.create().texOffs(34, 100).mirror().addBox(0.0F, 0.0F, 1.0F, 1, 2, 9), PartPose.offsetAndRotation(0.0F, 12.0F, -8.0F, 0.3490659F, -0.2617994F, 0.0F));
        root.addOrReplaceChild("feather2", CubeListBuilder.create().texOffs(0, 116).mirror().addBox(0.0F, 0.0F, 0.0F, 1, 2, 10), PartPose.offsetAndRotation(0.5F, 11.0F, -6.0F, 0.3490659F, 0.0F, 0.0F));
        root.addOrReplaceChild("feather3", CubeListBuilder.create().texOffs(25, 117).mirror().addBox(0.0F, 0.0F, 1.0F, 1, 2, 9), PartPose.offsetAndRotation(1.0F, 12.0F, -8.0F, 0.3490659F, 0.2617994F, 0.0F));
        root.addOrReplaceChild("lf1", CubeListBuilder.create().texOffs(45, 32).mirror().addBox(-1.0F, 0.0F, -2.0F, 4, 3, 3), PartPose.offset(4.0F, 14.0F, -7.0F));
        root.addOrReplaceChild("rf1", CubeListBuilder.create().texOffs(45, 32).mirror().addBox(-3.0F, 0.0F, -2.0F, 4, 3, 3), PartPose.offset(-2.0F, 14.0F, -7.0F));
        root.addOrReplaceChild("lb1", CubeListBuilder.create().texOffs(46, 22).mirror().addBox(-1.0F, 0.0F, 0.0F, 4, 3, 3), PartPose.offset(5.0F, 15.0F, 0.0F));
        root.addOrReplaceChild("rb1", CubeListBuilder.create().texOffs(46, 22).mirror().addBox(-4.0F, 0.0F, 0.0F, 4, 3, 3), PartPose.offset(-2.0F, 15.0F, 0.0F));
        return LayerDefinition.create(mesh, 64, 128);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float legAngle = limbSwingAmount > 0.1f ? Mth.cos(ageInTicks * 1.3f) * (float) Math.PI * 0.25f * limbSwingAmount : 0.0f;
        this.lf1.xRot = legAngle;
        this.rf1.xRot = -legAngle;
        this.lb1.xRot = -legAngle;
        this.rb1.xRot = legAngle;
        float tailAngle = Mth.cos(ageInTicks * 1.0f) * (float) Math.PI * 0.15f;
        this.tail1.yRot = tailAngle * 0.25f;
        float featherAngle = Mth.cos(ageInTicks * 1.25f) * (float) Math.PI * 0.2f;
        this.feather2.yRot = featherAngle;
    }

    @Override
    public void renderToBuffer(PoseStack ps, VertexConsumer vc, int light, int overlay, int color) {
        body0.render(ps, vc, light, overlay, color);
        body1.render(ps, vc, light, overlay, color);
        body2.render(ps, vc, light, overlay, color);
        head1.render(ps, vc, light, overlay, color);
        head2.render(ps, vc, light, overlay, color);
        head3.render(ps, vc, light, overlay, color);
        tail1.render(ps, vc, light, overlay, color);
        tail2.render(ps, vc, light, overlay, color);
        tail3.render(ps, vc, light, overlay, color);
        feather1.render(ps, vc, light, overlay, color);
        feather2.render(ps, vc, light, overlay, color);
        feather3.render(ps, vc, light, overlay, color);
        lf1.render(ps, vc, light, overlay, color);
        rf1.render(ps, vc, light, overlay, color);
        lb1.render(ps, vc, light, overlay, color);
        rb1.render(ps, vc, light, overlay, color);
    }
}
