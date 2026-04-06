package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.CreepingHorror;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class ModelCreepingHorror extends EntityModel<CreepingHorror> {
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart leg1;
    private final ModelPart leg2;
    private final ModelPart leg3;
    private final ModelPart leg4;

    public ModelCreepingHorror(ModelPart root) {
        this.body = root.getChild("body");
        this.head = root.getChild("head");
        this.leg1 = root.getChild("leg1");
        this.leg2 = root.getChild("leg2");
        this.leg3 = root.getChild("leg3");
        this.leg4 = root.getChild("leg4");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -2.0F, -4.0F, 6, 4, 8), PartPose.offset(0.0F, 21.0F, 0.0F));
        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 12).addBox(-2.0F, -2.0F, -3.0F, 4, 3, 3), PartPose.offset(0.0F, 21.0F, -4.0F));
        root.addOrReplaceChild("leg1", CubeListBuilder.create().texOffs(20, 0).addBox(-0.5F, 0.0F, -0.5F, 1, 3, 1), PartPose.offset(-2.0F, 23.0F, -3.0F));
        root.addOrReplaceChild("leg2", CubeListBuilder.create().texOffs(20, 0).addBox(-0.5F, 0.0F, -0.5F, 1, 3, 1), PartPose.offset(2.0F, 23.0F, -3.0F));
        root.addOrReplaceChild("leg3", CubeListBuilder.create().texOffs(20, 0).addBox(-0.5F, 0.0F, -0.5F, 1, 3, 1), PartPose.offset(-2.0F, 23.0F, 3.0F));
        root.addOrReplaceChild("leg4", CubeListBuilder.create().texOffs(20, 0).addBox(-0.5F, 0.0F, -0.5F, 1, 3, 1), PartPose.offset(2.0F, 23.0F, 3.0F));
        return LayerDefinition.create(mesh, 32, 32);
    }

    @Override
    public void setupAnim(CreepingHorror entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
        float swing = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        this.leg1.xRot = swing;
        this.leg2.xRot = -swing;
        this.leg3.xRot = -swing;
        this.leg4.xRot = swing;
    }

    @Override
    public void renderToBuffer(PoseStack ps, VertexConsumer vc, int light, int overlay, int color) {
        this.body.render(ps, vc, light, overlay, color);
        this.head.render(ps, vc, light, overlay, color);
        this.leg1.render(ps, vc, light, overlay, color);
        this.leg2.render(ps, vc, light, overlay, color);
        this.leg3.render(ps, vc, light, overlay, color);
        this.leg4.render(ps, vc, light, overlay, color);
    }
}
