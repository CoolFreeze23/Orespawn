package danger.orespawn.entity.client;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import org.joml.Matrix4d;
import org.joml.Vector3d;
import software.bernie.geckolib.animation.AnimationProcessor;
import software.bernie.geckolib.animation.state.BoneSnapshot;
import software.bernie.geckolib.cache.object.GeoBone;

/**
 * The classic FLAT rig as the classic statements pose it, resolved onto the bake's parent-child hierarchy (the FK slice,
 * owner 2026-09-15, closing set, item 4 and closing set continued second, item 35 (5); design section 5 "FK-chained": the
 * Alien and the Emperor Scorpion become real parent-child bone hierarchies rather than flat rigs whose child pivots the
 * classic code rewrites by trigonometry).
 *
 * <p>THE MAPPING. A classic model of this class is a flat {@code ModelPart} tree - every part a child of the unnamed root,
 * so a part's rotation point {@code (x, y, z)} and rotations {@code (xRot, yRot, zRot)} ARE its world transform,
 * {@code W = T(x, y, z) * Rz * Ry * Rx} ({@code ModelPart.translateAndRotate}) - and its {@code setupAnim} writes a chain
 * child's world pivot from its parent's by trigonometry (the Alien's tail rings 10 units along the ring before them, the
 * Emperor Scorpion's tail segments 9 / 10 / 3 units along the segment before them ...). The converter ships the same parts
 * as a HIERARCHY (child bones parented to their chain parents, the children's pivots DERIVED in Blockbench terms - the
 * classic bind pivot carried back through the parent's bind rotation - and their bind rotations LOCAL:
 * {@code tools/layer_definition_to_geo.py}, the hierarchy form), and GeckoLib composes a parent's transform onto its
 * children ({@code RenderUtil.prepMatrixForBone}: translate to the bone, to its pivot, rotate Z then Y then X, away from
 * the pivot - a child's {@code pos} and rotation are in its PARENT's frame, its pivot offset from the parent's). So the
 * classic statements are kept exactly as they are - they write the FLAT values into this buffer (every part's world
 * rotation point and world rotation, initialised to the bind) - and {@link #resolve} maps them onto the bake: a root bone
 * takes exactly the channels the statements wrote (the flat form, unchanged, so an unwritten channel stays the bake's bind
 * bit for bit and a written one is the classic float); a parented bone whose chain was written takes the PARENT-RELATIVE
 * transform that reproduces its flat world transform, {@code L = W_parent^-1 * W_child}, decomposed into ModelPart's own
 * form - the translation {@code (x, y, z)} in the parent's frame (through {@link OreSpawnGeoReplacement#moveTo}, which is
 * relative to the parent's pivot for a child bone) and the ZYX Euler angles {@code (xRot, yRot, zRot)} of the linear block
 * (through {@code rotateX / Y / Z}). The world transform of every link is then the classic's: {@code W_parent * L =
 * W_child}. Evaluated in double precision (JOML {@code Matrix4d}) from the float classic values and written back as
 * floats; the harness's chain-link leg ({@code tools/g1_render_parity.py chain_link_parity}) compares the bake's rendered
 * world matrices with the classic's at every link within the geometry leg's tolerance.</p>
 *
 * <p>Where the classic follow is a true joint follow (the Emperor Scorpion's tail: the next segment 9 / 10 units along
 * the previous segment's pitch) the resolved local position is the constant bind offset and the local rotation the
 * classic's per-link increment; where it is not (the Alien's arm chains follow the yaw only, ignoring the arm's bind
 * roll; the Emperor Scorpion's legs place the next segment by {@code 6 - 6|sin(yaw)|}, not {@code 6 cos(yaw)}) the local
 * position moves within the parent's frame exactly as much as the classic pivot does - the world transform is the
 * classic's either way, which is the ruling's proof, not a re-authored rig.</p>
 *
 * <p>THE FRAME (TEST-015, the basis facts of {@link OreSpawnGeoReplacement}: internal space is classic space reflected in
 * X and Y). The bind of every bone comes from the bake, never from a bone's current state (a bone written last frame is
 * being eased back toward its initial snapshot when the hook runs): the pivot (immutable, absolute; internal
 * {@code (-x, 24 - y, z)} of the classic, so the classic is {@code (-pivotX, 24 - pivotY, pivotZ)}) and the initial
 * snapshot GeckoLib saved when the processor registered the bone ({@code AnimationProcessor.registerGeoBone ->
 * GeoBone.saveInitialSnapshot}, 4.8.4 bytecode; internal rotation {@code (-xRot, -yRot, zRot)} of the classic, so the
 * classic is {@code (-rotX, -rotY, rotZ)}); a child's flat (world) bind rotation is its parents' composed onto its local
 * one and decomposed again, its flat bind position its parent's carried forward through the parent's bind rotation - the
 * converter's derivations inverted. Before TEST-015 (internal space reflected in Y alone) the same reads were
 * {@code (pivotX, ...)} and {@code (-rotX, rotY, -rotZ)}; nothing else of the mapping depends on the frame.</p>
 */
