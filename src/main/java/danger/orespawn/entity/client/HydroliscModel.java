package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.EntityHydrolisc;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class HydroliscModel extends EntityModel<EntityHydrolisc> {
    private final ModelPart tail2;
    private final ModelPart tail3;
    private final ModelPart body2;
    private final ModelPart lb2;
    private final ModelPart lb1;
    private final ModelPart spine3;
    private final ModelPart spine4;
    private final ModelPart rb1;
    private final ModelPart rb2;
    private final ModelPart spine1;
    private final ModelPart spine2;
    private final ModelPart lb3;
    private final ModelPart rb3;
    private final ModelPart body1;
    private final ModelPart body0;
    private final ModelPart lf1;
    private final ModelPart rf1;
    private final ModelPart rb6;
    private final ModelPart rb4;
    private final ModelPart rb5;
    private final ModelPart lb6;
    private final ModelPart lb5;
    private final ModelPart lb4;
    private final ModelPart head3;
    private final ModelPart feather3;
    private final ModelPart feather1;
    private final ModelPart feather2;
    private final ModelPart head1;
    private final ModelPart rf2;
    private final ModelPart rf3;
    private final ModelPart rf4;
    private final ModelPart rf5;
    private final ModelPart rf6;
    private final ModelPart lf2;
    private final ModelPart lf3;
    private final ModelPart lf4;
    private final ModelPart lf5;
    private final ModelPart lf6;
    private final ModelPart head2;
    private final ModelPart tail1;

    public HydroliscModel(ModelPart root) {
        this.tail2 = root.getChild("tail2");
        this.tail3 = root.getChild("tail3");
        this.body2 = root.getChild("body2");
        this.lb2 = root.getChild("lb2");
        this.lb1 = root.getChild("lb1");
        this.spine3 = root.getChild("spine3");
        this.spine4 = root.getChild("spine4");
        this.rb1 = root.getChild("rb1");
        this.rb2 = root.getChild("rb2");
        this.spine1 = root.getChild("spine1");
        this.spine2 = root.getChild("spine2");
        this.lb3 = root.getChild("lb3");
        this.rb3 = root.getChild("rb3");
        this.body1 = root.getChild("body1");
        this.body0 = root.getChild("body0");
        this.lf1 = root.getChild("lf1");
        this.rf1 = root.getChild("rf1");
        this.rb6 = root.getChild("rb6");
        this.rb4 = root.getChild("rb4");
        this.rb5 = root.getChild("rb5");
        this.lb6 = root.getChild("lb6");
        this.lb5 = root.getChild("lb5");
        this.lb4 = root.getChild("lb4");
        this.head3 = root.getChild("head3");
        this.feather3 = root.getChild("feather3");
        this.feather1 = root.getChild("feather1");
        this.feather2 = root.getChild("feather2");
        this.head1 = root.getChild("head1");
        this.rf2 = root.getChild("rf2");
        this.rf3 = root.getChild("rf3");
        this.rf4 = root.getChild("rf4");
        this.rf5 = root.getChild("rf5");
        this.rf6 = root.getChild("rf6");
        this.lf2 = root.getChild("lf2");
        this.lf3 = root.getChild("lf3");
        this.lf4 = root.getChild("lf4");
        this.lf5 = root.getChild("lf5");
        this.lf6 = root.getChild("lf6");
        this.head2 = root.getChild("head2");
        this.tail1 = root.getChild("tail1");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("tail2",
                CubeListBuilder.create().texOffs(29, 3).mirror()
                        .addBox(-1.0F, 0.0F, -0.8F, 2, 8, 2),
                PartPose.offsetAndRotation(1.0F, 20.0F, 13.53333F, 1.392442F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("tail3",
                CubeListBuilder.create().texOffs(39, 0).mirror()
                        .addBox(-1.0F, -1.0F, -2.0F, 2, 8, 2),
                PartPose.offsetAndRotation(1.0F, 20.0F, 21.0F, 1.72705F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("body2",
                CubeListBuilder.create().texOffs(0, 99).mirror()
                        .addBox(-2.0F, 14.0F, 0.0F, 6, 4, 10),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.0523599F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("lb2",
                CubeListBuilder.create().texOffs(45, 13).mirror()
                        .addBox(0.0F, 0.0F, 3.0F, 3, 2, 5),
                PartPose.offsetAndRotation(5.0F, 15.0F, 0.0F, -0.4886922F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("lb1",
                CubeListBuilder.create().texOffs(46, 22).mirror()
                        .addBox(-1.0F, 0.0F, 0.0F, 4, 3, 3),
                PartPose.offset(5.0F, 15.0F, 0.0F));

        partdefinition.addOrReplaceChild("spine3",
                CubeListBuilder.create().texOffs(11, 31).mirror()
                        .addBox(-1.0F, -5.0F, 0.0F, 2, 6, 2),
                PartPose.offsetAndRotation(1.0F, 14.0F, 6.0F, -1.117011F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("spine4",
                CubeListBuilder.create().texOffs(0, 30).mirror()
                        .addBox(-1.0F, -10.5F, -1.0F, 2, 6, 2),
                PartPose.offsetAndRotation(1.0F, 14.0F, 6.0F, -1.343904F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("rb1",
                CubeListBuilder.create().texOffs(46, 22).mirror()
                        .addBox(-4.0F, 0.0F, 0.0F, 4, 3, 3),
                PartPose.offset(-2.0F, 15.0F, 0.0F));

        partdefinition.addOrReplaceChild("rb2",
                CubeListBuilder.create().texOffs(45, 13).mirror()
                        .addBox(-4.0F, 0.0F, 2.0F, 3, 2, 5),
                PartPose.offsetAndRotation(-2.0F, 15.0F, 0.0F, -0.4886922F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("spine1",
                CubeListBuilder.create().texOffs(33, 19).mirror()
                        .addBox(-1.0F, -5.0F, 0.0F, 2, 6, 2),
                PartPose.offsetAndRotation(1.0F, 14.0F, 0.0F, -0.8552113F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("spine2",
                CubeListBuilder.create().texOffs(21, 19).mirror()
                        .addBox(-1.0F, -10.5F, -1.5F, 2, 6, 2),
                PartPose.offsetAndRotation(1.0F, 14.0F, 0.0F, -1.169371F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("lb3",
                CubeListBuilder.create().texOffs(0, 58).mirror()
                        .addBox(0.0F, -8.0F, -2.0F, 3, 2, 6),
                PartPose.offsetAndRotation(5.0F, 15.0F, 0.0F, -2.347623F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("rb3",
                CubeListBuilder.create().texOffs(0, 58).mirror()
                        .addBox(-4.0F, -8.0F, -2.0F, 3, 2, 6),
                PartPose.offsetAndRotation(-2.0F, 15.0F, 0.0F, -2.347623F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("body1",
                CubeListBuilder.create().texOffs(0, 79).mirror()
                        .addBox(-2.0F, 16.0F, -7.0F, 4, 2, 5),
                PartPose.offset(1.0F, -1.0F, 2.0F));

        partdefinition.addOrReplaceChild("body0",
                CubeListBuilder.create().texOffs(0, 0).mirror()
                        .addBox(-1.0F, 14.0F, -13.0F, 4, 3, 10),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0523599F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("lf1",
                CubeListBuilder.create().texOffs(45, 32).mirror()
                        .addBox(-1.0F, 0.0F, -2.0F, 4, 3, 3),
                PartPose.offset(4.0F, 14.0F, -7.0F));

        partdefinition.addOrReplaceChild("rf1",
                CubeListBuilder.create().texOffs(45, 32).mirror()
                        .addBox(-3.0F, 0.0F, -2.0F, 4, 3, 3),
                PartPose.offset(-2.0F, 14.0F, -7.0F));

        partdefinition.addOrReplaceChild("rb6",
                CubeListBuilder.create().texOffs(30, 39).mirror()
                        .addBox(-3.5F, 7.0F, 2.0F, 2, 3, 1),
                PartPose.offsetAndRotation(-2.0F, 15.0F, 0.0F, 0.1745329F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("rb4",
                CubeListBuilder.create().texOffs(20, 39).mirror()
                        .addBox(-2.0F, 3.0F, 6.0F, 1, 4, 1),
                PartPose.offsetAndRotation(-2.0F, 15.0F, 0.0F, -0.6283185F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("rb5",
                CubeListBuilder.create().texOffs(20, 39).mirror()
                        .addBox(-4.0F, 3.0F, 6.0F, 1, 4, 1),
                PartPose.offsetAndRotation(-2.0F, 15.0F, 0.0F, -0.6283185F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("lb6",
                CubeListBuilder.create().texOffs(30, 39).mirror()
                        .addBox(0.5F, 7.0F, 2.0F, 2, 3, 1),
                PartPose.offsetAndRotation(5.0F, 15.0F, 0.0F, 0.1745329F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("lb5",
                CubeListBuilder.create().texOffs(20, 39).mirror()
                        .addBox(2.0F, 3.0F, 6.0F, 1, 4, 1),
                PartPose.offsetAndRotation(5.0F, 15.0F, 0.0F, -0.6283185F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("lb4",
                CubeListBuilder.create().texOffs(20, 39).mirror()
                        .addBox(0.0F, 3.0F, 6.0F, 1, 4, 1),
                PartPose.offsetAndRotation(5.0F, 15.0F, 0.0F, -0.6283185F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("head3",
                CubeListBuilder.create().texOffs(38, 50).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 4, 2, 8),
                PartPose.offsetAndRotation(-1.0F, 15.0F, -13.0F, 0.5235988F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("feather3",
                CubeListBuilder.create().texOffs(25, 117).mirror()
                        .addBox(0.0F, 0.0F, 1.0F, 1, 2, 9),
                PartPose.offsetAndRotation(1.0F, 12.0F, -8.0F, 0.3490659F, 0.2617994F, 0.0F));

        partdefinition.addOrReplaceChild("feather1",
                CubeListBuilder.create().texOffs(34, 100).mirror()
                        .addBox(0.0F, 0.0F, 1.0F, 1, 2, 9),
                PartPose.offsetAndRotation(0.0F, 12.0F, -8.0F, 0.3490659F, -0.2617994F, 0.0F));

        partdefinition.addOrReplaceChild("feather2",
                CubeListBuilder.create().texOffs(0, 116).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 1, 2, 10),
                PartPose.offsetAndRotation(0.5F, 11.0F, -6.0F, 0.3490659F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("head1",
                CubeListBuilder.create().texOffs(38, 41).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 4, 3, 4),
                PartPose.offsetAndRotation(-1.0F, 15.0F, -15.0F, 0.1396263F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("rf2",
                CubeListBuilder.create().texOffs(19, 58).mirror()
                        .addBox(-3.0F, 0.0F, 0.0F, 3, 3, 6),
                PartPose.offsetAndRotation(-2.0F, 14.0F, -7.0F, -0.4886922F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("rf3",
                CubeListBuilder.create().texOffs(19, 47).mirror()
                        .addBox(-3.0F, -7.0F, 0.0F, 3, 3, 6),
                PartPose.offsetAndRotation(-2.0F, 14.0F, -7.0F, -2.347623F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("rf4",
                CubeListBuilder.create().texOffs(20, 39).mirror()
                        .addBox(0.0F, 6.0F, 4.0F, 1, 4, 1),
                PartPose.offsetAndRotation(-3.0F, 14.0F, -7.0F, -0.6283185F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("rf5",
                CubeListBuilder.create().texOffs(20, 39).mirror()
                        .addBox(-2.0F, 6.0F, 4.0F, 1, 4, 1),
                PartPose.offsetAndRotation(-3.0F, 14.0F, -7.0F, -0.6283185F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("rf6",
                CubeListBuilder.create().texOffs(30, 39).mirror()
                        .addBox(-2.5F, 6.0F, 0.0F, 2, 5, 1),
                PartPose.offsetAndRotation(-2.0F, 14.0F, -7.0F, 0.1745329F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("lf2",
                CubeListBuilder.create().texOffs(19, 58).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 3, 3, 6),
                PartPose.offsetAndRotation(4.0F, 14.0F, -7.0F, -0.4886922F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("lf3",
                CubeListBuilder.create().texOffs(19, 47).mirror()
                        .addBox(0.0F, -7.0F, 0.0F, 3, 3, 6),
                PartPose.offsetAndRotation(4.0F, 14.0F, -7.0F, -2.347623F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("lf4",
                CubeListBuilder.create().texOffs(20, 39).mirror()
                        .addBox(0.0F, 6.0F, 4.0F, 1, 4, 1),
                PartPose.offsetAndRotation(4.0F, 14.0F, -7.0F, -0.6283185F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("lf5",
                CubeListBuilder.create().texOffs(20, 39).mirror()
                        .addBox(2.0F, 6.0F, 4.0F, 1, 4, 1),
                PartPose.offsetAndRotation(4.0F, 14.0F, -7.0F, -0.6283185F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("lf6",
                CubeListBuilder.create().texOffs(30, 39).mirror()
                        .addBox(0.5F, 6.0F, -2.0F, 2, 5, 1),
                PartPose.offsetAndRotation(4.0F, 14.0F, -5.0F, 0.1745329F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("head2",
                CubeListBuilder.create().texOffs(19, 80).mirror()
                        .addBox(-1.0F, 16.0F, -16.0F, 4, 1, 5),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.1047198F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("tail1",
                CubeListBuilder.create().texOffs(9, 18).mirror()
                        .addBox(-1.0F, -1.0F, -3.0F, 2, 8, 2),
                PartPose.offsetAndRotation(1.0F, 15.0F, 9.0F, 1.095163F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 128);
    }

    @Override
    public void setupAnim(EntityHydrolisc entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float hf = 0.0f;
        float newangle = 0.0f;
        newangle = (double)limbSwingAmount > 0.1 ? Mth.cos((float)(ageInTicks * 1.3f * limbSwingAmount)) * (float)Math.PI * 0.25f * limbSwingAmount : 0.0f;
        this.lf1.xRot = newangle;
        this.lf2.xRot = newangle - 0.488f;
        this.lf3.xRot = newangle - 2.347f;
        this.lf4.xRot = newangle - 0.628f;
        this.lf5.xRot = newangle - 0.628f;
        this.lf6.xRot = newangle + 0.174f;
        this.rf1.xRot = -newangle;
        this.rf2.xRot = -newangle - 0.488f;
        this.rf3.xRot = -newangle - 2.347f;
        this.rf4.xRot = -newangle - 0.628f;
        this.rf5.xRot = -newangle - 0.628f;
        this.rf6.xRot = -newangle + 0.174f;
        this.lb1.xRot = -newangle;
        this.lb2.xRot = -newangle - 0.488f;
        this.lb3.xRot = -newangle - 2.347f;
        this.lb4.xRot = -newangle - 0.628f;
        this.lb5.xRot = -newangle - 0.628f;
        this.lb6.xRot = -newangle + 0.174f;
        this.rb1.xRot = newangle;
        this.rb2.xRot = newangle - 0.488f;
        this.rb3.xRot = newangle - 2.347f;
        this.rb4.xRot = newangle - 0.628f;
        this.rb5.xRot = newangle - 0.628f;
        this.rb6.xRot = newangle + 0.174f;
        newangle = Mth.cos((float)(ageInTicks * 1.0f * limbSwingAmount)) * (float)Math.PI * 0.15f;
        if (entity.isInSittingPose()) {
        newangle = 0.0f;
        }
        this.tail1.yRot = newangle * 0.25f;
        this.tail2.z = this.tail1.z + (float)Math.cos(this.tail1.yRot) * 5.0f;
        this.tail2.x = this.tail1.x + (float)Math.sin(this.tail1.yRot) * 5.0f;
        this.tail2.yRot = newangle * 0.5f;
        this.tail3.z = this.tail2.z + (float)Math.cos(this.tail2.yRot) * 8.0f;
        this.tail3.x = this.tail2.x + (float)Math.sin(this.tail2.yRot) * 8.0f;
        this.tail3.yRot = newangle * 0.75f;
        hf = entity.getHealth() / entity.getMaxHealth();
        this.feather2.yRot = newangle = Mth.cos((float)(ageInTicks * 1.25f * limbSwingAmount * hf)) * (float)Math.PI * 0.2f * hf;
        newangle = Mth.cos((float)(ageInTicks * 0.75f * limbSwingAmount * hf)) * (float)Math.PI * 0.2f * hf;
        this.feather1.yRot = newangle - 0.9f;
        this.feather3.yRot = -newangle + 0.9f;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.tail2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tail3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.body2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lb2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lb1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.spine3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.spine4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rb1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rb2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.spine1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.spine2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lb3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rb3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.body1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.body0.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lf1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rf1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rb6.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rb4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rb5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lb6.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lb5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lb4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.head3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.feather3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.feather1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.feather2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.head1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rf2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rf3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rf4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rf5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rf6.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lf2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lf3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lf4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lf5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lf6.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.head2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tail1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
