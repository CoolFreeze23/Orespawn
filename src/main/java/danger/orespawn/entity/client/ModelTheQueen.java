package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.TheQueen;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

/**
 * The Queen's entity model, ported from 1.7.10 Techne-generated {@code ModelTheQueen}
 * (see {@code reference_1_7_10_source/sources/danger/orespawn/ModelTheQueen.java}).
 *
 * <h2>Techne &rarr; Blaze3D paradigm shift</h2>
 * The legacy 1.7.10 model used flat {@code ModelRenderer} siblings with per-frame
 * absolute-position fixups inside {@code func_78088_a}. This modern port instead
 * arranges all 131 parts as a TRUE PARENT/CHILD TREE via
 * {@link PartDefinition#addOrReplaceChild(String, CubeListBuilder, PartPose)} so
 * that rotating a parent automatically cascades position and rotation to every
 * descendant. Animations only manipulate {@code xRot / yRot / zRot} (and
 * {@code body.y} for breathing), matching modern Blaze3D conventions.
 *
 * <h2>Rotations</h2>
 * All rotation literals are in RADIANS ({@link Mth#PI}, {@link Mth#DEG_TO_RAD},
 * {@code Mth.cos/sin}) &mdash; matching Blaze3D's {@code xRot/yRot/zRot} fields.
 * The legacy degree/radian mix from 1.7.10 has been fully normalized.
 *
 * <h2>Texture / UV</h2>
 * Texture is 2048x2048 ({@code thequeentexture.png}, {@code thequeentexture2.png}
 * for the "happy mood" variant). UV offsets in {@code .texOffs(u, v)} are 1:1
 * pixel coordinates into that sheet, mirroring the {@code texOffs} values from
 * the 1.7.10 source.
 *
 * <h2>Render passes</h2>
 * Each pass uses a distinct {@link net.minecraft.client.renderer.RenderType}
 * buffer, wired up in {@link TheQueenRenderer#render}:
 * <ol>
 *   <li>Opaque &mdash; {@link #renderToBuffer} (skips membranes, cubes, eyes).</li>
 *   <li>Translucent wing membranes &mdash; {@link #renderWingMembranes} (manual
 *       chain walk, ARGB-tinted).</li>
 *   <li>Fullbright power cubes &mdash; {@link #renderPowerCubes} (manual chain
 *       walk, entityCutoutNoCull buffer).</li>
 *   <li>Fullbright eyes &mdash; {@link #renderEyes} (manual chain walk for glow).</li>
 * </ol>
 */
public class ModelTheQueen extends HierarchicalModel<TheQueen> {

    private final ModelPart root;

    // ── core ───────────────────────────────────────────────────────────────
    private final ModelPart body;
    private final ModelPart Back1, Back2;
    private final ModelPart Ridge1, Ridge2, Ridge3, Ridge4, Ridge5, Ridge6;
    private final ModelPart NeckBone1, NeckBone2, NeckBone3;
    private final ModelPart Rib1, Rib2, Rib3, Rib4, Rib5, Rib6;
    private final ModelPart PowerCube1, PowerCube2, PowerCube3;

    // ── tail chain ─────────────────────────────────────────────────────────
    private final ModelPart Tail1, Tail2, Tail3, Tail4, Tail5, Tail6, Tail7;
    private final ModelPart TailTip, TailTip2, TailSpike;

    // ── centre neck / head ─────────────────────────────────────────────────
    private final ModelPart NeckC1, NeckC2, NeckC3, NeckC4;
    private final ModelPart CHead1, CHead2, CHead3;
    private final ModelPart CJaw1, CJaw2, CJaw3;
    private final ModelPart CTooth1, CTooth2, CTooth3, CTooth4;
    private final ModelPart CLEye, CREye;
    private final ModelPart CHeadMane, CLNoseSpike, CRNoseSpike;

    // ── left neck / head ───────────────────────────────────────────────────
    private final ModelPart NeckL1, NeckL2, NeckL3, NeckL4;
    private final ModelPart LHead1, LHead2, LHead3;
    private final ModelPart LJaw1, LJaw2, LJaw3;
    private final ModelPart LTooth1, LTooth2, LTooth3, LTooth4;
    private final ModelPart LLEye, LREye;
    private final ModelPart LHeadMane, LLNoseSpike, LRNoseSpike;

    // ── right neck / head ──────────────────────────────────────────────────
    private final ModelPart NeckR1, NeckR2, NeckR3, NeckR4;
    private final ModelPart RHead1, RHead2, RHead3;
    private final ModelPart RJaw1, RJaw2, RJaw3;
    private final ModelPart RTooth1, RTooth2, RTooth3, RTooth4;
    private final ModelPart RLEye, RREye;
    private final ModelPart RHeadMane, RLNoseSpike, RRNoseSpike;

    // ── left leg chain ─────────────────────────────────────────────────────
    private final ModelPart LThigh, LUpperLeg, LLowerLeg, LFoot;
    private final ModelPart LClawRear;
    private final ModelPart LLClaw1, LLClaw2;
    private final ModelPart LCClaw1, LCClaw2;
    private final ModelPart LRClaw1, LRClaw2;

    // ── right leg chain ────────────────────────────────────────────────────
    private final ModelPart RThigh, RUpperLeg, RLowerLeg, RFoot;
    private final ModelPart RClawRear;
    private final ModelPart RLClaw1, RLClaw2;
    private final ModelPart RCClaw1, RCClaw2;
    private final ModelPart RRClaw1, RRClaw2;

    // ── left wing chain ────────────────────────────────────────────────────
    private final ModelPart Lwing1, Lwing2, Lwing3, Lwing4, Lwing5, Lwing6;
    private final ModelPart Lwing7, Lwing8, Lwing9, Lwing10;

    // ── right wing chain ───────────────────────────────────────────────────
    private final ModelPart Rwing1, Rwing2, Rwing3, Rwing4, Rwing5, Rwing6;
    private final ModelPart Rwing7, Rwing8, Rwing9, Rwing10;

    // render-group arrays (populated in constructor)
    private final ModelPart[] wingMembranes;
    private final ModelPart[] powerCubes;
    private final ModelPart[] eyes;

