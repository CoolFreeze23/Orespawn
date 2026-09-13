package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.PitchBlack;
import danger.orespawn.entity.pose.PitchBlackPose;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Pitch Black (the hooks, owner 2026-09-14, addendum item 10): {@link ModelPitchBlack#poseFrom} verbatim on the
 * converted rig, ON THE HOOK (no keyframe layer, no transcription - the self-gate stays closed until an artist delivers
 * {@code idle} and {@code walk}; the landing slice adds the geo, the wiring and the proofs). Wingspeed 0.65f (orig
 * ModelPitchBlack.java:15,119 / ClientProxyOreSpawn.java:460), every rhythm divided by the entity's size tier
 * ({@code getPitchBlackScale()}, orig :39): the ACTIVITY branch on the wings ({@code getActivity() != 0} beats the
 * three-link wings on a 0.45 cosine x PI x 0.24, else they hang at -0.785 with a 0.05 breath), each link's pivot
 * FOLLOWING the last 21 / 43 units along its roll (POSITION writes through {@link #moveTo}; the membranes and the
 * three wing claws riding their link), the right wing mirrored; the HEAD-LOOK idiom ({@code netHeadYaw % 360} scaled
 * 0.2 flying / 0.55 walking, in radians, on the seven head parts, the five jaws, the twenty-three teeth and the eyes,
 * half of it on the third neck ring); the ATTACKING branch on the jaw ({@code getAttacking() != 0}: a 0.85 ws cosine x
 * PI x 0.16 + 0.5) over the CHOMP LATCH (the Robot2 precedent; orig ModelPitchBlack.java:741,817-830, ENT-S-093): the
 * phase of the 0.7 ws idle rhythm wraps against the per-entity {@code RenderInfo.rf1} and on each wrap {@code ri1} is
 * re-rolled from the entity's RNG ({@code nextInt(20) == 1}; the Kraken convention where orig :820 read the world's), a
 * set bit chomping on the 0.85 ws sine + 0.5, a clear one holding 0.196 rad; the legs by ACTIVITY: walking, the
 * THRESHOLD idiom per leg ({@code (double) limbSwingAmount > 0.001} selects the {@code 0.75 ws / pscale} cosine / sine,
 * the right leg a half turn behind) lifting the seven claws and the lower leg {@code sin x 6 pscale x amount} while the
 * sine is positive and sweeping them {@code 7 + 12 pscale x cos x amount} in z, the thigh and upper leg FOLLOWING the
 * lower leg by 17 units plus half the lift, the fourteen claw pitches reset; flying, the claws tucked at (7, 9) and
 * curled -0.7 rad, swung {@code cos(0.85 ws / pscale) x 0.2} x 30 units while attacking, the legs following at a
 * quarter of the curl; and the tail's yaw chain at 0.76 / 0.25 attacking over 0.26 / 0.08 ({@code / pscale}), the
 * rings FOLLOWING one another by 11 / 9 units with a -1 x nudge on the second, lagging 0.785 rad per ring, forking at
 * the fifth into two 0.174-rad branches with their spikes and points. The entity is read through
 * {@link PitchBlackPose} (ENT-S-093's interface, already on the entity and the model). Every value the classic reads
 * back from a part it just wrote is held in a local; the coordinates the classic never writes (the wing roots' pivots,
 * the wings' z, the claws' and legs' x, tail1's pivot and the tail's y) are read through {@link #classicPosition} (the
 * bind).
 *
 * <p>Scale and shadow follow {@link PitchBlackRenderer}: the entity's own {@code getPitchBlackScale()} applied around
 * {@code super.render} (orig RenderPitchBlack.preRenderScale :39-42; no SCALE constant) and a 1.25 x 1.0 shadow
 * (ENT-S-092). No zero-thickness cube.</p>
 */
public final class PitchBlackGeoReplacement extends OreSpawnGeoReplacement<PitchBlack> {
    /** orig ModelPitchBlack.java:15,119 {@code wingspeed} = 0.65f (ClientProxyOreSpawn.java:460): the chain's frequency multiplier. */
    static final float WINGSPEED = 0.65f;
    private static final GeoReplacementDescriptor<PitchBlack> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.PITCH_BLACK.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            PitchBlack.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/pitchblack.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/pitchblack.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/pitchblack.png"),
            PitchBlackRenderer.SHADOW) {
        @Override
        public void applyScale(PitchBlack entity, PoseStack poseStack, float partialTick) {
            // PitchBlackRenderer.render (orig RenderPitchBlack.preRenderScale :39-42): glScalef(getPitchBlackScale())
            float scale = entity.getPitchBlackScale();
            poseStack.scale(scale, scale, scale);
        }
    };

    private static final String[] L_CLAWS = {"lclaw6", "lclaw7", "lclaw1", "lclaw5", "lclaw4", "lclaw3", "lclaw2"};
    private static final String[] R_CLAWS = {"rclaw6", "rclaw7", "rclaw1", "rclaw5", "rclaw4", "rclaw3", "rclaw2"};

    public PitchBlackGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        PitchBlackPose entity = inputs.subject(PitchBlackPose.class);
        float limbSwingAmount = inputs.limbSwingAmount();
        float ageInTicks = inputs.ageInTicks();
        float netHeadYaw = inputs.netHeadYaw();
        // ModelPitchBlack.poseFrom verbatim, the float / double chain exactly as the classic casts it.
        float newangle = 0.0f;
        float lspeed = 0.0f;
        float tailspeed = 0.76f;
        float tailamp = 0.25f;
        float pi4 = 0.7853982f;
        float pscale = entity.getPitchBlackScale();
        // Per-entity scratch as in the original (orig PitchBlack.java:55, orig ModelPitchBlack.java:741,818-825): each
        // Nightmare keeps its own jaw-chomp latch instead of sharing one field on the model singleton (ENT-S-093).
        RenderInfo r = entity.getRenderInfo();
        newangle = entity.getActivity() != 0 ? Mth.cos((float) (ageInTicks * 0.45f * WINGSPEED / pscale)) * (float) Math.PI * 0.24f : -pi4 + Mth.cos((float) (ageInTicks * 0.05f * WINGSPEED / pscale)) * (float) Math.PI * 0.02f;
        float wing1ZRot = newangle;
        rotateZ(processor, "wing1", wing1ZRot);
        rotateZ(processor, "mem1", newangle);
        float wing2ZRot = newangle * 5.0f / 3.0f;
        rotateZ(processor, "wing2", wing2ZRot);
        float[] wing1 = classicPosition(bone(processor, "wing1"));  // never written: the bind pivot
        float wing2Y = wing1[1] + (float) Math.sin(wing1ZRot) * 21.0f;
        float wing2X = wing1[0] + (float) Math.cos(wing1ZRot) * 21.0f;
        moveXY(processor, "wing2", wing2X, wing2Y);
        rotateZ(processor, "mem2", newangle * 5.0f / 3.0f);
        moveXY(processor, "mem2", wing2X, wing2Y);
        float wing3ZRot = newangle * 2.0f;
        rotateZ(processor, "wing3", wing3ZRot);
        float wing3Y = wing2Y + (float) Math.sin(wing2ZRot) * 43.0f;
        float wing3X = wing2X + (float) Math.cos(wing2ZRot) * 43.0f;
        moveXY(processor, "wing3", wing3X, wing3Y);
        rotateZ(processor, "mem3", newangle * 2.0f);
        moveXY(processor, "mem3", wing3X, wing3Y);
        float wingclaw3ZRot = newangle * 3.0f / 2.0f;   // wingclaw2.zRot = wingclaw3.zRot = ...; wingclaw1.zRot = wingclaw3.zRot
        rotateZ(processor, "wingclaw3", wingclaw3ZRot);
        rotateZ(processor, "wingclaw2", wingclaw3ZRot);
        rotateZ(processor, "wingclaw1", wingclaw3ZRot);
        moveXY(processor, "wingclaw3", wing3X, wing3Y);
        moveXY(processor, "wingclaw2", wing3X, wing3Y);
        moveXY(processor, "wingclaw1", wing3X, wing3Y);
        float rwing1ZRot = -newangle;
        rotateZ(processor, "rwing1", rwing1ZRot);
        rotateZ(processor, "rmem1", -newangle);
        float rwing2ZRot = -newangle * 5.0f / 3.0f;
        rotateZ(processor, "rwing2", rwing2ZRot);
        float[] rwing1 = classicPosition(bone(processor, "rwing1"));  // never written: the bind pivot
        float rwing2Y = rwing1[1] - (float) Math.sin(rwing1ZRot) * 21.0f;
        float rwing2X = rwing1[0] - (float) Math.cos(rwing1ZRot) * 21.0f;
        moveXY(processor, "rwing2", rwing2X, rwing2Y);
        rotateZ(processor, "rmem2", -newangle * 5.0f / 3.0f);
        moveXY(processor, "rmem2", rwing2X, rwing2Y);
        float rwing3ZRot = -newangle * 2.0f;
        rotateZ(processor, "rwing3", rwing3ZRot);
        float rwing3Y = rwing2Y - (float) Math.sin(rwing2ZRot) * 43.0f;
        float rwing3X = rwing2X - (float) Math.cos(rwing2ZRot) * 43.0f;
        moveXY(processor, "rwing3", rwing3X, rwing3Y);
        rotateZ(processor, "rmem3", -newangle * 2.0f);
        moveXY(processor, "rmem3", rwing3X, rwing3Y);
        float rwingclaw3ZRot = -newangle * 3.0f / 2.0f;
        rotateZ(processor, "rwingclaw3", rwingclaw3ZRot);
        rotateZ(processor, "rwingclaw2", rwingclaw3ZRot);
        rotateZ(processor, "rwingclaw1", rwingclaw3ZRot);
        moveXY(processor, "rwingclaw3", rwing3X, rwing3Y);
        moveXY(processor, "rwingclaw2", rwing3X, rwing3Y);
        moveXY(processor, "rwingclaw1", rwing3X, rwing3Y);
        netHeadYaw %= 360.0f;
        netHeadYaw = entity.getActivity() != 0 ? (netHeadYaw *= 0.2f) : (netHeadYaw *= 0.55f);
        rotateY(processor, "neck3", (float) Math.toRadians(netHeadYaw) * 0.5f);
        float head1YRot = (float) Math.toRadians(netHeadYaw);   // head3.yRot = head4.yRot = ...; head2 / head6 / head7 / head1 / head5 copy it
        rotateY(processor, "head4", head1YRot);
        rotateY(processor, "head3", head1YRot);
        rotateY(processor, "head2", head1YRot);
        rotateY(processor, "head1", head1YRot);
        rotateY(processor, "head7", head1YRot);
        rotateY(processor, "head6", head1YRot);
        rotateY(processor, "head5", head1YRot);
        rotateY(processor, "jaw5", head1YRot);     // jaw4.yRot = jaw5.yRot = head1.yRot; jaw3 / jaw2 / jaw1 copy it
        rotateY(processor, "jaw4", head1YRot);
        rotateY(processor, "jaw3", head1YRot);
        rotateY(processor, "jaw2", head1YRot);
        rotateY(processor, "jaw1", head1YRot);
        rotateY(processor, "tooth5", head1YRot);   // tooth4.yRot = tooth5.yRot = head1.yRot; tooth3 / 2 / 1 copy it
        rotateY(processor, "tooth4", head1YRot);
        rotateY(processor, "tooth3", head1YRot);
        rotateY(processor, "tooth2", head1YRot);
        rotateY(processor, "tooth1", head1YRot);
        rotateY(processor, "tooth10", head1YRot);  // tooth9.yRot = tooth10.yRot = head1.yRot; tooth8 / 7 / 6 copy it
        rotateY(processor, "tooth9", head1YRot);
        rotateY(processor, "tooth8", head1YRot);
        rotateY(processor, "tooth7", head1YRot);
        rotateY(processor, "tooth6", head1YRot);
        rotateY(processor, "tooth15", head1YRot);  // tooth14.yRot = tooth15.yRot = head1.yRot; tooth13 / 12 / 11 copy it
        rotateY(processor, "tooth14", head1YRot);
        rotateY(processor, "tooth13", head1YRot);
        rotateY(processor, "tooth12", head1YRot);
        rotateY(processor, "tooth11", head1YRot);
        rotateY(processor, "tooth20", head1YRot);  // tooth19.yRot = tooth20.yRot = head1.yRot; tooth18 / 17 / 16 copy it
        rotateY(processor, "tooth19", head1YRot);
        rotateY(processor, "tooth18", head1YRot);
        rotateY(processor, "tooth17", head1YRot);
        rotateY(processor, "tooth16", head1YRot);
        rotateY(processor, "tooth23", head1YRot);  // tooth22.yRot = tooth23.yRot = head1.yRot; tooth21 copies it
        rotateY(processor, "tooth22", head1YRot);
        rotateY(processor, "tooth21", head1YRot);
        rotateY(processor, "leye", head1YRot);     // reye.yRot = leye.yRot = head1.yRot
        rotateY(processor, "reye", head1YRot);
        if (entity.getAttacking() != 0) {
            newangle = Mth.cos((float) (ageInTicks * 0.85f * WINGSPEED)) * (float) Math.PI * 0.16f;
            newangle += 0.5f;
        } else {
            // orig ModelPitchBlack.java:817-830 (ENT-S-093): latch on the entity's own RenderInfo; RNG stays
            // entity.getRandom() per the Kraken convention (orig :820 reads world RNG).
            newangle = ageInTicks * 0.7f * WINGSPEED % ((float) Math.PI * 2);
            if ((newangle = Math.abs(newangle)) < r.rf1) {
                r.ri1 = 0;
                if (entity.getRandom().nextInt(20) == 1) {
                    r.ri1 |= 1;
                }
            }
            r.rf1 = newangle;
            if (r.ri1 != 0) {
                newangle = Mth.sin((float) (ageInTicks * 0.85f * WINGSPEED)) * (float) Math.PI * 0.16f;
                newangle += 0.5f;
            } else {
                newangle = pi4 / 4.0f;
            }
        }
        rotateX(processor, "jaw5", newangle);      // jaw4.xRot = jaw5.xRot = newangle; jaw3 / jaw2 / jaw1 copy it
        rotateX(processor, "jaw4", newangle);
        rotateX(processor, "jaw3", newangle);
        rotateX(processor, "jaw2", newangle);
        rotateX(processor, "jaw1", newangle);
        rotateX(processor, "tooth15", newangle);   // tooth14.xRot = tooth15.xRot = newangle
        rotateX(processor, "tooth14", newangle);
        rotateX(processor, "tooth20", newangle);   // tooth19.xRot = tooth20.xRot = newangle; tooth18 / 17 / 16 copy it
        rotateX(processor, "tooth19", newangle);
        rotateX(processor, "tooth18", newangle);
        rotateX(processor, "tooth17", newangle);
        rotateX(processor, "tooth16", newangle);
        rotateX(processor, "tooth23", newangle);   // tooth22.xRot = tooth23.xRot = newangle; tooth21 copies it
        rotateX(processor, "tooth22", newangle);
        rotateX(processor, "tooth21", newangle);
        float clawZ = 7.0f;
        float clawY = 21.0f;
        float clawZamp = 12.0f * pscale;
        float clawYamp = 6.0f * pscale;
        if (entity.getActivity() == 0) {
            float t1 = 0.0f;
            float t2 = 0.0f;
            if ((double) limbSwingAmount > 0.001) {
                newangle = Mth.cos((float) (ageInTicks * 0.75f * WINGSPEED / pscale));
                t1 = Mth.sin((float) (ageInTicks * 0.75f * WINGSPEED / pscale));
            } else {
                newangle = 0.0f;
                t1 = 0.0f;
                t2 = 0.0f;
            }
            float lclaw1Y;
            if (t1 > 0.0f) {
                t2 = t1 * clawYamp * limbSwingAmount;
                lclaw1Y = clawY - t2;
            } else {
                lclaw1Y = clawY;
            }
            float lclaw1Z = clawZ + clawZamp * newangle * limbSwingAmount;
            // lclaw6.z = lclaw7.z = (lclaw1.z = ...); lclaw5..2.z = lclaw7.z; the same for y (x never written: the bind)
            for (String claw : L_CLAWS) {
                moveYZ(processor, claw, lclaw1Y, lclaw1Z);
            }
            float leftleg3Z = lclaw1Z;   // llegspike.z = leftleg3.z = lclaw1.z
            float leftleg3Y = lclaw1Y;   // llegspike.y = leftleg3.y = lclaw1.y
            moveYZ(processor, "leftleg3", leftleg3Y, leftleg3Z);
            moveYZ(processor, "llegspike", leftleg3Y, leftleg3Z);
            float leftleg3XRot = -0.61f + newangle * (float) Math.PI * 0.18f * limbSwingAmount;
            rotateX(processor, "leftleg3", leftleg3XRot);
            rotateX(processor, "llegspike", -0.785f + newangle * (float) Math.PI * 0.18f * limbSwingAmount);
            rotateX(processor, "leftleg1", -0.576f + newangle * (float) Math.PI * 0.18f * limbSwingAmount);
            rotateX(processor, "leftleg2", 0.977f + newangle * (float) Math.PI * 0.18f * limbSwingAmount);
            float leftleg1Y = leftleg3Y - (float) Math.cos(leftleg3XRot) * 17.0f + t2 / 2.0f;   // leftleg1.y = leftleg2.y = ...
            float leftleg1Z = leftleg3Z - (float) Math.sin(leftleg3XRot) * 17.0f;              // leftleg1.z = leftleg2.z = ...
            moveYZ(processor, "leftleg2", leftleg1Y, leftleg1Z);
            moveYZ(processor, "leftleg1", leftleg1Y, leftleg1Z);
            t1 = 0.0f;
            t2 = 0.0f;
            if ((double) limbSwingAmount > 0.001) {
                newangle = Mth.cos((float) (ageInTicks * 0.75f * WINGSPEED / pscale + pi4 * 4.0f));
                t1 = Mth.sin((float) (ageInTicks * 0.75f * WINGSPEED / pscale + pi4 * 4.0f));
            } else {
                newangle = 0.0f;
                t1 = 0.0f;
                t2 = 0.0f;
            }
            float rclaw1Y;
            if (t1 > 0.0f) {
                t2 = t1 * clawYamp * limbSwingAmount;
                rclaw1Y = clawY - t2;
            } else {
                rclaw1Y = clawY;
            }
            float rclaw1Z = clawZ + clawZamp * newangle * limbSwingAmount;
            for (String claw : R_CLAWS) {
                moveYZ(processor, claw, rclaw1Y, rclaw1Z);
            }
            float rightleg3Z = rclaw1Z;   // llegspike2.z = rightleg3.z = rclaw1.z
            float rightleg3Y = rclaw1Y;   // llegspike2.y = rightleg3.y = rclaw1.y
            moveYZ(processor, "rightleg3", rightleg3Y, rightleg3Z);
            moveYZ(processor, "llegspike2", rightleg3Y, rightleg3Z);
            float rightleg3XRot = -0.61f + newangle * (float) Math.PI * 0.18f * limbSwingAmount;
            rotateX(processor, "rightleg3", rightleg3XRot);
            rotateX(processor, "llegspike2", -0.785f + newangle * (float) Math.PI * 0.18f * limbSwingAmount);
            rotateX(processor, "rightleg1", -0.576f + newangle * (float) Math.PI * 0.18f * limbSwingAmount);
            rotateX(processor, "rightleg2", 0.977f + newangle * (float) Math.PI * 0.18f * limbSwingAmount);
            float rightleg1Y = rightleg3Y - (float) Math.cos(rightleg3XRot) * 17.0f + t2 / 2.0f;
            float rightleg1Z = rightleg3Z - (float) Math.sin(rightleg3XRot) * 17.0f;
            moveYZ(processor, "rightleg2", rightleg1Y, rightleg1Z);
            moveYZ(processor, "rightleg1", rightleg1Y, rightleg1Z);
            rotateX(processor, "lclaw1", 0.0f);
            rotateX(processor, "lclaw7", 0.0f);
            rotateX(processor, "lclaw6", 0.0f);
            rotateX(processor, "lclaw5", 0.0f);
            rotateX(processor, "lclaw4", 0.0f);
            rotateX(processor, "lclaw3", 0.0f);
            rotateX(processor, "lclaw2", 0.0f);
            rotateX(processor, "rclaw1", 0.0f);
            rotateX(processor, "rclaw7", 0.0f);
            rotateX(processor, "rclaw6", 0.0f);
            rotateX(processor, "rclaw5", 0.0f);
            rotateX(processor, "rclaw4", 0.0f);
            rotateX(processor, "rclaw3", 0.0f);
            rotateX(processor, "rclaw2", 0.0f);
        } else {
            clawZ = 7.0f;
            clawY = 9.0f;
            newangle = entity.getAttacking() != 0 ? Mth.cos((float) (ageInTicks * 0.85f * WINGSPEED / pscale)) * 0.2f : 0.0f;
            float lclaw1Z = clawZ;
            float lclaw1Y = clawY + newangle * 30.0f;
            float lclaw1XRot = -0.7f + newangle;
            // lclaw6.z = lclaw7.z = lclaw1.z; lclaw5..2.z = lclaw7.z; the same for y and xRot
            for (String claw : L_CLAWS) {
                moveYZ(processor, claw, lclaw1Y, lclaw1Z);
                rotateX(processor, claw, lclaw1XRot);
            }
            float leftleg3Z = lclaw1Z;   // llegspike.z = leftleg3.z = lclaw1.z
            float leftleg3Y = lclaw1Y;   // llegspike.y = leftleg3.y = lclaw1.y
            moveYZ(processor, "leftleg3", leftleg3Y, leftleg3Z);
            moveYZ(processor, "llegspike", leftleg3Y, leftleg3Z);
            float leftleg3XRot = -0.61f + lclaw1XRot;
            rotateX(processor, "leftleg3", leftleg3XRot);
            rotateX(processor, "llegspike", -0.785f + lclaw1XRot);
            rotateX(processor, "leftleg1", -0.576f - lclaw1XRot / 4.0f);
            rotateX(processor, "leftleg2", 0.977f - lclaw1XRot / 4.0f);
            float leftleg1Y = leftleg3Y - (float) Math.cos(leftleg3XRot) * 17.0f;   // leftleg1.y = leftleg2.y = ...
            float leftleg1Z = leftleg3Z - (float) Math.sin(leftleg3XRot) * 17.0f;   // leftleg1.z = leftleg2.z = ...
            moveYZ(processor, "leftleg2", leftleg1Y, leftleg1Z);
            moveYZ(processor, "leftleg1", leftleg1Y, leftleg1Z);
            float rclaw1Z = clawZ;
            float rclaw1Y = clawY - newangle * 30.0f;
            float rclaw1XRot = -0.7f - newangle;
            for (String claw : R_CLAWS) {
                moveYZ(processor, claw, rclaw1Y, rclaw1Z);
                rotateX(processor, claw, rclaw1XRot);
            }
            float rightleg3Z = rclaw1Z;   // llegspike2.z = rightleg3.z = rclaw1.z
            float rightleg3Y = rclaw1Y;   // llegspike2.y = rightleg3.y = rclaw1.y
            moveYZ(processor, "rightleg3", rightleg3Y, rightleg3Z);
            moveYZ(processor, "llegspike2", rightleg3Y, rightleg3Z);
            float rightleg3XRot = -0.61f + rclaw1XRot;
            rotateX(processor, "rightleg3", rightleg3XRot);
            rotateX(processor, "llegspike2", -0.785f + rclaw1XRot);
            rotateX(processor, "rightleg1", -0.576f - rclaw1XRot / 4.0f);
            rotateX(processor, "rightleg2", 0.977f - rclaw1XRot / 4.0f);
            float rightleg1Y = rightleg3Y - (float) Math.cos(rightleg3XRot) * 17.0f;
            float rightleg1Z = rightleg3Z - (float) Math.sin(rightleg3XRot) * 17.0f;
            moveYZ(processor, "rightleg2", rightleg1Y, rightleg1Z);
            moveYZ(processor, "rightleg1", rightleg1Y, rightleg1Z);
        }
        if (entity.getAttacking() != 0) {
            tailspeed = 0.76f / pscale;
            tailamp = 0.25f;
        } else {
            tailspeed = 0.26f / pscale;
            tailamp = 0.08f;
        }
        float tail1YRot = Mth.cos((float) (ageInTicks * tailspeed * WINGSPEED)) * (float) Math.PI * tailamp / 2.0f;
        rotateY(processor, "tail1", tail1YRot);
        float[] tail1 = classicPosition(bone(processor, "tail1"));  // never written: the bind pivot
        float tail2Z = tail1[2] + (float) Math.cos(tail1YRot) * 11.0f;
        float tail2X = tail1[0] - 1.0f + (float) Math.sin(tail1YRot) * 11.0f;
        moveXZ(processor, "tail2", tail2X, tail2Z);
        float tail2YRot = Mth.cos((float) (ageInTicks * tailspeed * WINGSPEED - pi4)) * (float) Math.PI * tailamp;
        rotateY(processor, "tail2", tail2YRot);
        float tail3Z = tail2Z + (float) Math.cos(tail2YRot) * 9.0f;
        float tail3X = tail2X + (float) Math.sin(tail2YRot) * 9.0f;
        moveXZ(processor, "tail3", tail3X, tail3Z);
        float tail3YRot = Mth.cos((float) (ageInTicks * tailspeed * WINGSPEED - 2.0f * pi4)) * (float) Math.PI * tailamp;
        rotateY(processor, "tail3", tail3YRot);
        float tail4Z = tail3Z + (float) Math.cos(tail3YRot) * 9.0f;
        float tail4X = tail3X + (float) Math.sin(tail3YRot) * 9.0f;
        moveXZ(processor, "tail4", tail4X, tail4Z);
        float tail4YRot = Mth.cos((float) (ageInTicks * tailspeed * WINGSPEED - 3.0f * pi4)) * (float) Math.PI * tailamp;
        rotateY(processor, "tail4", tail4YRot);
        float tail5Z = tail4Z + (float) Math.cos(tail4YRot) * 9.0f;
        float tail5X = tail4X + (float) Math.sin(tail4YRot) * 9.0f;
        moveXZ(processor, "tail5", tail5X, tail5Z);
        newangle = Mth.cos((float) (ageInTicks * tailspeed * WINGSPEED - 3.0f * pi4)) * (float) Math.PI * tailamp;
        float tail5YRot = tail4YRot + (newangle /= 2.0f);
        rotateY(processor, "tail5", tail5YRot);
        float tail6Z = tail5Z + (float) Math.cos(tail5YRot) * 9.0f;
        float tail6X = tail5X + (float) Math.sin(tail5YRot) * 9.0f;
        moveXZ(processor, "tail6", tail6X, tail6Z);
        float tail6YRot = 0.174f + tail5YRot + newangle;
        rotateY(processor, "tail6", tail6YRot);
        moveXZ(processor, "tailspike3", tail6X, tail6Z);   // tailspike2.z = tailspike3.z = tail6.z; x the same
        moveXZ(processor, "tailspike2", tail6X, tail6Z);
        rotateY(processor, "tailspike3", tail6YRot);       // tailspike2.yRot = tailspike3.yRot = tail6.yRot
        rotateY(processor, "tailspike2", tail6YRot);
        float tail9Z = tail6Z + (float) Math.cos(tail6YRot) * 9.0f;
        float tail9X = tail6X + (float) Math.sin(tail6YRot) * 9.0f;
        moveXZ(processor, "tail9", tail9X, tail9Z);
        float tail9YRot = tail6YRot + newangle;
        rotateY(processor, "tail9", tail9YRot);
        moveXZ(processor, "tailspike1", tail9X, tail9Z);
        rotateY(processor, "tailspike1", tail9YRot);
        float tailpoint1Z = tail9Z + (float) Math.cos(tail9YRot) * 9.0f;
        float tailpoint1X = tail9X + (float) Math.sin(tail9YRot) * 9.0f;
        moveXZ(processor, "tailpoint1", tailpoint1X, tailpoint1Z);
        rotateY(processor, "tailpoint1", tail9YRot + newangle);
        float tail7Z = tail5Z + (float) Math.cos(tail5YRot) * 9.0f;
        float tail7X = tail5X + (float) Math.sin(tail5YRot) * 9.0f;
        moveXZ(processor, "tail7", tail7X, tail7Z);
        float tail7YRot = -0.174f + tail5YRot + newangle;
        rotateY(processor, "tail7", tail7YRot);
        moveXZ(processor, "tailspike6", tail7X, tail7Z);   // tailspike5.z = tailspike6.z = tail7.z; x the same
        moveXZ(processor, "tailspike5", tail7X, tail7Z);
        rotateY(processor, "tailspike6", tail7YRot);       // tailspike5.yRot = tailspike6.yRot = tail7.yRot
        rotateY(processor, "tailspike5", tail7YRot);
        float tail8Z = tail7Z + (float) Math.cos(tail7YRot) * 9.0f;
        float tail8X = tail7X + (float) Math.sin(tail7YRot) * 9.0f;
        moveXZ(processor, "tail8", tail8X, tail8Z);
        float tail8YRot = tail7YRot + newangle;
        rotateY(processor, "tail8", tail8YRot);
        moveXZ(processor, "tailspike4", tail8X, tail8Z);
        rotateY(processor, "tailspike4", tail8YRot);
        float tailpoint2Z = tail8Z + (float) Math.cos(tail8YRot) * 9.0f;
        float tailpoint2X = tail8X + (float) Math.sin(tail8YRot) * 9.0f;
        moveXZ(processor, "tailpoint2", tailpoint2X, tailpoint2Z);
        rotateY(processor, "tailpoint2", tail8YRot + newangle);
    }

    /** {@code part.y = y; part.z = z} on a part whose x the classic never writes (the bind). */
    private static void moveYZ(AnimationProcessor<?> processor, String name, float y, float z) {
        float[] position = classicPosition(bone(processor, name));
        moveTo(processor, name, position[0], y, z);
    }

    /** {@code part.x = x; part.y = y} on a part whose z the classic never writes (the bind). */
    private static void moveXY(AnimationProcessor<?> processor, String name, float x, float y) {
        float[] position = classicPosition(bone(processor, name));
        moveTo(processor, name, x, y, position[2]);
    }

    /** {@code part.x = x; part.z = z} on a part whose y the classic never writes (the bind). */
    private static void moveXZ(AnimationProcessor<?> processor, String name, float x, float z) {
        float[] position = classicPosition(bone(processor, name));
        moveTo(processor, name, x, position[1], z);
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<PitchBlack, PitchBlackGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new PitchBlackGeoReplacement());
        }
    }
}
