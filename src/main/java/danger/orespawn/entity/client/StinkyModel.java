package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.EntityStinky;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class StinkyModel<T extends EntityStinky> extends EntityModel<T> {
    private final ModelPart body, neck, head, snout;
    private final ModelPart rleg1, lleg1, rleg2, lleg2;
    private final ModelPart tail1, tail2, tail3, tail4;
    private final ModelPart lwing, rwing;
    private final ModelPart lhorn1, rhorn1;

    public StinkyModel(ModelPart root) {
        this.body = root.getChild("body");
        this.neck = root.getChild("neck");
        this.head = root.getChild("head");
        this.snout = root.getChild("snout");
        this.rleg1 = root.getChild("rleg1");
        this.lleg1 = root.getChild("lleg1");
        this.rleg2 = root.getChild("rleg2");
        this.lleg2 = root.getChild("lleg2");
        this.tail1 = root.getChild("tail1");
        this.tail2 = root.getChild("tail2");
        this.tail3 = root.getChild("tail3");
        this.tail4 = root.getChild("tail4");
        this.lwing = root.getChild("lwing");
        this.rwing = root.getChild("rwing");
        this.lhorn1 = root.getChild("lhorn1");
        this.rhorn1 = root.getChild("rhorn1");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 12).mirror().addBox(-4.5F, -3.0F, -5.0F, 8, 8, 10), PartPose.offset(0.5F, 15.0F, 1.0F));
        root.addOrReplaceChild("neck", CubeListBuilder.create().texOffs(0, 42).mirror().addBox(-2.0F, -8.0F, -3.0F, 4, 8, 4), PartPose.offset(0.0F, 15.0F, -5.5F));
        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-2.5F, -10.0F, -3.5F, 5, 5, 5), PartPose.offset(0.0F, 15.0F, -5.5F));
        root.addOrReplaceChild("snout", CubeListBuilder.create().texOffs(32, 57).mirror().addBox(-1.5F, -8.0F, -6.5F, 3, 3, 4), PartPose.offset(0.0F, 15.0F, -5.5F));
        root.addOrReplaceChild("rleg1", CubeListBuilder.create().texOffs(19, 53).mirror().addBox(-1.5F, 0.0F, -1.0F, 3, 8, 3), PartPose.offset(2.0F, 16.0F, 5.5F));
        root.addOrReplaceChild("lleg1", CubeListBuilder.create().texOffs(19, 53).mirror().addBox(-1.5F, 0.0F, -0.5F, 3, 8, 3), PartPose.offset(-2.0F, 16.0F, 5.0F));
        root.addOrReplaceChild("rleg2", CubeListBuilder.create().texOffs(19, 53).mirror().addBox(-1.5F, 0.0F, -1.5F, 3, 8, 3), PartPose.offset(2.0F, 16.0F, -3.0F));
        root.addOrReplaceChild("lleg2", CubeListBuilder.create().texOffs(19, 53).mirror().addBox(-1.5F, 0.0F, -1.5F, 3, 8, 3), PartPose.offset(-2.0F, 16.0F, -3.0F));
        root.addOrReplaceChild("tail1", CubeListBuilder.create().texOffs(47, 55).mirror().addBox(-3.0F, -3.0F, -3.0F, 6, 6, 3), PartPose.offset(0.0F, 16.5F, -2.0F));
        root.addOrReplaceChild("tail2", CubeListBuilder.create().texOffs(19, 31).mirror().addBox(-2.5F, -2.5F, 0.0F, 5, 5, 5), PartPose.offsetAndRotation(0.0F, 16.0F, 7.0F, -0.3839724F, 0.0F, 0.0F));
        root.addOrReplaceChild("tail3", CubeListBuilder.create().texOffs(32, 46).mirror().addBox(-2.0F, -2.0F, 0.0F, 4, 4, 4), PartPose.offsetAndRotation(0.0F, 17.2F, 11.0F, -0.2094395F, 0.0F, 0.0F));
        root.addOrReplaceChild("tail4", CubeListBuilder.create().texOffs(37, 13).mirror().addBox(-1.5F, -1.5F, 0.0F, 3, 3, 5), PartPose.offsetAndRotation(0.0F, 17.5F, 14.0F, -0.0698132F, 0.0F, 0.0F));
        root.addOrReplaceChild("lwing", CubeListBuilder.create().texOffs(59, 0).mirror().addBox(-18.0F, 0.0F, -5.0F, 18, 0, 10), PartPose.offsetAndRotation(-2.0F, 12.6F, 0.0F, 0.0F, 0.0F, 0.4014257F));
        root.addOrReplaceChild("rwing", CubeListBuilder.create().texOffs(59, 11).mirror().addBox(0.0F, 0.0F, -5.0F, 18, 0, 10), PartPose.offsetAndRotation(2.0F, 12.6F, 0.0F, 0.0F, 0.0F, -0.4014257F));
        root.addOrReplaceChild("lhorn1", CubeListBuilder.create().texOffs(19, 47).mirror().addBox(-3.0F, -10.5F, -1.0F, 2, 2, 3), PartPose.offset(0.0F, 15.0F, -5.5F));
        root.addOrReplaceChild("rhorn1", CubeListBuilder.create().texOffs(19, 47).mirror().addBox(1.0F, -10.5F, -1.0F, 2, 2, 3), PartPose.offset(0.0F, 15.0F, -5.5F));
        return LayerDefinition.create(mesh, 128, 64);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float wingAngle = limbSwingAmount > 0.1f ? Mth.cos(ageInTicks * 2.3f) * (float) Math.PI * 0.4f * limbSwingAmount : 0.0f;
        this.rwing.zRot = wingAngle - 0.4f;
        this.lwing.zRot = -wingAngle + 0.4f;
        float legAngle = limbSwingAmount > 0.1f ? Mth.cos(ageInTicks * 2.0f) * (float) Math.PI * 0.25f * limbSwingAmount : 0.0f;
        this.rleg1.xRot = legAngle;
        this.lleg1.xRot = -legAngle;
        this.rleg2.xRot = -legAngle;
        this.lleg2.xRot = legAngle;
        this.head.yRot = (float) Math.toRadians(netHeadYaw);
        this.snout.yRot = this.head.yRot;
        this.neck.yRot = this.head.yRot / 2.0f;
        this.lhorn1.yRot = this.head.yRot;
        this.rhorn1.yRot = this.head.yRot;
        float tailAngle = Mth.cos(ageInTicks * 1.0f) * (float) Math.PI * 0.2f;
        this.tail2.yRot = tailAngle;
    }

    @Override
    public void renderToBuffer(PoseStack ps, VertexConsumer vc, int light, int overlay, int color) {
        body.render(ps, vc, light, overlay, color);
        neck.render(ps, vc, light, overlay, color);
        head.render(ps, vc, light, overlay, color);
        snout.render(ps, vc, light, overlay, color);
        rleg1.render(ps, vc, light, overlay, color);
        lleg1.render(ps, vc, light, overlay, color);
        rleg2.render(ps, vc, light, overlay, color);
        lleg2.render(ps, vc, light, overlay, color);
        tail1.render(ps, vc, light, overlay, color);
        tail2.render(ps, vc, light, overlay, color);
        tail3.render(ps, vc, light, overlay, color);
        tail4.render(ps, vc, light, overlay, color);
        lwing.render(ps, vc, light, overlay, color);
        rwing.render(ps, vc, light, overlay, color);
        lhorn1.render(ps, vc, light, overlay, color);
        rhorn1.render(ps, vc, light, overlay, color);
    }
}
