package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.EasterBunny;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class ModelEasterBunny extends EntityModel<EasterBunny> {
    private final ModelPart body;
    private final ModelPart tail;
    private final ModelPart lfoot;
    private final ModelPart lleg;
    private final ModelPart upperbody;
    private final ModelPart head;
    private final ModelPart nose;
    private final ModelPart lear;
    private final ModelPart lpaw;
    private final ModelPart rleg;
    private final ModelPart rfoot;
    private final ModelPart rear;
    private final ModelPart rpaw;

    public ModelEasterBunny(ModelPart root) {
        this.body = root.getChild("body");
        this.tail = root.getChild("tail");
        this.lfoot = root.getChild("lfoot");
        this.lleg = root.getChild("lleg");
        this.upperbody = root.getChild("upperbody");
        this.head = root.getChild("head");
        this.nose = root.getChild("nose");
        this.lear = root.getChild("lear");
        this.lpaw = root.getChild("lpaw");
        this.rleg = root.getChild("rleg");
        this.rfoot = root.getChild("rfoot");
        this.rear = root.getChild("rear");
        this.rpaw = root.getChild("rpaw");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 44).mirror().addBox(-3.0F, 0.0F, -3.0F, 6, 6, 7),
                PartPose.offset(0.0F, 17.0F, 0.0F));

        partdefinition.addOrReplaceChild("tail",
                CubeListBuilder.create().texOffs(0, 58).mirror().addBox(-2.0F, 0.0F, -2.0F, 4, 4, 4),
                PartPose.offset(0.0F, 19.0F, 6.0F));

        partdefinition.addOrReplaceChild("lfoot",
                CubeListBuilder.create().texOffs(0, 30).mirror().addBox(-1.0F, 2.0F, -5.0F, 3, 1, 7),
                PartPose.offset(3.0F, 21.0F, 1.0F));

        partdefinition.addOrReplaceChild("lleg",
                CubeListBuilder.create().texOffs(0, 20).mirror().addBox(0.0F, -2.0F, -2.0F, 1, 4, 5),
                PartPose.offset(3.0F, 21.0F, 1.0F));

        partdefinition.addOrReplaceChild("upperbody",
                CubeListBuilder.create().texOffs(42, 27).mirror().addBox(-2.0F, 0.0F, -2.0F, 4, 1, 5),
                PartPose.offset(0.0F, 16.0F, -1.0F));

        partdefinition.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(40, 17).mirror().addBox(-2.5F, 0.0F, -2.0F, 5, 4, 5),
                PartPose.offset(0.0F, 12.0F, -2.0F));

        partdefinition.addOrReplaceChild("nose",
                CubeListBuilder.create().texOffs(44, 9).mirror().addBox(-1.0F, -1.0F, 0.0F, 2, 2, 1),
                PartPose.offset(0.0F, 15.0F, -5.0F));

        partdefinition.addOrReplaceChild("lear",
                CubeListBuilder.create().texOffs(54, 0).mirror().addBox(0.0F, -10.0F, -1.0F, 1, 10, 3),
                PartPose.offsetAndRotation(2.0F, 13.0F, -1.0F, -0.2268928F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("lpaw",
                CubeListBuilder.create().texOffs(6, 7).mirror().addBox(0.0F, 0.0F, 0.0F, 1, 3, 1),
                PartPose.offset(0.5F, 19.0F, -4.0F));

        partdefinition.addOrReplaceChild("rleg",
                CubeListBuilder.create().texOffs(21, 20).mirror().addBox(0.0F, -2.0F, -2.0F, 1, 4, 5),
                PartPose.offset(-4.0F, 21.0F, 1.0F));

        partdefinition.addOrReplaceChild("rfoot",
                CubeListBuilder.create().texOffs(21, 30).mirror().addBox(-1.0F, 2.0F, -5.0F, 3, 1, 7),
                PartPose.offset(-4.0F, 21.0F, 1.0F));

        partdefinition.addOrReplaceChild("rear",
                CubeListBuilder.create().texOffs(32, 0).mirror().addBox(0.0F, -10.0F, -1.0F, 1, 10, 3),
                PartPose.offsetAndRotation(-3.0F, 13.0F, -1.0F, -0.418879F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("rpaw",
                CubeListBuilder.create().texOffs(0, 7).mirror().addBox(0.0F, 0.0F, 0.0F, 1, 3, 1),
                PartPose.offset(-1.5F, 19.0F, -4.0F));

        return LayerDefinition.create(meshdefinition, 64, 128);
    }

    @Override
    public void setupAnim(EasterBunny entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float newangle = 0.0f;
        float newangle2 = 0.0f;
        if ((double)limbSwingAmount > 0.1) {
        newangle = Mth.cos((float)(ageInTicks * 2.6f * limbSwingAmount)) * (float)Math.PI * 0.15f * limbSwingAmount;
        newangle2 = Mth.cos((float)(ageInTicks * 1.3f * limbSwingAmount)) * (float)Math.PI * 0.1f * limbSwingAmount;
        } else {
        newangle = 0.0f;
        newangle2 = Mth.cos((float)(ageInTicks * 1.3f * limbSwingAmount)) * (float)Math.PI * 0.01f;
        }
        this.lleg.xRot = this.lfoot.xRot = newangle;
        this.rleg.xRot = this.rfoot.xRot = -newangle;
        this.lear.xRot = -0.226f + newangle2;
        this.rear.xRot = -0.418f - newangle2;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tail.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lfoot.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lleg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.upperbody.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.head.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.nose.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lear.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lpaw.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rleg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rfoot.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rear.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rpaw.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
