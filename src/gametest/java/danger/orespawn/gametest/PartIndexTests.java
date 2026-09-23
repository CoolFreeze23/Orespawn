package danger.orespawn.gametest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;

import danger.orespawn.OreSpawnMod;
import danger.orespawn.gametest.HitboxProfileExpectations.Expected;
import de.dertoaster.multihitboxlib.api.IMHLibFieldAccessor;
import de.dertoaster.multihitboxlib.api.IMHLibPartIndexHolder;
import de.dertoaster.multihitboxlib.api.IMHLibPartIndexMember;
import de.dertoaster.multihitboxlib.api.IMultipartEntity;
import de.dertoaster.multihitboxlib.entity.MHLibPartEntity;
import de.dertoaster.multihitboxlib.entity.hitbox.HitboxProfile;
import de.dertoaster.multihitboxlib.init.MHLibDatapackLoaders;
import de.dertoaster.multihitboxlib.util.BoneInformation;
import de.dertoaster.multihitboxlib.util.BoneSyncGate;
import de.dertoaster.multihitboxlib.util.PartEntityIndex;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.PartEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * ENT-S-174: the two costs ENT-S-173 declared, taken down.
 *
 * <p>The part index ({@link PartEntityIndex}, MixinLevel, MixinEntity, the two callbacks mixins): NeoForge's box queries
 * get the parts of only the creatures whose envelope meets the box, and must return exactly what the flat walk returned
 * - s174a compares every query kind against a brute-force walk of the level's part map over creatures that move, turn,
 * teleport, die and spawn; s174b pins the locality (a box on one creature hands over its parts and none of the others');
 * s174c pins the membership (the index follows the map as a creature joins and leaves). s174e measures the part walk
 * against the flat walk and logs it; the times are information, never asserted (a timing budget would be a decision).</p>
 *
 * <p>The bone stream (BoneSyncGate, the profiles' bone-sync-interval): s174d drives BoneSyncGate.decide and shouldSend,
 * the functions IMultipartEntity#updateSynching's client branch switches over, through the cadence they give (a server
 * gametest cannot run that client branch itself), and pins the profiles' data (the 103 generated species 2, the other
 * six 1) and the codec's range. On the server's own code: s174f, a packet every 8 ticks (the keepalive) holds the
 * elected master and a gap of 11 loses it (the 10-tick timeout the keepalive must stay under); s174g, a tick with no
 * packet carries the last pose along with the creature's movement (IMultipartEntity#mhlibRetainedPoseRetrieval,
 * finding: the retained world positions froze while the creature moved).</p>
 *
 * <p>Batches: the moving creatures of s174a would walk into s174b's quiet structure, so the two run in different
 * batches; the benchmark has its own, the election and the carry theirs.</p>
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class PartIndexTests {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String[] MOVERS = {"orespawn:alosaurus", "orespawn:crab", "orespawn:basilisk", "orespawn:peacock",
            "orespawn:mothra", "orespawn:cockateil", "orespawn:dragon", "orespawn:frog", "orespawn:the_prince_adult"};
    private static final List<String> OTHER_PROFILED = List.of("orespawn:the_king", "orespawn:the_queen", "orespawn:kraken",
            "orespawn:godzilla", "orespawn:ant_robot", "orespawn:spider_robot");

    @SuppressWarnings("unchecked")
    private static EntityType<? extends Mob> type(GameTestHelper helper, String id) {
        Optional<EntityType<?>> type = BuiltInRegistries.ENTITY_TYPE.getOptional(ResourceLocation.parse(id));
        helper.assertTrue(type.isPresent(), "no entity type " + id);
        return (EntityType<? extends Mob>) type.get();
    }

    private static PartEntityIndex index(ServerLevel level) {
        Object holder = level;
        return ((IMHLibPartIndexHolder) holder)._mhlibAccess_getPartIndex();
    }

    private static Mob frozen(GameTestHelper helper, String id, BlockPos pos) {
        Mob mob = helper.spawnWithNoFreeWill(type(helper, id), pos);
        mob.setNoAi(true);
        mob.setPersistenceRequired();
        return mob;
    }

    private static Set<Entity> identitySet() {
        return Collections.newSetFromMap(new IdentityHashMap<>());
    }

    /** The flat walk, as NeoForge's loop in Level.getEntities does it, straight over the level's part map. */
    private static Set<Entity> brute(ServerLevel level, AABB box, Entity except) {
        Set<Entity> out = identitySet();
        for (PartEntity<?> part : level.getPartEntities()) {
            if (part != except && part.getBoundingBox().intersects(box)) {
                out.add(part);
            }
        }
        return out;
    }

    private static Set<Entity> viaQuery(ServerLevel level, AABB box, Entity except) {
        Set<Entity> out = identitySet();
        out.addAll(level.getEntities(except, box, e -> e instanceof PartEntity<?>));
        return out;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Set<Entity> viaTyped(ServerLevel level, AABB box) {
        Set<Entity> out = identitySet();
        EntityTypeTest test = EntityTypeTest.forClass(PartEntity.class);
        out.addAll(level.getEntities(test, box, e -> true));
        return out;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static List<Entity> viaTypedLimit(ServerLevel level, AABB box, int limit) {
        List<Entity> out = new ArrayList<>();
        EntityTypeTest test = EntityTypeTest.forClass(PartEntity.class);
        level.getEntities(test, box, e -> true, (List) out, limit);
        return out;
    }

    /** Every query kind against the flat walk, for one box; returns the number of parts the flat walk found. */
    private static int compare(GameTestHelper helper, ServerLevel level, AABB box, Entity except, String label) {
        Set<Entity> expected = brute(level, box, null);
        Set<Entity> got = viaQuery(level, box, null);
        helper.assertTrue(got.equals(expected), label + ": getEntities(null, box) handed back " + got.size() + " parts, the flat walk "
                + expected.size() + " (box " + box + ")");
        Set<Entity> typed = viaTyped(level, box);
        helper.assertTrue(typed.equals(expected), label + ": the typed getEntities handed back " + typed.size() + " parts, the flat walk "
                + expected.size() + " (box " + box + ")");
        if (except != null) {
            Set<Entity> expectedExcept = brute(level, box, except);
            Set<Entity> gotExcept = viaQuery(level, box, except);
            helper.assertTrue(gotExcept.equals(expectedExcept), label + ": getEntities(except, box) differs from the flat walk (box " + box + ")");
        }
        List<Entity> limited = viaTypedLimit(level, box, 1);
        helper.assertTrue(limited.size() == Math.min(1, expected.size()) && expected.containsAll(limited),
                label + ": the typed getEntities with limit 1 handed back " + limited + " against " + expected.size() + " parts (box " + box + ")");
        return expected.size();
    }

    private static void checkTick(GameTestHelper helper, ServerLevel level, PartEntityIndex index, Random random, int tick, int[] hits) {
        helper.assertTrue(index.partCount() == level.getPartEntities().size(), "tick " + tick + ": the index holds " + index.partCount()
                + " parts, the level's part map " + level.getPartEntities().size());
        Vec3 origin = helper.absoluteVec(Vec3.ZERO);
        List<PartEntity<?>> near = new ArrayList<>();
        for (PartEntity<?> part : level.getPartEntities()) {
            if (part.position().distanceToSqr(origin.add(24, 8, 24)) < 64 * 64) {
                near.add(part);
            }
        }
        List<AABB> boxes = new ArrayList<>();
        for (int i = 0; i < Math.min(24, near.size()); i++) {
            AABB b = near.get(random.nextInt(near.size())).getBoundingBox();
            boxes.add(b);
            double tip = 0.02 + random.nextDouble() * 0.2;
            boxes.add(new AABB(b.maxX - tip, b.maxY - tip, b.maxZ - tip, b.maxX + tip, b.maxY + tip, b.maxZ + tip));
            Vec3 c = b.getCenter();
            boxes.add(new AABB(c.x, c.y, c.z, c.x, c.y, c.z));
        }
        for (int i = 0; i < 30; i++) {
            double size = 0.05 + random.nextDouble() * 12.0;
            Vec3 c = origin.add(random.nextDouble() * 48.0, random.nextDouble() * 16.0, random.nextDouble() * 48.0);
            boxes.add(AABB.ofSize(c, size, size * (0.2 + random.nextDouble()), size));
        }
        boxes.add(new AABB(origin.x - 500, origin.y - 64, origin.z - 500, origin.x + 548, origin.y + 320, origin.z + 548));
        boxes.add(AABB.ofSize(origin.add(2000, 8, 2000), 4, 4, 4));
        for (AABB box : boxes) {
            Entity except = near.isEmpty() ? null : near.get(random.nextInt(near.size()));
            hits[0] += compare(helper, level, box, except, "tick " + tick);
            hits[1]++;
        }
    }

    @GameTest(template = "empty_large", timeoutTicks = 200, batch = "partIndexMotion")
    public static void s174a_every_query_returns_the_flat_walks_parts(GameTestHelper helper) {
        final ServerLevel level = helper.getLevel();
        final PartEntityIndex index = index(level);
        final long flatWalksBefore = index.flatWalks();
        final long queriesBefore = index.queries();
        final List<Mob> mobs = new ArrayList<>();
        for (int i = 0; i < MOVERS.length; i++) {
            Mob mob = helper.spawn(type(helper, MOVERS[i]), new BlockPos(6 + (i % 3) * 16, 2, 6 + (i / 3) * 16));
            mob.setPersistenceRequired();
            mobs.add(mob);
        }
        final Random random = new Random(174L);
        final int[] tick = {0};
        final int[] hits = {0, 0};
        final boolean[] events = {false, false, false};
        helper.onEachTick(() -> {
            final int t = ++tick[0];
            if (t > 80) {
                return;
            }
            for (Mob mob : mobs) {
                if (mob.isAlive()) {
                    mob.setDeltaMovement((random.nextDouble() - 0.5) * 0.8, mob.getDeltaMovement().y, (random.nextDouble() - 0.5) * 0.8);
                    mob.setYRot(mob.getYRot() + (float) (random.nextDouble() * 40.0 - 20.0));
                    mob.yBodyRot = mob.getYRot();
                }
            }
            // teleported this tick: the parts stay where the last alignment put them until the creature's own tick
            if (t % 7 == 0) {
                Mob mob = mobs.get(random.nextInt(mobs.size()));
                if (mob.isAlive()) {
                    Vec3 to = helper.absoluteVec(new Vec3(4 + random.nextDouble() * 40, 2, 4 + random.nextDouble() * 40));
                    mob.teleportTo(to.x, to.y, to.z);
                    events[0] = true;
                }
            }
            // a creature leaves the level (its parts leave the map) and a new one joins it, each checked the same tick
            if (t == 30) {
                mobs.get(0).discard();
                events[1] = true;
            }
            if (t == 45) {
                Mob fresh = helper.spawn(type(helper, "orespawn:trex"), new BlockPos(24, 2, 24));
                fresh.setPersistenceRequired();
                mobs.add(fresh);
                events[2] = true;
            }
            checkTick(helper, level, index, random, t, hits);
            if (t == 80) {
                helper.assertTrue(events[0] && events[1] && events[2], "the run must teleport, remove and add a creature");
                helper.assertTrue(hits[0] > 0, "no query found any part: the comparison proved nothing");
                helper.assertTrue(index.flatWalks() == flatWalksBefore, (index.flatWalks() - flatWalksBefore)
                        + " queries fell back to the flat walk: the index lost step with the part map");
                // every compare() runs at least three queries through Level.getEntities (null, typed, typed with a limit):
                // they must all have reached the index, or the comparison above compared the flat walk with itself
                helper.assertTrue(index.queries() - queriesBefore >= 3L * hits[1], (index.queries() - queriesBefore)
                        + " queries reached the index for " + hits[1] + " boxes: Level.getEntities does not consult it");
                LOGGER.info("ENT-S-174 s174a: {} boxes over 80 ticks, {} part hits, every query kind equal to the flat walk", hits[1], hits[0]);
                helper.succeed();
            }
        });
    }

    @GameTest(template = "empty_large", timeoutTicks = 60, batch = "partIndexStatic")
    public static void s174b_a_box_on_one_creature_hands_over_only_its_parts(GameTestHelper helper) {
        final Mob a = frozen(helper, "orespawn:alosaurus", new BlockPos(8, 2, 24));
        final Mob b = frozen(helper, "orespawn:basilisk", new BlockPos(40, 2, 8));
        final Mob c = frozen(helper, "orespawn:crab", new BlockPos(40, 2, 40));
        helper.runAfterDelay(4, () -> {
            final ServerLevel level = helper.getLevel();
            final PartEntityIndex index = index(level);
            Set<Entity> aParts = identitySet();
            Collections.addAll(aParts, a.getParts());
            Set<Entity> others = identitySet();
            Collections.addAll(others, b.getParts());
            Collections.addAll(others, c.getParts());
            helper.assertTrue(aParts.size() == a.getParts().length && aParts.size() > 1
                    && others.size() == b.getParts().length + c.getParts().length,
                    "the three creatures' parts are not distinct: " + aParts.size() + " and " + others.size());
            for (PartEntity<?> part : a.getParts()) {
                AABB box = AABB.ofSize(part.getBoundingBox().getCenter(), 0.2, 0.2, 0.2);
                Collection<PartEntity<?>> handed = index.near(level.getPartEntities(), box);
                Set<Entity> handedSet = identitySet();
                handedSet.addAll(handed);
                helper.assertTrue(handedSet.containsAll(aParts), "a box on the alosaurus's " + part + " did not hand over all its parts");
                for (Entity other : others) {
                    helper.assertFalse(handedSet.contains(other), "a box on the alosaurus handed over " + other + " of another creature");
                }
                helper.assertTrue(handed.size() < level.getPartEntities().size(), "the index handed over the whole part map");
            }
            AABB empty = AABB.ofSize(helper.absoluteVec(new Vec3(24, 14, 24)), 1, 1, 1);
            Set<Entity> handedEmpty = identitySet();
            handedEmpty.addAll(index.near(level.getPartEntities(), empty));
            for (Entity part : aParts) {
                helper.assertFalse(handedEmpty.contains(part), "a box in the empty air handed over the alosaurus's " + part);
            }
            for (Entity part : others) {
                helper.assertFalse(handedEmpty.contains(part), "a box in the empty air handed over " + part);
            }
            helper.succeed();
        });
    }

    @GameTest(template = "empty_large", timeoutTicks = 40, batch = "partIndexStatic")
    public static void s174c_the_index_follows_the_part_map(GameTestHelper helper) {
        final ServerLevel level = helper.getLevel();
        final PartEntityIndex index = index(level);
        helper.assertTrue(index.partCount() == level.getPartEntities().size(), "before the spawn: index " + index.partCount()
                + " parts, map " + level.getPartEntities().size());
        final Mob peacock = frozen(helper, "orespawn:peacock", new BlockPos(24, 2, 24));
        final PartEntityIndex.Group group = index.groupOf(peacock);
        helper.assertTrue(group != null && group.size() == peacock.getParts().length && group.parent() == peacock,
                "the spawned peacock has no group of its " + peacock.getParts().length + " parts in the index");
        helper.assertTrue(index.partCount() == level.getPartEntities().size(), "after the spawn: index " + index.partCount()
                + " parts, map " + level.getPartEntities().size());
        for (PartEntity<?> part : peacock.getParts()) {
            Object member = part;
            helper.assertTrue(((IMHLibPartIndexMember) member)._mhlibAccess_getPartIndexGroup() == group, part + " does not point at its group");
        }
        peacock.discard();
        helper.assertTrue(index.groupOf(peacock) == null, "the discarded peacock's group is still in the index");
        helper.assertTrue(index.partCount() == level.getPartEntities().size(), "after the discard: index " + index.partCount()
                + " parts, map " + level.getPartEntities().size());
        for (PartEntity<?> part : peacock.getParts()) {
            Object member = part;
            helper.assertTrue(((IMHLibPartIndexMember) member)._mhlibAccess_getPartIndexGroup() == null, part + " still points at a group");
        }
        helper.succeed();
    }

    private static DataResult<HitboxProfile> parseProfile(String intervalField) {
        String json = "{\"synched-assets\": {\"models\": [], \"animations\": [], \"textures\": []}, \"sync-with-model\": true, "
                + intervalField + "\"synched-bones\": [], \"main-hitbox\": {\"collidable\": false, \"canReceiveDamage\": false}, \"parts\": []}";
        JsonElement element = JsonParser.parseString(json);
        return HitboxProfile.CODEC.parse(JsonOps.INSTANCE, element);
    }

    /**
     * The master client of a creature drawn every frame (a builder is waiting at every tick), through the functions
     * IMultipartEntity#updateSynching's client branch switches over: BoneSyncGate.decide, then shouldSend for a built packet.
     */
    private static int sendsOver(int ticks, int interval, boolean poseChangesEveryTick) {
        int sinceLastSend = 0;
        boolean first = true;
        int sends = 0;
        for (int t = 1; t <= ticks; t++) {
            sinceLastSend = Math.min(sinceLastSend + 1, 8);
            final boolean firstNow = first;
            if (BoneSyncGate.decide(true, firstNow, true, sinceLastSend, interval) == BoneSyncGate.Step.BUILD
                    && BoneSyncGate.shouldSend(firstNow, sinceLastSend, () -> !poseChangesEveryTick)) {
                sends++;
                first = false;
                sinceLastSend = 0;
            }
        }
        return sends;
    }

    /**
     * A client that is not the master, through BoneSyncGate.decide and shouldSend and the builder alternation they give
     * an entity it does not draw bones for (START on a tick with no builder, BUILD on the next): its packets are empty
     * (tryAddBoneInformation refuses a non-master's bones), equal to the last one after the first.
     * {@code electionEveryTick}: the server is rotating its choice and every tick brings an SPacketSetMaster, which nulls
     * the cache. {@code before}: the rule without the ENT-S-174 gate, where every client built as the master does.
     */
    private static int nonMasterSendsOver(int ticks, boolean electionEveryTick, boolean before) {
        boolean builder = false;
        boolean first = true;
        int sinceLastSend = 0;
        int sends = 0;
        for (int t = 1; t <= ticks; t++) {
            if (electionEveryTick) {
                first = true;
            }
            sinceLastSend = Math.min(sinceLastSend + 1, 8);
            final boolean firstNow = first;
            switch (BoneSyncGate.decide(before, firstNow, builder, sinceLastSend, 1)) {
                case SILENT, HOLD -> builder = false;
                case START -> builder = true;
                case BUILD -> {
                    if (BoneSyncGate.shouldSend(firstNow, sinceLastSend, () -> true)) {
                        sends++;
                        first = false;
                        sinceLastSend = 0;
                    }
                    builder = false;
                }
            }
        }
        return sends;
    }

    @GameTest(template = "empty", timeoutTicks = 20, batch = "partIndexStatic")
    public static void s174d_the_bone_stream_gates_and_the_profiles_interval(GameTestHelper helper) {
        helper.assertTrue(BoneSyncGate.mayBuild(true, false), "the master must build in the steady state");
        helper.assertTrue(BoneSyncGate.mayBuild(true, true), "the master must build right after an election");
        helper.assertTrue(BoneSyncGate.mayBuild(false, true), "any client must answer once after a master change (the election settles on it)");
        helper.assertFalse(BoneSyncGate.mayBuild(false, false), "a client that is not the master must fall silent in the steady state");
        helper.assertTrue(nonMasterSendsOver(80, false, true) == 10 && nonMasterSendsOver(80, false, false) == 1,
                "a non-master in the steady state: 10 empty packets in 80 ticks before, 1 after; got " + nonMasterSendsOver(80, false, true)
                        + " / " + nonMasterSendsOver(80, false, false));
        helper.assertTrue(nonMasterSendsOver(80, true, false) == nonMasterSendsOver(80, true, true) && nonMasterSendsOver(80, true, false) == 40,
                "while the server rotates its choice a non-master must still answer every other tick, as before; got "
                        + nonMasterSendsOver(80, true, false) + " / " + nonMasterSendsOver(80, true, true));
        for (int interval = 1; interval <= 8; interval++) {
            for (int since = 0; since <= 8; since++) {
                helper.assertTrue(BoneSyncGate.intervalAllowsSend(true, since, interval), "the first payload after an election must go out at once");
                helper.assertTrue(BoneSyncGate.intervalAllowsSend(false, since, interval) == (since >= interval),
                        "interval " + interval + ", " + since + " ticks since the last send");
            }
        }
        helper.assertTrue(BoneSyncGate.decide(false, false, true, 3, 2) == BoneSyncGate.Step.SILENT
                && BoneSyncGate.decide(true, false, false, 3, 2) == BoneSyncGate.Step.START
                && BoneSyncGate.decide(true, false, true, 1, 2) == BoneSyncGate.Step.HOLD
                && BoneSyncGate.decide(true, false, true, 2, 2) == BoneSyncGate.Step.BUILD
                && BoneSyncGate.decide(true, true, true, 1, 2) == BoneSyncGate.Step.BUILD, "the four steps of decide");
        helper.assertTrue(BoneSyncGate.shouldSend(true, 1, () -> true) && BoneSyncGate.shouldSend(false, 8, () -> true)
                && BoneSyncGate.shouldSend(false, 1, () -> false) && !BoneSyncGate.shouldSend(false, 7, () -> true),
                "OPT-003's rule: first, keepalive or changed");
        helper.assertTrue(BoneSyncGate.intervalAllowsSend(false, 1, 0), "an interval below 1 reads as 1");
        helper.assertTrue(BoneSyncGate.intervalAllowsSend(false, 8, 20), "an interval above the keepalive reads as the keepalive");
        helper.assertTrue(sendsOver(80, 1, true) == 80, "every tick for interval 1 with a moving pose, got " + sendsOver(80, 1, true));
        helper.assertTrue(sendsOver(80, 2, true) == 40, "every second tick for interval 2 with a moving pose, got " + sendsOver(80, 2, true));
        helper.assertTrue(sendsOver(80, 1, false) == sendsOver(80, 2, false) && sendsOver(80, 2, false) == 10,
                "a still pose keeps the 8-tick keepalive at either interval: " + sendsOver(80, 1, false) + " / " + sendsOver(80, 2, false));

        final ServerLevel level = helper.getLevel();
        List<String> wrong = new ArrayList<>();
        for (Expected expected : HitboxProfileExpectations.ALL) {
            Optional<HitboxProfile> profile = MHLibDatapackLoaders.getHitboxProfile(ResourceLocation.parse(expected.id()), level.registryAccess());
            if (profile.isEmpty() || profile.get().boneSyncInterval() != 2) {
                wrong.add(expected.id() + "=" + profile.map(HitboxProfile::boneSyncInterval).orElse(-1));
            }
        }
        for (String id : OTHER_PROFILED) {
            Optional<HitboxProfile> profile = MHLibDatapackLoaders.getHitboxProfile(ResourceLocation.parse(id), level.registryAccess());
            if (profile.isEmpty() || profile.get().boneSyncInterval() != 1) {
                wrong.add(id + "=" + profile.map(HitboxProfile::boneSyncInterval).orElse(-1));
            }
        }
        helper.assertTrue(HitboxProfileExpectations.ALL.size() == 103 && wrong.isEmpty(),
                "bone-sync-interval must be 2 on the 103 generated profiles and 1 on the six others: " + wrong);

        helper.assertTrue(parseProfile("").result().map(HitboxProfile::boneSyncInterval).orElse(-1) == 1, "an absent interval must read as 1");
        helper.assertTrue(parseProfile("\"bone-sync-interval\": 8, ").result().map(HitboxProfile::boneSyncInterval).orElse(-1) == 8,
                "interval 8 must parse");
        helper.assertTrue(parseProfile("\"bone-sync-interval\": 0, ").result().isEmpty(), "interval 0 must be refused");
        helper.assertTrue(parseProfile("\"bone-sync-interval\": 9, ").result().isEmpty(), "interval 9 must be refused");
        helper.succeed();
    }

    private static long walkFlat(Collection<PartEntity<?>> flat, AABB[] boxes) {
        long hits = 0;
        for (AABB box : boxes) {
            for (PartEntity<?> part : flat) {
                if (part.getBoundingBox().intersects(box)) {
                    hits++;
                }
            }
        }
        return hits;
    }

    private static long walkIndexed(PartEntityIndex index, Collection<PartEntity<?>> flat, AABB[] boxes, List<PartEntityIndex.Group> groups) {
        long hits = 0;
        int q = 0;
        for (AABB box : boxes) {
            // one query per creature per tick, and every creature moves once a tick: every group is marked moved once
            // per groups.size() queries, so the rebuilds are paid as in play
            if (q++ % groups.size() == 0) {
                for (PartEntityIndex.Group group : groups) {
                    group.markMoved();
                }
            }
            for (PartEntity<?> part : index.near(flat, box)) {
                if (part.getBoundingBox().intersects(box)) {
                    hits++;
                }
            }
        }
        return hits;
    }

    @GameTest(template = "empty_large", timeoutTicks = 100, batch = "partIndexBench")
    public static void s174e_the_part_walk_against_the_flat_walk(GameTestHelper helper) {
        final List<Mob> mobs = new ArrayList<>();
        for (int i = 0; i < 120; i++) {
            Expected expected = HitboxProfileExpectations.ALL.get(i % HitboxProfileExpectations.ALL.size());
            mobs.add(frozen(helper, expected.id(), new BlockPos(2 + (i % 11) * 4, 2, 2 + (i / 11) * 4)));
        }
        helper.runAfterDelay(4, () -> {
            final ServerLevel level = helper.getLevel();
            final PartEntityIndex index = index(level);
            final Collection<PartEntity<?>> flat = level.getPartEntities();
            final List<PartEntityIndex.Group> groups = new ArrayList<>();
            for (Mob mob : mobs) {
                // a species that removes itself (the Urchin's 1.7.10 daytime discard, 1 in 400 a tick) takes its parts out
                // of the level's map, and the index follows: nothing to measure for it
                if (mob.isRemoved()) {
                    continue;
                }
                PartEntityIndex.Group group = index.groupOf(mob);
                helper.assertTrue(group != null, mob + " has no group");
                groups.add(group);
            }
            helper.assertTrue(groups.size() >= 100, "only " + groups.size() + " of the 120 creatures are still in the level");
            final Random random = new Random(1741L);
            final Vec3 origin = helper.absoluteVec(Vec3.ZERO);
            final AABB[] boxes = new AABB[8192];
            for (int i = 0; i < boxes.length; i++) {
                double size = 0.6 + random.nextDouble() * 2.4;
                boxes[i] = AABB.ofSize(origin.add(random.nextDouble() * 48.0, 1.0 + random.nextDouble() * 6.0, random.nextDouble() * 48.0), size, size * 1.5, size);
            }
            long hitsFlat = 0;
            long hitsIndexed = 0;
            for (int warm = 0; warm < 4; warm++) {
                hitsFlat = walkFlat(flat, boxes);
                hitsIndexed = walkIndexed(index, flat, boxes, groups);
            }
            final int rounds = 6;
            long t0 = System.nanoTime();
            for (int r = 0; r < rounds; r++) {
                hitsFlat = walkFlat(flat, boxes);
            }
            long t1 = System.nanoTime();
            for (int r = 0; r < rounds; r++) {
                hitsIndexed = walkIndexed(index, flat, boxes, groups);
            }
            long t2 = System.nanoTime();
            helper.assertTrue(hitsFlat == hitsIndexed, "the indexed walk found " + hitsIndexed + " parts, the flat walk " + hitsFlat);
            helper.assertTrue(hitsFlat > 0, "no box met a part: the benchmark measured nothing");
            final double queries = (double) rounds * boxes.length;
            final double flatNs = (t1 - t0) / queries;
            final double indexedNs = (t2 - t1) / queries;
            LOGGER.info(String.format(java.util.Locale.ROOT,
                    "ENT-S-174 bench: %d parts of %d creatures in the level's part map (%d here); flat walk %.0f ns per query, "
                            + "indexed %.0f ns per query (%.1fx), every creature's envelope rebuilt once per %d queries; %d part hits each way",
                    flat.size(), index.groupCount(), groups.stream().mapToInt(PartEntityIndex.Group::size).sum(), flatNs, indexedNs,
                    flatNs / Math.max(indexedNs, 1e-9), groups.size(), hitsFlat));
            helper.succeed();
        });
    }

    private static IMultipartEntity<?> multipart(GameTestHelper helper, Mob mob) {
        Object asObject = mob;
        helper.assertTrue(asObject instanceof IMultipartEntity<?> && asObject instanceof IMHLibFieldAccessor<?>,
                mob + " is not an MHLib multipart entity");
        return (IMultipartEntity<?>) asObject;
    }

    @GameTest(template = "empty_large", timeoutTicks = 200, batch = "partIndexElection")
    public static void s174f_a_packet_every_8_ticks_holds_the_master_and_a_gap_of_11_loses_it(GameTestHelper helper) {
        final Mob mob = frozen(helper, "orespawn:alosaurus", new BlockPos(24, 2, 24));
        final IMultipartEntity<?> ime = multipart(helper, mob);
        // two trackers the election can choose between (BenchHarnessTests' form: the queue is what the election reads)
        final Object asAccess = mob;
        final IMHLibFieldAccessor<?> access = (IMHLibFieldAccessor<?>) asAccess;
        access._mhlibAccess_getTrackerQueue().add(UUID.randomUUID());
        access._mhlibAccess_getTrackerQueue().add(UUID.randomUUID());
        final Map<String, BoneInformation> empty = new HashMap<>();
        final int[] tick = {0};
        final int[] lastPacket = {0};
        final UUID[] held = {null};
        final boolean[] lost = {false};
        helper.onEachTick(() -> {
            final int t = ++tick[0];
            if (t > 130) {
                return;
            }
            // the test's hook runs after the level's tick: a packet "arrives" here, between ticks, as a handled one does
            if (held[0] == null) {
                helper.assertTrue(t < 10, "no master elected from the two queued trackers by tick " + t);
                if (ime.getMasterUUID() != null) {
                    held[0] = ime.getMasterUUID();
                    ime.processBoneInformation(empty);
                    lastPacket[0] = t;
                }
                return;
            }
            if (t <= 70) {
                // the keepalive's cadence: a packet every 8 ticks never lets the elected master go
                if (t - lastPacket[0] >= 8) {
                    ime.processBoneInformation(empty);
                    lastPacket[0] = t;
                }
                helper.assertTrue(held[0].equals(ime.getMasterUUID()), "tick " + t + ": a packet every 8 ticks lost the master to "
                        + ime.getMasterUUID());
            } else {
                // a packet only every 11 ticks: the 10-tick timeout fires in each gap and the election moves on
                if (t - lastPacket[0] >= 11) {
                    ime.processBoneInformation(empty);
                    lastPacket[0] = t;
                }
                if (!held[0].equals(ime.getMasterUUID())) {
                    lost[0] = true;
                }
            }
            if (t == 130) {
                helper.assertTrue(lost[0], "a packet only every 11 ticks never timed the master out: the 10-tick timeout is not where the keepalive assumes it");
                helper.succeed();
            }
        });
    }

    @GameTest(template = "empty_large", timeoutTicks = 60, batch = "partIndexElection")
    public static void s174g_a_tick_without_a_packet_carries_the_last_pose_with_the_creature(GameTestHelper helper) {
        final Mob mob = frozen(helper, "orespawn:alosaurus", new BlockPos(24, 2, 24));
        final IMultipartEntity<?> ime = multipart(helper, mob);
        final Vec3 moveBy = new Vec3(1.0, 0.0, -0.5);
        final Map<Entity, Vec3> received = new IdentityHashMap<>();
        final Map<Entity, Vec3> carried = new IdentityHashMap<>();
        helper.runAfterDelay(3, () -> {
            // a pose as a master client would send it: every synched part's bone at a world point near the creature
            final Map<String, BoneInformation> pose = new HashMap<>();
            int i = 0;
            for (PartEntity<?> part : mob.getParts()) {
                final String name = ((MHLibPartEntity<?>) part).getConfigName();
                pose.put(name, new BoneInformation(name, false, mob.position().add(0.25 * i, 2.0 + 0.1 * i, -0.2 * i),
                        BoneInformation.DEFAULT_SCALING, Vec3.ZERO));
                i++;
            }
            ime.processBoneInformation(pose);
        });
        helper.runAfterDelay(4, () -> {
            // the packet's tick placed the parts as received; now the creature moves before its next tick
            for (PartEntity<?> part : mob.getParts()) {
                received.put(part, part.position());
            }
            mob.setPos(mob.getX() + moveBy.x, mob.getY() + moveBy.y, mob.getZ() + moveBy.z);
        });
        helper.runAfterDelay(5, () -> {
            for (PartEntity<?> part : mob.getParts()) {
                final Vec3 shift = part.position().subtract(received.get(part));
                helper.assertTrue(shift.distanceTo(moveBy) < 1e-9, part + " moved by " + shift + " on the tick without a packet; the creature moved by "
                        + moveBy + " (the retained pose must follow it)");
                carried.put(part, part.position());
            }
        });
        helper.runAfterDelay(6, () -> {
            for (PartEntity<?> part : mob.getParts()) {
                helper.assertTrue(part.position().equals(carried.get(part)), part + " moved while the creature stood still: "
                        + carried.get(part) + " -> " + part.position());
            }
            helper.succeed();
        });
    }
}
