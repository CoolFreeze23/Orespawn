package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.Baryonyx;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

public class ModelBaryonyx extends EntityModel<Baryonyx> {
    private float wingspeed = 1.0f;
    private final ModelPart shape27;
    private final ModelPart shape28;
    private final ModelPart shape29;
    private final ModelPart shape30;
    private final ModelPart shape31;
    private final ModelPart shape32;
    private final ModelPart shape33;
    private final ModelPart shape34;
    private final ModelPart shape35;
    private final ModelPart shape36;
    private final ModelPart shape37;
    private final ModelPart shape38;
    private final ModelPart shape39;
    private final ModelPart shape40;
    private final ModelPart shape41;
    private final ModelPart shape42;
    private final ModelPart shape43;
    private final ModelPart shape44;
    private final ModelPart shape45;
    private final ModelPart shape46;
    private final ModelPart shape47;
    private final ModelPart shape48;
    private final ModelPart shape49;
    private final ModelPart shape50;
    private final ModelPart shape51;
    private final ModelPart shape1;
    private final ModelPart shape2;
    private final ModelPart shape3;
    private final ModelPart shape4;
    private final ModelPart shape5;
    private final ModelPart shape6;
    private final ModelPart shape7;
    private final ModelPart shape8;
    private final ModelPart shape9;
    private final ModelPart shape10;
    private final ModelPart shape11;
    private final ModelPart shape12;
    private final ModelPart shape13;
    private final ModelPart shape14;
    private final ModelPart shape15;
    private final ModelPart shape16;
    private final ModelPart shape17;
    private final ModelPart shape18;
    private final ModelPart shape19;
    private final ModelPart shape20;
    private final ModelPart shape21;
    private final ModelPart shape22;
    private final ModelPart shape23;
    private final ModelPart shape24;
    private final ModelPart shape25;
    private final ModelPart shape26;
    private final ModelPart shape52;

    public ModelBaryonyx(ModelPart root) {
        this.shape27 = root.getChild("shape27");
        this.shape28 = root.getChild("shape28");
        this.shape29 = root.getChild("shape29");
        this.shape30 = root.getChild("shape30");
        this.shape31 = root.getChild("shape31");
        this.shape32 = root.getChild("shape32");
        this.shape33 = root.getChild("shape33");
        this.shape34 = root.getChild("shape34");
        this.shape35 = root.getChild("shape35");
        this.shape36 = root.getChild("shape36");
        this.shape37 = root.getChild("shape37");
        this.shape38 = root.getChild("shape38");
        this.shape39 = root.getChild("shape39");
        this.shape40 = root.getChild("shape40");
        this.shape41 = root.getChild("shape41");
        this.shape42 = root.getChild("shape42");
        this.shape43 = root.getChild("shape43");
        this.shape44 = root.getChild("shape44");
        this.shape45 = root.getChild("shape45");
        this.shape46 = root.getChild("shape46");
        this.shape47 = root.getChild("shape47");
        this.shape48 = root.getChild("shape48");
        this.shape49 = root.getChild("shape49");
        this.shape50 = root.getChild("shape50");
        this.shape51 = root.getChild("shape51");
        this.shape1 = root.getChild("shape1");
        this.shape2 = root.getChild("shape2");
        this.shape3 = root.getChild("shape3");
        this.shape4 = root.getChild("shape4");
        this.shape5 = root.getChild("shape5");
        this.shape6 = root.getChild("shape6");
        this.shape7 = root.getChild("shape7");
        this.shape8 = root.getChild("shape8");
        this.shape9 = root.getChild("shape9");
        this.shape10 = root.getChild("shape10");
        this.shape11 = root.getChild("shape11");
        this.shape12 = root.getChild("shape12");
        this.shape13 = root.getChild("shape13");
        this.shape14 = root.getChild("shape14");
        this.shape15 = root.getChild("shape15");
        this.shape16 = root.getChild("shape16");
        this.shape17 = root.getChild("shape17");
        this.shape18 = root.getChild("shape18");
        this.shape19 = root.getChild("shape19");
        this.shape20 = root.getChild("shape20");
        this.shape21 = root.getChild("shape21");
        this.shape22 = root.getChild("shape22");
        this.shape23 = root.getChild("shape23");
        this.shape24 = root.getChild("shape24");
        this.shape25 = root.getChild("shape25");
        this.shape26 = root.getChild("shape26");
        this.shape52 = root.getChild("shape52");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("shape27", CubeListBuilder.create()
            .texOffs(0, 0).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 0.0F, 2.0F, 1.0F),
            PartPose.offset(0.0F, -17.0F, -10.0F));

