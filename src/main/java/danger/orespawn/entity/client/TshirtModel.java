package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.EntityTshirt;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/**
 * The 1.7.10 Tshirt (orig ModelTshirt.java, ENT-S-091): two flat quads, a
 * 256x64 banner and a 128x128 body, both pivoting at (0, -128, 0) and turning
 * together on the slow Coin cosine. The original declares a 512x256 sheet and
 * binds a 320x160 image, so its UVs sample a 0.625 window of the image; that
 * declared size is kept on purpose, exactly as ModelCoin keeps its 512x512
 * (BUG-040). The port's three-part 320x160 rig was a re-authoring. Trailing
 * {@code mirror = true} in the original is inert (BUG-041). Proven by the
 * reference-geometry leg.
 */
public class TshirtModel<T extends EntityTshirt> extends EntityModel<T> {
    /** orig ClientProxyOreSpawn.java:418: new ModelTshirt(0.22f). */
    public static final float WINGSPEED = 0.22F;

    private final ModelPart shape1;
    private final ModelPart shape2;

    public TshirtModel(ModelPart root) {
        this.shape1 = root.getChild("Shape1");
        this.shape2 = root.getChild("Shape2");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("Shape1",
                CubeListBuilder.create()
                .texOffs(0, 0).addBox(-128.0F, -64.0F, 0.0F, 256.0F, 64.0F, 1.0F),
                PartPose.offset(0.0F, -128.0F, 0.0F));
        root.addOrReplaceChild("Shape2",
                CubeListBuilder.create()
                .texOffs(0, 64).addBox(-64.0F, 0.0F, 0.0F, 128.0F, 128.0F, 1.0F),
                PartPose.offset(0.0F, -128.0F, 0.0F));
        return LayerDefinition.create(mesh, 512, 256);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        // orig ModelTshirt.render: Shape1.rotateAngleY = Shape2.rotateAngleY = cos(f2 * 0.05f * wingspeed) * PI
        float yaw = Mth.cos(ageInTicks * 0.05F * WINGSPEED) * (float) Math.PI;
        this.shape1.yRot = yaw;
        this.shape2.yRot = yaw;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        this.shape1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.shape2.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
