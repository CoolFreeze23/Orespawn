package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import danger.orespawn.entity.EntityRotator;
import danger.orespawn.entity.pose.RotatorPose;
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
    /** orig ModelRotator.java:75 — degrees added to {@code rf1} per rendered frame. */
    public static final float FAN_ADVANCE_DEGREES = 2.0f;
    /** orig ModelRotator.java:76 — {@code rf1} wraps to 0 once it exceeds this. */
    public static final float FAN_WRAP_DEGREES = 359.0f;

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
        poseFrom(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
    }

    /**
     * The former {@link #setupAnim} body, entity-free: captures the per-entity
     * fan angle holder that {@link #renderToBuffer} spins by and advances. The
     * GeckoLib candidate ({@code RotatorGeoReplacement}) poses from the same
     * {@link RotatorPose}, which is what lets the Slice 4c parity harness drive
     * both sides from a declared {@code rf1} preset.
     */
    public void poseFrom(RotatorPose entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                         float netHeadYaw, float headPitch) {
        this.renderInfo = entity.getRenderInfo();
    }

    /**
     * The degrees-to-radians conversion {@code Axis.rotationDegrees} applies to
     * the fan spin in {@link #renderFan} (1.21.1 bytecode: {@code ldc 0.017453292f;
     * fmul}; the constant is {@code (float) (Math.PI / 180.0)}), so the GeckoLib
     * hook feeds its fan group bones the identical float.
     */
    public static float fanSpinRadians(float degrees) {
        return degrees * ((float) (Math.PI / 180.0));
    }

    /**
     * orig ModelRotator.java:75-78 — advance the fan 2° per rendered frame, wrap
     * at 359°. Shared by the classic draw loop and the GeckoLib hook, so the two
     * renderers step the per-entity angle identically.
     */
    public static void advanceFanSpin(RenderInfo ri) {
        ri.rf1 += FAN_ADVANCE_DEGREES;
        if (ri.rf1 > FAN_WRAP_DEGREES) {
            ri.rf1 = 0.0f;
        }
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
            advanceFanSpin(ri);
        }
    }

    /**
     * Renders {@code blade} 8 times at 45° Z-rotation increments inside a pose
     * rotated {@code spinDegrees} about {@code axis}, reproducing the original
     * glRotatef + 8-iteration render loop (orig ModelRotator.java:52-57).
     *
     * <p>Slice 4c: the GeckoLib rig reproduces this loop statically — draw
     * {@code k} of a blade is the clone bone {@code <blade>__i<k>} (its own
     * Z rotation {@code k * FAN_STEP}) under the fan group bone
     * {@code <blade>__fan} that the hook spins about {@code axis}; the
     * converter's render-instance expansion ({@code tools/s4_model_proofs.json},
     * {@code render_instances}) is what emits them.</p>
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
