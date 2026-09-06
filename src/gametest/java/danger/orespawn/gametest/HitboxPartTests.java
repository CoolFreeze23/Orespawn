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
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
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

    /**
     * Spawns a MODERN spider. Master-override ruling 2026-09-04: the
     * spiderMovement key is inert while {@code [modern] enabled} is off, so
     * the master is raised with the key and both are restored together once
     * the construction snapshot is taken.
     */
    private static SpiderRobot spawnModern(GameTestHelper helper, BlockPos pos) {
        boolean priorMaster = OreSpawnConfig.MODERN_ENABLED.get();
        OreSpawnConfig.SpiderMovement prior = OreSpawnConfig.SPIDER_MOVEMENT.get();
        try {
            OreSpawnConfig.MODERN_ENABLED.set(true);
            OreSpawnConfig.SPIDER_MOVEMENT.set(OreSpawnConfig.SpiderMovement.MODERN);
            return helper.spawn(ModEntities.SPIDER_ROBOT.get(), pos);
        } finally {
            OreSpawnConfig.SPIDER_MOVEMENT.set(prior);
            OreSpawnConfig.MODERN_ENABLED.set(priorMaster);
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
     * forced routing), and typed queries see no part contamination. Plus
     * the ENT-S-088 dims pin, both modes: 3.25×2.25 (orig
     * SpiderRobot.java:58) — the modern arm is the Size-hook trap (MHLib
     * applies the PROFILE main size via EntityEvent.Size, so a profile
     * drift would silently resize every modern spider; the ant's mirror
     * pin is s5b_ant_mode_gate_and_dims).
     */
    @GameTest(template = "empty", batch = "spiderGaitIsolation")
    public void s4_part_counts_and_classic_zero(GameTestHelper helper) {
        boolean priorMaster = OreSpawnConfig.MODERN_ENABLED.get();
        OreSpawnConfig.SpiderMovement prior = OreSpawnConfig.SPIDER_MOVEMENT.get();
        try {
            // Classic with the master off; modern below with the master on
            // (master-override ruling 2026-09-04).
            OreSpawnConfig.MODERN_ENABLED.set(false);
            OreSpawnConfig.SPIDER_MOVEMENT.set(OreSpawnConfig.SpiderMovement.CLASSIC);
            SpiderRobot classic = helper.spawnWithNoFreeWill(ModEntities.SPIDER_ROBOT.get(), new BlockPos(2, 2, 2));
            helper.assertTrue(classic.getParts() == null || classic.getParts().length == 0,
                    "CLASSIC spider constructed MHLib parts (D3 zero-parts law)");
            helper.assertFalse(classic.isMultipartEntity(), "CLASSIC spider reports multipart");
            helper.assertTrue(classic.isPickable(), "CLASSIC spider must stay pickable");
            helper.assertTrue(classic.getBbWidth() == 3.25f && classic.getBbHeight() == 2.25f,
                    "classic spider dims drifted from orig SpiderRobot.java:58 (ENT-S-088): "
                            + classic.getBbWidth() + "x" + classic.getBbHeight());

            OreSpawnConfig.MODERN_ENABLED.set(true);
            OreSpawnConfig.SPIDER_MOVEMENT.set(OreSpawnConfig.SpiderMovement.MODERN);
            SpiderRobot modern = helper.spawnWithNoFreeWill(ModEntities.SPIDER_ROBOT.get(), new BlockPos(2, 2, 6));
            helper.assertTrue(modern.getParts() != null && modern.getParts().length == 8,
                    "MODERN spider must carry exactly 8 leg parts, got "
                            + (modern.getParts() == null ? 0 : modern.getParts().length));
            helper.assertTrue(modern.isMultipartEntity(), "MODERN spider must report multipart");
            helper.assertTrue(modern.isPickable(),
                    "MODERN spider body must STAY pickable (profile main canReceiveDamage=true)");
            helper.assertTrue(modern.getBbWidth() == 3.25f && modern.getBbHeight() == 2.25f,
                    "modern spider dims drifted (Size-hook trap, ENT-S-088): "
                            + modern.getBbWidth() + "x" + modern.getBbHeight());
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
            OreSpawnConfig.MODERN_ENABLED.set(priorMaster);
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
        boolean priorMaster = OreSpawnConfig.MODERN_ENABLED.get();
        OreSpawnConfig.SpiderMovement prior = OreSpawnConfig.SPIDER_MOVEMENT.get();
        try {
            OreSpawnConfig.MODERN_ENABLED.set(true);
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
            OreSpawnConfig.MODERN_ENABLED.set(priorMaster);
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
        boolean priorMaster = OreSpawnConfig.MODERN_ENABLED.get();
        OreSpawnConfig.SpiderMovement priorMove = OreSpawnConfig.SPIDER_MOVEMENT.get();
        try {
            OreSpawnConfig.PLAY_NICELY.set(false);
            OreSpawnConfig.MODERN_ENABLED.set(true);
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
            OreSpawnConfig.MODERN_ENABLED.set(priorMaster);
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

    /**
     * MHLib harvest 2 (2026-09-06, wave 5; the MoreHitboxes evaluation section 3.5 (1) / section 4 row 2):
     * {@code Player.attack} on an {@link MHLibPartEntity} -- the melee packet's shape
     * ({@code ServerGamePacketListenerImpl.handleInteract} resolves the crosshair id to the part and calls
     * exactly this) -- lands on the PARENT for everything but the damage, which still enters through the
     * part and its damage modifier ({@code de.dertoaster.multihitboxlib.mixin.minecraft.MixinPlayer}: the
     * argument swapped to the parent at HEAD, the receiver of the inner {@code Entity.hurt} swapped back to
     * the part). NeoForge 21.1.223's own unwrap in {@code Player.attack} (offsets 1109-1126) feeds only the
     * weapon's {@code hurtEnemy} / {@code postHurtEnemy}.
     *
     * <p>Spider half (the push is measurable: KNOCKBACK_RESISTANCE zeroed in-row): a sprinting mock player 3
     * blocks at -z of a modern spider, facing +z (yaw 0), ATTACK_DAMAGE 10, ATTACK_SPEED raised so the
     * attack-strength scale is 1 without ticking the off-level mock player, hits leg0. Health drops by exactly
     * 10 x the leg's damage-modifier 1.0 (ARMOR zeroed) -- the part path; {@code getLastHurtMob()} is the
     * spider (HEAD: null -- {@code setLastHurtMob} stores a LivingEntity only, and the part is none); the
     * spider's deltaMovement.z is {@code (double) 0.4F / 2 + 0.5}: the hurt path's own 0.4F knockback away
     * from the player ({@code LivingEntity.hurt}, both trees) halved by the attack's sprint knockback 1 x 0.5
     * along the player's facing ({@code LivingEntity.knockback}: {@code v / 2 - dir x strength}) -- HEAD put
     * that 0.5 on the part's never-integrated deltaMovement, so the spider read 0.4F alone; x stays 0 (the
     * player straight behind: sin 0 = 0); y is not pinned (the onGround branch).
     *
     * <p>Queen half (the modifier is visible: Lwing1 is 0.25): the same blow on the wing arrives as exactly 2.5 before
     * armour and drops her health by what vanilla's {@code CombatRules.getDamageAfterAbsorb} leaves of it under her
     * constant {@code getArmorValue()} (the 1.7.10 DEFENSE_VALUE; the ARMOR attribute is not consulted -- the w5b gate),
     * records her as the last hurt mob, and moves her not at all -- her
     * KNOCKBACK_RESISTANCE 1.0 absorbs both pushes ({@code LivingEntity.knockback} scales the strength to 0).
     * {@code s4_part_damage_routes_one_to_one} (the direct {@code part.hurt} path) is unchanged.
     *
     * <p>Nothing here ticks (every read sits in the spawn's own synchronous stretch), so neither mob is frozen
     * against gravity -- refuter B's freeze applies to the ticking rows. No row covers the sweep (refuter A,
     * 2026-09-06): after the swap the sweep box is the PARENT's box inflated (1, 0.25, 1) -- 24 x 24.5 x 24 around
     * the hostile Queen's body, not around the struck wing -- and the parent is excluded from its candidates (at
     * HEAD it was one, taking the sweep's knockback(0.4) while its LivingEntity.hurt fell to the i-frames);
     * recorded in the FIX_LOG, not pinned.
     */
    @GameTest(template = "empty_large", batch = "spiderGaitIsolation")
    public void s4_player_attack_lands_on_the_parent_through_a_part(GameTestHelper helper) {
        boolean priorMaster = OreSpawnConfig.MODERN_ENABLED.get();
        OreSpawnConfig.SpiderMovement prior = OreSpawnConfig.SPIDER_MOVEMENT.get();
        SpiderRobot spider = null;
        TheQueen queen = null;
        try {
            OreSpawnConfig.MODERN_ENABLED.set(true);
            OreSpawnConfig.SPIDER_MOVEMENT.set(OreSpawnConfig.SpiderMovement.MODERN);
            spider = helper.spawnWithNoFreeWill(ModEntities.SPIDER_ROBOT.get(), new BlockPos(12, 1, 12));
        } finally {
            OreSpawnConfig.SPIDER_MOVEMENT.set(prior);
            OreSpawnConfig.MODERN_ENABLED.set(priorMaster);
        }
        try {
            MHLibPartEntity<?> leg = legPart(spider, 0);
            helper.assertTrue(leg != null, "leg0 part missing");
            helper.assertTrue(leg.getConfig().damageModifier() == 1.0f, "precondition: the spider leg's damage-modifier is 1.0"
                    + " (spider_robot.json), actual " + leg.getConfig().damageModifier());
            spider.getAttribute(Attributes.ARMOR).setBaseValue(0.0D);
            spider.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(0.0D);
            spider.setDeltaMovement(Vec3.ZERO);
            Player player = attacker(helper, spider.getX(), spider.getY(), spider.getZ() - 3.0D);
            float before = spider.getHealth();
            player.attack(leg);
            helper.assertTrue(spider.getHealth() == before - 10.0f, "harvest 2: 10 damage through leg0 (modifier 1.0, armor 0)"
                    + " must reach the spider through MHLibPartEntity.hurt, expected " + (before - 10.0f) + ", actual " + spider.getHealth());
            helper.assertTrue(player.getLastHurtMob() == spider, "harvest 2: Player.attack must record the PARENT as the last hurt"
                    + " mob (HEAD: null, the part is no LivingEntity), actual " + player.getLastHurtMob());
            final double expectedZ = ((double) 0.4F) / 2.0D + 0.5D;
            Vec3 motion = spider.getDeltaMovement();
            helper.assertTrue(motion.x == 0.0D && motion.z == expectedZ, "harvest 2: the sprint knockback must push the PARENT --"
                    + " deltaMovement (0, ?, (double) 0.4F / 2 + 0.5 = " + expectedZ + "); HEAD pushed the part and the spider kept"
                    + " the hurt path's 0.4F alone; actual " + motion);
            spider.discard();
            spider = null;

            queen = helper.spawnWithNoFreeWill(ModEntities.THE_QUEEN.get(), new BlockPos(36, 1, 36));
            MHLibPartEntity<?> wing = ((IMultipartEntity<?>) (Object) queen).getPartByName("Lwing1").orElse(null);
            helper.assertTrue(wing != null, "Queen Lwing1 part missing");
            helper.assertTrue(wing.getConfig().damageModifier() == 0.25f, "precondition: Lwing1's damage-modifier is 0.25"
                    + " (the_queen.json), actual " + wing.getConfig().damageModifier());
            helper.assertTrue(queen.getAttributeBaseValue(Attributes.KNOCKBACK_RESISTANCE) == 1.0D,
                    "precondition: the Queen's KNOCKBACK_RESISTANCE is 1.0 (TheQueen.createAttributes)");
            // TheQueen.getArmorValue() is the 1.7.10 constant DEFENSE_VALUE (MobStats; +2 below 2/3 health) and never
            // consults the ARMOR attribute, so the 2.5 that arrives through the part is then armour-absorbed by vanilla's own
            // CombatRules.getDamageAfterAbsorb (LivingEntity.getDamageAfterArmorAbsorb) -- the w5b gate: 0.525 arrived, not 2.5.
            // The pin computes the same call, so it is exact whatever DEFENSE_VALUE is.
            queen.setDeltaMovement(Vec3.ZERO);
            Player second = attacker(helper, queen.getX(), queen.getY(), queen.getZ() - 3.0D);
            float queenBefore = queen.getHealth();
            DamageSource queenSource = queen.damageSources().playerAttack(second);
            float afterArmor = net.minecraft.world.damagesource.CombatRules.getDamageAfterAbsorb(queen, 2.5f, queenSource,
                    (float) queen.getArmorValue(), (float) queen.getAttributeValue(Attributes.ARMOR_TOUGHNESS));
            helper.assertTrue(afterArmor > 0.0f && afterArmor < 2.5f, "precondition: the Queen's constant armour (getArmorValue "
                    + queen.getArmorValue() + ") absorbs part of 2.5, leaving " + afterArmor);
            second.attack(wing);
            helper.assertTrue(queen.getHealth() == queenBefore - afterArmor, "harvest 2: 10 damage through Lwing1 must arrive as 10 x 0.25"
                    + " = 2.5 before armour (the part's modifier still applies with the receiver swapped back), then vanilla's armour"
                    + " formula on TheQueen.getArmorValue() " + queen.getArmorValue() + " leaves " + afterArmor + "; expected "
                    + (queenBefore - afterArmor) + ", actual " + queen.getHealth());
            helper.assertTrue(second.getLastHurtMob() == queen, "harvest 2: the Queen is the last hurt mob, actual " + second.getLastHurtMob());
            helper.assertTrue(queen.getDeltaMovement().equals(Vec3.ZERO), "harvest 2: the Queen's KNOCKBACK_RESISTANCE 1.0 absorbs"
                    + " the unwrapped push (strength x (1 - 1.0) = 0), actual " + queen.getDeltaMovement());
        } finally {
            if (spider != null) {
                spider.discard();
            }
            if (queen != null) {
                queen.discard();
            }
        }
        helper.succeed();
    }

    /** A sprinting survival mock player at (x, y, z) facing +z with ATTACK_DAMAGE 10 and the attack-strength scale saturated. */
    private static Player attacker(GameTestHelper helper, double x, double y, double z) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setPos(x, y, z);
        player.setYRot(0.0F);
        player.setXRot(0.0F);
        player.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(10.0D);
        // getAttackStrengthScale(0.5) = clamp((ticker + 0.5) / (20 / ATTACK_SPEED), 0, 1): a huge attack speed saturates
        // it without ticking the (unticked, off-level) mock player.
        player.getAttribute(Attributes.ATTACK_SPEED).setBaseValue(1.0E6D);
        player.setSprinting(true);
        return player;
    }

    /**
     * OPT-013 / MHLib harvest 3 (2026-09-06, wave 5; the evaluation section 3.4): the conservative frustum
     * box. R is the profile's rest-pose reach -- the maximum over the parts of {@code |position| + |pivot| +
     * sqrt(w^2/2 + h^2)} ({@code MHLibPartEntity.mhlibRestReach}, computed once at construction from the parts
     * the profile built) -- and the box the CLIENT caches ({@code IMultipartEntity.mhlibCacheCullBox}: from
     * {@code tickParts} at LivingEntity.tick's TAIL, and again from a gait-fed robot's tick after its client
     * mirror moved the legs) is the body box, the cube position +- R x scale and the parts' live boxes unioned,
     * returned by {@code getBoundingBoxForCulling()} ({@code MixinLivingEntity}). The SERVER caches nothing
     * (refuters A / B, 2026-09-06: nothing calls getBoundingBoxForCulling there), which this server-side row
     * pins directly; the union itself is read through the public, side-effect-free
     * {@code mhlibComputeCullBox()} over the live server parts -- the same formula over the same kind of boxes
     * the client caches (the client path is beyond a server gametest: javap of the modifier and the mixin gate
     * cover it).
     *
     * <p>Queen (frozen with setNoGravity -- refuter B: spawnWithNoFreeWill removes goals and brain behaviours,
     * not gravity, and she is noPhysics): R from the_queen.json by hand is Lwing1's {@code |(5, 18.15, 6.25)| +
     * |(-22.18, 5.13, 8.78)| + sqrt(44.94^2 / 2 + 10.63^2)} = 19.836 + 24.400 + 33.508 = 77.744 (the
     * evaluation's "about 78"); the row pins the entity's radius to that hand number (the sizes travel as
     * floats: within 1e-3) and to the maximum of the parts' own {@code mhlibRestReach} (exact), then -- after two
     * level ticks with no master streaming, so the parts sit at the profile's fallback rest offsets
     * ({@code alignSynchedSubParts}) -- that the server holds no cached box and {@code getBoundingBoxForCulling()}
     * is her vanilla body box, and that every part's live box and her body box lie inside the R cube ALONE (the
     * formula's claim, not the union's) and inside the computed union. Modern spider (profile resolved: parts
     * exist; left on the template floor under gravity, where the gait's ground scan wants it): R = sqrt(0.6^2 /
     * 2 + 0.6^2) = 0.735 (rest offsets all zero); the rest-pose leg boxes (config offset 0, size 0.6 around the
     * body position) inside the R cube; the FED legs ({@code SpiderRobot.tick}'s {@code serverTick ->
     * feedParts} after {@code super.tick()} -- the same solve the client mirror places them by) inside the
     * computed union, and at least one of them OUTSIDE the R cube (the rig's segments are 99/16 blocks): the
     * union's own contract, and the reason a box cached before the feed -- refuter B's blocker -- held the
     * previous tick's legs. Classic spider (no profile): radius 0, no box, {@code getBoundingBoxForCulling()}
     * is the vanilla body box -- vanilla culling untouched.
     */
    @GameTest(template = "empty_large", timeoutTicks = 100, batch = "spiderGaitIsolation")
    public void s4_cull_box_bounds_the_rest_pose_parts(GameTestHelper helper) {
        final TheQueen queen = helper.spawnWithNoFreeWill(ModEntities.THE_QUEEN.get(), new BlockPos(24, 1, 24));
        // wave 5 (refuter B): spawnWithNoFreeWill leaves gravity on and the noPhysics Queen would sink 0.047 a tick; the containment
        // pins below are relative and would hold either way -- frozen for hygiene. The spiders stay on the floor under gravity.
        queen.setNoGravity(true);
        final SpiderRobot modern;
        final SpiderRobot classic;
        boolean priorMaster = OreSpawnConfig.MODERN_ENABLED.get();
        OreSpawnConfig.SpiderMovement prior = OreSpawnConfig.SPIDER_MOVEMENT.get();
        try {
            OreSpawnConfig.MODERN_ENABLED.set(true);
            OreSpawnConfig.SPIDER_MOVEMENT.set(OreSpawnConfig.SpiderMovement.MODERN);
            modern = helper.spawnWithNoFreeWill(ModEntities.SPIDER_ROBOT.get(), new BlockPos(6, 1, 6));
            OreSpawnConfig.SPIDER_MOVEMENT.set(OreSpawnConfig.SpiderMovement.CLASSIC);
            classic = helper.spawnWithNoFreeWill(ModEntities.SPIDER_ROBOT.get(), new BlockPos(42, 1, 6));
        } finally {
            OreSpawnConfig.SPIDER_MOVEMENT.set(prior);
            OreSpawnConfig.MODERN_ENABLED.set(priorMaster);
        }
        helper.runAfterDelay(2, () -> {
            try {
                // --- the Queen: R by hand, R from the parts; the server caches nothing; the rest-pose boxes inside the cube and the union
                helper.assertTrue(queen.getParts() != null && queen.getParts().length == 10, "precondition: ten Queen parts");
                IMHLibFieldAccessor<?> queenAccess = (IMHLibFieldAccessor<?>) (Object) queen;
                double radius = queenAccess._mhlibAccess_getCullRadius();
                double byHand = Math.sqrt(5.0D * 5.0D + 18.15D * 18.15D + 6.25D * 6.25D)
                        + Math.sqrt(22.18D * 22.18D + 5.13D * 5.13D + 8.78D * 8.78D)
                        + Math.sqrt(44.94D * 44.94D / 2.0D + 10.63D * 10.63D);
                helper.assertTrue(Math.abs(radius - byHand) < 1.0E-3D, "harvest 3: the Queen's cull radius is Lwing1's |position| +"
                        + " |pivot| + far corner from the_queen.json = " + byHand + " (the evaluation's ~78), actual " + radius);
                double fromParts = 0.0D;
                for (PartEntity<?> part : queen.getParts()) {
                    fromParts = Math.max(fromParts, ((MHLibPartEntity<?>) part).mhlibRestReach());
                }
                helper.assertTrue(radius == fromParts, "harvest 3: the radius is the maximum of the parts' own rest reach, expected "
                        + fromParts + ", actual " + radius);
                double scale = queen.mhlibGetEntitySizeScale(queen);
                AABB cube = new AABB(queen.getX() - radius * scale, queen.getY() - radius * scale, queen.getZ() - radius * scale,
                        queen.getX() + radius * scale, queen.getY() + radius * scale, queen.getZ() + radius * scale);
                helper.assertTrue(queenAccess._mhlibAccess_getCullBox() == null && queen.getBoundingBoxForCulling().equals(queen.getBoundingBox()),
                        "harvest 3: the SERVER caches no cull box (mhlibCacheCullBox is gated on isClientSide: nothing calls"
                                + " getBoundingBoxForCulling there) and the modifier passes vanilla's body box through; actual cached "
                                + queenAccess._mhlibAccess_getCullBox() + ", answered " + queen.getBoundingBoxForCulling());
                AABB cull = ((IMultipartEntity<?>) (Object) queen).mhlibComputeCullBox();
                helper.assertTrue(cull != null, "harvest 3: a profiled entity computes a cull box (mhlibComputeCullBox), actual null");
                assertInside(helper, queen.getBoundingBox(), cube, "the Queen's body box inside the R cube");
                assertInside(helper, queen.getBoundingBox(), cull, "the Queen's body box inside the computed cull box");
                assertInside(helper, cube, cull, "the R cube inside the computed cull box");
                for (PartEntity<?> part : queen.getParts()) {
                    String name = ((MHLibPartEntity<?>) part).getConfigName();
                    helper.assertTrue(!(part.getX() == 0.0D && part.getY() == 0.0D && part.getZ() == 0.0D),
                            "precondition: part " + name + " was aligned (not at the world origin), actual " + part.position());
                    assertInside(helper, part.getBoundingBox(), cube, "the Queen's rest-pose part " + name + " inside the R cube");
                    assertInside(helper, part.getBoundingBox(), cull, "the Queen's part " + name + " inside the computed cull box");
                }
                // --- the modern spider: R from the zero rest offsets, the rest boxes inside the cube, the fed legs inside the union and beyond the cube
                helper.assertTrue(modern.getParts() != null && modern.getParts().length == 8, "precondition: eight modern spider parts");
                IMHLibFieldAccessor<?> spiderAccess = (IMHLibFieldAccessor<?>) (Object) modern;
                double spiderRadius = spiderAccess._mhlibAccess_getCullRadius();
                double spiderByHand = Math.sqrt(((double) 0.6F) * ((double) 0.6F) / 2.0D + ((double) 0.6F) * ((double) 0.6F));
                helper.assertTrue(spiderRadius == spiderByHand, "harvest 3: the spider's cull radius is sqrt(0.6^2 / 2 + 0.6^2) from"
                        + " spider_robot.json's zero offsets, expected " + spiderByHand + ", actual " + spiderRadius);
                AABB spiderCube = new AABB(modern.getX() - spiderRadius, modern.getY() - spiderRadius, modern.getZ() - spiderRadius,
                        modern.getX() + spiderRadius, modern.getY() + spiderRadius, modern.getZ() + spiderRadius);
                helper.assertTrue(spiderAccess._mhlibAccess_getCullBox() == null && modern.getBoundingBoxForCulling().equals(modern.getBoundingBox()),
                        "harvest 3: the server caches nothing for the modern spider either; actual cached " + spiderAccess._mhlibAccess_getCullBox()
                                + ", answered " + modern.getBoundingBoxForCulling());
                AABB spiderCull = ((IMultipartEntity<?>) (Object) modern).mhlibComputeCullBox();
                helper.assertTrue(spiderCull != null, "harvest 3: the modern spider computes a cull box, actual null");
                assertInside(helper, modern.getBoundingBox(), spiderCull, "the spider's body box inside the computed cull box");
                boolean anyLegBeyondCube = false;
                for (PartEntity<?> part : modern.getParts()) {
                    MHLibPartEntity<?> leg = (MHLibPartEntity<?>) part;
                    AABB rest = leg.getDimensions(Pose.STANDING).makeBoundingBox(
                            modern.position().add(leg.getConfigPositionOffset()).subtract(leg.getPivot()));
                    assertInside(helper, rest, spiderCube, "the spider's rest-pose " + leg.getConfigName() + " box inside the R cube");
                    assertInside(helper, part.getBoundingBox(), spiderCull,
                            "the spider's fed " + leg.getConfigName() + " inside the computed cull box (the union)");
                    anyLegBeyondCube |= !isInside(part.getBoundingBox(), spiderCube);
                }
                helper.assertTrue(anyLegBeyondCube, "harvest 3: the gait plants at least one fed leg beyond the 0.735-block R cube (the rig's"
                        + " 99/16-block segments) -- the union's reason, and the post-mirror re-cache's (refuter B); actual leg0 "
                        + modern.getParts()[0].getBoundingBox() + " vs the cube " + spiderCube);
                // --- the classic spider: no profile, no radius, no box, vanilla's answer
                helper.assertTrue(classic.getParts() == null || classic.getParts().length == 0, "precondition: the classic spider has no parts");
                IMHLibFieldAccessor<?> classicAccess = (IMHLibFieldAccessor<?>) (Object) classic;
                helper.assertTrue(classicAccess._mhlibAccess_getCullRadius() == 0.0D && classicAccess._mhlibAccess_getCullBox() == null,
                        "harvest 3: a classic spider (no profile) carries no cull radius and no cached box, actual radius "
                                + classicAccess._mhlibAccess_getCullRadius() + " box " + classicAccess._mhlibAccess_getCullBox());
                helper.assertTrue(((IMultipartEntity<?>) (Object) classic).mhlibComputeCullBox() == null,
                        "harvest 3: a classic spider computes no cull box (radius 0), actual " + ((IMultipartEntity<?>) (Object) classic).mhlibComputeCullBox());
                helper.assertTrue(classic.getBoundingBoxForCulling().equals(classic.getBoundingBox()),
                        "harvest 3: a classic spider keeps vanilla's cull box (its body box), actual " + classic.getBoundingBoxForCulling());
            } finally {
                queen.discard();
                modern.discard();
                classic.discard();
            }
            helper.succeed();
        });
    }

    private static boolean isInside(AABB inner, AABB outer) {
        final double eps = 1.0E-9D;
        return inner.minX >= outer.minX - eps && inner.minY >= outer.minY - eps && inner.minZ >= outer.minZ - eps
                && inner.maxX <= outer.maxX + eps && inner.maxY <= outer.maxY + eps && inner.maxZ <= outer.maxZ + eps;
    }

    private static void assertInside(GameTestHelper helper, AABB inner, AABB outer, String what) {
        helper.assertTrue(isInside(inner, outer), "harvest 3: " + what + " -- inner " + inner + " is not inside outer " + outer);
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
