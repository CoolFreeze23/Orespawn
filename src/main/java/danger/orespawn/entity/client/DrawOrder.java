package danger.orespawn.entity.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;

/**
 * Phase G, the G2 root-order contract: GeckoLib's bone draw order made equal to the
 * classic renderer's part draw order.
 *
 * <p>Why the two orders differ (all from the pinned bytecode). GeckoLib 4.8.4
 * {@code GeometryTree.fromModel} keeps the top-level bones in an
 * {@code Object2ObjectOpenHashMap} (offsets 0-4) and every bone's children in another
 * ({@code BoneStructure.<init>(Bone)} offsets 2-9); {@code BakedModelFactory$Builtin
 * .constructGeoModel} iterates {@code topLevelBones().values()} (9-17) into a fresh
 * {@code ObjectArrayList} (0-4, {@code List.add} at 55) and hands that list to the
 * {@code BakedGeoModel} record (64-73, stored by {@code putfield} at 6);
 * {@code constructBone} iterates {@code BoneStructure.children().values()} (175-183)
 * into {@code GeoBone.getChildBones()} (214-226), the {@code ObjectArrayList} the bone's
 * constructor made (offsets 5-12). {@code GeoRenderer.actuallyRender} draws
 * {@code topLevelBones()} in list order (29-33, {@code renderRecursively} at 83);
 * {@code renderRecursively} draws the bone's cubes (36) and then
 * {@code renderChildBones} (92), which iterates {@code getChildBones()} (8-12,
 * {@code renderRecursively} at 62). So GeckoLib's draw order is a pre-order traversal
 * whose sibling order is fastutil's open-hash order of the bone names; the JSON order
 * never survives the load.</p>
 *
 * <p>The classic side. NeoForge 21.1.223 {@code PartDefinition.<init>} keeps children in
 * {@code Maps.newHashMap()} (offsets 5-8) and {@code bake} collects that map's entry
 * stream into an {@code Object2ObjectArrayMap} (1-44), so {@code ModelPart.render}
 * ({@code visible} at 1-4, {@code pushPose} 32, {@code translateAndRotate} 37,
 * {@code skipDraw} gating {@code compile} at 41-58, {@code children.values()} 62-70
 * drawn through {@code render} at 108, {@code popPose} 115) draws a tree in the
 * HashMap order of the child NAMES - and every landed OreSpawn classic model overrides
 * {@code renderToBuffer} and draws its parts explicitly, so its order is the code's.
 * Either way it is a pre-order traversal with a fixed sibling order, which is exactly
 * what {@link #apply} imposes on the baked GeckoLib lists.</p>
 *
 * <p>The contract travels inside the geo JSON: the converter
 * ({@code tools/layer_definition_to_geo.py}) writes the observed classic order, as
 * geo bone names in pre-order, under {@link #KEY} in the geometry's {@code description}.
 * GeckoLib's own deserializers provably ignore it ({@code MinecraftGeometry
 * .lambda$deserializer$0} reads only {@code bones}, {@code cape} and
 * {@code description}; {@code ModelProperties.lambda$deserializer$0} reads its
 * seventeen named keys and nothing else), so the shipped rig and its order are one
 * resource, named by the descriptor's model resource, and cannot drift apart. A
 * separate {@code .json} sidecar under {@code geo/} was rejected: {@code GeckoLibCache
 * .lambda$loadResources$6} lists every {@code .json} under {@code geo} and
 * {@code lambda$loadModels$5} bakes each one as a model.</p>
 *
 * <p>The missing-key policy (owner ruling 2026-09-06, addendum item 24 (3); the reading of
 * "loud" below was presented to the owner with the landing). A rig that ships WITHOUT the
 * key is not a build error of this mod but the ordinary shape of a third-party resource
 * pack: Blockbench's bedrock exporter rewrites {@code description} and drops every foreign
 * key, so a pack that re-exports one of the shipped rigs loses the order - and the owner's
 * rule is that a resource pack must never crash the client. So {@link #read} answers an
 * ABSENT key with an empty order, and {@link #applyOrFallback} answers an empty order by
 * leaving GeckoLib's own bone order in place (the mob still draws; only its
 * self-overlapping faces may resolve the other way), logged once per resource at WARN. A
 * key that is PRESENT and wrong - a non-array, a non-string or repeated entry, an empty
 * array, a name the rig lacks, a rig bone the key lacks, an order that is not a pre-order
 * of the rig's tree - does not crash the client either, because "never" is absolute: it is
 * loud as an ERROR log, once per resource, naming the geo, the exact reason ({@link #read}'s
 * or {@link #apply}'s own message) and the pack author's fix (carry the
 * {@code orespawn:bone_draw_order} array over from the shipped rig's {@code description}),
 * and then takes the same fallback - {@link #apply} validates everything before it touches
 * the bake, so the order it falls back to is exactly the factory's. The alternative reading
 * (a crash report naming the geo) is one line away and the owner's call. The shipped rigs
 * must never take either fallback: the asset audit ({@code tools/asset_audit.py};
 * {@code GECKO_GEO_DRAW_ORDER_MISSING} for a seam rig whose key is absent or whose content
 * is wrong, {@code GECKO_GEO_SEAM_UNRECONCILED} for a shipped geo the seam does not draw and
 * no dated exception names) fails the build, and the harness ({@code S4CandidateRuntime},
 * {@code G1AnimationRuntime}, the probe, the benchmark) never calls
 * {@code applyOrFallback} - it goes through the strict {@link #read} / {@link #apply}, which
 * keep throwing, so a proof can never come from a fallback bake.</p>
 *
 * <p>The lists sorted here are the factory's own mutable {@code ObjectArrayList}s:
 * {@code BakedGeoModel.topLevelBones()} returns its field (bytecode 0-4, a record
 * accessor) and {@code GeoBone.getChildBones()} returns its field (0-4); nothing copies
 * them defensively. Sorting a sorted list changes nothing, so applying the contract
 * twice (two model instances over one cached bake, or a bake reached again after a
 * resource reload) is harmless.</p>
 */
