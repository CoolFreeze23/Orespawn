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
    private final ModelPart body;
    private final ModelPart neck;
    private final ModelPart head;
    private final ModelPart jaw;
    private final ModelPart hips;
    private final ModelPart tail;
    private final ModelPart rightArm;
    private final ModelPart rightForearm;
    private final ModelPart crest;
    private final ModelPart rightleg;
    private final ModelPart snout;
    private final ModelPart rightleg2;
    private final ModelPart rightleg3;
    private final ModelPart rightleg4;
    private final ModelPart leftleg;
    private final ModelPart leftArm;
    private final ModelPart leftForearm;
    private final ModelPart leftleg2;
    private final ModelPart leftleg3;
    private final ModelPart leftleg4;

    public ModelCryolophosaurus(ModelPart root) {
        this.body = root.getChild("shape1");
        this.neck = root.getChild("shape2");
        this.head = root.getChild("shape3");
        this.jaw = root.getChild("jaw");
        this.hips = root.getChild("shape5");
        this.tail = root.getChild("shape6");
        this.rightArm = root.getChild("shape7");
        this.rightForearm = root.getChild("shape8");
        this.crest = root.getChild("shape9");
        this.rightleg = root.getChild("rightleg");
        this.snout = root.getChild("shape11");
        this.rightleg2 = root.getChild("rightleg2");
        this.rightleg3 = root.getChild("rightleg3");
        this.rightleg4 = root.getChild("rightleg4");
        this.leftleg = root.getChild("leftleg");
        this.leftArm = root.getChild("shape16");
        this.leftForearm = root.getChild("shape17");
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
        this.body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.neck.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.head.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.jaw.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.hips.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tail.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rightArm.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rightForearm.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.crest.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rightleg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.snout.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rightleg2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rightleg3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rightleg4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leftleg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leftArm.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leftForearm.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leftleg2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leftleg3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leftleg4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
