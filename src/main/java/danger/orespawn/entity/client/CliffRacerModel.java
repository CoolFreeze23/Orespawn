package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.EntityCliffRacer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class CliffRacerModel<T extends EntityCliffRacer> extends EntityModel<T> {
    private final ModelPart body;
    private final ModelPart fins;
    private final ModelPart lWing;
    private final ModelPart rWing;
    private final ModelPart tail;
    private final ModelPart tailEnd;
    private final ModelPart head;
    private final ModelPart beak;

    public CliffRacerModel(ModelPart root) {
        this.body = root.getChild("body");
        this.fins = root.getChild("fins");
        this.lWing = root.getChild("lwing");
        this.rWing = root.getChild("rwing");
        this.tail = root.getChild("tail");
        this.tailEnd = root.getChild("tailend");
        this.head = root.getChild("head");
        this.beak = root.getChild("beak");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 52).addBox(0.0F, 0.0F, 0.0F, 3, 1, 10), PartPose.offset(-1.0F, 15.0F, -4.0F));
        root.addOrReplaceChild("fins", CubeListBuilder.create().texOffs(0, 40).addBox(0.0F, -4.0F, 0.0F, 1, 6, 3), PartPose.offset(0.0F, 15.0F, -1.0F));
        root.addOrReplaceChild("lwing", CubeListBuilder.create().texOffs(0, 31).addBox(0.0F, 0.0F, 0.0F, 7, 1, 6), PartPose.offset(2.0F, 15.0F, -2.0F));
        root.addOrReplaceChild("rwing", CubeListBuilder.create().texOffs(39, 0).addBox(-7.0F, 0.0F, 0.0F, 7, 1, 6), PartPose.offset(-1.0F, 15.0F, -2.0F));
        root.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(0, 16).addBox(0.0F, 0.0F, 0.0F, 1, 1, 9), PartPose.offset(0.0F, 15.0F, 6.0F));
        root.addOrReplaceChild("tailend", CubeListBuilder.create().texOffs(0, 10).addBox(0.0F, -1.0F, 9.0F, 2, 2, 2), PartPose.offset(-0.5F, 15.0F, 6.0F));
        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(28, 21).addBox(0.0F, 0.0F, 0.0F, 2, 2, 2), PartPose.offset(-0.5F, 14.0F, -6.0F));
        root.addOrReplaceChild("beak", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 0.0F, 0.0F, 1, 1, 2), PartPose.offset(0.0F, 14.5F, -8.0F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float wingAngle = Mth.cos(ageInTicks * 1.3f) * (float) Math.PI * 0.25f;
        this.lWing.zRot = wingAngle;
        this.rWing.zRot = -wingAngle;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        fins.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        lWing.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        rWing.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        tail.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        tailEnd.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        head.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        beak.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
