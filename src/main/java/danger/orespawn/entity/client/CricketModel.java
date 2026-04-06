package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.EntityCricket;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class CricketModel<T extends EntityCricket> extends EntityModel<T> {
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart leg1;
    private final ModelPart leg2;
    private final ModelPart leg3;
    private final ModelPart leg4;
    private final ModelPart legBackLeft;
    private final ModelPart legBackRight;
    private final ModelPart antennaLeft;
    private final ModelPart antennaRight;

    public CricketModel(ModelPart root) {
        this.body = root.getChild("body");
        this.head = root.getChild("head");
        this.leg1 = root.getChild("leg1");
        this.leg2 = root.getChild("leg2");
        this.leg3 = root.getChild("leg3");
        this.leg4 = root.getChild("leg4");
        this.legBackLeft = root.getChild("leg_back_left");
        this.legBackRight = root.getChild("leg_back_right");
        this.antennaLeft = root.getChild("antenna_left");
        this.antennaRight = root.getChild("antenna_right");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-1.0F, -1.0F, -2.0F, 2, 2, 4), PartPose.offset(0.0F, 22.5F, 0.0F));
        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 6)
                .addBox(-1.0F, -1.0F, -1.5F, 2, 2, 2), PartPose.offset(0.0F, 22.0F, -2.0F));

        root.addOrReplaceChild("leg1", CubeListBuilder.create().texOffs(0, 10)
                .addBox(-0.5F, 0.0F, -0.5F, 1, 1, 1), PartPose.offset(-1.0F, 23.5F, -1.0F));
        root.addOrReplaceChild("leg2", CubeListBuilder.create().texOffs(0, 10)
                .addBox(-0.5F, 0.0F, -0.5F, 1, 1, 1), PartPose.offset(1.0F, 23.5F, -1.0F));
        root.addOrReplaceChild("leg3", CubeListBuilder.create().texOffs(0, 10)
                .addBox(-0.5F, 0.0F, -0.5F, 1, 1, 1), PartPose.offset(-1.0F, 23.5F, 0.5F));
        root.addOrReplaceChild("leg4", CubeListBuilder.create().texOffs(0, 10)
                .addBox(-0.5F, 0.0F, -0.5F, 1, 1, 1), PartPose.offset(1.0F, 23.5F, 0.5F));

        root.addOrReplaceChild("leg_back_left", CubeListBuilder.create().texOffs(4, 10)
                .addBox(-0.5F, 0.0F, -0.5F, 1, 2, 1), PartPose.offset(-1.0F, 23.0F, 1.5F));
        root.addOrReplaceChild("leg_back_right", CubeListBuilder.create().texOffs(4, 10)
                .addBox(-0.5F, 0.0F, -0.5F, 1, 2, 1), PartPose.offset(1.0F, 23.0F, 1.5F));

        root.addOrReplaceChild("antenna_left", CubeListBuilder.create().texOffs(8, 10)
                .addBox(-0.5F, -3.0F, 0.0F, 1, 3, 0), PartPose.offset(-0.5F, 21.0F, -3.5F));
        root.addOrReplaceChild("antenna_right", CubeListBuilder.create().texOffs(10, 10)
                .addBox(-0.5F, -3.0F, 0.0F, 1, 3, 0), PartPose.offset(0.5F, 21.0F, -3.5F));

        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
        this.head.xRot = headPitch * ((float) Math.PI / 180F);

        float legSwing = Mth.cos(limbSwing * 0.6662F) * 0.7F * limbSwingAmount;
        this.leg1.xRot = legSwing;
        this.leg2.xRot = -legSwing;
        this.leg3.xRot = -legSwing;
        this.leg4.xRot = legSwing;
        this.legBackLeft.xRot = legSwing * 1.2F;
        this.legBackRight.xRot = -legSwing * 1.2F;

        float antennaWave = Mth.cos(ageInTicks * 0.1F) * 0.15F;
        this.antennaLeft.zRot = -0.2F + antennaWave;
        this.antennaRight.zRot = 0.2F - antennaWave;
    }

    @Override
    public void renderToBuffer(PoseStack ps, VertexConsumer vc, int light, int overlay, int color) {
        body.render(ps, vc, light, overlay, color);
        head.render(ps, vc, light, overlay, color);
        leg1.render(ps, vc, light, overlay, color);
        leg2.render(ps, vc, light, overlay, color);
        leg3.render(ps, vc, light, overlay, color);
        leg4.render(ps, vc, light, overlay, color);
        legBackLeft.render(ps, vc, light, overlay, color);
        legBackRight.render(ps, vc, light, overlay, color);
        antennaLeft.render(ps, vc, light, overlay, color);
        antennaRight.render(ps, vc, light, overlay, color);
    }
}
