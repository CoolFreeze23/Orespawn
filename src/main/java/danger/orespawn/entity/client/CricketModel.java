package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;

public class CricketModel<T extends Mob> extends EntityModel<T> {
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart abdomen;
    private final ModelPart lfleg;
    private final ModelPart lrleg;
    private final ModelPart rfleg;
    private final ModelPart rrleg;
    private final ModelPart lleg1;
    private final ModelPart rleg1;
    private final ModelPart lleg2;
    private final ModelPart rleg2;

    public CricketModel(ModelPart root) {
        this.body = root.getChild("body");
        this.head = root.getChild("head");
        this.abdomen = root.getChild("abdomen");
        this.lfleg = root.getChild("lfleg");
        this.lrleg = root.getChild("lrleg");
        this.rfleg = root.getChild("rfleg");
        this.rrleg = root.getChild("rrleg");
        this.lleg1 = root.getChild("lleg1");
        this.rleg1 = root.getChild("rleg1");
        this.lleg2 = root.getChild("lleg2");
        this.rleg2 = root.getChild("rleg2");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 25).mirror()
                        .addBox(-1.0F, -1.0F, -3.0F, 3, 3, 6),
                PartPose.offset(0.0F, 21.0F, 0.0F));

        partdefinition.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 17).mirror()
                        .addBox(-1.0F, -2.0F, -1.0F, 3, 4, 3),
                PartPose.offsetAndRotation(0.0F, 21.0F, -5.0F, -0.1745329F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("abdomen",
                CubeListBuilder.create().texOffs(0, 36).mirror()
                        .addBox(-0.5F, -1.0F, 3.0F, 2, 2, 3),
                PartPose.offset(0.0F, 21.0F, 0.0F));

        partdefinition.addOrReplaceChild("lfleg",
                CubeListBuilder.create().texOffs(25, 0).mirror()
                        .addBox(2.0F, 0.0F, 0.0F, 5, 1, 1),
                PartPose.offsetAndRotation(0.0F, 21.0F, -2.0F, 0.0F, 0.4712389F, 0.418879F));

        partdefinition.addOrReplaceChild("lrleg",
                CubeListBuilder.create().texOffs(23, 4).mirror()
                        .addBox(1.0F, 0.0F, -2.0F, 6, 1, 1),
                PartPose.offsetAndRotation(0.0F, 21.0F, 0.0F, 0.0F, -0.296706F, 0.418879F));

        partdefinition.addOrReplaceChild("rfleg",
                CubeListBuilder.create().texOffs(25, 8).mirror()
                        .addBox(-7.0F, 0.0F, 0.0F, 5, 1, 1),
                PartPose.offsetAndRotation(1.0F, 21.0F, -2.0F, 0.0F, -0.5410521F, -0.4363323F));

        partdefinition.addOrReplaceChild("rrleg",
                CubeListBuilder.create().texOffs(25, 12).mirror()
                        .addBox(-7.0F, -1.0F, 0.0F, 5, 1, 1),
                PartPose.offsetAndRotation(1.0F, 22.0F, -2.0F, 0.0F, 0.3839724F, -0.418879F));

        partdefinition.addOrReplaceChild("lleg1",
                CubeListBuilder.create().texOffs(40, 0).mirror()
                        .addBox(-1.0F, -1.0F, 0.0F, 1, 2, 8),
                PartPose.offsetAndRotation(2.0F, 22.0F, 0.0F, 0.5585054F, 0.4363323F, 0.0F));

        partdefinition.addOrReplaceChild("rleg1",
                CubeListBuilder.create().texOffs(40, 11).mirror()
                        .addBox(0.0F, -1.0F, 0.0F, 1, 2, 8),
                PartPose.offsetAndRotation(-1.0F, 22.0F, 0.0F, 0.5585054F, -0.4363323F, 0.0F));

        partdefinition.addOrReplaceChild("lleg2",
                CubeListBuilder.create().texOffs(21, 23).mirror()
                        .addBox(-0.5F, -6.5F, 4.5F, 1, 1, 8),
                PartPose.offsetAndRotation(2.0F, 22.0F, 0.0F, -0.3665191F, 0.3490659F, 0.0F));

        partdefinition.addOrReplaceChild("rleg2",
                CubeListBuilder.create().texOffs(21, 34).mirror()
                        .addBox(-0.5F, -6.5F, 4.0F, 1, 1, 8),
                PartPose.offsetAndRotation(-1.0F, 22.0F, 0.0F, -0.3665191F, -0.3490659F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float newangle = limbSwingAmount > 0.1F
                ? Mth.cos(ageInTicks * 1.0F) * (float) Math.PI * 0.25F * limbSwingAmount
                : 0.0F;
        this.lfleg.yRot = 0.47F + newangle;
        this.rfleg.yRot = -0.54F + newangle;
        this.lrleg.yRot = -0.296F - newangle;
        this.rrleg.yRot = 0.384F - newangle;

        this.lleg1.yRot = 0.436F;
        this.lleg2.yRot = 0.349F;
        this.rleg1.yRot = -0.436F;
        this.rleg2.yRot = -0.349F;
        this.lleg1.xRot = 0.558F;
        this.lleg2.xRot = -0.366F;
        this.rleg1.xRot = 0.558F;
        this.rleg2.xRot = -0.366F;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.head.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.abdomen.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lfleg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lrleg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rfleg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rrleg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lleg1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rleg1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lleg2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rleg2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