public final class DrawOrder {
    /** The description key carrying the classic draw order as geo bone names in pre-order. */
    public static final String KEY = "orespawn:bone_draw_order";

    /**
     * The mod's logger by NAME, the literal on purpose: {@code OreSpawnMod} is not referenced
     * here at all (not even its compile-time {@code MOD_ID} constant), so the class file carries
     * no constant-pool entry for the mod class and the headless harness - the probe, the
     * benchmark, the fallback smoke - never loads it (javap-checked at the landing,
     * 2026-09-06). slf4j keys loggers by name, so this is the very logger
     * {@code OreSpawnMod.LOGGER} holds and the lines land under the mod's own prefix.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger("orespawn");

    /**
     * Once-per-resource logging: the geo, to the reason last logged for it. A repeat of the
     * same reason for the same resource is silent; a new reason (a pack that goes absent to
     * wrong, or wrong one way to wrong another way) logs again; an APPLIED bake clears the
     * entry, so a rig fixed and then broken again is reported again. The map lives for the
     * JVM, a design choice: it survives resource reloads on purpose (a reload re-bakes every
     * rig and would otherwise repeat every line) and it is process state only - never game
     * state, never read by anything but the log decision.
     */
    private static final Map<ResourceLocation, String> LAST_LOGGED = new ConcurrentHashMap<>();
    /** The absent key's entry in {@link #LAST_LOGGED}; no failure message equals it. */
    private static final String ABSENT = "absent";

    /** What the seam decided for one bake. */
    public enum Outcome {
        /** The key was present and right: the bake now draws in the classic order. */
        APPLIED,
        /** No key (the resource-pack case): GeckoLib's own order kept; WARN once per resource. */
        ABSENT_FALLBACK,
        /**
         * The key was present and wrong, or the resource could not be read: GeckoLib's own
         * order kept, the bake untouched; ERROR once per resource, naming the reason.
         */
        WRONG_KEY_FALLBACK
    }

    /**
     * The seam's decision for one bake, and whether THIS call wrote the once-per-resource log
     * line ({@code false} for a repeat of the same reason, and always for {@link Outcome#APPLIED}).
     */
    public record Decision(Outcome outcome, boolean logged) {
    }

    private DrawOrder() {
    }

    /**
     * The order under {@link #KEY} of a parsed geo JSON document. EMPTY when the rig ships
     * without the key (no {@code description}, or no such member: the resource-pack case,
     * answered by {@link #applyOrFallback} with GeckoLib's own order); throws when the key is
     * present and malformed (an explicit {@code null}, a non-array, a non-string or
     * repeated entry, an empty array). The message is the exact reason and names no fix:
     * the seam's log line adds the pack author's, the harness fails on it as it is.
     */
    public static List<String> read(JsonObject geoJson) {
        JsonArray geometries = geoJson.getAsJsonArray("minecraft:geometry");
        if (geometries == null || geometries.isEmpty()) {
            throw new IllegalStateException("geo JSON carries no minecraft:geometry");
        }
        JsonObject description = geometries.get(0).getAsJsonObject().getAsJsonObject("description");
        JsonElement element = description == null ? null : description.get(KEY);
        if (element == null) {
            return List.of();
        }
        if (!element.isJsonArray()) {
            throw new IllegalStateException(KEY + " is present but not an array: " + element);
        }
        List<String> order = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (JsonElement name : element.getAsJsonArray()) {
            if (!name.isJsonPrimitive() || !name.getAsJsonPrimitive().isString()) {
                throw new IllegalStateException(KEY + " holds a non-string entry: " + name);
            }
            if (!seen.add(name.getAsString())) {
                throw new IllegalStateException(KEY + " repeats bone " + name.getAsString());
            }
            order.add(name.getAsString());
        }
        if (order.isEmpty()) {
            throw new IllegalStateException(KEY + " is present but empty");
        }
        return List.copyOf(order);
    }