public final class FlatRig {
    private static final double GIMBAL_EPSILON = 1.0e-12;
    private final AnimationProcessor<?> processor;
    private final Map<String, Part> parts = new LinkedHashMap<>();

    /** One classic part: its flat bind and the flat values the statements wrote this frame. */
    private static final class Part {
        final GeoBone bone;
        final Part parent;
        /** Classic (x, y, z) rotation point and (xRot, yRot, zRot), world, model units / radians; the bind until written. */
        final float[] position;
        final float[] rotation;
        final boolean[] positionWritten = new boolean[3];
        final boolean[] rotationWritten = new boolean[3];
        Boolean dirty;

        Part(GeoBone bone, Part parent, float[] bindPosition, float[] bindRotation) {
            this.bone = bone;
            this.parent = parent;
            this.position = bindPosition.clone();
            this.rotation = bindRotation.clone();
        }

        boolean written() {
            return this.positionWritten[0] || this.positionWritten[1] || this.positionWritten[2]
                    || this.rotationWritten[0] || this.rotationWritten[1] || this.rotationWritten[2];
        }

        /** Whether this part or any part above it in the bake's hierarchy was written: its local transform must be resolved. */
        boolean dirty() {
            if (this.dirty == null) {
                this.dirty = written() || (this.parent != null && this.parent.dirty());
            }
            return this.dirty;
        }

        /** The flat world transform, T(position) * Rz * Ry * Rx, in double precision. */
        Matrix4d world() {
            return new Matrix4d().translation(this.position[0], this.position[1], this.position[2])
                    .rotateZYX(this.rotation[2], this.rotation[1], this.rotation[0]);
        }
    }

    private FlatRig(AnimationProcessor<?> processor) {
        this.processor = processor;
    }

    /** Every bone of the bake at its flat bind, in the classic rig's terms. */
    public static FlatRig bind(AnimationProcessor<?> processor) {
        FlatRig rig = new FlatRig(processor);
        Collection<GeoBone> bones = processor.getRegisteredBones();
        if (bones.isEmpty()) {
            throw new IllegalStateException("the animation processor has no registered bones (no active model)");
        }
        for (GeoBone bone : bones) {
            rig.part(bone);
        }
        return rig;
    }

    /** A bone's geo pivot in classic terms: the internal pivot is (-x, 24 - y, z) of the classic (the basis facts). */
    private static float[] classicPivot(GeoBone bone) {
        return new float[] {-bone.getPivotX(), 24.0F - bone.getPivotY(), bone.getPivotZ()};
    }

