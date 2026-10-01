package danger.orespawn.client.armour;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.core.Direction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * A humanoid armour model's parts from a piece's geometry, {@code assets/orespawn/armour_geo/<set>_<piece>.json}:
 * Bedrock-style cubes (origin, size, box uv, inflate, mirror, an optional rotation about a pivot) in the space of the
 * wearer's bone they belong to, y up, the bone's pivot at 0.
 * <p>
 * Java's part space has y down: a cube from y0 to y1 goes to -y1 to -y0; x and z stay (the humanoid's left arm is at
 * +5 in both). A rotated cube becomes a child part at its pivot, turned by (-x, y, -z). Box UV is laid out on the
 * cube's size rounded down (a 2.12-wide crest takes a 2-texel strip), or on its uv_size, so each face's polygon is
 * rebuilt from the cube Java builds, on the same vertices; the access transformer opens ModelPart.Cube's polygons,
 * ModelPart.Polygon with its constructor, and ModelPart.Vertex for that.
 * <p>
 * The cubes are also kept in the returned nodes, which {@link ArmourModel} draws itself: renderers that replace a
 * cube's drawing with their own box UV from its sizes (Sodium's) never reach them on that path, so the rebuilt faces
 * are what the armour layer draws, with or without such a renderer. The parts carry the same cubes too, as any model's
 * parts do, for code that reads a model's parts or draws them one by one.
 */
public final class ArmourGeometry {
    /** A part, the cubes drawn on it and its child parts (the rotated cubes). */
    public record Node(ModelPart part, List<ModelPart.Cube> cubes, List<Node> children) {
    }

    /** The model's root (the seven humanoid parts) and each top part's node. */
    public record Built(ModelPart root, Map<ModelPart, Node> nodes) {
    }

    /** The humanoid's parts: the model's name, the geometry's bone, the part's offset. */
    private static final String[][] PARTS = {
            {"head", "head", "0", "0", "0"}, {"hat", "hat", "0", "0", "0"}, {"body", "body", "0", "0", "0"},
            {"right_arm", "rightarm", "-5", "2", "0"}, {"left_arm", "leftarm", "5", "2", "0"},
            {"right_leg", "rightleg", "-1.9", "12", "0"}, {"left_leg", "leftleg", "1.9", "12", "0"}};
    /** The order Java's cube adds its faces in, all six visible. */
    private static final Direction[] FACES = {Direction.DOWN, Direction.UP, Direction.WEST, Direction.NORTH,
            Direction.EAST, Direction.SOUTH};

    private ArmourGeometry() {
    }

    public static Built build(JsonObject geometry) {
        float textureWidth = geometry.get("texture_width").getAsFloat();
        float textureHeight = geometry.get("texture_height").getAsFloat();
        JsonObject bones = geometry.getAsJsonObject("bones");
        for (String bone : bones.keySet()) {
            if (Arrays.stream(PARTS).noneMatch(part -> part[1].equals(bone))) {
                throw new IllegalArgumentException("not a humanoid bone: " + bone);
            }
        }
        Map<String, ModelPart> parts = new HashMap<>();
        Map<ModelPart, Node> nodes = new IdentityHashMap<>();
        for (String[] part : PARTS) {
            List<ModelPart.Cube> cubes = new ArrayList<>();
            List<Node> childNodes = new ArrayList<>();
            Map<String, ModelPart> children = new HashMap<>();
            if (bones.has(part[1])) {
                int index = 0;
                for (JsonElement element : bones.getAsJsonArray(part[1])) {
                    JsonObject cube = element.getAsJsonObject();
                    if (cube.has("rotation")) {
                        float[] pivot = floats(cube, "pivot", 0);
                        float[] rotation = floats(cube, "rotation", 0);
                        List<ModelPart.Cube> own = List.of(cube(cube, pivot, textureWidth, textureHeight));
                        ModelPart child = new ModelPart(own, Map.of());
                        PartPose pose = PartPose.offsetAndRotation(pivot[0], -pivot[1], pivot[2],
                                (float) Math.toRadians(-rotation[0]), (float) Math.toRadians(rotation[1]),
                                (float) Math.toRadians(-rotation[2]));
                        child.setInitialPose(pose);
                        child.loadPose(pose);
                        children.put("cube_" + index, child);
                        childNodes.add(new Node(child, own, List.of()));
                    } else {
                        cubes.add(cube(cube, new float[3], textureWidth, textureHeight));
                    }
                    index++;
                }
            }
            List<ModelPart.Cube> own = List.copyOf(cubes);
            ModelPart modelPart = new ModelPart(own, children);
            PartPose pose = PartPose.offset(Float.parseFloat(part[2]), Float.parseFloat(part[3]), Float.parseFloat(part[4]));
            modelPart.setInitialPose(pose);
            modelPart.loadPose(pose);
            parts.put(part[0], modelPart);
            nodes.put(modelPart, new Node(modelPart, own, List.copyOf(childNodes)));
        }
        return new Built(new ModelPart(List.of(), parts), nodes);
    }