    // ════════════════════════════════════════════════════════════════════════
    //  Constructor – walk the baked hierarchy to grab every ModelPart ref
    // ════════════════════════════════════════════════════════════════════════
    public ModelTheQueen(ModelPart root) {
        this.root = root;
        this.body = root.getChild("Body1");

        Back1 = body.getChild("Back1");
        Back2 = body.getChild("Back2");
        Ridge1 = body.getChild("Ridge1");
        Ridge2 = body.getChild("Ridge2");
        Ridge3 = body.getChild("Ridge3");
        NeckBone1 = body.getChild("NeckBone1");
        NeckBone2 = body.getChild("NeckBone2");
        NeckBone3 = body.getChild("NeckBone3");
        Rib1 = body.getChild("Rib1");
        Rib2 = body.getChild("Rib2");
        Rib3 = body.getChild("Rib3");
        Rib4 = body.getChild("Rib4");
        Rib5 = body.getChild("Rib5");
        Rib6 = body.getChild("Rib6");
        PowerCube1 = body.getChild("PowerCube1");
        PowerCube2 = body.getChild("PowerCube2");
        PowerCube3 = body.getChild("PowerCube3");

        // tail
        Tail1 = body.getChild("Tail1");
        Ridge4 = Tail1.getChild("Ridge4");
        Ridge5 = Tail1.getChild("Ridge5");
        Tail2 = Tail1.getChild("Tail2");
        Tail3 = Tail2.getChild("Tail3");
        Tail4 = Tail3.getChild("Tail4");
        Tail5 = Tail4.getChild("Tail5");
        Tail6 = Tail5.getChild("Tail6");
        Tail7 = Tail6.getChild("Tail7");
        Ridge6 = Tail7.getChild("Ridge6");
        TailTip = Tail7.getChild("TailTip");
        TailTip2 = TailTip.getChild("TailTip2");
        TailSpike = TailTip.getChild("TailSpike");

        // centre neck
        NeckC1 = body.getChild("NeckC1");
        NeckC2 = NeckC1.getChild("NeckC2");
        NeckC3 = NeckC2.getChild("NeckC3");
        NeckC4 = NeckC3.getChild("NeckC4");
        CHead1 = NeckC4.getChild("CHead1");
        CHead2 = CHead1.getChild("CHead2");
        CHead3 = CHead1.getChild("CHead3");
        CLEye = CHead1.getChild("CLEye");
        CREye = CHead1.getChild("CREye");
        CHeadMane = CHead1.getChild("CHeadMane");
        CLNoseSpike = CHead1.getChild("CLNoseSpike");
        CRNoseSpike = CHead1.getChild("CRNoseSpike");
        CJaw1 = CHead1.getChild("CJaw1");
        CJaw2 = CJaw1.getChild("CJaw2");
        CJaw3 = CJaw1.getChild("CJaw3");
        CTooth1 = CJaw1.getChild("CTooth1");
        CTooth2 = CJaw1.getChild("CTooth2");
        CTooth3 = CJaw1.getChild("CTooth3");
        CTooth4 = CJaw1.getChild("CTooth4");

        // left neck
        NeckL1 = body.getChild("NeckL1");
        NeckL2 = NeckL1.getChild("NeckL2");
        NeckL3 = NeckL2.getChild("NeckL3");
        NeckL4 = NeckL3.getChild("NeckL4");
        LHead1 = NeckL4.getChild("LHead1");
        LHead2 = LHead1.getChild("LHead2");
        LHead3 = LHead1.getChild("LHead3");
        LLEye = LHead1.getChild("LLEye");
        LREye = LHead1.getChild("LREye");
        LHeadMane = LHead1.getChild("LHeadMane");
        LLNoseSpike = LHead1.getChild("LLNoseSpike");
        LRNoseSpike = LHead1.getChild("LRNoseSpike");
        LJaw1 = LHead1.getChild("LJaw1");
        LJaw2 = LJaw1.getChild("LJaw2");
        LJaw3 = LJaw1.getChild("LJaw3");
        LTooth1 = LJaw1.getChild("LTooth1");
        LTooth2 = LJaw1.getChild("LTooth2");
        LTooth3 = LJaw1.getChild("LTooth3");
        LTooth4 = LJaw1.getChild("LTooth4");

        // right neck
        NeckR1 = body.getChild("NeckR1");
        NeckR2 = NeckR1.getChild("NeckR2");
        NeckR3 = NeckR2.getChild("NeckR3");
        NeckR4 = NeckR3.getChild("NeckR4");
        RHead1 = NeckR4.getChild("RHead1");
        RHead2 = RHead1.getChild("RHead2");
        RHead3 = RHead1.getChild("RHead3");
        RLEye = RHead1.getChild("RLEye");
        RREye = RHead1.getChild("RREye");
        RHeadMane = RHead1.getChild("RHeadMane");
        RLNoseSpike = RHead1.getChild("RLNoseSpike");
        RRNoseSpike = RHead1.getChild("RRNoseSpike");
        RJaw1 = RHead1.getChild("RJaw1");
        RJaw2 = RJaw1.getChild("RJaw2");
        RJaw3 = RJaw1.getChild("RJaw3");
        RTooth1 = RJaw1.getChild("RTooth1");
        RTooth2 = RJaw1.getChild("RTooth2");
        RTooth3 = RJaw1.getChild("RTooth3");
        RTooth4 = RJaw1.getChild("RTooth4");

        // left leg
        LThigh = body.getChild("LThigh");
        LUpperLeg = LThigh.getChild("LUpperLeg");
        LLowerLeg = LThigh.getChild("LLowerLeg");
        LFoot = LLowerLeg.getChild("LFoot");
        LClawRear = LFoot.getChild("LClawRear");
        LLClaw1 = LFoot.getChild("LLClaw1");
        LLClaw2 = LLClaw1.getChild("LLClaw2");
        LCClaw1 = LFoot.getChild("LCClaw1");
        LCClaw2 = LCClaw1.getChild("LCClaw2");
        LRClaw1 = LFoot.getChild("LRClaw1");
        LRClaw2 = LRClaw1.getChild("LRClaw2");

        // right leg
        RThigh = body.getChild("RThigh");
        RUpperLeg = RThigh.getChild("RUpperLeg");
        RLowerLeg = RThigh.getChild("RLowerLeg");
        RFoot = RLowerLeg.getChild("RFoot");
        RClawRear = RFoot.getChild("RClawRear");
        RLClaw1 = RFoot.getChild("RLClaw1");
        RLClaw2 = RLClaw1.getChild("RLClaw2");
        RCClaw1 = RFoot.getChild("RCClaw1");
        RCClaw2 = RCClaw1.getChild("RCClaw2");
        RRClaw1 = RFoot.getChild("RRClaw1");
        RRClaw2 = RRClaw1.getChild("RRClaw2");

        // left wing
        Lwing1 = body.getChild("Lwing1");
        Lwing2 = Lwing1.getChild("Lwing2");
        Lwing3 = Lwing1.getChild("Lwing3");
        Lwing4 = Lwing3.getChild("Lwing4");
        Lwing7 = Lwing3.getChild("Lwing7");
        Lwing8 = Lwing7.getChild("Lwing8");
        Lwing9 = Lwing3.getChild("Lwing9");
        Lwing10 = Lwing9.getChild("Lwing10");
        Lwing5 = Lwing3.getChild("Lwing5");
        Lwing6 = Lwing5.getChild("Lwing6");

        // right wing
        Rwing1 = body.getChild("Rwing1");
        Rwing2 = Rwing1.getChild("Rwing2");
        Rwing3 = Rwing1.getChild("Rwing3");
        Rwing4 = Rwing3.getChild("Rwing4");
        Rwing7 = Rwing3.getChild("Rwing7");
        Rwing8 = Rwing7.getChild("Rwing8");
        Rwing9 = Rwing3.getChild("Rwing9");
        Rwing10 = Rwing9.getChild("Rwing10");
        Rwing5 = Rwing3.getChild("Rwing5");
        Rwing6 = Rwing5.getChild("Rwing6");

        // render-group arrays
        wingMembranes = new ModelPart[]{
            Lwing2, Lwing4, Lwing6, Lwing8, Lwing10,
            Rwing2, Rwing4, Rwing6, Rwing8, Rwing10
        };
        powerCubes = new ModelPart[]{ PowerCube1, PowerCube2, PowerCube3 };
        eyes = new ModelPart[]{ LLEye, LREye, CLEye, CREye, RLEye, RREye };
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  createBodyLayer – hierarchical skeleton with relative offsets
    //  Original 1.7.10 used absolute world coordinates; every child offset
    //  below has been recalculated relative to its parent pivot.
    // ════════════════════════════════════════════════════════════════════════
    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // ── Body (root child) ──────────────────────────────────────────────
        PartDefinition body = root.addOrReplaceChild("Body1",
            CubeListBuilder.create().mirror()
                .texOffs(500, 732).addBox(-40.0F, -32.0F, -24.0F, 80, 62, 60),
            PartPose.offset(0.0F, -89.0F, 1.0F));

        // back plates
        body.addOrReplaceChild("Back1",
            CubeListBuilder.create().mirror()
                .texOffs(167, 186).addBox(0.0F, 0.0F, 0.0F, 30, 15, 49),
            PartPose.offsetAndRotation(-45.0F, -38.0F, -59.0F, -0.0698F, 0, 0));
        body.addOrReplaceChild("Back2",
            CubeListBuilder.create().mirror()
                .texOffs(0, 186).addBox(0.0F, 0.0F, 0.0F, 30, 15, 49),
            PartPose.offsetAndRotation(15.0F, -38.0F, -59.0F, -0.0698F, 0, 0));

        // ridges on body
        body.addOrReplaceChild("Ridge1",
            CubeListBuilder.create().mirror()
                .texOffs(850, 717).addBox(-1.0F, 0.0F, 0.0F, 3, 10, 24),
            PartPose.offsetAndRotation(0, -33.0F, -76.0F, 0.3665F, 0, 0));
        body.addOrReplaceChild("Ridge2",
            CubeListBuilder.create().mirror()
                .texOffs(850, 676).addBox(-1.0F, 0.0F, 0.0F, 3, 10, 24),
            PartPose.offsetAndRotation(0, -33.0F, -51.0F, 0.3665F, 0, 0));
        body.addOrReplaceChild("Ridge3",
            CubeListBuilder.create().mirror()
                .texOffs(800, 600).addBox(-1.0F, 0.0F, 0.0F, 3, 20, 49),
            PartPose.offsetAndRotation(0, -31.0F, -21.0F, 0.3665F, 0, 0));

        // neck bones (static decoration)
        body.addOrReplaceChild("NeckBone1",
            CubeListBuilder.create().mirror()
                .texOffs(300, 2).addBox(0.0F, 0.0F, 0.0F, 5, 11, 57),
            PartPose.offset(-2.0F, -34.0F, -80.0F));
        body.addOrReplaceChild("NeckBone2",
            CubeListBuilder.create().mirror()
                .texOffs(300, 78).addBox(0.0F, 0.0F, 0.0F, 5, 11, 21),
            PartPose.offset(27.0F, -34.0F, -80.0F));
        body.addOrReplaceChild("NeckBone3",
            CubeListBuilder.create().mirror()
                .texOffs(365, 78).addBox(0.0F, 0.0F, 0.0F, 5, 11, 21),
            PartPose.offset(-33.0F, -34.0F, -80.0F));

        // ribs
        body.addOrReplaceChild("Rib1",
            CubeListBuilder.create().mirror()
                .texOffs(0, 39).addBox(-2.0F, -2.0F, -12.0F, 4, 4, 24),
            PartPose.offsetAndRotation(29.0F, 18.0F, -68.0F, 0.5236F, 0.6632F, 0));
        body.addOrReplaceChild("Rib2",
            CubeListBuilder.create().mirror()
                .texOffs(0, 78).addBox(-2.0F, -2.0F, -10.0F, 4, 4, 19),
            PartPose.offsetAndRotation(28.0F, 23.0F, -54.0F, 0.5236F, 0.6632F, 0));
        body.addOrReplaceChild("Rib3",
            CubeListBuilder.create().mirror()
                .texOffs(0, 113).addBox(-2.0F, -2.0F, -10.0F, 4, 4, 20),
            PartPose.offsetAndRotation(30.0F, 6.0F, -71.0F, 0.5236F, 0.6632F, 0));
        body.addOrReplaceChild("Rib4",
            CubeListBuilder.create().mirror()
                .texOffs(64, 38).addBox(-2.0F, -2.0F, -12.0F, 4, 4, 24),
            PartPose.offsetAndRotation(-29.0F, 18.0F, -68.0F, 0.5236F, -0.6632F, 0));
        body.addOrReplaceChild("Rib5",
            CubeListBuilder.create().mirror()
                .texOffs(65, 76).addBox(-2.0F, -2.0F, -10.0F, 4, 4, 20),
            PartPose.offsetAndRotation(-30.0F, 6.0F, -71.0F, 0.5236F, -0.6632F, 0));
        body.addOrReplaceChild("Rib6",
            CubeListBuilder.create().mirror()
                .texOffs(63, 112).addBox(-2.0F, -2.0F, -10.0F, 4, 4, 19),
            PartPose.offsetAndRotation(-28.0F, 23.0F, -54.0F, 0.5236F, -0.6632F, 0));

        // power cubes (siblings under body, independently rotated)
        body.addOrReplaceChild("PowerCube1",
            CubeListBuilder.create().mirror()
                .texOffs(0, 484).addBox(-15.0F, -15.0F, -15.0F, 30, 30, 30),
            PartPose.offset(0, 3.0F, -51.0F));
        body.addOrReplaceChild("PowerCube2",
            CubeListBuilder.create().mirror()
                .texOffs(0, 556).addBox(-20.0F, -20.0F, -20.0F, 40, 40, 40),
            PartPose.offset(0, 3.0F, -51.0F));
        body.addOrReplaceChild("PowerCube3",
            CubeListBuilder.create().mirror()
                .texOffs(0, 646).addBox(-25.0F, -25.0F, -25.0F, 50, 50, 50),
            PartPose.offset(0, 3.0F, -51.0F));

        // ── Tail chain ─────────────────────────────────────────────────────
        PartDefinition tail1 = body.addOrReplaceChild("Tail1",
            CubeListBuilder.create().mirror()
                .texOffs(500, 614).addBox(-32.0F, 0.0F, 0.0F, 64, 50, 58),
            PartPose.offset(0, -25.0F, 28.0F));
        tail1.addOrReplaceChild("Ridge4",
            CubeListBuilder.create().mirror()
                .texOffs(800, 550).addBox(-1.0F, 3.0F, 9.0F, 3, 10, 22),
            PartPose.rotation(0.3665F, 0, 0));
        tail1.addOrReplaceChild("Ridge5",
            CubeListBuilder.create().mirror()
                .texOffs(800, 500).addBox(-1.0F, 13.0F, 33.0F, 3, 10, 20),
            PartPose.rotation(0.3665F, 0, 0));

        PartDefinition tail2 = tail1.addOrReplaceChild("Tail2",
            CubeListBuilder.create().mirror()
                .texOffs(500, 520).addBox(-25.0F, -19.0F, -3.0F, 50, 42, 46),
            PartPose.offset(0, 24.0F, 50.0F));
        PartDefinition tail3 = tail2.addOrReplaceChild("Tail3",
            CubeListBuilder.create().mirror()
                .texOffs(500, 432).addBox(-20.0F, -18.0F, 0.0F, 40, 36, 45),
            PartPose.offset(0, 2.0F, 38.0F));
        PartDefinition tail4 = tail3.addOrReplaceChild("Tail4",
            CubeListBuilder.create().mirror()
                .texOffs(500, 355).addBox(-16.0F, -15.0F, 0.0F, 32, 30, 38),
            PartPose.offset(0, 1.0F, 39.0F));
        PartDefinition tail5 = tail4.addOrReplaceChild("Tail5",
            CubeListBuilder.create().mirror()
                .texOffs(500, 286).addBox(-13.0F, -12.0F, 0.0F, 26, 24, 38),
            PartPose.offset(0, 0, 33.0F));
        PartDefinition tail6 = tail5.addOrReplaceChild("Tail6",
            CubeListBuilder.create().mirror()
                .texOffs(500, 218).addBox(-10.0F, -9.0F, 0.0F, 20, 18, 44),
            PartPose.offset(0, 0, 33.0F));
        PartDefinition tail7 = tail6.addOrReplaceChild("Tail7",
            CubeListBuilder.create().mirror()
                .texOffs(500, 150).addBox(-8.0F, -6.0F, -7.0F, 16, 12, 49),
            PartPose.offset(0, 0, 46.0F));
        tail7.addOrReplaceChild("Ridge6",
            CubeListBuilder.create().mirror()
                .texOffs(638, 165).addBox(-1.0F, 2.0F, 20.0F, 3, 10, 20),
            PartPose.rotation(0.3665F, 0, 0));
        PartDefinition tailTip = tail7.addOrReplaceChild("TailTip",
            CubeListBuilder.create().mirror()
                .texOffs(500, 102).addBox(-54.0F, -3.0F, 0.0F, 106, 6, 36),
            PartPose.offset(0, 1.0F, 40.0F));
        tailTip.addOrReplaceChild("TailTip2",
            CubeListBuilder.create().mirror()
                .texOffs(500, 63).addBox(-35.0F, -2.0F, 36.0F, 70, 4, 26),
            PartPose.ZERO);
        tailTip.addOrReplaceChild("TailSpike",
            CubeListBuilder.create().mirror()
                .texOffs(800, 0).addBox(-2.0F, -1.0F, -7.0F, 4, 4, 82),
            PartPose.offset(0, -1.0F, 67.0F));

        // ── Centre neck chain ──────────────────────────────────────────────
        PartDefinition nc1 = body.addOrReplaceChild("NeckC1",
            CubeListBuilder.create().mirror()
                .texOffs(700, 1100).addBox(-12.0F, -12.0F, -24.0F, 24, 24, 24),
            PartPose.offsetAndRotation(0, -24.0F, -77.0F, -0.1047F, 0, 0));
        PartDefinition nc2 = nc1.addOrReplaceChild("NeckC2",
            CubeListBuilder.create().mirror()
                .texOffs(700, 1154).addBox(-11.0F, -11.0F, -40.0F, 22, 22, 40),
            PartPose.offsetAndRotation(0, 0, -19.0F, -0.0524F, 0, 0));
        PartDefinition nc3 = nc2.addOrReplaceChild("NeckC3",
            CubeListBuilder.create().mirror()
                .texOffs(700, 1222).addBox(-10.0F, -10.0F, -40.0F, 20, 20, 40),
            PartPose.offsetAndRotation(0, 1.0F, -36.0F, -0.0523F, 0, 0));
        PartDefinition nc4 = nc3.addOrReplaceChild("NeckC4",
            CubeListBuilder.create().mirror()
                .texOffs(700, 1300).addBox(-9.0F, -9.0F, -40.0F, 18, 18, 40),
            PartPose.offsetAndRotation(0, 1.0F, -32.0F, -0.0524F, 0, 0));
        PartDefinition cHead = nc4.addOrReplaceChild("CHead1",
            CubeListBuilder.create().mirror()
                .texOffs(700, 1425).addBox(-13.0F, -13.0F, -30.0F, 26, 26, 30),
            PartPose.offsetAndRotation(0, -6.0F, -36.0F, -0.0349F, 0, 0));
        cHead.addOrReplaceChild("CHead2",
            CubeListBuilder.create().mirror()
                .texOffs(700, 1550).addBox(-10.0F, -7.0F, -58.0F, 20, 20, 28),
            PartPose.ZERO);
        cHead.addOrReplaceChild("CHead3",
            CubeListBuilder.create().mirror()
                .texOffs(700, 1605).addBox(-8.0F, -3.0F, -83.0F, 16, 16, 26),
            PartPose.ZERO);
        cHead.addOrReplaceChild("CLEye",
            CubeListBuilder.create().mirror()
                .texOffs(700, 1500).addBox(13.0F, -12.0F, -29.0F, 2, 6, 11),
            PartPose.ZERO);
        cHead.addOrReplaceChild("CREye",
            CubeListBuilder.create().mirror()
                .texOffs(733, 1500).addBox(-15.0F, -12.0F, -29.0F, 2, 6, 11),
            PartPose.ZERO);
        cHead.addOrReplaceChild("CHeadMane",
            CubeListBuilder.create().mirror()
                .texOffs(700, 1375).addBox(0.0F, -14.0F, 0.0F, 1, 23, 19),
            PartPose.rotation(0.4014F, 0, 0));
        cHead.addOrReplaceChild("CLNoseSpike",
            CubeListBuilder.create().mirror()
                .texOffs(0, 350).addBox(24.0F, -23.0F, -76.0F, 1, 1, 17),
            PartPose.offsetAndRotation(0, 0, 0, 0.2443F, 0.2443F, 0));
        cHead.addOrReplaceChild("CRNoseSpike",
            CubeListBuilder.create().mirror()
                .texOffs(0, 375).addBox(-26.0F, -24.0F, -75.0F, 1, 1, 17),
            PartPose.offsetAndRotation(0, 0, 0, 0.2618F, -0.2618F, 0));

        PartDefinition cJaw = cHead.addOrReplaceChild("CJaw1",
            CubeListBuilder.create().mirror()
                .texOffs(700, 1660).addBox(-14.0F, -2.0F, -14.0F, 28, 7, 16),
            PartPose.offsetAndRotation(0, 12.0F, -3.0F, 0.192F, 0, 0));
        cJaw.addOrReplaceChild("CJaw2",
            CubeListBuilder.create().mirror()
                .texOffs(700, 1700).addBox(-12.0F, -2.0F, -55.0F, 24, 6, 46),
            PartPose.ZERO);
        cJaw.addOrReplaceChild("CJaw3",
            CubeListBuilder.create().mirror()
                .texOffs(700, 1765).addBox(-10.0F, -2.0F, -77.0F, 20, 6, 22),
            PartPose.ZERO);
        cJaw.addOrReplaceChild("CTooth1",
            CubeListBuilder.create().mirror()
                .texOffs(50, 0).addBox(-10.0F, -10.0F, -77.0F, 2, 8, 2),
            PartPose.ZERO);
        cJaw.addOrReplaceChild("CTooth2",
            CubeListBuilder.create().mirror()
                .texOffs(60, 0).addBox(8.0F, -10.0F, -77.0F, 2, 8, 2),
            PartPose.ZERO);
        cJaw.addOrReplaceChild("CTooth3",
            CubeListBuilder.create().mirror()
                .texOffs(70, 0).addBox(-12.0F, -8.0F, -55.0F, 2, 6, 2),
            PartPose.ZERO);
        cJaw.addOrReplaceChild("CTooth4",
            CubeListBuilder.create().mirror()
                .texOffs(80, 0).addBox(10.0F, -8.0F, -55.0F, 2, 6, 2),
            PartPose.ZERO);

        // ── Left neck chain ────────────────────────────────────────────────
        PartDefinition nl1 = body.addOrReplaceChild("NeckL1",
            CubeListBuilder.create().mirror()
                .texOffs(500, 1100).addBox(-12.0F, -12.0F, -24.0F, 24, 24, 24),
            PartPose.offsetAndRotation(30.0F, -24.0F, -77.0F, -0.0524F, -0.1047F, 0));
        PartDefinition nl2 = nl1.addOrReplaceChild("NeckL2",
            CubeListBuilder.create().mirror()
                .texOffs(500, 1154).addBox(-11.0F, -11.0F, -40.0F, 22, 22, 40),
            PartPose.offsetAndRotation(2.0F, -1.0F, -19.0F, 0.0524F, -0.0698F, 0));
        PartDefinition nl3 = nl2.addOrReplaceChild("NeckL3",
            CubeListBuilder.create().mirror()
                .texOffs(500, 1222).addBox(-10.0F, -10.0F, -40.0F, 20, 20, 40),
            PartPose.offsetAndRotation(6.0F, 0, -36.0F, 0.0698F, -0.0873F, 0));
        PartDefinition nl4 = nl3.addOrReplaceChild("NeckL4",
            CubeListBuilder.create().mirror()
                .texOffs(500, 1300).addBox(-9.0F, -9.0F, -40.0F, 18, 18, 40),
            PartPose.offsetAndRotation(8.0F, 2.0F, -32.0F, 0.0698F, -0.1222F, 0));
        PartDefinition lHead = nl4.addOrReplaceChild("LHead1",
            CubeListBuilder.create().mirror()
                .texOffs(500, 1425).addBox(-13.0F, -13.0F, -30.0F, 26, 26, 30),
            PartPose.offset(13.0F, -2.0F, -32.0F));
        lHead.addOrReplaceChild("LHead2",
            CubeListBuilder.create().mirror()
                .texOffs(500, 1550).addBox(-10.0F, -7.0F, -58.0F, 20, 20, 28),
            PartPose.ZERO);
        lHead.addOrReplaceChild("LHead3",
            CubeListBuilder.create().mirror()
                .texOffs(500, 1605).addBox(-8.0F, -3.0F, -83.0F, 16, 16, 26),
            PartPose.ZERO);
        lHead.addOrReplaceChild("LLEye",
            CubeListBuilder.create().mirror()
                .texOffs(500, 1500).addBox(13.0F, -12.0F, -29.0F, 2, 6, 11),
            PartPose.ZERO);
        lHead.addOrReplaceChild("LREye",
            CubeListBuilder.create().mirror()
                .texOffs(533, 1500).addBox(-15.0F, -12.0F, -29.0F, 2, 6, 11),
            PartPose.ZERO);
        lHead.addOrReplaceChild("LHeadMane",
            CubeListBuilder.create().mirror()
                .texOffs(500, 1375).addBox(0.0F, -14.0F, 0.0F, 1, 23, 19),
            PartPose.rotation(0.384F, 0, 0));
        lHead.addOrReplaceChild("LLNoseSpike",
            CubeListBuilder.create().mirror()
                .texOffs(0, 300).addBox(24.0F, -23.0F, -76.0F, 1, 1, 17),
            PartPose.offsetAndRotation(0, 0, 0, 0.261F, 0.227F, 0));
        lHead.addOrReplaceChild("LRNoseSpike",
            CubeListBuilder.create().mirror()
                .texOffs(0, 325).addBox(-26.0F, -24.0F, -75.0F, 1, 1, 17),
            PartPose.offsetAndRotation(0, 0, 0, 0.279F, -0.244F, 0));

        PartDefinition lJaw = lHead.addOrReplaceChild("LJaw1",
            CubeListBuilder.create().mirror()
                .texOffs(500, 1660).addBox(-14.0F, -2.0F, -14.0F, 28, 7, 16),
            PartPose.offsetAndRotation(0, 14.0F, -1.0F, 0.104F, 0, 0));
        lJaw.addOrReplaceChild("LJaw2",
            CubeListBuilder.create().mirror()
                .texOffs(500, 1700).addBox(-12.0F, -2.0F, -55.0F, 24, 6, 46),
            PartPose.ZERO);
        lJaw.addOrReplaceChild("LJaw3",
            CubeListBuilder.create().mirror()
                .texOffs(500, 1765).addBox(-10.0F, -2.0F, -77.0F, 20, 6, 22),
            PartPose.ZERO);
        lJaw.addOrReplaceChild("LTooth1",
            CubeListBuilder.create().mirror()
                .texOffs(10, 0).addBox(-10.0F, -10.0F, -77.0F, 2, 8, 2),
            PartPose.ZERO);
        lJaw.addOrReplaceChild("LTooth2",
            CubeListBuilder.create().mirror()
                .texOffs(20, 0).addBox(8.0F, -10.0F, -77.0F, 2, 8, 2),
            PartPose.ZERO);
        lJaw.addOrReplaceChild("LTooth3",
            CubeListBuilder.create().mirror()
                .texOffs(30, 0).addBox(-12.0F, -8.0F, -55.0F, 2, 6, 2),
            PartPose.ZERO);
        lJaw.addOrReplaceChild("LTooth4",
            CubeListBuilder.create().mirror()
                .texOffs(40, 0).addBox(10.0F, -8.0F, -55.0F, 2, 6, 2),
            PartPose.ZERO);

        // ── Right neck chain ───────────────────────────────────────────────
        PartDefinition nr1 = body.addOrReplaceChild("NeckR1",
            CubeListBuilder.create().mirror()
                .texOffs(900, 1100).addBox(-12.0F, -12.0F, -24.0F, 24, 24, 24),
            PartPose.offsetAndRotation(-30.0F, -24.0F, -77.0F, -0.0524F, 0.1047F, 0));
        PartDefinition nr2 = nr1.addOrReplaceChild("NeckR2",
            CubeListBuilder.create().mirror()
                .texOffs(900, 1154).addBox(-11.0F, -11.0F, -40.0F, 22, 22, 40),
            PartPose.offsetAndRotation(-2.0F, -1.0F, -19.0F, 0, 0.0698F, 0));
        PartDefinition nr3 = nr2.addOrReplaceChild("NeckR3",
            CubeListBuilder.create().mirror()
                .texOffs(900, 1222).addBox(-10.0F, -10.0F, -40.0F, 20, 20, 40),
            PartPose.offsetAndRotation(-5.0F, -2.0F, -33.0F, 0, 0.1397F, 0));
        PartDefinition nr4 = nr3.addOrReplaceChild("NeckR4",
            CubeListBuilder.create().mirror()
                .texOffs(900, 1300).addBox(-9.0F, -9.0F, -40.0F, 18, 18, 40),
            PartPose.offsetAndRotation(-10.0F, -2.0F, -33.0F, -0.0523F, 0.0523F, 0));
        PartDefinition rHead = nr4.addOrReplaceChild("RHead1",
            CubeListBuilder.create().mirror()
                .texOffs(900, 1425).addBox(-13.0F, -13.0F, -30.0F, 26, 26, 30),
            PartPose.offsetAndRotation(-13.0F, -10.0F, -34.0F, -0.0175F, 0.0524F, 0));
        rHead.addOrReplaceChild("RHead2",
            CubeListBuilder.create().mirror()
                .texOffs(900, 1550).addBox(-10.0F, -7.0F, -58.0F, 20, 20, 28),
            PartPose.ZERO);
        rHead.addOrReplaceChild("RHead3",
            CubeListBuilder.create().mirror()
                .texOffs(900, 1605).addBox(-8.0F, -3.0F, -83.0F, 16, 16, 26),
            PartPose.ZERO);
        rHead.addOrReplaceChild("RLEye",
            CubeListBuilder.create().mirror()
                .texOffs(900, 1500).addBox(13.0F, -12.0F, -29.0F, 2, 6, 11),
            PartPose.ZERO);
        rHead.addOrReplaceChild("RREye",
            CubeListBuilder.create().mirror()
                .texOffs(933, 1500).addBox(-15.0F, -12.0F, -29.0F, 2, 5, 11),
            PartPose.ZERO);
        rHead.addOrReplaceChild("RHeadMane",
            CubeListBuilder.create().mirror()
                .texOffs(900, 1375).addBox(0.0F, -14.0F, 0.0F, 1, 23, 19),
            PartPose.rotation(0.489F, 0, 0));
        rHead.addOrReplaceChild("RLNoseSpike",
            CubeListBuilder.create().mirror()
                .texOffs(0, 400).addBox(24.0F, -23.0F, -76.0F, 1, 1, 17),
            PartPose.offsetAndRotation(0, 0, 0, 0.262F, 0.227F, 0));
        rHead.addOrReplaceChild("RRNoseSpike",
            CubeListBuilder.create().mirror()
                .texOffs(0, 425).addBox(-26.0F, -24.0F, -75.0F, 1, 1, 17),
            PartPose.offsetAndRotation(0, 0, 0, 0.279F, -0.244F, 0));

        PartDefinition rJaw = rHead.addOrReplaceChild("RJaw1",
            CubeListBuilder.create().mirror()
                .texOffs(900, 1660).addBox(-14.0F, -2.0F, -14.0F, 28, 7, 16),
            PartPose.offsetAndRotation(-2.0F, 13.0F, -5.0F, 0.279F, 0, 0));
        rJaw.addOrReplaceChild("RJaw2",
            CubeListBuilder.create().mirror()
                .texOffs(900, 1700).addBox(-12.0F, -2.0F, -55.0F, 24, 6, 46),
            PartPose.ZERO);
        rJaw.addOrReplaceChild("RJaw3",
            CubeListBuilder.create().mirror()
                .texOffs(900, 1765).addBox(-10.0F, -2.0F, -77.0F, 20, 6, 22),
            PartPose.ZERO);
        rJaw.addOrReplaceChild("RTooth1",
            CubeListBuilder.create().mirror()
                .texOffs(90, 0).addBox(-10.0F, -10.0F, -77.0F, 2, 8, 2),
            PartPose.ZERO);
        rJaw.addOrReplaceChild("RTooth2",
            CubeListBuilder.create().mirror()
                .texOffs(100, 0).addBox(8.0F, -10.0F, -77.0F, 2, 8, 2),
            PartPose.ZERO);
        rJaw.addOrReplaceChild("RTooth3",
            CubeListBuilder.create().mirror()
                .texOffs(110, 0).addBox(-12.0F, -8.0F, -55.0F, 2, 6, 2),
            PartPose.ZERO);
        rJaw.addOrReplaceChild("RTooth4",
            CubeListBuilder.create().mirror()
                .texOffs(120, 0).addBox(10.0F, -8.0F, -55.0F, 2, 6, 2),
            PartPose.ZERO);

        // ── Left leg chain ─────────────────────────────────────────────────
        // Thigh pivots at hip; xRot 0.785 = 45° forward lean at rest
        PartDefinition lThigh = body.addOrReplaceChild("LThigh",
            CubeListBuilder.create().mirror()
                .texOffs(0, 1050).addBox(0.0F, -14.0F, -21.0F, 26, 34, 42),
            PartPose.offsetAndRotation(40.0F, -2.0F, 1.0F, 0.7854F, 0, 0));
        lThigh.addOrReplaceChild("LUpperLeg",
            CubeListBuilder.create().mirror()
                .texOffs(0, 1137).addBox(0.0F, 12.0F, -16.0F, 24, 52, 24),
            PartPose.ZERO);
        // Offset (12, 52, -4) accounts for thigh 45° rotation
        PartDefinition lLower = lThigh.addOrReplaceChild("LLowerLeg",
            CubeListBuilder.create().mirror()
                .texOffs(0, 1220).addBox(-10.0F, 0.0F, -10.0F, 20, 59, 20),
            PartPose.offsetAndRotation(12.0F, 52.0F, -4.0F, -1.413F, 0, 0));
        PartDefinition lFoot = lLower.addOrReplaceChild("LFoot",
            CubeListBuilder.create().mirror()
                .texOffs(0, 1307).addBox(-18.0F, 59.0F, -10.0F, 34, 12, 20),
            PartPose.offset(1.0F, 0, 0));
        lFoot.addOrReplaceChild("LClawRear",
            CubeListBuilder.create().mirror()
                .texOffs(0, 1350).addBox(-2.0F, -3.0F, 0.0F, 4, 9, 30),
            PartPose.offsetAndRotation(-1.0F, 64.0F, 7.0F, -0.297F, 0, 0));
        PartDefinition llc1 = lFoot.addOrReplaceChild("LLClaw1",
            CubeListBuilder.create().mirror()
                .texOffs(0, 1400).addBox(-5.0F, -5.0F, -18.0F, 10, 10, 18),
            PartPose.offsetAndRotation(13.0F, 66.0F, -5.0F, 1.012F, 0, 0));
        llc1.addOrReplaceChild("LLClaw2",
            CubeListBuilder.create().mirror()
                .texOffs(0, 1550).addBox(-2.0F, -7.0F, -46.0F, 4, 7, 30),
            PartPose.rotation(0.262F, 0, 0));
        PartDefinition lcc1 = lFoot.addOrReplaceChild("LCClaw1",
            CubeListBuilder.create().mirror()
                .texOffs(0, 1450).addBox(-5.0F, -5.0F, -18.0F, 10, 10, 18),
            PartPose.offsetAndRotation(-1.0F, 66.0F, -5.0F, 1.012F, 0, 0));
        lcc1.addOrReplaceChild("LCClaw2",
            CubeListBuilder.create().mirror()
                .texOffs(0, 1600).addBox(-2.0F, -7.0F, -46.0F, 4, 7, 30),
            PartPose.rotation(0.262F, 0, 0));
        PartDefinition lrc1 = lFoot.addOrReplaceChild("LRClaw1",
            CubeListBuilder.create().mirror()
                .texOffs(0, 1500).addBox(-5.0F, -5.0F, -18.0F, 10, 10, 18),
            PartPose.offsetAndRotation(-15.0F, 66.0F, -5.0F, 1.012F, 0, 0));
        lrc1.addOrReplaceChild("LRClaw2",
            CubeListBuilder.create().mirror()
                .texOffs(0, 1650).addBox(-2.0F, -7.0F, -46.0F, 4, 7, 30),
            PartPose.rotation(0.262F, 0, 0));

        // ── Right leg chain ────────────────────────────────────────────────
        PartDefinition rThigh = body.addOrReplaceChild("RThigh",
            CubeListBuilder.create().mirror()
                .texOffs(200, 1050).addBox(0.0F, -14.0F, -21.0F, 26, 34, 42),
            PartPose.offsetAndRotation(-66.0F, -2.0F, 1.0F, 0.7854F, 0, 0));
        rThigh.addOrReplaceChild("RUpperLeg",
            CubeListBuilder.create().mirror()
                .texOffs(200, 1137).addBox(0.0F, 12.0F, -16.0F, 24, 52, 24),
            PartPose.offset(2.0F, 0, 0));
        PartDefinition rLower = rThigh.addOrReplaceChild("RLowerLeg",
            CubeListBuilder.create().mirror()
                .texOffs(200, 1220).addBox(-3.0F, 0.0F, -10.0F, 20, 59, 20),
            PartPose.offsetAndRotation(7.0F, 52.0F, -4.0F, -1.413F, 0, 0));
        PartDefinition rFoot = rLower.addOrReplaceChild("RFoot",
            CubeListBuilder.create().mirror()
                .texOffs(200, 1307).addBox(-10.0F, 59.0F, -10.0F, 34, 12, 20),
            PartPose.ZERO);
        rFoot.addOrReplaceChild("RClawRear",
            CubeListBuilder.create().mirror()
                .texOffs(200, 1350).addBox(-2.0F, -3.0F, 0.0F, 4, 9, 30),
            PartPose.offsetAndRotation(7.0F, 64.0F, 7.0F, -0.297F, 0, 0));
        PartDefinition rlc1 = rFoot.addOrReplaceChild("RLClaw1",
            CubeListBuilder.create().mirror()
                .texOffs(200, 1400).addBox(-5.0F, -5.0F, -18.0F, 10, 10, 18),
            PartPose.offsetAndRotation(21.0F, 66.0F, -5.0F, 1.012F, 0, 0));
        rlc1.addOrReplaceChild("RLClaw2",
            CubeListBuilder.create().mirror()
                .texOffs(200, 1550).addBox(-2.0F, -7.0F, -46.0F, 4, 7, 30),
            PartPose.rotation(0.262F, 0, 0));
        PartDefinition rcc1 = rFoot.addOrReplaceChild("RCClaw1",
            CubeListBuilder.create().mirror()
                .texOffs(200, 1450).addBox(-5.0F, -5.0F, -18.0F, 10, 10, 18),
            PartPose.offsetAndRotation(7.0F, 66.0F, -5.0F, 1.012F, 0, 0));
        rcc1.addOrReplaceChild("RCClaw2",
            CubeListBuilder.create().mirror()
                .texOffs(200, 1600).addBox(-2.0F, -7.0F, -46.0F, 4, 7, 30),
            PartPose.rotation(0.262F, 0, 0));
        PartDefinition rrc1 = rFoot.addOrReplaceChild("RRClaw1",
            CubeListBuilder.create().mirror()
                .texOffs(200, 1500).addBox(-5.0F, -5.0F, -18.0F, 10, 10, 18),
            PartPose.offsetAndRotation(-7.0F, 66.0F, -5.0F, 1.012F, 0, 0));
        rrc1.addOrReplaceChild("RRClaw2",
            CubeListBuilder.create().mirror()
                .texOffs(200, 1650).addBox(-2.0F, -7.0F, -46.0F, 4, 7, 30),
            PartPose.rotation(0.262F, 0, 0));

        // ── Left wing chain ────────────────────────────────────────────────
        PartDefinition lw1 = body.addOrReplaceChild("Lwing1",
            CubeListBuilder.create().mirror()
                .texOffs(250, 1000).addBox(0.0F, 0.0F, 0.0F, 87, 5, 5),
            PartPose.offsetAndRotation(40.0F, -32.0F, -51.0F, 0, 0.0349F, 0));
        lw1.addOrReplaceChild("Lwing2",
            CubeListBuilder.create().mirror()
                .texOffs(1220, 782).addBox(0.0F, 2.0F, 0.0F, 87, 1, 110),
            PartPose.offset(0, 0, 1.0F));
        PartDefinition lw3 = lw1.addOrReplaceChild("Lwing3",
            CubeListBuilder.create().mirror()
                .texOffs(250, 975).addBox(0.0F, 0.0F, 0.0F, 188, 5, 5),
            PartPose.offsetAndRotation(84.0F, 0, -3.0F, 0, -0.0349F, 0));
        lw3.addOrReplaceChild("Lwing4",
            CubeListBuilder.create().mirror()
                .texOffs(1341, 625).addBox(0.0F, 2.0F, 0.0F, 188, 1, 147),
            PartPose.offset(0, 0, 3.0F));
        PartDefinition lw7 = lw3.addOrReplaceChild("Lwing7",
            CubeListBuilder.create().mirror()
                .texOffs(250, 900).addBox(0.0F, 0.0F, 0.0F, 140, 5, 5),
            PartPose.rotation(0, 0, 0.2618F));
        lw7.addOrReplaceChild("Lwing8",
            CubeListBuilder.create().mirror()
                .texOffs(1300, 1156).addBox(0.0F, 2.0F, 0.0F, 140, 1, 120),
            PartPose.offset(0, 0, 3.0F));
        PartDefinition lw9 = lw3.addOrReplaceChild("Lwing9",
            CubeListBuilder.create().mirror()
                .texOffs(250, 925).addBox(0.0F, 0.0F, 0.0F, 170, 5, 5),
            PartPose.rotation(0, 0, -0.2618F));
        lw9.addOrReplaceChild("Lwing10",
            CubeListBuilder.create().mirror()
                .texOffs(1392, 326).addBox(0.0F, 2.0F, 0.0F, 170, 1, 136),
            PartPose.offset(0, 0, 3.0F));
        PartDefinition lw5 = lw3.addOrReplaceChild("Lwing5",
            CubeListBuilder.create().mirror()
                .texOffs(245, 950).addBox(0.0F, 0.0F, 0.0F, 87, 5, 5),
            PartPose.offsetAndRotation(185.0F, 0, 0, 0, -0.0349F, 0));
        lw5.addOrReplaceChild("Lwing6",
            CubeListBuilder.create().mirror()
                .texOffs(1300, 1300).addBox(0.0F, 2.0F, 1.0F, 87, 1, 125),
            PartPose.offset(0, 0, 3.0F));

        // ── Right wing chain (mirrored) ────────────────────────────────────
        PartDefinition rw1 = body.addOrReplaceChild("Rwing1",
            CubeListBuilder.create().mirror()
                .texOffs(650, 1000).addBox(-87.0F, 0.0F, 0.0F, 87, 5, 5),
            PartPose.offsetAndRotation(-40.0F, -32.0F, -51.0F, 0, -0.0349F, 0));
        rw1.addOrReplaceChild("Rwing2",
            CubeListBuilder.create().mirror()
                .texOffs(1619, 782).addBox(-87.0F, 2.0F, 0.0F, 87, 1, 110),
            PartPose.offset(0, 0, 1.0F));
        PartDefinition rw3 = rw1.addOrReplaceChild("Rwing3",
            CubeListBuilder.create().mirror()
                .texOffs(550, 950).addBox(-188.0F, 0.0F, 0.0F, 188, 5, 5),
            PartPose.offsetAndRotation(-84.0F, 0, -3.0F, 0, 0.0349F, 0));
        rw3.addOrReplaceChild("Rwing4",
            CubeListBuilder.create().mirror()
                .texOffs(1341, 470).addBox(-188.0F, 2.0F, 0.0F, 188, 1, 147),
            PartPose.offset(0, 0, 0));
        PartDefinition rw7 = rw3.addOrReplaceChild("Rwing7",
            CubeListBuilder.create().mirror()
                .texOffs(650, 900).addBox(-140.0F, 0.0F, 0.0F, 140, 5, 5),
            PartPose.rotation(0, 0, -0.2618F));
        rw7.addOrReplaceChild("Rwing8",
            CubeListBuilder.create().mirror()
                .texOffs(1300, 1024).addBox(-140.0F, 2.0F, 0.0F, 140, 1, 120),
            PartPose.offset(0, 0, 0));
        PartDefinition rw9 = rw3.addOrReplaceChild("Rwing9",
            CubeListBuilder.create().mirror()
                .texOffs(621, 925).addBox(-170.0F, 0.0F, 0.0F, 170, 5, 5),
            PartPose.rotation(0, 0, 0.2618F));
        rw9.addOrReplaceChild("Rwing10",
            CubeListBuilder.create().mirror()
                .texOffs(1391, 184).addBox(-170.0F, 2.0F, 0.0F, 170, 1, 136),
            PartPose.offset(0, 0, 0));
        PartDefinition rw5 = rw3.addOrReplaceChild("Rwing5",
            CubeListBuilder.create().mirror()
                .texOffs(750, 975).addBox(-87.0F, 0.0F, 0.0F, 87, 5, 5),
            PartPose.offsetAndRotation(-185.0F, 0, 0, 0, 0.0349F, 0));
        rw5.addOrReplaceChild("Rwing6",
            CubeListBuilder.create().mirror()
                .texOffs(1300, 1436).addBox(-87.0F, 2.0F, 1.0F, 87, 1, 125),
            PartPose.offset(0, 0, 0));

        return LayerDefinition.create(mesh, 2048, 2048);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  setupAnim – rotation-only (+ body.y for breathing)
    // ════════════════════════════════════════════════════════════════════════
    @Override
    public void setupAnim(TheQueen entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        boolean attacking = entity.getAttacking() != 0;
        float PI = (float) Math.PI;

        // ── idle breathing ─────────────────────────────────────────────────
        float breathe = Mth.sin(ageInTicks * 0.05F) * 0.5F;
        this.body.xRot += breathe * 0.01F;
        this.body.y += breathe;

        // ── wings ──────────────────────────────────────────────────────────
        // Shoulder angle; hierarchy cascades to elbow and tip automatically.
        // Child zRot is the INCREMENTAL rotation on top of the parent.
        float wingAngle = attacking
            ? Mth.cos(ageInTicks * 0.85F) * PI * 0.26F
            : Mth.cos(ageInTicks * 0.35F) * PI * 0.15F;

        this.Lwing1.zRot += wingAngle;
        this.Lwing3.zRot += wingAngle * 2.0F / 3.0F;   // total visual = 5/3
        this.Lwing5.zRot += wingAngle * 2.0F / 3.0F;   // total visual = 7/3
        this.Rwing1.zRot -= wingAngle;
        this.Rwing3.zRot -= wingAngle * 2.0F / 3.0F;
        this.Rwing5.zRot -= wingAngle * 2.0F / 3.0F;

        // figure-8 pitch sweep on shoulder
        float wingPitch = Mth.sin(wingAngle) * 0.15F;
        this.Lwing1.xRot += wingPitch;
        this.Rwing1.xRot += wingPitch;

        // ── tail ripple ────────────────────────────────────────────────────
        float tailSpeed = attacking ? 0.6F : 0.26F;
        float tailAmp   = attacking ? 0.2F : 0.08F;
        float pi4 = PI / 4.0F;
        this.Tail1.yRot  += Mth.cos(ageInTicks * tailSpeed)           * PI * tailAmp * 0.5F;
        this.Tail2.yRot  += Mth.cos(ageInTicks * tailSpeed - pi4)     * PI * tailAmp * 0.5F;
        this.Tail3.yRot  += Mth.cos(ageInTicks * tailSpeed - 2*pi4)   * PI * tailAmp * 0.5F;
        this.Tail4.yRot  += Mth.cos(ageInTicks * tailSpeed - 3*pi4)   * PI * tailAmp * 0.5F;
        this.Tail5.yRot  += Mth.cos(ageInTicks * tailSpeed - 4*pi4)   * PI * tailAmp * 0.4F;
        this.Tail6.yRot  += Mth.cos(ageInTicks * tailSpeed - 5*pi4)   * PI * tailAmp * 0.4F;
        this.Tail7.yRot  += Mth.cos(ageInTicks * tailSpeed - 6*pi4)   * PI * tailAmp * 0.4F;
        this.TailTip.yRot += Mth.cos(ageInTicks * tailSpeed - 7*pi4)  * PI * tailAmp * 0.3F;

        // ── neck & head animation ──────────────────────────────────────────
        float Lheadlr, Lheadud, Ljawangle;
        float Cheadlr, Cheadud, Cjawangle;
        float Rheadlr, Rheadud, Rjawangle;

        if (attacking) {
            Lheadlr   = Mth.sin(ageInTicks * 0.3F) * PI * 0.25F;
            Lheadud   = Mth.sin(ageInTicks * 0.2F) * PI * 0.25F;
            Ljawangle = Mth.sin(ageInTicks * 0.85F) * PI * 0.12F + 0.5F + Lheadud;
            Cheadlr   = Mth.sin(ageInTicks * 0.27F) * PI * 0.25F;
            Cheadud   = Mth.sin(ageInTicks * 0.18F) * PI * 0.25F;
            Cjawangle = Mth.sin(ageInTicks * 0.75F) * PI * 0.12F + 0.5F + Cheadud;
            Rheadlr   = Mth.sin(ageInTicks * 0.33F) * PI * 0.25F;
            Rheadud   = Mth.sin(ageInTicks * 0.22F) * PI * 0.25F;
            Rjawangle = Mth.sin(ageInTicks * 0.95F) * PI * 0.12F + 0.5F + Rheadud;
        } else {
            Lheadlr   = Mth.sin(ageInTicks * 0.17F) * PI * 0.08F;
            Lheadud   = Mth.sin(ageInTicks * 0.13F) * PI * 0.1F;
            Ljawangle = Mth.sin(ageInTicks * 0.45F) * PI * 0.04F + 0.25F + Lheadud;
            Cheadlr   = Mth.sin(ageInTicks * 0.13F) * PI * 0.08F;
            Cheadud   = Mth.sin(ageInTicks * 0.08F) * PI * 0.1F;
            Cjawangle = Mth.sin(ageInTicks * 0.65F) * PI * 0.04F + 0.25F + Cheadud;
            Rheadlr   = Mth.sin(ageInTicks * 0.19F) * PI * 0.08F;
            Rheadud   = Mth.sin(ageInTicks * 0.12F) * PI * 0.1F;
            Rjawangle = Mth.sin(ageInTicks * 0.55F) * PI * 0.04F + 0.25F + Rheadud;
        }
        // prevent left head crossing centre, right crossing centre
        if (Lheadlr > Cheadlr) Lheadlr = Cheadlr;
        if (Rheadlr < Cheadlr) Rheadlr = Cheadlr;

        // Centre neck – incremental fractions (sum = 1.0 for full turn)
        animateNeck(NeckC1, NeckC2, NeckC3, NeckC4, CHead1, Cheadlr, Cheadud);
        // Left neck
        animateNeck(NeckL1, NeckL2, NeckL3, NeckL4, LHead1, Lheadlr, Lheadud);
        // Right neck
        animateNeck(NeckR1, NeckR2, NeckR3, NeckR4, RHead1, Rheadlr, Rheadud);

        // Jaws – clamped so they cannot rotate up through the skull
        float REST_CJAW = 0.192F, REST_LJAW = 0.104F, REST_RJAW = 0.279F;
        this.CJaw1.xRot = Mth.clamp(Cjawangle, REST_CJAW, 1.5F);
        this.LJaw1.xRot = Mth.clamp(Ljawangle, REST_LJAW, 1.5F);
        this.RJaw1.xRot = Mth.clamp(Rjawangle, REST_RJAW, 1.5F);

        // ── legs ───────────────────────────────────────────────────────────
        // Attacking: kick animation; idle: blend with limbSwing for walking
        float legAngle = attacking
            ? Mth.cos(ageInTicks * 0.7F) * PI * 0.45F : 0;
        float walkAngle = Mth.cos(limbSwing * 0.6662F) * 0.7F * limbSwingAmount;

        this.LThigh.xRot    += (legAngle / 4.0F) + walkAngle * 0.15F;
        this.LLowerLeg.xRot += (legAngle / 4.0F) + walkAngle * 0.1F;
        this.RThigh.xRot    -= (legAngle / 4.0F) - walkAngle * 0.15F;
        this.RLowerLeg.xRot -= (legAngle / 4.0F) - walkAngle * 0.1F;

        // Claw clenching during attack
        float clawAngle = attacking
            ? Mth.cos(ageInTicks * 0.85F) * PI * 0.26F : 0;
        this.LClawRear.xRot += clawAngle;
        this.RClawRear.xRot += clawAngle;
        this.LLClaw1.xRot -= clawAngle;
        this.LCClaw1.xRot -= clawAngle;
        this.LRClaw1.xRot -= clawAngle;
        this.RLClaw1.xRot -= clawAngle;
        this.RCClaw1.xRot -= clawAngle;
        this.RRClaw1.xRot -= clawAngle;

        // ── power cubes (continuous rotation + pulsing scale) ──────────────
        this.PowerCube1.xRot = ageInTicks * 0.03F;
        this.PowerCube1.yRot = ageInTicks * 0.035F;
        this.PowerCube1.zRot = ageInTicks * 0.05F;
        this.PowerCube2.xRot = ageInTicks * 0.04F;
        this.PowerCube2.yRot = ageInTicks * 0.046F;
        this.PowerCube2.zRot = ageInTicks * 0.13F;
        this.PowerCube3.xRot = ageInTicks * 0.05F;
        this.PowerCube3.yRot = ageInTicks * 0.065F;
        this.PowerCube3.zRot = ageInTicks * 0.03F;

        float pulse1 = 1.0F + Mth.sin(ageInTicks * 0.1F)  * 0.15F;
        float pulse2 = 1.0F + Mth.sin(ageInTicks * 0.13F) * 0.15F;
        float pulse3 = 1.0F + Mth.sin(ageInTicks * 0.07F) * 0.15F;
        this.PowerCube1.xScale = this.PowerCube1.yScale = this.PowerCube1.zScale = pulse1;
        this.PowerCube2.xScale = this.PowerCube2.yScale = this.PowerCube2.zScale = pulse2;
        this.PowerCube3.xScale = this.PowerCube3.yScale = this.PowerCube3.zScale = pulse3;
    }

    /** Distributes yRot (left-right) and xRot (up-down) across 4 neck
     *  segments + head using incremental fractions that sum to 1.0 */
    private static void animateNeck(ModelPart n1, ModelPart n2, ModelPart n3,
                                    ModelPart n4, ModelPart head,
                                    float lr, float ud) {
        n1.yRot   += lr * 0.125F;
        n2.yRot   += lr * 0.125F;
        n3.yRot   += lr * 0.13F;
        n4.yRot   += lr * 0.12F;
        head.yRot += lr * 0.5F;

        n1.xRot   += ud * 0.125F;
        n2.xRot   += ud * 0.125F;
        n3.xRot   += ud * 0.13F;
        n4.xRot   += ud * 0.12F;
        head.xRot += ud * 0.5F;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  Rendering – multi-pass with visibility toggling & manual chain walk
    // ════════════════════════════════════════════════════════════════════════

    @Override
    public void renderToBuffer(PoseStack ps, VertexConsumer vc,
                               int light, int overlay, int color) {
        setGroupVisible(wingMembranes, false);
        setGroupVisible(powerCubes, false);
        setGroupVisible(eyes, false);
        this.root.render(ps, vc, light, overlay, color);
        setGroupVisible(wingMembranes, true);
        setGroupVisible(powerCubes, true);
        setGroupVisible(eyes, true);
    }

    /** Render translucent wing membranes at their correct hierarchical
     *  positions by manually walking each parent chain. */
    public void renderWingMembranes(PoseStack ps, VertexConsumer vc,
                                    int light, int overlay, int color) {
        // Left wing membranes
        chainRender(ps, vc, light, overlay, color, body, Lwing1, Lwing2);
        chainRender(ps, vc, light, overlay, color, body, Lwing1, Lwing3, Lwing4);
        chainRender(ps, vc, light, overlay, color, body, Lwing1, Lwing3, Lwing5, Lwing6);
        chainRender(ps, vc, light, overlay, color, body, Lwing1, Lwing3, Lwing7, Lwing8);
        chainRender(ps, vc, light, overlay, color, body, Lwing1, Lwing3, Lwing9, Lwing10);
        // Right wing membranes
        chainRender(ps, vc, light, overlay, color, body, Rwing1, Rwing2);
        chainRender(ps, vc, light, overlay, color, body, Rwing1, Rwing3, Rwing4);
        chainRender(ps, vc, light, overlay, color, body, Rwing1, Rwing3, Rwing5, Rwing6);
        chainRender(ps, vc, light, overlay, color, body, Rwing1, Rwing3, Rwing7, Rwing8);
        chainRender(ps, vc, light, overlay, color, body, Rwing1, Rwing3, Rwing9, Rwing10);
    }

    /** Render fullbright power cubes. Opaque purple tint (0xFF8800FF ARGB). */
    public void renderPowerCubes(PoseStack ps, VertexConsumer vc,
                                 int light, int overlay, int color, int power) {
        ps.pushPose();
        body.translateAndRotate(ps);
        PowerCube1.render(ps, vc, light, overlay, color);
        if (power > 350) PowerCube2.render(ps, vc, light, overlay, color);
        if (power > 650) PowerCube3.render(ps, vc, light, overlay, color);
        ps.popPose();
    }

    /** Render fullbright eyes by walking each head chain. */
    public void renderEyes(PoseStack ps, VertexConsumer vc,
                           int light, int overlay, int color) {
        // Centre eyes
        chainRender(ps, vc, light, overlay, color,
            body, NeckC1, NeckC2, NeckC3, NeckC4, CHead1, CLEye);
        chainRender(ps, vc, light, overlay, color,
            body, NeckC1, NeckC2, NeckC3, NeckC4, CHead1, CREye);
        // Left eyes
        chainRender(ps, vc, light, overlay, color,
            body, NeckL1, NeckL2, NeckL3, NeckL4, LHead1, LLEye);
        chainRender(ps, vc, light, overlay, color,
            body, NeckL1, NeckL2, NeckL3, NeckL4, LHead1, LREye);
        // Right eyes
        chainRender(ps, vc, light, overlay, color,
            body, NeckR1, NeckR2, NeckR3, NeckR4, RHead1, RLEye);
        chainRender(ps, vc, light, overlay, color,
            body, NeckR1, NeckR2, NeckR3, NeckR4, RHead1, RREye);
    }

    // ── helpers ─────────────────────────────────────────────────────────────

    /**
     * Walks a parent chain applying translateAndRotate for each ancestor,
     * then calls render() on the final (leaf) part.  Only the leaf's own
     * geometry is drawn; ancestor cubes are skipped.
     */
    private static void chainRender(PoseStack ps, VertexConsumer vc,
                                    int light, int overlay, int color,
                                    ModelPart... chain) {
        ps.pushPose();
        for (int i = 0; i < chain.length - 1; i++) {
            chain[i].translateAndRotate(ps);
        }
        chain[chain.length - 1].render(ps, vc, light, overlay, color);
        ps.popPose();
    }

    private static void setGroupVisible(ModelPart[] group, boolean visible) {
        for (ModelPart p : group) p.visible = visible;
    }
}
