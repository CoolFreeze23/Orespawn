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
        // i080: 8 of 12 parts (rfleg, lrleg, rrleg, body2, head, nose, lear, rear)
        // had invented UVs/geometry; every cube below now matches orig
        // ModelRat.java:32-103 exactly (texture 64x64, all parts mirrored).
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        // orig ModelRat.java:32-37
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(27, 0).mirror().addBox(-2.0F, -1.0F, 0.0F, 5, 3, 10), PartPose.offset(0.0F, 20.0F, -3.0F));
        // orig ModelRat.java:38-43
        root.addOrReplaceChild("tail1", CubeListBuilder.create().texOffs(0, 30).mirror().addBox(-0.5F, -1.0F, 0.0F, 2, 2, 9), PartPose.offset(0.0F, 21.0F, 7.0F));
        // orig ModelRat.java:44-49
        root.addOrReplaceChild("tail2", CubeListBuilder.create().texOffs(0, 43).mirror().addBox(0.0F, 0.0F, 0.0F, 1, 1, 12), PartPose.offset(0.0F, 21.0F, 16.0F));
        // orig ModelRat.java:50-55
        root.addOrReplaceChild("lfleg", CubeListBuilder.create().texOffs(0, 14).mirror().addBox(0.0F, 0.0F, 0.0F, 1, 2, 1), PartPose.offset(2.0F, 22.0F, -2.0F));
        // i080 fix — orig ModelRat.java:56-61 (texOffs was invented as 0,14)
        root.addOrReplaceChild("rfleg", CubeListBuilder.create().texOffs(10, 14).mirror().addBox(0.0F, 0.0F, 0.0F, 1, 2, 1), PartPose.offset(-2.0F, 22.0F, -2.0F));
        // i080 fix — orig ModelRat.java:62-67 (haunch: 2x4x2 @ (2,20,4), not a front-leg clone)
        root.addOrReplaceChild("lrleg", CubeListBuilder.create().texOffs(0, 18).mirror().addBox(0.0F, 0.0F, 0.0F, 2, 4, 2), PartPose.offset(2.0F, 20.0F, 4.0F));
        // i080 fix — orig ModelRat.java:68-73
        root.addOrReplaceChild("rrleg", CubeListBuilder.create().texOffs(9, 18).mirror().addBox(0.0F, 0.0F, 0.0F, 2, 4, 2), PartPose.offset(-3.0F, 20.0F, 4.0F));
        // i080 fix — orig ModelRat.java:74-79 (spine ridge 1x1x6, was a 3x2x10 second torso)
        root.addOrReplaceChild("body2", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(0.0F, 0.0F, 0.0F, 1, 1, 6), PartPose.offset(0.0F, 18.0F, 0.0F));
        // i080 fix — orig ModelRat.java:80-85
        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(27, 17).mirror().addBox(-1.0F, -2.0F, -3.0F, 3, 2, 4), PartPose.offset(0.0F, 22.0F, -4.0F));
        // i080 fix — orig ModelRat.java:86-91
        root.addOrReplaceChild("nose", CubeListBuilder.create().texOffs(27, 25).mirror().addBox(0.0F, -1.0F, -5.0F, 1, 1, 2), PartPose.offset(0.0F, 22.0F, -4.0F));
        // i080 fix — orig ModelRat.java:92-97 (1x1x1 nub ears, not 2x3x1 mouse ears)
        root.addOrReplaceChild("lear", CubeListBuilder.create().texOffs(0, 9).mirror().addBox(0.0F, 0.0F, 0.0F, 1, 1, 1), PartPose.offset(1.5F, 19.5F, -4.0F));
        // i080 fix — orig ModelRat.java:98-103
        root.addOrReplaceChild("rear", CubeListBuilder.create().texOffs(5, 9).mirror().addBox(0.0F, 0.0F, 0.0F, 1, 1, 1), PartPose.offset(-1.5F, 19.5F, -4.0F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        // orig ModelRat.java:111-115 — legs scurry on time at 1.7*wingspeed
        // (wingspeed 1.0, orig ClientProxyOreSpawn.java:482), amplitude scaled
        // by limbSwingAmount, frozen below the 0.1 movement threshold.
        float legAngle = limbSwingAmount > 0.1f ? Mth.cos(ageInTicks * 1.7f) * (float) Math.PI * 0.25f * limbSwingAmount : 0.0f;
        this.rfleg.xRot = legAngle;
        this.lfleg.xRot = -legAngle;
        this.rrleg.xRot = -legAngle;
        this.lrleg.xRot = legAngle;

        // orig ModelRat.java:116-120 — tail thrashes fast and wide while
        // getAttacking() != 0 (freq 1.5, amp 0.25π), gentle idle sway
        // otherwise (freq 0.4, amp 0.05π); tail2 follows tail1's tip.
        float tailAngle = entity.getAttacking() != 0
                ? Mth.cos(ageInTicks * 1.5f) * (float) Math.PI * 0.25f
                : Mth.cos(ageInTicks * 0.4f) * (float) Math.PI * 0.05f;
        this.tail1.yRot = tailAngle * 0.5f;
        this.tail2.yRot = tailAngle * 1.25f;
        this.tail2.z = this.tail1.z + (float) Math.cos(this.tail1.yRot) * 9.0f;
        this.tail2.x = this.tail1.x + (float) Math.sin(this.tail1.yRot) * 9.0f;
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