    private static ModelPart.Cube cube(JsonObject cube, float[] pivot, float textureWidth, float textureHeight) {
        float[] origin = floats(cube, "origin", 0);
        float[] size = floats(cube, "size", 0);
        float grow = cube.has("inflate") ? cube.get("inflate").getAsFloat() : 0;
        boolean mirror = cube.has("mirror") && cube.get("mirror").getAsBoolean();
        JsonArray uv = cube.getAsJsonArray("uv");
        int u = uv.get(0).getAsInt(), v = uv.get(1).getAsInt();
        float x = origin[0] - pivot[0];
        float y = -(origin[1] + size[1]) + pivot[1];
        float z = origin[2] - pivot[2];
        ModelPart.Cube built = new ModelPart.Cube(u, v, x, y, z, size[0], size[1], size[2], grow, grow, grow, mirror,
                textureWidth, textureHeight, EnumSet.allOf(Direction.class));
        // box UV on the cube's size rounded down, or on its own strip sizes
        float w = (float) Math.floor(Math.abs(size[0]) + 1e-4), h = (float) Math.floor(Math.abs(size[1]) + 1e-4),
                d = (float) Math.floor(Math.abs(size[2]) + 1e-4);
        if (cube.has("uv_size")) {
            // at least a texel each, so a slab thinner than a pixel still has its colour
            float[] uvSize = floats(cube, "uv_size", 1);
            w = uvSize[0];
            h = uvSize[1];
            d = uvSize[2];
        }
        for (int i = 0; i < FACES.length; i++) {
            ModelPart.Polygon polygon = built.polygons[i];
            ModelPart.Vertex[] vertices = polygon.vertices.clone();
            if (mirror) reverse(vertices);          // back to the order the polygon was made from
            float[] rect = rect(FACES[i], u, v, w, h, d);
            built.polygons[i] = new ModelPart.Polygon(vertices, rect[0], rect[1], rect[2], rect[3], textureWidth,
                    textureHeight, mirror, FACES[i]);
        }
        return built;
    }

    /** The box-UV rectangle of a face (u1, v1, u2, v2), as Java's cube lays its faces out. */
    private static float[] rect(Direction face, float u, float v, float w, float h, float d) {
        return switch (face) {
            case DOWN -> new float[]{u + d, v, u + d + w, v + d};
            case UP -> new float[]{u + d + w, v + d, u + d + w + w, v};
            case WEST -> new float[]{u, v + d, u + d, v + d + h};
            case NORTH -> new float[]{u + d, v + d, u + d + w, v + d + h};
            case EAST -> new float[]{u + d + w, v + d, u + d + w + d, v + d + h};
            case SOUTH -> new float[]{u + d + w + d, v + d, u + d + w + d + w, v + d + h};
        };
    }

    private static void reverse(ModelPart.Vertex[] vertices) {
        for (int i = 0, j = vertices.length - 1; i < j; i++, j--) {
            ModelPart.Vertex t = vertices[i];
            vertices[i] = vertices[j];
            vertices[j] = t;
        }
    }

    private static float[] floats(JsonObject object, String key, float fallback) {
        float[] out = {fallback, fallback, fallback};
        if (object.has(key)) {
            JsonArray array = object.getAsJsonArray(key);
            for (int i = 0; i < 3; i++) out[i] = array.get(i).getAsFloat();
        }
        return out;
    }
}
