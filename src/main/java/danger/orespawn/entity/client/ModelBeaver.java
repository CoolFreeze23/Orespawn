package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

public class ModelBeaver extends EntityModel<Entity> {
    private static final float ANIM_SPEED = 1.0F;

    private final ModelPart head;
    private final ModelPart nose;
    private final ModelPart teeth;
    private final ModelPart body;
    private final ModelPart tail;
    private final ModelPart rff;
    private final ModelPart lff;
    private final ModelPart rrf;
    private final ModelPart lrf;

    public ModelBeaver(ModelPart root) {
        this.head = root.getChild("head");
        this.nose = root.getChild("nose");
        this.teeth = root.getChild("teeth");
        this.body = root.getChild("body");
        this.tail = root.getChild("tail");
        this.rff = root.getChild("rff");
        this.lff = root.getChild("lff");
        this.rrf = root.getChild("rrf");
        this.lrf = root.getChild("lrf");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 3).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 6, 5, 5),
                PartPose.offset(0.0F, 15.0F, -8.0F));

        partdefinition.addOrReplaceChild("nose",
                CubeListBuilder.create().texOffs(6, 0).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 2, 1, 1),
                PartPose.offset(2.0F, 18.0F, -8.5F));

        partdefinition.addOrReplaceChild("teeth",
                CubeListBuilder.create().texOffs(0, 0).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 2, 2, 1),
                PartPose.offset(2.0F, 19.0F, -8.2F));

        partdefinition.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 13).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 8, 8, 10),
                PartPose.offset(-1.0F, 14.0F, -3.0F));

        partdefinition.addOrReplaceChild("tail",
                CubeListBuilder.create().texOffs(22, 0).mirror()
                        .addBox(0.0F, -1.0F, 0.0F, 5, 1, 8),
                PartPose.offset(0.5F, 21.0F, 7.0F));

        partdefinition.addOrReplaceChild("rff",
                CubeListBuilder.create().texOffs(22, 9).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 2, 2, 2),
                PartPose.offset(-0.5F, 22.0F, -2.5F));

        partdefinition.addOrReplaceChild("lff",
                CubeListBuilder.create().texOffs(22, 9).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 2, 2, 2),
                PartPose.offset(4.5F, 22.0F, -2.5F));

        partdefinition.addOrReplaceChild("rrf",
                CubeListBuilder.create().texOffs(22, 9).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 2, 2, 2),
                PartPose.offset(-0.5F, 22.0F, 4.5F));

        partdefinition.addOrReplaceChild("lrf",
                CubeListBuilder.create().texOffs(22, 9).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 2, 2, 2),
                PartPose.offset(4.5F, 22.0F, 4.5F));

        return LayerDefinition.create(meshdefinition, 64, 32);
    }

    @Override
    public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float newangle = Mth.cos(ageInTicks * 3.7F * ANIM_SPEED) * (float) Math.PI * 0.45F * limbSwingAmount;
        this.rff.xRot = newangle;
        this.lrf.xRot = newangle;
        this.lff.xRot = -newangle;
        this.rrf.xRot = -newangle;

        newangle = Mth.cos(ageInTicks * 2.7F * ANIM_SPEED) * (float) Math.PI * 0.25F;
        this.teeth.xRot = newangle;

        newangle = Mth.cos(ageInTicks * 0.5F * ANIM_SPEED) * (float) Math.PI * 0.05F;
        this.tail.xRot = newangle;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.head.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.nose.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.teeth.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tail.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rff.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lff.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rrf.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lrf.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
