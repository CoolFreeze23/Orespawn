package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.Irukandji;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class ModelIrukandji extends EntityModel<Irukandji> {
    private final ModelPart body;
    private final ModelPart tentacle1;
    private final ModelPart tentacle2;

    public ModelIrukandji(ModelPart root) {
        this.body = root.getChild("body");
        this.tentacle1 = root.getChild("tentacle1");
        this.tentacle2 = root.getChild("tentacle2");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition part = mesh.getRoot();
        part.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, -2.0F, -1.5F, 3, 3, 3), PartPose.offset(0.0F, 22.0F, 0.0F));
        part.addOrReplaceChild("tentacle1", CubeListBuilder.create().texOffs(12, 0).addBox(-0.5F, 0.0F, -0.5F, 1, 3, 1), PartPose.offset(-0.5F, 23.0F, 0.0F));
        part.addOrReplaceChild("tentacle2", CubeListBuilder.create().texOffs(12, 0).addBox(-0.5F, 0.0F, -0.5F, 1, 3, 1), PartPose.offset(0.5F, 23.0F, 0.0F));
        return LayerDefinition.create(mesh, 32, 16);
    }

    @Override
    public void setupAnim(Irukandji entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float swing = Mth.cos(ageInTicks * 0.4F) * 0.2F;
        this.tentacle1.xRot = swing;
        this.tentacle2.xRot = -swing;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        this.body.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.tentacle1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.tentacle2.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
