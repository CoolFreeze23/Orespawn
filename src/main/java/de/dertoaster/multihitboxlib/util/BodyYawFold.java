package de.dertoaster.multihitboxlib.util;

import net.minecraft.world.phys.Vec3;

/**
 * The body-yaw fold of the client's bone collector, as common code (ENT-S-173): the server-side fallback of
 * {@code IMultipartEntity.alignSynchedSubParts} places a part with the same composition the collector ships, so a
 * creature no client has sent bone positions for carries its boxes on the drawn rest segments, turned with its body.
 * Moved from {@code GeckolibBoneInformationCollectorLayer} (ENT-S-092), which delegates here; the maths is unchanged.
 *
 * <p>{@link #bodyYawRotationTerm}: the yaw {@code GeoEntityRenderer.actuallyRender} renders with is
 * {@code Mth.rotLerp(partialTick, yBodyRotO, yBodyRot)}; {@code applyRotations} turns the model by
 * {@code Axis.YP.rotationDegrees(180 - yaw)}, the bone world positions carry that turn, and the profile pivots are
 * authored in the yaw-0 frame (turned by 180), so relative to them the frame is turned by YP(-yaw). {@code Vec3.yRot(a)}
 * is exactly {@code Axis.YP.rotation(a)} (x' = x cos a + z sin a, z' = z cos a - x sin a), hence the term is
 * {@code -toRadians(yaw)}.</p>
 *
 * <p>{@link #foldBodyYaw}: folds the term into a summed bone rotation so that the fixed chain
 * {@code pivot.xRot(x).yRot(y).zRot(z)} in {@code MHLibPartEntity.applyInformation} equals the exact
 * {@code (pivot.xRot(rx).yRot(ry).zRot(rz)).yRot(yawTerm)}: the composed matrix {@code Ry(yaw) * Rz(rz) * Ry(ry) * Rx(rx)}
 * is re-decomposed into the same Z*Y*X Euler order (Vec3 conventions: xRot(a) = [[1,0,0],[0,c,s],[0,-s,c]],
 * yRot(a) = [[c,0,s],[0,1,0],[-s,0,c]], zRot(a) = [[c,s,0],[-s,c,0],[0,0,1]]; a vector chain v.xRot(a).yRot(b).zRot(c)
 * is Rz(c)*Ry(b)*Rx(a)*v). Adding the term to y alone would only be exact for rz == 0: a z-rotating chain (a wing flap)
 * would then be applied about the world z axis instead of the body's. A zero term returns the input unchanged.</p>
 */
public final class BodyYawFold {

	private BodyYawFold() {
	}

	public static double bodyYawRotationTerm(float lerpedBodyYawDegrees) {
		return -Math.toRadians(lerpedBodyYawDegrees);
	}

	public static Vec3 foldBodyYaw(double rx, double ry, double rz, double yawTerm) {
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

	/** {@code MHLibPartEntity.applyInformation}'s pivot chain: {@code v.xRot(rx).yRot(ry).zRot(rz)}. */
	public static Vec3 rotateLikeApplyInformation(Vec3 v, Vec3 rotation) {
		return v.xRot((float) rotation.x).yRot((float) rotation.y).zRot((float) rotation.z);
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
}
