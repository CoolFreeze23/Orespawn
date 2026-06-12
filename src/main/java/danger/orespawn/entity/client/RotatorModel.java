package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import danger.orespawn.entity.EntityRotator;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

/**
 * Rotator gyroscope model. The original (orig ModelRotator.java:44-80) renders
 * each of the 3 blade shapes 8 times in a fan (blade-to-blade step 45° =
 * 0.7853982 rad, orig ModelRotator.java:56), and spins the three 8-blade fans
 * on the X, Y and Z axes respectively (orig ModelRotator.java:52, 60, 68) by an
 * accumulating per-entity angle {@code ri.rf1} advanced 2° per rendered frame
 * and wrapped at 359° (orig ModelRotator.java:75-78) — 24 blades total forming
 * the signature gyroscope ball.
 */
public class RotatorModel<T extends EntityRotator> extends EntityModel<T> {
    /** orig ModelRotator.java:56 — 45° fan step between successive blades. */
    private static final float FAN_STEP = 0.7853982f;

    private final ModelPart shape1;
    private final ModelPart shape2;
    private final ModelPart shape3;

    /** Per-entity fan angle holder, captured in {@link #setupAnim} for {@link #renderToBuffer}. */
    private RenderInfo renderInfo;

    public RotatorModel(ModelPart root) {
        this.shape1 = root.getChild("shape1");
        this.shape2 = root.getChild("shape2");
        this.shape3 = root.getChild("shape3");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        // orig ModelRotator.java:24-41 — three blade boxes at increasing radius.
        root.addOrReplaceChild("shape1", CubeListBuilder.create().texOffs(0, 12).mirror().addBox(-2.0F, 3.9F, 0.0F, 4, 1, 1), PartPose.offset(0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("shape2", CubeListBuilder.create().texOffs(0, 7).mirror().addBox(-4.0F, 7.6F, 0.0F, 8, 2, 2), PartPose.offset(0.0F, 0.0F, -0.5F));
        root.addOrReplaceChild("shape3", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-7.0F, 13.7F, 0.0F, 14, 3, 3), PartPose.offset(0.0F, 0.0F, -1.0F));
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.renderInfo = entity.getRenderInfo();
    }

    @Override
    public void renderToBuffer(PoseStack ps, VertexConsumer vc, int light, int overlay, int color) {
        RenderInfo ri = this.renderInfo;
        float fanSpin = ri != null ? ri.rf1 : 0.0f;

        // orig ModelRotator.java:52-57 — shape1 fan spun about the X axis.
        renderFan(this.shape1, ps, vc, light, overlay, color, Axis.XP, fanSpin);
        // orig ModelRotator.java:60-65 — shape2 fan spun about the Y axis.
        renderFan(this.shape2, ps, vc, light, overlay, color, Axis.YP, fanSpin);
        // orig ModelRotator.java:68-73 — shape3 fan spun about the Z axis.
        renderFan(this.shape3, ps, vc, light, overlay, color, Axis.ZP, fanSpin);

        if (ri != null) {
            // orig ModelRotator.java:75-78 — advance 2° per rendered frame, wrap at 359°.
            ri.rf1 += 2.0f;
            if (ri.rf1 > 359.0f) {
                ri.rf1 = 0.0f;
            }
        }
    }

    /**
     * Renders {@code blade} 8 times at 45° Z-rotation increments inside a pose
     * rotated {@code spinDegrees} about {@code axis}, reproducing the original
     * glRotatef + 8-iteration render loop (orig ModelRotator.java:52-57).
     */
    private static void renderFan(ModelPart blade, PoseStack ps, VertexConsumer vc,
                                  int light, int overlay, int color, Axis axis, float spinDegrees) {
        ps.pushPose();
        ps.mulPose(axis.rotationDegrees(spinDegrees));
        float bladeAngle = 0.0f;
        for (int i = 0; i < 8; ++i) {
            blade.zRot = bladeAngle;
            blade.render(ps, vc, light, overlay, color);
            bladeAngle += FAN_STEP;
        }
        ps.popPose();
    }
}
