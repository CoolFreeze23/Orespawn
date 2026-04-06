package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.RockBase;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.Entity;

public class ModelRockBase extends EntityModel<Entity> {
    private final ModelPart RockShape1;
    private final ModelPart RockShape2;
    private final ModelPart RockShape3;
    private final ModelPart RockSmallShape1;
    private final ModelPart RockSmallShape2;
    private final ModelPart RockTNTShape1;
    private final ModelPart RockTNTShape2;
    private final ModelPart RockTNTShape3;
    private final ModelPart RockTNTShape4;
    private final ModelPart RockSpikeyShape1;
    private final ModelPart RockSpikeyShape2;
    private final ModelPart RockSpikeyShape3;
    private final ModelPart CrystalShape1;
    private final ModelPart CrystalShape2;
    private final ModelPart CrystalShape3a;
    private final ModelPart CrystalShape3b;
    private final ModelPart CrystalShape3c;
    private final ModelPart CrystalShape3d;
    private final ModelPart CrystalShape4a;
    private final ModelPart CrystalShape4b;
    private final ModelPart CrystalShape4c;
    private final ModelPart CrystalShape4d;

    private int rockType = 0;

    public ModelRockBase(ModelPart root) {
        this.RockShape1 = root.getChild("RockShape1");
        this.RockShape2 = root.getChild("RockShape2");
        this.RockShape3 = root.getChild("RockShape3");
        this.RockSmallShape1 = root.getChild("RockSmallShape1");
        this.RockSmallShape2 = root.getChild("RockSmallShape2");
        this.RockTNTShape1 = root.getChild("RockTNTShape1");
        this.RockTNTShape2 = root.getChild("RockTNTShape2");
        this.RockTNTShape3 = root.getChild("RockTNTShape3");
        this.RockTNTShape4 = root.getChild("RockTNTShape4");
        this.RockSpikeyShape1 = root.getChild("RockSpikeyShape1");
        this.RockSpikeyShape2 = root.getChild("RockSpikeyShape2");
        this.RockSpikeyShape3 = root.getChild("RockSpikeyShape3");
        this.CrystalShape1 = root.getChild("CrystalShape1");
        this.CrystalShape2 = root.getChild("CrystalShape2");
        this.CrystalShape3a = root.getChild("CrystalShape3a");
        this.CrystalShape3b = root.getChild("CrystalShape3b");
        this.CrystalShape3c = root.getChild("CrystalShape3c");
        this.CrystalShape3d = root.getChild("CrystalShape3d");
        this.CrystalShape4a = root.getChild("CrystalShape4a");
        this.CrystalShape4b = root.getChild("CrystalShape4b");
        this.CrystalShape4c = root.getChild("CrystalShape4c");
        this.CrystalShape4d = root.getChild("CrystalShape4d");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("RockShape1",
                CubeListBuilder.create().texOffs(0, 0).mirror()
                        .addBox(-3.0F, 0.0F, -1.0F, 6, 1, 2),
                PartPose.offset(0.0F, 23.0F, 0.0F));

        partdefinition.addOrReplaceChild("RockShape2",
                CubeListBuilder.create().texOffs(0, 4).mirror()
                        .addBox(-3.0F, 0.0F, 1.0F, 3, 1, 1),
                PartPose.offset(0.0F, 23.0F, 0.0F));

        partdefinition.addOrReplaceChild("RockShape3",
                CubeListBuilder.create().texOffs(0, 7).mirror()
                        .addBox(0.0F, 0.0F, -2.0F, 2, 1, 1),
                PartPose.offset(0.0F, 23.0F, 0.0F));

        partdefinition.addOrReplaceChild("RockSmallShape2",
                CubeListBuilder.create().texOffs(0, 4).mirror()
                        .addBox(-2.0F, 0.0F, 0.0F, 3, 1, 1),
                PartPose.offset(0.0F, 23.0F, 0.0F));

        partdefinition.addOrReplaceChild("RockSmallShape1",
                CubeListBuilder.create().texOffs(0, 7).mirror()
                        .addBox(0.0F, 0.0F, -1.0F, 2, 1, 1),
                PartPose.offset(0.0F, 23.0F, 0.0F));

        partdefinition.addOrReplaceChild("RockTNTShape1",
                CubeListBuilder.create().texOffs(0, 0).mirror()
                        .addBox(-3.0F, 0.0F, -1.0F, 6, 1, 2),
                PartPose.offset(0.0F, 23.0F, 0.0F));

        partdefinition.addOrReplaceChild("RockTNTShape2",
                CubeListBuilder.create().texOffs(0, 4).mirror()
                        .addBox(-3.0F, 0.0F, 1.0F, 3, 1, 1),
                PartPose.offset(0.0F, 23.0F, 0.0F));

        partdefinition.addOrReplaceChild("RockTNTShape3",
                CubeListBuilder.create().texOffs(0, 7).mirror()
                        .addBox(0.0F, 0.0F, -2.0F, 2, 1, 1),
                PartPose.offset(0.0F, 23.0F, 0.0F));

        partdefinition.addOrReplaceChild("RockTNTShape4",
                CubeListBuilder.create().texOffs(0, 10).mirror()
                        .addBox(-4.0F, 0.0F, -2.0F, 3, 1, 3),
                PartPose.offset(0.0F, 22.0F, 0.0F));

        partdefinition.addOrReplaceChild("RockSpikeyShape1",
                CubeListBuilder.create().texOffs(0, 0).mirror()
                        .addBox(-3.0F, 0.0F, -1.0F, 6, 1, 2),
                PartPose.offset(0.0F, 23.0F, 0.0F));

        partdefinition.addOrReplaceChild("RockSpikeyShape2",
                CubeListBuilder.create().texOffs(0, 4).mirror()
                        .addBox(-4.0F, 0.0F, -1.0F, 3, 1, 1),
                PartPose.offsetAndRotation(0.0F, 23.0F, 0.0F, 0.0F, 1.570796F, 0.0F));

        partdefinition.addOrReplaceChild("RockSpikeyShape3",
                CubeListBuilder.create().texOffs(0, 7).mirror()
                        .addBox(1.0F, 0.0F, 1.0F, 2, 1, 1),
                PartPose.offsetAndRotation(0.0F, 23.0F, 0.0F, 0.0F, 1.570796F, 0.0F));

        partdefinition.addOrReplaceChild("CrystalShape1",
                CubeListBuilder.create().texOffs(0, 0).mirror()
                        .addBox(-1.0F, -4.0F, -1.0F, 2, 5, 2),
                PartPose.offset(0.0F, 23.0F, 0.0F));

        partdefinition.addOrReplaceChild("CrystalShape2",
                CubeListBuilder.create().texOffs(10, 0).mirror()
                        .addBox(-0.5F, -7.0F, -0.5F, 1, 3, 1),
                PartPose.offset(0.0F, 23.0F, 0.0F));

        partdefinition.addOrReplaceChild("CrystalShape3a",
                CubeListBuilder.create().texOffs(0, 8).mirror()
                        .addBox(-1.0F, -5.0F, -1.0F, 1, 5, 1),
                PartPose.offsetAndRotation(0.0F, 23.0F, 0.0F, 0.5410521F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("CrystalShape3b",
                CubeListBuilder.create().texOffs(0, 8).mirror()
                        .addBox(0.0F, -5.0F, 0.0F, 1, 5, 1),
                PartPose.offsetAndRotation(0.0F, 23.0F, 0.0F, -0.5410521F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("CrystalShape3c",
                CubeListBuilder.create().texOffs(0, 8).mirror()
                        .addBox(0.0F, -5.0F, -1.0F, 1, 5, 1),
                PartPose.offsetAndRotation(0.0F, 23.0F, 0.0F, 0.0F, 0.0F, 0.5410521F));

        partdefinition.addOrReplaceChild("CrystalShape3d",
                CubeListBuilder.create().texOffs(0, 8).mirror()
                        .addBox(-1.0F, -5.0F, 0.0F, 1, 5, 1),
                PartPose.offsetAndRotation(0.0F, 23.0F, 0.0F, 0.0F, 0.0F, -0.5410521F));

        partdefinition.addOrReplaceChild("CrystalShape4a",
                CubeListBuilder.create().texOffs(0, 16).mirror()
                        .addBox(0.0F, -3.0F, -1.0F, 1, 3, 1),
                PartPose.offsetAndRotation(0.0F, 23.0F, 0.0F, 1.308997F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("CrystalShape4b",
                CubeListBuilder.create().texOffs(0, 16).mirror()
                        .addBox(-1.0F, -3.0F, 0.0F, 1, 3, 1),
                PartPose.offsetAndRotation(0.0F, 23.0F, 0.0F, -1.308997F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("CrystalShape4c",
                CubeListBuilder.create().texOffs(0, 16).mirror()
                        .addBox(0.0F, -3.0F, 0.0F, 1, 3, 1),
                PartPose.offsetAndRotation(0.0F, 23.0F, 0.0F, 0.0F, 0.0F, 1.308997F));

        partdefinition.addOrReplaceChild("CrystalShape4d",
                CubeListBuilder.create().texOffs(0, 16).mirror()
                        .addBox(-1.0F, -3.0F, -1.0F, 1, 3, 1),
                PartPose.offsetAndRotation(0.0F, 23.0F, 0.0F, 0.0F, 0.0F, -1.308997F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        if (entity instanceof RockBase rb) {
            this.rockType = rb.getRockType();
        }

        setAllVisible(false);

        if (rockType < 1 || rockType > 12) return;

        if (rockType == 1) {
            this.RockSmallShape1.visible = true;
            this.RockSmallShape2.visible = true;
        } else if (rockType == 7) {
            this.RockSpikeyShape1.visible = true;
            this.RockSpikeyShape2.visible = true;
            this.RockSpikeyShape3.visible = true;
        } else if (rockType == 8) {
            this.RockTNTShape1.visible = true;
            this.RockTNTShape2.visible = true;
            this.RockTNTShape3.visible = true;
            this.RockTNTShape4.visible = true;
        } else if (rockType >= 9 && rockType <= 12) {
            this.CrystalShape1.visible = true;
            this.CrystalShape2.visible = true;
            this.CrystalShape3a.visible = true;
            this.CrystalShape3b.visible = true;
            this.CrystalShape3c.visible = true;
            this.CrystalShape3d.visible = true;
            this.CrystalShape4a.visible = true;
            this.CrystalShape4b.visible = true;
            this.CrystalShape4c.visible = true;
            this.CrystalShape4d.visible = true;
        } else {
            this.RockShape1.visible = true;
            this.RockShape2.visible = true;
            this.RockShape3.visible = true;
        }
    }

    private void setAllVisible(boolean visible) {
        this.RockShape1.visible = visible;
        this.RockShape2.visible = visible;
        this.RockShape3.visible = visible;
        this.RockSmallShape1.visible = visible;
        this.RockSmallShape2.visible = visible;
        this.RockTNTShape1.visible = visible;
        this.RockTNTShape2.visible = visible;
        this.RockTNTShape3.visible = visible;
        this.RockTNTShape4.visible = visible;
        this.RockSpikeyShape1.visible = visible;
        this.RockSpikeyShape2.visible = visible;
        this.RockSpikeyShape3.visible = visible;
        this.CrystalShape1.visible = visible;
        this.CrystalShape2.visible = visible;
        this.CrystalShape3a.visible = visible;
        this.CrystalShape3b.visible = visible;
        this.CrystalShape3c.visible = visible;
        this.CrystalShape3d.visible = visible;
        this.CrystalShape4a.visible = visible;
        this.CrystalShape4b.visible = visible;
        this.CrystalShape4c.visible = visible;
        this.CrystalShape4d.visible = visible;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.RockShape1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.RockShape2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.RockShape3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.RockSmallShape1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.RockSmallShape2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.RockTNTShape1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.RockTNTShape2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.RockTNTShape3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.RockTNTShape4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.RockSpikeyShape1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.RockSpikeyShape2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.RockSpikeyShape3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.CrystalShape1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.CrystalShape2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.CrystalShape3a.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.CrystalShape3b.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.CrystalShape3c.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.CrystalShape3d.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.CrystalShape4a.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.CrystalShape4b.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.CrystalShape4c.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.CrystalShape4d.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
