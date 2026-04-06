package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.EntityAnt;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class AntModel<T extends EntityAnt> extends EntityModel<T> {
    private final ModelPart head;
    private final ModelPart thorax;
    private final ModelPart abdomen;
    private final ModelPart leg1;
    private final ModelPart leg2;
    private final ModelPart leg3;
    private final ModelPart leg4;
    private final ModelPart leg5;
    private final ModelPart leg6;
    private final ModelPart antennaLeft;
    private final ModelPart antennaRight;

    public AntModel(ModelPart root) {
        this.head = root.getChild("head");
        this.thorax = root.getChild("thorax");
        this.abdomen = root.getChild("abdomen");
        this.leg1 = root.getChild("leg1");
        this.leg2 = root.getChild("leg2");
        this.leg3 = root.getChild("leg3");
        this.leg4 = root.getChild("leg4");
        this.leg5 = root.getChild("leg5");
        this.leg6 = root.getChild("leg6");
        this.antennaLeft = root.getChild("antenna_left");
        this.antennaRight = root.getChild("antenna_right");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-1.0F, -1.0F, -2.0F, 2, 2, 2), PartPose.offset(0.0F, 22.5F, -2.0F));
        root.addOrReplaceChild("thorax", CubeListBuilder.create().texOffs(0, 4)
                .addBox(-0.5F, -0.5F, -1.0F, 1, 1, 2), PartPose.offset(0.0F, 22.5F, 0.0F));
        root.addOrReplaceChild("abdomen", CubeListBuilder.create().texOffs(0, 7)
                .addBox(-1.0F, -1.0F, 0.0F, 2, 2, 3), PartPose.offset(0.0F, 22.5F, 1.0F));

        root.addOrReplaceChild("leg1", CubeListBuilder.create().texOffs(0, 12)
                .addBox(-0.5F, 0.0F, 0.0F, 1, 1, 0), PartPose.offset(-0.5F, 23.0F, -0.5F));
        root.addOrReplaceChild("leg2", CubeListBuilder.create().texOffs(0, 12)
                .addBox(-0.5F, 0.0F, 0.0F, 1, 1, 0), PartPose.offset(0.5F, 23.0F, -0.5F));
        root.addOrReplaceChild("leg3", CubeListBuilder.create().texOffs(0, 12)
                .addBox(-0.5F, 0.0F, 0.0F, 1, 1, 0), PartPose.offset(-0.5F, 23.0F, 0.0F));
        root.addOrReplaceChild("leg4", CubeListBuilder.create().texOffs(0, 12)
                .addBox(-0.5F, 0.0F, 0.0F, 1, 1, 0), PartPose.offset(0.5F, 23.0F, 0.0F));
        root.addOrReplaceChild("leg5", CubeListBuilder.create().texOffs(0, 12)
                .addBox(-0.5F, 0.0F, 0.0F, 1, 1, 0), PartPose.offset(-0.5F, 23.0F, 0.5F));
        root.addOrReplaceChild("leg6", CubeListBuilder.create().texOffs(0, 12)
                .addBox(-0.5F, 0.0F, 0.0F, 1, 1, 0), PartPose.offset(0.5F, 23.0F, 0.5F));

        root.addOrReplaceChild("antenna_left", CubeListBuilder.create().texOffs(8, 0)
                .addBox(-0.5F, -2.0F, 0.0F, 1, 2, 0), PartPose.offset(-0.5F, 21.5F, -4.0F));
        root.addOrReplaceChild("antenna_right", CubeListBuilder.create().texOffs(10, 0)
                .addBox(-0.5F, -2.0F, 0.0F, 1, 2, 0), PartPose.offset(0.5F, 21.5F, -4.0F));

        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);

        float legSwing = Mth.cos(limbSwing * 0.6662F) * 0.8F * limbSwingAmount;
        this.leg1.xRot = legSwing;
        this.leg2.xRot = -legSwing;
        this.leg3.xRot = -legSwing;
        this.leg4.xRot = legSwing;
        this.leg5.xRot = legSwing;
        this.leg6.xRot = -legSwing;

        float antennaWave = Mth.cos(ageInTicks * 0.15F) * 0.2F;
        this.antennaLeft.zRot = -0.3F + antennaWave;
        this.antennaRight.zRot = 0.3F - antennaWave;
    }

    @Override
    public void renderToBuffer(PoseStack ps, VertexConsumer vc, int light, int overlay, int color) {
        head.render(ps, vc, light, overlay, color);
        thorax.render(ps, vc, light, overlay, color);
        abdomen.render(ps, vc, light, overlay, color);
        leg1.render(ps, vc, light, overlay, color);
        leg2.render(ps, vc, light, overlay, color);
        leg3.render(ps, vc, light, overlay, color);
        leg4.render(ps, vc, light, overlay, color);
        leg5.render(ps, vc, light, overlay, color);
        leg6.render(ps, vc, light, overlay, color);
        antennaLeft.render(ps, vc, light, overlay, color);
        antennaRight.render(ps, vc, light, overlay, color);
    }
}
