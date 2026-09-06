package danger.orespawn.entity.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.cache.object.GeoCube;
import software.bernie.geckolib.cache.object.GeoQuad;

/**
 * Phase G, ENT-S-146: the order of a CUBE's six faces made equal to the classic renderer's.
 *
 * <p>Why the two orders differ (pinned bytecode). NeoForge 21.1.223 {@code ModelPart.Cube.<init>}
 * fills its polygon array DOWN, UP, WEST, NORTH, EAST, SOUTH (offsets 365-785: DOWN 365, UP 436,
 * WEST 507, NORTH 578, EAST 649, SOUTH 720, each ending in the {@code Polygon.<init>} and
 * {@code aastore} of its slot) and {@code compile} emits them in that order; GeckoLib 4.8.4
 * {@code BakedModelFactory.buildQuads} builds a cube's quads WEST, EAST, NORTH, SOUTH, UP, DOWN
 * (offsets 20-130, one {@code buildQuad} per direction) and {@code GeoRenderer.renderCube} emits
 * {@code cube.quads()} in array order. The order contracted here is the 1.21.1 classic's, which is
 * NOT 1.7.10's: the original {@code ModelBox} (the 1.7.10 client jar, class {@code bis},
 * {@code <init>(ModelRenderer, int, int, float, float, float, int, int, int, float)}) stored its
 * six {@code TexturedQuad}s at offsets 365 / 439 / 500 / 561 / 628 / 695 as the x2, x1, y1, y2, z1, z2
 * faces (+X, -X, -Y, +Y, -Z, +Z; a mirrored box swaps x1 and x2 first, offsets 136-153, exactly as
 * {@code Cube.<init>} does at 121-135, and reverses every quad at 774-806) and drew the array in
 * order ({@code render}, offsets 0-28); the port cannot follow it, since the classic renderer is
 * vanilla's own {@code ModelPart.Cube} - disclosed in the ENT-S-146 record.
 * For an opaque (cutout) rig the order is invisible except where two faces of ONE cube are
 * coplanar (a zero-thickness box) - the open item recorded on the G2 root-order contract. For a
 * rig drawn with a BLENDING render type it decides the picture: with the depth mask on, a back
 * face emitted before its front face shows through the front face's alpha, one emitted after it
 * is rejected by the LEQUAL depth test and shows nothing, so a candidate that emits the faces in
 * GeckoLib's order blends a different image than the classic renderer does. The contract here is
 * the G2 one extended into the cube: the converter ({@code tools/layer_definition_to_geo.py})
 * writes, under {@link #KEY} in the geometry's {@code description}, the face order it observed
 * in the classic captures for every cube of every cube-bearing bone - as GeckoLib direction names,
 * the labels {@code buildQuads} stamps on the quads ({@code GeoQuad.direction()}) - and
 * {@link #apply} permutes each cube's quad array into it. GeckoLib's deserializers ignore the
 * key exactly as they ignore {@code DrawOrder.KEY}.</p>
 *
 * <p>The key is written only for rigs whose manifest entry asks for it ({@code cube_face_order:
 * "classic"}); every other shipped rig keeps GeckoLib's own order, so their proofs and captures
 * are untouched. The seam ({@code OreSpawnGeoReplacementModel.getBakedModel}) applies a present
 * key on every bake and, like the draw order, never crashes the client on a wrong one: a key that
 * is present and malformed, or names bones or cube counts the rig lacks, is logged at ERROR once
 * per resource (the geo, the exact reason, the pack author's fix) and the bake is left exactly as
 * the factory built it - every check runs before anything is touched. An absent key is silent
 * unless the descriptor says the rig needs it ({@code GeoReplacementDescriptor
 * .cubeFaceOrderRequired}: a translucent rig), where a resource pack that re-exported the rig and
 * dropped the key gets one WARN naming the fix. The harness ({@code S4CandidateRuntime},
 * {@code G1AnimationRuntime}) applies a present key through the strict {@link #apply} and takes no
 * fallback.</p>
 *
 * <p>The arrays permuted are the cubes' own: {@code GeoCube.quads()} is a record accessor returning
 * the field, and {@code GeoBone.getCubes()} the bone's list. Permuting an already permuted array
 * changes nothing, so applying the order twice (two model instances over one cached bake, or a
 * bake reached again after a resource reload) is harmless.</p>
 */