        partdefinition.addOrReplaceChild("shape28", CubeListBuilder.create()
            .texOffs(0, 0).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 0.0F, 2.0F, 1.0F),
            PartPose.offset(0.0F, -17.0F, -7.0F));

        partdefinition.addOrReplaceChild("shape29", CubeListBuilder.create()
            .texOffs(0, 0).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 0.0F, 2.0F, 1.0F),
            PartPose.offset(0.0F, -17.0F, -4.0F));

        partdefinition.addOrReplaceChild("shape30", CubeListBuilder.create()
            .texOffs(0, 0).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 0.0F, 2.0F, 1.0F),
            PartPose.offset(0.0F, -17.0F, -1.0F));

        partdefinition.addOrReplaceChild("shape31", CubeListBuilder.create()
            .texOffs(0, 0).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 0.0F, 2.0F, 1.0F),
            PartPose.offset(0.0F, -17.0F, 2.0F));

        partdefinition.addOrReplaceChild("shape32", CubeListBuilder.create()
            .texOffs(0, 0).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 0.0F, 2.0F, 1.0F),
            PartPose.offset(0.0F, -17.0F, 5.0F));

        partdefinition.addOrReplaceChild("shape33", CubeListBuilder.create()
            .texOffs(0, 0).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 0.0F, 2.0F, 1.0F),
            PartPose.offset(0.0F, -17.0F, 8.0F));

        partdefinition.addOrReplaceChild("shape34", CubeListBuilder.create()
            .texOffs(0, 0).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 0.0F, 2.0F, 1.0F),
            PartPose.offset(0.0F, -17.0F, 11.0F));

        partdefinition.addOrReplaceChild("shape35", CubeListBuilder.create()
            .texOffs(0, 0).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 0.0F, 2.0F, 1.0F),
            PartPose.offset(0.0F, -17.0F, 14.0F));

        partdefinition.addOrReplaceChild("shape36", CubeListBuilder.create()
            .texOffs(0, 0).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 0.0F, 2.0F, 1.0F),
            PartPose.offset(0.0F, -17.0F, 17.0F));

        partdefinition.addOrReplaceChild("shape37", CubeListBuilder.create()
            .texOffs(0, 0).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 0.0F, 2.0F, 1.0F),
            PartPose.offset(0.0F, -17.0F, 20.0F));

        partdefinition.addOrReplaceChild("shape38", CubeListBuilder.create()
            .texOffs(0, 0).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 0.0F, 2.0F, 1.0F),
            PartPose.offset(0.0F, -17.0F, 23.0F));

        partdefinition.addOrReplaceChild("shape39", CubeListBuilder.create()
            .texOffs(0, 0).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 0.0F, 2.0F, 1.0F),
            PartPose.offset(0.0F, -17.0F, 26.0F));

        partdefinition.addOrReplaceChild("shape40", CubeListBuilder.create()
            .texOffs(0, 0).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 0.0F, 2.0F, 1.0F),
            PartPose.offset(0.0F, -17.0F, 29.0F));

        partdefinition.addOrReplaceChild("shape41", CubeListBuilder.create()
            .texOffs(0, 0).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 0.0F, 2.0F, 1.0F),
            PartPose.offset(0.0F, -17.0F, 32.0F));

        partdefinition.addOrReplaceChild("shape42", CubeListBuilder.create()
            .texOffs(0, 0).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 0.0F, 2.0F, 1.0F),
            PartPose.offset(0.0F, -17.0F, 35.0F));

        partdefinition.addOrReplaceChild("shape43", CubeListBuilder.create()
            .texOffs(0, 0).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 0.0F, 2.0F, 1.0F),
            PartPose.offset(0.0F, -17.0F, 38.0F));

        partdefinition.addOrReplaceChild("shape44", CubeListBuilder.create()
            .texOffs(0, 0).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 0.0F, 2.0F, 1.0F),
            PartPose.offset(0.0F, -17.0F, 41.0F));

        partdefinition.addOrReplaceChild("shape45", CubeListBuilder.create()
            .texOffs(0, 0).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 0.0F, 2.0F, 1.0F),
            PartPose.offset(0.0F, -17.0F, 44.0F));

        partdefinition.addOrReplaceChild("shape46", CubeListBuilder.create()
            .texOffs(0, 0).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 0.0F, 2.0F, 1.0F),
            PartPose.offset(0.0F, -12.0F, -11.0F));

        partdefinition.addOrReplaceChild("shape47", CubeListBuilder.create()
            .texOffs(0, 0).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 0.0F, 2.0F, 1.0F),
            PartPose.offset(0.0F, -13.0F, -13.0F));

        partdefinition.addOrReplaceChild("shape48", CubeListBuilder.create()
            .texOffs(0, 0).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 0.0F, 2.0F, 1.0F),
            PartPose.offset(0.0F, -15.0F, -15.0F));

        partdefinition.addOrReplaceChild("shape49", CubeListBuilder.create()
            .texOffs(0, 0).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 0.0F, 2.0F, 1.0F),
            PartPose.offset(0.0F, -16.0F, -16.0F));

        partdefinition.addOrReplaceChild("shape50", CubeListBuilder.create()
            .texOffs(0, 0).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 1.0F),
            PartPose.offset(0.0F, -19.0F, -17.0F));

        partdefinition.addOrReplaceChild("shape51", CubeListBuilder.create()
            .texOffs(0, 0).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 1.0F),
            PartPose.offset(0.0F, -19.0F, -19.0F));

        partdefinition.addOrReplaceChild("shape1", CubeListBuilder.create()
            .texOffs(0, 0).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 10.0F, 17.0F, 25.0F),
            PartPose.offset(-5.0F, -15.0F, -10.0F));

        partdefinition.addOrReplaceChild("shape2", CubeListBuilder.create()
            .texOffs(0, 93).mirror()
            .addBox(-3.0F, 0.0F, -11.0F, 6.0F, 10.0F, 11.0F),
            PartPose.offsetAndRotation(0.0F, -10.0F, -6.0F, -0.1919862F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("shape3", CubeListBuilder.create()
            .texOffs(29, 110).mirror()
            .addBox(-2.0F, -9.0F, -8.0F, 4.0F, 9.0F, 8.0F),
            PartPose.offsetAndRotation(0.0F, -10.0F, -11.0F, 0.7504916F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("shape4", CubeListBuilder.create()
            .texOffs(54, 108).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 6.0F, 7.0F, 12.0F),
            PartPose.offset(-3.0F, -18.0F, -28.0F));

        partdefinition.addOrReplaceChild("shape5", CubeListBuilder.create()
            .texOffs(54, 86).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 3.0F, 6.0F, 15.0F),
            PartPose.offset(-1.5F, -17.5F, -43.0F));

        partdefinition.addOrReplaceChild("shape6", CubeListBuilder.create()
            .texOffs(0, 43).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 8.0F, 11.0F, 8.0F),
            PartPose.offset(-4.0F, -15.0F, 15.0F));

        partdefinition.addOrReplaceChild("shape7", CubeListBuilder.create()
            .texOffs(0, 63).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 6.0F, 6.0F, 23.0F),
            PartPose.offset(-3.0F, -15.0F, 23.0F));

        partdefinition.addOrReplaceChild("shape8", CubeListBuilder.create()
            .texOffs(47, 0).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 2.0F, 5.0F, 3.0F),
            PartPose.offset(5.0F, 0.0F, -7.0F));

        partdefinition.addOrReplaceChild("shape9", CubeListBuilder.create()
            .texOffs(49, 10).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 2.0F, 6.0F, 2.0F),
            PartPose.offsetAndRotation(5.1F, 3.0F, -6.0F, -0.3839724F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("shape10", CubeListBuilder.create()
            .texOffs(13, 17).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 2.0F, 4.0F, 3.0F),
            PartPose.offset(5.0F, 7.0F, -8.0F));

        partdefinition.addOrReplaceChild("shape11", CubeListBuilder.create()
            .texOffs(0, 17).mirror()
            .addBox(0.0F, 0.0F, -2.0F, 1.0F, 1.0F, 2.0F),
            PartPose.offset(5.0F, 8.0F, -8.0F));

        partdefinition.addOrReplaceChild("shape12", CubeListBuilder.create()
            .texOffs(0, 21).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F),
            PartPose.offset(5.0F, 9.0F, -11.0F));

        partdefinition.addOrReplaceChild("shape13", CubeListBuilder.create()
            .texOffs(95, 36).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 3.0F, 21.0F, 13.0F),
            PartPose.offset(5.0F, -15.0F, 2.0F));

        partdefinition.addOrReplaceChild("shape14", CubeListBuilder.create()
            .texOffs(36, 94).mirror()
            .addBox(0.0F, 0.0F, -3.0F, 3.0F, 5.0F, 3.0F),
            PartPose.offset(-1.5F, -17.0F, -43.0F));

        partdefinition.addOrReplaceChild("shape15", CubeListBuilder.create()
            .texOffs(113, 71).mirror()
            .addBox(0.0F, 18.0F, 8.0F, 3.0F, 18.0F, 4.0F),
            PartPose.offsetAndRotation(5.0F, -15.0F, 2.0F, -0.1745329F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("shape16", CubeListBuilder.create()
            .texOffs(13, 11).mirror()
            .addBox(-2.0F, 0.0F, 0.0F, 2.0F, 1.0F, 3.0F),
            PartPose.offset(5.0F, 10.0F, -8.0F));

        partdefinition.addOrReplaceChild("shape17", CubeListBuilder.create()
            .texOffs(0, 74).mirror()
            .addBox(0.0F, 35.0F, -1.0F, 3.0F, 3.0F, 6.0F),
            PartPose.offset(5.0F, -15.0F, 2.0F));

        partdefinition.addOrReplaceChild("shape18", CubeListBuilder.create()
            .texOffs(58, 0).mirror()
            .addBox(-2.0F, 0.0F, 0.0F, 2.0F, 5.0F, 3.0F),
            PartPose.offset(-5.0F, 0.0F, -7.0F));

        partdefinition.addOrReplaceChild("shape19", CubeListBuilder.create()
            .texOffs(59, 10).mirror()
            .addBox(-2.0F, 0.0F, 0.0F, 2.0F, 6.0F, 2.0F),
            PartPose.offsetAndRotation(-5.1F, 3.0F, -6.0F, -0.3839724F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("shape20", CubeListBuilder.create()
            .texOffs(71, 5).mirror()
            .addBox(-2.0F, 0.0F, 0.0F, 2.0F, 4.0F, 3.0F),
            PartPose.offset(-5.0F, 7.0F, -8.0F));

        partdefinition.addOrReplaceChild("shape21", CubeListBuilder.create()
            .texOffs(71, 0).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 2.0F, 1.0F, 3.0F),
            PartPose.offset(-5.0F, 10.0F, -8.0F));

        partdefinition.addOrReplaceChild("shape22", CubeListBuilder.create()
            .texOffs(0, 10).mirror()
            .addBox(-1.0F, 0.0F, -2.0F, 1.0F, 1.0F, 2.0F),
            PartPose.offset(-5.0F, 8.0F, -8.0F));

        partdefinition.addOrReplaceChild("shape23", CubeListBuilder.create()
            .texOffs(0, 14).mirror()
            .addBox(-1.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F),
            PartPose.offset(-5.0F, 9.0F, -11.0F));

        partdefinition.addOrReplaceChild("shape24", CubeListBuilder.create()
            .texOffs(95, 0).mirror()
            .addBox(-3.0F, 0.0F, 0.0F, 3.0F, 22.0F, 13.0F),
            PartPose.offset(-5.0F, -15.0F, 2.0F));

        partdefinition.addOrReplaceChild("shape25", CubeListBuilder.create()
            .texOffs(96, 71).mirror()
            .addBox(-3.0F, 18.0F, 8.0F, 3.0F, 18.0F, 4.0F),
            PartPose.offsetAndRotation(-5.0F, -15.0F, 2.0F, -0.1745329F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("shape26", CubeListBuilder.create()
            .texOffs(0, 64).mirror()
            .addBox(-3.0F, 35.0F, -1.0F, 3.0F, 3.0F, 6.0F),
            PartPose.offset(-5.0F, -15.0F, 2.0F));

        partdefinition.addOrReplaceChild("shape52", CubeListBuilder.create()
            .texOffs(9, 0).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 0.0F, 2.0F, 2.0F),
            PartPose.offset(0.0F, -19.0F, -30.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(Baryonyx entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float newangle = 0.0F;
        newangle = limbSwingAmount > 0.1F ? Mth.cos(ageInTicks * 1.3F * this.wingspeed) * (float) Math.PI * 0.15F * limbSwingAmount : 0.0F;
        this.shape24.xRot = newangle;
        this.shape25.xRot = -0.17F + newangle;
        this.shape26.xRot = newangle;
        this.shape13.xRot = -newangle;
        this.shape15.xRot = -0.17F - newangle;
        this.shape17.xRot = -newangle;
        newangle = Mth.cos(ageInTicks * 0.7F * this.wingspeed) * (float) Math.PI * 0.25F;
        this.shape21.zRot = newangle;
        this.shape16.zRot = -newangle;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.shape27.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.shape28.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.shape29.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.shape30.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.shape31.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.shape32.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.shape33.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.shape34.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.shape35.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.shape36.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.shape37.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.shape38.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.shape39.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.shape40.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.shape41.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.shape42.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.shape43.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.shape44.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.shape45.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.shape46.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.shape47.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.shape48.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.shape49.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.shape50.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.shape51.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.shape1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.shape2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.shape3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.shape4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.shape5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.shape6.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.shape7.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.shape8.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.shape9.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.shape10.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.shape11.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.shape12.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.shape13.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.shape14.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.shape15.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.shape16.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.shape17.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.shape18.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.shape19.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.shape20.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.shape21.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.shape22.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.shape23.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.shape24.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.shape25.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.shape26.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.shape52.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
