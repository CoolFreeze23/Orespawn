package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityBee;
import danger.orespawn.entity.pose.BeePose;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Bee (the third Tier-2 slice, 2026-09-13): {@link BeeModel#poseFrom} verbatim on the converted rig, ON THE
 * HOOK (owner 2026-09-13, Amendment 2 to Amendment 1: no keyframe layer, no transcription - the self-gate stays closed
 * until an artist delivers {@code idle} and {@code walk}). Wingspeed 2.0f (orig ModelBee.java:14,40 /
 * ClientProxyOreSpawn.java:425): the wings about Z at 1.1f, the pincers about Y at 0.3f, the left antenna's three
 * parts and pom about X at 0.21f and about Z at 0.31f, the right ones at 0.27f / 0.37f, and the ABDOMEN CURL - an
 * attacking-branch idiom (orig :214 {@code getAttacking() == 0}: 0.021f x PI x 0.023f at rest, 0.11f x PI x 0.055f
 * attacking) folded down the five-part chain with a -0.35f step and a POSITION follow (each part's y / z is the
 * previous part's plus (cos, sin) of its pitch times the segment length 10 / 10 / 6 / 5 / 7; orig :215-230), written
 * through {@link #moveTo}. The entity is read through {@link BeePose} (the S4 doctrine; the harness poses both sides
 * from a declared state).
 *
 * <p>Scale and shadow follow {@link BeeRenderer}: 1.1 render scale and a 0.9 x 1.1 shadow (ENT-S-092).</p>
 */
public final class BeeGeoReplacement extends OreSpawnGeoReplacement<EntityBee> {
    /** orig ModelBee.java:14,40 {@code wingspeed} = 2.0f (ClientProxyOreSpawn.java:425): the chain's second multiply. */
    static final float WINGSPEED = 2.0F;
    private static final GeoReplacementDescriptor<EntityBee> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.ENTITY_BEE.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            EntityBee.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/bee.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/bee.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/bee.png"),
            BeeRenderer.SHADOW) {
        @Override
        public void applyScale(EntityBee entity, PoseStack poseStack, float partialTick) {
            // orig RenderBee.preRenderScale: GL11.glScalef(scale, scale, scale) (BeeRenderer.scale)
            poseStack.scale(BeeRenderer.SCALE, BeeRenderer.SCALE, BeeRenderer.SCALE);
        }

        /**
         * A rig with zero-thickness cubes (the two wings, 0 x 8 x 24): their two coplanar faces z-fight and the winner
         * is the face emitted last, so the shipped geo carries the classic within-cube order ({@link FaceOrder#KEY};
         * the FaceOrder contract's open item for a cutout rig, TEST-007) and the seam expects it.
         */
        @Override
        public boolean cubeFaceOrderRequired() {
            return true;
        }
    };

    public BeeGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        BeePose entity = inputs.subject(BeePose.class);
        float ageInTicks = inputs.ageInTicks();
        // BeeModel.poseFrom (the body of the classic setupAnim) verbatim, the float chain left to right.
        float newangle = Mth.cos(ageInTicks * 1.1f * WINGSPEED) * (float) Math.PI * 0.3f;
        rotateZ(processor, "WingLeft", -1.745f - newangle);
        rotateZ(processor, "WingRight", 1.754f + newangle);
        newangle = Mth.cos(ageInTicks * 0.3f * WINGSPEED) * (float) Math.PI * 0.1f;
        rotateY(processor, "LeftPincerMain", -0.274f + newangle);
        rotateY(processor, "LeftPincerExtra", -0.274f + newangle);
        rotateY(processor, "RightPincerMain", 0.274f - newangle);
        rotateY(processor, "RightPincerExtra", 0.274f - newangle);
        newangle = Mth.cos(ageInTicks * 0.21f * WINGSPEED) * (float) Math.PI * 0.06f;
        rotateX(processor, "LA1", 0.261f + newangle);
        rotateX(processor, "LA2", 0.436f + newangle);
        rotateX(processor, "LA3", 0.611f + newangle);
        rotateX(processor, "LeftPom", newangle);
        newangle = Mth.cos(ageInTicks * 0.27f * WINGSPEED) * (float) Math.PI * 0.06f;
        rotateX(processor, "RA1", 0.261f + newangle);
        rotateX(processor, "RA2", 0.436f + newangle);
        rotateX(processor, "RA3", 0.611f + newangle);
        rotateX(processor, "RightPom", newangle);
        newangle = Mth.cos(ageInTicks * 0.31f * WINGSPEED) * (float) Math.PI * 0.06f;
        rotateZ(processor, "LA1", newangle);
        rotateZ(processor, "LA2", newangle);
        rotateZ(processor, "LA3", newangle);
        rotateZ(processor, "LeftPom", newangle);
        newangle = Mth.cos(ageInTicks * 0.37f * WINGSPEED) * (float) Math.PI * 0.06f;
        rotateZ(processor, "RA1", newangle);
        rotateZ(processor, "RA2", newangle);
        rotateZ(processor, "RA3", newangle);
        rotateZ(processor, "RightPom", newangle);
        // The abdomen curl: the attacking branch (orig :214), then the chain. The classic reads Abdomnem5's y / z
        // (never written, so the bind pivot) and each part's pitch and position it just assigned; those are held in
        // locals exactly as the classic's fields hold them.
        newangle = entity.getAttacking() == 0
                ? Mth.cos(ageInTicks * 0.021f * WINGSPEED) * (float) Math.PI * 0.023f
                : Mth.cos(ageInTicks * 0.11f * WINGSPEED) * (float) Math.PI * 0.055f;
        float abdomen5X = 1.099f + newangle;
        rotateX(processor, "Abdomnem5", abdomen5X);
        float[] abdomen5 = classicPosition(bone(processor, "Abdomnem5"));
        float abdomen4X = abdomen5X + newangle - 0.35f;
        rotateX(processor, "Abdomnem4", abdomen4X);
        float abdomen4Y = (float) ((double) abdomen5[1] + Math.cos(abdomen5X) * 10.0);
        float abdomen4Z = (float) ((double) abdomen5[2] + Math.sin(abdomen5X) * 10.0);
        moveTo(processor, "Abdomnem4", classicPosition(bone(processor, "Abdomnem4"))[0], abdomen4Y, abdomen4Z);
        float abdomen3X = abdomen4X + newangle - 0.35f;
        rotateX(processor, "Abdomnem3", abdomen3X);
        float abdomen3Y = (float) ((double) abdomen4Y + Math.cos(abdomen4X) * 10.0);
        float abdomen3Z = (float) ((double) abdomen4Z + Math.sin(abdomen4X) * 10.0);
        moveTo(processor, "Abdomnem3", classicPosition(bone(processor, "Abdomnem3"))[0], abdomen3Y, abdomen3Z);
        float abdomen2X = abdomen3X + newangle - 0.35f;
        rotateX(processor, "Abdomnem2", abdomen2X);
        float abdomen2Y = (float) ((double) abdomen3Y + Math.cos(abdomen3X) * 6.0);
        float abdomen2Z = (float) ((double) abdomen3Z + Math.sin(abdomen3X) * 6.0);
        moveTo(processor, "Abdomnem2", classicPosition(bone(processor, "Abdomnem2"))[0], abdomen2Y, abdomen2Z);
        float abdomen1X = abdomen2X + newangle - 0.35f;
        rotateX(processor, "Abdomnem1", abdomen1X);
        float abdomen1Y = (float) ((double) abdomen2Y + Math.cos(abdomen2X) * 5.0);
        float abdomen1Z = (float) ((double) abdomen2Z + Math.sin(abdomen2X) * 5.0);
        moveTo(processor, "Abdomnem1", classicPosition(bone(processor, "Abdomnem1"))[0], abdomen1Y, abdomen1Z);
        float stingX = abdomen1X + newangle - 0.35f;
        rotateX(processor, "Sting", stingX);
        float stingY = (float) ((double) abdomen1Y + Math.cos(abdomen1X) * 7.0);
        float stingZ = 1.0f + (float) ((double) abdomen1Z + Math.sin(abdomen1X) * 7.0);
        moveTo(processor, "Sting", classicPosition(bone(processor, "Sting"))[0], stingY, stingZ);
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<EntityBee, BeeGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new BeeGeoReplacement());
        }
    }
}
