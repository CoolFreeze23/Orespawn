package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.EntityMolenoid;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class MolenoidModel<T extends EntityMolenoid> extends EntityModel<T> {
    private final ModelPart body, shoulders, head1, head2;
    private final ModelPart larm, rarm, lleg, rleg;
    private final ModelPart butt, tail;

    public MolenoidModel(ModelPart root) {
        this.body = root.getChild("body");
        this.shoulders = root.getChild("shoulders");
        this.head1 = root.getChild("head1");
        this.head2 = root.getChild("head2");
        this.larm = root.getChild("larm");
        this.rarm = root.getChild("rarm");
        this.lleg = root.getChild("lleg");
        this.rleg = root.getChild("rleg");
        this.butt = root.getChild("butt");
        this.tail = root.getChild("tail");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 176).mirror().addBox(-16.0F, 0.0F, 0.0F, 32, 20, 56), PartPose.offset(0.0F, 1.0F, 6.0F));
        root.addOrReplaceChild("shoulders", CubeListBuilder.create().texOffs(0, 143).mirror().addBox(-17.0F, 0.0F, 0.0F, 34, 17, 10), PartPose.offset(0.0F, 3.0F, -4.0F));
        root.addOrReplaceChild("head1", CubeListBuilder.create().texOffs(0, 114).mirror().addBox(-14.0F, 0.0F, 0.0F, 28, 14, 10), PartPose.offset(0.0F, 5.0F, -14.0F));
        root.addOrReplaceChild("head2", CubeListBuilder.create().texOffs(0, 87).mirror().addBox(-8.0F, 0.0F, 0.0F, 16, 8, 14), PartPose.offset(0.0F, 8.0F, -28.0F));
        root.addOrReplaceChild("larm", CubeListBuilder.create().texOffs(176, 176).mirror().addBox(0.0F, 0.0F, -5.0F, 5, 20, 10), PartPose.offsetAndRotation(16.0F, 5.0F, -3.0F, 0.0F, 0.0F, -0.1745329F));
        root.addOrReplaceChild("rarm", CubeListBuilder.create().texOffs(176, 176).mirror().addBox(-5.0F, 0.0F, -5.0F, 5, 20, 10), PartPose.offsetAndRotation(-16.0F, 5.0F, -3.0F, 0.0F, 0.0F, 0.1745329F));
        root.addOrReplaceChild("lleg", CubeListBuilder.create().texOffs(208, 176).mirror().addBox(-3.0F, 0.0F, -3.0F, 6, 15, 6), PartPose.offset(10.0F, 9.0F, 40.0F));
        root.addOrReplaceChild("rleg", CubeListBuilder.create().texOffs(208, 176).mirror().addBox(-3.0F, 0.0F, -3.0F, 6, 15, 6), PartPose.offset(-10.0F, 9.0F, 40.0F));
        root.addOrReplaceChild("butt", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-12.0F, 0.0F, 0.0F, 24, 16, 12), PartPose.offset(0.0F, 2.0F, 62.0F));
        root.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(0, 30).mirror().addBox(-3.0F, 0.0F, 0.0F, 6, 6, 20), PartPose.offsetAndRotation(0.0F, 3.0F, 74.0F, -0.2617994F, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, 256, 256);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float legAngle = limbSwingAmount > 0.1f ? Mth.cos(ageInTicks * 0.8f) * (float) Math.PI * 0.2f * limbSwingAmount : 0.0f;
        this.lleg.xRot = legAngle;
        this.rleg.xRot = -legAngle;
        this.larm.xRot = -legAngle * 0.5f;
        this.rarm.xRot = legAngle * 0.5f;
        this.head1.yRot = netHeadYaw * ((float) Math.PI / 180F) * 0.5f;
        this.head2.yRot = this.head1.yRot;
    }

    @Override
    public void renderToBuffer(PoseStack ps, VertexConsumer vc, int light, int overlay, int color) {
        body.render(ps, vc, light, overlay, color);
        shoulders.render(ps, vc, light, overlay, color);
        head1.render(ps, vc, light, overlay, color);
        head2.render(ps, vc, light, overlay, color);
        larm.render(ps, vc, light, overlay, color);
        rarm.render(ps, vc, light, overlay, color);
        lleg.render(ps, vc, light, overlay, color);
        rleg.render(ps, vc, light, overlay, color);
        butt.render(ps, vc, light, overlay, color);
        tail.render(ps, vc, light, overlay, color);
    }
}
