package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.Skate;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/**
 * The 1.7.10 Skate (orig ModelSkate.java, ENT-S-091): a 6x1x6 body turned 45
 * degrees, an 11-long tail, and a 4-long tail tip that flaps. The port had a
 * hand-authored body/wings/tail rig. Geometry generated from the parsed
 * original; proven by the reference-geometry leg. Reference render order:
 * body, tail1, Shape1.
 */
public class ModelSkate extends EntityModel<Skate> {
    private final ModelPart body;
    private final ModelPart tail1;
    private final ModelPart shape1;

    public ModelSkate(ModelPart root) {
        this.body = root.getChild("body");
        this.tail1 = root.getChild("tail1");
        this.shape1 = root.getChild("Shape1");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body",
                CubeListBuilder.create()
                .texOffs(0, 13).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 1.0F, 6.0F),
                PartPose.offsetAndRotation(0.0F, 22.0F, 0.0F, 0.0F, 0.7853982F, 0.0F));
        root.addOrReplaceChild("tail1",
                CubeListBuilder.create()
                .texOffs(0, 0).addBox(-0.5F, 0.0F, 0.0F, 1.0F, 1.0F, 11.0F),
                PartPose.offset(0.0F, 22.0F, 3.0F));
        root.addOrReplaceChild("Shape1",
                CubeListBuilder.create()
                .texOffs(0, 21).addBox(-0.5F, 0.0F, 0.0F, 1.0F, 1.0F, 4.0F),
                PartPose.offsetAndRotation(0.0F, 22.0F, 5.0F, 0.7853982F, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void setupAnim(Skate entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        // orig ModelSkate.render: moving -> cos(f2*1.2)*PI*0.15*f1, idle -> cos(f2*0.4)*PI*0.05; tip rests at 0.785
        float newangle = (double) limbSwingAmount > 0.1
                ? Mth.cos(ageInTicks * 1.2F) * (float) Math.PI * 0.15F * limbSwingAmount
                : Mth.cos(ageInTicks * 0.4F) * (float) Math.PI * 0.05F;
        this.shape1.xRot = 0.785F + newangle;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        this.body.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.tail1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.shape1.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
