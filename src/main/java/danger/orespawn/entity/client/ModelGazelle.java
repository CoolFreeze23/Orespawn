package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.Gazelle;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

public class ModelGazelle extends EntityModel<Gazelle> {
    private final float wingspeed = 1.0F;
    private final ModelPart Chest;
    private final ModelPart lfleg1;
    private final ModelPart lrleg2;
    private final ModelPart lrleg1;
    private final ModelPart rfleg3;
    private final ModelPart rrleg2;
    private final ModelPart rrleg3;
    private final ModelPart rfleg2;
    private final ModelPart lrleg4;
    private final ModelPart tail;
    private final ModelPart lear;
    private final ModelPart rrleg1;
    private final ModelPart rfleg1;
    private final ModelPart lrleg3;
    private final ModelPart lfleg2;
    private final ModelPart rrleg5;
    private final ModelPart rrleg4;
    private final ModelPart lfleg3;
    private final ModelPart rfleg4;
    private final ModelPart lfleg4;
    private final ModelPart lrleg5;
    private final ModelPart Body;
    private final ModelPart neck;
    private final ModelPart la3;
    private final ModelPart throatfluff;
    private final ModelPart rear;
    private final ModelPart head;
    private final ModelPart ra1;
    private final ModelPart la1;
    private final ModelPart la2;
    private final ModelPart ra2;
    private final ModelPart ra3;
    private final ModelPart nose;
    private final ModelPart mouth;

