package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.ThePrinceTeen;
import danger.orespawn.entity.pose.ThePrinceTeenPose;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib The Prince (teen) (the hooks, owner 2026-09-14, addendum item 10; landed by the second Tier-1 slice T1b,
 * 2026-09-15, the owner's closing set item 4): {@link ModelThePrinceTeen#poseFrom} verbatim on the converted rig, ON THE
 * HOOK (no keyframe layer, no transcription - the self-gate stays closed until an artist delivers {@code idle} and
 * {@code walk}; the geo, the wiring and the proofs landed with T1b). Wingspeed 0.65f (orig ModelThePrinceTeen.java:16,90
 * / ClientProxyOreSpawn.java:503): the wings by ACTIVITY and ATTACKING (a 1.3 ws x PI x 0.2 x amount walking beat above
 * a tenth, else a 0.3 ws x 0.04 breath; flying a 1.4 ws x 0.4 beat; attacking 1.7 ws x 0.4) on the four links' roll
 * (x1.25 on the second, around -0.4 / -0.6 / -0.2), the second link's pivot FOLLOWING the first 22 units along its roll
 * (POSITION writes through {@link #moveTo}), the four membranes copying their link, the right wing mirrored; the legs by
 * THRESHOLD, ATTACKING and ACTIVITY ({@code (double) limbSwingAmount > 0.1}: the 0.55 ws x PI x 0.25 x amount gait a
 * quarter turn apart on the lower leg; attacking the 1.0 ws x 0.25 swing; flying the legs tucked -0.5 / -1.25 (mirrored)
 * and the ten claws curled -0.685), the lower leg FOLLOWING the upper by 14 units (+6 / +5) and the five claws of each
 * foot FOLLOWING the lower leg by 17 units (-1 in z); the tail's yaw chain at 0.56 / 0.19 attacking over 0.26 / 0.08,
 * stilled by the SIT order ({@code isOrderedToSit()}), lagging pi / 4 per ring and FOLLOWING by 11 / 9 / 9 / 9 units,
 * the three tail spikes riding the fifth ring by 9 / 15 / 11; the FLIGHT YAW LATCH (the Rotator / Leon precedent; orig
 * :669-678): while flying the head yaw is the body-yaw delta {@code (yRotO - getYRot()) x -10} eased into the per-entity
 * {@code RenderInfo.rf1} by a fiftieth per frame and clamped to +-50; the three-head LOOK split (two thirds of that yaw
 * / the head pitch, the side heads at half of it on the side the head turns to) on each head's two head parts, fin and
 * two jaws, an eighth / quarter / half of it on its three neck rings, the fins pitched +0.5; the three jaws' chatter
 * (attacking 0.9 / 1.1 / 1.3 ws x PI x 0.1 + 0.25, else 0.25 / 0.3 / 0.35 ws x 0.02 + 0.1); the three necks pitched by
 * the entity's HEAD EXTENSIONS ({@code getHead1Ext / 2 / 3}, degrees, a third / two thirds / all of it negated down the
 * rings) and each ring, then the head group, FOLLOWING the last 9 units along its pitch and yaw. The entity is read
 * through {@link ThePrinceTeenPose} (the Slice 4b doctrine). Every value the classic reads back from a part it just
 * wrote is held in a local; the wing roots', the upper legs', the first tail ring's and the first neck rings' pivots,
 * never written, are read through {@link #classicPosition} (the bind).
 *
 * <p>Scale and shadow follow {@link ThePrinceTeenRenderer}: 1.25 render scale and a 1.0 x 1.25 shadow (ENT-S-092). No
 * zero-thickness cube.</p>
 */
public final class ThePrinceTeenGeoReplacement extends OreSpawnGeoReplacement<ThePrinceTeen> {
    /** orig ModelThePrinceTeen.java:16,90 {@code wingspeed} = 0.65f (ClientProxyOreSpawn.java:503): the chain's frequency multiplier. */
    static final float WINGSPEED = 0.65f;
    private static final GeoReplacementDescriptor<ThePrinceTeen> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.THE_PRINCE_TEEN.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            ThePrinceTeen.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/theprinceteen.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/theprinceteen.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/theprince_teen.png"),
            ThePrinceTeenRenderer.SHADOW) {
        @Override
        public void applyScale(ThePrinceTeen entity, PoseStack poseStack, float partialTick) {
            // orig RenderThePrinceTeen.preRenderCallback: GL11.glScalef(scale, scale, scale) (ThePrinceTeenRenderer.scale)
            poseStack.scale(ThePrinceTeenRenderer.SCALE, ThePrinceTeenRenderer.SCALE, ThePrinceTeenRenderer.SCALE);
        }
    };

    private static final String[] L_CLAWS = {"lclaw2", "lclaw4", "lclaw5", "lclaw6", "lclaw7"};
    private static final String[] R_CLAWS = {"rclaw2", "rclaw4", "rclaw5", "rclaw6", "rclaw7"};

    public ThePrinceTeenGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        ThePrinceTeenPose entity = inputs.subject(ThePrinceTeenPose.class);
        float limbSwingAmount = inputs.limbSwingAmount();
        float ageInTicks = inputs.ageInTicks();
        float netHeadYaw = inputs.netHeadYaw();
        float headPitch = inputs.headPitch();
        // ModelThePrinceTeen.poseFrom verbatim.
        float d3;
        float h3;
        float newangle;
        float newangle2;
        float rnewangle;
        float rnewangle2;
        float clawangle;
        float tailspeed = 0.26f;
        float tailamp = 0.08f;
        float pi4 = 0.7853982f;
        int current_activity = entity.getActivity();
        // Per-entity scratch as in the original (orig ThePrinceTeen.java:80, orig ModelThePrinceTeen.java:525/538): each
        // teen keeps its own flight head-yaw latch instead of a per-frame local. ENT-S-093.
        RenderInfo r = entity.getRenderInfo();

        newangle = (double) limbSwingAmount > 0.1 && current_activity == 0
            ? Mth.cos(ageInTicks * 1.3f * WINGSPEED) * (float) Math.PI * 0.2f * limbSwingAmount
            : Mth.cos(ageInTicks * 0.3f * WINGSPEED) * (float) Math.PI * 0.04f;
        if (current_activity == 1) {
            newangle = Mth.cos(ageInTicks * 1.4f * WINGSPEED) * (float) Math.PI * 0.4f;
        }
        if (entity.getAttacking() != 0) {
            newangle = Mth.cos(ageInTicks * 1.7f * WINGSPEED) * (float) Math.PI * 0.4f;
        }

        float wing1ZRot = newangle - 0.4f;
        rotateZ(processor, "wing1", wing1ZRot);
        float wing2ZRot = newangle * 1.25f - 0.4f;
        rotateZ(processor, "wing2", wing2ZRot);
        float wing3ZRot = newangle - 0.6f;
        rotateZ(processor, "wing3", wing3ZRot);
        float wing4ZRot = newangle - 0.2f;
        rotateZ(processor, "wing4", wing4ZRot);
        float[] wing1 = classicPosition(bone(processor, "wing1"));  // never written: the bind pivot
        float wing2Y = wing1[1] + (float) Math.sin(wing1ZRot) * 22.0f;
        float wing2X = wing1[0] + (float) Math.cos(wing1ZRot) * 22.0f;
        moveXY(processor, "wing2", wing2X, wing2Y);
        rotateZ(processor, "mem1", wing1ZRot);
        rotateZ(processor, "mem2", wing2ZRot);
        rotateZ(processor, "mem3", wing3ZRot);
        rotateZ(processor, "mem4", wing4ZRot);
        moveXY(processor, "mem2", wing2X, wing2Y);

        float rwing1ZRot = -newangle + 0.4f;
        rotateZ(processor, "rwing1", rwing1ZRot);
        float rwing2ZRot = -newangle * 1.25f + 0.4f;
        rotateZ(processor, "rwing2", rwing2ZRot);
        float rwing3ZRot = -newangle + 0.6f;
        rotateZ(processor, "rwing3", rwing3ZRot);
        float rwing4ZRot = -newangle + 0.2f;
        rotateZ(processor, "rwing4", rwing4ZRot);
        float[] rwing1 = classicPosition(bone(processor, "rwing1"));  // never written: the bind pivot
        float rwing2Y = rwing1[1] - (float) Math.sin(rwing1ZRot) * 22.0f;
        float rwing2X = rwing1[0] - (float) Math.cos(rwing1ZRot) * 22.0f;
        moveXY(processor, "rwing2", rwing2X, rwing2Y);
        rotateZ(processor, "rmem1", rwing1ZRot);
        rotateZ(processor, "rmem2", rwing2ZRot);
        rotateZ(processor, "rmem3", rwing3ZRot);
        rotateZ(processor, "rmem4", rwing4ZRot);
        moveXY(processor, "rmem2", rwing2X, rwing2Y);

        if ((double) limbSwingAmount > 0.1) {
            newangle = Mth.cos(ageInTicks * 0.55f * WINGSPEED) * (float) Math.PI * 0.25f * limbSwingAmount;
            newangle2 = Mth.cos((float) ((double) (ageInTicks * 0.55f * WINGSPEED) + 1.5707963267948966)) * (float) Math.PI * 0.25f * limbSwingAmount;
            rnewangle = newangle;
            rnewangle2 = newangle2;
            clawangle = 0.0f;
        } else {
            newangle = 0.0f;
            newangle2 = 0.0f;
            rnewangle = 0.0f;
            rnewangle2 = 0.0f;
            clawangle = 0.0f;
        }
        if (entity.getAttacking() != 0) {
            newangle = Mth.cos(ageInTicks * WINGSPEED) * (float) Math.PI * 0.25f;
            newangle2 = Mth.cos((float) ((double) (ageInTicks * WINGSPEED) + 1.5707963267948966)) * (float) Math.PI * 0.25f;
            rnewangle = newangle;
            rnewangle2 = newangle2;
            clawangle = 0.0f;
        }
        if (current_activity == 1 && entity.getAttacking() == 0) {
            newangle = -0.5f;
            newangle2 = -1.25f;
            rnewangle = 0.5f;
            rnewangle2 = 1.25f;
        }
        if (current_activity == 1) {
            clawangle = -0.685f;
        }

        rotateX(processor, "leftleg1", newangle - 0.575f);
        float leftleg2XRot = newangle + 0.977f;
        rotateX(processor, "leftleg2", leftleg2XRot);
        float leftleg3XRot = newangle2 - 0.523f;
        rotateX(processor, "leftleg3", leftleg3XRot);
        float[] leftleg2 = classicPosition(bone(processor, "leftleg2"));  // never written: the bind pivot
        float leftleg3Y = leftleg2[1] + (float) Math.cos(leftleg2XRot) * 14.0f + 6.0f;
        float leftleg3Z = leftleg2[2] + (float) Math.sin(leftleg2XRot) * 14.0f;
        moveYZ(processor, "leftleg3", leftleg3Y, leftleg3Z);
        float lclaw2Y = leftleg3Y + (float) Math.cos(leftleg3XRot) * 17.0f;
        float lclaw2Z = leftleg3Z + (float) Math.sin(leftleg3XRot) * 17.0f - 1.0f;
        // lclaw2.y / z, then lclaw4..7 copying them (x never written: the bind)
        for (String claw : L_CLAWS) {
            moveYZ(processor, claw, lclaw2Y, lclaw2Z);
        }
        for (String claw : L_CLAWS) {
            rotateX(processor, claw, clawangle);
        }

        rotateX(processor, "rightleg1", -rnewangle - 0.575f);
        float rightleg2XRot = -rnewangle + 0.977f;
        rotateX(processor, "rightleg2", rightleg2XRot);
        float rightleg3XRot = -rnewangle2 - 0.523f;
        rotateX(processor, "rightleg3", rightleg3XRot);
        float[] rightleg2 = classicPosition(bone(processor, "rightleg2"));  // never written: the bind pivot
        float rightleg3Y = rightleg2[1] + (float) Math.cos(rightleg2XRot) * 14.0f + 5.0f;
        float rightleg3Z = rightleg2[2] + (float) Math.sin(rightleg2XRot) * 14.0f;
        moveYZ(processor, "rightleg3", rightleg3Y, rightleg3Z);
        float rclaw2Y = rightleg3Y + (float) Math.cos(rightleg3XRot) * 17.0f;
        float rclaw2Z = rightleg3Z + (float) Math.sin(rightleg3XRot) * 17.0f - 1.0f;
        for (String claw : R_CLAWS) {
            moveYZ(processor, claw, rclaw2Y, rclaw2Z);
        }
        for (String claw : R_CLAWS) {
            rotateX(processor, claw, clawangle);
        }

        if (entity.getAttacking() != 0) {
            tailspeed = 0.56f;
            tailamp = 0.19f;
        }
        if (entity.isOrderedToSit()) {
            tailamp = 0.0f;
        }

        float tail1YRot = Mth.cos(ageInTicks * tailspeed * WINGSPEED) * (float) Math.PI * tailamp / 4.0f;
        rotateY(processor, "tail1", tail1YRot);
        float[] tail1 = classicPosition(bone(processor, "tail1"));  // never written: the bind pivot
        float tail2Z = tail1[2] + (float) Math.cos(tail1YRot) * 11.0f;
        float tail2X = tail1[0] + (float) Math.sin(tail1YRot) * 11.0f;
        moveXZ(processor, "tail2", tail2X, tail2Z);
        float tail2YRot = Mth.cos(ageInTicks * tailspeed * WINGSPEED - pi4) * (float) Math.PI * tailamp;
        rotateY(processor, "tail2", tail2YRot);
        float tail3Z = tail2Z + (float) Math.cos(tail2YRot) * 9.0f;
        float tail3X = tail2X + (float) Math.sin(tail2YRot) * 9.0f;
        moveXZ(processor, "tail3", tail3X, tail3Z);
        float tail3YRot = Mth.cos(ageInTicks * tailspeed * WINGSPEED - 2.0f * pi4) * (float) Math.PI * tailamp;
        rotateY(processor, "tail3", tail3YRot);
        float tail4Z = tail3Z + (float) Math.cos(tail3YRot) * 9.0f;
        float tail4X = tail3X + (float) Math.sin(tail3YRot) * 9.0f;
        moveXZ(processor, "tail4", tail4X, tail4Z);
        float tail4YRot = Mth.cos(ageInTicks * tailspeed * WINGSPEED - 3.0f * pi4) * (float) Math.PI * tailamp;
        rotateY(processor, "tail4", tail4YRot);
        newangle = Mth.cos(ageInTicks * tailspeed * WINGSPEED - 3.0f * pi4) * (float) Math.PI * tailamp;
        float tail5Z = tail4Z + (float) Math.cos(tail4YRot) * 9.0f;
        float tail5X = tail4X + (float) Math.sin(tail4YRot) * 9.0f;
        moveXZ(processor, "tail5", tail5X, tail5Z);
        float tail5YRot = tail4YRot + (newangle /= 2.0f);
        rotateY(processor, "tail5", tail5YRot);
        float tailspike1Z = tail5Z + (float) Math.cos(tail5YRot) * 9.0f;
        float tailspike1X = tail5X + (float) Math.sin(tail5YRot) * 9.0f;
        moveXZ(processor, "Tailspike1", tailspike1X, tailspike1Z);
        float tailspike2Z = tail5Z + (float) Math.cos(tail5YRot) * 15.0f;
        float tailspike2X = tail5X + (float) Math.sin(tail5YRot) * 15.0f;
        moveXZ(processor, "Tailspike2", tailspike2X, tailspike2Z);
        float tailspike1YRot = tail5YRot + newangle * 2.0f / 3.0f;   // Tailspike1.yRot = Tailspike2.yRot = ...
        rotateY(processor, "Tailspike2", tailspike1YRot);
        rotateY(processor, "Tailspike1", tailspike1YRot);
        float tailspike3Z = tailspike1Z + (float) Math.cos(tailspike1YRot) * 11.0f;
        float tailspike3X = tailspike1X + (float) Math.sin(tailspike1YRot) * 11.0f;
        moveXZ(processor, "Tailspike3", tailspike3X, tailspike3Z);
        rotateY(processor, "Tailspike3", tailspike1YRot + newangle * 3.0f / 2.0f);

        float yaw = netHeadYaw;
        if (entity.getActivity() == 1) {
            // ENT-S-093: body-yaw delta as the original (orig ModelThePrinceTeen.java:669 field_70126_B - field_70177_z
            // = yRotO - getYRot()), not the head pair; latch/clamp orig :671-678 on the entity's own RenderInfo.
            yaw = (entity.yRotO() - entity.getYRot()) * 10.0f;
            yaw = -yaw;
            r.rf1 += (yaw - r.rf1) / 50.0f;
            if (r.rf1 > 50.0f) {
                r.rf1 = 50.0f;
            }
            if (r.rf1 < -50.0f) {
                r.rf1 = -50.0f;
            }
            yaw = r.rf1;
        }

        float h2 = h3 = yaw * 2.0f / 3.0f;
        float h1 = h3;
        float d2 = d3 = headPitch * 2.0f / 3.0f;
        float d1 = d3;
        if (h1 < 0.0f) {
            h2 = h3 = h1 / 2.0f;
            d2 = d3 = d1 / 2.0f;
        } else {
            h2 = h1 = h3 / 2.0f;
            d2 = d1 = d3 / 2.0f;
        }

        rotateY(processor, "head7", (float) Math.toRadians(h2));
        rotateY(processor, "head3", (float) Math.toRadians(h2));
        rotateY(processor, "headfin", (float) Math.toRadians(h2));
        rotateY(processor, "jaw5", (float) Math.toRadians(h2));
        rotateY(processor, "jaw1", (float) Math.toRadians(h2));
        float neck3YRot = (float) Math.toRadians(h2) / 8.0f;
        rotateY(processor, "neck3", neck3YRot);
        float neck4YRot = (float) Math.toRadians(h2) / 4.0f;
        rotateY(processor, "neck4", neck4YRot);
        float neck5YRot = (float) Math.toRadians(h2) / 2.0f;
        rotateY(processor, "neck5", neck5YRot);

        rotateY(processor, "head7L", (float) Math.toRadians(h1));
        rotateY(processor, "head3L", (float) Math.toRadians(h1));
        rotateY(processor, "headfinL", (float) Math.toRadians(h1));
        rotateY(processor, "jaw5L", (float) Math.toRadians(h1));
        rotateY(processor, "jaw1L", (float) Math.toRadians(h1));
        float neck3LYRot = (float) Math.toRadians(h1) / 8.0f;
        rotateY(processor, "neck3L", neck3LYRot);
        float neck4LYRot = (float) Math.toRadians(h1) / 4.0f;
        rotateY(processor, "neck4L", neck4LYRot);
        float neck5LYRot = (float) Math.toRadians(h1) / 2.0f;
        rotateY(processor, "neck5L", neck5LYRot);

        rotateY(processor, "head7R", (float) Math.toRadians(h3));
        rotateY(processor, "head3R", (float) Math.toRadians(h3));
        rotateY(processor, "headfinR", (float) Math.toRadians(h3));
        rotateY(processor, "jaw5R", (float) Math.toRadians(h3));
        rotateY(processor, "jaw1R", (float) Math.toRadians(h3));
        float neck3RYRot = (float) Math.toRadians(h3) / 8.0f;
        rotateY(processor, "neck3R", neck3RYRot);
        float neck4RYRot = (float) Math.toRadians(h3) / 4.0f;
        rotateY(processor, "neck4R", neck4RYRot);
        float neck5RYRot = (float) Math.toRadians(h3) / 2.0f;
        rotateY(processor, "neck5R", neck5RYRot);

        float Rjx;
        float jx;
        float Ljx;
        if (entity.getAttacking() != 0) {
            newangle = Mth.cos(ageInTicks * 0.9f * WINGSPEED) * (float) Math.PI * 0.1f;
            Ljx = 0.25f + newangle;
            newangle = Mth.cos(ageInTicks * 1.1f * WINGSPEED) * (float) Math.PI * 0.1f;
            Rjx = 0.25f + newangle;
            newangle = Mth.cos(ageInTicks * 1.3f * WINGSPEED) * (float) Math.PI * 0.1f;
            jx = 0.25f + newangle;
        } else {
            newangle = Mth.cos(ageInTicks * 0.25f * WINGSPEED) * (float) Math.PI * 0.02f;
            Ljx = 0.1f + newangle;
            newangle = Mth.cos(ageInTicks * 0.3f * WINGSPEED) * (float) Math.PI * 0.02f;
            Rjx = 0.1f + newangle;
            newangle = Mth.cos(ageInTicks * 0.35f * WINGSPEED) * (float) Math.PI * 0.02f;
            jx = 0.1f + newangle;
        }

        rotateX(processor, "head7", (float) Math.toRadians(d2));
        rotateX(processor, "head3", (float) Math.toRadians(d2));
        rotateX(processor, "headfin", (float) Math.toRadians(d2) + 0.5f);
        rotateX(processor, "jaw5", (float) Math.toRadians(d2) + jx);
        rotateX(processor, "jaw1", (float) Math.toRadians(d2) + jx);

        rotateX(processor, "head7L", (float) Math.toRadians(d1));
        rotateX(processor, "head3L", (float) Math.toRadians(d1));
        rotateX(processor, "headfinL", (float) Math.toRadians(d1) + 0.5f);
        rotateX(processor, "jaw5L", (float) Math.toRadians(d1) + Ljx);
        rotateX(processor, "jaw1L", (float) Math.toRadians(d1) + Ljx);

        rotateX(processor, "head7R", (float) Math.toRadians(d3));
        rotateX(processor, "head3R", (float) Math.toRadians(d3));
        rotateX(processor, "headfinR", (float) Math.toRadians(d3) + 0.5f);
        rotateX(processor, "jaw5R", (float) Math.toRadians(d3) + Rjx);
        rotateX(processor, "jaw1R", (float) Math.toRadians(d3) + Rjx);

        d1 = entity.getHead1Ext();
        d2 = entity.getHead2Ext();
        d3 = entity.getHead3Ext();

        float neck3LXRot = -((float) Math.toRadians((double) d1 / 3.0));
        rotateX(processor, "neck3L", neck3LXRot);
        float neck4LXRot = -((float) Math.toRadians((double) d1 * 2.0 / 3.0));
        rotateX(processor, "neck4L", neck4LXRot);
        float neck5LXRot = -((float) Math.toRadians(d1));
        rotateX(processor, "neck5L", neck5LXRot);

        float neck3XRot = -((float) Math.toRadians((double) d2 / 3.0));
        rotateX(processor, "neck3", neck3XRot);
        float neck4XRot = -((float) Math.toRadians((double) d2 * 2.0 / 3.0));
        rotateX(processor, "neck4", neck4XRot);
        float neck5XRot = -((float) Math.toRadians(d2));
        rotateX(processor, "neck5", neck5XRot);

        float neck3RXRot = -((float) Math.toRadians((double) d3 / 3.0));
        rotateX(processor, "neck3R", neck3RXRot);
        float neck4RXRot = -((float) Math.toRadians((double) d3 * 2.0 / 3.0));
        rotateX(processor, "neck4R", neck4RXRot);
        float neck5RXRot = -((float) Math.toRadians(d3));
        rotateX(processor, "neck5R", neck5RXRot);

        // Center neck chain (the first ring's pivot never written: the bind)
        float[] neck3 = classicPosition(bone(processor, "neck3"));
        float neck4Y = neck3[1] + (float) Math.sin(neck3XRot) * 9.0f;
        float neck4Z = neck3[2] - (float) Math.cos(neck3XRot) * 9.0f;
        float neck4X = neck3[0] - (float) Math.sin(neck3YRot) * 9.0f * (float) Math.cos(neck3XRot);
        moveTo(processor, "neck4", neck4X, neck4Y, neck4Z);
        float neck5Y = neck4Y + (float) Math.sin(neck4XRot) * 9.0f;
        float neck5Z = neck4Z - (float) Math.cos(neck4XRot) * 9.0f;
        float neck5X = neck4X - (float) Math.sin(neck4YRot) * 9.0f * (float) Math.cos(neck4XRot);
        moveTo(processor, "neck5", neck5X, neck5Y, neck5Z);
        float head7Y = neck5Y + (float) Math.sin(neck5XRot) * 9.0f;    // jaw1.y = head3.y = (head7.y = ...); jaw5, headfin copy it
        float head7Z = neck5Z - (float) Math.cos(neck5XRot) * 9.0f;
        float head7X = neck5X - (float) Math.sin(neck5YRot) * 9.0f * (float) Math.cos(neck5XRot);
        moveTo(processor, "head7", head7X, head7Y, head7Z);
        moveTo(processor, "head3", head7X, head7Y, head7Z);
        moveTo(processor, "jaw1", head7X, head7Y, head7Z);
        moveTo(processor, "jaw5", head7X, head7Y, head7Z);
        moveTo(processor, "headfin", head7X, head7Y, head7Z);

        // Left neck chain
        float[] neck3L = classicPosition(bone(processor, "neck3L"));
        float neck4LY = neck3L[1] + (float) Math.sin(neck3LXRot) * 9.0f;
        float neck4LZ = neck3L[2] - (float) Math.cos(neck3LXRot) * 9.0f;
        float neck4LX = neck3L[0] - (float) Math.sin(neck3LYRot) * 9.0f * (float) Math.cos(neck3LXRot);
        moveTo(processor, "neck4L", neck4LX, neck4LY, neck4LZ);
        float neck5LY = neck4LY + (float) Math.sin(neck4LXRot) * 9.0f;
        float neck5LZ = neck4LZ - (float) Math.cos(neck4LXRot) * 9.0f;
        float neck5LX = neck4LX - (float) Math.sin(neck4LYRot) * 9.0f * (float) Math.cos(neck4LXRot);
        moveTo(processor, "neck5L", neck5LX, neck5LY, neck5LZ);
        float head7LY = neck5LY + (float) Math.sin(neck5LXRot) * 9.0f;
        float head7LZ = neck5LZ - (float) Math.cos(neck5LXRot) * 9.0f;
        float head7LX = neck5LX - (float) Math.sin(neck5LYRot) * 9.0f * (float) Math.cos(neck5LXRot);
        moveTo(processor, "head7L", head7LX, head7LY, head7LZ);
        moveTo(processor, "head3L", head7LX, head7LY, head7LZ);
        moveTo(processor, "jaw1L", head7LX, head7LY, head7LZ);
        moveTo(processor, "jaw5L", head7LX, head7LY, head7LZ);
        moveTo(processor, "headfinL", head7LX, head7LY, head7LZ);

        // Right neck chain
        float[] neck3R = classicPosition(bone(processor, "neck3R"));
        float neck4RY = neck3R[1] + (float) Math.sin(neck3RXRot) * 9.0f;
        float neck4RZ = neck3R[2] - (float) Math.cos(neck3RXRot) * 9.0f;
        float neck4RX = neck3R[0] - (float) Math.sin(neck3RYRot) * 9.0f * (float) Math.cos(neck3RXRot);
        moveTo(processor, "neck4R", neck4RX, neck4RY, neck4RZ);
        float neck5RY = neck4RY + (float) Math.sin(neck4RXRot) * 9.0f;
        float neck5RZ = neck4RZ - (float) Math.cos(neck4RXRot) * 9.0f;
        float neck5RX = neck4RX - (float) Math.sin(neck4RYRot) * 9.0f * (float) Math.cos(neck4RXRot);
        moveTo(processor, "neck5R", neck5RX, neck5RY, neck5RZ);
        float head7RY = neck5RY + (float) Math.sin(neck5RXRot) * 9.0f;
        float head7RZ = neck5RZ - (float) Math.cos(neck5RXRot) * 9.0f;
        float head7RX = neck5RX - (float) Math.sin(neck5RYRot) * 9.0f * (float) Math.cos(neck5RXRot);
        moveTo(processor, "head7R", head7RX, head7RY, head7RZ);
        moveTo(processor, "head3R", head7RX, head7RY, head7RZ);
        moveTo(processor, "jaw1R", head7RX, head7RY, head7RZ);
        moveTo(processor, "jaw5R", head7RX, head7RY, head7RZ);
        moveTo(processor, "headfinR", head7RX, head7RY, head7RZ);
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

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<ThePrinceTeen, ThePrinceTeenGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new ThePrinceTeenGeoReplacement());
        }
    }
}
