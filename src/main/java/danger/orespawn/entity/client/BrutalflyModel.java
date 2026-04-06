package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.EntityBrutalfly;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class BrutalflyModel extends EntityModel<EntityBrutalfly> {
    private final ModelPart body;
    private final ModelPart leftwing;
    private final ModelPart rightwing;
    private final ModelPart leftwing2;
    private final ModelPart rightwing2;
    private final ModelPart leftwing3;
    private final ModelPart rightwing3;
    private final ModelPart head;
    private final ModelPart leftwing4;
    private final ModelPart rightwing4;
    private final ModelPart leftwing5;
    private final ModelPart leftwing6;
    private final ModelPart rightwing5;
    private final ModelPart rightwing6;

    public BrutalflyModel(ModelPart root) {
        this.body = root.getChild("body");
        this.leftwing = root.getChild("leftwing");
        this.rightwing = root.getChild("rightwing");
        this.leftwing2 = root.getChild("leftwing2");
        this.rightwing2 = root.getChild("rightwing2");
        this.leftwing3 = root.getChild("leftwing3");
        this.rightwing3 = root.getChild("rightwing3");
        this.head = root.getChild("head");
        this.leftwing4 = root.getChild("leftwing4");
        this.rightwing4 = root.getChild("rightwing4");
        this.leftwing5 = root.getChild("leftwing5");
        this.leftwing6 = root.getChild("leftwing6");
        this.rightwing5 = root.getChild("rightwing5");
        this.rightwing6 = root.getChild("rightwing6");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(21, 19).mirror()
                        .addBox(0.0F, 0.0F, -4.0F, 1, 1, 8),
                PartPose.offset(0.0F, 17.0F, 0.0F));

        partdefinition.addOrReplaceChild("leftwing",
                CubeListBuilder.create().texOffs(43, 24).mirror()
                        .addBox(0.0F, 0.0F, -4.0F, 1, 1, 5),
                PartPose.offset(1.0F, 17.0F, 0.0F));

        partdefinition.addOrReplaceChild("rightwing",
                CubeListBuilder.create().texOffs(43, 17).mirror()
                        .addBox(-1.0F, 0.0F, -4.0F, 1, 1, 5),
                PartPose.offset(0.0F, 17.0F, 0.0F));

        partdefinition.addOrReplaceChild("leftwing2",
                CubeListBuilder.create().texOffs(0, 0).mirror()
                        .addBox(1.0F, 0.0F, -6.0F, 6, 1, 7),
                PartPose.offset(1.0F, 17.0F, 0.0F));

        partdefinition.addOrReplaceChild("rightwing2",
                CubeListBuilder.create().texOffs(29, 0).mirror()
                        .addBox(-7.0F, 0.0F, -6.0F, 6, 1, 7),
                PartPose.offset(0.0F, 17.0F, 0.0F));

        partdefinition.addOrReplaceChild("leftwing3",
                CubeListBuilder.create().texOffs(0, 9).mirror()
                        .addBox(0.0F, 0.0F, 1.0F, 5, 1, 5),
                PartPose.offset(1.0F, 17.0F, 0.0F));

        partdefinition.addOrReplaceChild("rightwing3",
                CubeListBuilder.create().texOffs(27, 9).mirror()
                        .addBox(-5.0F, 0.0F, 1.0F, 5, 1, 5),
                PartPose.offset(0.0F, 17.0F, 0.0F));

        partdefinition.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(21, 11).mirror()
                        .addBox(0.0F, 0.0F, -6.0F, 1, 1, 1),
                PartPose.offset(0.0F, 17.0F, 1.0F));

        partdefinition.addOrReplaceChild("leftwing4",
                CubeListBuilder.create().texOffs(2, 24).mirror()
                        .addBox(0.0F, 0.0F, 6.0F, 2, 1, 7),
                PartPose.offset(1.0F, 17.0F, 0.0F));

        partdefinition.addOrReplaceChild("rightwing4",
                CubeListBuilder.create().texOffs(2, 16).mirror()
                        .addBox(-2.0F, 0.0F, 6.0F, 2, 1, 7),
                PartPose.offset(0.0F, 17.0F, 0.0F));

        partdefinition.addOrReplaceChild("leftwing5",
                CubeListBuilder.create().texOffs(21, 16).mirror()
                        .addBox(1.0F, 0.0F, -7.0F, 1, 1, 1),
                PartPose.offset(1.0F, 17.0F, 0.0F));

        partdefinition.addOrReplaceChild("leftwing6",
                CubeListBuilder.create().texOffs(50, 10).mirror()
                        .addBox(7.0F, 0.0F, -6.0F, 2, 1, 1),
                PartPose.offset(1.0F, 17.0F, 0.0F));

        partdefinition.addOrReplaceChild("rightwing5",
                CubeListBuilder.create().texOffs(27, 16).mirror()
                        .addBox(-2.0F, 0.0F, -7.0F, 1, 1, 1),
                PartPose.offset(0.0F, 17.0F, 0.0F));

        partdefinition.addOrReplaceChild("rightwing6",
                CubeListBuilder.create().texOffs(50, 13).mirror()
                        .addBox(-9.0F, 0.0F, -6.0F, 2, 1, 1),
                PartPose.offset(0.0F, 17.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 32);
    }

    @Override
    public void setupAnim(EntityBrutalfly entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.rightwing2.zRot = this.rightwing.zRot = Mth.cos((float)(ageInTicks * 1.3f * limbSwingAmount)) * (float)Math.PI * 0.25f;
        this.rightwing3.zRot = this.rightwing.zRot;
        this.rightwing4.zRot = this.rightwing.zRot;
        this.rightwing5.zRot = this.rightwing.zRot;
        this.rightwing6.zRot = this.rightwing.zRot;
        this.leftwing.zRot = -this.rightwing.zRot;
        this.leftwing2.zRot = -this.rightwing.zRot;
        this.leftwing3.zRot = -this.rightwing.zRot;
        this.leftwing4.zRot = -this.rightwing.zRot;
        this.leftwing5.zRot = -this.rightwing.zRot;
        this.leftwing6.zRot = -this.rightwing.zRot;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.head.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leftwing.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rightwing.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leftwing2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rightwing2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leftwing3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rightwing3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leftwing4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rightwing4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leftwing5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rightwing5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leftwing6.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rightwing6.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
