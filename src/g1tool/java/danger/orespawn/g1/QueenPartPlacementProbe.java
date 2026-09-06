package danger.orespawn.g1;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import de.dertoaster.multihitboxlib.api.IMHLibExtendedRenderLayer;
import de.dertoaster.multihitboxlib.client.geckolib.renderlayer.GeckolibBoneInformationCollectorLayer;
import de.dertoaster.multihitboxlib.mixin.geckolib.MixinGeoRenderer;
import de.dertoaster.multihitboxlib.util.RenderTickGate;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector4f;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.cache.object.GeoCube;
import software.bernie.geckolib.cache.object.GeoQuad;
import software.bernie.geckolib.cache.object.GeoVertex;
import software.bernie.geckolib.loading.json.raw.Model;
import software.bernie.geckolib.loading.json.typeadapter.KeyFramesAdapter;
import software.bernie.geckolib.loading.object.BakedModelFactory;
import software.bernie.geckolib.loading.object.GeometryTree;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
import software.bernie.geckolib.util.RenderUtil;

/**
 * ENT-S-092 / law 11 headless placement probe for The Queen's MHLib bone-synced
 * parts.
 *
 * <p>Bakes {@code the_queen.geo.json} through GeckoLib 4.8.4's own loader
 * ({@link KeyFramesAdapter#GEO_GSON} + {@link GeometryTree#fromModel} +
 * {@link BakedModelFactory#DEFAULT_FACTORY}), then re-executes the renderer's
 * static matrix chain for every bone with the real {@link RenderUtil} helpers and
 * the real {@link GeoBone} matrix setters, in the exact order of the 4.8.4
 * bytecode:</p>
 * <ul>
 *   <li>{@code GeoRenderer.defaultRender}: {@code pushPose}, then {@code preRender}.</li>
 *   <li>{@code GeoEntityRenderer.preRender} offsets 0-15: {@code entityRenderTranslations =
 *       new Matrix4f(poseStack.last().pose())} (the CAPTURE), then offset 38
 *       {@code scaleModelForRender(scaleWidth, scaleHeight, ...)}.</li>
 *   <li>{@code GeoRenderer.scaleModelForRender} offsets 0-24:
 *       {@code if (!isReRender && (w != 1 || h != 1)) poseStack.scale(w, h, w)} — this is the
 *       slot {@code QueenRenderer} overrides to feed the 1.7.10 factor, AFTER the capture.</li>
 *   <li>{@code GeoEntityRenderer.actuallyRender}: offset 1 {@code pushPose}; offsets 269-308
 *       {@code poseStack.scale(nativeScale x3)} with {@code nativeScale = LivingEntity.getScale()}
 *       (1.0 for the Queen: no SCALE attribute modifier); offset 322 {@code applyRotations} whose
 *       offsets 44-59 do {@code mulPose(Axis.YP.rotationDegrees(180 - yaw))} with yaw =
 *       {@code Mth.rotLerp(partialTick, yBodyRotO, yBodyRot)} (offsets 59-67); offsets 601-607
 *       {@code translate(0, 0.01f, 0)}; offsets 610-625 {@code modelRenderTranslations} capture;
 *       then {@code renderRecursively} per top-level bone.</li>
 *   <li>{@code GeoEntityRenderer.renderRecursively} offsets 1-21: {@code pushPose},
 *       {@code RenderUtil.translateMatrixToBone / translateToPivotPoint / rotateMatrixAroundBone /
 *       scaleMatrixForBone}; offsets 24-115 (gated on {@code GeoBone.isTrackingMatrices()}):
 *       {@code localMatrix = RenderUtil.invertAndMultiplyMatrices(poseState, entityRenderTranslations)},
 *       {@code setModelSpaceMatrix(invertAndMultiplyMatrices(poseState, modelRenderTranslations))},
 *       {@code setLocalSpaceMatrix(translateMatrix(localMatrix, getRenderOffset))},
 *       {@code setWorldSpaceMatrix(translateMatrix(new Matrix4f(localMatrix), animatable.position()))};
 *       offset 120 {@code translateAwayFromPivotPoint}; 148 {@code renderCubesOfBone}; 172
 *       {@code applyRenderLayersForBone} (where MHLib's collector layer reads
 *       {@code GeoBone.getWorldPosition()} = {@code worldSpaceMatrix * (0,0,0,1)}); 195
 *       {@code renderChildBones}; 199 {@code popPose}.</li>
 *   <li>{@code GeoRenderer.renderCube} offsets 2-12: cube pivot translate / rotate / untranslate, then
 *       {@code createVerticesOfQuad}: {@code poseState.transform(new Vector4f(vertex.position(), 1))}
 *       — used here only to measure the drawn extent of every bone's cubes.</li>
 * </ul>
 *
 * <p>MHLib's collector ({@code IBoneInformationCollectorLayerCommonLogic.onRenderBone}) is
 * mirrored for the placement math: the rotation vector is the SUM of {@code GeoBone.getRotX/Y/Z()}
 * down the chain and the scale vector the PRODUCT of {@code getScaleX/Y/Z()}, pushed at
 * renderRecursively HEAD and popped at TAIL ({@code MixinGeoRenderer}); the shipped rotation is
 * what the REAL {@code GeckolibBoneInformationCollectorLayer.foldBodyYaw} makes of that sum and the
 * body-yaw term ({@code bodyYawRotationTerm}) when the vendored collector has those methods, and the
 * bare sum otherwise (the collector before the ENT-S-092 yaw fix). The server side
 * ({@code MHLibPartEntity.applyInformation} :423-438) then places a part at
 * {@code worldPos - pivot.xRot(rx).yRot(ry).zRot(rz) * entityScale} with dimensions
 * {@code size * boneScale * entityScale}; that placement is re-executed here with the profile's
 * values so the part boxes can be compared against the drawn segments at body yaw 0, 45, 90 and
 * 180.</p>
 *
 * <p>The collector LIFECYCLE is not mirrored but driven for real: the vendored layer is
 * instantiated and its {@code onPreRender} / {@code onRenderRecursivelyStart} / {@code calcScales} /
 * {@code calcRotations} / {@code getRotationVector} / {@code onRenderRecursivelyEnd} /
 * {@code setScales(1,1,1)} + {@code setRotations(0,0,0)} / {@code onPostRender} sequence is run over
 * the baked rig for three frames, exactly as {@code MixinGeoRenderer} and
 * {@code GeckolibEntityRenderEventHandler} call it, asserting the rotation shipped for the trunk
 * bones (chains with zero rest rotation) is (0,0,0) every frame and that the shared statics
 * {@code IMHLibExtendedRenderLayer.DEFAULT_ROTATION / DEFAULT_SCALING} are unchanged after every
 * frame (BUG-043).</p>
 *
 * <p>BUG-044 (2026-09-04): the same layer's per-entity render-tick gate ({@code beginRenderPass} /
 * {@code isCollectingPass} / {@code endRenderPass}, what its {@code onPreRender(Entity)} /
 * {@code onPostRender(Entity)} hooks do with the entity's own stamp) is driven with fake entities
 * for the hitch case and the two-entities-one-layer case; see {@link #runRenderTickGate}.</p>
 *
 * <p>Nothing here needs a Minecraft bootstrap: {@link PoseStack}, {@link Axis}, {@link Vec3},
 * the JOML types, the GeckoLib bake, {@link RenderUtil} and the vendored layer are plain classes.
 * The one thing that is NOT callable is the real {@code GeoEntityRenderer} (its constructor needs
 * an {@code EntityRendererProvider.Context}), so its preRender / actuallyRender / renderRecursively
 * bodies are re-executed statement-for-statement from the bytecode offsets cited above instead;
 * likewise {@code renderForBone} needs a rendered Entity, so the yaw term is obtained from the
 * layer's public {@code bodyYawRotationTerm(float)} for the probed yaw. The
 * {@code isTrackingMatrices()} gate at renderRecursively offsets 24-28 is honoured; the probe
 * enables tracking on every bone the way {@code QueenRenderer} does for the synched ones.</p>
 *
 * <p>Usage: {@code QueenPartPlacementProbe <the_queen.geo.json> <the_queen.json> [report.json]}.
 * Exit code 0 when every check passes, 1 otherwise.</p>
 */
