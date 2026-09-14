package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Crab;
import danger.orespawn.entity.pose.CrabPose;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Crab (the hooks, owner 2026-09-14, addendum item 10; landed by the fourth Tier-2 slice T2d, 2026-09-14, the
 * owner's item 9 with the draw fix of item 3): the classic {@link ModelCrab} pose verbatim on the render-instance-expanded
 * rig, ON THE HOOK (Amendment 2 to Amendment 1: no keyframe layer, no transcription - the self-gate stays closed until an
 * artist delivers {@code idle} and {@code walk}). No wingspeed (orig ModelCrab.java has none).
 *
 * <p>THE LEGS, THE RENDER-INSTANCE FORM (the Slice 4c form, the explicit scope of 2026-09-14): 1.7.10's {@code render}
 * (orig ModelCrab.java:195-289) set each of the three leg parts' rotation point and yaw and DREW it eight times - four on
 * the left side (x 36) at z 0 / 10 / 20 / 30 with yaw -pi/2 + a, -pi/2 - a, -pi/2 + a, -pi/2 - a, then four on the right
 * (x -36) with the yaw negated - {@code a = cos(age * 1.7f) * PI * 0.15f * limbSwingAmount} the gait. The port's classic
 * model now does the same (ANIM-025, the draw fix of this slice): {@code ModelCrab.renderToBuffer} re-poses and draws
 * {@code leg1} / {@code leg2} / {@code leg3} at each of the eight poses from the two inputs {@code poseFrom} keeps
 * (orig :199-274). The reference entry ({@code reference_crab}, {@code tools/reference_model_proofs.json}) declares the
 * eight draws per part in the explicit form - a translation series with a mirrored yaw, which no rotational step about one
 * axis expresses - so the converter emits one TOP-LEVEL clone per draw, {@code <part>__i<k>} (draws 0-3 the left side
 * front to back, 4-7 the right), its bind pivot the declared point ((+-36, 3, z)) and its bind yaw -+pi/2, the part's own
 * pitch kept; there is no group bone. So this hook poses every clone directly, as {@link PurplePowerGeoReplacement} poses
 * its expanded rig: draw {@code k}'s classic pose - the pivot write ({@code x, y, z}, through {@link #moveTo}: the classic's
 * position write, landing on the clone's bind) and the yaw ({@code -pi/2 +- a} through {@link #rotateY}, the double / float
 * chain exactly as the classic casts it) - onto the three clones {@code leg1__i<k>}, {@code leg2__i<k>}, {@code leg3__i<k>},
 * in the classic's order of the eight poses; the composition leg proves every clone against the classic's measured draw
 * pose ({@code tools/g1_render_parity.py}, the explicit-scope case of this slice).</p>
 *
 * <p>THE REST (orig :275-318): the ATTACKING branch, read through {@link CrabPose} - at rest the four eye parts nod and
 * roll on 0.35 / 0.25 / 0.3 / 0.45 cosines x 0.05 around +-0.54 rad, the two mouth parts on a 0.25 cosine around -+0.72,
 * the three claw tips of each side on a 0.15 / 0.13 cosine x 0.03 / 0.02 around -+0.453 / -+0.349 / +-0.384; attacking the
 * same parts on 0.45 / 0.35 / 0.4 / 0.55 / 0.45 / 0.35 / 0.43 cosines at 0.1 / 0.15 / 0.13 / 0.12.</p>
 *
 * <p>Scale and shadow follow {@link CrabRenderer}: the per-entity {@code getCrabScale()} (orig RenderCrab.java:39-42,
 * through the {@code applyScale} hook) and a 0.99 x 1.0 shadow, constant, never crab-scaled (ENT-S-092).</p>
 */