    private Part part(GeoBone bone) {
        Part existing = this.parts.get(bone.getName());
        if (existing != null) {
            return existing;
        }
        Part parent = bone.getParent() == null ? null : part(bone.getParent());
        BoneSnapshot snapshot = bone.getInitialSnapshot();
        if (snapshot == null) {
            throw new IllegalStateException("GeckoLib bone " + bone.getName() + " has no initial snapshot (not registered)");
        }
        // the geo's pivot in classic terms (GeckoLib bakes every pivot absolute): a root's is the classic flat rotation
        // point itself, a child's the converter's derived pivot - the root branch of OreSpawnGeoReplacement.classicBindPivot
        float[] bindPosition = classicPivot(bone);
        // the bake's own rotation in classic terms (internal (-xRot, -yRot, zRot)): a root's flat bind; a child's LOCAL bind
        float[] local = {-snapshot.getRotX(), -snapshot.getRotY(), snapshot.getRotZ()};
        float[] bindRotation = local;
        if (parent != null) {
            // the flat (world) bind rotation: the parent's flat bind composed onto the local one, decomposed again - as the
            // classic authored it (classicBranch: a classic setupAnim writes one axis over the bind, so the triple must be
            // the classic's own, not an equivalent one; the converter checks the convention recovers it for every link)
            Matrix4d world = new Matrix4d().rotateZYX(parent.rotation[2], parent.rotation[1], parent.rotation[0])
                    .rotateZYX(local[2], local[1], local[0]);
            double[] angles = classicBranch(eulerZYX(world));
            bindRotation = new float[] {(float) angles[0], (float) angles[1], (float) angles[2]};
            // the flat (world) bind rotation point: GeckoLib rotates a child's pivot with its parent, so the converter
            // stored the classic pivot carried back through the parent's bind rotation (the derived pivot, Blockbench
            // terms) - carried forward again here: classic_c = classic_p + R_p * (P_c - P_p), the converter's derivation
            // inverted (the flat bind of a root is its own pivot, so the recursion closes at the chain's root)
            float[] parentPivot = classicPivot(parent.bone);
            Vector3d offset = new Vector3d(bindPosition[0] - parentPivot[0], bindPosition[1] - parentPivot[1],
                    bindPosition[2] - parentPivot[2]);
            new Matrix4d().rotateZYX(parent.rotation[2], parent.rotation[1], parent.rotation[0]).transformDirection(offset);
            bindPosition = new float[] {(float) (parent.position[0] + offset.x), (float) (parent.position[1] + offset.y),
                    (float) (parent.position[2] + offset.z)};
        }
        Part part = new Part(bone, parent, bindPosition, bindRotation);
        this.parts.put(bone.getName(), part);
        return part;
    }

    private Part named(String name) {
        Part part = this.parts.get(name);
        if (part == null) {
            throw new IllegalStateException("GeckoLib rig is missing bone " + name);
        }
        return part;
    }

    /** {@code part.xRot = xRot} in the classic flat rig. */
    public void rotateX(String name, float xRot) {
        Part part = named(name);
        part.rotation[0] = xRot;
        part.rotationWritten[0] = true;
    }

    /** {@code part.yRot = yRot} in the classic flat rig. */
    public void rotateY(String name, float yRot) {
        Part part = named(name);
        part.rotation[1] = yRot;
        part.rotationWritten[1] = true;
    }

    /** {@code part.zRot = zRot} in the classic flat rig. */
    public void rotateZ(String name, float zRot) {
        Part part = named(name);
        part.rotation[2] = zRot;
        part.rotationWritten[2] = true;
    }

    /** {@code part.x = x; part.y = y; part.z = z} in the classic flat rig (the world rotation point). */
    public void moveTo(String name, float x, float y, float z) {
        Part part = named(name);
        part.position[0] = x;
        part.position[1] = y;
        part.position[2] = z;
        part.positionWritten[0] = part.positionWritten[1] = part.positionWritten[2] = true;
    }

    /** {@code part.z = z; part.x = x} with the part's y left as it is (the follows of the Alien write z and x). */
    public void moveXZ(String name, float x, float z) {
        Part part = named(name);
        part.position[0] = x;
        part.position[2] = z;
        part.positionWritten[0] = part.positionWritten[2] = true;
    }

    /** The part's classic rotation point as the statements see it: the flat bind until this frame's statements wrote it. */
    public float[] position(String name) {
        return named(name).position.clone();
    }

    /** The part's classic rotation as the statements see it: the flat bind until this frame's statements wrote it. */
    public float[] rotation(String name) {
        return named(name).rotation.clone();
    }