public final class QueenPartPlacementProbe {
    /** orig ClientProxyOreSpawn.java:493 {@code new RenderTheQueen(..., 1.9f, 2.0f)} -> RenderTheQueen.java:25 scale 2.0. */
    private static final float HOSTILE_RENDER_SCALE = 2.0F;
    /** orig RenderTheQueen.java:40-46: scale / 4 while PlayNicely. */
    private static final float NICE_RENDER_SCALE = HOSTILE_RENDER_SCALE / 4.0F;
    /** TheQueen.mhlibGetEntitySizeScale while playNicelyShrunk (22/4 x 24/4 = 5.5 x 6). */
    private static final double NICE_ENTITY_SCALE = 0.25D;
    private static final float[] BODY_YAWS = {0.0F, 45.0F, 90.0F, 180.0F};
    private static final int LIFECYCLE_FRAMES = 3;
    private static final double LINEARITY_EPS = 1.0E-4D;
    private static final double BOX_EPS = 0.02D;
    private static final double FOLD_EPS = 1.0E-9D;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private QueenPartPlacementProbe() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length > 0 && BENCH_FLAG.equals(args[0])) {
            runBench(args);
            return;
        }
        if (args.length < 2) {
            throw new IllegalArgumentException(
                    "Usage: QueenPartPlacementProbe <the_queen.geo.json> <the_queen.json> [report.json]\n"
                            + "   or: QueenPartPlacementProbe " + BENCH_FLAG + " <runs> <the_queen.geo.json> <the_queen.json> <beaver.geo.json> <outDir>");
        }
        Path geoPath = Path.of(args[0]).toAbsolutePath().normalize();
        Path profilePath = Path.of(args[1]).toAbsolutePath().normalize();
        Path reportPath = args.length > 2 ? Path.of(args[2]).toAbsolutePath().normalize() : null;

        Model rawModel = KeyFramesAdapter.GEO_GSON.fromJson(Files.readString(geoPath, StandardCharsets.UTF_8), Model.class);
        BakedGeoModel model = BakedModelFactory.DEFAULT_FACTORY.constructGeoModel(GeometryTree.fromModel(rawModel));
        Profile profile = Profile.read(profilePath);

        Map<String, GeoBone> bonesByName = new LinkedHashMap<>();
        Map<String, GeoBone> parents = new LinkedHashMap<>();
        for (GeoBone top : model.topLevelBones()) {
            indexBones(top, null, bonesByName, parents);
        }
        for (String synced : profile.syncedBones) {
            if (!bonesByName.containsKey(synced)) {
                throw new IllegalStateException("synched bone " + synced + " is not in the geo");
            }
        }
        Map<String, Set<String>> segments = segments(bonesByName, profile.syncedBones);
        YawFold fold = YawFold.detect();

        StringBuilder out = new StringBuilder();
        JsonObject report = new JsonObject();
        report.addProperty("geo", geoPath.toString().replace('\\', '/'));
        report.addProperty("profile", profilePath.toString().replace('\\', '/'));
        report.addProperty("frame", "MHLib world frame: entity at the origin, blocks, y up; at yaw 0 (GeckoLib applyRotations'"
                + " 180-degree turn included) world +x = geo +x (the bake negates x, the turn negates it back) and"
                + " world +z = geo -z (heads at +z, tail at -z); other yaws turn that frame by YP(-yaw)");
        report.addProperty("collector_has_body_yaw_fold", fold.present);
        out.append("collector: ").append(fold.describe()).append("\n\n");
        boolean allPass = true;

        // ---- 0. what MHLib read before tracking was enabled --------------------------------
        BakedGeoModel untrackedModel = BakedModelFactory.DEFAULT_FACTORY.constructGeoModel(GeometryTree.fromModel(rawModel));
        Map<String, BoneSample> untracked2 = run(untrackedModel, HOSTILE_RENDER_SCALE, true, false, 0.0F, profile.syncedBones);
        out.append("== GeoBone.getWorldPosition() with isTrackingMatrices() false (as shipped: no caller of setTrackingMatrices)\n");
        boolean untrackedAllZero = true;
        JsonArray untrackedRows = new JsonArray();
        for (String bone : profile.syncedBones) {
            Vector3d p = untracked2.get(bone).worldPos;
            untrackedAllZero &= p.length() == 0.0D;
            out.append(String.format(Locale.ROOT, "%-9s | %s%n", bone, vec(p)));
            JsonObject row = new JsonObject();
            row.addProperty("bone", bone);
            row.add("world_pos_untracked_at_2", vecJson(p));
            untrackedRows.add(row);
        }
        out.append("UNTRACKED BONES REPORT THE WORLD ORIGIN FOR EVERY SYNCED BONE: ")
                .append(untrackedAllZero ? "CONFIRMED (every part would be placed at world 0,0,0)" : "not reproduced").append("\n\n");
        report.add("untracked", untrackedRows);
        report.addProperty("untracked_all_zero", untrackedAllZero);

        // ---- 1. bone world positions and scale linearity (yaw 0) ---------------------------
        Map<String, BoneSample> at1 = run(model, 1.0F, true, true, 0.0F, profile.syncedBones);
        Map<String, BoneSample> at2 = run(model, HOSTILE_RENDER_SCALE, true, true, 0.0F, profile.syncedBones);
        Map<String, BoneSample> atHalf = run(model, NICE_RENDER_SCALE, true, true, 0.0F, profile.syncedBones);
        Map<String, BoneSample> pre2 = run(model, HOSTILE_RENDER_SCALE, false, true, 0.0F, profile.syncedBones);
        out.append("== synced bone world positions at yaw 0 (what MHLib's collector reads via GeoBone.getWorldPosition)\n");
        out.append(String.format(Locale.ROOT, "%-9s | %-30s | %-30s | %-30s | %-8s | %-8s | %s%n",
                "bone", "scale 1.0", "scale 2.0 (post-capture)", "scale 0.5 (post-capture)", "x2/x1", "x0.5/x1", "pre-capture 2.0"));
        JsonArray linearity = new JsonArray();
        boolean linearityPass = true;
        boolean ghostShown = true;
        for (String bone : profile.syncedBones) {
            Vector3d p1 = at1.get(bone).worldPos;
            Vector3d p2 = at2.get(bone).worldPos;
            Vector3d ph = atHalf.get(bone).worldPos;
            Vector3d pp = pre2.get(bone).worldPos;
            double err2 = maxAbsDiff(p2, scaled(p1, HOSTILE_RENDER_SCALE));
            double errH = maxAbsDiff(ph, scaled(p1, NICE_RENDER_SCALE));
            double errPre = maxAbsDiff(pp, p1);
            boolean ok = err2 <= LINEARITY_EPS && errH <= LINEARITY_EPS;
            linearityPass &= ok;
            ghostShown &= errPre <= LINEARITY_EPS;
            out.append(String.format(Locale.ROOT, "%-9s | %-30s | %-30s | %-30s | %-8s | %-8s | %s%n",
                    bone, vec(p1), vec(p2), vec(ph), ratio(p2, p1), ratio(ph, p1), vec(pp)));
            JsonObject row = new JsonObject();
            row.addProperty("bone", bone);
            row.add("world_pos_at_1", vecJson(p1));
            row.add("world_pos_at_2_post_capture", vecJson(p2));
            row.add("world_pos_at_0_5_post_capture", vecJson(ph));
            row.add("world_pos_at_2_pre_capture", vecJson(pp));
            row.addProperty("max_abs_err_vs_2x", err2);
            row.addProperty("max_abs_err_vs_0_5x", errH);
            row.addProperty("pre_capture_equals_1x_err", errPre);
            row.addProperty("pass", ok);
            linearity.add(row);
        }
        out.append(String.format(Locale.ROOT, "LINEARITY (post-capture positions = render factor x 1.0 positions, eps %.0e): %s%n",
                LINEARITY_EPS, linearityPass ? "PASS" : "FAIL"));
        out.append(String.format(Locale.ROOT, "PRE-CAPTURE GHOST (today's order: 2.0 positions identical to 1.0, parts on a half-size ghost): %s%n%n",
                ghostShown ? "CONFIRMED" : "not reproduced"));
        report.add("linearity", linearity);
        report.addProperty("linearity_pass", linearityPass);
        report.addProperty("pre_capture_ghost_confirmed", ghostShown);
        allPass &= linearityPass;

        // ---- 2. drawn segments vs profile part boxes, per body yaw --------------------------
        JsonArray placement = new JsonArray();
        JsonObject placementByYaw = new JsonObject();
        boolean placementPass = true;
        Map<String, Map<String, Vec3>> anchorsAtYaw0 = new LinkedHashMap<>();
        for (float yaw : BODY_YAWS) {
            boolean yawPass = true;
            boolean axisAligned = yaw % 90.0F == 0.0F;
            double yawTerm = fold.term(yaw);
            Map<String, BoneSample> hostile = run(model, HOSTILE_RENDER_SCALE, true, true, yaw, profile.syncedBones);
            Map<String, BoneSample> nice = run(model, NICE_RENDER_SCALE, true, true, yaw, profile.syncedBones);
            for (Mode mode : List.of(new Mode("hostile", hostile, 1.0D, HOSTILE_RENDER_SCALE),
                    new Mode("playNicely", nice, NICE_ENTITY_SCALE, NICE_RENDER_SCALE))) {
                Map<String, Vec3> anchors0 = anchorsAtYaw0.computeIfAbsent(mode.name, k -> new LinkedHashMap<>());
                out.append(String.format(Locale.ROOT,
                        "== body yaw %.0f, %s: render scale %.2f, MHLib entityScale %.2f, shipped y term %.4f rad — profile part boxes vs drawn segments%s%n",
                        yaw, mode.name, mode.renderScale, mode.entityScale, yawTerm,
                        axisAligned ? "" : " (not axis-aligned: an AABB cannot fit the turned segment, only the anchor is pinned)"));
                out.append(String.format(Locale.ROOT, "%-9s | %-36s | %-36s | %-8s | %-8s | %-14s | %s%n",
                        "part", "part box min..max (x/y/z)", "drawn segment min..max", "covered", "centre",
                        "anchor err", "fit err (width vs max(x,z) extent / height / xz centre / bottom)"));
                for (String bone : profile.syncedBones) {
                    Part part = profile.parts.get(bone);
                    BoneSample sample = mode.samples.get(bone);
                    double[][] seg = segmentBox(mode.samples, segments.get(bone));
                    Vec3 shipped = fold.ship(sample.rotation, yawTerm);
                    Box box = part == null ? null : placePart(part, sample, shipped, mode.entityScale);
                    JsonObject row = new JsonObject();
                    row.addProperty("yaw", yaw);
                    row.addProperty("mode", mode.name);
                    row.addProperty("part", bone);
                    row.add("shipped_rotation", vecJson(shipped));
                    if (seg != null) {
                        row.add("segment_min", vecJson(seg[0]));
                        row.add("segment_max", vecJson(seg[1]));
                    }
                    if (box == null) {
                        out.append(String.format(Locale.ROOT, "%-9s | (no part in profile)%n", bone));
                        row.addProperty("pass", false);
                        yawPass = false;
                        placement.add(row);
                        continue;
                    }
                    row.add("box_min", vecJson(box.min));
                    row.add("box_max", vecJson(box.max));
                    double tolerance = BOX_EPS * Math.max(1.0D, mode.renderScale);
                    // The part anchor (box bottom-centre = worldPos - rotated pivot, MHLibPartEntity.setPos) must
                    // follow the body: at yaw t it is the yaw-0 anchor turned by YP(-t) = Vec3.yRot(term).
                    Vec3 anchor = new Vec3(0.5D * (box.min[0] + box.max[0]), box.min[1], 0.5D * (box.min[2] + box.max[2]));
                    double anchorErr;
                    if (yaw == 0.0F) {
                        anchors0.put(bone, anchor);
                        anchorErr = 0.0D;
                    } else {
                        Vec3 expected = anchors0.get(bone).yRot((float) yawTerm);
                        anchorErr = Math.max(Math.abs(anchor.x - expected.x),
                                Math.max(Math.abs(anchor.y - expected.y), Math.abs(anchor.z - expected.z)));
                    }
                    boolean anchorOk = anchorErr <= tolerance;
                    row.add("anchor", vecJson(anchor));
                    row.addProperty("anchor_err_vs_turned_yaw0_anchor", anchorErr);
                    if (seg == null) {
                        out.append(String.format(Locale.ROOT, "%-9s | %-36s | (segment has no cubes) | anchor err %.3f %s%n",
                                bone, box, anchorErr, anchorOk ? "ok" : "OFF"));
                        row.addProperty("pass", anchorOk);
                        yawPass &= anchorOk;
                        placement.add(row);
                        continue;
                    }
                    double covered = coveredFraction(box, seg);
                    boolean centreInside = contains(box, mid(seg[0], seg[1]));
                    double[] fit = fitErrors(box, seg);
                    double worstFit = Math.max(Math.max(fit[0], fit[1]), Math.max(fit[2], fit[3]));
                    boolean fitOk = covered >= 1.0D - 0.01D && centreInside && worstFit <= tolerance;
                    boolean ok = anchorOk && (!axisAligned || fitOk);
                    yawPass &= ok;
                    out.append(String.format(Locale.ROOT, "%-9s | %-36s | %-36s | %7.1f%% | %-8s | %-14s | %.3f / %.3f / %.3f / %.3f %s%n",
                            bone, box, boxString(seg), covered * 100.0D, centreInside ? "inside" : "OUTSIDE",
                            String.format(Locale.ROOT, "%.3f %s", anchorErr, anchorOk ? "ok" : "OFF"),
                            fit[0], fit[1], fit[2], fit[3], axisAligned ? (fitOk ? "ok" : "MISMATCH") : "(info)"));
                    row.addProperty("segment_covered_fraction", covered);
                    row.addProperty("segment_centre_inside_box", centreInside);
                    row.add("fit_errors_width_height_centre_bottom", arr(fit));
                    row.addProperty("fit_tolerance", tolerance);
                    row.addProperty("fit_required", axisAligned);
                    row.addProperty("pass", ok);
                    placement.add(row);
                }
                out.append('\n');
            }
            out.append(String.format(Locale.ROOT, "PART BOXES ON DRAWN SEGMENTS AT BODY YAW %.0f (%s): %s%n%n", yaw,
                    axisAligned ? "anchor follows the body AND segment inside its box" : "anchor follows the body",
                    yawPass ? "PASS" : "FAIL"));
            placementByYaw.addProperty(String.format(Locale.ROOT, "yaw%.0f", yaw), yawPass);
            placementPass &= yawPass;
        }
        out.append("PART BOXES ON DRAWN SEGMENTS, ALL YAWS (every anchor = yaw-0 anchor turned by the body yaw within ")
                .append(BOX_EPS).append(" x render scale; at the axis-aligned yaws also segment >= 99% inside its box, centre inside, "
                        + "width/height/centre/bottom fit within the same tolerance): ")
                .append(placementPass ? "PASS" : "FAIL").append("\n\n");
        report.add("placement", placement);
        report.add("placement_pass_by_yaw", placementByYaw);
        report.addProperty("placement_pass", placementPass);
        allPass &= placementPass;

        // ---- 3. the yaw fold itself: applyInformation's fixed chain must equal the exact outer yaw
        JsonObject foldReport = new JsonObject();
        if (fold.present) {
            double worst = 0.0D;
            String worstCase = "";
            double[] angles = {0.0D, 0.3D, -0.7D, 1.2D, 2.9D, -2.2D};
            double[] yaws = {0.0D, 45.0D, 90.0D, 135.0D, 180.0D, -90.0D, 270.0D, 10.0D};
            Vec3[] pivots = {new Vec3(-22.18D, 5.13D, 8.78D), new Vec3(1.51D, 4.49D, -4.21D), new Vec3(0.13D, 0.75D, 10.94D),
                    new Vec3(3.0D, -2.0D, 1.0D)};
            for (double rx : angles) {
                for (double ry : angles) {
                    for (double rz : angles) {
                        for (double yaw : yaws) {
                            double term = fold.term((float) yaw);
                            Vec3 folded = fold.ship(new Vec3(rx, ry, rz), term);
                            for (Vec3 p : pivots) {
                                // Both sides in double precision (Vec3's own Mth sin/cos tables are quantised to
                                // 2*pi/65536 rad, ~4e-3 blocks on a 24-block pivot); the placement rows above use Vec3.
                                double[] viaChain = rotateExact(p, folded.x, folded.y, folded.z);
                                double[] exact = yRotExact(rotateExact(p, rx, ry, rz), term);
                                double err = Math.max(Math.abs(viaChain[0] - exact[0]),
                                        Math.max(Math.abs(viaChain[1] - exact[1]), Math.abs(viaChain[2] - exact[2])));
                                if (err > worst) {
                                    worst = err;
                                    worstCase = String.format(Locale.ROOT, "rot (%.2f, %.2f, %.2f) yaw %.0f pivot %s", rx, ry, rz, yaw, vec(p));
                                }
                            }
                        }
                    }
                }
            }
            boolean foldPass = worst <= FOLD_EPS;
            out.append(String.format(Locale.ROOT,
                    "FOLD SELF-TEST (foldBodyYaw: pivot.xRot(a').yRot(b').zRot(c') == (pivot.xRot(rx).yRot(ry).zRot(rz)).yRot(term) over %d angle triples x %d yaws x %d pivots): worst %.2e at %s -> %s%n%n",
                    angles.length * angles.length * angles.length, yaws.length, pivots.length, worst, worstCase, foldPass ? "PASS" : "FAIL"));
            foldReport.addProperty("worst_error", worst);
            foldReport.addProperty("worst_case", worstCase);
            foldReport.addProperty("pass", foldPass);
            allPass &= foldPass;
        } else {
            out.append("FOLD SELF-TEST: skipped (this collector has no foldBodyYaw)\n\n");
            foldReport.addProperty("pass", false);
            foldReport.addProperty("skipped", true);
        }
        report.add("fold_self_test", foldReport);

        // ---- 4. the real collector lifecycle over the rig, three frames (BUG-043) ----------
        LifecycleResult lifecycle = runLifecycle(model, profile.syncedBones, parents, out);
        report.add("lifecycle", lifecycle.json);
        report.addProperty("lifecycle_pass", lifecycle.pass);
        allPass &= lifecycle.pass;

        // ---- 4b. the per-entity render-tick gate through the real layer (BUG-044) --------------
        RenderTickGateResult renderTickGate = runRenderTickGate(model, profile.syncedBones, out);
        report.add("render_tick_gate", renderTickGate.json);
        report.addProperty("render_tick_gate_pass", renderTickGate.pass);
        allPass &= renderTickGate.pass;

        // ---- 5. derivation of the profile values from the 2.0 rest pose --------------------
        out.append("== profile values derived from the drawn segments at render scale 2.0 (rest pose, yaw 0, entityScale 1)\n");
        out.append("   size = [max(x extent, z extent), y extent] of the segment; box bottom-centre = (segment x/z centre, segment y min);\n");
        out.append("   pivot = MHLib-inverse-rotated (bone world pos - box bottom-centre); position = bone world pos (fallback offset)\n");
        out.append(String.format(Locale.ROOT, "%-9s | %-16s | %-26s | %-26s | %-26s | %s%n",
                "part", "size [w, h]", "pivot (profile frame)", "position (fallback)", "bone rot sum (rad)", "pivot round-trip err"));
        JsonArray derived = new JsonArray();
        for (String bone : profile.syncedBones) {
            BoneSample sample = at2.get(bone);
            double[][] seg = segmentBox(at2, segments.get(bone));
            JsonObject row = new JsonObject();
            row.addProperty("part", bone);
            if (seg == null) {
                out.append(String.format(Locale.ROOT, "%-9s | (segment has no cubes)%n", bone));
                derived.add(row);
                continue;
            }
            double dx = seg[1][0] - seg[0][0];
            double dy = seg[1][1] - seg[0][1];
            double dz = seg[1][2] - seg[0][2];
            double w = Math.max(dx, dz);
            double[] bottomCentre = {0.5D * (seg[0][0] + seg[1][0]), seg[0][1], 0.5D * (seg[0][2] + seg[1][2])};
            Vec3 pivotWorld = new Vec3(sample.worldPos.x - bottomCentre[0], sample.worldPos.y - bottomCentre[1],
                    sample.worldPos.z - bottomCentre[2]);
            Vec3 pivotProfile = inverseRotate(pivotWorld, sample.rotation);
            Vec3 roundTrip = rotateLikeMhlib(pivotProfile, sample.rotation);
            double rtErr = Math.max(Math.abs(roundTrip.x - pivotWorld.x),
                    Math.max(Math.abs(roundTrip.y - pivotWorld.y), Math.abs(roundTrip.z - pivotWorld.z)));
            out.append(String.format(Locale.ROOT, "%-9s | [%6.2f, %6.2f] | %-26s | %-26s | %-26s | %.2e%n",
                    bone, w, dy, vec(pivotProfile), vec(sample.worldPos), vec(sample.rotation), rtErr));
            row.addProperty("segment_x_extent", dx);
            row.addProperty("segment_y_extent", dy);
            row.addProperty("segment_z_extent", dz);
            row.add("size", arr(w, dy));
            row.add("pivot", vecJson(pivotProfile));
            row.add("position", vecJson(sample.worldPos));
            row.add("bone_rotation_sum_rad", vecJson(sample.rotation));
            row.add("bone_scale_product", vecJson(sample.scale));
            row.addProperty("pivot_round_trip_err", rtErr);
            derived.add(row);
        }
        report.add("derived_at_2", derived);
        out.append("\nOVERALL: ").append(allPass ? "PASS" : "FAIL").append('\n');
        report.addProperty("overall_pass", allPass);

        System.out.print(out);
        if (reportPath != null) {
            Files.createDirectories(reportPath.getParent());
            Files.writeString(reportPath, GSON.toJson(report), StandardCharsets.UTF_8);
            System.out.println("report: " + reportPath);
        }
        if (!allPass) {
            System.exit(1);
        }
    }

    // ------------------------------------------------------------------ renderer chain

    /**
     * Re-executes GeoRenderer.defaultRender -> GeoEntityRenderer.preRender -> actuallyRender ->
     * renderRecursively for the whole tree at one render scale and body yaw and returns every
     * bone's sample. {@code postCapture} = the QueenRenderer order after the restore (capture, then
     * scaleModelForRender applies the factor); {@code false} = the order before it (poseStack.scale
     * before super.preRender, i.e. inside the capture). {@code tracking} = whether the synched bones
     * have setTrackingMatrices(true), as QueenRenderer now does.
     */
    private static Map<String, BoneSample> run(BakedGeoModel model, float renderScale, boolean postCapture, boolean tracking,
                                               float bodyYawDegrees, List<String> synced) {
        PoseStack poseStack = new PoseStack();
        poseStack.pushPose();                                                   // GeoRenderer.defaultRender
        if (!postCapture && renderScale != 1.0F) {
            poseStack.scale(renderScale, renderScale, renderScale);             // QueenRenderer.preRender before the restore (pre-capture)
        }
        Matrix4f entityRenderTranslations = new Matrix4f(poseStack.last().pose()); // GeoEntityRenderer.preRender @0-15
        float widthScale = postCapture ? renderScale : 1.0F;                    // QueenRenderer.scaleModelForRender: 1.0 * effective
        float heightScale = widthScale;
        if (widthScale != 1.0F || heightScale != 1.0F) {                        // GeoRenderer.scaleModelForRender @0-24, isReRender = false
            poseStack.scale(widthScale, heightScale, widthScale);
        }
        poseStack.pushPose();                                                   // actuallyRender @1
        float nativeScale = 1.0F;                                               // @269-283 LivingEntity.getScale() = 1 for the Queen
        poseStack.scale(nativeScale, nativeScale, nativeScale);                 // @301-308
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - bodyYawDegrees));    // applyRotations @44-59, yaw = lerped body yaw, not sleeping
        poseStack.translate(0.0F, 0.01F, 0.0F);                                 // @601-607
        Matrix4f modelRenderTranslations = new Matrix4f(poseStack.last().pose()); // @610-625

        Map<String, BoneSample> samples = new LinkedHashMap<>();
        Vector3d rotation = new Vector3d(0, 0, 0);                              // IMHLibExtendedRenderLayer.onPreRender DEFAULT_ROTATION
        Vector3d scaling = new Vector3d(1, 1, 1);                               // DEFAULT_SCALING
        Set<String> syncedSet = new LinkedHashSet<>(synced);
        for (GeoBone top : model.topLevelBones()) {                              // @628-653 per top-level bone
            renderRecursively(poseStack, top, entityRenderTranslations, modelRenderTranslations, rotation, scaling, tracking,
                    syncedSet, samples);
        }
        poseStack.popPose();                                                    // actuallyRender @657
        poseStack.popPose();                                                    // defaultRender
        return samples;
    }

    private static void renderRecursively(PoseStack poseStack, GeoBone bone, Matrix4f entityRenderTranslations,
                                          Matrix4f modelRenderTranslations, Vector3d rotation, Vector3d scaling,
                                          boolean tracking, Set<String> synced, Map<String, BoneSample> samples) {
        // MixinGeoRenderer @HEAD -> onRenderRecursivelyStart: push copies of the running rotation / scale
        Vector3d savedRotation = new Vector3d(rotation);
        Vector3d savedScaling = new Vector3d(scaling);

        poseStack.pushPose();                                                   // renderRecursively @1
        RenderUtil.translateMatrixToBone(poseStack, bone);                      // @6
        RenderUtil.translateToPivotPoint(poseStack, bone);                      // @11
        RenderUtil.rotateMatrixAroundBone(poseStack, bone);                     // @16
        RenderUtil.scaleMatrixForBone(poseStack, bone);                         // @21
        if (tracking) {
            bone.setTrackingMatrices(true);                                     // what QueenRenderer now does for the synched bones
        }
        if (bone.isTrackingMatrices()) {                                        // @24-28 gate; @31-115 verbatim:
            Matrix4f poseState = new Matrix4f(poseStack.last().pose());
            Matrix4f localMatrix = RenderUtil.invertAndMultiplyMatrices(poseState, entityRenderTranslations);
            bone.setModelSpaceMatrix(RenderUtil.invertAndMultiplyMatrices(poseState, modelRenderTranslations));
            bone.setLocalSpaceMatrix(RenderUtil.translateMatrix(localMatrix, new Vector3f(0, 0, 0)));      // getRenderOffset = Vec3.ZERO
            bone.setWorldSpaceMatrix(RenderUtil.translateMatrix(new Matrix4f(localMatrix), new Vector3f(0, 0, 0))); // entity at the origin
        }
        RenderUtil.translateAwayFromPivotPoint(poseStack, bone);                // @120

        // @148 renderCubesOfBone -> GeoRenderer.renderCube @2-12 + createVerticesOfQuad: drawn extent of this bone's cubes
        double[] min = {Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY};
        double[] max = {Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY};
        boolean anyVertex = false;
        if (!bone.isHidden()) {
            for (GeoCube cube : bone.getCubes()) {
                poseStack.pushPose();
                RenderUtil.translateToPivotPoint(poseStack, cube);
                RenderUtil.rotateMatrixAroundCube(poseStack, cube);
                RenderUtil.translateAwayFromPivotPoint(poseStack, cube);
                Matrix4f cubePose = new Matrix4f(poseStack.last().pose());
                for (GeoQuad quad : cube.quads()) {
                    if (quad == null) {
                        continue;
                    }
                    for (GeoVertex vertex : quad.vertices()) {
                        Vector3f p = vertex.position();
                        Vector4f v = cubePose.transform(new Vector4f(p.x(), p.y(), p.z(), 1.0F));
                        // Same frame as GeoBone.getWorldPosition(): undo the captured entityRenderTranslations
                        // (identity here) the way the bone matrices do.
                        Vector4f w = new Matrix4f(entityRenderTranslations).invert().transform(v);
                        min[0] = Math.min(min[0], w.x());
                        min[1] = Math.min(min[1], w.y());
                        min[2] = Math.min(min[2], w.z());
                        max[0] = Math.max(max[0], w.x());
                        max[1] = Math.max(max[1], w.y());
                        max[2] = Math.max(max[2], w.z());
                        anyVertex = true;
                    }
                }
                poseStack.popPose();
            }
        }

        // @172 applyRenderLayersForBone -> GeckolibBoneInformationCollectorLayer.onRenderBone:
        // getBoneWorldPosition, then calcScales (product) and calcRotations (sum), then tryAddBoneInformation
        scaling.x *= bone.getScaleX();
        scaling.y *= bone.getScaleY();
        scaling.z *= bone.getScaleZ();
        rotation.x += bone.getRotX();
        rotation.y += bone.getRotY();
        rotation.z += bone.getRotZ();
        Vector3d worldPos = bone.getWorldPosition();
        samples.put(bone.getName(), new BoneSample(bone.getName(), synced.contains(bone.getName()), worldPos,
                new Vec3(rotation.x, rotation.y, rotation.z), new Vec3(scaling.x, scaling.y, scaling.z),
                anyVertex ? min : null, anyVertex ? max : null));

        for (GeoBone child : bone.getChildBones()) {                            // @195 renderChildBones, inside the parent's pose
            renderRecursively(poseStack, child, entityRenderTranslations, modelRenderTranslations, rotation, scaling, tracking,
                    synced, samples);
        }
        poseStack.popPose();                                                    // @199
        // MixinGeoRenderer @TAIL -> onRenderRecursivelyEnd: restore the parent's running values
        rotation.set(savedRotation);
        scaling.set(savedScaling);
    }

    // ------------------------------------------------------------------ the real collector lifecycle

    /**
     * Instantiates the vendored GeckolibBoneInformationCollectorLayer and drives it exactly as
     * GeckolibEntityRenderEventHandler.onPreRenderEntity (onPreRender), MixinGeoRenderer HEAD/TAIL
     * (onRenderRecursivelyStart / End), onRenderBone (calcScales, calcRotations, getRotationVector
     * for the synched bones) and onPostRenderEntity (performGlibLogic -> setScales(1,1,1) +
     * setRotations(0,0,0), then IMHLibExtendedRenderLayer.onPostRender) do, for
     * {@value #LIFECYCLE_FRAMES} frames over the rest pose.
     */
    private static LifecycleResult runLifecycle(BakedGeoModel model, List<String> synced, Map<String, GeoBone> parents,
                                                StringBuilder out) {
        out.append("== real GeckolibBoneInformationCollectorLayer lifecycle over the rig, rest pose, ")
                .append(LIFECYCLE_FRAMES).append(" frames (BUG-043 check)\n");
        List<String> trunk = new ArrayList<>();
        for (String name : synced) {
            GeoBone bone = model.getBone(name).orElseThrow();
            double chainRotation = 0.0D;
            for (GeoBone b = bone; b != null; b = parents.get(b.getName())) {
                chainRotation += Math.abs(b.getRotX()) + Math.abs(b.getRotY()) + Math.abs(b.getRotZ());
            }
            if (chainRotation == 0.0D) {
                trunk.add(name);
            }
        }
        out.append("trunk bones (synched, zero rest rotation down the chain): ").append(trunk).append('\n');
        GeckolibBoneInformationCollectorLayer<GeoAnimatable> layer =
                new GeckolibBoneInformationCollectorLayer<>(new HeadlessGeoRenderer());
        JsonObject json = new JsonObject();
        json.add("trunk_bones", names(trunk));
        JsonArray frames = new JsonArray();
        boolean pass = true;
        for (int frame = 1; frame <= LIFECYCLE_FRAMES; frame++) {
            layer.onPreRender();                                                // GeckolibEntityRenderEventHandler.onPreRenderEntity
            boolean aliased = layer.getCurrentRotation() == IMHLibExtendedRenderLayer.DEFAULT_ROTATION
                    || layer.getCurrentScaling() == IMHLibExtendedRenderLayer.DEFAULT_SCALING;
            Vector3d seed = new Vector3d(layer.getCurrentRotation());
            Map<String, Vec3> shipped = new LinkedHashMap<>();
            for (GeoBone top : model.topLevelBones()) {
                walkLayer(layer, top, synced, shipped);
            }
            layer.setScales(1, 1, 1);                                           // IBoneInformationCollectorLayerCommonLogic.onPostRender(Entity) tail
            layer.setRotations(0, 0, 0);
            layer.onPostRender();                                               // IMHLibExtendedRenderLayer.onPostRender
            Vector3d staticRotation = new Vector3d(IMHLibExtendedRenderLayer.DEFAULT_ROTATION);
            Vector3d staticScaling = new Vector3d(IMHLibExtendedRenderLayer.DEFAULT_SCALING);
            boolean staticsIntact = staticRotation.length() == 0.0D
                    && staticScaling.x == 1.0D && staticScaling.y == 1.0D && staticScaling.z == 1.0D;
            boolean trunkZero = true;
            JsonObject frameJson = new JsonObject();
            frameJson.addProperty("frame", frame);
            frameJson.addProperty("seed_rotation_is_static_instance", aliased);
            frameJson.add("seed_rotation", vecJson(seed));
            JsonObject shippedJson = new JsonObject();
            out.append(String.format(Locale.ROOT, "frame %d: seed after onPreRender = %s (same object as the static: %b)%n",
                    frame, vec(seed), aliased));
            for (Map.Entry<String, Vec3> e : shipped.entrySet()) {
                boolean isTrunk = trunk.contains(e.getKey());
                boolean zero = e.getValue().x == 0.0D && e.getValue().y == 0.0D && e.getValue().z == 0.0D;
                if (isTrunk && !zero) {
                    trunkZero = false;
                }
                out.append(String.format(Locale.ROOT, "   %-9s shipped rotation = %s%s%n", e.getKey(), vec(e.getValue()),
                        isTrunk ? (zero ? "  (trunk, zero ok)" : "  (trunk, NOT ZERO)") : ""));
                shippedJson.add(e.getKey(), vecJson(e.getValue()));
            }
            frameJson.add("shipped", shippedJson);
            frameJson.add("static_default_rotation_after", vecJson(staticRotation));
            frameJson.add("static_default_scaling_after", vecJson(staticScaling));
            frameJson.addProperty("trunk_rotation_zero", trunkZero);
            frameJson.addProperty("statics_intact", staticsIntact);
            out.append(String.format(Locale.ROOT, "   static DEFAULT_ROTATION after frame %d = %s, DEFAULT_SCALING = %s -> %s%n",
                    frame, vec(staticRotation), vec(staticScaling),
                    (trunkZero && staticsIntact) ? "ok" : "DRIFT"));
            pass &= trunkZero && staticsIntact;
            frames.add(frameJson);
        }
        json.add("frames", frames);
        out.append("COLLECTOR LIFECYCLE (trunk rotation (0,0,0) every frame, statics unchanged every frame): ")
                .append(pass ? "PASS" : "FAIL").append("\n\n");
        return new LifecycleResult(pass, json);
    }

    private static void walkLayer(GeckolibBoneInformationCollectorLayer<GeoAnimatable> layer, GeoBone bone,
                                  List<String> synced, Map<String, Vec3> shipped) {
        layer.onRenderRecursivelyStart();                                       // MixinGeoRenderer @HEAD renderRecursively
        layer.calcScales(bone);                                                 // onRenderBone via applyRenderLayersForBone (@172)
        layer.calcRotations(bone);
        if (synced.contains(bone.getName())) {
            shipped.put(bone.getName(), layer.getRotationVector());             // tryAddBoneInformation(..., getRotationVector())
        }
        for (GeoBone child : bone.getChildBones()) {
            walkLayer(layer, child, synced, shipped);
        }
        layer.onRenderRecursivelyEnd();                                         // @TAIL renderRecursively
    }

    // ------------------------------------------------------------------ the headless collector benchmark (Phase G slice (d))

    private static final String BENCH_FLAG = "--bench";
    /** Walks per measured run (morehitboxes_evaluation.md Section 5: "the same nanoTime/allocated-bytes probe over 1,000 walks"). */
    private static final int BENCH_WALKS = 1000;
    /** Fewest unmeasured runs before the measured ones (JIT warm-up); see {@link #BENCH_WARMUP_SETTLE}. */
    private static final int BENCH_WARMUP_MIN_RUNS = 20;
    /** Most unmeasured runs, whatever the settling says. */
    private static final int BENCH_WARMUP_MAX_RUNS = 60;
    /**
     * Warm-up ends (after the minimum) when {@value #BENCH_WARMUP_SETTLED_RUNS} consecutive runs each
     * stayed within this fraction of the median of the three runs before them -- a plateau, i.e. the
     * per-run ns has stopped falling (the JIT has landed). A plateau, not "no new minimum": one early
     * fast run (an OSR burst) would otherwise count every later, slower run as settled. Refuter B
     * measured the inactive tax falling 837 -> 113 ns across the MEASURED runs with the former fixed
     * three-run warm-up; every warm-up run's value is reported so the reader can see the fall.
     */
    private static final double BENCH_WARMUP_SETTLE = 0.10D;
    private static final int BENCH_WARMUP_SETTLED_RUNS = 3;
    /** The steady-state figure: the median of the last this-many measured runs. */
    private static final int BENCH_LAST_RUNS = 5;
    private static volatile double benchSink;
    /**
     * Where the collecting walk stores every per-bone result so that it ESCAPES as in-game, where
     * {@code tryAddBoneInformation} / {@code applyInformation} retain the vectors. Without this the
     * walk read one field of each and dropped it, and after a long enough warm-up C2's escape
     * analysis eliminated the 110 world-position {@code Vec3}s and the 20 scale / rotation
     * {@code Vec3}s of the yaw-0 walk: 16,904 -> 11,704 B/walk in one invocation (fix lane,
     * 2026-09-06) -- the probe's own artefact, not the collector's cost. A store into a static array
     * makes the object reachable, which is what the in-game path does.
     */
    private static final Object[] BENCH_ESCAPE = new Object[3];

    /**
     * Phase G slice (d) (2026-09-06): {@code --bench <runs> <the_queen.geo.json> <the_queen.json>
     * <beaver.geo.json> <outDir>} -- the headless companion of the live counters
     * {@code client.collector_ns} / {@code client.collector_alloc_bytes}. The real vendored
     * {@link GeckolibBoneInformationCollectorLayer} is driven over the baked rigs with no GeckoLib
     * render behind it, so what is timed is the collector alone. The HEAD/TAIL hooks reach the
     * layer through the REAL {@code MixinGeoRenderer._mhlib_callLayers} loop (the interface's
     * default method, which {@link BenchGeoRenderer} implements as written: {@code getRenderLayers()},
     * the iterator, the {@code instanceof}, the {@code Consumer}) -- the way the mixin's
     * {@code renderRecursively} HEAD/TAIL injections deliver them in-game (refuter B):
     * <ul>
     *   <li>{@code queen_collect_yawpi} (the baseline row): one walk = the layer's {@code onPreRender},
     *       then for every bone of the Queen rig the HEAD hook, what {@code onRenderBone} does for a
     *       collecting profile ({@code getBoneWorldPosition} -- three {@code GeoBone.getWorldPosition()}
     *       reads and one {@code Vec3} -- {@code calcScales}, {@code calcRotations}, and for the ten
     *       synched bones {@code getScaleVector} and {@code getRotationVector}), the children, the
     *       TAIL hook; then the post hook's {@code setScales(1,1,1)} / {@code setRotations(0,0,0)} and
     *       {@code onPostRender}. The layer's private {@code bodyYawRotationTerm} -- what
     *       {@code renderForBone} sets per bone from the rendered entity's body yaw -- is set once
     *       (reflection) to {@code bodyYawRotationTerm(180)} = -pi, a Queen facing the camera as the
     *       scenes spawn them, so {@code getRotationVector}'s {@code foldBodyYaw} builds its matrices
     *       as in-game: seven 3x3 {@code double[][]} per synched bone. NOT included, both needing a
     *       live entity: {@code tryAddBoneInformation}, and the trust-client apply per synched bone
     *       ({@code getPartByName} + {@code MHLibPartEntity.applyInformation}, common logic :132-137;
     *       {@code the_queen.json} is trust-client: true) -- the companion measures the collector's
     *       walk and fold only. Nor GeckoLib's own matrix chain (the rest pose is walked as baked).</li>
     *   <li>{@code queen_collect_yaw0}: the same walk with the term at 0 -- {@code foldBodyYaw}'s
     *       early-out returns the input, no matrices -- the first lane's baseline, kept for comparison.</li>
     *   <li>{@code beaver_tax_inactive}: a FLOOR of the hook tax. The Beaver rig through a layer whose
     *       {@code isBoneCollectionActive()} is a constant false: the real chain (layer :58-66 --
     *       {@code renderer instanceof GeoReplacedEntityRenderer}, {@code getCurrentEntity()},
     *       {@code shouldCollectModelBones(entity)}: {@code isMultipartEntity}, {@code instanceof
     *       IMultipartEntity}, {@code getHitboxProfile()}) needs a client renderer and a live entity.
     *       The HEAD/TAIL hooks run through the real loop and return after their counter guard and
     *       the {@code isBoneCollectionActive()} call; {@code onRenderBone} returns at its multipart
     *       check, which the walk mirrors by calling nothing. Scene E's headless counterpart; the
     *       classic renderer's collector cost is zero by construction.</li>
     *   <li>{@code beaver_tax_active}: the same rig through the default layer (collection active):
     *       the push/pop per bone with its two {@code Vector3d} copies and the {@code Tuple} -- the
     *       upper bound a GeoEntity-path non-multipart entity would pay.</li>
     * </ul>
     * Warm-up: at least {@value #BENCH_WARMUP_MIN_RUNS} unmeasured runs and then until the per-run
     * ns has settled ({@link #BENCH_WARMUP_SETTLE}), at most {@value #BENCH_WARMUP_MAX_RUNS}; every
     * warm-up run's value is reported. Then {@code runs} measured runs of {@value #BENCH_WALKS} walks
     * each; every run's value, the median run, and the median of the last {@value #BENCH_LAST_RUNS}
     * runs (the steady state) are reported per walk, the medians per bone too. Time is
     * {@code System.nanoTime()}, bytes the calling thread's
     * {@code ThreadMXBean.getCurrentThreadAllocatedBytes()} (the live counter's source,
     * {@code MHLibCollectorProbe}). Writes {@code collector_bench.json} into {@code outDir}.
     */
    private static void runBench(String[] args) throws Exception {
        if (args.length < 6) {
            throw new IllegalArgumentException("Usage: QueenPartPlacementProbe " + BENCH_FLAG
                    + " <runs> <the_queen.geo.json> <the_queen.json> <beaver.geo.json> <outDir>");
        }
        int runs = Integer.parseInt(args[1]);
        if (runs < 1) {
            throw new IllegalArgumentException("runs must be >= 1");
        }
        Path queenGeo = Path.of(args[2]).toAbsolutePath().normalize();
        Path queenProfile = Path.of(args[3]).toAbsolutePath().normalize();
        Path beaverGeo = Path.of(args[4]).toAbsolutePath().normalize();
        Path outDir = Path.of(args[5]).toAbsolutePath().normalize();

        BakedGeoModel queen = bake(queenGeo);
        BakedGeoModel beaver = bake(beaverGeo);
        Profile profile = Profile.read(queenProfile);
        for (String synced : profile.syncedBones) {
            if (queen.getBone(synced).isEmpty()) {
                throw new IllegalStateException("synched bone " + synced + " is not in the geo");
            }
        }

        JsonObject report = new JsonObject();
        report.addProperty("probe", "QueenPartPlacementProbe " + BENCH_FLAG + " (Phase G slice (d), 2026-09-06)");
        report.addProperty("walks_per_run", BENCH_WALKS);
        report.addProperty("runs", runs);
        report.addProperty("warmup", "at least " + BENCH_WARMUP_MIN_RUNS + " runs, then until " + BENCH_WARMUP_SETTLED_RUNS + " consecutive runs stay within "
                + (int) (BENCH_WARMUP_SETTLE * 100) + " % of the fastest so far, at most " + BENCH_WARMUP_MAX_RUNS + " (per rig: warmup_runs_used)");
        report.addProperty("jdk", System.getProperty("java.version") + " (" + System.getProperty("java.vm.name") + ", " + System.getProperty("java.vendor") + ")");
        report.addProperty("allocation_probe_supported", de.dertoaster.multihitboxlib.util.MHLibCollectorProbe.allocationSupported());
        Path repositoryRoot = danger.orespawn.bench.BenchGit.repositoryRoot();
        report.addProperty("git_head", danger.orespawn.bench.BenchGit.head(repositoryRoot));
        report.addProperty("working_tree", danger.orespawn.bench.BenchGit.workingTree(repositoryRoot));
        report.addProperty("queen_geo", queenGeo.toString().replace('\\', '/'));
        report.addProperty("queen_profile", queenProfile.toString().replace('\\', '/'));
        report.addProperty("beaver_geo", beaverGeo.toString().replace('\\', '/'));
        report.addProperty("hooks", "HEAD/TAIL hooks through the real MixinGeoRenderer._mhlib_callLayers loop (getRenderLayers(), iterator, instanceof, Consumer)");
        report.addProperty("not_measured", "tryAddBoneInformation and the trust-client getPartByName + MHLibPartEntity.applyInformation per synched bone "
                + "(both need a live entity; the_queen.json is trust-client: true); the inactive rig's isBoneCollectionActive() chain "
                + "(needs a GeoReplacedEntityRenderer and a live entity) -- beaver_tax_inactive is a FLOOR of the hook tax");
        // -pi: bodyYawRotationTerm(180 degrees), a Queen facing the camera as the scenes spawn them (mobs face the origin).
        final double yawPiTerm = GeckolibBoneInformationCollectorLayer.bodyYawRotationTerm(180.0F);
        report.addProperty("body_yaw_term_yawpi", yawPiTerm);
        JsonObject rigs = new JsonObject();
        StringBuilder out = new StringBuilder();
        out.append("== headless collector benchmark: ").append(runs).append(" measured runs x ").append(BENCH_WALKS)
                .append(" walks after an adaptive warm-up (>= ").append(BENCH_WARMUP_MIN_RUNS).append(" runs, settled within ")
                .append((int) (BENCH_WARMUP_SETTLE * 100)).append(" %); median run and the median of the last ").append(BENCH_LAST_RUNS).append(" reported\n");

        BenchGeoRenderer queenPiRenderer = new BenchGeoRenderer();
        GeckolibBoneInformationCollectorLayer<GeoAnimatable> queenPiLayer = queenPiRenderer.attach(new GeckolibBoneInformationCollectorLayer<>(queenPiRenderer));
        setBodyYawRotationTerm(queenPiLayer, yawPiTerm);
        rigs.add("queen_collect_yawpi", benchRig(out, "queen_collect_yawpi", queen, profile.syncedBones, runs, queenPiRenderer, queenPiLayer, BenchWalk.COLLECT,
                "body yaw 180 degrees (the term -pi, set on the layer as renderForBone does per bone in-game): onPreRender; per bone: HEAD hook via the real "
                        + "_mhlib_callLayers loop, getBoneWorldPosition (3 reads + Vec3), calcScales, calcRotations, getScaleVector + getRotationVector for the synched "
                        + "bones (foldBodyYaw builds four 3x3 matrices and multiplies three: seven double[][] per synched bone), children, TAIL hook; "
                        + "setScales/setRotations; onPostRender. Not included: tryAddBoneInformation and the trust-client applyInformation (need a live entity)"));
        if (readBodyYawRotationTerm(queenPiLayer) != yawPiTerm) {
            throw new IllegalStateException("the body-yaw term did not survive the walks: " + readBodyYawRotationTerm(queenPiLayer));
        }
        BenchGeoRenderer queen0Renderer = new BenchGeoRenderer();
        GeckolibBoneInformationCollectorLayer<GeoAnimatable> queen0Layer = queen0Renderer.attach(new GeckolibBoneInformationCollectorLayer<>(queen0Renderer));
        rigs.add("queen_collect_yaw0", benchRig(out, "queen_collect_yaw0", queen, profile.syncedBones, runs, queen0Renderer, queen0Layer, BenchWalk.COLLECT,
                "the same walk at body yaw 0: foldBodyYaw's yawTerm == 0 early-out returns the input (no matrices) -- the first lane's baseline, kept for comparison"));
        BenchGeoRenderer inactiveRenderer = new BenchGeoRenderer();
        rigs.add("beaver_tax_inactive", benchRig(out, "beaver_tax_inactive", beaver, List.of(), runs, inactiveRenderer,
                inactiveRenderer.attach(new InactiveCollectorLayer(inactiveRenderer)), BenchWalk.TAX,
                "FLOOR of the hook tax: isBoneCollectionActive() is a constant false here (the real chain -- renderer instanceof GeoReplacedEntityRenderer, "
                        + "getCurrentEntity(), shouldCollectModelBones(entity) -- needs a client renderer and a live entity); onPreRender; per bone HEAD + TAIL hooks "
                        + "through the real _mhlib_callLayers loop, returning after the counter guard and the isBoneCollectionActive() call; onPostRender"));
        BenchGeoRenderer activeRenderer = new BenchGeoRenderer();
        rigs.add("beaver_tax_active", benchRig(out, "beaver_tax_active", beaver, List.of(), runs, activeRenderer,
                activeRenderer.attach(new GeckolibBoneInformationCollectorLayer<>(activeRenderer)), BenchWalk.TAX,
                "collection active (the GeoEntity path): onPreRender; per bone HEAD push + TAIL pop through the real _mhlib_callLayers loop "
                        + "(two Vector3d copies and a Tuple per bone); onPostRender"));
        report.add("rigs", rigs);
        report.addProperty("sink", benchSink);

        Files.createDirectories(outDir);
        Path json = outDir.resolve("collector_bench.json");
        Files.writeString(json, GSON.toJson(report), StandardCharsets.UTF_8);
        out.append("report: ").append(json).append('\n');
        System.out.print(out);
    }

    private static BakedGeoModel bake(Path geoPath) throws Exception {
        Model raw = KeyFramesAdapter.GEO_GSON.fromJson(Files.readString(geoPath, StandardCharsets.UTF_8), Model.class);
        return BakedModelFactory.DEFAULT_FACTORY.constructGeoModel(GeometryTree.fromModel(raw));
    }

    private enum BenchWalk { COLLECT, TAX }

    /** The layer's private {@code bodyYawRotationTerm}, which only {@code renderForBone} (entity-bound) sets in-game. */
    private static Field bodyYawRotationTermField() throws ReflectiveOperationException {
        Field field = GeckolibBoneInformationCollectorLayer.class.getDeclaredField("bodyYawRotationTerm");
        field.setAccessible(true);
        return field;
    }

    private static void setBodyYawRotationTerm(GeckolibBoneInformationCollectorLayer<?> layer, double term) throws ReflectiveOperationException {
        bodyYawRotationTermField().setDouble(layer, term);
    }

    private static double readBodyYawRotationTerm(GeckolibBoneInformationCollectorLayer<?> layer) throws ReflectiveOperationException {
        return bodyYawRotationTermField().getDouble(layer);
    }

    private static JsonObject benchRig(StringBuilder out, String name, BakedGeoModel model, List<String> synced, int runs, BenchGeoRenderer renderer,
                                       GeckolibBoneInformationCollectorLayer<GeoAnimatable> layer, BenchWalk walk, String description) {
        int bones = countBones(model);
        // Warm-up until the per-run ns plateaus (see BENCH_WARMUP_SETTLE), between the minimum and the maximum run counts.
        List<Double> warmup = new ArrayList<>();
        int settled = 0;
        while (warmup.size() < BENCH_WARMUP_MAX_RUNS) {
            double ns = benchRun(model, synced, renderer, layer, walk)[0] / (double) BENCH_WALKS;
            int n = warmup.size();
            if (n >= 3) {
                double reference = median(new double[]{warmup.get(n - 1), warmup.get(n - 2), warmup.get(n - 3)});
                settled = Math.abs(ns - reference) <= reference * BENCH_WARMUP_SETTLE ? settled + 1 : 0;
            }
            warmup.add(ns);
            if (warmup.size() >= BENCH_WARMUP_MIN_RUNS && settled >= BENCH_WARMUP_SETTLED_RUNS) {
                break;
            }
        }
        double[] nsPerWalk = new double[runs];
        double[] bytesPerWalk = new double[runs];
        for (int run = 0; run < runs; run++) {
            long[] measured = benchRun(model, synced, renderer, layer, walk);
            nsPerWalk[run] = measured[0] / (double) BENCH_WALKS;
            bytesPerWalk[run] = measured[1] / (double) BENCH_WALKS;
        }
        double medianNs = median(nsPerWalk);
        double medianBytes = median(bytesPerWalk);
        int last = Math.min(BENCH_LAST_RUNS, runs);
        double lastNs = median(Arrays.copyOfRange(nsPerWalk, runs - last, runs));
        double lastBytes = median(Arrays.copyOfRange(bytesPerWalk, runs - last, runs));
        double[] warmupArray = new double[warmup.size()];
        for (int i = 0; i < warmupArray.length; i++) {
            warmupArray[i] = warmup.get(i);
        }
        JsonObject json = new JsonObject();
        json.addProperty("walk", description);
        json.addProperty("bones", bones);
        json.addProperty("synced_bones", synced.size());
        json.addProperty("warmup_runs_used", warmup.size());
        json.addProperty("warmup_settled", settled >= BENCH_WARMUP_SETTLED_RUNS);
        json.add("warmup_runs_ns_per_walk", arr(warmupArray));
        json.add("runs_ns_per_walk", arr(nsPerWalk));
        json.add("runs_bytes_per_walk", arr(bytesPerWalk));
        json.addProperty("median_ns_per_walk", medianNs);
        json.addProperty("median_ns_last_runs", last);
        json.addProperty("median_ns_last_runs_per_walk", lastNs);
        json.addProperty("median_bytes_per_walk", medianBytes);
        json.addProperty("median_bytes_last_runs_per_walk", lastBytes);
        json.addProperty("median_ns_per_bone", medianNs / bones);
        json.addProperty("median_ns_last_runs_per_bone", lastNs / bones);
        json.addProperty("median_bytes_per_bone", medianBytes / bones);
        out.append(String.format(Locale.ROOT, "%-20s bones %3d synced %2d | median %10.1f ns/walk (last %d: %10.1f) %9.1f B/walk | %7.1f ns/bone %6.1f B/bone | warm-up %2d runs%s %s | runs ns %s%n",
                name, bones, synced.size(), medianNs, last, lastNs, medianBytes, medianNs / bones, medianBytes / bones, warmup.size(),
                settled >= BENCH_WARMUP_SETTLED_RUNS ? "" : " (NOT settled)", Arrays.toString(warmupArray), Arrays.toString(nsPerWalk)));
        return json;
    }

    /** {@code {nanos, bytes}} for one run of {@value #BENCH_WALKS} walks. */
    private static long[] benchRun(BakedGeoModel model, List<String> synced, BenchGeoRenderer renderer,
                                   GeckolibBoneInformationCollectorLayer<GeoAnimatable> layer, BenchWalk walk) {
        double sink = 0.0D;
        long bytes0 = de.dertoaster.multihitboxlib.util.MHLibCollectorProbe.currentThreadAllocatedBytes();
        long t0 = System.nanoTime();
        List<GeoBone> tops = model.topLevelBones();
        for (int i = 0; i < BENCH_WALKS; i++) {
            layer.onPreRender();
            for (int t = 0, n = tops.size(); t < n; t++) {
                GeoBone top = tops.get(t);
                sink += walk == BenchWalk.COLLECT ? walkLayerBench(renderer, layer, top, synced) : walkTax(renderer, top);
            }
            layer.setScales(1, 1, 1);
            layer.setRotations(0, 0, 0);
            layer.onPostRender();
        }
        long t1 = System.nanoTime();
        long bytes1 = de.dertoaster.multihitboxlib.util.MHLibCollectorProbe.currentThreadAllocatedBytes();
        benchSink += sink;
        return new long[]{t1 - t0, Math.max(0L, bytes1 - bytes0)};
    }

    /**
     * The collecting walk: what {@code onRenderBone} does for a collecting profile, minus the entity-bound
     * builder call and the trust-client apply. The HEAD/TAIL hooks go through the real
     * {@code MixinGeoRenderer._mhlib_callLayers} (the method refs written as the mixin writes them:
     * non-capturing, linked once). The children are walked by index so the walk itself allocates nothing
     * (a for-each iterator per bone would add ~32 B/bone of the probe's own to the layer's count), and
     * every per-bone result is stored into {@link #BENCH_ESCAPE} so it escapes as in-game.
     */
    private static double walkLayerBench(BenchGeoRenderer renderer, GeckolibBoneInformationCollectorLayer<GeoAnimatable> layer, GeoBone bone, List<String> synced) {
        double sink = 0.0D;
        renderer._mhlib_callLayers(IMHLibExtendedRenderLayer::onRenderRecursivelyStart);   // MixinGeoRenderer @HEAD renderRecursively
        Vec3 worldPos = layer.getBoneWorldPosition(bone);
        BENCH_ESCAPE[0] = worldPos;                                                        // escapes as in-game (see BENCH_ESCAPE)
        sink += worldPos.x;
        layer.calcScales(bone);
        layer.calcRotations(bone);
        if (synced.contains(bone.getName())) {
            Vec3 scale = layer.getScaleVector();
            Vec3 rotation = layer.getRotationVector();
            BENCH_ESCAPE[1] = scale;
            BENCH_ESCAPE[2] = rotation;
            sink += scale.x + rotation.y;
        }
        List<GeoBone> children = bone.getChildBones();
        for (int i = 0, n = children.size(); i < n; i++) {
            sink += walkLayerBench(renderer, layer, children.get(i), synced);
        }
        renderer._mhlib_callLayers(IMHLibExtendedRenderLayer::onRenderRecursivelyEnd);     // @TAIL renderRecursively
        return sink;
    }

    /** The non-multipart walk: only the HEAD/TAIL hooks (through the real loop) reach the layer ({@code onRenderBone} returns at its multipart check). */
    private static double walkTax(BenchGeoRenderer renderer, GeoBone bone) {
        double sink = 0.0D;
        renderer._mhlib_callLayers(IMHLibExtendedRenderLayer::onRenderRecursivelyStart);
        List<GeoBone> children = bone.getChildBones();
        for (int i = 0, n = children.size(); i < n; i++) {
            sink += walkTax(renderer, children.get(i));
        }
        renderer._mhlib_callLayers(IMHLibExtendedRenderLayer::onRenderRecursivelyEnd);
        return sink + 1.0D;
    }

    private static int countBones(BakedGeoModel model) {
        int count = 0;
        for (GeoBone top : model.topLevelBones()) {
            count += countBones(top);
        }
        return count;
    }

    private static int countBones(GeoBone bone) {
        int count = 1;
        for (GeoBone child : bone.getChildBones()) {
            count += countBones(child);
        }
        return count;
    }

    private static double median(double[] values) {
        double[] sorted = values.clone();
        Arrays.sort(sorted);
        int n = sorted.length;
        return n % 2 == 1 ? sorted[n / 2] : 0.5D * (sorted[n / 2 - 1] + sorted[n / 2]);
    }

    /**
     * The layer as a replaced renderer's non-multipart entity sees it: collection inactive, so the hooks
     * return after their guard. A constant false stands in for the real chain (a GeoReplacedEntityRenderer
     * and a live entity), which makes the inactive rig a FLOOR of the in-game hook tax.
     */
    private static final class InactiveCollectorLayer extends GeckolibBoneInformationCollectorLayer<GeoAnimatable> {
        InactiveCollectorLayer(GeoRenderer<GeoAnimatable> renderer) {
            super(renderer);
        }

        @Override
        public boolean isBoneCollectionActive() {
            return false;
        }
    }

    /**
     * The bench renderer: the headless no-op renderer plus the REAL {@link MixinGeoRenderer#_mhlib_callLayers}
     * -- the mixin interface's default method, run as written over a layer list the rig fills -- so a walk's
     * HEAD/TAIL hooks reach the layer the way the mixin's {@code renderRecursively} injections deliver them
     * in-game: {@code getRenderLayers()}, the list iterator, the {@code instanceof IMHLibExtendedRenderLayer},
     * the {@code Consumer.accept}. ({@code MixinGeoRenderer} is a plain interface outside the mixin
     * environment; its injector methods are never called here.)
     */
    private static final class BenchGeoRenderer extends HeadlessGeoRenderer implements MixinGeoRenderer {
        private final List<GeoRenderLayer<GeoAnimatable>> layers = new ArrayList<>();

        <L extends GeoRenderLayer<GeoAnimatable>> L attach(L layer) {
            this.layers.add(layer);
            return layer;
        }

        @Override
        public List<GeoRenderLayer<GeoAnimatable>> getRenderLayers() {
            return this.layers;
        }
    }

    // ------------------------------------------------------------------ the per-entity render-tick gate (BUG-044)

    /**
     * BUG-044 (2026-09-04): drives the real layer's pass gate -- {@code beginRenderPass} /
     * {@code isCollectingPass} / {@code endRenderPass}, the calls
     * {@code IBoneInformationCollectorLayerCommonLogic.onPreRender(Entity)} / {@code onPostRender(Entity)}
     * make around a rendered entity's bones with the entity's own render-tick stamp
     * ({@code IMHLibFieldAccessor._mhlibAccess_get/setRenderTickStamp}) -- with fake entities: a tick
     * count and a stamp, which is all the gate reads. Two checks, each failing the probe: the hitch
     * (the fake entity's tickCount jumps by 2 between two passes: the second pass must collect, a
     * further frame in the same tick must not) and two entities alternating through ONE layer (both
     * must collect on every tick, each carrying its own stamp). The bones are walked on every pass as
     * in {@link #runLifecycle}, so the decision is observed where {@code onRenderBone} reads it.
     */
    private static RenderTickGateResult runRenderTickGate(BakedGeoModel model, List<String> synced, StringBuilder out) {
        out.append("== per-entity render-tick gate through the real GeckolibBoneInformationCollectorLayer (BUG-044 check)\n");
        GeckolibBoneInformationCollectorLayer<GeoAnimatable> layer =
                new GeckolibBoneInformationCollectorLayer<>(new HeadlessGeoRenderer());
        JsonObject json = new JsonObject();

        // (1) the hitch: a pass at tick 10, then the entity ticks twice before the next frame
        FakeStampedEntity hitch = new FakeStampedEntity(10);
        boolean firstPass = renderPass(layer, model, synced, hitch);
        int stampAfterFirst = hitch.stamp;
        hitch.tickCount += 2;
        boolean afterJump = renderPass(layer, model, synced, hitch);
        int stampAfterJump = hitch.stamp;
        boolean sameTickAgain = renderPass(layer, model, synced, hitch);
        boolean hitchPass = firstPass && stampAfterFirst == 10 && afterJump && stampAfterJump == 12
                && !sameTickAgain && hitch.stamp == 12;
        out.append(String.format(Locale.ROOT,
                "hitch: pass at tick 10 collects=%b (stamp %d); tickCount -> 12; pass collects=%b (stamp %d); same tick again collects=%b (stamp %d) -> %s%n",
                firstPass, stampAfterFirst, afterJump, stampAfterJump, sameTickAgain, hitch.stamp, hitchPass ? "ok" : "WEDGED"));
        JsonObject hitchJson = new JsonObject();
        hitchJson.addProperty("first_pass_collects", firstPass);
        hitchJson.addProperty("stamp_after_first_pass", stampAfterFirst);
        hitchJson.addProperty("pass_after_two_tick_jump_collects", afterJump);
        hitchJson.addProperty("stamp_after_jump_pass", stampAfterJump);
        hitchJson.addProperty("same_tick_second_frame_collects", sameTickAgain);
        hitchJson.addProperty("stamp_after_same_tick_frame", hitch.stamp);
        hitchJson.addProperty("pass", hitchPass);
        json.add("hitch", hitchJson);

        // (2) two entities through the same layer, alternating, three ticks each, two frames per tick
        FakeStampedEntity a = new FakeStampedEntity(0);
        FakeStampedEntity b = new FakeStampedEntity(100);
        boolean twoPass = true;
        JsonArray ticks = new JsonArray();
        for (int tick = 1; tick <= 3; tick++) {
            a.tickCount++;
            b.tickCount++;
            boolean collectsA = renderPass(layer, model, synced, a);
            boolean collectsB = renderPass(layer, model, synced, b);
            boolean secondA = renderPass(layer, model, synced, a);
            boolean secondB = renderPass(layer, model, synced, b);
            boolean ok = collectsA && collectsB && !secondA && !secondB && a.stamp == a.tickCount && b.stamp == b.tickCount;
            twoPass &= ok;
            out.append(String.format(Locale.ROOT,
                    "two entities, tick %d: A (tick %d) collects=%b then %b, stamp %d; B (tick %d) collects=%b then %b, stamp %d -> %s%n",
                    tick, a.tickCount, collectsA, secondA, a.stamp, b.tickCount, collectsB, secondB, b.stamp, ok ? "ok" : "STARVED"));
            JsonObject row = new JsonObject();
            row.addProperty("tick", tick);
            row.addProperty("a_tick_count", a.tickCount);
            row.addProperty("a_collects", collectsA);
            row.addProperty("a_second_frame_collects", secondA);
            row.addProperty("a_stamp", a.stamp);
            row.addProperty("b_tick_count", b.tickCount);
            row.addProperty("b_collects", collectsB);
            row.addProperty("b_second_frame_collects", secondB);
            row.addProperty("b_stamp", b.stamp);
            row.addProperty("pass", ok);
            ticks.add(row);
        }
        JsonObject twoJson = new JsonObject();
        twoJson.add("ticks", ticks);
        twoJson.addProperty("pass", twoPass);
        json.add("two_entities", twoJson);

        boolean pass = hitchPass && twoPass;
        json.addProperty("pass", pass);
        out.append("RENDER-TICK GATE (hitch: a two-tick jump still collects and a tick collects once; two entities through one layer: both collect every tick with their own stamps): ")
                .append(pass ? "PASS" : "FAIL").append("\n\n");
        return new RenderTickGateResult(pass, json);
    }

    /**
     * One rendered frame of {@code fake} through the layer in GeckolibEntityRenderEventHandler's order:
     * beginRenderPass (onPreRender(Entity)), the layer's onPreRender, the bone walk, the post hook's
     * setScales/setRotations and endRenderPass (onPostRender(Entity)), the layer's onPostRender. Returns
     * whether the pass collected, i.e. what onRenderBone gated tryAddBoneInformation on for every bone of
     * the walk; a decision that did not hold through the walk or past the end is a probe error.
     */
    private static boolean renderPass(GeckolibBoneInformationCollectorLayer<GeoAnimatable> layer, BakedGeoModel model,
                                      List<String> synced, FakeStampedEntity fake) {
        boolean decided = layer.beginRenderPass(fake.stamp, fake.tickCount);
        layer.onPreRender();
        Map<String, Vec3> shipped = new LinkedHashMap<>();
        for (GeoBone top : model.topLevelBones()) {
            walkLayer(layer, top, synced, shipped);
        }
        if (layer.isCollectingPass() != decided) {
            throw new IllegalStateException("collecting-pass flag changed during the bone walk: decided " + decided
                    + ", after the walk " + layer.isCollectingPass());
        }
        layer.setScales(1, 1, 1);
        layer.setRotations(0, 0, 0);
        fake.stamp = layer.endRenderPass(fake.stamp, fake.tickCount);
        layer.onPostRender();
        if (layer.isCollectingPass()) {
            throw new IllegalStateException("collecting-pass flag still set after endRenderPass");
        }
        return decided;
    }

    /** A rendered entity as the gate sees it: its tickCount and its own render-tick stamp (IMHLibFieldAccessor). */
    private static final class FakeStampedEntity {
        int tickCount;
        int stamp = RenderTickGate.UNSTAMPED;

        FakeStampedEntity(int tickCount) {
            this.tickCount = tickCount;
        }
    }

    /** A GeoRenderer with no Minecraft behind it; GeoRenderLayer only stores it and isBoneCollectionActive only instanceof-checks it. */
    private static class HeadlessGeoRenderer implements GeoRenderer<GeoAnimatable> {
        @Override
        public GeoModel<GeoAnimatable> getGeoModel() {
            return null;
        }

        @Override
        public GeoAnimatable getAnimatable() {
            return null;
        }

        @Override
        public void fireCompileRenderLayersEvent() {
        }

        @Override
        public boolean firePreRenderEvent(PoseStack poseStack, BakedGeoModel model, MultiBufferSource bufferSource,
                                          float partialTick, int packedLight) {
            return true;
        }

        @Override
        public void firePostRenderEvent(PoseStack poseStack, BakedGeoModel model, MultiBufferSource bufferSource,
                                        float partialTick, int packedLight) {
        }

        @Override
        public void updateAnimatedTextureFrame(GeoAnimatable animatable) {
        }
    }

    // ------------------------------------------------------------------ MHLib placement

    /**
     * MHLibPartEntity.applyInformation (vendored :423-438): rotated pivot scaled by the parent's
     * IMHLibSizeCallback scale, part at worldPos - pivot, dimensions = profile size x bone scale x
     * entityScale (BoneInformation.scale(entityScale) in alignSynchedSubParts :375, getDimensions :333-341).
     */
    private static Box placePart(Part part, BoneSample sample, Vec3 shippedRotation, double entityScale) {
        Vec3 pivot = rotateLikeMhlib(part.pivot, shippedRotation).scale(entityScale);
        double px = sample.worldPos.x - pivot.x;
        double py = sample.worldPos.y - pivot.y;
        double pz = sample.worldPos.z - pivot.z;
        double w = part.width * sample.scale.x * entityScale;
        double h = part.height * sample.scale.y * entityScale;
        return new Box(new double[]{px - w / 2.0D, py, pz - w / 2.0D}, new double[]{px + w / 2.0D, py + h, pz + w / 2.0D});
    }

    /** {@code pivot.xRot(rx).yRot(ry).zRot(rz)} exactly as MHLibPartEntity.applyInformation :426. */
    private static Vec3 rotateLikeMhlib(Vec3 v, Vec3 rotation) {
        return v.xRot((float) rotation.x).yRot((float) rotation.y).zRot((float) rotation.z);
    }

    private static Vec3 inverseRotate(Vec3 world, Vec3 rotation) {
        return world.zRot((float) -rotation.z).yRot((float) -rotation.y).xRot((float) -rotation.x);
    }

    /** Vec3.xRot(rx).yRot(ry).zRot(rz) with the same formulas in double precision (Math trig instead of Mth's tables). */
    private static double[] rotateExact(Vec3 v, double rx, double ry, double rz) {
        double x = v.x;
        double y = v.y;
        double z = v.z;
        double c = Math.cos(rx);
        double s = Math.sin(rx);
        double y1 = y * c + z * s;                    // Vec3.xRot
        double z1 = z * c - y * s;
        double[] afterY = yRotExact(new double[]{x, y1, z1}, ry);
        c = Math.cos(rz);
        s = Math.sin(rz);
        return new double[]{afterY[0] * c + afterY[1] * s, afterY[1] * c - afterY[0] * s, afterY[2]}; // Vec3.zRot
    }

    /** Vec3.yRot in double precision: x' = x cos + z sin, z' = z cos - x sin. */
    private static double[] yRotExact(double[] v, double angle) {
        double c = Math.cos(angle);
        double s = Math.sin(angle);
        return new double[]{v[0] * c + v[2] * s, v[1], v[2] * c - v[0] * s};
    }

    /** The vendored collector's body-yaw fold, reached by reflection so the same probe binary runs on the old and the fixed layer. */
    private static final class YawFold {
        final boolean present;
        private final Method term;
        private final Method fold;

        private YawFold(Method term, Method fold) {
            this.term = term;
            this.fold = fold;
            this.present = term != null && fold != null;
        }

        static YawFold detect() {
            Method term = null;
            Method fold = null;
            try {
                term = GeckolibBoneInformationCollectorLayer.class.getMethod("bodyYawRotationTerm", float.class);
                fold = GeckolibBoneInformationCollectorLayer.class.getMethod("foldBodyYaw", double.class, double.class, double.class, double.class);
            } catch (NoSuchMethodException ignored) {
                // old collector: no body-yaw fold
            }
            return new YawFold(term, fold);
        }

        String describe() {
            return present
                    ? "GeckolibBoneInformationCollectorLayer has bodyYawRotationTerm/foldBodyYaw (yaw-folding collector)"
                    : "GeckolibBoneInformationCollectorLayer has NO body-yaw fold (pre-fix collector: ships the bare bone rotation sum)";
        }

        double term(float bodyYawDegrees) throws Exception {
            return present ? (double) term.invoke(null, bodyYawDegrees) : 0.0D;
        }

        Vec3 ship(Vec3 summed, double yawTerm) throws Exception {
            return present ? (Vec3) fold.invoke(null, summed.x, summed.y, summed.z, yawTerm) : summed;
        }
    }

    // ------------------------------------------------------------------ geometry helpers

    private static void indexBones(GeoBone bone, GeoBone parent, Map<String, GeoBone> out, Map<String, GeoBone> parents) {
        out.put(bone.getName(), bone);
        if (parent != null) {
            parents.put(bone.getName(), parent);
        }
        for (GeoBone child : bone.getChildBones()) {
            indexBones(child, bone, out, parents);
        }
    }

    /** subtree(bone) minus the subtrees of its synced descendants: the chain segment one part covers. */
    private static Map<String, Set<String>> segments(Map<String, GeoBone> bones, List<String> synced) {
        Map<String, Set<String>> out = new LinkedHashMap<>();
        for (String name : synced) {
            Set<String> segment = new LinkedHashSet<>();
            collectSegment(bones.get(name), synced, true, segment);
            out.put(name, segment);
        }
        return out;
    }

    private static void collectSegment(GeoBone bone, List<String> synced, boolean root, Set<String> out) {
        if (!root && synced.contains(bone.getName())) {
            return;
        }
        out.add(bone.getName());
        for (GeoBone child : bone.getChildBones()) {
            collectSegment(child, synced, false, out);
        }
    }

    private static double[][] segmentBox(Map<String, BoneSample> samples, Set<String> segment) {
        double[] min = {Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY};
        double[] max = {Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY};
        boolean any = false;
        for (String name : segment) {
            BoneSample s = samples.get(name);
            if (s == null || s.cubeMin == null) {
                continue;
            }
            for (int i = 0; i < 3; i++) {
                min[i] = Math.min(min[i], s.cubeMin[i]);
                max[i] = Math.max(max[i], s.cubeMax[i]);
            }
            any = true;
        }
        return any ? new double[][]{min, max} : null;
    }

    private static double coveredFraction(Box box, double[][] seg) {
        double vol = 1.0D;
        double inter = 1.0D;
        for (int i = 0; i < 3; i++) {
            double extent = Math.max(seg[1][i] - seg[0][i], 1.0E-9D);
            double lo = Math.max(box.min[i], seg[0][i]);
            double hi = Math.min(box.max[i], seg[1][i]);
            vol *= extent;
            inter *= Math.max(0.0D, hi - lo);
        }
        return inter / vol;
    }

    /** {width - max(dx, dz), height - dy, |xz centre offset|, |bottom offset|}, all absolute. */
    private static double[] fitErrors(Box box, double[][] seg) {
        double dx = seg[1][0] - seg[0][0];
        double dy = seg[1][1] - seg[0][1];
        double dz = seg[1][2] - seg[0][2];
        double boxW = Math.max(box.max[0] - box.min[0], box.max[2] - box.min[2]);
        double boxH = box.max[1] - box.min[1];
        double cxErr = Math.abs(0.5D * (box.min[0] + box.max[0]) - 0.5D * (seg[0][0] + seg[1][0]));
        double czErr = Math.abs(0.5D * (box.min[2] + box.max[2]) - 0.5D * (seg[0][2] + seg[1][2]));
        return new double[]{Math.abs(boxW - Math.max(dx, dz)), Math.abs(boxH - dy), Math.max(cxErr, czErr),
                Math.abs(box.min[1] - seg[0][1])};
    }

    private static boolean contains(Box box, double[] p) {
        for (int i = 0; i < 3; i++) {
            if (p[i] < box.min[i] || p[i] > box.max[i]) {
                return false;
            }
        }
        return true;
    }

    private static double[] mid(double[] a, double[] b) {
        return new double[]{0.5D * (a[0] + b[0]), 0.5D * (a[1] + b[1]), 0.5D * (a[2] + b[2])};
    }

    private static Vector3d scaled(Vector3d v, double s) {
        return new Vector3d(v.x * s, v.y * s, v.z * s);
    }

    private static double maxAbsDiff(Vector3d a, Vector3d b) {
        return Math.max(Math.abs(a.x - b.x), Math.max(Math.abs(a.y - b.y), Math.abs(a.z - b.z)));
    }

    private static String ratio(Vector3d num, Vector3d den) {
        double best = Double.NaN;
        double bestMag = 0.0D;
        double[] n = {num.x, num.y, num.z};
        double[] d = {den.x, den.y, den.z};
        for (int i = 0; i < 3; i++) {
            if (Math.abs(d[i]) > bestMag) {
                bestMag = Math.abs(d[i]);
                best = n[i] / d[i];
            }
        }
        return String.format(Locale.ROOT, "%.5f", best);
    }

    private static String vec(Vector3d v) {
        return String.format(Locale.ROOT, "(%.3f, %.3f, %.3f)", v.x, v.y, v.z);
    }

    private static String vec(Vec3 v) {
        return String.format(Locale.ROOT, "(%.3f, %.3f, %.3f)", v.x, v.y, v.z);
    }

    private static String boxString(double[][] b) {
        return String.format(Locale.ROOT, "%.2f..%.2f/%.2f..%.2f/%.2f..%.2f", b[0][0], b[1][0], b[0][1], b[1][1], b[0][2], b[1][2]);
    }

    private static JsonArray names(List<String> values) {
        JsonArray a = new JsonArray();
        values.forEach(a::add);
        return a;
    }

    private static JsonArray vecJson(Vector3d v) {
        return arr(v.x, v.y, v.z);
    }

    private static JsonArray vecJson(Vec3 v) {
        return arr(v.x, v.y, v.z);
    }

    private static JsonArray vecJson(double[] v) {
        return arr(v);
    }

    private static JsonArray arr(double... values) {
        JsonArray a = new JsonArray();
        for (double v : values) {
            a.add(v);
        }
        return a;
    }

    // ------------------------------------------------------------------ records

    private record BoneSample(String name, boolean synced, Vector3d worldPos, Vec3 rotation, Vec3 scale,
                              double[] cubeMin, double[] cubeMax) {
    }

    private record Mode(String name, Map<String, BoneSample> samples, double entityScale, float renderScale) {
    }

    private record LifecycleResult(boolean pass, JsonObject json) {
    }

    private record RenderTickGateResult(boolean pass, JsonObject json) {
    }

    private record Box(double[] min, double[] max) {
        @Override
        public String toString() {
            return String.format(Locale.ROOT, "%.2f..%.2f/%.2f..%.2f/%.2f..%.2f", min[0], max[0], min[1], max[1], min[2], max[2]);
        }
    }

    private record Part(String name, double width, double height, Vec3 position, Vec3 pivot) {
    }

    private static final class Profile {
        final List<String> syncedBones = new ArrayList<>();
        final Map<String, Part> parts = new LinkedHashMap<>();

        static Profile read(Path path) throws Exception {
            JsonObject root = JsonParser.parseString(Files.readString(path, StandardCharsets.UTF_8)).getAsJsonObject();
            Profile profile = new Profile();
            for (JsonElement e : root.getAsJsonArray("synched-bones")) {
                profile.syncedBones.add(e.getAsString());
            }
            for (JsonElement e : root.getAsJsonArray("parts")) {
                JsonObject part = e.getAsJsonObject();
                JsonObject box = part.getAsJsonObject("box");
                JsonArray size = box.getAsJsonArray("size");
                Vec3 position = vec3(box.getAsJsonArray("position"));
                Vec3 pivot = box.has("pivot") ? vec3(box.getAsJsonArray("pivot")) : Vec3.ZERO;
                String name = part.get("name").getAsString();
                profile.parts.put(name, new Part(name, size.get(0).getAsDouble(), size.get(1).getAsDouble(), position, pivot));
            }
            return profile;
        }

        private static Vec3 vec3(JsonArray a) {
            return new Vec3(a.get(0).getAsDouble(), a.get(1).getAsDouble(), a.get(2).getAsDouble());
        }
    }
}
