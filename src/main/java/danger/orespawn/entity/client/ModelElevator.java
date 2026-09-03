package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import danger.orespawn.entity.Elevator;

public class ModelElevator extends EntityModel<Elevator> {
    private final ModelPart shape1;
    private final ModelPart shape2;
    private final ModelPart shape3;
    private final ModelPart shape4;
    private final ModelPart shape5;

    public ModelElevator(ModelPart root) {
        this.shape1 = root.getChild("shape1");
        this.shape2 = root.getChild("shape2");
        this.shape3 = root.getChild("shape3");
        this.shape4 = root.getChild("shape4");
        this.shape5 = root.getChild("shape5");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        // ENT-S-091 (2026-09-02): rotation points are the original's (0, 0, 0) (orig
        // ModelElevator.java:24-48). The 1.7.10 RenderElevator was a plain Render with no
        // 24 px living lift; the port's MobRenderer lift is cancelled in ElevatorRenderer.scale()
        // (was TF-029's +24 px pivot bake, an equivalent re-expression the reference leg could
        // not see through). What renders is unchanged.
        partdefinition.addOrReplaceChild("shape1",
                CubeListBuilder.create().texOffs(0, 0).mirror()
                        .addBox(-4.0F, 0.0F, -8.0F, 8, 1, 16),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("shape2",
                CubeListBuilder.create().texOffs(0, 18).mirror()
                        .addBox(-3.0F, 0.0F, -9.0F, 6, 1, 1),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("shape3",
                CubeListBuilder.create().texOffs(0, 21).mirror()
                        .addBox(-1.0F, 0.0F, -10.0F, 2, 1, 1),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("shape4",
                CubeListBuilder.create().texOffs(17, 18).mirror()
                        .addBox(-3.0F, 0.0F, 8.0F, 6, 1, 1),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("shape5",
                CubeListBuilder.create().texOffs(17, 21).mirror()
                        .addBox(-1.0F, 0.0F, 9.0F, 2, 1, 1),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(Elevator entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    }

    /**
     * ENT-S-094: the incoming {@code packedOverlay} is ignored. LivingEntityRenderer
     * .render computes it with the {@code public static} getOverlayCoords
     * (invokestatic at bytecode 593; body reads hurtTime/deathTime and packs
     * OverlayTexture.v(hurt) = 3 into the red overlay), which no renderer hook
     * can replace. Orig RenderElevator.java:42-44 draws through the plain Render,
     * which had no hurt/death red pass (that pass lives in RendererLivingEntity
     * .doRender only). With u = 0, pack(0, 10) == NO_OVERLAY exactly, so this is
     * a zero-difference change when the board is not hurt and removes only the
     * red tint when hurtTime/deathTime > 0.
     */
    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        int overlay = OverlayTexture.NO_OVERLAY;
        shape1.render(poseStack, vertexConsumer, packedLight, overlay, color);
        shape2.render(poseStack, vertexConsumer, packedLight, overlay, color);
        shape3.render(poseStack, vertexConsumer, packedLight, overlay, color);
        shape4.render(poseStack, vertexConsumer, packedLight, overlay, color);
        shape5.render(poseStack, vertexConsumer, packedLight, overlay, color);
    }
}
