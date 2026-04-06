package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;

public class AntModel<T extends Mob> extends EntityModel<T> {
    private final ModelPart thorax;
    private final ModelPart thorax1;
    private final ModelPart thorax3;
    private final ModelPart abdomen;
    private final ModelPart abdomen1;
    private final ModelPart head;
    private final ModelPart jawsr;
    private final ModelPart jawsl;
    private final ModelPart llegtop1;
    private final ModelPart llegbot1;
    private final ModelPart llegtop2;
    private final ModelPart llegbot2;
    private final ModelPart llegtop3;
    private final ModelPart llegbot3;
    private final ModelPart rlegtop1;
    private final ModelPart rlegbot1;
    private final ModelPart rlegtop2;
    private final ModelPart rlegbot2;
    private final ModelPart rlegtop3;
    private final ModelPart rlegbot3;

    public AntModel(ModelPart root) {
        this.thorax = root.getChild("thorax");
        this.thorax1 = root.getChild("thorax1");
        this.thorax3 = root.getChild("thorax3");
        this.abdomen = root.getChild("abdomen");
        this.abdomen1 = root.getChild("abdomen1");
        this.head = root.getChild("head");
        this.jawsr = root.getChild("jawsr");
        this.jawsl = root.getChild("jawsl");
        this.llegtop1 = root.getChild("llegtop1");
        this.llegbot1 = root.getChild("llegbot1");
        this.llegtop2 = root.getChild("llegtop2");
        this.llegbot2 = root.getChild("llegbot2");
        this.llegtop3 = root.getChild("llegtop3");
        this.llegbot3 = root.getChild("llegbot3");
        this.rlegtop1 = root.getChild("rlegtop1");
        this.rlegbot1 = root.getChild("rlegbot1");
        this.rlegtop2 = root.getChild("rlegtop2");
        this.rlegbot2 = root.getChild("rlegbot2");
        this.rlegtop3 = root.getChild("rlegtop3");
        this.rlegbot3 = root.getChild("rlegbot3");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("thorax",
                CubeListBuilder.create().texOffs(22, 0).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 3, 3, 3),
                PartPose.offset(0.0F, 17.0F, 0.0F));

        partdefinition.addOrReplaceChild("thorax1",
                CubeListBuilder.create().texOffs(18, 0).mirror()
                        .addBox(1.0F, 1.0F, -1.0F, 1, 1, 1),
                PartPose.offset(0.0F, 17.0F, 0.0F));

        partdefinition.addOrReplaceChild("thorax3",
                CubeListBuilder.create().texOffs(34, 0).mirror()
                        .addBox(1.0F, 1.0F, 3.0F, 1, 1, 1),
                PartPose.offset(0.0F, 17.0F, 0.0F));

        partdefinition.addOrReplaceChild("abdomen",
                CubeListBuilder.create().texOffs(38, 0).mirror()
                        .addBox(0.0F, 0.0F, 4.0F, 3, 3, 5),
                PartPose.offset(0.0F, 17.0F, 0.0F));

        partdefinition.addOrReplaceChild("abdomen1",
                CubeListBuilder.create().texOffs(54, 0).mirror()
                        .addBox(1.0F, 1.0F, 9.0F, 1, 1, 1),
                PartPose.offset(0.0F, 17.0F, 0.0F));

