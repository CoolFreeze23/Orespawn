package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.EntityTrooperBug;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class TrooperBugModel<T extends EntityTrooperBug> extends EntityModel<T> {
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart armorTop;
    private final ModelPart leg1;
    private final ModelPart leg2;
    private final ModelPart leg3;
    private final ModelPart leg4;
    private final ModelPart leg5;
    private final ModelPart leg6;
    private final ModelPart mandibleLeft;
    private final ModelPart mandibleRight;

    public TrooperBugModel(ModelPart root) {
        this.body = root.getChild("body");
        this.head = root.getChild("head");
        this.armorTop = root.getChild("armor_top");
        this.leg1 = root.getChild("leg1");
        this.leg2 = root.getChild("leg2");
        this.leg3 = root.getChild("leg3");
        this.leg4 = root.getChild("leg4");
        this.leg5 = root.getChild("leg5");
        this.leg6 = root.getChild("leg6");
        this.mandibleLeft = root.getChild("mandible_left");
        this.mandibleRight = root.getChild("mandible_right");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-12.0F, -6.0F, -14.0F, 24, 12, 28), PartPose.offset(0.0F, -2.0F, 0.0F));
        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 40)
                .addBox(-7.0F, -6.0F, -10.0F, 14, 12, 10), PartPose.offset(0.0F, -2.0F, -14.0F));
        root.addOrReplaceChild("armor_top", CubeListBuilder.create().texOffs(0, 62)
                .addBox(-14.0F, -3.0F, -16.0F, 28, 3, 32), PartPose.offset(0.0F, -8.0F, 0.0F));

        root.addOrReplaceChild("leg1", CubeListBuilder.create().texOffs(0, 97)
                .addBox(-2.0F, 0.0F, -2.0F, 4, 20, 4), PartPose.offset(-10.0F, 4.0F, -10.0F));
        root.addOrReplaceChild("leg2", CubeListBuilder.create().texOffs(0, 97)
                .addBox(-2.0F, 0.0F, -2.0F, 4, 20, 4), PartPose.offset(10.0F, 4.0F, -10.0F));
        root.addOrReplaceChild("leg3", CubeListBuilder.create().texOffs(0, 97)
                .addBox(-2.0F, 0.0F, -2.0F, 4, 20, 4), PartPose.offset(-10.0F, 4.0F, 0.0F));
        root.addOrReplaceChild("leg4", CubeListBuilder.create().texOffs(0, 97)
                .addBox(-2.0F, 0.0F, -2.0F, 4, 20, 4), PartPose.offset(10.0F, 4.0F, 0.0F));
        root.addOrReplaceChild("leg5", CubeListBuilder.create().texOffs(0, 97)
                .addBox(-2.0F, 0.0F, -2.0F, 4, 20, 4), PartPose.offset(-10.0F, 4.0F, 10.0F));
        root.addOrReplaceChild("leg6", CubeListBuilder.create().texOffs(0, 97)
                .addBox(-2.0F, 0.0F, -2.0F, 4, 20, 4), PartPose.offset(10.0F, 4.0F, 10.0F));

        root.addOrReplaceChild("mandible_left", CubeListBuilder.create().texOffs(48, 40)
                .addBox(-1.5F, -1.5F, -10.0F, 3, 3, 10), PartPose.offset(-4.0F, -1.0F, -24.0F));
        root.addOrReplaceChild("mandible_right", CubeListBuilder.create().texOffs(48, 40)
                .addBox(-1.5F, -1.5F, -10.0F, 3, 3, 10), PartPose.offset(4.0F, -1.0F, -24.0F));

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

        float mandibleAnim = Mth.cos(ageInTicks * 0.12F) * 0.15F;
        this.mandibleLeft.yRot = -0.25F + mandibleAnim;
        this.mandibleRight.yRot = 0.25F - mandibleAnim;
    }

    @Override
    public void renderToBuffer(PoseStack ps, VertexConsumer vc, int light, int overlay, int color) {
        body.render(ps, vc, light, overlay, color);
        head.render(ps, vc, light, overlay, color);
        armorTop.render(ps, vc, light, overlay, color);
        leg1.render(ps, vc, light, overlay, color);
        leg2.render(ps, vc, light, overlay, color);
        leg3.render(ps, vc, light, overlay, color);
        leg4.render(ps, vc, light, overlay, color);
        leg5.render(ps, vc, light, overlay, color);
        leg6.render(ps, vc, light, overlay, color);
        mandibleLeft.render(ps, vc, light, overlay, color);
        mandibleRight.render(ps, vc, light, overlay, color);
    }
}
