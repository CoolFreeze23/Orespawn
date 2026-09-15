package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityLurkingTerror;
import danger.orespawn.entity.pose.LurkingTerrorPose;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Lurking Terror (the hook survey, landed by the remainder slice, 2026-09-15 - TEST-012 cleared under the
 * pair-contested rule): {@link LurkingTerrorModel#poseFrom} verbatim on the converted rig, ON THE HOOK (no keyframe layer, no
 * transcription - the self-gate stays closed until an artist delivers {@code idle} and {@code walk}). Wingspeed 1.0f (orig
 * ModelLurkingTerror.java:15; the default, the classic constructor takes none): the per-entity LATCHES (orig :435-562; the
 * Robot2 precedent) - each frame the leg rhythm's phase {@code |age * 0.7 ws mod 2 PI|} is compared with the last frame's ({@code
 * rf1}) and on each wrap the entity's own random rolls six selector bits into {@code ri1} (two at 1 in 3, two at 1 in 4, two at
 * 1 in 6), the mouth rhythm's phase {@code |age * 0.9 ws mod 2 PI|} likewise rolls {@code ri2} (1 in 20, forced by the ATTACKING
 * flag); the six legs then sway about Z on {@code sin(age * 0.7 ws) * PI * 0.25 / 0.15 / 0.1} around their +-0.191 / +-0.675 /
 * +-0.34 rests only while their bit is set (the {@code Mth.sin} idiom); the four jaws with their twenty-four teeth open on the
 * {@code |sin|} idiom ({@code |sin(age * 0.9 ws) * PI * 0.35|}, the side pair about Y, the top / bottom pair about X,
 * mirrored) while the mouth bit is set, the three tongue parts held straight with a POSITION write through {@link #moveTo} (the
 * tip 5 and 10 units out on the same angle); the thorax breathes on {@code sin(age * 0.1 ws) * PI * 0.06} with the abdomen
 * FOLLOWING 14 units of its sine; and the four wings beat about X on {@code cos(age * 1.4 ws) * PI * 0.2} around 0.455. The entity
 * is read through {@link LurkingTerrorPose}. The four wings are zero-thickness cubes (the seam draws every cube with its true
 * transformed normal, ENT-S-161, and its two coplanar faces in the classic order - below).
 *
 * <p>Scale and shadow follow {@link LurkingTerrorRenderer}: 0.85 render scale and a 0.45 x 0.85 shadow (ENT-S-092).</p>
 */