public final class CrabGeoReplacement extends OreSpawnGeoReplacement<Crab> {
    /** The three leg parts 1.7.10 drew eight times each (orig ModelCrab.java:195-289): the explicit-form clones are {@code <part>__i<k>}. */
    static final String[] LEG_PARTS = {"leg1", "leg2", "leg3"};
    /** The draws per leg part: {@code reference_crab}'s {@code render_instances.<part>.count}. */
    static final int DRAWS = 8;
    private static final GeoReplacementDescriptor<Crab> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.CRAB.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            Crab.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/crab.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/crab.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/crab.png"),
            CrabRenderer.SHADOW) {
        @Override
        public void applyScale(Crab entity, PoseStack poseStack, float partialTick) {
            // orig RenderCrab.preRenderScale (:39-42): GL11.glScalef(pscale, pscale, pscale) with pscale = getCrabScale()
            // (CrabRenderer.render)
            float scale = entity.getCrabScale();
            poseStack.scale(scale, scale, scale);
        }
    };

    public CrabGeoReplacement() {
        super(DESCRIPTOR);
    }

    /** The clone bone of draw {@code k} of a leg part: {@code <part>__i<k>}, the converter's explicit-form name. */
    static String clone(String part, int draw) {
        return part + "__i" + draw;
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        CrabPose entity = inputs.subject(CrabPose.class);
        float limbSwingAmount = inputs.limbSwingAmount();
        float ageInTicks = inputs.ageInTicks();
        // ModelCrab.poseFrom (the body of the classic setupAnim) verbatim: the eight poses the classic writes over the three
        // leg parts in turn (ModelCrab.java:183-234; 1.7.10's eight draws, orig :195-289), each onto its own clones.
        // pose 1 of 8 (draw 0): x 36, y 3, z 0, yaw -pi/2 + a
        draw(processor, 0, 36.0f, 3.0f, 0.0f,
                (float) (-1.5707963267948966 + (double) (Mth.cos((float) (ageInTicks * 1.7f)) * (float) Math.PI * 0.15f * limbSwingAmount)));
        // pose 2 of 8 (draw 1): z 10, yaw -pi/2 - a
        draw(processor, 1, 36.0f, 3.0f, 10.0f,
                (float) (-1.5707963267948966 - (double) (Mth.cos((float) (ageInTicks * 1.7f)) * (float) Math.PI * 0.15f * limbSwingAmount)));
        // pose 3 of 8 (draw 2): z 20, yaw -pi/2 + a
        draw(processor, 2, 36.0f, 3.0f, 20.0f,
                (float) (-1.5707963267948966 + (double) (Mth.cos((float) (ageInTicks * 1.7f)) * (float) Math.PI * 0.15f * limbSwingAmount)));
        // pose 4 of 8 (draw 3): z 30, yaw -pi/2 - a
        draw(processor, 3, 36.0f, 3.0f, 30.0f,
                (float) (-1.5707963267948966 - (double) (Mth.cos((float) (ageInTicks * 1.7f)) * (float) Math.PI * 0.15f * limbSwingAmount)));
        // pose 5 of 8 (draw 4): x -36, y 3, z 0, yaw -(-pi/2 + a)
        draw(processor, 4, -36.0f, 3.0f, 0.0f,
                (float) (-(-1.5707963267948966 + (double) (Mth.cos((float) (ageInTicks * 1.7f)) * (float) Math.PI * 0.15f * limbSwingAmount))));
        // pose 6 of 8 (draw 5): z 10, yaw -(-pi/2 - a)
        draw(processor, 5, -36.0f, 3.0f, 10.0f,
                (float) (-(-1.5707963267948966 - (double) (Mth.cos((float) (ageInTicks * 1.7f)) * (float) Math.PI * 0.15f * limbSwingAmount))));
        // pose 7 of 8 (draw 6): z 20, yaw -(-pi/2 + a)
        draw(processor, 6, -36.0f, 3.0f, 20.0f,
                (float) (-(-1.5707963267948966 + (double) (Mth.cos((float) (ageInTicks * 1.7f)) * (float) Math.PI * 0.15f * limbSwingAmount))));
        // pose 8 of 8 (draw 7): z 30, yaw -(-pi/2 - a)
        draw(processor, 7, -36.0f, 3.0f, 30.0f,
                (float) (-(-1.5707963267948966 - (double) (Mth.cos((float) (ageInTicks * 1.7f)) * (float) Math.PI * 0.15f * limbSwingAmount))));
        if (entity.getAttacking() == 0) {
            float leyeXRot = Mth.cos((float) (ageInTicks * 0.35f)) * (float) Math.PI * 0.05f;
            rotateX(processor, "Leye1", leyeXRot);
            rotateX(processor, "Leye2", leyeXRot);
            float leyeZRot = 0.54f + Mth.cos((float) (ageInTicks * 0.25f)) * (float) Math.PI * 0.05f;
            rotateZ(processor, "Leye1", leyeZRot);
            rotateZ(processor, "Leye2", leyeZRot);
            float reyeXRot = Mth.cos((float) (ageInTicks * 0.3f)) * (float) Math.PI * 0.05f;
            rotateX(processor, "Reye1", reyeXRot);
            rotateX(processor, "Reye2", reyeXRot);
            float reyeZRot = -0.54f + Mth.cos((float) (ageInTicks * 0.45f)) * (float) Math.PI * 0.05f;
            rotateZ(processor, "Reye1", reyeZRot);
            rotateZ(processor, "Reye2", reyeZRot);
            rotateY(processor, "Lmouth", -0.72f + Mth.cos((float) (ageInTicks * 0.25f)) * (float) Math.PI * 0.05f);
            rotateY(processor, "Rmouth", 0.72f - Mth.cos((float) (ageInTicks * 0.25f)) * (float) Math.PI * 0.05f);
            float newangle = Mth.cos((float) (ageInTicks * 0.15f)) * (float) Math.PI * 0.03f;
            rotateY(processor, "Lclaw3", -0.453f + newangle);
            rotateY(processor, "Lclaw4", -0.349f + newangle);
            rotateY(processor, "Lclaw5", 0.384f - newangle);
            newangle = Mth.cos((float) (ageInTicks * 0.13f)) * (float) Math.PI * 0.02f;
            rotateY(processor, "Rclaw3", 0.453f + newangle);
            rotateY(processor, "Rclaw4", 0.349f + newangle);
            rotateY(processor, "Rclaw5", -0.384f - newangle);
        } else {
            float leyeXRot = Mth.cos((float) (ageInTicks * 0.45f)) * (float) Math.PI * 0.1f;
            rotateX(processor, "Leye1", leyeXRot);
            rotateX(processor, "Leye2", leyeXRot);
            float leyeZRot = 0.54f + Mth.cos((float) (ageInTicks * 0.35f)) * (float) Math.PI * 0.1f;
            rotateZ(processor, "Leye1", leyeZRot);
            rotateZ(processor, "Leye2", leyeZRot);
            float reyeXRot = Mth.cos((float) (ageInTicks * 0.4f)) * (float) Math.PI * 0.1f;
            rotateX(processor, "Reye1", reyeXRot);
            rotateX(processor, "Reye2", reyeXRot);
            float reyeZRot = -0.54f + Mth.cos((float) (ageInTicks * 0.55f)) * (float) Math.PI * 0.1f;
            rotateZ(processor, "Reye1", reyeZRot);
            rotateZ(processor, "Reye2", reyeZRot);
            rotateY(processor, "Lmouth", -0.72f + Mth.cos((float) (ageInTicks * 0.45f)) * (float) Math.PI * 0.15f);
            rotateY(processor, "Rmouth", 0.72f - Mth.cos((float) (ageInTicks * 0.45f)) * (float) Math.PI * 0.15f);
            float newangle = Mth.cos((float) (ageInTicks * 0.35f)) * (float) Math.PI * 0.13f;
            rotateY(processor, "Lclaw3", -0.453f + newangle);
            rotateY(processor, "Lclaw4", -0.349f + newangle);
            rotateY(processor, "Lclaw5", 0.384f - newangle);
            newangle = Mth.cos((float) (ageInTicks * 0.43f)) * (float) Math.PI * 0.12f;
            rotateY(processor, "Rclaw3", 0.453f + newangle);
            rotateY(processor, "Rclaw4", 0.349f + newangle);
            rotateY(processor, "Rclaw5", -0.384f - newangle);
        }
    }

    /**
     * One of the classic's eight poses onto its three clones: {@code leg1.x = leg2.x = leg3.x = x; ... .y = y; ... .z = z}
     * (the pivot write, {@link #moveTo}) and {@code leg2.yRot = leg3.yRot = yaw; leg1.yRot = leg3.yRot}
     * ({@link #rotateY}) - on {@code leg1__i<draw>}, {@code leg2__i<draw>}, {@code leg3__i<draw>}.
     */
    private static void draw(AnimationProcessor<?> processor, int draw, float x, float y, float z, float yaw) {
        for (String part : LEG_PARTS) {
            String bone = clone(part, draw);
            moveTo(processor, bone, x, y, z);
            rotateY(processor, bone, yaw);
        }
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<Crab, CrabGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new CrabGeoReplacement());
        }
    }
}
