package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import software.bernie.geckolib.cache.object.GeoCube;
import software.bernie.geckolib.cache.object.GeoQuad;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.util.RenderUtil;

/**
 * ENT-S-161 (owner 2026-09-13, item 3; PN-027): GeckoLib 4.8.4's default {@code GeoRenderer.renderCube}
 * (bytecode 0-126) transforms every quad's normal by the pose's normal matrix (offsets 76-93) and then hands
 * it to {@code RenderUtil.fixInvertedFlatCube(cube, normal)} (98) before {@code createVerticesOfQuad}
 * (115). That helper (0-129) multiplies the normal's x by -1 when x is negative and the cube is flat in
 * y or z, its y by -1 when y is negative and the cube is flat in x or z, and its z by -1 when z is
 * negative and the cube is flat in x or y - component-wise, on the TRANSFORMED (pose-space) normal. A flat
 * cube whose pose-space normals stay axis-aligned survives (the harness's bind pose without entity yaw: the
 * Vortex's plate in the s4 proof); a ROTATED one (the Firefly's wings under Z 0.698, the Cloud Shark's and the
 * Gold Fish's fins) - or ANY flat cube at a generic entity yaw in-game - gets a vector that is neither the
 * classic renderer's normal nor a reflection of it, its two faces no longer opposite: the candidate is lit
 * differently from the classic renderer there (the surface leg's {@code RENDERER MAPPING MISMATCH} at 1e-6).
 *
 * <p>The classic renderer ({@code ModelPart.Cube.compile}) lights the true transformed normal, which is the
 * parity target, so the replacement seam draws GeckoLib's own {@code renderCube} minus that one call: the
 * pivot translation and rotation, the normal matrix, the pose copy and the per-quad loop are the library's,
 * statement for statement. One static so that {@link OreSpawnGeoReplacedEntityRenderer} and the harness's
 * capturing renderer ({@code G1ModelProbe.CapturingGeoRenderer}) draw through the same code and the proof
 * draws what the seam draws. A deliberate divergence from the library, scoped to the seam (the Queen's native
 * renderer keeps stock semantics until her own ruling), recorded as PN-027 with the upstream report text.</p>
 */
public final class TrueNormalCubeRenderer {
    private TrueNormalCubeRenderer() {
    }

    /**
     * {@code GeoRenderer.renderCube} (4.8.4, offsets 0-126) without {@code RenderUtil.fixInvertedFlatCube}:
     * the quad's normal transformed by the pose's normal matrix is what every vertex carries.
     */
    public static void render(GeoRenderer<?> renderer, PoseStack poseStack, GeoCube cube, VertexConsumer buffer,
                              int packedLight, int packedOverlay, int colour) {
        RenderUtil.translateToPivotPoint(poseStack, cube);
        RenderUtil.rotateMatrixAroundCube(poseStack, cube);
        RenderUtil.translateAwayFromPivotPoint(poseStack, cube);
        Matrix3f normalisedPoseState = poseStack.last().normal();
        Matrix4f poseState = new Matrix4f(poseStack.last().pose());
        for (GeoQuad quad : cube.quads()) {
            if (quad == null) {
                continue;
            }
            Vector3f normal = normalisedPoseState.transform(new Vector3f(quad.normal()));
            renderer.createVerticesOfQuad(quad, poseState, normal, buffer, packedLight, packedOverlay, colour);
        }
    }
}
