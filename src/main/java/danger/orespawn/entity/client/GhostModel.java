package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.Ghost;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/**
 * The 1.7.10 Ghost (orig ModelGhost.java, ENT-S-091): one 6x21x6 head-and-body
 * column and two 2x11x2 arms splayed 0.33 rad, on a 64x64 sheet. The port had
 * a two-part rig. Geometry generated from the parsed original; proven by the
 * reference-geometry leg. The reference draws with GL blending at colour
 * (0.75, 0.75, 0.75, 0.25): the translucency stays in GhostRenderer's render
 * type; the 0.75 tint and 0.25 alpha are a renderer question logged with
 * ENT-S-092's per-renderer findings, not geometry.
 */
public class GhostModel<T extends Ghost> extends EntityModel<T> {
    private final ModelPart headAndBody;
    private final ModelPart lArm;
    private final ModelPart rArm;

    public GhostModel(ModelPart root) {
        this.headAndBody = root.getChild("HeadAndBody");
        this.lArm = root.getChild("LArm");
        this.rArm = root.getChild("RArm");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("HeadAndBody",
                CubeListBuilder.create()
                .texOffs(0, 0).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 21.0F, 6.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("LArm",
                CubeListBuilder.create()
                .texOffs(34, 0).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 11.0F, 2.0F),
                PartPose.offsetAndRotation(3.0F, 6.0F, 0.0F, 0.0F, 0.0F, -0.3316126F));
        root.addOrReplaceChild("RArm",
                CubeListBuilder.create()
                .texOffs(25, 0).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 11.0F, 2.0F),
                PartPose.offsetAndRotation(-3.0F, 6.0F, 0.0F, 0.0F, 0.0F, 0.3316126F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        // orig ModelGhost.render: four slow cosines on the arms (0.30, 0.32, 0.34, 0.36), amplitude PI*0.05
        this.lArm.zRot = -0.33F + Mth.cos(ageInTicks * 0.3F) * (float) Math.PI * 0.05F;
        this.rArm.zRot = 0.33F + Mth.cos(ageInTicks * 0.32F) * (float) Math.PI * 0.05F;
        this.lArm.xRot = -0.33F + Mth.cos(ageInTicks * 0.34F) * (float) Math.PI * 0.05F;
        this.rArm.xRot = 0.33F + Mth.cos(ageInTicks * 0.36F) * (float) Math.PI * 0.05F;
    }

    @Override
    public void renderToBuffer(PoseStack ps, VertexConsumer vc, int light, int overlay, int color) {
        this.headAndBody.render(ps, vc, light, overlay, color);
        this.lArm.render(ps, vc, light, overlay, color);
        this.rArm.render(ps, vc, light, overlay, color);
    }
}
