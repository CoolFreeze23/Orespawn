package danger.orespawn.gametest;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.AntRobot;
import danger.orespawn.entity.client.RenderSpiderRobotInfo;
import danger.orespawn.entity.gait.AntRigProfile;
import danger.orespawn.entity.gait.LegRig;
import danger.orespawn.entity.gait.ModernSpiderGait;
import danger.orespawn.network.SpiderGaitKeyframePayload;
import de.dertoaster.multihitboxlib.api.IMultipartEntity;

import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.monster.Zombie;
import net.neoforged.neoforge.entity.PartEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * 2.0 S5b — the ant rig: render-parity harness on the ant grid (the same
 * mandated proof the spider got in S2 — the conversion is rig-parameterized
 * and must be proven per rig), the classic-table construction snapshot, the
 * mode gate + i083 Size-hook dims pin, flat-walk and the hover
 * all-legs-stranded degenerate, the S4-family part damage player-path, the
 * stream-gate predicate, and the leg-count-6 keyframe wire round-trip.
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class AntGaitTests {

    private static final double EPS_HIP = 1.0E-6;
    private static final double EPS_JOINT = 1.0E-4;
    private static final double EPS_TARGET = 0.011;

    private static AntRobot spawnMode(GameTestHelper helper, BlockPos pos,
                                      OreSpawnConfig.SpiderMovement mode) {
        OreSpawnConfig.SpiderMovement prior = OreSpawnConfig.SPIDER_MOVEMENT.get();
        try {
            OreSpawnConfig.SPIDER_MOVEMENT.set(mode);
            return helper.spawn(ModEntities.ANT_ROBOT.get(), pos);
        } finally {
            OreSpawnConfig.SPIDER_MOVEMENT.set(prior);
        }
    }

    /**
     * Rig-parameterized double-precision forward kinematics — the
     * SpiderGaitTests S2 helper generalized: {@code ModelAntRobot.poseLeg}
     * places hips and advances the chain with the structurally identical
     * formulas (design-doc S5 research, AntRobot model :242-245), with 49px
     * segments in place of 99px; the pixel length comes from the rig.
     */
    private static double[][] modelForwardKinematics(LegRig rig, int leg,
                                                     double bodyX, double bodyY, double bodyZ,
                                                     float yawDeg, double[] angles) {
        double yd = (float) angles[0];
        double ud = (float) angles[1];
        double ymid = rig.neutralYaw(leg);
        double legoff = rig.hipRadial(leg);
        double yoff = rig.hipVertical(leg);
        // INDEPENDENT transcription of ModelAntRobot.poseLeg's 49px segment
        // (review: taking this from rig.segmentLength() made the harness
        // self-referential — both sides would drift together).
        double segPx = 49.0;

        double[][] model = new double[4][3];
        model[0][0] = -Math.cos(ymid) * legoff * 16.0;
        model[0][1] = yoff * -16.0;
        model[0][2] = Math.sin(ymid) * legoff * 16.0;
        double[] segAngles = {
                (float) angles[2] + (float) ud,
                (float) angles[3] + (float) ud,
                (float) angles[4] + (float) ud};
        for (int i = 0; i < 3; ++i) {
            double a = segAngles[i];
            model[i + 1][0] = model[i][0] + Math.cos(a) * Math.sin(yd) * segPx;
            model[i + 1][1] = model[i][1] - Math.sin(a) * segPx;
            model[i + 1][2] = model[i][2] + Math.cos(a) * Math.cos(yd) * segPx;
        }

        double yawRad = Math.toRadians(Mth.wrapDegrees((double) yawDeg));
        double[][] world = new double[4][3];
        for (int i = 0; i < 4; ++i) {
            double h = Math.hypot(model[i][0], model[i][2]);
            double alphaM = Math.atan2(model[i][2], model[i][0]);
            double alphaW = yawRad - alphaM;
            world[i][0] = bodyX + h / 16.0 * Math.cos(alphaW);
            world[i][1] = bodyY - model[i][1] / 16.0;
            world[i][2] = bodyZ + h / 16.0 * Math.sin(alphaW);
        }
        return world;
    }

    private static void assertClose(GameTestHelper helper, String what,
                                    double ax, double ay, double az,
                                    double bx, double by, double bz, double eps) {
        double d = Math.sqrt((ax - bx) * (ax - bx) + (ay - by) * (ay - by) + (az - bz) * (az - bz));
        helper.assertTrue(d <= eps, what + " off by " + d + " (> " + eps + "): ("
                + ax + "," + ay + "," + az + ") vs (" + bx + "," + by + "," + bz + ")");
    }

    /**
     * The ant conversion proof (per-rig law: the harness must run the ant
     * grid, not inherit the spider's): every leg × yaw spread × targets at
     * rest (which for the ant's front pair is 9.03 of 9.1875 reach — 98.3%,
     * the very case the rig's 0.995 margin exists for), drifted, near-fold
     * (contraction floor band), raised, near-extension 8.5, 99.6% of max,
     * and unreachable.
     */
    @GameTest(template = "empty")
    public void s5b_ant_render_parity_harness(GameTestHelper helper) {
        LegRig rig = AntRigProfile.RIG;
        double bodyX = 100.5;
        double bodyY = 64.0;
        double bodyZ = -200.25;
        float[] yaws = {0.0f, 37.5f, 90.0f, 180.0f, 271.3f, -45.0f};
        double[][] joints = {new double[2], new double[2], new double[2], new double[2]};
        double[] angles = new double[5];
        int cases = 0;

        for (float yaw : yaws) {
            for (int leg = 0; leg < rig.legCount(); ++leg) {
                double hx = rig.hipX(leg, bodyX, yaw);
                double hy = rig.hipY(leg, bodyY);
                double hz = rig.hipZ(leg, bodyZ, yaw);
                double bearing = rig.legBearing(leg, yaw);
                double outX = -Math.sin(bearing);
                double outZ = Math.cos(bearing);
                double rest = rig.restReach(leg);

                double[][] targets = {
                        {hx + outX * rest, bodyY, hz + outZ * rest},                     // rest stance (front pair: 98.3% reach)
                        {hx + outX * (rest - 1.5) + 0.5, bodyY - 0.5, hz + outZ * (rest - 1.5) - 0.75}, // drifted
                        {hx + outX * 1.5, bodyY - 1.0, hz + outZ * 1.5},                 // near-fold (contraction floor band)
                        {hx + outX * 4.0, bodyY + 1.5, hz + outZ * 4.0},                 // raised footing
                        {hx + outX * 8.5, hy, hz + outZ * 8.5},                          // near-extension band
                        {hx + outX * 9.13, hy, hz + outZ * 9.13},                        // 99.4% of max — inside the 0.995 cap, so the landing assert FIRES
                        {hx + outX * 12.0, bodyY - 0.5, hz + outZ * 12.0},               // unreachable
                };
                for (double[] t : targets) {
                    ModernSpiderGait.solveLegAngles(rig, bodyX, bodyY, bodyZ, yaw,
                            leg, t[0], t[1], t[2], joints, angles);
                    double[][] fk = modelForwardKinematics(rig, leg, bodyX, bodyY, bodyZ, yaw, angles);

                    assertClose(helper, "ant leg " + leg + " yaw " + yaw + " hip",
                            fk[0][0], fk[0][1], fk[0][2], hx, hy, hz, EPS_HIP);

                    // Joints: FK-of-angles == the FABRIK joints mapped to world
                    // along the leg-plane bearing (solver u-axis fallback mirrored).
                    double dx = t[0] - hx;
                    double dz = t[2] - hz;
                    double dh = Math.hypot(dx, dz);
                    final double ux;
                    final double uz;
                    if (dh > 1.0E-6) {
                        ux = dx / dh;
                        uz = dz / dh;
                    } else {
                        double alphaW = rig.legBearing(leg, yaw) + Math.PI / 2.0;
                        ux = Math.cos(alphaW);
                        uz = Math.sin(alphaW);
                    }
                    for (int j = 1; j < 4; ++j) {
                        double wx = hx + ux * joints[j][0];
                        double wy = hy + joints[j][1];
                        double wz = hz + uz * joints[j][0];
                        assertClose(helper, "ant leg " + leg + " yaw " + yaw + " joint " + j,
                                fk[j][0], fk[j][1], fk[j][2], wx, wy, wz, EPS_JOINT);
                    }

                    // Reachable targets: the FK foot lands on the request.
                    double reachDist = Math.sqrt(dx * dx + dz * dz
                            + (t[1] - hy) * (t[1] - hy));
                    if (reachDist <= rig.maxReach() * rig.reachMargin()) {
                        assertClose(helper, "ant leg " + leg + " yaw " + yaw + " foot",
                                fk[3][0], fk[3][1], fk[3][2], t[0], t[1], t[2], EPS_TARGET);
                    }
                    ++cases;
                }
            }
        }
        helper.assertTrue(cases == 6 * 6 * 7, "ant case census " + cases);
        // S5b review: pin the rig-derived tilt spans against an independent
        // recompute from the RAW classic tables (total radius legoff+rest at
        // yaw 0: x = -R*cos(ymid), z = -R*sin(ymid); pitch over the
        // front(2,3)/rear(4,5) pairs, roll over even/odd triples).
        double[] legoffT = {0.75, 0.75, 1.0, 1.0, 1.15, 1.15};
        double[] ymidT = {0.0, Math.PI, -0.7853982, 3.9269907, 0.7853982, 2.3561945};
        double[] restT = {6.0, 6.0, 9.0, 9.0, 4.0, 4.0};
        double frontZ = 0.0;
        double rearZ = 0.0;
        double evenX = 0.0;
        double oddX = 0.0;
        for (int leg = 0; leg < 6; ++leg) {
            double radius = legoffT[leg] + restT[leg];
            double x = -radius * Math.cos(ymidT[leg]);
            double z = -radius * Math.sin(ymidT[leg]);
            if (leg == 2 || leg == 3) {
                frontZ += z / 2.0;
            } else if (leg == 4 || leg == 5) {
                rearZ += z / 2.0;
            }
            if ((leg & 1) == 0) {
                evenX += x / 3.0;
            } else {
                oddX += x / 3.0;
            }
        }
        helper.assertTrue(Math.abs(rig.pitchSpan() - Math.abs(frontZ - rearZ)) < 1.0E-9,
                "ant pitchSpan drifted from the table-derived value: " + rig.pitchSpan());
        helper.assertTrue(Math.abs(rig.rollSpan() - Math.abs(oddX - evenX)) < 1.0E-9,
                "ant rollSpan drifted from the table-derived value: " + rig.rollSpan());
        helper.succeed();
    }

    /**
     * Construction snapshot (the S2 pattern, ant edition): the modern rig
     * tables must mirror a LIVE classic ant's initLegData tables exactly —
     * legoff/ymid/yoff/|yrange|/pairedwith per leg — and the mode gate must
     * hold (classic gait null, modern gait ant-rigged with 6 legs).
     */
    @GameTest(template = "empty_large", timeoutTicks = 200, batch = "spiderGaitIsolation")
    public void s5b_ant_construction_snapshot(GameTestHelper helper) {
        AntRobot classic = spawnMode(helper, new BlockPos(12, 2, 12), OreSpawnConfig.SpiderMovement.CLASSIC);
        AntRobot modern = spawnMode(helper, new BlockPos(36, 2, 36), OreSpawnConfig.SpiderMovement.MODERN);
        try {
            helper.assertTrue(classic.getModernGait() == null, "classic ant must have no gait controller");
            helper.assertTrue(modern.getModernGait() != null, "modern ant must carry the gait controller");
            helper.assertTrue(modern.getModernGait().legCount() == 6, "ant gait must be 6-legged");

            RenderSpiderRobotInfo tables = classic.getRenderSpiderRobotInfo();
            LegRig rig = AntRigProfile.RIG;
            for (int leg = 0; leg < 6; ++leg) {
                helper.assertTrue(Math.abs(tables.legoff[leg] - rig.hipRadial(leg)) < 1.0E-6,
                        "legoff mirror broken at leg " + leg);
                helper.assertTrue(Math.abs(tables.ymid[leg] - rig.neutralYaw(leg)) < 1.0E-6,
                        "ymid mirror broken at leg " + leg);
                helper.assertTrue(Math.abs(tables.yoff[leg] - rig.hipVertical(leg)) < 1.0E-6,
                        "yoff mirror broken at leg " + leg);
                helper.assertTrue(Math.abs(Math.abs(tables.yrange[leg]) - rig.swingRange(leg)) < 1.0E-6,
                        "yrange mirror broken at leg " + leg);
                helper.assertTrue(tables.pairedwith[leg] == rig.pairedWith(leg),
                        "pairedwith mirror broken at leg " + leg);
            }
        } finally {
            classic.discard();
            modern.discard();
        }
        helper.succeed();
    }

    /**
     * Mode gate + the i083 Size-hook trap: a modern ant builds exactly 6
     * parts (leg0..leg5) with the id cascade; a classic ant builds ZERO;
     * and BOTH modes keep the classic dims EXACTLY 2.75×1.25 — MHLib
     * applies the profile main size through EntityEvent.Size, so a profile
     * size drift would silently resize every modern ant.
     */
    @GameTest(template = "empty_large", timeoutTicks = 200, batch = "spiderGaitIsolation")
    public void s5b_ant_mode_gate_and_dims(GameTestHelper helper) {
        AntRobot classic = spawnMode(helper, new BlockPos(12, 2, 12), OreSpawnConfig.SpiderMovement.CLASSIC);
        AntRobot modern = spawnMode(helper, new BlockPos(36, 2, 36), OreSpawnConfig.SpiderMovement.MODERN);
        try {
            helper.assertTrue(classic.getParts() == null || classic.getParts().length == 0,
                    "classic ant must construct ZERO parts");
            helper.assertTrue(modern.getParts() != null && modern.getParts().length == 6,
                    "modern ant must construct exactly 6 leg parts, got "
                            + (modern.getParts() == null ? 0 : modern.getParts().length));
            for (int i = 0; i < 6; ++i) {
                helper.assertTrue(modern.getParts()[i].getId() == modern.getId() + 1 + i,
                        "ant part id cascade broken at index " + i);
            }
            helper.assertTrue(classic.getBbWidth() == 2.75f && classic.getBbHeight() == 1.25f,
                    "classic ant dims drifted: " + classic.getBbWidth() + "x" + classic.getBbHeight());
            helper.assertTrue(modern.getBbWidth() == 2.75f && modern.getBbHeight() == 1.25f,
                    "modern ant dims drifted (Size-hook trap): "
                            + modern.getBbWidth() + "x" + modern.getBbHeight());
        } finally {
            classic.discard();
            modern.discard();
        }
        helper.succeed();
    }

    /**
     * Flat-ground walk: a driven modern ant steps (at least one swing
     * begins), never strands on flat ground, and settles fully grounded
     * with every foot inside leg reach of its hip after the drive stops.
     */
    @GameTest(template = "empty_large", timeoutTicks = 400, batch = "spiderGaitIsolation")
    public void s5b_ant_flat_walk(GameTestHelper helper) {
        final AntRobot ant = spawnModeNoFreeWill(helper, new BlockPos(10, 2, 24));
        ant.setYRot(0.0f);
        final ModernSpiderGait gait = ant.getModernGait();
        helper.assertTrue(gait != null, "modern ant must carry the gait controller");
        final int[] tick = {0};
        final boolean[] sawStep = {false};
        helper.onEachTick(() -> {
            try {
                int t = ++tick[0];
                if (t <= 80) {
                    ant.setDeltaMovement(0.15, ant.getDeltaMovement().y, 0.0);
                }
                for (int leg = 0; leg < 6; ++leg) {
                    if (gait.isSwinging(leg)) {
                        sawStep[0] = true;
                    }
                    helper.assertFalse(gait.isStranded(leg),
                            "leg " + leg + " stranded on FLAT ground at t=" + t);
                }
                if (t == 160) {
                    helper.assertTrue(sawStep[0], "driven ant never stepped");
                    for (int leg = 0; leg < 6; ++leg) {
                        helper.assertFalse(gait.isSwinging(leg), "leg " + leg + " stuck mid-swing");
                        helper.assertTrue(gait.isGrounded(leg), "leg " + leg + " not settled");
                        double dx = gait.footX(leg) - AntRigProfile.RIG.hipX(leg, ant.getX(), ant.getYRot());
                        double dy = gait.footY(leg) - AntRigProfile.RIG.hipY(leg, ant.getY());
                        double dz = gait.footZ(leg) - AntRigProfile.RIG.hipZ(leg, ant.getZ(), ant.getYRot());
                        helper.assertTrue(Math.sqrt(dx * dx + dy * dy + dz * dz) <= AntRigProfile.MAX_REACH,
                                "leg " + leg + " planted beyond max reach");
                    }
                    ant.discard();
                    helper.succeed();
                }
            } catch (RuntimeException e) {
                ant.discard();
                throw e;
            }
        });
    }

    /**
     * The hover degenerate (reviewer target): held 12 blocks up — beyond the
     * ant's classic 8-down scan window — every leg STRANDS (dangling, no
     * ground claim) and the visual body sags to the rig's −0.5 floor;
     * released, every leg re-plants unconditionally once footing re-enters
     * the window. Hover-ride physics are untouched by the gait, so a
     * test-held altitude is the same server-observable state a hovering
     * ridden ant produces.
     *
     * <p>Template note: the 34-high {@code empty_tall} cage is REQUIRED —
     * in the 16-high cages the framework's invisible BARRIER ceiling sits
     * ~17 over the floor, inside the ant's scan-up window and contracted
     * reach at any held altitude, and the gait CORRECTLY plants on it (a
     * real solid surface — first run's census proved it). No strand band
     * exists in a 16-high cage: window-down 8 ≈ reach 9.14 vs interior 17.</p>
     */
    @GameTest(template = "empty_tall", timeoutTicks = 400, batch = "spiderGaitIsolation")
    public void s5b_ant_hover_all_stranded_dangle(GameTestHelper helper) {
        final AntRobot ant = spawnModeNoFreeWill(helper, new BlockPos(24, 2, 24));
        ant.setYRot(0.0f);
        final ModernSpiderGait gait = ant.getModernGait();
        helper.assertTrue(gait != null, "modern ant must carry the gait controller");
        final double holdX = ant.getX();
        final double holdZ = ant.getZ();
        final double[] groundY = {Double.NaN};
        final int[] tick = {0};
        helper.onEachTick(() -> {
            try {
                int t = ++tick[0];
                if (t < 30) {
                    if (t == 29) {
                        groundY[0] = ant.getY(); // settled hover height over the floor
                    }
                    return; // settle planted on the ground first
                }
                if (t < 140) {
                    // Hold aloft: 12 above the settled height — beyond the
                    // ant's classic 8-down scan window.
                    ant.setDeltaMovement(0.0, 0.0, 0.0);
                    ant.setPos(holdX, groundY[0] + 12.0, holdZ);
                    if (t == 139) {
                        // Hold verification FIRST — a failed hold must fail
                        // as "hold failed", not masquerade as a gait bug.
                        helper.assertTrue(ant.getY() >= groundY[0] + 10.0,
                                "hold failed: ant at Y=" + ant.getY() + " vs ground " + groundY[0]);
                        StringBuilder census = new StringBuilder();
                        for (int leg = 0; leg < 6; ++leg) {
                            census.append(" leg").append(leg)
                                    .append(gait.isStranded(leg) ? "=STRANDED"
                                            : gait.isSwinging(leg) ? "=SWING" : "=PLANTED@" + gait.footY(leg));
                            if (!gait.isStranded(leg)) {
                                census.append("on[").append(ant.level().getBlockState(
                                        BlockPos.containing(gait.footX(leg), gait.footY(leg) - 0.5,
                                                gait.footZ(leg)))).append(']');
                            }
                        }
                        for (int leg = 0; leg < 6; ++leg) {
                            helper.assertTrue(gait.isStranded(leg),
                                    "leg " + leg + " not stranded while held aloft (bodyY=" + ant.getY()
                                            + " ground=" + groundY[0] + " census:" + census + ")");
                            helper.assertFalse(gait.isGrounded(leg),
                                    "leg " + leg + " claims ground contact in mid-air");
                        }
                        helper.assertTrue(Math.abs(gait.bodyLift() - (-0.5)) <= 1.0E-6,
                                "all-stranded body failed to sag to the rig floor: " + gait.bodyLift());
                    }
                    return;
                }
                // Released: falls, hovers near ground, re-plants.
                if (t == 300) {
                    for (int leg = 0; leg < 6; ++leg) {
                        helper.assertFalse(gait.isStranded(leg),
                                "leg " + leg + " still stranded after landing");
                        helper.assertTrue(gait.isGrounded(leg) || gait.isSwinging(leg),
                                "leg " + leg + " neither grounded nor stepping after landing");
                    }
                    ant.discard();
                    helper.succeed();
                }
            } catch (RuntimeException e) {
                ant.discard();
                throw e;
            }
        });
    }

    /**
     * S4-family damage rules on the ant (player-path law: the damage enters
     * through the same hurt router a player's attack uses): SOURCED damage
     * through a leg part lands on the parent at body-identical amounts
     * (twin comparison); SOURCE-LESS damage through a part is dropped
     * (env rule — the body takes environment directly instead).
     */
    @GameTest(template = "empty_large", timeoutTicks = 200, batch = "spiderGaitIsolation")
    public void s5b_ant_part_damage_rules(GameTestHelper helper) {
        AntRobot viaPart = spawnMode(helper, new BlockPos(12, 2, 12), OreSpawnConfig.SpiderMovement.MODERN);
        AntRobot viaBody = spawnMode(helper, new BlockPos(36, 2, 36), OreSpawnConfig.SpiderMovement.MODERN);
        Zombie attacker = helper.spawnWithNoFreeWill(net.minecraft.world.entity.EntityType.ZOMBIE,
                new BlockPos(24, 2, 24));
        try {
            helper.assertTrue(viaPart.getParts() != null && viaPart.getParts().length == 6,
                    "part census precondition failed");
            float beforePart = viaPart.getHealth();
            float beforeBody = viaBody.getHealth();
            DamageSource sourced = helper.getLevel().damageSources().mobAttack(attacker);

            PartEntity<?> leg = (PartEntity<?>) viaPart.getParts()[2];
            helper.assertTrue(leg.hurt(sourced, 25.0f), "sourced damage via ant leg was rejected");
            helper.assertTrue(viaBody.hurt(sourced, 25.0f), "sourced damage via ant body was rejected");
            float droppedPart = beforePart - viaPart.getHealth();
            float droppedBody = beforeBody - viaBody.getHealth();
            helper.assertTrue(droppedPart > 0.0f && Math.abs(droppedPart - droppedBody) < 1.0E-3,
                    "leg damage must be body-identical: leg " + droppedPart + " vs body " + droppedBody);

            // Env rule on a FRESH ant (the twins are inside the vanilla
            // hurt-cooldown from the sourced hits — a smaller follow-up hit
            // there is rejected by lastHurt, not by the env rule; first
            // suite run failed exactly that way):
            AntRobot envAnt = spawnMode(helper, new BlockPos(24, 2, 40), OreSpawnConfig.SpiderMovement.MODERN);
            try {
                float before = envAnt.getHealth();
                DamageSource sourceless = helper.getLevel().damageSources().generic();
                PartEntity<?> envLeg = (PartEntity<?>) envAnt.getParts()[1];
                envLeg.hurt(sourceless, 50.0f);
                helper.assertTrue(envAnt.getHealth() == before,
                        "source-less damage leaked through an ant leg part");
                envAnt.hurt(sourceless, 5.0f);
                helper.assertTrue(envAnt.getHealth() < before,
                        "the ant BODY must still take source-less damage directly");
            } finally {
                envAnt.discard();
            }
        } finally {
            viaPart.discard();
            viaBody.discard();
            attacker.discard();
        }
        helper.succeed();
    }

    /**
     * The ratified stream gate holds for the ant profile (sync-with-model
     * false, all deviations 0 → locally mirrored, gate after the pairing
     * seed) — the spider/Queen arms are pinned in RideTests.
     */
    @GameTest(template = "empty_large", timeoutTicks = 200, batch = "spiderGaitIsolation")
    public void s5b_ant_stream_gate_predicate(GameTestHelper helper) {
        AntRobot ant = spawnMode(helper, new BlockPos(24, 2, 24), OreSpawnConfig.SpiderMovement.MODERN);
        try {
            Object antObj = ant;
            helper.assertTrue(antObj instanceof IMultipartEntity<?> ime && !ime.mhlibShouldStreamParts(),
                    "modern ant must NOT stream parts (locally mirrored)");
        } finally {
            ant.discard();
        }
        helper.succeed();
    }

    /**
     * Wire law: the keyframe payload round-trips at leg count 6 — encode a
     * live ant keyframe, decode it, and get the identical state back (the
     * decoder's {6,8} envelope is the S5b registrar-1.3 format change).
     */
    @GameTest(template = "empty_large", timeoutTicks = 200, batch = "spiderGaitIsolation")
    public void s5b_ant_keyframe_roundtrip(GameTestHelper helper) {
        AntRobot ant = spawnMode(helper, new BlockPos(24, 2, 24), OreSpawnConfig.SpiderMovement.MODERN);
        try {
            SpiderGaitKeyframePayload sent = ant.getModernGait().buildKeyframe(ant);
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            SpiderGaitKeyframePayload.STREAM_CODEC.encode(buf, sent);
            SpiderGaitKeyframePayload received = SpiderGaitKeyframePayload.STREAM_CODEC.decode(buf);
            helper.assertTrue(received.footX().length == 6, "ant keyframe wire leg count");
            // Negative path (review): a leg count outside {6,8} must die at
            // decode, before any allocation.
            FriendlyByteBuf bad = new FriendlyByteBuf(Unpooled.buffer());
            bad.writeVarInt(42);
            bad.writeByte(7);
            boolean threw = false;
            try {
                SpiderGaitKeyframePayload.STREAM_CODEC.decode(bad);
            } catch (io.netty.handler.codec.DecoderException expected) {
                threw = true;
            }
            helper.assertTrue(threw, "leg count 7 must be rejected at decode");
            helper.assertTrue(received.entityId() == sent.entityId(), "entity id round-trip");
            for (int leg = 0; leg < 6; ++leg) {
                helper.assertTrue(received.footX()[leg] == sent.footX()[leg]
                                && received.footY()[leg] == sent.footY()[leg]
                                && received.footZ()[leg] == sent.footZ()[leg]
                                && received.grounded()[leg] == sent.grounded()[leg]
                                && received.stranded()[leg] == sent.stranded()[leg],
                        "keyframe round-trip mismatch at leg " + leg);
            }
        } finally {
            ant.discard();
        }
        helper.succeed();
    }

    /**
     * Spawn a modern ant that is genuinely inert: goals stripped AND
     * owned=1 — the ant's combat drive lives in customServerAiStep (which
     * removeFreeWill does NOT strip) behind the owned==0 gate, so without
     * this a future batch-mate spawning a LivingEntity within 12 blocks
     * would inject chase impulses into the walk/hover tests (review).
     */
    private static AntRobot spawnModeNoFreeWill(GameTestHelper helper, BlockPos pos) {
        OreSpawnConfig.SpiderMovement prior = OreSpawnConfig.SPIDER_MOVEMENT.get();
        try {
            OreSpawnConfig.SPIDER_MOVEMENT.set(OreSpawnConfig.SpiderMovement.MODERN);
            AntRobot ant = helper.spawnWithNoFreeWill(ModEntities.ANT_ROBOT.get(), pos);
            ant.setOwned();
            return ant;
        } finally {
            OreSpawnConfig.SPIDER_MOVEMENT.set(prior);
        }
    }
}
