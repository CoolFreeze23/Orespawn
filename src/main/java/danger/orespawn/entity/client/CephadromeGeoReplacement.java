package danger.orespawn.entity.client;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Cephadrome;
import danger.orespawn.entity.pose.CephadromePose;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Cephadrome (the hooks, landed by the first Tier-1 slice T1a): {@link ModelCephadrome#poseFrom} verbatim on
 * the converted rig, ON THE HOOK (no keyframe layer, no transcription - the self-gate stays closed until an
 * artist delivers {@code idle} and {@code walk}). Wingspeed 0.55f (orig ModelCephadrome.java:15,68 /
 * ClientProxyOreSpawn.java:446). The idioms, all read through {@link CephadromePose} : the walk amplitude from the
 * MOVEMENT DELTA - above a walking speed of a thousandth {@code cos(age * 0.75f * ws) * PI * lspeed * 0.4f} with
 * {@code lspeed} the horizontal distance moved this tick, clamped to +-0.75 past +-0.5 (orig :384-396); the ACTIVITY
 * branch on the eight leg parts (flying: a constant 1.0 lift on both sides; else the gait, the left side the
 * negative; orig :397-416); the ACTIVITY / ATTACKING ternary on the ten wing parts about Z (0.55 x 0.28 flying, -0.85
 * + 0.2 x 0.028 at rest, -0.65 + 0.9 x 0.068 attacking; orig :417-427); the {@code |cos|} idiom on the eight top fins /
 * membranes about X, halved down the row (orig :428-436); the tail rhythm switched by the flags (0.76 / 0.1
 * active, 0.22 / 0.03 idle; orig :437-441) with the POSITION-write idiom (through {@link #moveTo} ) - each tail
 * segment FOLLOWS the previous 13 / 13 / 10 units along (sin, cos) of its yaw with the classic's x nudges, the
 * seven tail fins / membranes copying the fin's pivot and yaw (orig :442-468); the RIDDEN-FLIGHT NECK LATCH (orig
 * :469-482, the Rotator's precedent): while activity is 1 the head yaw is the negated body-yaw delta x 10 eased into
 * the per-entity {@link RenderInfo} 's {@code rf1} by a fiftieth and clamped to +-50, else {@code netHeadYaw / 2} ;
 * the HEAD-LOOK idiom about Y at 0.125 / 0.25 / 0.5 / 0.75 of the radians on the three necks and the head, each pivot
 * following the previous 14 / 14 / 8 units back, the two hammerheads and the mouth copying the head (orig :483-502); and
 * the ATTACKING branch on the mouth's pitch (orig :503). The tail's and neck3's pivots are never written (the
 * bind), read through {@link #classicPosition} ; every value the classic reads back from a part it just wrote is
 * held in a local; a part whose x / z the classic writes keeps its bind y. Orig :504's {@code e.setRenderInfo(r)} is the
 * port's omitted self-copy (ENT-S-093).
 * <p>Shadow follows {@link CephadromeRenderer} : a 1.25 x 1.0 shadow (ENT-S-092; the constructor literal - the renderer
 * declares no SHADOW constant); the classic renderer scales by 1.0 (its private SCALE), so no scale hook. The rig has
 * zero-thickness cubes (the wing fins and membranes), so the shipped geo carries the classic within-cube face order
 * ({@link FaceOrder#KEY}; TEST-007) and the seam expects it.</p>
 */
public final class CephadromeGeoReplacement extends OreSpawnGeoReplacement<Cephadrome> {
    /** orig ModelCephadrome.java:15,68 {@code wingspeed} = 0.55f (ClientProxyOreSpawn.java:446): the chain's third multiply. */
    static final float WINGSPEED = 0.55F;
    private static final GeoReplacementDescriptor<Cephadrome> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.CEPHADROME.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            Cephadrome.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/cephadrome.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/cephadrome.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/cephadrome.png"),
            // orig RenderCephadrome.java:23 super(model, par2 * par3) with 1.25f x 1.0f: CephadromeRenderer's constructor
            // passes the literal 1.25f (it declares no SHADOW constant)
            1.25F) {
        /** Nine zero-thickness cubes (the wing fins and membranes): the seam expects {@link FaceOrder#KEY} in the shipped geo. */
        @Override
        public boolean cubeFaceOrderRequired() {
            return true;
        }
    };

    public CephadromeGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        CephadromePose entity = inputs.subject(CephadromePose.class);
        float limbSwingAmount = inputs.limbSwingAmount();
        float ageInTicks = inputs.ageInTicks();
        float netHeadYaw = inputs.netHeadYaw();
        // ModelCephadrome.poseFrom (the body of the classic setupAnim) verbatim, the float / double chain as the classic casts it.
        float newangle = 0.0f;
        float lspeed = 0.0f;
        float pi4 = 0.7853982f;
        float tailspeed = 0.76f;
        float tailamp = 0.1f;
        // ENT-S-093: per-entity scratch as in the original (orig Cephadrome.java:62, orig ModelCephadrome.java:375,383).
        RenderInfo r = entity.getRenderInfo();

        if ((double) limbSwingAmount > 0.001) {
            lspeed = (float) ((entity.xOld() - entity.getX()) * (entity.xOld() - entity.getX()) + (entity.zOld() - entity.getZ()) * (entity.zOld() - entity.getZ()));
            lspeed = (float) Math.sqrt(lspeed);
            newangle = Mth.cos(ageInTicks * 0.75f * WINGSPEED) * (float) Math.PI * lspeed * 0.4f;
            if ((double) newangle > 0.5) {
                newangle = 0.75f;
            }
            if ((double) newangle < -0.5) {
                newangle = -0.75f;
            }
        } else {
            newangle = 0.0f;
        }

        if (entity.getActivity() != 0) {
            newangle = 1.0f;
            rotateX(processor, "rightleg1", -0.58f + newangle);
            rotateX(processor, "rightleg2", 0.98f + newangle);
            rotateX(processor, "rightleg3", -0.52f + newangle);
            rotateX(processor, "rightfoot", newangle);
            rotateX(processor, "leftleg1", -0.58f + newangle);
            rotateX(processor, "leftleg2", 0.98f + newangle);
            rotateX(processor, "leftleg3", -0.52f + newangle);
            rotateX(processor, "leftfoot", newangle);
        } else {
            rotateX(processor, "rightleg1", -0.58f + newangle);
            rotateX(processor, "rightleg2", 0.98f + newangle);
            rotateX(processor, "rightleg3", -0.52f + newangle);
            rotateX(processor, "rightfoot", newangle);
            rotateX(processor, "leftleg1", -0.58f - newangle);
            rotateX(processor, "leftleg2", 0.98f - newangle);
            rotateX(processor, "leftleg3", -0.52f - newangle);
            rotateX(processor, "leftfoot", -newangle);
        }

        newangle = entity.getActivity() != 0 ? Mth.cos(ageInTicks * 0.55f * WINGSPEED) * (float) Math.PI * 0.28f : (entity.getAttacking() == 0 ? -0.85f + Mth.cos(ageInTicks * 0.2f * WINGSPEED) * (float) Math.PI * 0.028f : -0.65f + Mth.cos(ageInTicks * 0.9f * WINGSPEED) * (float) Math.PI * 0.068f);
        rotateZ(processor, "lefwingfin1", newangle);
        rotateZ(processor, "leftwingfin2", newangle);
        rotateZ(processor, "leftwingfin3", newangle);
        rotateZ(processor, "leftwingfin4", newangle);
        rotateZ(processor, "leftwingmembrane", newangle);
        rotateZ(processor, "rightwingfin1", -newangle);
        rotateZ(processor, "rightwingfin2", -newangle);
        rotateZ(processor, "rightwingfin3", -newangle);
        rotateZ(processor, "rightwingfin4", -newangle);
        rotateZ(processor, "rightwingmembrane", -newangle);

        newangle = Mth.cos(ageInTicks * 0.15f * WINGSPEED) * (float) Math.PI * 0.05f;
        rotateX(processor, "topfin1", -1.85f - Math.abs(newangle));
        rotateX(processor, "topmem1", -0.26f - Math.abs(newangle));
        rotateX(processor, "topfin2", -2.07f - Math.abs(newangle / 2.0f));
        rotateX(processor, "topmem2", -0.52f - Math.abs(newangle / 2.0f));
        rotateX(processor, "topfin3", -2.42f - Math.abs(newangle / 4.0f));
        rotateX(processor, "topmem3", -0.89f - Math.abs(newangle / 4.0f));
        rotateX(processor, "topfin4", -2.63f - Math.abs(newangle / 8.0f));
        rotateX(processor, "topmem4", -1.11f - Math.abs(newangle / 8.0f));

        if (entity.getActivity() == 0 && entity.getAttacking() == 0) {
            tailspeed = 0.22f;
            tailamp = 0.03f;
        }

        float tail1YRot = Mth.cos(ageInTicks * tailspeed * WINGSPEED) * (float) Math.PI * 0.04f;
        rotateY(processor, "tail1", tail1YRot);
        float[] tail1 = classicPosition(bone(processor, "tail1"));  // never written: the bind pivot
        float tail2Z = tail1[2] + (float) Math.cos(tail1YRot) * 13.0f;
        float tail2X = tail1[0] + 1.5f + (float) Math.sin(tail1YRot) * 13.0f;
        moveXZ(processor, "tail2", tail2X, tail2Z);
        float tail2YRot = Mth.cos(ageInTicks * tailspeed * WINGSPEED - pi4) * (float) Math.PI * tailamp;
        rotateY(processor, "tail2", tail2YRot);
        float tail3Z = tail2Z + (float) Math.cos(tail2YRot) * 13.0f;
        float tail3X = tail2X - 0.5f + (float) Math.sin(tail2YRot) * 13.0f;
        moveXZ(processor, "tail3", tail3X, tail3Z);
        float tail3YRot = Mth.cos(ageInTicks * tailspeed * WINGSPEED - 2.0f * pi4) * (float) Math.PI * tailamp;
        rotateY(processor, "tail3", tail3YRot);

        float tailfin1Z = tail3Z + (float) Math.cos(tail3YRot) * 10.0f;
        float tailfin1X = tail3X - 1.0f + (float) Math.sin(tail3YRot) * 10.0f;
        moveXZ(processor, "tailfin1", tailfin1X, tailfin1Z);
        float tailfin1YRot = Mth.cos(ageInTicks * tailspeed * WINGSPEED - 3.0f * pi4) * (float) Math.PI * tailamp;
        rotateY(processor, "tailfin1", tailfin1YRot);

        moveXZ(processor, "tailfin2", tailfin1X, tailfin1Z);
        rotateY(processor, "tailfin2", tailfin1YRot);
        moveXZ(processor, "tailfin3", tailfin1X, tailfin1Z);
        rotateY(processor, "tailfin3", tailfin1YRot);
        moveXZ(processor, "tailfin4", tailfin1X, tailfin1Z);
        rotateY(processor, "tailfin4", tailfin1YRot);
        moveXZ(processor, "tailmembrane1", tailfin1X, tailfin1Z);
        rotateY(processor, "tailmembrane1", tailfin1YRot);
        moveXZ(processor, "tailmembrane2", tailfin1X, tailfin1Z);
        rotateY(processor, "tailmembrane2", tailfin1YRot);
        moveXZ(processor, "tailmembrane3", tailfin1X, tailfin1Z);
        rotateY(processor, "tailmembrane3", tailfin1YRot);

        float headYaw = netHeadYaw;
        // orig ModelCephadrome.java:469-482 - ridden flight latches the BODY yaw delta (orig :470 field_70126_B -
        // field_70177_z = yRotO - getYRot()) into the entity's rf1 with a +/-50 clamp (ENT-S-093).
        if (entity.getActivity() == 1) {
            headYaw = (entity.yRotO() - entity.getYRot()) * 10.0f;
            headYaw = -headYaw;
            r.rf1 += (headYaw - r.rf1) / 50.0f;
            if (r.rf1 > 50.0f) {
                r.rf1 = 50.0f;
            }
            if (r.rf1 < -50.0f) {
                r.rf1 = -50.0f;
            }
            headYaw = r.rf1;
        } else {
            headYaw /= 2.0f;
        }
        // orig ModelCephadrome.java:504 e.setRenderInfo(r) omitted: r is the entity's own instance (ENT-S-093).

        float neck3YRot = (float) Math.toRadians(headYaw) * 0.125f;
        rotateY(processor, "neck3", neck3YRot);
        float[] neck3 = classicPosition(bone(processor, "neck3"));  // never written: the bind pivot
        float neck2Z = neck3[2] - (float) Math.cos(neck3YRot) * 14.0f;
        float neck2X = neck3[0] + 0.5f - (float) Math.sin(neck3YRot) * 14.0f;
        moveXZ(processor, "neck2", neck2X, neck2Z);
        float neck2YRot = (float) Math.toRadians(headYaw) * 0.25f;
        rotateY(processor, "neck2", neck2YRot);
        float neck1Z = neck2Z - (float) Math.cos(neck2YRot) * 14.0f;
        float neck1X = neck2X + 0.5f - (float) Math.sin(neck2YRot) * 14.0f;
        moveXZ(processor, "neck1", neck1X, neck1Z);
        float neck1YRot = (float) Math.toRadians(headYaw) * 0.5f;
        rotateY(processor, "neck1", neck1YRot);
        float headZ = neck1Z - (float) Math.cos(neck1YRot) * 8.0f;
        float headX = neck1X - (float) Math.sin(neck1YRot) * 8.0f;
        moveXZ(processor, "head", headX, headZ);
        float headYRot = (float) Math.toRadians(headYaw) * 0.75f;
        rotateY(processor, "head", headYRot);

        moveXZ(processor, "hammerhead", headX, headZ);
        rotateY(processor, "hammerhead", headYRot);
        moveXZ(processor, "hammerhead2", headX, headZ);
        rotateY(processor, "hammerhead2", headYRot);
        moveXZ(processor, "mouth", headX, headZ);
        rotateY(processor, "mouth", headYRot);

        newangle = Mth.cos(ageInTicks * 0.5f * WINGSPEED) * (float) Math.PI * 0.14f;
        rotateX(processor, "mouth", entity.getAttacking() != 0 ? -0.61f + newangle : -0.87f);
    }

    /** {@code part.x = x; part.z = z} in classic terms: the classic writes x and z only, so y keeps the part's bind. */
    private static void moveXZ(AnimationProcessor<?> processor, String name, float x, float z) {
        float[] current = classicPosition(bone(processor, name));
        moveTo(processor, name, x, current[1], z);
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<Cephadrome, CephadromeGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new CephadromeGeoReplacement());
        }
    }
}