    /**
     * {@link #read} over the geo resource GeckoLib loaded the bake from: empty when the
     * resource carries no key; {@link #read}'s own exception, unwrapped, when the key is
     * present and malformed (so the message stays the exact reason). The resource itself
     * must exist and parse - GeckoLib baked it moments earlier - so a failure to read it is
     * its own {@link IllegalStateException}, carrying the cause.
     */
    public static List<String> load(ResourceManager resources, ResourceLocation geo) {
        return read(geoJson(resources, geo));
    }

    /**
     * The geo resource GeckoLib loaded the bake from, parsed ONCE: the seam
     * ({@code OreSpawnGeoReplacementModel.getBakedModel}) hands the document to this class and to
     * {@link FaceOrder} rather than parsing the resource once per key (ENT-S-146, refuter A, D5: one
     * parse per bake and reload, for every seam rig). The resource must exist and parse - GeckoLib
     * baked it moments earlier - so a failure is its own {@link IllegalStateException}, carrying the
     * cause; {@link #unreadable} is the seam's answer to it.
     */
    public static JsonObject geoJson(ResourceManager resources, ResourceLocation geo) {
        try (BufferedReader reader = resources.getResourceOrThrow(geo).openAsReader()) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        } catch (IOException | RuntimeException exception) {
            throw new IllegalStateException("Unable to read " + geo, exception);
        }
    }

    /**
     * The production seam's policy over the geo resource GeckoLib baked {@code model} from -
     * the call {@code OreSpawnGeoReplacementModel.getBakedModel} makes, under
     * {@code EntityRenderDispatcher.render}, where a throw is a client crash on the mob's
     * first frame. Nothing thrown by {@link #load}, {@link #read} or {@link #apply} escapes:
     * an absent key is {@link Outcome#ABSENT_FALLBACK} (WARN once per resource); a key that
     * is present and wrong, or a resource that cannot be read, is
     * {@link Outcome#WRONG_KEY_FALLBACK} (ERROR once per resource, naming the geo, the exact
     * reason and the pack author's fix), the bake left exactly as the factory built it; a
     * right key is {@link Outcome#APPLIED}. The harness never calls this: it proves shipped
     * rigs through the strict statics and takes no fallback.
     */
    public static Decision applyOrFallback(BakedGeoModel model, ResourceManager resources, ResourceLocation geo) {
        JsonObject json;
        try {
            json = geoJson(resources, geo);
        } catch (IllegalStateException wrong) {
            return fallback(geo, reason(wrong));
        }
        return applyOrFallback(model, json, geo);
    }

    /**
     * The same policy over a geo document already parsed ({@link #geoJson}; the seam parses once and
     * hands the document to this class and to {@link FaceOrder}): {@link #read}'s failure - a key that
     * is present and malformed - is the ERROR-logged fallback, an absent key the WARN-logged one.
     */
    public static Decision applyOrFallback(BakedGeoModel model, JsonObject geoJson, ResourceLocation geo) {
        List<String> order;
        try {
            order = read(geoJson);
        } catch (IllegalStateException wrong) {
            return fallback(geo, reason(wrong));
        }
        return applyOrFallback(model, order, geo);
    }

    /**
     * The seam's answer when the geo resource itself cannot be read ({@link #geoJson} threw): the
     * ERROR-logged fallback, once per resource, exactly as the resource-based {@link #applyOrFallback}
     * answers it - the bake left as the factory built it.
     */
    public static Decision unreadable(ResourceLocation geo, IllegalStateException failure) {
        return fallback(geo, reason(failure));
    }

    /**
     * The same policy over an order already read ({@link #read}: empty means absent). A
     * present order goes through {@link #apply}; when that throws (a name the rig lacks, a
     * rig bone the key lacks, not a pre-order) the bake is untouched - {@link #apply} checks
     * everything before it sorts - and the decision is the ERROR-logged fallback.
     */
    public static Decision applyOrFallback(BakedGeoModel model, List<String> order, ResourceLocation geo) {
        if (order.isEmpty()) {
            boolean logged = !ABSENT.equals(LAST_LOGGED.put(geo, ABSENT));
            if (logged) {
                LOGGER.warn("Phase G draw order: {} ships without {} - GeckoLib's own bone order is used, not the "
                        + "classic one (a resource pack that re-exports the rig loses the key); fix: carry the {} "
                        + "array over from the shipped rig's description", geo, KEY, KEY);
            }
            return new Decision(Outcome.ABSENT_FALLBACK, logged);
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
            LOGGER.error("Phase G draw order: {} carries a wrong {} - GeckoLib's own bone order is used, not the "
                    + "classic one: {}; fix: carry the {} array over from the shipped rig's description",
                    geo, KEY, reason, KEY);
        }
        return new Decision(Outcome.WRONG_KEY_FALLBACK, logged);
    }

    /** The exact reason: the message, plus the cause when {@link #load} wrapped an unreadable resource. */
    private static String reason(IllegalStateException failure) {
        Throwable cause = failure.getCause();
        return cause == null ? failure.getMessage()
                : failure.getMessage() + " (" + cause.getClass().getSimpleName() + ": " + cause.getMessage() + ")";
    }

    /**
     * Sorts the bake's {@code topLevelBones()} and every {@code getChildBones()} list,
     * in place, into {@code order}: after this, GeckoLib's pre-order traversal draws
     * the bones exactly as the classic renderer draws the parts. Every bone of the rig
     * must appear in the order and every name in the order must be a bone of the rig, and
     * the order must be a pre-order of the rig's tree (each parent ahead of its subtree, each
     * subtree contiguous) - a permutation that is not one would sort into a traversal that
     * differs from the shipped list (refuter A, 2026-09-06); anything else is a rig that
     * drifted from its contract, and throws. Every check runs BEFORE anything is touched
     * (refuter A on the landing, 2026-09-06): the set both ways on the tree as it stands,
     * then the pre-order against a sorted COPY of the lists, and only then the in-place
     * sort - so a wrong order leaves the bake exactly as found, which is what the seam
     * falls back to. An empty order is refused here too: the missing-key fallback is
     * {@link #applyOrFallback}'s, never this method's.
     */
    public static void apply(BakedGeoModel model, List<String> order) {
        if (order.isEmpty()) {
            throw new IllegalStateException("draw order is empty: a rig without " + KEY
                    + " takes applyOrFallback, and the harness takes no fallback");
        }
        Map<String, Integer> rank = new HashMap<>();
        for (int index = 0; index < order.size(); index++) {
            if (rank.put(order.get(index), index) != null) {
                throw new IllegalStateException("draw order repeats bone " + order.get(index));
            }
        }
        Set<String> present = new HashSet<>();
        check(model.topLevelBones(), rank, present);
        if (present.size() != rank.size()) {
            Set<String> missing = new HashSet<>(rank.keySet());
            missing.removeAll(present);
            throw new IllegalStateException("draw order names bones the rig lacks: " + missing);
        }
        List<String> drawn = new ArrayList<>();
        collectSorted(model.topLevelBones(), rank, drawn);
        if (!drawn.equals(order)) {
            throw new IllegalStateException(KEY + " is not a pre-order of the rig: the sorted bake draws " + drawn
                    + " but the order says " + order);
        }
        sort(model.topLevelBones(), rank);
    }

    /** Every rig bone is in the order, once; touches nothing. */
    private static void check(List<GeoBone> bones, Map<String, Integer> rank, Set<String> present) {
        for (GeoBone bone : bones) {
            if (!rank.containsKey(bone.getName())) {
                throw new IllegalStateException("rig bone " + bone.getName() + " is missing from " + KEY);
            }
            if (!present.add(bone.getName())) {
                throw new IllegalStateException("rig repeats bone " + bone.getName());
            }
            check(bone.getChildBones(), rank, present);
        }
    }

    private static Comparator<GeoBone> byRank(Map<String, Integer> rank) {
        return Comparator.comparingInt(bone -> rank.get(bone.getName()));
    }

    /** The traversal the bake WOULD have after the sort, computed on copies of the lists. */
    private static void collectSorted(List<GeoBone> bones, Map<String, Integer> rank, List<String> drawn) {
        List<GeoBone> sorted = new ArrayList<>(bones);
        sorted.sort(byRank(rank));
        for (GeoBone bone : sorted) {
            drawn.add(bone.getName());
            collectSorted(bone.getChildBones(), rank, drawn);
        }
    }

    /** The in-place sort, run only after every check passed. */
    private static void sort(List<GeoBone> bones, Map<String, Integer> rank) {
        bones.sort(byRank(rank));
        for (GeoBone bone : bones) {
            sort(bone.getChildBones(), rank);
        }
    }

    /** The bake's current pre-order traversal, bone names in the order the renderer visits them. */
    public static List<String> traversal(BakedGeoModel model) {
        List<String> names = new ArrayList<>();
        collect(model.topLevelBones(), names);
        return names;
    }

    private static void collect(List<GeoBone> bones, List<String> names) {
        for (GeoBone bone : bones) {
            names.add(bone.getName());
            collect(bone.getChildBones(), names);
        }
    }
}
