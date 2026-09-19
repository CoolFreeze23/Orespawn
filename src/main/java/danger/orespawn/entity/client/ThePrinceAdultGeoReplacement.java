package danger.orespawn.entity.client;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.ThePrinceAdult;
import danger.orespawn.entity.pose.ThePrinceAdultPose;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib The Prince (adult) (the hooks, landed by the first Tier-1 slice T1a): {@link
 * ModelThePrinceAdult#poseFrom} verbatim on the converted rig (the King's part set under the Prince's own model), ON
 * THE HOOK (no keyframe layer, no transcription - the self-gate stays closed until an artist delivers {@code idle} and
 * {@code walk} ; the geo, the wiring and the proofs landed with T1a). Wingspeed 0.65f (the port's model field): the
 * wings' roll by ATTACKING / ACTIVITY / SIT (0.75 ws x PI x 0.21 attacking; 0.35 ws x 0.15 gliding, scaled by
 * the walking speed while the activity is 0; 0 while ordered to sit) on the ten parts of each wing - the third at
 * five thirds and the fifth at seven thirds of it, the third's pivot FOLLOWING the root 84 units along its
 * roll and the fifth's the third by 184 (POSITION writes through {@link #moveTo} ), the even parts and the four
 * feather parts riding the third at -+0.261, the right wing mirrored; the thirteen claw parts flexing on a 0.75 ws
 * cosine x PI x 0.25 while attacking (else 0) around -0.925 / 0.384 / 0.645; the legs' swing (0.6 ws x PI x 0.45
 * attacking; a 0.3 ws x PI x 0.25 x amount gait while walking and not sitting; else 0) - a quarter of it on the thigh
 * and upper leg around 0.785, half on the lower leg and foot around -0.628, the lower leg FOLLOWING the upper by 50
 * units and the seven claws of each foot the lower leg by 66 (at -0.1 / +0.15 rad); the tail's yaw chain at 0.56 /
 * 0.19 attacking over 0.26 / 0.08, both 0 while sitting, lagging pi / 4 per ring and FOLLOWING by 54 (a -1 x nudge) /
 * 42 / 41 / 34 / 34 / 40 / 43 / 58 units, the two rear ridges copying the first ring's yaw and the sixth ridge the
 * seventh ring; and the three heads through {@link #moveLeftHead} / {@link #moveCenterHead} / {@link
 * #moveRightHead} (the classic helpers, the same names: the one statement sequence over each head's part set): their
 * pitch from the entity's HEAD EXTENSIONS ({@code getHead1Ext / 2 / 3} minus 30, degrees) in every state, their
 * sideways sweeps attacking (0.3 / 0.28 / 0.32 ws sines x PI x 0.25, the jaws on 0.85 / 0.75 / 0.95 ws x 0.12 over 0.5)
 * or flying (0.17 / 0.13 / 0.19 ws x 0.08, the jaws 0.45 / 0.65 / 0.55 ws x 0.04 over 0.25), and walking the three-head
 * LOOK split (two thirds of the head yaw / pitch, the side heads at half of it on the side the head turns to, in
 * radians, the pitch added to the extension, the jaws 0.25 / 0.45 / 0.35 ws x 0.03 over 0.25) - each jaw adding its
 * head's pitch, the side heads clamped to the centre head's sweep; each head's four neck rings yawed and pitched at
 * 0.125 / 0.25 / 0.38 / 0.5 of it, FOLLOWING one another by 20 / 36 / 36 / 36 units first sideways, then foreshortened
 * and lifted by the pitch chain, the head group (three head parts, the mane, two eyes, two nose spikes) riding the
 * fourth ring by 36 and the jaw group (three jaws, four teeth) 38 units behind the fourth ring along its pitch, 12
 * along the head's pitch and 7 across the head's yaw. The entity is read through {@link ThePrinceAdultPose} (the Slice
 * 4b doctrine). Every value the classic reads back from a part it just wrote is held in a local (the head chains'
 * sideways-pass positions are re-derived by the pitch pass before any draw, so each part is written once with its final
 * values); the wing roots', the upper legs', the first tail ring's and the first neck rings' pivots, never written, are
 * read through {@link #classicPosition} (the bind).
 * <p>Shadow follows {@link ThePrinceAdultRenderer} 's constructor ({@code super(context, model, 1.2f)}: the literal it
 * passes - no SHADOW constant); its private {@code SCALE} is 1.0 (identity, applied around {@code super.render} ), so
 * no scale hook. No zero-thickness cube.</p>
 */
public final class ThePrinceAdultGeoReplacement extends OreSpawnGeoReplacement<ThePrinceAdult> {
    /** The port's {@code ModelThePrinceAdult.wingspeed} = 0.65f: the chain's frequency multiplier. */
    static final float WINGSPEED = 0.65F;
    private static final GeoReplacementDescriptor<ThePrinceAdult> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.THE_PRINCE_ADULT.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            ThePrinceAdult.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/theprinceadult.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/theprinceadult.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/theprinceadult.png"),
            // ThePrinceAdultRenderer's constructor passes the literal 1.2f (no SHADOW constant): the equal literal
            1.2F) {
    };

    public ThePrinceAdultGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        ThePrinceAdultPose entity = inputs.subject(ThePrinceAdultPose.class);
        float limbSwingAmount = inputs.limbSwingAmount();
        float ageInTicks = inputs.ageInTicks();
        float netHeadYaw = inputs.netHeadYaw();
        float headPitch = inputs.headPitch();
        // ModelThePrinceAdult.poseFrom verbatim.
        float newangle;
        float tailspeed = 0.26F;
        float tailamp = 0.08F;
        float pi4 = 0.7853982F;
        float Lheadlr = 0.0F;
        float Lheadud = 0.0F;
        float Ljawangle = 0.0F;
        float Cheadlr = 0.0F;
        float Cheadud = 0.0F;
        float Cjawangle = 0.0F;
        float Rheadlr = 0.0F;
        float Rheadud = 0.0F;
        float Rjawangle = 0.0F;

        if (entity.getAttacking() != 0) {
            newangle = Mth.cos(ageInTicks * 0.75F * WINGSPEED) * (float) Math.PI * 0.21F;
        } else {
            newangle = Mth.cos(ageInTicks * 0.35F * WINGSPEED) * (float) Math.PI * 0.15F;
            if (entity.getActivity() == 0) {
                newangle = Mth.cos(ageInTicks * 0.35F * WINGSPEED) * (float) Math.PI * 0.15F * limbSwingAmount;
            }
        }
        if (entity.isOrderedToSit()) {
            newangle = 0.0F;
        }

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

        newangle = 0.0F;
        if (entity.getAttacking() != 0) {
            newangle = Mth.cos(ageInTicks * 0.75F * WINGSPEED) * (float) Math.PI * 0.25F;
        }
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

        newangle = 0.0F;
        if (entity.getAttacking() != 0) {
            newangle = Mth.cos(ageInTicks * 0.6F * WINGSPEED) * (float) Math.PI * 0.45F;
        } else if (!entity.isOrderedToSit() && entity.getActivity() == 0) {
            newangle = Mth.cos(ageInTicks * 0.3F * WINGSPEED) * (float) Math.PI * 0.25F * limbSwingAmount;
        }

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

        if (entity.getAttacking() != 0) {
            tailspeed = 0.56F;
            tailamp = 0.19F;
        }
        if (entity.isOrderedToSit()) {
            tailspeed = 0.0F;
            tailamp = 0.0F;
        }

        float tail1YRot = Mth.cos(ageInTicks * tailspeed * WINGSPEED) * (float) Math.PI * tailamp / 2.0F;   // Ridge4.yRot = Ridge5.yRot = (Tail1.yRot = ...)
        rotateY(processor, "Tail1", tail1YRot);
        rotateY(processor, "Ridge5", tail1YRot);
        rotateY(processor, "Ridge4", tail1YRot);
        float[] tail1 = classicPosition(bone(processor, "Tail1"));  // never written: the bind pivot
        float tail2Z = tail1[2] + (float) Math.cos(tail1YRot) * 54.0F;
        float tail2X = tail1[0] - 1.0F + (float) Math.sin(tail1YRot) * 54.0F;
        moveXZ(processor, "Tail2", tail2X, tail2Z);
        float tail2YRot = Mth.cos(ageInTicks * tailspeed * WINGSPEED - pi4) * (float) Math.PI * tailamp;
        rotateY(processor, "Tail2", tail2YRot);
        float tail3Z = tail2Z + (float) Math.cos(tail2YRot) * 42.0F;
        float tail3X = tail2X + (float) Math.sin(tail2YRot) * 42.0F;
        moveXZ(processor, "Tail3", tail3X, tail3Z);
        float tail3YRot = Mth.cos(ageInTicks * tailspeed * WINGSPEED - 2.0F * pi4) * (float) Math.PI * tailamp;
        rotateY(processor, "Tail3", tail3YRot);
        float tail4Z = tail3Z + (float) Math.cos(tail3YRot) * 41.0F;
        float tail4X = tail3X + (float) Math.sin(tail3YRot) * 41.0F;
        moveXZ(processor, "Tail4", tail4X, tail4Z);
        float tail4YRot = Mth.cos(ageInTicks * tailspeed * WINGSPEED - 3.0F * pi4) * (float) Math.PI * tailamp;
        rotateY(processor, "Tail4", tail4YRot);
        newangle = Mth.cos(ageInTicks * tailspeed * WINGSPEED - 3.0F * pi4) * (float) Math.PI * tailamp;
        float tail5Z = tail4Z + (float) Math.cos(tail4YRot) * 34.0F;
        float tail5X = tail4X + (float) Math.sin(tail4YRot) * 34.0F;
        moveXZ(processor, "Tail5", tail5X, tail5Z);
        float tail5YRot = tail4YRot + (newangle /= 2.0F);
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

        if (entity.getAttacking() != 0) {
            Lheadlr = Mth.sin(ageInTicks * 0.3F * WINGSPEED) * (float) Math.PI * 0.25F;
            Lheadud = (float) Math.toRadians(entity.getHead1Ext() - 30);
            Ljawangle = Mth.sin(ageInTicks * 0.85F * WINGSPEED) * (float) Math.PI * 0.12F;
            Rheadlr = Mth.sin(ageInTicks * 0.32F * WINGSPEED) * (float) Math.PI * 0.25F;
            Rheadud = (float) Math.toRadians(entity.getHead3Ext() - 30);
            Rjawangle = Mth.sin(ageInTicks * 0.95F * WINGSPEED) * (float) Math.PI * 0.12F;
            Cheadlr = Mth.sin(ageInTicks * 0.28F * WINGSPEED) * (float) Math.PI * 0.25F;
            Cheadud = (float) Math.toRadians(entity.getHead2Ext() - 30);
            Cjawangle = Mth.sin(ageInTicks * 0.75F * WINGSPEED) * (float) Math.PI * 0.12F;
            Ljawangle += 0.5F;
            Ljawangle += Lheadud;
            Cjawangle += 0.5F;
            Cjawangle += Cheadud;
            Rjawangle += 0.5F;
            Rjawangle += Rheadud;
        } else {
            if (entity.getActivity() != 0) {
                Lheadlr = Mth.sin(ageInTicks * 0.17F * WINGSPEED) * (float) Math.PI * 0.08F;
                Lheadud = (float) Math.toRadians(entity.getHead1Ext() - 30);
                Ljawangle = Mth.sin(ageInTicks * 0.45F * WINGSPEED) * (float) Math.PI * 0.04F;
                Rheadlr = Mth.sin(ageInTicks * 0.19F * WINGSPEED) * (float) Math.PI * 0.08F;
                Rheadud = (float) Math.toRadians(entity.getHead3Ext() - 30);
                Rjawangle = Mth.sin(ageInTicks * 0.55F * WINGSPEED) * (float) Math.PI * 0.04F;
                Cheadlr = Mth.sin(ageInTicks * 0.13F * WINGSPEED) * (float) Math.PI * 0.08F;
                Cheadud = (float) Math.toRadians(entity.getHead2Ext() - 30);
                Cjawangle = Mth.sin(ageInTicks * 0.65F * WINGSPEED) * (float) Math.PI * 0.04F;
            } else {
                Lheadud = (float) Math.toRadians(entity.getHead1Ext() - 30);
                Rheadud = (float) Math.toRadians(entity.getHead3Ext() - 30);
                Cheadud = (float) Math.toRadians(entity.getHead2Ext() - 30);
                float h3 = netHeadYaw * 2.0F / 3.0F;
                float h2 = h3;
                float h1 = h3;
                float d3 = headPitch * 2.0F / 3.0F;
                float d2 = d3;
                float d1 = d3;
                if (h1 < 0.0F) {
                    h2 = h3 = h1 / 2.0F;
                    d2 = d3 = d1 / 2.0F;
                } else {
                    h2 = h1 = h3 / 2.0F;
                    d2 = d1 = d3 / 2.0F;
                }
                Lheadlr = (float) Math.toRadians(h1);
                Cheadlr = (float) Math.toRadians(h2);
                Rheadlr = (float) Math.toRadians(h3);
                Lheadud += (float) Math.toRadians(d1);
                Cheadud += (float) Math.toRadians(d2);
                Rheadud += (float) Math.toRadians(d3);
                Ljawangle = Mth.sin(ageInTicks * 0.25F * WINGSPEED) * (float) Math.PI * 0.03F;
                Rjawangle = Mth.sin(ageInTicks * 0.35F * WINGSPEED) * (float) Math.PI * 0.03F;
                Cjawangle = Mth.sin(ageInTicks * 0.45F * WINGSPEED) * (float) Math.PI * 0.03F;
            }
            Ljawangle += 0.25F;
            Ljawangle += Lheadud;
            Cjawangle += 0.25F;
            Cjawangle += Cheadud;
            Rjawangle += 0.25F;
            Rjawangle += Rheadud;
        }
        if (Lheadlr > Cheadlr) {
            Lheadlr = Cheadlr;
        }
        if (Rheadlr < Cheadlr) {
            Rheadlr = Cheadlr;
        }
        moveLeftHead(processor, Lheadlr, Lheadud, Ljawangle);
        moveCenterHead(processor, Cheadlr, Cheadud, Cjawangle);
        moveRightHead(processor, Rheadlr, Rheadud, Rjawangle);
    }

    /** ModelThePrinceAdult.moveLeftHead: the head statements over the {@code L} part set ({@code NeckL1..4}, {@code LHead1..3}, ...). */
    private static void moveLeftHead(AnimationProcessor<?> processor, float Lheadlr, float Lheadud, float Ljawangle) {
        moveHead(processor, "L", Lheadlr, Lheadud, Ljawangle);
    }

    /** ModelThePrinceAdult.moveCenterHead: the same statements over the {@code C} part set. */
    private static void moveCenterHead(AnimationProcessor<?> processor, float Cheadlr, float Cheadud, float Cjawangle) {
        moveHead(processor, "C", Cheadlr, Cheadud, Cjawangle);
    }

    /** ModelThePrinceAdult.moveRightHead: the same statements over the {@code R} part set. */
    private static void moveRightHead(AnimationProcessor<?> processor, float Rheadlr, float Rheadud, float Rjawangle) {
        moveHead(processor, "R", Rheadlr, Rheadud, Rjawangle);
    }

    /**
     * The three classic head helpers are one statement sequence over a head's part set (the {@code L} / {@code C} /
     * {@code R} prefix), transcribed once: the jaw pitches; the sideways chain (yaw at 0.125 / 0.25 / 0.38 / 0.5 down
     * the four neck rings, each ring's pivot 20 / 36 / 36 / 36 units on from the last along its yaw, the head group 36
     * past the fourth); then the pitch chain (pitch at the same fractions, each ring lifted by the sine and its
     * sideways offset foreshortened by the cosine of the previous ring's pitch), and the jaw group 38 units behind the
     * fourth ring along its pitch, 14 below the head, 12 along and 7 across the head's own pitch and yaw (where the
     * King's rides the fourth ring's yaw by 37 - the Prince's own form). The sideways-pass positions are the classic's
     * intermediate part values, re-derived by the pitch pass before any draw - held in locals and written once with
     * the final values; the first ring's pivot is never written (the bind).
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
        float head1YRot = headlr;
        rotateY(processor, head1, head1YRot);
        rotateY(processor, head2, headlr);
        rotateY(processor, head3, headlr);
        rotateY(processor, headMane, headlr);
        rotateY(processor, lEye, headlr);
        rotateY(processor, rEye, headlr);
        rotateY(processor, lNoseSpike, 0.244F + headlr);
        rotateY(processor, rNoseSpike, -0.261F + headlr);
        rotateY(processor, jaw1, headlr);
        rotateY(processor, jaw2, headlr);
        rotateY(processor, jaw3, headlr);
        rotateY(processor, tooth1, headlr);
        rotateY(processor, tooth2, headlr);
        rotateY(processor, tooth3, headlr);
        rotateY(processor, tooth4, headlr);
        // (the head group's z / x copied here are re-derived below before any draw)
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
        float jaw1Z = neck4Z + -38.0F * (float) Math.cos(neck4XRot);
        jaw1Z += (float) Math.sin(head1XRot) * 12.0F;
        float jaw1X = (float) ((double) head1X - Math.sin(head1YRot) * 7.0);
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

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<ThePrinceAdult, ThePrinceAdultGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new ThePrinceAdultGeoReplacement());
        }
    }

    /** The SPEC's {@code locomotion: flyer} (tools/artist_specs/the_prince_adult.json): {@code flying} is {@code !onGround()} (contract section 3; the weights slice: every landed species carries its seed's word, the asset audit pins it). */
    @Override
    public danger.orespawn.entity.client.animation.LocomotionKind locomotion() {
        return danger.orespawn.entity.client.animation.LocomotionKind.FLYER;
    }
}
