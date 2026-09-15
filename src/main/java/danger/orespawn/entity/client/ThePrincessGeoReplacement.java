package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.ThePrincess;
import danger.orespawn.entity.pose.ThePrincessPose;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationProcessor;
import software.bernie.geckolib.cache.object.GeoBone;

/**
 * GeckoLib The Princess (the remainder slice, 2026-09-15; owner 2026-09-15, closing set, item 3 with the bipeds - held
 * since the hooks' landing on the orbs' per-frame state): {@link ModelThePrincess#poseFrom} verbatim on the converted rig,
 * ON THE HOOK (no keyframe layer, no transcription - the self-gate stays closed until an artist delivers {@code idle} and
 * {@code walk}). Her pose is the Prince's (ModelThePrince.poseFrom, {@link ThePrinceGeoReplacement}): wingspeed 0.65f
 * (orig ModelThePrincess.java:16,56 / ClientProxyOreSpawn.java:511) - the THRESHOLD-OR-ATTACKING idiom on the six wing
 * parts' roll ({@code (double) limbSwingAmount > 0.1 || getAttacking() != 0}: a 2.3 ws cosine x PI x 0.4 x amount, else a
 * 0.3 ws x 0.04 breath, around -+0.4 / 0.6 / 0.2); the THRESHOLD idiom on the legs (2.0 ws x PI x 0.25 x amount, else 0)
 * under the ACTIVITY branch ({@code getActivity() != 2 || attacking}: the gait, else both legs tucked at -1.0); the tail
 * sway (0.9 ws x PI x 0.06) stilled by the SIT order ({@code isOrderedToSit()}) and overridden by the ATTACKING lash (1.3 ws
 * x PI x 0.12), fanned x1.6 / 2.6 / 3.6 / 4.6 down the five tail rings FOLLOWING one another by 6 / 5 / 4 / 4 units in x / z
 * (POSITION writes through {@link #moveTo}); the three-head LOOK split (two thirds of the head yaw / pitch, the side heads
 * at half of it on the side the head turns to, in radians on each head's head / snout / fin / jaw, half on its neck) with
 * the ATTACKING chatter on the three jaws (1.9 / 2.1 / 2.3 ws cosines x PI x 0.2 + 0.2); the three necks pitched by the
 * entity's HEAD EXTENSIONS ({@code getHead1Ext / 2 / 3}, degrees) and each head group (head, snout, fin, jaw) FOLLOWING
 * its neck 7 units along its pitch and yaw. PLUS THE THREE POWER ORBS (ModelThePrincess.java:319-354): {@code Lpower /
 * Cpower / Rpower} whose {@code xRot / yRot / zRot} the classic ADVANCES BY A CONSTANT PER RENDERED FRAME on the model
 * singleton's own parts ({@code += 0.03 / 0.04 / 0.05}, {@code += 0.035 / 0.046 / 0.065}, {@code += 0.05 / 0.13 / 0.03},
 * each wrapped below pi by {@code -= 2 pi}) - state on no entity field, frame-rate dependent in the classic. Carried as the
 * classic does: the hook reads each orb bone's rotation as this frame begins ({@link #classicRotX} / {@link #classicRotY} /
 * {@link #classicRotZ}: the bake's bone keeps a hook-written value between frames exactly as the classic ModelPart keeps
 * its field - GeckoLib's tickAnimation resets only a bone no hook wrote that tick - and both are one singleton per species,
 * shared by every entity of it), adds the constant, wraps and writes. On the probe a fresh bake per sample on this side and
 * {@code ModelPart.resetPose} per sample on the classic side put both at the bind (0) before the pose, so a sample sees one
 * step from the bind on both sides. Within the S4 doctrine's letter after item 3: no seam accumulator was added - the
 * bones the seam already keeps between frames are the model's own parts. The entity is read through
 * {@link ThePrincessPose} (the Slice 4b doctrine). Every value the classic reads back from a part it just wrote is held
 * in a local; the necks' and the second tail ring's pivots, never written, are read through {@link #classicPosition} (the
 * bind). The classic's early {@code jaw.z / x = snout.z / x - cos / sin(snout.yRot)} (and the side jaws') read the snout's
 * position before this frame writes it and are overwritten by the neck follow below before any read - dead writes,
 * transcribed as such.
 *
 * <p>Scale and shadow follow {@link ThePrincessRenderer}: 0.7 render scale ({@link ThePrincessRenderer#SCALE}, applied
 * around {@code super.render} - made public by this slice; the T2d form) and the {@code 0.7f * 0.7f} shadow literal its
 * constructor passes (no SHADOW constant). Tier 1: hitbox profiles excluded, the classic single hitbox stays. The six wing
 * parts are zero-thickness cubes (the seam draws every cube with its true transformed normal and its two coplanar faces in
 * the classic order, ENT-S-161 / TEST-007 - below).</p>
 */
