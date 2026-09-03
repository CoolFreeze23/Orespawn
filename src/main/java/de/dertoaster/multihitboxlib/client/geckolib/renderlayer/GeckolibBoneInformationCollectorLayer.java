package de.dertoaster.multihitboxlib.client.geckolib.renderlayer;

import org.joml.Vector3d;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import de.dertoaster.multihitboxlib.api.IMHLibExtendedRenderLayer;
import de.dertoaster.multihitboxlib.client.IBoneInformationCollectorLayerCommonLogic;
import de.dertoaster.multihitboxlib.util.MHLibCounters;
import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.GeoReplacedEntityRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class GeckolibBoneInformationCollectorLayer<T extends GeoAnimatable> extends GeoRenderLayer<T> implements IBoneInformationCollectorLayerCommonLogic<GeoBone>, IMHLibExtendedRenderLayer{

	private Stack<Tuple<Vector3d, Vector3d>> scaleAndRotationStack = new ObjectArrayList<>();
	private Vector3d currentScaling = new Vector3d(1,1,1);
	private Vector3d currentRotation = new Vector3d(0,0,0);
	
	public GeckolibBoneInformationCollectorLayer(GeoRenderer<T> entityRendererIn) {
		super(entityRendererIn);
	}
	
	/**
	 * BUG-044 (2026-09-04): whether the pass in flight collects, decided once per rendered entity by
	 * {@link IBoneInformationCollectorLayerCommonLogic#beginRenderPass} from the ENTITY's render-tick
	 * stamp and read per bone by {@code onRenderBone}. One flag per layer suffices because a renderer
	 * draws one entity at a time (Pre event, its bones, Post event); the stamp itself lives on the
	 * entity, which is what keeps two entities sharing this renderer from starving each other.
	 */
	private boolean collectingPass = false;

	/**
	 * ENT-S-092 (2026-09-03): the body yaw the frame being collected was rendered with, as the
	 * y-rotation that takes a yaw-0 profile pivot into the rendered frame (see
	 * {@link #bodyYawRotationTerm(Entity, float)}). Set per bone by {@link #renderForBone} and folded
	 * into every rotation vector this layer ships ({@link #getRotationVector()}), so the server's and
	 * the trusting client's {@code MHLibPartEntity.applyInformation} rotate the pivot in the SAME
	 * frame the bone world position was produced in. Without it a pivot authored at yaw 0 stayed
	 * unturned while the bone position turned with the body (33.7 blocks off for the Queen's wing
	 * at yaw 90).
	 */
	private double bodyYawRotationTerm = 0.0D;

	@Override
	public boolean isBoneCollectionActive() {
		if (this.renderer instanceof GeoReplacedEntityRenderer<?, ?> grer) {
			return IBoneInformationCollectorLayerCommonLogic.shouldCollectModelBones(
					grer.getCurrentEntity());
		}

		// Preserve the existing GeoEntity path, including The Queen.
		return true;
	}
	
	@Override
	public void renderForBone(PoseStack poseStack, T animatable, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
		Entity entity = null;
        if (!(animatable instanceof Entity)) {
            if (this.renderer instanceof GeoReplacedEntityRenderer<?, ?> grer) {
                entity = grer.getCurrentEntity();
            }
        } else {
            entity = (Entity)animatable;
        }
		this.bodyYawRotationTerm = bodyYawRotationTerm(entity, partialTick);
		this.onRenderBone(poseStack, entity, bone, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);
	}

	/**
	 * The yaw {@code GeoEntityRenderer.actuallyRender} rendered this frame with (4.8.4 bytecode offsets
	 * 59-67: {@code Mth.rotLerp(partialTick, yBodyRotO, yBodyRot)} for a LivingEntity, 0 otherwise), as
	 * the y-rotation the pivot needs. {@code applyRotations} (offsets 44-59) turns the model by
	 * {@code Axis.YP.rotationDegrees(180 - yaw)}; the bone world positions carry that turn and the
	 * profile pivots are authored in the yaw-0 frame (turned by 180), so relative to them the frame is
	 * turned by YP(-yaw). {@code Vec3.yRot(a)} is exactly {@code Axis.YP.rotation(a)} (x' = x cos a +
	 * z sin a, z' = z cos a - x sin a), hence the term is {@code -toRadians(yaw)}. The riding clamp and
	 * the isShaking jitter of applyRotations are not reproduced (no bone-synced entity rides or
	 * freezes).
	 */
	public static double bodyYawRotationTerm(Entity entity, float partialTick) {
		if (!(entity instanceof LivingEntity living)) {
			return 0.0D;
		}
		return bodyYawRotationTerm(Mth.rotLerp(partialTick, living.yBodyRotO, living.yBodyRot));
	}

	public static double bodyYawRotationTerm(float lerpedBodyYawDegrees) {
		return -Math.toRadians(lerpedBodyYawDegrees);
	}

	/**
	 * Folds the body-yaw term into a summed bone rotation so that the fixed chain
	 * {@code pivot.xRot(x).yRot(y).zRot(z)} in {@code MHLibPartEntity.applyInformation} (:424-427) equals
	 * the exact {@code (pivot.xRot(rx).yRot(ry).zRot(rz)).yRot(yawTerm)}: the composed matrix
	 * {@code Ry(yaw) * Rz(rz) * Ry(ry) * Rx(rx)} is re-decomposed into the same Z*Y*X Euler order (Vec3
	 * conventions: xRot(a) = [[1,0,0],[0,c,s],[0,-s,c]], yRot(a) = [[c,0,s],[0,1,0],[-s,0,c]],
	 * zRot(a) = [[c,s,0],[-s,c,0],[0,0,1]]; a vector chain v.xRot(a).yRot(b).zRot(c) is Rz(c)*Ry(b)*Rx(a)*v).
	 * Adding the term to y alone would only be exact for rz == 0: a z-rotating chain (a wing flap) would
	 * then be applied about the world z axis instead of the body's. A zero term returns the input
	 * unchanged, so the yaw-0 behaviour is untouched.
	 */
	public static Vec3 foldBodyYaw(double rx, double ry, double rz, double yawTerm) {
		if (MHLibCounters.ENABLED) {
			MHLibCounters.CLIENT_FOLDS.increment();
		}
		if (yawTerm == 0.0D) {
			return new Vec3(rx, ry, rz);
		}
		final double[][] m = mul(rotY(yawTerm), mul(rotZ(rz), mul(rotY(ry), rotX(rx))));
		// M = Rz(c) * Ry(b) * Rx(a): row 2 = [-sin b, -cos b sin a, cos b cos a], column 0 = [cos c cos b, -sin c cos b, -sin b]
		final double sinB = -m[2][0];
		final double cosB = Math.sqrt(m[2][1] * m[2][1] + m[2][2] * m[2][2]);
		final double b = Math.atan2(sinB, cosB);
		final double a;
		final double c;
		if (cosB > 1.0E-9D) {
			a = Math.atan2(-m[2][1], m[2][2]);
			c = Math.atan2(-m[1][0], m[0][0]);
		} else {
			// gimbal lock (b = +-90 degrees): only a -+ c is determined; with c = 0, column 1 gives
			// M[0][1] = -sin b sin a and M[1][1] = cos a
			c = 0.0D;
			a = Math.atan2(-sinB * m[0][1], m[1][1]);
		}
		return new Vec3(a, b, c);
	}

	private static double[][] rotX(double angle) {
		final double c = Math.cos(angle);
		final double s = Math.sin(angle);
		return new double[][] { { 1, 0, 0 }, { 0, c, s }, { 0, -s, c } };
	}

	private static double[][] rotY(double angle) {
		final double c = Math.cos(angle);
		final double s = Math.sin(angle);
		return new double[][] { { c, 0, s }, { 0, 1, 0 }, { -s, 0, c } };
	}

	private static double[][] rotZ(double angle) {
		final double c = Math.cos(angle);
		final double s = Math.sin(angle);
		return new double[][] { { c, s, 0 }, { -s, c, 0 }, { 0, 0, 1 } };
	}

	private static double[][] mul(double[][] p, double[][] q) {
		final double[][] out = new double[3][3];
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				out[i][j] = p[i][0] * q[0][j] + p[i][1] * q[1][j] + p[i][2] * q[2][j];
			}
		}
		return out;
	}

	@Override
	public boolean isCollectingPass() {
		return this.collectingPass;
	}

	@Override
	public void setCollectingPass(boolean collecting) {
		this.collectingPass = collecting;
	}

	@Override
	public void calcScales(GeoBone bone) {
		Vector3d scale = this.getCurrentScaling();
		scale.x *= bone.getScaleX();
		scale.y *= bone.getScaleY();
		scale.z *= bone.getScaleZ();
		//this.scaleX *= bone.getScaleX();
		//this.scaleY *= bone.getScaleY();
		//this.scaleZ *= bone.getScaleZ();
	}

	@Override
	public String getBoneName(GeoBone bone) {
		return bone.getName();
	}

	@Override
	public Vec3 getBoneWorldPosition(GeoBone bone) {
		if (MHLibCounters.ENABLED) {
			MHLibCounters.CLIENT_WORLD_POS_READS.add(3); // three GeoBone.getWorldPosition() calls below
		}
        final Vec3 worldPos = new Vec3(bone.getWorldPosition().x, bone.getWorldPosition().y, bone.getWorldPosition().z);
		return worldPos;
	}

	@Override
	public boolean isBoneHidden(GeoBone bone) {
		return bone.isHidden();
	}

	@Override
	public Vec3 getScaleVector() {
		Vector3d scale = this.getCurrentScaling();
		return new Vec3(scale.x, scale.y, scale.z);
	}

	@Override
	public void setScales(int x, int y, int z) {
		Vector3d scale = this.getCurrentScaling();
		if (scale == null) {
			// BUG-044: the replaced-renderer path now runs the post hook too; a pass whose collection was
			// inactive (IMHLibExtendedRenderLayer.onPreRender early-out) has no seeded vectors to reset.
			return;
		}
		scale.x *= x;
		scale.y *= y;
		scale.z *= z;
		/*this.scaleX = x;
		this.scaleY = y;
		this.scaleZ = z;*/
	}

	@Override
	public void calcRotations(GeoBone bone) {
		Vector3d rot = this.getCurrentRotation();
		rot.x += bone.getRotX();
		rot.y += bone.getRotY();
		rot.z += bone.getRotZ();
		/*this.rotX += bone.getRotX();
		this.rotY += bone.getRotY();
		this.rotZ += bone.getRotZ();*/
	}

	@Override
	public Vec3 getRotationVector() {
		Vector3d rot = this.getCurrentRotation();
		// ENT-S-092: ship the summed bone rotation with the body yaw folded in (see bodyYawRotationTerm).
		return foldBodyYaw(rot.x, rot.y, rot.z, this.bodyYawRotationTerm);
	}

	@Override
	public void setRotations(int x, int y, int z) {
		Vector3d rot = this.getCurrentRotation();
		if (rot == null) {
			return; // see setScales
		}
		rot.x = x;
		rot.y = y;
		rot.z = z;
		/*this.rotX = x;
		this.rotY = y;
		this.rotZ = z;*/
	}

	@Override
	public void pushToStack(Vector3d scaling, Vector3d rotation) {
		this.scaleAndRotationStack.push(new Tuple<>(scaling, rotation));
	}

	@Override
	public Tuple<Vector3d, Vector3d> popStack() {
		return this.scaleAndRotationStack.pop();
	}

	@Override
	public Vector3d getCurrentScaling() {
		return this.currentScaling;
	}

	@Override
	public Vector3d getCurrentRotation() {
		return this.currentRotation;
	}

	@Override
	public void applyCurrentValues(Vector3d scaling, Vector3d rotation) {
		this.currentRotation = rotation;
		this.currentScaling = scaling;
	}

	@Override
	public void resetStack() {
		this.scaleAndRotationStack = new ObjectArrayList<>();
	}

}
