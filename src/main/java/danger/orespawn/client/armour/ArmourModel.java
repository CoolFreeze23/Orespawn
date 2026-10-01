package danger.orespawn.client.armour;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HumanoidArmorModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * One piece of a set in the modern style: a humanoid model whose parts carry the piece's cubes (ArmourGeometry). Each
 * frame it takes the wearer's pose from the armour model the armour layer posed (players, armour stands and every mob
 * that wears armour, in every pose they take: walking, sneaking, swimming, riding, as babies) and shows the parts of its
 * slot, as vanilla shows its armour's.
 * <p>
 * The pieces are made on the player's armour boxes. A wearer whose armour model has those boxes moved or grown (a
 * zombie villager's helmet sits 2 higher on its taller head and its chestplate and boots are grown 0.1 over its robe;
 * Guard Villagers' guards have the taller head too) gets each part of the piece laid onto its own box the same way,
 * axis by axis, so the piece sits where vanilla puts the classic one. A smaller box keeps the player's size, so pieces
 * worn together never land on each other (a drowned's outer armour is baked at the inner deformation).
 * <p>
 * It draws its cubes itself, exactly as vanilla's ModelPart.render and ModelPart.Cube.compile do (the part's visibility,
 * its pose, then each face's four vertices with the face's normal), in HumanoidModel's own layout (the baby head scaled
 * 1.5 / 2 and lifted 16, the baby body scaled 1 / 2 and lowered 24), so the rebuilt box-UV faces are drawn whichever
 * renderer replaces the vanilla cube drawing.
 */
public final class ArmourModel extends HumanoidModel<LivingEntity> {
    /** The player's armour models (vanilla bakes PLAYER_OUTER_ARMOR at 1.0 and PLAYER_INNER_ARMOR at 0.5). */
    private static final HumanoidModel<LivingEntity> OUTER = reference(1.0F);
    private static final HumanoidModel<LivingEntity> INNER = reference(0.5F);

    private final Map<ModelPart, ArmourGeometry.Node> nodes;
    /** Each wearer's armour model's fits, worked out once: its boxes never change, only its pose. */
    private final Map<HumanoidModel<?>, float[][]> fitsByModel = new WeakHashMap<>();
    /** The fits on this wearer, for the head, body, right arm, left arm, right leg and left leg (null: none). */
    private float[][] fits = new float[6][];