public final class LurkingTerrorGeoReplacement extends OreSpawnGeoReplacement<EntityLurkingTerror> {
    /** orig ModelLurkingTerror.java:15 {@code wingspeed} = 1.0f (the default; ClientProxyOreSpawn.java:461 passes none). */
    static final float WINGSPEED = 1.0F;
    private static final GeoReplacementDescriptor<EntityLurkingTerror> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.ENTITY_LURKING_TERROR.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            EntityLurkingTerror.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/lurkingterror.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/lurkingterror.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/lurkingterror.png"),
            LurkingTerrorRenderer.SHADOW) {
        @Override
        public void applyScale(EntityLurkingTerror entity, PoseStack poseStack, float partialTick) {
            // orig RenderLurkingTerror.preRenderScale (:39-41): GL11.glScalef(scale, scale, scale) (LurkingTerrorRenderer.scale)
            poseStack.scale(LurkingTerrorRenderer.SCALE, LurkingTerrorRenderer.SCALE, LurkingTerrorRenderer.SCALE);
        }

        /**
         * A rig with zero-thickness cubes (the four wings, 8 x 0 x 22 and 4 x 0 x 18): the within-cube face order decides
         * a flat cube's z-fight, so the shipped geo carries the classic order ({@link FaceOrder#KEY}; TEST-007) and the
         * seam expects it.
         */
        @Override
        public boolean cubeFaceOrderRequired() {
            return true;
        }
    };

    public LurkingTerrorGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        LurkingTerrorPose entity = inputs.subject(LurkingTerrorPose.class);
        float ageInTicks = inputs.ageInTicks();
        // LurkingTerrorModel.poseFrom (the body of the classic setupAnim) verbatim, the float chain left to right.
        float newangle = 0.0f;
        float legspeed = 0.7f;
        float mouthspeed = 0.9f;
        // Per-entity scratch as in the original (orig LurkingTerror.java:49, orig ModelLurkingTerror.java:443)
        RenderInfo r = entity.getRenderInfo();
        newangle = ageInTicks * legspeed * WINGSPEED % ((float) Math.PI * 2);
        newangle = Math.abs(newangle);
        if (newangle < r.rf1) {
            r.ri1 = 0;
            if (entity.getRandom().nextInt(3) == 1) {
                r.ri1 |= 1;
            }
            if (entity.getRandom().nextInt(3) == 1) {
                r.ri1 |= 2;
            }
            if (entity.getRandom().nextInt(4) == 1) {
                r.ri1 |= 4;
            }
            if (entity.getRandom().nextInt(4) == 1) {
                r.ri1 |= 8;
            }
            if (entity.getRandom().nextInt(6) == 1) {
                r.ri1 |= 0x10;
            }
            if (entity.getRandom().nextInt(6) == 1) {
                r.ri1 |= 0x20;
            }
        }
        r.rf1 = newangle;
        newangle = ageInTicks * mouthspeed * WINGSPEED % ((float) Math.PI * 2);
        if ((newangle = Math.abs(newangle)) < r.rf2) {
            r.ri2 = 0;
            if (entity.getRandom().nextInt(20) == 1) {
                r.ri2 |= 1;
            }
            if (entity.getAttacking() != 0) {
                r.ri2 = 1;
            }
        }
        r.rf2 = newangle;
        newangle = 0.0f;
        if ((r.ri1 & 1) != 0) {
            newangle = Mth.sin(ageInTicks * legspeed * WINGSPEED) * (float) Math.PI * 0.25f;
        }
        rotateZ(processor, "leg2", 0.191f + newangle);
        rotateZ(processor, "leg2part2", 0.191f + newangle);
        rotateZ(processor, "leg2part3", 0.675f + newangle);
        newangle = 0.0f;
        if ((r.ri1 & 2) != 0) {
            newangle = Mth.sin(ageInTicks * legspeed * WINGSPEED) * (float) Math.PI * 0.25f;
        }
        rotateZ(processor, "leg1", -0.191f + newangle);
        rotateZ(processor, "leg1part2", -0.191f + newangle);
        rotateZ(processor, "leg1part3", -0.675f + newangle);
        newangle = 0.0f;
        if ((r.ri1 & 4) != 0) {
            newangle = Mth.sin(ageInTicks * legspeed * WINGSPEED) * (float) Math.PI * 0.15f;
        }
        rotateZ(processor, "leg4", 0.191f + newangle);
        rotateZ(processor, "leg4part2", 0.191f + newangle);
        rotateZ(processor, "leg4part3", 0.675f + newangle);
        newangle = 0.0f;
        if ((r.ri1 & 8) != 0) {
            newangle = Mth.sin(ageInTicks * legspeed * WINGSPEED) * (float) Math.PI * 0.15f;
        }
        rotateZ(processor, "leg3", -0.191f + newangle);
        rotateZ(processor, "leg3part2", -0.191f + newangle);
        rotateZ(processor, "leg3part3", -0.675f + newangle);
        newangle = 0.0f;
        if ((r.ri1 & 0x10) != 0) {
            newangle = Mth.sin(ageInTicks * legspeed * WINGSPEED) * (float) Math.PI * 0.1f;
        }
        rotateZ(processor, "leg6", -0.34f + newangle);
        rotateZ(processor, "leg6part2", -0.34f + newangle);
        newangle = 0.0f;
        if ((r.ri1 & 0x20) != 0) {
            newangle = Mth.sin(ageInTicks * legspeed * WINGSPEED) * (float) Math.PI * 0.1f;
        }
        rotateZ(processor, "leg5", 0.34f + newangle);
        rotateZ(processor, "leg5part2", 0.34f + newangle);
        newangle = 0.0f;
        if ((r.ri2 & 1) != 0) {
            newangle = Mth.sin(ageInTicks * mouthspeed * WINGSPEED) * (float) Math.PI * 0.35f;
            newangle = Math.abs(newangle);
        }
        rotateY(processor, "jaw1", newangle);
        rotateY(processor, "jaw1part2", newangle);
        rotateY(processor, "jaw1tooth3", newangle);
        rotateY(processor, "jaw1tooth5", newangle);
        rotateY(processor, "jaw1tooth1", newangle);
        rotateY(processor, "jaw1tooth4", newangle);
        rotateY(processor, "jaw1tooth6", newangle);
        rotateY(processor, "jaw1tooth2", newangle);
        rotateY(processor, "jaw2", -newangle);
        rotateY(processor, "jaw2part2", -newangle);
        rotateY(processor, "jaw2tooth3", -newangle);
        rotateY(processor, "jaw2tooth5", -newangle);
        rotateY(processor, "jaw2tooth1", -newangle);
        rotateY(processor, "jaw2tooth4", -newangle);
        rotateY(processor, "jaw2tooth6", -newangle);
        rotateY(processor, "jaw2tooth2", -newangle);
        rotateX(processor, "jaw3", -newangle);
        rotateX(processor, "jaw3part2", -newangle);
        rotateX(processor, "jaw3tooth3", -newangle);
        rotateX(processor, "jaw3tooth5", -newangle);
        rotateX(processor, "jaw3tooth1", -newangle);
        rotateX(processor, "jaw3tooth4", -newangle);
        rotateX(processor, "jaw3tooth6", -newangle);
        rotateX(processor, "jaw3tooth2", -newangle);
        rotateX(processor, "jaw4", newangle);
        rotateX(processor, "jaw4part2", newangle);
        rotateX(processor, "jaw4tooth3", newangle);
        rotateX(processor, "jaw4tooth5", newangle);
        rotateX(processor, "jaw4tooth1", newangle);
        rotateX(processor, "jaw4tooth4", newangle);
        rotateX(processor, "jaw4tooth6", newangle);
        rotateX(processor, "jaw4tooth2", newangle);
        rotateX(processor, "tonguepart3", 0.0f);
        rotateX(processor, "tonguepart2", 0.0f);
        rotateX(processor, "tonguepart1", 0.0f);
        rotateY(processor, "tonguepart3", 0.0f);
        rotateY(processor, "tonguepart2", 0.0f);
        rotateY(processor, "tonguepart1", 0.0f);
        rotateZ(processor, "tonguepart3", 0.0f);
        rotateZ(processor, "tonguepart2", 0.0f);
        rotateZ(processor, "tonguepart1", 0.0f);
        // tonguepart1.x = tonguepart3.x = tonguepart2.x; y likewise; z = tonguepart2.z - newangle * 5 / 10 (tonguepart2 never moves: the bind)
        float[] tonguepart2 = classicPosition(bone(processor, "tonguepart2"));
        moveTo(processor, "tonguepart1", tonguepart2[0], tonguepart2[1], tonguepart2[2] - newangle * 5.0f);
        moveTo(processor, "tonguepart3", tonguepart2[0], tonguepart2[1], tonguepart2[2] - newangle * 10.0f);
        rotateX(processor, "thorax", newangle = Mth.sin(ageInTicks * 0.1f * WINGSPEED) * (float) Math.PI * 0.06f);
        float[] thorax = classicPosition(bone(processor, "thorax"));  // never written: the bind pivot
        float[] abdomen = classicPosition(bone(processor, "abdomen"));  // x / z never written: the bind
        moveTo(processor, "abdomen", abdomen[0], (float) ((double) thorax[1] - Math.sin(newangle) * 14.0), abdomen[2]);
        newangle = Mth.cos(ageInTicks * 1.4f * WINGSPEED) * (float) Math.PI * 0.2f;
        rotateX(processor, "wing_1", 0.455f + newangle);
        rotateX(processor, "wing_2", 0.455f + newangle);
        rotateX(processor, "wing_3", 0.455f - newangle);
        rotateX(processor, "wing_4", 0.455f - newangle);
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<EntityLurkingTerror, LurkingTerrorGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new LurkingTerrorGeoReplacement());
        }
    }
}
