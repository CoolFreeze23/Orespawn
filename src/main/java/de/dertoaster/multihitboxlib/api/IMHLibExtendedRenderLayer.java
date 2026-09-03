package de.dertoaster.multihitboxlib.api;

import org.joml.Vector3d;
import net.minecraft.util.Tuple;

public interface IMHLibExtendedRenderLayer {

	/**
	 * Whether this layer needs its recursive collection lifecycle for the
	 * entity currently being rendered. Non-collector layers retain the legacy
	 * always-active behavior.
	 */
	default boolean isBoneCollectionActive() {
		return true;
	}

	void pushToStack(Vector3d scaling, Vector3d rotation);
	
	Tuple<Vector3d, Vector3d> popStack();
	
	Vector3d getCurrentScaling();
	Vector3d getCurrentRotation();
	
	void applyCurrentValues(Vector3d scaling, Vector3d rotation);
	
	void resetStack();
	
	static final Vector3d DEFAULT_SCALING = new Vector3d(1, 1, 1);
	static final Vector3d DEFAULT_ROTATION = new Vector3d(0,0,0);
	
	default void resetCurrentValues() {
		applyCurrentValues(null, null);
	}
	
	default void onPostRender() {
		if (!this.isBoneCollectionActive()) {
			return;
		}

		this.resetStack();
		this.resetCurrentValues();
	}
	
	default void onPreRender() {
		if (!this.isBoneCollectionActive()) {
			return;
		}

		this.resetStack();
		// BUG-043 (2026-09-03): the layer's calcScales/calcRotations mutate the CURRENT vectors in
		// place (GeckolibBoneInformationCollectorLayer.calcRotations: rot.x += bone.getRotX() ...),
		// and onRenderRecursivelyStart pushes copies while the first pop only replaces the current
		// reference, so seeding with the shared static instances corrupted DEFAULT_ROTATION /
		// DEFAULT_SCALING by the first-descendant chain's rotation every frame (+4 degrees of y per
		// frame on the Queen rig from root -> Lwing11 -> Lwing12) and every shipped bone rotation
		// inherited the drift. onPostRender's setRotations(0,0,0) hit a popped copy, never the
		// static. Seed with fresh copies so the statics stay (1,1,1) / (0,0,0).
		this.applyCurrentValues(new Vector3d(DEFAULT_SCALING), new Vector3d(DEFAULT_ROTATION));
	}
	
	default void onRenderRecursivelyStart() {
		if (!this.isBoneCollectionActive()) {
			return;
		}

		// Object needs to be cloned! Otherwise we will always modify the same thing
		final Vector3d currentRot = new Vector3d(this.getCurrentRotation());
		final Vector3d currentScale = new Vector3d(this.getCurrentScaling());
		if (currentRot != null && currentScale != null) {
			this.pushToStack(currentScale, currentRot);
		}
		
	}
	
	default void onRenderRecursivelyEnd() {
		if (!this.isBoneCollectionActive()) {
			return;
		}

		Tuple<Vector3d, Vector3d> tuple = this.popStack();
		this.applyCurrentValues(tuple.getA(), tuple.getB());
	}
	
	
}
