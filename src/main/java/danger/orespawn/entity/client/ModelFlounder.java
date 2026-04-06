package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.Flounder;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class ModelFlounder extends EntityModel<Flounder> {
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart tail1;
    private final ModelPart tail2;
    private final ModelPart rfin;
    private final ModelPart lfin;

    public ModelFlounder(ModelPart root) {
        this.body = root.getChild("body");
        this.head = root.getChild("head");
        this.tail1 = root.getChild("tail1");
        this.tail2 = root.getChild("tail2");
        this.rfin = root.getChild("rfin");
        this.lfin = root.getChild("lfin");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-4.0F, 0.0F, -5.0F, 8, 1, 12),
                PartPose.offset(0.0F, 22.0F, 0.0F));

        partdefinition.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 5).mirror().addBox(-2.0F, 0.0F, 0.0F, 4, 1, 2),
                PartPose.offset(0.0F, 22.0F, -7.0F));

        partdefinition.addOrReplaceChild("tail1",
                CubeListBuilder.create().texOffs(30, 0).mirror().addBox(-2.0F, 0.0F, 0.0F, 4, 1, 2),
                PartPose.offset(0.0F, 22.0F, 7.0F));

        partdefinition.addOrReplaceChild("tail2",
                CubeListBuilder.create().texOffs(30, 4).mirror().addBox(-3.0F, 0.0F, 2.0F, 6, 1, 3),
                PartPose.offset(0.0F, 22.0F, 7.0F));

        partdefinition.addOrReplaceChild("rfin",
                CubeListBuilder.create().texOffs(12, 0).mirror().addBox(-3.0F, 0.0F, 0.0F, 3, 1, 2),
                PartPose.offset(-4.0F, 22.0F, -2.0F));

        partdefinition.addOrReplaceChild("lfin",
                CubeListBuilder.create().texOffs(0, 0).mirror().addBox(0.0F, 0.0F, 0.0F, 3, 1, 2),
                PartPose.offset(4.0F, 22.0F, -2.0F));

        return LayerDefinition.create(meshdefinition, 64, 32);
    }

    @Override
    public void setupAnim(Flounder entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float newangle2;
        float newangle;
        if ((double)limbSwingAmount > 0.1) {
        newangle = Mth.cos((float)(ageInTicks * 1.3f)) * (float)Math.PI * 0.25f * limbSwingAmount;
        newangle2 = Mth.cos((float)(ageInTicks * 1.7f)) * (float)Math.PI * 0.25f * limbSwingAmount;
        } else {
        newangle = 0.0f;
        newangle2 = 0.0f;
        }
        this.lfin.zRot = newangle;
        this.rfin.zRot = newangle2;
        newangle = (double)limbSwingAmount > 0.1 ? Mth.cos((float)(ageInTicks * 1.2f)) * (float)Math.PI * 0.25f * limbSwingAmount : Mth.cos((float)(ageInTicks * 0.7f)) * (float)Math.PI * 0.05f;
        this.tail1.xRot = this.tail2.xRot = newangle;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.head.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tail1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tail2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rfin.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lfin.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