public final class FaceOrder {
    /** The description key: bone name to one array per cube (in {@code getCubes()} order) of the six direction names in draw order. */
    public static final String KEY = "orespawn:cube_face_order";
    /** The order GeckoLib 4.8.4's {@code BakedModelFactory.buildQuads} builds a cube's quads in (offsets 20-130). */
    public static final List<Direction> GECKOLIB_ORDER = List.of(
            Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH, Direction.UP, Direction.DOWN);

    /** The mod's logger by name, the literal on purpose (see {@code DrawOrder}): no {@code OreSpawnMod} reference in this class. */
    private static final Logger LOGGER = LoggerFactory.getLogger("orespawn");
    /** Once-per-resource logging, the geo to the reason last logged for it (the {@code DrawOrder} design; JVM lifetime). */
    private static final Map<ResourceLocation, String> LAST_LOGGED = new ConcurrentHashMap<>();
    private static final String ABSENT = "absent";

    /** What the seam decided for one bake. */
    public enum Outcome {
        /** The key was present and right: every cube now emits its faces in the classic order. */
        APPLIED,
        /** No key: GeckoLib's own face order kept; WARN once per resource when the descriptor requires the key, else silent. */
        ABSENT,
        /** The key was present and wrong, or the resource could not be read: GeckoLib's own order kept, the bake untouched; ERROR once per resource. */
        WRONG_KEY_FALLBACK
    }

    /** The seam's decision for one bake, and whether THIS call wrote the once-per-resource log line. */
    public record Decision(Outcome outcome, boolean logged) {
    }

    private FaceOrder() {
    }

