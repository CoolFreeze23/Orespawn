package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.EntityStinky;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class StinkyModel extends EntityModel<EntityStinky> {
    private final ModelPart body;
    private final ModelPart neck1;
    private final ModelPart neck;
    private final ModelPart neckbase;
    private final ModelPart head;
    private final ModelPart Rleg1;
    private final ModelPart Lleg1;
    private final ModelPart Lhorn1;
    private final ModelPart Rhorn1;
    private final ModelPart snout;
    private final ModelPart Lhorn2;
    private final ModelPart Rhorn2;
    private final ModelPart tail1;
    private final ModelPart Rleg2;
    private final ModelPart Lleg2;
    private final ModelPart tail2;
    private final ModelPart tail3;
    private final ModelPart tail4;
    private final ModelPart Lwing;
    private final ModelPart Rwing;

    public StinkyModel(ModelPart root) {
        this.body = root.getChild("body");
        this.neck1 = root.getChild("neck1");
        this.neck = root.getChild("neck");
        this.neckbase = root.getChild("neckbase");
        this.head = root.getChild("head");
        this.Rleg1 = root.getChild("Rleg1");
        this.Lleg1 = root.getChild("Lleg1");
        this.Lhorn1 = root.getChild("Lhorn1");
        this.Rhorn1 = root.getChild("Rhorn1");
        this.snout = root.getChild("snout");
        this.Lhorn2 = root.getChild("Lhorn2");
        this.Rhorn2 = root.getChild("Rhorn2");
        this.tail1 = root.getChild("tail1");
        this.Rleg2 = root.getChild("Rleg2");
        this.Lleg2 = root.getChild("Lleg2");
        this.tail2 = root.getChild("tail2");
        this.tail3 = root.getChild("tail3");
        this.tail4 = root.getChild("tail4");
        this.Lwing = root.getChild("Lwing");
        this.Rwing = root.getChild("Rwing");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 12).mirror()
                        .addBox(-4.5F, -3.0F, -5.0F, 8, 8, 10),
                PartPose.offset(0.5F, 15.0F, 1.0F));

        partdefinition.addOrReplaceChild("neck1",
                CubeListBuilder.create().texOffs(0, 31).mirror()
                        .addBox(-2.0F, -3.0F, -2.0F, 4, 5, 5),
                PartPose.offsetAndRotation(0.0F, 16.0F, -5.0F, 0.715585F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("neck",
                CubeListBuilder.create().texOffs(0, 42).mirror()
                        .addBox(-2.0F, -8.0F, -3.0F, 4, 8, 4),
                PartPose.offset(0.0F, 15.0F, -5.5F));

        partdefinition.addOrReplaceChild("neckbase",
                CubeListBuilder.create().texOffs(0, 55).mirror()
                        .addBox(-3.0F, -4.0F, 0.0F, 6, 6, 3),
                PartPose.offset(0.0F, 17.0F, 5.0F));

        partdefinition.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 0).mirror()
                        .addBox(-2.5F, -10.0F, -3.5F, 5, 5, 5),
                PartPose.offset(0.0F, 15.0F, -5.5F));

        partdefinition.addOrReplaceChild("Rleg1",
                CubeListBuilder.create().texOffs(19, 53).mirror()
                        .addBox(-1.5F, 0.0F, -1.0F, 3, 8, 3),
                PartPose.offset(2.0F, 16.0F, 5.5F));

        partdefinition.addOrReplaceChild("Lleg1",
                CubeListBuilder.create().texOffs(19, 53).mirror()
                        .addBox(-1.5F, 0.0F, -0.5F, 3, 8, 3),
                PartPose.offset(-2.0F, 16.0F, 5.0F));

        partdefinition.addOrReplaceChild("Lhorn1",
                CubeListBuilder.create().texOffs(19, 47).mirror()
                        .addBox(-3.0F, -10.5F, -1.0F, 2, 2, 3),
                PartPose.offset(0.0F, 15.0F, -5.5F));

        partdefinition.addOrReplaceChild("Rhorn1",
                CubeListBuilder.create().texOffs(19, 47).mirror()
                        .addBox(1.0F, -10.5F, -1.0F, 2, 2, 3),
                PartPose.offset(0.0F, 15.0F, -5.5F));

        partdefinition.addOrReplaceChild("snout",
                CubeListBuilder.create().texOffs(32, 57).mirror()
                        .addBox(-1.5F, -8.0F, -6.5F, 3, 3, 4),
                PartPose.offset(0.0F, 15.0F, -5.5F));

        partdefinition.addOrReplaceChild("Lhorn2",
                CubeListBuilder.create().texOffs(19, 42).mirror()
                        .addBox(-2.5F, -10.0F, 1.0F, 1, 1, 3),
                PartPose.offset(0.0F, 15.0F, -5.5F));

        partdefinition.addOrReplaceChild("Rhorn2",
                CubeListBuilder.create().texOffs(19, 42).mirror()
                        .addBox(1.5F, -10.0F, 1.0F, 1, 1, 3),
                PartPose.offset(0.0F, 15.0F, -5.5F));

        partdefinition.addOrReplaceChild("tail1",
                CubeListBuilder.create().texOffs(47, 55).mirror()
                        .addBox(-3.0F, -3.0F, -3.0F, 6, 6, 3),
                PartPose.offset(0.0F, 16.5F, -2.0F));

        partdefinition.addOrReplaceChild("Rleg2",
                CubeListBuilder.create().texOffs(19, 53).mirror()
                        .addBox(-1.5F, 0.0F, -1.5F, 3, 8, 3),
                PartPose.offset(2.0F, 16.0F, -3.0F));

        partdefinition.addOrReplaceChild("Lleg2",
                CubeListBuilder.create().texOffs(19, 53).mirror()
                        .addBox(-1.5F, 0.0F, -1.5F, 3, 8, 3),
                PartPose.offset(-2.0F, 16.0F, -3.0F));

        partdefinition.addOrReplaceChild("tail2",
                CubeListBuilder.create().texOffs(19, 31).mirror()
                        .addBox(-2.5F, -2.5F, 0.0F, 5, 5, 5),
                PartPose.offsetAndRotation(0.0F, 16.0F, 7.0F, -0.3839724F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("tail3",
                CubeListBuilder.create().texOffs(32, 46).mirror()
                        .addBox(-2.0F, -2.0F, 0.0F, 4, 4, 4),
                PartPose.offsetAndRotation(0.0F, 17.2F, 11.0F, -0.2094395F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("tail4",
                CubeListBuilder.create().texOffs(37, 13).mirror()
                        .addBox(-1.5F, -1.5F, 0.0F, 3, 3, 5),
                PartPose.offsetAndRotation(0.0F, 17.5F, 14.0F, -0.0698132F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("Lwing",
                CubeListBuilder.create().texOffs(59, 0).mirror()
                        .addBox(-18.0F, 0.0F, -5.0F, 18, 0, 10),
                PartPose.offsetAndRotation(-2.0F, 12.6F, 0.0F, 0.0F, 0.0F, 0.4014257F));

        partdefinition.addOrReplaceChild("Rwing",
                CubeListBuilder.create().texOffs(59, 11).mirror()
                        .addBox(0.0F, 0.0F, -5.0F, 18, 0, 10),
                PartPose.offsetAndRotation(2.0F, 12.6F, 0.0F, 0.0F, 0.0F, -0.4014257F));

        return LayerDefinition.create(meshdefinition, 128, 64);
    }

    @Override
    public void setupAnim(EntityStinky entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float ws = limbSwingAmount;

        float newangle = (double) limbSwingAmount > 0.1 ? Mth.cos(ageInTicks * 2.3F * ws) * (float) Math.PI * 0.4F * limbSwingAmount : 0.0F;
        this.Rwing.zRot = newangle - 0.4F;
        this.Lwing.zRot = -newangle + 0.4F;

        newangle = (double) limbSwingAmount > 0.1 ? Mth.cos(ageInTicks * 2.0F * ws) * (float) Math.PI * 0.25F * limbSwingAmount : 0.0F;
        int currentActivity = entity.getActivity();
        if (currentActivity != 2) {
            this.Rleg1.xRot = newangle;
            this.Lleg1.xRot = -newangle;
            this.Rleg2.xRot = -newangle;
            this.Lleg2.xRot = newangle;
        } else {
            newangle = -1.0F;
            this.Rleg2.xRot = newangle;
            this.Lleg2.xRot = newangle;
            newangle = 1.0F;
            this.Rleg1.xRot = newangle;
            this.Lleg1.xRot = newangle;
        }

        newangle = Mth.cos(ageInTicks * 1.0F * ws) * (float) Math.PI * 0.2F;
        if (entity.isInSittingPose()) {
            newangle = 0.0F;
        }
        this.tail2.yRot = newangle;
        this.tail3.z = this.tail2.z + (float) Math.cos(this.tail2.yRot) * 4.0F;
        this.tail3.x = this.tail2.x + (float) Math.sin(this.tail2.yRot) * 4.0F - 0.5F;
        this.tail3.yRot = newangle * 1.6F;
        this.tail4.z = this.tail3.z + (float) Math.cos(this.tail3.yRot) * 3.0F;
        this.tail4.x = this.tail3.x + (float) Math.sin(this.tail3.yRot) * 3.0F - 0.5F;
        this.tail4.yRot = newangle * 2.6F;

        this.head.yRot = (float) Math.toRadians(netHeadYaw);
        this.snout.yRot = (float) Math.toRadians(netHeadYaw);
        this.neck.yRot = (float) Math.toRadians(netHeadYaw) / 2.0F;
        this.Rhorn1.yRot = (float) Math.toRadians(netHeadYaw);
        this.Rhorn2.yRot = (float) Math.toRadians(netHeadYaw);
        this.Lhorn1.yRot = (float) Math.toRadians(netHeadYaw);
        this.Lhorn2.yRot = (float) Math.toRadians(netHeadYaw);

        this.head.xRot = (float) Math.toRadians(headPitch) / 3.0F;
        this.snout.xRot = (float) Math.toRadians(headPitch) / 3.0F;
        this.neck.xRot = (float) Math.toRadians(headPitch) / 3.0F;
        this.Rhorn1.xRot = (float) Math.toRadians(headPitch) / 3.0F;
        this.Rhorn2.xRot = (float) Math.toRadians(headPitch) / 3.0F;
        this.Lhorn1.xRot = (float) Math.toRadians(headPitch) / 3.0F;
        this.Lhorn2.xRot = (float) Math.toRadians(headPitch) / 3.0F;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.neck1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.neck.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.neckbase.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.head.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Rleg1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Lleg1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Lhorn1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Rhorn1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.snout.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Lhorn2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Rhorn2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tail1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Rleg2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Lleg2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tail2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tail3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tail4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Lwing.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Rwing.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