public final class ThePrincessGeoReplacement extends OreSpawnGeoReplacement<ThePrincess> {
    /** orig ModelThePrincess.java:16,56 {@code wingspeed} = 0.65f (ClientProxyOreSpawn.java:511): the chain's frequency multiplier. */
    static final float WINGSPEED = 0.65f;
    private static final GeoReplacementDescriptor<ThePrincess> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.THE_PRINCESS.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            ThePrincess.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/theprincess.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/theprincess.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/theprincess.png"),
            // ThePrincessRenderer's constructor passes the literal 0.7f * 0.7f (no SHADOW constant): the equal literal
            0.7F * 0.7F) {
        @Override
        public void applyScale(ThePrincess entity, PoseStack poseStack, float partialTick) {
            // ThePrincessRenderer.render: poseStack.scale(SCALE, SCALE, SCALE) around super.render (the renderer's constant)
            poseStack.scale(ThePrincessRenderer.SCALE, ThePrincessRenderer.SCALE, ThePrincessRenderer.SCALE);
        }

        /**
         * A rig with zero-thickness cubes (the six wing parts, 22 / 12 / 10 x 0 x 10): the within-cube face order
         * decides a flat cube's z-fight, so the shipped geo carries the classic order ({@link FaceOrder#KEY}; TEST-007)
         * and the seam expects it.
         */
        @Override
        public boolean cubeFaceOrderRequired() {
            return true;
        }
    };

    public ThePrincessGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        ThePrincessPose entity = inputs.subject(ThePrincessPose.class);
        float limbSwingAmount = inputs.limbSwingAmount();
        float ageInTicks = inputs.ageInTicks();
        float netHeadYaw = inputs.netHeadYaw();
        float headPitch = inputs.headPitch();
        // ModelThePrincess.poseFrom verbatim (the Prince's form, then the orbs).
        float newangle;
        int current_activity = entity.getActivity();

        newangle = (double) limbSwingAmount > 0.1 || entity.getAttacking() != 0
                ? Mth.cos(ageInTicks * 2.3f * WINGSPEED) * (float) Math.PI * 0.4f * limbSwingAmount
                : Mth.cos(ageInTicks * 0.3f * WINGSPEED) * (float) Math.PI * 0.04f;
        rotateZ(processor, "Rwing", newangle - 0.4f);
        rotateZ(processor, "Rwing2", newangle - 0.6f);
        rotateZ(processor, "Rwing3", newangle - 0.2f);
        rotateZ(processor, "Lwing", -newangle + 0.4f);
        rotateZ(processor, "Lwing2", -newangle + 0.6f);
        rotateZ(processor, "Lwing3", -newangle + 0.2f);

        newangle = (double) limbSwingAmount > 0.1
                ? Mth.cos(ageInTicks * 2.0f * WINGSPEED) * (float) Math.PI * 0.25f * limbSwingAmount
                : 0.0f;
        if (current_activity != 2 || entity.getAttacking() != 0) {
            rotateX(processor, "Rleg1", newangle);
            rotateX(processor, "Lleg1", -newangle);
        } else {
            float rleg1XRot = newangle = -1.0f;
            rotateX(processor, "Rleg1", rleg1XRot);
            rotateX(processor, "Lleg1", newangle);
        }

        newangle = Mth.cos(ageInTicks * 0.9f * WINGSPEED) * (float) Math.PI * 0.06f;
        if (entity.isOrderedToSit()) {
            newangle = 0.0f;
        }
        if (entity.getAttacking() != 0) {
            newangle = Mth.cos(ageInTicks * 1.3f * WINGSPEED) * (float) Math.PI * 0.12f;
        }
        float tail2YRot = newangle;
        rotateY(processor, "tail2", tail2YRot);
        float[] tail2 = classicPosition(bone(processor, "tail2"));  // never written: the bind pivot
        float tail3Z = tail2[2] + (float) Math.cos(tail2YRot) * 6.0f;
        float tail3X = tail2[0] + (float) Math.sin(tail2YRot) * 6.0f;
        moveXZ(processor, "tail3", tail3X, tail3Z);
        float tail3YRot = newangle * 1.6f;
        rotateY(processor, "tail3", tail3YRot);
        float tail4Z = tail3Z + (float) Math.cos(tail3YRot) * 5.0f;
        float tail4X = tail3X + (float) Math.sin(tail3YRot) * 5.0f;
        moveXZ(processor, "tail4", tail4X, tail4Z);
        float tail4YRot = newangle * 2.6f;
        rotateY(processor, "tail4", tail4YRot);
        float tail5Z = tail4Z + (float) Math.cos(tail4YRot) * 4.0f;
        float tail5X = tail4X + (float) Math.sin(tail4YRot) * 4.0f;
        moveXZ(processor, "Tail5", tail5X, tail5Z);
        float tail5YRot = newangle * 3.6f;
        rotateY(processor, "Tail5", tail5YRot);
        float tail6Z = tail5Z + (float) Math.cos(tail5YRot) * 4.0f;
        float tail6X = tail5X + (float) Math.sin(tail5YRot) * 4.0f;
        moveXZ(processor, "Tail6", tail6X, tail6Z);
        rotateY(processor, "Tail6", newangle * 4.6f);

        float h2, h3, h1, d2, d3, d1;
        h2 = h3 = netHeadYaw * 2.0f / 3.0f;
        h1 = h3;
        d2 = d3 = headPitch * 2.0f / 3.0f;
        d1 = d3;
        if (h1 < 0.0f) {
            h2 = h3 = h1 / 2.0f;
            d2 = d3 = d1 / 2.0f;
        } else {
            h2 = h1 = h3 / 2.0f;
            d2 = d1 = d3 / 2.0f;
        }

        rotateY(processor, "head", (float) Math.toRadians(h2));
        float snoutYRot = (float) Math.toRadians(h2);
        rotateY(processor, "snout", snoutYRot);
        rotateY(processor, "headfin", (float) Math.toRadians(h2));
        rotateY(processor, "jaw", (float) Math.toRadians(h2));
        // jaw.z / x = snout.z / x - cos / sin(snout.yRot): the snout's position as it stands before this frame's write
        // (the bind at hook time), overwritten by the neck follow below before any read - a dead write, as the classic's.
        float[] snout = classicPosition(bone(processor, "snout"));
        float jawZ = snout[2] - (float) Math.cos(snoutYRot);
        float jawX = snout[0] - (float) Math.sin(snoutYRot);
        moveXZ(processor, "jaw", jawX, jawZ);
        float neckYRot = (float) Math.toRadians(h2) / 2.0f;
        rotateY(processor, "neck", neckYRot);

        rotateY(processor, "Lhead", (float) Math.toRadians(h1));
        float lsnoutYRot = (float) Math.toRadians(h1);
        rotateY(processor, "Lsnout", lsnoutYRot);
        rotateY(processor, "Lheadfin", (float) Math.toRadians(h1));
        rotateY(processor, "Ljaw", (float) Math.toRadians(h1));
        float[] lsnout = classicPosition(bone(processor, "Lsnout"));   // the dead write, as above
        float ljawZ = lsnout[2] - (float) Math.cos(lsnoutYRot);
        float ljawX = lsnout[0] - (float) Math.sin(lsnoutYRot);
        moveXZ(processor, "Ljaw", ljawX, ljawZ);
        float lneckYRot = (float) Math.toRadians(h1) / 2.0f;
        rotateY(processor, "Lneck", lneckYRot);

        rotateY(processor, "Rhead", (float) Math.toRadians(h3));
        float rsnoutYRot = (float) Math.toRadians(h3);
        rotateY(processor, "Rsnout", rsnoutYRot);
        rotateY(processor, "Rheadfin", (float) Math.toRadians(h3));
        rotateY(processor, "Rjaw", (float) Math.toRadians(h3));
        float[] rsnout = classicPosition(bone(processor, "Rsnout"));   // the dead write, as above
        float rjawZ = rsnout[2] - (float) Math.cos(rsnoutYRot);
        float rjawX = rsnout[0] - (float) Math.sin(rsnoutYRot);
        moveXZ(processor, "Rjaw", rjawX, rjawZ);
        float rneckYRot = (float) Math.toRadians(h3) / 2.0f;
        rotateY(processor, "Rneck", rneckYRot);

        float Rjx = 0.0f;
        float jx = 0.0f;
        float Ljx = 0.0f;
        if (entity.getAttacking() != 0) {
            newangle = Mth.cos(ageInTicks * 1.9f * WINGSPEED) * (float) Math.PI * 0.2f;
            Ljx = 0.2f + newangle;
            newangle = Mth.cos(ageInTicks * 2.1f * WINGSPEED) * (float) Math.PI * 0.2f;
            Rjx = 0.2f + newangle;
            newangle = Mth.cos(ageInTicks * 2.3f * WINGSPEED) * (float) Math.PI * 0.2f;
            jx = 0.2f + newangle;
        }

        rotateX(processor, "head", (float) Math.toRadians(d2));
        rotateX(processor, "snout", (float) Math.toRadians(d2));
        rotateX(processor, "headfin", (float) Math.toRadians(d2));
        rotateX(processor, "jaw", (float) Math.toRadians(d2) + jx);
        rotateX(processor, "Lhead", (float) Math.toRadians(d1));
        rotateX(processor, "Lsnout", (float) Math.toRadians(d1));
        rotateX(processor, "Lheadfin", (float) Math.toRadians(d1));
        rotateX(processor, "Ljaw", (float) Math.toRadians(d1) + Ljx);
        rotateX(processor, "Rhead", (float) Math.toRadians(d3));
        rotateX(processor, "Rsnout", (float) Math.toRadians(d3));
        rotateX(processor, "Rheadfin", (float) Math.toRadians(d3));
        rotateX(processor, "Rjaw", (float) Math.toRadians(d3) + Rjx);

        d1 = entity.getHead1Ext();
        d2 = entity.getHead2Ext();
        d3 = entity.getHead3Ext();
        float lneckXRot = (float) Math.toRadians(d1);
        rotateX(processor, "Lneck", lneckXRot);
        float neckXRot = (float) Math.toRadians(d2);
        rotateX(processor, "neck", neckXRot);
        float rneckXRot = (float) Math.toRadians(d3);
        rotateX(processor, "Rneck", rneckXRot);

        // Lsnout.y = Ljaw.y = Lhead.y = ...; Lheadfin.y = Ljaw.y; the same for z and x (the neck's pivot never written: the bind)
        float[] lneck = classicPosition(bone(processor, "Lneck"));
        float lheadY = lneck[1] - (float) Math.cos(lneckXRot) * 7.0f;
        float lheadZ = lneck[2] - (float) Math.sin(lneckXRot) * 7.0f;
        float lheadX = lneck[0] - (float) Math.sin(lneckYRot) * 7.0f * (float) Math.sin(lneckXRot);
        moveTo(processor, "Lhead", lheadX, lheadY, lheadZ);
        moveTo(processor, "Ljaw", lheadX, lheadY, lheadZ);
        moveTo(processor, "Lsnout", lheadX, lheadY, lheadZ);
        moveTo(processor, "Lheadfin", lheadX, lheadY, lheadZ);

        float[] rneck = classicPosition(bone(processor, "Rneck"));
        float rheadY = rneck[1] - (float) Math.cos(rneckXRot) * 7.0f;
        float rheadZ = rneck[2] - (float) Math.sin(rneckXRot) * 7.0f;
        float rheadX = rneck[0] - (float) Math.sin(rneckYRot) * 7.0f * (float) Math.sin(rneckXRot);
        moveTo(processor, "Rhead", rheadX, rheadY, rheadZ);
        moveTo(processor, "Rjaw", rheadX, rheadY, rheadZ);
        moveTo(processor, "Rsnout", rheadX, rheadY, rheadZ);
        moveTo(processor, "Rheadfin", rheadX, rheadY, rheadZ);

        float[] neck = classicPosition(bone(processor, "neck"));
        float headY = neck[1] - (float) Math.cos(neckXRot) * 7.0f;
        float headZ = neck[2] - (float) Math.sin(neckXRot) * 7.0f;
        float headX = neck[0] - (float) Math.sin(neckYRot) * 7.0f * (float) Math.sin(neckXRot);
        moveTo(processor, "head", headX, headY, headZ);
        moveTo(processor, "jaw", headX, headY, headZ);
        moveTo(processor, "snout", headX, headY, headZ);
        moveTo(processor, "headfin", headX, headY, headZ);

        // The three power orbs (ModelThePrincess.java:319-354): each axis advanced by its constant from the value the bone holds
        // as this frame begins (the previous rendered frame's write; the bind on the first), wrapped below pi as the classic does
        // in double, one write per axis.
        advanceX(processor, "Lpower", 0.03f);
        advanceX(processor, "Cpower", 0.04f);
        advanceX(processor, "Rpower", 0.05f);
        advanceY(processor, "Lpower", 0.035f);
        advanceY(processor, "Cpower", 0.046f);
        advanceY(processor, "Rpower", 0.065f);
        advanceZ(processor, "Lpower", 0.05f);
        advanceZ(processor, "Cpower", 0.13f);
        advanceZ(processor, "Rpower", 0.03f);
    }

    /** {@code part.xRot += step; if ((double) part.xRot > Math.PI) part.xRot = (float) ((double) part.xRot - Math.PI * 2);} */
    private static void advanceX(AnimationProcessor<?> processor, String name, float step) {
        GeoBone bone = bone(processor, name);
        float xRot = classicRotX(bone) + step;
        if ((double) xRot > Math.PI) {
            xRot = (float) ((double) xRot - Math.PI * 2);
        }
        rotateX(processor, name, xRot);
    }

    /** {@code part.yRot += step; if ((double) part.yRot > Math.PI) part.yRot = (float) ((double) part.yRot - Math.PI * 2);} */
    private static void advanceY(AnimationProcessor<?> processor, String name, float step) {
        GeoBone bone = bone(processor, name);
        float yRot = classicRotY(bone) + step;
        if ((double) yRot > Math.PI) {
            yRot = (float) ((double) yRot - Math.PI * 2);
        }
        rotateY(processor, name, yRot);
    }

    /** {@code part.zRot += step; if ((double) part.zRot > Math.PI) part.zRot = (float) ((double) part.zRot - Math.PI * 2);} */
    private static void advanceZ(AnimationProcessor<?> processor, String name, float step) {
        GeoBone bone = bone(processor, name);
        float zRot = classicRotZ(bone) + step;
        if ((double) zRot > Math.PI) {
            zRot = (float) ((double) zRot - Math.PI * 2);
        }
        rotateZ(processor, name, zRot);
    }

    /** {@code part.x = x; part.z = z} on a part whose y the classic never writes (the bind). */
    private static void moveXZ(AnimationProcessor<?> processor, String name, float x, float z) {
        float[] position = classicPosition(bone(processor, name));
        moveTo(processor, name, x, position[1], z);
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<ThePrincess, ThePrincessGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new ThePrincessGeoReplacement());
        }
    }
}
