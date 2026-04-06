package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.EntityTshirt;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class TshirtModel<T extends EntityTshirt> extends EntityModel<T> {
    private final ModelPart body;
    private final ModelPart larm;
    private final ModelPart rarm;

    public TshirtModel(ModelPart root) {
        this.body = root.getChild("body");
        this.larm = root.getChild("larm");
        this.rarm = root.getChild("rarm");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -16.0F, -4.0F, 16, 24, 8), PartPose.offset(0.0F, 12.0F, 0.0F));
        root.addOrReplaceChild("larm", CubeListBuilder.create().texOffs(0, 33).addBox(0.0F, -2.0F, -3.0F, 6, 16, 6), PartPose.offset(8.0F, -2.0F, 0.0F));
        root.addOrReplaceChild("rarm", CubeListBuilder.create().texOffs(25, 33).addBox(-6.0F, -2.0F, -3.0F, 6, 16, 6), PartPose.offset(-8.0F, -2.0F, 0.0F));
        return LayerDefinition.create(mesh, 320, 160);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float newangle = 0.0f;
        this.larm.yRot = newangle = Mth.cos((float)(ageInTicks * 0.05f * limbSwingAmount)) * (float)Math.PI;
        this.rarm.yRot = newangle;
    }

    @Override
    public void renderToBuffer(PoseStack ps, VertexConsumer vc, int light, int overlay, int color) {
        body.render(ps, vc, light, overlay, color);
        larm.render(ps, vc, light, overlay, color);
        rarm.render(ps, vc, light, overlay, color);
    }
}