    /**
     * Writes the flat pose onto the bake: a root bone takes the channels the statements wrote, exactly (an unwritten
     * channel is left at the bake's bind); a parented bone whose chain was written takes the parent-relative transform
     * that reproduces its flat world transform (the mapping above); an untouched chain stays at the bake's bind.
     */
    public void resolve() {
        for (Part part : this.parts.values()) {
            String name = part.bone.getName();
            if (part.parent == null) {
                if (part.rotationWritten[0]) {
                    OreSpawnGeoReplacement.rotateX(this.processor, name, part.rotation[0]);
                }
                if (part.rotationWritten[1]) {
                    OreSpawnGeoReplacement.rotateY(this.processor, name, part.rotation[1]);
                }
                if (part.rotationWritten[2]) {
                    OreSpawnGeoReplacement.rotateZ(this.processor, name, part.rotation[2]);
                }
                if (part.positionWritten[0] || part.positionWritten[1] || part.positionWritten[2]) {
                    OreSpawnGeoReplacement.moveTo(this.processor, name, part.position[0], part.position[1], part.position[2]);
                }
                continue;
            }
            if (!part.dirty()) {
                continue;
            }
            // L = W_parent^-1 * W_child: the child's transform in the parent's frame (ModelPart's own child form)
            Matrix4d local = part.parent.world().invert().mul(part.world());
            double[] angles = eulerZYX(local);
            OreSpawnGeoReplacement.moveTo(this.processor, name, (float) local.m30(), (float) local.m31(), (float) local.m32());
            OreSpawnGeoReplacement.rotateX(this.processor, name, (float) angles[0]);
            OreSpawnGeoReplacement.rotateY(this.processor, name, (float) angles[1]);
            OreSpawnGeoReplacement.rotateZ(this.processor, name, (float) angles[2]);
        }
    }

    /**
     * The classic (xRot, yRot, zRot) of a rotation {@code M = Rz * Ry * Rx} (JOML's {@code mCR}: column C, row R, so
     * {@code m02} is the third row's first entry, {@code -sin(yRot)}). Away from the gimbal {@code xRot = atan2(M[2][1],
     * M[2][2])} and {@code zRot = atan2(M[1][0], M[0][0])}; at it (yRot = +-pi/2) x and z are not separable - z is taken
     * as 0 and x from the first row ({@code M[0][1] = sin(y) sin(x)}, {@code M[0][2] = sin(y) cos(x)}). The same
     * decomposition as the converter's {@code euler_angles_zyx}.
     */
    static double[] eulerZYX(Matrix4d m) {
        double sy = Math.max(-1.0, Math.min(1.0, -m.m02()));
        if (Math.abs(sy) < 1.0 - GIMBAL_EPSILON) {
            return new double[] {Math.atan2(m.m12(), m.m22()), Math.asin(sy), Math.atan2(m.m01(), m.m00())};
        }
        return new double[] {Math.atan2(sy * m.m10(), sy * m.m20()), Math.copySign(Math.PI / 2.0, sy), 0.0};
    }

    /**
     * The ZYX triple of a bind rotation as a classic rig authors it: a rotation has two ZYX triples, {@code (x, y, z)}
     * with {@code y} in {@code [-pi/2, pi/2]} and {@code (x + pi, pi - y, z + pi)}; the classic's own is the one with the
     * smaller {@code |xRot| + |zRot|} (a part turned past a quarter turn about Y is authored as a yaw, never as a
     * half-turn pitch and roll beside it) - the convention the converter checks for every link
     * ({@code euler_angles_zyx_classic_branch}).
     */
    static double[] classicBranch(double[] first) {
        double[] second = {wrap(first[0] + Math.PI), wrap(Math.PI - first[1]), wrap(first[2] + Math.PI)};
        return Math.abs(second[0]) + Math.abs(second[2]) < Math.abs(first[0]) + Math.abs(first[2]) - GIMBAL_EPSILON
                ? second : first;
    }

    /** An angle into {@code (-pi, pi]}. */
    private static double wrap(double value) {
        double wrapped = (value + Math.PI) % (2.0 * Math.PI);
        if (wrapped <= 0.0) {
            wrapped += 2.0 * Math.PI;
        }
        return wrapped - Math.PI;
    }
}
