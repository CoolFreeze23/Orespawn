package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.AttackSquid;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class ModelAttackSquid extends EntityModel<AttackSquid> {
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart tentacle1;
    private final ModelPart tentacle2;
    private final ModelPart tentacle3;
    private final ModelPart tentacle4;

    public ModelAttackSquid(ModelPart root) {
        this.body = root.getChild("body");
        this.head = root.getChild("head");
        this.tentacle1 = root.getChild("tentacle1");
        this.tentacle2 = root.getChild("tentacle2");
        this.tentacle3 = root.getChild("tentacle3");
        this.tentacle4 = root.getChild("tentacle4");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition part = mesh.getRoot();
        part.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -6.0F, -4.0F, 8, 8, 8), PartPose.offset(0.0F, 16.0F, 0.0F));
        part.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 16).addBox(-3.0F, -3.0F, -5.0F, 6, 6, 2), PartPose.offset(0.0F, 14.0F, 0.0F));
        part.addOrReplaceChild("tentacle1", CubeListBuilder.create().texOffs(32, 0).addBox(-1.0F, 0.0F, -1.0F, 2, 6, 2), PartPose.offset(-2.0F, 22.0F, -2.0F));
        part.addOrReplaceChild("tentacle2", CubeListBuilder.create().texOffs(32, 0).addBox(-1.0F, 0.0F, -1.0F, 2, 6, 2), PartPose.offset(2.0F, 22.0F, -2.0F));
        part.addOrReplaceChild("tentacle3", CubeListBuilder.create().texOffs(32, 0).addBox(-1.0F, 0.0F, -1.0F, 2, 6, 2), PartPose.offset(-2.0F, 22.0F, 2.0F));
        part.addOrReplaceChild("tentacle4", CubeListBuilder.create().texOffs(32, 0).addBox(-1.0F, 0.0F, -1.0F, 2, 6, 2), PartPose.offset(2.0F, 22.0F, 2.0F));
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void setupAnim(AttackSquid entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float swing = Mth.cos(ageInTicks * 0.3F) * 0.15F;
        this.tentacle1.xRot = swing;
        this.tentacle2.xRot = -swing;
        this.tentacle3.xRot = -swing;
        this.tentacle4.xRot = swing;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        this.body.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.head.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.tentacle1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.tentacle2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.tentacle3.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.tentacle4.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
