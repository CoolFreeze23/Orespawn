package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.DungeonBeast;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class ModelDungeonBeast extends EntityModel<DungeonBeast> {
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart leg1;
    private final ModelPart leg2;
    private final ModelPart leg3;
    private final ModelPart leg4;
    private final ModelPart tail;

    public ModelDungeonBeast(ModelPart root) {
        this.body = root.getChild("body");
        this.head = root.getChild("head");
        this.leg1 = root.getChild("leg1");
        this.leg2 = root.getChild("leg2");
        this.leg3 = root.getChild("leg3");
        this.leg4 = root.getChild("leg4");
        this.tail = root.getChild("tail");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -3.0F, -5.0F, 8, 6, 10), PartPose.offset(0.0F, 19.0F, 0.0F));
        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 16).addBox(-3.0F, -3.0F, -4.0F, 6, 5, 4), PartPose.offset(0.0F, 19.0F, -5.0F));
        root.addOrReplaceChild("leg1", CubeListBuilder.create().texOffs(28, 0).addBox(-1.0F, 0.0F, -1.0F, 2, 5, 2), PartPose.offset(-3.0F, 22.0F, -3.0F));
        root.addOrReplaceChild("leg2", CubeListBuilder.create().texOffs(28, 0).addBox(-1.0F, 0.0F, -1.0F, 2, 5, 2), PartPose.offset(3.0F, 22.0F, -3.0F));
        root.addOrReplaceChild("leg3", CubeListBuilder.create().texOffs(28, 0).addBox(-1.0F, 0.0F, -1.0F, 2, 5, 2), PartPose.offset(-3.0F, 22.0F, 3.0F));
        root.addOrReplaceChild("leg4", CubeListBuilder.create().texOffs(28, 0).addBox(-1.0F, 0.0F, -1.0F, 2, 5, 2), PartPose.offset(3.0F, 22.0F, 3.0F));
        root.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(20, 16).addBox(-1.0F, -1.0F, 0.0F, 2, 2, 6), PartPose.offset(0.0F, 19.0F, 5.0F));
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void setupAnim(DungeonBeast entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
        this.head.xRot = headPitch * ((float) Math.PI / 180F);
        float swing = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        this.leg1.xRot = swing;
        this.leg2.xRot = -swing;
        this.leg3.xRot = -swing;
        this.leg4.xRot = swing;
        this.tail.yRot = Mth.cos(ageInTicks * 0.2F) * 0.4F;
    }

    @Override
    public void renderToBuffer(PoseStack ps, VertexConsumer vc, int light, int overlay, int color) {
        this.body.render(ps, vc, light, overlay, color);
        this.head.render(ps, vc, light, overlay, color);
        this.leg1.render(ps, vc, light, overlay, color);
        this.leg2.render(ps, vc, light, overlay, color);
        this.leg3.render(ps, vc, light, overlay, color);
        this.leg4.render(ps, vc, light, overlay, color);
        this.tail.render(ps, vc, light, overlay, color);
    }
}
