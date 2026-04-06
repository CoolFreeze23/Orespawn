package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.GhostSkelly;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class GhostSkellyModel extends EntityModel<GhostSkelly> {
    private int ri1, ri2;
    private float rf2;
    private final ModelPart body;
    private final ModelPart shirt;
    private final ModelPart head;
    private final ModelPart stem;
    private final ModelPart rarm;
    private final ModelPart larm;
    private final ModelPart rsleeve;
    private final ModelPart lsleeve;
    private final ModelPart lchains;
    private final ModelPart rchains;

    public GhostSkellyModel(ModelPart root) {
        this.body = root.getChild("body");
        this.shirt = root.getChild("shirt");
        this.head = root.getChild("head");
        this.stem = root.getChild("stem");
        this.rarm = root.getChild("rarm");
        this.larm = root.getChild("larm");
        this.rsleeve = root.getChild("rsleeve");
        this.lsleeve = root.getChild("lsleeve");
        this.lchains = root.getChild("lchains");
        this.rchains = root.getChild("rchains");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 0).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 1, 21, 1),
                PartPose.offset(0.0F, -1.0F, 0.0F));

        partdefinition.addOrReplaceChild("shirt",
                CubeListBuilder.create().texOffs(42, 43).mirror()
                        .addBox(-2.0F, 0.0F, -2.0F, 5, 12, 5),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(40, 29).mirror()
                        .addBox(-3.0F, 0.0F, -3.0F, 7, 5, 7),
                PartPose.offset(0.0F, -6.0F, 0.0F));

        partdefinition.addOrReplaceChild("stem",
                CubeListBuilder.create().texOffs(49, 23).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 1, 2, 1),
                PartPose.offsetAndRotation(0.0F, -8.0F, 0.0F, 0.1745329F, 0.0F, 0.1745329F));

        partdefinition.addOrReplaceChild("rarm",
                CubeListBuilder.create().texOffs(26, 0).mirror()
                        .addBox(-14.0F, 0.0F, 0.0F, 15, 1, 1),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("larm",
                CubeListBuilder.create().texOffs(63, 0).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 15, 1, 1),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("rsleeve",
                CubeListBuilder.create().texOffs(31, 7).mirror()
                        .addBox(-11.0F, 0.0F, -1.0F, 9, 8, 3),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("lsleeve",
                CubeListBuilder.create().texOffs(71, 7).mirror()
                        .addBox(3.0F, 0.0F, -1.0F, 9, 8, 3),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("lchains",
                CubeListBuilder.create().texOffs(98, 0).mirror()
                        .addBox(11.0F, -1.0F, 0.0F, 3, 16, 1),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("rchains",
                CubeListBuilder.create().texOffs(12, 0).mirror()
                        .addBox(-13.0F, -1.0F, 0.0F, 3, 10, 1),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 64);
    }

    @Override
    public void setupAnim(GhostSkelly entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float newangle = 0.0f;
        float newrf1 = 0.0f;
        this.lsleeve.zRot = this.lchains.zRot = Mth.cos((float)(ageInTicks * 0.2f)) * (float)Math.PI * 0.05f;
        this.larm.zRot = this.lchains.zRot;
        this.rsleeve.zRot = this.rchains.zRot = Mth.cos((float)(ageInTicks * 0.22f)) * (float)Math.PI * 0.05f;
        this.rarm.zRot = this.rchains.zRot;
        this.lsleeve.yRot = this.lchains.yRot = Mth.cos((float)(ageInTicks * 0.24f)) * (float)Math.PI * 0.05f;
        this.larm.yRot = this.lchains.yRot;
        this.rsleeve.yRot = this.rchains.yRot = Mth.cos((float)(ageInTicks * 0.26f)) * (float)Math.PI * 0.05f;
        this.rarm.yRot = this.rchains.yRot;
        newangle = Mth.cos((float)(ageInTicks * 0.05f)) * (float)Math.PI * 2.0f;
        newrf1 = ageInTicks * 0.05f % ((float)Math.PI * 2);
        newrf1 = Math.abs(newrf1);
        if (newrf1 < rf2) {
        ri2 = 0;
        if (entity.getRandom().nextInt(3) == 1) {
        ri2 |= 1;
        }
        }
        rf2 = newrf1;
        if ((ri2 & 1) == 0) {
        newangle = 0.0f;
        }
        this.head.yRot = newangle;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.shirt.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.head.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.stem.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rarm.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.larm.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rsleeve.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lsleeve.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lchains.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rchains.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
