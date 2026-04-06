package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.Frog;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

public class ModelFrog extends EntityModel<Frog> {
    private final float wingspeed = 1.0F;
    private final ModelPart body;
    private final ModelPart jaw;
    private final ModelPart lfleg;
    private final ModelPart rfleg;
    private final ModelPart lleg1;
    private final ModelPart rleg1;
    private final ModelPart lleg2;
    private final ModelPart rleg2;
    private final ModelPart leye;
    private final ModelPart reye;

    public ModelFrog(ModelPart root) {
        this.body = root.getChild("body");
        this.jaw = root.getChild("jaw");
        this.lfleg = root.getChild("lfleg");
        this.rfleg = root.getChild("rfleg");
        this.lleg1 = root.getChild("lleg1");
        this.rleg1 = root.getChild("rleg1");
        this.lleg2 = root.getChild("lleg2");
        this.rleg2 = root.getChild("rleg2");
        this.leye = root.getChild("leye");
        this.reye = root.getChild("reye");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("body", CubeListBuilder.create()
            .texOffs(41, 0).mirror()
            .addBox(-4.0F, -10.0F, 0.0F, 8.0F, 11.0F, 2.0F),
            PartPose.offsetAndRotation(0.0F, 24.0F, 2.0F, 0.7330383F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("jaw", CubeListBuilder.create()
            .texOffs(42, 15).mirror()
            .addBox(-4.0F, -8.0F, 0.0F, 8.0F, 8.0F, 1.0F),
            PartPose.offsetAndRotation(0.0F, 24.0F, 2.0F, 1.22173F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("lfleg", CubeListBuilder.create()
            .texOffs(14, 0).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 1.0F, 5.0F, 1.0F),
            PartPose.offsetAndRotation(3.0F, 20.0F, 0.0F, -0.5235988F, 0.0F, -0.4712389F));

        partdefinition.addOrReplaceChild("rfleg", CubeListBuilder.create()
            .texOffs(20, 0).mirror()
            .addBox(-1.0F, 0.0F, 0.0F, 1.0F, 5.0F, 1.0F),
            PartPose.offsetAndRotation(-3.0F, 20.0F, 0.0F, -0.5235988F, 0.0F, 0.4712389F));

        partdefinition.addOrReplaceChild("lleg1", CubeListBuilder.create()
            .texOffs(10, 8).mirror()
            .addBox(0.0F, -9.0F, -1.0F, 1.0F, 9.0F, 2.0F),
            PartPose.offsetAndRotation(3.0F, 24.0F, 3.0F, 0.0F, 0.0F, 0.2268928F));

        partdefinition.addOrReplaceChild("rleg1", CubeListBuilder.create()
            .texOffs(18, 8).mirror()
            .addBox(-1.0F, -9.0F, -1.0F, 1.0F, 9.0F, 2.0F),
            PartPose.offsetAndRotation(-3.0F, 24.0F, 3.0F, 0.0F, 0.0F, -0.2268928F));

        partdefinition.addOrReplaceChild("lleg2", CubeListBuilder.create()
            .texOffs(11, 20).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 1.0F, 10.0F, 1.0F),
            PartPose.offsetAndRotation(5.0F, 15.0F, 3.0F, 0.0F, 0.0F, -0.3839724F));

        partdefinition.addOrReplaceChild("rleg2", CubeListBuilder.create()
            .texOffs(19, 20).mirror()
            .addBox(-1.0F, 0.0F, 0.0F, 1.0F, 10.0F, 1.0F),
            PartPose.offsetAndRotation(-5.0F, 15.0F, 3.0F, 0.0F, 0.0F, 0.3839724F));

        partdefinition.addOrReplaceChild("leye", CubeListBuilder.create()
            .texOffs(0, 8).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 1.0F, 2.0F, 1.0F),
            PartPose.offsetAndRotation(2.0F, 17.0F, -2.0F, 0.7330383F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("reye", CubeListBuilder.create()
            .texOffs(0, 4).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 1.0F, 2.0F, 1.0F),
            PartPose.offsetAndRotation(-3.0F, 17.0F, -2.0F, 0.7330383F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(Frog entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float newangle = limbSwingAmount > 0.1F
            ? Mth.cos(ageInTicks * this.wingspeed * 1.4F) * (float) Math.PI * 0.55F * limbSwingAmount
            : 0.0F;
        this.lfleg.yRot = newangle;
        this.rfleg.yRot = -newangle;
        this.lleg2.yRot = -newangle / 2.0F;
        this.rleg2.yRot = newangle / 2.0F;

        newangle = entity.getSinging() != 0
            ? Mth.cos(ageInTicks * 0.85F * this.wingspeed) * (float) Math.PI * 0.15F
            : 0.0F;
        this.jaw.xRot = newangle + 1.22F;

        if (entity.getDeltaMovement().y > 0.1 || entity.getDeltaMovement().y < -0.1) {
            this.lleg1.zRot = 2.44F;
            this.rleg1.zRot = -2.44F;
        } else {
            this.lleg1.zRot = 0.227F;
            this.rleg1.zRot = -0.227F;
        }

        this.lleg2.y = this.lleg1.y - (float) Math.cos(this.lleg1.zRot) * 9.0F;
        this.lleg2.z = this.lleg1.z + (float) Math.sin(this.lleg1.zRot) * 9.0F;
        this.rleg2.y = this.rleg1.y - (float) Math.cos(this.rleg1.zRot) * 9.0F;
        this.rleg2.z = this.rleg1.z + (float) Math.sin(this.rleg1.zRot) * 9.0F;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.jaw.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lfleg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rfleg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lleg1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rleg1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lleg2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rleg2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leye.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.reye.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
