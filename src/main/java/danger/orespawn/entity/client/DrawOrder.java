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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
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

    private DrawOrder() {
    }

    /** The order under {@link #KEY} of a parsed geo JSON document; fails loudly when a rig ships without it. */
    public static List<String> read(JsonObject geoJson) {
        JsonArray geometries = geoJson.getAsJsonArray("minecraft:geometry");
        if (geometries == null || geometries.isEmpty()) {
            throw new IllegalStateException("geo JSON carries no minecraft:geometry");
        }
        JsonObject description = geometries.get(0).getAsJsonObject().getAsJsonObject("description");
        JsonElement element = description == null ? null : description.get(KEY);
        if (element == null || !element.isJsonArray()) {
            throw new IllegalStateException("geo JSON carries no " + KEY
                    + " (the G2 draw-order contract); regenerate it with tools/layer_definition_to_geo.py");
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
            throw new IllegalStateException(KEY + " is empty");
        }
        return List.copyOf(order);
    }

    /** {@link #read} over the geo resource GeckoLib loaded the bake from. */
    public static List<String> load(ResourceManager resources, ResourceLocation geo) {
        try (BufferedReader reader = resources.getResourceOrThrow(geo).openAsReader()) {
            return read(JsonParser.parseReader(reader).getAsJsonObject());
        } catch (IOException | RuntimeException exception) {
            throw new IllegalStateException("Unable to read the draw order of " + geo, exception);
        }
    }

    /**
     * Sorts the bake's {@code topLevelBones()} and every {@code getChildBones()} list,
     * in place, into {@code order}: after this, GeckoLib's pre-order traversal draws
     * the bones exactly as the classic renderer draws the parts. Every bone of the rig
     * must appear in the order and every name in the order must be a bone of the rig, and
     * the order must be a pre-order of the rig's tree (each parent ahead of its subtree, each
     * subtree contiguous) — a permutation that is not one would sort into a traversal that
     * differs from the shipped list (refuter A, 2026-09-06); anything else is a rig that
     * drifted from its contract, and fails.
     */
    public static void apply(BakedGeoModel model, List<String> order) {
        Map<String, Integer> rank = new HashMap<>();
        for (int index = 0; index < order.size(); index++) {
            if (rank.put(order.get(index), index) != null) {
                throw new IllegalStateException("draw order repeats bone " + order.get(index));
            }
        }
        Set<String> present = new HashSet<>();
        sort(model.topLevelBones(), rank, present);
        if (present.size() != rank.size()) {
            Set<String> missing = new HashSet<>(rank.keySet());
            missing.removeAll(present);
            throw new IllegalStateException("draw order names bones the rig lacks: " + missing);
        }
        List<String> drawn = traversal(model);
        if (!drawn.equals(order)) {
            throw new IllegalStateException(KEY + " is not a pre-order of the rig: the sorted bake draws " + drawn
                    + " but the order says " + order);
        }
    }

    private static void sort(List<GeoBone> bones, Map<String, Integer> rank, Set<String> present) {
        for (GeoBone bone : bones) {
            if (!rank.containsKey(bone.getName())) {
                throw new IllegalStateException("rig bone " + bone.getName() + " is missing from " + KEY);
            }
            if (!present.add(bone.getName())) {
                throw new IllegalStateException("rig repeats bone " + bone.getName());
            }
            sort(bone.getChildBones(), rank, present);
        }
        bones.sort(Comparator.comparingInt(bone -> rank.get(bone.getName())));
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
