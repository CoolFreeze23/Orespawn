package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.EntityTriffid;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class TriffidModel<T extends EntityTriffid> extends EntityModel<T> {
    private final ModelPart base, stalk, head, jaw;

    public TriffidModel(ModelPart root) {
        this.base = root.getChild("base");
        this.stalk = root.getChild("stalk");
        this.head = root.getChild("head");
        this.jaw = root.getChild("jaw");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("base", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, 0.0F, -6.0F, 12, 8, 12), PartPose.offset(0.0F, 16.0F, 0.0F));
        root.addOrReplaceChild("stalk", CubeListBuilder.create().texOffs(0, 21).addBox(-3.0F, -20.0F, -3.0F, 6, 20, 6), PartPose.offset(0.0F, 16.0F, 0.0F));
        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 48).addBox(-5.0F, -8.0F, -5.0F, 10, 8, 10), PartPose.offset(0.0F, -4.0F, 0.0F));
        root.addOrReplaceChild("jaw", CubeListBuilder.create().texOffs(0, 67).addBox(-4.0F, 0.0F, -6.0F, 8, 3, 6), PartPose.offset(0.0F, -4.0F, 0.0F));
        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
        this.jaw.yRot = this.head.yRot;
    }

    @Override
    public void renderToBuffer(PoseStack ps, VertexConsumer vc, int light, int overlay, int color) {
        base.render(ps, vc, light, overlay, color);
        stalk.render(ps, vc, light, overlay, color);
        head.render(ps, vc, light, overlay, color);
        jaw.render(ps, vc, light, overlay, color);
    }
}
