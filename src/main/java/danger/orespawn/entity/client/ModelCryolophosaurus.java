package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.Cryolophosaurus;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

public class ModelCryolophosaurus extends EntityModel<Cryolophosaurus> {
    private float wingspeed = 1.0f;
    private final ModelPart shape1;
    private final ModelPart shape2;
    private final ModelPart shape3;
    private final ModelPart jaw;
    private final ModelPart shape5;
    private final ModelPart shape6;
    private final ModelPart shape7;
    private final ModelPart shape8;
    private final ModelPart shape9;
    private final ModelPart rightleg;
    private final ModelPart shape11;
    private final ModelPart rightleg2;
    private final ModelPart rightleg3;
    private final ModelPart rightleg4;
    private final ModelPart leftleg;
    private final ModelPart shape16;
    private final ModelPart shape17;
    private final ModelPart leftleg2;
    private final ModelPart leftleg3;
    private final ModelPart leftleg4;

    public ModelCryolophosaurus(ModelPart root) {
        this.shape1 = root.getChild("shape1");
        this.shape2 = root.getChild("shape2");
        this.shape3 = root.getChild("shape3");
        this.jaw = root.getChild("jaw");
        this.shape5 = root.getChild("shape5");
        this.shape6 = root.getChild("shape6");
        this.shape7 = root.getChild("shape7");
        this.shape8 = root.getChild("shape8");
        this.shape9 = root.getChild("shape9");
        this.rightleg = root.getChild("rightleg");
        this.shape11 = root.getChild("shape11");
        this.rightleg2 = root.getChild("rightleg2");
        this.rightleg3 = root.getChild("rightleg3");
        this.rightleg4 = root.getChild("rightleg4");
        this.leftleg = root.getChild("leftleg");
        this.shape16 = root.getChild("shape16");
        this.shape17 = root.getChild("shape17");
        this.leftleg2 = root.getChild("leftleg2");
        this.leftleg3 = root.getChild("leftleg3");
        this.leftleg4 = root.getChild("leftleg4");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("shape1", CubeListBuilder.create()
            .texOffs(0, 0).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 8.0F, 9.0F, 18.0F),
            PartPose.offset(0.0F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("shape2", CubeListBuilder.create()
            .texOffs(53, 0).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 6.0F, 4.0F, 11.0F),
            PartPose.offsetAndRotation(1.0F, -2.0F, -7.0F, -0.2268928F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("shape3", CubeListBuilder.create()
            .texOffs(0, 41).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 6.0F, 4.0F, 10.0F),
            PartPose.offset(1.0F, -2.0F, -15.0F));

        partdefinition.addOrReplaceChild("jaw", CubeListBuilder.create()
            .texOffs(0, 30).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 4.0F, 9.0F, 1.0F),
            PartPose.offsetAndRotation(2.0F, 1.0F, -8.0F, -1.256637F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("shape5", CubeListBuilder.create()
            .texOffs(91, 0).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 6.0F, 6.0F, 7.0F),
            PartPose.offset(1.0F, 0.0F, 18.0F));

        partdefinition.addOrReplaceChild("shape6", CubeListBuilder.create()
            .texOffs(36, 31).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 4.0F, 4.0F, 14.0F),
            PartPose.offset(2.0F, 0.0F, 25.0F));

        partdefinition.addOrReplaceChild("shape7", CubeListBuilder.create()
            .texOffs(43, 8).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 1.0F, 4.0F, 2.0F),
            PartPose.offsetAndRotation(-1.0F, 8.0F, 0.0F, 0.1919862F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("shape8", CubeListBuilder.create()
            .texOffs(9, 0).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 1.0F, 3.0F, 1.0F),
            PartPose.offsetAndRotation(-1.0F, 11.0F, 1.0F, -0.2617994F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("shape9", CubeListBuilder.create()
            .texOffs(0, 0).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 2.0F, 4.0F, 1.0F),
            PartPose.offsetAndRotation(3.0F, -4.0F, -9.0F, -0.9424778F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("rightleg", CubeListBuilder.create()
            .texOffs(0, 58).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 2.0F, 10.0F, 6.0F),
            PartPose.offsetAndRotation(-1.0F, 2.0F, 12.0F, -0.2792527F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("shape11", CubeListBuilder.create()
            .texOffs(39, 0).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 4.0F, 3.0F, 3.0F),
            PartPose.offset(2.0F, -1.0F, -18.0F));

        partdefinition.addOrReplaceChild("rightleg2", CubeListBuilder.create()
            .texOffs(0, 77).mirror()
            .addBox(0.0F, 7.0F, -5.0F, 2.0F, 10.0F, 3.0F),
            PartPose.offsetAndRotation(-1.0F, 2.0F, 12.0F, 0.3839724F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("rightleg3", CubeListBuilder.create()
            .texOffs(35, 31).mirror()
            .addBox(0.0F, 10.0F, 12.0F, 2.0F, 7.0F, 2.0F),
            PartPose.offsetAndRotation(-1.0F, 2.0F, 12.0F, -0.6806784F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("rightleg4", CubeListBuilder.create()
            .texOffs(68, 55).mirror()
            .addBox(0.0F, 20.0F, -5.0F, 2.0F, 2.0F, 6.0F),
            PartPose.offset(-1.0F, 2.0F, 12.0F));

        partdefinition.addOrReplaceChild("leftleg", CubeListBuilder.create()
            .texOffs(22, 58).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 2.0F, 10.0F, 6.0F),
            PartPose.offsetAndRotation(7.0F, 2.0F, 12.0F, -0.2792527F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("shape16", CubeListBuilder.create()
            .texOffs(0, 8).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 1.0F, 4.0F, 2.0F),
            PartPose.offsetAndRotation(8.0F, 8.0F, 0.0F, 0.1919862F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("shape17", CubeListBuilder.create()
            .texOffs(9, 9).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 1.0F, 3.0F, 1.0F),
            PartPose.offsetAndRotation(8.0F, 11.0F, 1.0F, -0.2617994F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("leftleg2", CubeListBuilder.create()
            .texOffs(16, 77).mirror()
            .addBox(0.0F, 7.0F, -5.0F, 2.0F, 10.0F, 3.0F),
            PartPose.offsetAndRotation(7.0F, 2.0F, 12.0F, 0.3839724F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("leftleg3", CubeListBuilder.create()
            .texOffs(67, 31).mirror()
            .addBox(0.0F, 10.0F, 12.0F, 2.0F, 7.0F, 2.0F),
            PartPose.offsetAndRotation(7.0F, 2.0F, 12.0F, -0.6806784F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("leftleg4", CubeListBuilder.create()
            .texOffs(47, 56).mirror()
            .addBox(0.0F, 20.0F, -5.0F, 2.0F, 2.0F, 6.0F),
            PartPose.offset(7.0F, 2.0F, 12.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(Cryolophosaurus entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float newangle = 0.0F;
        newangle = limbSwingAmount > 0.1F ? Mth.cos(ageInTicks * 1.3F * this.wingspeed) * (float) Math.PI * 0.25F * limbSwingAmount : 0.0F;
        this.rightleg.xRot = -0.2792527F + newangle;
        this.rightleg2.xRot = 0.384F + newangle;
        this.rightleg3.xRot = -0.68F + newangle;
        this.rightleg4.xRot = newangle;
        this.leftleg.xRot = -0.2792527F - newangle;
        this.leftleg2.xRot = 0.384F - newangle;
        this.leftleg3.xRot = -0.68F - newangle;
        this.leftleg4.xRot = -newangle;
        this.jaw.xRot = -1.15F + Mth.cos(ageInTicks * 0.28F) * (float) Math.PI * 0.1F;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.shape1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.shape2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.shape3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.jaw.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.shape5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.shape6.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.shape7.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.shape8.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.shape9.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rightleg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.shape11.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rightleg2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rightleg3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rightleg4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leftleg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.shape16.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.shape17.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leftleg2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leftleg3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leftleg4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