    public ModelGazelle(ModelPart root) {
        this.Chest = root.getChild("Chest");
        this.lfleg1 = root.getChild("lfleg1");
        this.lrleg2 = root.getChild("lrleg2");
        this.lrleg1 = root.getChild("lrleg1");
        this.rfleg3 = root.getChild("rfleg3");
        this.rrleg2 = root.getChild("rrleg2");
        this.rrleg3 = root.getChild("rrleg3");
        this.rfleg2 = root.getChild("rfleg2");
        this.lrleg4 = root.getChild("lrleg4");
        this.tail = root.getChild("tail");
        this.lear = root.getChild("lear");
        this.rrleg1 = root.getChild("rrleg1");
        this.rfleg1 = root.getChild("rfleg1");
        this.lrleg3 = root.getChild("lrleg3");
        this.lfleg2 = root.getChild("lfleg2");
        this.rrleg5 = root.getChild("rrleg5");
        this.rrleg4 = root.getChild("rrleg4");
        this.lfleg3 = root.getChild("lfleg3");
        this.rfleg4 = root.getChild("rfleg4");
        this.lfleg4 = root.getChild("lfleg4");
        this.lrleg5 = root.getChild("lrleg5");
        this.Body = root.getChild("Body");
        this.neck = root.getChild("neck");
        this.la3 = root.getChild("la3");
        this.throatfluff = root.getChild("throatfluff");
        this.rear = root.getChild("rear");
        this.head = root.getChild("head");
        this.ra1 = root.getChild("ra1");
        this.la1 = root.getChild("la1");
        this.la2 = root.getChild("la2");
        this.ra2 = root.getChild("ra2");
        this.ra3 = root.getChild("ra3");
        this.nose = root.getChild("nose");
        this.mouth = root.getChild("mouth");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("Chest", CubeListBuilder.create()
            .texOffs(12, 57).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 5.0F, 2.0F, 3.0F),
            PartPose.offsetAndRotation(-2.5F, 8.0F, -6.0F, 2.342252F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("lfleg1", CubeListBuilder.create()
            .texOffs(0, 31).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 2.0F, 6.0F, 3.0F),
            PartPose.offsetAndRotation(2.0F, 6.0F, -6.0F, 0.2974289F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("lrleg2", CubeListBuilder.create()
            .texOffs(16, 49).mirror()
            .addBox(0.0F, 5.0F, -1.0F, 2.0F, 2.0F, 6.0F),
            PartPose.offsetAndRotation(2.0F, 4.0F, 3.0F, 0.1858931F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("lrleg1", CubeListBuilder.create()
            .texOffs(23, 31).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 2.0F, 6.0F, 3.0F),
            PartPose.offset(2.0F, 4.0F, 3.0F));

        partdefinition.addOrReplaceChild("rfleg3", CubeListBuilder.create()
            .texOffs(40, 49).mirror()
            .addBox(0.0F, 10.0F, 6.0F, 2.0F, 6.0F, 2.0F),
            PartPose.offsetAndRotation(-4.0F, 5.966667F, -6.0F, -0.4089647F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("rrleg2", CubeListBuilder.create()
            .texOffs(16, 49).mirror()
            .addBox(0.0F, 5.0F, -1.0F, 2.0F, 2.0F, 6.0F),
            PartPose.offsetAndRotation(-4.0F, 4.0F, 3.0F, 0.1858931F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("rrleg3", CubeListBuilder.create()
            .texOffs(32, 11).mirror()
            .addBox(0.0F, 4.0F, 5.0F, 2.0F, 12.0F, 2.0F),
            PartPose.offsetAndRotation(-4.0F, 3.966667F, 3.0F, -0.0743572F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("rfleg2", CubeListBuilder.create()
            .texOffs(24, 11).mirror()
            .addBox(0.0F, 2.0F, 2.0F, 2.0F, 12.0F, 2.0F),
            PartPose.offsetAndRotation(-4.0F, 5.966667F, -6.0F, -0.0743572F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("lrleg4", CubeListBuilder.create()
            .texOffs(32, 49).mirror()
            .addBox(0.0F, 11.0F, 9.5F, 2.0F, 6.0F, 2.0F),
            PartPose.offsetAndRotation(2.0F, 4.0F, 3.0F, -0.4089647F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("tail", CubeListBuilder.create()
            .texOffs(0, 49).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 4.0F, 4.0F, 4.0F),
            PartPose.offsetAndRotation(-2.0F, 0.0F, 4.0F, 0.9666439F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("lear", CubeListBuilder.create()
            .texOffs(18, 0).mirror()
            .addBox(-5.0F, -3.0F, 2.0F, 3.0F, 2.0F, 1.0F),
            PartPose.offsetAndRotation(0.0F, -9.0F, -6.0F, -0.1047198F, 1.570796F, 0.0F));

        partdefinition.addOrReplaceChild("rrleg1", CubeListBuilder.create()
            .texOffs(23, 31).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 2.0F, 6.0F, 3.0F),
            PartPose.offset(-4.0F, 4.0F, 3.0F));

        partdefinition.addOrReplaceChild("rfleg1", CubeListBuilder.create()
            .texOffs(0, 31).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 2.0F, 6.0F, 3.0F),
            PartPose.offsetAndRotation(-4.0F, 6.0F, -6.0F, 0.2974289F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("lrleg3", CubeListBuilder.create()
            .texOffs(32, 11).mirror()
            .addBox(0.0F, 4.0F, 5.0F, 2.0F, 12.0F, 2.0F),
            PartPose.offsetAndRotation(2.0F, 3.966667F, 3.0F, -0.0743572F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("lfleg2", CubeListBuilder.create()
            .texOffs(24, 11).mirror()
            .addBox(0.0F, 2.0F, 2.0F, 2.0F, 12.0F, 2.0F),
            PartPose.offsetAndRotation(2.0F, 5.966667F, -6.0F, -0.0743572F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("rrleg5", CubeListBuilder.create()
            .texOffs(0, 58)
            .addBox(-0.5F, 17.0F, 2.0F, 3.0F, 3.0F, 3.0F),
            PartPose.offset(-4.0F, 4.0F, 3.0F));

        partdefinition.addOrReplaceChild("rrleg4", CubeListBuilder.create()
            .texOffs(32, 49).mirror()
            .addBox(0.0F, 11.0F, 9.5F, 2.0F, 6.0F, 2.0F),
            PartPose.offsetAndRotation(-4.0F, 3.966667F, 3.0F, -0.4089647F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("lfleg3", CubeListBuilder.create()
            .texOffs(40, 49).mirror()
            .addBox(0.0F, 10.0F, 6.0F, 2.0F, 6.0F, 2.0F),
            PartPose.offsetAndRotation(2.0F, 5.966667F, -6.0F, -0.4089647F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("rfleg4", CubeListBuilder.create()
            .texOffs(0, 58).mirror()
            .addBox(-0.5F, 15.0F, -1.0F, 3.0F, 3.0F, 3.0F),
            PartPose.offset(-4.0F, 6.0F, -6.0F));

        partdefinition.addOrReplaceChild("lfleg4", CubeListBuilder.create()
            .texOffs(0, 58).mirror()
            .addBox(-0.5F, 15.0F, -1.0F, 3.0F, 3.0F, 3.0F),
            PartPose.offset(2.0F, 6.0F, -6.0F));

        partdefinition.addOrReplaceChild("lrleg5", CubeListBuilder.create()
            .texOffs(0, 58).mirror()
            .addBox(-0.5F, 17.0F, 2.0F, 3.0F, 3.0F, 3.0F),
            PartPose.offset(2.0F, 4.0F, 3.0F));

        partdefinition.addOrReplaceChild("Body", CubeListBuilder.create()
            .texOffs(0, 12).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 6.0F, 6.0F, 13.0F),
            PartPose.offsetAndRotation(-3.0F, 2.0F, -7.0F, 0.2230717F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("neck", CubeListBuilder.create()
            .texOffs(0, 31).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 5.0F, 5.0F, 13.0F),
            PartPose.offsetAndRotation(-2.5F, 6.0F, -8.0F, 1.524323F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("la3", CubeListBuilder.create()
            .texOffs(4, 12).mirror()
            .addBox(0.5F, -12.5F, 3.0F, 1.0F, 5.0F, 1.0F),
            PartPose.offsetAndRotation(0.0F, -9.0F, -6.0F, -0.3346075F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("throatfluff", CubeListBuilder.create()
            .texOffs(36, 41).mirror()
            .addBox(0.0F, -2.0F, 0.0F, 4.0F, 3.0F, 5.0F),
            PartPose.offsetAndRotation(-2.0F, 0.0F, -8.0F, 1.07818F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("rear", CubeListBuilder.create()
            .texOffs(18, 0).mirror()
            .addBox(-5.0F, -3.0F, -3.0F, 3.0F, 2.0F, 1.0F),
            PartPose.offsetAndRotation(0.0F, -9.0F, -6.0F, 0.1047198F, 1.570796F, 0.0F));

        partdefinition.addOrReplaceChild("head", CubeListBuilder.create()
            .texOffs(0, 0).mirror()
            .addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F),
            PartPose.offset(0.0F, -9.0F, -6.0F));

        partdefinition.addOrReplaceChild("ra1", CubeListBuilder.create()
            .texOffs(0, 12).mirror()
            .addBox(-1.5F, -5.0F, 0.0F, 1.0F, 4.0F, 1.0F),
            PartPose.offsetAndRotation(0.0F, -9.0F, -6.0F, -0.3717861F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("la1", CubeListBuilder.create()
            .texOffs(0, 12).mirror()
            .addBox(0.5F, -5.0F, 0.0F, 1.0F, 4.0F, 1.0F),
            PartPose.offsetAndRotation(0.0F, -9.0F, -6.0F, -0.3717861F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("la2", CubeListBuilder.create()
            .texOffs(0, 17).mirror()
            .addBox(0.5F, -8.5F, -3.0F, 1.0F, 5.0F, 1.0F),
            PartPose.offsetAndRotation(0.0F, -9.0F, -6.0F, -1.041001F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("ra2", CubeListBuilder.create()
            .texOffs(0, 17).mirror()
            .addBox(-1.5F, -8.5F, -3.0F, 1.0F, 5.0F, 1.0F),
            PartPose.offsetAndRotation(0.0F, -9.0F, -6.0F, -1.041001F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("ra3", CubeListBuilder.create()
            .texOffs(4, 12).mirror()
            .addBox(-1.5F, -12.5F, 3.0F, 1.0F, 5.0F, 1.0F),
            PartPose.offsetAndRotation(0.0F, -9.0F, -6.0F, -0.3346075F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("nose", CubeListBuilder.create()
            .texOffs(24, 0).mirror()
            .addBox(-2.5F, 0.0F, -7.0F, 5.0F, 3.0F, 5.0F),
            PartPose.offset(0.0F, -9.0F, -6.0F));

        partdefinition.addOrReplaceChild("mouth", CubeListBuilder.create()
            .texOffs(28, 57).mirror()
            .addBox(-2.0F, 2.0F, -6.0F, 4.0F, 2.0F, 5.0F),
            PartPose.offset(0.0F, -9.0F, -6.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(Gazelle entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float newangle = limbSwingAmount > 0.1F
            ? Mth.cos(ageInTicks * 1.1F * this.wingspeed) * (float) Math.PI * 0.12F * limbSwingAmount
            : 0.0F;

        this.lfleg1.xRot = 0.297F + newangle;
        this.lfleg2.xRot = -0.074F + newangle;
        this.lfleg3.xRot = -0.409F + newangle;
        this.lfleg4.xRot = newangle;
        this.rfleg1.xRot = 0.297F - newangle;
        this.rfleg2.xRot = -0.074F - newangle;
        this.rfleg3.xRot = -0.409F - newangle;
        this.rfleg4.xRot = -newangle;
        this.lrleg1.xRot = -newangle;
        this.lrleg2.xRot = 0.185F - newangle;
        this.lrleg3.xRot = -0.074F - newangle;
        this.lrleg4.xRot = -0.409F - newangle;
        this.lrleg5.xRot = -newangle;
        this.rrleg1.xRot = newangle;
        this.rrleg2.xRot = 0.185F + newangle;
        this.rrleg3.xRot = -0.074F + newangle;
        this.rrleg4.xRot = -0.409F + newangle;
        this.rrleg5.xRot = newangle;

        newangle = Mth.cos(ageInTicks * 0.5F) * (float) Math.PI * 0.02F;
        this.nose.yRot = this.head.yRot = (float) Math.toRadians(netHeadYaw) * 0.45F;
        this.mouth.yRot = this.head.yRot;
        this.lear.yRot = 1.57F + this.head.yRot + newangle;
        this.rear.yRot = 1.57F + this.head.yRot + newangle;
        this.la1.yRot = this.head.yRot;
        this.la2.yRot = this.head.yRot;
        this.la3.yRot = this.head.yRot;
        this.ra1.yRot = this.head.yRot;
        this.ra2.yRot = this.head.yRot;
        this.ra3.yRot = this.head.yRot;

        if (!entity.isCrouching()) {
            this.tail.xRot = 1.0F + Mth.cos(ageInTicks * 0.1F) * (float) Math.PI * 0.06F;
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.Chest.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lfleg1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lrleg2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lrleg1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rfleg3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rrleg2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rrleg3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rfleg2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lrleg4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tail.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lear.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rrleg1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rfleg1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lrleg3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lfleg2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rrleg5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rrleg4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lfleg3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rfleg4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lfleg4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lrleg5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.neck.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.la3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.throatfluff.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rear.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.head.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.ra1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.la1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.la2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.ra2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.ra3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.nose.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.mouth.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
