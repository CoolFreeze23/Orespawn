package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.SeaMonster;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class ModelSeaMonster extends EntityModel<SeaMonster> {
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart tail;
    private final ModelPart finLeft;
    private final ModelPart finRight;

    public ModelSeaMonster(ModelPart root) {
        this.body = root.getChild("body");
        this.head = root.getChild("head");
        this.tail = root.getChild("tail");
        this.finLeft = root.getChild("fin_left");
        this.finRight = root.getChild("fin_right");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition part = mesh.getRoot();
        part.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -6.0F, -12.0F, 16, 12, 24), PartPose.offset(0.0F, 14.0F, 0.0F));
        part.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 36).addBox(-6.0F, -4.0F, -8.0F, 12, 8, 8), PartPose.offset(0.0F, 14.0F, -12.0F));
        part.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(56, 0).addBox(-3.0F, -4.0F, 0.0F, 6, 8, 12), PartPose.offset(0.0F, 14.0F, 12.0F));
        part.addOrReplaceChild("fin_left", CubeListBuilder.create().texOffs(0, 52).addBox(0.0F, 0.0F, -4.0F, 10, 1, 6), PartPose.offset(8.0F, 18.0F, 0.0F));
        part.addOrReplaceChild("fin_right", CubeListBuilder.create().texOffs(0, 52).mirror().addBox(-10.0F, 0.0F, -4.0F, 10, 1, 6), PartPose.offset(-8.0F, 18.0F, 0.0F));
        return LayerDefinition.create(mesh, 128, 64);
    }

    @Override
    public void setupAnim(SeaMonster entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.tail.yRot = Mth.cos(ageInTicks * 0.15F) * 0.3F;
        this.finLeft.zRot = Mth.cos(ageInTicks * 0.2F) * 0.15F;
        this.finRight.zRot = -Mth.cos(ageInTicks * 0.2F) * 0.15F;
        this.head.xRot = headPitch * ((float) Math.PI / 180F);
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        this.body.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.head.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.tail.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.finLeft.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.finRight.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
