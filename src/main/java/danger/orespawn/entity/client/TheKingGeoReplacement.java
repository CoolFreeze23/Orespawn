package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.TheKing;
import danger.orespawn.entity.pose.TheKingPose;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib The King (the hooks, owner 2026-09-14, addendum item 10; landed by the remainder slice, 2026-09-15 -
 * TEST-018, the second pass declared and proven pass by pass, the owner's closing set item 4):
 * {@link ModelTheKing#poseFrom} verbatim on the converted rig, ON THE HOOK (no keyframe layer, no transcription - the
 * self-gate stays closed until an artist delivers {@code idle} and {@code walk}; the geo, the wiring and the proofs
 * landed with the remainder). The port's classic model as it is, {@code WING_SPEED} 1.0f (orig
 * ClientProxyOreSpawn.java:492 passes {@code new ModelTheKing(0.65f)}; the port's model is what the hook transcribes);
 * every rhythm on the ATTACKING branch ({@code getAttacking() != 0}): the wings' roll (0.75 ws x PI x 0.21 attacking,
 * 0.35 ws x 0.15 gliding) on the ten parts of each wing - the third at five thirds and the fifth at seven thirds of it,
 * the third's pivot FOLLOWING the root 84 units along its roll and the fifth's the third by 184 (POSITION writes
 * through {@link #moveTo}), the even parts and the four feather parts riding the third at -+0.261, the right wing
 * mirrored; the thirteen claw parts flexing on a 0.75 ws cosine x PI x 0.25 (else 0) around -0.925 / 0.384 / 0.645; the
 * legs' swing on a 0.6 ws cosine x PI x 0.45 (else 0) - a quarter of it on the thigh and upper leg around 0.785, half
 * on the lower leg and foot around -0.628, the lower leg FOLLOWING the upper by 50 units and the seven claws of each
 * foot the lower leg by 66 (at -0.1 / +0.15 rad); the tail's yaw chain at 0.56 / 0.19 attacking over 0.26 / 0.08,
 * lagging pi / 4 per ring and FOLLOWING by 54 (a -1 x nudge) / 42 / 41 / 34 / 34 / 40 / 43 / 58 units, the two rear
 * ridges copying the first ring's yaw and the sixth ridge the seventh ring; and the three heads through
 * {@link #moveLeftHead} / {@link #moveCenterHead} / {@link #moveRightHead} (the classic helpers, the same names: the
 * one statement sequence over each head's part set): their sweeps (attacking 0.3 / 0.28 / 0.32 ws sines x PI x 0.25
 * sideways and 0.2 / 0.19 / 0.21 ws up-down, the jaws on 0.85 / 0.75 / 0.95 ws x 0.12 over 0.5; idle 0.17 / 0.13 / 0.19
 * ws x 0.08 sideways, 0.13 / 0.08 / 0.12 ws x 0.1 up-down, the jaws 0.45 / 0.65 / 0.55 ws x 0.04 over 0.25 - each jaw
 * adding its head's pitch), the side heads clamped to the centre head's sweep, and each head's four neck rings yawed
 * and pitched at 0.125 / 0.25 / 0.38 / 0.5 of it, FOLLOWING one another by 20 / 36 / 36 / 36 units first sideways, then
 * foreshortened and lifted by the pitch chain, the head group (three head parts, the mane, two eyes, two nose spikes)
 * riding the fourth ring by 36 and the jaw group (three jaws, four teeth) by 37 and 14 more along the head's pitch. The
 * entity is read through {@link TheKingPose} (the Slice 4b doctrine). Every value the classic reads back from a part it
 * just wrote is held in a local (the head chains' sideways-pass positions are re-derived by the pitch pass before any
 * draw, so each part is written once with its final values); the wing roots', the upper legs', the first tail ring's
 * and the first neck rings' pivots, never written, are read through {@link #classicPosition} (the bind).
 *
 * <p>THE SECOND PASS (TEST-018, landed): the classic draws the ten wing membranes ({@code Lwing2 / 4 / 6 / 8 / 10},
 * {@code Rwing2 / 4 / 6 / 8 / 10}) again in a translucent pass ({@link TheKingRenderer#render}:
 * {@link ModelTheKing#renderWingMembranes} on the model's own {@link ModelTheKing#WING_MEMBRANE_RENDER_TYPE} at
 * {@link ModelTheKing#WING_MEMBRANE_COLOR}; orig ModelTheKing.java's GL block) - not a setupAnim statement: the
 * descriptor declares it ({@link GeoReplacementDescriptor#secondPass}), the seam's renderer draws it as a layer after
 * the opaque pass with those bones hidden in it, and the probe captures both passes.</p>
 *
 * <p>Scale and shadow follow {@link TheKingRenderer}: 2.1 render scale, a quarter of it while
 * {@code getPlayNicely() != 0}, and a 1.9 x 2.1 shadow (ENT-S-092). No zero-thickness cube.</p>
 */
public final class TheKingGeoReplacement extends OreSpawnGeoReplacement<TheKing> {
    /** The port's {@code ModelTheKing.WING_SPEED} = 1.0f (orig ClientProxyOreSpawn.java:492 passes 0.65f): the chain's frequency multiplier. */
    static final float WING_SPEED = 1.0F;
    private static final GeoReplacementDescriptor<TheKing> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.THE_KING.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            TheKing.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/theking.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/theking.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/thekingtexture.png"),
            TheKingRenderer.SHADOW) {
        @Override
        public void applyScale(TheKing entity, PoseStack poseStack, float partialTick) {
            // orig RenderTheKing.preRenderScale (:39-45): PlayNicely gets glScalef(scale / 4), otherwise glScalef(scale)
            // (TheKingRenderer.scale) - the T2f Frog form the reference-renderer pins tool reads (the remainder slice)
            if (entity.getPlayNicely() != 0) {
                poseStack.scale(TheKingRenderer.SCALE / 4.0F, TheKingRenderer.SCALE / 4.0F, TheKingRenderer.SCALE / 4.0F);
                return;
            }
            poseStack.scale(TheKingRenderer.SCALE, TheKingRenderer.SCALE, TheKingRenderer.SCALE);
        }

        /** TEST-018 (the remainder slice): the ten wing membranes drawn again after the opaque pass on the model's own pass function and tint. */
        @Override
        public SecondPass secondPass() {
            return new SecondPass(java.util.List.of("Lwing2", "Lwing4", "Lwing6", "Lwing8", "Lwing10", "Rwing2", "Rwing4", "Rwing6", "Rwing8", "Rwing10"),
                    ModelTheKing.WING_MEMBRANE_RENDER_TYPE, ModelTheKing.WING_MEMBRANE_COLOR);
        }
    };

    public TheKingGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        TheKingPose entity = inputs.subject(TheKingPose.class);
        float ageInTicks = inputs.ageInTicks();
        // ModelTheKing.poseFrom verbatim.
        boolean attacking = entity.getAttacking() != 0;

        // --- Wing animation ---
        float newangle = attacking
                ? Mth.cos(ageInTicks * 0.75F * WING_SPEED) * (float) Math.PI * 0.21F
                : Mth.cos(ageInTicks * 0.35F * WING_SPEED) * (float) Math.PI * 0.15F;

        float lwing1ZRot = newangle;
        rotateZ(processor, "Lwing1", lwing1ZRot);
        rotateZ(processor, "Lwing2", newangle);
        float lwing3ZRot = newangle * 5.0F / 3.0F;
        rotateZ(processor, "Lwing3", lwing3ZRot);
        float[] lwing1 = classicPosition(bone(processor, "Lwing1"));  // never written: the bind pivot
        float lwing3Y = lwing1[1] + (float) Math.sin(lwing1ZRot) * 84.0F;
        float lwing3X = lwing1[0] + (float) Math.cos(lwing1ZRot) * 84.0F;
        moveXY(processor, "Lwing3", lwing3X, lwing3Y);
        rotateZ(processor, "Lwing4", lwing3ZRot);
        moveXY(processor, "Lwing4", lwing3X, lwing3Y);
        float lwing5ZRot = newangle * 7.0F / 3.0F;
        rotateZ(processor, "Lwing5", lwing5ZRot);
        float lwing5Y = lwing3Y + (float) Math.sin(lwing3ZRot) * 184.0F;
        float lwing5X = lwing3X + (float) Math.cos(lwing3ZRot) * 184.0F;
        moveXY(processor, "Lwing5", lwing5X, lwing5Y);
        rotateZ(processor, "Lwing6", lwing5ZRot);
        moveXY(processor, "Lwing6", lwing5X, lwing5Y);
        moveXY(processor, "Lwing9", lwing3X, lwing3Y);   // Lwing7.y = Lwing9.y = Lwing3.y; x the same
        moveXY(processor, "Lwing7", lwing3X, lwing3Y);
        moveXY(processor, "Lwing10", lwing3X, lwing3Y);  // Lwing8.y = Lwing10.y = Lwing3.y; x the same
        moveXY(processor, "Lwing8", lwing3X, lwing3Y);
        rotateZ(processor, "Lwing8", 0.261F + lwing3ZRot);   // Lwing7.zRot = Lwing8.zRot = ...
        rotateZ(processor, "Lwing7", 0.261F + lwing3ZRot);
        rotateZ(processor, "Lwing10", -0.261F + lwing3ZRot);  // Lwing9.zRot = Lwing10.zRot = ...
        rotateZ(processor, "Lwing9", -0.261F + lwing3ZRot);

        float rwing1ZRot = -newangle;
        rotateZ(processor, "Rwing1", rwing1ZRot);
        rotateZ(processor, "Rwing2", -newangle);
        float rwing3ZRot = -newangle * 5.0F / 3.0F;
        rotateZ(processor, "Rwing3", rwing3ZRot);
        float[] rwing1 = classicPosition(bone(processor, "Rwing1"));  // never written: the bind pivot
        float rwing3Y = rwing1[1] - (float) Math.sin(rwing1ZRot) * 84.0F;
        float rwing3X = rwing1[0] - (float) Math.cos(rwing1ZRot) * 84.0F;
        moveXY(processor, "Rwing3", rwing3X, rwing3Y);
        rotateZ(processor, "Rwing4", rwing3ZRot);
        moveXY(processor, "Rwing4", rwing3X, rwing3Y);
        float rwing5ZRot = -newangle * 7.0F / 3.0F;
        rotateZ(processor, "Rwing5", rwing5ZRot);
        float rwing5Y = rwing3Y - (float) Math.sin(rwing3ZRot) * 184.0F;
        float rwing5X = rwing3X - (float) Math.cos(rwing3ZRot) * 184.0F;
        moveXY(processor, "Rwing5", rwing5X, rwing5Y);
        rotateZ(processor, "Rwing6", rwing5ZRot);
        moveXY(processor, "Rwing6", rwing5X, rwing5Y);
        moveXY(processor, "Rwing9", rwing3X, rwing3Y);
        moveXY(processor, "Rwing7", rwing3X, rwing3Y);
        moveXY(processor, "Rwing10", rwing3X, rwing3Y);
        moveXY(processor, "Rwing8", rwing3X, rwing3Y);
        rotateZ(processor, "Rwing8", -0.261F + rwing3ZRot);
        rotateZ(processor, "Rwing7", -0.261F + rwing3ZRot);
        rotateZ(processor, "Rwing10", 0.261F + rwing3ZRot);
        rotateZ(processor, "Rwing9", 0.261F + rwing3ZRot);

        // --- Claw animation ---
        newangle = attacking
                ? Mth.cos(ageInTicks * 0.75F * WING_SPEED) * (float) Math.PI * 0.25F
                : 0.0F;

        rotateX(processor, "RClawRear", -0.925F + newangle);   // LClawRear.xRot = RClawRear.xRot = ...
        rotateX(processor, "LClawRear", -0.925F + newangle);
        rotateX(processor, "LLClaw1", 0.384F - newangle);
        rotateX(processor, "LLClaw2", 0.645F - newangle);
        rotateX(processor, "LCClaw1", 0.384F - newangle);
        rotateX(processor, "LCClaw2", 0.645F - newangle);
        rotateX(processor, "LRClaw1", 0.384F - newangle);
        rotateX(processor, "LRClaw2", 0.645F - newangle);
        rotateX(processor, "RLClaw1", 0.384F - newangle);
        rotateX(processor, "RLClaw2", 0.645F - newangle);
        rotateX(processor, "RCClaw1", 0.384F - newangle);
        rotateX(processor, "RCClaw2", 0.645F - newangle);
        rotateX(processor, "RRClaw1", 0.384F - newangle);
        rotateX(processor, "RRClaw2", 0.645F - newangle);

        // --- Leg animation ---
        newangle = attacking
                ? Mth.cos(ageInTicks * 0.6F * WING_SPEED) * (float) Math.PI * 0.45F
                : 0.0F;

        float lUpperLegXRot = 0.785F + newangle / 4.0F;   // LThigh.xRot = LUpperLeg.xRot = ...
        rotateX(processor, "LUpperLeg", lUpperLegXRot);
        rotateX(processor, "LThigh", lUpperLegXRot);
        float lLowerLegXRot = -0.628F + newangle / 2.0F;   // LLowerLeg.xRot = LFoot.xRot = ...
        rotateX(processor, "LFoot", lLowerLegXRot);
        rotateX(processor, "LLowerLeg", lLowerLegXRot);
        float[] lUpperLeg = classicPosition(bone(processor, "LUpperLeg"));  // never written: the bind pivot
        float lLowerLegY = lUpperLeg[1] + (float) Math.cos(lUpperLegXRot) * 50.0F;   // LLowerLeg.y = LFoot.y = ...
        float lLowerLegZ = lUpperLeg[2] + (float) Math.sin(lUpperLegXRot) * 50.0F;   // LLowerLeg.z = LFoot.z = ...
        moveYZ(processor, "LFoot", lLowerLegY, lLowerLegZ);
        moveYZ(processor, "LLowerLeg", lLowerLegY, lLowerLegZ);
        float lLClaw1Y = lLowerLegY + (float) Math.cos(lLowerLegXRot - 0.1F) * 66.0F;   // LLClaw1.y = LLClaw2.y = ...
        float lLClaw1Z = lLowerLegZ + (float) Math.sin(lLowerLegXRot - 0.1F) * 66.0F;   // LLClaw1.z = LLClaw2.z = ...
        moveYZ(processor, "LLClaw2", lLClaw1Y, lLClaw1Z);
        moveYZ(processor, "LLClaw1", lLClaw1Y, lLClaw1Z);
        moveYZ(processor, "LCClaw2", lLClaw1Y, lLClaw1Z);   // LCClaw1.y = LCClaw2.y = LLClaw1.y; z the same
        moveYZ(processor, "LCClaw1", lLClaw1Y, lLClaw1Z);
        moveYZ(processor, "LRClaw2", lLClaw1Y, lLClaw1Z);   // LRClaw1.y = LRClaw2.y = LLClaw1.y; z the same
        moveYZ(processor, "LRClaw1", lLClaw1Y, lLClaw1Z);
        float lClawRearY = lLowerLegY + (float) Math.cos(lLowerLegXRot + 0.15F) * 66.0F;
        float lClawRearZ = lLowerLegZ + (float) Math.sin(lLowerLegXRot + 0.15F) * 66.0F;
        moveYZ(processor, "LClawRear", lClawRearY, lClawRearZ);

        float rUpperLegXRot = 0.785F - newangle / 4.0F;   // RThigh.xRot = RUpperLeg.xRot = ...
        rotateX(processor, "RUpperLeg", rUpperLegXRot);
        rotateX(processor, "RThigh", rUpperLegXRot);
        float rLowerLegXRot = -0.628F - newangle / 2.0F;   // RLowerLeg.xRot = RFoot.xRot = ...
        rotateX(processor, "RFoot", rLowerLegXRot);
        rotateX(processor, "RLowerLeg", rLowerLegXRot);
        float[] rUpperLeg = classicPosition(bone(processor, "RUpperLeg"));  // never written: the bind pivot
        float rLowerLegY = rUpperLeg[1] + (float) Math.cos(rUpperLegXRot) * 50.0F;
        float rLowerLegZ = rUpperLeg[2] + (float) Math.sin(rUpperLegXRot) * 50.0F;
        moveYZ(processor, "RFoot", rLowerLegY, rLowerLegZ);
        moveYZ(processor, "RLowerLeg", rLowerLegY, rLowerLegZ);
        float rLClaw1Y = rLowerLegY + (float) Math.cos(rLowerLegXRot - 0.1F) * 66.0F;
        float rLClaw1Z = rLowerLegZ + (float) Math.sin(rLowerLegXRot - 0.1F) * 66.0F;
        moveYZ(processor, "RLClaw2", rLClaw1Y, rLClaw1Z);
        moveYZ(processor, "RLClaw1", rLClaw1Y, rLClaw1Z);
        moveYZ(processor, "RCClaw2", rLClaw1Y, rLClaw1Z);
        moveYZ(processor, "RCClaw1", rLClaw1Y, rLClaw1Z);
        moveYZ(processor, "RRClaw2", rLClaw1Y, rLClaw1Z);
        moveYZ(processor, "RRClaw1", rLClaw1Y, rLClaw1Z);
        float rClawRearY = rLowerLegY + (float) Math.cos(rLowerLegXRot + 0.15F) * 66.0F;
        float rClawRearZ = rLowerLegZ + (float) Math.sin(rLowerLegXRot + 0.15F) * 66.0F;
        moveYZ(processor, "RClawRear", rClawRearY, rClawRearZ);

        // --- Tail animation ---
        float tailspeed = 0.26F;
        float tailamp = 0.08F;
        float pi4 = 0.7853982F;
        if (attacking) {
            tailspeed = 0.56F;
            tailamp = 0.19F;
        }

        float tail1YRot = Mth.cos(ageInTicks * tailspeed * WING_SPEED) * (float) Math.PI * tailamp / 2.0F;   // Ridge4.yRot = Ridge5.yRot = Tail1.yRot = ...
        rotateY(processor, "Tail1", tail1YRot);
        rotateY(processor, "Ridge5", tail1YRot);
        rotateY(processor, "Ridge4", tail1YRot);
        float[] tail1 = classicPosition(bone(processor, "Tail1"));  // never written: the bind pivot
        float tail2Z = tail1[2] + (float) Math.cos(tail1YRot) * 54.0F;
        float tail2X = tail1[0] - 1.0F + (float) Math.sin(tail1YRot) * 54.0F;
        moveXZ(processor, "Tail2", tail2X, tail2Z);
        float tail2YRot = Mth.cos(ageInTicks * tailspeed * WING_SPEED - pi4) * (float) Math.PI * tailamp;
        rotateY(processor, "Tail2", tail2YRot);
        float tail3Z = tail2Z + (float) Math.cos(tail2YRot) * 42.0F;
        float tail3X = tail2X + (float) Math.sin(tail2YRot) * 42.0F;
        moveXZ(processor, "Tail3", tail3X, tail3Z);
        float tail3YRot = Mth.cos(ageInTicks * tailspeed * WING_SPEED - 2.0F * pi4) * (float) Math.PI * tailamp;
        rotateY(processor, "Tail3", tail3YRot);
        float tail4Z = tail3Z + (float) Math.cos(tail3YRot) * 41.0F;
        float tail4X = tail3X + (float) Math.sin(tail3YRot) * 41.0F;
        moveXZ(processor, "Tail4", tail4X, tail4Z);
        float tail4YRot = Mth.cos(ageInTicks * tailspeed * WING_SPEED - 3.0F * pi4) * (float) Math.PI * tailamp;
        rotateY(processor, "Tail4", tail4YRot);

        newangle = Mth.cos(ageInTicks * tailspeed * WING_SPEED - 3.0F * pi4) * (float) Math.PI * tailamp;
        float tail5Z = tail4Z + (float) Math.cos(tail4YRot) * 34.0F;
        float tail5X = tail4X + (float) Math.sin(tail4YRot) * 34.0F;
        moveXZ(processor, "Tail5", tail5X, tail5Z);
        newangle /= 2.0F;
        float tail5YRot = tail4YRot + newangle;
        rotateY(processor, "Tail5", tail5YRot);
        float tail6Z = tail5Z + (float) Math.cos(tail5YRot) * 34.0F;
        float tail6X = tail5X + (float) Math.sin(tail5YRot) * 34.0F;
        moveXZ(processor, "Tail6", tail6X, tail6Z);
        float tail6YRot = tail5YRot + newangle;
        rotateY(processor, "Tail6", tail6YRot);
        float tail7Z = tail6Z + (float) Math.cos(tail6YRot) * 40.0F;   // Tail7.z = Ridge6.z = ...
        float tail7X = tail6X + (float) Math.sin(tail6YRot) * 40.0F;   // Tail7.x = Ridge6.x = ...
        moveXZ(processor, "Ridge6", tail7X, tail7Z);
        moveXZ(processor, "Tail7", tail7X, tail7Z);
        float tail7YRot = tail6YRot + newangle;   // Tail7.yRot = Ridge6.yRot = ...
        rotateY(processor, "Ridge6", tail7YRot);
        rotateY(processor, "Tail7", tail7YRot);
        float tailTipZ = tail7Z + (float) Math.cos(tail7YRot) * 43.0F;   // TailTip.z = TailTip2.z = ...
        float tailTipX = tail7X + (float) Math.sin(tail7YRot) * 43.0F;   // TailTip.x = TailTip2.x = ...
        moveXZ(processor, "TailTip2", tailTipX, tailTipZ);
        moveXZ(processor, "TailTip", tailTipX, tailTipZ);
        float tailTipYRot = tail7YRot + newangle;   // TailTip.yRot = TailTip2.yRot = ...
        rotateY(processor, "TailTip2", tailTipYRot);
        rotateY(processor, "TailTip", tailTipYRot);
        float tailSpikeZ = tailTipZ + (float) Math.cos(tailTipYRot) * 58.0F;
        float tailSpikeX = tailTipX + (float) Math.sin(tailTipYRot) * 58.0F;
        moveXZ(processor, "TailSpike", tailSpikeX, tailSpikeZ);
        rotateY(processor, "TailSpike", tailTipYRot + newangle);

        // --- Head animation ---
        float Lheadlr, Lheadud, Ljawangle;
        float Cheadlr, Cheadud, Cjawangle;
        float Rheadlr, Rheadud, Rjawangle;

        if (attacking) {
            Lheadlr = Mth.sin(ageInTicks * 0.3F * WING_SPEED) * (float) Math.PI * 0.25F;
            Lheadud = Mth.sin(ageInTicks * 0.2F * WING_SPEED) * (float) Math.PI * 0.25F;
            Ljawangle = Mth.sin(ageInTicks * 0.85F * WING_SPEED) * (float) Math.PI * 0.12F;
            Rheadlr = Mth.sin(ageInTicks * 0.32F * WING_SPEED) * (float) Math.PI * 0.25F;
            Rheadud = Mth.sin(ageInTicks * 0.21F * WING_SPEED) * (float) Math.PI * 0.25F;
            Rjawangle = Mth.sin(ageInTicks * 0.95F * WING_SPEED) * (float) Math.PI * 0.12F;
            Cheadlr = Mth.sin(ageInTicks * 0.28F * WING_SPEED) * (float) Math.PI * 0.25F;
            Cheadud = Mth.sin(ageInTicks * 0.19F * WING_SPEED) * (float) Math.PI * 0.25F;
            Cjawangle = Mth.sin(ageInTicks * 0.75F * WING_SPEED) * (float) Math.PI * 0.12F;
            Ljawangle += 0.5F + Lheadud;
            Cjawangle += 0.5F + Cheadud;
            Rjawangle += 0.5F + Rheadud;
        } else {
            Lheadlr = Mth.sin(ageInTicks * 0.17F * WING_SPEED) * (float) Math.PI * 0.08F;
            Lheadud = Mth.sin(ageInTicks * 0.13F * WING_SPEED) * (float) Math.PI * 0.1F;
            Ljawangle = Mth.sin(ageInTicks * 0.45F * WING_SPEED) * (float) Math.PI * 0.04F;
            Rheadlr = Mth.sin(ageInTicks * 0.19F * WING_SPEED) * (float) Math.PI * 0.08F;
            Rheadud = Mth.sin(ageInTicks * 0.12F * WING_SPEED) * (float) Math.PI * 0.1F;
            Rjawangle = Mth.sin(ageInTicks * 0.55F * WING_SPEED) * (float) Math.PI * 0.04F;
            Cheadlr = Mth.sin(ageInTicks * 0.13F * WING_SPEED) * (float) Math.PI * 0.08F;
            Cheadud = Mth.sin(ageInTicks * 0.08F * WING_SPEED) * (float) Math.PI * 0.1F;
            Cjawangle = Mth.sin(ageInTicks * 0.65F * WING_SPEED) * (float) Math.PI * 0.04F;
            Ljawangle += 0.25F + Lheadud;
            Cjawangle += 0.25F + Cheadud;
            Rjawangle += 0.25F + Rheadud;
        }

        if (Lheadlr > Cheadlr) Lheadlr = Cheadlr;
        if (Rheadlr < Cheadlr) Rheadlr = Cheadlr;

        moveLeftHead(processor, Lheadlr, Lheadud, Ljawangle);
        moveCenterHead(processor, Cheadlr, Cheadud, Cjawangle);
        moveRightHead(processor, Rheadlr, Rheadud, Rjawangle);
    }

    /** ModelTheKing.moveLeftHead: the head statements over the {@code L} part set ({@code NeckL1..4}, {@code LHead1..3}, ...). */
    private static void moveLeftHead(AnimationProcessor<?> processor, float Lheadlr, float Lheadud, float Ljawangle) {
        moveHead(processor, "L", Lheadlr, Lheadud, Ljawangle);
    }

    /** ModelTheKing.moveCenterHead: the same statements over the {@code C} part set. */
    private static void moveCenterHead(AnimationProcessor<?> processor, float Cheadlr, float Cheadud, float Cjawangle) {
        moveHead(processor, "C", Cheadlr, Cheadud, Cjawangle);
    }

    /** ModelTheKing.moveRightHead: the same statements over the {@code R} part set. */
    private static void moveRightHead(AnimationProcessor<?> processor, float Rheadlr, float Rheadud, float Rjawangle) {
        moveHead(processor, "R", Rheadlr, Rheadud, Rjawangle);
    }

    /**
     * The three classic head helpers are one statement sequence over a head's part set (the {@code L} / {@code C} /
     * {@code R} prefix), transcribed once: the jaw pitches; the sideways chain (yaw at 0.125 / 0.25 / 0.38 / 0.5 down
     * the four neck rings, each ring's pivot 20 / 36 / 36 / 36 units on from the last along its yaw, the head group 36
     * and the jaw group 37 past the fourth); then the pitch chain (pitch at the same fractions, each ring lifted by the
     * sine and its sideways offset foreshortened by the cosine of the previous ring's pitch, the jaw 14 more along the
     * head's pitch). The sideways-pass positions are the classic's intermediate part values, re-derived by the pitch
     * pass before any draw - held in locals and written once with the final values; the first ring's pivot is never
     * written (the bind).
     */
    private static void moveHead(AnimationProcessor<?> processor, String p, float headlr, float headud, float jawangle) {
        String neck1 = "Neck" + p + "1";
        String neck2 = "Neck" + p + "2";
        String neck3 = "Neck" + p + "3";
        String neck4 = "Neck" + p + "4";
        String head1 = p + "Head1";
        String head2 = p + "Head2";
        String head3 = p + "Head3";
        String headMane = p + "HeadMane";
        String lEye = p + "LEye";
        String rEye = p + "REye";
        String lNoseSpike = p + "LNoseSpike";
        String rNoseSpike = p + "RNoseSpike";
        String jaw1 = p + "Jaw1";
        String jaw2 = p + "Jaw2";
        String jaw3 = p + "Jaw3";
        String tooth1 = p + "Tooth1";
        String tooth2 = p + "Tooth2";
        String tooth3 = p + "Tooth3";
        String tooth4 = p + "Tooth4";

        rotateX(processor, jaw1, jawangle);
        rotateX(processor, jaw2, jawangle);
        rotateX(processor, jaw3, jawangle);
        rotateX(processor, tooth1, jawangle);
        rotateX(processor, tooth2, jawangle);
        rotateX(processor, tooth3, jawangle);
        rotateX(processor, tooth4, jawangle);

        // Horizontal (yRot) chain
        float neck1YRot = headlr * 0.125F;
        rotateY(processor, neck1, neck1YRot);
        float[] neck1Position = classicPosition(bone(processor, neck1));  // never written: the bind pivot
        float neck2Z = neck1Position[2] - (float) Math.cos(neck1YRot) * 20.0F;
        float neck2X = neck1Position[0] - (float) Math.sin(neck1YRot) * 20.0F;
        float neck2YRot = headlr * 0.25F;
        rotateY(processor, neck2, neck2YRot);
        float neck3Z = neck2Z - (float) Math.cos(neck2YRot) * 36.0F;
        float neck3X = neck2X - (float) Math.sin(neck2YRot) * 36.0F;
        float neck3YRot = headlr * 0.38F;
        rotateY(processor, neck3, neck3YRot);
        float neck4Z = neck3Z - (float) Math.cos(neck3YRot) * 36.0F;
        float neck4X = neck3X - (float) Math.sin(neck3YRot) * 36.0F;
        float neck4YRot = headlr * 0.5F;
        rotateY(processor, neck4, neck4YRot);
        float head1Z = neck4Z - (float) Math.cos(neck4YRot) * 36.0F;
        float head1X = neck4X - (float) Math.sin(neck4YRot) * 36.0F;
        rotateY(processor, head1, headlr);
        rotateY(processor, head2, headlr);
        rotateY(processor, head3, headlr);
        rotateY(processor, headMane, headlr);
        rotateY(processor, lEye, headlr);
        rotateY(processor, rEye, headlr);
        rotateY(processor, lNoseSpike, 0.244F + headlr);
        rotateY(processor, rNoseSpike, -0.261F + headlr);
        rotateY(processor, jaw1, headlr);
        float jaw1Z = neck4Z - (float) Math.cos(neck4YRot) * 37.0F;
        float jaw1X = neck4X - (float) Math.sin(neck4YRot) * 37.0F;
        rotateY(processor, jaw2, headlr);
        rotateY(processor, jaw3, headlr);
        rotateY(processor, tooth1, headlr);
        rotateY(processor, tooth2, headlr);
        rotateY(processor, tooth3, headlr);
        rotateY(processor, tooth4, headlr);
        // (the head and jaw groups' z / x copied here are re-derived below before any draw)

        // Vertical (xRot) chain
        float neck1XRot = headud * 0.125F;
        rotateX(processor, neck1, neck1XRot);
        float neck2Y = neck1Position[1] + (float) Math.sin(neck1XRot) * 20.0F;
        neck2Z = neck1Position[2] + (neck2Z - neck1Position[2]) * (float) Math.cos(neck1XRot);
        neck2X = neck1Position[0] + (neck2X - neck1Position[0]) * (float) Math.cos(neck1XRot);
        moveTo(processor, neck2, neck2X, neck2Y, neck2Z);
        float neck2XRot = headud * 0.25F;
        rotateX(processor, neck2, neck2XRot);
        float neck3Y = neck2Y + (float) Math.sin(neck2XRot) * 36.0F;
        neck3Z = neck2Z + (neck3Z - neck2Z) * (float) Math.cos(neck2XRot);
        neck3X = neck2X + (neck3X - neck2X) * (float) Math.cos(neck2XRot);
        moveTo(processor, neck3, neck3X, neck3Y, neck3Z);
        float neck3XRot = headud * 0.38F;
        rotateX(processor, neck3, neck3XRot);
        float neck4Y = neck3Y + (float) Math.sin(neck3XRot) * 36.0F;
        neck4Z = neck3Z + (neck4Z - neck3Z) * (float) Math.cos(neck3XRot);
        neck4X = neck3X + (neck4X - neck3X) * (float) Math.cos(neck3XRot);
        moveTo(processor, neck4, neck4X, neck4Y, neck4Z);
        float neck4XRot = headud * 0.5F;
        rotateX(processor, neck4, neck4XRot);
        float head1Y = neck4Y + (float) Math.sin(neck4XRot) * 36.0F;
        head1Z = neck4Z + (head1Z - neck4Z) * (float) Math.cos(neck4XRot);
        head1X = neck4X + (head1X - neck4X) * (float) Math.cos(neck4XRot);
        moveTo(processor, head1, head1X, head1Y, head1Z);
        float head1XRot = headud;
        rotateX(processor, head1, head1XRot);
        rotateX(processor, head2, headud);
        moveTo(processor, head2, head1X, head1Y, head1Z);
        rotateX(processor, head3, headud);
        moveTo(processor, head3, head1X, head1Y, head1Z);
        rotateX(processor, headMane, 0.384F + headud);
        moveTo(processor, headMane, head1X, head1Y, head1Z);
        rotateX(processor, lEye, headud);
        moveTo(processor, lEye, head1X, head1Y, head1Z);
        rotateX(processor, rEye, headud);
        moveTo(processor, rEye, head1X, head1Y, head1Z);
        rotateX(processor, lNoseSpike, 0.244F + headud);
        moveTo(processor, lNoseSpike, head1X, head1Y, head1Z);
        rotateX(processor, rNoseSpike, 0.261F + headud);
        moveTo(processor, rNoseSpike, head1X, head1Y, head1Z);
        float jaw1Y = head1Y + (float) Math.cos(head1XRot) * 14.0F;
        jaw1Z = neck4Z + (jaw1Z - neck4Z) * (float) Math.cos(neck4XRot);
        jaw1Z += (float) Math.sin(head1XRot) * 14.0F;
        jaw1X = neck4X + (jaw1X - neck4X) * (float) Math.cos(neck4XRot);
        moveTo(processor, jaw1, jaw1X, jaw1Y, jaw1Z);
        moveTo(processor, jaw2, jaw1X, jaw1Y, jaw1Z);
        moveTo(processor, jaw3, jaw1X, jaw1Y, jaw1Z);
        moveTo(processor, tooth1, jaw1X, jaw1Y, jaw1Z);
        moveTo(processor, tooth2, jaw1X, jaw1Y, jaw1Z);
        moveTo(processor, tooth3, jaw1X, jaw1Y, jaw1Z);
        moveTo(processor, tooth4, jaw1X, jaw1Y, jaw1Z);
    }

    /** {@code part.x = x; part.y = y} on a part whose z the classic never writes (the bind). */
    private static void moveXY(AnimationProcessor<?> processor, String name, float x, float y) {
        float[] position = classicPosition(bone(processor, name));
        moveTo(processor, name, x, y, position[2]);
    }

    /** {@code part.y = y; part.z = z} on a part whose x the classic never writes (the bind). */
    private static void moveYZ(AnimationProcessor<?> processor, String name, float y, float z) {
        float[] position = classicPosition(bone(processor, name));
        moveTo(processor, name, position[0], y, z);
    }

    /** {@code part.x = x; part.z = z} on a part whose y the classic never writes (the bind). */
    private static void moveXZ(AnimationProcessor<?> processor, String name, float x, float z) {
        float[] position = classicPosition(bone(processor, name));
        moveTo(processor, name, x, position[1], z);
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<TheKing, TheKingGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new TheKingGeoReplacement());
        }

        /** {@code TheKingRenderer.shouldRender} is unconditionally true (OPT-013: the huge box pops out of vanilla's culling); the seam's renderer the same. */
        @Override
        public boolean shouldRender(TheKing entity, net.minecraft.client.renderer.culling.Frustum frustum, double x, double y, double z) {
            return true;
        }
    }
}