    public ArmourModel(ArmourGeometry.Built built) {
        super(built.root());
        this.nodes = built.nodes();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void follow(HumanoidModel<?> original, EquipmentSlot slot) {
        HumanoidModel<?> reference = slot == EquipmentSlot.LEGS ? INNER : OUTER;
        fits = fitsByModel.computeIfAbsent(original, model -> fits(model, reference));
        ((HumanoidModel) original).copyPropertiesTo((HumanoidModel) this);
        setAllVisible(false);
        switch (slot) {
            case HEAD -> head.visible = true;
            case CHEST -> {
                body.visible = true;
                rightArm.visible = true;
                leftArm.visible = true;
            }
            case LEGS -> {
                body.visible = true;
                rightLeg.visible = true;
                leftLeg.visible = true;
            }
            case FEET -> {
                rightLeg.visible = true;
                leftLeg.visible = true;
            }
            default -> {
            }
        }
    }

    /** The cubes a part draws, with its child parts' (for the build's checks). */
    public int cubeCount(ModelPart part) {
        ArmourGeometry.Node node = nodes.get(part);
        return node == null ? 0 : count(node);
    }

    private static int count(ArmourGeometry.Node node) {
        int n = node.cubes().size();
        for (ArmourGeometry.Node child : node.children()) n += count(child);
        return n;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int light, int overlay, int color) {
        if (young) {
            poseStack.pushPose();
            float headScale = 1.5F / 2.0F;
            poseStack.scale(headScale, headScale, headScale);
            poseStack.translate(0.0F, 16.0F / 16.0F, 0.0F);
            draw(head, fits[0], poseStack, buffer, light, overlay, color);
            poseStack.popPose();
            poseStack.pushPose();
            float bodyScale = 1.0F / 2.0F;
            poseStack.scale(bodyScale, bodyScale, bodyScale);
            poseStack.translate(0.0F, 24.0F / 16.0F, 0.0F);
            drawBody(poseStack, buffer, light, overlay, color);
            poseStack.popPose();
        } else {
            draw(head, fits[0], poseStack, buffer, light, overlay, color);
            drawBody(poseStack, buffer, light, overlay, color);
        }
    }

    private void drawBody(PoseStack poseStack, VertexConsumer buffer, int light, int overlay, int color) {
        draw(body, fits[1], poseStack, buffer, light, overlay, color);
        draw(rightArm, fits[2], poseStack, buffer, light, overlay, color);
        draw(leftArm, fits[3], poseStack, buffer, light, overlay, color);
        draw(rightLeg, fits[4], poseStack, buffer, light, overlay, color);
        draw(leftLeg, fits[5], poseStack, buffer, light, overlay, color);
        draw(hat, null, poseStack, buffer, light, overlay, color);
    }

    private void draw(ModelPart part, float[] fit, PoseStack poseStack, VertexConsumer buffer, int light, int overlay,
                      int color) {
        ArmourGeometry.Node node = nodes.get(part);
        if (node != null) draw(node, fit, poseStack, buffer, light, overlay, color);
    }

    private static void draw(ArmourGeometry.Node node, float[] fit, PoseStack poseStack, VertexConsumer buffer, int light,
                             int overlay, int color) {
        ModelPart part = node.part();
        if (!part.visible || (node.cubes().isEmpty() && node.children().isEmpty())) return;
        poseStack.pushPose();
        part.translateAndRotate(poseStack);
        if (fit != null) {
            poseStack.translate(fit[3] / 16.0F, fit[4] / 16.0F, fit[5] / 16.0F);
            poseStack.scale(fit[0], fit[1], fit[2]);
        }
        if (!part.skipDraw) {
            PoseStack.Pose pose = poseStack.last();
            for (ModelPart.Cube cube : node.cubes()) compile(cube, pose, buffer, light, overlay, color);
        }
        for (ArmourGeometry.Node child : node.children()) draw(child, null, poseStack, buffer, light, overlay, color);
        poseStack.popPose();
    }

    /** Vanilla's ModelPart.Cube.compile on the cube's rebuilt faces. */
    private static void compile(ModelPart.Cube cube, PoseStack.Pose pose, VertexConsumer buffer, int light, int overlay,
                                int color) {
        Matrix4f matrix = pose.pose();
        Vector3f scratch = new Vector3f();
        for (ModelPart.Polygon polygon : cube.polygons) {
            Vector3f normal = pose.transformNormal(polygon.normal, scratch);
            float nx = normal.x(), ny = normal.y(), nz = normal.z();
            for (ModelPart.Vertex vertex : polygon.vertices) {
                Vector3f position = matrix.transformPosition(vertex.pos.x() / 16.0F, vertex.pos.y() / 16.0F,
                        vertex.pos.z() / 16.0F, scratch);
                buffer.addVertex(position.x(), position.y(), position.z(), color, vertex.u, vertex.v, overlay, light,
                        nx, ny, nz);
            }
        }
    }

    private static HumanoidModel<LivingEntity> reference(float deformation) {
        return new HumanoidModel<>(LayerDefinition.create(
                HumanoidArmorModel.createBodyLayer(new CubeDeformation(deformation)), 64, 32).bakeRoot());
    }

    private static float[][] fits(HumanoidModel<?> wearer, HumanoidModel<?> reference) {
        return new float[][]{fit(wearer.head, reference.head), fit(wearer.body, reference.body),
                fit(wearer.rightArm, reference.rightArm), fit(wearer.leftArm, reference.leftArm),
                fit(wearer.rightLeg, reference.rightLeg), fit(wearer.leftLeg, reference.leftLeg)};
    }

    /**
     * What lays the player's armour box of a part onto the wearer's, axis by axis: {scale x, y, z, shift x, y, z} in
     * model units. Null when they are the same box, when the wearer's box is not the player's box moved or grown (an
     * armour model of another shape keeps the player's fit), or when it is smaller on any axis: a drowned wears its
     * outer armour at the inner deformation, and a chestplate shrunk to it would lie on the leggings.
     */
    public static float[] fit(ModelPart wearer, ModelPart reference) {
        float[] w = box(wearer), r = box(reference);
        if (w == null || r == null) return null;
        float[] fit = new float[6];
        boolean same = true;
        for (int a = 0; a < 3; a++) {
            if (Math.abs(w[6 + a] - r[6 + a]) > 1.0E-4F) return null;
            float wearerSpan = w[3 + a] - w[a], referenceSpan = r[3 + a] - r[a];
            if (wearerSpan < 1.0E-4F || referenceSpan < 1.0E-4F) return null;
            float scale = wearerSpan / referenceSpan;
            if (scale < 1.0F || scale > 2.0F) return null;
            fit[a] = scale;
            fit[3 + a] = w[a] - scale * r[a];
            same &= scale == 1.0F && fit[3 + a] == 0.0F;
        }
        return same ? null : fit;
    }

    /** A part's first cube: {min x, y, z, max x, y, z} of its drawn corners, then its box's size {x, y, z}; or null. */
    private static float[] box(ModelPart part) {
        ModelPart.Cube[] first = new ModelPart.Cube[1];
        part.visit(new PoseStack(), (pose, path, index, cube) -> {
            if (first[0] == null && path.isEmpty()) first[0] = cube;
        });
        ModelPart.Cube cube = first[0];
        if (cube == null || cube.polygons.length == 0) return null;
        float[] box = {Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE,
                -Float.MAX_VALUE, cube.maxX - cube.minX, cube.maxY - cube.minY, cube.maxZ - cube.minZ};
        for (ModelPart.Polygon polygon : cube.polygons) {
            for (ModelPart.Vertex vertex : polygon.vertices) {
                for (int a = 0; a < 3; a++) {
                    box[a] = Math.min(box[a], vertex.pos.get(a));
                    box[3 + a] = Math.max(box[3 + a], vertex.pos.get(a));
                }
            }
        }
        return box;
    }
}
