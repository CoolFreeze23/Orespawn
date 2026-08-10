package danger.orespawn.gametest;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Cockateil;
import danger.orespawn.entity.EntityLavaLovingItem;
import danger.orespawn.entity.ThePrince;
import danger.orespawn.entity.ThePrincess;
import danger.orespawn.entity.UltimateFishHook;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Loot-parity GameTests (testing_session batch "loot", K base 300).
 *
 * <p>Far-region usage: exactly ONE fixed probe position is consumed —
 * K=300 → the Nether block-break probe at (300*512, 100, 50000) =
 * (153600, 100, 50000) used by {@link #i059_lavafoam_xp_overworld_vs_nether}.
 * Every other assertion runs inside its own template.</p>
 *
 * <p>Checklist items covered: i014-item-003-021, i033-ent-a-095-097,
 * i038-ent-d-037-ent-k-046, i055-c1-loot, i056-c2-loot, i057-c3-loot,
 * i058-c4-loot, i059-d4-drops, i060-boss-025-035-042, i086-pools,
 * i105-b1-boss-drops-survival-kills, i115-c7-crystal-egg-ores,
 * i131-break-any-spawn-ore-drops-itself-50-of-breaks-pop-5-9-xp.</p>
 *
 * <p>Method: per the batch mandate, drop tables are verified against the
 * loot-table DATA (the datapack JSON the server's resource stack actually
 * serves, plus a registration check against
 * {@code server.reloadableRegistries()}), with LIVE table rolls wherever a
 * drop is conditional on entity state (OreSpawnTamed NBT, BirdType) or kill
 * context (killed_by_player), and REAL controlled kills where the drop logic
 * lives in code ({@code die()} spawns, TheKing's registry shower) or where
 * the checklist explicitly asks for kills (i038/i060/i105, Stinky).</p>
 *
 * <p>Citation convention: "orig X.java:NN" =
 * reference_1_7_10_source/sources/danger/orespawn/X.java; C1..C4 =
 * phase_c_reports/C{1..4}_*.md; B1 = phase_b_reports/B1_drops.md;
 * D4 = phase_d_reports/D4_items_small_entities.md.</p>
 *
 * <p><b>Known main-code defect surfaced while writing this batch</b> (see
 * {@link #i014_i131_break_pops_xp_end_to_end}): on NeoForge 1.21.1 every
 * block-break path routes drops through
 * {@code CommonHooks.handleBlockDrops}, which invokes
 * {@code state.spawnAfterBreak(level, pos, tool, false)} — the comment in
 * the patched source reads "Always pass false for the dropXP (last) param
 * to spawnAfterBreak since we handle XP" — and takes break XP exclusively
 * from {@code IBlockExtension.getExpDrop} (via BlockDropsEvent). The port's
 * OreGenericEgg / OreUranium / OreTitanium implement their bonus XP inside
 * {@code spawnAfterBreak} gated on that always-false flag, so their
 * documented break XP can never reach the player through mining. Lavafoam
 * uses {@code getExpDrop} and is unaffected. The end-to-end test asserts
 * the DOCUMENTED behavior and is therefore expected to FAIL until the XP
 * rolls are moved into {@code getExpDrop} overrides.</p>
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class LootTests {

    private static final String NS = "orespawn";

    // ------------------------------------------------------------------
    // Shared plumbing — registry lookups
    // ------------------------------------------------------------------

    private static ResourceLocation rl(String id) {
        return ResourceLocation.parse(id);
    }

    /** Registry lookup that fails the test loudly instead of yielding AIR. */
    private static Item item(String id) {
        return BuiltInRegistries.ITEM.getOptional(rl(id))
                .orElseThrow(() -> new IllegalStateException("item not registered: " + id));
    }

    private static Block block(String id) {
        return BuiltInRegistries.BLOCK.getOptional(rl(id))
                .orElseThrow(() -> new IllegalStateException("block not registered: " + id));
    }

    private static EntityType<?> entityType(String path) {
        return BuiltInRegistries.ENTITY_TYPE.getOptional(rl(NS + ":" + path))
                .orElseThrow(() -> new IllegalStateException("entity type not registered: " + path));
    }

    // ------------------------------------------------------------------
    // Shared plumbing — loot-table data access
    // ------------------------------------------------------------------

    /**
     * Reads the raw loot-table JSON exactly as the running server's resource
     * stack serves it (data/orespawn/loot_table/{kind}/{name}.json). Shapes
     * asserted below therefore match the authored datapack 1:1, with no
     * codec round-trip normalization.
     */
    private static JsonObject lootJson(GameTestHelper helper, String kind, String name) {
        ResourceLocation file =
                ResourceLocation.fromNamespaceAndPath(NS, "loot_table/" + kind + "/" + name + ".json");
        Resource resource = helper.getLevel().getServer().getResourceManager().getResource(file)
                .orElseThrow(() -> new IllegalStateException("missing loot table file " + file));
        try (BufferedReader reader = resource.openAsReader()) {
            return GsonHelper.parse(reader);
        } catch (IOException e) {
            throw new UncheckedIOException("cannot read " + file, e);
        }
    }

    /** Asserts the entity's default loot table actually BOUND in the reloadable registry. */
    private static LootTable registeredEntityTable(GameTestHelper helper, String entityPath) {
        ResourceKey<LootTable> key = entityType(entityPath).getDefaultLootTable();
        LootTable table = helper.getLevel().getServer().reloadableRegistries().getLootTable(key);
        helper.assertTrue(table != LootTable.EMPTY,
                entityPath + ": loot table " + key.location() + " did not register");
        return table;
    }

    private static JsonArray pools(JsonObject tableJson) {
        return GsonHelper.getAsJsonArray(tableJson, "pools");
    }

    private static JsonObject pool(JsonObject tableJson, int index) {
        return pools(tableJson).get(index).getAsJsonObject();
    }

    private static int[] rollsRange(JsonObject pool) {
        JsonElement rolls = pool.get("rolls");
        if (rolls.isJsonPrimitive()) {
            int v = rolls.getAsInt();
            return new int[]{v, v};
        }
        JsonObject o = rolls.getAsJsonObject();
        return new int[]{o.get("min").getAsInt(), o.get("max").getAsInt()};
    }

    /** One expected weighted-pool entry: item id (or "EMPTY"), weight, min..max count. */
    private record PoolEntryExp(String id, int weight, int min, int max) {
        String key() {
            return id + "|w" + weight + "|" + min + ".." + max;
        }
    }

    private static PoolEntryExp e(String id, int weight, int min, int max) {
        return new PoolEntryExp(id, weight, min, max);
    }

    /** weight-1 entry with the vanilla no-set_count default of exactly 1. */
    private static PoolEntryExp one(String id) {
        return new PoolEntryExp(id, 1, 1, 1);
    }

    /** weight-1 entry with the 1.7.10 "vanilla drop core" count of 0-2 (+looting, not asserted). */
    private static PoolEntryExp core02(String id) {
        return new PoolEntryExp(id, 1, 0, 2);
    }

    private static PoolEntryExp empty(int weight) {
        return new PoolEntryExp("EMPTY", weight, 0, 0);
    }

    private static List<String> actualEntryKeys(JsonObject pool) {
        List<String> keys = new ArrayList<>();
        for (JsonElement el : GsonHelper.getAsJsonArray(pool, "entries")) {
            JsonObject entry = el.getAsJsonObject();
            String type = GsonHelper.getAsString(entry, "type");
            int weight = GsonHelper.getAsInt(entry, "weight", 1);
            if (type.equals("minecraft:empty")) {
                keys.add(new PoolEntryExp("EMPTY", weight, 0, 0).key());
                continue;
            }
            String name = GsonHelper.getAsString(entry, "name", "<" + type + ">");
            int min = 1;
            int max = 1;
            if (entry.has("functions")) {
                for (JsonElement fnEl : entry.getAsJsonArray("functions")) {
                    JsonObject fn = fnEl.getAsJsonObject();
                    if (GsonHelper.getAsString(fn, "function").equals("minecraft:set_count")) {
                        JsonElement count = fn.get("count");
                        if (count.isJsonPrimitive()) {
                            min = max = count.getAsInt();
                        } else {
                            min = count.getAsJsonObject().get("min").getAsInt();
                            max = count.getAsJsonObject().get("max").getAsInt();
                        }
                    }
                }
            }
            keys.add(new PoolEntryExp(name, weight, min, max).key());
        }
        return keys;
    }

    /** Order-insensitive exact-multiset compare of a pool's entries + its rolls range. */
    private static void assertPool(GameTestHelper helper, String what, JsonObject pool,
            int rollsMin, int rollsMax, PoolEntryExp... expected) {
        int[] rr = rollsRange(pool);
        helper.assertTrue(rr[0] == rollsMin && rr[1] == rollsMax,
                what + ": rolls " + rr[0] + ".." + rr[1] + " != documented " + rollsMin + ".." + rollsMax);
        List<String> actual = new ArrayList<>(actualEntryKeys(pool));
        List<String> exp = new ArrayList<>();
        for (PoolEntryExp entry : expected) {
            exp.add(entry.key());
        }
        Collections.sort(actual);
        Collections.sort(exp);
        helper.assertTrue(actual.equals(exp), what + ": entries " + actual + " != documented " + exp);
    }

    /**
     * Asserts the pool is gated on the OreSpawnTamed NBT flag
     * (entity_properties predicate, optionally wrapped in minecraft:inverted) —
     * the Gazelle/Ostrich convention (phase_c_reports/C2/C3, ENT-D-037/ENT-K-046).
     */
    private static void assertTamedGate(GameTestHelper helper, String what, JsonObject pool, boolean inverted) {
        JsonArray conds = GsonHelper.getAsJsonArray(pool, "conditions");
        helper.assertTrue(conds.size() == 1, what + ": expected exactly 1 pool condition");
        JsonObject cond = conds.get(0).getAsJsonObject();
        String type = GsonHelper.getAsString(cond, "condition");
        JsonObject props;
        if (inverted) {
            helper.assertTrue(type.equals("minecraft:inverted"), what + ": condition " + type + " != inverted");
            props = GsonHelper.getAsJsonObject(cond, "term");
            helper.assertTrue(GsonHelper.getAsString(props, "condition").equals("minecraft:entity_properties"),
                    what + ": inverted term is not entity_properties");
        } else {
            helper.assertTrue(type.equals("minecraft:entity_properties"),
                    what + ": condition " + type + " != entity_properties");
            props = cond;
        }
        String nbt = GsonHelper.getAsString(GsonHelper.getAsJsonObject(props, "predicate"), "nbt");
        helper.assertTrue(nbt.contains("OreSpawnTamed:1b"), what + ": predicate nbt " + nbt);
    }

    // ------------------------------------------------------------------
    // Shared plumbing — live rolls, kills, drop/XP counting
    // ------------------------------------------------------------------

    /**
     * Rolls the entity's registered loot table with the exact parameter shape
     * the vanilla death path builds (LivingEntity.dropFromLootTable):
     * THIS_ENTITY / ORIGIN / DAMAGE_SOURCE always, plus LAST_DAMAGE_PLAYER +
     * ATTACKING_ENTITY when simulating a player kill — LAST_DAMAGE_PLAYER is
     * precisely what minecraft:killed_by_player tests.
     */
    private static List<ItemStack> rollEntityTable(GameTestHelper helper, LivingEntity mob,
            boolean playerKill, Player player) {
        ServerLevel level = helper.getLevel();
        LootTable table = level.getServer().reloadableRegistries().getLootTable(mob.getLootTable());
        DamageSource source = playerKill
                ? level.damageSources().playerAttack(player)
                : level.damageSources().generic();
        LootParams.Builder builder = new LootParams.Builder(level)
                .withParameter(LootContextParams.THIS_ENTITY, mob)
                .withParameter(LootContextParams.ORIGIN, mob.position())
                .withParameter(LootContextParams.DAMAGE_SOURCE, source);
        if (playerKill) {
            builder.withParameter(LootContextParams.LAST_DAMAGE_PLAYER, player)
                    .withOptionalParameter(LootContextParams.ATTACKING_ENTITY, player);
        }
        return table.getRandomItems(builder.create(LootContextParamSets.ENTITY));
    }

    private static int countOf(List<ItemStack> stacks, Item item) {
        int total = 0;
        for (ItemStack stack : stacks) {
            if (stack.is(item)) {
                total += stack.getCount();
            }
        }
        return total;
    }

    /** Controlled player-attributed kill: floor the health, land one real player hit. */
    private static void killByPlayer(GameTestHelper helper, LivingEntity mob, Player player) {
        mob.setHealth(1.0f);
        mob.hurt(helper.getLevel().damageSources().playerAttack(player), 200.0f);
        helper.assertTrue(mob.isDeadOrDying(),
                "player hit failed to kill " + mob.getType() + " (health=" + mob.getHealth() + ")");
    }

    private static AABB box(Vec3 center, double horizontal, double vertical) {
        return new AABB(center.x - horizontal, center.y - vertical, center.z - horizontal,
                center.x + horizontal, center.y + vertical, center.z + horizontal);
    }

    /** Total item counts by item type inside the box (stack counts summed). */
    private static Map<Item, Integer> collectDrops(GameTestHelper helper, AABB area) {
        Map<Item, Integer> out = new HashMap<>();
        for (ItemEntity drop : helper.getLevel().getEntitiesOfClass(ItemEntity.class, area)) {
            out.merge(drop.getItem().getItem(), drop.getItem().getCount(), Integer::sum);
        }
        return out;
    }

    private static int totalCount(Map<Item, Integer> drops) {
        int total = 0;
        for (int v : drops.values()) {
            total += v;
        }
        return total;
    }

    private static void clearDropsAndXp(GameTestHelper helper, AABB area) {
        helper.getLevel().getEntitiesOfClass(ItemEntity.class, area).forEach(Entity::discard);
        helper.getLevel().getEntitiesOfClass(ExperienceOrb.class, area).forEach(Entity::discard);
    }

    /**
     * Total XP points inside the box. Orb entities merge (same value → count++),
     * so the true total is Value×Count per orb, both read from the orb's saved
     * NBT (ExperienceOrb.addAdditionalSaveData writes "Value"/"Count").
     */
    private static int totalXp(ServerLevel level, AABB area) {
        int total = 0;
        for (ExperienceOrb orb : level.getEntitiesOfClass(ExperienceOrb.class, area)) {
            CompoundTag tag = new CompoundTag();
            orb.saveWithoutId(tag);
            total += tag.getShort("Value") * Math.max(1, tag.getInt("Count"));
        }
        return total;
    }

    private static LivingEntity spawnLiving(GameTestHelper helper, String path, BlockPos relPos) {
        Entity spawned = helper.spawn(entityType(path), relPos);
        return (LivingEntity) spawned;
    }

    // ==================================================================
    // i014-item-003-021 — ITEM-003 / ITEM-021
    // ==================================================================

    /**
     * Checklist i014-item-003-021 / finding ITEM-003 — "Break
     * ore_uranium/ore_titanium placed below y=40 and above. Expect: XP only
     * below y=40" (FIX_LOG.md:110).
     *
     * <p>Documented values: orig OreUranium.java:101-107 — bonus XP
     * {@code 5 + nextInt(5) + nextInt(10)} (= 5..19) on EVERY break with
     * {@code y < 40}, nothing at y >= 40; identical shape in orig
     * OreTitanium (port OreUranium.java:60-66 / OreTitanium.java, constant
     * XP_MAX_Y_EXCLUSIVE = 40). Mechanism assert: since the 2026-08-10 fix
     * the roll lives in {@code getExpDrop} — NeoForge 1.21.1's sole break-XP
     * source (CommonHooks.handleBlockDrops calls spawnAfterBreak with
     * dropExperience=false unconditionally) — invoked here via the
     * {@code BlockState.getExpDrop} delegate at absolute Y 30 and Y 45.
     * Deterministic — every sub-40 call must return 5..19, every 45 call 0.
     * The end-to-end mining wiring is the (green)
     * {@link #i014_i131_break_pops_xp_end_to_end} sentinel.</p>
     */
    @GameTest(template = "empty")
    public void i014_uranium_titanium_xp_gate_mechanism(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 1, 1));
        for (String id : new String[]{NS + ":ore_uranium", NS + ":ore_titanium"}) {
            BlockState state = block(id).defaultBlockState();
            BlockPos below = new BlockPos(anchor.getX(), 30, anchor.getZ());
            BlockPos above = new BlockPos(anchor.getX(), 45, anchor.getZ());
            for (int i = 0; i < 20; i++) {
                int xp = state.getExpDrop(level, below, null, null, ItemStack.EMPTY);
                helper.assertTrue(xp >= 5 && xp <= 19,
                        id + " below y40: XP " + xp + " outside 5..19 (orig OreUranium.java:101-107)");
            }
            for (int i = 0; i < 20; i++) {
                int xpAbove = state.getExpDrop(level, above, null, null, ItemStack.EMPTY);
                helper.assertTrue(xpAbove == 0, id + " at y45 must never pop XP, got " + xpAbove);
            }
        }
        helper.succeed();
    }

    /**
     * Checklist i014-item-003-021 / finding ITEM-021 — "breaking an
     * ender-pearl/eye-of-ender egg block pops 5-9 XP half the time and never
     * duplicates itself" (FIX_LOG.md:110).
     *
     * <p>Documented values: orig OreGenericEgg.java:24-30 —
     * {@code j1 = 5 + nextInt(3) + nextInt(3)} (= 5..9) awarded on a
     * {@code nextInt(2) == 1} roll (50%); port OreGenericEgg.java:33-39.
     * The removed exploit (ITEM-021) had the block drop 5..9 EXTRA COPIES of
     * itself — the self-drop must therefore be exactly one
     * (loot_table/blocks/block_ender_pearl.json: single unconditional
     * self-entry, no count functions).</p>
     *
     * <p>Statistics: 300 direct {@code BlockState.getExpDrop} rolls (the
     * sole NeoForge break-XP source since the 2026-08-10 fix — see
     * {@link #i014_uranium_titanium_xp_gate_mechanism}), nonzero returns
     * counted against the Hoeffding band [90, 210] around the expected 150:
     * P(|X-150| >= 60) <= 2*exp(-2*300*0.2^2) = 2e^-24 ≈ 7.6e-11
     * &lt; 1e-9. Every nonzero return must be 5..9.</p>
     */
    @GameTest(template = "empty")
    public void i014_egg_block_xp_mechanism_and_no_duplication(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Player breaker = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos probePos = new BlockPos(helper.absolutePos(new BlockPos(1, 1, 1)).getX(), 30,
                helper.absolutePos(new BlockPos(1, 1, 1)).getZ());

        for (String id : new String[]{NS + ":block_ender_pearl", NS + ":block_eye_of_ender"}) {
            // JSON structure: exactly one pool, exactly one self entry, count 1.
            JsonObject json = lootJson(helper, "blocks", id.substring(NS.length() + 1));
            helper.assertTrue(pools(json).size() == 1, id + ": expected a single loot pool");
            assertPool(helper, id + " self-drop", pool(json, 0), 1, 1, one(id));

            // 50% roll of 5..9 XP (mechanism).
            BlockState state = block(id).defaultBlockState();
            int events = 0;
            for (int i = 0; i < 300; i++) {
                int xp = state.getExpDrop(level, probePos, null, null, ItemStack.EMPTY);
                if (xp > 0) {
                    events++;
                    helper.assertTrue(xp >= 5 && xp <= 9,
                            id + ": XP event " + xp + " outside 5..9 (orig OreGenericEgg.java:26-29)");
                }
            }
            helper.assertTrue(events >= 90 && events <= 210,
                    id + ": " + events + "/300 XP events outside the 50% Hoeffding band [90,210]");

            // Never duplicates itself: in-world break drops EXACTLY one of the block.
            BlockPos breakPos = helper.absolutePos(new BlockPos(1, 2, 1));
            for (int i = 0; i < 10; i++) {
                clearDropsAndXp(helper, box(Vec3.atCenterOf(breakPos), 4, 4));
                level.setBlock(breakPos, state, 3);
                level.destroyBlock(breakPos, true, breaker);
                Map<Item, Integer> drops = collectDrops(helper, box(Vec3.atCenterOf(breakPos), 4, 4));
                helper.assertTrue(drops.getOrDefault(item(id), 0) == 1 && totalCount(drops) == 1,
                        id + ": break #" + i + " dropped " + drops + " instead of exactly 1x itself (ITEM-021)");
            }
            clearDropsAndXp(helper, box(Vec3.atCenterOf(breakPos), 4, 4));
        }
        helper.succeed();
    }

    /**
     * Checklist i014-item-003-021 + i131 (D5c) end-to-end acceptance — XP must
     * actually pop when a player MINES the blocks: uranium below y=40 pops
     * 5..19 every break (ITEM-003, orig OreUranium.java:101-107, FIX_LOG.md:110),
     * the egg block and every SpawnOres-pool block pop 5..9 on ~50% of breaks
     * (ITEM-021 / D5c "Break any spawn ore ... ~50% of breaks pop 5-9 XP",
     * orig OreGenericEgg.java:24-30).
     *
     * <p><b>EXPECTED-FAIL sentinel</b>: static analysis of the NeoForge
     * 1.21.1 runtime (patched sources, CommonHooks.handleBlockDrops) shows
     * every break path calls {@code spawnAfterBreak(..., dropXP=false)} and
     * sources break XP solely from {@code getExpDrop}, which these blocks do
     * not override — so this documented behavior cannot currently occur.
     * The test stands as the acceptance criterion; it goes green once the
     * XP rolls move to {@code getExpDrop} (BlockDropsEvent path).</p>
     *
     * <p>Statistics (as documented behavior): uranium XP is unconditional
     * below y=40 → 10 breaks deterministic; the 50% blocks get 60 breaks each
     * with an at-least-one-event assert — false-fail 0.5^60 ≈ 8.7e-19.</p>
     */
    @GameTest(template = "empty")
    public void i014_i131_break_pops_xp_end_to_end(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Player breaker = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos breakPos = helper.absolutePos(new BlockPos(1, 1, 1)); // template floor sits far below y=40
        helper.assertTrue(breakPos.getY() < 40,
                "test-world template must sit below y=40 for the uranium gate, is at " + breakPos.getY());
        AABB area = box(Vec3.atCenterOf(breakPos), 4, 6);

        // ITEM-003: every sub-40 uranium break pops XP.
        int uraniumBreaksWithXp = 0;
        for (int i = 0; i < 10; i++) {
            clearDropsAndXp(helper, area);
            level.setBlock(breakPos, block(NS + ":ore_uranium").defaultBlockState(), 3);
            level.destroyBlock(breakPos, true, breaker);
            if (totalXp(level, area) > 0) {
                uraniumBreaksWithXp++;
            }
        }
        helper.assertTrue(uraniumBreaksWithXp == 10,
                "ore_uranium below y=40: only " + uraniumBreaksWithXp
                        + "/10 player breaks popped XP — documented: every break pops 5..19"
                        + " (ITEM-003; XP roll is stranded in spawnAfterBreak, see class javadoc)");

        // ITEM-021 + D5c: 50%-roll blocks must pop XP on some of 60 breaks.
        for (String id : new String[]{NS + ":block_ender_pearl", NS + ":spider_spawn_block"}) {
            BlockState state = block(id).defaultBlockState();
            int xpEvents = 0;
            for (int i = 0; i < 60; i++) {
                clearDropsAndXp(helper, area);
                level.setBlock(breakPos, state, 3);
                level.destroyBlock(breakPos, true, breaker);
                if (totalXp(level, area) > 0) {
                    xpEvents++;
                }
            }
            clearDropsAndXp(helper, area);
            helper.assertTrue(xpEvents > 0,
                    id + ": 0/60 player breaks popped XP — documented: ~50% pop 5..9"
                            + " (ITEM-021/D5c; false-fail 0.5^60 ≈ 8.7e-19)");
        }
        helper.succeed();
    }

    // ==================================================================
    // i033-ent-a-095-097 — ENT-A-095 / ENT-A-097
    // ==================================================================

    /**
     * Checklist i033-ent-a-095-097 — "Summon ~10 Cockateils. Expect: random
     * bird types; only type-5 birds can drop rubies (player kill, 1-in-3)."
     *
     * <p>Documented values: ENT-A-095 (C1_entities_A_C.md:65) — orig
     * Cockateil.java:82-86, {@code birdtype = nextInt(6)} at spawn (port
     * finalizeSpawn override). ENT-A-097 (C1:41) — orig Cockateil.java:242-248,
     * ruby only when BirdType==5 AND killedByPlayer AND nextInt(3)==1, else
     * feather, single pick (never both), count core 0-2; port cockateil.json
     * alternatives with BirdType NBT predicate + killed_by_player +
     * random_chance 0.3333.</p>
     *
     * <p>Statistics: variant spread — P(30 natural spawns land in only ≤2 of
     * the 6 types) ≤ C(6,2)·(2/6)^30 ≈ 7e-14. Ruby presence over 100
     * player-kill rolls of a type-5 bird — per-roll observable-ruby chance is
     * 1/3 (pick) × 2/3 (count 1-2 of 0-2) = 2/9, false-fail (7/9)^100
     * ≈ 1.2e-11. Negative sides (non-player kill ×60, type≠5 ×60, and 25 real
     * player kills) are hard zero-probability asserts under the documented
     * table, so any ruby there is a genuine failure.</p>
     */
    @GameTest(template = "empty_large")
    public void i033_cockateil_variants_and_type5_ruby(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        Item ruby = item(NS + ":ruby");
        Item feather = Items.FEATHER;

        // ENT-A-097 structure: alternatives [ruby(BirdType 5 + killed_by_player + 1/3), feather].
        JsonObject json = lootJson(helper, "entities", "cockateil");
        JsonObject alt = pool(json, 0).getAsJsonArray("entries").get(0).getAsJsonObject();
        helper.assertTrue(GsonHelper.getAsString(alt, "type").equals("minecraft:alternatives"),
                "cockateil: first entry must be an alternatives pick (single pick, never both)");
        JsonArray children = GsonHelper.getAsJsonArray(alt, "children");
        helper.assertTrue(children.size() == 2, "cockateil: alternatives must hold ruby+feather");
        JsonObject rubyChild = children.get(0).getAsJsonObject();
        helper.assertTrue(GsonHelper.getAsString(rubyChild, "name").equals(NS + ":ruby"),
                "cockateil: first alternative must be the ruby branch");
        Set<String> condTypes = new HashSet<>();
        double chance = -1.0;
        boolean birdType5 = false;
        for (JsonElement condEl : GsonHelper.getAsJsonArray(rubyChild, "conditions")) {
            JsonObject cond = condEl.getAsJsonObject();
            String type = GsonHelper.getAsString(cond, "condition");
            condTypes.add(type);
            if (type.equals("minecraft:random_chance")) {
                chance = cond.get("chance").getAsDouble();
            }
            if (type.equals("minecraft:entity_properties")) {
                birdType5 = GsonHelper.getAsString(
                        GsonHelper.getAsJsonObject(cond, "predicate"), "nbt").contains("BirdType:5");
            }
        }
        helper.assertTrue(condTypes.contains("minecraft:killed_by_player"),
                "cockateil ruby branch must require killed_by_player (orig Cockateil.java:242-248)");
        helper.assertTrue(birdType5, "cockateil ruby branch must require BirdType:5");
        helper.assertTrue(Math.abs(chance - (1.0 / 3.0)) < 0.01,
                "cockateil ruby chance " + chance + " != 1-in-3 (orig nextInt(3)==1)");
        helper.assertTrue(GsonHelper.getAsString(children.get(1).getAsJsonObject(), "name")
                .equals("minecraft:feather"), "cockateil fallback alternative must be feather");

        // ENT-A-095: birdtype = nextInt(6) at spawn.
        BlockPos relSpot = new BlockPos(24, 2, 24);
        Set<Integer> seenTypes = new HashSet<>();
        for (int i = 0; i < 30; i++) {
            Cockateil bird = ModEntities.COCKATEIL.get().create(level);
            helper.assertTrue(bird != null, "cockateil create failed");
            BlockPos abs = helper.absolutePos(relSpot);
            bird.moveTo(abs.getX(), abs.getY(), abs.getZ(), 0, 0);
            bird.finalizeSpawn(level, level.getCurrentDifficultyAt(abs), MobSpawnType.NATURAL, null);
            int type = bird.getBirdType();
            helper.assertTrue(type >= 0 && type <= 5, "BirdType " + type + " outside nextInt(6)");
            seenTypes.add(type);
            bird.discard();
        }
        helper.assertTrue(seenTypes.size() >= 3,
                "30 natural spawns produced only bird types " + seenTypes + " — not randomized (ENT-A-095)");

        // Mechanism rolls on a type-5 bird.
        Cockateil bird5 = ModEntities.COCKATEIL.get().create(level);
        bird5.moveTo(helper.absoluteVec(new Vec3(24, 2, 24)));
        bird5.setBirdType(5);
        boolean sawRuby = false;
        for (int i = 0; i < 100; i++) {
            List<ItemStack> roll = rollEntityTable(helper, bird5, true, player);
            int rubies = countOf(roll, ruby);
            int feathers = countOf(roll, feather);
            helper.assertTrue(!(rubies > 0 && feathers > 0),
                    "cockateil rolled ruby AND feather in one kill — must be a single pick");
            for (ItemStack stack : roll) {
                helper.assertTrue(stack.is(ruby) || stack.is(feather),
                        "cockateil rolled foreign item " + stack);
            }
            if (rubies > 0) {
                sawRuby = true;
            }
        }
        helper.assertTrue(sawRuby,
                "type-5 cockateil never rolled a ruby in 100 player-kill rolls (p_miss ≈ 1.2e-11)");
        for (int i = 0; i < 60; i++) {
            helper.assertTrue(countOf(rollEntityTable(helper, bird5, false, player), ruby) == 0,
                    "type-5 cockateil rolled a ruby WITHOUT killed_by_player context");
        }
        Cockateil bird2 = ModEntities.COCKATEIL.get().create(level);
        bird2.moveTo(helper.absoluteVec(new Vec3(24, 2, 24)));
        bird2.setBirdType(2);
        for (int i = 0; i < 60; i++) {
            helper.assertTrue(countOf(rollEntityTable(helper, bird2, true, player), ruby) == 0,
                    "type-2 cockateil rolled a ruby — only type 5 may (ENT-A-097)");
        }

        // Controlled REAL kills (kill-context path end-to-end).
        AABB killArea = box(helper.absoluteVec(new Vec3(24, 2, 24)), 6, 6);
        for (int i = 0; i < 25; i++) {
            clearDropsAndXp(helper, killArea);
            Cockateil victim = (Cockateil) spawnLiving(helper, "cockateil", relSpot);
            victim.setBirdType(5);
            killByPlayer(helper, victim, player);
            Map<Item, Integer> drops = collectDrops(helper, killArea);
            for (Item dropped : drops.keySet()) {
                helper.assertTrue(dropped == ruby || dropped == feather,
                        "type-5 cockateil kill dropped foreign item " + dropped);
            }
            helper.assertTrue(!(drops.containsKey(ruby) && drops.containsKey(feather)),
                    "type-5 cockateil kill dropped ruby AND feather — single pick violated");
            victim.discard();
        }
        clearDropsAndXp(helper, killArea);
        helper.succeed();
    }

    // ==================================================================
    // i038-ent-d-037-ent-k-046 — ENT-D-037 / ENT-K-046
    // ==================================================================

    /**
     * Checklist i038-ent-d-037-ent-k-046 — "Kill tamed vs untamed Gazelle and
     * Ostrich. Expect: tamed 2-6 poppies; untamed Gazelle 0-2 beef, untamed
     * Ostrich 0-2 feathers."
     *
     * <p>Documented values: ENT-D-037 (AUDIT_FINDINGS.md:1377-1383,
     * FIX_LOG.md:70) — orig Gazelle.java:341-352 tamed → 2-6 poppies
     * (nextInt(5)+2), untamed → vanilla beef 0-2 (:337-339). ENT-K-046
     * (AUDIT_FINDINGS.md:2078-2084) — orig Ostrich.java:283-294 tamed → 2-6
     * poppies, untamed → feather 0-2 (:279-281). Both ported as
     * OreSpawnTamed-NBT-branched JSON (gazelle.json / ostrich.json).
     * Real controlled kills with player attribution; every bound here is
     * deterministic per kill (tamed minimum 2 makes the branch observable
     * on every single kill).</p>
     */
    @GameTest(template = "empty_large")
    public void i038_gazelle_ostrich_tamed_vs_untamed_kills(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos relSpot = new BlockPos(24, 2, 24);
        AABB area = box(helper.absoluteVec(new Vec3(24, 2, 24)), 6, 6);
        Item poppy = Items.POPPY;

        String[][] species = {
                {"gazelle", "minecraft:beef"},
                {"ostrich", "minecraft:feather"},
        };
        for (String[] row : species) {
            String path = row[0];
            Item untamedDrop = item(row[1]);

            // Structural: two pools branched on OreSpawnTamed.
            JsonObject json = lootJson(helper, "entities", path);
            assertTamedGate(helper, path + " tamed pool", pool(json, 0), false);
            assertPool(helper, path + " tamed pool", pool(json, 0), 1, 1,
                    e("minecraft:poppy", 1, 2, 6));
            assertTamedGate(helper, path + " untamed pool", pool(json, 1), true);
            assertPool(helper, path + " untamed pool", pool(json, 1), 1, 1, core02(row[1]));
            registeredEntityTable(helper, path);

            for (int i = 0; i < 6; i++) {
                clearDropsAndXp(helper, area);
                LivingEntity tamed = spawnLiving(helper, path, relSpot);
                ((TamableAnimal) tamed).setTame(true, false);
                killByPlayer(helper, tamed, player);
                Map<Item, Integer> drops = collectDrops(helper, area);
                int poppies = drops.getOrDefault(poppy, 0);
                helper.assertTrue(poppies >= 2 && poppies <= 6,
                        "tamed " + path + " dropped " + poppies + " poppies, documented 2-6");
                helper.assertTrue(totalCount(drops) == poppies,
                        "tamed " + path + " dropped extras: " + drops);
                tamed.discard();
            }
            for (int i = 0; i < 20; i++) {
                clearDropsAndXp(helper, area);
                LivingEntity wild = spawnLiving(helper, path, relSpot);
                killByPlayer(helper, wild, player);
                Map<Item, Integer> drops = collectDrops(helper, area);
                helper.assertTrue(!drops.containsKey(poppy),
                        "untamed " + path + " dropped poppies: " + drops);
                int core = drops.getOrDefault(untamedDrop, 0);
                helper.assertTrue(core <= 2, "untamed " + path + " dropped " + core
                        + "x " + row[1] + ", documented 0-2 (no looting)");
                helper.assertTrue(totalCount(drops) == core,
                        "untamed " + path + " dropped foreign items: " + drops);
                wild.discard();
            }
            clearDropsAndXp(helper, area);
        }
        helper.succeed();
    }

    // ==================================================================
    // i055-c1-loot — C1 drop spot-checks
    // ==================================================================

    /**
     * Checklist i055-c1-loot — Alien / AntRobot / Beaver / tamed Camarasaurus
     * / CaveFisher / CliffRacer / CloudShark / Cryolophosaurus /
     * CreepingHorror / Coin tables, asserted from the loot-table data.
     *
     * <p>Documented values (phase_c_reports/C1_entities_A_C.md rows
     * ENT-A-037/070/081/090/113 + the audit-corrected drop notes, and the
     * per-table orig citations embedded in each JSON): Alien.java:179-192
     * (5-10 spider eyes, 5-10 flint, map, clock, compass);
     * AntRobot.java:1112-1164 (7-13 rolls of nextInt(12): 10 redstone-part
     * items, redstone_block twice = weight 2, one dead slot);
     * Beaver.java:265-267 (porkchop 0-2); Camarasaurus.java:303-312 (tamed
     * only: 2-6 poppies, untamed nothing); CaveFisher.java:141-153 (d6:
     * gold/uranium/titanium nugget else nothing, core 0-2);
     * CliffRacer.java:149-161 (d8: chicken/uranium/titanium else nothing);
     * CloudShark.java:263-275 (d3: paper/string/bone);
     * Cryolophosaurus.java:120-132 (d10: chicken/uranium/titanium else
     * nothing); CreepingHorror.java:119-128 (d3: rotten flesh/bone/string);
     * Coin.java:98-129 (single d10 jackpot; slot 8 = coin_spawn_egg since D4
     * §8, ENT-A-098).</p>
     */
    @GameTest(template = "empty")
    public void i055_c1_drop_tables(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);

        registeredEntityTable(helper, "alien");
        JsonObject alien = lootJson(helper, "entities", "alien");
        helper.assertTrue(pools(alien).size() == 5, "alien: 5 independent pools documented");
        assertPool(helper, "alien eyes", pool(alien, 0), 1, 1, e("minecraft:spider_eye", 1, 5, 10));
        assertPool(helper, "alien flint", pool(alien, 1), 1, 1, e("minecraft:flint", 1, 5, 10));
        assertPool(helper, "alien map", pool(alien, 2), 1, 1, one("minecraft:map"));
        assertPool(helper, "alien clock", pool(alien, 3), 1, 1, one("minecraft:clock"));
        assertPool(helper, "alien compass", pool(alien, 4), 1, 1, one("minecraft:compass"));

        registeredEntityTable(helper, "ant_robot");
        JsonObject antRobot = lootJson(helper, "entities", "ant_robot");
        assertPool(helper, "ant_robot d12", pool(antRobot, 0), 7, 13,
                one("minecraft:redstone"), one("minecraft:repeater"), one("minecraft:comparator"),
                e("minecraft:redstone_block", 2, 1, 1), one("minecraft:dispenser"),
                one("minecraft:sticky_piston"), one("minecraft:piston"), one("minecraft:lever"),
                one("minecraft:light_weighted_pressure_plate"), one("minecraft:iron_ingot"), empty(1));

        registeredEntityTable(helper, "beaver");
        assertPool(helper, "beaver", pool(lootJson(helper, "entities", "beaver"), 0), 1, 1,
                core02("minecraft:porkchop"));

        registeredEntityTable(helper, "cave_fisher");
        assertPool(helper, "cave_fisher d6", pool(lootJson(helper, "entities", "cave_fisher"), 0), 1, 1,
                core02("minecraft:gold_nugget"), core02(NS + ":uranium_nugget"),
                core02(NS + ":titanium_nugget"), empty(3));

        registeredEntityTable(helper, "cliff_racer");
        assertPool(helper, "cliff_racer d8", pool(lootJson(helper, "entities", "cliff_racer"), 0), 1, 1,
                core02("minecraft:chicken"), core02(NS + ":uranium_nugget"),
                core02(NS + ":titanium_nugget"), empty(5));

        registeredEntityTable(helper, "cloud_shark");
        assertPool(helper, "cloud_shark d3", pool(lootJson(helper, "entities", "cloud_shark"), 0), 1, 1,
                core02("minecraft:paper"), core02("minecraft:string"), core02("minecraft:bone"));

        registeredEntityTable(helper, "cryolophosaurus");
        assertPool(helper, "cryolophosaurus d10", pool(lootJson(helper, "entities", "cryolophosaurus"), 0), 1, 1,
                core02("minecraft:chicken"), core02(NS + ":uranium_nugget"),
                core02(NS + ":titanium_nugget"), empty(7));

        registeredEntityTable(helper, "creeping_horror");
        assertPool(helper, "creeping_horror d3", pool(lootJson(helper, "entities", "creeping_horror"), 0), 1, 1,
                core02("minecraft:rotten_flesh"), core02("minecraft:bone"), core02("minecraft:string"));

        // Coin: single-roll 10-slot jackpot incl. the D4-restored coin_spawn_egg (ENT-A-098).
        registeredEntityTable(helper, "coin");
        assertPool(helper, "coin d10 jackpot", pool(lootJson(helper, "entities", "coin"), 0), 1, 1,
                one("minecraft:diamond"), one(NS + ":uranium_nugget"), one(NS + ":titanium_nugget"),
                one("minecraft:emerald"), one(NS + ":emerald_axe"), one(NS + ":emerald_shovel"),
                one(NS + ":emerald_pickaxe"), one(NS + ":emerald_hoe"), one(NS + ":coin_spawn_egg"),
                one(NS + ":emerald_sword"));

        // Camarasaurus: tamed-only branch, proven with live rolls bound to real entity NBT.
        registeredEntityTable(helper, "camarasaurus");
        JsonObject cama = lootJson(helper, "entities", "camarasaurus");
        helper.assertTrue(pools(cama).size() == 1, "camarasaurus: single tamed-only pool documented");
        assertTamedGate(helper, "camarasaurus", pool(cama, 0), false);
        assertPool(helper, "camarasaurus tamed", pool(cama, 0), 1, 1, e("minecraft:poppy", 1, 2, 6));

        LivingEntity cam = (LivingEntity) entityType("camarasaurus").create(helper.getLevel());
        helper.assertTrue(cam != null, "camarasaurus create failed");
        cam.moveTo(helper.absoluteVec(new Vec3(1, 1, 1)));
        for (int i = 0; i < 20; i++) {
            helper.assertTrue(rollEntityTable(helper, cam, true, player).isEmpty(),
                    "UNTAMED camarasaurus rolled drops — documented: nothing (ENT-A-070)");
        }
        ((TamableAnimal) cam).setTame(true, false);
        for (int i = 0; i < 20; i++) {
            List<ItemStack> roll = rollEntityTable(helper, cam, true, player);
            int poppies = countOf(roll, Items.POPPY);
            helper.assertTrue(poppies >= 2 && poppies <= 6 && roll.size() == 1,
                    "tamed camarasaurus rolled " + roll + ", documented 2-6 poppies only");
        }
        helper.succeed();
    }

    // ==================================================================
    // i056-c2-loot — C2 drop spot-checks
    // ==================================================================

    /**
     * Checklist i056-c2-loot — DungeonBeast / Fairy / Firefly / GammaMetroid /
     * Ghost / GhostSkelly / GiantRobot tables.
     *
     * <p>Documented values (phase_c_reports/C2_entities_D_I.md:30-35):
     * ENT-D-008 orig DungeonBeast.java:145-157 — nextInt(4) pick of
     * crystal_pink_ingot / crystal_apple / oak log / nothing (25% each), core
     * 0-2; ENT-D-029 orig Fairy.java:168-170 — crystal torch 0-2; ENT-D-031
     * orig Firefly.java:91-93 — extreme torch 0-2; ENT-D-035 orig
     * GammaMetroid.java:223-233 — gold nuggets 5-14 + iron ingots 6-15;
     * ENT-D-038/040 — Ghost/GhostSkelly have NO drop override → nothing
     * (empty pools); ENT-D-045 orig GiantRobot.java:158-211 — 15-29 drops of
     * laser ball ×4 (= 60-116 laser balls) plus 10-19 rolls of nextInt(12):
     * SpiderRobotKit / AntRobotKit / RayGun / redstone_block / dispenser /
     * sticky_piston / piston / lever / iron_block / detector_rail, 2 dead
     * slots.</p>
     */
    @GameTest(template = "empty")
    public void i056_c2_drop_tables(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);

        registeredEntityTable(helper, "dungeon_beast");
        assertPool(helper, "dungeon_beast 25% slots", pool(lootJson(helper, "entities", "dungeon_beast"), 0), 1, 1,
                core02(NS + ":crystal_pink_ingot"), core02(NS + ":crystal_apple"),
                core02("minecraft:oak_log"), empty(1));

        registeredEntityTable(helper, "fairy");
        assertPool(helper, "fairy", pool(lootJson(helper, "entities", "fairy"), 0), 1, 1,
                core02(NS + ":crystal_torch"));

        registeredEntityTable(helper, "firefly");
        assertPool(helper, "firefly", pool(lootJson(helper, "entities", "firefly"), 0), 1, 1,
                core02(NS + ":extreme_torch"));

        registeredEntityTable(helper, "gamma_metroid");
        JsonObject gamma = lootJson(helper, "entities", "gamma_metroid");
        assertPool(helper, "gamma_metroid gold", pool(gamma, 0), 1, 1, e("minecraft:gold_nugget", 1, 5, 14));
        assertPool(helper, "gamma_metroid iron", pool(gamma, 1), 1, 1, e("minecraft:iron_ingot", 1, 6, 15));

        // Ghost / GhostSkelly: NOTHING — empty pools, and live rolls stay empty.
        for (String ghost : new String[]{"ghost", "ghost_skelly"}) {
            registeredEntityTable(helper, ghost);
            helper.assertTrue(pools(lootJson(helper, "entities", ghost)).size() == 0,
                    ghost + ": documented drop is NOTHING (ENT-D-038/040) — pools must be empty");
            LivingEntity mob = (LivingEntity) entityType(ghost).create(helper.getLevel());
            helper.assertTrue(mob != null, ghost + " create failed");
            mob.moveTo(helper.absoluteVec(new Vec3(1, 1, 1)));
            for (int i = 0; i < 5; i++) {
                helper.assertTrue(rollEntityTable(helper, mob, true, player).isEmpty(),
                        ghost + " rolled items despite the documented empty table");
            }
        }

        registeredEntityTable(helper, "giant_robot");
        JsonObject robot = lootJson(helper, "entities", "giant_robot");
        assertPool(helper, "giant_robot laser balls (60-116 total)", pool(robot, 0), 15, 29,
                e(NS + ":laser_ball", 1, 4, 4));
        assertPool(helper, "giant_robot kit/component d12", pool(robot, 1), 10, 19,
                one(NS + ":spider_robot_kit"), one(NS + ":ant_robot_kit"), one(NS + ":ray_gun"),
                one("minecraft:redstone_block"), one("minecraft:dispenser"), one("minecraft:sticky_piston"),
                one("minecraft:piston"), one("minecraft:lever"), one("minecraft:iron_block"),
                one("minecraft:detector_rail"), empty(2));
        helper.succeed();
    }

    // ==================================================================
    // i057-c3-loot — C3 drop spot-checks
    // ==================================================================

    /** The shared d15 redstone-part table of Robot2-5 / SpiderRobot (orig Robot2.java:165-221 etc.). */
    private static PoolEntryExp[] d15RedstoneTable() {
        return new PoolEntryExp[]{
                one("minecraft:redstone"), one("minecraft:repeater"), one("minecraft:comparator"),
                e("minecraft:redstone_block", 2, 1, 1), one("minecraft:dispenser"),
                one("minecraft:sticky_piston"), one("minecraft:piston"), one("minecraft:lever"),
                one("minecraft:light_weighted_pressure_plate"), empty(5),
        };
    }

    /**
     * Checklist i057-c3-loot — LeafMonster / LurkingTerror / Rat / Robot2 /
     * Robot3 / Robot4 / Robot5 / Rotator tables.
     *
     * <p>Documented values (phase_c_reports/C3_entities_K_R.md:34-41):
     * ENT-K-014 orig LeafMonster.java:144-153 — one of oak log / oak leaves /
     * rotten flesh, core 0-2; ENT-K-027 orig LurkingTerror.java:368-377 —
     * one of beef / flint / feather; ENT-K-059 orig Rat.java:140-142 —
     * rotten flesh 0-2; ENT-K-064 orig Robot2.java:165-221 — iron_block 2-9
     * + iron_ingot 5-10 + d15 table 5-14 rolls (redstone_block w2, empty w5);
     * ENT-K-066 orig Robot3.java:166-219 — laser ball ×4 rolls 5-10 (= 20-40)
     * + d15 rolls 5-14; ENT-K-071 orig Robot4.java:195-250 — laser ball ×4
     * rolls 5-14 (= 20-56) + RayGun ×1 + painting ×1 + d15 rolls 10-24;
     * ENT-K-073 orig Robot5.java:138-190 — laser ball ×4 rolls 5-10 + d15
     * rolls 2-6; ENT-K-081 orig Rotator.java:385-400 — ONE of
     * crystal_pink_ingot / tigers_eye_ingot / crystal_coal / iron_ingot,
     * core 0-2.</p>
     */
    @GameTest(template = "empty")
    public void i057_c3_drop_tables(GameTestHelper helper) {
        registeredEntityTable(helper, "leaf_monster");
        assertPool(helper, "leaf_monster one-of", pool(lootJson(helper, "entities", "leaf_monster"), 0), 1, 1,
                core02("minecraft:oak_log"), core02("minecraft:oak_leaves"), core02("minecraft:rotten_flesh"));

        registeredEntityTable(helper, "lurking_terror");
        assertPool(helper, "lurking_terror one-of", pool(lootJson(helper, "entities", "lurking_terror"), 0), 1, 1,
                core02("minecraft:beef"), core02("minecraft:flint"), core02("minecraft:feather"));

        registeredEntityTable(helper, "rat");
        assertPool(helper, "rat", pool(lootJson(helper, "entities", "rat"), 0), 1, 1,
                core02("minecraft:rotten_flesh"));

        registeredEntityTable(helper, "robot_2");
        JsonObject robot2 = lootJson(helper, "entities", "robot_2");
        assertPool(helper, "robot_2 iron blocks", pool(robot2, 0), 2, 9, one("minecraft:iron_block"));
        assertPool(helper, "robot_2 iron ingots", pool(robot2, 1), 5, 10, one("minecraft:iron_ingot"));
        assertPool(helper, "robot_2 d15", pool(robot2, 2), 5, 14, d15RedstoneTable());

        registeredEntityTable(helper, "robot_3");
        JsonObject robot3 = lootJson(helper, "entities", "robot_3");
        assertPool(helper, "robot_3 laser balls (20-40 total)", pool(robot3, 0), 5, 10,
                e(NS + ":laser_ball", 1, 4, 4));
        assertPool(helper, "robot_3 d15", pool(robot3, 1), 5, 14, d15RedstoneTable());

        registeredEntityTable(helper, "robot_4");
        JsonObject robot4 = lootJson(helper, "entities", "robot_4");
        assertPool(helper, "robot_4 laser balls (20-56 total)", pool(robot4, 0), 5, 14,
                e(NS + ":laser_ball", 1, 4, 4));
        assertPool(helper, "robot_4 ray gun", pool(robot4, 1), 1, 1, one(NS + ":ray_gun"));
        assertPool(helper, "robot_4 painting", pool(robot4, 2), 1, 1, one("minecraft:painting"));
        assertPool(helper, "robot_4 d15", pool(robot4, 3), 10, 24, d15RedstoneTable());

        registeredEntityTable(helper, "robot_5");
        JsonObject robot5 = lootJson(helper, "entities", "robot_5");
        assertPool(helper, "robot_5 laser balls (20-40 total)", pool(robot5, 0), 5, 10,
                e(NS + ":laser_ball", 1, 4, 4));
        assertPool(helper, "robot_5 d15", pool(robot5, 1), 2, 6, d15RedstoneTable());

        registeredEntityTable(helper, "rotator");
        assertPool(helper, "rotator one-of-4", pool(lootJson(helper, "entities", "rotator"), 0), 1, 1,
                core02(NS + ":crystal_pink_ingot"), core02(NS + ":tigers_eye_ingot"),
                core02(NS + ":crystal_coal"), core02("minecraft:iron_ingot"));
        helper.succeed();
    }

    // ==================================================================
    // i058-c4-loot — C4 drop spot-checks
    // ==================================================================

    /**
     * Checklist i058-c4-loot — Scorpion / Skate / SpiderRobot / SpitBug /
     * tamed Spyro / TerribleTerror / tamed VelocityRaptor / Vortex / WormSmall.
     *
     * <p>Documented values (phase_c_reports/C4_entities_S_Z.md:38-46):
     * ENT-S-004 orig Scorpion.java:148-160 — d10: gold/uranium/titanium
     * nugget (~10% each) else nothing, core 0-2; ENT-S-014 orig
     * Skate.java:122-124 — string 0-2; ENT-S-023 orig
     * SpiderRobot.java:1079-1126 — d15 table, 14-27 rolls; ENT-S-026 orig
     * SpitBug.java:208-213 — amethyst gem 1-3; ENT-S-028 orig
     * Spyro.java:378-387 — TAMED → beef 1-4, untamed nothing; ENT-S-039 orig
     * TerribleTerror.java:313-322 — one of rotten flesh / emerald / feather;
     * ENT-S-066 orig VelocityRaptor.java:329-338 — TAMED → poppy 2-6,
     * untamed nothing; ENT-S-070 orig Vortex.java:369-399 — vortex eye ×1 +
     * painting ×1 + 5-11 rolls of d10 mixed parts (2 dead slots); ENT-S-079
     * orig WormSmall.java:230-232 — nothing.</p>
     */
    @GameTest(template = "empty")
    public void i058_c4_drop_tables(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);

        registeredEntityTable(helper, "scorpion");
        assertPool(helper, "scorpion d10 (~10% each)", pool(lootJson(helper, "entities", "scorpion"), 0), 1, 1,
                core02("minecraft:gold_nugget"), core02(NS + ":uranium_nugget"),
                core02(NS + ":titanium_nugget"), empty(7));

        registeredEntityTable(helper, "skate");
        assertPool(helper, "skate", pool(lootJson(helper, "entities", "skate"), 0), 1, 1,
                core02("minecraft:string"));

        registeredEntityTable(helper, "spider_robot");
        assertPool(helper, "spider_robot d15 (14-27 rolls)",
                pool(lootJson(helper, "entities", "spider_robot"), 0), 14, 27, d15RedstoneTable());

        registeredEntityTable(helper, "spit_bug");
        assertPool(helper, "spit_bug", pool(lootJson(helper, "entities", "spit_bug"), 0), 1, 1,
                e(NS + ":amethyst_gem", 1, 1, 3));

        registeredEntityTable(helper, "terrible_terror");
        assertPool(helper, "terrible_terror one-of", pool(lootJson(helper, "entities", "terrible_terror"), 0), 1, 1,
                core02("minecraft:rotten_flesh"), core02("minecraft:emerald"), core02("minecraft:feather"));

        registeredEntityTable(helper, "vortex");
        JsonObject vortex = lootJson(helper, "entities", "vortex");
        assertPool(helper, "vortex eye", pool(vortex, 0), 1, 1, one(NS + ":vortex_eye"));
        assertPool(helper, "vortex painting", pool(vortex, 1), 1, 1, one("minecraft:painting"));
        assertPool(helper, "vortex d10 mixed (5-11 rolls)", pool(vortex, 2), 5, 11,
                one("minecraft:stick"), one(NS + ":tigers_eye_ingot"), one(NS + ":crystal_pink_ingot"),
                one("minecraft:iron_ingot"), one(NS + ":uranium_nugget"), one(NS + ":titanium_nugget"),
                one(NS + ":dead_irukandji"), one(NS + ":crystal_coal"), empty(2));

        // WormSmall: nothing — empty pools + live rolls.
        registeredEntityTable(helper, "worm_small");
        helper.assertTrue(pools(lootJson(helper, "entities", "worm_small")).size() == 0,
                "worm_small: documented drop is NOTHING (ENT-S-079)");
        LivingEntity worm = (LivingEntity) entityType("worm_small").create(helper.getLevel());
        helper.assertTrue(worm != null, "worm_small create failed");
        worm.moveTo(helper.absoluteVec(new Vec3(1, 1, 1)));
        for (int i = 0; i < 5; i++) {
            helper.assertTrue(rollEntityTable(helper, worm, true, player).isEmpty(),
                    "worm_small rolled items despite the documented empty table");
        }

        // Spyro / VelocityRaptor tamed conditionals via live rolls on real entity state.
        String[][] tamedOnly = {
                {"spyro", "minecraft:beef", "1", "4"},
                {"velocity_raptor", "minecraft:poppy", "2", "6"},
        };
        for (String[] row : tamedOnly) {
            registeredEntityTable(helper, row[0]);
            JsonObject json = lootJson(helper, "entities", row[0]);
            helper.assertTrue(pools(json).size() == 1, row[0] + ": single tamed-only pool documented");
            assertTamedGate(helper, row[0], pool(json, 0), false);
            assertPool(helper, row[0] + " tamed", pool(json, 0), 1, 1,
                    e(row[1], 1, Integer.parseInt(row[2]), Integer.parseInt(row[3])));

            LivingEntity mob = (LivingEntity) entityType(row[0]).create(helper.getLevel());
            helper.assertTrue(mob != null, row[0] + " create failed");
            mob.moveTo(helper.absoluteVec(new Vec3(1, 1, 1)));
            for (int i = 0; i < 20; i++) {
                helper.assertTrue(rollEntityTable(helper, mob, true, player).isEmpty(),
                        "UNTAMED " + row[0] + " rolled drops — documented: nothing");
            }
            ((TamableAnimal) mob).setTame(true, false);
            Item drop = item(row[1]);
            int min = Integer.parseInt(row[2]);
            int max = Integer.parseInt(row[3]);
            for (int i = 0; i < 20; i++) {
                List<ItemStack> roll = rollEntityTable(helper, mob, true, player);
                int count = countOf(roll, drop);
                helper.assertTrue(count >= min && count <= max && roll.size() == 1,
                        "tamed " + row[0] + " rolled " + roll + ", documented " + min + "-" + max
                                + "x " + row[1] + " only");
            }
        }
        helper.succeed();
    }

    // ==================================================================
    // i059-d4-drops — D4 small-pet tables + Stinky kills
    // ==================================================================

    /**
     * Checklist i059-d4-drops (pets half) — "Chipmunk (orig table + poppy only
     * when tamed), GoldFish, RubberDucky, Stinky (beef only when tamed)".
     *
     * <p>Documented values (phase_d_reports/D4_items_small_entities.md §8 +
     * JSON orig citations): orig Chipmunk.java:227-242 — tamed exactly 2-6
     * poppies (no looting), untamed wheat 0-2; orig GoldFish func_146068_u —
     * equal 1/3 pick of gold BLOCK / uranium nugget / titanium nugget, core
     * 0-2; orig RubberDucky func_146068_u — 50% feather / 25% RubberDuckyEgg
     * / 25% nothing, core 0-2; orig Stinky.java:257-266 — tamed exactly 1-4
     * raw beef, untamed NOTHING (full override). Chipmunk conditional proven
     * by live rolls on real entity NBT; Stinky proven by real controlled
     * kills (the checklist's kill phrasing).</p>
     */
    @GameTest(template = "empty_large")
    public void i059_d4_pet_drop_tables_and_stinky_kills(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);

        registeredEntityTable(helper, "chipmunk");
        JsonObject chipmunk = lootJson(helper, "entities", "chipmunk");
        assertTamedGate(helper, "chipmunk tamed pool", pool(chipmunk, 0), false);
        assertPool(helper, "chipmunk tamed pool", pool(chipmunk, 0), 1, 1, e("minecraft:poppy", 1, 2, 6));
        assertTamedGate(helper, "chipmunk untamed pool", pool(chipmunk, 1), true);
        assertPool(helper, "chipmunk untamed pool", pool(chipmunk, 1), 1, 1, core02("minecraft:wheat"));

        LivingEntity chip = (LivingEntity) entityType("chipmunk").create(helper.getLevel());
        helper.assertTrue(chip != null, "chipmunk create failed");
        chip.moveTo(helper.absoluteVec(new Vec3(24, 2, 24)));
        for (int i = 0; i < 20; i++) {
            List<ItemStack> roll = rollEntityTable(helper, chip, true, player);
            helper.assertTrue(countOf(roll, Items.POPPY) == 0 && countOf(roll, Items.WHEAT) <= 2,
                    "untamed chipmunk rolled " + roll + ", documented wheat 0-2 only");
        }
        ((TamableAnimal) chip).setTame(true, false);
        for (int i = 0; i < 20; i++) {
            List<ItemStack> roll = rollEntityTable(helper, chip, true, player);
            int poppies = countOf(roll, Items.POPPY);
            helper.assertTrue(poppies >= 2 && poppies <= 6 && countOf(roll, Items.WHEAT) == 0,
                    "tamed chipmunk rolled " + roll + ", documented 2-6 poppies only");
        }

        registeredEntityTable(helper, "gold_fish");
        assertPool(helper, "gold_fish 1/3 pick", pool(lootJson(helper, "entities", "gold_fish"), 0), 1, 1,
                core02("minecraft:gold_block"), core02(NS + ":uranium_nugget"),
                core02(NS + ":titanium_nugget"));

        registeredEntityTable(helper, "rubber_ducky");
        assertPool(helper, "rubber_ducky 50/25/25", pool(lootJson(helper, "entities", "rubber_ducky"), 0), 1, 1,
                e("minecraft:feather", 2, 0, 2), core02(NS + ":rubber_ducky_spawn_egg"), empty(1));

        // Stinky: real controlled kills — tamed 1-4 beef, untamed NOTHING.
        registeredEntityTable(helper, "stinky");
        JsonObject stinky = lootJson(helper, "entities", "stinky");
        helper.assertTrue(pools(stinky).size() == 1, "stinky: single tamed-only pool documented");
        assertTamedGate(helper, "stinky", pool(stinky, 0), false);
        assertPool(helper, "stinky tamed", pool(stinky, 0), 1, 1, e("minecraft:beef", 1, 1, 4));

        BlockPos relSpot = new BlockPos(24, 2, 24);
        AABB area = box(helper.absoluteVec(new Vec3(24, 2, 24)), 6, 6);
        for (int i = 0; i < 6; i++) {
            clearDropsAndXp(helper, area);
            LivingEntity tamed = spawnLiving(helper, "stinky", relSpot);
            ((TamableAnimal) tamed).setTame(true, false);
            killByPlayer(helper, tamed, player);
            Map<Item, Integer> drops = collectDrops(helper, area);
            int beef = drops.getOrDefault(Items.BEEF, 0);
            helper.assertTrue(beef >= 1 && beef <= 4 && totalCount(drops) == beef,
                    "tamed stinky dropped " + drops + ", documented exactly 1-4 beef");
            tamed.discard();
        }
        for (int i = 0; i < 10; i++) {
            clearDropsAndXp(helper, area);
            LivingEntity wild = spawnLiving(helper, "stinky", relSpot);
            killByPlayer(helper, wild, player);
            Map<Item, Integer> drops = collectDrops(helper, area);
            helper.assertTrue(drops.isEmpty(),
                    "untamed stinky dropped " + drops + ", documented NOTHING (orig Stinky.java:257-266)");
            wild.discard();
        }
        clearDropsAndXp(helper, area);
        helper.succeed();
    }

    /**
     * Checklist i059-d4-drops (Lavafoam half) — "Lavafoam: break in overworld
     * (0 XP) vs Nether (5-13 XP)".
     *
     * <p>Documented values: orig Lavafoam.java:110-116 — dimension -1 grants
     * {@code 5 + nextInt(5) + nextInt(5)} = 5..13 XP, overworld nothing; fix
     * record FIX_LOG.md:925-926 ("Lavafoam Nether XP bonus ... via
     * getExpDrop"); port block/Lavafoam.java:36-45. Unlike the OreGenericEgg
     * family this block uses the NeoForge {@code getExpDrop} hook, so BOTH
     * the mechanism sampling and the real break path are asserted.</p>
     *
     * <p>K-region note: consumes K=300 → fixed Nether probe at
     * (300*512, 100, 50000) = (153600, 100, 50000), inside the world border
     * and far from every test grid; ServerLevel.setBlock force-loads the
     * chunk synchronously. Statistics: 200 Nether getExpDrop samples all in
     * [5,13] with ≥3 distinct values (P(≤2 distinct) ≤ C(9,2)·(9/25)^200,
     * astronomically below 1e-9).</p>
     */
    @GameTest(template = "empty")
    public void i059_lavafoam_xp_overworld_vs_nether(GameTestHelper helper) {
        ServerLevel overworld = helper.getLevel();
        ServerLevel nether = overworld.getServer().getLevel(Level.NETHER);
        helper.assertTrue(nether != null, "gametest server must load the Nether for the Lavafoam split");

        Block lavafoam = block(NS + ":lavafoam");
        BlockState state = lavafoam.defaultBlockState();
        Player breaker = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos owPos = helper.absolutePos(new BlockPos(1, 1, 1));

        // Mechanism: getExpDrop per dimension.
        for (int i = 0; i < 50; i++) {
            int xp = lavafoam.getExpDrop(state, overworld, owPos, null, breaker, ItemStack.EMPTY);
            helper.assertTrue(xp == 0, "overworld lavafoam getExpDrop returned " + xp + ", documented 0");
        }
        BlockPos netherPos = new BlockPos(153600, 100, 50000); // K=300 probe
        Set<Integer> seen = new HashSet<>();
        for (int i = 0; i < 200; i++) {
            int xp = lavafoam.getExpDrop(state, nether, netherPos, null, breaker, ItemStack.EMPTY);
            helper.assertTrue(xp >= 5 && xp <= 13,
                    "nether lavafoam getExpDrop " + xp + " outside 5..13 (orig Lavafoam.java:110-116)");
            seen.add(xp);
        }
        helper.assertTrue(seen.size() >= 3, "nether XP roll not randomized: values " + seen);

        // End-to-end: real breaks in both dimensions (documented /setblock procedure).
        AABB owArea = box(Vec3.atCenterOf(owPos), 4, 6);
        clearDropsAndXp(helper, owArea);
        overworld.setBlock(owPos, state, 3);
        overworld.destroyBlock(owPos, true, breaker);
        helper.assertTrue(totalXp(overworld, owArea) == 0, "overworld lavafoam break must drop 0 XP");
        Map<Item, Integer> owDrops = collectDrops(helper, owArea);
        helper.assertTrue(owDrops.getOrDefault(item(NS + ":lavafoam"), 0) == 1,
                "overworld lavafoam break must drop itself, got " + owDrops);
        clearDropsAndXp(helper, owArea);

        AABB netherArea = box(Vec3.atCenterOf(netherPos), 4, 6);
        nether.getEntitiesOfClass(ExperienceOrb.class, netherArea).forEach(Entity::discard);
        nether.setBlock(netherPos, state, 3);
        nether.destroyBlock(netherPos, true, breaker);
        int netherXp = totalXp(nether, netherArea);
        helper.assertTrue(netherXp >= 5 && netherXp <= 13,
                "nether lavafoam break dropped " + netherXp + " XP, documented 5-13");
        nether.getEntitiesOfClass(ExperienceOrb.class, netherArea).forEach(Entity::discard);
        nether.getEntitiesOfClass(ItemEntity.class, netherArea).forEach(Entity::discard);
        helper.succeed();
    }

    // ==================================================================
    // i060-boss-025-035-042 — royal family drops
    // ==================================================================

    /**
     * Checklist i060-boss-025-035-042 — "ThePrince (1-4 beef), ThePrincess
     * (1-4 beef), ThePrinceAdult (1 prince egg) — no diamonds/gold."
     *
     * <p>Documented values: BOSS-025 orig ThePrince.java:354-361
     * (nextInt(4)+1 beef) and BOSS-042 orig ThePrincess.java:342-349 —
     * phase_c_reports/C5_bosses.md:23/:30, FIX_LOG.md:104 (the pre-fix
     * tables dropped 1-4 DIAMOND, hence the explicit negative). BOSS-035
     * orig ThePrinceAdult.java:313-315 — PrinceEgg ×1 (C5:28); since D5 the
     * functional egg item is {@code orespawn:the_prince_spawn_egg}
     * (ITEM-066, phase_d_reports/D5_structures_spawnores.md:107 — drops at
     * ThePrinceAdult.java:314). Real controlled player kills; every bound
     * deterministic per kill.</p>
     */
    @GameTest(template = "empty_large")
    public void i060_prince_family_drops(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos relSpot = new BlockPos(24, 2, 24);
        AABB area = box(helper.absoluteVec(new Vec3(24, 2, 24)), 8, 10);

        // Structure: beef 1-4 / single egg, which also proves no diamond/gold entries exist.
        registeredEntityTable(helper, "the_prince");
        assertPool(helper, "the_prince", pool(lootJson(helper, "entities", "the_prince"), 0), 1, 1,
                e("minecraft:beef", 1, 1, 4));
        registeredEntityTable(helper, "the_princess");
        assertPool(helper, "the_princess", pool(lootJson(helper, "entities", "the_princess"), 0), 1, 1,
                e("minecraft:beef", 1, 1, 4));
        registeredEntityTable(helper, "the_prince_adult");
        assertPool(helper, "the_prince_adult", pool(lootJson(helper, "entities", "the_prince_adult"), 0), 1, 1,
                one(NS + ":the_prince_spawn_egg"));

        for (String path : new String[]{"the_prince", "the_princess"}) {
            for (int i = 0; i < 3; i++) {
                clearDropsAndXp(helper, area);
                LivingEntity royal = spawnLiving(helper, path, relSpot);
                killByPlayer(helper, royal, player);
                Map<Item, Integer> drops = collectDrops(helper, area);
                int beef = drops.getOrDefault(Items.BEEF, 0);
                helper.assertTrue(beef >= 1 && beef <= 4, path + " dropped " + beef + " beef, documented 1-4");
                helper.assertTrue(!drops.containsKey(Items.DIAMOND) && !drops.containsKey(Items.GOLD_INGOT),
                        path + " dropped diamonds/gold: " + drops + " (BOSS-025/042 regression)");
                helper.assertTrue(totalCount(drops) == beef, path + " dropped extras: " + drops);
                royal.discard();
            }
        }
        for (int i = 0; i < 3; i++) {
            clearDropsAndXp(helper, area);
            LivingEntity adult = spawnLiving(helper, "the_prince_adult", relSpot);
            killByPlayer(helper, adult, player);
            Map<Item, Integer> drops = collectDrops(helper, area);
            helper.assertTrue(drops.getOrDefault(item(NS + ":the_prince_spawn_egg"), 0) == 1
                            && totalCount(drops) == 1,
                    "the_prince_adult dropped " + drops
                            + ", documented exactly 1 prince egg (BOSS-035/ITEM-066)");
            helper.assertTrue(!drops.containsKey(Items.DIAMOND) && !drops.containsKey(Items.GOLD_INGOT),
                    "the_prince_adult dropped diamonds/gold: " + drops);
            adult.discard();
        }
        clearDropsAndXp(helper, area);
        helper.succeed();
    }

    // ==================================================================
    // i105-b1-boss-drops-survival-kills — the five loot-table bosses
    // ==================================================================

    /**
     * Checklist i105-b1-boss-drops-survival-kills (all but TheKing — see
     * {@link #i105_boss_drops_the_king_registry_shower}) — real
     * player-attributed kills of Kraken, Godzilla, TheQueen, Mothra and
     * Dragon, counting every ItemEntity the death produces.
     *
     * <p>Documented values (phase_b_reports/B1_drops.md bosses table):
     * Kraken (ENT-K-003, orig Kraken.java:236-871) — kraken tooth ×1,
     * painting ×1, ink sac 120-279 (the pre-fix bug dropped COOKED COD →
     * hard negative), 5-14 rolls of the d53 gear table (53 weight-1 slots,
     * no dead slot); Godzilla (BOSS-015, orig Godzilla.java:820-1775) —
     * painting ×1, godzilla scale 50-79, beef 100-259, bone 50-109, 25-39
     * rolls of the d80 gear table (weight sum 80 with a weight-5 empty; NO
     * emeralds — the pre-fix bug swapped beef→emeralds); TheQueen (BOSS-011,
     * orig TheQueen.java:190-200) — royal guardian sword ×1, prince egg ×1,
     * exactly 56× each of queen scale / beef / bone / rotten flesh, plus The
     * Princess spawned from die() (orig TheQueen.java:193); Mothra
     * (ENT-K-040, orig Mothra.java:341-363) — painting ×1, 53 gold nuggets,
     * 25 moth scales, 3 blaze rods, 1 nether star, plus the 20-Luna-Moth
     * burst from die() (orig Mothra.java:344-362); Dragon (B1 handoff row,
     * orig Dragon.java:342-347) — beef 1-6 and NOTHING else (no bones).
     * "Nothing drops twice" is enforced via the exact-count asserts (a
     * double-drop would double the fixed counts).</p>
     *
     * <p>Statistics: the only non-deterministic bound is Godzilla's gear
     * count lower bound — with ≥25 rolls at 75/80 hit chance,
     * P(hits &lt; 10) &lt; 1e-13; asserted band [10, 39].</p>
     */
    @GameTest(template = "empty_large", timeoutTicks = 200)
    public void i105_boss_drops_kraken_godzilla_queen_mothra_dragon(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos relSpot = new BlockPos(24, 2, 24);
        Vec3 center = helper.absoluteVec(new Vec3(24, 2, 24));
        AABB area = box(center, 16, 30);

        // Structural gear-pool shape (B1 d53 / d80 documentation).
        JsonObject kraken = lootJson(helper, "entities", "kraken");
        assertPool(helper, "kraken ink", pool(kraken, 2), 1, 1, e("minecraft:ink_sac", 1, 120, 279));
        JsonObject krakenGear = pool(kraken, 3);
        int[] krakenRolls = rollsRange(krakenGear);
        List<String> krakenKeys = actualEntryKeys(krakenGear);
        helper.assertTrue(krakenRolls[0] == 5 && krakenRolls[1] == 14 && krakenKeys.size() == 53,
                "kraken gear pool must be 5-14 rolls over the 53-slot d53, has "
                        + krakenKeys.size() + " slots, rolls " + krakenRolls[0] + ".." + krakenRolls[1]);
        helper.assertTrue(krakenKeys.stream().noneMatch(k -> k.startsWith("minecraft:cooked_cod|"))
                        && krakenKeys.stream().anyMatch(k -> k.startsWith(NS + ":ultimate_sword|")),
                "kraken d53 must hold ultimate gear and no cooked cod (ENT-K-003)");
        JsonObject godzilla = lootJson(helper, "entities", "godzilla");
        JsonObject godzillaGear = pool(godzilla, 4);
        int[] gzRolls = rollsRange(godzillaGear);
        int gzWeightSum = 0;
        int gzEmptyWeight = 0;
        boolean gzEmerald = false;
        for (String key : actualEntryKeys(godzillaGear)) {
            int weight = Integer.parseInt(key.split("\\|")[1].substring(1));
            gzWeightSum += weight;
            if (key.startsWith("EMPTY|")) {
                gzEmptyWeight += weight;
            }
            if (key.startsWith("minecraft:emerald|")) {
                gzEmerald = true;
            }
        }
        helper.assertTrue(gzRolls[0] == 25 && gzRolls[1] == 39 && gzWeightSum == 80 && gzEmptyWeight == 5
                        && !gzEmerald,
                "godzilla gear pool must be 25-39 rolls over the d80 (weight sum 80, empty weight 5,"
                        + " no emerald slot — BOSS-015); got rolls " + gzRolls[0] + ".." + gzRolls[1]
                        + ", weights " + gzWeightSum + ", empty " + gzEmptyWeight + ", emerald " + gzEmerald);

        // ---- Dragon ----
        clearDropsAndXp(helper, area);
        LivingEntity dragon = spawnLiving(helper, "dragon", relSpot);
        killByPlayer(helper, dragon, player);
        Map<Item, Integer> drops = collectDrops(helper, area);
        int dragonBeef = drops.getOrDefault(Items.BEEF, 0);
        helper.assertTrue(dragonBeef >= 1 && dragonBeef <= 6,
                "Dragon dropped " + dragonBeef + " beef, documented 1-6 (orig Dragon.java:342-347)");
        helper.assertTrue(!drops.containsKey(Items.BONE) && totalCount(drops) == dragonBeef,
                "Dragon must drop beef only (no bones), got " + drops);
        dragon.discard();

        // ---- Mothra ----
        clearDropsAndXp(helper, area);
        LivingEntity mothra = spawnLiving(helper, "mothra", relSpot);
        killByPlayer(helper, mothra, player);
        drops = collectDrops(helper, area);
        helper.assertTrue(drops.getOrDefault(Items.PAINTING, 0) == 1
                        && drops.getOrDefault(Items.GOLD_NUGGET, 0) == 53
                        && drops.getOrDefault(item(NS + ":moth_scale"), 0) == 25
                        && drops.getOrDefault(Items.BLAZE_ROD, 0) == 3
                        && drops.getOrDefault(Items.NETHER_STAR, 0) == 1
                        && totalCount(drops) == 83,
                "Mothra drops " + drops + " != documented painting/53 nuggets/25 scales/3 rods/1 star"
                        + " exactly once each (ENT-K-040)");
        List<? extends Entity> moths = helper.getEntities(ModEntities.ENTITY_LUNA_MOTH.get());
        helper.assertTrue(moths.size() == 20,
                "Mothra death released " + moths.size() + " Luna Moths, documented 20 (orig Mothra.java:344-362)");
        moths.forEach(Entity::discard);
        mothra.discard();

        // ---- Kraken ----
        clearDropsAndXp(helper, area);
        LivingEntity krakenBoss = spawnLiving(helper, "kraken", relSpot);
        killByPlayer(helper, krakenBoss, player);
        drops = collectDrops(helper, area);
        int ink = drops.getOrDefault(Items.INK_SAC, 0);
        helper.assertTrue(drops.getOrDefault(item(NS + ":kraken_tooth"), 0) == 1,
                "Kraken must drop exactly one kraken tooth, got " + drops);
        helper.assertTrue(drops.getOrDefault(Items.PAINTING, 0) == 1,
                "Kraken must drop exactly one painting, got " + drops);
        helper.assertTrue(ink >= 120 && ink <= 279,
                "Kraken dropped " + ink + " ink sacs, documented 120-279 (ENT-K-003)");
        helper.assertTrue(drops.getOrDefault(Items.COOKED_COD, 0) == 0,
                "Kraken dropped COOKED COD — the exact ENT-K-003 regression");
        int krakenGearCount = totalCount(drops) - ink - 2;
        helper.assertTrue(krakenGearCount >= 5 && krakenGearCount <= 14,
                "Kraken d53 gear items " + krakenGearCount + " outside the documented 5-14 rolls");
        krakenBoss.discard();

        // ---- Godzilla ----
        clearDropsAndXp(helper, area);
        LivingEntity godzillaBoss = spawnLiving(helper, "godzilla", relSpot);
        killByPlayer(helper, godzillaBoss, player);
        drops = collectDrops(helper, area);
        int scale = drops.getOrDefault(item(NS + ":godzilla_scale"), 0);
        int beef = drops.getOrDefault(Items.BEEF, 0);
        int bone = drops.getOrDefault(Items.BONE, 0);
        helper.assertTrue(drops.getOrDefault(Items.PAINTING, 0) == 1,
                "Godzilla must drop exactly one painting, got " + drops.getOrDefault(Items.PAINTING, 0));
        helper.assertTrue(scale >= 50 && scale <= 79, "Godzilla scales " + scale + " outside 50-79");
        helper.assertTrue(beef >= 100 && beef <= 259, "Godzilla beef " + beef + " outside 100-259");
        helper.assertTrue(bone >= 50 && bone <= 109, "Godzilla bones " + bone + " outside 50-109");
        helper.assertTrue(drops.getOrDefault(Items.EMERALD, 0) == 0,
                "Godzilla dropped EMERALDS — the exact BOSS-015 regression");
        int godzillaGearCount = totalCount(drops) - scale - beef - bone - 1;
        helper.assertTrue(godzillaGearCount >= 10 && godzillaGearCount <= 39,
                "Godzilla d80 gear items " + godzillaGearCount
                        + " outside [10,39] (25-39 rolls at 75/80 hit chance; P(<10) < 1e-13)");
        godzillaBoss.discard();

        // ---- TheQueen ----
        clearDropsAndXp(helper, area);
        LivingEntity queen = spawnLiving(helper, "the_queen", relSpot);
        killByPlayer(helper, queen, player);
        drops = collectDrops(helper, area);
        helper.assertTrue(drops.getOrDefault(item(NS + ":royal_guardian_sword"), 0) == 1
                        && drops.getOrDefault(item(NS + ":the_prince_spawn_egg"), 0) == 1
                        && drops.getOrDefault(item(NS + ":queen_scale"), 0) == 56
                        && drops.getOrDefault(Items.BEEF, 0) == 56
                        && drops.getOrDefault(Items.BONE, 0) == 56
                        && drops.getOrDefault(Items.ROTTEN_FLESH, 0) == 56
                        && totalCount(drops) == 2 + 4 * 56,
                "Queen drops " + drops + " != documented sword + egg + 56x scale/beef/bone/flesh"
                        + " exactly once (BOSS-011; doubles would break the exact counts)");
        List<ThePrincess> princesses = helper.getEntities(ModEntities.THE_PRINCESS.get());
        helper.assertTrue(princesses.size() == 1,
                "Queen death spawned " + princesses.size() + " Princesses, documented exactly 1"
                        + " (orig TheQueen.java:193)");
        princesses.forEach(Entity::discard);
        queen.discard();

        clearDropsAndXp(helper, area);
        helper.succeed();
    }

    /**
     * Checklist i105-b1-boss-drops-survival-kills (TheKing) — "TheKing (royal
     * set + 300 random registry items + Prince spawns)".
     *
     * <p>Documented values: BOSS-004 + B1_drops.md exception 1 — orig
     * TheKing.java:183-227: royal helmet / chestplate / leggings / boots +
     * royal guardian sword ×1 each (now the_king.json), PLUS 150 random
     * item-registry draws + 150 random block-registry draws scattered ±20
     * blocks at +12Y (retained code path, port TheKing.java:1318-1340), PLUS
     * The Prince spawned from die() (orig TheKing.java:187, port :1343-1353).
     * Assert: total dropped item count is EXACTLY 305 (300 draws of 1 + the
     * 5 royal pieces — a doubled royal set or doubled shower would break the
     * exact total, covering "nothing drops twice"), with at least one of
     * each royal piece present (the random draws may add more of any item,
     * so exact-one per piece is not assertable), and exactly one ThePrince
     * spawned. The royal-set loot JSON itself is asserted exactly.</p>
     */
    @GameTest(template = "empty_large", timeoutTicks = 200)
    public void i105_boss_drops_the_king_registry_shower(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);

        registeredEntityTable(helper, "the_king");
        JsonObject king = lootJson(helper, "entities", "the_king");
        helper.assertTrue(pools(king).size() == 5, "the_king.json must hold exactly the 5 royal pieces");
        assertPool(helper, "king chestplate", pool(king, 0), 1, 1, one(NS + ":royal_chestplate"));
        assertPool(helper, "king helmet", pool(king, 1), 1, 1, one(NS + ":royal_helmet"));
        assertPool(helper, "king leggings", pool(king, 2), 1, 1, one(NS + ":royal_leggings"));
        assertPool(helper, "king boots", pool(king, 3), 1, 1, one(NS + ":royal_boots"));
        assertPool(helper, "king sword", pool(king, 4), 1, 1, one(NS + ":royal_guardian_sword"));

        BlockPos relSpot = new BlockPos(24, 2, 24);
        Vec3 center = helper.absoluteVec(new Vec3(24, 2, 24));
        AABB area = box(center, 26, 45); // dropItemRand scatters +-20 blocks at +12Y
        clearDropsAndXp(helper, area);

        LivingEntity kingBoss = spawnLiving(helper, "the_king", relSpot);
        killByPlayer(helper, kingBoss, player);

        Map<Item, Integer> drops = collectDrops(helper, area);
        int total = totalCount(drops);
        helper.assertTrue(total == 305,
                "King death produced " + total + " items, documented exactly 305 = 5 royal pieces"
                        + " + 150 item draws + 150 block draws (orig TheKing.java:183-227)");
        for (String piece : new String[]{"royal_helmet", "royal_chestplate", "royal_leggings",
                "royal_boots", "royal_guardian_sword"}) {
            helper.assertTrue(drops.getOrDefault(item(NS + ":" + piece), 0) >= 1,
                    "King death missing " + piece + " (royal set, the_king.json)");
        }
        List<ThePrince> princes = helper.getEntities(ModEntities.THE_PRINCE.get());
        helper.assertTrue(princes.size() == 1,
                "King death spawned " + princes.size() + " Princes, documented exactly 1"
                        + " (orig TheKing.java:187)");
        princes.forEach(Entity::discard);
        kingBoss.discard();
        clearDropsAndXp(helper, area);
        helper.succeed();
    }

    // ==================================================================
    // i086-pools — Ultimate Rod fishing
    // ==================================================================

    /** Junk pool item set — orig UltimateFishHook.java:41, port :61-74. */
    private static Set<Item> junkPoolItems() {
        return Set.of(Items.LEATHER_BOOTS, Items.LEATHER, Items.BONE, Items.POTION, Items.STRING,
                Items.FISHING_ROD, Items.BOWL, Items.STICK, Items.INK_SAC, Items.TRIPWIRE_HOOK,
                Items.ROTTEN_FLESH);
    }

    /** Treasure pool — orig :42, port :77-83 (an enchanted BOOK becomes minecraft:enchanted_book). */
    private static Set<Item> treasurePoolItems() {
        return Set.of(Items.LILY_PAD, Items.NAME_TAG, Items.SADDLE, Items.BOW, Items.FISHING_ROD,
                Items.BOOK, Items.ENCHANTED_BOOK);
    }

    /** Vanilla + OreSpawn water fish pools — orig :43/:45, port :86-90/:101-106. */
    private static Set<Item> waterFishItems() {
        return Set.of(Items.COD, Items.SALMON, Items.TROPICAL_FISH, Items.PUFFERFISH,
                item(NS + ":blue_fish"), item(NS + ":pink_fish"), item(NS + ":rock_fish"),
                item(NS + ":wood_fish"), item(NS + ":grey_fish"));
    }

    /** Lava fish pool — orig :44, port :93-98. */
    private static Set<Item> lavaFishItems() {
        return Set.of(item(NS + ":sunspot_urchin"), item(NS + ":lava_eel"), item(NS + ":sun_fish"),
                item(NS + ":spark_fish"), item(NS + ":fire_fish"));
    }

    private static Set<Item> orespawnWaterFish() {
        return Set.of(item(NS + ":blue_fish"), item(NS + ":pink_fish"), item(NS + ":rock_fish"),
                item(NS + ":wood_fish"), item(NS + ":grey_fish"));
    }

    /** Builds a sealed 1×1 lava cell (glass shell) centred at the given relative pos. */
    private static void buildLavaCell(GameTestHelper helper, BlockPos relCenter) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    helper.setBlock(relCenter.offset(dx, dy, dz), Blocks.GLASS.defaultBlockState());
                }
            }
        }
        helper.setBlock(relCenter, Blocks.LAVA.defaultBlockState());
    }

    /**
     * One controlled catch: fresh hook at the given absolute spot, forced
     * bite ({@code nibble} is the access-transformed FishingHook bite
     * counter), then retrieve. Returns the caught stack after asserting the
     * catch mechanics (LavaLovingItem carrier, flight toward the player,
     * 1-6 XP at the player's feet).
     */
    private static ItemStack retrieveOnce(GameTestHelper helper, ServerPlayer angler, ItemStack rod,
            Vec3 hookSpot) {
        ServerLevel level = helper.getLevel();
        AABB hookArea = box(hookSpot, 3, 3);
        AABB playerArea = box(angler.position(), 2, 2);
        level.getEntitiesOfClass(ItemEntity.class, hookArea).forEach(Entity::discard);
        level.getEntitiesOfClass(ExperienceOrb.class, playerArea).forEach(Entity::discard);

        UltimateFishHook hook = new UltimateFishHook(angler, level, 0, 0);
        hook.setPos(hookSpot.x, hookSpot.y, hookSpot.z);
        level.addFreshEntity(hook);
        hook.nibble = 1; // AT'd private field — forces the "fish on" state
        int result = hook.retrieve(rod);
        helper.assertTrue(result == 1, "retrieve() returned " + result + ", expected 1 (caught item)");
        helper.assertTrue(hook.isRemoved(), "retrieve() must dismiss the hook");

        List<EntityLavaLovingItem> caught =
                level.getEntitiesOfClass(EntityLavaLovingItem.class, hookArea);
        helper.assertTrue(caught.size() == 1,
                "catch must arrive as exactly one fire-immune EntityLavaLovingItem (orig :409), got "
                        + caught.size());
        EntityLavaLovingItem carrier = caught.get(0);
        Vec3 toPlayer = angler.position().subtract(hookSpot);
        Vec3 motion = carrier.getDeltaMovement();
        helper.assertTrue(toPlayer.x * motion.x + toPlayer.z * motion.z > 0.0,
                "caught item does not fly toward the player (orig :400-410): motion " + motion);

        int xp = totalXp(level, playerArea);
        helper.assertTrue(xp >= 1 && xp <= 6,
                "catch XP at the player " + xp + " outside 1-6 (orig :411)");

        ItemStack stack = carrier.getItem().copy();
        carrier.discard();
        level.getEntitiesOfClass(ExperienceOrb.class, playerArea).forEach(Entity::discard);
        return stack;
    }

    private static ServerPlayer anglerAt(GameTestHelper helper, Vec3 absolutePos, ItemStack rod) {
        ServerPlayer angler = helper.makeMockServerPlayerInLevel();
        angler.teleportTo(helper.getLevel(), absolutePos.x, absolutePos.y, absolutePos.z, 0.0f, 0.0f);
        angler.setItemInHand(InteractionHand.MAIN_HAND, rod);
        return angler;
    }

    private static void removeAngler(GameTestHelper helper, ServerPlayer angler) {
        helper.getLevel().getServer().getPlayerList().remove(angler);
        angler.discard();
    }

    /**
     * Checklist i086-pools (pool membership + catch delivery) — "Fish ~10
     * catches (water and lava). Expect: junk / treasure / vanilla fish /
     * OreSpawn fish pools; lava adds lava-fish; caught items fly to you ...;
     * XP orb at your feet per catch."
     *
     * <p>Documented values: orig UltimateFishHook.java:41-45 (the five
     * weighted pools, transcribed at port UltimateFishHook.java:61-106),
     * :422-449 (base odds junk 10% / treasure 5% / fish 85% split 50/50
     * vanilla-vs-OreSpawn; in lava the catch ALWAYS comes from the lava
     * pool, :430-434), :400-411 (catch flung to the player + 1-6 XP);
     * phase_d_reports/D4_items_small_entities.md (ENT-S-059). Catches are
     * driven through the real {@code retrieve()} path with the
     * access-transformed {@code nibble} counter forced — the same seam the
     * classification blessed.</p>
     *
     * <p>Statistics: 40 water catches — P(no OreSpawn fish) = (1-0.425)^40
     * ≈ 2.4e-10 &lt; 1e-9; every water catch must fall in
     * junk ∪ treasure ∪ fish sets (deterministic), every one of 20 lava
     * catches in the lava set (deterministic).</p>
     */
    @GameTest(template = "empty_large", timeoutTicks = 200, batch = "orespawnLootFishing")
    public void i086_fishing_pools_water_and_lava(GameTestHelper helper) {
        Vec3 playerPos = helper.absoluteVec(new Vec3(10.5, 3.0, 10.5));
        ItemStack rod = new ItemStack(item(NS + ":ultimate_fishing_rod"));
        ServerPlayer angler = anglerAt(helper, playerPos, rod);

        // Water-context catches (hook not in lava -> junk/treasure/fish pools).
        Vec3 waterSpot = helper.absoluteVec(new Vec3(16.5, 4.0, 16.5));
        Set<Item> waterAllowed = new HashSet<>();
        waterAllowed.addAll(junkPoolItems());
        waterAllowed.addAll(treasurePoolItems());
        waterAllowed.addAll(waterFishItems());
        boolean sawOreSpawnFish = false;
        for (int i = 0; i < 40; i++) {
            ItemStack caught = retrieveOnce(helper, angler, rod, waterSpot);
            helper.assertTrue(waterAllowed.contains(caught.getItem()),
                    "water catch " + caught + " outside the documented junk/treasure/fish pools"
                            + " (orig UltimateFishHook.java:41-45)");
            if (orespawnWaterFish().contains(caught.getItem())) {
                sawOreSpawnFish = true;
            }
        }
        helper.assertTrue(sawOreSpawnFish,
                "40 water catches produced no OreSpawn fish (p_miss ≈ 2.4e-10) — the orig :445-448"
                        + " 50/50 OreSpawn fish pool is not being rolled");

        // Lava catches: hook inside a lava cell -> ALWAYS the lava-fish pool (orig :430-434).
        BlockPos lavaCellRel = new BlockPos(6, 3, 6);
        buildLavaCell(helper, lavaCellRel);
        Vec3 lavaSpot = Vec3.atCenterOf(helper.absolutePos(lavaCellRel));
        for (int i = 0; i < 20; i++) {
            ItemStack caught = retrieveOnce(helper, angler, rod, lavaSpot);
            helper.assertTrue(lavaFishItems().contains(caught.getItem()),
                    "lava catch " + caught + " outside the documented lava-fish pool (orig :44,:430-434)");
        }

        removeAngler(helper, angler);
        helper.succeed();
    }

    /**
     * Checklist i086-pools (gear condition, hook persistence, lava survival)
     * — "caught items ... SURVIVE the lava (LavaLovingItem); gear arrives
     * pre-damaged with a level-30 enchant; rod keeps working (doesn't
     * dismiss the custom hook)".
     *
     * <p>Documented values: orig UltimateFishHook.java func_150708_a — bow /
     * fishing-rod treasure entries carry damagePercent 0.25 plus a random
     * level-30 enchant (port PoolEntry records, UltimateFishHook.java:77-83,
     * finishCatch :284-302); orig :175 — the hook survives only while the
     * ULTIMATE rod is held (vanilla checks minecraft:fishing_rod and would
     * discard this hook on its first tick — port shouldStopFishing
     * :127-136); orig :409 — catches ride a fire-resistant item
     * (EntityLavaLovingItem, fire-immune ItemEntity).</p>
     *
     * <p>Statistics: treasure odds are raised deterministically through the
     * documented Luck-of-the-Sea formula (orig :424-429: f_treasure =
     * 0.05 + 0.01·level → 0.5 at level 45, junk 0). P(no damaged+enchanted
     * bow/rod in 120 catches) = (1 - 0.5·2/6)^120 = (5/6)^120 ≈ 3.2e-10
     * &lt; 1e-9.</p>
     */
    @GameTest(template = "empty_large", timeoutTicks = 250, batch = "orespawnLootFishing")
    public void i086_fishing_gear_hook_lifecycle_lava_survival(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Vec3 playerPos = helper.absoluteVec(new Vec3(10.5, 3.0, 10.5));
        ItemStack luckRod = new ItemStack(item(NS + ":ultimate_fishing_rod"));
        luckRod.enchant(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(Enchantments.LUCK_OF_THE_SEA), 45);
        ServerPlayer angler = anglerAt(helper, playerPos, luckRod);

        // Pre-damaged, enchanted treasure gear.
        Vec3 hookSpot = helper.absoluteVec(new Vec3(16.5, 4.0, 16.5));
        boolean sawDamagedEnchantedGear = false;
        for (int i = 0; i < 120 && !sawDamagedEnchantedGear; i++) {
            ItemStack caught = retrieveOnce(helper, angler, luckRod, hookSpot);
            if ((caught.is(Items.BOW) || caught.is(Items.FISHING_ROD))
                    && caught.getDamageValue() >= 1 && caught.isEnchanted()) {
                sawDamagedEnchantedGear = true;
            }
        }
        helper.assertTrue(sawDamagedEnchantedGear,
                "120 luck-45 catches produced no pre-damaged enchanted bow/rod (p_miss ≈ 3.2e-10) —"
                        + " orig func_150708_a damage+level-30-enchant path");

        // Hook persistence platform + hook.
        helper.setBlock(new BlockPos(20, 2, 20), Blocks.GLASS.defaultBlockState());
        Vec3 hookPersistSpot = helper.absoluteVec(new Vec3(20.5, 4.0, 20.5));
        ItemStack plainRod = new ItemStack(item(NS + ":ultimate_fishing_rod"));
        angler.setItemInHand(InteractionHand.MAIN_HAND, plainRod);
        UltimateFishHook persistentHook = new UltimateFishHook(angler, level, 0, 0);
        persistentHook.setPos(hookPersistSpot.x, hookPersistSpot.y, hookPersistSpot.z);
        level.addFreshEntity(persistentHook);

        // Lava-survival cells: the mod's carrier vs a vanilla control item.
        buildLavaCell(helper, new BlockPos(6, 3, 9));
        buildLavaCell(helper, new BlockPos(6, 3, 12));
        Vec3 survivorSpot = Vec3.atCenterOf(helper.absolutePos(new BlockPos(6, 3, 9)));
        Vec3 controlSpot = Vec3.atCenterOf(helper.absolutePos(new BlockPos(6, 3, 12)));
        EntityLavaLovingItem survivor = new EntityLavaLovingItem(level,
                survivorSpot.x, survivorSpot.y, survivorSpot.z, new ItemStack(Items.STICK));
        survivor.setDeltaMovement(Vec3.ZERO);
        level.addFreshEntity(survivor);
        ItemEntity control = new ItemEntity(level, controlSpot.x, controlSpot.y, controlSpot.z,
                new ItemStack(Items.STICK));
        control.setDeltaMovement(Vec3.ZERO);
        level.addFreshEntity(control);

        helper.runAtTickTime(40, () -> {
            helper.assertTrue(!persistentHook.isRemoved(),
                    "hook was dismissed while the Ultimate rod was held — orig :175 redirect broken"
                            + " (vanilla's minecraft:fishing_rod check would discard it on tick 1)");
            angler.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        });
        helper.runAtTickTime(55, () -> helper.assertTrue(persistentHook.isRemoved(),
                "hook must dismiss once the Ultimate rod leaves the hand (orig :175)"));
        helper.runAtTickTime(75, () -> {
            helper.assertTrue(survivor.isAlive(),
                    "EntityLavaLovingItem burned up in lava — must survive (orig :409 fireResistance)");
            helper.assertTrue(control.isRemoved(),
                    "vanilla control ItemEntity survived 75 ticks in lava — lava cell is not lethal,"
                            + " survival assert would be vacuous");
            survivor.discard();
            removeAngler(helper, angler);
            helper.succeed();
        });
    }

    // ==================================================================
    // i115-c7-crystal-egg-ores
    // ==================================================================

    /**
     * Checklist i115-c7-crystal-egg-ores — "breaking oreurchin/orerat/etc.
     * gives block item + occasional 5-9 XP, NO mob spawn."
     *
     * <p>Documented values: the 11 crystal-dimension egg ores (WGEN-023 pool
     * of ChunkProviderOreSpawn5.generateCrystalOres:586-633) are
     * OreGenericEgg blocks — orig OreSpawnMain.java:6326-6336, orig
     * OreGenericEgg.java:16-30 (50% chance of 5..9 XP), and "they do NOT
     * spawn mobs — only the separate CrystalRat/CrystalFairy deep-vein
     * OreBasicStone blocks do" (port ModBlocks.java:166-171;
     * phase_c_reports/C7_worldgen.md:122). Self-drop and the no-mob negative
     * are exercised through real player breaks; the XP roll through the
     * spawnAfterBreak mechanism (see
     * {@link #i014_i131_break_pops_xp_end_to_end} for the end-to-end XP
     * wiring sentinel).</p>
     *
     * <p>Statistics: 60 mechanism rolls per block, at-least-one-event —
     * false-fail 0.5^60 ≈ 8.7e-19 per block (×11 blocks ≈ 1e-17).</p>
     */
    @GameTest(template = "empty_large", timeoutTicks = 200)
    public void i115_crystal_egg_ore_breaks(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Player breaker = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos breakPos = helper.absolutePos(new BlockPos(24, 2, 24));
        AABB area = box(Vec3.atCenterOf(breakPos), 4, 6);
        String[] eggOres = {"ore_urchin", "ore_flounder", "ore_skate", "ore_rotator", "ore_peacock",
                "ore_fairy", "ore_dungeon_beast", "ore_vortex", "ore_rat", "ore_whale", "ore_irukandji"};

        for (String name : eggOres) {
            String id = NS + ":" + name;
            BlockState state = block(id).defaultBlockState();

            // Self-drop JSON + real breaks + no-mob-spawn negative.
            JsonObject json = lootJson(helper, "blocks", name);
            assertPool(helper, id + " self-drop", pool(json, 0), 1, 1, one(id));
            for (int i = 0; i < 3; i++) {
                clearDropsAndXp(helper, area);
                level.setBlock(breakPos, state, 3);
                level.destroyBlock(breakPos, true, breaker);
                Map<Item, Integer> drops = collectDrops(helper, area);
                helper.assertTrue(drops.getOrDefault(item(id), 0) == 1 && totalCount(drops) == 1,
                        id + ": break dropped " + drops + " instead of exactly 1x itself");
            }
            helper.assertTrue(helper.getLevel().getEntitiesOfClass(Mob.class, helper.getBounds()).isEmpty(),
                    id + ": breaking spawned a mob — egg ORES must never spawn (ModBlocks.java:166-171)");

            // 50% 5..9 XP mechanism (orig OreGenericEgg.java:26-29).
            int events = 0;
            for (int i = 0; i < 60; i++) {
                clearDropsAndXp(helper, area);
                state.spawnAfterBreak(level, breakPos, ItemStack.EMPTY, true);
                int xp = totalXp(level, area);
                if (xp > 0) {
                    events++;
                    helper.assertTrue(xp >= 5 && xp <= 9, id + ": XP event " + xp + " outside 5..9");
                }
            }
            helper.assertTrue(events > 0,
                    id + ": 0/60 XP events from the 50% roll (false-fail 8.7e-19)");
            clearDropsAndXp(helper, area);
        }
        helper.succeed();
    }

    // ==================================================================
    // i131 — SpawnOres pool blocks: break behavior
    // ==================================================================

    /**
     * Checklist i131 (D5c) — "Break any spawn ore: drops itself; ~50% of
     * breaks pop 5-9 XP; Silk Touch: block, no XP; nothing ever spawns from
     * breaking."
     *
     * <p>Documented values: every SpawnOres pool block is an OreGenericEgg
     * (ModBlocks.java:195-203, spawn_ores_spec.md §2; orig
     * OreSpawnMain.java:534-652/:1981-2099) with the orig
     * OreGenericEgg.java:24-30 XP roll — 50% chance of 5..9 — while the
     * 1.7.10 silk-harvest path skipped the roll (port OreGenericEgg
     * javadoc). Representative sample: spider_spawn_block (common c0),
     * kraken/dragon boss eggs (c50/c53), trex_spawn_block. The self-drop
     * table is silk-independent (single unconditional self entry).</p>
     *
     * <p>Statistics: rate assert on spider_spawn_block — 300 mechanism rolls
     * against the Hoeffding band [90,210] (false-fail ≈ 7.6e-11); presence
     * asserts (60 rolls) on the other three (0.5^60 ≈ 8.7e-19 each). The
     * mined-XP wiring itself is the
     * {@link #i014_i131_break_pops_xp_end_to_end} sentinel; the silk "no XP"
     * half is asserted end-to-end here (a pass under both the documented and
     * the current behavior, and a regression guard once the XP wiring is
     * fixed).</p>
     */
    @GameTest(template = "empty_large", timeoutTicks = 200)
    public void i131_spawn_ore_breaks(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Player breaker = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos breakPos = helper.absolutePos(new BlockPos(24, 2, 24));
        AABB area = box(Vec3.atCenterOf(breakPos), 4, 6);
        ItemStack silkPick = new ItemStack(Items.DIAMOND_PICKAXE);
        silkPick.enchant(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(Enchantments.SILK_TOUCH), 1);

        String[] spawnOres = {"spider_spawn_block", "kraken_spawn_block", "dragon_spawn_block",
                "trex_spawn_block"};
        for (int b = 0; b < spawnOres.length; b++) {
            String id = NS + ":" + spawnOres[b];
            Block blk = block(id);
            BlockState state = blk.defaultBlockState();

            // Self-drop structure: one unconditional self entry (silk-independent).
            JsonObject json = lootJson(helper, "blocks", spawnOres[b]);
            helper.assertTrue(pools(json).size() == 1, id + ": expected a single self-drop pool");
            assertPool(helper, id + " self-drop", pool(json, 0), 1, 1, one(id));

            // Plain break: itself, exactly once, nothing else, no mob.
            for (int i = 0; i < 3; i++) {
                clearDropsAndXp(helper, area);
                level.setBlock(breakPos, state, 3);
                level.destroyBlock(breakPos, true, breaker);
                Map<Item, Integer> drops = collectDrops(helper, area);
                helper.assertTrue(drops.getOrDefault(item(id), 0) == 1 && totalCount(drops) == 1,
                        id + ": plain break dropped " + drops + " instead of exactly 1x itself");
            }

            // Silk Touch break through the real playerDestroy path: block, no XP.
            for (int i = 0; i < 3; i++) {
                clearDropsAndXp(helper, area);
                level.setBlock(breakPos, state, 3);
                level.removeBlock(breakPos, false);
                blk.playerDestroy(level, breaker, breakPos, state, null, silkPick);
                Map<Item, Integer> drops = collectDrops(helper, area);
                helper.assertTrue(drops.getOrDefault(item(id), 0) == 1 && totalCount(drops) == 1,
                        id + ": silk break dropped " + drops + " instead of exactly 1x itself");
                helper.assertTrue(totalXp(level, area) == 0,
                        id + ": silk break must pop NO XP (1.7.10 silk-harvest path)");
            }
            helper.assertTrue(helper.getLevel().getEntitiesOfClass(Mob.class, helper.getBounds()).isEmpty(),
                    id + ": breaking spawned a mob — spawn ORES never spawn on break (D5c)");

            // XP roll mechanism.
            int rolls = b == 0 ? 300 : 60;
            int events = 0;
            for (int i = 0; i < rolls; i++) {
                clearDropsAndXp(helper, area);
                state.spawnAfterBreak(level, breakPos, ItemStack.EMPTY, true);
                int xp = totalXp(level, area);
                if (xp > 0) {
                    events++;
                    helper.assertTrue(xp >= 5 && xp <= 9,
                            id + ": XP event " + xp + " outside 5..9 (orig OreGenericEgg.java:26-29)");
                }
            }
            if (b == 0) {
                helper.assertTrue(events >= 90 && events <= 210,
                        id + ": " + events + "/300 XP events outside the 50% band [90,210]");
            } else {
                helper.assertTrue(events > 0, id + ": 0/" + rolls + " XP events from the 50% roll");
            }
            clearDropsAndXp(helper, area);
        }
        helper.succeed();
    }
}
