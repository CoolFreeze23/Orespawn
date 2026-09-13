package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.EntityLeafMonster;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
/**
 * BUG-041 stage 2 (2026-09-13): the 1.7.10 export sets {@code mirror = true} AFTER {@code addBox} (orig ModelLeafMonster.java:
 * 5 stores, all inert - 1.7.10's ModelBox reads the flag in its constructor, law 11 from Mojang's 1.7.10 jar),
 * so the original rendered UNMIRRORED. The port's 5 {@code .mirror()} calls preceded {@code addBox} and flipped
 * every face's U: dropped port-wide as the EnderReaper precedent was (5354420); geometry unchanged; proven by the
 * reference-geometry leg.
 */

public class LeafMonsterModel<T extends EntityLeafMonster> extends EntityModel<T> {
    private final ModelPart body;
    private final ModelPart larm;
    private final ModelPart rarm;
    private final ModelPart lleg;
    private final ModelPart rleg;

    public LeafMonsterModel(ModelPart root) {
        this.body = root.getChild("body");
        this.larm = root.getChild("larm");
        this.rarm = root.getChild("rarm");
        this.lleg = root.getChild("lleg");
        this.rleg = root.getChild("rleg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(32, 32).addBox(-8.0F, -8.0F, -8.0F, 16, 16, 16), PartPose.offset(0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("larm", CubeListBuilder.create().texOffs(64, 0).addBox(0.0F, -16.0F, -8.0F, 16, 16, 16), PartPose.offset(8.0F, -8.0F, 0.0F));
        root.addOrReplaceChild("rarm", CubeListBuilder.create().texOffs(0, 0).addBox(-16.0F, -16.0F, -8.0F, 16, 16, 16), PartPose.offset(-8.0F, -8.0F, 0.0F));
        root.addOrReplaceChild("lleg", CubeListBuilder.create().texOffs(64, 64).addBox(0.0F, 0.0F, -8.0F, 16, 16, 16), PartPose.offset(8.0F, 8.0F, 0.0F));
        root.addOrReplaceChild("rleg", CubeListBuilder.create().texOffs(0, 64).addBox(-16.0F, 0.0F, -8.0F, 16, 16, 16), PartPose.offset(-8.0F, 8.0F, 0.0F));
        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        int attacking = entity.getAttacking();
        if (attacking == 0) {
            this.body.y = 16.0F;
            this.rarm.y = 8.0F;
            this.larm.y = 8.0F;
            this.rarm.yRot = 0.0F;
            this.larm.yRot = 0.0F;
            this.rarm.xRot = 0.0F;
            this.larm.xRot = 0.0F;
            this.lleg.xRot = 0.0F;
            this.rleg.xRot = 0.0F;
        } else {
            this.body.y = 0.0F;
            this.rarm.y = -8.0F;
            this.larm.y = -8.0F;
            float legAngle = limbSwingAmount > 0.1f ? Mth.cos(ageInTicks * 0.95f) * (float) Math.PI * 0.25f * limbSwingAmount : 0.0f;
            this.lleg.xRot = legAngle;
            this.rleg.xRot = -legAngle;
            float armAngle = Mth.cos(ageInTicks * 0.7f) * (float) Math.PI * 0.55f;
            this.rarm.yRot = -Math.abs(armAngle);
            this.larm.yRot = Math.abs(armAngle);
            this.rarm.xRot = -Math.abs(armAngle);
            this.larm.xRot = -Math.abs(armAngle);
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        larm.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        rarm.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        lleg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        rleg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
