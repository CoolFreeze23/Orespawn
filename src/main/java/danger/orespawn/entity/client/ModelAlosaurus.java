package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.Alosaurus;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class ModelAlosaurus extends EntityModel<Alosaurus> {
    private final ModelPart headSpikeLeft;
    private final ModelPart headSpikeRight;
    private final ModelPart flankSpikeLeft;
    private final ModelPart flankSpikeRight;
    private final ModelPart torso;
    private final ModelPart hips;
    private final ModelPart tail;
    private final ModelPart neck;
    private final ModelPart snoutUpper;
    private final ModelPart snoutLower;
    private final ModelPart jaw;
    private final ModelPart leftleg;
    private final ModelPart leftleg2;
    private final ModelPart leftleg3;
    private final ModelPart leftForelimb;
    private final ModelPart rightleg;
    private final ModelPart rightleg2;
    private final ModelPart rightleg3;
    private final ModelPart leftleg4;
    private final ModelPart rightleg4;
    private final ModelPart rightForelimb;

    public ModelAlosaurus(ModelPart root) {
        this.headSpikeLeft = root.getChild("shape18");
        this.headSpikeRight = root.getChild("shape19");
        this.flankSpikeLeft = root.getChild("shape20");
        this.flankSpikeRight = root.getChild("shape21");
        this.torso = root.getChild("shape1");
        this.hips = root.getChild("shape2");
        this.tail = root.getChild("shape3");
        this.neck = root.getChild("shape4");
        this.snoutUpper = root.getChild("shape5");
        this.snoutLower = root.getChild("shape6");
        this.jaw = root.getChild("jaw");
        this.leftleg = root.getChild("leftleg");
        this.leftleg2 = root.getChild("leftleg2");
        this.leftleg3 = root.getChild("leftleg3");
        this.leftForelimb = root.getChild("shape11");
        this.rightleg = root.getChild("rightleg");
        this.rightleg2 = root.getChild("rightleg2");
        this.rightleg3 = root.getChild("rightleg3");
        this.leftleg4 = root.getChild("leftleg4");
        this.rightleg4 = root.getChild("rightleg4");
        this.rightForelimb = root.getChild("shape17");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("shape18",
                CubeListBuilder.create().texOffs(91, 114).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 2, 4, 5),
                PartPose.offsetAndRotation(3.3F, -25.0F, -27.0F, 0.5759587F, 0.0F, 0.5585054F));
        partdefinition.addOrReplaceChild("shape19",
                CubeListBuilder.create().texOffs(71, 114).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 2, 4, 5),
                PartPose.offsetAndRotation(-4.0F, -24.0F, -28.0F, 0.5759587F, 0.0F, -0.5585054F));
        partdefinition.addOrReplaceChild("shape20",
                CubeListBuilder.create().texOffs(91, 30).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 2, 7, 5),
                PartPose.offsetAndRotation(5.0F, -8.0F, -6.0F, 0.3839724F, 0.0F, 0.0F));
        partdefinition.addOrReplaceChild("shape21",
                CubeListBuilder.create().texOffs(93, 46).mirror()
                        .addBox(-2.0F, 0.0F, 0.0F, 2, 7, 5),
                PartPose.offsetAndRotation(-4.0F, -8.0F, -6.0F, 0.3839724F, 0.0F, 0.0F));
        partdefinition.addOrReplaceChild("shape1",
                CubeListBuilder.create().texOffs(0, 0).mirror()
                        .addBox(-7.0F, 0.0F, 0.0F, 10, 18, 31),
                PartPose.offset(2.5F, -19.0F, -8.0F));
        partdefinition.addOrReplaceChild("shape2",
                CubeListBuilder.create().texOffs(62, 0).mirror()
                        .addBox(-5.0F, 0.0F, 0.0F, 10, 11, 11),
                PartPose.offset(0.5F, -19.0F, 23.0F));
        partdefinition.addOrReplaceChild("shape3",
                CubeListBuilder.create().texOffs(10, 54).mirror()
                        .addBox(-3.0F, 0.0F, 0.0F, 7, 7, 25),
                PartPose.offset(0.0F, -19.0F, 34.0F));
        partdefinition.addOrReplaceChild("shape4",
                CubeListBuilder.create().texOffs(68, 88).mirror()
                        .addBox(-5.0F, 0.0F, 0.0F, 8, 9, 16),
                PartPose.offsetAndRotation(1.5F, -25.0F, -16.0F, -0.4014257F, 0.0F, 0.0F));
        partdefinition.addOrReplaceChild("shape5",
                CubeListBuilder.create().texOffs(75, 65).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 9, 9, 12),
                PartPose.offset(-4.0F, -25.0F, -27.0F));
        partdefinition.addOrReplaceChild("shape6",
                CubeListBuilder.create().texOffs(0, 50).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 7, 9, 9),
                PartPose.offset(-3.0F, -25.0F, -36.0F));
        partdefinition.addOrReplaceChild("jaw",
                CubeListBuilder.create().texOffs(0, 86).mirror()
                        .addBox(-5.0F, 0.0F, -10.0F, 7, 1, 13),
                PartPose.offsetAndRotation(2.0F, -15.0F, -24.0F, 0.5201081F, 0.0F, 0.0F));
        partdefinition.addOrReplaceChild("leftleg",
                CubeListBuilder.create().texOffs(0, 0).mirror()
                        .addBox(-1.0F, 0.0F, 0.0F, 3, 16, 10),
                PartPose.offsetAndRotation(6.0F, -10.0F, 11.0F, -0.1745329F, 0.0F, 0.0F));
        partdefinition.addOrReplaceChild("leftleg2",
                CubeListBuilder.create().texOffs(0, 106).mirror()
                        .addBox(-1.0F, 12.0F, -8.0F, 3, 15, 5),
                PartPose.offsetAndRotation(6.0F, -10.0F, 11.0F, 0.5061455F, 0.0F, 0.0F));
        partdefinition.addOrReplaceChild("leftleg3",
                CubeListBuilder.create().texOffs(112, 89).mirror()
                        .addBox(-1.0F, 19.0F, 16.0F, 3, 9, 3),
                PartPose.offsetAndRotation(6.0F, -10.0F, 11.0F, -0.4014257F, 0.0F, 0.0F));
        partdefinition.addOrReplaceChild("shape11",
                CubeListBuilder.create().texOffs(0, 72).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 2, 10, 2),
                PartPose.offsetAndRotation(5.0F, -5.0F, -3.0F, -0.5235988F, 0.0F, 0.0F));
        partdefinition.addOrReplaceChild("rightleg",
                CubeListBuilder.create().texOffs(54, 51).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 3, 16, 10),
                PartPose.offsetAndRotation(-7.0F, -10.0F, 11.0F, -0.1745329F, 0.0F, 0.0F));
        partdefinition.addOrReplaceChild("rightleg2",
                CubeListBuilder.create().texOffs(23, 106).mirror()
                        .addBox(0.0F, 12.0F, -8.0F, 3, 15, 5),
                PartPose.offsetAndRotation(-7.0F, -10.0F, 11.0F, 0.5061455F, 0.0F, 0.0F));
        partdefinition.addOrReplaceChild("rightleg3",
                CubeListBuilder.create().texOffs(70, 90).mirror()
                        .addBox(0.0F, 19.0F, 16.0F, 3, 9, 3),
                PartPose.offsetAndRotation(-7.0F, -10.0F, 11.0F, -0.4014257F, 0.0F, 0.0F));
        partdefinition.addOrReplaceChild("leftleg4",
                CubeListBuilder.create().texOffs(42, 113).mirror()
                        .addBox(-1.0F, 31.0F, -1.0F, 3, 3, 8),
                PartPose.offset(6.0F, -10.0F, 11.0F));
        partdefinition.addOrReplaceChild("rightleg4",
                CubeListBuilder.create().texOffs(44, 93).mirror()
                        .addBox(0.0F, 31.0F, -1.0F, 3, 3, 8),
                PartPose.offset(-7.0F, -10.0F, 11.0F));
        partdefinition.addOrReplaceChild("shape17",
                CubeListBuilder.create().texOffs(112, 60).mirror()
                        .addBox(-2.0F, 0.0F, 0.0F, 2, 10, 2),
                PartPose.offsetAndRotation(-4.0F, -3.533333F, -3.0F, -0.5235988F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(Alosaurus entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float newangle = 0.0f;
        if (limbSwingAmount > 0.1f) {
            newangle = Mth.cos(ageInTicks * 1.3f * 0.22f) * (float) Math.PI * 0.25f * limbSwingAmount;
        }
        this.rightleg.xRot = -0.174f + newangle;
        this.rightleg2.xRot = 0.506f + newangle;
        this.rightleg3.xRot = -0.401f + newangle;
        this.rightleg4.xRot = newangle;
        this.leftleg.xRot = -0.174f - newangle;
        this.leftleg2.xRot = 0.506f - newangle;
        this.leftleg3.xRot = -0.401f - newangle;
        this.leftleg4.xRot = -newangle;
        this.jaw.xRot = entity.getAttacking() != 0
                ? 0.52f + Mth.cos(ageInTicks * 0.45f) * (float) Math.PI * 0.18f
                : 0.1f;
        this.rightForelimb.xRot = -0.523f + Mth.cos(ageInTicks * 0.1f) * (float) Math.PI * 0.05f;
        this.leftForelimb.xRot = -0.523f + Mth.cos(ageInTicks * 0.1f) * (float) Math.PI * 0.05f;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.headSpikeLeft.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.headSpikeRight.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.flankSpikeLeft.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.flankSpikeRight.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.torso.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.hips.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tail.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.neck.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.snoutUpper.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.snoutLower.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.jaw.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leftleg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leftleg2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leftleg3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leftForelimb.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rightleg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rightleg2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rightleg3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leftleg4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rightleg4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rightForelimb.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
