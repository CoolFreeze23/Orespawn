package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.EntityRat;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class RatModel<T extends EntityRat> extends EntityModel<T> {
    private final ModelPart body;
    private final ModelPart tail1;
    private final ModelPart tail2;
    private final ModelPart lfleg;
    private final ModelPart rfleg;
    private final ModelPart lrleg;
    private final ModelPart rrleg;
    private final ModelPart body2;
    private final ModelPart head;
    private final ModelPart nose;
    private final ModelPart lear;
    private final ModelPart rear;

    public RatModel(ModelPart root) {
        this.body = root.getChild("body");
        this.tail1 = root.getChild("tail1");
        this.tail2 = root.getChild("tail2");
        this.lfleg = root.getChild("lfleg");
        this.rfleg = root.getChild("rfleg");
        this.lrleg = root.getChild("lrleg");
        this.rrleg = root.getChild("rrleg");
        this.body2 = root.getChild("body2");
        this.head = root.getChild("head");
        this.nose = root.getChild("nose");
        this.lear = root.getChild("lear");
        this.rear = root.getChild("rear");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(27, 0).mirror().addBox(-2.0F, -1.0F, 0.0F, 5, 3, 10), PartPose.offset(0.0F, 20.0F, -3.0F));
        root.addOrReplaceChild("tail1", CubeListBuilder.create().texOffs(0, 30).mirror().addBox(-0.5F, -1.0F, 0.0F, 2, 2, 9), PartPose.offset(0.0F, 21.0F, 7.0F));
        root.addOrReplaceChild("tail2", CubeListBuilder.create().texOffs(0, 43).mirror().addBox(0.0F, 0.0F, 0.0F, 1, 1, 12), PartPose.offset(0.0F, 21.0F, 16.0F));
        root.addOrReplaceChild("lfleg", CubeListBuilder.create().texOffs(0, 14).mirror().addBox(0.0F, 0.0F, 0.0F, 1, 2, 1), PartPose.offset(2.0F, 22.0F, -2.0F));
        root.addOrReplaceChild("rfleg", CubeListBuilder.create().texOffs(0, 14).mirror().addBox(0.0F, 0.0F, 0.0F, 1, 2, 1), PartPose.offset(-2.0F, 22.0F, -2.0F));
        root.addOrReplaceChild("lrleg", CubeListBuilder.create().texOffs(0, 14).mirror().addBox(0.0F, 0.0F, 0.0F, 1, 2, 1), PartPose.offset(2.0F, 22.0F, 5.0F));
        root.addOrReplaceChild("rrleg", CubeListBuilder.create().texOffs(0, 14).mirror().addBox(0.0F, 0.0F, 0.0F, 1, 2, 1), PartPose.offset(-2.0F, 22.0F, 5.0F));
        root.addOrReplaceChild("body2", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-1.0F, 0.0F, 0.0F, 3, 2, 10), PartPose.offset(0.0F, 19.0F, -3.0F));
        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(27, 14).mirror().addBox(-1.0F, -1.0F, -4.0F, 3, 3, 4), PartPose.offset(0.0F, 20.0F, -3.0F));
        root.addOrReplaceChild("nose", CubeListBuilder.create().texOffs(27, 22).mirror().addBox(0.0F, 0.0F, -2.0F, 1, 1, 2), PartPose.offset(0.0F, 20.0F, -7.0F));
        root.addOrReplaceChild("lear", CubeListBuilder.create().texOffs(5, 14).mirror().addBox(0.0F, -3.0F, 0.0F, 2, 3, 1), PartPose.offset(1.0F, 19.0F, -5.0F));
        root.addOrReplaceChild("rear", CubeListBuilder.create().texOffs(5, 14).mirror().addBox(-2.0F, -3.0F, 0.0F, 2, 3, 1), PartPose.offset(-1.0F, 19.0F, -5.0F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float legAngle = limbSwingAmount > 0.1f ? Mth.cos(ageInTicks * 1.3f) * (float) Math.PI * 0.25f * limbSwingAmount : 0.0f;
        this.lfleg.xRot = legAngle;
        this.rfleg.xRot = -legAngle;
        this.lrleg.xRot = -legAngle;
        this.rrleg.xRot = legAngle;
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        tail1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        tail2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        lfleg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        rfleg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        lrleg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        rrleg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        body2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        head.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        nose.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        lear.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        rear.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
