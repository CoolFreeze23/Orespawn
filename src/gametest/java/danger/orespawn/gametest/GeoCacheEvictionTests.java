package danger.orespawn.gametest;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.client.GeoReplacementCacheEvictor;
import danger.orespawn.entity.client.BeaverGeoReplacement;
import danger.orespawn.entity.client.CoinGeoReplacement;
import danger.orespawn.entity.client.GeoReplacementCaches;
import danger.orespawn.entity.client.OreSpawnAnimatableInstanceCache;
import danger.orespawn.entity.client.OreSpawnGeoReplacement;
import de.dertoaster.multihitboxlib.util.MHLibCounters;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.Rotation;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;

/**
 * OPT-029 -- per-entity GeckoLib animation-cache eviction for the replaced renderers (Phase G slice (a), the owner's
 * sequencing ruling of 2026-09-05, scope addendum item 23 (8)(a); raised at Slice 2, FIX_LOG.md:3761-3764). GeckoLib
 * 4.8.4's {@code SingletonAnimatableInstanceCache.getManagerForId(long)} creates one {@code AnimatableManager} per id
 * and nothing in GeckoLib ever removes one; a replaced renderer keys them by entity id
 * ({@code GeoReplacedEntityRenderer.getInstanceId} = {@code currentEntity.getId()}), so every entity ever drawn kept its
 * manager for the client session. The fix: {@link OreSpawnAnimatableInstanceCache} (the same class GeckoLib picks, with
 * {@code evict} / {@code evictAll} / {@code size}), registered per entity type in {@link GeoReplacementCaches} by the
 * renderer that owns each {@link OreSpawnGeoReplacement} (its constructor, on a bootstrapped client -- never the
 * replacement's own constructor, which the headless s4 probe runs un-bootstrapped), and a client evictor
 * ({@link GeoReplacementCacheEvictor}) that drops a
 * manager on {@code EntityLeaveLevelEvent} and everything on {@code LevelEvent.Unload} -- for client levels only.
 *
 * <p>What the game-test server can pin: the cache, the registry, the gauge the MHLib counters dump reads, and the
 * evictor's static core with its server-side guard. The two client listeners themselves are not exercised here (no
 * {@code ClientLevel} exists on this server); their hook sites are cited from the NeoForge 21.1.223 bytecode on
 * {@link GeoReplacementCaches}. The rows construct real replacements ({@code new BeaverGeoReplacement()},
 * {@code new CoinGeoReplacement()}) and register each one's cache the way the renderer constructor does (no renderer
 * can be built on this server: the renderer classes are client-only -- and the rows load only because the
 * replacements' {@code SHADOW} constants fold at compile time; a non-constant {@code SHADOW} would turn into a
 * {@code getstatic} on a client renderer class and stop this server before any test runs), and the finally block restores
 * the entry that was there before (TEST-003). Frozen mobs spawn with their feet ON the floor (rel y 0, harness note
 * F0.7). A {@link GameTestGenerator} over {@link #rows()}, one synchronous {@link TestFunction} per row,
 * {@code geocacheevictiontests.opt029_NN_<row>}, own batch {@code geoCacheEviction} (TEST-003).</p>
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class GeoCacheEvictionTests {

    private static final String BATCH = "geoCacheEviction";
    private static final String TEST_PREFIX = "geocacheevictiontests.";
    /** Generated TestFunctions bypass the holder's template prefixing, so the template is named in full (IMobConventionTests). */
    private static final String EMPTY_LARGE = OreSpawnMod.MOD_ID + ":empty_large";
    private static final int TIMEOUT_TICKS = 100;
    private static final String FINDING = "OPT-029";

    /** Row 1's synthetic ids 1..100; the one evicted by id. */
    private static final int ID_COUNT = 100;
    private static final long EVICTED_ID = 37L;
    /** Row 2's twenty frozen Beavers: a line along x, two blocks apart, on the template floor (rel y 0), z 24. */
    private static final int BEAVER_COUNT = 20;
    private static final int BEAVER_X0 = 4;
    private static final int BEAVER_Z = 24;
    /** Row 3's pig and row 5's Beaver, feet on the floor. */
    private static final BlockPos PIG_POS = new BlockPos(24, 0, 12);
    private static final BlockPos BEAVER_POS = new BlockPos(20, 0, 24);
    /** Row 4: managers per cache. */
    private static final int PER_CACHE = 5;

    /** The nine MHLib counters in declaration order -- the contract the OPT-029 additions leave unchanged. */
    private static final List<String> MHLIB_COUNTER_NAMES = List.of(
            "client.frames", "client.collecting_passes", "client.recursive_start", "client.recursive_end",
            "client.bones_visited", "client.world_pos_reads", "client.folds", "client.bone_infos_built",
            "client.apply_information");

    // ------------------------------------------------------------------
    // The row table
    // ------------------------------------------------------------------

    private record Row(int index, String tag, Consumer<GameTestHelper> body) {
        String testName() {
            return TEST_PREFIX + String.format("opt029_%02d_%s", this.index, this.tag);
        }
    }

    private static List<Row> rows() {
        return List.of(
                new Row(1, "cache_one_manager_per_id_evict_by_id", GeoCacheEvictionTests::cacheOneManagerPerIdEvictById),
                new Row(2, "registry_evicts_by_type_and_id_gauge_follows", GeoCacheEvictionTests::registryEvictsByTypeAndIdGaugeFollows),
                new Row(3, "unregistered_type_is_a_no_op", GeoCacheEvictionTests::unregisteredTypeIsANoOp),
                new Row(4, "evict_all_clears_every_registered_cache", GeoCacheEvictionTests::evictAllClearsEveryRegisteredCache),
                new Row(5, "server_side_leave_is_ignored_by_the_guard", GeoCacheEvictionTests::serverSideLeaveIsIgnoredByTheGuard));
    }

    /** One test per row: five TestFunctions in the {@code geoCacheEviction} batch. */
    @GameTestGenerator
    public Collection<TestFunction> geoCacheEvictionRows() {
        List<TestFunction> functions = new ArrayList<>();
        for (Row row : rows()) {
            functions.add(new TestFunction(BATCH, row.testName(), EMPTY_LARGE, Rotation.NONE, TIMEOUT_TICKS, 0L, true,
                    row.body()));
        }
        return functions;
    }

    // ------------------------------------------------------------------
    // Row 1: the cache alone -- one manager per id, eviction by id, re-creation on the next draw
    // ------------------------------------------------------------------

    private static void cacheOneManagerPerIdEvictById(GameTestHelper helper) {
        EntityType<?> type = ModEntities.BEAVER.get();
        OreSpawnAnimatableInstanceCache previous = GeoReplacementCaches.cacheFor(type);
        try {
            OreSpawnAnimatableInstanceCache cache = registeredCacheOf(helper, new BeaverGeoReplacement());
            helper.assertTrue(cache.size() == 0, FINDING + ": a fresh replacement cache holds no manager -- expected size 0, actual "
                    + cache.size());
            List<AnimatableManager<?>> first = new ArrayList<>();
            for (long id = 1; id <= ID_COUNT; id++) {
                AnimatableManager<?> manager = cache.getManagerForId(id);
                AnimatableManager<?> again = cache.getManagerForId(id);
                helper.assertTrue(manager != null && manager == again, FINDING + ": getManagerForId(" + id
                        + ") creates one manager and returns the same instance on the second call -- expected identical, actual "
                        + manager + " / " + again);
                first.add(manager);
            }
            helper.assertTrue(cache.size() == ID_COUNT, FINDING + ": one manager per id -- expected size " + ID_COUNT + " after "
                    + ID_COUNT + " ids, actual " + cache.size());
            helper.assertTrue(cache.evict(EVICTED_ID), FINDING + ": evict(" + EVICTED_ID + ") on a held id -- expected true, actual false");
            helper.assertTrue(cache.size() == ID_COUNT - 1, FINDING + ": after the eviction -- expected size " + (ID_COUNT - 1)
                    + ", actual " + cache.size());
            helper.assertTrue(!cache.evict(EVICTED_ID), FINDING + ": evict(" + EVICTED_ID + ") again -- expected false (nothing held), actual true");
            helper.assertTrue(cache.size() == ID_COUNT - 1, FINDING + ": a false eviction touches nothing -- expected size "
                    + (ID_COUNT - 1) + ", actual " + cache.size());
            // The safety argument: the next draw of an evicted id re-creates a fresh manager (GeckoLib's getManagerForId).
            AnimatableManager<?> old = first.get((int) EVICTED_ID - 1);
            AnimatableManager<?> recreated = cache.getManagerForId(EVICTED_ID);
            helper.assertTrue(recreated != null && recreated != old && cache.size() == ID_COUNT, FINDING
                    + ": drawing an evicted id again re-creates a fresh manager -- expected a new instance and size " + ID_COUNT
                    + ", actual " + (recreated == old ? "the old instance" : "a new instance") + " and size " + cache.size());
            helper.assertTrue(cache.evict(EVICTED_ID) && cache.size() == ID_COUNT - 1, FINDING
                    + ": the re-created manager evicts again -- expected true and size " + (ID_COUNT - 1) + ", actual size " + cache.size());
            int cleared = cache.evictAll();
            helper.assertTrue(cleared == ID_COUNT - 1, FINDING + ": evictAll returns the count held -- expected " + (ID_COUNT - 1)
                    + ", actual " + cleared);
            helper.assertTrue(cache.size() == 0, FINDING + ": evictAll empties the cache -- expected size 0, actual " + cache.size());
            int clearedAgain = cache.evictAll();
            helper.assertTrue(clearedAgain == 0, FINDING + ": evictAll on an empty cache -- expected 0, actual " + clearedAgain);
        } finally {
            restore(type, previous);
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------
    // Row 2: the registry -- evict(entity) by type and id; the gauge the dump reads follows
    // ------------------------------------------------------------------

    private static void registryEvictsByTypeAndIdGaugeFollows(GameTestHelper helper) {
        EntityType<? extends Mob> type = ModEntities.BEAVER.get();
        OreSpawnAnimatableInstanceCache previous = GeoReplacementCaches.cacheFor(type);
        List<Mob> beavers = new ArrayList<>();
        try {
            OreSpawnAnimatableInstanceCache cache = registeredCacheOf(helper, new BeaverGeoReplacement());
            // Managers held by any other registered cache (none on the game-test server); the row asserts deltas from it.
            long base = GeoReplacementCaches.managersHeld();
            for (int i = 0; i < BEAVER_COUNT; i++) {
                beavers.add(spawnFrozen(helper, type, new BlockPos(BEAVER_X0 + 2 * i, 0, BEAVER_Z)));
            }
            for (Mob beaver : beavers) {
                // The draw: GeoModel.handleAnimations -> getAnimatableInstanceCache().getManagerForId(getInstanceId), the entity id.
                cache.getManagerForId(beaver.getId());
            }
            helper.assertTrue(cache.size() == BEAVER_COUNT, FINDING + ": one manager per drawn Beaver -- expected size " + BEAVER_COUNT
                    + ", actual " + cache.size());
            long held = GeoReplacementCaches.managersHeld();
            helper.assertTrue(held == base + BEAVER_COUNT, FINDING + ": managersHeld sums the registered caches -- expected "
                    + (base + BEAVER_COUNT) + ", actual " + held);
            Map<String, Long> dump = MHLibCounters.sumAndResetAll();
            Long gauge = dump.get(GeoReplacementCaches.MANAGERS_HELD_GAUGE);
            helper.assertTrue(gauge != null && gauge == base + BEAVER_COUNT, FINDING + ": the value the counters dump prints for "
                    + GeoReplacementCaches.MANAGERS_HELD_GAUGE + " -- expected " + (base + BEAVER_COUNT) + ", actual " + gauge);
            assertDumpOrder(helper, dump);
            for (Mob beaver : beavers) {
                helper.assertTrue(GeoReplacementCaches.evict(beaver), FINDING + ": evict(beaver " + beaver.getId()
                        + ") through the registry -- expected true, actual false");
            }
            long afterHeld = GeoReplacementCaches.managersHeld();
            helper.assertTrue(cache.size() == 0 && afterHeld == base, FINDING + ": every Beaver evicted -- expected cache size 0 and held "
                    + base + ", actual size " + cache.size() + " and held " + afterHeld);
            helper.assertTrue(!GeoReplacementCaches.evict(beavers.get(0)), FINDING + ": evicting an already-evicted Beaver -- expected false, actual true");
            Long gaugeAfter = MHLibCounters.sumAndResetAll().get(GeoReplacementCaches.MANAGERS_HELD_GAUGE);
            helper.assertTrue(gaugeAfter != null && gaugeAfter == base, FINDING + ": the gauge follows the evictions -- expected " + base
                    + ", actual " + gaugeAfter);
        } finally {
            for (Mob beaver : beavers) {
                discardQuietly(beaver);
            }
            restore(type, previous);
        }
        helper.succeed();
    }

    /** The dump's key order: the nine MHLib counters first, unchanged; the evictions counter after them; the gauge after every counter. */
    private static void assertDumpOrder(GameTestHelper helper, Map<String, Long> dump) {
        List<String> keys = new ArrayList<>(dump.keySet());
        int nine = MHLIB_COUNTER_NAMES.size();
        helper.assertTrue(keys.size() >= nine && keys.subList(0, nine).equals(MHLIB_COUNTER_NAMES), FINDING
                + ": the nine MHLib counters keep their names and order at the head of the dump -- expected " + MHLIB_COUNTER_NAMES
                + ", actual " + keys);
        int evictions = keys.indexOf(GeoReplacementCaches.EVICTIONS_COUNTER);
        helper.assertTrue(evictions >= nine, FINDING + ": " + GeoReplacementCaches.EVICTIONS_COUNTER
                + " follows the nine -- expected index >= " + nine + ", actual " + evictions);
        int gauge = keys.indexOf(GeoReplacementCaches.MANAGERS_HELD_GAUGE);
        int counters = MHLibCounters.all().size();
        helper.assertTrue(gauge > evictions && gauge >= counters, FINDING + ": the gauge is appended after every counter -- expected index >= "
                + counters + " and after " + GeoReplacementCaches.EVICTIONS_COUNTER + ", actual " + gauge + " in " + keys);
    }

    // ------------------------------------------------------------------
    // Row 3: a type without a cache is a no-op
    // ------------------------------------------------------------------

    private static void unregisteredTypeIsANoOp(GameTestHelper helper) {
        Mob pig = null;
        try {
            OreSpawnAnimatableInstanceCache pigCache = GeoReplacementCaches.cacheFor(EntityType.PIG);
            helper.assertTrue(pigCache == null, FINDING + ": precondition -- the pig has no replacement cache, actual " + pigCache);
            pig = spawnFrozen(helper, EntityType.PIG, PIG_POS);
            long before = GeoReplacementCaches.managersHeld();
            boolean evicted = GeoReplacementCaches.evict(pig);
            helper.assertTrue(!evicted, FINDING + ": evict(pig) for a type without a cache -- expected false, actual true");
            long after = GeoReplacementCaches.managersHeld();
            helper.assertTrue(after == before, FINDING + ": a no-op eviction leaves the gauge unchanged -- expected " + before + ", actual " + after);
        } finally {
            discardQuietly(pig);
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------
    // Row 4: evictAll clears every registered cache (Beaver + Coin)
    // ------------------------------------------------------------------

    private static void evictAllClearsEveryRegisteredCache(GameTestHelper helper) {
        EntityType<?> beaverType = ModEntities.BEAVER.get();
        EntityType<?> coinType = ModEntities.COIN.get();
        OreSpawnAnimatableInstanceCache previousBeaver = GeoReplacementCaches.cacheFor(beaverType);
        OreSpawnAnimatableInstanceCache previousCoin = GeoReplacementCaches.cacheFor(coinType);
        try {
            OreSpawnAnimatableInstanceCache beaverCache = registeredCacheOf(helper, new BeaverGeoReplacement());
            OreSpawnAnimatableInstanceCache coinCache = registeredCacheOf(helper, new CoinGeoReplacement());
            helper.assertTrue(beaverCache != coinCache, FINDING + ": two species, two caches -- expected distinct caches, actual the same instance");
            long base = GeoReplacementCaches.managersHeld();
            for (long id = 1; id <= PER_CACHE; id++) {
                beaverCache.getManagerForId(id);
                coinCache.getManagerForId(id);
            }
            helper.assertTrue(beaverCache.size() == PER_CACHE && coinCache.size() == PER_CACHE, FINDING + ": " + PER_CACHE
                    + " managers per cache -- expected " + PER_CACHE + " / " + PER_CACHE + ", actual " + beaverCache.size() + " / " + coinCache.size());
            long held = GeoReplacementCaches.managersHeld();
            helper.assertTrue(held == base + 2 * PER_CACHE, FINDING + ": managersHeld across both caches -- expected " + (base + 2 * PER_CACHE)
                    + ", actual " + held);
            int evicted = GeoReplacementCaches.evictAll();
            helper.assertTrue(evicted == base + 2 * PER_CACHE, FINDING + ": evictAll returns every manager held -- expected "
                    + (base + 2 * PER_CACHE) + ", actual " + evicted);
            long afterHeld = GeoReplacementCaches.managersHeld();
            helper.assertTrue(afterHeld == 0 && beaverCache.size() == 0 && coinCache.size() == 0, FINDING
                    + ": every registered cache is empty after evictAll -- expected held 0 and sizes 0 / 0, actual held " + afterHeld
                    + " and sizes " + beaverCache.size() + " / " + coinCache.size());
        } finally {
            restore(beaverType, previousBeaver);
            restore(coinType, previousCoin);
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------
    // Row 5: the evictor's guard -- a server-side level is ignored; the registry still evicts
    // ------------------------------------------------------------------

    private static void serverSideLeaveIsIgnoredByTheGuard(GameTestHelper helper) {
        EntityType<? extends Mob> type = ModEntities.BEAVER.get();
        OreSpawnAnimatableInstanceCache previous = GeoReplacementCaches.cacheFor(type);
        Mob beaver = null;
        try {
            OreSpawnAnimatableInstanceCache cache = registeredCacheOf(helper, new BeaverGeoReplacement());
            beaver = spawnFrozen(helper, type, BEAVER_POS);
            cache.getManagerForId(beaver.getId());
            long held = GeoReplacementCaches.managersHeld();
            helper.assertTrue(cache.size() == 1, FINDING + ": the drawn Beaver holds one manager -- expected size 1, actual " + cache.size());
            ServerLevel level = helper.getLevel();
            helper.assertTrue(!level.isClientSide(), FINDING + ": precondition -- the game-test level is a server level, expected isClientSide false, actual true");
            boolean guarded = GeoReplacementCacheEvictor.evictIfClient(level, beaver);
            helper.assertTrue(!guarded, FINDING + ": evictIfClient on a server level -- expected false (the guard), actual true");
            long afterGuard = GeoReplacementCaches.managersHeld();
            helper.assertTrue(cache.size() == 1 && afterGuard == held, FINDING
                    + ": the guard touches nothing -- expected size 1 and held " + held + ", actual size " + cache.size() + " and held " + afterGuard);
            helper.assertTrue(GeoReplacementCaches.evict(beaver), FINDING + ": the registry evicts the same Beaver directly -- expected true, actual false");
            long afterEvict = GeoReplacementCaches.managersHeld();
            helper.assertTrue(cache.size() == 0 && afterEvict == held - 1, FINDING + ": after the direct eviction -- expected size 0 and held "
                    + (held - 1) + ", actual size " + cache.size() + " and held " + afterEvict);
        } finally {
            discardQuietly(beaver);
            restore(type, previous);
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    /**
     * The replacement's cache, checked to be the evictable class, registered for the replacement's type the way
     * {@code OreSpawnGeoReplacedEntityRenderer}'s constructor does in production (no renderer can be built here), and
     * checked to be the registry's entry for that type afterwards (replacing any previous entry).
     */
    private static OreSpawnAnimatableInstanceCache registeredCacheOf(GameTestHelper helper, OreSpawnGeoReplacement<?> replacement) {
        AnimatableInstanceCache cache = replacement.getAnimatableInstanceCache();
        helper.assertTrue(cache instanceof OreSpawnAnimatableInstanceCache, FINDING
                + ": a replacement's cache is the evictable OreSpawnAnimatableInstanceCache -- expected that class, actual "
                + (cache == null ? "null" : cache.getClass().getName()));
        helper.assertTrue(replacement.animatableCache() == cache, FINDING
                + ": animatableCache() is the cache GeckoLib reads through getAnimatableInstanceCache() -- expected the same instance");
        EntityType<?> type = replacement.getReplacingEntityType();
        GeoReplacementCaches.register(type, replacement.animatableCache());
        OreSpawnAnimatableInstanceCache registered = GeoReplacementCaches.cacheFor(type);
        helper.assertTrue(registered == cache, FINDING + ": registering a replacement's cache makes it the registry's entry for "
                + type + " (replacing any previous entry) -- expected the replacement's own cache, actual " + registered);
        return (OreSpawnAnimatableInstanceCache) cache;
    }

    /** Restores the registry entry a row replaced (TEST-003): the previous cache back, or the type unregistered when there was none. */
    private static void restore(EntityType<?> type, OreSpawnAnimatableInstanceCache previous) {
        if (previous == null) {
            GeoReplacementCaches.unregister(type);
        } else {
            GeoReplacementCaches.register(type, previous);
        }
    }

    /** A frozen mob with its feet ON the floor (rel y 0, F0.7): no AI, no gravity, persistent, on the ground. */
    private static Mob spawnFrozen(GameTestHelper helper, EntityType<? extends Mob> type, BlockPos pos) {
        Mob mob = helper.spawn(type, pos);
        mob.setNoAi(true);
        mob.setPersistenceRequired();
        mob.setNoGravity(true);
        mob.setOnGround(true);
        return mob;
    }

    private static void discardQuietly(Entity entity) {
        if (entity != null && !entity.isRemoved()) {
            entity.discard();
        }
    }
}