        partdefinition.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(6, 0).mirror()
                        .addBox(0.0F, -1.0F, -4.0F, 3, 3, 3),
                PartPose.offset(0.0F, 17.0F, 0.0F));

        partdefinition.addOrReplaceChild("jawsr",
                CubeListBuilder.create().texOffs(0, 9).mirror()
                        .addBox(-1.0F, 0.0F, -6.0F, 1, 1, 3),
                PartPose.offset(0.0F, 17.0F, 0.0F));

        partdefinition.addOrReplaceChild("jawsl",
                CubeListBuilder.create().texOffs(0, 14).mirror()
                        .addBox(3.0F, 0.0F, -6.0F, 1, 1, 3),
                PartPose.offset(0.0F, 17.0F, 0.0F));

        partdefinition.addOrReplaceChild("llegtop1",
                CubeListBuilder.create().texOffs(15, 10).mirror()
                        .addBox(3.0F, 1.0F, 1.0F, 3, 1, 1),
                PartPose.offsetAndRotation(0.0F, 17.0F, 0.0F, 0.0F, 0.0F, 0.3839724F));

        partdefinition.addOrReplaceChild("llegbot1",
                CubeListBuilder.create().texOffs(15, 19).mirror()
                        .addBox(5.0F, -3.0F, 1.0F, 3, 1, 1),
                PartPose.offsetAndRotation(0.0F, 17.0F, 0.0F, 0.0F, 0.0F, 1.064651F));

        partdefinition.addOrReplaceChild("llegtop2",
                CubeListBuilder.create().texOffs(15, 13).mirror()
                        .addBox(3.0F, 1.0F, 2.0F, 3, 1, 1),
                PartPose.offsetAndRotation(0.0F, 17.0F, 0.0F, 0.0F, -0.2094395F, 0.3839724F));

        partdefinition.addOrReplaceChild("llegbot2",
                CubeListBuilder.create().texOffs(15, 22).mirror()
                        .addBox(5.0F, -3.0F, 2.0F, 3, 1, 1),
                PartPose.offsetAndRotation(0.0F, 17.0F, 0.0F, 0.0F, -0.2268928F, 1.064651F));

        partdefinition.addOrReplaceChild("llegtop3",
                CubeListBuilder.create().texOffs(15, 16).mirror()
                        .addBox(3.0F, 1.0F, 0.0F, 3, 1, 1),
                PartPose.offsetAndRotation(0.0F, 17.0F, 0.0F, 0.0F, 0.3490659F, 0.3839724F));

        partdefinition.addOrReplaceChild("llegbot3",
                CubeListBuilder.create().texOffs(15, 25).mirror()
                        .addBox(5.0F, -3.0F, 0.0F, 3, 1, 1),
                PartPose.offsetAndRotation(0.0F, 17.0F, 0.0F, 0.0F, 0.3490659F, 1.064651F));

        partdefinition.addOrReplaceChild("rlegtop1",
                CubeListBuilder.create().texOffs(25, 10).mirror()
                        .addBox(-4.0F, 2.0F, 1.0F, 3, 1, 1),
                PartPose.offsetAndRotation(0.0F, 17.0F, 0.0F, 0.0F, 0.0F, -0.4712389F));

        partdefinition.addOrReplaceChild("rlegbot1",
                CubeListBuilder.create().texOffs(25, 19).mirror()
                        .addBox(-7.0F, 0.0F, 1.0F, 3, 1, 1),
                PartPose.offsetAndRotation(0.0F, 17.0F, 0.0F, 0.0F, 0.0F, -0.9773844F));

        partdefinition.addOrReplaceChild("rlegtop2",
                CubeListBuilder.create().texOffs(25, 13).mirror()
                        .addBox(-4.0F, 2.0F, 0.0F, 3, 1, 1),
                PartPose.offsetAndRotation(0.0F, 17.0F, 0.0F, 0.0F, -0.5934119F, -0.4712389F));

        partdefinition.addOrReplaceChild("rlegbot2",
                CubeListBuilder.create().texOffs(25, 22).mirror()
                        .addBox(-7.0F, 0.0F, 0.0F, 3, 1, 1),
                PartPose.offsetAndRotation(0.0F, 17.0F, 0.0F, 0.0F, -0.5934119F, -0.9773844F));

        partdefinition.addOrReplaceChild("rlegtop3",
                CubeListBuilder.create().texOffs(25, 16).mirror()
                        .addBox(-4.0F, 2.0F, 2.0F, 3, 1, 1),
                PartPose.offsetAndRotation(0.0F, 17.0F, 0.0F, 0.0F, 0.418879F, -0.4712389F));

        partdefinition.addOrReplaceChild("rlegbot3",
                CubeListBuilder.create().texOffs(25, 25).mirror()
                        .addBox(-7.0F, 0.0F, 2.0F, 3, 1, 1),
                PartPose.offsetAndRotation(0.0F, 17.0F, 0.0F, 0.0F, 0.418879F, -0.9773844F));

        return LayerDefinition.create(meshdefinition, 64, 32);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.llegbot1.xRot = this.llegtop1.xRot = Mth.cos((float)(ageInTicks * 2.7f)) * (float)Math.PI * 0.45f * limbSwingAmount;
        this.rlegtop2.xRot = this.llegtop1.xRot;
        this.rlegbot2.xRot = this.llegtop1.xRot;
        this.rlegtop3.xRot = this.llegtop1.xRot;
        this.rlegbot3.xRot = this.llegtop1.xRot;
        this.rlegtop1.xRot = -this.llegtop1.xRot;
        this.rlegbot1.xRot = -this.llegtop1.xRot;
        this.llegtop2.xRot = -this.llegtop1.xRot;
        this.llegbot2.xRot = -this.llegtop1.xRot;
        this.llegtop3.xRot = -this.llegtop1.xRot;
        this.llegbot3.xRot = -this.llegtop1.xRot;
        this.jawsl.yRot = Mth.cos((float)(ageInTicks * 0.4f)) * (float)Math.PI * 0.05f;
        this.jawsr.yRot = -this.jawsl.yRot;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.thorax.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.thorax1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.thorax3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.abdomen.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.abdomen1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.head.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.jawsr.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.jawsl.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.llegtop1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.llegbot1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.llegtop2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.llegbot2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.llegtop3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.llegbot3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rlegtop1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rlegbot1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rlegtop2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rlegbot2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rlegtop3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rlegbot3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
