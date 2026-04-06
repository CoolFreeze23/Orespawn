package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.CloudShark;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class ModelCloudShark extends EntityModel<CloudShark> {
    private final ModelPart body;
    private final ModelPart tail;
    private final ModelPart finLeft;
    private final ModelPart finRight;

    public ModelCloudShark(ModelPart root) {
        this.body = root.getChild("body");
        this.tail = root.getChild("tail");
        this.finLeft = root.getChild("fin_left");
        this.finRight = root.getChild("fin_right");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition part = mesh.getRoot();
        part.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -3.0F, -6.0F, 6, 5, 12), PartPose.offset(0.0F, 20.0F, 0.0F));
        part.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(24, 0).addBox(-1.0F, -2.0F, 0.0F, 2, 4, 4), PartPose.offset(0.0F, 20.0F, 6.0F));
        part.addOrReplaceChild("fin_left", CubeListBuilder.create().texOffs(0, 17).addBox(0.0F, 0.0F, -2.0F, 5, 1, 3), PartPose.offset(3.0F, 21.0F, -1.0F));
        part.addOrReplaceChild("fin_right", CubeListBuilder.create().texOffs(0, 17).mirror().addBox(-5.0F, 0.0F, -2.0F, 5, 1, 3), PartPose.offset(-3.0F, 21.0F, -1.0F));
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void setupAnim(CloudShark entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.tail.yRot = Mth.cos(ageInTicks * 0.4F) * 0.3F;
        this.finLeft.zRot = Mth.cos(ageInTicks * 0.3F) * 0.15F;
        this.finRight.zRot = -Mth.cos(ageInTicks * 0.3F) * 0.15F;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        this.body.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.tail.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.finLeft.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.finRight.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
