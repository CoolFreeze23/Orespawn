package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.EntityVortex;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class VortexModel<T extends EntityVortex> extends EntityModel<T> {
    private final ModelPart bottom;
    private final ModelPart middle;
    private final ModelPart top;

    public VortexModel(ModelPart root) {
        this.bottom = root.getChild("bottom");
        this.middle = root.getChild("middle");
        this.top = root.getChild("top");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("bottom", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, 0.0F, -8.0F, 16, 10, 16), PartPose.offset(0.0F, 14.0F, 0.0F));
        root.addOrReplaceChild("middle", CubeListBuilder.create().texOffs(0, 27).addBox(-6.0F, 0.0F, -6.0F, 12, 8, 12), PartPose.offset(0.0F, 6.0F, 0.0F));
        root.addOrReplaceChild("top", CubeListBuilder.create().texOffs(0, 48).addBox(-4.0F, 0.0F, -4.0F, 8, 6, 8), PartPose.offset(0.0F, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, 128, 64);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.bottom.yRot = ageInTicks * 0.1f;
        this.middle.yRot = -ageInTicks * 0.15f;
        this.top.yRot = ageInTicks * 0.2f;
    }

    @Override
    public void renderToBuffer(PoseStack ps, VertexConsumer vc, int light, int overlay, int color) {
        bottom.render(ps, vc, light, overlay, color);
        middle.render(ps, vc, light, overlay, color);
        top.render(ps, vc, light, overlay, color);
    }
}
