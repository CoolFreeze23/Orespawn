package danger.orespawn.gametest;

import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.client.DrawOrder;
import danger.orespawn.entity.client.DrawOrder.Decision;
import danger.orespawn.entity.client.DrawOrder.Outcome;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.loading.json.raw.Model;
import software.bernie.geckolib.loading.json.typeadapter.KeyFramesAdapter;
import software.bernie.geckolib.loading.object.BakedModelFactory;
import software.bernie.geckolib.loading.object.GeometryTree;

/**
 * Pins the G2 missing-key policy (owner ruling 2026-09-06, addendum item 24 (3), and the reading
 * of "loud" presented with the landing) on the game-test server, through the production seam's own
 * entry point: {@link DrawOrder#applyOrFallback(BakedGeoModel, ResourceManager, ResourceLocation)},
 * the call {@code OreSpawnGeoReplacementModel.getBakedModel} makes, over an in-memory
 * {@link ResourceManager} that serves the synthetic rig under whatever {@code description} the
 * step wants. Every decision is pinned through the production return value
 * ({@link Decision}: the {@link Outcome} and whether the call logged) - no test seam:
 * <ul>
 * <li>an ABSENT key reads as an empty order and is {@link Outcome#ABSENT_FALLBACK}: GeckoLib's own
 *     order left exactly as the factory built it, logged on the first call for a resource and NOT
 *     on the second, and again for a second resource;</li>
 * <li>a correct key is {@link Outcome#APPLIED}: the bake sorted into it (a different pre-order, so
 *     the sort is visible), idempotent, nothing logged;</li>
 * <li>every PRESENT-AND-WRONG shape - an explicit null, a string, an object, an empty array, a
 *     non-string entry, a JSON null entry, a repeat, an unknown name, a missing rig bone, a child
 *     ahead of its parent, an interleaved subtree - is {@link Outcome#WRONG_KEY_FALLBACK}: never a
 *     throw, the bake's traversal IDENTICAL to the pre-call traversal (staged into a pre-order the
 *     old partial sort would have changed, refuter A's A-D8), logged once per resource;</li>
 * <li>the once-per-resource log is keyed to the LAST reason: absent then wrong logs twice, the same
 *     reason twice logs once, another reason logs again, and a rig fixed (APPLIED) then broken again
 *     logs again;</li>
 * <li>the strict statics the harness uses ({@link DrawOrder#read}, {@link DrawOrder#apply}) still
 *     throw on the same inputs, and the refused {@code apply} touches nothing either.</li>
 * </ul>
 * The bake is a synthetic four-bone rig baked through GeckoLib's own loader and factory
 * ({@code KeyFramesAdapter.GEO_GSON}, {@code GeometryTree.fromModel}, {@code BakedModelFactory
 * .DEFAULT_FACTORY}: none of them {@code @OnlyIn(CLIENT)}, javap-checked), so the "GeckoLib's own
 * order" the fallback keeps is the real fastutil hash order, whatever it is - the row asserts only
 * that it is a pre-order of the rig and that the fallback leaves it exactly as found. The resource
 * manager classes are common ({@code net.minecraft.server.packs.*}); the override itself
 * ({@code Minecraft.getInstance()}) is client-only and is proven by reading and by the owner's
 * Section E look.
 *
 * <p>One synchronous row, no entity spawned, no level state touched; its only global touch is the
 * synthetic resources it adds to {@code DrawOrder}'s once-per-resource log map (static, JVM-wide),
 * which is why their names carry a per-run stamp (a re-run in the same JVM must see fresh
 * resources) and why the class sits in its own batch {@code drawOrderFallback} (TEST-003).</p>
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class DrawOrderFallbackTests {
    private static final String BATCH = "drawOrderFallback";
    /** Two top-level bones (body, base), body with two children (head, tail): four pre-orders exist. */
    private static final String RIG = """
            {"format_version": "1.12.0", "minecraft:geometry": [{"description": {
            "identifier": "geometry.orespawn.gametest.draw_order_fallback", "texture_width": 64, "texture_height": 64},
            "bones": [
            {"name": "body", "pivot": [0, 0, 0], "cubes": [{"origin": [-2, 0, -2], "size": [4, 4, 4], "uv": [0, 0]}]},
            {"name": "head", "parent": "body", "pivot": [0, 4, 0], "cubes": [{"origin": [-1, 4, -1], "size": [2, 2, 2], "uv": [0, 8]}]},
            {"name": "tail", "parent": "body", "pivot": [0, 2, 2], "cubes": [{"origin": [-1, 1, 2], "size": [2, 2, 3], "uv": [16, 0]}]},
            {"name": "base", "pivot": [0, 0, 0], "cubes": [{"origin": [-3, -1, -3], "size": [6, 1, 6], "uv": [0, 16]}]}
            ]}]}
            """;
    private static final List<List<String>> PRE_ORDERS = List.of(
            List.of("body", "head", "tail", "base"), List.of("body", "tail", "head", "base"),
            List.of("base", "body", "head", "tail"), List.of("base", "body", "tail", "head"));
    /** The pre-order the wrong shapes are built from, and the one they would sort into under a partial sort. */
    private static final List<String> FIXED = List.of("body", "head", "tail", "base");
    /** The pre-order the bake is staged into before the wrong shapes: differs from FIXED, so a partial sort would show. */
    private static final List<String> STAGED = List.of("base", "body", "tail", "head");

    @GameTest(template = "empty", batch = BATCH)
    public static void g2_001_missing_key_falls_back_to_geckolib_order(GameTestHelper helper) {
        String run = Long.toHexString(System.nanoTime());
        Map<ResourceLocation, String> pack = new HashMap<>();
        ResourceManager resources = inMemory(pack);
        ResourceLocation one = synthetic(run, "one");
        ResourceLocation two = synthetic(run, "two");

        // 1. Absent: read is empty; the seam decides ABSENT_FALLBACK, GeckoLib's own order kept, logged once per resource.
        helper.assertTrue(DrawOrder.read(rigJson()).isEmpty(), "a rig without the key must read as an empty order");
        BakedGeoModel baked = bake();
        List<String> geckolib = DrawOrder.traversal(baked);
        helper.assertTrue(PRE_ORDERS.contains(geckolib), "GeckoLib's own traversal is a pre-order of the rig: " + geckolib);
        pack.put(one, RIG);
        expect(helper, "absent, first call", DrawOrder.applyOrFallback(baked, resources, one), Outcome.ABSENT_FALLBACK, true);
        unchanged(helper, "the absent-key fallback", baked, geckolib);
        expect(helper, "absent, second call, same resource", DrawOrder.applyOrFallback(baked, resources, one), Outcome.ABSENT_FALLBACK, false);
        unchanged(helper, "the absent-key fallback, twice", baked, geckolib);
        pack.put(two, RIG);
        expect(helper, "absent, another resource", DrawOrder.applyOrFallback(baked, resources, two), Outcome.ABSENT_FALLBACK, true);
        expect(helper, "absent through the read order, same resource", DrawOrder.applyOrFallback(baked, List.of(), two),
                Outcome.ABSENT_FALLBACK, false);
        unchanged(helper, "the absent-key fallback on a second resource", baked, geckolib);

        // 2. A correct key: APPLIED, the bake sorted into it (a pre-order that differs from GeckoLib's own), idempotent, silent.
        List<String> contracted = PRE_ORDERS.stream().filter(order -> !order.equals(geckolib)).findFirst().orElseThrow();
        helper.assertTrue(DrawOrder.read(rigJson(contracted)).equals(contracted), "a present key reads back as written");
        pack.put(one, rigJson(contracted).toString());
        expect(helper, "a correct key", DrawOrder.applyOrFallback(baked, resources, one), Outcome.APPLIED, false);
        unchanged(helper, "the applied contract", baked, contracted);
        expect(helper, "a correct key, twice", DrawOrder.applyOrFallback(baked, resources, one), Outcome.APPLIED, false);
        unchanged(helper, "the contract applied twice", baked, contracted);

        // 3. Present and wrong, every shape: WRONG_KEY_FALLBACK, never a throw, the traversal identical to the pre-call one,
        //    logged once per resource. The bake is staged into STAGED first: the partial sort the old apply ran before it
        //    validated would have left the two pre-order failures and the unknown name at FIXED (refuter A's A-D8).
        pack.put(one, rigJson(STAGED).toString());
        expect(helper, "staging the bake", DrawOrder.applyOrFallback(baked, resources, one), Outcome.APPLIED, false);
        unchanged(helper, "the staged order", baked, STAGED);
        Map<String, JsonObject> wrong = new LinkedHashMap<>();
        JsonObject explicitNull = rigJson();
        description(explicitNull).add(DrawOrder.KEY, JsonNull.INSTANCE);
        wrong.put("an explicit null", explicitNull);
        JsonObject string = rigJson();
        description(string).addProperty(DrawOrder.KEY, "body");
        wrong.put("a string", string);
        JsonObject object = rigJson();
        description(object).add(DrawOrder.KEY, new JsonObject());
        wrong.put("an object", object);
        wrong.put("an empty array", rigJson(List.of()));
        JsonObject nonString = rigJson(FIXED);
        description(nonString).getAsJsonArray(DrawOrder.KEY).add(7);
        wrong.put("a non-string entry", nonString);
        JsonObject nullEntry = rigJson(FIXED);
        description(nullEntry).getAsJsonArray(DrawOrder.KEY).add(JsonNull.INSTANCE);
        wrong.put("a JSON null entry", nullEntry);
        List<String> repeated = new ArrayList<>(FIXED);
        repeated.add(FIXED.get(0));
        wrong.put("a repeat", rigJson(repeated));
        List<String> unknown = new ArrayList<>(FIXED);
        unknown.add("not_a_bone");
        wrong.put("an unknown name", rigJson(unknown));
        wrong.put("a missing rig bone", rigJson(FIXED.subList(1, FIXED.size())));
        wrong.put("a child ahead of its parent", rigJson(List.of("head", "body", "tail", "base")));
        wrong.put("an interleaved subtree", rigJson(List.of("body", "head", "base", "tail")));
        int index = 0;
        for (Map.Entry<String, JsonObject> shape : wrong.entrySet()) {
            ResourceLocation geo = synthetic(run, "wrong_" + index++);
            pack.put(geo, shape.getValue().toString());
            expect(helper, shape.getKey() + ", first call", DrawOrder.applyOrFallback(baked, resources, geo),
                    Outcome.WRONG_KEY_FALLBACK, true);
            unchanged(helper, shape.getKey(), baked, STAGED);
            expect(helper, shape.getKey() + ", second call, same resource", DrawOrder.applyOrFallback(baked, resources, geo),
                    Outcome.WRONG_KEY_FALLBACK, false);
            unchanged(helper, shape.getKey() + ", twice", baked, STAGED);
        }
        // The read-order overload decides the same way for the shapes that reach apply.
        expect(helper, "an unknown name through the read order", DrawOrder.applyOrFallback(baked, unknown, two),
                Outcome.WRONG_KEY_FALLBACK, true);
        unchanged(helper, "an unknown name through the read order", baked, STAGED);

        // 4. One resource, the LAST reason: absent -> wrong logs twice; the same wrong is silent; another wrong logs
        //    again; wrong -> absent logs again; fixed (APPLIED) then broken the same way logs again.
        ResourceLocation churn = synthetic(run, "churn");
        pack.put(churn, RIG);
        expect(helper, "churn: absent", DrawOrder.applyOrFallback(baked, resources, churn), Outcome.ABSENT_FALLBACK, true);
        pack.put(churn, explicitNull.toString());
        expect(helper, "churn: absent -> wrong", DrawOrder.applyOrFallback(baked, resources, churn), Outcome.WRONG_KEY_FALLBACK, true);
        expect(helper, "churn: the same wrong again", DrawOrder.applyOrFallback(baked, resources, churn), Outcome.WRONG_KEY_FALLBACK, false);
        pack.put(churn, string.toString());
        expect(helper, "churn: another wrong", DrawOrder.applyOrFallback(baked, resources, churn), Outcome.WRONG_KEY_FALLBACK, true);
        pack.put(churn, RIG);
        expect(helper, "churn: wrong -> absent", DrawOrder.applyOrFallback(baked, resources, churn), Outcome.ABSENT_FALLBACK, true);
        pack.put(churn, rigJson(STAGED).toString());
        expect(helper, "churn: fixed", DrawOrder.applyOrFallback(baked, resources, churn), Outcome.APPLIED, false);
        pack.put(churn, string.toString());
        expect(helper, "churn: broken again after a fix", DrawOrder.applyOrFallback(baked, resources, churn),
                Outcome.WRONG_KEY_FALLBACK, true);
        unchanged(helper, "the churn", baked, STAGED);

        // 5. A resource the manager cannot serve is the ERROR-logged fallback too, never a throw.
        expect(helper, "a resource the manager lacks", DrawOrder.applyOrFallback(baked, resources, synthetic(run, "nope")),
                Outcome.WRONG_KEY_FALLBACK, true);
        unchanged(helper, "the unreadable-resource fallback", baked, STAGED);

        // 6. The strict statics (the harness path) still throw on the same inputs, and the refused apply touches nothing.
        expectFailure(helper, "read: an explicit null key", () -> DrawOrder.read(explicitNull));
        expectFailure(helper, "read: an empty array", () -> DrawOrder.read(rigJson(List.of())));
        BakedGeoModel fresh = bake();
        List<String> freshBefore = DrawOrder.traversal(fresh);
        expectFailure(helper, "apply: an unknown name", () -> DrawOrder.apply(fresh, unknown));
        expectFailure(helper, "apply: a child ahead of its parent", () -> DrawOrder.apply(fresh, List.of("head", "body", "tail", "base")));
        expectFailure(helper, "apply: an empty order", () -> DrawOrder.apply(fresh, List.of()));
        unchanged(helper, "the refused strict apply", fresh, freshBefore);
        helper.succeed();
    }

    private static void expect(GameTestHelper helper, String what, Decision decision, Outcome outcome, boolean logged) {
        helper.assertTrue(decision.outcome() == outcome && decision.logged() == logged,
                what + ": expected " + outcome + (logged ? ", logged" : ", not logged") + " but the seam decided " + decision);
    }

    private static void unchanged(GameTestHelper helper, String what, BakedGeoModel baked, List<String> expected) {
        List<String> traversal = DrawOrder.traversal(baked);
        helper.assertTrue(traversal.equals(expected), what + ": the bake must draw " + expected + " but draws " + traversal);
    }

    private static ResourceLocation synthetic(String run, String label) {
        return ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID,
                "geo/entity/gametest_draw_order_fallback_" + run + "_" + label + ".geo.json");
    }

    /** A resource manager over {@code pack}: the geo text under its location, nothing else. */
    private static ResourceManager inMemory(Map<ResourceLocation, String> pack) {
        return new ResourceManager() {
            @Override
            public Set<String> getNamespaces() {
                return Set.of(OreSpawnMod.MOD_ID);
            }

            @Override
            public Optional<Resource> getResource(ResourceLocation location) {
                String text = pack.get(location);
                if (text == null) {
                    return Optional.empty();
                }
                IoSupplier<InputStream> supplier = () -> new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
                return Optional.of(new Resource((PackResources) null, supplier));
            }

            @Override
            public List<Resource> getResourceStack(ResourceLocation location) {
                return getResource(location).map(List::of).orElse(List.of());
            }

            @Override
            public Map<ResourceLocation, Resource> listResources(String path, Predicate<ResourceLocation> filter) {
                return Map.of();
            }

            @Override
            public Map<ResourceLocation, List<Resource>> listResourceStacks(String path, Predicate<ResourceLocation> filter) {
                return Map.of();
            }

            @Override
            public Stream<PackResources> listPacks() {
                return Stream.empty();
            }
        };
    }

    private static BakedGeoModel bake() {
        Model raw = KeyFramesAdapter.GEO_GSON.fromJson(RIG, Model.class);
        return BakedModelFactory.DEFAULT_FACTORY.constructGeoModel(GeometryTree.fromModel(raw));
    }

    private static JsonObject rigJson() {
        return JsonParser.parseString(RIG).getAsJsonObject();
    }

    private static JsonObject rigJson(List<String> order) {
        JsonObject json = rigJson();
        JsonArray array = new JsonArray();
        order.forEach(array::add);
        description(json).add(DrawOrder.KEY, array);
        return json;
    }

    private static JsonObject description(JsonObject json) {
        return json.getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject().getAsJsonObject("description");
    }

    private static void expectFailure(GameTestHelper helper, String what, Runnable action) {
        try {
            action.run();
        } catch (IllegalStateException expected) {
            return;
        }
        helper.fail(what + " must throw (the harness path takes no fallback)");
    }
}
