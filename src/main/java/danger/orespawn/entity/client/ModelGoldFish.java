package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.GoldFish;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class ModelGoldFish extends EntityModel<GoldFish> {
    private final ModelPart body;
    private final ModelPart tail;
    private final ModelPart finTop;

    public ModelGoldFish(ModelPart root) {
        this.body = root.getChild("body");
        this.tail = root.getChild("tail");
        this.finTop = root.getChild("fin_top");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition part = mesh.getRoot();
        part.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -2.0F, -3.0F, 4, 4, 6), PartPose.offset(0.0F, 21.0F, 0.0F));
        part.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(20, 0).addBox(-1.0F, -2.0F, 0.0F, 2, 4, 3), PartPose.offset(0.0F, 21.0F, 3.0F));
        part.addOrReplaceChild("fin_top", CubeListBuilder.create().texOffs(14, 0).addBox(-0.5F, -2.0F, -1.0F, 1, 2, 3), PartPose.offset(0.0F, 19.0F, 0.0F));
        return LayerDefinition.create(mesh, 32, 16);
    }

    @Override
    public void setupAnim(GoldFish entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.tail.yRot = Mth.cos(ageInTicks * 0.5F) * 0.4F;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        this.body.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.tail.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.finTop.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
