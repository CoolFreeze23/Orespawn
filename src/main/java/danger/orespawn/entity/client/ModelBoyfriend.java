package danger.orespawn.entity.client;

import danger.orespawn.entity.Boyfriend;
import danger.orespawn.entity.pose.HumanoidPose;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class ModelBoyfriend extends HumanoidModel<Boyfriend> {

    public ModelBoyfriend(ModelPart root) {
        super(root);
    }

    /**
     * The parity probe's entry (the remainder slice, 2026-09-15; owner's closing set item 3, TEST-010 (b)): this model
     * declares no {@code setupAnim} - the classic renderer draws vanilla {@code HumanoidModel.setupAnim} (21.1.223,
     * HumanoidModel.java:137-286) after setting {@code attackTime} / {@code riding} / {@code swimAmount} per frame - so the
     * probe, which has no entity, poses through {@link HumanoidClassicPose}: the renderer's three field sets at the frame's
     * partial tick, then vanilla's body transcribed statement by statement over these parts. The in-game classic path is
     * untouched: {@code HumanoidMobRenderer} still calls vanilla's own method. The seven-float form ({@code partialTick}
     * last) is the one the probe prefers over the six-float {@code poseFrom} where a model declares it.
     */
    public void poseFrom(HumanoidPose entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
                         float headPitch, float partialTick) {
        HumanoidClassicPose.prepare(this, entity, partialTick);
        HumanoidClassicPose.setupAnim(this, entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("hat",
                CubeListBuilder.create().texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, new CubeDeformation(0.5F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("right_arm",
                CubeListBuilder.create().texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4, 12, 4),
                PartPose.offset(-5.0F, 2.0F, 0.0F));
        root.addOrReplaceChild("left_arm",
                CubeListBuilder.create().texOffs(40, 16).mirror().addBox(-1.0F, -2.0F, -2.0F, 4, 12, 4),
                PartPose.offset(5.0F, 2.0F, 0.0F));
        root.addOrReplaceChild("right_leg",
                CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4),
                PartPose.offset(-1.9F, 12.0F, 0.0F));
        root.addOrReplaceChild("left_leg",
                CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4),
                PartPose.offset(1.9F, 12.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 32);
    }
}