    /**
     * The order under {@link #KEY} of a parsed geo JSON document: bone name to one list per cube of
     * the six directions in draw order. EMPTY when the rig ships without the key (no
     * {@code description}, or no such member); throws when the key is present and malformed (not an
     * object, a member that is not an array of arrays, an entry that is not a direction name, a list
     * that is not a permutation of the six directions). The message is the exact reason.
     */
    public static Map<String, List<List<Direction>>> read(JsonObject geoJson) {
        JsonArray geometries = geoJson.getAsJsonArray("minecraft:geometry");
        if (geometries == null || geometries.isEmpty()) {
            throw new IllegalStateException("geo JSON carries no minecraft:geometry");
        }
        JsonObject description = geometries.get(0).getAsJsonObject().getAsJsonObject("description");
        JsonElement element = description == null ? null : description.get(KEY);
        if (element == null) {
            return Map.of();
        }
        if (!element.isJsonObject()) {
            throw new IllegalStateException(KEY + " is present but not an object: " + element);
        }
        Map<String, List<List<Direction>>> order = new LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> bone : element.getAsJsonObject().entrySet()) {
            if (!bone.getValue().isJsonArray()) {
                throw new IllegalStateException(KEY + "." + bone.getKey() + " is not an array: " + bone.getValue());
            }
            List<List<Direction>> cubes = new ArrayList<>();
            for (JsonElement cube : bone.getValue().getAsJsonArray()) {
                if (!cube.isJsonArray()) {
                    throw new IllegalStateException(KEY + "." + bone.getKey() + " holds a cube entry that is not an array: " + cube);
                }
                List<Direction> faces = new ArrayList<>();
                for (JsonElement face : cube.getAsJsonArray()) {
                    Direction direction = face.isJsonPrimitive() && face.getAsJsonPrimitive().isString()
                            ? Direction.byName(face.getAsString()) : null;
                    if (direction == null) {
                        throw new IllegalStateException(KEY + "." + bone.getKey() + " holds an entry that is not a direction name: " + face);
                    }
                    faces.add(direction);
                }
                if (faces.size() != Direction.values().length || faces.stream().distinct().count() != faces.size()) {
                    throw new IllegalStateException(KEY + "." + bone.getKey() + " cube " + cubes.size()
                            + " is not a permutation of the six directions: " + faces);
                }
                cubes.add(List.copyOf(faces));
            }
            if (cubes.isEmpty()) {
                throw new IllegalStateException(KEY + "." + bone.getKey() + " is empty");
            }
            order.put(bone.getKey(), List.copyOf(cubes));
        }
        if (order.isEmpty()) {
            throw new IllegalStateException(KEY + " is present but empty");
        }
        return Map.copyOf(order);
    }

    /** {@link #read} over the geo resource GeckoLib loaded the bake from ({@link DrawOrder#geoJson}, the {@code DrawOrder.load} contract). */
    public static Map<String, List<List<Direction>>> load(ResourceManager resources, ResourceLocation geo) {
        return read(DrawOrder.geoJson(resources, geo));
    }

    /**
     * The production seam's policy over the geo resource GeckoLib baked {@code model} from - the call
     * {@code OreSpawnGeoReplacementModel.getBakedModel} makes, under {@code EntityRenderDispatcher
     * .render}, where a throw is a client crash. Nothing thrown by {@link #load}, {@link #read} or
     * {@link #apply} escapes: an absent key is {@link Outcome#ABSENT} (WARN once per resource only when
     * {@code required}); a key that is present and wrong, or a resource that cannot be read, is
     * {@link Outcome#WRONG_KEY_FALLBACK} (ERROR once per resource), the bake left as the factory built
     * it; a right key is {@link Outcome#APPLIED}.
     */
    public static Decision applyOrFallback(BakedGeoModel model, ResourceManager resources, ResourceLocation geo,
                                           boolean required) {
        Map<String, List<List<Direction>>> order;
        try {
            order = load(resources, geo);
        } catch (IllegalStateException wrong) {
            return fallback(geo, reason(wrong));
        }
        return applyOrFallback(model, order, geo, required);
    }

    /**
     * The same policy over a geo document already parsed ({@link DrawOrder#geoJson}; the seam parses the
     * resource once per bake and hands the document to both keys - ENT-S-146, refuter A, D5): {@link #read}'s
     * failure (a key present and malformed) is the ERROR-logged fallback, an absent key {@link Outcome#ABSENT}.
     */
    public static Decision applyOrFallback(BakedGeoModel model, JsonObject geoJson, ResourceLocation geo,
                                           boolean required) {
        Map<String, List<List<Direction>>> order;
        try {
            order = read(geoJson);
        } catch (IllegalStateException wrong) {
            return fallback(geo, reason(wrong));
        }
        return applyOrFallback(model, order, geo, required);
    }

    /** The seam's answer when the geo resource itself cannot be read: the ERROR-logged fallback, once per resource ({@link DrawOrder#unreadable}). */
    public static Decision unreadable(ResourceLocation geo, IllegalStateException failure) {
        return fallback(geo, reason(failure));
    }

    /** The same policy over an order already read ({@link #read}: empty means absent). */
    public static Decision applyOrFallback(BakedGeoModel model, Map<String, List<List<Direction>>> order,
                                           ResourceLocation geo, boolean required) {
        if (order.isEmpty()) {
            if (!required) {
                return new Decision(Outcome.ABSENT, false);
            }
            boolean logged = !ABSENT.equals(LAST_LOGGED.put(geo, ABSENT));
            if (logged) {
                LOGGER.warn("Phase G face order: {} ships without {} - GeckoLib's own face order is used inside each cube, "
                        + "not the classic one, so this translucent rig blends its faces differently from the classic "
                        + "renderer (a resource pack that re-exports the rig loses the key); fix: carry the {} object "
                        + "over from the shipped rig's description", geo, KEY, KEY);
            }
            return new Decision(Outcome.ABSENT, logged);
        }
        try {
            apply(model, order);
        } catch (IllegalStateException wrong) {
            return fallback(geo, reason(wrong));
        }
        LAST_LOGGED.remove(geo);
        return new Decision(Outcome.APPLIED, false);
    }

    private static Decision fallback(ResourceLocation geo, String reason) {
        boolean logged = !reason.equals(LAST_LOGGED.put(geo, reason));
        if (logged) {
            LOGGER.error("Phase G face order: {} carries a wrong {} - GeckoLib's own face order is used inside each "
                    + "cube, not the classic one: {}; fix: carry the {} object over from the shipped rig's description",
                    geo, KEY, reason, KEY);
        }
        return new Decision(Outcome.WRONG_KEY_FALLBACK, logged);
    }

    private static String reason(IllegalStateException failure) {
        Throwable cause = failure.getCause();
        return cause == null ? failure.getMessage()
                : failure.getMessage() + " (" + cause.getClass().getSimpleName() + ": " + cause.getMessage() + ")";
    }

    /**
     * Permutes, in place, every cube's quad array into {@code order}: after this
     * {@code GeoRenderer.renderCube} emits the faces of every cube exactly as {@code ModelPart.Cube
     * .compile} emits the classic cube's. Strict: the order must name exactly the rig's cube-bearing
     * bones, with one list per cube; every quad's direction must be in its list (a null quad - a face
     * GeckoLib did not build - sorts last); a rig cube with two quads of one direction is refused.
     * Every check runs BEFORE anything is touched, so a wrong order leaves the bake exactly as found -
     * what the seam falls back to. An empty order is refused here: the absent-key policy is
     * {@link #applyOrFallback}'s.
     */
    public static void apply(BakedGeoModel model, Map<String, List<List<Direction>>> order) {
        if (order.isEmpty()) {
            throw new IllegalStateException("face order is empty: a rig without " + KEY
                    + " takes applyOrFallback, and the harness takes no fallback");
        }
        Map<String, GeoBone> cubeBearing = new HashMap<>();
        collectCubeBearing(model.topLevelBones(), cubeBearing);
        for (String bone : order.keySet()) {
            if (!cubeBearing.containsKey(bone)) {
                throw new IllegalStateException(KEY + " names " + bone + ", which is not a cube-bearing bone of the rig");
            }
        }
        for (String bone : cubeBearing.keySet()) {
            if (!order.containsKey(bone)) {
                throw new IllegalStateException("cube-bearing rig bone " + bone + " is missing from " + KEY);
            }
        }
        List<GeoQuad[]> permuted = new ArrayList<>();
        List<GeoQuad[]> targets = new ArrayList<>();
        for (Map.Entry<String, List<List<Direction>>> entry : order.entrySet()) {
            List<GeoCube> cubes = cubeBearing.get(entry.getKey()).getCubes();
            if (cubes.size() != entry.getValue().size()) {
                throw new IllegalStateException(KEY + "." + entry.getKey() + " lists " + entry.getValue().size()
                        + " cubes but the rig bone has " + cubes.size());
            }
            for (int index = 0; index < cubes.size(); index++) {
                GeoQuad[] quads = cubes.get(index).quads();
                targets.add(quads);
                permuted.add(permutation(entry.getKey(), index, quads, entry.getValue().get(index)));
            }
        }
        for (int index = 0; index < targets.size(); index++) {
            System.arraycopy(permuted.get(index), 0, targets.get(index), 0, targets.get(index).length);
        }
    }

    /** The quad array as it will be after the permutation; checks only, touches nothing. */
    private static GeoQuad[] permutation(String bone, int cubeIndex, GeoQuad[] quads, List<Direction> faces) {
        Map<Direction, Integer> rank = new HashMap<>();
        for (int index = 0; index < faces.size(); index++) {
            rank.put(faces.get(index), index);
        }
        boolean[] seen = new boolean[Direction.values().length];
        for (GeoQuad quad : quads) {
            if (quad == null) {
                continue;
            }
            Direction direction = quad.direction();
            if (direction == null || !rank.containsKey(direction)) {
                throw new IllegalStateException(bone + " cube " + cubeIndex + " has a quad of direction " + direction
                        + ", which " + KEY + " does not order");
            }
            if (seen[direction.ordinal()]) {
                throw new IllegalStateException(bone + " cube " + cubeIndex + " has two quads of direction " + direction);
            }
            seen[direction.ordinal()] = true;
        }
        GeoQuad[] sorted = Arrays.copyOf(quads, quads.length);
        Arrays.sort(sorted, Comparator.comparingInt(
                quad -> quad == null ? Integer.MAX_VALUE : rank.get(quad.direction())));
        return sorted;
    }

    private static void collectCubeBearing(List<GeoBone> bones, Map<String, GeoBone> cubeBearing) {
        for (GeoBone bone : bones) {
            if (!bone.getCubes().isEmpty() && cubeBearing.put(bone.getName(), bone) != null) {
                throw new IllegalStateException("rig repeats bone " + bone.getName());
            }
            collectCubeBearing(bone.getChildBones(), cubeBearing);
        }
    }

    /**
     * The face order the bake currently draws: every cube-bearing bone, in traversal order, to one
     * list per cube of its quads' directions in array order (a null quad is skipped). The harness
     * records it beside the key it applied; the game-test row pins {@link #apply} through it.
     */
    public static Map<String, List<List<Direction>>> faceOrders(BakedGeoModel model) {
        Map<String, List<List<Direction>>> orders = new LinkedHashMap<>();
        collectFaceOrders(model.topLevelBones(), orders);
        return orders;
    }

    private static void collectFaceOrders(List<GeoBone> bones, Map<String, List<List<Direction>>> orders) {
        for (GeoBone bone : bones) {
            if (!bone.getCubes().isEmpty()) {
                List<List<Direction>> cubes = new ArrayList<>();
                for (GeoCube cube : bone.getCubes()) {
                    List<Direction> faces = new ArrayList<>();
                    for (GeoQuad quad : cube.quads()) {
                        if (quad != null) {
                            faces.add(quad.direction());
                        }
                    }
                    cubes.add(List.copyOf(faces));
                }
                orders.put(bone.getName(), List.copyOf(cubes));
            }
            collectFaceOrders(bone.getChildBones(), orders);
        }
    }
}
