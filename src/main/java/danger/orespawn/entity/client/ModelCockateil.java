package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.Cockateil;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class ModelCockateil extends EntityModel<Cockateil> {
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart wingRight;
    private final ModelPart wingLeft;
    private final ModelPart tail;
    private final ModelPart legRight;
    private final ModelPart legLeft;
    private final ModelPart crest;

    public ModelCockateil(ModelPart root) {
        this.head = root.getChild("head");
        this.body = root.getChild("body");
        this.wingRight = root.getChild("wing_right");
        this.wingLeft = root.getChild("wing_left");
        this.tail = root.getChild("tail");
        this.legRight = root.getChild("leg_right");
        this.legLeft = root.getChild("leg_left");
        this.crest = root.getChild("crest");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, -2.0F, 4, 4, 4), PartPose.offset(0.0F, 18.0F, -2.0F));
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 8).addBox(-2.0F, 0.0F, -1.5F, 4, 4, 3), PartPose.offset(0.0F, 18.0F, 0.0F));
        root.addOrReplaceChild("wing_right", CubeListBuilder.create().texOffs(14, 8).addBox(-1.0F, 0.0F, -1.0F, 1, 3, 4), PartPose.offset(-2.0F, 18.0F, 0.0F));
        root.addOrReplaceChild("wing_left", CubeListBuilder.create().texOffs(14, 8).mirror().addBox(0.0F, 0.0F, -1.0F, 1, 3, 4), PartPose.offset(2.0F, 18.0F, 0.0F));
        root.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(24, 0).addBox(-1.0F, 0.0F, 0.0F, 2, 1, 4), PartPose.offsetAndRotation(0.0F, 21.0F, 1.5F, 0.4363F, 0.0F, 0.0F));
        root.addOrReplaceChild("leg_right", CubeListBuilder.create().texOffs(24, 5).addBox(-0.5F, 0.0F, -0.5F, 1, 2, 1), PartPose.offset(-1.0F, 22.0F, 0.0F));
        root.addOrReplaceChild("leg_left", CubeListBuilder.create().texOffs(24, 5).mirror().addBox(-0.5F, 0.0F, -0.5F, 1, 2, 1), PartPose.offset(1.0F, 22.0F, 0.0F));
        root.addOrReplaceChild("crest", CubeListBuilder.create().texOffs(16, 0).addBox(-0.5F, -6.0F, -1.0F, 1, 3, 1), PartPose.offset(0.0F, 18.0F, -2.0F));
        return LayerDefinition.create(mesh, 32, 16);
    }

    @Override
    public void setupAnim(Cockateil entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
        this.head.xRot = headPitch * ((float) Math.PI / 180F);
        this.crest.yRot = this.head.yRot;
        this.crest.xRot = this.head.xRot;
        this.wingRight.zRot = Mth.cos(ageInTicks * 1.2F) * (float) Math.PI * 0.25F;
        this.wingLeft.zRot = -this.wingRight.zRot;
    }

    @Override
    public void renderToBuffer(PoseStack ps, VertexConsumer vc, int light, int overlay, int color) {
        this.head.render(ps, vc, light, overlay, color);
        this.body.render(ps, vc, light, overlay, color);
        this.wingRight.render(ps, vc, light, overlay, color);
        this.wingLeft.render(ps, vc, light, overlay, color);
        this.tail.render(ps, vc, light, overlay, color);
        this.legRight.render(ps, vc, light, overlay, color);
        this.legLeft.render(ps, vc, light, overlay, color);
        this.crest.render(ps, vc, light, overlay, color);
    }
}
