package danger.orespawn.gametest;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Godzilla;
import danger.orespawn.entity.SpiderRobot;
import danger.orespawn.entity.TheKing;
import danger.orespawn.entity.TheQueen;
import danger.orespawn.entity.gait.ModernSpiderGait;
import danger.orespawn.entity.gait.PlanarFabrik;
import danger.orespawn.entity.gait.SpiderRigProfile;
import de.dertoaster.multihitboxlib.api.IMHLibFieldAccessor;
import de.dertoaster.multihitboxlib.api.IMultipartEntity;
import de.dertoaster.multihitboxlib.entity.MHLibPartEntity;

import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.entity.PartEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * 2.0 spider overhaul, S4 — multi-part hitbox invariants (design doc tests
 * 5-8) plus the updateSynching election-neutrality observation and the
 * part→parent sweep backing the crosshair-HUD unwrap.
 *
 * <p>Harness-independence note (project law): the part-tracking test
 * recomputes expected positions with the same validated component chain
 * (inverse transform / planar solve / forward transform) the S2 and S3b
 * harnesses anchored against independent formulations (test-side FK, the
 * JOML renderer replay). What THIS test therefore proves is the FEED WIRING
 * — that parts are placed from the live gait state every tick — with the
 * math itself vouched for by those independent anchors, not by this loop.</p>
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class HitboxPartTests {

    private static SpiderRobot spawnModern(GameTestHelper helper, BlockPos pos) {
        OreSpawnConfig.SpiderMovement prior = OreSpawnConfig.SPIDER_MOVEMENT.get();
        try {
            OreSpawnConfig.SPIDER_MOVEMENT.set(OreSpawnConfig.SpiderMovement.MODERN);
            return helper.spawn(ModEntities.SPIDER_ROBOT.get(), pos);
        } finally {
            OreSpawnConfig.SPIDER_MOVEMENT.set(prior);
        }
    }

    private static MHLibPartEntity<?> legPart(SpiderRobot spider, int leg) {
        Object self = spider;
        if (self instanceof IMultipartEntity<?> multipart) {
            return multipart.getPartByName("leg" + leg).orElse(null);
        }
        return null;
    }

    /**
     * Test 7 (design doc): classic constructs ZERO parts and stays exactly
     * the 1.0 entity (pickable, not multipart); modern carries 8 leg parts
     * with the body STILL pickable (D3: legs are additional surfaces, never
     * forced routing), and typed queries see no part contamination.
     */
    @GameTest(template = "empty", batch = "spiderGaitIsolation")
    public void s4_part_counts_and_classic_zero(GameTestHelper helper) {
        OreSpawnConfig.SpiderMovement prior = OreSpawnConfig.SPIDER_MOVEMENT.get();
        try {
            OreSpawnConfig.SPIDER_MOVEMENT.set(OreSpawnConfig.SpiderMovement.CLASSIC);
            SpiderRobot classic = helper.spawnWithNoFreeWill(ModEntities.SPIDER_ROBOT.get(), new BlockPos(2, 2, 2));
            helper.assertTrue(classic.getParts() == null || classic.getParts().length == 0,
                    "CLASSIC spider constructed MHLib parts (D3 zero-parts law)");
            helper.assertFalse(classic.isMultipartEntity(), "CLASSIC spider reports multipart");
            helper.assertTrue(classic.isPickable(), "CLASSIC spider must stay pickable");

            OreSpawnConfig.SPIDER_MOVEMENT.set(OreSpawnConfig.SpiderMovement.MODERN);
            SpiderRobot modern = helper.spawnWithNoFreeWill(ModEntities.SPIDER_ROBOT.get(), new BlockPos(2, 2, 6));
            helper.assertTrue(modern.getParts() != null && modern.getParts().length == 8,
                    "MODERN spider must carry exactly 8 leg parts, got "
                            + (modern.getParts() == null ? 0 : modern.getParts().length));
            helper.assertTrue(modern.isMultipartEntity(), "MODERN spider must report multipart");
            helper.assertTrue(modern.isPickable(),
                    "MODERN spider body must STAY pickable (profile main canReceiveDamage=true)");
            // Server part-id cascade contract — the exact sequence the
            // client's id-restore build must reproduce (multiplayer id
            // integrity; the client half is client-only code, TO BE
            // owner-verified in-game per the recorded exit evidence).
            java.util.Set<Integer> partIds = new java.util.HashSet<>();
            for (PartEntity<?> part : modern.getParts()) {
                helper.assertTrue(part.getParent() == modern,
                        "leg part parent mismatch (HUD unwrap contract)");
                partIds.add(part.getId());
            }
            for (int i = 1; i <= 8; ++i) {
                helper.assertTrue(partIds.contains(modern.getId() + i),
                        "part ids must cascade parentId+1..+8 (got " + partIds + " for base "
                                + modern.getId() + ")");
            }
            // CLASS-based count (review: typed EntityType queries DO return
            // parts as their parent's type once fed — the honest fed-state
            // pin lives in s4_parts_track_solver_legs).
            helper.assertTrue(
                    helper.getLevel().getEntitiesOfClass(SpiderRobot.class,
                            new net.minecraft.world.phys.AABB(helper.absolutePos(new BlockPos(2, 2, 4)))
                                    .inflate(12.0)).size() == 2,
                    "class query must see exactly the two spiders");
            classic.discard();
            modern.discard();
        } finally {
            OreSpawnConfig.SPIDER_MOVEMENT.set(prior);
        }
        helper.succeed();
    }

    /**
     * Test 5 (design doc): while the spider walks, every planted leg's part
     * sits on the lower-segment midpoint of the live solve (see the class
     * javadoc for what this proves under the harness-independence law).
     */
    @GameTest(template = "empty_large", timeoutTicks = 400, batch = "spiderGaitIsolation")
    public void s4_parts_track_solver_legs(GameTestHelper helper) {
        final SpiderRobot spider = spawnModern(helper, new BlockPos(24, 2, 24));
        final ModernSpiderGait gait;
        try {
            gait = spider.getModernGait();
            helper.assertTrue(gait != null, "modern spider must carry the gait controller");
            helper.assertTrue(spider.getParts() != null && spider.getParts().length == 8,
                    "modern spider must carry 8 parts");
        } catch (RuntimeException e) {
            spider.discard();
            throw e;
        }
        final int[] tick = {0};
        final int[] checked = {0};
        final double[][] joints = {new double[2], new double[2], new double[2], new double[2]};

        helper.onEachTick(() -> {
            try {
                int t = ++tick[0];
                if (t < 30) {
                    return;
                }
                if (t <= 80) {
                    spider.setDeltaMovement(0.30, spider.getDeltaMovement().y, 0.0);
                }
                float yaw = spider.getYRot();
                long time = helper.getLevel().getGameTime();
                double[] livePos = new double[3];
                for (int leg = 0; leg < SpiderRigProfile.LEG_COUNT; ++leg) {
                    if (gait.isStranded(leg)) {
                        continue;
                    }
                    MHLibPartEntity<?> part = legPart(spider, leg);
                    helper.assertTrue(part != null, "missing part leg" + leg);
                    // Restated tolerance (design ruling, Option A): PLANTED
                    // legs and SWINGING legs alike assert against the
                    // SERVER-TRUE trajectory (currentFootPos) — never a
                    // rendered position. Expected midpoint mirrors the feed.
                    gait.currentFootPos(leg, time, livePos);
                    double[] rel = {
                            livePos[0] - spider.getX(),
                            livePos[1] - spider.getY(),
                            livePos[2] - spider.getZ()};
                    ModernSpiderGait.inverseBodyTransform(yaw, gait.bodyPitch(), gait.bodyRoll(),
                            gait.bodyLift(), rel);
                    double hipRelX = SpiderRigProfile.hipX(leg, spider.getX(), yaw) - spider.getX();
                    double hipRelY = SpiderRigProfile.hipY(leg, spider.getY()) - spider.getY();
                    double hipRelZ = SpiderRigProfile.hipZ(leg, spider.getZ(), yaw) - spider.getZ();
                    double rx = rel[0] - hipRelX;
                    double ry = rel[1] - hipRelY;
                    double rz = rel[2] - hipRelZ;
                    double dist = Math.sqrt(rx * rx + ry * ry + rz * rz);
                    double cap = SpiderRigProfile.MAX_REACH * 0.98;
                    if (dist > cap) {
                        double s = cap / dist;
                        rel[0] = hipRelX + rx * s;
                        rel[1] = hipRelY + ry * s;
                        rel[2] = hipRelZ + rz * s;
                    }
                    double dx = rel[0] - hipRelX;
                    double dz = rel[2] - hipRelZ;
                    double dh = Math.sqrt(dx * dx + dz * dz);
                    // Mirror of the production degenerate fallback exactly
                    // (same 1e-6 threshold, same neutral bearing — fix-review).
                    final double ux;
                    final double uz;
                    if (dh > 1.0E-6) {
                        ux = dx / dh;
                        uz = dz / dh;
                    } else {
                        double alphaW = SpiderRigProfile.legBearing(leg, yaw) + Math.PI / 2.0;
                        ux = Math.cos(alphaW);
                        uz = Math.sin(alphaW);
                    }
                    PlanarFabrik.solve(SpiderRigProfile.SEGMENT_LENGTH, dh, rel[1] - hipRelY,
                            PlanarFabrik.DEFAULT_KNEE_BIAS, joints);
                    double midU = (joints[2][0] + joints[3][0]) * 0.5;
                    double midV = (joints[2][1] + joints[3][1]) * 0.5;
                    double[] world = {hipRelX + ux * midU, hipRelY + midV, hipRelZ + uz * midU};
                    ModernSpiderGait.bodyTransform(yaw, gait.bodyPitch(), gait.bodyRoll(),
                            gait.bodyLift(), world);
                    double ex = spider.getX() + world[0];
                    double ey = spider.getY() + world[1] - 0.3;
                    double ez = spider.getZ() + world[2];
                    double miss = Math.abs(part.getX() - ex) + Math.abs(part.getY() - ey)
                            + Math.abs(part.getZ() - ez);
                    helper.assertTrue(miss < 1.0E-3,
                            "leg" + leg + " part off its solver midpoint by " + miss + " (test 5)");
                    ++checked[0];
                }
                if (t == 50) {
                    // Honest fed-state pin (review): type-matched queries DO
                    // include part entities as their parent's type — vanilla
                    // EnderDragon parity. One spider + 8 fed legs = 9. (The
                    // UNTYPED overload is used deliberately: the typed one
                    // heap-pollutes its List<SpiderRobot> with parts and CCEs
                    // in the predicate — the exact hazard the class-based
                    // rewrite of the count assertions guards against.)
                    int typeMatched = 0;
                    for (Entity e : helper.getLevel().getEntities((Entity) null,
                            spider.getBoundingBox().inflate(24.0))) {
                        if (e.getType() == ModEntities.SPIDER_ROBOT.get()) {
                            ++typeMatched;
                        }
                    }
                    helper.assertTrue(typeMatched == 9,
                            "type-matched census must see the spider AND its 8 fed parts, got " + typeMatched);
                }
                if (t == 110) {
                    helper.assertTrue(checked[0] >= 200,
                            "part-tracking test barely ran (" + checked[0] + " checks) — census broken");
                    spider.discard();
                    helper.succeed();
                }
            } catch (RuntimeException e) {
                spider.discard();
                throw e;
            }
        });
    }

    /**
     * Test 6 (design doc): equal damage through a leg part and through the
     * body of twin spiders yields equal health loss — the ×1.0 routing
     * contract (no new weak points, no shielding).
     */
    @GameTest(template = "empty", batch = "spiderGaitIsolation")
    public void s4_part_damage_routes_one_to_one(GameTestHelper helper) {
        OreSpawnConfig.SpiderMovement prior = OreSpawnConfig.SPIDER_MOVEMENT.get();
        try {
            OreSpawnConfig.SPIDER_MOVEMENT.set(OreSpawnConfig.SpiderMovement.MODERN);
            SpiderRobot viaPart = helper.spawnWithNoFreeWill(ModEntities.SPIDER_ROBOT.get(), new BlockPos(2, 2, 2));
            SpiderRobot viaBody = helper.spawnWithNoFreeWill(ModEntities.SPIDER_ROBOT.get(), new BlockPos(2, 2, 8));
            MHLibPartEntity<?> part = legPart(viaPart, 0);
            helper.assertTrue(part != null, "leg0 part missing");
            // SOURCED damage (a mob attack) — the player-path shape. The
            // first cut used generic (source-less) damage, which the S4
            // environmental routing rule correctly drops via parts; real
            // directed damage always carries a source entity.
            DamageSource attack = helper.getLevel().damageSources().mobAttack(viaBody);
            part.hurt(attack, 50.0f);
            viaBody.hurt(helper.getLevel().damageSources().mobAttack(viaPart), 50.0f);
            helper.assertTrue(viaPart.getHealth() < viaPart.getMaxHealth(),
                    "damage through the part never reached the spider");
            helper.assertValueEqual(viaPart.getHealth(), viaBody.getHealth(),
                    "part-routed vs body damage (x1.0 contract, test 6)");
            viaPart.discard();
            viaBody.discard();
        } finally {
            OreSpawnConfig.SPIDER_MOVEMENT.set(prior);
        }
        helper.succeed();
    }

    /**
     * Test 8 (design doc): gait/part state is transient by design (MOD-022
     * family) — after an NBT round trip the reloaded spider RE-SETTLES: all
     * feet re-plant and all 8 parts sit within leg reach of the body.
     */
    @GameTest(template = "empty_large", timeoutTicks = 400, batch = "spiderGaitIsolation")
    public void s4_nbt_roundtrip_parts_resettle(GameTestHelper helper) {
        final SpiderRobot first = spawnModern(helper, new BlockPos(24, 3, 24));
        final int[] tick = {0};
        final SpiderRobot[] reloaded = {null};
        helper.onEachTick(() -> {
            try {
                int t = ++tick[0];
                if (t == 30) {
                    CompoundTag tag = first.saveWithoutId(new CompoundTag());
                    // Identity stays with the freshly spawned entity — loading
                    // the old UUID onto an already-registered entity corrupts
                    // the level's byUuid index for the rest of the run
                    // (review); this test exercises gait/part transience only.
                    tag.remove("UUID");
                    first.discard();
                    SpiderRobot second = spawnModern(helper, new BlockPos(24, 3, 24));
                    second.load(tag);
                    reloaded[0] = second;
                    return;
                }
                if (t == 90) {
                    SpiderRobot second = reloaded[0];
                    helper.assertTrue(second != null && second.isAlive(), "reloaded spider missing");
                    ModernSpiderGait gait = second.getModernGait();
                    helper.assertTrue(gait != null, "reloaded spider lost the gait controller");
                    helper.assertTrue(second.getParts() != null && second.getParts().length == 8,
                            "reloaded spider must carry 8 parts");
                    for (int leg = 0; leg < SpiderRigProfile.LEG_COUNT; ++leg) {
                        helper.assertTrue(gait.isGrounded(leg),
                                "leg " + leg + " not re-planted after the NBT round trip (test 8)");
                    }
                    for (PartEntity<?> part : second.getParts()) {
                        double d = Math.sqrt(second.distanceToSqr(part.getX(), part.getY(), part.getZ()));
                        helper.assertTrue(d < SpiderRigProfile.MAX_REACH + 2.0,
                                "part " + d + " blocks from the reloaded body — feed not re-settled");
                    }
                    second.discard();
                    helper.succeed();
                }
            } catch (RuntimeException e) {
                if (first.isAlive()) {
                    first.discard();
                }
                if (reloaded[0] != null) {
                    reloaded[0].discard();
                }
                throw e;
            }
        });
    }

    /**
     * The owner-directed before/after observation for the vendored
     * updateSynching + tracking-hook gates (honest wording per review: the
     * first gate alone covered only the re-election LOOP; the tracking-start/
     * stop hooks and the client keepalive branch are now gated too). This
     * test drives the tracker queue DIRECTLY (no players exist in gametests)
     * to exercise the updateSynching election branch: a boneless-profile
     * spider must not elect there, while TheQueen (sync-with-model=true)
     * elects exactly as before — election being the precondition of every
     * SPacketSetMaster broadcast, this is the packet-level neutrality
     * observable available server-side.
     */
    @GameTest(template = "empty_large", timeoutTicks = 400, batch = "spiderGaitIsolation")
    public void s4_master_election_gated_for_boneless_profiles(GameTestHelper helper) {
        final SpiderRobot spider = spawnModern(helper, new BlockPos(10, 2, 10));
        final TheQueen queen = helper.spawnWithNoFreeWill(ModEntities.THE_QUEEN.get(), new BlockPos(34, 2, 34));
        Object spiderObj = spider;
        Object queenObj = queen;
        if (spiderObj instanceof IMHLibFieldAccessor<?> spiderAccess) {
            spiderAccess._mhlibAccess_getTrackerQueue().add(UUID.randomUUID());
        }
        if (queenObj instanceof IMHLibFieldAccessor<?> queenAccess) {
            queenAccess._mhlibAccess_getTrackerQueue().add(UUID.randomUUID());
        }
        final int[] tick = {0};
        helper.onEachTick(() -> {
            try {
                if (++tick[0] < 30) {
                    return;
                }
                UUID spiderMaster = spiderObj instanceof IMultipartEntity<?> mp ? mp.getMasterUUID() : null;
                UUID queenMaster = queenObj instanceof IMultipartEntity<?> mq ? mq.getMasterUUID() : null;
                helper.assertTrue(spiderMaster == null,
                        "boneless-profile spider elected a bone-stream master — updateSynching gate broken");
                helper.assertTrue(queenMaster != null,
                        "TheQueen no longer elects a master — the vendored gate is NOT Queen-neutral");
                spider.discard();
                queen.discard();
                helper.succeed();
            } catch (RuntimeException e) {
                spider.discard();
                queen.discard();
                throw e;
            }
        });
    }

    /**
     * The part→parent sweep backing the HUD unwrap: every part of every
     * part-bearing entity (King manual parts, Godzilla manual parts, Queen
     * MHLib parts, modern spider legs) resolves to its parent — the
     * assumption the crosshair overlay's PartEntity unwrap rests on.
     */
    @GameTest(template = "empty_large", timeoutTicks = 400, batch = "spiderGaitIsolation")
    public void s4_part_parent_sweep_for_hud_unwrap(GameTestHelper helper) {
        boolean priorNice = OreSpawnConfig.PLAY_NICELY.get();
        OreSpawnConfig.SpiderMovement priorMove = OreSpawnConfig.SPIDER_MOVEMENT.get();
        try {
            OreSpawnConfig.PLAY_NICELY.set(false);
            OreSpawnConfig.SPIDER_MOVEMENT.set(OreSpawnConfig.SpiderMovement.MODERN);
            TheKing king = helper.spawnWithNoFreeWill(ModEntities.THE_KING.get(), new BlockPos(10, 3, 10));
            Godzilla godzilla = helper.spawnWithNoFreeWill(ModEntities.GODZILLA.get(), new BlockPos(36, 3, 36));
            TheQueen queen = helper.spawnWithNoFreeWill(ModEntities.THE_QUEEN.get(), new BlockPos(10, 3, 38));
            SpiderRobot spider = helper.spawnWithNoFreeWill(ModEntities.SPIDER_ROBOT.get(), new BlockPos(38, 3, 10));
            assertAllPartsParent(helper, king, 5, "TheKing");
            helper.assertTrue(godzilla.getParts() != null && godzilla.getParts().length > 0,
                    "Godzilla has no parts to sweep");
            assertAllPartsParent(helper, godzilla, godzilla.getParts().length, "Godzilla");
            assertAllPartsParent(helper, queen, 10, "TheQueen");
            assertAllPartsParent(helper, spider, 8, "modern SpiderRobot");
            king.discard();
            godzilla.discard();
            queen.discard();
            spider.discard();
        } finally {
            OreSpawnConfig.PLAY_NICELY.set(priorNice);
            OreSpawnConfig.SPIDER_MOVEMENT.set(priorMove);
        }
        helper.succeed();
    }

    /**
     * PROJECT-LAW test ("a test must exercise the path the player uses, not
     * the API beneath it"): a REAL arrow flown into a planted leg must hit
     * it and damage the spider — the exact reachability the first S4 cut
     * silently lacked (collidable:false made parts invisible to every pick
     * path while a direct part.hurt() test stayed green). Also pins the two
     * gate predicates those player paths consult.
     */
    @GameTest(template = "empty_large", timeoutTicks = 400, batch = "spiderGaitIsolation")
    public void s4_arrow_hits_leg_part(GameTestHelper helper) {
        final SpiderRobot spider = spawnModern(helper, new BlockPos(24, 2, 24));
        final int[] tick = {0};
        helper.onEachTick(() -> {
            try {
                int t = ++tick[0];
                spider.setYRot(0.0f);
                if (t < 30) {
                    return;
                }
                if (t == 30) {
                    MHLibPartEntity<?> part = legPart(spider, 1); // +X-side front leg
                    helper.assertTrue(part != null, "leg1 part missing");
                    helper.assertTrue(part.isPickable(),
                            "leg part not pickable — the melee/ray gate is closed (S4 isPickable fix)");
                    helper.assertTrue(part.canBeHitByProjectile(),
                            "leg part not projectile-hittable — canBeHitByProjectile gate closed");
                    // Fire a real arrow from beyond the leg, flying inward at
                    // the part's center: first pickable surface on the path
                    // is the leg (the body is ~16 blocks further in).
                    double px = part.getX();
                    double py = part.getY() + 0.3;
                    double pz = part.getZ();
                    double dirX = px - spider.getX();
                    double dirZ = pz - spider.getZ();
                    double len = Math.sqrt(dirX * dirX + dirZ * dirZ);
                    dirX /= len;
                    dirZ /= len;
                    net.minecraft.world.entity.projectile.Arrow arrow =
                            net.minecraft.world.entity.EntityType.ARROW.create(helper.getLevel());
                    helper.assertTrue(arrow != null, "arrow create failed");
                    arrow.setPos(px + dirX * 4.0, py, pz + dirZ * 4.0);
                    arrow.setDeltaMovement(-dirX * 1.4, 0.0, -dirZ * 1.4);
                    arrow.setNoGravity(true);
                    helper.getLevel().addFreshEntity(arrow);
                    return;
                }
                if (t == 40) {
                    // Assert BEFORE the earliest possible body impact (~t45
                    // at 1.4 b/t over the ~20-block ray to the body): the
                    // damage must have come from the LEG, so this pins the
                    // level-query half of the pick path too (fix-review).
                    helper.assertTrue(spider.getHealth() < spider.getMaxHealth(),
                            "a real arrow through a planted leg never damaged the spider (player-path law)");
                    spider.discard();
                    helper.succeed();
                }
            } catch (RuntimeException e) {
                spider.discard();
                throw e;
            }
        });
    }

    /**
     * The S4 lava routing rule, both branches. Spider branch is PLAYER-PATH:
     * a modern spider standing at a lava pool with legs planted/dangling in
     * it (a scenario classic's visual-only legs shrugged off) takes ZERO
     * damage — the environment acts on the body, not the honest-surface
     * legs. Queen branch pins the rule's other arm at the routing seam
     * (parts are her ONLY damage channel, so environmental damage must
     * still route) — API-direct by necessity: staging a live Queen in lava
     * is not a deterministic gametest, and her branch is a single guarded
     * conditional.
     */
    @GameTest(template = "empty_large", timeoutTicks = 400, batch = "spiderGaitIsolation")
    public void s4_lava_never_routes_through_spider_legs(GameTestHelper helper) {
        // Elevated platform (surface rel 5) beside a two-layer lava pool at
        // the same surface height: the spider's +X legs plant into the pool.
        for (int x = 2; x <= 20; ++x) {
            for (int z = 4; z <= 44; ++z) {
                for (int y = 0; y <= 4; ++y) {
                    helper.setBlock(new BlockPos(x, y, z), net.minecraft.world.level.block.Blocks.STONE);
                }
            }
        }
        for (int x = 22; x <= 40; ++x) {
            for (int z = 4; z <= 44; ++z) {
                for (int y = 0; y <= 2; ++y) {
                    helper.setBlock(new BlockPos(x, y, z), net.minecraft.world.level.block.Blocks.STONE);
                }
                helper.setBlock(new BlockPos(x, 3, z), net.minecraft.world.level.block.Blocks.LAVA);
                helper.setBlock(new BlockPos(x, 4, z), net.minecraft.world.level.block.Blocks.LAVA);
            }
        }
        final SpiderRobot spider = spawnModern(helper, new BlockPos(10, 6, 24));
        final int[] tick = {0};
        final boolean[] sawPartInLava = {false};
        helper.onEachTick(() -> {
            try {
                int t = ++tick[0];
                spider.setYRot(0.0f); // +X legs face the pool
                if (t < 30) {
                    return;
                }
                // GEOMETRIC census — part boxes standing in lava blocks.
                // (Entity.isInLava() is useless here and that is itself an
                // S4 finding: MHLib's alignSubParts stomp re-stacks parts at
                // the BODY position before they tick, so parts fluid-sample
                // at the body every tick and never register leg-position
                // fluids — the tick-ordering that makes the lava channel
                // unreachable in production. The routing rule remains the
                // defensive second wall; this test pins the player-visible
                // outcome over the real scenario.)
                if (spider.getParts() != null) {
                    for (PartEntity<?> part : spider.getParts()) {
                        if (helper.getLevel().getBlockState(net.minecraft.core.BlockPos.containing(
                                        part.getX(), part.getY() + 0.1, part.getZ()))
                                .is(net.minecraft.world.level.block.Blocks.LAVA)) {
                            sawPartInLava[0] = true;
                        }
                    }
                }
                if (t == 90) {
                    StringBuilder diag = new StringBuilder();
                    if (spider.getParts() != null) {
                        for (PartEntity<?> part : spider.getParts()) {
                            diag.append(String.format(" [%.1f,%.1f,%.1f %s inLava=%s]",
                                    part.getX(), part.getY(), part.getZ(),
                                    helper.getLevel().getBlockState(
                                            net.minecraft.core.BlockPos.containing(
                                                    part.getX(), part.getY() + 0.3, part.getZ()))
                                            .getBlock().getName().getString(),
                                    part.isInLava()));
                        }
                    }
                    helper.assertTrue(sawPartInLava[0],
                            "census broken: no leg part ever touched the lava pool — body ("
                                    + String.format("%.1f,%.1f,%.1f yaw %.0f", spider.getX(),
                                            spider.getY(), spider.getZ(), spider.getYRot())
                                    + ") parts:" + diag);
                    helper.assertTrue(spider.getHealth() >= spider.getMaxHealth(),
                            "lava damaged the spider through a leg part — classic never took this (S4 rule)");
                    // Queen branch: environmental damage must STILL route
                    // (main hitbox cannot receive damage; parts are her only
                    // channel).
                    TheQueen queen = helper.spawnWithNoFreeWill(ModEntities.THE_QUEEN.get(), new BlockPos(10, 7, 40));
                    float before = queen.getHealth();
                    Object queenObj = queen;
                    if (queenObj instanceof IMultipartEntity<?> mq) {
                        MHLibPartEntity<?> queenPart = mq.getPartByName("Body1").orElse(null);
                        helper.assertTrue(queenPart != null, "Queen Body1 part missing");
                        queenPart.hurt(helper.getLevel().damageSources().lava(), 4.0f);
                    }
                    helper.assertTrue(queen.getHealth() < before,
                            "environmental damage stopped routing through Queen parts — rule not Queen-safe");
                    // Direct pin of the spider DROP arm at the same seam
                    // (fix-review: the geometric outcome above is
                    // overdetermined by tick-ordering; this catches a
                    // deleted return-false branch outright).
                    MHLibPartEntity<?> spiderLeg = legPart(spider, 1);
                    helper.assertTrue(spiderLeg != null, "leg1 missing for the drop-arm pin");
                    float beforeSpider = spider.getHealth();
                    spiderLeg.hurt(helper.getLevel().damageSources().lava(), 4.0f);
                    helper.assertTrue(spider.getHealth() >= beforeSpider,
                            "source-less damage routed through a spider leg — env rule's drop arm broken");
                    queen.discard();
                    spider.discard();
                    helper.succeed();
                }
            } catch (RuntimeException e) {
                spider.discard();
                throw e;
            }
        });
    }

    private static void assertAllPartsParent(GameTestHelper helper, Entity parent,
                                             int expectedCount, String label) {
        helper.assertTrue(parent.getParts() != null && parent.getParts().length == expectedCount,
                label + " part count != " + expectedCount + " (got "
                        + (parent.getParts() == null ? 0 : parent.getParts().length) + ")");
        for (PartEntity<?> part : parent.getParts()) {
            helper.assertTrue(part.getParent() == parent,
                    label + " part does not resolve to its parent (HUD unwrap contract)");
        }
    }
}
