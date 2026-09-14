package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.CreepingHorror;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Creeping Horror (the hooks, landed by the fourth Tier-2 slice T2d): {@link
 * ModelCreepingHorror#setupAnim} verbatim on the converted rig, ON THE HOOK (no
 * keyframe layer, no transcription - the self-gate stays closed until an artist delivers {@code idle} and {@code walk}).
 * No wingspeed (orig ModelCreepingHorror.java has none): the GAIT-scaled idiom on the eight leg parts about Y - {@code
 * cos(age * 1.25f) * PI * 0.35f * limbSwingAmount} around +-0.576 rad (orig :272-277; no threshold); the four pincer
 * parts about Y on a 0.48 cosine x 0.15 (orig :278-281); the {@code |cos|} idiom on the three tail segments about X -
 * {@code |cos(age * 0.11f) * PI * 0.25f|} around -0.55 / 0 / -0.22 rad (orig :282-286); and the five spikes on their own
 * 0.08 cosines about all three axes - X at 0.81 / 0.87 / 0.99 / 0.103 / 0.107 around 0.7 rad, Y at 1.11 / 1.17 / 1.25 /
 * 1.28 / 1.31, Z at 1.41 / 1.47 / 1.55 / 1.58 / 1.61 (orig :287-306).
 *
 * <p>Scale and shadow follow {@link CreepingHorrorRenderer}: 0.75 render scale and a 0.45 x 0.75 shadow (ENT-S-092). The
 * rig has zero-thickness cubes (seven, among the pincers and spikes), so the shipped geo carries the classic within-cube
 * face order ({@link FaceOrder#KEY}; TEST-007) and the seam expects it.</p>
 */
public final class CreepingHorrorGeoReplacement extends OreSpawnGeoReplacement<CreepingHorror> {
    private static final GeoReplacementDescriptor<CreepingHorror> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.CREEPING_HORROR.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            CreepingHorror.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/creepinghorror.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/creepinghorror.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/creepinghorror.png"),
            CreepingHorrorRenderer.SHADOW) {
        @Override
        public void applyScale(CreepingHorror entity, PoseStack poseStack, float partialTick) {
            // orig RenderCreepingHorror.preRenderScale (:39-45): GL11.glScalef(scale, scale, scale) (CreepingHorrorRenderer.scale)
            poseStack.scale(CreepingHorrorRenderer.SCALE, CreepingHorrorRenderer.SCALE, CreepingHorrorRenderer.SCALE);
        }

        /** Seven zero-thickness cubes (pincer and spike blades): the seam expects {@link FaceOrder#KEY} in the shipped geo. */
        @Override
        public boolean cubeFaceOrderRequired() {
            return true;
        }
    };

    public CreepingHorrorGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        float limbSwingAmount = inputs.limbSwingAmount();
        float ageInTicks = inputs.ageInTicks();
        // ModelCreepingHorror.setupAnim verbatim, the float chain left to right.
        float newangle = Mth.cos((float) (ageInTicks * 1.25f)) * (float) Math.PI * 0.35f * limbSwingAmount;
        rotateY(processor, "leg1", 0.576f + newangle);
        rotateY(processor, "leg1part2", 0.576f + newangle);
        rotateY(processor, "leg2", -0.576f - newangle);
        rotateY(processor, "leg2part2", -0.576f - newangle);
        rotateY(processor, "leg3", -0.576f - newangle);
        rotateY(processor, "leg3part2", -0.576f - newangle);
        rotateY(processor, "leg4", 0.576f + newangle);
        rotateY(processor, "leg4part2", 0.576f + newangle);
        newangle = Mth.cos((float) (ageInTicks * 0.48f)) * (float) Math.PI * 0.15f;
        rotateY(processor, "pincer1", newangle);
        rotateY(processor, "pincer1part2", newangle);
        rotateY(processor, "pincer2", -newangle);
        rotateY(processor, "pincer2part2", -newangle);
        newangle = Mth.cos((float) (ageInTicks * 0.11f)) * (float) Math.PI * 0.25f;
        newangle = Math.abs(newangle);
        rotateX(processor, "tailseg1", -0.55f + newangle);
        rotateX(processor, "tailseg3", -0.22f + newangle);
        rotateX(processor, "tailseg2", newangle);
        newangle = Mth.cos((float) (ageInTicks * 0.81f)) * (float) Math.PI * 0.08f;
        rotateX(processor, "spike1", 0.7f + newangle);
        newangle = Mth.cos((float) (ageInTicks * 0.87f)) * (float) Math.PI * 0.08f;
        rotateX(processor, "spike2", 0.7f + newangle);
        newangle = Mth.cos((float) (ageInTicks * 0.99f)) * (float) Math.PI * 0.08f;
        rotateX(processor, "spike3", 0.7f + newangle);
        newangle = Mth.cos((float) (ageInTicks * 0.103f)) * (float) Math.PI * 0.08f;
        rotateX(processor, "spike4", 0.7f + newangle);
        newangle = Mth.cos((float) (ageInTicks * 0.107f)) * (float) Math.PI * 0.08f;
        rotateX(processor, "spike5", 0.7f + newangle);
        rotateY(processor, "spike1", newangle = Mth.cos((float) (ageInTicks * 1.11f)) * (float) Math.PI * 0.08f);
        rotateY(processor, "spike2", newangle = Mth.cos((float) (ageInTicks * 1.17f)) * (float) Math.PI * 0.08f);
        rotateY(processor, "spike3", newangle = Mth.cos((float) (ageInTicks * 1.25f)) * (float) Math.PI * 0.08f);
        rotateY(processor, "spike4", newangle = Mth.cos((float) (ageInTicks * 1.28f)) * (float) Math.PI * 0.08f);
        rotateY(processor, "spike5", newangle = Mth.cos((float) (ageInTicks * 1.31f)) * (float) Math.PI * 0.08f);
        rotateZ(processor, "spike1", newangle = Mth.cos((float) (ageInTicks * 1.41f)) * (float) Math.PI * 0.08f);
        rotateZ(processor, "spike2", newangle = Mth.cos((float) (ageInTicks * 1.47f)) * (float) Math.PI * 0.08f);
        rotateZ(processor, "spike3", newangle = Mth.cos((float) (ageInTicks * 1.55f)) * (float) Math.PI * 0.08f);
        rotateZ(processor, "spike4", newangle = Mth.cos((float) (ageInTicks * 1.58f)) * (float) Math.PI * 0.08f);
        rotateZ(processor, "spike5", newangle = Mth.cos((float) (ageInTicks * 1.61f)) * (float) Math.PI * 0.08f);
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<CreepingHorror, CreepingHorrorGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new CreepingHorrorGeoReplacement());
        }
    }
}
