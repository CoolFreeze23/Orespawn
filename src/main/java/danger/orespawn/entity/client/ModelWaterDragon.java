package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.WaterDragon;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class ModelWaterDragon extends EntityModel<WaterDragon> {
    private final ModelPart Head;
    private final ModelPart neck1;
    private final ModelPart body1;
    private final ModelPart Leg8;
    private final ModelPart Leg2;
    private final ModelPart Leg7;
    private final ModelPart Leg1;
    private final ModelPart neck2;
    private final ModelPart neck3;
    private final ModelPart neck4;
    private final ModelPart body2;
    private final ModelPart body3;
    private final ModelPart body4;
    private final ModelPart tail1;
    private final ModelPart tailmiddle;
    private final ModelPart tailtop;
    private final ModelPart tailbottom;
    private final ModelPart nose;
    private final ModelPart headfin;
    private final ModelPart rightear;
    private final ModelPart leftear;
    private final ModelPart neackfin;
    private final ModelPart Bodyfin;
    private final ModelPart jaw;

    public ModelWaterDragon(ModelPart root) {
        this.Head = root.getChild("Head");
        this.neck1 = root.getChild("neck1");
        this.body1 = root.getChild("body1");
        this.Leg8 = root.getChild("Leg8");
        this.Leg2 = root.getChild("Leg2");
        this.Leg7 = root.getChild("Leg7");
        this.Leg1 = root.getChild("Leg1");
        this.neck2 = root.getChild("neck2");
        this.neck3 = root.getChild("neck3");
        this.neck4 = root.getChild("neck4");
        this.body2 = root.getChild("body2");
        this.body3 = root.getChild("body3");
        this.body4 = root.getChild("body4");
        this.tail1 = root.getChild("tail1");
        this.tailmiddle = root.getChild("tailmiddle");
        this.tailtop = root.getChild("tailtop");
        this.tailbottom = root.getChild("tailbottom");
        this.nose = root.getChild("nose");
        this.headfin = root.getChild("headfin");
        this.rightear = root.getChild("rightear");
        this.leftear = root.getChild("leftear");
        this.neackfin = root.getChild("neackfin");
        this.Bodyfin = root.getChild("Bodyfin");
        this.jaw = root.getChild("jaw");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("Head",
                CubeListBuilder.create().texOffs(79, 64).mirror().addBox(-4.0F, -4.0F, -8.0F, 7, 8, 8),
                PartPose.offset(0.0F, 0.0F, -3.0F));

        partdefinition.addOrReplaceChild("neck1",
                CubeListBuilder.create().texOffs(29, 70).mirror().addBox(-2.0F, 0.0F, -3.0F, 5, 5, 5),
                PartPose.offsetAndRotation(-1.0F, 4.0F, -5.0F, -0.1858931F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("body1",
                CubeListBuilder.create().texOffs(0, 33).mirror().addBox(-5.0F, -4.0F, -6.0F, 9, 9, 9),
                PartPose.offset(0.0F, 19.0F, 2.0F));

        partdefinition.addOrReplaceChild("Leg8",
                CubeListBuilder.create().texOffs(23, 25).mirror().addBox(0.0F, -1.0F, -1.0F, 9, 2, 3),
                PartPose.offsetAndRotation(3.0F, 22.0F, -2.0F, 0.0F, 0.5759587F, 0.1919862F));

        partdefinition.addOrReplaceChild("Leg2",
                CubeListBuilder.create().texOffs(80, 18).mirror().addBox(0.0F, -1.0F, -1.0F, 9, 2, 3),
                PartPose.offsetAndRotation(2.0F, 22.0F, 13.0F, 0.0F, -0.5759587F, 0.1919862F));

        partdefinition.addOrReplaceChild("Leg7",
                CubeListBuilder.create().texOffs(23, 18).mirror().addBox(-9.0F, -1.0F, -1.0F, 9, 2, 3),
                PartPose.offsetAndRotation(-4.0F, 22.0F, -1.0F, 0.0F, -0.5759587F, -0.1919862F));

        partdefinition.addOrReplaceChild("Leg1",
                CubeListBuilder.create().texOffs(80, 25).mirror().addBox(-9.0F, -1.0F, -2.0F, 9, 2, 3),
                PartPose.offsetAndRotation(-3.0F, 22.0F, 14.0F, 0.0F, 0.5759587F, -0.1919862F));

        partdefinition.addOrReplaceChild("neck2",
                CubeListBuilder.create().texOffs(0, 11).mirror().addBox(-2.0F, 0.0F, -2.0F, 5, 5, 5),
                PartPose.offsetAndRotation(-1.0F, 9.0F, -7.0F, 0.1115358F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("neck3",
                CubeListBuilder.create().texOffs(0, 22).mirror().addBox(-2.0F, 0.0F, -2.0F, 5, 5, 5),
                PartPose.offsetAndRotation(-1.0F, 14.0F, -6.0F, 0.4461433F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("neck4",
                CubeListBuilder.create().texOffs(26, 12).mirror().addBox(-3.0F, 0.0F, -2.0F, 5, 3, 3),
                PartPose.offsetAndRotation(0.0F, 18.0F, -4.0F, 1.226894F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("body2",
                CubeListBuilder.create().texOffs(0, 52).mirror().addBox(-5.0F, -5.0F, 0.0F, 7, 7, 9),
                PartPose.offset(1.0F, 21.0F, 5.0F));

        partdefinition.addOrReplaceChild("body3",
                CubeListBuilder.create().texOffs(0, 69).mirror().addBox(-3.0F, -3.0F, 0.0F, 5, 5, 7),
                PartPose.offset(0.0F, 20.0F, 14.0F));

        partdefinition.addOrReplaceChild("body4",
                CubeListBuilder.create().texOffs(0, 89).mirror().addBox(-1.0F, -1.0F, 0.0F, 3, 3, 5),
                PartPose.offset(-1.0F, 19.0F, 21.0F));

        partdefinition.addOrReplaceChild("tail1",
                CubeListBuilder.create().texOffs(0, 82).mirror().addBox(0.0F, 0.0F, 0.0F, 1, 2, 3),
                PartPose.offset(-1.0F, 19.0F, 25.0F));

        partdefinition.addOrReplaceChild("tailmiddle",
                CubeListBuilder.create().texOffs(55, 37).mirror().addBox(-1.0F, -6.0F, 0.0F, 2, 11, 9),
                PartPose.offset(0.0F, 19.0F, 28.0F));

        partdefinition.addOrReplaceChild("tailtop",
                CubeListBuilder.create().texOffs(82, 36).mirror().addBox(-1.0F, -11.0F, 0.0F, 2, 11, 9),
                PartPose.offsetAndRotation(0.0F, 14.0F, 28.0F, -0.6320364F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("tailbottom",
                CubeListBuilder.create().texOffs(56, 60).mirror().addBox(0.0F, 0.0F, 0.0F, 2, 11, 9),
                PartPose.offsetAndRotation(-1.0F, 23.0F, 28.0F, 0.6320361F, 0.0F, -0.0174533F));

        partdefinition.addOrReplaceChild("nose",
                CubeListBuilder.create().texOffs(54, 19).mirror().addBox(-3.0F, -2.0F, -5.0F, 5, 5, 5),
                PartPose.offset(0.0F, -2.0F, -11.0F));

        partdefinition.addOrReplaceChild("headfin",
                CubeListBuilder.create().texOffs(0, 99).mirror().addBox(0.0F, -5.0F, 0.0F, 0, 10, 9),
                PartPose.offsetAndRotation(0.0F, -4.0F, -6.0F, 0.1396263F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("rightear",
                CubeListBuilder.create().texOffs(38, 32).mirror().addBox(0.0F, 0.0F, 0.0F, 0, 5, 5),
                PartPose.offsetAndRotation(-4.0F, -2.0F, -5.0F, 0.0698132F, -0.418879F, 0.0F));

        partdefinition.addOrReplaceChild("leftear",
                CubeListBuilder.create().texOffs(38, 32).mirror().addBox(0.0F, 0.0F, 0.0F, 0, 5, 5),
                PartPose.offsetAndRotation(3.0F, -2.0F, -5.0F, 0.0698132F, 0.418879F, 0.0F));

        partdefinition.addOrReplaceChild("neackfin",
                CubeListBuilder.create().texOffs(42, 47).mirror().addBox(0.0F, -1.0F, 0.0F, 0, 5, 5),
                PartPose.offsetAndRotation(0.0F, 3.0F, -3.0F, -0.185895F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("Bodyfin",
                CubeListBuilder.create().texOffs(21, 91).mirror().addBox(0.0F, -6.0F, -3.0F, 0, 10, 9),
                PartPose.offsetAndRotation(0.0F, 15.0F, 2.0F, -0.0698132F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("jaw",
                CubeListBuilder.create().texOffs(76, 8).mirror().addBox(-2.0F, 0.0F, -5.0F, 5, 1, 5),
                PartPose.offset(-1.0F, 3.0F, -10.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(WaterDragon entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float newangle = 0.0f;
        float pi4 = 0.7853982f;
        float root13 = (float)Math.sqrt(13.0);
        float root20 = (float)Math.sqrt(20.0);
        newangle = (double)limbSwingAmount > 0.1 ? Mth.cos((float)(ageInTicks * 1.3f * limbSwingAmount)) * (float)Math.PI * 0.2f * limbSwingAmount : 0.0f;
        this.body3.yRot = Mth.cos((float)(ageInTicks * 1.3f * limbSwingAmount)) * (float)Math.PI * 0.4f * limbSwingAmount;
        this.body4.z = this.body3.z + (float)Math.cos(this.body3.yRot) * 7.0f;
        this.body4.x = this.body3.x - 1.0f + (float)Math.sin(this.body3.yRot) * 7.0f;
        this.body4.yRot = Mth.cos((float)(ageInTicks * 1.3f * limbSwingAmount - pi4)) * (float)Math.PI * 0.4f * limbSwingAmount;
        this.tail1.z = this.body4.z + (float)Math.cos(this.body4.yRot) * 5.0f;
        this.tail1.x = this.body4.x + (float)Math.sin(this.body4.yRot) * 5.0f;
        this.tail1.yRot = Mth.cos((float)(ageInTicks * 1.3f * limbSwingAmount - 2.0f * pi4)) * (float)Math.PI * 0.4f * limbSwingAmount;
        this.tailmiddle.z = this.tail1.z + (float)Math.cos(this.tail1.yRot) * 3.0f;
        this.tailmiddle.x = this.tail1.x + (float)Math.sin(this.tail1.yRot) * 3.0f;
        this.tailtop.yRot = this.tailmiddle.yRot = Mth.cos((float)(ageInTicks * 1.3f * limbSwingAmount - 3.0f * pi4)) * (float)Math.PI * 0.4f * limbSwingAmount;
        this.tailtop.z = this.tailmiddle.z;
        this.tailtop.x = this.tailmiddle.x;
        this.tailbottom.yRot = this.tailmiddle.yRot;
        this.tailbottom.z = this.tailmiddle.z;
        this.tailbottom.x = this.tailmiddle.x;
        this.Leg8.yRot = 0.58f + newangle;
        this.Leg2.yRot = -0.58f + newangle;
        this.Leg7.yRot = -0.58f - newangle;
        this.Leg1.yRot = 0.58f - newangle;
        newangle = Mth.cos((float)(ageInTicks * 0.8f * limbSwingAmount)) * (float)Math.PI * 0.1f;
        this.leftear.yRot = 0.62f + newangle;
        this.rightear.yRot = -0.62f - newangle;
        newangle = Mth.cos((float)(ageInTicks * 0.7f * limbSwingAmount)) * (float)Math.PI * 0.02f;
        if (entity.isInSittingPose()) {
        newangle = 0.0f;
        }
        this.Bodyfin.zRot = newangle;
        newangle = Mth.cos((float)(ageInTicks * 0.6f * limbSwingAmount)) * (float)Math.PI * 0.1f;
        if (entity.isInSittingPose()) {
        newangle = 0.0f;
        }
        this.neackfin.yRot = newangle;
        newangle = Mth.cos((float)(ageInTicks * 0.5f * limbSwingAmount)) * (float)Math.PI * 0.05f;
        if (entity.isInSittingPose()) {
        newangle = 0.0f;
        }
        this.headfin.yRot = newangle;
        this.jaw.xRot = entity.getAttacking() == 1 ? (newangle = Mth.cos((float)(ageInTicks * 1.2f * limbSwingAmount)) * (float)Math.PI * 0.25f) : (entity.getAttacking() == 2 ? 0.45f : -0.25f);
        this.Head.yRot = newangle = (float)Math.toRadians(netHeadYaw) * 0.75f;
        this.nose.yRot = newangle;
        this.nose.z = this.Head.z - (float)Math.cos(this.Head.yRot) * 8.0f;
        this.nose.x = this.Head.x - (float)Math.sin(this.Head.yRot) * 8.0f;
        this.jaw.yRot = newangle;
        this.jaw.z = this.Head.z - (float)Math.cos(this.Head.yRot) * 7.0f;
        this.jaw.x = this.Head.x - (float)Math.sin(this.Head.yRot) * 7.0f - 1.0f;
        this.headfin.yRot = newangle;
        this.headfin.z = this.Head.z - (float)Math.cos(this.Head.yRot) * 3.0f;
        this.headfin.x = this.Head.x - (float)Math.sin(this.Head.yRot) * 3.0f;
        this.leftear.yRot += newangle;
        this.leftear.z = this.Head.z - (float)Math.cos(this.Head.yRot - pi4) * root13;
        this.leftear.x = this.Head.x - (float)Math.sin(this.Head.yRot - pi4) * root13;
        this.rightear.yRot += newangle;
        this.rightear.z = this.Head.z - (float)Math.cos(this.Head.yRot + pi4) * root20;
        this.rightear.x = this.Head.x - (float)Math.sin(this.Head.yRot + pi4) * root20;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.Head.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.neck1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.body1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Leg8.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Leg2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Leg7.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Leg1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.neck2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.neck3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.neck4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.body2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.body3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.body4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tail1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tailmiddle.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tailtop.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tailbottom.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.nose.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.headfin.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rightear.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leftear.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.neackfin.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Bodyfin.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.jaw.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
